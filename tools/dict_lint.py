#!/usr/bin/env python3
"""Duplicate key assignments in the dict files (design rule DR-C2, plan_rules_fixes.md section 7).

A dict key assigned twice in one dict file is a bug: the later line wins silently, so the earlier one is dead
(V-22 to V-24: sea pickles, hops, lead springs). This lint reads every `*_dict.js` in Creative Common's `_lib/`
and fails when one `<alias>.<key> =` appears on two lines of the same file.

	python3 tools/dict_lint.py        # exit 1 on a duplicate
	python3 tools/dict_lint.py -v     # also list the keys checked per file
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DICT_DIR = ROOT / 'development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib'
ASSIGN = re.compile(r'^\s*([A-Za-z_$][\w$]*(?:\.[\w$]+)*)\.([\w$]+)\s*=(?!=)')


def duplicates(path):
	seen = defaultdict(list)
	for n, line in enumerate(path.read_text(encoding='utf-8').splitlines(), 1):
		m = ASSIGN.match(line)
		if m:
			seen[(m.group(1), m.group(2))].append(n)
	return seen, {k: v for k, v in seen.items() if len(v) > 1}


def main():
	verbose = '-v' in sys.argv
	bad = 0
	for path in sorted(DICT_DIR.glob('*_dict.js')):
		seen, dups = duplicates(path)
		rel = path.relative_to(ROOT)
		if verbose:
			print(f'{rel}: {len(seen)} keys')
		for (alias, key), lines in sorted(dups.items()):
			bad += 1
			print(f'{rel}: {alias}.{key} assigned on lines {", ".join(map(str, lines))} (the last one wins)')
	print(f'dict_lint: {bad} duplicate keys' if bad else 'dict_lint: OK, no duplicate keys')
	return 1 if bad else 0


if __name__ == '__main__':
	sys.exit(main())
