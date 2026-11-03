#!/usr/bin/env bash
# Offline checks for the mob spawn tuning (feature_writeup/mob_spawns.md section 6):
#   1. Java/Python parity: tools/mob_spawns_test/cases.json through patcherMod SpawnTuning.apply on real vanilla spawn
#      lists (Bootstrap) and through gen_spawn_table.apply_rule; spawners after and change lines must be identical
#   2. Rhino: kubejs/server_scripts/worldgen/mob_spawns.js in the pack's Rhino fork with tools/mob_spawns_test/
#      rules_fixture.json; every tag must equal the generator's selection, and the one tune_spawns modifier + the
#      forge:none files must be what the rules say
#   3. Rhino: bad rules are reported and left out; CEConfig mobSpawns off, an old jar (no SpawnTuning) and a missing
#      mod write nothing for what they cover
#   4. generator on a fixture dump: runs, warns that the dump predates the rules, and stops warning once the dump
#      shows them; --mob prints a table
# Needs a built patcherMod (./gradlew :patcherMod:compileJava) and the ATLauncher library folder. No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
D="tools/mob_spawns_test"
cd "$ROOT"
status=0

echo "== 1. Java/Python parity"
source "$ROOT/tools/java_test_cp.sh"
mkdir -p "$T/stub" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
javac -proc:none -nowarn -cp "$T/stub:$CP" -d "$T/test" "$D"/com/SaintAlfonzo/creativeworldgen/worldgen/SpawnTuningParity.java 2>&1 | grep -v 'warning\|unchecked\|deprecat' || true
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeworldgen.worldgen.SpawnTuningParity "$D/cases.json" "$T/parity.json" 2>&1 \
  | grep -E 'cases run|Exception|Error' || status=1
python3 - "$T/parity.json" "$D/cases.json" <<'PY' || status=1
import json, sys
sys.path.insert(0, 'tools')
import gen_spawn_table as g
got = json.load(open(sys.argv[1]))
cases = {c['name']: c for c in json.load(open(sys.argv[2]))['cases']}
def lists(entries):
    sp = {}
    for e in entries:
        sp.setdefault(e['category'], []).append({'type': e['mob'], 'weight': e['weight'], 'min': e['min'], 'max': e['max']})
    return sp
bad = 0
for name, res in got['cases'].items():
    sp = lists(res['before'])
    lines = []
    for n, r in enumerate(cases[name]['rules'], 1):
        line = g.apply_rule(sp, r, got['categories'][r['mob']])
        lines.append(line)
    want = g.strip(lists(res['after']))
    if g.strip(sp) != want or lines != res['changes']:
        bad += 1
        print('MISMATCH', name, '\n  java  ', res['changes'], want, '\n  python', lines, g.strip(sp))
print(f"{len(got['cases'])} cases: Java and Python agree" if not bad else f'{bad} cases differ')
sys.exit(1 if bad else 0)
PY

echo "== 2. Rhino"
RHINO="$(ls mods/rhino-forge-*.jar | head -1)"; GSON="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
mkdir -p "$T/cls"
javac -proc:none -cp "$RHINO:$GSON" -d "$T/cls" "$D/RhinoMobSpawns.java"
run() { java -cp "$T/cls:$RHINO:$GSON" RhinoMobSpawns "$ROOT" "$@"; }
run "$T/on" "$D/rules_fixture.json" > "$T/on.log"; tail -2 "$T/on.log"
! grep -q '^ERROR\|^WARN' "$T/on.log" || { echo "script logged warnings/errors:"; grep '^ERROR\|^WARN' "$T/on.log"; status=1; }
python3 - "$T/on" "$D/rules_fixture.json" <<'PY' || status=1
import json, sys, glob, os
sys.path.insert(0, 'tools')
import gen_spawn_table as g
traits = g.load_traits()
rules = json.load(open(sys.argv[2]))['rules']
tags = json.load(open(os.path.join(sys.argv[1], 'tags.json')))
bad = 0
for r in rules:
    ids, tg, _ = g.resolve_rule(r, traits, {})
    got = tags.get(g.TAG_PREFIX + r['id'], [])
    if ids + tg != got:
        bad += 1; print('MISMATCH', r['id'], sorted(set(ids + tg) ^ set(got)))
files = {os.path.relpath(p, os.path.join(sys.argv[1], 'data')): json.load(open(p))
         for p in glob.glob(os.path.join(sys.argv[1], 'data/**/*.json'), recursive=True)}
mod = files.get('creativeengineering/forge/biome_modifier/mob_spawns.json')
nones = sorted(p for p, d in files.items() if d['type'] == 'forge:none')
ok = mod is not None and mod['type'] == 'creativeengineering:tune_spawns' and [x['id'] for x in mod['rules']] == [r['id'] for r in rules]
for x, r in zip(mod['rules'] if mod else [], rules):
    ok &= x['biomes'] == '#' + g.TAG_PREFIX + r['id'] and x['mob'] == r['mob'] and x['action'] == r['action']
    ok &= all(x.get(k) == r.get(k) for k in ('weight', 'min', 'max', 'category') if r['action'] == 'set')
    ok &= r['action'] != 'scale' or x['factor'] == r['factor']
want_nones = sorted(m.replace(':', '/forge/biome_modifier/') + '.json' for r in rules for m in r.get('disable_modifiers', []))
ok &= nones == want_nones
raw = open(os.path.join(sys.argv[1], 'data/creativeengineering/forge/biome_modifier/mob_spawns.json')).read()
ok &= '.0,' not in raw and '.0}' not in raw      # JS-style numbers, not Gson's 2.0
# spawn conditions: tags = the generator's selection, modifier entries in order, everywhere = no biomes field
cfg = json.load(open(sys.argv[2]))
dump = {'biomes': dict({b: {'tags': []} for b in traits}, **{'minecraft:nether_wastes': {'tags': ['minecraft:is_nether']}}),
        'entities': {m: {} for m in [r['mob'] for r in rules] + [m for c in cfg['conditions'] for m in c['mobs']]}}
rows = [(r, g.resolve_rule(r, traits, {})[2] or ['x'], 0) for r in rules]
conds, cprob, _ = g.build_conditions(dump, cfg, traits, rows)
cok = not cprob and mod is not None and [x['id'] for x in mod.get('conditions', [])] == [c['id'] for c, _, _ in conds] == [c['id'] for c in cfg['conditions']]
for (c, mobs, biomes), x in zip(conds, mod.get('conditions', []) if mod else []):
    cok &= x['condition'] == c['condition'] and x['mobs'] == c['mobs']
    if biomes is None:
        cok &= 'biomes' not in x and g.COND_PREFIX + c['id'] not in tags
    else:
        got = tags.get(g.COND_PREFIX + c['id'], [])
        # tag references stay references in the script's tag; the generator expands them from the dump's tags
        want = sorted(b for b in biomes if b in traits or b in c.get('add', [])) + [t for t in c.get('add', []) if t.startswith('#')]
        cok &= biomes != [] and ('#minecraft:is_nether' not in c.get('add', []) or 'minecraft:nether_wastes' in biomes)
        cok &= x['biomes'] == '#' + g.COND_PREFIX + c['id'] and sorted(got) == sorted(want)
        if sorted(got) != sorted(want):
            print('MISMATCH condition', c['id'], sorted(set(got) ^ set(want)))
ground = {t: v for t, v in cfg['spawn_ground'].items() if not t.startswith('_')}
gok = all(tags.get('block:' + t) == v for t, v in ground.items())
print(f'{len(conds)} conditions: ' + ('tags and modifier entries as expected' if cok else 'UNEXPECTED'))
print('spawn_ground block tags: ' + ('as expected' if gok else 'UNEXPECTED ' + str({k: v for k, v in tags.items() if k.startswith('block:')})))
bad += 0 if cok and gok else 1
print(f'{len(rules)} rule tags match the generator' if not bad else f'{bad} tags differ')
print(f"modifier with {len(mod['rules']) if mod else 0} rules, {len(nones)} forge:none: " + ('as expected' if ok else 'UNEXPECTED'))
sys.exit(1 if bad or not ok else 0)
PY

echo "== 3. bad rules, switch off, old jar, missing mod"
run "$T/bad" "$D/bad_rules_fixture.json" > "$T/bad.log" || true
for want in 'Bad Id): id must be' 'no_action): action must be' 'set_no_weight): set needs a weight' 'dup): duplicate id' \
            'typo_trait: trait "forrest"' 'tag_except: except takes biome ids' 'bad_category): unknown category' \
            'bad_kind): condition must be' 'no_mobs): mobs must be' 'bad_ref): rules: no usable rule set_no_weight'; do
  grep -qF "$want" "$T/bad.log" || { echo "bad rules: no '$want' in the log"; status=1; }
done
python3 -c "import json,sys; d=json.load(open('$T/bad/data/creativeengineering/forge/biome_modifier/mob_spawns.json')); ids=[r['id'] for r in d['rules']]+[c['id'] for c in d['conditions']]; sys.exit(0 if ids==['dup','typo_trait','tag_except','good_cond'] else print('bad rules kept:', ids) or 1)" \
  && echo "bad rules: reported and left out" || status=1
run "$T/off" "$D/rules_fixture.json" off | tail -1 | grep -q '^0 tags, 0 data files' && echo "off: nothing applied" || status=1
run "$T/old" "$D/rules_fixture.json" notuning > "$T/old.log"
tail -1 "$T/old.log" | grep -q '^0 tags, 0 data files' && grep -q 'NOT applied' "$T/old.log" && echo "old jar: nothing written, error logged" || status=1
run "$T/miss" "$D/rules_fixture.json" missing=untitledduckmod > "$T/miss.log"
grep -q 'ducks: mod not loaded' "$T/miss.log" && ! grep -rq 'untitledduckmod' "$T/miss/data" && ! grep -q 'untitledduckmod' "$T/miss/tags.json" \
  && echo "missing mod: its rule, condition mobs, ground blocks and modifiers skipped" || status=1

echo "== 4. generator"
python3 - "$T" <<'PY' || status=1
import json, subprocess, sys, os, copy
sys.path.insert(0, 'tools')
import gen_spawn_table as g
t = sys.argv[1]
traits = g.load_traits()
plains = {'creature': [{'type': 'minecraft:sheep', 'weight': 12, 'min': 4, 'max': 4}, {'type': 'minecraft:pig', 'weight': 10, 'min': 4, 'max': 4},
                       {'type': 'minecraft:chicken', 'weight': 10, 'min': 4, 'max': 4}, {'type': 'minecraft:cow', 'weight': 8, 'min': 4, 'max': 4}],
          'monster': [{'type': 'minecraft:zombie', 'weight': 95, 'min': 4, 'max': 4}, {'type': 'minecraft:skeleton', 'weight': 100, 'min': 4, 'max': 4}]}
ents = {m: {'category': c, 'spawns_in_biomes': 1, 'placement': 'ON_GROUND', 'heightmap': 'MOTION_BLOCKING_NO_LEAVES',
            'predicate_source': 'net.minecraft.world.entity.SpawnPlacements', 'spawn_egg': m + '_spawn_egg'}
        for c, ms in [('creature', ['minecraft:sheep', 'minecraft:pig', 'minecraft:chicken', 'minecraft:cow', 'untitledduckmod:duck']),
                      ('monster', ['minecraft:zombie', 'minecraft:skeleton', 'minecraft:pillager', 'minecraft:strider'])] for m in ms}
dump = {'generated': 'fixture', 'categories': {'creature': {'max_per_chunk': 10, 'persistent': True}, 'monster': {'max_per_chunk': 70}},
        'biomes': {b: {'dimensions': ['minecraft:overworld'], 'creature_probability': 0.1, 'spawners': copy.deepcopy(plains), 'tags': []}
                   for b in traits},
        'entities': ents, 'tuning': {'rules': [], 'matched': {}, 'biomes': {}}}
json.dump(dump, open(os.path.join(t, 'spawns0.json'), 'w'))
rules = os.path.join('tools/mob_spawns_test/rules_fixture.json')
def gen(dumpfile, out, *extra):
    return subprocess.run([sys.executable, 'tools/gen_spawn_table.py', '--dump', dumpfile, '--rules', rules, '--out', out, *extra],
                          capture_output=True, text=True)
r0 = gen(os.path.join(t, 'spawns0.json'), os.path.join(t, 'r0.md'))
ok = r0.returncode == 0 and 'predates' not in r0.stdout and 'the dump (fixture) ran 0 rules' in r0.stdout
md = open(os.path.join(t, 'r0.md')).read()
ok &= all(h in md for h in ('## 1. Pack tuning', '## 2. Mobs by category', '## 3. Creatures by biome type', '## 4. Per mob',
                           '## 5. Per biome', '## 6. Spawn conditions', '## 7. Gaps'))
# the dump as the game would write it after a restart: tuned lists live, the untuned ones in the tuning record
cfg = json.load(open(rules))
tuned, untuned, rows, problems, warnings, stale, changes = g.build(dump, cfg, traits)
live = copy.deepcopy(dump)
for b in changes:
    live['biomes'][b]['spawners'] = tuned[b]
    live['tuning']['biomes'][b] = {'before': [dict(e, category=c) for c, lst in untuned[b].items() for e in lst], 'changes': changes[b]}
live['tuning']['rules'] = [r['id'] for r in cfg['rules']]
live['tuning']['conditions'] = [c['id'] for c, _, _ in g.build_conditions(dump, cfg, traits, rows)[0]]
json.dump(live, open(os.path.join(t, 'spawns1.json'), 'w'))
r1 = gen(os.path.join(t, 'spawns1.json'), os.path.join(t, 'r1.md'))
ok &= r1.returncode == 0 and 'ran ' not in r1.stdout
r2 = gen(os.path.join(t, 'spawns1.json'), os.path.join(t, 'r2.md'), '--mob', 'minecraft:sheep')
ok &= r2.returncode == 0 and r2.stdout.startswith('| Biome |') and '(was 12)' in r2.stdout
print(f'fixture: {sum(len(c) for c in changes.values())} changes over {len(changes)} biomes; stale warning before the restart, '
      'none after; --mob table: ' + ('as expected' if ok else 'UNEXPECTED'))
if not ok:
    print(r0.stdout, r0.stderr, r1.stdout, r1.stderr, r2.stdout[:500], r2.stderr)
sys.exit(0 if ok else 1)
PY

[ "$status" = 0 ] && echo "ALL OK" || echo "FAILED"
exit "$status"
