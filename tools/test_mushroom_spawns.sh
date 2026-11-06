#!/usr/bin/env bash
# Offline checks for the mushroom spawn system (feature_writeup/fungi_delight.md section 5.1.1):
#   1. generator: tools/gen_crop_worldgen_table.py runs with no config problems (it evaluates mushroom_spawns.json too)
#   2. Rhino: kubejs/data/creativeworldgen/kubejs/worldgen/mushroom_spawns.js (+ _biome_rules.js) in the pack's Rhino fork; every
#      rule tag must equal the generator's resolved list (cave rules include ocean and cave biomes), one add modifier
#      per rule at its mode's step, forge:none for every disable_modifiers id, no remove modifier; the brown/red
#      colonies are no longer wild_crops.json rules
#   3. placed features: every feature a rule names is a pack JSON with a rarity_filter; surface ones use a heightmap,
#      cave ones search a floor below sea level, 13+ blocks under the surface, on air
#   4. Rhino with CEConfig mushroomSpawns off: no tags, no files
#   5. Rhino with Farmer's Delight missing: its modifiers are not switched off, nothing else changes
# Needs the ATLauncher library folder (Rhino's gson). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
cd "$ROOT"

echo "== 1. generator"
python3 tools/gen_crop_worldgen_table.py | tee "$T/gen.txt" | head -1
! grep -q PROBLEM "$T/gen.txt" || { grep PROBLEM "$T/gen.txt"; echo "generator reports config problems"; exit 1; }

echo "== 2. Rhino"
RHINO="$(ls mods/rhino-forge-*.jar | head -1)"; GSON="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
mkdir -p "$T/cls"
javac -proc:none -cp "$RHINO:$GSON" -d "$T/cls" tools/wild_crops_test/RhinoWildCrops.java
java -cp "$T/cls:$RHINO:$GSON" RhinoWildCrops "$ROOT" "$T/on" script=mushroom_spawns | tee "$T/on.log" | tail -2
! grep -q '^ERROR\|^WARN' "$T/on.log" || { echo "script logged warnings/errors"; exit 1; }
python3 - "$T/on" <<'PY'
import json, sys, glob, os
sys.path.insert(0, 'tools')
import gen_crop_worldgen_table as g
cfg, traits = g.load_wild_crops()
rules = {k.split(':', 1)[1]: c for k, c in cfg['crops'].items() if c.get('_mushroom')}
tags = json.load(open(os.path.join(sys.argv[1], 'tags.json')))
bad = 0
for key, rule in rules.items():
    want = g.resolve_crop(traits, rule)
    got = tags.get('creativeengineering:mushroom_spawns/' + key, [])
    if want != got:
        bad += 1; print('MISMATCH', key, sorted(set(want) ^ set(got)))
    if rule.get('mode') == 'cave' and not {b for b in got if traits[b] & {'ocean', 'cave'}}:
        bad += 1; print('cave rule without ocean/cave biomes:', key)
files = {p: json.load(open(p)) for p in glob.glob(os.path.join(sys.argv[1], 'data/**/*.json'), recursive=True)}
adds = [d for d in files.values() if d['type'] == 'forge:add_features']
nones = [p for p, d in files.items() if d['type'] == 'forge:none']
rem = [d for d in files.values() if d['type'] == 'forge:remove_features']
want_nones = {m for c in rules.values() for m in c.get('disable_modifiers', [])}
ok = len(adds) == sum(1 for c in rules.values() if c.get('features')) and len(nones) == len(want_nones) and not rem
for d in adds:
    rule = rules[d['biomes'].split('/', 1)[1]]
    step = 'underground_decoration' if rule.get('mode') == 'cave' else 'vegetal_decoration'
    ok &= d['step'] == step and d['biomes'].startswith('#creativeengineering:mushroom_spawns/') and d['features'] == rule['features']
moved = [k for k, c in cfg['crops'].items() if not c.get('_mushroom') and 'mushroom_colon' in json.dumps(c)]
print(f'{len(rules)} rule tags match the generator' if not bad else f'{bad} tags differ')
print(f'{len(adds)} add modifiers, {len(nones)} forge:none, {len(rem)} remove modifier: ' + ('as expected' if ok else 'UNEXPECTED'))
print('brown/red colonies only in mushroom_spawns.json' if not moved else f'still in wild_crops.json: {moved}')
sys.exit(1 if bad or not ok or moved else 0)
PY

echo "== 3. placed features"
python3 - <<'PY'
import json, sys
sys.path.insert(0, 'tools')
import gen_crop_worldgen_table as g
cfg, traits = g.load_wild_crops()
bad = n = 0
for key, rule in cfg['crops'].items():
    if not rule.get('_mushroom'):
        continue
    for fid in rule.get('features', []):
        ns, path = fid.split(':', 1)
        try:
            d = json.load(open(f'kubejs/data/{ns}/worldgen/placed_feature/{path}.json'))
        except FileNotFoundError:
            bad += 1; print('no pack JSON for', fid); continue
        n += 1
        types = [m['type'] for m in d['placement']]
        ok = 'minecraft:rarity_filter' in types and types[-1] == 'minecraft:biome'
        if rule.get('mode') == 'cave':
            byt = {m['type']: m for m in d['placement']}
            top = byt.get('minecraft:height_range', {}).get('height', {}).get('max_inclusive', {}).get('absolute', 999)
            # carrots never above Y 48: origin <= 44, the patch spreads 3 up and the scan offset adds 1
            ok &= top <= (44 if 'cave_carrots' in fid else 62)
            ok &= byt.get('minecraft:environment_scan', {}).get('direction_of_search') == 'down'
            ok &= byt.get('minecraft:surface_relative_threshold_filter', {}).get('max_inclusive', 0) <= -13
            pred = byt.get('minecraft:block_predicate_filter', {}).get('predicate', {})
            air = ['minecraft:air', 'minecraft:cave_air'] if 'cave_carrots' in fid else ['minecraft:air']  # carrots also fill carver caves
            ok &= pred.get('type') == 'minecraft:all_of' and pred['predicates'][0] == {'type': 'minecraft:matching_blocks', 'blocks': air}
            if 'cave_carrots' in fid:
                ok &= byt['minecraft:environment_scan']['allowed_search_condition'] == {'type': 'minecraft:matching_blocks', 'blocks': air}
        else:
            ok &= 'minecraft:heightmap' in types
        if not ok:
            bad += 1; print('unexpected placement:', fid)
print(f'{n} placed features checked' + (f', {bad} problems' if bad else ': as expected'))
sys.exit(1 if bad else 0)
PY

echo "== 4. switch off"
java -cp "$T/cls:$RHINO:$GSON" RhinoWildCrops "$ROOT" "$T/off" off script=mushroom_spawns | tail -1 | grep -q '^0 tags, 0 data files' && echo "off: nothing applied"

echo "== 5. a mod missing"
java -cp "$T/cls:$RHINO:$GSON" RhinoWildCrops "$ROOT" "$T/miss" missing=farmersdelight script=mushroom_spawns > "$T/miss.log"
! grep -rq 'farmersdelight' "$T/miss/data" && [ "$(find "$T/miss/data" -name '*.json' | wc -l)" -gt 0 ] && echo "missing mod: its modifiers left alone, the rest applied"
