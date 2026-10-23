#!/usr/bin/env bash
# Offline checks for the wild crop placement (feature_writeup/crops.md section 2.8):
#   1. generator: tools/gen_crop_worldgen_table.py runs with no config problems
#   2. Rhino: kubejs/server_scripts/worldgen/wild_crops.js (+ _biome_rules.js) in the pack's Rhino fork; every crop tag
#      must equal the generator's resolved list, and every data file must be valid JSON with the expected modifiers
#      at the crop's step
#   3. Rhino with CEConfig wildCrops off: no tags, no files
#   4. Rhino with a mod missing (neapolitan): its features/modifiers skipped, nothing else changes
# Needs the ATLauncher library folder (Rhino's gson). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
cd "$ROOT"

echo "== 1. generator"
python3 tools/gen_crop_worldgen_table.py | tee "$T/gen.txt" | head -2
! grep -q PROBLEM "$T/gen.txt" || { echo "generator reports config problems"; exit 1; }

echo "== 2. Rhino"
RHINO="$(ls mods/rhino-forge-*.jar | head -1)"; GSON="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
mkdir -p "$T/cls"
javac -proc:none -cp "$RHINO:$GSON" -d "$T/cls" tools/wild_crops_test/RhinoWildCrops.java
java -cp "$T/cls:$RHINO:$GSON" RhinoWildCrops "$ROOT" "$T/on" | tee "$T/on.log" | tail -2
! grep -q '^ERROR\|^WARN' "$T/on.log" || { echo "script logged warnings/errors"; exit 1; }
python3 - "$T/on" <<'PY'
import json, sys, glob, os
sys.path.insert(0, 'tools')
import gen_crop_worldgen_table as g
cfg, traits = g.load_wild_crops()
cfg['crops'] = {k: c for k, c in cfg['crops'].items() if not c.get('_mushroom')}   # mushroom_spawns.json: own test
tags = json.load(open(os.path.join(sys.argv[1], 'tags.json')))
bad = 0
for key, crop in cfg['crops'].items():
    want = g.resolve_crop(traits, crop)
    got = tags.get('creativeengineering:wild_crops/' + key, [])
    if want != got:
        bad += 1; print('MISMATCH', key, sorted(set(want) ^ set(got)))
files = {p: json.load(open(p)) for p in glob.glob(os.path.join(sys.argv[1], 'data/**/*.json'), recursive=True)}
adds = [d for d in files.values() if d['type'] == 'forge:add_features']
nones = [p for p, d in files.items() if d['type'] == 'forge:none']
rem = [d for d in files.values() if d['type'] == 'forge:remove_features']
want_nones = {m for c in cfg['crops'].values() for m in c.get('disable_modifiers', [])}
want_rem = sorted({f for c in cfg['crops'].values() for f in c.get('remove_features', [])})
ok = len(adds) == sum(1 for c in cfg['crops'].values() if c.get('features')) and len(nones) == len(want_nones) and len(rem) == 1 and rem[0]['features'] == want_rem
for d in adds:
    crop = cfg['crops'][d['biomes'].split('/', 1)[1]]
    ok &= d['step'] == crop.get('step', 'vegetal_decoration') and d['biomes'].startswith('#creativeengineering:wild_crops/') and bool(d['features'])
print(f"{len(cfg['crops'])} crop tags match the generator" if not bad else f'{bad} tags differ')
print(f'{len(adds)} add modifiers, {len(nones)} forge:none, {len(rem)} remove modifier: ' + ('as expected' if ok else 'UNEXPECTED'))
sys.exit(1 if bad or not ok else 0)
PY

echo "== 3. switch off"
java -cp "$T/cls:$RHINO:$GSON" RhinoWildCrops "$ROOT" "$T/off" off | tail -1 | grep -q '^0 tags, 0 data files' && echo "off: nothing applied"

echo "== 4. a mod missing"
java -cp "$T/cls:$RHINO:$GSON" RhinoWildCrops "$ROOT" "$T/miss" missing=neapolitan > "$T/miss.log"
grep -q 'skipped.*neapolitan:' "$T/miss.log" && ! grep -rq 'neapolitan' "$T/miss/data" && echo "missing mod: its features and modifiers skipped"
