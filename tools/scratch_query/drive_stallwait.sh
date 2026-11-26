#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
# Stall-watch wait for a qrun.py drive (skill scratch-test B3 "Stall watch"): block until the drive's running step
# changes (or reaches UNTIL), the driver stops, or no step server log and no RES row log was written for STALL
# minutes (default 10). Prints the reason and exits. Usage (in the background):
#   bash tools/scratch_query/drive_stallwait.sh RUN [UNTIL] [STALL_MIN]
RUN=${1:?run id}
UNTIL=${2:-}
STALL=${3:-10}
cd "$(dirname "$0")/../.." || exit 1
D=perf/runs/$RUN
st() { python3 tools/scratch_query/qrun.py drive status "$RUN" 2>&1; }
cur=$(st | grep -E "^ *[0-9]+ running" | awk '{print $1}')
while true; do
	s=$(st)
	n=$(echo "$s" | grep -E "^ *[0-9]+ running" | awk '{print $1}')
	if echo "$s" | head -1 | grep -qE "driver not running|finished"; then
		echo "DRIVER STOPPED"; echo "$s" | head -1; exit 0
	fi
	if [ -n "$UNTIL" ]; then [ -n "$n" ] && [ "$n" -ge "$UNTIL" ] && { echo "reached step $n"; exit 0; }
	elif [ -n "$n" ] && [ "$n" != "$cur" ]; then echo "step $cur -> $n"; exit 0; fi
	if [ -n "$n" ]; then
		k=$(printf 's%02d' $((n - 1)))
		newest=$(find "$D/$k/srv/logs/latest.log" "$D/${k}_res/rows" -type f -mmin -"$STALL" 2>/dev/null | head -1)
		if [ -z "$newest" ] && [ -e "$D/$k/srv/logs/latest.log" ]; then
			echo "STALL: step $n, no log write for $STALL min"; tail -n 3 "$D/$k/srv/logs/latest.log" | cut -c1-200; exit 0
		fi
	fi
	sleep 60
done
