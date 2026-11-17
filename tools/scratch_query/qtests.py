#!/usr/bin/env python3
"""Offline tests for the tiered scratch queue (plan_process_overhaul.md Phase 2, PROC-20 to PROC-28). No game: a fake
scratch_server.sh writes commands into a fake latest.log, rows files live in a temp dir (QROWS_DIR). Run by
tools/test_queue_rows.sh (check_all.py)."""
import json
import os
import shutil
import subprocess
import sys
import tempfile
import time
import zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
INST = HERE.parent.parent
TMP = Path(tempfile.mkdtemp(prefix='qtests_'))
ROWS = TMP / 'rows'
ROWS.mkdir()
os.environ['QROWS_DIR'] = str(ROWS)
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent))
import qrows  # noqa: E402
import qrowlint  # noqa: E402
import qplan  # noqa: E402
import qpin  # noqa: E402
import qverdict  # noqa: E402

FAKE = TMP / 'fake_scratch.sh'
FAKE.write_text('''#!/usr/bin/env bash
what=$1; OUT=$2; L="$OUT/srv/logs/latest.log"
case "$what" in
  boot) mkdir -p "$OUT/srv/logs"; echo "[Server] Done (1.0s)!" > "$L"; echo booting;;
  cmd) echo "[Server thread/INFO] [fake] $3" >> "$L";;
  stop) echo stopped;;
esac
''')
FAKE.chmod(0o755)
os.environ['QRUN_SCRATCH'] = str(FAKE)
os.environ['QRUN_SETTLE'] = '0.2'
QRUN = [sys.executable, str(HERE / 'qrun.py')]
results = []


def case(name):
	def wrap(fn):
		try:
			fn()
			results.append((name, True, ''))
		except AssertionError as e:
			results.append((name, False, str(e)))
		except Exception as e:  # noqa: BLE001
			results.append((name, False, '%s: %s' % (type(e).__name__, e)))
		return fn
	return wrap


def fake_out(name, jars=()):
	out = TMP / name
	(out / 'srv/mods').mkdir(parents=True, exist_ok=True)
	(out / 'srv/logs').mkdir(parents=True, exist_ok=True)
	(out / 'srv/logs/latest.log').write_text('[Server] Done (1.0s)!\n')
	for j in jars:
		shutil.copy2(j, out / 'srv/mods')
	qpin.pin(out)
	return out


def write_rows(name, text):
	p = ROWS / name
	p.write_text(text)
	return p


SECTION = '''
pace 0.1
@@section 1
@@title SQ-ZT Test section, 2026-11-16
@@date 2026-11-16
@@jars %s
@@tier %s
@@text intro line
@@text
@@table
'''


def row(rid, steps, extra=''):
	return '## %s | t\n@group A\n@commands c\n@pass p\n@fails f\n%s%s\n\n' % (rid, extra, steps)


@case('PROC-20 rows round trip: every queue rows block equals its .qb section; qrows check OK')
def t20():
	os.environ.pop('QROWS_DIR')
	import importlib
	importlib.reload(qrows)
	try:
		q = qrows.QUEUE.read_text().split('\n')
		n = 0
		for name, (f, sec) in qrows.sections_by_block().items():
			i = next(k for k, l in enumerate(q) if l.startswith('<!-- generated:status_index begin %s ' % name))
			j = q.index('<!-- generated:status_index end %s -->' % name, i)
			assert q[i + 1:j] == qrows.render(sec), '%s differs from its .qb section' % name
			n += 1
		assert n > 100, 'only %d rows blocks' % n
		fails, _ = qrows.check()
		assert not fails, fails[:3]
	finally:
		os.environ['QROWS_DIR'] = str(ROWS)
		importlib.reload(qrows)


@case('PROC-21 qrun runs a rows file: steps run, a draft row is listed and not run, --section selects')
def t21():
	p = write_rows('ZT.qb', SECTION % ('zjar', 'T2') + row('SQ-ZT-01', '> say hello\nexpect \\[fake\\] say hello')
				   + row('SQ-ZT-02', '', '@draft no steps yet\n'))
	out = fake_out('o21')
	res = TMP / 'r21'
	r = subprocess.run(QRUN + ['run', str(out), str(p), str(res), '--no-watch', '--section', '1'], capture_output=True,
					   text=True)
	assert 'SQ-ZT-02 DRAFT' in r.stdout, r.stdout[-400:]
	d = json.loads((res / 'results.json').read_text())
	assert d.get('SQ-ZT-01', {}).get('result') == 'PASS', d
	assert 'SQ-ZT-02' not in d
	v = json.loads((res / 'verdicts/SQ-ZT-01.json').read_text())
	assert v.get('stamps') == {}, 'verdict stamps %r' % v.get('stamps')


@case('PROC-22 plan: a one-jar stamp change plans T1 of that jar plus its T2 rows, not the others; under 1 h')
def t22():
	for f in ROWS.glob('*.qb'):
		f.unlink()
	write_rows('SMKZ.qb', SECTION % ('jara', 'T1') + row('SQ-SMKZ-01', '> say a1') + row('SQ-SMKZ-02', '> say a2')
			   + SECTION.replace('@@section 1', '@@section 2') % ('jarb', 'T1') + row('SQ-SMKZ-03', '> say b1'))
	write_rows('ZA.qb', SECTION % ('jara', 'T2') + row('SQ-ZA-01', '> say za1') + row('SQ-ZA-02', '> say za2'))
	write_rows('ZB.qb', SECTION % ('jarb', 'T2') + row('SQ-ZB-01', '> say zb1')
			   + row('SQ-ZB-02', '> say zb2', '@reviewed 2026-11-17\n'))
	ids = ['SQ-SMKZ-01', 'SQ-SMKZ-02', 'SQ-SMKZ-03', 'SQ-ZA-01', 'SQ-ZA-02', 'SQ-ZB-01', 'SQ-ZB-02']
	old_js, old_hist = qplan.jar_state, qplan.history
	qplan.jar_state = lambda: {'jara': {'id': 'A2', 'changed': time.time(), 'name': 'a.jar', 'side': 'server'},
							   'jarb': {'id': 'B1', 'changed': time.time(), 'name': 'b.jar', 'side': 'server'}}
	qplan.history = lambda: {i: ('2026-11-16', 'rz', 'PASS', {'jara': 'A1', 'jarb': 'B1'}) for i in ids}
	old_git = qrowlint.git_times
	qrowlint.git_times = lambda: {}
	qrowlint.newest_change.cache_clear()
	try:
		import io
		import contextlib
		buf = io.StringIO()
		with contextlib.redirect_stdout(buf):
			qplan.plan(['--json'])
		got = sorted(x['id'] for x in json.loads(buf.getvalue()))
		assert got == ['SQ-SMKZ-01', 'SQ-SMKZ-02', 'SQ-ZA-01', 'SQ-ZA-02'], got
		st = {x['row']['id']: x['state'] for x in qplan.row_states()}
		assert st['SQ-ZB-01'] == 'current' and st['SQ-ZA-01'] == 'jar-changed', st
		# a PASSed row rewritten later (@reviewed after the PASS), jar unchanged: rewritten, only with --rewritten
		assert st['SQ-ZB-02'] == 'rewritten', st
		buf = io.StringIO()
		with contextlib.redirect_stdout(buf):
			qplan.plan(['--json', '--rewritten'])
		got_rw = sorted(x['id'] for x in json.loads(buf.getvalue()))
		assert got_rw == sorted(got + ['SQ-ZB-02']), got_rw
		lo, hi = qplan.estimate([x for x in qplan.row_states() if x['row']['id'] in got])
		assert hi < 3600, 'estimate %d s' % hi
	finally:
		qplan.jar_state, qplan.history, qrowlint.git_times = old_js, old_hist, old_git
		qrowlint.newest_change.cache_clear()


@case('PROC-23 lint: a seeded stale row (source changed after @@reviewed) and a seeded dead id are flagged')
def t23():
	p = write_rows('ZS.qb', SECTION.replace('@@jars %s', '@@jars %s\n@@sources feature_writeup/zz_fake.md')
				   % ('jara', 'T2') + row('SQ-ZS-01', '> setblock 0 -60 0 minecraft:not_a_block_zz')
				   + row('SQ-ZS-02', '> setblock 0 -60 0 minecraft:stone'))
	old_git = qrowlint.git_times
	qrowlint.git_times = lambda: {'feature_writeup/zz_fake.md': int(time.time()) + 3 * 86400}
	qrowlint.newest_change.cache_clear()
	try:
		fs = qrowlint.lint_file(p)
		stale = [x for x in fs if 'stale' in x[3]]
		dead = [x for x in fs if 'dead id' in x[3]]
		assert len(stale) == 2 and all(x[0] == 'W' for x in stale), stale
		assert [x[2] for x in dead] == ['SQ-ZS-01'] and dead[0][0] == 'E', dead
		fs = qrowlint.lint_file(p, changed=['feature_writeup/zz_fake.md'])
		assert any(x[0] == 'E' and 'stale' in x[3] for x in fs), 'stale not an error when the source changed in session'
		# P3.4 h: a change confined to a generated block or a section 0 status paragraph is not a source change
		base = '# T\n\n## 0. At a glance\n\nText.\n'
		gen = base + '<!-- generated:status_index begin x sha1=1 -->\n| a |\n<!-- generated:status_index end x -->\n'
		sec0 = base.replace('Text.', '**README index entry, moved here 2026-11-16 (owner ruling D6).** Status: X\nmore\n\nText.')
		assert qrowlint._status_free(gen) == qrowlint._status_free(base), 'generated block counted'
		assert qrowlint._status_free(sec0) == qrowlint._status_free(base), 'section 0 paragraph counted'
		assert qrowlint._status_free(base + 'New line.\n') != qrowlint._status_free(base), 'real change ignored'
	finally:
		qrowlint.git_times = old_git
		qrowlint.newest_change.cache_clear()


def own_jar():
	for j in sorted((INST / 'mods').glob('*.jar')):
		if qpin.own_info(j):
			return j
	raise AssertionError('no own jar in mods/')


@case('PROC-24 pin: own jar links become copies; verify refuses an unlisted jar and a jar changed after prep')
def t24():
	j = own_jar()
	out = TMP / 'o24'
	(out / 'srv/mods').mkdir(parents=True)
	os.link(j, out / 'srv/mods' / j.name)
	qpin.pin(out)
	m = out / 'srv/mods' / j.name
	assert not qpin.linked(m), 'still a link'
	assert qpin.verify(out) == [], qpin.verify(out)
	with zipfile.ZipFile(out / 'srv/mods/zzunlisted-1.0.jar', 'w') as z:
		z.writestr('a.txt', 'x')
	probs = qpin.verify(out)
	assert any('zzunlisted' in p for p in probs), probs
	(out / 'srv/mods/zzunlisted-1.0.jar').unlink()
	with zipfile.ZipFile(m, 'a') as z:
		z.writestr('zz_changed.txt', 'y')
	if qpin.own_info(m)[1].startswith('sha1:'):
		probs = qpin.verify(out)
		assert any('changed since prep' in p for p in probs), probs
	else:  # stamped jar: the stamp is in ce-build.json; rewrite it
		data = {n: zipfile.ZipFile(m).read(n) for n in zipfile.ZipFile(m).namelist()}
		data['META-INF/ce-build.json'] = json.dumps({'mod_id': qpin.own_info(m)[0], 'stamp': 'zz-new'}).encode()
		with zipfile.ZipFile(m, 'w') as z:
			for n, b in data.items():
				z.writestr(n, b)
		probs = qpin.verify(out)
		assert any('changed since prep' in p for p in probs), probs


@case('PROC-25 qrun refuses to start on an unlisted jar; --no-pin runs and records it')
def t25():
	p = write_rows('ZR.qb', SECTION % ('jara', 'T2') + row('SQ-ZR-01', '> say r'))
	out = fake_out('o25')
	with zipfile.ZipFile(out / 'srv/mods/zzunlisted-1.0.jar', 'w') as z:
		z.writestr('a.txt', 'x')
	r = subprocess.run(QRUN + ['run', str(out), str(p), str(TMP / 'r25'), '--no-watch'], capture_output=True, text=True)
	assert r.returncode and 'REFUSE' in r.stdout and not (TMP / 'r25/results.json').exists(), r.stdout[-300:]
	r = subprocess.run(QRUN + ['run', str(out), str(p), str(TMP / 'r25'), '--no-watch', '--no-pin'],
					   capture_output=True, text=True)
	v = json.loads((TMP / 'r25/verdicts/SQ-ZR-01.json').read_text())
	assert '_no_pin' in (v.get('stamps') or {}), v.get('stamps')


@case('PROC-26 HARNESS: bad regex, unset capture, shell exit-only failure; a real FAIL stays FAIL')
def t26():
	p = write_rows('ZH.qb', SECTION % ('jara', 'T2')
				   + row('SQ-ZH-01', '> say x\nexpect [unclosed')
				   + row('SQ-ZH-02', '> say {ZPOS}\nexpect fake', '')
				   + row('SQ-ZH-03', '> say ok3\nexpect ok3\nshell exit 1')
				   + row('SQ-ZH-04', '> say ok4\nexpect never_printed_zz')
				   + row('SQ-ZH-05', 'capture ZPOS never_matches_(\\d+)'))
	out = fake_out('o26')
	res = TMP / 'r26'
	subprocess.run(QRUN + ['run', str(out), str(p), str(res), '--no-watch', '--rows',
						   'SQ-ZH-01,SQ-ZH-02,SQ-ZH-03,SQ-ZH-04'], capture_output=True, text=True)
	d = json.loads((res / 'results.json').read_text())
	got = {k: v['result'] for k, v in d.items()}
	assert got == {'SQ-ZH-01': 'HARNESS', 'SQ-ZH-02': 'HARNESS', 'SQ-ZH-03': 'HARNESS', 'SQ-ZH-04': 'FAIL'}, got
	v = json.loads((res / 'verdicts/SQ-ZH-01.json').read_text())
	assert v['verdict'] == 'HARNESS', v['verdict']


@case('PROC-27 judge --reclassify: refused without a cause; with one the verdict changes and checks stay')
def t27():
	res = TMP / 'r26'
	before = json.loads((res / 'verdicts/SQ-ZH-04.json').read_text())
	assert qverdict.reclassify(str(res), 'SQ-ZH-04', 'BATCH-FAULT', '') is not None
	assert qverdict.reclassify(str(res), 'SQ-ZH-04', 'BATCH-FAULT', 'qtests, 2026-11-16, seeded') is None
	after = json.loads((res / 'verdicts/SQ-ZH-04.json').read_text())
	assert after['verdict'] == 'BATCH-FAULT' and after['checks'] == before['checks'], after['verdict']
	assert after['reclassified']['from'] == 'FAIL'


@case('PROC-28 drive: state file, a killed step resumes from its first unrun row, RUNNER.md claimed and released')
def t28():
	for f in ROWS.glob('*.qb'):
		f.unlink()
	write_rows('ZD.qb', SECTION % ('jara', 'T2') + row('SQ-ZD-01', '> say d1\nexpect d1')
			   + row('SQ-ZD-02', '> say d2\nexpect d2')
			   + SECTION.replace('@@section 1', '@@section 2') % ('jara', 'T2') + row('SQ-ZD-03', '> say d3'))
	prep = TMP / 'fake_prep.sh'
	prep.write_text('#!/usr/bin/env bash\nmkdir -p "$1/srv/mods" "$1/srv/logs"\npython3 %s pin "$1"\n' % (HERE / 'qpin.py'))
	runner = TMP / 'RUNNER.md'
	runner.write_text('# claim\n\n```\nState: idle\nUpdated: x\n\nLast run: none\n```\n')
	env = dict(os.environ, QDRIVE_PREP=str(prep), QDRIVE_RUNS=str(TMP / 'runs'), QDRIVE_RUNNER=str(runner),
			   QDRIVE_NO_JVM_CHECK='1')
	rel = os.path.relpath(ROWS / 'ZD.qb', INST)
	plan = [{'id': i, 'file': rel, 'section': s, 'state': 'never', 'why': '', 'tier': 'T2', 'client': False}
			for i, s in (('SQ-ZD-01', 1), ('SQ-ZD-02', 1), ('SQ-ZD-03', 2))]
	pj = TMP / 'plan.json'
	pj.write_text(json.dumps(plan))
	r = subprocess.run(QRUN + ['drive', 'start', 'zrun', '--plan-json', str(pj)], capture_output=True, text=True,
					   env=env, timeout=300)
	st = json.loads((TMP / 'runs/zrun/drive/state.json').read_text())
	assert [s['status'] for s in st['steps']] == ['done', 'done'], (st['steps'], r.stdout[-500:], r.stderr[-500:])
	assert 'State: idle' in runner.read_text() and 'zrun finished' in runner.read_text(), runner.read_text()
	# simulate a driver killed in step 1 after its first row: drop row 2's result, mark the step running
	res = Path(st['steps'][0]['res'])
	d = json.loads((res / 'results.json').read_text())
	d.pop('SQ-ZD-02')
	(res / 'results.json').write_text(json.dumps(d))
	st['steps'][0]['status'] = 'running'
	st['status'], st['pid'] = 'running', 999999
	(TMP / 'runs/zrun/drive/state.json').write_text(json.dumps(st))
	log0 = (TMP / 'runs/zrun/drive/s00_qrun.txt').read_text()
	r = subprocess.run(QRUN + ['drive', 'resume', 'zrun'], capture_output=True, text=True, env=env, timeout=300)
	st = json.loads((TMP / 'runs/zrun/drive/state.json').read_text())
	assert [s['status'] for s in st['steps']] == ['done', 'done'], (st['steps'], r.stdout[-500:])
	new = (TMP / 'runs/zrun/drive/s00_qrun.txt').read_text()[len(log0):]
	assert 'SQ-ZD-02' in new and 'SQ-ZD-01 ...' not in new, new[-400:]


sys.path.insert(0, str(HERE.parent / 'client_capture'))
sys.path.insert(0, str(HERE.parent / 'perf/eval'))


@case('TXQ-OFF-01 qpair refusal regexes: Forge 43.3.9 handshake lines match, a plain quit does not')
def tx01():
	import re
	import qpair
	for l in ('[Netty Client IO #1/ERROR] [ne.mi.ne.NetworkRegistry/]: Terminating connection with server, '
			  'mismatched mod list',
			  'Connection closed - mismatched mod channel list', 'Failed to synchronize registry data from server',
			  'Your client is missing the following mods, install these mods to join this server:'):
		assert re.search(qpair.CLI_REFUSAL, l), l
	for l in ('Terminating connection with client, mismatched mod list',
			  'Connection closed - mismatched mod channel list'):
		assert re.search(qpair.SRV_REFUSAL, l), l
	for l in ('PerfEval lost connection: Disconnected', 'Stopping server', 'Mod rejections []'):
		assert not re.search(qpair.SRV_REFUSAL, l) and not re.search(qpair.CLI_REFUSAL, l), l


def pair_fixture(name, launch='sleep 30'):
	import qpair
	out, game = TMP / (name + '_out'), TMP / (name + '_game')
	(out / 'srv/logs').mkdir(parents=True)
	(out / 'srv/logs/latest.log').write_text('')
	(game / 'logs').mkdir(parents=True)
	(game / 'logs/latest.log').write_text('')
	os.environ['QPAIR_CLI_LAUNCH'] = launch
	return qpair.Pair(str(out), str(game), str(TMP / (name + '_res')), log=lambda *a: None), game


@case('TXQ-OFF-02 qpair: a previous launch\'s pid and SERVE heartbeat are not read as the new client\'s join')
def tx02():
	p, game = pair_fixture('stale')
	ch = game / 'cdev_channel'
	ch.mkdir()
	(ch / 'pid').write_text(str(os.getpid()))
	(ch / 'state.json').write_text(json.dumps({'phase': 'SERVE', 'time': int(time.time() * 1000)}))
	try:
		p.launch_client()
		assert not (ch / 'pid').exists() and not (ch / 'state.json').exists(), os.listdir(ch)
		out, ev = p.wait_join(3)
		assert out == 'HANG', (out, ev)
		# a heartbeat written after the launch is a join
		(ch / 'state.json').write_text(json.dumps({'phase': 'SERVE', 'time': int(time.time() * 1000)}))
		out, ev = p.wait_join(3)
		assert out == 'JOINED', (out, ev)
	finally:
		p.serve.kill()


@case('TXQ-OFF-03 qpair hold: a client that dies after joining is CRASH-CLIENT; one that lives is not')
def tx03():
	p, game = pair_fixture('hold')
	try:
		p.launch_client()
		short = subprocess.Popen(['sleep', '1'])
		p.cli_pid = short.pid
		out, ev = p.hold(6)
		short.wait()
		assert out == 'CRASH-CLIENT', (out, ev)
		p.cli_pid = p.serve.pid
		out, ev = p.hold(3)
		assert out is None, (out, ev)
	finally:
		p.serve.kill()


@case('TXQ-OFF-04 serve --drop / --config: link unlinked (source kept), toml key set, bad glob and path refused')
def tx04():
	import run_captures as rc
	game = TMP / 'drop_game'
	(game / 'mods').mkdir(parents=True)
	(game / 'config').mkdir()
	src = TMP / 'creativelabels-1.0.0.jar'
	src.write_bytes(b'jar')
	os.link(src, game / 'mods/creativelabels-1.0.0.jar')
	(game / 'config/x-client.toml').write_text('[general]\n\ta = 1\n')
	rc.apply_client_changes(str(game), ['creativelabels-*.jar'], ['config/x-client.toml:general:a=5'])
	assert not (game / 'mods/creativelabels-1.0.0.jar').exists() and src.read_bytes() == b'jar'
	assert 'a = 5' in (game / 'config/x-client.toml').read_text()
	for drops, cfgs in ((['nothing-*.jar'], []), ([], ['../escape.toml:s:k=1'])):
		try:
			rc.apply_client_changes(str(game), drops, cfgs)
			raise AssertionError('not refused: %s %s' % (drops, cfgs))
		except SystemExit:
			pass


@case('TXQ-OFF-05 xvfb display (TX-07): own Xvfb starts, reports a display, pid file written (skipped without Xvfb)')
def tx05():
	if not shutil.which('Xvfb'):
		return
	import eval_client
	game = TMP / 'xvfb_game'
	game.mkdir()
	x, disp = eval_client.start_xvfb(str(game))
	try:
		assert disp.startswith(':') and disp[1:].isdigit(), disp
		assert (game / 'xvfb.pid').read_text() == str(x.pid)
		assert x.poll() is None
	finally:
		x.kill()
		x.wait()


def main():
	try:
		for name, ok, why in results:
			print('%s %s%s' % ('PASS' if ok else 'FAIL', name, '' if ok else '\n     ' + why[:600]))
		bad = sum(1 for _, ok, _ in results if not ok)
		print('qtests: %d/%d passed' % (len(results) - bad, len(results)))
		return 1 if bad else 0
	finally:
		shutil.rmtree(TMP, ignore_errors=True)


if __name__ == '__main__':
	sys.exit(main())
