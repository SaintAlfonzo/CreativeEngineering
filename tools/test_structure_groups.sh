#!/usr/bin/env bash
# Offline checks for the structure groups (feature_writeup/structure_tuning.md section 6):
#   1. node: tools/structure_tuning_sim.js on the real configs (errors -> exit 1)
#   2. Rhino: the same script in the pack's own Rhino fork; its output must be byte-identical to node's
#      ... also at debug density x4 and x0.3 and with structure groups off (CEConfig [worldgen])
#   3. Rhino with no StructureFrequencies class (an old patcherMod jar): must apply nothing
#   4. Java: tools/structure_groups_test/FreqTest against development/patcherMod/build/classes (run ./gradlew build first)
# Needs the ATLauncher library folder (Rhino's gson, Forge's bootstrap deps). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"

echo "== 1. node simulation"
node tools/structure_tuning_sim.js "$T/node_report.md" "$T/node" | head -3

echo "== 2. Rhino parity"
RHINO="$(ls mods/rhino-forge-*.jar | head -1)"; GSON="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
mkdir -p "$T/rhino_cls"
javac -proc:none -cp "$RHINO:$GSON" -d "$T/rhino_cls" tools/structure_groups_test/RhinoRun.java
java -cp "$T/rhino_cls:$RHINO:$GSON" RhinoRun "$ROOT" "$T/rhino" | tail -1
diff -r --exclude='frequencies*' --exclude=report.md "$T/node" "$T/rhino" && echo "structure_set files identical"
python3 -c "import json,sys; a=json.load(open(sys.argv[1])); b=json.load(open(sys.argv[2])); sys.exit(0 if a == b else 1)" \
  "$T/node/frequencies.json" "$T/rhino/frequencies.raw.json" && echo "frequency tables identical"
diff <(sed 1,3d "$T/node_report.md") <(sed 1,3d "$T/rhino/report.md") >/dev/null && echo "reports identical (timestamp aside)"
for v in "density=4 --density=4" "density=0.3 --density=0.3" "groupsoff --groups-off"; do
  set -- $v
  node tools/structure_tuning_sim.js "$T/node_$1.md" "$T/node_$1" "$2" > /dev/null
  java -cp "$T/rhino_cls:$RHINO:$GSON" RhinoRun "$ROOT" "$T/rhino_$1" "$1" > /dev/null
  diff -r --exclude='frequencies*' --exclude=report.md "$T/node_$1" "$T/rhino_$1" >/dev/null \
    && diff <(sed 1,3d "$T/node_$1.md") <(sed 1,3d "$T/rhino_$1/report.md") >/dev/null \
    && echo "$1: node and Rhino identical ($(find "$T/rhino_$1" -name '*.json' -path '*structure_set*' | wc -l) sets)" || { echo "$1: node and Rhino DIFFER"; exit 1; }
done

echo "== 3. old patcherMod jar"
java -cp "$T/rhino_cls:$RHINO:$GSON" RhinoRun "$ROOT" "$T/rhino_old" nopatcher | tail -1 | grep -q '^0 files, staged nothing' \
  && echo "applies nothing without StructureFrequencies"

echo "== 4. StructureFrequencies logic"
source "$ROOT/tools/java_test_cp.sh"
mkdir -p "$T/stub" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
javac -proc:none -nowarn -cp "$T/stub:$CP" -d "$T/test" tools/structure_groups_test/com/SaintAlfonzo/creativeengineering/worldgen/FreqTest.java 2>&1 | grep -v 'warning\|EventPriority' || true
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.FreqTest 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)'
