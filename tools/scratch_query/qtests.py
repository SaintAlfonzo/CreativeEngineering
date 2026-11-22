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
  boot) mkdir -p "$OUT/srv/logs"; echo boot >> "$OUT/boots.txt"
    if [ -f "$OUT/srv/nodone" ]; then : > "$L"; else echo "[Server] Done (1.0s)!" > "$L"; fi; echo booting;;
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


@case('PROC-21b qrun: a repeated waitlog pattern waits for a new line, not the one an earlier waitlog matched')
def t21b():
	p = write_rows('ZW.qb', SECTION % ('zjar', 'T2') + row('SQ-ZW-01', '> say Task finished\nwaitlog Task finished 5\n'
															'> say Task finished\nwaitlog Task finished 5')
				   + row('SQ-ZW-02', '> say Task finished\nwaitlog Task finished 5\nwaitlog Task finished 2'))
	out = fake_out('o21b')
	res = TMP / 'r21b'
	subprocess.run(QRUN + ['run', str(out), str(p), str(res), '--no-watch'], capture_output=True, text=True)
	d = json.loads((res / 'results.json').read_text())
	assert d.get('SQ-ZW-01', {}).get('result') == 'PASS', d
	assert d.get('SQ-ZW-02', {}).get('result') == 'FAIL', d


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


@case('R1118-R5a install_probes: a non-_probe.js command file (ztrades.js) is found for its root (SQ-FGD-11)')
def r5a():
	import qrun
	need, roots = qrun.probes_needed([{'id': 'x', 'steps': [('cmd', 'ztrades farmer 3 20', 0)], 'meta': {}}])
	assert any(f.endswith('fungi_probe/ztrades.js') for f in need) and 'ztrades' in roots, (need, roots)


@case('R1118-R5b tag_diff: a tag and its entries renamed on 2026-11-09 compare under the old or the new name')
def r5b():
	b, a = TMP / 'tb', TMP / 'ta'
	b.mkdir()
	a.mkdir()
	(b / 'block_tags.json').write_text(json.dumps({'creativeengineering:oven_heat_sources': [
		'minecraft:fire', 'creativeengineering:dark_fire'], 'minecraft:x': ['creativeengineering:sourdough_block']}))
	(a / 'block_tags.json').write_text(json.dumps({'creativedelight:oven_heat_sources': [
		'minecraft:fire', 'creativemagic:dark_fire'], 'minecraft:x': ['creativedelight:sourdough_block']}))
	for spec in ('block:creativeengineering:oven_heat_sources', 'block:creativedelight:oven_heat_sources', 'block:minecraft:x'):
		r = subprocess.run([sys.executable, str(HERE / 'tag_diff.py'), str(b), str(a), spec], capture_output=True, text=True)
		assert ': same (' in r.stdout, (spec, r.stdout, r.stderr)
	r = subprocess.run([sys.executable, str(HERE / 'tag_diff.py'), str(b), str(a), 'block:minecraft:x', '--no-rename'],
					   capture_output=True, text=True)
	assert 'only before' in r.stdout, r.stdout


@case('R1118-A2 restart: edit lines run with the server down, the row and batch go on on the new boot, log kept')
def ra2():
	p = write_rows('ZR.qb', SECTION % ('zjar', 'T2')
				   + row('SQ-ZR-01', '> say before\nedit echo "$OUT" > edited.txt\nrestart 20\n> say after\n'
							'expect say before\nexpect say after\nbootreject say before\nwaitlog say after 5')
				   + row('SQ-ZR-02', '> say next\nexpect say next\nreject say before'))
	out = fake_out('or2')
	res = TMP / 'rr2'
	r = subprocess.run(QRUN + ['run', str(out), str(p), str(res)], capture_output=True, text=True, timeout=120)
	d = json.loads((res / 'results.json').read_text())
	assert d.get('SQ-ZR-01', {}).get('result') == 'PASS', (d, r.stdout[-600:])
	assert d.get('SQ-ZR-02', {}).get('result') == 'PASS', (d, r.stdout[-600:])
	assert (out / 'srv/edited.txt').read_text().strip() == str(out), 'edit ran with OUT set, cwd srv'
	assert 'say before' in (out / 'srv/logs/qrun_boot1.log').read_text()
	assert (out / 'boots.txt').read_text().count('boot') == 1, 'fake boots: one restart'
	v = json.loads((res / 'verdicts/SQ-ZR-01.json').read_text())
	assert [c['step'] for c in v['checks']][:2] == ['edit', 'restart'], v['checks']
	# no Done after the restart: the row FAILs, the batch ends before the next row
	(out / 'srv/nodone').write_text('')
	res2 = TMP / 'rr2b'
	p2 = write_rows('ZR2.qb', SECTION % ('zjar', 'T2') + row('SQ-ZR-03', '> say x\nrestart 3')
					+ row('SQ-ZR-04', '> say y'))
	r = subprocess.run(QRUN + ['run', str(out), str(p2), str(res2), '--no-watch'], capture_output=True, text=True,
					   timeout=120)
	d = json.loads((res2 / 'results.json').read_text())
	assert d.get('SQ-ZR-03', {}).get('result') == 'FAIL' and 'SQ-ZR-04' not in d, (d, r.stdout[-400:])
	assert 'ABORT: restart failed' in r.stdout, r.stdout[-400:]


@case('R1118-A1 boot phases: @boot rows run on boots 1..N of one folder, @@prep boot=N: props / toml / run / fresh '
	  'before boot N, lower-boot rows join as setup; C1 run gets $BOOT and $OUT; lint E / W boot')
def ra1():
	import qrows
	import qlint
	import qrun
	for f in ROWS.glob('*.qb'):
		f.unlink()
	sec = SECTION % ('jara', 'T2') + ('@@prep boot=2: toml x.toml s k 5\n@@prep boot=2: run echo "$BOOT $OUT" > ran.txt\n'
									  '@@prep boot=2: difficulty=hard\n@@prep boot=2: fresh\n')
	p = write_rows('ZB.qb', sec + row('SQ-ZB-01', '> say b1\nexpect say b1')
				   + row('SQ-ZB-02', '> say b2\nexpect say b2\nreject say b1\nshell grep -q "k = 5" config/x.toml\n'
							'shell test -f ran.txt', '@boot 2\n')
				   + row('SQ-ZB-03', '> say b1b\nexpect say b1b', '@boot 1\n'))
	fs = qlint.lint_rows(qrun.parse(str(p)), qrun.parse(str(p)))
	assert any(x[:3] == ('W', 'boot', 'SQ-ZB-03') for x in fs), fs
	bad = write_rows('ZBX.qb', SECTION % ('jara', 'T2') + row('SQ-ZBX-01', '> say x', '@boot two\n'))
	assert any(x[:2] == ('E', 'boot') for x in qlint.lint_rows(qrun.parse(str(bad)), qrun.parse(str(bad)))), 'E boot'
	bad.unlink()
	for txt, ok in (('boot=2: NOSWAP=1', False), ('fresh', False), ('boot=3: A: run true', True), ('B: boot=2: fresh', True)):
		try:
			qrows.prep_line_boot(txt)
			assert ok, txt
		except ValueError:
			assert not ok, txt
	sec1 = qrows.section_of(p, 1)
	assert qrows.prep_of(sec1, 'A') == {'env': {}, 'props': [], 'actions': [], 'lines': []}, 'boot=2 lines stay out of boot 1'
	assert [k for k, _ in qrows.prep_of(sec1, 'A', 2)['actions']] == ['toml', 'run', 'fresh']
	prep = TMP / 'fake_prep_b.sh'
	prep.write_text('#!/usr/bin/env bash\nmkdir -p "$1/srv/mods" "$1/srv/logs" "$1/srv/config"\n'
					'echo level-name=$2 > "$1/srv/server.properties"\npython3 %s pin "$1"\n' % (HERE / 'qpin.py'))
	runner = TMP / 'RUNNER_b.md'
	runner.write_text('# claim\n\n```\nState: idle\nUpdated: x\n\nLast run: none\n```\n')
	env = dict(os.environ, QDRIVE_PREP=str(prep), QDRIVE_RUNS=str(TMP / 'runs'), QDRIVE_RUNNER=str(runner),
			   QDRIVE_NO_JVM_CHECK='1')
	rel = os.path.relpath(p, INST)
	pj = TMP / 'plan_b.json'
	pj.write_text(json.dumps([{'id': 'SQ-ZB-02', 'file': rel, 'section': 1, 'state': 'never', 'why': '', 'tier': 'T2',
							   'client': False}]))
	r = subprocess.run(QRUN + ['drive', 'start', 'zboot', '--plan-json', str(pj)], capture_output=True, text=True,
					   env=env, timeout=300)
	st = json.loads((TMP / 'runs/zboot/drive/state.json').read_text())
	s0 = st['steps'][0]
	assert s0['rows'] == ['SQ-ZB-01', 'SQ-ZB-03', 'SQ-ZB-02'] and s0['setup'] == ['SQ-ZB-01', 'SQ-ZB-03'], s0
	assert s0['status'] == 'done', (s0, r.stdout[-600:], r.stderr[-600:])
	out = Path(s0['out'])
	assert (out / 'boots.txt').read_text().count('boot') == 2, 'two boots'
	assert (out / 'srv/ran.txt').read_text().split() == ['2', str(out.resolve())], (out / 'srv/ran.txt').read_text()
	props = (out / 'srv/server.properties').read_text()
	assert 'difficulty=hard' in props and 'level-name=zboot_s00_b2' in props, props
	res = json.loads((Path(s0['res']) / 'results.json').read_text())
	assert all(res[k]['result'] == 'PASS' for k in ('SQ-ZB-01', 'SQ-ZB-02', 'SQ-ZB-03')), res
	# resume after boot 1: the boot-2 prep is applied once, boot 1 is not rerun
	res.pop('SQ-ZB-02')
	(Path(s0['res']) / 'results.json').write_text(json.dumps(res))
	(out / 'srv/ran.txt').unlink()
	s0['status'], s0['prepped'] = 'running', 2
	st['status'], st['pid'] = 'running', 999999
	(TMP / 'runs/zboot/drive/state.json').write_text(json.dumps(st))
	r = subprocess.run(QRUN + ['drive', 'resume', 'zboot'], capture_output=True, text=True, env=env, timeout=300)
	st = json.loads((TMP / 'runs/zboot/drive/state.json').read_text())
	assert st['steps'][0]['status'] == 'done', (st['steps'][0], r.stdout[-500:])
	assert (out / 'boots.txt').read_text().count('boot') == 3 and not (out / 'srv/ran.txt').exists(), 'one more boot only'


@case('R1118-B1 mod sets: manifests resolve (groups, -@group, extends), pack / CEPortTesting builds prune and pin '
	  '(unlisted_ok), @@modset steps get their own key, boot=N: modset switches jars with the world kept')
def rb1():
	import qmodset
	import qrows
	for n in qmodset.manifests():
		for v in sorted(qmodset.load(n).get('variants', {})) or [None]:
			qmodset.resolve(qmodset.parse_spec(n + (' ' + v if v else '')))
	r = qmodset.resolve(qmodset.parse_spec('cc_only full -@iw'))
	assert 'immersive_weathering-*' not in r['keep'] and 'moonlight-*' in r['keep'] and 'heartstone-*' in r['keep'], r
	assert qmodset.resolve(qmodset.parse_spec('cco_sets without_trims'))['m']['config'] == 'empty', 'extends cc_only'
	assert qmodset.resolve(qmodset.parse_spec('port_chain optional'))['add'][:2] == ['alexsmobs-*.jar', 'citadel-*.jar']
	for bad in ('nosuch', 'cc_only nosuchvariant', 'cc_only full -@nogroup', 'cc_only full kubejs=bogus', 'pack x'):
		try:
			qmodset.parse_spec(bad)
			raise AssertionError('accepted ' + bad)
		except ValueError:
			pass
	try:
		qrows.prep_line_boot('modset cc_only full')
		raise AssertionError('modset at boot 1 accepted')
	except ValueError:
		pass
	assert qrows.prep_line_boot('boot=2: modset pack')[2:] == ('modset', ['pack'])
	# fixtures: manifests, a fake prep_srv.sh / scratch_server.sh setup, a fake CEPortTesting
	md, port = TMP / 'modsets', TMP / 'port'
	md.mkdir()
	(port / 'mods').mkdir(parents=True)
	(port / 'kubejs/server_scripts').mkdir(parents=True)
	(port / 'kubejs/server_scripts/example.js').write_text('//')
	for j in ('xa-1.0.jar', 'xb-1.0.jar', 'xc-1.0.jar'):
		(port / 'mods' / j).write_text(j)
	(md / 'zpack.toml').write_text('source = "pack"\nkubejs = "probes"\nconfig = "empty"\ndefault = "v"\n'
								   '[groups]\nkeep = ["ja-*", "jb-*"]\n[variants.v]\nkeep = ["@keep"]\n'
								   '[variants.w]\nkeep = ["ja-*"]\n')
	(md / 'zport.toml').write_text('source = "ceporttesting"\nkubejs = "source"\nunlisted_ok = true\n'
								   'require = ["xa-*"]\n[variants.v]\nkeep = ["xa-*", "xb-*"]\n')
	(md / 'zport2.toml').write_text('extends = "zport"\nunlisted_ok = false\n')
	prep = TMP / 'fake_prep_m.sh'
	prep.write_text('#!/usr/bin/env bash\nS="$1/srv"; mkdir -p "$S/mods" "$S/logs" "$S/config" "$S/kubejs/server_scripts" '
					'"$S/kubejs/data/zq" "$S/kubejs/data/other"\nfor j in ja-1.0.jar jb-1.0.jar jc-1.0.jar; do '
					'echo $j > "$S/mods/$j"; done\necho x > "$S/config/a.toml"\necho p > "$S/kubejs/server_scripts/zz_q_probe.js"\n'
					'echo s > "$S/kubejs/server_scripts/pack.js"\necho level-name=$2 > "$S/server.properties"\n'
					'python3 %s pin "$1"\n' % (HERE / 'qpin.py'))
	setup = TMP / 'fake_setup.sh'
	setup.write_text('#!/usr/bin/env bash\nS="$2/srv"; mkdir -p "$S/mods" "$S/logs" "$S/kubejs/server_scripts" "$S/config"\n'
					 'echo l > "$S/kubejs/server_scripts/zz_cl_probe.js"\necho level-name=$3 > "$S/server.properties"\n')
	env = dict(QMODSET_DIR=str(md), QDRIVE_PREP=str(prep), QMODSET_SETUP=str(setup), QMODSET_PORT=str(port),
			   QDRIVE_NO_JVM_CHECK='1')
	old = {k: os.environ.get(k) for k in env}
	os.environ.update(env)
	try:
		import importlib
		importlib.reload(qmodset)
		out = TMP / 'om1'
		rc, text = qmodset.build(out, 'lv', 'A', 'zpack')
		assert rc == 0, text
		srv = out / 'srv'
		assert sorted(p.name for p in (srv / 'mods').glob('*.jar')) == ['ja-1.0.jar', 'jb-1.0.jar'], text
		assert not list((srv / 'config').iterdir()) and (srv / 'defaultconfigs').is_dir(), 'config empty'
		ss = sorted(p.name for p in (srv / 'kubejs/server_scripts').iterdir())
		assert ss == ['zz_q_probe.js'] and (srv / 'kubejs/data/zq').is_dir() and not (srv / 'kubejs/data/other').exists(), ss
		assert json.loads((out / 'modset.json').read_text())['spec'] == 'zpack'
		out2 = TMP / 'om2'
		rc, text = qmodset.build(out2, 'lv', 'B', 'zport v -xb-1.0.jar')
		assert rc == 0, text
		assert sorted(p.name for p in (out2 / 'srv/mods').glob('*.jar')) == ['xa-1.0.jar'], text
		assert (out2 / 'srv/kubejs/server_scripts/example.js').exists(), 'kubejs from the source'
		assert not (out2 / 'srv/kubejs/server_scripts/zz_cl_probe.js').exists(), 'Labels probe dropped without Labels'
		assert 'level-seed=8675309' in (out2 / 'srv/server.properties').read_text(), 'group B props'
		assert qpin.verify(out2) == [], qpin.verify(out2)
		out3 = TMP / 'om3'
		assert qmodset.build(out3, 'lv', 'A', 'zport2 v')[0] == 0
		assert any('no mod_sides.md row' in x for x in qpin.verify(out3)), 'unlisted_ok false refuses'
		assert qmodset.build(TMP / 'om4', 'lv', 'A', 'zport v -xa-1.0.jar')[0] == 1, 'require fails the build'
		(srv / 'world').mkdir()
		qmodset.switch(out, 'zpack w')
		assert sorted(p.name for p in (srv / 'mods').glob('*.jar')) == ['ja-1.0.jar'] and (srv / 'world').is_dir()
		# driver: a @@modset section and a @modset none row are two steps; the modset step builds through qmodset
		for f in ROWS.glob('*.qb'):
			f.unlink()
		p = write_rows('ZM.qb', SECTION.replace('@@date', '@@modset zport v\n@@date') % ('jara', 'T2')
					   + row('SQ-ZM-01', '> say m1\nexpect say m1') + row('SQ-ZM-02', '> say m2', '@modset none\n'))
		rel = os.path.relpath(p, INST)
		import qdrive
		steps, _ = qdrive.build_steps([{'id': i, 'file': rel, 'section': 1, 'state': 'never', 'why': '', 'tier': 'T2',
										'client': False} for i in ('SQ-ZM-01', 'SQ-ZM-02')])
		assert [s.get('modset') for s in steps] == ['zport v', None] and steps[0]['key'].endswith('#modset=zport v'), steps
		runner = TMP / 'RUNNER_m.md'
		runner.write_text('# claim\n\n```\nState: idle\nUpdated: x\n\nLast run: none\n```\n')
		pj = TMP / 'plan_m.json'
		pj.write_text(json.dumps([{'id': 'SQ-ZM-01', 'file': rel, 'section': 1, 'state': 'never', 'why': '',
								   'tier': 'T2', 'client': False}]))
		r = subprocess.run(QRUN + ['drive', 'start', 'zmod', '--plan-json', str(pj)], capture_output=True, text=True,
						   env=dict(os.environ, QDRIVE_RUNS=str(TMP / 'runs'), QDRIVE_RUNNER=str(runner)), timeout=300)
		st = json.loads((TMP / 'runs/zmod/drive/state.json').read_text())
		assert st['steps'][0]['status'] == 'done', (st['steps'][0]['status'],
													(TMP / 'runs/zmod/drive/s00_qrun.txt').read_text()[-600:])
		assert 'modset zport v' in (TMP / 'runs/zmod/drive/s00_prep.txt').read_text()
		assert sorted(x.name for x in (Path(st['steps'][0]['out']) / 'srv/mods').glob('*.jar')) == ['xa-1.0.jar', 'xb-1.0.jar']
	finally:
		for k, v in old.items():
			if v is None:
				os.environ.pop(k, None)
			else:
				os.environ[k] = v
		importlib.reload(qmodset)


@case('R1118-F25 driver follow-ups: a deploy mod set keeps prep_srv.sh\'s perfBot (dev_keep) and pins clean; '
	  'W boot keys on modset / phase and skips client and draft rows; qrun.py toml section - sets top-level keys')
def rf25():
	import importlib
	import qlint
	import qmodset
	import qrun
	# W boot: another @modset (row or section), a client row and a draft are not ordered against boot 2
	p = write_rows('ZW.qb', SECTION % ('jara', 'T2') + row('SQ-ZW-01', '> say a', '@boot 2\n')
				   + row('SQ-ZW-02', '> say b', '@modset other\n') + row('CQ-ZW-03', '> say c')
				   + row('SQ-ZW-04', '> say d', '@draft x\n') + row('SQ-ZW-05', '> say e', '@phase worldgen\n')
				   + row('SQ-ZW-06', '> say f')
				   + SECTION.replace('@@section 1', '@@section 2').replace('@@date', '@@modset other\n@@date')
				   % ('jara', 'T2') + row('SQ-ZW-11', '> say g', '@boot 2\n') + row('SQ-ZW-12', '> say h', '@modset none\n')
				   + row('SQ-ZW-13', '> say i'))
	rows = qrun.parse(str(p), errors=[])
	w = sorted(x[2] for x in qlint.lint_rows(rows, rows) if x[:2] == ('W', 'boot'))
	assert w == ['SQ-ZW-06', 'SQ-ZW-13'], w
	p.unlink()
	# toml section '-': replace a top-level key, insert one before the first [section], leave sections alone
	t = TMP / 'top.toml'
	t.write_text('# c\nfoo = 1\n\n[a]\n\tfoo = 2\n')
	for k, v in (('foo', '5'), ('bar', 'true')):
		subprocess.run(QRUN + ['toml', str(t), '-', k, v], check=True, capture_output=True)
	assert t.read_text() == '# c\nfoo = 5\n\nbar = true\n\n[a]\n\tfoo = 2\n', t.read_text()
	# deploy mod set: release jars over the pack, the release filter keeps perfBot unless dev_keep = []
	md, dep = TMP / 'modsets_f25', TMP / 'deploy_f25'
	md.mkdir()
	(dep / 'mods').mkdir(parents=True)
	(dep / 'verify').mkdir()
	(dep / 'mods/ja-2.0.jar').write_text('ja2')
	(dep / 'verify/server.installed.sha1').write_text('abc  mods/ja-2.0.jar\n')
	(md / 'zdep.toml').write_text('source = "deploy"\nunlisted_ok = true\n')
	(md / 'zdep0.toml').write_text('extends = "zdep"\ndev_keep = []\n')
	prep = TMP / 'fake_prep_f25.sh'
	prep.write_text('#!/usr/bin/env bash\nS="$1/srv"; mkdir -p "$S/mods" "$S/logs" "$S/config" "$S/kubejs/server_scripts"\n'
					'for j in ja-1.0.jar jb-1.0.jar cdevperfbot-0.1.0.jar; do echo $j > "$S/mods/$j"; done\n'
					'echo "-Dcdevperfbot.out=perf/bot" >> "$S/user_jvm_args.txt"\n'
					'echo level-name=$2 > "$S/server.properties"\npython3 %s pin "$1"\n' % (HERE / 'qpin.py'))
	env = dict(QMODSET_DIR=str(md), QDRIVE_PREP=str(prep), QMODSET_DEPLOY=str(dep), QDRIVE_NO_JVM_CHECK='1')
	old = {k: os.environ.get(k) for k in env}
	os.environ.update(env)
	try:
		importlib.reload(qmodset)
		out = TMP / 'of25'
		rc, text = qmodset.build(out, 'lv', 'A', 'zdep')
		assert rc == 0, text
		assert sorted(x.name for x in (out / 'srv/mods').glob('*.jar')) == ['cdevperfbot-0.1.0.jar', 'ja-2.0.jar'], text
		assert 'dev jar kept: cdevperfbot-0.1.0.jar' in text and 'not in the release: jb-1.0.jar' in text, text
		assert qpin.verify(out) == [], qpin.verify(out)
		out0 = TMP / 'of25b'
		assert qmodset.build(out0, 'lv', 'A', 'zdep0')[0] == 0
		assert sorted(x.name for x in (out0 / 'srv/mods').glob('*.jar')) == ['ja-2.0.jar']
	finally:
		for k, v in old.items():
			if v is None:
				os.environ.pop(k, None)
			else:
				os.environ[k] = v
		importlib.reload(qmodset)


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


@case('PROC-29 forceload release plan: a box goes after its last user row; overlaps, placeholders and ~ hold it')
def t29():
	import qforceload
	mk = lambda i, cmds: {'id': i, 'section': '1', 'meta': {}, 'steps': [('cmd', c, 0) for c in cmds]}  # noqa: E731
	rows = [mk('A', ['forceload add 0 0 31 31', 'setblock 5 -60 5 stone']), mk('B', ['setblock 20 -60 20 dirt']),
			mk('C', ['execute in minecraft:the_end run forceload add 1000 1000']), mk('D', ['say x'])]
	rel, dims = qforceload.plan(rows)
	assert rel == {'B': ['forceload remove 0 0 31 31'],
				   'C': ['execute in minecraft:the_end run forceload remove 1000 1000']}, rel
	assert dims == {'minecraft:overworld', 'minecraft:the_end'}, dims
	rows[3] = mk('D', ['setblock {HX} {HY} {HZ} stone'])
	assert qforceload.plan(rows)[0].get('D') == ['forceload remove 0 0 31 31'], 'placeholder did not hold the box'
	rows[3] = mk('D', ['forceload add 16 16 40 40'])
	assert qforceload.plan(rows)[0].get('D') == ['forceload remove 0 0 31 31', 'forceload remove 16 16 40 40'], \
		'overlapping boxes not released together'
	rows[3] = mk('D', ['forceload add {X} {Z}'])
	rel = qforceload.plan(rows)[0]
	assert 'D' not in rel and rel.get('C') and 'B' not in rel, 'placeholder box: %r' % rel
	rows.append(mk('E', ['forceload add 900 900', 'setblock 900 -60 900 stone']))
	assert 'E' not in qforceload.plan(rows)[0], 'a box after a placeholder box was released'
	rows[3] = mk('D', ['forceload remove all'])
	assert 'B' not in qforceload.plan(rows)[0], 'remove all did not end the box'


@case('PROC-29b qrun run releases forceloads: remove after the last user row, remove all at the end; --keep-forceloads')
def t29b():
	p = write_rows('ZFL.qb', SECTION % ('zjar', 'T2')
				   + row('SQ-ZFL-01', '> forceload add 0 0 31 31\n> setblock 5 -60 5 stone')
				   + row('SQ-ZFL-02', '> setblock 20 -60 20 dirt')
				   + row('SQ-ZFL-03', '> say last'))
	for name, extra, want in (('o29b', [], True), ('o29c', ['--keep-forceloads'], False)):
		out = fake_out(name)
		subprocess.run(QRUN + ['run', str(out), str(p), str(TMP / ('r' + name)), '--no-watch'] + extra,
					   capture_output=True, text=True)
		log = (out / 'srv/logs/latest.log').read_text().splitlines()
		cmds = [ln.split('[fake] ', 1)[1] for ln in log if '[fake] ' in ln]
		if not want:
			assert not any('remove' in c for c in cmds), cmds
			continue
		i_rm = cmds.index('forceload remove 0 0 31 31')
		assert cmds.index('setblock 20 -60 20 dirt') < i_rm < cmds.index('say last'), cmds
		assert cmds[-1] == 'forceload remove all', cmds


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
