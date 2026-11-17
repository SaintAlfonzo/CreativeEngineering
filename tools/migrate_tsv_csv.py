#!/usr/bin/env python3
"""One-time move of the status and process tables from TSV to CSV (plan_process_overhaul.md P3.3 item 1, D14).
Leading `#` comment lines are kept (the word "Tab separated" becomes "Comma separated (CSV)"); every row is checked to
read back identical. Tracked files move with `git mv` so history follows. Usage: python3 tools/migrate_tsv_csv.py [--dry]
"""
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import table_io  # noqa: E402

FILES = ['feature_writeup/status/features.tsv', 'jar_archive/installed.tsv', 'tools/check_known.tsv',
		 'feature_writeup/texture_sources/sources.tsv', 'feature_writeup/texture_sources/ledger.tsv']


def tracked(p):
	return subprocess.run(['git', 'ls-files', '--error-unmatch', p], cwd=ROOT, capture_output=True).returncode == 0


def main(dry):
	for rel in FILES:
		src = ROOT / rel
		dst = src.with_suffix('.csv')
		if not src.exists():
			print('skip (absent):', rel)
			continue
		lines = src.read_text(encoding='utf-8').rstrip('\n').split('\n')
		comments = []
		while lines and lines[0].startswith('#'):
			comments.append(lines.pop(0).replace('<TAB>', ',').replace('Tab separated', 'Comma separated (CSV)')
							.replace('.tsv', '.csv'))
		header = lines[0].split('\t')
		rows = [l.split('\t') for l in lines[1:] if l.strip()]
		rows = [r + [''] * (len(header) - len(r)) for r in rows]
		text = table_io.dumps(header, rows, comments)
		if dry:
			print('%s -> %s: %d rows' % (rel, dst.name, len(rows)))
			continue
		if tracked(rel):
			subprocess.run(['git', 'mv', rel, str(dst.relative_to(ROOT))], cwd=ROOT, check=True)
		else:
			src.rename(dst)
		dst.write_text(text, encoding='utf-8')
		c2, h2, r2 = table_io.read(dst)
		assert h2 == header and r2 == rows and c2 == comments, 'round trip differs: ' + rel
		print('%s -> %s: %d rows, round trip ok' % (rel, dst.name, len(rows)))


if __name__ == '__main__':
	main('--dry' in sys.argv[1:])
