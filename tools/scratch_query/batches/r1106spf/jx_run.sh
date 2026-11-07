#!/bin/bash
# r1106spf SQ-SPF-111: one Creative jar out at a time on the spf save. jx_run.sh OUT [jar ...]
cd "$(dirname "$0")/../../../.." || exit 1
OUT=${1:?OUT}; shift; Q=tools/scratch_query; B=$Q/batches/r1106spf; mkdir -p "$OUT/held"
for j in "${@:-compat magic equipment worldgen calendar}"; do for jj in $j; do
  echo "== $jj $(date +%H:%M:%S)"
  [ -n "$(bash $Q/game_jvms.sh)" ] && { echo "a game JVM is up"; exit 1; }
  mv "$OUT"/srv/mods/creative$jj-*.jar "$OUT/held/" || continue
  bash tools/creative_labels_test/scratch_server.sh boot "$OUT" >/dev/null 2>&1 &
  sleep 2; r=$(bash $Q/wait_boot.sh "$OUT" 400); echo "boot: $r"
  case "$r" in DONE*) python3 $Q/qrun.py run "$OUT" $B/jx_$jj.qb perf/runs/r1106spf/jx_$jj 2>&1 | tail -1;;
    *) mkdir -p perf/runs/r1106spf/jx_$jj; cp "$OUT"/srv/crash-reports/* perf/runs/r1106spf/jx_$jj/ 2>/dev/null; rm -f "$OUT"/srv/crash-reports/*;;
  esac
  gzip -c "$OUT/srv/logs/latest.log" > perf/runs/r1106spf/jx_$jj/latest.log.gz 2>/dev/null
  bash tools/creative_labels_test/scratch_server.sh stop "$OUT" >/dev/null 2>&1
  for p in $(pgrep -x java); do [ "$(readlink /proc/$p/cwd)" = "$OUT/srv" ] && { sleep 5; kill $p 2>/dev/null; sleep 5; kill -9 $p 2>/dev/null; }; done
  until [ -z "$(bash $Q/game_jvms.sh)" ]; do sleep 2; done
  mv "$OUT"/held/creative$jj-*.jar "$OUT/srv/mods/"
done; done
echo JX-DONE
