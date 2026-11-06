#!/bin/bash
# r1102b driver: runs the boots after s2_A in order (setup per <slice>_setup.md), one JVM at a time.
#   nohup bash tools/scratch_query/batches/r1102b/drive.sh [FROM_STEP] > perf/runs/r1102b/drive.log 2>&1 &
# Steps: s2Astop s2B s2Coff s2N s2RDstop s3A s3A2 s5A s5A2 s5B s5B2 wA w1 w2 w3 w4 w9 w10 w11
# Stops at the first boot that does not reach "Done (" (except s2RDstop, which must not). Every step logs a line
# "STEP <name> <phase> <time>" so a later session can resume with FROM_STEP.
set -u
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/133c37cb-cd14-46c8-b6a3-0d4c84f23691/scratchpad/r1102b
B=$INST/tools/scratch_query/batches/r1102b
Q="python3 $INST/tools/scratch_query/qrun.py"
R=$INST/perf/runs/r1102b
export JFR_MAX=200m
cd "$INST" || exit 1
log() { echo "STEP $* $(date +%H:%M:%S)"; }
prep() { bash tools/scratch_query/prep_srv.sh "$@" 2>&1 | tail -1; decouple "$1"; }
decouple() {  # replace hard-linked jars by copies (a parallel installToMods rewrote linked jars mid-boot, 18:41)
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
run() {  # run OUT BATCH RESDIR
  mkdir -p "$3"; $Q run "$1" "$B/$2" "$3" > "$3/qrun_${2%.qb}.txt" 2>&1
  log "$2" "$(grep '^totals' "$3/qrun_${2%.qb}.txt")"
}
must() { "$@" || { log ABORT "$*"; exit 1; }; }
probes() {  # probes OUT file:target ...
  local o=$1; shift; for p in "$@"; do cp "$INST/${p%%:*}" "$o/srv/kubejs/server_scripts/${p##*:}"; done
}
STEPS="s2Astop s2B s2Coff s2N s2RDstop s3A s3A2 s5A s5A2 s5B s5B2 wA w1 w2 w3 w4 w9 w10 w11"
FROM=${1:-s2Astop}; go=0
for st in $STEPS; do
  [ "$st" = "$FROM" ] && go=1; [ $go = 1 ] || continue
  log "$st" start; df -h / | tail -1 | awk '{print "disk free " $4}'
  case $st in
  s2Astop) until grep -q "^totals" "$R/s2/qrun_A.txt"; do sleep 20; done; log s2A "$(grep "^totals" "$R/s2/qrun_A.txt")"
       stopsrv "$SCR/s2a" "$R/s2" bootA ;;
  s2B) O=$SCR/s2b; prep "$O" s2b B; probes "$O" tools/fungi_probe/zcount.js:zz_count.js
       must boot "$O"; run "$O" s2_B.qb "$R/s2"; stopsrv "$O" "$R/s2" bootB; rm -rf "$O/srv/s2b" ;;
  s2Coff) O=$SCR/s2c; prep "$O" s2c B; probes "$O" tools/fungi_probe/zcount.js:zz_count.js
       T=$O/srv/config/creativedelight-common.toml
       $Q toml $T qol remainderBuffer false; $Q toml $T qol compost false; $Q toml $T qol manualOnlyRoutes false
       $Q toml $T worldgen wildCrops false; cp "$SCR/s2a/cdq05_on.json" "$O/" 2>/dev/null || log s2Coff "no cdq05_on.json"
       must boot "$O"; run "$O" s2_Coff.qb "$R/s2"; stopsrv "$O" "$R/s2" bootCoff; rm -rf "$O" ;;
  s2N) O=$SCR/s2n; prep "$O" s2n B; probes "$O" tools/fungi_probe/zcount.js:zz_count.js
       rm -f "$O"/srv/mods/fruitsdelight-0.5.11.jar "$O"/srv/mods/FarmersRespite-1.19-2.0.jar
       [ -d "$O/srv/kubejs/data/creativedelight" ] && [ ! -L "$O/srv/kubejs" ] && rm -r "$O/srv/kubejs/data/creativedelight"
       must boot "$O"; run "$O" s2_N.qb "$R/s2"; stopsrv "$O" "$R/s2" bootN ;;
  s2RDstop) O=$SCR/s2n; [ -L "$O/srv/rustic_upstream" ] && rm "$O/srv/rustic_upstream"
       boot "$O" 300; log s2RDstop "boot result $?"
       sleep 20; bash tools/scratch_query/stop_wait.sh "$O" 30
       run "$O" s2_RDstop.qb "$R/s2"; mkdir -p "$R/s2/srvlogs"; cp "$O/srv/logs/latest.log" "$R/s2/srvlogs/bootRDstop.log"; rm -rf "$O" ;;
  s3A) O=$SCR/s3a; prep "$O" s3a A spawn-animals=true
       probes "$O" tools/cdb_en_probe/cdb_en_probe.js:zz_cdb_en_probe.js tools/salt_probe/salt_probe.js:zz_salt_probe.js \
         tools/flower_pot_test/pot_probe.js:zz_pot_probe.js tools/armor_trims_cache_test/atc_probe.js:zz_atc_probe.js \
         tools/worldedit_probe/we_probe.js:zz_we_probe.js tools/fungi_probe/zcount.js:zz_count.js tools/scratch_query/zact_probe.js:zz_zact_probe.js
       ls "$O/srv/mods" | grep -i 'NethersDelight-1.19-3.1' && log s3A "WARN standalone NethersDelight in srv/mods"
       grep -n 'SaltDissolvingInRain' "$O/srv/config/salt-common.toml"
       must boot "$O"; run "$O" s3_A.qb "$R/s3"; stopsrv "$O" "$R/s3" bootA ;;
  s3A2) O=$SCR/s3a; [ -L "$O/srv/farmers_upstream" ] && rm "$O/srv/farmers_upstream"
       must boot "$O"; run "$O" s3_A2.qb "$R/s3"; stopsrv "$O" "$R/s3" bootA2; rm -rf "$O" ;;
  s5A) O=$SCR/s5a; prep "$O" s5a A difficulty=normal spawn-monsters=true spawn-animals=true spawn-protection=16
       probes "$O" tools/fungi_probe/zcount.js:zz_count.js tools/scratch_query/s5_probe.js:zz_s5_probe.js
       C=$O/srv/config/creativedelight-common.toml
       if [ -f "$O/srv/config/vintagedelight-common.toml" ]; then
         $Q toml "$O/srv/config/vintagedelight-common.toml" pickling_jar remainderBuffer true
         $Q toml "$O/srv/config/vintagedelight-common.toml" pickling_jar stallWhenFull false
       else
         $Q toml $C vintage.pickling_jar remainderBuffer true; $Q toml $C vintage.pickling_jar stallWhenFull false
       fi
       for k in secondFruitMinutes thirdFruitMinutes fourthFruitMinutes; do $Q toml $C stem_fruit $k 0; done
       $Q toml $C stem_fruit maxFruits 3; $Q toml $C stem_fruit fertileSoilBoost false
       must boot "$O"; run "$O" s5_A.qb "$R/s5"; stopsrv "$O" "$R/s5" bootA ;;
  s5A2) O=$SCR/s5a; C=$O/srv/config/creativedelight-common.toml
       $Q toml $C vintage.pickling_jar remainderBuffer false; $Q toml $C stem_fruit fertileSoilBoost true; $Q toml $C qol cookwarePickup false
       must boot "$O"; run "$O" s5_A2.qb "$R/s5"; stopsrv "$O" "$R/s5" bootA2; rm -rf "$O" ;;
  s5B) O=$SCR/s5b; prep "$O" s5b B difficulty=normal spawn-monsters=true spawn-animals=true
       probes "$O" tools/fungi_probe/zcount.js:zz_count.js tools/scratch_query/s5_probe.js:zz_s5_probe.js
       must boot "$O"; run "$O" s5_B.qb "$R/s5"; stopsrv "$O" "$R/s5" bootB ;;
  s5B2) O=$SCR/s5b; must boot "$O"; run "$O" s5_B2.qb "$R/s5"; stopsrv "$O" "$R/s5" bootB2; rm -rf "$O" ;;
  wA) O=$SCR/a; prep "$O" wga A
       must boot "$O"; run "$O" w_5_A.qb "$R/w"; stopsrv "$O" "$R/w" bootA1
       $Q toml "$O/srv/config/creativeengineering-common.toml" worldgen.first_tick_water windowTicks 0
       must boot "$O"; run "$O" w_6_A.qb "$R/w"; stopsrv "$O" "$R/w" bootA2
       must boot "$O"; run "$O" w_7_A.qb "$R/w"; stopsrv "$O" "$R/w" bootA3
       must boot "$O"; run "$O" w_8_A.qb "$R/w"; stopsrv "$O" "$R/w" bootA4; rm -rf "$O" ;;
  w1) O=$SCR/w1; prep "$O" wge B; T=$O/srv/config/creativeengineering-common.toml
       $Q toml $T worldgen.earth_fill log '"ALL"'; $Q toml $T worldgen.meta_structures log true; mkdir -p "$R/w"
       must boot "$O" 1200; run "$O" w_1_B.qb "$R/w"; stopsrv "$O" "$R/w" bootW1 ;;
  w2) O=$SCR/w1; sed -i '/^\s*searchTimeoutSeconds\s*=/d' "$O/srv/config/creativeengineering-common.toml"
       must boot "$O" 1200; run "$O" w_2_B.qb "$R/w"; stopsrv "$O" "$R/w" bootW1b ;;
  w3) O=$SCR/w2; prep "$O" efb6 B; T=$O/srv/config/creativeengineering-common.toml
       $Q toml $T worldgen.earth_fill log '"ALL"'; $Q toml $T worldgen.earth_fill waterReach 6
       must boot "$O" 1200; run "$O" w_3_B.qb "$R/w"; stopsrv "$O" "$R/w" bootW2; rm -rf "$O" "$SCR/w1/srv/wge" ;;
  w4) O=$SCR/w3; prep "$O" efboff B; T=$O/srv/config/creativeengineering-common.toml
       $Q toml $T worldgen.systems earthFill false; $Q toml $T worldgen.locate searchTimeoutSeconds 5
       must boot "$O" 1200; run "$O" w_4_B.qb "$R/w"; stopsrv "$O" "$R/w" bootW3; rm -rf "$O" ;;
  w9) O=$SCR/f1; prep "$O" ftw_on B level-seed=-8188900049893920259 difficulty=normal spawn-monsters=true spawn-animals=true view-distance=10 simulation-distance=8
       must boot "$O" 1200; run "$O" w_9_B.qb "$R/w"; stopsrv "$O" "$R/w" bootF1; rm -rf "$O" ;;
  w10) O=$SCR/f2; prep "$O" ftw_off B level-seed=-8188900049893920259 difficulty=normal spawn-monsters=true spawn-animals=true view-distance=10 simulation-distance=8
       $Q toml "$O/srv/config/creativeengineering-common.toml" worldgen.systems firstTickWater false
       must boot "$O" 1200; run "$O" w_10_B.qb "$R/w"; stopsrv "$O" "$R/w" bootF2; rm -rf "$O" ;;
  w11) O=$SCR/g1; prep "$O" cdj5c B spawn-animals=true; probes "$O" tools/scratch_query/zq_biomegrid.js:zz_zq_biomegrid.js
       must boot "$O" 1200; run "$O" w_11_B.qb "$R/w"; stopsrv "$O" "$R/w" bootG1; rm -rf "$O" ;;
  esac
  log "$st" end
done
log ALL done
