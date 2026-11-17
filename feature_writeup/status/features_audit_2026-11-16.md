# Features table audit, 2026-11-16

Plan: [plan_process_overhaul.md](../plan_process_overhaul.md) P3.3 item 2. Source: `features.tsv` at commit afea3f23 (183 rows),
now `features.csv`. Row numbers below are file lines (header = 1). Proposed values for every row (jars, queue codes,
reference, override) are in the agent's `features_proposed.csv`; the migration applies them. Evidence: class and script
locations in `development/*/src` and `kubejs/data/<ns>/kubejs` today (not the writeups' pre-carve paths), queue sections
and row texts in `testing/rows/*.qb`, run notes in the writeups' moved README index entries, TODO rows.

## (a) BROKEN rows (14)

The seed (`status_seed.py`) takes BROKEN from any bold run note containing BROKEN/FAILED, also when a later note or TODO
row says the failure was fixed. 13 of 14 are stale; 1 is a real open break.

| Line | Feature | BROKEN came from | Now | Cause | Proposed |
|---|---|---|---|---|---|
| 26 | Can parity worksheet | r1103 SQ-VNG-01 FAILED | fixed 11-03 (`dishes/ingredients #6`, vinegar alias) | seed error; also a worksheet | `reference` (override, see below); its queue codes move to Cans |
| 47 | Vintage Delight | r1103 SQ-VP-06 FAILED, SQ-VTD6-02 open | VP-06 fixed 11-03 (fork B); VTD6-02 is a hand-check | seed error (stale) | derived (BUILT) |
| 49 | Creative Delight | r1114 SQ-CDW-01/-02/-03b, SQ-MIN-03; r1103 CDFW-07, WCD-02 | r1103 pair fixed 11-03; MIN-03 rerun PASS r1115; CDW fix built 11-14 23:30, not rerun | real at r1114, fix awaiting rerun | derived (BUILT) |
| 56 | Creative Worldgen | r1103 SQ-CWG-06 FAILED | `punch` key fixed 11-03 (`structures/metas #12` Testing) | seed error (stale) | derived |
| 60 | Creative Split | r1114 SQ-DEP-02 | note itself says "fixed, see r1115"; r1115 rerun done | seed error | derived |
| 61 | End's and Ender's Delight | r1103 SQ-ENM-03, -04b FAILED | both fixed and installed 11-03 (`creativedelight/members #46`) | seed error (stale) | derived |
| 64 | Golden Food | r1103 SQ-GLD-03 FAILED | ruled environmental 11-03 (bundle not from the drinks); rerun on a fresh bot | seed error (stale) | derived |
| 70 | Barbeque's Delight | r1114 SQ-BBQW-02s | fixed r1115 (fix-2, CD 03:18; `dishes/meals #67` Testing) | real at r1114, fixed | derived |
| 73 | Farmer's Delight 1.3.4 | r1103 SQ-FDB-05 FAILED | by design (`placeAs` needs a restart) | seed error | derived (DEPLOYED) |
| 77 | Farmer's Respite 2.1.2 | r1103 SQ-RSP-06, -15 FAILED | batch fault, not a pack bug (plan_scratch_bugfix_1103 2.2) | seed error | derived (DEPLOYED) |
| 114 | Time system | r1103 SQ-PST-08/-10/-11/-11b FAILED | rule changed and hooks rebuilt 11-03 (`farming/crops #28` Testing) | seed error (stale) | derived |
| 119 | Creative Labels | r1103 SQ-LBS-03, -04 FAILED | fixed in `creativeLabels-1.0.1` (`largecontent/stickynotes #7` Testing); jar not installed as of that note | seed error (stale); check `jarstamp.py pending` | derived (BUILT) |
| 140 | Portals | r1115 SQ-PG-01c FAIL, -01b | URGENT Bug `gameplay/tnt-and-portal #38` open (client dies lighting a Flint and Terrasteel portal); `#15` Bug | **real break** | override `BROKEN` until #38 closes |
| 169 | Third-party bug fixes | r1114 SQ-TPF-01 (Debugify all-on crash) | r1115 SQ-TPF-00, -01 PASS with the shipped debugify.json | seed error (stale) | derived |

## (b) jars filled (96 empty) and corrected

Empty rows: 34 become `reference` (section d), 9 are `planned` with nothing built and stay empty (29 Dyeing, 32 Gold
panning, 90 Hostile Delight, 91 Unusual Delight, 97 Extra Delight, 103 Unused textures, 132 Enchantment power,
147 Loot tuning, 183 Rules violation plan); 3 are tooling (see decision below); 50 filled:

| Line | Feature | jars | Evidence (where the code is now) |
|---|---|---|---|
| 8 | Locate | creativeworldgen | Locate*, AsyncLocatorBridge |
| 9 | Loot | creativeworldgen, creativedelight, creativecommon | loot engine kjs in worldgen and CD; LootTablesOptionalModsMixin |
| 12 | Performance | creativecommon, creativeworldgen | Perf* classes in Common; biome cache in Worldgen |
| 13 | Perf suite | creativedevtools | perfbot member |
| 19 | Trash slot | creativecompat | TrashSlot* mixins |
| 20 | Fridge sort item loss | creativecompat | QuarkSortingHandlerMixin, QuarkSafeSort |
| 28 | Plantable items | creativecommon, kubejs | plantabletags.js (Common), plantable_tooltip.js (instance) |
| 33 | Mixed jars | creativecompat, creativedelight | Supplementaries jar mixins; jar_tags.js |
| 45 | Alcohol: Vinery backport | creativebrewing | members/vinery |
| 83, 84, 85 | Corn, More, Abnormals Delight updates | creativedelight | CD kjs trees |
| 88 | Sky Village addon houses | kubejs | `kubejs/data/skyvillages` templates only |
| 96 | Cozy's Improved Cats | creativecompat, creativeworldgen | CozyCats, Cat*/Cozy* mixins; PetCozyYarnMixin |
| 99 | Storage audit | creativecompat, creativecommon | SupplementariesSackItemMixin, CreativeMenuDisableMixin |
| 100 | Backpack skins | creativeequipment | Backpack* classes, backpack_skins.js |
| 101 | Lunchboxes | creativedelight | Lunch* classes, lunchboxes.js |
| 105 | Crop yields | creativedelight | crop_drops.js |
| 109 | Wild flowers | creativeworldgen | wild_flowers.js |
| 115 | Advancements switched off | creativecommon | ServerAdvancementManagerMixin |
| 121 | JEI coverage gaps | creativecompat, kubejs | Compat JEI plugin; jeichanges.js |
| 122 | JEI world-interaction tabs | creativecompat | WorldInteractionCategory |
| 123 | Wandering Trader + Winemaker | creativecommon, creativebrewing | WanderingTrader* (Common), WinemakerTrades (Brewing) |
| 126 | Shields | creativeequipment | Shield* |
| 127 | Armor and tool effects | creativecommon, creativeequipment | armor_effects.js, gear_effects.js |
| 128 | Gear tuning | creativeequipment | tune_tools.js |
| 129 | Potions and brewing | creativebrewing, creativecommon | potions.js both; effects are Common's |
| 130 | Potion buckets | creativebrewing | potion_buckets.js |
| 135 | Archery and bullets | creativeequipment | archery.js, bullets.js |
| 136 | Pack tooling | creativedevtools, creativecommon | dump_all.js; export_io.js, java_guard.js |
| 137 | Client capture | creativedevtools | capture member |
| 138 | WTHIT lines | creativecompat | Wthit* |
| 139 | Nether portal ignition | creativemagic | PortalFailureHandler, IPFireGateMixin |
| 141 | Mob and block tweaks | creativeworldgen, creativecompat, kubejs | villager gate/ladder mixins; MonsterBoxBlockEntityMixin; block_properties.js |
| 149, 150, 152, 159 | Mooblooms, Mob spawns, Bee nests, Biome tuning | creativeworldgen | Moobloom*, mob_spawns.js, bee_nests.js, biome_overrides.js |
| 157, 176, 177, 178 | Engineering; engineering, metal, wood recipes | creativeengineering | Engineering jar kjs (metal_*, wood_*) |
| 158 | Item physics | creativecompat | ItemPhysic* |
| 160 | Nether blocks in Overworld generation | creativeworldgen, kubejs | swap lists; `generation_scripts/worldgen/nether_swap_rules.json` |
| 168 | Fixes and compat | creativecompat, creativecommon, creativeworldgen, creativeequipment | mixins listed in the writeup, located per jar |
| 173 | Composting | creativedelight, creativecommon, creativebrewing | compost*.js |
| 175 | TNT shrapnel | creativecompat | TntShrapnelHandler |
| 179 | Cooking recipes | creativedelight | CD kjs |
| 180 | Magic recipes | creativemagic | Magic kjs |
| 181 | Recipe progression hub | creativecompat, creativecommon | dead_recipe_removal.js (Compat), removed_items.js (Common) |

**Stale jars on filled rows** (seeded from `patcherMod` = creativecommon, before the carves):

| Line | Feature | was | proposed | why |
|---|---|---|---|---|
| 2 | BlockRunner | creativedevtools | creativecompat, creativedevtools | BlockRunner classes in Compat; `/dumpsurface` in Devtools |
| 3 | Tooltips | creativecommon | creativecompat, creativecommon | tooltips carved to Compat (SQ-CIF) |
| 4 | Structure tuning | creativecommon | creativeworldgen | Structure* classes |
| 15 | Curios slots | creativecommon | creativeequipment, creativecompat | curiostags.js |
| 16 | Stepstools | creativecommon | creativeequipment | Stepstool* |
| 17 | Feature porting | creativecommon | creativecompat | feature_ports.js |
| 18 | Baby mobs | creativedelight | creativeworldgen | member babymobs |
| 27 | Placeable food | creativecommon | creativedelight | placeable*.js |
| 30 | Furniture storage | creativecommon | creativecompat | Handcrafted nightstand mixins |
| 36 | Oven | creativecommon | creativedelight | Oven* classes |
| 44 | Alcohol: Brewin' backport | creativecommon | creativebrewing | member brewin |
| 111 | Flower pots and planters | creativecommon | creativecompat, creativebrewing | FramedFlowerPotUnframedMixin, LegacyFlowerPot* |
| 112, 114 | Weather, Time system | creativecommon | creativecalendar | Season*/Clock* classes; time is Calendar's |
| 118 | Toasts | creativecommon, exposure | creativecompat, exposure | toast filter in Compat |
| 142 | Vine shearing | creativecommon | creativecompat | VineShearing, VineBlockShearMixin |
| 144 | JEI ordering | creativecommon, creativecompat | creativecompat | JeiOrder* |

**Decision needed: tooling rows.** 14 Resource pack bench, 21 Deploy pipeline, 184 Process review live only in `tools/`.
Proposed as jars `tools` (a new word outside the list); the alternative is `reference`, which would hide their TODO rows.

## (c) queue code links that look wrong

399 links checked: every code exists in `testing/rows/`. 188 match by the row's writeup in the section title or sources;
the rest were read row by row. Batch sections ("Queue", "Run log", "Deep scan", "Gap fill") were judged by their row texts.

| Code | On (line) | Rows test | Proposed |
|---|---|---|---|
| CQ-SY | Tooltips (3) | Rich Soil tooltip | move to Crop yields (105) |
| SQ-LE | Structure tuning (4) | potion mixes, buckets | move to Potion buckets (130) |
| SQ-PERFCFG | Perf suite (13) | DH and ModernFix defaults | move to Performance (12) |
| CQ-FF | Food families (25) | firefly bush biomes | move to Wild flowers (109) |
| SQ-CNP, SQ-VNG | Can parity (26) | can grid guard, vinegar cans | move to Cans (172) |
| SQ-CAT | Item merges (38) | Cozy's Cats mixins and merges | move to Cozy's Cats (96) |
| CQ-CDFO | Kitchen (41) | CB publish prep step 1 | move to CB publish prep (51) |
| CQ-JO | Creative Delight (49) | JEI order report | move to JEI ordering (144) |
| CQ-WT | Creative Delight (49) | WTHIT silk touch, season lines | move to WTHIT lines (138) |
| SQ-FFH | Creative Delight (49) | food family helpers | move to Food families (25) |
| SQ-OPEN | Creative Delight (49) | open rows of many features (LO-, FP-, ...) | drop |
| CQ-PRI | CB publish prep (51) | priority run, mixed | drop |
| CQ-CHK | CE and Common (59) | chat key fix | move to Fixes and compat (168) |
| CQ-CIF | CE and Common (59) | interface carve | move to Creative Split (60), beside SQ-CIF |
| CQ-CDM2, SQ-CDM2, SQ-CDB | Ocean's Delight (65) | cow eggs, cantaloupe, sweeteners | move to Creative Delight (49) |
| SQ-CM | Ocean's Delight (65) | cantaloupe item merges | move to Item merges (38) |
| CQ-NS, SQ-NS | Hearth and Harvest (92) | nightstands | move to Furniture storage (30) |
| CQ-TSK, SQ-TSK | Hearth and Harvest (92) | treated sticks | move to Placed treated sticks (31) |
| CQ-SG, SQ-SGF | Hearth and Harvest (92) | Spout Gun wets farmland | drop (no feature row; owner: own row or Fixes and compat) |
| SQ-OB | Hearth and Harvest (92) | owner batch, mixed | drop |
| SQ-FDI | Lunchboxes (101) | Food Dictionary and Diet Tracker | move to Food (11) |
| SQ-PS15 | Lunchboxes (101) | salt dust layers | move to Salt (62) |
| SQ-GF31 | Time system (114) | sawmill, dispenser gap fill | drop |
| CQ-BBQ2 | Names (120) | seasoning names (BBQ-34) | move to Large Meals/Roll/BBQ changes (80) |
| CQ-NEW | Client capture (137) | Xaero time line, moss colour | drop |
| SQ-MB | Wood recipes (178) | monster box vaults | move to Mob and block tweaks (141) |
| SQ-PSU | Cooking recipes (179) | pre-scratch UI/gear mixins | drop |
| all 49 | Rules violation fix plan (183) | rows the plan renames or cites, none its own (nothing built) | drop all |

Unclear, left in place for the owner: CQ-TX and CQ-LCA on Kitchen (41) (texture fixes on crops; licence compartment
art); SQ-KS on Creative Delight (49) (KubeJS scripts in a datapack, closer to Pack tooling); SQ-PST on Time system (114)
(grass seasons, arguably Creative Calendar 52).

**Code collision:** SQ-CNS names two things: Comfort/Nourishment rows (in FO.qb, RD.qb; Comfort audit, line 74) and the
Creative Common namespace wave 9 section (CNS.qb; CQ-CNS on Creative Split, line 60). One needs a rename in the test
prefix index.

## (d) rows that should be `reference`

Already reference and confirmed (33): 5, 6, 7, 10, 23, 24, 34, 35, 39, 40, 42, 66, 71, 94, 106, 107, 110, 113, 125, 145,
146, 151, 153, 154, 155, 156, 161, 162, 163, 164, 165, 167, 171.

Changes:

| Line | Feature | was | proposed | why |
|---|---|---|---|---|
| 26 | Can parity worksheet | BROKEN, no jars | reference (override, its fix rows are Cans') | worksheet |
| 22 | Vinery JEI pages | reference + jars creativecommon, creativebrewing | reference, jars cleared | removed 2026-10-27, no code |
| 131 | Effects | reference + jar creativecommon | reference, jars cleared | catalog (140 effects); effect code is under Potions |
| 104 | Crops | reference + jars + 6 queue codes | not reference (jars creativedelight, creativecalendar kept) | per-hoe areas, wild crops, tobacco parity are built code |

## (e) other

- No dead file links (every `file` path exists); no duplicate keys. `alcohol.md` backs two rows (44 Brewin', 45 Vinery) by design.
- `test prefix` column holds queue codes instead of a test prefix on lines 51-59, 63, 68, 78-80, 151, 169, 170 (for
  example `SQ-CAL, CAL-`, `SQ-MELT-`, `LGM-01..20 drafted; scratch SQ-LGM-01..10`). Proposal: keep only the doc's test
  prefix there.
- Test prefix collisions between features: LT- (9, 117), PB- (13, 130), RP- (14, 102, 181), TS- (19, 124), EN- (61, 157),
  CN- (83, 172), BP- (100, 108), IP- (140, 158), ST- (4, 5, 163, 164). These are the renames in
  [testing/test_prefix_index.md](../testing/test_prefix_index.md) (rules plan); not changed here.
- Rows whose only jar is `kubejs` (88 Sky Village addons) have no jar stamp, so the DEPLOYED rule cannot fire for them;
  the rule needs a fallback (deploy version of the instance payload) or such rows keep an override.
- Many DEPLOYED rows with "since 1.0.3" (Engineering, recipes, Shields, Archery) now map to jars carved on 11-01 to 11-04;
  their derived status will read BUILT until the next deploy carries those jars, which is accurate for the code but not
  for the feature. Owner: accept, or override DEPLOYED until the next release.

## Applied 2026-11-16 (P3.3)

- jars, queue codes, reference and the two overrides (Portals BROKEN, Can parity reference) applied to
  [features.csv](features.csv); rows the audit made `reference` but that still list TODO ids carry `override=reference`.
- The 57 dropped queue codes sit on a holding row, **Unassigned queue sections** (override `reference`), so the
  queue check keeps passing. Reassign each to its feature (section c above).
- Status words are now derived (`tools/feature_status.py`). Result on 2026-11-16: 1 BROKEN, 135 BUILT, 2 planned,
  44 reference, 2 VERIFIED, 0 DEPLOYED.

## Decisions for the owner

| # | Question | Taken for now |
|---|---|---|
| 1 | Tooling rows (Resource pack bench, Deploy pipeline, Process review) have jar `tools`, a code location without a stamp | kept; `tools`, `kubejs` and `datapack` never make a feature DEPLOYED |
| 2 | Rows whose only location is `kubejs` can never derive DEPLOYED (scripts carry no stamp) | kept; override where wanted |
| 3 | Every own jar except Tempad differs in content from the 1.2.02 payload (reinstalled 2026-11-16, and most changed since 11-09), so the 62 features marked DEPLOYED derive BUILT until the next release | rule kept, no blanket override; `build_deploy.py --snapshot` now records stamp and content digest per own jar, so the next release sets DEPLOYED exactly |
| 4 | Holding row for 57 queue codes | reassign per section c |
