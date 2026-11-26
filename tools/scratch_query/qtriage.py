#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Queue triage for a prioritized scratch run (owner request 2026-11-26).

Sorts every server-side queue row (client rows with --client) into work buckets, in owner priority order:
  bf_rewrite   last result BATCH-FAULT and the row text not reviewed since: a writer fixes the steps first
  bf_rerun     last result BATCH-FAULT but the row was rewritten (@reviewed) after it: run as is
  draft        no runnable steps (@draft): a writer adds steps
  never        runnable, never run
  rerun        last result not PASS (FAIL, INCONCLUSIVE, HARNESS, OPEN, LOOK, ...) and a jar of the row changed or the
               row was rewritten since that result (a fix may have landed); a non-PASS with nothing changed is
               'hold' (rerunning it would repeat the same result)
  history      @draft run-log entry placeholders (a past verdict kept as a row): never written or run
  stale        last PASS, but a source changed after the row was reviewed: rewrite, do not run as is
  outdated     a row whose @sources path is gone, or whose steps name ids missing from the dumps (qrowlint E):
               skipped until rewritten
  jar_changed  last PASS, a jar changed since: ranked by risk (see below)
Superseded and owner rows are left out.

Risk of a jar-changed row (higher runs first): installs of the row's jars after its last PASS (jar_archive/installed.csv,
up to 5 per jar, 15 in all, 2 points each) + commits touching its sources after the last PASS (up to 10, 3 points each)
+ days since the last PASS (1 point per 2 days, cap 20).

    qtriage.py [--client] [--json OUT] [--top N]    prints bucket counts and the top N jar-changed rows (default 30)
    qtriage.py --ids BUCKET[,BUCKET] [--top N] [--phase P,..|--not-phase P,..] [--min-risk R]
        prints the ids of those buckets, comma separated (jar_changed in risk order, cut to --top, default 30);
        --phase / --not-phase filter on the row phase (server, worldgen, long, longwait, priority)
"""
import collections
import json
import re
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent))
import qplan  # noqa: E402
import qrowlint  # noqa: E402

ROOT = qplan.ROOT


def installs():
	"""{mod key: [epoch, ...]} from jar_archive/installed.csv (installed / on-exit rows)."""
	import table_io
	out = collections.defaultdict(list)
	if not qplan.LEDGER.exists():
		return out
	for c in table_io.read(qplan.LEDGER)[2]:
		if len(c) >= 6 and c[5] in ('installed', 'on-exit'):
			key = re.sub(r'-\d.*$', '', c[1]).lower().replace('.jar', '')
			try:
				out[key].append(time.mktime(time.strptime(c[0], '%Y-%m-%dT%H:%M:%S')))
			except ValueError:
				pass
	return out


_commit_cache = {}


def commits_after(src, day):
	key = (src, day)
	if key not in _commit_cache:
		try:
			r = subprocess.run(['git', 'log', '--oneline', '--since', day + 'T23:59:59', '--', src], cwd=ROOT,
								capture_output=True, text=True, timeout=30)
			_commit_cache[key] = len([l for l in r.stdout.splitlines() if '[no-stale]' not in l])
		except (OSError, subprocess.SubprocessError):
			_commit_cache[key] = 0
	return _commit_cache[key]


def lint_errors(paths):
	"""{row id: [messages]} for qrowlint E findings (dead ids) in these files."""
	out = collections.defaultdict(list)
	for p in paths:
		try:
			for sev, _line, rid, msg in qrowlint.lint_file(p):
				if sev == 'E' and rid:
					out[rid].append(msg)
		except Exception as e:  # noqa: BLE001
			out['__lint__'].append('%s: %s' % (p, e))
	return out


def triage(client=False):
	rows = qplan.row_states()
	inst = installs()
	now = time.time()
	files = sorted({x['file'] for x in rows})
	dead = lint_errors(files)
	out = []
	for x in rows:
		m, r, last = x['meta'], x['row'], x['last']
		if (m.get('client') == 'true') != client:
			continue
		st = x['state']
		if st in ('superseded', 'owner', 'current'):
			continue
		e = {'id': r['id'], 'file': str(Path(x['file']).relative_to(ROOT)), 'section': x['sec']['key'],
			 'phase': m.get('phase') or 'server', 'tier': m.get('tier'), 'last': list(last[:3]) if last else None,
			 'why': x['why'], 'risk': 0}
		srcs = (m.get('sources') or '').split()
		missing = [s for s in srcs if not (ROOT / s.split('#')[0]).exists()]
		if st == 'draft':
			# run-log placeholders (a past run's verdict kept as a row) are history, not tests to write
			e['bucket'] = 'history' if (r['meta'].get('draft') or '').startswith('run-log entry') else 'draft'
		elif missing or dead.get(r['id']):
			e['bucket'] = 'outdated'
			e['why'] = '; '.join((['source gone: ' + ' '.join(missing)] if missing else []) + dead.get(r['id'], [])[:3])
		elif st == 'never':
			e['bucket'] = 'never'
		elif st == 'failed':
			rev = qrowlint.reviewed(x['sec'], r) or ''
			moved = []
			for j in qplan.jars_of(m):
				if any(t > qplan._day_end(last[0]) for t in inst.get(j, ())):
					moved.append(j)
			if last[2] == 'BATCH-FAULT':
				e['bucket'] = 'bf_rerun' if rev > last[0] else 'bf_rewrite'
			elif rev > last[0] or moved:
				e['bucket'] = 'rerun'
				e['why'] += '; changed since: ' + (', '.join(moved) or 'row rewritten ' + rev)
			else:
				e['bucket'] = 'hold'
		elif st == 'stale':
			e['bucket'] = 'stale'
		elif st in ('jar-changed', 'rewritten'):
			e['bucket'] = 'jar_changed' if st == 'jar-changed' else 'rerun'
			end = qplan._day_end(last[0])
			n_inst = min(15, sum(min(5, sum(1 for t in inst.get(j, ()) if t > end)) for j in qplan.jars_of(m)))
			n_com = min(10, sum(commits_after(s, last[0]) for s in srcs))
			age = min(20, int((now - end) / 86400 / 2))
			e['risk'] = 2 * n_inst + 3 * n_com + age
			e['risk_parts'] = {'installs': n_inst, 'commits': n_com, 'age_pts': age}
		else:
			e['bucket'] = st
		out.append(e)
	out.sort(key=lambda e: -e['risk'])
	return out


ORDER = ('bf_rewrite', 'draft', 'history', 'bf_rerun', 'never', 'rerun', 'jar_changed', 'stale', 'outdated', 'hold')


def main(argv):
	opt = lambda k, d=None: argv[argv.index(k) + 1] if k in argv else d  # noqa: E731
	rows = triage('--client' in argv)
	top = int(opt('--top', '30'))
	if '--json' in argv:
		Path(opt('--json')).write_text(json.dumps(rows, indent=1))
	if '--ids' in argv:
		want = opt('--ids').split(',')
		sel = [e for e in rows if e['bucket'] in want]
		if '--phase' in argv:
			ph = opt('--phase').split(',')
			sel = [e for e in sel if e['phase'] in ph]
		if '--not-phase' in argv:
			ph = opt('--not-phase').split(',')
			sel = [e for e in sel if e['phase'] not in ph]
		if '--min-risk' in argv:
			mr = int(opt('--min-risk'))
			sel = [e for e in sel if e['bucket'] != 'jar_changed' or e['risk'] >= mr]
		if 'jar_changed' in want:
			sel = [e for e in sel if e['bucket'] != 'jar_changed'] + [e for e in sel if e['bucket'] == 'jar_changed'][:top]
		print(','.join(e['id'] for e in sel))
		return 0
	c = collections.Counter(e['bucket'] for e in rows)
	for b in ORDER + tuple(k for k in c if k not in ORDER):
		if c.get(b):
			print('%-12s %5d' % (b, c[b]))
	print('top jar-changed by risk:')
	for e in [e for e in rows if e['bucket'] == 'jar_changed'][:top]:
		print('  %-16s risk %3d %s %s' % (e['id'], e['risk'], e['risk_parts'], e['file']))
	return 0


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
