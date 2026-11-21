#!/usr/bin/env bash
# Offline check of NCH.ieBlueprint's input merge (tooling/conventions #7, plan_small_fixes_1116.md P1): runs Creative
# Common's real development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js (with
# func/has.js) in the pack's own Rhino fork with stubs (tools/nch_test/prelude.js) and asserts that repeated identical
# inputs become one counted ingredient while the recipe id stays the same. No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
cd "$ROOT"
RHINO="$(ls mods/rhino-forge-*.jar | head -1)"; GSON="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
javac -proc:none -cp "$RHINO:$GSON" -d "$T" tools/nch_test/NchRun.java
C=development/creativeCommon/src/main/resources/kubejs_startup/creativecommon
OUT="$(java -cp "$T:$RHINO:$GSON" NchRun "$ROOT" tools/nch_test/prelude.js "$C/func/has.js" "$C/func/neoCraftHelper.js" tools/nch_test/blueprint_test.js)"
# creativebrewing/publish #25: a tag in a result slot is refused (fresh scope, same stubs)
OUT="$OUT
$(java -cp "$T:$RHINO:$GSON" NchRun "$ROOT" tools/nch_test/prelude.js "$C/func/has.js" "$C/func/neoCraftHelper.js" tools/nch_test/result_tag_test.js)"
echo "$OUT"
case "$OUT" in *FAIL*) exit 1;; esac
