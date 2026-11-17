#!/usr/bin/env bash
# Headless harness for /cdevtest's pure classes (TestArgs, TestLine): compiles them with the harness into a temp dir
# and runs it. No game, no Gradle. Usage: bash tools/scratch_query/cdevtest_harness/run.sh
set -euo pipefail
INST="$(cd "$(dirname "$0")/../../.." && pwd)"
SRC="$INST/development/creativeDevtools/src/main/java/com/SaintAlfonzo/creativedevtools/test"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" "$SRC/TestArgs.java" "$SRC/TestLine.java" "$(dirname "$0")/CdevtestHarness.java"
java -cp "$OUT" CdevtestHarness
