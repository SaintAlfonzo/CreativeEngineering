#!/usr/bin/env bash
# Headless checks of the remainder buffer routing (feature_writeup/remainder_buffer.md 3.8 and 6): LeftoverPlan and
# OverflowPull, the shared station logic (development/shared/stations, compiled into Creative Delight as
# com.SaintAlfonzo.creativedelight.shared). Build first: ./gradlew :creativeCommon:compileJava :creativeDelight:compileJava --offline
# No game needed. The pot / kettle / oven GameTests (server boot, stale since the carve) are tools/gametest_remainder_buffer.sh.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
CP="$CP:$ROOT/development/creativeDelight/build/classes/java/main"
javac -proc:none -nowarn -cp "$CP" -d "$T" tools/remainder_buffer_test/headless/RemainderLogicTest.java
java -cp "$T:$CP" RemainderLogicTest 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error'
