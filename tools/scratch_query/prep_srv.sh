#!/usr/bin/env bash
# Builds a scratch server for a bulk queue run (feature_writeup/testing/scratch_run_2026-10-31.md).
#   prep_srv.sh OUT LEVEL A|B [prop=value ...]
# scratch_server.sh setup, then:
#   - every jar HARD-LINKED (cp -l) instead of symlinked, so a parallel session's installToMods or rebuild cannot
#     break a booting server (installToMods moves the old file; the link keeps it)
#   - dev jars newer than mods/ swapped in (SWAPS below: build/libs jar replaces the mods/ jar of the same mod)
#   - the dev-only perfBot jar added (bots = fake players; never in the instance's mods/), -Dceperfbot.out=perf/bot
#   - /zq probe copied in as a scratch-only server script, plus tools/scratch_query/datapack/zq (tag #zq:villagers)
#   - group B: normal world, structures on, seed 8675309 unless level-seed= is given
#   - prop=value pairs written into server.properties (difficulty=normal, spawn-monsters=true, ...)
#   - NOSWAP=1 in the environment: no dev-jar swaps (the installed mods/ jars only)
# Refuses while any game JVM is up (one game JVM at a time).
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; LEVEL=${2:?LEVEL}; GROUP=${3:?A or B}; shift 3
if [ -n "$(bash "$INST/tools/scratch_query/game_jvms.sh" 2>/dev/null)" ]; then
  echo "a game JVM is up: not building over it" >&2; exit 1
fi
bash "$INST/tools/creative_labels_test/scratch_server.sh" setup "$OUT" "$LEVEL"
SRV="$OUT/srv"
cd "$SRV" || exit 1
# symlinks -> hard links (falls back to a copy across filesystems)
for l in mods/*.jar; do
  [ -L "$l" ] || continue
  t=$(readlink -f "$l"); rm "$l"; cp -l "$t" "$l" 2>/dev/null || cp "$t" "$l"
done
# dev jars: "glob in srv/mods to drop|build jar to use"; applied only when the build jar is newer than the mods/ one
SWAPS=(
  "creativeengineering-*.jar|$(ls -t "$INST"/development/patcherMod/build/libs/creativeengineering-*.jar | head -1)"
  "creativedelight-*.jar|$INST/development/creativeDelight/build/libs/creativedelight-1.0.0.jar"
  # Veggies, Vintage, Fungi, Miner's and Display are nested in the CD jar since 2026-11-02 (plan_creative_delight.md H):
  # no standalone swap rows for them (a rebuilt member reaches the scratch server through a rebuilt CD jar)
  "creativebrewing-*.jar|$INST/development/creativeBrewing/build/libs/creativebrewing-1.0.0.jar"
  "creativecompat-*.jar|$INST/development/creativeCompat/build/libs/creativecompat-1.0.0.jar"
  "creativecalendar-*.jar|$INST/development/creativeCalendar/build/libs/creativecalendar-1.0.0.jar"
  "creativedevtools-*.jar|$INST/development/creativeDevtools/build/libs/creativedevtools-1.0.0.jar"
  "repackedStorage-*.jar|$INST/development/repackedStorage/build/libs/repackedStorage-25.12.09-1.19-forge-ce-19.2-01.jar"
  "creativeworldgen-*.jar|$INST/development/creativeWorldgen/build/libs/creativeworldgen-1.0.0.jar"
  "creativeLabels-*.jar|$(ls -t "$INST"/development/creativeLabels/build/libs/creativeLabels-*.jar | grep -v -- -sources | head -1)"
)
# Creative Delight and Creative Brewing had underscored ids up to 2026-11-01 (creative_split.md 7.7): drop the old jars
rm -f mods/creative_delight-*.jar mods/creative_brewing-*.jar
[ "${NOSWAP:-0}" = 1 ] && { SWAPS=(); echo "swap: off (NOSWAP=1)"; }
for s in "${SWAPS[@]+"${SWAPS[@]}"}"; do
  g=${s%%|*}; b=${s#*|}
  [ -f "$b" ] || { echo "swap: no build jar $b"; continue; }
  old=$(ls mods/$g 2>/dev/null | head -1 || true)
  if [ -n "$old" ] && ! [ "$b" -nt "$INST/mods/$(basename "$old")" ]; then
    echo "swap: kept $(basename "$old") (build jar not newer)"; continue
  fi
  rm -f mods/$g; cp -l "$b" mods/ 2>/dev/null || cp "$b" mods/
  echo "swap: $(basename "${old:-none}") -> $(basename "$b") ($(date -r "$b" +%m-%d_%H:%M))"
done
# Creative Brewing nests the Vinery and Brewin' ports: their standalone jars (and what they replace) would win over it
if ls mods/creativebrewing-*.jar >/dev/null 2>&1; then
  rm -f mods/backportedVinery-*.jar mods/backportedBrewin-*.jar mods/letsdo-vinery-forge-1.3.10.jar mods/doapi-*.jar mods/BrewinAndChewin-1.19-*.jar
fi
# Creative Delight nests Baby Mobs and Incubation Additions since wave 4 (creative_split.md 7.8) and the five ports
# (Veggies, Vintage, Fungi, Miner's, Display) since 2026-11-02: a standalone jar would win over the nested one
for cd in mods/creativedelight-*.jar; do
  [ -f "$cd" ] || continue
  for m in babyMobs incubationAdditions backportedVeggies backportedVintage backportedFungi backportedMiners backportedDisplay; do
    if unzip -l "$cd" | grep -q "META-INF/jarjar/$m-"; then rm -f mods/$m-*.jar; echo "nested in CD: dropped standalone $m"; fi
  done
done
PB="$INST/development/creativeDevtools/members/perfbot/build/libs/ceperfbot-0.1.0.jar"
[ -f "$PB" ] && { cp "$PB" mods/; echo "-Dceperfbot.out=perf/bot" >> user_jvm_args.txt; echo "perfBot added"; }
cp "$INST/tools/scratch_query/zq_probe.js" kubejs/server_scripts/zz_zq_probe.js
# scratch-only data (zq namespace): entity tag #zq:villagers (vanilla + MCA) for selectors
mkdir -p kubejs/data && cp -r "$INST/tools/scratch_query/datapack/zq" kubejs/data/
setprop() { local k=$1 v=$2; if grep -q "^$k=" server.properties; then sed -i "s|^$k=.*|$k=$v|" server.properties; else echo "$k=$v" >> server.properties; fi; }
if [ "$GROUP" = B ]; then
  setprop level-type 'minecraft\:normal'; setprop generate-structures true; setprop level-seed 8675309
fi
for kv in "$@"; do setprop "${kv%%=*}" "${kv#*=}"; done
echo "prep done: $(ls mods | wc -l) jars, level $LEVEL, group $GROUP"
grep -E '^(level-type|level-seed|difficulty|spawn-monsters|spawn-animals|gamemode)=' server.properties | tr '\n' ' '; echo
