#!/usr/bin/env bash
# Creative Brewing chain on its own, from the CEPortTesting instance (scratch_queue.md "SQ-CBA").
#   port_chain.sh server OUT LEVEL A|B chain|cooking|optional|instance [prop=value ...]
#       scratch dedicated server (scratch_server.sh setup, then its mods/kubejs/config replaced) built from
#       CEPortTesting/mods: "chain" = only the CHAIN jars below, "cooking" = CHAIN + COOKING (the cooking mods Creative
#       Delight and Creative Brewing integrate with but do not nest, all optional dependencies, and their own required
#       mods), "optional" = CHAIN + OPTIONAL (Creative Brewing's 24 optional dependencies and their required jars, SQ-CBO),
#       "instance" = every non-client jar CEPortTesting has.
#       ADD="glob,glob" (comma-separated: some jar names have spaces) adds jars after the pick (SQ-CBO single-mod boots); a glob not in CEPortTesting/mods or its park
#       folder is taken from the pack's mods/ (read only, hard link or copy into OUT). A glob found nowhere stops the run.
#       kubejs/ = CEPortTesting's (examples only, no pack scripts) plus the /zq and /cbo probes; config/ starts EMPTY (the jars
#       write their defaults) unless KEEPCFG=1 (copy CEPortTesting's). Donor folders are CEPortTesting's *_upstream;
#       NOUPSTREAM="vinery_upstream ..." leaves those out. DROP="glob glob" removes jars after the pick (missing-dependency
#       rows).
#   port_chain.sh bare [cooking|optional]   CEPortTesting client: move every jar outside CHAIN (or CHAIN + COOKING,
#                           CHAIN + OPTIONAL) to disabledmods/cba_parked/ (game closed); "optional" also copies the
#                           OPTIONAL jars CEPortTesting lacks from the pack's mods/ (listed in cba_parked/.added_from_pack)
#   port_chain.sh restore   delete the jars copied from the pack, move the parked ones back
#   port_chain.sh check     CHAIN jars present in CEPortTesting/mods; Creative jars older than the pack's mods/ copy
# Refuses server/bare/restore while any game JVM is up (one game JVM at a time). Writes nothing into the pack.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
PORT="$INST/../CEPortTesting"
PARK="$PORT/disabledmods/cba_parked"
# Hard dependencies of Creative Brewing, all the way down (plan_cb_publish.md 1.2 and 14; nested members' mods.toml
# checked 2026-11-06: Vinery needs creativecalendar + foundation, Brewin' farmersdelight, both nested; MoreJS required by
# Creative Brewing since 2026-11-06).
CHAIN=(
  "rhino-forge-*.jar" "architectury-*-forge.jar" "kubejs-forge-*.jar"
  "create-1.19.2-*.jar" "kubejs-create-forge-*.jar" "kubejsdelight-*.jar" "lootjs-forge-*.jar" "morejs-forge-*.jar"
  "SereneSeasons-*.jar"
  "creativecommon-*.jar" "creativecompat-*.jar" "creativecalendar-*.jar" "creativedelight-*.jar" "creativebrewing-*.jar"
)
# Cooking mods CD / CB integrate with but do not nest or repack (optional dependencies in creativedelight's mods.toml,
# 2026-11-06), then their own required mods. Applied Cooking (AE2) is left out: no CD/CB rows use it.
COOKING=(
  "fruitsdelight-*.jar" "Hostile_Delight_*.jar" "moredelight-*.jar" "abnormals_delight-*.jar" "alexsdelight-*.jar"
  "chefsdelight-*.jar" "cookingforblockheads-*.jar" "corn_delight-*.jar" "create_central_kitchen-*.jar"
  "endersdelight-*.jar" "Engineers Delight *.jar" "FarmersRespite-*.jar" "quark_delight_*.jar"
  "blueprint-*.jar" "alexsmobs-*.jar" "citadel-*.jar" "balm-forge-*.jar" "mysterious_mountain_lib-*.jar"
  "ImmersiveEngineering-*.jar" "Quark-*.jar" "AutoRegLib-*.jar"
)
# Creative Brewing's optional dependencies (its mods.toml, mandatory=false) and their own required jars (SQ-CBO,
# 2026-11-06). Taken from the pack's mods/ when CEPortTesting lacks them.
OPTIONAL=(
  "alexsmobs-*.jar" "citadel-*.jar" "appleskin-*.jar" "CerbonsBetterBeacons-*.jar" "Botania-*.jar" "curios-forge-*.jar"
  "Patchouli-*.jar" "buzzier_bees-*.jar" "blueprint-*.jar" "create_central_kitchen-*.jar" "create_deep_dark-*.jar"
  "dragonloot-*.jar" "everythingcopper-*.jar" "FarmersRespite-*.jar" "Felsic Gear *.jar" "fruitsdelight-*.jar"
  "geodeforge-*.jar" "immersive_armors-*.jar" "immersive_weathering-*.jar" "moonlight-*.jar"
  "ImmersiveEngineering-*.jar" "jeed-*.jar" "jei-*.jar" "neapolitan-*.jar" "potionbundles-*.jar" "Quark-*.jar"
  "AutoRegLib-*.jar" "supplementaries-*.jar" "Engineers Delight *.jar" "upgrade_aquatic-*.jar"
)
# add_glob GLOB: copy the newest match from CEPortTesting/mods, its park folder, else the pack's mods/ into ./mods
add_glob() {
  local g=$1 j="" d c p=${1// /?}   # spaces as '?': an unquoted glob with a space splits into words
  for d in "$PORT/mods" "$PARK" "$INST/mods"; do
    for c in "$d"/$p; do [ -f "$c" ] && { [ -z "$j" ] || [ "$c" -nt "$j" ]; } && j=$c; done
    [ -n "$j" ] && break
  done
  [ -n "$j" ] || { echo "ADD: no jar for '$g' in CEPortTesting or the pack" >&2; exit 1; }
  b=$(basename "$j"); rm -f "mods/$b"; cp -l "$j" mods/ 2>/dev/null || cp "$j" mods/; echo "added: $b"
}
in_chain() { local b=$1 g; for g in "${CHAIN[@]}"; do [[ $b == $g ]] && return 0; done; return 1; }
in_optional() { local b=$1 g; for g in "${OPTIONAL[@]}"; do [[ $b == $g ]] && return 0; done; return 1; }
in_cooking() { local b=$1 g; for g in "${COOKING[@]}"; do [[ $b == $g ]] && return 0; done; return 1; }
no_jvm() {
  if [ -n "$(bash "$INST/tools/scratch_query/game_jvms.sh" 2>/dev/null)" ]; then
    echo "a game JVM is up: not touching jars" >&2; exit 1
  fi
}
check() {
  local g ok=0
  for g in "${CHAIN[@]}"; do
    # shellcheck disable=SC2086
    ls "$PORT"/mods/$g >/dev/null 2>&1 || { echo "MISSING in CEPortTesting/mods: $g"; ok=1; }
  done
  for g in "${COOKING[@]}"; do
    compgen -G "$PORT/mods/$g" >/dev/null || compgen -G "$PARK/$g" >/dev/null || echo "MISSING (cooking set only): $g"
  done
  for j in "$PORT"/mods/creative*.jar; do
    p="$INST/mods/$(basename "$j")"
    [ -f "$p" ] && [ "$p" -nt "$j" ] && echo "STALE: $(basename "$j") (pack mods/ copy is newer: copy it over)"
  done
  [ -d "$PORT/vinery_upstream" ] || echo "MISSING: CEPortTesting/vinery_upstream"
  return $ok
}
what=${1:?usage: port_chain.sh server|bare|restore|check ...}
case "$what" in
check) check ;;
bare)
  no_jvm; mkdir -p "$PARK"
  n=0; for j in "$PORT"/mods/*.jar; do b=$(basename "$j"); in_chain "$b" && continue
    [ "${2:-}" = cooking ] && in_cooking "$b" && continue
    [ "${2:-}" = optional ] && in_optional "$b" && continue; mv "$j" "$PARK/"; n=$((n+1)); done
  echo "parked $n jars in disabledmods/cba_parked"
  if [ "${2:-}" = optional ]; then
    # optional jars CEPortTesting lacks: copied from the pack's mods/, listed so restore deletes exactly these
    for g in "${OPTIONAL[@]}"; do
      p=${g// /?}; have=""; for c in "$PORT"/mods/$p; do [ -f "$c" ] && have=1; done; [ -n "$have" ] && continue
      src=""; for c in "$INST"/mods/$p; do [ -f "$c" ] && src=$c; done
      [ -n "$src" ] || { echo "no jar for '$g' in the pack's mods/" >&2; continue; }
      cp "$src" "$PORT/mods/" && basename "$src" >> "$PARK/.added_from_pack" && echo "added from the pack: $(basename "$src")"
    done
  fi
  echo "mods/ now: $(ls "$PORT"/mods/*.jar | wc -l) jars"
  ;;
restore)
  no_jvm; [ -d "$PARK" ] || { echo "nothing parked"; exit 0; }
  if [ -f "$PARK/.added_from_pack" ]; then
    while IFS= read -r b; do [ -n "$b" ] && rm -f "$PORT/mods/$b" && echo "removed (added from the pack): $b"; done < "$PARK/.added_from_pack"
    rm -f "$PARK/.added_from_pack"
  fi
  n=$(ls "$PARK" | wc -l); mv "$PARK"/*.jar "$PORT/mods/" 2>/dev/null || true; rmdir "$PARK"
  echo "restored $n jars; mods/ now: $(ls "$PORT"/mods/*.jar | wc -l) jars"
  ;;
server)
  OUT=${2:?OUT}; LEVEL=${3:?LEVEL}; GROUP=${4:?A or B}; MODE=${5:?chain or instance}; shift 5
  no_jvm
  check || { echo "chain incomplete: fix CEPortTesting/mods first" >&2; exit 1; }
  # parked jars belong to the instance set too
  CE_PERF=${CE_PERF:-1} bash "$INST/tools/creative_labels_test/scratch_server.sh" setup "$OUT" "$LEVEL" >/dev/null
  SRV="$OUT/srv"; cd "$SRV" || exit 1
  rm -f mods/*.jar
  grep '^| client |' "$INST/mod_sides.md" | awk -F'`' '{print $4}' > client_jars.txt
  for j in "$PORT"/mods/*.jar "$PARK"/*.jar; do
    [ -f "$j" ] || continue; b=$(basename "$j")
    if [ "$MODE" = chain ] || [ "$MODE" = optional ]; then in_chain "$b" || continue
    elif [ "$MODE" = cooking ]; then in_chain "$b" || in_cooking "$b" || continue
    else grep -qxF "$b" client_jars.txt && continue; fi
    rm -f "mods/$b"; cp -l "$j" mods/ 2>/dev/null || cp "$j" mods/   # unlink first: never write through a hard link
  done
  if [ "$MODE" = optional ]; then for g in "${OPTIONAL[@]}"; do add_glob "$g" >/dev/null; done; fi
  if [ -n "${ADD:-}" ]; then IFS=, read -ra addg <<< "$ADD"; for g in "${addg[@]}"; do add_glob "$g"; done; fi
  for g in ${DROP:-}; do rm -f mods/$g; echo "dropped: $g"; done
  for l in *_upstream; do [ -L "$l" ] && rm "$l"; done
  for u in "$PORT"/*_upstream; do
    b=$(basename "$u")
    if [[ " ${NOUPSTREAM:-} " == *" $b "* ]]; then echo "donor folder left out: $b"; continue; fi
    ln -sfn "$u" "$b"
  done
  rm -rf kubejs config defaultconfigs
  cp -r "$PORT/kubejs" kubejs; mkdir -p config defaultconfigs
  [ "${KEEPCFG:-0}" = 1 ] && cp -r "$PORT/config/." config/ && echo "config: CEPortTesting's"
  cp "$INST/tools/scratch_query/zq_probe.js" kubejs/server_scripts/zz_zq_probe.js
  cp "$INST/tools/scratch_query/cbo_probe.js" kubejs/server_scripts/zz_cbo_probe.js   # /cbo (SQ-CBO)
  setprop() { local k=$1 v=$2; if grep -q "^$k=" server.properties; then sed -i "s|^$k=.*|$k=$v|" server.properties; else echo "$k=$v" >> server.properties; fi; }
  if [ "$GROUP" = B ]; then setprop level-type 'minecraft\:normal'; setprop generate-structures true; setprop level-seed 8675309; fi
  for kv in "$@"; do setprop "${kv%%=*}" "${kv#*=}"; done
  echo "port_chain $MODE: $(ls mods | wc -l) jars, level $LEVEL, group $GROUP"
  ;;
*) echo "unknown: $what" >&2; exit 2 ;;
esac
