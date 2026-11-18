#!/usr/bin/env bash
# Offline checks for Common's own config watcher (plan_post_scratch_1115.md G11, feature_writeup/creative_devtools.md "Own config watcher"):
# ConfigWatcherTest runs OwnConfigLogic (own mod ids, 1 s debounce, content guard) and checks that a JDK WatchService
# registered for MODIFY and CREATE sees an in-place save and a rename save. Compiles
# development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/config/OwnConfigLogic.java (Forge-free) with
# the test; no game, no build/classes needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
cd "$ROOT"
javac -proc:none -nowarn -d "$T" \
  development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/config/OwnConfigLogic.java \
  tools/config_watcher_test/com/SaintAlfonzo/creativecommon/config/ConfigWatcherTest.java
java -cp "$T" com.SaintAlfonzo.creativecommon.config.ConfigWatcherTest > "$T/java.txt" 2>&1 || true
grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error' "$T/java.txt" || true
grep -q '^ALL [0-9]* OK' "$T/java.txt"
