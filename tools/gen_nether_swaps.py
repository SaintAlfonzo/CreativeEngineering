#!/usr/bin/env python3
"""Nether block swaps: expands kubejs/generation_scripts/worldgen/nether_swap_rules.json over every structure template
into exact per-position swaps, kubejs/generation_scripts/worldgen/nether_block_swaps.json, which patcherMod
worldgen/TemplateBlockSwaps applies when a template loads (feature_writeup/structure_tuning.md section 7).

    python3 tools/gen_nether_swaps.py

Owners: every structure (live or not) whose pool walk reaches the template (terrain_adaptation_scan.reach). A template
some Nether or End structure can also place is skipped (the walk ignores jigsaw names, so it overcounts: an Overworld
ship "reaches" the Nether ship's crimson keel). A rule applies to a template when one of its Overworld owners is in
the rule's structures and none of its owners is excepted (rule "except").
"except_holding" (block selectors) also skips every structure any of whose templates holds a matching block (palette
check), e.g. ["@sculk"] for the sculk structures.
Block selectors: an id, "@nether" (every block nether_blocks_scan classifies), "@nether_bricks" (the nether brick
family), "@sculk" (any block whose id path contains "sculk"). Targets: a block id (properties carried over per KEEP,
else by id suffix, FAMILY_KEEP), "air", or
"deepslate" (the nether brick family to its deepslate brick twin; a fence becomes a wall).
Also writes feature_writeup/nether_swaps.md (what changed where).
"""
import json
import sys
from collections import Counter, OrderedDict, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_structure_groups as GSG  # noqa: E402
import loot_breakdown_scan as S  # noqa: E402
import nether_blocks_scan as N  # noqa: E402
import terrain_adaptation_scan as T  # noqa: E402

ROOT = GSG.INSTANCE
RULES = T.WORLDGEN / 'nether_swap_rules.json'
OUT_JSON = T.WORLDGEN / 'nether_block_swaps.json'
OUT_MD = ROOT / 'feature_writeup/nether_swaps.md'

BRICKS = {
    'minecraft:nether_bricks': 'minecraft:deepslate_bricks', 'minecraft:red_nether_bricks': 'minecraft:deepslate_bricks',
    'minecraft:cracked_nether_bricks': 'minecraft:cracked_deepslate_bricks', 'minecraft:chiseled_nether_bricks': 'minecraft:chiseled_deepslate',
    'minecraft:nether_brick_slab': 'minecraft:deepslate_brick_slab', 'minecraft:red_nether_brick_slab': 'minecraft:deepslate_brick_slab',
    'minecraft:nether_brick_stairs': 'minecraft:deepslate_brick_stairs', 'minecraft:red_nether_brick_stairs': 'minecraft:deepslate_brick_stairs',
    'minecraft:nether_brick_wall': 'minecraft:deepslate_brick_wall', 'minecraft:red_nether_brick_wall': 'minecraft:deepslate_brick_wall',
    'minecraft:nether_brick_fence': 'minecraft:deepslate_brick_wall',
}
# properties each target keeps from the source state (the rest of the target's properties take their defaults)
KEEP = {
    'minecraft:deepslate_brick_slab': ['type', 'waterlogged'],
    'minecraft:deepslate_brick_stairs': ['facing', 'half', 'shape', 'waterlogged'],
    'minecraft:deepslate_brick_wall': ['east', 'north', 'south', 'west', 'up', 'waterlogged'],
    'minecraft:basalt': ['axis'],
    'minecraft:campfire': ['facing', 'lit', 'signal_fire', 'waterlogged'],
    'minecraft:lantern': ['hanging', 'waterlogged'],
    'minecraft:wall_torch': ['facing'],
}
# fallback by id suffix for targets not in KEEP (first match), so a new target never silently drops its state
FAMILY_KEEP = [
    ('_trapdoor', ['facing', 'half', 'open', 'powered', 'waterlogged']),
    ('_button', ['face', 'facing', 'powered']),
    ('_pressure_plate', ['powered']),
    ('_slab', ['type', 'waterlogged']),
    ('_stairs', ['facing', 'half', 'shape', 'waterlogged']),
    ('_wall', ['east', 'north', 'south', 'west', 'up', 'waterlogged']),
    ('wall_torch', ['facing']),
    ('lantern', ['hanging', 'waterlogged']),
    ('_log', ['axis']), ('_wood', ['axis']), ('_hyphae', ['axis']), ('_stem', ['axis']),
]
SIDES = ('east', 'north', 'south', 'west')


def keep_for(name):
    if name in KEEP:
        return KEEP[name]
    return next((k for suf, k in FAMILY_KEEP if name.endswith(suf)), [])


def target_state(src, to):
    """The {Name, Properties} a source palette state becomes."""
    props = src.get('Properties', {}) or {}
    if to == 'air':
        return {'Name': 'minecraft:air'}
    name = BRICKS[src['Name']] if to == 'deepslate' else to
    out = {}
    if src['Name'] == 'minecraft:nether_brick_fence' and name == 'minecraft:deepslate_brick_wall':
        out = {s: ('low' if props.get(s) == 'true' else 'none') for s in SIDES}
        out['up'] = 'true'
        if 'waterlogged' in props:
            out['waterlogged'] = props['waterlogged']
    else:
        out = {k: v for k, v in props.items() if k in keep_for(name)}
    if name == 'immersive_weathering:permafrost':
        out = {'dirty': 'false'}
    return {'Name': name, 'Properties': out} if out else {'Name': name}


def matches(selector, block):
    if selector == '@nether':
        return N.classify(block) is not None
    if selector == '@nether_bricks':
        return block in BRICKS
    if selector == '@sculk':
        return 'sculk' in block.partition(':')[2]
    return block == selector


def structure_owners(pack, overworld, elsewhere):
    """template id -> structures whose pool walk reaches it; plus the Overworld-owned and Nether/End-shared sets."""
    structs, pools, templates, _ = T.load_worldgen()
    live = GSG.read_json(T.WORLDGEN / 'pool_config.json')['pools']
    pad, plants, _ = T.load_tags()
    cache = T.TemplateCache(templates, pad, plants, False)
    owners = defaultdict(set)
    ow_owned = set()   # templates at least one Overworld structure can place
    other_dim = set()
    for sid in sorted(pack.pool_all):
        if sid in pack.meta:
            continue
        d = structs.get(sid)
        if d and d.get('start_pool'):
            reach = T.reach(T.G.norm_id(d['start_pool']), int(d.get('size', 1) or 1), pools, live, cache.get)
        elif sid in T.VANILLA_JIGSAW:
            sp, size, _ = T.VANILLA_JIGSAW[sid]
            reach = T.reach(sp, size, pools, live, cache.get)
        else:
            ns, _, path = sid.partition(':')
            leaf = path.rsplit('/', 1)[-1]
            reach = {t: 1 for t in templates if t.startswith(ns + ':') and leaf in t.split(':', 1)[1]}
            if not reach and d:
                siblings = [o.partition(':')[2].rsplit('/', 1)[-1] for o in structs if o.startswith(ns + ':') and o != sid]
                reach = {t: 1 for t in templates if t.startswith(ns + ':') and not any(x in t.split(':', 1)[1] for x in siblings)}
        ow = bool(pack.pool_all[sid] & overworld)
        for t in reach:
            owners[t].add(sid)
            if ow:
                ow_owned.add(t)
            if pack.pool_all[sid] & elsewhere or not ow:
                other_dim.add(t)
    cache.save()
    return owners, ow_owned, other_dim, templates


def main():
    doc = GSG.read_json(RULES)
    rules = doc['rules']
    pack = GSG.Pack()
    dims = GSG.read_json(GSG.BIOME_CONFIG)['dimensions']
    overworld = set(dims['minecraft:overworld']['biomes'])
    elsewhere = set(dims['minecraft:the_nether']['biomes']) | set(dims['minecraft:the_end']['biomes'])

    owners, ow_owned, other_dim, templates = structure_owners(pack, overworld, elsewhere)

    loaded = OrderedDict()   # template id -> (palette, blocks), every Overworld-owned template
    for t in sorted(owners):
        if t not in templates or t not in ow_owned:
            continue
        _, read, name = templates[t]
        d = S.read_template(read(name))
        if d:
            loaded[t] = (d.get('palette') or (d.get('palettes') or [[]])[0], d.get('blocks', []))
    # except_holding: per rule, the structures any of whose (Overworld) templates hold a matching block
    holding = []
    for r in rules:
        sels = r.get('except_holding', [])
        found = set()
        if sels:
            for t, (pal, _) in loaded.items():
                if any(matches(sel, p.get('Name', '')) for p in pal for sel in sels):
                    found |= owners[t]
        holding.append(found)

    out, report = OrderedDict(), defaultdict(Counter)
    skipped_dim = Counter()
    for t, (pal, blocks) in loaded.items():
        entries = []
        for b in blocks:
            s = b.get('state', 0)
            if s >= len(pal):
                continue
            src = pal[s]
            block = src.get('Name', 'minecraft:air')
            for i, r in enumerate(rules):
                if not any(matches(sel, block) for sel in r['blocks']):
                    continue
                scope = r['structures']
                ow_owners = owners[t]
                if scope != ['*'] and not (ow_owners & set(scope)):
                    continue
                if ow_owners & (set(r.get('except', [])) | holding[i]):
                    continue
                if t in other_dim:
                    skipped_dim[block] += 1
                    break
                to = target_state(src, r['to'])
                entries.append({'pos': b['pos'], 'from': block, 'to': to})
                report[i][(t, block, to['Name'])] += 1
                break
        if entries:
            out[t] = entries
    OUT_JSON.write_text(json.dumps({'_readme': {
        'what': 'Generated by python3 tools/gen_nether_swaps.py from nether_swap_rules.json. Do not edit: edit the rules and rerun.',
        'entries': 'template id -> [{pos, from, to}], the same format as meat_block_swaps.json; applied by patcherMod '
                   'worldgen/TemplateBlockSwaps when the template loads, only where the template still has `from` at `pos`.'},
        'templates': out}, indent=1) + '\n')
    write(rules, report, owners, skipped_dim, holding)
    n = sum(len(v) for v in out.values())
    print(f'{n} block swap(s) in {len(out)} template(s) -> {OUT_JSON.relative_to(ROOT)}; '
          f'{sum(skipped_dim.values())} matching block(s) left alone in templates Nether/End structures share')


def write(rules, report, owners, skipped_dim, holding):
    L = ['# Nether block swaps', '', 'Written by `python3 tools/gen_nether_swaps.py` from '
         '`kubejs/generation_scripts/worldgen/nether_swap_rules.json`. Do not edit. Per rule: every template it changes, '
         'with block counts. Applied by patcherMod `TemplateBlockSwaps` when a template loads.', '']
    for i, r in enumerate(rules):
        rows = report.get(i, {})
        total = sum(rows.values())
        L += [f"## {i + 1}. {r.get('note', '')} ({total} blocks)", '']
        if r.get('except_holding'):
            L += [f"Skipped, holding {', '.join(f'`{x}`' for x in r['except_holding'])}: "
                  + (', '.join(f'`{o}`' for o in sorted(holding[i])) or 'none') + '.', '']
        if not rows:
            L += ['Nothing matched.', '']
            continue
        L += ['| Template | Structures | From | To | Blocks |', '|---|---|---|---|---:|']
        for (t, f, to), n in sorted(rows.items()):
            own = sorted(owners[t])
            L.append(f"| `{t}` | {', '.join(f'`{o}`' for o in own[:2])}{' ...' if len(own) > 2 else ''} | `{f.split(':')[1]}` | "
                     f"`{to}` | {n} |")
        L.append('')
    if skipped_dim:
        L += ['## Left alone (templates a Nether or End structure also places)', '',
              ', '.join(f'`{b.split(":")[1]}` {n}' for b, n in skipped_dim.most_common()), '']
    OUT_MD.write_text('\n'.join(L) + '\n')


if __name__ == '__main__':
    main()
