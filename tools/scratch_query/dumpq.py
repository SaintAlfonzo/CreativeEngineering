#!/usr/bin/env python3
"""Checks values in a scratch server's kubejs/exported dumps (run from OUT/srv, e.g. a qrun.py `shell` step).

    dumpq.py food    ID key=value ... [ID key=value ...]   food/foods.json (after /dumpfood)
    dumpq.py gear    ID key=value ...                       gear/tools.json + gear/armor.json (after /dumpgear)
    dumpq.py stack   ID key=value ...                       stacks/stack_sizes.json (after /dumpstacks; unlisted = 64)
    dumpq.py compost ID=chance ...                          compost/compost.json "now" (after /dumpcompost); chance
                                                            "absent" = no compost row
    dumpq.py mod     MOD [MIN]                              items_by_mod/MOD.json item count (after /dumpitemsbymod)
    dumpq.py sset    SET +STRUCTURE ... -STRUCTURE ...      worldgen/structuregen.json (after /dumpstructuregen): the
                                                            structure set holds (+) or lacks (-) each structure; SET
                                                            "any" with -STRUCTURE = in no structure set at all
    dumpq.py tagdiff OTHER PREFIX ...                       tags/item_tags.json (after /dumptags item): size of every
                                                            item tag starting with a PREFIX here and in OTHER (another
                                                            exported dir, e.g. $INST/kubejs/exported); prints totals and
                                                            each differing tag with the ids only on one side (no FAIL:
                                                            judge by hand, absent mods drop entries)

A key=value pair checks row[key] (dotted keys reach into dicts); value "absent" = the row does not exist. Numbers
compare as floats (0.7 == 0.70), the rest as text (True, EAT). Prints "[dumpq] PASS|FAIL ..." per check; exit 1 on any
FAIL. Override the dump dir with DUMPQ_DIR (default kubejs/exported).
"""
import json
import os
import sys

BASE = os.environ.get('DUMPQ_DIR', 'kubejs/exported')
fails = 0


def load(rel):
    with open(os.path.join(BASE, rel)) as f:
        return json.load(f)


def same(got, want):
    try:
        return abs(float(got) - float(want)) < 1e-6
    except (TypeError, ValueError):
        return str(got) == want


def report(ok, what):
    global fails
    if not ok:
        fails += 1
    print('[dumpq] %s %s' % ('PASS' if ok else 'FAIL', what))


def dig(row, key):
    for k in key.split('.'):
        if not isinstance(row, dict) or k not in row:
            return '<missing>'
        row = row[k]
    return row


def rows_check(index, args, default=None):
    cur = None
    for a in args:
        if '=' not in a:
            cur = a
            continue
        k, v = a.split('=', 1)
        row = index.get(cur)
        if row is None and default is not None:
            row = default(cur)
        if v == 'absent':
            report(row is None, '%s absent' % cur)
            continue
        if row is None:
            report(False, '%s %s=%s (no row)' % (cur, k, v))
            continue
        got = dig(row, k)
        report(same(got, v), '%s %s=%s (got %s)' % (cur, k, v, got))


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    kind, args = argv[0], argv[1:]
    if kind == 'food':
        rows_check({r['id']: r for r in load('food/foods.json')['foods']}, args)
    elif kind == 'gear':
        idx = {r['id']: r for r in load('gear/tools.json')['items']}
        idx.update({r['id']: r for r in load('gear/armor.json')['items']})
        rows_check(idx, args)
    elif kind == 'stack':
        rows_check({r['id']: r for r in load('stacks/stack_sizes.json')['items']}, args,
                   default=lambda i: {'id': i, 'stackSize': 64})
    elif kind == 'compost':
        now = load('compost/compost.json')['now']
        for a in args:
            i, v = a.split('=', 1)
            row = now.get(i)
            if v == 'absent':
                report(row is None, '%s no compost row (got %s)' % (i, row and row.get('chance')))
            else:
                got = row.get('chance') if row else None
                report(row is not None and same(got, v), '%s compost %s (got %s)' % (i, v, got))
    elif kind == 'mod':
        try:
            n = len(load('items_by_mod/%s.json' % args[0])['items'])
        except FileNotFoundError:
            n = 0
        want = int(args[1]) if len(args) > 1 else 1
        report(n >= want, '%s items %d (at least %d)' % (args[0], n, want))
    elif kind == 'sset':
        sets = load('worldgen/structuregen.json')['structure_sets']
        def members(v):
            return {x.get('structure') for x in v.get('structures', [])}
        name = args[0]
        for a in args[1:]:
            sid = a[1:]
            if name == 'any':
                hit = sorted(k for k, v in sets.items() if sid in members(v))
                report(a[0] == '-' and not hit or a[0] == '+' and bool(hit), '%s in sets %s' % (sid, hit))
            else:
                v = sets.get(name)
                inside = v is not None and sid in members(v)
                report(inside == (a[0] == '+'), '%s %s %s' % (name, 'holds' if a[0] == '+' else 'lacks', sid))
    elif kind == 'tagdiff':
        here = load('tags/item_tags.json')
        with open(os.path.join(args[0], 'tags/item_tags.json')) as f:
            there = json.load(f)
        def ids(d, t):
            v = d.get(t, [])
            return set(v.get('items', v) if isinstance(v, dict) else v)
        for pre in args[1:]:
            tags = sorted(t for t in set(here) | set(there) if t.startswith(pre))
            nh = sum(len(ids(here, t)) for t in tags)
            nt = sum(len(ids(there, t)) for t in tags)
            print('[dumpq] tagdiff %s: %d tags, %d entries here, %d in %s' % (pre, len(tags), nh, nt, args[0]))
            for t in tags:
                a, b = ids(here, t), ids(there, t)
                if a != b:
                    print('[dumpq]   %s here %d other %d; only here %s; only other %s' % (
                        t, len(a), len(b), sorted(a - b)[:8], sorted(b - a)[:8]))
    else:
        print('unknown kind ' + kind)
        return 2
    return 1 if fails else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
