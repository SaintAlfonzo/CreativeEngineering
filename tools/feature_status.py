#!/usr/bin/env python3
"""Derived feature status (plan_process_overhaul.md P3.3 item 3). The status word of a features.csv row is computed;
`override` (with `override_reason` and `override_date`) replaces it by hand. Rules, first match wins:

  override   the `override` cell, when set
  reference  no jars and no linked TODO rows (audits, surveys, worksheets)
  BROKEN     a linked open Bug row marked as a blocker (URGENT, blocker, High priority), or a FAIL verdict whose pinned
             stamp of one of the feature's jars is the stamp installed in mods/ now
  VERIFIED   linked TODO rows exist and every one is Complete
  DEPLOYED   every real jar of the feature is in the last deploy: same stamp, or, for a deploy recorded before stamps
             (before 2026-11-16), the same content digest as the jar in deploy/latest/mods/
  planned    every linked row is Planned or Pinned (nothing built)
  BUILT      anything else

Jars: mod ids of features.csv `jars`. `kubejs`, `datapack` and `tools` are code locations without a stamp: they never
make a feature DEPLOYED on their own. Linked TODO rows: the rows whose `feature` field is the feature's key (P3.5).

Library: derive(feats, todo) -> {key: (word, why)}. CLI: python3 tools/feature_status.py [KEY ...] prints the
derivation for the named features (all when none).
"""
import json
import os
import re
import sys
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import jarstamp  # noqa: E402

PSEUDO_JARS = {'kubejs', 'datapack', 'tools'}
DEPLOY = ROOT / 'deploy'
VERDICT_ROOT = ROOT / 'perf' / 'runs'
BLOCKER_RE = re.compile(r'URGENT|\bblocker\b|High priority', re.I)


def split_list(s):
	return [x.strip() for x in (s or '').split(',') if x.strip()]


@lru_cache(maxsize=None)
def installed():
	"""{mod id (lowercase): (jar Path, stamp or None)} of the own projects' jars in mods/."""
	out = {}
	for gp in sorted(jarstamp.DEV.glob('*/gradle.properties')):
		p = jarstamp._props(gp)
		if 'mod_id' not in p or p.get('bundled_in'):
			continue
		j = jarstamp.installed_by_name(p.get('jar_name', p['mod_id']))
		if j:
			out[p['mod_id'].lower()] = (j, jarstamp.stamp_of(j))
	return out


@lru_cache(maxsize=None)
def last_deploy():
	"""{mod id: {'stamp', 'digest'}} of the last server snapshot's own jars; the digest is read from
    deploy/latest/mods when the snapshot recorded none and the payload is that version's."""
	try:
		ver = json.load(open(DEPLOY / 'DEPLOYED.json'))['server']['version']
		snap = json.load(open(DEPLOY / 'history' / ('%s.json' % ver)))
	except (OSError, ValueError, KeyError):
		return {}
	readme = DEPLOY / 'latest' / 'README.md'
	payload_is_snap = readme.exists() and (' %s ' % ver) in readme.read_text(errors='replace').split('\n', 1)[0] + ' '
	out = {}
	for jar, m in snap.get('mods', {}).items():
		mid = (m.get('modId') or '').lower()
		if not mid:
			continue
		d = {'stamp': m.get('stamp'), 'digest': m.get('digest')}
		p = DEPLOY / 'latest' / 'mods' / jar
		if not d['digest'] and payload_is_snap and p.exists():
			d['digest'] = jarstamp.content_digest(p)
		out[mid] = d
	return out


@lru_cache(maxsize=None)
def installed_digest(mid):
	p = installed().get(mid, (None,))[0]
	return jarstamp.content_digest(p) if p else None


def jar_deployed(mid):
	dep = last_deploy().get(mid)
	inst = installed().get(mid)
	if not dep or not inst:
		return False
	if dep.get('stamp') and dep['stamp'] == inst[1]:
		return True
	return bool(dep.get('digest')) and dep['digest'] == installed_digest(mid)


@lru_cache(maxsize=None)
def failing_stamps():
	"""{queue code: {(mod id, stamp)}} of FAIL verdicts that pinned stamps."""
	out = {}
	for dirpath, _d, files in os.walk(VERDICT_ROOT):
		if os.path.basename(dirpath) != 'verdicts':
			continue
		for fn in files:
			if not fn.endswith('.json'):
				continue
			try:
				v = json.load(open(os.path.join(dirpath, fn)))
			except (OSError, ValueError):
				continue
			if (v.get('verdict') or v.get('result') or '').upper() != 'FAIL' or not isinstance(v.get('stamps'), dict):
				continue
			row = v.get('id') or fn[:-5]
			m = re.match(r'((?:SQ|CQ)-[A-Z0-9]+)', row)
			if m:
				out.setdefault(m.group(1), set()).update((k.lower(), s) for k, s in v['stamps'].items())
	return out


def derive_one(f, todo_status, todo_text, linked=()):
	if f.get('override', '').strip():
		return f['override'].strip(), 'override: %s' % (f.get('override_reason') or 'no reason given')
	jars = [j.lower() for j in split_list(f.get('jars'))]
	real = [j for j in jars if j not in PSEUDO_JARS]
	known = [r for r in linked if r in todo_status]
	if not jars and not known:
		return 'reference', 'no jars, no TODO rows'
	for r in known:
		if todo_status[r] == 'Bug' and BLOCKER_RE.search(todo_text.get(r, '')):
			return 'BROKEN', 'blocker Bug row %s' % r
	inst = installed()
	for code in split_list(f.get('queue codes')):
		for mid, st in failing_stamps().get(code, ()):
			if mid in real and inst.get(mid, (None, None))[1] == st:
				return 'BROKEN', 'FAIL in %s on the installed %s' % (code, mid)
	sts = [todo_status[r] for r in known]
	if sts and all(s == 'Complete' for s in sts):
		return 'VERIFIED', 'every linked row Complete'
	if real and all(jar_deployed(j) for j in real):
		return 'DEPLOYED', 'jars in the last deploy: %s' % ', '.join(real)
	if sts and all(s in ('Planned', 'Pinned') for s in sts):
		return 'planned', 'linked rows Planned/Pinned only'
	off = [j for j in real if not jar_deployed(j)]
	return 'BUILT', ('not in the last deploy: %s' % ', '.join(off)) if off else 'code location without a stamp'


def derive(feats, todo):
	"""todo: status_index.read_todo() result (open rows with status and text; archive ids are Complete)."""
	status = {r: 'Complete' for r in todo['archive']}
	text = {}
	for r in todo['open']:
		status[r['id']] = r['status']
		text[r['id']] = r.get('text', '')
	linked = {}
	for r in todo.get('rows', []):
		if r.get('feature'):
			linked.setdefault(r['feature'], []).append(r['id'])
	return {f['key']: derive_one(f, status, text, linked.get(f['key'], ())) for f in feats}


def main(argv):
	import status_index as si
	feats = si.read_features()
	res = derive(feats, si.read_todo())
	for f in feats:
		if not argv or f['key'] in argv:
			w, why = res[f['key']]
			print('%-10s %-45s %s' % (w, f['key'][:45], why))
	return 0


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
