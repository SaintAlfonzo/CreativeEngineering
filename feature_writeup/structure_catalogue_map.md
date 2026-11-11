# Structure Catalogue: family map

Companion to [plan_structure_catalogue.md](plan_structure_catalogue.md). Snapshot built 2026-11-09 from `kubejs/exported/structures.json` and `structure_tags.json`; the live build applies the rules in the plan, so it stays correct when this file ages.

Reading a line: `- <biome column> (<count>): <items>`. Each item goes one after another along +Z in that column. `name{1-4}` is shorthand for numbered variations.

Every other level runs along +X. Function headings are in bold; families without a function heading skip that level.

Live build differs from this snapshot in two places (plan section 8): `towns_and_towers:village_ocean` is placed whole, and 35 structures are dried rather than 33.

| Count | What |
|---|---|
| 1606 | placed in families (1328 village and sky village pieces, 278 whole structures) |
| 44 | unmapped: no `type/*` tag, placed along -X |
| 133 | skipped: Terralith biome markers (`terralith:zpointer/*`), which contain no blocks |
| 46 | skipped: pieces that only hold a villager or golem |

# Families

## Village (1292)

### Villager Structures (201)

**Workplaces** (40)

- Desert (7): `advancedperipherals:villages/scientist_desert`, `immersiveengineering:village/houses/desert_engineer`, `immersiveengineering:village/houses/desert_machinist`, `monobank:village/monobank/desert_locked`, `monobank:village/monobank/desert_unlocked`, `veggiesdelight:village/houses/desert_depot_house`, `wares:village/houses/desert_warehouse`
- Plains (7): `advancedperipherals:villages/scientist_plains`, `immersiveengineering:village/houses/plains_engineer`, `immersiveengineering:village/houses/plains_machinist`, `monobank:village/monobank/plains_locked`, `monobank:village/monobank/plains_unlocked`, `veggiesdelight:village/houses/plains_depot_house`, `wares:village/houses/plains_warehouse`
- Savanna (7): `advancedperipherals:villages/scientist_savanna`, `immersiveengineering:village/houses/savanna_engineer`, `immersiveengineering:village/houses/savanna_machinist`, `monobank:village/monobank/savanna_locked`, `monobank:village/monobank/savanna_unlocked`, `veggiesdelight:village/houses/savanna_depot_house`, `wares:village/houses/savanna_warehouse`
- Snowy (7): `advancedperipherals:villages/scientist_snowy`, `immersiveengineering:village/houses/snowy_engineer`, `immersiveengineering:village/houses/snowy_machinist`, `monobank:village/monobank/snowy_locked`, `monobank:village/monobank/snowy_unlocked`, `veggiesdelight:village/houses/snowy_depot_house`, `wares:village/houses/snowy_warehouse`
- Taiga (7): `advancedperipherals:villages/scientist_taiga`, `immersiveengineering:village/houses/taiga_engineer`, `immersiveengineering:village/houses/taiga_machinist`, `monobank:village/monobank/taiga_locked`, `monobank:village/monobank/taiga_unlocked`, `veggiesdelight:village/houses/taiga_depot_house`, `wares:village/houses/taiga_warehouse`
- Wandering Trader Camp (2): `kaisyn:village/exclusives/wandering_trader_camp/side/trader_tent_blue_1`, `kaisyn:village/exclusives/wandering_trader_camp/side/trader_tent_red_1`
- Any (3): `dustrial_decor:village/pillager_prison`, `dustrial_decor:village/workshop`, `monobank:village/monobank/empty`

**Animal pens** (36)

- Desert (3): `incubation:village/desert_chicken_coop_1`, `minecraft:village/desert/houses/desert_animal_pen_{1-2}`
- Flower Forest Japanese (4): `kaisyn:village/flower_forest_japanese/houses/flower_forest_animal_pen_{1-3}`, `kaisyn:village/flower_forest_japanese/houses/flower_forest_stable_1`
- Forest Ruins (4): `kaisyn:village/forest_ruins/houses/forest_animal_pen_{1-3}`, `kaisyn:village/forest_ruins/houses/forest_stable_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_animal_pen_1`
- Plains (6): `incubation:village/plains_chicken_coop_1`, `minecraft:village/plains/houses/plains_animal_pen_{1-3}`, `minecraft:village/plains/houses/plains_stable_{1-2}`
- Plains zombie (2): `minecraft:village/plains/zombie/houses/plains_animal_pen_3`, `minecraft:village/plains/zombie/houses/plains_stable_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/houses/rustic_barn_professions_1`
- Savanna (4): `incubation:village/savanna_chicken_coop_1`, `minecraft:village/savanna/houses/savanna_animal_pen_{1-3}`
- Savanna zombie (2): `minecraft:village/savanna/zombie/houses/savanna_animal_pen_{2-3}`
- Snowy (3): `incubation:village/snowy_chicken_coop_1`, `minecraft:village/snowy/houses/snowy_animal_pen_{1-2}`
- Taiga (2): `incubation:village/taiga_chicken_coop_1`, `minecraft:village/taiga/houses/taiga_animal_pen_1`
- Terralith Fortified (2): `terralith:village/fortified/houses/barn`, `terralith:village/fortified/houses/stable`
- Wandering Trader Camp (2): `kaisyn:village/exclusives/wandering_trader_camp/side/trader_llama_pen_{1-2}`

**Temples** (24)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/houses/badlands_temple_1`
- Classic (1): `kaisyn:village/exclusives/classic/houses/classic_church_1`
- Desert (2): `minecraft:village/desert/houses/desert_temple_{1-2}`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_temple_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_temple_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/houses/iberian_temple_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_temple_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_temple_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_temple_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_temple_1`
- Plains (2): `minecraft:village/plains/houses/plains_temple_{3-4}`
- Rustic (1): `kaisyn:village/exclusives/rustic/houses/rustic_temple_1`
- Savanna (2): `minecraft:village/savanna/houses/savanna_temple_{1-2}`
- Snowy (1): `minecraft:village/snowy/houses/snowy_temple_1`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_temple_1`
- Swamp Boat (1): `kaisyn:village/swamp_boat/houses/swamp_b_farm_and_temple_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_temple_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_temple_1`
- Terralith Desert (1): `terralith:village/desert/houses/chapel1`
- Terralith Fortified (1): `terralith:village/fortified/houses/church`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_temple_1`

**Town centres** (68)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/town_centers/badlands_meeting_point_1`
- Beach Lighthouse (1): `kaisyn:village/beach_lighthouse/town_centers/beach_meeting_point_1`
- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/town_centers/birch_forest_meeting_point_1`
- Classic (2): `kaisyn:village/exclusives/classic/town_centers/classic_meeting_point_1/well_bottom`, `kaisyn:village/exclusives/classic/town_centers/classic_meeting_point_1/well_top`
- Desert (3): `minecraft:village/desert/town_centers/desert_meeting_point_{1-3}`
- Desert zombie (3): `minecraft:village/desert/zombie/town_centers/desert_meeting_point_{1-3}`
- Flower Forest Japanese (2): `kaisyn:village/flower_forest_japanese/town_centers/flower_forest_meeting_point_{1-2}`
- Forest Ruins (2): `kaisyn:village/forest_ruins/town_centers/forest_meeting_point_{1-2}`
- Grove Villager Outpost (1): `kaisyn:village/grove_villager_outpost/town_centers/grove_meeting_point_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/town_centers/iberian_meeting_point_1`
- Jungle Tribal (3): `kaisyn:village/jungle_tribal/town_centers/jungle_meeting_point_{1-3}`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/town_centers/meadow_meeting_point_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/town_centers/med_meeting_point_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/town_centers/mushroom_meeting_point_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/base_plate`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/town_centers/old_growth_taiga_meeting_point_1`
- Plains (6): `minecraft:village/plains/houses/plains_meeting_point_{4-5}`, `minecraft:village/plains/town_centers/plains_fountain_1`, `minecraft:village/plains/town_centers/plains_meeting_point_{1-3}`
- Plains zombie (6): `minecraft:village/plains/zombie/houses/plains_meeting_point_{4-5}`, `minecraft:village/plains/zombie/town_centers/plains_fountain_1`, `minecraft:village/plains/zombie/town_centers/plains_meeting_point_{1-3}`
- Rustic (1): `kaisyn:village/exclusives/rustic/town_centers/rustic_meeting_point_1`
- Savanna (4): `minecraft:village/savanna/town_centers/savanna_meeting_point_{1-4}`
- Savanna zombie (4): `minecraft:village/savanna/zombie/town_centers/savanna_meeting_point_{1-4}`
- Snowy (3): `minecraft:village/snowy/town_centers/snowy_meeting_point_{1-3}`
- Snowy Slopes Inn (1): `kaisyn:village/snowy_slopes_inn/town_centers/snowy_slopes_meeting_point_1`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/town_centers/snowy_taiga_meeting_point_1`
- Snowy zombie (3): `minecraft:village/snowy/zombie/town_centers/snowy_meeting_point_{1-3}`
- Sparse Jungle Polynesian (1): `kaisyn:village/sparse_jungle_polynesian/town_centers/sparse_jungle_meeting_point_1`
- Swamp Boat (1): `kaisyn:village/swamp_boat/town_centers/swamp_b_meeting_point_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/town_centers/swedish_meeting_point_1`
- Taiga (2): `minecraft:village/taiga/town_centers/taiga_meeting_point_{1-2}`
- Taiga zombie (2): `minecraft:village/taiga/zombie/town_centers/taiga_meeting_point_{1-2}`
- Terralith Fortified (1): `terralith:village/fortified/start_castle`
- Tudor (1): `kaisyn:village/exclusives/tudor/town_centers/tudor_meeting_point_1`
- Wandering Trader Camp (1): `kaisyn:village/exclusives/wandering_trader_camp/town_centers/trader_meeting_point_1`
- Wooded Badlands Tipi (3): `kaisyn:village/wooded_badlands_tipi/town_centers/wooded_badlands_meeting_point_{1-3}`
- Any (1): `minecraft:village/common/well_bottom`

**Farms** (30)

- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_small_farm_1`
- Classic (2): `kaisyn:village/exclusives/classic/houses/classic_large_farm_1`, `kaisyn:village/exclusives/classic/houses/classic_small_farm_1`
- Desert (3): `minecraft:village/desert/houses/desert_farm_{1-2}`, `minecraft:village/desert/houses/desert_large_farm_1`
- Flower Forest Japanese (2): `kaisyn:village/flower_forest_japanese/houses/flower_forest_large_farm_1`, `kaisyn:village/flower_forest_japanese/houses/flower_forest_small_farm_1`
- Forest Ruins (2): `kaisyn:village/forest_ruins/houses/forest_large_farm_1`, `kaisyn:village/forest_ruins/houses/forest_small_farm_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/houses/iberian_garden_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_small_farm_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_small_farm_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_small_farm_1`
- Plains (2): `minecraft:village/plains/houses/plains_large_farm_1`, `minecraft:village/plains/houses/plains_small_farm_1`
- Savanna (3): `minecraft:village/savanna/houses/savanna_large_farm_{1-2}`, `minecraft:village/savanna/houses/savanna_small_farm`
- Savanna zombie (1): `minecraft:village/savanna/zombie/houses/savanna_large_farm_2`
- Snowy (2): `minecraft:village/snowy/houses/snowy_farm_{1-2}`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_small_farm_1`
- Sparse Jungle Polynesian (2): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_large_farm_1`, `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_small_farm_1`
- Taiga (3): `minecraft:village/taiga/houses/taiga_large_farm_{1-2}`, `minecraft:village/taiga/houses/taiga_small_farm_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_large_farm_2`
- Terralith Desert (1): `terralith:village/desert/houses/farm1`

**Other** (1)

- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_empty_piece_1`

**Gates and towers** (2)

- Flower Forest Japanese (2): `kaisyn:village/flower_forest_japanese/flower_forest_gate_{1-2}`

### Decor and Infrastructure (506)

**Decor** (36)

- Birch Forest Romanian (3): `kaisyn:village/birch_forest_romanian/birch_forest_decor_{1-3}`
- Desert (1): `waystones:village/desert/waystone`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/flower_forest_planter_1`
- Grove Villager Outpost (1): `kaisyn:village/grove_villager_outpost/grove_table_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/iberian_plants_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/jungle_decor_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/med_planter_1`
- Modded (3): `kaisyn:village/modded/waystones/waystone_default`, `kaisyn:village/modded/waystones/waystone_desert`, `kaisyn:village/modded/waystones/waystone_mossy`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/old_growth_taiga_planter_1`
- Plains (1): `minecraft:village/plains/houses/plains_accessory_1`
- Snowy Slopes Inn (1): `kaisyn:village/snowy_slopes_inn/snowy_slopes_table_1`
- Snowy Taiga Viking (2): `kaisyn:village/snowy_taiga_viking/snowy_taiga_drakkar_1`, `kaisyn:village/snowy_taiga_viking/snowy_taiga_wood_pile_1`
- Sunflower Plains Farm (7): `kaisyn:village/sunflower_plains_farm/side/sunflower_plains_farm_beetroot_1`, `kaisyn:village/sunflower_plains_farm/side/sunflower_plains_farm_pumpkin_1`, `kaisyn:village/sunflower_plains_farm/side/sunflower_plains_farm_wheat_1`, `kaisyn:village/sunflower_plains_farm/side/sunflower_plains_windmill_1`, `kaisyn:village/sunflower_plains_farm/sunflower_plains_scarecrow_{1-2}`, `kaisyn:village/sunflower_plains_farm/town_centers/sunflower_plains_meeting_point_1`
- Taiga (6): `minecraft:village/taiga/taiga_decoration_{1-6}`
- Tudor (1): `kaisyn:village/exclusives/tudor/tudor_table_1`
- Any (5): `dustrial_decor:village/cardboard_castle`, `minecraft:village/decays/grass_9x9`, `minecraft:village/decays/grass_11x13`, `minecraft:village/decays/grass_16x16`, `waystones:village/common/waystone`

**Animals** (23)

- Meadow Swiss (1): `kaisyn:village/meadow_swiss/animals/dog`
- Mushroom Fields Fantasy (2): `kaisyn:village/mushroom_fields_fantasy/animals/mooshroom_brown`, `kaisyn:village/mushroom_fields_fantasy/animals/mooshroom_red`
- Any (20): `incubation:village/chickens_1`, `minecraft:village/common/animals/cat_black`, `minecraft:village/common/animals/cat_british`, `minecraft:village/common/animals/cat_calico`, `minecraft:village/common/animals/cat_jellie`, `minecraft:village/common/animals/cat_persian`, `minecraft:village/common/animals/cat_ragdoll`, `minecraft:village/common/animals/cat_red`, `minecraft:village/common/animals/cat_siamese`, `minecraft:village/common/animals/cat_tabby`, `minecraft:village/common/animals/cat_white`, `minecraft:village/common/animals/cows_1`, `minecraft:village/common/animals/horses_{1-5}`, `minecraft:village/common/animals/pigs_1`, `minecraft:village/common/animals/sheep_{1-2}`

**Lamps** (19)

- Badlands Pueblo (3): `kaisyn:village/badlands_pueblo/badlands_lamp_{1-3}`
- Desert (1): `minecraft:village/desert/desert_lamp_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/flower_forest_lamp_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/iberian_lamp_post_1`
- Jungle Tribal (3): `kaisyn:village/jungle_tribal/jungle_lamp_{1-3}`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/meadow_lamp_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/med_lamp_1`
- Plains (1): `minecraft:village/plains/plains_lamp_1`
- Savanna (1): `minecraft:village/savanna/savanna_lamp_post_1`
- Snowy (3): `minecraft:village/snowy/snowy_lamp_post_{1-3}`
- Sparse Jungle Polynesian (1): `kaisyn:village/sparse_jungle_polynesian/sparse_jungle_tiki_torch_1`
- Taiga (1): `minecraft:village/taiga/taiga_lamp_post_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/tudor_lamp_1`

**Streets** (428)

- Badlands Pueblo (6): `kaisyn:village/badlands_pueblo/streets/crossroad_{1-3}`, `kaisyn:village/badlands_pueblo/streets/straight_1`, `kaisyn:village/badlands_pueblo/terminators/terminator_{1-2}`
- Birch Forest Romanian (20): `kaisyn:village/birch_forest_romanian/streets/corner_{1-3}`, `kaisyn:village/birch_forest_romanian/streets/crossroad_{1-6}`, `kaisyn:village/birch_forest_romanian/streets/straight_{1-6}`, `kaisyn:village/birch_forest_romanian/streets/turn_1`, `kaisyn:village/birch_forest_romanian/terminators/terminator_{1-4}`
- Classic (6): `kaisyn:village/exclusives/classic/streets/crossroad_{1-3}`, `kaisyn:village/exclusives/classic/streets/straight_{1-3}`
- Desert (27): `minecraft:village/desert/streets/corner_{1-2}`, `minecraft:village/desert/streets/crossroad_{1-3}`, `minecraft:village/desert/streets/square_{1-2}`, `minecraft:village/desert/streets/straight_{1-3}`, `minecraft:village/desert/streets/turn_1`, `minecraft:village/desert/terminators/terminator_{1-2}`, `minecraft:village/desert/zombie/streets/corner_{1-2}`, `minecraft:village/desert/zombie/streets/crossroad_{1-3}`, `minecraft:village/desert/zombie/streets/square_{1-2}`, `minecraft:village/desert/zombie/streets/straight_{1-3}`, `minecraft:village/desert/zombie/streets/turn_1`, `minecraft:village/desert/zombie/terminators/terminator_2`, `monobank:village/streets/desert_straight_fix_1`, `pneumaticcraft:villages/custom_street_desert`
- Flower Forest Japanese (20): `kaisyn:village/flower_forest_japanese/streets/corner_{1-3}`, `kaisyn:village/flower_forest_japanese/streets/crossroad_{1-6}`, `kaisyn:village/flower_forest_japanese/streets/straight_{1-6}`, `kaisyn:village/flower_forest_japanese/streets/turn_1`, `kaisyn:village/flower_forest_japanese/terminators/terminator_{1-4}`
- Forest Ruins (20): `kaisyn:village/forest_ruins/streets/corner_{1-3}`, `kaisyn:village/forest_ruins/streets/crossroad_{1-6}`, `kaisyn:village/forest_ruins/streets/straight_{1-6}`, `kaisyn:village/forest_ruins/streets/turn_1`, `kaisyn:village/forest_ruins/terminators/terminator_{1-4}`
- Iberian (18): `kaisyn:village/exclusives/iberian/streets/corner_{1-4}`, `kaisyn:village/exclusives/iberian/streets/crossroad_{1-4}`, `kaisyn:village/exclusives/iberian/streets/straight_{1-4}`, `kaisyn:village/exclusives/iberian/terminators/terminator_{1-6}`
- Jungle Tribal (20): `kaisyn:village/jungle_tribal/streets/corner_{1-3}`, `kaisyn:village/jungle_tribal/streets/crossroad_{1-6}`, `kaisyn:village/jungle_tribal/streets/straight_{1-6}`, `kaisyn:village/jungle_tribal/streets/turn_1`, `kaisyn:village/jungle_tribal/terminators/terminator_{1-4}`
- Meadow Swiss (15): `kaisyn:village/meadow_swiss/streets/corner_{1-3}`, `kaisyn:village/meadow_swiss/streets/crossroad_{1-4}`, `kaisyn:village/meadow_swiss/streets/straight_{1-5}`, `kaisyn:village/meadow_swiss/terminators/terminator_{1-3}`
- Mediterranean (12): `kaisyn:village/exclusives/mediterranean/streets/avenue/avenue_{1-5}`, `kaisyn:village/exclusives/mediterranean/streets/promenade/promenade_{1-3}`, `kaisyn:village/exclusives/mediterranean/terminators/avenue/avenue_end_{1-2}`, `kaisyn:village/exclusives/mediterranean/terminators/promenade/promenade_end_{1-2}`
- Mushroom Fields Fantasy (6): `kaisyn:village/mushroom_fields_fantasy/streets/crossroad_1`, `kaisyn:village/mushroom_fields_fantasy/streets/straight_{1-3}`, `kaisyn:village/mushroom_fields_fantasy/streets/turn_1`, `kaisyn:village/mushroom_fields_fantasy/terminators/terminator_1`
- Old Growth Taiga Polish (8): `kaisyn:village/old_growth_taiga_polish/streets/corner_{1-2}`, `kaisyn:village/old_growth_taiga_polish/streets/crossroad_{1-2}`, `kaisyn:village/old_growth_taiga_polish/streets/straight_{1-2}`, `kaisyn:village/old_growth_taiga_polish/terminators/terminator_{1-2}`
- Plains (37): `minecraft:village/plains/streets/corner_{1-3}`, `minecraft:village/plains/streets/crossroad_{1-6}`, `minecraft:village/plains/streets/straight_{1-6}`, `minecraft:village/plains/streets/turn_1`, `minecraft:village/plains/terminators/terminator_{1-4}`, `minecraft:village/plains/zombie/streets/corner_{1-3}`, `minecraft:village/plains/zombie/streets/crossroad_{1-6}`, `minecraft:village/plains/zombie/streets/straight_{1-6}`, `minecraft:village/plains/zombie/streets/turn_1`, `monobank:village/streets/plains_straight_fix_1`
- Rustic (14): `kaisyn:village/exclusives/rustic/streets/corner_{1-3}`, `kaisyn:village/exclusives/rustic/streets/crossroad_{1-4}`, `kaisyn:village/exclusives/rustic/streets/straight_{1-5}`, `kaisyn:village/exclusives/rustic/terminators/terminator_{1-2}`
- Savanna (41): `minecraft:village/savanna/streets/corner_{1,3}`, `minecraft:village/savanna/streets/crossroad_{2-7}`, `minecraft:village/savanna/streets/split_{1-2}`, `minecraft:village/savanna/streets/straight_{2,4-6,8-11}`, `minecraft:village/savanna/streets/turn_1`, `minecraft:village/savanna/terminators/terminator_5`, `minecraft:village/savanna/zombie/streets/corner_{1,3}`, `minecraft:village/savanna/zombie/streets/crossroad_{2-7}`, `minecraft:village/savanna/zombie/streets/split_{1-2}`, `minecraft:village/savanna/zombie/streets/straight_{2,4-6,8-11}`, `minecraft:village/savanna/zombie/streets/turn_1`, `minecraft:village/savanna/zombie/terminators/terminator_5`, `monobank:village/streets/savanna_straight_fix_1`
- Snowy (36): `minecraft:village/snowy/streets/corner_{1-3}`, `minecraft:village/snowy/streets/crossroad_{1-6}`, `minecraft:village/snowy/streets/square_1`, `minecraft:village/snowy/streets/straight_{1-4,6,8}`, `minecraft:village/snowy/streets/turn_1`, `minecraft:village/snowy/zombie/streets/corner_{1-3}`, `minecraft:village/snowy/zombie/streets/crossroad_{1-6}`, `minecraft:village/snowy/zombie/streets/square_1`, `minecraft:village/snowy/zombie/streets/straight_{1-4,6,8}`, `minecraft:village/snowy/zombie/streets/turn_1`, `monobank:village/streets/snowy_straight_fix_1`, `pneumaticcraft:villages/custom_street_snowy`
- Snowy Taiga Viking (20): `kaisyn:village/snowy_taiga_viking/streets/corner_{1-3}`, `kaisyn:village/snowy_taiga_viking/streets/crossroad_{1-6}`, `kaisyn:village/snowy_taiga_viking/streets/straight_{1-6}`, `kaisyn:village/snowy_taiga_viking/streets/turn_1`, `kaisyn:village/snowy_taiga_viking/terminators/terminator_{1-4}`
- Sparse Jungle Polynesian (10): `kaisyn:village/sparse_jungle_polynesian/streets/corner_{1-2}`, `kaisyn:village/sparse_jungle_polynesian/streets/crossroad_{1-3}`, `kaisyn:village/sparse_jungle_polynesian/streets/straight_{1-4}`, `kaisyn:village/sparse_jungle_polynesian/terminators/terminator_1`
- Sunflower Plains Farm (1): `kaisyn:village/sunflower_plains_farm/streets/turn_1`
- Swamp Boat (9): `kaisyn:village/swamp_boat/streets/corner_1`, `kaisyn:village/swamp_boat/streets/crossroad_{1-2}`, `kaisyn:village/swamp_boat/streets/straight_{1-2}`, `kaisyn:village/swamp_boat/streets/turn_1`, `kaisyn:village/swamp_boat/terminators/terminator_{1-3}`
- Swedish (13): `kaisyn:village/exclusives/swedish/streets/corner_{1-3}`, `kaisyn:village/exclusives/swedish/streets/crossroad_{1-4}`, `kaisyn:village/exclusives/swedish/streets/straight_{1-5}`, `kaisyn:village/exclusives/swedish/terminators/terminator_1`
- Taiga (33): `minecraft:village/taiga/streets/corner_{1-3}`, `minecraft:village/taiga/streets/crossroad_{1-6}`, `minecraft:village/taiga/streets/straight_{1-6}`, `minecraft:village/taiga/streets/turn_1`, `minecraft:village/taiga/zombie/streets/corner_{1-3}`, `minecraft:village/taiga/zombie/streets/crossroad_{1-6}`, `minecraft:village/taiga/zombie/streets/straight_{1-6}`, `minecraft:village/taiga/zombie/streets/turn_1`, `monobank:village/streets/taiga_straight_fix_1`
- Terralith Desert (15): `terralith:village/desert/roads/bend{1-2}`, `terralith:village/desert/roads/inter1`, `terralith:village/desert/roads/straight{1-6}`, `terralith:village/desert/roads/t{1-2}`, `terralith:village/desert/terminators/term{1-4}`
- Terralith Fortified (13): `terralith:village/fortified/roads/bend{1-2}`, `terralith:village/fortified/roads/inter{1-2}`, `terralith:village/fortified/roads/intersection_big`, `terralith:village/fortified/roads/straight{1-2}`, `terralith:village/fortified/roads/t{1-2}`, `terralith:village/fortified/terminators/term{1-3,5}`
- Tudor (8): `kaisyn:village/exclusives/tudor/streets/corner_{1-2}`, `kaisyn:village/exclusives/tudor/streets/crossroad_{1-2}`, `kaisyn:village/exclusives/tudor/streets/straight_{1-2}`, `kaisyn:village/exclusives/tudor/terminators/terminator_{1-2}`

### Villager Houses (585)

**Other homes** (60)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/houses/badlands_misc_professions_1`
- Beach Lighthouse (3): `kaisyn:village/beach_lighthouse/side/beach_giant_cross_1`, `kaisyn:village/beach_lighthouse/side/beach_main_house_1`, `kaisyn:village/beach_lighthouse/side/beach_outdoor_shack_1`
- Desert (7): `etched:village/desert/houses/desert_bard_house_1`, `farmersdelight:village/houses/desert_compost_pile`, `immersiveengineering:village/houses/desert_electrician`, `immersiveengineering:village/houses/desert_gunsmith`, `immersiveengineering:village/houses/desert_outfitter`, `monobank:village/houses/desert_vault`, `pneumaticcraft:villages/mechanic_house_desert`
- Flower Forest Japanese (2): `kaisyn:village/flower_forest_japanese/houses/flower_forest_park_{1-2}`
- Mushroom Fields Fantasy (2): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_heavy_professions_gazebo`, `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_light_professions_gazebo`
- Plains (7): `etched:village/plains/houses/plains_bard_house_1`, `farmersdelight:village/houses/plains_compost_pile`, `immersiveengineering:village/houses/plains_electrician`, `immersiveengineering:village/houses/plains_gunsmith`, `immersiveengineering:village/houses/plains_outfitter`, `monobank:village/houses/plains_vault`, `pneumaticcraft:villages/mechanic_house_plains`
- Savanna (7): `etched:village/savanna/houses/savanna_bard_house_1`, `farmersdelight:village/houses/savanna_compost_pile`, `immersiveengineering:village/houses/savanna_electrician`, `immersiveengineering:village/houses/savanna_gunsmith`, `immersiveengineering:village/houses/savanna_outfitter`, `monobank:village/houses/savanna_vault`, `pneumaticcraft:villages/mechanic_house_savanna`
- Snowy (7): `etched:village/snowy/houses/snowy_bard_house_1`, `farmersdelight:village/houses/snowy_compost_pile`, `immersiveengineering:village/houses/snowy_electrician`, `immersiveengineering:village/houses/snowy_gunsmith`, `immersiveengineering:village/houses/snowy_outfitter`, `monobank:village/houses/snowy_vault`, `pneumaticcraft:villages/mechanic_house_snowy`
- Taiga (7): `etched:village/taiga/houses/taiga_bard_house_1`, `farmersdelight:village/houses/taiga_compost_pile`, `immersiveengineering:village/houses/taiga_electrician`, `immersiveengineering:village/houses/taiga_gunsmith`, `immersiveengineering:village/houses/taiga_outfitter`, `monobank:village/houses/taiga_vault`, `pneumaticcraft:villages/mechanic_house_taiga`
- Terralith Desert (8): `terralith:village/desert/houses/armorsmith1`, `terralith:village/desert/houses/castle`, `terralith:village/desert/houses/generic{1-6}`
- Terralith Fortified (8): `terralith:village/fortified/houses/archer`, `terralith:village/fortified/houses/generic{1-5}`, `terralith:village/fortified/houses/graveyard`, `terralith:village/fortified/houses/smith1`
- Any (1): `dustrial_decor:village/insane_house`

**Mason** (22)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/houses/badlands_mason_1`
- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_mason_1`
- Desert (1): `minecraft:village/desert/houses/desert_mason_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_mason_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_mason_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_mason_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_mason_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/heavy_professions/mushroom_mason_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_mason_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_mason_1`
- Plains (1): `minecraft:village/plains/houses/plains_masons_house_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_mason_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_mason_1`
- Snowy (2): `minecraft:village/snowy/houses/snowy_masons_house_{1-2}`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_mason_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_masons_house_1`
- Terralith Desert (1): `terralith:village/desert/houses/mason1`
- Terralith Fortified (1): `terralith:village/fortified/houses/mason`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_mason_1`
- Any (2): `dustrial_decor:village/mason`, `dustrial_decor:village/mason_2`

**Toolsmith** (21)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/houses/badlands_toolsmith_and_weaponsmith_1`
- Desert (1): `minecraft:village/desert/houses/desert_tool_smith_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_toolsmith_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_toolsmith_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_toolsmith_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_toolsmith_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_toolsmith_and_weaponsmith_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/heavy_professions/mushroom_toolsmith_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_toolsmith_and_weaponsmith_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_toolsmith_and_weaponsmith_1`
- Plains (1): `minecraft:village/plains/houses/plains_tool_smith_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_toolsmith_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_tool_smith_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_tool_smith_1`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_toolsmith_and_weaponsmith_1`
- Sparse Jungle Polynesian (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_toolsmith_and_weaponsmith_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_toolsmith_and_weaponsmith_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_tool_smith_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_tool_smith_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_toolsmith_and_weaponsmith_1`
- Any (1): `dustrial_decor:village/tool_smith`

**Armorer** (24)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/houses/badlands_armorer_1`
- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_armorer_and_toolsmith_1`
- Desert (1): `minecraft:village/desert/houses/desert_armorer_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_armorer_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_armorer_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_armorer_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_armorer_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_armorer_and_leatherworker_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_armorer_and_weaponsmith_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/heavy_professions/mushroom_armorer_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_armorer_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_armorer_and_butcher_1`
- Plains (1): `minecraft:village/plains/houses/plains_armorer_house_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_armorer_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_armorer_1`
- Snowy (2): `minecraft:village/snowy/houses/snowy_armorer_house_{1-2}`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_armorer_1`
- Sparse Jungle Polynesian (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_armorer_and_mason_1`
- Swamp Boat (1): `kaisyn:village/swamp_boat/houses/swamp_b_armorer_and_toolsmith_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_armorer_and_mason_1`
- Taiga (2): `minecraft:village/taiga/houses/taiga_armorer_2`, `minecraft:village/taiga/houses/taiga_armorer_house_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_butcher_and_armorer_1`

**Cartographer** (26)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/houses/badlands_cartographer_1`
- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_cartographer_and_library_1`
- Desert (1): `minecraft:village/desert/houses/desert_cartographer_house_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_cartographer_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_cartographer_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_cartographer_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_cartographer_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_cartographer_and_library_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_cartographer_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/light_professions/mushroom_cartographer_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_cartographer_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_cartographer_1`
- Plains (1): `minecraft:village/plains/houses/plains_cartographer_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_cartographer_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_cartographer_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_cartographer_house_1`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_cartographer_1`
- Sparse Jungle Polynesian (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_cartographer_and_library_1`
- Swamp Boat (1): `kaisyn:village/swamp_boat/houses/swamp_b_cartographer_and_shepherd_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_cartographer_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_cartographer_house_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_cartographer_house_1`
- Terralith Desert (2): `terralith:village/desert/houses/cartographer{1-2}`
- Terralith Fortified (1): `terralith:village/fortified/houses/cartographer`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_cartographer_and_fletcher_1`

**Large homes** (35)

- Badlands Pueblo (4): `kaisyn:village/badlands_pueblo/houses/badlands_large_house_{1-4}`
- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_large_house_1`
- Classic (1): `kaisyn:village/exclusives/classic/houses/classic_large_house_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_large_house_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_large_house_1`
- Iberian (2): `kaisyn:village/exclusives/iberian/houses/iberian_large_house_{1-2}`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_large_house_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_large_house_1`
- Mediterranean (3): `kaisyn:village/exclusives/mediterranean/houses/regular/med_large_house_{1-3}`
- Mushroom Fields Fantasy (2): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_large_house_{1-2}`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_large_house_1`
- Plains (1): `minecraft:village/plains/houses/plains_big_house_1`
- Plains zombie (1): `minecraft:village/plains/zombie/houses/plains_big_house_1`
- Rustic (4): `kaisyn:village/exclusives/rustic/houses/rustic_large_house_{1-4}`
- Sparse Jungle Polynesian (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_large_house_1`
- Swamp Boat (2): `kaisyn:village/swamp_boat/houses/swamp_b_large_house_{1-2}`
- Terralith Desert (1): `terralith:village/desert/houses/generic_big1`
- Terralith Fortified (6): `terralith:village/fortified/houses/big1`, `terralith:village/fortified/houses/generic_big{1-4}`, `terralith:village/fortified/houses/large_house_rework`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_large_house_1`

**Librarian** (21)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/houses/badlands_library_1`
- Classic (1): `kaisyn:village/exclusives/classic/houses/classic_library_1`
- Desert (1): `minecraft:village/desert/houses/desert_library_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_library_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_library_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_librarian_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_library_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_library_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_library_1`
- Plains (2): `minecraft:village/plains/houses/plains_library_{1-2}`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_librarian_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_library_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_library_1`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_library_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_library_and_temple_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_library_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_library_1`
- Terralith Desert (1): `terralith:village/desert/houses/library1`
- Terralith Fortified (1): `terralith:village/fortified/houses/library`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_library_1`

**Medium homes** (61)

- Badlands Pueblo (4): `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_1_l`, `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_1_r`, `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_2_l`, `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_2_r`
- Birch Forest Romanian (2): `kaisyn:village/birch_forest_romanian/houses/birch_forest_medium_house_{1-2}`
- Classic (2): `kaisyn:village/exclusives/classic/houses/classic_medium_house_{1-2}`
- Desert (2): `minecraft:village/desert/houses/desert_medium_house_{1-2}`
- Desert zombie (2): `minecraft:village/desert/zombie/houses/desert_medium_house_{1-2}`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_medium_house_1`
- Forest Ruins (2): `kaisyn:village/forest_ruins/houses/forest_medium_house_{1-2}`
- Iberian (8): `kaisyn:village/exclusives/iberian/houses/iberian_medium_house_{1-8}`
- Meadow Swiss (5): `kaisyn:village/meadow_swiss/houses/meadow_medium_house_{1-5}`
- Mediterranean (2): `kaisyn:village/exclusives/mediterranean/houses/regular/med_medium_house_{1-2}`
- Old Growth Taiga Polish (2): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_medium_house_{1-2}`
- Plains (2): `minecraft:village/plains/houses/plains_medium_house_{1-2}`
- Plains zombie (2): `minecraft:village/plains/zombie/houses/plains_medium_house_{1-2}`
- Savanna (2): `minecraft:village/savanna/houses/savanna_medium_house_{1-2}`
- Savanna zombie (2): `minecraft:village/savanna/zombie/houses/savanna_medium_house_{1-2}`
- Snowy (3): `minecraft:village/snowy/houses/snowy_medium_house_{1-3}`
- Snowy zombie (3): `minecraft:village/snowy/zombie/houses/snowy_medium_house_{1-3}`
- Sparse Jungle Polynesian (3): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_medium_house_{1-3}`
- Swamp Boat (4): `kaisyn:village/swamp_boat/houses/swamp_b_medium_house_{1-4}`
- Taiga (4): `minecraft:village/taiga/houses/taiga_medium_house_{1-4}`
- Taiga zombie (4): `minecraft:village/taiga/zombie/houses/taiga_medium_house_{1-4}`

**Small homes** (171)

- Badlands Pueblo (12): `kaisyn:village/badlands_pueblo/houses/badlands_small_house_1_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_1_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_2_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_2_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_3_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_3_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_4_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_4_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_5_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_5_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_6_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_6_r`
- Birch Forest Romanian (6): `kaisyn:village/birch_forest_romanian/houses/birch_forest_small_house_{1-6}`
- Classic (2): `kaisyn:village/exclusives/classic/houses/classic_small_house_{1-2}`
- Desert (8): `minecraft:village/desert/houses/desert_small_house_{1-8}`
- Desert zombie (8): `minecraft:village/desert/zombie/houses/desert_small_house_{1-8}`
- Flower Forest Japanese (6): `kaisyn:village/flower_forest_japanese/houses/flower_forest_small_house_{1-6}`
- Forest Ruins (8): `kaisyn:village/forest_ruins/houses/forest_small_house_{1-8}`
- Iberian (3): `kaisyn:village/exclusives/iberian/houses/iberian_small_house_{1-3}`
- Jungle Tribal (4): `kaisyn:village/jungle_tribal/houses/jungle_small_house_{1-4}`
- Meadow Swiss (2): `kaisyn:village/meadow_swiss/houses/meadow_small_house_{1-2}`
- Mediterranean (7): `kaisyn:village/exclusives/mediterranean/houses/corner/med_small_house_{1-2}`, `kaisyn:village/exclusives/mediterranean/houses/regular/med_small_house_{1-5}`
- Mushroom Fields Fantasy (2): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_small_house_{1-2}`
- Nilotic (2): `kaisyn:village/exclusives/nilotic/houses/nilotic_small_house_{1-2}`
- Old Growth Taiga Polish (6): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_small_house_{1-6}`
- Plains (8): `minecraft:village/plains/houses/plains_small_house_{1-8}`
- Plains zombie (8): `minecraft:village/plains/zombie/houses/plains_small_house_{1-8}`
- Rustic (4): `kaisyn:village/exclusives/rustic/houses/rustic_small_house_{1-4}`
- Savanna (8): `minecraft:village/savanna/houses/savanna_small_house_{1-8}`
- Savanna zombie (8): `minecraft:village/savanna/zombie/houses/savanna_small_house_{1-8}`
- Snowy (8): `minecraft:village/snowy/houses/snowy_small_house_{1-8}`
- Snowy Taiga Viking (5): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_small_house_{1-5}`
- Snowy zombie (8): `minecraft:village/snowy/zombie/houses/snowy_small_house_{1-8}`
- Sparse Jungle Polynesian (4): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_small_house_{1-4}`
- Swamp Boat (3): `kaisyn:village/swamp_boat/houses/swamp_b_small_house_{1-3}`
- Swedish (5): `kaisyn:village/exclusives/swedish/houses/swedish_small_house_{1-5}`
- Taiga (5): `minecraft:village/taiga/houses/taiga_small_house_{1-5}`
- Taiga zombie (5): `minecraft:village/taiga/zombie/houses/taiga_small_house_{1-5}`
- Tudor (4): `kaisyn:village/exclusives/tudor/houses/tudor_small_house_{1-4}`
- Wooded Badlands Tipi (12): `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_black_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_blue_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_brown_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_green_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_red_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_yellow_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_black_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_blue_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_brown_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_green_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_red_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_yellow_1`

**Butcher** (28)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_butcher_1`
- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_butcher_and_leatherworker_1`
- Classic (1): `kaisyn:village/exclusives/classic/houses/classic_butcher_1`
- Desert (1): `minecraft:village/desert/houses/desert_butcher_shop_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_butcher_1`
- Forest Ruins (2): `kaisyn:village/forest_ruins/houses/forest_butcher_{1-2}`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_butcher_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_butcher_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_butcher_and_mason_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_butcher_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_butcher_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_butcher_and_fisher_1`
- Plains (2): `minecraft:village/plains/houses/plains_butcher_shop_{1-2}`
- Plains zombie (1): `minecraft:village/plains/zombie/houses/plains_butcher_shop_2`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_butcher_1`
- Savanna (2): `minecraft:village/savanna/houses/savanna_butchers_shop_{1-2}`
- Snowy (2): `minecraft:village/snowy/houses/snowy_butchers_shop_{1-2}`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_butcher_1`
- Sparse Jungle Polynesian (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_butcher_and_shepherd_1`
- Swamp Boat (1): `kaisyn:village/swamp_boat/houses/swamp_b_butcher_and_mason_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_butcher_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_butcher_shop_1`
- Terralith Desert (1): `terralith:village/desert/houses/butcher1`
- Terralith Fortified (1): `terralith:village/fortified/houses/butcher`

**Farmer** (9)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_farmer_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_farmer_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_farmer_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_farmer_and_mason_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_farmer_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_farmer_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_farmer_and_fisher_1`
- Terralith Fortified (1): `terralith:village/fortified/houses/farmer`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_farmer_1`

**Fisher** (24)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_fisher_1`
- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_fisher_1`
- Desert (1): `minecraft:village/desert/houses/desert_fisher_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_fisher_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_fisher_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_fisher_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_fisher_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_fisher_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_fisher_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/light_professions/mushroom_fisher_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_fisher_1`
- Plains (1): `minecraft:village/plains/houses/plains_fisher_cottage_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_fisher_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_fisher_cottage_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_fisher_cottage`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_fisher_1`
- Sparse Jungle Polynesian (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_fisher_and_fletcher_1`
- Swamp Boat (1): `kaisyn:village/swamp_boat/houses/swamp_b_fisher_and_leatherworker_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_fisher_cottage_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_fisher_cottage_1`
- Terralith Desert (2): `terralith:village/desert/houses/fisherman{1-2}`
- Terralith Fortified (1): `terralith:village/fortified/houses/fisherman`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_fisher_1`

**Fletcher** (22)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_fletcher_1`
- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_fletcher_1`
- Desert (1): `minecraft:village/desert/houses/desert_fletcher_house_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_fletcher_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_fletcher_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_fletcher_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_fletcher_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_fletcher_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_fletcher_and_toolsmith_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/light_professions/mushroom_fletcher_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_fletcher_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_fletcher_1`
- Plains (1): `minecraft:village/plains/houses/plains_fletcher_house_1`
- Plains zombie (1): `minecraft:village/plains/zombie/houses/plains_fletcher_house_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_fletcher_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_fletcher_house_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_fletcher_house_1`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_fletcher_1`
- Swamp Boat (1): `kaisyn:village/swamp_boat/houses/swamp_b_fletcher_and_library_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_fletcher_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_fletcher_house_1`
- Terralith Desert (1): `terralith:village/desert/houses/fletcher1`

**Leatherworker** (20)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_leatherworker_1`
- Desert (1): `minecraft:village/desert/houses/desert_tannery_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_leatherworker_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_leatherworker_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_leatherworker_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_leatherworker_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_leatherworker_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_leatherworker_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_leatherworker_and_shepherd_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_leatherworker_1`
- Plains (1): `minecraft:village/plains/houses/plains_tannery_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_leatherworker_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_tannery_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_tannery_1`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_leatherworker_1`
- Sparse Jungle Polynesian (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_leatherworker_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_leatherworker_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_tannery_1`
- Terralith Desert (1): `terralith:village/desert/houses/tanner1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_leatherworker_1`

**Shepherd** (23)

- Badlands Pueblo (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_shepherd_1`
- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_shepherd_1`
- Desert (1): `minecraft:village/desert/houses/desert_shepherd_house_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_shepherd_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_shepherd_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_shepherd_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_shepherd_1`
- Meadow Swiss (1): `kaisyn:village/meadow_swiss/houses/meadow_shepherd_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_shepherd_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/light_professions/mushroom_shepherd_1`
- Old Growth Taiga Polish (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_shepherd_1`
- Plains (1): `minecraft:village/plains/houses/plains_shepherds_house_1`
- Plains zombie (1): `minecraft:village/plains/zombie/houses/plains_shepherds_house_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_shepherd_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_shepherd_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_shepherds_house_1`
- Snowy Taiga Viking (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_shepherd_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_shepherd_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_shepherds_house_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_shepherds_house_1`
- Terralith Desert (1): `terralith:village/desert/houses/shepherd1`
- Terralith Fortified (1): `terralith:village/fortified/houses/shepherd`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_shepherd_1`

**Weaponsmith** (18)

- Birch Forest Romanian (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_weaponsmith_1`
- Classic (1): `kaisyn:village/exclusives/classic/houses/classic_blacksmith_1`
- Desert (1): `minecraft:village/desert/houses/desert_weaponsmith_1`
- Flower Forest Japanese (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_weaponsmith_1`
- Forest Ruins (1): `kaisyn:village/forest_ruins/houses/forest_weaponsmith_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_weaponsmith_1`
- Jungle Tribal (1): `kaisyn:village/jungle_tribal/houses/jungle_weaponsmith_1`
- Mushroom Fields Fantasy (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/heavy_professions/mushroom_weaponsmith_1`
- Plains (1): `minecraft:village/plains/houses/plains_weaponsmith_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_weaponsmith_1`
- Savanna (2): `minecraft:village/savanna/houses/savanna_weaponsmith_{1-2}`
- Snowy (1): `minecraft:village/snowy/houses/snowy_weapon_smith_1`
- Swamp Boat (1): `kaisyn:village/swamp_boat/houses/swamp_b_weaponsmith_1`
- Taiga (2): `minecraft:village/taiga/houses/taiga_weaponsmith_{1-2}`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_weaponsmith_2`
- Terralith Desert (1): `terralith:village/desert/houses/weaponsmith1`

## Pillager (54)

### Witches and mansions (4)

- Dark Forest (1): `minecraft:mansion`
- Swamp (3): `betterwitchhuts:witch_hut`, `minecraft:swamp_hut`, `villagesandpillages:village_witch`

### Ships (4)

- Ocean (1): `towns_and_towers:pillager_outpost_ocean`
- Any (3): `blocky_siege:pillager_ship`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`

### Camps (6)

- Badlands (1): `towns_and_towers:pillager_outpost_wooded_badlands`
- Beach (1): `towns_and_towers:pillager_outpost_beach`
- Jungle (1): `towns_and_towers:pillager_outpost_sparse_jungle`
- Plains (2): `eugene:fake_trader_camp`, `eugene:pillager_camp`
- Snowy (1): `towns_and_towers:pillager_outpost_snowy_beach`

### Siege and wagons (8)

- Dark Forest (1): `eugene:witch_wagon`
- Plains (7): `eugene:pillager_loot_carriage`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`

### Towers (23)

- Acacia (1): `mysticrift_pillagers_towers_aris:acacia_pillager_tower`
- Birch (1): `mysticrift_pillagers_towers_aris:birch_pillager_tower`
- Cherry (1): `mysticrift_pillagers_towers_aris:cherry_pillager_tower`
- Dark Forest (1): `minecraft:pillager_outpost`
- Desert (1): `mysticrift_pillagers_towers_aris:desert_pillager_tower`
- Forest (3): `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`
- Jungle (1): `mysticrift_pillagers_towers_aris:jungle_pillager_tower`
- Mangrove (1): `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`
- Meadow (1): `towns_and_towers:pillager_outpost_meadow`
- Mushroom (1): `towns_and_towers:pillager_outpost_mushroom_fields`
- Oak (1): `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`
- Oriental (1): `towns_and_towers:exclusives/pillager_outpost_oriental`
- Plains (1): `towns_and_towers:pillager_outpost_sunflower_plains`
- Savanna (1): `towns_and_towers:pillager_outpost_savanna`
- Snowy (3): `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`
- Swamp (1): `towns_and_towers:pillager_outpost_swamp`
- Swedish (1): `towns_and_towers:exclusives/pillager_outpost_swedish`
- Taiga (1): `towns_and_towers:pillager_outpost_taiga`
- Tudor (1): `towns_and_towers:exclusives/pillager_outpost_tudor`

### Outposts (9)

- Badlands (1): `towns_and_towers:pillager_outpost_badlands`
- Classic (1): `towns_and_towers:exclusives/pillager_outpost_classic`
- Desert (1): `towns_and_towers:pillager_outpost_desert`
- Grove (1): `towns_and_towers:pillager_outpost_grove`
- Iberian (1): `towns_and_towers:exclusives/pillager_outpost_iberian`
- Jungle (1): `towns_and_towers:pillager_outpost_jungle`
- Mediterranean (1): `towns_and_towers:exclusives/pillager_outpost_mediterranean`
- Rustic (1): `towns_and_towers:exclusives/pillager_outpost_rustic`
- Taiga (1): `towns_and_towers:pillager_outpost_old_growth_taiga`

## Ruins (28)

### Surface ruins (21)

- Badlands (1): `philipsruins:badlands_structures`
- Desert (1): `philipsruins:desert_structures`
- Icy (1): `terralith:glacial_hut`
- Plains (17): `eugene:ruined_drum_tower`, `forest_ruins:ruin_{1-7}`, `forgotten_church:forgotten_church`, `philipsruins:ancient_ruins`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`
- Spruce (1): `watchtower_building:abandoned_watchtower`

### Portals (6)

- Desert (1): `minecraft:ruined_portal_desert`
- Jungle (1): `minecraft:ruined_portal_jungle`
- Ocean (1): `minecraft:ruined_portal_ocean`
- Swamp (1): `minecraft:ruined_portal_swamp`
- Any (2): `minecraft:ruined_portal`, `minecraft:ruined_portal_mountain`

### Towers (1)

- Plains (1): `philipsruins:ancient_towers`

## Underground (20)

### Other (8)

- Dark Forest (1): `farmers_structures:squids`
- Swamp (1): `farmers_structures:ratatouilles`
- Any (6): `minecraft:buried_treasure`, `philipsruins:underground_structures`, `terralith:underground/giant_bee_hive`, `terralith:underground/old_refinery`, `terralith:underground/sunken_tower`, `terralith:underground/witch_hut`

### Mineshafts (3)

- Badlands (1): `minecraft:mineshaft_mesa`
- Any (2): `minecraft:mineshaft`, `terralith:underground/mining_outpost`

### Cabins (9)

- Badlands (1): `subterrestrial:mesa_cabin`
- Deepslate (1): `subterrestrial:deepslate_cabin`
- Default (1): `subterrestrial:default_cabin`
- Desert (1): `subterrestrial:desert_cabin`
- Ice (1): `subterrestrial:ice_cabin`
- Jungle (1): `subterrestrial:jungle_cabin`
- Oak (1): `terralith:underground/oak_cabin`
- Ocean (1): `subterrestrial:ocean_cabin`
- Taiga (1): `subterrestrial:taiga_cabin`

## Underwater (11)

### Fossils (1)

- Any (1): `hopo:underwater_fossils`

### Ruins (7)

- Ocean (4): `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`
- Warm (1): `hopo:underwater_warm_small_ruins`
- Any (2): `hopo:underwater_ruins`, `hopo:underwater_small_ruins`

### Shipwrecks (3)

- Ocean (1): `towns_and_towers:wreckage_ocean`
- Any (2): `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`

## Sky (39)

### Balloons and temples (3)

- Any (3): `cloudstorage:big_balloon`, `cloudstorage:sky_temple`, `creativeengineering:sky_island`

### Sky village: Villager Houses (13)

**Cleric** (1)

- Sky (1): `skyvillages:alchemist_1`

**Cartographer** (1)

- Sky (1): `skyvillages:cartographer_1`

**Farmer** (3)

- Sky (3): `skyvillages:farmersdelight/sky_village_farmersdelight_farm_{0-2}`

**Fletcher** (1)

- Sky (1): `skyvillages:fletcher_1`

**Other homes** (1)

- Sky (1): `skyvillages:house_1`

**Librarian** (2)

- Sky (2): `skyvillages:librarian_{1-2}`

**Weaver** (1)

- Sky (1): `skyvillages:loom_1`

**Mason** (1)

- Sky (1): `skyvillages:mason_1`

**Weaponsmith** (2)

- Sky (2): `skyvillages:weaponsmith_{1-2}`

### Sky village: Decor and Infrastructure (14)

**Streets** (5)

- Sky (5): `skyvillages:bridge_{1-5}`

**Decor** (9)

- Sky (9): `skyvillages:letsdo_brewery/sky_village_letsdo_brewery_deco_{0-8}`

### Sky village: Villager Structures (9)

**Farms** (6)

- Sky (6): `skyvillages:farm_{1-3}`, `skyvillages:letsdo_vinery/sky_village_letsdo_vinery_{0-2}`

**Town centres** (1)

- Sky (1): `skyvillages:fountain`

**Workplaces** (2)

- Sky (2): `skyvillages:letsdo_brewery/sky_village_letsdo_brewery_{0-1}`

## Buildings (79)

### Houses and workplaces (59)

**Churches and temples** (4)

- Desert (1): `desert_village_temple:village_temple`
- Forest (1): `simply_houses:forest_chapel`
- Jungle (1): `ancient_enchantment_temple:temple`
- Plains (1): `gothic_church:church`

**Farms and stables** (9)

- Highlands (1): `llama_stable:llama_stable`
- Jungle (1): `farmers_structures:cacaos`
- Plains (7): `animal_barn:barn`, `bighorsestable:big_horse_stable`, `farmers_structures:shepherd_farms`, `hearthandharvest:corn_maze`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `villager_windmill:windmill`

**Homes** (22)

- Badlands (2): `saloon:saloon`, `villager_gold_washing_facility:villager_gold_washing_facility`
- Desert (1): `beautify:botanist_house_desert`
- Flower (1): `villager_flower_house:flower_house`
- Forest (5): `hobbit_house_:hobbit_house`, `sawmillhouse:sawmill`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`
- Plains (3): `beautify:botanist_house_plains`, `simply_houses:plains_house`, `simply_houses:plains_mansion`
- Savanna (3): `beautify:botanist_house_savanna`, `greek_villager_house:greek_house`, `villager_country_house:country_house`
- Snowy (2): `beautify:botanist_house_snowy`, `minecraft:igloo`
- Taiga (5): `beautify:botanist_house_taiga`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`

**Watchtowers** (3)

- Forest (1): `forest_watchtower:forest_watchtower`
- Oak (1): `mini_clock_tower:mini_clock_tower`
- Sand (1): `desert_watchtower:watchtower`

**Food** (9)

- Beach (1): `farmers_structures:aquacultures_beach`
- Plains (3): `farmers_structures:aquacultures_cod`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`
- Any (5): `farmers_structures:aethers`, `farmers_structures:aquacultures_carp`, `farmers_structures:cooks_moss`, `farmers_structures:sushis`, `farmers_structures:undergardens_sea`

**Fisher** (4)

- Beach (1): `villager_fish_shop:fish_shop`
- Plains (1): `villager_fishermans_hut:villager_fishermans_hut`
- Snowy (1): `farmers_structures:fishs_stew`
- Swamp (1): `old_swamp_fisherman_house:fisherman_house`

**Weaponsmith** (2)

- Plains (1): `villager_forge:villager_forge`
- Taiga (1): `simply_houses:taiga_forge`

**Butcher** (1)

- Plains (1): `villager_butcher_house:butcher_house`

**Cartographer** (1)

- Plains (1): `villager_cartographer_house:villager_cartographer_house`

**Cleric** (1)

- Plains (1): `villager_cleric_house:villager_cleric_house`

**Farmer** (1)

- Plains (1): `villager_farmer_house:villager_farmer_house`

**Fletcher** (1)

- Plains (1): `villager_fletcher_house:villager_fletcher_house`

**Leatherworker** (1)

- Plains (1): `villager_tannery:tannery`

### Ships (6)

- Any (6): `antiquetradingship:antique_trading_ship`, `unwrecked_ships:nether_ship`, `unwrecked_ships:ship`, `villager_rafts:raft_{1-3}`

### Camps (11)

- Beach (1): `campsite_structures:fisher_campsite`
- Plains (9): `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `eugene:camp`, `plains_villager_tents:big_villager_tent`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`
- Snowy (1): `snowy_tents:snowy_tent`

### Towers and castles (3)

- Icy (1): `terralith:spire`
- Plains (1): `eugene:red_roof_castle`
- Spruce (1): `watchtower_building:fresh_watchtower`

## Nature and Decor (7)

### Environmental (1)

- Any (1): `ae2:meteorite`

### Statues and markers (6)

- Plains (5): `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `small_cat_statues:cat_{1-3}`
- Any (1): `supplementaries:way_sign`

## Nether (20)

### Constructed (18)

- Any (11): `betterdungeons:small_nether_dungeon`, `betterfortresses:fortress`, `minecraft:bastion_remnant`, `minecraft:fortress`, `philipsruins:lost_soul_city`, `philipsruins:lost_soul_dungeon`, `philipsruins:nether_lava_ruins`, `philipsruins:rare_ruin`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_dungeon`, `philipsruins:start_nether_ruin`

**Camps** (1)

- Any (1): `eugene:piglin_camp`

**Houses** (2)

- Any (2): `farmers_structures:cooks_nether_{1-2}`

**Towers** (2)

- Crimson (1): `hearths:crimson_tower`
- Warped (1): `hearths:warped_tower`

**Portals** (1)

- Any (1): `minecraft:ruined_portal_nether`

**Decoration** (1)

- Any (1): `twigs:bloodstone_obelisk`

### Environmental (2)

**Decoration** (2)

- Any (2): `hearths:netherrack_spiral`, `minecraft:nether_fossil`

## End (22)

### Constructed (17)

- End (1): `endersdelight:end_house`
- Any (11): `mes:astral_hideaway`, `mes:enderkeep_courtyard`, `mes:enderwatch_tower`, `mes:manuscript_shrine`, `mes:monolith`, `mes:mythic_garden`, `mes:phantom_citadel`, `mes:starlight_voyager`, `minecraft:end_city`, `philipsruins:end_gate_fortress`, `philipsruins:end_ruins`

**Decoration** (4)

- Any (4): `mes:ender_spire`, `mes:enderpin_spikes`, `mes:endscraps`, `mes:ruined_pillar`

**Towers** (1)

- End (1): `mysticrift_pillagers_towers_aris:end_pillager_tower`

### Environmental (5)

- Grove (1): `mes:enderbloom_grove`
- Any (4): `mes:astral_meteorite`, `mes:enderskog`, `mes:placid_prairie`, `nullscape:dragon_skeleton`

## Composite (34)

### Sky (4)

- Any (4): `creativeengineering:meta/balloon_fleet`, `creativeengineering:meta/cloud_balloons`, `creativeengineering:meta/cloud_cluster`, `creativeengineering:meta/sky_village_islands`

### Nature (7)

- Acacia (1): `creativeengineering:meta/deadwood_acacia`
- Birch (1): `creativeengineering:meta/deadwood_birch`
- Cherry (1): `creativeengineering:meta/deadwood_cherry`
- Jungle (1): `creativeengineering:meta/deadwood_jungle`
- Oak (1): `creativeengineering:meta/deadwood_oak`
- Spruce (1): `creativeengineering:meta/deadwood_spruce`
- Any (1): `creativeengineering:meta/driftwood_flotilla`

### Camps and houses (4)

- Any (4): `creativeengineering:meta/farmers_camp`, `creativeengineering:meta/house_camp`, `creativeengineering:meta/waysign_camps`, `creativeengineering:meta/waysign_house_camp`

### Villages (4)

- Any (4): `creativeengineering:meta/fortified_camps`, `creativeengineering:meta/fortified_ruins`, `creativeengineering:meta/town_villages`, `creativeengineering:meta/village_towers`

### Pillager (6)

- Any (6): `creativeengineering:meta/pillager_camp_ruin`, `creativeengineering:meta/pillager_camp_ruins`, `creativeengineering:meta/pillager_camp_siege`, `creativeengineering:meta/pillager_great_tower_siege`, `creativeengineering:meta/pillager_stronghold_siege`, `creativeengineering:meta/pillager_tower_siege`

### Ships (2)

- Any (2): `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/raft_convoy`

### Ruins (3)

- Any (3): `creativeengineering:meta/ruin_cluster`, `creativeengineering:meta/ruin_scatter`, `creativeengineering:meta/waystone_ruins`

### Underwater (4)

- Any (4): `creativeengineering:meta/shipwreck_debris`, `creativeengineering:meta/shipwreck_graveyard`, `creativeengineering:meta/sunken_ruin_cluster`, `creativeengineering:meta/underwater_meteorite`

# Unmapped (needs a `type/*` tag in structure_tags.json)

Columns along -X by mod, each mod running along +Z.

- betterdeserttemples (1): `betterdeserttemples:desert_temple`
- betterdungeons (4): `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`
- betterjungletemples (1): `betterjungletemples:jungle_temple`
- betterstrongholds (1): `betterstrongholds:stronghold`
- betterwitchhuts (1): `betterwitchhuts:witch_circle`
- creativeengineering (14): `creativeengineering:meta/desert_temple_ruins`, `creativeengineering:meta/jungle_temple_ruins`, `creativeengineering:meta/meteor_shower`, `creativeengineering:meta/meteorite_desert_pyramid`, `creativeengineering:meta/meteorite_village`, `creativeengineering:meta/pillager_camp_meteorite`, `creativeengineering:meta/pillager_caravan`, `creativeengineering:meta/struck_monument`, `creativeengineering:meta/trader_caravan`, `creativeengineering:meta/village_siege_camp`, `creativeengineering:meta/village_siege_ring`, `creativeengineering:meta/witch_cart_caravan`, `creativeengineering:meta/witch_coven`, `creativeengineering:waystone_ruin_part`
- eugene (2): `eugene:castle`, `eugene:undead_castle`
- minecraft (7): `minecraft:ancient_city`, `minecraft:desert_pyramid`, `minecraft:jungle_pyramid`, `minecraft:monument`, `minecraft:shipwreck`, `minecraft:shipwreck_beached`, `minecraft:stronghold`
- nullscape (1): `nullscape:phantom_nest`
- philipsruins (6): `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:badlands_start_dungeon`, `philipsruins:bone_dungeon`, `philipsruins:sculk_dungeon`
- takesapillage (2): `takesapillage:bastille`, `takesapillage:pillager_camp`
- terralith (2): `terralith:underground/frosted_dungeon`, `terralith:underground_cabin`
- towns_and_towers (1): `towns_and_towers:mimic_desert`
- underground_bunkers (1): `underground_bunkers:underground_bunker`

# Dried out

Whole structures that get the water removal pass (rule in the plan, section 4).

`antiquetradingship:antique_trading_ship`, `blocky_siege:pillager_ship`, `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/raft_convoy`, `creativeengineering:meta/shipwreck_debris`, `creativeengineering:meta/shipwreck_graveyard`, `creativeengineering:meta/struck_monument`, `creativeengineering:meta/sunken_ruin_cluster`, `creativeengineering:meta/underwater_meteorite`, `hopo:underwater_fossils`, `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `minecraft:monument`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `minecraft:ruined_portal_ocean`, `minecraft:shipwreck`, `minecraft:shipwreck_beached`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`, `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`, `subterrestrial:ocean_cabin`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:wreckage_ocean`, `unwrecked_ships:nether_ship`, `unwrecked_ships:ship`, `villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`

# Skipped entity pieces

`kaisyn:village/beach_lighthouse/villagers/lighthouse_master`, `kaisyn:village/exclusives/mediterranean/villagers/bishop`, `kaisyn:village/exclusives/wandering_trader_camp/villagers/trader_{0-9}`, `kaisyn:village/jungle_tribal/villagers/baby`, `kaisyn:village/jungle_tribal/villagers/nitwit`, `kaisyn:village/jungle_tribal/villagers/unemployed`, `kaisyn:village/sparse_jungle_polynesian/villagers/village_chief`, `kaisyn:village/swamp_boat/villagers/baby`, `kaisyn:village/swamp_boat/villagers/nitwit`, `kaisyn:village/swamp_boat/villagers/unemployed`, `minecraft:village/common/iron_golem`, `minecraft:village/desert/villagers/baby`, `minecraft:village/desert/villagers/nitwit`, `minecraft:village/desert/villagers/unemployed`, `minecraft:village/desert/zombie/villagers/nitwit`, `minecraft:village/desert/zombie/villagers/unemployed`, `minecraft:village/plains/villagers/baby`, `minecraft:village/plains/villagers/nitwit`, `minecraft:village/plains/villagers/unemployed`, `minecraft:village/plains/zombie/villagers/nitwit`, `minecraft:village/plains/zombie/villagers/unemployed`, `minecraft:village/savanna/villagers/baby`, `minecraft:village/savanna/villagers/nitwit`, `minecraft:village/savanna/villagers/unemployed`, `minecraft:village/savanna/zombie/villagers/nitwit`, `minecraft:village/savanna/zombie/villagers/unemployed`, `minecraft:village/snowy/villagers/baby`, `minecraft:village/snowy/villagers/nitwit`, `minecraft:village/snowy/villagers/unemployed`, `minecraft:village/snowy/zombie/villagers/nitwit`, `minecraft:village/snowy/zombie/villagers/unemployed`, `minecraft:village/taiga/villagers/baby`, `minecraft:village/taiga/villagers/nitwit`, `minecraft:village/taiga/villagers/unemployed`, `minecraft:village/taiga/zombie/villagers/nitwit`, `minecraft:village/taiga/zombie/villagers/unemployed`, `skyvillages:villager`
