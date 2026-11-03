#!/usr/bin/env python3
"""
Loot table census: every loot table in the pack, per namespace and category, where each one is
shipped from, what the game actually loaded (/dumploot), and how it maps onto the chest-loot
tuning catalog. Read-only: it never touches loot_config.json / loot_index.json.

Writes kubejs/data/creativeworldgen/kubejs/loot/loot_census.md (KubeJS only loads .js, so it's inert there).

Sources, in datapack priority order (lowest first, same as the game and gen_loot_config.py):
the vanilla -extra jar, mods/*.jar sorted by file name, kubejs/data. Optional extras:
  kubejs/exported/loot_tables/*.json   /dumploot export = what the server really loaded. Files
                                        older than the newest one by > 10 min are leftovers from
                                        removed mods (the dump never deletes a namespace file).
  kubejs/data/creativeworldgen/kubejs/loot/loot_index.json + loot_config.json   the tuning scope and catalog.
  Structure templates (audit_loot_containers.py's scan of every .nbt in mods/*.jar + vanilla): which
  tables containers really roll, from which structures. Takes ~30 s.

Chest scope reuses gen_loot_config.py's rule (type minecraft:chest, or an untyped / generic table
under a chests/ or chest/ path segment, plus everything those pull in), so both tools agree on
"chest loot". A scope table counts as a container root when nothing pulls it in, or when a structure
template places it directly (vanilla village tables are both: SimplyHouses borrows them).

Run: python3 tools/loot_census.py      (after mod updates, a fresh /dumploot, or gen_loot_config.py)
"""
import json
import re
import sys
from collections import Counter, OrderedDict, defaultdict
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from audit_loot_containers import scan as scan_templates  # noqa: E402
from gen_loot_config import (CHEST_PATH, CHEST_TYPE, CONFIG_PATH, EXPORT_DIR, INDEX_PATH, INSTANCE,  # noqa: E402
                             NON_CHEST_PREFIX, glm_targets, iter_data_sources, norm_id, norm_type,
                             table_refs, walk_entries)

OUT = INSTANCE / 'kubejs/data/creativeworldgen/kubejs/loot/loot_census.md'
STALE_SECONDS = 600

CATEGORIES = ['chest', 'chest_sub', 'block', 'entity', 'gameplay', 'config_driven', 'other']
CAT_LABEL = {
    'chest': 'chest', 'chest_sub': 'chest sub', 'block': 'block', 'entity': 'entity',
    'gameplay': 'gameplay', 'config_driven': 'code/config', 'other': 'other',
}


def is_chest_seed(tid, table):
    path = tid.split(':', 1)[1]
    ttype = norm_type(table.get('type'))
    if ttype == CHEST_TYPE:
        return True
    return ttype in (None, 'minecraft:generic') and bool(CHEST_PATH.search(path)) and not path.startswith(NON_CHEST_PREFIX)


def chest_scope(effective, code_pools, glms, placed=()):
    """(scope, roots) the way gen_loot_config.py builds them: seeds (chest-typed, chests/ folders, and
    every table a structure template places) + everything they pull in."""
    glm_edges = defaultdict(set)
    for _, data in glms.values():
        sub = data.get('lootTable') or data.get('loot_table') or data.get('table')
        if isinstance(sub, str):
            for t in glm_targets(data.get('conditions')):
                glm_edges[t].add(norm_id(sub))

    def children(tid):
        pools = list(effective.get(tid, {}).get('pools', [])) + code_pools.get(tid, [])
        return table_refs(pools) | glm_edges.get(tid, set())

    scope, todo = set(), [t for t, v in effective.items() if is_chest_seed(t, v) or t in placed]
    while todo:
        tid = todo.pop()
        if tid in scope or tid not in effective:
            continue
        scope.add(tid)
        todo += list(children(tid))
    has_parent = {c for tid in scope for c in children(tid) if c in scope}
    return scope, scope - has_parent


def category(tid, table, scope, roots, placed=()):
    path = tid.split(':', 1)[1]
    ttype = norm_type(table.get('type'))
    if tid in roots or tid in placed:
        return 'chest'
    if tid in scope:
        return 'chest_sub'
    if ttype == 'minecraft:block' or path.startswith('blocks/'):
        return 'block'
    if ttype == 'minecraft:entity' or path.startswith('entities/'):
        return 'entity'
    if ttype in ('minecraft:fishing', 'minecraft:gift', 'minecraft:barter', 'minecraft:advancement_reward') \
            or path.startswith('gameplay/'):
        return 'gameplay'
    if ttype == 'minecraft:empty':
        return 'config_driven'
    return 'other'


def layout(path):
    """Folder part of a table path, with the leaf folder kept (chests/village/x -> chests/village/)."""
    return path.rsplit('/', 1)[0] + '/' if '/' in path else '(top level)'


def load_raw():
    """{table id: [(source label, table json), ...]} in priority order (last one wins in game)."""
    shipped = defaultdict(list)
    glms = {}
    glm_active = set()
    for label, read, names in iter_data_sources():
        for n in names:
            m = re.match(r'data/([^/]+)/loot_tables/(.+)\.json$', n)
            if m:
                try:
                    shipped[f'{m[1]}:{m[2]}'].append((label, json.loads(read(n))))
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
                    glms[f'{m[1]}:{m[2]}'] = (label, data)
    return shipped, {k: v for k, v in glms.items() if k in glm_active}


def load_export():
    """(fresh {id: table}, [stale namespace files]) or (None, [])."""
    files = sorted(EXPORT_DIR.glob('*.json')) if EXPORT_DIR.exists() else []
    if not files:
        return None, []
    newest = max(f.stat().st_mtime for f in files)
    fresh, stale = {}, []
    for f in files:
        if newest - f.stat().st_mtime > STALE_SECONDS:
            stale.append(f.name)
            continue
        for path, table in json.loads(f.read_text()).items():
            fresh[f'{f.stem}:{path}'] = table
    return fresh, stale


def short_jar(label):
    return re.sub(r'\.jar$', '', label)


def md_table(header, rows, align=None):
    align = align or ['---'] * len(header)
    out = ['| ' + ' | '.join(header) + ' |', '|' + '|'.join(align) + '|']
    out += ['| ' + ' | '.join(str(c) for c in r) + ' |' for r in rows]
    return out


def main():
    shipped, glms = load_raw()
    effective = {tid: srcs[-1][1] for tid, srcs in shipped.items()}
    export, stale = load_export()
    index = json.loads(INDEX_PATH.read_text()) if INDEX_PATH.exists() else None
    config = json.loads(CONFIG_PATH.read_text()) if CONFIG_PATH.exists() else None

    # pools added in code (LootTableLoadEvent) = live pools past the JSON pool count
    code_pools = {}
    if export is not None:
        for tid, t in export.items():
            if tid in effective:
                extra = t.get('pools', [])[len(effective[tid].get('pools', [])):]
                if extra:
                    code_pools[tid] = extra

    # structure templates: table -> templates whose containers roll it
    placed = defaultdict(set)
    containers = defaultdict(Counter)   # table -> {template namespace: containers}
    for row in scan_templates():
        if row['loot']:
            placed[norm_id(row['loot'])].add(row['template'])
            containers[norm_id(row['loot'])][row['template'].split(':', 1)[0]] += 1

    scope, roots = chest_scope(effective, code_pools, glms, placed)
    index_scope = set(index['tables']) if index else set()
    roots |= {t for t in scope if t in placed}
    dead_refs = {t: tpls for t, tpls in placed.items() if t not in effective and (export is None or t not in export)}
    cat = {tid: category(tid, t, scope, roots, placed) for tid, t in effective.items()}

    # --------------------------------------------------------------------------- per namespace
    by_ns = defaultdict(Counter)
    ns_sources = defaultdict(Counter)
    for tid, srcs in shipped.items():
        ns = tid.split(':', 1)[0]
        by_ns[ns][cat[tid]] += 1
        ns_sources[ns][short_jar(srcs[-1][0])] += 1

    live_only = {}
    raw_not_live = []
    if export is not None:
        live_only = {tid: t for tid, t in export.items() if tid not in effective}
        raw_not_live = sorted(tid for tid in effective if tid not in export)
    live_only_ns = Counter(tid.split(':', 1)[0] for tid in live_only)

    # --------------------------------------------------------------------------- overrides
    overrides = OrderedDict()
    for tid in sorted(shipped):
        labels = [short_jar(l) for l, _ in shipped[tid]]
        if len(labels) > 1:
            overrides[tid] = labels

    # --------------------------------------------------------------------------- chest detail
    chest_ns = defaultdict(lambda: {'roots': 0, 'sub': 0, 'untyped': 0, 'layouts': Counter(), 'outside': 0,
                                    'code_pools': 0, 'unindexed': 0, 'placed': 0, 'users': Counter()})
    outside = sorted(t for t in effective if t in placed and t not in scope)
    for tid in sorted(scope | set(outside)):
        ns, path = tid.split(':', 1)
        d = chest_ns[ns]
        d['roots' if tid in roots or tid in placed else 'sub'] += 1
        if tid not in scope:
            d['outside'] += 1
        if tid in placed:
            d['placed'] += 1
            d['users'].update(containers[tid])
        d['layouts'][layout(path)] += 1
        if tid in roots and norm_type(effective[tid].get('type')) != CHEST_TYPE:
            d['untyped'] += 1
        if index and tid in scope and tid not in index_scope:
            d['unindexed'] += 1
    for tid in code_pools:
        if tid in scope:
            chest_ns[tid.split(':', 1)[0]]['code_pools'] += 1

    # who adds code pools: namespaces referenced by the extra pools
    code_pool_adders = defaultdict(set)
    for tid, pools in code_pools.items():
        refs = table_refs(pools) | {norm_id(e['name']) for p in pools for e in walk_entries(p.get('entries'))
                                    if 'name' in e}
        for r in refs:
            code_pool_adders[r.split(':', 1)[0]].add(tid)

    # items by owning mod in the chest catalog
    item_mods = defaultdict(lambda: {'items': 0, 'tables': set(), 'tuned': 0})
    patterns = []
    if config:
        for key, spec in config.get('patterns', {}).items():
            if key.startswith('@'):
                patterns.append((key, (lambda p: lambda i: i.startswith(p))(key[1:] + ':'), spec))
            elif key.startswith('/') and key.rfind('/') > 0:
                rx = re.compile(key[1:key.rfind('/')])
                patterns.append((key, (lambda r: lambda i: bool(r.search(i)))(rx), spec))

        def tuned(item, entry):
            if entry.get('remove') or entry.get('frequency', 1) != 1:
                return True
            for key, test, spec in patterns:
                if test(item) and item not in [norm_id(x) for x in spec.get('except', [])]:
                    return spec.get('remove') is True or spec.get('frequency', 1) != 1
            return False

        for item, entry in config['items'].items():
            d = item_mods[item.split(':', 1)[0]]
            d['items'] += 1
            d['tables'] |= set(entry.get('where', {}))
            if tuned(item, entry):
                d['tuned'] += 1

    # --------------------------------------------------------------------------- GLMs
    glm_rows = []
    for gid, (label, data) in sorted(glms.items()):
        targets = sorted(set(glm_targets(data.get('conditions'))))
        chest_targets = [t for t in targets if t in scope]
        if targets and len(chest_targets) == len(targets):
            kind = 'chest'
        elif chest_targets:
            kind = 'mixed'
        elif targets:
            kind = cat.get(targets[0], 'other') if targets[0] in cat else 'other'
        else:
            kind = 'no table-id condition'
        glm_rows.append((gid, norm_id(data.get('type', '?')), short_jar(label), kind, len(targets)))

    # --------------------------------------------------------------------------- write
    total = Counter()
    for c in by_ns.values():
        total.update(c)
    n_tables = sum(total.values())
    jars_with_tables = len({short_jar(s[-1][0]) for s in shipped.values()})

    L = ['# Loot table census', '',
         f'Generated by `python3 tools/loot_census.py` on {date.today().isoformat()}. Read-only report; '
         'the numbers change after mod updates. The writeup that explains them is '
         '[feature_writeup/loot.md](../../../feature_writeup/loot.md).', '']

    L += ['## Totals', '']
    rows = [
        ('loot table JSON files shipped (all sources, incl. overridden copies)', sum(len(s) for s in shipped.values())),
        ('distinct loot tables after datapack priority', n_tables),
        ('namespaces with at least one table', len(by_ns)),
        ('sources shipping tables (vanilla + jars + kubejs/data)', jars_with_tables),
        ('tables overridden by a higher-priority source', len(overrides)),
    ]
    rows += [(f'category: {CAT_LABEL[c]}', total[c]) for c in CATEGORIES]
    if export is not None:
        rows += [('live tables (/dumploot, fresh files only)', len(export)),
                 ('live-only tables (built in code, no JSON anywhere)', len(live_only)),
                 ('shipped but not live (see its section)', len(raw_not_live)),
                 ('tables with pools added in code (live pools > JSON pools)', len(code_pools)),
                 ('stale export files (removed mods)', len(stale))]
    rows += [('chest-loot scope now: tables (roots + sub-tables)', len(scope)),
             ('chest-loot scope now: roots (tables a container rolls)', len(roots))]
    if index:
        rows += [('container tables outside the tuning scope (see its section)', len(outside)),
                 ('loot_index.json scope (as of its last generation)', len(index_scope)),
                 ('chest tables missing from loot_index.json (re-run gen_loot_config.py)', len(scope - index_scope)),
                 ('chest catalog items (loot_config.json)', len(config['items']) if config else '?')]
    L += md_table(['what', 'count'], rows, ['---', '---:'])

    L += ['', '## Category rules', '',
          '- **chest**: what a container rolls: a root of the chest-loot scope, or any table a structure template '
          'container names (even outside the scope). Scope seeds are tables declared `minecraft:chest`, or '
          'untyped/generic under a `chests/` or `chest/` folder, plus everything they pull in (same rule as '
          '`gen_loot_config.py`).',
          '- **chest sub**: in the scope but pulled into another chest table (loot_table entry, code-added '
          'inject pool, or a sub-table GLM such as Farmer\'s Delight\'s). Tuned together with chests.',
          '- **block** / **entity**: declared type, or `blocks/` / `entities/` folder.',
          '- **gameplay**: fishing, gifts (cat / hero of the village), bartering, advancement rewards.',
          '- **code/config**: declared `minecraft:empty`; the owning mod fills it in code from its config '
          '(Lightman\'s coin tiers).',
          '- **other**: anything else (mod inject tables not reachable from a chest, mod-internal tables).', '']

    L += ['## Per namespace', '',
          'Every namespace with a loot table. "source" is the jar whose copy wins (`kubejs/data` and '
          'vanilla are labelled as such); a namespace can be shipped by several jars.', '']
    header = ['namespace', 'total'] + [CAT_LABEL[c] for c in CATEGORIES] + ['live-only', 'source']
    rows = []
    for ns in sorted(by_ns, key=lambda n: (-sum(by_ns[n].values()), n)):
        c = by_ns[ns]
        srcs = ', '.join(f'{s}' + (f' ({k})' if len(ns_sources[ns]) > 1 else '') for s, k in ns_sources[ns].most_common())
        rows.append([f'`{ns}`', sum(c.values())] + [c[k] or '' for k in CATEGORIES] + [live_only_ns[ns] or '', srcs])
    for ns in sorted(set(live_only_ns) - set(by_ns)):
        rows.append([f'`{ns}`', 0] + [''] * len(CATEGORIES) + [live_only_ns[ns], '(code only)'])
    L += md_table(header, rows, ['---', '---:'] + ['---:'] * len(CATEGORIES) + ['---:', '---'])

    L += ['', '## Chest loot by namespace', '',
          '"roots" = tables a container rolls; "sub" = tables only pulled into other chest tables. "outside" = '
          'roots a structure places that the tuning scope misses (see the section below). '
          '"untyped" = roots with no `minecraft:chest` type, caught by path (LootJS `LootType.CHEST` still sees them '
          'when a container rolls them). "code pools" = tables of this namespace that another mod appends a pool to '
          'in code. "unindexed" = in the scope now but not in loot_index.json yet. "placed" = roots a structure '
          'template container names directly; the rest are assigned in code or by processors (vanilla code-built '
          'structures, YUNG\'s) or unused. "placed by" = namespaces of the templates doing it, with container '
          'counts. "folders" is where the tables live under `data/<ns>/loot_tables/`.', '']
    rows = []
    for ns in sorted(chest_ns, key=lambda n: (-(chest_ns[n]['roots'] + chest_ns[n]['sub']), n)):
        d = chest_ns[ns]
        folders = ', '.join(f'`{f}` {k}' for f, k in d['layouts'].most_common(4))
        if len(d['layouts']) > 4:
            folders += f', +{len(d["layouts"]) - 4} more'
        users = ', '.join(f'{u} {k}' for u, k in d['users'].most_common(4))
        if len(d['users']) > 4:
            users += f', +{len(d["users"]) - 4} more'
        rows.append([f'`{ns}`', d['roots'] or '', d['sub'] or '', d['placed'] or '', d['outside'] or '',
                     d['untyped'] or '', d['code_pools'] or '', d['unindexed'] or '', users, folders])
    L += md_table(['namespace', 'roots', 'sub', 'placed', 'outside', 'untyped', 'code pools', 'unindexed', 'placed by',
                   'folders'], rows, ['---', '---:', '---:', '---:', '---:', '---:', '---:', '---:', '---', '---'])

    L += ['', '## Container tables outside the tuning scope', '',
          'Structure containers roll these, but they are neither declared `minecraft:chest` nor under a `chests/` '
          'folder, so `gen_loot_config.py` never sees them: no pre-roll rewrite, not in the catalog. Post-roll '
          'rules still reach them (LootJS types a roll by the container that asks, not by the table\'s declared type).', '']
    L += md_table(['table', 'declared type', 'containers'],
                  [[f'`{t}`', f'`{norm_type(effective[t].get("type"))}`' if effective[t].get('type') else '(none)',
                    sum(containers[t].values())] for t in outside], ['---', '---', '---:']) if outside else ['None.']

    shared = sorted(((t, containers[t]) for t in placed
                     if t in effective and set(containers[t]) - {t.split(':', 1)[0]}),
                    key=lambda kv: (-sum(kv[1].values()), kv[0]))
    L += ['', '## Tables other mods\' structures borrow', '',
          'Chest tables placed by templates outside their own namespace. Tuning one of these also changes '
          'the borrowing mods\' structures. Counts are template containers.', '']
    L += md_table(['table', 'containers', 'templates by namespace'],
                  [[f'`{t}`', sum(c.values()), ', '.join(f'{u} {k}' for u, k in c.most_common())] for t, c in shared],
                  ['---', '---:', '---']) if shared else ['None.']

    L += ['', '## Containers naming a table that does not exist', '',
          'These structure containers always generate empty (Lootr shows an empty container). Upstream '
          'bugs; a JSON at `kubejs/data/<ns>/loot_tables/<path>.json` would give them loot.', '']
    L += md_table(['missing table', 'containers', 'templates'],
                  [[f'`{t}`', sum(containers[t].values()), ', '.join(f'`{x}`' for x in sorted(tpls))]
                   for t, tpls in sorted(dead_refs.items())],
                  ['---', '---:', '---']) if dead_refs else ['None.']

    if config:
        L += ['', '## Whose items show up in chests', '',
              'Catalog items grouped by the mod that owns the item (not the table). "roots" = how many '
              'container tables can give at least one of them. "tuned" = items currently hit by an item '
              'edit or a pattern in loot_config.json.', '']
        rows = []
        for ns in sorted(item_mods, key=lambda n: (-item_mods[n]['items'], n)):
            d = item_mods[ns]
            rows.append([f'`{ns}`', d['items'], len(d['tables']), d['tuned'] or ''])
        L += md_table(['item namespace', 'items', 'roots', 'tuned'], rows, ['---', '---:', '---:', '---:'])

    L += ['', '## Overrides (same table id shipped by more than one source)', '']
    if overrides:
        by_cat = Counter(cat[t] for t in overrides)
        L += ['The last source wins (vanilla lowest, then mod jars, then `kubejs/data`). By category: ' +
              ', '.join(f'{CAT_LABEL[c]} {k}' for c, k in by_cat.most_common()) + '. Chest tables shipped by two '
              'mod jars: ' + str(sum(1 for t, s in overrides.items() if cat[t] in ('chest', 'chest_sub')
                                     and sum(1 for x in s if not x.startswith('client-')) > 1)) + '.', '']
        rows = [[f'`{tid}`', CAT_LABEL[cat[tid]], ' -> '.join(srcs)] for tid, srcs in overrides.items()]
        L += md_table(['table', 'category', 'sources (low -> high priority)'], rows)
    else:
        L += ['None.']

    L += ['', '## Global loot modifiers (active)', '',
          'Listed in a `forge:global_loot_modifiers` entries list. They run after the table rolls. '
          '"targets" = tables named in a `forge:loot_table_id` condition. "no table-id condition" ones pick what '
          'they touch by entity / block / tool conditions or in code (Lightman\'s coins_in_chests reads its config).', '']
    L += md_table(['modifier', 'type', 'jar', 'applies to', 'targets'],
                  [[f'`{g}`', f'`{t}`', j, k, n or ''] for g, t, j, k, n in glm_rows],
                  ['---', '---', '---', '---', '---:'])

    if export is not None:
        L += ['', '## Pools added in code', '',
              'Tables whose live copy has more pools than their JSON, grouped by the namespace the added pools '
              'point at (normally the mod that injects them; `minecraft` = the added pool holds vanilla items).', '']
        rows = [[f'`{ns}`', len(ts), ', '.join(f'`{t}`' for t in sorted(ts)[:6]) + (' ...' if len(ts) > 6 else '')]
                for ns, ts in sorted(code_pool_adders.items(), key=lambda kv: (-len(kv[1]), kv[0]))]
        L += md_table(['added pools point at', 'tables', 'examples'], rows, ['---', '---:', '---'])

        L += ['', '## Live-only tables (built in code)', '']
        if live_only:
            L += md_table(['namespace', 'tables', 'examples'],
                          [[f'`{ns}`', k, ', '.join(f'`{t}`' for t in sorted(x for x in live_only if x.startswith(ns + ':'))[:4])]
                           for ns, k in live_only_ns.most_common()], ['---', '---:', '---'])
        else:
            L += ['None.']

        L += ['', '## Shipped but not live', '',
              'JSON exists but the table is missing from the export. Usual causes: the table names a block or item '
              'that isn\'t registered (compat tables for mods not in the pack; the game logs "Couldn\'t parse loot '
              'table"), the mod did not load in the session that ran `/dumploot`, or `/dumploot` could not serialize '
              'it (its WARN lines in logs/kubejs/server.log).', '']
        if raw_not_live:
            nl = Counter((t.split(':', 1)[0], CAT_LABEL[cat[t]]) for t in raw_not_live)
            L += md_table(['namespace', 'category', 'tables'], [[f'`{ns}`', c, k] for (ns, c), k in nl.most_common()],
                          ['---', '---', '---:'])
            odd = [t for t in raw_not_live if cat[t] != 'block']
            if odd:
                L += ['', 'Non-block ones: ' + ', '.join(f'`{t}`' for t in odd) + '.']
        else:
            L += ['None.']

        L += ['', '## Stale export files', '',
              '`/dumploot` only writes namespaces that exist, so files for removed mods stay behind. They are '
              'ignored here; `gen_loot_config.py` reads them but only uses tables that still exist in a jar.', '']
        L += [f'- `kubejs/exported/loot_tables/{s}`' for s in stale] or ['None.']

    OUT.write_text('\n'.join(L) + '\n')
    print(f'{n_tables} tables in {len(by_ns)} namespaces from {jars_with_tables} sources; '
          f'chest {total["chest"]}, chest sub {total["chest_sub"]}, block {total["block"]}, entity {total["entity"]}; '
          f'{len(overrides)} overrides, {len(glm_rows)} active GLMs'
          + (f', {len(live_only)} live-only, {len(stale)} stale export files' if export is not None else '')
          + f' -> {OUT.relative_to(INSTANCE)}')


if __name__ == '__main__':
    main()
