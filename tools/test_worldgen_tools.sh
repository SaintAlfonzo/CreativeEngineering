#!/usr/bin/env bash
# Offline checks for the worldgen tools (feature_writeup/structure_tuning.md section 6):
#   1. GroundMatchTest: ground matching (patcherMod worldgen/GroundMatch)
#   2. MassPlacerTest: /ceplace filters, rows on the real structure_tags.json, signs, alignment; WorldgenConfig defaults
#   3. EarthFillTest: earth-fill and water-fill counting, template earth and water through rotation, pool elements, overrides, seabed
#   4. node: CEConfig [worldgen.debug] structureDensity scales every set's rate exactly (tools/structure_density_check.js)
#   5. TerrainAdaptationTest: template trim (kinds, columns, real ported templates), tag precedence, distance fields, soft beard vs vanilla
#   6. MetaTest: meta structures (codec on the real meta_structures.json and bad input, the anchor's frequency roll, reach and clearance)
#   7. FitTest: biome fits (the node sim's staged fits parsed by StructureBiomeFits, bad input, fit unions, value-based membership)
#   8. LocateTest: locate counters (misses before a hit, stages, batch capture) and Distant Horizons' fake seabed roll
#   9. tools/check_worldgen_json.py: kubejs/data worldgen JSON has the keys 1.19.2's codecs require (ported 1.20 data)
#  10. PoolTunerTest: canonical pool order, processor lists of pieces added in code rebound to the world's registry
#  11. BiomeLayoutCacheTest: the biome tuner's disk cache (hit, wrong key, corrupt file, edited input)
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
echo "== 6. meta structures"
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.MetaTest "$ROOT" 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' || status=1
echo "== 7. biome fits"
node tools/structure_tuning_sim.js "$T/fits_report.md" "$T/fits" > /dev/null
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.FitTest "$T/fits/fits.json" 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' || status=1
echo "== 8. locate"
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.LocateTest 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' || status=1
echo "== 9. worldgen JSON shape (1.19.2 codecs)"
python3 tools/check_worldgen_json.py || status=1
echo "== 10. pool order + processor rebind"
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.PoolTunerTest 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' || status=1
echo "== 11. biome tuner disk cache"
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeengineering.worldgen.BiomeLayoutCacheTest "$ROOT" 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' || status=1
exit $status
