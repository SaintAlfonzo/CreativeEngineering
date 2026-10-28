#!/usr/bin/env bash
# Perf session: records every scratch server of a bulk queue run, start to end (feature_writeup/perf.md 14).
#   perf_session.sh start [NAME]     open perf/sessions/NAME/ (default run_<date>) and start the collector
#   perf_session.sh mark TEXT        start a new segment; use the test ID ("SQ-VP-02", "bench:PB-04 reload")
#   perf_session.sh status           open session, collector pid, boots seen, disk free
#   perf_session.sh stop             stop the collector (after the last server exited) and close the session
#   perf_session.sh process [NAME]   per-boot HTML reports from the boots' recordings (perf_report.py)
#   perf_session.sh report [NAME] [--baseline OTHER] [--prune]
#                                    process, then summary.md / summary.json: boots, segments, worst ticks
# scratch_server.sh writes boot / cmd / stop lines to the timeline by itself; marks name the segments.
# Output stays in perf/sessions/ (untracked), not in the scratchpad.
set -uo pipefail
INST="$(cd "$(dirname "$0")/../../.." && pwd)"
HERE="$INST/tools/perf/session"
SESS="$INST/perf/sessions"
what=${1:?usage: perf_session.sh start|mark|status|stop|process|report ...}
cur() { [ -f "$SESS/CURRENT" ] && cat "$SESS/CURRENT"; }
tl() { local n; n=$(cur) || { echo "no open session"; return 1; }
  printf '%s\t%s\t-\t%s\n' "$(date +%s.%N | cut -c1-14)" "$1" "$2" >> "$SESS/$n/timeline.tsv"; }
case "$what" in
start)
  n=$(cur) && { echo "session $n is already open"; exit 1; }
  NAME=${2:-run_$(date +%m%d_%H%M)}
  D="$SESS/$NAME"
  mkdir -p "$D/boots"
  echo "$NAME" > "$SESS/CURRENT"
  tl session "start $(df -h --output=avail / | tail -1 | tr -d ' ') free"
  nohup setsid bash "$HERE/collect.sh" "$D" > "$D/collector.log" 2>&1 < /dev/null &
  echo $! > "$D/collector.pid"
  echo "session $NAME open, collector pid $(cat "$D/collector.pid")"
  ;;
mark)
  tl mark "${2:?text}" && echo "marked: $2"
  ;;
status)
  n=$(cur) || { echo "no open session"; exit 0; }
  D="$SESS/$n"
  p=$(cat "$D/collector.pid" 2>/dev/null)
  kill -0 "$p" 2>/dev/null && c="running ($p)" || c="NOT running"
  echo "session $n, collector $c, boots: $(ls "$D/boots" | wc -l), free: $(df -h --output=avail / | tail -1 | tr -d ' ')"
  ls "$D/boots"
  tail -3 "$D/timeline.tsv"
  ;;
stop)
  n=$(cur) || { echo "no open session"; exit 1; }
  D="$SESS/$n"
  tl session stop
  touch "$D/stop.req"
  # the collector finishes the boots it is still watching, then exits
  for _ in $(seq 120); do kill -0 "$(cat "$D/collector.pid")" 2>/dev/null || break; sleep 1; done
  kill -0 "$(cat "$D/collector.pid")" 2>/dev/null && echo "collector still waiting for a server to exit (see collector.log)"
  rm -f "$SESS/CURRENT"
  echo "session $n closed; next: perf_session.sh report $n"
  ;;
process|report)
  shift
  if [ $# -gt 0 ] && [ "${1#--}" = "$1" ]; then NAME=$1; shift
  else NAME=$(cur || ls -t "$SESS" | grep -vx CURRENT | head -1); fi
  python3 "$HERE/session_report.py" "$what" "$SESS/$NAME" "$@"
  ;;
*)
  echo "unknown: $what"; exit 1
  ;;
esac
