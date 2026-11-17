#!/usr/bin/env python3
"""Plan lint (plan_process_overhaul.md P3.4 l). WARN only, exit 0.

A feature_writeup/plan_*.md created after 2026-11-16 needs an "Owner rulings" table (skill `intake`: the rulings
that settled the owner's open items). Creation date: the commit that added the file, or today when untracked.
A file whose head (first 10 lines) carries the `**Superseded by` marker is a kept record, not a live plan, and is skipped.
Usage: python3 tools/plan_lint.py
"""
import datetime as dt
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SINCE = '2026-11-16'
RULINGS_RE = re.compile(r'^#+ .*owner rulings|^\|[^\n]*\bruling\b', re.I | re.M)
SUPERSEDED_RE = re.compile(r'\*\*Superseded by\b')


def created(path):
	r = subprocess.run(['git', 'log', '--diff-filter=A', '--format=%as', '--', str(path.relative_to(ROOT))],
					   cwd=ROOT, capture_output=True, text=True)
	days = [l for l in r.stdout.split() if l]
	return days[-1] if days else dt.date.today().isoformat()


def main():
	n = 0
	for p in sorted((ROOT / 'feature_writeup').glob('plan_*.md')):
		day = created(p)
		if day <= SINCE:
			continue
		text = p.read_text(errors='replace')
		if SUPERSEDED_RE.search('\n'.join(text.split('\n')[:10])):
			continue
		n += 1
		if not RULINGS_RE.search(text):
			print('WARN %s (created %s): no "Owner rulings" table; intake records the rulings (skill intake)'
				  % (p.relative_to(ROOT), day))
	print('plan_lint: %d plan(s) created after %s checked' % (n, SINCE))
	return 0


if __name__ == '__main__':
	sys.exit(main())
