#!/bin/bash
# r1106spf SQ-SPF-92: r1106cbp's port-chain boots (gen.py plan) rerun on the 18:5x builds copied into CEPortTesting/mods.
# Results perf/runs/r1106spf/c_<name>/. chain_boots.sh [regex]; OUTBASE must be set (under the scratchpad).
cd "$(dirname "$0")/../../../.." || exit 1
INST=$PWD; Q=tools/scratch_query; B=$Q/batches/r1106cbp
python3 $B/gen.py plan | awk -F'\t' -v m="${1:-.}" '$1 ~ m' | while IFS=$'\t' read -r name mode batch env; do
  echo "== c_$name $batch $(date +%H:%M:%S)"
  env INST="$INST" RUN=r1106spf bash -c "$env bash $Q/cbo_run.sh 'c_$name' '$mode' '$B/$batch.qb'" < /dev/null
  tail -1 perf/runs/r1106spf/c_$name/qrun.txt 2>/dev/null
done
echo ALL-BOOTS-DONE
