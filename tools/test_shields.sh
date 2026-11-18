#!/usr/bin/env bash
# Offline checks for the tier shields (feature_writeup/shields.md section 6):
#   1. tools/shield_check.py: effective ladder, toml vs code defaults, references, tag overlaps
#   2. ShieldConfigTest: [shields] spec builds, burn-tick ladder + live edit, cooldown default, effect-tick context
# The Java part runs against development/creativeCommon + creativeEquipment build/classes (the shields are in Equipment;
# run ./gradlew :creativeCommon:compileJava :creativeEquipment:compileJava --offline first). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
CP="$CP:$ROOT/development/creativeEquipment/build/classes/java/main"
status=0
python3 tools/shield_check.py > "$T/check.txt" || status=1
sed -n '/== 3. Problems/,/== 4/p' "$T/check.txt" | sed '$d'
mkdir -p "$T/stub" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
javac -proc:none -nowarn -cp "$T/stub:$CP" -d "$T/test" tools/shield_test/com/SaintAlfonzo/creativeequipment/shield/*.java 2>&1 | grep -v 'warning\|unchecked\|deprecat\|Unsafe' || true
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativeequipment.shield.ShieldConfigTest > "$T/java.txt" 2>&1 || true
grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' "$T/java.txt" || true
grep -q '^ALL [0-9]* OK' "$T/java.txt" || status=1 # a FAIL line or a crash fails the script
exit $status
