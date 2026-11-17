#!/usr/bin/env python3
"""Move dated history out of code comments into the writeup History (design_rules.md DR-C26; plan_rules_fixes.md 17).

Dry run by default: writes the report (one table per writeup: file, line, old text, what happens, proposed History
line) and prints a summary. `--apply` edits the code comments and appends the History lines.

	python3 tools/history_extract.py                    # dry run, report to the temp dir
	python3 tools/history_extract.py --report FILE.md   # report path
	python3 tools/history_extract.py --apply            # edit code, append History, write the review list
	python3 tools/history_extract.py --lint             # dated comment lines left; exit 1 when any (check_all)
	python3 tools/history_extract.py FILE ...           # only these files

Each comment sentence that holds a date gets one of three outcomes:
- history: a sentence about a past change (moved, carved, ported, seeded, replaced, until, was ...) is deleted;
- rewrite: a sentence that states a rule keeps its words and loses the date (`(owner 2026-11-01, TODO x)` ->
  `(owner, TODO x)`, ` since 2026-11-14` -> ``); these are listed for owner review;
- clause: a dated history clause inside a parenthetical is dropped, the rest of the sentence stays.
Every dated sentence also becomes `- YYYY-MM-DD: <sentence> (file)` in its writeup's History, through
tools/writeup_owner.json (longest source folder prefix wins). Year-less dates are read as 2026 (D5) first.

Comments only: Java, JS, Gradle (`//`, `/* */`), Python (tokenize), shell and mods.toml (`#`). Dates inside paths,
ids and code spans are not dates. Skipped: recurring calendar dates (Halloween, every year), generated and upstream
trees, game configs.
"""
import argparse
import io
import json
import re
import subprocess
import sys
import tempfile
import textwrap
import tokenize
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import iso_dates  # noqa: E402

OWNER_FILE = ROOT / 'tools/writeup_owner.json'
# The date tools document their patterns with example dates.
SELF = {'tools/history_extract.py', 'tools/iso_dates.py', 'tools/us_spelling.py'}
SKIP = re.compile(r'(^|/)(exported|logs|jar_archive|build|deploy|loot_catalog|generation_scripts|moved_code_backup)/|'
				  r'_upstream/|^(config|configs_edited|defaultconfigs|server)/')
ISO = re.compile(r'(?<![\w/.\-])(20\d\d)-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])(?:[a-d]\b)?(?: \d\d:\d\d)?(?![\w/\-])')
RECURRING = re.compile(r'real (local )?date|every year|each year|Halloween|Christmas|annual|calendar day', re.I)
# A past change: the sentence (or clause) is history, not a rule.
HIST = re.compile(r'\b(moved|carved|ported|seeded|migrated|MIGRATED|renamed|swapped|split out|took over|taken over|'
				  r'replaces|replaced|until|reverted|was|were|used to|formerly|previously|no longer|rows unchanged|'
				  r'rewritten|introduced|added|removed|dropped|deleted|retired|superseded|became|changed|landed|merged|'
				  r'raised|lowered|restored|reworked|switched|fixed|built|measured|found|written|carried|copied|'
				  r'taken|lifted)\b|\bup to 20\d\d-|\b\d+(\.\d+)? (from|since|before|until) 20\d\d-', re.I)
# Comment lines that are read by a tool or carry a license notice: never part of a paragraph, never edited.
DIRECTIVE = re.compile(r'^\s*(requires|priority|ignored|side|packmode|eslint|@formatter|noinspection|noqa|type|global|'
					   r'language)\b\s*:?|^\s*@ts-')
LICENSE = re.compile(r'MODIFIED|Attribution Notice|[Mm]odified (this|by|for)|Modifications?\b|SPDX|Copyright|\(c\)', re.I)
STRONG = re.compile(r'^(Was|Were)\b|\b(until|moved|carved|ported|seeded|replaced|renamed|formerly|previously|no longer|'
					r'rows unchanged|migrated|MIGRATED|before that|after that|until then)\b|\bup to 20\d\d-')
UNDATED = re.compile(r'\b(moved (from|to|out|into|here)|used to|formerly|previously|no longer|will become|planned for|'
					 r'rows unchanged|renamed from|was renamed|becomes \S+ at the|until the \w+ (swap|split|move))\b', re.I)


def git(*a):
	return subprocess.run(['git', *a], cwd=ROOT, capture_output=True, text=True).stdout


def code_scope():
	files = git('ls-files', '*.java', '*.js', '*.py', '*.gradle', '*.kts', '*.sh', '*mods.toml').split('\n')
	return [ROOT / f for f in files if f and not SKIP.search(f) and f not in SELF]


def owners():
	data = json.loads(OWNER_FILE.read_text(encoding='utf-8'))
	return {k: v for k, v in data.items() if not k.startswith('_')}


def owner_of(rel, table):
	best = max((k for k in table if rel == k or rel.startswith(k)), key=len, default=None)
	return table[best] if best else None


# ---- comment extraction ------------------------------------------------------------------------------------------
# A comment line: (line_no, col, prefix, text, full) where prefix is what precedes the text on that line (indent and
# marker) and full is True when the line holds nothing but the comment.

def c_comments(lines):
	out, in_block = [], False
	for n, line in enumerate(lines, 1):
		i, quote = 0, None
		if in_block:
			end = line.find('*/')
			body = line if end < 0 else line[:end]
			m = re.match(r'^(\s*\*?\s?)(?!/)', body)
			pre = m.group(1) if m else ''
			if body.strip() and body.strip() != '*':
				out.append((n, len(pre), pre, body[len(pre):].rstrip(), end < 0 or not line[end + 2:].strip()))
			if end < 0:
				continue
			in_block, i = False, end + 2
		while i < len(line):
			ch = line[i]
			if quote:
				if ch == '\\':
					i += 2
					continue
				if ch == quote:
					quote = None
			elif ch in '"\'`':
				quote = ch
			elif line.startswith('//', i) and not line[max(0, i - 1):i] == ':':
				pre = line[:i] + re.match(r'//+\s?', line[i:]).group(0)
				out.append((n, len(pre), pre, line[len(pre):].rstrip(), not line[:i].strip()))
				break
			elif line.startswith('/*', i):
				end = line.find('*/', i + 2)
				m = re.match(r'/\*+\s?', line[i:])
				pre = line[:i] + m.group(0)
				body = line[len(pre):] if end < 0 else line[len(pre):end]
				if body.strip():
					out.append((n, len(pre), pre, body.rstrip(), not line[:i].strip() and (end < 0 or not line[end + 2:].strip())))
				if end < 0:
					in_block = True
					break
				i = end + 2
				continue
			i += 1
	return out


def hash_comments(lines, python):
	out = []
	if python:
		src = '\n'.join(lines) + '\n'
		try:
			for tok in tokenize.generate_tokens(io.StringIO(src).readline):
				if tok.type == tokenize.COMMENT:
					n, c = tok.start
					line = lines[n - 1]
					if n == 1 and line.startswith('#!'):
						continue
					m = re.match(r'#+\s?', line[c:])
					pre = line[:c] + m.group(0)
					out.append((n, len(pre), pre, line[len(pre):].rstrip(), not line[:c].strip()))
		except (tokenize.TokenError, IndentationError, SyntaxError):
			pass
		return out
	for n, line in enumerate(lines, 1):
		if n == 1 and line.startswith('#!'):
			continue
		quote = None
		for i, ch in enumerate(line):
			if quote:
				if ch == quote:
					quote = None
			elif ch in '"\'':
				quote = ch
			elif ch == '#' and (i == 0 or line[i - 1] in ' \t'):
				m = re.match(r'#+\s?', line[i:])
				pre = line[:i] + m.group(0)
				out.append((n, len(pre), pre, line[len(pre):].rstrip(), not line[:i].strip()))
				break
	return out


def comments(path, lines):
	if path.suffix in ('.java', '.js', '.gradle', '.kts'):
		return c_comments(lines)
	return hash_comments(lines, path.suffix == '.py')


def paragraphs(cms):
	"""Group full-line comments into paragraphs: consecutive lines, same prefix, no blank comment line or list item
	between. A trailing comment is a paragraph of its own."""
	paras, cur = [], []
	for c in cms:
		n, _, pre, text, full = c
		if DIRECTIVE.match(text):
			if cur:
				paras.append(cur)
			cur = []
			continue
		new_item = re.match(r'^\s*([-*]|\d+[.)])\s|^\s*(<p>|<li>|@\w+)', text) or (cur and text.startswith(' ') and not cur[-1][3].startswith(' ')
															   and not cur[0][3].startswith(('-', '*')))
		if cur and full and cur[-1][4] and cur[-1][0] == n - 1 and cur[-1][2].rstrip() == pre.rstrip() and text.strip() and not new_item:
			cur.append(c)
			continue
		if cur:
			paras.append(cur)
		cur = [c] if text.strip() else []
	if cur:
		paras.append(cur)
	return paras


# ---- sentences ----------------------------------------------------------------------------------------------------

SENT = re.compile(r'(?<=[.!?])(?<!\be\.g\.)(?<!\bi\.e\.)(?<!\bvs\.)(?<!\betc\.)\s+(?=[A-Z(\[`"\'])')


def mask(text):
	"""Blank code spans, {@code} and path or id tokens so dates inside them are never read."""
	def blank(m):
		return ' ' * len(m.group(0))
	t = re.sub(r'`[^`]*`|\{@\w+ [^}]*\}', blank, text)
	return re.sub(r'[\w.\-]*[/\\_:][\w./\\:\-]*[\w/]', lambda m: blank(m) if not ISO.fullmatch(m.group(0)) else m.group(0), t)


def dates_in(text):
	return [m for m in ISO.finditer(mask(text))]


YEARLESS = re.compile(iso_dates.MMDD.pattern + '|' + iso_dates.MONTH_D.pattern + '|' + iso_dates.D_MONTH.pattern)


def to_iso(text, limit):
	"""Year-less and month-name dates -> ISO (iso_dates rules, D5)."""
	new, _, _, _ = iso_dates.convert_line(text, limit)
	return new


STRIP = [
	# citation forms: (owner 2026-11-01, x) -> (owner, x); (owner decision 2026-11-01) -> (owner decision)
	(r'\b(owner|user|Owner|User)( decision| ruling| answer| request)?,? (on )?DATE', r'owner\2'),
	(r',? (decided|dated|confirmed|agreed|ruled|answered|requested|asked|set|chosen|picked) (on )?DATE', ''),
	(r'(,?\s+|(?<=\())(since|as of|from|on|in|at|after|before|by|of|until|till) DATE', ''),
	(r'DATE (scratch )?(run|session|audit|scan|batch|pass|review)\b(?![-/])', ''),
	(r'(?<=[\w\]]) DATE:', ':'),
	(r'(?<=[\w\]]) DATE([,;])', r'\1'),
	(r'DATE[,;:]? ', ''),
	(r',? DATE', ''),
	(r'DATE', ''),
]


def strip_dates_inline(s):
	"""strip_dates for a formatted line: the same patterns, spacing and layout kept."""
	d = ISO.pattern
	s = re.sub(d + r'((,| and| to| or|/)\s*' + d + ')+', lambda m: m.group(0)[:10], s)
	for pat, rep in STRIP:
		s = re.sub(pat.replace('DATE', d), rep, s)
	s = re.sub(r'\(\s*[,;]\s*', '(', s)
	s = re.sub(r'\s*[,;]\s*\)', ')', s)
	s = re.sub(r'\(\s*(since|before|after|until|on|from|by|of|in)?\s*\)', '()', s)
	s = re.sub(r' ?\(\s*\)', '', s)
	return re.sub(r'(?<=\S) ([,.;:])(?=\s|$)', r'\1', s)


def strip_dates(s):
	d = ISO.pattern
	s = re.sub(d + r'((,| and| to| or|/)\s*' + d + ')+', lambda m: m.group(0)[:10], s)
	for pat, rep in STRIP:
		s = re.sub(pat.replace('DATE', d), rep, s)
	s = re.sub(r'\(\s*[,;]\s*', '(', s)
	s = re.sub(r'\s*[,;]\s*\)', ')', s)
	s = re.sub(r'\(\s*(since|before|after|until|on|from|by|of|in)?\s*\)', '()', s)
	s = re.sub(r'\s*\(\s*\)', '', s)
	s = re.sub(r'  +', ' ', s)
	s = re.sub(r' ([,.;:])(?=\s|$)', r'\1', s)
	return s.strip()


def split0(s, sep):
	"""Split at `sep` outside parentheses and braces."""
	parts, depth, cur, i = [], 0, '', 0
	while i < len(s):
		ch = s[i]
		depth += (ch in '({[') - (ch in ')}]')
		if depth == 0 and s.startswith(sep, i):
			parts.append(cur)
			cur = ''
			i += len(sep)
			continue
		cur += ch
		i += 1
	return parts + [cur]


def fix_parens(s):
	"""Dates inside parentheticals: drop a dated history clause, strip the date from the rest."""
	def fix(m):
		inner = m.group(1)
		if not dates_in(inner):
			return m.group(0)
		keep = []
		for p in split0(inner, ';'):
			if dates_in(p) and HIST.search(re.sub(ISO.pattern, '', p)) and not re.match(r'\s*(owner|user)\b', p, re.I):
				continue
			keep.append(strip_dates('(' + p.strip() + ')')[1:-1] if dates_in(p) else p.strip())
		keep = [k for k in keep if k.strip()]
		return '(' + '; '.join(keep) + ')' if keep else ''
	new = re.sub(r'\(([^()]*)\)', fix, s)
	new = re.sub(r'\s*\(\s*\)', '', new)
	new = re.sub(r'  +', ' ', new)
	return re.sub(r' ([,.;:])(?=\s|$)', r'\1', new).strip()


def handle_part(p):
	"""One `;` part of a sentence -> (text or '', kind)."""
	main = re.sub(r'\([^()]*\)', '', p)
	if STRONG.search(main) or (dates_in(main) and HIST.search(main)):
		if not (dates_in(main) or dates_in(p)):
			return ('', 'drop') if STRONG.search(main) else (p, 'keep')
		clauses = split0(p, ', ')
		idx = next(i for i, c in enumerate(clauses)
				   if dates_in(c) or STRONG.search(re.sub(r'\([^()]*\)', '', c)) or HIST.search(re.sub(r'\([^()]*\)', '', c)))
		if idx > 0 and not dates_in(', '.join(clauses[:idx])) and re.match(
				r'\s*(carved|moved|ported|seeded|replaced|renamed|migrated|retired|removed|added|introduced|merged|split|'
				r'swapped|written|built|was|were|until|on 20\d\d-|since 20\d\d-|from 20\d\d-|\d\d\d\d-)', clauses[idx]):
			return fix_parens(', '.join(clauses[:idx])), 'drop'
		if re.search(r'\S: \S', main[main.find(dates_in(main)[0].group(0)) if dates_in(main) else 0:]) or len(p) > 220:
			return p, 'manual'
		return '', 'drop'
	if dates_in(main):
		return strip_dates(p), 'rewrite'
	if dates_in(p):
		new = fix_parens(p)
		if not re.sub(r'[\s.,;:()]', '', new):
			return '', 'drop'
		return new, 'drop' if len(new) < len(strip_dates(p)) - 1 else 'rewrite'
	return p, 'keep'


def handle_sentence(s):
	"""-> (new_sentence or '', kind) for a sentence that holds a date: history (all dropped), clause (part dropped),
	rewrite (date stripped, words kept) or manual (left as written, for a hand edit)."""
	if re.match(r'^(Rule|Pack rule|Owner rule)\b', s):
		return strip_dates(fix_parens(s)), 'rewrite'
	res = [handle_part(p.strip()) for p in split0(s, ';')]
	kinds = {k for _, k in res}
	if 'manual' in kinds:
		return s, 'manual'
	keep = [t for t, k in res if t]
	if not keep:
		return '', 'history'
	out = '; '.join(keep).strip()
	out = out[:1].upper() + out[1:] if not res[0][0] else out
	out = out.rstrip(' ,;' if s.rstrip().endswith(':') else ' ,;:')
	if s.rstrip().endswith('.') and not out.endswith(('.', '!', '?')):
		out += '.'
	return out, 'clause' if 'drop' in kinds else 'rewrite'


def history_text(s):
	s = s.strip()
	s = re.sub(r'^\(?(.*?)\)?\.?$', r'\1', s) if s.startswith('(') and s.endswith(')') else s
	return s.rstrip('.')


# ---- per file -----------------------------------------------------------------------------------------------------

def process(path, apply):
	rel = path.relative_to(ROOT).as_posix()
	try:
		text = path.read_text(encoding='utf-8')
	except (UnicodeDecodeError, FileNotFoundError):
		return [], []
	lines = text.split('\n')
	limit = None
	items, undated = [], []
	edits = {}   # first line -> (last line, [new lines])
	for para in paragraphs(comments(path, lines)):
		joined = ' '.join(c[3].strip() for c in para)
		if RECURRING.search(joined) or LICENSE.search(joined):
			continue
		if YEARLESS.search(joined):
			limit = limit or iso_dates.last_change(path)
			iso_joined = to_iso(joined, limit)
		else:
			iso_joined = joined
		if not dates_in(iso_joined):
			for c in para:
				if UNDATED.search(c[3]):
					undated.append((rel, c[0], c[3].strip()))
			continue
		prose = not any(re.search(r'\S  +\S|(?<!^)</?(p|ul|ol|li|b|i|br|pre|code|h\d|table|tr|td|th)>|([#=*~_-])\1\1|\|', c[3]) for c in para)
		sents = SENT.split(iso_joined)
		out, changed = [], False
		for s in sents:
			ds = dates_in(s)
			if not ds:
				out.append(s)
				continue
			new, kind = handle_sentence(s)
			date = ISO.search(s).group(0)[:10]
			if not prose and (len(para) > 1 or not para[0][4]):
				kind = 'manual'
			items.append({'file': rel, 'line': para[0][0], 'old': s, 'new': new, 'kind': kind, 'date': date,
						  'history': f'- {date}: {history_text(s)} ({rel})'})
			changed = changed or kind != 'manual'
			if new:
				out.append(new)
		if not prose and (len(para) > 1 or not para[0][4]) or any(i['kind'] == 'manual' for i in items[-len(sents):]
																   if i['file'] == rel and i['line'] == para[0][0]):
			# formatted comment (table, list, aligned columns) or a sentence left for a hand edit: strip the dates
			# line by line, keep the layout
			for c in para:
				line = lines[c[0] - 1]
				body = line[c[1]:c[1] + len(c[3])]
				if dates_in(to_iso(body, limit or iso_dates.last_change(path)) if YEARLESS.search(body) else body):
					iso = to_iso(body, limit or iso_dates.last_change(path)) if YEARLESS.search(body) else body
					edits[c[0]] = (c[0], [line[:c[1]] + strip_dates_inline(iso) + line[c[1] + len(c[3]):]])
			continue
		if not changed:
			continue
		body = ' '.join(out).strip()
		first = para[0]
		if len(para) == 1 and not first[4]:
			# trailing comment: rewrite in place or drop the comment
			line = lines[first[0] - 1]
			code = line[:first[1]]
			code = re.sub(r'\s*(//+|#+)\s?$', '', code) if not body else code
			edits[first[0]] = (first[0], [code + body if body else code.rstrip()])
			continue
		pre = first[2]
		widest = max(len(lines[c[0] - 1]) for c in para)
		width = 120 if widest > 120 else max(100, widest)
		cont = para[1][2] + re.match(r'^\s*', para[1][3]).group(0) if len(para) > 1 else pre
		if not body:
			new_lines = []
		else:
			glued = re.sub(r'\{@[^}]*\}|`[^`]*`', lambda m: m.group(0).replace(' ', '\x00'), body)
			new_lines = [l.replace('\x00', ' ') for l in textwrap.wrap(
				glued, width=width, initial_indent=pre, subsequent_indent=cont, break_long_words=False,
				break_on_hyphens=False)]
			# keep the tail of the last line (a closing */) if the comment ended there
			last = lines[para[-1][0] - 1]
			tail = last[para[-1][1] + len(para[-1][3]):]
			if tail.strip():
				new_lines[-1] += tail
		edits[first[0]] = (para[-1][0], new_lines)
		# a /** */ block whose only text was this paragraph is left with empty markers: drop them below
	if apply and edits:
		out, n = [], 1
		while n <= len(lines):
			if n in edits:
				last, repl = edits[n]
				out += repl
				n = last + 1
				continue
			out.append(lines[n - 1])
			n += 1
		out = drop_empty_blocks(out)
		path.write_text('\n'.join(out), encoding='utf-8')
	return items, undated


def drop_empty_blocks(lines):
	"""Remove `/**` + `*/` pairs and `//` runs left with no text by a deleted paragraph."""
	out, i = [], 0
	while i < len(lines):
		if re.match(r'^\s*/\*\*?\s*$', lines[i]):
			j = i + 1
			while j < len(lines) and re.match(r'^\s*\*\s*$', lines[j]):
				j += 1
			if j < len(lines) and re.match(r'^\s*\*/\s*$', lines[j]):
				i = j + 1
				continue
		out.append(lines[i])
		i += 1
	return out


# ---- writeups ------------------------------------------------------------------------------------------------------

HIST_INTRO = 'Lines moved out of code comments ({today}, plan_rules_fixes.md section 17):'


def append_history(doc, lines, today):
	path = ROOT / 'feature_writeup' / doc
	text = path.read_text(encoding='utf-8')
	block = [HIST_INTRO.format(today=today), ''] + sorted(set(lines), key=lambda l: l[2:12]) + ['']
	rows = text.split('\n')
	intro = HIST_INTRO.format(today=today)
	if intro in rows:
		# same day: merge into the block already written
		i = rows.index(intro) + 2
		j = i
		while j < len(rows) and rows[j].startswith('- '):
			j += 1
		block = rows[i:j]
		for new in sorted(set(lines) - set(block), key=lambda l: l[2:12]):
			k = next((n for n, old in enumerate(block) if old[2:12] > new[2:12]), len(block))
			block.insert(k, new)
		rows[i:j] = block
		path.write_text('\n'.join(rows).rstrip('\n') + '\n', encoding='utf-8')
		return
	h = next((i for i, l in enumerate(rows) if re.match(r'^##\s+(\d+\.\s*)?History\b', l)), None)
	if h is None:
		while rows and not rows[-1].strip():
			rows.pop()
		rows += ['', '## History', ''] + block
	else:
		end = next((i for i in range(h + 1, len(rows)) if re.match(r'^##?\s', rows[i])), len(rows))
		while end > h + 1 and not rows[end - 1].strip():
			end -= 1
		rows[end:end] = [''] + block
	path.write_text('\n'.join(rows).rstrip('\n') + '\n', encoding='utf-8')


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	ap.add_argument('--apply', action='store_true')
	ap.add_argument('--lint', action='store_true')
	ap.add_argument('--report', default=str(Path(tempfile.gettempdir()) / 'history_extract_report.md'))
	ap.add_argument('--review', default=str(ROOT / 'feature_writeup/history_extract_review.md'),
					help='with --apply: the owner review list (rewritten rules, undated history phrases)')
	ap.add_argument('--no-history', action='store_true', help='with --apply: edit code only (History already written)')
	ap.add_argument('files', nargs='*')
	a = ap.parse_args()
	paths = [Path(f).resolve() for f in a.files] or code_scope()
	table = owners()
	items, undated = [], []
	for p in paths:
		i, u = process(p, a.apply and not a.lint)
		items += i
		undated += u
	if a.lint:
		for it in items:
			print(f"{it['file']}:{it['line']}: {it['old'][:120]}")
		print(f'{len(items)} dated comment sentences (DR-C26; tools/history_extract.py moves them)', file=sys.stderr)
		return 1 if items else 0
	by_doc = defaultdict(list)
	for it in items:
		by_doc[owner_of(it['file'], table) or '(no owner)'].append(it)
	rep = ['# history_extract report', '', f'{len(items)} dated comment sentences in {len({i["file"] for i in items})} files; '
		   + ', '.join(f'{k} {sum(1 for i in items if i["kind"] == k)}' for k in ('history', 'clause', 'rewrite', 'manual')), '']
	for doc in sorted(by_doc):
		rep += [f'## {doc}', '', '| File | Line | Kind | Old | New |', '|---|---|---|---|---|']
		for it in by_doc[doc]:
			rep.append(f"| {it['file']} | {it['line']} | {it['kind']} | {it['old'].replace('|', '/')} | "
					   f"{it['new'].replace('|', '/')} |")
		rep.append('')
	rep += ['## Undated history phrases (left in code, owner review)', '']
	rep += [f'- {f}:{n}: {t}' for f, n, t in undated]
	Path(a.report).write_text('\n'.join(rep) + '\n', encoding='utf-8')
	for doc in sorted(by_doc):
		print(f'{len(by_doc[doc]):5d}  {doc}')
	print(f'total {len(items)}; undated phrases {len(undated)}; report: {a.report}')
	if a.apply:
		import datetime as dt
		today = dt.date.today().isoformat()
		for doc, its in by_doc.items():
			if a.no_history:
				break
			if doc == '(no owner)':
				print('no owner for: ' + ', '.join(sorted({i['file'] for i in its})), file=sys.stderr)
				continue
			append_history(doc, [i['history'] for i in its], today)
		review = ['# Comment rewrites for owner review (plan_rules_fixes.md section 17)', '',
				  f'Written by `tools/history_extract.py --apply` on {today}. Each row is a dated comment that states a rule: '
				  'the date was taken out and the rule kept in present tense; its dated line is in the writeup History. '
				  'Check that the rule is still wanted and reads right.', '',
				  '| File | Line | Was | Now |', '|---|---|---|---|']
		review += [f"| {i['file']} | {i['line']} | {i['old'].replace('|', '/')} | {i['new'].replace('|', '/')} |"
				   for i in items if i['kind'] != 'history']
		review += ['', '## Undated history phrases, left in code', '',
				   'Comment lines that read as history but carry no date; not moved (no date to file them under).', '']
		review += [f'- {f}:{n}: {t}' for f, n, t in undated]
		if not a.no_history:
			Path(a.review).write_text('\n'.join(review) + '\n', encoding='utf-8')
			print(f'review list: {a.review}')
	return 0


if __name__ == '__main__':
	sys.exit(main())
