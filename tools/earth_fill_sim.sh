#!/usr/bin/env bash
# Calibration for CEConfig [worldgen.earth_fill] limits (feature_writeup/structure_tuning.md 3.13): generates real
# vanilla structures on vanilla overworld terrain at random sites in their biomes (no mods, builtin registries, the
# real template and terrain code), counts earth over water / air the way EarthFill does, and prints the distribution
# plus the share each candidate limit pair would cancel. About 7 minutes for 60 sites each; ~1.2 GB heap.
# Usage: tools/earth_fill_sim.sh [seed] [sites per structure]    (run ./gradlew build in patcherMod first)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
V="$HOME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.19.2"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
mkdir -p "$T/stub" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
javac -proc:none -nowarn -cp "$T/stub:$CP" -d "$T/test" tools/worldgen_test/com/SaintAlfonzo/creativeengineering/worldgen/EarthFillSim.java 2>&1 | grep -E 'error' -A3 || true
java -Xmx1200m -cp "$T/stub:$T/test:$CP:$V/client-extra.jar" com.SaintAlfonzo.creativeengineering.worldgen.EarthFillSim "${1:-4242}" "${2:-60}" 2>&1 \
  | grep -v '^\[\|SLF4J\|StatusLogger'
