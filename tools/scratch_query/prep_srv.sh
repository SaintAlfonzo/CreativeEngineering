#!/usr/bin/env bash
# Builds a scratch server for a bulk queue run (feature_writeup/testing/scratch_run_2026-10-31.md).
#   prep_srv.sh OUT LEVEL A|B [prop=value ...]
# scratch_server.sh setup, then:
#   - every jar HARD-LINKED (cp -l) instead of symlinked, so a parallel session's installToMods or rebuild cannot
#     break a booting server (installToMods moves the old file; the link keeps it); at the end own jars are COPIED
#     and pinned (qpin.py: OUT/pinned.json; qrun.py run refuses a server whose pinned jars changed)
#   - dev jars newer than mods/ swapped in (SWAPS below: build/libs jar replaces the mods/ jar of the same mod)
#   - the dev-only perfBot jar added (bots = fake players; never in the instance's mods/), -Dcdevperfbot.out=perf/bot
#   - tools/scratch_query/datapack/zq (tag #zq:villagers) copied in; the /zq probe (zq_probe.js) is retired and not
#     copied: every reusable probe verb is /cdevtest in the Creative Devtools jar
#   - group B: normal world, structures on, seed 8675309 unless level-seed= is given
#   - prop=value pairs written into server.properties (difficulty=normal, spawn-monsters=true, ...)
#   - NOSWAP=1 in the environment: no dev-jar swaps (the installed mods/ jars only)
#   - SKIPSWAP="creativecompat creativedelight": no swap for those mods (glob prefix), e.g. a build another session is
#     still rewriting
#   - COPYDEV=1: swapped dev jars are COPIED, not hard-linked (use while other sessions run Gradle builds)
#   - DROPJARS="glob ...": remove matching jars from srv/mods before the pin (unlisted jars another session added)
#   - OVERRIDEJARS="path ...": copy each jar into srv/mods over the jar of the same file name (after the swaps), e.g. the
#     last good build of a non-target own jar whose installed build breaks the boot (r1125pub: Creative Delight)
#   - PGJAR=<path>: also copy that Power Grid jar (never in mods/; the orchestrator names it in jar_archive/pwg_gate/);
#     PGJAR=1 takes the newest development/backportedPowerGrid build jar instead
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
# newest build jar of a project (no -own / -sources / -overlay), or a non-existent path
newest() { ls -t "$INST"/development/$1/build/libs/$2-*.jar 2>/dev/null | grep -vE -- '-(own|sources|overlay)\.jar$' | head -1 || echo "$INST/development/$1/build/libs/none"; }
# dev jars: "glob in srv/mods to drop|build jar to use"; applied only when the build jar is newer than the mods/ one
SWAPS=(
  # creativecommon is the shared core; creativeengineering is the Engineering jar
  "creativecommon-*.jar|$(newest creativeCommon creativecommon)"
  "creativeengineering-*.jar|$(newest creativeEngineering creativeengineering)"
  "creativeequipment-*.jar|$(newest creativeEquipment creativeequipment)"
  "creativemagic-*.jar|$(newest creativeMagic creativemagic)"
  "creativedelight-*.jar|$(newest creativeDelight creativedelight)"
  # Veggies, Vintage, Fungi, Miner's and Display are nested in the CD jar (plan_creative_delight.md H): no standalone
  # swap rows for them (a rebuilt member reaches the scratch server through a rebuilt CD jar)
  "creativebrewing-*.jar|$INST/development/creativeBrewing/build/libs/creativebrewing-1.0.0.jar"
  "creativecompat-*.jar|$INST/development/creativeCompat/build/libs/creativecompat-1.0.0.jar"
  "creativecalendar-*.jar|$INST/development/creativeCalendar/build/libs/creativecalendar-1.0.0.jar"
  "creativedevtools-*.jar|$INST/development/creativeDevtools/build/libs/creativedevtools-1.0.0.jar"
  "repackedStorage-*.jar|$INST/development/repackedStorage/build/libs/repackedStorage-25.12.09-1.19-forge-ce-19.2-01.jar"
  "creativeloot-*.jar|$(newest creativeLoot creativeloot)"
  "creativeworldgen-*.jar|$INST/development/creativeWorldgen/build/libs/creativeworldgen-1.0.0.jar"
  "creativeLabels-*.jar|$(ls -t "$INST"/development/creativeLabels/build/libs/creativeLabels-*.jar | grep -v -- -sources | head -1)"
)
rm -f mods/creative_delight-*.jar mods/creative_brewing-*.jar
[ "${NOSWAP:-0}" = 1 ] && { SWAPS=(); echo "swap: off (NOSWAP=1)"; }
for s in "${SWAPS[@]+"${SWAPS[@]}"}"; do
  g=${s%%|*}; b=${s#*|}
  skip=0; for k in ${SKIPSWAP:-}; do case "$g" in "$k"-*) skip=1;; esac; done
  [ "$skip" = 1 ] && { echo "swap: skipped $g (SKIPSWAP)"; continue; }
  [ -f "$b" ] || { echo "swap: no build jar $b"; continue; }
  old=$(ls mods/$g 2>/dev/null | head -1 || true)
  if [ -n "$old" ] && ! [ "$b" -nt "$INST/mods/$(basename "$old")" ]; then
    echo "swap: kept $(basename "$old") (build jar not newer)"; continue
  fi
  # unlink the target name too: srv/mods files are hard links to the instance's mods/, so a plain cp onto an existing
  # name writes THROUGH the link into the pack's jar (r1106cbb: the pack's creativecommon jar was overwritten that way)
  rm -f mods/$g "mods/$(basename "$b")"
  if [ "${COPYDEV:-0}" = 1 ]; then cp "$b" mods/; else cp -l "$b" mods/ 2>/dev/null || cp "$b" mods/; fi
  echo "swap: $(basename "${old:-none}") -> $(basename "$b") ($(date -r "$b" +%m-%d_%H:%M))"
done
# Creative Brewing nests the Vinery and Brewin' ports: their standalone jars (and what they replace) would win over it
if ls mods/creativebrewing-*.jar >/dev/null 2>&1; then
  rm -f mods/backportedVinery-*.jar mods/backportedBrewin-*.jar mods/letsdo-vinery-forge-1.3.10.jar mods/doapi-*.jar mods/BrewinAndChewin-1.19-*.jar
fi
# Creative Delight nests Baby Mobs and Incubation Additions (creative_split.md 7.8) and the five ports
# (Veggies, Vintage, Fungi, Miner's, Display): a standalone jar would win over the nested one
for cd in mods/creativedelight-*.jar; do
  [ -f "$cd" ] || continue
  for m in babyMobs incubationAdditions backportedVeggies backportedVintage backportedFungi backportedMiners backportedDisplay; do
    if unzip -l "$cd" | grep -q "META-INF/jarjar/$m-"; then rm -f mods/$m-*.jar; echo "nested in CD: dropped standalone $m"; fi
  done
done
# Power Grid: only by PGJAR. A copy in the instance mods/ has no mod_sides.md row (r1113 section 11):
# drop any copy prep linked in, so queue servers never carry an unlisted port jar.
rm -f mods/backportedPowerGrid-*.jar
# PGJAR is resolved against the instance root (the script runs in OUT/srv by now; r1115 a relative path was skipped silently)
case "${PGJAR:-}" in ""|1|/*) ;; *) PGJAR="$INST/$PGJAR" ;; esac
if [ -n "${PGJAR:-}" ] && [ "$PGJAR" != 1 ] && [ ! -f "$PGJAR" ]; then echo "power grid: PGJAR NOT FOUND: $PGJAR"; fi
if [ -f "${PGJAR:-}" ]; then
  cp -p "$PGJAR" mods/; echo "power grid: $(basename "$PGJAR") ($(date -r "$PGJAR" +%m-%d_%H:%M:%S), sha1 $(sha1sum "$PGJAR" | cut -c1-12))"
elif [ "${PGJAR:-0}" = 1 ]; then
  pg=$(ls -t "$INST"/development/backportedPowerGrid/build/libs/*.jar 2>/dev/null | grep -vE -- '-(sources|own|dev|slim)\.jar$' | head -1)
  if [ -n "$pg" ]; then cp -p "$pg" mods/; echo "power grid: $(basename "$pg") ($(date -r "$pg" +%m-%d_%H:%M))"; else echo "power grid: NO build jar"; fi
fi
PB="$INST/development/creativeDevtools/members/perfbot/build/libs/cdevperfbot-0.1.0.jar"
[ -f "$PB" ] && { cp "$PB" mods/; echo "-Dcdevperfbot.out=perf/bot" >> user_jvm_args.txt; echo "perfBot added"; }
# Creative Delight's wild crop worldgen source (kubejs/data/creativedelight/worldgen) is dev-only: the jar build splits it
# into data/ and gated packs/worldgen/<mod>/ (framework #13), and build_deploy.py never ships it. The ungated copy here
# would load beside the jar and fail a boot without the mod it names (framework #16, r1114 SQ-CDW-01), so drop it as
# the deploy does. A worldgen JSON edit reaches a scratch server through a rebuilt Creative Delight jar.
rm -rf kubejs/data/creativedelight/worldgen
# Scratch-only data (zq namespace): entity tag #zq:villagers (vanilla + MCA) for
# selectors
mkdir -p kubejs/data && cp -r "$INST/tools/scratch_query/datapack/zq" kubejs/data/
setprop() { local k=$1 v=$2; if grep -q "^$k=" server.properties; then sed -i "s|^$k=.*|$k=$v|" server.properties; else echo "$k=$v" >> server.properties; fi; }
if [ "$GROUP" = B ]; then
  setprop level-type 'minecraft\:normal'; setprop generate-structures true; setprop level-seed 8675309
fi
for kv in "$@"; do setprop "${kv%%=*}" "${kv#*=}"; done
# DROPJARS="glob ...": drop these jars from srv/mods (r1123cc: jars the owner put in mods/ that night, no mod_sides.md
# row yet, kept the publish run on the known pack instead of adding rows from a runner)
for g in ${DROPJARS:-}; do for j in mods/$g; do [ -e "$j" ] && { rm -f "$j"; echo "dropjars: removed ${j#mods/}"; }; done; done
# OVERRIDEJARS="path ...": a named jar replaces the srv/mods jar of the same file name (r1125pub: the installed Creative
# Delight build failed every full-pack boot, so the run used the last good one). rm first: srv/mods names are hard links
for o in ${OVERRIDEJARS:-}; do
  case "$o" in /*) ;; *) o="$INST/$o" ;; esac
  if [ -f "$o" ]; then rm -f "mods/$(basename "$o")"; cp -p "$o" mods/; echo "override: $(basename "$o") from $o (sha1 $(sha1sum "$o" | cut -c1-12))"
  else echo "override: NOT FOUND: $o"; fi
done
# pin (plan_process_overhaul.md P2.4): own jars become real copies, OUT/pinned.json records their stamps; qrun.py run
# refuses to start when they change, turn into links again, or an unlisted jar is in mods/
python3 "$INST/tools/scratch_query/qpin.py" pin "$OUT"
python3 "$INST/tools/scratch_query/qpin.py" verify "$OUT" | grep -v '^qpin: OK' || true
echo "prep done: $(ls mods | wc -l) jars, level $LEVEL, group $GROUP"
grep -E '^(level-type|level-seed|difficulty|spawn-monsters|spawn-animals|gamemode)=' server.properties | tr '\n' ' '; echo
