#!/usr/bin/env python3
"""Scratch run planner (plan_process_overhaul.md P2.2, P2.3): which queue rows a run needs, from what changed.

Row state (every row of feature_writeup/testing/rows/*.qb, metadata after section defaults):
  current      last result PASS, every jar of the row unchanged since (the stamp the verdict file recorded, or, for
               results from before jar pinning, no install / rebuild of the jar after the PASS day), sources unchanged
  jar-changed  last PASS, but a jar of the row changed since
  stale        last PASS, jars unchanged, but a source changed after the row was reviewed (qrowlint.py): the row text
               is rewritten before it runs again
  rewritten    last PASS, jars and sources unchanged, but the row text was rewritten (@reviewed after the PASS day):
               it needs a rerun, the old PASS is history; low priority (owner ruling 2026-11-16), left out of the
               default plan, added by --rewritten or --all
  failed       last result not PASS (FAIL, BATCH-FAULT, CRASH, LOOK, ...)
  never        no result in any run doc or verdict file
  draft        no runnable steps yet (@draft); owner rows (@owner true) go to the playtest list instead
A PASSed row whose text was rewritten later (@reviewed after the PASS day) is rewritten, not stale: the PASS was for the
old text, but nothing it tests changed, so it waits for --rewritten. A source change after the review still makes it stale.

Jar change: the jar in mods/ (client_mods/ for client rows) has a different build stamp than the run recorded, or,
without a recorded stamp, its last jar_archive/installed.csv row (else its file time) is after the PASS day.

    qrun.py plan [--jars a,b] [--all] [--never] [--failed] [--rewritten] [--tier T1,T2] [--code MELT,..] [--client]
                 [--json]
        default: T1 rows of every changed own jar plus the T2 rows whose jars or sources changed (jar-changed, stale);
        rewritten rows are left out; --jars: treat these jars as changed (a planned rebuild); --never / --failed /
        --rewritten add those rows; --all: every runnable row, rewritten ones included (only when the owner asks).
        Prints one qrun command per rows file and section, the drafts that need steps first, the stale rows that need
        rewriting first, a count of the rewritten rows left out, and a time estimate.
    qrun.py plan --release        T3: T3 rows, T1 of every own jar, the owner playtest list (see playtest)
    qrun.py playtest [--out FILE] the owner playtest checklist: @owner rows and client rows nobody automates
    qrun.py states [--code C]     per rows file: counts by state
    qrun.py archive [--apply]     sections whose rows are all current (P2.5); --apply marks them @@archived, then
                                  status_index.py moves them to scratch_queue_archive.md with their verdict history;
                                  archived sections with a row no longer current lose @@archived (back to the queue)
    qrun.py never [--out FILE]    never-run rows per section (runnable / draft / client), for the owner (P2.5)
"""
import collections
import json
import os
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent))
import qrows  # noqa: E402
import qrowlint  # noqa: E402

ROOT = qrows.ROOT
LEDGER = ROOT / 'jar_archive/installed.csv'


def _day_end(day):
	try:
		return time.mktime(time.strptime(day[:10], '%Y-%m-%d')) + 86400
	except ValueError:
		return 0


def jar_state():
	"""{own jar key (lowercase mod id): {'id' (stamp, or sha1:<12> unstamped, as qpin.py pins), 'changed' (epoch),
    'name', 'side'}} for mods/ and client_mods/."""
	import qpin
	ledger = {}
	if LEDGER.exists():
		sys.path.insert(0, str(ROOT / 'tools'))
		import table_io
		for c in table_io.read(LEDGER)[2]:
			if len(c) >= 6 and c[5] in ('installed', 'on-exit'):
				try:
					ledger[c[1]] = time.mktime(time.strptime(c[0], '%Y-%m-%dT%H:%M:%S'))
				except ValueError:
					pass
	out = {}
	for folder, side in (('mods', 'server'), ('client_mods', 'client')):
		d = ROOT / folder
		for j in sorted(d.glob('*.jar')) if d.is_dir() else ():
			info = qpin.own_info(j)
			if not info:
				continue
			t = ledger.get(j.name) or j.stat().st_mtime
			prev = out.get(info[0])
			if not prev or t > prev['changed']:
				out[info[0]] = {'id': info[1], 'changed': t, 'name': j.name, 'side': side}
	return out


def history():
	"""row id -> newest (day, run, word, stamps or None)."""
	import status_index
	runs = status_index.read_runs()
	out = {}
	for rid, run in runs.items():
		for row, word in run['results'].items():
			cur = out.get(row)
			if not cur or run['date'] > cur[0]:
				out[row] = (run['date'], rid, word, None)
	# verdict files written since jar pinning (P2.4) carry the stamps of the jars the run used
	vroot = ROOT / 'perf/runs'
	for p in vroot.glob('*/**/verdicts/*.json'):
		try:
			d = json.loads(p.read_text())
		except (OSError, ValueError):
			continue
		if not d.get('stamps'):
			continue
		row = d.get('id') or p.stem
		day = (d.get('ended_at') or d.get('started') or '')[:10]
		cur = out.get(row)
		if not cur or day >= cur[0]:
			out[row] = (day, p.relative_to(vroot).parts[0], (d.get('verdict') or d.get('result') or '').upper(),
						d['stamps'])
	return out


def jars_of(meta):
	return [j.lower() for j in (meta.get('jars') or '').split()]


def row_states(changed_jars=()):
	"""[{file, code, sec, row, meta, state, why, last}]"""
	jars = jar_state()
	hist = history()
	forced = {j.lower() for j in changed_jars}
	out = []
	for p in qrows.all_files():
		f = qrows.load(p)
		for sec in f['sections']:
			for kind, r in sec['items']:
				if kind != 'row':
					continue
				m = qrows.effective(sec, r)
				last = hist.get(r['id'])
				x = {'file': p, 'code': f['code'], 'sec': sec, 'row': r, 'meta': m, 'last': last, 'why': ''}
				if m.get('superseded'):
					x['state'] = 'superseded'
				elif m.get('owner') == 'true':
					x['state'] = 'owner'
				elif not qrows.runnable(r):
					x['state'] = 'draft'
				elif not last:
					x['state'] = 'never'
				elif last[2] != 'PASS':
					x['state'], x['why'] = 'failed', '%s in %s (%s)' % (last[2], last[1], last[0])
				else:
					moved = []
					for j in jars_of(m):
						js = jars.get(j)
						if j in forced:
							moved.append(j + ' (--jars)')
						elif not js:
							continue
						elif last[3] is not None:
							if last[3].get(j) != js['id']:
								moved.append('%s %s -> %s' % (j, last[3].get(j), js['id']))
						elif js['changed'] > _day_end(last[0]):
							moved.append('%s installed %s' % (j, time.strftime('%Y-%m-%d', time.localtime(js['changed']))))
					stale, rewritten = [], ''
					day = qrowlint.reviewed(sec, r)
					for src in (m.get('sources') or '').split():
						hit = qrowlint.changed_after(src, max(day, last[0]))
						if hit:
							stale.append(hit[1])
					if day and day > last[0]:
						# the row text was rewritten (@reviewed) after its last PASS: that PASS was for the old text
						rewritten = 'row rewritten %s, after its last PASS (%s)' % (day, last[0])
					if moved:
						x['state'], x['why'] = 'jar-changed', ', '.join(moved)
					elif stale:
						x['state'], x['why'] = 'stale', 'source changed: ' + ', '.join(stale + ([rewritten] if rewritten else []))
					elif rewritten:
						x['state'], x['why'] = 'rewritten', rewritten
					else:
						x['state'] = 'current'
					x['stale'] = stale
				out.append(x)
	return out


def estimate(selected):
	"""(lo, hi) seconds for the selected rows (qlint's per-row estimate)."""
	import qlint
	import qrun
	by_file = collections.defaultdict(set)
	for x in selected:
		by_file[x['file']].add(x['row']['id'])
	lo = hi = 0.0
	for p, ids in by_file.items():
		for r in qrun.parse(str(p), errors=[]):
			if r['id'] in ids:
				a, b = qlint._est(r)
				lo += a
				hi += b
	return lo, hi


def plan(argv):
	opt = lambda k: argv[argv.index(k) + 1] if k in argv else None  # noqa: E731
	forced = (opt('--jars') or '').split(',') if '--jars' in argv else []
	forced = [j for j in forced if j]
	tiers = set((opt('--tier') or '').split(',')) - {''}
	codes = set((opt('--code') or '').split(',')) - {''}
	release = '--release' in argv
	states = row_states(forced)
	jars = jar_state()
	# jars that changed: forced, or any jar some PASSed row of it saw change
	changed = {j.lower() for j in forced}
	for x in states:
		if x['state'] == 'jar-changed':
			changed.update(j for j in jars_of(x['meta']) if any(w.startswith(j + ' ') for w in x['why'].split(', ')))
	sel, drafts, stale, owner = [], [], [], []
	for x in states:
		m, st = x['meta'], x['state']
		if codes and x['code'] not in codes:
			continue
		if tiers and m['tier'] not in tiers:
			continue
		if '--client' not in argv and m.get('client') == 'true' and not release:
			continue
		want = False
		if '--all' in argv:
			want = st not in ('owner',)
		elif release:
			want = m['tier'] in ('T1', 'T3')
		else:
			if m['tier'] == 'T1':
				want = bool(set(jars_of(m)) & changed) and st not in ('current', 'rewritten')
			elif st in ('jar-changed', 'stale'):
				want = True
			if '--never' in argv and (st == 'never' or st == 'draft' and not x['last']):
				want = True
			if '--failed' in argv and st == 'failed':
				want = True
			if '--rewritten' in argv and st == 'rewritten':
				want = True
		if st == 'owner':
			owner.append(x)
			continue
		if st == 'superseded':
			continue
		if not want:
			continue
		if st == 'draft':
			drafts.append(x)
		elif st == 'stale':
			stale.append(x)
			sel.append(x)
		else:
			sel.append(x)
	if '--json' in argv:
		print(json.dumps([{'id': x['row']['id'], 'file': os.path.relpath(x['file'], ROOT), 'section': x['sec']['key'],
						   'state': x['state'], 'why': x['why'], 'tier': x['meta']['tier'],
						   'client': x['meta'].get('client') == 'true'} for x in sel + drafts], indent=1))
		return 0
	print('changed jars: %s' % (', '.join(sorted(changed)) or 'none'))
	lo, hi = estimate(sel)
	print('planned: %d rows (%s), about %d to %d min; %d drafts need steps first; %d owner rows (playtest)' % (
		len(sel), ', '.join('%s %d' % kv for kv in sorted(collections.Counter(x['state'] for x in sel).items())),
		lo // 60, hi // 60, len(drafts), len(owner)))
	groups = collections.OrderedDict()
	for x in sel:
		k = (x['file'], x['sec']['key'], x['meta'].get('client') == 'true')
		groups.setdefault(k, []).append(x)
	for (p, key, client), xs in groups.items():
		rel = os.path.relpath(p, ROOT)
		ids = ','.join(x['row']['id'] for x in xs)
		grp = sorted({x['meta'].get('group', '').split(',')[0].strip() for x in xs})
		print('  %s section %d (%d rows, group %s, %s):' % (rel, key, len(xs), '/'.join(grp) or '-',
														 'client' if client else 'server'))
		print('    qrun.py %s OUT %s RES --rows %s' % ('client' if client else 'run', rel, ids))
	if stale:
		print('rewrite before running (sources changed after the row was reviewed; set @reviewed after):')
		for x in stale[:40]:
			print('  %s (%s): %s' % (x['row']['id'], x['file'].name, x['why']))
		if len(stale) > 40:
			print('  ... %d more (--json for all)' % (len(stale) - 40))
	if drafts:
		print('drafts (write the steps into the .qb once, then they run like any row):')
		for x in drafts[:40]:
			print('  %s (%s section %d)' % (x['row']['id'], x['file'].name, x['sec']['key']))
		if len(drafts) > 40:
			print('  ... %d more (--json for all)' % (len(drafts) - 40))
	left = [x for x in states if x['state'] == 'rewritten' and x not in sel and (not codes or x['code'] in codes)]
	if left:
		print('%d rewritten rows left out (passed before the rewrite; low priority): --rewritten adds them' % len(left))
	if release:
		print('owner playtest list: qrun.py playtest')
	return 0


def playtest(argv):
	"""Owner rows (@owner true) as a markdown checklist. Client rows are not on it: agents run them with their own
    server and client (owner ruling 2026-11-16)."""
	states = row_states()
	xs = [x for x in states if x['state'] == 'owner']
	lines = ['# Owner playtest checklist', '',
			 'Generated by `python3 tools/scratch_query/qrun.py playtest` on %s from feature_writeup/testing/rows/: '
			 'rows marked @owner (design feel only the owner judges). %d rows.' % (time.strftime('%Y-%m-%d'), len(xs)), '']
	by = collections.OrderedDict()
	for x in xs:
		by.setdefault(x['sec']['meta'].get('title', x['code']), []).append(x)
	for title, rows in by.items():
		lines += ['## ' + title, '']
		for x in rows:
			m = x['meta']
			lines.append('- [ ] **%s** %s. Pass: %s' % (x['row']['id'], m.get('commands', ''), m.get('pass', '')))
		lines.append('')
	text = '\n'.join(lines)
	out = argv[argv.index('--out') + 1] if '--out' in argv else None
	if out:
		Path(out).write_text(text)
		print('playtest: %d rows -> %s' % (len(xs), out))
	else:
		print(text)
	return 0


def states_cmd(argv):
	codes = set(argv[argv.index('--code') + 1].split(',')) if '--code' in argv else None
	per = collections.defaultdict(collections.Counter)
	for x in row_states():
		if codes and x['code'] not in codes:
			continue
		per[x['code']][x['state']] += 1
	cols = ('current', 'jar-changed', 'stale', 'rewritten', 'failed', 'never', 'draft', 'owner', 'superseded')
	print('%-10s ' % 'code' + ' '.join('%11s' % c for c in cols))
	tot = collections.Counter()
	for code in sorted(per):
		tot.update(per[code])
		print('%-10s ' % code + ' '.join('%11d' % per[code][c] for c in cols))
	print('%-10s ' % 'total' + ' '.join('%11d' % tot[c] for c in cols))
	return 0


def archive(argv):
	"""P2.5: sections whose rows (superseded ones aside) are all current get @@archived (with --apply)."""
	by = collections.OrderedDict()
	for x in row_states():
		by.setdefault((x['file'], x['sec']['key']), []).append(x)
	elig, back = [], []
	for (p, key), xs in by.items():
		live = [x for x in xs if x['state'] != 'superseded']
		cur = live and all(x['state'] == 'current' for x in live)
		if cur and not xs[0]['sec']['meta'].get('archived'):
			elig.append((p, key, len(live)))
		elif not cur and xs[0]['sec']['meta'].get('archived'):
			back.append((p, key, sum(1 for x in live if x['state'] != 'current')))
	for p, key, n in back:
		print('  back to the queue: %s section %d (%d rows no longer current)' % (os.path.relpath(p, ROOT), key, n))
	for p, key, n in elig:
		print('  %s section %d (%d rows current)' % (os.path.relpath(p, ROOT), key, n))
	print('archive: %d section(s) eligible%s' % (len(elig), '' if '--apply' in argv or not elig else
												 '; --apply marks them @@archived'))
	if '--apply' in argv and (elig or back):
		day = time.strftime('%Y-%m-%d')
		for p in sorted({e[0] for e in elig + back}):
			f = qrows.load(p)
			keys = {k for q, k, _ in elig if q == p}
			unkeys = {k for q, k, _ in back if q == p}
			for sec in f['sections']:
				if sec['key'] in keys:
					sec['meta']['archived'] = day
				if sec['key'] in unkeys:
					sec['meta'].pop('archived', None)
			hdr = [l for l in p.read_text().split('\n')[:3] if l.startswith('#')]
			qrows.write_qb(p, f['code'], f['sections'], hdr)
		print('marked; run python3 tools/status_index.py to move them to scratch_queue_archive.md')
	return 0


def never(argv):
	"""P2.5 item 3: never-run rows per section, runnable or draft, as a markdown list for the owner."""
	by = collections.OrderedDict()
	for x in row_states():
		if x['state'] in ('never', 'draft') and not (x['state'] == 'draft' and x['last']):
			by.setdefault((x['file'], x['sec']['key']), []).append(x)
	tot = collections.Counter()
	lines = ['| Rows file | Section | Never run, runnable | Never run, draft | Client | First rows |', '|---|---|---|---|---|---|']
	for (p, key), xs in by.items():
		c = collections.Counter(x['state'] for x in xs)
		cl = sum(1 for x in xs if x['meta'].get('client') == 'true')
		tot.update(c)
		tot['client'] += cl
		title = xs[0]['sec']['meta'].get('title', '')[:70].replace('|', '/')
		lines.append('| %s | %d: %s | %d | %d | %d | %s |' % (p.name, key, title, c['never'], c['draft'], cl,
															', '.join(x['row']['id'] for x in xs[:3])))
	lines.append('')
	lines.append('Total: %d never-run rows with steps, %d drafts (no steps yet), %d of them client rows.' % (
		tot['never'], tot['draft'], tot['client']))
	text = '\n'.join(lines)
	out = argv[argv.index('--out') + 1] if '--out' in argv else None
	if out:
		Path(out).write_text(text + '\n')
		print('never: %d sections -> %s' % (len(by), out))
	else:
		print(text)
	return 0


def main(argv):
	if not argv:
		print(__doc__)
		return 2
	cmd, rest = argv[0], argv[1:]
	if cmd == 'plan':
		return plan(rest)
	if cmd == 'playtest':
		return playtest(rest)
	if cmd == 'states':
		return states_cmd(rest)
	if cmd == 'archive':
		return archive(rest)
	if cmd == 'never':
		return never(rest)
	print(__doc__)
	return 2


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
