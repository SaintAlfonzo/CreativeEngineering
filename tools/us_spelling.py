#!/usr/bin/env python3
"""American spelling and `->` arrows in own prose and player text (design_rules.md DR-L7; plan_rules_fixes.md 15b).

Dry run by default: prints a per-file count and writes the full report (every change with its context) to the report
path. `--apply` rewrites.

	python3 tools/us_spelling.py                  # dry run, doc and lang scope
	python3 tools/us_spelling.py --apply
	python3 tools/us_spelling.py --scope code     # comments and strings in code (plan phase P5), same word list
	python3 tools/us_spelling.py --lint           # remaining hits only (for design_rules_audit.py)
	python3 tools/us_spelling.py FILE ...         # only these files

Prose only: code spans, fenced blocks, link targets, `ns:id` ids, paths, snake_case, camelCase and dotted tokens are
never changed, nor are proper names in PROTECT (upstream mod and item names). Lang files: only own namespaces'
en_us.json values, never keys; arrows are left in lang (player text keeps what it has). Skipped: deployed changelogs
(DR-P9), exported/, upstream and donor trees, repack members' lang.
"""
import argparse
import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MEMORY = Path.home() / '.claude/projects/-home-[REDACTED]--local-share-atlauncher-instances-CreativeEngineering2/memory'
SKIP = re.compile(r'(^|/)(exported|logs|jar_archive|build|deploy|loot_catalog)/|_upstream/|moved_code_backup/|'
				  r'kubejs/assets/creativecommon/changelog/(?!unreleased\.md)')
GENERATED = {'feature_writeup/todo/TODO.md', 'feature_writeup/testing/scratch_queue.md', 'feature_writeup/README.md'}
# Own lang files: our jars' own namespaces and the pack's kubejs/assets overrides. Repack and port members keep
# their upstream text (D20).
OWN_LANG = re.compile(r'^(development/(creative[A-Za-z]+|solRaspberryPie)/src/main/resources/assets/[a-z]+/lang/en_us\.json|'
					  r'kubejs/assets/[a-z_]+/lang/en_us\.json)$')

# British stem -> American stem; matched case-preserving at a word start, with the listed endings.
STEMS = {
	'colour': 'color', 'discolour': 'discolor', 'armour': 'armor', 'behaviour': 'behavior', 'flavour': 'flavor',
	'favour': 'favor', 'honour': 'honor', 'labour': 'labor', 'neighbour': 'neighbor', 'harbour': 'harbor',
	'humour': 'humor', 'odour': 'odor', 'rumour': 'rumor', 'vapour': 'vapor', 'vigour': 'vigor',
	'centre': 'center', 'metre': 'meter', 'litre': 'liter', 'theatre': 'theater', 'calibre': 'caliber',
	'licence': 'license', 'defence': 'defense', 'offence': 'offense',
	'catalogue': 'catalog', 'programme': 'program', 'analogue': 'analog',
	'grey': 'gray', 'mould': 'mold', 'plough': 'plow', 'sceptre': 'scepter',
	# not here: aluminium (Immersive Engineering's item names say Aluminium), fibre (an item physics family key)
}
STEM_END = r'(s|ed|ing|ings|ful|ish|ist|ists|ite|ites|less|y|ness|able)?'
# -ise verbs (explicit list: advertise, exercise, surprise, otherwise and the like never change)
ISE = ['normalis', 'organis', 'recognis', 'customis', 'capitalis', 'initialis', 'serialis', 'deserialis', 'synchronis',
	   'optimis', 'prioritis', 'minimis', 'maximis', 'summaris', 'categoris', 'finalis', 'realis', 'utilis', 'visualis',
	   'standardis', 'randomis', 'emphasis', 'specialis', 'authoris', 'apologis', 'memoris', 'stabilis', 'neutralis',
	   'materialis', 'generalis', 'localis', 'centralis', 'harmonis', 'itemis', 'tokenis', 'parametris', 'sanitis',
	   'criticis', 'familiaris', 'personalis', 'modernis', 'digitis', 'rationalis', 'symbolis', 'legalis', 'mobilis',
	   'colouris', 'vaporis', 'crystallis', 'caramelis', 'pasteuris', 'fertilis', 'sterilis', 'carbonis', 'oxidis',
	   'ionis', 'polymeris', 'magnetis', 'hospitalis', 'immobilis', 'desensitis', 'sensitis', 'pressuris', 'vandalis',
	   'tantalis', 'mesmeris', 'patronis', 'scrutinis', 'agonis', 'theoris']
ISE_END = {'e': 'ze', 'es': 'zes', 'ed': 'zed', 'ing': 'zing', 'er': 'zer', 'ers': 'zers', 'ation': 'zation',
		   'ations': 'zations', 'able': 'zable'}
DOUBLE_L = {'travelled': 'traveled', 'travelling': 'traveling', 'traveller': 'traveler', 'travellers': 'travelers',
			'modelled': 'modeled', 'modelling': 'modeling', 'labelled': 'labeled', 'labelling': 'labeling',
			'levelled': 'leveled', 'levelling': 'leveling', 'fuelled': 'fueled', 'fuelling': 'fueling',
			'signalled': 'signaled', 'signalling': 'signaling', 'cancelled': 'canceled', 'cancelling': 'canceling',
			'marvellous': 'marvelous', 'jewellery': 'jewelry', 'analyse': 'analyze', 'analysed': 'analyzed',
			'analysing': 'analyzing', 'paralysed': 'paralyzed', 'enrolment': 'enrollment', 'fulfil': 'fulfill',
			'fulfils': 'fulfills', 'skilful': 'skillful', 'wilful': 'willful', 'centred': 'centered',
			'centring': 'centering', 'licenced': 'licensed', 'licencing': 'licensing', 'practise': 'practice'}
# Proper names that keep their spelling (upstream mods, items and people's names as the game or the author writes them)
PROTECT = re.compile(r"Colourful Azaleas|Colorful Azaleas|Better Armour|Armourer'?s Workshop|Armour ?Bar|Grey Goo|"
					 r"Greyhound|Colour ?Wheel|Licence to|Centre Pompidou|Mouldy|Defence Tower|Colourful Mobs|"
					 r"Neighbourhood|grey_|Grey Elf", re.I)

TOKEN_SKIP = re.compile(r'[A-Za-z0-9]*[_./:#\\][\w./:#\\-]*|[a-z]+[A-Z]\w*|[A-Z][a-z]+[A-Z]\w*')


def build():
	pats = []
	for br, am in STEMS.items():
		pats.append((re.compile(r'\b(' + br + ')' + STEM_END + r'\b', re.I), br, am))
	for st in ISE:
		pats.append((re.compile(r'\b(' + st + ')(' + '|'.join(sorted(ISE_END, key=len, reverse=True)) + r')\b', re.I), st, None))
	for br, am in DOUBLE_L.items():
		pats.append((re.compile(r'\b(' + br + r')\b', re.I), br, am))
	return pats


PATS = build()


def case_like(src, dst):
	if src.isupper():
		return dst.upper()
	if src[:1].isupper():
		return dst[:1].upper() + dst[1:]
	return dst


def blank_spans(line):
	"""Mask code spans, link targets, ids, paths and code-like tokens with spaces (same length)."""
	def blank(m):
		return ' ' * len(m.group(0))
	m = re.sub(r'`[^`]*`', blank, line)
	m = re.sub(r'\]\([^)]*\)', blank, m)
	m = re.sub(r'<[^>]+>', blank, m)
	m = TOKEN_SKIP.sub(blank, m)
	m = PROTECT.sub(blank, m)
	return m


def convert(text, arrows=True):
	"""(new_text, [(old, new)]) for one line or one lang value."""
	masked = blank_spans(text)
	edits = []
	for rx, br, am in PATS:
		for m in rx.finditer(masked):
			word = m.group(0)
			if am is None:   # -ise verb
				stem, end = m.group(1), m.group(2)
				new = case_like(word, (stem[:-1] + ISE_END[end.lower()]).lower())
			elif am in STEMS.values():
				new = case_like(m.group(1), am) + (m.group(2) or '')
			else:
				new = case_like(word, am)
			if new != word and not any(s < m.end() and m.start() < e for s, e, _ in edits):
				edits.append((m.start(), m.end(), new))
	if arrows:
		for m in re.finditer('→', masked):
			edits.append((m.start(), m.end(), '->'))
	out, changes = text, []
	for s, e, new in sorted(edits, reverse=True):
		changes.append((text[s:e], new))
		out = out[:s] + new + out[e:]
	return out, changes[::-1]


def generated_doc(path):
	"""A doc a generator writes (its header says so): the generator's own strings are fixed in the code pass."""
	if path.suffix != '.md':
		return False
	try:
		head = path.read_text(encoding='utf-8')[:1500]
	except OSError:
		return False
	return bool(re.search(r'Generated by|Do not edit by hand', head))


def git(*a):
	return subprocess.run(['git', *a], cwd=ROOT, capture_output=True, text=True).stdout.split('\n')


def doc_scope():
	files = [f for f in git('ls-files', 'feature_writeup/*.md', '.claude/*.md', 'CLAUDE.md', 'mod_sides.md',
							'feature_writeup/todo/rows/*.csv', 'feature_writeup/status/features.csv', 'feature_writeup/todo/layout.md',
							'feature_writeup/testing/rows/*.qb', 'kubejs/assets/creativecommon/changelog/unreleased.md')
			 if f and not SKIP.search(f) and f not in GENERATED]
	out = [ROOT / f for f in files if not generated_doc(ROOT / f)]
	out += [ROOT / f for f in git('ls-files', '*en_us.json') if f and OWN_LANG.match(f)]
	if MEMORY.is_dir():
		out += sorted(MEMORY.glob('*.md'))
	return out


def process(path, apply):
	text = path.read_text(encoding='utf-8')
	changes = []
	if path.name == 'en_us.json':
		out_lines = []
		for i, line in enumerate(text.split('\n'), 1):
			m = re.match(r'^(\s*"(?:[^"\\]|\\.)*"\s*:\s*")((?:[^"\\]|\\.)*)("\s*,?\s*)$', line)
			if m:
				new, c = convert(m.group(2), arrows=False)
				changes += [(i, o, n, line) for o, n in c]
				line = m.group(1) + new + m.group(3)
			out_lines.append(line)
		new_text = '\n'.join(out_lines)
		if changes:
			json.loads(new_text)   # still valid
	elif path.suffix == '.csv':
		import csv
		import io
		rows = list(csv.reader(io.StringIO(text)))
		for i, row in enumerate(rows, 1):
			for j, cell in enumerate(row):
				new, c = convert(cell)
				changes += [(i, o, n, cell) for o, n in c]
				row[j] = new
		buf = io.StringIO()
		csv.writer(buf, lineterminator='\n').writerows(rows)
		new_text = buf.getvalue()
	else:
		out_lines, fence = [], False
		for i, line in enumerate(text.split('\n'), 1):
			if line.lstrip().startswith('```'):
				fence = not fence
			# .qb runnable lines (commands, ids) are code; only @@text, @@title, @pass, @fails, @group and ## titles are prose
			if fence or line.lstrip().startswith('```') or (path.suffix == '.qb' and not re.match(
					r'^(@@text|@@title|@pass|@fails|@group|## )', line)):
				out_lines.append(line)
				continue
			new, c = convert(line)
			changes += [(i, o, n, line) for o, n in c]
			out_lines.append(new)
		new_text = '\n'.join(out_lines)
	if apply and changes and new_text != text:
		path.write_text(new_text, encoding='utf-8')
	return changes


def rel(p):
	try:
		return p.relative_to(ROOT).as_posix()
	except ValueError:
		return 'memory/' + p.name


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	ap.add_argument('--apply', action='store_true')
	ap.add_argument('--lint', action='store_true')
	ap.add_argument('--report', default=str(Path(tempfile.gettempdir()) / 'us_spelling_report.txt'))
	ap.add_argument('files', nargs='*')
	a = ap.parse_args()
	paths = [Path(f).resolve() for f in a.files] or doc_scope()
	rep, total, words = [], 0, {}
	for p in paths:
		c = process(p, a.apply and not a.lint)
		if not c:
			continue
		total += len(c)
		for _, o, n, _ in c:
			words[(o.lower(), n.lower())] = words.get((o.lower(), n.lower()), 0) + 1
		if a.lint:
			for ln, o, n, _ in c:
				print(f'{rel(p)}:{ln}: {o} -> {n}')
			continue
		print(f'{len(c):5d}  {rel(p)}')
		rep.append(f'## {rel(p)}')
		for ln, o, n, ctx in c:
			i = ctx.find(o)
			rep.append(f'  {ln}: {o} -> {n}    | {ctx[max(0, i - 40):i + len(o) + 30].strip()}')
	print(f'total: {total} {"changed" if a.apply else "to change"}')
	if not a.lint:
		rep.append('## word counts')
		rep += [f'  {n:5d} {o} -> {w}' for (o, w), n in sorted(words.items(), key=lambda kv: -kv[1])]
		Path(a.report).write_text('\n'.join(rep) + '\n', encoding='utf-8')
		print(f'report: {a.report}')
	return 0


if __name__ == '__main__':
	sys.exit(main())
