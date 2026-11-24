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
  BF-FIX SQ-PS15-07, SQ-PS15-01: expectations predate melting.md / salt.md (owner 2026-11-17) -> rewritten from the docs; queued.
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
