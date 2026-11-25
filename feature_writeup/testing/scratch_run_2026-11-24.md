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

## 4. r1124gate: W-P7 publish gate (Wave 1 and 2)

runner-4 for creativeengineering2-19. Owner request: the W-P7 gate of [plan_wave1_publish.md](../plan_wave1_publish.md)
and [plan_wave2_publish.md](../plan_wave2_publish.md) on the final jars. Results `perf/runs/r1124gate/`, perf session
r1124gate.

### 4.1 Scope

148 ids (`perf/runs/r1124gate/ids.txt`): CC13, CMP, CPP, RPS, SMK (T1) rows, the wave carve ids and held failed rows.
Not taken (wave session ruling): SQ-CEN-10, SQ-CMP-09s, CQ-CMP-09, SQ-PSM-08, SQ-CGE-15. CQ-RPI-90 is an owner row
(playtest list only). Snow! Real Magic stays in the pack (SQ-RFX-35 tests its boot fix).

### 4.2 Jars and preflight

| Jar | Stamp in mods/ (jarstamp.py, 21:10) |
|---|---|
| creativecommon-1.3.0.jar | 1.3.0+ddd126587.2611242032 |
| creativecompat-1.0.0.jar | 1.0.0+edd3b66b9.2611242047 |
| solRaspberryPie-1.0.0.jar | 1.0.0+8f883cd64.2611242037 |

All three match the brief. `check_all.py --fast`: first pass 1 FAIL (`test_queue_rows`, rendered queue behind the new
draft steps) -> `status_index.py` -> 43 run, 0 failed. `game_jvms.sh` empty, 21 GB free on /. DROPJARS as r1124nw
(`perf/runs/r1124gate/env.sh`: four new jars without a mod_sides.md row).

### 4.3 Drafts and pre-screen

- 21:00 to 21:10: four drafting agents wrote steps for 55 of 56 draft rows (MELT, GSH, SNP, CGE, CM; MT, FCK, WT, RP,
  CEN, LT; CMP; RPS, CC13, CPP), lint 0 E. Left draft: CQ-RPI-95 (needs two real players), CQ-RPI-90 (owner row). New
  modset `tools/scratch_query/modsets/loot_alone.toml` (SQ-WT-04). New sections: CM.qb 7 (SQ-CM-42 jar swap), CMP.qb 7
  (SQ-CMP-35 without trashcans), RPS.qb 9 (CQ-RPS-12 fixture).
- Plan `perf/runs/r1124gate/plan.json` (qrerun.py): 116 server rows in 45 sections; 31 client rows run after.
- qprescreen: 4 likely. Fixed before boot: SQ-LE-03 (unset {H} capture -> own home-marker capture in the row);
  SQ-TPF-10 (strike points unloaded -> forceload in the row); SQ-SMK-20 (`@absent` for the weathered cardboard box,
  an Engineering id since 2026-11-24, after the last /dumpall). Left: SQ-MELT-24 (End coordinates, the lint ignores
  `dim=`, false positive).

### 4.4 Run notes

- 21:11 RUNNER.md claimed (r1124p4 paused idle at 20:53, no game JVM).
- 21:13 perf session r1124gate open (systemd user scope `r1124gate-perf`); 21:14 drive started as user unit
  `r1124gate-drive` (`perf/runs/r1124gate/start_drive.sh`, log `drive_main.out`): 54 steps, 116 rows.

### 4.5 Verdicts (judged 2026-11-24, scratch-judge; rows of steps 1 to 19 and the finished part of step 20)

Counts: PASS 26, BATCH-FAULT 8, FAIL 3 (37 rows; 20 RES dirs `perf/runs/r1124gate/s00_res` to `s19_res`). No LOOK left open.
Rulings applied from `perf/runs/r1124gate/judge_rulings.txt`: SQ-CGE-17 and SQ-GSH-12 BATCH-FAULT (wrong modset). SQ-RPS-06/-08,
SQ-WT-04, SQ-GSH-08, SQ-MELT-23/-24 and CQ-RPS-03 are in unrun steps, so nothing to judge yet.

| ID | Result | Evidence |
|---|---|---|
| SQ-CEN-09 | PASS | makes line is the splashing recipe createsplashing/cardboard_box_to_weathered_cardboard_box (compat); trailing unknown-block-type line is a stray command |
| SQ-CGE-17 | BATCH-FAULT | reclassified from FAIL: modset wrong (Compat in the set), per wave 1/2 ruling |
| SQ-CGE-18 | PASS | judge low: 9 types ratio 0.200 End vs overworld; same with only overworld listed: ratio 1.000 |
| SQ-CM-38 | FAIL | 7 pies on a warped composter: use returns PASS, level stays 0; seeds reach only level 1 or 2 (known, TODO composting #9) |
| SQ-CM-41 | FAIL | furnace with acacia composter as fuel kept raw_iron*3 and the composter after tickwarp 1600 (known, TODO composting #10) |
| SQ-CM-43 | PASS | shaped recipe from 7 spruce slabs; creativedelight:composters n=13; creativecommon:composters n=0; reg creativecommon composters 0, creativedelight 12; empty spruce composter drops itself x20 |
| SQ-CM-42 | BATCH-FAULT | checks pass; closing reject regex matches boot-2 KubeJS recipe WARN noise (create:milling biomesoplenty Unknown item); anchor it to command replies |
| SQ-CMP-01 | BATCH-FAULT | shell exit only: cmp_mixin_check.py reports "applied twice" for the Low gravity mixins (one mixin, several target classes); tool false positive |
| SQ-CMP-03 | PASS | creativecompat jar-startup packs load, item_physics.js starts and loads ItemPhysicSplit |
| SQ-CMP-04 | PASS | dumpphysics ran: 20657 items, 1416 survive lava, ItemPhysicSplit loaded |
| SQ-CMP-05 | PASS | tnt item burned, fireproof netherite item survived (CMP lines) |
| SQ-CMP-06 | PASS | weathered cardboard box placed, no Unknown block/item/loot table; creativecompat:sinking block tag (CEN-09) and physics/floats item tag n=1089 (CM-43) |
| SQ-CMP-07 | PASS | 11 paintings log lines, no immersive paintings error (445-of-445 line not in the row log) |
| SQ-CMP-08 | PASS | 0 Error occurred while handling event; moved Compat scripts load |
| SQ-CMP-10 | PASS | [corpses] present: skeletonTime 168000, emptyDespawnTime 6000, netherWitherTime 24000, noDecayDimensions the_end |
| SQ-CMP-11 | PASS | c11a 1b, c11b 0b, DeathTime 563 at game time ~482 |
| SQ-CMP-13 | PASS | c13a 1b, c13b 2b, c13c 0b |
| SQ-CMP-16 | BATCH-FAULT | skeleton at -60 (PASS); player corpse at -54.5 after 5 s, still rising to -50: wait too short |
| SQ-CMP-17 | PASS | PASS: sectionedScreen = true, curioCap = 24; PASS: Stage 0b, EmptiedAt -1L |
| SQ-CMP-23 | BATCH-FAULT | 36 [mixins] keys, row says 32 (wave 2 added 4 melting switches; row text stale); cmp_mixin_check false positive as CMP-01 |
| SQ-CMP-24 | FAIL | both mixins switched off, 0 cast errors, but the floating snow layer at 0 120 0 did not stay (PASS r1123); Snow Real Magic 6.5.4 is in the set and may fall unsupported snow: product or stale row, owner to rule |
| SQ-CMP-25 | PASS | 4 script lines; jar crop recipes 39 = pack folder 39; 2 tags |
| SQ-CMP-27 | BATCH-FAULT | substance PASS (one carry-over line in qrun_boot1.log, pagesOff right, none after restart); bootexpect after restart reads boot 2 |
| SQ-CMP-28 | PASS | Botania vine fertile=false placed (SOLID_VINE_FERTILE_STATE), hops vine fertile -> unknown property error, HOPS_PLACED |
| SQ-CMP-34 | PASS | 5 checks passed |
| SQ-CMP-35 | PASS | no trashcans jar: 0, 0 mentions in log; boots to Done |
| SQ-CPP-01 | PASS | metal_dusts n=15 with IE dusts and zinc grit; bark n=20 IW barks; only tag errors are mekanism:mekasuit_helmet (creategoggles); removed = 3 + alexsmobs:fear |
| SQ-CPP-02 | PASS | zsa result echo_shovel enchantments=true name=true; selftest 0 fail |
| SQ-CPP-03 | PASS | bottle recipe swiftness regular -> Potion swiftness; refinery -> lingering swiftness |
| SQ-CPP-04 | PASS | 0 Error parsing recipe, 0 KubeJS ERROR lines, bottling recipe present |
| SQ-CPP-05 | PASS | 0 scrap errors, 0 KubeJS ERROR (apart from none), startup order ok |
| SQ-FCK-24 | PASS | 7 checks passed |
| SQ-FCK-25 | PASS | 7 checks passed |
| SQ-GSH-10 | PASS | pane gave 28 blue shards, framed glass 38 clear shards, no GlmMerge |
| SQ-GSH-11 | BATCH-FAULT | substance PASS (GlmMerge 191 entries, no glass_shards skip, pane 28 blue shards); expect ran before the loot line appeared (5 s late) |
| SQ-GSH-12 | BATCH-FAULT | reclassified from PASS: modset wrong (cc_only full, no Compat), per wave 1/2 ruling; log looked fine (no glass_shards line, glass 63 clear shards) |
| SQ-MELT-21 | PASS | merged line names [melting]; Compat saltRange = 2, endNoFire = false |

FAIL: SQ-CM-38 (known, composting #9), SQ-CM-41 (known, composting #10), SQ-CMP-24 (snow layer did not stay; suspected Snow Real Magic falling
unsupported snow, or a stale row: owner to rule; docs say it stays, the SRM rulings say it falls).
BATCH-FAULT causes, not fixed in-run because testing was stopped (rerun with the next drive): CGE-17 and GSH-12 modset; CM-42 reject regex too broad;
CMP-01 and CMP-23 `cmp_mixin_check.py` double-count (a tool change, not simple) and CMP-23 count 32 -> 36; CMP-16 wait 5 s -> about 12 s;
CMP-27 `bootexpect` -> `shell grep logs/qrun_boot1.log`; GSH-11 `waitlog` before the expect.

### 4.6 Stop note

- 22:12 owner: "gently stop testing". The drive was stopped at a row boundary after 19 of 54 steps; step 20 stopped mid-step and its
  unfinished rows are discarded (not judged). 34 server steps and all 31 client rows were not run.
- Resume: `qrun.py drive resume r1124gate` under systemd-run (as `perf/runs/r1124gate/start_drive.sh`). Perf summary:
  `perf/sessions/r1124gate/summary.md`.

### 4.7 Fixes after the stop (2026-11-24, findings session)

- BATCH-FAULT row fixes for SQ-CGE-17, SQ-CM-42, SQ-CMP-01 (`cmp_mixin_check.py` counts per mixin and target), -16, -23,
  -24 (moved to CMP.qb section 8, mod set `nosrm`; new SQ-CMP-38 for the Snow! Real Magic hand-over), -27, SQ-GSH-08,
  -12: written by another session (uncommitted at 22:40). SQ-GSH-11: `waitlog verb=loot` before the expect.
- SQ-CM-41 reclassified as a test fault: the row's `setblock` furnaces had no `CookTimeTotal` (0), and vanilla finishes an
  item only when the progress equals it, so the composter burned and nothing smelted. Row sets `CookTimeTotal:200`.
- SQ-CM-38 product cause: Creative Delight farmers member `FDBackportEvents.onPlacePumpkinPie` denied the block's use
  and placed the pie on top (the first use CONSUME at level 0, the rest PASS). Fixed (block use first unless sneaking),
  TODO `composting #9`; the row gained a sneaking placement check. Needs the Creative Delight rebuild in `mods/`.
- Rerun: SQ-CM-38, -41, -42, SQ-CGE-17, SQ-CMP-01, -16, -23, -24, -27, -38, SQ-GSH-08, -11, -12.

#### 4.7 After the stop (wave 1/2 session, 2026-11-24)

- New final jars installed: creativecommon 1.3.0+db212e956, creativecompat 1.0.0+db212e956 (Raspberry Pie unchanged, 8f883cd64). Changes: mod descriptions; owner ruling that Snow! Real Magic handles snow fall when loaded (TODO creative-compat-publish #12, new SQ-CMP-38; SQ-CMP-24, SQ-PS15B-13, -14, -19, -22 moved to a `nosrm` mod set).
- Row fixes in 9d855c389 (SQ-RPS-06/-08 superseded, CQ-CC13-02/-04 jars, SQ-WT-04, SQ-GSH-08/-11/-12, SQ-CGE-17, SQ-MELT-23/-24, CQ-RPS-03, SQ-CM-42, SQ-CMP-16/-23/-27; cmp_mixin_check.py counts per mixin-target pair).
- Rerun list for the resume (perf/runs/r1124gate/rerun.txt): SQ-CMP-01, -16, -23, -24, -27, SQ-CGE-17, SQ-GSH-11, -12, SQ-CM-42. The resumed drive runs on the db212e956 jars: rerun prescreen and jarstamp first, and re-plan (`qrerun.py`) since the jar stamps changed; the 26 PASSes on ddd126587 / edd3b66b9 stand unless qplan marks them jar-changed.

## 5. r1124w7: W-P7 resumed on the release jars

runner-w7 (scratch-test runner subagent) for the session orchestrating wave 1. Owner authorized the boots (2026-11-24,
chat). Results `perf/runs/r1124w7/`, perf session r1124w7.

### 5.1 Scope

r1124gate's 148 ids (`perf/runs/r1124gate/ids.txt`) minus the 26 rows that passed there (section 4.5), plus SQ-CMP-38 (new,
Snow! Real Magic hand-over): `perf/runs/r1124w7/ids.txt`. The 26 PASSes stand: from ddd126587 / edd3b66b9 to db212e956
Common changed only its version and description, Compat only the snow-fall hand-over (CreativeCompat.java,
CompatMixinPlugin.java, SnowLayerFallMixin.java, FallingLayers.java), which none of the 26 rows test. Plan
(`qrerun.py`, `perf/runs/r1124w7/plan.json`): 90 server rows in 40 steps; then client rows. Left out: SQ-RPS-06, -08
(superseded), CQ-RPI-90, -95 (draft: owner row, two real players), SQ-PS15B-14, -19, -22 (draft, nosrm mod set; not in
the gate scope, added and dropped).

### 5.2 Jars and preflight

| Jar | Stamp in mods/ (jarstamp.py, 23:00) | sha1 |
|---|---|---|
| creativecommon-1.3.0.jar | 1.3.0+db212e956.2611242239 | e267d26763f3e29de1c4bca661d931c5d4f80b0d |
| creativecompat-1.0.0.jar | 1.0.0+db212e956.2611242239 | ad4746c0c4ef238ff6acb1ae957767fd3d99dd2f |
| solRaspberryPie-1.0.0.jar | 1.0.0+8f883cd64.2611242037 | cb71f0ccc76834ef24e7b060c272dba62163941f |

All three match the brief and `jar_archive/installed.csv` (22:42, 22:44, 20:38). Creative Delight in mods/ is
1.1.0+2d9bb69dc (22:49, carries the SQ-CM-38 pumpkin pie fix). `jarstamp.py pending` lists 4 newer build/libs jars
(Common, Compat, Calendar, Loot at 2d9bb69dc), so the drive runs with `NOSWAP=1` (`perf/runs/r1124w7/env.sh`): the
installed release jars only. DROPJARS as r1124gate (4 jars without a mod_sides.md row). `check_all.py --fast`: 43 run,
0 failed. `game_jvms.sh` empty, 16 GB free on /.

Pre-screen (`perf/runs/r1124w7/prescreen.txt`): 1 likely (SQ-MELT-24 End coordinates, the known false positive), 2
risk (MT), 64 stale: 46 SMK smoke rows and the RPS rows, whose sources are whole jar trees or the removed
`solRaspberryPie/libs.csv` (no row reads it); left as they are.

### 5.3 Run notes

- 23:05 RUNNER.md claimed (state idle since 22:12, no game JVM).
- 23:06 perf session r1124w7 open (systemd user scope `r1124w7-perf`); 23:07 drive started as user unit `r1124w7-drive`
  (`perf/runs/r1124w7/start_drive.sh`, log `drive_main.out`): 40 steps, 90 rows.
- 23:08 drive restarted (the first unit start failed on a relative script path, nothing ran): 44 steps with the boots, 90
  rows. Step 1 prep log `swap: off (NOSWAP=1)`.
- 23:16 BF-FIX SQ-CM-38: the 7 pies took the Warped Composter to level 7 (each use CONSUME, the pie fix works), but the
  row read the state after `sleep 2`, when vanilla's scheduled tick had already turned 7 into ready 8 -> the level-7
  check reads the 7th use reply instead (CM.qb). Rerun at the end of the drive (`perf/runs/r1124w7/rerun.txt`).
  SQ-CM-41 PASS (the CookTimeTotal fix).
- 23:31 SQ-GSH-11 BATCH-FAULT (second fault after the r1124gate fix, so not rerun in-run): the added `waitlog [GlmMerge]`
  waits for a line logged at load ([main], before Done), so it timed out; substance passes (191 entries, no
  glass_shards skip, one stack of blue shards per pane). Row text fixed for the next run (waitlog removed, bootexpect
  reads the line).
- 23:58 BF-FIX SQ-PST-08, -11, -11b: spring and winter counts all 0 (no spread at all): forceloaded chunks with no player
  get no random ticks, the cause r1124fx found for SQ-PST-10c and flagged for these three -> the same bot `s5pl` on the
  pad for the ticking part (PSU.qb). Queued for the rerun.
- 00:02 SQ-PTH-29, -30 FAIL on checks, `[zpth] mod ... none found`: the summoned fox, bee and zombie are gone (the known
  r1124fx/r1124nw batch fault: mobs vanish in 2 to 4 s, section-level spawn props shared; not simple) -> BATCH-FAULT.
- 00:04 SQ-TI-00 BF-FIX: the `ClassNotFound ... net.minecraft.client` reject matched D&D's dedicated-server event scan
  (`ClassNotFoundException: net.minecraft.client.particle.TextureSheetParticle`, create_dd on the ERROR line; not ours;
  no other match in the log) -> excluded in RD.qb; queued. SQ-LE-03 FAIL, repeat of the r1124 finding: the dispensed
  Swiftness potion bucket makes no cloud (Creative Brewing `PotionBucketDispenser` / `potion_area.js`; not a wave 1 jar).
- 00:16 SQ-SG-02, -03: the unsheared control patches kept 24 of 24 again with bot `qa` on the pad (vanilla grass,
  lush grass, grass slab): repeat of TODO `creative-calendar #2` (grass does not spread on the full pack). FAIL, known.
- 00:3x BF-FIX SQ-SMK-56: 3 Raspberry Pie items, row said 2; the third is the hidden `diet_adjustment` entry documented in
  raspberry_pie.md -> count 3 and an id check (SMK.qb); queued.
- 00:5x SQ-TPF-10 FAIL: three 1-health cows killed by lightning (MC-206922 on in the shipped debugify.json), no item
  within 20 blocks (PASS r1113tpf). Debugify is a third-party jar, not a wave 1 jar; cause not analyzed (fire from the
  bolt, or the fix). SQ-TPF-18 BATCH-FAULT: the row expects the fix's saved `AABB`, but MC-2025 is off in the shipped
  debugify.json (thirdparty_fixes.md rule 2: base never reproduced); the row needs a fix-on config for its own boot or
  a base-only rewrite (not simple: section prep).
- 01:0x SQ-WT-04 FAIL: offer counts one above the row in both modes (Loot on, 25 + 5: 31 not 30; Loot off: 7 not 6);
  the loot_alone boot's config checks pass. The constant +1 with Loot off suggests another mod's wandering-trader
  offer (Forge `WandererTradesEvent`), so the row's count may be stale; not proven from the log (the row stores counts
  only), so reported, not edited. Creative Loot is a wave 2 jar.
- 01:1x SQ-MT-04, -05, -06 HARNESS (captures unset): the summoned squid, glow squid and cachalot whale
  (`PersistenceRequired`, forceloaded deep ocean, group B) answer "No entity was found" 1 s after `Summoned new ...`.
  Same pattern as SQ-PTH-29, -30 here and SQ-SMK-31 (r1124): summoned mobs disappear on scratch worlds, cause unknown
  (a join or tick remover). Treated as a finding, not a row fault: FAIL (uncertain), one TODO row for the shared cause.
- 01:28 drive r1124w7 done, 44/44 steps (01:08 to 01:28 the long rows: SQ-CMP-12 PASS; SQ-PSM-09 INCONCLUSIVE, after
  tickwarp 12000 only 7 glow squid, no squid or dolphins: too few natural mobs for a rate). judge-1 (scratch-judge) on
  steps 1 to 42: PASS 69, FAIL 9, BATCH-FAULT 7, HARNESS 3 (SQ-MT-04 to -06: qrun keeps HARNESS while captures are
  unset; their LOOK is filled FAIL, counted FAIL here). SQ-SMK-31 FAIL: the summoned moobloom is gone (the shared cause).
- 01:29 rerun drive `r1124w7r` started (user unit, `perf/runs/r1124w7/rerun.plan.json`): SQ-CM-38, SQ-PST-08, -11,
  -11b, SQ-TI-00, SQ-SMK-56.
- 01:45 rerun r1124w7r done (01:29 to 01:45, 4 steps): SQ-CM-38, SQ-PST-08, -11, SQ-TI-00, SQ-SMK-56 PASS. SQ-PST-11b
  FAIL on substance: winter/spring 0.25 (limit 0.35 to 0.85; r1124 0.20, r1120nv 0.88), the repeat of TODO
  `creative-calendar #1`. Its `no such bot` reject hit is the row's old `pbot remove s5pd` (a bot from SQ-PST-10, not
  in the selection): left as is.
- 01:47 client block 1: full-pack capture client (`run_captures.py serve`, the 4 unlisted jars dropped; 421 jars in
  GAME/mods start fine, so the 420-jar limit in gotchas.md no longer holds), pid 1841986. `qrun.py client` on CC13
  (CQ-CC13-01 to -04), CMP (CQ-CMP-14, -15, -18 to -22, -26, -30 to -33, -36, -37), CQ-CPP-01, CQ-MELT-02, CQ-CEN-02,
  CQ-LTP-17 -> `perf/runs/r1124w7/cli_res`.
- 01:58 client block 1 cascade: CQ-CPP-01's `look from "4 -60 5"` is the eye position, so the feet went into the grass;
  the player suffocated and the death screen timed out every later `screen` step (CQ-CPP-01, CQ-MELT-02, CQ-CEN-02).
  Rows before it were not affected. Player respawned (`tools/scratch_query/batches/r1124w7/respawn.qb`); look fixed;
  the three rerun -> `cli_res2`. judge-2 (scratch-judge) on cli_res / cli_res2: BF-FIX CQ-CMP-30 (salt:salt is merged
  into vintagedelight:salt_dust, no uses with Seasoning off -> salt_dust), CQ-CMP-33 names (read the kill replies),
  CQ-CEN-02 (look coordinates `.5` doubled by the driver), CQ-CPP-01 (stand on the open pipe end, x 3; files count 3);
  rerun -> `cli_res3`: CQ-CMP-30, CQ-CEN-02 LOOK (owner shots); CQ-CMP-33 FAIL (sample syringe JEI page);
  CQ-CPP-01 haste now arrives (amp 0), but the player died again of fall damage after `gamemode survival` at the
  previous row's height (second fault: row fixed with a floor `tp`, not rerun); the row stays FAIL on the version_filter
  reject below.
- Product finding (Creative Common 1.3.0+db212e956, a wave 1 jar): every client start logs `Loading tips: skipped
  loading_tips:version_filter: Missing tips, expected to find a JsonArray`. LoadingTips.java applies the file's `remove`
  list, then reads `tips`, which version_filter.json (W-P1 tip filter) lacks; the removals still apply (213 tips from 3
  files, 319 removed) but the WARN fires. CQ-LTP-17 and CQ-CPP-01 FAIL on it.
- 02:18 CQ-RPI-94: own capture client launch with `--config config/solraspberrypie-client.toml:effects:fleetFootedFov=0.5`
  (fresh config copy): the carry-over to Compat `[fleet_footed] fov = 0.5` and its removal from the Raspberry Pie toml
  pass; the shots stay for the owner. JFR dumped to `perf/runs/r1124w7/jfr/`. Client stopped 02:19.
- 02:20 Raspberry Pie isolated pair block (`qrun.py pair`, server `qmodset.py build` with NOSWAP=1: `rps only` +
  Common, Devtools, KubeJS, Rhino, Architectury; Common 1.3.0+db212e956, Raspberry Pie cb71f0cc; client drops the other
  419 jars) -> `perf/runs/r1124w7/pair_rps`. judge-3 (scratch-judge): CQ-RPS-02, -03, -15 row faults (tooltip regex
  anchor; bread 0.80 / apple 0.52 are Apple Pie's own values, q = hunger*(1+modifier)/2 then q/5, the row's 1.00 was
  stale; `extraWidth=-1` is clamped to 0 by Forge, a string value is reset to the default instead) fixed; CQ-RPS-14
  BATCH-FAULT (12 foods reach 7.12, only Regeneration I unlocked and already in slot 1, so the change picker is empty
  and no Confirm exists; needs a rewrite with more foods; the drop half passed). Rerun `pair_rps2` 02:41: CQ-RPS-03,
  -15 checks pass (`corrected from x to its default, 48`; shots for the owner); CQ-RPS-02 second regex fault (the
  driver matches the tooltip text without the `tooltip_text ID N:` prefix), substance right (Diet Log, Food
  Dictionary, Apple Pie's book recipe gone): BATCH-FAULT, expects fixed.
- 02:5x CQ-RPS-12: server folder `rpsB` with the `rps_fixture.py table` copy of the Raspberry Pie jar (instance jar
  untouched, sha1 cb71f0cc), re-pinned; pair run -> `perf/runs/r1124w7/pair_rps12`.
- 03:0x write-back: TODO rows set Complete (all their tests PASS): composting #9, #10, corpses #9, creative-common-publish
  #18, creative-compat-publish #1 to #6, #11, #12, creativesplit/compat #5, raspberry-pie-publish #4. Not set (their
  rows files hold another session's uncommitted edits): food-tuning #38, #39, #45 (SQ-RPS-01, -04, -07 PASS) and
  mod-integration #2 (SQ-TI-00 PASS). New Bug rows: creative-common-publish #21 (version_filter WARN), creative-compat-
  publish #14 (CQ-CMP-19 mana ring), #15 (CQ-CMP-33 syringe JEI page), #16 (CQ-CMP-37 sneak use), #17 (summoned mobs
  vanish). Logged: creative-calendar #1, #2, creative-common-publish #19, creative-compat-publish #9, raspberry-pie-
  publish #24, potions/potion-buckets #5. SQ-TPF-10 (Debugify MC-206922, largecontent/mod-additions #80) not logged.
- Perf: perf session r1124w7 closed, summary `perf/sessions/r1124w7/summary.md`; the first full-pack client closed without a
  JFR dump (missed), the CQ-RPI-94 client was dumped.

### 5.4 Verdicts

Latest result per row over `perf/runs/r1124w7/s00_res` to `s43_res`, rerun `perf/runs/r1124w7r/s00_res` to `s03_res`,
clients `cli_res`, `cli_res2`, `cli_res3`, `cli_rpi94`, pairs `pair_rps`, `pair_rps2`, `pair_rps12` (`qrun.py table`).
Counts (120 rows): PASS 77, FAIL 17 (SQ-MT-04 to -06 listed HARNESS by qrun: captures unset because the mobs vanished;
counted FAIL), BATCH-FAULT 4, LOOK 21 (owner shots), INCONCLUSIVE 1. With r1124gate's 26 standing PASSes (4.5): PASS 103.

| ID | Result | Evidence |
|---|---|---|
| SQ-CGE-17 | PASS | End listed: zombie and bot 0.016; only overworld listed: 0.08; 9 drop types ratio 1.000 (shell and log lines 53-54, 858-859) |
| SQ-CM-38 | PASS | runner-w7: Warped and vanilla composters both in 1 to 7 after 30 seeds, no steady bias over two runs (warped 3 / vanilla 1 in r1124w7, 2 / 5 here); 7 pies reach level 7, ready 8 after tickwarp, bone meal out, sneaking places the pie |
| SQ-CM-41 | PASS | 7 checks passed |
| SQ-CM-42 | PASS | boot 2 expects all passed: blocks, chest 2 birch (creativedelight), stand 1; swap remap bootexpects and no Missing/unknown composter line |
| SQ-CMP-01 | PASS | 3 checks passed |
| SQ-CMP-16 | PASS | PASS: c16a Pos y -60.0 (column floor, sunk); PASS: c16b Pos y -49.28 (water surface, floating) |
| SQ-CMP-23 | PASS | 36 true keys; skipped by gate 10 (same count as r1123 PASS), all absent-mod or client-only Breezy; no range or Breezy gate line |
| SQ-CMP-38 | PASS | handover line count 1; row bootexpect passed and the y 116 landing checks passed |
| SQ-CMP-27 | PASS | boot 1: one carry-over line; file has pagesOff = [seasoning, tipped_arrows] only; restart: 0 lines, same list |
| SQ-CMP-24 | PASS | 0 ClassCast/NoClassDef lines; SNOW_STAYED said; switched-off line bootexpect passed; no Snow Real Magic jar (0) |
| SQ-GSH-11 | BATCH-FAULT | reclassified from FAIL: runner-w7 2026-11-25: waitlog [GlmMerge] waits for a load-time line logged before Done; second fault, row text fixed, no rerun; timeout 60 s waiting for /\[GlmMerge\] \d+ entries/; PASS: GlmMerge line present, no glass_shards skip, one stack of blue shards (row batch-fault on waitlog only) |
| SQ-GSH-12 | PASS | 0 glass_shards lines; pane rolls=20 drops=-; glass drops quark:clear_shard:64 (40..80) |
| SQ-MELT-22 | PASS | carried-over line expected and matched; Compat toml created; printed keys saltRange = 2, endNoFire = false; Common has no [melting] |
| SQ-MELT-23 | PASS | bootexpect tag counts (35/40/123/44) and the six expects (salt 40, fire 123, saltable 44, meltable 35, old creativecommon tags n=0) all matched; melt expects passed |
| SQ-MELT-24 | PASS | all expects matched: true refused setblock, flint, kept soul fire; false placed both fires; sound not judged (hand) |
| SQ-PS15B-13 | PASS | 6 checks passed |
| SQ-PST-08 | PASS | 4 checks passed |
| SQ-PST-11 | PASS | 3 checks passed |
| SQ-PST-11b | FAIL | shell exit 1: PST11b: spring [71, 62, 53] mean 62.0; winter [10, 28, 8] mean 15.3; winter/spring 0.25 (pass 0.35-0.85); /(Unknown (item/block/block type/entity)/does not (have/accept)/Incorrect argument for command/Unknown or incomplete command/That position is not loaded/No player was found/no such bot/no such item/Invalid (block state/tag)/\[(zq/s5)\] (error/no player)/\[cdevtest\] .*(done=0/error=))/ matched: [25Nov2026 01:41:40.845] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: no such bot; PASS: runner-w7: season set back to mid_spring (row FAIL stands on the 0.25 ratio) |
| SQ-PTH-29 | FAIL | no line matches /\[zpth\] mod f29l .* mod=none/; no line matches /\[zpth\] mod b29l .* mod=none/; no line matches /\[zpth\] mod z29l .* mod=-/; no line matches /\[zpth\] mod f29s .* mod=-/; no line matches /\[zpth\] mod b29s .* mod=-/; no line matches /\[zpth\] mod z29s .* mod=-/; summoned mobs disappear on scratch worlds (shared cause, see run doc 5.3) |
| SQ-PTH-30 | FAIL | no line matches /has the following entity data: \[100[0-3]\./ |
| SQ-LE-03 | FAIL | no line matches /LE03 swiftness cloud/; no line matches /(?i)has the following entity data: .*(swiftness/minecraft:speed/Id: 1b)/ |
| SQ-TI-00 | PASS | runner-w7: creativecompat-common.toml [moss] tintStrength = 1.0, biomeTint = true |
| SQ-RFX-35 | PASS | all six counts 1 or more (Mmlib 2) |
| SQ-RPI-96 | PASS | 7 checks passed |
| SQ-RPS-01 | PASS | Diet ladder line once in latest.log (grep -c 1), text exact; reject checks clean |
| SQ-RPS-10 | PASS | 6 checks passed |
| SQ-RPS-07 | PASS | maxDiversity 11.9666 (fixture bread), summary line 1648 values, 3 ids skipped, no per-id warning for nosuchmod |
| SQ-RPS-04 | PASS | loose solapplepie-1.19.2-2.2.0.jar and solRaspberryPie-1.0.0.jar in mods; one solapplepie mod file, one Diet ladder line (bootexpect), Done reached |
| SQ-RPS-11 | PASS | no recipe parse or edible ingredient error; Done and ladder line bootexpects passed; crafting is CQ-RPS-12 (client, deferred) |
| SQ-RPS-13 | PASS | 6 checks passed |
| SQ-SG-02 | FAIL | unsheared control kept 24 of 24 (known TODO creative-calendar #2, grass does not spread on the full pack) |
| SQ-SG-03 | FAIL | count /Successfully filled 24 block/ = 4, wanted == 2; unsheared control kept 24 of 24 (known TODO creative-calendar #2) |
| SQ-SMK-01 | PASS | 11 checks passed |
| SQ-SMK-02 | PASS | 4 checks passed |
| SQ-SMK-03 | PASS | 4 checks passed |
| SQ-SMK-04 | PASS | 3 checks passed |
| SQ-SMK-33 | PASS | 11 checks passed |
| SQ-SMK-34 | PASS | 5 checks passed |
| SQ-SMK-35 | PASS | 5 checks passed |
| SQ-SMK-36 | PASS | 3 checks passed |
| SQ-SMK-37 | PASS | 12 checks passed |
| SQ-SMK-38 | PASS | 3 checks passed |
| SQ-SMK-39 | PASS | 3 checks passed |
| SQ-SMK-40 | PASS | 5 checks passed |
| SQ-SMK-51 | PASS | 5 checks passed |
| SQ-SMK-52 | PASS | 4 checks passed |
| SQ-SMK-53 | PASS | 3 checks passed |
| SQ-SMK-54 | PASS | 9 checks passed |
| SQ-SMK-55 | PASS | 2 checks passed |
| SQ-SMK-56 | PASS | 5 checks passed |
| SQ-SMK-05 | PASS | 8 checks passed |
| SQ-SMK-06 | PASS | 3 checks passed |
| SQ-SMK-07 | PASS | 3 checks passed |
| SQ-SMK-08 | PASS | 4 checks passed |
| SQ-SMK-09 | PASS | 9 checks passed |
| SQ-SMK-10 | PASS | 4 checks passed |
| SQ-SMK-11 | PASS | 4 checks passed |
| SQ-SMK-12 | PASS | 8 checks passed |
| SQ-SMK-13 | PASS | 4 checks passed |
| SQ-SMK-14 | PASS | 2 checks passed |
| SQ-SMK-15 | PASS | 6 checks passed |
| SQ-SMK-16 | PASS | 5 checks passed |
| SQ-SMK-17 | PASS | 2 checks passed |
| SQ-SMK-18 | PASS | 10 checks passed |
| SQ-SMK-19 | PASS | 3 checks passed |
| SQ-SMK-20 | PASS | 3 checks passed |
| SQ-SMK-21 | PASS | 8 checks passed |
| SQ-SMK-22 | PASS | 4 checks passed |
| SQ-SMK-23 | PASS | 2 checks passed |
| SQ-SMK-24 | PASS | 7 checks passed |
| SQ-SMK-25 | PASS | 6 checks passed |
| SQ-SMK-26 | PASS | 4 checks passed |
| SQ-SMK-27 | PASS | 3 checks passed |
| SQ-SMK-28 | PASS | 2 checks passed |
| SQ-SMK-29 | PASS | 9 checks passed |
| SQ-SMK-30 | PASS | 3 checks passed |
| SQ-SMK-31 | FAIL | no line matches /verb=entities n=1\b/ |
| SQ-SMK-32 | PASS | 2 checks passed |
| SQ-SNP-06 | PASS | 13 checks passed |
| SQ-TPF-10 | FAIL | no line matches /Test passed, count: [1-9]/ |
| SQ-TPF-18 | BATCH-FAULT | reclassified from FAIL: runner-w7 2026-11-25: MC-2025 off in the shipped debugify.json, row expects the fix's AABB; needs fix-on config or base-only rewrite, not simple; no line matches /has the following entity data/; FAIL: batch fault, see reclassify: MC-2025 off in shipped debugify.json |
| SQ-TPF-19 | PASS | 1 check passed |
| SQ-WT-03 | PASS | 7 checks passed |
| SQ-WT-04 | FAIL | no line matches /#a has 30 \[wt4\]/; no line matches /#b has 6 \[wt4\]/; /(Unknown (item/block/block type/entity)/Incorrect argument for command/Unknown or incomplete command/That position is not loaded/Found no elements matching)/ matched: [25Nov2026 01:03:27.627] [KubeJS Recipe Event Worker 0/WARN] [KubeJS Server/]: Error parsing recipe create:milling/compat/biomesoplenty/burning_blossom[create:milling]: {"type":"create:milling","condi |
| SQ-MT-04 | HARNESS | capture M4SA: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4GA: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4WA: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4SB: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4GB: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4WB: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4SC: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4GC: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4SD: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4GD: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4OR: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4SE: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4GE: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4SF: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M4GF: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; placeholder M4WA1, M4WA2, M4WA3, M4WB1, M4WB2, M4WB3, M4OR1, M4OR2, M4OR3, M4SA1, M4SA2, M4SA3, M4SB1, M4SB2, M4SB3, M4SC1, M4SC2, M4SC3, M4SD1, M4SD2, M4SD3, M4SE1, M4SE2, M4SE3, M4SF1, M4SF2, M4SF3, M4GA1, M4GA2, M4GA3, M4GB1, M4GB2, M4GB3, M4GC1, M4GC2, M4GC3, M4GD1, M4GD2, M4GD3, M4GE1, M4GE2, M4GE3, M4GF1, M4GF2, M4GF3 unset (no capture in this batch, not in RES/vars.json); FAIL: summoned mobs disappear on scratch worlds (shared cause, see run doc 5.3) |
| SQ-MT-05 | HARNESS | capture M5QA: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M5WA: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M5HA: no line matches /entity data: (-?[\d.]+)f/; capture M5QB: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M5HB: no line matches /entity data: (-?[\d.]+)f/; capture M5WB: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M5QC: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M5QD: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M5HC: no line matches /entity data: (-?[\d.]+)f/; capture M5QE: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M5HD: no line matches /entity data: (-?[\d.]+)f/; capture M5HA, M5HB, M5HB, M5HC, M5HD unset (its row was skipped or failed); placeholder M5WA1, M5WA2, M5WA3, M5WB1, M5WB2, M5WB3, M5QA1, M5QA2, M5QA3, M5QB1, M5QB2, M5QB3, M5QC1, M5QC2, M5QC3, M5QD1, M5QD2, M5QD3, M5QE1, M5QE2, M5QE3 unset (no capture in this batch, not in RES/vars.json); FAIL: summoned mobs disappear on scratch worlds (shared cause, see run doc 5.3) |
| SQ-MT-06 | HARNESS | capture M6QA: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6WA: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6GA: no line matches /entity data: (-?[\d.]+)f/; capture M6HA: no line matches /entity data: (-?[\d.]+)f/; capture M6QB: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6WB: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6GB: no line matches /entity data: (-?[\d.]+)f/; capture M6HB: no line matches /entity data: (-?[\d.]+)f/; capture M6QC: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6WC: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6GC: no line matches /entity data: (-?[\d.]+)f/; capture M6HC: no line matches /entity data: (-?[\d.]+)f/; capture M6QD: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6WD: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6GD: no line matches /entity data: (-?[\d.]+)f/; capture M6HD: no line matches /entity data: (-?[\d.]+)f/; capture M6QE: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6WE: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6GE: no line matches /entity data: (-?[\d.]+)f/; capture M6HE: no line matches /entity data: (-?[\d.]+)f/; capture M6QF: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6WF: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6GF: no line matches /entity data: (-?[\d.]+)f/; capture M6HF: no line matches /entity data: (-?[\d.]+)f/; capture M6QG: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6WG: no line matches /entity data: \[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/; capture M6GG: no line matches /entity data: (-?[\d.]+)f/; capture M6HG: no line matches /entity data: (-?[\d.]+)f/; capture M6GA, M6GB, M6GC, M6GD, M6GE, M6GF, M6GG, M6HA, M6HB, M6HC, M6HD, M6HE, M6HF, M6HG unset (its row was skipped or failed); placeholder M6QA1, M6QA2, M6QA3, M6QB1, M6QB2, M6QB3, M6QC1, M6QC2, M6QC3, M6QD1, M6QD2, M6QD3, M6QE1, M6QE2, M6QE3, M6QF1, M6QF2, M6QF3, M6QG1, M6QG2, M6QG3, M6WA1, M6WA2, M6WA3, M6WB1, M6WB2, M6WB3, M6WC1, M6WC2, M6WC3, M6WD1, M6WD2, M6WD3, M6WE1, M6WE2, M6WE3, M6WF1, M6WF2, M6WF3, M6WG1, M6WG2, M6WG3 unset (no capture in this batch, not in RES/vars.json); FAIL: summoned mobs disappear on scratch worlds (shared cause, see run doc 5.3) |
| SQ-CMP-12 | PASS | 4 checks passed |
| SQ-PSM-09 | INCONCLUSIVE | runner-w7: after tickwarp 12000 only 0 squid, 7 glow squid (0 babies), 0 dolphins: too few natural mobs for a rate (needs 60+); no mobcap line read |
| CQ-CC13-01 | PASS | reload lines 01:48:58 (after the levelCaps edit; luck amp 2 -> 0) and 01:49:05 (after the revert; amp back to 2); the byte-identical rewrite between them logged no reload; count == 2 check passed; no failure lines |
| CQ-CC13-02 | LOOK | open look 1: ice_tooltip: the gray "Saltable" line under the name; salt and Salt Melt READ lines and shots: what the salt item says about its use (the code has no salt-item tooltip line, only the Saltable line on blocks and the JEI info page; a missing use line is a doc question, not a FAIL) |
| CQ-CC13-03 | LOOK | open look 1: hud and inventory_effects: Nausea without a level numeral, Speed shows III; potion_tooltip: "Nausea (1:00)" with no numeral; wine and JEI page not captured here (a JEED effect page has no driver screen; owner playtest) |
| CQ-CC13-04 | LOOK | open look 1: owner: per wood (planks row in front: spruce, birch, dark oak, jungle, acacia, mangrove, twisted, ancient, blossom, azalea, crimson, warped) the empty, filling (level 4) and ready (level 8) composters; the compost gaps take each plank's darkest shades, no missing texture; on approval the `sources.csv` composter rows go from `confirm` to `ok` |
| CQ-CEN-02 | LOOK | open look 1: shots: no purple-black anywhere; each face one tile of the sheet (taped top, flap bottom, handles on north and south, labels on east and west, as CQ-CMP-09); the WTHIT overlay on face_north_wthit names Weathered Cardboard Box and Creative Engineering; held and inventory icons show the box model; jei_recipe shows the splashing recipe and the box (hover its mod line by hand if JEI's mod name is not in the shot) |
| CQ-CMP-14 | LOOK | open look 1: armor_before: the corpse wears the iron armor and holds the sword; armor_after: no helmet, no sword, chestplate, leggings and boots still on; wither_corpse: a normal-size skeleton with the dark wither skeleton texture wearing the gold helmet |
| CQ-CMP-15 | PASS | final player line: botania:dodge_ring in curios:inventory ring Stacks slot 0, not in Inventory; chestplate in armor slot 102; slot_item 46 golden_helmet and slot 6 iron_chestplate checks passed |
| CQ-CMP-18 | LOOK | open look 1: sections_idle: top row 4 armor, 4 cosmetic (gold helmet leftmost, gold boots at the right end, the middle two show armor silhouettes), offhand at the right end; a Curios-style panel on the window's left edge with one slot per curio (back, belt, necklace, ring, ring); no Additional Items button; curio_hover: the tooltip names "Curio: Ring"; two_columns: 15 curios, a second column further left past 12 |
| CQ-CMP-19 | FAIL | slot_item: slot_item: no line matches /mana_ring/; open look 1: (a) the slot_item checks show the shift-clicked, dragged and hotbar-swapped items left their section slots and sit in the inventory with no tag (no nbt on the dragged ring and the swapped helmet); the shift-clicked dodge ring is in the first player line. (b) second player line: ring slot 0 holds aura_ring (put there after death), ring slot 1 mana_ring (its own index), dodge_ring in Inventory (its slot was taken), backpack in back, travel belt in belt, cloud pendant in necklace; the cosmetic helmet is back in its cosmetic slot. Hand: the extra Additional Items stack (no real-death source in this kit) |
| CQ-CMP-20 | LOOK | open look 1: corpse_gold: the corpse wears the gold helmet (its own cosmetic stack), not the leather one; corpse_iron: after the gold helmet was taken it shows the iron helmet; self_third_person: the living player still wears the leather cosmetic helmet and the corpse beyond it the iron one |
| CQ-CMP-21 | LOOK | open look 1: (with the CQ-CMP-18 shots) model_front / model_side: the corpse's player model shows the belt, the necklace and the elytra on its back over the iron chestplate; *_taken: elytra and belt gone, necklace still shown. Hand: one curio with its render toggled off does not show |
| CQ-CMP-22 | LOOK | open look 1: (with the CQ-CMP-18 shots) skeleton_dressed: the skeleton wears the gold helmet over the iron one, belt, necklace and the elytra over the chestplate; skeleton_taken: iron helmet shows, elytra gone; wither_dressed: the wither skeleton dressed the same. Hand: the render-toggled curio does not show |
| CQ-CMP-26 | LOOK | open look 1: (with the CQ-CMP-18 shots) own_wings: the living player's wings, left red, right the banner; skeleton_custom: the skeleton's wings show the same dye and banner; skeleton_vanilla: plain vanilla elytra wings |
| CQ-CMP-30 | LOOK | open look 1: salt_uses_off: no Seasoning page (salt's seasoning crafts on the Crafting / Mixing pages); tipped_arrow_off: no Tipped Arrows page (Crafting / Spout); canning and potion_buckets open; with the empty list the Seasoning and Tipped Arrows pages open too (a jei_category step that fails = page missing) |
| CQ-CMP-31 | LOOK | open look 1: the WTHIT run speed line on the powdered sugar block shows the 0.9 stuck multiplier (look_at_block; inside_block if WTHIT shows it from inside) |
| CQ-CMP-32 | LOOK | open look 1: the parts (Jump Boost and the mid-air jump line) listed under "When Applied:" as before |
| CQ-CMP-33 | FAIL | screen: timeout after 601 ticks; open look 1: names "Ancient Corpse" / "Ancient Remains" (shots and the entities line); both cookies in one jar (be line); sample_syringe_tab: the world tab shows the syringe as its icon and catalyst, no barrier icon |
| CQ-CMP-36 | LOOK | open look 1: can_filled: the cobblestone and the apples are in the can's slots; can_reopened: all 27 slots empty; the stick stack held on the cursor at close is back in the inventory (clear line), nothing on the ground; holding the cobblestone did not place it (first use with the stack opened the screen) |
| CQ-CMP-37 | FAIL | screen_info: screen_info: /deleted on close/ matched: net.minecraft.client.gui.screens.inventory.ContainerScreen title "Trash Can (deleted on close)"; open look 1: item_can_sneak: the mod's own filter screen (one item filter slot, whitelist button); ultimate, liquid and energy cans: their usual screens with and without sneak (the screen_info lines in the row log) |
| CQ-CPP-01 | FAIL | screen: timeout after 601 ticks; screen: timeout after 601 ticks; boot log: /Loading tips: skipped/Loading tips: built-in pack .* is missing/ matched: [01:47:20] [Render thread/WARN]: Loading tips: skipped loading_tips:version_filter: Missing tips, expected to find a JsonArray; open look 1: jei_apple_cider: owner hovers or reads the cider fluid's tooltip (effect lines under the fluid name; not automated); descriptions (shell): Common and Engineering texts as in their gradle.properties; tips line counts 3 files; tea_pipe_hud: the Haste icon on the HUD while the open pipe spills tea (tank amount falling in `data get`) |
| CQ-LTP-17 | FAIL | /Loading tips: (skipped/built-in pack .* is missing)/ matched: [02:01:21] [Render thread/WARN]: Loading tips: skipped loading_tips:version_filter: Missing tips, expected to find a JsonArray |
| CQ-MELT-02 | LOOK | open look 1: the driver steps passed (text "Saltable" on six saltable items, none on blue ice, permafrost, salt block); the shot shows the line gray under the name when hovered |
| CQ-RPI-94 | LOOK | open look 1: shots: the sprint_jump view is only slightly wider than sprint_ground (fov 0.5 = half the Fleet-Footed shift); the feel is the owner's live check (CQ-RPI-76) |
| CQ-RPS-02 | BATCH-FAULT | reclassified from FAIL: runner-w7 2026-11-25: second regex fault (the driver matches the tooltip text without the 'tooltip_text ID N:' prefix); substance PASS from the READ lines (Diet Log, Food Dictionary, Unknown recipe solapplepie:food_book); expects now 'Diet Log$' / 'Food Dictionary$'; tooltip_text: tooltip_text: no line matches /tooltip_text solapplepie:food_book 0: Diet Log$/; tooltip_text: tooltip_text: no line matches /tooltip_text solcarrot:food_book 0: Food Dictionary$/; PASS: runner-w7: server reply 'Unknown recipe: solapplepie:food_book'; the potato Diet Log craft passed in pair_rps (judge-3) |
| CQ-RPS-03 | LOOK | open look 1: the Diet Log diversity page shows the same total as `solrp diversity get` (bread 0.80 plus golden apple 4.00 at Apple Pie's decay weights) and the meal list shows bread and golden apple; cake has no item tooltip value (a placed block; Apple Pie counts its slices when eaten), so the cake read only shows its lines |
| CQ-RPS-05 | LOOK | open look 1: G1 above 0 and the gate shot shows no "Eat N more foods" line (gate open at the first meal); D1 from 33 meals (the oldest bread out of the 32-meal window); D2 = D1 minus what the 5 oldest window foods gave (respec_confirm lists them; after_respec pages no longer show them); D3 below D2 by the drop cost (drop_confirm); after death diversity 0.00 and the Diet Log pages empty |
| CQ-RPS-09 | LOOK | open look 1: pickers: Regeneration I at 5 and 9 (gone from 13, flat Regeneration at 10), Jump Boost I, Luck I (gone from 17.5 up), Night Vision, Water Breathing, Fire Resistance, Jump Boost II as they unlock; Speed, Haste and Strength in no picker shot; nothing_eaten pages: the diversity icon is a vanilla seed (or rotten flesh / spider eye); div50 pages (Overview, Diet Ladder, Milestones): unlock icon an awkward potion, permanent-buff icon a diamond, slot icon an item frame |
| CQ-RPS-14 | BATCH-FAULT | reclassified from FAIL: judge-w7 2026-11-25: change-slot-1 step ran at diversity 7.12 (12 foods); only Regeneration I is unlocked (Jump Boost I at 8.8) and slot 1 already holds it, so the change picker is empty, Button n 0 is Cancel, no confirm panel (shot change1_confirm). Needs more foods or another design (not simple: counts and expected aged-out numbers change). Drop part passed (27.49); click_widget: no widget labeled "Confirm"; open look 1: after_drop3: slots 1 and 2 keep their effects, slot 3 empty and grayed; drop3_confirm lists only slot 3 and its effect as lost; changed pages: the 5 oldest of the 12 foods gone from the Last meals grid and the history, E1 - E2 = what they gave (change1_confirm); death pages: grid and history empty; E3 = apple's full 1.00 (an aged-out food counts in full again) |
| CQ-RPS-15 | LOOK | open look 1: nothing eaten, so every food is uneaten: hardest pages start with the enchanted golden apple (10), golden apple (4), then the 2s (cooked beef and porkchop before golden carrot: more hunger first), then the 1.0 foods by hunger; easiest pages the reverse by base diversity; diet_log_width: the Diet Log about 25% wider than the plain book (same as before); the grep shows HARDEST after EASIEST in the comment and the allowed values |
| CQ-RPS-12 | LOOK | open look 1: liquid_log: the drinks newest first with the third drink (potion) on top, milk below it, still listed after the bread was eaten (the state sync without a drink list kept the client's list) |


FAIL, one line each:
- Wave 1 jars: CQ-LTP-17, CQ-CPP-01 (Creative Common: `Loading tips: skipped loading_tips:version_filter` WARN at every
  client start, creative-common-publish #21; CQ-CPP-01's tea pipe haste itself arrived in cli_res3); CQ-CMP-19 (Compat
  corpse screen: the cursor-swapped mana ring ends in no slot, #14); CQ-CMP-33 (Sample Syringe JEI uses page does not
  open, #15); CQ-CMP-37 (sneak use on the Item Trash Can opened the deleted-on-close screen; may be the driver's
  sneak timing, #16); SQ-MT-04 to -06, SQ-PTH-29, -30, SQ-SMK-31 (summoned mobs vanish within 1 to 4 s, cause unknown,
  #17; blocks the squid flee check of #9).
- Other jars, repeats: SQ-SG-02, -03 (grass does not spread, creative-calendar #2); SQ-PST-11b (winter/spring 0.25,
  creative-calendar #1); SQ-LE-03 (potion bucket dispenser cloud, potions/potion-buckets #5); SQ-TPF-10 (Debugify
  MC-206922: no drops after lightning); SQ-WT-04 (Creative Loot, wave 2: offer counts +1 in both modes,
  creative-common-publish #19).
BATCH-FAULT: SQ-GSH-11 (load-time waitlog; fixed, not rerun), SQ-TPF-18 (MC-2025 off in the shipped debugify.json),
CQ-RPS-02 (second regex fault; substance right; fixed), CQ-RPS-14 (needs more foods to unlock a second effect).
INCONCLUSIVE: SQ-PSM-09 (too few natural squid). Not run: CQ-RPI-90, -95 (draft), SQ-PS15B-14, -19, -22 (draft).

Owner shots (LOOK open): CQ-CC13-02, -03, -04, CQ-CEN-02, CQ-CMP-14, -18, -20, -21, -22, -26, -30, -31, -32, -36,
CQ-MELT-02, CQ-RPI-94, CQ-RPS-03, -05, -09, -12, -15 (`qrun.py judge RES` lists them; shots in
`test_captures/judgement/qrun_cli_res*`, `qrun_cli_rpi94`, `qrun_pair_rps*`).

Wave 1 for W-P8: not clear. Raspberry Pie has no product FAIL (its open items are owner shots and the CQ-RPS-14
rewrite). Creative Common has the version_filter WARN (cosmetic, the filter works). Creative Compat has CQ-CMP-19
(possible item loss in the corpse screen), CQ-CMP-33, the unproven CQ-CMP-37, and the shared mob-vanish cause; per
W-P7 a product fix sends the jar back to W-P6. Owner ruling needed on which of these block the upload.
