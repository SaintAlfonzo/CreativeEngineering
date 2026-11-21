#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
# Block until a qrun.py drive leaves its current running step or the driver stops; print the change and exit.
# Usage: bash tools/scratch_query/drive_wait.sh RUN [UNTIL]   (in the background; one notice per step, or once the
# running step number reaches UNTIL)
RUN=${1:?run id}
UNTIL=${2:-}
cd "$(dirname "$0")/../.." || exit 1
st() { python3 tools/scratch_query/qrun.py drive status "$RUN" 2>&1; }
cur=$(st | grep -E "^ *[0-9]+ running" | awk '{print $1}')
while true; do
	s=$(st)
	n=$(echo "$s" | grep -E "^ *[0-9]+ running" | awk '{print $1}')
	if echo "$s" | head -1 | grep -qE "driver not running|finished"; then
		echo "DRIVER STOPPED"; echo "$s" | head -1; echo "$s" | grep -vE " (done|pending) " | tail -n 5; exit 0
	fi
	if [ -n "$UNTIL" ]; then [ -n "$n" ] && [ "$n" -ge "$UNTIL" ] && { echo "reached step $n"; exit 0; }
	elif [ "$n" != "$cur" ]; then echo "step $cur -> $n"; echo "$s" | grep -E "^ *($cur|$n) "; exit 0; fi
	sleep 60
done
