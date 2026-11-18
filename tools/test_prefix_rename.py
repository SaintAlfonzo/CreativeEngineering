#!/usr/bin/env python3
"""Apply the prefix renames of feature_writeup/testing/test_prefix_index.md section 2 (plan_rules_fixes.md section 2).

Dry run by default: prints a per-file count and writes the full report (every change, every occurrence left as the
kept prefix in a file that also names a renamed doc) to the report path. `--apply` rewrites.

	python3 tools/test_prefix_rename.py                 # dry run
	python3 tools/test_prefix_rename.py --apply
	python3 tools/test_prefix_rename.py --report out.txt

How an occurrence is resolved (first rule that decides wins):
1. In a renamed doc (OWNER files below) every bare id of the old prefix is renamed.
2. In a queue rows file, a SECTION rule names the target for the ids of that file (and section).
3. A queue id whose number only one target uses (UNIQUE) is renamed everywhere.
4. Keywords on the line (and, in a .qb file, the section title) pick the target; no keyword = the id stays (it is
   the kept doc's).
Run docs (testing/scratch_run_*, testing/scratch_queue_run_*) keep the old id and get "(now X)" after its first
quote in the file. Not touched: the index itself, generated files (TODO.md, scratch_queue.md; rerun
tools/status_index.py), design_rules.md and plan_rules_fixes.md (they quote the old ids as evidence), dated status
snapshots, tools/scratch_query/batches/ (past run inputs).
"""
import argparse
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FW = 'feature_writeup/'

# old prefix -> list of targets. Each target: new prefix, owner docs (bare ids renamed), keyword regex for references.
RENAMES = {
	'EN': [
		{'new': 'EDL', 'owners': [FW + 'ends_delight.md'],
			'kw': r"ends_delight|End'?s( and Ender'?s)? Delight|Ender'?s Delight|\bEDL\b|server half|2026-10-26 run|Round 1"},
		{'new': 'ENC', 'owners': [FW + 'briefs/potions_enchanting.md'],
			'kw': r'potions_enchanting|enchant|librarian|cashier|\bNEA\b|Nock Enough|Bug batch|SQ-HR-05'},
	],
	'LT': [{'new': 'LTP', 'owners': [FW + 'loading_tips.md', FW + 'loading_tips_audit.md', FW + 'plan_loading_tips_screens.md'],
			'kw': r'loading_tips|loading[- ](screen )?tip|tip bar|LTC'}],
	'PB': [{'new': 'PFS', 'owners': [FW + 'perf_suite.md'], 'kw': r'perf_suite|perf suite|perfBot|bot player'}],
	'BP': [{'new': 'BOT', 'owners': [FW + 'botany_pots.md'], 'kw': r'botany_pots|Botany Pots'}],
	'RP': [
		{'new': 'RPB', 'owners': [FW + 'perf_rpack.md'], 'kw': r'perf_rpack|resource pack bench|rpack'},
		{'new': 'RPI', 'owners': [FW + 'raspberry_pie.md'], 'kw': r'raspberry|Raspberry'},
	],
	'CN': [{'new': 'CORN', 'owners': [FW + 'corn_delight.md'], 'kw': r'corn_delight|Corn Delight|\bcorn'}],
	'IP': [{'new': 'PRT', 'owners': [FW + 'immersive_portals.md'], 'kw': r'immersive_portals|Immersive Portals|portal'}],
	'TS': [{'new': 'TSY', 'owners': [FW + 'trade_sync.md'], 'kw': r'trade_sync|TradeSync|Trade Sync'}],
	# queue ids only (the bare writeup prefix MB- stays mooblooms): monster box rows and the Mulberry run (follow-up F3)
	'MB': [
		{'new': 'MBX', 'owners': [], 'kw': r'monster box|monster_box|mob_block_tweaks|vault'},
		{'new': 'RW', 'owners': [], 'kw': r'mulberry|recipes_wood|river|removed ?wood'},
	],
}
QUEUE_ONLY = {'MB'}
ROWS = FW + 'testing/rows/'
# queue rows files: (file, section key or None for all, line regex or None) -> {old: new}
SECTION = [
	(ROWS + 'EN.qb', None, None, {'EN': 'EDL'}),
	(ROWS + 'SSP.qb', None, None, {'EN': 'ENC'}),
	(ROWS + 'LT.qb', None, None, {'LT': 'LTP'}),
	(ROWS + 'LTC.qb', None, None, {'LT': 'LTP'}),
	(ROWS + 'CC.qb', None, r'CQ-LT-', {'LT': 'LTP'}),
	(ROWS + 'PB.qb', None, None, {'PB': 'PFS'}),
	(ROWS + 'RP.qb', 1, None, {'RP': 'RPB'}),
	(ROWS + 'RP.qb', 2, None, {'RP': 'RPI'}),
	(ROWS + 'RD.qb', None, None, {'CN': 'CORN'}),
	(ROWS + 'FO.qb', None, None, {'CN': 'CORN'}),
	(ROWS + 'IP.qb', 2, None, {'IP': 'PRT'}),
	(ROWS + 'SALT.qb', None, r'SQ-HR-05', {'EN': 'ENC'}),
	(ROWS + 'SALT.qb', None, r'SQ-EN-', {'EN': 'EDL'}),
	(ROWS + 'SALT.qb', None, r'SQ-PB-', {'PB': 'PFS'}),
	(ROWS + 'SALT.qb', None, r'SQ-RP', {'RP': 'RPI'}),
	(ROWS + 'SALT.qb', None, r'SQ-CN-', {'CN': 'CORN'}),
	(ROWS + 'MB.qb', None, None, {'MB': 'MBX'}),
	(FW + 'mob_block_tweaks.md', None, None, {'MB': 'MBX'}),
	(FW + 'testing/scratch_run_2026-10-31.md', None, None, {'MB': 'MBX'}),
	(FW + 'testing/scratch_run_2026-11-06.md', None, None, {'MB': 'MBX'}),
	(FW + 'recipes_wood.md', None, None, {'MB': 'RW'}),
	(FW + 'testing/scratch_run_2026-10-22b.md', None, None, {'MB': 'RW'}),
	(ROWS + 'SALT.qb', None, r'SQ-MB-0[0-4]', {'MB': 'RW'}),
	(ROWS + '_lint_baseline.csv', None, r'^EN\.qb,', {'EN': 'EDL'}),
	(ROWS + '_lint_baseline.csv', None, r'^SSP\.qb,', {'EN': 'ENC'}),
]
# docs that keep the old prefix (and their seed briefs): their bare ids are never renamed
KEPT = {
	'EN': re.compile(r'^feature_writeup/(engineering\.md|briefs/engineering_)'),
	'LT': re.compile(r'^feature_writeup/(loot\.md|briefs/loot_)'),
	'PB': re.compile(r'^feature_writeup/(potion_buckets\.md|briefs/potions_potion-buckets)'),
	'BP': re.compile(r'^feature_writeup/backpack_skins\.md'),
	'RP': re.compile(r'^feature_writeup/(recipe_progression\.md|briefs/recipes_progression)'),
	'CN': re.compile(r'^feature_writeup/(cans\.md|briefs/cans_)'),
	'IP': re.compile(r'^feature_writeup/(item_physics\.md|briefs/items_item-physics)'),
	'TS': re.compile(r'^feature_writeup/(trashslot\.md|plan_1\.0\.3\.md)'),
	'MB': re.compile(r'^feature_writeup/(mooblooms\.md|briefs/mobs_mooblooms)'),
}
# queue ids that mean one target wherever they appear: (kind, old) -> (new, numbers or None for all)
UNIQUE = {
	('SQ', 'EN'): ('EDL', {f'{n:02d}' for n in range(3, 11)}),
	('SQ', 'PB'): ('PFS', None),
	('SQ', 'CN'): ('CORN', None),
	('CQ', 'CN'): ('CORN', None),
	('CQ', 'LT'): ('LTP', None),
	('SQ', 'RP'): ('RPI', None),
	('CQ', 'RP'): ('RPI', {f'{n:02d}' for n in range(4, 40)}),
	('SQ', 'IP'): ('PRT', {'01', '02', '03', '04', '05'}),
	('CQ', 'IP'): ('PRT', None),
	('SQ', 'MB'): ('MBX', {'05', '06', '07'}),
	('CQ', 'MB'): ('MBX', None),
}
SKIP = {FW + 'testing/test_prefix_index.md', FW + 'todo/TODO.md', FW + 'testing/scratch_queue.md',
		FW + 'design_rules.md', FW + 'plan_rules_fixes.md'}
SKIP_RE = re.compile(r'^feature_writeup/status/(?!features\.csv)|^tools/scratch_query/batches/|/archive/')
RUN_DOC = re.compile(r'^feature_writeup/testing/(scratch_run_|scratch_queue_run_)')
ONLY = set()  # --only OLD[,OLD]: rename just these old prefixes (the others were applied earlier; reruns would repeat the run-doc notes)
ID = re.compile(r'(?<![\w-])(?:(SQ|CQ)-)?(' + '|'.join(RENAMES) + r')-(\d+[a-z]?)(?![\w])')


def scope():
	out = subprocess.run(['git', 'ls-files', 'feature_writeup', '.claude', 'CLAUDE.md', 'mod_sides.md', 'tools/scratch_query'],
						 cwd=ROOT, capture_output=True, text=True).stdout.split('\n')
	return [f for f in out if f and f not in SKIP and not SKIP_RE.search(f)
			and Path(f).suffix in ('.md', '.csv', '.qb', '.py', '.sh', '.js', '.txt')]


def resolve(path, kind, old, num, line, section, title):
	"""New prefix for one occurrence, or None (stays)."""
	targets = RENAMES[old]
	if old in QUEUE_ONLY and not kind:
		return None
	for t in targets:
		if path in t['owners']:
			return t['new']
	if not kind and KEPT[old].search(path):
		return None
	if True:
		for f, key, rx, mp in SECTION:
			if f == path and (key is None or key == section) and (rx is None or re.search(rx, line)) and old in mp:
				if kind or rx is None:
					return mp[old]
	if kind and (kind, old) in UNIQUE:
		new, nums = UNIQUE[(kind, old)]
		if nums is None or num[:2] in nums:
			return new
	hits = [t['new'] for t in targets if re.search(t['kw'], line + ' ' + title, re.I)]
	return hits[0] if len(hits) == 1 else None


def process(path, apply):
	text = (ROOT / path).read_text(encoding='utf-8')
	changes, kept = [], []
	out, section, title = [], None, ''
	now_done = set()
	run_doc = bool(RUN_DOC.search(path))
	any_kw = {o: [t for t in ts if re.search(t['kw'], text, re.I)] for o, ts in RENAMES.items()}
	for i, line in enumerate(text.split('\n'), 1):
		m = re.match(r'@@section (\d+)', line)
		if m:
			section, title = int(m.group(1)), ''
		if line.startswith('@@title '):
			title = line[8:]
		pieces, pos = [], 0
		for m in ID.finditer(line):
			kind, old, num = m.group(1), m.group(2), m.group(3)
			if ONLY and old not in ONLY:
				continue
			new = resolve(path, kind, old, num, line, section, title)
			if new is None:
				if any_kw[old] and not KEPT[old].search(path) and (kind or old not in QUEUE_ONLY):
					kept.append((i, m.group(0), line.strip()[:120]))
				continue
			repl = (kind + '-' if kind else '') + new + '-' + num
			if run_doc:
				if m.group(0) in now_done:
					continue
				now_done.add(m.group(0))
				repl = m.group(0) + f' (now {repl})'
			pieces.append(line[pos:m.start()] + repl)
			pos = m.end()
			changes.append((i, m.group(0), repl))
		out.append(''.join(pieces) + line[pos:] if pieces else line)
	if apply and changes:
		(ROOT / path).write_text('\n'.join(out), encoding='utf-8')
	return changes, kept


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	ap.add_argument('--apply', action='store_true')
	ap.add_argument('--only', default='', help='comma list of old prefixes to rename (default: all)')
	ap.add_argument('--report', default=str(Path(tempfile.gettempdir()) / 'test_prefix_rename_report.txt'))
	a = ap.parse_args()
	ONLY.update(x for x in a.only.split(',') if x)
	rep, tc, tk = [], 0, 0
	for path in scope():
		try:
			c, k = process(path, a.apply)
		except UnicodeDecodeError:
			continue
		if not (c or k):
			continue
		tc += len(c)
		tk += len(k)
		print(f'{len(c):5d} renamed {len(k):4d} kept  {path}')
		rep.append(f'## {path}')
		rep += [f'  {ln}: {o} -> {n}' for ln, o, n in c]
		rep += [f'  {ln}: KEPT {o}: {ctx}' for ln, o, ctx in k]
	print(f'total: {tc} {"renamed" if a.apply else "to rename"}, {tk} kept as the old prefix in files naming a renamed doc')
	Path(a.report).write_text('\n'.join(rep) + '\n', encoding='utf-8')
	print(f'report: {a.report}')
	return 0


if __name__ == '__main__':
	sys.exit(main())
