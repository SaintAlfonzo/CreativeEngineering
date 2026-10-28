#!/usr/bin/env bash
# Group A (superflat) entries of scratch_queue.md added 2026-10-26/27, headless:
#   SQ-PF-01/02/05, SQ-ATC-01..04, SQ-SALT-05..07, SQ-POT-01..05, SQ-HT-01 (+ the no-player half of -03), SQ-FTW-01, -04..06.
#   run_group_a_1027.sh OUT        results in perf/group_a_1027/ (key lines, logs); srv/ is kept for the pot audit copy-back.
# Boot 1 as shipped; boot 2 with [worldgen.first_tick_water] windowTicks = 0 for SQ-FTW-06.
set -uo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
SS="$INST/tools/creative_labels_test/scratch_server.sh"
EV="$INST/tools/perf/eval/eval_server.sh"
OUT=${1:?OUT}; SRV="$OUT/srv"
RES="$INST/perf/group_a_1027"; mkdir -p "$RES"
TL="$RES/timeline.txt"; : > "$TL"
mark() { echo "$(date +%s) $*" | tee -a "$TL"; }
cmd() { bash "$SS" cmd "$OUT" "$1"; }
wait_for() { bash "$EV" wait "$OUT" "$1" "${2:-900}"; }
srv_pid() { for p in $(pgrep -x java); do case "$(readlink /proc/$p/cwd)" in "$SRV"*) echo "$p"; return;; esac; done; }
stop_srv() {
  bash "$SS" stop "$OUT"
  for _ in $(seq 60); do [ -z "$(srv_pid)" ] && break; sleep 2; done
  p=$(srv_pid); [ -n "$p" ] && { kill -9 "$p"; mark "killed $p"; }
}

bash "$SS" setup "$OUT" qa_1027
cd "$SRV" || exit 1
for j in mods/*.jar; do t=$(readlink -f "$j"); ln -f "$t" "$j" 2>/dev/null || cp --remove-destination "$t" "$j"; done
mkdir -p perf/exit; cp "$INST/server/perf/ce-server.jfc" perf/
cat > user_jvm_args.txt <<EOF
-Xmx6G
-XX:FlightRecorderOptions=stackdepth=256,repository=perf/jfr-repo
-XX:StartFlightRecording=name=ce,settings=perf/ce-server.jfc,disk=true,maxage=30m,maxsize=300m
EOF
cp "$INST/tools/armor_trims_cache_test/atc_probe.js" kubejs/server_scripts/zz_atc_probe.js
cp "$INST/tools/salt_probe/salt_probe.js" kubejs/server_scripts/zz_salt_probe.js
cp "$INST/tools/flower_pot_test/pot_probe.js" kubejs/server_scripts/zz_pot_probe.js
cp "$INST/tools/scratch_query/zq_probe.js" kubejs/server_scripts/zz_zq_probe.js

# ---- boot 1
live=$(bash "$INST/tools/scratch_query/game_jvms.sh"); [ -n "$live" ] && { mark "JVM up: $live"; exit 1; }
mark boot1; bash "$SS" boot "$OUT"
r=$(wait_for "Done (" 900); mark "done: $r"
case "$r" in CRASH|TIMEOUT) cp logs/latest.log "$RES/boot1.log"; stop_srv; exit 2;; esac
cmd "ceperf status"                                               # SQ-PF-01
cmd "forceload add 290 290 350 310"; cmd "forceload add 300 60"; cmd "forceload add 400 400"
sleep 30
cmd "atctest"; wait_for "ATC-DONE" 300 > /dev/null; mark atctest            # SQ-ATC-02/03
cmd "salttest"; wait_for "[salttest] done" 600 > /dev/null; mark salttest   # SQ-SALT-05..07
cmd "dumppots"; sleep 10; cmd "pottest all"
wait_for "PP-DONE" 900 > /dev/null; mark pottest                            # SQ-POT-01..04
# SQ-FTW-04: spawn chunks ticked well past the 600-tick window: a washed torch drops
cmd "kill @e[type=item]"; cmd "setblock 10 -60 10 minecraft:torch"; cmd "setblock 9 -60 10 minecraft:water"; sleep 3
cmd "execute if block 10 -60 10 minecraft:torch"; cmd "execute if entity @e[type=item,x=10,y=-60,z=10,distance=..8]"
mark ftw04; sleep 2
# SQ-FTW-05: a chunk that has just started ticking: washed torch, no item
cmd "forceload add 2000 2000"; sleep 4
cmd "kill @e[type=item]"; cmd "setblock 2000 -60 2000 minecraft:torch"; cmd "setblock 1999 -60 2000 minecraft:water"; sleep 3
cmd "execute if block 2000 -60 2000 minecraft:torch"; cmd "execute if entity @e[type=item,x=2000,y=-60,z=2000,distance=..8]"
mark ftw05; sleep 2
sleep 60; cmd "ceperf dump smoke"; sleep 5; mark "ceperf dump"           # SQ-PF-02
cmd "reload"; wait_for "Reloaded" 300 > /dev/null 2>&1; sleep 20; mark reload1
cmd "atctest"; sleep 20; mark "atctest after reload"                   # SQ-ATC-04
cmd "reload"; sleep 60; mark reload2
cmd "ceperf status"; sleep 3                                            # SQ-PF-05
stop_srv
cp logs/latest.log "$RES/boot1.log"
mark "boot1 stopped"

# ---- boot 2: windowTicks = 0 (SQ-FTW-06)
T=config/creativeengineering-common.toml
if grep -q '^\s*\[worldgen.first_tick_water\]' "$T"; then
  sed -i '/^\s*\[worldgen.first_tick_water\]/,/^\s*\[/ s/^\(\s*windowTicks\s*=\).*/\1 0/' "$T"
else
  printf '\t[worldgen.first_tick_water]\n\t\twindowTicks = 0\n' >> "$T"
fi
grep -n -A12 'first_tick_water\]' "$T" | grep windowTicks >> "$TL"
mark boot2; bash "$SS" boot "$OUT"
r=$(wait_for "Done (" 900); mark "done2: $r"
case "$r" in CRASH|TIMEOUT) cp logs/latest.log "$RES/boot2.log"; stop_srv; exit 2;; esac
cmd "forceload add -2000 -2000"; sleep 4
cmd "kill @e[type=item]"; cmd "setblock -2000 -60 -2000 minecraft:torch"; cmd "setblock -2001 -60 -2000 minecraft:water"; sleep 3
cmd "execute if block -2000 -60 -2000 minecraft:torch"; cmd "execute if entity @e[type=item,x=-2000,y=-60,z=-2000,distance=..8]"
mark ftw06; sleep 3
stop_srv
cp logs/latest.log "$RES/boot2.log"
mark finished
