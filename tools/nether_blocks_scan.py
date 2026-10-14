#!/usr/bin/env python3
"""Nether blocks in Overworld structures: every structure that can spawn in an Overworld biome, the Nether blocks its
templates hold, by tier (feature_writeup/structure_tuning.md section 7, "Overworld structures with Nether blocks").

    python3 tools/nether_blocks_scan.py [--json OUT]

Tiers (NETHER_TIERS below; edit there):
  1  netherrack, soul sand / soul soil, nether wart (the user's main concern)
  2  crying obsidian, the blackstone family (gilded included), basalt (not smooth basalt, which the Overworld has)
  3  every other Nether block: nether bricks, crimson / warped wood and plants, nylium, wart blocks, shroomlight,
     soul fire / torch / lantern / campfire, magma, glowstone, Nether ores, ancient debris, respawn anchor...

What a structure can place: jigsaw structures walk their pools (terrain_adaptation_scan.reach, live pool weights;
pieces pool_tags.json removes with multiplier 0 are skipped); single-template structures use the templates whose path
names the structure, else every template of the mod not named after another of its structures ("approx"). Not seen: blocks a processor list
swaps in at generation, and blocks code-built structures place without a template.

Writes feature_writeup/nether_blocks_scan.md.
"""
import fnmatch
import json
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_structure_groups as GSG  # noqa: E402
import loot_breakdown_scan as S  # noqa: E402
import terrain_adaptation_scan as T  # noqa: E402

ROOT = GSG.INSTANCE
OUT_MD = ROOT / 'feature_writeup/nether_blocks_scan.md'
POOL_TAGS = T.WORLDGEN / 'pool_tags.json'

# (tier, label, regex on the block path, namespace minecraft unless the pattern has a colon)
NETHER_TIERS = [
    (1, 'netherrack', r'netherrack'),
    (1, 'soul sand/soil', r'soul_(sand|soil)'),
    (1, 'nether wart', r'nether_wart$'),
    (2, 'crying obsidian', r'crying_obsidian'),
    (2, 'gilded blackstone', r'gilded_blackstone'),
    (2, 'blackstone', r'blackstone'),
    (2, 'basalt', r'^(polished_)?basalt$'),
    (3, 'nether bricks', r'nether_brick'),
    (3, 'crimson/warped', r'crimson|warped'),
    (3, 'wart block', r'wart_block'),
    (3, 'shroomlight', r'shroomlight'),
    (3, 'soul fire/light', r'soul_(fire|torch|wall_torch|lantern|campfire)'),
    (3, 'magma', r'magma_block'),
    (3, 'glowstone', r'^glowstone$'),
    (3, 'nether ores', r'nether_(gold|quartz)_ore|ancient_debris'),
    (3, 'nether vines/plants', r'weeping_vines|twisting_vines|nether_sprouts'),
    (3, 'respawn anchor', r'respawn_anchor'),
]
_COMPILED = [(t, label, re.compile(p)) for t, label, p in NETHER_TIERS]


def classify(block):
    """(tier, label) for a Nether block id, else None. Only minecraft: blocks; a mod's netherrack-like block isn't guessed."""
    ns, _, path = block.partition(':')
    if ns != 'minecraft':
        return None
    path = path.removeprefix('stripped_')
    for tier, label, rx in _COMPILED:
        if rx.search(path):
            return tier, label
    return None


def removed_pieces(all_pieces):
    """Pool pieces pool_tags.json multiplies by 0 somewhere (the tech village houses...)."""
    if not POOL_TAGS.exists():
        return set()
    tags = GSG.read_json(POOL_TAGS).get('tags', {})
    memo = {}

    def members(name, stack=()):
        if name in memo:
            return memo[name]
        inc, exc = set(), set()
        for v in tags.get(name, {}).get('values', []):
            neg = v.startswith('!')
            v = v.lstrip('!')
            if v.startswith('#'):
                got = members(v[1:], stack + (name,)) if v[1:] not in stack else set()
            elif '*' in v:
                got = {p for p in all_pieces if fnmatch.fnmatchcase(p, v)}
            else:
                got = {v}
            (exc if neg else inc).update(got)
        memo[name] = inc - exc
        return memo[name]

    out = set()
    for name, d in tags.items():
        if d.get('multiplier') == 0 and not d.get('pools'):
            out |= members(name)
    return out


def main():
    out_json = sys.argv[sys.argv.index('--json') + 1] if '--json' in sys.argv else None
    pack = GSG.Pack()
    dims = GSG.read_json(GSG.BIOME_CONFIG)['dimensions']
    overworld = set(dims['minecraft:overworld']['biomes'])
    nether = set(dims['minecraft:the_nether']['biomes'])
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
    gone = removed_pieces({k for p in live.values() for k in p.get('elements', {})})
    pad, plants, _ = T.load_tags()
    cache = T.TemplateCache(templates, pad, plants, False)
    memo = {}

    def nether_in(tid):
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
                        if s < len(names) and classify(names[s]):
                            c[names[s]] += 1
            memo[tid] = c
        return memo[tid]

    rows = []
    for sid in sorted(pack.pool_all):
        if sid in pack.meta or not (pack.pool_all[sid] & overworld):
            continue
        d = structs.get(sid)
        approx = False
        if d and d.get('start_pool'):
            reach = T.reach(T.G.norm_id(d['start_pool']), int(d.get('size', 1) or 1), pools, live, cache.get)
        elif sid in T.VANILLA_JIGSAW:
            sp, size, _ = T.VANILLA_JIGSAW[sid]
            reach = T.reach(sp, size, pools, live, cache.get)
        else:
            ns, _, path = sid.partition(':')
            leaf = path.rsplit('/', 1)[-1]
            reach = {t: 'rigid' for t in templates if t.startswith(ns + ':') and leaf in t.split(':', 1)[1]}
            if not reach and d:
                # every template of the mod except those that name one of its other structures (YUNG's spider
                # dungeon is built in code: it must not inherit the small Nether dungeon's templates)
                siblings = [o.partition(':')[2].rsplit('/', 1)[-1] for o in structs if o.startswith(ns + ':') and o != sid]
                reach = {t: 'rigid' for t in templates if t.startswith(ns + ':')
                         and not any(x in t.split(':', 1)[1] for x in siblings)}
                approx = bool(reach)
        reach = {t: p for t, p in reach.items() if t not in gone}
        blocks, where = Counter(), defaultdict(Counter)
        for t in reach:
            for block, n in nether_in(t).items():
                blocks[block] += n
                where[t][block] += n
        if not blocks:
            continue
        tiers = Counter()
        for block, n in blocks.items():
            tiers[classify(block)[0]] += n
        rows.append({'id': sid, 'group': group_of.get(sid, '-'), 'off': sid in off, 'blocked': sid in pack.blocked,
                     'approx': approx, 'templates': len(reach), 'tiers': dict(tiers),
                     'blocks': blocks.most_common(), 'where': {t: c.most_common() for t, c in where.items()},
                     'also_nether': bool(pack.pool_all[sid] & nether)})
    cache.save()
    rows.sort(key=lambda r: (-r['tiers'].get(1, 0), -r['tiers'].get(2, 0), -r['tiers'].get(3, 0), r['id']))
    if out_json:
        Path(out_json).write_text(json.dumps(rows, indent=1))
    write(rows)
    live_rows = [r for r in rows if not r['off'] and not r['blocked']]
    print(f'{len(rows)} Overworld structures hold Nether blocks ({len(live_rows)} live); '
          f'tier 1 in {sum(1 for r in live_rows if r["tiers"].get(1))} live -> {OUT_MD.relative_to(ROOT)}')


def write(rows):
    L = ['# Nether blocks in Overworld structures', '',
         'Written by `python3 tools/nether_blocks_scan.py`. Do not edit. Every structure that can spawn in an Overworld '
         'biome (rate model biome lists: fits and biome rules applied) whose templates hold Nether blocks. Counts are '
         'blocks summed over every template the structure can place (a jigsaw village counts each house once, not as '
         'often as it is built). Not seen: processor-list swaps and code-built blocks.', '',
         '**Tier 1** netherrack, soul sand/soil, nether wart. **Tier 2** crying obsidian, blackstone family (gilded '
         'included), basalt. **Tier 3** other Nether blocks (nether bricks, crimson/warped, wart blocks, shroomlight, '
         'soul fire/light, magma, glowstone, Nether ores...).', '',
         'Status: OFF = disabled in `structure_groups.json`, BLOCKED = another mod stops it, approx = templates matched '
         'by mod, not by structure. "Also Nether" = its biome list includes Nether biomes too.', '']
    live = [r for r in rows if not r['off'] and not r['blocked']]
    L += ['## Summary', '', '| Structure | Group | Status | T1 | T2 | T3 | Blocks |', '|---|---|---|---:|---:|---:|---|']
    for r in rows:
        st = ', '.join(x for x in ('OFF' if r['off'] else '', 'BLOCKED' if r['blocked'] else '', 'approx' if r['approx'] else '',
                                   'also Nether' if r['also_nether'] else '') if x)
        bl = ', '.join(f"{b.split(':')[1]} {n}" for b, n in r['blocks'][:6]) + (' ...' if len(r['blocks']) > 6 else '')
        L.append(f"| `{r['id']}` | {r['group']} | {st} | {r['tiers'].get(1, '')} | {r['tiers'].get(2, '')} | "
                 f"{r['tiers'].get(3, '')} | {bl} |")
    L += ['', f'{len(rows)} structures, {len(live)} live; tier 1 in '
          f'{sum(1 for r in live if r["tiers"].get(1))}, tier 2 in {sum(1 for r in live if r["tiers"].get(2))}, '
          f'tier 3 only in {sum(1 for r in live if not r["tiers"].get(1) and not r["tiers"].get(2))}.', '']
    L += ['## By block', '', '| Block | Tier | Live structures | Total blocks |', '|---|---:|---:|---:|']
    by_block = defaultdict(lambda: [0, 0])
    for r in live:
        for b, n in r['blocks']:
            by_block[b][0] += 1
            by_block[b][1] += n
    for b, (k, n) in sorted(by_block.items(), key=lambda x: (classify(x[0])[0], -x[1][0], x[0])):
        L.append(f'| `{b}` | {classify(b)[0]} | {k} | {n} |')
    L += ['', '## Templates (live structures with tier 1 or 2)', '']
    for r in live:
        if not (r['tiers'].get(1) or r['tiers'].get(2)):
            continue
        L.append(f"### `{r['id']}`")
        L.append('')
        for t, bl in sorted(r['where'].items(), key=lambda x: -sum(n for b, n in x[1] if classify(b)[0] <= 2)):
            if not any(classify(b)[0] <= 2 for b, _ in bl):
                continue
            L.append(f"- `{t}`: " + ', '.join(f"{b.split(':')[1]} {n}" for b, n in bl))
        L.append('')
    OUT_MD.write_text('\n'.join(L) + '\n')


if __name__ == '__main__':
    main()
