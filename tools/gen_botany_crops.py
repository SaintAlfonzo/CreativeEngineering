#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Botany Pots crops the pack adds, and the Nether / End dimension tags (botany_pots.md 3; farming/crops #36, R11).

    python3 tools/gen_botany_crops.py            write the crop recipes and the two tags
    python3 tools/gen_botany_crops.py --check    write nothing; exit 1 when an output is stale or a row is invalid
    python3 tools/gen_botany_crops.py --report   also list plantables that still have no pot crop

Source: kubejs/generation_scripts/recipes/botany_pot_crops.csv (format in its header). Writes
  kubejs/data/creativecompat/recipes/botanypots/crop/<name>.json        one botanypots:crop per kind=new row
  kubejs/data/creativecompat/tags/items/botany_pots/nether_only.json    seeds that grow only in the Nether
  kubejs/data/creativecompat/tags/items/botany_pots/end_only.json       seeds that grow only in the End
The pack folder's kubejs/data is a datapack, so /reload applies the recipes; the tags are read by Creative Compat's
BotanyPotDimensionMixin. botany_pots.js then scales growthTicks of every pot crop, these included.

Reads: item_dict.js and block_dict.js (keys -> ids), kubejs/exported/recipes/all.csv (the loaded pot crops and soils),
kubejs/exported/crops/crop_growth_audit.json (the block each seed places, its age property), item_tags.json (tag
members, #forge:seeds for --report), and the pot recipe JSON of every loaded crop and soil from the pack folder, the
in-tree mods and the jars in mods/ (nested bundle jars included). Run /dumpall first when mods changed.
"""
import argparse
import csv
import io
import json
import re
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pack_paths  # noqa: E402
import table_io  # noqa: E402

INSTANCE = pack_paths.INSTANCE
EXP = INSTANCE / 'kubejs/exported'
SRC = INSTANCE / 'kubejs/generation_scripts/recipes/botany_pot_crops.csv'
OUT_RECIPES = INSTANCE / 'kubejs/data/creativecompat/recipes/botanypots/crop'
OUT_TAGS = INSTANCE / 'kubejs/data/creativecompat/tags/items/botany_pots'
DIMENSIONS = ('nether', 'end')
# Soils made by scripts, not JSON files (botany_pots.js: Fertile Soil); their categories.
SCRIPT_SOILS = {'creativecompat:botany_soil/fertile_soil': ['dirt', 'farmland']}
# --report: crop-growth blocks that are not pot plantables (trees, leaves, moss and sand spread, decor with an age).
NOT_PLANTABLE = re.compile(r'Leaves|LeafPile|Sapling|Propagule|Mossable|Mossy|Sandy|Trellis|Grass|Lantern|Cheese|'
                           r'Basket|Pot|Jar|Residue|Spray|Ivy|Weeds|Lattice|Stem|Wall|Slab|Stairs|'
                           r'VineBush|DoubleFruitBush|FruitBush|Chorus(Flower)?Block$')

_BLOCK_ASSIGN = re.compile(r"""^[ \t]*b\.(\w+)[ \t]*=[ \t]*(?:'([^'\n]*)'|"([^"\n]*)")""", re.M)


def block_dict():
	text = (pack_paths.STARTUP_LIB / 'block_dict.js').read_text(encoding='utf-8')
	return {m.group(1): m.group(2) if m.group(2) is not None else m.group(3) for m in _BLOCK_ASSIGN.finditer(text)}


class Recipes:
	"""Pot crop and soil recipe JSON by recipe id, found in the pack folder, the in-tree mods and the jars."""

	def __init__(self):
		self.files = {}
		roots = [INSTANCE / 'kubejs/data']
		roots += sorted(INSTANCE.glob('development/*/src/main/resources/data'))
		roots += sorted(INSTANCE.glob('development/*/members/*/src/main/resources/data'))
		self.roots = roots
		self.jars = None

	def _jar_index(self):
		"""Path inside a jar -> a zero-argument opener of that jar. Members nested in the bundle jars (Creative Delight,
        Creative Brewing) are read in memory, so the check never rewrites tools/.nested_jars."""
		if self.jars is None:
			self.jars = {}
			for jar in sorted(pack_paths.MODS.glob('*.jar')):
				try:
					with zipfile.ZipFile(jar) as z:
						names = z.namelist()
						self._index(names, lambda jar=jar: zipfile.ZipFile(jar))
						for n in names:
							if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
								data = z.read(n)
								with zipfile.ZipFile(io.BytesIO(data)) as inner:
									self._index(inner.namelist(), lambda data=data: zipfile.ZipFile(io.BytesIO(data)))
				except (zipfile.BadZipFile, OSError):
					pass
		return self.jars

	def _index(self, names, opener):
		for n in names:
			if n.endswith('.json') and (('/blockstates/' in n and n.startswith('assets/')) or
			                            (n.startswith('data/') and '/recipes/' in n and 'botanypots' in n)):
				self.jars.setdefault(n, opener)

	def _read(self, rel):
		opener = self._jar_index().get(rel)
		if opener is None:
			return None
		with opener() as z:
			return z.read(rel).decode('utf-8')

	def blockstate_props(self, bid):
		"""Property names of a block from its blockstate file (None = no file found; empty set = multipart or none)."""
		ns, path = bid.split(':', 1)
		rel = f'assets/{ns}/blockstates/{path}.json'
		raw = None
		for f in [INSTANCE / 'kubejs' / rel] + [r.parent / rel for r in self.roots[1:]]:
			if f.is_file():
				raw = f.read_text(encoding='utf-8')
				break
		if raw is None:
			raw = self._read(rel)
		if raw is None:
			# vanilla blockstates are not in mods/; Botany Pots' own recipes cover them
			return set() if ns == 'minecraft' else None
		props = set()
		for key in (pack_paths.loads_lenient(raw).get('variants') or {}):
			props |= {kv.split('=', 1)[0] for kv in key.split(',') if '=' in kv}
		return props

	def get(self, rid):
		ns, path = rid.split(':', 1)
		rel = f'{ns}/recipes/{path}.json'
		for root in self.roots:
			f = root / rel
			if f.is_file():
				return unwrap(pack_paths.loads_lenient(f.read_text(encoding='utf-8')))
		raw = self._read('data/' + rel)
		return unwrap(pack_paths.loads_lenient(raw)) if raw is not None else None


def unwrap(j):
	if isinstance(j, dict) and j.get('type') == 'forge:conditional':
		return j['recipes'][0]['recipe']
	return j


def ingredient_ids(x, tags):
	out = []
	if isinstance(x, list):
		for y in x:
			out += ingredient_ids(y, tags)
	elif isinstance(x, dict) and 'item' in x:
		out.append(x['item'])
	elif isinstance(x, dict) and 'tag' in x:
		out += tags.get(x['tag'], [])
	return out


def loaded_ids(kind):
	path = EXP / 'recipes/all.csv'
	with open(path, encoding='utf-8') as f:
		return [r['id'] for r in csv.DictReader(f) if r['type'] == 'botanypots:' + kind]


def parse_display(spec, item_of_key, blocks, audit_by_block, errors, name):
	phases = []
	for part in spec.split('>'):
		m = re.fullmatch(r'\s*([\w:./-]+)\s*(?:\[([^\]]*)\])?\s*', part)
		if not m:
			errors.append(f'{name}: display part "{part}" not understood')
			continue
		bid = blocks.get(m.group(1), m.group(1))
		if ':' not in bid:
			errors.append(f'{name}: display block "{m.group(1)}" is neither a block_dict key nor an id')
			continue
		props = {}
		for kv in filter(None, (m.group(2) or '').split(',')):
			k, _, v = kv.partition('=')
			v = v.strip()
			props[k.strip()] = int(v) if v.isdigit() else (v == 'true' if v in ('true', 'false') else v)
		phase = {'block': bid}
		if props:
			phase['properties'] = props
		phases.append(phase)
	if len(phases) == 1 and 'properties' not in phases[0]:
		b = audit_by_block.get(phases[0]['block'])
		if b and any(p['name'] == 'age' for p in b.get('props', [])):
			return {'type': 'botanypots:aging', 'block': phases[0]['block']}
		return {'block': phases[0]['block']}
	return {'type': 'botanypots:transitional', 'phases': phases}


def parse_drops(spec, item_of_key, errors, name):
	drops = []
	for part in filter(None, spec.split('|')):
		m = re.fullmatch(r'\s*(\w+)@([0-9.]+)(?:x(\d+)-(\d+))?\s*', part)
		if not m:
			errors.append(f'{name}: drop "{part}" not understood (key@chance or key@chance x min-max)')
			continue
		iid = item_of_key(m.group(1), f'{name} drop')
		d = {'chance': float(m.group(2)), 'output': {'item': iid}}
		if m.group(3):
			d['minRolls'], d['maxRolls'] = int(m.group(3)), int(m.group(4))
		drops.append(d)
	return drops


def namespaces(recipe):
	ns = set()

	def walk(o):
		if isinstance(o, dict):
			for k in ('item', 'block'):
				if isinstance(o.get(k), str) and ':' in o[k]:
					ns.add(o[k].split(':', 1)[0])
			for v in o.values():
				walk(v)
		elif isinstance(o, list):
			for v in o:
				walk(v)
	walk(recipe)
	ns.discard('minecraft')
	return sorted(ns)


def reference_schema(recipes, loaded_crops):
	"""Keys and display types the loaded pot crops use (Botany Pots' own recipes and the mods'): what a generated crop
    may use. Read from the recipes, so a Botany Pots update that changes the format shows up as errors here."""
	top, drop, disp = {'conditions'}, set(), set()
	for rid in loaded_crops:
		j = recipes.get(rid)
		if not j:
			continue
		top |= set(j)
		for d in j.get('drops', []):
			drop |= set(d)
		ds = j.get('display', {})
		for one in (ds if isinstance(ds, list) else [ds]):
			disp.add(one.get('type', 'botanypots:simple'))
	return {'top': top, 'drop': drop, 'display': disp}


def schema_errors(name, recipe, schema, recipes):
	errs = []
	for k in set(recipe) - schema['top']:
		errs.append(f'{name}: key "{k}" is used by no loaded pot crop')
	for d in recipe['drops']:
		for k in set(d) - schema['drop']:
			errs.append(f'{name}: drop key "{k}" is used by no loaded pot crop')
		if not 0 < d['chance'] <= 1 or d.get('minRolls', 1) > d.get('maxRolls', 1):
			errs.append(f'{name}: drop {d} has a chance outside (0, 1] or minRolls > maxRolls')
	disp = recipe['display']
	if disp.get('type', 'botanypots:simple') not in schema['display']:
		errs.append(f'{name}: display type {disp.get("type")} is used by no loaded pot crop')
	for phase in disp.get('phases', [disp]):
		props = recipes.blockstate_props(phase['block'])
		if props is None:
			# donor-jar ports (Miner's Delight, Vinery) load their assets at runtime from an upstream folder
			print(f'note: {name}: display block {phase["block"]}: no blockstate file in mods/ or the pack, property names not checked')
			continue
		for p in phase.get('properties', {}):
			if props and p not in props:
				errs.append(f'{name}: display block {phase["block"]} has no property "{p}" (has {sorted(props)})')
	return errs


def build(args):
	errors, notes = [], []
	_, keyval, _ = pack_paths.parse_item_dict()
	blocks = block_dict()
	tags = json.loads((EXP / 'tags/item_tags.json').read_text(encoding='utf-8'))
	audit = json.loads((EXP / 'crops/crop_growth_audit.json').read_text(encoding='utf-8'))['blocks']
	audit_by_block = {b['id']: b for b in audit}
	block_of_item = {}
	for b in audit:
		if b.get('item'):
			block_of_item.setdefault(b['item'], b['id'])
	recipes = Recipes()

	def item_of_key(key, ctx):
		v = keyval.get(key)
		if v is None:
			errors.append(f'{ctx}: "{key}" is not an item_dict key')
			return key
		return v

	soil_cats = {}
	for rid in loaded_ids('soil'):
		j = recipes.get(rid)
		if j is None and rid in SCRIPT_SOILS:
			j = {'categories': SCRIPT_SOILS[rid]}
		if j is None:
			notes.append(f'soil {rid}: no JSON found (made by a script?); its categories are not known')
			continue
		for c in j.get('categories', []):
			soil_cats.setdefault(c, []).append(rid)
	loaded_crops = loaded_ids('crop')
	covered = {}
	for rid in loaded_crops:
		j = recipes.get(rid)
		if j and 'seed' in j:
			for s in ingredient_ids(j['seed'], tags):
				covered.setdefault(s, rid)

	schema = reference_schema(recipes, loaded_crops)
	rows = table_io.read_dicts(SRC)
	out = {}
	dim_tags = {d: [] for d in DIMENSIONS}
	names = set()
	for r in rows:
		name, kind = r['name'], r['kind']
		if name in names:
			errors.append(f'{name}: duplicate name')
		names.add(name)
		seed = item_of_key(r['seed'], f'{name} seed')
		dim = r['dimension'].strip()
		if dim and dim not in DIMENSIONS:
			errors.append(f'{name}: dimension "{dim}" is not one of {DIMENSIONS}')
		elif dim:
			dim_tags[dim].append(seed)
		base = {}
		if r['like']:
			if r['like'] not in loaded_crops:
				errors.append(f'{name}: like {r["like"]} is not a loaded pot crop (recipes/all.csv)')
			base = recipes.get(r['like']) or {}
			if not base:
				errors.append(f'{name}: like {r["like"]}: recipe JSON not found')
		if kind == 'gate':
			if seed not in covered:
				errors.append(f'{name}: gate row, but no loaded pot crop has the seed {seed}')
			continue
		if kind != 'new':
			errors.append(f'{name}: kind "{kind}" is not new or gate')
			continue
		if seed in covered:
			errors.append(f'{name}: {seed} already has the pot crop {covered[seed]}')
		cats = r['categories'].split('|') if r['categories'] else list(base.get('categories', []))
		for c in cats:
			if c not in soil_cats:
				errors.append(f'{name}: soil category "{c}" has no loaded pot soil')
		drops = parse_drops(r['drops'], item_of_key, errors, name) if r['drops'] else base.get('drops', [])
		disp = r['display'].strip()
		if disp.startswith('mix:'):
			members = [m for m in ingredient_ids({'tag': item_of_key(disp[4:], name).lstrip('#')}, tags)]
			if not members:
				errors.append(f'{name}: {disp} has no members in item_tags.json')
			mix_drops, display = [], None
			for m in members:
				mj = recipes.get(covered[m]) if m in covered else None
				if not mj:
					errors.append(f'{name}: mix member {m} has no pot crop')
					continue
				mix_drops.append({'chance': round(1.0 / len(members), 3), 'output': mj['drops'][0]['output']})
				display = display or mj['display']
			if not r['drops']:
				drops = mix_drops
		elif disp:
			display = parse_display(disp, item_of_key, blocks, audit_by_block, errors, name)
		elif base.get('display'):
			display = base['display']
		else:
			bid = block_of_item.get(seed)
			if not bid:
				errors.append(f'{name}: no block for {seed} in crop_growth_audit.json; fill display')
				display = {'block': 'minecraft:air'}
			else:
				display = parse_display(bid, item_of_key, blocks, audit_by_block, errors, name)
		ticks = int(r['growth_ticks']) if r['growth_ticks'] else int(base.get('growthTicks', 1200))
		if not drops:
			errors.append(f'{name}: no drops')
		recipe = {'type': 'botanypots:crop', 'seed': {'item': seed}, 'categories': cats, 'growthTicks': ticks,
		          'display': display, 'drops': drops}
		if 'lightLevel' in base:
			recipe['lightLevel'] = base['lightLevel']
		recipe = {'conditions': [{'type': 'forge:mod_loaded', 'modid': 'botanypots'}] +
		                        [{'type': 'forge:mod_loaded', 'modid': ns} for ns in namespaces(recipe)], **recipe}
		errors += schema_errors(name, recipe, schema, recipes)
		out[OUT_RECIPES / f'{name}.json'] = recipe
	for d in DIMENSIONS:
		out[OUT_TAGS / f'{d}_only.json'] = {'replace': False, 'values': [{'id': s, 'required': False} for s in sorted(set(dim_tags[d]))]}

	report = []
	if args.report:
		new_seeds = {r['seed'] and keyval.get(r['seed']) for r in rows}
		cands = set(tags.get('forge:seeds', []))
		for b in audit:
			if b.get('item') and b.get('cropLike') and not NOT_PLANTABLE.search(b['classes'][0].rsplit('.', 1)[-1]):
				cands.add(b['item'])
		report = sorted(c for c in cands if c not in covered and c not in new_seeds)
	return out, errors, notes, report


def text_of(obj):
	return json.dumps(obj, indent=2) + '\n'


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	ap.add_argument('--check', action='store_true', help='write nothing; exit 1 when stale or invalid')
	ap.add_argument('--report', action='store_true', help='list plantables that still have no pot crop')
	args = ap.parse_args()
	out, errors, notes, report = build(args)
	for n in notes:
		print('note:', n)
	for e in errors:
		print('ERROR:', e)
	stale = [p for p, o in out.items() if not p.is_file() or p.read_text(encoding='utf-8') != text_of(o)]
	extra = sorted(p for p in OUT_RECIPES.glob('*.json') if p not in out) if OUT_RECIPES.is_dir() else []
	if args.report:
		print(f'{len(report)} plantables without a pot crop (forge:seeds and crop-growth blocks, trees and decor left out):')
		for c in report:
			print('  ' + c)
	if args.check:
		for p in stale:
			print('stale:', p.relative_to(INSTANCE))
		for p in extra:
			print('not generated (remove):', p.relative_to(INSTANCE))
		n_new = sum(1 for p in out if p.parent == OUT_RECIPES)
		print(f'gen_botany_crops: {n_new} crops, {len(stale)} stale, {len(extra)} extra, {len(errors)} errors')
		return 1 if (stale or extra or errors) else 0
	if errors:
		print('gen_botany_crops: not written, fix the errors first')
		return 1
	for p, o in out.items():
		if p in stale:
			p.parent.mkdir(parents=True, exist_ok=True)
			p.write_text(text_of(o), encoding='utf-8')
	for p in extra:
		p.unlink()
	print(f'gen_botany_crops: {sum(1 for p in out if p.parent == OUT_RECIPES)} crops; wrote {len(stale)}, removed {len(extra)}')
	return 0


if __name__ == '__main__':
	sys.exit(main())
