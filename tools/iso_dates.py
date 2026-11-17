#!/usr/bin/env python3
"""Rewrite year-less and non-ISO dates in docs to ISO 8601 (design_rules.md DR-L4b, DR-L5; plan_rules_fixes.md section 15).

Dry run by default: prints a per-file summary and writes a full report (every change and every unresolved hit) to
the report path. `--apply` rewrites the files.

	python3 tools/iso_dates.py                     # dry run over the doc scope, report to the temp dir
	python3 tools/iso_dates.py --apply             # rewrite
	python3 tools/iso_dates.py --report out.txt    # report path
	python3 tools/iso_dates.py --scope code        # code scope (plan phase P5), same rules
	python3 tools/iso_dates.py --lint              # list remaining hits (for design_rules_audit.py), no report file

Forms handled:
- year-less `MM-DD` (`11-14`, `11-14b` run suffixes) -> `2026-11-14` when the context marks it as a date (a citation,
  "since", "on", a bullet or table cell start, a following colon); other hits are listed as ambiguous, not changed.
- month names (`Nov 14`, `14 November`) -> ISO, for September to December only.
- `YYYY.MM.DD` and `YYYY/MM/DD` -> `YYYY-MM-DD`.
- citations `(user, date)`, `(owner 11-05)`, `(user 2026-11-05)` -> `(owner, 2026-11-05)` (D9).
Year: every year-less date is 2026 (D5). A date later than the file's last change (last commit, or mtime when the file
is modified or untracked) is impossible and goes to the owner list unchanged.
Skipped: fenced code blocks, inline code spans, link targets, deployed changelog files (DR-P9), exported/, logs/,
jar_archive/, version strings (`25.12.09`).
"""
import argparse
import csv
import io
import datetime as dt
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MEMORY = Path.home() / '.claude/projects/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/memory'
YEAR = 2026
SKIP = re.compile(r'(^|/)(exported|logs|jar_archive|build|deploy|loot_catalog)/|_upstream/|kubejs/assets/creativecommon/changelog/(?!unreleased\.md)')
# Generated from other files (rows, .qb): converting the source is enough.
GENERATED = {'feature_writeup/todo/TODO.md', 'feature_writeup/testing/scratch_queue.md'}
MONTHS = {m: i for i, m in enumerate(['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'], 1)}
MON = r'(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|June?|July?|Aug(?:ust)?|Sept?(?:ember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)'

# MM-DD with no year: not glued to digits, words, dots, slashes or hyphens (so 1.19.2, v2-11 never match).
MMDD = re.compile(r'(?<![\w./\-:#])(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])(?P<suf>[a-d])?(?![\w/\-]|\.\d)')
# Context that marks a bare MM-DD as a date.
CUE_BEFORE = re.compile(r'(\(|\b(owner|user|since|on|until|till|before|after|dated|as of|of|built|fixed|added|deployed|'
						r'tested|run|ran|written|moved|set|decided|ruling|rulings|confirmed|updated|landed|merged|pushed|logged|'
						r'noted|released|reported|created|planned|passed|failed|session|audit|scan|PASSED|BUILT|FIXED|DEPLOYED|'
						r'VERIFIED|EXECUTED|Built|Fixed|Added|Deployed|Tested|Owner|User|Since|On|r\d{4}|and|to|the|by|from the|'
						r'Written|Updated|Planned|Decided|Confirmed|night|evening|morning|answered|rev|accepted|again|The|ported|Done|'
						r'applied|installed|investigated|Logged|Displaced|seen|newest|triage|between|later|Until|stable|OK|update|'
						r'History:|weeks:|raised|notes|dump|jar|run)[ ,]+|(\d\d-\d\d[a-d]?\**(,| /|;| and| or| to)\s*)|^\s*(?:[-*]|\d+\.)\s*\**|^\s*\*\*|'
						r'@@date\s+)$')
CUE_AFTER = re.compile(r'^([a-d])?(\**:|\)|\s*(run|ruling|rulings|session|owner|build|plan|audit|brief|notes?|decision|'
						r'decisions|pass|scan|batch|wave|review|evening|morning|night|work|change|changes|fix|fixes|'
						r'deploy|release|snapshot|test|tests|dump|dumps|row|retest|census|jar|merge|move|index|crashes|\d\d:\d\d)\b|\s*\()')
MONTH_D = re.compile(r'(?<![\w-])' + MON + r'\.? (\d{1,2})(?:st|nd|rd|th)?(?:,? (\d{4}))?(?![\w:])')
D_MONTH = re.compile(r'(?<![\w.-])(\d{1,2}) ' + MON + r'(?: (\d{4}))?(?![\w-])')
DOTTED = re.compile(r'(?<![\w.])(20\d\d)[./](0[1-9]|1[0-2])[./](0[1-9]|[12]\d|3[01])(?![\w.])')
CITE = re.compile(r'\((user|owner|User|Owner),?\s+((?:20\d\d-)?(?:0[1-9]|1[0-2])-(?:0[1-9]|[12]\d|3[01]))([a-d])?(\)|[,;])')


def git(*a):
	return subprocess.run(['git', *a], cwd=ROOT, capture_output=True, text=True).stdout


def doc_scope():
	files = git('ls-files', 'feature_writeup/*.md', '.claude/*.md', 'CLAUDE.md', 'mod_sides.md', 'README.md',
				'feature_writeup/todo/rows/*.csv', 'feature_writeup/status/features.csv', 'feature_writeup/testing/rows/*.qb',
				'kubejs/assets/creativecommon/changelog/unreleased.md').split('\n')
	out = [ROOT / f for f in files if f and not SKIP.search(f) and f not in GENERATED]
	if MEMORY.is_dir():
		out += sorted(MEMORY.glob('*.md'))
	return out


def code_scope():
	files = git('ls-files', '*.java', '*.js', '*.py', '*.gradle', '*.kts', '*.sh').split('\n')
	return [ROOT / f for f in files if f and not SKIP.search(f) and '/kubejs/generation_scripts/' not in '/' + f]


def last_change(path):
	"""Latest date the file could have been written: last commit, or mtime when modified, untracked or outside git."""
	mtime = dt.date.fromtimestamp(path.stat().st_mtime)
	try:
		rel = path.relative_to(ROOT).as_posix()
	except ValueError:
		return mtime
	if git('status', '--porcelain', '--', rel).strip():
		return mtime
	d = git('log', '-1', '--format=%cs', '--', rel).strip()
	return dt.date.fromisoformat(d) if d else mtime


def mask(line):
	"""Blank out inline code spans and link targets so their text is never changed."""
	def blank(m):
		return m.group(0)[0] + ' ' * (len(m.group(0)) - 2) + m.group(0)[-1]
	line = re.sub(r'`[^`]*`', blank, line)
	return re.sub(r'\]\([^)]*\)', blank, line)


def check(month, day, limit):
	try:
		d = dt.date(YEAR, month, day)
	except ValueError:
		return None, 'not a calendar date'
	if d > limit:
		return None, f'later than the file\'s last change {limit}'
	return d.isoformat(), None


def convert_line(line, limit, lint=False):
	"""Returns (new_line, changes, unresolved, ambiguous); each list holds (old, new_or_reason)."""
	changes, unresolved, ambiguous = [], [], []
	masked = mask(line)
	edits = []  # (start, end, text)

	def taken(a, b):
		return any(not (b <= s or a >= e) for s, e, _ in edits)

	for m in CITE.finditer(masked):
		date = m.group(2)
		if len(date) == 5:
			iso, why = check(int(date[:2]), int(date[3:]), limit)
			if not iso:
				unresolved.append((m.group(0), why))
				continue
			date = iso
		new = f'(owner, {date}{m.group(3) or ""}{m.group(4)}'
		if new != m.group(0):
			edits.append((m.start(), m.end(), new))
	for m in DOTTED.finditer(masked):
		if not taken(m.start(), m.end()):
			edits.append((m.start(), m.end(), f'{m.group(1)}-{m.group(2)}-{m.group(3)}'))
	for m in MMDD.finditer(masked):
		if taken(m.start(), m.end()):
			continue
		before, after = masked[:m.start()], masked[m.end() - len(m.group('suf') or ''):]
		month, day = int(m.group(1)), int(m.group(2))
		if not (CUE_BEFORE.search(before[-40:]) or CUE_AFTER.match(after)):
			ambiguous.append((m.group(0), before[-30:].lstrip() + '[' + m.group(0) + ']' + after[:20]))
			continue
		# a range such as "10-20 blocks" or "3-12" with a unit after it is not a date
		if re.match(r'^\s*(blocks?|ticks?|seconds?|minutes?|items?|levels?|%|x\b|times|->|,\s*\d+-\d+)', after) or re.search(r'\b(lines?|lvl|rolls|in|v|u|x)\s*$', before):
			ambiguous.append((m.group(0), 'number range'))
			continue
		iso, why = check(month, day, limit)
		if not iso:
			unresolved.append((m.group(0), why + ': ' + before[-30:].lstrip() + '[' + m.group(0) + ']' + after[:20]))
			continue
		edits.append((m.start(), m.end(), iso + (m.group('suf') or '')))
	# recurring calendar dates (a seasonal event's window) have no year by design
	recurring = re.search(r'real (local )?date|every year|each year|Halloween|Christmas|annual', masked, re.I)
	for rx, mi, di, yi in ((MONTH_D, 1, 2, 3), (D_MONTH, 2, 1, 3)):
		for m in rx.finditer(masked):
			if recurring:
				ambiguous.append((m.group(0), 'recurring calendar date'))
				continue
			if taken(m.start(), m.end()):
				continue
			month = MONTHS[m.group(mi)[:3].lower()]
			day = int(m.group(di))
			if m.group(mi) == 'May' and rx is MONTH_D and not m.group(yi):
				ambiguous.append((m.group(0), 'May as a word'))
				continue
			if m.group(yi):
				try:
					iso = dt.date(int(m.group(yi)), month, day).isoformat()
				except ValueError:
					unresolved.append((m.group(0), 'not a calendar date'))
					continue
			else:
				iso, why = check(month, day, limit)
				if not iso:
					unresolved.append((m.group(0), why))
					continue
			edits.append((m.start(), m.end(), iso))
	if lint:
		return line, [(line[s:e], t) for s, e, t in edits], unresolved, ambiguous
	for s, e, t in sorted(edits, reverse=True):
		changes.append((line[s:e], t))
		line = line[:s] + t + line[e:]
	return line, changes[::-1], unresolved, ambiguous


def process(path, apply, lint=False):
	try:
		text = path.read_text(encoding='utf-8')
	except (UnicodeDecodeError, FileNotFoundError):
		return [], [], []
	limit = last_change(path)
	if path.suffix == '.csv':
		return process_csv(path, text, limit, apply, lint)
	out, changes, unresolved, ambiguous = [], [], [], []
	fence = False
	is_md = path.suffix == '.md'
	for i, line in enumerate(text.split('\n'), 1):
		if is_md and line.lstrip().startswith('```'):
			fence = not fence
		if fence or (is_md and line.lstrip().startswith('```')):
			out.append(line)
			continue
		new, c, u, a = convert_line(line, limit, lint)
		changes += [(i, old, new, line) for old, new in c]
		unresolved += [(i, *x) for x in u]
		ambiguous += [(i, *x) for x in a]
		out.append(new)
	if apply and changes:
		path.write_text('\n'.join(out), encoding='utf-8')
	return changes, unresolved, ambiguous


def ctx(line, old):
	i = line.find(old)
	return line[max(0, i - 35):i + len(old) + 25].strip()


def process_csv(path, text, limit, apply, lint):
	"""A CSV table: each cell is converted on its own and the table is written back with the csv module, so a date
	form that adds a comma ("(owner, 2026-11-05)") stays inside its quoted cell."""
	rows = list(csv.reader(io.StringIO(text)))
	changes, unresolved, ambiguous = [], [], []
	for i, row in enumerate(rows, 1):
		for j, cell in enumerate(row):
			parts = cell.split('\n')
			for k, part in enumerate(parts):
				new, c, u, a = convert_line(part, limit, lint)
				changes += [(i, old, nw, part) for old, nw in c]
				unresolved += [(i, *x) for x in u]
				ambiguous += [(i, *x) for x in a]
				parts[k] = new
			row[j] = '\n'.join(parts)
	if apply and changes:
		buf = io.StringIO()
		csv.writer(buf, lineterminator='\n').writerows(rows)
		path.write_text(buf.getvalue(), encoding='utf-8')
	return changes, unresolved, ambiguous


def rel(p):
	try:
		return p.relative_to(ROOT).as_posix()
	except ValueError:
		return 'memory/' + p.name


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	ap.add_argument('--apply', action='store_true')
	ap.add_argument('--scope', choices=['docs', 'code'], default='docs')
	ap.add_argument('--report', default=str(Path(tempfile.gettempdir()) / 'iso_dates_report.txt'))
	ap.add_argument('--lint', action='store_true', help='list remaining hits only')
	ap.add_argument('files', nargs='*', help='only these files')
	a = ap.parse_args()
	paths = [Path(f).resolve() for f in a.files] or (doc_scope() if a.scope == 'docs' else code_scope())
	lines, tot = [], [0, 0, 0]
	for p in paths:
		c, u, amb = process(p, a.apply and not a.lint, a.lint)
		if not (c or u or amb):
			continue
		tot[0] += len(c)
		tot[1] += len(u)
		tot[2] += len(amb)
		if a.lint:
			for ln, old, new, _ in c:
				print(f'{rel(p)}:{ln}: {old} -> {new}')
			continue
		print(f'{len(c):5d} changed {len(u):4d} unresolved {len(amb):4d} ambiguous  {rel(p)}')
		lines.append(f'## {rel(p)}')
		lines += [f'  {ln}: {old} -> {new}    | {ctx(src, old)}' for ln, old, new, src in c]
		lines += [f'  {ln}: UNRESOLVED {old}: {why}' for ln, old, why in u]
		lines += [f'  {ln}: AMBIGUOUS {old}: {ctx}' for ln, old, ctx in amb]
	verb = 'changed' if a.apply else 'would change'
	print(f'total: {tot[0]} {verb}, {tot[1]} unresolved (owner list), {tot[2]} ambiguous (left as is)')
	if not a.lint:
		Path(a.report).parent.mkdir(parents=True, exist_ok=True)
		Path(a.report).write_text('\n'.join(lines) + '\n', encoding='utf-8')
		print(f'report: {a.report}')
	return 0


if __name__ == '__main__':
	sys.exit(main())
