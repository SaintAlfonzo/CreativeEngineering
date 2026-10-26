#!/usr/bin/env python3
"""Move-diff harness for Creative Delight member moves (creativedelight framework #6, offline half).

Run `snapshot` before a move and `diff` after it. Nothing may disappear: a script move copies rows from the pack's
script folders into the CD trees, so every code line that existed before must still exist somewhere in the scanned
trees, and every data file's content must still exist at some path (its path may change, e.g. into creative_delight).

What it compares:
  lines  every code line of the scanned scripts, comments and whitespace stripped, as a multiset. Lines that are only
         punctuation (`})`, `];`) are skipped. A line that is gone everywhere is reported LOST; the wrapper lines a CD
         file adds (gates, `var i = global.item`) show up as ADDED and are fine.
  data   every data file (json, mcfunction, nbt) under kubejs/data/ and CD's data trees, by content hash. A content
         that is gone is LOST; a content found at another path is MOVED.

Not covered (the in-game half, needs a dump): registry names, recipe ids and counts (`/dumprecipes`), tag contents
and loot table ids after a reload. Script order is not checked: read the moved rows for removals that run later.

    python3 tools/cd_move_diff.py snapshot [--out FILE]
    python3 tools/cd_move_diff.py diff [--snapshot FILE] [--verbose]     exit 1 when anything is LOST
"""
import argparse
import hashlib
import json
import re
import sys
from collections import Counter
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
DEFAULT_SNAPSHOT = INSTANCE / 'tools/creative_delight_test/move_diff_snapshot.json'

SCRIPT_ROOTS = [
    'kubejs/server_scripts',
    'kubejs/startup_scripts',
    'kubejs/client_scripts',
    'kubejs/data/creative_delight/kubejs',
    'kubejs/assets/creative_delight/kubejs',
    'development/creativeDelight/src/main/resources/kubejs_startup',
    'development/patcherMod/src/main/resources/kubejs_startup',
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


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest='cmd', required=True)
    s = sub.add_parser('snapshot')
    s.add_argument('--out', type=Path, default=DEFAULT_SNAPSHOT)
    d = sub.add_parser('diff')
    d.add_argument('--snapshot', type=Path, default=DEFAULT_SNAPSHOT)
    d.add_argument('--verbose', action='store_true')
    a = ap.parse_args()
    if a.cmd == 'snapshot':
        snapshot(a.out.resolve())
        return 0
    return diff(a.snapshot.resolve(), a.verbose)


if __name__ == '__main__':
    sys.exit(main())
