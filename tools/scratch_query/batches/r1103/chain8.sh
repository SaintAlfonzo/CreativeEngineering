#!/bin/bash
# r1103: after chain7.sh, p2_R2.qb (two boots on fresh A world s5r2, header of p2_R2.qb).
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/51e0db54-43f7-4a69-b393-9d5ee1c5b98d/scratchpad/r1103
cd "$INST" || exit 1
until grep -q 'CHAIN7 p2R done' perf/runs/r1103/chain.log; do sleep 30; done
export JFR_MAX=200m
Q="python3 tools/scratch_query/qrun.py"; O=$SCR/s5r2; R=$INST/perf/runs/r1103/p2R2; mkdir -p "$R"; C=$O/srv/config/creativedelight-common.toml
bootw() { while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
  rm -f "$O/srv/logs/latest.log"; mkdir -p "$O/old-crash"; mv "$O"/srv/crash-reports/* "$O/old-crash/" 2>/dev/null
  (bash tools/creative_labels_test/scratch_server.sh boot "$O" >/dev/null 2>&1 &)
  sleep 5; r=$(bash tools/scratch_query/wait_boot.sh "$O" 900); echo "boot $r $(date +%H:%M:%S)" >> "$R/drive.log"; case $r in DONE*) return 0;; *) return 1;; esac; }
while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
rm -rf "$O"; bash tools/scratch_query/prep_srv.sh "$O" s5r2 A difficulty=normal spawn-monsters=true spawn-animals=true spawn-protection=16 > "$R/prep.txt" 2>&1
for j in "$O"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done
cp tools/fungi_probe/zcount.js "$O/srv/kubejs/server_scripts/zz_count.js"; cp tools/scratch_query/s5_probe.js "$O/srv/kubejs/server_scripts/zz_s5_probe.js"
for k in secondFruitMinutes thirdFruitMinutes fourthFruitMinutes; do $Q toml $C stem_fruit $k 0; done
$Q toml $C stem_fruit maxFruits 3; $Q toml $C stem_fruit fertileSoilBoost false
if bootw; then $Q run "$O" tools/scratch_query/batches/r1103/p2_R2.qb "$R" --rows S5R2-00,SQ-PSF-08a,SQ-RB-21b,SQ-DRK-04,SQ-PSF-02,SQ-FCB-07c,S5RA-PSF-bot,SQ-PSF-08 > "$R/qrun1.txt" 2>&1; fi
bash tools/scratch_query/stop_wait.sh "$O" 240 >> "$R/drive.log" 2>&1; cp "$O/srv/logs/latest.log" "$R/boot1.log" 2>/dev/null
$Q toml $C stem_fruit fertileSoilBoost true
if bootw; then $Q run "$O" tools/scratch_query/batches/r1103/p2_R2.qb "$R" --rows S5R2-00,S5RA-PSF-bot,SQ-PSF-08b > "$R/qrun2.txt" 2>&1; fi
bash tools/scratch_query/stop_wait.sh "$O" 240 >> "$R/drive.log" 2>&1; cp "$O/srv/logs/latest.log" "$R/boot2.log" 2>/dev/null; rm -rf "$O"
echo "CHAIN8 p2R2 done $(date +%H:%M:%S)" >> perf/runs/r1103/chain.log
