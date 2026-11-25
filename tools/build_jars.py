#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Build-time third-party jars of the in-tree mods: the tracked manifest and the fetch for a clean clone.

The in-tree mods compile against mod jars that are never committed (`.gitignore`): each mod's `libs/` (copies of pack
jars), Creative Delight's `upstream/` (the repack and port base jars) and, for Tempad, the pack's `mods/`.
`development/build_jars.csv` records every one (path, version, sha1, download url, page, origin, the projects that need
it), so a clone can be filled and checked byte for byte (publish_readiness.md PR-RE-06, PR-CM-05).

  python3 tools/build_jars.py scan [--online]
        Owner machine: rewrite the manifest from the jars in this tree. Sources come by sha1 from pack/manifest.json
        (every committed version), the Modrinth sha1 lookup (--online, only for jars still without a source) and the
        jars nested in a pack jar (META-INF/jarjar/). A jar that is gone keeps its old row.
  python3 tools/build_jars.py fetch [--from DIR] [--projects a,b] [--no-download] [--mods]
        Fill the missing or wrong jars, each sha1-checked: copied from another instance or checkout DIR (same path, or
        the same file name in its mods/, client_mods/, *_upstream/, devmods/, development/), extracted from the pack
        jar that nests it, or downloaded from the manifest url. --projects limits it to the jars those Gradle projects
        need (with their project dependencies and bundle members), e.g. creativeCommon,creativeCompat,solRaspberryPie.
        Rows under mods/ (Tempad) are written only with --mods, so a real instance's mods/ is never touched by default.
  python3 tools/build_jars.py check [--projects a,b]
        No network: exit 1 when a jar is missing or its sha1 differs.

Then: ./gradlew :<project>:build --offline. Jars that only port tools read (upstream/donors/, devmods/) are listed with
the project `tool`.
"""
import argparse
import csv
import fnmatch
import hashlib
import json
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MANIFEST = ROOT / 'development/build_jars.csv'
HEADER = ['path', 'version', 'sha1', 'url', 'page', 'origin', 'nested_in', 'projects']
AGENT = 'Mozilla/5.0 (build script)'
SKIP_DIRS = ('/build/', '/jar_archive/', '/moved_code_backup/', '/.gradle/', '/run/')
# Jars the pack lists as "local" (hand-installed) and Modrinth does not have, by sha1: CurseForge file id, checked by
# downloading the file and comparing its sha1.
CURSEFORGE_BY_SHA1 = {
	'2e1fbc6a5961d7ff196cf80a569d4341d2913680': ('biomes-o-plenty', 4473556),  # BiomesOPlenty-1.19.2-17.1.2.544.jar
}


def sha1(path):
	h = hashlib.sha1()
	with open(path, 'rb') as f:
		for chunk in iter(lambda: f.read(1 << 16), b''):
			h.update(chunk)
	return h.hexdigest()


def read_manifest():
	if not MANIFEST.exists():
		return []
	with open(MANIFEST, newline='', encoding='utf-8') as f:
		return list(csv.DictReader(f))


# ---- projects and what each one compiles against ----------------------------------------------------------------

def projects():
	"""Gradle project name -> project dir (repo-relative), from settings.gradle."""
	s = (ROOT / 'settings.gradle').read_text(encoding='utf-8')
	s = re.sub(r'//[^\n]*', '', s)
	out = {}
	for m in re.finditer(r'\[([^\]]*)\]\.each\s*\{.*?file\("([^"]+)"\)', s, re.S):
		body, tmpl = m.groups()
		pairs = re.findall(r"'([^']+)'\s*:\s*'([^']+)'", body)
		if pairs:
			for name, d in pairs:
				out[name] = tmpl.replace('${dir}', d).replace('${name}', name)
		else:
			for name in re.findall(r"'([^']+)'", body):
				out[name] = tmpl.replace('${name}', name)
	for name, d in re.findall(r"project\(':(\w+)'\)\.projectDir\s*=\s*file\('([^']+)'\)", s):
		out[name] = d
	return out


def props(pdir):
	p = ROOT / pdir / 'gradle.properties'
	out = {}
	if p.exists():
		text = re.sub(r'\\\n\s*', '', p.read_text(encoding='utf-8'))
		for line in text.splitlines():
			if '=' in line and not line.lstrip().startswith('#'):
				k, v = line.split('=', 1)
				out[k.strip()] = v.strip()
	return out


def jars_in(rel_dir, patterns=('*.jar',)):
	d = ROOT / rel_dir
	if not d.is_dir():
		return []
	return sorted(str(p.relative_to(ROOT)) for p in d.iterdir()
			if p.is_file() and any(fnmatch.fnmatch(p.name, pat) for pat in patterns))


def direct_needs(pdir, manifest_paths):
	"""(jar paths, project deps) one project names in its build.gradle and gradle.properties."""
	bg = ROOT / pdir / 'build.gradle'
	text = re.sub(r'//[^\n]*', '', bg.read_text(encoding='utf-8')) if bg.exists() else ''
	pr = props(pdir)
	jars, deps = set(), set(re.findall(r"project\(':(\w+)'\)", text))
	deps |= {m.lstrip(':') for m in pr.get('bundle_members', '').split(',') if m.strip()}

	def resolve(path, rooted):
		return str(Path(path) if rooted else Path(pdir, path)).replace('\\', '/')

	def norm(p):
		return str(Path(*[x for x in Path(p).parts if x != '.'])) if '..' not in p else str((ROOT / p).resolve().relative_to(ROOT))

	def existing_or_listed(rel_dir, pats):
		found = set(jars_in(rel_dir, pats))
		found |= {p for p in manifest_paths if str(Path(p).parent) == rel_dir and any(fnmatch.fnmatch(Path(p).name, x) for x in pats)}
		return found

	# own libs/ (the flatDir 'local:' repository and fileTree 'libs')
	jars |= existing_or_listed(norm(f'{pdir}/libs'), ('*.jar',))
	for m in re.finditer(r"fileTree\(\s*dir:\s*(rootProject\.file\()?'([^']+)'\)?\s*,\s*include:\s*(\[[^\]]*\]|'[^']*')", text, re.S):
		rooted, d, inc = m.groups()
		pats = re.findall(r"'([^']+)'", inc)
		jars |= existing_or_listed(norm(resolve(d, rooted)), pats)
	for m in re.finditer(r"files\(\s*(rootProject\.file\()?'([^']+\.jar)'", text):
		rooted, f = m.groups()
		jars.add(norm(resolve(f, rooted)))
	if 'ce.forge-repack' in text and pr.get('upstream_jar'):
		jars.add(f"development/creativeDelight/upstream/{pr['upstream_jar']}")
	return jars, deps


def needs_by_project(manifest_paths=()):
	projs = projects()
	direct = {n: direct_needs(d, set(manifest_paths)) for n, d in projs.items()}
	closure = {}

	def walk(n, seen):
		if n in seen or n not in direct:
			return set()
		seen.add(n)
		jars, deps = direct[n]
		out = set(jars)
		for d in deps:
			out |= walk(d, seen)
		return out

	for n in projs:
		closure[n] = walk(n, set())
	return closure


# ---- sources ----------------------------------------------------------------------------------------------------

def cf_download(fid, name):
	return f'https://mediafilez.forgecdn.net/files/{fid // 1000}/{fid % 1000}/{urllib.parse.quote(name)}'


def pack_index():
	"""sha1 -> (version, url, page, origin) from every committed version of pack/manifest.json."""
	idx = {}
	try:
		revs = subprocess.run(['git', 'log', '--format=%H', '--', 'pack/manifest.json'], cwd=ROOT,
				capture_output=True, text=True, check=True).stdout.split()
	except Exception:
		revs = []
	texts = []
	if (ROOT / 'pack/manifest.json').exists():
		texts.append(('pack', (ROOT / 'pack/manifest.json').read_text(encoding='utf-8')))
	for r in revs:
		out = subprocess.run(['git', 'show', f'{r}:pack/manifest.json'], cwd=ROOT, capture_output=True, text=True)
		if out.returncode == 0:
			texts.append(('pack history', out.stdout))
	for origin, t in texts:
		try:
			d = json.loads(t)
		except ValueError:
			continue
		for e in d.get('mods', []) + d.get('disabledMods', []):
			src = e.get('source')
			if not isinstance(src, dict) or e.get('sha1') in idx:
				continue
			mr, cf = src.get('modrinth') or {}, src.get('curseforge') or {}
			url = mr.get('download') or (cf_download(cf['fileId'], e['file']) if cf.get('fileId') else '')
			page = cf.get('url') or mr.get('url') or ''
			if url:
				idx[e['sha1']] = (e.get('version', ''), url, page, 'modrinth' if mr.get('download') else 'curseforge')
	return idx


def modrinth_lookup(h):
	try:
		req = urllib.request.Request(f'https://api.modrinth.com/v2/version_file/{h}?algorithm=sha1', headers={'User-Agent': AGENT})
		with urllib.request.urlopen(req, timeout=30) as r:
			d = json.load(r)
	except Exception:
		return None
	for f in d.get('files', []):
		if f.get('hashes', {}).get('sha1') == h:
			return (d.get('version_number', ''), f['url'], f"https://modrinth.com/mod/{d['project_id']}/version/{d['id']}", 'modrinth')
	return None


def nested_index():
	"""sha1 -> (inner name, parent file, parent sha1) for the jars nested (jarjar) in the pack's mods/ jars."""
	idx = {}
	for p in sorted((ROOT / 'mods').glob('*.jar')):
		try:
			with zipfile.ZipFile(p) as z:
				for n in z.namelist():
					if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
						idx.setdefault(hashlib.sha1(z.read(n)).hexdigest(), (n, p.name, sha1(p)))
		except (zipfile.BadZipFile, OSError):
			pass
	return idx


def guess_version(name):
	m = re.search(r'(\d+(?:\.\d+)+[^/]*?)\.jar$', name)
	return m.group(1) if m else ''


def scan(online):
	old = {r['path']: r for r in read_manifest()}
	tools = set(jars_in('development/creativeDelight/upstream/donors'))
	closure = needs_by_project(old.keys())
	users = {}
	for n, jars in closure.items():
		for j in jars:
			users.setdefault(j, set()).add(n)
	for t in tools:
		users.setdefault(t, set()).add('tool')
	pidx, nidx = pack_index(), None
	by_sha = {r['sha1']: r for r in old.values() if r.get('url')}
	rows, missing = [], []
	for path in sorted(users):
		f = ROOT / path
		if not f.is_file():
			if path in old:
				r = dict(old[path])
				r['projects'] = ' '.join(sorted(users[path]))
				rows.append(r)
			else:
				missing.append(path)
			continue
		h = sha1(f)
		r = {'path': path, 'sha1': h, 'version': '', 'url': '', 'page': '', 'origin': '', 'nested_in': '',
				'projects': ' '.join(sorted(users[path]))}
		if h in pidx:
			r['version'], r['url'], r['page'], r['origin'] = pidx[h]
		elif h in CURSEFORGE_BY_SHA1:
			slug, fid = CURSEFORGE_BY_SHA1[h]
			r['url'], r['page'], r['origin'] = cf_download(fid, f.name), f'https://www.curseforge.com/minecraft/mc-mods/{slug}/files/{fid}', 'curseforge'
		elif h in by_sha:
			for k in ('version', 'url', 'page', 'origin', 'nested_in'):
				r[k] = by_sha[h][k]
		else:
			if nidx is None:
				nidx = nested_index()
			if h in nidx:
				inner, parent, psha = nidx[h]
				pv = pidx.get(psha)
				r['version'] = guess_version(inner)
				r['nested_in'] = f'{parent}|{inner}'
				r['origin'] = 'nested'
				if pv:
					r['url'], r['page'] = pv[1], pv[2]
			elif online and (hit := modrinth_lookup(h)):
				r['version'], r['url'], r['page'], r['origin'] = hit
			else:
				r['origin'] = 'local'
		if not r['version']:
			r['version'] = guess_version(f.name)
		rows.append(r)
	with open(MANIFEST, 'w', newline='', encoding='utf-8') as out:
		w = csv.DictWriter(out, HEADER, lineterminator='\n')
		w.writeheader()
		w.writerows(rows)
	nosrc = [r['path'] for r in rows if not r['url']]
	print(f'build_jars: {len(rows)} jars, {len({r["sha1"] for r in rows})} distinct, {len(nosrc)} without a download url')
	for p in nosrc:
		print(f'  no source: {p}')
	for p in missing:
		print(f'  named by a build file but not in this tree: {p}')
	return 0


# ---- fetch and check --------------------------------------------------------------------------------------------

def selected(rows, names, with_mods=True):
	if names:
		want = set(names.split(','))
		unknown = want - set(projects())
		if unknown:
			sys.exit(f'build_jars: unknown project(s) {", ".join(sorted(unknown))}')
		rows = [r for r in rows if want & set(r['projects'].split())]
	if not with_mods:
		rows = [r for r in rows if not r['path'].startswith('mods/')]
	return rows


def source_index(src):
	"""file name -> paths under the other instance or checkout."""
	idx = {}
	globs = ['mods/*.jar', 'client_mods/*.jar', 'disabledmods/**/*.jar', '*_upstream/*.jar', 'devmods/**/*.jar',
			'development/**/libs/*.jar', 'development/creativeDelight/upstream/**/*.jar']
	for g in globs:
		for p in src.glob(g):
			if not any(s in str(p) for s in SKIP_DIRS):
				idx.setdefault(p.name, []).append(p)
	return idx


def download(url, dest):
	req = urllib.request.Request(url, headers={'User-Agent': AGENT})
	with urllib.request.urlopen(req, timeout=180) as r, open(dest, 'wb') as out:
		shutil.copyfileobj(r, out)


def fetch(args):
	rows = selected(read_manifest(), args.projects, args.mods)
	src = Path(args.from_dir).expanduser().resolve() if args.from_dir else None
	idx = source_index(src) if src else {}
	done, failed, counts = {}, [], {'ok': 0, 'copied': 0, 'extracted': 0, 'downloaded': 0}
	tmpdir = Path(tempfile.mkdtemp(prefix='build_jars-'))
	try:
		for r in rows:
			dest = ROOT / r['path']
			if dest.is_file() and sha1(dest) == r['sha1']:
				counts['ok'] += 1
				done.setdefault(r['sha1'], dest)
				continue
			dest.parent.mkdir(parents=True, exist_ok=True)
			got, how = None, ''
			if r['sha1'] in done:
				got, how = done[r['sha1']], 'copied'
			if not got and src:
				for c in [src / r['path']] + idx.get(Path(r['path']).name, []):
					if c.is_file() and sha1(c) == r['sha1']:
						got, how = c, 'copied'
						break
			if not got and r.get('nested_in'):
				parent, inner = r['nested_in'].split('|', 1)
				cands = [c for c in idx.get(parent, []) if c.is_file()]
				if not cands and r['url'] and not args.no_download:
					t = tmpdir / parent
					try:
						download(r['url'], t)
						cands = [t]
					except Exception as e:
						print(f'  {parent}: {e}')
				for c in cands:
					try:
						with zipfile.ZipFile(c) as z:
							data = z.read(inner)
					except (KeyError, zipfile.BadZipFile):
						continue
					if hashlib.sha1(data).hexdigest() == r['sha1']:
						t = tmpdir / Path(inner).name
						t.write_bytes(data)
						got, how = t, 'extracted'
						break
			if not got and r['url'] and not r.get('nested_in') and not args.no_download:
				t = tmpdir / Path(r['path']).name
				try:
					download(r['url'], t)
					if sha1(t) == r['sha1']:
						got, how = t, 'downloaded'
					else:
						print(f'  {r["path"]}: download has sha1 {sha1(t)}, not {r["sha1"]}')
				except Exception as e:
					print(f'  {r["path"]}: {e}')
			if got:
				shutil.copyfile(got, dest)
				done[r['sha1']] = dest
				counts[how] += 1
			else:
				failed.append(r)
	finally:
		shutil.rmtree(tmpdir, ignore_errors=True)
	print('build_jars: ' + ', '.join(f'{v} {k}' for k, v in counts.items()) + f', {len(failed)} failed')
	for r in failed:
		print(f'  FAIL {r["path"]} (sha1 {r["sha1"]}; {r["page"] or "no public source: copy it with --from"})')
	return 1 if failed else 0


def check(args):
	bad = 0
	for r in selected(read_manifest(), args.projects):
		p = ROOT / r['path']
		if not p.is_file():
			print(f'missing {r["path"]}')
			bad += 1
		elif sha1(p) != r['sha1']:
			print(f'sha1 differs {r["path"]}')
			bad += 1
	print('build_jars: complete' if not bad else f'build_jars: {bad} jar(s) to fetch (python3 tools/build_jars.py fetch)')
	return 1 if bad else 0


def main():
	ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
	sub = ap.add_subparsers(dest='cmd', required=True)
	s = sub.add_parser('scan')
	s.add_argument('--online', action='store_true', help='look jars without a pack source up on Modrinth by sha1')
	f = sub.add_parser('fetch')
	f.add_argument('--from', dest='from_dir', help='another instance or checkout to copy the jars from')
	f.add_argument('--projects', help='comma-separated Gradle project names')
	f.add_argument('--no-download', action='store_true')
	f.add_argument('--mods', action='store_true', help='also write the rows under mods/ (clean clones only)')
	c = sub.add_parser('check')
	c.add_argument('--projects')
	a = ap.parse_args()
	if a.cmd == 'scan':
		return scan(a.online)
	return fetch(a) if a.cmd == 'fetch' else check(a)


if __name__ == '__main__':
	sys.exit(main())
