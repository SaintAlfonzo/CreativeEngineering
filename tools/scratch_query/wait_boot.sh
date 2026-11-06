#!/bin/bash
# Wait for a scratch server boot to finish: prints "DONE <secs>" or "CRASH <file>" or "GONE" (no game JVM) or "TIMEOUT".
#   wait_boot.sh OUT [MAXSECS=900]
OUT=${1:?OUT}; MAX=${2:-900}; L="$OUT/srv/logs/latest.log"; t=0
while [ $t -lt $MAX ]; do
  if grep -q 'Done (' "$L" 2>/dev/null; then echo "DONE $(grep -m1 -o 'Done ([0-9.]*s)' "$L")"; exit 0; fi
  c=$(ls "$OUT"/srv/crash-reports/*.txt 2>/dev/null | head -1); [ -n "$c" ] && { echo "CRASH $c"; exit 1; }
  if [ $t -gt 30 ] && [ -z "$(bash "$(dirname "$0")/game_jvms.sh")" ]; then echo GONE; tail -5 "$L" | cut -c1-300; exit 1; fi
  sleep 3; t=$((t+3))
done; echo TIMEOUT; exit 2
