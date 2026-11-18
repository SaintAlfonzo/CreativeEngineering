# Scratch run 2026-11-18 (r1118)

Owner request 2026-11-18 00:40: "orchestrator for a long running scratch test execution. the goal of this run will be
bug discovery and documentation, not fixing. use agents to run scratch tests to optimize context, apply content/tool
fixes to errors preventing testing passed up by the agent." Main session (883b649d) as orchestrator. The server rows
run in one detached `qrun.py drive` (run id r1118, state in `perf/runs/r1118/drive/`); judge subagents
(judge-1, judge-2, ...) take finished steps in batches, judge LOOKs, sort FAIL vs BATCH-FAULT, fix row steps, and
write sections 7 and 8. The orchestrator keeps sections 1, 3, 4 and 9 current and makes the content/tool fixes judges
pass up. Behavior fixes are findings (section 9 + TODO rows), not made in the run.

## 0. Performance capture

Perf session `r1118`, opened 00:49 before the first boot (collector pid 4001527); the drive passes `--session r1118`.
Closed 17:05 and reported (no `--prune`): [perf/sessions/r1118/summary.md](../../perf/sessions/r1118/summary.md), per boot `perf/sessions/r1118/boots/*/report.html`.

## 1. Scope

Taken: `qrun.py plan` default (saved as `perf/runs/r1118/plan.json`): 832 rows (jar-changed 807, stale 25) in 167
drive steps; phases server 760 rows, longwait 6, worldgen 62, long 4. Estimate 561 to 1755 min (drive: about 1826).

Result: STOPPED by the owner 2026-11-18 16:59 at step 150 of 167 (section 8, 10).

Not taken: client rows (pinned owner note 2026-11-01, and drive is server-only); `--never` / `--failed` /
`--rewritten`; the 3 owner playtest rows. The 25 "rewrite before running" rows run as written; a judge rewrites a row
only when its result shows the row is stale (BATCH-FAULT).

Preflight 00:45: `check_all.py --fast` 3 FAIL, all offline doc lints in files with other sessions' uncommitted work,
none affects a boot (left to their owners): `status_index` and `test_queue_rows` (SQ-MELT-08b in MELT.qb has no
steps and no @draft), `comment_dates` (OwnConfigLogic.java:26, Melting.java:486, passthrough_tags.js:134). Disk 50 GB
free. No game JVM up.

## 2. Tools

`qrun.py drive` (qdrive.py; first bulk use of the driver), `qrun.py run` per step, `qrun.py judge` / `table`,
`perf_session.sh`. Server dirs `perf/runs/r1118/sNN`, results `perf/runs/r1118/sNN_res`, driver logs
`perf/runs/r1118/drive/`.

## 3. Jars

Built, not installed at the start (prep_srv.sh swaps the build jar in where newer than `mods/`; each step pins its
own jars, `sNN/pinned.json`):

| Jar | `mods/` | build/libs |
|---|---|---|
| creativeCalendar | 1.0.0+f63fca1b.2611172128 | 1.0.0+5c65bb80.2611172316 |
| creativeCommon | 1.2.0+2029cd2c.2611172231 | 1.2.0+5c65bb80.dirty-f76642.2611172335 |
| creativeCompat | 1.0.0+4c5a5747.2611172244 | 1.0.0+5c65bb80.2611172316 |
| creativeDelight | 1.1.0+2029cd2c.2611172231 | 1.1.0+5c65bb80.dirty-482897.2611172334 |
| creativeLoot | 1.0.0+f63fca1b.2611172128 | 1.0.0+5c65bb80.2611172316 |

The other own jars: `mods/` and the build agree.

## 4. Order

One drive, plan order: server (steps by rows file, cheapest groups first), longwait, worldgen, long. Group letters
collapse to A or B per step (qdrive). Judges take steps in batches as they finish.

## 5. Row map

Steps and rows: `python3 tools/scratch_query/qrun.py drive status r1118`.

## 6. After the run

Write-back per skill B5; perf report; RUNNER.md idle.

## 7. Run notes

- 00:49 drive r1118 started (167 steps, 832 rows), perf session r1118 open.
- 00:55 drive hung in step 1: `scratch_server.sh boot` was run with a captured pipe and the fifo feeder (`sleep
  infinity > in.fifo &`) inherited stderr, so `subprocess.run` never returned although the server reached Done at
  00:51. Tool fixes: feeder stderr to /dev/null (scratch_server.sh); qdrive boot/stop output to `drive/sNN_boot.txt` /
  DEVNULL. Driver killed (-9), step 1 server stopped by hand (the watchdog starts only after boot returns), resumed
  00:56.
- 00:57 step 1 refused by qpin: `kotlinforforge-3.12.0-all.jar` "has no mod_sides.md row". The row exists with a bare
  (unticked) id cell, which `qpin.listed()` skipped. Tool fix: any backticked `.jar` name and a bare id cell count.
  Step 1 left `incomplete` (rerun by `drive resume` at the end); step 2 on runs with the fix.
- 01:00 new hang poll `tools/scratch_query/drive_watch.py` (owner request): run in the background, exits on BATCH n
  (judge hand-off), FINISHED, DRIVER-DEAD, STALL (10 min without interaction on the running step, with crash reports,
  JVMs, log tail; `--jstack` on the second silent check), ORPHAN-JVM, TIMEOUT heartbeat. Never kills.
- 01:03 owner rulings for this run: (1) a scratch server of this run silent for 30 min after the jstack check may be
  killed (-9) without asking; the row is recorded HANG (finding) and the drive continues; (2) no 12-hour polling cap:
  run to the end, write findings back at milestones where they are unlikely to be rewritten (end of each phase).
- 01:25 judge-1 (steps 2, 3; [j1](r1118/j1.md)): PASS 3, BATCH-FAULT 3, no real FAIL. Systemic: the
  move of rows into rows/*.qb dropped setup rows (r1113 SQ-C2-00 bot spawns, r1115 P3-00/01 MELT builds) and nothing
  installed the KubeJS scratch probes (about 250 planned rows). Tool fix (judge-1): `qrun.py run` copies the probes the
  selected rows use as zz_*.js and reloads once (`--no-probes`). Rows fixed: BBQ2.qb (c2b spawns), BBQF.qb SQ-SALT2-06.
  Rerun at the end: probe rows of steps 1 to 6. rowfix-1 started on the 20 other bot rows and a machine-readable
  section prep key (before-boot setup the driver ignores). MELT rows deferred (MELT.qb has another session's
  uncommitted work).
- 01:50 rowfix-1 ([rowfix1](r1118/rowfix1.md)): 81 rows in 20 .qb files spawn their own bot
  (`tools/scratch_query/rowfix_spawns.py`); new section key `@@prep` (qrows/qdrive/qrun, `qrun.py prep`) filled for 39
  sections; driver STOP at the step 9 boundary to restart with it. Still without setup (rows run, expected
  BATCH-FAULT, listed for the owner): MELT (another session's file), PSU FCB-04/PSF-08 and RD VTD6-02/FDB-05/-11 home
  markers, lost gamerule/season/time setup, multi-boot sections (WO, HH restarts, CBA/CBO/CFR/CGE/TP/PSU, SPF boot 0),
  CCO/CC12 (cc_only_srv.sh), legacy-config fixture sections (CAL, CBJ4, CDJ, CDJ4, CEN, CEQ, CFR, CMP, CS2, CDNS), PWG 2
  ponder datapack.
- 02:05 judge-2 (steps 4 to 8; [j2](r1118/j2.md)): 13 rows, PASS 6, BATCH-FAULT 7, no real FAIL, no
  handler errors in the logs. Row fixes: SQ-BREED-03 (flat setup in group B), SQ-CAL-01, -04/-05/-07 (`/cetime` ->
  `/ccaltime`, also TD, SPF, IDS), `Unknown command` rejects broadened. Tool fix: qrun.py probe wait read the KubeJS
  log instead of latest.log (180 s lost per probe step). Side notes for the owner: Create warns "more inputs/outputs
  than supported" for 9 of our compacting/mixing/vibrating recipes (unchecked whether they work). Rerun list:
  `perf/runs/r1118/judge/rerun.txt`.
- 01:29 driver resumed with `@@prep` (step 1 rerun first).
- 03:10 judge-3 (steps 1, 9 to 17; [j3](r1118/j3.md)): 87 rows, PASS 20, FAIL 2 (real, F1),
  BATCH-FAULT 65 (CBA/CBB/CBC/CBO need port_chain.sh chain or cooking boots and restarts the driver does not do; CBJ4
  legacy fixture lost; 14 rows fixed for rerun). `rowfix_spawns.py` SPEC_J3 adds qa/qe spawns to 21 more rows.
  Duplicate id SQ-AC-02 (AC.qb and SALT.qb).
- 03:20 judge-4 (steps 18 to 30; [j4](r1118/j4.md)): 60 rows, PASS 18, BATCH-FAULT 42 (chain mod
  sets, CC-only sets, legacy fixtures, CDS tag_diff against renamed tags), 0 FAIL, 30 LOOKs wait for reruns; no handler
  errors. 10 rows fixed for rerun. Open tool items: `install_probes` skips probes without a command root; `tag_diff.py`
  needs the 2026-11-09 rename map; SQ-CDJ4-03 and SQ-CDFW-11 to retire or rewrite. status_index rerun 03:22 (only the
  known MELT FAIL left).
- 04:30 judge-6 (steps 44 to 55; [j6](r1118/j6.md)): 46 rows, PASS 26, BATCH-FAULT 17,
  INCONCLUSIVE 2 (SQ-CS-04, SQ-FCK-11), HARNESS 1 (SQ-CS2-06, fixed), 0 FAIL; no handler errors. 12 rows fixed for rerun.
  Power Grid absent from scratch mods/ is by design (only via `PGJAR`, set in the PWG sections' `@@prep`). Tool item
  for judge-5: install_probes should wait for the reload's `Added N recipes` line, not the scripts-loaded line.
- 04:40 judge-5 (steps 31 to 43; [j5](r1118/j5.md)): 60 rows, PASS 28, FAIL 1 (real, F7),
  BATCH-FAULT 31 (17 fixed for rerun; legacy fixtures, cge2_run.sh boots b2 to b5, chain and Common-alone sets not);
  no handler errors. Tool fixes: install_probes also installs command-less probes (picked by file name or a log tag
  only that probe prints); new lint warning `trail-space` (qrun strips a check's trailing space; 264 check lines in 34
  .qb files end in one: owner decision, parsing unchanged mid-run). SQ-CMV-04 cannot pass in a full pack (advancements
  dropped). status_index rerun 04:42.
- 07:50 judge-8 (steps 56 to 64, 68 to 80; [j8](r1118/j8.md)): 70 rows, PASS 30, BATCH-FAULT
  40, 0 FAIL; no handler errors. 20 rows fixed in 15 .qb files. Tool items sent to judge-7 (kitchen probe loads removed
  `BAConfig`; raw `{CAP}` ends FAIL not HARNESS; probe-install reload warnings inside the first row's window). For the
  MELT.qb owner: P1/P1b put snow layers on ice (vanilla never keeps them); MELT-09/-09b rejects need to skip KubeJS
  recipe lines. Owner side notes: rose gold missing from the gold block tag (item tag has it); orphan zucchini_quiche
  transitional item in `cd_transition_items.js`.
- 07:55 judge-7 (steps 65 to 67, HH; [j7](r1118/j7.md)): 92 rows, PASS 58, FAIL 1 (real, F10),
  BATCH-FAULT 29 (fixed; salt lick block is `salt:raw_rock_salt_block` since G2b, section 3 forceload, bot pickups,
  regex/NBT order), INCONCLUSIVE 3, HARNESS 1; known TODO #65 to #69 still hold. Tool fixes: install_probes waits for
  the KubeJS end-of-reload line (first row's window opens after it); an undefined `{NAME}` placeholder ends HARNESS
  and is not sent; kitchen probe reads `CDConfig.CFB`; hh_t12_probe expected 191. Open: 81 `salt:salt_block` lines in
  HH.qb (and SALT, PS15) and SLK-03/-04 (salt dust refills removed in G2b) need rewriting. F10 checked by the
  orchestrator in source and the owner's latest.log (no `GlmMerge` line). status_index rerun 07:56.
- 12:35 judge-11 (steps 98 to 105; [j11](r1118/j11.md)): 65 rows, PASS 32, BATCH-FAULT 18, HARNESS
  13, INCONCLUSIVE 2, no real FAIL; no handler errors. 23 rows fixed (rows carrying their own {H} setup, cup parts kept,
  quiche oven-only, silk_touch_differs, namespaces, VG/VL to group A, NM-02/-03 to worldgen). SALT.qb section 1 is a
  Run log whose entries kept steps since the P2.1 migration, so the driver ran them: 10 marked @superseded. False
  passes SQ-VG-03 (villager suffocated) and SQ-RDB-01 (rustic_upstream folder present) sent to judge-9 with the
  reclassify fix. Driver gap: "old world"/"old save" rows run on a fresh group A world.
- 12:50 judge-10 (steps 81 to 93; [j10](r1118/j10.md)): 47 rows, PASS 14, BATCH-FAULT 32,
  HARNESS 1, no real FAIL; r1115 PTH passes held. New known cause: the .qb move dropped setup-row forceloads (r1115
  pth_a0, r1116 ps15_a; the r1115 box also stopped at z 1111): forceload added in 17 PTH, 8 PS15 and 2 PS15B rows. 25
  rows to rerun. Owner notes: SQ-PG-01b look expects "floor portal false" but portalgun's own value is 1b; SQ-PJL-04
  should allow 10 jam jars with Central Kitchen.
- 13:05 judge-9 (steps 94 to 97, PWG and QN; [j9](r1118/j9.md)): 79 rows, PASS 27, BATCH-FAULT 52,
  0 FAIL. Jar `-20` in every PWG step. The .qb move dropped r1113 pwgr1's world setup (P11A forceloads, gamerules,
  animal clear, SQ-PWG-38B generator line): `tools/scratch_query/rowfix_pwg.py` gives the 71 planned rows of sections
  2 and 3 a self-contained setup; stale level paths and `-17` regexes fixed; SQ-PWG-91 (r1113 FAIL) now PASS. 49 rows
  to rerun. Tool fixes: install every command-registering probe (fungi ztrades/zcount/zgoose); `--reclassify` takes
  PASS -> BATCH-FAULT (SQ-VG-03, SQ-RDB-01 done); unloaded-coordinate lint covers probe and cdevtest commands. Watch on
  rerun: SQ-PWG-31 Power Grid ERROR "Both nodes of a wire must be part of the network" at 1112 -60 1080 (unloaded
  area; a port bug if it recurs in loaded chunks). Perf session r1118 records every boot (judge-9's "no perf session"
  holds only for the PWG perf rows' own p99 reads). Owner note: `power_grid.js` `var ACID = f.acid ?? f.acid` is a
  no-op and its comment is wrong (file has uncommitted owner work).
- 14:20 judge-13 (steps 116 to 125; [j13](r1118/j13.md)): 69 rows, PASS 35, BATCH-FAULT 32,
  INCONCLUSIVE 1 (SQ-WC-05), HARNESS 1 (SQ-WCT-05: `cdevtest rdetail` prints `ingredients=-` for smithing recipes),
  no real FAIL. 18 rows fixed (TPF `@@prep` animals, TX forceload, NS-01 hopper feed, TSK vine age, VS-03 ceiling,
  WO-05 tickwarp 2400). Settle on rerun: SQ-VS-03 (age-0 weeping vine no growth in 90 random ticks; suspect Calendar
  crop seasons), SQ-WCT-03 (well 1000 mB drain on a fill). 17 WO rows need their own boots (A2 to A5, B, S): ran on A1
  defaults, SQ-WO-31/-33 false passes. Owner question: `vintagedelight:salt` missing from plan_post_scratch_1115b.md D1;
  `cdevtest layerdump` marks the six blocks moved out of the pass-through tag on 2026-11-17 as `class=new`.
- 14:40 judge-12 (steps 106 to 115; [j12](r1118/j12.md)): PASS 25, BATCH-FAULT 35, INCONCLUSIVE 2
  (SQ-SPF-11, SQ-NCH-01: rerun), FAIL 1 (real, F12); SPF/SPFX/TP repeats of known-PASS rows failed on row or
  single-boot faults; SY-14 row fix from r1115 lost in the .qb move. Tool fix: ztp_probe.js `/ztp settings` no longer
  throws on getTag. 30 rows to rerun. SQ-TP-04 needs the macro path (cooldown not seen via `/ztp open`).
- 14:45 server phase write-back (milestone): TODO rows conventions #38 (F10, high), terrain-adaptation #1 (F1),
  crops #58 (F12), creative-delight-members #1 (F3 + F9), generators-and-tests #19 (F11); logs on dishes/meals #38 (F7)
  and conventions #26. Checks F2, F4, F5, F6, F8 stay here for the owner. status_index rerun.
- 14:39 drive_watch STALL (step 138, EF worldgen, SQ-EF-11): the row's `> celocate census` hit "Unknown or incomplete
  command" (renamed `cwglocate`), so its 2400 s waitlog idles until about 15:08; left to time out (no mid-row kill).
  Rows fixed: `> celocate` -> `> cwglocate` in EF, IDS, SSP, IP, CWG. Also seen: `place structure
  creativeengineering:meta/house_camp` and others "no structure with type" (stale meta ids; judge to map).
- 15:43 step 143 (HH worldgen) refused to boot: step 142's server (s141, HCS group B) was still up. Its rows had ended
  at 15:32 (SQ-HCS-02, -04 LOOK) but the stop was never read: jstacks (`drive/s141_jstack_*.txt`) show the server thread
  inside a console `forceload add` (HCS.qb lines 86 to 96: six 169-chunk boxes, about 1000 fresh normal-world chunks
  generated on the main thread), which blocks every later console line, `stop` included; SIGTERM also waited on it.
  Killed (-9) 15:46 under the owner's ruling (no row discarded); drive resumed. Not a game hang: BATCH-FAULT in the
  HCS rows (gotcha added).
- 16:25 judge-14 (steps 126 to 142, longwait + worldgen; [j14](r1118/j14.md)): 30 rows, PASS 9,
  BATCH-FAULT 16, no real FAIL. Longwait TODO rows got no usable evidence (EF/TER config switches lived only in
  @commands text): structures/earthfill #13, #15, pack/server #14, flowers #3, creativedelight/members #1,
  structures/terrain #2 stay Testing. Rows fixed (EF/TER `@@prep toml`, meta ids -> `creativeworldgen:meta/`, fresh
  world rows -> group B, `region_blocks.py @` level, HCS-04 waits for its forceloads); 14 to rerun. Tool fixes: a
  repeated waitlog pattern waits for a new line (68 rows; PROC-21b); region_blocks.py `@` level and exit 2 on a
  missing region; qdrive maps new/fresh/normal-world groups to B and runs `bootreject Done` steps (load on driver
  restart). Open: about 130 stale `creativeengineering:meta/` lines in SB, PSU, SPF, CWG, RD, WGE, META31, SSP, SALT,
  WG30.
- 16:50 rowfix-2: 161 stale `creativeengineering:` worldgen ids -> `creativeworldgen:` in 10 .qb files
  (`tools/scratch_query/rowfix_meta_ids.py`, idempotent; targets checked against the worldgen data, not the dumps:
  `kubejs/exported` predates the 2026-11-09 move). 30 rows to rerun. The rerun pass starts with `/dumpall` (rows such
  as SQ-CWG-07 read the dumps).
- 16:43 drive_watch STALL (step 149 MIN, SQ-CDW-02): not hung; the row's `cdevtest count` box (160 x 110 x 160) is over
  the verb's 262144-block cap (`error="box over 262144 blocks"`), so two 600 s waitlogs idle. Row fixed for the rerun:
  count and forceload boxes +-24, waitlogs also end on `error=`.
- 16:59 owner: wrap up. `qrun.py drive stop r1118`; the driver stopped after SQ-NSM's first row (step 150 of 167),
  no JVM left. Perf session r1118 stopped and reported (`perf/runs/r1118/perf_report.txt`). Verdict table, judge
  reports and the rerun list copied into [r1118/](r1118/) (perf/runs is not tracked). Handover: section 10.

## 8. Final verdicts

Full table (799 row results over 150 steps, `qrun.py table` of every `sNN_res`, judged verdicts included):
[r1118/verdicts.md](r1118/verdicts.md). Counts: PASS 330, BATCH-FAULT 420, FAIL 15, HARNESS 19, INCONCLUSIVE 10,
LOOK 5. Judge reports per batch: [r1118/](r1118/) (j1 to j14, rowfix1; BRIEF.md is the judge brief).

Judged real FAILs (5 rows, 4 findings): SQ-CBA-18, SQ-CBC-04 (F1), SQ-CM-04 (F7), SQ-HH-1269 (F10), SQ-SY-21 (F12).

Not judged (steps 143 to 150, finished after the last judge round; raw qrun verdicts): FAIL SQ-HH-319, -320, -328,
SQ-HR-05, SQ-IDS-02, SQ-META31-08, SQ-CDW-01, -02 (row fault: count box over the cdevtest cap, fixed), -03, -03b;
LOOK SQ-IWF-03, SQ-META31-01, SQ-MIN-01 (tag now `creativecalendar:seasons/arid`), SQ-MIN-07, SQ-NSM-06.

Not run (stopped by the owner 16:59): steps 151 to 167 (NSM 2nd row, PS15, PSU, PWG, RD, RDB, SALT, SB x2, SPFX, SSP,
TSK, UC, VH 10 rows: worldgen; EF, GFB, RD, SSP: long) and the rerun pass of `r1118/rerun.txt`.

## 9. Findings routing

| Finding | Rows | Route |
|---|---|---|
| F1: Creative Common `tag_helper.js` `_ents` absent-mods filter treats the 44 structure datapacks ported into `kubejs/data/<ns>` as absent mods and drops about 520 structure tag entries; `creativeworldgen:terrain_adaptation/*` and `template_trim` lose every ported structure (forest_ruins, illager_siege_weapons, eugene, ...); the owner's `logs/kubejs/server.log` shows it too | SQ-CBA-18, SQ-CBC-04 | TODO terrain-adaptation #1; queue row (terrain_adaptation holds `forest_ruins:ruin_1`); Common rebuild |
| F2 (check): Miner's Delight item names untranslated on the server (`/give` feedback shows `item.miners_delight.cave_carrot`); lang only from the donor jar; confirm on a client | judge-4 side note | owner / client row |
| F3: `[mod_names]` in a Creative config still lists `incubation_additions` (now in Creative Delight): WARN at every boot | judge-4 side note | TODO creative-delight-members #1 |
| F4 (note): full-pack failed recipes 14 (r1101) -> 91, all third-party compat recipes for absent mods (Biomes O' Plenty, Dramatic Doors, Create Confectionery) | judge-4 | owner: log noise, optional removal |
| F5 (check): Create warns "more inputs/outputs than supported" for 9 of our compacting/mixing/vibrating recipes | judge-2 | queue row: recipes still work |
| F6 (check): Calendar spring fertile crop bonus measured 1.10 (SQ-CS-03) and 1.07 (SQ-CS-04) against 1.25; small samples, both low | SQ-CS-03, -04 | rerun with a larger sample; then TODO if still low |
| F7: Hearth and Harvest macaroni and cheese and its Slice and Dice copy accept only `farmersdelight:raw_pasta`, not `#forge:pasta`; `kubejs/data/creativedelight/kubejs/hearthandharvest/recipes.js:65` uses `i.raw_pasta` for `i.pasta` | SQ-CM-04 | logged on TODO dishes/meals #38; Delight rebuild |
| F8 (check): rose gold block missing from the gold block tag (only the item tag has it) | judge-8 side note | owner |
| F9: orphan transitional item `zucchini_quiche` still registered in `cd_transition_items.js` after the quiche became a baking recipe | judge-8 side note | TODO creative-delight-members #1 |
| F10 (high): Creative Loot's `LootModifierManagerMixin` targets `m_5787_(Ljava/util/Map;...)V`, which in Forge 43.3.13 is only the bridge `(Ljava/lang/Object;...)`; the injector matches nothing and fails silently (no `defaultRequire`), so no jar's `glm_entries.json` loot modifiers run since commit 89a25077 (2026-11-17): Hearth pitchfork straw and corn husk, Brewin', Ends, Rustic, Veggies, Vintage, Vinery, Exposure. Fix: target `apply`, add `"injectors": {"defaultRequire": 1}` to `creativeloot.mixins.json` | SQ-HH-1269 | TODO conventions #38 (high); Loot rebuild + rerun SQ-HH-1269 |
| F11 (tool): Creative Devtools `cdevtest rdetail` prints `ingredients=-` for smithing and IE sawmill recipes | SQ-WCT-05, judge-8 | TODO generators-and-tests #19 |
| F12: Botany Pots growth times back at mod defaults (wheat on dirt 1200, want 9600; Fertile Soil 999, want 8000; Rich Soil Farmland 960, want 8727); `botany_pots.js` logs 3 soils / 175 crops scaled but the loaded recipes keep defaults; regression since r1115, suspects NCH.scaleRecipeInt / setRecipeNumber (neoCraftHelper.js 902 to 930) after the move into Compat (91d2ffb5, 2026-11-17) | SQ-SY-21 | TODO crops #58 |

## 10. Handover for analysis (2026-11-18 17:10)

For the session that analyzes this run. Nothing is booted; RUNNER.md idle; perf session r1118 closed (summary:
`perf/sessions/r1118/summary.md`, recordings kept, no `--prune`).

1. **Real bugs (section 9, TODO rows written 14:45):** F10 `conventions #38` (high: no jar GLM entries run, Creative
   Loot mixin target miss, since 2026-11-17), F1 `terrain-adaptation #1`, F12 `crops #58`, F7 logged on
   `dishes/meals #38`, F3 + F9 `creative-delight-members #1`, F11 `generators-and-tests #19`. Checks for the owner
   with no TODO row: F2, F4, F5, F6, F8. Nothing was fixed in game code (owner ruling: discovery, not fixing).
2. **Judge the 15 unjudged rows** (section 8) from `perf/runs/r1118/s142_res` .. `s149_res`: method in
   [r1118/BRIEF.md](r1118/BRIEF.md). The HH salt cave / corn maze rows (319, 320, 328) and SQ-HR-05 wild raspberries
   are the likeliest real findings.
3. **Main result: most FAILs were row faults (420 BATCH-FAULT), not game bugs.** The 2026-11-16 move of the queue into
   `rows/*.qb` dropped every setup row: bot spawns, forceloads, home-marker captures, world builds, gamerules,
   config switches. Restored per row this run (`tools/scratch_query/rowfix_spawns.py`, `rowfix_pwg.py`,
   `rowfix_meta_ids.py`, the `@@prep` section key, hand fixes by each judge). Section 7 has the cause per batch.
4. **Rerun pass:** [r1118/rerun.txt](r1118/rerun.txt) (about 333 fixed row ids, one line per judge). Before it:
   `/dumpall` (the `kubejs/exported` dumps predate the 2026-11-09 id move), and start a fresh driver
   (`qrun.py drive start RUN --rows ...` or a `plan --json` filtered to the list) so the qdrive fixes load (group
   mapping, `bootreject Done` steps). Then steps 151 to 167 (`qrun.py drive resume r1118`).
5. **Still unrunnable by the driver (need owner or tool work):** multi-boot sections (WO A2 to A5/B/S, HH restarts,
   CBA/CBO/CFR/CGE/TP/PSU, SPF boot 0, PWG-20/-22/-54r, RDB-01/-06, PTH-07, PG-01b); chain or reduced mod sets
   (port_chain.sh, cc_only_srv.sh: CBA/CBB/CBC/CBO/CBP, CC/CC12/CCO, CMV, CIF-04, CPP-04/05, ENGM-05, DEP-02, HOP-01);
   old-save and legacy-config fixtures (NSM, CBJ4 (retire?), CEN-02, CMG-02, CFR-03/04, CMP-02, CDJ4-02, CDJ5,
   GLD-16, FLR-06, HOP-11, HH-1546b); MELT (MELT.qb had another session's uncommitted work: P1/P1b snow on ice, lost
   builds, SQ-MELT-08b without steps); salt rows on `salt:salt_block` (81 lines in HH.qb, SALT, PS15; SLK-03/-04 test
   refills G2b removed). A row-level `@prep` that splits a step would cover most of the first group.
6. **Owner decisions:** 264 check lines in 34 .qb files end in a space that qrun strips (`trail-space` lint);
   duplicate id SQ-AC-02 (AC.qb and SALT.qb; SALT records the dragon set at 33 armor, AC expects 36); SQ-CDJ4-03 and
   SQ-CDFW-11 retire or rewrite; `tag_diff.py` rename map for 2026-11-09; WO section split per boot; SQ-CMV-04 cannot
   pass in a full pack (advancements dropped).
7. **Tool changes this run** (all in `tools/scratch_query/` unless named; committed): qdrive boot pipe hang
   (and `scratch_server.sh` feeder stderr), qpin bare-id rows, probe install (every probe a row names, wait for the
   end of the reload, first row's window after it), `@@prep` (qrows/qdrive/qrun, `qrun.py prep`), undefined
   `{NAME}` -> HARNESS, `--reclassify` of a PASS, repeated waitlog waits for a new line, lint `trail-space` and
   unloaded-coordinate warnings, region_blocks.py `@` level, `drive_watch.py` hang poll (owner request), kitchen and
   Tempad probes. New gotchas in `.claude/skills/scratch-test/gotchas.md` (r1118).
8. **Not done in this run:** feature writeups' test lists and README status words from these results (only the
   generated status was refreshed); no changelog bullet (tooling and test rows only, nothing player-visible changed).
