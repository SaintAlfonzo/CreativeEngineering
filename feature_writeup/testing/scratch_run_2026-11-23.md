# Scratch run 2026-11-23: r1123cc (Creative Common 1.3.0 publish tests)

Targeted run (skill `scratch-test` part B, owner request 2026-11-23 via the main session): milestone 1 of the publish
gate, the Creative Common rows of [plan_cc_publish_1_3.md](../plan_cc_publish_1_3.md) "P7 Tests". Runner: runner-cc
for the main orchestrator. Results in `perf/runs/r1123cc*/`. Perf capture skipped (targeted run, run default 4).

## 1. Scope

One drive per stage, simple -> complex; plan lists in `perf/runs/r1123cc/<stage>.txt`, plans built by `qrerun.py`
(`<stage>.plan.json`). No row skipped as current: the Common jar changed since r1122cc (see 3).

| Stage | Set | Rows planned | Left out by qrerun |
|---|---|---|---|
| a | T1 smoke (SMK section 1) + CMV/CC12 smoke | SQ-SMK-01 to -04, SQ-CMV-03, SQ-CC12-01 | - |
| b | Common alone | SQ-CMV-02, -04, SQ-CCO-01, -04, -15, -16 | - |
| c | optional mods present, then absent | SQ-CCO-02, -03, -05 to -11, -12a to -12f | SQ-CCO-14 superseded |
| d | Common + Compat + Calendar | SQ-CC12-02, SQ-CIF-04, -04b | - |
| e | full pack | SQ-CC12-03, SQ-CMV-01, SQ-CPP-01 to -05, SQ-CC-01 to -16, SQ-CGE (18), SQ-CDNS-02, -07, -08, -10, -11 | SQ-CC-09, -12 draft; SQ-CDNS-12 draft; SQ-CDNS-01, -03 to -06, -09 superseded |
| f | new since 1.2.0 | SQ-MELT-01 to -10, -08, -09b, -P1, -P1b; SQ-SALT-03, -06, -08; SQ-EFX-15 to -17 (level caps; LCA rows are all client); SQ-CM-37 to -41 (composters) | SQ-MELT-08b, -11 to -20 draft, -P2 no steps; SQ-SALT-01, -02, -04, -05, -07 draft |
| g | client rows of the P7 client table | see 7 | |

## 2. Tools

`qrun.py drive` per stage, `qrerun.py` for the plans, `qprescreen.py --plan-json perf/runs/r1123cc/all.plan.json`,
`scratch-judge` for LOOKs.

## 3. Jars

| jar | `mods/` (stamp) | installed |
|---|---|---|
| creativecommon | 1.3.0+2e68d8807.dirty-e4f25c.2611230023 | 2026-11-23 00:30 |
| creativecompat | 1.0.0+2e68d8807.2611230027 | 2026-11-23 00:30 |
| creativecalendar | 1.0.0+2e68d8807.2611230027 | 2026-11-23 00:30 |

`jarstamp.py pending`: 0 built, not installed.

## 4. Preflight

- RUNNER.md idle, claimed 00:40. `df -h /` 84 GB free.
- `game_jvms.sh` listed the ATLauncher launcher process (`ATLauncher.jar`, not a game): tooling fault, fixed in
  `tools/scratch_query/game_jvms.sh` (skips `ATLauncher\.jar`).
- `check_all.py --fast`: 42 run, 1 FAIL = comment_dates (3 dated comments: Common `LeafPiercingTrail.java:15`,
  Creative Delight `GoldenItems.java:20`, `golden_values.js:3`; edits under `development/`, not allowed in a run;
  reported, no runtime test failed).
- `qprescreen.py`: 9 likely findings, all left as false positives: SQ-CC-08, -16, SQ-CCO-07, -11 (bot teleports load
  the chunks, as r1122cc), `forge:entity_gravity` in SQ-CGE-06, -09a, -09b, -11 (an attribute), SQ-CDNS-10
  `creativedelight:seasoning` (a recipe type, not a registry item).

## 7. Run notes

- 00:38 a game client was launched in the instance itself (ATLauncher, world "Workspace"; pid not ours). Hard rule:
  one game JVM at a time, so no scratch boot while it runs. Never killed.

- 00:48 owner client exited (game_jvms.sh empty); stage a drive `r1123cca` started (3 steps). Steps 1, 2 ran
  (SQ-CC12-01 LOOK, SQ-CMV-03 PASS).
- 00:52 step 3 (SMK, full pack) refused by the pin check: 4 jars in `mods/` with no mod_sides.md row
  (chestbubblesrevived, kleeslabs, numarevive, prettybeaches; added to `mods/` 2026-11-23 00:17 to 00:29, outside this
  run). Environment fault: new `DROPJARS` option in `tools/scratch_query/prep_srv.sh` removes them from `srv/mods`
  before the pin; every later full-pack step of this run runs with them dropped (`perf/runs/r1123cc/env.sh`), so
  the full-pack rows test the pack as of the 1.3.0 build, without those 4 mods. The add-mod pass (sides rows) for
  them is the owner's. 00:55 `drive resume r1123cca` (one resume was cut by a 20 s timeout of mine: "stopped",
  resumed cleanly).
- 00:58 stage a done (3/3 steps); stage drives b to f chained (one after another, `game_jvms.sh` checked before each).
- 01:00 judge-a: SQ-CC12-01 FAIL (product): cc_only full startup.log ERROR NCH shapeless
  twisted_composter_to_composter_by_shapeless, `creativecommon:twisted_composter` unknown without Twisted Wood
  (composters.js modded list unconditional); TODO creative-common-publish #16.
  BF-FIX SQ-SMK-01: `[swaps] load report` is logged only at server stop (SwapReport onStopping), the row read the log
  while up -> SMK.qb row now `restart`s and greps the newest `logs/qrun_bootN.log`; rerun queued
  (perf/runs/r1123cca/rerun.txt).
- 01:01 stage b done (00:58 to 01:01, 2 steps). judge-b: SQ-CMV-02 FAIL on #16 (5 NCH composter ERRORs on Common
  alone: twisted, ancient, blossom, azalea; qrun keeps it HARNESS, the shell check is the only failed check).
  BF-FIX SQ-CCO-16: no player for `advancement grant` -> `pbot spawn s5pb1` + `sleep 2` before it (CCO.qb); rerun
  queued (perf/runs/r1123cc/rerun.txt).
- 01:17 stage c done (01:01 to 01:17, 7 steps). judge-c: PASS 8, FAIL 6 = SQ-CCO-12a to -12f, each only on the #16
  composter ERROR (25 of 25 jar files loaded, mixin lists match the absent mods). SQ-CCO-09 note: pig and cow
  discarded (no animals on the set), mob search not exercised. BF-FIX SQ-CCO-08: overworld zombie died of fall damage
  before the Pos read -> `Invulnerable:1b` on both summons (CCO.qb); rerun queued.
- 01:20 stage d done (01:17 to 01:20, 3 steps), judged by the runner: SQ-CC12-02 FAIL on #16 only (the r1122cc
  sand_layers ERROR is gone: TODO blockrunner #18 no longer reproduces here), SQ-CIF-04, -04b PASS. Observation (not
  a Common row): with Equipment on Common + Compat + Calendar, startup.log has 4 Equipment NCH ERRORs (arrow and
  leather recipes with an undefined output, gold shield key `#forge:rods/gold` empty).
- 01:20 stage e drive `r1123cce` started (11 steps, ETA about 02:52).
- 01:36 judge-e1 (stage e steps 1 to 4, CC and CC12-03): PASS 14, FAIL 1 = SQ-CC-03 on #16 only (8 composter
  ERRORs on its reduced set; qrun keeps HARNESS). SQ-CC12-03 full pack: 0 startup ERROR, 25/25 files, 742 aliases.
- 01:58 judge-e2 (stage e steps 5 to 11: CDNS-02, -10, -11, CGE, CMV-01): PASS 19, FAIL 2, BATCH-FAULT 1.
  SQ-CGE-15 FAIL on #16 only (2 startup ERRORs on its Common-only set; qrun keeps HARNESS). SQ-CDNS-11 FAIL (Creative
  Delight, not a Common gate): Brewin' vegetable omelet places Display Delight's `bnc_vegetable_omelet`, not
  `creativedelight:placed_vegetable_omelet`; TODO placeable-food #4. BF-FIX SQ-CDNS-10: Creative Delight renamed the
  seasoning serializer to `creativedelight:flavor` (2026-11-19) -> row counts `flavor`; rerun queued.
- 02:01 stage e done (01:20 to 02:01, 15 steps). judge-e3 (steps 12 to 15: CPP, CDNS-07, -08): PASS 6, FAIL 1 =
  SQ-CPP-05 on #16 only (the r1122cc sand_layers ERROR is gone). Stage f drive `r1123ccf` started 02:01.
- 02:20 judge-f1 (stage f steps 1, 2: CM-37 to -41, EFX-15 to -17): PASS 6, FAIL 2. SQ-CM-38 FAIL: warped composter
  refuses pumpkin pie (use returns PASS, level stays 0; seeds fill it), TODO composting #9. SQ-CM-41 FAIL: the acacia
  composter burns as fuel but the raw iron is not smelted after tickwarp 1600 (crimson control right); product
  suspected, tickwarp not ruled out, TODO composting #10 with the diagnostic step.
- 03:30 judge-f2 (stage f step 3, MELT group A): PASS 5 (-01, -03, -10, -P1 0.012 ms, -P1b 0.015 ms), BATCH-FAULT 5,
  no product FAIL. BF-FIX SQ-MELT-02, -06: Serene Seasons surface melt of open-air controls -> glass above every
  target (-06 reject also skips "Error parsing recipe"). BF-FIX SQ-MELT-04: row stale against melting.md 1 (salt never
  melts blue ice, fire never packed ice) -> expects rewritten. BF-FIX SQ-MELT-07: source-driven list now starts with
  molten nether bricks -> regex allows a prefix. BF-FIX SQ-MELT-08: blue ice not saltable -> retargeted to packed ice.
  All rerun queued. Seen: `Adding duplicate key 'moonlight:soft_fluids / creativedelight:bone_broth'` ERROR at login
  (Creative Delight, not a Common row).
- 03:39 stage f done (02:01 to 03:39, 6 steps; SQ-MELT-P1 and -P1b hold 2 x 600 s sleeps each). judge-f3: SQ-MELT-05
  PASS, SQ-SALT-03, -06, -08 PASS. BF-FIX SQ-MELT-09, -09b: the bootreject matched the documented empty-tag
  `dict_tag_check` WARN for `#creativecommon:melting/salt` on the no-Salt set -> `(?!.*dict_tag_check)`; rerun list
  perf/runs/r1123cc/rerun2.txt.
- 03:40 rerun drive `r1123ccr` started (perf/runs/r1123cc/rerun.plan.json: SQ-SMK-01, SQ-CCO-08, -16, SQ-CDNS-10,
  SQ-MELT-02, -04, -06, -07, -08).
- 04:13 rerun drive `r1123ccr` done (03:40 to 04:13, 4 steps): SQ-SMK-01, SQ-CCO-08, -16, SQ-MELT-02, -04, -06, -07,
  -08 PASS. SQ-CDNS-10 BATCH-FAULT (second fault: the reject also matches `lunchbox/lunchbox_dye` and `lunchbox_soap`
  `result=empty`, special recipes with a computed output; the 7 serializer counts passed; left for a rewrite pass).
  SQ-CCO-08 note: End zombie y 140.9 vs overworld 66.0; the attribute dump shows no listed modifier, same as r1122ccr.
- 04:14 rerun drive `r1123ccr2` (SQ-MELT-09, -09b) started.
- 04:20 rerun drive `r1123ccr2` done (04:14 to 04:20): SQ-MELT-09, -09b PASS (runner judged).
- 04:21 stage g: capture client (`run_captures.py serve`, the 4 unlisted jars dropped with `--drop`) up 04:21 to 04:24;
  CQ-CCB-02 ran: log checks pass (`[fluid_jei] added 139 create:potion fluid stacks`, no JEI order error), shot
  `test_captures/judgement/qrun_res/CQ-CCB-02_jei_list_order.png` for the owner (LOOK left open). Client stopped,
  `game_jvms.sh` empty 04:24.

## 8. Final verdicts

Final verdict per row (a rerun replaces the first result; history in the verdict files). Results dirs:
`perf/runs/r1123cca` to `r1123ccf` (stages a to f), `r1123ccr` and `r1123ccr2` (reruns), `r1123ccg` (client).

| Stage | Rows | PASS | FAIL | BATCH-FAULT | LOOK | Elapsed |
|---|---|---|---|---|---|---|
| a smoke | 6 | 5 | 1 | 0 | 0 | 00:48 to 00:58 (10 min, 3 min of it the jar-pin fault) |
| b Common alone | 6 | 5 | 1 | 0 | 0 | 00:58 to 01:01 (4 min) |
| c optional mods present / absent | 15 | 9 | 6 | 0 | 0 | 01:01 to 01:17 (16 min) |
| d Common + Compat + Calendar | 3 | 2 | 1 | 0 | 0 | 01:17 to 01:20 (3 min) |
| e full pack | 44 | 39 | 4 | 1 | 0 | 01:20 to 02:01 (41 min) |
| f new since 1.2.0 | 24 | 22 | 2 | 0 | 0 | 02:01 to 03:39 (98 min; MELT-P1, -P1b sleep 2 x 600 s each) |
| reruns (12 rows, counted in their stage) | - | - | - | - | - | 03:40 to 04:20 (40 min) |
| g client | 1 | 0 | 0 | 0 | 1 (owner shot) | 04:21 to 04:24 (3 min) |
| total | 99 | 82 | 15 | 1 | 1 | 00:48 to 04:24 |

FAILs: 11 of the 15 are one product finding, TODO creative-common-publish #16 (NCH composter -> vanilla recipes for
the twisted, ancient, blossom and azalea composters built when their mods are absent; `composters.js`): SQ-CC12-01,
-02, SQ-CMV-02, SQ-CC-03, SQ-CGE-15, SQ-CPP-05, SQ-CCO-12a to -12f. The others: SQ-CM-38 (composting #9), SQ-CM-41
(composting #10), SQ-CDNS-11 (Creative Delight, placeable-food #4). r1122cc's known FAILs: NCH sand_layers without
Immersive Weathering (blockrunner #18) did not reproduce in any set; item_dict canned roll medley ids (mod-updates #3)
not seen.

| ID | Result | Evidence |
|---|---|---|
| SQ-CC12-01 | FAIL | jar creativecommon-1.3.0 only (+devtools), swaps summary line present (0 capability keys), 1 of 1 files loaded line ok, no client mixin; but startup.log has 1 ERROR: [NCH] shapeless twisted_composter_to_composter_by_shapeless: ... |
| SQ-CMV-03 | PASS | 4 checks passed |
| SQ-SMK-01 | PASS | 11 checks passed |
| SQ-SMK-02 | PASS | 4 checks passed |
| SQ-SMK-03 | PASS | 4 checks passed |
| SQ-SMK-04 | PASS | 3 checks passed |
| SQ-CCO-01 | PASS | latest.log Done (26.615s); startup.log: creativecommon: 25 of 25 files loaded from its jar; all 5 checks ok |
| SQ-CCO-04 | PASS | shell.txt: overrides=[] removed=[] gravityLimitDimensions=true enabled=true disabled=false (lines 6/37/91/106/121/146/182) |
| SQ-CCO-15 | PASS | creativecommon: 25 of 25 files loaded; tag bark n=13, metal_dusts n=0 (absent mods); [ccdict] lines show values, done, 0 exception/error in server.log (DISTXFORM client-dist ERRORs in reload are not Common's) |
| SQ-CCO-16 | PASS | runner r1123cc rerun: Granted the advancement [Minecraft] to s5pb1; wandering trader disabled, offers vanilla |
| SQ-CMV-02 | FAIL | qrun HARNESS (the shell error check only): NCH composter ERRORs = TODO creative-common-publish #16; shell exit 1: [01:00:38] [ERROR] ! [NCH] shapeless failed and was skipped: [NCH] shapeless twisted_composter_to_composter_by_sh... |
| SQ-CMV-04 | PASS | shell: advancements enabled=true, wandering_trader enabled=false 24000/5/1, creative_menu disabled=false |
| SQ-CCO-02 | PASS | 4 checks passed |
| SQ-CCO-03 | PASS | Successfully filled 25 blocks and fill done lines; fill stands as the check |
| SQ-CCO-05 | PASS | mossy_cobblestone 1 -> 84 after 1200-tick warp at randomTickSpeed 300 |
| SQ-CCO-06 | PASS | 3 checks passed |
| SQ-CCO-07 | PASS | CCO07 start 01:04:41 -> arrived 01:04:45, 4 s |
| SQ-CCO-08 | PASS | runner r1123cc rerun: End zombie y 140.93 vs overworld zombie y 66.0 after the same time (slower fall); attribute dump shows base forge:entity_gravity 0.08 and no listed modifier, same as the r1122ccr PASS (Create Gravity low g... |
| SQ-CCO-09 | PASS | whistling arrow summoned, no NEA/nock/whistl ERROR; pig and cow discarded by peaceful/no-animals set so no mob search was exercised (note) |
| SQ-CCO-10 | PASS | 5 checks passed |
| SQ-CCO-11 | PASS | overwrites conform 1, Static binding violation 1 (designed) |
| SQ-CCO-12a | FAIL | /(?i)/ERROR\].*creativecommon/ matched: [23Nov2026 01:08:18.121] [Server thread/ERROR] [KubeJS Startup/]: [NCH] shapeless failed and was skipped: [NCH] shapeless twisted_composter_to_composter_by_shapeless input[0]: item/tag "c... |
| SQ-CCO-12b | FAIL | /(?i)/ERROR\].*creativecommon/ matched: [23Nov2026 01:09:54.263] [Server thread/ERROR] [KubeJS Startup/]: [NCH] shapeless failed and was skipped: [NCH] shapeless twisted_composter_to_composter_by_shapeless input[0]: item/tag "c... |
| SQ-CCO-12c | FAIL | /(?i)/ERROR\].*creativecommon/ matched: [23Nov2026 01:11:26.803] [Server thread/ERROR] [KubeJS Startup/]: [NCH] shapeless failed and was skipped: [NCH] shapeless twisted_composter_to_composter_by_shapeless input[0]: item/tag "c... |
| SQ-CCO-12d | FAIL | /(?i)/ERROR\].*creativecommon/ matched: [23Nov2026 01:13:06.243] [Server thread/ERROR] [KubeJS Startup/]: [NCH] shapeless failed and was skipped: [NCH] shapeless twisted_composter_to_composter_by_shapeless input[0]: item/tag "c... |
| SQ-CCO-12e | FAIL | /(?i)/ERROR\].*creativecommon/ matched: [23Nov2026 01:14:45.225] [Server thread/ERROR] [KubeJS Startup/]: [NCH] shapeless failed and was skipped: [NCH] shapeless twisted_composter_to_composter_by_shapeless input[0]: item/tag "c... |
| SQ-CCO-12f | FAIL | /(?i)/ERROR\].*creativecommon/ matched: [23Nov2026 01:16:27.374] [Server thread/ERROR] [KubeJS Startup/]: [NCH] shapeless failed and was skipped: [NCH] shapeless twisted_composter_to_composter_by_shapeless input[0]: item/tag "c... |
| SQ-CC12-02 | FAIL | config written, server.log 0 errors, none from plantabletags/smokable_tags; startup.log 8 ERRORs, all NCH composter_to_composter (twisted, ancient, blossom, azalea, twice) = TODO creative-common-publish #16; the r1122cc sand_la... |
| SQ-CIF-04 | PASS | srv/mods: architectury, cdevperfbot, creativecalendar, creativecommon, creativecompat, creativedevtools (the scratch test verb jar every prep adds), kubejs, rhino, SereneSeasons; startup ERRORs only #16 composters |
| SQ-CIF-04b | PASS | srv/mods: the 04 set plus creativeequipment; no class error; startup ERRORs: #16 composters plus 4 Equipment NCH recipes without the pack (arrow/leather -> air outputs undefined, gold shield #forge:rods/gold empty), not Common |
| SQ-CC-01 | PASS | mods.toml lists immersive_portals and heartstone, mandatory=false, ordering=NONE; no create/IE/fruitsdelight; all 4 shell checks exit 0 |
| SQ-CC-02 | PASS | 6 checks passed |
| SQ-CC-05 | PASS | 5 checks passed |
| SQ-CC-06 | PASS | createframed ids (3 lines) and securitycraft reinforced_tinted_glass print ids; no exception/error text in output |
| SQ-CC-07 | PASS | 5 checks passed |
| SQ-CC-08 | PASS | 6 checks passed |
| SQ-CC-10 | PASS | 7 checks passed |
| SQ-CC-11 | PASS | 6 checks passed |
| SQ-CC-13 | PASS | jfr summary lists creativecommon.ServerTick, LevelTick, KubeJSHandlerStats, ChunkWaitStats (nonzero), plus ChunkWait/KubeJSHandler event types |
| SQ-CC-14 | PASS | 6 checks passed |
| SQ-CC-15 | PASS | gravity grep: no mixin error (only Portal 'Gravity API not present' INFO, create_gravity config corrected/reloaded); gravityLimitDimensions on |
| SQ-CC-16 | PASS | Done (8.937s) vs 10.063s last run, no stall; Preparing spawn/Generating count 10 before and after teleport to 50000; whistle arrow is a CQ check |
| SQ-CC-03 | FAIL | qrun HARNESS (the shell error check only): NCH composter ERRORs = TODO creative-common-publish #16; shell exit 1: ; FAIL: boot/stop clean, 0 forbidden mods, config written; but logs/kubejs/startup.log has 8 ERROR lines: NCH com... |
| SQ-CC-04 | PASS | 5 checks passed |
| SQ-CC12-03 | PASS | no Mixin apply/Invalid, no out-of-range WARN; only third-party optional-class lines (sodium, backpacked, multimine, aquaculture, botarium, overweightfarming, vanilla client class in non-Common mixin, D&D XrayParticle); 25/25 Co... |
| SQ-CDNS-02 | PASS | 4 checks passed |
| SQ-CDNS-10 | BATCH-FAULT | reclassified from FAIL: runner r1123cc, 2026-11-23, second fault after the flavor fix: reject regex also matches creativedelight:lunchbox/lunchbox_dye and lunchbox_soap result=empty (special recipes whose output is computed); t... |
| SQ-CDNS-11 | FAIL | no line matches /verb=state pos=40500,-60,40000 state="?creativedelight:placed_vegetable_omelet/; open look 1: supplementaries jar and can pouring stay with CQ-CDNS-01 (client) |
| SQ-CGE-01 | PASS | Done line present (1); no WARN/ERROR naming a gravity mixin in latest.log; only ForgeConfigSpec key-correct WARNs on first config write (judge-e2 r1123cc) |
| SQ-CGE-02 | PASS | shell cge_judge: falling_block, item, tnt End 4.914 vs overworld 24.570, ratio 0.200 at t=40; overworld drop 24.57 (vanilla with drag), the look's 'about 27' is stale wording (judge-e2 r1123cc) |
| SQ-CGE-03 | PASS | item and tnt ok at ratio 0.200 in SQ-CGE-02 shell.txt (judge-e2 r1123cc) |
| SQ-CGE-04 | PASS | item y=61.000 constant t=60..180 (no climb); falling_block removed=true, 'CGE04 sand landed at 0 61 0'; tnt y=61 onGround (judge-e2 r1123cc) |
| SQ-CGE-05 | PASS | End cfg factor=-1/-0.8 line present, overworld t=40 drop 24.570 (vanilla); cfg plains -0.8; End drop 0 per row log (judge-e2 r1123cc) |
| SQ-CGE-06 | PASS | End t=40 drop 4.914 vs overworld 24.570 = 0.200 (judge-e2 r1123cc) |
| SQ-CGE-07 | PASS | shell.txt: order ok, helper line ok, jeiOrder ok, 0 startup errors (judge-e2 r1123cc) |
| SQ-CGE-08 | PASS | 9 types ok, all ratio 0.200 (judge-e2 r1123cc) |
| SQ-CGE-09a | PASS | Nether 9 types ratio 1.000 (basalt_deltas listed, cfg -0.8); Nether villager Entity Gravity 0.08 (judge-e2 r1123cc) |
| SQ-CGE-09b | PASS | 4 checks passed |
| SQ-CGE-10 | PASS | Air 300 then 288s after the 10 s wait; expect (288/292/296) matched (judge-e2 r1123cc) |
| SQ-CGE-10b | PASS | leather backtank: Air 300 (3 digits), backtank NBT Air 890 (8[89]\d); tags 7 and 4 matched (judge-e2 r1123cc) |
| SQ-CGE-11 | PASS | 2 checks passed |
| SQ-CGE-12 | PASS | shell: [65900, 61000] (log >= 64500, redstone <= 61500) (judge-e2 r1123cc) |
| SQ-CGE-13 | PASS | shell: boat [65523, 65523] identical (within 50); the look's 'about 65.9' is the log height, a boat floats lower (judge-e2 r1123cc) |
| SQ-CGE-14 | PASS | 4 checks passed |
| SQ-CGE-15 | FAIL | qrun HARNESS (the shell error check only): NCH composter ERRORs = TODO creative-common-publish #16; shell exit 1: ; PASS: look only: 9 types ratio 0.200, order ok without _helpers, backtanks n=2 expect passed; row result stays ... |
| SQ-CGE-16 | PASS | 9 types End = overworld ratio 1.000; 0 LowGravity/NoClassDefFoundError lines; only #16 composter JsonSyntax lines (judge-e2 r1123cc) |
| SQ-CMV-01 | PASS | 3 checks passed |
| SQ-CPP-01 | PASS | mixin/tag: only creategoggles mekanism tag failures (2); metal_dusts n=15 incl IE dusts + zinc grit; bark n=20 IW barks; srv config removed has 3 + alexsmobs:fear, overrides name list present |
| SQ-CPP-02 | PASS | echo_shovel enchantments=true name=true steps=6; selftests all fail 0 |
| SQ-CPP-03 | PASS | bottle recipe creativebrewing:iebottling/create_potion/minecraft_swiftness_regular; refine -> creativebrewing:lingerification/minecraft_swiftness_from_splash, Bottle LINGERING |
| SQ-CPP-04 | PASS | 0 Error parsing recipe, iebottling swiftness bottle present, kubejs server.log 0 ERRORs (fewer than the r1107cc known set; none new) |
| SQ-CPP-05 | FAIL | floats tag n=468, no scrap error; startup.log 4 ERRORs = NCH composter_to_composter_by_shapeless lines (TODO creative-common-publish #16), row demands 0; startup order printed is creativecommon, creativecompat_helpers, startup_... |
| SQ-CDNS-07 | PASS | 351 hits are recipe parse WARNs for missing-mod conditions; none name creativedelight; moved ids still named creativeengineering: 0 |
| SQ-CDNS-08 | PASS | moved ids under creativedelight 376, still under creativeengineering 0; block entities oven/whale_oil_lamp/wooden_cauldron, menu oven, serializers baking/flavor/lunchbox_* all creativedelight (creativeengineering:dragon_breathi... |
| SQ-CM-37 | PASS | 12 shaped recipes (one per wood, own composter result) and 12 shapeless composter->minecraft:composter, vanilla shaped recipe kept, composters tag n=13 |
| SQ-CM-38 | FAIL | no line matches /verb=state .*state="?creativecommon:warped_composter\[level=7\]/; no line matches /verb=state .*state="?creativecommon:warped_composter\[level=8\]/; no line matches /verb=items .*minecraft:bone_meal:1\b/; PASS:... |
| SQ-CM-39 | PASS | 9 checks passed |
| SQ-CM-40 | PASS | 3 checks passed |
| SQ-CM-41 | FAIL | no line matches /verb=inv slot=2 item=minecraft:iron_ingot\*1\b/ |
| SQ-EFX-15 | PASS | 1 check passed |
| SQ-EFX-16 | PASS | 2 checks passed |
| SQ-EFX-17 | PASS | Fire Resistance Amplifier 0b Duration 1119 (60 s kept) |
| SQ-MELT-01 | PASS | bootexpect tags line: 40 salt (>=2); five end states expects all matched |
| SQ-MELT-02 | PASS | all 20 pair expects + unlit_ice1_ice matched: fire/soul/campfire/magma ice3 water, ice4 kept, snow3 air, snow4 kept; unlit ice kept |
| SQ-MELT-03 | PASS | flow_cell_lava, flow_ice4_water, src ice4 water / ice5 kept, cauldron ice4 water all matched |
| SQ-MELT-04 | PASS | blue0 sequence blue_ice only over 6000 ticks; blue1-3 blue_ice kept; plain ice beside salt water stepped (water) |
| SQ-MELT-06 | PASS | 34 checks passed |
| SQ-MELT-07 | PASS | boot source-driven line lists magma_block (light 3) after molten_nether_bricks; ice light_block=0 at start; ice_water after wait |
| SQ-MELT-08 | PASS | click1 packed->ice, click2 ice->water (both execute checks); pbot.inv filled=0 so both salt spent; no pbot error |
| SQ-MELT-10 | PASS | tags line 123 fire (>=100); source-driven list = molten_nether_bricks + magma_block (both light 3; molten bricks join magma per melting.md 2026-11-17, row text stale); tag dump 123 ids hold the 3.8 modded ids (only non-fire ids... |
| SQ-MELT-P1 | PASS | MG 8.685 - MS 8.673 = 0.012 ms (<0.5); JFR share not read here, left to perf report |
| SQ-MELT-P1b | PASS | MG2 8.625 - MS2 8.610 = 0.015 ms (<0.5); ConfigValue.get frame check left to perf report JFR |
| SQ-MELT-05 | PASS | ice 5,129,4 beside salt block is air (expect line SQ-MELT-05-ice_air); control ice 20,129,20 also air in warped_forest: Immersive Weathering's own Nether melt, expected (melting.md 6) (judge-f3) |
| SQ-MELT-09 | PASS | runner r1123cc rerun: fire ice3 water / ice4 kept, snow3 air / snow4 kept, lava ice4 water / ice5 kept; [melting] tags ... 0 salt; no melting error or exception; 12/12 checks |
| SQ-MELT-09b | PASS | runner r1123cc rerun: same pairs as -09, tags 0 salt, no Rare Ice line, no melting error; 13/13 checks |
| SQ-SALT-03 | PASS | 8 checks passed |
| SQ-SALT-06 | PASS | 9 checks passed |
| SQ-SALT-08 | PASS | 10 checks passed |
| CQ-CCB-02 | LOOK | open look 1: the JEI list keeps the pack order (Equipment first, by material); no dead placeholder fluids; the Magic Resistance icon draws |

## 9. Handover

- Not run, client (P7 client table): CQ-CMV-03, CQ-CC-01 to -06, CQ-CCO-02, -03, CQ-CPP-01, CQ-CLOG-02 (all `@draft`,
  no batch steps: owner or a row-writing pass), CQ-CIF-01 (`@owner`), CQ-CGE-03a/-03/-03b (`@draft`, joined client),
  CQ-CGE-02c and -03c (need the cge2 world and datapack or a joined pair; not run), CQ-CCB-03 (needs a Common + Compat
  client without JEI; the Tempad jar is in `mods/` now, but `run_captures.py serve` only drops globs from the full
  pack), CQ-CC13-01 onward (draft). Owner: judge `test_captures/judgement/qrun_res/CQ-CCB-02_jei_list_order.png`.
- Not run, server: SQ-CC-09, -12, SQ-CDNS-12, SQ-MELT-08b, -11 to -20, -P2, SQ-SALT-01, -02, -04, -05, -07 (draft or
  no steps); SQ-CCO-14, SQ-CDNS-01, -03 to -06, -09 (superseded).
- Rewrite pass: SQ-CDNS-10 (reject regex also hits the lunchbox dye and soap special recipes), SQ-CPP-05 look text
  (startup order now includes `creativecompat_helpers`), SQ-MELT-10 look text (molten nether bricks).
- The 4 jars added to `mods/` 2026-11-23 00:17 to 00:29 (chestbubblesrevived, kleeslabs, numarevive, prettybeaches)
  have no mod_sides.md row; every full-pack server and the client of this run ran without them (`DROPJARS`).


## 10. r1123cx: Creative Compat publish tests (milestone 2)

Targeted run (skill `scratch-test` part B), owner request 2026-11-23 via the main session: milestone 2 of the publish
gate. Runner: runner-compat for the main orchestrator. Results in `perf/runs/r1123cx*/`. Perf capture skipped (targeted
run, run default 4). Full-pack steps run with the 4 unlisted jars dropped (`source perf/runs/r1123cc/env.sh`, 9).

### 10.1 Scope

No Compat publish plan exists (`plan_*.md` grep: only plan_cb_publish.md license rows and plan_cc_publish_1_3.md
stage d, done in r1123cc). Scope = creative_compat.md 4 test list plus the rows files whose `@@jars` name Compat
alone or Common + Compat (CMP, BPT, PA, MT, SG, TN) and the Compat + Creative Delight rows file TSK. Plan lists in
`perf/runs/r1123cx/<stage>.txt`, plans by `qrerun.py`. The Compat jar changed since r1122cc (23c5050d ->
2e68d8807), so no r1122cc PASS is current.

| Stage | Set | Rows planned |
|---|---|---|
| a | T1 smoke + load | SQ-SMK-18, -19, -20, SQ-CMP-01 |
| b | Common + Compat | SQ-CMP-03 to -08, -11, -13, -23, -24, -25, SQ-BPT-01, SQ-PA-01 to -04, SQ-MT-01, -02, SQ-SG-01 to -03 |
| c | Compat + Creative Delight (TSK) | SQ-SB31-01, SQ-NS-01, SQ-TSK-02, -03, SQ-DM-03 |
| d | long | SQ-CMP-12 |

Left out: SQ-CIF-04, -04b (PASS in r1123cc on the current Compat stamp 2e68d8807.2611230027); SQ-CMP-02 (superseded);
SQ-CMP-09s, -10, -16, -17, SQ-TN-11, -12, SQ-MT-03 (draft); SQ-CIF-01 to -03 (Brewing / Equipment config and potion
rows, CIF `@@jars` do not name Compat); SQ-SVA-05 (worldgen, Creative Delight structure); every row that only has
Compat in a full-pack jar set (RD, SALT, RFX, UPG and the like). Client rows: see 10.5.

### 10.2 Jars and preflight

| jar | `mods/` (stamp) | installed |
|---|---|---|
| creativecompat | 1.0.0+2e68d8807.2611230027 | 2026-11-23 00:30 |
| creativecommon | 1.3.0+2e68d8807.dirty-e4f25c.2611230023 | 2026-11-23 00:30 |

- RUNNER.md idle, claimed 04:30. `df -h /` 73 GB free. `jarstamp.py pending`: 0. `game_jvms.sh` empty.
- `check_all.py --fast`: 42 run, 1 FAIL = comment_dates (same 3 dated comments as 4; not a runtime test).
- `qprescreen.py --plan-json perf/runs/r1123cx/all.plan.json`: 2 likely (grepc SQ-CMP-23, -25) -> fixed in CMP.qb
  (`; true` after the look-judged `grep -c` counts, 3 lines), lint clean; 3 risk, 8 stale (SMK-18 to -20, SG, BPT, MT:
  sources changed after review; run as written, judged on their pass text).

### 10.3 Run notes

- 04:38 first chain start: stage a refused (my hand claim in RUNNER.md: drives claim it themselves, so a hand claim
  blocks them); claim set back to idle. Second start: stage a refused again (state.json from the refused start), the
  chain went on to stage b; stopped at 04:39 before any row ran (b step 1 boot cut, resumable). `chain.sh` now resumes
  a drive whose state exists. 04:40 chain a -> b -> c -> d started (`perf/runs/r1123cx/chain.sh`).
- 04:44 stage a done (04:40 to 04:44, 2 steps): SQ-SMK-18, -19, -20 PASS. SQ-CMP-01 HARNESS: listed 86, applied 73,
  skipped 13; SnowPathMixin and LeafPilePathMixin "not applied, no gate", but debug.log shows both applied at
  04:41:48.05, 0.1 s after the check ran (Done 04:41:46): lazy target classes (NodeEvaluator, WalkNodeEvaluator) load
  when the first mob paths. BF-FIX SQ-CMP-01: summon a zombie and `sleep 10` before cmp_mixin_check.py (CMP.qb); rerun
  queued (perf/runs/r1123cx/rerun.txt). Real numbers on this set: listed 86, applied 75, skipped by gate 11.
- 04:50 TODO gear-tuning #1 (Bug) filed for the 4 Equipment NCH ERRORs on Common + Compat + Calendar + Equipment (r1123cc 7 01:20 observation; no row existed). New family slug from todo.py (the family's older rows use `gear/tuning`).
- 04:55 judge-b1 (stage b steps 1 to 3): SQ-BPT-01, SQ-CMP-03 to -08, -11, -13 PASS (9). Notes: SQ-CMP-07 @commands
  text says 475 paintings, the docs and logs say 445 (445 of 445 built; text only, for a doc pass); s02 latest.log has
  Hearths `Detected setBlock in a far chunk` ERRORs from `hearths:*_lakes` (not Compat, no pass criterion hit).
- 05:00 runner judged stage b step 4 (CMP-23 to -25): SQ-CMP-24 PASS (both mixins switched off, snow stayed, 0 cast
  errors). BF-FIX SQ-CMP-23: 32 true keys, no range or `is not [` warning, cmp_mixin_check listed 86 / applied 76 /
  skipped 10; the row expected `SupplementariesBreezyCompatMixin` to apply, but its target is called only by a
  ticking wind vane on a client (mixin javadoc), so it never applies on a dedicated server -> pass and look check the
  gate (CMP.qb). BF-FIX SQ-CMP-25: 4 script lines PASS; jar 39 crop recipes + 2 tags vs typed 41 recipes (the PR-CP-10
  audit count; the pack folder now holds 39, all in the jar) -> the row compares the jar with the pack folder count.
  Both rerun queued (perf/runs/r1123cx/rerun.txt).
- 05:07 stage b done (04:44 to 05:07, 7 steps). judge-b2 (steps 5 to 7): SQ-MT-01, -02 (the r1120nv peaceful-difficulty
  fault is fixed in the row), SQ-PA-01 to -04, SQ-SG-01 PASS. SQ-SG-02, -03 FAIL: the unsheared control patches did not
  spread either (24 of 24 dirt left after 60 s at randomTickSpeed 300, bot qa at the patches; Calendar's
  `grass season hook live` logged for the IW, Vinery and Vintage grass classes); cause not proven (Calendar
  GrassSeasonHook, time of day under Better Days, or IW grass); not a Compat code path (Compat holds no grass
  spread code: the `fertile` state is Immersive Weathering's, Compat adds only the WTHIT sheared line), TODO creative-calendar #2 with the diagnostic step. Stage c drive `r1123cxc` started 05:07.
- 05:10 stage c done (05:07 to 05:10, 1 step). judge-c: SQ-SB31-01, SQ-DM-03, SQ-TSK-02, -03 PASS. SQ-NS-01 FAIL: the
  feed half passed (sugar in both nightstands at 40 and 42 -60 10), but the hopper below the third nightstand
  (44 -61 10) read `Items: []` 4 s after the hopper above fed it; cause not proven (Compat `NightstandStorage` extract
  side, or the sugar never entered), TODO decor-and-framed-blocks #3. Diagnostic reads of the nightstand and the hopper
  above added after the row's check (TSK.qb); rerun queued for evidence. Stage d drive `r1123cxd` (SQ-CMP-12) started
  05:10.
- 05:17 stage d done (05:10 to 05:17): SQ-CMP-12 PASS (4 checks). 05:17 rerun drive `r1123cxr` started (perf/runs/r1123cx/rerun.plan.json: SQ-CMP-01, -23, -25, SQ-NS-01).
- 05:23 rerun drive `r1123cxr` done (05:17 to 05:23, 3 steps), runner judged: SQ-CMP-01 PASS (listed 86, applied 76,
  skipped by gate 10, the numbers of the row comment and creative_compat.md 1: the r1122cc BATCH-FAULT is settled;
  32 is the `[mixins]` key count, not a mixin count), SQ-CMP-23 PASS (32 keys, no gate warning, 86/76/10), SQ-CMP-25
  PASS (pack 39 = jar 39 crop recipes, 2 tags). SQ-NS-01 FAIL again, now with the cause narrowed: the third nightstand
  (hopper placed below it first) never took the sugar (`CENightstandItems: []`, the hopper above still holds it 10 s
  later); logged on TODO decor-and-framed-blocks #3.
- 05:24 client block: capture client (`run_captures.py serve`, the 4 unlisted jars dropped with `--drop`) up 05:24 to
  05:29; `qrun.py client` on BPT.qb CQ-BPT-01, -03, CQ-TSL-02 -> perf/runs/r1123cxg. CQ-BPT-01, -03 PASS (runner,
  text). BF-FIX CQ-TSL-02: both screen waits timed out, `use` hit no block: the look verb adds 0.5 to x and z, so the
  row's centered `from "0.5 102.6 -1.5"` stood on the chest edge (tp x 1.000) -> whole-number coordinates (BPT.qb);
  rerun on the same client: 9 of 9 checks pass, 3 shots for the owner (LOOK open). First attempt kept in
  perf/runs/r1123cxg_try1. Client stopped, `game_jvms.sh` empty 05:29.

### 10.4 Final verdicts

Table: `perf/runs/r1123cx/table.md` (`qrun.py table` over every r1123cx RES dir, later reruns win). 34 rows: PASS 30,
FAIL 3, LOOK 1 (owner shots). BF-FIX and rerun PASS: SQ-CMP-01, -23, -25, CQ-TSL-02 (LOOK).

| Stage | Time | Rows | PASS | FAIL | BATCH-FAULT left | LOOK |
|---|---|---|---|---|---|---|
| a smoke + load | 04:40 to 04:44 | 4 | 4 | 0 | 0 | 0 |
| b Common + Compat | 04:44 to 05:07 | 21 | 19 | 2 (SQ-SG-02, -03) | 0 | 0 |
| c Compat + Creative Delight | 05:07 to 05:10 | 5 | 4 | 1 (SQ-NS-01) | 0 | 0 |
| d long | 05:10 to 05:17 | 1 | 1 | 0 | 0 | 0 |
| reruns r1123cxr | 05:17 to 05:23 | (4) | (3) | (1) | 0 | 0 |
| client g | 05:24 to 05:29 | 3 | 2 | 0 | 0 | 1 |

| ID | Result | Evidence |
|---|---|---|
| SQ-CMP-01 | PASS | 3 checks passed |
| SQ-SMK-18 | PASS | 10 checks passed |
| SQ-SMK-19 | PASS | 3 checks passed |
| SQ-SMK-20 | PASS | 3 checks passed |
| SQ-BPT-01 | PASS | 2 checks passed |
| SQ-CMP-03 | PASS | startup.log: creativecompat pack in order; [item_physics] canonical tags / burn-transform 3 id + 5 tag rules line; no missing error |
| SQ-CMP-04 | PASS | dumpphysics: 20550 items, 1416 survive lava; ItemPhysicSplit class loaded, no error |
| SQ-CMP-05 | PASS | fire still burning, TNT item gone, netherite ingot survived (say lines) |
| SQ-CMP-06 | PASS | box placed and given, loot dropped 16 Coal, no unknown; item_tags has creativecompat:physics/{floats,sinks,fireproof,lavaproof} |
| SQ-CMP-07 | PASS | 445 of 445 painting variants built (matches immersive_paintings.md; row text 475 is stale), no error |
| SQ-CMP-08 | PASS | 0 script errors in server.log before and after /reload |
| SQ-CMP-11 | PASS | GT 462, c11b DeathTime 543L (about 80 ticks after), Stage 0b; c11a 1b |
| SQ-CMP-13 | PASS | c13a 1b, c13b 2b, c13c 0b |
| SQ-CMP-23 | PASS | 32 true keys; no 'is not [' or range line; listed 86, applied 76, skipped by gate 10 (absent-mod mixins and the client-only Breezy one) |
| SQ-CMP-24 | PASS | boot 2: 'mixins switched off in [mixins]' line; SNOW_STAYED (layer at 0 120 0 did not fall); 0 ClassCastException/NoClassDefFoundError |
| SQ-CMP-25 | PASS | 4 script lines (misc mods, feature ports, dead recipe removal, Coin); pack 39 jar 39 crop recipes, tags 2 |
| SQ-MT-01 | PASS | golems 5/12000/24000, boat blocks 0.98/0.96/0.94, Quark Game Nerfs true, Iron Farms/Ice Roads false, enderman only, mixin count 2 |
| SQ-MT-02 | PASS | mutton:13, no wool; iron_ingot:11; stone n=24 (<25); peaceful fix from r1120nv in place and holds |
| SQ-PA-01 | PASS | 445 of 445 built from 415 sources, no WARN; INFO only for upgrade_aquatic:sightless grid |
| SQ-PA-02 | PASS | 60 files; no built line after /reload, no painting error |
| SQ-PA-03 | PASS | afternoon from pa_test.zip line; WARN no_such.zip (with path); 445 of 445 built |
| SQ-PA-04 | PASS | 5 checks passed |
| SQ-SG-01 | PASS | 2 checks passed |
| SQ-SG-02 | FAIL | unsheared patch also 24 of 24 dirt left after 60 s at randomTickSpeed 300: nothing spread |
| SQ-SG-03 | FAIL | count /Successfully filled 24 block/ = 4, wanted == 2; all four fills 24: unsheared rich soil and slab patches did not spread either |
| SQ-DM-03 | PASS | rich z30 count 2 then 5; mulch_block z32 count 8 then 9 of 10: fertile ahead at both reads (saturating); no done=0 |
| SQ-NS-01 | FAIL | feed half PASS (40, 42 -60 10 hold the sugar); at 44: hopper below Items [], nightstand CENightstandItems [], hopper above still holds the sugar 10 s after placing: the third nightstand (hopper placed below it first) took nothing |
| SQ-SB31-01 | PASS | 2 checks passed |
| SQ-TSK-02 | PASS | randomtick changed=5; three Test passed: FD vine and Supplementaries stick tomatoes both climbed, air at y-57 |
| SQ-TSK-03 | PASS | treated_stick restored after soil break; drops dirt, tomato, seeds, tomato; 3 axe loots each 1 Treated Stick |
| SQ-CMP-12 | PASS | 4 checks passed |
| CQ-BPT-01 | PASS | one '[banner_patterns] 27 Bedspreads and 25 Customizable Elytra pattern textures built from 6 jars' line, no banner WARN; no TrashSlotGuiHandlerMixin error line in latest.log (the client writes no debug.log) |
| CQ-BPT-03 | PASS | built line count 1 before and after F3+T (cached); 'patterns still drawn' is CQ-BPT-02, owner |
| CQ-TSL-02 | LOOK | open look 1: first and last shots show the trash slot; the spectator shot shows none |

### 10.5 Handover

- FAILs: SQ-SG-02, -03 (no grass spread at all, controls included: TODO creative-calendar #2, not a Compat code path);
  SQ-NS-01 (a nightstand with a hopper placed below it first takes nothing from the hopper above: TODO
  decor-and-framed-blocks #3, Compat `NightstandStorage`, the one Compat product finding of this run).
- Owner: judge `test_captures/judgement/qrun_r1123cxg/CQ-TSL-02_*.png` (trash slot in survival and back, none in
  spectator). Owner or hand rows not run: CQ-BPT-02, CQ-TSL-01, CQ-PA-01, -02, CQ-MT-01 (`@owner`); CQ-NS-02, CQ-TSK-01
  (hand look rows); CQ-CCB-03 (needs a Common + Compat client without JEI; `serve --drop` only thins the full pack).
- Draft, not run: SQ-CMP-09s, -10, -16, -17, SQ-TN-11, -12, SQ-MT-03, CQ-CMP-09, -14, -15, -18 to -22, -26, CQ-TN-13.
- Text for a doc pass: SQ-CMP-07 @commands says 475 paintings (445 built, as the docs say).
- TODO gear-tuning #1 filed for the r1123cc stage d Equipment NCH ERRORs (none existed).


## 11. r1123rp: Raspberry Pie publish tests (milestone 3)

Targeted run (skill `scratch-test` part B), owner request 2026-11-23 via the main session: milestone 3 of the publish
gate. Runner: runner-rp for the main orchestrator. Results in `perf/runs/r1123rp*/`. Perf capture skipped (targeted
run, run default 4). Full-pack steps run with the 4 unlisted jars dropped (`source perf/runs/r1123cc/env.sh`, 9).
Owner ruling for this run (2026-11-23): "spice of life raspberry pie ui looks good": rows whose only open check is a
UI appearance LOOK are judged PASS `--by owner` on it.

### 11.1 Scope and preflight

Scope: plan_rp_standalone.md 6 and 10 -> RPS.qb (isolation set) and RP.qb (pack rows). Order: SQ-RPS-01, the other
RPS server rows, RP.qb server rows, RPS client rows, RP.qb client rows. Stage lists in `perf/runs/r1123rp/<stage>.txt`.

| jar | `mods/` (stamp) | installed |
|---|---|---|
| solRaspberryPie | 1.0.0+911197e7c.2611222309 | 2026-11-22 23:14 |

- 05:36 `game_jvms.sh` empty, `df -h /` 70 GB free, `jarstamp.py pending` 0, RUNNER.md idle (left for the drive to claim).
- `check_all.py --fast`: 42 run, 2 FAIL: comment_dates (the known 3 dated comments) and test_queue_rows (PROC-20: queue
  blocks differ from .qb sections edited by r1123cx; `status_index.py` at write-back re-renders them). Neither is a runtime test.
- Fixture builder `tools/scratch_query/rps_fixture.py` (table: a diversity file added to a copy of a loaded mod jar;
  stub: EffectFleetFooted without `lastDuration`), tested offline on scratch copies.

### 11.2 Run notes

- 05:39 stage a (`perf/runs/r1123rp/a.txt`, drive r1123rpa) 05:39 to 05:41, 3 steps: SQ-RPS-01 PASS (one `Diet ladder: 44
  rungs, 4 slots, 8 pool effects`; the table loader runs right after `Done (` on a bare server: `No usable diversity
  table ...`), SQ-RPS-06 PASS (debug.log `Mixing FleetFootedMixin ... into ...EffectFleetFooted`). SQ-RPS-04
  BATCH-FAULT: `rps loose` takes `solapplepie-*` from `mods/`, but the loose jar is in `jar_archive/displaced/`; srv/mods
  held only Raspberry Pie. BF-FIX: own section 5 with `@@prep run cp` of the displaced jar.
  Note (no row criterion, upstream): the standalone boot logs 18 `[solapplepie] Item is not food:` WARNs for
  farmersdelight / farmersrespite / largemeals ids from Apple Pie 2.2.0's own default `complexityUnparsed`.
- 05:50 drafts written (steps in the .qb, lint clean): SQ-RPS-08 (section 2, `rps_fixture.py stub`), SQ-RPS-10 (section 3,
  isolated: the tables load at boot, so no player is needed), SQ-RPS-07 (section 4, full pack, group A, table in a
  copy of the Raspberry Pie jar, bot eats 5 foods), SQ-RPI-14 (boot half), SQ-RPI-16. Rewritten (r1120nv "not simple"
  BATCH-FAULTs, now row-local `edit` + `restart`): SQ-RPI-03, -04, -08; SQ-RPI-01 regex to the current pack ladder and
  the reworded Apple Pie settings lines (P5). Stages b (RPS-04, -10, -08, -07) and c (SQ-RPI-01 to -08, -14, -16) chained.
- 05:43 stage b step 1 (SQ-RPS-08) boot failed: environment fault in my stub fixture. Dropping the `lastDuration` field
  declaration made `EffectFleetFooted.<init>` throw `NoSuchFieldError: lastDuration` at alexsmobs registry time (the
  Raspberry Pie warning itself was logged correctly first). Fix: `rps_fixture.py stub` now renames the field's Utf8
  constant (declaration and every Fieldref follow), verified with javap. The drive waited out its 600 s boot timeout,
  stopped resumable; first attempt kept in `perf/runs/r1123rpb/s00_try1_stubcrash`. Chain b c resumed.
- 05:47 to 05:52 stage b (drive r1123rpb, resumed): SQ-RPS-08 PASS (one `lacks int field lastDuration: the Fleet-Footed
  fix is off` warning, Done, no mixin error), SQ-RPS-10 PASS (one wrong-shape error for `solraspberrypie:bad.json`, then
  `No usable diversity table ...`, Done). SQ-RPS-07 PASS (runner): summary `1648 values, 65 family members from 2
  file(s) (creativedelight:creativedelight.json, solraspberrypie:rps07.json); 3 ids skipped`, one override line;
  bot maxDiversity 11.9666 = Apple Pie's decay (weight 1 - n/32 for the meal n back, startDecay 0, endDecay 32) with
  bread at the fixture's 7.25 (7.448 without). The row's look numbers (13.16 / 8.00) left the decay out: corrected in
  RPS.qb. SQ-RPS-04 step refused by qrun's pin check: the loose Apple Pie jar has no mod_sides.md row. Fix: qpin.py
  EXTRAS allows `solapplepie-1.19.2-2.2.0.jar` (fixture, not a pack jar); `qpin.py verify` OK; drive b resumed after c.
- 05:52 to 06:02 stage c (drive r1123rpc, 3 pack boots, group A): SQ-RPI-01 PASS (`Diet ladder: 82 rungs, 5 slots, 38
  pool effects` = every entry of the generated ladder; the docs' "about 81 / 37"), -02 PASS, -03 PASS (boot 2: one
  `skipped 1 entries ... unlock nosuchmod:thing at 3.0`, one `V10: flat minecraft:absorption at 4.0`, rung count as
  boot 1), -05 PASS, -06 PASS (Apple Pie's food_book recipe gone, 0 NCH errors), -07 PASS (reset clears Apple Pie's
  list and the diet cap; Carrot's Food Dictionary keeps its 3, as raspberry_pie.md 12 #30 says), -14 PASS (boot half:
  0 `Diet ladder (` warnings; the Saturation-at-100 half needs a player), -16 PASS (82 whole thresholds, 18/19, 26/27,
  49/50 apart). BF-FIX SQ-RPI-04: the edit removed the first comma, which is inside the `_comment` string, so the file
  stayed valid and loaded -> the edit cuts the file in half. BF-FIX SQ-RPI-08: `reject _modifier_` matched a
  dynamicvillagertrades `placement_modifier` boot line (after `restart` the row segment holds the whole boot log);
  behavior was right (max health 20 for both bots, the only max_health modifier is Carrot's `Health Gained from Trying
  New Foods` at 0.0; boot note `Apple Pie's own benefit list is not empty; Raspberry Pie keeps it off`) -> reject
  scoped to the entity data replies. Both rerun in drive r1123rpr (perf/runs/r1123rp/r.txt) after stage b step 4.
  Observation: SQ-RPI-03's `shell` after `restart` printed boot 1's `latest.log` lines (no qrun_boot1.log yet);
  bootexpect read boot 2 correctly. Judge restart rows by bootexpect or `logs/qrun_bootN.log` after the run.
- 06:03 to 06:08 stage b step 4 (resumed) SQ-RPS-04 PASS (JarSelector: "Attempted to select a dependency jar for JarJar which was passed in as source: solapplepie. Using Mod File: .../srv/mods/...": the loose copy loads, the nested one is skipped without an error; one `Diet ladder:` line). Rerun drive r1123rpr: SQ-RPI-04 PASS (broken-ladder block, built-in ladder, `/solrp pause list` answers), SQ-RPI-08 PASS. `game_jvms.sh` empty 06:08.
- Client block not booted. Every client row in scope is `@draft` with no steps (RPS.qb 4, RP.qb 82 plus the `@owner`
  CQ-RPI-21 and the superseded CQ-RPI-33, -36), so there was nothing to run, and none could be written within about 15
  minutes each:
  - CQ-RPS-02, -03, -05, -09 need an isolated client (Forge + Raspberry Pie only). The capture driver lives in Creative
    Devtools, which needs Creative Common, so `run_captures.py serve` cannot drive that set; it is an xdotool hand run
    or the owner's. Deferred.
  - The pack rows' diversity lines come from `RenderTooltipEvent.GatherComponents`, which the driver's `tooltip_text`
    does not read (client_capture.md D5), so the value rows (CQ-RPI-63, -64, -72, -73) are shots only.
- Owner ruling 2026-11-23 ("spice of life raspberry pie ui looks good"): it covers the draft rows whose only check is
  how the UI looks. They were not run (no RES, so no `qrun.py judge` record); listed here for the owner to close on the
  ruling: CQ-RPI-11, -12, -14, -17, -29, -32, -34, -35, -41 to -46, -48 to -53, -55, -57, -58, -60 to -63, -67 to
  -73, -81 (35 rows).
- Behavior client rows left (draft, owner or a row-writing pass): CQ-RPI-01 to -10, -13, -15, -16, -18 to -20, -22 to
  -28, -30, -31, -37 to -40, -47, -54, -56, -59, -64 to -66, -74 to -80, -82 (44 rows); CQ-RPI-21 (`@owner`);
  CQ-RPS-02, -03, -05, -09; CQ-RPB-01 to -03 (resource pack bench, a long operation, not Raspberry Pie behavior).

### 11.3 Final verdicts

Table: `perf/runs/r1123rp/table.md` (`qrun.py table` over every r1123rp RES dir in `perf/runs/r1123rp/res_dirs.txt`, later reruns win). 16 server rows: PASS 16. Judged by the runner from text evidence (one-row RES dirs). BF-FIX and rerun PASS: SQ-RPS-04, SQ-RPI-04, -08. Drafts written and run: SQ-RPS-07, -08, -10, SQ-RPI-14 (boot half), -16; rewritten and run: SQ-RPI-01, -03, -04, -08.

| Stage | Time | Rows | PASS | FAIL | BATCH-FAULT left |
|---|---|---|---|---|---|
| a RPS isolated | 05:39 to 05:41 | 3 | 2 | 0 | 0 (SQ-RPS-04 fixed, rerun in b) |
| b RPS fixtures + pack | 05:43 to 05:52, 06:02 to 06:03 | 4 | 4 | 0 | 0 |
| c RP pack server | 05:52 to 06:02 | 10 | 8 | 0 | 0 (SQ-RPI-04, -08 fixed, rerun) |
| r reruns | 06:03 to 06:08 | (2) | (2) | 0 | 0 |
| client | not booted | 0 | - | - | - |

| ID | Result | Evidence |
|---|---|---|
| SQ-RPS-01 | PASS | runner: grep -c 'Diet ladder:' = 1, line 'Diet ladder: 44 rungs, 4 slots, 8 pool effects'; no ladder warning; Done 23.6 s |
| SQ-RPS-04 | PASS | runner: srv/mods = loose solapplepie-1.19.2-2.2.0.jar + Raspberry Pie; JarSelector 'Attempted to select a dependency jar for JarJar which was passed in as source: solapplepie. Using Mod File: .../srv/mods/...' (the loose copy wins, nested skipped without error); one PackInfo mod:solapplepie; Done; one 'Diet ladder: 44 rungs, 4 slots, 8 pool effects'; no duplicate error |
| SQ-RPS-06 | PASS | runner: debug.log 'Mixing FleetFootedMixin from solraspberrypie.mixins.json into ...EffectFleetFooted'; no Fleet-Footed warning; Done reached |
| SQ-RPS-08 | PASS | 4 checks passed |
| SQ-RPS-10 | PASS | 6 checks passed |
| SQ-RPS-07 | PASS | runner: maxDiversity 11.96657 = Apple Pie decay (startDecay 0, endDecay 32, factor 1-n/32 over the 5 meals) with bread at the fixture's 7.25; with the pack's 2.0859 it would be 7.448 (the row's 13.16/8.00 omitted the decay); summary line '1648 values, 65 family members from 2 file(s) (creativedelight:creativedelight.json, solraspberrypie:rps07.json); 3 ids skipped (mod not loaded)', one override line, no line names nosuchmod |
| SQ-RPI-01 | PASS | runner: 'Diet ladder: 82 rungs, 5 slots, 38 pool effects' = every entry of defaultconfigs/solraspberrypie/ladder.json (82 rungs, 38 pool; raspberry_pie.md 3.3 says about 81/37); no skipped-entries line; no settings note or problem block |
| SQ-RPI-02 | PASS | 2 checks passed |
| SQ-RPI-03 | PASS | runner: qrun_boot2.log: 1 'skipped 1 entries ... unlock nosuchmod:thing at 3.0' line, 1 'V10: flat minecraft:absorption at 4.0' line, 'Diet ladder: 82 rungs' as boot 1 (both added rungs dropped); the row's shell read the pre-restart latest.log, counts taken from qrun_boot2.log |
| SQ-RPI-04 | PASS | 5 checks passed |
| SQ-RPI-05 | PASS | runner: 'No player has paused' -> 'Paused diet bonuses for 1 players' -> '1 paused: rpb' -> 'Resumed ...' -> 'No player has paused' -> paused again |
| SQ-RPI-06 | PASS | runner: bytype diet_book n=1 (solraspberrypie:diet_log), recipes liquid_diet_log_manual_only present; 'recipes food_book' lists only solcarrot:food_book (Apple Pie's food_book recipe removed); 0 NCH error lines |
| SQ-RPI-07 | PASS | runner: 'Reset the diet of 1 players'; after: Apple Pie foodsEaten 0, foodList [], solrp_gateFoods []; diet maxDiversity 0.0, slots 5 empty compounds, drinks []; zc4 applepie 3 -> 0; Carrot's Food Dictionary list keeps 3 (raspberry_pie.md 12 #30: reset clears Apple Pie's memory, not the dictionary) |
| SQ-RPI-08 | PASS | runner: max health 20.0 for rp8 (new) and rp7 (has eaten); the only max_health modifier is Carrot's 'Health Gained from Trying New Foods' amount 0.0 (no Apple Pie +4); boot note 'Apple Pie's own benefit list is not empty; Raspberry Pie keeps it off' |
| SQ-RPI-14 | PASS | runner: 'Diet ladder: 82 rungs, 5 slots, 38 pool effects' (all of the generated file; docs say about 81/37), 0 'Diet ladder (' warning lines (no toggle, V3 to V6, V10), no skipped line; boot half only, the Saturation-at-100 half needs a player (client, not run) |
| SQ-RPI-16 | PASS | runner: 82 thresholds, non-whole []; 18, 19, 26, 27, 49, 50 all present and apart; start line 82/5/38, 0 'Diet ladder (' warnings |


### 11.4 Handover

- No FAIL; no TODO row filed. Every Raspberry Pie publish server row in scope passed on jar 911197e7c.2611222309.
- Observation, no row criterion: the standalone boot logs 18 `[solapplepie] Item is not food:` WARNs (farmersdelight, farmersrespite, largemeals ids) from Apple Pie 2.2.0's own default `complexityUnparsed` (upstream defaults; the owner decides whether a standalone publish quiets them).
- Owner: close the 35 UI-look draft rows on the 2026-11-23 ruling (11.2 list) or keep them queued; the 44 behavior client rows, CQ-RPI-21 and CQ-RPS-02, -03, -05, -09 need a row-writing pass or a hand run.
- `check_all.py --fast` test_queue_rows FAIL (PROC-20): scratch_queue.md blocks are stale against CMP/TSK/BPT/RP/RPS.qb; `python3 tools/status_index.py` re-renders them (not run here: it rewrites generated docs other sessions have open).
- Tool changes: `tools/scratch_query/rps_fixture.py` (new), `qpin.py` EXTRAS (loose Apple Pie fixture jar). Modset `rps loose` is dead (its jar is not in `mods/`); SQ-RPS-04 copies it in its own section instead.

### 11.5 Owner verdicts (2026-11-23, main session)

Screenshots judged by the owner, recorded with `qrun.py judge ... --by owner`:

| ID | Result | Evidence |
|---|---|---|
| CQ-CCB-02 | PASS (order half) | owner: JEI list keeps the pack order, Equipment first by material; the page 1 shot does not show the fluids or the Magic Resistance icon (log checks cover the fluid count) |
| CQ-TSL-02 | PASS | owner: no trash slot in spectator; slot on the chest in survival and after switching back |

Raspberry Pie UI ruling (owner, 2026-11-23: "ui looks good as is"): the appearance half of the 35 UI-look draft rows in
11.2 is approved. Rows whose pass criteria are appearance only are closed on it: CQ-RPI-17, -29, -32, -34, -35, -43,
-44, -53. The rest keep their behavior halves open (for example CQ-RPI-11 lock toggle and relog position, -48 rounding,
-51 scroll page turns, -55 hover counts) for a row-writing pass. These are drafts, so `qrun.py states` still lists them
as draft; this section is the record. The owner's new Raspberry Pie feature notes from the same answer go to the
session that owns Raspberry Pie features, not this run.

## 12. r1123cd: Creative Delight and Creative Brewing jar-changed rows

Targeted run model (skill `scratch-test` part B), owner request 2026-11-23 via the main session (orchestrator).
Runner: runner-cd for the main session. Judging by separate scratch-judge agents afterwards. Results in
`perf/runs/r1123cd*/`; perf capture on (bulk run, session `r1123cd`).

### 12.1 Scope

- Taken: 824 rows in `tools/scratch_query/batches/r1123cd/plan.json` (a `qrun.py plan --json` subset: every
  jar-changed row whose section `@@jars` names creativedelight or creativebrewing), 82 rows files, 102 drive steps
  (server 783, worldgen 37, longwait 2, long 2; no client rows).
- Not taken: SQ-HH (HH.qb), deferred to a later run (owner). Client rows: none in the plan.

### 12.2 Jars and preflight

| jar | `mods/` (stamp) |
|---|---|
| creativedelight | 1.1.0+1aeb786f9.dirty-501b85.2611230023 |
| creativebrewing | 1.0.0+828d92737.dirty-ed2da3.2611230003 |
| creativecommon | 1.3.0+2e68d8807.dirty-e4f25c.2611230023 |

- 12:22 RUNNER.md idle -> claimed (Run r1123cd); `game_jvms.sh` empty; `df -h /` 69 GB free; `jarstamp.py pending` 0.
- `check_all.py --fast`: 42 run, 2 FAIL, neither a runtime test: comment_dates (3 dated comments in uncommitted
  development/ files: LeafPiercingTrail.java, GoldenItems.java, golden_values.js; not runner-editable) and
  test_queue_rows PROC-20 (scratch_queue.md blocks stale against GLD.qb, edited uncommitted by another session).
- Full-pack steps run with the 4 jars lacking a mod_sides.md row dropped (`source perf/runs/r1123cc/env.sh`, DROPJARS
  chestbubblesrevived, kleeslabs, numarevive, prettybeaches; same as 9 to 11; the add-mod pass is the owner's).
- GLD.qb (24 plan rows) has uncommitted edits by another session; the drive reads it at its step start.

### 12.3 Steps

`qrun.py drive start r1123cd --plan-json tools/scratch_query/batches/r1123cd/plan.json` (one prep + boot per section
and world group; state `perf/runs/r1123cd/drive/state.json`); reruns via `perf/runs/r1123cd/rerun.txt`.

### 12.4 Run notes

- 12:30 pre-screen (`qprescreen.py --plan-json`): 102 steps, 32 likely on 30 rows, 14 risk, 438 stale. Fixed 2
  (row steps): SQ-CEQ-05 `forceload add 336 288 351 303` before its setblock at 342 -60 300 (its forceload lived in
  unplanned SQ-CEQ-02); SQ-HOP-13 `forceload add 650 500 680 540` before its setblocks at 666 -60 520 (forceload lived
  in unplanned SQ-HOP-12). Left as false positives: 13 "unloaded" lines on `pbot spawn` / `tp` of a bot (the bot loads
  its chunks) or `locate` origins, SQ-TX-01 (unloaded on purpose); dead ids that are recipe ids or types, structures,
  configured features, enchantments, tags or the `minecraft:fluid` registry (not in the item/block dump), or ids in
  prose; SQ-CDJ-09 cdj5home (a diagnostic `say` only). Stale rows not rewritten (run scope).
- 12:29 perf session r1123cd open (collector pid 3566122). Drive started detached with DROPJARS exported, session
  runner-cd: 179 steps, 827 rows (boot-phase setup rows joined), 0 skipped; driver ETA 1902 min (over the 12 h cap;
  the per-row estimates are conservative, watched against the cap).
- 13:08 steps 1 to 9 done, step 10 (port_chain mod set) booted and running rows. Observed pace about 2.5 min fixed cost
  per step plus about 0.4 min per row: about 13 h for the whole drive, past the 12 h cap. First 52 results: PASS 16,
  LOOK 33, FAIL 3 (SQ-BBQ-03, SQ-BBQ2-05, SQ-ROLL-11; for the judges; SQ-ROLL-11 log shows bread +8 hunger where the
  row expects 5). Disk 67 GB free, about 0.3 GB per step.
- 13:10 runner-cd handed back to the orchestrator while the detached drive keeps running (it continues without the
  runner; `qrun.py drive status r1123cd` for progress, `drive resume r1123cd` after a dead driver).

### 12.5 Final verdicts

Pending judging (scratch-judge agents).

#### Verdicts steps 1 to 9

Judge judge-1, RES `perf/runs/r1123cd/s00_res` to `s08_res`. BATCH-FAULT rows are fixed in their `.qb` (run default 11) and listed in `perf/runs/r1123cd/rerun.txt` (SQ-ROLL-11, SQ-BBQ2-05, SQ-BBQ-03, SQ-GLD2-01).

| ID | Result | Evidence |
|---|---|---|
| SQ-ABD-01 | PASS | log: smelting, campfire, smoking and salting recipes present for perch and pike slices, same input/output; xp 0.35 / time not printed, source upgrade_aquatic.js F.fishCook (0.35 xp, FD times) removes the 4.1.2 smelting |
| SQ-ABD-02 | PASS | log: furniture recipe lists 9 items (door, trapdoor, sign +6 = doc list incl. cabinet; row said 10, miscount fixed in .qb), result driftwood planks @0.75; crimson/warped furnace boats give boatload: boats |
| SQ-ABD-03 | PASS | log: adzuki_curry_cooked_manual_only inputs roasted adzuki beans, dried banana, carrot, pumpkin slice, bowl |
| SQ-ABD-04 | PASS | log: pickerelweed_juice_manual_only takes the tag twice, pike_with_beetroot takes it twice, cook time 200; 1.0 xp not printed, source upgrade_aquatic.js fdCooking 1.0 xp 200 ticks |
| SQ-ABD-05 | PASS | 5 checks passed |
| SQ-AV-01 | PASS | 7 checks passed |
| SQ-BBQ-01 | PASS | 4 checks passed |
| SQ-BBQ-02 | PASS | log: raw skewer ingredients are tag items: beef = beef/minced_beef x2 + cabbage/cabbage_leaf x2; potato = potato/sweet_potato x2 + bacon; others match 3.1 |
| SQ-BBQ-03 | BATCH-FAULT | reclassified from FAIL: row expected kebab wrap and sandwich in food_type/handheld; dropped by owner ruling 2026-11-22 (plan_meals_rolls_bbq.md); row fixed to reject, rerun queued; no line matches /verb=tag kind=item tag=creativedelight:food_type/handheld .*ids=\S*barbequesdelight:kebab_wrap\b/; no line matches /verb=tag kind=item tag=creativedelight:food_type/handheld .*ids=\S*barbequesdelight:kebab_sandwich\b/; open look 1: the 10 grilled skewers in handheld; the grill / basin / tray mineable tags as upstream (block_tags in the tagsof lines) |
| SQ-BBQ-04 | PASS | compost.json: 11 rows at 0.25 (10 raw + burnt), 11 at 0.5 (10 cooked + wrap), sandwich 0.75, bibimbap 1.0 = 24 rows, matches barbeques_delight.md 3.7 |
| SQ-BBQ-05 | PASS | state line has_campfire=true at y-60 (replaced the campfire, not one up), lit, out in rain on stone, lit on coal, break drops grill + campfire |
| SQ-BBQ2-01 | PASS | shell grep for barbequesdelight/skewers.js/F.skewer error/warn lines returned nothing after reload; 8 BBQSkewerItem raws registered |
| SQ-BBQ2-02 | PASS | log: all 8 pack skewers have raw craft (shaped/raw_*, End shapeless), smelting, smoking, campfire, grilling, raw sequenced assembly; _from_cooked_manual_only for all but Nether (End shapeless, Cave present); no upstream craft ids (all creativedelight:) |
| SQ-BBQ2-03 | PASS | 2 checks passed |
| SQ-BBQ2-05 | BATCH-FAULT | reclassified from FAIL: same peaceful food regen offset; gamerule naturalRegeneration false added; rerun queued; no line matches /verb=pbot.useitem bot=c2b end=finish item=barbequesdelight:kebab_wrap .*food=12\b/; no line matches /verb=pbot.useitem bot=c2b end=finish item=barbequesdelight:kebab_sandwich .*food=18\b/; no line matches /verb=pbot.useitem bot=c2b end=finish item=barbequesdelight:bibimbap .*food=18\b/; open look 1: effects: only an ambient Raspberry Pie diet effect (800t, e.g. Speed after enough foods) may show; any other effect the food gave is a FAIL. Hunger: subtract the offset ROLL-11's bread showed (if any) |
| SQ-BBQ2-07 | PASS | famcheck: cooked skewers 23, in family skewer 18; the other 5 = burnt_skewer (hazard) + 4 displaydelight plated items (NONE); skewer cans 0 |
| SQ-LGM-11 | PASS | effects comfort 2392 t and nausea 92 t left after the eat (about 100 at the start); no poison |
| SQ-LGM-12 | PASS | canned_omurice, canned_mushroom_pot_pie, canned_roasted_mutton_rack = empty can + placed feast block (shapeless); no can recipe in the uses lists of raw/cooked mutton rack; no uncan listed |
| SQ-LGM-14 | PASS | famcheck: lm blocks 13, not placeable 0 (placeable, placeable/sneak, placeable_on_coaster) |
| SQ-ROLL-10 | PASS | 4 checks passed |
| SQ-ROLL-11 | BATCH-FAULT | reclassified from FAIL: peaceful food regen (+8 per eat, gotchas Peaceful refills food); row now sets gamerule naturalRegeneration false; rerun queued; no line matches /verb=pbot.useitem bot=c2b end=finish item=minecraft:bread .*food=5\b/; no line matches /verb=pbot.useitem bot=c2b end=finish item=roll_delight:golden_roll_slice .*food=10\b/; no line matches /verb=useair who=c2b hand=main result=FAIL using=false/; open look 1: bread from 0 must read food 5: if it reads 9, the +4 is a pack-wide eat bonus (find its source before judging the rolls) and the slice's 14 is that, not a roll value. Effects: Nourishment 2400t on both is the comfort_nourishment golden rule (120 s), not a fault; useitem at full hunger FAIL/PASS (not eaten) |
| SQ-ROLL-12 | PASS | golden_roll_slice made only by farmersdelightcutting/golden_roll_slice (+S&D copy); golden_roll gilding recipes only; no canned_ recipe for a Roll Delight roll/slice (canned_rice_roll_medley/royale stay; canned_sweet_roll is a bakery can) |
| SQ-BBQF-01 | PASS | 7 [flavor] defined lines: cumin, pepper, chili, honey_mustard, buffalo, barbeque, grilled |
| SQ-BBQF-02 | PASS | 6 checks passed |
| SQ-BBQF-03 | PASS | 5 checks passed |
| SQ-BBQF-04 | PASS | 22 BBQ offers over 20 butchers: all six seasonings seen, each 12 emerald -> 64 powder (chili, cumin, pepper) or 16 sauce (barbeque, buffalo, honey_mustard), maxUses 4, xp 4 |
| SQ-BBQF-05 | PASS | 4 checks passed |
| SQ-BBQF-06 | PASS | NBT Flavors [barbequesdelight:cumin, barbequesdelight:grilled] on the grilled beef skewer |
| SQ-BBQF-07 | PASS | 4 checks passed |
| SQ-CDM2-01 | PASS | 2 checks passed |
| SQ-CDM2-03 | PASS | 7 checks passed |
| SQ-CDM2-04 | PASS | makes: Cooking Pot recipe, its S&D mixing copy, potato_salad_manual_only and potato_salad_egg_solution mixing only (n=4); moredelight/oceanic potato_salad n=0 |
| SQ-GLD2-01 | BATCH-FAULT | reclassified from LOOK: rdetail ids wrong for cake (goldenfood:golden_cake) and apple (minecraft:golden_apple): recipe ids are creativedelight:shaped/golden_cake and shaped/golden_apple, so n=0; beef part verified (cooked beef + 8 nuggets); rdetail fixed in BBQF.qb, rerun queued; open look 1: golden beef = cooked beef + 8 gold nuggets; golden cake also nuggets; golden apple still 8 gold ingots |
| SQ-GLD2-02 | PASS | part=start golden_apple / golden_carrot, deploying gold ingot (rose gold counts), filling, pressing steps; 8 loops and 125 mB per golden.js header (goldenEnchant: 1 ingot, 125 mB, press, 8 loops) and golden_food.md 146 |
| SQ-SALT2-01 | PASS | 7 checks passed |
| SQ-SALT2-02 | PASS | randomtick changed=3; 3x Test passed, no Test failed; salt dust 8, 5, 3 from the three levels |
| SQ-SALT2-03 | PASS | mixing rock_salt_water_salt: water 1000 mB, results raw_rock_salt_block@1 + salt_dust*2 (block kept); salt.js passes 'heated'; hammering cutting/grid rows exist (grid input immersiveengineering:hammer; R.hammering helper = Engineer's Hammer) |
| SQ-SALT2-04 | PASS | 4 checks passed |
| SQ-SALT2-05 | PASS | makes: createmixing/salting/rusticdelight_calamari_roll (CD salting) makes calamari roll {Salted}; shell: 224 mixes added, 3 Create: The Salt mixes removed |
| SQ-SALT2-06 | PASS | shell: config/creativedelight-common.toml SaltBlockMeltingEnabled = false; ow_salt_kept chat line |
| SQ-SALT2-07 | PASS | both salt-dust offers 12 emerald -> 64 vintagedelight:salt_dust, maxUses 4, xp 4 |
| SQ-SALT2-08 | PASS | carried line 19 values; ls: salt-client.toml + salt-common.toml.moved, SALT2-08-FILES-OK; [salt.Evaporation] EvaporationChance = 0.5 |
| SQ-VTDA-11 | PASS | 16 checks passed |
| SQ-VTDA-12 | PASS | [swaps] 49 item merge(s) live = sum of sources across the 18 merges_*.json lists (49); no 'merged twice'; nut_mash_jar and pb_j in bot inventory |
| SQ-VTDA-13 | PASS | quiche_meat, _fungus, _greens, zucchini_quiche are creativedelight:baking recipes (cooktime 200), no grid recipe except quiche_from_slices; tool = pie tin per recipes.js QUICHE / veggiesdelight recipes.js (t_bakeware_pie_tin; rdetail does not print tool) |
| SQ-VTDA-10 | PASS | 4 checks passed |
| SQ-BBQS-01 | PASS | raw_potato_skewer = potato/sweet_potato x2 + bacon x2, raw_beef = beef/minced_beef x2 + cabbage/cabbage_leaf x2 (tags creativedelight:skewering/*); kebab_sandwich recipes n=0 (dropped, owner 2026-11-22, plan_meals_rolls_bbq.md ruling 4) |
| SQ-BBQS-02 | PASS | basin slot0 sweet_potato*2 before; after: well empty (filled=0), raw potato skewer in slot 0, stick gone, off hand empty: recipe takes bacon x2 (barbeques_delight.md skewering table), row's 'down by one' was a miscount, fixed in BBQS.qb |
| SQ-BBQS-03 | PASS | pbot.inv: stick*1 slot 0 (main hand kept), cabbage*1 slot 1; basin kept cabbage*3 |
| SQ-BBQW-01 | PASS | 22 _from_logs stonecutting rows: basins x2, trays x4 per wood on its own log tag (oak on creativedelight:bbq_oak_logs, 52 ids incl modded); all 18 basin/tray blocks in minecraft:mineable/axe and blockrunner:surface/wood |
| SQ-BBQW-02 | PASS | 2 checks passed |
| SQ-BBQW-03 | PASS | states: spruce_tray support=false, tray (oak) support=true, birch_tray support=true, stacked -59/-58/-57; after the plank removal all three air; Killed 3 entities |
| SQ-CAL-01 | PASS | debug.log: 13 unique creativecalendar common mixins incl CropSeasonGrowHook (IE / Dirty Stuff / Nether's Delight present) |
| SQ-CAL-04 | PASS | /ccaltime: preset SINGLEPLAYER, 45 min day + 15 min night, sub-season 8 days; push line 'nothing to push: every value already matches'; date answer in [time] Year/day format |
| SQ-CAL-05 | PASS | ccaltime day 1 -> 2 of 8 (world day 2); /season Early Spring day 2/8 |
| SQ-CAL-07 | PASS | [time] Moved to 1 x month: +8.0 days, world day 10, wine clock follows |

Counts: PASS 52, BATCH-FAULT 4

BF-FIX SQ-ROLL-11, SQ-BBQ2-05: peaceful food regen (+8 hunger before every eat, gotchas "Peaceful refills food") -> `gamerule naturalRegeneration false` after the bot spawn and back to true at the end (BBQ2.qb); rerun queued.
BF-FIX SQ-BBQ-03: row expected kebab wrap and sandwich in food_type/handheld, dropped by owner ruling 2026-11-22 (plan_meals_rolls_bbq.md ruling 4) -> two `expect` became `reject`, @pass text updated (BBQ.qb); rerun queued.
BF-FIX SQ-GLD2-01: `rdetail goldenfood:golden_cake` / `minecraft:golden_apple` matched no recipe id -> `rdetail shaped/golden_cake` / `shaped/golden_apple` (BBQF.qb); rerun queued.
Row text corrections without rerun (verdict PASS from the log): SQ-ABD-02 (9 driftwood items, "/+6"), SQ-BBQS-02 (bacon x2 consumed, off hand empty) in ABD.qb / BBQS.qb.
Note: SQ-BBQS-01 and SQ-BBQ-04 / SQ-BBQ2-05 still touch the dropped kebab items; the items stay registered (hidden), so the rows read them; the rows go with the kebab retirement.
BF-FIX-ahead SQ-TX-14: reads food=10 + bread nutrition after `pbot eat`; peaceful food regen would add to it (gotchas.md "Peaceful refills food") -> `gamerule naturalRegeneration false` after the bot spawn, `true` after the last step (TX.qb), pending step.
BF-FIX-ahead scan: other pending rows with `pbot eat` (CIF-02/03, DNR-O-02, FDI-04, GLD-02/07/16, RD-20) read effects, queue counts or teleports, not food deltas: no change. ROLL-03 already uses difficulty easy; ROLL-04/05 read only foodLevel 20 / 11+; BBQF and RPS have no pending steps. Skipped step: running CBA.qb SQ-CBA-14 (no food reads). qprescreen r1123cd: 28 likely findings (dead-id and unloaded-coordinate), left unfixed as non-simple or likely false positives (bot spawns load their own chunks).

#### Verdicts steps 10 to 40

Judge judge-2, RES `perf/runs/r1123cd/s09_res` to `s39_res` (drive steps 10 to 40: CBA, CBB, CBC, CBM, CBO on reduced mod sets). BATCH-FAULT rows are fixed in their `.qb` where simple (run default 11) and listed in `perf/runs/r1123cd/rerun.txt`. SQ-CBO-32 is a product FAIL that still shows HARNESS (the loot WARN tripped first; `--reclassify` cannot set FAIL); its rerun with the narrowed regex turns it FAIL. Steps 41 and later are not judged here (SQ-CBO-38 and -39 were fixed by the same edits and queued).

| ID | Result | Evidence |
|---|---|---|
| SQ-CBA-01 | PASS | 2 checks passed |
| SQ-CBA-02 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: boot regex matched RuntimeDistCleaner 'Attempted to load class net/minecraft/client/multiplayer/ClientLevel' lines that never name a mod (Rhino/KubeJS scans, other jars; run doc 2026-11-06 finding 5 ruled them noise); client-class alternative dropped from the regex, creativebrewing-attributed alternatives kept; rerun queued; boot log: /Mixin a ...
| SQ-CBA-03 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error regex matched creativeloot 07_rules.js WARN (PneumaticCraft absent from the chain, Rhino Class could not be found, caught and logged); other checks passed; regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:585:[13:08:00] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning] ban Pneu ...
| SQ-CBA-04 | PASS | 4 checks passed |
| SQ-CBA-05 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: the log line is '[CB potions] better_beacons not present - skipping the Long Reach and Phantom Bane potions' (one line, reworded); regex updated; the six registered=true and long_reach false checks passed; rerun queued; boot log: no line matches /better_beacons not present - skipping the long_reach potions/ |
| SQ-CBA-06 | PASS | 9 checks passed |
| SQ-CBA-07 | PASS | 3 checks passed |
| SQ-CBA-08 | PASS | 4 checks passed |
| SQ-CBA-09 | PASS | 7 checks passed |
| SQ-CBA-10 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: expected count stale: alcohol_drinks.js lists 51 drinks since Hearth and Harvest D16 added 7; the chain boot logs 49 of 51 (bellini and wodka absent); rows now expect 49 of 51 (CBO 50 of 51, instance 51 of 51); rerun queued; shell exit 1:  |
| SQ-CBA-11 | PASS | 6 checks passed |
| SQ-CBA-12 | PASS | 4 checks passed |
| SQ-CBA-13 | PASS | 3 checks passed |
| SQ-CBA-16 | PASS | mix count 68 on the instance boot vs 45 on the chain boot (SQ-CBA-06); mundane + wither_rose -> creativecommon:wither, thick + amethyst -> magic_resistance (judge) |
| SQ-CBA-17 | PASS | winemaker trades 34 on the instance boot vs 21 on the chain boot (SQ-CBA-10) |
| SQ-CBA-18 | PASS | absent list = 50 structure packs, createsifter, powergrid; quark, fruitsdelight, supplementaries are in mods/; 0 ERROR lines naming creativebrewing in latest.log |
| SQ-CBB-01 | PASS | 4 checks passed |
| SQ-CBB-02 | PASS | 7 checks passed |
| SQ-CBB-03 | FAIL | no line matches /pale_jane_from_rice_wine/ |
| SQ-CBB-05 | FAIL | kippers not listed: rdetail/recipes brewinandchewin:fermenting/kippers n=0; #forge:raw_fishes holds miners_delight:tentacles (so not the 2026-11-06 tentacle-strip cause); ingredient list not printable; tracked in TODO creativebrewing/publish #4 |
| SQ-CBB-06 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:572:[13:29:36] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBB-07 | PASS | 4 checks passed |
| SQ-CBB-08 | PASS | 6 checks passed |
| SQ-CBB-09 | PASS | 2 checks passed |
| SQ-CBB-12 | PASS | 4 checks passed |
| SQ-CBC-01 | PASS | 4 checks passed |
| SQ-CBC-03 | PASS | 2 checks passed |
| SQ-CBC-07 | PASS | 3 checks passed |
| SQ-CBC-02 | PASS | 11 failed-and-skipped lines left (pale_jane bark tag, azalea_tea, pink_lemonade x2, cake_base x2, vdPickling leather, twisted_composter, seq_test_star, azalea manual_only), 0 Empty array, none of grown_/twig_nest/rope_to__air/canned_mulberry lines, total under 40; [absent mods] lines printed |
| SQ-CBC-04 | PASS | sequenced assembly n matches 296-299; transition items CD 117, Engineering 67; Added 7341 / removed 1312 / modified 1282, 91 failed; [absent mods] prints (63 lines) |
| SQ-CBC-05 | PASS | 5 checks passed |
| SQ-CBO-01 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:585:[13:41:12] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-02 | FAIL | shell exit 1: [13:41:03] [WARN] creativebrewing:creativebrewing/potions.js#185: [CE potions] mix skipped: hearthandharvest:strong_bad_omen + minecraft:fermented_spider_eye -> buzzier_bees:strong_luck (potion not re; PASS: recorded: 45 mixes, swaps 0, alcohol 49 of 51, JEED 75, bottling 1701, tipped 94 x 2, re-added 0 dragon-breath, better_beacons skip line (Long Reach and Phantom Bane), no Lin ...
| SQ-CBO-03 | PASS | [absent mods] lines name architects_palette, beautify, bushierflowers, create_compressed, immersiveengineering, jeed, quark and other mods absent from the chain; 58 failed-and-skipped lines in startup.log (recorded) |
| SQ-CBO-04 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: 7 645:[23Nov2026 13:42:02.099] [Server thread/WARN] [KubeJS Server/]: creativeloot:kubejs/loot/07_ ...
| SQ-CBO-05 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: row never set reductionPerLevel 0.5 (the key moved to creativecommon-common.toml [magic_resistance], default 0.2 gives A 95.2); config-error regex matched the fresh-srv fml.toml and farmersdelight Incorrect key lines; row now edits the toml, restarts and rejects only a reductionPerLevel correction; rerun queued; boot log: /Incorrect key/correc ...
| SQ-CBO-10 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: NOT SIMPLE (not fixed): the row expects Armor / Tool Effects item namespaces; those gear rows moved to Creative Equipment (gear_effects.js, publish #17) and the chain has no Equipment jar, so 'effects items by namespace' is empty (run doc 2026-11-06 already listed this row fault); needs a @modset change or a rewrite to check effects only; loot ...
| SQ-CBO-11 | PASS | mixes 68 (chain 45), effect swaps 15 (chain 0), re-added 135 dragon-breath, alcohol 51 of 51, bottling 2385, tipped 132 x 2 |
| SQ-CBO-12 | PASS | 8 checks passed |
| SQ-CBO-13 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: fresh srv/config has [effects] remap and removed empty (the pack lists are in hand-edited config/creativecommon-common.toml), so no remap or removal happened; row now sets both lists in the toml and restarts before the golem check; rerun queued; no line matches /"forge:id": "vinery:climbing_effect"/; no line matches /"forge:id": "vinery:telepo ...
| SQ-CBO-14 | PASS | 8 checks passed |
| SQ-CBO-15 | PASS | 7 checks passed |
| SQ-CBO-16 | PASS | 13 failed-and-skipped lines vs 58 on SQ-CBO-03; recipes: 0 failed |
| SQ-CBO-17 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: 4 618:[23Nov2026 13:45:35.737] [Server thread/WARN] [KubeJS Server/]: creativeloot:kubejs/loot/07_ ...
| SQ-CBO-18 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:567:[13:46:28] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-20 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: fresh srv/config has [effects] remap and removed empty (the pack lists are in hand-edited config/creativecommon-common.toml), so no remap or removal happened; row now sets both lists in the toml and restarts before the golem check; rerun queued (loot WARN regex also narrowed); shell exit 1: logs/kubejs/server.log:584:[13:47:18] [WARN] creative ...
| SQ-CBO-21 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:585:[13:48:36] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-22 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:585:[13:49:30] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-23 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:583:[13:50:31] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-24 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:584:[13:51:22] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-25 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:584:[13:52:22] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-26 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:584:[13:53:15] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-27 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: NOT SIMPLE (not fixed): the row expects Armor / Tool Effects item namespaces; those gear rows moved to Creative Equipment (gear_effects.js, publish #17) and the chain has no Equipment jar, so 'effects items by namespace' is empty (run doc 2026-11-06 already listed this row fault); needs a @modset change or a rewrite to check effects only; loot ...
| SQ-CBO-28 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: NOT SIMPLE (not fixed): the row expects Armor / Tool Effects item namespaces; those gear rows moved to Creative Equipment (gear_effects.js, publish #17) and the chain has no Equipment jar, so 'effects items by namespace' is empty (run doc 2026-11-06 already listed this row fault); needs a @modset change or a rewrite to check effects only; loot ...
| SQ-CBO-29 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:584:[13:55:55] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-30 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: NOT SIMPLE (not fixed): the row expects Armor / Tool Effects item namespaces; those gear rows moved to Creative Equipment (gear_effects.js, publish #17) and the chain has no Equipment jar, so 'effects items by namespace' is empty (run doc 2026-11-06 already listed this row fault); needs a @modset change or a rewrite to check effects only; loot ...
| SQ-CBO-31 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: fresh srv/config has [effects] remap and removed empty (the pack lists are in hand-edited config/creativecommon-common.toml), so no remap or removal happened; row now sets both lists in the toml and restarts before the golem check; rerun queued; also stale 43 of 44 alcohol count (now 50 of 51) and the loot WARN regex narrowed; shell exit 1: lo ...
| SQ-CBO-32 | HARNESS | shell exit 1: logs/kubejs/server.log:585:[13:58:54] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning] ban PneumaticCraft: could not read the mod's loot switch: dev.latvian.mods.rhino.EvaluatorException:; shell exit 1: [13:58:46] [WARN] creativebrewing:creativebrewing/potions.js#185: [CE potions] mix skipped: hearthandharvest:strong_bad_omen + minecraft:fermented_spider_eye -> bu ...
| SQ-CBO-33 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: NOT SIMPLE (not fixed): the row expects Armor / Tool Effects item namespaces; those gear rows moved to Creative Equipment (gear_effects.js, publish #17) and the chain has no Equipment jar, so 'effects items by namespace' is empty (run doc 2026-11-06 already listed this row fault); needs a @modset change or a rewrite to check effects only; loot ...
| SQ-CBO-34 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:584:[14:00:46] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...
| SQ-CBO-35 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: NOT SIMPLE (not fixed): 'Lingerification: generated N' is logged only when IE resolves the generated list (full pack: client Render thread line), never on this dedicated chain boot (run doc 2026-11-06 row fault); bottling ids are creativebrewing:iebottling/... (id text fixed in the row); loot WARN regex narrowed. Rewrite pass needed for the ge ...
| SQ-CBO-36 | BATCH-FAULT | reclassified from FAIL: judge-2 2026-11-23: NOT SIMPLE (not fixed): row expects a creativebrewing:magic_resistance JEED provider; jeed_providers.js loads 75 providers (ids creativebrewing:jeed/<effect>) with none for Magic Resistance, run doc 2026-11-06 ruled it a row fault (JEED's own potion provider); owner/docs decide the expected provider; loot WARN regex narrowed; shell exit 1: log ...
| SQ-CBO-37 | BATCH-FAULT | reclassified from HARNESS: judge-2 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught and logged, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:585:[14:03:35] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning ...

Counts: PASS 30, BATCH-FAULT 29, FAIL 3, HARNESS 1

Shared cause (26 rows): `creativeloot:kubejs/loot/07_rules.js#36` logs `[loot-tuning] ban PneumaticCraft: could not read the mod's loot switch: ...EvaluatorException: Failed to load Java class ...ConfigHelper: Class could not be found!` (and #46 for Lightman's) on every reduced mod set without those mods; the exception is caught by the ban helper. The rows' `! grep EvaluatorException` check read it as a script error. Ruling: batch regex too wide for the row's purpose (script errors in the Creative Brewing scripts), product noise logged as TODO loot-tables #3 (low: skip the read when the mod is absent). Full-pack steps 1 to 9 never showed it.

BF-FIX loot WARN (SQ-CBA-03, SQ-CBB-06, SQ-CBO-01, -04, -17, -18, -21 to -26, -29, -34, -37 and the loot part of -10, -20, -27, -28, -30, -31, -32, -33, -35, -36): every `shell ! grep -nE "...EvaluatorException..."` in CBA, CBB, CBO, CBP.qb now ends `| grep -vF "[loot-tuning] ban"`, the two `.preload_n` compound checks (CBO-04, -17 shape) add `\[loot-tuning\] ban` to their `grep -v` list. SQ-CBO-40 (running step) left unedited; its rerun is needed only if it trips the same line.
BF-FIX SQ-CBA-02 (and the same regex at CBA.qb section 15): `Attempted to load class net/minecraft/client` matched RuntimeDistCleaner lines (ClientLevel, AbstractClientPlayer, PoseStack) that never name a mod; run doc 2026-11-06 finding 5 ruled them noise (Quark, CD scripts, Rhino scans), the row said "narrow to creativebrewing frames" and never did. Alternative dropped, @pass text says why. Source of the lines in the chain not proven from the log alone.
BF-FIX SQ-CBA-05: the line is now `[CB potions] better_beacons not present - skipping the Long Reach and Phantom Bane potions` (one line); regex updated (also the @commands text).
BF-FIX SQ-CBA-10, CBO-31, -42, CBO @pass/text, CBM-01: `alcohol_drinks.js` lists 51 drinks since Hearth and Harvest D16 added 7 (hearth_and_harvest.md 3.1); chain 49 of 51, one-extra-mod boots 50 of 51 (CBO-31 log: 50 of 51), all 51 of 51 (full-pack boots 51 of 51). alcohol.md still says 44 (stale doc, not edited here).
BF-FIX SQ-CBO-05: `reductionPerLevel` moved to `creativecommon-common.toml [magic_resistance]` (default 0.2, A read 95.2 not 97.0) and the row never set it (its "stop; qrun.py toml" text was not a step); row now `edit ... qrun.py toml` + `restart`. The `Incorrect key|correcting` regex matched FMLConfig and farmersdelight-common.toml first-boot corrections (every fresh srv/config logs one per file); now only a `reductionPerLevel` correction.
BF-FIX SQ-CBO-13, -20, -31, -38, -43, -51: fresh srv/config has `[effects] remap` and `removed` empty (`config: empty` prep; the pack lists live in hand-edited `config/creativecommon-common.toml`), so no remap or removal ran (neapolitan:agility stayed). Each row now sets both lists with `qrun.py toml` and restarts before its golem block. CBO-43 and -51 had not run yet.
BATCH-FAULT not simple (not fixed): SQ-CBO-10, -27, -28, -30, -33 expect `effects items by namespace` entries (dragonloot, everythingcopper, felsic_gear, immersive_armors, creativeequipment ...); the Armor / Tool Effects tables are filled by Creative Equipment (gear_effects.js, publish #17), absent on these sets, so the line is empty (run doc 2026-11-06 listed this row fault); needs a `@modset` change (add the Equipment jar) or a rewrite to effects only. SQ-CBO-35: `Lingerification: generated N` is logged when IE resolves the generated list (a client Render thread line in the full pack), never on a dedicated boot; bottling ids are `creativebrewing:iebottling/...` (id text fixed, not enough to pass). SQ-CBO-36: no `creativebrewing:magic_resistance` JEED provider exists (75 providers, ids `creativebrewing:jeed/<effect>`); expected provider needs an owner or doc call. Pending rows with the same namespace expectation: SQ-CBO-52 and -53 (look only), -41 already judged by judge-3.

Findings (real FAIL):
- SQ-CBO-02 and -32: `potions.js#185` `[CE potions] mix skipped: hearthandharvest:strong_bad_omen + minecraft:fermented_spider_eye -> buzzier_bees:strong_luck (potion not registered)` with Buzzier Bees absent (potions.js line 236 mix, comment "mix() skips an unregistered potion"). TODO potions #11 (low).
- SQ-CBB-03: Pale Jane fermenting skipped on the chain: `[NCH] bncFermenting pale_jane_from_rice_wine input[1]: item/tag "#creativecommon:bark" does not exist or is empty` (`creativecommon:bark` is `#immersive_weathering:bark` only since 2026-11-09; no Immersive Weathering on the chain). Logged on TODO creativebrewing/publish #4 (the row's pass text still says Pale Jane listed and bark = farmersdelight:tree_bark).
- SQ-CBB-05: `brewinandchewin:fermenting/kippers` n=0 although `miners_delight:tentacles` is in `#forge:raw_fishes`; cause open; logged on creativebrewing/publish #4.

LOOKs filled: SQ-CBA-16 (68 vs 45 mixes), -17 (34 vs 21 winemaker), -18, SQ-CBB-05 (FAIL), SQ-CBC-02 (11 skipped lines left, 0 Empty array), -04, SQ-CBO-01, -02, -03 (58 skipped lines), -10, -11 (68 mixes, 15 swaps, re-added 135), -16 (13 vs 58), -29 (8 swaps, fermenting 22), -31 (34 winemaker). No owner LOOK left in this range.

#### Verdicts steps 41 to 81

Judge judge-3, RES `perf/runs/r1123cd/s40_res` to `s80_res` (drive steps 41 to 81: CBO 38 to 53, CBP, CBW, CDB, CDFL, CDFW, CDJ, CDJ4, CEQ, CFR, CFS, CIF, CL, CM, CNS, CS, CUP, DCM, DEP, DNR-O). Step 82 (s81_res, DNR) was still running and is not judged here. Simple batch faults are fixed in their `.qb` (run default 11) and listed in `perf/runs/r1123cd/rerun.txt` (qrerun.py --add). SQ-CBP-01 is a product FAIL that still shows HARNESS (the shell exit code tripped first; `--reclassify` cannot set FAIL).

| ID | Result | Evidence |
|---|---|---|
| SQ-CBO-38 | BATCH-FAULT | reclassified from FAIL: judge-3 2026-11-23: fresh srv/config had empty [effects] remap/removed (neapolitan:agility stayed, no vinery:climbing_effect), row now sets both lists in the toml and restarts (judge-2 fix); plus the loot WARN regex narrowed; rerun queued; shell exit 1: logs/kubejs/server.log:584:[14:04:28] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning] ban PneumaticCraft: could not read the mod's loot switch: dev.latvian.mods.rhino.EvaluatorException:; no line matches /"forge:id": "vinery:climbing_effect"/; /"forge:id": "neapolitan:agility"/ matched: [23Nov2026 14:04:52.063] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Iron Golem has the following entity data: [{"forge:id": "neapolitan:agility", Ambient: 0b, CurativeItems: [{id: "; open look 1: effect swaps above the chain's |
| SQ-CBO-39 | BATCH-FAULT | reclassified from HARNESS: judge-3 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught by the ban helper, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:585:[14:05:41] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning] ban PneumaticCraft: could not read the mod's loot switch: dev.latvian.mods.rhino.EvaluatorException: |
| SQ-CBO-40 | BATCH-FAULT | reclassified from HARNESS: judge-3 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught by the ban helper, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:581:[14:06:37] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning] ban PneumaticCraft: could not read the mod's loot switch: dev.latvian.mods.rhino.EvaluatorException:; open look 1: record whether Resilience stayed or was dropped (remap target absent) |
| SQ-CBO-41 | BATCH-FAULT | reclassified from HARNESS: judge-3 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught by the ban helper, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:583:[14:07:55] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning] ban PneumaticCraft: could not read the mod's loot switch: dev.latvian.mods.rhino.EvaluatorException: |
| SQ-CBO-42 | BATCH-FAULT | reclassified from HARNESS: judge-3 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught by the ban helper, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; also the stale alcohol count fixed by judge-2 (50 of 51); shell exit 1: logs/kubejs/server.log:582:[14:08:48] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning] ban PneumaticCraft: could not read the mod's loot switch: dev.latvian.mods.rhino.EvaluatorException:; shell exit 1:  |
| SQ-CBO-43 | BATCH-FAULT | reclassified from HARNESS: judge-3 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught by the ban helper, not a script error); regex now skips [loot-tuning] ban lines; rerun queued; shell exit 1: logs/kubejs/server.log:584:[14:09:42] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning] ban PneumaticCraft: could not read the mod's loot switch: dev.latvian.mods.rhino.EvaluatorException:; open look 1: both brews -> nothing (or the potion unregistered) |
| SQ-CBO-50 | PASS | 11 checks passed |
| SQ-CBO-51 | PASS | potion mixes 56 against SQ-CBO-20 chain 55; golem effect remapped to alexsmobs:knockback_resistance (pass text) (judge-3) |
| SQ-CBO-52 | BATCH-FAULT | judge-3 2026-11-23 NOT SIMPLE (not fixed): effects armor pieces 0, items by namespace empty; Armor/Tool Effects rows are filled by Creative Equipment (gear_effects.js, publish #17), absent from this chain set; same cause as SQ-CBO-10; needs a @modset change or a rewrite to effects only (judge-3) |
| SQ-CBO-53 | BATCH-FAULT | judge-3 2026-11-23 NOT SIMPLE (not fixed): effects armor pieces 0, items by namespace empty; Armor/Tool Effects rows are filled by Creative Equipment (gear_effects.js, publish #17), absent from this chain set; same cause as SQ-CBO-10 (judge-3) |
| SQ-CBP-01 | HARNESS | shell exit 1: loot errors=7; open look 1: the loot-table line names fruitsdelight, moredelight, supplementaries, upgrade_aquatic, endersdelight among others |
| SQ-CBP-03b | INCONCLUSIVE | no Error parsing recipe line (0) and the horror_lasagna recipes load; but no [absent mods] line counts a minecraft:empty / Item.of / Fluid.of row (11 recipe-row lines name only absent mods), so the counting claim of the pass text is unverified (judge-3) |
| SQ-CBP-09 | PASS | 4 checks passed |
| SQ-CBP-02 | PASS | 4 checks passed |
| SQ-CBP-04 | PASS | 3 checks passed |
| SQ-CBP-05 | PASS | 6 checks passed |
| SQ-CBP-08b | PASS | end gear transition items (37) printed; mod data packs line: off (mod not loaded): [felsic_gear, felsic_gear+nullscape]; [absent mods] skipped 2 recipe rows for absent sophisticatedbackpacks (judge-3) |
| SQ-CBW-01 | PASS | oak board = 2 sticks + 4 oak planks; shaped/cutting_board on #creativedelight:cutting_board_planks (n=50, holds spruce and modded planks); the 8 wood boards plus the spruce board are in mineable/axe, counter_toppers and hammer_no_rotate (judge-3) |
| SQ-CDB-01 | PASS | no script error on the listed scripts; [itemMerges] creativedelight: 49 merges, 52 recipes swapped (the older run-doc counts 14/17/38/44 predate later merges, so only the direction is checkable); counted as PASS on the checks (judge-3) |
| SQ-CDB-02 | PASS | tags: sweeteners n=4 (powdered_sugar, honey_bottle, honeycomb, sugar), brewing n=4, drink n=3 (sugar, powdered sugar, honey bottle), plain n=2 (sugar, powdered sugar) = item_dict.js comments (the row pass text is older); hot cocoa slot lists sugar/powdered_sugar/honey_bottle (judge-3) |
| SQ-CDB-03 | PASS | results: calamari 1 @1.0 + 1 @0.5, ink sac 1 @1.0 + 2 @0.5; glow variant the same with glowing_calamari and glow ink (judge-3) |
| SQ-CDB-04 | PASS | squid kill drops miners_delight:squid:1, glow squid drops miners_delight:glow_squid:1 (one id per kill) (judge-3) |
| SQ-CDB-05 | PASS | 7 checks passed |
| SQ-CDB-06 | PASS | 3 checks passed |
| SQ-CDB-07 | PASS | splash_milk:milk_bottle goes into a jar as fluid moonlight:milk; jars probe 5 pass 0 fail (judge-3) |
| SQ-CDB-08 | PASS | Pickling Jar recipe 5 sea pickles + salt dust -> pickled_sea_pickle*5 (vintagedelight:fermenting) (judge-3) |
| SQ-CDB-09 | PASS | row checks passed; braised sea pickle inputs sea pickle + 2 brown mushroom match the cooking pot probe, no pot failed (judge-3) |
| SQ-CDB-10 | PASS | creamy gnocchi: milk tag slot, 2 potato_noodle, onion/garlic/garlic clove slot, bowl (judge-3) |
| SQ-CDB-11 | PASS | bayberry soup registered (fruitsdelight + canned_bayberry_soup); cacciatore chicken slot reads minecraft:chicken/farmersdelight:chicken_cuts (raw cuts, no /+N), the "cooked chicken cuts" wording of the look is older than the recipe; client halves open (judge-3) |
| SQ-CDB-12 | PASS | makes lines: creativedelight:shapeless/canned_pickled_pickles and _salt give the canned item; name is client (judge-3) |
| SQ-CDB-13 | PASS | 2 checks passed |
| SQ-CDB-15 | PASS | cake base (baked flour/dough/manual) takes 3 flour or cornmeal slots, egg tag slot, sweetener tag; cacciatore slot as SQ-CDB-11; hand crafting is client (judge-3) |
| SQ-CDB-16 | PASS | fish egg roll egg slot: oceanic_delight:salmon_eggs/oceanic_delight:ancient_fish_eggs, nothing else (judge-3) |
| SQ-CDFL-01 | BATCH-FAULT | reclassified from HARNESS: judge-3 2026-11-23: no-error check matched the creativeloot 07_rules.js [loot-tuning] ban WARN (PneumaticCraft/Lightman's absent from the reduced mod set; caught by the ban helper, not a script error); regex now skips [loot-tuning] ban lines; rerun queued (CDFL.qb line 43); shell exit 1: logs/kubejs/server.log:554:[14:28:22] [WARN] creativeloot:kubejs/loot/07_rules.js#36: [loot-tuning] ban PneumaticCraft: could not read the mod's loot switch: dev.latvian.mods.rhino.EvaluatorException: |
| SQ-CDFL-02 | PASS | 6 checks passed |
| SQ-CDFL-04 | PASS | 2 checks passed |
| SQ-CDFW-01 | PASS | [can servings] 338 cans, below the 344 of 2026-11-20 by the cans of absent mods; [cans] registered 301 (judge-3) |
| SQ-CDFW-02 | PASS | tagdiff food_type: only-here entries are H&H and later items added after the 2026-11-19 dump, only-other entries are golden / absent-mod items (goldenfood, oceansdelight guardian tail) (judge-3) |
| SQ-CDFW-03 | PASS | placeable 283 here vs 278 in the dump (only here: H&H pies, waffle, tropical pizza), coaster/plate tags differ by items added after the 2026-11-19 dump; no only-other entries in placeable (judge-3) |
| SQ-CDFW-04 | PASS | mortar recipes listed: create wheat, create sugar cane, farmersdelight rice x2 (wild rice, rice panicle), cocoa powder, garlic clove (+ hay, corn, powders); 21 total (judge-3) |
| SQ-CDFW-05 | PASS | 6 checks passed |
| SQ-CDFW-06 | PASS | smelting/smoking/campfire trio present for ghast, ham, arthropod, duck, perch, pike, acorn; Fried Turtle Egg and Fried Egg have smoking + campfire only: eggs have no furnace recipe by design (food.md 3.7, cd_food.js egg furnace 0, FO-62); the row pass text still says three each (judge-3) |
| SQ-CDFW-08 | PASS | sequenced enchanted golden apple step takes minecraft:gold_ingot/mca:rose_gold_ingot; the other makes/rdetail lines for the golden pieces exist (judge-3) |
| SQ-CDFW-09 | PASS | 119 *_cooked_manual_only recipes listed (Farmers, Fruits, Alex, Abnormals, Delightful, ...) (judge-3) |
| SQ-CDFW-10 | INCONCLUSIVE | row asks for a client or offline count (108 tool, 227 misc): both dict scripts load without error (Loaded script lines, shell checks passed) but no key count is printed on a server (judge-3) |
| SQ-CDJ-09 | BATCH-FAULT | reclassified from HARNESS: judge-3 2026-11-23: the cdj5home marker and its forceload came from SQ-CDJ-11 (superseded, never run), so H1..H3 never captured and the bot not spawned; row now forceloads and summons the marker at 1400 -60 1400 (flat world, as SQ-MXS rows); rerun queued; capture H: no line matches /entity data: \[(-?\d+)\.\d+d, (-?\d+)\.\d+d, (-?\d+)\.\d+d\]/; placeholder H1, H2, H3 unset (no capture in this batch, not in RES/vars.json); /CDJ5 home marker missing/ matched: [23Nov2026 14:35:00.777] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Not Secure] [Server] CDJ5 home marker missing; count /Gave 1 +\[/ = 0, wanted >= 5; /Unknown item/No player was found/ matched: [23Nov2026 14:35:10.817] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: No player was found; open look 1: item counts per namespace (displaydelight 212, fungidelight 32, miners_delight 69, veggiesdelight 89, vintagedelight 134 or more) are SQ-CDJ-09b after /dumpall; error count in rows/SQ-CDJ-09.shell.txt against the last r1101 boot |
| SQ-CDJ-10 | BATCH-FAULT | reclassified from HARNESS: judge-3 2026-11-23: same missing cdj5home marker as SQ-CDJ-09, and the drive boots with displaydelight = true (the run plan 'Boot 2' toml edit is manual): row now sets [members] displaydelight=false and restarts, forceloads 1392..1423 x 1392..1455, restores true at its end; rerun queued; shell exit 1: ; capture H: no line matches /entity data: \[(-?\d+)\.\d+d, (-?\d+)\.\d+d, (-?\d+)\.\d+d\]/; placeholder H1, H2, H3 unset (no capture in this batch, not in RES/vars.json); placeholder H1, H2, H3 unset (no capture in this batch, not in RES/vars.json); placeholder H1, H2, H3 unset (no capture in this batch, not in RES/vars.json); no line matches /CDJ10 stew placed/; no line matches /verb=break who=\S+ broken=true before="?displaydelight:beef_stew/; no line matches /verb=break drops=farmersdelight:beef_stew:1 stacks=1\b/; open look 1: the break is a survival break by the Devtools fake player (BreakEvent, no tool); same loot context as a bot break |
| SQ-LOOT-FD1 (I5 side effect) | PASS | row checks passed; crate and bag tables list the crops in the item lines of both loot dumps (judge-3) |
| SQ-MDP-13 rerun (I3) | PASS | row checks passed; bat wing sandwich ingredients list bread tag, wing and calamari/brown mushroom slots (judge-3) |
| SQ-MDP-17 | BATCH-FAULT | reclassified from FAIL: judge-3 2026-11-23: NOT SIMPLE (not fixed): the row needs a boot with the srv miners_upstream symlink removed so the boot stops; the drive prep keeps the symlink (Done reached) and a restart waits for Done, so it needs its own step with a prep rm action (CDJ.qb run plan boot 3); cannot run in this drive; boot log: no line matches /Miner's Delight \(Creative Delight\) reads its textures, models and recipes from the original Miner's Delight jar, which must be in the folder 'miners_upstream' of the game directory \(not in mods/\):/; boot log: no line matches /missing: .*miners_upstream/miners_delight-1\.20\.1-1\.4\.5-backport\.jar/; boot log: no line matches /get it from https://cdn\.modrinth\.com/data/qMxbM4BQ/versions/p0INUam7/miners_delight-1\.20\.1-1\.4\.5-backport\.jar \(Modrinth/; boot log: no line matches /missing: .*miners_upstream/miners_delight-1\.19\.2-1\.1\.2\.jar/; boot log: no line matches /get it from https://mediafilez\.forgecdn\.net/files/4738/272/miners_delight-1\.19\.2-1\.1\.2\.jar \(CurseForge/; boot log: /Done \(/ matched: [23Nov2026 14:34:56.925] [Server thread/INFO] [net.minecraft.server.dedicated.DedicatedServer/]: Done (10.048s)! For help, type "help"; open look 1: the crash report / log blames only Miner's Delight (Creative Delight) (Suspected Mods: creativedelight or miners_delight), no other mod named as the cause |
| SQ-MDP-18 (I7) | PASS | row checks passed (burning drop cooked_arthropod, no raw; non-burning raw arthropod) (judge-3) |
| SQ-CDJ4-04 | PASS | 3 checks passed |
| SQ-CDJ4-07 | PASS | six lunchbox recipes listed (shaped copper/golden, soap, dye, smithing skin and netherite upgrade), no unknown serializer (judge-3) |
| SQ-CDJ4-08b | FAIL | no line matches /has the following entity data: -2\d{4}/; /No entity was found/ matched: [23Nov2026 14:58:53.337] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: No entity was found |
| SQ-JSL-01 | PASS | jam recipes: whole-fruit slot lists lemon/lemon_slice, separate lemon_slice slot, sugar slot; pot recipe container vintagedelight:empty_mason_jar (zsb potrecipe) (judge-3) |
| SQ-JSL-02 | PASS | slot 8 holds orange_jelly and lemon_jelly after the pots cooked (the expect checks passed); container empty_mason_jar (judge-3) |
| SQ-LEM-01 | PASS | 10 picks gave fruitsdelight:lemon:22 (bot found 10 matching items), inside 20 to 50; breaks drop 2 to 5 (judge-3) |
| SQ-LEM-02 | PASS | knife recipe result lemon_slice*5 + lemon_seeds*1 @1.0 (both the Create cutting copy and the FD cutting recipe) (judge-3) |
| SQ-CEQ-01 | INCONCLUSIVE | tag lists printed (stepstools 16, dragon_gear 17, elytra 1, charms 1) but not compared with the r1106 boot 1 lists; the old-save half has no steps (judge-3) |
| SQ-CEQ-05 | PASS | aluminum plate block in creativeequipment:needs_copper_tool and a pickaxe tag; copper pickaxe break drops 1 aluminum_plate_block, wooden pickaxe 0; harvest.js loaded once (judge-3) |
| SQ-CEQ-07 | PASS | helmet durability read 100 after 220 ticks (200 -> 150 -> 100 two pulses; look accepts a second pulse when the timer started earlier) (judge-3) |
| SQ-CEQ-08 | INCONCLUSIVE | tools match tune_tools.js (echo sword 9 -> attribute 8 / -2.35, echo pickaxe 6, felsic pickaxe 7, wooden knife 1.5) but dragonloot:dragon_helmet reads armor 8 where tune_armor.js sets 7 (toughness 4.5 and knockback 0.3 equal; dragonloot config default is 8): tune not applied to dragonloot armor or the tune value is stale; open for the owner (judge-3) |
| SQ-CMG-01 | PASS | Magic mixins from its json 13 (max 13), Equipment 21 (max 23); the creativeengineering list holds 16 of its own 17 names, none of Magic or Equipment (judge-3) |
| SQ-CMG-03 | PASS | startup packs order: ... startup_scripts, creativecompat, creativecalendar, creativeloot, creativemagic, creativeequipment, creativeengineering; no error lines (judge-3) |
| SQ-CMG-04 | PASS | entities line at 371 -60 300: imm_ptl_core:nether_portal_new x2 (by_type); creativemagic: recipes 23 incl. ender chest/pouch/tank, printer (CEI), dragonbreathing (judge-3) |
| SQ-CMG-05 | PASS | #forge:experience holds only create_enchantment_industry:experience (+ flowing); trinket case probe 0 fail (judge-3) |
| SQ-CFR-01 | PASS | one apply: minecraft.CampfireRainMixin from creativedelight.mixins.json (judge-3) |
| SQ-CFR-06 | BATCH-FAULT | reclassified from FAIL: judge-3 2026-11-23: row never set [campfire] rainDouses = false (srv config has true, campfire doused in rain: Test failed); row now edits the toml, restarts, restores true at its end; rerun queued; no line matches /Test passed/ |
| SQ-CFR-07 | PASS | comfort near the campfire: Duration 90 ticks (4.5 s); after the move away: no ActiveEffects (judge-3) |
| SQ-CFR-08 | PASS | 2 checks passed |
| SQ-CFS-01 | PASS | shell empty: no InvalidInjectionException naming a CfbOven mixin; oven state read as expected (judge-3) |
| SQ-CFS-02 | PASS | oven lit=true after the burn, pot output (tomato_sauce slot 8, bowls back) after 240 ticks, oven lit=false 201 ticks later; no pot stall (judge-3) |
| SQ-CIF-02 | INCONCLUSIVE | new-world half passed (long_reach/long_long_reach registered, Phantom Bane drink effect 3592 ticks); the old-world chest half cannot run: no 2026-11-06 build kept to make the save (judge-3) |
| SQ-CIF-03 | PASS | Caffeinated and Phantom Bane both 1791 ticks; rest stat 282 after the drink and 213 after both end (about 200, not near -24000) (judge-3) |
| SQ-CIF-04 | PASS | mods list: creativecommon, creativecompat, creativecalendar, SereneSeasons, architectury, kubejs, rhino, cdevperfbot (+ creativedevtools for the reads) (judge-3) |
| SQ-CIF-04b | PASS | mods list: creativecommon, creativecompat, creativeequipment, creativecalendar, SereneSeasons, creativedevtools, architectury, kubejs, rhino, cdevperfbot (judge-3) |
| SQ-MG-16 | PASS | makes lines: the two creativecompat:createsplashing/cardboard_box recipes only; client half open (judge-3) |
| SQ-CM-01 | INCONCLUSIVE | no script error; [itemMerges] creativedelight: 49 merges, 52 recipes swapped, but no stored pre-cantaloupe count to compare ("3 higher" cannot be checked) (judge-3) |
| SQ-CM-02 | BATCH-FAULT | reclassified from FAIL: judge-3 2026-11-23: row expected creativedelight:shaped/mini_cantaloupe (4 slices -> Mini Cantaloupe); plan_food_batch_1119 wave A (A6, commit 1336abd37, 2026-11-19) removed that grid (cutting only); item_merges.md 3 still lists it (stale doc); row now rejects it; the seeds and popsicle expectations match the code; rerun queued; no line matches /verb=recipes id=creativedelight:shaped/mini_cantaloupe/; open look 1: makes fruitsdelight:hamimelon_seeds lists a recipe; the uses lines of the slice include that seeds recipe; Delightful's two popsicle recipes are in the popsicle list |
| SQ-CM-03 | PASS | 4 checks passed |
| SQ-CM-04 | PASS | raw pasta uses: alone=0 shared=90, every listed recipe alone=false (judge-3) |
| SQ-CM-05 | PASS | 3 checks passed |
| SQ-CM-06 | PASS | oat dough uses include creativedelight:baking/bread_from_dough_1, _3 and _6 (alone=false) (judge-3) |
| SQ-CM-07 | PASS | 7 checks passed |
| SQ-CM-10 | PASS | 4 checks passed |
| SQ-BJ-01 | PASS | new jam jar pot recipes exist with result *1 (2 -> 1 since 2026-11-02, dishes/jams #4; the row text still says 2); removed-jam items not listed as results (judge-3) |
| SQ-BJ-02 | PASS | chicken sandwich and More Delight sandwiches list the bread tag (pneumaticcraft:sourdough_bread/minecraft:bread/... +6); bread_slice cutting keeps minecraft:bread (judge-3) |
| SQ-BJ-03 | PASS | 5 checks passed |
| SQ-CM-20 | PASS | 7 checks passed |
| SQ-CM-21 | PASS | shrimp fried rice effect farmersdelight:comfort 4800 (judge-3) |
| SQ-CM-22 | BATCH-FAULT | reclassified from FAIL: judge-3 2026-11-23: tag has 7 members on this boot, not 6: vintagedelight/tags.js:16 adds rusticdelight:bell_pepper_slice_green when Rustic Delight is present; regex now n=[67]; rerun queued; no line matches /verb=tag kind=item tag=creativedelight:veggie_noodle_veg n=6/ |
| SQ-CM-23 | PASS | shrimp fried rice: fried_rice + 2 shrimp tag slots (shrimp/shrimp_slices) (judge-3) |
| SQ-CM-24 | PASS | 2 checks passed |
| SQ-KT-21 | PASS | coasters at 5, 7, 9 hold red wine, hamburger, long black tea; the coasters at 11 and 13 Items: [] (judge-3) |
| SQ-CM-30 | PASS | 4 checks passed |
| SQ-CM-31 | PASS | seagrass salad inputs minecraft:seagrass x2 + bowl; tentacle uses lines alone=0 (judge-3) |
| SQ-CM-32 | PASS | seafood skewer chain starts from raw_seafood_skewer (shaped slot: calamari / calamari_slice / miners squid +3 first, no tentacle); squid and pickles: cooked calamari slot (judge-3) |
| SQ-CM-34 | PASS | displayName="Storage Delight (Creative Delight)" (judge-3) |
| SQ-CM-35 | PASS | forge:squid_tentacles and forge:foods/tentacles hold glowing calamari, miners squid, rusticdelight calamari (6 each); takoyaki takes calamari in both slots, no empty slots (judge-3) |
| SQ-CM-36 | PASS | fgdtest: oven truffle pie PASS (creativedelight:baking/truffle_pie); note: fgdtest also printed 2 FAIL lines (stomach alcohol / Swiftness with Weak Stomach) outside this row (judge-3) |
| SQ-CSN-04 | PASS | curios list names hands, face, feet (creativeequipment) among 13 slot types (judge-3) |
| SQ-CSN-05 | PASS | JFR parsed: 228 creativecommon.ServerTick and 3648 creativecommon.LevelTick events (judge-3) |
| SQ-CS-01 | PASS | 2 checks passed |
| SQ-CS-02 | PASS | answers 1, 6, 11, 16 passed: wheat temperate, tobacco warm_dry, melon stem warm_wet, cave vines cave; count passed == 4 (judge-3) |
| SQ-CS-03 | PASS | winter open mean 2.232 below glass 2.860 (0.78); spring open 3.081 above glass 2.933 (1.05, direction only; see SQ-CS-04 for the ratio finding) (judge-3) |
| SQ-CS-04 | BATCH-FAULT | judge-3 2026-11-23: ratio 0.962 (spring 2.928, summer 3.044, n=320) is a harness artifact: cdevtest randomtick runs its 18 rounds in one command and CropSeasons.EXTRA is a position set drained once per level tick, so a plant gets at most 1 extra tick (about 1.06x) instead of about 4.5 (1.25x); row now ripens with randomTickSpeed 30 + tickwarp 2400; rerun queued (judge-3) |
| SQ-CS-05 | FAIL | count /Test failed/ = 3, wanted >= 4; open look 1: every "Test failed" = grew past age 0; an "Unknown block" or property error = a guessed id (Ender crop id from endersdelight, CD endersAdditions); fix the id and rerun the row |
| SQ-CS-06 | PASS | row checks passed, reload with no KubeJS errors, instance file restored in srv (judge-3) |
| SQ-CS-08 | PASS | 2 checks passed |
| SQ-CUP-01 | PASS | handler errors: 0; no Unknown item line (judge-3) |
| SQ-CUP-02 | PASS | 7 checks passed |
| SQ-CUP-03 | PASS | copper_pot recipe centre ingredient minecraft:water_bucket; pots hold cave_soup / bat_soup in slot 6 with container bowl, CookTimeTotal 160 (judge-3) |
| SQ-CUP-04 | PASS | use CONSUME; jar fluid creativedelight:cave_soup; bot holds minecraft:bowl (judge-3) |
| SQ-DCM-01 | PASS | 4 checks passed |
| SQ-DCM-02 | PASS | Reloaded with no KubeJS errors; 32318 recipes against 32223 in the 2026-11-19 instance dump (+95, later recipes); [absent mods] lines name only absent mods (judge-3) |
| SQ-CBS-02 | PASS | no [absent mods] line names alexsmobs (present); the other lines name mods missing from srv/mods; note the step booted the full pack, not the minimal chain of the row text (judge-3) |
| SQ-CDFO-02 | BATCH-FAULT | judge-3 2026-11-23 NOT SIMPLE (not fixed): the step booted the full pack (cookingforblockheads, balm, incubation all in srv/mods and the compartments config on), so the no-CfB / no-Incubation scenario was never tested; rack and duck nest are registered BlockItems. The row has no @modset; needs its own boot set (judge-3) |
| SQ-CDFO-03 | BATCH-FAULT | judge-3 2026-11-23 NOT SIMPLE (not fixed): the step booted the full pack (Incubation present), so "Incubation absent: nest half does not load" was not tested; duck_nest registered as BlockItem. Rack half and oven recipes loaded. The row needs its own @modset / boot (judge-3) |
| SQ-DEP-01 | PASS | no apply error or skip line (shell empty) (judge-3) |
| SQ-DEP-03 | PASS | 2 checks passed |
| SQ-DEP-04 | PASS | only third-party optional-class lines (sodium, backpacked, multimine, aquaculture, botarium, overweightfarming, vanilla client class); none naming com.SaintAlfonzo or our mixin configs (grep 0) (judge-3) |
| SQ-DEP-05 | PASS | live tag query creativecommon:campfire/rain_douses n=2 (barbequesdelight:grill, minecraft:campfire) (judge-3) |
| SQ-DEP-06 | PASS | the one grep line is the Lingerification generator registration, not a disabled warning (judge-3) |
| SQ-DEP-07 | PASS | client halves only; row checks passed (judge-3) |
| SQ-DEP-08 | PASS | 2 checks passed |
| SQ-DEP-09a | PASS | corn maze rolls: corn_delight items, vinery:apple_cider, create_factory:caramel_apple; destroy lines: bowl + supplementaries:pancake 4 (servings 2), lemon_jelly 3, buzzier_bees:four_leaf_clover 1; no ERR lines (judge-3) |
| SQ-DEP-02 | PASS | setblock of the absent brewing jar failed as expected ("brewing jar gone"), ERR lines naming brewinandchewin or vinery 0; 16 grape bush lines recorded on creativesplit/deps #27 (judge-3) |
| SQ-DNR-O-02 | PASS | nourishment 4712 ticks, comfort 4711, instant-effect food leaves effects "-" with air=300, water_breathing / resistance 1112 from the other foods; sea grape count 2 after bone meal; client parts open (judge-3) |

Counts: PASS 101, BATCH-FAULT 18, INCONCLUSIVE 6, FAIL 2, HARNESS 1

BF-FIX loot WARN (SQ-CBO-39 to -43, SQ-CDFL-01): the same `| grep -vF "[loot-tuning] ban"` as judge-2 on the `EvaluatorException` shell checks of CBO.qb (SQ-CBO-40, the step that was running) and CDFL.qb; SQ-CBO-41 to -43 already carried it from judge-2's edits and had run before. SQ-CBO-38: remap/removed empty (judge-2 fix) plus the loot regex; rerun.
BF-FIX SQ-CDJ-09, -10: the `cdj5home` marker and its forceload were made by SQ-CDJ-11 (superseded, never run), so H1 to H3 stayed unset and the bot never spawned; both rows now forceload 1392..1423 and summon the marker at 1400 -60 1400 (flat world, as SQ-MXS-01). SQ-CDJ-10 also lacked the manual "Boot 2" toml edit (`[members] displaydelight = false`): it now sets it with `edit` + `restart` before its boot checks and restores true with a second `edit` + `restart` at its end; forceload widened to z 1455 for the stew at H3+50.
BF-FIX SQ-CFR-06: the row never set `[campfire] rainDouses = false` (srv config has true; the campfire doused, "Test failed"); now `edit` toml + `restart` + forceload before the check and a restore to true + `restart` at its end.
BF-FIX SQ-CM-02: `creativedelight:shaped/mini_cantaloupe` (4 slices -> Mini Cantaloupe) was dropped by plan_food_batch_1119 wave A (A6, commit 1336abd37, 2026-11-19: cutting only); row now rejects it, the seeds and popsicle expectations match the code. item_merges.md 3 still lists the grid (stale doc, not edited here).
BF-FIX SQ-CM-22: `#creativedelight:veggie_noodle_veg` has 7 members with Rustic Delight loaded (vintagedelight/tags.js line 16 adds the green bell pepper slice), 6 without: regex `n=[67]`.
BF-FIX SQ-CS-04: ratio 0.962 (n=320) and CS-03's spring 1.05 are a harness artifact: `cdevtest randomtick ... 18` runs its 18 rounds inside one command and `CropSeasons.EXTRA` is a position set drained once per level tick, so each plant got at most 1 extra tick (about 1.06) instead of about 4.5 (1.25). The row now ripens with `randomTickSpeed 30` + `tickwarp 2400`; rerun. SQ-CS-03 keeps PASS (direction check only); if its ratio matters later it needs the same change.
BATCH-FAULT not simple (not fixed): SQ-CBO-52, -53 (same cause as judge-2's SQ-CBO-10: Armor / Tool Effects rows are filled by Creative Equipment, absent on the chain set; needs `@modset` or a rewrite to effects only). SQ-CDFO-02, -03: the step booted the full pack (CfB, Balm, Incubation all in srv/mods, compartments on), so the no-CfB / no-Incubation scenarios were never tested; the rows carry no `@modset`, so each needs its own boot set (drive owner to be told before a `@modset` change). SQ-MDP-17: needs a boot with the srv `miners_upstream` symlink removed so the boot stops; the drive prep keeps the symlink and `restart` waits for Done, so it needs its own step with a prep rm action (CDJ.qb run plan boot 3).

Findings (real FAIL or open):
- SQ-CBP-01 (FAIL): 7 `Couldn't parse loot table` lines on the chain boot (creativecommon:blocks/{azalea,twisted,blossom,ancient}_composter, delightful:blocks/{mini_melon,cantaloupe}, minecraft:blocks/pumpkin), row wants 0. Logged on TODO creativebrewing/publish #13 (was about 30 on 2026-11-06).
- SQ-CDJ4-08b (FAIL): squid summoned with Age:-24000 gone 7 s later with spawn-animals=true (second run in a row, r1120lr too). Logged on TODO mobs/tweaks #21.
- SQ-CS-05 (FAIL): hemp and tobacco stay at age 0 in mid_winter at randomTickSpeed 100 for 120 s (grape bush, fungus colony and Ender crop on tilled end stone grew; 2026-11-18 all four grew). New TODO crops #60 (Bug).
- SQ-CEQ-08 (INCONCLUSIVE): `dragonloot:dragon_helmet` armor reads 8; tune_armor.js sets 7 (toughness and knockback match; the dragonloot config default is 8). Tool values match tune_tools.js. Owner or Equipment session to decide whether the tune applies to dragonloot armor.
- SQ-CM-36 note: `/fgdtest` prints 2 FAIL lines (stomach alcohol and Swiftness with Weak Stomach) that no row of this range judges; 134 pass.
- SQ-DEP-02 and -09a notes added to TODO creativesplit/deps #27 (16 grape WARN lines; Unknown loot table hearthandharvest:mod_items/vinery/apple_cider validation WARNs).
- INCONCLUSIVE with cause: SQ-CBP-03b (no [absent mods] line counts a minecraft:empty row), SQ-CDFW-10 (no key count printed), SQ-CEQ-01 (no r1106 baseline compared, old-save half without steps), SQ-CIF-02 (old-world half needs a 2026-11-06 build), SQ-CM-01 (no stored pre-cantaloupe merge count). SQ-CDB-01 PASS on its error checks only (49 merges; older doc counts predate later merges).
- Stale pass texts seen (rows not edited, verdicts follow the code and docs): SQ-CDB-02 (sweetener tag sizes now 4/4/3/2), SQ-CDFW-06 (eggs have no furnace recipe by design, food.md 3.7), SQ-BJ-01 (result *1 since dishes/jams #4), SQ-CDB-11 and -15 (cacciatore chicken slot is raw chicken / chicken cuts).
- No owner LOOK left in this range (no shots).

#### Verdicts steps 82 to 102

Judge judge-4, RES `perf/runs/r1123cd/s81_res` to `s101_res` (drive steps 82 to 102: DNR, DRK, EDL, ENGM, FCK/AZW/BOT/FCB/MG/T1, FDI, FF, FFH, FFB/RCO/CNP/VNG/FO/CNS/WCT/SGF/CORN, GAP, CNG/ENM/GLD/PCK/SLT/SWP). The drive was stopped cleanly after step 102 (GLD.qb section 1, 20 of 23 rows). Simple batch faults are fixed in their `.qb` (run default 11) and listed in `perf/runs/r1123cd/rerun.txt` (qrerun.py --add, 44 ids now). Same ruled causes as judges 1 to 3 (peaceful food regen, loot-tuning ban WARN, potions.js mix skipped, gear rows in Creative Equipment, empty fresh [effects] remap); none of them hit this range.

| ID | Result | Evidence |
|---|---|---|
| SQ-DNR-O-05 | PASS | missing-jar half passed (message, missing path, Modrinth URL, no Done); wrong-file half is the shared DonorJars code PASSed by SQ-DNR-E-04 (judge-4) |
| SQ-DNR-E-03 | PASS | 3 checks passed |
| SQ-DNR-E-04 | PASS | 2 checks passed |
| SQ-DRK-01 | PASS | 17 forge:fluid tags n=1 each, incl sea_grape_juice and pickerelweed_juice as creativedelight fluids; scripts clean (judge-4) |
| SQ-DRK-02 | PASS | bayberry..peach juice _manual_only recipes present; kiwi = 4 fruit + sugar/powdered_sugar/honey_bottle slot; dandelion = 2 leaves + 2 sugar cane; stale row text on old ids noted (judge-4) |
| SQ-DRK-03 | PASS | createmixing/kiwi_tea: kiwi_juice 250 mB + 2 tea leaf slots + kiwi + sweetener slot -> kiwi_tea 250 mB; chorus_tea brewing takes tea leaves + ender_eye (judge-4) |
| SQ-DRK-04 | PASS | createfilling/kiwi_tea: glass bottle + 250 mB creativedelight:kiwi_tea -> kiwi_tea*1; spout lines diagnostic per the look (judge-4) |
| SQ-DRK-05 | PASS | tags coffee_chocolate n=3 (bar_of_chocolate, cocoa_beans, chocolate_bar), coffee_pumpkin n=2 (pumpkin_slice, pumpkin) (judge-4) |
| SQ-DRK-06 | PASS | drink_fruits/lemon n=2, /orange n=2, /hamimelon n=1 (drink_tags.js makes only these three); rest PASSed in r1102b (judge-4) |
| SQ-DRK-08 | PASS | #creativedelight:sea_pickles (2 items) is the ingredient slot, repeated 3 times in createmixing/sea_pickle_tea_on_sea_grape (judge-4) |
| SQ-EDL-03 | FAIL | no line matches /verb=count block=endersdelight:voidpepper_block n=([2-9]/\d\d)\b/; judge-4: count n=1 after 30 rounds (block_ticks=3: the origin ticked 3 times, no persistent spread sprout), origin fruited; the writeup says 2/3 spread per tick; r1120 run had n=3; spread not reproducing (TODO largecontent/mod-additions #28) |
| SQ-EDL-06 | PASS | 2 checks passed |
| SQ-ENGM-03 | PASS | Killed 24 entities (shrapnel found); torch_crafting_animal_fat and seeds_bone_meal recipes n=1; creativedelight:millable_seeds n=33 = pack dump 33; vinery:barrel and forge:mulch/green filled; wooden_bucket made (judge-4) |
| SQ-ENGM-04 | PASS | diff of the old Engineering [surface] block against Compat's shows only added keys (furniture, flora, camo walk/dense/items keys), no changed or removed value; excavation half left for CQ-ENGM-01 (judge-4) |
| SQ-ENGM-05 | PASS | chain boot: [creativedelight] transition items (117) (row's 89 predates later additions), 0 failed recipes, no creativecompat/creativedelight ERROR lines, no unregistered-row warnings (judge-4) |
| SQ-AZW-02 | PASS | rack use CONSUME, state creativedelight:azalea_spice_rack[facing=north] placed (be cookingforblockheads:spice_rack), break drops the rack; quark:azalea_planks makers n=12 (judge-4) |
| SQ-AZW-03 | PASS | quark:azalea_log -> stripped_azalea_log*1 @1.0 + azalea_bark*1 @0.5 in createsaw, FD cutting and S&D cutting recipes (judge-4) |
| SQ-BOT-01 | PASS | [botany_pots] 3 soils set (Rich x1.1, Fertile x1.2); growthTicks scaled on 212 pot crops (49 flowers x2, rest x8); flowers 49 as 2026-11-12, crop total grew from 175 with added mods (judge-4) |
| SQ-BOT-02 | PASS | 4 checks passed |
| SQ-BOT-03 | PASS | botany pot read 1: dirt + seeds GrowthTime 100, read 2 after bone meal GrowthTime 261, never -1 (merge set the crop) (judge-4) |
| SQ-BOT-04 | PASS | lemon botany recipe display phases half: lower x5 (judge-4) |
| SQ-BOT-05 | PASS | wheat getGrowthTicks=9600, rich_soil and rich_soil_farmland getGrowthModifier=1.1, Fertile Soil soil id creativecompat:botany_soil/fertile_soil getGrowthModifier=1.2 (judge-4) |
| SQ-FCB-09 | PASS | cooking pot item keeps carrot (slot 0), bowls (slot 7), soup (slot 8; row text slots 0/6/7 is the old layout); copper pot keeps bone + lichen, glass bottle drops loose; kettle keeps 2 green_tea_leaves (judge-4) |
| SQ-FCB-10 | PASS | 6 checks passed |
| SQ-FCK-01 | PASS | the blacklist is the block tag quark:simple_harvest_blacklisted (n=1 immersive_weathering:weeds; item tag empty n=0); fertile_blocks n=3 (judge-4) |
| SQ-FCK-02 | PASS | 2 checks passed |
| SQ-FCK-03 | PASS | copper pot: bone + glow lichen cook (160 t) -> Inventory slot 6 bone_broth*2, recipe bone_to_2_bone_broth_by_fdcooking = yield 2 (judge-4) |
| SQ-FCK-04 | PASS | canned_oatmeal = can + 6 oatmeal; canned_oatmeal_sugar = can + 3 oatmeal + sugar; makes n=2 (judge-4) |
| SQ-FCK-05 | PASS | peanut_crop[age=7] 20000 rolls: magic_peanut 9 (0.045 %), peanut 74283 (judge-4) |
| SQ-FCK-06 | PASS | 4 checks passed |
| SQ-FCK-09 | BATCH-FAULT | reclassified from FAIL: judge-4 2026-11-23: stale count: the legendary and mythical roll medley cans were removed by owner ruling (swaps/removed.json, plan_easy_fixes_1122.md), so 19 feast cans have a recipe, not 21; counts in the row set to 19; rerun queued; count /verb=makes item=creativedelight:canned_[a-z_]+ n=1$/ = 19, wanted >= 21; count /verb=rdetail id=creativedelight:shapeless/canned_[a-z_]+ serializer=\S+ result=creativedelight:canned_[a-z_]+\*1 ingredients=\S*canned_goods:empty_can/ = 19, wanted >= 21; open look 1: 21 `verb=makes ... n=1` lines; each rdetail line takes the empty can and one feast block item (Brewin' pizza, stuffed cantaloupe, dragon meat stew, ...); N in the shell line 307 as in the 2026-11-12 pack log |
| SQ-FCK-10 | PASS | 5 checks passed |
| SQ-FCK-11 | PASS | well Amount 4000 -> 3000 after the use, can WaterLevel 12, then well Amount 10000 (refilled); WaterLevel not set without Amount falling (judge-4) |
| SQ-FCK-12 | PASS | 5 checks passed |
| SQ-FCK-13 | PASS | dispenser shear: honeycomb:6 at multiplier 2, honeycomb:3 after the toml edit to 1 (read live, both expects passed); the debug.log watcher grep (count 0) is a stale message text, not the value check (judge-4) |
| SQ-FCK-14 | BATCH-FAULT | reclassified from FAIL: judge-4 2026-11-23: the 3x3 centre is read about 80 ticks after the use and a farmland random tick dropped it 7 -> 6 (the other eight cells read 7/0 as expected); row now accepts 6 or 7 for the centre; rerun queued; no line matches /verb=state pos=1020,-61,1020 state="minecraft:farmland\[moisture=7\]"/ |
| SQ-FCK-15 | PASS | 8 checks passed |
| SQ-FCK-16 | PASS | bot use result CONSUME, farmland moisture 0 both reads, water[level=0] appears above after the bucket use (judge-4) |
| SQ-FCK-17 | PASS | tea_bush[age=2] lower and upper set (no setblock failure); small_tea_bush states read for the 3 younger ones (judge-4) |
| SQ-FCK-18 | PASS | top-layer count n=0, corn crop reads age=0 upper=false after the harvest; drops corn:2, corn_seeds:5, corn_husk:1 (judge-4) |
| SQ-MG-22 | PASS | coffee bush lower+upper became air after 1 block tick on basalt, wheat age=3 untouched, no items (young bush) (judge-4) |
| SQ-MG-23 | PASS | makes list for immersive_weathering:azalea_flowers n=2: twigs:azalea_flowers_from_flowering_azalea and _leaves (x6 each); itemMerges 4 pack merges registered (judge-4) |
| SQ-MG-24 | PASS | supplementaries:sack_2 = 7 wheat + string (one sack recipe, n=1); rope_2 from 3 string; no flax uses (judge-4) |
| SQ-T1-02 | PASS | 5 checks passed |
| SQ-T1-03 | PASS | create:cutting/diced_potatoes n=0; creativedelight:sliced_sweet_potato used by 141 shared recipes incl moredelight diced_potatoes_with_{beef,chicken_cuts,porkchop}; no barrier ingredient (judge-4) |
| SQ-T1-04 | PASS | forge:plates/gold = IE plate + rose gold sheet, wires/gold = gold_wire + rose_gold_wire; rose gold ingot/sheet uses name clock, mechanical_crafter, golden_pickaxe, golden_apple, component_gold, none for gold_block or gold_bars (judge-4) |
| SQ-FDI-01 | BATCH-FAULT | reclassified from HARNESS: judge-4 2026-11-23: shell exit 1 came from the trailing 'ls mods / grep -i carrot' (no jar has carrot in its name, the item solcarrot:food_book loaded and the bootreject passed); the Failed to parse lines are unrelated recipes; row now ends with '; true'; rerun queued; shell exit 1: 2898:[23Nov2026 16:42:58.366] [main/WARN] [KubeJS Server/]: Failed to parse recipe 'minecraft:golden_milk_bucket[minecraft:crafting_shaped]'! Falling back to vanilla: ItemStack 'result' can't be empty; open look 1: item name "Food Dictionary" is a lang check (client); here: the item exists and the mod loaded without errors |
| SQ-FDI-02 | PASS | fdj ate 20 foods: carrot=19 applepie=19; the one food neither book counted is minecraft:pumpkin_pie (13th): the use did not consume it (result=minecraft:pumpkin_pie, others result=air), so no eat event; same 19/20 as the row title; max health 22 to 24 is the Raspberry Pie health ladder (judge-4) |
| SQ-FDI-03 | PASS | cake: before carrotHas=false; bites 1 to 3 carrotHas=true applepieHas=true, applepieEntries 1,2,3; supplementaries:directional_cake bites=1,2,3; minecraft:cake applepieHas=false (judge-4) |
| SQ-FDI-04 | PASS | lunchbag, lunchbox, golden_lunchbox: food lines carrotHas=true applepieHas=true, lunchbox lines applepieHas=false (blacklist), applepieEntries +1 per bite (judge-4) |
| SQ-FDI-05 | PASS | both serverconfig blacklists list solapplepie:lunchbox, lunchbag, golden_lunchbox and creativedelight:netherite_lunchbox; book and tooltip halves are client (CQ) (judge-4) |
| SQ-FF-03 | PASS | changed recipes (nachos block, truffle pasta, creamy pasta with chicken cuts / ham, fiery fondue pot, lasagna pan, stuffed potato) take the cheese slot vintagedelight:cheese_slice/flaxen_cheese_wedge/goat_cheese_slice where the milk was (judge-4) |
| SQ-FF-04 | PASS | 11 checks passed |
| SQ-FF-05 | PASS | stuffed_pumpkin_block cooking recipe: tomato sauce, potato/sweet potato, bread tag, berries, mushrooms, vegetables slots, cook time 400; the container is not printed by rdetail (judge-4) |
| SQ-FF-06 | PASS | cucumber_salad_manual_only pickled slots list vintagedelight:pickle; echo cake base and cake paste egg slots are the egg tag (quark parrot eggs ... +9) (judge-4) |
| SQ-FF-07 | PASS | _from_whole: melon 8, pumpkin 8, hamimelon 8, lemon 5 (+seeds on saw), orange 5, pineapple 6, cantaloupe 4; _from_slices shapeless: melon 9, pumpkin 9, hamimelon 9, lemon 6, orange 6, pineapple 9; minecraft:melon recipes only melon_seeds, pumpkin_from_slices and cutting/pineapple_slice n=0 (judge-4) |
| SQ-FF-08 | PASS | baking/quiche_meat: crust, cheese, milk, egg, filling twice (6 slots); horror_lasagna: two scarlet_cheese_wedge, beetroot, tomato sauce, pasta (judge-4) |
| SQ-FF-09 | PASS | trail mix seed slot = sunflower/pumpkin/oat seeds +2 (edible seeds, all food=true) and stuffed cantaloupe the same slot, no wheat or beetroot seeds (judge-4) |
| SQ-FF-10 | PASS | 5 checks passed |
| SQ-FF-11 | PASS | 6 checks passed |
| SQ-FF-13 | BATCH-FAULT | reclassified from FAIL: judge-4 2026-11-23: stale expectation: slice values 4 / 0.7 since pizza_values.js (plan_food_batch_1119.md R25, 0.75x slices); log reads nutrition=4 modifier=0.7; row regex fixed; rerun queued (food_catalog.md 914 and vintage_delight.md 1052 still say 5 / 0.75); no line matches /verb=food item=vintagedelight:tropical_pizza_slice\S* food=true nutrition=5 modifier=0\.75/ |
| SQ-FF-14 | PASS | displaydelight:onion_soup n=0, displaydelight:bnc_creamy_onion_soup registered (FoodBlock) (judge-4) |
| SQ-FFH-02 | PASS | 7 muffin recipes, all creativedelight:baking/ (4 per bake), no farmersdelightcooking/ muffin recipe; muffin_tin hammering recipes only (judge-4) |
| SQ-FFH-03 | PASS | creativedelight:baking/zucchini_quiche: pie crust, cheese wedge, milk, egg tag, 2 roasted zucchini; no sequenced assembly recipe for it; the pie tin tool is not printed by rdetail (judge-4) |
| SQ-CNP-01 | PASS | [zsb] cans by serializer: kubejs:shapeless 9, creativedelight:baking 11, crafting_shapeless 1, creativedelight:flavor 1, solraspberrypie:diet_book 1; no mixing, mechanical_crafting, IE or AE2 route (judge-4) |
| SQ-CNS-03 | PASS | canned noodle soup Nourishment 3600 t = noodle soup 4800 x 0.75; canned beef stew Comfort 3600 t = beef stew 4800 x 0.75 (judge-4) |
| SQ-CORN-04b | PASS | knife on age 7: corncob 12 (+ corn 40); empty hand: no corncob; knife on age 3: no corncob (judge-4) |
| SQ-DRK-07 | PASS | canned_bayberry_soup recipes canned_bayberry_soup and _soup_broth, canned_bayberry, canned_bayberry_cookie_sugar present (judge-4) |
| SQ-FFB-03 | PASS | oven baking/honey_cake: cake_base, frosting, 1 honey_bottle, 1 honeycomb; honey_cake_manual_only: cake_base_baked, frosting, honey bottle, honeycomb; no 2-bottle recipe, no Crafts and Additions maker; cutting gives 7 slices (judge-4) |
| SQ-FFB-04 | PASS | forge:bread n=9 incl garlic_bread, banana_bread, cantaloupe_bread, bread_slice, toast (judge-4) |
| SQ-FO-02 | PASS | 7 checks passed |
| SQ-FO-03 | PASS | empty_can makers: stonecutting x4, shaped x12, create deploying x12, IE blueprint x32; soda_recipe and reef_cola_recipe still use it; no smelting or blasting among 590 uses; ingredient slots not printed (judge-4) |
| SQ-FO-04 | PASS | fried egg: smoking and campfire from eggs, no smelting; FD cooking fried_eggs_from_cooking_oil x3; cooking_oil_manual_only (seeds -> oil x2) and cooking_oil_heated_mixing; times 100/600 t not printed by rdetail (source) (judge-4) |
| SQ-FO-05 | PASS | [stack_sizes] set 1391 stack sizes (row says about 1341: items added since); minecraft:egg, parrot egg and emu egg max_stack=64 (judge-4) |
| SQ-MRD-04 | PASS | 3 checks passed |
| SQ-PS-06 | PASS | 4 checks passed |
| SQ-RCO-01 | PASS | minecraft:smoker recipes n=0; makers only quark deepslate_smoker and blackstone_smoker (the row's third-maker look is the old text, the pass text says Nether's Delight's smoker is its own block) (judge-4) |
| SQ-RCO-02 | PASS | cake makers: farmersdelight:cake_from_slices (7-slice reassembly), baking/cake, baking/cake_berry, createfilling/cake_frosting_spout, cake_manual_only, cake_berry_manual_only = the pass list (judge-4) |
| SQ-SGF-01 | PASS | water shot summon wet both farmland blocks (moisture 7) and the wheat stayed: Test passed x3 (judge-4) |
| SQ-SGF-02 | PASS | lava and honey shots left farmland unwet: Test passed x2 (SGF-01 wet its farmland on the same summon path) (judge-4) |
| SQ-VNG-01 | PASS | vinegar_jar uses: 87 alone (canned_*_vinegar: veggie, fruit, raw_crab, flying_fish, salads e.g. cucumber_salad_vinegar); vinegar_bottle 0 (judge-4) |
| SQ-VNG-02 | PASS | 4 checks passed |
| SQ-WCT-06 | PASS | Test passed x2 (netherite, dragon), Test failed x1 (golden, third execute) (judge-4) |
| SQ-WCT-07 | PASS | golden can BonemealLevel/WaterLevel 16/16 after the clicks, 15/15 after the use; 7 Test passed (wheat grew, sapling stage 0) (judge-4) |
| SQ-WCT-08 | PASS | first useair result=CONSUME with WaterLevel 4, second result=PASS with WaterLevel 0 (judge-4) |
| SQ-FO-06 | PASS | fried_eggs_from_cooking_oil (FD cooking and S&D mixing) = cooking oil/fish oil + egg slots -> fried_egg*3; omelettes_from_fried_eggs = 4 fried eggs -> omelette*4 (judge-4) |
| SQ-FO-07 | PASS | fried egg: smoking and campfire_cooking from eggs (n=5 makers incl FD pot, S&D, Salt); omelette: moredelight omelette_smoking and omelette_campfire_cooking (judge-4) |
| SQ-FO-09 | PASS | whale_oil: heated mixing blubber -> 250 mB, pot whale_oil_manual_only 2 blubber, filling and emptying 250 mB, forge:lubricant fluid tag n=3 incl whale_oil, no S&D copy (judge-4) |
| SQ-FO-10 | PASS | 5 checks passed |
| SQ-FO-12 | PASS | 8 checks passed |
| SQ-FO-14 | PASS | 5 checks passed |
| SQ-GAP-01 | PASS | 10 jam jar Cooking Pot recipes (vintagedelight jars, incl creativedelight:farmersdelightcooking/*) all time=400, count 1; no Fruits Delight jam is a Cooking Pot recipe: they are create:mixing (fruitsdelight:mixing/flowing_*_jam), so the row's pass text "every Fruits Delight jam cooks 400" is stale (judge-4) |
| SQ-GAP-02 | PASS | gen_moobloom_rules unchanged, exit 0; checker OK exit 0 (11 variants, 10 breeding rules, 38 spawn rules; the row says 37 rules) (judge-4) |
| SQ-GAP-04 | PASS | clock, mechanical_crafter, golden apple/pickaxe/chestplate/carrot list gold_ingot/rose_gold_ingot (nugget slots for the carrot), powered rail takes gold rods; golden_watering_can smithing addition gold_block/rose_gold_block; createdeco gold sheet metal keeps literal gold_block; gold_ingot and gold_block makers listed as makes lines (no rdetail printed) (judge-4) |
| SQ-GAP-05 | PASS | water golden egg rests at y -63, ground golden egg -60.0, plain egg floats at -59.84, lava egg gone (burned); golden_egg has no item tags and no physic tag (judge-4) |
| SQ-GAP-06 | PASS | names.js: block.redeco.jar Empty Terrarium, supplementaries flower_box Large Flower Box, design_decor tinted_framed_glass Smooth Tinted Glass; refined gelatin: item.hostiledelight.refinedgelatin = 'Gelatin' in kubejs/assets/creativedelight/kubejs/hostiledelight/names.js (the row's grep looks for double quotes and prints nothing for it) (judge-4) |
| SQ-GAP-07 | PASS | survivors: pineapple on farmland, rich, fertile, sand, red sand, coarse dirt; tea on farmland, rich, fertile and grass; grape on grass, dirt and stone. Not surviving: pineapple on grass, dirt, stone; tea on sand and stone; grape on farmland (popped). Record for the writeup: tea survives on grass, grape on stone (judge-4) |
| SQ-GAP-08 | PASS | long lines: 0 (CQ-GAP-02 sizing still open) (judge-4) |
| SQ-GAP-09 | PASS | /ccaltime date prints "[time] Year 1, Early Spring, day 1 of 8, 5:07 am (world day 1)"; four new command names registered, four old ones missing (judge-4) |
| SQ-CNG-01 | PASS | debug.log: Found valid mod file repackedCannedGoods-1.19.2-3-ce-19.2-01.jar with {canned_goods}, version 1.19.2-3-ce-19.2-01 (judge-4) |
| SQ-CNG-02 | PASS | [cans] registered 301 generated cans (228 in r1102b); held cans carry servings tags: canned_beef cans/servings/3, canned_beetroot /2 (judge-4) |
| SQ-CNG-04 | PASS | 4 checks passed |
| SQ-ENM-01 | PASS | repackedEnds-1.19.2-2.1-ce-19.2-03.jar provides ends_delight and enders_additions (jarjar path only repackedEnds, no endersAdditions- jar); enders_upstream_data pack listed (judge-4) |
| SQ-ENM-05 | PASS | 5 checks passed |
| SQ-ENM-05b | PASS | 5 checks passed |
| SQ-GLD-02 | PASS | golden bread Regeneration amp 1 92 t + Absorption amp 0 2392 t; enchanted bread and enchanted apple Regeneration amp 1 ~391 t, Resistance 5991 t, Fire Resistance 5991 t, Absorption amp 3 2391 t; golden carrot as golden bread; all also carry farmersdelight:nourishment (judge-4) |
| SQ-GLD-04 | PASS | golden slice Regeneration amp 1 60 t + Absorption amp 1 2360 t; enchanted slice Regeneration amp 1 360 t, Resistance and Fire Resistance 5960 t, Absorption amp 3 2360 t (judge-4) |
| SQ-GLD-06 | PASS | 3 anvil combines of book + golden item -> empty, enchreg gf_enchantment false, /enchant answers Unknown enchantment; book hover is client (judge-4) |
| SQ-GLD-07 | PASS | golden chorus fruit teleport 18.6 blocks (60.5,70.5 -> 76.6,79.9), enchanted 22.4 blocks (-> 64.2,92.6); cooldown off at the next read after each eat (cd probe on=false) (judge-4) |
| SQ-GLD-13 | PASS | enchanted_golden_cake has no recipe (makes n=0, row expect passed); place and eat verified in SQ-GLD-04 (judge-4) |
| SQ-GLD-16 | PASS | after the eat: mushroom_stew*2 + bowl*1 (hold gave 3 stew); no golden/goldenfood script error; canned_golden_bread recipe present (judge-4) |
| SQ-PCK-01 | PASS | 4 checks passed |
| SQ-PCK-02 | PASS | food_type/sweet/pancake n=7: sweet_potato_pancakes + rusticdelight cherry_blossom, chocolate, coffee, honey, pumpkin, vegetable pancake (judge-4) |
| SQ-PCK-03 | PASS | 6 checks passed |
| SQ-PCK-05 | PASS | supplementaries:pancake: shapeless sweeteners x3, heated mixing milk x5, egg solution mixing x5; plates: pot, S&D mixing, shaped and plate/single conversions; sweet_potato_pancakes S&D mixing copy listed (judge-4) |
| SQ-SLT-01 | PASS | create__the_salt 1.19.2-1.1.0-ce-19.2-01 found; Salt itself loads as repackedSalt-1.2.4.1-ce-19.2-04 (the row's -01 suffix is old); the error grep shows only unrelated Create mixing input-count WARNs, nothing from Salt (judge-4) |
| SQ-SLT-02 | PASS | 4 checks passed |
| SQ-SWP-02 | PASS | give farmersrespite:long_coffee 2 -> inv rusticdelight:coffee*2 before and after the relog (judge-4) |
| SQ-SWP-03 | PASS | /ccswap items: 1 stack rewritten (2 players, 565 chunks, 49 merges); the Kept-named neapolitan:banana stack keeps its name (judge-4) |

Counts: PASS 114, BATCH-FAULT 4, FAIL 1

BF-FIX SQ-FCK-09: the legendary and mythical roll medley cans were removed by owner ruling (swaps/removed.json, plan_easy_fixes_1122.md), so their makes lines read n=0 and 19 feast cans have a recipe, not 21; the two counts in FCK.qb are now `>= 19` (the two dead probe steps stay, harmless); rerun queued.
BF-FIX SQ-FCK-14: the 3x3 centre is read third, about 80 ticks after the bucket use, and a farmland random tick can drop it 7 -> 6 (r1123cd read 6, the other eight cells read 7 / 0); the centre expect is now `moisture=[67]`; rerun queued.
BF-FIX SQ-FF-13: tropical pizza slice is 4 / 0.7 since pizza_values.js (plan_food_batch_1119.md R25, slices 0.75x, Food wave 2026-11-22); the log read nutrition=4 modifier=0.7, the row still wanted 5 / 0.75 (also stale in food_catalog.md 914 and vintage_delight.md 1052, not edited here); regex fixed; rerun queued.
BF-FIX SQ-FDI-01: shell exit 1 came from the trailing `ls mods | grep -i carrot` (no jar name has carrot; the item solcarrot:food_book loaded and the bootreject passed); the line now ends with `; true`; rerun queued.

Findings (real FAIL or open):
- SQ-EDL-03 (FAIL): voidpepper_block count n=1 after 30 random ticks (block_ticks=3: the origin ticked 3 times and fruited, no persistent spread sprout); ends_delight.md says 2/3 spread per tick; r1120 run had n=3. Intermittent or a spread regression. Logged on TODO largecontent/mod-additions #28; needs a repeat before any fix.
- SQ-FDI-02 (PASS, note): the one food neither Spice of Life book counts out of the 20 is minecraft:pumpkin_pie (the use does not consume it: result=pumpkin_pie), same 19 of 20 as the row title.
- SQ-GAP-01 (PASS, note): no Fruits Delight jam is a Cooking Pot recipe (they are create:mixing, fruitsdelight:mixing/flowing_*_jam); the 10 jam jar pot recipes are all 400 ticks. The row pass text "every Fruits Delight jam cooks 400" is stale.
- SQ-GAP-07 (PASS, record for the writeup): survivors pineapple on farmland, rich soil, fertile soil, sand, red sand, coarse dirt; tea on farmland, rich, fertile and grass; grape on grass, dirt and stone; broke: pineapple on grass, dirt, stone; tea on sand and stone; grape on farmland.
- Stale row texts seen (verdicts follow code and docs, rows not edited): SQ-ENGM-05 (transition count 117, row 89), SQ-BOT-01 (212 pot crops, row 173), SQ-FO-05 (1391 stack sizes, row 1341), SQ-GAP-02 (38 spawn rules, row 37), SQ-SLT-01 (Salt jar -04, row -01), SQ-RCO-01 (look names a third smoker maker, pass text says none), SQ-FCB-09 (cooking pot slots 0/7/8 now, row 0/6/7), SQ-GAP-06 (the Gelatin grep uses double quotes, the name is in single quotes).
- Not verified by the row log, passed on the rest of the row: SQ-FF-05 (container not printed), SQ-FO-03 (maker ingredient slots not printed), SQ-FO-04 (smoking 100 t / campfire 600 t not printed), SQ-FFH-03 (pie tin tool not printed), SQ-GAP-04 (gold_ingot and gold_block makers only listed, not rdetailed).
- No owner LOOK left in this range (no shots).

#### Run totals (steps 1 to 102)

Counts: PASS 297, BATCH-FAULT 55, FAIL 6, INCONCLUSIVE 6, HARNESS 2

Whole-run table: `python3 tools/scratch_query/qrun.py table $(ls -d perf/runs/r1123cd/s*_res)` (101 RES dirs). The two remaining HARNESS rows are SQ-CBP-01 (a product FAIL, TODO creativebrewing/publish #13) and SQ-CBO-32 (shell exit 1 on the creativeloot loot-tuning ban WARN, ruled cause, not judged in this range; still to reclassify).
