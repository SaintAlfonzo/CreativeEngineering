# Nether blocks in Overworld generation (scan)

Written by `python3 tools/nether_worldgen_scan.py`. Do not edit. The decision table built from it is [nether_worldgen.md](nether_worldgen.md). Biome shares are `result_pct` from `biome_config.json` (share of the Overworld after biome tuning; 0 = never hit in the climate samples).

## 1. Surface rules (Terralith `minecraft:overworld` noise settings)

| Block | Tier | Biomes | Other conditions on the path |
|---|---:|---|---|
| `magma_block` | 3 | volcanic_crater 0.15% | above_preliminary_surface; floor depth 0; Y >= 61; noise terralith:tropics/volcano_surface -0.025..0.025 |
| `magma_block` | 3 | volcanic_crater 0.15% | above_preliminary_surface; floor depth 0; Y >= 61; noise terralith:tropics/volcano_magma -0.025..0.025 |
| `basalt` | 2 | basalt_cliffs 0.3% | above_preliminary_surface; not (gradient 57 to 61) |
| `blackstone` | 2 | ashen_savanna 0.255% | above_preliminary_surface; floor depth 0 + surface depth, secondary 10; not (gradient 85 to 115); noise terralith:tropics/volcano_surface -0.105..0.105 |
| `blackstone` | 2 | tropical_jungle 0.1% | above_preliminary_surface; floor depth 0 + surface depth; gradient 63 to 67 |
| `basalt` | 2 | tropical_jungle 0.1% | above_preliminary_surface; floor depth 0 + surface depth; gradient 65 to 70 |
| `blackstone` | 2 | tropical_jungle 0.1% | above_preliminary_surface; floor depth 0 + surface depth; not (gradient 105 to 125); noise terralith:tropics/volcano_surface -0.105..0.105 |
| `blackstone` | 2 | volcanic_peaks 0.69%, volcanic_crater 0.15% | above_preliminary_surface; noise terralith:tropics/volcano_surface -0.11..0.11 |
| `basalt` | 2 | volcanic_peaks 0.69%, volcanic_crater 0.15% | above_preliminary_surface; noise terralith:tropics/volcano_surface -0.75..0.75 |
| `blackstone` | 2 | scarlet_mountains 0.25% | above_preliminary_surface; noise terralith:scarlet/surface_pattern -0.375..0.375 |
| `blackstone` | 2 | yellowstone 4.275% | above_preliminary_surface; noise terralith:yellowstone/calcite_patch 0.615..100 |

## 2. Biome features

Rate is the placed feature's attempts per chunk; nested features keep their own placement. "OFF" = the json sets count 0.

| Feature | Source | Nether blocks | Rate | Height | Biomes | Notes |
|---|---|---|---|---|---|---|
| `terralith:cave/fuck_art_just_need_performance/blackstone` | Terralith | blackstone (T2) | 256 per chunk | Y bottom+32 to 0 | cave/mantle_caves 1.196% |  |
| `terralith:cave/mantle/basalt_strip` | Terralith | blackstone (T2) | 140 per chunk | Y bottom to 64 | cave/mantle_caves 1.196% | scans for a ceiling/floor |
| `terralith:cave/mantle/magma_strip` | Terralith | blackstone (T2), magma_block (T3) | 140 per chunk | Y bottom to 64 | cave/mantle_caves 1.196% | scans for a ceiling/floor |
| `terralith:cave/mantle/replacer` | Terralith | blackstone (T2) | 250 per chunk | Y bottom to 30 | cave/mantle_caves 1.196% |  |
| `terralith:cave/thermal/surface/floor` | Terralith | blackstone (T2) | variable | surface (feature finds it) | cave/thermal_caves 1.173% | noise-based count |
| `terralith:cave/thermal/surface/floor_low` | Terralith | blackstone (T2) | variable | surface (feature finds it) | cave/thermal_caves 1.173% | noise-based count |
| `terralith:cliffs/basalt/basalt_column` | Terralith | basalt (T2) | 200 per chunk | Y 75 to 75 | basalt_cliffs 0.3% | scans for a ceiling/floor |
| `terralith:cliffs/basalt/basalt_column_upper` | Terralith | basalt (T2) | 100 per chunk | Y 180 to 180 | basalt_cliffs 0.3% | scans for a ceiling/floor |
| `terralith:cliffs/basalt/basaltifier` | Terralith | basalt (T2) | 700 per chunk | Y 62 to 164 | basalt_cliffs 0.3% |  |
| `terralith:savanna/badlands/grass_gravel` | Terralith | blackstone (T2) | 200 per chunk | surface (feature finds it) | ashen_savanna 0.255% |  |
| `terralith:volcano/crater_fixer` | Terralith | basalt (T2), blackstone (T2), magma_block (T3) | 512 per chunk | surface or sea floor | volcanic_crater 0.15% |  |
| `terralith:volcano/lava_flow` | Terralith | blackstone (T2) | 80 per chunk | surface (feature finds it) | volcanic_crater 0.15% |  |
| `terralith:volcano/magma_barrier` | Terralith | blackstone (T2) | 2048 per chunk | WORLD_SURFACE | volcanic_crater 0.15% |  |
| `terralith:yellowstone/acid_black` | Terralith | blackstone (T2) | 228 per chunk | Y 61 to 63 | yellowstone 4.275% |  |
| `volcanic_caverns:basalt_pillar` | Java | basalt (T2) | 4 per chunk | Y bottom+9 to bottom+24 | cave/deep_caves 18.01%, lush_caves 12.02%, deep_ocean 8.5%, deep_cold_ocean 6.645%, deep_lukewarm_ocean 6.305%, cave/andesite_caves 5.976%, ocean 5.135%, yellowstone 4.275% (+137 more) | pillar from cave floor to ceiling; Volcanic Caverns 1.2.2 (VCPlacedFeatures / VCConfiguredFeatures) |
| `volcanic_caverns:basalt_spike` | Java | basalt (T2) | 2 to 8 per chunk | Y bottom+2 to bottom+28 | cave/deep_caves 18.01%, lush_caves 12.02%, deep_ocean 8.5%, deep_cold_ocean 6.645%, deep_lukewarm_ocean 6.305%, cave/andesite_caves 5.976%, ocean 5.135%, yellowstone 4.275% (+137 more) | large dripstone-shaped spikes; Volcanic Caverns 1.2.2 (VCPlacedFeatures / VCConfiguredFeatures) |
| `yungsextras:desert/obelisks/desert_obelisk_rare` | YungsExtras | blackstone (T2), blackstone_stairs (T2), gilded_blackstone (T2), polished_blackstone (T2), polished_blackstone_brick_stairs (T2), polished_blackstone_bricks (T2) | 1 in 3000 chunks | surface | lush_desert 1.355%, desert_canyon 0.57%, desert 0.315%, desert_spires 0.205%, desert_oasis 0.16%, sandstone_valley 0.095% | via `yungsextras:desert/obelisks/desert_obelisk_rare (template yungsextras:desert/obelisk/obelisk_rare)` |
| `terralith:cave/frostfire/frostfire_ceiling` | Terralith | soul_lantern (T3) | 12 per chunk | Y bottom to 30 | cave/frostfire_caves 0.861% | scans for a ceiling/floor |
| `terralith:cave/frostfire/frostfire_patch` | Terralith | soul_fire (T3) | 17 per chunk | Y bottom to 30 | cave/frostfire_caves 0.861% | scans for a ceiling/floor |
| `terralith:cave/thermal/surface/ceiling` | Terralith | magma_block (T3) | 512 per chunk | Y -64 to 192 | cave/thermal_caves 1.173% | scans for a ceiling/floor |
| `terralith:cave/thermal/surface/ceiling_low` | Terralith | magma_block (T3) | 512 per chunk | Y -64 to 192 | cave/thermal_caves 1.173% | scans for a ceiling/floor |
| `terralith:yellowstone/magma` | Terralith | magma_block (T3) | 2.5 per chunk | surface (feature finds it) | yellowstone 4.275%, caldera 0.305% |  |
| `upgrade_aquatic:underwater_canyon` | Java carver | magma_block (T3) | 1 in 30 chunks starts one |  | deep_ocean 8.5%, deep_cold_ocean 6.645%, deep_lukewarm_ocean 6.305%, ocean 5.135%, deep_frozen_ocean 3.245%, lukewarm_ocean 3.235%, cold_ocean 3.015%, warm_ocean 2.99% (+1 more) | canyon Y 38 to 58; where it reaches Y 10 the floor is 25% magma, 75% obsidian (UnderwaterCanyonWorldCarver, Upgrade Aquatic 5.1.0) |
| `volcanic_caverns:delta` | Java | magma_block (T3) | 6 per chunk | Y bottom+8 to bottom+18, biased low | cave/deep_caves 18.01%, lush_caves 12.02%, deep_ocean 8.5%, deep_cold_ocean 6.645%, deep_lukewarm_ocean 6.305%, cave/andesite_caves 5.976%, ocean 5.135%, yellowstone 4.275% (+137 more) | lava delta (vanilla delta feature) with a magma rim; Volcanic Caverns 1.2.2 (VCPlacedFeatures / VCConfiguredFeatures) |
| `volcanic_caverns:underground_magma` | Java | magma_block (T3) | 10 per chunk | Y bottom+8 to bottom+15, biased low | cave/deep_caves 18.01%, lush_caves 12.02%, deep_ocean 8.5%, deep_cold_ocean 6.645%, deep_lukewarm_ocean 6.305%, cave/andesite_caves 5.976%, ocean 5.135%, yellowstone 4.275% (+137 more) | ore blob size 33 in natural stone; Volcanic Caverns 1.2.2 (VCPlacedFeatures / VCConfiguredFeatures) |
| `terralith:volcano/magma_veins` OFF | Terralith | blackstone (T2), magma_block (T3) | 0 | WORLD_SURFACE | volcanic_crater 0.15% |  |

### Json-less mod features (checked by jar)

Features an Overworld biome gets that have no json. Each mod's worldgen classes are searched for a Nether `Blocks` field (SRG name); "clean" = none referenced.

| Mod | Features | Nether references in worldgen classes | Verdict |
|---|---:|---|---|
| `buzzier_bees` | 3 | - | clean |
| `farmersdelight` | 2 | - | clean |
| `farmersrespite` | 1 | FRGeneration.class: basalt | FRGeneration: coffee bush placement predicate (on basalt, Nether only) |
| `fireflybushport` | 1 | - | clean |
| `fruitsdelight` | 17 | - | clean |
| `immersiveengineering` | 7 | - | clean |
| `immersivepetroleum` | 1 | - | clean |
| `incubation` | 1 | - | clean |
| `moyai` | 2 | - | clean |
| `mysticaloaktree` | 1 | - | clean |
| `neapolitan` | 7 | - | clean |
| `rare-ice` | 1 | - | clean |
| `sky_structures` | 9 | - | clean |
| `upgrade_aquatic` | 7 | PrismarineCoralFeature.class: magma_block; UAWorldCarvers$UAConfiguredWorldCarvers.class: warped_button; UnderwaterCanyonWorldCarver.class: magma_block | PrismarineCoralFeature only looks for magma/obsidian below (UnderwaterCanyonWorldCarver: CODE_ADDED) |

## 3. Structures after the swaps

What `nether_block_swaps.json` leaves in each live Overworld structure, plus the Nether blocks the swaps put in ("swapped in"). Counts sum every template the structure can place. "Shared" = templates a Nether or End structure also reaches: never swapped, and usually that structure's own pieces the pool walk over-reaches into (it ignores jigsaw names), so not counted in the tiers.

| Structure | Group | T1 | T2 | T3 | Blocks left | Swapped in | Shared |
|---|---|---:|---:|---:|---|---|---|
| `minecraft:ancient_city` | standalone | 173 | 705 | 128 | polished_basalt 705, soul_sand 173, soul_lantern 128 | - | - |
| `philipsruins:sculk_dungeon` | standalone | 4 | 36 |  | polished_basalt 17, basalt 10, polished_blackstone_brick_wall 8, soul_soil 4, blackstone 1 | - | - |
| `animal_barn:barn` | remote |  | 822 |  | polished_blackstone_stairs 410, polished_blackstone_slab 248, polished_blackstone 158, polished_blackstone_pressure_plate 6 | - | - |
| `philipsruins:underground_structures` | standalone |  | 603 |  | polished_basalt 363, basalt 240 | basalt 204 | - |
| `terralith:fortified_village` | settlements |  | 480 | 4 | basalt 480, magma_block 4 | - | - |
| `simply_houses:plains_mansion` | remote |  | 315 |  | polished_basalt 171, basalt 144 | - | - |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | pillager_settlements |  | 312 |  | polished_blackstone_bricks 141, polished_blackstone_brick_stairs 41, polished_blackstone_brick_slab 40, polished_blackstone_brick_wall 36, cracked_polished_blackstone_bricks 30, polished_blackstone 24 | - | - |
| `terralith:underground/frosted_dungeon` | standalone |  | 233 |  | polished_basalt 233 | - | - |
| `minecraft:pillager_outpost` | pillager_settlements |  | 174 |  | polished_blackstone_button 96, polished_blackstone_wall 54, polished_blackstone 24 | - | - |
| `simply_houses:taiga_forge` | remote |  | 168 | 4 | basalt 74, polished_basalt 71, blackstone 8, polished_blackstone_brick_slab 5, magma_block 4, polished_blackstone_brick_wall 3, blackstone_slab 3, gilded_blackstone 2, polished_blackstone_brick_stairs 2 | - | - |
| `betterstrongholds:stronghold` | standalone |  | 154 |  | polished_blackstone_slab 64, polished_blackstone_stairs 48, polished_blackstone 31, crying_obsidian 10, chiseled_polished_blackstone 1 | - | - |
| `betterdeserttemples:desert_temple` | desert_temples |  | 130 | 48 | magma_block 48, polished_blackstone 44, blackstone_wall 32, polished_blackstone_stairs 29, blackstone 10, blackstone_slab 8, gilded_blackstone 4, polished_blackstone_button 3 | - | - |
| `simply_houses:plains_house` | remote |  | 97 |  | basalt 50, polished_basalt 47 | - | - |
| `underground_bunkers:underground_bunker` | standalone |  | 59 | 10 | basalt 52, magma_block 10, polished_basalt 7 | basalt 30 | - |
| `philipsruins:level_two_ruins_pool` | ruins |  | 37 |  | polished_basalt 34, basalt 3 | basalt 3 | - |
| `terralith:spire` | surface_dungeons |  | 30 |  | blackstone 26, polished_blackstone_brick_wall 4 | - | - |
| `towns_and_towers:pillager_outpost_ocean` | boats |  | 20 |  | basalt 20 | - | - |
| `towns_and_towers:village_mushroom_fields` | settlements |  | 13 |  | basalt 11, polished_basalt 2 | - | - |
| `villager_gold_washing_facility:villager_gold_washing_facility` | remote |  | 10 |  | blackstone_stairs 6, polished_blackstone_slab 4 | - | - |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | pillager_settlements |  | 9 |  | crying_obsidian 9 | - | - |
| `oldvillagerstatue:old_villager_statue` | ruins |  | 8 |  | crying_obsidian 8 | - | - |
| `philipsruins:ancient_crypt` | standalone |  | 6 |  | basalt 6 | basalt 6 | - |
| `desert_village_temple:village_temple` | remote |  | 4 | 3 | blackstone_slab 4, magma_block 3 | - | - |
| `antiquetradingship:antique_trading_ship` | boats |  | 2 |  | polished_blackstone_button 2 | - | - |
| `towns_and_towers:village_forest` | settlements |  | 2 |  | blackstone 2 | - | - |
| `betterjungletemples:jungle_temple` | surface_dungeons |  |  | 177 | magma_block 177 | magma_block 34 | - |
| `terralith:fortified_desert_village` | settlements |  |  | 8 | magma_block 8 | magma_block 8 | - |
| `hopo:underwater_ruins` | ocean_ruins |  |  | 5 | magma_block 5 | - | - |
| `eugene:red_roof_castle` | settlements |  |  | 3 | magma_block 3 | magma_block 3 | - |
| `minecraft:village_savanna` | settlements |  |  | 2 | magma_block 2 | - | - |
| `minecraft:village_taiga` | settlements |  |  | 2 | magma_block 2 | - | - |
| `philipsruins:bone_dungeon` | standalone |  |  | 2 | soul_lantern 2 | - | - |
| `simply_houses:taiga_house` | remote |  |  | 2 | magma_block 2 | - | - |
| `villager_forge:villager_forge` | remote |  |  | 2 | magma_block 2 | - | - |
| `farmers_structures:cooks` | remote |  |  | 1 | magma_block 1 | - | - |
| `illager_siege_weapons:catapult` | vehicles |  |  | 1 | magma_block 1 | - | - |
| `illager_siege_weapons:trebuchet` | vehicles |  |  | 1 | magma_block 1 | - | - |
| `minecraft:village_desert` | settlements |  |  | 1 | magma_block 1 | - | - |
| `minecraft:village_plains` | settlements |  |  | 1 | magma_block 1 | - | - |
| `towns_and_towers:village_old_growth_taiga` | settlements |  |  | 1 | magma_block 1 | - | - |
| `towns_and_towers:village_swamp` | settlements |  |  | 1 | magma_block 1 | - | - |
| `unwrecked_ships:ship` | boats |  |  |  | - | - | warped_wart_block 303, nether_wart_block 303, crimson_planks 110, warped_planks 110, crimson_slab 80, warped_slab 80, crimson_fence 47, warped_fence 47, crimson_stairs 25, warped_stairs 25, blackstone 21, magma_block 16, nether_gold_ore 7, soul_lantern 7, crimson_trapdoor 5, warped_trapdoor 5, polished_blackstone_pressure_plate 4 |

Not live (off or blocked): `minecraft:ruined_portal`.

## 4. Structure processors

150 processor lists read (jars, kubejs/data wins). Nether blocks a processor puts out (input and location predicates skipped): it can place them where the template has none. The lists are not tied to a dimension here; check which structures use a hit.

| Processor list | Source | Nether blocks out (processor type) |
|---|---|---|
| `betterdungeons:small_nether_dungeon/props` | YungsBetterDungeons | crying_obsidian (rule), nether_quartz_ore (rule) |
| `betterdungeons:small_nether_dungeon/small_nether_dungeon_blaze` | YungsBetterDungeons | crying_obsidian (rule), nether_quartz_ore (rule) |
| `betterdungeons:small_nether_dungeon/small_nether_dungeon_skeleton` | YungsBetterDungeons | crying_obsidian (rule), nether_quartz_ore (rule) |
| `betterdungeons:small_nether_dungeon/small_nether_dungeon_wither_skeleton` | YungsBetterDungeons | crying_obsidian (rule), nether_quartz_ore (rule) |
| `betterdungeons:small_nether_dungeon/small_nether_dungeon_zombified_piglin` | YungsBetterDungeons | crying_obsidian (rule), nether_quartz_ore (rule) |
| `betterfortresses:main` | YungsBetterNetherFortresses | cracked_nether_bricks (betterfortresses:pillar_processor/rule), crying_obsidian (rule), magma_block (betterfortresses:pillar_processor), nether_bricks (betterfortresses:pillar_processor), red_nether_bricks (betterfortresses:pillar_processor) |
| `kaisyn:village/street_mushroom` | Towns | basalt (rule) |
| `nullscape:phantom_nest` | Nullscape | crying_obsidian (rule) |
| `philipsruins:nether_lava_ruins_processor` | PhilipsRuins1.19.2 | blackstone (rule), cracked_nether_bricks (rule), cracked_polished_blackstone_bricks (rule), crimson_nylium (rule), gilded_blackstone (rule) |
| `terralith:village/desert/road` | Terralith | basalt (rule) |
| `terralith:village/fortified/road` | Terralith | basalt (rule) |
| `unwrecked_ships:blackstone` | Unwrecked Ships Plus 1.19 | gilded_blackstone (rule) |
| `unwrecked_ships:stripped_mast` | Unwrecked Ships Plus 1.19 | stripped_crimson_stem (rule), stripped_warped_stem (rule) |

Mod processor classes (decompiled, no json): `betterdungeons:nether_block_processor` only removes Nether blocks from YUNG's Better Dungeons pieces; `betterfortresses:item_frame_processor` Better Nether Fortresses item frames (glowstone dust and the like as frame items, not blocks); Nether only.
