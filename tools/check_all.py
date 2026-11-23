#!/usr/bin/env python3
"""One offline check of the pack (plan_process_overhaul.md P1.1): every tools/test_*.sh plus the lints, in parallel,
each with a timeout. One line per check:

    PASS|FAIL|KNOWN|SKIP  name  seconds  first-error

Exit status 0 only when nothing FAILed (KNOWN and SKIP do not fail the run).

Usage: python3 tools/check_all.py [--fast] [--changed] [--full] [--only name,...] [-j N] [-v]
  --fast     skip checks that take over 15 s on their own (SLOW below)
  --changed  run only checks whose inputs changed since the last commit (git diff HEAD plus untracked files)
  --changed-file F  with --changed: the changed paths come from file F, one per line (Stop hook: own files only)
  --full     also run the slow report checks (texture_provenance, design_rules_audit); the release gate uses it
  --only     run just these checks
  -v         print the output tail of every FAILed or KNOWN check
  --list     print the checks and their inputs, run nothing

Inputs of a test: the paths its script names (tools/..., development/<mod>/..., kubejs/...), with
development/<mod>/build mapped to development/<mod>, plus the script itself. Any change to check_all.py,
check_known.csv or java_test_cp.sh selects every check.

Class-based tests read development/<mod>/build/classes. A mod whose sources are newer than its classes is compiled
first, in one `./gradlew :<mod>:compileJava ... --offline` call; inside Gradle (CE_IN_GRADLE=1, the install gate) a nested
Gradle run would deadlock on the build lock, so those tests SKIP with "stale classes (<project>)" instead; the gate
compiles the installing project before it runs the checks and names it in CE_INSTALL_PROJECT, so only another
project's stale classes cause a SKIP. --fast with --changed still runs a slow check whose own inputs changed. --no-compile does the same outside Gradle.

KNOWN failures: tools/check_known.csv (columns check, regex, reason, date yyyy-mm-dd). A FAILed check whose output
matches the regex counts as KNOWN. Entries older than 30 days print a warning, so accepted failures get revisited.

Gates that call this: installToMods (--fast --changed, build-logic), build_deploy.py stage (--full), the Claude Code
Stop hook (--fast --changed, report only).
"""
import argparse
import concurrent.futures as cf
import datetime as dt
import hashlib
import json
import os
import re
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TOOLS = ROOT / 'tools'
KNOWN_FILE = TOOLS / 'check_known.csv'
TIMES_FILE = ROOT / 'jar_archive' / 'check_times.json'   # last run time per check (not tracked)
FAST_LIMIT = 15.0
# Checks over FAST_LIMIT on their own; --fast skips them. Times under parallel load are higher and are only recorded
# for information (jar_archive/check_times.json).
SLOW = {'test_worldgen_tools': 75}
KNOWN_MAX_AGE = 30
GLOBAL_INPUTS = ['tools/check_all.py', 'tools/check_known.csv', 'tools/java_test_cp.sh', 'tools/pack_paths.py']

SCRIPT_TREES = ['development/', 'kubejs/data/', 'kubejs/assets/', 'kubejs/startup_scripts/', 'kubejs/server_scripts/',
				'kubejs/client_scripts/']
# every tracked top-level tree: any tracked file can carry personal data
PRIVACY_INPUTS = SCRIPT_TREES + ['tools/', 'feature_writeup/', 'config/', 'configs_edited/', 'defaultconfigs/', 'server/',
								  'deploy/', 'pack/', '.claude/', 'build-logic/', 'textureswapper/', 'texturetools/', 'CLAUDE.md',
								  'README.md', '.gitignore', 'mod_sides.md']
# name -> (command, inputs, estimate s, full only)
LINTS = {
	'id_owner_lint': (['python3', 'tools/id_owner_lint.py'], ['development/', 'kubejs/data/', 'kubejs/assets/'], 7, False),
	'dict_lint': (['python3', 'tools/dict_lint.py'], ['development/creativeCommon/', 'tools/dict_lint.py'], 1, False),
	'trade_lint': (['python3', 'tools/design_rules_audit.py', 'trades', '--fail'], SCRIPT_TREES + ['tools/design_rules_audit.py'], 2, False),
	'guard_check': (['python3', 'tools/guard_check.py'], SCRIPT_TREES + ['mods/'], 7, False),
	'optional_ref_audit': (['python3', 'tools/optional_ref_audit.py'], SCRIPT_TREES, 3, False),
	'botany_crops': (['python3', 'tools/gen_botany_crops.py', '--check'],
					 ['tools/gen_botany_crops.py', 'kubejs/generation_scripts/recipes/botany_pot_crops.csv',
					  'kubejs/data/creativecompat/recipes/botanypots/', 'kubejs/data/creativecompat/tags/items/botany_pots/',
					  'development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/',
					  'kubejs/exported/recipes/', 'kubejs/exported/crops/'], 3, False),
	'changelog_lint': (['python3', 'tools/changelog_lint.py'], ['kubejs/assets/creativecommon/changelog/',
															   'tools/changelog_lint.py'], 1, False),
	'status_index': (['python3', 'tools/status_index.py', '--check'], ['feature_writeup/', 'tools/status_index.py',
																	  'tools/scratch_query/qrows.py',
																	  'jar_archive/installed.csv'], 2, False),
	'queue_rows': (['python3', 'tools/scratch_query/qrowlint.py', '--gate', '--quiet'],
				   ['feature_writeup/testing/rows/', 'tools/scratch_query/qrowlint.py', 'tools/scratch_query/qrows.py',
					'kubejs/exported/'], 2, False),
	'texture_provenance': (['python3', 'tools/texture_provenance.py'], ['feature_writeup/texture_sources/',
																	   'textureswapper/'], 70, True),
	'design_rules_audit': (['python3', 'tools/design_rules_audit.py'], SCRIPT_TREES + ['feature_writeup/'], 30, True),
	'iso_dates_docs': (['python3', 'tools/iso_dates.py', '--lint'], ['feature_writeup/', '.claude/', 'CLAUDE.md',
																	'kubejs/assets/creativecommon/changelog/'], 17, False),
	'iso_dates_code': (['python3', 'tools/iso_dates.py', '--lint', '--scope', 'code'], SCRIPT_TREES + ['tools/'], 3, False),
	'spelling_docs': (['python3', 'tools/us_spelling.py', '--lint'], ['feature_writeup/', '.claude/', 'CLAUDE.md',
																	'kubejs/assets/'], 5, False),
	'spelling_code': (['python3', 'tools/us_spelling.py', '--lint', '--scope', 'code'], SCRIPT_TREES + ['tools/'], 14, False),
	'comment_dates': (['python3', 'tools/history_extract.py', '--lint'], SCRIPT_TREES + ['tools/', 'build-logic/'], 6, False),
	'tab_indent': (['python3', 'tools/tab_indent.py', '--lint'], SCRIPT_TREES + ['tools/', 'build-logic/', 'settings.gradle'],
				   7, False),
	'test_prefix_lint': (['python3', 'tools/test_prefix_lint.py', '--quiet'],
						 ['feature_writeup/', 'tools/test_prefix_lint.py'], 1, False),
	'plan_lint': (['python3', 'tools/plan_lint.py'], ['feature_writeup/', 'tools/plan_lint.py'], 2, False),
	'test_lines': (['python3', 'tools/test_line_fix.py', '--lint'], ['feature_writeup/', 'tools/test_line_fix.py',
															 'tools/test_prefix_lint.py'], 1, False),
	'cleanup': (['python3', 'tools/cleanup_check.py'], ['feature_writeup/', 'tools/cleanup_check.py',
														'tools/cleanup_thresholds.csv', '.claude/skills/'], 17, False),
	# every pre-2026-11-09 id of our namespaces exists now or is in a swap list (plan_rowfix_r1118.md section 4, R5);
	# inputs: the swap list folders only, not all of development/
	'old_id_check': (['python3', 'tools/old_id_check.py'], ['tools/old_id_baseline/', 'kubejs/exported/items_by_mod/',
														   'development/creativeDelight/members/display/tools/port_resources.py']
					 + sorted({str(p.relative_to(ROOT)) for p in ROOT.glob('development/**/src/main/resources/data/*/swaps')
							   if '/build/' not in str(p)}), 1, False),
	# personal data in tracked files (feature_writeup/privacy.md PV-1 to PV-9); --jars adds own jars and deploy/latest
	'privacy_lint': (['python3', 'tools/privacy_lint.py'], PRIVACY_INPUTS, 5, False),
	'privacy_lint_jars': (['python3', 'tools/privacy_lint.py', '--jars'], PRIVACY_INPUTS + ['deploy/latest/'], 8, True),
	'todo_rows': (['python3', 'tools/todo.py', 'lint', '--changed'], ['feature_writeup/todo/', 'feature_writeup/briefs/',
																	 'tools/todo.py', 'tools/todo_rows.py'], 2, False),
}
PATH_RE = re.compile(r'\b((?:development|kubejs|tools|data|config|configs_edited|generation_scripts|texturetools|textureswapper)/[A-Za-z0-9_.\-/]+)')


def test_inputs(script):
	paths = {str(script.relative_to(ROOT))}
	for m in PATH_RE.finditer(script.read_text(errors='replace')):
		p = m.group(1).rstrip('/.')
		parts = p.split('/')
		if parts[0] == 'development' and len(parts) >= 2:
			p = 'development/' + parts[1]          # build/ output follows the sources
		elif parts[0] == 'kubejs' and len(parts) >= 3:
			p = '/'.join(parts[:3])
		elif parts[0] == 'tools' and len(parts) >= 2:
			p = '/'.join(parts[:2])
		elif parts[0] in ('texturetools', 'textureswapper'):
			p = parts[0]                            # the package and its shims, whole
		paths.add(p)
	return sorted(paths)


CLASS_RE = re.compile(r'development/([A-Za-z0-9_]+)/build')


def class_mods(script):
	text = script.read_text(errors='replace')
	if 'java_test_cp.sh' in text:
		text += (TOOLS / 'java_test_cp.sh').read_text(errors='replace')
	return sorted(set(CLASS_RE.findall(text)))


def newest(path, pattern):
	best = 0.0
	for f in path.rglob(pattern):
		try:
			best = max(best, f.stat().st_mtime)
		except OSError:
			pass
	return best


def stale_mods(mods):
	out = []
	for m in mods:
		d = ROOT / 'development' / m
		if not (d / 'src').is_dir():
			continue
		if newest(d / 'src', '*.java') > newest(d / 'build' / 'classes', '*.class'):
			out.append(m)
	return out


def tracked_dirty():
	"""{path: sha1} of tracked files that differ from HEAD."""
	r = subprocess.run(['git', 'diff', '--name-only', 'HEAD'], cwd=ROOT, capture_output=True, text=True)
	out = {}
	for p in r.stdout.split('\n'):
		f = ROOT / p
		if p and f.is_file():
			try:
				out[p] = hashlib.sha1(f.read_bytes()).hexdigest()
			except OSError:
				pass
	return out


def checks():
	out = {}
	for s in sorted(TOOLS.glob('test_*.sh')):
		out[s.stem] = dict(cmd=['bash', str(s.relative_to(ROOT))], inputs=test_inputs(s), est=SLOW.get(s.stem, 10), full=False,
						   classes=class_mods(s))
	for name, (cmd, inputs, est, full) in LINTS.items():
		if not (ROOT / cmd[1]).exists():
			continue
		out[name] = dict(cmd=cmd, inputs=inputs + [cmd[1]], est=est, full=full, classes=[])
	return out


def changed_paths():
	def git(*a):
		r = subprocess.run(['git', *a], cwd=ROOT, capture_output=True, text=True)
		return [l for l in r.stdout.splitlines() if l]
	return set(git('diff', '--name-only', 'HEAD')) | set(git('ls-files', '--others', '--exclude-standard'))


def touches(inputs, changed):
	for c in changed:
		for i in inputs:
			if c == i or c.startswith(i.rstrip('/') + '/'):
				return True
	return False


def load_known():
	rows = []
	if not KNOWN_FILE.exists():
		return rows
	sys.path.insert(0, str(TOOLS))
	import table_io
	for d in table_io.read_dicts(KNOWN_FILE):
		if not d.get('check') or not d.get('regex') or not d.get('date'):
			print('WARN check_known.csv line %d: want check, regex, reason, date' % d['_line'])
			continue
		rows.append(dict(check=d['check'], pattern=re.compile(d['regex']), reason=d.get('reason', ''),
						 date=d['date'].strip()))
	return rows


def first_error(out):
	for line in out.splitlines():
		if re.search(r'(?i)\b(FAIL(?:ED)?|ERROR|Exception|Traceback|error:|missing|no rule)\b', line) \
				and not re.match(r'\s*(==|#)', line):
			return line.strip()[:160]
	lines = [l.strip() for l in out.splitlines() if l.strip()]
	return lines[-1][:160] if lines else ''


def run_one(name, c, timeout):
	t0 = time.time()
	try:
		r = subprocess.run(c['cmd'], cwd=ROOT, capture_output=True, text=True, timeout=timeout,
						   env={**os.environ, 'CHECK_ALL': '1'})
		rc, out = r.returncode, r.stdout + r.stderr
	except subprocess.TimeoutExpired as e:
		rc, out = 124, ((e.stdout or b'').decode(errors='replace') if isinstance(e.stdout, bytes) else (e.stdout or '')) \
			+ '\nERROR timeout after %d s' % timeout
	return name, rc, out, time.time() - t0


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	ap.add_argument('--fast', action='store_true')
	ap.add_argument('--changed', action='store_true')
	ap.add_argument('--changed-file', help='with --changed: take the changed paths from this file (one per line) '
					'instead of the git diff (the Stop hook passes a session\'s own files)')
	ap.add_argument('--full', action='store_true')
	ap.add_argument('--only')
	ap.add_argument('--list', action='store_true')
	ap.add_argument('-j', type=int, default=max(2, min(6, (os.cpu_count() or 4) // 2)))
	ap.add_argument('--timeout', type=int, default=300)
	ap.add_argument('-v', action='store_true')
	ap.add_argument('--no-compile', action='store_true')
	a = ap.parse_args()

	allc = checks()
	if a.list:
		for n, c in allc.items():
			print('%-24s %s%s' % (n, ' '.join(c['inputs']), '  (full only)' if c['full'] else ''))
		return 0
	try:
		times = json.loads(TIMES_FILE.read_text())
	except (OSError, ValueError):
		times = {}
	if a.changed and a.changed_file:
		changed = {l.strip() for l in open(a.changed_file) if l.strip()}
	else:
		changed = changed_paths() if a.changed else None
	all_changed = changed is not None and touches(GLOBAL_INPUTS, changed)
	selected, skipped = {}, []
	only = set(a.only.split(',')) if a.only else None
	for n, c in allc.items():
		if only is not None and n not in only:
			continue
		if c['full'] and not a.full and only is None:
			continue
		# --fast skips slow checks, except under --changed when the check's own inputs changed (P3.4 c): a worldgen
		# tool change still runs test_worldgen_tools at install. A change to the global inputs alone does not.
		own_change = changed is not None and touches(c['inputs'], changed)
		if a.fast and c['est'] > FAST_LIMIT and not own_change:
			skipped.append((n, 'slow (about %d s)' % c['est']))
			continue
		if changed is not None and not all_changed and not touches(c['inputs'], changed):
			continue
		selected[n] = c

	need = sorted({m for c in selected.values() for m in c['classes']})
	stale = stale_mods(need)
	inst = os.environ.get('CE_INSTALL_PROJECT')
	if os.environ.get('CE_IN_GRADLE') and inst:
		# the gate's `classes` dependency just compiled the installing project: its classes are current even when a
		# touched-but-unchanged source left them older (Gradle UP-TO-DATE keeps the old class files) (P3.4 b)
		stale = [m for m in stale if m != inst]
	if stale and not (a.no_compile or os.environ.get('CE_IN_GRADLE')):
		tasks = [':%s:compileJava' % m for m in stale]
		print('compiling stale classes: ' + ', '.join(stale))
		r = subprocess.run(['./gradlew', *tasks, '--offline', '-q'], cwd=ROOT, capture_output=True, text=True)
		if r.returncode != 0:
			print('WARN compile failed: ' + first_error(r.stdout + r.stderr))
		stale = stale_mods(stale)
	for n in [n for n, c in selected.items() if set(c['classes']) & set(stale)]:
		skipped.append((n, 'stale classes (%s): compile first' % ', '.join(sorted(set(selected[n]['classes']) & set(stale)))))
		del selected[n]

	known = load_known()
	today = dt.date.today()
	for k in known:
		try:
			age = (today - dt.date.fromisoformat(k['date'])).days
		except ValueError:
			print('WARN check_known.csv %s: bad date %r' % (k['check'], k['date']))
			continue
		if age > KNOWN_MAX_AGE:
			print('WARN KNOWN entry for %s is %d days old (%s): revisit it' % (k['check'], age, k['reason']))

	t0 = time.time()
	before = tracked_dirty()
	results = []
	with cf.ThreadPoolExecutor(max_workers=a.j) as ex:
		futs = [ex.submit(run_one, n, c, a.timeout) for n, c in selected.items()]
		for f in cf.as_completed(futs):
			results.append(f.result())
	failed = 0
	for name, rc, out, secs in sorted(results):
		times[name] = round(secs, 1)
		if rc == 0:
			print('PASS  %-24s %5.1f' % (name, secs))
			for line in [l for l in out.splitlines() if l.startswith('WARN')][:5]:
				print('WARN  %-24s %s' % (name, line[5:].strip()[:160]))
			continue
		k = next((k for k in known if k['check'] == name and k['pattern'].search(out)), None)
		if k:
			print('KNOWN %-24s %5.1f  %s' % (name, secs, k['reason']))
		else:
			failed += 1
			print('FAIL  %-24s %5.1f  %s' % (name, secs, first_error(out)))
		if a.v:
			for line in out.strip().splitlines()[-15:]:
				print('      | ' + line[:200])
	for n, why in skipped:
		print('SKIP  %-24s   -    %s' % (n, why))
	dirtied = sorted(p for p, h in tracked_dirty().items() if before.get(p) != h)
	if dirtied:
		# P3.4 d: a check must not write tracked files (a parallel session's edit during the run shows here too)
		word = 'FAIL ' if a.full else 'WARN '
		failed += a.full
		print('%s %-24s tracked file(s) changed during the run: %s' % (word, 'dirty_tree', ', '.join(dirtied[:5])
																		 + (' ...' if len(dirtied) > 5 else '')))
	try:
		TIMES_FILE.parent.mkdir(exist_ok=True)
		TIMES_FILE.write_text(json.dumps(times, indent=1, sort_keys=True))
	except OSError:
		pass
	if not selected:
		print('check_all: nothing to run%s' % (' (no check inputs changed)' if a.changed else ''))
	else:
		print('check_all: %d run, %d failed, %.1f s wall%s' % (len(selected), failed, time.time() - t0,
															', %d skipped' % len(skipped) if skipped else ''))
	return 1 if failed else 0


if __name__ == '__main__':
	sys.exit(main())
