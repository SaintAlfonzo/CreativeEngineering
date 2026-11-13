#!/bin/bash
# r1109wo runner (SQ-WO Wise Oak, scratch_queue.md): wo_run.sh OUT RES STEP...   STEP = a1 a1b a2 a3 a4 a5 s (group A, same OUT) or b (OUT is group B).
# Every boot: refuses with a game JVM up, sets the srv toml for the step, boots, waits for Done, runs the step's batch, stops.
INST="$(cd "$(dirname "$0")/../.." && pwd)"; Q="$INST/tools/scratch_query"; B="${WO_BATCHES:-$Q/batches/r1109wo}"; SS="$INST/tools/creative_labels_test/scratch_server.sh"
OUT=${1:?OUT}; RES=${2:?RES}; shift 2; T="$OUT/srv/config/creativeworldgen-common.toml"
t() { python3 "$Q/qrun.py" toml "$T" "$1" "$2" "$3" >/dev/null; }
up() { # up BATCH [strace]
  [ -z "$(bash "$Q/game_jvms.sh")" ] || { echo "a game JVM is up"; exit 1; }
  rm -f "$OUT"/srv/crash-reports/*.txt 2>/dev/null
  if [ "$2" = strace ]; then
    cd "$OUT/srv" || exit 1; rm -f logs/latest.log console.log; [ -p in.fifo ] || mkfifo in.fifo
    (sleep infinity > in.fifo & echo $! > feeder.pid)
    nohup bash -c 'strace -f --seccomp-bpf -s 200 -e trace=connect,sendto,sendmmsg -o wo_net.txt bash run.sh nogui < in.fifo > console.log 2>&1' >/dev/null 2>&1 &
    cd "$INST" || exit 1
  else bash "$SS" boot "$OUT"; fi
  r=$(bash "$Q/wait_boot.sh" "$OUT" 900); echo "boot: $r"; case "$r" in DONE*) ;; *) exit 1;; esac
  python3 "$Q/qrun.py" run "$OUT" "$B/$1" "$RES" | tail -40 | cut -c1-200
  bash "$Q/stop_wait.sh" "$OUT"
}
for s in "$@"; do echo "=== $s $(date +%H:%M:%S)"; case $s in
  a1) up b1_defaults.qb;;
  a1b) up b1b_restart.qb;;
  a2) t wise_oak sleepDurationSeconds 5; t wise_oak angerDurationSeconds 5; t wise_oak angrySleepDurationSeconds 5; t wise_oak.gifts chancePercent 100.0; t wise_oak.gifts trustThreshold 75; up b2_short_timers.qb;;
  a3) t wise_oak.gifts chancePercent 0.0; t wise_oak.gifts trustThreshold 0; up b3_chance0.qb;;
  a4) t wise_oak.gifts chancePercent 100.0; t wise_oak.gifts trustThreshold 0; t wise_oak.gifts saplings '["minecraft:oak_sapling", "nope:bad"]'; t wise_oak.gifts saplingCount 3; t wise_oak.gifts leaves '["minecraft:totem_of_undying"]'; t wise_oak.gifts leavesCount 3; t wise_oak.gifts fruitSaplings '[]'; t wise_oak.gifts fruit '[]'; up b4_lists.qb;;
  a5) t wise_oak.gifts saplings '[]'; t wise_oak.gifts leaves '[]'; t wise_oak.gifts saplingCount 1; t wise_oak.gifts leavesCount 16; up b5_empty.qb;;
  s) rm -f "$T"; up b6_strace.qb strace; f="$OUT/srv/wo_net.txt"; echo "strace lines: $(wc -l < "$f"), dns-or-inet control: $(grep -c -E 'AF_INET|:53|htons\(53\)' "$f")"; echo "lookup hosts: $(grep -c -E 'api-ninjas|fantasynamegenerators|checkip|weatherunlocked|8\.8\.8\.8' "$f")"; grep -E 'api-ninjas|fantasynamegenerators|checkip|weatherunlocked|8\.8\.8\.8' "$f" | head -5 | cut -c1-300;;
  b) sed -i -E 's/^(\s*tree_rarity\s*=\s*).*/\11/' "$OUT/srv/config/mysticaloaktree-common.toml"; grep tree_rarity "$OUT/srv/config/mysticaloaktree-common.toml"; up b7_worldgen.qb;;
  *) echo "unknown step $s"; exit 1;;
esac; done
