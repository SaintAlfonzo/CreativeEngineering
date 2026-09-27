#!/usr/bin/env python3
"""
Builds the chest-loot tuning catalog + index read by kubejs/server_scripts/loot/05_tuning.js:

  kubejs/server_scripts/loot/loot_config.json  - EDITABLE. Every item that can show up in chest
                                                 loot, with its "remove" / "frequency" knobs and a
                                                 read-only "where" view (chance per chest, %).
  kubejs/server_scripts/loot/loot_index.json   - GENERATED, don't edit. Which tables hold a direct
                                                 entry for each item (what the runtime rewrites
                                                 pre-roll), and each table's declared type.

Sources:
  - Raw loot table JSON, pristine (never the tuned result): the vanilla "-extra" jar, then every
    mods/*.jar, then kubejs/data/ (highest priority, same as in game). No chest table is shipped
    by two jars in this pack, so jar-vs-jar priority never matters here.
  - Global loot modifiers (data/<ns>/loot_modifiers/*.json, only the ones listed in a
    forge:global_loot_modifiers entries list): farmersdelight:add_loot_table-style ones become
    sub-table edges; add_item-style ones (an "item"/"addition" field) become injected-item
    occurrences; the rest are opaque code and only listed in the index report.
  - kubejs/exported/loot_tables/*.json (/dumploot), OPTIONAL but recommended: only used to find
    pools that mods add in code via LootTableLoadEvent (Botania / Supplementaries / Sophisticated
    Backpacks / MineColonies / CC inject pools). Those are the export's pools past the raw pool
    count, so the export being a post-tuning dump doesn't matter (tuning never drops a pool).
  - kubejs/exported/tags/item_tags.json (/dumptags), optional: members of expand:true tag entries.

"Chest loot" = every table declaring type minecraft:chest, every untyped table with a chests/ or
chest/ path segment (cloudstorage's balloon, Domestication Innovation's pet shop), plus everything
reachable from those through loot_table entries, code-added pools, and add_loot_table GLMs.
Untyped roots may be rolled by mod code into chests we can't name, so their items get "reach": "*".

Safe to re-run after mod updates or a fresh /dumploot: "remove" / "frequency" / any extra keys
you added to an item are preserved; items that vanished but still carry an edit are kept and
flagged "missing"; "settings" and "patterns" are never touched once they exist.

Run: python3 tools/gen_loot_config.py      then /reload in game.
"""
import glob
import json
import math
import re
import zipfile
from collections import OrderedDict, defaultdict
from datetime import date
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
MODS_DIR = INSTANCE / 'mods'
KUBEJS_DATA = INSTANCE / 'kubejs/data'
EXPORT_DIR = INSTANCE / 'kubejs/exported/loot_tables'
TAGS_EXPORT = INSTANCE / 'kubejs/exported/tags/item_tags.json'
OUT_DIR = INSTANCE / 'kubejs/server_scripts/loot'
CONFIG_PATH = OUT_DIR / 'loot_config.json'
INDEX_PATH = OUT_DIR / 'loot_index.json'

# Same lookup as gen_structure_config.py: the Forge "-extra" jar is the one with loose data/ jsons.
_vanilla_jar_candidates = glob.glob(
    str(Path.home() / '.local/share/atlauncher/libraries/net/minecraft/client/1.19.2*/*-extra.jar')
)
VANILLA_JAR = Path(_vanilla_jar_candidates[0]) if _vanilla_jar_candidates else None

CHEST_TYPE = 'minecraft:chest'
CHEST_PATH = re.compile(r'(^|/)chests?/')
NON_CHEST_PREFIX = ('blocks/', 'entities/', 'gameplay/')

DEFAULT_SETTINGS = OrderedDict([
    ('enabled', True),
    ('slack', 'redistribute'),
    ('log_tables', False),
])

# Seeded on first generation only. Migrated from the old post-roll-only 10_removals.js.
DEFAULT_PATTERNS = OrderedDict([
    ('@pneumaticcraft', OrderedDict([('remove', True)])),
])

README = OrderedDict([
    ('what', 'Chest loot tuning. Every item that can appear in chest loot is listed under "items". '
             'Set "remove": true to take it out of every chest, or "frequency": 0.5 to make it show up '
             'half as often wherever it appears. Then /reload in game. Nothing else to run.'),
    ('items', OrderedDict([
        ('remove', 'true = never appears in any chest loot.'),
        ('frequency', '0..1, multiplies how often the item appears per chest-loot roll, in every table. '
                      '1 = unchanged, 0.25 = a quarter as often, 0 = same as remove. Values above 1 are '
                      'clamped to 1 (use 30_redistribute.js to add or boost loot).'),
        ('chests', 'Read-only. How many loot tables (roots: the ones containers actually use) can give this item.'),
        ('where', 'Read-only. Chance (%) that one chest from that table contains at least one of the item, '
                  'BEFORE tuning, assuming no Luck. Includes sub-tables and mod-injected loot.'),
        ('missing', 'Set by the generator when an item you edited no longer shows up in any chest table.'),
    ])),
    ('patterns', 'Bulk rules keyed "@modid" (every item of a mod) or "/regex/" (matched against the item id). '
                 'Value: {"remove": true} or {"frequency": 0.5}, optional "except": ["ns:id", ...]. '
                 'An item whose own entry is edited (remove true or frequency != 1) ignores patterns. '
                 'Tag patterns ("#ns:tag") are not supported: tags are not bound yet while loot tables load.'),
    ('settings', OrderedDict([
        ('enabled', 'false = the whole tuning system does nothing.'),
        ('slack', '"redistribute" (default): the share an item loses goes to the other entries of the same pool, '
                  'so chests stay as full as before (like deleting the entry in a datapack). "empty": the lost '
                  'share becomes an empty roll, every other item keeps its exact odds, chests get a bit emptier.'),
        ('log_tables', 'true = log one line per rewritten table to logs/kubejs/server.log.'),
    ])),
    ('how', 'Where the item is a normal entry in a loot table JSON (almost always), its weight is rewritten '
            'before the roll. Items that mods inject in code (global loot modifiers, tag entries, pools added '
            'in code) are thinned or removed after the roll by LootJS instead: removed stacks leave an empty slot there. '
            'Full details: kubejs/server_scripts/loot/README.md.'),
    ('regenerate', 'python3 tools/gen_loot_config.py (after mod updates / a fresh /dumploot). Keeps your edits.'),
])


def norm_id(s):
    s = str(s)
    return s if ':' in s else 'minecraft:' + s


def norm_type(t):
    return norm_id(t) if t else None


# ---------------------------------------------------------------------------------------- loading

def iter_data_sources():
    """(label, name->bytes reader, [names]) in datapack priority order, lowest first."""
    if VANILLA_JAR and VANILLA_JAR.exists():
        z = zipfile.ZipFile(VANILLA_JAR)
        yield VANILLA_JAR.name, z.read, z.namelist()
    else:
        print('WARNING: vanilla -extra jar not found; vanilla loot tables will be missing')
    for jar in sorted(MODS_DIR.glob('*.jar')):
        try:
            z = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        yield jar.name, z.read, z.namelist()
    if KUBEJS_DATA.exists():
        files = [p for p in KUBEJS_DATA.rglob('*.json')]
        names = ['data/' + p.relative_to(KUBEJS_DATA).as_posix() for p in files]
        yield 'kubejs/data', (lambda n: (INSTANCE / 'kubejs' / n).read_bytes()), names


def load_sources():
    tables = {}
    glm_files, glm_active = {}, set()
    for label, read, names in iter_data_sources():
        for n in names:
            m = re.match(r'data/([^/]+)/loot_tables/(.+)\.json$', n)
            if m:
                try:
                    tables[f'{m[1]}:{m[2]}'] = json.loads(read(n))
                except Exception as err:
                    print(f'WARNING: {label} {n}: {err}')
                continue
            m = re.match(r'data/([^/]+)/loot_modifiers/(.+)\.json$', n)
            if m:
                try:
                    data = json.loads(read(n))
                except Exception:
                    continue
                if m[1] == 'forge' and m[2] == 'global_loot_modifiers':
                    if data.get('replace'):
                        glm_active.clear()
                    glm_active.update(norm_id(e) for e in data.get('entries', []))
                else:
                    glm_files[f'{m[1]}:{m[2]}'] = (label, data)
    return tables, {k: v for k, v in glm_files.items() if k in glm_active}


def load_export():
    if not EXPORT_DIR.exists():
        return None
    out = {}
    for f in EXPORT_DIR.glob('*.json'):
        for path, table in json.loads(f.read_text()).items():
            out[f'{f.stem}:{path}'] = table
    return out or None


def load_tags():
    if not TAGS_EXPORT.exists():
        return {}
    return {norm_id(k): [norm_id(m) for m in v] for k, v in json.loads(TAGS_EXPORT.read_text()).items()}


# ------------------------------------------------------------------------------------- GLM parsing

def glm_targets(cond_list):
    """Loot table ids a GLM is restricted to (forge:loot_table_id, also inside alternatives)."""
    out = []
    for c in cond_list or []:
        kind = norm_id(c.get('condition', ''))
        if kind == 'forge:loot_table_id' and 'loot_table_id' in c:
            out.append(norm_id(c['loot_table_id']))
        elif kind in ('minecraft:alternative', 'minecraft:any_of'):
            out += glm_targets(c.get('terms'))
    return out


# GLM types whose own "chance" field is an integer percent rolled as
# nextIntBetweenInclusive(0, 100) <= chance (bytecode-checked: armortrims ChestLootModifier).
PERCENT_CHANCE_GLMS = {'armor_trims:chest_loot_modifier', 'armor_trims:entity_loot_modifier'}


def glm_chance(data):
    p = 1.0
    for c in data.get('conditions') or []:
        if norm_id(c.get('condition', '')) == 'minecraft:random_chance':
            p *= float(c.get('chance', 1))
    ch = data.get('chance')
    if isinstance(ch, (int, float)):
        if norm_id(data.get('type', '')) in PERCENT_CHANCE_GLMS:
            p *= (ch + 1) / 101.0
        else:
            p *= ch / 100.0 if ch > 1 else ch
    return max(0.0, min(1.0, p))


def glm_item(data):
    for key in ('item', 'addition'):
        v = data.get(key)
        if isinstance(v, str):
            return norm_id(v)
        if isinstance(v, dict):
            for k in ('id', 'item'):
                if isinstance(v.get(k), str):
                    return norm_id(v[k])
    return None


# ----------------------------------------------------------------------------------- table walking

def walk_entries(entries):
    for e in entries or []:
        yield e
        if 'children' in e:
            yield from walk_entries(e['children'])


def table_refs(pools):
    return {norm_id(e['name']) for p in pools for e in walk_entries(p.get('entries'))
            if norm_type(e.get('type')) == 'minecraft:loot_table' and 'name' in e}


def direct_items(pools):
    return {norm_id(e['name']) for p in pools for e in walk_entries(p.get('entries'))
            if norm_type(e.get('type')) == 'minecraft:item' and 'name' in e}


def rolls_dist(v):
    """Number provider -> [(int rolls, probability)], matching vanilla getInt rounding."""
    if isinstance(v, (int, float)):
        return [(int(round(v)), 1.0)]
    if not isinstance(v, dict):
        return [(1, 1.0)]
    t = norm_type(v.get('type')) or ('minecraft:uniform' if 'min' in v else 'minecraft:constant')
    if t == 'minecraft:constant':
        return rolls_dist(v.get('value', 1))
    if t == 'minecraft:uniform':
        lo, hi = mean_int(v.get('min', 0)), mean_int(v.get('max', 1))
        if hi < lo:
            return [(lo, 1.0)]
        return [(n, 1.0 / (hi - lo + 1)) for n in range(lo, hi + 1)]
    if t == 'minecraft:binomial':
        n, p = mean_int(v.get('n', 1)), float(v.get('p', 0.5)) if isinstance(v.get('p'), (int, float)) else 0.5
        return [(k, math.comb(n, k) * p ** k * (1 - p) ** (n - k)) for k in range(n + 1)]
    return [(1, 1.0)]


def mean_int(v):
    if isinstance(v, (int, float)):
        return int(round(v))
    return int(round(sum(n * p for n, p in rolls_dist(v))))


def cond_chance(conds):
    p = 1.0
    for c in conds or []:
        if norm_id(c.get('condition', '')) in ('minecraft:random_chance', 'minecraft:random_chance_with_looting'):
            p *= float(c.get('chance', 1))
    return p


class Analyzer:
    """P(at least one of item X) for one roll of a table, recursing through loot_table entries."""

    def __init__(self, tables, code_pools, tags):
        self.tables, self.code_pools, self.tags = tables, code_pools, tags
        self.memo, self.stack = {}, set()

    def pools_of(self, tid):
        return list(self.tables.get(tid, {}).get('pools', [])) + self.code_pools.get(tid, [])

    def presence(self, tid):
        if tid in self.memo:
            return self.memo[tid]
        if tid in self.stack:
            return {}
        self.stack.add(tid)
        none = defaultdict(lambda: 1.0)
        for pool in self.pools_of(tid):
            for item, q in self.pool_none(pool).items():
                none[item] *= q
        self.stack.discard(tid)
        self.memo[tid] = {k: 1.0 - v for k, v in none.items() if v < 1.0}
        return self.memo[tid]

    def candidates(self, entries, scale=1.0):
        """[(weight, {item: P(item | this candidate picked)})] - vanilla expands tags / flattens composites."""
        out = []
        for e in entries or []:
            t = norm_type(e.get('type')) or 'minecraft:item'
            w = float(e.get('weight', 1)) * cond_chance(e.get('conditions')) * scale
            if 'children' in e:
                out += self.candidates(e['children'], cond_chance(e.get('conditions')) * scale)
            elif t == 'minecraft:item':
                out.append((w, {norm_id(e['name']): 1.0}))
            elif t == 'minecraft:tag':
                members = self.tags.get(norm_id(e['name']), [])
                if e.get('expand'):
                    out += [(w, {m: 1.0}) for m in members]
                elif members:
                    out.append((w, {m: 1.0 for m in members}))
            elif t == 'minecraft:loot_table':
                out.append((w, self.presence(norm_id(e['name']))))
            else:
                out.append((w, {}))
        return out

    def pool_none(self, pool):
        cands = self.candidates(pool.get('entries'))
        total = sum(w for w, _ in cands)
        if total <= 0:
            return {}
        per_roll = defaultdict(float)
        for w, dist in cands:
            for item, p in dist.items():
                per_roll[item] += (w / total) * p
        gate = cond_chance(pool.get('conditions'))
        dist = rolls_dist(pool.get('rolls', 1))
        return {item: (1 - gate) + gate * sum(pr * (1 - p) ** n for n, pr in dist)
                for item, p in per_roll.items()}


def pct(p):
    return float(f'{p * 100:.3g}')


# ------------------------------------------------------------------------------------------ main

def main():
    tables, glms = load_sources()
    export = load_export()
    tags = load_tags()
    if export is None:
        print('WARNING: no /dumploot export - pools mods add in code (Botania, Supplementaries, '
              'Sophisticated Backpacks, MineColonies...) will be missing from the catalog')

    # pools added in code (LootTableLoadEvent) = export pools past the raw pool count
    code_pools = {}
    if export:
        for tid, raw in tables.items():
            if tid in export:
                extra = export[tid].get('pools', [])[len(raw.get('pools', [])):]
                if extra:
                    code_pools[tid] = extra

    # GLMs
    glm_edges = defaultdict(dict)     # target table -> {sub-table rolled into it: GLM chance}
    glm_items = defaultdict(list)     # target table -> [(item, chance, glm id)]
    opaque = []
    for gid, (label, data) in sorted(glms.items()):
        targets = glm_targets(data.get('conditions'))
        sub = data.get('lootTable') or data.get('loot_table') or data.get('table')
        item = glm_item(data)
        if isinstance(sub, str) and targets:
            for t in targets:
                glm_edges[t][norm_id(sub)] = glm_chance(data)
        elif item and targets:
            for t in targets:
                glm_items[t].append((item, glm_chance(data), gid))
        else:
            opaque.append(OrderedDict([('glm', gid), ('type', data.get('type')), ('jar', label),
                                       ('targets', sorted(set(targets)))]))

    # scope: seeds + everything reachable
    def children(tid):
        pools = list(tables.get(tid, {}).get('pools', [])) + code_pools.get(tid, [])
        return table_refs(pools) | set(glm_edges.get(tid, {}))

    seeds = set()
    for tid, t in tables.items():
        path = tid.split(':', 1)[1]
        ttype = norm_type(t.get('type'))
        if ttype == CHEST_TYPE:
            seeds.add(tid)
        elif ttype in (None, 'minecraft:generic') and CHEST_PATH.search(path) and not path.startswith(NON_CHEST_PREFIX):
            seeds.add(tid)
    scope, todo, missing_refs = set(), list(seeds), set()
    while todo:
        tid = todo.pop()
        if tid in scope:
            continue
        if tid not in tables:
            missing_refs.add(tid)
            continue
        scope.add(tid)
        todo += list(children(tid))

    parents = defaultdict(set)
    for tid in scope:
        for c in children(tid):
            if c in scope:
                parents[c].add(tid)
    roots = sorted(t for t in scope if not parents[t])
    orphans = sorted(t for t in roots if norm_type(tables[t].get('type')) != CHEST_TYPE)

    def ancestors_or_self(tid):
        seen, todo = set(), [tid]
        while todo:
            t = todo.pop()
            if t not in seen:
                seen.add(t)
                todo += list(parents[t])
        return seen

    # index: which tables hold a direct item entry (raw JSON only: that's what the runtime rewrites)
    edit = defaultdict(set)
    for tid in scope:
        for item in direct_items(tables[tid].get('pools', [])):
            edit[item].add(tid)

    index_items = OrderedDict()
    for item in sorted(edit):
        reach = set()
        for tid in edit[item]:
            reach |= ancestors_or_self(tid)
        star = any(t in orphans for t in reach)
        index_items[item] = OrderedDict([
            ('edit', sorted(edit[item])),
            ('reach', '*' if star else sorted(reach)),
        ])

    # catalog: chance per root table
    an = Analyzer(tables, code_pools, tags)
    where = defaultdict(dict)
    for root in roots:
        none = defaultdict(lambda: 1.0)
        for item, p in an.presence(root).items():
            none[item] *= 1 - p
        for sub, chance in glm_edges.get(root, {}).items():  # GLMs only fire for the table actually rolled
            for item, p in an.presence(sub).items():
                none[item] *= 1 - chance * p
        for item, p, _ in glm_items.get(root, ()):
            none[item] *= 1 - p
        for item, q in none.items():
            if q < 1.0:
                where[item][root] = 1 - q

    # merge with existing config
    old = {}
    if CONFIG_PATH.exists():
        old = json.loads(CONFIG_PATH.read_text(), object_pairs_hook=OrderedDict)
    old_items = old.get('items', {})

    items = OrderedDict()
    kept_missing = 0
    for item in sorted(set(where) | set(old_items)):
        prev = old_items.get(item, {})
        edited = prev.get('remove') is True or prev.get('frequency', 1) != 1
        if item not in where and not edited:
            continue
        entry = OrderedDict([('remove', prev.get('remove', False)), ('frequency', prev.get('frequency', 1))])
        spots = sorted(where.get(item, {}).items(), key=lambda kv: (-kv[1], kv[0]))
        entry['chests'] = len(spots)
        entry['where'] = OrderedDict((t, pct(p)) for t, p in spots)
        for k, v in prev.items():  # user-added keys survive
            if k not in entry and k != 'missing':
                entry[k] = v
        if item not in where:
            entry['missing'] = True
            kept_missing += 1
        items[item] = entry

    config = OrderedDict([
        ('_readme', README),
        ('settings', old.get('settings', DEFAULT_SETTINGS)),
        ('patterns', old.get('patterns', DEFAULT_PATTERNS)),
        ('items', items),
    ])
    index = OrderedDict([
        ('_about', 'Generated by tools/gen_loot_config.py on ' + date.today().isoformat() +
                   ' - do not edit. "edit": tables with a direct entry for the item (rewritten pre-roll). '
                   '"reach": tables whose rolls those edits already cover (post-roll thinning skips them); '
                   '"*" = also rolled by mod code into unknown chests, so post-roll thinning never applies.'),
        ('tables', OrderedDict((t, norm_type(tables[t].get('type'))) for t in sorted(scope))),
        ('items', index_items),
        ('report', OrderedDict([
            ('roots', len(roots)),
            ('orphan_roots', orphans),
            ('code_pool_tables', sorted(t for t in code_pools if t in scope)),
            ('glm_sub_tables', {t: sorted(s) for t, s in sorted(glm_edges.items()) if t in scope}),
            ('glm_items', {t: [f'{i} ({pct(p)}%, {g})' for i, p, g in v] for t, v in sorted(glm_items.items()) if t in scope}),
            # code GLMs we can't read items from - their items are only reachable post-roll
            ('opaque_glms', [g for g in opaque if set(g['targets']) & scope
                             or (not g['targets'] and 'chest' in g['glm'] + str(g['type']))]),
            ('missing_refs', sorted(missing_refs)),
        ])),
    ])

    CONFIG_PATH.write_text(json.dumps(config, indent=2) + '\n')
    INDEX_PATH.write_text(json.dumps(index, indent=1) + '\n')

    edited = sum(1 for e in items.values() if e['remove'] or e['frequency'] != 1)
    print(f'scope: {len(scope)} tables ({len(roots)} roots, {len(orphans)} code-rolled orphans, '
          f'{sum(1 for t in code_pools if t in scope)} with code-added pools)')
    print(f'glms: {len(glms)} active, {sum(len(v) for v in glm_edges.values())} sub-table edges, '
          f'{sum(len(v) for v in glm_items.values())} injected items, {len(index["report"]["opaque_glms"])} opaque chest GLMs')
    print(f'items: {len(items)} in catalog ({edited} edited, {kept_missing} kept as missing), '
          f'{len(index_items)} with direct table entries')
    if missing_refs:
        print(f'note: {len(missing_refs)} referenced tables do not exist: {sorted(missing_refs)[:5]}...')
    print(f'wrote {CONFIG_PATH.relative_to(INSTANCE)} ({CONFIG_PATH.stat().st_size // 1024} KB), '
          f'{INDEX_PATH.relative_to(INSTANCE)} ({INDEX_PATH.stat().st_size // 1024} KB)')


if __name__ == '__main__':
    main()
