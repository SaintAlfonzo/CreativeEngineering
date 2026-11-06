#!/bin/bash
# Stop a scratch server and wait until no game JVM runs from OUT/srv. If the JVM whose cwd is OUT/srv is still up
# after MAXSECS (a crashed server can hang after "Shutting down map thread"), kill it: only that pid, never others.
#   stop_wait.sh OUT [MAXSECS=180]
OUT=${1:?OUT}; MAX=${2:-180}; D=$(dirname "$0")
bash "$D/../creative_labels_test/scratch_server.sh" stop "$OUT" >/dev/null 2>&1
SRV=$(readlink -f "$OUT/srv"); t=0
while [ $t -lt $MAX ]; do
  mine=$(bash "$D/game_jvms.sh" | awk -v s="$SRV" '$2==s{print $1}')
  [ -z "$mine" ] && { echo "stopped"; bash "$D/game_jvms.sh"; exit 0; }
  sleep 3; t=$((t+3))
done
for p in $mine; do echo "hung, killing own pid $p"; kill $p; sleep 5; kill -0 $p 2>/dev/null && kill -9 $p; done
bash "$D/game_jvms.sh"
