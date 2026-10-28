#!/usr/bin/env bash
# Offline check for the food family crafts (feature_writeup/creative_delight.md 30, food_families.md 4.5, test FFH-03):
# runs CD's cd_food.js and the server scripts that call global.CD.food in the pack's own Rhino fork, with the real
# item and fluid dicts and a recording global.NCH stub, and prints every recipe call. Then lists the family recipe ids
# that are not in the last recipe dump (kubejs/exported/recipes/all.csv). Usage: tools/test_cd_food.sh [server script ...]
# (paths under kubejs/data/creative_delight/kubejs/; "parse:<path>" only compiles one). Needs the ATLauncher library
# folder (Rhino's gson). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
cd "$ROOT"
RHINO="$(ls mods/rhino-forge-*.jar | head -1)"; GSON="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
javac -proc:none -cp "$RHINO:$GSON" -d "$T" tools/cd_food_test/FfhRun.java
if [ $# -eq 0 ]; then set -- creativeengineering/baking.js rusticdelight/recipes.js creativeengineering/cultivated_meat.js; fi
java -cp "$T:$RHINO:$GSON" FfhRun "$ROOT" tools/cd_food_test/prelude.js "$@" | tee "$T/out.txt"
python3 - "$T/out.txt" <<'PY'
import sys, re, csv
types = {'ceBaking': 'baking', 'fdCooking': 'farmersdelightcooking', 'shapeless': 'shapeless', 'fdCutting': 'farmersdelightcutting',
	'smelting': 'smelting', 'smoking': 'smoking', 'campfireCooking': 'campfirecooking'}
made = set()
for line in open(sys.argv[1]).read().splitlines():
	m = re.match(r'(\w+)\((.*)\)$', line)
	if not m or m.group(1) not in types: continue
	parts = m.group(2).split(' | ')
	if 'e' in parts and parts.index('e') + 1 < len(parts):
		made.add('creative_delight:%s/%s' % (types[m.group(1)], parts[parts.index('e') + 1]))
dump = set(r['id'] for r in csv.DictReader(open('kubejs/exported/recipes/all.csv')))
new = sorted(x for x in made if x not in dump)
print('== %d ids made, %d not in the dump' % (len(made), len(new)))
for x in new: print('  ' + x)
PY
