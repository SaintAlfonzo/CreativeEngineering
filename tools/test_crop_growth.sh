#!/usr/bin/env bash
# Offline checks for the WTHIT crop growth rules (feature_writeup/crop_growth.md):
#   1. CropGrowth maths + rule parsing, against the shipped crop_growth.json (javac/java, only needs gson)
#   2. tools/crop_growth_coverage.py: every growing block in the /dumpcropgrowth audit is ruled, fallback or skipped
# Build the patcherMod classes first: ./gradlew :patcherMod:compileJava --offline
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
GSON="$HOME/.local/share/atlauncher/libraries/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
CLS="$ROOT/development/patcherMod/build/classes/java/main"
RULES="$ROOT/development/patcherMod/src/main/resources/assets/creativeengineering/crop_growth.json"
cd "$ROOT"

echo "== 1. logic"
javac -proc:none -cp "$CLS:$GSON" -d "$T" tools/crop_growth_test/CropGrowthTest.java
java -cp "$T:$CLS:$GSON" CropGrowthTest "$RULES"

echo "== 2. coverage"
python3 tools/crop_growth_coverage.py
