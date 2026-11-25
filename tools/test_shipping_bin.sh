#!/usr/bin/env bash
# Offline check of the Shipping Bin sale arithmetic (TODO mod-integration #7, feature_writeup/farming_for_blockheads.md 7,
# ruling D3a): Creative Delight's farm/shippingbin/SaleMath (plain Java, no game classes) against
# tools/shipping_bin_test/SaleMathTest.java. Build first: ./gradlew :creativeDelight:compileJava --offline
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
CLS="$ROOT/development/creativeDelight/build/classes/java/main"
cd "$ROOT"
javac -proc:none -cp "$CLS" -d "$T" tools/shipping_bin_test/SaleMathTest.java
java -cp "$T:$CLS" SaleMathTest
