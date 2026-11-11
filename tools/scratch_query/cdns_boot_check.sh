#!/usr/bin/env bash
# One fixture-copy boot on a named jar set, for the Creative Delight namespace rows and for bisecting a boot or tick crash.
#   cdns_boot_check.sh OUT RES NAME KUBEJS JAR [JAR ...]
# OUT: scratch dir. RES: results dir. NAME: file stem in RES. KUBEJS: "current" (the instance tree) or a snapshot dir.
# JAR: jars copied over the mods/ set in OUT/srv/mods, matched by the file name part before the first '-'.
# Server stays up HOLD seconds (default 30) after Done so entities tick. Writes RES/NAME.{txt,log,json,cmp}; prints a verdict.
set -uo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; RES=${2:?RES}; NAME=${3:?NAME}; KJS=${4:?kubejs}; shift 4
HOLD=${HOLD:-30}
FIX=$INST/perf/runs/r1110/fixture
SS="bash $INST/tools/creative_labels_test/scratch_server.sh"
mkdir -p "$RES"
clear_jvm() { for _ in $(seq 1 90); do [ -z "$(bash "$INST/tools/scratch_query/game_jvms.sh" 2>/dev/null)" ] && return 0; sleep 2; done; echo "JVM STILL UP"; exit 1; }
clear_jvm
# REUSE=1: boot the world and jars already in OUT again (second and third boots of a row).
if [ -z "${REUSE:-}" ]; then
  NOSWAP=1 bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" cdnsfix A >/dev/null 2>&1 || { echo "prep failed"; exit 1; }
  cd "$OUT/srv/mods" || exit 1
  for f in *.jar; do cp "$f" "$f.tmp" && mv "$f.tmp" "$f"; done
  for j in "$@"; do n=$(basename "$j"); rm -f ./"${n%%-*}"-*.jar; cp "$j" .; done
  cd "$OUT/srv" || exit 1
  if [ "$KJS" != current ]; then
    rsync -a --delete --exclude exported --exclude generation_scripts --exclude server_scripts/zz_zq_probe.js --exclude data/zq "$KJS/" kubejs/
  fi
  # FRESH=1: no fixture, the server generates a new world (the census at the end has nothing to compare).
  rm -rf cdnsfix; [ -n "${FRESH:-}" ] || cp -a "${WORLD:-$FIX/world_pristine}" cdnsfix
fi
cd "$OUT/srv" || exit 1
rm -rf crash-reports
L=logs/latest.log; rm -f "$L"
$SS boot "$OUT" >/dev/null 2>&1
state=timeout
for _ in $(seq 1 120); do
  [ -f "$L" ] && grep -q 'Done (' "$L" && { state=done; break; }
  ls crash-reports/* >/dev/null 2>&1 && { state=boot-crash; break; }
  sleep 2
done
if [ "$state" = done ]; then
  for _ in $(seq 1 "$HOLD"); do sleep 1; ls crash-reports/* >/dev/null 2>&1 && { state=tick-crash; break; }; done
fi
# CMDS: console commands separated by ';', run after the hold (bot logins, /data get).
if [ "$state" = done ] && [ -n "${CMDS:-}" ]; then
  IFS=';' read -ra list <<< "$CMDS"
  for c in "${list[@]}"; do $SS cmd "$OUT" "$c" >/dev/null 2>&1; sleep 3; done
  ls crash-reports/* >/dev/null 2>&1 && state=tick-crash
fi
[ "$state" = done ] && { $SS cmd "$OUT" "save-all flush" >/dev/null 2>&1; sleep 6; }
$SS stop "$OUT" >/dev/null 2>&1
clear_jvm
{
  echo "state: $state"
  echo "jars: $(ls -l --time-style=+%H:%M mods | grep -E 'creative(common|delight|equipment|compat|engineering|worldgen)-' | awk '{print $6,$7}' | tr '\n' ' ')"
  ls crash-reports 2>/dev/null | head -2
  grep -hE '^Description|^java.lang|^Caused by' crash-reports/* 2>/dev/null | cut -c1-200 | head -4
  echo "alias lines: $(grep -c 'permanent alias' "$L"), load report: $(grep -c 'load report' "$L"), unresolved: $(grep -c 'swaps\] unresolved' "$L")"
} | tee "$RES/$NAME.txt"
grep -E '\[swaps\]' "$L" | cut -c1-240 > "$RES/$NAME.log"
[ -n "${FRESH:-}" ] && exit 0
python3 "$INST/tools/scratch_query/cdns_probe.py" scan cdnsfix "$RES/$NAME.json" 2>&1 | tail -1 | cut -c1-200 | tee -a "$RES/$NAME.txt"
python3 "$INST/tools/scratch_query/cdns_probe.py" compare "$FIX/baseline.json" "$RES/$NAME.json" > "$RES/$NAME.cmp" 2>&1
echo "compare exit $?, $(wc -l < "$RES/$NAME.cmp") lines" | tee -a "$RES/$NAME.txt"
