# Nether blocks in Overworld structures

Written by `python3 tools/nether_blocks_scan.py`. Do not edit. Every structure that can spawn in an Overworld biome (rate model biome lists: fits and biome rules applied) whose templates hold Nether blocks. Counts are blocks summed over every template the structure can place (a jigsaw village counts each house once, not as often as it is built). Not seen: processor-list swaps and code-built blocks.

**Tier 1** netherrack, soul sand/soil, nether wart. **Tier 2** crying obsidian, blackstone family (gilded included), basalt. **Tier 3** other Nether blocks (nether bricks, crimson/warped, wart blocks, shroomlight, soul fire/light, magma, glowstone, Nether ores...).

Status: OFF = disabled in `structure_groups.json`, BLOCKED = another mod stops it, approx = templates matched by mod, not by structure. "Also Nether" = its biome list includes Nether biomes too.

## Summary

| Structure | Group | Status | T1 | T2 | T3 | Blocks |
|---|---|---|---:|---:|---:|---|
| `minecraft:ruined_portal` | standalone | OFF | 1860 |  | 1 | netherrack 1860, magma_block 1 |
| `philipsruins:underground_structures` | standalone |  | 204 | 399 | 17 | polished_basalt 363, soul_soil 110, soul_sand 94, basalt 36, soul_lantern 17 |
| `minecraft:ancient_city` | standalone |  | 173 | 705 | 301 | polished_basalt 705, soul_sand 173, soul_fire 173, soul_lantern 128 |
| `betterdungeons:zombie_dungeon` | entrance_dungeons |  | 97 |  | 2 | soul_sand 97, warped_stairs 1, soul_lantern 1 |
| `underground_bunkers:underground_bunker` | standalone |  | 60 | 29 | 24 | soul_sand 30, nether_wart 30, basalt 22, nether_brick_wall 14, magma_block 10, polished_basalt 7 |
| `betterjungletemples:jungle_temple` | surface_dungeons |  | 34 |  | 150 | magma_block 143, netherrack 34, soul_wall_torch 7 |
| `terralith:underground/frosted_dungeon` | standalone |  | 32 | 233 | 48 | polished_basalt 233, soul_soil 32, soul_fire 28, soul_lantern 20 |
| `terralith:fortified_desert_village` | settlements |  | 8 |  | 36 | crimson_trapdoor 36, netherrack 8 |
| `philipsruins:ancient_crypt` | standalone |  | 6 |  |  | soul_sand 6 |
| `philipsruins:sculk_dungeon` | standalone |  | 4 | 36 | 4 | polished_basalt 17, basalt 10, polished_blackstone_brick_wall 8, soul_soil 4, soul_fire 4, blackstone 1 |
| `philipsruins:level_two_ruins_pool` | ruins |  | 3 | 34 |  | polished_basalt 34, soul_sand 2, soul_soil 1 |
| `eugene:red_roof_castle` | settlements |  | 3 |  |  | netherrack 3 |
| `simply_houses:taiga_woodcutter` | remote |  | 1 |  | 3 | nether_wart_block 2, netherrack 1, soul_lantern 1 |
| `animal_barn:barn` | remote |  |  | 822 |  | polished_blackstone_stairs 410, polished_blackstone_slab 248, polished_blackstone 158, polished_blackstone_pressure_plate 6 |
| `terralith:fortified_village` | settlements |  |  | 480 | 13 | basalt 480, soul_campfire 6, magma_block 4, potted_crimson_fungus 2, soul_lantern 1 |
| `simply_houses:plains_mansion` | remote |  |  | 315 | 1 | polished_basalt 171, basalt 144, soul_lantern 1 |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | pillager_settlements |  |  | 312 |  | polished_blackstone_bricks 141, polished_blackstone_brick_stairs 41, polished_blackstone_brick_slab 40, polished_blackstone_brick_wall 36, cracked_polished_blackstone_bricks 30, polished_blackstone 24 |
| `minecraft:pillager_outpost` | pillager_settlements |  |  | 174 | 12 | polished_blackstone_button 96, polished_blackstone_wall 54, polished_blackstone 24, soul_lantern 12 |
| `simply_houses:taiga_forge` | remote |  |  | 168 | 15 | basalt 74, polished_basalt 71, blackstone 8, nether_wart_block 6, polished_blackstone_brick_slab 5, magma_block 4 ... |
| `betterstrongholds:stronghold` | standalone |  |  | 154 | 223 | nether_gold_ore 172, polished_blackstone_slab 64, polished_blackstone_stairs 48, polished_blackstone 31, crimson_button 24, nether_brick_fence 12 ... |
| `betterdeserttemples:desert_temple` | desert_temples |  |  | 130 | 80 | magma_block 48, polished_blackstone 44, blackstone_wall 32, polished_blackstone_stairs 29, warped_pressure_plate 24, blackstone 10 ... |
| `simply_houses:plains_house` | remote |  |  | 97 | 1 | basalt 50, polished_basalt 47, soul_lantern 1 |
| `terralith:spire` | surface_dungeons |  |  | 30 | 230 | soul_lantern 221, blackstone 26, warped_trapdoor 8, polished_blackstone_brick_wall 4, soul_wall_torch 1 |
| `unwrecked_ships:ship` | boats |  |  | 25 | 1170 | warped_wart_block 303, nether_wart_block 303, crimson_planks 110, warped_planks 110, crimson_slab 80, warped_slab 80 ... |
| `towns_and_towers:pillager_outpost_ocean` | boats |  |  | 20 |  | basalt 20 |
| `towns_and_towers:village_mushroom_fields` | settlements |  |  | 13 | 19 | twisting_vines_plant 14, basalt 11, soul_lantern 2, shroomlight 2, polished_basalt 2, twisting_vines 1 |
| `villager_gold_washing_facility:villager_gold_washing_facility` | remote |  |  | 10 |  | blackstone_stairs 6, polished_blackstone_slab 4 |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | pillager_settlements |  |  | 9 |  | crying_obsidian 9 |
| `oldvillagerstatue:old_villager_statue` | ruins |  |  | 8 |  | crying_obsidian 8 |
| `desert_village_temple:village_temple` | remote |  |  | 4 | 3 | blackstone_slab 4, magma_block 3 |
| `antiquetradingship:antique_trading_ship` | boats |  |  | 2 |  | polished_blackstone_button 2 |
| `towns_and_towers:village_forest` | settlements |  |  | 2 |  | blackstone 2 |
| `illagerwarship:warship` | boats |  |  |  | 115 | nether_bricks 69, nether_brick_slab 37, nether_brick_fence 9 |
| `towns_and_towers:pillager_outpost_mushroom_fields` | pillager_settlements |  |  |  | 28 | shroomlight 28 |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | pillager_settlements |  |  |  | 14 | ancient_debris 7, glowstone 6, respawn_anchor 1 |
| `simply_houses:taiga_house` | remote |  |  |  | 13 | soul_lantern 7, magma_block 2, crimson_roots 1, crimson_fungus 1, warped_fungus 1, nether_sprouts 1 |
| `simply_houses:taiga_inn` | remote |  |  |  | 9 | soul_lantern 9 |
| `skyvillages:skyvillage` | sky |  |  |  | 8 | glowstone 8 |
| `terralith:underground/giant_bee_hive` | standalone |  |  |  | 7 | shroomlight 7 |
| `hopo:underwater_ruins` | ocean_ruins |  |  |  | 5 | magma_block 5 |
| `eugene:witch_wagon` | vehicles |  |  |  | 4 | soul_lantern 4 |
| `gothic_church:church` | remote |  |  |  | 4 | glowstone 4 |
| `minecraft:village_snowy` | settlements |  |  |  | 4 | soul_wall_torch 4 |
| `minecraft:village_savanna` | settlements |  |  |  | 3 | magma_block 2, potted_crimson_fungus 1 |
| `minecraft:village_taiga` | settlements |  |  |  | 3 | magma_block 2, potted_crimson_fungus 1 |
| `philipsruins:pumpkin_ruins` | ruins |  |  |  | 3 | soul_lantern 3 |
| `simply_houses:forest_fort` | remote |  |  |  | 3 | soul_lantern 2, soul_campfire 1 |
| `betterwitchhuts:witch_hut` | witch_huts |  |  |  | 2 | crimson_trapdoor 2 |
| `minecraft:village_plains` | settlements |  |  |  | 2 | potted_crimson_fungus 1, magma_block 1 |
| `philipsruins:bone_dungeon` | standalone |  |  |  | 2 | soul_lantern 2 |
| `towns_and_towers:pillager_outpost_jungle` | pillager_settlements |  |  |  | 2 | crimson_button 2 |
| `towns_and_towers:pillager_outpost_snowy_slopes` | pillager_settlements |  |  |  | 2 | soul_lantern 2 |
| `towns_and_towers:village_swamp` | settlements |  |  |  | 2 | soul_lantern 1, magma_block 1 |
| `villager_forge:villager_forge` | remote |  |  |  | 2 | magma_block 2 |
| `betterdungeons:skeleton_dungeon` | standalone |  |  |  | 1 | soul_campfire 1 |
| `farmers_structures:cooks` | remote |  |  |  | 1 | magma_block 1 |
| `illager_siege_weapons:catapult` | vehicles |  |  |  | 1 | magma_block 1 |
| `illager_siege_weapons:trebuchet` | vehicles |  |  |  | 1 | magma_block 1 |
| `minecraft:village_desert` | settlements |  |  |  | 1 | magma_block 1 |
| `philipsruins:ocean_ruins` | ocean_ruins |  |  |  | 1 | stripped_warped_hyphae 1 |
| `plains_villager_tents:big_villager_tent` | remote |  |  |  | 1 | glowstone 1 |
| `simply_houses:forest_stonemason` | remote |  |  |  | 1 | soul_lantern 1 |
| `towns_and_towers:pillager_outpost_flower_forest` | pillager_settlements |  |  |  | 1 | shroomlight 1 |
| `towns_and_towers:pillager_outpost_snowy_plains` | pillager_settlements |  |  |  | 1 | potted_crimson_roots 1 |
| `towns_and_towers:village_old_growth_taiga` | settlements |  |  |  | 1 | magma_block 1 |

65 structures, 64 live; tier 1 in 12, tier 2 in 25, tier 3 only in 33.

## By block

| Block | Tier | Live structures | Total blocks |
|---|---:|---:|---:|
| `minecraft:soul_sand` | 1 | 6 | 402 |
| `minecraft:netherrack` | 1 | 4 | 46 |
| `minecraft:soul_soil` | 1 | 4 | 147 |
| `minecraft:nether_wart` | 1 | 1 | 30 |
| `minecraft:polished_basalt` | 2 | 10 | 1650 |
| `minecraft:basalt` | 2 | 9 | 847 |
| `minecraft:blackstone` | 2 | 6 | 68 |
| `minecraft:polished_blackstone` | 2 | 5 | 281 |
| `minecraft:polished_blackstone_brick_wall` | 2 | 4 | 51 |
| `minecraft:blackstone_slab` | 2 | 3 | 15 |
| `minecraft:crying_obsidian` | 2 | 3 | 27 |
| `minecraft:polished_blackstone_button` | 2 | 3 | 101 |
| `minecraft:polished_blackstone_slab` | 2 | 3 | 316 |
| `minecraft:polished_blackstone_stairs` | 2 | 3 | 487 |
| `minecraft:gilded_blackstone` | 2 | 2 | 6 |
| `minecraft:polished_blackstone_brick_slab` | 2 | 2 | 45 |
| `minecraft:polished_blackstone_brick_stairs` | 2 | 2 | 43 |
| `minecraft:polished_blackstone_pressure_plate` | 2 | 2 | 10 |
| `minecraft:blackstone_stairs` | 2 | 1 | 6 |
| `minecraft:blackstone_wall` | 2 | 1 | 32 |
| `minecraft:chiseled_polished_blackstone` | 2 | 1 | 1 |
| `minecraft:cracked_polished_blackstone_bricks` | 2 | 1 | 30 |
| `minecraft:polished_blackstone_bricks` | 2 | 1 | 141 |
| `minecraft:polished_blackstone_wall` | 2 | 1 | 54 |
| `minecraft:soul_lantern` | 3 | 23 | 448 |
| `minecraft:magma_block` | 3 | 19 | 248 |
| `minecraft:crimson_trapdoor` | 3 | 4 | 44 |
| `minecraft:glowstone` | 3 | 4 | 19 |
| `minecraft:potted_crimson_fungus` | 3 | 4 | 5 |
| `minecraft:shroomlight` | 3 | 4 | 38 |
| `minecraft:crimson_button` | 3 | 3 | 28 |
| `minecraft:nether_wart_block` | 3 | 3 | 311 |
| `minecraft:soul_campfire` | 3 | 3 | 8 |
| `minecraft:soul_fire` | 3 | 3 | 205 |
| `minecraft:soul_wall_torch` | 3 | 3 | 12 |
| `minecraft:crimson_slab` | 3 | 2 | 86 |
| `minecraft:nether_brick_fence` | 3 | 2 | 21 |
| `minecraft:nether_brick_wall` | 3 | 2 | 20 |
| `minecraft:nether_gold_ore` | 3 | 2 | 179 |
| `minecraft:warped_stairs` | 3 | 2 | 26 |
| `minecraft:warped_trapdoor` | 3 | 2 | 13 |
| `minecraft:ancient_debris` | 3 | 1 | 7 |
| `minecraft:crimson_fence` | 3 | 1 | 47 |
| `minecraft:crimson_fungus` | 3 | 1 | 1 |
| `minecraft:crimson_planks` | 3 | 1 | 110 |
| `minecraft:crimson_roots` | 3 | 1 | 1 |
| `minecraft:crimson_stairs` | 3 | 1 | 25 |
| `minecraft:nether_brick_slab` | 3 | 1 | 37 |
| `minecraft:nether_brick_stairs` | 3 | 1 | 8 |
| `minecraft:nether_bricks` | 3 | 1 | 69 |
| `minecraft:nether_sprouts` | 3 | 1 | 1 |
| `minecraft:potted_crimson_roots` | 3 | 1 | 1 |
| `minecraft:respawn_anchor` | 3 | 1 | 1 |
| `minecraft:stripped_warped_hyphae` | 3 | 1 | 1 |
| `minecraft:twisting_vines` | 3 | 1 | 1 |
| `minecraft:twisting_vines_plant` | 3 | 1 | 14 |
| `minecraft:warped_fence` | 3 | 1 | 47 |
| `minecraft:warped_fungus` | 3 | 1 | 1 |
| `minecraft:warped_planks` | 3 | 1 | 110 |
| `minecraft:warped_pressure_plate` | 3 | 1 | 24 |
| `minecraft:warped_slab` | 3 | 1 | 80 |
| `minecraft:warped_wart_block` | 3 | 1 | 303 |

## Templates (live structures with tier 1 or 2)

### `philipsruins:underground_structures`

- `philipsruins:deep_ruinbig1`: polished_basalt 106, soul_soil 39, soul_sand 32, basalt 13, soul_lantern 2
- `philipsruins:deep_ruin8`: polished_basalt 85, soul_soil 28, soul_sand 22, soul_lantern 5
- `philipsruins:deep_ruin6`: polished_basalt 63, basalt 19, soul_soil 19, soul_sand 13, soul_lantern 5
- `philipsruins:deep_ruin5`: polished_basalt 53, soul_sand 11, soul_soil 7, basalt 4, soul_lantern 1
- `philipsruins:deep_ruin9`: polished_basalt 34
- `philipsruins:deep_ruin7`: polished_basalt 22, soul_soil 3, soul_sand 1
- `philipsruins:deep_ruin3`: soul_soil 8, soul_sand 8, soul_lantern 1
- `philipsruins:deep_ruin2`: soul_sand 7, soul_soil 6, soul_lantern 3

### `minecraft:ancient_city`

- `minecraft:ancient_city/city/entrance/entrance_connector`: polished_basalt 138, soul_lantern 6
- `minecraft:ancient_city/city/entrance/entrance_path_1`: polished_basalt 138, soul_lantern 7
- `minecraft:ancient_city/city/entrance/entrance_path_2`: polished_basalt 113, soul_lantern 7
- `minecraft:ancient_city/city/entrance/entrance_path_4`: polished_basalt 91, soul_lantern 2
- `minecraft:ancient_city/city/entrance/entrance_path_3`: polished_basalt 90, soul_lantern 4
- `minecraft:ancient_city/city/entrance/entrance_path_5`: polished_basalt 72
- `minecraft:ancient_city/city_center/city_center_1`: soul_sand 57, soul_fire 57, soul_lantern 6
- `minecraft:ancient_city/city_center/city_center_2`: soul_sand 57, soul_fire 57, soul_lantern 6
- `minecraft:ancient_city/city_center/city_center_3`: soul_sand 57, soul_fire 57, soul_lantern 3
- `minecraft:ancient_city/structures/tall_ruin_1`: polished_basalt 43, soul_lantern 4
- `minecraft:ancient_city/walls/intact_corner_wall_1`: soul_lantern 9, polished_basalt 5
- `minecraft:ancient_city/walls/intact_intersection_wall_1`: soul_lantern 8, polished_basalt 5
- `minecraft:ancient_city/walls/intact_lshape_wall_1`: polished_basalt 5, soul_lantern 5
- `minecraft:ancient_city/walls/ruined_corner_wall_1`: soul_lantern 8, polished_basalt 5
- `minecraft:ancient_city/structures/barracks`: soul_sand 2, soul_fire 2

### `betterdungeons:zombie_dungeon`

- `betterdungeons:zombie_dungeon/floor1/room/floor1_specialroom_0`: soul_sand 97, soul_lantern 1

### `underground_bunkers:underground_bunker`

- `underground_bunkers:underground_bunker/underground_bunkers_room_7`: soul_sand 30, nether_wart 30
- `underground_bunkers:underground_bunker/underground_bunkers_room_9`: basalt 15, magma_block 10, polished_basalt 7
- `underground_bunkers:underground_bunker/underground_bunkers_room_2`: basalt 7

### `betterjungletemples:jungle_temple`

- `betterjungletemples:floor_1/room_back/floor_1_room_back_2`: netherrack 34

### `terralith:underground/frosted_dungeon`

- `terralith:underground/dungeon1`: polished_basalt 233, soul_soil 32, soul_fire 28, soul_lantern 20

### `terralith:fortified_desert_village`

- `terralith:village/desert/houses/armorsmith1`: netherrack 8

### `philipsruins:ancient_crypt`

- `philipsruins:crypt_room5`: soul_sand 4
- `philipsruins:crypt_room8`: soul_sand 2

### `philipsruins:sculk_dungeon`

- `philipsruins:sculk_dungeon_room1`: polished_basalt 16, polished_blackstone_brick_wall 8, basalt 7, soul_soil 4, soul_fire 4, blackstone 1
- `philipsruins:sculk_dungeon_start`: basalt 3, polished_basalt 1

### `philipsruins:level_two_ruins_pool`

- `philipsruins:deep_ruin9`: polished_basalt 34
- `philipsruins:ancient_ruin_soul1`: soul_sand 2, soul_soil 1

### `eugene:red_roof_castle`

- `eugene:red_roof`: netherrack 3

### `simply_houses:taiga_woodcutter`

- `simply_houses:taiga_woodcutter/taiga_woodcutter`: nether_wart_block 2, netherrack 1, soul_lantern 1

### `animal_barn:barn`

- `animal_barn:barn`: polished_blackstone_stairs 410, polished_blackstone_slab 248, polished_blackstone 158, polished_blackstone_pressure_plate 6

### `terralith:fortified_village`

- `terralith:village/fortified/houses/cartographer`: basalt 151, potted_crimson_fungus 1
- `terralith:village/fortified/houses/church`: basalt 151, potted_crimson_fungus 1
- `terralith:village/fortified/roads/intersection_big`: basalt 99
- `terralith:village/fortified/houses/mason`: basalt 24
- `terralith:village/fortified/houses/big1`: basalt 23
- `terralith:village/fortified/start_castle`: basalt 10, magma_block 2, soul_campfire 2
- `terralith:village/fortified/houses/library`: basalt 6, soul_campfire 1
- `terralith:village/fortified/houses/butcher`: basalt 6
- `terralith:village/fortified/houses/graveyard`: basalt 5, soul_lantern 1
- `terralith:village/fortified/houses/generic2`: basalt 2
- `terralith:village/fortified/houses/generic_big1`: basalt 2, soul_campfire 1
- `terralith:village/fortified/houses/generic_big3`: basalt 1

### `simply_houses:plains_mansion`

- `simply_houses:plains_mansion/plains_mansion`: polished_basalt 171, basalt 144, soul_lantern 1

### `towns_and_towers:exclusives/pillager_outpost_swedish`

- `kaisyn:outpost/towers/exclusives/swedish/base_plate`: polished_blackstone_bricks 141, polished_blackstone_brick_stairs 41, polished_blackstone_brick_slab 40, polished_blackstone_brick_wall 36, cracked_polished_blackstone_bricks 30, polished_blackstone 24

### `minecraft:pillager_outpost`

- `minecraft:pillager_outpost/feature_cage2`: polished_blackstone_button 32, polished_blackstone_wall 18, polished_blackstone 8
- `minecraft:pillager_outpost/feature_cage3`: polished_blackstone_button 32, polished_blackstone_wall 18, polished_blackstone 8
- `minecraft:pillager_outpost/feature_cage_with_allays`: polished_blackstone_button 32, polished_blackstone_wall 18, polished_blackstone 8

### `simply_houses:taiga_forge`

- `simply_houses:taiga_forge/taiga_forge`: basalt 74, polished_basalt 71, blackstone 8, nether_wart_block 6, polished_blackstone_brick_slab 5, magma_block 4, soul_lantern 4, polished_blackstone_brick_wall 3, blackstone_slab 3, gilded_blackstone 2, polished_blackstone_brick_stairs 2, crimson_trapdoor 1

### `betterstrongholds:stronghold`

- `betterstrongholds:starts/junction_lg`: polished_blackstone_slab 58, polished_blackstone_stairs 44, polished_blackstone 30, crimson_button 20
- `betterstrongholds:rooms/mess`: polished_blackstone_slab 3, polished_blackstone_stairs 2, crimson_button 2, polished_blackstone 1
- `betterstrongholds:rooms/junction_md`: polished_blackstone_slab 3, polished_blackstone_stairs 2, crimson_button 2, chiseled_polished_blackstone 1
- `betterstrongholds:treasure/portal/treasure_portal_1`: crying_obsidian 5
- `betterstrongholds:treasure/portal/treasure_portal_3`: crying_obsidian 3
- `betterstrongholds:treasure/portal/treasure_portal_5`: crying_obsidian 2

### `betterdeserttemples:desert_temple`

- `betterdeserttemples:hall_room/5x5/hall_room_5x5_18`: polished_blackstone 16, polished_blackstone_stairs 7, polished_blackstone_button 1
- `betterdeserttemples:hall_room/5x5/hall_room_5x5_17`: polished_blackstone 11, polished_blackstone_stairs 9, gilded_blackstone 2
- `betterdeserttemples:hall_room/5x5/hall_room_5x5_16`: polished_blackstone 11, polished_blackstone_stairs 6, gilded_blackstone 2, polished_blackstone_button 1
- `betterdeserttemples:hall_room/5x5/hall_room_5x5_19`: polished_blackstone_stairs 7, polished_blackstone 6
- `betterdeserttemples:statues/statue_2`: blackstone_wall 8, crimson_button 2, blackstone 1
- `betterdeserttemples:statues/statue_0`: blackstone_wall 6, blackstone 2
- `betterdeserttemples:statues/statue_6`: blackstone_wall 4, blackstone 2
- `betterdeserttemples:statues/statue_3`: blackstone_wall 3, blackstone 2
- `betterdeserttemples:statues/statue_4`: blackstone_wall 4, blackstone 1
- `betterdeserttemples:statues/statue_5`: blackstone_wall 4, blackstone 1
- `betterdeserttemples:statues/statue_1`: blackstone_wall 3, blackstone 1
- `betterdeserttemples:puzzle_big/puzzle_big_2_2`: blackstone_slab 3
- `betterdeserttemples:puzzle_big/puzzle_big_2_4`: blackstone_slab 3
- `betterdeserttemples:puzzle_big/puzzle_big_2_1`: blackstone_slab 2
- `betterdeserttemples:hall_room/drowning_trap`: polished_blackstone_button 1

### `simply_houses:plains_house`

- `simply_houses:plains_house/plains_house`: basalt 34, polished_basalt 26, soul_lantern 1
- `simply_houses:plains_house/plains_house_bridge`: polished_basalt 21, basalt 16

### `terralith:spire`

- `terralith:spire/layer4`: soul_lantern 38, blackstone 26
- `terralith:spire/layer1`: soul_lantern 73, polished_blackstone_brick_wall 4

### `unwrecked_ships:ship`

- `unwrecked_ships:ship/sailor_layout/nether/nether_privateer_ship`: blackstone 21, polished_blackstone_pressure_plate 4

### `towns_and_towers:pillager_outpost_ocean`

- `kaisyn:ships/pillager_outpost_ocean/base_plate`: basalt 10
- `kaisyn:ships/pillager_outpost_ocean/variant/base_plate`: basalt 10

### `towns_and_towers:village_mushroom_fields`

- `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_small_farm_1`: basalt 11, polished_basalt 2

### `villager_gold_washing_facility:villager_gold_washing_facility`

- `villager_gold_washing_facility:villager_gold_washing_facility_lt`: blackstone_stairs 6, polished_blackstone_slab 4

### `mysticrift_pillagers_towers_aris:jungle_pillager_tower`

- `mysticrift_pillagers_towers_aris:jungle_pillager_tower`: crying_obsidian 9

### `oldvillagerstatue:old_villager_statue`

- `oldvillagerstatue:oldvillagerstatue`: crying_obsidian 8

### `desert_village_temple:village_temple`

- `desert_village_temple:villager_desert_temple`: blackstone_slab 4, magma_block 3

### `antiquetradingship:antique_trading_ship`

- `antiquetradingship:shipo`: polished_blackstone_button 2

### `towns_and_towers:village_forest`

- `kaisyn:village/forest_ruins/houses/forest_weaponsmith_1`: blackstone 2

