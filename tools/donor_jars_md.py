#!/usr/bin/env python3
"""Prints the donor-jar download list (folder, file, sha1, source) as a Markdown table, read from every
UpstreamJars.java under development/ (the same `new Jar("file", "sha1", "source")` lines tools/build_deploy.py reads).
For the README and the Creative Brewing / Creative Delight project pages (creative_brewing.md 9.5, R2).
Usage: tools/donor_jars_md.py [--mods creativebrewing,creativedelight]   (default: every jar)"""
import glob, os, re, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
# project folder -> mod id of the jar a player installs (the folder's donor is needed only with that jar)
OWNER = {'creativeBrewing': 'creativebrewing', 'creativeCompat': 'creativecompat', 'creativeDelight': 'creativedelight', 'creativeWorldgen': 'creativeworldgen'}
JAR = re.compile(r'new Jar\(\s*"([^"]+)",\s*"([0-9a-f]{40})"(?:,\s*"((?:[^"\\]|\\.)*)")?', re.S)


def rows():
    out = []
    for f in sorted(glob.glob(os.path.join(ROOT, 'development', '**', 'UpstreamJars.java'), recursive=True)):
        rel = os.path.relpath(f, ROOT)
        if 'moved_code_backup' in rel or os.sep + 'build' + os.sep in rel:
            continue
        s = open(f, encoding='utf-8').read()
        m = re.search(r'FOLDER\s*=\s*"([^"]+)"', s)
        folder = m.group(1) if m else 'farmers_upstream'  # Worldgen's donor names its folder elsewhere
        owner = OWNER.get(rel.split(os.sep)[1], rel.split(os.sep)[1])
        for j in JAR.finditer(s):
            out.append((owner, folder, j.group(1), j.group(2), (j.group(3) or '').strip()))
    return out


def main():
    want = None
    if '--mods' in sys.argv:
        want = set(sys.argv[sys.argv.index('--mods') + 1].split(','))
    print('| Needed by | Folder (in the game directory) | File | sha1 | Where to get it |')
    print('|---|---|---|---|---|')
    for owner, folder, name, sha1, src in rows():
        if want and owner not in want:
            continue
        print(f'| {owner} | `{folder}/` | `{name}` | `{sha1}` | {src.replace(chr(92) + chr(34), chr(34)).replace("|", "/")} |')


if __name__ == '__main__':
    main()
