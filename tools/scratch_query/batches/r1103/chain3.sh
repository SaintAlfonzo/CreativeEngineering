#!/bin/bash
# r1103: after chain2.sh (SQ-LBS rerun), run sB_R.qb (SQ-CN-04b, SQ-WCT-08) on a fresh group A superflat sbr2.
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/51e0db54-43f7-4a69-b393-9d5ee1c5b98d/scratchpad/r1103
cd "$INST" || exit 1
until grep -q 'CHAIN2 sBR done' perf/runs/r1103/chain.log; do sleep 30; done
export JFR_MAX=200m
O=$SCR/sbr2; R=$INST/perf/runs/r1103/sBR2; mkdir -p "$R"
while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
rm -rf "$O"; bash tools/scratch_query/prep_srv.sh "$O" sbr2 A > "$R/prep.txt" 2>&1
for j in "$O"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done
cp tools/scratch_query/zsb_probe.js "$O/srv/kubejs/server_scripts/zz_zsb_probe.js"
(bash tools/creative_labels_test/scratch_server.sh boot "$O" >/dev/null 2>&1 &)
sleep 5; r=$(bash tools/scratch_query/wait_boot.sh "$O" 900); echo "boot $r $(date +%H:%M:%S)" >> "$R/drive.log"
case $r in DONE*) python3 tools/scratch_query/qrun.py run "$O" tools/scratch_query/batches/r1103/sB_R.qb "$R" > "$R/qrun.txt" 2>&1;; esac
bash tools/scratch_query/stop_wait.sh "$O" 240 >> "$R/drive.log" 2>&1; cp "$O/srv/logs/latest.log" "$R/boot.log" 2>/dev/null; rm -rf "$O"
echo "CHAIN3 sBR2 done $(date +%H:%M:%S)" >> perf/runs/r1103/chain.log
