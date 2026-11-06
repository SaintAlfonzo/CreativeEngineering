#!/usr/bin/env python3
"""Move-diff harness for Creative Delight member moves (creativedelight framework #6, offline half).

Run `snapshot` before a move and `diff` after it. Nothing may disappear: a script move copies rows from the pack's
script folders into the CD trees, so every code line that existed before must still exist somewhere in the scanned
trees, and every data file's content must still exist at some path (its path may change, e.g. into creativedelight).

What it compares:
  lines  every code line of the scanned scripts, comments and whitespace stripped, as a multiset. Lines that are only
         punctuation (`})`, `];`) are skipped. A line that is gone everywhere is reported LOST; the wrapper lines a CD
         file adds (gates, `var i = global.item`) show up as ADDED and are fine.
  data   every data file (json, mcfunction, nbt) under kubejs/data/ and CD's data trees, by content hash. A content
         that is gone is LOST; a content found at another path is MOVED.

Dump half (framework #6, 2026-11-02): `dump-snapshot` / `dump-diff` read the game dumps in kubejs/exported/ (refresh
them with /dumpall and the client /cedumpnames before each side of the move):
  registry  every id of every registry in display_names.json (block, item, fluid, effect, entity, ...); a block's state
            property names too (an old world needs them unchanged)
  recipes   recipes/all.csv ids with their type. A lost id is allowed only when `creativedelight:<same path>` exists with
            the same type (the namespace change of Q24 / R2); per-type counts are printed when they drop
  tags      every entry of every item, block, fluid and entity type tag (tags/*_tags.json)
  loot      every loot table id (loot_tables/<namespace>.json keys)
`--ns a,b` limits both sides to those namespaces (recipes: the id's namespace or the recipe's mod column; tags: the tag's
namespace or an entry's). Script order is not checked: read the moved rows for removals that run later.

    python3 tools/cd_move_diff.py snapshot [--out FILE]
    python3 tools/cd_move_diff.py diff [--snapshot FILE] [--verbose]     exit 1 when anything is LOST
    python3 tools/cd_move_diff.py dump-snapshot [--out FILE] [--exported DIR] [--ns a,b]
    python3 tools/cd_move_diff.py dump-diff [--snapshot FILE] [--exported DIR] [--ns a,b]   exit 1 when anything is LOST
"""
import csv
import argparse
import hashlib
import json
import re
import sys
from collections import Counter
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
DEFAULT_SNAPSHOT = INSTANCE / 'tools/creative_delight_test/move_diff_snapshot.json'
DEFAULT_DUMP_SNAPSHOT = INSTANCE / 'tools/creative_delight_test/move_diff_dump_snapshot.json'
DEFAULT_EXPORTED = INSTANCE / 'kubejs/exported'
ALLOWED_NS = 'creativedelight'
TAG_FILES = ['item_tags.json', 'block_tags.json', 'fluid_tags.json', 'entity_type_tags.json']

SCRIPT_ROOTS = [
    'kubejs/server_scripts',
    'kubejs/startup_scripts',
    'kubejs/client_scripts',
    'kubejs/data/creativedelight/kubejs',
    'kubejs/assets/creativedelight/kubejs',
    'development/creativeDelight/src/main/resources/kubejs_startup',
    'development/creativeCommon/src/main/resources/kubejs_startup',
]
DATA_ROOTS = ['kubejs/data']
DATA_SUFFIXES = {'.json', '.mcfunction', '.nbt'}
SKIP_PARTS = {'_cdtest', 'exported', 'archive'}
# Dicts are not moved by member rows and dominate the line count.
SKIP_FILES = {'item_dict.js', 'food_dict.js', 'block_dict.js', 'fluid_dict.js'}


def skipped(p):
    return bool(SKIP_PARTS & set(p.parts)) or p.name in SKIP_FILES


def strip_comments(src):
    out, i, n = [], 0, len(src)
    quote = None
    while i < n:
        c = src[i]
        if quote:
            out.append(c)
            if c == '\\' and i + 1 < n:
                out.append(src[i + 1])
                i += 2
                continue
            if c == quote:
                quote = None
            i += 1
            continue
        if c in '\'"`':
            quote = c
            out.append(c)
        elif src.startswith('//', i):
            j = src.find('\n', i)
            i = n if j < 0 else j
            continue
        elif src.startswith('/*', i):
            j = src.find('*/', i + 2)
            out.append('\n' * src.count('\n', i, n if j < 0 else j))
            i = n if j < 0 else j + 2
            continue
        else:
            out.append(c)
        i += 1
    return ''.join(out)


def norm_line(line):
    line = re.sub(r'\s+', '', line).rstrip(',;').lstrip(';')
    return line if re.search(r'[A-Za-z0-9]', line) else None


def scan_lines():
    lines = Counter()
    where = {}
    for root in SCRIPT_ROOTS:
        base = INSTANCE / root
        if not base.is_dir():
            continue
        for p in sorted(base.rglob('*.js')):
            rel = p.relative_to(INSTANCE)
            if skipped(rel):
                continue
            for raw in strip_comments(p.read_text(errors='replace')).splitlines():
                key = norm_line(raw)
                if key:
                    lines[key] += 1
                    where.setdefault(key, str(rel))
    return lines, where


def scan_data():
    data = {}
    for root in DATA_ROOTS:
        base = INSTANCE / root
        for p in sorted(base.rglob('*')):
            rel = p.relative_to(INSTANCE)
            if not p.is_file() or p.suffix not in DATA_SUFFIXES or skipped(rel):
                continue
            # kubejs/data/<ns>/kubejs/ holds scripts and their json inputs: those are scripts, not datapack files
            if len(rel.parts) > 3 and rel.parts[3] == 'kubejs':
                continue
            raw = p.read_bytes()
            if p.suffix == '.json':
                try:
                    raw = json.dumps(json.loads(raw), sort_keys=True).encode()
                except ValueError:
                    pass
            data.setdefault(hashlib.sha1(raw).hexdigest(), []).append(str(rel))
    return data


def snapshot(out):
    lines, where = scan_lines()
    snap = {'lines': dict(lines), 'where': where, 'data': scan_data()}
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(snap, indent=0, sort_keys=True))
    print(f'snapshot: {sum(lines.values())} lines, {len(snap["data"])} data contents -> {out.relative_to(INSTANCE)}')


def diff(snap_path, verbose):
    snap = json.loads(snap_path.read_text())
    before, where = Counter(snap['lines']), snap['where']
    after, where_now = scan_lines()
    lost = before - after
    added = after - before
    data_before, data_after = snap['data'], scan_data()

    problems = 0
    for key, n in sorted(lost.items(), key=lambda kv: where.get(kv[0], '')):
        print(f'LOST line x{n} (was in {where.get(key, "?")}): {key[:160]}')
        problems += 1
    for h, paths in sorted(data_before.items(), key=lambda kv: kv[1][0]):
        now = data_after.get(h)
        if not now:
            print(f'LOST data: {", ".join(paths)}')
            problems += 1
        elif sorted(now) != sorted(paths):
            print(f'MOVED data: {", ".join(paths)} -> {", ".join(now)}')
    for h, paths in data_after.items():
        if h not in data_before:
            print(f'ADDED data: {", ".join(paths)}')
    print(f'{sum(added.values())} lines added (wrappers and new rows)')
    if verbose:
        for key, n in sorted(added.items(), key=lambda kv: where_now.get(kv[0], '')):
            print(f'  ADDED x{n} ({where_now.get(key, "?")}): {key[:160]}')
    print('move diff: ' + ('FAIL, %d lost' % problems if problems else 'clean'))
    return 1 if problems else 0


def _ns(rl):
    return rl.lstrip('#').split(':', 1)[0] if ':' in rl else 'minecraft'


def scan_dumps(exported, nss=None):
    """The dump half: registry ids, recipe ids by type, tag entries, loot table ids from kubejs/exported/."""
    keep = (lambda ns: True) if not nss else (lambda ns: ns in nss)
    out = {'registry': {}, 'block_props': {}, 'recipes': {}, 'tags': {}, 'loot': []}
    names = exported / 'display_names.json'
    if names.is_file():
        for reg, rows in json.loads(names.read_text())['entries'].items():
            ids = sorted(r['id'] for r in rows if keep(_ns(r['id'])))
            out['registry'][reg] = ids
            if reg == 'block':
                out['block_props'] = {r['id']: sorted(r.get('props') or []) for r in rows if keep(_ns(r['id']))}
    else:
        print(f'warning: {names} missing (client /cedumpnames), registry not compared')
    allcsv = exported / 'recipes/all.csv'
    if allcsv.is_file():
        with allcsv.open(newline='') as f:
            for row in csv.DictReader(f):
                if keep(_ns(row['id'])) or keep(row.get('mod') or ''):
                    out['recipes'][row['id']] = row['type']
    else:
        print(f'warning: {allcsv} missing (/dumprecipes), recipes not compared')
    for name in TAG_FILES:
        tf = exported / 'tags' / name
        if not tf.is_file():
            print(f'warning: {tf} missing (/dumptags), those tags not compared')
            continue
        kind = name[:-len('_tags.json')]
        for tag, entries in json.loads(tf.read_text()).items():
            ents = sorted(e if isinstance(e, str) else json.dumps(e, sort_keys=True) for e in entries)
            if not keep(_ns(tag)):
                ents = [e for e in ents if keep(_ns(e))]
                if not ents:
                    continue
            out['tags'][f'{kind}:{tag}'] = ents
    lootdir = exported / 'loot_tables'
    if lootdir.is_dir():
        for lf in sorted(lootdir.glob('*.json')):
            if lf.name.startswith('_') or not keep(lf.stem):
                continue
            out['loot'].extend(f'{lf.stem}:{k}' for k in json.loads(lf.read_text()))
    else:
        print(f'warning: {lootdir} missing (/dumploot), loot not compared')
    return out


def dump_snapshot(out, exported, nss):
    snap = scan_dumps(exported, nss)
    snap['ns'] = sorted(nss) if nss else None
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(snap, sort_keys=True))
    print('dump snapshot: %d registry ids, %d recipes, %d tags, %d loot tables -> %s' % (
        sum(len(v) for v in snap['registry'].values()), len(snap['recipes']), len(snap['tags']), len(snap['loot']),
        out.relative_to(INSTANCE) if out.is_relative_to(INSTANCE) else out))


def dump_diff(snap_path, exported, nss, verbose):
    before = json.loads(snap_path.read_text())
    if nss is None and before.get('ns'):
        nss = set(before['ns'])
    after = scan_dumps(exported, nss)
    problems = 0

    def lost(kind, what):
        nonlocal problems
        problems += 1
        print(f'LOST {kind}: {what}')

    for reg, ids in before['registry'].items():
        now = set(after['registry'].get(reg, []))
        if reg not in after['registry']:
            continue
        for i in ids:
            if i not in now:
                lost(reg, i)
    for b, props in before['block_props'].items():
        if b in after['block_props'] and after['block_props'][b] != props:
            lost('block state', f'{b} {props} -> {after["block_props"][b]}')
    renamed = 0
    if after['recipes'] or not before['recipes']:
        for rid, typ in before['recipes'].items():
            if rid in after['recipes']:
                continue
            alt = f'{ALLOWED_NS}:{rid.split(":", 1)[1]}'
            if after['recipes'].get(alt) == typ:
                renamed += 1
                if verbose:
                    print(f'  RENAMED recipe {rid} -> {alt}')
                continue
            lost('recipe', f'{rid} ({typ})')
        cb, ca = Counter(before['recipes'].values()), Counter(after['recipes'].values())
        for typ, n in sorted(cb.items()):
            if ca[typ] < n:
                print(f'COUNT {typ}: {n} -> {ca[typ]}')
    for tag, ents in before['tags'].items():
        now = after['tags'].get(tag)
        if now is None:
            if after['tags']:
                lost('tag', f'{tag} ({len(ents)} entries)')
            continue
        gone = sorted(set(ents) - set(now))
        if gone:
            lost('tag entries', f'{tag}: {", ".join(gone[:8])}{" ..." if len(gone) > 8 else ""}')
    if after['loot']:
        now = set(after['loot'])
        for lid in before['loot']:
            if lid not in now:
                lost('loot table', lid)
    print(f'{renamed} recipe ids moved to {ALLOWED_NS}:')
    print('dump diff: ' + ('FAIL, %d lost' % problems if problems else 'clean'))
    return 1 if problems else 0


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest='cmd', required=True)
    s = sub.add_parser('snapshot')
    s.add_argument('--out', type=Path, default=DEFAULT_SNAPSHOT)
    d = sub.add_parser('diff')
    d.add_argument('--snapshot', type=Path, default=DEFAULT_SNAPSHOT)
    d.add_argument('--verbose', action='store_true')
    ds = sub.add_parser('dump-snapshot')
    ds.add_argument('--out', type=Path, default=DEFAULT_DUMP_SNAPSHOT)
    dd = sub.add_parser('dump-diff')
    dd.add_argument('--snapshot', type=Path, default=DEFAULT_DUMP_SNAPSHOT)
    dd.add_argument('--verbose', action='store_true')
    for x in (ds, dd):
        x.add_argument('--exported', type=Path, default=DEFAULT_EXPORTED)
        x.add_argument('--ns', help='comma-separated namespaces to compare (default: all)')
    a = ap.parse_args()
    if a.cmd.startswith('dump-'):
        nss = set(a.ns.split(',')) if a.ns else None
        if a.cmd == 'dump-snapshot':
            dump_snapshot(a.out.resolve(), a.exported.resolve(), nss)
            return 0
        return dump_diff(a.snapshot.resolve(), a.exported.resolve(), nss, a.verbose)
    if a.cmd == 'snapshot':
        snapshot(a.out.resolve())
        return 0
    return diff(a.snapshot.resolve(), a.verbose)


if __name__ == '__main__':
    sys.exit(main())
