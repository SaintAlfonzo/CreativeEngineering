#!/bin/bash
# Wait for a scratch server boot to finish: prints "DONE <secs>" or "CRASH <file>" or "GONE" (no game JVM) or "TIMEOUT",
# or "MISSING" when FML refuses a missing mandatory dependency (r1115: the JVM can sit idle after that).
# "FAILED" on "Failed to start the minecraft server" (mod loading error), "DATAPACK" when the server stops at "Failed to load datapacks" (r1115; earlier it waited out MAXSECS).
#   wait_boot.sh OUT [MAXSECS=900]
OUT=${1:?OUT}; MAX=${2:-900}; L="$OUT/srv/logs/latest.log"; t=0
while [ $t -lt $MAX ]; do
  if grep -q 'Done (' "$L" 2>/dev/null; then echo "DONE $(grep -m1 -o 'Done ([0-9.]*s)' "$L")"; exit 0; fi
  if grep -q "Failed to start the minecraft server" "$L" 2>/dev/null; then echo "FAILED (Failed to start the minecraft server: the JVM can sit idle; stop it)"; grep -m1 -E "encountered an error|Caused by" "$L" | cut -c1-300; exit 5; fi
  if grep -q "Failed to load datapacks, can't proceed" "$L" 2>/dev/null; then echo "DATAPACK (Failed to load datapacks: the JVM sits idle; stop it)"; grep -m1 -E "Unknown registry key|JsonParseException" "$L" | cut -c1-300; exit 4; fi
  if grep -q 'Missing or unsupported mandatory dependencies' "$L" 2>/dev/null; then echo "MISSING (FML refused: missing mandatory dependency)"; exit 3; fi
  c=$(ls "$OUT"/srv/crash-reports/*.txt 2>/dev/null | head -1); [ -n "$c" ] && { echo "CRASH $c"; exit 1; }
  if [ $t -gt 30 ] && [ -z "$(bash "$(dirname "$0")/game_jvms.sh")" ]; then echo GONE; tail -5 "$L" | cut -c1-300; exit 1; fi
  sleep 3; t=$((t+3))
done; echo TIMEOUT; exit 2
