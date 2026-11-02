#!/usr/bin/env bash
# Creative Split waves 2, 3 and 4 scratch run (feature_writeup/testing/scratch_run_2026-11-01.md 7).
# One group B world upgraded across three jar sets, the way a player's save goes:
#   [LEVEL=name] split_w234.sh prep1 OUT   old set: patcherMod 1.1.05, creative_delight / creative_brewing (underscore ids), the
#                             committed (HEAD) kubejs/, config/ and defaultconfigs/, one edited value per moved config
#   split_w234.sh prep2 OUT   waves 2 and 3: the installed mods/ jars (1.1.06, creativedelight, creativebrewing,
#                             creativecompat), the instance's current kubejs/; srv config kept (upgrade path)
#   split_w234.sh prep3 OUT   wave 4: build/libs jars (1.1.07, CD nesting Baby Mobs and Incubation Additions, CB);
#                             edited values in the CE file's sections that wave 4 moves to CB and CD
# Boot with tools/creative_labels_test/scratch_server.sh boot OUT; stop before the next prep.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
what=${1:?prep1|prep2|prep3}; OUT=${2:?OUT}; SRV="$OUT/srv"
if [ -n "$(bash "$INST/tools/scratch_query/game_jvms.sh" 2>/dev/null)" ]; then
  echo "a game JVM is up: not changing jars under it" >&2; exit 1
fi
QR="python3 $INST/tools/scratch_query/qrun.py toml"
# plain copies: a hard link shares the file with build/libs, and a parallel build rewrites it in place (seen 2026-11-01)
link() { rm -f "$SRV/mods/$(basename "$1")"; cp "$1" "$SRV/mods/"; echo "jar: $(basename "$1") ($(date -r "$1" +%m-%d_%H:%M))"; }
probe() {
  cp "$INST/tools/scratch_query/zq_probe.js" "$SRV/kubejs/server_scripts/zz_zq_probe.js"
  mkdir -p "$SRV/kubejs/data" && cp -r "$INST/tools/scratch_query/datapack/zq" "$SRV/kubejs/data/"
}
case "$what" in
prep1)
  NOSWAP=1 bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" "${LEVEL:-cs234}" B
  cd "$SRV" || exit 1
  # the installed Calendar and Devtools are wave 3 builds (require Compat); their wave 1 jars were not archived, and
  # boot 1 only has to write a save with the old ids
  rm -f mods/creativeengineering-*.jar mods/creativedelight-*.jar mods/creativebrewing-*.jar mods/creativecompat-*.jar \
    mods/creativecalendar-*.jar mods/creativedevtools-*.jar
  link "$INST/development/patcherMod/jar_archive/displaced/creativeengineering-1.1.05.jar"
  link "$INST/jar_archive/displaced/creative_delight-1.0.0.jar"
  link "$INST/jar_archive/displaced/creative_brewing-1.0.0.jar"
  # the old CD and CB require Calendar: a copy of the installed one without its Compat dependency block (the only
  # change to Calendar since HEAD)
  python3 -I - "$INST/mods/creativecalendar-1.0.0.jar" mods/creativecalendar-1.0.0.jar <<'PY'
import re, sys, zipfile
src, dst = sys.argv[1:3]
with zipfile.ZipFile(src) as zi, zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zo:
    for it in zi.infolist():
        data = zi.read(it.filename)
        if it.filename == "META-INF/mods.toml":
            t = data.decode()
            t2 = re.sub(r'(#[^\n]*\n)?\[\[dependencies\.creativecalendar\]\]\nmodId="creativecompat"(\n[^\[#][^\n]*)*\n', "", t)
            assert t2 != t, "no compat block"
            data = t2.encode()
        zo.writestr(it, data)
PY
  echo "jar: creativecalendar-1.0.0.jar (installed, Compat dependency removed)"
  rm -rf kubejs config defaultconfigs
  git -C "$INST" archive HEAD kubejs config defaultconfigs | tar -x
  probe
  # SQ-CMP: no Compat file yet, one edited value in a section Compat takes over
  rm -f config/creativecompat-common.toml
  $QR config/creativeengineering-common.toml campfire rainOutTicks 200
  # SQ-CS2-05: one edited value per underscore file (CB's file has only [members]: a marker comment instead)
  $QR config/creative_delight-common.toml qol placeableTooltips false
  sed -i '1i # scratch marker cs234' config/creative_brewing-common.toml
  echo "prep1 done: $(ls mods | wc -l) jars"
  ;;
prep2)
  cd "$SRV" || exit 1
  rm -f mods/creativeengineering-*.jar mods/creative_delight-*.jar mods/creative_brewing-*.jar
  for j in creativeengineering-1.1.06 creativedelight-1.0.0 creativebrewing-1.0.0 creativecompat-1.0.0 \
      creativecalendar-1.0.0 creativedevtools-1.0.0; do link "$INST/mods/$j.jar"; done
  rm -rf kubejs && cp -r "$INST/kubejs" kubejs && probe
  echo "prep2 done: $(ls mods | wc -l) jars"
  ;;
prep3)
  cd "$SRV" || exit 1
  rm -f mods/creativeengineering-*.jar mods/creativedelight-*.jar mods/creativebrewing-*.jar mods/creativecompat-*.jar \
    mods/creativecalendar-*.jar mods/creativedevtools-*.jar mods/creative_delight-*.jar mods/creative_brewing-*.jar
  link "$INST/development/patcherMod/build/libs/creativeengineering-1.1.07.jar"
  for p in creativeDelight/build/libs/creativedelight creativeBrewing/build/libs/creativebrewing \
      creativeCompat/build/libs/creativecompat creativeCalendar/build/libs/creativecalendar \
      creativeDevtools/build/libs/creativedevtools; do link "$INST/development/$p-1.0.0.jar"; done
  rm -f mods/babyMobs-*.jar mods/incubationAdditions-*.jar
  rm -rf kubejs && cp -r "$INST/kubejs" kubejs && probe
  # SQ-CBJ4 / SQ-CDJ4: edited values in the sections wave 4 moves out of the CE file (merge path)
  CE=config/creativeengineering-common.toml
  $QR $CE alcohol quarkLine false
  $QR $CE magic_resistance reductionPerLevel 0.3
  $QR $CE copper_poisoning intervalSeconds 10
  $QR $CE copper_poisoning chancePerPiece 1.0
  $QR $CE canning emptyCanChance 0.5
  $QR $CE perf troughMissCooldownTicks 300
  echo "prep3 done: $(ls mods | wc -l) jars"
  ;;
*) echo "prep1|prep2|prep3" >&2; exit 2 ;;
esac
