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

Filled per slice in the results dirs (`results.md`, `judged.md`).

## 6. After the run

To fill.

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
- 01:01 slice B started (`drive_sB.sh`), level sb1.

## 8. Final verdicts

To fill.
