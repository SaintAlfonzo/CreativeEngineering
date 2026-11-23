#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Harvest and replant audit: every crop block has a hand rule, and the game agrees with it (farming/crops #3).

    python3 tools/crop_harvest_coverage.py [audit.json] [--no-write]

Reads kubejs/exported/crops/crop_harvest_audit.json (from /dumpcropharvest, part of /dumpall) and the hand rule
file tools/crop_harvest_rules.json. Without the harvest audit it falls back to crop_growth_audit.json
(/dumpcropgrowth): then only UNKNOWN is checked, since that dump has no Quark data.

Non-crops (leaves, saplings, decorations, weathering states...) are skipped with crop_growth_coverage.skip_reason;
column plants and vines are not skipped here (they are harvested). Every other block lands in one of:

  ok        a rule matches and the dump agrees
  UNKNOWN   no rule: a new mod's crop. Add a rule (and a crops.md 2.1 row) and rerun
  MISMATCH  the dump disagrees with the rule, e.g. rule replant but Quark has no ripe state for it, a Quark replant
            on a crop whose rule is own_pick / shears / break (Quark's replant wins over the crop's own pick unless
            the block is in #creativedelight:crops/own_pick), or a blacklisted replant crop
  FLAG      not a failure: replant + click (Quark clicks first, crops #2 pineapple), and crops whose class declares
            its own canSurvive / mayPlaceOn outside the parity tag (pack soils may refuse them; test placement)

Writes feature_writeup/crop_harvest_audit.md (table + findings) unless --no-write. Exit 1 on UNKNOWN or MISMATCH.
"""
import fnmatch
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from crop_growth_coverage import skip_reason, simple  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
AUDIT = os.path.join(ROOT, 'kubejs/exported/crops/crop_harvest_audit.json')
GROWTH = os.path.join(ROOT, 'kubejs/exported/crops/crop_growth_audit.json')
RULES = os.path.join(ROOT, 'tools/crop_harvest_rules.json')
OUT_MD = os.path.join(ROOT, 'feature_writeup/crop_harvest_audit.md')
HARVESTS = ('replant', 'own_pick', 'shears', 'break', 'top_break', 'skip')
# crop_growth_coverage skips these for WTHIT, but they are harvested, so they need a rule here
NOT_SKIPPED = ('column plant / vine',)


def load_rules():
	with open(RULES) as f:
		raw = json.load(f)
	rules, problems = [], []
	for key, r in raw.items():
		if key.startswith('_'):
			continue
		if r.get('harvest') not in HARVESTS:
			problems.append(f'rule {key}: harvest {r.get("harvest")!r} not one of {", ".join(HARVESTS)}')
		if not r.get('why'):
			problems.append(f'rule {key}: no why')
		rules.append((key, r))
	return rules, problems


def rule_for(rules, block_id):
	exact = [(k, r) for k, r in rules if k == block_id]
	if exact:
		return exact[0]
	for k, r in rules:
		if fnmatch.fnmatchcase(block_id, k):
			return k, r
	return None, None


def is_skipped(row):
	if not row.get('props') and row.get('source') == 'quark':
		return None
	why = skip_reason(row)
	if why and not why.startswith(NOT_SKIPPED):
		return why
	return None


def check(row, rule, rows_by_id):
	"""(mismatches, flags) for one block under its rule."""
	mism, flags = [], []
	if 'quark' not in row:
		return mism, flags
	harvest = rule['harvest']
	target = rows_by_id.get(rule.get('via', row['id']), row)
	q = set(target.get('quark', []))
	via = f' (via {target["id"]})' if target is not row else ''
	if harvest == 'replant':
		if 'replant' not in q:
			mism.append(f'rule replant, Quark has no ripe state{via}')
		if target.get('ownPick'):
			mism.append(f'rule replant, but in #creativedelight:crops/own_pick{via}')
		if 'blacklisted' in q:
			mism.append(f'rule replant, but in #quark:simple_harvest_blacklisted{via}')
		if 'click' in q:
			flags.append(f'replant + click: Quark right-clicks it first{via}')
	elif harvest == 'own_pick':
		if 'replant' in q and not target.get('ownPick') and 'blacklisted' not in q:
			mism.append(f'rule own_pick, but Quark replants it (add to #creativedelight:crops/own_pick){via}')
	elif harvest != 'skip':
		if 'replant' in q and 'blacklisted' not in q:
			mism.append(f'rule {harvest}, but Quark replants it{via}')
	if row.get('ownSoilCheck') and not row.get('parity') and harvest != 'skip' and not rule.get('soil_ok'):
		flags.append('own soil check (' + ', '.join(simple(m.split('#')[0]) + '#' + m.split('#')[1]
												   for m in row['ownSoilCheck']) + '), not in crops/farmland_crops')
	return mism, flags


def main():
	args = [a for a in sys.argv[1:] if not a.startswith('--')]
	path = args[0] if args else (AUDIT if os.path.exists(AUDIT) else GROWTH)
	if not os.path.exists(path):
		sys.exit('missing dump - run /dumpcropharvest (or /dumpall)')
	with open(path) as f:
		audit = json.load(f)
	harvest_dump = any('quark' in r for r in audit['blocks'])
	rules, problems = load_rules()
	rows_by_id = {r['id']: r for r in audit['blocks']}
	used = set()
	ok, unknown, mismatch, flagged, skipped, table = [], [], [], [], 0, []
	for row in audit['blocks']:
		key, rule = rule_for(rules, row['id'])
		if rule is None and is_skipped(row):
			skipped += 1
			continue
		if rule is None:
			cls = simple(row['classes'][0]) if row.get('classes') else '?'
			unknown.append(f'{row["id"]} ({cls}; Quark {"/".join(row.get("quark", [])) or "-"})')
			table.append((row, None, 'UNKNOWN', []))
			continue
		used.add(key)
		m, fl = check(row, rule, rows_by_id)
		mismatch += [f'{row["id"]}: {x}' for x in m]
		flagged += [f'{row["id"]}: {x}' for x in fl]
		if rule['harvest'] != 'skip':
			table.append((row, rule, 'MISMATCH' if m else ('FLAG' if fl else 'ok'), m + fl))
		ok.append(row['id'])
	dead = [k for k, _ in rules if k not in used]

	src = os.path.relpath(path, ROOT)
	if src.startswith('..'):
		src = os.path.basename(path)  # no machine paths in the report
	print(f'crop_harvest_coverage: {len(audit["blocks"])} blocks in {src}, {skipped} not crops, {len(ok)} by rule, '
		  f'{len(unknown)} UNKNOWN, {len(mismatch)} MISMATCH, {len(flagged)} FLAG'
		  + ('' if harvest_dump else ' (growth dump: no Quark data, only UNKNOWN checked; run /dumpcropharvest)'))
	if harvest_dump and not audit.get('quark', True):
		print(f'WARNING: Quark not read in the dump ({audit.get("quarkError")}); every replant rule will MISMATCH')
	for u in unknown:
		print('UNKNOWN:', u)
	for m in mismatch:
		print('MISMATCH:', m)
	for fl in flagged:
		print('FLAG:', fl)
	for p in problems:
		print('PROBLEM:', p)
	for k in dead:
		print(f'WARN: rule {k} matches no block in the dump')
	if '--no-write' not in sys.argv:
		write_md(table, src, harvest_dump, unknown, mismatch, flagged, dead)
	return 1 if unknown or mismatch or problems else 0


def write_md(table, src, harvest_dump, unknown, mismatch, flagged, dead):
	out = ['# Crop harvest audit', '',
		   'Generated by `tools/crop_harvest_coverage.py` from `%s` and `tools/crop_harvest_rules.json`; do not edit. '
		   'Design: [briefs/farming_crops.md](briefs/farming_crops.md) crops #3; the hand table is '
		   '[crops.md](crops.md) 2.1.' % src, '']
	if not harvest_dump:
		out += ['Built from the growth dump: no Quark data yet, so only rule coverage is checked. '
				'Run `/dumpcropharvest` (or `/dumpall`) in game, then rerun.', '']
	out += ['| Block | Rule | Quark | Own pick | Parity | Season | Result | Notes |', '|---|---|---|---|---|---|---|---|']
	for row, rule, result, notes in sorted(table, key=lambda t: t[0]['id']):
		yn = lambda k: ('yes' if row.get(k) else 'no') if k in row else '-'  # noqa: E731
		out.append('| `%s` | %s | %s | %s | %s | %s | %s | %s |' % (
			row['id'], rule['harvest'] if rule else '-',
			'/'.join(row.get('quark', [])) or ('-' if 'quark' not in row else 'none'),
			yn('ownPick'), yn('parity'), ', '.join(row.get('season', [])) or '-', result,
			'; '.join(notes).replace('|', '/') or (rule.get('why', '') if rule else '')))
	out += ['', '## Findings', '']
	for title, items in (('UNKNOWN (no rule)', unknown), ('MISMATCH', mismatch), ('FLAG', flagged),
						 ('Rules that match no block', dead)):
		out.append(f'- {title}: ' + (', '.join(f'`{x}`' if ' ' not in x else x for x in items) if items else 'none'))
	with open(OUT_MD, 'w', encoding='utf-8') as f:
		f.write('\n'.join(out) + '\n')


if __name__ == '__main__':
	sys.exit(main())
