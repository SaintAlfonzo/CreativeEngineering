#!/usr/bin/env python3
"""Write a Supplementaries jar soft fluid for every soup and stew no mod already covers.

Soups are the food_types.json "soup" type, as item_dict keys (the golden and gilded stews were removed 2026-11-10). A soup is covered when a
soft fluid in mods/*.jar (data/<ns>/moonlight/soft_fluids/*.json) already lists it as a filled item;
those keep the mod's json and colour. Each remaining soup gets
development/creativeDelight/src/main/resources/data/creativedelight/moonlight/soft_fluids/<name>.json (in the
Creative Delight jar since 2026-11-06, plan_cb_polish.md 4; the soft fluid ids are creativedelight since plan_cd_namespace.md (2026-11-10; before that creativeengineering,
kept then because Moonlight has no alias for a renamed soft fluid), Supplementaries' pumpkin soup as the
template: its empty container is the item's remainder from kubejs/exported/food/foods.json (/dumpfood;
bowl when it has none), one jar space for a bowl or a bottle (items/jars #4, 2026-11-21; a bowl was 2), the same
both ways (a bowl in and a bowl out move the same amount). Items in removed_items.js are left out (Ender's Delight's wooden-bowl stews), so
the shulker bowl is the only container for the Chorus and Endermite Stew fluids.

The color is the average of the item texture's pixels that are not the empty container's own colors,
so the bowl drops out and the soup is left. COLOR_OVERRIDE wins over that. The output folder belongs to
this tool: a json there that no longer matches a soup is deleted, except the hand-written drink fluids in
HAND_WRITTEN.

Soups a mod already covers (Supplementaries' and Moonlight's 11) keep the mod's json except the bowl's capacity: an
override with the pack's bowl value goes to kubejs/data/<ns>/moonlight/soft_fluids/<name>.json (KubeJS data outranks
the jars), listed in COVERED_OVERRIDES_LIST so a later run removes only its own files.

  python3 tools/gen_soup_soft_fluids.py [--dry-run]
"""
import argparse
import glob
import io
import json
import re
import sys
import zipfile
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from food_catalog import INSTANCE, parse_dict  # noqa: E402
import pack_paths  # noqa: E402

REMOVED_JS = pack_paths.REMOVED_ITEMS_JS
TYPES = INSTANCE / 'kubejs/generation_scripts/food/food_types.json'
FOODS = INSTANCE / 'kubejs/exported/food/foods.json'
OUT = INSTANCE / 'development/creativeDelight/src/main/resources/data/creativedelight/moonlight/soft_fluids'
# this tool's own output inside an installed Creative Delight jar: not a mod's coverage
OWN_PREFIX = 'data/creativedelight/moonlight/soft_fluids/'
VANILLA = pack_paths.vanilla_jar()
SOFT_FLUID_RE = re.compile(r'^data/[^/]+/moonlight/soft_fluids/.+\.json$')
# Jar spaces one serving fills (items/jars #4: a bowl of soup or stew is 1 space; mixed_jars.md 9). JAR_UNIT is
# the soft fluid units of one space: 1 while a jar holds 12 (Supplementaries [functional.jar] capacity). items/jars #1
# (capacity 60) rescales JAR_UNIT only (to 5); the spaces stay the rule.
JAR_UNIT = 1
BOWL_SPACES = 1
SPACES = {'minecraft:glass_bottle': 1}   # every other container is a bowl
# covered soups: their bowl capacity is overridden in the KubeJS data tree (see the docstring)
COVERED_OUT = INSTANCE / 'kubejs/data'
COVERED_OVERRIDES_LIST = INSTANCE / 'kubejs/generation_scripts/food/soup_soft_fluid_overrides.txt'


def capacity(empty):
	return SPACES.get(empty, BOWL_SPACES) * JAR_UNIT
# Forge fluids of the same soup: the jar takes them from a bucket or a pipe as this soft fluid
EQUIVALENT = {}
# hand-written soft fluids for bottled drinks in the same folder: never written or deleted by this tool
HAND_WRITTEN = {'nut_milk.json', 'carrot_juice.json', 'dandelion_juice.json', 'turnip_water.json',
				'fermented_garlic_honey.json',
				'bayberry_soup.json'}   # Bayberry Juice (a drink since food/drinks #9; keeps its id and Forge fluid)
# item id -> '#rrggbb', where the patch picks garnish, shadow or a gold bowl (checked against the textures)
COLOR_OVERRIDE = {
	'alexsdelight:acacia_blossom_soup': '#d6cba0',
	'farmersdelight:bone_broth': '#c8b48a',
	'farmersrespite:blazing_chili': '#9a2c12',
	'endersdelight:chorus_stew': '#9a6aa0',
	'endersdelight:endermite_stew': '#4a3a5c',
	'delightful:sinigang': '#a8763e',
	'vintagedelight:oatmeal': '#b29a7c',       # the patch is the bowl's shadow side
	'vintagedelight:pickle_soup': '#e3bf74',   # the broth, not the pickle
}


def jar_index():
	"""Every mods/*.jar (and the jars nested in it under META-INF/jarjar, e.g. Creative Delight's members),
    the *_upstream donor jars, then the vanilla client jar, highest priority first, as open ZipFiles."""
	jars = []
	for p in sorted(glob.glob(str(INSTANCE / 'mods/*.jar'))):
		z = zipfile.ZipFile(p)
		jars.append(z)
		for n in z.namelist():
			if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
				jars.append(zipfile.ZipFile(io.BytesIO(z.read(n))))
	jars += [zipfile.ZipFile(p) for p in sorted(glob.glob(str(INSTANCE / '*_upstream/*.jar')))]
	if VANILLA:
		jars.append(zipfile.ZipFile(VANILLA))
	return jars


def read(jars, path):
	for z in jars:
		try:
			return z.read(path)
		except KeyError:
			continue
	return None


def covered(jars):
	"""Filled items of every soft fluid the mods ship."""
	out = {}
	for z in jars:
		for n in z.namelist():
			if not SOFT_FLUID_RE.match(n) or n.startswith(OWN_PREFIX):
				continue
			try:
				d = json.loads(z.read(n))
			except ValueError:
				continue
			for c in d.get('containers', []):
				for f in c.get('filled', []):
					out[f] = n
	return out


def texture(jars, item):
	"""The item model's layer0 texture as an RGBA image (follows one parent hop for generated items)."""
	ns, name = item.split(':', 1)
	tex = f'{ns}:item/{name}'
	model = read(jars, f'assets/{ns}/models/item/{name}.json')
	if model:
		layers = json.loads(model).get('textures', {})
		tex = layers.get('layer0', tex)
	tns, tpath = tex.split(':', 1) if ':' in tex else ('minecraft', tex)
	png = read(jars, f'assets/{tns}/textures/{tpath}.png')
	if not png:
		return None
	img = Image.open(io.BytesIO(png)).convert('RGBA')
	w = img.width
	return img.crop((0, 0, w, w))   # first frame of an animated strip


def soup_colour(jars, item, empty):
	img = texture(jars, item)
	if img is None:
		return None
	base = texture(jars, empty)
	near = set()
	if base is not None:
		near = {p[:3] for p in base.getdata() if p[3] > 0}

	def is_container(rgb):
		return any(sum(abs(a - b) for a, b in zip(rgb, c)) <= 24 for c in near)

	px = [p[:3] for p in img.getdata() if p[3] > 128]
	soup = [p for p in px if not is_container(p)] or px
	if not soup:
		return None
	# the broth is the largest patch of one color; garnish is many small ones, so take the pixels near the
	# commonest color (counting each pixel's neighbors within 48) and average those
	def close(p, q):
		return sum(abs(a - b) for a, b in zip(p, q)) <= 48
	seed = max(soup, key=lambda p: sum(close(p, q) for q in soup))
	patch = [q for q in soup if close(seed, q)]
	avg = [round(sum(p[i] for p in patch) / len(patch)) for i in range(3)]
	return '#%02x%02x%02x' % tuple(avg)


def write_covered_overrides(jars, paths, dry_run):
	"""A copy of each covering mod json with every bowl container at the pack's capacity (and the food divider to
	match), in the KubeJS data tree. Files this tool wrote before and no longer needs are removed."""
	done = []
	for path in sorted(paths):
		raw = read(jars, path)
		if raw is None:
			continue
		d = json.loads(raw)
		changed = False
		for c in d.get('containers', []):
			if c.get('empty') not in SPACES and c.get('capacity') != capacity(c.get('empty')):
				c['capacity'] = capacity(c.get('empty'))
				changed = True
		food = d.get('food')
		if isinstance(food, dict):
			for c in d.get('containers', []):
				if food.get('item') in c.get('filled', []) and food.get('divider') != c['capacity']:
					food['divider'] = c['capacity']
					changed = True
		if not changed:
			continue
		rel = path[len('data/'):]
		out = COVERED_OUT / rel
		done.append(rel)
		text = json.dumps(d, indent=2) + '\n'
		print(f'  override {rel}')
		if not dry_run and (not out.exists() or out.read_text() != text):
			out.parent.mkdir(parents=True, exist_ok=True)
			out.write_text(text)
	old = COVERED_OVERRIDES_LIST.read_text().split() if COVERED_OVERRIDES_LIST.exists() else []
	for rel in old:
		if rel not in done and (COVERED_OUT / rel).exists():
			print(f'  removed override {rel}')
			if not dry_run:
				(COVERED_OUT / rel).unlink()
	if not dry_run:
		COVERED_OVERRIDES_LIST.write_text(''.join(r + '\n' for r in done))
	return done


def main():
	ap = argparse.ArgumentParser()
	ap.add_argument('--dry-run', action='store_true')
	a = ap.parse_args()

	types = json.loads(TYPES.read_text())['types']
	keys = list(types.get('soup', []))
	_, key_val, _ = parse_dict()
	removed = set(pack_paths.removed_item_keys())
	keys = [k for k in keys if k not in removed]
	remainder = {f['id']: f.get('remainder') for f in json.loads(FOODS.read_text())['foods']}
	jars = jar_index()
	have = covered(jars)

	fluids = {}   # base key -> list of item ids, base first
	for k in keys:
		base = k[:-5] if k.endswith('_wood') and k[:-5] in keys else k
		fluids.setdefault(base, [])
		if k == base:
			fluids[base].insert(0, key_val[k])
		else:
			fluids[base].append(key_val[k])

	OUT.mkdir(parents=True, exist_ok=True)
	written, skipped, problems = set(), [], []
	covered_paths = set()
	for base, items in fluids.items():
		done = [i for i in items if i in have]
		if done:
			skipped.append(f'{base} ({have[done[0]].split("/")[1]})')
			covered_paths.add(have[done[0]])
			if len(done) < len(items):
				problems.append(f'{base}: {set(items) - set(done)} not in the covering fluid')
			continue
		first = items[0]
		ns, name = first.split(':', 1)
		containers = []
		for it in items:
			empty = remainder.get(it) or 'minecraft:bowl'
			containers.append({'filled': [it], 'empty': empty, 'capacity': capacity(empty)})
		colour = COLOR_OVERRIDE.get(first) or soup_colour(jars, first, containers[0]['empty'])
		if not colour:
			problems.append(f'{base}: no texture for {first}, color left gray')
			colour = '#a08060'
		d = {
			'color': colour,
			'containers': containers,
		}
		if first in EQUIVALENT:
			d['equivalent_fluids'] = EQUIVALENT[first]
		d.update({
			'flowing_texture': 'moonlight:soft_fluids/potion_flow',
			'food': {'divider': containers[0]['capacity'], 'item': first},
			'from_mod': ns,
			'still_texture': 'moonlight:soft_fluids/soup_liquid',
			'translation_key': f'item.{ns}.{name}',
		})
		path = OUT / f'{base}.json'
		written.add(path.name)
		text = json.dumps(d, indent=2) + '\n'
		print(f'  {base:36} {colour}  {" + ".join(c["empty"] for c in containers)}')
		if not a.dry_run and (not path.exists() or path.read_text() != text):
			path.write_text(text)

	for p in OUT.glob('*.json'):
		if p.name not in written and p.name not in HAND_WRITTEN:
			print(f'  removed {p.name}')
			if not a.dry_run:
				p.unlink()
	overrides = write_covered_overrides(jars, covered_paths, a.dry_run)
	print(f'{len(written)} soft fluids; already covered by a mod: {", ".join(skipped)}')
	print(f'{len(overrides)} covered soups with the pack bowl capacity ({COVERED_OUT.relative_to(INSTANCE)})')
	for p in problems:
		print('WARNING', p)
	return 1 if problems else 0


if __name__ == '__main__':
	sys.exit(main())
