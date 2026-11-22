#!/usr/bin/env bash
# Offline checks for Raspberry Pie standing alone (feature_writeup/plan_rp_standalone.md P2 to P4): RpStandaloneTest parses
# the built-in default ladder on vanilla registries (no file warning, the ruled shape), checks the diversity table merge
# rules, loads the pack table (development/creativeDelight/src/main/resources/solraspberrypie/diversity) and applies
# the lang name overrides. Runs against
# development/solRaspberryPie build/classes (./gradlew :solRaspberryPie:compileJava --offline first). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
AP="$HOME/.gradle/caches/forge_gradle/deobf_dependencies/local/solapplepie-1.19.2/2.2.0_mapped_official_1.19.2/solapplepie-1.19.2-2.2.0_mapped_official_1.19.2.jar"
CP="$CP:$ROOT/development/solRaspberryPie/build/classes/java/main:$AP"
mkdir -p "$T/stub" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
javac -proc:none -nowarn -cp "$T/stub:$CP" -d "$T/test" tools/rp_test/com/SaintAlfonzo/solraspberrypie/*/*.java 2>&1 | grep -v 'warning\|unchecked\|deprecat\|Unsafe' || true
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.solraspberrypie.ladder.RpStandaloneTest "$ROOT" > "$T/java.txt" 2>&1 || true
grep -E '^(ok|FAIL|note|ALL|[0-9]+ FAILED)|^     warning|Exception|Error' "$T/java.txt" || true
grep -q '^ALL [0-9]* OK' "$T/java.txt" # a FAIL line or a crash fails the script
