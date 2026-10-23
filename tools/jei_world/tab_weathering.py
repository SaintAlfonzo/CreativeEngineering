"""Weathering and Growth, Lightning Conversion and Lava Generation tabs: Immersive Weathering's data-driven rules.

Sources (jar data with kubejs/data on top, so datapack overrides count):
  - data/*/block_growths/*.json -> block_growth, or lightning when the tick source is lightning_strike.
  - data/*/fluid_generators/*.json -> lava_generation.
  - IW "builtin" growths whose pairs come from block tags (sandy / snowy stones, sand pile seeping, leaf piles,
    lightning vitrify / crack) and the builtin burn_moss fluid generator; pairs from kubejs/exported/tags.
Hardcoded behaviour with no pairing data (soot, icicles, ice melting, vanilla / Create / DnD lava, copper and
lightning, IE concrete, Salt) is in each tab's manual.json.

Semantics read from the IW 1.2.10 jar (javap, 2026-10-22):
  - ConfigurableBlockGrowth: growth_chance per tick; weighted face (growth_for_face[].weight, default 1; a face
    with no direction covers all six), then a weighted block on that face. target_self replaces the owner, else
    the block on that face must match replacing_target (random_block_match also rolls its probability).
    destroy_target breaks the target first. above_block places a second block above.
  - Tick sources: none = random tick; rain / snow / clear_sky = the top block of a random column each chunk tick
    (rain or snow by biome when it is raining, clear_sky when not); lightning_strike = the struck block.
    "sky_access" is not a valid source and is ignored here.
  - generate_if_not_too_many: fails when requiredAmount or more of the possible results (plus `includes`) are
    within the radius; must_have needs one match in the radius, must_not_have none. neighbor_based_generation:
    must_have on an adjacent block.
  - Config: config/immersive_weathering-common.toml (block_growths on, blacklist empty, vitrified_lightning on,
    fulgurite_chance 0.4, leaf_piles.reach 12).
"""
import json
import re

from .common import block, block_tag, blocks, fluid, item, row

REQ = ['immersive_weathering']
ALL_DIRS = ['down', 'up', 'north', 'south', 'west', 'east']
HORIZONTAL = {'north', 'south', 'west', 'east'}
# Blocks with no item that read better as their item form.
ITEM_FORM = {'minecraft:tall_seagrass': 'minecraft:seagrass', 'minecraft:bamboo_sapling': 'minecraft:bamboo'}
LAVA_ACTION = 'minecraft:lava_bucket'
LIGHTNING = {'item': 'minecraft:lightning_rod', 'label': 'Lightning strike'}
# Plain names for the tags the rules test against.
TAG_WORDS = {
    'immersive_weathering:grass_spread_source': 'grass',
    'immersive_weathering:magma_source': 'lava or magma',
    'immersive_weathering:weeds_replaceable': 'air or a crop',
    'minecraft:dirt': 'dirt',
    'minecraft:sand': 'sand',
    'minecraft:base_stone_overworld': 'stone',
    'immersive_weathering:ash': 'ash',
}


class _Names:
    def __init__(self, ctx):
        dump = ctx.exported('display_names.json') or {}
        entries = dump.get('entries', {})
        self.blocks = {e['id']: e for e in entries.get('block', [])}
        tags = ctx.exported('tags/block_tags.json') or {}
        self.tags = {k: v for k, v in tags.items()}

    def name(self, id_):
        e = self.blocks.get(id_)
        if e and e.get('name'):
            return e['name']
        return _humanize(id_)

    def exists(self, id_):
        return id_ in self.blocks

    def has_item(self, id_):
        e = self.blocks.get(id_)
        return bool(e and e.get('hasItem'))

    def tag(self, tag_id):
        return list(self.tags.get(tag_id.lstrip('#'), []))


def _humanize(id_):
    path = id_.lstrip('#').split(':', 1)[-1].split('/')[-1]
    return ' '.join(w.capitalize() for w in path.split('_'))


def _pct(p):
    pct = p * 100
    if pct >= 10 or pct == round(pct):
        return f'{round(pct)}%'
    s = f'{pct:.2f}'.rstrip('0').rstrip('.')
    return f'{s}%'


def _as_list(v):
    if v is None:
        return []
    return v if isinstance(v, list) else [v]


# Ingredient helpers ----------------------------------------------------------------------------------------

def _owner_ing(names, owners, **extra):
    owners = _as_list(owners)
    tags = [o for o in owners if o.startswith('#')]
    ids = [o for o in owners if not o.startswith('#')]
    if tags and not ids and len(tags) == 1:
        return block_tag(tags[0], **extra)
    if ids and not tags:
        return block(ids[0], **extra) if len(ids) == 1 else blocks(ids, **extra)
    out = []
    for t in tags:
        out += names.tag(t)
    return blocks(ids + out, **extra)


def _state_ing(names, state, label=None):
    """A placed block state -> an output ingredient (item form for itemless plants, fluid for liquids)."""
    bid = state['Name']
    extra = {'label': label} if label else {}
    if bid in ITEM_FORM:
        return item(ITEM_FORM[bid], label=label or f'Grows as {names.name(bid)}')
    if bid in ('minecraft:water', 'minecraft:lava'):
        return fluid(bid, **extra)
    return block(bid, **extra)


def _rule_ing(names, rule, label):
    """A RuleTest (replacing_target, area must_have, adjacent block) -> ingredient, or None for air / unknown."""
    if not rule:
        return None
    t = rule.get('predicate_type', '')
    if t.endswith('fluid_match'):
        fl = _fluid_ids(rule)
        return fluid(fl[0], label=label) if fl else None
    if t.endswith('blockstate_match'):
        bid = rule['block_state']['Name']
        return None if bid == 'minecraft:air' else _state_ing(names, rule['block_state'], label)
    if 'block' in rule and isinstance(rule['block'], str):
        return None if rule['block'] == 'minecraft:air' else block(rule['block'], label=label)
    if 'tag' in rule:
        return block_tag(rule['tag'], label=label)
    if 'blocks' in rule:
        b = [x for x in _as_list(rule['blocks']) if x != 'minecraft:air']
        if not b:
            return None
        if len(b) == 1:
            return block_tag(b[0], label=label) if b[0].startswith('#') else block(b[0], label=label)
        return blocks(b, label=label)
    return None


def _fluid_ids(rule):
    out = []
    for f in _as_list(rule.get('fluids')):
        f = f.lstrip('#').replace('flowing_', '')
        if f not in out:
            out.append(f)
    return out


def _tag_word(tag_id):
    return TAG_WORDS.get(tag_id.lstrip('#'), _humanize(tag_id).lower())


def _rule_name(names, rule):
    if not rule:
        return None
    t = rule.get('predicate_type', '')
    if t.endswith('fluid_match'):
        return ' or '.join(_humanize(f).lower() for f in _fluid_ids(rule))
    if t.endswith('blockstate_match'):
        return names.name(rule['block_state']['Name']).lower()
    if isinstance(rule.get('block'), str):
        return names.name(rule['block']).lower()
    if 'tag' in rule:
        return _tag_word(rule['tag'])
    b = _as_list(rule.get('blocks'))
    if b and all(x.startswith('#') for x in b):
        return ' or '.join(_tag_word(x) for x in b)
    if b:
        return ' or '.join(names.name(x).lower() for x in b)
    return None


# Block growths ---------------------------------------------------------------------------------------------

def _tick_text(sources, chance):
    c = _pct(chance) if chance < 1 else None
    srcs = [s for s in sources if s in ('rain', 'snow', 'clear_sky', 'block_tick')]
    if not srcs or srcs == ['block_tick']:
        base = 'Random tick'
    else:
        words = {'rain': 'rain', 'snow': 'snow', 'clear_sky': 'clear sky', 'block_tick': 'random tick'}
        base = ' or '.join(words[s] for s in srcs)
        base = base[0].upper() + base[1:]
    return f'{base} {c}' if c else base


def _weather(sources):
    return any(s in ('rain', 'snow', 'clear_sky') for s in sources)


def _face_text(faces, target_self):
    if target_self:
        return 'changes in place'
    dirs = set(faces)
    if dirs == set(ALL_DIRS):
        return 'grows on any side'
    bits = []
    if 'up' in dirs:
        bits.append('on top')
    if dirs & HORIZONTAL:
        bits.append('sideways')
    if 'down' in dirs:
        bits.append('below')
    return 'grows ' + ', '.join(bits[:-1]) + (' or ' if len(bits) > 1 else '') + bits[-1]


def _biome_text(pred):
    b = pred.get('biomes')
    if isinstance(b, str):
        t = b.lstrip('#').split(':', 1)[-1]
        return {'icy': 'icy biomes', 'is_nether': 'the Nether'}.get(t, _humanize(b) + ' biomes')
    names = [_humanize(x) for x in b]
    return ', '.join(names)


def _pos_notes(preds):
    """(short note parts, long label parts) from position_predicates."""
    short, long_ = [], []
    for p in preds:
        t = p.get('type')
        if t == 'biome_match':
            text = _biome_text(p)
            long_.append('Biome: ' + text)
            short.append(text if len(text) <= 22 else 'some biomes')
        elif t == 'day_test':
            short.append('at night' if not p.get('day') else 'by day')
        elif t == 'block_test':
            for prop in p.get('predicate', {}).get('properties', []):
                if prop.get('property') == 'can_expand':
                    long_.append('Only thin ice that can still spread')
                elif prop.get('property') == 'layers':
                    short.append(f"pile {prop.get('value')}+ layers")
    return short, long_


def _area_text(names, ac, out_names):
    if not ac:
        return None, None
    if ac['type'] == 'neighbor_based_generation':
        n = _rule_name(names, ac.get('must_have'))
        if ac['must_have'].get('predicate_type', '').endswith('burnable_test'):
            return 'next to a burnable block', None
        if n == 'air':
            return None, None
        return (f'next to {n}' if n else None), None
    r = max(ac.get('radiusX', 0), ac.get('radiusZ', 0))
    req = ac.get('requiredAmount')
    parts = []
    if ac.get('must_have'):
        n = _rule_name(names, ac['must_have'])
        if n:
            parts.append(f'near {n}')
    if ac.get('must_not_have'):
        n = _rule_name(names, ac['must_not_have'])
        if n:
            parts.append(f'no {n} near')
    return ('; '.join(parts) or None), f'Stops when {req} or more of the results are within {r} blocks'


def _growth_rows(ctx, names, key, d):
    owners = d.get('owners')
    if not owners:
        ctx.warn(f'block_growths {key}: no owners')
        return 'block_growth', []
    sources = _as_list(d.get('tick_sources')) or ['block_tick']
    lightning = 'lightning_strike' in sources
    chance = float(d.get('growth_chance', 1))
    if chance <= 0:
        # growth_chance 0 (frost_growth, frost_spreading): the rule can never fire.
        return 'block_growth', []
    target_self = bool(d.get('target_self'))
    rt = d.get('replacing_target')

    # Face -> weighted entries, then output chances.
    faces = []
    for g in d['growth_for_face']:
        dirs = [g['direction']] if 'direction' in g else ALL_DIRS
        for dr in dirs:
            faces.append((dr, g.get('weight', 1), g['growth']))
    total_fw = sum(fw for _, fw, _ in faces) or 1
    acc = {}
    order = []
    for dr, fw, entries in faces:
        tw = sum(e.get('weight', 1) for e in entries) or 1
        for e in entries:
            w = e.get('weight', 1)
            if w <= 0:
                continue
            data = e['data']
            state = data['block']
            label = None
            if 'above_block' in data:
                above = data['above_block']
                if above['Name'] != state['Name']:
                    label = f"Placed on a {names.name(state['Name'])}"
                    state = above
            bid = state['Name']
            if bid.endswith('_wall_fan'):
                bid = bid[:-len('_wall_fan')] + '_fan'  # same item as the fan
            k = (bid, label)
            if k not in acc:
                acc[k] = 0.0
                order.append(k)
            acc[k] += (fw / total_fw) * (w / tw)
    outs = []
    for k in sorted(order, key=lambda k: -acc[k]):
        bid, label = k
        st = {'Name': bid}
        ing = _state_ing(names, st, label)
        if acc[k] < 0.9999:
            ing['chance'] = round(acc[k], 4)
        outs.append(ing)
    if not outs:
        return 'block_growth', []

    # Inputs.
    pos_short, pos_long = _pos_notes(d.get('position_predicates', []))
    area_short, area_long = _area_text(names, d.get('area_condition'), None)
    owner_label = '; '.join((['Top block, open sky'] if _weather(sources) else []) + pos_long
                            + ([area_long] if area_long else [])) or None
    inputs = [_owner_ing(names, owners, **({'label': owner_label} if owner_label else {}))]
    if not target_self and rt:
        tgt_label = 'Broken and replaced' if d.get('destroy_target') else 'Replaced'
        tgt = _rule_ing(names, rt, tgt_label)
        if tgt:
            inputs.append(tgt)

    # Notes: tick + face, then conditions.
    face_dirs = [f[0] for f in faces]
    face = _face_text(face_dirs, target_self)
    tgt_name = None if target_self else _rule_name(names, rt)
    if tgt_name and tgt_name != 'air' and not target_self:
        face += f', into {tgt_name}'
    head = 'Struck' if lightning else _tick_text(sources, chance)
    notes = [f'{head}: {face}']
    cond = [s for s in pos_short] + ([area_short] if area_short else [])
    if cond:
        c = ', '.join(cond)
        notes.append(c[0].upper() + c[1:])

    tab = 'lightning' if lightning else 'block_growth'
    rows = []
    for i in range(0, len(outs), 6):
        rows.append(row(inputs, outs[i:i + 6], action=LIGHTNING if lightning else None, notes=notes,
                        requires=REQ + _mods(owners, outs)))
    return tab, rows


def _mods(*things):
    mods = set()
    for t in things:
        for x in _as_list(t):
            s = x if isinstance(x, str) else (x.get('block') or x.get('item') or x.get('fluid') or '')
            if isinstance(s, str) and ':' in s:
                ns = s.lstrip('#').split(':', 1)[0]
                if ns not in ('minecraft', 'forge', 'c'):
                    mods.add(ns)
    return sorted(mods)


# Tag-paired builtin growths --------------------------------------------------------------------------------

def _pairs(names, sources, prefix_fn):
    """[(source, result)] where the result id exists in the block dump."""
    out = []
    for s in sources:
        for cand in prefix_fn(s):
            if names.exists(cand):
                out.append((s, cand))
                break
    return out


def _sandy_snowy(names, word):
    src = names.tag(f'immersive_weathering:{word}able')

    def cands(s):
        path = s.split(':', 1)[1]
        return [f'immersive_weathering:{word}y_{path}']
    return _pairs(names, src, cands)


def _builtin_rows(ctx, names, builtins):
    rows = {'block_growth': [], 'lightning': []}
    if 'sandy_stones' in builtins:
        p = _sandy_snowy(names, 'sand')
        if p:
            rows['block_growth'].append(row(
                [blocks([a for a, _ in p], label='Top block in desert or warm ocean biomes')],
                [blocks([b for _, b in p]), block('immersive_weathering:sand_layer_block', label='Sometimes on top')],
                notes=['Sandstorm ' + _pct(builtins['sandy_stones'].get('growth_chance', 1)) + ': turns sandy',
                       'Rain in desert or warm ocean biomes'],
                linked=True, requires=REQ))
    if 'snowy_stones' in builtins:
        p = _sandy_snowy(names, 'snow')
        if p:
            rows['block_growth'].append(row(
                [blocks([a for a, _ in p])], [blocks([b for _, b in p])],
                notes=['Snow ' + _pct(builtins['snowy_stones'].get('growth_chance', 1))
                       + ': turns snowy', 'Block light 11 or less above'],
                linked=True, requires=REQ))
    if 'sand_layer_seeping' in builtins:
        p = _sandy_snowy(names, 'sand')
        if p:
            rows['block_growth'].append(row(
                [blocks([a for a, _ in p], label='Below the sand pile'),
                 block('immersive_weathering:sand_layer_block', label='2 or more layers')],
                [blocks([b for _, b in p]), block('immersive_weathering:sand_layer_block', label='One layer less')],
                notes=['Random tick: the pile seeps into the block below'],
                linked=True, requires=REQ))
    if 'leaf_piles_from_leaves' in builtins:
        leaves = names.tag('minecraft:leaves')

        def cands(s):
            ns, path = s.split(':', 1)
            stem = path[:-len('_leaves')] if path.endswith('_leaves') else path
            pile = f'{stem}_leaf_pile'
            return [f'immersive_weathering:{pile}' if ns == 'minecraft' else f'immersive_weathering:{ns}/{pile}']
        p = _pairs(names, leaves, cands)
        reach = 12
        if p:
            rows['block_growth'].append(row(
                [blocks([a for a, _ in p], label='Natural (not player placed) leaves')],
                [blocks([b for _, b in p], label='Lands on the ground below')],
                notes=['Random tick ' + _pct(builtins['leaf_piles_from_leaves'].get('growth_chance', 1))
                       + f': drops a pile up to {reach} blocks below'],
                linked=True, requires=REQ))
    if 'lightning_vitrified_sand' in builtins:
        rows['lightning'].append(row(
            [block_tag('minecraft:sand')],
            [block('immersive_weathering:vitrified_sand'),
             block('immersive_weathering:fulgurite', chance=0.4, label='On an open side')],
            action=LIGHTNING,
            notes=['Spreads up to 5 blocks, rarer each step'],
            requires=REQ))
        crack = names.tag('immersive_weathering:crackable')

        def ccands(s):
            ns, path = s.split(':', 1)
            return [f'{ns}:cracked_{path}', f'minecraft:cracked_{path}', f'immersive_weathering:cracked_{path}']
        p = _pairs(names, crack, ccands)
        if p:
            rows['lightning'].append(row(
                [blocks([a for a, _ in p])], [blocks([b for _, b in p])],
                action=LIGHTNING,
                notes=['Cracks the struck block', 'Spreads up to 5 blocks, rarer each step'],
                linked=True, requires=REQ))
    return rows


# Fluid generators ------------------------------------------------------------------------------------------

def _gen_rows(ctx, names, key, d):
    t = d.get('type', '')
    if t == 'burn_moss':
        mossy = names.tag('immersive_weathering:mossy')

        def cands(s):
            ns, path = s.split(':', 1)
            clean = path.replace('mossy_', '', 1)
            return [f'{ns}:{clean}', f'minecraft:{clean}', f'immersive_weathering:{clean}']
        p = _pairs(names, mossy, cands)
        if not p:
            return []
        return [row([blocks([a for a, _ in p], label='Touching the lava')], [blocks([b for _, b in p])],
                    action=item(LAVA_ACTION, label='Lava, source or flowing'),
                    notes=['Lava burns the moss off', 'Stops it getting mossy'],
                    linked=True, requires=REQ)]
    fl = d.get('fluid', 'minecraft:lava')
    if fl not in ('minecraft:lava', 'minecraft:flowing_lava'):
        ctx.warn(f'fluid_generators {key}: fluid {fl} not handled')
        return []
    ftype = d.get('fluid_type', 'both')
    lava_label = {'still': 'Lava source only', 'flowing': 'Flowing lava only'}.get(ftype, 'Lava, source or flowing')
    out = _state_ing(names, d['generate'])
    if t.endswith('target_other'):
        tgt = _rule_ing(names, d.get('target'), 'Next to the lava')
        if not tgt:
            return []
        return [row([tgt], [out], action=item(LAVA_ACTION, label=lava_label),
                    notes=['Flowing lava next to it converts it'] + ([lava_label] if ftype != 'both' else []),
                    requires=REQ)]
    adj = d.get('adjacent_blocks', {})
    inputs, conds = [], []
    for r in adj.get('sides', []):
        ing = _rule_ing(names, r, 'Beside the lava')
        if ing:
            inputs.append(ing)
            conds.append(f'{_rule_name(names, r)} beside')
    for r in adj.get('any', []):
        ing = _rule_ing(names, r, 'Touching the lava')
        if ing:
            inputs.append(ing)
            conds.append(f'{_rule_name(names, r)} touching')
    for side, word in (('up', 'above'), ('down', 'below')):
        if adj.get(side):
            ing = _rule_ing(names, adj[side], f'{word.capitalize()} the lava')
            if ing:
                inputs.append(ing)
                conds.append(f'{_rule_name(names, adj[side])} {word}')
    if not inputs:
        ctx.warn(f'fluid_generators {key}: no adjacent blocks')
        return []
    if len(inputs) > 3:
        ctx.warn(f'fluid_generators {key}: more than 3 conditions')
    note = 'Needs ' + ', '.join(conds)
    notes = [note[0].upper() + note[1:]]
    if ftype != 'both':
        notes.append(lava_label)
    return [row(inputs[:3], [out], action=item(LAVA_ACTION, label=lava_label), notes=notes, requires=REQ)]


def build(ctx):
    names = _Names(ctx)
    out = {'block_growth': [], 'lightning': [], 'lava_generation': []}
    builtins = {}
    for key, d in ctx.data_files('block_growths').items():
        if 'builtin' in d:
            builtins[d['builtin']] = d
            continue
        if 'growth_for_face' not in d:
            ctx.warn(f'block_growths {key}: unknown format')
            continue
        tab, rows = _growth_rows(ctx, names, key, d)
        out[tab] += rows
    for tab, rows in _builtin_rows(ctx, names, builtins).items():
        out[tab] += rows
    for key, d in ctx.data_files('fluid_generators').items():
        out['lava_generation'] += _gen_rows(ctx, names, key, d)
    return out
