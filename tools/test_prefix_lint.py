#!/usr/bin/env python3
"""Test prefix lint (design_rules.md DR-W3b; plan_rules_fixes.md section 2).

Reads feature_writeup/testing/test_prefix_index.md (section 3 writeup prefixes, section 4 queue series) and checks:
  a. every defined test id's prefix has an index row (writeup ids in docs; queue ids in testing/rows/*.qb);
  b. a writeup id is defined only in its owner doc; quoting docs may define it too: the queue rows, run docs,
     briefs, plans, the seed docs named on the row, archive/ and status/;
  c. no prefix has two rows;
  d. no queue id is defined twice in testing/rows/*.qb (a letter suffix is its own id; an id cell that says "rerun" or
     "(rewritten" is a rerun copy, not a second definition).
A defining line is a test line `- [ ] **PREFIX-NN` (or `- [x]`, or a bare `**PREFIX-NN` line start) or a table row whose
first cell is the id. Exit 1 on any failure.

	python3 tools/test_prefix_lint.py          # summary and every failure
	python3 tools/test_prefix_lint.py --quiet  # failures count only
"""
import re
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FW = 'feature_writeup/'
INDEX = FW + 'testing/test_prefix_index.md'
ROWS = FW + 'testing/rows/'
DEF_MD = re.compile(r'^\s*(?:[-*] \[[ xX]\] )?\*\*~*([A-Z][A-Z0-9]{1,7})-(\d+[a-z]?)\b|^\|\s*\**~*([A-Z][A-Z0-9]{1,7})-(\d+[a-z]?)\**~*\s*\|')
# a rerun or rewritten copy of a row: the id cell says so (`## SQ-X-01 (rerun 5.2)`, `## SQ-X-01 rerun (I3)`,
# `## SQ-X-01 (rewritten, I2)`); it reuses the id on purpose, so it is not a second definition
RERUN = re.compile(r'\brerun\b|\(rewritten\b')
DEF_QB = re.compile(r'^## ((?:SQ|CQ)-([A-Z][A-Z0-9-]*?)-(\d+[a-z0-9]*))\b')
QUOTING = re.compile(r'^feature_writeup/(testing/|briefs/|plan_[^/]*\.md$|archive/|status/|todo/|agent_notes/|README\.md$|'
					 r'design_rules\.md$|reports?/)')
# queue sections that log results of other sections' rows (their ids quote, they do not define)
RESULTS = re.compile(r'^@@title (Run log|Results)\b')
# id forms that are not test prefixes (section 1 of the index): design rules, violations, release checks
NOT_TESTS = re.compile(r'^(DR|V|G|R\d|W\d|ST|LO|SQ|CQ)$')


def git_files(*pat):
	out = subprocess.run(['git', 'ls-files', *pat], cwd=ROOT, capture_output=True, text=True).stdout
	return [f for f in out.split('\n') if f]


def link_target(cell):
	m = re.search(r'\]\(([^)]+)\)', cell)
	if not m:
		return None
	return (Path(INDEX).parent / m.group(1)).resolve().relative_to(ROOT.resolve()).as_posix() if not m.group(1).startswith('http') else None


def read_index():
	text = (ROOT / INDEX).read_text(encoding='utf-8')
	sec3 = text.split('\n## 3.', 1)[1].split('\n## 4.', 1)[0]
	sec4 = text.split('\n## 4.', 1)[1].split('\n## History', 1)[0]
	rows = defaultdict(list)  # prefix -> [(owner, seeds)]
	for line in sec3.split('\n'):
		m = re.match(r'^\| `([A-Z][A-Z0-9]*)-` \|([^|]*)\|([^|]*)\|([^|]*)\|', line)
		if m:
			seeds = {FW + s.strip() for s in m.group(4).split(',') if s.strip().endswith('.md')}
			rows[m.group(1)].append((link_target(m.group(2)), seeds, m.group(3)))
	queue = set()
	for line in sec4.split('\n'):
		m = re.match(r'^\| `(?:SQ|CQ)-([A-Z][A-Z0-9-]*?)-` \|', line)
		if m:
			queue.add(m.group(1))
	for pre, rs in rows.items():
		for r in rs:
			queue.update(re.findall(r'(?:SQ|CQ)-([A-Z][A-Z0-9-]*?) \(', r[2]))
	return rows, queue


def main():
	quiet = '--quiet' in sys.argv
	rows, queue_series = read_index()
	fails = defaultdict(list)
	for pre, rs in rows.items():
		if len(rs) > 1:
			fails['c two rows for one prefix'].append(f'{pre}- ({len(rs)} rows)')
	defs = defaultdict(set)
	for path in git_files(FW + '*.md'):
		if path == INDEX:
			continue
		try:
			lines = (ROOT / path).read_text(encoding='utf-8').split('\n')
		except UnicodeDecodeError:
			continue
		fence = False
		for i, line in enumerate(lines, 1):
			if line.lstrip().startswith('```'):
				fence = not fence
			if fence:
				continue
			m = DEF_MD.match(line)
			if not m:
				continue
			pre = m.group(1) or m.group(3)
			num = m.group(2) or m.group(4)
			if NOT_TESTS.match(pre):
				continue
			defs[pre].add(path)
			if pre not in rows:
				fails['a prefix with no index row'].append(f'{path}:{i}: {pre}-{num}')
				continue
			owner, seeds, _ = rows[pre][0]
			if path != owner and path not in seeds and not QUOTING.search(path):
				fails['b defined outside its owner doc'].append(f'{path}:{i}: {pre}-{num} (owner {owner})')
	seen = {}
	for path in git_files(ROWS + '*.qb'):
		results = False
		for i, line in enumerate((ROOT / path).read_text(encoding='utf-8').split('\n'), 1):
			if line.startswith('@@title '):
				results = bool(RESULTS.search(line))
			m = DEF_QB.match(line)
			if not m or results:
				continue
			rid, series = m.group(1), m.group(2)
			if line.rstrip().endswith('(rerun)') or RERUN.search(line.split('|')[0]):
				continue
			if series not in queue_series and series not in rows:
				fails['a queue series with no index row'].append(f'{path}:{i}: {rid}')
			if rid in seen:
				fails['d queue id defined twice'].append(f'{path}:{i}: {rid} (also {seen[rid]})')
			else:
				seen[rid] = f'{path}:{i}'
	total = sum(len(v) for v in fails.values())
	if not quiet:
		print(f'test_prefix_lint: {len(rows)} writeup prefixes, {len(queue_series)} queue series indexed; '
			  f'{sum(len(v) for v in defs.values())} prefix/doc pairs define tests; {len(seen)} queue ids')
		for k in sorted(fails):
			print(f'## {k}: {len(fails[k])}')
			for f in fails[k][:60]:
				print('   ' + f)
			if len(fails[k]) > 60:
				print(f'   ... {len(fails[k]) - 60} more')
	print(f'test_prefix_lint: {"FAIL" if total else "PASS"} ({total} failures)')
	return 1 if total else 0


if __name__ == '__main__':
	sys.exit(main())
