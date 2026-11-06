#!/usr/bin/env python3
"""Where wild bee nests generate and how often: before and after bee_nests.json, and as measured in game.

    python3 tools/gen_bee_nests.py

Writes feature_writeup/bee_nests_by_biome.md (feature_writeup/bee_nests.md is the reference).

Wild nests only come from the minecraft:beehive tree decorator (bees have no natural spawn in any biome). Each tree
rolls its decorator's probability once, and the nest then needs a free spot beside the trunk under the lowest
leaves. So per biome: nests per chunk = sum over its tree features of (trees per chunk x nest chance per tree).

Offline estimate ("before" = the mods as loaded + the pack's wild_crops.json changes; "after" = bee_nests.json
applied the way kubejs/data/creativeworldgen/kubejs/worldgen/bee_nests.js and patcherMod BeeNests apply it):
- tree counts are placement ATTEMPTS: block/noise filters, the biome check and failed trunks are not simulated,
  so heavily filtered features (Terralith's 200-try Yellowstone birches) read far too high. Nest chances are exact.
- bushes (trunk at most 1 tall) never get a nest: the decorator looks for a log above the first one.
- Java-only vanilla trees that no jar overrides as JSON are modelled in JAVA_TREES.
The measured table (section 2) comes from /dumpbeenests (kubejs/exported/bee_nests.json) and is the real number.

Calibration: bee_nests_baseline.json is a /dumpbeenests taken with no rules (copy kubejs/exported/bee_nests.json
there after a no-rules run). For a biome with at least MIN_CHUNKS measured chunks, "before" is the measured nest
rate and "after" splits the measured trees into nest-carrying ones (measured nests / the offline mean chance) and
the rest; see calibrate(). Extra trees from `trees` rules are added at their written rate.

Biome traits (biome_traits.json) and the where/add/except selector come from tools/gen_crop_worldgen_table.py.
"""
import json
import math
import sys
from collections import defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_crop_worldgen_table as g  # noqa: E402
import gen_wild_flowers as gw  # noqa: E402

INSTANCE = g.INSTANCE
CONFIG_PATH = INSTANCE / 'kubejs/generation_scripts/worldgen/bee_nests.json'
DUMP_PATH = INSTANCE / 'kubejs/exported/bee_nests.json'
# A /dumpbeenests taken with NO rules, kept as tuning input: real trees and nests per chunk per biome.
BASELINE_PATH = INSTANCE / 'kubejs/generation_scripts/worldgen/bee_nests_baseline.json'
MIN_CHUNKS = 50
OUT_MD = INSTANCE / 'feature_writeup/bee_nests_by_biome.md'
DEFAULT_SAPLING = 'minecraft:oak_sapling'
NEVER = {'ocean', 'cave'}

# Vanilla placed features registered in Java (TreePlacements, 1.19.2 mojmap) that the biome files list and no jar
# overrides as JSON. Their configured trees ARE json (Terralith / Geophilic / Immersive Weathering ship them).
_TREE_PL = [{'type': 'minecraft:in_square'}, {'type': 'minecraft:surface_water_depth_filter', 'max_water_depth': 0},
            {'type': 'minecraft:heightmap', 'heightmap': 'OCEAN_FLOOR'}, {'type': 'minecraft:biome'}]
_FAKE_TREE = {'type': 'minecraft:tree', 'config': {'trunk_placer': {'base_height': 4, 'height_rand_a': 2, 'height_rand_b': 0},
                                                   'decorators': []}}
JAVA_TREES = {
    # MEADOW_TREES: random_selector(fancy_oak_bees 0.5, default super_birch_bees), RarityFilter 100
    'minecraft:trees_meadow': {'feature': {'type': 'minecraft:random_selector', 'config': {
        'features': [{'chance': 0.5, 'feature': {'feature': 'minecraft:fancy_oak_bees', 'placement': []}}],
        'default': {'feature': 'minecraft:super_birch_bees', 'placement': []}}},
        'placement': [{'type': 'minecraft:rarity_filter', 'chance': 100}] + _TREE_PL},
    # TreeFeatures.OAK, countExtra(5, 0.1, 1)
    'minecraft:trees_badlands': {'feature': _FAKE_TREE, 'placement': [{'type': 'minecraft:count', 'count': 5.1}] + _TREE_PL},
    # TreeFeatures.SPRUCE, countExtra(0, 0.1, 1)
    'minecraft:trees_snowy': {'feature': _FAKE_TREE, 'placement': [{'type': 'minecraft:count', 'count': 0.1}] + _TREE_PL},
}


def js_round(x):
    return int(math.floor(x + 0.5))


def parse_rate(r):
    if isinstance(r, (int, float)):
        return float(r)
    s = str(r).strip()
    if '/' in s:
        a, b = s.split('/', 1)
        return float(a) / float(b)
    return float(s)


def tier_rate(rate):
    """The rate bee_nests.js actually writes: count round(rate), or one tree in round(1/rate) chunks."""
    return float(js_round(rate)) if rate >= 1 else 1 / max(1, js_round(1 / rate))


# ── placement rates ──────────────────────────────────────────────────────────

def _t(m):
    t = m.get('type', '')
    return t if ':' in t else 'minecraft:' + t


def mean_nonneg(c):
    """Mean of max(0, IntProvider): a negative count places nothing (Terralith uses that)."""
    if isinstance(c, (int, float)):
        return max(0.0, float(c))
    t = _t(c)
    if t == 'minecraft:constant':
        return max(0.0, float(c['value']))
    if t in ('minecraft:uniform', 'minecraft:biased_to_bottom', 'minecraft:clamped'):
        v = c.get('value', c)
        if 'source' in v:
            return mean_nonneg(v['source'])
        lo, hi = int(v['min_inclusive']), int(v['max_inclusive'])
        return sum(max(0, x) for x in range(lo, hi + 1)) / (hi - lo + 1)
    if t == 'minecraft:weighted_list':
        d = c['distribution']
        tw = sum(e['weight'] for e in d)
        return sum(mean_nonneg(e['data']) * e['weight'] for e in d) / tw if tw else 0.0
    return 1.0


def noise_count_mean(ratio, offset):
    """noise_based_count: ceil((noise + offset) * ratio), noise taken as uniform in [-1, 1]."""
    n = 200
    return sum(max(0, math.ceil(((i + 0.5) / n * 2 - 1 + offset) * ratio)) for i in range(n)) / n


def rate_of(placement):
    rate = 1.0
    for m in placement:
        t = _t(m)
        if t == 'minecraft:count':
            rate *= mean_nonneg(m['count'])
        elif t == 'minecraft:rarity_filter':
            rate /= m['chance']
        elif t == 'minecraft:noise_threshold_count':
            rate *= (max(0, m['below_noise']) + max(0, m['above_noise'])) / 2
        elif t == 'minecraft:noise_based_count':
            rate *= noise_count_mean(m.get('noise_to_count_ratio', 1), m.get('noise_offset', 0))
        elif t == 'minecraft:count_on_every_layer':
            rate *= mean_nonneg(m['count'])
    return rate


# ── tree walking ─────────────────────────────────────────────────────────────

def merge(dst, src, f=1.0):
    for k, v in src.items():
        dst[k] += v * f


def tree_nest_chance(cfg):
    """(nest chance, is_bush) of one minecraft:tree config."""
    tp = cfg.get('trunk_placer', {})
    height = tp.get('base_height', 2) + tp.get('height_rand_a', 0) + tp.get('height_rand_b', 0)
    p = sum(d.get('probability', 0) for d in cfg.get('decorators', []) if d.get('type') == 'minecraft:beehive')
    return min(1.0, p), height <= 1


class Walker:
    """Expected trees per placement, keyed by nest chance: {p: trees}. Bushes are left out."""

    def __init__(self, world):
        self.placed, self.configured = world['placed'], world['configured']
        self.cache = {}

    def feature(self, fid):
        if fid not in self.cache:
            self.cache[fid] = self.placed_walk(fid, frozenset())
        return self.cache[fid]

    def walk(self, v, seen):
        out = defaultdict(float)
        if isinstance(v, str):
            if v in self.configured and ('c', v) not in seen:
                return self.walk(self.configured[v][1], seen | {('c', v)})
            if v in self.placed and ('p', v) not in seen:
                return self.placed_walk(v, seen)
            return out
        if isinstance(v, list):
            for x in v:
                merge(out, self.walk(x, seen))
            return out
        if not isinstance(v, dict):
            return out
        t = v.get('type')
        cfg = v.get('config') if isinstance(v.get('config'), dict) else None
        if cfg and 'trunk_placer' in cfg:
            p, bush = tree_nest_chance(cfg)
            if not bush:
                out[p] += 1.0
            return out
        if t == 'minecraft:simple_random_selector' and cfg:
            fs = cfg.get('features') or []
            for f in fs:
                merge(out, self.placed_walk(f, seen), 1 / len(fs))
            return out
        if t == 'minecraft:random_selector' and cfg:
            left = 1.0
            for e in cfg.get('features') or []:
                merge(out, self.placed_walk(e.get('feature'), seen), left * e.get('chance', 0))
                left *= 1 - e.get('chance', 0)
            merge(out, self.placed_walk(cfg.get('default'), seen), left)
            return out
        if 'placement' in v and 'feature' in v:
            return self.placed_walk(v, seen)
        for k, x in v.items():
            if k != 'type':
                merge(out, self.walk(x, seen))
        return out

    def placed_walk(self, ref, seen):
        if isinstance(ref, str):
            if ref in self.placed and ('p', ref) not in seen:
                return self.placed_walk(self.placed[ref][1], seen | {('p', ref)})
            if ref in self.configured:
                return self.walk(ref, seen)
            return defaultdict(float)
        if not isinstance(ref, dict):
            return defaultdict(float)
        r = rate_of(ref.get('placement', []))
        out = self.walk(ref.get('feature'), seen)
        return defaultdict(float, {k: x * r for k, x in out.items()})


def nests(dist, eff=None):
    """Nests per chunk of a {p: trees} distribution, with a biome's (chance, scale) when given."""
    if eff is None:
        return sum(p * n for p, n in dist.items())
    chance, scale = eff
    return sum(min(1.0, max(p * scale, chance)) * n for p, n in dist.items())


# ── config ───────────────────────────────────────────────────────────────────

def load_config():
    return g.loads_lenient(CONFIG_PATH.read_text())


def resolve_rule(traits, rule, world):
    out = {b for b, tr in traits.items() if not tr & NEVER and any(g.clause_matches(tr, c) for c in rule.get('where', []))}
    for a in rule.get('add', []):
        out |= set(g.resolve_tag(a[1:], world['tags'], world['cache'])) if a.startswith('#') else {a}
    return sorted(out - set(rule.get('except', [])))


def check_rules(cfg, traits, world, problems):
    """Same checks as bee_nests.js invalid(); returns [(rule, biomes)] for the usable ones."""
    vocab = {t for tr in traits.values() for t in tr}
    seen, out = set(), []
    for n, r in enumerate(cfg.get('rules', [])):
        rid = r.get('id') or f'rule {n + 1}'
        bad = None
        if not r.get('id') or not all(c.islower() or c.isdigit() or c == '_' for c in r['id']):
            bad = 'id must be lowercase a-z 0-9 _'
        elif r['id'] in seen:
            bad = 'duplicate id'
        elif r.get('chance') is None and r.get('scale') is None and r.get('trees') is None:
            bad = 'needs chance, scale or trees'
        elif r.get('chance') is not None and not (isinstance(r['chance'], (int, float)) and 0 <= r['chance'] <= 1):
            bad = 'chance must be a number from 0 to 1'
        elif r.get('scale') is not None and not (isinstance(r['scale'], (int, float)) and 0 <= r['scale'] <= 1000):
            bad = 'scale must be a number from 0 to 1000'
        elif r.get('trees') is not None:
            t = r['trees']
            try:
                rate = parse_rate(t.get('rate'))
            except (TypeError, ValueError):
                rate = -1
            if ':' not in str(t.get('feature', '')):
                bad = 'trees.feature must be a configured feature id'
            elif not (0 < rate <= 64):
                bad = 'trees.rate must be above 0 and at most 64 per chunk'
            elif t['feature'] not in world['configured'] and not t['feature'].startswith('minecraft:'):
                bad = f"trees.feature {t['feature']} is not a configured feature in any jar (the world would not load)"
        if bad:
            problems.append(f'{rid}: {bad}')
            continue
        seen.add(r['id'])
        for clause in r.get('where', []):
            for tok in clause.split():
                if tok.lstrip('!') not in vocab:
                    problems.append(f"{rid}: trait '{tok.lstrip('!')}' is on no biome (typo?)")
        bs = resolve_rule(traits, r, world)
        if not bs:
            problems.append(f'{rid}: matches no biome')
            continue
        out.append((r, bs))
    return out


def fold(rules):
    """biome -> (chance, scale, [rule ids]); fields cascade in file order (BeeNests.resolve)."""
    out = {}
    for r, bs in rules:
        if r.get('chance') is None and r.get('scale') is None:
            continue
        for b in bs:
            c, s, ids = out.get(b, (0.0, 1.0, []))
            out[b] = (r['chance'] if r.get('chance') is not None else c,
                      r['scale'] if r.get('scale') is not None else s, ids + [r['id']])
    return out


# ── main ─────────────────────────────────────────────────────────────────────

def fmt(r):
    if r <= 0:
        return '-'
    if r >= 1:
        return f'{r:.2f} per chunk'
    return f'1 in {1 / r:,.0f}'


def main():
    world = gw.load_world()
    for fid, data in JAVA_TREES.items():
        if fid not in world['placed']:
            world['placed'][fid] = ('Java (modelled)', data)
    walker = Walker(world)
    wild = g.load_wild_crops()
    traits = wild[1]
    wt = (world['tags'], world['biome_files'], world['placed'], world['configured'], world['modifiers'],
          world['bdata'], world['cache'])
    fb, _ = g.feature_map(wt, lambda f: bool(walker.feature(f)), wild)

    per_biome = defaultdict(dict)      # biome -> feature -> {p: trees}
    for f, bm in fb.items():
        for b in bm:
            per_biome[b][f] = walker.feature(f)

    problems = []
    cfg = load_config()
    rules = check_rules(cfg, traits, world, problems)
    eff = fold(rules)
    extra = defaultdict(list)          # biome -> [(rule id, feature, {p: trees})]
    for r, bs in rules:
        t = r.get('trees')
        if not t:
            continue
        dist = walker.walk(t['feature'], frozenset())
        if not dist:
            dist = {0.0: 1.0}
        rate = tier_rate(parse_rate(t['rate']))
        for b in bs:
            extra[b].append((r['id'], t['feature'], {p: n * rate for p, n in dist.items()}))

    area = world['area']
    rows = []
    for b, (dim, layer, pct) in sorted(area.items()):
        if dim != 'minecraft:overworld':
            continue
        fs = per_biome.get(b, {})
        trees = sum(sum(d.values()) for d in fs.values())
        before = sum(nests(d) for d in fs.values())
        e = eff.get(b)
        after = sum(nests(d, e[:2] if e else None) for d in fs.values())
        for _, _, d in extra.get(b, []):
            after += nests(d, e[:2] if e else None)
            trees += sum(d.values())
        src = sorted(((f, nests(d)) for f, d in fs.items() if nests(d) > 0), key=lambda x: -x[1])
        rows.append(dict(biome=b, layer=layer, pct=pct, traits=' '.join(sorted(traits.get(b, []))), trees=trees,
                         before=before, after=after, rules=(e[2] if e else []) + [x[0] for x in extra.get(b, [])
                                                                                    if not e or x[0] not in e[2]],
                         eff=e, sources=src))

    dump = json.loads(DUMP_PATH.read_text()) if DUMP_PATH.exists() else None
    base = json.loads(BASELINE_PATH.read_text()) if BASELINE_PATH.exists() else None
    if base and base.get('rules'):
        problems.append(f'{BASELINE_PATH.name} was taken with rules {base["rules"]}: not used for calibration')
        base = None
    calibrate(rows, base, per_biome, eff, extra)
    write_md(rows, rules, problems, dump, world, walker, base)
    surf = [r for r in rows if r['layer'] == 'surface' and 'ocean' not in r['traits']]
    tb = sum(r['pct'] * r['before'] for r in surf) / 100
    ta = sum(r['pct'] * r['after'] for r in surf) / 100
    print(f'overworld surface: before {fmt(tb)} chunks, after {fmt(ta)} chunks; biomes with nests '
          f"{sum(1 for r in surf if r['before'] > 0)} -> {sum(1 for r in surf if r['after'] > 0)}; "
          f'{len(rules)} rules -> {OUT_MD.relative_to(INSTANCE)}')
    for p in problems:
        print('PROBLEM', p)


def calibrate(rows, base, per_biome, eff, extra):
    """Measured trees/chunk and nest rate where the baseline has enough chunks (see the module docstring).

    Measured T trees and N nests per chunk. The offline nest-carrying trees give the mix of nest chances and their
    mean p; B = N / mean p of the measured trees carried those chances, the other T - B had none (Terralith's own
    trees, bushes the walker can't tell apart...). After = the B trees with the rule applied + (T - B) x chance.
    """
    for r in rows:
        r['measured'] = False
        m = (base or {}).get('biomes', {}).get(r['biome'])
        if not m or m.get('chunks', 0) < MIN_CHUNKS:
            continue
        c, t, n = m['chunks'], m.get('trees', 0), m.get('nests', 0)
        T, N = t / c, n / c
        mix = defaultdict(float)
        for d in per_biome.get(r['biome'], {}).values():
            for p, k in d.items():
                if p > 0:
                    mix[p] += k
        e = eff.get(r['biome'])
        chance, scale = (e[0], e[1]) if e else (0.0, 1.0)
        tot = sum(mix.values())
        mean_p = sum(p * k for p, k in mix.items()) / tot if tot else 0.0
        B = min(T, N / mean_p) if mean_p else 0.0
        after = sum(min(1.0, max(p * scale, chance)) * k / tot * B for p, k in mix.items()) if tot else 0.0
        after += (T - B) * chance
        if N and not B:
            after += N
        ex = [d for _, _, d in extra.get(r['biome'], [])]
        after += sum(nests(d, (chance, scale)) for d in ex)
        r.update(measured=True, chunks=c, nests_seen=n, before=N, after=after,
                 trees=T + sum(sum(d.values()) for d in ex))


def write_md(rows, rules, problems, dump, world, walker, base=None):
    L = []
    w = L.append
    w('# Bee nests by biome')
    w('')
    w('GENERATED by `python3 tools/gen_bee_nests.py`: do not edit. Reference and tests: [bee_nests.md](bee_nests.md).')
    w('Config: [bee_nests.json](../kubejs/generation_scripts/worldgen/bee_nests.json).')
    w('')
    surf = [r for r in rows if r['layer'] == 'surface' and 'ocean' not in r['traits']]
    tb = sum(r['pct'] * r['before'] for r in surf) / 100
    ta = sum(r['pct'] * r['after'] for r in surf) / 100
    w('## 0. Summary')
    w('')
    w('| | before | after |')
    w('|---|---|---|')
    w(f'| overworld land, area-weighted (chunks per nest) | {fmt(tb)} | {fmt(ta)} |')
    w(f"| land biomes with any nests | {sum(1 for r in surf if r['before'] > 0)} of {len(surf)} | "
      f"{sum(1 for r in surf if r['after'] > 0)} of {len(surf)} |")
    w('')
    w('"before" = mods as loaded plus the pack\'s wild_crops.json changes; "after" = bee_nests.json applied.')
    if base:
        nb = sum(1 for r in surf if r['measured'])
        w(f'Rows marked **m** ({nb} biomes) are calibrated on the measured baseline ({BASELINE_PATH.name}, '
          f"{sum(v.get('chunks', 0) for v in base['biomes'].values()):,} chunks, {base.get('generated', '?')[:10]}): "
          'real trees/chunk and nest rate, "after" scaled from it.')
    w('Other rows are offline estimates: tree counts are placement attempts (filters not simulated) and can be far')
    w('too high, so a `chance` rule\'s "after" there is an upper bound. Section 2 has the live numbers from `/dumpbeenests`.')
    w('')
    w('## 1. Per biome (offline estimate)')
    w('')
    w('Overworld land biomes, most nests first. "1 in N" = one nest per N chunks of that biome. Area = share of the')
    w('overworld surface (biome_config.json). Rules: chance/scale rules in fold order, then extra-tree rules.')
    w('')
    w('| biome | area % | m | trees/chunk | before | after | rules | nest sources (before) |')
    w('|---|---|---|---|---|---|---|---|')
    for r in sorted(surf, key=lambda r: (-r['after'], -r['before'], r['biome'])):
        src = ', '.join(f'{f} ({fmt(n)})' for f, n in r['sources'][:3])
        mk = f"m ({r['chunks']} ch, {r['nests_seen']} nests)" if r['measured'] else ''
        w(f"| {r['biome']} | {r['pct']:.2f} | {mk} | {r['trees']:.2f} | {fmt(r['before'])} | {fmt(r['after'])} | "
          f"{', '.join(r['rules'])} | {src} |")
    under = [r for r in rows if r['layer'] != 'surface' and (r['before'] or r['after'])]
    if under:
        w('')
        w('Underground biomes with nests (trees under the surface, not in the table above):')
        w('')
        for r in under:
            w(f"- {r['biome']}: before {fmt(r['before'])}, after {fmt(r['after'])}")
    w('')
    w('## 2. Measured in game (/dumpbeenests)')
    w('')
    if not dump:
        w('No `kubejs/exported/bee_nests.json` yet. In game: `/dumpbeenests reset`, generate fresh terrain (Chunky in an')
        w('unexplored area), then `/dumpbeenests`, and rerun this generator.')
    else:
        w(f"Dump {dump.get('generated', '?')}, counting since {dump.get('counting_since', '?')}; "
          f"rules: {', '.join(dump.get('rules', [])) or 'none'}. Biomes with fewer than 50 chunks are noisy.")
        w('')
        w('| biome | chunks | trees | nests | nests: 1 in N chunks | trees/chunk | nests per 100 trees | estimate (after) | chance / scale |')
        w('|---|---|---|---|---|---|---|---|---|')
        est = {r['biome']: r['after'] for r in rows}
        for b, d in sorted(dump.get('biomes', {}).items(), key=lambda kv: -kv[1].get('chunks', 0)):
            c, t, n = d.get('chunks', 0), d.get('trees', 0), d.get('nests', 0)
            if not (c or t or n):
                continue
            cs = f"{d['chance']:g} / {d['scale']:g}" if 'chance' in d else ''
            w(f"| {b} | {c} | {t} | {n} | {fmt(n / c) if c else '-'} | {t / c if c else 0:.2f} | "
              f"{100 * n / t if t else 0:.2f} | {fmt(est.get(b, 0))} | {cs} |")
    w('')
    w('## 3. Rules resolved')
    w('')
    if not rules:
        w('No usable rules.')
    for r, bs in rules:
        what = []
        if r.get('chance') is not None:
            what.append(f"chance {r['chance']}")
        if r.get('scale') is not None:
            what.append(f"scale {r['scale']}")
        if r.get('trees'):
            t = r['trees']
            what.append(f"trees {t['feature']} at {fmt(tier_rate(parse_rate(t['rate'])))} chunks")
        w(f"- **{r['id']}** ({'; '.join(what)}): {len(bs)} biomes: {', '.join(bs)}")
    w('')
    w('## 4. Nest sources')
    w('')
    w('Every placed feature some overworld biome uses that can carry a nest, with its nest chance per tree.')
    w('')
    w('| placed feature | source | trees per placement by nest chance | biomes |')
    w('|---|---|---|---|')
    users = defaultdict(set)
    for r in rows:
        for f, _ in r['sources']:
            users[f].add(r['biome'])
    for f in sorted(users):
        d = walker.feature(f)
        dist = ', '.join(f'{n:.3g} at p={p:g}' for p, n in sorted(d.items()) if n)
        w(f"| {f} | {world['placed'][f][0]} | {dist} | {', '.join(sorted(users[f]))} |")
    w('')
    w('## 5. Problems')
    w('')
    w('\n'.join(f'- {p}' for p in problems) if problems else 'None.')
    OUT_MD.write_text('\n'.join(L) + '\n')


if __name__ == '__main__':
    main()
