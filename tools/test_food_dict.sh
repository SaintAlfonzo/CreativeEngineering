#!/usr/bin/env bash
# Offline checks for the food dict (feature_writeup/creative_delight.md 13, tests CDT-08 and CDT-10):
#   1. tools/food_dict.py --check: the lint passes
#   2. Rhino: item_dict.js, food_grammar.js and food_dict.js load in the pack's own Rhino fork and the load check passes
#   3. Rhino: three deliberately bad lines (missing general key, key outside the grammar, item named twice) make it throw
# Needs the ATLauncher library folder (Rhino's gson). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
cd "$ROOT"

echo "== 1. lint"
python3 tools/food_dict.py --check

RHINO="$(ls mods/rhino-forge-*.jar | head -1)"; GSON="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
javac -proc:none -cp "$RHINO:$GSON" -d "$T" tools/food_dict_test/RhinoRun.java
echo "== 2. Rhino load"
java -cp "$T:$RHINO:$GSON" RhinoRun "$ROOT"
echo "== 3. Rhino bad keys"
java -cp "$T:$RHINO:$GSON" RhinoRun "$ROOT" bad
echo "all food dict checks passed"
