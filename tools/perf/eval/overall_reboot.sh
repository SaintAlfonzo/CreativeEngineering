#!/usr/bin/env bash
# Reboot of an overall_run.sh world (perf.md section 12): boot, wall time to Done, /forge tps at Done + 240 s.
#   overall_reboot.sh OUT LEVEL TAG     results appended to perf/overall/LEVEL/timeline.txt, log copied as TAG.log
set -uo pipefail
INST="$(cd "$(dirname "$0")/../../.." && pwd)"
EV="$INST/tools/perf/eval/eval_server.sh"
OUT=${1:?OUT}; LEVEL=${2:?LEVEL}; TAG=${3:?TAG}
SRV="$OUT/srv"
RES="${RES:-$INST/perf/overall/$LEVEL}"
TL="$RES/timeline.txt"
mark() { echo "$(date +%s) $*" | tee -a "$TL"; }
mark "$TAG boot"
bash "$EV" boot "$OUT" || { mark "$TAG boot refused"; exit 1; }
r=$(bash "$EV" wait "$OUT" "Done (" 1200); mark "$TAG done: $r"
cp "$SRV/logs/latest.log" "$RES/$TAG.log"
[ "$r" = CRASH ] || [ "$r" = TIMEOUT ] && exit 2
[ "${IDLE:-1}" = 1 ] || exit 0
sleep 240
bash "$EV" cmd "$OUT" "forge tps"; sleep 3
mark "$TAG tps: $(grep -E 'Overall *:' "$SRV/logs/latest.log" | tail -1 | sed 's/^.*Overall/Overall/')"
