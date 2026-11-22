# SPDX-License-Identifier: GPL-3.0-only
"""Bundle plain plan rows into shared boots (r1121q, 2026-11-21).

qdrive.py boots once per (rows file, section, world group). Most sections need nothing but prep_srv.sh defaults, so
their rows can share one boot. This splits a saved plan (`qrun.py plan --json`, qrerun.py's rerun.plan.json) in two:

  bundle  rows of server-phase sections with no @@prep for their group, no @@modset / @modset, every selected row
          on boot 1, no failed-boot expectation; copied (steps verbatim, section defaults filled into the row) into
          run batch files OUTDIR/bundle_<G><n>.qb, one @@section per file, at most --chunk rows each, rows kept in
          plan order and a section's rows never split across files (captures and markers stay with their row chain)
  rest    every other plan row, written as OUTDIR/rest.plan.json for the drive as it is

and writes OUTDIR/bundle.plan.json (the bundle rows, `file` = the bundle file) so one drive runs both:
`qrun.py drive start RUN --plan-json OUTDIR/all.plan.json` (bundle steps first, then the rest, in plan order).
Row ids stay the queue's, so verdicts land on the queue rows.

    python3 tools/scratch_query/qbundle.py PLAN.json OUTDIR [--chunk 40] [--drop ID,ID] [--no-worldgen]

--drop leaves rows out entirely (listed in OUTDIR/dropped.txt); --no-worldgen drops worldgen / long rows the same way.
"""
import json
import re
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
INST = HERE.parent.parent
sys.path.insert(0, str(HERE))
import qrows  # noqa: E402

FAILED_BOOT = re.compile(r'^\s*bootreject\s+Done')
RELOAD = re.compile(r'^\s*>\s*/?reload\b')


def opts(argv):
	o = {'chunk': 40, 'drop': set(), 'noworld': False, 'args': []}
	it = iter(argv)
	for a in it:
		if a == '--chunk':
			o['chunk'] = int(next(it))
		elif a == '--drop':
			o['drop'] |= {x.strip() for x in next(it).split(',') if x.strip()}
		elif a == '--no-worldgen':
			o['noworld'] = True
		else:
			o['args'].append(a)
	if len(o['args']) != 2:
		raise SystemExit(__doc__)
	return o


def row_text(sec, row):
	m = qrows.effective(sec, row)
	out = ['## %s | %s' % (row['id'], row['title'])]
	for k in qrows.ROW_META:
		v = row['meta'].get(k) or (m.get(k) if k in ('jars', 'sources', 'tier') else '')
		if v and k not in ('modset', 'boot', 'phase'):
			out.append('@%s %s' % (k, v))
	return out + row['steps']


def main(argv):
	o = opts(argv)
	plan = json.loads(Path(o['args'][0]).read_text())
	outdir = Path(o['args'][1]).resolve()
	outdir.mkdir(parents=True, exist_ok=True)
	cache, items, dropped = {}, [], []
	for e in plan:
		if e['id'] in o['drop'] or (o['noworld'] and e.get('phase') in ('worldgen', 'long', 'longwait')):
			dropped.append(e['id'])
			continue
		f = e['file']
		if f not in cache:
			cache[f] = qrows.load(INST / f)
		sec = next(s for s in cache[f]['sections'] if s['key'] == e['section'])
		row = next(r for k, r in sec['items'] if k == 'row' and r['id'] == e['id'])
		# '@group same' continues the previous row's fixture: take that row's group
		group, prev = '', None
		for k, r in sec['items']:
			if k != 'row':
				continue
			g = (r['meta'].get('group') or 'A').strip()
			prev = prev if g.lower().startswith('same') else g[:1].upper()
			if r is row:
				group = prev or 'A'
				break
		m = qrows.effective(sec, row)
		plain = (e.get('phase') == 'server' and group in 'AB' and not m['modset']
				 and not qrows.prep_of(sec, group)['lines'] and not any(qrows.prep_of(sec, group, b)['lines'] for b in (2, 3, 4))
				 and int(row['meta'].get('boot') or 1) == 1 and not any(FAILED_BOOT.match(l) for l in row['steps']))
		items.append((e, sec, row, group, plain))
	# a (file, section, group) is bundled only when every selected row of it is plain (row chains stay together)
	mixed = {(x[0]['file'], x[0]['section'], x[3]) for x in items if not x[4]}
	bundle = [x[:4] for x in items if (x[0]['file'], x[0]['section'], x[3]) not in mixed]
	rest = [x[:4] for x in items if (x[0]['file'], x[0]['section'], x[3]) in mixed]
	# chunk per group; keep each (file, section) run of rows in one chunk
	files, bplan = [], []
	for g in 'AB':
		runs, cur, key = [], [], None
		for item in (x for x in bundle if x[3] == g):
			k = (item[0]['file'], item[0]['section'])
			if k != key and cur:
				runs.append(cur)
				cur = []
			cur.append(item)
			key = k
		if cur:
			runs.append(cur)
		# a /reload truncates the KubeJS log later rows read (qlint W kjs): sections with one go last
		runs.sort(key=lambda r: any(RELOAD.match(l) for x in r for l in x[2]['steps']))
		chunks, c = [], []
		for r in runs:
			if c and len(c) + len(r) > o['chunk']:
				chunks.append(c)
				c = []
			c += r
		if c:
			chunks.append(c)
		for n, c in enumerate(chunks, 1):
			path = outdir / ('bundle_%s%d.qb' % (g, n))
			srcs = sorted({x[0]['file'].split('/')[-1] for x in c})
			lines = ['@@section 1', '@@title bundle %s%d (run batch, not queue rows): %s' % (g, n, ' '.join(srcs)),
					 '@@date 2026-11-21', '@@phase server',
					 '@@text Run-only copy of plan rows that share one group %s boot (qbundle.py); the queue rows stay in rows/*.qb.' % g,
					 '@@table']
			for e, sec, row, _ in c:
				lines += [''] + row_text(sec, row)
				bplan.append(dict(e, file=str(path.relative_to(INST)), section=1))
			path.write_text('\n'.join(lines) + '\n')
			files.append((path, len(c)))
	restplan = [x[0] for x in rest]
	(outdir / 'bundle.plan.json').write_text(json.dumps(bplan, indent=1))
	(outdir / 'rest.plan.json').write_text(json.dumps(restplan, indent=1))
	(outdir / 'all.plan.json').write_text(json.dumps(bplan + restplan, indent=1))
	(outdir / 'dropped.txt').write_text(''.join(i + '\n' for i in dropped))
	for p, n in files:
		print('%s: %d rows' % (p.relative_to(INST), n))
	steps = len({(x['file'], x['section'], (x.get('group') or '')) for x in restplan})
	print('bundle %d rows in %d boots; rest %d rows (%d plan sections); dropped %d'
		  % (len(bplan), len(files), len(restplan), steps, len(dropped)))


if __name__ == '__main__':
	main(sys.argv[1:])
