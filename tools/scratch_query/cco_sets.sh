#!/usr/bin/env bash
# SQ-CCO-12 / -13 driver (r1107cco): for each set, build a fresh cc_only_srv.sh server, boot it, run
# batches/r1107cco/b4_without.qb, stop it and wait until no game JVM is up. One JVM at a time; stops at the first set
# that does not reach Done (its srv is left for reading).
#   cco_sets.sh SCRATCH RUNID set [set ...]      set = without=<keys> | alone | full
# Results: perf/runs/RUNID/b4_<set with , and = replaced>/; srv dirs: SCRATCH/cco_<same>/.
set -uo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"; cd "$INST" || exit 1
SCR=${1:?SCRATCH}; RUN=${2:?RUNID}; shift 2
Q=tools/scratch_query
for set in "$@"; do
  tag=$(echo "$set" | tr ',=' '__')
  OUT="$SCR/cco_$tag"; RES="perf/runs/$RUN/b4_$tag"
  [ -n "$(bash $Q/game_jvms.sh)" ] && { echo "$(date +%H:%M:%S) $set: a game JVM is up, stopping"; exit 1; }
  rm -rf "$OUT"
  bash $Q/cc_only_srv.sh "$OUT" cco1 "$set" difficulty=normal > "$SCR/prep_$tag.txt" 2>&1 || { echo "$set: prep failed"; tail -3 "$SCR/prep_$tag.txt"; exit 1; }
  echo "$(date +%H:%M:%S) $set: $(grep 'cc_only done' "$SCR/prep_$tag.txt")"
  (bash tools/creative_labels_test/scratch_server.sh boot "$OUT" >/dev/null 2>&1 &)
  sleep 8
  r=$(bash $Q/wait_boot.sh "$OUT" 400); echo "$(date +%H:%M:%S) $set: $r"
  case "$r" in DONE*) ;; *) bash $Q/stop_wait.sh "$OUT" 60 >/dev/null; echo "$set: no Done, stopping"; exit 1 ;; esac
  python3 $Q/qrun.py run "$OUT" $Q/batches/r1107cco/b4_without.qb "$RES" 2>&1 | grep -E "^SQ-|totals"
  bash $Q/stop_wait.sh "$OUT" 120 >/dev/null
  cp "$OUT/srv/logs/latest.log" "$RES/latest.log" 2>/dev/null
  echo "$(date +%H:%M:%S) $set: stopped"
done
