#!/usr/bin/env bash
# Offline checks for the player changelog (feature_writeup/changelog.md 6):
#   1. version ordering + Markdown -> blocks (patcherMod changelog/ classes, headless), and every file in
#      kubejs/assets/creativeengineering/changelog/ parses with no raw Markdown left
#   2. tools/changelog_lint.py on the same folder (+ its own self-test)
#   3. build_deploy.py staging stamp, --snapshot freeze, frozen deployed files (scratch folder)
# Build the patcherMod classes first: ./gradlew :patcherMod:compileJava --offline
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
source "$ROOT/tools/java_test_cp.sh"
CP="$CP:$ROOT/development/patcherMod/build/shade/shade-relocated.jar"
cd "$ROOT"

echo "== 1. logic"
javac -proc:none -cp "$CP" -d "$T" tools/changelog_test/ChangelogTest.java
java -cp "$T:$CP" ChangelogTest kubejs/assets/creativeengineering/changelog

echo "== 2. lint"
python3 tools/changelog_lint.py --self-test
python3 tools/changelog_lint.py

echo "== 3. deploy steps (scratch folder)"
python3 tools/changelog_test/deploy_test.py
