# Scratch run r1103 (2026-11-03)

Owner request 2026-11-03 00:30: execute the scratch queue; plan approved ("ok") with the defaults: worldgen slice
stays postponed, client rows stay pinned (wave 8), unfixed r1102b FAILs are not rerun. Runner: session 51e0db54.

## 0. Performance capture

Perf session `r1103` (`perf/sessions/r1103/`), opened 00:43, JFR on, `JFR_MAX=200m` per boot, report without `--prune`.

## 1. Scope

Taken:
- Boot 0: smoke boot of the never-booted jars (wave 5 Creative Worldgen, patcherMod 1.1.08, CD and CB 00:08), dumps.
- Slice A: the 78 never-run server rows of SQ-CWG, SQ-CDJ6 (Salt, SQ-CNG, End's member, SQ-GLD, SQ-MDL, Pancake,
  Swappers) and SQ-CDJ7 (`tools/scratch_query/batches/r1103/sliceA_sections.txt`).
- Slice B: the 96 other never-run server rows (`sliceB_sections.txt`); 38 batched, 58 skipped (`sB_skip.md`).
- Phase 2: the r1102b rerun blocks (HANDOVER section 4: s1_R, r_A, r_B, r_B2, s5_R_A/A2/A3, s5_B2) on fresh worlds.

Not taken:
- Client rows: 123 CQ- ids (`client_pinned.txt`), pinned until Creative Split wave 8.
- Worldgen slice `w_*` of r1102b and the worldgen rows of slice B (SQ-WGE-09 to -16): postponed by the owner.
- r1102b FAILs not fixed since: SQ-MRD-01, SQ-LEM-01, -02, SQ-LGM-10, SQ-POT-03, -04, SQ-FSD-02, SQ-PSU-06,
  SQ-VTD6-01, SQ-FDB-04.

## 2. Tools

- `tools/scratch_query/batches/r1103/`: `b0.qb`, `sA_*.qb` + `sA_setup.md` / `sA_skip.md`, `sB_1.qb`, `sB_2.qb` +
  `sB_setup.md` / `sB_skip.md`, drivers `drive_sB.sh`, `drive_p2.sh` (phase 2 reuses `batches/r1102b/*.qb`).
- New probes: `zsb_probe.js` (/zsb), `zlbs_probe.js` (/zlbs) (slice B writer; untested before this run).
- `prep_srv.sh`: SWAPS now also cover creativeworldgen and creativeLabels (newest build jar).
- Results: `perf/runs/r1103/{b0,sB,p2,sA}/`.

## 3. Jars

Checked 00:43. `prep_srv.sh` swaps in build jars newer than `mods/`; the srv jars are copies (not links).

| Jar | `mods/` | build/libs | Rows |
|---|---|---|---|
| creativeengineering-1.1.08 (patcherMod) | 11-02 23:20 | 11-03 00:08 | all |
| creativedelight-1.0.0 | 11-03 00:08 | 11-03 00:48 (runner rebuild, LemonTreePickYieldMixin fix) | food, CD members |
| creativebrewing-1.0.0 | 11-03 00:08 | 11-03 00:08 | brewing |
| creativeworldgen-1.0.0 | 11-02 23:16 | 11-02 23:16 | SQ-CWG, loot |
| creativecalendar-1.0.0 | 11-02 18:41 | 11-03 00:09 | seasons, time |
| creativecompat-1.0.0 | 11-02 18:41 | 11-02 23:17 | compat |
| creativedevtools-1.0.0 | 11-01 12:49 | 11-02 22:46 | dumps, cegen |
| creativeLabels | 1.0.0 (10-27) | 1.0.1 (11-02 22:46) | SQ-LBS |

## 4. Order

Changed from the approved plan at 01:00 (owner informed): slice B and phase 2 batches were ready before slice A's,
so they run first; the time total is unchanged.

| Step | Setup | Run | Estimate |
|---|---|---|---|
| Boot 0 | b0, group A | dumps | 40 min (3 boots: two boot crashes fixed) |
| Slice B | sb1, group A, 2 boots | `drive_sB.sh` | 30 min |
| Phase 2 | s1a A, rb B, s5ra A, s5b B; 8 boots | `drive_p2.sh` | 2 h 45 min |
| Slice A | per `sA_setup.md` | | 2 h |
| Judge + write back | | | 45 min |

## 5. Row map

| Slice | Batches | Results |
|---|---|---|
| Boot 0 | b0.qb | perf/runs/r1103/b0/ |
| B | sB_1.qb, sB_2.qb; reruns sB_2 --rows SQ-LBS-* (sBR), sB_R.qb (sBR2) | perf/runs/r1103/sB, sBR, sBR2 |
| A | sA_1..4.qb, sA_g10.qb; reruns sA_R.qb part 1 (sAR), part 3 (sAR3) | perf/runs/r1103/sA, sAR, sAR3 |
| Phase 2 | r1102b s1_R, r_A, r_B, r_B2, s1_A (SQ-VP-06), s5_R_A*, s5_B, s5_B2; reruns p2_R.qb, p2_R2.qb | perf/runs/r1103/p2, p2R, p2R2 |
| ENM-03 repro | chain4.sh | perf/runs/r1103/enm3 |

## 6. After the run

- Owner: the installed `mods/creativedelight-1.0.0.jar` (00:08) does not load (LemonTreePickYieldMixin); the fixed
  build (00:48) is in `development/creativeDelight/build/libs/`, not installed. `creativeLabels-1.0.1` is not in `mods/`.
- Fixes waiting for the owner: the 19 FAILs (FINAL.md, TODO rows). Owner decisions and stale row texts: FINAL.md.
- Left: sA_R.qb part 2 (old world, village half of SQ-CWG-08), SQ-RD-10b (group B), SQ-ENM-05b (after the SQ-ENM-03
  fix), OPEN rows listed in FINAL.md, client rows, the worldgen slice.
- Generators not run (need client dumps): `/cegen run food_families`, `tools/gen_can_types.py` (SQ-GLD2-05).

## 7. Run notes

- 00:42 RUNNER.md claimed. 00:43 perf session r1103 open. Disk 19 GB free.
- 00:44 Boot 0 CRASH (mod loading): `creativedelight.mixins.json:fruitsdelight.LemonTreePickYieldMixin` failed to apply:
  `@ModifyArg` handler took the enclosing `use()` params, which ModifyArg does not capture. The installed `mods/`
  CD jar (00:08) has the same mixin, so the dev client does not load either. Fixed in source (handler takes
  `(ItemLike, int)`, ThreadLocalRandom, same 2 to 5 range), CD rebuilt 00:48 (not installed to `mods/`).
  Crash report in `perf/runs/r1103/b0/`.
- 00:49 Boot 0 CRASH: KubeJS "startup script syntax errors": `Invalid file name: Uppercase 'R' in
  kubejs/data/creativeworldgen/kubejs/loot/README.md` (moved there by the wave-5 carve). Renamed to `readme.md`;
  the two references (`tools/gen_loot_config.py`, `loot_config.json`) updated. Affects the dev client too.
- 00:53 Boot 0 up. No `Error occurred while handling event`, no KubeJS script errors.
- 00:54 to 00:59 `/dumpall` 24/25 ok in 211 s (failed: `/dumpclimatecaves`), `/dumpfood` 1609 foods. `/cegen
  food_families` was a BATCH-FAULT (syntax is `cegen run <id>`); generators not run: food_families.py and
  gen_can_types.py write repo files from food dumps, which need the client. Deferred to the owner. Dumps stay in the
  scratch srv (not copied into `kubejs/exported/`).
- 01:01 slice B started (`drive_sB.sh`), level sb1. 01:02 boot 1 (Creative Labels 1.0.0) up; SQ-LBS-05a FAIL on
  the probe: `/zlbs dump` raised "An unexpected error occurred trying to execute that command" (uncaught Java error
  in zlbs_probe.js dump, not a Labels result), so SQ-LBS-05 has no baseline (BATCH-FAULT expected). 01:05 boot 2 up.
- 01:10 SQ-CDF-08 (rerun) offline: `gen_compost_values.py --dump <b0 compost.json> --check` exits 0 (0 stale, 0 new). PASS.
- 01:15 slice A batches ready (writer agent): 5 files, 77 of 78 rows; boot 1 runs the 1.1.06 release from
  `deploy/latest` (old jars are no longer in `mods/`), so old-world rows cross the real upgrade path.
  `drive_sA.sh` written. SQ-VP-06 rerun added to `drive_p2.sh` rA (`s1_A.qb --rows SQ-VP-06`); SQ-RD-22 rerun is in r_B.
- 01:17 `chain.sh`: after slice B, `drive_sA.sh` then `drive_p2.sh` (order: slice A before the reruns).

- 01:20 every `/zlbs` call in boot 2 also failed (5 of 5): `run()` called `l.dimension.location()` outside its try
  (KubeJS makes `level.dimension` a ResourceLocation property). zlbs_probe.js fixed (`server.overworld()`; `be()` by
  block id, not `getClass()`). SQ-LBS-00 to -05 rerun queued: `chain2.sh` runs `drive_sB.sh` with LEVEL=sbr,
  R=perf/runs/r1103/sBR, ROWS2=SQ-LBS-00..05 after chain.sh ends. `drive_sB.sh` takes LEVEL / R / ROWS2 for reruns.- 01:25 slice B done: sB_2 totals FAIL 16, LOOK 18, PASS 5 (sB_1: SQ-LBS-05a FAIL, probe). Judge agent on
  `perf/runs/r1103/sB/judged.md` (sB_R.qb for cheap batch faults).
- 01:25 slice A boot 1 (deploy/latest 1.1.06, level sAo) up 01:26; sA_1: PASS 2, FAIL 2, LOOK 3. SQ-GLD-01o FAIL (no
  `goldenfood:gf_enchantment` book in the saved chest), SQ-CWG-08o FAIL ("That position is not loaded" at the village
  place): their boot-2 read-backs are setup gaps. 01:30 boot 2 (current jars on sAo) preparing.
- 01:42 slice B judged (`perf/runs/r1103/sB/judged.md`): PASS 29, FAIL 1 (SQ-VNG-01: four pickling recipes still
  take `vintagedelight:vinegar_bottle`; `i.vinegar` in item_dict.js names the bottle), BATCH-FAULT 2 (SQ-CN-04b: same
  dimension bug in zsb_probe.js, fixed; SQ-WCT-08: bot aim, fixed in `sB_R.qb`), RERUN pending 7 (SQ-LBS). Owner
  decisions: SQ-FO-02, SQ-CNS-03, SQ-FFB-02 (judged.md). `chain3.sh` runs sB_R.qb after chain2.
- 01:31 slice A boot 2 up on the 1.1.06 world with current jars (no event errors: the upgrade path loads). 02:06
  sA_2 totals PASS 15, FAIL 22, LOOK 36; judge agent on `perf/runs/r1103/sA/judged.md`. 02:07 boot 3 (config edits).
- 02:11 boot 3 done (sA_3, 10 rows). 02:11 and 02:14 SQ-GLD-10a / -10b boots stopped at startup as intended.
- 02:17 boot 4 without `endersdelight-1.19-1.2.2.jar` CRASHED: KubeJS startup script syntax errors (crash report
  `perf/runs/r1103/sA/a4_crash/`): SQ-ENM-03 FAIL. Driver bug: the srv dir (with `logs/kubejs/startup.log`) was
  deleted before copy; `chain4.sh` reruns the boot at the end and keeps the logs in `perf/runs/r1103/enm3/`.
  02:22 retry with the jar on level sAb2 up; `sA_4.qb --from SQ-VTDA-10` (SQ-ENM-05b unrun).
- 02:33 slice A done (boot 4 sAb2 `--from SQ-VTDA-10`). Judge (boots 1 to 3): PASS 66, FAIL 2 (SQ-GLD-03 golden
  honey / milk add an unexplained 280-tick effect bundle; SQ-ENM-04b `members.enders_additions` key gone, CDConfig
  readMembers lists one id per nested jar), BATCH-FAULT 9, OPEN 3. SQ-GLD-01o was not a setup gap (regex). Owner
  decisions and stale row texts in `perf/runs/r1103/sA/judged.md`. `chain5.sh` runs `sA_R.qb` part 1 (13 rows, fresh
  superflat) after chain4; part 2 (old-world village half of SQ-CWG-08, two boots) NOT queued: only the village
  read-back is open.
- 02:33 phase 2 started (`drive_p2.sh` rA).
- 02:45 sA_4 judged. Slice A totals (89 row halves): PASS 67, FAIL 4 (SQ-GLD-03, SQ-ENM-04b, SQ-ENM-03, SQ-CWG-06:
  `META_PART_KEYS` in `kubejs/data/creativeworldgen/kubejs/worldgen/structure_overrides.js` lacks `punch`, so the
  meteorite desert pyramid meta never loads), BATCH-FAULT 12, OPEN 6. Most of sA_4 was a runner fault: `--from
  SQ-VTDA-10` skipped setup row SA4-00 (no `{SBH}` capture). `chain6.sh` runs `sA_R.qb` part 3 (6 rows, fresh group B
  world sar3) after chain5.
- 02:33 to 03:18 phase 2 rA / rB / rB2 on fresh worlds: s1_R PASS 1 FAIL 7 LOOK 1; r_A + r_B + r_B2 PASS 3 FAIL 18
  LOOK 14; SQ-VP-06 rerun FAIL (4 s). High FAIL share expected from missing prior-boot state (r1102b ran these on its
  s1a world); judge agent on `perf/runs/r1103/p2/judged.md`. 03:18 RA started.
- 03:40 phase 2 first half judged (`perf/runs/r1103/p2/judged.md`, 45 rows): PASS 20, FAIL 4 (SQ-VP-06 PackJams
  Forge fluids clash with the soft fluid ids, jam jars not soft-fluid items; SQ-CDFW-07 duck fillet cut loop;
  SQ-POT-04v two pots drop nothing; SQ-WCD-02 no jungle melons), BATCH-FAULT 19 (13: home markers missing on fresh
  worlds), OPEN 2. `chain7.sh` runs `p2_R.qb` (19 rows) after chain6. SQ-RD-10b, -26, SQ-WCD-04 need group B locates:
  not rerun.
- 03:18 to 04:30 phase 2 RA / RA2 / RA3 (s5_R_A*: PASS 8, FAIL 11, LOOK 12 cumulative), B (SQ-PSM-06a, 11 s, LOOK 1)
  and B2 (PASS 2, LOOK 3). 04:30 chain.sh done; chain2 (SQ-LBS rerun) started. Judge resumed for the second half.
- 04:45 phase 2 second half judged: PASS 18, FAIL 5 (SQ-RB-21b Copper Pot never starts with a bowl in slot 5 and
  two leftover kinds, `CopperPotBlockEntity.leftoversFit`; SQ-PST-08, -10, -11, -11b no winter slowdown on Botania /
  Vintage / IW grasses, Calendar `ModdedGrassSpreadSeasonMixin` likely not applying, `require = 0` hides it),
  BATCH-FAULT 5, OPEN 3; s5b all PASS. Phase 2 totals (76): PASS 38, FAIL 9, BATCH-FAULT 24, OPEN 5. `chain8.sh` runs
  `p2_R2.qb` (two boots) after chain7. 04:34 chain2 and 04:39 chain3 done.
- 04:34 to 05:25 reruns: chain2 SQ-LBS (sBR), chain3 sB_R (sBR2), chain4 ENM-03 repro (enm3), chain5 sA_R part 1
  (sAR), chain6 sA_R part 3 (sAR3), chain7 p2_R (p2R). chain8 p2_R2 running.
- SQ-ENM-03 cause (enm3 startup.log): `MoreJSEvents.registerPotionBrewing` throws "Ingredient must have at least one
  item": Creative Brewing `potions.js` line 47, levitation reagent `endersdelight:...` with no mod-loaded guard.
- 05:27 final judge agent on the reruns; writes `perf/runs/r1103/FINAL.md`.
- 05:47 last boot stopped (chain8 p2_R2). 05:50 perf session stopped and reported (perf/sessions/r1103/summary.md).
  Final judge: FINAL.md. 13 GB free at the end.

## 8. Final verdicts

[perf/runs/r1103/FINAL.md](../../perf/runs/r1103/FINAL.md) (every FAIL, counts, owner decisions, stale row texts);
row tables in `perf/runs/r1103/{sB,sA,p2}/judged.md`.

| Slice | Rows | PASS | FAIL | BATCH-FAULT left | OPEN |
|---|---|---|---|---|---|
| sB | 39 | 36 | 3 | 0 | 0 |
| sA | 89 | 80 | 4 | 1 | 4 |
| p2 | 76 | 55 | 12 | 1 | 8 |
| Run | 204 | 171 | 19 | 2 | 12 |

## 9. r1103w: worldgen slice (targeted run, afternoon)

Owner request 2026-11-03 13:20: "execute the worldgen scratchtest in the queue". Scope chosen by the owner: full
worldgen set. Disk: the owner OK'd clearing old session scratchpads (11 GB freed, 22 GB free at start).

- Scope: the r1102b worldgen slice `w_1` to `w_11` (54 rows: first-tick water, census ties, SQ-WGE-01 to -08,
  SQ-EFB, SQ-FGDC, SQ-PSW), copied to `tools/scratch_query/batches/r1103w/` and refreshed for the wave 5 carve; new
  `w_12_B.qb` on the W1 world for SQ-WGE-00b, -09 to -16, SQ-MOB-N5, -N6, -N8; SQ-CWG-08 village half
  (`r1103/sA_R.qb` part 2, two boots: 1.1.06 release, then current jars) at the end.
- Not taken: CQ-WGE-07a to -07c (client), the look halves listed in `r1103w/w_skip.md`.
- Known before the run: SQ-CWG-06 FAIL (r1103: `META_PART_KEYS` lacks `punch`, not fixed), so SQ-WGE-12 (punch
  meteorite pyramid) is expected to FAIL on the same cause.
- Refresh for the carve: worldgen keys live in `config/creativeworldgen-common.toml` now (every batch grep and
  `qrun.py toml` call retargeted); cross-boot files go to `perf/runs/r1103w/w/`; RES `perf/runs/r1103w/w/`.
  `tools/structure_tuning_sim.js`, the four `tools/test_*.sh` Rhino harness headers and `RhinoRun`, `RhinoBeeNests`,
  `RhinoMobSpawns`, `RhinoWildFlowers` read the scripts from `kubejs/data/creativeworldgen/kubejs/worldgen/`
  (they still read `kubejs/server_scripts/worldgen/`, removed by the carve). `test_worldgen_tools.sh` then passes all 12 groups.
- Jars: patcherMod 1.1.08 build 11-03 01:30, CD build 01:31, Devtools build 11-02 22:46, Creative Labels 1.0.1
  (prep_srv.sh swaps, newer than `mods/`); Creative Worldgen 11-02 23:16 (= `mods/`, source unchanged since).
- Perf session `r1103w`, JFR on with `JFR_MAX=200m` per boot (disk).

### 9.1 Run notes

- 13:20 RUNNER.md claimed. Old scratchpads cleared (22 GB free). Gate `test_worldgen_tools.sh` failed on the moved
  `structure_overrides.js` path; tools fixed, gate passes.
- 13:29 perf session r1103w open. W1 prep (`wge`, B, seed 8675309, earth-fill log ALL, meta log true). 13:30 W1 boot.
- 13:34 W1 Done (95.5 s). `w_1_B.qb` started (qrun pid 2840943, output `perf/runs/r1103w/w1_qrun.out`).
- 13:40 SQ-WGE-00b offline: PASS (1514 swaps in 117 templates; 274 worldgen files, 0 problems; `meta/waystone_ruins`
  in `ruins` at weight 1, its "empty biome tag" estimate note is the same as the other metas' and was there before;
  moobloom check OK, 38 spawn rules; `end_divers.js` parses). Output `perf/runs/r1103w/w/wge00b.txt`.
- 13:41 SQ-PSW-00 qrun FAIL is a BATCH-FAULT: the one "glowstone" line in `nether_worldgen_scan.md` is the decompiled
  Better Nether Fortresses item-frame processor note (glowstone dust in Nether frames), not Overworld glowstone; 0
  worldgen problems. The scan regenerated `feature_writeup/nether_worldgen_scan.md` (43 structures [47]: the C3 swaps
  removed the crimson / warped rows; the committed copy predates C3). Kept, it is a generated file.
- 13:50 New `w_12_B.qb` (generated by `r1103w/gen_w12.py`) for SQ-WGE-09 to -16, SQ-MOB-N5, -N6, -N8 on the W1 world
  after w_2; new probe `tools/scratch_query/meteor_heights.py` (SQ-WGE-13: AE2 sky stone clusters against the ring
  ground height). SQ-WGE-09 reads the SQ-PSW-02 / -03 rows plus a Nether soul-block count; SQ-WGE-15 reruns
  SQ-PSM-02 (r1102b BATCH-FAULT) with the part line printed; SQ-PSM-03 PASSED in r1102b.
- 13:41 SQ-EFB-01 FAIL (BATCH-FAULT): `/zq under` threw `IllegalAccessException` on
  `getAllStarts().entrySet().forEach` (Rhino and the JDK's private unmodifiable entry set). Fixed in
  `tools/scratch_query/zq_probe.js` (public HashMap / ArrayList copies); gotcha added. No `/reload` in W1 (drops
  Chunky): every `/zq under` row of W1 is rerun at the W1b restart (driver step w2 reads `perf/runs/r1103w/w1_rerun_rows.txt`).
- 13:41 SQ-WGE-01b: shadowlands from 0 0 at 2552 blocks, from 3000 0 at 1629 (row: under 1500); from 0 3000 45,
  from -3000 -3000 1362. Real FAIL candidates, judged at the end.
- 13:42 Driver `tools/scratch_query/batches/r1103w/drive.sh` started (log `perf/runs/r1103w/drive.log`): waits for W1,
  then W1b (+ W1 rerun rows), W1c (w_12), W2, W3, A1 to A4, F1, F2, G1, then SQ-CWG-08 part 2 (c8o, c8).
- 14:38 W1 CRASH: `OutOfMemoryError: Java heap space` (6 GB) in W1-GEN-SP (spawn r 1000, 11,757 of 16,129 chunks,
  about 30k chunks into the boot; Chunky down to 0.5 cps before it). Class histogram (`perf/runs/r1103w/w1_oom_histo.txt`):
  2.9 GB `long[]` and 18.3 M `LongArrayList` in 4461 `LongArrayList[4096]` arrays, the shape of Distant Horizons'
  `FullDataSourceV2` (the only jar with that array type). DH runs on the dedicated server (`enableServerGeneration`,
  `enableDistantGeneration` true) and its LOD backlog grew with the pregen; latest.log carries DH's "slow down your
  world pre-generator" warning. FINDING for the owner (pack: DH server side, long pregen), TODO row. Crash report and
  logs in `perf/runs/r1103w/w/`, JFR dump `w1/srv/perf/exit/w1_oom.jfr`. The hung JVM (pid 2837349, mine) took SIGKILL.
- 14:40 Runner slip: a `pkill -f "r1103w/drive.sh"` matched its own shell (the skill forbids that); it ended the driver
  and that command only. Pids from here on.
- 14:50 W1 boot 2 on the same world, srv only `-Xmx8G` (DH kept as shipped), fixed `/zq` copied in: Done 5.4 s.
  `w_1_B.qb --from W1-GEN-SP` (qrun pid 2887601, appended to w1_qrun.out); driver restarted (pid 2887602),
  `w1_rerun_rows.txt` = SQ-EFB-01 (the only `/zq under` row before the crash).
- 15:23 Every `/zq under` call of W1 boot 2 failed too: `TypeError: kind is not a function` (block-level function
  declarations invisible to the forEach closures in Rhino). Fixed (`let kind = function`), gotcha added. The SQ-EFB
  probe rows of W1 judge nothing this boot. `gen_w1r.py` writes `w_1r_B.qb`: the 23 W1 rows that use the probe or its
  judges, with the generation-time judges reading `../w1_all.log` (boot 1 to the OOM + boot 2 + W1b). Driver restarted
  (pid 2917496): W1b runs w_2_B.qb and then w_1r_B.qb on the same boot.
- 15:26 `/reload` on W1 (Chunky rows all done) with the fixed probe: `/zq under` prints full lines again (badlands tile 15 starts). W1 probe rows from SQ-EFB-09 on judge real output; w_1r_B.qb still reruns the earlier ones.
- 16:25 W1 batch done: PASS 57, LOOK 21, FAIL 12 (`perf/runs/r1103w/w/results.md`). Probe-broken rows (EFB-01, -06,
  -07, -09, -11, -21 partly) rerun in W1b. Real-looking FAILs to judge: SQ-WGE-01b / -01b-2 (shadowlands 2552 / 1629
  blocks), SQ-WGE-05b (waystone in 8 of 10 villages, need 9: none at -240 1120 and 2496 -1488), SQ-PSW-04 (16 single
  glowstone blocks at y 70 to 196 in 16 Overworld chunks, one at the -704 2512 village), SQ-WGE-03b (no
  undead_castle limit-800 line), SQ-EFB-21 (a T&T badlands village kept with over6 55, base water 6, foreign earth).
  16:25 driver: W1 stopped, W1b (w2) boot.
- 17:20 W1b done: w_2 6 PASS; w_1r rerun (24 rows) with the working probe. 17:20 W1c boot (w_12_B.qb).
- 17:45 W1 judged (judge agent, `perf/runs/r1103w/w/judged_w1.md`, 97 rows): PASS 73, FAIL 10, BATCH-FAULT 4, OPEN 7,
  LOOK-client 3. FAIL: SQ-WGE-08 (356 ERROR lines [341], 64 client-class RuntimeDistCleaner lines [37]),
  SQ-WGE-01b, -01b-2 (shadowlands share on target 16.6 %, but clustered: 2552 / 1629 blocks), SQ-EFB-02 (plains
  village -25 32: 5 columns over 6, logged 0), SQ-EFB-03 (hobbit house 106 96), SQ-EFB-04 (meta/jungle_temple_ruins
  17 -108: 72 columns over 6, base water 76, logged "0 over air": the Better Jungle Temples temple has no earth
  pieces), SQ-EFB-21 (T&T badlands village -227 263 kept at 212 over air under the huge limit 320, 55 columns over 6),
  SQ-EFB-11 (savanna village 17 -82: logged 1 over air, placed 166 columns over 6 and water 7 deep), SQ-WGE-05b
  (8 of 10; one savanna village's waystone pieces hold only air / dirt), SQ-PSW-04 (16 Overworld glowstone, all
  under a waystone: template `waystones:village/common/waystone`, villages and waystone_ruins; needs a swap rule).
- Tooling found by the judge: (1) the earth-fill `--except` regex had `mine`, which matches `minecraft:`, so every
  vanilla start was silently excepted; fixed in the w_4_B.qb copy (`:mines?\b|_mine\b`), W1 rows judged with the
  corrected regex. (2) `[zq] under` lines carry no dimension, so the Nether SQ-PSW-02 probes fell into the Overworld
  judges (judge separated them). (3) `/place structure` saves no start and runs no earth fill, so SQ-EFB-07, -08, -15
  placed parts cannot be probed (batch faults; rows need natural starts).
- 17:56 SQ-WGE-12 FAIL as expected ("There is no structure with type creativeengineering:meta/meteorite_desert_pyramid";
  `[meta-structures] replacement ... is not a loaded structure`: the SQ-CWG-06 cause).
- 18:00 STOPPED by the owner (relayed by session creativeengineering2-0b): "finish the test in progress, then stop".
  Driver (pid 2917496) killed; qrun stopped after SQ-WGE-14 (it had finished); SQ-WGE-15 had just started and is
  discarded (half-run, no result recorded). W1c stopped cleanly, no JVM up. Perf session closed and reported (no prune).
- 18:10 SQ-WGE-13 probe fault: `meteor_heights.py` took WORLD_SURFACE (the sea surface over oceans) as the ground.
  Switched to OCEAN_FLOOR, rerun offline on the saved W1 world: 9 meteorites, surface 3, underground 5, air 1.

### 9.2 Verdicts

W1 and W1b (97 rows): judged in [perf/runs/r1103w/w/judged_w1.md](../../perf/runs/r1103w/w/judged_w1.md) (judge
agent, regex fix applied): PASS 73, FAIL 10, BATCH-FAULT 4, OPEN 7, LOOK-client 3. W1c and offline rows:

| ID | Result | Evidence |
|---|---|---|
| SQ-WGE-00b | PASS | 1514 swaps in 117 templates; 274 worldgen files, 0 problems; waystone_ruins in `ruins` at weight 1; moobloom check OK, 38 spawn rules; `end_divers.js` parses |
| SQ-WGE-09 | PASS | SQ-PSW-02 and -02-b to -d PASS (found in the Nether), -o1 to -o4 PASS (none in the Overworld), SQ-PSW-03 PASS; soul blocks kept in the Nether ruins (city 53, dungeon 39, rare ruin 8 lanterns) |
| SQ-WGE-10 | PASS (state LOOK-client) | 9 structures placed on fresh chunks (-6000 to -2800, z -6000); 0 crimson / warped trapdoors, buttons, slabs, plates, stairs, stripped hyphae; mangrove trapdoor 39, button 30, slab 6, dark oak trapdoor 89 present. Facing / half / open and the desert temple plate puzzle: client look at -5600 -6000 |
| SQ-WGE-11 | OPEN | the census reports slots per structure, not meta shares (metas and anchors show the same slots; built 0 to 2), so the 1.7x / 0.6x / 0.66x shares cannot be read from it; needs a count of meta starts over a large generated area |
| SQ-WGE-12 | FAIL | `meta/meteorite_desert_pyramid` is not a loaded structure (SQ-CWG-06: `META_PART_KEYS` lacks `punch`, `kubejs/data/creativeworldgen/kubejs/worldgen/structure_overrides.js`) |
| SQ-WGE-13 | OPEN | 9 meteorites in the spawn r 1000 square (OCEAN_FLOOR ground): surface 3, underground 5, air 1 (+60, inside 16 to 96); too few for 40 / 30 / 30 and two are metas; no `could not move` error; meta locates: village -1296 240, camp -7136 6160, underwater 400 304 (client look) |
| SQ-WGE-14 | FAIL (probable) | 3 distinct waystone_ruins located (one locate repeated a hit); a waystone block within 24 blocks in 1 of 3 (-224 3184, anchor level_one_ruins, part at -237 101 3166); 0 at -464 -192 and 2576 -512; `waystone_ruin_part` alone found only next to a meta (-466 -210); ruin_scatter found. Check at -464 -192 first: whether the part generated without its waystone or the locate point is off |
| SQ-WGE-15, -16, SQ-MOB-N5, -N6, -N8 | NOT RUN | stopped by the owner (SQ-WGE-15 half-run, discarded) |

### 9.3 Not run (stopped by the owner, not failed)

- W1c remainder: SQ-WGE-15, SQ-WGE-16, SQ-MOB-N5, -N6, -N8 (`w_12_B.qb`, `--from SQ-WGE-15` on a W1 world).
- W2 `w_3_B.qb` (SQ-EFB-19, -22, SQ-WGE-06d-x), W3 `w_4_B.qb` (SQ-EFB-16, SQ-WGE-02c), A1 to A4 `w_5` to `w_8`
  (SQ-PSW-01, SQ-FTW-05, -06, SQ-IP-15, -16, SQ-WGE-06a to -06c), F1 / F2 `w_9`, `w_10` (SQ-FTW-02, SQ-TIE-01),
  G1 `w_11` (SQ-FGD-12m, -15c), SQ-CWG-08 village half (`r1103/sA_R.qb` part 2). Client rows CQ-WGE-07a to -07c.
- Resume: `drive.sh` steps (`w12` needs `--from SQ-WGE-15` in its run line; the W1 world is kept in this session's
  scratchpad `r1103w/w1/srv/wge`, 4 GB, and may be wiped), then `w3 w4 wA w9 w10 w11 c8o c8`.

### 9.4 Findings and owner decisions

1. Distant Horizons server side: a 6 GB server ran out of heap in a long Chunky pregen (LOD backlog). TODO
   `pack/server #12`. Owner: turn DH server generation off or bound it, or raise the heap.
2. Earth fill misses overhangs the log reports as 0 (SQ-EFB-02 plains village -25 32, SQ-EFB-11 savanna village
   17 -82: 166 columns over 6, water 7 deep, logged 1; SQ-EFB-04 jungle temple ruins meta: the Better Jungle Temples
   temple has no earth pieces, 72 columns over 6). TODO `structures/earthfill #18`.
3. SQ-EFB-21: the huge-size limit (320 over air) keeps a T&T badlands village with 55 columns over 6. Owner: is the
   limit right for villages?
4. SQ-PSW-04: glowstone under every Waystones village waystone (`waystones:village/common/waystone`, also inside
   meta/waystone_ruins). Needs a row in `nether_swap_rules.json`. TODO `biomes/netherblocks #4`.
5. SQ-WGE-01b: End shadowlands share is on target (16.6 %) but clustered: 2552 blocks from 0 0. TODO
   `worldgen/biomes #9`. Owner: accept clustering, or change the row's 1500 limit.
6. SQ-WGE-05b: village waystone in 8 of 10 (one savanna village's waystone pieces hold no waystone, one taiga
   village has no waystone piece). TODO `structures/rates-and-groups #11`.
7. SQ-WGE-08: boot ERROR lines 356 [341], RuntimeDistCleaner client-class lines 64 [37]; no mod named. TODO
   `pack/server #37`.
8. SQ-WGE-12 / SQ-CWG-06: punch meta dropped (known, `creativesplit/worldgen #8`).
9. Tooling fixed in `tools/` during the run: `zq_probe.js` (two Rhino faults), the earth-fill `--except` regex
   (`mine` matched `minecraft:`; fixed only in the unrun w_4 copy), `meteor_heights.py` (OCEAN_FLOOR), the moved
   worldgen script paths in the offline harnesses. Gotchas added. No pack or gameplay file changed; no changelog bullet.

### 9.5 Perf

Perf session `r1103w` (4 boots, JFR 200 MB cap, no prune): `perf/sessions/r1103w/summary.md`. W1 boot 1 OOM
recording `w1/srv/perf/exit/w1_oom.jfr` (scratchpad) and histogram `perf/runs/r1103w/w1_oom_histo.txt`.
