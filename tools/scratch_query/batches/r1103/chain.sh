#!/bin/bash
# r1103: after drive_sB.sh ends, run drive_sA.sh then drive_p2.sh (one JVM at a time; each driver waits for game_jvms.sh empty).
INST=/home/liam/.local/share/atlauncher/instances/CreativeEngineering2
cd "$INST" || exit 1
until grep -qE 'STEP (ALL|ABORT)' perf/runs/r1103/sB/drive.log; do sleep 20; done
echo "CHAIN sB done $(date +%H:%M:%S)"
mkdir -p perf/runs/r1103/sA perf/runs/r1103/p2
bash tools/scratch_query/batches/r1103/drive_sA.sh > perf/runs/r1103/sA/drive.log 2>&1
echo "CHAIN sA done $(date +%H:%M:%S)"
bash tools/scratch_query/batches/r1103/drive_p2.sh > perf/runs/r1103/p2/drive.log 2>&1
echo "CHAIN p2 done $(date +%H:%M:%S)"
