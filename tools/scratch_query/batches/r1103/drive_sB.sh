#!/bin/bash
# r1103 slice B driver: sB_1 (Creative Labels 1.0.0) then sB_2 (1.0.1) on one level sb1 (sB_setup.md).
#   nohup bash tools/scratch_query/batches/r1103/drive_sB.sh [FROM_STEP] > perf/runs/r1103/sB/drive.log 2>&1 &
#   rerun of chosen rows: LEVEL=sbr R=$INST/perf/runs/r1103/sBR ROWS2=SQ-LBS-01,... bash drive_sB.sh (boot 1 runs whole sB_1)
# Steps: b1 b2. Logs "STEP <name> <phase> <time>"; stops at the first boot that does not reach "Done (".
set -u
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=${SCR:-/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/51e0db54-43f7-4a69-b393-9d5ee1c5b98d/scratchpad/r1103}
B=$INST/tools/scratch_query/batches/r1103
Q="python3 $INST/tools/scratch_query/qrun.py"
R=${R:-$INST/perf/runs/r1103/sB}
O=$SCR/${LEVEL:-sb1}
export JFR_MAX=200m
cd "$INST" || exit 1
mkdir -p "$R"
log() { echo "STEP $* $(date +%H:%M:%S)"; }
decouple() { for j in "$1"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done; true; }
boot() {
  while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
  rm -f "$1/srv/logs/latest.log"; mkdir -p "$1/old-crash"; mv "$1"/srv/crash-reports/* "$1/old-crash/" 2>/dev/null
  (bash tools/creative_labels_test/scratch_server.sh boot "$1" >/dev/null 2>&1 &)
  sleep 5; bash tools/scratch_query/wait_boot.sh "$1" "${2:-900}"
}
stopsrv() {
  bash tools/scratch_query/stop_wait.sh "$1" 240
  mkdir -p "$R/srvlogs"; cp "$1/srv/logs/latest.log" "$R/srvlogs/$2.log" 2>/dev/null
  cp "$1/srv/logs/kubejs/server.log" "$R/srvlogs/$2_kubejs.log" 2>/dev/null; true
}
run() { $Q run "$O" "$B/$1" "$R" > "$R/qrun_${1%.qb}.txt" 2>&1; log "$1" "$(grep '^totals' "$R/qrun_${1%.qb}.txt")"; }
FROM=${1:-b1}; go=0
for st in b1 b2; do
  [ "$st" = "$FROM" ] && go=1; [ $go = 1 ] || continue
  log "$st" start; df -h / | tail -1 | awk '{print "disk free " $4}'
  case $st in
  b1) rm -rf "$O"; bash tools/scratch_query/prep_srv.sh "$O" ${LEVEL:-sb1} A 2>&1 | tail -1; decouple "$O"
      rm -f "$O"/srv/mods/creativeLabels-*.jar && cp "$INST/mods/creativeLabels-1.0.0.jar" "$O/srv/mods/"
      mkdir -p "$O/srv/defaultconfigs" && cp "$INST/configs_edited/server_config/solapplepie-server.toml" "$O/srv/defaultconfigs/"
      cp "$INST/tools/scratch_query/zlbs_probe.js" "$O/srv/kubejs/server_scripts/zz_zlbs_probe.js"
      r=$(boot "$O"); log b1 "boot $r"; case $r in DONE*) ;; *) log ABORT b1; exit 1;; esac
      run sB_1.qb; stopsrv "$O" boot1 ;;
  b2) rm -f "$O"/srv/mods/creativeLabels-*.jar
      cp "$(ls -t $INST/development/creativeLabels/build/libs/creativeLabels-1.0.1*.jar | grep -v sources | head -1)" "$O/srv/mods/"
      cp "$INST/tools/scratch_query/zsb_probe.js" "$O/srv/kubejs/server_scripts/zz_zsb_probe.js"
      r=$(boot "$O"); log b2 "boot $r"; case $r in DONE*) ;; *) log ABORT b2; exit 1;; esac
      if [ -n "${ROWS2:-}" ]; then $Q run "$O" "$B/sB_2.qb" "$R" --rows "$ROWS2" > "$R/qrun_sB_2.txt" 2>&1; log sB_2 "$(grep "^totals" "$R/qrun_sB_2.txt")"; else run sB_2.qb; fi
      stopsrv "$O" boot2 ;;
  esac
  log "$st" end
done
log ALL end
