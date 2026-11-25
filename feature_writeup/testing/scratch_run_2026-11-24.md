# Scratch run 2026-11-24: r1124fx (failed rows with changed jars, then the publish gate and newest rows)

Owner priorities via the main session creativeengineering2-19 (orchestrator), skill `scratch-test` part B: (1) failed
rows whose jars changed or whose row text was reviewed since they failed, (2) the Wave 1/2 publish gate (Creative
Common, Creative Compat, Raspberry Pie; a later runner, after the wave 1/2 carve), (3) the newest rows until the owner
says stop. Results in `perf/runs/r1124fx*/`.

## 1. r1124fx phase 1: failed rows with a changed jar

Bulk model, scope = this phase. Runner: runner-1 for creativeengineering2-19. Judging by scratch-judge agents.
Perf capture on (session `r1124fx`).

### 1.1 Scope

- Taken: `perf/runs/r1124fx/phase1_plan.json`, 132 rows in 52 rows files, 59 drive steps (from 142 ids in
  `phase1_ids.txt`: failed rows with a jar rebuilt or the row text reviewed after the failure).
- Not taken (orchestrator): draft rows, superseded rows, one client row.

### 1.2 Jars and preflight

| jar | `mods/` stamp at preflight (00:40) |
|---|---|
| creativecommon | 1.3.0+9af9e36c4.dirty-979dc2.2611240023 (mid-carve, another session; pinned per step) |
| creativecompat | 1.0.0+9af9e36c4.dirty-63b4d2.2611240023 (mid-carve, another session; pinned per step) |
| solRaspberryPie | 1.0.0 (mods/ 2026-11-24 00:24; mid-carve, another session; pinned per step) |
| creativedelight | 1.1.0+9af9e36c4.dirty-54ba2a.2611240023 |
| creativebrewing | 1.0.0+ac5d4ff23.2611232324 |
| creativecalendar | mods/ 1.0.0+2e68d8807.2611230027; build 1.0.0+9af9e36c4.2611240022 swapped in by prep_srv |
| creativeloot | mods/ 1.0.0+ac5d4ff23.2611232324; build 1.0.0+9af9e36c4.2611240022 swapped in by prep_srv |
| creativeworldgen | 1.0.0+e6004091.2611221216 |
| creativeengineering | 2.0.0+d97a2c9d.dirty-034823.2611221220 |
| creativedevtools | 1.0.0+ac5d4ff23.2611232324 |

- 00:37 RUNNER.md idle -> claimed (Run r1124fx). `session_role.py test`. `game_jvms.sh` empty; `df -h /` 56 GB free.
- `check_all.py --fast`: 43 run, 0 failed.
- `jarstamp.py pending`: creativeCalendar, creativeLoot built, not installed (prep_srv swaps them in).
- Full-pack steps drop the 4 jars without a mod_sides.md row (DROPJARS chestbubblesrevived, kleeslabs, numarevive,
  prettybeaches; same as r1123cc/cd; the add-mod pass is the owner's).
- Perf session r1124fx open.

### 1.3 Steps

`qrun.py drive start r1124fx --plan-json perf/runs/r1124fx/phase1_plan.json` (state
`perf/runs/r1124fx/drive/state.json`); reruns via `perf/runs/r1124fx/rerun.txt`, skips in `drive/skip.txt`.

### 1.4 Run notes

- 00:45 pre-screen (`qprescreen.py --plan-json`): 59 steps, 35 likely on 31 rows. Fixed in the row steps (run default
  11, each lint-clean with its own selection):
  - captures moved into the row (the capturing rows are not in the selection): s1home `H` in SQ-MDP-14, SQ-MDP-16,
    SQ-VTD5-01, SQ-VTD8-01; the s1home summon in SQ-FDB-11; rhome `R` (with its forceload) in SQ-RSP-07; `FO` locate
    in SQ-HCS-03; `PL` locate in SQ-IWF-04; SQ-FGD-14 locates and pregenerates its four squares (DF, SW, TA, BI) as
    SQ-FGD-12 does.
  - forceloads added: SQ-WO-03, -04, -06, -08, -09, -34 (`30000 30000 30100 30040`); SQ-BRA-05 box widened to x 880
    (its fill starts at 890), SQ-BRA-06, -07 get the box (section 2 is its own step); SQ-PS15B-01 (`992 976 1231 1215`);
    SQ-PS15-14, SQ-PWG-82 (`1136 1850 1160 1915`); SQ-EF-05 (`2144 -2592` before its place structure); SQ-HH-228 (plus
    the hb1 bot if missing), SQ-HH-262 (SQ-HH-201's big forceload is not in the selection).
  - SQ-SPF-70 ruin_scatter locate waitlog names its target (risk finding).
  - Left as false positives (9): `locate` origins (SQ-EF-05, SQ-RD-26), a nether forceload read as overworld
    (SQ-SPD-08), a probe argument list (SQ-PTH-11), a bot `tp` (SQ-SPF-72), ids the dump lacks but that exist
    (minecraft:desert_pyramid structure, creativeworldgen:sky_island structure, minecraft:oak configured feature,
    `create:kjs_` recipe-id prefix). Stale rows not rewritten (run scope).

- 00:43 drive started detached (DROPJARS from `perf/runs/r1124fx/env.sh`, session runner-1): 75 steps, 160 rows
  (boot-phase setup rows joined), 0 skipped. Pace about 1.5 to 2 min per full-pack step, under 1 min per mod-set step.
- 01:20 judge-1 (scratch-judge) on steps 1 to 14 (18 rows): PASS 14, FAIL 2 (SQ-BRA-03, SQ-BRA-07), BATCH-FAULT 2.
  BF-FIX SQ-CBA-15: `port_chain instance` set runs creativeworldgen with `kubejs = probes`, so the pack's wild_crops
  data is absent and `[wild-crops] could not read` trips the TypeError grep -> filtered in the row; queued in
  rerun.txt. SQ-GLD2-03 BATCH-FAULT not simple: row greps `[golden values] 46`, log says 39 queued; golden_food.md
  says both 46 and 41 (honey bottle and milk bucket removed 2026-11-22): owner rules the count. No later planned row
  uses the same instance-set grep (CBB, CBO, CBP rows already passed).
- 01:58 judge-2 on steps 15 to 29 (34 rows): PASS 21, FAIL 7 (SQ-CDNS-11, SQ-EDL-04, SQ-HH-1232, -1273, -262, -1292,
  -227; all repeats of known findings or rows that expect a FAIL), BATCH-FAULT 4, INCONCLUSIVE 2 (SQ-CEN-05 boot-1
  chest absent in this step; SQ-CEQ-04 filling amount not printed). SQ-HH-228 PASS after the pre-run fix.
  BF-FIX SQ-CDJ4-08: old id incubation_additions:emu_nest -> creativedelight:emu_nest; queued.
  BF-FIX SQ-CM-01: Diet Log recipe is a shared use of every food -> `shared=[01]`; queued.
  BF-FIX SQ-CS-05: no forceload in the step (lived in SQ-CS-03/04) -> `forceload add 96 8 111 16`; queued.
  SQ-VTD8-01 BATCH-FAULT not simple: the step has no pre-boot vintagedelight-common.toml with remainderBuffer=false
  (needs a section `@@prep`), so the carry line never shows.
- 03:20 judge-3 on steps 30 to 45 (28 rows): PASS 8, FAIL 10 (SQ-JFP-01 to -04, SQ-LGM-07, SQ-POT-02, -04v, SQ-PTH-11
  repeats; SQ-POT-04 new symptom, SQ-IPO-02 truss read unverified), BATCH-FAULT 9, INCONCLUSIVE 1 (SQ-JFP-05: pump moves
  nothing in any JFP row). Pre-run fixes caused no failure.
  BF-FIX SQ-IWF-04: block_growths=false never set -> toml edit + restart in the row, restored at the end; queued.
  BF-FIX SQ-MCL-04: mobCapForceLevel=2 never set, no qa bot -> edit + restart, bot, restore; queued.
  BF-FIX SQ-PS15-07, SQ-PS15-01: expectations predate melting.md / salt.md (owner, 2026-11-17) -> rewritten from the docs; queued.
  BF-FIX SQ-PS15-14: Power Grid -19 jar not in srv mods (no PGJAR prep) -> `edit cp` of the gate jar + restart; queued.
  BF-FIX SQ-PS15B-01, -10, -12: section 1 server peaceful, summoned zombies discarded -> `difficulty normal`; queued.
  BF-FIX SQ-PST-10c: no player, forceloaded chunks get no random ticks -> bot s5pl spawned in the row; queued.
  SQ-PST-08, -09c, -11, -11b (PSU.qb, not in this run) likely have the same no-player fault: for a later rewrite pass.
- 05:15 judge-4 on steps 46 to 59 (planned 34: PASS 18, FAIL 5, BATCH-FAULT 8, HARNESS 1, INCONCLUSIVE 2; WO boot-phase
  setup 28: PASS 21, BATCH-FAULT 5, FAIL 1, INCONCLUSIVE 1). FAILs: SQ-FDB-11 (arrow kills drop no flesh or onion,
  new), SQ-MDP-14 (hopper under the copper pot pulls nothing, new), SQ-VP-08, SQ-NS-01, SQ-RFX-28 (repeats). SQ-RB-22
  HARNESS (no such recipe, repeat). INCONCLUSIVE SQ-FGD-14 (truffle dirt in terralith:haze_mountain: owner or biome tag
  check), SQ-RFX-02 (recipe dump diff needs an explanation pass). No pre-run fix caused a failure.
  BF-FIX SQ-PWG-82: source board built by unselected SQ-PWG-76 -> `zpwg9 board` in the row; queued.
  BF-FIX SQ-RSP-07: reject matched the remainder_buffer slot -> expect the empty kettle Inventory; queued.
  BF-FIX SQ-VTD5-01: pizza slices x4 -> x8 (changelog); queued (TNT not exploding still unproven).
  BF-FIX SQ-SPF-71: no bot qa or PL in its step -> self-contained setup; queued.
  BF-FIX SQ-WO-34: no bot G in boot 5 -> spawned in the row; queued.
  SQ-FDB-11 pumpkin expectation 4 -> 8 slices (changelog) and queued as a second look.
  Not simple: SQ-VP-03 (shared vintage_probe flags the salting mixes, salt.md S5), SQ-WO-04, -06 (tired sleep within
  the 2400-tick warp; needs a probe or config change). WO A2 setup rows lack the r1115 `pace 0.2`.
- 06:39 main drive done, 75/75 steps. 06:40 rerun drive r1124fxr started on `perf/runs/r1124fx/rerun.plan.json`
  (19 ids from rerun.txt, 13 sections; 52 rows with the WO boot-phase setup rows joined).
- 06:45 judge-5 on steps 60 to 75 (18 worldgen/long rows): PASS 2 (SQ-WO-35, SQ-RD-10), FAIL 3 (SQ-CWG-10
  build_deploy dry run: 7 kubejs files without a KUBEJS_CLASSES rule (repeat) and the 4 unlisted jars; SQ-SPF-70
  ruin_scatter overhangs, repeat of 2026-11-06; SQ-GFB-02 96 falling blocks, repeat), BATCH-FAULT 5, INCONCLUSIVE 7
  (SQ-CDM2-02, SQ-NEST-02, SQ-BLC-01, SQ-TER-06 sample too small, repeats; SQ-SPF-72 bot not lifted by Forgiving
  Void; SQ-TER-01 50 tagged vs 109 earlier, owner baseline; SQ-HCS-03 9 nests, about 20 expected on the forest
  share), OWNER 1 (SQ-META31-04 client look).
  BF-FIX SQ-EF-05: the pre-run forceload got 3 s, place still "not loaded"; ocean positions not forceloaded -> sleep 20
  and forceloads for all three places; queued in rerun2.txt.
  BF-FIX SQ-HH-304: forceload box 272 chunks (over 256) and a countset printed after the row -> 240-block boxes and a
  waitlog per countset; queued in rerun2.txt.
  BF-FIX SQ-RD-26: count boxes over the 262144-block verb limit -> z slices and a summing shell check; rerun2.txt.
  BF-FIX SQ-SB-04: bot qa spawned only in unselected SQ-SB-01 -> spawned in the row; rerun2.txt.
  SQ-HOP-06 not simple: no own steps, judged from SQ-HH-304's countsets in another step.
- 08:40 r1124fxr done (13/13 steps). judge-6 on its 19 ids: PASS 8 (SQ-CBA-15, SQ-PST-10c, SQ-CM-01, SQ-IWF-04,
  SQ-MCL-04, SQ-PS15B-12, SQ-PWG-82, SQ-RSP-07), FAIL 2 (SQ-PS15-07 one salt melt added no Damage; SQ-WO-34 gift gave
  oak_sapling x3 with saplingCount 1, new), BATCH-FAULT 9 (second faults, not fixed per default 11; causes in the
  verdicts for a rewrite pass): SQ-CDJ4-08 padded `Gave 1    [` regex; SQ-CS-05 still no player for random ticks;
  SQ-PS15-01 state=air count 6 not 4 (post-tick lines); SQ-PS15-14 source board only SQ-PWG-76 builds; SQ-PS15B-01,
  -10 zombies burn in daylight after `difficulty normal`; SQ-FDB-11 step peaceful; SQ-VTD5-01 TNT never ticks (no
  player/forceload); SQ-SPF-71 census printed nothing.
- 08:41 rerun drive r1124fxr2 started on rerun2.plan.json (SQ-EF-05, SQ-HH-304, SQ-RD-26, SQ-SB-04).

- 09:06 r1124fxr2 done (4/4). judge-7: SQ-EF-05 BATCH-FAULT (second fault: forceloaded chunks not full after 20 s
  under heavy worldgen; needs pregen before the places), SQ-HH-304 FAIL (wild_hops 0 at two 256-chunk sites, columns
  skipped 0), SQ-RD-26 FAIL (jungle wild bell peppers and coffee 0, savanna cotton 0), SQ-SB-04 FAIL (no evoker,
  illusioner at the meta pillager_tower_siege; T&T control tower spawns fine). HH-304 and RD-26 together suggest
  wild crops are not generating in the full pack: first thing for the next session.
- 09:10 perf session r1124fx stopped; summary `perf/sessions/r1124fx/summary.md`. All JVMs stopped (`game_jvms.sh` empty).

### 1.5 Final verdicts

Results in `perf/runs/r1124fx/sNN_res` (main drive), `perf/runs/r1124fxr/` (rerun 1) and `perf/runs/r1124fxr2/`
(rerun 2); `qrun.py table` on them prints the per-row table. A rerun verdict replaces the main-drive one.

132 planned rows: PASS 71, FAIL 31, BATCH-FAULT 16, INCONCLUSIVE 12, OWNER 1 (SQ-META31-04 client look), HARNESS 1
(SQ-RB-22, "no such recipe", repeat). Not run: CQ-CCB-03, CQ-TD-06 (client), SQ-SALT-01, SQ-SB-03 (not in the plan).

- FAIL: SQ-BRA-03, -07, SQ-CDNS-11, SQ-CWG-10, SQ-EDL-04, SQ-GFB-02, SQ-HH-227, -262, -304, -1232, -1273, -1292,
  SQ-IPO-02, SQ-JFP-01 to -04, SQ-LGM-07, SQ-MDP-14, SQ-NS-01, SQ-POT-02, -04, -04v, SQ-PS15-07, SQ-PTH-11, SQ-RD-26,
  SQ-RFX-28, SQ-SB-04, SQ-SPF-70, SQ-VP-08, SQ-WO-34.
- BATCH-FAULT (rewrite pass; causes in the verdict files and the notes above): SQ-CDJ4-08, SQ-CS-05, SQ-EF-05,
  SQ-FDB-11, SQ-GLD2-03, SQ-HOP-06, SQ-PS15-01, -14, SQ-PS15B-01, -10, SQ-SPF-71, SQ-VP-03, SQ-VTD5-01, SQ-VTD8-01,
  SQ-WO-04, -06.
- INCONCLUSIVE: SQ-BLC-01, SQ-CDM2-02, SQ-CEN-05, SQ-CEQ-04, SQ-FGD-14, SQ-HCS-03, SQ-JFP-05, SQ-NEST-02, SQ-RFX-02,
  SQ-SPF-72, SQ-TER-01, SQ-TER-06.

## 2. r1124nw phase 3: newest server rows

Bulk model, scope = this phase. Runner: runner-2 for creativeengineering2-19. Judging by scratch-judge agents. Perf
capture on (session `r1124nw`). Results in `perf/runs/r1124nw/`.

### 2.1 Scope

- Taken: `perf/runs/r1124nw/plan.json` (from `perf/runs/r1124fx/phase3_ids.txt`, 71 ids: sections dated 2026-11-21 or
  later, never / failed / jar-changed / stale), 69 rows in 26 drive steps.
- Not taken: SQ-FO-18, SQ-RFX-34 (draft); gate codes CC13, CMP, RPS, RPI (except SQ-RPI-14 in RP.qb), CPP, SMK and
  client rows (orchestrator).

### 2.2 Jars and preflight

| jar | `mods/` stamp at preflight (09:20) |
|---|---|
| creativecommon | 1.3.0+16c9e8ace.dirty-b5ca75.2611240104 (mid-carve, another session; pinned per step) |
| creativecompat | 1.0.0+16c9e8ace.2611240057 (mid-carve, another session; pinned per step) |
| solRaspberryPie | 1.0.0+9af9e36c4.dirty-5e614f.2611240022 (mid-carve, another session; pinned per step) |
| creativedelight | 1.1.0+16c9e8ace.dirty-078aab.2611240103 |
| creativebrewing | 1.0.0+ac5d4ff23.2611232324 |
| creativecalendar | mods/ 1.0.0+2e68d8807.2611230027; build 1.0.0+16c9e8ace.2611240057 swapped in by prep_srv |
| creativeloot | 1.0.0+16c9e8ace.dirty-68c76a.2611240102 |
| creativeengineering | 2.0.0+0f78d9277.2611240050 |
| creativeequipment | 1.0.0+933fe3b20.dirty-0fe1fd.2611231840 |
| creativeworldgen | 1.0.0+e6004091.2611221216 |
| creativedevtools | 1.0.0+ac5d4ff23.2611232324 |

- 09:18 RUNNER.md idle -> claimed (r1124nw runner-2); the driver rewrote the `Run:` line at start. `session_role.py
  test`. `game_jvms.sh` empty; `df -h /` 30 GB free.
- `check_all.py --fast`: 43 run, 1 FAIL `test_prefix_lint` (SQ-FCK-20, -21 defined twice in FCK.qb, lines 577/627 and
  1224/1231: another session's uncommitted queue edit; FCK not in this run; not fixed here).
- `jarstamp.py pending`: creativeCalendar built, not installed (prep_srv swaps it in).
- Full-pack steps drop the 4 jars without a mod_sides.md row (DROPJARS in `perf/runs/r1124nw/env.sh`, same as r1124fx).

### 2.3 Pre-screen and stale review (before the first boot)

`qprescreen.py --plan-json`: 22 likely on 9 rows, 51 stale. Reviewed by three helper agents (one owner per .qb) and the
runner; after it 0 likely, 0 stale, lint clean.
- Dead ids, newer than the dumps (all in the installed jars or tags built at load): `@absent` added in SQ-FF-17, -18,
  -20, -23, -26, -27, -31 and SQ-CEN-09.
- SQ-RFX-35: the two Create Central Kitchen tooltip mixins are client-only (`client` list of creativedelight.mixins.json),
  never applied on a dedicated server; pass now the six common mixins, the `grep -c` step a per-name count loop.
- SQ-RFX-33: look line names the composter move to `creativedelight:` (W2-9) and the box move to Engineering.
- SQ-RE-46: golden and gilded milk bucket removed (owner, 2026-11-22): its expect became a reject. Doc finding:
  `creative_delight_drinks.md` 273, 277 still describe the golden milk bucket.
- GSH section `@@sources` path follows glm_entries.json to Compat.
- Reviewed unchanged (`@reviewed 2026-11-24`): the other SQ-FF, SQ-GSH, SQ-SNP, SQ-LOOT-13, SQ-SMO, SQ-MJ, SQ-RE, SQ-FO,
  SQ-EN-25, SQ-RM-15, SQ-RW-32, SQ-CP-04, SQ-GR-45, SQ-RPI-14 (source changes were namespace moves, literal -> dict key
  sweeps, comment reflows or generated blocks).

### 2.4 Run notes

- 09:27 perf session r1124nw open; drive started detached (`qrun.py drive start r1124nw --plan-json
  perf/runs/r1124nw/plan.json --session runner-2`, DROPJARS from env.sh): 26 steps, 69 rows, 0 skipped. ETA 12:17.
- 09:47 steps 1 to 7 done (19 rows). 09:50 drive died during step 8's boot (FF.qb section 7): driver, server and perf
  collector gone with no crash report, hs_err or kernel OOM line. Cause (user journal 09:50:52): systemd-oomd killed the
  whole editor scope the session runs in (13652 processes); the driver, its server and the collector lived in that
  cgroup (setsid does not leave it). Found 18:55 by the orchestrator.
- 18:55 step 8's half boot moved to `perf/runs/r1124nw/s07_dead_0950` (discarded). A first `drive resume` (setsid) was
  stopped by the runner after the cause was found (it marked step 8 failed at prep; its server stopped cleanly);
  18:57 resumed as systemd user units outside the editor scope: `r1124nw-drive` (`qrun.py drive resume r1124nw`,
  DROPJARS as before) and `r1124nw-perfcollect` (perf collector, same session dir). Gotcha for gotchas.md: start long
  drives with `systemd-run --user`, not setsid, so an oomd kill of the editor does not take the run.
- 18:59 step 8 done on the resumed drive (FF section 7, 7 rows).
- judge-1 (scratch-judge) steps 1 to 7, 21 rows: PASS 14, FAIL 3, BATCH-FAULT 4.
  - FAIL SQ-CM-38: pumpkin pie on a Warped Composter accepted, not consumed, level stays 0 (TODO `composting #9`).
  - FAIL SQ-CM-41: acacia composter burns as fuel but the furnace smelts nothing (TODO `composting #10`).
  - FAIL SQ-FF-23: the three muffin and cupcake `*_sequenced_assembly` recipes do not exist (cd_food.js `muffinBake`
    `F.assemble`, runs only when `global.fluid.t_milk` is set); flour regex also corrected; rerun queued.
  - BF-FIX SQ-CM-37: no rdetail step for the ingredient look -> rdetail of the spruce shaped and shapeless recipes added.
  - BF-FIX SQ-DRK-09: `makes` regex field order (result before serializer) -> swapped.
  - BF-FIX SQ-DRK-11: bottle checked in the main hand, drink code gives it to the inventory -> `pbot inv` check for
    `glass_bottle*3`.
  - BF-FIX SQ-EN-25: `cdevtest uses` has no `max=` and no result field -> `makes` on the twisted button and pressure plate.
  - Reruns queued in `perf/runs/r1124nw/rerun.txt`: SQ-CM-37, SQ-DRK-09, SQ-DRK-11, SQ-EN-25, SQ-FF-23.
- 19:22 step 16 done. judge-2 (scratch-judge) steps 8 to 16, 24 rows: PASS 20, FAIL 1, BATCH-FAULT 1, LOOK 2 (owner,
  client).
  - FAIL SQ-FF-26: no orange cranberry muffin (or carrot muffin) `*_sequenced_assembly` recipe, only the baking one;
    same cause as SQ-FF-23 (cd_food.js `muffinBake`, `F.assemble` behind `t_milk`); TODO `baking/baked-goods #2`, `#30`.
  - BF-FIX SQ-FF-28: `rdetail vinegar_jar` matches recipe ids, not results -> `rdetail vinegar_from_apple`; rerun queued.
  - LOOK left for the owner (JEI, client): SQ-CP-04 (dirt slot cycles 8 blocks, podzol craft), SQ-GR-45 (dragon helmet
    3 loops, echo sword 500 mB); their server checks passed.
- 19:50 main drive done, 26/26 steps (the driver set RUNNER.md idle). judge-3 (scratch-judge) steps 17 to 26, 24 rows:
  PASS 15, FAIL 1, BATCH-FAULT 7, INCONCLUSIVE 1.
  - FAIL SQ-RE-40: the wooden cauldron refuses Hearth and Harvest's goat milk iron bucket (use PASS, level stays 0);
    3 of 4 iron scoops work. Suspect `WoodenCauldronBlock.java` (`FluidUtil.getFluidHandler`, that bucket has no fluid
    handler), creativeDelight farm/bucket. No TODO row yet.
  - BF-FIX SQ-POT-10: NBT printed with escaped quotes -> regex `PlantA:\\*"minecraft:poppy` (and PlantB).
  - BF-FIX SQ-RE-41: `mean=0.063` failed the non-zero lookahead -> `(?!0(\.0+)?\s)`.
  - BF-FIX SQ-RE-46: `cdevtest uses` has no `max=` -> removed from its 5 calls.
  - BF-FIX SQ-RFX-35: debug.log names mixins with a package prefix; the tobacco mixin applies only after a `canSurvive`
    call -> regex `(\S+\.)?` prefix, a tobacco block with a neighbor update before the count.
  - BF-FIX SQ-SMO-02: look had no evidence (bottle given to the inventory) -> `data get entity smo2 Inventory` plus an
    expect for `minecraft:glass_bottle`.
  - BATCH-FAULT, not simple (section `@@prep` shared): SQ-RE-44 needs `spawn-animals=true` on RE.qb section 2 (the
    summoned goat and cow were discarded; the drinks passed); SQ-SNP-04 needs `spawn-protection=16` on SNP.qb section 1
    (the non-op bot could pick the sack up at spawn-protection 0).
  - INCONCLUSIVE SQ-RFX-33: diffs against a stale `kubejs/exported` baseline (about 2026-11-17); needs a fresh
    `/dumpall` (TODO `publish-readiness #6`). Boot checks passed.
  - SQ-RPI-14 PASS at `83 rungs, 5 slots, 39 pool effects`; row text moved from 81/37 per raspberry_pie.md.
- 19:51 rerun drive r1124nwr started (systemd user unit `r1124nwr-drive`, `perf/runs/r1124nw/rerun_plan.json`, 11 rows
  in 10 steps: SQ-CM-37, SQ-DRK-09, -11, SQ-EN-25, SQ-FF-23, -28, SQ-POT-10, SQ-RE-41, -46, SQ-RFX-35, SQ-SMO-02).
- 20:15 rerun steps 1 to 8 done (step 8 rebooted once by the driver for the known Rhino tag-load flake). Pack changed
  mid-rerun: Creative Common 1.3.0+4fd51289e.dirty-10384c.2611242009 installed 20:12 (another session; steps 7 and 8
  on), Snow! Real Magic 6.5.4 put in `mods/` 20:14 (integration session, sides row present).
- 20:26, 20:36 rerun steps 9 (SQ-RFX-35) and 10 (SQ-SMO-02) failed, server not ready in 600 s: boot dies at mixin
  apply, `Mixin apply failed creativecompat.mixins.json:VanillaLightMeltMixin -> SnowLayerBlock: InvalidInjectionException
  @At("INVOKE") ... cannot inject` (Snow! Real Magic rewrites SnowLayerBlock). Finding for the Snow! Real Magic
  integration (Creative Compat `VanillaLightMeltMixin` vs that mod); logs kept in `perf/runs/r1124nwr/s08_srmcrash`,
  `s09_srmcrash`. Steps 9 and 10 resumed 20:37 on the run's known pack (DROPJARS adds `SnowRealMagic-*.jar`, unit
  `r1124nwr2-drive`).
- 20:41 rerun done (steps 9, 10 booted without Snow! Real Magic). judge-4 (scratch-judge) rerun steps 1 to 8 and the
  runner steps 9, 10: PASS 8 (SQ-CM-37, SQ-DRK-09, -11, SQ-FF-28, SQ-POT-10, SQ-RE-41, SQ-RFX-35, SQ-SMO-02), FAIL 1
  (SQ-FF-23, as expected), BATCH-FAULT 2 after one fix (not simple, rewrite pass): SQ-EN-25 (IE sawmill `rdetail` prints
  no `part=fields`, the fields look needs a capture or probe change), SQ-RE-46 (`uses` lines carry `alone=` between id
  and serializer; regex `id=\S+ alone=\S+ serializer=...`).
- 20:45 perf session r1124nw closed and reported (`perf/sessions/r1124nw/summary.md`); RUNNER.md released.

### 2.5 Final verdicts

69 rows (main drive, reruns counted once at their latest verdict): PASS 57, FAIL 5, BATCH-FAULT 4, LOOK 2 (owner),
INCONCLUSIVE 1. Tables: `qrun.py table perf/runs/r1124nw/s*_res` and `perf/runs/r1124nwr/s0*_res`.

- FAIL: SQ-CM-38 (TODO `composting #9`), SQ-CM-41 (`composting #10`), SQ-FF-23 and SQ-FF-26 (muffin sequenced
  assembly recipes missing; `baking/baked-goods #2`, `#30`), SQ-RE-40 (wooden cauldron refuses the Hearth and Harvest
  goat milk bucket; no TODO row yet).
- BATCH-FAULT (rewrite pass): SQ-EN-25, SQ-RE-46 (second fault after a fix), SQ-RE-44 (RE.qb section 2 needs
  `spawn-animals=true`), SQ-SNP-04 (SNP.qb section 1 needs `spawn-protection=16`).
- LOOK left for the owner (client JEI): SQ-CP-04, SQ-GR-45.
- INCONCLUSIVE: SQ-RFX-33 (needs a fresh `/dumpall` baseline, TODO `publish-readiness #6`).
- Other findings: Creative Compat `VanillaLightMeltMixin` crashes the boot with Snow! Real Magic 6.5.4 (installed 20:14);
  `creative_delight_drinks.md` 273, 277 still describe the removed golden milk bucket.

## 3. r1124p4 phase 4: next-newest server rows

Bulk model, scope = this phase. Runner: runner-3 for creativeengineering2-19. Judging by scratch-judge agents. Perf
session `r1124p4` (not opened yet). Results in `perf/runs/r1124p4/`.

### 3.1 Scope

- Taken: `perf/runs/r1124p4/plan.json` (from `perf/runs/r1124fx/phase4_ids.txt`, 91 ids: sections dated 2026-11-17 to
  2026-11-20, never / failed / jar-changed / stale; gate codes and client rows excluded), 87 rows in 31 drive steps.
- Not taken: SQ-BBQ2-08, SQ-LOOT-05, SQ-TN-11, SQ-TN-12 (draft).

### 3.2 Preflight

- 20:48 RUNNER.md idle -> claimed (r1124p4 runner-3). `session_role.py test`. `df -h /` 21 GB free.
- `check_all.py --fast`: 43 run, 0 failed (the SQ-FCK-20/-21 duplicate is fixed at HEAD), WARN dirty_tree only.
- `jarstamp.py pending` 20:52: creativeCalendar (built 1.0.0+16c9e8ace.2611240057, installed 1.0.0+2e68d8807.2611230027) and
  creativeCommon (built 1.3.0+edd3b66b9.2611242047, installed 1.3.0+ddd126587.2611242032) built, not installed.
  Creative Compat reinstalled 20:47 with the Snow! Real Magic fix (1.0.0+edd3b66b9, per the orchestrator).

### 3.3 Pre-screen and stale review (before the first boot)

`qprescreen.py --plan-json`: 6 likely on 5 rows, 2 risk, 69 stale. After it: 1 likely (accepted), 2 risk (accepted),
0 stale; lint 0 E on every touched file.
- Dead ids, false positives: `@absent` added in OR1120.qb on SQ-HTD-02 (incomplete item registered at load), SQ-CP-02
  (tag built at load), SQ-FF-15 (recipe id), SQ-CDFL-05 (fluid registered at load; the bucket is a negative check).
- SQ-UPG-13 "unloaded 3000 64 3000": the step is a `locate biome`, which works on unloaded chunks; left.
- SQ-PS15B-26, -27 cfgedit risk: the live config edit is what the rows test (Creative Common's own watcher); left.
- Rewritten by the stale helpers (three doc-pass agents, one owner per .qb): SQ-RFX-03, -08, -11 (look lines list the
  composter move to `creativedelight:`, the Cardboard Box move to Engineering, smoothie / half cabinet / bake additions;
  RFX-08 no longer claims zero `creativedelight:` ONLY_ lines); SQ-RFX-16 (fragments regex `\d+ files, \d+ entries, 0 gated
  off`; the log shows 10 files, 56 entries); SQ-FF-13 pass text slice nutrition 4 / 0.7 (pizza_values.js, plan R25);
  SQ-TL-10 (`t_bone_broth` line no longer expected after G2b); SQ-NCH-02 (no `in a result slot` line after G2).
- Reviewed unchanged (`@reviewed 2026-11-24`): the other RFX, FF, UPG (section 1), OR1120, FO, LOOT, EFX, ENC, PS15B,
  AV, DRK, EX, HH-1617, SWL, WT rows.
- Doc finding: `creative_loot.md` 71 to 76 says the fragments are "9 files, 56 entries"; the 2026-11-23 log shows 10 files
  (Compat glass shard fragment, wave 2 carve).

### 3.4 Run notes

- 20:52 PAUSED before the first boot, step 0 of 31, for the W-P7 publish gate (orchestrator message). No drive, no
  server, no perf session started; a game JVM at the instance root (pid 1174112, not this runner's) was up at 20:52.
  RUNNER.md released.
- Resume: `qrun.py drive start r1124p4 --plan-json perf/runs/r1124p4/plan.json --session runner-3` as a systemd user unit
  (`r1124p4-drive`), perf collector likewise (`perf_session.sh start r1124p4`); DROPJARS = the 4 jars of
  `perf/runs/r1124nw/env.sh` only (the Snow! Real Magic workaround is dropped: Compat 1.0.0+edd3b66b9 carries the fix).
  Rerun `qprescreen.py` first if rows changed meanwhile; recheck `jarstamp.py pending` and record the stamps.
