#!/usr/bin/env bash
# Runs SQ-CDNS-01, -04, -06, -02 (swapper rows, plan_cd_namespace.md 5.2) on fixture copies with the cdns_step2 jars.
#   cdns_step2_run.sh WORKDIR RES
# Jars: copies of mods/ (NOSWAP) plus jar_archive/cdns_step2/*. kubejs: the snapshot taken before the rename started.
# Writes RES/<row>.log (grepped [swaps] lines), RES/<row>.cmp (probe compare), RES/verdict_input.txt; last line ALLDONE.
set -uo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
W=${1:?workdir}; RES=${2:?results dir}
RUN=$INST/perf/runs/r1110
FIX=$RUN/fixture
SNAP=$RUN/kubejs_snapshot_0016
STEP2=$INST/jar_archive/cdns_step2
SS="bash $INST/tools/creative_labels_test/scratch_server.sh"
mkdir -p "$W" "$RES"
log() { echo "$(date +%H:%M:%S) $*" | tee -a "$RES/run.txt"; }

wait_clear() { for _ in $(seq 1 90); do [ -z "$(bash "$INST/tools/scratch_query/game_jvms.sh" 2>/dev/null)" ] && return 0; sleep 2; done; log "JVM STILL UP"; return 1; }

prep() { # OUT
  local out=$1
  wait_clear || return 1
  NOSWAP=1 bash "$INST/tools/scratch_query/prep_srv.sh" "$out" cdnsfix A >/dev/null 2>&1 || { log "prep failed $out"; return 1; }
  ( cd "$out/srv/mods" || exit 1
    for f in *.jar; do cp "$f" "$f.tmp" && mv "$f.tmp" "$f"; done
    for j in "$STEP2"/*.jar; do
      n=$(basename "$j"); stem=${n%%-*}
      rm -f ./"$stem"-*.jar; cp "$j" .
    done )
  rsync -a --delete --exclude exported --exclude generation_scripts --exclude server_scripts/zz_zq_probe.js \
    --exclude data/zq "$SNAP/" "$out/srv/kubejs/"
  rm -rf "$out/srv/cdnsfix"; cp -a "$FIX/world_pristine" "$out/srv/cdnsfix"
  log "prepped $out: $(ls -l --time-style=+%H:%M "$out/srv/mods" | grep -E 'creative(common|delight|equipment|compat)-' | awk '{print $6,$7}' | tr '\n' ' ')"
}

boot() { # OUT -> 0 when Done
  local out=$1 L=$1/srv/logs/latest.log
  wait_clear || return 1
  rm -f "$L"; rm -rf "$out/srv/crash-reports"
  $SS boot "$out" >/dev/null 2>&1
  for _ in $(seq 1 120); do
    if [ -f "$L" ] && grep -q 'Done (' "$L"; then
      # entities only tick after Done: hold so a tick crash shows up before any row trusts this boot
      sleep 25
      if ls "$out"/srv/crash-reports/* >/dev/null 2>&1; then log "TICK CRASH $out"; return 1; fi
      return 0
    fi
    ls "$out"/srv/crash-reports/* >/dev/null 2>&1 && { log "CRASH in boot $out"; return 1; }
    sleep 2
  done
  log "boot timeout $out"; return 1
}

finish() { # OUT ROW
  local out=$1 row=$2 L=$1/srv/logs/latest.log
  $SS cmd "$out" "save-all flush" >/dev/null 2>&1; sleep 6
  $SS stop "$out" >/dev/null 2>&1
  wait_clear
  grep -E '\[swaps\]|SwapReport|Swap.*Mixin|ERROR.*[Ss]wap|left missing' "$L" | cut -c1-260 > "$RES/$row.log"
  grep -cE 'Exception|Mixin apply failed' "$L" > "$RES/$row.errcount"
  python3 "$INST/tools/scratch_query/cdns_probe.py" scan "$out/srv/cdnsfix" "$RES/$row.json" 2>&1 | tail -1 > "$RES/$row.scan"
  python3 "$INST/tools/scratch_query/cdns_probe.py" compare "$FIX/baseline.json" "$RES/$row.json" > "$RES/$row.cmp" 2>&1
  echo "exit $?" >> "$RES/$row.cmp"
}

A=$W/s2a
prep "$A" || { log ALLDONE; exit 1; }
if boot "$A"; then log "01 boot done"; finish "$A" SQ-CDNS-01; else log "01 boot FAILED"; cp "$A/srv/logs/latest.log" "$RES/SQ-CDNS-01.boot.log"; log ALLDONE; exit 1; fi
for n in 2 3; do
  if boot "$A"; then log "04 boot $n done"; finish "$A" "SQ-CDNS-04_boot$n"; else log "04 boot $n FAILED"; cp "$A/srv/logs/latest.log" "$RES/SQ-CDNS-04_boot$n.boot.log"; fi
done
# 02 last on this copy: its /give and /data merge would change the counts the 04 compares read.
if boot "$A"; then
  C="$SS cmd $A"; L=$A/srv/logs/latest.log
  log "02 boot done"
  $C "pbot spawn cdnsOn 2 -60 20 minecraft:overworld survival" >/dev/null; sleep 3
  $C "give cdnsOn creativeengineering:wooden_bucket" >/dev/null; sleep 2
  $C "data get entity cdnsOn Inventory" >/dev/null; sleep 2
  $C "forceload add 300 300" >/dev/null; sleep 4
  $C "setblock 300 -60 300 minecraft:chest" >/dev/null; sleep 1
  $C 'data merge block 300 -60 300 {Items:[{Slot:0b,id:"creativeengineering:wooden_bucket",Count:1b}]}' >/dev/null; sleep 2
  $C "save-all flush" >/dev/null; sleep 6
  $C "forceload remove 300 300" >/dev/null; sleep 15
  $C "forceload add 300 300" >/dev/null; sleep 5
  $C "data get block 300 -60 300 Items" >/dev/null; sleep 2
  grep -v 'Error parsing' "$L" | grep -E 'has the following|Gave |Unknown item|unknown item|Incorrect argument' | cut -c1-900 > "$RES/SQ-CDNS-02.out"
  finish "$A" SQ-CDNS-02
else log "02 boot FAILED"; fi

B=$W/s2b
if prep "$B"; then
  python3 -I - "$B/srv/mods" <<'PY'
import glob, json, sys, zipfile
jar = glob.glob(sys.argv[1] + "/creativedelight-*.jar")[0]
with zipfile.ZipFile(jar, "a") as z:
    z.writestr("data/creativedelight/swaps/zz_test.json",
               json.dumps({"replaces": {"creativedelight:does_not_exist": ["creativeengineering:wooden_water_bucket"]}}))
print("zz_test added to", jar)
PY
  if boot "$B"; then log "06 boot done"; finish "$B" SQ-CDNS-06; else log "06 boot FAILED"; cp "$B/srv/logs/latest.log" "$RES/SQ-CDNS-06.boot.log"; fi
fi
log ALLDONE
