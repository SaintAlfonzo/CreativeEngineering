#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Items with no creative tab: which of them a player can get (jei/jei-coverage #29).

An item with no creative tab is never in creative mode, its search or JEI. Creative Common gives the items on its
[creative_menu] orphanTabItems list a tab (util/OrphanTabs, mixin/ItemOrphanTabMixin); every other orphan stays
hidden. This report lists the orphans with their sources, so a hidden one that players can craft or loot stands out.

Inputs (all optional except the source of the allow list):
  kubejs/exported/items/no_tab.json            /dumpnotab (Creative Devtools): every orphan, listed or not
  kubejs/exported/recipes/crafting_audit.json  /dumpcraftingaudit: crafting results
  kubejs/exported/recipes/all.csv              /dumprecipes: every recipe id (an id naming the item is a hint only)
  kubejs/exported/loot_tables/*.json           /dumploot: every loot table, item entries read recursively
  config/creativecommon-common.toml            [creative_menu] orphanTabItems (else the defaults in OrphanTabs.java)

Without the no_tab dump the report covers only the allow list and KEEP_HIDDEN below, and says so.

    python3 tools/no_tab_report.py           rewrite the generated block of feature_writeup/jei_coverage.md
    python3 tools/no_tab_report.py --check   exit 1 when a hidden orphan has a crafting or loot source and no
                                             KEEP_HIDDEN entry
"""
import argparse
import csv
import json
import re
import sys
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
EXPORTED = INSTANCE / 'kubejs/exported'
NO_TAB = EXPORTED / 'items/no_tab.json'
AUDIT = EXPORTED / 'recipes/crafting_audit.json'
ALL_CSV = EXPORTED / 'recipes/all.csv'
LOOT_DIR = EXPORTED / 'loot_tables'
CONFIG = INSTANCE / 'config/creativecommon-common.toml'
JAVA = INSTANCE / 'development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/util/OrphanTabs.java'
DOC = INSTANCE / 'feature_writeup/jei_coverage.md'
BEGIN = '<!-- BEGIN GENERATED: no_tab -->'
END = '<!-- END GENERATED: no_tab -->'

# Orphans the pack keeps hidden on purpose, with the reason (owner ruling R12 in plan_owner_rows_1120.md).
KEEP_HIDDEN = {
	'abnormals_delight:escargot': 'stays hidden (R12): salting recipe only, no clear source',
	'abnormals_delight:maple_cookie': 'left to its merge into the Syrup Cookie (food/types-and-tuning #10)',
}


def allow_list():
	"""(ids, where they came from)."""
	if CONFIG.exists():
		text = CONFIG.read_text(encoding='utf-8')
		m = re.search(r'^\s*orphanTabItems\s*=\s*\[(.*?)\]', text, re.M | re.S)
		if m:
			return re.findall(r'"([^"]+)"', m.group(1)), CONFIG.relative_to(INSTANCE).as_posix()
	m = re.search(r'DEFAULTS\s*=\s*List\.of\((.*?)\);', JAVA.read_text(encoding='utf-8'), re.S)
	return (re.findall(r'"([^"]+)"', m.group(1)) if m else []), 'OrphanTabs.DEFAULTS (config not written yet)'


def crafting_sources():
	out = {}
	if not AUDIT.exists():
		return out, False
	for r in json.loads(AUDIT.read_text(encoding='utf-8')).get('recipes', []):
		res = r.get('result')
		if res:
			out.setdefault(res, []).append(r['id'])
	return out, True


def loot_sources():
	out = {}
	if not LOOT_DIR.is_dir():
		return out, False

	def walk(node, table):
		if isinstance(node, dict):
			if node.get('type') == 'minecraft:item' and isinstance(node.get('name'), str):
				out.setdefault(node['name'], set()).add(table)
			for v in node.values():
				walk(v, table)
		elif isinstance(node, list):
			for v in node:
				walk(v, table)

	for f in sorted(LOOT_DIR.glob('*.json')):
		try:
			data = json.loads(f.read_text(encoding='utf-8'))
		except (OSError, ValueError):
			continue
		for table, body in data.items():
			walk(body, f.stem + ':' + table)
	return out, True


def recipe_id_hints():
	ids = []
	if ALL_CSV.exists():
		with ALL_CSV.open(encoding='utf-8', newline='') as fh:
			ids = [row['id'] for row in csv.DictReader(fh)]
	return ids


def hints_for(item, ids):
	ns, path = item.split(':', 1)
	pat = re.compile(r'(^|[/_:])' + re.escape(ns) + r'_' + re.escape(path) + r'($|[/_])|(^|/)' + re.escape(path) + r'$')
	return [i for i in ids if pat.search(i)]


def build():
	allowed, allowed_from = allow_list()
	crafting, have_audit = crafting_sources()
	loot, have_loot = loot_sources()
	ids = recipe_id_hints()
	dump = None
	if NO_TAB.exists():
		dump = json.loads(NO_TAB.read_text(encoding='utf-8'))

	rows = {}
	if dump:
		for r in dump.get('items', []):
			rows[r['id']] = {'listed': r.get('listed'), 'tab': r.get('tab'), 'from_dump': True}
	for i in allowed:
		rows.setdefault(i, {'listed': True, 'tab': None, 'from_dump': False})
	for i in KEEP_HIDDEN:
		rows.setdefault(i, {'listed': i in allowed, 'tab': None, 'from_dump': False})

	report = []
	for item in sorted(rows):
		r = rows[item]
		craft = crafting.get(item, [])
		lt = sorted(loot.get(item, []))
		hint = hints_for(item, ids) if not craft else []
		if r['listed']:
			state = 'shown' + (' (' + r['tab'] + ' tab)' if r.get('tab') else '')
		elif item in KEEP_HIDDEN:
			state = 'hidden: ' + KEEP_HIDDEN[item]
		else:
			state = 'hidden'
		gettable = bool(craft or lt)
		report.append((item, state, craft, lt, hint, gettable, r))
	return {
		'allowed': allowed, 'allowed_from': allowed_from, 'have_audit': have_audit, 'have_loot': have_loot,
		'have_ids': bool(ids), 'dump': dump, 'rows': report,
	}


def short(lst, n=3):
	if not lst:
		return ''
	s = ', '.join('`' + x + '`' for x in lst[:n])
	return s + (' +' + str(len(lst) - n) if len(lst) > n else '')


def render(b):
	lines = [BEGIN, '']
	if b['dump']:
		d = b['dump']
		lines.append(f"From `/dumpnotab`: {d.get('hidden', 0)} hidden orphans, {d.get('listed', 0)} listed, of "
					 f"{d.get('total', 0)} items. Only hidden orphans with a crafting or loot source, the allow list and "
					 "the keep-hidden list are in the table.")
	else:
		lines.append('`kubejs/exported/items/no_tab.json` is missing (run `/dumpnotab` in game, then rerun this): the '
					 'table covers only the allow list and the keep-hidden list, so other orphans are not checked yet.')
	lines.append('')
	lines.append(f"Allow list from {b['allowed_from']}: {len(b['allowed'])} items. Sources: crafting results "
				 f"({'crafting_audit.json' if b['have_audit'] else 'missing'}), loot entries "
				 f"({'loot_tables/' if b['have_loot'] else 'missing'}), and recipe ids naming the item when no crafting "
				 f"result matches ({'all.csv' if b['have_ids'] else 'missing'}; a hint, the recipe type has no result "
				 "column).")
	lines.append('')
	lines.append('| Item | State | Crafting | Loot | Other recipe ids |')
	lines.append('|---|---|---|---|---|')
	shown = 0
	for item, state, craft, lt, hint, gettable, r in b['rows']:
		if r['from_dump'] and not r['listed'] and item not in KEEP_HIDDEN and not gettable:
			continue
		shown += 1
		lines.append(f"| `{item}` | {state} | {short(craft)} | {short(lt)} | {short(hint)} |")
	if shown == 0:
		lines.append('| (none) | | | | |')
	lines.append('')
	lines.append(END)
	return '\n'.join(lines)


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	ap.add_argument('--check', action='store_true')
	args = ap.parse_args()
	b = build()
	missing = [item for item, state, craft, lt, hint, gettable, r in b['rows']
			   if not r['listed'] and item not in KEEP_HIDDEN and gettable]
	if args.check:
		for m in missing:
			print(f'[no_tab_report] hidden but gettable: {m}')
		return 1 if missing else 0
	block = render(b)
	doc = DOC.read_text(encoding='utf-8')
	if BEGIN in doc and END in doc:
		pre = doc[:doc.index(BEGIN)]
		post = doc[doc.index(END) + len(END):]
		doc = pre + block + post
	else:
		print(f'[no_tab_report] {DOC.name} has no {BEGIN} block; add the markers where the table goes', file=sys.stderr)
		return 2
	DOC.write_text(doc, encoding='utf-8')
	print(f"[no_tab_report] {len(b['rows'])} rows, {len(missing)} hidden but gettable, dump "
		  f"{'present' if b['dump'] else 'missing'}")
	return 1 if missing else 0


if __name__ == '__main__':
	sys.exit(main())
