#!/bin/bash
# Dict baseline (feature_writeup/plan_cb_publish.md 2.2): loads the given startup-script folders headless and prints
# per-global key counts and hashes. Compare the output before and after a dict move: everything but "order" must match.
# Usage: tools/dict_baseline/run.sh <folder> [<folder> ...]
set -e
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
CP="$ROOT/mods/rhino-forge-1902.2.3-build.284.jar:$HOME/.local/share/atlauncher/libraries/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
OUT="${TMPDIR:-/tmp}/dict_baseline_classes"
mkdir -p "$OUT"
javac -proc:none -nowarn -cp "$CP" -d "$OUT" "$ROOT/tools/dict_baseline/DictBaseline.java"
java -cp "$CP:$OUT" DictBaseline "$@"
