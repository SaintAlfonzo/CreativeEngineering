#!/usr/bin/env python3
"""Wood mismatch: every live land structure, the woods its templates are built from, and how much of that wood is
foreign to the biomes it can spawn in (feature_writeup/structure_tuning.md section 7, "matching wood").

    python3 tools/wood_mismatch_scan.py [--json OUT]

Native woods of a biome = the wood fits of biome_fits.json that list it (WOOD_FITS: oak -> land/oak, dark oak ->
land/dark_forest, ...). Crimson and warped are native nowhere in the Overworld; a biome in no wood fit (sand deserts,
bare peaks) has no native wood. Wood blocks are counted over every template the structure can place (the jigsaw walk
of terrain_adaptation_scan, pieces pool_tags.json removes skipped), by structure_biome_coverage.family().

Per structure:
  wood %      wood blocks / all counted blocks (air, markers, plants, pads, fluids left out)
  mismatch %  area-weighted over its land biomes: the share of its wood that is foreign there
  keep %      share of its current land area where its main wood is native (what a restriction to that wood's fit keeps)
Verdict (first rule that matches):
  unchanged   wood < 10% of the blocks, or < 40 wood blocks, or mismatch < 20%
  woodless    >= 60% of its area is biomes with no native tree (deserts, mushroom fields): any wood is "foreign";
              needs a rule for what wood a desert or mushroom build may use
  trim        its main wood is native almost everywhere it spawns (keep >= 90%): only secondary trim woods are foreign
  swap        a settlement (type/village) or a fort, or keep < 35% (restricting would cut it to a fraction):
              keep the biomes, swap the wood per biome (no such system yet)
  restrict    put it in its main wood's fit (land/<wood>)
Writes feature_writeup/wood_mismatch.md.
"""
import json
import sys
from collections import Counter, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_structure_groups as GSG  # noqa: E402
import loot_breakdown_scan as S  # noqa: E402
import nether_blocks_scan as N  # noqa: E402
import structure_biome_coverage as C  # noqa: E402
import terrain_adaptation_scan as T  # noqa: E402

ROOT = GSG.INSTANCE
OUT_MD = ROOT / 'feature_writeup/wood_mismatch.md'
WOOD_FITS = {'oak': 'land/oak', 'spruce': 'land/spruce', 'birch': 'land/birch', 'dark_oak': 'land/dark_forest',
             'acacia': 'land/acacia', 'jungle': 'land/jungle', 'mangrove': 'land/mangrove', 'cherry': 'land/cherry'}
WOODS = set(C.WOODS)
# structure families for the breakdown, first match wins (type/* tags)
FAMILIES = [('type/village', 'Villages and towns'), ('type/fort', 'Pillager forts'), ('pillager', 'Pillager towers and camps'),
            ('type/tower', 'Towers'), ('type/house', 'Houses and lone buildings'), ('type/camp', 'Camps'),
            ('type/ruin', 'Ruins'), ('type/ship', 'Ships'), ('type/shipwreck', 'Shipwrecks')]


def main():
    out_json = sys.argv[sys.argv.index('--json') + 1] if '--json' in sys.argv else None
    pack = GSG.Pack()
    raw, cache_b = GSG.load_biome_tags(), {}
    fits = GSG.read_json(GSG.FITS)['fits']

    def fit_biomes(name):
        out = set()
        for b in fits.get(name, {}).get('biomes', []):
            out |= GSG.resolve_biome_tag(GSG.full_id(b[1:]), raw, cache_b) if b.startswith('#') else {GSG.full_id(b)}
        return out
    native = defaultdict(set)
    for wood, fit in WOOD_FITS.items():
        for b in fit_biomes(fit):
            native[b].add(wood)

    biomes = GSG.read_json(GSG.BIOME_CONFIG)['dimensions']['minecraft:overworld']['biomes']
    land = {b: i['result_pct'] for b, i in biomes.items()
            if i.get('layer') == 'surface' and (i.get('result_pct') or 0) > 0 and not C.WATER.search(b)}
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
    gone = N.removed_pieces({k for p in live.values() for k in p.get('elements', {})})
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
        if sid in pack.meta or sid in off or sid in pack.blocked:
            continue
        on_land = {b: land[b] for b in pack.pools[sid] if b in land}
        if not on_land:
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
        reach = [t for t in reach if t not in gone]
        total, wood = 0, Counter()
        for t in reach:
            for block, n in materials(t).items():
                total += n
                f = C.family(block)
                if f in WOODS:
                    wood[f] += n
        wsum = sum(wood.values())
        if not total:
            continue
        area = sum(on_land.values())
        mism = sum(a * sum(n for w, n in wood.items() if w not in native[b]) / wsum for b, a in on_land.items()) / area if wsum else 0
        main = wood.most_common(1)[0][0] if wsum else None
        keep = sum(a for b, a in on_land.items() if main in native[b]) / area if main else 1
        woodless = sum(a for b, a in on_land.items() if not native[b]) / area
        tags = {t for t, m in pack.tag_members.items() if sid in m}
        fam = next((label for t, label in FAMILIES if t in tags), 'Other')
        wood_pct = wsum / total
        if wood_pct < 0.10 or wsum < 40 or mism < 0.20:
            verdict = 'unchanged'
        elif woodless >= 0.60:
            verdict = 'woodless'
        elif keep >= 0.90:
            verdict = 'trim'
        elif fam in ('Villages and towns', 'Pillager forts') or keep < 0.35 or main in ('crimson', 'warped'):
            verdict = 'swap'
        else:
            verdict = 'restrict'
        # where the foreign wood shows up most (area-weighted)
        bad = Counter()
        for b, a in on_land.items():
            if wsum and any(w not in native[b] for w in wood):
                bad[b] += a * sum(n for w, n in wood.items() if w not in native[b]) / wsum
        rows.append({'id': sid, 'family': fam, 'group': group_of.get(sid, '-'), 'fitted': pack.fitted.get(sid, []),
                     'wood_pct': round(wood_pct * 100), 'wood': wood.most_common(), 'main': main,
                     'mismatch': round(mism * 100), 'keep': round(keep * 100), 'woodless': round(woodless * 100), 'n_biomes': len(on_land),
                     'worst': [b for b, _ in bad.most_common(4)], 'verdict': verdict,
                     'fit': WOOD_FITS.get(main)})
    cache.save()
    if out_json:
        Path(out_json).write_text(json.dumps(rows, indent=1))
    write(rows)
    v = Counter(r['verdict'] for r in rows)
    print(f"{len(rows)} live land structures: {v['unchanged']} unchanged, {v['trim']} trim, {v['woodless']} woodless, {v['restrict']} restrict, {v['swap']} swap "
          f"-> {OUT_MD.relative_to(ROOT)}")


def short(b):
    return b.replace('minecraft:', '').replace('terralith:', 'T:')


def write(rows):
    L = ['# Wood mismatch: structures built of wood foreign to their biomes', '',
         'Written by `python3 tools/wood_mismatch_scan.py`. Do not edit. Every live land structure; wood counted over '
         'every template it can place; native woods per biome from the wood fits in `biome_fits.json`. **Wood** = wood '
         'blocks / all blocks. **Mismatch** = share of its wood that is foreign where it spawns (area-weighted). '
         '**Keep** = share of its land area where its main wood is native (what restricting it to that wood keeps). '
         'Verdicts: **restrict** (to its main wood fit), **swap** (keep the biomes, swap the wood per biome: '
         'settlements, forts, or a restriction that would keep under 35%), **trim** (main wood native almost everywhere, '
         'only trim woods foreign), **woodless** (mostly biomes with no native tree: deserts, mushroom fields), '
         '**unchanged** (little wood or little mismatch).', '']
    order = [label for _, label in FAMILIES] + ['Other']
    for verdict in ('restrict', 'swap', 'trim', 'woodless', 'unchanged'):
        sel = [r for r in rows if r['verdict'] == verdict]
        L += [f'## {verdict.capitalize()} ({len(sel)})', '']
        for fam in order:
            fr = sorted((r for r in sel if r['family'] == fam), key=lambda r: -r['mismatch'])
            if not fr:
                continue
            L += [f'### {fam} ({len(fr)})', '', '| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |',
                  '|---|---|---|---:|---|---:|---:|---|']
            for r in fr:
                woods = ', '.join(f'{w} {n}' for w, n in r['wood'][:4])
                fits = ' + '.join(f.replace('land/', '') for f in r['fitted'][:4] if f != 'land') or 'own list'
                L.append(f"| `{r['id']}` | {r['group']} | {fits} | {r['wood_pct']}% | {woods} | {r['mismatch']}% | "
                         f"{r['keep']}% | {', '.join(short(b) for b in r['worst'])} |")
            L.append('')
    OUT_MD.write_text('\n'.join(L) + '\n')


if __name__ == '__main__':
    main()
