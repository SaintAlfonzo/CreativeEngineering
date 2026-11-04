#!/bin/bash
# r1103: after chain4.sh, sA_R.qb part 1 (rows from sA_R_rows.txt, read at run time) on a fresh group A superflat sar,
# current jars, setup as sA_setup.md Boot 2 (probes incl. the fixed sA_probe.js). Part 2 (old 1.1.06 world) not run.
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/51e0db54-43f7-4a69-b393-9d5ee1c5b98d/scratchpad/r1103
cd "$INST" || exit 1
until grep -q 'CHAIN4 enm3 done' perf/runs/r1103/chain.log; do sleep 30; done
export JFR_MAX=200m B0=$SCR/b0
O=$SCR/sar; R=$INST/perf/runs/r1103/sAR; mkdir -p "$R"
while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
rm -rf "$O"; bash tools/scratch_query/prep_srv.sh "$O" sar A difficulty=normal spawn-monsters=true spawn-animals=true > "$R/prep.txt" 2>&1
for j in "$O"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done
S=$O/srv/kubejs/server_scripts
cp tools/scratch_query/zp_probe.js $S/zz_zp_probe.js; cp tools/scratch_query/zact_probe.js $S/zz_zact_probe.js
cp tools/fungi_probe/zcount.js $S/zz_count.js; cp tools/scratch_query/zloot_probe.js $S/zz_zloot_probe.js
cp tools/salt_probe/salt_probe.js $S/zz_salt_probe.js; cp tools/scratch_query/sA_probe.js $S/zz_sA_probe.js
(bash tools/creative_labels_test/scratch_server.sh boot "$O" >/dev/null 2>&1 &)
sleep 5; r=$(bash tools/scratch_query/wait_boot.sh "$O" 900); echo "boot $r $(date +%H:%M:%S)" >> "$R/drive.log"
case $r in DONE*) python3 tools/scratch_query/qrun.py run "$O" tools/scratch_query/batches/r1103/sA_R.qb "$R" --rows "$(cat tools/scratch_query/batches/r1103/sA_R_rows.txt)" > "$R/qrun.txt" 2>&1;; esac
bash tools/scratch_query/stop_wait.sh "$O" 240 >> "$R/drive.log" 2>&1
cp "$O/srv/logs/latest.log" "$R/boot.log" 2>/dev/null; cp "$O/srv/logs/kubejs/server.log" "$R/boot_kubejs.log" 2>/dev/null; rm -rf "$O"
echo "CHAIN5 sAR done $(date +%H:%M:%S)" >> perf/runs/r1103/chain.log
