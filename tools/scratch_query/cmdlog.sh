#!/usr/bin/env bash
# Hand steps on a running scratch server: sends each command (scratch_server.sh cmd), waits, prints only the log lines
# they produced (logger prefix and probe file names cut, worldgen "far chunk" noise and "[x] done" dropped).
#   cmdlog.sh OUT [-w SECS_AFTER_EACH=0.6] [-e SECS_AT_END=1.5] "cmd 1" "cmd 2" ...
# A command "sleep N" waits N seconds instead of being sent.
set -u
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; shift
W=0.6; E=1.5
while [ $# -gt 0 ]; do case "$1" in -w) W=$2; shift 2;; -e) E=$2; shift 2;; *) break;; esac; done
LOG="$OUT/srv/logs/latest.log"
L=$(wc -l < "$LOG")
for c in "$@"; do
  if [[ "$c" =~ ^sleep\ ([0-9.]+)$ ]]; then sleep "${BASH_REMATCH[1]}"; continue; fi
  bash "$INST/tools/creative_labels_test/scratch_server.sh" cmd "$OUT" "$c" >/dev/null; sleep "$W"
done
sleep "$E"
tail -n +$((L + 1)) "$LOG" | sed -E 's/^\[[^]]*\] \[[^]]*\] \[[^]]*\]: //; s/^zz_[a-z_]+\.js#[0-9]+: //' \
  | grep -vE "Detected setBlock in a far chunk|^\[z[a-z]+\] done$|Can't keep up" | cut -c1-400
