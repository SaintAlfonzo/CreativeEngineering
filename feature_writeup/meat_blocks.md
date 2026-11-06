# Meat blocks: lone red blocks in structure templates

Written by `python3 tools/meat_block_scan.py`. Do not edit. Every red block (`red_glazed_terracotta`, `red_terracotta`, `red_wool`, `red_concrete`, `red_concrete_powder`, `pink_glazed_terracotta`, `red_mushroom_block`, `nether_wart_block`, `redstone_block`) in the templates of live Overworld structures. **Lone** = no neighbour of the same block. **Marked**: lone `red_glazed_terracotta` in a settlement-family structure (village, house, camp, fort, pillager) or a food-named template, to become `architects_palette:salmon_log[axis=y]` (marks in `kubejs/generation_scripts/worldgen/meat_block_swaps.json`). **Review**: every other lone red block.

## Red blocks overall

| Block | Lone | In groups |
|---|---:|---:|
| `minecraft:red_glazed_terracotta` | 6 | 0 |
| `minecraft:red_terracotta` | 71 | 865 |
| `minecraft:red_wool` | 169 | 600 |
| `minecraft:red_concrete` | 18 | 668 |
| `minecraft:red_concrete_powder` | 3 | 2 |
| `minecraft:red_mushroom_block` | 2 | 242 |
| `minecraft:nether_wart_block` | 40 | 271 |
| `minecraft:redstone_block` | 71 | 151 |

## Marked (5 blocks in 4 templates)

| Template | Structures | Positions |
|---|---|---|
| `kaisyn:village/birch_forest_romanian/houses/birch_forest_butcher_and_leatherworker_1` | `towns_and_towers:village_birch_forest` | 5 2 4 |
| `kaisyn:village/exclusives/mediterranean/houses/regular/med_butcher_1` | `towns_and_towers:exclusives/village_mediterranean` | 1 2 1 |
| `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_butcher_1` | `towns_and_towers:village_mushroom_fields` | 0 2 2, 2 2 4 |
| `skyvillages:farm_2` | `skyvillages:skyvillage` | 7 15 7 |

## Review (375 lone red blocks, not marked)

Settlement = in a settlement-family structure or a food-named template.

| Template | Structures | Settlement | Lone blocks |
|---|---|---|---|
| `animal_barn:barn` | `animal_barn:barn` | yes | red_concrete 4 |
| `campsite_structures:armorer_campsite` | `campsite_structures:armorer_campsite` | yes | red_wool 5 |
| `desert_watchtower:desert_watch_tower` | `desert_watchtower:watchtower` | yes | red_terracotta 8 |
| `etched:village/taiga/houses/taiga_bard_house_1` | `minecraft:village_taiga` | yes | red_wool 1 |
| `gothic_church:church` | `gothic_church:church` | yes | red_wool 8 |
| `kaisyn:outpost/towers/mushroom_fields/base_plate` | `towns_and_towers:pillager_outpost_mushroom_fields` | yes | red_mushroom_block 1 |
| `kaisyn:ships/pillager_outpost_ocean/base_plate` | `towns_and_towers:pillager_outpost_ocean` | yes | redstone_block 1 |
| `kaisyn:ships/pillager_outpost_ocean/variant/base_plate` | `towns_and_towers:pillager_outpost_ocean` | yes | redstone_block 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_large_house_1` | `towns_and_towers:village_badlands` | yes | red_terracotta 2 |
| `kaisyn:village/badlands_pueblo/houses/badlands_large_house_2` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_large_house_3` | `towns_and_towers:village_badlands` | yes | red_terracotta 3 |
| `kaisyn:village/badlands_pueblo/houses/badlands_large_house_4` | `towns_and_towers:village_badlands` | yes | red_terracotta 5 |
| `kaisyn:village/badlands_pueblo/houses/badlands_mason_1` | `towns_and_towers:village_badlands` | yes | red_terracotta 7 |
| `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_1_l` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_1_r` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_2_l` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_1_l` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_1_r` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_2_l` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_2_r` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_3_l` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_3_r` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_4_l` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_4_r` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_5_l` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_5_r` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_6_l` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/houses/badlands_small_house_6_r` | `towns_and_towers:village_badlands` | yes | red_terracotta 1 |
| `kaisyn:village/badlands_pueblo/town_centers/badlands_meeting_point_1` | `towns_and_towers:village_badlands` | yes | red_terracotta 12 |
| `kaisyn:village/exclusives/swedish/houses/swedish_library_and_temple_1` | `towns_and_towers:exclusives/village_swedish` | yes | red_wool 1 |
| `kaisyn:village/exclusives/wandering_trader_camp/side/trader_tent_red_1` | `towns_and_towers:exclusives/village_wandering_trader_camp` | yes | red_wool 4 |
| `kaisyn:village/grove_villager_outpost/town_centers/grove_meeting_point_1` | `towns_and_towers:village_grove` | yes | red_wool 10 |
| `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_small_farm_1` | `towns_and_towers:village_mushroom_fields` | yes | red_mushroom_block 1 |
| `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_leatherworker_1` | `towns_and_towers:village_snowy_taiga` | yes | red_wool 5 |
| `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_mason_1` | `towns_and_towers:village_snowy_taiga` | yes | red_wool 9 |
| `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_red_1` | `towns_and_towers:village_wooded_badlands` | yes | red_wool 5 |
| `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_red_1` | `towns_and_towers:village_wooded_badlands` | yes | red_wool 4 |
| `minecraft:village/savanna/houses/savanna_temple_2` | `minecraft:village_savanna` | yes | red_terracotta 6 |
| `terralith:village/desert/houses/castle` | `terralith:fortified_desert_village` | yes | red_terracotta 1 |
| `villager_circus_tent:circus_tent` | `villager_circus_tent:villager_circus_tent` | yes | red_wool 64 |
| `villager_fishermans_hut:villagerfisherman` | `villager_fishermans_hut:villager_fishermans_hut` | yes | red_concrete 4 |
| `villager_forge:villager-forge` | `villager_forge:villager_forge` | yes | red_concrete 8 |
| `wares:village/houses/desert_warehouse` | `minecraft:village_desert` | yes | red_terracotta 1 |
| `wares:village/houses/plains_warehouse` | `minecraft:village_plains`, `minecraft:village_savanna`, `minecraft:village_taiga` | yes | red_terracotta 1 |
| `wares:village/houses/savanna_warehouse` | `minecraft:village_savanna` | yes | red_terracotta 1 |
| `wares:village/houses/snowy_warehouse` | `minecraft:village_snowy` | yes | red_terracotta 1 |
| `wares:village/houses/taiga_warehouse` | `minecraft:village_taiga` | yes | red_terracotta 1 |
| `betterdeserttemples:hall_room/5x5/hall_room_5x5_9` | `betterdeserttemples:desert_temple` | no | redstone_block 4 |
| `betterdeserttemples:hall_room/drowning_trap` | `betterdeserttemples:desert_temple` | no | redstone_block 4 |
| `betterdeserttemples:hall_room/wipeout/wipeout_0` | `betterdeserttemples:desert_temple` | no | redstone_block 2 |
| `betterdeserttemples:hall_room/wipeout/wipeout_0_no_left_wall` | `betterdeserttemples:desert_temple` | no | redstone_block 2 |
| `betterdeserttemples:main/front_puzzle_connector` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:main/left_puzzle_connector` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:main/right_puzzle_connector` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_0` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_1` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_2` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_3` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_4` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_4_5` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_6_0` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_6_1` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_6_2` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_6_3` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_6_4` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:puzzle_big/puzzle_big_6_5` | `betterdeserttemples:desert_temple` | no | redstone_block 1 |
| `betterdeserttemples:statues/statue_2` | `betterdeserttemples:desert_temple` | no | red_concrete_powder 1 |
| `betterjungletemples:challenge_room/challenge_room_water_parkour` | `betterjungletemples:jungle_temple` | no | redstone_block 2 |
| `betterjungletemples:challenge_room/serpent_back/challenge_room_serpent_back_0` | `betterjungletemples:jungle_temple` | no | redstone_block 2 |
| `betterjungletemples:challenge_room/serpent_back/challenge_room_serpent_back_1` | `betterjungletemples:jungle_temple` | no | redstone_block 2 |
| `betterjungletemples:challenge_room/serpent_back/challenge_room_serpent_back_2` | `betterjungletemples:jungle_temple` | no | redstone_block 2 |
| `betterjungletemples:challenge_room/serpent_back/challenge_room_serpent_back_3` | `betterjungletemples:jungle_temple` | no | redstone_block 2 |
| `betterjungletemples:challenge_room/serpent_back/challenge_room_serpent_back_4` | `betterjungletemples:jungle_temple` | no | redstone_block 2 |
| `betterjungletemples:floor_2/room/floor_2_corridor_room_0` | `betterjungletemples:jungle_temple` | no | redstone_block 1 |
| `betterjungletemples:floor_2/room/floor_2_corridor_room_1` | `betterjungletemples:jungle_temple` | no | redstone_block 1 |
| `betterjungletemples:floor_2/room/floor_2_corridor_room_2` | `betterjungletemples:jungle_temple` | no | redstone_block 1 |
| `betterjungletemples:treasure_room/treasure_room_0` | `betterjungletemples:jungle_temple` | no | redstone_block 1 |
| `cloudstorage:big_balloon_1` | `cloudstorage:big_balloon` | no | red_wool 41 |
| `cloudstorage:big_balloon_2` | `cloudstorage:big_balloon` | no | red_wool 1 |
| `eugene:red_roof` | `eugene:red_roof_castle` | no | red_wool 2 |
| `eugene:ruined_drum_tower` | `eugene:ruined_drum_tower` | no | red_concrete 2, redstone_block 2 |
| `hopo:underwater_ruins/copper/3` | `hopo:underwater_ruins` | no | redstone_block 2 |
| `minecraft:ancient_city/city_center/city_center_1` | `minecraft:ancient_city` | no | redstone_block 2 |
| `minecraft:ancient_city/city_center/city_center_2` | `minecraft:ancient_city` | no | redstone_block 2 |
| `minecraft:ancient_city/city_center/city_center_3` | `minecraft:ancient_city` | no | redstone_block 2 |
| `philipsruins:desert_ruin3` | `philipsruins:desert_structures` | no | red_terracotta 1 |
| `philipsruins:field_stone_ruins1` | `philipsruins:field_stone_ruins` | no | red_terracotta 5, red_concrete_powder 2 |
| `philipsruins:sculk_dungeon_room1` | `philipsruins:sculk_dungeon` | no | redstone_block 1 |
| `skyvillages:cartographer_1` | `skyvillages:skyvillage` | no | redstone_block 4 |
| `skyvillages:letsdo_brewery/sky_village_letsdo_brewery_0` | `skyvillages:skyvillage` | no | red_wool 2 |
| `skyvillages:letsdo_brewery/sky_village_letsdo_brewery_1` | `skyvillages:skyvillage` | no | red_wool 7 |
| `skyvillages:letsdo_vinery/sky_village_letsdo_vinery_2` | `skyvillages:skyvillage` | no | red_glazed_terracotta 1 |
| `skyvillages:librarian_1` | `skyvillages:skyvillage` | no | redstone_block 1 |
| `terralith:spire/base_l` | `terralith:spire` | no | redstone_block 1 |
| `underground_bunkers:underground_bunker/underground_bunkers_large_room_0` | `underground_bunkers:underground_bunker` | no | redstone_block 3 |
| `underground_bunkers:underground_bunker/underground_bunkers_large_room_1` | `underground_bunkers:underground_bunker` | no | redstone_block 5 |
| `underground_bunkers:underground_bunker/underground_bunkers_passage_0` | `underground_bunkers:underground_bunker` | no | redstone_block 1 |
| `unwrecked_ships:ship/sails/nether/crimson_sails` | `unwrecked_ships:ship` | no | nether_wart_block 4 |
| `unwrecked_ships:ship/sails/nether/horizontal_crimson_sails_primary` | `unwrecked_ships:ship` | no | nether_wart_block 4 |
| `unwrecked_ships:ship/sails/nether/horizontal_crimson_sails_secondary` | `unwrecked_ships:ship` | no | nether_wart_block 2 |
| `unwrecked_ships:ship/sails/nether/vertical_crimson_sails_primary` | `unwrecked_ships:ship` | no | nether_wart_block 16 |
| `unwrecked_ships:ship/sails/nether/vertical_crimson_sails_secondary` | `unwrecked_ships:ship` | no | nether_wart_block 14 |
