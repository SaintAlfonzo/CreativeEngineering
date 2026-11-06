# Scratch run 2026-11-02 evening (r1102b): every unrun server row

Bulk run. Owner request 2026-11-02 evening: "execute the scratch test queue", scope chosen: every unrun server row.

## 0. Performance capture

Perf session `r1102b` (`tools/perf/session/perf_session.sh start r1102b`) before the first boot, JFR on, report without
`--prune`.

## 1. Scope

Taken: every server row (SQ-) of [scratch_queue.md](scratch_queue.md) that no run doc, batch, judged file, Run log
entry or "Results" line mentions: 425 server rows in 75 sections, listed by
`python3 tools/scratch_query/unrun_rows.py --ids` (new tool, heuristic) and frozen in
`tools/scratch_query/batches/r1102b/scope.txt`. Rows whose premise no longer holds (pre-nesting standalone jars,
superseded) are skipped with a reason in the `*_skip.md` files of the batch dir.

Postponed by the user 2026-11-02 (19:05, relayed by session creativeengineering2-7e): the worldgen slice `w_*`
(w_1 to w_11, 54 rows: first-tick water, census ties, SQ-WGE, SQ-EFB, SQ-FGDC, SQ-PSW). Not failed, not run in r1102b;
the batches and w_setup.md stay ready for a later run.

Not taken: every client row (CQ-, about 95): the owner pin holds creative-split client rows until wave 8 (wave 8 not
done on 2026-11-02). r1031 deferred long operations L1 to L4 (pregen, perf suite, rpack bench, DH flight).

## 2. Tools

`prep_srv.sh`, `scratch_server.sh`, `qrun.py`, `unrun_rows.py` (new). Batches: `tools/scratch_query/batches/r1102b/`,
written by five batch-writer agents by scope slice: `s1_*` (scope lines 1-20, 10-23 to 10-25 food rows), `s2_*`
(21-33: Rustic, Delightful, Kettle, investigations, cats), `s3_*` (34-66: 10-25 to 11-01 sections), `w_*` (worldgen:
first-tick water, census ties, SQ-WGE, SQ-EFB, SQ-FGDC, SQ-PSW), `s5_*` (11-02 food and the pre-scratch pass). Each
slice has `<slice>_setup.md` and `<slice>_skip.md`. Results: `perf/runs/r1102b/<boot>/`. OUT: scratchpad `r1102b/`.

## 3. Jars

All checked 14:33 against build/libs: `mods/` holds the newest builds.

| Jar | mods/ | build/libs | Rows |
|---|---|---|---|
| creativeengineering-1.1.07.jar (patcherMod) | 11-02 14:08 | 11-02 14:08 | all |
| creativedelight-1.0.0.jar | 11-02 14:01 | 11-02 14:01 | food, CD members |
| creativebrewing-1.0.0.jar | 11-02 13:52 | 11-02 13:52 | brewing, drinks |
| creativecalendar-1.0.0.jar | 11-02 14:08 | 11-02 14:08 | SQ-PST, seasons |
| creativecompat-1.0.0.jar | 11-02 13:52 | 11-02 13:52 | compat rows |
| creativedevtools-1.0.0.jar | 11-01 12:49 | 11-01 12:49 | probes |

## 4. Order

One boot per batch file (setup per boot in `<slice>_setup.md`). Results dir per slice: `perf/runs/r1102b/<slice>/`
(worldgen: one RES `w/` for all w_* files, later boots read vars.json).

| Step | Boots (batch files) | World | Estimate |
|---|---|---|---|
| 1 | s1_O, s1_A | A `s1a` (old world, then current jars) | 50 min |
| 2 | s1_B | B fresh | 30 min |
| 3 | s2_O0, s2_A, s2_B, s2_Coff, s2_N, s2_RDstop | A old world, then B worlds | 2 h |
| 4 | s3_A, s3_A2 | A, spawn-animals | 70 min |
| 5 | s5_A, s5_A2, s5_B, s5_B2 | A, then B | 3 h |
| 6 | POSTPONED by the user: w_5..w_8 (A), w_1_B (wge, seed 8675309), w_2, w_3, w_4, w_9, w_10, w_11 | A, fresh B worlds | 6.5 h |

Total about 14 h with boots.

## 5. Row map

| Slice | Scope lines | Batched rows | Skipped (see `<slice>_skip.md`) |
|---|---|---|---|
| s1 | 1-20 | 54 (O 1, A 50, B 3) | 16 (8 already recorded in range notation, 2 superseded, 5 client, 1 player) |
| s2 | 21-33 | 93 (O0 3, A 79, B 11, Coff 4, N 3, RDstop 1) | 16 (12 client, 2 stale, 1 superseded, 1 player) |
| s3 | 34-66 less 46, 48 | 116 (A 114, A2 2) | 23 (17 client, 3 need a pre-change save, 1 player, 2 already passed) |
| s5 | 69, 71-78, 80 | 93 incl. setup rows (A 69, A2 5, B 15, B2 4) | 10 client |
| w | 46, 48, 67, 68, 70, 79 | 54 queue rows (w_1 to w_11) | 3 client; SQ-WGE-00 offline in w_offline.md |

## 6. After the run

Run log entry in the queue, results into the feature writeups' test lists, README status, TODO rows for FAILs, gotchas.

## 7. Run notes

- 14:33 RUNNER.md claimed (its format block had been lost in the 11-02 edit; restored). game_jvms.sh empty, 24 GB
  free on /. Jars match. Scope 425 server rows (unrun_rows.py). Five batch-writer agents started.
- s1 in: s1_O (old-world build with pre-nest Vintage -03, Vinery -06, Brewin' -04), s1_A 50 rows, s1_B 3 rows;
  16 skipped. unrun_rows.py misses range notation ("-04, -05"): 8 of s1's IDs were already recorded (fix after the run).
- 14:57 Boot s1 O (prep A s1a, three pre-nest jars): BOOT CRASH in vanilla bootstrap, also without the old jars
  (15:01). Cause: Creative Calendar 14:08 `ModdedGrassSpreadSeasonMixin` (and `IWGrowthSeasonMixin`) hold
  `@Unique static final boolean = ModList.get().isLoaded("sereneseasons")`; Mixin merges it into Immersive Weathering's
  `ModGrassBlock.<clinit>`, which IW runs from `Blocks.<clinit>` before ModList exists: NPE, no boot. The `mods/` jar
  (14:08) cannot boot a dedicated server (the client most likely crashes the same way). FIX (no behaviour change, run
  default 1: boot blocker): both fields read `LoadingModList.get().getModFileById(...) != null`; creativeCalendar built
  15:04 into build/libs, copied into the scratch srv only. `mods/` still holds the 14:08 jar: owner installs.
- 15:05 Boot O with the fix: Done. First tick crashed: the old `backportedVinery-...-06` calls
  `creativeengineering.time.WineTime` (moved to Creative Calendar): ClassNotFoundException. Setup limit, not a pack bug
  (that jar no longer ships). The crashed JVM hung after "Shutting down map thread" and was still up when the next
  boot started (two JVMs for about 2 min, same srv dir): own pid killed, world deleted, clean reboot. New
  `tools/scratch_query/stop_wait.sh` (stop, wait, kill only the hung own pid) and `wait_boot.sh`.
- 15:14 Boot O again with Vintage -03 and Brewin' -04 only (barrel from the nested Vinery): s1_O LOOK (setup row ran,
  blocks placed). Stopped.
- 15:17 Boot A (same world, old jars out, probes in): s1_A started. SQ-EFB-00 / -17 gate: tools/test_worldgen_tools.sh
  does not compile (WorldgenFiles moved to util, duplicate local in EarthFillTest, node sim stub): tooling agent fixing it.
- 15:22 test_worldgen_tools.sh fixed (tools only: util.WorldgenFiles in the tests and the node sim stub,
  rootOverride by reflection, cache test settles once, FitTest counts 189 / 34 after the 11-02 Nether move): 12 of 12
  checks pass. SQ-EFB-00 / -17 gate open.
- 15:43 s1_A done: PASS 8, FAIL 19, LOOK 24 (judge agent on perf/runs/r1102b/s1/judged.md). Boot A logs in s1/srvlogs.
  Note: background Bash and Monitor notices here arrive late, not early; wait with a foreground `timeout tail -f`.
- 15:47 Boot s1_B (prep B s1b, seed 8675309; prep_srv picked the fixed creativecalendar from build/libs): Done 93 s.
  s1_B started.
- 16:00 s1 judged (perf/runs/r1102b/s1/judged.md): of 43 FAIL/LOOK rows 21 PASS, 3 FAIL (SQ-VP-06 stale
  pepper_jam / relish soft-fluid json in kubejs/data/vintagedelight; SQ-VTD6-01 wild ghost pepper on grass via
  #minecraft:nylium from Terralith; SQ-FDB-04 gleaming salad has no Nourishment rule), 17 BATCH-FAULT (regexes fixed
  in s1_A.qb), 2 OPEN (FDB-05, -06). Rerun later on s1a: CNS-02, FDB-09, VTD6-02, VP-04, VP-07, FDB-11, FDB-07, -06, -05.
- 16:27 s1_B done: FN-03 LOOK, VP-15 LOOK, LE-06 FAIL (locate timeout 900 s; to judge).
- 16:30 Boot s2 O0 (s2a + Rustic 1.3.0 + Delightful 3.4.1): boot fails, `Unbound values in registry placed_feature:
  rusticdelight:bell_pepper_block_patch_placed` (CD data vs the old jar). Per s2_setup: old jars out, SQ-RD-03,
  SQ-DF-07, SQ-RSP-14 (and `a` rows) NOT RUN (old jars do not boot beside CD). Hung JVM, own pid killed.
- 16:41 Boot s2a clean: Done. SQ-RD-03a run for the marker (FAIL accepted). s2_A started 16:42.
- 16:44 drive.sh (tools/scratch_query/batches/r1102b/drive.sh, log perf/runs/r1102b/drive.log) runs every boot after
  s2_A unattended (JFR_MAX=200m instead of CE_PERF=0 for disk). s1_R.qb (9 batch-fault reruns, vintage_probe.js fixed)
  waits for the end, on s1a. Agent note: FDB-05 / -07 toml live reload never fired in run 1 (possible defect).
- 17:24 s2_A done: totals: FAIL 24, LOOK 43, PASS 13 -> perf/runs/r1102b/s2/results.md. 17:33 s2_B booted.
- 17:43 s2_B done, 17:48 s2_Coff, 17:52 s2_N, 17:54 s2_RDstop (boot stopped as expected). s2 A+B judged
  (s2/judged.md): FAIL SQ-RD-22 (comfort_nourishment.json of 10-27 has no rusticdelight entries: every Rustic food
  loses Nourishment), SQ-CDF-08 (gen_compost_values.py --check: 21 compostables without a row); 23 BATCH-FAULT (13
  proven PASS), 4 OPEN, 4 NOT RUN. 17:54 s3_A started.
- 18:31 s3_A done (PASS 32, FAIL 32, LOOK 50; judge agent on s3/judged.md). s3_A2 (no farmers_upstream): SQ-FSD-02
  FAIL: boot dies, `Unbound values in registry worldgen/structure: [farmers_structures:aquacultures_cod, cacaos,
  fishs_stew, ...]` instead of loading on Farmer's Structures 1.0.0 data (log s3/srvlogs/bootA2_fsd02.log); JVM hung,
  own pid killed, driver aborted at s3A2 (expected).
- 18:44 SQ-WE-04b reboot of s3a crashed: `NoSuchFieldError: ENTITY_TYPE_REGISTRY`. A parallel session built and
  installed patcherMod 1.1.07 and creativecalendar into mods/ at 18:37-18:41 (hard-linked srv jars rewritten). Boots on
  hold; asked that session (creativeengineering2-7e) what it installed and when it is done. Later boots copy jars.
- 18:48 Peer session says it built nothing; most likely the owner installed the fixed creativecalendar (installToMods
  also rebuilt patcherMod and creativecompat): mods/ 18:41 creativecalendar (has the LoadingModList fix),
  creativeengineering-1.1.07, creativecompat. s1 to s3 ran on the 14:08 / 13:52 builds plus the 15:04 calendar fix;
  s5 and worldgen run on the 18:41 builds. drive.sh now copies every jar into srv (decouple after prep).
- 18:50 s3 judged (s3/judged.md): FAIL SQ-MRD-01 (More Delight knives mine at 8, tune_tools.js), SQ-LEM-02 (lemon
  cutting 4 slices not 8, fruitsdelight/recipes.js replaceOutput no-op), SQ-VTD7-02 (bales and salt blocks in the
  Blockrunner soft list 0.88x, changelog says 1.05x), SQ-LGM-10 (duplicate chicken_curry recipe id with Ender's, Large
  Meals curry has none), SQ-LEM-01 (14 lemons over 10 picks), SQ-POT-04 (Beautify hanging pot loses its plant on
  break), SQ-POT-03 (hanging_roots throws on 6 holders; old F1 not reproduced), SQ-FSD-02; 30 batch faults (19 proven
  PASS), 4 OPEN. SQ-WE-04b rerun on a clean restart: /wetest timeout (to judge).
- 18:52 drive.sh restarted from s5A (log perf/runs/r1102b/drive.log; first part drive_1.log).
- 19:05 Owner (relayed): worldgen slice w postponed (not run in r1102b); the owner installed patcherMod and
  creativecalendar at 18:37-18:41 themselves, no more installs planned; the owner reruns gen_comfort_nourishment.py and
  gen_compost_values.py (SQ-RD-22, SQ-CDF-08) during the run (told: done before 20:15 or after the s5B boot).
  stop_after.sh kills drive.sh at "STEP s5B2 end". Next: s1_R and s2 reruns, then judging and write-back.
- 19:35 Rerun block written: r_A.qb (28 rows: 11 slice-2, 16 slice-3, SQ-WE-04b), r_B.qb (5), r_B2.qb (SQ-WCD-04,
  wildCrops off), r_setup.md, r_skip.md; zq_probe.js (rdetail+, bytype, useitem offhand) and zact_probe.js (useon)
  updated. drive_r.sh (s1_R + r_A on s1a with jars refreshed from mods/, then rb, rb2) starts by itself when drive.sh
  is stopped after s5B2. SQ-WE-04b's 18:49 run: the server logged nothing after Done, /wetest never ran (rerun in r_A).
- 20:09 s5_A INVALID: the server crashed at 19:03:38 (11 min in): `AbstractMethodError` on
  create_mechanical_extruder `ExtrudingRecipe.getType()`, called by the new tools/scratch_query/s5_probe.js
  (`containers` / `potrecipe` / `jarrecipe` ran r.getType() over every recipe). qrun went on against the dead server
  (53 FAIL on timeouts; moved to perf/runs/r1102b/s5_dead1903/). The leftover crash report then aborted s5A2's boot.
  BATCH-FAULT (probe), not a pack defect: probe now uses getAllRecipesFor(type). drive.sh moves old crash reports
  before each boot. gotchas.md updated. 20:10 s5a deleted, drive.sh restarted from s5A (fresh world), watchers re-armed.
- 21:19 s5_A rerun done (PASS 12, FAIL 30, LOOK 27), 21:29 s5_A2. Judged A + A2 (s5/judged.md): PASS 31, FAIL 1
  (SQ-PSU-06: lemon and orange jam-from-slices pot recipes return a glass bottle, not the mason jar,
  kubejs/data/creativedelight/kubejs/fruitsdelight/recipes.js lines 50, 59), 23 BATCH-FAULT (every SQ-PST row: Serene
  Seasons `season set` needs upper case, so no season changed; s5_probe getItems / getTag / FluidStack; crows and slimes
  in crop rows; a stale stem-age capture), 6 OPEN; SQ-PSU-09 needs the owner (comfort_nourishment.json gives the
  vinegar jar no Nourishment, the row expects it). s5_R rerun batch being written (fresh A world after drive_r).
- 22:05 s5_R_A / _A2 / _A3 written (27 rows; s5_probe.js fixed: getStacks, .nbt, loadFluidStackFromNBT, selfloop by
  type; season names upper case; mob spawning off and crows / slimes killed in crop rows). drive_s5r.sh chained after
  drive_r.sh (results perf/runs/r1102b/s5r).
- 22:11 STOPPED by the owner (relayed by creativeengineering2-7e) during the s5_B2 boot (Done, no row ran). Drivers
  and watchers killed, s5b server stopped, perf session closed and reported (perf/sessions/r1102b/summary.md). s5_B and
  s1_B judged. Everything not run: "NOT RUN: stopped by the user 2026-11-02"; worldgen "postponed by the user".
  Handover: [perf/runs/r1102b/HANDOVER.md](../../perf/runs/r1102b/HANDOVER.md).

## 8. Final verdicts

Per-row verdicts: `perf/runs/r1102b/s1/judged.md`, `s2/judged.md`, `s3/judged.md`, `s5/judged.md` (the first s5_A run
in `s5_dead1903/` is invalid). Counts, defects, owner decisions and the resume steps:
[HANDOVER.md](../../perf/runs/r1102b/HANDOVER.md) sections 2 to 4.

| Slice | Judged rows | PASS | FAIL | OPEN | BATCH-FAULT (no verdict) | NOT RUN |
|---|---|---|---|---|---|---|
| s1 | 54 | 39 | 3 | 3 | 9 | 0 |
| s2 | 99 | 78 | 2 | 5 | 10 | 4 |
| s3 | 116 | 92 | 8 | 4 | 11 | 1 |
| s5 | 88 | 48 | 1 | 10 | 26 | 3 |
| total | 357 | 257 | 14 | 22 | 56 | 8 |

FAIL: SQ-VP-06, SQ-VTD6-01, SQ-FDB-04, SQ-RD-22, SQ-CDF-08, SQ-MRD-01, SQ-LEM-01, SQ-LEM-02, SQ-VTD7-02 (owner decides),
SQ-LGM-10, SQ-POT-03, SQ-POT-04, SQ-FSD-02, SQ-PSU-06; owner decision SQ-PSU-09; boot crash in Creative Calendar
(fixed, installed 18:41). Postponed by the user: worldgen slice (54 rows). Not run, stopped by the user 2026-11-02:
s5_B2 (SQ-PSM-06, -06b, -07), reruns s1_R (9), r_A (28), r_B (6, with SQ-RD-22), r_B2 (1), s5_R_A / A2 / A3 (27).
