#!/bin/bash
# r1102b slice-5 rerun (after drive_r.sh): s5_R_A, s5_R_A2, s5_R_A3 on a fresh A world (s5_R_setup.md).
# old header: r1102b rerun block (after drive.sh): s1_R.qb + r_A.qb on level s1a, r_B.qb on a fresh B world, r_B2.qb on it with
# wildCrops off (setup: r_setup.md, s1_R from perf/runs/r1102b/s1/judged.md). Jars: copies of the current mods/.
#   nohup bash tools/scratch_query/batches/r1102b/drive_s5r.sh [FROM] > perf/runs/r1102b/drive_s5r.log 2>&1 &
set -u
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/133c37cb-cd14-46c8-b6a3-0d4c84f23691/scratchpad/r1102b
B=$INST/tools/scratch_query/batches/r1102b
Q="python3 $INST/tools/scratch_query/qrun.py"
R=$INST/perf/runs/r1102b
export JFR_MAX=200m
cd "$INST" || exit 1
log() { echo "STEP $* $(date +%H:%M:%S)"; }
decouple() { for j in "$1"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done; true; }
boot() {
  while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
  rm -f "$1/srv/logs/latest.log"; rm -rf "$1/srv/crash-reports"
  (bash tools/creative_labels_test/scratch_server.sh boot "$1" >/dev/null 2>&1 &)
  sleep 5; bash tools/scratch_query/wait_boot.sh "$1" "${2:-900}"
}
stopsrv() { bash tools/scratch_query/stop_wait.sh "$1" 240; mkdir -p "$2/srvlogs"; cp "$1/srv/logs/latest.log" "$2/srvlogs/$3.log" 2>/dev/null; true; }
run() { mkdir -p "$3"; $Q run "$1" "$B/$2" "$3" > "$3/qrun_${2%.qb}.txt" 2>&1; log "$2" "$(grep '^totals' "$3/qrun_${2%.qb}.txt")"; }
must() { "$@" || { log ABORT "$*"; exit 1; }; }
FROM=${1:-RA}; go=0
for st in RA RA2 RA3; do
  [ "$st" = "$FROM" ] && go=1; [ $go = 1 ] || continue
  log "$st" start
  O=$SCR/s5ra; C=$O/srv/config/creativedelight-common.toml
  case $st in
  RA) bash tools/scratch_query/prep_srv.sh "$O" s5ra A difficulty=normal spawn-monsters=true spawn-animals=true spawn-protection=16 2>&1 | tail -1; decouple "$O"
      cp tools/fungi_probe/zcount.js "$O/srv/kubejs/server_scripts/zz_count.js"; cp tools/scratch_query/s5_probe.js "$O/srv/kubejs/server_scripts/zz_s5_probe.js"
      if [ -f "$O/srv/config/vintagedelight-common.toml" ]; then
        $Q toml "$O/srv/config/vintagedelight-common.toml" pickling_jar remainderBuffer true; $Q toml "$O/srv/config/vintagedelight-common.toml" pickling_jar stallWhenFull false
      else $Q toml $C vintage.pickling_jar remainderBuffer true; $Q toml $C vintage.pickling_jar stallWhenFull false; fi
      for k in secondFruitMinutes thirdFruitMinutes fourthFruitMinutes; do $Q toml $C stem_fruit $k 0; done
      $Q toml $C stem_fruit maxFruits 3; $Q toml $C stem_fruit fertileSoilBoost false
      must boot "$O"; run "$O" s5_R_A.qb "$R/s5r"; stopsrv "$O" "$R/s5r" bootRA ;;
  RA2) $Q toml $C vintage.pickling_jar remainderBuffer false; $Q toml $C stem_fruit fertileSoilBoost true; $Q toml $C qol cookwarePickup false
      must boot "$O"; run "$O" s5_R_A2.qb "$R/s5r"; stopsrv "$O" "$R/s5r" bootRA2 ;;
  RA3) $Q toml $C vintage.pickling_jar overflowEjectDelayTicks 0
      must boot "$O"; run "$O" s5_R_A3.qb "$R/s5r"; stopsrv "$O" "$R/s5r" bootRA3; rm -rf "$O" ;;
  esac
  log "$st" end
done
log ALL done
