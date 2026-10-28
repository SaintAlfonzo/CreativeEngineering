#!/usr/bin/env python3
"""Compares two worldgen A/B worlds written by wgp_run.sh (perf/wgp/<level>/):
   wgp_compare.py perf/wgp/wgp_old perf/wgp/wgp_df
Prints the timeline numbers of both, the census totals, every structure whose built count differs, the false
negatives, and the fill.txt lines only one side has (SQ-WGP-03/04: both must be empty for an exact option)."""
import json, os, re, sys


def load(d):
    c = os.path.join(d, 'locate_census.json')
    census = json.load(open(c)) if os.path.exists(c) else None
    fill = set(open(os.path.join(d, 'fill.txt')).read().splitlines()) if os.path.exists(os.path.join(d, 'fill.txt')) else set()
    tl = open(os.path.join(d, 'timeline.txt')).read() if os.path.exists(os.path.join(d, 'timeline.txt')) else ''
    return census, fill, tl


def nums(tl):
    out = {}
    m = re.search(r'Done \(([\d.]+)s\)', tl); out['done'] = m and m.group(1)
    m = re.search(r'Total time: ([\d:]+)', tl); out['pregen'] = m and m.group(1)
    m = re.search(r'tps: Overall: Mean tick time: ([\d.]+)', tl); out['idle_mspt'] = m and m.group(1)
    ts = {k: int(v) for v, k in re.findall(r'^(\d+) (boot|census start|stop)$', tl, re.M)}
    m = re.search(r'^(\d+) done:', tl, re.M)
    if m and 'boot' in ts: out['boot_wall_s'] = int(m.group(1)) - ts['boot']
    return out


def main():
    a, b = sys.argv[1], sys.argv[2]
    (ca, fa, ta), (cb, fb, tb) = load(a), load(b)
    na, nb = os.path.basename(a.rstrip('/')), os.path.basename(b.rstrip('/'))
    print('%-14s %s' % (na, nums(ta)))
    print('%-14s %s' % (nb, nums(tb)))
    if ca and cb:
        for k in ('slotChunks', 'seconds', 'falseNegatives'):
            print('census %-15s %s %s' % (k, ca.get(k), cb.get(k)))
        sa = {s['structure']: s for s in ca['structures']}
        sb = {s['structure']: s for s in cb['structures']}
        tot = [sum(s.get('built', 0) for s in sa.values()), sum(s.get('built', 0) for s in sb.values())]
        print('built total', tot[0], tot[1])
        diff = [(k, sa.get(k, {}).get('built', 0), sb.get(k, {}).get('built', 0)) for k in sorted(set(sa) | set(sb))]
        diff = [d for d in diff if d[1] != d[2]]
        print('structures with a different built count: %d' % len(diff))
        for k, x, y in sorted(diff, key=lambda d: -abs(d[1] - d[2]))[:40]:
            print('  %-60s %6d %6d  %+d' % (k, x, y, y - x))
        for name, c in ((na, ca), (nb, cb)):
            fn = [(s['structure'], s.get('falseNegatives')) for s in c['structures'] if s.get('falseNegatives')]
            if fn: print('false negatives %s: %s' % (name, fn))
    only_a, only_b = sorted(fa - fb), sorted(fb - fa)
    print('fill lines: %d %d; only in %s: %d, only in %s: %d' % (len(fa), len(fb), na, len(only_a), nb, len(only_b)))
    for l in only_a[:10]: print('  - ' + l[:200])
    for l in only_b[:10]: print('  + ' + l[:200])
    for name, f in ((na, fa), (nb, fb)):
        print('cancelled %s: %d' % (name, sum(1 for l in f if l.rstrip().endswith('CANCELLED'))))


main()
