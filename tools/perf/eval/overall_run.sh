#!/usr/bin/env bash
# Overall pack measurement on one scratch world (perf.md section 12): everything as shipped, no per-fix worlds.
#   overall_run.sh OUT LEVEL        eval seed and square of perf.md 10.1; results in RES=perf/overall/LEVEL
# Steps: eval_server.sh setup, boot (wall time to Done), entity snapshot at Done + 120 s, /forge tps at Done + 240 s,
# pregen (Chunky radius 400 at 4000 4000, own JFR recording), /forge tps, /reload (time to a quiet log).
# The server is LEFT RUNNING for the scratch tests; stop it with eval_server.sh stop OUT.
set -uo pipefail
INST="$(cd "$(dirname "$0")/../../.." && pwd)"
EV="$INST/tools/perf/eval/eval_server.sh"
OUT=${1:?OUT}; LEVEL=${2:?LEVEL}
SRV="$OUT/srv"
RES="${RES:-$INST/perf/overall/$LEVEL}"
PREGEN=${PREGEN:-400}
mkdir -p "$RES"
TL="$RES/timeline.txt"
mark() { echo "$(date +%s) $*" | tee -a "$TL"; }
cmd() { bash "$EV" cmd "$OUT" "$1"; }
wait_for() { bash "$EV" wait "$OUT" "$1" "${2:-900}"; }
srv_pid() { for p in $(pgrep -x java); do case "$(readlink /proc/$p/cwd)" in "$SRV"*) echo "$p"; return;; esac; done; }
wait_count() { for _ in $(seq "${3:-600}"); do [ "$(grep -c -- "$1" "$SRV/logs/latest.log" 2>/dev/null)" -ge "$2" ] && return 0; sleep 1; done; return 1; }
# waits until the log has not grown for N seconds
wait_quiet() { local last=-1 same=0 n; for _ in $(seq 600); do n=$(stat -c %s "$SRV/logs/latest.log"); \
  if [ "$n" = "$last" ]; then same=$((same + 1)); [ $same -ge "$1" ] && return 0; else same=0; last=$n; fi; sleep 1; done; }

: > "$TL"
mark "level $LEVEL as shipped, PREGEN=$PREGEN"
JFR_MAX=400m bash "$EV" setup "$OUT" "$LEVEL"
cd "$SRV" || exit 1
ls -la --time-style=+%m-%d_%H:%M mods/ | grep -E "creativeengineering|creativedelight|modernfix" >> "$TL"

mark boot
bash "$EV" boot "$OUT" || { mark "boot refused"; exit 1; }
r=$(wait_for "Done (" 1200); mark "done: $r"
[ "$r" = CRASH ] || [ "$r" = TIMEOUT ] && { cp logs/latest.log "$RES/"; cp -r crash-reports "$RES/" 2>/dev/null; bash "$EV" stop "$OUT"
  p=$(srv_pid); [ -n "$p" ] && { kill -9 "$p"; mark "killed $p"; }; exit 2; }
PID=$(srv_pid); mark "pid $PID"
cp logs/latest.log "$RES/boot1.log"

sleep 120
cmd "save-all flush"; wait_count "Saved the game" 1 120
mkdir -p "$RES/snap"; cp -r "$LEVEL/entities" "$RES/snap/"
mark "entity snapshot"
python3 "$INST/tools/perf/eval/entity_census.py" "$RES/snap" > "$RES/entity_census.txt" 2>&1
rm -rf "$RES/snap"

sleep 120
cmd "forge tps"; sleep 3; mark "tps: $(grep -E 'Overall *:' logs/latest.log | tail -1 | sed 's/^.*Overall/Overall/')"

if [ "$PREGEN" -gt 0 ]; then
  jcmd "$PID" JFR.start name=pregen settings="$SRV/perf/ce-server.jfc" disk=true > /dev/null
  cmd "chunky center 4000 4000"; cmd "chunky radius $PREGEN"; sleep 1
  mark "pregen start"; cmd "chunky start"
  r=$(wait_for "Task finished" 1800); mark "pregen: $r"
  jcmd "$PID" JFR.stop name=pregen filename="$RES/pregen.jfr" > /dev/null
  cmd "forge tps"; sleep 3; mark "tps after pregen: $(grep -E 'Overall *:' logs/latest.log | tail -1 | sed 's/^.*Overall/Overall/')"
fi

wait_quiet 5
mark "reload start"; cmd "reload"
wait_quiet 10; mark "reload quiet (minus 10 s)"
mark "left running, pid $PID"
