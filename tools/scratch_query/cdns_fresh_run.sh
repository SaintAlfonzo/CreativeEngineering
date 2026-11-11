#!/usr/bin/env bash
# SQ-CDNS-07 rerun and batches/r1110/cdns_10_11.qb on one fresh world with the full build (GROUP=A or B, default A; B needs the crow fix).
#   cdns_fresh_run.sh OUT RES
set -uo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"; Q=$INST/tools/scratch_query; OUT=${1:?OUT}; RES=${2:?RES}
[ -z "$(bash "$Q/game_jvms.sh")" ] || { echo "a game JVM is up"; exit 1; }
mkdir -p "$RES"
NOSWAP=1 bash "$Q/prep_srv.sh" "$OUT" cdnsfresh "${GROUP:-A}" 2>&1 | tail -1 | cut -c1-120
cd "$OUT/srv/mods" || exit 1
for f in *.jar; do cp "$f" "$f.tmp" && mv "$f.tmp" "$f"; done
for j in $(ls "$INST"/jar_archive/cdns_full/*.jar | grep -v -- '-own') "$INST/jar_archive/cdns_step2/creativeequipment-1.0.0.jar"; do n=$(basename "$j"); rm -f ./"${n%%-*}"-*.jar; cp "$j" .; done
ls -l --time-style=+%H:%M creative{common,delight}-*.jar | awk '{print $6,$7}'
cd "$INST" || exit 1
bash tools/creative_labels_test/scratch_server.sh boot "$OUT" >/dev/null 2>&1
r=$(bash "$Q/wait_boot.sh" "$OUT" 900); echo "boot: $r"
case "$r" in DONE*) python3 "$Q/qrun.py" run "$OUT" "$Q/batches/r1110/cdns_10_11.qb" "$RES" | tail -12 | cut -c1-200;; esac
sleep 3; cp "$OUT/srv/logs/latest.log" "$RES/SQ-CDNS-07.latest.log"
bash "$Q/stop_wait.sh" "$OUT"
L=$RES/SQ-CDNS-07.latest.log
echo "alias lines: $(grep -c 'permanent alias' "$L")"; grep 'permanent alias' "$L" | sed -E 's/^.*\]: //' | cut -c1-100
echo "replaceItems canned: $(grep -c 'replaceItems.*canned_' "$L")"
ls "$OUT"/srv/crash-reports 2>/dev/null | head -2
echo FRESHDONE
