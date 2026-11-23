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
