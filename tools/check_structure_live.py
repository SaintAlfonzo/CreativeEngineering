#!/usr/bin/env python3
"""Compare structure tuning against the live game (the /dumpstructuregen export).

  python3 tools/check_structure_live.py

Reads:
  kubejs/exported/worldgen/structuregen.json            live registry (/dumpstructuregen in game)
  kubejs/generation_scripts/worldgen/structure_config.json
  kubejs/generation_scripts/worldgen/structure_tag_report.md   what structure_overrides.js wrote

Prints:
  1. live structure_sets missing from structure_config.json (not tunable: re-run
     tools/gen_structure_config.py, or add a code-registered set to its CODE_REGISTERED_SETS table)
  2. config sets that aren't live (a mod was removed: re-run the scanner)
  3. live structures that belong to no set (never generate on their own)
  4. every set the report says was written, checked against the live spacing / separation / count.
     A mismatch means something replaced the override after it loaded (a mod applying its own
     config in code), or the dump comes from an older world load than the report.
The live dump can't read frequency or salt (Rhino method lookup), so those aren't compared.
Exit code 1 when section 1, 2 or 4 has findings.
"""
import json, os, re, sys, datetime

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DUMP = os.path.join(ROOT, 'kubejs/exported/worldgen/structuregen.json')
CONFIG = os.path.join(ROOT, 'kubejs/generation_scripts/worldgen/structure_config.json')
REPORT = os.path.join(ROOT, 'kubejs/generation_scripts/worldgen/structure_tag_report.md')
SPLIT = 'creativeengineering:split/'

def stamp(path):
    return datetime.datetime.fromtimestamp(os.path.getmtime(path)).strftime('%Y-%m-%d %H:%M')

live = json.load(open(DUMP))
live_sets, live_structs = live['structure_sets'], live['structures']
cfg_sets = json.load(open(CONFIG))['structure_sets']
print('dump %s (%s) | report %s' % (live.get('generated', '?'), stamp(DUMP), stamp(REPORT)))
bad = 0

missing = sorted(s for s in live_sets if s not in cfg_sets and not s.startswith(SPLIT))
print('\n1. live sets not in structure_config.json: %d' % len(missing))
for s in missing:
    print('   ', s, [e['structure'] for e in live_sets[s].get('structures') or []])
bad += len(missing)

gone = sorted(set(cfg_sets) - set(live_sets))
print('\n2. config sets not live: %d' % len(gone))
for s in gone:
    print('   ', s, '(' + cfg_sets[s].get('source', '?') + ')')
bad += len(gone)

in_set = {e['structure'] for v in live_sets.values() for e in v.get('structures') or []}
orphans = sorted(set(live_structs) - in_set)
groups = {}
for s in orphans:
    key = 'terralith:zpointer/*' if s.startswith('terralith:zpointer/') else s
    groups[key] = groups.get(key, 0) + 1
print('\n3. live structures in no set (never generate naturally): %d' % len(orphans))
for k, n in sorted(groups.items()):
    print('   ', k + (' x%d' % n if n > 1 else ''))

# "- Written as `id`: target x.., got x... spacing 34 -> **30**, separation 8 -> **7**, frequency 1 -> **0.9**"
written = {}
for line in open(REPORT, encoding='utf-8'):
    m = re.match(r'- Written as `([^`]+)`:(.*)$', line)
    if not m:
        continue
    want = {}
    for field in ('spacing', 'separation', 'count'):
        f = re.search(field + r' (\d+)(?: -> \*\*(\d+)\*\*)?', m.group(2))
        if f:
            want[field] = int(f.group(2) or f.group(1))
    written[m.group(1)] = want

print('\n4. written overrides vs live values: %d sets' % len(written))
mism = 0
for sid, want in sorted(written.items()):
    got = (live_sets.get(sid) or {}).get('placement')
    if got is None:
        print('    NOT LIVE', sid)
        mism += 1
        continue
    diffs = ['%s want %s live %s' % (k, v, got.get(k)) for k, v in want.items() if got.get(k) is not None and got.get(k) != v]
    if diffs:
        print('    MISMATCH', sid + ':', ', '.join(diffs))
        mism += 1
print('    %d mismatch(es)' % mism)
bad += mism
sys.exit(1 if bad else 0)
