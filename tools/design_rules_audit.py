#!/usr/bin/env python3
"""Mechanical checks for the design rules in feature_writeup/design_rules.md (DR-* ids).

Read-only. Prints one summary block per check: the rule id, a count, and the worst files. The judgement checks
(layout, prose, code structure) are not here; their results are in design_rules.md sections 8 and 9.

    python3 tools/design_rules_audit.py            # all checks, summary
    python3 tools/design_rules_audit.py -v         # also list every hit (long)
    python3 tools/design_rules_audit.py emoji ids  # only the named checks

Scope: tracked files (git ls-files). Generated and upstream trees are skipped unless a check says otherwise:
kubejs/exported/, kubejs/generation_scripts/, */build/, *_upstream/, moved_code_backup/, jar_archive/, deploy/.
"""
import re
import subprocess
import sys
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SKIP = re.compile(r'^(kubejs/exported/|kubejs/generation_scripts/|jar_archive/|deploy/)|(^|/)moved_code_backup/|/build/|_upstream/')
TEXT_EXT = {'.md', '.java', '.js', '.py', '.json', '.json5', '.toml', '.txt', '.gradle', '.properties', '.cfg', '.tsv',
			'.sh', '.mcmeta', '.yml', '.yaml', '.snbt', '.kts'}
OWN_JAVA = 'src/main/java/com/SaintAlfonzo/'

VERBOSE = '-v' in sys.argv
ONLY = [a for a in sys.argv[1:] if not a.startswith('-')]


def tracked():
	out = subprocess.run(['git', 'ls-files', '-z'], cwd=ROOT, capture_output=True, check=True).stdout
	return [p for p in out.decode().split('\0') if p and not SKIP.search(p)]


FILES = tracked()


def read(p):
	try:
		return (ROOT / p).read_text(encoding='utf-8')
	except (UnicodeDecodeError, FileNotFoundError, IsADirectoryError):
		return None


def report(rule, title, hits, unit='hits', top=8):
	"""hits: list of (path, line_no, text)."""
	per_file = Counter(h[0] for h in hits)
	print(f'## {rule} {title}: {len(hits)} {unit} in {len(per_file)} files')
	for path, n in per_file.most_common(top):
		print(f'   {n:6d}  {path}')
	if VERBOSE:
		for path, ln, text in hits:
			print(f'      {path}:{ln}  {text[:140]}')
	print()


def lines_of(paths):
	for p in paths:
		s = read(p)
		if s is None:
			continue
		for i, line in enumerate(s.splitlines(), 1):
			yield p, i, line


def wants(name):
	return not ONLY or name in ONLY


# DR-L1: emoji. Arrows, dashes and the multiplication sign are allowed; pictographs, dingbat check marks and
# variation selectors are not.
EMOJI = re.compile('[\U0001F000-\U0001FAFF☀-⛿✀-➿️⭐⭕✅❌]')
if wants('emoji'):
	paths = [p for p in FILES if Path(p).suffix in TEXT_EXT]
	hits = [(p, i, l.strip()) for p, i, l in lines_of(paths) if EMOJI.search(l)]
	own = [h for h in hits if '/src/main/java/' not in h[0] or OWN_JAVA in h[0]]
	report('DR-L1', 'emoji in tracked text files (own files; upstream Java excluded)', own)

DOCS = [p for p in FILES if p.endswith('.md') and (p.startswith('feature_writeup/') or p.startswith('.claude/')
		or p in ('CLAUDE.md', 'mod_sides.md')) and '/archive/' not in p]

# DR-L4: relative dates in docs.
REL = re.compile(r'\b(yesterday|tomorrow|last (week|night|month)|next (week|month)|this (week|morning|evening)|'
				 r'a few days ago|recently)\b', re.I)
if wants('dates'):
	report('DR-L4', 'relative date words in docs', [(p, i, l) for p, i, l in lines_of(DOCS) if REL.search(l)])

# DR-L4b, DR-L5: non-ISO dates and the old citation form in the doc scope of tools/iso_dates.py (docs, memory, skills,
# TODO rows, queue rows). check_all fails on them (iso_dates_docs, iso_dates_code); this lists them.
if wants('dates'):
	sys.path.insert(0, str(ROOT / 'tools'))
	import iso_dates
	hits, unresolved = [], []
	for path in iso_dates.doc_scope():
		c, u, _ = iso_dates.process(path, apply=False, lint=True)
		hits += [(iso_dates.rel(path), ln, f'{old} -> {new}') for ln, old, new, _ in c]
		unresolved += [(iso_dates.rel(path), ln, f'{old}: {why}') for ln, old, why in u]
	report('DR-L4b', 'year-less, month-name or dotted dates and "(user, date)" citations (iso_dates.py fixes them)', hits)
	report('DR-L4b', 'year-less dates the tool cannot resolve (owner list or number ranges)', unresolved)

# DR-W3b: test prefixes against feature_writeup/testing/test_prefix_index.md (the lint fails on its own in check_all).
if wants('prefixes'):
	out = subprocess.run([sys.executable, str(ROOT / 'tools/test_prefix_lint.py')], cwd=ROOT, capture_output=True,
						 text=True).stdout.rstrip()
	print('## DR-W3b ' + out.replace('\n', '\n   ')[:4000])
	print()

# G1: spelling mix in docs (prose only, code spans removed).
if wants('spelling'):
	pairs = [('colour', 'color'), ('behaviour', 'behavior'), ('capitalis', 'capitaliz'), ('organis', 'organiz'),
			 ('customis', 'customiz'), ('recognis', 'recogniz'), ('licence', 'license'), ('grey', 'gray'),
			 ('centre', 'center'), ('flavour', 'flavor'), ('armour', 'armor')]
	text = '\n'.join(re.sub(r'`[^`]*`', '', read(p) or '') for p in DOCS).lower()
	print('## G1 British / American spelling in docs (code spans removed)')
	for br, am in pairs:
		nb, na = len(re.findall(r'\b' + br, text)), len(re.findall(r'\b' + am, text))
		print(f'   {br:12s} {nb:5d}   {am:12s} {na:5d}')
	print()

# DR-L7: British spellings and arrows left in the doc and own-lang scope of tools/us_spelling.py (which fixes them);
# check_all fails on them (spelling_docs, spelling_code).
if wants('spelling'):
	out = subprocess.run([sys.executable, str(ROOT / 'tools/us_spelling.py'), '--lint'], cwd=ROOT, capture_output=True,
						 text=True).stdout.rstrip().split('\n')
	hits = [(l.split(':', 2)[0], int(l.split(':', 2)[1]), l.split(':', 2)[2].strip()) for l in out
			if l.count(':') >= 2 and l.split(':', 2)[1].isdigit()]
	report('DR-L7', 'British spellings and arrows in docs, memory, skills, rows and own lang (us_spelling.py fixes them)', hits)

# DR-L5: decision citations.
if wants('citations'):
	hits_user = [(p, i, l) for p, i, l in lines_of(DOCS) if re.search(r'\(user,? \d{4}-\d\d-\d\d', l)]
	hits_owner = [(p, i, l) for p, i, l in lines_of(DOCS) if re.search(r'\(owner,? \d{4}-\d\d-\d\d', l)]
	print(f'## DR-L5 decision citations: "(owner, date)" {len(hits_owner)}, "(user, date)" {len(hits_user)}')
	print()
	report('DR-L5', '"(user, date)" citations (older form)', hits_user)

# DR-W9: file paths written as code spans in writeups where the file exists (a link would be clickable).
if wants('paths'):
	tracked_set = set(FILES)
	span = re.compile(r'(?<!\[)`([\w./-]+/[\w.-]+\.(?:md|js|java|py|json|toml|png|tsv|txt))`')
	hits = []
	for p, i, l in lines_of([d for d in DOCS if d.startswith('feature_writeup/')]):
		for m in span.finditer(l):
			target = m.group(1)
			if target in tracked_set or (Path(p).parent / target).as_posix() in tracked_set:
				hits.append((p, i, target))
	report('DR-W9', 'existing repo files named as `code spans` instead of links in writeups', hits)

# DR-W16: configs_edited backups that no longer match the live config.
if wants('configs'):
	hits, missing = [], []
	for sub, live in (('root_config', 'config'), ('server_config', None)):
		base = ROOT / 'configs_edited' / sub
		if not base.is_dir() or live is None:
			continue
		for f in base.rglob('*'):
			if f.is_file():
				rel = f.relative_to(base)
				target = ROOT / live / rel
				if not target.exists():
					missing.append((f'configs_edited/{sub}/{rel}', 0, f'no {live}/{rel}'))
				elif target.read_bytes() != f.read_bytes():
					hits.append((f'{live}/{rel}', 0, 'differs from its backup'))
	report('DR-W16', 'live configs that differ from their configs_edited backup', hits, 'files')
	report('DR-W16', 'backups whose live config is gone', missing, 'files')
	changed = subprocess.run(['git', 'status', '--porcelain', '--', 'config'], cwd=ROOT, capture_output=True,
							 text=True).stdout.split('\n')
	nobak = [(l[3:], 0, 'modified, no backup') for l in changed if l.strip()
			 and not (ROOT / 'configs_edited/root_config' / l[3:].removeprefix('config/')).exists()]
	report('DR-W16', 'configs modified in the working tree with no backup', nobak, 'files')

# DR-W16: mod_sides.md rows against mods/.
if wants('sides'):
	import zipfile
	sides = read('mod_sides.md') or ''
	ids = set(re.findall(r'^\|[^|]*\|[^|]*\| `([^`]+)` \|', sides, re.M))
	jars_col = set(re.findall(r'\| `([^`]+\.jar)` \|', sides))
	found, nomatch = set(), []
	for jar in sorted([*(ROOT / 'mods').glob('*.jar'), *(ROOT / 'client_mods').glob('*.jar')]):
		try:
			with zipfile.ZipFile(jar) as z:
				toml = z.read('META-INF/mods.toml').decode('utf-8', 'replace') if 'META-INF/mods.toml' in z.namelist() else ''
		except zipfile.BadZipFile:
			continue
		# Only [[mods]] blocks; [[dependencies.*]] blocks name forge, minecraft and the like.
		blocks = re.split(r'^\s*\[\[', toml, flags=re.M)
		jar_ids = [m for blk in blocks if blk.startswith('mods]]')
				   for m in re.findall(r'^\s*modId\s*=\s*"([^"]+)"', blk, re.M)]
		if jar_ids:
			found.update(jar_ids)
			if not any(m in ids for m in jar_ids):
				nomatch.append((jar.relative_to(ROOT).as_posix(), 0, ','.join(jar_ids)))
		elif jar.name not in jars_col:
			nomatch.append((jar.relative_to(ROOT).as_posix(), 0, 'no mods.toml and no Jar column row'))
	report('DR-W16', 'jars in mods/ and client_mods/ with no mod_sides.md row (mod id cell must be exactly `id`)', nomatch, 'jars')

# DR-C10: underscores in own mod ids, packages, config names, script folders.
if wants('naming'):
	hits = []
	for p in FILES:
		if p.endswith('gradle.properties') and p.startswith('development/'):
			m = re.search(r'^mod_id=(\S+)', read(p) or '', re.M)
			if m and '_' in m.group(1):
				hits.append((p, 0, f'mod_id={m.group(1)}'))
		if OWN_JAVA in p:
			pkg = p.split(OWN_JAVA, 1)[1].rsplit('/', 1)[0]
			if '_' in pkg:
				hits.append((p, 0, f'package {pkg}'))
		m = re.match(r'kubejs/(data|assets)/(creative[^/]*_[^/]*)/', p)
		if m:
			hits.append((p, 0, f'script folder {m.group(2)}'))
	pkgs = {}
	for h in hits:
		pkgs.setdefault(h[2], h)
	report('DR-C10', 'underscored own names (upstream ids kept by ports are listed too: check)', list(pkgs.values()),
		   'names', top=20)

# DR-C19: licence fields of own mods.
if wants('licence'):
	hits = []
	for p in FILES:
		if p.endswith('gradle.properties') and p.startswith('development/'):
			m = re.search(r'^mod_license=(.*)$', read(p) or '', re.M)
			if m:
				hits.append((p, 0, m.group(1).strip()))
	vals = Counter(h[2] for h in hits)
	print(f'## DR-C19 mod_license values in development/*/gradle.properties ({len(hits)} mods)')
	for v, n in vals.most_common():
		print(f'   {n:4d}  {v}')
	roots = sorted({str(Path(h[0]).parent) for h in hits})
	nolic = [r for r in roots if not any((ROOT / r).glob('LICENSE*'))]
	print(f'   mod roots without a LICENSE file: {len(nolic)}' + (': ' + ', '.join(nolic) if nolic else ''))
	bad = [p for p in FILES if p.startswith('development/') and Path(p).name.startswith('NOTICE')
		   and not re.fullmatch(r'NOTICE-[a-z0-9]+-[A-Za-z0-9.]+\.txt', Path(p).name)]
	print(f'   NOTICE files not named NOTICE-<scope>-<licence>.txt: {len(bad)}' + (': ' + ', '.join(bad) if bad else ''))
	print()

# DR-C1, DR-C2: raw recipe calls and literal ids in scripts (judgement of each hit is in design_rules.md 10).
SCRIPTS = [p for p in FILES if p.endswith('.js') and (p.startswith('kubejs/') or '/kubejs_startup/' in p)]
DICTS = re.compile(r'_lib/(item|block|fluid|[a-z_]*)_dict\.js$|/_lib/[a-z_]*dict[a-z_]*\.js$')
HELPERS = re.compile(r'neoCraftHelper\.js$|tag_helper\.js$|compost\.js$|trade_helper\.js$')
if wants('recipes'):
	raw = re.compile(r'\b(e|event)\.(shaped|shapeless|custom|smelting|blasting|smoking|campfireCooking|stonecutting|'
					 r'smithing)\s*\(|\b(e|event)\.recipes\.\w+')
	hits = [(p, i, l.strip()) for p, i, l in lines_of([s for s in SCRIPTS if not HELPERS.search(s)])
			if raw.search(l) and not l.strip().startswith('//')]
	report('DR-C1', 'raw recipe calls outside the helpers', hits)
	# Only files with a tag event; e.add elsewhere is lang, tiers or datapack writes.
	tagadd = re.compile(r'\b(e|event)\.(add|remove|removeAll)\s*\(\s*[\'"]')
	tag_files = [s for s in SCRIPTS if not HELPERS.search(s) and re.search(r'Events\.tags\s*\(', read(s) or '')]
	hits = [(p, i, l.strip()) for p, i, l in lines_of(tag_files)
			if tagadd.search(l) and not l.strip().startswith('//')]
	report('DR-C1', 'raw tag event.add/remove calls outside the helpers', hits)
if wants('ids'):
	lit = re.compile(r'[\'"](#?[a-z0-9_.-]+:[a-z0-9_./-]+)[\'"]')
	hits = []
	for p, i, l in lines_of([s for s in SCRIPTS if not DICTS.search(s)]):
		if l.strip().startswith('//'):
			continue
		for m in lit.finditer(l):
			hits.append((p, i, m.group(1)))
	report('DR-C2', 'literal namespace:id strings in scripts outside the dicts (includes legitimate uses)', hits,
		   top=15)
	assign = re.compile(r'^\s*(i|global\.item)\.\w+\s*=')
	hits = [(p, i, l.strip()) for p, i, l in lines_of([s for s in SCRIPTS if not DICTS.search(s)]) if assign.search(l)]
	report('DR-C2', 'dict keys assigned outside the dict files', hits)
	sys.path.insert(0, str(ROOT / 'tools'))
	import dict_lint
	hits = []
	for dp in sorted(dict_lint.DICT_DIR.glob('*_dict.js')):
		for (alias, key), nums in dict_lint.duplicates(dp)[1].items():
			hits.append((str(dp.relative_to(ROOT)), nums[0], f'{alias}.{key} also on {nums[1:]}'))
	report('DR-C2', 'dict keys assigned twice in one dict file (tools/dict_lint.py fails on them)', hits)
	adv = re.compile(r'advancement', re.I)
	hits = [(p, i, l.strip()) for p, i, l in lines_of(SCRIPTS) if adv.search(l) and not l.strip().startswith('//')]
	report('DR-C14', 'scripts mentioning advancements (check each)', hits)

# DR-C9: ids in the owner's namespace (tools/namespace_lint.py, report mode until plan_rules_fixes.md P3 ends).
if wants('namespace'):
	out = subprocess.run([sys.executable, str(ROOT / 'tools/namespace_lint.py')], cwd=ROOT, capture_output=True,
						 text=True).stdout.rstrip()
	print('## DR-C9 namespace lint (tools/namespace_lint.py; feature_writeup/namespace_report.md with --write)')
	print('   ' + '\n   '.join(l for l in out.split('\n') if l.startswith('##') or l.startswith('namespace_lint')))
	print()

# DR-C1: villager trades through global.trades.villager (plan_rules_fixes.md 12c); report until the P3 conversions.
if wants('recipes'):
	hits = [(p, i, l.strip()) for p, i, l in lines_of([s for s in SCRIPTS if not HELPERS.search(s)])
			if re.search(r'\b(e|event)\.addTrade\s*\(', l) and not l.strip().startswith('//')]
	report('DR-C1', 'raw villager e.addTrade calls outside global.trades.villager', hits)

# DR-P12: tooltip colors outside the col roles (plan_rules_fixes.md section 12, V-43): raw section signs, bare
# Text.<color>( calls, raw color keys of the palette. Report until the P3 replacements; the palette lib is the source.
if wants('colors'):
	# Skipped: the palette lib, Devtools' command output (chat, not tooltips) and the palette demo rows (a swatch
	# line names its own color: ['Dark Gray', col.dgray]).
	palette = re.compile(r'kubejs/assets/creativecommon/kubejs/_lib/tooltips\.js$|/creativedevtools/')
	swatch = re.compile(r"^\['([A-Z][a-z]+(?: [A-Z][a-z]+)?)', col\.\w+\]")
	color = re.compile(r'§[0-9a-fk-or]|\bText\.(black|darkBlue|darkGreen|darkAqua|darkRed|darkPurple|gold|gray|darkGray|'
					   r'blue|green|aqua|red|lightPurple|yellow|white)\(|\bcol\.(black|dark_\w+|d(blue|green|aqua|red|purple|gray)'
					   r'|gold|gray|blue|green|aqua|red|light_purple|lpurple|yellow|white)\b')
	hits = [(p, i, l.strip()) for p, i, l in lines_of([s for s in SCRIPTS if not palette.search(s)])
			if color.search(l) and not l.strip().startswith('//') and not swatch.match(l.strip())]
	report('DR-P12', 'tooltip colors picked by look (raw section signs, Text.<color>, raw col color keys)', hits)

# DR-P15: global loot modifiers (plan_rules_fixes.md section 5, V-08). No own jar ships Forge's list
# data/forge/loot_modifiers/global_loot_modifiers.json (it would race the other copies); each member writes a fragment
# data/<ns>/glm_entries.json that Creative Loot's GlmMerge reads, and every fragment entry has its definition
# data/<ns>/loot_modifiers/<name>.json in the repo or, for a repacked member, in its upstream jar (gradle.properties
# upstream_jar, development/creativeDelight/upstream/).
if wants('glm'):
	import json
	import zipfile
	own = re.compile(r'^development/.*/src/[^/]+/resources/(.*/)?data/')
	lists = [(p, 0, 'Forge list') for p in FILES if own.match(p) and p.endswith('data/forge/loot_modifiers/global_loot_modifiers.json')]
	defs = {re.sub(r'^.*?data/([^/]+)/loot_modifiers/(.+)\.json$', r'\1:\2', p) for p in FILES
			if re.search(r'data/[^/]+/loot_modifiers/.+\.json$', p)}
	missing = []
	for p in FILES:
		if not (own.match(p) and re.search(r'data/[^/]+/glm_entries\.json$', p)):
			continue
		try:
			entries = json.loads(read(p))['entries']
		except (TypeError, ValueError, KeyError):
			missing.append((p, 0, 'no entries array'))
			continue
		upstream = set()
		props = ROOT / p.split('/src/')[0] / 'gradle.properties'
		jar = re.search(r'^upstream_jar=(.+)$', props.read_text(encoding='utf-8'), re.M) if props.exists() else None
		if jar and (ROOT / 'development/creativeDelight/upstream' / jar.group(1).strip()).exists():
			with zipfile.ZipFile(ROOT / 'development/creativeDelight/upstream' / jar.group(1).strip()) as z:
				upstream = {re.sub(r'^data/([^/]+)/loot_modifiers/(.+)\.json$', r'\1:\2', n) for n in z.namelist()
							if re.match(r'data/[^/]+/loot_modifiers/.+\.json$', n)}
		missing += [(p, 0, e) for e in entries if e not in defs and e not in upstream]
	report('DR-P15', 'own jars shipping the Forge global loot modifier list (use a glm_entries.json fragment)', lists)
	report('DR-P15', 'global loot modifier fragment entries with no definition', missing)

# DR-C21 to DR-C25: formatting facts.
if wants('format'):
	groups = {'own Java': [p for p in FILES if OWN_JAVA in p and p.endswith('.java')],
			  'KubeJS JS': SCRIPTS,
			  'tools Python': [p for p in FILES if p.startswith('tools/') and p.endswith('.py')]}
	print('## DR-C21..C25 formatting (own code)')
	print('   group           files   tab-indent  mixed-in-file  CRLF  no-final-newline  lines>120  trailing-space-lines')
	for name, paths in groups.items():
		tabs = mixed = crlf = nofinal = long_ = trail = 0
		for p in paths:
			raw_b = (ROOT / p).read_bytes() if (ROOT / p).exists() else b''
			s = raw_b.decode('utf-8', 'replace')
			if b'\r\n' in raw_b:
				crlf += 1
			if s and not s.endswith('\n'):
				nofinal += 1
			t = sp = 0
			for line in s.splitlines():
				if line.startswith('\t'):
					t += 1
				elif line.startswith('    '):
					sp += 1
				if len(line) > 120:
					long_ += 1
				if line != line.rstrip():
					trail += 1
			tabs += t > sp
			mixed += t > 0 and sp > 0
		print(f'   {name:14s} {len(paths):6d}   {tabs:10d}  {mixed:13d}  {crlf:4d}  {nofinal:16d}  {long_:9d}  {trail:20d}')
	print()

# DR-C21, DR-C22: space indent or trailing whitespace left in scope files (tools/tab_indent.py converts them; the
# lint fails in check_all as tab_indent).
if wants('format'):
	out = subprocess.run([sys.executable, str(ROOT / 'tools/tab_indent.py'), '--lint'], cwd=ROOT, capture_output=True,
						 text=True).stdout.rstrip().split('\n')
	hits = [(l.split(':', 1)[0], 0, l.split(':', 1)[1].strip()) for l in out if l and not l.startswith('tab_indent:')]
	report('DR-C21', 'own source files with space indent or trailing whitespace (tab_indent.py converts them)', hits,
		   'files', top=20)

# DR-C26: dates in code comments (tools/history_extract.py moves them to the writeup History; it fails in check_all),
# and source folders with no owning writeup in tools/writeup_owner.json (history_extract.py files lines by it).
if wants('history'):
	out = subprocess.run([sys.executable, str(ROOT / 'tools/history_extract.py'), '--lint'], cwd=ROOT,
						 capture_output=True, text=True).stdout.rstrip().split('\n')
	hits = [(l.split(':', 2)[0], int(l.split(':', 2)[1]), l.split(':', 2)[2].strip()) for l in out
			if l.count(':') >= 2 and l.split(':', 2)[1].isdigit()]
	report('DR-C26', 'dated sentences in code comments (history_extract.py moves them)', hits)
	import json
	owner = [k for k in json.loads((ROOT / 'tools/writeup_owner.json').read_text(encoding='utf-8')) if not k.startswith('_')]
	folders = set()
	for p in FILES:
		m = re.match(r'(development/[^/]+/|kubejs/(?:data|assets)/[^/]+/|kubejs/[a-z]+_scripts/|tools/|build-logic/)', p)
		if m and Path(p).suffix in ('.java', '.js', '.py', '.gradle', '.sh'):
			folders.add(m.group(1))
	missing = [(f, 0, 'no entry in tools/writeup_owner.json') for f in sorted(folders)
			   if not any(f.startswith(k) for k in owner)]
	report('DR-C26', 'source folders with no owning writeup (WARNING: add a tools/writeup_owner.json entry)', missing,
		   'folders', top=20)

# DR-W3: test lines not in the D12 form (tools/test_line_fix.py fixes them; it fails in check_all as test_lines).
if wants('testlines'):
	out = subprocess.run([sys.executable, str(ROOT / 'tools/test_line_fix.py'), '--lint', '-v'], cwd=ROOT,
						 capture_output=True, text=True).stdout.rstrip().split('\n')
	hits = [(l.split(':', 2)[0], int(l.split(':', 2)[1]), l.split(':', 2)[2].strip()) for l in out
			if not l.startswith('AMBIGUOUS ') and l.count(':') >= 2 and l.split(':', 2)[1].isdigit()]
	report('DR-W3', 'test lines off the D12 form (test_line_fix.py --apply fixes them)', hits)
	amb = [(l.split(' ', 1)[1].split(':', 2)[0], int(l.split(' ', 1)[1].split(':', 2)[1]), l.split(':', 3)[-1].strip())
		   for l in out if l.startswith('AMBIGUOUS ')]
	report('DR-W3', 'test lines the fixer cannot read (WARNING: fix by hand or leave as prose)', amb)

# DR-W11: doc file names.
if wants('docnames'):
	hits = [(p, 0, Path(p).name) for p in FILES if p.startswith('feature_writeup/') and p.endswith('.md')
			and not re.fullmatch(r'[a-z0-9_]+(_\d{4}-\d\d-\d\d[a-z]?)?(\.[0-9]+)*\.md|README\.md|TODO\.md|RUNNER\.md|[A-Z_]+\.md',
								 Path(p).name)
			and not (p.startswith('feature_writeup/briefs/')        # briefs/<family>_<sub>.md, sub keeps its hyphens
					 and re.fullmatch(r'[a-z0-9]+_[a-z0-9-]+\.md', Path(p).name))]
	report('DR-W11', 'writeup file names that are not snake_case', hits, 'files', top=20)
