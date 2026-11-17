#!/usr/bin/env python3
"""Pinned jars for a scratch run (plan_process_overhaul.md P2.4 item 2).

A run tests the jars it was prepared with, and records which ones. prep_srv.sh calls `pin` after its swaps:
  - every own jar in OUT/srv/mods becomes a real copy (a symlink or hard link is replaced by a copy), so a parallel
    session's installToMods or Gradle build cannot change it under the run (scratch-test gotchas, 10-21 to 11-01);
  - OUT/pinned.json records each own jar's identity: its build stamp, or sha1 for a jar built before stamping.
`qrun.py run` calls verify before the first row and refuses to start when:
  - OUT/pinned.json is missing (the server was not prepared by prep_srv.sh / pin);
  - an own jar is a link again, or its identity differs from pinned.json (swapped after prep);
  - a jar in srv/mods has no mod_sides.md row (another session's work in progress; extras below are allowed).
Every verdict file then carries `stamps` (pinned identities), which qplan.py compares with mods/ to judge a row current.

    qpin.py pin OUT            copy-pin the own jars and write OUT/pinned.json
    qpin.py verify OUT         print the problems; exit 1 when there are any
    qpin.py show OUT           the pinned identities
"""
import hashlib
import json
import os
import re
import shutil
import sys
import time
from functools import lru_cache
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
import jarstamp  # noqa: E402

ROOT = HERE.parent.parent
MOD_SIDES = ROOT / 'mod_sides.md'
# jars prep_srv.sh adds on purpose that have no mod_sides.md row (dev-only)
EXTRAS = (re.compile(r'cdevperfbot-.*\.jar$'), re.compile(r'backportedPowerGrid-.*\.jar$'))


@lru_cache(maxsize=1)
def listed():
    """(jar names, mod ids) from mod_sides.md."""
    names, ids = set(), set()
    for l in MOD_SIDES.read_text(errors='replace').split('\n'):
        if not re.match(r'^\| (both|server|client|[a-z-]+) \|', l):
            continue
        ticks = l.split('`')
        if len(ticks) >= 4:
            ids.add(ticks[1].strip().lower())
            names.add(ticks[3].strip())
    return names, ids


@lru_cache(maxsize=1)
def own_prefixes():
    out = set()
    for gp in jarstamp.DEV.glob('*/gradle.properties'):
        p = jarstamp._props(gp)
        if 'mod_id' in p:
            out.add(p.get('jar_name', p['mod_id']))
    return out


def own_info(path):
    """(key, identity) for an own jar, else None. key: mod id (lowercase); identity: stamp, or sha1:<12> unstamped."""
    path = Path(path)
    own, _members = jarstamp.stamp_info(path)
    if own and own.get('stamp'):
        return (own.get('mod_id') or path.name).lower(), own['stamp']
    stem = re.sub(r'-[0-9][^-]*\.jar$', '', path.name)
    if stem in own_prefixes():
        h = hashlib.sha1()
        with open(path, 'rb') as f:
            for chunk in iter(lambda: f.read(1 << 20), b''):
                h.update(chunk)
        return stem.lower(), 'sha1:' + h.hexdigest()[:12]
    return None


def linked(p):
    return os.path.islink(p) or os.stat(p).st_nlink > 1


def pin(out):
    mods = Path(out) / 'srv' / 'mods'
    pinned = {}
    copied = 0
    for j in sorted(mods.glob('*.jar')):
        info = own_info(j)
        if not info:
            continue
        if linked(j):
            src = os.path.realpath(j)
            tmp = j.with_name(j.name + '.pin')
            shutil.copy2(src, tmp)
            os.replace(tmp, j)
            copied += 1
        pinned[info[0]] = {'jar': j.name, 'id': info[1]}
    doc = {'pinned_at': time.strftime('%Y-%m-%d %H:%M:%S'), 'jars': pinned}
    (Path(out) / 'pinned.json').write_text(json.dumps(doc, indent=1))
    print('pin: %d own jars (%d link(s) replaced by copies) -> %s' % (len(pinned), copied, Path(out) / 'pinned.json'))
    return doc


def load(out):
    try:
        return json.loads((Path(out) / 'pinned.json').read_text())
    except (OSError, ValueError):
        return None


def verify(out):
    """[problem text]; empty when the run may start."""
    mods = Path(out) / 'srv' / 'mods'
    doc = load(out)
    probs = []
    if doc is None:
        return ['%s/pinned.json missing: prepare the server with prep_srv.sh (or run qpin.py pin OUT)' % out]
    names, ids = listed()
    seen = {}
    for j in sorted(mods.glob('*.jar')):
        info = own_info(j)
        if info:
            seen[info[0]] = (j, info[1])
            if linked(j):
                probs.append('%s is a link, not a copy (pin it: qpin.py pin OUT)' % j.name)
        if j.name not in names and not (info and info[0] in ids) and not any(x.search(j.name) for x in EXTRAS):
            probs.append('%s has no mod_sides.md row (another session\'s jar?); remove it or add the row' % j.name)
    for key, p in doc['jars'].items():
        if key not in seen:
            probs.append('%s (%s) pinned but gone from srv/mods' % (key, p['jar']))
        elif seen[key][1] != p['id']:
            probs.append('%s changed since prep: pinned %s, now %s (%s)' % (key, p['id'], seen[key][1],
                                                                        seen[key][0].name))
    for key in sorted(set(seen) - set(doc['jars'])):
        probs.append('%s (%s) is an own jar added after prep (not pinned)' % (key, seen[key][0].name))
    return probs


def stamps(out):
    doc = load(out)
    return {k: v['id'] for k, v in doc['jars'].items()} if doc else None


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    cmd, out = argv[0], argv[1]
    if cmd == 'pin':
        pin(out)
        return 0
    if cmd == 'verify':
        probs = verify(out)
        for p in probs:
            print('REFUSE ' + p)
        print('qpin: %s' % ('%d problem(s)' % len(probs) if probs else 'OK'))
        return 1 if probs else 0
    if cmd == 'show':
        print(json.dumps(load(out), indent=1))
        return 0
    print(__doc__)
    return 2


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
