#!/usr/bin/env python3
"""Decompile a 1.19.2 Forge mod jar into readable Java (SRG names -> Mojang names).

    python3 tools/decompile_mod.py mods/letsdo-vinery-forge-1.3.10.jar /tmp/vinery_src

Runs Vineflower on the jar, then rewrites every m_NNNN_ / f_NNNN_ in the output .java files to its
official name using ForgeGradle's cached srg_to_official mapping. Used to read mod behaviour
(timings, slot rules, growth checks) for the feature writeups, e.g. feature_writeup/vinery.md.
Class names in a reobfuscated mod jar are already official, so only members need renaming.
"""
import glob
import os
import re
import subprocess
import sys

VINEFLOWER = os.path.expanduser('~/.cache/ce-tools/vineflower-1.10.1.jar')
MAPPING_GLOB = os.path.expanduser(
    '~/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.19.2-*/srg_to_official_1.19.2.tsrg')
SRG = re.compile(r'\b[mf]_\d+_\b')


def load_mapping():
    paths = sorted(glob.glob(MAPPING_GLOB))
    if not paths:
        sys.exit('no srg_to_official_1.19.2.tsrg under ~/.gradle (build patcherMod once to populate it)')
    names = {}
    with open(paths[-1]) as fh:
        for line in fh:
            if not line.startswith('\t') or line.startswith('\t\t'):
                continue
            parts = line.split()
            # field: "\tf_1_ name"   method: "\tm_1_ (desc)V name"
            if len(parts) >= 2 and SRG.fullmatch(parts[0]):
                names[parts[0]] = parts[-1]
    return names


def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    jar, out = sys.argv[1], sys.argv[2]
    if not os.path.isfile(VINEFLOWER):
        sys.exit('missing ' + VINEFLOWER)
    subprocess.run(['java', '-jar', VINEFLOWER, '-log=ERROR', jar, out], check=True,
                   stdout=subprocess.DEVNULL)
    names = load_mapping()
    count = 0
    for root, _, files in os.walk(out):
        for name in files:
            if name.endswith('.java'):
                path = os.path.join(root, name)
                with open(path) as fh:
                    src = fh.read()
                with open(path, 'w') as fh:
                    fh.write(SRG.sub(lambda m: names.get(m.group(0), m.group(0)), src))
                count += 1
    print('%d java files in %s, %d names mapped' % (count, out, len(names)))


if __name__ == '__main__':
    main()
