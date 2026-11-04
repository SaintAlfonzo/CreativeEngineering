#!/bin/bash
# r1103 phase 2: the r1102b rerun blocks (perf/runs/r1102b/HANDOVER.md section 4) on FRESH worlds with today's jars
# (the r1102b worlds were made on patcherMod 1.1.07 / CD 14:01). Batches stay in batches/r1102b/; results go to
# perf/runs/r1103/p2/.
#   nohup bash tools/scratch_query/batches/r1103/drive_p2.sh [FROM_STEP] > perf/runs/r1103/p2/drive.log 2>&1 &
# Steps: rA rB rB2 RA RA2 RA3 B B2. Logs "STEP <name> <phase> <time>"; stops at the first boot that does not reach "Done (".
#   rA  fresh A world s1a: s1_R.qb, r_A.qb (s1_setup.md Boot A probes + drive_r.sh rA probes; rows re-capture markers)
#   rB / rB2  fresh B world rb: r_B.qb, then wildCrops false and r_B2.qb (as drive_r.sh)
#   RA / RA2 / RA3  fresh A world s5ra: s5_R_A*.qb (as drive_s5r.sh)
#   B / B2  fresh B world s5b: s5_B.qb --rows SQ-PSM-06a (monument captures), then s5_B2.qb (s5_setup.md)
set -u
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=${SCR:-/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/51e0db54-43f7-4a69-b393-9d5ee1c5b98d/scratchpad/r1103}
B=$INST/tools/scratch_query/batches/r1102b
Q="python3 $INST/tools/scratch_query/qrun.py"
R=$INST/perf/runs/r1103/p2
export JFR_MAX=200m
cd "$INST" || exit 1
mkdir -p "$R"
log() { echo "STEP $* $(date +%H:%M:%S)"; }
prep() { rm -rf "$1"; bash tools/scratch_query/prep_srv.sh "$@" 2>&1 | tail -1; decouple "$1"; }
decouple() { for j in "$1"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done; true; }
boot() {
  while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
  rm -f "$1/srv/logs/latest.log"; mkdir -p "$1/old-crash"; mv "$1"/srv/crash-reports/* "$1/old-crash/" 2>/dev/null
  (bash tools/creative_labels_test/scratch_server.sh boot "$1" >/dev/null 2>&1 &)
  sleep 5; local r; r=$(bash tools/scratch_query/wait_boot.sh "$1" "${2:-900}"); log boot "$r"
  case $r in DONE*) return 0;; *) return 1;; esac
}
stopsrv() {
  bash tools/scratch_query/stop_wait.sh "$1" 240
  mkdir -p "$2/srvlogs"; cp "$1/srv/logs/latest.log" "$2/srvlogs/$3.log" 2>/dev/null
  cp "$1/srv/logs/kubejs/server.log" "$2/srvlogs/$3_kubejs.log" 2>/dev/null; true
}
run() { local o=$1 b=$2 res=$3; shift 3; mkdir -p "$res"; $Q run "$o" "$B/$b" "$res" "$@" > "$res/qrun_${b%.qb}.txt" 2>&1
  log "$b" "$(grep '^totals' "$res/qrun_${b%.qb}.txt")"; }
must() { "$@" || { log ABORT "$*"; exit 1; }; }
probes() { local o=$1; shift; for p in "$@"; do cp "$INST/${p%%:*}" "$o/srv/kubejs/server_scripts/${p##*:}"; done; }
STEPS="rA rB rB2 RA RA2 RA3 B B2"
FROM=${1:-rA}; go=0
for st in $STEPS; do
  [ "$st" = "$FROM" ] && go=1; [ $go = 1 ] || continue
  log "$st" start; df -h / | tail -1 | awk '{print "disk free " $4}'
  case $st in
  rA) O=$SCR/s1a; prep "$O" s1a A spawn-animals=true
      probes "$O" tools/vintage_probe/vintage_probe.js:zz_vtd_probe.js tools/kitchen_probe/cfb_kitchen_probe.js:zz_kt_probe.js \
        tools/fungi_probe/zcount.js:zz_count.js tools/fungi_probe/ztrades.js:zz_trades.js tools/scratch_query/zloot_probe.js:zz_zloot_probe.js \
        tools/scratch_query/zti_probe.js:zz_zti_probe.js tools/scratch_query/zact_probe.js:zz_zact_probe.js tools/scratch_query/zp_probe.js:zz_zp_probe.js \
        tools/salt_probe/salt_probe.js:zz_salt_probe.js tools/worldedit_probe/we_probe.js:zz_we_probe.js
      must boot "$O"; run "$O" s1_R.qb "$R/s1R"; run "$O" r_A.qb "$R/r"
      run "$O" s1_A.qb "$R/vp06" --rows SQ-VP-06   # SQ-CDJ7 rerun of SQ-VP-06 (first half; the jar fill is judged by look)
      stopsrv "$O" "$R/r" bootrA; rm -rf "$O" ;;
  rB) O=$SCR/rb; prep "$O" rb B spawn-animals=true
      must boot "$O"; run "$O" r_B.qb "$R/r"; stopsrv "$O" "$R/r" bootrB ;;
  rB2) O=$SCR/rb; $Q toml "$O/srv/config/creativedelight-common.toml" worldgen wildCrops false
      must boot "$O"; run "$O" r_B2.qb "$R/r"; stopsrv "$O" "$R/r" bootrB2; rm -rf "$O" ;;
  RA|RA2|RA3) O=$SCR/s5ra; C=$O/srv/config/creativedelight-common.toml
      case $st in
      RA) prep "$O" s5ra A difficulty=normal spawn-monsters=true spawn-animals=true spawn-protection=16
          probes "$O" tools/fungi_probe/zcount.js:zz_count.js tools/scratch_query/s5_probe.js:zz_s5_probe.js
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
      esac ;;
  B) O=$SCR/s5b; prep "$O" s5b B difficulty=normal spawn-monsters=true spawn-animals=true
     probes "$O" tools/fungi_probe/zcount.js:zz_count.js tools/scratch_query/s5_probe.js:zz_s5_probe.js
     must boot "$O"; run "$O" s5_B.qb "$R/s5b" --rows SQ-PSM-06a; stopsrv "$O" "$R/s5b" bootB ;;
  B2) O=$SCR/s5b; must boot "$O"; run "$O" s5_B2.qb "$R/s5b"; stopsrv "$O" "$R/s5b" bootB2; rm -rf "$O" ;;
  esac
  log "$st" end
done
log ALL end
