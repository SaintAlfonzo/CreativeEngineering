#!/bin/bash
# r1103: after chain.sh ends, rerun SQ-LBS-05a (sB_1) and SQ-LBS-00..05 (sB_2) with the fixed zlbs_probe.js.
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
cd "$INST" || exit 1
until grep -q 'CHAIN p2 done' perf/runs/r1103/chain.log; do sleep 30; done
mkdir -p perf/runs/r1103/sBR
LEVEL=sbr R=$INST/perf/runs/r1103/sBR ROWS2=SQ-LBS-00,SQ-LBS-01,SQ-LBS-02,SQ-LBS-03,SQ-LBS-04,SQ-LBS-05 \
  bash tools/scratch_query/batches/r1103/drive_sB.sh > perf/runs/r1103/sBR/drive.log 2>&1
echo "CHAIN2 sBR done $(date +%H:%M:%S)" >> perf/runs/r1103/chain.log
