#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Raspberry Pie's third-party files: fetch the compile jars into libs/ (sha1-checked) and the LGPL source zips for a release.

  python3 tools/rp_libs.py fetch            download every jar in development/solRaspberryPie/libs.csv that libs/ lacks
                                            or holds with a wrong sha1; each download must match its sha1 or it is
                                            discarded (exit 1). Run once on a fresh clone before
                                            ./gradlew :solRaspberryPie:build --offline (publish_readiness.md PR-RP-02).
  python3 tools/rp_libs.py check            exit 1 when a libs/ jar is missing or its sha1 differs (no network).
  python3 tools/rp_libs.py sources DIR      download the source of each nested LGPL-2.1 mod at the pinned tag or commit
                                            (lgpl_sources.csv, the same refs as the jar's NOTICE) as a zip into DIR, and
                                            print each zip's sha1. Attach the zips to the Raspberry Pie GitHub release
                                            (PR-RP-08, owner 2026-11-22).
libs/ stays git-ignored; libs.csv is the record of what goes in it.
"""
import argparse
import csv
import hashlib
import shutil
import sys
import tempfile
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MOD = ROOT / 'development/solRaspberryPie'
LIBS = MOD / 'libs'
LIBS_CSV = MOD / 'libs.csv'
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


def check():
	bad = 0
	for r in rows(LIBS_CSV):
		p = LIBS / r['file']
		if not p.exists():
			print(f'missing {r["file"]}')
			bad += 1
		elif sha1(p) != r['sha1']:
			print(f'sha1 differs {r["file"]}')
			bad += 1
	print('rp_libs: libs/ complete' if not bad else f'rp_libs: {bad} jar(s) to fetch (python3 tools/rp_libs.py fetch)')
	return 1 if bad else 0


def fetch():
	LIBS.mkdir(exist_ok=True)
	failed = 0
	for r in rows(LIBS_CSV):
		p = LIBS / r['file']
		if p.exists() and sha1(p) == r['sha1']:
			print(f'ok {r["file"]}')
			continue
		with tempfile.TemporaryDirectory() as t:
			tmp = Path(t) / r['file']
			try:
				download(r['url'], tmp)
			except Exception as e:
				print(f'FAIL {r["file"]}: {e} (get it by hand from {r["page"]})')
				failed += 1
				continue
			got = sha1(tmp)
			if got != r['sha1']:
				print(f'FAIL {r["file"]}: sha1 {got}, expected {r["sha1"]}; discarded')
				failed += 1
				continue
			shutil.move(str(tmp), p)
			print(f'fetched {r["file"]}')
	return 1 if failed else 0


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
	ap.add_argument('cmd', choices=['fetch', 'check', 'sources'])
	ap.add_argument('dir', nargs='?')
	a = ap.parse_args()
	if a.cmd == 'fetch':
		return fetch()
	if a.cmd == 'check':
		return check()
	if not a.dir:
		ap.error('sources needs an output directory')
	return sources(a.dir)


if __name__ == '__main__':
	sys.exit(main())
