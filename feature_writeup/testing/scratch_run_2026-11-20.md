# Scratch run 2026-11-20 (r1120)

Owner request (2026-11-20): session creativeengineering2-ae orchestrates scratch testing. Another session
(creativeengineering2-6f) is building; its rows are fast-tracked once it reports the build done. Until then: low-risk
never-run server rows, oldest section first.

## 0. Performance capture

Perf session `r1120` (`tools/perf/session/perf_session.sh start r1120`).

## 1. Scope

Taken (filler, drive `r1120lr`): never-run server-phase rows from sections dated up to 2026-11-01 with no `@@modset`
and at most one `@@prep` line; 143 rows in 30 sections, oldest first: CDB 1, EN 1, SALT 2, SV 1, ATC 1, CL 1, GFB 1,
HT 1, IMS 1, IPO 1, POT 1, WE 1, CM 3, AC 1, MB 1, GF31 1, BBQ 1, BM 1, CDFW 1, CDJ4 1, CSI 1, DNR 1, LGM 1, MXS 1,
NDM 1, PT 1, ROLL 1, SSP 1, STK 1, VDF 1. Plan file: `perf/runs/r1120lr/plan_lowrisk.json`.

Not taken: undated sections (ABD, FFH, MRD, RD, STD), perf rows (PF, PB, PERFCFG), sections with a mod set or
multi-line prep, worldgen / long / client phases, drafts, jar-changed and failed rows. The drive is stopped at a row
boundary (`qrun.py drive stop r1120lr`) when the build session reports in; its rows run next as a separate drive.

## 3. Jars

Preflight 2026-11-20 00:20: `check_all.py --fast` 37 run, 0 failed. `jarstamp.py pending`: creativeBrewing,
creativeCalendar, creativeCommon, creativeCompat, creativeLoot built 2026-11-19 22:33 to 22:45, not installed;
prep_srv.sh swaps these build jars in (newer than `mods/`). Stamps per step: `perf/runs/r1120lr/sNN/pinned.json`.

## 7. Run notes

- 00:23 a mistaken `qrun.py drive start --help` started a drive named `--help`; it failed at prep with no server
  booted. RUNNER.md restored from git, `perf/runs/--help/` deleted.
- 00:24 drive r1120lr started (31 steps, 143 rows), perf session r1120 open.
- 00:4x build session ("Raspberry Pi plan", plan_diet_books_r2) reported: solraspberrypie, creativecommon,
  creativedelight installed to `mods/` 2026-11-20 00:38; no queue rows for round 2 yet (plan section 3: rows follow the
  owner's client look-check), so nothing to fast-track. Drive steps prepped after 00:38 pin the new Common and
  Delight jars; earlier steps pinned the previous build (each step's pinned.json has the stamp). Unqueued server
  changes it named (Apple Pie endDecay 32, gateOpened flag, gear_effects packet): not run ad hoc, they wait for rows.
- 01:15 ENV FAULT step 11 (POT.qb 1): `zz_pot_probe.js#126 ReferenceError: "guardThrowable" is not defined`; every
  POT row waits 900 s for `PP-DONE`. Cause: the probe called `guardThrowable` / `writeExportJson` as bare functions,
  which since the Creative split live only in the Devtools script pack's scope. Fix: `tools/flower_pot_test/pot_probe.js`
  calls `global.guardThrowable` / `global.writeExportJson`. `drive stop r1120lr` requested at 01:17; the srv copy in
  s10 is patched before `drive resume`; POT rows that ran on the broken probe are rerun.
- 01:16 drive resumed; step 11 re-prepped with the fixed probe (no ReferenceError since). SQ-POT-02 judged
  BATCH-FAULT (broken probe), rerun after the drive. On the fixed probe: SQ-POT-03 FAIL (no PP-03 FAIL line for the
  Another Furniture planter box: the expected F1 exception is gone or the box was not probed), SQ-POT-04 FAIL (one PP-04
  FAIL), SQ-POT-04v FAIL (`/cdevtest break` gave no reply in 30 s). Judged at the end.
- 02:26 drive r1120lr done, 31/31 steps. Raw counts: PASS 25, FAIL 57, LOOK 59, HARNESS 1, BATCH-FAULT 1. Many
  FAILs look like stale expectations (sections 3 to 4 weeks old). Judges 1 to 3 (subagents) judge s00-s11, s12-s21,
  s22-s30.
- 02:35 rerun drive r1120pot: SQ-POT-01, -02 on the fixed probe.
- 02:34 r1120pot done: SQ-POT-02 FAIL on the fixed probe (real run now): framedblocks:framed_flower_pot takes
  flowering azalea leaves, moss block, pumpkin, melon, hamimelon (vanilla pot refuses them);
  supplementaries:hanging_flower_pot refuses saplings and grass the vanilla pot takes. SQ-POT-01 LOOK (text).
- 02:35 to 02:50 judges 1 to 3 (subagents) judged s00-s11, s12-s21, s22-s30 with `qrun.py judge` only.

## 8. Final verdicts

Counts (r1120lr + r1120pot): PASS 72, BATCH-FAULT 60, FAIL 3, LOOK 7 open, HARNESS 1 (SQ-IPO-01). SQ-POT-02 appears
twice: BATCH-FAULT on the broken probe (r1120lr), FAIL on the rerun (r1120pot).

Real FAILs (TODO rows):
- SQ-EDL-04: dragon kills drop no End's Delight meat, legs or teeth (passed r1102b). TODO `creative-delight-members #2`.
- SQ-IPO-02: iron fence on an Immersive Posts post base no longer grows the post (grew on 2026-10-27). TODO
  `performance-and-server #2`.
- SQ-POT-02: framed flower pot and Supplementaries hanging pot accept different plants than the vanilla pot, against
  flower_pots.md. TODO `decor-and-framed-blocks #1`.

Possible changelog gap (judge-2): 1.2.00 says a lemon cuts into 8 slices; it now gives 5 plus seeds
(plan_food_batch_1119 A6); no unreleased bullet seen.

Open LOOKs (no text evidence): SQ-NDM-05, SQ-STK-06 (defers to SQ-VTD8-03), SQ-CM-33 and SQ-MBX-05 (client),
SQ-MBX-06, SQ-CDFW-07, SQ-POT-01 (rerun).

### 8.1 BATCH-FAULT rewrite list (TODO `performance-and-server #3`)

- Unloaded lanes, add `forceload` first: LGM-03/05/07/08 (175 495 215 510), ROLL-05..08 (145 495 175 510), SSP-01
  (x 125 z 500), BBQ-05..07 (z 500 lane), JSL-02 (x 130-134 z 500), POT-04v (288 496 296 508), CDJ4-08b.
- Ids moved to `creativedelight:` / `creativecommon:`: SSP-02/03 (sliced_sweet_potato, food_type tag), VDF-06
  (food_family/preserve/pickle), VDF-09 (canned_*), BBQ-03 (rain_douses, food_type/*, raw_skewers 18), CDFW-02/03
  (tagdiff), IMS-03 (merge_core_probe.js:140 potato_slices id).
- Stale counts or intended changes: STK-01 (1398 stack sizes), CDFW-01 (32 types, loosen can count), BBQ-02
  (grilling 18, no combine, stonecutting ids bbq_oak_basin/tray_from_logs), VDF-04 (quiche oven only, baking id),
  VDF-05 (anchor on /turnip_mutton_skewer), LEM-02 (lemon_slice_from_whole, 5 slices), CDB-10 (potato_noodle*2),
  CDB-11 (bayberry_soup), CDFW-06 (hostiledelight ids without underscores), SALT-05/06/08 (melt cases moved to SQ-MELT,
  placed_rock_salt_block, creativedelight-common.toml, add weather rain), POT-03 (F1 gone: expect 0), POT-04 (floralench
  pot drops the filled pot item, flower_pots.md 1), VTD7-02 (blockrunner:surface/soft), DNR-O-02 (effects 4:00).
- Regex anchors: VDF-03/07, JSL-01, MG-21 (`n=... shown`), VDF-07/09 (narrow the done=0 reject), IPO-03 (reject only
  `immersiveposts:\w*post`), IMS-01 (reload doubles lines).
- Fixtures and boots: MBX-02..04 (spawn bot qa, cooldownTicks 200), MBX-07 (own boot vaultMode=false), BM-01 (Worldgen
  members line), BM-02 and CDJ4-08b (spawn-animals=true), CSI-01/02 (curios list syntax, second boot), DNR-E-03/E-04/O-05
  (own boot with the donor jar moved away), ROLL-03 (pbot hunger before eating), EDL-02 (summon on ground, NoAI), SV-01
  (wait after boot, selector), POT-05 (rerun after POT-02).
- Client only: AC-04, WE-02 (the `//` half).

### 8.2 Verdict table

| ID | Result | Evidence |
|---|---|---|
| SQ-CDB-01 | PASS | no script error for the six files; [itemMerges] pack 327, creativedelight 47 (44 on 2026-11-18 runs): count rose |
| SQ-CDB-02 | PASS | sweeteners n=4 (sugar, powdered sugar, honey bottle, honeycomb; glycerol out of #forge:sugar by design), drink n=3 sugar/powdered/honey bottle, brewing n=4, plain n=2 sugar+powdered (tags.js: non-food uses take both); hot cocoa and pumpkin pie slots name sugar/powdered_sugar/honey_bottle |
| SQ-CDB-03 | PASS | squid_to_calamari: calamari*1@1.0 + *1@0.5, ink_sac*1@1.0 + *2@0.5; glow variant same with glowing_calamari / glow_ink_sac; baked: cooked_calamari 1 + 50 % |
| SQ-CDB-04 | PASS | 4 kills (sword, flint knife): drops=miners_delight:squid:1 / miners_delight:glow_squid:1 each, nothing else |
| SQ-CDB-05 | PASS | 7 checks passed |
| SQ-CDB-06 | PASS | 3 checks passed |
| SQ-CDB-07 | PASS | PASS jars splash_milk:milk_bottle goes in: count 1, fluid moonlight:milk |
| SQ-CDB-08 | PASS | creativedelight:pickling/pickled_sea_pickle vintagedelight:fermenting: 5 sea_pickle + salt_dust -> pickled_sea_pickle*5 |
| SQ-CDB-09 | PASS | both pots cooked: slot 8 pickle_soup and braised_sea_pickle expects matched after tickwarp 500 |
| SQ-CDB-10 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, regex wants result \S*gnocchi but the Gnocchi item id is veggiesdelight:potato_noodle; creativedelight:farmersdelightcutting/gnocchi_from_sweet_potato_dough gives potato_noodle*2 from sweet_potato_dough (real PASS); no line matches /verb=rdetail id=\S*gnocchi\S* serializer=farmersdelight:cutting result=\S*gnocchi\*2\b ingredients=veggiesdelight:sweet_potato_dough/; PASS: creamy_gnocchi (farmersdelight:cooking): milk bucket/bottle/+3, potato_noodle x2, onion/garlic/garlic_clove/+1; gnocchi = veggiesdelight:potato_noodle |
| SQ-CDB-11 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, regex bayberry_juice: the rename is name-only, ids stay bayberry_soup (fruits_delight.md Bayberry Juice section); no line matches /verb=reg kind=item regex=bayberry_juice n=[1-9]/; PASS: cacciatore_cooked_manual_only grid slot: minecraft:cooked_chicken/farmersdelight:cooked_chicken_cuts (two cuts listed) |
| SQ-CDB-12 | PASS | 2 makes recipes (canned_pickled_pickles, _salt); CD vintagedelight/can_types.js sources pickle, pickled_sea_pickle |
| SQ-CDB-13 | PASS | 2 checks passed |
| SQ-CDB-15 | PASS | cake_base_flour_manual_only: 3 flour/cornmeal, 2 egg-tag slots, 2 sweetener-tag slots (sugar/powdered/honey bottle/+1, sweetener rework); pie_crust_flour_manual_only takes flour/cornmeal; cacciatore grid slot cooked_chicken/cooked_chicken_cuts |
| SQ-CDB-16 | PASS | oceanic_delight:fish_egg_roll egg slot oceanic_delight:salmon_eggs/oceanic_delight:ancient_fish_eggs only |
| SQ-CDB-17 | PASS | 2 checks passed |
| SQ-EDL-01 | PASS | level-1 librarian Offers: bookshelf, enchanted_book Thorns II; Offers did not reset at the level-5 merge; no nock_enough_arrows in Offers or the 43-profession trade dump |
| SQ-EDL-02 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, MCA swapped the summoned villager (profession none) and it fell from y 101 to death; row needs NoAI, a ground-level y and a Lightman's cashier read that does not use villager Offers; FAIL: no cashier data: the summoned villager became mca:male_villager profession minecraft:none (Isak) and died of fall damage (summoned at y 101 over superflat, no NoAI); no Offers to read |
| SQ-EDL-03 | PASS | count voidpepper_block n=3 after 30 random ticks; EN03 origin fruited; breaks: pepper=true drops voidpepper:3, plain sprout voidpepper:1 |
| SQ-EDL-04 | FAIL | no line matches /\[cbetest\] PASS dragon dragon bare: 5-7 meat, 2 legs, 1-2 teeth per kill/; count /\[cbetest\] PASS dragon dragon (knife/machete): \+2 meat, \+1 tooth per kill/ = 0, wanted == 2 |
| SQ-EDL-06 | PASS | 2 checks passed |
| SQ-SALT-05 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, block melting moved to Creative Common's melting system (salt.md 2026-11-14; melting.md 2: since 2026-11-17 a source tick only schedules the target's melt tick after meltTickInterval), so 400 direct salt randomTicks cannot melt; rain cases need rain (SALT-06); raw rock salt now swaps to hearthandharvest:placed_rock_salt_block (salt.md placed twin). Melting is covered by SQ-MELT rows; no line matches /\[salttest\] PASS vintagedelight:salt_bricks melts minecraft:snow/; no line matches /\[salttest\] PASS vintagedelight:salt_bricks melts minecraft:ice/; no line matches /\[salttest\] PASS vintagedelight:salt_brick_stairs melts minecraft:snow/; no line matches /\[salttest\] PASS vintagedelight:salt_brick_stairs melts minecraft:ice/; no line matches /\[salttest\] PASS vintagedelight:salt_lamp_default melts minecraft:snow/; no line matches /\[salttest\] PASS vintagedelight:salt_lamp_default melts minecraft:ice/; no line matches /\[salttest\] PASS vintagedelight:salt melts minecraft:snow/; no line matches /\[salttest\] PASS vintagedelight:salt melts minecraft:ice/; no line matches /\[salttest\] PASS salt:raw_rock_salt_block melts minecraft:snow/; no line matches /\[salttest\] PASS salt:raw_rock_salt_block melts minecraft:ice/; no line matches /\[salttest\] PASS salt:salt_block melts minecraft:snow/; no line matches /\[salttest\] PASS salt:salt_block melts minecraft:ice/; /\[salttest\] FAIL .* melts minecraft:/ matched: [20Nov2026 00:33:46.511] [Server thread/INFO] [KubeJS Server/]: zz_salt_probe.js#20: [salttest] FAIL vintagedelight:salt_bricks melts minecraft:snow: still minecraft:snow after 400 |
| SQ-SALT-06 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, rain, water, torch, evaporator and Salt Block cases PASS; fails are the stale melt cases (melting system scheduled ticks since 2026-11-17) and raw rock salt read back as hearthandharvest:placed_rock_salt_block (intended placed twin, salt.md); no line matches /\[salttest\] PASS raw rock salt beside water stays/; no line matches /\[salttest\] PASS salt:raw_rock_salt_block in rain, open sky, stays/ |
| SQ-SALT-07 | PASS | 2 checks passed |
| SQ-SALT-08 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, config/salt-common.toml moved into config/creativedelight-common.toml (SaltDissolvingInRain = false there; salt.md section 6 S10); row sets no rain, so rain cases report no rain; melt cases stale as SALT-05; shell exit 2: grep: config/salt-common.toml: No such file or directory; no line matches /\[salttest\] PASS salt:salt_block in rain, open sky, stays/; no line matches /\[salttest\] PASS salt:rock_salt_ore in rain, open sky, stays/; no line matches /\[salttest\] PASS vintagedelight:salt_bricks in rain, open sky, stays/; no line matches /\[salttest\] PASS vintagedelight:salt in rain, open sky, stays/ |
| SQ-SV-01 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, the tp selector found no armor stand (No entity was found) so nothing was measured; the Can't keep up line is the post-boot backlog 8 s after Done, before the tp; no r.585.585 region. Row needs a settle wait after boot and a selector that finds the End stand (e.g. execute in the_end as @e[...] or a UUID); /Can't keep up/ticks behind/ matched: [20Nov2026 00:36:16.168] [Server thread/WARN] [net.minecraft.server.MinecraftServer/]: Can't keep up! Is the server overloaded? Running 3644ms or 72 ticks behind; FAIL: tp never ran: 'No entity was found' for @e[tag=sv01] after the End summon; the only Can't keep up (3644 ms) is at 00:36:16, 8 s after Done, before the tp (boot backlog) |
| SQ-ATC-01 | PASS | ArmorTrimsLargeItemListsMixin mixed into LargeItemLists (debug.log), no warning or error line for it; ATC-03 to -06 PASS |
| SQ-ATC-02 | PASS | 5 checks passed |
| SQ-ATC-03 | PASS | 3 checks passed |
| SQ-ATC-04 | PASS | after reload ATC-03..06 PASS again, 50 entries both runs, ATC-07 PASS; Reloading! 00:38:53.4 -> pocket computer line 00:39:04.4 = 11 s (28 s limit) |
| SQ-MG-16 | PASS | makes lines: only creativecompat:createsplashing/cardboard_box_to_cardboard_box_by_splashing and .../cardboard_box_to_weathered_cardboard_box_by_splashing; tags mineable/axe, blockrunner:surface/soft, creativecompat:sinking |
| SQ-GFB-01 | PASS | 2 checks passed |
| SQ-HT-01 | PASS | 4 checks passed |
| SQ-IMS-01 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, the two shell == 1 counts failed because the harness ran a /reload at 00:46:56, 3 s after Done (latest.log), so each [itemMerges] recipe line shows twice (once per recipe event, correct); count per load or read before any reload; shell exit 1: ; shell exit 1: ; PASS: 4 pack merges registered; pack 327 merges / 13 swapped, creativedelight 47 / 54 (later merges raised them); once per recipe event; no already merged/ran/chains line |
| SQ-IMS-02 | PASS | Posted recipe events in 4.976 s at boot, 3.1 s / 2.9 s on the reloads (12.2 s on 2026-10-27) |
| SQ-IMS-03 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, the one mgcore FAIL 'Rustic potato cutting kept' is stale: kubejs/data/creativedelight/kubejs/rusticdelight/recipes.js removes rusticdelight:cutting/potato_slices on purpose and adds a 3-slice creativedelight recipe (TODO dishes/ingredients #26); probe tools/item_merges_probe/merge_core_probe.js:140 needs the new id; count /\[mgcore\] done \d+ pass, 0 fail/ = 0, wanted >= 2; /\[(mgcore/fdtest)\] FAIL/ matched: [20Nov2026 00:47:17.251] [Server thread/INFO] [KubeJS Server/]: zz_merge_core_probe.js#29: [mgcore] FAIL Rustic potato cutting kept:; PASS: [itemMerges] pack and creativedelight lines once per recipe event (boot, harness reload, row reload); fdtest coffee 19/0 twice; mgcore 32/1 twice |
| SQ-IMS-04 | PASS | all 13 makes n=0, all 9 uses alone=0 shared=0; jeiRemove tail names only Vintage Improvements sheets/rods/wires; Gambler's Tea under creativedelight:farmersrespitebrewing |
| SQ-IPO-01 | HARNESS | shell exit 1: states 749157 cut 21802 |
| SQ-IPO-02 | FAIL | no line matches /verb=count block=immersiveposts:ironpost n=3\b/; count ironpost n=0 after 6 iron-fence uses (result SUCCESS, fence not consumed, postbase unchanged); 2705 -58 air after the hammer; 2706 -57 air (the -57 use placed a plain fence_iron) |
| SQ-IPO-03 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, reject state="?immersiveposts: also matches immersiveposts:fence_silver, the plain fence block the pass text allows; only a *post state should be rejected (real PASS); /verb=state pos=2712,-59,2704 state="?immersiveposts:/ matched: [20Nov2026 00:52:19.210] [Server thread/INFO] [com.SaintAlfonzo.creativedevtools.test.TestCtx/]: [cdevtest] verb=state pos=2712,-59,2704 state="immersiveposts:fence_silver[east=false,north=false,south; PASS: silver fence placed as plain immersiveposts:fence_silver at 2712 -59 2704 (plain fence block); no exception |
| SQ-POT-01 | LOOK | open look 1: quark entries 35 (shell line 1); potLike lists flower_box, hanging_flower_pot, planter(s), planter boxes, large_pot, vinery pot / box, silt pots, Handcrafted pots, framed pot (shell line 2) |
| SQ-POT-02 | FAIL | no line matches /\[pottest\] PASS PP-02/; count /\[pottest\] FAIL PP-0[12]:/ = 2, wanted == 0; open look 1: PP-SUM per-holder counts close to flower_pots.md section 1 (probe copied fresh from tools/flower_pot_test/pot_probe.js) |
| SQ-POT-03 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, steps still expect the F1 planter box ClassCastException (count FAIL PP-03 == 1 and two expects) while the pass text says F1 struck; the exception is gone, all 12 holders PASS PP-03 (real PASS); count /\[pottest\] FAIL PP-03:/ = 0, wanted == 1; no line matches /\[pottest\] FAIL PP-03: another_furniture:oak_planter_box .*ClassCastException/; no line matches /\[pottest\] FAIL PP-03: another_furniture:oak_planter_box .*fungidelight:cleaned_cap/; PASS: PASS PP-03 for all 12 holders incl. another_furniture:oak_planter_box, 0 FAIL PP-03, no hanging_roots exception; PP-DONE reached (server kept running) |
| SQ-POT-04 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, the only FAIL PP-04 is floralench:large_pot dropping the filled pot item (e.g. large_double_allium_pot), which flower_pots.md section 1 table records as its drop (plant kept inside the item; large pots hidden since 2026-10-31, replacement planned); probe should count the filled pot item as returning the plant or skip floralench; count /\[pottest\] FAIL PP-04:/ = 1, wanted == 0 |
| SQ-POT-04v | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, no forceload for x 290 z 500: both setblocks 'That position is not loaded', the bot spawned at spawn, no break ran; add forceload add 288 496 296 508 first; timeout 30 s waiting for /\[cdevtest\] verb=break done=/; count /\[cdevtest\] verb=break .*broken=true/ = 0, wanted >= 2; FAIL: no state or break lines: setblock 290 -60 500 answered 'That position is not loaded' twice; the bot spawned at 6.5 -60 5.5 |
| SQ-POT-05 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, audit inputs come from this run's pot probe, broken per SQ-POT-02 (BATCH-FAULT, probe fixed after the drive); Supp. hanging pot 0 accepted is the known probe-face artifact and the vpot/vbox predictions predate the Vinery pot changes; rerun with SQ-POT-02; FAIL: audit ran (555 plants, 755 probe mismatches, 0 exceptions); mismatches are 124 hanging (Supp. hanging pot 0 accepted, probe face), 345 vpot / 102 vbox predicted no probe yes, 105 planter: not only explainable rows |
| SQ-WE-01 | PASS | no WorldEdit mixin error; on the dedicated server FTB Quests loads after Done (01:24:07.036) and the next line follows in 9 ms, so the 44 s silent stretch is gone; mod load FTB line -> Done 47 s is not the perf.md 11.1 measure |
| SQ-WE-02 | BATCH-FAULT | reclassified from FAIL: judge-1, 2026-11-20, WorldEdit ignores commands from the perf bot session (no reply, nothing placed); the set/replace/undo path passed through the API in SQ-WE-03 (wetest SQ-WE-02 set/replace/undo PASS); move the // half to a client row; no line matches /verb=state pos=3,-55,3 state="minecraft:oak_stairs\[facing=east,half=top/; no line matches /verb=count block=minecraft:oak_stairs n=2816\b/; no line matches /verb=count block=minecraft:stone n=2816\b/; PASS: refusal recorded: the bot's /pos1, /pos2, /set, /replace produced no reply and no blocks (oak_stairs 0, stone 0, 3 -55 3 air); // commands are a client check |
| SQ-WE-03 | PASS | wetest: copy matches the source, undo leaves air (0 of 64), redo restores the copy; 10 pass 0 fail |
| SQ-WE-04 | PASS | 4 checks passed |
| SQ-CM-33 | LOOK | open look 1: client half (JEI item list in wood order) stays open: CQ row / client run |
| SQ-CM-34 | PASS | judge-2: shell.txt displayName="Storage Delight (Creative Delight)"; mod list screen is client (CQ) |
| SQ-CM-36 | PASS | judge-2: [fgdtest] PASS recipes oven truffle pie: creativedelight:baking/truffle_pie; name/sprite are client (CQ) |
| SQ-AC-04 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, @group client row (do/shot/@s) run on a server-only boot without the capture driver; no player so armor never applied; do needs the capture driver (qrun client on run_captures.py serve); no line matches /is 40/; open look 1: the HUD armor bar shows 40 (one extra row past 20; Detail Armor Bar style) |
| SQ-MBX-01 | PASS | 2 checks passed |
| SQ-MBX-02 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, missing fixture: no bot player qa in s14 (tp qa -> No entity was found), box never triggered (phase 0b, wave []); no line matches /creativeengineering:phase": 1b/; open look 1: the wave list holds 5 to 8 UUIDs |
| SQ-MBX-03 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, follows SQ-MBX-02: no bot qa, no wave, so no reward or phase 2b; boot also had cooldownTicks 72000 not 200; no line matches /creativeengineering:phase": 2b/; no line matches /Test passed, count: 1/; open look 1: until = game time + 72000 normally; this boot has cooldownTicks 200, so until = game time + 200 |
| SQ-MBX-04 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, missing fixture: no bot qa and boot had cooldownTicks 72000 (section needs 200); no line matches /creativeengineering:phase": 1b/ |
| SQ-MBX-05 | LOOK | open look 1: hardness 25 per the changelog (queue row says 50): check with WTHIT in the client block (CQ-MBX-08) |
| SQ-MBX-06 | LOOK | open look 1: if the drill did not turn (motor facing wrong), set facing=east on the motor and rerun the row |
| SQ-MBX-07 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, missing fixture: needs its own boot with vaultMode=false (s14 had vaultMode=true) and bot qa (absent); no line matches /Test failed/ |
| SQ-JRFTL-01 (r1101 PASS) | PASS | judge-2: verb=uses id=creativeengineering:smoking/prepared_flesh_to_leather_by_smoking serializer=minecraft:smoking (id names leather output); name/texture are client (CQ) |
| SQ-BBQ-01 | PASS | 4 checks passed |
| SQ-BBQ-02 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, stale counts/ids: combine type dropped by plan_meals_rolls_bbq C8 (combos now shaped/assembly), grilling 18 = 10 + 8 pack raw skewers (plan answers), stonecutting ids now bbq_<wood>_basin/tray_from_logs (barbeques_delight.md 3.11); count /verb=rdetail id=creativedelight:grilling/\S+ serializer=/ = 18, wanted == 10; count /verb=rdetail id=creativedelight:combine/\S+ serializer=/ = 0, wanted == 3; no line matches /verb=rdetail id=creativedelight:stonecutting/bbq_basin_from_logs serializer=/; no line matches /verb=rdetail id=creativedelight:stonecutting/bbq_tray_from_logs serializer=/; PASS: judge-2: raw_beef_skewer fields ingredient:2xminecraft:beef/farmersdelight:minced_beef, side:2xfarmersdelight:cabbage/cabbage_leaf (the forge:raw_beef / crops/cabbage contents) |
| SQ-BBQ-03 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, stale ids: rain_douses is creativecommon:campfire/rain_douses (grill tagsof shows it), food_type tags are creativedelight:food_type/* (instance dump has wrap/sandwich handheld, bibimbap meal, burnt hazard); raw_skewers 18 = 10 + 8 pack raws; no line matches /verb=tag kind=item tag=barbequesdelight:raw_skewers n=10\b/; no line matches /verb=tag kind=block tag=creativeengineering:campfire/rain_douses .*ids=\S*minecraft:campfire\b/; no line matches /verb=tag kind=block tag=creativeengineering:campfire/rain_douses .*ids=\S*barbequesdelight:grill\b/; no line matches /verb=tag kind=item tag=creativeengineering:food_type/handheld .*ids=\S*barbequesdelight:kebab_wrap\b/; no line matches /verb=tag kind=item tag=creativeengineering:food_type/handheld .*ids=\S*barbequesdelight:kebab_sandwich\b/; no line matches /verb=tag kind=item tag=creativeengineering:food_type/meal .*ids=\S*barbequesdelight:bibimbap\b/; no line matches /verb=tag kind=item tag=creativeengineering:food_type/hazard .*ids=\S*barbequesdelight:burnt_skewer\b/; open look 1: the 10 grilled skewers in handheld; the grill / basin / tray mineable tags as upstream (block_tags in the tagsof lines) |
| SQ-BBQ-04 | PASS | judge-2: run compost.json (s16/srv): 10 barbequesdelight:raw_* all 0.25, 10 grilled_* all 0.5, matching 3.7 |
| SQ-BBQ-05 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, fixture: the 4 setblocks at x212-216 z500 ran before the chunk loaded ('That position is not loaded'), no campfire/stone/coal; grill placed on grass has_campfire=false; flint use FAIL follows; no line matches /BBQ05 grill on campfire 212/; no line matches /BBQ05 grill on campfire 216/; no line matches /BBQ05 lit 212/; no line matches /BBQ05 lit 216/; no line matches /BBQ05 still lit on coal/; no line matches /verb=state pos=212,-60,500 state="?barbequesdelight:grill\[.*lit=true.* light_block=15\b/; no line matches /verb=break drops=barbequesdelight:grill:1,minecraft:campfire:1 stacks=2\b/; open look 1: if the grill landed one block up (y -59) instead of replacing the campfire, the execute lines are silent: check the cdevtest state line |
| SQ-BBQ-06 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, fixture: campfire setblock at 220 -60 500 'That position is not loaded'; grill unlit, skewer never placed (Entries air); no line matches /has the following block data: .*barbequesdelight:burnt_skewer/; open look 1: the two last reads (20 ticks apart) are the same burnt entry; no block-update spam lines |
| SQ-BBQ-07 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, fixture: setblock 'That position is not loaded' before the bot loaded z500; grill never seated/lit, Entries stay air; FAIL: judge-2: not judgeable: grill never got a campfire (setblock 'That position is not loaded'), grill Entries stay minecraft:air after the skewer use |
| SQ-BBQ-09 | PASS | judge-2: beef steps beef,cabbage,beef,cabbage (tag contents); nether start minecraft:blaze_rod; crispy chorus, chorus, endersdelight:mite_crust |
| SQ-BM-01 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, stale: Baby Mobs moved to the Creative Worldgen bundle (members/babymobs), so the [creativedelight] members line no longer lists it; babyMobs-1.1.0.jar loads (BuildStamps babymobs 1.1.0); boot log: no line matches /\[creativedelight\] members: \[.*\bbabymobs\b/; PASS: judge-2: debug.log applies only LivingEntityBabyMixin, MobBabyMixin, DolphinFeedMixin (no mixin/client); no babymobs error; kjs handler errors 0 |
| SQ-BM-02 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, fixture: s17 server.properties spawn-animals=false; vanilla ServerLevel discards every Animal on its first tick (all 7 animals gone, MCA villager and zombie kept, same as SQ-CDJ4-08b); row/section must set spawn-animals=true; count /has the following entity data: -2\d{4}$/ = 1, wanted == 8; /No entity was found/ matched: [20Nov2026 01:41:55.520] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: No entity was found; /BM02 cat gone/ matched: [20Nov2026 01:42:42.824] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Not Secure] [Server] BM02 cat gone at 5 s; FAIL: judge-2: all 7 summoned animals (cow pig sheep chicken wolf cat horse) gone within 1 s, nothing in their place (entities read at 1 s: only mca:male_villager); BM02 alive names only Valeri (MCA villager, Age -23458) and the zombie (IsBaby 1b); no removal line in latest/debug log |
| SQ-CDFW-01 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, stale counts: food_catalog.md lists 32 food_type tags (row wants 31); 344 cans after post-2026-11-01 can additions (row wants 31x); boot log: no line matches /\[food types\] \d+ foods in 31 types/; boot log: no line matches /\[can servings\] 31\d cans/; PASS: judge-2: [can servings] 344 cans (3x1..50x10), [cans] 307 generated, [food types] 1647 foods in 32 types; growth past 313 is later can work, no warning lines |
| SQ-CDFW-02 | PASS | judge-2: row tagdiff used the old creativeengineering: namespace (0 tags); rerun on s18 dump with creativedelight: food_type 35 tags/3657, canned_source 364, cans/servings 9 tags/686 = instance dump |
| SQ-CDFW-03 | PASS | judge-2: rerun on s18 dump: creativedelight:placeable* 5 tags, 890 entries here = 890 in instance dump (row's creativeengineering: namespace gave 0) |
| SQ-CDFW-04 | PASS | judge-2: mortar/create_wheat, create_sugar_cane, farmersdelight_rice_panicle + farmersdelight_wild_rice, create_confectionery cocoa_powder, veggiesdelight_wild_garlics -> garlic_clove; [mortar] 6 of 6 catalog crafts |
| SQ-CDFW-05 | PASS | 6 checks passed |
| SQ-CDFW-06 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, row ids wrong: Hostile Delight items are grilledghast, cookedrottenham, friedturtleegg (no underscores); the 6 other items show their three creativedelight lines; FAIL: judge-2: arthropod, duck fillet, perch, pike, acorn each have creativedelight smelting/smoking/campfirecooking; fried egg campfire+smoking only (food_families.md: no furnace, intended); grilled_ghast / cooked_rotten_ham / fried_turtle_egg n=0: wrong ids, Hostile Delight's are hostiledelight:grilledghast / cookedrottenham / friedturtleegg |
| SQ-CDFW-07 | LOOK | open look 1: yields (catfish 6 slices + bone meal, salmon / cod 6 + bone meal, duck 2 fillets) via `cdevtest rdetail farmersdelightcutting/` if needed; the board run is not repeated here |
| SQ-CDFW-08 | PASS | judge-2: shaped/golden_apple ring slots minecraft:gold_ingot/mca:rose_gold_ingot; golden_carrot ring gold_nugget/rose_gold_nugget |
| SQ-CDFW-09 | PASS | judge-2: recipes _cooked_manual_only n=120; outputs per mod: farmersdelight 17, alexsdelight 4, fruitsdelight 1, abnormals_delight 1, delightful 1 |
| SQ-CDJ4-08b | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, fixture: water fill at 130 -61 500 'That position is not loaded' and s19 server.properties spawn-animals=false (row needs true), so vanilla discards the squid; no line matches /has the following entity data: -2\d{4}/; /No entity was found/ matched: [20Nov2026 01:48:03.140] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: No entity was found |
| SQ-JSL-01 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, regex: count 'n=[1-9]\d*$' misses the 4 rdetail text lines because they end in ' shown=1' (all 4 have n=1); jam_from_slices n=0 passed; count /verb=rdetail text=\S+ n=[1-9]\d*$/ = 0, wanted == 4; PASS: judge-2: orange_jelly / lemon_jelly / flowing_*_jam slots fruitsdelight:orange/lemon / *_slice, separate lemon_slice slot kept; container vintagedelight:empty_mason_jar ([zsb] potrecipe lines in SQ-JSL-02.log) |
| SQ-JSL-02 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, fixture: chunk at x130-134 z500 not loaded (no forceload in s19), campfires/pots never placed; no line matches /130, -59, 500 has the following block data: .*Slot: 8b, id: "fruitsdelight:orange_jelly"/; no line matches /134, -59, 500 has the following block data: .*Slot: 8b, id: "fruitsdelight:lemon_jelly"/; /(Unknown (item/block/entity)/Unknown or incomplete command/Incorrect argument for command/That position is not loaded/\[(zsb/zlbs)\] (error/unknown)/verb=tickwarp .*done=0)/ matched: [20Nov2026 01:51:06.389] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: That position is not loaded; FAIL: judge-2: not judgeable: every setblock/data merge at x130/134 z500 returned 'That position is not loaded'; no pot was placed |
| SQ-LEM-01 | PASS | judge-2: 10 picks: verb=items fruitsdelight:lemon:23 on the ground + 'Found 4' on bot lem = 27 (20 to 50), 10x 'reset to age 2' |
| SQ-LEM-02 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, stale: plan_food_batch_1119 A6 (2026-11-19) remade lemon cuts as creativedelight:farmersdelightcutting/lemon_slice_from_whole, 5 slices + seeds (row wants fruitsdelight ids, 8); no line matches /id=fruitsdelight:lemon_cutting serializer=\S+ result=\S*lemon_slice\*8/; no line matches /id=fruitsdelight:cutting/lemon_slice serializer=\S+ result=\S*lemon_slice\*8/; PASS: judge-2: creativedelight:farmersdelightcutting/lemon_slice_from_whole results lemon_slice*5@1.0, lemon_seeds*1@1.0 (saw copy same) |
| SQ-CSI-01 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, wrong command: 'curios list csi' -> Incorrect argument for command (Curios 1.19.2 /curios list takes no player); slots never printed; no line matches /\bhands\b/; no line matches /\bface\b/; no line matches /\bfeet\b/; open look 1: rows/SQ-CSI-01.log: the three new slots listed alongside head, back and the rest |
| SQ-CSI-02 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, wrong command: 'curios list csi' -> Incorrect argument; also ran on the same boot as SQ-CSI-01, not a later boot of its world; no line matches /\bhands\b/; no line matches /\bface\b/; no line matches /\bfeet\b/; open look 1: the world had SQ-CSI-01's bot csi saved on an earlier boot (else this is a first join and proves nothing): one slot each of hands, face, feet |
| SQ-DNR-E-03 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, missing fixture: needs its own boot with the donor jar moved away/replaced; s21 booted with the donor in place (enders_upstream_data / oceanic_upstream_data loaded), so no missing/wrong-file message; boot log: no line matches /(?i)Ender's Delight Additions.*enders_upstream.*missing/; open look 1: own boot with enders_upstream/endersdelight-1.1.4.jar moved away: startup stops with the missing message; restore the jar after |
| SQ-DNR-E-04 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, missing fixture: needs its own boot with the donor jar moved away/replaced; s21 booted with the donor in place (enders_upstream_data / oceanic_upstream_data loaded), so no missing/wrong-file message; boot log: no line matches /(?i)wrong file.*d1541d70/; open look 1: own boot with another jar under enders_upstream/endersdelight-1.1.4.jar: startup stops with the sha1 message; restore the jar after |
| SQ-DNR-O-02 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, stale + harness: pack Comfort/Nourishment rule (comfort_nourishment.md 10; food_catalog shrimp stew nourishment 240s) gives 4:00 (reads 4711/4712), not upstream 5:00; bowl/bottle were returned (useitem result=minecraft:bowl x2, glass_bottle, nautilus_shell) but each pbot hold overwrote slot 0, so only the shell is left in inv; no line matches /verb=player name=dnr2 effects=\S*farmersdelight:comfort:0:5\d\d\d\b/; no line matches /verb=player name=dnr2 effects=\S*farmersdelight:nourishment:0:5\d\d\d\b/; no line matches /verb=pbot.inv bot=dnr2 slot=\d+ item=minecraft:bowl\*/; no line matches /verb=pbot.inv bot=dnr2 slot=\d+ item=minecraft:glass_bottle\*/; open look 1: the five player lines: Comfort 5:00, Nourishment 5:00, Instant Breath (air=300) after the right food; the sea grape count above 1 after the bone meal; the empty paella pan pick-up (pan + skillet), the "You need a Bowl" message, the blue tooltip lines and JEI are client (CQ) |
| SQ-DNR-O-05 | BATCH-FAULT | reclassified from FAIL: judge-2, 2026-11-20, missing fixture: needs its own boot with the donor jar moved away/replaced; s21 booted with the donor in place (enders_upstream_data / oceanic_upstream_data loaded), so no missing/wrong-file message; boot log: no line matches /(?i)original Oceanic Delight jar.*oceanic_upstream.*missing/oceanic_upstream.*missing.*modrinth/; open look 1: own boot with oceanic_upstream/oceanic_delight-1.0.1-forge-1.19.2.jar moved away: startup stops with the missing message and the Modrinth URL; a second boot with another jar under that name stops with "wrong file ... expected 91fd8842..." (bootexpect wrong file .*91fd8842 on that boot); restore the jar after |
| SQ-LGM-01 | PASS | 4 checks passed |
| SQ-LGM-02 | PASS | items_by_mod largemeals: 18 ids, the 3 feast blocks (omurice_block, mushroom_pot_pie_block, roasted_mutton_rack_block) among them |
| SQ-LGM-03 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: x 180-210 z 500 not forceloaded ('That position is not loaded' / verb=kill error=unloaded); row needs forceload add 175 495 215 510 first; no line matches /Slot: 8b?, id: "largemeals:potato_soup"/; no line matches /Slot: 6b?, id: "largemeals:potato_soup"/ |
| SQ-LGM-04 | PASS | verb=item sweet_berry_custard max_stack=32 (drink-like bottle, 16 or 32 allowed) |
| SQ-LGM-05 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: x 180-210 z 500 not forceloaded ('That position is not loaded' / verb=kill error=unloaded); row needs forceload add 175 495 215 510 first; count /verb=kill type=minecraft:sheep n=20 killed=20/ = 0, wanted == 3; no line matches /verb=kill drops=\S*largemeals:raw_mutton_rack:([5-9]/1[0-5])\b/; no line matches /verb=kill drops=\S*largemeals:cooked_mutton_rack:([5-9]/1[0-5])\b/; count /verb=kill drops=\S*mutton_rack:/ = 0, wanted == 2; /\[cdevtest\] verb=\S+ .*error=/ matched: [20Nov2026 01:59:10.401] [Server thread/INFO] [com.SaintAlfonzo.creativedevtools.test.TestCtx/]: [cdevtest] verb=kill error="unloaded 190,-60,500 (forceload add first)"; FAIL: not run: all three verb=kill lines error=unloaded 190,-60,500 (no forceload in the row) |
| SQ-LGM-06 | PASS | 5 checks passed |
| SQ-LGM-07 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: x 180-210 z 500 not forceloaded ('That position is not loaded' / verb=kill error=unloaded); row needs forceload add 175 495 215 510 first; no line matches /Found 4 matching item/; FAIL: bowl half not run: setblock at 200 -60 500 'That position is not loaded', 5x use PASS on air; break half: servings=4 full drops omurice_block:1, servings=2 drops bowl:1 (property name servings fits) |
| SQ-LGM-08 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: x 180-210 z 500 not forceloaded ('That position is not loaded' / verb=kill error=unloaded); row needs forceload add 175 495 215 510 first; count /Slot: [68]b?, id: "largemeals:roasted_mutton_rack_block"/ = 0, wanted == 2 |
| SQ-LGM-09 | PASS | verb=uses item=minecraft:dragon_egg alone=0 shared=0, no egg dish listed |
| SQ-LGM-10 | PASS | manual_only: soups pufferfish_broth, red_soup, tomato_egg_soup, potato_soup (4); pasta, chicken_curry, cod_deluxe, rice_pudding, hearty_lunch, sweet_berry_custard (6); 3 feast blocks; assemblies F.assemble(i.bowl, ...) in creativedelight largemeals/recipes.js |
| SQ-VTD7-01 | PASS | 2 checks passed |
| SQ-VTD7-02 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, row expects oat_bale and salt:salt_block in natural_smooth but its own @commands and blocktags.js put them in blockrunner:surface/soft (0.88x); row never dumps the soft tag; no line matches /verb=tag kind=block tag=blockrunner:surface/natural_smooth n=\d+ ids=.*vintagedelight:oat_bale/; no line matches /verb=tag kind=block tag=blockrunner:surface/natural_smooth n=\d+ ids=.*salt:salt_block/ |
| SQ-NDM-01 | PASS | repackedNethers-1.19-3.1-ce-19.2-01.jar nethersdelight 1.19-3.1-ce-19.2-01; mixin config nethersdelight.ce.mixins.json registered and prepared; display name left to the client row |
| SQ-NDM-02 | PASS | 7 checks passed |
| SQ-NDM-03 | PASS | 3 checks passed |
| SQ-NDM-04 | PASS | farmersdelight:cooking/stuffed_hoglin = 2 nether_salad + raw_stuffed_hoglin, cook 400; minecraft:netherite_machete_smithing base diamond_machete + netherite_ingot; pot slot 8 stuffed_hoglin |
| SQ-NDM-05 | LOOK | open look 1: the broken open-sky colony dropped its item (an item entity at 262 -59 500) |
| SQ-NDM-06 | PASS | verb=ages n=50 hist=0:9,1:11,2:16,3:14 after randomtick rounds=9: reaches age 3 |
| SQ-NDM-07 | PASS | rdetail nethersdelight:hoglin_mount slot 5 = minecraft:white_wool/immersiveengineering:hemp_fabric |
| SQ-PT-01 | PASS | [plantable] seed=50, crop=14, sapling=37, fungi_sapling=23, water=11, vine=24, vertical=5 (tags n: seed 48, sapling 36, vine 24); within 1-2 of plantable.md 2 |
| SQ-ROLL-01 | PASS | 5 checks passed |
| SQ-ROLL-02 | PASS | 8 checks passed |
| SQ-ROLL-03 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: effect hunger 1 255 left the bot at foodLevel 20, so the (not always-edible) roll could not be eaten; set food with pbot hunger rl3 19 (as ROLL-05 does) and expect foodLevel < 20 first; no line matches /has the following entity data: \[\{.*Duration/; no line matches /\[cdevtest\] verb=useair who=rl3 hand=off result=(CONSUME/SUCCESS)/; no line matches /has the following entity data: \{.*id: "roll_delight:chopsticks".*Damage: 1\b/; FAIL: not eaten: foodLevel 20 before and after (hunger 1 s did not drain the bot), useair hand=off result=FAIL, no ActiveEffects, roll still in slot -106 |
| SQ-ROLL-04 | PASS | cooked_rice still in slot -106, foodLevel 20, chopsticks Damage 0, no Damage above 0; bread off-hand use FAIL (bread was overwritten by the rice, not read back) |
| SQ-ROLL-05 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: x 150-170 z 500 not forceloaded ('That position is not loaded' / verb=break error=unloaded); row needs forceload add 145 495 175 510 first; no line matches /verb=items items=\S*minecraft:bowl:1/; FAIL: not run: setblock 155 -60 500 'That position is not loaded'; 9 clicks on air, SelectedItem air each time |
| SQ-ROLL-06 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: x 150-170 z 500 not forceloaded ('That position is not loaded' / verb=break error=unloaded); row needs forceload add 145 495 175 510 first; count /\[cdevtest\] verb=break who=fake broken=true before=\"?roll_delight:mythical_roll_medley/ = 0, wanted == 2; no line matches /\[cdevtest\] verb=break drops=\S*\bminecraft:bowl:1\b/; no line matches /\[cdevtest\] verb=break drops=roll_delight:mythical_roll_medley:1 stacks=1/; /\[cdevtest\] verb=\S+ done=0/ matched: [20Nov2026 02:12:57.615] [Server thread/INFO] [com.SaintAlfonzo.creativedevtools.test.TestCtx/]: [cdevtest] verb=break done=0; FAIL: not run: both verb=break error=unloaded 160,-60,500 |
| SQ-ROLL-07 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: x 150-170 z 500 not forceloaded ('That position is not loaded' / verb=break error=unloaded); row needs forceload add 145 495 175 510 first; count /Removed 1 item/ = 3, wanted == 4 |
| SQ-ROLL-08 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: x 150-170 z 500 not forceloaded ('That position is not loaded' / verb=break error=unloaded); row needs forceload add 145 495 175 510 first; count /\[cdevtest\] verb=break who=fake broken=true before=minecraft:frogspawn/ = 0, wanted == 2; no line matches /\[cdevtest\] verb=break drops=minecraft:frogspawn:1 stacks=1/; no line matches /\[cdevtest\] verb=break drops=- stacks=0/; /\[cdevtest\] verb=\S+ done=0/ matched: [20Nov2026 02:13:51.964] [Server thread/INFO] [com.SaintAlfonzo.creativedevtools.test.TestCtx/]: [cdevtest] verb=break done=0 |
| SQ-ROLL-09 | PASS | 2 checks passed |
| SQ-ENC-01 | PASS | level-1 librarian offers paper->emerald and enchanted_book (luck_of_the_sea 1); level-5 merge with Offers:{} did not regenerate (same 2 offers); run's villager_trades.json has no nock_enough_arrows ids |
| SQ-ENC-02 | PASS | summoned villager came up profession minecraft:none, no Offers in entity data; run's villager_trades.json cashier entry: 96 enchanted_book offer variants, no nock_enough_arrows ids |
| SQ-SSP-01 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, fixture: x 125 z 500 not forceloaded; also the clear step uses the old id creativeengineering:sliced_sweet_potato (now creativedelight:); FAIL: board half not run: setblock 125 -60 500 'That position is not loaded', both verb=items items=- stacks=0; recipes exist (potato_slices*3, creativedelight:sliced_sweet_potato*3, board and S&D) |
| SQ-SSP-02 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, stale namespace: item is creativedelight:sliced_sweet_potato and carries creativedelight:food_type/raw/veggie/sliced (ids-follow-owning-mod); all forge tags present; no line matches /verb=tagsof id=creativeengineering:sliced_sweet_potato .*item_tags=.*\bcreativeengineering:food_type/raw/veggie/sliced\b/ |
| SQ-SSP-03 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, stale id: item is creativedelight:sliced_sweet_potato (nutrition 1, modifier 0.3; compost.json chance 0.5); row queries creativeengineering:; shell exit 1: [dumpq] FAIL creativeengineering:sliced_sweet_potato compost 0.5 (got None); no line matches /verb=food item=creativeengineering:sliced_sweet_potato\*\d+ food=true nutrition=1 modifier=0.3/ |
| SQ-STK-01 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, stale count: boot says set 1398 stack sizes, no could-not-set, no item_dict warning; generation_scripts/stacks/stack_sizes.json regenerated 2026-11-06 and 2026-11-11 after the row's 1341; boot log: no line matches /\[stack_sizes\] set 1341 stack sizes/ |
| SQ-STK-02 | PASS | 2 checks passed |
| SQ-STK-03 | PASS | 2 checks passed |
| SQ-STK-04 | PASS | brew_flask max_stack=1, potion max_stack=1; instance stack_sizes.json stackSize 1 for both (unchanged) |
| SQ-STK-06 | LOOK | open look 1: the jar half (3 cucumbers + 2 zucchini + salt in the jar at 1000 -60 1009) is judged in SQ-VTD8-03's check row below |
| SQ-MG-21 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, stale count/format: fish_and_chips n=3 = creativedelight:farmersdelightcooking/fish_and_chips + its S&D mirror + the later salting recipe; verb=makes prints id= result=, not '->'; cod_n_chips n=0, uses 0, give/clear as expected; no line matches /verb=makes item=veggiesdelight:fish_and_chips n=[12]$/; no line matches /creativedelight:\S*cooking/fish_and_chips ->/ |
| SQ-VDF-01 | PASS | 2 checks passed |
| SQ-VDF-02 | PASS | Mashed Potatoes = moredelight:mashed_potatoes (pot potato/potato_slices x2 + milk), Mashed Sweet Potatoes = veggiesdelight:mashed_potatoes (pot sweet_potato/sliced_sweet_potato x2 + milk; grid 3 baked_sweet_potato + milk + bowl); bowl is the pot container |
| SQ-VDF-03 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, regex: verb=recipes prints 'n=0 shown=0', row anchors n=0$; content passes (no veggiesdelight:bread_from_ recipes); no line matches /verb=recipes text=veggiesdelight:bread_from_ n=0$/; PASS: bread_from_dough_1/3/6 dough slot lists create:dough/veggiesdelight:sweet_potato_dough/vintagedelight:oat_dough |
| SQ-VDF-04 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, row contradicts its @pass: quiche is oven only (creativedelight:baking/zucchini_quiche), yet counts want a quiche manual_only and assembly == 1; count /id=creativedelight:\S*zucchini_quiche_manual_only serializer=/ = 0, wanted == 1; count /id=creativedelight:\S*zucchini_quiche\S*sequenced_assembly serializer=/ = 0, wanted == 1; PASS: vegan_pizza_manual_only 3x2: row 2 dough, tomato_sauce, dough; assemblies (veggiesdelight/recipes.js 95-102) start on i.any_dough and fd.veggie_cooked_zucchini |
| SQ-VDF-05 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, regex \S*turnip_mutton_skewer\S* also matches raw_turnip_mutton_skewer_sequenced_assembly, added by the BBQ F.skewer move (plan_meals_rolls_bbq C4); one assembly for the cooked skewer; count /id=creativedelight:\S*turnip_mutton_skewer\S*sequenced_assembly serializer=/ = 2, wanted == 1 |
| SQ-VDF-06 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, stale namespace: tag is creativedelight:food_family/preserve/pickle and holds veggiesdelight:fermented_garlic_honey; creativeengineering: one is empty; no line matches /verb=tag kind=item tag=creativeengineering:food_family/preserve/pickle n=\d+ ids=.*veggiesdelight:fermented_garlic_honey/ |
| SQ-VDF-07 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, regex: 'n=0 shown=0' vs n=0$, and the expected unknown-item verb=item prints done=0 which the row rejects; all content checks pass; no line matches /verb=recipes text=creativedelight:cooking/fries n=0$/; /verb=\S+ .*done=0/ matched: [20Nov2026 02:25:05.075] [Server thread/INFO] [com.SaintAlfonzo.creativedevtools.test.TestCtx/]: [cdevtest] verb=item done=0; open look 1: Chips hidden in JEI is the client half |
| SQ-VDF-08 | PASS | uses lists hold creativedelight:createmixing/salting/moredelight_mashed_potatoes and .../veggiesdelight_mashed_potatoes |
| SQ-VDF-09 | BATCH-FAULT | reclassified from FAIL: judge-3, 2026-11-20, stale namespace: cans are creativedelight:canned_* (recipes creativedelight:shapeless/canned_* and _broth in recipes/all.csv); canned_mhadjeb unknown; done=0 is the expected unknown-item line; no line matches /verb=makes item=creativeengineering:canned_mashed_potatoes n=[1-9]/; no line matches /verb=makes item=creativeengineering:canned_mashed_sweet_potatoes n=[1-9]/; no line matches /verb=makes item=creativeengineering:canned_bat_soup n=[2-9]/; no line matches /verb=makes item=creativeengineering:canned_cave_soup n=[2-9]/; no line matches /verb=makes item=creativeengineering:canned_insect_stew n=[2-9]/; no line matches /verb=makes item=creativeengineering:canned_golden_nutritional_bar n=[1-9]/; /verb=\S+ .*done=0/ matched: [20Nov2026 02:25:29.224] [Server thread/INFO] [com.SaintAlfonzo.creativedevtools.test.TestCtx/]: [cdevtest] verb=item done=0; PASS: creativedelight:canned_noodle_soup effects=farmersdelight:nourishment, canned_bone_broth effects=-; _broth recipes for bat/cave soup and insect stew in recipes/all.csv; golden bar can inputs include honey_bottle (miners_delight/can_types.js) |
| SQ-VDF-10 | PASS | vintagedelight:cucumber_noodles not in compost now in the run or instance dump (unchanged, not compostable) |
| SQ-VDF-11 | PASS | farmersdelightcutting and createmixing garlic_clove_from_* give garlic_clove*5; sliceanddice:creativedelight_farmersdelightcutting/garlic_clove_from_cutting *5 |

## 9. r1120nv: every never-run row

Owner request (2026-11-20): execute the scratch test queue, starting with the never-run rows. This section is that
first pass; jar-changed and failed rows follow as their own drives once it ends.

- Scope taken: every never-run row with steps, server-side (`qrun.py plan --never --json` filtered to state `never`):
  1249 rows in 138 sections; phases server 1105, longwait 7, worldgen 108, long 29, in skill order. Plan file
  `perf/runs/r1120nv/plan_never.json`.
- Not taken: 317 never-run drafts (no steps; 270 server, 47 worldgen), client rows (pinned until wave 8), jar-changed
  (336) and failed rows (next drives), owner rows.
- Perf: session `r1120nv` (collector pid 915413).
- Jars, preflight 2026-11-20: `check_all.py --fast` 37 run, 0 failed. `jarstamp.py pending`: creativeBrewing
  (2611192245), creativeCalendar, creativeCompat, creativeLoot (2611200015) built, not installed; prep_srv.sh swaps them
  in. Per-step stamps in `perf/runs/r1120nv/sNN/pinned.json`. Disk 54 GB free.

### 9.1 Run notes

- 08:56 drive r1120nv started detached (182 steps, 1336 rows incl. lower-boot rows pulled in by @boot N rows), driver ETA about 3102 min; RUNNER.md claimed by the drive.
- 09:27 after steps 1 to 10 (49 rows: PASS 5, FAIL 20, LOOK 15, HARNESS 9): 9 HARNESS rows had {SAH1..3} / {SBH1,3}
  unset. Cause (ENV, batch): those rows use home coordinates from the r1103 home batches (`batches/r1103/sA_*.qb`
  capture SAH / SBH), never captured in their own rows file; the never-only selection is not the cause. Missed in
  preflight: no per-section `qrun.py lint --rows` was run before the drive. Drive stopped at 09:28 (step 11 had reached
  Done; the stop shut it down, no fault). Lint of every planned section with the same selection
  (`perf/runs/r1120nv/lint_bad.json`): 20 rows affected, a bounded fault, so they are judged BATCH-FAULT and the
  drive goes on. Rows to rewrite with their own home marker and capture, then rerun: SQ-BBQF-04, SQ-CNG-02, SQ-DRK-01, SQ-DW-02, SQ-EFB-01, SQ-EFB-15, SQ-EFB-16, SQ-ENM-03, SQ-FCB-00, SQ-FGD-12m, SQ-FGD-15c, SQ-JFP-01, SQ-PSD-06, SQ-PSM-06, SQ-PSM-07, SQ-PSM-08, SQ-RB-21, SQ-SLT-05, SQ-VTDA-10, SQ-WCD-07.
  Other lint warnings over the plan: W stale 945 (row sources changed after the rows were written: expect more
  batch faults on old sections), W boot 68, W waitlog 50, W unloaded 46.
- 09:30 drive resumed at step 11.
- 09:35 judging steps 1 to 10: SQ-SALT2-03, -04 are BATCH-FAULT (regexes expect the retired zq_probe output, e.g.
  "block tags of X:", where /cdevtest prints `verb=tagsof id=X block_tags=...`; the data itself is right). Decision (run
  default 3, as r1120): the drive goes on; stale-format rows are judged from their log evidence and reclassified
  BATCH-FAULT with the real result in the cause, then rewritten after the run. Not stopping to rewrite the 945 stale
  rows first.
- 09:34 steps 13, 14 FAILED in prep: modset port_chain (source ceporttesting) finds its chain jars missing (moved to CEPortTesting disabledmods/ 2026-11-13; r1119 note 20:52, r1118 handover 5). Not fixable here (another instance); the driver skips them, rows blocked; owner blocker stands.
- 10:0x ENV FIX port_chain (owner: these rows must run, fix in-run). Two causes: (1) the manifest took its jars from
  CEPortTesting, whose chain jars are in disabledmods/ since 2026-11-13 (Common-only setup there, left as is);
  `modsets/port_chain.toml` now uses source pack (pack mods/ plus build-jar swaps), kubejs probes (empty plus the zz_
  probes, as CEPortTesting's examples-only kubejs was), upstream pack. (2) qmodset.py checked `require` after the
  spec's -GLOB drops, so a spec that drops a chain jar on purpose (step 14: -creativecalendar -creativedelight
  -creativebrewing) failed even with every jar present; a require glob the spec drops is no longer missing. Test build
  of step 14's spec: 13 jars, drops applied, pinned. Drive stopped at a row boundary and resumed: resume resets the
  failed steps 13, 14 to pending and reruns them first. Clears the r1118 handover 5 / r1119 handover 3 blocker.
- 10:00 step 13 rerun crashed at FML load: creativedelight and creativebrewing require creativeloot (carved after the manifest was written; not in the chain group). Checked the mandatory deps of every own jar in the chain (mods.toml): only creativeloot was missing (Farmer's Delight is nested in Creative Delight). Added creativeloot-*.jar to port_chain.toml's chain group; crash report kept in perf/runs/r1120nv/crash_s12_loot/. Stopped and resumed at 10:01.
- 10:03 step 14 crashed at FML load: `kubejsdelight requires farmersdelight`. The spec of SQ-CBP-08 and its rerun row (CBP.qb section 1, Equipment without CB) drops creativedelight, which nests Farmer's Delight, but kept kubejsdelight from the chain group; never booted before (never-run row). Row fix: both @modset lines add `-kubejsdelight-*.jar`; step 14's stored modset and key in drive/state.json updated to match (backup state.before_cbp_fix.json). Crash report in perf/runs/r1120nv/crash_s13_kjsdelight/. Resumed 10:14.
- 2026-11-20 11:02 judge-1 real FAIL SQ-CCO-12a..f (all six Common-only sets): `creativecommon:kubejs/tags/wthittags.js#32`
  throws `TypeError: Cannot read property "required" from null` at boot and on /reload. The sets have no
  `kubejs/generation_scripts/wthit/silk_touch_blocks.json`; `JsonIO.readString` returns null, `JSON.parse` gives null
  and the script reads `doc.required` (no null guard, no jar fallback as `global.genText` gives others). Everything else
  in the six rows passed (startup ERROR count 0, Magic Resistance applied, Phantom Bane unknown only without beacons).
- 2026-11-20 11:02 judge-1 real FAIL SQ-GLD2-03: `[golden values] minecraft:pumpkin_pie is not a food` (x2): the
  golden and gilded pumpkin pie keep upstream 4 / 1.2 and 8 / 8.0 (2026-11-19 food dump) instead of golden_food.md 3.6's
  16 / 20; `goldenfood/golden_values.js` reads Pumpkin Pie's food after the pack made it a placed pie (log_warnings.md
  S16, no TODO row seen). Count line now reads 41 queued (row's 46 is stale); beef values right (16, 20).
- 2026-11-20 11:02 judge-1 FAIL SQ-CFR-05 (cause not proven): after `/weather rain` and 220 warped ticks
  (rainOutTicks 160) the open-sky campfire on stone and the fixture campfire stay lit; coal-block, soul and roofed ones
  lit as expected. Mixin applied (SQ-CFR-01), `creativecommon:campfire/rain_douses` filled, season clock early spring,
  plains. SQ-CFR-06 (stays lit) passed only because nothing douses. Suspect `CampfireRain.tick`'s `isRainingAt` gate
  (CD `campfire/CampfireRain.java`); rerun with a rain probe at the campfire before fixing.
- 2026-11-20 11:02 judge-1 FAIL SQ-ROLL-12 (known): Roll Delight medley cans (`canned_legendary_roll_medley`,
  `canned_mythical_roll_medley`) exist: the open SQ-ROLL-12 vs SQ-ROLL-27 owner pick (TODO `largecontent/mod-additions #39`).
- 11:04 judge-1 (steps 1 to 35, 101 rows): PASS 46, BATCH-FAULT 41, FAIL 9, LOOK 5 open. Real FAILs to TODO: SQ-CCO-12a..f -> wthit #17; SQ-GLD2-03 -> food-tuning #30; SQ-CFR-05 (-06 PASS void) -> misc-gameplay #1; SQ-ROLL-12 is the open owner decision mod-additions #39. Batch-fault causes grouped in judge-1's lines above, for one rewrite pass after the run.
- 11:20 qdrive.py: a step now re-reads its rows file at start and drops rows marked @superseded or @draft since planning (logged 'dropped'); for the parallel batch-fault session (Scratch test batch faults [688943]), which edits .qb rows from the front of the pending list. Driver stopped and resumed to load it.
- 11:27 ENV FIX step 23 (CDJ.qb section 1, incomplete 1/6): qrun crashed with FileNotFoundError on RES/rows/'SQ-CDJ-05 / SQ-DDP-01 rerun (I1)'.shell.txt (a '/' in the row id); the 10:14 and 11:20 reruns of the step died the same way. qrun.py, qverdict.py, qlint.py, qpair.py now use a file-name form of the id (fsid: '/' -> '_') for rows/, verdicts/ and crash/ paths; qtests 20/21 (PROC-20 = queue render behind the parallel session's .qb edits, not this change). Step 23's 5 rows rerun by drive resume after the drive ends. Parallel session added skill run default 11 (simple BATCH-FAULT fixed and rerun in-run; rows in its owned files go to it).
- 2026-11-20 11:32 judge-2 real FAIL SQ-FF-04: `corn_delight:nachos_bowl` reads 12 / 1.0, wanted 13 / 1.0 (the cheese
  rule, `creativedelight/cheese_values.js` row `['nachos_bowl', 13, 1.0]`); the other eight dishes of the same list
  applied (broccoli soup 13 / 0.7 ... fiery fondue 15 / 0.75), no `[food values]` error. Suspect the
  `food_value_copies.js` `absolute()` modify does not reach the Corn Delight item (stack-sensitive food or a later writer).
- 2026-11-20 11:32 judge-2 BATCH-FAULT (steps 36 to 45, s35_res..s44_res; causes in each verdict): SQ-DEP-05: no forceload
  at 330,300 (campfire douse untested, tag right); SQ-DEP-02 (rerun 5.2): no @modset dropping creativebrewing, full pack
  booted; SQ-DRK-01: {S5H*} home capture (09:27 group); SQ-DRK-04: bytype create:filling capped at 500 lines and the s5
  spout probe reads -1 mB for the water control too (createfilling/kiwi_tea exists); SQ-EX-01: `Gave 1 \[` vs padded
  `Gave 1    [`; SQ-BOT-04: zz_c3_probe prints TransitionalDisplayState@hash, no states; SQ-FCK-20: no forceload at
  1200..1234,1200 and '35 rules on 35 blocks' should be 33 blocks (two lantern blocks have two rules); SQ-FCK-21,
  SQ-FCK-23: no forceload (1250,1200; 1260,1240); SQ-MG-23: itemMerges count 2 -> 4 (Little Logistics components;
  item_merges.md line 553 stale too); SQ-MG-24: rope rdetail prints the shaped grid with `empty` slots; SQ-T1-02: regex
  wants cookedrottenham,rottenham adjacent, frozen_flesh sorts between; SQ-FDI-05: shell reads world dir r1113c4a/, the
  step world is r1120nv_<step>; SQ-FF-07: `recipes minecraft:melon` substring hits melon_seeds, rdetail _from_slices
  capped at 40 of 46. Real results of all 14 PASS where tested.
- 11:32 judge-2 (steps 36 to 45, 54 rows): PASS 38, BATCH-FAULT 14, FAIL 1 (SQ-FF-04), LOOK 1 open (SQ-T1-01 names
  half, client lang).
- 11:33 judge-2 (steps 36 to 45, 54 rows): PASS 38, BATCH-FAULT 14, FAIL 1, LOOK 1 open (SQ-T1-01 names need a client lang dump). SQ-FF-04 -> TODO meals #69. Note: SQ-DEP-05 (campfire rain douse) unloaded, so campfire rain is still untested beside SQ-CFR-05.
- 11:44 qdrive.py reads drive/skip.txt at each step start (rows left out of a running drive without a restart); SQ-PROC-14 (step 83) skipped: T3 release gate needing an install-mode payload, deploy/latest is the 2026-11-09 update payload; runs at release. Driver restarted to load it. Parallel session's rerun list perf/runs/r1120nv/rerun.txt holds 83 fixed rows for a rerun drive after r1120nv.
- 11:53 skip.txt + rerun.txt: SQ-FSD-02 (step 149) and SQ-VH-12 (step 169) gained @boot 2 after planning; SQ-FTW-02 (step 151) had @group same -> A, now @group B (FTW.qb line 33, same world kind as SQ-FTW-01). All three run in the rerun drive. Worldgen sections GFB, WGP, BLC, CWG, RING, SVA, TER now prep group A as normal worlds (batch-fault session): longer boots there.
- 2026-11-20 13:45 judge-3 real FAIL SQ-FF-09: `corn_delight:corn_seeds` reads 1 / 0.2, wanted 1 / 0.25 (`creativedelight/edible_seeds.js` row
  `['corn_seeds', 1, 0.25]`); sunflower 0.25 and the pumpkin / oat / mixed seeds 0 applied. Same `foodValueCopies` absolute
  path as SQ-FF-04's `corn_delight:nachos_bowl`: Corn Delight items are not reached (suspect the applier or a later Corn Delight writer).
- 2026-11-20 13:45 judge-3 real FAIL SQ-CNP-01: `[zsb] cans: 22 recipes take a pack can, 1 violations`: Raspberry Pie's own
  `solraspberrypie:liquid_diet_log` (crafting_shapeless) takes `canned_goods:oculemonade` without `_manual_only` (suspect that recipe,
  or the NCH can guard not covering mod recipes); no Create / IE / AE2 route takes a can.
- 2026-11-20 13:45 judge-3 real FAIL SQ-FO-08: `supplementaries:soap` and `creativecompat:createmixing/soap_heated_mixing` take only
  `rusticdelight:cooking_oil`, not blubber or whale oil (pass: both take them; Compat `other/misc_mods.js` lines 21-29 `SOAP_FAT`
  collapses to one item; Compat jar 2611200015 holds the 2026-11-19 change). Blubber item, cut and tag right; cachalot drop untested.
- 2026-11-20 13:45 judge-3 real FAIL SQ-GLD-03 (milk half): after `effect give speed 60` both golden and enchanted milk leave
  `minecraft:speed:0:792`, not effects=- (golden_food.md 3.1: milk clears every effect, `ShinyItems.GoldenMilk` / `Milk`). The 1200 t Speed
  was gone, a fresh ~800 t Speed was there (honey lines also showed Speed 780 / 650): source not proven, rerun with a probe before fixing.
  Stew half stale (stews removed 2026-11-10), honey stack 16 per the doc (row says 8 / 4); honey cures Poison.
- 2026-11-20 13:45 judge-3 FAIL SQ-PCK-02 (known, 9.2 owner item): the pancake tags hold 8 (the seven plus `hearthandharvest:waffle`).
- 2026-11-20 13:45 judge-3 BATCH-FAULT (steps 46 to 57 and step 23's 5 rows; causes in each verdict). Fixed and queued (below): SQ-FF-11,
  SQ-FFH-02, SQ-CNS-03, SQ-CORN-04b, SQ-FO-02, -04, -07, -14, SQ-FFB-04 (look PASS, namespace), SQ-PS-06, SQ-VNG-01, SQ-STD-03,
  SQ-CNG-01, SQ-ENM-01, SQ-GLD-02, SQ-SWP-03, -04, -05, SQ-GAP-05. For later (not simple): SQ-CDJ-05/DDP-01 rerun (I1) and
  SQ-CDJ-12 (I8) (2026-11-02 swap lists on the Boot 0 world; jar now creativedelight-1.1.0.jar, members newer, row stale per lint);
  SQ-FFB-02 (owner: keep Oceanic's sponge_cake_from_slices? then n=2 and serializer creativedelight:baking); SQ-FO-01 (32 food types with
  sweet/pancake, food.md still 31); SQ-CF-02 (Spice of Life config reset by plan_diet_books_r2: row obsolete); SQ-ENM-03 (needs @modset
  without Ender's 1.2.2 and 60 rounds), SQ-ENM-04 (toml + restart missing), SQ-ENM-04b (reject hits /reload recipe warnings, waitlog
  matched 'Reloading!'; lint W cfgedit); SQ-GLD-05 (bot from SQ-GLD-04; expect the pass column), SQ-GLD-08, -11 (stale looks: creativedelight:
  ids, 8 ingot loops), SQ-GLD-12 (21 after the stew removal, Boot 0 shell), SQ-GLD-14 (reject hits the probe line, librarian offers
  unread), SQ-MDL-01 (27 drop pools since the blubber rule), SQ-PCK-04, SQ-SLT-04 (retired r1103 Boot 0 path), SQ-SLT-03 (salt probe:
  melting moved to Common 2026-11-14, placed_rock_salt_block), SQ-SLT-05 (config is `[salt]` in creativedelight-common.toml since
  S10), SQ-SWP-01, -08 (creativecommon-swaps.toml, item_merges.md 6 still names creativeengineering-swaps.toml), SQ-SWP-06 (setup row
  SQ-SWP-06o not run), SQ-HCS-07 (feature nest from the r1113 c4_a world). Real results PASS where tested (SQ-ENM-03 chorusflame growth inconclusive).
- 2026-11-20 13:45 judge-3 owner looks / records: SQ-GAP-05 the golden egg sinks to the water bottom (-63.0) while a plain egg floats
  (-60.02) (row wants the same depth; item-physics #5); SQ-GAP-07 grape seeds stay on stone (unlisted cell, for the writeup).
  Pre-screen hint: `mods/creativedelight-1.0.0.jar` (now 1.1.0) is still named in 17 .qb files and `> ceswap` in IDS.qb (3).
- 2026-11-20 13:45 judge-3 BF-FIX (rule 11, rerun in perf/runs/r1120nv/rerun.txt, now 135 ids): SQ-FF-11 `\S+pizza_slices` -> `\S*`;
  SQ-FFH-02 look follows the oven-only muffins; SQ-CNS-03, SQ-CORN-04b, SQ-FO-02, SQ-FFB-04, SQ-PS-06, SQ-VNG-01 ids moved to
  `creativedelight:` (SQ-FO-02 carrot reject -> expect, per its pass); SQ-FO-04 reject only an S&D copy of the seed `_manual_only`;
  SQ-FO-07 `reject verb=recipes id=alexsmobs:boiled_emu_egg result=`; SQ-FO-14 one rdetail per powder; SQ-STD-03 BurnTime 120 to 300
  (read 8 s into the burn); SQ-CNG-01 bootexpect on the builds line; SQ-ENM-01 `creativedelight-*.jar`; SQ-GLD-02 max_stack 16;
  SQ-SWP-03, -04, -05 `/ccswap` (SQ-SWP-05 places its own chest, reads `Items[{Slot:1b}]`); SQ-GAP-05 `.*item_tags=`. SQ-FF-09's
  `modifier=0\.0` regexes take `modifier=0` too (row stays FAIL, not queued). Lint clean for each.
- 13:46 judge-3 (steps 46 to 57 + step 23's 5 rows, 92 rows): PASS 47, BATCH-FAULT 40, FAIL 5 (SQ-FF-09, SQ-CNP-01, SQ-FO-08, SQ-GLD-03,
  SQ-PCK-02 known), no LOOK open.

### 9.2 r1120fix: batch-fault fixes, pre-screen and fixed rows waiting for rerun

Owner request (2026-11-20 11:10, session "Scratch test batch faults"): fix the r1120 and r1120nv batch faults, fix
pending rows that would probably batch-fault before the drive reaches them, mark unneeded rows superseded, and add a
skill rule for fixing and rerunning simple batch faults in-run. Six subagents owned the `.qb` files by drive position
(G1 FF..HCS and the judge-2 files DEP, DRK, EX, FCK, FDI, FF; G2 HH; G3 HOP..PWG; G4 RB..VN; G5 the worldgen and long
files; G6 files with no pending step). Drive owner ("Scratch tests breakdown") kept the drive, state.json, qdrive.py and
judging; no edit touched a running step. Brief, inputs and per-group results:
`tools/scratch_query/batches/r1120fix/` (BRIEF.md, G1..G6.csv: one line per test id, kind and fix).

- Counts (G1..G6.csv): bf-fixed 112 (already ran, rerun), prefixed 288 (pending, fixed before running), reviewed 325
  (stale, source diff read, no change), superseded 24, supersede candidates 6, suspected product 4, unfixed 13.
- Pre-screen (`qprescreen.py r1120nv`), pending steps: before, about 300 likely findings (E var 7, W var 54, W unloaded 61,
  dead ids 184 including structure ids the dumps lack) and 778 stale rows; after, 20 likely, all W unloaded on bot
  spawns, teleports, locate origins or generator probes (checked, intended), 2 stale.
- Main causes fixed: home captures from the retired r1103 batches (SAH, SBH, S5H: each row now places its own marker);
  captures read from rows the selection skips; missing forceloads; ids moved to `creativedelight:` / `creativecommon:` /
  `creativebrewing:` / `creativeworldgen:`; retired `/zq` output; stale counts (merges 4, idle-tick rules 35 on 33
  blocks, stack sizes); salt licks now the placed Rock Salt Block (18 HH rows); stale `@draft` lines on rows with steps
  (the drive drops them: SQ-LOOT-07, -09, SQ-PTH-29, -30, SQ-TP-42); worldgen group A sections that need terrain now
  boot normal worlds (`@@prep A: level-type=minecraft:normal ...`); rows needing their own boot got `@boot 2` or
  `edit` + `restart`; `expect` used on shell output (reads the log only).
- Superseded (24): SQ-BBQ-08, SQ-CFR-02, SQ-CIF-01, SQ-CWG-02, -04, SQ-EDL-01, -02, SQ-HH-464, -474, -475, -476,
  SQ-MDP-03, -12, SQ-PST-06, -09, -10, SQ-PWG-09, -12, -24, -45, -46, -47, -67, SQ-RW-04 (reasons on each row).
- Tooling: qrun.py lists `@superseded` rows and runs none; qdrive.py drops them (drive owner merged); new
  `qprescreen.py`, `qrerun.py`; `merge_core_probe.js` checks the Creative Delight potato cutting id;
  `dump_diff.py` trades mode ignores the listing object hash. Skill run default 11 (fix and rerun), B1 pre-screen,
  gotchas (r1103 home captures, superseded rows in a planned drive).
- Docs: crops.md "Grasses" follows the shipped 1.2.02 rule (winter off only); item_merges.md expects 4 pack merges.

Fixed rows waiting for rerun: `perf/runs/r1120nv/rerun.txt` (116 ids: the bf-fixed rows plus SQ-FSD-02, SQ-VH-12,
SQ-FTW-02 skipped in r1120nv after their @boot / @group changed). The drive owner runs
`qrerun.py perf/runs/r1120nv/rerun.txt` and `qrun.py drive start <RUNr> --plan-json perf/runs/r1120nv/rerun.plan.json`
after r1120nv. Skipped in r1120nv (`drive/skip.txt`): SQ-PROC-14 (release gate), SQ-TSY-01 (needs a remote client),
SQ-WCD-04 (needs its own fresh level, rewrite after the drive).

Open for the owner (not edited):
- Suspected product: SQ-PCK-02 (8 pancakes in the pancake tags since the Hearth & Harvest waffle; row expects 7);
  SQ-PST-12 (row expects jungle grass slowed, code makes tropical seasons neutral); SQ-PSU-09 (steps expect
  Nourishment from vinegar, pass text says Nausea only); SQ-RFX-28 (British "colour" in Compat config comments, TODO
  `conventions #39`); SQ-AV-01 ("Unknown recipe: minecraft:furnace" on a full-pack scratch server, though the dump
  lists it; row now uses a stick).
- Expectations changed to follow the docs, veto if the docs are wrong: SQ-LGM-11 Comfort 120 s (R5, dump; plan A4 said
  1 min); SQ-HH-477 Rock Salt dispenser keeps a waxed lick waxed (salt.md 8); SQ-PST-03, -08, -09c, -10c, -11, -11b
  grass winter only (1.2.02).
- Supersede candidates: SQ-ENM-05b (duplicate commands of SQ-ENM-05), SQ-SLK-01 (SQ-SLK-01c covers it), SQ-CBP-07
  (legacy config carry), SQ-CUP-05 (needs a save from before the cup removal), SQ-MDP-01, -06, -10 (half removed cups,
  half valid: split or rewrite).
- Unfixed: SQ-FF-03..08 ran before the fix (no fault found in lint); SQ-PROC-14, SQ-TSY-01, SQ-WCD-04 (skipped above);
  SQ-PSW-03 waitlog shares text with SQ-PSM-04 (only bites on a locate timeout); SQ-DEP-02 (@modset, drive owner).
- Tool follow-ups: `s5_probe.js` spout reads -1 mB even for water; `c3_probe.js` pot prints TransitionalDisplayState@hash
  (no row depends on either now); `salt_probe.js` should accept `hearthandharvest:placed_rock_salt_block`; qrowlint
  calls structure ids dead (read `kubejs/exported/structures.json`); qlint W unloaded fires on bot teleports, locates
  and `dim=` probes; SQ-PWG-90 still expects the -17 jar; SQ-MOB-N6, -N8 (WGE) unset captures, not in a pending step.
- 13:17 ENV FIX step 23 (5/6): SQ-VTD8-03 (Vintage -18, strict per slot) never ran: its id holds a comma and the driver's --rows list is comma-joined, so the id split and matched nothing (silent skip). qrun.py picked() and qlint.select now pick an id when every comma part is in the set. Step 23 reruns that row on the post-drive resume.
- 13:35 judge-3 (steps 46 to 57 + step 23's 5 rows, 92 rows): PASS 47, BATCH-FAULT 40, FAIL 5; 19 rows fixed and queued (rerun.txt 135). TODO: SQ-FF-09 joins meals #69 (Corn Delight items miss foodValueCopies values); SQ-CNP-01 -> canning #22; SQ-FO-08 -> mod-integration #1; SQ-GLD-03 -> food-tuning #31; SQ-PCK-02 is the owner item in 9.2. CDJ.qb SQ-CDJ-12 shells now glob mods/creativedelight-*.jar (rest of the row still stale, not queued).
- 14:17 handover point (main session): drive alive at step 58 (HH.qb section 2, 274/513). To continue in a new session:
  `qrun.py drive status r1120nv`; judge finished steps from s57_res on (judges 1 to 3 covered s00 to s56, s22); after
  the drive ends: `qrun.py drive resume r1120nv` (step 23's SQ-VTD8-03 and any incomplete steps), then the rerun drive
  `python3 tools/scratch_query/qrerun.py perf/runs/r1120nv/rerun.txt` -> `qrun.py drive start r1120nvr --plan-json
  perf/runs/r1120nv/rerun.plan.json`; SQ-DEP-02 needs its @modset (drops creativebrewing) before the rerun; then
  jar-changed and failed rows; B5 write-back and perf_session.sh stop r1120nv at the end. Open owner question: fix the
  WTHIT null guard (wthit #17) mid-run or not.
- 14:40 wound down by the owner: `drive stop` at a row boundary in step 58 (HH.qb section 2, boot 1, 329 of 513 rows);
  no JVM left; RUNNER.md idle; perf session r1120nv stopped and reported
  ([summary](../../perf/sessions/r1120nv/summary.md), no `--prune`).

### 9.3 Findings

Scope run: steps 1 to 57 of 182 done (step 23 at 5 of 6), step 58 stopped at 329 of 513; steps 59 to 182 not started.
Verdicts (`qrun.py table` over the result dirs):

| Part | PASS | BATCH-FAULT | FAIL | LOOK open |
|---|---|---|---|---|
| Steps 1 to 57, judged (judges 1 to 3) | 131 | 95 | 15 | 6 |
| Step 58, HH section 2, not judged | 187 | - | 28 | 114 |

Real product bugs (TODO rows; fixes wait for the owner):

| Rows | What is wrong | TODO |
|---|---|---|
| SQ-CCO-12a to -12f | `wthittags.js:32` reads `doc.required` with `doc` null when the generated silk touch list is absent (Common-only sets): TypeError at boot and on /reload | wthit #17 |
| SQ-GLD2-03 | Golden and gilded pumpkin pie keep upstream values (4 / 1.2, 8 / 8.0; wanted 16 / 20): golden_values.js reads pumpkin pie after it became a placed pie | food-tuning #30 |
| SQ-CFR-05 (SQ-CFR-06 PASS void) | Campfires stay lit in rain; suspect the isRainingAt gate in CD CampfireRain.java; SQ-DEP-05 (douse) did not reach its check | misc-gameplay #1 |
| SQ-FF-04, SQ-FF-09 | Corn Delight items miss their foodValueCopies values (nachos bowl 12 not 13, corn seeds saturation 0.2 not 0.25) | meals #69 |
| SQ-CNP-01 | `solraspberrypie:liquid_diet_log` takes a can without `_manual_only` | canning #22 |
| SQ-FO-08 | Soap takes only cooking oil, not blubber or whale oil (Compat misc_mods.js SOAP_FAT) | mod-integration #1 |
| SQ-GLD-03 | Golden milk leaves a fresh ~800 t Speed after clearing effects; source unproven | food-tuning #31 |
| SQ-ROLL-12 | Roll Delight medley can recipes exist: open owner decision vs SQ-ROLL-27 | mod-additions #39 |
| SQ-PCK-02 | Waffle makes the pancake tags hold 8, not 7: owner item in 9.2 | (9.2) |

Low-priority tooling row: generators-and-tests #27 (`/cdevtest` bytype 500-line and rdetail 40-match caps).
Owner looks: SQ-GAP-05 (golden egg sinks while a plain egg floats), SQ-GAP-07 (grape seeds stay planted on stone).
Open LOOKs: SQ-AR-01, SQ-BBQ2-04 (client), SQ-BBQ2-08 (eating), SQ-CEQ-02, SQ-CFS-03, SQ-T1-01 (client lang dump).

Batch faults (95): causes grouped in the judge lines of 9.1; most rows are fixed and on the rerun list (135 ids,
`perf/runs/r1120nv/rerun.txt`; fixes by the parallel batch-fault session, section 9.2, and judge-3). Still to rewrite:
SQ-CDJ-12 (stale member versions), SQ-ENM-04b, the not-simple causes in 9.1, the WG30 rows whose ids carry old results.

Environment faults found and fixed in-run (tools committed f3f1cfc2, d64c746a, bad2bdd5, 965db4dc): port_chain mod set
(pack jars, Creative Loot, require vs `-GLOB` drops, SQ-CBP-08 drops kubejsdelight); row ids with "/" crashed qrun;
row ids with "," never ran; driver drops rows marked @superseded / @draft and rows in `drive/skip.txt` at step start.
Not fixed: none outstanding. Gotchas added to the skill's gotchas.md.

No player changelog bullet: a test run, nothing player-visible changed.

### 9.4 Handover

State: drive r1120nv stopped and resumable (`perf/runs/r1120nv/drive/state.json`); RUNNER.md idle; perf session closed.
Next session, in order:
1. Claim RUNNER.md, preflight (skill B1), new perf session; `python3 tools/scratch_query/qprescreen.py r1120nv`.
2. Judge step 58's 329 rows (`perf/runs/r1120nv/s57_res`: PASS 187, FAIL 28, LOOK 114) as judges 1 to 3 did.
3. `qrun.py drive resume r1120nv`: reruns step 23's SQ-VTD8-03 and continues step 58 at row 330, then steps 59 to 182
   (server, then longwait, worldgen, long; about 45 h by the driver's estimate). Skipped by `drive/skip.txt`:
   SQ-PROC-14 (release gate), SQ-FSD-02, SQ-VH-12, SQ-FTW-02 (on the rerun list).
4. Before the rerun drive: SQ-DEP-02 needs an @modset that drops creativebrewing. Then
   `python3 tools/scratch_query/qrerun.py perf/runs/r1120nv/rerun.txt` and
   `qrun.py drive start r1120nvr --plan-json perf/runs/r1120nv/rerun.plan.json`.
5. After the never-run pass: the jar-changed rows (336) and failed rows (`qrun.py plan --failed`), then the 317
   never-run drafts need steps.
6. Write-back (B5): Run log entry per drive; per-feature writeup test lists and README status words were not updated
   in this session (too many features for the judged part; do it with the judged totals per feature).
Owner question still open: fix the WTHIT null guard (wthit #17) before the resume, so later Common-only steps test it.
