#!/usr/bin/env bash
# Runs a file of console commands (one per line, # comments skipped) against a scratch server and prints the
# [zq]/probe lines they produced.   zq_batch.sh OUT COMMANDS_FILE [WAIT_SECONDS_PER_COMMAND]
set -euo pipefail
OUT=${1:?OUT}; F=${2:?commands file}; W=${3:-2}
L="$OUT/srv/logs/latest.log"
start=$(wc -l < "$L")
while IFS= read -r c; do
  [[ -z "$c" || "$c" == \#* ]] && continue
  echo "$c" > "$OUT/srv/in.fifo"; sleep "$W"
done < "$F"
sleep 3
tail -n +"$((start + 1))" "$L" | grep -E '\[zq\]' | sed -E 's/^.*\[zq\] /  /' | grep -v '^  done$'
