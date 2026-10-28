#!/usr/bin/env python3
"""
Loot breakdown: a readable, per-entry catalog of every container loot table in the pack, plus every
container whose contents are hand-placed in a structure template (no loot table at all).

Writes feature_writeup/loot_catalog/:
  README.md            how to read it, totals, index of mods (links to the files below)
  by_structure.md      per structure: tables rolled, container counts, spawn settings
  handplaced.md        containers with fixed items and no table (grouped by mod, then template), plus the
                       containers tools/lootr_fixed_containers.py already converted into chests/fixed/* tables
  tables/<ns>.md       one file per namespace: every container table (and the sub-tables they pull in)
                       with rolls, every entry (id, count min-max, weight, chance per roll, functions,
                       conditions), where it is placed, what code / GLMs add to it afterwards

Sources (same priority order as the game): vanilla -extra jar, mods/*.jar, kubejs/data. Tables are shown
AS SHIPPED, i.e. before the pack's own pre-roll rules (kubejs/server_scripts/loot/05_tuning.js,
loot_config.json, 07_rules.js) rewrite them. Pools mods add in code come from the /dumploot export
(kubejs/exported/loot_tables) and are marked.

Needs the template scan (tools/loot_breakdown_scan.py, cached) and the worldgen JSON in the jars.

Run:  python3 tools/loot_breakdown.py          (after mod updates / a fresh /dumploot)
"""
import glob
import json
import math
import os
import re
import sys
import zipfile
from collections import Counter, OrderedDict, defaultdict
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_loot_config as G  # noqa: E402
import loot_breakdown_scan as S  # noqa: E402
from pack_paths import loads_lenient  # noqa: E402  (Philip's Ruins ships annotated worldgen json)
from loot_census import chest_scope  # noqa: E402

ROOT = G.INSTANCE
OUT = ROOT / 'feature_writeup/loot_catalog'
WORLDGEN = ROOT / 'kubejs/generation_scripts/worldgen'

# ------------------------------------------------------------------------------------ vanilla knowledge
# Vanilla structures are registered in code (no worldgen JSON in the 1.19.2 data jar), so their jigsaw start
# pools and the loot tables they set in code are written down here.
VANILLA_START_POOLS = {
    'minecraft:village_plains': 'minecraft:village/plains/town_centers',
    'minecraft:village_desert': 'minecraft:village/desert/town_centers',
    'minecraft:village_savanna': 'minecraft:village/savanna/town_centers',
    'minecraft:village_snowy': 'minecraft:village/snowy/town_centers',
    'minecraft:village_taiga': 'minecraft:village/taiga/town_centers',
    'minecraft:pillager_outpost': 'minecraft:pillager_outpost/base_plates',
    'minecraft:bastion_remnant': 'minecraft:bastion/starts',
    'minecraft:ancient_city': 'minecraft:ancient_city/city_center',
}
VANILLA_TEMPLATE_PREFIX = [  # non-jigsaw vanilla structures that place templates: prefix -> structure
    ('minecraft:igloo/', ['minecraft:igloo']),
    ('minecraft:shipwreck/', ['minecraft:shipwreck', 'minecraft:shipwreck_beached']),
    ('minecraft:underwater_ruin/', ['minecraft:ocean_ruin_cold', 'minecraft:ocean_ruin_warm']),
    ('minecraft:woodland_mansion/', ['minecraft:mansion']),
    ('minecraft:end_city/', ['minecraft:end_city']),
    ('minecraft:ruined_portal/', ['minecraft:ruined_portal']),
    ('minecraft:endcity/', ['minecraft:end_city']),
    ('minecraft:fossils/', []),
    ('minecraft:ancient_city/', ['minecraft:ancient_city']),
    ('quark:big_dungeon/', ['quark:big_dungeon']),
]
VANILLA_CODE_TABLES = {  # tables vanilla code assigns to containers (no template scan can see them)
    'minecraft:chests/abandoned_mineshaft': 'minecraft:mineshaft / mineshaft_mesa (chest minecart, code)',
    'minecraft:chests/buried_treasure': 'minecraft:buried_treasure (code)',
    'minecraft:chests/desert_pyramid': 'minecraft:desert_pyramid (code)',
    'minecraft:chests/end_city_treasure': 'minecraft:end_city (templates + code)',
    'minecraft:chests/igloo_chest': 'minecraft:igloo (template metadata, code)',
    'minecraft:chests/jungle_temple': 'minecraft:jungle_pyramid (code)',
    'minecraft:chests/jungle_temple_dispenser': 'minecraft:jungle_pyramid (dispenser, code)',
    'minecraft:chests/nether_bridge': 'minecraft:fortress (code)',
    'minecraft:chests/ruined_portal': 'minecraft:ruined_portal (template metadata)',
    'minecraft:chests/shipwreck_map': 'minecraft:shipwreck (template metadata)',
    'minecraft:chests/shipwreck_supply': 'minecraft:shipwreck (template metadata)',
    'minecraft:chests/shipwreck_treasure': 'minecraft:shipwreck (template metadata)',
    'minecraft:chests/simple_dungeon': 'monster room (feature, code)',
    'minecraft:chests/spawn_bonus_chest': 'bonus chest world option (code)',
    'minecraft:chests/stronghold_corridor': 'minecraft:stronghold (code)',
    'minecraft:chests/stronghold_crossing': 'minecraft:stronghold (code)',
    'minecraft:chests/stronghold_library': 'minecraft:stronghold (code)',
    'minecraft:chests/underwater_ruin_big': 'minecraft:ocean_ruin_* (template metadata)',
    'minecraft:chests/underwater_ruin_small': 'minecraft:ocean_ruin_* (template metadata)',
    'minecraft:chests/woodland_mansion': 'minecraft:mansion (template metadata)',
}
# Table families that are not structure chests even though they sit under chests/ or are untyped.
NON_CONTAINER_NS = {
    'wares': 'Wares trade agreements and packages (rolled when a delivery is generated, not by a chest)',
    'oldguns': 'Old Guns blueprint / design-note tables (rolled by the mod, injected into dungeon chests by GLMs)',
}

CONTAINER_LIKE = {'minecraft:chest', 'minecraft:trapped_chest', 'minecraft:barrel', 'farmersdelight:basket',
                  'minecraft:chiseled_bookshelf', 'aether:altar'}

# ------------------------------------------------------------------------------------------ helpers


def md_escape(s):
    return str(s).replace('|', '\\|').replace('\n', ' ')


def num(x):
    if isinstance(x, float) and x == int(x):
        return str(int(x))
    if isinstance(x, float):
        return f'{x:.4g}'
    return str(x)


def short_jar(label):
    return re.sub(r'\.jar$', '', label)


def slug(tid):
    return re.sub(r'[^a-z0-9_-]', '-', tid.lower())


def np_range(v):
    """Number provider -> (text, min, max, mean)."""
    if isinstance(v, (int, float)):
        return num(v), float(v), float(v), float(v)
    if not isinstance(v, dict):
        return '1', 1.0, 1.0, 1.0
    t = G.norm_type(v.get('type')) or ('minecraft:uniform' if 'min' in v else 'minecraft:constant')
    if t == 'minecraft:constant':
        x = v.get('value', 1)
        if isinstance(x, dict):
            return np_range(x)
        return num(x), float(x), float(x), float(x)
    if t == 'minecraft:uniform':
        lo, hi = np_range(v.get('min', 0)), np_range(v.get('max', 1))
        lo_t, hi_t = (lo[0], hi[0])
        if lo[1] == hi[2]:
            return lo_t, lo[1], lo[1], lo[1]
        return f'{lo_t}-{hi_t}', lo[1], hi[2], (lo[3] + hi[3]) / 2
    if t == 'minecraft:binomial':
        n = np_range(v.get('n', 1))
        p = v.get('p', 0.5)
        p = p if isinstance(p, (int, float)) else 0.5
        return f'0-{n[0]} (binomial p={num(p)})', 0.0, n[2], n[3] * p
    if t == 'minecraft:score':
        return f"score {v.get('score')}", 0.0, 0.0, 0.0
    return t.split(':')[-1], 1.0, 1.0, 1.0


def fmt_cond(c):
    if isinstance(c, list):
        return ' & '.join(fmt_cond(x) for x in c)
    cid = G.norm_id(c.get('condition', 'minecraft:?'))
    name = cid.split(':', 1)[1] if cid.startswith('minecraft:') else cid
    rest = {k: v for k, v in c.items() if k != 'condition'}
    if name in ('random_chance', 'random_chance_with_looting'):
        extra = f", looting x{num(rest['looting_multiplier'])}" if 'looting_multiplier' in rest else ''
        return f"chance {num(rest.get('chance', 1))}{extra}"
    if name == 'killed_by_player':
        return 'killed by player'
    if name == 'inverted':
        return 'not ' + fmt_cond(rest.get('term', {}))
    if name in ('alternative', 'any_of'):
        return '(' + ' or '.join(fmt_cond(t) for t in rest.get('terms', [])) + ')'
    if name == 'entity_properties':
        return 'entity ' + json.dumps(rest.get('predicate', {}), separators=(',', ':'))[:90]
    if name == 'location_check':
        return 'location ' + json.dumps(rest.get('predicate', {}), separators=(',', ':'))[:90]
    if name == 'table_bonus':
        return f"table_bonus {rest.get('enchantment')} {rest.get('chances')}"
    if name == 'match_tool':
        return 'tool ' + json.dumps(rest.get('predicate', {}), separators=(',', ':'))[:90]
    if not rest:
        return name
    return name + ' ' + json.dumps(rest, separators=(',', ':'), ensure_ascii=False)[:100]


def fmt_conds(conds):
    return '; '.join(fmt_cond(c) for c in conds or [])


def short_snbt(s, n=110):
    s = str(s)
    return s if len(s) <= n else s[:n - 3] + '...'


def count_info(fns):
    """(count text, count min, count max, other function notes)."""
    cnt = '1'
    lo = hi = 1.0
    notes = []
    for f in fns or []:
        fid = G.norm_id(f.get('function', '?'))
        name = fid.split(':', 1)[1] if fid.startswith('minecraft:') else fid
        cond = fmt_conds(f.get('conditions'))
        cond = f' [if {cond}]' if cond else ''
        if name == 'set_count':
            t, a, b, _ = np_range(f.get('count', 1))
            if f.get('add'):
                cnt, lo, hi = f'+{t}', lo + a, hi + b
            else:
                cnt, lo, hi = t, a, b
            if cond:
                notes.append('count' + cond)
        elif name == 'limit_count':
            lim = f.get('limit')
            notes.append(f'limit {np_range(lim)[0] if not isinstance(lim, dict) or "min" not in lim and "max" not in lim else json.dumps(lim)}')
        elif name == 'enchant_with_levels':
            lv = np_range(f.get('levels', 1))[0]
            opt = f.get('options')
            extra = ' (treasure)' if f.get('treasure') else ''
            if opt:
                opts = opt if isinstance(opt, list) else [opt]
                extra += ' from ' + ','.join(G.norm_id(o).replace('minecraft:', '') for o in opts)
            notes.append(f'enchant lvl {lv}{extra}{cond}')
        elif name == 'enchant_randomly':
            ens = f.get('enchantments')
            notes.append('random enchant' + (' from ' + ','.join(G.norm_id(o).replace('minecraft:', '') for o in ens) if ens else '') + cond)
        elif name == 'set_nbt':
            notes.append('nbt ' + short_snbt(f.get('tag', '')) + cond)
        elif name == 'set_potion':
            notes.append(f"potion {f.get('id')}" + cond)
        elif name == 'set_damage':
            notes.append(f"damage {np_range(f.get('damage', 1))[0]}" + cond)
        elif name == 'set_name':
            nm = f.get('name')
            notes.append('name ' + short_snbt(json.dumps(nm, ensure_ascii=False) if not isinstance(nm, str) else nm, 60) + cond)
        elif name == 'set_lore':
            notes.append('lore' + cond)
        elif name == 'exploration_map':
            notes.append(f"map to {f.get('destination')}" + cond)
        elif name == 'set_stew_effect':
            notes.append('stew effects ' + ','.join(str(e.get('type', '?')).replace('minecraft:', '') for e in f.get('effects', [])) + cond)
        elif name == 'set_enchantments':
            notes.append('enchants ' + json.dumps(f.get('enchantments', {}), separators=(',', ':')) + cond)
        elif name == 'set_attributes':
            notes.append('attributes' + cond)
        elif name == 'set_contents':
            notes.append('contents' + cond)
        elif name == 'looting_enchant':
            notes.append('looting bonus' + cond)
        elif name in ('furnace_smelt', 'explosion_decay'):
            notes.append(name.replace('_', ' ') + cond)
        elif name in ('copy_name', 'copy_nbt', 'copy_state'):
            notes.append(name + cond)
        elif name == 'set_loot_table':
            notes.append(f"container table {f.get('name')}" + cond)
        elif name == 'set_instrument':
            notes.append('instrument ' + str(f.get('options')) + cond)
        else:
            rest = {k: v for k, v in f.items() if k not in ('function', 'conditions')}
            notes.append(name + (' ' + short_snbt(json.dumps(rest, separators=(',', ':'), ensure_ascii=False), 80) if rest else '') + cond)
    return cnt, lo, hi, notes


def entry_label(e):
    t = G.norm_type(e.get('type')) or 'minecraft:item'
    name = e.get('name')
    if t == 'minecraft:item':
        return f'`{G.norm_id(name)}`'
    if t == 'minecraft:tag':
        return f"`#{G.norm_id(name)}`" + (' (every member)' if e.get('expand') else ' (one member)')
    if t == 'minecraft:loot_table':
        return None  # handled by the caller (needs a link)
    if t == 'minecraft:empty':
        return '*(nothing)*'
    if t == 'minecraft:dynamic':
        return f'dynamic `{name}`'
    kind = t.split(':', 1)[1] if t.startswith('minecraft:') else t
    return f'*{kind}*'


# --------------------------------------------------------------------------------------------- data


class Data:
    def __init__(self):
        self.overrides = {}
        self.tables, self.glms = G.load_sources(self.overrides)
        self.export = G.load_export() or {}
        self.runtime = G.load_runtime_additions()
        self.tags = G.load_tags()
        self.index = json.loads(G.INDEX_PATH.read_text())
        self.report = self.index['report']
        self.templates = S.scan()
        self.table_origin = {}
        for label, read, names in G.iter_data_sources():
            for n in names:
                m = re.match(r'data/([^/]+)/loot_tables/(.+)\.json$', n)
                if m:
                    self.table_origin[f'{m[1]}:{m[2]}'] = label
        self.build_code_pools()
        self.build_structures()
        self.build_placements()
        self.build_scope()
        self.build_code_references()

    # pools added by mod code after load (LootTableLoadEvent): export pools past the raw count, entries added
    # to an existing pool = in the export but not the JSON (see gen_loot_config.injected_entries)
    def build_code_pools(self):
        self.code_pools, self.injected = {}, {}
        self.analysis = dict(self.tables)
        for tid, raw in self.tables.items():
            if tid not in self.export:
                continue
            ours = self.runtime.get(tid, {'pools': 0, 'items': set()})
            raw_pools = raw.get('pools', [])
            exp_pools = self.export[tid].get('pools', [])
            extra = exp_pools[len(raw_pools) + ours['pools']:]
            if extra:
                self.code_pools[tid] = extra
            for i in range(min(len(raw_pools), len(exp_pools))):
                add = G.injected_entries(raw_pools[i], exp_pools[i], ours['items'])
                if add:
                    self.injected.setdefault(tid, {}).setdefault(i, []).extend(add)

    def build_structures(self):
        """structure id -> {start, biomes, group, freq}; template -> {structures}."""
        structs, pools = {}, {}
        for label, read, names in G.iter_data_sources():
            for n in names:
                m = re.match(r'data/([^/]+)/worldgen/(structure|template_pool)/(.+)\.json$', n)
                if not m:
                    continue
                try:
                    d = loads_lenient(read(n))
                except Exception:
                    continue
                (structs if m[2] == 'structure' else pools)[f'{m[1]}:{m[3]}'] = d
        live = json.loads((WORLDGEN / 'pool_config.json').read_text())['pools']
        cfg = json.loads((WORLDGEN / 'structure_config.json').read_text())['structure_sets']
        groups = json.loads((WORLDGEN / 'structure_groups.json').read_text())
        self.struct_info = {}
        for sid, d in structs.items():
            self.struct_info[sid] = {'start': G.norm_id(d['start_pool']) if d.get('start_pool') else None}
        for sid, sp in VANILLA_START_POOLS.items():
            self.struct_info.setdefault(sid, {})['start'] = sp
        for sid in list(self.struct_info):
            self.struct_info[sid].setdefault('biomes', None)
        for sname, s in cfg.items():
            for st in s.get('structures', []):
                info = self.struct_info.setdefault(st['structure'], {'start': None})
                info['biomes'] = st.get('biomes')
                info['set'] = sname
                if st.get('blocked'):
                    info['blocked'] = st['blocked']
        for gname, g in groups.get('groups', {}).items():
            for sid, v in g.get('structures', {}).items():
                info = self.struct_info.setdefault(sid, {'start': None})
                info['group'] = gname
                info['freq'] = v.get('frequency', 1)
                info['enabled'] = v.get('enabled', True)
        for sname, g in groups.get('standalone', {}).items():
            for sid, v in g.get('structures', {}).items():
                info = self.struct_info.setdefault(sid, {'start': None})
                info['group'] = None
                info['freq'] = v.get('frequency', 1)
                info['enabled'] = v.get('enabled', True)

        def elems(pid):
            out = set()
            p = pools.get(pid)
            if p:
                def walk(el):
                    if 'location' in el:
                        out.add(G.norm_id(el['location']))
                    for x in el.get('elements', []):
                        walk(x)
                for e in p.get('elements', []):
                    walk(e['element'])
            lp = live.get(pid)
            if lp:
                out.update(G.norm_id(k) for k in lp['elements'])
            return out

        sizes = {sid: d.get('size') for sid, d in structs.items()}
        VANILLA_SIZE = {'minecraft:pillager_outpost': 7, 'minecraft:bastion_remnant': 6, 'minecraft:ancient_city': 7}
        self.reach = defaultdict(set)
        for sid, info in self.struct_info.items():
            sp = info.get('start')
            if not sp:
                continue
            size = sizes.get(sid)
            if size is None:
                size = VANILLA_SIZE.get(sid, 6)
            # level-by-level walk: a piece at depth d may open jigsaws only while d < size (the game's own limit);
            # without it a standalone house that has one jigsaw into a village street pool "reaches" the whole village
            best = {}
            frontier = [sp]
            depth = 0
            seen_t = set()
            while frontier and depth <= size:
                nxt = []
                for p in frontier:
                    if best.get(p, 99) <= depth:
                        continue
                    best[p] = depth
                    for t in elems(p):
                        seen_t.add(t)
                        self.reach[t].add(sid)
                        for jp in self.templates.get(t, {}).get('jigsaw', []):
                            nxt.append(G.norm_id(jp))
                    fb = (pools.get(p) or {}).get('fallback') or (live.get(p) or {}).get('fallback')
                    if fb and fb != 'minecraft:empty':
                        nxt.append(G.norm_id(fb))
                frontier = nxt
                depth += 1
        for t in self.templates:
            for prefix, sids in VANILLA_TEMPLATE_PREFIX:
                if t.startswith(prefix):
                    self.reach[t].update(sids)
        # template namespace fallback for code-registered structures with one structure of the same namespace
        by_ns = defaultdict(set)
        for sid in self.struct_info:
            by_ns[sid.split(':')[0]].add(sid)
        self.ns_fallback = by_ns

    def structures_for(self, template):
        s = self.reach.get(template)
        if s:
            return sorted(s), False
        ns = template.split(':')[0]
        c = self.ns_fallback.get(ns)
        if c and len(c) <= 3:
            return sorted(c), True  # guessed from the namespace
        return [], False

    def build_placements(self):
        """table -> {'containers': n, 'templates': {template: n}}; fixed tables included."""
        self.placed = defaultdict(lambda: {'containers': 0, 'templates': Counter()})
        for tid, v in self.templates.items():
            for c in v['containers']:
                if c['loot']:
                    p = self.placed[G.norm_id(c['loot'])]
                    p['containers'] += 1
                    p['templates'][tid] += 1

    def build_scope(self):
        effective = {t: dict(v) for t, v in self.tables.items()}
        glm_pairs = {k: v for k, v in self.glms.items()}
        self.scope, self.roots = chest_scope(effective, self.code_pools, glm_pairs, set(self.placed))
        # tools/lootr_fixed_containers.py output: hand-placed contents turned into tables; shown in handplaced.md part C
        self.fixed_tables = {t for t in self.scope if ':chests/fixed/' in t}
        self.scope -= self.fixed_tables
        self.roots -= self.fixed_tables
        parents = defaultdict(set)
        glm_edges = defaultdict(set)
        for gid, (label, data) in self.glms.items():
            sub = data.get('lootTable') or data.get('loot_table') or data.get('table')
            if isinstance(sub, str):
                for t in G.glm_targets(data.get('conditions')):
                    glm_edges[t].add((G.norm_id(sub), gid))
        self.parents, self.glm_parents = defaultdict(set), defaultdict(set)
        for tid in self.scope:
            pools = list(self.tables.get(tid, {}).get('pools', [])) + self.code_pools.get(tid, [])
            for sub in G.table_refs(pools):
                if sub in self.scope:
                    self.parents[sub].add(tid)
            for sub, gid in glm_edges.get(tid, ()):
                if sub in self.scope:
                    self.glm_parents[sub].add((tid, gid))

    def build_code_references(self):
        """For tables with no template, parent table or GLM parent: look for the path string in the classes / json /
        toml of the jars that ship the table's namespace (code-set tables). {table: [(jar, file)]}"""
        unresolved = [t for t in self.scope if t not in self.placed and t not in self.parents
                      and t not in self.glm_parents and t not in VANILLA_CODE_TABLES]
        by_ns = defaultdict(list)
        for t in unresolved:
            by_ns[t.split(':')[0]].append(t)
        self.code_refs = {}
        for jar in sorted(glob.glob(str(ROOT / 'mods/*.jar'))):
            try:
                z = zipfile.ZipFile(jar)
            except zipfile.BadZipFile:
                continue
            names = z.namelist()
            owned = {ns for ns in by_ns if any(n.startswith(f'data/{ns}/loot_tables/') for n in names)}
            if not owned:
                continue
            needles = [(t, t.split(':', 1)[1].encode(), t.encode()) for ns in owned for t in by_ns[ns]]
            for n in names:
                if '/loot_tables/' in n or not n.endswith(('.class', '.json', '.toml', '.cfg', '.js')):
                    continue
                try:
                    b = z.read(n)
                except Exception:
                    continue
                is_class = n.endswith('.class')
                for t, path_b, full_b in needles:
                    if t in self.code_refs:
                        continue
                    # json/toml name the full id; classes usually build it from (namespace, path), so the path
                    # alone has to be specific enough not to match unrelated strings
                    if (full_b in b) or (is_class and (len(path_b) >= 10 or b'/' in path_b) and path_b in b):
                        self.code_refs[t] = [(os.path.basename(jar), n)]

    def code_reference(self, tid):
        return self.code_refs.get(tid, [])


# ---------------------------------------------------------------------------------------- table render


def table_link(tid, home=None):
    """Link from inside tables/<ns>.md (same folder) to a table's heading in its namespace file."""
    return f'[`{tid}`]({tid.split(":", 1)[0]}.md#{slug(tid)})'


def glm_text(entries):
    out = []
    for e in entries:
        m = re.match(r'^(\S+) \(([\d.]+)%, (.+)\)$', e)
        out.append(f'`{m[1]}` {m[2]}% (via `{m[3]}`)' if m else e)
    return '; '.join(out)


def render_entries(entries, total, depth, d, lines, scope, tablelink, inject=False):
    for e in entries:
        t = G.norm_type(e.get('type')) or 'minecraft:item'
        w = float(e.get('weight', 1))
        conds = fmt_conds(e.get('conditions'))
        quality = e.get('quality')
        children = e.get('children')
        cnt, lo, hi, notes = count_info(e.get('functions'))
        label = entry_label(e)
        if t == 'minecraft:loot_table':
            sub = G.norm_id(e['name'])
            label = f'-> table {tablelink(sub)}' if sub in scope else f'-> table `{sub}`'
            cnt = 'sub-table'
        if children is not None:
            kind = t.split(':', 1)[1] if t.startswith('minecraft:') else t
            label = {'alternatives': 'first matching of', 'group': 'all of', 'sequence': 'in order until one fails'}.get(kind, kind)
            label = f'*{label}:*'
            cnt = ''
        if t in ('minecraft:item',) and not e.get('functions'):
            cnt = '1'
        if t == 'minecraft:empty':
            cnt = ''
        pad = '&nbsp;&nbsp;' * depth + ('↳ ' if depth else '')
        n = []
        if quality:
            n.append(f'quality {num(quality)}')
        if conds:
            n.append(f'if {conds}')
        n += notes
        if inject:
            n.append('**added in code by a mod**')
        chance = '' if total <= 0 or depth else f'{w / total * 100:.3g}%'
        wtxt = '' if depth else num(w)
        lines.append(f"| {pad}{label} | {md_escape(cnt)} | {wtxt} | {chance} | {md_escape('; '.join(n))} |")
        if children is not None:
            render_entries(children, total, depth + 1, d, lines, scope, tablelink)


def render_pool(i, pool, lines, scope, tablelink, injected=(), label_extra=''):
    rolls_t, rmin, rmax, rmean = np_range(pool.get('rolls', 1))
    bonus = pool.get('bonus_rolls', 0)
    bt = np_range(bonus)[0] if bonus not in (0, 0.0) else None
    conds = fmt_conds(pool.get('conditions'))
    entries = list(pool.get('entries', []))
    allw = entries + [dict(e) for e in injected]
    total = sum(float(e.get('weight', 1)) for e in allw)
    head = f'**Pool {i}**{label_extra} - rolls {rolls_t}'
    if bt:
        head += f', bonus rolls {bt} per luck'
    if pool.get('name'):
        head += f" (named `{pool['name']}`)"
    if conds:
        head += f'; only if {conds}'
    head += f'; total weight {num(total)}'
    lines.append(head)
    lines.append('')
    lines.append('| Entry | Count | Weight | Chance per roll | Notes |')
    lines.append('|---|---|---:|---:|---|')
    render_entries(entries, total, 0, None, lines, scope, tablelink)
    if injected:
        render_entries(injected, total, 0, None, lines, scope, tablelink, inject=True)
    lines.append('')
    return rmin, rmax, rmean


def structure_text(D, sid):
    info = D.struct_info.get(sid, {})
    bits = []
    if info.get('group'):
        bits.append(f"group {info['group']}")
    if info.get('freq') is not None and info['freq'] != 1:
        bits.append(f"freq {num(round(info['freq'], 3))}")
    if info.get('enabled') is False:
        bits.append('**DISABLED in structure_groups.json**')
    if info.get('blocked'):
        bits.append(f"**blocked: {short_snbt(info['blocked'], 70)}**")
    b = info.get('biomes')
    if isinstance(b, list):
        if len(b) > 6:
            bits.append(f'biomes: {", ".join(x.replace("minecraft:", "") for x in b[:6])} +{len(b) - 6} more')
        elif b:
            bits.append('biomes: ' + ', '.join(x.replace('minecraft:', '') for x in b))
    elif isinstance(b, str):
        bits.append(f'biomes: `{b}`')
    return f"`{sid}`" + (f" ({'; '.join(bits)})" if bits else '')


def render_table(D, tid, lines):
    t = D.tables[tid]
    ns, path = tid.split(':', 1)
    lines.append(f'<a id="{slug(tid)}"></a>')
    lines.append(f'### `{tid}`')
    lines.append('')
    ttype = G.norm_type(t.get('type')) or 'untyped'
    src = D.table_origin.get(tid, '?')
    ov = D.overrides.get(tid)
    role = None
    placed = D.placed.get(tid)
    facts = []
    facts.append(f'- **Mod / source:** {short_jar(src)}' + (f" (overrides a copy in {', '.join(short_jar(x) for x in ov['over'])})" if ov else '') + f' | declared type `{ttype}`')
    if tid in D.parents or tid in D.glm_parents:
        ps = sorted(D.parents.get(tid, ()))
        gp = sorted({p for p, _ in D.glm_parents.get(tid, ())})
        if ps:
            facts.append('- **Sub-table of:** ' + ', '.join(table_link(p, ns) if p in D.scope else f'`{p}`' for p in ps[:12]) + (f' (+{len(ps) - 12} more)' if len(ps) > 12 else ''))
        if gp:
            facts.append('- **Rolled into by global loot modifier:** ' + ', '.join(table_link(p, ns) if p in D.scope else f'`{p}`' for p in gp[:12]))
    if ns in NON_CONTAINER_NS and not placed:
        facts.append(f'- **Not a chest table:** {NON_CONTAINER_NS[ns]}')
    if placed:
        n_t = len(placed['templates'])
        facts.append(f"- **Placed by:** {placed['containers']} container(s) in {n_t} structure template(s)")
        sset = Counter()
        guessed = set()
        for tpl, n in placed['templates'].items():
            ss, guess = D.structures_for(tpl)
            for s in ss:
                sset[s] += n
                if guess:
                    guessed.add(s)
        if sset:
            shown = [structure_text(D, s) + (' (guessed from namespace)' if s in guessed else '') for s, _ in sset.most_common(8)]
            facts.append('- **Structures:** ' + '; '.join(shown) + (f' ... +{len(sset) - 8} more' if len(sset) > 8 else ''))
        else:
            facts.append('- **Structures:** none resolved (templates not reachable from a known structure start pool; may be unused or placed in code)')
        tl = sorted(placed['templates'].items())
        tpl_txt = ', '.join(f'`{a}`' + (f' x{n}' if n > 1 else '') for a, n in tl[:6])
        if len(tl) > 6:
            rest = ', '.join(f'`{a}`' + (f' x{n}' if n > 1 else '') for a, n in tl[6:])
            facts.append(f'- **Templates:** {tpl_txt} ... <details><summary>all {len(tl)}</summary>{rest}</details>')
        else:
            facts.append(f'- **Templates:** {tpl_txt}')
    elif tid in VANILLA_CODE_TABLES:
        facts.append(f'- **Placed by:** {VANILLA_CODE_TABLES[tid]}')
    elif tid not in D.parents and tid not in D.glm_parents and ns not in NON_CONTAINER_NS:
        hits = D.code_reference(tid)
        if hits:
            facts.append('- **Placed by:** referenced outside the loot tables, i.e. set by code or data (found in ' + ', '.join(f'`{short_jar(j)}` {n.rsplit("/", 1)[-1]}' for j, n in hits[:3]) + ')')
        else:
            facts.append('- **Placed by:** **unused as installed**: no structure template, parent table, loot modifier or jar code mentions it')
    inj = D.report.get('injected_entries', {}).get(tid)
    glm_items = D.report.get('glm_items', {}).get(tid)
    glm_sub = D.report.get('glm_sub_tables', {}).get(tid)
    if glm_items:
        facts.append('- **Global loot modifiers add after the roll:** ' + glm_text(glm_items))
    if glm_sub:
        facts.append('- **Global loot modifiers roll these tables as well:** ' + ', '.join(table_link(x, ns) if x in D.scope else f'`{x}`' for x in glm_sub))
    if tid in D.code_pools:
        facts.append(f'- **Mod code appends {len(D.code_pools[tid])} pool(s)** (listed below, marked)')
    if tid in D.injected:
        facts.append('- **Mod code adds entries to existing pools** (listed in the pool, marked)')
    if ns == 'pneumaticcraft':
        facts.append('- **Pack:** PneumaticCraft chest loot is switched off in its config; the pack bans its items from chests')
    lines += facts
    lines.append('')
    tot_min = tot_max = tot_mean = 0.0
    pools = t.get('pools', [])
    if not pools:
        lines.append('*(no pools)*')
        lines.append('')
    for i, pool in enumerate(pools, 1):
        a, b, c = render_pool(i, pool, lines, D.scope, lambda x: table_link(x, ns), injected=D.injected.get(tid, {}).get(i - 1, ()))
        tot_min, tot_max, tot_mean = tot_min + a, tot_max + b, tot_mean + c
    n0 = len(pools)
    for j, pool in enumerate(D.code_pools.get(tid, []), n0 + 1):
        a, b, c = render_pool(j, pool, lines, D.scope, lambda x: table_link(x, ns), label_extra=' (added in code by a mod)')
        tot_min, tot_max, tot_mean = tot_min + a, tot_max + b, tot_mean + c
    if len(pools) + len(D.code_pools.get(tid, [])) > 1 or tot_max:
        rng = num(tot_min) if tot_min == tot_max else f'{num(tot_min)}-{num(tot_max)}'
        lines.append(f'Draws per open: {rng} (mean {tot_mean:.3g}) across all pools, before empty entries and sub-table rolls.')
        lines.append('')


def render_tables(D):
    by_ns = defaultdict(list)
    for tid in sorted(D.scope):
        by_ns[tid.split(':', 1)[0]].append(tid)
    tdir = OUT / 'tables'
    tdir.mkdir(parents=True, exist_ok=True)
    for old in tdir.glob('*.md'):
        old.unlink()
    summary = []
    for ns, tids in sorted(by_ns.items()):
        lines = [f'# Loot tables: `{ns}`', '',
                 f'Generated by `python3 tools/loot_breakdown.py` on {date.today().isoformat()}. As shipped (before the pack\'s pre-roll rules). '
                 'Back to the [index](../README.md).', '']
        srcs = Counter(short_jar(D.table_origin.get(t, '?')) for t in tids)
        lines.append('Source: ' + ', '.join(f'{k} ({v})' for k, v in srcs.most_common()))
        lines.append('')
        lines.append('| Table | Role | Pools | Entries |')
        lines.append('|---|---|---:|---:|')
        for tid in tids:
            tb = D.tables[tid]
            role = 'container' if tid in D.placed else ('sub-table' if (tid in D.parents or tid in D.glm_parents) else 'code / unused')
            n_pools = len(tb.get('pools', [])) + len(D.code_pools.get(tid, []))
            n_ent = sum(len(list(G.walk_entries(p.get('entries')))) for p in tb.get('pools', []) + D.code_pools.get(tid, []))
            lines.append(f"| [`{tid}`](#{slug(tid)}) | {role} | {n_pools} | {n_ent} |")
        lines.append('')
        for tid in tids:
            render_table(D, tid, lines)
        (tdir / f'{ns}.md').write_text('\n'.join(lines) + '\n')
        summary.append((ns, tids))
    return summary


# ---------------------------------------------------------------------------------------- hand-placed


def merge_stacks(items):
    """[(id, tag, total count, stacks)] in first-seen order."""
    order = OrderedDict()
    for s in items:
        k = (s['id'], s.get('tag'))
        if k not in order:
            order[k] = [0, 0]
        order[k][0] += s['n']
        order[k][1] += 1
    return [(k[0], k[1], v[0], v[1]) for k, v in order.items()]


def contents_text(items):
    if not items:
        return '*(empty)*'
    out = []
    for iid, tag, n, stacks in merge_stacks(items):
        t = f'`{iid}` x{n}' + (f' ({stacks} stacks)' if stacks > 1 else '')
        if tag:
            t += f' [nbt {short_snbt(tag, 120)}]'
        out.append(t)
    return ', '.join(out)


def origin_names(D):
    """namespace -> origin jar for ported structure namespaces in kubejs/data (the jar sits in disabledmods/archive/)."""
    out = {}
    for jar in glob.glob(str(ROOT / 'disabledmods/**/*.jar'), recursive=True):
        try:
            z = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        for n in z.namelist():
            m = re.match(r'data/([^/]+)/structures/', n)
            if m and m[1] not in out:
                out[m[1]] = os.path.basename(jar)
    return out


def render_handplaced(D):
    ported = origin_names(D)
    lines = ['# Hand-placed loot: containers with fixed contents and no loot table', '',
             f'Generated by `python3 tools/loot_breakdown.py` on {date.today().isoformat()}. Back to the [index](README.md).', '',
             'A structure builder filled these containers by hand, so nothing is rolled: **every item listed is always there** '
             '(count shown is the fixed stack size; slot positions are not shown). Lootr cannot give these containers per-player loot, '
             'because it only converts containers that have a loot table. To replace one with your own table you have to edit the template '
             '(or run `tools/lootr_fixed_containers.py`, which does it for the structures in `kubejs/data`).', '',
             'Positions are block coordinates inside the template (x, y, z), enough to find the container in the structure block. '
             '"Structures" resolves each template to the structure(s) that can place it; village houses are one piece of a village.', '']

    # collect rows
    rows = []  # (origin, template, container)
    converted = defaultdict(list)  # fixed table -> [(template, container)]
    for tid, v in sorted(D.templates.items()):
        for c in v['containers']:
            if c['loot'] and '/fixed/' in c['loot'] and ':chests/fixed/' in G.norm_id(c['loot']):
                converted[G.norm_id(c['loot'])].append((tid, v['origin']))
            if not c['loot'] and c['items']:
                rows.append((v['origin'], tid, c))
    loot_like = [r for r in rows if r[2]['block'] in CONTAINER_LIKE]
    functional = [r for r in rows if r[2]['block'] not in CONTAINER_LIKE]

    def mod_label(origin, tid):
        if origin == 'vanilla':
            return 'Vanilla Minecraft'
        if origin == 'kubejs/data':
            ns = tid.split(':')[0]
            return f"{ported.get(ns, 'kubejs/data')} (ported to kubejs/data as `{ns}`)"
        return short_jar(origin)

    # ---- summary
    cnt = Counter(r[2]['block'] for r in rows)
    lines += ['## Totals', '',
              f'{len(rows)} containers with fixed items in {len({r[1] for r in rows})} templates '
              f'({len(loot_like)} chest-like, {len(functional)} functional); '
              f'{sum(len(v) for v in converted.values())} more were already converted to loot tables by the pack (part C).', '',
              '| Block | Containers |', '|---|---:|']
    for b, n in cnt.most_common():
        lines.append(f'| `{b}` | {n} |')
    lines.append('')

    def section_by_mod(title, rowset, grouped_functional):
        lines.append(title)
        lines.append('')
        by_mod = defaultdict(list)
        for o, t, c in rowset:
            by_mod[mod_label(o, t)].append((t, c))
        for mod, items in sorted(by_mod.items(), key=lambda kv: kv[0].lower()):
            lines.append(f'### {mod}')
            lines.append('')
            if grouped_functional:
                groups = OrderedDict()
                for t, c in items:
                    sig = (c['block'], tuple((s['id'], s['n'], s.get('tag')) for s in sorted(c['items'], key=lambda s: s.get('slot', 0))))
                    groups.setdefault(sig, []).append((t, c))
                lines.append('| Block | Count | In templates | Fixed contents |')
                lines.append('|---|---:|---|---|')
                for sig, lst in sorted(groups.items(), key=lambda kv: (kv[0][0], -len(kv[1]))):
                    tpls = Counter(t for t, _ in lst)
                    shown = ', '.join(f'`{a}`' + (f' x{n}' if n > 1 else '') for a, n in sorted(tpls.items())[:4])
                    if len(tpls) > 4:
                        shown += f' ... +{len(tpls) - 4} more'
                    lines.append(f"| `{sig[0]}` | {len(lst)} | {shown} | {md_escape(contents_text(lst[0][1]['items']))} |")
                lines.append('')
            else:
                by_t = defaultdict(list)
                for t, c in items:
                    by_t[t].append(c)
                for t, cs in sorted(by_t.items()):
                    ss, guess = D.structures_for(t)
                    stext = '; '.join(structure_text(D, s) for s in ss[:5]) + (f' +{len(ss) - 5} more' if len(ss) > 5 else '')
                    if guess and stext:
                        stext += ' (guessed from namespace)'
                    lines.append(f"**`{t}`**: {len(cs)} container(s). Structures: {stext or 'none resolved (template not reachable from a known start pool)'}")
                    lines.append('')
                    sigs = OrderedDict()
                    for c in cs:
                        sig = (c['block'], tuple((s['id'], s['n'], s.get('tag')) for s in sorted(c['items'], key=lambda s: s.get('slot', 0))))
                        sigs.setdefault(sig, []).append(c)
                    lines.append('| Block | Positions | Fixed contents |')
                    lines.append('|---|---|---|')
                    for sig, cl in sigs.items():
                        pos = ', '.join('(%d, %d, %d)' % tuple(c['pos']) for c in cl[:6]) + (f' ... +{len(cl) - 6}' if len(cl) > 6 else '')
                        lines.append(f"| `{sig[0]}` x{len(cl)} | {pos} | {md_escape(contents_text(cl[0]['items']))} |")
                    lines.append('')

    section_by_mod('## A. Chests, barrels and other loot-like containers', loot_like, False)
    section_by_mod('## B. Functional containers (furnaces, dispensers, brewing stands, campfires, hoppers...)', functional, True)
    lines.append('## C. Hand-placed containers the pack already converted to tables (`chests/fixed/*`)')
    lines.append('')
    lines.append('`tools/lootr_fixed_containers.py` replaced each container\'s fixed `Items` with a `LootTable` whose pools drop exactly those stacks '
                 '(one pool per stack, always rolled once), so Lootr can give each player their own copy. They are still deterministic: '
                 'the contents below are always the result. Identical contents share one table. Edit the table JSON in `kubejs/data/<ns>/loot_tables/chests/fixed/` to change them.')
    lines.append('')
    by_ns = defaultdict(list)
    for tbl in sorted(converted):
        by_ns[tbl.split(':')[0]].append(tbl)
    for ns, tbls in sorted(by_ns.items()):
        lines.append(f"### `{ns}` ({ported.get(ns, 'kubejs/data')})")
        lines.append('')
        lines.append('| Table | Containers | Templates | Contents (always) |')
        lines.append('|---|---:|---|---|')
        for tbl in tbls:
            t = D.tables.get(tbl, {})
            stacks = []
            for p in t.get('pools', []):
                for e in p.get('entries', []):
                    cnt, lo, hi, notes = count_info(e.get('functions'))
                    st = f"`{G.norm_id(e.get('name', '?'))}` x{cnt}"
                    if notes:
                        st += ' [' + '; '.join(notes) + ']'
                    stacks.append(st)
            tpls = Counter(t_ for t_, _ in converted[tbl])
            shown = ', '.join(f'`{a}`' + (f' x{n}' if n > 1 else '') for a, n in sorted(tpls.items())[:3])
            if len(tpls) > 3:
                shown += f' ... +{len(tpls) - 3} more'
            lines.append(f"| `{tbl}` | {len(converted[tbl])} | {shown} | {md_escape(', '.join(stacks) or '*(empty)*')} |")
        lines.append('')
    (OUT / 'handplaced.md').write_text('\n'.join(lines) + '\n')
    return len(rows), len(loot_like), len(functional), sum(len(v) for v in converted.values())


def render_by_structure(D):
    """structure -> loot tables its containers roll (+ hand-placed container counts)."""
    tabs = defaultdict(Counter)    # structure -> table -> containers
    fixed = defaultdict(Counter)   # structure -> block -> hand-placed containers (no table)
    orphan_tabs, orphan_fixed = Counter(), Counter()
    for tid, v in D.templates.items():
        sids, guess = D.structures_for(tid)
        for c in v['containers']:
            loot = G.norm_id(c['loot']) if c['loot'] else None
            if loot is None and not c['items']:
                continue
            for sid in sids or [None]:
                if sid is None:
                    (orphan_tabs if loot else orphan_fixed)[loot or c['block']] += 1
                elif loot:
                    tabs[sid][loot] += 1
                else:
                    fixed[sid][c['block']] += 1
    lines = ['# Loot by structure', '',
             f'Generated by `python3 tools/loot_breakdown.py` on {date.today().isoformat()}. Back to the [index](README.md).', '',
             'For every structure that places a container with loot, the tables those containers roll and how many containers use each, '
             'plus containers that are hand-filled (no table). Resolved by walking each structure\'s jigsaw start pool down to its `size`, so a template shared by '
             'several structures appears under each. Spawn info is the pack\'s current setting (group, frequency, `DISABLED`), so you can skip structures that cannot generate. '
             'Table counts are containers inside one structure, not per generated structure (a village has many houses).', '']
    by_ns = defaultdict(list)
    for sid in sorted(set(tabs) | set(fixed)):
        by_ns[sid.split(':')[0]].append(sid)
    for ns, sids in sorted(by_ns.items()):
        lines.append(f'## `{ns}`')
        lines.append('')
        lines.append('| Structure | Spawn | Loot tables (containers) | Hand-placed containers |')
        lines.append('|---|---|---|---|')
        for sid in sids:
            st = structure_text(D, sid)
            name, _, rest = st.partition(' (')
            conv = sum(n for t, n in tabs[sid].items() if ':chests/fixed/' in t)
            tl = ', '.join(f'[`{t}`](tables/{t.split(":", 1)[0]}.md#{slug(t)}) x{n}' if t in D.scope else f'`{t}` x{n}'
                           for t, n in sorted(tabs[sid].items()) if ':chests/fixed/' not in t)
            fx = ', '.join(f'`{b}` x{n}' for b, n in fixed[sid].most_common())
            if conv:
                fx = (fx + ', ' if fx else '') + f'{conv} converted to `chests/fixed/*` tables ([part C](handplaced.md))'
            lines.append(f"| {name} | {md_escape(rest.rstrip(')'))} | {tl or '-'} | {fx or '-'} |")
        lines.append('')
    lines.append('## Containers in templates no structure start pool reaches')
    lines.append('')
    lines.append('Placed by features or code the scan cannot follow, or by templates that are unused as installed.')
    lines.append('')
    lines.append('| Table or block | Containers |')
    lines.append('|---|---:|')
    for k, n in sorted((orphan_tabs + orphan_fixed).items()):
        lines.append(f'| `{k}` | {n} |')
    lines.append('')
    lines.append('## Vanilla tables set in code (no template to scan)')
    lines.append('')
    lines.append('| Table | Where vanilla uses it |')
    lines.append('|---|---|')
    for t, w in sorted(VANILLA_CODE_TABLES.items()):
        lines.append(f'| [`{t}`](tables/minecraft.md#{slug(t)}) | {w} |')
    lines.append('')
    (OUT / 'by_structure.md').write_text('\n'.join(lines) + '\n')


# -------------------------------------------------------------------------------------------- readme


def render_readme(D, summary, hp):
    cats = Counter()
    for tid in D.scope:
        cats['container' if tid in D.placed else ('sub' if (tid in D.parents or tid in D.glm_parents) else 'code')] += 1
    lines = ['# Loot catalog: every container loot table and every hand-placed container', '',
             f'Generated by `python3 tools/loot_breakdown.py` on {date.today().isoformat()} (re-run after mod updates or a fresh `/dumploot`). '
             'Read-only reference for building replacement loot tables; nothing here is applied by the game.', '',
             '## What is in here', '',
             f'- [handplaced.md](handplaced.md): {hp[0]} containers filled by hand in structure templates ({hp[1]} chest-like, {hp[2]} functional), '
             f'plus {hp[3]} converted to `chests/fixed/*` tables. No loot table exists for these.',
             '- [by_structure.md](by_structure.md): the same data turned around, one row per structure: which tables its containers roll, how many containers, '
             'whether the structure can spawn at all in this pack, and its hand-placed containers. Start here to work through structures one at a time.',
             f'- `tables/<namespace>.md`: {len(D.scope)} container loot tables in {len(summary)} namespaces '
             f'({cats["container"]} placed directly by a structure template, {cats["sub"]} pulled into other tables, {cats["code"]} reached by code or loot modifiers). '
             'Block drops, mob drops and gameplay tables (fishing, gifts, bartering) are not containers and are not listed.', '',
             '## How to read a table entry', '',
             '- **Count** is the stack size an entry gives (`set_count`): `1-3` is uniform min-max, `+n` adds to an earlier count, `1` when there is no `set_count`.',
             '- **Weight** and **Chance per roll** are the entry\'s weight and its share of the pool\'s total weight. Vanilla drops entries whose conditions '
             'fail from the draw, so conditional entries change the real shares; the percentage is nominal.',
             '- **Rolls** are the number of draws per pool each time the container is opened (a range is uniform). A pool draws entries independently, with replacement, '
             'so the same entry can come up twice. `bonus_rolls` only matter with luck.',
             '- **Sub-table** entries (`-> table x`) roll another table in place; the sub-table is listed in its own right (linked).',
             '- **Tag** entries pick one member of the tag (or all, with `expand`).',
             '- Entries marked *added in code by a mod* are not in the JSON: a mod appends them when the table loads (Quark, Buzzier Bees, Supplementaries, Sophisticated Backpacks...). '
             'Weights of those come from the `/dumploot` export and are approximate.',
             '- Global loot modifiers (Armor Trims, Farmer\'s Delight, Everything Copper...) add items **after** the roll; they are listed above each table, not in it.',
             '', '## Dead references: containers that roll a table that does not exist', '',
             'These containers always open empty (the game logs a missing table). Fix the id in the template, or add the table.', '',
             '| Missing table | Containers | Template | Structure |', '|---|---:|---|---|']
    dead = [(t, p) for t, p in sorted(D.placed.items()) if t not in D.tables]
    for t, p in dead:
        for tp, n in sorted(p['templates'].items()):
            ss, _ = D.structures_for(tp)
            lines.append(f"| `{t}` | {n} | `{tp}` | {', '.join(f'`{x}`' for x in ss[:2]) or 'unresolved'} |")
    lines += ['', '## What this does not show', '',
             '- The pack\'s own pre-roll rules (`loot_config.json`, `07_rules.js`: PneumaticCraft and Lightman\'s coins banned, chalk x0.5, golden food x2, packed food) rewrite weights at load; '
             'tables here are the mods\' originals. See [loot.md](../loot.md) section 3.8.',
             '- Lootr: each player rolls their own copy at first open; the tables are the same.',
             '- Containers whose table is chosen in code (vanilla mineshafts, strongholds, YUNG\'s data markers...) show their table under "Placed by" with the path where it was found.',
             '', '## Index by namespace', '',
             '| Namespace | Tables | Containers | Source |', '|---|---:|---:|---|']
    for ns, tids in summary:
        srcs = sorted({short_jar(D.table_origin.get(t, '?')) for t in tids})
        placed = sum(1 for t in tids if t in D.placed)
        lines.append(f"| [`{ns}`](tables/{ns}.md) | {len(tids)} | {placed} | {', '.join(srcs)[:90]} |")
    lines.append('')
    (OUT / 'README.md').write_text('\n'.join(lines) + '\n')


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    D = Data()
    summary = render_tables(D)
    hp = render_handplaced(D)
    render_by_structure(D)
    render_readme(D, summary, hp)
    print(f'{len(D.scope)} tables in {len(summary)} namespaces; {hp[0]} hand-placed containers '
          f'({hp[1]} chest-like), {hp[3]} converted; output in {OUT.relative_to(ROOT)}')


if __name__ == '__main__':
    main()
