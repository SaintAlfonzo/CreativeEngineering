# Structure testing pipeline: plan (2026-10-14)

The plan for this run, as I understood the instructions. The reference for what is being built and tested stays
[structure_tuning.md](../../structure_tuning.md) (7.4 for the build, 8.0 for the run order, section 8 for the
test lines). This folder is the running log of the run.

## 1. What runs, in order

1. **Build plan 7.4** (new structure mods, `vehicles` group, fortified-town shares), 7.4.0 to 7.4.8 in sequence.
   **Offline checks only**: gradle build, `tools/test_worldgen_tools.sh`, `tools/test_structure_groups.sh`, node sim,
   MetaTest/FitTest, the template scans. **No scratch server in this step.**
2. **Scratch server, new world**: the 7.4 tests, ST-170 to ST-190 (the "New structure mods before Phase 3" block).
   This is the first scratch-server work of the run.
3. **Phase 3** (8.0.3, placing and probing) on the same world: ST-14, ST-47, ST-92, ST-130, ST-28, ST-30, ST-32,
   ST-31, ST-12, ST-149 (block part), ST-133 (geodes), ST-48, ST-50.
4. **Report after Phase 3, then continue** into Phases 4 to 10 (config switches, batch searches, pregenerated
   area, calibration, two-world comparisons, flat world, profiling), one phase at a time, **as long as enough tokens
   remain** either to finish the next phase or to get significant progress through it (about 35% of its tests).
   Before starting each phase I check that; if there isn't enough left, I stop at the phase boundary and report. If a
   phase stops partway, its scratchpad state block says exactly where, so the next session resumes there. The
   client list (8.0.11) is yours.

## 2. Milestones and files in this folder

| # | Milestone | Ends when | Scratchpad (live notes) | Writeup (after) |
|---|---|---|---|---|
| M1 | 7.4 built, offline checks pass | every 7.4.x step done, all offline checks green | `01_build_7_4.scratch.md` | `01_build_7_4.md` |
| M2 | 7.4 tests on the scratch server | ST-170..190 each PASSED, FAILED or PARTLY with a reason | `02_new_mods_tests.scratch.md` | `02_new_mods_tests.md` |
| M3 | Phase 3 | every Phase 3 test has a result | `03_phase_3.scratch.md` | `03_phase_3.md` |
| M4+ | Phases 4..10 (while tokens allow, see 1.4) | per phase | `04_phase_4.scratch.md`, ... | `04_phase_4.md`, ... |

**Scratchpad** (human readable, not pretty), created when the milestone starts and updated as I go, whenever it
doesn't slow the work down. It must be enough to write the milestone writeup if the session dies halfway. Each one
holds:
- **State block at the top**, kept current: milestone, current step, what's done, what's next; server dir, world
  name, seed, jar file + build time; every toml/config value changed from default and every soft rule loosened
  (7.4.8), with its original value, so it can be put back; anything still running (server pid, a long `/locate`).
- **Log, append-only, timestamped**: commands run, what came back, numbers (counts, coordinates, biomes, timings),
  files changed, dead ends, surprises.
- **Results table**: test ID, result word, one-line evidence.
- **Open issues**: bugs found, decisions needed, leads for later phases.

**Milestone writeup** (standard, clean) once the milestone ends: what was done, results per test, bugs found and
what happened to them, values changed and restored, follow-ups. The result lines also go inline into
structure_tuning.md section 8 and the summary under the phase, as in Phases 1 and 2, so that file stays the single
reference.

## 3. Rules I'll follow

- **M1 is offline only.** Nothing from 7.4 goes on a scratch server before M2.
- **Correctness first** (7.4 request): no structure in a wrong biome, inside a peaceful radius, or past earth/water
  fill. For rare structures I loosen only soft rules (frequency, weight, share, spacing/separation, debug density),
  never biome fits, biome rules, peaceful radius, placement overlaps or fill limits. Each loosened value goes in the
  scratchpad state block and the result line, and is put back after.
- **Decisions D2 to D7** in 7.4 use their written defaults (D1 is settled at 0.20 / 0.20 / 0.40). If the build turns
  up a reason to change one, I note it and ask instead of choosing.
- **Bugs**: a bug in the new 7.4 work gets fixed and the test rerun, both logged. A bug in an older system is
  recorded, the independent tests carry on, and it comes to you at the end of the milestone (as in Phase 2).
- **Scratch server safety** (from earlier runs): server built in this session's scratchpad per 8.0.0 (seed 8675309,
  `max-tick-time=-1`, `asyncLocatorThreads = 4`, the build/libs patcherMod jar, not the one in `mods/`); `forceload`
  before any KubeJS probe of new chunks (Rhino deadlock); kill the java pid after "All dimensions are saved" when an
  Async Locator search is still running; exact-match process kills only (`pkill -fx 'sleep infinity'`, never
  `pkill -f` on a string in my own command line); never list the game's processes with args (access token).
- **Nothing installed or deployed**: no jar into `mods/`, no snapshot/deploy, no commit, unless you ask. The one
  `mods/` change that is part of 7.4 itself: the Camps. Castles. Carriages. 1.1.1 jar moves to
  `disabledmods/archive/`.
- **Config edits** that are part of 7.4 follow the configs_edited backup rule and mirror to the server.
- No emoji in any file.

## 4. History

- 2026-10-14: plan written; folder created.
- 2026-10-14: Phases 4 to 10 continue after Phase 3 while tokens allow finishing a phase or about 35% of it (user).
- 2026-10-14 10:45: M2 done (writeup 02_new_mods_tests.md). Session 2 rebuilt the scratch server (the old
  scratchpad was gone) and used Chunky pregen + region-file analysis at density 4. Next: M3 (Phase 3).
- 2026-10-14 11:40: M3 (Phase 3) done, writeup 03_phase_3.md: 11 passed, ST-133 partly, ST-50 failed; 2 older-system
  bugs (ground match over deep structures, driftwood water versions) + geode-drop tuning for the user. Next: M4 (Phase 4).
- 2026-10-14 12:40: M4 (Phase 4) done, writeup 04_phase_4.md: all passed except ST-65 partly (trim half -> [game]),
  ST-76 skipped; no bugs. Next: M5 (Phase 5, batch searches).
- 2026-10-14 12:52: the structure work as of now (patcherMod 1.0.6, 12:44 rebuild of the 01:28 source, plus all worldgen
  data) ships in pack 1.0.6, marked INCOMPLETE (user). Fixes from Phase 5 onward go into 1.0.7 (patcherMod `mod_version`
  1.0.7); open bugs at staging are listed in [plan_1.0.6.md](../../plan_1.0.6.md) 2.1.
- 2026-10-14 16:10: M5 (Phase 5) done, writeup 05_phase_5.md: LO-02, 06 (adapted), 08, 15 passed, LO-01 partly, LO-05
  FAILED (non-blocking locate loads still block the server thread, LocateLoads.java:82; older code, fix for 1.0.7 is
  the user's call). Also: Cloud Storage / Domestication Innovation village pieces dropped on reload, village layout
  differs between JVM runs, /celocate stop row label. Session 4 redid the lost ground-truth step. Next: M6 (Phase 6).
- 2026-10-14 17:42: M6 (Phase 6) done, writeup 06_phase_6.md: ST-141, 154, 107, 20, 74, LO-12 passed; ST-142, ST-114
  scratch parts passed; LO-04 partly; rate tests skipped; LO-03 and LO-14 FAILED on a census bug (slots run set by set,
  the first set loads the chunk, later sets count as "known"). Older locate code, with the Phase 5 load bug: fix for
  1.0.7 is the user's call. Next: M7 (Phase 7, calibration runs).
