#!/bin/bash
# r1106spfx SQ-SPFX-05: Creative jar SETS out one boot at a time on the a1/a2 save.
#   jx_run.sh OUT "name:jar jar ..." ...     e.g. "delight:delight brewing" removes creativedelight + creativebrewing
# Writes jx_<name>.qb from jx.qb; results perf/runs/r1106spfx/jx_<name>/.
cd "$(dirname "$0")/../../../.." || exit 1
OUT=${1:?OUT}; shift; Q=tools/scratch_query; B=$Q/batches/r1106spfx; mkdir -p "$OUT/held"
for spec in "$@"; do
  name=${spec%%:*}; jars=${spec#*:}; R=perf/runs/r1106spfx/jx_$name; mkdir -p "$R"
  echo "== $name ($jars) $(date +%H:%M:%S)"
  [ -n "$(bash $Q/game_jvms.sh)" ] && { echo "a game JVM is up"; exit 1; }
  for jj in $jars; do mv "$OUT"/srv/mods/creative$jj-*.jar "$OUT/held/" || echo "no jar $jj"; done
  sed "s/NAME/$name/g" $B/jx.qb > $B/jx_$name.qb
  bash tools/creative_labels_test/scratch_server.sh boot "$OUT" >/dev/null 2>&1 &
  sleep 2; r=$(bash $Q/wait_boot.sh "$OUT" 500); echo "boot: $r" | head -2
  case "$r" in DONE*) python3 $Q/qrun.py run "$OUT" $B/jx_$name.qb $R 2>&1 | tail -1;;
    *) cp "$OUT"/srv/crash-reports/* $R/ 2>/dev/null; rm -f "$OUT"/srv/crash-reports/*;;
  esac
  gzip -c "$OUT/srv/logs/latest.log" > $R/latest.log.gz 2>/dev/null
  cp "$OUT"/srv/logs/kubejs/server.log $R/kjs_server.log 2>/dev/null
  bash $Q/stop_wait.sh "$OUT" 180 | head -1
  until [ -z "$(bash $Q/game_jvms.sh)" ]; do sleep 2; done
  mv "$OUT"/held/*.jar "$OUT/srv/mods/"
done
echo JX-DONE
