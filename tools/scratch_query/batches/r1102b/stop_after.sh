#!/bin/bash
# stop_after.sh PID STEP LOG: kill the driver PID the moment LOG shows "STEP <STEP> end" (scope cut at a step boundary).
P=$1; S=$2; L=$3
until grep -q "STEP $S end" "$L"; do kill -0 "$P" 2>/dev/null || exit 0; sleep 1; done
kill "$P"; echo "STEP STOPPED by stop_after after $S $(date +%H:%M:%S)" >> "$L"
