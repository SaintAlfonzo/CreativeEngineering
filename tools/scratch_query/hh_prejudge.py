#!/usr/bin/env python3
"""Pre-judges LOOK rows of a results dir (run r1108hh) from the pass text the batch carried.
    hh_prejudge.py RES   prints each LOOK row with the checks it could not confirm; rows with no doubt print OK
Spans: "log matches `R`" / "log has no `R`" / "count `R` == N" / any other `R` as a plain expected span."""
import json, re, sys
res = sys.argv[1]
r = json.load(open(res + '/results.json'))
ok = 0
for k, v in sorted(r.items()):
    if v['result'] != 'LOOK': continue
    text = ' '.join(v.get('looks') or [])
    try: log = open(f'{res}/rows/{k}.log', errors='replace').read()
    except OSError: log = ''
    try: log += open(f'{res}/rows/{k}.shell.txt', errors='replace').read()
    except OSError: pass
    bad = []
    def has(p):
        try: return re.search(p, log, re.M) is not None
        except re.error: return p in log
    for m in re.finditer(r'(log has no|no|count|log matches)? ?`([^`]+)`( == (\d+))?', text):
        kind, p, _, n = m.groups()
        if kind == 'count' and n is not None:
            c = len(re.findall(p, log, re.M))
            if c != int(n): bad.append(f'count {p[:60]} = {c}, want {n}')
        elif kind in ('log has no', 'no'):
            if has(p): bad.append(f'present: {p[:70]}')
        elif not has(p): bad.append(f'missing: {p[:70]}')
    if bad: print(k, ' | '.join(bad)[:330])
    else: ok += 1
print('no doubt:', ok)
