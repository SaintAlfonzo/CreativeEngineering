#!/usr/bin/env python3
"""
Earth-fill base scan: what the bottom layers (EarthFill baseLayers, 3) of a structure's rigid templates are made of,
so you can tell whether the earth-fill water / air limits can see its foundation (only blocks in the block tag
creativeworldgen:earth_fill/earth count, plus earth_fill/stone_base for structures in the structure tag of that
name). Written 2026-11-02 for the desert check (TODO structures/earthfill #19).

  python3 tools/earth_base_scan.py                 structures in land/desert and land/badlands
  python3 tools/earth_base_scan.py <id> [<id> ...] those structures (ids as in structure_tags.json)

Per structure: templates reached (rigid only, as the scan), block counts in layers 0..2 by class:
earth (counts today), sandstone, terracotta, stone (stone_base), wood, other built. Air and structure voids skipped.
Uses the template reader and pool walk of tools/terrain_adaptation_scan.py (same sources: mod jars, kubejs/data).
"""
import json
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import terrain_adaptation_scan as T  # noqa: E402

ROOT = T.ROOT
LAYERS = 3
EARTH = {'minecraft:dirt', 'minecraft:grass_block', 'minecraft:coarse_dirt', 'minecraft:podzol', 'minecraft:rooted_dirt',
         'minecraft:mud', 'minecraft:moss_block', 'minecraft:mycelium', 'minecraft:sand', 'minecraft:red_sand',
         'minecraft:gravel', 'minecraft:clay', 'minecraft:dirt_path', 'minecraft:farmland', 'minecraft:muddy_mangrove_roots'}
SKIP = {'minecraft:air', 'minecraft:cave_air', 'minecraft:structure_void', 'minecraft:jigsaw', 'minecraft:structure_block'}


def klass(name):
    if name in EARTH:
        return 'earth'
    if 'sandstone' in name:
        return 'sandstone'
    if 'terracotta' in name:
        return 'terracotta'
    if any(w in name for w in ('stone', 'cobble', 'andesite', 'diorite', 'granite', 'deepslate', 'brick', 'basalt', 'tuff')):
        return 'stone'
    if any(w in name for w in ('plank', '_log', '_wood', 'stripped_', 'fence', '_slab', '_stairs')):
        return 'wood'
    return 'other'


def base_counts(raw):
    d = T.S.read_template(raw)
    if d is None:
        return None
    palette = d.get('palette') or (d.get('palettes') or [[]])[0]
    names = [p.get('Name', 'minecraft:air') for p in palette]
    c, ids = Counter(), Counter()
    for b in d.get('blocks', []):
        s = b.get('state', 0)
        if s >= len(names) or int(b['pos'][1]) >= LAYERS:
            continue
        n = names[s]
        if n in SKIP:
            continue
        c[klass(n)] += 1
        ids[n] += 1
    return c, ids


def main():
    tags = json.loads((T.WORLDGEN / 'structure_tags.json').read_text())['tags']
    want = sys.argv[1:] or sorted(set(tags['land/desert']['values']) | set(tags['land/badlands']['values']))
    structs, pools, templates, _ = T.load_worldgen()
    live = json.loads((T.WORLDGEN / 'pool_config.json').read_text())['pools']
    pad, plants, _ = T.load_tags()
    cache = T.TemplateCache(templates, pad, plants, False)
    for sid in want:
        d = structs.get(sid)
        start = T.G.norm_id(d['start_pool']) if d and d.get('start_pool') else None
        size = int((d or {}).get('size', 1) or 1)
        if not start and sid in T.VANILLA_JIGSAW:
            start, size, _ta = T.VANILLA_JIGSAW[sid]
        if not start:
            print(f'{sid}: not a jigsaw (legacy or custom pieces), no templates to read')
            continue
        reach = T.reach(start, size, pools, live, cache.get)
        tot, ids, n = Counter(), Counter(), 0
        for tid, proj in reach.items():
            if proj == 'terrain_matching' or tid not in templates:
                continue
            label, read, name = templates[tid]
            r = base_counts(read(name))
            if not r:
                continue
            n += 1
            tot.update(r[0])
            ids.update(r[1])
        all_ = sum(tot.values()) or 1
        share = ', '.join(f'{k} {v} ({100 * v // all_}%)' for k, v in tot.most_common())
        top = ', '.join(f'{k.split(":")[1]} {v}' for k, v in ids.most_common(5))
        print(f'{sid}: {n} templates | {share or "nothing"} | top: {top}')


if __name__ == '__main__':
    main()
