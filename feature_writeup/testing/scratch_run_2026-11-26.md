# Scratch run 2026-11-26: r1126q, the scratch queue in priority order

Owner request (chat 2026-11-26, via the r1126q orchestrator): run the scratch queue in priority order: (1) rewrite
batch-fault rows, (2) write steps for drafts, (3) run never-run and needs-rerun rows, (4) run jar-changed rows ranked by
risk; rows outdated by changed features are not run. Writer agents 1 to 6 rewrite batch-fault, draft, stale and
outdated rows in parallel (files in `perf/runs/r1126q/writer1.csv` .. `writer6.csv`); the runner (runner-1) does not
edit those files. Triage per id: `perf/runs/r1126q/triage.json` (`tools/scratch_query/qtriage.py`).

## 0. Performance capture

Perf session `r1126q` (systemd user scope `r1126q-perf`), JFR on for every boot, report without `--prune` after the
last phase.

## 1. Scope

- Taken, phase R1 (now): the 110 server/worldgen rows of `perf/runs/r1126q/r1_ids.txt`, never-run and needs-rerun rows
  in rows files no writer owns. `qrerun.py` plan: 105 rows in 46 sections (`perf/runs/r1126q/r1_plan.json`); left out as
  drafts (no steps): SQ-SHB-04, -06, -11, SQ-TN-11, -12.
- Phase R2 (canceled 2026-11-26 by the owner wind-down; was planned): the writer-rewritten rows, the remaining never-run and needs-rerun rows, and jar-changed rows with
  qtriage risk >= 40.
- Not taken: rows outdated by changed features (writers rewrite them first), client rows.

## 2. Tools

`qrun.py drive` (qdrive.py) as user unit `r1126q-drive` (`perf/runs/r1126q/start_drive.sh`), `qprescreen.py`,
`qrerun.py`, `qrun.py judge` / `table`, judges of type `scratch-judge` per RES dir.

## 3. Jars

Installed jars, `NOSWAP=1` (`perf/runs/r1126q/env.sh`); DROPJARS = the 4 jars without a mod_sides.md row
(chestbubblesrevived, kleeslabs, numarevive, prettybeaches), as r1126gp.

| Jar | `mods/` stamp | Note |
|---|---|---|
| creativecommon-1.3.0 | 1.3.0+ca3a3fa7b.2611260131 | |
| creativecompat-1.0.0 | 1.0.0+ca3a3fa7b.2611260131 | |
| solRaspberryPie-1.0.0 | 1.0.0+ca3a3fa7b.2611260131 | |
| creativedelight-1.1.0 | 1.1.0+edc3667aa.dirty-65ca41.2611260108 | |
| creativebrewing-1.0.0 | 1.0.0+ac5d4ff23.2611232324 | |
| creativeengineering-2.0.0 | 2.0.0+049a239c1.dirty-0fb3d8.2611252131 | |
| creativedevtools-1.0.0 | 1.0.0+8321c7e04.dirty-9c6649.2611252043 | |
| creativecalendar-1.0.0 | 1.0.0+5729e53f7.2611251113 | build 1.0.0+edc3667aa.2611260108 pending, not used |
| creativeloot-1.0.0 | 1.0.0+16c9e8ace.dirty-68c76a.2611240102 | build 1.0.0+edc3667aa.2611260108 pending, not used |
| creativeequipment-1.0.0 | 1.0.0+933fe3b20.dirty-0fe1fd.2611231840 | |
| creativemagic-1.0.0 | 1.0.0+c9668bef.2611221203 | |
| creativeLabels-1.0.1 | 1.0.1+c9668bef.2611221203 | |
| creativeworldgen-1.0.0 | 1.0.0+e6004091.2611221216 | |
| exposureBackport-1.9.19-ce-19.2-04 | 1.9.19-ce-19.2-04+649f213d.dirty-4c0aad.2611211240 | |
| backportedYuushyaModelling-2.4.1-ce-19.2-02 | 2.4.1-ce-19.2-02+dbc3e8405.dirty-745b93.2611252136 | |
| backportedTempad-3.0.4-ce-19.2-01 | 3.0.4-ce-19.2-01+fb231ec9.2611190230 | |
| backportedPowerGrid-0.6.2-ce-19.2-20 | 0.6.2-ce-19.2-20+39141b4e.dirty-3b3927.2611181941 | |

## 4. Order

Drive order from the rows' phases: priority (PRI) -> server steps -> worldgen steps (fresh world per group) -> long
(SQ-SCAT-08). Estimate: about 4 h for R1 (FGD Chunky squares up to 90 min each).

## 5. Row map (R1)

| Rows file and section | Phase | Rows |
|---|---|---|
| PRI 1 | priority | SQ-PRI-01, SQ-PRI-02, SQ-PRI-03 |
| AR 1 | server | SQ-AR-01 |
| BRA 1 | server | SQ-BRA-02, SQ-BRA-03 |
| BRA 2 | server | SQ-BRA-07 |
| CAL 1 | server | SQ-CAL-02, SQ-CAL-03 |
| CBW 1 | server | SQ-CBW-02, SQ-CBW-03 |
| CDFW 1 | server | SQ-CDFW-07 |
| CDV 1 | server | SQ-CDV-04 |
| CFS 1 | server | SQ-CFS-03 |
| CMV 1 | server | SQ-CMV-02 |
| ENC 1 | server | SQ-ENC-03, SQ-ENC-04 |
| GF31 1 | server | SQ-TL-09 |
| GSH 2 | server | SQ-GSH-08 |
| HT 1 | server | SQ-HT-02 |
| IP 3 | server | SQ-IP-19, SQ-IP-20, SQ-IP-21 |
| IPO 1 | server | SQ-IPO-01, SQ-IPO-02 |
| JFP 1 | server | SQ-JFP-01, SQ-JFP-02, SQ-JFP-03, SQ-JFP-04, SQ-JFP-05 |
| LGM 1 | server | SQ-LGM-07 |
| MND 1 | server | SQ-MND-18 |
| MXS 1 | server | SQ-MXS-03 |
| OB 1 | server | SQ-OB-03, SQ-OB-04, SQ-OB-06, SQ-OB-05, SQ-OB-07, SQ-OB-08, SQ-OB-10 |
| OR1120 1 | server | SQ-JC-01, SQ-TL-10, SQ-NCH-02 |
| OR1120 3 | server | SQ-HTD-01, SQ-HTD-02, SQ-DA-01, SQ-DA-02, SQ-DA-03 |
| OR1120 4 | server | SQ-CP-02, SQ-CP-03, SQ-FF-15, SQ-CDFL-05 |
| OR1120 5 | server | SQ-CP-04 |
| OR1120 6 | server | SQ-GR-45 |
| POT 1 | server | SQ-POT-02, SQ-POT-04, SQ-POT-04v |
| RW 1 | server | SQ-RW-24 |
| SG 1 | server | SQ-SG-02, SQ-SG-03 |
| SHB 1 | server | SQ-SHB-01, SQ-SHB-02, SQ-SHB-03, SQ-SHB-05 |
| SHB 2 | server | SQ-SHB-07, SQ-SHB-08, SQ-SHB-09, SQ-SHB-10 |
| STD 2 | server | SQ-STD-04, SQ-STD-05, SQ-STD-06, SQ-STD-07, SQ-STD-08 |
| TD 1 | server | SQ-TD-04 |
| TSK 1 | server | SQ-NS-01 |
| WT 1 | server | SQ-WT-02 |
| WT 2 | server | SQ-WT-04 |
| FGD 1 | worldgen | SQ-FGD-12m, SQ-FGD-15c |
| FTW 1 | worldgen | SQ-FTW-01, SQ-FTW-02, SQ-FTW-04, SQ-FTW-05, SQ-FTW-06 |
| IP 1 | worldgen | SQ-TIE-01 |
| OB 1 | worldgen | SQ-OB-01 |
| RING 1 | worldgen | SQ-RING-01, SQ-RING-02, SQ-RING-03 |
| SCAT 1 | worldgen | SQ-SCAT-07, SQ-SCAT-06 |
| ST 1 | worldgen | SQ-ST-02 |
| SVA 1 | worldgen | SQ-SVA-02, SQ-SVA-03, SQ-SVA-04 |
| TER 1 | worldgen | SQ-TER-02, SQ-TER-03, SQ-TER-04, SQ-TER-07 |
| UC 1 | worldgen | SQ-UC-02 |
| VH 1 | worldgen | SQ-VH-01, SQ-VH-10, SQ-VH-12, SQ-VH-13 |
| SCAT 1 | long | SQ-SCAT-08 |

## 6. After the run

Judge per RES dir (`scratch-judge`), BF fixes rerun as `r1126qr`; R2 plan by the orchestrator; write-back (queue Run
log, writeups, TODO rows) after R2.

## 7. Run notes

### 7.1 R1 preflight (2026-11-26 02:06 to 02:13)

- 02:06 RUNNER.md claimed (run r1126q, runner-1). `game_jvms.sh` empty; 72 GB free on /.
- `check_all.py --fast`: 47 run, 2 FAIL, neither a boot risk: test_prefix_lint (29 `RP-n` / `CC-n` ids in
  `project_pages/screenshots.md`, commit 5b7e8f856, outside their owner docs) and test_queue_rows (rows files edited by
  the writer agents before `status_index.py` re-rendered them). Left to the orchestrator: writers own those files.
- `jarstamp.py pending`: Calendar and Loot edc3667aa built, not installed; the run uses the installed jars (NOSWAP).
- Queue header: no pinned owner notes. Latest run doc (scratch_run_2026-11-24.md 10): DROPJARS for 4 unlisted jars;
  Creative Delight boots again since ce1c1a033 (mod-integration #27 fixed), so no OVERRIDEJARS.
- Pre-screen (`perf/runs/r1126q/prescreen.txt`): 16 likely findings; 6 were real and fixed (run default 11), 10 false
  positives (bot or locate-loaded coordinates, probe arguments read as coordinates, a reg regex prefix):
  - BF-FIX SQ-IP-19: basin loaded only by SQ-IP-18's forceload -> row forceloads it itself.
  - BF-FIX SQ-SHB-10: z 630 outside section 2's forceload (z to 620) -> row forceloads its spot.
  - BF-FIX SQ-SVA-02: keg and press counts read to x 420, forceload to x 400 -> forceload to x 420.
  - BF-FIX SQ-OB-06: `corn_delight:corn_cob` never existed -> `creativedelight:corncob` (dumps).
  - BF-FIX SQ-PRI-01: `vintagedelight:green_bell_pepper` not registered -> `rusticdelight:bell_pepper_green`
    (pizza topping tag dump).
  - 36 stale rows (sources changed after `@reviewed`): not reviewed before the run (they are in R1 by the
    orchestrator's triage); a FAIL on a stale row is judged against the current writeup.

### 7.2 R1 execution

- 02:14 perf session r1126q open (user scope `r1126q-perf`); 02:13 drive started as user unit `r1126q-drive`: 51 steps, 115 rows (lower-boot rows brought along), driver ETA 14:50.
- 02:13 to 02:31 steps 1 to 7 (PRI, AR, BRA 1 and 2, CAL, CBW, CDFW), about 2 min per step. Judge 1 (steps 1 to 7):
  PASS 6 (SQ-PRI-02, SQ-BRA-02, SQ-CAL-02, -03, SQ-CBW-02, -03), FAIL 2 known product (SQ-BRA-03 = blockrunner #20,
  346 walkable untiered blocks; SQ-BRA-07 = blockrunner #19, mosaic masonry bonds "has no modifier"), BATCH-FAULT 3.
  - BF-FIX SQ-PRI-01: tag holds cooked_beef / cooked_mutton (docs) but expects wanted raw ids; Brewin' kimchi removed
    (merges/dishes #9) -> vintagedelight:kimchi; `makes supreme_pizza` -> `makes pizza`; stub check reads comment-only
    lines; rerun queued.
  - BF-FIX SQ-PRI-03: `bootreject (?i)jrftl` matched the deliberate `[reg] item jrftl:prepared_flesh` line -> narrowed
    to `(?i)(ERROR|Exception).*jrftl` (as SQ-JRFTL-01); rerun queued.
  - BF-FIX SQ-CDFW-07: `uses` prints no yields -> 4 `rdetail` steps added; rerun queued (LOOK until then).
  - SQ-AR-01 BATCH-FAULT, not simple: IE blueprint `rdetail` prints `ingredients=-`, so the repeated-input check needs
    a new probe or a headless NCH check; `rdetail noteblock` id is note_block. Left for a rewrite pass.
- 02:31 to 02:46 steps 8 to 15 (CDV, CFS, CMV B, ENC A and B, GF31, GSH 2, HT). Judge 2: PASS 6 (SQ-CDV-04, SQ-CFS-03
  skillet half only, keg half needs the client, SQ-CMV-02, SQ-ENC-03, -04, SQ-GSH-08), BATCH-FAULT 2, no product FAIL.
  - BF-FIX SQ-TL-09: the scratch srv has no `tools/generators.json` -> the row copies it in before `/cdevgen`; pass
    count 50 -> 63 (manifest); rerun queued.
  - SQ-HT-02 BATCH-FAULT, not simple: summoned horses vanish (srv `spawn-animals=false`); the fix is
    `@@prep A: spawn-animals=true` on HT.qb section 1, shared with SQ-HT-01 and -03 -> for R2.
  - Doc note: SQ-ENC-03 pass text says the trades.js removal count is at least 6; the run logged 5 with zero book
    listings in the dump (behavior holds; the row text or the animal-tamer count needs a check).
- 02:46 to 03:36 steps 16 to 25 (IP 3, IPO, JFP, LGM, MND, MXS, OB B, OR1120 1, 3, 4). Judge 3: PASS 19, FAIL 6,
  BATCH-FAULT 7.
  - FAIL SQ-JFP-01 to -04 (known, TODO jars #15): the jar pump moves no fluid (jar FluidHolder empty, tank stays 4000,
    pump 64 RPM). SQ-JFP-05 PASS is vacuous until that is fixed.
  - FAIL SQ-LGM-07 (known, TODO mod-updates #1): a full omurice block drops the block and a bowl (2 stacks, row wants 1).
  - FAIL SQ-MND-18: both potted-gossypium checks failed, one gossypium item killed afterward; cause unproven ->
    diagnostics added, rerun queued before a verdict.
  - New finding: every boot logs `[Server thread/ERROR] Adding duplicate key
    'ResourceKey[moonlight:soft_fluids / creativedelight:bone_broth]' to registry` (s20, s21; also r1123); suspected the
    bone broth soft fluid registered twice by Creative Delight. Logged on TODO dishes/ingredients #36 (bone broth fluid, Testing).
  - BF-FIX SQ-IPO-01: stale 770,959 baseline in the shell guard -> guards only the 2^20 ceiling; rerun queued.
  - BF-FIX SQ-OB-05: ids are `veggiesdelight:*_vegetarian_patty` -> regex fixed; rerun queued.
  - BF-FIX SQ-OB-06 (second fault after the pre-run id fix, kept as a diagnostic rerun): outputs never printed ->
    `rdetail` steps added; SQ-OB-10 likewise (ingredients); reruns queued.
  - BF-FIX SQ-TL-10: the summary line is logged at boot, before the row -> `bootexpect`; rerun queued. Its two flagged
    tags: `any_bricks -> #minecraft:bricks` may be a real unknown tag; `no_world_dye` is empty on purpose.
  - BF-FIX SQ-HTD-02: recipe id is `creativedelight:createsequencedassembly/...` -> expect fixed, `rdetail` added; rerun.
  - SQ-IPO-02 BATCH-FAULT, not simple: 2706 -57 is air, no truss formed (needs the Immersive Posts truss mechanics).
  - SQ-DA-02 PASS; its shell grep and pass text now use the `[itemMerges]` count (the code logs counts, not ids).
- 03:36 to 04:06 steps 26 to 35 (OR1120 5, 6, POT, RW, SG, SHB 1, 2, STD B, TD, TSK). Judge 4: PASS 10, FAIL 6,
  BATCH-FAULT 5, LOOK 2 (owner JEI: SQ-CP-04, SQ-GR-45). SQ-SHB-10 PASS with the pre-run forceload.
  - FAIL SQ-NS-01 (known, TODO decor-and-framed-blocks #3): nightstand and hoppers stay empty, as r1123cx.
  - FAIL SQ-POT-02 (known, decor-and-framed-blocks #1): framed pot takes azalea leaves, moss, pumpkin, melon,
    hamimelon; Supplementaries hanging pot accepts 0 of 555.
  - FAIL SQ-POT-04, -04v (known, framed-and-decor #6): every holder drops `[]` on the second `/pottest` pass, vanilla
    pot included (as r1124fx); cause unproven (probe or product). POT-04v batch ran late after a 100 s server idle
    (03:41:55 to 03:43:36), not what failed it.
  - FAIL SQ-SHB-08, -09 (new, TODO mod-integration #44): rabbit trap never catches (AloneSince ticks, no
    CaughtEntity after 20 s); ocelot and duck traps empty. Suspected `RabbitTrapBlockEntity.lure()`.
  - BF-FIX SQ-SHB-03: expect/reject read the whole row log -> two expects pinned to each read's Items list; rerun.
  - BF-FIX SQ-STD-05 to -08: rdetail ids `creativeengineering:shape...` -> `storagedelight:shape...`
    (storage_delight.js); reruns queued (their looks unjudged until then).
- 04:06 to 04:13 steps 36, 37 (WT 1, 2); 04:13 step 38 (FGD 1, group B fresh world, SQ-FGD-12m, -15c).
- Environment fault (run default 1), found by the orchestrator at 10:15, missed by this runner (the step wait had no
  stall check; 08:47 to 10:15 without log writes): every `chunky ...` line on step 38 answered "Unknown or incomplete
  command" (04:16:30 on), so each `waitlog Task finished 5400` ran out its cap: SQ-FGD-12m FAIL on timeouts, -15c
  waiting on its third square. Cause: Chunky-1.3.38 loaded (debug.log), but qrun's probe install copied
  `zq_biomegrid.js` after Done and sent `/reload` (04:16:01 "Reloading!"), which drops Chunky's commands for the rest of
  the boot (gotchas.md "Configs and /reload", r1103w). Fix (tools/, run default 2): `qdrive.py` `preinstall_probes`
  copies the step's probes into `srv/kubejs/server_scripts` before the boot, so qrun finds them in place and sends no
  /reload. SQ-FGD-12m reclassified BATCH-FAULT (environment). 10:16 drive stopped at the row boundary; 10:17 resumed
  (user unit `r1126q-drive2`), step 38 reruns both rows on a fresh prep. Remaining chunky steps (FTW, TER) get the
  same fix. New stall watch: `tools/scratch_query/drive_stallwait.sh RUN [UNTIL] [MIN]` (returns on a step change, a
  stopped driver, or 10 min without a server or row log write), used for the rest of R1.
- 10:17 to 10:19 resumed step 38 booting (prep log: `probes copied before boot: zq_biomegrid.js`, so the fix is in
  place) when the orchestrator relayed the owner's wind-down (R2 canceled): `drive stop` during the boot ended step 38
  "failed: server not ready" (no row was running); game_jvms.sh empty 10:19.
- 10:20 judged steps 36, 37 by hand: SQ-WT-02 PASS (pool_1 196, 16 agreements, pool_2 0); SQ-WT-04 FAIL: offers 31
  with the pack values (row 30) and 7 with `enabled = false` (row 6), one extra offer both times, not analyzed; logged
  on TODO creative-common-publish #19; its whole-log reject also matched a boot-time KubeJS recipe line (row text).
- 10:25 perf session r1126q stopped and reported (no `--prune`): `perf/sessions/r1126q/summary.md`.
- Deferred (owner wind-down 2026-11-26), resumable with `qrun.py drive resume r1126q` (source
  `perf/runs/r1126q/env.sh` first, or run `perf/runs/r1126q/resume_drive.sh` as a user unit): step 38 FGD 1 B
  (SQ-FGD-12m, -15c), 39 FTW 1 B, 40 FTW 1 A, 41 IP 1 B, 42 OB 1 B, 43 RING 1, 44 SCAT 1 B, 45 ST 1, 46 SVA 1
  (SQ-SVA-02 pre-run forceload fix untested), 47 TER 1, 48 UC 1 B, 49 VH 1 B (2 boots), 50 VH 1 A, 51 SCAT 1 B long
  (SQ-SCAT-08). Then the rerun list: `qrerun.py perf/runs/r1126q/rerun.txt` -> `qrun.py drive start r1126qr
  --plan-json perf/runs/r1126q/rerun.plan.json` (17 ids).

### 7.3 R1 resume, reruns and R2 (main session as orchestrator, from 2026-11-26 10:50)

Owner request (chat 2026-11-26 10:50): execute this run doc, R1 rest + rerun list + R2 (R2 cancellation lifted),
subagents for judging, spread over up to 72 h. Jars: Calendar and Loot 235159e21 installed first (owner); Common and
Compat rebuilt as 235159e21 by the same Gradle call but with no source change since ca3a3fa7b, not installed.

- 10:50 `installToMods` refused on check_all FAIL spelling_docs (this doc, `armoured` fixed) and test_prefix_lint (29
  ids in `project_pages/screenshots.md`, known since 7.1, no boot risk); installed with `-PskipChecks` (logged).
  creativecalendar 1.0.0+235159e21.2611261028, creativeloot 1.0.0+235159e21.2611261028.
- 10:52 RUNNER.md claimed. game_jvms.sh empty; 60 GB free. Pre-screen of the 14 remaining steps: 12 likely, the same
  rows R1 preflight read as false positives (Chunky / tp / locate-loaded coordinates); VH dead ids left to the judge.
- 10:54 perf session r1126q2 open (user unit `r1126q2-perf`); 10:55 drive resumed as user unit `r1126q-drive3`
  (`--same-dir`; the first try without it exited 127), step 38 FGD 1 B; driver ETA 19:32.
- 12:21 drive done, steps 38 to 51 (51/51), 38 rows: raw PASS 5, FAIL 6, LOOK 26, BATCH-FAULT 1 (old FGD-12m mark).
  Step 38 (FGD) took about 10 min against up to 90 min per Chunky square: checked by judge-A. game_jvms.sh empty.
- 12:25 judges A (s37 to s39: FGD, FTW, TIE), B (s40 to s47, s50: IP, OB, RING, SCAT, ST, SVA, TER, UC) and C (s48,
  s49: VH) started; .qb owners as listed.
- judge-C done (VH, s48/s49): PASS 11, BATCH-FAULT 2, FAIL 1. BF-FIX SQ-VH-07: forceload over the 256-chunk cap and
  count boxes over 262144 blocks -> forceload split in two halves, counts narrowed to H +/-22; rerun queued. BF-FIX
  SQ-VH-13: `uses` has no `max=` -> removed; rerun queued (iecrushing secondary not printed by rdetail, may stay LOOK).
  SQ-VH-06c FAIL: flesh in crimson_forest 1204 / warped_forest 1905 (interior 0 / 254), the known Heap spill
  (visceral_heap.md r1115 note).
- judge-B done (s40 to s47, s50): PASS 8, FAIL 1, BATCH-FAULT 3, LOOK 5 owner (SQ-SVA-02 fly-by part, -04, SQ-TER-02,
  -03, -04). BF-FIX SQ-SVA-03: y bands 40 high = 262440 blocks over the cap -> bands 39 high; rerun queued. BF-FIX
  SQ-UC-02: bot `qa` never spawned in the step -> `pbot spawn qa` added as in SQ-UC-03; rerun queued. SQ-TIE-01
  BATCH-FAULT, not simple: baseline perf/wgp/wgp_new (2026-10-27) predates the creativeworldgen namespace move, needs a
  fresh baseline (real checks passed). SQ-TER-07 FAIL: `[absent mods] skipped 63 tag entry rows for absent
  forest_ruins` (also illager_siege_weapons 84, eugene 59), the worldgen-tag keep path (`global.has.datapack`,
  tag_helper.js) does not fire on the installed Common.
- judge-A done (s37 to s39): PASS 5 (SQ-FTW-01, -02, -04, -05, -06), BATCH-FAULT 2. Chunky ran (3 "Task finished", 0
  unknown command): the preinstall_probes fix works. The resumed step 38 ran only SQ-FGD-15c (SQ-FGD-12m kept its
  first-attempt result; it is in rerun.txt). SQ-FGD-15c BATCH-FAULT, not simple: "Could not spread 24 entities", only
  24 of 60 summoned cows tagged, so the `@e[tag=fgd15mf]` select missed the mooshrooms (FGD.qb summon/tag steps);
  product half unjudged. SQ-TIE-01 (judged by A and B alike) BATCH-FAULT: stale wgp_new baseline. SQ-FTW-02 PASS with a
  note: moss carpet 26 and glow berries 13 exceed the literal "each under 10" (perf.md 10.9 accepted 14).
- 12:25 rerun r1126qr: rerun.txt 21 ids + SQ-VH-04 (captures {H1} for SQ-VH-07, pre-screen `likely var`) = 22 rows in
  14 steps (`perf/runs/r1126q/rerun.plan.json`), pre-screen 5 likely = the known false-positive kinds; drive started
  as user unit `r1126qr-drive` (`perf/runs/r1126q/start_rerun.sh`, env.sh), driver ETA 14:42.
- 13:10 r1126qr done (15 steps, 22 rows); judge-R: PASS 15, FAIL 1, BATCH-FAULT 6 (all second faults after a BF-FIX,
  not simple, no further fix). PASS: SQ-PRI-01, -03, SQ-CDFW-07, SQ-TL-09, -10, SQ-IPO-01, SQ-HTD-02, SQ-OB-05, -06,
  SQ-STD-05 to -08, SQ-VH-04, -13. Second faults: SQ-OB-10 (no bot ob8 on the fresh boot), SQ-SHB-03 (604 600 not
  loaded), SQ-VH-07 (bone_block 0 in the narrowed box; shell grep matches boot recipe-parse WARNs), SQ-SVA-03 (village 1
  counts 0, villages 2 to 5 only located), SQ-UC-02 (randomtick block_ticks=0), SQ-FGD-12m (Chunky fix works, 441
  chunks in 42 s; the group B seed has only a speck of mushroom_fields, row needs its own seed). SQ-MND-18 FAIL
  (unproven): the potted gossypium break dropped the plant but no flower_pot (only item entity in the world); suspect
  kubejs/data/miners_delight/loot_tables/blocks/potted_gossypium.json, or the 1-block pillar row geometry.
- 13:15 R2 planned: r2a 425 rows / 121 steps (drafts SQ-WGP-01, -02 left out), r2b 317 rows / 67 steps (drafts
  SQ-SALT-02, -04 left out), no client rows. Pre-screen (`perf/runs/r1126q/prescreen_r2a.txt`, `prescreen_r2b.txt`):
  r2a 38 likely on 29 rows, r2b 27 likely on 25 rows, 266 stale rows in r2b.
- 13:20 to 13:30 R2 pre-screen fixes (fixer-P1 on WGE, PTH, SPF, SB, HH, ENGM, CC, CCO, BBQ2; fixer-P2 on the other
  files), 12 real of 73 likely, the rest false positives (bot / tp / locate-loaded coordinates, structure and feature
  ids outside the registry dump, prose ids, awk `{exit}`, tags set by `zpth mod`):
  - PRE-FIX SQ-MOB-N5: {PL1}/{PL3} captured in another batch -> row locates plains and captures PL itself.
  - PRE-FIX SQ-MOB-N6: captures PO01.. read as PO0 by qlint -> renamed POA/POB/POC.
  - PRE-FIX SQ-MOB-N8: {S1}/{S3} from another batch -> forceload -8 -8 8 8, summon at 0 120 0.
  - PRE-FIX SQ-HH-466: marker r465 from SQ-HH-465 (boot 1) -> SQ-HH-465 added to r2b (318 rows).
  - PRE-FIX SQ-BM-02: forceload only in SQ-BM-01 (not planned) -> row forceloads 140 495 150 515.
  - PRE-FIX SQ-EF-10: {RV} from SQ-EF-09 (not planned) -> row does the river locate and capture itself.
  - PRE-FIX SQ-PS15B-02: forceload in SQ-PS15B-01 (r2a) while -02 is in r2b -> own forceload.
  - PRE-FIX SQ-CGE-06, -11, -09a, -09b: forge:entity_gravity (attribute, not dumped) -> `@absent`.
  - PRE-FIX SQ-HH-1546c: hearthandharvest:tilling (enchantment, not dumped) -> `@absent`.
  - PRE-FIX SQ-MDP-10: copper cup steps for an item removed 2026-11-02 (owner) -> steps deleted; the row's
    @commands/@pass text still names the cup (row text pass for later).
  Re-screen: r2a 25 likely, r2b 21 likely, all of the false-positive kinds above.
- 13:17 R2a drive r1126q2a started (user unit `r1126q2a-drive`, `perf/runs/r1126q/start_r2a.sh`), driver ETA
  2026-11-28 22:48; perf session r1126q2 still open. 14:13 step 32 of 121.
- judge-2a1 (steps 1 to 31, 60 rows): PASS 49, BATCH-FAULT 10, LOOK 1 (SQ-BBQ2-04 crafting half, client), product
  FAIL 0, no environment fault. BF-FIX (reruns in `perf/runs/r1126q/rerun2.txt`):
  - SQ-BBQ-06: grill progress reset to 0 (rain likely, fresh world) -> `weather clear`, state reads.
  - SQ-BBQ-12: rdetail text never matches -> `rdetail grilling/nether_skewer`.
  - SQ-ROLL-11: roll eaten at food 20 with Nourishment active -> `effect clear` first; if it still eats, product FAIL
    (food.md 3.10 "every golden food always edible" vs roll_delight.md ROLL-25 "refused at full": docs disagree).
  - SQ-BM-02: peaceful discards the baby zombie, cat moved out of a 1x1 box -> difficulty easy, wider boxes.
  - SQ-BREED-03: bees discarded with spawn-animals=false -> `@@prep A: spawn-animals=true` on BREED.qb section 1
    (a section-level prep, outside rule 11's simple list; accepted by the orchestrator, as HT.qb in 7.2).
  - SQ-CBA-03, SQ-CBO-32: two absent mods give two ban INFO lines -> `-le 2` (CBO-32 applied after the CBO steps).
  - SQ-CBB-10: Better Jungle Temples now installed; Corn Delight GLM WARN -> gate regex and reject narrowed.
  - SQ-CBB-11: log tag is `[structure-tuning]` -> grep fixed.
  - SQ-CBM-04: pool 184 / 37 since the 2026-11-20 book trade removal -> regex `common [1-9]\d\d, rare [1-9]\d`.
- 15:19 step 61 of 121; SQ-CBO-32 fix applied by the orchestrator (`-le 2`, @pass "per absent mod").
- judge-2a2 (steps 32 to 60, 42 rows): PASS 33, BATCH-FAULT 6, LOOK 2 left (SQ-CEN-05 carried-chest half needs a
  carried save; SQ-CEQ-04 filling fluid mB not printed by rdetail), product FAIL 0. BF-FIX (rerun2.txt, 16 ids):
  - SQ-DDP-07: cookie stayed in the bot's hand -> `clear` before the sneak half.
  - SQ-CDNS-11: placed state was displaydelight:bnc_vegetable_omelet -> row now holds the inky cap scrambled eggs.
    Orchestrator note: the swap avoids, not explains, Display Delight's own omelet shadowing the Brewin' omelet; check
    at write-back whether that overlap is intended (possible finding).
  - SQ-PS-05: powdered sugar ids moved to creativedelight:. SQ-CM-33: tag 230 (140 + 90 half cabinets).
  - SQ-CSN-02: glass is in silk_touch_differs -> glass_pane. SQ-CCO-08: zombies landed before the reads -> y 1900.
  - Notes: SQ-CBP-01 look text refreshed (other mods' loot ships with the mod since 2026-11-14); SQ-CBP-07 srv
    `[effects] levelCaps` missing from configs_edited/root_config/creativecommon-common.toml (backup drift?);
    SQ-CCO-16 creativeloot toml absent from the srv set, offers vanilla (check at write-back).
- 20:31 step 91 of 121; judge-2a3 (steps 61 to 90, the big HH sections): after its pass PASS 347, LOOK 169 open,
  BATCH-FAULT 26, FAIL 15, HARNESS 1; the 169 LOOKs (HH 15xx / 2xx text rows) went to judge-2a4.
  - Known FAILs: SQ-HH-227, -228, -262, -1232 (TODO hearth-and-harvest #2), SQ-HH-1273 (known), SQ-FF-23, -26 (TODO
    baking/baked-goods #2: no muffin sequenced-assembly ids).
  - New FAILs: SQ-HH-1300 (cow 10.47 from the bot under Tempting, may have wandered), SQ-HH-1307 (sap bucket use on a
    Create tank: tank empty), SQ-HH-1603 (watering can WaterLevel 5 -> 4 on grown wheat; doc: nothing spent),
    SQ-HH-448 (unguarded wheat not pecked by crows), SQ-MELT-11 (cryosol by lava, packed ice by salt unchanged),
    SQ-MELT-17, -18, -20 (one decor variant each); SQ-HH-439 HARNESS (log checks passed, shell count differs).
  - BF-FIX (19 rows, rerun2.txt now 35 ids): SQ-CS-04 (bot for random ticks), SQ-EDL-04 and SQ-NDM-05 (forceload),
    SQ-FCK-14, SQ-FF-12, SQ-FO-13, -15 (`naturalRegeneration false`: peaceful refilled food), SQ-ENM-03, -04, SQ-SWP-06
    (reject skips `create:milling/compat/`; ENM-03 expects air without Ender 1.2.2), SQ-GLD-08, -12 (counts after the
    2026-11-22 removals), SQ-HH-1289, -1292 (sleep before the read), SQ-HH-1311 (cdevtest randomtick), SQ-HH-303, -318
    (1.19.2 `Placed "<id>" at`), SQ-MELT-19, SQ-MG-20.
  - BATCH-FAULT, not simple: SQ-SLT-04 (tag 285 vs 290 after removals; no doc count), SQ-LOOT-01, -03 (baseline
    `baseline_loot_split_2026-11-17` stale), SQ-MG-19 (needs a new setup), SQ-OXD-01, -07, -08 (OXD.qb locked by the
    drive; OXD-07 reject matches its own echo, OXD-01/08 probably no bot).
  - Environment faults across rows (fix in tools/ after R2a, before R2b): (1) 11 boot WARNs `Error parsing recipe
    create:milling/compat/biomesoplenty/...: Unknown item` fall into restarting rows' segments and hit the generic
    Unknown-item reject; (2) peaceful food refill breaks hunger rows without `naturalRegeneration false`; (3) random-tick
    reads with no player near; (4) SQ-BOT-06 shell grep counts the probe's own lines.
- judge-2a4 (open LOOKs of steps 61 to 90, 201 filled): PASS 174, FAIL 1, BATCH-FAULT 22, INCONCLUSIVE 3 (SQ-CDFO-03,
  SQ-BOT-08, SQ-FCK-09), OWNER 1 (SQ-MG-20 JEI search).
  - FAIL SQ-HH-323: `/place structure corn_maze` places 0 chests (twice, also r1120nv); suspect CornMazePiece.java
    `placeChest`, or the row's count box misses the centre.
  - BF-FIX (HH.qb, rerun2.txt now 43 ids): SQ-HH-1502 (sleep before the read), -1563 (season MID_SUMMER), -1104 (toml
    keys `litterMaxExtra`, `truffleLitterBoost`, `truffleLitterBonus`; nested -04 jar), -1117 (51 drinks, alcohol.md),
    -235 (water window), -266 (3 dirt, writeup 3.10), -1613, -1615 (look text limited to what rdetail prints; the
    container and heated parts left as owner checks: review at write-back). SQ-HH-401 vacuous look line removed, PASS.
  - Not simple: SQ-HH-301 (reads the boot-2 toml SQ-HH-274 edited; needs a reorder), SQ-FO-01 (358 canned sources vs
    368, no doc count), SQ-GLD-11 (milk removed 2026-11-22), SQ-SLT-03 (zz_salt_probe.js expects raw rock salt to stay
    raw; placed-twin swap documented in salt.md).
  - Doc drift: hearth_and_harvest.md still names `litterChance` and the -03 jar.

## 8. Final verdicts (R1, judged 2026-11-26 10:25)

R1 wound down by the owner at drive step 38 of 51 (37 steps done). Judges 1 to 4 (scratch-judge) and runner-1.
SQ-MND-18 FAIL is unproven (diagnostics added, rerun queued); SQ-FGD-15c has no result (step 38 stopped).

| ID | Result | Evidence |
|---|---|---|
| SQ-PRI-01 | BATCH-FAULT | reclassified from FAIL: judge 2026-11-26: tag holds cooked beef/mutton (docs: beef_cooked, mutton_cooked) not raw ids; Brewin kimchi removed (Vintage kimchi is the survivor); shell test ! -s fails on comment-only stubs; fixed in PRI.qb; shell exit 1: ; no line matches /verb=tag kind=item tag=brewinandchewin:cooked_horror_lasagna_meats .*.*minecraft:beef/; no line matches /verb=tag kind=item tag=brewinandchewin:cooked_horror_lasagna_meats .*.*minecraft:mutton/; open look 1: lasagna meats = beef, mutton, beef patty, cooked mutton chops; kimchi in the cooked soup-veggie tag; bell peppers in the pizza topping tag; the three dishes have recipes |
| SQ-PRI-02 | PASS | recipes floral_large n=0; setblock floralench:large_poppy_pot changed the block, execute-if test passed |
| SQ-PRI-03 | BATCH-FAULT | reclassified from FAIL: judge 2026-11-26: bootreject (?i)jrftl matches the deliberate jrftl:prepared_flesh registration line (misc_items.js); narrowed to errors, as SQ-JRFTL-01; look itself passes (item resolves, rotten flesh in 19 alone + 11 shared recipes); boot log: /(?i)jrftl/ matched: [26Nov2026 02:14:31.433] [modloading-worker-0/INFO] [KubeJS Startup/]: creativecommon:creativecommon/func/reg_helper.js#35: [reg] item jrftl:prepared_flesh registered from the creativeengineering pack; open look 1: prepared_flesh: a CE or KubeJS replacement item resolves, or the route lists the pack's own prepared flesh; rotten flesh still in the Create / IE recipes |
| SQ-AR-01 | BATCH-FAULT | reclassified from LOOK: judge 2026-11-26: IE blueprint rdetail prints ingredients=- so no ingredient list is visible; rdetail text noteblock n=0 (id is note_block); needs a new probe or headless NCH check, not a simple fix; log shows ingredients=- for IE blueprint recipes; cannot judge (stray PASS entry withdrawn) |
| SQ-BRA-02 | PASS | asphalt tier asphalt 1.5, soul_sand no tier speedFactor 0.4, white_carpet collisionTop 0.0625, errors 0 (15304 blocks) |
| SQ-BRA-03 | FAIL | exit=3: 346 walkable blocks untiered in 11 mods (dramaticdoors 149, createframed 85, everythingcopper 56, minecraft 42); known, TODO blockrunner #20 owner decision |
| SQ-BRA-07 | FAIL | common_bond and english_bond: 'has no modifier' (audit tier constructed_brick 1.25); copper_fence and gate +0.025; known TODO blockrunner #19 |
| SQ-CAL-02 | PASS | calendar toml has [seasons] [crop_seasons] [time.*] [perf] seasonsMeltIntervalTicks=4 [wthit] cropSeasons/cropOutOfSeason; no CE common toml, so no calendar sections there |
| SQ-CAL-03 | PASS | pack order lists creativecalendar before creativedelight, creativebrewing; core finished; server scripts ready line |
| SQ-CBW-02 | PASS | both boards filled=0 after knife use; 4 breaks drop own board x1 (bare hand and axe) |
| SQ-CBW-03 | PASS | pie in board inventory (filled=1), air above board, bot main=empty |
| SQ-CDFW-07 | LOOK | open look 1: yields (catfish 6 slices + bone meal, salmon / cod 6 + bone meal, duck 2 fillets) via `cdevtest rdetail farmersdelightcutting/` if needed; the board run is not repeated here |
| SQ-CDV-04 | PASS | 30 recipe ids under creativedevtools:createmechanicalcrafting (metals, woods, netherite) plus createsequencedassembly/seq_test_star_all_processes |
| SQ-CFS-03 | PASS | skillet: use SUCCESS, unlit 120 ticks CookTime 0 with beef kept, after BurnTime+160 ticks inventory empty (cooked and ejected), oven lit=false at end; keg NBT has no temperature field (FermentTime/Inventory/FluidTank only): keg half not judged per row note (client WTHIT) |
| SQ-CMV-02 | PASS | latest.log after Stopping server: Saving players/worlds/chunks, all dimensions saved, no exception; boot 24 s, KubeJS error grep exit 0 |
| SQ-ENC-03 | PASS | hits [] and librarian keeps paper, bookshelf, compass, clock; note: removed count 5 vs writeup pass text 'at least 6' (trades.js logs 5) |
| SQ-ENC-04 | PASS | offers at levels 1/3/5 populated (paper, bookshelf, emerald sells), reject enchanted_book and ancient_tome both no match |
| SQ-TL-09 | BATCH-FAULT | reclassified from LOOK: judge r1126q 2026-11-26: scratch srv has no tools/generators.json (GeneratorRunner reads it from server root); fixed by a shell copy step in GF31.qb, rerun; open look 1: lists the generators from tools/generators.json with groups (stacks among them) |
| SQ-GSH-08 | PASS | shell lists creativecommon, creativecompat, Quark, createframed, no creativeloot; blue pane, tinted glass, tinted framed pane loot expects matched; no GlmMerge |
| SQ-HT-02 | BATCH-FAULT | reclassified from FAIL: judge r1126q 2026-11-26: scratch srv spawn-animals=false discards summoned horses (No entity was found at item replace and both data get); HT.qb section 1 has no @@prep A: spawn-animals=true (gotchas Difficulty, spawning and mobs); section prep shared by HT-01/-03, not simple by rule 11; no line matches /has the following entity data: (?=.*Id: 10b?\b)(?=.*Amplifier: 0b)/; no line matches /Found no elements matching ActiveEffects/; open look 1: the armored horse (first data get) has Regeneration (Id 10) amplifier 0; the second horse and the bot have none |
| SQ-IP-19 | PASS | sword/crimson on lava at -54.19, diamond block -63.0, oak planks gone; ash items not seen in the box (may burn later) |
| SQ-IP-20 | PASS | leaves -59.625, planks -59.875 |
| SQ-IP-21 | PASS | rocket gone, ice burned, iron survives, campfire kept |
| SQ-IPO-01 | BATCH-FAULT | reclassified from HARNESS: r1126q judge 2026-11-26: stale 770,959 baseline in the shell guard (pack now 764,198); mixin and silverpost expects passed; guard changed to the 2^20 ceiling; shell exit 1: states 764198 cut 6761 |
| SQ-IPO-02 | BATCH-FAULT | reclassified from FAIL: r1126q judge 2026-11-26: truss not formed by the fence use at 2704 -57 (2706 -57 air); arm and post growth fine; truss path needs Immersive Posts mechanics, rewrite pass; FAIL: arm formed (type=arm at 2705 -58) and 3 fence uses grew the post, but 2706 -57 is air: no truss formed |
| SQ-JFP-01 | FAIL | jar FluidHolder empty after 58 s, tank still 4000 (pump 64 RPM read back) |
| SQ-JFP-02 | FAIL | both pump directions: jar empty, tank 4000; no transfer |
| SQ-JFP-03 | FAIL | count /has the following block data: .*PendingMb: (?:[1-9]\d?/1\d\d/2[0-4]\d)[,}]/ = 0, wanted >= 2; no PendingMb on jar before unload; jar empty |
| SQ-JFP-04 | FAIL | jar FluidHolder empty in 4a and 4b, tank keeps 4000 potion swiftness; nothing moved |
| SQ-JFP-05 | PASS | jar keeps cookie, no water, tank 4000 (vacuous: pump moves nothing anywhere, TODO jars #15; recheck after fix; row puts 1 cookie not 3) |
| SQ-LGM-07 | FAIL | no line matches /verb=break drops=largemeals:omurice_block:1 stacks=1\b/; full omurice_block drops block:1 + bowl:1 stacks=2 (row wants stacks=1); 4 bowls then leftover OK; servings=2 drops bowl; TODO mod-updates #1 |
| SQ-MND-18 | FAIL | /Test failed/ matched: [26Nov2026 03:05:33.274] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Test failed; both execute checks Test failed; 1 item killed after (gossypium); cause unproven, diagnostic added, rerun queued |
| SQ-MXS-03 | PASS | chests 21 mixed seeds in 40 rolls; grass 5 of 300; shears 0 (grass:100 only) |
| SQ-OB-03 | PASS | 7 checks passed |
| SQ-OB-04 | PASS | all 5 pies/cheesecakes crust+3 fruit+2 sweet; _from_filling crust+can; 3 pot recipes unchanged |
| SQ-OB-05 | BATCH-FAULT | reclassified from FAIL: r1126q judge 2026-11-26: item id is veggiesdelight:*_vegetarian_patty, not veggie_patty; results 2/2 correct, n=4; regex fixed; no line matches /\[cdevtest\] verb=rdetail id=\S*raw_veggie_patty_from_cave_carrot .*veggie_patty\*2\b/; no line matches /\[cdevtest\] verb=rdetail id=\S*cooked_veggie_patty_from_baked_cave_carrot .*veggie_patty\*2\b/; open look 1: 4 recipes (2 cutting, 2 S&D copies): results 2 raw / 2 cooked veggie patties, inputs cave carrot / baked cave carrot, never air |
| SQ-OB-06 | BATCH-FAULT | reclassified from FAIL: r1126q judge 2026-11-26: row listed ids only; rdetail steps added; FAIL: only uses ids listed, no outputs captured |
| SQ-OB-07 | PASS | dough, raw sausage, salt dust, milk (bucket/bottle/...) ; S&D copy bowl + 250 mB milk |
| SQ-OB-08 | PASS | street_corn effects haste 600 + fire_resistance 600, no nourishment; bot after eat has both |
| SQ-OB-10 | BATCH-FAULT | reclassified from FAIL: r1126q judge 2026-11-26: makes prints no ingredients; rdetail step added; FAIL: makes shows only recipe id, no planks ingredient line |
| SQ-JC-01 | PASS | 5 listed with tab abnormals_delight; escargot and maple_cookie listed false tab null; mixin no reject |
| SQ-NCH-02 | PASS | result-slot count 0 (G2 done); no Unknown item # error |
| SQ-TL-10 | BATCH-FAULT | reclassified from FAIL: r1126q judge 2026-11-26: summary logged at server start before the row, waitlog timed out; bootexpect now. Line present: 103 checked, 2 empty/unknown; timeout 120 s waiting for /\[dict_tag_check\] \d+ dict and NCH tags checked/; open look 1: the summary line, and each problem line is a real empty or unknown tag |
| SQ-DA-01 | PASS | items: guardian shell x4, shard 10, crystals 4, no fillet/tail (bot-kill half not driven) |
| SQ-DA-02 | PASS | cutting 1/0.5/0.5 fillet + shard 0.3; cooked_guardian_tail recipes n=0; itemMerges creativedelight 49 = baseline (code logs counts only) |
| SQ-DA-03 | PASS | 4 checks passed |
| SQ-HTD-01 | PASS | six foods poison:1:100:0.2 with stated nutrition; hazard tag holds all six; salt reject passed |
| SQ-HTD-02 | BATCH-FAULT | reclassified from FAIL: r1126q judge 2026-11-26: recipe id is creativedelight:createsequencedassembly/..., text filter missed susbanderecp; ids and rdetail fixed; no line matches /verb=rdetail id=creativedelight:sequenced_assembly/suspiciousbaconandaggs_sequenced_assembly/; no line matches /verb=rdetail id=hostiledelight:susbanderecp/ |
| SQ-CDFL-05 | PASS | no bone_broth dict_tag_check line; all expects passed |
| SQ-CP-02 | PASS | 9 checks passed |
| SQ-CP-03 | PASS | organic_mash 0.5 plant_matter, value; no warning |
| SQ-FF-15 | PASS | 8 checks passed |
| SQ-CP-04 | LOOK | open look 1: hand: JEI dirt slot cycles the 8 blocks; craft one with podzol |
| SQ-GR-45 | LOOK | open look 1: hand: JEI dragon helmet 3 loops, echo sword 500 mB lava (before the values go back) |
| SQ-POT-02 | FAIL | no line matches /\[pottest\] PASS PP-02/; count /\[pottest\] FAIL PP-0[12]:/ = 2, wanted == 0; PP-SUM counts match section 1 but PP-02 FAIL: framed_flower_pot takes azalea leaves, moss, pumpkin, melon, hamimelon; hanging_flower_pot 0 of 555 (TODO decor-and-framed-blocks #1) |
| SQ-POT-04 | FAIL | count /\[pottest\] FAIL PP-04: (?!floralench:large_pot )/ = 7, wanted == 0; /\[pottest\] FAIL PP-04: floralench:large_pot .*\[(?!1x floralench:large_\w+_pot\])[^\]]*\]/ matched: [26Nov2026 03:41:50.373] [Server thread/INFO] [KubeJS Server/]: zz_pot_probe.js#50: [pottest] FAIL PP-04: floralench:large_pot returns the plant on break: minecraft:dandelion []; minecraft:poppy []; m |
| SQ-POT-04v | FAIL | timeout 30 s waiting for /\[cdevtest\] verb=break done=/; count /\[cdevtest\] verb=break .*broken=true/ = 0, wanted >= 2; state: potted_gossypium and potted_hemp placed; both breaks drops=- stacks=0 (no pot, no plant). Commands ran 03:43:36, after the 30 s waitlog gave up (server idle 03:41:55 to 03:43:36); product part is TODO framed-and-decor #6 |
| SQ-RW-24 | PASS | makes list holds createfilling/treated_wood_planks and treated_wood_smoked_planks plus iebottling/treated_wood_planks and treated_wood_smoked_planks |
| SQ-SG-02 | PASS | 03:48:29 Test passed (sheared center still fertile=false), fill 24 dirt left sheared, 'No blocks were filled' unsheared (fewer than 24) |
| SQ-SG-03 | PASS | sheared rich soil 24, unsheared none, sheared slab 24, unsheared none (03:48:37 to 03:48:43) |
| SQ-SHB-01 | PASS | 10 checks passed |
| SQ-SHB-02 | PASS | 3 checks passed |
| SQ-SHB-03 | BATCH-FAULT | reclassified from FAIL: runner-1 judge, 2026-11-26: reject/expect read the whole row log, so the emerald reject hit the second read and the wheat reject the first; reads behave as documented (31 wheat + 8 cobble, then 1 emerald + cobble); steps pinned to exact Items lists, rerun queued; /has the following block data: .*minecraft:emerald/ matched: [26Nov2026 03:51:02.757] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: 604, -60, 600 has the following block data: {Buffer: [], CourierDeadline: 0L, Credit: 0, Cooldown: 1, Value: 0, x; /has the following block data: .*minecraft:wheat/ matched: [26Nov2026 03:50:54.756] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: 604, -60, 600 has the following block data: {Buffer: [], CourierDeadline: 0L, Credit: 0, Cooldown: 0, Value: 124, |
| SQ-SHB-05 | PASS | 4 checks passed |
| SQ-SHB-07 | PASS | 8 checks passed |
| SQ-SHB-08 | FAIL | no line matches /has the following block data: .*CaughtEntity: \{.*id: "minecraft:chicken"/; no line matches /Test passed/; no line matches /Test passed/; no line matches /Test passed/; no line matches /Test passed/ |
| SQ-SHB-09 | FAIL | no line matches /has the following block data: .*id: "minecraft:ocelot"/; no line matches /has the following block data: .*id: "untitledduckmod:duck"/ |
| SQ-SHB-10 | PASS | 2 checks passed |
| SQ-STD-04 | PASS | reg half_cabinet n=99 (90+9 hearth); half_cabinet_with n=70 = 10 glass-door + 60 countertop splits, +20 glass/single-door _from_cabinet = 90 per storage_delight.js; join n=90; axe tag expect passed |
| SQ-STD-05 | BATCH-FAULT | reclassified from FAIL: runner-1 judge, 2026-11-26: rdetail ids use creativeengineering: but storage_delight.js registers under storagedelight: (n=0 shown=0); ids renamed in rows, rerun queued; no line matches /verb=rdetail id=creativeengineering:shapeless/glass_oak_half_cabinet_from_cabinet serializer=\S+ result=storagedelight:glass_oak_half_cabinet\*2\b/; no line matches /verb=rdetail id=creativeengineering:shaped/glass_oak_cabinet_from_half_cabinets serializer=\S+ result=storagedelight:glass_oak_cabinet\*1\b/; no line matches /verb=rdetail id=creativeengineering:shapeless/glass_birch_half_cabinet_from_cabinet serializer=\S+ result=storagedelight:glass_birch_half_cabinet\*2\b/; no line matches /verb=rdetail id=creativeengineering:shaped/glass_birch_cabinet_from_half_cabinets serializer=\S+ result=storagedelight:glass_birch_cabinet\*1\b/; open look 1: the `pbot use` replies (a menu opened = SUCCESS / CONSUME); the shaped recipe pattern is `H` over `H` |
| SQ-STD-06 | BATCH-FAULT | reclassified from FAIL: runner-1 judge, 2026-11-26: rdetail ids use creativeengineering: but storage_delight.js registers under storagedelight: (n=0 shown=0); ids renamed in rows, rerun queued; no line matches /verb=rdetail id=creativeengineering:shapeless/spruce_half_cabinet_with_glass_doors_from_cabinet serializer=\S+ result=storagedelight:spruce_half_cabinet_with_glass_doors\*2\b/; no line matches /verb=rdetail id=creativeengineering:shaped/spruce_cabinet_with_glass_doors_from_half_cabinets serializer=\S+ result=storagedelight:spruce_cabinet_with_glass_doors\*1\b/; no line matches /verb=rdetail id=creativeengineering:shapeless/bamboo_half_cabinet_with_glass_doors_from_cabinet serializer=\S+ result=storagedelight:bamboo_half_cabinet_with_glass_doors\*2\b/; no line matches /verb=rdetail id=creativeengineering:shaped/bamboo_cabinet_with_glass_doors_from_half_cabinets serializer=\S+ result=storagedelight:bamboo_cabinet_with_glass_doors\*1\b/; open look 1: the `pbot use` replies (a menu opened = SUCCESS / CONSUME); the shaped recipe pattern is `H` over `H` |
| SQ-STD-07 | BATCH-FAULT | reclassified from FAIL: runner-1 judge, 2026-11-26: rdetail ids use creativeengineering: but storage_delight.js registers under storagedelight: (n=0 shown=0); ids renamed in rows, rerun queued; no line matches /verb=rdetail id=creativeengineering:shapeless/dark_oak_single_door_half_cabinet_from_cabinet serializer=\S+ result=storagedelight:dark_oak_single_door_half_cabinet\*2\b/; no line matches /verb=rdetail id=creativeengineering:shaped/dark_oak_single_door_cabinet_from_half_cabinets serializer=\S+ result=storagedelight:dark_oak_single_door_cabinet\*1\b/; no line matches /verb=rdetail id=creativeengineering:shapeless/warped_single_door_half_cabinet_from_cabinet serializer=\S+ result=storagedelight:warped_single_door_half_cabinet\*2\b/; no line matches /verb=rdetail id=creativeengineering:shaped/warped_single_door_cabinet_from_half_cabinets serializer=\S+ result=storagedelight:warped_single_door_cabinet\*1\b/; open look 1: the `pbot use` replies (a menu opened = SUCCESS / CONSUME); the shaped recipe pattern is `H` over `H` |
| SQ-STD-08 | BATCH-FAULT | reclassified from FAIL: runner-1 judge, 2026-11-26: rdetail ids use creativeengineering: but storage_delight.js registers under storagedelight: (n=0 shown=0); ids renamed in rows, rerun queued; no line matches /verb=rdetail id=creativeengineering:shapeless/oak_half_cabinet_with_polished_andesite_countertop_from_cabinet serializer=\S+ result=storagedelight:oak_half_cabinet_with_polished_andesite_countertop\*2\b/; no line matches /verb=rdetail id=creativeengineering:shaped/oak_cabinet_with_polished_andesite_countertop_from_half_cabinets serializer=\S+ result=storagedelight:oak_cabinet_with_polished_andesite_countertop\*1\b/; no line matches /verb=rdetail id=creativeengineering:shapeless/crimson_half_cabinet_with_polished_tuff_countertop_from_cabinet serializer=\S+ result=storagedelight:crimson_half_cabinet_with_polished_tuff_countertop\*2\b/; no line matches /verb=rdetail id=creativeengineering:shaped/crimson_cabinet_with_polished_tuff_countertop_from_half_cabinets serializer=\S+ result=storagedelight:crimson_cabinet_with_polished_tuff_countertop\*1\b/; open look 1: the `pbot use` replies (a menu opened = SUCCESS / CONSUME); the shaped recipe pattern is `H` over `H` |
| SQ-TD-04 | PASS | [time] +8.0 days; wine clock follows, then -8.0 days; wine clock moved back (console prints only lang keys for /wine time, values not visible); no WARN |
| SQ-NS-01 | FAIL | 44,-60,10: nightstand CENightstandItems [] and hopper below (44,-61) [] while the hopper above (44,-59) still holds the sugar 10 s later; same as r1123cx (TODO decor-and-framed-blocks #3) |
| SQ-WT-02 | PASS | runner-1: shell read pool_1 196 entries, 16 agreements, pool_2 0 (instance dump kubejs/exported/villager_trades.json) |
| SQ-WT-04 | FAIL | no line matches /#a has 30 \[wt4\]/; no line matches /#b has 6 \[wt4\]/; /(Unknown (item/block/block type/entity)/Incorrect argument for command/Unknown or incomplete command/That position is not loaded/Found no elements matching)/ matched: [26Nov2026 04:10:45.072] [KubeJS Recipe Event Worker 0/WARN] [KubeJS Server/]: Error parsing recipe create:cutting/compat/biomesoplenty/dead_wood[create:cutting]: {"type":"create:cutting","conditions" |
| SQ-FGD-12m | BATCH-FAULT | reclassified from FAIL: runner-1, 2026-11-26 10:20: environment fault, qrun's probe-copy /reload (zq_biomegrid.js) at 04:16:01 dropped Chunky's commands, every chunky line 'Unknown or incomplete command'; fixed in qdrive.py (probes copied before boot); rerun; timeout 5400 s waiting for /Task finished/; shell exit 1: ; open look 1: "chunks with a match by surface biome": mushroom_fields at least 300 chunks; huge inky about 1 of 3 chunks, huge morel 1 of 4; inky and morel singles at depth 0..1; colonies next to dirt / podzol |

Counts: PASS 42, BATCH-FAULT 18, FAIL 15, LOOK 3

## Queue banner lines

Moved out of the queue banner 2026-11-26:

- r1115 (10 phases, 2026-11-15 00:10 to 08:18; [scratch_run_2026-11-15.md](scratch_run_2026-11-15.md) 8)
