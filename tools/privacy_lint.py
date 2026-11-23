#!/usr/bin/env python3
"""Privacy lint (feature_writeup/privacy.md, rules PV-1 to PV-9): personal data in anything the pack tracks or ships.

Scans every tracked text file (git ls-files) and, with --jars, the text entries of our own built jars
(development/**/build/libs/*.jar) and the staged payload in deploy/latest/ when it exists. Fails on:

  term      a personal term of this machine: the Unix user name (as a path part or `ls -l` owner), the home folder,
            the global git name and email, and every line of the untracked file .privacy_terms (owner's names,
            other people's names, server names, handles; one per line, # comments). The terms are read at run time
            and never written into a tracked file, so this lint holds no personal data itself.
  email     a mail address other than the allowed ones (GitHub noreply, git@github.com, the Claude trailer,
            example.com / example.org)
  home      an absolute home path of any user (/home/<name>/, /Users/<name>/, C:\\Users\\<name>\\) or a Claude
            Code project folder name built from one (-home-<name>-)
  path      a tracked file of a kind that holds player data (PV-3): world saves, logs, crash reports, screenshots,
            JEI per-world bookmarks, account and server lists, options.txt, permission screenshots, perf reports

Usage: python3 tools/privacy_lint.py [--jars] [-v]
  --jars  also scan own built jars and deploy/latest/ (slower; build_deploy.py runs check_all --full, which does)
  -v      print every hit (default: the first 40)

Exit 1 on any hit. A line that must keep a match (for example this lint's own patterns) carries `privacy: keep`.
"""
import argparse
import getpass
import re
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TERMS_FILE = ROOT / '.privacy_terms'
KEEP = 'privacy: keep'
ALLOWED_EMAIL = re.compile(r'(@users\.noreply\.github\.com|^git@github\.com|^noreply@anthropic\.com'  # privacy: keep
						   r'|@example\.(com|org))$', re.I)  # privacy: keep
EMAIL = re.compile(r'[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}')
HOME = re.compile(r'(/home/|/Users/|[A-Za-z]:\\\\?Users\\\\?)(?!<|\$|\{)([A-Za-z0-9._-]+)'  # privacy: keep
				  r'|-home-([A-Za-z0-9_]+)-')  # privacy: keep
HOME_OK = {'user', 'username', 'runner', 'name', 'you', 'me', 'example', 'Public', 'Shared', 'Default'}
# PV-3: kinds of files that hold players' data; never tracked, never shipped
PATHS = re.compile(r'(^|/)(saves|logs|crash-reports|screenshots|simplebackups|backups)/'
				   r'|(^|/)(options\.txt|optionsof\.txt|servers\.dat|usercache\.json|usernamecache\.json|ops\.json'
				   r'|whitelist\.json|banned-players\.json|banned-ips\.json|launcher_accounts[^/]*\.json'
				   r'|realms_persistence\.json)$'
				   r'|^config/jei/world/|^config/simplehats_hatdl/|^communications/|^server/perf/reports/'
				   r'|\.jfr$|(^|/)hs_err_pid\d+\.log$')
TEXT_EXT = {'.md', '.txt', '.csv', '.tsv', '.json', '.json5', '.toml', '.cfg', '.ini', '.properties', '.js', '.java',
			'.py', '.sh', '.ps1', '.bat', '.gradle', '.kts', '.qb', '.yml', '.yaml', '.xml', '.mcmeta', '.lang',
			'.snbt', '.nbt.txt', '.html', '.css', '.lcconfig', '.jfc', ''}


def terms():
	"""Personal terms of this machine, lower case, longest first. Read at run time, never stored in a tracked file."""
	out = set()
	user = getpass.getuser()
	if len(user) >= 3:
		out.add(user)
	for key in ('user.name', 'user.email'):
		r = subprocess.run(['git', 'config', '--global', key], cwd=ROOT, capture_output=True, text=True)
		v = r.stdout.strip()
		if v and not ALLOWED_EMAIL.search(v):
			out.add(v)
	if TERMS_FILE.exists():
		for line in TERMS_FILE.read_text(encoding='utf8').splitlines():
			line = line.split('#', 1)[0].strip()
			if len(line) >= 3:
				out.add(line)
	return sorted({t.lower() for t in out}, key=len, reverse=True)


class Patterns(list):
	"""The compiled term patterns, plus the raw terms for the whole-file prefilter."""
	terms = ()


def term_patterns(ts):
	user = getpass.getuser().lower()
	pats = Patterns()
	pats.terms = tuple(ts)
	for t in ts:
		if t == user:
			# the user name only where it names the account: a path part, a Claude project slug, `ls -l` owner
			pats.append(re.compile(r'(?i)(/%s(/|\b)|-%s-|\s%s\s+%s\s)' % ((re.escape(t),) * 4)))
		else:
			pats.append(re.compile(r'(?i)(?<![A-Za-z0-9])%s(?![A-Za-z0-9])' % re.escape(t)))
	return pats


HOME_MARKS = ('/home/', '/users/', 'users\\', '-home-')


def scan_text(name, text, pats, hits):
	# substring checks on the whole file first: most files match nothing, and the per-line loop is the slow part
	low = text.lower()
	if not EMAIL.search(text) and not any(m in low for m in HOME_MARKS) and not any(t in low for t in pats.terms):
		return
	for n, line in enumerate(text.splitlines(), 1):
		if KEEP in line:
			continue
		for p in pats:
			if p.search(line):
				hits.append((name, n, 'term', '(personal term, value not printed)'))
				break
		for m in EMAIL.finditer(line):
			if not ALLOWED_EMAIL.search(m.group(0)):
				hits.append((name, n, 'email', m.group(0)[:1] + '***@' + m.group(0).split('@', 1)[1]))
		for m in HOME.finditer(line):
			who = m.group(2) or m.group(3)
			if who and len(who) > 2 and who not in HOME_OK:
				hits.append((name, n, 'home', m.group(1) or '-home-' + '<name>'))


def tracked():
	r = subprocess.run(['git', 'ls-files', '-z'], cwd=ROOT, capture_output=True, text=True)
	return [p for p in r.stdout.split('\0') if p]


def is_text(path):
	return Path(path).suffix.lower() in TEXT_EXT


def read(path):
	try:
		data = (ROOT / path).read_bytes()
	except OSError:
		return None
	if b'\0' in data[:4096]:
		return None
	return data.decode('utf8', errors='replace')


def scan_jar(jar, pats, hits):
	try:
		with zipfile.ZipFile(jar) as z:
			for info in z.infolist():
				if info.is_dir() or not is_text(info.filename) or info.file_size > 4_000_000:
					continue
				if info.filename.endswith('.jar'):
					continue
				data = z.read(info)
				if b'\0' in data[:4096]:
					continue
				scan_text('%s!%s' % (jar.relative_to(ROOT), info.filename), data.decode('utf8', errors='replace'),
						  pats, hits)
	except zipfile.BadZipFile:
		pass


def main():
	ap = argparse.ArgumentParser()
	ap.add_argument('--jars', action='store_true')
	ap.add_argument('-v', action='store_true')
	a = ap.parse_args()
	pats = term_patterns(terms())
	hits = []
	files = tracked()
	for p in files:
		if PATHS.search(p):
			hits.append((p, 0, 'path', 'kind of file that holds player data (PV-3)'))
		if p == 'tools/privacy_lint.py' or not is_text(p):
			continue
		text = read(p)
		if text is not None:
			scan_text(p, text, pats, hits)
	if a.jars:
		for jar in sorted(ROOT.glob('development/**/build/libs/*.jar')):
			if not jar.name.endswith(('-sources.jar', '-slim.jar')):
				scan_jar(jar, pats, hits)
		latest = ROOT / 'deploy' / 'latest'
		if latest.is_dir():
			for f in sorted(latest.rglob('*')):
				rel = str(f.relative_to(ROOT))
				if f.is_file() and PATHS.search(str(f.relative_to(latest))):
					hits.append((rel, 0, 'path', 'kind of file that holds player data (PV-3)'))
				elif f.is_file() and is_text(f.name):
					text = read(rel)
					if text is not None:
						scan_text(rel, text, pats, hits)
	for h in hits[:None if a.v else 40]:
		print('FAIL %s:%d %s %s' % h)
	if len(hits) > 40 and not a.v:
		print('... %d more (-v)' % (len(hits) - 40))
	print('privacy_lint: %d file(s) scanned, %d hit(s)%s' % (len(files), len(hits), '' if a.jars else ' (tracked files only)'))
	return 1 if hits else 0


if __name__ == '__main__':
	sys.exit(main())
