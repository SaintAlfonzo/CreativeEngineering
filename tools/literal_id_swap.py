#!/usr/bin/env python3
"""Swap quoted 'namespace:id' literals in scripts for their dict keys (plan_rules_fixes.md section 7, DR-C2).

Dry run by default: prints what would change. Reads Common's dicts (item, block, fluid, effect), finds quoted
literals in the KubeJS scripts whose whole text is an id or tag with a dict key, and rewrites each one to the key,
through the name the file already binds for that dict (`var i = global.item` -> `i.key`; no binding ->
`global.item.key`). Skipped: ids with no key, strings that are not exactly an id (tooltip text, lang, '2x ns:id'),
comments, object keys ('ns:id': ...), recipe ids (`.id('...')`, `id: '...'`), registrations (`.create(...)`, which
section 6 moves to global.reg), LootJS names, Common's dicts and helpers, and tools/ Python.

	python3 tools/literal_id_swap.py                 # dry run: per-file counts
	python3 tools/literal_id_swap.py -v              # dry run: every change
	python3 tools/literal_id_swap.py --apply         # rewrite the files
	python3 tools/literal_id_swap.py --missing       # ids with no key used 2 or more times
	python3 tools/literal_id_swap.py --add-keys      # add those keys to item_dict.js / effect_dict.js (dry run
	                                                 # without --apply); a Common rebuild follows
	python3 tools/literal_id_swap.py --aliases       # recipe helper aliases: `var R = global.NCH` outside the pack
	                                                 # folder -> `global.NCH.forPack('<folder namespace>')` (section 6)
	python3 tools/literal_id_swap.py PATH ...        # only these files or folders

After an --apply: /reload, /dumpall, and the recipe and tag counts must not change.
"""
import json
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import namespace_lint  # noqa: E402

DICT_DIR = namespace_lint.DICT_DIR
EXPORTED = ROOT / 'kubejs/exported'
# global name -> dict file, the aliases scripts usually bind, and the header for added keys.
DICTS = {
	'item': ('item_dict.js', ('i', 'item')),
	'blocks': ('block_dict.js', ('b',)),
	'fluid': ('fluid_dict.js', ('f',)),
	'effect': ('effect_dict.js', ('ef',)),
}
PREFER = ('item', 'blocks', 'fluid', 'effect')
LITERAL = re.compile(r'(?P<q>[\'"])(?P<id>#?[a-z0-9_.-]+:[a-z0-9_./-]+)(?P=q)')
ASSIGN = re.compile(r'^\s*[A-Za-z_$][\w$]*\.([A-Za-z_$][\w$]*)\s*=\s*[\'"]([^\'"]+)[\'"]')
BIND = re.compile(r'\b(?:var|let|const)\s+(\w+)\s*=\s*global\.(item|blocks|fluid|effect)\s*(?:;|$)', re.M)
BEFORE_SKIP = re.compile(r'(\.id\(|\bid\s*:|logName\(|\.create\(|requires:|\.name\(|translate\w*\(|Text\.\w+\()\s*$')
NCH_ALIAS = re.compile(r'\b((?:var|let|const)\s+\w+\s*=\s*)global\.NCH(\s*(?:;|$))', re.M)
SKIP_FILES = re.compile(r'kubejs_startup/creativecommon/(_lib|func)/')
KEY_OK = re.compile(r'^[A-Za-z_$][\w$]*$')
NOT_KEYS = {'na', 'none', 'incomplete'}


def load_dicts():
	"""global name -> (key -> value, value -> [keys])."""
	out = {}
	for name, (fname, _) in DICTS.items():
		keys, rev = {}, defaultdict(list)
		for line in (DICT_DIR / fname).read_text(encoding='utf-8').splitlines():
			m = ASSIGN.match(line)
			if m and KEY_OK.match(m.group(1)):
				keys[m.group(1)] = m.group(2)
		for k, v in keys.items():
			if k not in NOT_KEYS:
				rev[v].append(k)
		out[name] = (keys, rev)
	return out


def best_key(id_, keys):
	tail = re.split(r'[:/]', id_.lstrip('#'))[-1]
	want = ('t_' + tail) if id_.startswith('#') else tail
	return want if want in keys else keys[0]


def binding(text, table):
	for name, g in BIND.findall(text):
		if g == table:
			return name + '.'
	return 'global.' + table + '.'


def files(args):
	paths = [a for a in args if not a.startswith('-')]
	for path, rel in namespace_lint.script_files():
		if SKIP_FILES.search(rel):
			continue
		if paths and not any(rel.startswith(p.rstrip('/')) for p in paths):
			continue
		yield path, rel


def swaps_in(text, dicts):
	"""[(line, col, old, new)] for one file; the binding the file uses for each dict."""
	out = []
	bound = {g for _, g in BIND.findall(text)}
	for n, line in enumerate(text.splitlines(), 1):
		if line.lstrip().startswith('//') or line.lstrip().startswith('*'):
			continue
		code_end = len(line)
		cm = re.search(r'(?<![:\'"])//', line)
		if cm:
			code_end = cm.start()
		for m in LITERAL.finditer(line):
			if m.start() >= code_end:
				break
			if BEFORE_SKIP.search(line[:m.start()]):
				continue
			if re.match(r'\s*:', line[m.end():]):
				continue
			id_ = m.group('id')
			order = [t for t in PREFER if t in bound] + [t for t in PREFER if t not in bound]
			for table in order:
				rev = dicts[table][1]
				if id_ in rev:
					new = binding(text, table) + best_key(id_, rev[id_])
					out.append((n, m.start(), m.group(0), new))
					break
	return out


def apply_swaps(text, swaps):
	lines = text.split('\n')
	by_line = defaultdict(list)
	for n, col, old, new in swaps:
		by_line[n].append((col, old, new))
	for n, items in by_line.items():
		line = lines[n - 1]
		for col, old, new in sorted(items, reverse=True):
			line = line[:col] + new + line[col + len(old):]
		lines[n - 1] = line
	return '\n'.join(lines)


def known_ids():
	"""Item ids and item tags ('#ns:tag') and effect ids, from the /dumpall exports."""
	items = set()
	for p in (EXPORTED / 'items_by_mod').glob('*.json'):
		try:
			items.update(json.loads(p.read_text())['items'])
		except (ValueError, KeyError):
			pass
	try:
		items.update('#' + t for t in json.loads((EXPORTED / 'tags/item_tags.json').read_text()))
	except (ValueError, FileNotFoundError):
		pass
	effects = set()
	try:
		for mod in json.loads((EXPORTED / 'mob_effects.json').read_text()).values():
			effects.update(mod.keys())
	except (ValueError, FileNotFoundError):
		pass
	return items, effects


def missing(dicts):
	have = set()
	for _, rev in dicts.values():
		have.update(rev.keys())
	uses = Counter()
	where = defaultdict(set)
	for path, rel in files(sys.argv[1:]):
		for n, line in enumerate(path.read_text(encoding='utf-8', errors='replace').splitlines(), 1):
			if line.lstrip().startswith('//'):
				continue
			for m in LITERAL.finditer(line):
				id_ = m.group('id')
				if id_ in have or BEFORE_SKIP.search(line[:m.start()]) or re.match(r'\s*:', line[m.end():]):
					continue
				uses[id_] += 1
				where[id_].add(rel)
	return [(id_, c, sorted(where[id_])) for id_, c in uses.most_common() if c >= 2]


def add_keys(dicts, rows, apply):
	items, effects = known_ids()
	plan = {'item': [], 'effect': []}
	skipped = []
	for id_, c, _ in rows:
		table = 'item' if id_ in items else 'effect' if id_ in effects else None
		if not table:
			skipped.append(id_)
			continue
		keys = dicts[table][0]
		ns, path = id_.lstrip('#').split(':', 1)
		base = re.sub(r'[^a-z0-9_]', '_', path)
		key = ('t_' + base) if id_.startswith('#') else base
		if key in keys or any(key == k for k, _ in plan[table]):
			key = (('t_' if id_.startswith('#') else '') + re.sub(r'[^a-z0-9_]', '_', ns) + '_' + base)
		if key in keys or not KEY_OK.match(key):
			skipped.append(id_)
			continue
		plan[table].append((key, id_))
	for table, rows_ in plan.items():
		if not rows_:
			continue
		fname, aliases = DICTS[table]
		alias = {'item': 'i', 'effect': 'ef'}[table]
		block = ['', '// ############################## Keys added by tools/literal_id_swap.py ##############################',
			'// Ids used two or more times as literals in scripts (plan_rules_fixes.md section 7).']
		block += [f"{alias}.{k} = '{v}';" for k, v in rows_]
		print(f'{fname}: {len(rows_)} keys' + ('' if apply else ' (dry run)'))
		for line in block[3:]:
			print('   ' + line)
		if apply:
			p = DICT_DIR / fname
			text = p.read_text(encoding='utf-8').rstrip('\n')
			if table == 'effect' and '// ############################## Resolver' in text:
				head, tail = text.split('// ############################## Resolver', 1)
				text = head.rstrip('\n') + '\n' + '\n'.join(block) + '\n\n// ############################## Resolver' + tail
			else:
				text = text + '\n' + '\n'.join(block)
			p.write_text(text + '\n', encoding='utf-8')
	if skipped:
		print(f'not added ({len(skipped)}: not an item or effect in the dumps, or no free key): ' + ', '.join(skipped[:40]))


def aliases(apply, verbose):
	total = 0
	for path, rel in files(sys.argv[1:]):
		owner, pack_folder = namespace_lint.owner_of(rel)
		if not owner or pack_folder:
			continue
		text = path.read_text(encoding='utf-8')
		new, n = NCH_ALIAS.subn(lambda m: f"{m.group(1)}global.NCH.forPack('{owner}'){m.group(2)}", text)
		if n:
			total += n
			print(f'{n:4d}  {rel}  -> forPack(\'{owner}\')')
			if apply:
				path.write_text(new, encoding='utf-8')
	print(f'literal_id_swap --aliases: {total} aliases' + (' rewritten' if apply else ' (dry run)'))


def main():
	apply = '--apply' in sys.argv
	verbose = '-v' in sys.argv
	dicts = load_dicts()
	if '--aliases' in sys.argv:
		aliases(apply, verbose)
		return 0
	if '--missing' in sys.argv or '--add-keys' in sys.argv:
		rows = missing(dicts)
		if '--add-keys' in sys.argv:
			add_keys(dicts, rows, apply)
		else:
			for id_, c, where in rows:
				print(f'{c:5d}  {id_}  ({len(where)} files: {", ".join(where[:3])}{" ..." if len(where) > 3 else ""})')
			print(f'literal_id_swap --missing: {len(rows)} ids with no key used 2 or more times')
		return 0
	total = 0
	for path, rel in files(sys.argv[1:]):
		text = path.read_text(encoding='utf-8')
		swaps = swaps_in(text, dicts)
		if not swaps:
			continue
		total += len(swaps)
		print(f'{len(swaps):5d}  {rel}')
		if verbose:
			for n, _, old, new in swaps:
				print(f'         {n}: {old} -> {new}')
		if apply:
			path.write_text(apply_swaps(text, swaps), encoding='utf-8')
	print(f'literal_id_swap: {total} literals' + (' rewritten' if apply else ' would be rewritten (dry run)'))
	return 0


if __name__ == '__main__':
	sys.exit(main())
