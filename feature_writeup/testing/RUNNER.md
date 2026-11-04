# Scratch runner claim

One session runs scratch servers or clients at a time (skill `scratch-test`, part B1). Claim this file before
preflight, keep `Phase` / `ETA` / `Updated` current at every phase boundary, set `State: idle` when done. Other
sessions read this file for status instead of messaging every session.

Format while running:

```
State: idle
Runner: none (boot check 2026-11-04 done; no JVM up)
Updated: 2026-11-04 00:43

Last run: 2026-11-04 b1104 (boot check only: changed mods rebuilt, group A superflat boot reached Done in 10.4 s, no errors from our jars), [scratch_run_2026-11-04.md](scratch_run_2026-11-04.md). Before it: 2026-11-03 r1103w (worldgen slice, STOPPED by the owner 18:00 after W1c SQ-WGE-14; W1 judged PASS 73 FAIL 10), [scratch_run_2026-11-03.md](scratch_run_2026-11-03.md) section 9. Before it: 2026-11-03 r1103 (every never-run server row + r1102b reruns; 204 judged: PASS 171, FAIL 19), [scratch_run_2026-11-03.md](scratch_run_2026-11-03.md), FAILs in perf/runs/r1103/FINAL.md.
