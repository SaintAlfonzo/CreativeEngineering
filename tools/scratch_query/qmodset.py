#!/usr/bin/env python3
"""Mod-set manifests for scratch servers (plan_rowfix_r1118.md section 5, R4 B1): reduced and chain jar sets as data.

A manifest is tools/scratch_query/modsets/NAME.toml. A rows section names one with `@@modset SPEC` (a row overrides it
with `@modset SPEC`, `@modset none` = the full pack); the driver (qdrive.py) then builds the step's server from it
instead of plain prep_srv.sh, and the spec is part of the step key, so rows with different sets get their own boots.
`@@prep boot=N: modset SPEC` switches the jar set (and the kubejs / config policy) of an existing folder before boot N,
keeping the world (the 1.2.02 upgrade section: boot 1 on the release jars, boot 2 on the current ones).

SPEC:  NAME [VARIANT] [+GLOB | +@GROUP ...] [-GLOB | -@GROUP ...] [KEY=VALUE ...]   (shell-quoted: "Felsic Gear *.jar")
       NAME `pack` = the full pack as prep_srv.sh builds it (only useful in `@@prep boot=N: modset pack`).
       +GLOB / +@GROUP add jars (the newest match in the source folders, else the pack's mods/); -@GROUP takes a group out
       of the variant's keep list before the pick (cc_only's without=); -GLOB drops jars after the pick. KEY=VALUE
       overrides a manifest field: kubejs, config, upstream (one word), noupstream (comma list).

Manifest fields (TOML; every field optional):
    description    one line
    extends        another manifest: its fields, groups and variants are the base, this file's win
    source         pack (default) | ceporttesting | deploy: where the jars come from.
                   pack: prep_srv.sh (hard links, dev-jar swaps, perfBot, zq datapack, props), then the pick prunes it.
                   ceporttesting: ../CEPortTesting/mods and its disabledmods/cba_parked/ (scratch_server.sh setup only).
                   deploy: the pack's jars (prep_srv.sh with NOSWAP=1) with deploy/latest/mods laid over them (same jar
                   stem replaced), then every jar not in deploy/latest/verify/server.installed.sha1 dropped except
                   the dev_keep jars
    swap           pack: prep_srv.sh dev-jar swaps (default true; false = NOSWAP=1)
    group          "B" forces a normal world (seed 8675309) whatever the rows' group; default: the step's group
    kubejs         pack (default) | source (the source instance's kubejs/) | probes (empty but server_scripts/zz_*_probe.js
                   and data/zq) | release (git archive of release_commit, the zz_ probes and zq data kept)
    config         pack (default) | source | empty (config/ and defaultconfigs/ empty: every jar writes its defaults) |
                   release (git archive of release_commit)
    release_commit the commit kubejs / config = release read (default 40ed32be, the 1.2.02 release)
    upstream       pack (default: the pack's *_upstream donor folders) | source | none;  noupstream = ["name", ...]
    copy           [["repo path", "srv path"], ...] copied in after the build
    forbid         ["regex", ...]: a jar name in srv/mods matching one fails the build
    require        ["glob" or "@group", ...]: each must match a jar after the pick, else the build fails (a chain jar
                   missing from the source; port_chain.sh check)
    dev_keep       deploy: globs of prep_srv.sh's dev-only jars kept through the release filter (default
                   ["cdevperfbot-*.jar"]: perfBot with its -Dcdevperfbot.out JVM arg; [] = the release jars only)
    unlisted_ok    true: qpin accepts the source's jars without a mod_sides.md row (deploy: owner ruling 3,
                   2026-11-18; ceporttesting: plan section 5 B1); jars from the pack still need their row
    default        the variant a spec without one uses
    [groups]       NAME = ["glob", ...]   (globs match jar file names; '@NAME' in a list names a group)
    [variants.V]   keep = [...] (absent: every non-client jar of the source), add = [...], drop = [...]

    qmodset.py list                         manifests and their variants
    qmodset.py show SPEC                    the resolved manifest
    qmodset.py globs NAME GROUP|VARIANT     the globs of a group or a variant's keep list, one per line (port_chain.sh)
    qmodset.py build OUT LEVEL A|B SPEC [prop=value ...]   build OUT/srv (the driver does this for @@modset steps)
    qmodset.py switch OUT SPEC              replace srv/mods (and kubejs / config / donor links by policy) of an
                                            existing OUT, world kept (what `@@prep boot=N: modset SPEC` does)
Writes OUT/modset.json (spec, source, the jars taken from the source); qpin.py reads it for unlisted_ok.
Refuses while a game JVM is up (prep_srv.sh and scratch_server.sh setup are not run over a live server).
"""
import fnmatch
import json
import os
import re
import shlex
import shutil
import subprocess
import sys
from pathlib import Path

try:
	import tomllib
except ImportError:  # Python 3.10
	import tomli as tomllib

HERE = Path(__file__).resolve().parent
INST = HERE.parent.parent
DIR = Path(os.environ.get('QMODSET_DIR', HERE / 'modsets'))
PREP = Path(os.environ.get('QDRIVE_PREP', HERE / 'prep_srv.sh'))
SETUP = Path(os.environ.get('QMODSET_SETUP', INST / 'tools/creative_labels_test/scratch_server.sh'))
PORT = Path(os.environ.get('QMODSET_PORT', INST.parent / 'CEPortTesting'))
DEPLOY = Path(os.environ.get('QMODSET_DEPLOY', INST / 'deploy/latest'))
FIELDS = ('description', 'extends', 'source', 'swap', 'group', 'kubejs', 'config', 'release_commit', 'upstream',
		  'noupstream', 'copy', 'forbid', 'require', 'unlisted_ok', 'dev_keep', 'default', 'groups', 'variants')
OPTIONS = ('kubejs', 'config', 'upstream', 'noupstream')
POLICY = {'source': ('pack', 'ceporttesting', 'deploy'), 'kubejs': ('pack', 'source', 'probes', 'release'),
		  'config': ('pack', 'source', 'empty', 'release'), 'upstream': ('pack', 'source', 'none')}


def manifests():
	return sorted(p.stem for p in DIR.glob('*.toml'))


def load(name, seen=()):
	"""Manifest NAME with its `extends` chain merged (groups and variants merged by key). ValueError when bad."""
	p = DIR / (name + '.toml')
	if not p.exists():
		raise ValueError('no modset %s (%s; known: %s)' % (name, p, ', '.join(manifests())))
	m = tomllib.loads(p.read_text())
	bad = sorted(set(m) - set(FIELDS))
	if bad:
		raise ValueError('%s: unknown field(s) %s' % (p.name, ', '.join(bad)))
	if m.get('extends'):
		if m['extends'] in seen:
			raise ValueError('%s: extends loop' % p.name)
		base = load(m['extends'], seen + (name,))
		for k in ('groups', 'variants'):
			m[k] = dict(base.get(k, {}), **m.get(k, {}))
		m = dict(base, **m)
	for k, ok in POLICY.items():
		if k in m and m[k] not in ok:
			raise ValueError('%s: %s = %r (one of %s)' % (p.name, k, m[k], ', '.join(ok)))
	return m


def parse_spec(text):
	"""SPEC -> {'name', 'variant', 'add', 'drop', 'ungroup', 'set', 'text'}. ValueError when bad."""
	words = shlex.split(text)
	if not words:
		raise ValueError('empty modset spec')
	spec = {'name': words[0], 'variant': None, 'add': [], 'drop': [], 'ungroup': [], 'set': {}, 'text': text.strip()}
	for w in words[1:]:
		if w.startswith('+') and len(w) > 1:
			spec['add'].append(w[1:])
		elif w.startswith('-@') and len(w) > 2:
			spec['ungroup'].append(w[2:])
		elif w.startswith('-') and len(w) > 1:
			spec['drop'].append(w[1:])
		elif '=' in w:
			k, _, v = w.partition('=')
			if k not in OPTIONS:
				raise ValueError('modset option %s (known: kubejs, config, upstream, noupstream)' % k)
			if k in POLICY and v not in POLICY[k]:
				raise ValueError('modset %s=%s (one of %s)' % (k, v, ', '.join(POLICY[k])))
			spec['set'][k] = v.split(',') if k == 'noupstream' else v
		elif spec['variant'] is None:
			spec['variant'] = w
		else:
			raise ValueError('modset spec: a second variant %r' % w)
	if spec['name'] == 'pack':
		if spec['variant'] or spec['add'] or spec['drop'] or spec['ungroup'] or spec['set']:
			raise ValueError('modset pack takes nothing else')
		return spec
	resolve(spec)
	return spec


def resolve(spec):
	"""The manifest of a parsed spec, with the variant's keep / add / drop and the spec's changes applied:
	{'m': manifest, 'keep': [globs] or None, 'add': [globs], 'drop': [globs]}."""
	m = load(spec['name'])
	v = spec['variant'] or m.get('default')
	vars_ = m.get('variants', {})
	if v is None and vars_:
		raise ValueError('modset %s: name a variant (%s)' % (spec['name'], ', '.join(sorted(vars_))))
	if v is not None and v not in vars_:
		raise ValueError('modset %s: no variant %s (%s)' % (spec['name'], v, ', '.join(sorted(vars_)) or 'none'))
	var = vars_.get(v, {})
	groups = m.get('groups', {})
	for g in spec['ungroup']:
		if g not in groups:
			raise ValueError('modset %s: no group %s' % (spec['name'], g))

	def expand(items, skip=()):
		out = []
		for x in items:
			if x.startswith('@'):
				if x[1:] not in groups:
					raise ValueError('modset %s: no group %s' % (spec['name'], x[1:]))
				if x[1:] not in skip:
					out += expand(groups[x[1:]], skip)
			else:
				out.append(x)
		return out
	keep = expand(var['keep'], spec['ungroup']) if 'keep' in var else None
	add = expand(var.get('add', []), spec['ungroup']) + expand(spec['add'])
	drop = expand(var.get('drop', [])) + expand(spec['drop'])
	m = dict(m)
	for k, val in spec['set'].items():
		m[k] = val
	spec['variant'] = v
	return {'m': m, 'keep': keep, 'add': add, 'drop': drop, 'require': expand(m.get('require', []))}


def _match(name, globs):
	return any(fnmatch.fnmatchcase(name, g) for g in globs)


def client_jars():
	out = set()
	for l in (INST / 'mod_sides.md').read_text(errors='replace').split('\n'):
		if l.startswith('| client |'):
			t = l.split('`')
			if len(t) >= 4:
				out.add(t[3].strip())
	return out


def source_dirs(m):
	src = m.get('source', 'pack')
	if src == 'ceporttesting':
		return [PORT / 'mods', PORT / 'disabledmods/cba_parked']
	if src == 'deploy':
		return [DEPLOY / 'mods', INST / 'mods']
	return [INST / 'mods']


def source_root(m):
	return PORT if m.get('source') == 'ceporttesting' else INST


def newest(dirs, glob):
	for d in dirs:
		hits = [p for p in d.glob('*.jar') if fnmatch.fnmatchcase(p.name, glob)] if d.is_dir() else []
		if hits:
			return max(hits, key=lambda p: p.stat().st_mtime)
	return None


def link(src, mods):
	dst = mods / src.name
	if dst.exists() or dst.is_symlink():
		dst.unlink()   # unlink first: never write through a hard link into the pack's jar (r1106cbb)
	try:
		os.link(src, dst)
	except OSError:
		shutil.copy2(src, dst)


def setprop(srv, k, v):
	sys.path.insert(0, str(HERE))
	import qdrive
	qdrive.setprop(srv, k, v)


def game_jvms():
	if os.environ.get('QDRIVE_NO_JVM_CHECK'):
		return ''
	return subprocess.run(['bash', str(HERE / 'game_jvms.sh')], capture_output=True, text=True).stdout.strip()


def _run(cmd, env=None, cwd=None):
	r = subprocess.run(cmd, capture_output=True, text=True, env=dict(os.environ, **(env or {})), cwd=cwd,
					   stdin=subprocess.DEVNULL)
	return r.returncode, r.stdout + r.stderr


def build(out, level, group, spec_text, props=(), env=None):
	"""Build OUT/srv from SPEC (module docstring). Returns (returncode, log text)."""
	if game_jvms():
		return 1, 'a game JVM is up: not building over it\n'
	spec = parse_spec(spec_text)
	if spec['name'] == 'pack':
		rc, text = _run(['bash', str(PREP), str(out), level, group] + list(props), env)
		return rc, 'modset pack: prep_srv.sh\n' + text
	r = resolve(spec)
	m = r['m']
	src = m.get('source', 'pack')
	g = m.get('group') or group
	log = ['modset %s (variant %s, source %s, group %s)' % (spec['text'], spec['variant'], src, g)]
	out = Path(out)
	srv = out / 'srv'
	if src in ('pack', 'deploy'):
		e = dict(env or {})
		if src == 'deploy' or m.get('swap') is False:
			e['NOSWAP'] = '1'
		rc, text = _run(['bash', str(PREP), str(out), level, g] + list(props), e)
		log.append(text.rstrip())
		if rc:
			return rc, '\n'.join(log) + '\n'
	else:
		rc, text = _run(['bash', str(SETUP), 'setup', str(out), level])
		log.append(text.rstrip())
		if rc:
			return rc, '\n'.join(log) + '\n'
		for j in (srv / 'mods').glob('*.jar'):
			j.unlink()
		if g == 'B':
			for k, v in (('level-type', 'minecraft\\:normal'), ('generate-structures', 'true'), ('level-seed', '8675309')):
				setprop(srv, k, v)
		for kv in props:
			k, _, v = kv.partition('=')
			setprop(srv, k, v)
	try:
		log += pick(out, m, r, src, spec['text'])
		log += trees(out, m)
	except RuntimeError as e:
		log.append('ERROR: %s' % e)
		return 1, '\n'.join(log) + '\n'
	rc, text = _run([sys.executable, str(HERE / 'qpin.py'), 'pin', str(out)])
	log.append(text.rstrip())
	log.append('modset done: %d jars, level %s, group %s' % (len(list((srv / 'mods').glob('*.jar'))), level, g))
	return rc, '\n'.join(log) + '\n'


def pick(out, m, r, src, text=''):
	"""The jar pick of a build: keep / add / drop / forbid, deploy overlay; writes OUT/modset.json."""
	srv = Path(out) / 'srv'
	mods = srv / 'mods'
	log, from_source = [], []
	if src == 'ceporttesting':
		clients = client_jars()
		seen = set()
		for d in source_dirs(m):
			for j in sorted(d.glob('*.jar')) if d.is_dir() else []:
				if j.name in seen:
					continue
				if (r['keep'] is None and j.name not in clients) or (r['keep'] is not None and _match(j.name, r['keep'])):
					link(j, mods)
					seen.add(j.name)
					from_source.append(j.name)
	elif src == 'deploy':
		for j in sorted((DEPLOY / 'mods').glob('*.jar')):
			stem = re.sub(r'-[0-9].*$', '', j.name)
			for old in mods.glob('*.jar'):
				if re.sub(r'-[0-9].*$', '', old.name) == stem:
					old.unlink()
			link(j, mods)
			from_source.append(j.name)
			log.append('release jar: %s' % j.name)
		listed = DEPLOY / 'verify/server.installed.sha1'
		if listed.exists():
			names = {l.split('/')[-1].strip() for l in listed.read_text().split('\n') if l.strip()}
			dev = m.get('dev_keep', ['cdevperfbot-*.jar'])
			for j in sorted(mods.glob('*.jar')):
				if j.name in names:
					continue
				if _match(j.name, dev):
					log.append('dev jar kept: %s' % j.name)   # prep_srv.sh's perfBot (r1118 follow-up)
				else:
					j.unlink()
					log.append('not in the release: %s' % j.name)
		if r['keep'] is not None:
			for j in sorted(mods.glob('*.jar')):
				if not _match(j.name, r['keep']):
					j.unlink()
	else:
		if r['keep'] is not None:
			n = 0
			for j in sorted(mods.glob('*.jar')):
				if not _match(j.name, r['keep']):
					j.unlink()
					n += 1
			log.append('pruned %d jars to the keep list' % n)
	for gl in r['add']:
		hit = newest(source_dirs(m) + [INST / 'mods'], gl)
		if hit is None:
			raise RuntimeError('add: no jar for %r in %s or the pack' % (gl, ', '.join(str(d) for d in source_dirs(m))))
		link(hit, mods)
		if hit.parent != INST / 'mods':
			from_source.append(hit.name)
		log.append('added: %s' % hit.name)
	for gl in r['drop']:
		for j in sorted(mods.glob('*.jar')):
			if fnmatch.fnmatchcase(j.name, gl):
				j.unlink()
				log.append('dropped: %s' % j.name)
	missing = [gl for gl in r.get('require', []) if not any(fnmatch.fnmatchcase(j.name, gl) for j in mods.glob('*.jar'))]
	if missing:
		raise RuntimeError('required jar(s) missing after the pick: %s (source %s)' % (', '.join(missing), src))
	for rx in m.get('forbid', []):
		bad = [j.name for j in mods.glob('*.jar') if re.search(rx, j.name)]
		if bad:
			raise RuntimeError('forbidden jar(s) in srv/mods (%s): %s' % (rx, ', '.join(sorted(bad))))
	doc = {'spec': text, 'source': src,
		   'unlisted_ok': bool(m.get('unlisted_ok')), 'from_source': sorted(set(from_source))}
	(Path(out) / 'modset.json').write_text(json.dumps(doc, indent=1))
	return log


def _archive(srv, commit, paths):
	p1 = subprocess.Popen(['git', '-C', str(INST), 'archive', commit] + paths, stdout=subprocess.PIPE)
	r = subprocess.run(['tar', '-x'], stdin=p1.stdout, cwd=srv)
	p1.wait()
	if p1.returncode or r.returncode:
		raise RuntimeError('git archive %s %s failed' % (commit, ' '.join(paths)))


def trees(out, m):
	"""kubejs / config / donor folder policies and the copy list."""
	srv = Path(out) / 'srv'
	root = source_root(m)
	log = []
	commit = m.get('release_commit', '40ed32be')
	kj = m.get('kubejs', 'pack')
	if kj in ('source', 'release', 'probes'):
		keep = srv.parent / '.modset_keep'
		shutil.rmtree(keep, ignore_errors=True)
		keep.mkdir()
		for f in (srv / 'kubejs/server_scripts').glob('zz_*_probe.js'):
			shutil.copy2(f, keep / f.name)
		if (srv / 'kubejs/data/zq').is_dir():
			shutil.copytree(srv / 'kubejs/data/zq', keep / 'zq')
		shutil.rmtree(srv / 'kubejs', ignore_errors=True)
		if kj == 'source':
			shutil.copytree(root / 'kubejs', srv / 'kubejs')
		elif kj == 'release':
			_archive(srv, commit, ['kubejs'])
		(srv / 'kubejs/server_scripts').mkdir(parents=True, exist_ok=True)
		for f in keep.glob('zz_*_probe.js'):
			shutil.copy2(f, srv / 'kubejs/server_scripts' / f.name)
		if (keep / 'zq').is_dir():
			shutil.copytree(keep / 'zq', srv / 'kubejs/data/zq', dirs_exist_ok=True)
		shutil.rmtree(keep, ignore_errors=True)
		log.append('kubejs: %s' % kj)
	if not any(j.name.lower().startswith('creativelabels-') for j in (srv / 'mods').glob('*.jar')):
		# the Creative Labels probe (scratch_server.sh setup) loads Labels classes: a KubeJS error without Labels (r1114)
		(srv / 'kubejs/server_scripts/zz_cl_probe.js').unlink(missing_ok=True)
	cf = m.get('config', 'pack')
	if cf != 'pack':
		for d in ('config', 'defaultconfigs'):
			shutil.rmtree(srv / d, ignore_errors=True)
		if cf == 'source':
			shutil.copytree(root / 'config', srv / 'config')
			(srv / 'defaultconfigs').mkdir(exist_ok=True)
		elif cf == 'release':
			_archive(srv, commit, ['config', 'defaultconfigs'])
		else:
			(srv / 'config').mkdir()
			(srv / 'defaultconfigs').mkdir()
		log.append('config: %s' % cf)
	up = m.get('upstream', 'pack')
	if up != 'pack' or m.get('noupstream'):
		for l in srv.glob('*_upstream'):
			if l.is_symlink():
				l.unlink()
		if up != 'none':
			for u in sorted((root if up == 'source' else INST).glob('*_upstream')):
				if u.name in m.get('noupstream', []):
					log.append('donor folder left out: %s' % u.name)
					continue
				(srv / u.name).symlink_to(u)
		log.append('donor folders: %s' % up)
	for a, b in m.get('copy', []):
		s, d = INST / a, srv / b
		if not s.exists():
			raise RuntimeError('copy: no %s' % a)
		d.parent.mkdir(parents=True, exist_ok=True)
		(shutil.copytree(s, d, dirs_exist_ok=True) if s.is_dir() else shutil.copy2(s, d))
		log.append('copy: %s -> %s' % (a, b))
	return log


def switch(out, spec_text, env=None):
	"""`@@prep boot=N: modset SPEC`: build SPEC into OUT/.modset_switch, then move its mods/ (and kubejs / config /
	donor links when the manifest sets them, all of them for `pack`) and pinned.json into OUT, keeping the world and
	server.properties. Returns log lines; RuntimeError on a failure."""
	out = Path(out)
	tmp = out / '.modset_switch'
	shutil.rmtree(tmp, ignore_errors=True)
	rc, text = build(tmp, 'switch', 'A', spec_text, (), env)
	if rc:
		raise RuntimeError('modset switch build failed: %s' % text.strip()[-400:])
	spec = parse_spec(spec_text)
	m = {} if spec['name'] == 'pack' else resolve(spec)['m']
	srv, new = out / 'srv', tmp / 'srv'
	moved = ['mods']
	if spec['name'] == 'pack' or m.get('kubejs', 'pack') != 'pack':
		moved.append('kubejs')
	if spec['name'] == 'pack' or m.get('config', 'pack') != 'pack':
		moved += ['config', 'defaultconfigs']
	for d in moved:
		shutil.rmtree(srv / d, ignore_errors=True)
		if (new / d).exists():
			shutil.move(str(new / d), str(srv / d))
	for l in srv.glob('*_upstream'):
		if l.is_symlink():
			l.unlink()
	for l in new.glob('*_upstream'):
		if l.is_symlink():
			(srv / l.name).symlink_to(os.readlink(l))
	for f in ('pinned.json', 'modset.json'):
		(out / f).unlink(missing_ok=True)
		if (tmp / f).exists():
			shutil.move(str(tmp / f), str(out / f))
	shutil.rmtree(tmp, ignore_errors=True)
	return ['modset switch %s: %s replaced (%d jars), world kept' % (spec_text, ', '.join(moved),
																	   len(list((srv / 'mods').glob('*.jar'))))]


def main(argv):
	if not argv:
		print(__doc__)
		return 2
	cmd = argv[0]
	try:
		if cmd == 'list':
			for n in manifests():
				m = load(n)
				print('%-12s %-55s variants: %s' % (n, m.get('description', '')[:55], ', '.join(sorted(m.get('variants', {})))
												  or '-'))
			return 0
		if cmd == 'show':
			spec = parse_spec(' '.join(shlex.quote(a) for a in argv[1:]))
			r = resolve(spec)
			print(json.dumps({'spec': spec, 'keep': r['keep'], 'add': r['add'], 'drop': r['drop'],
							  'manifest': {k: v for k, v in r['m'].items() if k not in ('groups', 'variants')}}, indent=1))
			return 0
		if cmd == 'globs':
			m = load(argv[1])
			key = argv[2]
			if key in m.get('groups', {}):
				print('\n'.join(m['groups'][key]))
			else:
				r = resolve(parse_spec('%s %s' % (argv[1], key)))
				print('\n'.join(r['keep'] or []))
			return 0
		if cmd == 'build':
			out, level, group = argv[1:4]
			rest = argv[4:]
			props = [a for a in rest if re.match(r'[a-z][a-z0-9.-]*=', a) and a.partition('=')[0] not in OPTIONS]
			spec = ' '.join(shlex.quote(a) for a in rest if a not in props)
			rc, text = build(out, level, group, spec, props)
			print(text, end='')
			return rc
		if cmd == 'switch':
			print('\n'.join(switch(argv[1], ' '.join(shlex.quote(a) for a in argv[2:]))))
			return 0
	except (ValueError, RuntimeError) as e:
		print('qmodset: %s' % e, file=sys.stderr)
		return 1
	print(__doc__)
	return 2


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
