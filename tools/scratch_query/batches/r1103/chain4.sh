#!/bin/bash
# r1103: after chain3.sh, repro of the SQ-ENM-03 boot crash (no endersdelight-1.19-1.2.2.jar): keep logs/kubejs/startup.log.
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/51e0db54-43f7-4a69-b393-9d5ee1c5b98d/scratchpad/r1103
cd "$INST" || exit 1
until grep -q 'CHAIN3 sBR2 done' perf/runs/r1103/chain.log; do sleep 30; done
export JFR_MAX=200m
O=$SCR/enm3; R=$INST/perf/runs/r1103/enm3; mkdir -p "$R"
while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
rm -rf "$O"; bash tools/scratch_query/prep_srv.sh "$O" enm3 A > "$R/prep.txt" 2>&1
for j in "$O"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done
rm -f "$O"/srv/mods/endersdelight-1.19-1.2.2.jar
(bash tools/creative_labels_test/scratch_server.sh boot "$O" >/dev/null 2>&1 &)
sleep 5; r=$(bash tools/scratch_query/wait_boot.sh "$O" 600); echo "boot $r $(date +%H:%M:%S)" > "$R/drive.log"
bash tools/scratch_query/stop_wait.sh "$O" 120 >> "$R/drive.log" 2>&1
cp "$O/srv/logs/kubejs/startup.log" "$R/" 2>/dev/null; cp "$O/srv/logs/latest.log" "$R/" 2>/dev/null; cp -r "$O/srv/crash-reports" "$R/" 2>/dev/null
rm -rf "$O"
echo "CHAIN4 enm3 done $(date +%H:%M:%S)" >> perf/runs/r1103/chain.log
