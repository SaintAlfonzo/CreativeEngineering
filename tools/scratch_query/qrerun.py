#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Rerun list -> drive plan (skill scratch-test, run default 11: fix a batch fault and rerun it in-run).

    qrerun.py LIST [--out PLAN.json] [--add ID ...]
        LIST: a text file with one row id per line (anything after the id is ignored, # comments allowed) or a CSV
        with an `id` column (and optional `kind`: only bf-fixed / prefixed-ran / rerun rows are taken when present).
        Writes a `qrun.py plan --json`-shaped list (id, file, section, phase, state, client) for every runnable server
        row named, in qrows phase order, for `qrun.py drive start RUN --plan-json PLAN.json`. Superseded, draft and
        client rows are listed and left out. Default PLAN: LIST with .plan.json.
    qrerun.py --add LIST ID [ID ...]   append ids to LIST (a text list), skipping ones already in it

The rerun list of a drive lives at perf/runs/RUN/rerun.txt; the runner runs it at a step boundary or at the end.
"""
import csv
import json
import os
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
INST = HERE.parent.parent
sys.path.insert(0, str(HERE))
import qrows  # noqa: E402

TAKE = {'bf-fixed', 'prefixed-ran', 'rerun'}


def read_ids(path):
	p = Path(path)
	if not p.exists():
		return []
	text = p.read_text()
	if p.suffix == '.csv':
		rows = list(csv.DictReader(text.splitlines()))
		if rows and 'kind' in rows[0]:
			rows = [r for r in rows if r['kind'] in TAKE]
		return [r['id'].split(' ')[0] for r in rows]
	out = []
	for line in text.splitlines():
		line = line.split('#', 1)[0].strip()
		if line:
			out.append(line.split()[0])
	return out


def index():
	idx = {}
	for p in qrows.all_files():
		f = qrows.load(p)
		for sec in f['sections']:
			for kind, r in sec['items']:
				if kind == 'row':
					idx.setdefault(r['id'], (p, sec, r, qrows.effective(sec, r)))
	return idx


def main(argv):
	if not argv:
		sys.exit(__doc__)
	if argv[0] == '--add':
		lst, new = argv[1], argv[2:]
		have = set(read_ids(lst))
		with open(lst, 'a') as f:
			for i in new:
				if i not in have:
					f.write(i + '\n')
					have.add(i)
		print('%s: %d ids' % (lst, len(have)))
		return 0
	lst = argv[0]
	out = argv[argv.index('--out') + 1] if '--out' in argv else str(Path(lst).with_suffix('.plan.json'))
	ids = read_ids(lst)
	idx = index()
	plan, left = [], []
	order = {p: i for i, p in enumerate(getattr(qrows, 'PHASES', ('server', 'longwait', 'worldgen', 'long')))}
	for rid in dict.fromkeys(ids):
		hit = idx.get(rid)
		if not hit:
			left.append((rid, 'not in any rows file'))
			continue
		p, sec, r, m = hit
		if m.get('superseded') or m.get('draft') or m.get('client') == 'true' or not qrows.runnable(r):
			left.append((rid, 'superseded' if m.get('superseded') else 'draft' if m.get('draft') else
						 'client' if m.get('client') == 'true' else 'no steps'))
			continue
		plan.append({'id': rid, 'file': os.path.relpath(p, INST), 'section': sec['key'],
					 'phase': m.get('phase') or 'server', 'state': 'failed', 'why': 'rerun after fix', 'tier':
					 m.get('tier') or 'T2', 'client': False})
	plan.sort(key=lambda x: (order.get(x['phase'], 9), x['file'], str(x['section'])))
	Path(out).write_text(json.dumps(plan, indent=1))
	for rid, why in left:
		print('left out %s: %s' % (rid, why))
	print('%s: %d rows in %d sections -> qrun.py drive start RUN --plan-json %s' % (
		out, len(plan), len({(x['file'], x['section']) for x in plan}), out))
	return 0


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
