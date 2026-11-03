#!/bin/bash
# r1102b rerun block (after drive.sh): s1_R.qb + r_A.qb on level s1a, r_B.qb on a fresh B world, r_B2.qb on it with
# wildCrops off (setup: r_setup.md, s1_R from perf/runs/r1102b/s1/judged.md). Jars: copies of the current mods/.
#   nohup bash tools/scratch_query/batches/r1102b/drive_r.sh [FROM] > perf/runs/r1102b/drive_r.log 2>&1 &
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
FROM=${1:-rA}; go=0
for st in rA rB rB2; do
  [ "$st" = "$FROM" ] && go=1; [ $go = 1 ] || continue
  log "$st" start
  case $st in
  rA) O=$SCR/s1a; S=$O/srv/kubejs/server_scripts
      for j in "$INST"/mods/creative*.jar; do rm -f "$O/srv/mods/$(basename "$j")"; cp "$j" "$O/srv/mods/"; done; decouple "$O"
      cp tools/vintage_probe/vintage_probe.js $S/zz_vtd_probe.js; cp tools/scratch_query/zq_probe.js $S/zz_zq_probe.js
      cp tools/scratch_query/zact_probe.js $S/zz_zact_probe.js; cp tools/scratch_query/zp_probe.js $S/zz_zp_probe.js
      cp tools/salt_probe/salt_probe.js $S/zz_salt_probe.js; cp tools/worldedit_probe/we_probe.js $S/zz_we_probe.js
      sed -i 's/^spawn-animals=.*/spawn-animals=true/' "$O/srv/server.properties"
      must boot "$O"; run "$O" s1_R.qb "$R/s1R"; run "$O" r_A.qb "$R/r"; stopsrv "$O" "$R/r" bootrA ;;
  rB) O=$SCR/rb; bash tools/scratch_query/prep_srv.sh "$O" rb B spawn-animals=true 2>&1 | tail -1; decouple "$O"
      must boot "$O"; run "$O" r_B.qb "$R/r"; stopsrv "$O" "$R/r" bootrB ;;
  rB2) O=$SCR/rb; $Q toml "$O/srv/config/creativedelight-common.toml" worldgen wildCrops false
      must boot "$O"; run "$O" r_B2.qb "$R/r"; stopsrv "$O" "$R/r" bootrB2; rm -rf "$O" ;;
  esac
  log "$st" end
done
log ALL done
