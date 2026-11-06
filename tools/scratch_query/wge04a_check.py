#!/usr/bin/env python3
"""SQ-WGE-04a (run r1102b): the eight Mystic Rift pillager tower weights x3 by hand are still in structure_groups.json and
the estimates table lists the expected chunks per start.

    wge04a_check.py [KUBEJS_DIR]      default: kubejs (run from a scratch srv/ or the instance root)

Prints one line per tower (weight, frequency, tuned note end, estimate) and exits 1 on any mismatch. Pure reader.
"""
import json
import os
import re
import sys

WEIGHT = {'acacia': 30, 'birch': 24, 'cherry': 51, 'desert': 3, 'end': 3, 'jungle': 30, 'mangrove': 15, 'oak_wood': 15}
NEW_PER = {'acacia': 115, 'birch': 169, 'cherry': 137, 'desert': 355, 'jungle': 91, 'mangrove': 765, 'oak_wood': 190, 'end': 232}
NOTE = 'weight x3 by hand (user, 2026-11-01)'


def walk(x):
    if isinstance(x, dict):
        for k, v in x.items():
            yield k, v
            yield from walk(v)
    elif isinstance(x, list):
        for v in x:
            yield from walk(v)


def main():
    kj = sys.argv[1] if len(sys.argv) > 1 else 'kubejs'
    wg = os.path.join(kj, 'generation_scripts', 'worldgen')
    groups = json.load(open(os.path.join(wg, 'structure_groups.json')))
    md = open(os.path.join(wg, 'structure_group_estimates.md')).read()
    found = {k: v for k, v in walk(groups) if isinstance(v, dict) and k.startswith('mysticrift_pillagers_towers_aris:') and 'weight' in v}
    bad = 0
    for t, w in WEIGHT.items():
        k = 'mysticrift_pillagers_towers_aris:%s_pillager_tower' % t
        v = found.get(k, {})
        m = re.search(r'\| `' + re.escape(k) + r'` \| (\d+) \| ([\d.]+) \| \S+ \| (\d+) \|', md)
        ok = v.get('weight') == w and str(v.get('tuned', '')).endswith(NOTE) and m is not None and int(m.group(3)) == NEW_PER[t]
        bad += 0 if ok else 1
        print('%s %s weight %s frequency %s, note ends "%s", estimate 1 per %s'
              % ('ok  ' if ok else 'FAIL', k, v.get('weight'), v.get('frequency'), str(v.get('tuned', ''))[-38:], m and m.group(3)))
    sys.exit(1 if bad else 0)


if __name__ == '__main__':
    main()
