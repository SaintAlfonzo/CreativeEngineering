#!/usr/bin/env python3
"""Structure biome coverage: every structure that can stand on land, how much of the land its biome list covers, and
what it is built from. Finds the structures still limited to a subset of biomes (feature_writeup/structure_tuning.md
7.2, the plan to give them biome fits like the towers in 3.17).

Biome lists are resolved the way the rate model sees them (gen_structure_groups.Pack: own list or tag, Terralith's tag
additions, biome_fits.json, the water rule), cut to the land biomes that generate here (overworld
surface layer, result_pct > 0 in biome_config.json, no ocean or river). Materials are block counts over every template
the structure can place (terrain_adaptation_scan's jigsaw walk), by wood or stone family.

    python3 tools/structure_biome_coverage.py [--all] [--json OUT]

Prints structures under 95% of the land by area (--all: every land structure), smallest share first:
share, land biome count, Terralith biome count, id, environment, group, OFF/BLOCKED/FITTED, type tags, materials.
"""
import json
import re
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_structure_groups as GSG  # noqa: E402
import loot_breakdown_scan as S  # noqa: E402
import terrain_adaptation_scan as T  # noqa: E402

ROOT = GSG.INSTANCE
WATER = re.compile(r'ocean|river')
WOODS = ['dark_oak', 'mangrove', 'crimson', 'warped', 'spruce', 'acacia', 'jungle', 'cherry', 'birch', 'oak']
WOOD_PARTS = ('log', 'wood', 'plank', 'stairs', 'slab', 'fence', 'door', 'trapdoor', 'button', 'pressure', 'sign')
STONES = [('red_sandstone', 'red_sandstone'), ('sandstone', 'sandstone'), ('terracotta', 'terracotta'),
          ('mud_brick', 'mud_brick'), ('packed_ice', 'ice'), ('blue_ice', 'ice'), ('snow', 'snow'),
          ('deepslate', 'deepslate'), ('blackstone', 'blackstone'), ('stone_brick', 'stone_brick'),
          ('cobblestone', 'cobblestone'), ('andesite', 'andesite'), ('prismarine', 'prismarine'), ('purpur', 'purpur'),
          ('end_stone', 'end_stone'), ('bamboo', 'bamboo'), ('quartz', 'quartz'), ('brick', 'brick'), ('wool', 'wool'),
          ('hay', 'hay')]


def family(block):
    path = block.split(':', 1)[1].removeprefix('stripped_')
    for w in WOODS:   # longest names first: dark_oak before oak
        if path.startswith(w + '_') and any(k in path for k in WOOD_PARTS):
            return w
    for key, fam in STONES:
        if key in path:
            return fam
    return None


def main():
    show_all = '--all' in sys.argv
    out_json = sys.argv[sys.argv.index('--json') + 1] if '--json' in sys.argv else None

    pack = GSG.Pack()
    biomes = GSG.read_json(GSG.BIOME_CONFIG)['dimensions']['minecraft:overworld']['biomes']
    land = {b: i['result_pct'] for b, i in biomes.items()
            if i.get('layer') == 'surface' and (i.get('result_pct') or 0) > 0 and not WATER.search(b)}
    total = sum(land.values())
    scan = {r['id']: r for r in GSG.read_json(T.OUT_JSON)['structures']} if T.OUT_JSON.exists() else {}
    groups = GSG.read_json(GSG.GROUPS)
    group_of, off = {}, set()
    for part in ('groups', 'standalone'):
        for name, v in groups.get(part, {}).items():
            for sid, s in v.get('structures', {}).items():
                group_of[sid] = name if part == 'groups' else 'standalone'
                if s.get('enabled') is False or s.get('frequency', 1) == 0:
                    off.add(sid)

    structs, pools, templates, _ = T.load_worldgen()
    live = GSG.read_json(T.WORLDGEN / 'pool_config.json')['pools']
    pad, plants, _ = T.load_tags()
    cache = T.TemplateCache(templates, pad, plants, False)
    ignore = T.AIR | T.MARKER | pad | plants | {'minecraft:water', 'minecraft:lava'}
    memo = {}

    def materials(tid):
        if tid not in memo:
            c = Counter()
            if tid in templates:
                _, read, name = templates[tid]
                d = S.read_template(read(name))
                if d:
                    pal = d.get('palette') or (d.get('palettes') or [[]])[0]
                    names = [p.get('Name', 'minecraft:air') for p in pal]
                    for b in d.get('blocks', []):
                        s = b.get('state', 0)
                        if s < len(names) and names[s] not in ignore:
                            c[names[s]] += 1
            memo[tid] = c
        return memo[tid]

    rows = []
    for sid in sorted(pack.pools):
        if sid in pack.meta:
            continue
        on_land = {b for b in pack.pools[sid] if b in land}
        if not on_land:
            continue
        d = structs.get(sid)
        if d and d.get('start_pool'):
            reach = T.reach(T.G.norm_id(d['start_pool']), int(d.get('size', 1) or 1), pools, live, cache.get)
        elif sid in T.VANILLA_JIGSAW:
            sp, size, _ = T.VANILLA_JIGSAW[sid]
            reach = T.reach(sp, size, pools, live, cache.get)
        elif d:   # single-template mods: every template of the namespace
            ns = sid.split(':')[0]
            reach = {t: 'rigid' for t in templates if t.startswith(ns + ':')}
        else:
            reach = {}
        fam = Counter()
        for t in reach:
            for block, n in materials(t).items():
                f = family(block)
                if f:
                    fam[f] += n
        rows.append({'id': sid, 'env': scan.get(sid, {}).get('env', '?'), 'group': group_of.get(sid, '-'),
                     'off': sid in off, 'blocked': sid in pack.blocked, 'fitted': pack.fitted.get(sid),
                     'n': len(on_land), 'n_terralith': sum(1 for b in on_land if b.startswith('terralith:')),
                     'share': round(sum(land[b] for b in on_land) / total * 100, 1), 'biomes': sorted(on_land),
                     'materials': fam.most_common(5),
                     'tags': sorted(t for t, m in pack.tag_members.items() if sid in m and t.startswith('type/'))})
    cache.save()

    if out_json:
        Path(out_json).write_text(json.dumps({'land': land, 'rows': rows}, indent=1))
    print(f'{len(land)} land biomes, {len(rows)} structures can stand on land')
    for r in sorted(rows, key=lambda r: r['share']):
        if r['share'] >= 95 and not show_all:
            continue
        flag = 'OFF' if r['off'] else 'BLOCKED' if r['blocked'] else 'FITTED' if r['fitted'] else ''
        mats = ', '.join(f'{f} {n}' for f, n in r['materials'])
        print(f"{r['share']:5.1f}% n={r['n']:3d} tl={r['n_terralith']:2d} {r['id']:60s} {r['env']:12s} {r['group']:20s} "
              f"{flag:7s} {','.join(r['tags'])} | {mats}")


if __name__ == '__main__':
    main()
