#!/usr/bin/env python3
"""One-time normalization of off-legend TODO statuses (plan_process_overhaul.md P1.4 item 3, 2026-11-16).

Mapping (no judgment beyond it):
  Built...            -> Testing (In Progress when the old status text says FAIL or "not built")
  Installed..., Tested -> Testing (Built variants)
  Done / Complete     -> moved to the archive table (Status and Tests folded into Details)
  Open, Idea          -> Planned
  In progress         -> In Progress (case)
Extra text in an old status cell is kept at the start of the row's Details cell. Writes the changed-row list to
feature_writeup/status/normalise_2026-11-16.md. Usage: python3 tools/status_normalise_todo.py [--dry]
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TODO = ROOT / 'feature_writeup' / 'todo' / 'TODO.md'
OUT = ROOT / 'feature_writeup' / 'status' / 'normalise_2026-11-16.md'
LEGEND = ('Bug', 'Planned', 'Pinned', 'In Progress', 'Testing', 'Complete')
STATUS_HDR = '| Id | Change | Status | Tests |'


def split_row(line):
    return [c.strip() for c in re.split(r'(?<!\\)\|', line.strip())[1:-1]]


def new_status(old):
    o = old.strip()
    if o in LEGEND and o != 'Complete':
        return None
    low = o.lower()
    if low.startswith('built'):
        return 'In Progress' if ('FAIL' in o or 'not built' in low) else 'Testing'
    if low.startswith('installed') or low == 'tested':
        return 'Testing'
    if low in ('done', 'complete'):
        return 'ARCHIVE'
    if low in ('open', 'idea'):
        return 'Planned'
    if low == 'in progress':
        return 'In Progress'
    return '?'


def main(dry):
    lines = TODO.read_text().split('\n')
    arch = next(i for i, l in enumerate(lines) if l.strip() == '# archive')
    changed, moved, unknown = [], [], []
    in_status_table = False
    out = []
    for i, l in enumerate(lines):
        if i >= arch:
            out.append(l)
            continue
        if l.startswith('| Id |'):
            in_status_table = l.startswith(STATUS_HDR)
        elif not l.startswith('|'):
            in_status_table = False
        m = in_status_table and re.match(r'\| `[^`]+ #\d+`', l)
        if not m:
            out.append(l)
            continue
        cells = split_row(l)
        old = cells[2]
        ns = new_status(old)
        if ns is None:
            out.append(l)
            continue
        if ns == '?':
            unknown.append((cells[0], old))
            out.append(l)
            continue
        word = re.match(r'[A-Za-z]+(?: [a-z]+)?', old).group(0) if re.match(r'[A-Za-z]', old) else old
        extra = old if old.lower() not in ('built', 'done', 'complete', 'open', 'idea', 'tested', 'in progress') else ''
        if ns == 'ARCHIVE':
            # archive columns: Id | Change | Work | Details | Decisions | Writeup
            det = 'Status was %s, moved to the archive 2026-11-16; Tests: %s. %s' % (old, cells[3] or 'none', cells[5])
            moved.append('| %s | %s | %s | %s | %s | %s |' % (cells[0], cells[1], cells[4], det, cells[6], cells[7]))
            changed.append((cells[0], old, 'Complete (moved to the archive)'))
            continue
        cells[2] = ns
        if extra:
            cells[5] = '**Status was "%s" (normalized 2026-11-16).** %s' % (extra, cells[5])
        changed.append((cells[0], old, ns))
        out.append('| ' + ' | '.join(cells) + ' |')
    # append moved rows at the end of the archive table (the file ends with it)
    while out and out[-1] == '':
        out.pop()
    out.extend(moved)
    out.append('')
    if unknown:
        print('UNMAPPED (left as is):')
        for u in unknown:
            print('  %s  %r' % u)
    print('%d rows changed (%d moved to the archive)' % (len(changed), len(moved)))
    if dry:
        for c in changed:
            print('  %s | %s | %s' % c)
        return 0
    TODO.write_text('\n'.join(out))
    OUT.parent.mkdir(parents=True, exist_ok=True)
    rep = ['# TODO status normalization, 2026-11-16', '',
           'One-time pass by `tools/status_normalise_todo.py` (plan_process_overhaul.md P1.4 item 3). Mapping: "Built" to',
           'Testing (In Progress when the old text says FAIL or "not built"); "Installed ..." and "Tested" to Testing; "Done" /',
           '"Complete" outside the archive moved to the archive table (Status and Tests kept in Details); "Open" and "Idea" to',
           'Planned; "In progress" to In Progress. Extra text in an old status cell now starts the row\'s Details cell.',
           'Rows that already sat in the archive were not touched.', '',
           '%d rows changed.' % len(changed), '',
           '| Id | Old status | New status |', '|---|---|---|']
    for c in changed:
        rep.append('| %s | %s | %s |' % (c[0], c[1].replace('|', '/'), c[2]))
    if unknown:
        rep += ['', 'Left as is (no mapping):', ''] + ['- %s: %s' % u for u in unknown]
    OUT.write_text('\n'.join(rep) + '\n')
    return 0


if __name__ == '__main__':
    sys.exit(main('--dry' in sys.argv))
