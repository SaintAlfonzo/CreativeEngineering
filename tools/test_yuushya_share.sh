#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
# Headless check of the Yuushya Modelling backport's share codes (plan_yuushya_backport.md A11, PORT_NOTES.md section 3):
# tools/yuushya_share_test/. UpstreamShareFormat writes the 2.4.1 format with upstream's records under Gson 2.10.1
# (Minecraft 1.20.1's); ShareUtilsTest reads and writes it with our ShareUtils under Gson 2.8.9 (Minecraft 1.19.2's).
# ShareUtils.java is compiled fresh; the classes it references come from the project's Gradle build
# (development/backportedYuushyaModelling/build/classes, so build it once first). Picked up by tools/check_all.py.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
MOD="$ROOT/development/backportedYuushyaModelling"
BUILT="$MOD/build/classes/java/main"
G289="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
G2101="$L/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
[ -d "$BUILT/com/yuushya" ] || { echo "FAIL: $BUILT missing (./gradlew :backportedYuushyaModelling:build --offline)"; exit 1; }
mkdir -p "$T/up" "$T/mod" "$T/test" "$T/out"
javac -proc:none -nowarn -cp "$G2101" -d "$T/up" tools/yuushya_share_test/yuushyatest/UpstreamShareFormat.java
java -cp "$T/up:$G2101" yuushyatest.UpstreamShareFormat "$T/out"
CPY="$G289:$CP:$BUILT"
javac -proc:none -nowarn -implicit:none -cp "$CPY" -d "$T/mod" "$MOD/src/main/java/com/yuushya/modelling/utils/ShareUtils.java" 2>&1 | grep -v '^Note' || true
javac -proc:none -nowarn -cp "$T/mod:$CPY" -d "$T/test" tools/yuushya_share_test/yuushyatest/ShareUtilsTest.java 2>&1 | grep -v '^Note' || true
java -cp "$T/test:$T/mod:$CPY" yuushyatest.ShareUtilsTest "$T/out" > "$T/java.txt" 2>&1 || true
grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' "$T/java.txt" || true
grep -q '^ALL OK' "$T/java.txt" # a FAIL line or a crash fails the script
