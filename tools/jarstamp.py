#!/usr/bin/env python3
"""Build stamps of the pack's own jars (plan_process_overhaul.md P1.2). Every jar built by build-logic carries
META-INF/ce-build.json and Implementation-Version <mod_version>+<sha>[.dirty-<hash6>].<yyMMddHHmm>; bundles stamp the
outer jar and each nested member.

Usage:
  python3 tools/jarstamp.py [FOLDER ...]        stamps of every own jar in the folders (default mods/), members indented
  python3 tools/jarstamp.py diff A B            compare two folders (mods/, a scratch srv/mods, a staged payload ...)
  python3 tools/jarstamp.py pending             built jars (development/*/build/libs) whose stamp is not in mods/
  python3 tools/jarstamp.py JAR                 one jar's stamp

Jars built before stamping show `unstamped`. diff exits 1 when anything differs; pending exits 1 when anything is
pending. Library use: stamp_of(path), folder_stamps(folder), shipped_builds().
"""
import io
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MODS = ROOT / 'mods'
DEV = ROOT / 'development'


def _read(z):
    try:
        return json.loads(z.read('META-INF/ce-build.json'))
    except KeyError:
        pass
    try:
        mf = z.read('META-INF/MANIFEST.MF').decode('utf-8', 'replace')
        m = re.search(r'^Implementation-Version: *(\S*\+\S+)', mf, re.M)
        if m:
            return {'stamp': m.group(1)}
    except KeyError:
        pass
    return None


def stamp_info(path_or_bytes):
    """(stamp dict or None, [(member name, stamp dict or None)]) of one jar."""
    src = io.BytesIO(path_or_bytes) if isinstance(path_or_bytes, bytes) else path_or_bytes
    try:
        with zipfile.ZipFile(src) as z:
            own = _read(z)
            members = []
            for n in sorted(z.namelist()):
                if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
                    members.append((n.rsplit('/', 1)[1], stamp_info(z.read(n))[0]))
            return own, members
    except (zipfile.BadZipFile, OSError):
        return None, []


def stamp_of(path):
    s = stamp_info(path)[0]
    return s['stamp'] if s else None


def folder_stamps(folder):
    """{key: (jar name, stamp or None, members)} for every jar in a folder. Key: mod_id when stamped, else the name
    without its version."""
    out = {}
    for j in sorted(Path(folder).glob('*.jar')):
        own, members = stamp_info(j)
        key = own.get('mod_id') if own and own.get('mod_id') else re.sub(r'-[0-9][^-]*\.jar$', '', j.name)
        out[key] = (j.name, own['stamp'] if own else None, members)
    return out


def _props(f):
    out = {}
    for line in f.read_text(errors='replace').splitlines():
        m = re.match(r'\s*([A-Za-z_][\w.]*)\s*=\s*(.*?)\s*$', line)
        if m:
            out[m.group(1)] = m.group(2)
    return out


def shipped_builds():
    """[(project folder, mod_id, jar_name, built jar Path)] for every own project that ships its own jar."""
    out = []
    for gp in sorted(DEV.glob('*/gradle.properties')):
        p = _props(gp)
        if 'mod_id' not in p or p.get('bundled_in'):
            continue
        jar_name = p.get('jar_name', p['mod_id'])
        jar = gp.parent / 'build' / 'libs' / ('%s-%s.jar' % (jar_name, p.get('mod_version', '')))
        if jar.exists():
            out.append((gp.parent.name, p['mod_id'], jar_name, jar))
    return out


def installed_by_name(jar_name):
    """The mods/ jar for a jar_name prefix (a version follows the prefix), or None."""
    for j in sorted(MODS.glob(jar_name + '-*.jar')):
        if re.fullmatch(re.escape(jar_name) + r'-[0-9][^/]*\.jar', j.name):
            return j
    return None


def cmd_list(folders):
    for folder in folders:
        f = Path(folder)
        if f.is_file():
            own, members = stamp_info(f)
            print('%s  %s' % (f.name, own['stamp'] if own else 'unstamped'))
            for n, s in members:
                print('    %s  %s' % (n, s['stamp'] if s else 'unstamped'))
            continue
        print('== %s' % folder)
        for key, (name, stamp, members) in folder_stamps(f).items():
            if stamp is None and not any(s for _, s in members):
                continue   # third-party jar
            print('%-40s %s' % (name, stamp or 'unstamped'))
            for n, s in members:
                print('    %-36s %s' % (n, s['stamp'] if s else 'unstamped'))
    return 0


def cmd_diff(a, b):
    sa, sb = folder_stamps(a), folder_stamps(b)
    own = {k for k, v in list(sa.items()) + list(sb.items()) if v[1] or any(s for _, s in v[2])}
    diffs = 0
    for k in sorted(own):
        va, vb = sa.get(k), sb.get(k)
        if va and vb and va[1] == vb[1]:
            continue
        diffs += 1
        print('%-28s %-50s %s' % (k, '%s %s' % (va[0], va[1] or 'unstamped') if va else '(absent)',
                                  '%s %s' % (vb[0], vb[1] or 'unstamped') if vb else '(absent)'))
    print('%d own jar(s) differ' % diffs)
    return 1 if diffs else 0


def pending():
    """[(project, built jar, built stamp, installed jar name or None, installed stamp)] not installed."""
    out = []
    for proj, mod_id, jar_name, jar in shipped_builds():
        built = stamp_of(jar)
        if built is None:
            continue
        inst = installed_by_name(jar_name)
        inst_stamp = stamp_of(inst) if inst else None
        if inst_stamp != built:
            out.append((proj, jar, built, inst.name if inst else None, inst_stamp))
    return out


def cmd_pending():
    rows = pending()
    for proj, jar, built, inst, inst_stamp in rows:
        print('%-22s built %-48s installed %s' % (proj, built, '%s %s' % (inst, inst_stamp or 'unstamped') if inst
                                                   else '(none)'))
    print('%d built, not installed' % len(rows))
    return 1 if rows else 0


def main(argv):
    if argv[:1] == ['diff']:
        if len(argv) != 3:
            sys.exit('usage: jarstamp.py diff A B')
        return cmd_diff(argv[1], argv[2])
    if argv[:1] == ['pending']:
        return cmd_pending()
    return cmd_list(argv or [str(MODS)])


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
