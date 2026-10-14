#!/usr/bin/env python3
"""Meat blocks: lone red blocks that stand in for meat (a single red glazed terracotta hanging in a butcher's stall)
in the templates of Overworld structures, and the marks that swap them for Architect's Palette fish blocks
(feature_writeup/structure_tuning.md section 7, "swap meat blocks").

    python3 tools/meat_block_scan.py

A red block is LONE when none of its 6 neighbours in the template is the same block (a red glazed terracotta floor
or a wool wall is not meat). RED (below) is every red block scanned; MARK is the subset marked for replacement when
lone in a structure of a settlement family (villages, houses, camps, forts and outposts) or in a template whose path
names food (butcher, kitchen, tavern, market...). Everything else lone and red is listed for review only.

Writes:
  kubejs/generation_scripts/worldgen/meat_block_swaps.json   the marks: template -> [{pos, from, to}]
  feature_writeup/meat_blocks.md                              per structure, per template, marked and review rows
"""
import json
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_structure_groups as GSG  # noqa: E402
import loot_breakdown_scan as S  # noqa: E402
import nether_blocks_scan as N  # noqa: E402
import terrain_adaptation_scan as T  # noqa: E402

ROOT = GSG.INSTANCE
OUT_MD = ROOT / 'feature_writeup/meat_blocks.md'
OUT_JSON = T.WORLDGEN / 'meat_block_swaps.json'
RED = ['minecraft:red_glazed_terracotta', 'minecraft:red_terracotta', 'minecraft:red_wool', 'minecraft:red_concrete',
       'minecraft:red_concrete_powder', 'minecraft:pink_glazed_terracotta', 'minecraft:red_mushroom_block',
       'minecraft:nether_wart_block', 'minecraft:redstone_block']
MARK = {'minecraft:red_glazed_terracotta'}
TARGET = {'Name': 'architects_palette:salmon_log', 'Properties': {'axis': 'y'}}
FAMILIES = ['type/village', 'type/house', 'type/camp', 'type/fort', 'pillager']
FOOD = re.compile(r'butcher|meat|food|kitchen|cook|tavern|inn|market|stall|shop|smoke|farm|bakery|deli|fish|hunter')


def main():
    pack = GSG.Pack()
    overworld = set(GSG.read_json(GSG.BIOME_CONFIG)['dimensions']['minecraft:overworld']['biomes'])
    groups = GSG.read_json(GSG.GROUPS)
    off = {sid for part in ('groups', 'standalone') for v in groups.get(part, {}).values()
           for sid, s in v.get('structures', {}).items() if s.get('enabled') is False or s.get('frequency', 1) == 0}
    structs, pools, templates, _ = T.load_worldgen()
    live = GSG.read_json(T.WORLDGEN / 'pool_config.json')['pools']
    gone = N.removed_pieces({k for p in live.values() for k in p.get('elements', {})})
    pad, plants, _ = T.load_tags()
    cache = T.TemplateCache(templates, pad, plants, False)
    memo = {}

    def red_in(tid):
        """[(pos, block, lone)] for every red block of the template."""
        if tid not in memo:
            out = []
            if tid in templates:
                _, read, name = templates[tid]
                d = S.read_template(read(name))
                if d:
                    pal = d.get('palette') or (d.get('palettes') or [[]])[0]
                    names = [p.get('Name', 'minecraft:air') for p in pal]
                    at = {}
                    for b in d.get('blocks', []):
                        s = b.get('state', 0)
                        if s < len(names):
                            at[tuple(b['pos'])] = names[s]
                    for pos, blk in at.items():
                        if blk in RED:
                            x, y, z = pos
                            nb = [(x + 1, y, z), (x - 1, y, z), (x, y + 1, z), (x, y - 1, z), (x, y, z + 1), (x, y, z - 1)]
                            out.append((list(pos), blk, not any(at.get(n) == blk for n in nb)))
            memo[tid] = out
        return memo[tid]

    owners = defaultdict(set)   # template -> structures that can place it
    family_of = {}
    for sid in sorted(pack.pool_all):
        if sid in pack.meta or sid in off or sid in pack.blocked or not (pack.pool_all[sid] & overworld):
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
        tags = {t for t, m in pack.tag_members.items() if sid in m}
        family_of[sid] = next((f for f in FAMILIES if f in tags), None)
        for t in reach:
            if t not in gone and red_in(t):
                owners[t].add(sid)
    cache.save()

    marks, review = {}, defaultdict(list)
    for t in sorted(owners):
        settlement = any(family_of.get(s) for s in owners[t]) or bool(FOOD.search(t.split(':', 1)[1]))
        for pos, blk, lone in red_in(t):
            if not lone:
                continue
            if blk in MARK and settlement:
                marks.setdefault(t, []).append({'pos': pos, 'from': blk, 'to': TARGET})
            else:
                review[t].append((pos, blk, settlement))
    doc = {'_readme': {
        'what': 'Lone "meat" blocks in structure templates, marked to be swapped for Architect\'s Palette fish blocks standing '
                'upright when the structure is placed. Written by python3 tools/meat_block_scan.py (do not edit by hand: rerun '
                'it). A lone block has no neighbour of the same block in the template.',
        'entries': 'template id -> [{pos: [x, y, z] in the template, from: block id there, to: {Name, Properties}}]. A swap '
                   'applies only when the template still has `from` at `pos` (a mod update that moves things skips it).',
        'applied by': 'patcherMod worldgen/TemplateBlockSwaps: each marked template is patched once when it loads (every '
                      'rotation and every structure using it get the new block). Read at every world load; new chunks only. '
                      'Switch: CEConfig [worldgen.systems] templateBlockSwaps.'},
        'templates': marks}
    OUT_JSON.write_text(json.dumps(doc, indent=1) + '\n')
    write(owners, family_of, marks, review, red_in)
    print(f"{sum(len(v) for v in marks.values())} lone meat block(s) marked in {len(marks)} template(s); "
          f"{sum(len(v) for v in review.values())} other lone red block(s) for review -> {OUT_MD.relative_to(ROOT)}")


def write(owners, family_of, marks, review, red_in):
    L = ['# Meat blocks: lone red blocks in structure templates', '',
         'Written by `python3 tools/meat_block_scan.py`. Do not edit. Every red block (' + ', '.join(f'`{b.split(":")[1]}`' for b in RED)
         + ') in the templates of live Overworld structures. **Lone** = no neighbour of the same block. **Marked**: lone '
         '`red_glazed_terracotta` in a settlement-family structure (village, house, camp, fort, pillager) or a food-named '
         'template, to become `architects_palette:salmon_log[axis=y]` (marks in '
         '`kubejs/generation_scripts/worldgen/meat_block_swaps.json`). **Review**: every other lone red block.', '']
    total = Counter()
    for t in owners:
        for _, blk, lone in red_in(t):
            total[(blk, lone)] += 1
    L += ['## Red blocks overall', '', '| Block | Lone | In groups |', '|---|---:|---:|']
    for b in RED:
        if total[(b, True)] or total[(b, False)]:
            L.append(f"| `{b}` | {total[(b, True)]} | {total[(b, False)]} |")
    L += ['', f'## Marked ({sum(len(v) for v in marks.values())} blocks in {len(marks)} templates)', '',
          '| Template | Structures | Positions |', '|---|---|---|']
    for t, rows in marks.items():
        L.append(f"| `{t}` | {', '.join(f'`{s}`' for s in sorted(owners[t]))} | "
                 + ', '.join(f"{p[0]} {p[1]} {p[2]}" for p in (r['pos'] for r in rows)) + ' |')
    L += ['', f'## Review ({sum(len(v) for v in review.values())} lone red blocks, not marked)', '',
          'Settlement = in a settlement-family structure or a food-named template.', '',
          '| Template | Structures | Settlement | Lone blocks |', '|---|---|---|---|']
    for t in sorted(review, key=lambda t: (not review[t][0][2], t)):
        c = Counter(b.split(':')[1] for _, b, _ in review[t])
        L.append(f"| `{t}` | {', '.join(f'`{s}`' for s in sorted(owners[t])[:3])}{' ...' if len(owners[t]) > 3 else ''} | "
                 f"{'yes' if review[t][0][2] else 'no'} | {', '.join(f'{b} {n}' for b, n in c.most_common())} |")
    OUT_MD.write_text('\n'.join(L) + '\n')


if __name__ == '__main__':
    main()
