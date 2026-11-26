#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Headless data check of the Yuushya Modelling backport (plan_yuushya_backport.md A9/A10, bug B1).

Upstream's 1.20.1 jar shipped 1.21 data (singular `recipe/` and `loot_table/` folders, `{"id": ...}` results), which
1.19.2 ignores. This checks the fork's resources load as 1.19.2 data:

- pack.mcmeta pack_format 9
- data/<ns>/ has no 1.21 singular folders; recipes in recipes/, loot in loot_tables/
- every recipe parses, has a known type, result in the 1.19.2 form (crafting {"item": id[, "count"]}; stonecutting
  "result": id + "count"), no "category", 52 recipes
- every loot table parses, entries name an item
- every yuushya: id a recipe or loot table names is registered in the source (ItemRegistry / BlockRegistry)
- every literal lang key in the Java source, every registered item/block name and the enum-built screen keys
  resolve in en_us.json; en_us has no item/block key without a registered id (bug B4)

Usage: python3 tools/yuushya_data_check.py   (exit 1 on any failure)
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PROJ = ROOT / 'development' / 'backportedYuushyaModelling'
RES = PROJ / 'src' / 'main' / 'resources'
SRC = PROJ / 'src' / 'main' / 'java'
EXPECTED_RECIPES = 52
VANILLA_KEYS = {'narrator.select', 'options.generic_value', 'options.percent_value'}

errors = []


def err(msg):
	errors.append(msg)


def load(p):
	try:
		return json.loads(p.read_text(encoding='utf-8'))
	except (OSError, ValueError) as e:
		err(f'{p.relative_to(ROOT)}: does not parse ({e})')
		return None


def registered_ids():
	ids = set()
	for name in ('ItemRegistry.java', 'BlockRegistry.java'):
		for f in SRC.rglob(name):
			ids.update(re.findall(r'register\(\s*"([a-z0-9_]+)"', f.read_text(encoding='utf-8')))
	return ids


def check_pack():
	d = load(RES / 'pack.mcmeta')
	if d and d.get('pack', {}).get('pack_format') != 9:
		err('pack.mcmeta: pack_format is not 9')


def item_ref(x, where, ids):
	if isinstance(x, str) and x.startswith('yuushya:') and x.split(':', 1)[1] not in ids:
		err(f'{where}: {x} is not registered')


def walk_items(node, where, ids):
	if isinstance(node, dict):
		for k, v in node.items():
			if k in ('item', 'name') and isinstance(v, str):
				item_ref(v, where, ids)
			else:
				walk_items(v, where, ids)
	elif isinstance(node, list):
		for v in node:
			walk_items(v, where, ids)


def check_data(ids):
	n = 0
	for ns in sorted((RES / 'data').iterdir()):
		for bad in ('recipe', 'loot_table', 'advancement', 'tags/item', 'tags/block'):
			if (ns / bad).exists():
				err(f'data/{ns.name}/{bad}/: 1.21 folder name (1.19.2 reads the plural form)')
		for f in sorted((ns / 'recipes').glob('**/*.json')):
			where = str(f.relative_to(RES))
			d = load(f)
			if d is None:
				continue
			n += 1
			t = d.get('type')
			if 'category' in d:
				err(f'{where}: "category" (1.19.3+ field)')
			r = d.get('result')
			if t == 'minecraft:stonecutting':
				if not isinstance(r, str) or not isinstance(d.get('count'), int):
					err(f'{where}: stonecutting result must be a string id plus "count"')
				item_ref(r, where, ids)
			elif t in ('minecraft:crafting_shaped', 'minecraft:crafting_shapeless'):
				if not isinstance(r, dict) or 'item' not in r or 'id' in r:
					err(f'{where}: result must be {{"item": id}} (found {r!r})')
				if t == 'minecraft:crafting_shaped':
					keys = set(''.join(d.get('pattern', []))) - {' '}
					if keys != set(d.get('key', {})):
						err(f'{where}: pattern symbols and key differ')
				elif not d.get('ingredients'):
					err(f'{where}: no ingredients')
			else:
				err(f'{where}: unexpected recipe type {t}')
			walk_items({k: v for k, v in d.items() if k != 'result'}, where, ids)
			if isinstance(r, dict):
				walk_items(r, where, ids)
		for f in sorted((ns / 'loot_tables').glob('**/*.json')):
			where = str(f.relative_to(RES))
			d = load(f)
			if d is None:
				continue
			if not d.get('pools'):
				err(f'{where}: no pools')
			for pool in d.get('pools', []):
				for e in pool.get('entries', []):
					if e.get('type') == 'minecraft:item' and not e.get('name'):
						err(f'{where}: item entry without name')
			walk_items(d, where, ids)
	if n != EXPECTED_RECIPES:
		err(f'{n} recipes, expected {EXPECTED_RECIPES}')
	for t in ('showblock', 'itemblock'):
		if not (RES / 'data' / 'yuushya' / 'loot_tables' / 'blocks' / f'{t}.json').is_file():
			err(f'loot_tables/blocks/{t}.json missing')


def check_lang(ids):
	lang = load(RES / 'assets' / 'yuushya_modelling' / 'lang' / 'en_us.json') or {}
	text = '\n'.join(f.read_text(encoding='utf-8') for f in SRC.rglob('*.java'))
	for k in sorted(set(re.findall(r'translatable\(\s*"([A-Za-z0-9_.]+)"', text))):
		if k not in lang and k not in VANILLA_KEYS and not k.endswith('.'):
			err(f'lang key {k} used in code, missing from en_us')
	blocks = {'showblock', 'itemblock'}
	for i in ids:
		k = ('block.yuushya.' if i in blocks else 'item.yuushya.') + i
		if k not in lang:
			err(f'{k} missing from en_us')
	for k in lang:
		m = re.match(r'(item|block)\.yuushya\.([a-z0-9_]+)$', k)
		if m and m.group(2) not in ids:
			err(f'en_us {k}: no registered {m.group(1)} (bug B4 kind)')
	# enum-built keys: Mode("x") -> gui.<screen>.mode.x, BlockShape -> gui.showBlockScreen.shape.<name>
	for screen, f in (('showBlockScreen', 'gui/showblock/ShowBlockScreen.java'),
					  ('itemBlockScreen', 'gui/itemblock/ItemBlockScreen.java')):
		p = SRC / 'com' / 'yuushya' / 'modelling' / f
		body = p.read_text(encoding='utf-8').split('enum Mode', 1)
		if len(body) == 2:
			for name in re.findall(r'^\s*[A-Z_]+\("([a-z_]+)"\)', body[1].split(';', 1)[0], re.M):
				if f'gui.{screen}.mode.{name}' not in lang:
					err(f'gui.{screen}.mode.{name} missing from en_us')
	shape = SRC / 'com' / 'yuushya' / 'modelling' / 'blockentity' / 'BlockShape.java'
	for name in re.findall(r'^\s*([A-Z_]+)\(Shapes', shape.read_text(encoding='utf-8'), re.M):
		if f'gui.showBlockScreen.shape.{name.lower()}' not in lang:
			err(f'gui.showBlockScreen.shape.{name.lower()} missing from en_us')


def main():
	ids = registered_ids()
	if len(ids) < 50:
		err(f'only {len(ids)} registered ids found in the source (registry parse broke?)')
	check_pack()
	check_data(ids)
	check_lang(ids)
	for e in errors:
		print('FAIL', e)
	print(f'yuushya_data_check: {len(ids)} ids, {len(errors)} problem(s)')
	return 1 if errors else 0


if __name__ == '__main__':
	sys.exit(main())
