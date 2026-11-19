#!/usr/bin/env bash
# Creative Brewing chain on its own, from the CEPortTesting instance (scratch_queue.md "SQ-CBA").
#   port_chain.sh server OUT LEVEL A|B chain|cooking|optional|instance [prop=value ...]
#       scratch dedicated server built by qmodset.py from modsets/port_chain.toml (the driver's `@@modset port_chain
#       VARIANT` is the same build) from CEPortTesting/mods: "chain" = only the chain group's jars, "cooking" = CHAIN + COOKING (the cooking mods Creative
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
# CHAIN / COOKING / OPTIONAL live in modsets/port_chain.toml (groups chain, cooking, optional; plan_rowfix_r1118.md R4
# B1): the driver builds the same servers from `@@modset port_chain VARIANT`; bare / restore / check read them
# from there too.
mapfile -t CHAIN < <(python3 "$INST/tools/scratch_query/qmodset.py" globs port_chain chain)
mapfile -t COOKING < <(python3 "$INST/tools/scratch_query/qmodset.py" globs port_chain cooking)
mapfile -t OPTIONAL < <(python3 "$INST/tools/scratch_query/qmodset.py" globs port_chain optional)
[ "${#CHAIN[@]}" -gt 0 ] || { echo "modsets/port_chain.toml: no chain group" >&2; exit 1; }
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
  # the build is qmodset.py on modsets/port_chain.toml; ADD / DROP / NOUPSTREAM / KEEPCFG become spec words
  args=(port_chain "$MODE")
  if [ -n "${ADD:-}" ]; then IFS=, read -ra addg <<< "$ADD"; for g in "${addg[@]}"; do args+=("+$g"); done; fi
  for g in ${DROP:-}; do args+=("-$g"); done
  [ -n "${NOUPSTREAM:-}" ] && args+=("noupstream=$(echo $NOUPSTREAM | tr ' ' ,)")
  [ "${KEEPCFG:-0}" = 1 ] && args+=("config=source")
  CE_PERF=${CE_PERF:-1} python3 "$INST/tools/scratch_query/qmodset.py" build "$OUT" "$LEVEL" "$GROUP" "${args[@]}" "$@"
  ;;
*) echo "unknown: $what" >&2; exit 2 ;;
esac
