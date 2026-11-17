#!/usr/bin/env python3
"""Compare two /dumpall outputs (a baseline folder and kubejs/exported/) for a change that must not move content.

plan_rules_fixes.md P3 (sections 6, 7, 12b, 12c): recipe ids that change namespace, tags moved between script packs,
villager trades moved onto global.trades.villager. Prints a summary and the differences, exits 1 when a check fails.

	python3 tools/dump_diff.py recipes BASE NEW [--move OLD_NS=NEW_NS ...]
	    BASE, NEW: folders holding recipes/all.csv. Pass: the same recipe count, and every id only in one side is
	    the same path under a namespace pair given with --move (creativeengineering=creativecompat). Prints the
	    moved count per pair and every unexplained id.
	python3 tools/dump_diff.py tags BASE NEW [--rename OLD_TAG_PREFIX=NEW_TAG_PREFIX ...]
	    BASE, NEW: folders holding tags/<kind>_tags.json. Pass: every tag has the same members, after the renames
	    (creativeworldgen:loot/=creativeloot:loot/).
	python3 tools/dump_diff.py trades BASE NEW PROFESSION ...
	    BASE, NEW: folders holding villager_trades.json. Pass: the listed professions' offers are equal.
	python3 tools/dump_diff.py food BASE NEW
	    BASE, NEW: folders holding food/foods.json (/dumpfood). Pass: the same foods with the same values and effects.
"""
import csv
import json
import sys
from collections import Counter
from pathlib import Path


def pairs(args, flag):
	out, rest, i = [], [], 0
	while i < len(args):
		if args[i] == flag and i + 1 < len(args):
			a, b = args[i + 1].split('=', 1)
			out.append((a, b))
			i += 2
		else:
			rest.append(args[i])
			i += 1
	return out, rest


def recipe_ids(folder):
	with open(Path(folder) / 'recipes' / 'all.csv', newline='', encoding='utf-8') as fh:
		return [row['id'] for row in csv.DictReader(fh)]


def recipes(base, new, moves):
	a, b = recipe_ids(base), recipe_ids(new)
	print(f'recipes: base {len(a)}, new {len(b)}')
	only_a, only_b = set(a) - set(b), set(b) - set(a)
	moved = Counter()
	for old_ns, new_ns in moves:
		for rid in sorted(only_a):
			if rid.startswith(old_ns + ':'):
				target = new_ns + ':' + rid.split(':', 1)[1]
				if target in only_b:
					only_a.discard(rid)
					only_b.discard(target)
					moved[(old_ns, new_ns)] += 1
	for (o, n), count in sorted(moved.items()):
		print(f'moved {o} -> {n}: {count}')
	for rid in sorted(only_a):
		print(f'ONLY_BASE {rid}')
	for rid in sorted(only_b):
		print(f'ONLY_NEW {rid}')
	ok = len(a) == len(b) and not only_a and not only_b
	print('RECIPES_SAME' if ok else 'RECIPES_DIFFER')
	return ok


def tags(base, new, renames):
	ok = True
	for path in sorted((Path(base) / 'tags').glob('*_tags.json')):
		other = Path(new) / 'tags' / path.name
		if not other.exists():
			print(f'MISSING {other}')
			ok = False
			continue
		a = json.loads(path.read_text(encoding='utf-8'))
		b = json.loads(other.read_text(encoding='utf-8'))

		def rename(tag):
			for old, nw in renames:
				if tag.startswith(old):
					return nw + tag[len(old):]
			return tag
		a = {rename(t): sorted(rename(m) for m in v) for t, v in a.items()}
		b = {t: sorted(rename(m) for m in v) for t, v in b.items()}
		diff = [t for t in sorted(set(a) | set(b)) if a.get(t) != b.get(t)]
		print(f'{path.name}: {len(a)} / {len(b)} tags, {len(diff)} differ')
		for t in diff:
			gone = sorted(set(a.get(t, [])) - set(b.get(t, [])))
			added = sorted(set(b.get(t, [])) - set(a.get(t, [])))
			print(f'  {t}: -{gone[:8]} +{added[:8]}')
		ok = ok and not diff
	print('TAGS_SAME' if ok else 'TAGS_DIFFER')
	return ok


def trades(base, new, professions):
	a = json.loads((Path(base) / 'villager_trades.json').read_text(encoding='utf-8'))['professions']
	b = json.loads((Path(new) / 'villager_trades.json').read_text(encoding='utf-8'))['professions']
	ok = True
	for prof in professions:
		same = a.get(prof) == b.get(prof)
		print(f'{prof}: {"same" if same else "DIFFERS"}')
		ok = ok and same
	print('TRADES_SAME' if ok else 'TRADES_DIFFER')
	return ok


def food(base, new):
	def load(folder):
		data = json.loads((Path(folder) / 'food' / 'foods.json').read_text(encoding='utf-8'))
		return {f['id']: f for f in data['foods']}
	a, b = load(base), load(new)
	only_a, only_b = sorted(set(a) - set(b)), sorted(set(b) - set(a))
	changed = sorted(k for k in set(a) & set(b) if a[k] != b[k])
	for k in only_a:
		print(f'ONLY_BASE {k}')
	for k in only_b:
		print(f'ONLY_NEW {k}')
	for k in changed:
		diff = sorted(f for f in set(a[k]) | set(b[k]) if a[k].get(f) != b[k].get(f))
		print(f'CHANGED {k}: {", ".join(diff)}')
	ok = not (only_a or only_b or changed)
	print(f'foods: base {len(a)}, new {len(b)}')
	print('FOOD_SAME' if ok else 'FOOD_DIFFERS')
	return ok


def main(argv):
	if len(argv) < 3 or argv[0] not in ('recipes', 'tags', 'trades', 'food'):
		print(__doc__)
		return 2
	kind, rest = argv[0], argv[1:]
	if kind == 'recipes':
		moves, rest = pairs(rest, '--move')
		return 0 if recipes(rest[0], rest[1], moves) else 1
	if kind == 'tags':
		renames, rest = pairs(rest, '--rename')
		return 0 if tags(rest[0], rest[1], renames) else 1
	if kind == 'food':
		return 0 if food(rest[0], rest[1]) else 1
	return 0 if trades(rest[0], rest[1], rest[2:]) else 1


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
