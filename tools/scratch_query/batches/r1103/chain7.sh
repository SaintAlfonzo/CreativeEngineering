#!/bin/bash
# r1103: after chain6.sh, p2_R.qb (19 phase-2 batch-fault rows) on a fresh group A superflat p2r (setup = drive_p2.sh rA).
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/51e0db54-43f7-4a69-b393-9d5ee1c5b98d/scratchpad/r1103
cd "$INST" || exit 1
until grep -q 'CHAIN6 sAR3 done' perf/runs/r1103/chain.log; do sleep 30; done
export JFR_MAX=200m
O=$SCR/p2r; R=$INST/perf/runs/r1103/p2R; mkdir -p "$R"
while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
rm -rf "$O"; bash tools/scratch_query/prep_srv.sh "$O" p2r A spawn-animals=true > "$R/prep.txt" 2>&1
for j in "$O"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done
S=$O/srv/kubejs/server_scripts
for p in tools/vintage_probe/vintage_probe.js:zz_vtd_probe.js tools/kitchen_probe/cfb_kitchen_probe.js:zz_kt_probe.js \
  tools/fungi_probe/zcount.js:zz_count.js tools/fungi_probe/ztrades.js:zz_trades.js tools/scratch_query/zloot_probe.js:zz_zloot_probe.js \
  tools/scratch_query/zti_probe.js:zz_zti_probe.js tools/scratch_query/zact_probe.js:zz_zact_probe.js tools/scratch_query/zp_probe.js:zz_zp_probe.js \
  tools/salt_probe/salt_probe.js:zz_salt_probe.js tools/worldedit_probe/we_probe.js:zz_we_probe.js; do cp "${p%%:*}" "$S/${p##*:}"; done
(bash tools/creative_labels_test/scratch_server.sh boot "$O" >/dev/null 2>&1 &)
sleep 5; r=$(bash tools/scratch_query/wait_boot.sh "$O" 900); echo "boot $r $(date +%H:%M:%S)" >> "$R/drive.log"
case $r in DONE*) python3 tools/scratch_query/qrun.py run "$O" tools/scratch_query/batches/r1103/p2_R.qb "$R" > "$R/qrun.txt" 2>&1;; esac
bash tools/scratch_query/stop_wait.sh "$O" 240 >> "$R/drive.log" 2>&1
cp "$O/srv/logs/latest.log" "$R/boot.log" 2>/dev/null; cp "$O/srv/logs/kubejs/server.log" "$R/boot_kubejs.log" 2>/dev/null; rm -rf "$O"
echo "CHAIN7 p2R done $(date +%H:%M:%S)" >> perf/runs/r1103/chain.log
