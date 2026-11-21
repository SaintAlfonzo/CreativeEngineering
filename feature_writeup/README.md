# Feature writeups

One file per finished (or nearly finished) pack feature, for two readers: you, when you come back to
test or retune something, and Claude, at the start of a later session. Each writeup is the single
up-to-date reference for its feature. The code and config stay the source of truth for values; the
writeup explains how the pieces fit and how to prove they work.

What is still left across every writeup, one table per subfamily (sorted by status: Bug, Planned, Pinned, In Progress, Testing), one row per change with its status,
work, decisions and pending tests: [todo/TODO.md](todo/TODO.md). Finished (Complete) rows are in its archive section at the bottom. New ideas go in
[todo/scratchpad.md](todo/scratchpad.md) first.

## Index

Test prefixes: every prefix, its owner doc and the renamed ones are in
[testing/test_prefix_index.md](testing/test_prefix_index.md); a new prefix gets its row there before its first test.

<!-- generated:status_index begin readme sha1=f286331a646a -->
| Feature | File | Test prefix | Status | Last run (per queue code) | Jars | Note |
|---|---|---|---|---|---|---|
| BlockRunner walk speed by surface | [blockrunner.md](blockrunner.md) | `BR-` | BUILT | SQ-PTH r1120nv 2026-11-21: 2 INCONCLUSIVE; SQ-BRA r1119 2026-11-19: 2 PASS, 1 LOOK; CQ-PTH r1116 2026-11-16: 1 DEFERRED | creativecompat pending, creativedevtools installed 2026-11-20, creativedelight installed 2026-11-21 | DEPLOYED, in-game test pending |
| Tooltips | [tooltips.md](tooltips.md) | `TT-` | BUILT | none | creativecompat pending, creativecommon pending | DEPLOYED (new removals), in-game test pending |
| Structure tuning | [structure_tuning.md](structure_tuning.md) | `ST-` | BUILT | SQ-TER r1120nv 2026-11-21: 1 INCONCLUSIVE; SQ-ST r1120nv 2026-11-21: 1 PASS; SQ-PSM r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT, 1 INCONCLUSIVE; +6 codes | creativeworldgen installed 2026-11-21 | DEPLOYED, in-game test pending (plans 7.1 to 7.4 went out INCOMPLETE with 1.0.6 |
| Structure testing log | [00_plan.md](testing/structures/00_plan.md) | `ST-`, `LO-` | reference | none |  | scratch run 8.0.1 to 8.0.10 done 2026-10-19 (M1 build, M2, Phases 3 to 10), 11 (1.0.8 fixes) and 12 (earth ... |
| Structures by biome | [structures_by_biome.md](structures_by_biome.md) | none | reference | none |  | reference, regenerate with `python3 tools/gen_structure_biome_table.py` |
| Biome fits | [biome_fits.md](biome_fits.md) | none | reference | none |  | reference |
| Locate | [locate.md](locate.md) | `LO-` | BUILT | none | creativeworldgen installed 2026-11-21 | DEPLOYED with 1.0.6 as part of the INCOMPLETE structure tuning (fold stages A+B, non-blocking loads, ... |
| Loot | [loot.md](loot.md) | `LT-` | BUILT | none | creativeloot pending, creativedelight installed 2026-11-21, creativecommon pending | DEPLOYED, in-game test pending (LT-01, LT-06 passed |
| Loot catalog | [loot_catalog/README.md](loot_catalog/README.md) | none | reference | none |  | reference, regenerate with `python3 tools/loot_breakdown.py` |
| Food | [food.md](food.md) | `FO-` | BUILT | SQ-SNF r1120nv 2026-11-21: 1 PASS; SQ-PS r1120nv 2026-11-21: 3 PASS, 1 BATCH-FAULT; SQ-FO r1120nv 2026-11-21: 6 PASS, 1 FAIL, 5 BATCH-FAULT; +2 codes | creativecommon pending, creativedelight installed 2026-11-21 | BUILT/DEPLOYED parts, see its status block (FO-01 passed |
| Performance | [perf.md](perf.md) | `PF-` | BROKEN | SQ-WGP r1120nv 2026-11-21: 1 PASS; SQ-PF r1120nv 2026-11-21: 2 PASS, 2 BATCH-FAULT; SQ-PERFCFG r1120nv 2026-11-21: 2 PASS; +13 codes | creativecommon pending, creativeworldgen installed 2026-11-21 | DEPLOYED (report tooling with 1.0.3, hot-spot fixes with 1.0.4), PF-01 to PF-12 pending |
| Perf suite | [perf_suite.md](perf_suite.md) | `PFS-`, `G`, `C`, `W` | BUILT | SQ-SK r1120nv 2026-11-21: 2 PASS; SQ-PFS r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT; SQ-SV r1120lr 2026-11-20: 1 BATCH-FAULT | creativedevtools installed 2026-11-20 | C- and W- await the owner's veto |
| Resource pack bench | [perf_rpack.md](perf_rpack.md) | `RPB-` | BUILT | none | tools | BUILT 2026-10-29 |
| Curios slots | [curios_slots.md](curios_slots.md) | `CS-` | BUILT | SQ-CSI r1120lr 2026-11-20: 2 BATCH-FAULT | creativeequipment installed 2026-11-21, creativecompat pending | BUILT |
| Stepstools | [stepstool.md](stepstool.md) | `STL-` | BUILT | SQ-STL r1118 2026-11-18: 1 PASS, 1 BATCH-FAULT | creativeequipment installed 2026-11-21 | STL-01 to STL-16 pending in game |
| Feature porting | [feature_porting.md](feature_porting.md) | `FP-` | BUILT | none | creativecompat pending | research 2026-10-18 |
| Baby mobs | [baby_mobs.md](baby_mobs.md) | `BM-` | BUILT | SQ-BM r1120lr 2026-11-20: 2 BATCH-FAULT | creativeworldgen installed 2026-11-21 | spawn egg on an adult fixed and milestone 2 BUILT 2026-11-01 (babyMobs 1.1.0, waits for the Creative Delight ... |
| Trash slot | [trashslot.md](trashslot.md) | `TS-` | VERIFIED | none | creativecompat pending | DEPLOYED with 1.0.3, TS-01 to TS-13 pending |
| Fridge sort item loss | [plan.md](testing/fridge_sort/plan.md) | `FS-` | BUILT | none | creativecompat pending | fix DEPLOYED with 1.0.7 (2026-10-18) |
| Deploy pipeline | [deploy.md](deploy.md) | `DP-` | BUILT | none | tools | BUILT, used for the 1.0.3, 1.0.4, 1.0.6, 1.0.7, 1.0.8, 1.0.9, 1.1.01 to 1.1.06 and 1.2.00 deploys (server ... |
| Vinery JEI pages | [vinery_jei.md](vinery_jei.md) | `VJ-` | reference | none |  | REMOVED 2026-10-27: Creative Common's 1.3.10 plugin deleted (pack_hygiene.md finding 6) |
| Vinery | [vinery.md](vinery.md) | `VN-` | reference | SQ-VN r1120nv 2026-11-21: 1 BATCH-FAULT |  | reference, in-game checks pending (VN-01 to VN-20) |
| Food catalog | [food_catalog.md](food_catalog.md) | none | reference | none |  | reference, regenerate with `python3 tools/food_catalog.py` after `/dumpfood` |
| Food families | [food_families.md](food_families.md) | `FF-` | BUILT | SQ-RM r1120nv 2026-11-21: 3 PASS, 1 BATCH-FAULT; SQ-PCK r1120nv 2026-11-21: 3 PASS, 1 FAIL, 1 BATCH-FAULT; SQ-FFH r1120nv 2026-11-21: 1 BATCH-FAULT; +2 codes | creativecommon pending, creativedelight installed 2026-11-21 | 2026-11-02 night: pancake family (4.7) BUILT, CD jar installed 2026-11-02 23:16, not booted |
| Can parity worksheet | [can_parity.md](can_parity.md) | `CP-` | reference | none |  | worksheet written 2026-10-13 |
| Placeable food | [placeable_food.md](placeable_food.md) | `PL-` | BUILT | none | creativedelight installed 2026-11-21 | DEPLOYED with 1.0.6 |
| Plantable items | [plantable.md](plantable.md) | `PT-` | BUILT | SQ-PT r1120lr 2026-11-20: 1 PASS | creativecommon pending, kubejs | BUILT 2026-11-01, unreleased |
| Dyeing | [dyeing.md](dyeing.md) | `DY-` | BUILT | none |  | audit written 2026-10-27 |
| Furniture storage | [furniture_storage.md](furniture_storage.md) | `NS-` | BUILT | SQ-NS r1118 2026-11-18: 1 BATCH-FAULT; CQ-NS r1101 2026-11-01: 1 OPEN | creativecompat pending | BUILT 2026-10-31 (Creative Common), installed, NS-01 to NS-05 pending |
| Placed treated sticks | [treated_sticks.md](treated_sticks.md) | `TSK-` | BUILT | SQ-TSK r1118 2026-11-18: 2 BATCH-FAULT; CQ-TSK r1101 2026-11-01: 1 PASS | creativedelight installed 2026-11-21 | BUILT 2026-10-31 (Creative Delight), installed, TSK-01 to TSK-05 pending |
| Gold panning | [gold_panning.md](gold_panning.md) | none | planned | none |  | idea only, not started |
| Mixed jars | [mixed_jars.md](mixed_jars.md) | `MJ-` | BUILT | SQ-JFP r1120nv 2026-11-21: 4 FAIL, 1 INCONCLUSIVE | creativecommon pending, creativecompat pending, creativedelight installed 2026-11-21 | DEPLOYED with 1.1.01 (2026-10-22) |
| Placeable food catalog | [placeable_catalog.md](placeable_catalog.md) | none | reference | none |  | reference, regenerate with `python3 tools/placeable_catalog.py` after `/dumpplaceable` |
| Coaster model candidates | [coaster_model_candidates.md](coaster_model_candidates.md) | none | reference | none |  | ANALYSIS 2026-10-26 |
| Oven | [oven.md](oven.md) | `OV-` | BUILT | SQ-DB r1120nv 2026-11-21: 3 PASS | creativedelight installed 2026-11-21 | first version DEPLOYED with 1.0.7 |
| Remainder buffer | [remainder_buffer.md](remainder_buffer.md), [plan_remainder_buffer_old.md](plan_remainder_buffer_old.md) | `RB-` | BUILT | SQ-STN r1120nv 2026-11-21: 4 PASS, 1 BATCH-FAULT; SQ-RB r1120nv 2026-11-21: 1 BATCH-FAULT, 1 INCONCLUSIVE | creativecommon pending, creativedelight installed 2026-11-21 | BUILT 2026-11-03, scratch test queued (SQ-STN-01 to -05, CQ-STN-01, SQ-RB-21b rerun |
| Item merges | [item_merges.md](item_merges.md) | `MG-` | BUILT | SQ-VTDA r1120nv 2026-11-21: 1 PASS, 3 BATCH-FAULT; SQ-SWP r1120nv 2026-11-21: 2 PASS, 6 BATCH-FAULT; SQ-MG r1120nv 2026-11-21: 2 BATCH-FAULT; +2 codes | creativecommon pending, creativedelight installed 2026-11-21 | 2026-11-02/03: swap-list merges and world half (6) BUILT, patcherMod 1.1.08 and CD jar installed 2026-11-02 ... |
| Texture sources | [texture_sources.md](texture_sources.md) | none | reference | none |  | audit 2026-11-13: 4 swap, 27 redo, 349 confirm |
| Texture reuse | [texture_reuse.md](texture_reuse.md) | none | reference | none |  | research only, 2026-10-22 |
| Kitchen | [kitchen.md](kitchen.md) | `KT-` | BUILT | SQ-KT r1120nv 2026-11-21: 4 PASS; SQ-CFS r1120nv 2026-11-21: 1 PASS, 1 BATCH-FAULT, 1 LOOK; SQ-CBW r1120nv 2026-11-21: 1 BATCH-FAULT; +1 codes | creativecommon pending, creativedelight installed 2026-11-21 | latest: double fridges built 2026-11-16 (kitchen.md 2.15), SQ-KT-29 / CQ-KT-29 queued; history in kitchen.md 8 |
| Stack sizes | [stack_sizes.md](stack_sizes.md) | none | reference | none |  | reference from the 2026-10-02 `/dumpstacks` (2141 items), before the stack size policy (DEPLOYED with 1.0.6): ... |
| Exposure cameras and photographs | [exposure.md](exposure.md) | `EX-` | BUILT | SQ-EX r1120nv 2026-11-21: 1 BATCH-FAULT | exposure in mods/ | DEPLOYED (1.0.3 |
| Alcohol: Brewin' and Chewin' backport | [alcohol.md](alcohol.md) | `AL-` | BUILT | none | creativebrewing installed 2026-11-21 | port `-01` DEPLOYED with 1.0.7 (beside the 2.0 jar |
| Alcohol: Vinery backport | [alcohol_part_b.md](alcohol_part_b.md) | `VB-` | BUILT | none | creativebrewing installed 2026-11-21 | port `-01` DEPLOYED with 1.0.7 (replaces Vinery 1.3.10 + doapi |
| Veggies Delight backport and implementation description | [veggies_delight.md](veggies_delight.md) | `VD-` | BUILT | SQ-VDF r1120lr 2026-11-20: 5 PASS, 6 BATCH-FAULT; SQ-DM r1118 2026-11-18: 1 PASS | creativedelight installed 2026-11-21 | 2026-11-02 night: R7 pancakes BUILT, CD jar installed 2026-11-02 23:16, not booted (SQ-PCK, SQ-CDJ6) |
| Vintage Delight backport and integration plan | [vintage_delight.md](vintage_delight.md) | `VTD-` | BUILT | SQ-VTD8 r1120nv 2026-11-21: 1 FAIL; SQ-VTD6 r1120nv 2026-11-21: 2 PASS; SQ-VTD4 r1120nv 2026-11-21: 3 PASS, 2 BATCH-FAULT; +4 codes | creativedelight installed 2026-11-21 | SQ-VTD6-02 open (hand-check) |
| Miner's Delight merged backport | [miners_delight.md](miners_delight.md) | `MND-` | BUILT | SQ-MDP r1120nv 2026-11-21: 2 PASS; SQ-MDL r1120nv 2026-11-21: 1 BATCH-FAULT; SQ-CUP r1120nv 2026-11-21: 1 PASS, 3 BATCH-FAULT; +1 codes | creativedelight installed 2026-11-21, creativeworldgen installed 2026-11-21 | 2026-11-02 night: loot modifiers replaced by pack loot rules (8.1) BUILT, CD jar installed 2026-11-02 23:16 ... |
| Creative Delight | [creative_delight.md](creative_delight.md) | `CDT- (19.2)`, `CDQ- (22.4)` | BUILT | SQ-WCD r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT; SQ-MIN r1120nv 2026-11-21: 1 BATCH-FAULT; SQ-CDQ r1120nv 2026-11-21: 3 PASS, 3 BATCH-FAULT; +11 codes | creativecommon pending, creativedelight installed 2026-11-21 | 2026-11-10: content moved to the `creativedelight` namespace ([plan_cd_namespace.md](plan_cd_namespace.md)), ... |
| Creative Brewing | [creative_brewing.md](creative_brewing.md) | `CBR-` | BUILT | SQ-CBB r1119 2026-11-19: 1 PASS; SQ-CBO r1118 2026-11-18: 5 PASS, 37 BATCH-FAULT; SQ-CBJ r1118 2026-11-18: 1 PASS, 1 BATCH-FAULT, 1 HARNESS; +3 codes | creativecommon pending, creativebrewing installed 2026-11-21 | BUILT 2026-10-31, not installed (`creativebrewing-1.0.0.jar`: Vinery `-08`, Brewin' `-05`) |
| Creative Brewing publish prep | [plan_cb_publish.md](plan_cb_publish.md), [plan_cb_polish.md](plan_cb_polish.md) | `SQ-DCM-`, `SQ-CBS-`, `SQ-CDFO-`, `CQ-CBS-`, `CQ-CDFO-`, `SQ-CBA-`, `CQ-CBA-`, `SQ-CBO-`, `CQ-CBO-`, `SQ-CBP-`, `CQ-CBP-`, `SQ-CDFL-` | BUILT | SQ-DCM r1120nv 2026-11-21: 1 PASS; SQ-CBS r1120nv 2026-11-21: 1 PASS; SQ-ENGM r1118 2026-11-18: 3 PASS, 2 BATCH-FAULT; +3 codes | creativecommon pending, creativedelight installed 2026-11-21, creativebrewing installed 2026-11-21 | Chain-alone set SQ-CBA / CQ-CBA queued 2026-11-06 (CEPortTesting, scratch test queued) |
| Creative Calendar | [creative_calendar.md](creative_calendar.md) | `SQ-CAL`, `CAL-` | BUILT | SQ-CAL r1119 2026-11-19: 2 LOOK; CQ-CAL r1101 2026-11-01: 1 OPEN | creativedelight installed 2026-11-21, creativebrewing installed 2026-11-21, creativecalendar pending | carved 2026-11-01, installed |
| Creative Compat | [creative_compat.md](creative_compat.md) | `SQ-CMP`, `CMP-` | BUILT | SQ-CMP r1120nv 2026-11-21: 4 PASS, 2 BATCH-FAULT; SQ-BPT r1120nv 2026-11-21: 1 PASS | creativecommon pending, creativecompat pending | carved 2026-11-01, installed |
| Creative Devtools | [creative_devtools.md](creative_devtools.md) | `CDV-` | BUILT | SQ-TX r1120nv 2026-11-21: 5 PASS; SQ-CDV r1119 2026-11-19: 1 LOOK; CQ-TXR r1116 2026-11-16: 1 DEFERRED | creativecommon pending, creativedevtools installed 2026-11-20 | carved 2026-11-01, installed |
| Structure Catalog world type | [plan_structure_catalogue.md](plan_structure_catalogue.md) | `SCAT-` | BUILT | none | creativedevtools installed 2026-11-20 | client rows not run) |
| Creative Worldgen | [creative_worldgen.md](creative_worldgen.md) | `SQ-CWG`, `CWG-` | BROKEN | SQ-CWG r1120nv 2026-11-21: 2 PASS, 1 FAIL, 5 BATCH-FAULT | creativecommon pending, creativeworldgen installed 2026-11-21 | SQ-CWG-08 / -08o open (old-world rerun) |
| Creative Loot | [creative_loot.md](creative_loot.md) | `SQ-LOOT`, `LOOT-` | BUILT | SQ-LOOT r1120nv 2026-11-21: 6 PASS, 2 BATCH-FAULT | creativecommon pending, creativeloot pending | split out of Creative Worldgen 2026-11-17 (plan_rules_fixes.md 5b); member fragments and the Delight drop rules since P3c; SQ-LOOT queued |
| Creative Magic | [creative_magic.md](creative_magic.md) | `SQ-CMG (tests in nether_portal.md NP-`, `recipes_magic.md RA-)` | BUILT | none | creativecommon pending, creativemagic installed 2026-11-21 | built and installed 2026-11-04 19:17 with patcherMod 1.1.09, not booted |
| Creative Equipment | [creative_equipment.md](creative_equipment.md) | `SQ-CEQ`, `CQ-CEQ` | BUILT | SQ-CEQ r1120nv 2026-11-21: 4 PASS, 1 BATCH-FAULT, 1 LOOK; SQ-CBP r1120nv 2026-11-21: 1 PASS, 1 BATCH-FAULT | creativecommon pending, creativeequipment installed 2026-11-21 | built and installed 2026-11-04 19:17 with patcherMod 1.1.09, not booted |
| Creative Engineering and Creative Common | [creative_engineering.md](creative_engineering.md) | `SQ-CEN`, `CQ-CEN`, `SQ-CC`, `SQ-CPP` | BUILT | SQ-CMV r1120nv 2026-11-21: 1 PASS; SQ-CEN r1120nv 2026-11-21: 4 PASS, 1 BATCH-FAULT; SQ-CCO r1120nv 2026-11-21: 6 FAIL; +14 codes | creativecommon pending, creativeengineering installed 2026-11-21 | carved and swapped 2026-11-04 |
| Creative Split | [creative_split.md](creative_split.md), [plan_owner_ids.md](plan_owner_ids.md), [plan_cd_namespace.md](plan_cd_namespace.md), [namespace_move.md](namespace_move.md), [split/README.md](split/README.md), [creative_magic.md](creative_magic.md), [creative_computers.md](creative_computers.md) | none | BUILT | SQ-UPG r1120nv 2026-11-21: 11 PASS, 5 BATCH-FAULT; SQ-SWL r1120nv 2026-11-21: 1 PASS; SQ-NSM r1120nv 2026-11-21: 1 PASS, 1 BATCH-FAULT; +9 codes | creativecommon pending, creativedelight installed 2026-11-21, creativebrewing installed 2026-11-21, creativedevtools installed 2026-11-20, creativeengineering installed 2026-11-21, creativemagic installed 2026-11-21 | waves 1 to 8 carved 2026-11-01 to 2026-11-04 |
| End's Delight and Ender's Delight | [ends_delight.md](ends_delight.md) | `EDL-` | BUILT | SQ-ENM r1120nv 2026-11-21: 2 PASS, 4 BATCH-FAULT; SQ-DNR r1120nv 2026-11-21: 2 BATCH-FAULT; SQ-EDL r1120lr 2026-11-20: 3 PASS, 1 FAIL, 1 BATCH-FAULT | creativedelight installed 2026-11-21 | SQ-ENM-05b waits for the -03 fix |
| Salt and Create: The Salt as Creative Delight repack members | [salt.md](salt.md) | `SLT-` | BUILT | SQ-SLT r1120nv 2026-11-21: 2 PASS, 3 BATCH-FAULT; SQ-SALT2 r1120nv 2026-11-21: 2 PASS, 5 BATCH-FAULT; SQ-PS15 r1118 2026-11-18: 2 PASS, 6 BATCH-FAULT | creativedelight installed 2026-11-21 | 2026-11-02 night: BUILT, nested in the installed CD jar (23:16 |
| Melting | [melting.md](melting.md) | `SQ-MELT-` | BUILT | SQ-MELT r1118 2026-11-18: 11 BATCH-FAULT | creativecommon pending | 2026-11-15: -08 closed by design (a click places a layer) |
| Golden Food as a Creative Delight donor member | [golden_food.md](golden_food.md) | `GLD-` | planned | SQ-GLD2 r1120nv 2026-11-21: 1 PASS, 1 FAIL, 1 BATCH-FAULT; SQ-GLD r1120nv 2026-11-21: 5 PASS, 1 FAIL, 6 BATCH-FAULT | creativedelight installed 2026-11-21 | owner decisions in FINAL.md (stack 16, golden cake candle, pumpkin pie values, village loot) |
| Ocean's Delight and Oceanic Delight | [ocean_delight.md](ocean_delight.md) | `DA-` | BUILT | SQ-FN r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT; SQ-AD r1120nv 2026-11-21: 1 PASS | creativecommon pending, creativedelight installed 2026-11-21 | 2026-11-03: sea grape hoe tag, wild sea grape rule, Oceanic Comfort / Nourishment by rule (8) BUILT, CD jar ... |
| Mineral Delight 2.9 | [mineral_delight.md](archive/mineral_delight.md) | `MIN-` | reference | none |  | archived 2026-10-30: will not be ported, features will not be added |
| Roll Delight 1.3.1 | [roll_delight.md](roll_delight.md) | `ROLL-` | BUILT | SQ-ROLL r1120nv 2026-11-21: 1 PASS, 1 FAIL | creativedelight installed 2026-11-21 | BUILT 2026-11-01 (CD member `backportedRoll`, scripts) |
| Large Meals 1.3.0 | [large_meals.md](large_meals.md) | `LGM-01..20 drafted; scratch SQ-LGM-01..10` | BUILT | SQ-LGM r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT | creativedelight installed 2026-11-21 | BUILT 2026-11-01 (member `backportedLargeMeals`, CD scripts, Display Delight `lm_`) |
| Seasonings and sauces catalog | [seasonings.md](seasonings.md) | none | BUILT | none | creativedelight installed 2026-11-21 | 2026-11-03: Barbeque's seasonings folded into the pack framework (barbeques_delight.md 4.5) BUILT, CD jar ... |
| Barbeque's Delight 1.3.0 | [barbeques_delight.md](barbeques_delight.md) | `BBQ-` | BUILT | SQ-BBQW r1120nv 2026-11-21: 1 BATCH-FAULT; SQ-BBQS r1120nv 2026-11-21: 3 PASS; SQ-BBQF r1120nv 2026-11-21: 7 BATCH-FAULT; +1 codes | creativedelight installed 2026-11-21 | 2026-11-03: fold (4.5) BUILT, CD jar installed 2026-11-03 00:08, not booted |
| Carnivore 1.5.0 | [carnivore.md](archive/carnivore.md) | `CRN-` | reference | none |  | archived 2026-10-30: will not be ported, features will not be added |
| Nether's Delight 4.0 update | [nethers_delight.md](nethers_delight.md) | none | BUILT | SQ-NDM r1120lr 2026-11-20: 6 PASS, 1 LOOK | creativedelight installed 2026-11-21 | BUILT 2026-11-01 as CD member `repackedNethers` (4.9), installed in `mods/` 2026-11-02, not deployed |
| Farmer's Delight 1.3.4 update | [farmers_delight.md](farmers_delight.md) | none | BUILT | SQ-FDB r1120nv 2026-11-21: 8 PASS, 1 FAIL, 4 BATCH-FAULT | creativedelight installed 2026-11-21 | check by hand first) |
| Comfort vs Nourishment audit | [comfort_nourishment.md](comfort_nourishment.md) | `CNS-` | BUILT | SQ-CNS r1120nv 2026-11-21: 1 PASS, 2 BATCH-FAULT | creativedelight installed 2026-11-21 | Audit DONE 2026-10-26 |
| Rustic Delight 1.7.0 backport in Creative Delight | [rustic_delight.md](rustic_delight.md) | `RD-` | BUILT | SQ-RDX r1120nv 2026-11-21: 10 PASS, 1 BATCH-FAULT, 1 OWNER; SQ-RDB r1120nv 2026-11-21: 1 BATCH-FAULT; SQ-RD r1120nv 2026-11-21: 15 PASS, 1 FAIL, 3 BATCH-FAULT, 2 INCONCLUSIVE | creativedelight installed 2026-11-21 | 2026-11-03: one potato salad, Comfort rule recheck (5.12) BUILT, CD jar installed 2026-11-03 00:08, not booted |
| Fruits Delight 1.1.3 fixes on the installed 0.5.11 | [fruits_delight.md](fruits_delight.md) | `FR-` | BUILT | SQ-FR r1120nv 2026-11-21: 4 PASS, 2 BATCH-FAULT | creativedelight installed 2026-11-21 | DEPLOYED with 1.1.03 (in the CD jar) |
| Fruit reference | [fruit.md](fruit.md) | none | planned | none |  | reference: fruit effects, slices, berry colors, Fruits Delight length ratios |
| Smoothies and brain freeze | [smoothies.md](smoothies.md) | none | planned | none |  | planned 2026-11-18, plan_smoothies.md |
| Farmer's Respite 2.1.2 update | [farmers_respite.md](farmers_respite.md) | `RSP-` | BUILT | SQ-RSP r1120nv 2026-11-21: 6 PASS, 1 FAIL, 6 BATCH-FAULT | creativecommon pending, creativedelight installed 2026-11-21 | Spout / drain fix in patcherMod 1.1.07 (not deployed) |
| Drink rules: juice, tea, latte and coffee | [creative_delight_drinks.md](creative_delight_drinks.md) | `SQ-DRK-` | BUILT | SQ-DRK r1120nv 2026-11-21: 6 PASS, 2 BATCH-FAULT | creativedelight installed 2026-11-21 | TESTING: built 2026-11-02 (section 8), in the `mods/` Creative Delight jar |
| Food and cooking batch 2026-11-02 | [plan_food_cooking_batch.md](plan_food_cooking_batch.md) | `SQ-FCB-` | planned | SQ-FCB r1120nv 2026-11-21: 6 PASS, 3 BATCH-FAULT | creativedelight installed 2026-11-21 | TESTING: built 2026-11-02 ("Built") |
| Large Meals, Roll Delight and Barbeque's Delight changes 2026-11-04 | [plan_meals_rolls_bbq.md](plan_meals_rolls_bbq.md) | `SQ-LGM-11..14`, `SQ-ROLL-10..12`, `SQ-BBQ2-`, `CQ-BBQ2-01` | BUILT | SQ-BBQ2 r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT, 2 LOOK | creativedelight installed 2026-11-21 | BUILT 2026-11-04 (CD jar built, not installed), not run |
| Display Delight 1.7.0 port | [display_delight.md](display_delight.md) | `DDP-` | BUILT | SQ-DDP r1118 2026-11-18: 1 BATCH-FAULT, 3 HARNESS | creativecommon pending, creativedelight installed 2026-11-21 | patcherMod 1.1.07 (18:41, with the swap-list `removed` key) is installed |
| Delightful 3.8.1 update | [delightful.md](delightful.md) | none | BUILT | SQ-DF r1120nv 2026-11-21: 6 PASS | creativedelight installed 2026-11-21 | 2026-11-03: cantaloupe names in their own lang namespace (5) BUILT, CD jar installed 2026-11-03 00:08, not ... |
| Corn Delight 1.2.11 update | [corn_delight.md](corn_delight.md) | `CORN-` | BUILT | SQ-CORN r1120nv 2026-11-21: 6 PASS, 1 FAIL, 2 BATCH-FAULT | creativedelight installed 2026-11-21 | DEPLOYED with 1.1.03 |
| More Delight 26.05.20 update | [more_delight.md](more_delight.md) | none | BUILT | SQ-MRD r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT | creativedelight installed 2026-11-21 | DEPLOYED with 1.1.03 (knives, fast toasts, Cheese Toast, singular tags) |
| Abnormals Delight 6.0.3 update | [abnormals_delight.md](abnormals_delight.md) | `ABD-` | BUILT | SQ-ABD r1120nv 2026-11-21: 3 PASS, 2 BATCH-FAULT | creativedelight installed 2026-11-21 | DEPLOYED with 1.1.03: slice XP, salvaging, Adzuki Curry grid craft, two-pickerelweed recipes, 4-bottle milk |
| Storage Delight 26.10.05 update | [storage_delight.md](storage_delight.md) | none | BUILT | SQ-STD r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT | storagedelight in mods/ | `mod-additions #33` |
| Farmer's Structures 1.0.6 donor | [farmers_structures.md](farmers_structures.md) | none | BUILT | none | creativecommon pending | cook house pool names TODO row 30 |
| Sky Village addon houses | [sky_village_addons.md](sky_village_addons.md) | `SVA-` | BUILT | SQ-SVA r1101 2026-11-01: 1 PASS | kubejs | BUILT 2026-10-30 (data only, needs a restart): 17 templates, 8 houses at weight 20 + 9 Brewery deco, Brewery ... |
| devmods evaluation | [devmods_evaluation.md](devmods_evaluation.md) | none | BUILT | none | creativedelight installed 2026-11-21 | plan 2026-10-25 |
| Hostile Delight integration plan | [hostile_delight.md](hostile_delight.md) | `HTD-` | BUILT | none | creativedelight installed 2026-11-21 | planned (2026-10-23) |
| Unusual Delight overlap survey | [unusual_delight.md](unusual_delight.md) | `UD-` | BUILT | none |  | survey 2026-10-30, nothing built |
| Hearth and Harvest plan | [hearth_and_harvest.md](hearth_and_harvest.md), [plan_hearth_crow_swap.md](plan_hearth_crow_swap.md) | `HNH-` | BUILT | SQ-WCT r1120nv 2026-11-21: 3 PASS; SQ-HOP r1120nv 2026-11-21: 2 PASS, 1 FAIL; SQ-HCS r1120nv 2026-11-21: 1 PASS, 1 BATCH-FAULT; +8 codes | creativedelight installed 2026-11-21 | Crow swap built 2026-11-10, not installed (4.7: H&H crow replaces AM's, nests make crows, tree-top and roof ... |
| Create: Power Grid overlap, port and integration report | [power_grid.md](power_grid.md) | `PWG-` | BUILT | SQ-PWG r1120nv 2026-11-21: 9 PASS, 3 BATCH-FAULT; CQ-PWG r1115 2026-11-15: 1 INCONCLUSIVE | powergrid in mods/ | in progress: port phases P0 to P10 built 2026-11-13 (`development/backportedPowerGrid`, solver core with ... |
| Energy basic mode | [energy_basic.md](energy_basic.md) | none | reference | none |  | reference (2026-11-12) |
| Energy advanced mode | [energy_advanced.md](energy_advanced.md) | `(tests in power_grid.md PWG-)` | BUILT | none | powergrid in mods/ | planned, nothing built (2026-11-12) |
| Cozy's Improved Cats and Domestication Innovation overlap and integration report | [cozy_cats.md](cozy_cats.md) | `CIC-` | BUILT | SQ-CAT r1120nv 2026-11-21: 5 PASS | creativecompat pending, creativeworldgen installed 2026-11-21 | rest of the fold-in planned |
| Extra Delight overlap and integration report | [extra_delight.md](extra_delight.md) | `EXD-` | BUILT | none |  | survey 2026-10-30, re-scan 2026-11-08, owner rulings R1 to R5 2026-11-09 (all stations, furniture and decor ... |
| Fungi Delight backport | [fungi_delight.md](fungi_delight.md) | `FGD-` | BUILT | SQ-FGD r1118 2026-11-18: 9 PASS, 2 HARNESS, 1 INCONCLUSIVE | creativecommon pending, creativedelight installed 2026-11-21 | 2026-11-03: cow names, Weak Stomach JEED provider BUILT, CD jar installed 2026-11-03 00:08, not booted |
| Storage audit | [storage_audit.md](storage_audit.md) | none | BUILT | none | creativecommon pending, creativecompat pending | audit 2026-10-19, decisions open |
| Backpack skins | [backpack_skins.md](backpack_skins.md) | `BP-` | BUILT | none | creativeequipment installed 2026-11-21 | DEPLOYED with 1.1.01, untested in game |
| Lunchboxes | [lunchboxes.md](lunchboxes.md) | `LB-` | BUILT | SQ-LB r1118 2026-11-18: 1 PASS, 1 BATCH-FAULT; CQ-LB r1101 2026-11-01: 1 PASS | creativedelight installed 2026-11-21 |  |
| Raspberry Pie | [raspberry_pie.md](raspberry_pie.md) | `RPI-` | BUILT | SQ-RPI r1120nv 2026-11-21: 2 PASS, 6 BATCH-FAULT | creativedelight installed 2026-11-21, creativebrewing installed 2026-11-21, solraspberrypie pending | 12.6 diet books polish BUILT 2026-11-19 (plan_diet_books.md, look-check pending); 12.4 owner notes BUILT 2026-11-13 (unique-food gate, reset command, windowed bar with icons, double page, ... |
| Unused textures | [unused_textures.md](unused_textures.md) | none | reference | none |  | audit 2026-10-22, nothing added yet |
| Crops | [crops.md](crops.md) | `CR-` | BUILT | SQ-PSF r1120nv 2026-11-21: 6 PASS, 3 BATCH-FAULT; SQ-PSD r1120nv 2026-11-21: 1 PASS, 1 FAIL, 1 BATCH-FAULT; SQ-CS r1118 2026-11-18: 5 PASS, 2 BATCH-FAULT, 1 INCONCLUSIVE | creativedelight installed 2026-11-21, creativecalendar pending | reference + tobacco parity, per-hoe areas and wild-crop placement DEPLOYED with 1.0.4 |
| Crop yields | [crop_yields.md](crop_yields.md), [plan_crop_yields_wave2.md](plan_crop_yields_wave2.md) | `SY-` | BUILT | SQ-SY r1120nv 2026-11-21: 6 PASS; CQ-SY r1115 2026-11-15: 1 PASS | creativedelight installed 2026-11-21 | BUILT 2026-10-28, reworked 2026-11-03 (installed) |
| Crops by biome | [crops_by_biome.md](crops_by_biome.md) | none | reference | none |  | reference, regenerate with `python3 tools/gen_crop_worldgen_table.py` |
| Crop growth seasons audit | [crop_seasons_audit.md](crop_seasons_audit.md) | none | reference | none |  | reference regenerated 2026-11-02 (grasses typed |
| Botany Pots | [botany_pots.md](botany_pots.md) | `BOT-` | BUILT | SQ-BOT r1120nv 2026-11-21: 3 PASS, 1 BATCH-FAULT | creativecommon pending, creativecompat pending | BUILT 2026-11-07 (script + data |
| Wild flowers | [flowers.md](flowers.md) | `FL-` | BUILT | SQ-FCK r1120nv 2026-11-21: 13 PASS, 3 BATCH-FAULT; CQ-FF r1031 2026-10-31: 1 PASS | creativeworldgen installed 2026-11-21 | DEPLOYED with 1.0.6 (2026-10-14 |
| Flowers by biome | [flowers_by_biome.md](flowers_by_biome.md) | none | reference | none |  | reference, regenerate with `python3 tools/gen_wild_flowers.py` |
| Flower pots and planters | [flower_pots.md](flower_pots.md), [flower_pots_matrix.md](flower_pots_matrix.md) | `POT-` | BUILT | SQ-POT r1120nv 2026-11-21: 1 PASS | creativecompat pending, creativebrewing installed 2026-11-21, creativedevtools installed 2026-11-20 | audit 2026-10-27 (static) |
| Weather: season types for Serene Seasons | [weather.md](weather.md) | `WE-` | BUILT | none | creativecalendar pending | DEPLOYED with 1.0.6 (Creative Common 1.0.5 + 1.0.6 code |
| Weather by biome | [weather_by_biome.md](weather_by_biome.md) | none | reference | none |  | reference, regenerate with `python3 tools/gen_weather_table.py` after `/dumpbiomes` or `/dumpclimate` |
| Time system | [time.md](time.md) | `TI-` | BUILT | SQ-TI r1120nv 2026-11-21: 1 PASS, 1 FAIL; SQ-PST r1120nv 2026-11-21: 4 PASS, 4 FAIL, 1 HARNESS; SQ-TD r1118 2026-11-18: 3 PASS, 1 BATCH-FAULT; +1 codes | creativecalendar pending | DEPLOYED with 1.0.8 (2026-10-19, Creative Common 1.0.8): master, push, sun seasons, pause when empty, GUI, ... |
| Advancements switched off | [advancements.md](advancements.md) | `AV-` | BUILT | SQ-AV r1120nv 2026-11-21: 1 BATCH-FAULT | creativecommon pending | DEPLOYED with 1.0.8 (2026-10-19 |
| Player changelog | [changelog.md](changelog.md) | `CL-` | BUILT | none | creativecommon pending | DEPLOYED with 1.1.01 (2026-10-22), 1.1.02 to 1.1.04 notes shipped |
| Loading screen tips | [loading_tips.md](loading_tips.md), [plan_creative_tips.md](plan_creative_tips.md), [plan_loading_tips_screens.md](plan_loading_tips_screens.md), [loading_tips_audit.md](loading_tips_audit.md) | `LTP-` | BUILT | SQ-QN r1118 2026-11-18: 2 PASS | creativecommon pending | BUILT 2026-11-06 in creativecommon-1.1.0 (not installed) |
| Toasts | [toasts.md](toasts.md) | `TO-` | BUILT | none | creativecompat pending, exposure in mods/ | DEPLOYED with 1.1.01 (Creative Common 1.1.01, 2026-10-22) |
| Creative Labels: sticky notes and labels as face paper | [sticky_notes.md](sticky_notes.md) | `SN-` | BUILT | SQ-LBS r1120nv 2026-11-21: 4 PASS, 1 BATCH-FAULT | creativelabels installed 2026-11-17 | DEPLOYED with 1.1.01 |
| Names | [names.md](names.md) | `NM-` | BUILT | none | creativecommon pending | audit DEPLOYED with 1.1.01 (436 fixes |
| JEI coverage gaps | [jei_coverage.md](jei_coverage.md) | `JC-` | BUILT | none | creativecommon pending, creativecompat pending, creativedevtools installed 2026-11-20, kubejs | part 1 (crafting) generated, regenerate with `python3 tools/jei_coverage.py` after `/dumpcraftingaudit` |
| JEI world-interaction tabs | [jei_world_tabs.md](jei_world_tabs.md) | `JW-` | BUILT | none | creativecompat pending | DEPLOYED with 1.1.01 |
| Wandering Trader + Wandering Winemaker | [wandering_trader.md](wandering_trader.md) | `WT-` | BUILT | SQ-WT r1118 2026-11-18: 1 BATCH-FAULT | creativecommon pending, creativebrewing installed 2026-11-21 | DEPLOYED with 1.0.4 |
| Trade sync | [trade_sync.md](trade_sync.md) | `TS-` | BUILT | none | creativecommon pending | BUILT 2026-11-03 (patcherMod, installed in `mods/`, not booted |
| Wandering Trader catalog | [wandering_trader_catalog.md](wandering_trader_catalog.md) | none | reference | none |  | reference, regenerate with `python3 tools/wandering_trader_report.py` after `/dumptrades` |
| Shields | [shields.md](shields.md) | `SH-` | BUILT | none | creativeequipment installed 2026-11-21 | DEPLOYED, in-game test pending (SH-06 passed) |
| Armor and tool effects | [armor_effects.md](armor_effects.md) | `AE-` | BUILT | SQ-EFX r1120nv 2026-11-21: 4 PASS | creativecommon pending, creativeequipment installed 2026-11-21 | DEPLOYED (since 1.0.3 |
| Gear tuning | [gear_tuning.md](gear_tuning.md) | `GR-` | BUILT | SQ-AC r1120lr 2026-11-20: 1 BATCH-FAULT | creativeequipment installed 2026-11-21 | DEPLOYED (since 1.0.3), in-game test pending (GR-01 to GR-30) |
| Potions and brewing | [potions.md](potions.md) | `PO-` | BUILT | none | creativebrewing installed 2026-11-21, creativecommon pending | DEPLOYED (built 2026-09-06 to 2026-09-08, shipped since 1.0.3), in-game test pending (PO-01 to PO-26) |
| Potion buckets and tipped arrows | [potion_buckets.md](potion_buckets.md) | `PB-` | BUILT | SQ-LE r1120nv 2026-11-21: 4 PASS, 1 FAIL | creativebrewing installed 2026-11-21 | DEPLOYED (since 1.0.3), in-game test pending (PB-01 to PB-20) |
| Effects | [effects.md](effects.md) | `EF-` | reference | none |  | reference, written 2026-10-19 (140 effects) |
| Enchantment power | [enchant_power.md](enchant_power.md) | `EP-` | reference | none |  | audit written 2026-10-31, nothing built |
| Wise Oak | [wise_oak.md](wise_oak.md) | `WO-` | BUILT | SQ-WO r1118 2026-11-18: 13 PASS, 17 BATCH-FAULT | creativeworldgen installed 2026-11-21 | built 2026-11-09 with high-trust gifts and `/wiseoak trust`, Creative Worldgen installed in `mods/`, scratch ... |
| Visceral Heap | [visceral_heap.md](visceral_heap.md) | `VH-` | planned | SQ-FLR r1118 2026-11-18: 5 PASS, 1 BATCH-FAULT; SQ-VH r1115 2026-11-15: 10 PASS, 2 OTHER | creativeworldgen installed 2026-11-21 | 2026-11-14: all flesh (plan_bugfix_1114.md 6.4) BUILT, Creative Worldgen installed |
| Archery and bullets | [archery.md](archery.md) | `AR-` | BUILT | SQ-AR r1120nv 2026-11-21: 1 LOOK | creativeequipment installed 2026-11-21, creativecommon pending | DEPLOYED (since 1.0.3), in-game test pending (AR-01 to AR-18) |
| Pack tooling | [pack_tooling.md](pack_tooling.md) | `TL-` | BUILT | SQ-INV r1120nv 2026-11-21: 10 PASS; SQ-NCH r1118 2026-11-18: 1 INCONCLUSIVE; SQ-TL r1101 2026-11-01: 1 OPEN | creativedevtools installed 2026-11-20, creativecommon pending | in use |
| Client capture | [client_capture.md](client_capture.md) | `CC-` | BUILT | CQ-TXC r1116 2026-11-16: 10 DEFERRED | creativedevtools installed 2026-11-20 | BUILT 2026-11-15 (driver, runner, cropper, channel, qrun driver transport, first specs), scratch test queued ... |
| WTHIT lines | [wthit_lines.md](wthit_lines.md) | `WL-` | BUILT | SQ-WT r1118 2026-11-18: 1 BATCH-FAULT; SQ-SG r1118 2026-11-18: 3 PASS | creativecompat pending | DD icons DEPLOYED 1.0.4, Growth 1.0.6 |
| Nether portal ignition | [nether_portal.md](nether_portal.md) | `NP-` | BUILT | SQ-PRT r1120nv 2026-11-21: 4 PASS, 1 FAIL | creativemagic installed 2026-11-21 | DEPLOYED, Flint and Terrasteel still marked temporary |
| Portals | [immersive_portals.md](immersive_portals.md) | `PRT-` | BROKEN | SQ-TP r1120nv 2026-11-21: 2 BATCH-FAULT; SQ-PRT r1120nv 2026-11-21: 4 PASS, 1 FAIL; CQ-PG r1115 2026-11-15: 1 LOOK; +1 codes | creativecompat pending, tempad in mods/ | 2026-11-14: exit lift rebuilt for every portal (plan_bugfix_1114.md 4.1), Creative Compat installed, scratch ... |
| Mob and block tweaks | [mob_block_tweaks.md](mob_block_tweaks.md) | `MT-` | BUILT | SQ-MT r1120nv 2026-11-21: 1 PASS, 1 BATCH-FAULT; SQ-MBX r1120lr 2026-11-20: 1 PASS, 4 BATCH-FAULT, 2 LOOK; SQ-VL r1118 2026-11-18: 1 PASS, 2 BATCH-FAULT; +2 codes | creativeworldgen installed 2026-11-21, creativecompat pending, kubejs | DEPLOYED (trough 1.0.4, bubble columns and Bushier Flowers 1.0.6), MT-01 to MT-12 pending |
| Vine shearing | [vine_shearing.md](vine_shearing.md) | `VS-` | BUILT | SQ-VS r1118 2026-11-18: 3 PASS, 1 BATCH-FAULT | creativecompat pending | BUILT 2026-10-31 (Creative Common 1.1.04, installed 2026-10-31) |
| Immersive Paintings | [immersive_paintings.md](immersive_paintings.md) | `PA-` | BUILT | SQ-PA r1120nv 2026-11-21: 4 PASS | creativecompat pending | DEPLOYED with 1.0.6 (475 paintings, scratch-server verified) |
| JEI ordering | [jei_ordering.md](jei_ordering.md) | `JO-` | BUILT | none | creativecompat pending | steps 1 to 3 DEPLOYED with 1.0.7 (Creative Common sorter + mixins, KubeJS definition/tags, first small tree, ... |
| JEI craftable-first port map | [jei_craftable_first_port.md](jei_craftable_first_port.md) | none | reference | none |  | reference + implementation |
| Loot table islands | [loot_islands.md](loot_islands.md) | none | reference | none |  | analysis only, 2026-10-03 |
| Loot tuning system | [loot_tiers.md](loot_tiers.md) | none | planned | none |  | design only, 2026-11-01, nothing built |
| Nests and egg crates | [nests.md](nests.md) | `NE-` | BUILT | SQ-NEST r1120nv 2026-11-21: 1 BATCH-FAULT, 1 INCONCLUSIVE | creativedelight installed 2026-11-21 | 2026-11-03: wild duck, goose and emu nests (2.4) BUILT, CD jar installed 2026-11-03 00:08, not booted |
| Mooblooms | [mooblooms.md](mooblooms.md) | `MB-` | BUILT | none | creativeworldgen installed 2026-11-21 | More Mooblooms 1.0.0 DEPLOYED with 1.0.6 (first release |
| Mob spawns by biome | [mob_spawns.md](mob_spawns.md) | `MS-` | BUILT | SQ-MOB r1103 2026-11-03: 3 NOT-RUN | creativeworldgen installed 2026-11-21 | DEPLOYED with 1.0.6: 12 tuning rules + 3 spawn conditions + spawn ground |
| Animal breeding item and bee flower audit | [breeding_audit.md](breeding_audit.md) | `SQ-BREED-` | reference | SQ-BREED r1120nv 2026-11-21: 2 PASS |  | audit DONE 2026-11-10 (static) |
| Bee nests | [bee_nests.md](bee_nests.md) | `BN-` | BUILT | none | creativeworldgen installed 2026-11-21 | DEPLOYED with 1.0.6 with the starter rules (scratch-server verified: 44 biomes) |
| Bee nests by biome | [bee_nests_by_biome.md](bee_nests_by_biome.md) | none | reference | none |  | reference, regenerate with `python3 tools/gen_bee_nests.py` |
| Spawns by biome | [spawns_by_biome.md](spawns_by_biome.md) | none | reference | none |  | reference, generated from the 2026-10-09 `/dumpspawns` |
| Monkeys and apes | [monkeys.md](monkeys.md) | none | reference | none |  | reference, decision open (TODO `merges/mobs #1`) |
| Terrain adaptation scan | [terrain_adaptation_scan.md](terrain_adaptation_scan.md) | none | reference | none |  | reference, regenerate with `python3 tools/terrain_adaptation_scan.py` after adding structure mods |
| Engineering | [engineering.md](engineering.md) | `EN-` | BUILT | SQ-EN r1101 2026-11-01: 2 PASS | creativecommon pending, creativeengineering installed 2026-11-21 | DEPLOYED (since 1.0.3), EN-01 to EN-18 pending |
| Item physics | [item_physics.md](item_physics.md) | `IP-` | BUILT | none | creativecompat pending | DEPLOYED (since 1.0.3), IP-01 to IP-14 pending |
| Biome tuning | [biome_tuning.md](biome_tuning.md) | `WG-` | BUILT | none | creativeworldgen installed 2026-11-21 | DEPLOYED (17 edits, new worlds only), WG-01 to WG-08 pending |
| Nether blocks in Overworld generation | [nether_worldgen.md](nether_worldgen.md) | `NW-` | BUILT | none | creativeworldgen installed 2026-11-21, kubejs | scan BUILT 2026-10-19 |
| Nether worldgen scan | [nether_worldgen_scan.md](nether_worldgen_scan.md) | none | reference | none |  | reference, regenerate with `python3 tools/nether_worldgen_scan.py` |
| Nether blocks in Overworld structures | [nether_blocks_scan.md](nether_blocks_scan.md) | none | reference | none |  | reference, regenerate with `python3 tools/nether_blocks_scan.py` |
| Nether block swaps | [nether_swaps.md](nether_swaps.md) | `ST-` | reference | none |  | reference, regenerate with `python3 tools/gen_nether_swaps.py` |
| Meat blocks | [meat_blocks.md](meat_blocks.md) | `ST-` | reference | none |  | reference, regenerate with `python3 tools/meat_block_scan.py` |
| Wood mismatch | [wood_mismatch.md](wood_mismatch.md) | none | reference | none |  | reference, regenerate with `python3 tools/wood_mismatch_scan.py` |
| Pack hygiene | [pack_hygiene.md](pack_hygiene.md) | `PH-` | BUILT | none | creativedelight installed 2026-11-21 | AUDIT 2026-10-27: 55 findings |
| Design rules | [design_rules.md](design_rules.md) | `DR-` | reference | none |  | reference |
| Fixes and compat | [fixes_compat.md](fixes_compat.md) | `FX-` | BUILT | SQ-UC r1118 2026-11-18: 1 PASS | creativecompat pending, creativecommon pending, creativeworldgen installed 2026-11-21, creativeequipment installed 2026-11-21 | DEPLOYED (1.0.3 to 1.0.8, 2.22 with 1.0.8) |
| Third-party bug fixes | [thirdparty_fixes.md](thirdparty_fixes.md) | `SQ-TPF`, `CQ-TPF` | planned | SQ-TPF r1118 2026-11-18: 5 PASS, 4 BATCH-FAULT; CQ-TPF r1114 2026-11-14: 61 NOT-RUN | creativecommon pending | 2026-11-14: Debugify config shipped (9 fixes, plan_bugfix_1114.md 2), row and changelog |
| Jar handle leak | [jar_handle_leak.md](jar_handle_leak.md) | `CQ-FDR-` | VERIFIED | CQ-FDR r1115 2026-11-15: 4 PASS | creativecommon pending | BUILT and installed 2026-11-14 |
| Log warnings | [log_warnings.md](log_warnings.md), [log_warnings_lists.md](log_warnings_lists.md) | `LW-` | reference | none |  | written 2026-10-24 |
| Cans | [cans.md](cans.md) | `CN-` | BUILT | SQ-VNG r1120nv 2026-11-21: 1 PASS, 1 BATCH-FAULT; SQ-CNP r1120nv 2026-11-21: 1 FAIL; SQ-CNG r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT | creativedelight installed 2026-11-21 | 2026-11-02 night: Canned Goods repacked as a CD member (9) BUILT, CD jar installed 2026-11-02 23:16, not ... |
| Composting | [composting.md](composting.md) | `CM-` | BUILT | SQ-CM r1120lr 2026-11-20: 2 PASS, 1 LOOK; SQ-CP r1118 2026-11-18: 1 BATCH-FAULT | creativebrewing installed 2026-11-21, creativecommon pending, creativedelight installed 2026-11-21, creativeengineering installed 2026-11-21 | DEPLOYED (since 1.0.3), CM-01 to CM-08 pending |
| Camera bags | [camera_bags.md](camera_bags.md) | `CB-` | BUILT | none | exposure in mods/ | DEPLOYED (1.0.3, moved 1.0.4), never tested in game |
| TNT shrapnel | [tnt_shrapnel.md](tnt_shrapnel.md) | `TN-` | BUILT | none | creativecompat pending | DEPLOYED (1.0.3 jar), TN-01 to TN-10 pending |
| Engineering recipes | [recipes_engineering.md](recipes_engineering.md) | `RE-` | BUILT | SQ-RE r1120nv 2026-11-21: 3 PASS, 1 BATCH-FAULT | creativedelight installed 2026-11-21, creativeengineering installed 2026-11-21 | DEPLOYED (since 1.0.3), RE-01 to RE-20 pending |
| Metal recipes | [recipes_metal.md](recipes_metal.md) | `RM-` | BUILT | SQ-RM r1120nv 2026-11-21: 3 PASS, 1 BATCH-FAULT | creativeengineering installed 2026-11-21 | DEPLOYED (since 1.0.3), RM-01 to RM-14 pending |
| Wood recipes | [recipes_wood.md](recipes_wood.md) | `RW-` | BUILT | SQ-RW r1118 2026-11-18: 3 PASS, 2 BATCH-FAULT | creativeengineering installed 2026-11-21 | DEPLOYED (since 1.0.3), RW-01 to RW-10 pending |
| Cooking recipes | [recipes_cooking.md](recipes_cooking.md) | `RC-` | BUILT | SQ-RCO r1120nv 2026-11-21: 2 PASS; SQ-RC r1120nv 2026-11-21: 2 PASS, 1 BATCH-FAULT; SQ-PJL r1120nv 2026-11-21: 2 PASS, 2 BATCH-FAULT; +1 codes | creativedelight installed 2026-11-21 | DEPLOYED (since 1.0.3) |
| Magic recipes | [recipes_magic.md](recipes_magic.md) | `RA-` | planned | none | creativemagic installed 2026-11-21 | DEPLOYED (since 1.0.3), RA-01 to RA-08 pending |
| Recipe progression hub | [recipe_progression.md](recipe_progression.md) | `RP-` | BUILT | none | creativecompat pending, creativecommon pending | DEPLOYED (since 1.0.3), RP-01 to RP-08 pending |
| Bugfix run from the 2026-11-11 to 2026-11-14 scratch runs | [plan_bugfix_1114.md](plan_bugfix_1114.md) | `rows named in the plan` | BUILT | SQ-T1 r1120nv 2026-11-21: 1 PASS, 1 BATCH-FAULT, 1 LOOK; SQ-PG r1120nv 2026-11-21: 1 BATCH-FAULT; SQ-HH r1120nv 2026-11-21: 453 PASS, 18 FAIL, 45 BATCH-FAULT; +6 codes | creativedelight installed 2026-11-21, powergrid in mods/ | planned 2026-11-14 |
| Rules violation fix plan | [plan_rules_fixes.md](plan_rules_fixes.md), [test_prefix_index.md](testing/test_prefix_index.md) | `SQ-RFX`, `CQ-RFX`, `RFX-` | BROKEN | SQ-RFX r1120nv 2026-11-21: 10 PASS, 3 FAIL, 6 BATCH-FAULT, 1 HARNESS | creativecommon pending, creativecompat pending, creativemagic installed 2026-11-21, creativelabels installed 2026-11-17, creativeequipment installed 2026-11-21, creativedevtools installed 2026-11-20, creativeworldgen installed 2026-11-21, creativeloot pending, creativeengineering installed 2026-11-21, creativecalendar pending, creativedelight installed 2026-11-21, creativebrewing installed 2026-11-21, exposure in mods/ | P0 to P7 built 2026-11-17 (plan Hand-over); SQ-RFX and CQ-RFX queued |
| Process review and overhaul plan | [process_review.md](process_review.md), [plan_process_overhaul.md](plan_process_overhaul.md) | `PROC-` | BUILT | SQ-SMK r1120nv 2026-11-21: 50 PASS, 2 FAIL, 4 BATCH-FAULT; SQ-PROC r1120nv 2026-11-21: 3 PASS | tools | Phases 1 and 2 built 2026-11-16; Phase 3 planned 2026-11-16 |
| Random-tick audit | [random_tick_audit.md](random_tick_audit.md) | none | reference | none |  | audit 2026-11-07 (plan_farming_cooking_batch.md F2, TODO `farming/crops #15`); moved out of briefs/ 2026-11-17 |
| Unassigned queue sections | [features_audit_2026-11-16.md](status/features_audit_2026-11-16.md) | none | reference | SQ-SGF r1120nv 2026-11-21: 2 PASS; SQ-PSW r1120nv 2026-11-21: 1 PASS, 1 BATCH-FAULT; SQ-PSU r1120nv 2026-11-21: 7 PASS; +29 codes |  | Holding row: queue sections with no single owning feature (mixed owner batches, priority runs) or dropped by the 2026-11-16 audit; reassign each to its feature |
| Glass shards | [glass_shards.md](glass_shards.md) | `GSH-` | BUILT | none | creativecommon pending, creativeloot pending | built 2026-11-21 (plan_wood_bucket_batch_1121) |
<!-- generated:status_index end readme -->

## Release plans

| Release | File | Status |
|---|---|---|
| 1.0.3: deploy install/update modes, server performance report, render desync, item-loss monitor, trash slot, join protection; plus everything else added during development (section 0.1) | [plan_1.0.3.md](plan_1.0.3.md) | DEPLOYED 2026-10-02 (server + clients) |
| 1.0.4: fixes for the 1.0.3 join kick and client crash, camera bags moved to Exposure Extras, crop parity, per-hoe harvest areas, dyeable canned golem, new mods, data refresh | [plan_1.0.4.md](plan_1.0.4.md) | DEPLOYED 2026-10-04 (server + clients) |
| 1.0.5: Incubation Additions (emu/duck/goose nests), More Mooblooms, stack size policy, generated cans, wild flowers, bee nests, mob spawn tuning, season types, water fill, terrain adaptation, meta structures, biome fits, shield gates, tooltip/JEI fixes, new mods (Cool Rain, Pretty Rain, MEED, Breedable Killer Rabbit) | [plan_1.0.5.md](plan_1.0.5.md) | never deployed: folded whole into 1.0.6 |
| 1.0.6: all of 1.0.5, seasons snow line / cold type / permanent skylands seasons, can parity, Immersive Paintings variants, 23 new mods; **structure tuning 7.1 to 7.4 shipped INCOMPLETE** (built, scratch test run partly done, open bugs listed) | [plan_1.0.6.md](plan_1.0.6.md) | DEPLOYED 2026-10-14 (server + clients) |
| 1.0.7: structure and locate fixes from the scratch run, Quark sort fix, remainder buffer, the oven, JEI ordering steps 1 to 3, Vinery port `-01` (replaces 1.3.10), Brewin port `-01` (beside the 2.0 jar), Exposure `-04`, Create: The Salt, Storage Labels | [plan_1.0.7.md](plan_1.0.7.md) | DEPLOYED 2026-10-18 (server + clients); plan written after the fact 2026-10-19 |
| 1.0.8: time master, advancements off, Brewin port `-02` (replaces both 1.0.7 jars) and Vinery port `-02` with the third change lists, baking update (37 oven recipes, hammered bakeware, cornmeal), coaster placeables | [plan_1.0.8.md](plan_1.0.8.md) | DEPLOYED 2026-10-19 10:20 (server + clients; restaged 10:06 with the dedicated-server boot fix and the scratch-run worldgen fixes); server time preset still open (TODO `pack/release #2`) |
| 1.0.9: bug fixes from the old TODO section 3 (oven JEI [+], NEA loot enchantments, felsic shield config, air brake config, two class-load crashes, potion bucket tooltip, recipe and dict fixes), rsec removed, duplicate-jar preflight, nether blocks in Overworld generation, biome rarity floor, Quark spire teleport hook, earth fill on water + camp plinths + `avoidCoasts`; ports `-03` set | [plan_1.0.9.md](plan_1.0.9.md) | DEPLOYED 2026-10-19 15:54 (server + clients); fix checks in game: TODO `pack/release #4` |
| 1.1.01: player changelog, Brewing Update (every change since 1.0.9; notes in `changelog/1.1.01.md`) | [deploy log](../deploy/DEPLOY_LOG.md) | DEPLOYED 2026-10-22 22:39 (server + clients) |
| 1.1.02: Vintage Delight, Veggies Delight and Fungi Delight ports, mooshroom breeding, Sky Structures, spawn and worldgen tuning (notes in `changelog/1.1.02.md`); **does not load** (Vintage placed features), hotfix 1.1.03 built and installed 2026-10-26, not yet staged | [deploy log](../deploy/DEPLOY_LOG.md) | DEPLOYED 2026-10-23 (server + clients; time not recorded) |
| 1.1.03: Vintage load hotfix, Creative Delight member jars, Corn / Miner's / Veggies / Vintage additions and fixes, golden goose egg, patcherMod 1.1.03 (working notes in `kubejs/assets/creativecommon/changelog/unreleased.md`) | [deploy log](../deploy/DEPLOY_LOG.md) | NOT STAGED: all jars built and installed 2026-10-26, game not started since 2026-10-24; boot check `SQ-VP-00` first (TODO `pack/release`) |

## Plans and reference docs

Docs with no index row of their own: batch plans that span features, and plans, audits and reports of one feature.
Status is in each doc's header or Open work block and in the TODO rows, not here.

| Doc | File | Feature |
|---|---|---|
| Batch plan: pre-scratch fix pass (2026-11-02) | [plan_prescratch_fixes.md](plan_prescratch_fixes.md) | several |
| Batch plan: fixes from the 2026-11-02/03 scratch runs | [plan_scratch_bugfix_1103.md](plan_scratch_bugfix_1103.md) | several |
| Batch plan: farming, cooking and homesteading (2026-11-07) | [plan_farming_cooking_batch.md](plan_farming_cooking_batch.md) | several |
| Batch plan: after scratch run r1115 | [plan_post_scratch_1115.md](plan_post_scratch_1115.md) | several |
| Batch plan: small fixes (2026-11-16) | [plan_small_fixes_1116.md](plan_small_fixes_1116.md) | several |
| Scratch testing expansion (inventory, hurdles, proposals) | [plan_scratch_testing_expansion.md](plan_scratch_testing_expansion.md) | [client_capture.md](client_capture.md) |
| BlockRunner audit | [plan_blockrunner_audit.md](plan_blockrunner_audit.md) | [blockrunner.md](blockrunner.md) |
| Pass-through blocks | [plan_passthrough.md](plan_passthrough.md) | [blockrunner.md](blockrunner.md) |
| Pass-through rework after r1115 | [plan_post_scratch_1115b.md](plan_post_scratch_1115b.md) | [blockrunner.md](blockrunner.md) |
| Animal breeding item audit | [plan_breeding_audit.md](plan_breeding_audit.md) | [breeding_audit.md](breeding_audit.md) |
| Creative Delight execution plan (2026-11-02) | [plan_creative_delight.md](plan_creative_delight.md) | [creative_delight.md](creative_delight.md) |
| Rustic Delight baked into its member | [plan_rustic_fold_in.md](plan_rustic_fold_in.md) | [rustic_delight.md](rustic_delight.md) |
| Extra Delight texture comparison | [extra_delight_textures.md](extra_delight_textures.md) | [extra_delight.md](extra_delight.md) |
| Flesh recipes | [plan_flesh_recipes.md](plan_flesh_recipes.md) | [visceral_heap.md](visceral_heap.md) |
| Visceral Heap | [plan_visceral_heap.md](plan_visceral_heap.md) | [visceral_heap.md](visceral_heap.md) |
| Hearth and Harvest: crow and salt lick (crow part superseded) | [plan_hearth_crow_salt.md](plan_hearth_crow_salt.md) | [hearth_and_harvest.md](hearth_and_harvest.md) |
| Food Dictionary and Diet Tracker | [food_dictionary.md](food_dictionary.md) | [food.md](food.md) |
| Item physics family audit | [item_physics_audit.md](item_physics_audit.md) | [item_physics.md](item_physics.md) |
| Loading screen tips, by pack | [loading_tips_list.md](loading_tips_list.md) | [loading_tips.md](loading_tips.md) |
| Custom JEI tab tweaks | [plan_jei_tab_tweaks.md](plan_jei_tab_tweaks.md) | [jei_world_tabs.md](jei_world_tabs.md) |
| Immersive Portals changes | [plan_portal_changes.md](plan_portal_changes.md) | [immersive_portals.md](immersive_portals.md) |
| Portal Gun fixes and portal color dyeing | [plan_portal_gun.md](plan_portal_gun.md) | [immersive_portals.md](immersive_portals.md) |
| Create: Power Grid backport, plan per phase | [plan_power_grid.md](plan_power_grid.md) | [power_grid.md](power_grid.md) |
| Worldgen edit plan (2026-11-01) | [plan_worldgen_edit.md](plan_worldgen_edit.md) | [creative_worldgen.md](creative_worldgen.md) |
| Structure Catalog: family map and its preview | [structure_catalogue_map.md](structure_catalogue_map.md), [structure_catalogue_map_preview.md](structure_catalogue_map_preview.md) | [plan_structure_catalogue.md](plan_structure_catalogue.md) |
| Comment rewrites for owner review | [history_extract_review.md](history_extract_review.md) | [plan_rules_fixes.md](plan_rules_fixes.md) |
| Namespace report (generated) | [namespace_report.md](namespace_report.md) | [plan_rules_fixes.md](plan_rules_fixes.md) |

## Backlog: the rest of the pack, one small chunk at a time

All 21 rows written as of 2026-10-18; each row links its writeup and the index above has a row for each. New
chunks go here as they come up: one writeup-sized chunk per line, prefixes are proposals.

| # | Chunk | Covers | Prefix |
|---|---|---|---|
| 1 | ~~Shields~~ | done: [shields.md](shields.md) | `SH-` |
| 2 | ~~Armor and tool effects~~ | done: [armor_effects.md](armor_effects.md) | `AE-` |
| 3 | ~~Gear tuning~~ | done: [gear_tuning.md](gear_tuning.md) | `GR-` |
| 4 | ~~Potions and brewing~~ | done: [potions.md](potions.md) | `PO-` |
| 5 | ~~Potion buckets and tipped arrows~~ | done: [potion_buckets.md](potion_buckets.md) | `PB-` |
| 6 | ~~Engineering recipes~~ | done: [engineering.md](engineering.md) | `EN-` |
| 7 | ~~Item physics~~ | done: [item_physics.md](item_physics.md) | `IP-` |
| 8 | ~~Loot~~ | done: [loot.md](loot.md) | `LT-` |
| 9 | ~~Biome tuning~~ | done: [biome_tuning.md](biome_tuning.md) | `WG-` |
| 10 | ~~Fixes and compat~~ | done: [fixes_compat.md](fixes_compat.md) | `FX-` |
| 11 | ~~Pack tooling~~ | done: [pack_tooling.md](pack_tooling.md) | `TL-` |
| 12 | ~~Nests~~ | done: [nests.md](nests.md) | `NE-` |
| 13 | ~~Cans~~ | done: [cans.md](cans.md) | `CN-` |
| 14 | ~~Composting~~ | done: [composting.md](composting.md) | `CM-` |
| 15 | ~~Camera bags~~ | done: [camera_bags.md](camera_bags.md) | `CB-` |
| 16 | ~~WTHIT lines~~ | done: [wthit_lines.md](wthit_lines.md) | `WL-` |
| 17 | ~~TNT shrapnel~~ | done: [tnt_shrapnel.md](tnt_shrapnel.md) | `TN-` |
| 18 | ~~Nether portal ignition~~ | done: [nether_portal.md](nether_portal.md) | `NP-` |
| 19 | ~~Mob and block tweaks~~ | done: [mob_block_tweaks.md](mob_block_tweaks.md) | `MT-` |
| 20 | ~~Recipe progression~~ | done: hub [recipe_progression.md](recipe_progression.md), per folder [recipes_engineering.md](recipes_engineering.md), [recipes_metal.md](recipes_metal.md), [recipes_wood.md](recipes_wood.md), [recipes_cooking.md](recipes_cooking.md), [recipes_magic.md](recipes_magic.md) | `RP-` (`RE-`, `RM-`, `RW-`, `RC-`, `RA-`) |
| 21 | ~~Archery and bullets~~ | done: [archery.md](archery.md) | `AR-` |

## Status legend

- **VERIFIED**: every test in the suite passed in game (the date is on the file)
- **DEPLOYED**: built and in `mods/` / scripts live, but not (fully) tested in game. A feature installed in the dev instance but not yet released to players says "installed <date>" next to BUILT; the release itself is recorded in the release plans table and [deploy/DEPLOY_LOG.md](../deploy/DEPLOY_LOG.md)
- **BUILT**: compiles and passes offline checks, not yet deployed
- **built, scratch test queued**: BUILT, with its live check waiting in [testing/scratch_queue.md](testing/scratch_queue.md) for a bulk scratch-server session
- **built, scratch server PASSED**: BUILT, and every check a dedicated scratch server can run passed (probe in `tools/`); the client-side and real-player tests are still open
- **BROKEN**: a test failed; the failing IDs are listed in the file's status block

## Layout every writeup follows

0. **At a glance**: status per part, the files to open, the config knobs. Read this first.
1. **Overview**: what the feature (or the underlying mod) does out of the box.
2. **Reference**: the values and tags, with examples.
3. **What the pack changes**: each customisation, how it's wired and why it was done that way.
4. **Limits and decisions**: known gaps and the options that were rejected, with reasons, so they don't get re-proposed.
5. **File map**
6. **Offline checks**: what can be verified without launching the game.
7. **In-game test suite**: numbered tests (`PREFIX-NN`) with setup, action and expected result.
8. **History**: dated changes.

## Reporting test results

Tick the box in the file, or just tell Claude the ID and what happened. For example:
`BR-12 fail: extended plain piston head still slows me`. Claude updates the checkboxes, the status
block and the History section. The status word is derived (`tools/feature_status.py`, rules in its header): VERIFIED
when every linked TODO row is Complete, DEPLOYED when every jar is in the last deploy, and so on. Set a TODO row's
status, then `python3 tools/status_index.py` writes the word into [status/features.csv](status/features.csv) and the
index. A hand `override` there needs `override_reason` and `override_date`.

## Keeping them current

When a feature changes, or a bug fix or tweak touches it, its writeup changes in the same session: values, file map, new or retired
tests (retire an ID rather than renumbering it) and a History line, plus its TODO row and its row in
[status/features.csv](status/features.csv); then run `python3 tools/status_index.py`. The index table, the TODO snapshot
counts and the queue coverage block are generated: never hand-edit inside their markers or paste run results into them. Tooltip changes always go in
[tooltips.md](tooltips.md), with a cross-link from the feature they belong to. Memory entries stay short and
point here instead of repeating the details.
