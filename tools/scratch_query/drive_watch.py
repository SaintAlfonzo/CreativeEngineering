#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Hang poll for a `qrun.py drive` run (r1118, owner request 2026-11-18): exits with one reason line when the
orchestrator needs to act, so it can run as a background task instead of blind waits.

    python3 tools/scratch_query/drive_watch.py RUN [--until N] [--stall MIN] [--jstack] [--max MIN] [--every SEC]

Exit reasons (first line of stdout, then diagnostics):
  BATCH n/N        at least --until steps are finished (done / failed / incomplete / stopped): hand them to a judge
  FINISHED status  the drive is no longer running (done, incomplete, stopped)
  DRIVER-DEAD      state says running but the driver pid is gone (resume: qrun.py drive resume RUN)
  STALL min        the running step showed no interaction for --stall minutes (skill scratch-test B3 stall watch):
                   newest mtime of the step's srv/logs/latest.log, sNN_res/rows/*.log, drive/sNN_qrun.txt, drive.log
  ORPHAN-JVM       a game JVM is up while no step is running for over --stall minutes
  TIMEOUT          --max minutes passed with none of the above (a heartbeat; rerun the watcher)
Diagnostics on STALL: current row (qrun output tail), crash reports, game_jvms.sh, server log tail; with --jstack one
jstack of the scratch server pid is saved to drive/sNN_jstack_<time>.txt (skill: only on the second silent check).
Never kills anything.
"""
import argparse
import json
import os
import subprocess
import sys
import time
from pathlib import Path

INST = Path(__file__).resolve().parents[2]
FIN = ('done', 'failed', 'incomplete', 'stopped')


def jvms():
	r = subprocess.run(['bash', str(INST / 'tools/scratch_query/game_jvms.sh')], capture_output=True, text=True)
	return [l for l in r.stdout.splitlines() if l.strip()]


def tail(p, n):
	try:
		return '\n'.join(Path(p).read_text(errors='replace').splitlines()[-n:])
	except OSError:
		return '(none)'


def newest(paths):
	m = 0.0
	for p in paths:
		try:
			m = max(m, p.stat().st_mtime)
		except OSError:
			pass
	return m


def main():
	ap = argparse.ArgumentParser()
	ap.add_argument('run')
	ap.add_argument('--until', type=int, default=0, help='exit BATCH when this many steps are finished')
	ap.add_argument('--stall', type=float, default=10)
	ap.add_argument('--jstack', action='store_true')
	ap.add_argument('--max', type=float, default=590)
	ap.add_argument('--every', type=float, default=60)
	a = ap.parse_args()
	base = INST / 'perf/runs' / a.run
	drive = base / 'drive'
	start = time.time()
	idle_since = None
	while True:
		try:
			st = json.loads((drive / 'state.json').read_text())
		except (OSError, ValueError):
			time.sleep(a.every)
			continue
		steps = st['steps']
		fin = sum(1 for s in steps if s['status'] in FIN)
		if a.until and fin >= a.until:
			print('BATCH %d/%d' % (fin, len(steps)))
			return
		if st.get('status') != 'running':
			print('FINISHED %s %d/%d' % (st.get('status'), fin, len(steps)))
			return
		pid = st.get('pid')
		if pid and not os.path.exists('/proc/%d' % pid):
			print('DRIVER-DEAD pid %s, %d/%d steps finished' % (pid, fin, len(steps)))
			return
		run = [(i, s) for i, s in enumerate(steps) if s['status'] == 'running']
		now = time.time()
		if run:
			idle_since = None
			i, s = run[0]
			tag = 's%02d' % i
			out, res = base / tag, base / (tag + '_res')
			paths = [out / 'srv/logs/latest.log', drive / (tag + '_qrun.txt'), drive / 'drive.log']
			paths += list((res / 'rows').glob('*.log')) if (res / 'rows').is_dir() else []
			last = newest(paths)
			idle = (now - last) / 60 if last else 0
			if idle >= a.stall:
				print('STALL %.0f min, step %d %s section %s group %s' % (idle, i + 1, Path(s['file']).name,
																		 s['section'], s['group']))
				print('-- qrun tail:\n' + tail(drive / (tag + '_qrun.txt'), 4))
				cr = out / 'srv/crash-reports'
				print('-- crash reports: %s' % (sorted(x.name for x in cr.iterdir()) if cr.is_dir() else []))
				print('-- game JVMs: %s' % (jvms() or 'none'))
				print('-- server log tail:\n' + tail(out / 'srv/logs/latest.log', 5))
				if a.jstack:
					for l in jvms():
						p, cwd = l.split(None, 1)
						if str(out) in cwd:
							f = drive / ('%s_jstack_%s.txt' % (tag, time.strftime('%H%M')))
							with open(f, 'w') as fh:
								subprocess.run(['jstack', p], stdout=fh, stderr=subprocess.STDOUT, timeout=60)
							print('-- jstack: %s' % f)
				return
		else:
			if jvms():
				idle_since = idle_since or now
				if (now - idle_since) / 60 >= a.stall:
					print('ORPHAN-JVM no step running, JVMs: %s' % jvms())
					return
			else:
				idle_since = None
		if (now - start) / 60 >= a.max:
			print('TIMEOUT %.0f min, %d/%d steps finished' % (a.max, fin, len(steps)))
			return
		time.sleep(a.every)


if __name__ == '__main__':
	sys.exit(main())
