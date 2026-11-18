#!/usr/bin/env python3
"""Resumable scratch run driver (plan_process_overhaul.md P2.4 item 3, scratch plan TX-44; perf_suite.py pattern).

A run is a list of steps, one per (rows file, section, world group A/B) of a plan (qrun.py plan --json). Each step:
prep_srv.sh (copies and pins the jars) -> the section's @@prep actions -> scratch_server.sh boot -> wait for "Done (" ->
qrun.py run on the step's rows -> scratch_server.sh stop. Section prep (`@@prep` lines, format in qrows.py's docstring) is
read from the rows file when the step starts, so an edit reaches every later step of a running driver: its KEY=VALUE
pairs become prep_srv.sh environment (NOSWAP, SKIPSWAP, COPYDEV, PGJAR) and server.properties props, then its toml /
copy / rm actions edit OUT/srv before the boot (a change under mods/ re-pins: qpin.py pin). sNN_prep.txt logs both. State lives in perf/runs/RUN/drive/state.json (written at every step boundary), so a killed
or crashed driver resumes where it stopped: a step that was running is stopped (its server too) and continues from its
first row without a result. RUNNER.md is claimed at start (State: running, Phase, ETA, Updated) and released at the end.
A watchdog process stops the step's server when the driver dies (kill -9 included), so no server is left orphaned.

    qrun.py drive start RUN [plan options, as qrun.py plan] [--session NAME]   plan, write the state, run
    qrun.py drive start RUN --plan-json FILE                                    steps from a saved `plan --json`
    qrun.py drive resume RUN       continue after a dead driver, a signal or a reboot
    qrun.py drive status RUN       steps, rows done, ETA
    qrun.py drive stop RUN         ask a running driver to stop after the current row (state stays resumable)
    qrun.py prep FILE SECTION [A|B] [--out OUT --level LEVEL]   a section's @@prep: print it, or with --out run
                                   prep_srv.sh and the actions as a driver step does (no boot)
Start it detached so the session can end: setsid nohup python3 tools/scratch_query/qrun.py drive start RUN ... &
Server rows only: client rows (CQ-) are planned but skipped here (scratch plan TX-01 to TX-05 drive clients).
Refuses while RUNNER.md is claimed by another run or a game JVM is up (one game JVM at a time).
"""
import json
import os
import re
import signal
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
INST = HERE.parent.parent
RUNS = INST / 'perf/runs'
RUNNER = INST / 'feature_writeup/testing/RUNNER.md'
# overridable for the offline test (tools/test_queue_rows.sh)
PREP = Path(os.environ.get('QDRIVE_PREP', HERE / 'prep_srv.sh'))
SCRATCH = Path(os.environ.get('QRUN_SCRATCH', INST / 'tools/creative_labels_test/scratch_server.sh'))
RUNS = Path(os.environ.get('QDRIVE_RUNS', RUNS))
RUNNER = Path(os.environ.get('QDRIVE_RUNNER', RUNNER))
STOP = {'flag': False}


def now():
	return time.strftime('%Y-%m-%d %H:%M')


class Drive:
	def __init__(self, run):
		self.run = run
		self.dir = RUNS / run / 'drive'
		self.path = self.dir / 'state.json'
		self.state = None

	def load(self):
		self.state = json.loads(self.path.read_text())

	def save(self):
		self.dir.mkdir(parents=True, exist_ok=True)
		tmp = self.path.with_suffix('.tmp')
		tmp.write_text(json.dumps(self.state, indent=1))
		os.replace(tmp, self.path)

	def log(self, text):
		line = '%s %s' % (time.strftime('%H:%M:%S'), text)
		print(line, flush=True)
		with open(self.dir / 'drive.log', 'a') as f:
			f.write(line + '\n')


# ---------------------------------------------------------------- RUNNER.md claim

def runner_state():
	try:
		text = RUNNER.read_text()
	except OSError:
		return None, ''
	m = re.search(r'^State: *(\S+)', text, re.M)
	return (m.group(1) if m else None), text


def runner_write(fields):
	"""Rewrite the State / Updated / Phase / ETA / Run lines of the claim block, keep everything else."""
	_, text = runner_state()
	for k, v in fields.items():
		line = '%s: %s' % (k, v)
		if re.search(r'^%s: .*$' % k, text, re.M):
			text = re.sub(r'^%s: .*$' % k, line.replace('\\', '\\\\'), text, count=1, flags=re.M)
		else:
			text = re.sub(r'^(Updated: .*)$', lambda m: m.group(1) + '\n' + line, text, count=1, flags=re.M)
	RUNNER.write_text(text)


def claim(d):
	st, text = runner_state()
	mine = re.search(r'^Run: %s\b' % re.escape(d.run), text, re.M)
	if st not in (None, 'idle') and not mine:
		sys.exit('RUNNER.md is claimed (State: %s); not starting. Ask the user, skill scratch-test B1.' % st)
	runner_write({'State': 'running', 'Updated': now(), 'Run': '%s (qrun.py drive, state %s)' % (
		d.run, os.path.relpath(d.path, INST)), 'Phase': 'starting', 'ETA': eta(d)})


def release(d, summary):
	runner_write({'State': 'idle', 'Updated': now(), 'Phase': 'done', 'ETA': '-',
				  'Run': '%s finished %s: %s' % (d.run, now(), summary)})


def eta(d):
	left = sum(s.get('est', 0) for s in d.state['steps'] if s['status'] != 'done')
	return '%s (about %d min left)' % (time.strftime('%H:%M', time.localtime(time.time() + left)), left // 60)


# ---------------------------------------------------------------- steps

def game_jvms():
	if os.environ.get('QDRIVE_NO_JVM_CHECK'):
		return ''
	r = subprocess.run(['bash', str(HERE / 'game_jvms.sh')], capture_output=True, text=True)
	return r.stdout.strip()


def world_letter(group):
	"""A row's @group text -> world group 'A' (superflat) or 'B' (normal world). B: text starting with B, or naming a
	new / fresh / normal world or level (judge-14, r1118: "new world", "Fresh world" took the first letter and ran on
	the superflat, so locate biome found nothing). Everything else is A."""
	g = (group or 'A').strip()
	if g[:1].upper() == 'B' or re.search(r'\b(new|fresh|normal)\s+(world|level)\b|\bgroup\s+B\b', g, re.I):
		return 'B'
	return 'A'


def build_steps(plan_rows):
	"""plan --json rows -> steps, one per (phase, file, section, group letter), in plan order; client rows skipped."""
	sys.path.insert(0, str(HERE))
	import qrows
	meta = {}
	for p in qrows.all_files():
		f = qrows.load(p)
		for sec in f['sections']:
			for kind, r in sec['items']:
				if kind == 'row':
					meta[(os.path.relpath(p, INST), r['id'])] = qrows.effective(sec, r)
	steps, skipped = {}, []
	for x in plan_rows:
		if x.get('client') or x['state'] == 'draft':
			skipped.append(x['id'])
			continue
		m = meta.get((x['file'], x['id']), {})
		g = world_letter(m.get('group'))
		ph = x.get('phase', 'server')
		key = '%s#%s#%s#%s' % (ph, x['file'], x['section'], g)   # plan --json is in run-phase order (qrows.PHASES)
		steps.setdefault(key, {'key': key, 'file': x['file'], 'section': x['section'], 'group': g, 'phase': ph, 'rows': [],
							   'status': 'pending', 'est': 0})['rows'].append(x['id'])
	out = list(steps.values())
	import qlint
	import qrun
	for s in out:
		ids = set(s['rows'])
		s['est'] = int(sum(sum(qlint._est(r)) / 2 for r in qrun.parse(str(INST / s['file']), errors=[])
						   if r['id'] in ids)) + 240   # boot and stop
	return out, skipped


def section_prep(file, section, group):
	"""The step's @@prep (qrows.prep_of), read from the rows file now. A file or section that is gone: no prep."""
	sys.path.insert(0, str(HERE))
	import qrows
	path = Path(file) if Path(file).is_absolute() else INST / file
	sec = qrows.section_of(path, section) if path.exists() else None
	return qrows.prep_of(sec or {}, group)


def prep_srv(out, level, group, prep):
	"""prep_srv.sh with the prep's environment and props; (returncode, output)."""
	env = dict(os.environ, **prep['env'])
	r = subprocess.run(['bash', str(PREP), str(out), level, group] + prep['props'], capture_output=True, text=True,
					   env=env, stdin=subprocess.DEVNULL)
	head = ''.join('prep: %s\n' % t for t in prep['lines'])
	if prep['env']:
		head += 'prep env: %s\n' % ' '.join('%s=%s' % kv for kv in sorted(prep['env'].items()))
	return r.returncode, head + r.stdout + r.stderr


def prep_actions(out, prep):
	"""Apply the prep's toml / copy / rm actions to OUT/srv (after prep_srv.sh, before the boot); log lines.
	RuntimeError on a missing source."""
	import glob
	import shutil
	srv = Path(out) / 'srv'
	log, mods = [], False
	for kind, args in prep['actions']:
		if kind == 'toml':
			f, section, key, value = args
			path = srv / (f if '/' in f else 'config/' + f)
			path.parent.mkdir(parents=True, exist_ok=True)
			path.touch()
			r = subprocess.run([sys.executable, str(HERE / 'qrun.py'), 'toml', str(path), section, key, value],
							   capture_output=True, text=True)
			log.append('toml: ' + (r.stdout + r.stderr).strip())
			if r.returncode:
				raise RuntimeError('@@prep toml failed: %s' % ' '.join(args))
		elif kind == 'copy':
			src, dest = INST / args[0], srv / args[1]
			if not src.exists():
				raise RuntimeError('@@prep copy: no %s' % args[0])
			if args[1].endswith('/'):
				dest = dest / src.name
			dest.parent.mkdir(parents=True, exist_ok=True)
			if src.is_dir():
				shutil.copytree(src, dest, dirs_exist_ok=True)
			else:
				shutil.copy2(src, dest)
			log.append('copy: %s -> %s' % (args[0], dest.relative_to(srv)))
		elif kind == 'rm':
			hits = sorted(glob.glob(str(srv / args[0])))
			for h in hits:
				(shutil.rmtree if os.path.isdir(h) and not os.path.islink(h) else os.remove)(h)
			log.append('rm: %s (%d)' % (args[0], len(hits)))
		mods = mods or (args[0] if kind in ('toml', 'rm') else args[1]).startswith('mods')
	if mods:
		r = subprocess.run([sys.executable, str(HERE / 'qpin.py'), 'pin', str(out)], capture_output=True, text=True)
		log.append('repin: ' + (r.stdout + r.stderr).strip()[-300:])
	return log


def wait_ready(out, secs=600):
	log = Path(out) / 'srv/logs/latest.log'
	end = time.time() + secs
	while time.time() < end and not STOP['flag']:
		try:
			if 'Done (' in log.read_text(errors='replace'):
				return True
		except OSError:
			pass
		time.sleep(3)
	return False


def wait_failed_boot(out, secs=600):
	"""A step whose rows all expect the boot to fail (`bootreject Done \(`, e.g. SQ-CBC-06 missing dependency): wait until
	the log shows Done (the rows then FAIL as they should), a crash or dependency error, or the server stopped
	(judge-14, r1118: wait_ready waited 600 s and raised, so such a row never ran)."""
	log = Path(out) / 'srv/logs/latest.log'
	end = time.time() + secs
	rx = re.compile(r'Done \(|Missing or unsupported mandatory dependencies|Failed to start|Crash report|crash-reports|'
					r'Stopping server|ModLoadingException')
	while time.time() < end and not STOP['flag']:
		try:
			if rx.search(log.read_text(errors='replace')):
				time.sleep(10)
				return True
		except OSError:
			pass
		time.sleep(3)
	return False


def expects_failed_boot(file, rows):
	"""True when every row of the step has a `bootreject Done \(` step."""
	try:
		import qrun
		parsed = {r['id']: r for r in qrun.parse(str(INST / file))}
	except Exception:  # noqa: BLE001
		return False
	sel = [parsed.get(r) for r in rows]
	return bool(sel) and all(r and any(w.endswith('bootreject') and arg.startswith('Done') for w, arg, _ in r['steps'])
							 for r in sel)


def watchdog(pid, out):
	"""Detached: stop the step's server when the driver pid is gone."""
	return subprocess.Popen([sys.executable, str(Path(__file__)), 'watchdog', str(pid), str(out)],
							start_new_session=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def first_unrun(res, rows):
	try:
		done = json.loads((Path(res) / 'results.json').read_text())
	except (OSError, ValueError):
		done = {}
	return [r for r in rows if r not in done]


def run_step(d, i, s):
	out = d.dir.parent / ('s%02d' % i)
	res = d.dir.parent / ('s%02d_res' % i)
	s.update({'out': str(out), 'res': str(res), 'status': 'running', 'started': s.get('started') or now()})
	d.save()
	runner_write({'Updated': now(), 'Phase': 'step %d/%d %s section %s group %s (%d rows)' % (
		i + 1, len(d.state['steps']), Path(s['file']).name, s['section'], s['group'], len(s['rows'])), 'ETA': eta(d)})
	todo = first_unrun(res, s['rows'])
	if not todo:
		s['status'] = 'done'
		d.save()
		return
	if game_jvms():
		d.log('a game JVM is up; waiting up to 10 min (one game JVM at a time)')
		end = time.time() + 600
		while game_jvms() and time.time() < end and not STOP['flag']:
			time.sleep(10)
		if game_jvms():
			raise RuntimeError('a game JVM is still up; not booting a second one')
	level = '%s_s%02d' % (d.run, i)
	d.log('step %d: prep %s group %s' % (i + 1, out, s['group']))
	try:
		prep = section_prep(s['file'], s['section'], s['group'])
	except (ValueError, SystemExit) as e:
		raise RuntimeError('bad @@prep in %s section %s: %s' % (s['file'], s['section'], e))
	if prep['lines']:
		d.log('step %d: @@prep %s' % (i + 1, ' | '.join(prep['lines'])))
	rc, text = prep_srv(out, level, s['group'], prep)
	(d.dir / ('s%02d_prep.txt' % i)).write_text(text)
	if rc:
		raise RuntimeError('prep_srv.sh failed (s%02d_prep.txt)' % i)
	if prep['actions']:
		with open(d.dir / ('s%02d_prep.txt' % i), 'a') as f:
			f.write('\n'.join(prep_actions(out, prep)) + '\n')
	# output to a file, never a pipe: the server's fifo feeder inherits the fds and holds a pipe open (r1118)
	with open(d.dir / ('s%02d_boot.txt' % i), 'a') as bf:
		subprocess.run(['bash', str(SCRATCH), 'boot', str(out)], stdout=bf, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL)
	dog = watchdog(os.getpid(), out)
	s['watchdog'] = dog.pid
	d.save()
	try:
		noboot = expects_failed_boot(s['file'], todo)
		if noboot:
			if not wait_failed_boot(out):
				raise RuntimeError('boot neither failed nor finished in 600 s (%s/srv/logs/latest.log)' % out)
			d.log('step %d: rows expect a failed boot; running them on the log' % (i + 1))
		elif not wait_ready(out):
			raise RuntimeError('server not ready in 600 s (%s/srv/logs/latest.log)' % out)
		d.log('step %d: %d rows' % (i + 1, len(todo)))
		cmd = [sys.executable, str(HERE / 'qrun.py'), 'run', str(out), str(INST / s['file']), str(res), '--rows',
			   ','.join(todo)] + (['--no-watch', '--no-probes'] if noboot else [])
		with open(d.dir / ('s%02d_qrun.txt' % i), 'a') as f:
			p = subprocess.Popen(cmd, stdout=f, stderr=subprocess.STDOUT)
			while p.poll() is None:
				if STOP['flag']:
					p.send_signal(signal.SIGINT)
				time.sleep(2)
		left = first_unrun(res, s['rows'])
		s['status'] = 'done' if not left else ('stopped' if STOP['flag'] else 'incomplete')
		s['left'] = left
	finally:
		subprocess.run(['bash', str(SCRATCH), 'stop', str(out)], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, stdin=subprocess.DEVNULL)
		try:
			os.kill(dog.pid, signal.SIGTERM)
		except OSError:
			pass
		s['ended'] = now()
		d.save()


def drive(d):
	for sig in (signal.SIGTERM, signal.SIGINT, signal.SIGHUP):
		signal.signal(sig, lambda *_: STOP.__setitem__('flag', True))
	d.state['pid'] = os.getpid()
	d.state['status'] = 'running'
	d.save()
	claim(d)
	try:
		for i, s in enumerate(d.state['steps']):
			if STOP['flag'] or (d.dir / 'STOP').exists():
				STOP['flag'] = True
				break
			if s['status'] == 'done':
				continue
			try:
				run_step(d, i, s)
			except RuntimeError as e:
				s['status'] = 'failed'
				s['error'] = str(e)
				d.log('step %d failed: %s' % (i + 1, e))
				d.save()
				if 'game JVM' in str(e):
					break
	finally:
		steps = d.state['steps']
		done = sum(1 for s in steps if s['status'] == 'done')
		d.state['status'] = 'stopped' if STOP['flag'] else ('done' if done == len(steps) else 'incomplete')
		d.save()
		summary = '%d/%d steps done%s; qrun.py drive status %s' % (
			done, len(steps), ' (stopped, resumable)' if STOP['flag'] else '', d.run)
		if STOP['flag']:
			runner_write({'State': 'idle', 'Updated': now(), 'Phase': 'stopped, resumable', 'ETA': '-',
						  'Run': '%s stopped %s: %s' % (d.run, now(), summary)})
		else:
			release(d, summary)
		d.log(summary)


def status(d):
	st = d.state
	alive = False
	try:
		os.kill(st.get('pid', 0), 0)
		alive = bool(st.get('pid'))
	except OSError:
		pass
	print('%s: %s (driver %s); %s' % (d.run, st['status'], 'alive' if alive else 'not running', eta(d)))
	for i, s in enumerate(st['steps']):
		left = first_unrun(s['res'], s['rows']) if s.get('res') else s['rows']
		print('  %2d %-11s %s section %s group %s: %d/%d rows%s' % (
			i + 1, s['status'], Path(s['file']).name, s['section'], s['group'], len(s['rows']) - len(left),
			len(s['rows']), ('  ' + s['error']) if s.get('error') else ''))
	if st.get('skipped'):
		print('  client or draft rows not driven: %d' % len(st['skipped']))


def main_prep(argv):
	"""qrun.py prep FILE SECTION [A|B] [--out OUT --level LEVEL]"""
	if len(argv) < 2:
		sys.exit(main_prep.__doc__)
	opt = {k: argv[argv.index(k) + 1] for k in ('--out', '--level') if k in argv}
	pos = [a for j, a in enumerate(argv) if not a.startswith('--') and (j == 0 or argv[j - 1] not in ('--out', '--level'))]
	group = pos[2] if len(pos) > 2 else 'A'
	prep = section_prep(pos[0], pos[1], group)
	print(json.dumps(prep, indent=1))
	if '--out' not in opt:
		return 0
	if game_jvms():
		sys.exit('a game JVM is up: not preparing over it')
	rc, text = prep_srv(opt['--out'], opt.get('--level', Path(opt['--out']).name), group, prep)
	print(text[-2000:])
	if rc:
		return rc
	print('\n'.join(prep_actions(opt['--out'], prep)))
	return 0


def main(argv):
	if len(argv) < 2:
		print(__doc__)
		return 2
	cmd, run = argv[0], argv[1]
	if cmd == 'watchdog':
		pid, out = int(argv[1]), argv[2]
		while True:
			try:
				os.kill(pid, 0)
			except OSError:
				subprocess.run(['bash', str(SCRATCH), 'stop', out], capture_output=True)
				return 0
			time.sleep(5)
	d = Drive(run)
	if cmd == 'start':
		if d.path.exists():
			sys.exit('%s exists: qrun.py drive resume %s' % (d.path, run))
		if game_jvms():
			sys.exit('a game JVM is up; not starting (one game JVM at a time)')
		rest = argv[2:]
		if '--plan-json' in rest:
			rows = json.loads(Path(rest[rest.index('--plan-json') + 1]).read_text())
		else:
			r = subprocess.run([sys.executable, str(HERE / 'qrun.py'), 'plan', '--json'] + rest, capture_output=True,
							   text=True)
			if r.returncode:
				sys.exit(r.stdout + r.stderr)
			rows = json.loads(r.stdout)
		steps, skipped = build_steps(rows)
		if not steps:
			sys.exit('nothing to drive (%d client or draft rows skipped)' % len(skipped))
		d.state = {'run': run, 'created': now(), 'status': 'new', 'steps': steps, 'skipped': skipped,
				   'plan_args': rest}
		d.save()
		d.log('%d steps, %d rows, %d skipped (client or draft)' % (len(steps), sum(len(s['rows']) for s in steps),
																   len(skipped)))
		drive(d)
		return 0
	if not d.path.exists():
		sys.exit('no drive state for %s (%s)' % (run, d.path))
	d.load()
	if cmd == 'status':
		status(d)
		return 0
	if cmd == 'stop':
		(d.dir / 'STOP').write_text(now())
		try:
			os.kill(d.state.get('pid', 0), signal.SIGTERM)
		except OSError:
			pass
		print('stop requested')
		return 0
	if cmd == 'resume':
		try:
			os.kill(d.state.get('pid', 0), 0)
			if d.state.get('pid') and d.state['status'] == 'running':
				sys.exit('driver %d is still alive; qrun.py drive stop %s first' % (d.state['pid'], run))
		except OSError:
			pass
		(d.dir / 'STOP').unlink(missing_ok=True)
		for s in d.state['steps']:
			if s['status'] in ('running', 'stopped', 'incomplete', 'failed'):
				if s.get('out'):
					subprocess.run(['bash', str(SCRATCH), 'stop', s['out']], capture_output=True)
				s['status'] = 'pending'
				s.pop('error', None)
		d.log('resume')
		drive(d)
		return 0
	print(__doc__)
	return 2


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
