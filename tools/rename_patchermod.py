#!/usr/bin/env python3
"""patcherMod -> Creative Common in text (design_rules.md DR-L2; plan_rules_fixes.md section 13, D18).

Dry run by default: prints a per-file count and writes the report (every change, and every hit left as written with
its kind) to the report path. `--apply` rewrites.

	python3 tools/rename_patchermod.py                    # dry run, report to the temp dir
	python3 tools/rename_patchermod.py --apply
	python3 tools/rename_patchermod.py --identifiers FILE # also write the hits left as written (CSV) for the rename audit

Changed: prose in docs, skills, TODO and queue rows, and in code comments and prose strings (log and chat text):
`patcherMod` -> "Creative Common"; in a path or Gradle task (`development/patcherMod/`, `:patcherMod:build`) ->
`creativeCommon`. Phrases of the 2026 swap plan are rewritten in present tense ("becomes creativecommon at the Q1
swap" goes). Left as written and listed (D18): code identifiers, strings read by code (a lone token, a path, a
regex), JSON and properties values, jar file names (`patcherMod-1.1.06.jar`), and the skip list: CLAUDE.md,
namespace_move.md, deployed changelogs, jar_archive/, deploy/, config trees, generated docs, fenced code blocks and
quoted log lines in run docs.
"""
import argparse
import csv
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import us_spelling  # noqa: E402  (code_spans)

SKIP = re.compile(r'(^|/)(exported|logs|jar_archive|build|deploy|loot_catalog|generation_scripts|moved_code_backup)/|'
				  r'_upstream/|^(config|configs_edited|defaultconfigs|server)/|'
				  r'kubejs/assets/creativecommon/changelog/(?!unreleased\.md)|^CLAUDE\.md$|namespace_move\.md$|'
				  r'^tools/rename_patchermod\.py$|agent_notes/patchermod-|feature_writeup/(plan_rules_fixes|design_rules)\.md$')
# The swap itself is named with the old name: "patcherMod became Creative Common".
# A line that names both the old and the new core (or the old name as past state) is swap history: left.
BOTH = re.compile(r'rename_patchermod|identifier rename|old name|Creative Common|creativecommon|creativeCommon|\bCommon\b|\b(was|were|is now|became|until the swap|before '
				  r'the swap)\b[^.]{0,30}patcherMod|patcherMod[^.]{0,30}\b(was|is now|became|until|before the swap)\b')
KEEP = re.compile(r'patcherMod(,? which is now| became| becomes| = | \(now)')
GENERATED = {'feature_writeup/todo/TODO.md', 'feature_writeup/testing/scratch_queue.md', 'feature_writeup/README.md'}
DOC_EXT = {'.md', '.csv', '.qb'}
CODE_EXT = {'.java', '.js', '.py', '.gradle', '.kts', '.sh'}
HASH_EXT = {'.toml', '.properties'}

PHRASES = [
	(r'patcherMod \(becomes creativecommon at the Q1 swap\)', 'Creative Common'),
	(r' ?Becomes creativecommon at the (final|Q1) swap \(creative_split\.md 7\.6 Q1\)\.', ''),
	(r' \(Creative Common after the Q1 swap\)', ''),
	(r'\bevery patcherMod that\b', 'every Creative Common version that'),
	(r'\bthe patcherMod build\b', 'the Creative Common build'),
	(r'`patcherMod`', 'Creative Common'),
]
JAR = re.compile(r'patcherMod-\d[\w.\-]*(\.jar)?')
TOKEN = re.compile(r'[\w.\-]*[/:][\w.\-/:]*patcherMod[\w.\-/:]*|[\w.\-/:]*patcherMod[\w.\-]*[/:][\w.\-/:]*')
LOGLINE = re.compile(r'^\s*(\[\d\d:\d\d|\[[A-Z][a-z]+ ?Thread|\d{4}-\d\d-\d\d[ T]\d\d:\d\d|\[main/|\[Server thread)')


def git(*a):
	return subprocess.run(['git', *a], cwd=ROOT, capture_output=True, text=True).stdout


def scope():
	files = git('grep', '-l', 'patcherMod').split('\n')
	return [f for f in files if f and not SKIP.search(f) and f not in GENERATED]


def rewrite(text):
	"""Prose rewrite of one span: -> (new, [(old, new)], [(hit, kind)] left)."""
	changes, left = [], []
	for pat, rep in PHRASES:
		for m in re.finditer(pat, text):
			changes.append((m.group(0), rep))
		text = re.sub(pat, rep, text)
	out, i = [], 0
	for m in re.finditer(r'patcherMod', text):
		if m.start() < i:
			continue
		if KEEP.match(text, m.start()):
			out.append(text[i:m.end()])
			left.append(('patcherMod', 'names the swap'))
			i = m.end()
			continue
		jar = JAR.match(text, m.start())
		if jar:
			left.append((jar.group(0), 'jar file name'))
			out.append(text[i:jar.end()])
			i = jar.end()
			continue
		tok = next((t for t in TOKEN.finditer(text) if t.start() <= m.start() < t.end()), None)
		word = tok.group(0).rstrip(':.,;') if tok else ''
		if tok and re.search(r'[/:]', word):
			new = word.replace('patcherMod', 'creativeCommon')
			out.append(text[i:tok.start()] + new)
			changes.append((word, new))
			i = tok.start() + len(word)
			continue
		out.append(text[i:m.start()] + 'Creative Common')
		changes.append(('patcherMod', 'Creative Common'))
		i = m.end()
	out.append(text[i:])
	return ''.join(out), changes, left


def process_doc(path, text):
	lines = text.split('\n')
	changes, left, fence = [], [], False
	qb = path.suffix == '.qb'
	for n, line in enumerate(lines):
		if line.lstrip().startswith('```'):
			fence = not fence
			continue
		if 'patcherMod' not in line:
			continue
		if BOTH.search(line):
			left.append((n + 1, 'patcherMod', 'swap history (names both)'))
			continue
		if fence or LOGLINE.match(line) or (qb and not re.match(r'^(@@text|@@title|@pass|@fails|@group|## )', line)):
			left.append((n + 1, 'patcherMod', 'quoted log line or code block' if not qb else 'queue row command'))
			continue
		new, c, lf = rewrite(line)
		changes += [(n + 1, o, w) for o, w in c]
		left += [(n + 1, h, k) for h, k in lf]
		lines[n] = new
	return '\n'.join(lines), changes, left


def process_code(path, text):
	changes, left, edits = [], [], []
	if path.suffix in HASH_EXT:
		spans = [(m.start(), m.end(), 'comment') for m in re.finditer(r'(?m)#.*$', text)]
	else:
		spans = us_spelling.code_spans(path, text)
	covered = set()
	for a, b, kind in spans:
		span = text[a:b]
		if 'patcherMod' not in span:
			continue
		covered.update(range(a, b))
		ln = text.count('\n', 0, a) + 1
		if any(BOTH.search(l) for l in span.split('\n') if 'patcherMod' in l):
			# names both somewhere in this span: rewrite only the lines that do not
			parts = span.split('\n')
			for k, l in enumerate(parts):
				if 'patcherMod' in l and not BOTH.search(l):
					parts[k], c, lf = rewrite(l)
					changes += [(ln + k, o, w) for o, w in c]
					left += [(ln + k, h, kd) for h, kd in lf]
				elif 'patcherMod' in l:
					left.append((ln + k, 'patcherMod', 'swap history (names both)'))
			if '\n'.join(parts) != span:
				edits.append((a, b, '\n'.join(parts)))
			continue
		if kind == 'string' and not re.search(r'[A-Za-z]{2,} +[A-Za-z]{2,}', span):
			left.append((ln, span[:80], 'string read by code'))
			continue
		new, c, lf = rewrite(span)
		changes += [(ln, o, w) for o, w in c]
		left += [(ln, h, k) for h, k in lf]
		if new != span:
			edits.append((a, b, new))
	for m in re.finditer(r'patcherMod', text):
		if m.start() not in covered:
			left.append((text.count('\n', 0, m.start()) + 1, 'patcherMod', 'identifier'))
	for a, b, new in sorted(edits, reverse=True):
		text = text[:a] + new + text[b:]
	return text, changes, left


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	ap.add_argument('--apply', action='store_true')
	ap.add_argument('--report', default=str(Path(tempfile.gettempdir()) / 'rename_patchermod_report.txt'))
	ap.add_argument('--identifiers', help='CSV of the hits left as written (file, line, hit, kind)')
	a = ap.parse_args()
	rep, kept, total = [], [], 0
	for f in scope():
		path = ROOT / f
		try:
			text = path.read_text(encoding='utf-8')
		except (UnicodeDecodeError, FileNotFoundError):
			continue
		if path.suffix in DOC_EXT:
			new, c, lf = process_doc(path, text)
		elif path.suffix in CODE_EXT or path.suffix in HASH_EXT:
			new, c, lf = process_code(path, text)
		else:
			kept += [(f, 0, 'patcherMod', f'{path.suffix or path.name} file, not text this tool edits')]
			continue
		kept += [(f, n, h, k) for n, h, k in lf]
		if not c:
			continue
		total += len(c)
		print(f'{len(c):5d}  {f}')
		rep.append(f'## {f}')
		rep += [f'  {n}: {o} -> {w}' for n, o, w in c]
		if a.apply and new != text:
			path.write_text(new, encoding='utf-8')
	rep.append('## left as written')
	rep += [f'  {f}:{n}: {h} ({k})' for f, n, h, k in kept]
	Path(a.report).write_text('\n'.join(rep) + '\n', encoding='utf-8')
	if a.identifiers:
		with open(a.identifiers, 'w', newline='', encoding='utf-8') as fh:
			w = csv.writer(fh, lineterminator='\n')
			w.writerow(['file', 'line', 'hit', 'kind'])
			w.writerows(kept)
	print(f'total: {total} {"changed" if a.apply else "to change"}; {len(kept)} left as written; report: {a.report}')
	return 0


if __name__ == '__main__':
	sys.exit(main())
