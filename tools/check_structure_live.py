#!/usr/bin/env python3
"""Compare structure tuning against the live game (the /dumpstructuregen export).

  python3 tools/check_structure_live.py

Reads:
  kubejs/exported/worldgen/structuregen.json            live registry (/dumpstructuregen in game)
  kubejs/generation_scripts/worldgen/structure_config.json
  what structure_overrides.js writes for the CURRENT configs (runs tools/structure_tuning_sim.js)

Prints:
  1. live structure_sets missing from structure_config.json (not tunable: re-run
     tools/gen_structure_config.py, or add a code-registered set to its CODE_REGISTERED_SETS table);
     the creativeengineering:group/* sets are expected and not listed, and sets with no members are skipped
  2. config sets that aren't live (a mod was removed: re-run the scanner)
  3. live structures that belong to no set (never generate on their own)
  4. every set the script would write, checked against the live spacing / separation / count and
     members. A mismatch means the configs changed since that world load (rejoin), or something
     replaced the override after it loaded (a mod applying its own config in code).
frequency and salt are in the dump (patcherMod worldgen.PlacementInfo) but aren't compared, and the
per-structure frequencies live in patcherMod, not the registry (the log line
"[structure-frequency] N structure(s) in M set(s)" confirms them).
Exit code 1 when section 1, 2 or 4 has findings.
"""
import json, os, re, shutil, subprocess, sys, tempfile, datetime

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DUMP = os.path.join(ROOT, 'kubejs/exported/worldgen/structuregen.json')
CONFIG = os.path.join(ROOT, 'kubejs/generation_scripts/worldgen/structure_config.json')
GROUP = 'creativeengineering:group/'

def stamp(path):
    return datetime.datetime.fromtimestamp(os.path.getmtime(path)).strftime('%Y-%m-%d %H:%M')

live = json.load(open(DUMP))
live_sets, live_structs = live['structure_sets'], live['structures']
cfg_sets = json.load(open(CONFIG))['structure_sets']
print('dump %s (%s)' % (live.get('generated', '?'), stamp(DUMP)))
bad = 0

untuned = sorted(s for s in live_sets if s not in cfg_sets and not s.startswith(GROUP))
# A set with no members places nothing (e.g. gen_structure_config DONOR_EMPTIED_SETS), so it needs no tuning row.
empty = [s for s in untuned if not live_sets[s].get('structures')]
missing = [s for s in untuned if s not in empty]
print('\n1. live sets not in structure_config.json: %d' % len(missing))
if empty:
    print('    (%d empty set(s) skipped, they place nothing: %s)' % (len(empty), ', '.join(empty)))
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

# what structure_overrides.js adds for the current configs
tmp = tempfile.mkdtemp()
sim = subprocess.run(['node', os.path.join(ROOT, 'tools/structure_tuning_sim.js'), os.path.join(tmp, 'report.md'), tmp],
                     capture_output=True, text=True)
if sim.returncode != 0:
    print('\n4. the simulation reports config errors, nothing would be applied:\n' + sim.stdout)
    sys.exit(1)
written = {}
for dirpath, _, files in os.walk(tmp):
    for f in files:
        rel = os.path.relpath(os.path.join(dirpath, f), tmp)
        m = re.match(r'([^/]+)/worldgen/structure_set/(.+)\.json$', rel)
        if m:
            written[m.group(1) + ':' + m.group(2)] = json.load(open(os.path.join(dirpath, f)))
shutil.rmtree(tmp, ignore_errors=True)

print('\n4. written sets vs live values: %d sets' % len(written))
mism = 0
for sid, want in sorted(written.items()):
    got = live_sets.get(sid)
    if got is None or got.get('placement') is None:
        print('    NOT LIVE', sid)
        mism += 1
        continue
    diffs = ['%s want %s live %s' % (k, want['placement'][k], got['placement'].get(k))
             for k in ('spacing', 'separation', 'count')
             if k in want['placement'] and got['placement'].get(k) is not None and got['placement'][k] != want['placement'][k]]
    want_members = sorted(e['structure'] for e in want['structures'])
    live_members = sorted(e['structure'] for e in got.get('structures') or [])
    if want_members != live_members:
        diffs.append('members want %d live %d' % (len(want_members), len(live_members)))
    if diffs:
        print('    MISMATCH', sid + ':', ', '.join(diffs))
        mism += 1
print('    %d mismatch(es)' % mism)
bad += mism
sys.exit(1 if bad else 0)
