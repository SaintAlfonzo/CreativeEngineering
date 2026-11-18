#!/usr/bin/env bash
# Offline checks for idle random ticks (feature_writeup/random_tick_audit.md): IdleRandomTicksTest parses rule lines,
# checks the [perf] defaults and runs the rule table on vanilla block states. Runs against development/creativeCommon
# build/classes (./gradlew :creativeCommon:compileJava --offline first). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
mkdir -p "$T/stub" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
javac -proc:none -nowarn -cp "$T/stub:$CP" -d "$T/test" tools/idle_ticks_test/com/SaintAlfonzo/creativecommon/perf/*.java 2>&1 | grep -v 'warning\|unchecked\|deprecat\|Unsafe' || true
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativecommon.perf.IdleRandomTicksTest > "$T/java.txt" 2>&1 || true
grep -E '^(ok|FAIL|skip|ALL|[0-9]+ FAILED)|Exception|Error' "$T/java.txt" || true
grep -q '^ALL [0-9]* OK' "$T/java.txt" # a FAIL line or a crash fails the script
