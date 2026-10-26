#!/usr/bin/env python3
"""Where every wild flower generates, how often, and in which colors: before and after wild_flowers.json.

    python3 tools/gen_wild_flowers.py

Writes:
- kubejs/generation_scripts/worldgen/flower_features.json: snapshot of the flower features the biome files list
  (effective JSON: jars, then kubejs/data, so the soil-compat overrides are in it). wild_flowers.js reads it at
  world load to scale those features and mix Flower Patch blocks into them. Regenerate after a mod update or
  after tools/gen_soil_compat.py (both change that JSON); the test script fails while it is stale.
- feature_writeup/flowers_by_biome.md: per-biome flower density + colors + Botania palette, per-flower and
  per-Botania-color density, gaps, config problems.

kubejs/generation_scripts/worldgen/wild_flowers.json is evaluated with the same rules as
kubejs/server_scripts/worldgen/wild_flowers.js (biome traits come from biome_traits.json). The CEConfig wildFlowers
switch can't be read offline: "after" assumes it is on, "before" is the mods' defaults.

Density is "flower patches per chunk": placement rate (count / rarity) times the block's share of the patch.
Patches differ in tries and spread, and a patch still fails on the wrong ground (sand, stone, snow), so compare
rows with each other rather than reading them as block counts. Feature walking and the loaders come from
tools/gen_crop_worldgen_table.py.
"""
import json
import math
import sys
from collections import defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_crop_worldgen_table as g  # noqa: E402

INSTANCE = g.INSTANCE
CONFIG_PATH = INSTANCE / 'kubejs/generation_scripts/worldgen/wild_flowers.json'
SNAPSHOT = INSTANCE / 'kubejs/generation_scripts/worldgen/flower_features.json'
OUT_MD = INSTANCE / 'feature_writeup/flowers_by_biome.md'

# Visual color of every ornamental flower (what the player sees, not the dye it crafts into). A block in
# #minecraft:flowers that is missing here is not counted as a flower (wild crops, azalea leaves, propagules).
COLORS = {
    'minecraft:dandelion': 'yellow', 'veggiesdelight:mature_dandelion': 'yellow', 'minecraft:poppy': 'red', 'minecraft:blue_orchid': 'light_blue',
    'minecraft:allium': 'magenta', 'minecraft:azure_bluet': 'white', 'minecraft:red_tulip': 'red',
    'minecraft:orange_tulip': 'orange', 'minecraft:white_tulip': 'white', 'minecraft:pink_tulip': 'pink',
    'minecraft:oxeye_daisy': 'white', 'minecraft:cornflower': 'blue', 'minecraft:lily_of_the_valley': 'white',
    'minecraft:wither_rose': 'black', 'minecraft:sunflower': 'yellow', 'minecraft:lilac': 'magenta',
    'minecraft:rose_bush': 'red', 'minecraft:peony': 'pink',
    'buzzier_bees:pink_clover': 'pink', 'buzzier_bees:white_clover': 'white',
    'upgrade_aquatic:pink_searocket': 'pink', 'upgrade_aquatic:white_searocket': 'white',
    'upgrade_aquatic:flowering_rush': 'pink', 'twigs:azalea_flowers': 'magenta',
}
DYES = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan',
        'purple', 'blue', 'brown', 'green', 'red', 'black']
for _c in DYES:
    COLORS[f'botania:{_c}_mystical_flower'] = _c
    COLORS[f'botania:{_c}_double_flower'] = _c

# Modifiers a built-in pack of another mod switches off (packs/ inside the jar, invisible to the loader here).
# Buttercups are gone pack-wide (More Mooblooms removeButtercupWorldgen, plus its recipes/tags/trade), so the
# buttercup is also left out of COLORS: naming it in wild_flowers.json is reported as a problem.
PACK_OFF_MODIFIERS = {'buzzier_bees:add_feature/buttercup'}

# Java-registered flower features a modifier adds (no json to walk): (rate per chunk, {block: share}).
JAVA_FLOWERS = {
    'buzzier_bees:flower_pink_clover': (1 / 64, {'buzzier_bees:pink_clover': 1.0}),
    'buzzier_bees:flower_white_clover': (1 / 64, {'buzzier_bees:white_clover': 1.0}),
}

LOG = []


# ── config rules (same as wild_flowers.js) ───────────────────────────────────

def parse_rate(r):
    """'1/32' | '3' | 0.5 -> patches per chunk."""
    if isinstance(r, (int, float)):
        return float(r)
    s = str(r).strip()
    if '/' in s:
        a, b = s.split('/', 1)
        return float(a) / float(b)
    return float(s)


def tier_key(rate):
    """Placement a rate becomes: 'c<count>' (rate >= 1) or 'r<rarity>' (one patch in N chunks); None = off."""
    if rate <= 0:
        return None
    if rate >= 1:
        return 'c%d' % js_round(rate)
    return 'r%d' % max(1, js_round(1 / rate))


def tier_rate(key):
    return float(key[1:]) if key[0] == 'c' else 1 / float(key[1:])


def js_round(x):
    """Math.round: halves go up (Python's round() goes to even)."""
    return int(math.floor(x + 0.5))


def selector_matches(sel, biome, traits):
    return sel == biome if ':' in sel else g.clause_matches(traits, sel)


def rate_for(biome, traits, entry, density):
    r = parse_rate(entry.get('rate', 0))
    for sel, val in entry.get('rates', []):
        if selector_matches(sel, biome, traits):
            r = parse_rate(val)
            break
    return r * density


def resolve_entry(all_traits, entry):
    if entry.get('follows'):
        return list(entry.get('_follows', []))
    return g.resolve_crop(all_traits, entry)


def resolve_follows(world, cfg, all_traits, snap):
    """A flowers entry with 'follows': <block> goes in every biome where that block generates once the pack's other
    flower rules apply (its Flower Patch block counts too). Sets entry['_follows'] and returns {key: [biomes]}, which
    the snapshot carries to wild_flowers.js."""
    keys = [k for k, e in cfg.get('flowers', {}).items() if e.get('follows')]
    for k in keys:
        cfg['flowers'][k]['_follows'] = []
    if not keys:
        return {}
    pre, _ = inventory(world, cfg, all_traits, snap, after=True)
    patch_blocks = cfg.get('flower_patch', {}).get('blocks', {})
    out = {}
    for k in keys:
        block = cfg['flowers'][k]['follows']
        wanted = {block, patch_blocks.get(block)} - {None}
        out[k] = sorted(b for b, dist in pre.items() if any(dist.get(x, 0) > 0 for x in wanted)
                        and b in all_traits and not all_traits[b] & g.NEVER)
        cfg['flowers'][k]['_follows'] = out[k]
    return out


def normalize_palette(p):
    """'red white*2' -> ['red', 'white', 'white'] (sorted, so equal palettes share a feature)."""
    out = []
    for tok in str(p or '').split():
        name, _, n = tok.partition('*')
        out += [name] * (int(n) if n else 1)
    return sorted(out)


def botania_plan(cfg, all_traits):
    """[(palette list, tier key, [biomes])] sorted; same order as the script's feature numbering."""
    bot = cfg.get('botania') or {}
    pals = bot.get('palettes', {})
    density = float(cfg.get('density', 1)) * float(bot.get('density', 1))
    groups = defaultdict(list)
    for b in sorted(all_traits):
        tr = all_traits[b]
        if tr & g.NEVER:
            continue
        pal = normalize_palette(pals[b] if b in pals else pals.get('_default', ''))
        if not pal:
            continue
        key = tier_key(rate_for(b, tr, bot, density))
        if key:
            groups[(' '.join(pal), key)].append(b)
    # sorted by 'palette|tier', the same string order wild_flowers.js numbers its features in
    return [(k[0].split(), k[1], groups[k]) for k in sorted(groups, key=lambda k: k[0] + '|' + k[1])]


def entry_plan(cfg, all_traits, key):
    """{tier key: [biomes]} for one flowers entry."""
    entry = cfg['flowers'][key]
    density = float(cfg.get('density', 1)) * float(entry.get('density', 1))
    tiers = defaultdict(list)
    for b in resolve_entry(all_traits, entry):
        if b not in all_traits:
            continue
        t = tier_key(rate_for(b, all_traits[b], entry, density))
        if t:
            tiers[t].append(b)
    return dict(sorted(tiers.items()))


def removal_biomes(spec, all_traits):
    sels = spec.get('remove', [])
    return sorted(b for b, tr in all_traits.items() if any(selector_matches(s, b, tr) for s in sels))


# ── Flower Patch conversion (same as wild_flowers.js) ────────────────────────

def gcd_all(ns):
    out = 0
    for n in ns:
        out = math.gcd(out, n)
    return out or 1


def patch_weights(share, sizes):
    """[(size or None for the plain flower, weight)], weights reduced; share 0 -> [(None, 1)]."""
    if share <= 0:
        return [(None, 1)]
    keys = sorted(sizes, key=int)
    total = sum(float(sizes[k]) for k in keys)
    out = []
    if share < 1:
        out.append((None, js_round(100 * (1 - share))))
    for k in keys:
        out.append((k, js_round(100 * min(share, 1) * float(sizes[k]) / total)))
    out = [(k, w) for k, w in out if w > 0]
    d = gcd_all([w for _, w in out])
    return [(k, w // d) for k, w in out]


def patch_state(fp, state, size):
    if size is None:
        return state
    tgt = fp['blocks'][state['Name']]
    if isinstance(tgt, dict):
        return {'Name': tgt['block'], 'Properties': {tgt.get('property', 'flowers'): str(size)}}
    return {'Name': tgt, 'Properties': {'flowers': str(size)}}


def convertible(fp, state):
    return isinstance(state, dict) and state.get('Name') in fp['blocks'] and not state.get('Properties')


def convert_provider(p, fp, share):
    """Provider dict -> (new provider, converted?). Mirrors convertProvider in wild_flowers.js."""
    t = p.get('type')
    ws = patch_weights(share, fp['sizes'])
    if t == 'minecraft:simple_state_provider' and convertible(fp, p.get('state')):
        return {'type': 'minecraft:weighted_state_provider', 'entries': [
            {'data': patch_state(fp, p['state'], k), 'weight': w} for k, w in ws]}, True
    if t == 'minecraft:weighted_state_provider':
        if not any(convertible(fp, e['data']) for e in p['entries']):
            return p, False
        entries = []
        for e in p['entries']:
            if convertible(fp, e['data']):
                entries += [{'data': patch_state(fp, e['data'], k), 'weight': e['weight'] * w} for k, w in ws]
            else:
                entries.append({'data': e['data'], 'weight': e['weight'] * sum(w for _, w in ws)})
        return dict(p, entries=entries), True
    if t == 'minecraft:noise_threshold_provider':
        changed = False
        q = dict(p)
        for field in ('low_states', 'high_states'):
            lst = p.get(field) or []
            if any(convertible(fp, s) for s in lst):
                out = []
                for s in lst:
                    if convertible(fp, s):
                        for k, w in ws:
                            out += [patch_state(fp, s, k)] * w
                    else:
                        out += [s] * sum(w for _, w in ws)
                q[field] = out
                changed = True
        return q, changed
    if t == 'minecraft:noise_provider':
        lst = p.get('states') or []
        if not any(convertible(fp, s) for s in lst):
            return p, False
        out = []
        for s in lst:
            if convertible(fp, s):
                for k, w in ws:
                    out += [patch_state(fp, s, k)] * w
            else:
                out += [s] * sum(w for _, w in ws)
        return dict(p, states=out), True
    return p, False


PROVIDERS = ('minecraft:simple_state_provider', 'minecraft:weighted_state_provider',
             'minecraft:noise_threshold_provider', 'minecraft:noise_provider', 'minecraft:dual_noise_provider')


def convert_json(v, fp, share, stats):
    """Deep copy of a configured feature with every convertible provider converted."""
    if isinstance(v, list):
        return [convert_json(x, fp, share, stats) for x in v]
    if not isinstance(v, dict):
        return v
    if v.get('type') in PROVIDERS:
        q, ok = convert_provider(v, fp, share)
        if ok:
            stats['converted'] += 1
        elif v.get('type') == 'minecraft:dual_noise_provider' and any(convertible(fp, s) for s in v.get('states', [])):
            stats['skipped'] += 1
        return q
    return {k: convert_json(x, fp, share, stats) for k, x in v.items()}


# ── feature walking: {block: patches per placement} ──────────────────────────

def provider_dist(p):
    t = p.get('type')
    if t == 'minecraft:simple_state_provider':
        return {p['state']['Name']: 1.0}
    if t == 'minecraft:weighted_state_provider':
        tw = sum(e['weight'] for e in p['entries']) or 1
        out = defaultdict(float)
        for e in p['entries']:
            out[e['data']['Name']] += e['weight'] / tw
        return out
    if t == 'minecraft:noise_threshold_provider':
        lo, hi = p.get('low_states') or [], p.get('high_states') or []
        hc = p.get('high_chance', 0.5)
        out = defaultdict(float)
        # half the area below the threshold (rough), the rest high_chance high_states / default
        for s in lo:
            out[s['Name']] += 0.5 / len(lo)
        for s in hi:
            out[s['Name']] += 0.5 * hc / len(hi)
        out[p['default_state']['Name']] += 0.5 * (1 - hc) if hi else 0.5
        return out
    if t in ('minecraft:noise_provider', 'minecraft:dual_noise_provider'):
        st = p.get('states') or []
        out = defaultdict(float)
        for s in st:
            out[s['Name']] += 1 / len(st)
        return out
    return {}


def merge(dst, src, f=1.0):
    for k, v in src.items():
        dst[k] += v * f


def walk(v, world, seen):
    """Expected flower 'patches' per placement of a configured-feature value."""
    placed, configured = world['placed'], world['configured']
    out = defaultdict(float)
    if isinstance(v, str):
        if v in configured and ('c', v) not in seen:
            merge(out, walk(configured[v][1], world, seen | {('c', v)}))
        return out
    if isinstance(v, list):
        for x in v:
            merge(out, walk(x, world, seen))
        return out
    if not isinstance(v, dict):
        return out
    t = v.get('type')
    if t in PROVIDERS:
        return defaultdict(float, {k: x for k, x in provider_dist(v).items() if k in COLORS})
    cfg = v.get('config') if isinstance(v.get('config'), dict) else None
    if t in ('minecraft:random_patch', 'minecraft:flower', 'minecraft:no_bonemeal_flower') and cfg:
        return walk(cfg.get('feature'), world, seen)
    if t == 'minecraft:simple_random_selector' and cfg:
        fs = cfg.get('features') or []
        for f in fs:
            merge(out, placed_walk(f, world, seen), 1 / len(fs))
        return out
    if t == 'minecraft:random_selector' and cfg:
        left = 1.0
        for e in cfg.get('features') or []:
            merge(out, placed_walk(e.get('feature'), world, seen), left * e.get('chance', 0))
            left *= 1 - e.get('chance', 0)
        merge(out, placed_walk(cfg.get('default'), world, seen), left)
        return out
    if 'placement' in v and 'feature' in v:          # inline placed feature
        return placed_walk(v, world, seen)
    for k, x in v.items():
        merge(out, walk(x, world, seen))
    return out


def placed_walk(ref, world, seen):
    """A placed feature (id or inline): its configured feature times its own count/rarity."""
    placed = world['placed']
    if isinstance(ref, str):
        if ref in placed and ('p', ref) not in seen:
            return placed_walk(placed[ref][1], world, seen | {('p', ref)})
        return defaultdict(float)
    if not isinstance(ref, dict):
        return defaultdict(float)
    rate = g.placement_info(ref.get('placement', []))[0]
    out = walk(ref.get('feature'), world, seen)
    if rate is not None and rate != 1:
        out = defaultdict(float, {k: x * rate for k, x in out.items()})
    return out


def top_rate(placement):
    """Patches per chunk from a top-level placement; noise counts fall back to their mean."""
    rate = 1.0
    for m in placement:
        t = m.get('type', '')
        if t == 'minecraft:count':
            c = g.mean_count(m['count'])
            rate *= c if c is not None else 1
        elif t == 'minecraft:rarity_filter':
            rate /= m['chance']
        elif t == 'minecraft:noise_threshold_count':
            rate *= (m['below_noise'] + m['above_noise']) / 2
        elif t == 'minecraft:noise_based_count':
            rate *= m.get('noise_to_count_ratio', 1) / 2
    return rate


def configured_reach(ref, world, seen=None):
    """Configured feature ids (json) reachable from a placed feature id."""
    seen = seen if seen is not None else set()
    placed, configured = world['placed'], world['configured']
    out = []

    def go(v):
        if isinstance(v, str):
            if v in configured and v not in seen:
                seen.add(v)
                out.append(v)
                go(configured[v][1])
            elif v in placed and ('p' + v) not in seen:
                seen.add('p' + v)
                go(placed[v][1].get('feature'))
        elif isinstance(v, list):
            for x in v:
                go(x)
        elif isinstance(v, dict):
            for x in v.values():
                go(x)
    go(placed[ref][1].get('feature'))
    return out


# ── scaling an existing placed feature (same as wild_flowers.js) ─────────────

def scale_placement(pl, s):
    """Placement list with its rate times s (s > 0)."""
    pl = [dict(m) for m in pl]
    ri = next((i for i, m in enumerate(pl) if m.get('type') == 'minecraft:rarity_filter'), None)
    if ri is not None:
        n = pl[ri]['chance'] / s
        if n >= 1:
            pl[ri]['chance'] = max(1, js_round(n))
            return pl
        del pl[ri]
        s = 1 / n
    if abs(s - 1) < 1e-9:
        return pl
    if s < 1:
        return [{'type': 'minecraft:rarity_filter', 'chance': max(1, js_round(1 / s))}] + pl
    return [{'type': 'minecraft:count', 'count': js_round(s)}] + pl


# ── main ─────────────────────────────────────────────────────────────────────

def load_world():
    tags, biome_files, placed, configured, modifiers = g.load_all()
    bdata = json.loads(g.BIOME_DATA.read_text())
    cfg_b = json.loads(g.BIOME_CONFIG.read_text())
    area = {}
    for dim, dd in cfg_b['dimensions'].items():
        for b, info in dd['biomes'].items():
            area[b] = (dim, info.get('layer', 'surface'), info.get('result_pct', info.get('share_pct')) or 0.0)
    return dict(tags=tags, biome_files=biome_files, placed=placed, configured=configured, modifiers=modifiers,
                bdata=bdata, area=area, cache={})


def flower_features_in_biomes(world, all_traits):
    """placed id -> sorted overworld land biomes whose biome json lists it (flower features only)."""
    out = defaultdict(set)
    for b, (label, data) in world['biome_files'].items():
        if b not in all_traits:
            continue
        for step in data.get('features', []):
            for fid in step if isinstance(step, list) else [step]:
                if isinstance(fid, str) and fid in world['placed']:
                    out[fid].add(b)
    return {f: sorted(bs) for f, bs in out.items() if placed_walk(f, world, set())}


def build_snapshot(world, all_traits, fp):
    feats = flower_features_in_biomes(world, all_traits)
    snap = {'_readme': [
        'GENERATED by tools/gen_wild_flowers.py: do not edit. The flower features that overworld biome files list,',
        'as the game loads them (mod jars, then kubejs/data). kubejs/server_scripts/worldgen/wild_flowers.js scales',
        'these placed features and mixes Flower Patch blocks into these configured features (wild_flowers.json',
        '"existing" + "flower_patch"). Regenerate after a mod update or tools/gen_soil_compat.py.'],
        'placed': {}, 'configured': {}, 'configured_of': {}, 'biomes': {}}
    for f in sorted(feats):
        snap['placed'][f] = world['placed'][f][1]
        snap['biomes'][f] = feats[f]
        reach = []
        for c in configured_reach(f, world):
            stats = {'converted': 0, 'skipped': 0}
            convert_json(world['configured'][c][1], fp, 0.5, stats)
            if stats['converted'] or stats['skipped']:
                reach.append(c)
                snap['configured'][c] = world['configured'][c][1]
        snap['configured_of'][f] = reach
    return snap


def existing_shares(cfg, snap):
    """configured id -> Flower Patch share (max over the placed features that reach it)."""
    fp = cfg['flower_patch']
    dflt = float(fp.get('existing_share', 0))
    out = {}
    for f, cs in snap['configured_of'].items():
        share = float((cfg.get('existing', {}).get(f) or {}).get('flower_patch', dflt))
        for c in cs:
            out[c] = max(out.get(c, 0), share)
    return out


def pack_features(cfg, all_traits):
    """[(feature id, tier key, biomes, {block: share per patch}, source label)] the script adds."""
    out = []
    bot = cfg.get('botania') or {}
    for i, (pal, key, biomes) in enumerate(botania_plan(cfg, all_traits)):
        dist = defaultdict(float)
        for c in pal:
            dist[f'botania:{c}_mystical_flower'] += 1 / len(pal)
        out.append((f'creativeengineering:wild_flowers/botania/{i}', key, biomes, dist, 'botania ' + ' '.join(pal)))
    for key, entry in cfg.get('flowers', {}).items():
        blocks = entry.get('blocks', {})
        tw = sum(blocks.values()) or 1
        dist = {b: w / tw for b, w in blocks.items()}
        for t, biomes in entry_plan(cfg, all_traits, key).items():
            out.append((f'creativeengineering:wild_flowers/{key}/{t}', t, biomes, dist, key))
    return out


def inventory(world, cfg, all_traits, snap, after):
    """biome -> {block: patches per chunk}, and biome -> [(feature, rate, source)]."""
    inv = defaultdict(lambda: defaultdict(float))
    srcs = defaultdict(list)
    dist_cache = {}

    def dist_of(f):
        if f not in dist_cache:
            dist_cache[f] = walk(world['placed'][f][1].get('feature'), world, {('p', f)})
        return dist_cache[f]

    existing = cfg.get('existing', {}) if after else {}
    for f, biomes in snap['biomes'].items():
        spec = existing.get(f) or {}
        scale = float(spec.get('scale', 1)) if after else 1.0
        if scale <= 0:
            continue
        removed = set(removal_biomes(spec, all_traits)) if after else set()
        rate = top_rate(snap['placed'][f].get('placement', [])) * scale
        for b in biomes:
            if b in removed:
                continue
            merge(inv[b], dist_of(f), rate)
            srcs[b].append((f, rate, 'biome json'))
    disabled = set(PACK_OFF_MODIFIERS)
    if after:
        disabled |= set((cfg.get('botania') or {}).get('disable_modifiers', []))
        for e in cfg.get('flowers', {}).values():
            disabled |= set(e.get('disable_modifiers', []))
    for mid, (label, data) in sorted(world['modifiers'].items()):
        if mid in disabled or data.get('type') != 'forge:add_features':
            continue
        feats = data.get('features')
        feats = [feats] if isinstance(feats, str) else (feats or [])
        for f in feats:
            if f in JAVA_FLOWERS:
                rate, dist = JAVA_FLOWERS[f]
            elif f in world['placed']:
                dist = placed_walk(f, world, set())
                rate = 1.0
                if not dist:
                    continue
            else:
                continue
            for b in g.holder_set(data.get('biomes'), world['tags'], world['cache'], sorted(world['bdata'])):
                if b in all_traits and not all_traits[b] & g.NEVER:
                    merge(inv[b], dist, rate)
                    srcs[b].append((f, rate, 'modifier ' + mid))
    if after:
        for fid, key, biomes, dist, label in pack_features(cfg, all_traits):
            rate = tier_rate(key)
            for b in biomes:
                merge(inv[b], dist, rate)
                srcs[b].append((fid, rate, 'wild_flowers.json ' + label))
    return inv, srcs


def check_config(cfg, world, all_traits, snap):
    problems = []
    vocab = set().union(*all_traits.values())
    fp = cfg.get('flower_patch', {})
    known_blocks = set(COLORS) | set(fp.get('blocks', {}))

    def check_sel(where, sel):
        if ':' in sel:
            if sel not in all_traits:
                problems.append(f'{where}: {sel} is not a biome in biome_traits.json')
        else:
            for t in sel.split():
                if t.lstrip('!') not in vocab:
                    problems.append(f'{where}: trait "{t.lstrip("!")}" is on no biome (typo?)')

    bot = cfg.get('botania') or {}
    for b, p in bot.get('palettes', {}).items():
        if b != '_default' and b not in all_traits:
            problems.append(f'botania.palettes: {b} is not a biome in biome_traits.json')
        for c in normalize_palette(p):
            if c not in DYES:
                problems.append(f'botania.palettes {b}: "{c}" is not a dye color')
    for sel, _ in bot.get('rates', []):
        check_sel('botania.rates', sel)
    for key, e in cfg.get('flowers', {}).items():
        for clause in e.get('where', []):
            check_sel(key, clause)
        for b in e.get('add', []) + e.get('except', []):
            if b not in all_traits:
                problems.append(f'{key}: add/except names {b}, which is not in "biomes"')
        for sel, _ in e.get('rates', []):
            check_sel(key + '.rates', sel)
        for b in e.get('blocks', {}):
            if b not in known_blocks:
                problems.append(f'{key}: block {b} is not a known flower (add its color to COLORS in the generator)')
        if not e.get('blocks'):
            problems.append(f'{key}: no blocks')
        for m in e.get('disable_modifiers', []):
            if m not in world['modifiers']:
                problems.append(f'{key}: disable_modifiers names {m}, which no jar ships')
        if not entry_plan(cfg, all_traits, key):
            problems.append(f'{key}: the rule matches no biome')
    for m in bot.get('disable_modifiers', []):
        if m not in world['modifiers']:
            problems.append(f'botania: disable_modifiers names {m}, which no jar ships')
    for f, spec in cfg.get('existing', {}).items():
        if f.startswith('_'):
            continue
        if f not in snap['placed']:
            problems.append(f'existing: {f} is not a flower feature any overworld biome file lists')
        for sel in spec.get('remove', []):
            check_sel(f'existing {f}.remove', sel)
    for b in fp.get('blocks', {}):
        if b not in COLORS:
            problems.append(f'flower_patch.blocks: {b} has no color in the generator COLORS table')
    return problems


def short(b):
    return b.replace('minecraft:', '')


def fmt(r):
    if r <= 0:
        return '-'
    if r >= 1:
        return f'{r:.2f}'.rstrip('0').rstrip('.')
    return f'1/{1 / r:.0f}' if 1 / r >= 10 else f'1/{1 / r:.1f}'


def color_totals(inv_b):
    out = defaultdict(float)
    for blk, r in inv_b.items():
        out[COLORS[blk]] += r
    return out


def write_md(cfg, all_traits, world, before, after, src_after, problems, gaps, snap, shares, conv_stats):
    area = world['area']
    land = [b for b in sorted(all_traits) if not all_traits[b] & g.NEVER]
    L = []
    w = L.append
    w('# Wild flowers by biome (generated)')
    w('')
    w('GENERATED by `python3 tools/gen_wild_flowers.py` from the mod jars, kubejs/data and')
    w('[wild_flowers.json](../kubejs/generation_scripts/worldgen/wild_flowers.json). Do not edit; see')
    w('[flowers.md](flowers.md) for how the system works and its tests.')
    w('')
    w('Rates are flower patches per chunk (`1/32` = one patch per 32 chunks), counting each flower species by its')
    w('share of the patch. "Per 1000" multiplies by the share of the overworld the biome covers')
    w('(biome_config.json). A patch still fails on sand, stone or snow, so treat these as relative numbers.')
    w('')
    if problems:
        w('## Config problems')
        w('')
        for p in problems:
            w(f'- {p}')
        w('')

    def per1000(inv):
        out = defaultdict(float)
        for b in land:
            pct = area.get(b, ('', '', 0))[2]
            for blk, r in inv.get(b, {}).items():
                out[blk] += r * pct / 100 * 1000
        return out

    pb, pa = per1000(before), per1000(after)
    w('## 1. Botania colors')
    w('')
    w('Every color must stay findable: petals of all 16 are crafting inputs.')
    w('')
    w('| Color | Before per 1000 chunks | After | Biomes after | Main biomes (by area) |')
    w('|---|---|---|---|---|')
    for c in DYES:
        blk = f'botania:{c}_mystical_flower'
        bs = [b for b in land if after.get(b, {}).get(blk)]
        top = sorted(bs, key=lambda b: -after[b][blk] * area.get(b, ('', '', 0))[2])[:5]
        w(f'| {c} | {pb[blk]:.1f} | {pa[blk]:.1f} | {len(bs)} | {", ".join(short(b) for b in top)} |')
    w('')
    w('## 2. Other flowers')
    w('')
    w('| Flower | Color | Before per 1000 chunks | After | Biomes after |')
    w('|---|---|---|---|---|')
    for blk in sorted(k for k in COLORS if not k.startswith('botania:')):
        if not pb[blk] and not pa[blk]:
            continue
        n = sum(1 for b in land if after.get(b, {}).get(blk))
        w(f'| {blk} | {COLORS[blk]} | {pb[blk]:.1f} | {pa[blk]:.1f} | {n} |')
    w('')
    w('## 3. Gaps')
    w('')
    w(f'Grassy land biomes with fewer than {GAP} flower patches per chunk after the rules (biome traits from')
    w('biome_traits.json; sandy, rocky and icy biomes are expected to be bare).')
    w('')
    if gaps:
        for b, r in gaps:
            w(f'- {b}: {fmt(r)} ({all_traits[b] and " ".join(sorted(all_traits[b]))})')
    else:
        w('None.')
    w('')
    w('## 4. Per biome')
    w('')
    w('Total = all flower patches per chunk (Botania included). Colors after = share of the flowers, largest first.')
    w('')
    w('| Biome | Area % | Total before | Total after | Botania after | Colors after | Botania palette |')
    w('|---|---|---|---|---|---|---|')
    pals = (cfg.get('botania') or {}).get('palettes', {})
    for b in land:
        tb = sum(before.get(b, {}).values())
        ta = sum(after.get(b, {}).values())
        bot = sum(r for k, r in after.get(b, {}).items() if k.startswith('botania:'))
        ct = color_totals(after.get(b, {}))
        tot = sum(ct.values()) or 1
        cols = ', '.join(f'{c} {100 * r / tot:.0f}%' for c, r in sorted(ct.items(), key=lambda x: -x[1]) if r / tot >= 0.03)
        pal = ' '.join(normalize_palette(pals[b] if b in pals else pals.get('_default', '')))
        w(f'| {short(b)} | {area.get(b, ("", "", 0))[2]:.2f} | {fmt(tb)} | {fmt(ta)} | {fmt(bot)} | {cols} | {pal} |')
    w('')
    w('## 5. Existing features (biome files)')
    w('')
    w('Scale and Flower Patch share as wild_flowers.json applies them. "Converted" = state providers that get')
    w('patch blocks; dual-noise providers (meadow) cannot be converted and are counted as skipped.')
    w('')
    w('| Placed feature | Biomes | Rate | Scale | Patch share | Converted / skipped |')
    w('|---|---|---|---|---|---|')
    ex = cfg.get('existing', {})
    fp_existing = cfg['flower_patch'].get('existing_share', 0)
    for f in sorted(snap['placed']):
        spec = ex.get(f) or {}
        cs = snap['configured_of'][f]
        share = max([shares.get(c, 0) for c in cs] or [0])
        share = max([share] + ([float(spec.get('flower_patch', fp_existing))] if f in conv_stats else []))
        cv = sum(conv_stats[c]['converted'] for c in cs + ([f] if f in conv_stats else []))
        sk = sum(conv_stats[c]['skipped'] for c in cs + ([f] if f in conv_stats else []))
        w(f'| {f} | {len(snap["biomes"][f])} | {fmt(top_rate(snap["placed"][f].get("placement", [])))} | '
          f'{spec.get("scale", 1)} | {share:g} | {cv} / {sk} |')
    w('')
    w('## 6. Pack features (what wild_flowers.js adds)')
    w('')
    w('| Feature | Rate | Biomes |')
    w('|---|---|---|')
    for fid, key, biomes, dist, label in pack_features(cfg, all_traits):
        w(f'| {fid.split(":", 1)[1]} ({label}) | {fmt(tier_rate(key))} | {", ".join(short(b) for b in biomes)} |')
    w('')
    OUT_MD.write_text('\n'.join(L))


GAP = 0.25


def main():
    cfg = g.loads_lenient(CONFIG_PATH.read_text())
    wc, all_traits = g.load_wild_crops()
    world = load_world()
    fp = cfg['flower_patch']
    snap = build_snapshot(world, all_traits, fp)
    snap['follows'] = resolve_follows(world, cfg, all_traits, snap)
    text = json.dumps(snap, indent=1, sort_keys=False) + '\n'
    old = SNAPSHOT.read_text() if SNAPSHOT.exists() else ''
    SNAPSHOT.write_text(text)
    print(f'{len(snap["placed"])} flower features, {len(snap["configured"])} convertible configured features -> '
          f'{SNAPSHOT.relative_to(INSTANCE)}' + (' (changed)' if old != text else ' (unchanged)'))
    shares = existing_shares(cfg, snap)
    conv_stats = {}
    for c, data in snap['configured'].items():
        st = {'converted': 0, 'skipped': 0}
        convert_json(data, fp, max(shares.get(c, 0), 0.5), st)
        conv_stats[c] = st
    for f, data in snap['placed'].items():          # inline configured features
        if isinstance(data.get('feature'), dict):
            st = {'converted': 0, 'skipped': 0}
            convert_json(data['feature'], fp, 0.5, st)
            if st['converted'] or st['skipped']:
                conv_stats[f] = st
    before, _ = inventory(world, cfg, all_traits, snap, after=False)
    after, src_after = inventory(world, cfg, all_traits, snap, after=True)
    problems = check_config(cfg, world, all_traits, snap)
    land = [b for b in sorted(all_traits) if not all_traits[b] & g.NEVER]
    gaps = [(b, sum(after.get(b, {}).values())) for b in land
            if 'grassy' in all_traits[b] and sum(after.get(b, {}).values()) < GAP]
    write_md(cfg, all_traits, world, before, after, src_after, problems, gaps, snap, shares, conv_stats)
    print(f'report -> {OUT_MD.relative_to(INSTANCE)}')
    for p in problems:
        print('  PROBLEM: ' + p)
    for c in DYES:
        if not any(after.get(b, {}).get(f'botania:{c}_mystical_flower') for b in land):
            print(f'  PROBLEM: Botania {c} generates in no biome')
    if gaps:
        print(f'  {len(gaps)} grassy biomes under {GAP} patches/chunk: ' + ', '.join(short(b) for b, _ in gaps))


if __name__ == '__main__':
    main()
