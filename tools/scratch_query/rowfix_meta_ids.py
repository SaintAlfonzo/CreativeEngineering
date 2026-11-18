# SPDX-License-Identifier: GPL-3.0-only
"""Rewrite stale creativeengineering: worldgen ids in scratch queue rows to creativeworldgen: (r1118 rowfix-2, 2026-11-18).

The 2026-11-09 namespace moves put meta structures, structure tags (meta/, group/, type/), group structure sets,
sky_island / waystone_ruin_part, the moved dimensions and the earth_fill / ground_match / grass_like_soils block tags
under creativeworldgen:. An id is rewritten only when its creativeworldgen: twin exists in the current sources
(meta_structures.json, structure_tags.json, structure_groups.json, Creative Worldgen's data folder, the tag scripts);
a fragment ending in `/` or `_` (a regex or glob prefix) only when some current id starts with it. Rows that carry
`@absent` (old-save or alias checks) are left alone. Idempotent: a second run changes nothing.

Usage: python3 tools/scratch_query/rowfix_meta_ids.py [--dry] [FILE.qb ...]   (default: the r1118 file list)
"""
import json
import os
import re
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
ROWS = os.path.join(ROOT, 'feature_writeup', 'testing', 'rows')
DEFAULT = ['SB', 'PSU', 'SPF', 'CWG', 'RD', 'WGE', 'META31', 'SSP', 'SALT', 'WG30']
NEW = 'creativeworldgen:'
FAMILIES = ('meta/', 'group/', 'type/', 'earth_fill/', 'ground_match/', 'worldgen/')
ID = re.compile(r'creativeengineering:([a-z0-9_/]+)')


def valid_ids():
	gen = os.path.join(ROOT, 'kubejs', 'generation_scripts', 'worldgen')
	ids = set(json.load(open(os.path.join(gen, 'meta_structures.json')))['structures'])
	ids |= {NEW + t for t in json.load(open(os.path.join(gen, 'structure_tags.json')))['tags']}
	ids |= {NEW + 'group/' + g for g in json.load(open(os.path.join(gen, 'structure_groups.json')))['groups']}
	data = os.path.join(ROOT, 'development', 'creativeWorldgen', 'src', 'main', 'resources', 'data', 'creativeworldgen')
	for sub in ('dimension', 'worldgen/structure', 'worldgen/structure_set', 'worldgen/template_pool'):
		d = os.path.join(data, sub)
		ids |= {NEW + f[:-5] for f in os.listdir(d) if f.endswith('.json')} if os.path.isdir(d) else set()
	ids.add(NEW + 'seabed_clearance')   # SeabedClearance.java
	tag = re.compile(r'creativeworldgen:((?:earth_fill|ground_match)/[a-z0-9_/]+|worldgen/grass_like_soils)')
	for base in ('kubejs/server_scripts', 'kubejs/data/creativeworldgen', 'development/creativeWorldgen/src/main'):
		for dp, _, fs in os.walk(os.path.join(ROOT, base)):
			for f in fs:
				if f.endswith(('.js', '.json', '.java')):
					try:
						ids |= {NEW + m for m in tag.findall(open(os.path.join(dp, f), encoding='utf-8').read())}
					except (OSError, UnicodeDecodeError):
						pass
	return ids


def is_worldgen(path, ids):
	return path.startswith(FAMILIES) or (NEW + path) in ids


def target(path, ids):
	new = NEW + path
	if new in ids:
		return new
	if path.endswith(('/', '_')) and any(i.startswith(new) for i in ids):
		return new
	return None


def fix(path, ids, unresolved):
	lines = open(path, encoding='utf-8').read().split('\n')
	out, changed_rows, n, absent = [], set(), 0, False
	# a row runs from its `## ` header to the next `## ` / `@@` line; mark rows that hold @absent
	starts = [i for i, l in enumerate(lines) if l.startswith('## ') or l.startswith('@@')] + [len(lines)]
	skip = set()
	for a, b in zip(starts, starts[1:]):
		if lines[a].startswith('## ') and any(l.startswith('@absent') for l in lines[a:b]):
			skip.update(range(a, b))
	row = None
	for i, l in enumerate(lines):
		if l.startswith('## '):
			row = l[3:].split('|')[0].split('(')[0].split('**')[0].strip()
		elif l.startswith('@@'):
			row = None
		if i in skip:
			if ID.search(l):
				absent = True
			out.append(l)
			continue

		def sub(m):
			p = m.group(1)
			if not is_worldgen(p, ids):
				return m.group(0)
			t = target(p, ids)
			if t is None:
				unresolved.add('creativeengineering:' + p)
				return m.group(0)
			return t
		nl = ID.sub(sub, l)
		if nl != l:
			n += 1
			if row:
				changed_rows.add(row)
		out.append(nl)
	return '\n'.join(out), n, changed_rows, absent


def main(argv):
	dry = '--dry' in argv
	files = [a for a in argv if not a.startswith('--')] or [os.path.join(ROWS, c + '.qb') for c in DEFAULT]
	ids = valid_ids()
	unresolved, all_rows = set(), []
	for f in files:
		if os.path.basename(f) == 'MELT.qb':
			continue
		text, n, rows, absent = fix(f, ids, unresolved)
		print('%s: %d lines%s' % (os.path.basename(f), n, ' (an @absent row kept)' if absent else ''))
		if n and not dry:
			open(f, 'w', encoding='utf-8').write(text)
		all_rows += sorted(rows)
	print('rows: ' + ' '.join(all_rows))
	print('unresolved: ' + (' '.join(sorted(unresolved)) or 'none'))


if __name__ == '__main__':
	main(sys.argv[1:])
