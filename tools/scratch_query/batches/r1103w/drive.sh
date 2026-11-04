#!/bin/bash
# r1103w driver (worldgen slice, scratch_run_2026-11-03.md section 9): the boots after W1, one JVM at a time.
#   nohup bash tools/scratch_query/batches/r1103w/drive.sh [FROM_STEP] > perf/runs/r1103w/drive.log 2>&1 &
# Steps: w1stop w2 w12 w3 w4 wA w9 w10 w11 c8o c8
#   w1stop waits for the W1 qrun (w_1_B.qb, started by hand) to print its totals, then stops W1.
#   w2 = W1b (w_2_B.qb), w12 = W1c (w_12_B.qb) on the W1 world; w3 deletes the W1 world after W2 (06d-x reads it).
#   c8o / c8 = SQ-CWG-08 village half (r1103/sA_R.qb part 2): 1.1.06 release world, then current jars on it.
# Setup per w_setup.md (worldgen keys are in creativeworldgen-common.toml since the wave 5 carve). Every step logs
# "STEP <name> <phase> <time>"; resume with FROM_STEP. Stops at the first boot that does not reach "Done (".
set -u
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/288946da-307e-457f-a955-0b504a02002b/scratchpad/r1103w
B=$INST/tools/scratch_query/batches/r1103w
Q="python3 $INST/tools/scratch_query/qrun.py"
R=$INST/perf/runs/r1103w
export JFR_MAX=200m
cd "$INST" || exit 1
log() { echo "STEP $* $(date +%H:%M:%S)"; }
prep() { bash tools/scratch_query/prep_srv.sh "$@" 2>&1 | tail -1; decouple "$1"; }
decouple() {  # copies instead of hard links (parallel installToMods / rebuilds)
  for j in "$1"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done; true; }
boot() {  # boot OUT [maxsecs]
  while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
  rm -f "$1/srv/logs/latest.log"; mkdir -p "$1/old-crash"; mv "$1"/srv/crash-reports/* "$1/old-crash/" 2>/dev/null
  (bash tools/creative_labels_test/scratch_server.sh boot "$1" >/dev/null 2>&1 &)
  sleep 5; bash tools/scratch_query/wait_boot.sh "$1" "${2:-900}"
}
stopsrv() {  # stopsrv OUT RESDIR NAME
  bash tools/scratch_query/stop_wait.sh "$1" 240
  mkdir -p "$2/srvlogs"; cp "$1/srv/logs/latest.log" "$2/srvlogs/$3.log" 2>/dev/null
  cp "$1/srv/logs/kubejs/server.log" "$2/srvlogs/$3_kubejs.log" 2>/dev/null; true
}
run() {  # run OUT BATCH RESDIR [qrun args]
  local o=$1 b=$2 r=$3; shift 3; mkdir -p "$r"
  $Q run "$o" "$b" "$r" "$@" > "$r/qrun_$(basename "${b%.qb}").txt" 2>&1
  log "$(basename "$b")" "$(grep '^totals' "$r/qrun_$(basename "${b%.qb}").txt")"
}
must() { "$@" || { log ABORT "$*"; exit 1; }; }
CW=config/creativeworldgen-common.toml
STEPS="w1stop w2 w12 w3 w4 wA w9 w10 w11 c8o c8"
FROM=${1:-w1stop}; go=0
for st in $STEPS; do
  [ "$st" = "$FROM" ] && go=1; [ $go = 1 ] || continue
  log "$st" start; df -h / | tail -1 | awk '{print "disk free " $4}'
  case $st in
  w1stop) until grep -q "^totals" "$R/w1_qrun.out"; do sleep 30; done; log w1 "$(grep '^totals' "$R/w1_qrun.out")"
       stopsrv "$SCR/w1" "$R/w" bootW1 ;;
  w2) O=$SCR/w1; sed -i '/^\s*searchTimeoutSeconds\s*=/d' "$O/srv/$CW"
       # fixed /zq probe (13:41 IllegalAccess, 15:2x "kind is not a function"); w_1r_B.qb (gen_w1r.py) reruns every W1 row
       # that uses /zq under or its judges, with the three W1 logs joined for the generation-time lines
       cp tools/scratch_query/zq_probe.js "$O/srv/kubejs/server_scripts/zz_zq_probe.js"
       must boot "$O" 1200; run "$O" "$B/w_2_B.qb" "$R/w"
       mkdir -p "$R/w1r"; $Q run "$O" "$B/w_1r_B.qb" "$R/w" > "$R/w1r/qrun_w1r.txt" 2>&1
       log w1r "$(grep '^totals' "$R/w1r/qrun_w1r.txt")"
       stopsrv "$O" "$R/w" bootW1b ;;
  w12) O=$SCR/w1
       must boot "$O" 1200; run "$O" "$B/w_12_B.qb" "$R/w"; stopsrv "$O" "$R/w" bootW1c ;;
  w3) O=$SCR/w2; prep "$O" efb6 B; T=$O/srv/$CW
       $Q toml $T worldgen.earth_fill log '"ALL"'; $Q toml $T worldgen.earth_fill waterReach 6
       must boot "$O" 1200; run "$O" "$B/w_3_B.qb" "$R/w"; stopsrv "$O" "$R/w" bootW2; rm -rf "$O" "$SCR/w1/srv/wge" ;;
  w4) O=$SCR/w3; prep "$O" efboff B; T=$O/srv/$CW
       $Q toml $T worldgen.systems earthFill false; $Q toml $T worldgen.locate searchTimeoutSeconds 5
       must boot "$O" 1200; run "$O" "$B/w_4_B.qb" "$R/w"; stopsrv "$O" "$R/w" bootW3; rm -rf "$O" ;;
  wA) O=$SCR/a; prep "$O" wga A
       must boot "$O"; run "$O" "$B/w_5_A.qb" "$R/w"; stopsrv "$O" "$R/w" bootA1
       $Q toml "$O/srv/$CW" worldgen.first_tick_water windowTicks 0
       must boot "$O"; run "$O" "$B/w_6_A.qb" "$R/w"; stopsrv "$O" "$R/w" bootA2
       must boot "$O"; run "$O" "$B/w_7_A.qb" "$R/w"; stopsrv "$O" "$R/w" bootA3
       must boot "$O"; run "$O" "$B/w_8_A.qb" "$R/w"; stopsrv "$O" "$R/w" bootA4; rm -rf "$O" ;;
  w9) O=$SCR/f1; prep "$O" ftw_on B level-seed=-8188900049893920259 difficulty=normal spawn-monsters=true spawn-animals=true view-distance=10 simulation-distance=8
       must boot "$O" 1200; run "$O" "$B/w_9_B.qb" "$R/w"; stopsrv "$O" "$R/w" bootF1; rm -rf "$O" ;;
  w10) O=$SCR/f2; prep "$O" ftw_off B level-seed=-8188900049893920259 difficulty=normal spawn-monsters=true spawn-animals=true view-distance=10 simulation-distance=8
       $Q toml "$O/srv/$CW" worldgen.systems firstTickWater false
       must boot "$O" 1200; run "$O" "$B/w_10_B.qb" "$R/w"; stopsrv "$O" "$R/w" bootF2; rm -rf "$O" ;;
  w11) O=$SCR/g1; prep "$O" cdj5c B spawn-animals=true
       cp tools/scratch_query/zq_biomegrid.js "$O/srv/kubejs/server_scripts/zz_zq_biomegrid.js"
       must boot "$O" 1200; run "$O" "$B/w_11_B.qb" "$R/w"; stopsrv "$O" "$R/w" bootG1; rm -rf "$O" ;;
  c8o) O=$SCR/c8o; D=$INST/deploy/latest   # r1103/sA_setup.md Boot 1: the 1.1.06 release as a whole
       NOSWAP=1 bash tools/scratch_query/prep_srv.sh "$O" sAo A difficulty=normal spawn-monsters=true spawn-animals=true 2>&1 | tail -1
       ( cd "$O/srv" || exit 1
         rm -f mods/*.jar && cp "$D"/mods/*.jar mods/
         rm -rf kubejs config defaultconfigs && cp -r "$D/kubejs" "$D/config" "$D/defaultconfigs" .
         for u in vinery_upstream miners_upstream rustic_upstream farmers_upstream; do rm -f "$u"; ln -sfn "$D/$u" "$u"; done )
       log c8o "new-id jars in old srv: $(ls "$O/srv/mods" | grep -ciE '^(creativeworldgen|creativedelight-|creativebrewing-)')"
       must boot "$O" 1200; run "$O" "$INST/tools/scratch_query/batches/r1103/sA_R.qb" "$R/c8" --rows SA-R-00,SQ-CWG-08o-R
       stopsrv "$O" "$R/c8" bootC8o ;;
  c8) O=$SCR/c8; prep "$O" sAo A difficulty=normal spawn-monsters=true spawn-animals=true
       rm -rf "$O/srv/sAo"; cp -r "$SCR/c8o/srv/sAo" "$O/srv/sAo"
       must boot "$O" 1200; run "$O" "$INST/tools/scratch_query/batches/r1103/sA_R.qb" "$R/c8" --rows SA-R-00,SQ-CWG-08-R
       stopsrv "$O" "$R/c8" bootC8; rm -rf "$O" "$SCR/c8o" ;;
  esac
  log "$st" end
done
log ALL done
