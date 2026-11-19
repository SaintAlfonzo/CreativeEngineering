#!/usr/bin/env python3
"""Scratch queue rows as data (plan_process_overhaul.md P2.1, TX-42; owner ruling D4).

Every queue section lives in feature_writeup/testing/rows/<CODE>.qb (CODE = the section's code without SQ-/CQ-, so
SQ-MELT and CQ-MELT share MELT.qb). The file is a qrun batch: qrun.py runs it directly. scratch_queue.md shows each
section as a generated block (`<!-- generated:status_index begin rows_<CODE>_<KEY> ... -->`), rendered from the file
by tools/status_index.py; never edit inside those blocks, edit the .qb and run status_index.py.

File format (on top of the qrun batch format in qrun.py's docstring; qrun ignores every '@' line):

    @@section KEY             starts a section (KEY: integer, stable; new sections take the file's highest + 1)
    @@title TEXT              the queue heading without '## ' (date, writeup links, TODO ids)
    @@date YYYY-MM-DD         defaults for the section's rows:
    @@jars a b ...              own jars (mod ids) the rows test
    @@sources PATH ...          files the rows were written from (repo paths; a directory covers every file in it)
    @@tier T2                   T1 smoke, T2 feature (default), T3 release gate
    @@phase NAME                run-order phase of the section's server rows (scratch-test skill step 6): priority, server
                                (default), worldgen, long; client rows are phase client unless the row says otherwise.
                                qrun.py plan orders a run priority, server, longwait (a long row a Testing TODO row's
                                queue column cites), worldgen, client, long
    @@modset SPEC             the section's jar set (qmodset.py docstring: modsets/NAME.toml, e.g. `cc_only full -@iw`,
                              `port_chain cooking +"alexsmobs-*.jar"`): the driver builds the step's server from it
                              instead of plain prep_srv.sh; part of the step key. A row overrides it with @modset SPEC
                              (`@modset none` = the full pack)
    @@reviewed YYYY-MM-DD     the rows were last matched to their sources that day (default: @@date); qrowlint.py
                              flags a row whose sources changed later (stale)
    @@archived YYYY-MM-DD     every row PASSed on the jars still installed (qrun.py archive): the section is shown in
                              scratch_queue_archive.md with its verdict history instead of the queue; it stays data
                              (qrun.py plan still plans it when a jar or source changes)
    @@prep [A:|B:] [boot=N:] SPEC   pre-boot setup of the section's driver steps (qdrive.py, before prep_srv.sh and the
                              boot; by hand: qrun.py prep). Lines add up in order; a leading A: / B: limits the line to the
                              step of that world group; boot=N: (N >= 2) makes it a change before boot N of a multi-boot
                              step (rows with `@boot N`), applied to the folder and world boot N-1 left (untagged lines
                              are boot 1's). SPEC is shell-quoted (shlex) and one of:
                                KEY=VALUE ...           UPPERCASE KEY -> prep_srv.sh environment (NOSWAP, SKIPSWAP,
                                                        COPYDEV, PGJAR); lowercase key -> server.properties (prep_srv.sh
                                                        prop=value: difficulty, spawn-animals, spawn-monsters, level-seed)
                                toml FILE SECTION KEY VALUE   qrun.py toml on OUT/srv/FILE after prep (FILE without '/'
                                                        is config/FILE; created when absent); VALUE is the rest of the
                                                        line, verbatim; a SECTION with spaces is quoted with its own
                                                        quotes inside: '"Loading Protection Configuration"';
                                                        SECTION `-` = a top-level key (before the first [section])
                                copy SRC DEST           repo file or directory SRC -> OUT/srv/DEST (DEST ending in '/' = into)
                                rm PATH                 delete OUT/srv/PATH (a glob; a jar the section runs without)
                                run CMD                 bash CMD (the rest of the line, verbatim) in OUT/srv with $INST,
                                                        $OUT, $SRV and $BOOT set, logged in sNN_prep.txt; a non-zero
                                                        exit fails the step; srv/mods is re-pinned after it
                                fresh                   boot=N: only: a new world for boot N (level-name LEVEL_bN)
                                modset SPEC             boot=N: only: switch srv/mods (and kubejs / config by the
                                                        manifest) to mod set SPEC, world kept (qmodset.py switch);
                                                        `modset pack` = back to the full pack
                              UPPERCASE keys are boot 1 only (prep_srv.sh runs once); boot=N: lowercase keys are set in
                              server.properties before boot N.
                              Only what every planned row of the section needs before its boot belongs here; a row that
                              restarts with other values keeps its own `shell` / `qrun.py toml` steps. Rendered into the
                              queue as a "Driver prep" line.
    @@text LINE               one markdown line of the section, verbatim ('@@text' alone = blank line)
    @@table                   the standard table header: | ID | Group | Commands | Pass | If it fails | and |---|...
    @@table world             the same with World in place of Group (rows that need a particular world; DR-W4)
    ## ID | title             a row (ID exactly as the queue shows it)
    @group / @commands / @pass / @fails TEXT     the table cells (columns 2 to 5; @c6, @c7 for wider tables)
    @jars / @sources / @tier / @phase  override the section defaults
    @owner true               design-feel row the owner judges in a playtest (never automated)
    @client true              needs a client (default: true for CQ- ids)
    @reviewed YYYY-MM-DD      as @@reviewed, for this row
    @from PATH [passed]       where the steps came from (a past batch; "passed" = a run with that text passed)
    @modset SPEC              this row's jar set (overrides @@modset; `none` = the full pack)
    @boot N                   driver boot phase (default 1): the step runs boots 1..N in order on one server folder and
                              world, each boot's rows after its `@@prep boot=N:` lines (qdrive.py); a planned row of boot
                              N brings the section's rows of lower boots (same world group) into the step as setup
    @superseded TEXT          the row no longer applies (feature removed or replaced; TEXT names the replacement):
                              never planned, not counted as never-run
    @draft TEXT               no runnable steps yet: the next runner writes them here once (the reason in TEXT)
    @absent ID ... (TEXT)     ids the steps name that the dumps do not list, on purpose: removed ids a negative test
                              gives, old ids an old-jar boot places, ids registered after the last /dumpall;
                              qrowlint.py skips them (ids in TEXT count too)
    > cmd / waitlog / ...     the row's steps (qrun format)

Commands:
    qrows.py migrate [--dry] [--only CODE,...]   move queue sections into rows/*.qb (newest first is irrelevant: one pass)
    qrows.py check                               every .qb parses; rendering each section equals its queue block
    qrows.py stats                               rows, drafts, steps, owner rows per file
    qrows.py jars [--dry]                        reset each section's @@jars from features.csv (jars of the file's code
                                                 and of every row code in the section); row-level @jars stay
"""
import collections
import json
import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
QUEUE = ROOT / 'feature_writeup/testing/scratch_queue.md'
ROWS = Path(os.environ.get('QROWS_DIR', ROOT / 'feature_writeup/testing/rows'))
BATCHES = ROOT / 'tools/scratch_query/batches'
RUNS = ROOT / 'perf/runs'
FEATURES = ROOT / 'feature_writeup/status/features.csv'

STD_HEAD = '| ID | Group | Commands | Pass | If it fails |'
WORLD_HEAD = '| ID | World | Commands | Pass | If it fails |'
STD_SEP = '|---|---|---|---|---|'
CELL_KEYS = ['id', 'group', 'commands', 'pass', 'fails']
ROW_RE = re.compile(r'^\|\s*((?:SQ|CQ)-[A-Za-z][A-Za-z0-9]*)-\S')
CODE_RE = re.compile(r'\b(?:SQ|CQ)-([A-Za-z][A-Za-z0-9]*)(?=-[A-Za-z0-9])')
BLOCK_BEGIN = re.compile(r'^<!-- generated:status_index begin (rows_(\w+?)_(\d+)) sha1=([0-9a-f]+) -->$')
SECTION_META = ('title', 'date', 'reviewed', 'jars', 'sources', 'tier', 'phase', 'archived', 'modset')
ROW_META = ('group', 'commands', 'pass', 'fails', 'jars', 'sources', 'tier', 'phase', 'owner', 'client', 'reviewed', 'from', 'draft', 'superseded', 'absent', 'boot', 'modset')
TIERS = ('T1', 'T2', 'T3')
# run order (scratch-test skill step 6); longwait is never written, qplan.py derives it from long + a Testing TODO row
PHASES = ('priority', 'server', 'longwait', 'worldgen', 'client', 'long')


def cells_of(line):
	"""Cells of a table line. A '|' inside a backtick span or escaped as '\\|' is cell text (queue rows quote grep
    patterns with bare pipes); a line without the closing '|' keeps its last cell."""
	line = line.strip()
	parts, cur, tick, i = [], '', False, 0
	if line.count('`') % 2:
		tick = None
	while i < len(line):
		ch = line[i]
		if ch == '\\' and i + 1 < len(line):
			cur += line[i:i + 2]
			i += 2
			continue
		if ch == '`' and tick is not None:
			tick = not tick
		if ch == '|' and not tick:
			parts.append(cur.strip())
			cur = ''
		else:
			cur += ch
		i += 1
	parts.append(cur.strip())
	return parts[1:-1] if line.endswith('|') else parts[1:]


def cell_key(i):
	return CELL_KEYS[i] if i < len(CELL_KEYS) else 'c%d' % (i + 1)


def row_line(row):
	cells = [row['id']] + [row['meta'].get(cell_key(i), '') for i in range(1, row['ncells'])]
	return '|' + ''.join(' %s |' % c if c else ' |' for c in cells)


# ---------------------------------------------------------------- .qb files

def load(path, errors=None):
	"""{'path', 'code', 'sections': [{key, meta, items}]}; items: ('text', line) or ('row', row);
    row: {id, title, meta, ncells, steps (raw lines), line}."""
	path = Path(path)
	out = {'path': path, 'code': path.stem, 'sections': []}
	sec = row = None

	def bad(n, msg):
		if errors is None:
			raise SystemExit('%s:%d: %s' % (path, n, msg))
		errors.append((n, msg))

	for n, raw in enumerate(path.read_text().split('\n'), 1):
		s = raw.rstrip()
		if s.startswith('@@'):
			word, _, rest = s[2:].partition(' ')
			if word == 'section':
				sec = {'key': int(rest), 'meta': {}, 'items': [], 'line': n}
				out['sections'].append(sec)
				row = None
			elif sec is None:
				bad(n, '%s before the first @@section' % word)
			elif word == 'text':
				row = None
				sec['items'].append(('text', raw[7:] if raw.startswith('@@text ') else ''))
			elif word == 'table':
				row = None
				if rest.strip() not in ('', 'world'):
					bad(n, '@@table takes nothing or "world"')
				sec['items'] += [('text', WORLD_HEAD if rest.strip() == 'world' else STD_HEAD), ('text', STD_SEP)]
			elif word == 'prep':
				try:
					prep_line(rest)
				except ValueError as e:
					bad(n, '@@prep: %s' % e)
				sec.setdefault('prep', []).append(rest.strip())
			elif word in SECTION_META:
				sec['meta'][word] = rest
				if word == 'modset':
					err = modset_error(rest)
					if err:
						bad(n, '@@modset: %s' % err)
			else:
				bad(n, 'unknown section field @@%s' % word)
			continue
		if s.startswith('## '):
			if sec is None:
				bad(n, 'row before the first @@section')
				continue
			rid, _, title = s[3:].partition('|')
			row = {'id': rid.strip(), 'title': title.strip(), 'meta': {}, 'ncells': 0, 'steps': [], 'line': n}
			sec['items'].append(('row', row))
			continue
		if s.startswith('@'):
			word, _, rest = s[1:].partition(' ')
			if row is None:
				bad(n, '@%s outside a row' % word)
			elif word in ROW_META or re.fullmatch(r'c\d+', word):
				row['meta'][word] = rest
				if word == 'modset':
					err = modset_error(rest)
					if err:
						bad(n, '@modset: %s' % err)
				if word in CELL_KEYS or re.fullmatch(r'c\d+', word):
					i = CELL_KEYS.index(word) if word in CELL_KEYS else int(word[1:]) - 1
					row['ncells'] = max(row['ncells'], i + 1)
			else:
				bad(n, 'unknown row field @%s' % word)
			continue
		if row is not None and (s or row['steps']):
			row['steps'].append(raw)
	for sec in out['sections']:
		for kind, r in sec['items']:
			if kind == 'row':
				while r['steps'] and not r['steps'][-1].strip():
					r['steps'].pop()
				r['ncells'] = max(r['ncells'], 2)
	return out


def modset_error(text):
	"""'' when TEXT is a valid mod-set spec (qmodset.py) or 'none', else the reason."""
	if text.strip() == 'none':
		return ''
	sys.path.insert(0, str(Path(__file__).resolve().parent))
	try:
		import qmodset
		qmodset.parse_spec(text)
	except (ValueError, ImportError) as e:
		return str(e)
	except Exception as e:  # noqa: BLE001  (a TOML syntax error in a manifest)
		return '%s: %s' % (type(e).__name__, e)
	return ''


def all_files():
	return sorted(ROWS.glob('*.qb'))


def runnable(row):
	return any(l.strip() and not l.strip().startswith('#') for l in row['steps'])


def effective(sec, row):
	"""Row metadata with the section defaults filled in."""
	m = dict(row['meta'])
	for k in ('jars', 'sources', 'tier', 'date'):
		if not m.get(k) and sec['meta'].get(k):
			m[k] = sec['meta'][k]
	m.setdefault('tier', 'T2')
	# mod set (r1118 R4 B1): the row's @modset, else the section's @@modset; 'none' = the full pack
	ms = (m.get('modset') or sec['meta'].get('modset') or '').strip()
	m['modset'] = '' if ms == 'none' else ms
	if 'client' not in m:
		m['client'] = 'true' if row['id'].startswith('CQ-') else 'false'
	if not m.get('phase'):
		m['phase'] = 'client' if m['client'] == 'true' else sec['meta'].get('phase') or 'server'
	return m


def tiers_of(path):
	"""row id -> effective tier for a queue row file; {} for a plain batch."""
	out = {}
	for sec in load(path, errors=[])['sections']:
		for kind, r in sec['items']:
			if kind == 'row':
				out[r['id']] = effective(sec, r)['tier']
	return out


PREP_ENV = ('NOSWAP', 'SKIPSWAP', 'COPYDEV', 'PGJAR')
PREP_ACTIONS = {'toml': 4, 'copy': 2, 'rm': 1, 'run': 1, 'fresh': 0, 'modset': 1}


def prep_line(text):
	"""One @@prep line -> (group or None, kind, args) (prep_line_boot without the boot number)."""
	group, _boot, kind, args = prep_line_boot(text)
	return group, kind, args


def prep_line_boot(text):
	"""One @@prep line -> (group or None, boot (1 when untagged), kind, args). kind 'set': args [(KEY, VALUE)];
    'toml' / 'copy' / 'rm': the words (toml VALUE = the rest of the line); 'run': [the rest of the line]; 'fresh': [].
    ValueError on a bad line."""
	import shlex
	text = text.strip()
	group, boot = None, 1
	for _ in range(2):
		m = re.match(r'([AB]):\s*', text)
		if m and group is None:
			group, text = m.group(1), text[m.end():]
			continue
		m = re.match(r'boot=(\d+):\s*', text)
		if m and boot == 1:
			boot, text = int(m.group(1)), text[m.end():]
			if boot < 1:
				raise ValueError('boot=N: wants N >= 1')
	if not text:
		raise ValueError('empty')
	word = text.split()[0]
	if word == 'run':
		rest = text[3:].strip()
		if not rest:
			raise ValueError('run takes a command')
		return group, boot, 'run', [rest]
	if word == 'modset':
		rest = text[6:].strip()
		if boot < 2:
			raise ValueError('modset in @@prep is a boot=N: (N >= 2) action; boot 1 takes the section @@modset')
		err = modset_error(rest) if rest != 'none' else 'modset none: name the set (modset pack = the full pack)'
		if err:
			raise ValueError(err)
		return group, boot, 'modset', [rest]
	if word == 'fresh':
		if text.strip() != 'fresh':
			raise ValueError('fresh takes no argument')
		if boot < 2:
			raise ValueError('fresh is a boot=N: (N >= 2) action: boot 1 is always a new world')
		return group, boot, 'fresh', []
	if word in PREP_ACTIONS:
		n = PREP_ACTIONS[word]
		if word == 'toml':
			# FILE SECTION KEY shell-quoted (a section with spaces keeps its inner quotes: '"A B"'); VALUE raw
			lex = shlex.shlex(text, posix=True)
			lex.whitespace_split = True
			args = [lex.get_token() for _ in range(4)][1:]
			args = [a for a in args if a is not None]
			rest = lex.instream.read().strip()
			if rest:
				args.append(rest)
		else:
			args = shlex.split(text)[1:]
		if len(args) != n:
			raise ValueError('%s takes %d argument(s), got %d' % (word, n, len(args)))
		dest = args[0] if word == 'toml' else args[-1]
		if '..' in Path(dest).parts or dest.startswith('/'):
			raise ValueError('%s: the srv path must be relative, without ..' % word)
		return group, boot, word, args
	pairs = []
	for tok in shlex.split(text):
		k, eq, v = tok.partition('=')
		if not eq or not re.fullmatch(r'[A-Za-z][A-Za-z0-9_.-]*', k):
			raise ValueError('expected KEY=VALUE, toml, copy or rm, got %r' % tok)
		if k.isupper() and k not in PREP_ENV:
			raise ValueError('unknown prep_srv.sh variable %s (known: %s)' % (k, ', '.join(PREP_ENV)))
		if k.isupper() and boot > 1:
			raise ValueError('%s is a prep_srv.sh variable: boot 1 only (prep_srv.sh runs once, before boot 1)' % k)
		pairs.append((k, v))
	return group, boot, 'set', pairs


def prep_of(sec, group, boot=1):
	"""The section's @@prep for boot BOOT of a step of world group A/B: {'env': {KEY: VALUE}, 'props': ['k=v', ...],
    'actions': [(kind, args), ...], 'lines': [the @@prep lines that apply]}. Untagged lines are boot 1's."""
	out = {'env': {}, 'props': [], 'actions': [], 'lines': []}
	for text in sec.get('prep', []):
		g, b, kind, args = prep_line_boot(text)
		if (g and g != group) or b != boot:
			continue
		out['lines'].append(text)
		if kind == 'set':
			for k, v in args:
				if k.isupper():
					out['env'][k] = v
				else:
					out['props'].append('%s=%s' % (k, v))
		else:
			out['actions'].append((kind, args))
	return out


def section_of(path, key):
	"""Section KEY of a rows file, or None."""
	for sec in load(path)['sections']:
		if str(sec['key']) == str(key):
			return sec
	return None


def render(sec):
	"""The section as queue markdown: heading, then its lines; row lines rebuilt from the cells."""
	out = ['## ' + sec['meta'].get('title', '')]
	if sec.get('prep'):
		out += ['', 'Driver prep (`@@prep`, applied before each boot): ' + '; '.join('`%s`' % t for t in sec['prep'])]
	if sec['meta'].get('modset'):
		out += ['', 'Driver mod set (`@@modset`, tools/scratch_query/modsets/): `%s`' % sec['meta']['modset']]
	for kind, x in sec['items']:
		out.append(x if kind == 'text' else row_line(x))
	return out


def block_name(code, key):
	return 'rows_%s_%d' % (code, key)


def sections_by_block():
	return {block_name(f['code'], s['key']): (f, s) for f in map(load, all_files()) for s in f['sections']}


def write_qb(path, code, sections, header=None):
	lines = header or [
		'# Scratch queue rows %s (plan_process_overhaul.md P2.1). Data: the queue table in scratch_queue.md is generated' % code,
		'# from this file (python3 tools/status_index.py). Format: tools/scratch_query/qrows.py docstring.',
		'# Run: qrun.py run OUT %s RES [--section KEY] [--rows ID,...]' % os.path.relpath(path, ROOT)]
	for sec in sections:
		lines += ['', '@@section %d' % sec['key']]
		for k in SECTION_META:
			if sec['meta'].get(k):
				lines.append('@@%s %s' % (k, sec['meta'][k]))
		lines += ['@@prep ' + t for t in sec.get('prep', [])]
		items = sec['items']
		i = 0
		while i < len(items):
			kind, x = items[i]
			if kind == 'text':
				if x in (STD_HEAD, WORLD_HEAD) and i + 1 < len(items) and items[i + 1] == ('text', STD_SEP):
					lines.append('@@table' if x == STD_HEAD else '@@table world')
					i += 2
					continue
				lines.append(('@@text ' + x) if x else '@@text')
			else:
				lines.append('## %s | %s' % (x['id'], x['title']) if x['title'] else '## ' + x['id'])
				for k in list(CELL_KEYS[1:]) + sorted((k for k in x['meta'] if re.fullmatch(r'c\d+', k)),
													   key=lambda k: int(k[1:])) + list(ROW_META[4:]):
					if k in x['meta'] and (x['meta'][k] or k in CELL_KEYS):
						lines.append(('@%s %s' % (k, x['meta'][k])).rstrip())
				lines += x['steps']
				if x['steps']:
					lines.append('')
			i += 1
	while lines and not lines[-1]:
		lines.pop()
	path.write_text('\n'.join(lines) + '\n')


# ---------------------------------------------------------------- migration

def queue_sections(lines):
	"""[(start, end)] of every '## ' section of the queue that holds at least one row line and is not already a
    generated rows block. end is exclusive."""
	heads = [i for i, l in enumerate(lines) if l.startswith('## ')]
	out = []
	for j, h in enumerate(heads):
		end = heads[j + 1] if j + 1 < len(heads) else len(lines)
		# a generated block or a trailing marker ends the section
		for k in range(h + 1, end):
			if lines[k].startswith('<!-- generated:status_index'):
				end = k
				break
		if any(ROW_RE.match(l) for l in lines[h:end]):
			out.append((h, end))
	return out


def section_code(head, body):
	m = CODE_RE.search(head)
	if m:
		return m.group(1)
	c = collections.Counter(ROW_RE.match(l).group(1).split('-', 1)[1] for l in body if ROW_RE.match(l))
	return c.most_common(1)[0][0]


def feature_jars():
	"""queue code (no SQ-/CQ-) -> set of jars, from features.csv."""
	out = collections.defaultdict(set)
	if not FEATURES.exists():
		return out
	sys.path.insert(0, str(ROOT / 'tools'))
	import table_io
	for r in table_io.read_dicts(FEATURES):
		jars = [j.strip() for j in r.get('jars', '').split(',') if j.strip()]
		for c in r.get('queue codes', '').split(','):
			c = c.strip()
			if c:
				out[c.split('-', 1)[1]].update(jars)
	return out


def head_sources(head):
	out = []
	for m in re.finditer(r'\]\((\.\./[^)#\s]+|[^)#\s:]+\.md)', head):
		p = os.path.normpath(os.path.join('feature_writeup/testing', m.group(1)))
		if p not in out and (ROOT / p).exists():
			out.append(p)
	return out


def batch_index():
	"""row id -> [(mtime, path, title, steps)] from every past batch."""
	idx = collections.defaultdict(list)
	for p in BATCHES.rglob('*.qb'):
		try:
			lines = p.read_text(errors='replace').split('\n')
		except OSError:
			continue
		mt = p.stat().st_mtime
		cur = None
		for l in lines + ['## ']:
			if l.startswith('## '):
				if cur:
					st = cur[2]
					while st and not st[-1].strip():
						st.pop()
					idx[cur[0]].append((mt, str(p.relative_to(ROOT / 'tools/scratch_query')), cur[1], st))
				rid, _, title = l[3:].partition('|')
				cur = (rid.strip(), title.strip(), []) if rid.strip() else None
			elif cur is not None and not l.startswith('pace '):
				cur[2].append(l)
	return idx


def passed_titles():
	"""row id -> titles that ended PASS in some qrun results.json."""
	out = collections.defaultdict(set)
	for p in RUNS.rglob('results.json'):
		try:
			d = json.loads(p.read_text())
		except (OSError, ValueError):
			continue
		for rid, r in d.items():
			if isinstance(r, dict) and r.get('result') == 'PASS':
				out[rid].add(r.get('title', ''))
	return out


def pick_steps(rid, idx, passed):
	cands = [c for c in idx.get(rid, ()) if any(l.strip() and not l.strip().startswith('#') for l in c[3])]
	if not cands:
		return None
	good = [c for c in cands if c[2] in passed.get(rid, ())]
	c = max(good or cands)
	return {'from': 'batches/' + c[1].split('batches/', 1)[-1] + (' passed' if good else ''), 'title': c[2],
			'steps': c[3]}


OWNER_RE = re.compile(r'\bowner (judges|decides|looks?|feel|playtest)|\bfeel(s)? right\b|\blooks? right\b', re.I)


def reset_jars(argv):
	"""P3.3 item 4: the section jars follow the repaired features.csv."""
	dry = '--dry' in argv
	jars_of = feature_jars()
	files = changed = 0
	for path in all_files():
		lines = path.read_text().split('\n')
		starts = [i for i, l in enumerate(lines) if l.startswith('@@section ')] + [len(lines)]
		new = list(lines)
		inserts = []
		touched = False
		for a, b in zip(starts, starts[1:]):
			codes = {path.stem}
			for l in lines[a:b]:
				m = re.match(r'## (?:SQ|CQ)-([A-Z0-9]+)-', l)
				if m:
					codes.add(m.group(1))
			jars = ' '.join(sorted(set().union(*(jars_of.get(c, set()) for c in codes)) - {'kubejs', 'datapack', 'tools'}))
			at = next((i for i in range(a, b) if lines[i].startswith('@@jars')), None)
			old = lines[at][7:].strip() if at is not None else ''
			if old == jars:
				continue
			if at is not None:
				new[at] = '@@jars ' + jars if jars else None
			elif jars:
				after = max(i for i in range(a, b) if lines[i].startswith(('@@section', '@@title', '@@date')))
				inserts.append((after + 1, '@@jars ' + jars))
			changed += 1
			touched = True
			print('%s section %s: %s -> %s' % (path.name, lines[a][10:], old or '(none)', jars or '(none)'))
		if touched:
			files += 1
			for i, l in sorted(inserts, reverse=True):
				new.insert(i, l)
			if not dry:
				path.write_text('\n'.join(l for l in new if l is not None))
	print('%d section(s) in %d file(s) %s' % (changed, files, 'would change' if dry else 'changed'))
	return 0


def migrate(argv):
	dry = '--dry' in argv
	only = None
	if '--only' in argv:
		only = set(argv[argv.index('--only') + 1].split(','))
	lines = QUEUE.read_text().split('\n')
	jars_of = feature_jars()
	idx, passed = batch_index(), passed_titles()
	files = collections.defaultdict(list)
	existing = {p.stem: load(p) for p in all_files()}
	plan = []
	stats = collections.Counter()
	for h, end in queue_sections(lines):
		head, body = lines[h], lines[h + 1:end]
		while body and not body[-1].strip():
			body.pop()
		code = section_code(head, body)
		if only and code not in only:
			continue
		have = existing.get(code, {'sections': []})['sections'] + files[code]
		key = max([s['key'] for s in have], default=0) + 1
		title = head[3:]
		d = re.search(r'\b(20\d\d-\d\d-\d\d)\b', head)
		codes = {code} | {ROW_RE.match(l).group(1).split('-', 1)[1] for l in body if ROW_RE.match(l)}
		jars = sorted(set().union(*(jars_of.get(c, set()) for c in codes)))
		meta = {'title': title, 'date': d.group(1) if d else '', 'jars': ' '.join(jars),
				'sources': ' '.join(head_sources(head))}
		items = []
		for l in body:
			if ROW_RE.match(l):
				cells = cells_of(l)
				rid = cells[0]
				row = {'id': rid, 'title': '', 'meta': {cell_key(i): c for i, c in enumerate(cells) if i},
					   'ncells': len(cells), 'steps': [], 'line': 0}
				got = pick_steps(rid, idx, passed)
				if got:
					row['title'], row['steps'] = got['title'], got['steps']
					row['meta']['from'] = got['from']
					stats['steps' + (' (passed)' if got['from'].endswith('passed') else '')] += 1
				else:
					row['meta']['draft'] = 'no batch holds this row yet'
					stats['draft'] += 1
				if OWNER_RE.search(row['meta'].get('pass', '')):
					row['meta']['owner'] = 'true'
					stats['owner'] += 1
				if row_line(row) != l.rstrip():
					stats['row line normalized'] += 1
				stats['rows'] += 1
				items.append(('row', row))
			else:
				items.append(('text', l))
		sec = {'key': key, 'meta': meta, 'items': items}
		files[code].append(sec)
		plan.append((h, end, code, key, len(body)))
		stats['sections'] += 1
	print('migrate: %s' % ', '.join('%s %d' % kv for kv in sorted(stats.items())))
	print('files: %d (%s)' % (len(files), ' '.join(sorted(files))))
	if dry:
		return 0
	ROWS.mkdir(parents=True, exist_ok=True)
	for code, secs in files.items():
		old = existing.get(code)
		write_qb(ROWS / ('%s.qb' % code), code, (old['sections'] if old else []) + secs)
	sys.path.insert(0, str(ROOT / 'tools'))
	import status_index
	for h, end, code, key, _ in sorted(plan, reverse=True):
		sec = next(s for s in load(ROWS / ('%s.qb' % code))['sections'] if s['key'] == key)
		content = render(sec)
		lines[h:end] = status_index.empty_block(block_name(code, key), content) + ([''] if end < len(lines) else [])
	QUEUE.write_text('\n'.join(lines))
	print('queue: %d sections now generated blocks' % len(plan))
	return 0


# ---------------------------------------------------------------- check / stats

def check(argv=()):
	fails = []
	secs = {}
	for p in all_files():
		errs = []
		f = load(p, errs)
		for n, m in errs:
			fails.append('%s:%d: %s' % (os.path.relpath(p, ROOT), n, m))
		keys = collections.Counter(s['key'] for s in f['sections'])
		for k, c in keys.items():
			if c > 1:
				fails.append('%s: section key %d used %d times' % (os.path.relpath(p, ROOT), k, c))
		for s in f['sections']:
			secs[block_name(f['code'], s['key'])] = s
			for kind, r in s['items']:
				if kind != 'row':
					continue
				m = effective(s, r)
				if m['phase'] not in PHASES or m['phase'] == 'longwait':
					fails.append('%s:%d: %s phase %r not in %s' % (os.path.relpath(p, ROOT), r['line'], r['id'], m['phase'],
																   '/'.join(x for x in PHASES if x != 'longwait')))
				if m['tier'] not in TIERS:
					fails.append('%s:%d: %s tier %r not in %s' % (os.path.relpath(p, ROOT), r['line'], r['id'], m['tier'],
																  '/'.join(TIERS)))
				if not runnable(r) and 'draft' not in r['meta'] and m.get('owner') != 'true' and \
						'superseded' not in r['meta']:
					fails.append('%s:%d: %s has no steps and no @draft' % (os.path.relpath(p, ROOT), r['line'], r['id']))
	return fails, secs


def stats(argv=()):
	tot = collections.Counter()
	for p in all_files():
		f = load(p)
		c = collections.Counter()
		for s in f['sections']:
			for kind, r in s['items']:
				if kind == 'row':
					c['rows'] += 1
					c['runnable' if runnable(r) else 'draft'] += 1
					c['owner'] += r['meta'].get('owner') == 'true'
		tot.update(c)
		print('%-10s %4d sections %4d rows %4d runnable %4d draft %3d owner' % (
			f['code'], len(f['sections']), c['rows'], c['runnable'], c['draft'], c['owner']))
	print('total: %d rows, %d runnable, %d draft, %d owner' % (tot['rows'], tot['runnable'], tot['draft'], tot['owner']))


def main(argv):
	if not argv:
		print(__doc__)
		return 2
	if argv[0] == 'jars':
		return reset_jars(argv[1:])
	if argv[0] == 'migrate':
		return migrate(argv[1:])
	if argv[0] == 'check':
		fails, _ = check()
		for f in fails:
			print('FAIL ' + f)
		print('qrows: %s' % ('FAIL %d' % len(fails) if fails else 'OK'))
		return 1 if fails else 0
	if argv[0] == 'stats':
		stats()
		return 0
	print(__doc__)
	return 2


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
