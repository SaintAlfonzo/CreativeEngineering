#!/usr/bin/env bash
# Pack performance evaluation on a scratch dedicated server (feature_writeup/perf.md section 10).
#   eval_server.sh setup OUT LEVEL [events-off]   scratch_server.sh setup, then: normal world with structures and a fixed
#                                                 seed, jars COPIED (not symlinked), JFR recording with ce-server.jfc
#                                                 (events-off: every creativecommon.* event disabled), the probes
#   eval_server.sh boot OUT                       refuses to start while another game JVM is up
#   eval_server.sh wait OUT PATTERN [SECS]        waits until logs/latest.log has PATTERN (or a crash), prints the line
#   eval_server.sh cmd|stop OUT ...               as scratch_server.sh
# Nothing here writes into the instance.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../../.." && pwd)"
SS="$INST/tools/creative_labels_test/scratch_server.sh"
SEED=-8188900049893920259
what=${1:?usage: eval_server.sh setup|boot|wait|cmd|stop OUT ...}
OUT=${2:?OUT dir}
SRV="$OUT/srv"
case "$what" in
setup)
  LEVEL=${3:?level name}
  bash "$SS" setup "$OUT" "$LEVEL"
  cd "$SRV" || exit 1
  # hard links (copies across filesystems), so a parallel session's installToMods can't break a running boot:
  # installToMods moves the old file away, the link keeps its content, and no disk space is used
  for j in mods/*.jar; do t=$(readlink -f "$j"); ln -f "$t" "$j" 2>/dev/null || cp --remove-destination "$t" "$j"; done
  sed -i -e 's/^level-type=.*/level-type=minecraft\\:normal/' -e 's/^generate-structures=.*/generate-structures=true/' \
    -e 's/^difficulty=.*/difficulty=normal/' -e 's/^spawn-monsters=.*/spawn-monsters=true/' \
    -e 's/^spawn-animals=.*/spawn-animals=true/' -e 's/^view-distance=.*/view-distance=10/' \
    -e 's/^simulation-distance=.*/simulation-distance=8/' server.properties
  echo "level-seed=$SEED" >> server.properties
  mkdir -p perf/recordings perf/exit
  cp "$INST/server/perf/ce-server.jfc" perf/
  if [ "${4:-}" = events-off ]; then
    python3 - perf/ce-server.jfc <<'EOF'
import re, sys
p = sys.argv[1]
s = open(p).read()
s = re.sub(r'(<event name="creativeengineering\.[^"]+">\s*<setting name="enabled">)true', r'\1false', s)
open(p, "w").write(s)
print("events off:", s.count('creativecommon.'), "creativeengineering names in jfc")
EOF
  fi
  cat > user_jvm_args.txt <<EOF
-Xmx6G
-XX:FlightRecorderOptions=stackdepth=256,repository=perf/jfr-repo
-XX:StartFlightRecording=name=ce,settings=perf/ce-server.jfc,disk=true,maxage=60m,maxsize=${JFR_MAX:-1g},dumponexit=true,filename=perf/exit
EOF
  for p in scratch_query/zq_probe.js food_drinks_probe/food_drinks_probe.js creative_delight_test/cdj_probe.js \
           fungi_probe/fungi_probe.js kitchen_probe/cfb_kitchen_probe.js vintage_probe/vintage_probe.js \
           seasoning_probe/seasoning_probe.js item_merges_probe/merge_probe.js \
           worldgen_perf_probe/wgp_probe.js; do
    cp "$INST/tools/$p" "kubejs/server_scripts/zz_$(basename "$p")"
  done
  echo "eval srv ready: $(ls mods | wc -l) jars copied, level $LEVEL, seed $SEED"
  ;;
boot)
  live=$(bash "$INST/tools/scratch_query/game_jvms.sh" 2>/dev/null || true)
  if [ -n "$live" ]; then echo "game JVM already up: $live"; exit 1; fi
  bash "$SS" boot "$OUT"
  ;;
wait)
  PAT=${3:?pattern}
  SECS=${4:-900}
  for _ in $(seq "$SECS"); do
    if [ -f "$SRV/logs/latest.log" ]; then
      l=$(grep -m1 -F -- "$PAT" "$SRV/logs/latest.log" || true)
      [ -n "$l" ] && { echo "$l"; exit 0; }
      grep -q "Crash report saved\|rolling back to VANILLA" "$SRV/logs/latest.log" && { echo CRASH; exit 2; }
    fi
    # kill -9 / OOM kill leave no crash report: no java with its cwd in srv/ = dead
    if [ $((SECONDS % 5)) = 0 ]; then
      alive=0; for p in $(pgrep -x java); do case "$(readlink /proc/$p/cwd)" in "$SRV"*) alive=1;; esac; done
      [ $alive = 0 ] && [ "$SECONDS" -gt 20 ] && { echo DEAD; exit 3; }
    fi
    sleep 1
  done
  echo TIMEOUT
  exit 1
  ;;
cmd|stop)
  bash "$SS" "$@"
  ;;
esac
