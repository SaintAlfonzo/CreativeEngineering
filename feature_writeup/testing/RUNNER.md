# Scratch runner claim

One session runs scratch servers or clients at a time (skill `scratch-test`, part B1). Claim this file before
preflight, keep `Phase` / `ETA` / `Updated` current at every phase boundary, set `State: idle` when done. Other
sessions read this file for status instead of messaging every session.

Format while running:

```
State: running
Runner: <session / task that holds the claim>
Run: <run id>, <run doc link>
Phase: slice B (drive_sB.sh, 2 boots, level sb1), then r1102b reruns, then slice A
ETA: 2026-11-03 about 10:00
JVM pids: scratch servers under scratchpad r1102b/ only (cwd check via game_jvms.sh)
Updated: 2026-11-03 01:01
```

When done: `State: idle` and one `Last run:` line.

---

State: running
Runner: session 51e0db54 (owner request 2026-11-03: execute the scratch queue, plan approved)
Run: r1103, [scratch_run_2026-11-03.md](scratch_run_2026-11-03.md)
Phase: slice B (drive_sB.sh, 2 boots, level sb1), then r1102b reruns, then slice A
ETA: 2026-11-03 about 10:00
JVM pids: scratch servers under scratchpad 51e0db54.../r1103/ only (cwd check via game_jvms.sh)
Updated: 2026-11-03 01:01

Last run: 2026-11-02 r1102b (every unrun server row: s1, s2, s3, s5 run; worldgen postponed; reruns not run), [scratch_run_2026-11-02b.md](scratch_run_2026-11-02b.md) section 8.
