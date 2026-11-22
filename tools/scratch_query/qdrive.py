#!/usr/bin/env python3
"""Resumable scratch run driver (plan_process_overhaul.md P2.4 item 3, scratch plan TX-44; perf_suite.py pattern).

A run is a list of steps, one per (rows file, section, world group A/B) of a plan (qrun.py plan --json). Each step:
prep_srv.sh (copies and pins the jars) -> the section's @@prep actions -> scratch_server.sh boot -> wait for "Done (" ->
qrun.py run on the step's rows -> scratch_server.sh stop. Section prep (`@@prep` lines, format in qrows.py's docstring) is
read from the rows file when the step starts, so an edit reaches every later step of a running driver: its KEY=VALUE
pairs become prep_srv.sh environment (NOSWAP, SKIPSWAP, COPYDEV, PGJAR) and server.properties props, then its toml /
copy / rm / run actions edit OUT/srv before the boot (a change under mods/, or any run, re-pins: qpin.py pin). sNN_prep.txt
logs both. Boot phases (r1118 R4 A1): a step whose rows carry `@boot N` runs boots 1..N in order on the same folder and
world: boot 1 as above with that boot's rows, then for each later boot: stop, the section's `@@prep boot=N:` lines
(props into server.properties, toml / copy / rm / run / fresh), boot, its rows, stop. A planned row of boot N brings the
section's rows of lower boots (same world group) into the step: they build the world it reads. Steps without @boot rows
run exactly as before (one boot). A resumed multi-boot step continues at the boot of its first row without a result,
on the folder it left (that boot's prep is not applied twice). Mod sets (B1): a row whose effective `@modset` / section
`@@modset` names a manifest (modsets/NAME.toml) gets its own step (the spec is in the step key) built by qmodset.py
build instead of prep_srv.sh; `@@prep boot=N: modset SPEC` switches the jar set before boot N, world kept. State lives in perf/runs/RUN/drive/state.json (written at every step boundary), so a killed
or crashed driver resumes where it stopped: a step that was running is stopped (its server too) and continues from its
first row without a result. RUNNER.md is claimed at start (State: running, Phase, ETA, Updated) and released at the end.
A watchdog process stops the step's server when the driver dies (kill -9 included), so no server is left orphaned.

    qrun.py drive start RUN [plan options, as qrun.py plan] [--session NAME]   plan, write the state, run
    qrun.py drive start RUN --plan-json FILE                                    steps from a saved `plan --json`
    qrun.py drive resume RUN       continue after a dead driver, a signal or a reboot
    qrun.py drive status RUN       steps, rows done, ETA
    qrun.py drive stop RUN         ask a running driver to stop after the current row (state stays resumable)
    qrun.py prep FILE SECTION [A|B] [--boot N] [--out OUT --level LEVEL]   a section's @@prep: print it, or with --out run
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
	meta, files = {}, {}
	# the queue's rows files plus any run batch the plan names (qbundle.py bundles, r1121b: without them a bundle's
	# group B rows read as group A)
	extra = {INST / x['file'] for x in plan_rows} - set(qrows.all_files())
	for p in list(qrows.all_files()) + sorted(q for q in extra if q.is_file()):
		f = qrows.load(p)
		files[os.path.relpath(p, INST)] = f
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
		ms = m.get('modset') or ''
		if ms:
			key += '#modset=' + ms   # B1: rows of one section on different jar sets get their own steps
		st = steps.setdefault(key, {'key': key, 'file': x['file'], 'section': x['section'], 'group': g, 'phase': ph,
									'rows': [], 'status': 'pending', 'est': 0})
		st['rows'].append(x['id'])
		if ms:
			st['modset'] = ms
	out = list(steps.values())
	for s in out:
		boot_phases(s, meta, files)
	import qlint
	import qrun
	for s in out:
		ids = set(s['rows'])
		s['est'] = int(sum(sum(qlint._est(r)) / 2 for r in qrun.parse(str(INST / s['file']), errors=[])
						   if r['id'] in ids)) + 240 * len(set((s.get('boots') or {'': 1}).values()))   # boot and stop
	return out, skipped


def boot_of(m):
	"""A row's @boot (1 when absent or not a number; qlint E boot flags the bad ones)."""
	v = str((m or {}).get('boot') or '1').strip()
	return int(v) if v.isdigit() and int(v) >= 1 else 1


def boot_phases(s, meta, files):
	"""A1: when a step holds a row with @boot N > 1, add the section's runnable server rows of lower boots (same world
	group) it lacks, sort the rows by boot (file order within a boot) and record s['boots'] = {row id: N}. A step with
	only boot-1 rows is left exactly as it was."""
	boots = {r: boot_of(meta.get((s['file'], r))) for r in s['rows']}
	if all(b == 1 for b in boots.values()):
		return
	sys.path.insert(0, str(HERE))
	import qrows
	top = max(boots.values())
	f = files.get(s['file'])
	order = []
	for sec in (f or {}).get('sections', []):
		if str(sec['key']) != str(s['section']):
			continue
		for kind, r in sec['items']:
			if kind != 'row':
				continue
			order.append(r['id'])
			m = meta.get((s['file'], r['id']), {})
			if r['id'] in boots or m.get('superseded') or m.get('draft') or m.get('client') == 'true':
				continue
			if not qrows.runnable(r) or world_letter(m.get('group')) != s['group'] or (m.get('modset') or '') != s.get('modset', ''):
				continue
			b = boot_of(m)
			if b < top:
				boots[r['id']] = b
				s.setdefault('setup', []).append(r['id'])
	pos = {r: i for i, r in enumerate(order)}
	s['rows'] = sorted(boots, key=lambda r: (boots[r], pos.get(r, 10 ** 6)))
	s['boots'] = {r: boots[r] for r in s['rows']}


def section_prep(file, section, group, boot=1):
	"""The step's @@prep for boot BOOT (qrows.prep_of), read from the rows file now. A file or section that is gone: no
	prep."""
	sys.path.insert(0, str(HERE))
	import qrows
	path = Path(file) if Path(file).is_absolute() else INST / file
	sec = qrows.section_of(path, section) if path.exists() else None
	return qrows.prep_of(sec or {}, group, boot)


def prep_srv(out, level, group, prep):
	"""prep_srv.sh with the prep's environment and props; (returncode, output)."""
	env = dict(os.environ, **prep['env'])
	r = subprocess.run(['bash', str(PREP), str(out), level, group] + prep['props'], capture_output=True, text=True,
					   env=env, stdin=subprocess.DEVNULL)
	head = ''.join('prep: %s\n' % t for t in prep['lines'])
	if prep['env']:
		head += 'prep env: %s\n' % ' '.join('%s=%s' % kv for kv in sorted(prep['env'].items()))
	return r.returncode, head + r.stdout + r.stderr


def setprop(srv, key, value):
	"""key=value in srv/server.properties (replaced or appended), as prep_srv.sh's setprop."""
	p = Path(srv) / 'server.properties'
	lines = p.read_text().split('\n') if p.exists() else []
	for j, l in enumerate(lines):
		if l.startswith(key + '='):
			lines[j] = '%s=%s' % (key, value)
			break
	else:
		if lines and lines[-1] == '':
			lines.insert(len(lines) - 1, '%s=%s' % (key, value))
		else:
			lines.append('%s=%s' % (key, value))
	p.write_text('\n'.join(lines))


def prep_actions(out, prep, boot=1):
	"""Apply the prep's toml / copy / rm / run / fresh actions to OUT/srv (after prep_srv.sh, before the boot; for boot
	N > 1 before that boot, its props first); log lines. RuntimeError on a missing source or a failed run."""
	import glob
	import shutil
	srv = Path(out) / 'srv'
	log, mods = [], False
	if boot > 1:
		for kv in prep['props']:
			k, _, v = kv.partition('=')
			setprop(srv, k, v)
			log.append('prop: %s' % kv)
	for kind, args in prep['actions']:
		if kind == 'run':
			r = subprocess.run(['bash', '-c', args[0]], cwd=srv, capture_output=True, text=True, stdin=subprocess.DEVNULL,
							   env=dict(os.environ, INST=str(INST), OUT=str(Path(out).resolve()), SRV=str(srv.resolve()),
										BOOT=str(boot)))
			log.append('run: %s (exit %d)%s' % (args[0], r.returncode, ''.join('\n  ' + x for x in
																			  (r.stdout + r.stderr).strip().split('\n')[-20:] if x)))
			if r.returncode:
				raise RuntimeError('@@prep run failed (exit %d): %s' % (r.returncode, args[0]))
			mods = True
			continue
		if kind == 'modset':
			sys.path.insert(0, str(HERE))
			import qmodset
			try:
				log += qmodset.switch(out, args[0])
			except (ValueError, RuntimeError) as e:
				raise RuntimeError('@@prep modset %s failed: %s' % (args[0], e))
			continue
		if kind == 'fresh':
			p = srv / 'server.properties'
			cur = re.search(r'^level-name=(.*)$', p.read_text(), re.M) if p.exists() else None
			base = re.sub(r'_b\d+$', '', cur.group(1)) if cur else 'world'
			setprop(srv, 'level-name', '%s_b%d' % (base, boot))
			log.append('fresh: level-name %s_b%d' % (base, boot))
			continue
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
			t = log.read_text(errors='replace')
			if 'Done (' in t:
				return True
			if 'Failed to load datapacks' in t:   # the JVM sits idle after this; no point waiting out the cap (r1121)
				time.sleep(5)
				return False
		except OSError:
			pass
		time.sleep(3)
	return False


def rhino_flake(out):
	"""Known third-party boot flake (gotchas.md, Boot and shutdown): "Failed to load datapacks" caused in Rhino
	Interpreter.getSourcePositionFromStack (NPE `frame is null` or ArrayIndexOutOfBounds) from KubeJS
	TagLoaderKJS.customTags console.info on a worker thread. Clean on reboot (r1115 phase 4, r1121 step 95)."""
	try:
		t = (Path(out) / 'srv/logs/latest.log').read_text(errors='replace')
	except OSError:
		return False
	return 'Failed to load datapacks' in t and 'getSourcePositionFromStack' in t and 'TagLoaderKJS' in t


def kill_own_jvm(d, out, wait=60):
	"""After a stop: a JVM whose cwd is OUT/srv that is still up (a failed boot never reads the fifo, so stop does not
	end it) is this drive's own; kill it so the next step can boot (gotchas.md, Boot and shutdown)."""
	srv = os.path.realpath(str(Path(out) / 'srv'))
	end = time.time() + wait
	while True:
		pids = [ln.split()[0] for ln in game_jvms().splitlines()
				if len(ln.split()) > 1 and os.path.realpath(ln.split(None, 1)[1]) == srv]
		if not pids or time.time() >= end:
			break
		time.sleep(3)
	for pid in pids:
		try:
			os.kill(int(pid), signal.SIGKILL)
			d.log('killed own JVM %s (%s) still up after stop' % (pid, srv))
		except (OSError, ValueError):
			pass
	if pids:
		time.sleep(5)


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


def superseded_now(file):
	"""Row ids of FILE marked @superseded now (a mark set after the plan; qrun.py lists them and runs none)."""
	sys.path.insert(0, str(HERE))
	import qrows
	try:
		f = qrows.load(INST / file)
	except Exception:  # a rows file mid-edit: run what the plan says
		return set()
	return {r['id'] for sec in f.get('sections', []) for kind, r in sec['items']
			if kind == 'row' and qrows.effective(sec, r).get('superseded')}


def first_unrun(res, rows, file=None):
	try:
		done = json.loads((Path(res) / 'results.json').read_text())
	except (OSError, ValueError):
		done = {}
	gone = superseded_now(file) if file else set()
	return [r for r in rows if r not in done and r not in gone]


def retired_rows(s):
	"""Ids of the step's rows that the rows file now marks @superseded or @draft (read fresh at step start, so a .qb
	edit made while the driver runs reaches pending steps)."""
	sys.path.insert(0, str(HERE))
	import qrows
	try:
		doc = qrows.load(str(INST / s['file']))
	except Exception:
		return set()
	want = set(s['rows'])
	out = set()
	for sec in doc['sections']:
		for kind, r in sec['items']:
			if kind == 'row' and r.get('id') in want and ('superseded' in r['meta'] or 'draft' in r['meta']):
				out.add(r['id'])
	return out


def run_step(d, i, s):
	out = d.dir.parent / ('s%02d' % i)
	res = d.dir.parent / ('s%02d_res' % i)
	s.update({'out': str(out), 'res': str(res), 'status': 'running', 'started': s.get('started') or now()})
	retired = retired_rows(s)   # r1120nv: rows marked @superseded / @draft after planning drop out at step start
	try:   # drive/skip.txt: one row id per line (# comments), rows to leave out of a running drive without a restart
		skip = {l.split('#')[0].strip() for l in open(d.dir / 'skip.txt')} - {''}
	except OSError:
		skip = set()
	retired |= set(s['rows']) & skip
	if retired:
		s['rows'] = [r for r in s['rows'] if r not in retired]
		s['dropped'] = sorted(set(s.get('dropped', [])) | retired)
		d.log('step %d: dropped (@superseded / @draft in the rows file, or drive/skip.txt): %s' % (i + 1, ', '.join(sorted(retired))))
	d.save()
	runner_write({'Updated': now(), 'Phase': 'step %d/%d %s section %s group %s (%d rows)' % (
		i + 1, len(d.state['steps']), Path(s['file']).name, s['section'], s['group'], len(s['rows'])), 'ETA': eta(d)})
	todo = first_unrun(res, s['rows'], s['file'])
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
	if s.get('boots'):
		return run_boots(d, i, s, out, res, level, todo)
	prep_boot1(d, i, s, out, level)
	boot_and_run(d, i, s, out, res, todo)
	left = first_unrun(res, s['rows'], s['file'])
	s['status'] = 'done' if not left else ('stopped' if STOP['flag'] else 'incomplete')
	s['left'] = left
	d.save()


def prep_boot1(d, i, s, out, level):
	"""prep_srv.sh (or, for a step with a mod set, qmodset.py build) with the section's (boot 1) @@prep, then its
	actions."""
	d.log('step %d: prep %s group %s' % (i + 1, out, s['group']))
	try:
		prep = section_prep(s['file'], s['section'], s['group'])
	except (ValueError, SystemExit) as e:
		raise RuntimeError('bad @@prep in %s section %s: %s' % (s['file'], s['section'], e))
	if prep['lines']:
		d.log('step %d: @@prep %s' % (i + 1, ' | '.join(prep['lines'])))
	if s.get('modset'):
		sys.path.insert(0, str(HERE))
		import qmodset
		d.log('step %d: mod set %s' % (i + 1, s['modset']))
		try:
			rc, text = qmodset.build(out, level, s['group'], s['modset'], prep['props'], prep['env'])
		except ValueError as e:
			rc, text = 1, 'bad mod set %s: %s\n' % (s['modset'], e)
		text = ''.join('prep: %s\n' % t for t in prep['lines']) + text
	else:
		rc, text = prep_srv(out, level, s['group'], prep)
	(d.dir / ('s%02d_prep.txt' % i)).write_text(text)
	if rc:
		raise RuntimeError('prep_srv.sh failed (s%02d_prep.txt)' % i)
	if prep['actions']:
		with open(d.dir / ('s%02d_prep.txt' % i), 'a') as f:
			f.write('\n'.join(prep_actions(out, prep)) + '\n')


def boot_and_run(d, i, s, out, res, todo, boot=None, retry=1, keep=False):
	"""Boot OUT, wait for Done (or the failed boot the rows expect), qrun.py run on todo, stop (always). A boot that
	hits the known Rhino tag-load flake is stopped, its JVM killed and booted again, `retry` times. keep: qrun keeps
	the forceloads (--keep-forceloads), for a boot whose world a later boot of the step reuses (qforceload.py)."""
	tag = '' if boot is None else ' boot %d' % boot
	try:
		_boot_and_run(d, i, s, out, res, todo, boot, tag, keep)
	except RuntimeError:
		if retry > 0 and rhino_flake(out):
			log = Path(out) / 'srv/logs/latest.log'
			kept = Path(out) / ('srv/logs/rhino_flake_step%02d.log' % (i + 1))
			try:
				kept.write_bytes(log.read_bytes())
			except OSError:
				pass
			d.log('step %d%s: known Rhino tag-load flake (Failed to load datapacks); rebooting once (log kept as %s)'
				  % (i + 1, tag, kept.name))
			return boot_and_run(d, i, s, out, res, todo, boot, retry - 1, keep)
		raise


def _boot_and_run(d, i, s, out, res, todo, boot, tag, keep=False):
	# output to a file, never a pipe: the server's fifo feeder inherits the fds and holds a pipe open (r1118)
	with open(d.dir / ('s%02d_boot.txt' % i), 'a') as bf:
		subprocess.run(['bash', str(SCRATCH), 'boot', str(out)], stdout=bf, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL)
	dog = watchdog(os.getpid(), out)
	s['watchdog'] = dog.pid
	d.save()
	try:
		noboot = bool(todo) and expects_failed_boot(s['file'], todo)
		if noboot:
			if not wait_failed_boot(out):
				raise RuntimeError('boot neither failed nor finished in 600 s (%s/srv/logs/latest.log)' % out)
			d.log('step %d%s: rows expect a failed boot; running them on the log' % (i + 1, tag))
		elif not wait_ready(out):
			raise RuntimeError('server not ready in 600 s (%s/srv/logs/latest.log)%s' % (out, tag))
		if not todo:
			d.log('step %d%s: no rows of this boot left; stopping' % (i + 1, tag))
			return
		d.log('step %d%s: %d rows' % (i + 1, tag, len(todo)))
		cmd = [sys.executable, str(HERE / 'qrun.py'), 'run', str(out), str(INST / s['file']), str(res), '--rows',
			   ','.join(todo)] + (['--no-watch', '--no-probes'] if noboot else []) + (['--keep-forceloads'] if keep else [])
		with open(d.dir / ('s%02d_qrun.txt' % i), 'a') as f:
			p = subprocess.Popen(cmd, stdout=f, stderr=subprocess.STDOUT)
			while p.poll() is None:
				if STOP['flag']:
					p.send_signal(signal.SIGINT)
				time.sleep(2)
	finally:
		subprocess.run(['bash', str(SCRATCH), 'stop', str(out)], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, stdin=subprocess.DEVNULL)
		kill_own_jvm(d, out)
		try:
			os.kill(dog.pid, signal.SIGTERM)
		except OSError:
			pass
		s['ended'] = now()
		d.save()


def run_boots(d, i, s, out, res, level, todo):
	"""A1: boots 1..N of a multi-boot step on one folder and world (qdrive docstring)."""
	boots = s['boots']
	left = set(todo)
	need = sorted({boots.get(r, 1) for r in left})
	first, last = need[0], need[-1]
	srv = Path(out) / 'srv'
	if first == 1 or not srv.is_dir():
		if first > 1:
			raise RuntimeError('resume at boot %d needs %s from the earlier boots; it is gone (rerun the step from boot '
							   '1: drop its rows from s%02d_res/results.json)' % (first, srv, i))
		prep_boot1(d, i, s, out, level)
		s['prepped'] = 1
		d.save()
	for b in range(first, last + 1):
		if STOP['flag']:
			break
		if b > first:
			end = time.time() + 120
			while game_jvms() and time.time() < end:   # stop can return while the JVM still saves
				time.sleep(3)
			if game_jvms():
				raise RuntimeError('a game JVM is still up 120 s after the boot %d stop; not booting boot %d' % (b - 1, b))
		if b > 1 and s.get('prepped', 0) < b:
			try:
				prep = section_prep(s['file'], s['section'], s['group'], b)
			except (ValueError, SystemExit) as e:
				raise RuntimeError('bad @@prep in %s section %s: %s' % (s['file'], s['section'], e))
			d.log('step %d: before boot %d: %s' % (i + 1, b, ' | '.join(prep['lines']) or 'no @@prep boot=%d: lines' % b))
			with open(d.dir / ('s%02d_prep.txt' % i), 'a') as f:
				f.write('\n# before boot %d\n' % b + ''.join('prep: %s\n' % t for t in prep['lines'])
						+ '\n'.join(prep_actions(out, prep, b)) + '\n')
			s['prepped'] = b
		s['boot'] = b
		d.save()
		rows_b = [r for r in s['rows'] if boots.get(r, 1) == b and r in left]
		# forced chunks are saved with the world: a boot before the step's last keeps them for the next boot's rows
		boot_and_run(d, i, s, out, res, rows_b, b, keep=b < last)
		if rows_b and first_unrun(res, rows_b, s['file']) and not STOP['flag']:
			d.log('step %d: boot %d left rows without a result; later boots skipped' % (i + 1, b))
			break
	left = first_unrun(res, s['rows'], s['file'])
	s['status'] = 'done' if not left else ('stopped' if STOP['flag'] else 'incomplete')
	s['left'] = left
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
		left = first_unrun(s['res'], s['rows'], s['file']) if s.get('res') else s['rows']
		print('  %2d %-11s %s section %s group %s: %d/%d rows%s%s' % (
			i + 1, s['status'], Path(s['file']).name, s['section'], s['group'], len(s['rows']) - len(left),
			len(s['rows']), ' (boots 1-%d, at %d)' % (max(s['boots'].values()), s.get('boot', 1)) if s.get('boots')
			else '', ('  ' + s['error']) if s.get('error') else ''))
	if st.get('skipped'):
		print('  client or draft rows not driven: %d' % len(st['skipped']))


def main_prep(argv):
	"""qrun.py prep FILE SECTION [A|B] [--boot N] [--out OUT --level LEVEL]   (--boot N > 1 with --out: only that boot's
	actions on an existing OUT, as the driver applies them before boot N)"""
	if len(argv) < 2:
		sys.exit(main_prep.__doc__)
	keys = ('--out', '--level', '--boot')
	opt = {k: argv[argv.index(k) + 1] for k in keys if k in argv}
	pos = [a for j, a in enumerate(argv) if not a.startswith('--') and (j == 0 or argv[j - 1] not in keys)]
	group = pos[2] if len(pos) > 2 else 'A'
	boot = int(opt.get('--boot', 1))
	prep = section_prep(pos[0], pos[1], group, boot)
	print(json.dumps(prep, indent=1))
	if '--out' not in opt:
		return 0
	if game_jvms():
		sys.exit('a game JVM is up: not preparing over it')
	if boot > 1:
		print('\n'.join(prep_actions(opt['--out'], prep, boot)))
		return 0
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
