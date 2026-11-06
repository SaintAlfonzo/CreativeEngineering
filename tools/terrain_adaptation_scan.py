#!/usr/bin/env python3
"""
Terrain adaptation scan: which structures would look better with the soft beard and/or template trim
(patcherMod worldgen/TerrainAdaptation + TemplateTrim, feature_writeup/structure_tuning.md 3.15).

For every structure in the pack (worldgen/structure JSON from the vanilla -extra jar, mods/*.jar and kubejs/data,
last one wins like datapacks, plus the vanilla jigsaw structures registered in code) it follows the start pool and
every jigsaw link (up to the structure's size) to the templates it can place, and measures each rigid template the
way TemplateTrim sees it:
  box     template columns (x * z)
  built   columns with a built block (not air, pad, plant, structure block / jigsaw / structure void)
  pad     columns with pad (grass/dirt/sand/gravel...) or plants but nothing built: a flat slab around the building
  air     columns holding only air: a rectangular notch in any slope
  out-air air blocks outside the built columns (what trim stops placing)
Pad and plant blocks are the template_trim/* block tags as blocktags.js builds them, resolved through the live tag
export (kubejs/exported/tags/block_tags.json, /dumptags).

Writes feature_writeup/terrain_adaptation_scan.md (+ kubejs/generation_scripts/worldgen/terrain_adaptation_scan.json).
Template measurements are cached in tools/.cache/terrain_scan.json (keyed by the jar and kubejs/data file lists).

Run:  python3 tools/terrain_adaptation_scan.py [--fresh]
"""
import gzip
import hashlib
import json
import re
import sys
from collections import defaultdict
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import audit_loot_containers as A  # noqa: E402
import gen_loot_config as G  # noqa: E402
import loot_breakdown_scan as S  # noqa: E402
from gen_structure_config import loads_lenient  # noqa: E402

ROOT = G.INSTANCE
WORLDGEN = ROOT / 'kubejs/generation_scripts/worldgen'
OUT_MD = ROOT / 'feature_writeup/terrain_adaptation_scan.md'
OUT_JSON = WORLDGEN / 'terrain_adaptation_scan.json'
CACHE = ROOT / 'tools/.cache/terrain_scan.json'
TAG_JS = ROOT / 'kubejs/data/creativeworldgen/kubejs/worldgen/terrain_adaptation.js'
TAG_EXPORT = ROOT / 'kubejs/exported/tags/block_tags.json'
EARTH_FILL_JS = ROOT / 'kubejs/data/creativeworldgen/kubejs/worldgen/earth_fill.js'
MEASURE_VERSION = 2   # bump when measure() changes, so the cache is rebuilt

# Vanilla jigsaw structures (registered in code in 1.19.2: no structure JSON to read). Values from
# net.minecraft.data.worldgen.Structures: start pool, size, terrain adaptation.
VANILLA_JIGSAW = {
    'minecraft:village_plains': ('minecraft:village/plains/town_centers', 6, 'beard_thin'),
    'minecraft:village_desert': ('minecraft:village/desert/town_centers', 6, 'beard_thin'),
    'minecraft:village_savanna': ('minecraft:village/savanna/town_centers', 6, 'beard_thin'),
    'minecraft:village_snowy': ('minecraft:village/snowy/town_centers', 6, 'beard_thin'),
    'minecraft:village_taiga': ('minecraft:village/taiga/town_centers', 6, 'beard_thin'),
    'minecraft:pillager_outpost': ('minecraft:pillager_outpost/base_plates', 7, 'beard_thin'),
    'minecraft:bastion_remnant': ('minecraft:bastion/starts', 6, 'none'),
    'minecraft:ancient_city': ('minecraft:ancient_city/city_center', 7, 'beard_box'),
}
VANILLA_ENV = {'minecraft:bastion_remnant': 'nether', 'minecraft:ancient_city': 'underground'}
# Vanilla structures built in code (no jigsaw): what they are, for the "not scannable" list.
VANILLA_CODE = {
    'minecraft:igloo': 'template piece, adaptation none, sits on the surface',
    'minecraft:desert_pyramid': 'code-built (blocked by YUNG\'s Better Desert Temples)',
    'minecraft:jungle_pyramid': 'code-built (blocked by YUNG\'s Better Jungle Temples)',
    'minecraft:swamp_hut': 'code-built (blocked: Better Witch Huts)',
    'minecraft:mansion': 'template pieces, adaptation none; its own foundation code fills under it',
    'minecraft:monument': 'code-built, underwater',
    'minecraft:shipwreck': 'template piece, water',
    'minecraft:shipwreck_beached': 'template piece, beach',
    'minecraft:ocean_ruin_cold': 'template pieces, seabed',
    'minecraft:ocean_ruin_warm': 'template pieces, seabed',
    'minecraft:buried_treasure': 'one chest underground',
    'minecraft:mineshaft': 'code-built, underground', 'minecraft:mineshaft_mesa': 'code-built, underground',
    'minecraft:stronghold': 'code-built, underground',
    'minecraft:fortress': 'code-built, Nether', 'minecraft:end_city': 'template pieces, End',
    'minecraft:nether_fossil': 'template, Nether',
    'minecraft:ruined_portal': 'removed in this pack (structure_groups)',
}

AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
MARKER = {'minecraft:structure_block', 'minecraft:jigsaw', 'minecraft:structure_void'}
# Fallbacks when no tag export exists (vanilla members of the same tags).
FALLBACK_TAGS = {
    'minecraft:dirt': ['minecraft:dirt', 'minecraft:grass_block', 'minecraft:podzol', 'minecraft:coarse_dirt', 'minecraft:mycelium',
                       'minecraft:rooted_dirt', 'minecraft:moss_block', 'minecraft:mud', 'minecraft:muddy_mangrove_roots'],
    'forge:sand': ['minecraft:sand', 'minecraft:red_sand'], 'forge:gravel': ['minecraft:gravel'],
    'creativeengineering:ground_match/plants': ['minecraft:grass', 'minecraft:fern', 'minecraft:tall_grass', 'minecraft:large_fern',
                                                'minecraft:dead_bush', 'minecraft:poppy', 'minecraft:dandelion'],
    'minecraft:flowers': [], 'minecraft:saplings': [],
}

# Recommendation thresholds (per structure, summed over its rigid templates).
STRONG_WASTE = 0.30      # >= 30% of the template box isn't built on
STRONG_COLS = 12         # ...and at least this many pad or air-only columns
MILD_WASTE = 0.15
MILD_COLS = 6


# ------------------------------------------------------------------------------------------ tags

def load_tags():
    tags = None
    if TAG_EXPORT.exists():
        tags = json.loads(TAG_EXPORT.read_text())
    def members(t, seen=()):
        src = tags if tags is not None else FALLBACK_TAGS
        out = set()
        for e in src.get(t, []):
            if e.startswith('#'):
                if e[1:] not in seen:
                    out |= members(e[1:], seen + (t,))
            else:
                out.add(e)
        return out
    # = blocktags.js template_trim/pad and template_trim/plants
    pad = members('minecraft:dirt') | members('forge:sand') | members('forge:gravel') | {'minecraft:clay', 'minecraft:snow_block'}
    plants = members('creativeengineering:ground_match/plants') | {'minecraft:snow', 'minecraft:moss_carpet'}
    if tags is None or not members('creativeengineering:ground_match/plants'):
        plants |= set(FALLBACK_TAGS['creativeengineering:ground_match/plants']) | members('minecraft:flowers') | members('minecraft:saplings')
    return pad, plants, tags is not None


# ------------------------------------------------------------------------------------------ data

def load_worldgen():
    """structure jsons, pool jsons, template sources (id -> (label, reader, name)); last source wins."""
    structs, pools, templates, s_origin = {}, {}, {}, {}
    for label, read, names in G.iter_data_sources():
        for n in names:
            m = re.match(r'data/([^/]+)/worldgen/(structure|template_pool)/(.+)\.json$', n)
            if m:
                try:
                    raw = read(n)
                    d = loads_lenient(raw.decode('utf-8') if isinstance(raw, bytes) else raw)
                except Exception:
                    continue
                key = f'{m[1]}:{m[3]}'
                if m[2] == 'structure':
                    structs[key] = d
                    s_origin[key] = label
                else:
                    pools[key] = d
                continue
            m = re.match(r'data/([^/]+)/structures/(.+)\.nbt$', n)
            if m:
                templates[f'{m[1]}:{m[2]}'] = (label, read, n)
    # G.iter_data_sources only lists kubejs/data JSON; the ported structure mods keep their templates there too
    for path in sorted((ROOT / 'kubejs/data').glob('*/structures/**/*.nbt')):
        rel = path.relative_to(ROOT / 'kubejs/data').as_posix().split('/')
        templates[f"{rel[0]}:{'/'.join(rel[2:])[:-4]}"] = ('kubejs/data', lambda n, p=path: p.read_bytes(), str(path))
    return structs, pools, templates, s_origin


def template_signature(pad, plants):
    h = hashlib.sha1(json.dumps([sorted(pad), sorted(plants)]).encode()).hexdigest()
    return [h, MEASURE_VERSION] + S.signature()


def measure(raw, pad, plants):
    d = S.read_template(raw)
    if d is None:
        return None
    palette = d.get('palette') or (d.get('palettes') or [[]])[0]
    names, waterlogged = [], []
    for p in palette:
        names.append(p.get('Name', 'minecraft:air'))
        waterlogged.append((p.get('Properties') or {}).get('waterlogged') == 'true')
    size = [int(x) for x in d.get('size', [0, 0, 0])]
    cols = defaultdict(lambda: [0, 0, 0, 0])   # air, pad/plant, built, marker
    air_blocks, water, jig = 0, 0, set()
    for b in d.get('blocks', []):
        s = b.get('state', 0)
        if s >= len(names):
            continue
        n = names[s]
        x, y, z = (int(v) for v in b['pos'])
        c = cols[(x, z)]
        if n in AIR:
            c[0] += 1
        elif n in MARKER:
            c[3] += 1
            if n == 'minecraft:jigsaw' and b.get('nbt', {}).get('pool'):
                jig.add(G.norm_id(b['nbt']['pool']))
        elif n in pad or n in plants:
            c[1] += 1
        else:
            c[2] += 1
        if n == 'minecraft:water' or waterlogged[s]:
            water += 1
    built = sum(1 for c in cols.values() if c[2])
    pad_cols = sum(1 for c in cols.values() if not c[2] and c[1])
    air_cols = sum(1 for c in cols.values() if not c[2] and not c[1] and c[0])
    out_air = sum(c[0] for c in cols.values() if not c[2]) if built else 0
    pad_blocks = sum(c[1] for c in cols.values() if not c[2]) if built else 0
    return {'size': size, 'box': size[0] * size[2], 'built': built, 'pad': pad_cols, 'air': air_cols,
            'out_air': out_air, 'pad_blocks': pad_blocks, 'air_total': sum(c[0] for c in cols.values()),
            'solid_blocks': sum(c[1] + c[2] for c in cols.values()), 'water': water, 'jigsaw': sorted(jig)}


class TemplateCache:
    """Template measurements, loaded once, measured on demand, saved once."""

    def __init__(self, templates, pad, plants, fresh):
        self.templates, self.pad, self.plants = templates, pad, plants
        self.sig = template_signature(pad, plants)
        self.data, self.changed = {}, False
        if not fresh and CACHE.exists():
            c = json.loads(CACHE.read_text())
            if c.get('sig') == self.sig:
                self.data = c['templates']

    def get(self, tid):
        if tid not in self.data:
            if tid not in self.templates:
                return None
            label, read, name = self.templates[tid]
            m = measure(read(name), self.pad, self.plants)
            self.data[tid] = dict(m, origin=label) if m else None
            self.changed = True
        return self.data[tid]

    def save(self):
        if self.changed:
            CACHE.parent.mkdir(parents=True, exist_ok=True)
            CACHE.write_text(json.dumps({'sig': self.sig, 'templates': self.data}))


# ------------------------------------------------------------------------------------------ structures

def pool_elements(pid, pools, live):
    """[(template id, projection)], plus fallback pool. Vanilla pools come from pool_config.json (no JSON)."""
    out, p = [], pools.get(pid)
    if p:
        def walk(el, proj):
            proj = el.get('projection', proj)
            if el.get('location'):
                loc = el['location'] if isinstance(el['location'], str) else el['location'].get('location', '')
                if loc:
                    out.append((G.norm_id(loc), proj))
            for x in el.get('elements', []):
                walk(x, proj)
        for e in p.get('elements', []):
            walk(e.get('element', {}), 'rigid')
        return out, G.norm_id(p['fallback']) if p.get('fallback') else None
    lp = live.get(pid)
    if lp:
        matching = any(w in pid for w in ('/streets', '/terminators', 'street'))
        for k in lp['elements']:
            out.append((G.norm_id(k), 'terrain_matching' if matching else 'rigid'))
        return out, G.norm_id(lp['fallback']) if lp.get('fallback') else None
    return out, None


def reach(start, size, pools, live, tmeta_lookup):
    """Template ids (with projection) a jigsaw structure can place: start pool, then jigsaw links, depth < size."""
    seen_pools, found = set(), {}
    frontier = [(start, 0)]
    while frontier:
        pid, depth = frontier.pop()
        if pid in seen_pools or pid == 'minecraft:empty':
            continue
        seen_pools.add(pid)
        els, fb = pool_elements(pid, pools, live)
        if fb and fb != 'minecraft:empty':
            frontier.append((fb, depth))
        for tid, proj in els:
            if tid not in found or proj == 'rigid':
                found[tid] = proj
            if depth + 1 < max(size, 1):
                for nxt in (tmeta_lookup(tid) or {}).get('jigsaw', []):
                    frontier.append((nxt, depth + 1))
    return found


def floating():
    """Structures earth fill exempts because they float or stand in water on purpose (ships, rafts, sky)."""
    if not EARTH_FILL_JS.exists():
        return set()
    return set(re.findall(r"'([a-z0-9_.-]+:[a-z0-9_/.-]+)'", re.sub(r'//[^\n]*', '', EARTH_FILL_JS.read_text())))


def environment(sid, d, biomes, floats=frozenset()):
    """surface | afloat | buried | seabed | sky | underground | nether | end."""
    if sid in floats:
        return 'afloat'
    if sid in VANILLA_ENV:
        return VANILLA_ENV[sid]
    b = json.dumps(biomes if biomes is not None else d.get('biomes', '')).lower()
    if re.search(r'nether|basalt_deltas|crimson_forest|warped_forest|soul_sand_valley', b):
        return 'nether'
    if re.search(r'(?<![a-z])end(?=[_/"]|$)|nullscape', b):   # the_end, is_end, end_highlands, has_structure/end_ruins_biomes
        return 'end'
    hm = d.get('project_start_to_heightmap')
    sh = d.get('start_height', {})
    def lowest(v):
        if isinstance(v, dict):
            if 'absolute' in v:
                return v['absolute']
            if 'above_bottom' in v:
                return -999
            if 'below_top' in v:
                return 999
            vals = [lowest(x) for x in v.values() if isinstance(x, (dict, int, float))]
            return min(vals) if vals else 0
        return v if isinstance(v, (int, float)) else 0
    low = lowest(sh)
    if hm:
        if 'OCEAN_FLOOR' in hm:
            return 'seabed'
        return 'surface' if low >= -4 else 'buried'
    if low >= 120:
        return 'sky'
    return 'underground' if low < 50 else 'fixed height'


def current_tags():
    """{structure id: set(tag paths)} from terrain_adaptation.js (vars and literal arrays, nested #tag refs)."""
    if not TAG_JS.exists():
        return {}
    src = re.sub(r'//[^\n]*', '', TAG_JS.read_text())
    vars_ = {m[1]: re.findall(r"'([^']+)'", m[2]) for m in re.finditer(r'var\s+(\w+)\s*=\s*\[(.*?)\]', src, re.S)}
    adds = defaultdict(list)
    for m in re.finditer(r"global\.tags\.add\(e,\s*'([^']+)',\s*(\w+|\[.*?\])\s*\)", src, re.S):
        adds[m[1]] += vars_.get(m[2], []) if not m[2].startswith('[') else re.findall(r"'([^']+)'", m[2])
    def expand(tag, seen=()):
        out = set()
        for e in adds.get(tag, []):
            out |= expand(e[1:], seen + (tag,)) if e.startswith('#') and e[1:] not in seen else ({e} if not e.startswith('#') else set())
        return out
    res = defaultdict(set)
    for tag in adds:
        for sid in expand(tag):
            res[sid].add(tag.split(':', 1)[1])
    return res


def spawn_state():
    """{structure: 'off' | None} from structure_groups.json, plus code blocks from structure_config.json."""
    state = {}
    g = json.loads((WORLDGEN / 'structure_groups.json').read_text())
    for part in ('groups', 'standalone'):
        for v in g.get(part, {}).values():
            for sid, s in v.get('structures', {}).items():
                if s.get('enabled') is False or s.get('frequency', 1) == 0:
                    state[sid] = 'off'
    cfg = json.loads((WORLDGEN / 'structure_config.json').read_text())
    biomes = {}
    for s in cfg.get('structure_sets', {}).values():
        for st in s.get('structures', []):
            biomes[st['structure']] = st.get('biomes')
            if st.get('blocked'):
                state[st['structure']] = 'blocked'
    return state, biomes


def adaptation(d):
    eta = d.get('enhanced_terrain_adaptation')
    if isinstance(eta, dict):
        t = eta.get('type', '').split(':')[-1]
        if t == 'custom':
            return f"yung custom (carves {str(eta.get('carves')).lower()}, beards {str(eta.get('beards')).lower()}, kernel {eta.get('kernel_size')})"
        return f'yung {t}'
    return d.get('terrain_adaptation', 'none')


def main():
    fresh = '--fresh' in sys.argv
    pad, plants, live_tags = load_tags()
    structs, pools, templates, s_origin = load_worldgen()
    live = json.loads((WORLDGEN / 'pool_config.json').read_text())['pools']
    state, cfg_biomes = spawn_state()
    tagged = current_tags()
    floats = floating()

    entries = {}
    for sid, d in structs.items():
        entries[sid] = {'d': d, 'start': G.norm_id(d['start_pool']) if d.get('start_pool') else None,
                        'size': int(d.get('size', 1) or 1), 'type': G.norm_id(d.get('type', '?')), 'origin': s_origin[sid]}
    for sid, (sp, size, ta) in VANILLA_JIGSAW.items():
        if sid not in entries:
            entries[sid] = {'d': {'terrain_adaptation': ta, 'project_start_to_heightmap': 'WORLD_SURFACE_WG'}, 'start': sp, 'size': size,
                            'type': 'minecraft:jigsaw', 'origin': 'vanilla (code)'}

    # first pass: reachable templates (needs the jigsaw links, so templates are measured as they are reached)
    cache = TemplateCache(templates, pad, plants, fresh)
    for sid, e in entries.items():
        e['reach'] = reach(e['start'], e['size'], pools, live, cache.get) if e['start'] else {}
    cache.save()

    rows = []
    for sid, e in sorted(entries.items()):
        d = e['d']
        yung = e['type'] == 'yungsapi:yung_jigsaw' or isinstance(d.get('enhanced_terrain_adaptation'), dict)
        env = environment(sid, d, cfg_biomes.get(sid), floats)
        rigid = [(t, cache.get(t)) for t, proj in e['reach'].items() if proj != 'terrain_matching']
        rigid = [(t, m) for t, m in rigid if m and m['box']]
        missing = [t for t, proj in e['reach'].items() if not cache.get(t)]
        box = sum(m['box'] for _, m in rigid)
        built = sum(m['built'] for _, m in rigid)
        padc = sum(m['pad'] for _, m in rigid)
        airc = sum(m['air'] for _, m in rigid)
        out_air = sum(m['out_air'] for _, m in rigid)
        water = sum(m['water'] for _, m in rigid)
        solid = sum(m.get('solid_blocks', 0) for _, m in rigid)
        waste = (box - built) / box if box else 0.0
        worst = max(rigid, key=lambda r: (r[1]['pad'] + r[1]['air']), default=(None, None))
        matching = sum(1 for p in e['reach'].values() if p == 'terrain_matching')
        row = {'id': sid, 'type': e['type'], 'origin': e['origin'], 'adaptation': adaptation(d), 'yung': yung, 'env': env,
               'state': state.get(sid), 'tags': sorted(tagged.get(sid, [])), 'templates': len(rigid), 'matching': matching,
               'box': box, 'built': built, 'pad': padc, 'air': airc, 'out_air': out_air, 'water': water, 'solid': solid,
               'waste': round(waste, 3), 'worst': worst[0], 'missing': missing[:5], 'jigsaw': bool(e['start'])}
        row['verdict'], row['why'] = verdict(row)
        rows.append(row)

    write(rows, live_tags, pad, plants)
    counts = defaultdict(int)
    for r in rows:
        counts[r['verdict']] += 1
    print(f'{len(rows)} structures: ' + ', '.join(f'{k} {v}' for k, v in sorted(counts.items())))
    print(f'wrote {OUT_MD.relative_to(ROOT)} and {OUT_JSON.relative_to(ROOT)}')


def verdict(r):
    """(verdict key, short reason)."""
    if r['tags']:
        return 'tagged', 'already in ' + ', '.join(r['tags'])
    if not r['jigsaw']:
        return 'code', 'built in code, no templates to read'
    if not r['templates']:
        if r['matching'] and not r['missing']:
            return 'follows', 'every template is terrain_matching: placed column by column on the ground, no beard'
        return 'unread', 'no rigid template found' + (f" (missing {r['missing'][0]})" if r['missing'] else '')
    if r['env'] == 'afloat':
        return 'afloat', 'floats or stands in water on purpose (earth_fill/exempt)'
    if r['env'] != 'surface':
        return 'skip-env', f"{r['env']}: its air and ground are part of the design"
    if r['solid'] and r['water'] >= 0.2 * r['solid']:
        return 'skip-env', f"{r['water']} of its {r['solid']} blocks hold water"
    if r['state'] in ('off', 'blocked'):
        tail = 'disabled in structure_groups' if r['state'] == 'off' else 'blocked in code'
    else:
        tail = None
    cols = r['pad'] + r['air']
    if 'village' in r['id'].split(':', 1)[1] or (r['matching'] >= 3 and r['templates'] >= 5):
        key = 'settlement'
        why = f"settlement ({r['templates']} pieces, {r['matching']} terrain-matching)"
    elif r['waste'] >= STRONG_WASTE and cols >= STRONG_COLS:
        key = 'strong'
        why = f"{round(100 * r['waste'])}% of the box unbuilt, {r['pad']} pad + {r['air']} air-only columns"
    elif r['waste'] >= MILD_WASTE and cols >= MILD_COLS:
        key = 'mild'
        why = f"{round(100 * r['waste'])}% unbuilt, {r['pad']} pad + {r['air']} air-only columns"
    else:
        key = 'compact'
        why = f"{round(100 * r['waste'])}% unbuilt"
    if tail:
        why += f'; {tail}'
    return key, why


def suggestion(r):
    if r['yung']:
        return 'template_trim (mode tags have no effect on YUNG structures)'
    ta = r['adaptation']
    if r['verdict'] == 'settlement':
        return 'review: soft + trim for the houses changes the whole village'
    if ta in ('beard_thin', 'beard_box'):
        return 'soft + template_trim'
    if ta == 'bury':
        return 'template_trim (keep bury)'
    return 'template_trim' + (' (+ soft if the pad hangs on slopes)' if r['pad'] else '')


# ------------------------------------------------------------------------------------------ report

def fmt_row(r, with_suggestion=True):
    mod = r['id'].split(':')[0]
    cells = [f"`{r['id']}`", r['adaptation'], str(r['templates']), str(r['box']), str(r['built']), str(r['pad']), str(r['air']),
             f"{r['out_air']:,}", f"{round(100 * r['waste'])}%"]
    if with_suggestion:
        cells.append(suggestion(r) + (f" ({'off' if r['state'] == 'off' else 'blocked'})" if r['state'] else ''))
    else:
        cells.append(r['why'])
    return '| ' + ' | '.join(cells) + ' |'


HEAD = ('| Structure | Adaptation | Templates | Box cols | Built | Pad cols | Air-only cols | Air outside built | Unbuilt | {} |\n'
        '|---|---|---|---|---|---|---|---|---|---|')


def write(rows, live_tags, pad, plants):
    by = defaultdict(list)
    for r in rows:
        by[r['verdict']].append(r)
    for v in by.values():
        v.sort(key=lambda r: (-(r['pad'] + r['air']), r['id']))
    L = []
    L.append('# Terrain adaptation scan: structures that could use the soft beard or template trim')
    L.append('')
    L.append(f'Generated {date.today().isoformat()} by `python3 tools/terrain_adaptation_scan.py` (re-run after adding or updating '
             'structure mods). What the tags do: [structure_tuning.md 3.15](structure_tuning.md). Data: '
             f'`{OUT_JSON.relative_to(ROOT)}`.')
    L.append('')
    L.append('**How it reads a structure.** It follows the start pool and every jigsaw link (up to the structure\'s `size`) to the '
             'templates the structure can place, and measures each rigid template (terrain-matching street pieces are left out, '
             'they follow the ground anyway) the way `TemplateTrim` does:')
    L.append('- **Box cols**: template width x depth, summed over the templates; **Built**: columns with a built block.')
    L.append('- **Pad cols**: columns with only pad (grass, dirt, sand, gravel...) and plants: a flat slab around the building.')
    L.append('- **Air-only cols**: columns with only saved air: a rectangular notch cut into any slope.')
    L.append('- **Air outside built**: air blocks trim would stop placing. **Unbuilt**: share of the box that isn\'t built on '
             '(what vanilla\'s beard flattens for nothing).')
    L.append(f'- Pad and plants are the `template_trim/*` block tags as `blocktags.js` builds them ({len(pad)} pad blocks, '
             f'{len(plants)} plants), resolved through {"the live tag export" if live_tags else "vanilla fallbacks (no tag export found)"}.')
    L.append(f'- **Strong**: surface structure, at least {round(100 * STRONG_WASTE)}% of the box unbuilt and {STRONG_COLS}+ pad or '
             f'air-only columns. **Mild**: {round(100 * MILD_WASTE)}% and {MILD_COLS}+. Totals over multi-template structures '
             'add every reachable variant once, so a jigsaw structure with many alternatives counts each of them.')
    L.append('')
    L.append('**Choosing tags** (add ids to `kubejs/data/creativeworldgen/kubejs/worldgen/terrain_adaptation.js`, then rejoin; new chunks only):')
    L.append('- `soft` + `template_trim`: a building on a pad. The pad and the air around it go, the ground is levelled only under '
             'the building and blends out in 4 blocks (+ `skirtBump`).')
    L.append('- `soft` alone: keeps the template\'s pad and courtyards (they are levelled too) but rounds and shortens the '
             'skirt. For walled compounds and forts whose yards should stay flat.')
    L.append('- `template_trim` alone: for `none` structures (and YUNG\'s, which ignore the mode tags): no more air notches '
             'or floating pad, nothing else changes.')
    L.append('- The suggestion column is a starting point from the numbers only; look at a structure with `/ceplace` or '
             '`/locate` before tagging a whole family.')
    L.append('')
    L.append('| Verdict | Structures |')
    L.append('|---|---|')
    names = [('strong', 'Strong candidates'), ('mild', 'Mild candidates'), ('settlement', 'Settlements (review)'),
             ('compact', 'Compact surface structures (nothing to gain)'), ('tagged', 'Already tagged'),
             ('afloat', 'Ships, rafts and sky structures (earth_fill/exempt)'),
             ('skip-env', 'Not surface (buried, seabed, sky, underground, Nether, End, mostly water)'),
             ('follows', 'Terrain-matching only (already follow the ground)'),
             ('code', 'Built in code (no templates)'), ('unread', 'Templates not found')]
    for k, n in names:
        L.append(f'| {n} | {len(by.get(k, []))} |')
    L.append('')

    def section(title, key, text, sugg=True):
        L.append(f'## {title}')
        L.append('')
        L.append(text)
        L.append('')
        if not by.get(key):
            L.append('None.')
            L.append('')
            return
        L.append(HEAD.format('Suggested tags' if sugg else 'Why'))
        for r in by[key]:
            L.append(fmt_row(r, sugg))
        L.append('')

    section('1. Strong candidates', 'strong',
            'Surface structures whose templates are mostly pad or saved air. These look like the ballista did: a flat '
            'rectangle (and with `none`, a notch) the size of the whole template. "Worst template" is in the JSON.')
    section('2. Mild candidates', 'mild', 'Some pad or air around the building; trim and soft would help a little.')
    section('3. Settlements (review)', 'settlement',
            'Villages and other jigsaw settlements with terrain-matching streets. Their houses often carry small pads; '
            'tagging the structure changes every house in it, so look at one first (`/ceplace structures <id>`).')
    section('3b. Ships, rafts and sky structures', 'afloat',
            'They float on the sea or in the sky (`earth_fill/exempt`). At sea, trim would stop their saved air from replacing '
            'sea water around the hull: the water would stay where the template had air. Not suggested by default; the '
            'numbers are here if you want to try it on one.')
    section('4. Already tagged', 'tagged', 'In `worldgen/terrain_adaptation.js` now, with their numbers for reference.', sugg=False)
    section('5. Compact surface structures', 'compact', 'Built edge to edge; neither tag would change much.', sugg=False)
    section('6. Not surface structures', 'skip-env',
            'Their air carves rooms, their water belongs, or they float on purpose: trim would break them. Left alone.', sugg=False)
    L.append('## 7. Terrain-matching only, built in code, or no templates found')
    L.append('')
    L.append('| Structure | Type | Note |')
    L.append('|---|---|---|')
    for r in by.get('follows', []) + by.get('code', []) + by.get('unread', []):
        L.append(f"| `{r['id']}` | `{r['type']}` | {r['why']} |")
    for sid, note in VANILLA_CODE.items():
        L.append(f'| `{sid}` | vanilla | {note} |')
    L.append('')
    OUT_MD.write_text('\n'.join(L) + '\n')
    OUT_JSON.write_text(json.dumps({'generated': date.today().isoformat(), 'thresholds': {
        'strong': [STRONG_WASTE, STRONG_COLS], 'mild': [MILD_WASTE, MILD_COLS]}, 'structures': rows}, indent=1) + '\n')


if __name__ == '__main__':
    main()
