#!/usr/bin/env bash
# Runs SQ-CDNS-09, -03 (two boots) and -05 (rollback) of plan_cd_namespace.md 5.2 through cdns_boot_check.sh.
#   cdns_full_run.sh WORK RES ROLLBACK_WORLD
# ROLLBACK_WORLD: a save last written by the step2 or full jars. Last line of RES/run.txt is ALLDONE.
set -uo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
W=${1:?work}; RES=${2:?res}; ROLL=${3:?rollback world}
CHK="bash $INST/tools/scratch_query/cdns_boot_check.sh"
mkdir -p "$W" "$RES"
FULL=$(ls "$INST"/jar_archive/cdns_full/*.jar | grep -v -- '-own')
EQ=$INST/jar_archive/cdns_step2/creativeequipment-1.0.0.jar
SNAP=$INST/perf/runs/r1110/kubejs_snapshot_0016
note() { echo "$(date +%H:%M:%S) $*" | tee -a "$RES/run.txt"; }

BOTS="pbot spawn cdnsOn 2 -60 20 minecraft:overworld survival;pbot spawn cdnsOff 6 -60 20 minecraft:overworld survival"
note "SQ-CDNS-09 and 03 boot 1"
# shellcheck disable=SC2086
HOLD=70 CMDS="$BOTS;data get block 0 -60 16;data get block 2 -60 16" $CHK "$W/f1" "$RES" SQ-CDNS-09 current $FULL "$EQ" 2>&1 | cut -c1-220
cp "$W/f1/srv/logs/latest.log" "$RES/SQ-CDNS-09.latest.log"
note "SQ-CDNS-03 boot 2"
REUSE=1 HOLD=30 CMDS="data get block 0 -60 16;data get block 2 -60 16" $CHK "$W/f1" "$RES" SQ-CDNS-03_boot2 current 2>&1 | cut -c1-220
cp "$W/f1/srv/logs/latest.log" "$RES/SQ-CDNS-03_boot2.latest.log"
note "SQ-CDNS-05 rollback on the mods/ jars"
WORLD=$ROLL HOLD=30 $CHK "$W/f2" "$RES" SQ-CDNS-05 "$SNAP" 2>&1 | cut -c1-220
cp "$W/f2/srv/logs/latest.log" "$RES/SQ-CDNS-05.latest.log"
note ALLDONE
