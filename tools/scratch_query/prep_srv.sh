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
  "creativeengineering-*.jar|$INST/development/patcherMod/build/libs/creativeengineering-1.1.04.jar"
  "creative_delight-*.jar|$INST/development/creativeDelight/build/libs/creative_delight-1.0.0.jar"
  "backportedVintage-*.jar|$INST/development/backportedVintage/build/libs/backportedVintage-0.1.6-ce-19.2-09.jar"
  "backportedVeggies-*.jar|$INST/development/backportedVeggies/build/libs/backportedVeggies-1.9.2-ce-19.2-03.jar"
  "creative_brewing-*.jar|$INST/development/creativeBrewing/build/libs/creative_brewing-1.0.0.jar"
  "repackedStorage-*.jar|$INST/development/repackedStorage/build/libs/repackedStorage-25.12.09-1.19-forge-ce-19.2-01.jar"
)
for s in "${SWAPS[@]}"; do
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
if ls mods/creative_brewing-*.jar >/dev/null 2>&1; then
  rm -f mods/backportedVinery-*.jar mods/backportedBrewin-*.jar mods/letsdo-vinery-forge-1.3.10.jar mods/doapi-*.jar mods/BrewinAndChewin-1.19-*.jar
fi
PB="$INST/development/perfBot/build/libs/ceperfbot-0.1.0.jar"
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
