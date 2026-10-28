"""Budding Blocks tab: every block in the pack that grows crystal buds, its bud stages, and what each stage drops.

Families come from the item dumps (kubejs/exported/items_by_mod), drops from the runtime loot dumps
(kubejs/exported/loot_tables/<ns>.json, so datapack overrides count). Growth rules are hardcoded per mod and were
read from the jars (javap, 2026-10-22):
  - vanilla BuddingAmethystBlock, Geode Forge Budding*Block (one class per mineral, stone variants share it),
    AE2 BuddingCertusQuartzBlock: random tick, 1 in 5 (AE2 GROWTH_CHANCE = 5), pick a random face; air or water
    there gets a small bud, a bud there steps up small -> medium -> large -> cluster.
  - AE2: after a growth, a non-flawless budding block has 1 in 12 (DECAY_CHANCE) to step down
    flawed -> chipped -> damaged -> certus quartz block.
  - Quark CorundumBlock: not waxed, below y 24, air above, fewer than 4 corundum below; 1 in
    [world.corundum] "Cave Crystal Growth Chance" random ticks it grows a block upward and may sprout a cluster
    on an open side (config/quark-common.toml).
Rock salt clusters (salt mod) are left out: Salt's own JEI pages show crystal growing.
"""
import re

from .common import ROOT, block, blocks, item, row, tag

TAB = 'budding'
PICKAXE_TAG = 'minecraft:cluster_max_harvestables'
PISTON_NOTE = 'A piston breaks it.'
GROW_NOTE = 'Random tick, 1 in 5: a bud grows or steps up on an open face.'


def _ids(ctx, mod):
    data = ctx.exported(f'items_by_mod/{mod}.json')
    return set(data.get('items', [])) if data else set()


def _loot(ctx, block_id, cache):
    ns, path = block_id.split(':', 1)
    if ns not in cache:
        cache[ns] = ctx.exported(f'loot_tables/{ns}.json') or {}
    table = cache[ns].get(f'blocks/{path}')
    if table is None:
        ctx.warn(f'budding: no loot table for {block_id}')
    return table or {}


# Loot summary ----------------------------------------------------------------------------------------------

def _cond_label(conditions):
    """('Silk Touch' | 'Pickaxe' | None, skip) for an entry's conditions."""
    label = None
    for c in conditions or []:
        kind = c.get('condition', '').split(':')[-1]
        if kind == 'match_tool':
            pred = c.get('predicate', {})
            ench = [e.get('enchantment') for e in pred.get('enchantments', [])]
            if 'minecraft:silk_touch' in ench:
                label = 'Silk Touch'
            elif pred.get('tag') == PICKAXE_TAG:
                label = 'Pickaxe'
            else:
                label = 'Some tools'
        elif kind in ('survives_explosion', 'block_state_property'):
            continue
        elif kind == 'inverted':
            label = 'Without Silk Touch'
    return label


def _count(functions):
    count, fortune = 1, False
    for f in functions or []:
        kind = f.get('function', '').split(':')[-1]
        if kind == 'set_count':
            c = f.get('count')
            if isinstance(c, (int, float)):
                count = int(c)
            elif isinstance(c, dict) and 'min' in c:
                count = (int(c['min']), int(c['max']))
        elif kind == 'apply_bonus' and f.get('enchantment') == 'minecraft:fortune':
            fortune = True
    return count, fortune


def _walk(entry, inherited, out):
    """Alternatives: each child applies only when the ones before it didn't."""
    label = _cond_label(entry.get('conditions')) or inherited
    etype = entry.get('type', '').split(':')[-1]
    if etype == 'alternatives':
        for n, child in enumerate(entry.get('children', [])):
            child_label = _cond_label(child.get('conditions'))
            if child_label is None and n > 0:
                # the fallback branch: name it after what the earlier branches needed
                earlier = [_cond_label(c.get('conditions')) for c in entry['children'][:n]]
                child_label = 'Any other tool' if 'Pickaxe' not in earlier else 'Other tool or hand'
            _walk(child, child_label or label, out)
    elif etype == 'item':
        count, fortune = _count(entry.get('functions'))
        out.append({'id': entry['name'], 'count': count, 'fortune': fortune, 'label': label})


def drops(table):
    out = []
    for pool in table.get('pools', []):
        pool_label = _cond_label(pool.get('conditions'))
        for entry in pool.get('entries', []):
            _walk(entry, pool_label, out)
    return out


def _drop_ing(d):
    count = d['count']
    label_parts = [d['label'] or 'Any tool']
    if isinstance(count, tuple):
        label_parts.append(f'{count[0]} to {count[1]}')
        count = count[1]
    if d['fortune']:
        label_parts.append('Fortune adds more')
    ing = item(d['id'], label=', '.join(label_parts))
    if count != 1:
        ing['count'] = count
    return ing


def _harvest_row(ctx, cache, block_ids, requires, extra_notes=()):
    """Cluster (or any stage) -> its drops, labelled by the tool each needs."""
    table = _loot(ctx, block_ids[0], cache)
    found = drops(table)
    if not found:
        return row([blocks(block_ids)], [block('minecraft:air')],
                   notes=['Drops nothing, even with Silk Touch.', *extra_notes], requires=requires)
    outs = [_drop_ing(d) for d in found][:6]
    notes = list(extra_notes)
    if all(d['label'] == 'Silk Touch' for d in found):
        notes.insert(0, 'Only with Silk Touch.')
    return row([blocks(block_ids)], outs, action=tag(PICKAXE_TAG) if any(d['label'] == 'Pickaxe' for d in found) else None,
               notes=notes or None, requires=requires)


def _silk_only(ctx, cache, ids):
    """True when every block drops only itself under Silk Touch (buds)."""
    for bid in ids:
        found = drops(_loot(ctx, bid, cache))
        if not (len(found) == 1 and found[0]['label'] == 'Silk Touch' and found[0]['id'] == bid):
            return False
    return True


def _family_rows(ctx, cache, budding_ids, stages, cluster, requires, grow_notes):
    rows = [row([blocks(budding_ids)], [block(s) for s in stages] + [block(cluster)],
                action=None, notes=grow_notes, requires=requires)]
    # budding block itself
    rows.append(_harvest_row(ctx, cache, budding_ids, requires, [PISTON_NOTE]))
    # buds
    if _silk_only(ctx, cache, stages):
        rows.append(row([blocks(stages)], [blocks(stages, label='Silk Touch')], linked=True,
                        notes=['Buds drop only with Silk Touch.'], requires=requires))
    else:
        for s in stages:
            rows.append(_harvest_row(ctx, cache, [s], requires))
    rows.append(_harvest_row(ctx, cache, [cluster], requires))
    return rows


# Families --------------------------------------------------------------------------------------------------

def _vanilla(ctx, cache):
    stages = [f'minecraft:{s}_amethyst_bud' for s in ('small', 'medium', 'large')]
    return _family_rows(ctx, cache, ['minecraft:budding_amethyst'], stages, 'minecraft:amethyst_cluster', None,
                        [GROW_NOTE])


def _ae2(ctx, cache):
    ids = _ids(ctx, 'ae2')
    if not ids:
        return []
    req = ['ae2']
    tiers = ['flawless', 'flawed', 'chipped', 'damaged']
    budding = [f'ae2:{t}_budding_quartz' for t in tiers]
    stages = [f'ae2:{s}_quartz_bud' for s in ('small', 'medium', 'large')]
    rows = _family_rows(ctx, cache, budding, stages, 'ae2:quartz_cluster', req,
                        [GROW_NOTE, 'Every tier grows alike.'])
    # the shared drops row is wrong for AE2 (each tier drops something else): one row per tier instead
    rows = [r for r in rows if r['inputs'] != [blocks(budding)] or 'action' in r or r['outputs'][0].get('block')]
    for n, b in enumerate(budding):
        rows.insert(1 + n, _harvest_row(ctx, cache, [b], req, [PISTON_NOTE]))
    decay = [block(b) for b in budding[2:]] + [block('ae2:quartz_block')]
    rows.insert(1, row([block(budding[1])], decay, notes=[
        'Each growth: 1 in 12 to drop a tier. Flawless never decays.'], requires=req))
    return rows


def _geode(ctx, cache):
    ids = _ids(ctx, 'geode')
    if not ids:
        return []
    families = sorted(m.group(1) for i in ids if (m := re.fullmatch(r'geode:small_([a-z_]+)_bud', i)))
    by_family = {}
    for bid in sorted(i for i in ids if i.startswith('geode:budding_')):
        rest = bid[len('geode:budding_'):]
        tail = rest.split('_', 1)[1] if '_' in rest else ''
        fam = next((f for f in families if f == rest), None) \
            or next((f for f in families if f == 'nether_' + tail), None) \
            or next((f for f in families if f == tail), None)
        if fam is None:
            ctx.warn(f'budding: no bud family for {bid}')
            continue
        by_family.setdefault(fam, []).append(bid)
    rows = []
    for fam, budding in sorted(by_family.items()):
        cluster = next((c for c in (f'geode:{fam}_cluster', f'geode:{fam}_crystal') if c in ids), None)
        if cluster is None:
            ctx.warn(f'budding: no cluster for geode {fam}')
            continue
        # base variant first, then the stone variants (same class, same buds)
        budding.sort(key=lambda b: (b.count('_') - (1 if fam in b else 0), b))
        stages = [f'geode:{s}_{fam}_bud' for s in ('small', 'medium', 'large')]
        notes = [GROW_NOTE]
        rows += _family_rows(ctx, cache, budding, stages, cluster, ['geode'], notes)
    return rows


def _quark_growth_chance():
    cfg = ROOT / 'config' / 'quark-common.toml'
    if not cfg.exists():
        return None
    in_section = False
    for line in cfg.read_text(encoding='utf-8').splitlines():
        s = line.strip()
        if s.startswith('['):
            in_section = s == '[world.corundum]'
        elif in_section and s.startswith('"Cave Crystal Growth Chance"'):
            return int(s.split('=', 1)[1])
    return None


def _quark(ctx, cache):
    ids = _ids(ctx, 'quark')
    colors = sorted(m.group(1) for i in ids if (m := re.fullmatch(r'quark:([a-z]+)_corundum_cluster', i)))
    if not colors:
        return []
    chance = _quark_growth_chance()
    if chance is not None and chance < 1:
        return []
    req = ['quark']
    blocks_ = [f'quark:{c}_corundum' for c in colors]
    clusters = [f'quark:{c}_corundum_cluster' for c in colors]
    every = f'1 in {chance}' if chance else 'Some'
    rows = [row([blocks(blocks_)], [blocks(blocks_, label='Grows upward'), blocks(clusters, label='On an open side')],
                linked=True, requires=req, notes=[
                    f'{every} random ticks below y 24: grows up (max 4), sprouts clusters. Not waxed.'])]
    found = drops(_loot(ctx, clusters[0], cache))
    if found and all(d['label'] is None for d in found):
        rows.append(row([blocks(clusters)], [blocks(clusters, label='Any tool')], linked=True, requires=req,
                        notes=['Drops itself with any tool.']))
    else:
        rows.append(_harvest_row(ctx, cache, [clusters[0]], req))
    return rows


def build(ctx):
    cache = {}
    rows = _vanilla(ctx, cache) + _ae2(ctx, cache) + _geode(ctx, cache) + _quark(ctx, cache)
    return {TAB: rows}
