#!/usr/bin/env bash
# Offline checks for Creative Oxidation (feature_writeup/plan_creative_oxidation.md P1 to P4): target ids, the B1 state
# property decision, the B2 random tick decisions, aging chances, item NBT, the creativeoxidation:staged ingredient, and the P3/P4
# decisions (drops, placer items, display name, honeycomb/scraper/soap), and the P8 JEI state changes and subtypes.
# Compiles the member's sources (development/creativeEngineering/members/creativeoxidation/src) itself against the
# mapped Forge jar plus the pack's Create, Flywheel and Registrate jars, so it needs no Gradle build. No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
LIBS="$ROOT/development/creativeCommon/libs"
MIXIN="$(find "$C/org.spongepowered/mixin" -name 'mixin-0.8.5.jar' | head -1)"
CP2="$CP:$LIBS/create-1.19.2-0.5.1.i.jar:$LIBS/flywheel-forge-1.19.2-0.6.11-22.jar:$LIBS/Registrate-MC1.19-1.1.5.jar:$LIBS/jei-1.19.2-forge-11.8.1.1034.jar:$MIXIN"
mkdir -p "$T/stub" "$T/mod" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
# the shared gated mixin plugin (development/shared/mixinplugin), package rewritten as ce.forge-mod's sharedSources does
mkdir -p "$T/shared/com/SaintAlfonzo/creativeoxidation/shared"
sed 's/\bce\.shared\b/com.SaintAlfonzo.creativeoxidation.shared/' development/shared/mixinplugin/ce/shared/GatedMixinPlugin.java > "$T/shared/com/SaintAlfonzo/creativeoxidation/shared/GatedMixinPlugin.java"
javac -proc:none -nowarn -cp "$CP2" -d "$T/mod" $(find development/creativeEngineering/members/creativeoxidation/src/main/java "$T/shared" -name '*.java') 2>&1 | grep -v 'warning\|unchecked\|deprecat\|^Note' || true
javac -proc:none -nowarn -cp "$T/mod:$CP2" -d "$T/test" tools/creative_oxidation_test/com/SaintAlfonzo/creativeoxidation/*.java 2>&1 | grep -v 'warning\|unchecked\|deprecat\|^Note' || true
java -cp "$T/stub:$T/test:$T/mod:$CP2" com.SaintAlfonzo.creativeoxidation.CreativeOxidationTest > "$T/java.txt" 2>&1 || true
grep -E '^(ok|FAIL|skip|ALL|[0-9]+ FAILED)|Exception|Error' "$T/java.txt" || true
grep -q '^ALL [0-9]* OK' "$T/java.txt" # a FAIL line or a crash fails the script
