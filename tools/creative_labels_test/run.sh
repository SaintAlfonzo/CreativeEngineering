#!/usr/bin/env bash
# Offline checks for Creative Labels (feature_writeup/sticky_notes.md, BRIEF 6):
#   1. SpotsTest: the host table of 3.3 on real vanilla blocks (full, 1/16 and 2/16 insets, stairs per facing and
#      half, slabs, double slab, posts, deny/posts predicates), face frames, floor rotation, piece boxes, hit
#      resolution, the covered-face rule
#   2. NbtTest: note item NBT, item label lines, label picture stripping (C2), face / note / label round trips,
#      the Mask remap (C7), colour mixing against vanilla's dyeArmor, the note text sanitiser (C6), the four
#      recipe matchers on a 3x3 grid
#   3. NoteShapeTest: the fallback note shape and its placement on every face (paper plane, curl direction, winding),
#      the label model mapping, DonorArt's image maths on the decompiled donor PNGs
#   4. LabelRenderTest: the dyed-icon ramp and pixel packing, the label icon / name view frame on every face,
#      the text rotation quaternion
# Runs against development/creativeLabels/build/classes/java/main (run `./gradlew :creativeLabels:build` first).
# Needs the ATLauncher library folder (Forge's bootstrap deps). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
CP="$ROOT/development/creativeLabels/build/classes/java/main:$CP"
[ -d "$ROOT/development/creativeLabels/build/classes/java/main" ] || { echo "build creativeLabels first"; exit 1; }
mkdir -p "$T/stub" "$T/test"
# FMLEnvironment stub: Bootstrap.bootStrap() and ForgeConfigSpec read it; the real class needs a live ModLauncher.
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
javac -proc:none -nowarn -cp "$T/stub:$CP" -d "$T/test" tools/creative_labels_test/SpotsTest.java tools/creative_labels_test/NbtTest.java tools/creative_labels_test/NoteShapeTest.java tools/creative_labels_test/LabelRenderTest.java 2>&1 | grep -v 'warning\|unchecked\|deprecat' || true
status=0
echo "== 1. spots"
java -cp "$T/stub:$T/test:$CP" SpotsTest 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED|==)|Exception|Error' || status=1
echo "== 2. NBT"
java -cp "$T/stub:$T/test:$CP" NbtTest 2>&1 | grep -E '^(ok|FAIL|ALL|[0-9]+ FAILED|==)|Exception|Error' || status=1
echo "== 3. note shape, label mapping, donor art"
java -cp "$T/stub:$T/test:$CP" NoteShapeTest 2>&1 | grep -E '^(FAIL|ALL|[0-9]+ FAILED|==|skip)|Exception|Error' || status=1
echo "== 4. icon dye, label layout"
java -cp "$T/stub:$T/test:$CP" LabelRenderTest 2>&1 | grep -E '^(FAIL|ALL|[0-9]+ FAILED|==)|Exception|Error' || status=1
exit $status
