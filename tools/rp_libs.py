#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Raspberry Pie's LGPL source zips for a release.

  python3 tools/rp_libs.py sources DIR      download the source of each nested LGPL-2.1 mod at the pinned tag or commit
                                            (lgpl_sources.csv, the same refs as the jar's NOTICE) as a zip into DIR, and
                                            print each zip's sha1. Attach the zips to the Raspberry Pie GitHub release
                                            (PR-RP-08, owner 2026-11-22).
The compile jars in libs/ come from tools/build_jars.py (development/build_jars.csv, the manifest of every in-tree mod):
  python3 tools/build_jars.py fetch --projects solRaspberryPie
"""
import argparse
import csv
import hashlib
import shutil
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MOD = ROOT / 'development/solRaspberryPie'
SOURCES_CSV = MOD / 'lgpl_sources.csv'
AGENT = 'Mozilla/5.0 (build script)'


def sha1(path):
	h = hashlib.sha1()
	with open(path, 'rb') as f:
		for chunk in iter(lambda: f.read(1 << 16), b''):
			h.update(chunk)
	return h.hexdigest()


def rows(path):
	with open(path, newline='', encoding='utf-8') as f:
		return list(csv.DictReader(f))


def download(url, dest):
	req = urllib.request.Request(url, headers={'User-Agent': AGENT})
	with urllib.request.urlopen(req, timeout=120) as r, open(dest, 'wb') as out:
		shutil.copyfileobj(r, out)



def sources(out_dir):
	out = Path(out_dir)
	out.mkdir(parents=True, exist_ok=True)
	failed = 0
	for r in rows(SOURCES_CSV):
		url = f'{r["repo"]}/archive/{r["ref"]}.zip'
		dest = out / r['zip']
		try:
			download(url, dest)
		except Exception as e:
			print(f'FAIL {r["name"]}: {e} ({url})')
			failed += 1
			continue
		print(f'{dest.name}  sha1 {sha1(dest)}  from {url}')
	return 1 if failed else 0


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	ap.add_argument('cmd', choices=['sources'])
	ap.add_argument('dir')
	a = ap.parse_args()
	return sources(a.dir)


if __name__ == '__main__':
	sys.exit(main())
