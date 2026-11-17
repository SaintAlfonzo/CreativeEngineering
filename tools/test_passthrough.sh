#!/usr/bin/env bash
# Offline checks for pass-through blocks, phases 1, 2, 3 and 5 (feature_writeup/plan_passthrough.md 8): tier defaults, rule results
# (ring 0/5/6/8, cube 0/5/6/25/26), neighbour counts, growth rules (one- and two-block crops, hemp enum, rice panicles),
# minLayers, fall distance per block passed, modifier / wading / field of view math, item stick point, tag ids, the
# phase 2 pure logic (multi-cell item stick path, surrounded climb, arrow despawn limit and cap pick, projectile group
# defaults and tag ids), phases 3 and 5 (the [leaf_piles] -> [surface.passthrough.leaf_pile] minLayers move, on fixtures
# and a copy of the live config; the WTHIT Walk Through / Fall Through / Neighbors format; StuckInBlock untiered only),
# [surface.passthrough] spec and the sync packet; scratch fixes 2026-11-15 (fast-fall reset clip, break stall, region counts,
# per-tick count cache); the 1115b rework (plan_post_scratch_1115b.md: lone / stacked / dense web fall, breakStallTicks, 4 / 6
# neighbour model, baked chunk counts and their invalidation keying, growth vs neighbours, config migration, sync format 2,
# layer height rule). Runs against development/creativeCompat/build/classes: build first
# (./gradlew :creativeCompat:build --offline). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
CP="$CP:$ROOT/development/creativeCompat/build/classes/java/main"
mkdir -p "$T/stub" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
javac -proc:none -nowarn -cp "$T/stub:$CP" -d "$T/test" tools/passthrough_test/com/SaintAlfonzo/creativecompat/surface/*.java 2>&1 \
  | grep -E 'error' || true
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativecompat.surface.PassThroughTest 2>&1 \
  | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED)|Exception|Error|at com\.Saint' || true
java -cp "$T/stub:$T/test:$CP" com.SaintAlfonzo.creativecompat.surface.PassThroughTest > /dev/null 2>&1
