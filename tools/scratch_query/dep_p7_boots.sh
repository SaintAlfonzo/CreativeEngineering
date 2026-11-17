#!/usr/bin/env bash
# r1115 phase 7 (SQ-DEP-09b drop-one boots, SQ-DEP-02 no-Brewing boot): one scratch server per spec, in order, one JVM
# at a time. Batches come from tools/scratch_query/dep_gen_p7.py.
#   dep_p7_boots.sh S SPEC [SPEC ...]
#     S     scratch root (OUT dirs are S/p7<label>)
#     SPEC  label:group:jarregex[:batch]   e.g. "dbb:A:buzzier_bees-:p7_d_buzzier_bees.qb"; jarregex = ERE matched against
#           the start of each srv/mods file name (jars removed); no batch = a cascade confirm boot (boot, grep, stop)
# Per spec: NOSWAP=1 prep_srv.sh (spawn-animals=false, difficulty=normal; group B gets a new level), remove the jars,
# probes zz_c3_probe.js, zz_hh2.js, zz_zqn_probe.js, boot (output to S/p7<label>/boot.txt), wait_boot.sh, qrun.py into
# perf/runs/r1115_p7_<label>/ when Done, stop_wait.sh. Progress lines go to stdout with clock times.
set -u
INST="$(cd "$(dirname "$0")/../.." && pwd)"
S="$1"; shift
T="$INST/tools/scratch_query"
for spec in "$@"; do
  IFS=: read -r label group rx batch <<<"$spec"
  OUT="$S/p7$label"
  echo "$(date +%T) [$label] prep group $group, remove ^($rx)"
  if [ -n "$(bash "$T/game_jvms.sh")" ]; then echo "$(date +%T) [$label] a game JVM is up: stop"; exit 1; fi
  rm -rf "$OUT"
  extra=""; [ "$group" = B ] && extra="level-seed=8675309"
  NOSWAP=1 bash "$T/prep_srv.sh" "$OUT" "p7$label" "$group" difficulty=normal $extra > "$S/p7$label.prep.txt" 2>&1 || { echo "[$label] prep failed"; tail -3 "$S/p7$label.prep.txt"; exit 1; }
  ( cd "$OUT/srv" || exit 1
    for j in mods/*.jar; do b=$(basename "$j"); echo "$b" | grep -qE "^($rx)" && { rm -f "$j"; echo "removed $b"; }; done
    cp "$T/c3_probe.js" kubejs/server_scripts/zz_c3_probe.js
    cp "$T/hh_mw_probe.js" kubejs/server_scripts/zz_hh2.js
    cp "$T/zqn_probe.js" kubejs/server_scripts/zz_zqn_probe.js
    echo "jars $(ls mods | wc -l)"; for j in mods/*.jar; do grep -qF "$(basename "$j")" "$INST/mod_sides.md" || echo "unlisted $j"; done
  ) | sed "s/^/$(date +%T) [$label] /"
  echo "$(date +%T) [$label] boot"
  bash "$INST/tools/creative_labels_test/scratch_server.sh" boot "$OUT" > "$OUT/boot.txt" 2>&1 < /dev/null &
  r=$(timeout 600 bash "$T/wait_boot.sh" "$OUT" 580 2>&1 | tail -1)
  pid=$(for p in $(pgrep -x java); do [ "$(readlink /proc/$p/cwd)" = "$OUT/srv" ] && echo $p; done | head -1)
  echo "$(date +%T) [$label] boot result: $r pid ${pid:-none}"
  grep -nE "Missing or unsupported mandatory|requires|Mod ID: '|is required" "$OUT/srv/logs/latest.log" 2>/dev/null | head -12 | cut -c1-260 | sed "s/^/  [$label] /"
  if [[ "$r" == DONE* ]] && [ -n "${batch:-}" ]; then
    python3 "$T/qrun.py" run "$OUT" "$T/batches/r1115/$batch" "$INST/perf/runs/r1115_p7_$label" < /dev/null 2>&1 | grep -E " \.\.\. |totals" | sed "s/^/$(date +%T) [$label] /"
  fi
  timeout 150 bash "$T/stop_wait.sh" "$OUT" 120 < /dev/null 2>&1 | tail -1 | sed "s/^/$(date +%T) [$label] stop: /"
  # stop_wait can be cut off by its timeout while scratch_server.sh stop hangs (a server idle after "Failed to load
  # datapacks" never reads the fifo): kill our own JVM (cwd OUT/srv) then
  for p in $(pgrep -x java); do [ "$(readlink /proc/$p/cwd)" = "$OUT/srv" ] && { echo "$(date +%T) [$label] killing own pid $p"; kill $p; sleep 5; kill -0 $p 2>/dev/null && kill -9 $p; }; done
  for i in $(seq 1 60); do [ -z "$(bash "$T/game_jvms.sh")" ] && break; sleep 2; done
  [ -n "$(bash "$T/game_jvms.sh")" ] && { echo "$(date +%T) [$label] JVM still up after stop: halt"; exit 1; }
  echo "$(date +%T) [$label] done"
done
echo "$(date +%T) all done"
