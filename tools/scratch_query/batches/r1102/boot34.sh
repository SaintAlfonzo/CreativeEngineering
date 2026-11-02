#!/usr/bin/env bash
# r1102 Boots 3 and 4 (expected to stop at startup): SQ-MDP-17 (no miners_upstream), SQ-MDP-15 (only the 1.4.5 jar).
#   boot34.sh OUT       OUT = the cdj5 scratch dir; the cdj5 server must be stopped by this script's first step.
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
OUT=$1; S=$OUT/srv; [ -d "$S" ] || exit 1
jv() { bash "$INST/tools/scratch_query/game_jvms.sh"; }
waitempty() { for i in $(seq 90); do jv | grep -q . || return 0; sleep 2; done; return 1; }
bootstop() { # $1 = label; boots, waits for the stop signal, kills our own JVM if it lingers
  local start; start=$(date +%s)
  (bash "$INST/tools/creative_labels_test/scratch_server.sh" boot "$OUT" >/dev/null 2>&1 &)
  sleep 10; local pid; pid=$(jv | awk '{print $1}' | head -1); echo "$1 pid=$pid"
  for i in $(seq 120); do
    grep -q "reads its textures" "$S/logs/latest.log" 2>/dev/null && { echo "$1 stop-message"; break; }
    grep -qE "Done \(" "$S/logs/latest.log" 2>/dev/null && { echo "$1 REACHED-DONE"; break; }
    [ -n "$(find "$S/crash-reports" -name '*.txt' -newermt "@$start" 2>/dev/null)" ] && { echo "$1 crash-report"; break; }
    jv | grep -q . || { echo "$1 jvm-exited"; break; }
    sleep 2
  done
  sleep 8
  if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
    grep -qE "Done \(" "$S/logs/latest.log" && bash "$INST/tools/creative_labels_test/scratch_server.sh" stop "$OUT" >/dev/null 2>&1
    waitempty || { kill "$pid"; echo "$1 killed own pid $pid"; waitempty; }
  fi
  jv | grep -q . && { echo "JVM still up, abort"; exit 1; }
}
bash "$INST/tools/creative_labels_test/scratch_server.sh" stop "$OUT" >/dev/null 2>&1; waitempty || { echo "boot2 JVM did not stop"; exit 1; }
test -L "$S/miners_upstream" && rm "$S/miners_upstream"
bootstop boot3
python3 "$INST/tools/scratch_query/qrun.py" run "$OUT" "$INST/tools/scratch_query/batches/r1102/b3_nodonor.qb" "$INST/perf/runs/r1102/b3" 2>&1 | tail -2
mkdir "$S/miners_upstream" && cp "$INST/miners_upstream/miners_delight-1.20.1-1.4.5-backport.jar" "$S/miners_upstream/"
bootstop boot4
python3 "$INST/tools/scratch_query/qrun.py" run "$OUT" "$INST/tools/scratch_query/batches/r1102/b4_wrongdonor.qb" "$INST/perf/runs/r1102/b4" 2>&1 | tail -2
rm -r "$S/miners_upstream" && ln -sfn "$INST/miners_upstream" "$S/miners_upstream" && echo "miners_upstream symlink restored"
