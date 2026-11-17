# Test and id prefix index

The single list of every id prefix in the repo docs: who owns it, what kind of id it is, and which prefixes were renamed.
Built 2026-11-16 from a scan of `feature_writeup/` (defining lines `**PREFIX-NN**` and table rows `| PREFIX-NN |`), the
README "Test prefix" column and the scratch queue's row ids. Rules: [design_rules.md](../design_rules.md) DR-W3, DR-W3b.
Plan: [plan_rules_fixes.md](../plan_rules_fixes.md) section 2.

## 0. Making a new test or a new prefix

1. Look the feature up in section 3. If it has a prefix, use it: writeup tests `PREFIX-NN`, server queue rows
   `SQ-PREFIX-NN`, client queue rows `CQ-PREFIX-NN`. Continue the numbering from the highest id in the owner doc.
2. A new feature gets a new prefix: 3 or more capital letters (digits allowed after the first letter), not in this
   file, and with no hits from `grep -rn "PREFIX-[0-9]" feature_writeup`. Add its row to section 3 **before** writing a test that uses it, then
   add the same prefix to the feature's README row.
3. One prefix, one owner doc. Other docs (plans, briefs, the queue, run docs) may quote an owner's ids but never define
   new ones under it. A plan that drafts tests for a writeup uses the writeup's prefix and the tests move into the
   writeup when it is built.
4. Never reuse a retired prefix (section 2) for another feature.
5. `python3 tools/test_prefix_lint.py` must pass after the edit (DR-W3b).

## 1. Prefix types

| Type | Form | Owner | Scope | Notes |
|---|---|---|---|---|
| Writeup test | `PREFIX-NN` (`EN-07`) | the writeup in section 3 | global, indexed here | defined as checkbox lines in the owner's Tests section |
| Server queue row | `SQ-PREFIX-NN` | [scratch_queue.md](scratch_queue.md) | global, indexed here | PREFIX is the feature's writeup prefix where one exists |
| Client queue row | `CQ-PREFIX-NN` | [scratch_queue.md](scratch_queue.md) | global, indexed here | as above |
| Structure test series | `ST-NN`, `LO-NN` in `testing/structures/` | [structure_tuning.md](../structure_tuning.md), [locate.md](../locate.md) | global | the phase files and `.scratch.md` mirrors quote, do not own |
| Design rule | `DR-L`, `DR-W`, `DR-C`, `DR-P`, `DR-A` + number | [design_rules.md](../design_rules.md) | global | new rule letters (`DR-W3b`) slot in without renumbering |
| Rule violation | `V-NN` | [design_rules.md](../design_rules.md) section 9 | global | fix plans quote them |
| Rule gap | `GNN` (no hyphen: `G13`) | [design_rules.md](../design_rules.md) section 7 | global | |
| TODO row | `family/sub #N` | [todo/TODO.md](../todo/TODO.md) | global | not a test id |
| Decision, bug, phase, step in one doc | `D1`, `B1`, `P1`, `E1`, `R1` (no hyphen) | the doc that writes it | doc-local | always cite with the doc name ("plan_rules_fixes D7"); never indexed |
| Release check | `R4-NN` and similar in `plan_1.x.md` | the release plan | doc-local | |
| Run result | none: run docs quote queue ids | `testing/scratch_run_<date>.md` | | |

## 2. Renamed prefixes (collisions found 2026-11-16)

Each pair below defined tests under one prefix in two docs. The older doc (first git commit) keeps the prefix; the
newer gets a new one. `tools/test_prefix_rename.py` (plan section 2) applied these on 2026-11-17 (writeups,
plans, TODO rows, README, briefs, skills, queue rows; run docs keep the old id with "(now X)" at the first quote).
The rows files keep their names (`EN.qb` holds `SQ-EDL-` rows). Queue rows are renamed by section heading, because the same queue prefix covers two features.

| Old | Doc (first commit) | New | Kept by | Queue rows renamed |
|---|---|---|---|---|
| `EN-` | [ends_delight.md](../ends_delight.md) (2026-10-25) | `EDL-` | [engineering.md](../engineering.md) (2026-10-18) | `SQ-EN-01` to `-08` in "End's and Ender's Delight, 2026-10-25" and the results in "2026-10-26 run" to `SQ-EDL-` |
| `EN-` | [briefs/potions_enchanting.md](../briefs/potions_enchanting.md) | `ENC-` | engineering.md | `SQ-EN-01`, `-02` in "Bug batch, 2026-11-01" to `SQ-ENC-` |
| `LT-` | [loading_tips.md](../loading_tips.md) (2026-11-07), [loading_tips_audit.md](../loading_tips_audit.md), [plan_loading_tips_screens.md](../plan_loading_tips_screens.md) | `LTP-` | [loot.md](../loot.md) (2026-09-27) | `CQ-LT-` to `CQ-LTP-` |
| `PB-` | [perf_suite.md](../perf_suite.md) (2026-10-28) | `PFS-` | [potion_buckets.md](../potion_buckets.md) (2026-10-18) | `SQ-PB-` to `SQ-PFS-` (all rows are the perf suite) |
| `BP-` | [botany_pots.md](../botany_pots.md) (2026-11-08) | `BOT-` | [backpack_skins.md](../backpack_skins.md) (2026-10-23) | none: its queue rows already use `SQ-BOT-` |
| `RP-` | [perf_rpack.md](../perf_rpack.md) (2026-10-31) | `RPB-` | [recipe_progression.md](../recipe_progression.md) (2026-10-18) | `CQ-RP-01` to `-03` in "Resource pack bench, 2026-10-29" to `CQ-RPB-` |
| `RP-` | [raspberry_pie.md](../raspberry_pie.md) (2026-11-11, README cell) | `RPI-` | recipe_progression.md | `SQ-RP-`, and `CQ-RP-` in the two Raspberry Pie sections, to `SQ-RPI-`, `CQ-RPI-` |
| `CN-` | [corn_delight.md](../corn_delight.md) (2026-10-25, README cell) | `CORN-` | [cans.md](../cans.md) (2026-10-18) | `SQ-CN-`, `CQ-CN-` (all Corn Delight) to `SQ-CORN-`, `CQ-CORN-` |
| `IP-` | [immersive_portals.md](../immersive_portals.md) (2026-11-07, README cell) | `PRT-` | [item_physics.md](../item_physics.md) (2026-10-18) | `SQ-IP-01` to `-05`, `CQ-IP-` in "SQ-IP Immersive Portals changes" to `SQ-PRT-`, `CQ-PRT-`; `SQ-IP-15`, `-16` stay |
| `TS-` | [trade_sync.md](../trade_sync.md) (2026-11-06) | `TSY-` | [trashslot.md](../trashslot.md) (2026-10-02) | none: already `SQ-TSY-`, `CQ-TSY-` |
| `PWG-` | [energy_advanced.md](../energy_advanced.md) README cell | none | [power_grid.md](../power_grid.md) | none: energy_advanced.md has no tests of its own; its README cell becomes "tests in power_grid.md" |
| `NP-`, `RA-` | [creative_magic.md](../creative_magic.md) README cell | none | [nether_portal.md](../nether_portal.md), [recipes_magic.md](../recipes_magic.md) | none: README cell becomes a reference |

Run docs that quote an old id in a past result keep it and get "(now X)" once, at the first quote.

## 3. Writeup test prefixes

The queue column counts rows in [scratch_queue.md](scratch_queue.md) per queue prefix. "Seed or reference docs" quote or drafted the
ids (briefs, plans); they do not own them. Rows marked "not in the README index yet" get a README cell in the
same pass as the renames.

| Prefix | Owner doc | Queue prefixes (rows) | Seed or reference docs | Note |
|---|---|---|---|---|
| `ABD-` | [abnormals_delight.md](../abnormals_delight.md) | CQ-ABD (1), SQ-ABD (5) |  |  |
| `AE-` | [armor_effects.md](../armor_effects.md) |  | briefs/potions_effects.md |  |
| `AL-` | [alcohol.md](../alcohol.md) |  | briefs/alcohol_brewinport.md |  |
| `AR-` | [archery.md](../archery.md) |  |  |  |
| `AV-` | [advancements.md](../advancements.md) |  |  |  |
| `BBQ-` | [barbeques_delight.md](../barbeques_delight.md) | SQ-BBQ (8) | plan_meals_rolls_bbq.md |  |
| `BM-` | [baby_mobs.md](../baby_mobs.md) | CQ-BM (7), SQ-BM (2) |  |  |
| `BN-` | [bee_nests.md](../bee_nests.md) |  |  |  |
| `BOT-` | [botany_pots.md](../botany_pots.md) | SQ-BOT (4) |  | new 2026-11-16, was BP- (collided with backpack_skins.md); matches SQ-BOT |
| `BP-` | [backpack_skins.md](../backpack_skins.md) |  |  |  |
| `BR-` | [blockrunner.md](../blockrunner.md) |  | briefs/gameplay_blockrunner.md |  |
| `CAL-` | [creative_calendar.md](../creative_calendar.md) | CQ-CAL (1), SQ-CAL (7) |  |  |
| `CB-` | [camera_bags.md](../camera_bags.md) |  |  |  |
| `CBR-` | [creative_brewing.md](../creative_brewing.md) |  |  |  |
| `CC-` | [client_capture.md](../client_capture.md) | CQ-CC (6), SQ-CC (16) |  |  |
| `CDM-` | [creative_delight.md](../creative_delight.md) | SQ-CDM (6) |  | not in the README index yet |
| `CDQ-` | [creative_delight.md](../creative_delight.md) | SQ-CDQ (9) |  |  |
| `CDT-` | [creative_delight.md](../creative_delight.md) | SQ-CDT (17) |  |  |
| `CDV-` | [creative_devtools.md](../creative_devtools.md) | SQ-CDV (4) |  |  |
| `CIC-` | [cozy_cats.md](../cozy_cats.md) |  |  |  |
| `CL-` | [changelog.md](../changelog.md) | CQ-CL (1), SQ-CL (1) |  |  |
| `CM-` | [composting.md](../composting.md) | SQ-CM (24) |  |  |
| `CMP-` | [creative_compat.md](../creative_compat.md) | SQ-CMP (8) |  |  |
| `CN-` | [cans.md](../cans.md) |  |  |  |
| `CNS-` | [comfort_nourishment.md](../comfort_nourishment.md) | CQ-CNS (1), SQ-CNS (8) |  |  |
| `CORN-` | [corn_delight.md](../corn_delight.md) | CQ-CORN (1), SQ-CORN (9) |  | new 2026-11-16, was CN- (README only; cans.md owns CN-); queue SQ-CN and CQ-CN become SQ-CORN, CQ-CORN |
| `CP-` | [can_parity.md](../can_parity.md) | SQ-CP (1) |  |  |
| `CR-` | [crops.md](../crops.md) |  | briefs/farming_crops.md, briefs/farming_wild-crops.md |  |
| `CRN-` | [archive/carnivore.md](../archive/carnivore.md) |  |  |  |
| `CS-` | [curios_slots.md](../curios_slots.md) | CQ-CS (1), SQ-CS (8) |  |  |
| `CT-` | [plan_creative_tips.md](../plan_creative_tips.md) |  |  | not in the README index yet |
| `CWG-` | [creative_worldgen.md](../creative_worldgen.md) | CQ-CWG (1), SQ-CWG (10) |  |  |
| `DA-` | [ocean_delight.md](../ocean_delight.md) |  | briefs/farming_crops.md |  |
| `DDP-` | [display_delight.md](../display_delight.md) | CQ-DDP (2), SQ-DDP (7) |  |  |
| `DF-` | [delightful.md](../delightful.md) | CQ-DF (1), SQ-DF (7) |  | not in the README index yet |
| `DP-` | [deploy.md](../deploy.md) |  | briefs/alcohol_vineryport.md, plan_1.0.3.md |  |
| `DR-` | [design_rules.md](../design_rules.md) | SQ-DR (1) |  |  |
| `DY-` | [dyeing.md](../dyeing.md) |  |  |  |
| `EDL-` | [ends_delight.md](../ends_delight.md) | SQ-EDL (10) |  | new 2026-11-16, was EN- (collided with engineering.md) |
| `EF-` | [effects.md](../effects.md) | SQ-EF (12) |  |  |
| `EN-` | [engineering.md](../engineering.md) |  |  | Ends Delight rows SQ-EN-01 to -08 (and the 2026-10-26 run results) become SQ-EDL |
| `ENC-` | [briefs/potions_enchanting.md](../briefs/potions_enchanting.md) | SQ-ENC (2) |  | new 2026-11-16, was EN-; queue SQ-EN-01, -02 in the 2026-11-01 bug batch become SQ-ENC |
| `ENM-` | [ends_delight.md](../ends_delight.md) | SQ-ENM (7) |  | not in the README index yet |
| `EP-` | [enchant_power.md](../enchant_power.md) |  |  |  |
| `EX-` | [exposure.md](../exposure.md) |  |  |  |
| `EXD-` | [extra_delight.md](../extra_delight.md) |  |  |  |
| `FDI-` | [food_dictionary.md](../food_dictionary.md) | SQ-FDI (7) |  | not in the README index yet |
| `FF-` | [food_families.md](../food_families.md) | CQ-FF (1), SQ-FF (3) |  |  |
| `FFH-` | [food_families.md](../food_families.md) | SQ-FFH (3) |  | not in the README index yet |
| `FGD-` | [fungi_delight.md](../fungi_delight.md) | SQ-FGD (26) |  |  |
| `FL-` | [flowers.md](../flowers.md) |  |  |  |
| `FO-` | [food.md](../food.md) | CQ-FO (2), SQ-FO (5) |  |  |
| `FP-` | [feature_porting.md](../feature_porting.md) |  |  |  |
| `FR-` | [fruits_delight.md](../fruits_delight.md) | CQ-FR (1), SQ-FR (5) |  |  |
| `FS-` | [testing/fridge_sort/plan.md](../testing/fridge_sort/plan.md) |  |  |  |
| `FX-` | [fixes_compat.md](../fixes_compat.md) |  | briefs/recipes_magic.md |  |
| `GLD-` | [golden_food.md](../golden_food.md) | CQ-GLD (3), SQ-GLD (16) |  |  |
| `GR-` | [gear_tuning.md](../gear_tuning.md) |  | briefs/gear_tuning.md |  |
| `HNH-` | [hearth_and_harvest.md](../hearth_and_harvest.md) |  | plan_hearth_crow_salt.md |  |
| `HTD-` | [hostile_delight.md](../hostile_delight.md) |  |  |  |
| `IM-` | [plan_1.0.3.md](../plan_1.0.3.md) |  |  | not in the README index yet |
| `IP-` | [item_physics.md](../item_physics.md) | SQ-IP (2) |  | queue SQ-IP-15, -16 stay (item physics) |
| `JC-` | [jei_coverage.md](../jei_coverage.md) |  |  |  |
| `JO-` | [jei_ordering.md](../jei_ordering.md) | CQ-JO (1) |  |  |
| `JW-` | [jei_world_tabs.md](../jei_world_tabs.md) |  | briefs/alcohol_vineryport.md |  |
| `KT-` | [kitchen.md](../kitchen.md) | CQ-KT (1), SQ-KT (9) |  |  |
| `LB-` | [lunchboxes.md](../lunchboxes.md) | CQ-LB (1), SQ-LB (2) |  |  |
| `LGM-` | [large_meals.md](../large_meals.md) | CQ-LGM (1), SQ-LGM (14) | plan_meals_rolls_bbq.md |  |
| `LO-` | [locate.md](../locate.md) |  | testing/structures/, briefs/worldgen_locate.md |  |
| `LT-` | [loot.md](../loot.md) |  | briefs/loot_lootjs-layer.md, briefs/loot_lootr.md, briefs/loot_tables.md |  |
| `LTP-` | [loading_tips.md](../loading_tips.md) | CQ-LTP (15) | loading_tips_audit.md, plan_loading_tips_screens.md | new 2026-11-16, was LT- (collided with loot.md); also loading_tips_audit.md and plan_loading_tips_screens.md |
| `LW-` | [log_warnings.md](../log_warnings.md) |  |  |  |
| `MB-` | [mooblooms.md](../mooblooms.md) | CQ-MB (1), SQ-MB (8) | briefs/mobs_mooblooms.md |  |
| `MC-` | [thirdparty_fixes.md](../thirdparty_fixes.md) |  |  | not in the README index yet |
| `MG-` | [item_merges.md](../item_merges.md) | CQ-MG (1), SQ-MG (10) |  |  |
| `MIN-` | [archive/mineral_delight.md](../archive/mineral_delight.md) | SQ-MIN (9) |  |  |
| `MJ-` | [mixed_jars.md](../mixed_jars.md) | SQ-MJ (1) |  |  |
| `MND-` | [miners_delight.md](../miners_delight.md) | CQ-MND (2) |  |  |
| `MRD-` | [more_delight.md](../more_delight.md) | CQ-MRD (1), SQ-MRD (4) |  | not in the README index yet |
| `MS-` | [mob_spawns.md](../mob_spawns.md) |  | briefs/mobs_pillagers.md, briefs/mobs_spawns.md |  |
| `MT-` | [mob_block_tweaks.md](../mob_block_tweaks.md) |  | briefs/mobs_tweaks.md |  |
| `N23-` | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |  | not in the README index yet |
| `N24-` | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |  | not in the README index yet |
| `N25-` | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |  | not in the README index yet |
| `N26-` | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |  | not in the README index yet |
| `NE-` | [nests.md](../nests.md) |  | briefs/mobs_nests.md |  |
| `NM-` | [names.md](../names.md) | SQ-NM (3) |  |  |
| `NP-` | [nether_portal.md](../nether_portal.md) |  | creative_magic.md |  |
| `NS-` | [furniture_storage.md](../furniture_storage.md) | CQ-NS (1), SQ-NS (1) |  |  |
| `NW-` | [nether_worldgen.md](../nether_worldgen.md) |  | briefs/structures_netherblockswap.md, briefs/worldgen_biomes_netherblocks.md |  |
| `OV-` | [oven.md](../oven.md) |  |  |  |
| `PA-` | [immersive_paintings.md](../immersive_paintings.md) | CQ-PA (2), SQ-PA (4) |  |  |
| `PB-` | [potion_buckets.md](../potion_buckets.md) |  | briefs/potions_potion-buckets.md |  |
| `PF-` | [perf.md](../perf.md) | SQ-PF (13) | plan_1.0.3.md |  |
| `PFS-` | [perf_suite.md](../perf_suite.md) | SQ-PFS (7) |  | new 2026-11-16, was PB- (collided with potion_buckets.md); queue SQ-PB becomes SQ-PFS |
| `PH-` | [pack_hygiene.md](../pack_hygiene.md) |  |  | PH-01 to PH-04 are mirrored as queue rows (allowed: the queue may restate an owner test) |
| `PL-` | [placeable_food.md](../placeable_food.md) |  |  |  |
| `PO-` | [potions.md](../potions.md) |  | briefs/potions_potions.md |  |
| `POT-` | [flower_pots.md](../flower_pots.md) | SQ-POT (8) |  |  |
| `PROC-` | [plan_process_overhaul.md](../plan_process_overhaul.md) | SQ-PROC (7), CQ-PROC (1) |  | new 2026-11-16 (the plan drafted PO-, owned by potions.md); process tooling, no writeup; offline tests PROC-20 to -28 (`tools/test_queue_rows.sh`) |
| `PRT-` | [immersive_portals.md](../immersive_portals.md) | CQ-PRT (4), SQ-PRT (5) |  | new 2026-11-16, was IP- (README only; item_physics.md owns IP-); queue SQ-IP-01 to -05 and CQ-IP in the Immersive Portals section become SQ-PRT, CQ-PRT |
| `PS-` | [perf.md](../perf.md) | SQ-PS (7) |  | not in the README index yet |
| `PT-` | [plantable.md](../plantable.md) | CQ-PT (1), SQ-PT (1) |  |  |
| `PWG-` | [power_grid.md](../power_grid.md) | CQ-PWG (30), SQ-PWG (101) | energy_advanced.md | energy_advanced.md has no tests of its own; its SQ-PWG-01 to -08 perf rows are in the same series |
| `R4-` | [plan_1.0.4.md](../plan_1.0.4.md) |  |  | not in the README index yet |
| `RA-` | [recipes_magic.md](../recipes_magic.md) |  | creative_magic.md, briefs/recipes_magic.md |  |
| `RB-` | [remainder_buffer.md](../remainder_buffer.md) | SQ-RB (2) |  |  |
| `RC-` | [recipes_cooking.md](../recipes_cooking.md) |  | briefs/recipes_cooking.md |  |
| `RD-` | [rustic_delight.md](../rustic_delight.md) | SQ-RD (34) | plan_1.0.3.md |  |
| `RE-` | [recipes_engineering.md](../recipes_engineering.md) | SQ-RE (1) | briefs/recipes_engineering.md |  |
| `RM-` | [recipes_metal.md](../recipes_metal.md) | SQ-RM (4) | briefs/recipes_metal.md |  |
| `ROLL-` | [roll_delight.md](../roll_delight.md) | CQ-ROLL (1), SQ-ROLL (12) | plan_meals_rolls_bbq.md |  |
| `RP-` | [recipe_progression.md](../recipe_progression.md) |  | briefs/recipes_progression.md | queue CQ-RP splits: the bench rows (CQ-RP-01 to -03) to CQ-RPB, the Raspberry Pie rows to CQ-RPI |
| `RPB-` | [perf_rpack.md](../perf_rpack.md) | CQ-RPB (3) |  | new 2026-11-16, was RP- (collided with recipe_progression.md); queue CQ-RP-01 to -03 in the bench section become CQ-RPB |
| `RPI-` | [raspberry_pie.md](../raspberry_pie.md) | CQ-RPI (40), SQ-RPI (8) |  | new 2026-11-16, was RP- (README only); queue SQ-RP and the Raspberry Pie CQ-RP rows become SQ-RPI, CQ-RPI |
| `RSP-` | [farmers_respite.md](../farmers_respite.md) | CQ-RSP (1), SQ-RSP (15) |  |  |
| `RW-` | [recipes_wood.md](../recipes_wood.md) | CQ-RW (1), SQ-RW (9) | briefs/recipes_wood.md |  |
| `SB-` | [storage_audit.md](../storage_audit.md) | SQ-SB (10) |  | not in the README index yet |
| `SCAT-` | [plan_structure_catalogue.md](../plan_structure_catalogue.md) | CQ-SCAT (5), SQ-SCAT (3) |  |  |
| `SH-` | [shields.md](../shields.md) |  |  |  |
| `SLT-` | [salt.md](../salt.md) | CQ-SLT (2), SQ-SLT (5) |  |  |
| `SN-` | [sticky_notes.md](../sticky_notes.md) | SQ-SN (4) |  |  |
| `SP-` | [plan_1.0.3.md](../plan_1.0.3.md) |  |  | not in the README index yet |
| `ST-` | [structure_tuning.md](../structure_tuning.md) |  | meat_blocks.md (ST-149), nether_swaps.md (ST-150 to ST-152), briefs/mobs_pillagers.md, briefs/structures_contents.md | ranges handed to other docs stay ST- (one series) |
| `STD-` | [storage_delight.md](../storage_delight.md) | CQ-STD (1), SQ-STD (3) |  | not in the README index yet |
| `STL-` | [stepstool.md](../stepstool.md) | SQ-STL (2) |  |  |
| `SVA-` | [sky_village_addons.md](../sky_village_addons.md) | CQ-SVA (1), SQ-SVA (5) |  |  |
| `SWP-` | [item_merges.md](../item_merges.md) | SQ-SWP (8) |  | not in the README index yet |
| `SY-` | [crop_yields.md](../crop_yields.md) | CQ-SY (1), SQ-SY (22) |  |  |
| `TI-` | [time.md](../time.md) | SQ-TI (2) | briefs/gameplay_time.md |  |
| `TL-` | [pack_tooling.md](../pack_tooling.md) | SQ-TL (1) |  |  |
| `TN-` | [tnt_shrapnel.md](../tnt_shrapnel.md) |  |  |  |
| `TO-` | [toasts.md](../toasts.md) |  |  |  |
| `TS-` | [trashslot.md](../trashslot.md) |  | plan_1.0.3.md |  |
| `TSK-` | [treated_sticks.md](../treated_sticks.md) | CQ-TSK (1), SQ-TSK (2) |  |  |
| `TSY-` | [trade_sync.md](../trade_sync.md) | SQ-TSY (2) |  | new 2026-11-16, was TS- (collided with trashslot.md); matches SQ-TSY, CQ-TSY |
| `TT-` | [tooltips.md](../tooltips.md) |  |  |  |
| `TX-` | [plan_scratch_testing_expansion.md](../plan_scratch_testing_expansion.md) | CQ-TX (4), SQ-TX (18) |  | not in the README index yet |
| `UD-` | [unusual_delight.md](../unusual_delight.md) |  |  |  |
| `VB-` | [alcohol_part_b.md](../alcohol_part_b.md) |  | briefs/alcohol_vineryport.md, alcohol.md |  |
| `VD-` | [veggies_delight.md](../veggies_delight.md) |  |  |  |
| `VH-` | [visceral_heap.md](../visceral_heap.md) | CQ-VH (5), SQ-VH (14) | plan_visceral_heap.md |  |
| `VJ-` | [vinery_jei.md](../vinery_jei.md) |  |  |  |
| `VN-` | [vinery.md](../vinery.md) |  |  |  |
| `VS-` | [vine_shearing.md](../vine_shearing.md) | SQ-VS (4) |  |  |
| `VTD-` | [vintage_delight.md](../vintage_delight.md) |  | ocean_delight.md |  |
| `WCD-` | [creative_delight.md](../creative_delight.md) | SQ-WCD (7) |  | not in the README index yet |
| `WE-` | [weather.md](../weather.md) | SQ-WE (5) | briefs/worldgen_seasons.md |  |
| `WG-` | [biome_tuning.md](../biome_tuning.md) |  |  |  |
| `WL-` | [wthit_lines.md](../wthit_lines.md) |  |  |  |
| `WO-` | [wise_oak.md](../wise_oak.md) | CQ-WO (7), SQ-WO (37) |  |  |
| `WT-` | [wandering_trader.md](../wandering_trader.md) | CQ-WT (4), SQ-WT (1) | briefs/loot_trader.md |  |
## 4. Queue prefixes with no writeup prefix of their own

Queue series named after a batch, a run or a sub-feature rather than a writeup prefix. Owner = the doc that quotes the
series most (outside the queue, run docs, README, TODO and briefs). New queue rows do not start new series like these:
they use the feature's writeup prefix (section 0). Rows with no owner get one at the first lint run.

| Queue prefix | Rows | Owner doc (most references) | Note |
|---|---|---|---|
| `CQ-ATC-` | 1 | [perf.md](../perf.md) |  |
| `CQ-AZW-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-BBQ2-` | 1 | [names.md](../names.md) |  |
| `CQ-BBQF-` | 1 | [barbeques_delight.md](../barbeques_delight.md) |  |
| `CQ-BPT-` | 3 | [creative_compat.md](../creative_compat.md) |  |
| `CQ-BRA-` | 1 | [blockrunner.md](../blockrunner.md) |  |
| `CQ-CAT-` | 10 | [cozy_cats.md](../cozy_cats.md) |  |
| `CQ-CBA-` | 4 | [creative_brewing.md](../creative_brewing.md) |  |
| `CQ-CBC-` | 1 | [plan_cb_publish.md](../plan_cb_publish.md) |  |
| `CQ-CBO-` | 4 | [creative_brewing.md](../creative_brewing.md) |  |
| `CQ-CBP-` | 2 | [plan_cb_polish.md](../plan_cb_polish.md) |  |
| `CQ-CBS-` | 1 | [plan_cb_publish.md](../plan_cb_publish.md) |  |
| `CQ-CCB-` | 3 | [creative_engineering.md](../creative_engineering.md) |  |
| `CQ-CCO-` | 3 | [creative_engineering.md](../creative_engineering.md) |  |
| `CQ-CDFL-` | 1 | [plan_cb_polish.md](../plan_cb_polish.md) |  |
| `CQ-CDFO-` | 1 | [nests.md](../nests.md) |  |
| `CQ-CDJ-` | 1 | [plan_creative_delight.md](../plan_creative_delight.md) |  |
| `CQ-CDM2-` | 1 | [ocean_delight.md](../ocean_delight.md) |  |
| `CQ-CDNS-` | 1 | [plan_cd_namespace.md](../plan_cd_namespace.md) |  |
| `CQ-CDTX-` | 1 | [creative_delight.md](../creative_delight.md) |  |
| `CQ-CEN-` | 1 | [creative_split.md](../creative_split.md) |  |
| `CQ-CEQ-` | 1 | [creative_equipment.md](../creative_equipment.md) |  |
| `CQ-CFR-` | 1 | [creative_split.md](../creative_split.md) |  |
| `CQ-CGE-` | 9 | [creative_engineering.md](../creative_engineering.md) |  |
| `CQ-CHK-` | 1 | [fixes_compat.md](../fixes_compat.md) |  |
| `CQ-CIF-` | 1 | [creative_engineering.md](../creative_engineering.md) |  |
| `CQ-CLOG-` | 2 | [creative_engineering.md](../creative_engineering.md) |  |
| `CQ-CMV-` | 3 | [creative_engineering.md](../creative_engineering.md) |  |
| `CQ-CNG-` | 2 | [cans.md](../cans.md) |  |
| `CQ-CNP-` | 1 | [cans.md](../cans.md) |  |
| `CQ-CPP-` | 1 | [creative_engineering.md](../creative_engineering.md) |  |
| `CQ-CSR-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-DCM-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-DEP-` | 1 | [creative_split.md](../creative_split.md) |  |
| `CQ-DRK-` | 2 | [creative_delight_drinks.md](../creative_delight_drinks.md) |  |
| `CQ-DUMP-` | 6 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-ENGM-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-FAU-` | 2 | [kitchen.md](../kitchen.md) |  |
| `CQ-FBS-` | 3 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-FCB-` | 2 | [kitchen.md](../kitchen.md) |  |
| `CQ-FCK-` | 2 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-FDB-` | 1 | [farmers_delight.md](../farmers_delight.md) |  |
| `CQ-FDR-` | 4 | [jar_handle_leak.md](../jar_handle_leak.md) |  |
| `CQ-FLR-` | 3 | [visceral_heap.md](../visceral_heap.md) |  |
| `CQ-FSD-` | 1 | [farmers_structures.md](../farmers_structures.md) |  |
| `CQ-GAP-` | 3 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-GF31-` | 3 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-GLD2-` | 1 | [golden_food.md](../golden_food.md) |  |
| `CQ-HCS-` | 1 | [plan_hearth_crow_swap.md](../plan_hearth_crow_swap.md) |  |
| `CQ-HH-` | 32 | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |
| `CQ-HOP-` | 1 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `CQ-HR-` | 1 | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |
| `CQ-IDS-` | 6 | [plan_owner_ids.md](../plan_owner_ids.md) |  |
| `CQ-JB-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-JG-` | 3 | [testing/RUNNER.md](../testing/RUNNER.md) |  |
| `CQ-JT-` | 10 | [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md) |  |
| `CQ-LBS-` | 1 | [sticky_notes.md](../sticky_notes.md) |  |
| `CQ-LCA-` | 5 | [nests.md](../nests.md) |  |
| `CQ-LTC-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-MRGT-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-MXS-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-NCR-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-NDM-` | 1 | [nethers_delight.md](../nethers_delight.md) |  |
| `CQ-NEW-` | 6 | [client_capture.md](../client_capture.md) |  |
| `CQ-NSM-` | 1 | [namespace_move.md](../namespace_move.md) |  |
| `CQ-OAT-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-OB-` | 4 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-OLD-` | 8 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-PCK-` | 2 | [veggies_delight.md](../veggies_delight.md) |  |
| `CQ-PG-` | 5 | [plan_portal_gun.md](../plan_portal_gun.md) |  |
| `CQ-PJL-` | 2 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-PNE-` | 9 | [perf.md](../perf.md) |  |
| `CQ-PRI-` | 4 | [plan_cb_publish.md](../plan_cb_publish.md) |  |
| `CQ-PS15-` | 3 | [plan_post_scratch_1115.md](../plan_post_scratch_1115.md) |  |
| `CQ-PS15B-` | 2 | [plan_post_scratch_1115b.md](../plan_post_scratch_1115b.md) |  |
| `CQ-PSD-` | 2 | [plan_prescratch_fixes.md](../plan_prescratch_fixes.md) |  |
| `CQ-PSF-` | 1 | [crops.md](../crops.md) |  |
| `CQ-PST-` | 2 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-PSU-` | 5 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-PTH-` | 6 | [blockrunner.md](../blockrunner.md) |  |
| `CQ-RDB-` | 1 | [rustic_delight.md](../rustic_delight.md) |  |
| `CQ-SG-` | 1 | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |
| `CQ-SINK-` | 3 | [blockrunner.md](../blockrunner.md) |  |
| `CQ-SLK-` | 1 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `CQ-SPC-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-SPD-` | 1 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `CQ-SPF-` | 2 | [creative_split.md](../creative_split.md) |  |
| `CQ-SPFX-` | 2 | [creative_engineering.md](../creative_engineering.md) |  |
| `CQ-SWL-` | 2 | [creative_split.md](../creative_split.md) |  |
| `CQ-TD-` | 1 | [time.md](../time.md) |  |
| `CQ-TP-` | 16 | [testing/RUNNER.md](../testing/RUNNER.md) |  |
| `CQ-TPF-` | 45 | [thirdparty_fixes.md](../thirdparty_fixes.md) |  |
| `CQ-TSL-` | 2 | [trashslot.md](../trashslot.md) |  |
| `CQ-TTP-` | 3 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-TXC-` | 10 | [client_capture.md](../client_capture.md) |  |
| `CQ-TXP-` | 2 | [plan_scratch_testing_expansion.md](../plan_scratch_testing_expansion.md) |  |
| `CQ-TXR-` | 1 | [plan_post_scratch_1115b.md](../plan_post_scratch_1115b.md) |  |
| `CQ-UI-` | 4 | [plan_creative_delight.md](../plan_creative_delight.md) |  |
| `CQ-VP-` | 14 | none found | no doc names it: the first lint run assigns an owner |
| `CQ-VTDA-` | 1 | [vintage_delight.md](../vintage_delight.md) |  |
| `CQ-WC-` | 3 | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |
| `CQ-WGE-` | 5 | [plan_worldgen_edit.md](../plan_worldgen_edit.md) |  |
| `CQ-XAC-` | 1 | [perf.md](../perf.md) |  |
| `SQ-AC-` | 5 | [gear_tuning.md](../gear_tuning.md) |  |
| `SQ-AD-` | 2 | [ocean_delight.md](../ocean_delight.md) |  |
| `SQ-ATC-` | 5 | [perf.md](../perf.md) |  |
| `SQ-AZW-` | 4 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-BBQ2-` | 7 | [plan_meals_rolls_bbq.md](../plan_meals_rolls_bbq.md) |  |
| `SQ-BBQF-` | 7 | [barbeques_delight.md](../barbeques_delight.md) |  |
| `SQ-BBQS-` | 3 | [barbeques_delight.md](../barbeques_delight.md) |  |
| `SQ-BBQW-` | 5 | [barbeques_delight.md](../barbeques_delight.md) |  |
| `SQ-BJ-` | 3 | [recipes_cooking.md](../recipes_cooking.md) |  |
| `SQ-BK-` | 1 | [oven.md](../oven.md) |  |
| `SQ-BLC-` | 4 | [perf.md](../perf.md) |  |
| `SQ-BPT-` | 1 | [creative_compat.md](../creative_compat.md) |  |
| `SQ-BRA-` | 7 | [blockrunner.md](../blockrunner.md) |  |
| `SQ-BREED-` | 3 | [breeding_audit.md](../breeding_audit.md) |  |
| `SQ-CAT-` | 5 | [item_merges.md](../item_merges.md) |  |
| `SQ-CBA-` | 18 | [creative_brewing.md](../creative_brewing.md) |  |
| `SQ-CBB-` | 12 | [creative_brewing.md](../creative_brewing.md) |  |
| `SQ-CBC-` | 7 | [plan_cb_publish.md](../plan_cb_publish.md) |  |
| `SQ-CBJ-` | 5 | [creative_brewing.md](../creative_brewing.md) |  |
| `SQ-CBJ4-` | 7 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-CBM-` | 6 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-CBO-` | 42 | [plan_cb_polish.md](../plan_cb_polish.md) |  |
| `SQ-CBP-` | 13 | [creative_equipment.md](../creative_equipment.md) |  |
| `SQ-CBS-` | 2 | [plan_cb_publish.md](../plan_cb_publish.md) |  |
| `SQ-CBW-` | 4 | [kitchen.md](../kitchen.md) |  |
| `SQ-CC12-` | 3 | [creative_engineering.md](../creative_engineering.md) |  |
| `SQ-CCB-` | 1 | [jei_ordering.md](../jei_ordering.md) |  |
| `SQ-CCO-` | 16 | [creative_engineering.md](../creative_engineering.md) |  |
| `SQ-CDB-` | 20 | [ocean_delight.md](../ocean_delight.md) |  |
| `SQ-CDF-` | 10 | [creative_delight.md](../creative_delight.md) |  |
| `SQ-CDFL-` | 4 | [creative_delight.md](../creative_delight.md) |  |
| `SQ-CDFO-` | 3 | [plan_cb_publish.md](../plan_cb_publish.md) |  |
| `SQ-CDFW-` | 11 | [creative_delight.md](../creative_delight.md) |  |
| `SQ-CDJ-` | 16 | [creative_delight.md](../creative_delight.md) |  |
| `SQ-CDJ4-` | 9 | [creative_delight.md](../creative_delight.md) |  |
| `SQ-CDM2-` | 4 | [ocean_delight.md](../ocean_delight.md) |  |
| `SQ-CDNS-` | 12 | [plan_cd_namespace.md](../plan_cd_namespace.md) |  |
| `SQ-CDS-` | 4 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-CDW-` | 8 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-CEN-` | 8 | [creative_engineering.md](../creative_engineering.md) |  |
| `SQ-CEQ-` | 8 | [creative_split.md](../creative_split.md) |  |
| `SQ-CF-` | 2 | [food.md](../food.md) |  |
| `SQ-CFR-` | 8 | [creative_split.md](../creative_split.md) |  |
| `SQ-CFS-` | 3 | [kitchen.md](../kitchen.md) |  |
| `SQ-CGE-` | 18 | [creative_engineering.md](../creative_engineering.md) |  |
| `SQ-CIF-` | 4 | [creative_split.md](../creative_split.md) |  |
| `SQ-CMG-` | 5 | [creative_magic.md](../creative_magic.md) |  |
| `SQ-CMV-` | 4 | [creative_engineering.md](../creative_engineering.md) |  |
| `SQ-CNG-` | 4 | [cans.md](../cans.md) |  |
| `SQ-CNP-` | 1 | [cans.md](../cans.md) |  |
| `SQ-CO-` | 1 | [item_merges.md](../item_merges.md) |  |
| `SQ-CPP-` | 5 | [creative_engineering.md](../creative_engineering.md) |  |
| `SQ-CS2-` | 8 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-CSI-` | 2 | [curios_slots.md](../curios_slots.md) |  |
| `SQ-CUP-` | 5 | [miners_delight.md](../miners_delight.md) |  |
| `SQ-DB-` | 3 | [oven.md](../oven.md) |  |
| `SQ-DCM-` | 4 | [plan_cb_publish.md](../plan_cb_publish.md) |  |
| `SQ-DD-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-DEP-` | 12 | [creative_split.md](../creative_split.md) |  |
| `SQ-DM-` | 4 | [veggies_delight.md](../veggies_delight.md) |  |
| `SQ-DNR-E-` | 4 | [ends_delight.md](../ends_delight.md) |  |
| `SQ-DNR-O-` | 5 | [ocean_delight.md](../ocean_delight.md) |  |
| `SQ-DRK-` | 7 | [creative_delight_drinks.md](../creative_delight_drinks.md) |  |
| `SQ-DW-` | 3 | [structure_tuning.md](../structure_tuning.md) |  |
| `SQ-EFB-` | 23 | [structure_tuning.md](../structure_tuning.md) |  |
| `SQ-EFX-` | 14 | [effects.md](../effects.md) |  |
| `SQ-ENGM-` | 5 | [plan_cb_polish.md](../plan_cb_polish.md) |  |
| `SQ-FCB-` | 11 | [plan_food_cooking_batch.md](../plan_food_cooking_batch.md) |  |
| `SQ-FCK-` | 18 | [plan_farming_cooking_batch.md](../plan_farming_cooking_batch.md) |  |
| `SQ-FD-` | 1 | [creative_delight.md](../creative_delight.md) |  |
| `SQ-FDB-` | 15 | [farmers_delight.md](../farmers_delight.md) |  |
| `SQ-FFB-` | 5 | [oven.md](../oven.md) |  |
| `SQ-FLR-` | 6 | [visceral_heap.md](../visceral_heap.md) |  |
| `SQ-FN-` | 6 | [ocean_delight.md](../ocean_delight.md) |  |
| `SQ-FSD-` | 10 | [farmers_structures.md](../farmers_structures.md) |  |
| `SQ-FTW-` | 8 | [perf.md](../perf.md) |  |
| `SQ-GAP-` | 9 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-GE-` | 1 | [structure_tuning.md](../structure_tuning.md) |  |
| `SQ-GF-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-GF31-` | 4 | [time.md](../time.md) |  |
| `SQ-GFB-` | 7 | [perf_suite.md](../perf_suite.md) |  |
| `SQ-GLD2-` | 4 | [golden_food.md](../golden_food.md) |  |
| `SQ-GM-` | 1 | [fixes_compat.md](../fixes_compat.md) |  |
| `SQ-GT-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-HCS-` | 12 | [plan_hearth_crow_swap.md](../plan_hearth_crow_swap.md) |  |
| `SQ-HH-` | 578 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-HOP-` | 11 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-HR-` | 5 | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |
| `SQ-HRU-` | 4 | [perf.md](../perf.md) |  |
| `SQ-HSC-` | 5 | [plan_hearth_crow_swap.md](../plan_hearth_crow_swap.md) |  |
| `SQ-HT-` | 6 | [perf.md](../perf.md) |  |
| `SQ-IDS-` | 3 | [plan_owner_ids.md](../plan_owner_ids.md) |  |
| `SQ-IMS-` | 6 | [perf.md](../perf.md) |  |
| `SQ-INV-` | 21 | [pack_tooling.md](../pack_tooling.md) |  |
| `SQ-IPO-` | 5 | [perf_suite.md](../perf_suite.md) |  |
| `SQ-IWF-` | 5 | [perf.md](../perf.md) |  |
| `SQ-JFP-` | 5 | [mixed_jars.md](../mixed_jars.md) |  |
| `SQ-JRFTL-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-JSL-` | 2 | [creative_delight.md](../creative_delight.md) |  |
| `SQ-KS-` | 6 | [creative_delight.md](../creative_delight.md) |  |
| `SQ-LBS-` | 6 | [sticky_notes.md](../sticky_notes.md) |  |
| `SQ-LE-` | 7 | [structure_tuning.md](../structure_tuning.md) |  |
| `SQ-LEM-` | 2 | [plan_scratch_bugfix_1103.md](../plan_scratch_bugfix_1103.md) |  |
| `SQ-LP-` | 1 | [mob_block_tweaks.md](../mob_block_tweaks.md) |  |
| `SQ-LR-` | 1 | [fixes_compat.md](../fixes_compat.md) |  |
| `SQ-MCL-` | 7 | [perf.md](../perf.md) |  |
| `SQ-MD-` | 1 | [plan_creative_delight.md](../plan_creative_delight.md) |  |
| `SQ-MDL-` | 5 | [miners_delight.md](../miners_delight.md) |  |
| `SQ-MDP-` | 21 | [plan_creative_delight.md](../plan_creative_delight.md) |  |
| `SQ-MELT-` | 11 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-META31-` | 11 | [structure_tuning.md](../structure_tuning.md) |  |
| `SQ-MXS-` | 3 | [vintage_delight.md](../vintage_delight.md) |  |
| `SQ-NCH-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-NDM-` | 8 | [nethers_delight.md](../nethers_delight.md) |  |
| `SQ-NEST-` | 2 | [nests.md](../nests.md) |  |
| `SQ-NSM-` | 10 | [namespace_move.md](../namespace_move.md) |  |
| `SQ-OB-` | 12 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-OF-` | 1 | [recipes_cooking.md](../recipes_cooking.md) |  |
| `SQ-OM-` | 1 | [alcohol.md](../alcohol.md) |  |
| `SQ-PCK-` | 5 | [food_families.md](../food_families.md) |  |
| `SQ-PERFCFG-` | 4 | [perf_suite.md](../perf_suite.md) |  |
| `SQ-PF16-` | 4 | [perf.md](../perf.md) |  |
| `SQ-PG-` | 4 | [plan_portal_gun.md](../plan_portal_gun.md) |  |
| `SQ-PJL-` | 5 | [recipes_cooking.md](../recipes_cooking.md) |  |
| `SQ-PR-` | 2 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-PRI-` | 3 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-PS15-` | 19 | [plan_post_scratch_1115.md](../plan_post_scratch_1115.md) |  |
| `SQ-PS15B-` | 12 | [plan_post_scratch_1115b.md](../plan_post_scratch_1115b.md) |  |
| `SQ-PSD-` | 7 | [plan_prescratch_fixes.md](../plan_prescratch_fixes.md) |  |
| `SQ-PSF-` | 9 | [crops.md](../crops.md) |  |
| `SQ-PSM-` | 9 | [plan_prescratch_fixes.md](../plan_prescratch_fixes.md) |  |
| `SQ-PST-` | 11 | [plan_prescratch_fixes.md](../plan_prescratch_fixes.md) |  |
| `SQ-PSU-` | 9 | [plan_scratch_bugfix_1103.md](../plan_scratch_bugfix_1103.md) |  |
| `SQ-PSW-` | 6 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-PTH-` | 35 | [blockrunner.md](../blockrunner.md) |  |
| `SQ-QN-` | 2 | [loading_tips_audit.md](../loading_tips_audit.md) |  |
| `SQ-RCO-` | 2 | [recipes_cooking.md](../recipes_cooking.md) |  |
| `SQ-RDB-` | 6 | [plan_rustic_fold_in.md](../plan_rustic_fold_in.md) |  |
| `SQ-RDX-` | 14 | [rustic_delight.md](../rustic_delight.md) |  |
| `SQ-RING-` | 4 | [structure_tuning.md](../structure_tuning.md) |  |
| `SQ-RS-` | 2 | [recipe_progression.md](../recipe_progression.md) |  |
| `SQ-SALT-` | 16 | [item_merges.md](../item_merges.md) |  |
| `SQ-SALT2-` | 9 | [salt.md](../salt.md) |  |
| `SQ-SB31-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-SF-` | 1 | [crops.md](../crops.md) |  |
| `SQ-SG-` | 3 | [wthit_lines.md](../wthit_lines.md) |  |
| `SQ-SMK-` | 56 | [plan_process_overhaul.md](../plan_process_overhaul.md) | new 2026-11-16: T1 smoke rows, one section per own jar (`testing/rows/SMK.qb`, P2.2); a series of its own because the rows belong to jars, not to one feature |
| `SQ-SGF-` | 2 | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |
| `SQ-SK-` | 3 | [perf_suite.md](../perf_suite.md) |  |
| `SQ-SLK-` | 5 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-SNF-` | 4 | [food.md](../food.md) |  |
| `SQ-SPC-` | 2 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-SPD-` | 9 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-SPF-` | 38 | [creative_split.md](../creative_split.md) |  |
| `SQ-SPFX-` | 7 | [creative_split.md](../creative_split.md) |  |
| `SQ-SSP-` | 3 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-STC-` | 3 | [perf.md](../perf.md) |  |
| `SQ-STK-` | 6 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-STN-` | 7 | [vintage_delight.md](../vintage_delight.md) |  |
| `SQ-SV-` | 3 | [testing/structures/12_earth_fill_camps.md](../testing/structures/12_earth_fill_camps.md) |  |
| `SQ-SWL-` | 1 | [creative_split.md](../creative_split.md) |  |
| `SQ-T1-` | 4 | [plan_bugfix_1114.md](../plan_bugfix_1114.md) |  |
| `SQ-TC-` | 1 | none found | no doc names it: the first lint run assigns an owner |
| `SQ-TD-` | 5 | [time.md](../time.md) |  |
| `SQ-TER-` | 8 | [structure_tuning.md](../structure_tuning.md) |  |
| `SQ-TIE-` | 1 | [locate.md](../locate.md) |  |
| `SQ-TP-` | 39 | [immersive_portals.md](../immersive_portals.md) |  |
| `SQ-TPF-` | 13 | [thirdparty_fixes.md](../thirdparty_fixes.md) |  |
| `SQ-TXQ-` | 3 | [plan_scratch_testing_expansion.md](../plan_scratch_testing_expansion.md) |  |
| `SQ-UC-` | 3 | [fixes_compat.md](../fixes_compat.md) |  |
| `SQ-VDF-` | 10 | [veggies_delight.md](../veggies_delight.md) |  |
| `SQ-VF-` | 5 | [crops.md](../crops.md) |  |
| `SQ-VG-` | 4 | [mob_block_tweaks.md](../mob_block_tweaks.md) |  |
| `SQ-VL-` | 3 | [mob_block_tweaks.md](../mob_block_tweaks.md) |  |
| `SQ-VNG-` | 2 | [plan_scratch_bugfix_1103.md](../plan_scratch_bugfix_1103.md) |  |
| `SQ-VP-` | 23 | [vintage_delight.md](../vintage_delight.md) |  |
| `SQ-VTD4-` | 9 | [vintage_delight.md](../vintage_delight.md) |  |
| `SQ-VTD5-` | 1 | [vintage_delight.md](../vintage_delight.md) |  |
| `SQ-VTD6-` | 2 | [plan_scratch_bugfix_1103.md](../plan_scratch_bugfix_1103.md) |  |
| `SQ-VTD7-` | 2 | [vintage_delight.md](../vintage_delight.md) |  |
| `SQ-VTD8-` | 4 | [vintage_delight.md](../vintage_delight.md) |  |
| `SQ-VTD9-` | 1 | [vintage_delight.md](../vintage_delight.md) |  |
| `SQ-VTDA-` | 4 | [item_merges.md](../item_merges.md) |  |
| `SQ-WC-` | 5 | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |
| `SQ-WCT-` | 8 | [hearth_and_harvest.md](../hearth_and_harvest.md) |  |
| `SQ-WGE-` | 27 | [plan_scratch_bugfix_1103.md](../plan_scratch_bugfix_1103.md) |  |
| `SQ-WGP-` | 15 | [perf.md](../perf.md) |  |

## History

- 2026-11-16: index built from a scan of the docs, the README prefix column and the scratch queue (plan_rules_fixes.md section 2); 10 renames proposed (section 2), none applied yet.
- 2026-11-17: the 10 renames applied by `tools/test_prefix_rename.py` (591 ids; 9 by hand after the report); queue columns updated; `tools/test_prefix_lint.py` built (fails on 20 queue ids defined twice, listed in plan_rules_fixes.md "Owner list (P1)").
