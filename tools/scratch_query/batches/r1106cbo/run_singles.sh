#!/bin/bash
# r1106cbo: every single-mod and pair boot (SQ-CBO-20 to -43, -50 to -53), one after another. OUTBASE must be set.
cd "$(dirname "$0")/../../../.." || exit 1
Q=tools/scratch_query; B=$Q/batches/r1106cbo
python3 $B/gen.py plan | awk -F'\t' '$1 ~ /^s[0-9]+$/' | while IFS=$'\t' read -r name mode batch env; do
  add=$(echo "$env" | sed -E 's/^ADD="(.*)"$/\1/')
  ADD="$add" bash $Q/cbo_run.sh "$name" chain "$B/$batch.qb" < /dev/null
  tail -1 perf/runs/r1106cbo/$name/qrun.txt 2>/dev/null
done
echo ALL-SINGLES-DONE
