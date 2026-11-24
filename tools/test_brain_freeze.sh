#!/usr/bin/env bash
# Offline check of brain freeze's roll window (TODO sweets #9, feature_writeup/smoothies.md section 6): Creative Common's
# effect/BrainFreezeRoll (plain Java, no game classes) against tools/brain_freeze_test/BrainFreezeRollTest.java.
# Build first: ./gradlew :creativeCommon:compileJava --offline
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
CLS="$ROOT/development/creativeCommon/build/classes/java/main"
cd "$ROOT"
javac -proc:none -cp "$CLS" -d "$T" tools/brain_freeze_test/BrainFreezeRollTest.java
java -cp "$T:$CLS" BrainFreezeRollTest
