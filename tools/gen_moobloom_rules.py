#!/usr/bin/env python3
"""Moobloom spawn rules that follow the wild flowers (mooblooms.md 2.8 and 4.9, D47).

    python3 tools/gen_moobloom_rules.py           # write the rule file and the writeup tables
    python3 tools/gen_moobloom_rules.py --check   # print what would change, write nothing (exit 1 if anything)

Inputs:
- the wild flowers after the pack's tuning: tools/gen_wild_flowers.py's inventory(after=True) over
  kubejs/generation_scripts/worldgen/wild_flowers.json, the mod jars and kubejs/data (same numbers as
  flowers_by_biome.md)
- the variant pools: More Mooblooms' moobloom_variants/*.json in the Creative Worldgen member (written by
  tools/gen_moobloom_variants.py from the mooblooms.md 4.4 table). A pool flower shared by several variants is split
  by feed_weight (default 1). Only blocks gen_wild_flowers counts as flowers (its COLORS) are pool flowers.
- cows: kubejs/exported/spawns.json (/dumpspawns), a biome has cows when minecraft:cow is in its creature spawners
- kubejs/generation_scripts/worldgen/moobloom_rules.json: tiers, the drop threshold and the hand-set ("manual")
  rules, which are written as they are and never recomputed.

Writes:
- kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json (copied into the Creative Worldgen jar at
  build, so a change needs a :creativeWorldgen: rebuild and install)
- feature_writeup/mooblooms.md: the parts between the gen_moobloom_rules markers in 2.8 and 4.9
"""
import json
import sys
from collections import defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_wild_flowers as wf  # noqa: E402

g = wf.g
INSTANCE = wf.INSTANCE
CONFIG = INSTANCE / 'kubejs/generation_scripts/worldgen/moobloom_rules.json'
RULES_OUT = INSTANCE / 'kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json'
VARIANTS = INSTANCE / 'development/creativeWorldgen/members/mooblooms/src/main/resources/data/more_mooblooms/moobloom_variants'
SPAWNS = INSTANCE / 'kubejs/exported/spawns.json'
DOC = INSTANCE / 'feature_writeup/mooblooms.md'
NS = 'more_mooblooms:'
WARN = []


def warn(msg):
    WARN.append(msg)
    print('WARNING: ' + msg)


# ── inputs ───────────────────────────────────────────────────────────────────

def load_pools(exclude):
    """flower block -> {variant: feed_weight}, variant -> [flower blocks]."""
    by_flower, by_variant = defaultdict(dict), {}
    for f in sorted(VARIANTS.glob('*.json')):
        vid = f.stem
        if vid in exclude:
            continue
        fl = json.loads(f.read_text())['flowers']
        fl = fl if isinstance(fl, list) else [fl]
        blocks = []
        for e in fl:
            e = {'state': e} if isinstance(e, str) else e
            blk = e['state'].split('[')[0]
            if blk not in wf.COLORS:
                continue        # wild crops (wild onions) and other non-flowers are not counted
            by_flower[blk][vid] = int(e.get('feed_weight', 1))
            blocks.append(blk)
        by_variant[vid] = blocks
    return by_flower, by_variant


def load_cows():
    if not SPAWNS.exists():
        sys.exit(f'missing {SPAWNS.relative_to(INSTANCE)}: run /dumpspawns in game (or /cdevgen run moobloom_rules dumps)')
    d = json.loads(SPAWNS.read_text())['biomes']
    return {b for b, info in d.items()
            if any(s.get('type') == 'minecraft:cow' for s in (info.get('spawners') or {}).get('creature', []))}


def flower_inventory():
    cfg = g.loads_lenient(wf.CONFIG_PATH.read_text())
    _, all_traits = g.load_wild_crops()
    world = wf.load_world()
    snap = wf.build_snapshot(world, all_traits, cfg['flower_patch'])
    wf.resolve_follows(world, cfg, all_traits, snap)
    inv, _ = wf.inventory(world, cfg, all_traits, snap, after=True)
    land = [b for b in sorted(all_traits) if not all_traits[b] & g.NEVER]
    area = {b: world['area'].get(b, ('', '', 0))[2] for b in land}
    return inv, land, area


# ── maths ────────────────────────────────────────────────────────────────────

def variant_mix(inv_b, by_flower):
    """(pool-flower patches per chunk, kinds, {variant: share 0..1})."""
    mix, total, kinds = defaultdict(float), 0.0, 0
    for blk, vs in by_flower.items():
        r = inv_b.get(blk, 0)
        if r <= 0:
            continue
        kinds += 1
        total += r
        tw = sum(vs.values())
        for v, wgt in vs.items():
            mix[v] += r * wgt / tw
    return total, kinds, {v: x / total for v, x in mix.items()} if total else {}


def apportion(shares, total, decimals):
    """Largest remainder: shares (ordered) scaled to total, rounded to decimals, summing exactly to total."""
    unit = 10 ** decimals
    s = sum(x for _, x in shares)
    raw = [(v, x / s * total * unit) for v, x in shares]
    out = {v: int(r) for v, r in raw}
    left = round(total * unit) - sum(out.values())
    for v, r in sorted(raw, key=lambda p: -(p[1] - int(p[1])))[:left]:
        out[v] += 1
    return [(v, num(out[v] / unit)) for v, _ in shares if out[v] > 0]


def num(x):
    x = round(x, 2)
    return int(x) if x == int(x) else x


def manual_entries(m, even_variants):
    if 'replace' in m:
        return [(v, num(p)) for v, p in m['replace'].items()]
    bloom, split = m['bloom'], m['split']
    if split == 'even':
        split = {v: 1 for v in m.get('variants', even_variants)}
    tot = sum(split.values())
    out = [[v, num(bloom * x / tot)] for v, x in split.items()]
    over = round(sum(p for _, p in out) - bloom, 2)
    if over > 0:                    # rounding pushed the rule past its bloom %: take it off the largest share
        top = max(out, key=lambda e: e[1])
        top[1] = num(top[1] - over)
    return [tuple(e) for e in out]


# ── output ───────────────────────────────────────────────────────────────────

def rules_text(rules):
    lines = ['{', '  "rules": [']
    for i, (biomes, entries) in enumerate(rules):
        bl = ', '.join(json.dumps(b) for b in biomes)
        rep = ', '.join('{ "variant": "%s", "percent": %s }' % (NS + v, json.dumps(p)) for v, p in entries)
        lines.append(f'    {{ "biomes": [{bl}],')
        lines.append(f'      "replace": [{rep}] }}' + (',' if i < len(rules) - 1 else ''))
    lines += ['  ]', '}']
    return '\n'.join(lines) + '\n'


def short(b):
    return b.replace('minecraft:', '')


def fmt_rate(r):
    return f'{r:.2f}' if r >= 0.1 else f'{r:.2g}'


def fmt_area(a):
    return '<0.03' if a < 0.025 else f'{a:.2f}'


def tables_28(inv, land, area, cows, by_flower, cfg):
    low = min(t['min'] for t in cfg['tiers'])
    L = []
    w = L.append
    w('**Per flower:**')
    w('')
    w('| Variant | Flower | Per 1000 chunks | Biomes | Most patches per chunk (top 6) |')
    w('|---|---|---|---|---|')
    rows = []
    for blk, vs in by_flower.items():
        bs = [b for b in land if inv.get(b, {}).get(blk, 0) > 0]
        per1000 = sum(inv[b][blk] * area[b] / 100 * 1000 for b in bs)
        top = sorted(bs, key=lambda b: (-inv[b][blk], b))[:6]
        names = ', '.join(v.capitalize() for v, _ in sorted(vs.items(), key=lambda p: (-p[1], p[0])))
        rows.append((names, short(blk).replace('_', ' '), per1000, len(bs),
                     ', '.join(f'`{short(b)}` {fmt_rate(inv[b][blk])}' for b in top)))
    for names, fl, p, n, top in sorted(rows):
        w(f'| {names} | {fl} | {p:.1f} | {n} | {top} |')
    w('')
    w(f'**Per biome**, every biome with {low} or more pool-flower patches per chunk, densest first:')
    w('')
    w('| Biome | Area % | Cows | Pool flowers per chunk | Kinds | Variant mix (% of the pool flowers) |')
    w('|---|---|---|---|---|---|')
    rest = defaultdict(list)
    stats = {b: variant_mix(inv.get(b, {}), by_flower) for b in land}
    for b in sorted(land, key=lambda b: (-round(stats[b][0], 2), b)):
        total, kinds, mix = stats[b]
        if total < low:
            present = tuple(sorted(short(f).replace('_', ' ') for f in by_flower if inv.get(b, {}).get(f, 0) > 0))
            rest[present].append(b)
            continue
        m = ', '.join(f'{v} {round(x * 100)}' for v, x in sorted(mix.items(), key=lambda p: (-p[1], p[0]))
                      if x * 100 >= 3)
        w(f'| `{short(b)}` | {fmt_area(area[b])} | {"yes" if b in cows else "no"} | {total:.2f} | {kinds} | {m} |')
    w('')
    w(f'The rest, under {low} pool-flower patches per chunk, grouped by the pool flowers they have:')
    w('')
    w(f'| Pool flowers (all under {low} per chunk) | Biomes |')
    w('|---|---|')
    for present, bs in sorted(rest.items(), key=lambda p: (-len(p[1]), p[0])):
        bs = sorted(bs, key=lambda b: (':' in short(b), short(b)))
        w(f'| {", ".join(present) or "none"} | {len(bs)}: {", ".join(f"`{short(b)}`" for b in bs)} |')
    return L


def tables_49(computed, cfg):
    L = []
    w = L.append
    for t in cfg['tiers']:
        label = 'Flowery' if t['percent'] >= 10 else 'Some flowers'
        w(f'**{label} ({t["percent"]}%):**')
        w('')
        w('| Biomes | Variants, % of the cows that spawn |')
        w('|---|---|')
        for biomes, entries, tier in computed:
            if tier is t:
                w(f'| {", ".join(f"`{short(b)}`" for b in biomes)} | {", ".join(f"{v} {num(p)}" for v, p in entries)} |')
        w('')
    return L[:-1]


def splice(text, name, lines):
    begin, end = f'<!-- gen_moobloom_rules:{name} begin -->', f'<!-- gen_moobloom_rules:{name} end -->'
    if begin not in text or end not in text:
        warn(f'mooblooms.md has no {name} markers; that table was not written')
        return text
    a = text.index(begin) + len(begin)
    b = text.index(end)
    return text[:a] + '\n' + '\n'.join(lines) + '\n' + text[b:]


def main():
    check = '--check' in sys.argv
    cfg = g.loads_lenient(CONFIG.read_text())
    by_flower, _ = load_pools(set(cfg.get('exclude_variants', [])))
    cows = load_cows()
    inv, land, area = flower_inventory()

    manual = cfg.get('manual', [])
    manual_biomes = {b for m in manual for b in m['biomes']}
    tiers = sorted(cfg['tiers'], key=lambda t: -t['min'])
    groups = defaultdict(list)       # (tier index, entries) -> biomes
    for b in land:
        if b in manual_biomes:
            continue
        total, _, mix = variant_mix(inv.get(b, {}), by_flower)
        if total >= cfg['flower_min']:
            warn(f'{b} has {total:.2f} pool-flower patches per chunk (flower biome) but no manual rule; '
                 f'given the top tier instead')
        tier = next((t for t in tiers if total >= t['min']), None)
        if tier is None:
            continue
        if b not in cows:
            print(f'  no cows, no rule: {b} ({total:.2f})')
            continue
        kept = [(v, x) for v, x in sorted(mix.items(), key=lambda p: (-p[1], p[0]))
                if x * 100 >= cfg['drop_under_pct']]
        entries = tuple(apportion(kept, tier['percent'], tier['decimals']))
        groups[(tiers.index(tier), entries)].append((total, b))
    computed = []
    for (ti, entries), tb in sorted(groups.items(), key=lambda kv: (kv[0][0], -max(t for t, _ in kv[1]),
                                                                    min(b for _, b in kv[1]))):
        computed.append((sorted(b for _, b in tb), list(entries), tiers[ti]))

    for m in manual:
        for b in m['biomes']:
            if b not in cows and not m.get('last'):
                warn(f'manual rule biome {b} has no cows in spawns.json (moobloom_cows.json missing from the dump?)')
    rules = [(m['biomes'], manual_entries(m, cfg['even_variants'])) for m in manual if not m.get('last')]
    rules += [(bs, es) for bs, es, _ in computed]
    rules += [(m['biomes'], manual_entries(m, cfg['even_variants'])) for m in manual if m.get('last')]
    for bs, es in rules:
        s = sum(p for _, p in es)
        if s > 100.001:
            warn(f'rule {bs} adds up to {s}')

    text = rules_text(rules)
    old = RULES_OUT.read_text() if RULES_OUT.exists() else ''
    doc = DOC.read_text()
    new_doc = splice(doc, '2.8', tables_28(inv, land, area, cows, by_flower, cfg))
    new_doc = splice(new_doc, '4.9', tables_49(computed, cfg))
    changed = []
    if text != old:
        changed.append(RULES_OUT)
    if new_doc != doc:
        changed.append(DOC)
    print(f'{len(rules)} rules ({len([m for m in manual])} manual, {len(computed)} from the flowers)')
    for f in changed:
        print(('would change: ' if check else 'changed: ') + str(f.relative_to(INSTANCE)))
    if not changed:
        print('unchanged')
    if check:
        sys.exit(1 if changed else 0)
    if RULES_OUT in changed:
        RULES_OUT.write_text(text)
        print('apply: /reload (kubejs/data); also rebuild and install :creativeWorldgen:, whose jar ships the rule file')
    if DOC in changed:
        DOC.write_text(new_doc)


if __name__ == '__main__':
    main()
