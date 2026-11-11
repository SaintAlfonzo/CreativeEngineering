# Scratch run 2026-11-09

## 1. r1109wo: SQ-WO Wise Oak server rows

- **Owner request:** execute the Wise Oak scratch testing.
- **Scope:** SQ-WO-01 to -37 (server). CQ-WO-01 to -07 are client look rows and are not part of this run unless noted.
- **Batches:** `tools/scratch_query/batches/r1109wo/` (one per boot), runner `tools/scratch_query/wo_run.sh`, judge `tools/scratch_query/wo_judge.py`.
- **Results:** `perf/runs/r1109wo/`.
- **Jars:** Creative Worldgen 11-09 20:25 from `mods/` (same as build/libs), Mystical Oak Tree 1.10.
- **Perf:** short targeted run, no perf session opened. JFR stays on for every boot (scratch_server.sh default).
- **Boots:** A1, A1b (restart for SQ-WO-10), A2, A3, A4, A5, S on one group A world; B on a fresh normal world.

### Batch changes from the queue text (scratch-only, no gameplay change)

- `gamerule randomTickSpeed 300` with `sleep 30` replaces `sleep 120` in SQ-WO-04 to -07: at speed 3 a tree misses every random tick in 120 s about 17 percent of the time.
- `randomTickSpeed 0` elsewhere: a timerless `[state=sleeping]` tree placed by day wakes on any random tick.
- Timer rows at pace 0.2: at 2 s per command the 5 s timers expire before they are read.
- SQ-WO-14: a short random-tick window, then an immediate read. A 5 s tired sleep inside a 90 s wait is usually over.
- SQ-WO-18 and the angry case of -28: the tree is made angry by waking a sleeper, so it has a real timer. A bare `[state=angry]` has none and sleeps within 23 ticks.
- SQ-WO-28: one Z per case, so no blow or cooldown carries over.
- SQ-WO-36: on a grass platform at Y 200 at the taiga spot, since 1.19.2 has no `execute positioned over`.
- SQ-WO-37: one talk at trust 100 added, so a server-side lookup would be reached.

### Run notes

- 21:55 RUNNER.md claimed. No game JVM up, 50 GB free. r1108hh stays paused.
- 21:51 to 21:58 boots A1 and A1b ran (server up 4.2 s to Done, no crash). Batches `b1_defaults.qb`, `b1b_restart.qb`; results `perf/runs/r1109wo/` (`results.md`, `rows/`).
- 21:58 the runner script finished, but the background task never reported (a leftover server stdin feeder kept its output open). Nothing ran from 21:58 to 22:52, when the owner asked for status. Feeder killed, no game JVM up. Run STOPPED here, not by a failure.

### Verdicts so far (A1, default config)

| ID | Result | Evidence |
|---|---|---|
| SQ-WO-01 | PASS | web block line once, config defaults, `/zwo` answers (every verb live-called by the later rows) |
| SQ-WO-02 | PASS | trust 0 / 80 / 50, H 0; NBT holds one record `{trust: 50, id: ...}` for G only |
| SQ-WO-03 | FAIL (BATCH-FAULT suspected) | `Nobody123` was accepted (`Set trust of Nobody123 ... to 50`): offline-mode scratch server resolves any name to a profile. All other errors passed (101, -1, not loaded, `No Wise Oak` twice). Fix: drop that command or use a selector that matches nobody |
| SQ-WO-04 | FAIL | tree `state=sleeping` at noon, clear weather, at randomTickSpeed 300. Suspect the stock tired roll (`isTiredOfYou`: `dialoguesUntilSlept` clamped 1..8, roll `nextInt(n)==0`, no talks needed) firing under the raised tick speed. Not confirmed; rerun at speed 3 and a longer wait, or read `isTiredOfYou` (stock, javap showed `getfield dialoguesUntilSlept`) before calling it a bug |
| SQ-WO-05 | PASS | sleeping at night, awake by day, no `sleep_until` in the NBT |
| SQ-WO-06 | FAIL | stayed `sleeping` after `weather clear` at noon. Same suspicion as -04 (tired sleep by day gives a timer of 300 s default, so the wake needs 6000 ticks); check `sleep_until` before judging |
| SQ-WO-07 | PASS | still sleeping after the storm ends at night; awake by day |
| SQ-WO-08 | FAIL | `pbot use` on a placed `[state=sleeping]` tree returned `use CONSUME`, tree `state=none` facing west (turned to the bot), no `angry_until`. So the tree was already awake when clicked (something woke it between setblock and use, or `setblock` of a sleeping state wakes it) |
| SQ-WO-09 | FAIL | depends on -08 (tree never angry) |
| SQ-WO-10 / 10r | PASS | `sleep_until` 32125 before and after the restart; the trust of 40 check passed |
| SQ-WO-11 | PASS | lightning on an oak sapling grew a Wise Oak |
| SQ-WO-12 | PASS | sapling restored when boxed in |
| SQ-WO-13 | PASS | birch sapling ignored |

PASS 9 (counting -10r with -10), FAIL 5, LOOK 0 open. Not run: SQ-WO-14 to -37, CQ-WO-01 to -07.

### Resume here

1. Debug the wake-a-sleeper path first (SQ-WO-08): -16, -17, -22 and the angry cases of -18 and -28 depend on it. In `WiseOakTileMixin.onInteract` the wake inject is `shift = AFTER` the stock `wakeUp` call, so it only fires when the stock click path calls `wakeUp`. Check whether a `[state=sleeping]` tree set with `setblock` is treated as `canSleep()` false or the stock click on a sleeper does not call `wakeUp` when the tree was not put to sleep by `goToSleep`. Probe: set the tree sleeping with `setblock`, run `zwo state` at once, then `pbot use`, then `zwo state`, with `randomTickSpeed 0` and the A1 config. Log lines in `perf/runs/r1109wo/rows/SQ-WO-08.log`.
2. Settle -04 and -06: read `creativeworldgen_sleep_until` of the sleeping tree and whether a tired sleep (timer present) or a night sleep (none) happened; rerun at `randomTickSpeed 3` with a 120 s wait if it must be exact.
3. Fix -03 in `b1_defaults.qb` (drop the `Nobody123` line, count `That player does not exist` as 0 on this server or remove the expect).
4. Server setup is reproducible: `bash tools/scratch_query/prep_srv.sh OUT wo A`, `cp tools/scratch_query/wo_probe.js OUT/srv/kubejs/server_scripts/zz_wo_probe.js` (the old scratch server is `.../scratchpad/woa`, may be wiped). Then `bash tools/scratch_query/wo_run.sh OUT perf/runs/r1109wo STEP...` with STEP in `a1 a1b a2 a3 a4 a5 s` (same OUT) and `b` (fresh normal world OUT). It sets each step's config, boots, runs `batches/r1109wo/*.qb`, stops.
5. Boots A2, A3, A4, A5, S, B and the batches for them are written but never run: `b2_short_timers.qb` (SQ-WO-14 to -30), `b3_chance0.qb` (-31), `b4_lists.qb` (-32), `b5_empty.qb` (-33, -34), `b6_strace.qb` (-37), `b7_worldgen.qb` (-35, -36). `wo_judge.py` has `toml` and `gifts` judges; the `region` judge only prints the census and is unfinished (SQ-WO-35 is judged by hand from `region_blocks.py`).
6. Do not run the CQ rows; they are the owner's.
7. Background tasks: run `wo_run.sh` with output to a file and poll the file or use Monitor; a scratch server's stdin feeder (`srv/feeder.pid`) holds a background task open until killed.

### Batch caveats (written, not yet exercised)

- `b7_worldgen.qb` uses `execute positioned {A1} ...` with `capture` variables of the `at [x, y, z]` locate line; 1.19.2 `/locate biome` prints `at [x, ~, z]`, so `{A2}` is `~`. Check the capture and the `execute positioned` lines with a first live call. `forceload add {A1-32}` arithmetic is supported by `qrun.py`'s `{NAME-N}`.
- `b6_strace.qb`/`wo_run.sh s` use `strace --seccomp-bpf` (strace 5.16 present); the control count is a rough grep.
- The A2 rows were written around timers of 5 s; the queue text assumed pace 2, so they use pace 0.2 or 0.5.
