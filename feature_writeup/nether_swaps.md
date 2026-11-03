# Nether block swaps

Written by `python3 tools/gen_nether_swaps.py` from `kubejs/generation_scripts/worldgen/nether_swap_rules.json`. Do not edit. Per rule: every template it changes, with block counts. Applied by patcherMod `TemplateBlockSwaps` when a template loads.

## 1. Philip's underground structures: soul sand and soil to basalt (204 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `philipsruins:deep_ruin2` | `philipsruins:underground_structures` | `soul_sand` | `minecraft:basalt` | 7 |
| `philipsruins:deep_ruin2` | `philipsruins:underground_structures` | `soul_soil` | `minecraft:basalt` | 6 |
| `philipsruins:deep_ruin3` | `philipsruins:underground_structures` | `soul_sand` | `minecraft:basalt` | 8 |
| `philipsruins:deep_ruin3` | `philipsruins:underground_structures` | `soul_soil` | `minecraft:basalt` | 8 |
| `philipsruins:deep_ruin5` | `philipsruins:underground_structures` | `soul_sand` | `minecraft:basalt` | 11 |
| `philipsruins:deep_ruin5` | `philipsruins:underground_structures` | `soul_soil` | `minecraft:basalt` | 7 |
| `philipsruins:deep_ruin6` | `philipsruins:underground_structures` | `soul_sand` | `minecraft:basalt` | 13 |
| `philipsruins:deep_ruin6` | `philipsruins:underground_structures` | `soul_soil` | `minecraft:basalt` | 19 |
| `philipsruins:deep_ruin7` | `philipsruins:underground_structures` | `soul_sand` | `minecraft:basalt` | 1 |
| `philipsruins:deep_ruin7` | `philipsruins:underground_structures` | `soul_soil` | `minecraft:basalt` | 3 |
| `philipsruins:deep_ruin8` | `philipsruins:underground_structures` | `soul_sand` | `minecraft:basalt` | 22 |
| `philipsruins:deep_ruin8` | `philipsruins:underground_structures` | `soul_soil` | `minecraft:basalt` | 28 |
| `philipsruins:deep_ruinbig1` | `philipsruins:underground_structures` | `soul_sand` | `minecraft:basalt` | 32 |
| `philipsruins:deep_ruinbig1` | `philipsruins:underground_structures` | `soul_soil` | `minecraft:basalt` | 39 |

## 2. YUNG's crypt: soul sand to coarse dirt (97 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterdungeons:zombie_dungeon/floor1/room/floor1_specialroom_0` | `betterdungeons:zombie_dungeon` | `soul_sand` | `minecraft:coarse_dirt` | 97 |

## 3. Underground bunker: nether wart removed (its farm's soul sand is basalt via the catch-all below) (30 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `underground_bunkers:underground_bunker/underground_bunkers_room_7` | `underground_bunkers:underground_bunker` | `nether_wart` | `minecraft:air` | 30 |

## 4. Jungle temple, fortified desert village: netherrack to magma (magma keeps fire burning: vanilla infiniburn_overworld); their soul sand/soil goes to basalt below (42 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterjungletemples:floor_1/room_back/floor_1_room_back_2` | `betterjungletemples:jungle_temple` | `netherrack` | `minecraft:magma_block` | 34 |
| `terralith:village/desert/houses/armorsmith1` | `terralith:fortified_desert_village` | `netherrack` | `minecraft:magma_block` | 8 |

## 5. Terralith frosted dungeon: soul soil to permafrost, soul fire put out (32 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `terralith:underground/dungeon1` | `terralith:underground/frosted_dungeon` | `soul_soil` | `immersive_weathering:permafrost` | 32 |

## 6. Terralith frosted dungeon: soul fire put out (28 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `terralith:underground/dungeon1` | `terralith:underground/frosted_dungeon` | `soul_fire` | `minecraft:air` | 28 |

## 7. Simply Houses taiga woodcutter: nether wart and netherrack removed (3 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `simply_houses:taiga_woodcutter/taiga_woodcutter` | `simply_houses:taiga_woodcutter` | `nether_wart_block` | `minecraft:air` | 2 |
| `simply_houses:taiga_woodcutter/taiga_woodcutter` | `simply_houses:taiga_woodcutter` | `netherrack` | `minecraft:air` | 1 |

## 8. Terralith fortified village: soul campfires to campfires (6 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `terralith:village/fortified/houses/generic_big1` | `terralith:fortified_village` | `soul_campfire` | `minecraft:campfire` | 1 |
| `terralith:village/fortified/houses/library` | `terralith:fortified_village` | `soul_campfire` | `minecraft:campfire` | 1 |
| `terralith:village/fortified/houses/smith1` | `terralith:fortified_village` | `soul_campfire` | `minecraft:campfire` | 2 |
| `terralith:village/fortified/start_castle` | `terralith:fortified_village` | `soul_campfire` | `minecraft:campfire` | 2 |

## 9. Simply Houses taiga house: crimson and warped plants, nether sprouts removed (4 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `simply_houses:taiga_house/taiga_house` | `simply_houses:taiga_house` | `crimson_fungus` | `minecraft:air` | 1 |
| `simply_houses:taiga_house/taiga_house` | `simply_houses:taiga_house` | `crimson_roots` | `minecraft:air` | 1 |
| `simply_houses:taiga_house/taiga_house` | `simply_houses:taiga_house` | `nether_sprouts` | `minecraft:air` | 1 |
| `simply_houses:taiga_house/taiga_house` | `simply_houses:taiga_house` | `warped_fungus` | `minecraft:air` | 1 |

## 10. Sky village: glowstone to yellow corundum (8 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `skyvillages:alchemist_1` | `skyvillages:skyvillage` | `glowstone` | `quark:yellow_corundum` | 2 |
| `skyvillages:cartographer_1` | `skyvillages:skyvillage` | `glowstone` | `quark:yellow_corundum` | 1 |
| `skyvillages:fletcher_1` | `skyvillages:skyvillage` | `glowstone` | `quark:yellow_corundum` | 1 |
| `skyvillages:fountain` | `skyvillages:skyvillage` | `glowstone` | `quark:yellow_corundum` | 2 |
| `skyvillages:loom_1` | `skyvillages:skyvillage` | `glowstone` | `quark:yellow_corundum` | 1 |
| `skyvillages:weaponsmith_1` | `skyvillages:skyvillage` | `glowstone` | `quark:yellow_corundum` | 1 |

## 11. Simply Houses taiga forge: wart blocks removed (6 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `simply_houses:taiga_forge/taiga_forge` | `simply_houses:taiga_forge` | `nether_wart_block` | `minecraft:air` | 6 |

## 12. YUNG's stronghold: nether gold ore to gold ore (172 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterstrongholds:treasure/corner/treasure_corner_10` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 7 |
| `betterstrongholds:treasure/corner/treasure_corner_11` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 6 |
| `betterstrongholds:treasure/corner/treasure_corner_6` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 23 |
| `betterstrongholds:treasure/corner/treasure_corner_7` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 20 |
| `betterstrongholds:treasure/corner/treasure_corner_8` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 14 |
| `betterstrongholds:treasure/corner/treasure_corner_9` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 12 |
| `betterstrongholds:treasure/wall/treasure_wall_0` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 21 |
| `betterstrongholds:treasure/wall/treasure_wall_1` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 21 |
| `betterstrongholds:treasure/wall/treasure_wall_2` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 18 |
| `betterstrongholds:treasure/wall/treasure_wall_3` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 16 |
| `betterstrongholds:treasure/wall/treasure_wall_4` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 7 |
| `betterstrongholds:treasure/wall/treasure_wall_5` | `betterstrongholds:stronghold` | `nether_gold_ore` | `minecraft:gold_ore` | 7 |

## 13. T&T mushroom village: twisting vines removed (15 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `kaisyn:village/mushroom_fields_fantasy/town_centers/mushroom_meeting_point_1` | `towns_and_towers:village_mushroom_fields` | `twisting_vines` | `minecraft:air` | 1 |
| `kaisyn:village/mushroom_fields_fantasy/town_centers/mushroom_meeting_point_1` | `towns_and_towers:village_mushroom_fields` | `twisting_vines_plant` | `minecraft:air` | 14 |

## 14. Mysticrift mangrove tower: every Nether block to gold ore (14 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower_new` | `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | `ancient_debris` | `minecraft:gold_ore` | 7 |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower_new` | `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | `glowstone` | `minecraft:gold_ore` | 6 |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower_new` | `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | `respawn_anchor` | `minecraft:gold_ore` | 1 |

## 15. Every nether brick block to its deepslate brick twin (155 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterstrongholds:rooms/grand_library` | `betterstrongholds:stronghold` | `nether_brick_fence` | `minecraft:deepslate_brick_wall` | 4 |
| `betterstrongholds:rooms/grand_library` | `betterstrongholds:stronghold` | `nether_brick_wall` | `minecraft:deepslate_brick_wall` | 2 |
| `betterstrongholds:statues/statue_book_l` | `betterstrongholds:stronghold` | `nether_brick_stairs` | `minecraft:deepslate_brick_stairs` | 2 |
| `betterstrongholds:statues/statue_book_r` | `betterstrongholds:stronghold` | `nether_brick_stairs` | `minecraft:deepslate_brick_stairs` | 2 |
| `betterstrongholds:statues/statue_chest_l` | `betterstrongholds:stronghold` | `nether_brick_fence` | `minecraft:deepslate_brick_wall` | 2 |
| `betterstrongholds:statues/statue_chest_l` | `betterstrongholds:stronghold` | `nether_brick_wall` | `minecraft:deepslate_brick_wall` | 1 |
| `betterstrongholds:statues/statue_chest_r` | `betterstrongholds:stronghold` | `nether_brick_fence` | `minecraft:deepslate_brick_wall` | 2 |
| `betterstrongholds:statues/statue_chest_r` | `betterstrongholds:stronghold` | `nether_brick_wall` | `minecraft:deepslate_brick_wall` | 1 |
| `betterstrongholds:terminators/hallway_end_statue_book_l` | `betterstrongholds:stronghold` | `nether_brick_stairs` | `minecraft:deepslate_brick_stairs` | 2 |
| `betterstrongholds:terminators/hallway_end_statue_book_r` | `betterstrongholds:stronghold` | `nether_brick_stairs` | `minecraft:deepslate_brick_stairs` | 2 |
| `betterstrongholds:terminators/hallway_end_statue_chest_l` | `betterstrongholds:stronghold` | `nether_brick_fence` | `minecraft:deepslate_brick_wall` | 2 |
| `betterstrongholds:terminators/hallway_end_statue_chest_l` | `betterstrongholds:stronghold` | `nether_brick_wall` | `minecraft:deepslate_brick_wall` | 1 |
| `betterstrongholds:terminators/hallway_end_statue_chest_r` | `betterstrongholds:stronghold` | `nether_brick_fence` | `minecraft:deepslate_brick_wall` | 2 |
| `betterstrongholds:terminators/hallway_end_statue_chest_r` | `betterstrongholds:stronghold` | `nether_brick_wall` | `minecraft:deepslate_brick_wall` | 1 |
| `illagerwarship:illager-warship` | `illagerwarship:warship` | `nether_brick_fence` | `minecraft:deepslate_brick_wall` | 9 |
| `illagerwarship:illager-warship` | `illagerwarship:warship` | `nether_brick_slab` | `minecraft:deepslate_brick_slab` | 37 |
| `illagerwarship:illager-warship` | `illagerwarship:warship` | `nether_bricks` | `minecraft:deepslate_bricks` | 69 |
| `underground_bunkers:underground_bunker/underground_bunkers_large_room_2` | `underground_bunkers:underground_bunker` | `nether_brick_wall` | `minecraft:deepslate_brick_wall` | 14 |

## 16. Any netherrack left to magma (the ruined portals, all off, keep theirs) (3 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `eugene:red_roof` | `eugene:red_roof_castle` | `netherrack` | `minecraft:magma_block` | 3 |

## 17. Any soul sand or soil left to basalt (the ancient city and Philip's sculk dungeon keep theirs) (39 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `philipsruins:ancient_ruin_soul1` | `philipsruins:level_two_ruins_pool` | `soul_sand` | `minecraft:basalt` | 2 |
| `philipsruins:ancient_ruin_soul1` | `philipsruins:level_two_ruins_pool` | `soul_soil` | `minecraft:basalt` | 1 |
| `philipsruins:crypt_room5` | `philipsruins:ancient_crypt` | `soul_sand` | `minecraft:basalt` | 4 |
| `philipsruins:crypt_room8` | `philipsruins:ancient_crypt` | `soul_sand` | `minecraft:basalt` | 2 |
| `underground_bunkers:underground_bunker/underground_bunkers_room_7` | `underground_bunkers:underground_bunker` | `soul_sand` | `minecraft:basalt` | 30 |

## 18. Shroomlight to orange corundum (38 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `kaisyn:outpost/towers/flower_forest/base_plate` | `towns_and_towers:pillager_outpost_flower_forest` | `shroomlight` | `quark:orange_corundum` | 1 |
| `kaisyn:outpost/towers/mushroom_fields/base_plate` | `towns_and_towers:pillager_outpost_mushroom_fields` | `shroomlight` | `quark:orange_corundum` | 28 |
| `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_large_house_1` | `towns_and_towers:village_mushroom_fields` | `shroomlight` | `quark:orange_corundum` | 1 |
| `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_small_house_1` | `towns_and_towers:village_mushroom_fields` | `shroomlight` | `quark:orange_corundum` | 1 |
| `terralith:underground/giant_bee_hive_1` | `terralith:underground/giant_bee_hive` | `shroomlight` | `quark:orange_corundum` | 4 |
| `terralith:underground/giant_bee_hive_2` | `terralith:underground/giant_bee_hive` | `shroomlight` | `quark:orange_corundum` | 3 |

## 19. Any soul campfire left to a campfire (2 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterdungeons:skeleton_dungeon/skeleton_shell` | `betterdungeons:skeleton_dungeon` | `soul_campfire` | `minecraft:campfire` | 1 |
| `simply_houses:forest_fort/forest_fort` | `simply_houses:forest_fort` | `soul_campfire` | `minecraft:campfire` | 1 |

## 20. Any soul fire left to fire (the frosted dungeon's is put out above) (177 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `minecraft:ancient_city/city_center/city_center_1` | `minecraft:ancient_city` | `soul_fire` | `minecraft:fire` | 57 |
| `minecraft:ancient_city/city_center/city_center_2` | `minecraft:ancient_city` | `soul_fire` | `minecraft:fire` | 57 |
| `minecraft:ancient_city/city_center/city_center_3` | `minecraft:ancient_city` | `soul_fire` | `minecraft:fire` | 57 |
| `minecraft:ancient_city/structures/barracks` | `minecraft:ancient_city` | `soul_fire` | `minecraft:fire` | 2 |
| `philipsruins:sculk_dungeon_room1` | `philipsruins:sculk_dungeon` | `soul_fire` | `minecraft:fire` | 4 |

## 21. Any glowstone left to yellow corundum (the Mysticrift mangrove tower's is gold ore above) (5 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `gothic_church:church` | `gothic_church:church` | `glowstone` | `quark:yellow_corundum` | 4 |
| `plains_villager_tents:villager_plains_tent` | `plains_villager_tents:big_villager_tent` | `glowstone` | `quark:yellow_corundum` | 1 |

## 22. Potted Nether plants to a potted dead bush (4 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `chefsdelight:plains_chef_house` | `minecraft:village_plains`, `minecraft:village_savanna` ... | `potted_crimson_fungus` | `minecraft:potted_dead_bush` | 1 |
| `kaisyn:outpost/towers/snowy_plains/base_plate` | `towns_and_towers:pillager_outpost_snowy_plains` | `potted_crimson_roots` | `minecraft:potted_dead_bush` | 1 |
| `terralith:village/fortified/houses/cartographer` | `terralith:fortified_village` | `potted_crimson_fungus` | `minecraft:potted_dead_bush` | 1 |
| `terralith:village/fortified/houses/church` | `terralith:fortified_village` | `potted_crimson_fungus` | `minecraft:potted_dead_bush` | 1 |

## 23. Soul lanterns to lanterns, hanging kept (K25); ancient cities and sculk structures keep theirs (311 blocks)

Skipped, holding `@sculk`: `minecraft:ancient_city`, `philipsruins:bone_dungeon`, `philipsruins:sculk_dungeon`.

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterdungeons:zombie_dungeon/floor1/room/floor1_specialroom_0` | `betterdungeons:zombie_dungeon` | `soul_lantern` | `minecraft:lantern` | 1 |
| `betterstrongholds:rooms/cmd_acarii` | `betterstrongholds:stronghold` | `soul_lantern` | `minecraft:lantern` | 1 |
| `eugene:witch_wagon` | `eugene:witch_wagon` | `soul_lantern` | `minecraft:lantern` | 4 |
| `ipo:pillager_outpost/tower/top/overgrown` | `minecraft:pillager_outpost` | `soul_lantern` | `minecraft:lantern` | 3 |
| `ipo:pillager_outpost/tower/top/overgrown_active` | `minecraft:pillager_outpost` | `soul_lantern` | `minecraft:lantern` | 3 |
| `ipo:pillager_outpost/tower/top/overgrown_damaged` | `minecraft:pillager_outpost` | `soul_lantern` | `minecraft:lantern` | 3 |
| `ipo:pillager_outpost/tower/top/overgrown_damaged_active` | `minecraft:pillager_outpost` | `soul_lantern` | `minecraft:lantern` | 3 |
| `kaisyn:outpost/towers/snowy_slopes/base_plate` | `towns_and_towers:pillager_outpost_snowy_slopes` | `soul_lantern` | `minecraft:lantern` | 2 |
| `kaisyn:village/mushroom_fields_fantasy/town_centers/mushroom_meeting_point_1` | `towns_and_towers:village_mushroom_fields` | `soul_lantern` | `minecraft:lantern` | 2 |
| `kaisyn:village/swamp_boat/town_centers/swamp_b_meeting_point_1` | `towns_and_towers:village_swamp` | `soul_lantern` | `minecraft:lantern` | 1 |
| `philipsruins:deep_ruin2` | `philipsruins:underground_structures` | `soul_lantern` | `minecraft:lantern` | 3 |
| `philipsruins:deep_ruin3` | `philipsruins:underground_structures` | `soul_lantern` | `minecraft:lantern` | 1 |
| `philipsruins:deep_ruin5` | `philipsruins:underground_structures` | `soul_lantern` | `minecraft:lantern` | 1 |
| `philipsruins:deep_ruin6` | `philipsruins:underground_structures` | `soul_lantern` | `minecraft:lantern` | 5 |
| `philipsruins:deep_ruin8` | `philipsruins:underground_structures` | `soul_lantern` | `minecraft:lantern` | 5 |
| `philipsruins:deep_ruinbig1` | `philipsruins:underground_structures` | `soul_lantern` | `minecraft:lantern` | 2 |
| `philipsruins:pumpkin_ruin_four` | `philipsruins:pumpkin_ruins` | `soul_lantern` | `minecraft:lantern` | 2 |
| `philipsruins:pumpkin_ruin_six` | `philipsruins:pumpkin_ruins` | `soul_lantern` | `minecraft:lantern` | 1 |
| `simply_houses:forest_fort/forest_fort` | `simply_houses:forest_fort` | `soul_lantern` | `minecraft:lantern` | 2 |
| `simply_houses:forest_stonemason/forest_stonemason` | `simply_houses:forest_stonemason` | `soul_lantern` | `minecraft:lantern` | 1 |
| `simply_houses:plains_house/plains_house` | `simply_houses:plains_house` | `soul_lantern` | `minecraft:lantern` | 1 |
| `simply_houses:plains_mansion/plains_mansion` | `simply_houses:plains_mansion` | `soul_lantern` | `minecraft:lantern` | 1 |
| `simply_houses:taiga_forge/taiga_forge` | `simply_houses:taiga_forge` | `soul_lantern` | `minecraft:lantern` | 4 |
| `simply_houses:taiga_house/taiga_house` | `simply_houses:taiga_house` | `soul_lantern` | `minecraft:lantern` | 7 |
| `simply_houses:taiga_inn/taiga_inn` | `simply_houses:taiga_inn` | `soul_lantern` | `minecraft:lantern` | 9 |
| `simply_houses:taiga_woodcutter/taiga_woodcutter` | `simply_houses:taiga_woodcutter` | `soul_lantern` | `minecraft:lantern` | 1 |
| `terralith:spire/base_l` | `terralith:spire` | `soul_lantern` | `minecraft:lantern` | 14 |
| `terralith:spire/base_r` | `terralith:spire` | `soul_lantern` | `minecraft:lantern` | 10 |
| `terralith:spire/layer1` | `terralith:spire` | `soul_lantern` | `minecraft:lantern` | 73 |
| `terralith:spire/layer2` | `terralith:spire` | `soul_lantern` | `minecraft:lantern` | 39 |
| `terralith:spire/layer3` | `terralith:spire` | `soul_lantern` | `minecraft:lantern` | 47 |
| `terralith:spire/layer4` | `terralith:spire` | `soul_lantern` | `minecraft:lantern` | 38 |
| `terralith:underground/dungeon1` | `terralith:underground/frosted_dungeon` | `soul_lantern` | `minecraft:lantern` | 20 |
| `terralith:village/fortified/houses/graveyard` | `terralith:fortified_village` | `soul_lantern` | `minecraft:lantern` | 1 |

## 24. Soul wall torches to wall torches, facing kept (K32); ancient cities and sculk structures keep theirs (12 blocks)

Skipped, holding `@sculk`: `minecraft:ancient_city`, `philipsruins:bone_dungeon`, `philipsruins:sculk_dungeon`.

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterjungletemples:challenge_room/challenge_room_water_parkour` | `betterjungletemples:jungle_temple` | `soul_wall_torch` | `minecraft:wall_torch` | 2 |
| `betterjungletemples:floor_2/room/floor_2_parkour_room_0` | `betterjungletemples:jungle_temple` | `soul_wall_torch` | `minecraft:wall_torch` | 1 |
| `betterjungletemples:floor_2/room/floor_2_parkour_room_1` | `betterjungletemples:jungle_temple` | `soul_wall_torch` | `minecraft:wall_torch` | 1 |
| `betterjungletemples:floor_2/room/floor_2_parkour_room_2` | `betterjungletemples:jungle_temple` | `soul_wall_torch` | `minecraft:wall_torch` | 1 |
| `betterjungletemples:floor_2/room/floor_2_parkour_room_3` | `betterjungletemples:jungle_temple` | `soul_wall_torch` | `minecraft:wall_torch` | 1 |
| `betterjungletemples:floor_2/room/floor_2_parkour_room_4` | `betterjungletemples:jungle_temple` | `soul_wall_torch` | `minecraft:wall_torch` | 1 |
| `terralith:spire/base_l` | `terralith:spire` | `soul_wall_torch` | `minecraft:wall_torch` | 1 |
| `veggiesdelight:village/houses/snowy_depot_house` | `minecraft:village_snowy` | `soul_wall_torch` | `minecraft:wall_torch` | 4 |

## 25. Standing soul torches to torches (K32); ancient cities and sculk structures keep theirs (0 blocks)

Skipped, holding `@sculk`: `minecraft:ancient_city`, `philipsruins:bone_dungeon`, `philipsruins:sculk_dungeon`.

Nothing matched.

## 26. Crimson trapdoors to mangrove trapdoors, state kept (K28) (39 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterwitchhuts:witch_hut_sm` | `betterwitchhuts:witch_hut` | `crimson_trapdoor` | `minecraft:mangrove_trapdoor` | 2 |
| `simply_houses:taiga_forge/taiga_forge` | `simply_houses:taiga_forge` | `crimson_trapdoor` | `minecraft:mangrove_trapdoor` | 1 |
| `terralith:village/desert/houses/cartographer2` | `terralith:fortified_desert_village` | `crimson_trapdoor` | `minecraft:mangrove_trapdoor` | 36 |

## 27. Crimson buttons to mangrove buttons, state kept (K29) (28 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterdeserttemples:statues/statue_2` | `betterdeserttemples:desert_temple` | `crimson_button` | `minecraft:mangrove_button` | 2 |
| `betterstrongholds:rooms/junction_md` | `betterstrongholds:stronghold` | `crimson_button` | `minecraft:mangrove_button` | 2 |
| `betterstrongholds:rooms/mess` | `betterstrongholds:stronghold` | `crimson_button` | `minecraft:mangrove_button` | 2 |
| `betterstrongholds:starts/junction_lg` | `betterstrongholds:stronghold` | `crimson_button` | `minecraft:mangrove_button` | 20 |
| `kaisyn:outpost/forts/jungle/base_plate` | `towns_and_towers:pillager_outpost_jungle` | `crimson_button` | `minecraft:mangrove_button` | 2 |

## 28. Crimson slabs to mangrove slabs, state kept (K33) (6 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterdeserttemples:main_entrance_doorway/main_entrance_doorway_0` | `betterdeserttemples:desert_temple` | `crimson_slab` | `minecraft:mangrove_slab` | 6 |

## 29. Warped pressure plates to dark oak pressure plates (K30) (24 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterdeserttemples:puzzle_big/puzzle_big_4_0` | `betterdeserttemples:desert_temple` | `warped_pressure_plate` | `minecraft:dark_oak_pressure_plate` | 4 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_1` | `betterdeserttemples:desert_temple` | `warped_pressure_plate` | `minecraft:dark_oak_pressure_plate` | 4 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_2` | `betterdeserttemples:desert_temple` | `warped_pressure_plate` | `minecraft:dark_oak_pressure_plate` | 4 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_3` | `betterdeserttemples:desert_temple` | `warped_pressure_plate` | `minecraft:dark_oak_pressure_plate` | 4 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_4` | `betterdeserttemples:desert_temple` | `warped_pressure_plate` | `minecraft:dark_oak_pressure_plate` | 4 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_5` | `betterdeserttemples:desert_temple` | `warped_pressure_plate` | `minecraft:dark_oak_pressure_plate` | 4 |

## 30. Warped trapdoors to dark oak trapdoors, state kept (K31) (8 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `terralith:spire/base_l` | `terralith:spire` | `warped_trapdoor` | `minecraft:dark_oak_trapdoor` | 8 |

## 31. Warped stairs to dark oak stairs, state kept (K35) (1 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `betterdungeons:zombie_dungeon/zombie_start` | `betterdungeons:zombie_dungeon` | `warped_stairs` | `minecraft:dark_oak_stairs` | 1 |

## 32. Stripped warped hyphae to stripped dark oak wood, axis kept (K36) (1 blocks)

| Template | Structures | From | To | Blocks |
|---|---|---|---|---:|
| `philipsruins:ocean_ruin6` | `philipsruins:ocean_ruins` | `stripped_warped_hyphae` | `minecraft:stripped_dark_oak_wood` | 1 |

## Left alone (templates a Nether or End structure also places)

`crimson_slab` 80, `warped_stairs` 25, `soul_lantern` 7, `crimson_trapdoor` 5, `warped_trapdoor` 5

