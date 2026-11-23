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

