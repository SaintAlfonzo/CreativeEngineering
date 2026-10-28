#!/usr/bin/env python3
"""Structures by biome: which structures can spawn in every biome, grouped by kind, with the expected rate,
plus which professions every village style builds. Writes feature_writeup/structures_by_biome.md.

Everything comes from the files the game uses: structure_groups.json (rates, disabled members), the group/type
tags in structure_tags.json, each structure's biome pool (jar tags + kubejs/data + biome_fits.json, as
gen_structure_groups.py resolves them), the biome areas in biome_config.json, and for professions
pool_config.json + pool_tags.json (so pieces tuned to 0, like the tech houses, don't count).

    python3 tools/gen_structure_biome_table.py

Re-run after changing structure_groups.json, the tags, biome fits, pools or mods.
"""
import json
import math
import re
import sys
import zipfile
from collections import OrderedDict, defaultdict
from datetime import datetime
from fnmatch import fnmatchcase
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_structure_groups as g  # noqa: E402

INSTANCE = g.INSTANCE
WG = g.WG
OUT = INSTANCE / 'feature_writeup/structures_by_biome.md'
POOL_CONFIG = WG / 'pool_config.json'
POOL_TAGS = WG / 'pool_tags.json'
BIOME_CONFIG = WG / 'biome_config.json'

# Kinds, in priority order: a structure is listed under the first kind whose tags hold it.
KINDS = [
    ('Villages', ['type/village']),
    ('Houses', ['type/house']),
    ('Pillager', ['pillager', 'type/enemy_building']),
    ('Sky', ['type/sky']),   # before dungeons: the sky temple floats over every biome and would hide real gaps
    ('Surface dungeons', ['dungeon/surface']),
    ('Ruins', ['type/ruin', 'type/ruined_portal']),
    ('Water', ['type/underwater_ruin', 'type/shipwreck', 'type/ship']),
    ('Underground dungeons', ['dungeon/underground']),
    ('Underground', ['type/underground', 'type/mineshaft']),
    ('Environmental', ['type/environmental', 'type/decoration']),
    ('Nether', ['nether', 'type/nether_constructed', 'type/nether_environmental']),
    ('End', ['end', 'type/end_constructed', 'type/end_environmental']),
    ('Other dungeons', ['dungeons']),   # ocean monument and the like
]
OTHER = 'Other'
# Kinds whose per-biome lists are written out in full (the rest only appear in the matrices).
DETAIL_KINDS = ['Villages', 'Houses', 'Pillager', 'Surface dungeons', 'Ruins']
# Kinds whose holes are listed in the summary for overworld surface biomes.
HOLE_KINDS = ['Villages', 'Houses', 'Pillager', 'Surface dungeons', 'Ruins']
MIN_AREA_PCT = 0.05   # smaller biomes are left out of the hole lists (still in the matrix)

PROFESSIONS = OrderedDict([
    ('armorer', [r'armou?rer']),
    ('butcher', [r'butcher']),
    ('cartographer', [r'cartograph']),
    ('cleric', [r'temple', r'church', r'cleric']),
    ('farmer', [r'(?<![a-z])farm(?!house)', r'farmer', r'barn']),
    ('fisherman', [r'fisher']),
    ('fletcher', [r'fletcher', r'archer']),
    ('leatherworker', [r'tanner', r'leatherworker']),
    ('librarian', [r'librar']),
    ('mason', [r'mason']),
    ('shepherd', [r'shepherd']),
    ('toolsmith', [r'tool_?smith']),
    ('weaponsmith', [r'weapon_?smith']),
])
GENERIC_SMITH = re.compile(r'(?<![a-z])(black)?smith')  # "smith1", "blacksmith": some smith, trade unknown
MISC_PROFESSIONS = re.compile(r'misc_professions')      # T&T mixed-profession building


def fmt_rate(d):
    if d <= 0:
        return '-'
    n = math.sqrt(1 / d)
    return f'1/{n:.0f}' if n >= 10 else f'1/{n:.1f}'


def short(sid):
    return sid.replace('minecraft:', '')


def load_json_lenient(path):
    return g.loads_lenient(path.read_text())


# ── structures ───────────────────────────────────────────────────────────────

def kind_of(pack):
    out = {}
    for sid in pack.all_ids:
        out[sid] = OTHER
        for name, tags in KINDS:
            if any(sid in pack.tag_members.get(t, ()) for t in tags):
                out[sid] = name
                break
    return out


def biome_layers():
    """biome -> (dimension, layer, area_pct); area is the post-tuning share of its dimension layer."""
    cfg = json.loads(BIOME_CONFIG.read_text())
    out = {}
    for dim, dd in cfg['dimensions'].items():
        for b, info in dd['biomes'].items():
            pct = info.get('result_pct', info.get('share_pct', 0)) or 0
            out[b] = (dim, info.get('layer', 'surface'), pct)
    return out


# ── village professions ──────────────────────────────────────────────────────

def installed_namespaces():
    ns = {'minecraft'}
    for jar in sorted(g.MODS_DIR.glob('*.jar')):
        try:
            with zipfile.ZipFile(jar) as z:
                for n in z.namelist():
                    m = re.match(r'data/([^/]+)/', n)
                    if m:
                        ns.add(m.group(1))
        except zipfile.BadZipFile:
            continue
    for d in (INSTANCE / 'kubejs/data').glob('*'):
        ns.add(d.name)
    return ns


def resolve_pool_tags(defs, keys):
    """pool_tags.json syntax: ids, globs, #tags, ! excludes -> {tag: set(keys)}."""
    done = {}

    def resolve(name, stack=()):
        if name in done:
            return done[name]
        if name not in defs or name in stack:
            return set()
        inc, exc = set(), set()
        for raw in defs[name].get('values', []):
            neg = raw.startswith('!')
            v = raw[1:] if neg else raw
            if v.startswith('#'):
                ids = resolve(v[1:], stack + (name,))
            elif '*' in v:
                ids = {k for k in keys if fnmatchcase(k, v)}
            else:
                ids = {v} & keys
            (exc if neg else inc).update(ids)
        done[name] = inc - exc
        return done[name]

    for n in defs:
        resolve(n)
    return done


def village_styles(pack):
    """style (house-pool prefix) -> {'professions': {prof: [pieces]}, 'modded': {ns: n}, 'uncertain': [...],
    'villages': [structure ids]}. Only pieces that really spawn: enabled, current > 0, after pool_tags multipliers."""
    pools = load_json_lenient(POOL_CONFIG).get('pools', {})
    tags = load_json_lenient(POOL_TAGS).get('tags', {}) if POOL_TAGS.exists() else {}
    installed = installed_namespaces()
    keys = {k for p in pools.values() for k in p.get('elements', {})}
    members = resolve_pool_tags(tags, keys)

    def multiplier(pool_id, key):
        m = 1.0
        for t, d in tags.items():
            if key not in members.get(t, ()):
                continue
            pats = d.get('pools')
            if pats and not any(fnmatchcase(pool_id, p) for p in pats):
                continue
            m *= d.get('multiplier', 1)
        return m

    # A style = a village folder with a houses pool; every pool under it counts (T&T keeps most profession
    # buildings in professions_misc pools, the mushroom village in heavy/light "gazebo" pools).
    style_ids = []
    for pool_id in sorted(pools):
        m = re.match(r'^(.+?:village/.+?)/houses(?:/.*)?$', pool_id)
        if m and '/zombie' not in pool_id and m.group(1) not in style_ids:
            style_ids.append(m.group(1))
    styles = OrderedDict()
    for pool_id in sorted(pools):
        if '/zombie' in pool_id:
            continue
        style = next((st for st in sorted(style_ids, key=len, reverse=True) if pool_id.startswith(st + '/')), None)
        if style is None:
            continue
        ns = pool_id.split(':')[0]
        if ns not in installed or pools[pool_id].get('missing'):
            continue
        st = styles.setdefault(style, {'professions': defaultdict(list), 'modded': defaultdict(int),
                                       'uncertain': [], 'villages': [], 'live': 0})
        for key, e in pools[pool_id].get('elements', {}).items():
            if e.get('missing') or not e.get('enabled', True):
                continue
            if (e.get('current', e.get('original', 0)) or 0) * multiplier(pool_id, key) <= 0:
                continue
            piece_ns = key.split(':')[0] if ':' in key else ns
            name = key.split('/')[-1].split(' #')[0].lower()
            if key == 'minecraft:empty' or key.startswith('feature:'):   # placed features: trees, hay piles
                continue
            st['live'] += 1
            if piece_ns not in (ns, 'minecraft'):   # injected by another mod (pet shop, chef house, bank...)
                st['modded'][piece_ns] += 1
                continue
            hit = False
            for prof, pats in PROFESSIONS.items():
                if any(re.search(p, name) for p in pats):
                    st['professions'][prof].append(name)
                    hit = True
            if not hit and GENERIC_SMITH.search(name):
                st['uncertain'].append(f'{name} (a smith, trade not in the name)')
            if MISC_PROFESSIONS.search(name):
                st['uncertain'].append(f'{name} (several professions, not named)')

    # which village structure starts in which style: its start_pool folder
    starts = {}
    for sid in pack.all_ids:
        ns, _, path = sid.partition(':')
        if ns == 'minecraft' and path.startswith('village_'):
            starts[sid] = f'minecraft:village/{path[len("village_"):]}'
    for jar in sorted(g.MODS_DIR.glob('*.jar')):
        try:
            z = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        for n in z.namelist():
            mm = re.match(r'^data/([^/]+)/worldgen/structure/(.+)\.json$', n)
            if not mm:
                continue
            sid = f'{mm.group(1)}:{mm.group(2)}'
            if sid not in pack.all_ids:
                continue
            try:
                sp = g.loads_lenient(z.read(n).decode('utf-8', 'replace')).get('start_pool')
            except (ValueError, AttributeError):
                continue
            if isinstance(sp, str):
                starts[sid] = sp
    styles = OrderedDict((k, v) for k, v in styles.items() if v['live'] > 0)   # e.g. PNC's own pools, tuned to 0
    for sid, sp in starts.items():
        for style in styles:
            if sp == style or sp.startswith(style + '/'):
                styles[style]['villages'].append(sid)
    return styles


# ── the document ─────────────────────────────────────────────────────────────

def main():
    pack = g.Pack()
    doc = g.read_json(g.GROUPS)
    dens = g.new_densities(pack, doc)          # sid -> {biome: slots per chunk^2}
    kinds = kind_of(pack)
    layers = biome_layers()

    per_biome = defaultdict(lambda: defaultdict(dict))   # biome -> kind -> {sid: density}
    for sid, bd in dens.items():
        for b, d in bd.items():
            if d > 0:
                per_biome[b][kinds[sid]][sid] = d

    dims = OrderedDict([('minecraft:overworld', 'Overworld'), ('minecraft:the_nether', 'Nether'), ('minecraft:the_end', 'End')])
    sections = []   # (title, [biomes])
    for dim, label in dims.items():
        for layer in ('surface', 'underground'):
            bs = [b for b, (d, l, _) in layers.items() if d == dim and l == layer]
            if bs:
                bs.sort(key=lambda b: (-layers[b][2], b))
                sections.append((f'{label}, {"caves" if layer == "underground" else "surface"}' if dim == 'minecraft:overworld' else
                                 f'{label}' + (' (inner biomes)' if layer == 'underground' else ''), bs, dim, layer))

    live_kinds = [k for k, _ in KINDS] + [OTHER]
    now = datetime.now().strftime('%Y-%m-%d %H:%M')
    L = []
    w = L.append
    w('# Structures by biome')
    w('')
    w(f'Generated {now} by `python3 tools/gen_structure_biome_table.py`. Do not edit; re-run it after changing '
      '`structure_groups.json`, the structure tags, biome fits, village pools or the mod list. Test prefix: none (a '
      'reference, checked against the game through ST-19 and ST-39 in [structure_tuning.md](structure_tuning.md)).')
    w('')
    w('**How to read it.** Every structure that can spawn in a biome, grouped by kind. A rate like `1/48` means one '
      'structure of that kind per 48 x 48 chunks of that biome, as the game would place them now: the group grids, '
      'weights and frequencies in `structure_groups.json`, disabled members left out, structures another mod blocks '
      'in code left out. A structure is listed under its first kind in this order: '
      + ', '.join(k for k, _ in KINDS) + ' (by the `type/*` and meta tags in `structure_tags.json`). Biome areas '
      'are each biome\'s share of its dimension layer after biome tuning (`biome_config.json`); 0% = removed by '
      'biome tuning. **none** = a hole.')
    w('')

    # ── holes summary ────────────────────────────────────────────────────────
    ow = [b for b, (d, l, a) in layers.items() if d == 'minecraft:overworld' and l == 'surface' and a >= MIN_AREA_PCT]
    ow.sort(key=lambda b: -layers[b][2])
    total_ow = sum(layers[b][2] for b, (d, l, a) in layers.items() if d == 'minecraft:overworld' and l == 'surface')
    w('## 0. Holes at a glance (overworld surface)')
    w('')
    w(f'Biomes with at least {MIN_AREA_PCT}% of the overworld surface and no structure of a kind, biggest first. '
      'Oceans and rivers are expected holes for land kinds.')
    w('')
    w('| Kind | Biomes without it | Their share of the surface | Biomes |')
    w('|---|---|---|---|')
    for k in HOLE_KINDS:
        holes = [b for b in ow if not per_biome[b].get(k)]
        area = sum(layers[b][2] for b in holes)
        names = ', '.join(f'`{short(b)}` {layers[b][2]:.1f}%' for b in holes)
        w(f'| {k} | {len(holes)} of {len(ow)} | {100 * area / max(total_ow, 1e-9):.0f}% | {names or "none"} |')
    w('')
    styles = village_styles(pack)
    gaps = [(s_, [p for p in PROFESSIONS if not st['professions'].get(p)]) for s_, st in styles.items()]
    gaps = [(s_, miss) for s_, miss in gaps if miss]
    if gaps:
        w('Village styles that never build some profession buildings (section ' + str(len(sections) + len(DETAIL_KINDS) + 1)
          + ' has the full matrix; names that hide the trade are listed there):')
        w('')
        for s_, miss in gaps:
            vill = ', '.join(f'`{short(v)}`' for v in sorted(styles[s_]['villages'])) or 'no village uses it'
            w(f'- `{s_}` ({vill}): ' + ('**none at all**' if len(miss) == len(PROFESSIONS) else ', '.join(miss)))
        w('')
    thin = []
    for b in ow:
        vd = sum(per_biome[b].get('Villages', {}).values())
        if 0 < vd and math.sqrt(1 / vd) > 150:
            thin.append((math.sqrt(1 / vd), b))
    if thin:
        w('Biomes where villages exist but are very rare (fewer than 1 per 150 x 150 chunks): '
          + ', '.join(f'`{short(b)}` 1/{n:.0f}' for n, b in sorted(thin, reverse=True)) + '.')
        w('')

    # ── matrices ─────────────────────────────────────────────────────────────
    n = 1
    for title, bs, dim, layer in sections:
        cols = [k for k in live_kinds if any(per_biome[b].get(k) for b in bs)]
        if not cols:
            continue
        w(f'## {n}. {title}: kinds per biome')
        n += 1
        w('')
        w('Cell = number of structures of that kind that can spawn there, and their combined rate.')
        w('')
        w('| Biome | Area | ' + ' | '.join(cols) + ' |')
        w('|---|---|' + '---|' * len(cols))
        for b in bs:
            cells = []
            for k in cols:
                m = per_biome[b].get(k, {})
                cells.append(f'{len(m)} · {fmt_rate(sum(m.values()))}' if m else '**none**')
            w(f'| `{short(b)}` | {layers[b][2]:.2f}% | ' + ' | '.join(cells) + ' |')
        w('')

    # ── per-kind detail ──────────────────────────────────────────────────────
    for k in DETAIL_KINDS:
        w(f'## {n}. {k} by biome')
        n += 1
        w('')
        w('Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.')
        w('')
        w('| Biome | Area | ' + k + ' |')
        w('|---|---|---|')
        rows = [b for b in layers if per_biome[b].get(k) or (layers[b][0] == 'minecraft:overworld' and layers[b][1] == 'surface')]
        rows.sort(key=lambda b: (list(dims).index(layers[b][0]), layers[b][1] != 'surface', -layers[b][2], b))
        for b in rows:
            m = per_biome[b].get(k, {})
            txt = ', '.join(f'`{short(s)}` {fmt_rate(d)}' for s, d in sorted(m.items(), key=lambda kv: -kv[1])) or '**none**'
            w(f'| `{short(b)}` | {layers[b][2]:.2f}% | {txt} |')
        w('')

    # ── professions ──────────────────────────────────────────────────────────
    profs = list(PROFESSIONS)
    w(f'## {n}. Professions per village style')
    n += 1
    w('')
    w('Which profession buildings each village style can build, read from every pool of the style in `pool_config.json` '
      'after your pool edits and `pool_tags.json` multipliers (so the tech houses tuned to 0 are not counted). A '
      'profession is found by the building\'s name (`plains_armorer_house_1`, `tudor_butcher_and_armorer_1`, '
      'Terralith\'s `church`), so a building named only "house" may still hold a workstation. Zombie variants '
      'are left out. **no** = the style never builds it (villagers there only get that job from a workstation '
      'elsewhere). Styles map to villages through each village structure\'s `start_pool`.')
    w('')
    w('| Style | Villages | ' + ' | '.join(profs) + ' | Missing | Modded buildings |')
    w('|---|---|' + '---|' * (len(profs) + 2))
    for style, st in styles.items():
        cells = ['yes' if st['professions'].get(p) else '**no**' for p in profs]
        missing = [p for p in profs if not st['professions'].get(p)]
        vill = ', '.join(f'`{short(v)}`' for v in sorted(st['villages'])) or '-'
        mod = ', '.join(f'{ns} ({c})' for ns, c in sorted(st['modded'].items())) or '-'
        w(f'| `{style}` | {vill} | ' + ' | '.join(cells) + f' | {len(missing)} | {mod} |')
    w('')
    notes = [(s, st['uncertain']) for s, st in styles.items() if st['uncertain']]
    if notes:
        w('Buildings whose profession the name doesn\'t say (not counted above):')
        w('')
        for s, u in notes:
            w(f'- `{s}`: ' + ', '.join(sorted(set(u))))
        w('')

    OUT.write_text('\n'.join(L) + '\n')
    print(f'{sum(len(x[1]) for x in sections)} biomes, {len(dens)} structures with a rate, {len(styles)} village styles -> {OUT}')


if __name__ == '__main__':
    main()
