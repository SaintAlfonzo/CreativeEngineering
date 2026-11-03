# Scratch runner claim

One session runs scratch servers or clients at a time (skill `scratch-test`, part B1). Claim this file before
preflight, keep `Phase` / `ETA` / `Updated` current at every phase boundary, set `State: idle` when done. Other
sessions read this file for status instead of messaging every session.

Format while running:

```
State: running
Runner: <session / task that holds the claim>
Run: <run id>, <run doc link>
Phase: step 5 (s5 B, B2), then drive_r.sh and drive_s5r.sh (chained)
ETA: 2026-11-03 about 02:45
JVM pids: scratch servers under scratchpad r1102b/ only (cwd check via game_jvms.sh)
Updated: 2026-11-02 22:05
```

When done: `State: idle` and one `Last run:` line.

---

State: stopped
Runner: none (r1102b stopped by the owner 2026-11-02 22:11; no JVM up)
Run: r1102b, [scratch_run_2026-11-02b.md](scratch_run_2026-11-02b.md); handover [perf/runs/r1102b/HANDOVER.md](../../perf/runs/r1102b/HANDOVER.md)
Updated: 2026-11-02 22:12

Last run: 2026-11-02 r1102b (every unrun server row: s1, s2, s3, s5 run; worldgen postponed; reruns not run), [scratch_run_2026-11-02b.md](scratch_run_2026-11-02b.md) section 8.
