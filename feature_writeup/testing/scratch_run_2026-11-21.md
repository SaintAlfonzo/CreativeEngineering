# Scratch run 2026-11-21: r1120nv resume, then the rest of the queue

## 1. Scope

Owner request (2026-11-21): orchestrate the never-run scratch rows and fix test bugs as they appear; when the never-run
rows run out, continue with the rest of the queue. Order: judge r1120nv step 58 (s57_res), resume drive r1120nv
(steps 58 at row 330 to 182), rerun drive r1120nvr (`perf/runs/r1120nv/rerun.txt`), then jar-changed rows, failed rows,
then never-run drafts (steps needed). Coordination: session `*Plan_owner_rows_1120.md` is editing Creative Loot,
Creative Worldgen, Creative Devtools and OR1120.qb, FCK.qb and the loot / mob tweak .qb files; no boot until it reports
"editing done", none of those .qb edited by this run until then.

## 7. Run notes

- 2026-11-21 judge-4 (s57_res, SQ-HH-201 to -263, 63 LOOKs): PASS 45, FAIL 0, BATCH-FAULT 14, all fixed in HH.qb under
  rule 11 (lint clean, rerun.txt now 164 ids; 216, 218, 219, 229, 255, 256 rely on SQ-HH-201's forceload):
  - BF-FIX SQ-HH-201, -204, -205: stomping count 9 stale, 17 since the 8 Vinery Grapevine Pot mirrors (2026-11-14,
    vinery_stomping.js) -> count 17, mirror lines in 204, `grape` dropped from 205's reject; 201 gets `weather clear`.
  - BF-FIX SQ-HH-206: bottles tag 26 (cherry cream soda, passion aloe nectar removed; Neapolitan milk bottle merged)
    -> look 26. Writeup J49 wording still says 29 (doc owner).
  - BF-FIX SQ-HH-211, -255, -256: stray "UNPARSED CMD" look from @commands prose -> deleted.
  - BF-FIX SQ-HH-215: no read after the second use -> tank and SelectedItem reads added.
  - BF-FIX SQ-HH-216: jug fill steps at x 179 never written -> 4 steps added, stray look deleted.
  - BF-FIX SQ-HH-218, -219, -221: rain (4 mB/s into troughs) until SQ-HH-223's weather clear -> `weather clear` first
    in each row; 219 counts `water*500/1000` x3.
  - BF-FIX SQ-HH-229: real-time drift between fill and read (890) -> fill just before the 320-tick warp, read right
    after, expect 930 to 960.
  - BF-FIX SQ-HH-234: `sleep 7` timing -> `cdevtest tickwarp 400`, expect 1100 to 1400 of 10000 (100 mB per 2 s).
- 2026-11-21 judge-5 (s57_res, SQ-HH-264 to -1328 LOOKs + the 28 FAILs): step 58 table now PASS 285, BATCH-FAULT 32,
  FAIL 12, LOOK 0. Real FAILs: SQ-HH-1106, -1109, -1110, -1111 (Nourishment missing on H&H foods, comfort_nourishment
  .json, TODO mod-additions #53 open); SQ-HH-1230, -1232, -1326 (Quark Pig Litters stacks on the H&H litter, mod-additions
  #52 open); SQ-HH-1273 (young hemp gives straw, PitchforkStraw.isMature, known); SQ-HH-262 (3-block drop no stomp,
  known PEAK sampling); SQ-HH-228 (no manure via trough feed, ManureDrops.schedule suspect); SQ-HH-227 (no calf after
  400 ticks, uncertain); SQ-HH-1292 (wolf dead before the Prickly read, uncertain). PASS with caveats: SQ-HH-1216 (IE
  fertilizer recipes expose no ingredients to `uses`), -266 (one dirt placed on the basin top), -301 (scratch toml
  left by an earlier SQ-HH-318 run on the reused server). Not simple batch faults: SQ-HH-303 (wild_hops is a
  trunk_vine now, needs a trunk fixture), -325 (Lootr chests, unpack 0), -1307 (Create tank ignores bot bucket use),
  -1267 (squid ignores hits, needs a water pool fixture).
- 2026-11-21 01:12 preflight: RUNNER claimed; check_all --fast 38 run, 0 failed; game_jvms empty (one transient JVM
  seen once, gone on recheck); disk 46 GB free; jarstamp pending: creativeBrewing, creativeCalendar, creativeCommon
  (changelog text only), creativeCompat, solRaspberryPie built newer than mods/ (prep_srv.sh swaps them in). Other
  session installed creativecommon, creativedevtools, creativedelight, creativeengineering, creativeequipment,
  creativecompat, creativeloot, creativeworldgen (agent_staging 8498f460). Perf session r1121nv (collector 797129).
  qprescreen r1120nv: 126 steps, 20 likely (unloaded coords, mostly worldgen steps), 112 risk, 52 stale; fixer-1 on
  the non-HH files, judge-5 on SQ-HH-459.
- 2026-11-21 judge-5 BF-FIX (HH.qb, ids confirmed in kubejs/exported, rerun.txt now 179 ids): SQ-HH-1108 saturation
  0.3 -> 0.25 (edible_seeds.js); -1141, -1142 cookie_dough and -1214, -1205 cornmeal -> creativedelight:; -1215 corn
  cutting per farming/crops #48; -1153 pot ingredients per meals #64; -1157 shapeless biscuits removed (meals #64), expect
  n=0 (@pass prose still old); -1280 tapper refill, Amount \d{1,2}; -1241 read 1 s after the effect, \d+; -1298 goals
  read at 2 s; -329 hops -> hops_vine; -1304 alexsmobs:crow -> hearthandharvest:crow; -318 placed_rock_salt_block in the
  countset; -227 tickwarp 400 -> 600 (row's @fails asks for it); -459 forceload at the bot spawn. -1292 left FAIL.
- 01:14 first `drive resume` refused: RUNNER.md held by this session's own claim with a `Run:` line not naming
  r1120nv (qdrive.claim only accepts its own run id) -> `Run: r1120nv resume (...)`; resumed 01:14:22, step 23 group A
  booted (pid 803304). Gotcha for the skill: claim RUNNER with `Run: <drive run id> ...` or let the driver claim it.
- 01:15 fixer-1: the 19 remaining prescreen "likely unloaded" findings in IP, PB, PF, UPG, FGD, FTW, SVA, TER, WG30, WGE
  are false positives (qlint.py about lines 354-388 reads `positioned ... run locate` origins, `pbot spawn` / `tp <bot>`
  targets, probe grid arguments and `cdevtest ... dim=` positions as unloaded overworld blocks). No .qb changed.
  Tool fix for later (not mid-drive): teach qlint those four cases. The 52 stale rows sit in CDJ, LOOT, MT, PJL, RC,
  RFX, SMK, CWG, HH.
- 01:3x step 23 (CDJ group A rerun, s22_res): PASS 3, BATCH-FAULT 2, FAIL 1. SQ-VTD8-03 FAIL again: jar (c) at 1251 made
  one batch (pickle*5 in slot 7) and stopped with cucumber*2 in slots 0-4 and salt_dust*2 left; expected 15 after 3600
  ticks. Suspect: the fermenting jar does not merge a second batch into an occupied output slot, or stops after one
  cycle (vintagedelight fermenting jar block entity, Creative Delight Vintage member). Product finding, not fixed in-run.
- step 58 HH group A resumed at row 330.
- 02:55 step 58 done (513/513): PASS 385, FAIL 27, BATCH-FAULT 32, LOOK 69 -> judge-6 (rows 330 to 513, edits HH.qb
  only while no HH step runs). Step 59 (HH section 3): SQ-DEP-02 (rerun), SQ-MIN-03 (rerun) reclassified BATCH-FAULT
  (both need a reduced @modset: without creativebrewing / without Corn Delight; section 4 boots the full pack). Not
  simple; for the modset rewrite pass. Helper `tools/scratch_query/drive_wait.sh RUN [UNTIL]` added (background wait for
  a step change).
- 2026-11-21 judge-6 (s57_res rows 330 to 513): step 58 table final PASS 452, BATCH-FAULT 43, FAIL 18, LOOK 0.
  - Real FAILs (new): SQ-HH-1607 to -1611 watering can on ripe wheat on dry farmland: use CONSUME, WaterLevel stays 5,
    no moisture (HNH-33 says a ripe crop only gets soaked); suspect Quark Simple Harvest takes the click before
    WateringCanItem.useOn (hearth member item/WateringCanItem.java, gear/QuarkCompat.java). SQ-HH-1606 wheat age 0 on
    moisture 0: use PASS, clicked wheat gone (23 of 25 left), cause unproven. No TODO yet.
  - BF-FIX SQ-HH-1511 lamp lit window 20 ticks -> `cdevtest watch` before the summon; -1520 stem state read added (plus
    forceload, spawn, bag hold); -1532, -1533 dry farmland reverted on random ticks -> randomTickSpeed 0 around till and
    count; -1588 n=0 counted JEED pages -> reject excludes `:jeed/` (JEED pages for the removed Fear effect still exist:
    creativeequipment:jeed/gear_alexsmobs_fear, creativebrewing:jeed/alexsmobs_fear, doc owner); -1590 waffle in
    food_type/baked per owner ruling R8 (writeup J84 still sweet/pancake, doc owner); -1595 persistent cherry leaves;
    -477 block-data regex; -494 quoted NBT item; -501 J66 cooldown allows repeat thefts -> reject gold_ingot*4.
    Stray `look UNPARSED CMD` lines deleted on PASS rows -410, -422, -428, -431, -433, -445, -447, -454, -457, -470,
    -484, -486, -495, -500. rerun.txt now 189 ids.
  - Not simple: SQ-HH-439 (tamed crows also fetch thrown items, 13 diamonds for 7 crows; row design). HH.qb has two
    `## SQ-HH-1574` headers (lines about 10935 and 11788): SQ-HH-1573's shell step errored on it (row rewrite pass).
  - PASS with caveats: SQ-HH-1502, -1515, -1587, -1594, -1605 (thin evidence, see verdicts).
- 03:5x judge-7 (steps 60 to 69): 60 PASS 1; 61 BF 2; 62 PASS 2 FAIL 1; 63 PASS 4 FAIL 1; 64 FAIL 4 INCONCLUSIVE 1; 65 PASS 1;
  66 PASS 4 BF 1; 67 PASS 3 BF 2; 68 empty; 69 PASS 1.
  - Real FAILs: SQ-HOP-12 organic mash on an age-1 hops vine uses both items and jumps to age 3 (HopsVineBlock.use hands
    any BoneMealItem to OrganicMashItem.useOn, which applies bone meal 3 times and shrinks once more; hearth
    block/HopsVineBlock.java, vintage item/custom/OrganicMashItem.java). SQ-PRT-01 lightning frame: Immersive
    Weathering crying_obsidian_from_lightning broke the frame, no PortalSpawnEvent (chance-based; r1114 passed; owner
    call). SQ-JFP-01 to -04 Create pump at 64 RPM moves nothing between tank and jar in any phase (creativeCompat
    compat/JarFluidPipes.java suspect, or the layout lacks a pipe segment; unproven); SQ-JFP-05 INCONCLUSIVE.
  - BF-FIX SQ-HH-1546 (HOP.qb) bot G never spawned -> spawn + wider forceload; SQ-HH-266 (HOP.qb) dirt*[34] (r1115
    ruling); SQ-LOOT-01 baseline adjusted by loot/tables #18 (Lexica rule), @reviewed 2026-11-21.
  - BF-FIX SQ-LBS-04 (orchestrator, tools/scratch_query/zlbs_probe.js, only LBS.qb uses it): hosts at Y -58 over air,
    the Supplementaries sack fell before refit -> stone floor under each host in partLabels and partCentre; refit's
    sticky-note hit skips the second `Text:"top ` note. Lint clean, rerun.txt 193 ids.
  - Not simple: SQ-LOOT-03 (2026-11-17 dump baseline stale: 9 namespace dumps differ, creativeloot.json and
    moremooblooms.json new; needs a refreshed baseline).
- 04:4x judge-7 batch 2 (steps 70 to 84): no real FAIL. 70 PASS 1 BF 1; 71 PASS 1 BF 1; 72 PASS 1 BF 1; 73 PASS 1; 74 PASS 4;
  75 PASS 2 BF 1; 76 PASS 2; 77 PASS 1 BF 2; 78 PASS 1; 79 BF 1; 80 PASS 2 BF 2; 81 PASS 1; 82 PASS 3; 83 empty; 84 PASS 1 BF 1.
  - BF-FIX SQ-MRD-02 cheese toast -> creativedelight:cheese_toast; SQ-MT-02 peaceful removes the creeper -> difficulty
    easy around the summon; SQ-NSM-01 Seasoning -> Flavor (seasonings.md 7) ids; SQ-PFS-03 regex `[. ]` and own bot spawn;
    SQ-PF-01, -02 idle flat world ticks nothing -> tagged pig and hopper during the check, PF-02/-03 shell reads the new
    HTML report (`Measured by the pack`); SQ-PG-02 spawn player G; SQ-PJL-02 reject narrowed to its @pass; SQ-PJL-05
    `.*item_tags=`. rerun.txt 202.
  - BF-FIX SQ-PS15B-13 (orchestrator): salt 7 + 2 layers is a salt block per blockrunner.md "Full block" (9 layers = 1
    block, SaltLayerBlock.settle), not 8 + 1 -> check salt:salt_block with air above. Lint clean, rerun.txt 203.
  - Side observation (SQ-PF-03 HTML): Creative Common `ItemMerges$1#get` waited 12.6 s on chunk loads during teleports.
  - Process note: judge-7's batch 1 edit of zlbs_probe.js was refused by the auto-mode permission check (outside its
    .qb-only brief); the orchestrator then made the same edit under its own permission (rule 2 allows tools/). Flagged to
    the owner.
- 07:18 step 95 (RDX, s94) boot hit the known Rhino tag-load flake: "Failed to load datapacks", ArrayIndexOutOfBounds in
  Rhino Interpreter.getSourcePositionFromStack from KubeJS TagLoaderKJS.customTags console.info (gotchas.md "Boot and
  shutdown"; third-party race, not our jars). The driver waited 600 s, `stop` could not end the idle JVM, step 96
  refused to boot beside it and the driver ended 07:38 (94/182 done). Environment fault; fix in
  tools/scratch_query/qdrive.py: wait_ready returns at once on "Failed to load datapacks"; after every stop
  `kill_own_jvm` SIGKILLs a JVM whose cwd is the step's own srv/ (logged); `boot_and_run` reboots once when the log shows
  the Rhino flake (log kept as srv/logs/rhino_flake_stepNN.log). Tested on the hung step 95 JVM (detected, killed);
  check_all --fast --changed: PROC-28 drive PASS, PROC-20 needed status_index (done). Resumed 07:45, step 95 booted
  07:47 (12 rows). Rows affected: none (step 95 and 96 had not started rows).
- 08:2x judge-7 batch 3 (steps 85 to 94, helpers judge-7b RD, -7c PSU, -7d PWG): 85 PASS 1 BF 1; 86 PSU A PASS 26 BF 8 FAIL 5
  HARNESS 1; 87 PASS 1 BF 1; 88 PTH INCONCLUSIVE 2; 89 PWG PASS 9 BF 3; 90 RB BF 1 INC 1; 91 RC PASS 2 BF 1; 92 RD A PASS 113
  BF 27 FAIL 8 INC 2; 93 RD B PASS 5 BF 1; 94 RDB BF 1. rerun.txt 236 ids.
  - Real FAILs: SQ-CORN-02 corncob mills/crushes to 3 kernels + 50% cornmeal, docs say 4 (creativedelight/recipes.js:61,
    immersiveengineering/recipes.js:29); SQ-FDB-11 `/loot mine` pumpkin with a knife drops nothing (farmersdelight/
    loot.js:17, creativeloot 09_drops.js T.swapDrop; slices 8 in code vs 4 in docs); SQ-LE-03 dispensed Swiftness potion
    bucket makes no cloud (creativebrewing PotionBucketDispenser / potion_area.js; row does not read the dispenser);
    SQ-RD-17 eaten honey pancake stack drops 5 pancakes + bowl, rustic_delight.md 2.4 says nothing (code vs docs: owner
    ruling); SQ-RSP-07 remainderBuffer=true kettle bottles stay in slot 0 (KettleTankMixin ce$serverTick /
    ce$stowBottles); SQ-TI-00 client classes (ItemTransforms$TransformType, BlockEntityRendererProvider$Context) load on
    the dedicated server (BackpackSkin mixins in the common list of creativeequipment.mixins.json); SQ-VP-03 Cesar Salad
    cheese only, VTD-52 says garlic or cheese (veggiesdelight/cooking.js:40); SQ-VP-08 Pickling Jar sides differ from
    vintage_delight.md 4.0 (FermentingJarBlockEntity sidedHandler.forSide); SQ-PSD-01 empty-hand click on ripe hemp's
    bottom half does not take the top (CR-100; hemp_harvest.js or bot face, unproven); SQ-PST-03, -08, -10c, -11, -11b
    grass spreads the same in winter and spring (ratios 0.88 to 1.05, band 0.35 to 0.85; changelog 1.2.02, crops.md
    "Grasses"; creativeCalendar CropSeasons.refuseSpread / GrassSpreadSeasonMixin; -03 stays HARNESS).
  - BF-FIX SQ-PS15B-26 (count before the restore's reload), SQ-PSM-06 (restart boot-line WARN), SQ-RB-21 (order-free
    lookahead), SQ-WCD-07 (count box over 262144 -> two halves); PSU: SQ-FCB-01, -07, -08, SQ-PSF-02, -08, -09, SQ-PSW-00;
    PWG: SQ-PWG-11, -35 (6 wire types), -55 (P10 recipe files); RD: SQ-CDQ-00, SQ-CNS-02, SQ-CORN-07, SQ-FDB-02, -04, -06,
    SQ-FR-04, SQ-FF-02, SQ-RD-08, -19, SQ-RM-03, SQ-RSP-02, -05, -06, -11, -15, SQ-VTD4-07; SQ-RD-07, -15 fixed but left
    INCONCLUSIVE (reclassify takes only FAIL).
  - Not simple: SQ-PTH-29, -30 summoned mobs vanish in 2 to 4 s (peaceful explains only zombies); SQ-RB-22 (N/A per its
    look); SQ-RC-01 rdetail prints no inputs for IE arc furnace (devtools rdetail change); SQ-RDB-02 base is the
    2026-11-19 dump (needs /dumpall or a same-boot base); SQ-CDF-06 (probe getTool), SQ-CDQ-02 (@@prep remainderBuffer
    false), SQ-CDQ-03, SQ-FR-05 (own @modset), SQ-FDB-07 (villager becomes MCA), SQ-RSP-14 (kettle @@prep), SQ-VTD4-05
    (filled-jar fixture), SQ-VP-02, -06, -07, SQ-VTD4-08 (vintage_probe.js: removed cans and soups, i.vinegar,
    creativedelight fluid keys, explicit Rhino overloads; machineRoutes skip `/salting/`); SQ-PSD-04, -05 superseded by
    the azalea wood restore (retire or rewrite). SQ-PWG-12, -24, -46 carry the same stale 5-wire bootexpect and P7 rejects.
  - Doc gaps: nether_worldgen.md 47 live Overworld structures vs 43 scanned; merge-count looks say 24, now 49; kitchen.md
    2.9 says skillet contents go to the inventory, the code drops them at the block.
- 08:5x judge-8 (steps 95 to 109, helpers judge-8b RFX, -8c RP): 95 RDX PASS 10 BF 1 OWNER 1 (SQ-RDX-02 client JEI half);
  96 RE PASS 3 BF 1; 97 RFX PASS 4 FAIL 1; 98 PASS 4 BF 2; 99 PASS 1 BF 1 HARNESS 1; 100 FAIL 1; 101 FAIL 1; 102 PASS 1;
  103 BF 3; 104 RP PASS 2 BF 6; 105 SMK PASS 3 BF 1; 106 PASS 2 BF 2; 107 PASS 1 FAIL 2; 108 PASS 3; 109 PASS 3. rerun.txt 250.
  - Real FAIL, high impact: SQ-SMK-09, -11, SQ-RFX-02, -19: end_gear.js:55 "TypeError: redeclaration of var felsicIngot"
    since commit be78080c (Owner rows 1120 G4, 2026-11-21 00:26); NCH skips the namespace, 27 creativeequipment
    sequenced-assembly recipes (dragon, felsic, echo, air brake) missing, upstream dragonloot / felsic_gear recipes back.
    On every boot from s96 on: Equipment-recipe rows in later steps fail for this cause. Reported to the session that
    made the commit (Plan_owner_rows_1120); not fixed in-run.
  - Real FAILs: SQ-RFX-27 exposureextras:camera_bag unregistered (only leather / iron / netherite tiers), swap alias in
    exposureBackport extras swaps/owner_namespace.json unresolved, the stack is lost (owner: pick a target or the row
    is wrong); SQ-RFX-28 UK spellings of "behavior" and "color" in config comments (CommonConfig.java:104, CDConfig.java:447,
    CWGConfig.java:359).
  - BF-FIX SQ-RDX-06 salting recipe in the count -> `\*\d+ serializer=`; SQ-RE-31 PneumaticCraft `result=empty` -> bytype
    ids (crystal.js:38-39); SQ-SMK-06 package `.items.`; SQ-SMK-07 floor 1300 (1394 in the 2026-11-19 dump); SQ-RFX-09, -19
    name/tag-only CHANGED lines ignored; SQ-RFX-13 `data get entity ... Inventory`; SQ-RFX-24 `10 day`; SQ-RFX-30 own
    `/dumpall` before the diff; SQ-RFX-31, -32 `n=[1-9]`; SQ-RPI-02 world from level-name; SQ-RPI-06
    liquid_diet_log_manual_only; SQ-RPI-07 `pbot hunger` before each eat.
  - Not simple: SQ-SMK-01 (SwapReport logs only on ServerStoppingEvent, needs a check after stop); SQ-RPI-03, -04, -08
    (each needs its own reboot with config; lost batch prep, no @boot lines; -08 path still p9a/).
  - Tool gaps: rdetail / bytype print no result for PneumaticCraft recipes and no step fluid in sequenced assembly;
    qverdict cannot turn a HARNESS row into FAIL; qlint does not flag @commands that say restart/boot without @boot.
    Gotcha: dedicated-server dumps carry raw lang keys, so rows comparing names with the client pack dump see noise; a
    row that diffs against the pack dump runs its own `/dumpall`.
- 09:0x Plan_owner_rows_1120 session took the end_gear.js bug; it will report when the fixed creativeequipment jar is installed. Later steps pick up the new jar at their prep (prep_srv pins per step); queue SQ-SMK-09, -11, SQ-RFX-02, -19 for rerun then.
- 09:1x fixed creativeequipment installed by the other session (installed.csv 2026-11-21T08:50:13, commit 9f48f07f); SQ-SMK-09, -11, SQ-RFX-02, -19 added to rerun.txt. Steps prepped after 08:50 run on the fixed jar.
- 09:3x judge-8 batch 2 (steps 110 to 125): 110 to 112 PASS 3, 3, 5; 113 PASS 3 FAIL 1; 114 to 120 PASS 4, 4, 3, 4, 3, 3, 3;
  121 PASS 1; 122 PASS 2; 123 STN PASS 4 BF 1; 124 PASS 1; 125 empty. Steps 110 to 115 ran on the old end_gear.js jar, no
  Equipment-recipe row failed. rerun.txt 254.
  - FAIL (uncertain) SQ-SMK-31: a summoned NoAI moobloom is gone in under 4 s on the flat peaceful world (forceloaded;
    type registered). Same pattern as SQ-PTH-29, -30 (summoned foxes, bees, zombies gone in 2 to 4 s): possibly one
    shared despawn cause on scratch worlds (a join or tick remover) rather than three features. Look together before
    any fix; suspects Moobloom.java (creativeWorldgen mooblooms member) or another mod's join remover.
  - BF-FIX SQ-STN-04: Farmer's Respite 1.19-2.0 keeps kettle water in the `water_level` blockstate, not FluidTank NBT;
    x 80 z -60 not forceloaded -> forceload, `kettle[water_level=4]`, FluidTank dropped (600 from kettleBrewTicks).
  - PASS with caveats: SQ-STN-02 (extract rights read from the hopper result; `cdevtest inv face=` prints no flags),
    SQ-STN-07 (bowl already out at t=0), SQ-STD-01 (two bamboo blocks lack the `bamboo_` prefix; look wording).
- 10:3x judge-8 batch 3 (steps 126 to 139, helper judge-8d UPG): 126 TP BF 1; 127 TP BF 1; 128 empty; 129 TX PASS 4; 130 PASS 1;
  131 UPG PASS 9 BF 4; 132 UPG PASS 2 BF 1; 133 VN BF 1; 134 GFB PASS 1 FAIL 1; 135 LOOT PASS 1; 136 PSU B INCONCLUSIVE 1;
  137 RD A BF 1; 138 TER B INCONCLUSIVE 1; 139 WGP PASS 1. rerun.txt 257.
  - Vanishing summoned mobs explained (environment): vanilla ServerLevel discards Animal / WaterAnimal entities when
    spawn-animals=false, the prep default on many steps. SQ-SMK-31 reclassified BATCH-FAULT; SQ-UPG-10, -11 entity half
    the same; SQ-PTH-29, -30 probably. Not simple (section @@prep spawn-animals=true, shared). Tool follow-up: a qlint W
    for `summon` of an animal or water animal in a section without spawn-animals=true.
  - Real FAIL: SQ-GFB-02 holdGeneratedFallingBlocks=true on a normal world: after pregen, a 2400-tick warp and a save, 96
    falling_block entities and 37 gravel items remain (Pass near 0 / under 21); Creative Worldgen falling-block hold or
    pregen chunks that never tick again; row design unproven.
  - BF-FIX SQ-TP-41 macro id `tempad:default` (ModMacros.java:14); SQ-TP-42 chronometer capacity 0, setchr lines dropped
    (one generator ~67 chronons per 2400 ticks observed, A4 holds); SQ-VN-01 bone meal adds 2 to 5 stages, lattice placed
    at age 3; SQ-UPG-09 regex skips vanilla "Missing data pack more_mooblooms:" (not rerun until the switch fix).
  - Not simple: SQ-FN-03 needs group B (superflat has no river); SQ-UPG-06, -09, -11 config half: `qmodset.py switch`
    replaces srv/config with the pack's, deleting the boot-1 old-name tomls (tool fix: keep boot-1 files the pack lacks);
    SQ-UPG-14 own forceload collides with the fixture's, and Storage Delight absent on boot 1 (release_1202 filter drops
    repackedStorage-02, -01 not in deploy/latest/mods).
  - INCONCLUSIVE: SQ-PSM-09 (12000 ticks deep ocean: squid 0, glow squid 8, dolphin 0; `cdevperf mobcap` prints no
    per-category cap); SQ-TER-06 (no geode in the 128x128 sample).
  - PASS with caveats: SQ-WGP-07 (2:57 vs a 5:26 baseline measured with Distant Horizons on); SQ-HRU-01 (20 unkeyed classes
    ask about create:copycat_base; perf.md 13.3 entry for the doc owner).
- 11:36 owner wind-down: `qrun.py drive stop r1120nv`, stopped at step 147 (EFB group B) after row 3 of 9; no JVM left;
  RUNNER idle (driver release); perf session r1121nv stopped, report interrupted by the owner (not run).
- 12:0x judge-9 (steps 140 to 147): 140 INCONCLUSIVE 2 (SQ-CDM2-02, SQ-NEST-02); 141 BF 1; 142 INC 1 BF 1; 143 CWG PASS 2 BF 5
  FAIL 1; 144 BF 1; 145 BF 1; 146 PASS 2; 147 PASS 1 BF 1 INC 1, SQ-EFB-12 cut off by the stop (no verdict).
  - Real FAIL: SQ-CWG-10 `build_deploy.py --dry-run` exits 1 at preflight: no KUBEJS_CLASSES rule covers
    kubejs/generation_scripts/worldgen/misc_block_swaps.json and vanilla_nether_noise_settings.json.
  - BF-FIX SQ-DNR-O-04 knife for scavenging_elder_eye; SQ-DNR-E-02 locate from 1200 64 0 (search timeout); SQ-CWG-03
    unchanged files reported as unchanged; SQ-CWG-06 place the meteorite pyramid in a located desert; SQ-CWG-09
    difficulty easy around the summon; SQ-EFB-10 waitlog after each `cdevtest under` (qrerun cannot resolve its header,
    rerun with `--rows "SQ-EFB-10 (earth, the beard band)"`); SQ-CWG-08a forceload widened (needs spawn-animals). rerun.txt 263.
  - Not simple: SQ-NEST-01, SQ-CWG-08, -08a spawn-animals=false; SQ-BLC-02 deleting the server cache falls back to the
    jar copy (WorldgenFiles.existing), needs another way to force a solve; SQ-CWG-09 Forgiving Void never lifted the pbot
    in the End (fake players may skip its player tick, unproven); SQ-EFB-11 Chunky stalled at 32.68 % for 29 min while
    ticking (IOWorker "Failed to read chunk [-35, 37]"; a rerun needs a thread dump); SQ-CDM2-02, SQ-NEST-02, SQ-BLC-01
    and the sea grape half of SQ-DNR-O-04 prove nothing (scan size, biome edges).

## 8. Handover

State: drive r1120nv stopped and resumable at step 147 row 4 (`qrun.py drive resume r1120nv`, steps 147 to 182: worldgen
then long); RUNNER.md idle; perf session r1121nv closed, report not run (`perf_session.sh report r1121nv`, no `--prune`).
Product findings -> [plan_scratch_findings_1121.md](../plan_scratch_findings_1121.md) with owner rulings 2026-11-21.
Next scratch session, in order:
1. Resume r1120nv (36 steps left), judge as steps land.
2. Rerun drive: `python3 tools/scratch_query/qrerun.py perf/runs/r1120nv/rerun.txt`, then
   `qrun.py drive start r1120nvr --plan-json perf/runs/r1120nv/rerun.plan.json` (SQ-EFB-10 by hand, see above).
3. Row rewrite pass for the not-simple batch faults in section 7 (spawn-animals=true sections, @modset rows, qmodset switch
   keeps boot-1 config, qlint false positives, rows needing their own reboot).
4. Then the rest of the queue (owner, 2026-11-21): jar-changed rows, failed rows, never-run drafts.
Client rows were not run.

## 9. r1121q: rerun list, bundled (evening session)

Owner request (2026-11-21 17:00): execute the scratch queue, as many low-effort rows as possible, bundle tests when
possible. Scope taken: handover item 2, the r1120nv rerun list (`perf/runs/r1120nv/rerun.txt`, qrerun.py plan: 286 rows,
87 sections). Not taken: the 36 worldgen / long steps of r1120nv (handover item 1), client rows, drafts.

- Bundling: new `tools/scratch_query/qbundle.py` copies the plan rows of sections with no `@@prep` for their group,
  no `@@modset` / `@modset`, all on boot 1 and no failed-boot row into run batches
  `tools/scratch_query/batches/r1121q/bundle_A1..A4.qb`, `bundle_B1.qb` (at most 40 rows per boot, a section's rows
  never split, sections with a `/reload` last in a boot); the other sections run as normal drive steps. One drive,
  `--plan-json tools/scratch_query/batches/r1121q/all.plan.json`: 28 boots for 274 rows (99 unbundled).
- Dropped (tools/scratch_query/batches/r1121q/dropped.txt): SQ-HH-477, -494, -501 (boot-2 rows: they bring the 486
  boot-1 rows of HH section 2 along, about 3.6 h) and the worldgen rows SQ-CWG-03, -06, -09, SQ-DNR-O-04, SQ-DNR-E-02,
  SQ-FSD-02, SQ-FTW-02, SQ-HH-318, SQ-VH-12 (fresh worlds; not low effort).
- Jars: prep_srv.sh swaps in the build jars newer than `mods/` (Common 1.2.0+6da18f23.dirty-fa644e.2611211613, Compat,
  Calendar, Loot 6da18f23 2611211558/1618; `jarstamp.py pending` lists these 4 as built, not installed).
- Preflight: `check_all.py --fast` 3 FAIL, all lint on uncommitted work (comment_dates, spelling_code, tab_indent),
  none boot-relevant; disk 15 GB free.

### 9.1 Run notes

- 17:00 RUNNER idle, no game JVM. qprescreen on the rerun plan: 10 likely. Fixed (run default 11, row steps only):
  SQ-FDB-02, -04, -06 and SQ-RSP-05 read the s1home / rhome markers only the section setup row makes -> the setup lines
  copied into each row; SQ-CFS-02 placed blocks at 304 -60 310 unloaded -> forceload added. Left as false positives:
  SQ-LGM-11, SQ-PFS-03, SQ-ROLL-03, SQ-MG-21 (a bot spawn loads the chunk), SQ-DNR-E-02 (locate).
- 17:06 perf session r1121q opened; drive r1121q started on the unbundled plan (99 steps, 737 rows, ETA 1000 min) and
  stopped at once at step 1 to bundle (see above). SQ-JFP-02 (`@group same`) split from its fixture rows in the first
  bundling: qbundle.py now inherits the previous row's group and keeps a section whole when any of its rows is not plain.
- 17:18 drive r1121b started on tools/scratch_query/batches/r1121q/all.plan.json (36 steps, 277 rows, ETA 01:02); r1121q
  (unbundled) left stopped at step 1, not resumed.
- 17:40 step 1 (bundle A1) done; judge-1: PASS 29, BATCH-FAULT 9, INCONCLUSIVE 2, no real FAIL.
  - BF-FIX SQ-BBQ-03 bibimbap dropped (plan_wood_bucket_batch_1121.md R18) -> meal expect turned into a reject;
    SQ-CEN-05 TNT burst before the count at pace 2 -> pace 0.05 around the summon (SQ-SPF-13 pattern); SQ-CMP-13 c13a
    read past the wither line -> netherWitherTime 1000, tickwarp 1200; SQ-CDM2-03 typed count 285 -> read N (329) from
    comfort_nourishment.json; SQ-ROLL-12 can / slice rejects too wide (FD rice roll cans, Create sweet roll, S&D copy)
    -> scoped; SQ-VTDA-12 inventory swap merges at /give, relog line never appears -> expect dropped. All in
    perf/runs/r1121b/rerun.txt.
  - Not simple: SQ-BBQ-06, -07 `pbot use` hits y+1.0, above GrillBlockEntity.getBox (0.8125 to 0.99): needs a hit-point
    option on `pbot use` (tool change in Creative Devtools perfbot). SQ-GLD2-03 log `[golden values] 41` vs
    golden_food.md 3.6 "46 items": docs and code disagree, owner rules.
  - INCONCLUSIVE: SQ-CEQ-04 (rdetail prints no sequenced-assembly fluid amounts), SQ-CFR-05 (SPF-81 fixture campfire
    absent in a bundle boot).
  - Notes: SQ-BBQ-04 still expects compost bibimbap=1 (same R18 cause); SQ-CDFW-06 @pass text needs a refresh (fried egg
    items have no furnace recipe, food.md 3.7).
- 18:13 step 2 (bundle A2) done: PASS 7, FAIL 3, LOOK 30 raw; judge-2 on it. Step 3 (bundle A3) booting.
- 18:3x judge-2 (step 2, bundle A2): PASS 36, BATCH-FAULT 2, INCONCLUSIVE 1, FAIL 1.
  - Real FAIL SQ-GLD-03 milk half: after golden milk and enchanted milk the Speed is cured but the bot then reads
    `effects=minecraft:poison:0:92` (Poison I about 100 t), and hunger 18 -> 20 on finish (golden_food.md 3.1 "milk
    never eats"). ShinyItems.GoldenMilk / Milk only cure; suspect a use-finish handler or the drink food rules
    (food_types.js). TODO food-tuning #31.
  - BF-FIX SQ-FF-04 cdevtest prints `modifier=1` -> regex `modifier=1(\.0)? saturation=`; SQ-PCK-02 waffle moved to
    `food_family/baked/misc` (owner 2026-11-20, food_families.md 4.9) -> row queries and expects that tag.
  - INCONCLUSIVE SQ-FO-08: no cachalot kill step, the blubber drop is untested (rewrite pass). PASS caveats: SQ-FO-04 cook
    times not printed by rdetail; SQ-JFP-05 look text says 3 cookies, setup inserts 1.
- 18:36 step 3 (bundle A3) done; judge-3: PASS 23, BATCH-FAULT 9, FAIL 4, INCONCLUSIVE 3.
  - Real FAIL SQ-LGM-07: a full omurice_block (servings=4) drops the block and a bowl (bowl only when servings is not 4).
    SQ-POT-02: framed_flower_pot accepts melon, pumpkin, moss block, azalea leaves, hamimelon; hanging_flower_pot accepts
    0 of 555 (as r1120pot; suspect SuppFlowerBoxPlantsMixin / framed pot mixin, or the probe face). SQ-POT-04: vanilla
    pot with gossypium does not return the plant. SQ-POT-04v: potted_gossypium / potted_hemp break drops nothing
    (pack loot tables in kubejs/data not applied?).
  - BF-FIX SQ-LBS-04 hosts from other boots -> `zlbs labels` / `centre` first; SQ-LGM-08 pot contents -> recipes.js
    inputs; SQ-RFX-09, -19 Spice of Life values changed on purpose (food diversity 2026-11-21) -> filter ignores sol;
    SQ-ROLL-03 peaceful refills hunger -> difficulty easy around the eat.
  - Environment: server thread stalled 18:21:11 to 18:28:11 at SQ-PS15B-13's `forceload add 1290 1290 1330 1310` (chunk
    generation suspected, no thread dump); collateral SQ-PS15B-13, -26, SQ-RB-21, SQ-RE-31 BATCH-FAULT, rerun. A second
    stall on PS15B-13 = stop and jstack.
  - INCONCLUSIVE SQ-RFX-02, -19, -30: scratch dumps differ from the pack dump by 373 recipe and 142 tag lines (client
    mods, oceansdelight, IE sawmill, powergrid; baseline 2026-11-17 looks stale: owner refreshes the baseline);
    SQ-RFX-24 /wine time prints bare lang keys, no values (needs a value probe).
  - Side effect: SQ-POT-05's shell rewrites feature_writeup/flower_pots_matrix.md (no net change in git).
- 18:47 step 4 (bundle A4) done: PASS 10, LOOK 9, FAIL 2 raw; judge-4 on it. Step 5 (bundle B1) started as group A.
  - Tool fault: qdrive.build_steps read row metadata only from rows/*.qb, so every bundle row took the default group A
    and bundle B1's three group B rows (SQ-ABD-01, -04, SQ-VTDA-10) run on the superflat. Fix in qdrive.py: also load
    the files the plan names. Those three rows go to the rerun; bundles A1 to A4 unaffected (all group A, no modset).
- 19:0x judge-4 (step 4, bundle A4): PASS 19, BATCH-FAULT 2, no real FAIL.
  - BF-FIX SQ-CUP-04 reject matched another section's /reload recipe warning -> anchored on MinecraftServer lines;
    SQ-TP-41 replies 40 ticks late at pace 2 -> macro / cd / doors under pace 0. **Owner check:** the same fix changed
    `doors total 1` to `2` from the observed output (one macro opens a linked pair); no writeup states the count.
  - Weak evidence on PASS: SQ-VDF-09 can honey ingredient from can_types.js only; SQ-VDF-07 server half only.
- 19:4x judge-5 (steps 6 to 20, 23 rows): PASS 20, BATCH-FAULT 3, no real FAIL.
  - BF-FIX SQ-BM-02 Cozy's Improved Cats replaces the summoned cat (tag lost) -> select by type and box; SQ-JSL-02
    `Slot: 8` prints without b -> `Slot: 8b?,`; SQ-MG-24 surviving rope recipe is supplementaries:rope_2 -> `rope(?:_2)?`.
  - Finding (not a row fault): 10 KubeJS server ERRORs "Failed to create custom JSON recipe" from creativedevtools
    sequence_assembly_processes.js#58 (createaddition, vintageimprovements types) in every CCO-12 set; TODO at write-back.
- 20:4x judge-6 (steps 21 to 27, 64 rows): PASS 58, BATCH-FAULT 4, FAIL 2.
  - Real FAIL SQ-HH-1232: `litter minecraft:pig x300 truffle: babies 623, extra 323`, row wants 864 to 963 (plain pigs
    SQ-HH-1230 extra 289; r1120nv on the old code 550); truffleLitterBonus=1 in the srv toml, CD jar newer than the
    2026-11-21 13:49 commit. Suspect hearth AnimalEvents.java 103-107 or fungi ModEntitiesEvents.onBabySpawn.
    SQ-HH-1608: iron watering can 2x2 soaks (561,-61,576) but not the clicked column (560,-61,575); 3x3 / 4x4 pass.
    Suspect WateringCanItem area code (~295) or the pbot hit point; unproven.
  - BF-FIX SQ-HH-1326 needs SQ-HH-1324's toml edits and restart -> added; SQ-HH-1511 summon after the watch -> moved
    before; SQ-HH-1607 can level inherited from -1606 -> own item replace, forceload, bot; SQ-HH-227 waitlog ticks=400 vs
    warp 600 -> 600 (cow count 1 of 3 may remain); SQ-MBX-06 judged PASS, Speed control check added and queued.
  - Row text stale (no rerun): SQ-GAP-05 egg floats by design (physics/floats) -> row text fixed; SQ-FFH-02 @pass says
    muffins have pot recipes (oven only since 2026-10-29); SQ-MBX-05 hardness 50 in the row vs 25 in changelog 1.1.05
    (owner settles).
- 21:18 drive r1121b 36/36 steps done; no JVM left; RUNNER idle (driver release). judge-7 on steps 28 to 36; the rerun
  drive r1121br (perf/runs/r1121b/rerun.txt) starts after it.
- 21:2x judge-7 (steps 28 to 36, 47 rows): PASS 39, FAIL 4, BATCH-FAULT 4. The copied marker setups in SQ-FDB-02, -04,
  -06 and SQ-RSP-05 worked.
  - Real FAIL SQ-PSF-09: well Amount 51000 before and after the can use, FillTick 163 -> 135: the drain does not reach
    the tank (r1120nv same shape; H&H well / can handler in the CD hearthandharvest member). SQ-PST-11b: grass over
    mycelium winter/spring 0.20, limit 0.35 to 0.85 (r1120nv 0.88, r1102b 0.13: unstable measure; IWGrassSpreadSeasonMixin
    or the count). SQ-FDB-06: Rich Soil holds, but the vanilla control farmland is not trampled either (golem 6-block
    drop, mobGriefing true; crops.md says farmland tramples; cause not found). SQ-RSP-02: 95 kettle recipes, row expects
    84 from the 2026-11-19 dump (owner: stale dump or 11 new recipes).
  - BF-FIX SQ-PSM-06 reject hit a conditional recipe WARN after the restart -> `^(?!.*Error parsing recipe)` prefix;
    SQ-RD-08 reject hit the pre-loop kill on an empty area -> `execute if entity` guard; SQ-TI-00 bootreject hit another
    mod's optional mixin WARN -> `[mixin/]: Error loading class` excluded.
  - Not simple: SQ-VP-06 tools/vintage_probe/vintage_probe.js OLD_SOUPS still lists the 6 goldenfood stews removed
    2026-11-10 (shared probe; drop them and the row's 32 soups -> 26 in a rewrite pass).
  - Row text stale: SQ-PSW-00 "47" structures, the scan prints 43.
- 21:30 rerun drive r1121br: perf/runs/r1121b/rerun.txt (33 ids) plus SQ-BM-01 (forceload fixture for SQ-BM-02),
  bundled by qbundle.py into tools/scratch_query/batches/r1121br/ (bundle A1 19 rows, B1 3 rows) plus 7 sections: 9
  steps, 34 rows, ETA 23:23. The group fix in qdrive.py is in this drive (bundle B1 resolves to group B).
