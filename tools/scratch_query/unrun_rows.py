#!/usr/bin/env python3
"""List scratch queue rows per section that no run doc / batch / judged file mentions.
Usage: python3 tools/scratch_query/unrun_rows.py [--ids]
Heuristic: a row counts as run when its ID appears in the queue Run log, a "Results" or PASS/FAIL prose line, feature_writeup/testing/scratch_run_*.md,
scratch_queue_run_*.md, any tools/scratch_query/batches/**/*.qb, or perf/runs/**/judged*.md."""
import re, sys, glob, pathlib
root = pathlib.Path(__file__).resolve().parents[2]
q = (root/'feature_writeup/testing/scratch_queue.md').read_text().splitlines()
seen = set()
srcs = glob.glob(str(root/'feature_writeup/testing/scratch_run_*.md')) + glob.glob(str(root/'feature_writeup/testing/scratch_queue_run_*.md')) \
     + glob.glob(str(root/'tools/scratch_query/batches/**/*.qb'), recursive=True) + glob.glob(str(root/'perf/runs/**/judged*.md'), recursive=True)
for f in srcs:
    seen |= set(re.findall(r'[SC]Q-[A-Z0-9]+-[0-9]+[a-z]?', open(f, errors='replace').read()))
rl = False
for ln in q:
    if ln.startswith('## Run log'): rl = True
    elif ln.startswith('## '): rl = False
    if rl or ln.startswith('Results') or re.search(r'\b(PASS|FAIL)\b', ln) and not ln.startswith('| '):
        seen |= set(re.findall(r'[SC]Q-[A-Z0-9]+-[0-9]+[a-z]?', ln))
sec = None; out = {}
runlog = False
for ln in q:
    if ln.startswith('## Run log'): runlog = True
    elif ln.startswith('## '): runlog = False
    if ln.startswith('#'): sec = ln.lstrip('#').strip()[:90]
    m = re.match(r'\| \**([SC]Q-[A-Z0-9]+-[0-9]+[a-z]?)\b', ln)
    if m and not runlog:
        rid = m.group(1)
        if rid not in seen and 'superseded' not in ln.lower():
            out.setdefault(sec, []).append(rid)
tot = 0
for s, ids in out.items():
    tot += len(ids); srv = sum(i.startswith('SQ') for i in ids)
    print(f'{len(ids):3d} ({srv:3d} srv) {s}' + (('  ' + ' '.join(ids)) if '--ids' in sys.argv else ''))
print('total', tot)
