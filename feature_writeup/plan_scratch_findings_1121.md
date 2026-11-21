# Plan: scratch findings 2026-11-21 (r1120nv)

<!-- generated:status_index begin open_work sha1=8c8251b0a719 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 10 open TODO rows (Bug 1, Testing 9), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `jars #15` | Bug | SQ-JFP-01 SQ-JFP-02 SQ-JFP-03 SQ-JFP-04 SQ-JFP-05 | Create pump moves no fluid between a tank and a jar |
| `camera-bags #5` | Testing | SQ-RFX-27 | Old plain camera bag lost on load |
| `creative-calendar #1` | Testing | SQ-PST-03 SQ-PST-08 SQ-PST-10c SQ-PST-11 SQ-PST-11b | Grass spreads as fast in winter as in spring |
| `largecontent/mod-additions #52` | Testing |  | Hearth and Harvest: pigs get both Quark's and H&H's litters |
| `largecontent/mod-additions #53` | Testing |  | Hearth and Harvest foods lose Nourishment |
| `mod-integration #2` | Testing | SQ-TI-00 | Backpack skin mixins load client classes on the dedicated server |
| `rustic-delight-member #1` | Testing | SQ-RD-17 | Pancake stack drops: doc says nothing drops |
| `tnt-and-portal #40` | Testing | SQ-PRT-01 | Lightning-lit portal broken by Immersive Weathering's crying obsidian rule |
| `vintage-delight-port #1` | Testing | SQ-HOP-12 | Organic mash consumes extra items on each use |
| `watering-and-soil #1` | Testing | SQ-HH-1606 SQ-HH-1607 SQ-HH-1608 SQ-HH-1609 SQ-HH-1610 SQ-HH-1611 | Watering can does nothing on ripe crops (Quark Simple Harvest takes the click) |
<!-- generated:status_index end open_work -->

Source: the real FAILs of scratch run r1120nv (resume 2026-11-21, steps 58 to 147), listed to the owner as "Product
findings worth your attention"; owner asked for a fix plan (chat 2026-11-21). Evidence per row:
[scratch_run_2026-11-21.md](testing/scratch_run_2026-11-21.md) section 7 and `perf/runs/r1120nv/s*_res/verdicts/`.
TODO rows: see the Rows column; status lives in the rows.

## 1. Intake

| # | Item | Class | Premise check | Rows |
|---|---|---|---|---|
| F0 | Creative Equipment end_gear.js "redeclaration of var felsicIngot" drops 27 sequenced-assembly recipes (SQ-SMK-09, -11, SQ-RFX-02, -19) | premise | Already fixed by the Plan_owner_rows_1120 session (commit 9f48f07f, installed 2026-11-21 08:50); reruns queued in perf/runs/r1120nv/rerun.txt | none (rerun only) |
| F1 | Client classes load on the dedicated server (SQ-TI-00: ItemTransforms$TransformType, BlockEntityRendererProvider$Context) | clear | creativeequipment.mixins.json lists BackpackSkinItemRendererMixin (target BackpackItemStackRenderer) and BackpackSkinBakedModelMixin (target client.render.BackpackDynamicModel$BackpackBakedModel) under `"mixins"` (lines 11, 12); both reference client classes. BackpackSkinBlockEntityMixin has 1 client reference, check it too | `mod-integration #2` |
| F2 | Grass spreads at the same rate in winter and spring (SQ-PST-03, -08, -10c, -11, -11b; ratios 0.88 to 1.05, band 0.35 to 0.85) | clear | changelog 1.2.02 and crops.md "Grasses" promise slower winter spread; hooks are creativeCalendar mixin/GrassSpreadSeasonMixin.java, IWGrassSpreadSeasonMixin.java, seasons/CropSeasons.java (refuseSpread), GrassSeasonHook.java. Cause not yet proven (mixin not applied, wrong season read, or the swapped-in Calendar build) | `creative-calendar #1` |
| F3 | Watering can does nothing on ripe wheat on dry farmland (SQ-HH-1607 to -1611); wheat age 0 vanished on use (SQ-HH-1606, unproven) | clear | HNH-33 says a ripe crop is soaked only. Suspect Quark Simple Harvest (on in config/quark-common.toml) takes the right-click before hearth item/WateringCanItem.java:176 useOn; gear/QuarkCompat.java exists | `watering-and-soil #1` |
| F4 | Organic mash on an age-1 hops vine uses two items and jumps to age 3 (SQ-HOP-12) | clear | hearth block/HopsVineBlock.java:75 passes any BoneMealItem to its own useOn; vintage item/custom/OrganicMashItem.java:28-34 calls BoneMealItem.applyBonemeal 3 times (each success already shrinks the stack) and then shrinks once more | `vintage-delight-port #1` |
| F5 | Hearth and Harvest foods lose Nourishment (SQ-HH-1106, -1109, -1110, -1111) | premise | TODO `largecontent/mod-additions #53` is status Complete but its work field says Open and the rows still fail: reopen. Files tools/gen_comfort_nourishment.py, kubejs/generation_scripts/food/comfort_nourishment.json (no H&H rows) | `largecontent/mod-additions #53` |
| F6 | Pigs get both Quark's Pig Litters and H&H's litter (SQ-HH-1230, -1232, -1326) | premise + open | TODO `largecontent/mod-additions #52` Complete but failing: reopen. Litter code hearth event/AnimalEvents.java, config HHConfig.java | `largecontent/mod-additions #52` |
| F7 | Create pump moves no fluid between a tank and a jar (SQ-JFP-01 to -04; SQ-JFP-05 INCONCLUSIVE) | clear | creativeCompat compat/JarFluidPipes.java (fill, canInteractWithSoftFluidTank); the row layout may lack a pipe segment: prove which first | `jars #15` |
| F8 | Partly eaten pancake stack drops 5 pancakes + bowl; rustic_delight.md 2.4 says nothing (SQ-RD-17) | open | code and doc disagree | `rustic-delight-member #1` |
| F9 | Old plain camera bag is lost on load (SQ-RFX-27) | open | exposureBackport extras swaps/owner_namespace.json:10 maps `exposure_extras:camera_bag` to `exposureextras:camera_bag`, which is not registered (only leather, iron, netherite tiers, CameraBagTier.java); log "[swaps] unresolved exposure_extras:camera_bag: 1" | `camera-bags #5` |
| F10 | Lightning on a portal frame turns a bottom obsidian into crying obsidian, so the lightning-lit portal never forms (SQ-PRT-01) | open | Immersive Weathering `crying_obsidian_from_lightning` vs creativeMagic PortalFailureHandler; chance-based (r1114 passed) | `tnt-and-portal #40` |

## 2. Owner rulings

| # | Item | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | F8 pancake stack drops | open | Drop what is left: keep the code (remaining pancakes and the bowl), fix rustic_delight.md 2.4 | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R2 | F9 old plain camera bag | open | Map `exposure_extras:camera_bag` to `exposureextras:leather_camera_bag`; contents move with it | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R3 | F10 portal frame vs IW lightning | open | Portal lighting wins: skip the crying obsidian change when the struck block is part of a valid portal frame | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R4 | F6 pig litters | open | H&H litter only, Quark Pig Litters off; average litter 2, range 1 to 3 | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R5 | F4 organic mash strength | clear | Keep upstream's 3 bone-meal applications per use, consume exactly 1 mash | 2026-11-21 | agent default, unconfirmed |
| R6 | F1 mixin side | clear | Move the client-only BackpackSkin mixins to the `"client"` list; keep in common only what touches server classes | 2026-11-21 | agent default, unconfirmed |
| R7 | F5 H&H Nourishment | open | Pack rule stands for Hearth and Harvest foods; rows rewritten to the rule's result | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R8 | F5 drift of 29 non-H&H foods on rerun | open | Hold them back: jellies never give Comfort; every existing food's current treatment is pinned (comfort_nourishment_pins.csv) | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R9 | F6 rabbits | open | Rabbits get the same litters as pigs (1 to 3, average 2); truffle pigs roll twice, keep the higher | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R10 | F4 mash on hops | clear | On a hops vine Organic Mash grows one age like the other fertilizers (hops rule, SQ-HOP-12, changelog); 3 applications elsewhere (R5) | 2026-11-21 | agent default, unconfirmed |

## 3. Work, in order

1. F1 (high: server stability). Creative Equipment: move BackpackSkinItemRendererMixin and BackpackSkinBakedModelMixin to
   `"client"`, check BackpackSkinBlockEntityMixin's client reference (split if it needs both sides). Verify: dedicated
   server boot logs no client class load (SQ-TI-00 rerun), client backpack skins still render (CQ row).
2. F6 (R4). Quark Pig Litters off in config/quark-common.toml (plus configs_edited copy); H&H litter tuned to average 2,
   range 1 to 3 (HHConfig.java / AnimalEvents.java). Verify SQ-HH-1230, -1232, -1326; reopen and close #52.
3. F5. Add H&H foods to the Nourishment generator (gen_comfort_nourishment.py), regenerate, Creative Common rebuild if the
   output lands in a dict. Verify SQ-HH-1106, -1109, -1110, -1111; reopen and close #53.
4. F3. Watering can before Quark Simple Harvest on ripe crops (QuarkCompat or an event priority), per HNH-33; then prove
   SQ-HH-1606 (age-0 wheat vanished) by hand. Verify SQ-HH-1606 to -1611.
5. F4 (R5). OrganicMashItem: apply bone meal on a copy (or count successes) so one use consumes one mash. Verify SQ-HOP-12.
6. F2. Creative Calendar grass spread: prove why winter spread is unchanged (mixin applied? season read?), fix, verify
   SQ-PST-03, -08, -10c, -11, -11b.
7. F7. Jar pumping: first a hand or row check with a pipe segment between pump and jar; if still nothing moves, fix
   JarFluidPipes. Verify SQ-JFP-01 to -05.
8. F10 (R3). Portal-frame guard on IW's lightning rule (Creative Magic or Compat). Verify SQ-PRT-01.
9. F9 (R2). Swap alias to leather_camera_bag. Verify SQ-RFX-27 on an old-id save.
10. F8 (R1). rustic_delight.md 2.4 doc fix; SQ-RD-17 rewritten to the ruling.

Each code change: changelog bullet (player-visible), writeup and test list, rerun rows queued.

## 4. Built 2026-11-21

| # | Change | Jar | Rows to run |
|---|---|---|---|
| F1 | BackpackSkinItemRenderer, BakedModel and Worn mixins to the `client` list (BlockEntity mixin stays common: Forge `ModelData` only) | Creative Equipment | SQ-TI-00, CQ-CEQ-02 (new) |
| F2 | Cause: `refuseSpread` read the season at the grass block, which never sees the sky; below Serene Seasons `underground_fertility_level` 32 (scratch pad y -61) every season was neutral. Now reads one block up | Creative Calendar | SQ-PST-03, -08, -10c, -11, -11b |
| F3 | `QuarkCompat` cancels Quark's SimpleHarvestEvent (right-click) when the hand holds a watering can with water; the run had logged `use CONSUME`, Quark's result. SQ-HH-1606 passed because the clicked wheat was already gone (2 of 25 missing before the use): still to prove by hand | Creative Delight | SQ-HH-1606 to -1611 |
| F4 | Organic Mash applies bone meal on a copy, one mash per use (R5); hops vine grows one age for it (R10) | Creative Delight | SQ-HOP-12 |
| F5 | Generator: pins (R8) and jelly rule; rerun adds H&H only (3 Comfort 240 s, hops salad none, 2 kept, 15 bottles stripped) | data (instance file, CD jar copy) | SQ-HH-1106, -1109, -1110, -1111 (rewritten) |
| F6 | Quark Pig Litters off; H&H `litterMaxExtra` 2, `truffleLitterBoost` (R4, R9) | Creative Delight, configs | SQ-HH-1230, -1231, -1232 (rewritten), -1326 |
| F7 | No defect found by reading; diagnostic rows with a pipe segment and a tank-to-tank control | none | SQ-JFP-01b, -01c (new), then -01 to -05 |
| F8 | rustic_delight.md 2.4 states the built loot | none | SQ-RD-17 (rewritten) |
| F9 | `exposure_extras:camera_bag` -> `exposureextras:leather_camera_bag` | exposureBackport | SQ-RFX-27 (rewritten) |
| F10 | Creative Magic `IWLightningPortalFrameMixin` + `PortalFrameGuard`: no IW growth on frame obsidian | Creative Magic | SQ-PRT-01 |

## 5. Other real FAILs from the same run (not in this plan; take in at a later intake)

SQ-HH-228 (no manure via trough feed), -227 (no calf, uncertain), -1292 (Prickly too strong, uncertain), -1273 (young
hemp gives straw), -262 (3-block drop no stomp); SQ-VTD8-03 (fermenting jar stops after one batch); SQ-CORN-02 (corncob 3
kernels, docs 4); SQ-FDB-11 (pumpkin `/loot mine` with a knife drops nothing); SQ-LE-03 (dispensed potion bucket, no
cloud); SQ-RSP-07 (kettle bottles not buffered); SQ-VP-03 (Cesar Salad garlic); SQ-VP-08 (Pickling Jar sides); SQ-PSD-01
(hemp bottom-half click); SQ-RFX-28 (UK spellings in three config comments); SQ-GFB-02 (falling blocks after pregen);
SQ-CWG-10 (build_deploy KUBEJS_CLASSES misses two worldgen files). Details: run doc section 7.
