#!/usr/bin/env bash
# Offline checks for the worldgen tools (feature_writeup/structure_tuning.md section 6):
#   1. GroundMatchTest: ground matching (patcherMod worldgen/GroundMatch)
#   2. MassPlacerTest: /ceplace filters, rows on the real structure_tags.json, signs, alignment; WorldgenConfig defaults
#   3. EarthFillTest: earth-fill and water-fill counting, template earth and water through rotation, pool elements, overrides, seabed
#   4. node: CEConfig [worldgen.debug] structureDensity scales every set's rate exactly (tools/structure_density_check.js)
#   5. TerrainAdaptationTest: template trim (kinds, columns, real ported templates), tag precedence, distance fields, soft beard vs vanilla
# Java parts run against development/patcherMod/build/classes (run ./gradlew build first). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
mkdir -p "$T/stub" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
javac -proc:none -nowarn -cp "$T/stub:$CP" -d "$T/test" tools/worldgen_test/com/SaintAlfonzo/creativeengineering/worldgen/*.java 2>&1 | grep -v 'warning\|unchecked\|deprecat' || true
status=0
echo "== 1. ground matching"
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.GroundMatchTest 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' || status=1
echo "== 2. /ceplace + WorldgenConfig"
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.MassPlacerTest "$ROOT" 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED| +T&T)|Exception|Error' || status=1
echo "== 3. earth fill"
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.EarthFillTest 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' || status=1
echo "== 4. debug structure density"
node tools/structure_density_check.js || status=1
echo "== 5. terrain adaptation + template trim"
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.TerrainAdaptationTest "$ROOT" 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' || status=1
exit $status
