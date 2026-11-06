#!/bin/bash
# r1103 slice A driver (sA_setup.md): a1 (old pack 1.1.06 from deploy/latest, level sAo), a2 (current jars on that
# world), a3 (four config edits), g10a / g10b (SQ-GLD-10: boots that must stop), a4 (fresh B world sAb, no Ender's 1.2.2).
#   nohup bash tools/scratch_query/batches/r1103/drive_sA.sh [FROM_STEP] > perf/runs/r1103/sA/drive.log 2>&1 &
# Logs "STEP <name> <phase> <time>". a1 failing to boot is not fatal (rows marked setup-blocked, a2 preps a fresh level).
set -u
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
SCR=${SCR:-/tmp/claude-1000/-home-liam--local-share-atlauncher-instances-CreativeEngineering2/51e0db54-43f7-4a69-b393-9d5ee1c5b98d/scratchpad/r1103}
B=$INST/tools/scratch_query/batches/r1103
Q="python3 $INST/tools/scratch_query/qrun.py"
R=$INST/perf/runs/r1103/sA
export B0=$SCR/b0
export JFR_MAX=200m
OUT=$SCR/sA; OUT2=$SCR/sA2; OUT4=$SCR/sAb; D=$INST/deploy/latest
cd "$INST" || exit 1
mkdir -p "$R"
log() { echo "STEP $* $(date +%H:%M:%S)"; }
decouple() { for j in "$1"/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done; true; }
boot() {  # boot OUT [maxsecs] -> prints the wait_boot result, returns 0 on DONE
  while [ -n "$(bash tools/scratch_query/game_jvms.sh)" ]; do sleep 10; done
  rm -f "$1/srv/logs/latest.log"; mkdir -p "$1/old-crash"; mv "$1"/srv/crash-reports/* "$1/old-crash/" 2>/dev/null
  (bash tools/creative_labels_test/scratch_server.sh boot "$1" >/dev/null 2>&1 &)
  sleep 5; local r; r=$(bash tools/scratch_query/wait_boot.sh "$1" "${2:-900}"); log boot "$r"
  case $r in DONE*) return 0;; *) return 1;; esac
}
stopsrv() {
  bash tools/scratch_query/stop_wait.sh "$1" 240
  mkdir -p "$R/srvlogs"; cp "$1/srv/logs/latest.log" "$R/srvlogs/$2.log" 2>/dev/null
  cp "$1/srv/logs/kubejs/server.log" "$R/srvlogs/$2_kubejs.log" 2>/dev/null; true
}
run() { local o=$1 b=$2; shift 2; $Q run "$o" "$B/$b" "$R" "$@" > "$R/qrun_${b%.qb}$(echo "$*" | tr -dc 'A-Za-z0-9-').txt" 2>&1
  log "$b $*" "$(grep -h '^totals' "$R"/qrun_${b%.qb}*.txt | tail -1)"; }
probes() { local S=$1/srv/kubejs/server_scripts
  cp tools/scratch_query/zp_probe.js $S/zz_zp_probe.js; cp tools/scratch_query/zact_probe.js $S/zz_zact_probe.js
  cp tools/fungi_probe/zcount.js $S/zz_count.js; cp tools/scratch_query/zloot_probe.js $S/zz_zloot_probe.js
  cp tools/salt_probe/salt_probe.js $S/zz_salt_probe.js; cp tools/scratch_query/sA_probe.js $S/zz_sA_probe.js; }
A1OK=1
STEPS="a1 a2 a3 g10a g10b a4"
FROM=${1:-a1}; go=0
for st in $STEPS; do
  [ "$st" = "$FROM" ] && go=1; [ $go = 1 ] || continue
  log "$st" start; df -h / | tail -1 | awk '{print "disk free " $4}'
  case $st in
  a1) rm -rf "$OUT"; NOSWAP=1 bash tools/scratch_query/prep_srv.sh "$OUT" sAo A difficulty=normal spawn-monsters=true spawn-animals=true 2>&1 | tail -1
      ( cd "$OUT/srv" || exit 1
        rm -f mods/*.jar && cp "$D"/mods/*.jar mods/
        cp "$INST/development/creativeDevtools/members/perfbot/build/libs/ceperfbot-0.1.0.jar" mods/
        rm -rf kubejs config defaultconfigs && cp -r "$D/kubejs" "$D/config" "$D/defaultconfigs" .
        for u in vinery_upstream miners_upstream rustic_upstream farmers_upstream; do rm -f "$u"; ln -sfn "$D/$u" "$u"; done
        echo "new-id jars in old pack: $(ls mods | grep -ciE '^(creativeworldgen|creativedelight-|creativebrewing-)')" )
      if boot "$OUT"; then run "$OUT" sA_1.qb; else A1OK=0; log a1 "SETUP-BLOCKED: old pack did not boot"; fi
      stopsrv "$OUT" a1 ;;
  a2) rm -rf "$OUT2"; bash tools/scratch_query/prep_srv.sh "$OUT2" sAo A difficulty=normal spawn-monsters=true spawn-animals=true 2>&1 | tail -1
      [ $A1OK = 1 ] && [ -d "$OUT/srv/sAo" ] && cp -r "$OUT/srv/sAo" "$OUT2/srv/sAo"
      decouple "$OUT2"; probes "$OUT2"
      ( cd "$OUT2/srv" || exit 1
        rm -f config/creativeworldgen-common.toml
        $Q toml config/creativeengineering-common.toml named_mobs chance 0.02
        echo "old jars left: $(ls mods | grep -ciE '^(salt-|create_salt|CannedGoods|goldenfood|moreMooblooms|babyMobs)')" )
      boot "$OUT2" || { log ABORT a2; exit 1; }
      run "$OUT2" sA_2.qb; stopsrv "$OUT2" a2 ;;
  a3) ( cd "$OUT2/srv" || exit 1; T="$Q toml"
        $T config/salt-common.toml Dissolving SaltDissolvingChance 1.0
        $T config/creativeengineering-swaps.toml swappers onPlayerDataLoad false
        $T config/creativeengineering-swaps.toml swappers onCommand false
        $T config/enders_additions-common.toml growth chorusflameGrowthSlow 0
        mkdir -p ../moved_loot && mv kubejs/data/creativeworldgen/kubejs/loot/loot_config.json kubejs/data/creativeworldgen/kubejs/loot/loot_index.json ../moved_loot/ )
      probes "$OUT2"
      boot "$OUT2" || { log ABORT a3; exit 1; }
      run "$OUT2" sA_3.qb; stopsrv "$OUT2" a3 ;;
  g10a) ( cd "$OUT2/srv" || exit 1; [ -L goldenfood_upstream ] && rm goldenfood_upstream; mkdir -p goldenfood_upstream )
      boot "$OUT2" 300; bash tools/scratch_query/stop_wait.sh "$OUT2" 60 >/dev/null 2>&1
      run "$OUT2" sA_g10.qb --rows SQ-GLD-10a; cp "$OUT2/srv/logs/latest.log" "$R/srvlogs/g10a.log" 2>/dev/null ;;
  g10b) cp "$INST/mods/creativeLabels-1.0.0.jar" "$OUT2/srv/goldenfood_upstream/goldenfood-1.19.2-2.0.0.jar"
      boot "$OUT2" 300; bash tools/scratch_query/stop_wait.sh "$OUT2" 60 >/dev/null 2>&1
      run "$OUT2" sA_g10.qb --rows SQ-GLD-10b; cp "$OUT2/srv/logs/latest.log" "$R/srvlogs/g10b.log" 2>/dev/null
      ( cd "$OUT2/srv" || exit 1; [ ! -L goldenfood_upstream ] && rm -rf goldenfood_upstream; ln -sfn "$INST/goldenfood_upstream" goldenfood_upstream ) ;;
  a4) rm -rf "$OUT4"; bash tools/scratch_query/prep_srv.sh "$OUT4" sAb B difficulty=normal spawn-monsters=true spawn-animals=true 2>&1 | tail -1
      rm -f "$OUT4"/srv/mods/endersdelight-1.19-1.2.2.jar; decouple "$OUT4"; probes "$OUT4"
      if boot "$OUT4"; then run "$OUT4" sA_4.qb
      else log a4 "boot without Ender's 1.2.2 failed: SQ-ENM-03 FAIL; retry with the jar on level sAb2"
        cp -r "$OUT4/srv/crash-reports" "$R/a4_crash" 2>/dev/null; bash tools/scratch_query/stop_wait.sh "$OUT4" 60 >/dev/null 2>&1
        rm -rf "$OUT4"; bash tools/scratch_query/prep_srv.sh "$OUT4" sAb2 B difficulty=normal spawn-monsters=true spawn-animals=true 2>&1 | tail -1
        decouple "$OUT4"; probes "$OUT4"; boot "$OUT4" || { log ABORT a4; exit 1; }
        run "$OUT4" sA_4.qb --from SQ-VTDA-10; fi
      stopsrv "$OUT4" a4 ;;
  esac
  log "$st" end
done
log ALL end
