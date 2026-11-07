#!/bin/bash
# r1106cbp: every port_chain boot of gen.py's BOOTS (or only those whose name matches $1, a regex), one after another.
# OUTBASE must be set (under the scratchpad). Results perf/runs/r1106cbp/<name>/.
cd "$(dirname "$0")/../../../.." || exit 1
INST=$PWD; Q=tools/scratch_query; B=$Q/batches/r1106cbp
python3 $B/gen.py plan | awk -F'\t' -v m="${1:-.}" '$1 ~ m' | while IFS=$'\t' read -r name mode batch env; do
  echo "== $name $batch $(date +%H:%M:%S)"
  env INST="$INST" RUN=r1106cbp bash -c "$env bash $Q/cbo_run.sh '$name' '$mode' '$B/$batch.qb'" < /dev/null
  tail -1 perf/runs/r1106cbp/$name/qrun.txt 2>/dev/null
done
echo ALL-BOOTS-DONE
