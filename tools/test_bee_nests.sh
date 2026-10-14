#!/usr/bin/env bash
# Offline checks for the bee nest tuning (feature_writeup/bee_nests.md section 6):
#   1. generator: tools/gen_bee_nests.py runs on the real bee_nests.json with no config problems
#   2. Rhino, real config: kubejs/server_scripts/worldgen/bee_nests.js in the pack's Rhino fork; every rule tag equals
#      the generator's resolved list, the bee_nests modifier holds the chance/scale rules in file order, and every
#      trees rule has its placed feature (rate tier as the generator computes it) + add_features modifier
#   3. Rhino, fixture (tools/bee_nests_test/rules_fixture.json): the same parity on every rule shape; each bad rule
#      is rejected with an error, the rule matching no biome is skipped
#   4. switch off: no tags, no files
#   5. old jar (no BeeNests class): no bee_nests modifier (its type would be unknown), trees still written, error logged
#   6. a mod missing (vinery, fixture): its tree rule writes no feature, nothing else changes
# Needs the ATLauncher library folder (Rhino's gson). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
FIX="$ROOT/tools/bee_nests_test/rules_fixture.json"
cd "$ROOT"

echo "== 1. generator"
python3 tools/gen_bee_nests.py | tee "$T/gen.txt" | head -1
! grep -q PROBLEM "$T/gen.txt" || { echo "generator reports config problems"; exit 1; }

RHINO="$(ls mods/rhino-forge-*.jar | head -1)"; GSON="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
mkdir -p "$T/cls"
javac -proc:none -cp "$RHINO:$GSON" -d "$T/cls" tools/bee_nests_test/RhinoBeeNests.java
run() { java -cp "$T/cls:$RHINO:$GSON" RhinoBeeNests "$ROOT" "$@"; }

parity() {   # parity <out dir> <config path>
python3 - "$1" "$2" <<'PY'
import json, sys, glob, os
sys.path.insert(0, 'tools')
import gen_bee_nests as b, gen_crop_worldgen_table as g, gen_wild_flowers as gw
out, cfg_path = sys.argv[1], sys.argv[2]
cfg = g.loads_lenient(open(cfg_path).read())
world = gw.load_world()
traits = g.load_wild_crops()[1]
problems = []
rules = b.check_rules(cfg, traits, world, problems)
tags = json.load(open(os.path.join(out, 'tags.json')))
files = {os.path.relpath(p, os.path.join(out, 'data')): json.load(open(p))
         for p in glob.glob(os.path.join(out, 'data/**/*.json'), recursive=True)}
bad = []
for r, bs in rules:
    got = tags.get('creativeengineering:bee_nests/' + r['id'], [])
    if bs != got:
        bad.append(f"tag {r['id']}: {sorted(set(bs) ^ set(got))}")
want_mod = [{k: v for k, v in (('id', r['id']), ('biomes', '#creativeengineering:bee_nests/' + r['id']),
             ('chance', r.get('chance')), ('scale', r.get('scale'))) if v is not None}
            for r, _ in rules if r.get('chance') is not None or r.get('scale') is not None]
mod = files.pop('creativeengineering/forge/biome_modifier/bee_nests.json', None)
if want_mod and (not mod or mod['type'] != 'creativeengineering:bee_nests' or mod['rules'] != want_mod):
    bad.append(f'bee_nests modifier: want {want_mod}, got {mod}')
if not want_mod and mod:
    bad.append('bee_nests modifier written with no chance/scale rules')
missing = set(os.environ.get('MISSING', '').split()) - {''}
for r, _ in rules:
    t = r.get('trees')
    pf = files.pop(f"creativeengineering/worldgen/placed_feature/bee_nests/{r['id']}.json", None)
    am = files.pop(f"creativeengineering/forge/biome_modifier/bee_nests/{r['id']}.json", None)
    if not t or t['feature'].split(':')[0] in missing:
        if pf or am:
            bad.append(f"{r['id']}: tree files written but should not be")
        continue
    if not pf or not am:
        bad.append(f"{r['id']}: tree files missing"); continue
    first = pf['placement'][0]
    rate = first['count'] if first['type'] == 'minecraft:count' else 1 / first['chance']
    if pf['feature'] != t['feature'] or abs(rate - b.tier_rate(b.parse_rate(t['rate']))) > 1e-9:
        bad.append(f"{r['id']}: placed feature {pf}")
    sap = t.get('sapling', b.DEFAULT_SAPLING)
    if pf['placement'][-1]['predicate']['state']['Name'] != sap:
        bad.append(f"{r['id']}: sapling check is not {sap}")
    if am != {'type': 'forge:add_features', 'biomes': '#creativeengineering:bee_nests/' + r['id'],
              'features': 'creativeengineering:bee_nests/' + r['id'], 'step': 'vegetal_decoration'}:
        bad.append(f"{r['id']}: add modifier {am}")
if files:
    bad.append(f'unexpected files {sorted(files)}')
for x in bad:
    print('MISMATCH', x)
print(f'{len(rules)} rules: tags, modifier and tree features match the generator' if not bad else f'{len(bad)} mismatches')
sys.exit(1 if bad else 0)
PY
}

echo "== 2. Rhino, real config"
run "$T/on" | tee "$T/on.log" | tail -1
! grep -q '^ERROR\|^WARN' "$T/on.log" || { echo "script logged warnings/errors"; exit 1; }
parity "$T/on" kubejs/generation_scripts/worldgen/bee_nests.json

echo "== 3. Rhino, fixture"
run "$T/fix" "config=$FIX" > "$T/fix.log"
for want in 'Bad_Id): id must be' '(floor): duplicate id' '(nothing): needs chance' '(too_likely): chance must' \
            '(no_rate): trees.rate must' 'no_biome: matches no biome' 'trait "no_such_trait" is on no biome'; do
  grep -qF "$want" "$T/fix.log" || { echo "fixture: expected log line missing: $want"; cat "$T/fix.log"; exit 1; }
done
echo "fixture: 7 bad rules reported"
parity "$T/fix" "$FIX" 2>&1 | grep -v '^PROBLEM' || true
parity "$T/fix" "$FIX" > /dev/null

echo "== 4. switch off"
run "$T/off" off | tail -1 | grep -q '^0 tags, 0 data files' && echo "off: nothing applied"

echo "== 5. old jar"
run "$T/nojava" nojava "config=$FIX" > "$T/nojava.log"
grep -q 'ERROR.*old jar' "$T/nojava.log" && [ ! -e "$T/nojava/data/creativeengineering/forge/biome_modifier/bee_nests.json" ] \
  && ls "$T/nojava/data/creativeengineering/worldgen/placed_feature/bee_nests/" | grep -q meadow_trees \
  && echo "old jar: chance/scale modifier left out, trees written, error logged"

echo "== 6. a mod missing"
run "$T/miss" missing=vinery "config=$FIX" > "$T/miss.log"
grep -q 'apple_bees: mod not loaded, trees skipped' "$T/miss.log" && MISSING=vinery parity "$T/miss" "$FIX" > /dev/null \
  && echo "missing mod: its tree feature skipped, everything else unchanged"
