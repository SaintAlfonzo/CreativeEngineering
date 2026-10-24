# Structure tuning report

Written 2026-10-24 08:24:18 UTC at world load by `kubejs/server_scripts/worldgen/structure_overrides.js`. Do not edit, it is rewritten every load. Edit `structure_groups.json` (rates) or the `group/*` tags in `structure_tags.json` (membership), then rejoin the world. Expected rates per biome: `structure_group_estimates.md` (`python3 tools/gen_structure_groups.py --check`).

## Biome rules

Only the allowed structures may start in a rule's biomes; every other structure loses those biomes from its own list (patcherMod StructureBiomeRules, CEConfig [worldgen.systems] structureBiomeRules). The game log line `[structure-biome-rules]` lists the structures each rule kept out.

### `water`

Oceans and rivers hold water structures only. Mods give some structures every overworld biome (Mysticrift pillager towers: #minecraft:is_overworld), which put them in the open sea.

Biomes: `#minecraft:is_ocean`, `#minecraft:is_river`, `#c:river`. Allowed (`#water_ok`, 67): `ae2:meteorite`, `antiquetradingship:antique_trading_ship`, `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterstrongholds:stronghold`, `blocky_siege:pillager_ship`, `cloudstorage:big_balloon`, `cloudstorage:sky_temple`, `creativeengineering:meta/driftwood_flotilla`, `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/raft_convoy`, `creativeengineering:meta/shipwreck_debris`, `creativeengineering:meta/shipwreck_graveyard`, `creativeengineering:meta/sunken_ruin_cluster`, `farmers_structures:sushis`, `hopo:underwater_fossils`, `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `mes:starlight_voyager`, `minecraft:ancient_city`, `minecraft:buried_treasure`, `minecraft:mineshaft`, `minecraft:mineshaft_mesa`, `minecraft:monument`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `minecraft:ruined_portal_ocean`, `minecraft:stronghold`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_city`, `philipsruins:lost_soul_dungeon`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`, `philipsruins:sculk_dungeon`, `philipsruins:underground_structures`, `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`, `skyvillages:skyvillage`, `subterrestrial:deepslate_cabin`, `subterrestrial:default_cabin`, `subterrestrial:desert_cabin`, `subterrestrial:ice_cabin`, `subterrestrial:jungle_cabin`, `subterrestrial:mesa_cabin`, `subterrestrial:ocean_cabin`, `subterrestrial:taiga_cabin`, `terralith:underground/frosted_dungeon`, `terralith:underground/giant_bee_hive`, `terralith:underground/mining_outpost`, `terralith:underground/oak_cabin`, `terralith:underground/old_refinery`, `terralith:underground/sunken_tower`, `terralith:underground/witch_hut`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:village_ocean`, `towns_and_towers:wreckage_ocean`, `unwrecked_ships:nether_ship`, `unwrecked_ships:ship`, `villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`
Surface check (`#surface_check`): `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon` may not start where the surface above them is in these biomes or under water.

## Biome fits

A structure in a fit's structure tag spawns in that fit's biomes plus its parents' instead of its own list (`biome_fits.json`, patcherMod StructureBiomeFits, CEConfig [worldgen.systems] structureBiomeFits). Each fit is also the real biome tag and structure tag `creativeengineering:<fit>`. The game log line `[structure-biome-fits]` gives each structure's old and new biome count.

| Fit | Spawns in | Biomes listed | Structures |
|---|---|---|---|
| `land` | `land` | 0 | none |
| `land/plains` | `land/plains` + `land` | 8 | `animal_barn:barn`, `bighorsestable:big_horse_stable`, `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:pillager_loot_carriage`, `eugene:red_roof_castle`, `eugene:ruined_drum_tower`, `eugene:undead_castle`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `gothic_church:church`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:village_plains`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `plains_villager_tents:big_villager_tent`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `terralith:fortified_village`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:village_meadow`, `towns_and_towers:village_sunflower_plains`, `villager_archer_camp:villager_archer_camp`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_circus_tent:villager_circus_tent`, `villager_cleric_house:villager_cleric_house`, `villager_farmer_house:villager_farmer_house`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_forge:villager_forge`, `villager_tannery:tannery`, `villager_windmill:windmill` |
| `land/highlands` | `land/highlands` + `land` | 6 | `animal_barn:barn`, `bighorsestable:big_horse_stable`, `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:ruined_drum_tower`, `eugene:undead_castle`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `gothic_church:church`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `llama_stable:llama_stable`, `minecraft:village_plains`, `minecraft:village_taiga`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `plains_villager_tents:big_villager_tent`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `terralith:fortified_village`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:village_old_growth_taiga`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_farmer_house:villager_farmer_house`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_forge:villager_forge`, `villager_tannery:tannery`, `villager_windmill:windmill` |
| `land/forest` | `land/forest` + `land` | 17 | `bighorsestable:big_horse_stable`, `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:red_roof_castle`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `gothic_church:church`, `hobbit_house_:hobbit_house`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:village_forest`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`, `villager_tannery:tannery` |
| `land/birch` | `land/birch` + `land` | 18 | `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:village_birch_forest` |
| `land/flower` | `land/flower` + `land` | 11 | `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `hobbit_house_:hobbit_house`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:village_flower_forest`, `towns_and_towers:village_meadow`, `towns_and_towers:village_sunflower_plains`, `villager_flower_house:flower_house` |
| `land/dark_forest` | `land/dark_forest` + `land` | 7 | `bighorsestable:big_horse_stable`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:witch_wagon`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forest_watchtower:forest_watchtower`, `forgotten_church:forgotten_church`, `gothic_church:church`, `hobbit_house_:hobbit_house`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:mansion`, `minecraft:pillager_outpost`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `villager_tannery:tannery` |
| `land/taiga` | `land/taiga` + `land` | 14 | `bighorsestable:big_horse_stable`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:undead_castle`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `gothic_church:church`, `hobbit_house_:hobbit_house`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:village_taiga`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `sawmillhouse:sawmill`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `small_viking_villager_house:small_viking_house`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `terralith:fortified_village`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:village_old_growth_taiga`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_farmer_house:villager_farmer_house`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_forge:villager_forge`, `villager_tannery:tannery`, `villager_windmill:windmill` |
| `land/snowy` | `land/snowy` + `land` | 18 | `eugene:pillager_loot_carriage`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:igloo`, `minecraft:village_snowy`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:pumpkin_ruins`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`, `snowy_tents:snowy_tent`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:village_grove`, `towns_and_towers:village_snowy_slopes`, `towns_and_towers:village_snowy_taiga` |
| `land/icy` | `land/icy` + `land` | 7 | `terralith:glacial_hut`, `terralith:spire` |
| `land/desert` | `land/desert` + `land` | 7 | `betterdeserttemples:desert_temple`, `desert_village_temple:village_temple`, `eugene:pillager_loot_carriage`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:desert_pyramid`, `minecraft:village_desert`, `philipsruins:desert_structures`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `terralith:fortified_desert_village`, `towns_and_towers:mimic_desert`, `towns_and_towers:pillager_outpost_desert` |
| `land/badlands` | `land/badlands` + `land` | 10 | `eugene:pillager_loot_carriage`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:mineshaft_mesa`, `philipsruins:badlands_start_dungeon`, `philipsruins:badlands_structures`, `saloon:saloon`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_wooded_badlands`, `towns_and_towers:village_badlands`, `towns_and_towers:village_wooded_badlands`, `villager_gold_washing_facility:villager_gold_washing_facility` |
| `land/savanna` | `land/savanna` + `land` | 11 | `eugene:pillager_loot_carriage`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `greek_villager_house:greek_house`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `llama_stable:llama_stable`, `minecraft:village_savanna`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins_rocks`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_savanna`, `villager_country_house:country_house` |
| `land/warm_dry` | `land/warm_dry` + `land` | 7 | `greek_villager_house:greek_house`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/village_mediterranean`, `villager_country_house:country_house` |
| `land/oasis` | `land/oasis` + `land` | 5 | `betterdeserttemples:desert_temple`, `desert_village_temple:village_temple`, `minecraft:desert_pyramid`, `minecraft:village_desert`, `philipsruins:desert_structures`, `terralith:fortified_desert_village`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `towns_and_towers:mimic_desert`, `towns_and_towers:pillager_outpost_desert` |
| `land/jungle` | `land/jungle` + `land` | 8 | `ancient_enchantment_temple:temple`, `betterjungletemples:jungle_temple`, `minecraft:jungle_pyramid`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:rare_ruin`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:village_jungle`, `towns_and_towers:village_sparse_jungle` |
| `land/swamp` | `land/swamp` + `land` | 4 | `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:witch_wagon`, `minecraft:swamp_hut`, `old_swamp_fisherman_house:fisherman_house`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `philipsruins:rare_ruin`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:village_swamp`, `villagesandpillages:village_witch` |
| `land/beach` | `land/beach` + `land` | 3 | `campsite_structures:fisher_campsite`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:village_beach`, `villager_fish_shop:fish_shop` |
| `land/peaks` | `land/peaks` + `land` | 11 | `philipsruins:ancient_ruins`, `philipsruins:ancient_towers` |
| `land/oak` | `land/oak` + `land` | 36 | `mini_clock_tower:mini_clock_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` |
| `land/spruce` | `land/spruce` + `land` | 44 | `forest_watchtower:forest_watchtower`, `watchtower_building:abandoned_watchtower`, `watchtower_building:fresh_watchtower` |
| `land/acacia` | `land/acacia` + `land` | 19 | `mysticrift_pillagers_towers_aris:acacia_pillager_tower` |
| `land/mangrove` | `land/mangrove` + `land` | 1 | `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` |
| `land/cherry` | `land/cherry` + `land` | 2 | `mysticrift_pillagers_towers_aris:cherry_pillager_tower` |
| `land/sand` | `land/sand` + `land` | 10 | `desert_watchtower:watchtower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower` |
| `land/end` | `land/end` | 2 | `mysticrift_pillagers_towers_aris:end_pillager_tower` |
| `land/mushroom` | `land/mushroom` + `land` | 2 | `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:village_mushroom_fields` |
| `land/any` | `land/any` + `land` | 113 | `betterdungeons:small_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon` |
| `land/any/deep` | `land/any/deep` + `land/any` + `land` | 14 | `betterdungeons:skeleton_dungeon`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_city` |
| `land/sky` | `land/sky` + `land` | 114 | `skyvillages:skyvillage` |
| `ocean/deep` | `ocean/deep` | 1 | `philipsruins:ocean_fortress` |

## Placement priority

A structure whose pieces would overlap a higher-ranked structure's never starts (patcherMod PlacementPriority, CEConfig [worldgen.systems] placementPriority). Same rank: no check (a group's grid keeps its own members apart).

| Group | Rank |
|---|---:|
| `settlements` | 95 |
| `pillager_settlements` | 90 |
| `desert_temples` | 82 |
| `surface_dungeons` | 80 |
| `witch_huts` | 79 |
| `entrance_dungeons` | 78 |
| `vehicles` | 76 |
| `remote` | 70 |
| `way_signs` | 69 |
| `ruins` | 60 |
| `nether_ruins` | 60 |
| `sky` | 50 |
| `ocean_dungeons` | 40 |
| `ocean_ruins` | 35 |
| `boats` | 30 |
| `flotsam` | 25 |

- `#priority/big_settlements`: rank 100
- shaped (jigsaw members grow around higher-ranked structures instead of being dropped): `settlements` (35 structures)
- companions of `creativeengineering:meta/town_villages`: 1 to 3 of 28 structures, 4 to 6 chunks out, rank 94

### Peaceful radius

No hostile structure starts with a piece within this many chunks (square, XZ) of a peaceful structure's pieces, whatever the ranks; peaceful structures ignore hostile ones in their own rank check.

| Peaceful | Radius (chunks) |
|---|---:|
| `#priority/big_settlements` | 8 |
| `settlements` | 5 |
| `remote` | 3 |

- hostile: `pillager_settlements`, `surface_dungeons`, `desert_temples`, `witch_huts`, `entrance_dungeons`, `vehicles` (70 structures)

## Groups

Members of a group share one grid: at most one structure per grid slot, and no two members start closer than separation + 1 chunks. "Alone: 1 per N" = one per N x N chunks where it is the only member that fits the biome (sharing a biome splits the slots by weight). 112 frequencies below 1 are rolled by patcherMod.

### `settlements` -> `creativeengineering:group/settlements`

Friendly settlements: vanilla and Towns & Towers villages, Terralith fortified villages. Meta structures join later.

Grid: spacing 48, separation 12, salt 871105700. Members never start closer than 13 chunks (208 blocks). 35 of 35 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `creativeengineering:meta/fortified_camps` | 1 | 1 | 48 | `(meta structure)` | meta structure: takes 20% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/fortified_ruins` | 1 | 1 | 48 | `(meta structure)` | meta structure: takes 20% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/town_villages` | 1 | 1 | 48 | `(meta structure)` | meta structure: takes 40% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/village_towers` | 1 | 1 | 48 | `(meta structure)` | meta structure: takes 33% of the slots its anchors win here; this weight is never used |
| `eugene:red_roof_castle` | 106 | 1 | 48 | `eugene:castle` |  |
| `minecraft:village_desert` | 424 | 0.734547 | 56 | `minecraft:villages` |  |
| `minecraft:village_plains` | 162 | 1 | 48 | `minecraft:villages` |  |
| `minecraft:village_savanna` | 103 | 0.99558 | 48 | `minecraft:villages` |  |
| `minecraft:village_snowy` | 132 | 1 | 48 | `minecraft:villages` |  |
| `minecraft:village_taiga` | 153 | 1 | 48 | `minecraft:villages` |  |
| `terralith:fortified_desert_village` | 108 | 1 | 48 | `terralith:rare_village` |  |
| `terralith:fortified_village` | 53 | 1 | 48 | `terralith:rare_village` |  |
| `towns_and_towers:exclusives/village_classic` | 101 | 0.1623 | 119 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_iberian` | 100 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_mediterranean` | 41 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_nilotic` | 100 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_rustic` | 100 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_swedish` | 22 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_tudor` | 99 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_badlands` | 173 | 0.430191 | 73 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_beach` | 101 | 0.171066 | 116 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_birch_forest` | 162 | 0.361997 | 80 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_flower_forest` | 360 | 0.41542 | 74 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_forest` | 144 | 0.457061 | 71 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_grove` | 213 | 0.499402 | 68 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_jungle` | 46 | 0.897 | 51 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_meadow` | 62 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_mushroom_fields` | 1 | 0.1623 | 119 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_old_growth_taiga` | 110 | 0.717581 | 57 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_snowy_slopes` | 141 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_snowy_taiga` | 108 | 0.590369 | 62 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_sparse_jungle` | 46 | 0.9 | 51 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_sunflower_plains` | 228 | 0.544825 | 65 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_swamp` | 142 | 0.582744 | 63 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_wooded_badlands` | 205 | 0.340365 | 82 | `towns_and_towers:towns` |  |

### `pillager_settlements` -> `creativeengineering:group/pillager_settlements`

Pillager outposts: vanilla, and every Towns & Towers tower, fort and camp outpost except the ship (boats).

Grid: spacing 32, separation 8, salt 1867953461, exclusion_zone creativeengineering:group/settlements r4. Members never start closer than 9 chunks (144 blocks). 47 of 47 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `creativeengineering:meta/pillager_camp_ruin` | 1 | 1 | 32 | `(meta structure)` | meta structure: takes 15% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/pillager_camp_ruins` | 1 | 1 | 32 | `(meta structure)` | meta structure: takes 15% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/pillager_camp_siege` | 1 | 1 | 32 | `(meta structure)` | meta structure: takes 20% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/pillager_great_tower_siege` | 1 | 1 | 32 | `(meta structure)` | meta structure: takes 50% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/pillager_stronghold_siege` | 1 | 1 | 32 | `(meta structure)` | meta structure: takes 40% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/pillager_tower_siege` | 1 | 1 | 32 | `(meta structure)` | meta structure: takes 35% of the slots its anchors win here; this weight is never used |
| `eugene:fake_trader_camp` | 6 | 1 | 32 | `eugene:camp` |  |
| `eugene:pillager_camp` | 26 | 1 | 32 | `eugene:camp` |  |
| `minecraft:pillager_outpost` | 74 | 1 | 32 | `minecraft:pillager_outposts` |  |
| `mysticrift_pillagers_towers_aris:acacia_pillager_tower` | 10 | 0.930863 | 33 | `mysticrift_pillagers_towers_aris:acacia_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:birch_pillager_tower` | 8 | 0.905604 | 34 | `mysticrift_pillagers_towers_aris:birch_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:cherry_pillager_tower` | 17 | 0.856877 | 35 | `mysticrift_pillagers_towers_aris:cherry_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:desert_pillager_tower` | 1 | 0.650021 | 40 | `mysticrift_pillagers_towers_aris:desert_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:end_pillager_tower` | 1 | 0.019706 | 228 | `mysticrift_pillagers_towers_aris:end_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | 10 | 0.973476 | 32 | `mysticrift_pillagers_towers_aris:jungle_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | 5 | 0.038089 | 164 | `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` | 5 | 0.467934 | 47 | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` |  |
| `takesapillage:bastille` | 34 | 1 | 32 | `takesapillage:pillager_structure` |  |
| `takesapillage:pillager_camp` | 87 | 1 | 32 | `takesapillage:pillager_structure` |  |
| `towns_and_towers:exclusives/pillager_outpost_classic` | 80 | 0.2338 | 66 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_iberian` | 100 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_mediterranean` | 78 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_oriental` | 65 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_rustic` | 100 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | 31 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_tudor` | 100 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_badlands` | 83 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_beach` | 4 | 0.878499 | 34 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_birch_forest` | 132 | 0.720211 | 38 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_desert` | 33 | 0.995091 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_flower_forest` | 210 | 0.891321 | 34 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_forest` | 136 | 0.610697 | 41 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_grove` | 172 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_jungle` | 109 | 0.560666 | 43 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_meadow` | 152 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_mushroom_fields` | 1 | 0.237868 | 66 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_old_growth_taiga` | 108 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_savanna` | 42 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_snowy_beach` | 151 | 0.4394 | 48 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_snowy_plains` | 156 | 0.584075 | 42 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_snowy_slopes` | 67 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_snowy_taiga` | 100 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_sparse_jungle` | 110 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_sunflower_plains` | 100 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_swamp` | 2 | 0.915151 | 33 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_taiga` | 45 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_wooded_badlands` | 81 | 1 | 32 | `towns_and_towers:towers` |  |

### `ruins` -> `creativeengineering:group/ruins`

Surface land ruins: Philip's Ruins and Terralith's glacial hut. Underground ruins stay standalone.

Grid: spacing 16, separation 8, salt 1418638049. Members never start closer than 9 chunks (144 blocks). 31 of 31 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `creativeengineering:meta/ruin_cluster` | 1 | 1 | 16 | `(meta structure)` | meta structure: takes 50% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/ruin_scatter` | 1 | 1 | 16 | `(meta structure)` | meta structure: takes 30% of the slots its anchors win here; this weight is never used |
| `eugene:ruined_drum_tower` | 19 | 1 | 16 | `eugene:castle` |  |
| `forest_ruins:ruin_1` | 38 | 0.904155 | 17 | `forest_ruins:ruin_1` |  |
| `forest_ruins:ruin_2` | 38 | 0.904155 | 17 | `forest_ruins:ruin_2` |  |
| `forest_ruins:ruin_3` | 38 | 0.904155 | 17 | `forest_ruins:ruin_3` |  |
| `forest_ruins:ruin_4` | 38 | 0.904155 | 17 | `forest_ruins:ruin_4` |  |
| `forest_ruins:ruin_5` | 38 | 0.904155 | 17 | `forest_ruins:ruin_5` |  |
| `forest_ruins:ruin_6` | 38 | 0.904155 | 17 | `forest_ruins:ruin_6` |  |
| `forest_ruins:ruin_7` | 38 | 0.904155 | 17 | `forest_ruins:ruin_7` |  |
| `forgotten_church:forgotten_church` | 16 | 1 | 16 | `forgotten_church:forgotten_church` |  |
| `old_illager_statue:old_illager_statue` | 26 | 1 | 16 | `old_illager_statue:old_illager_statue` |  |
| `oldvillagerstatue:old_villager_statue` | 26 | 1 | 16 | `oldvillagerstatue:old_villager_statue` |  |
| `philipsruins:ancient_ruins` | 119 | 0.860538 | 17 | `philipsruins:ancient_ruins` |  |
| `philipsruins:ancient_towers` | 197 | 0.862964 | 17 | `philipsruins:ancient_towers` |  |
| `philipsruins:badlands_structures` | 250 | 0.266459 | 31 | `philipsruins:desert_structures` |  |
| `philipsruins:desert_structures` | 250 | 0.250975 | 32 | `philipsruins:desert_structures` |  |
| `philipsruins:field_stone_ruins` | 45 | 1 | 16 | `philipsruins:field_stone_ruins` |  |
| `philipsruins:field_stone_ruins_rocks` | 196 | 0.513101 | 22 | `philipsruins:field_stone_ruins_rocks` |  |
| `philipsruins:level_one_ruins` | 45 | 1 | 16 | `philipsruins:level_one_ruins` |  |
| `philipsruins:level_three_ruins` | 35 | 1 | 16 | `philipsruins:level_three_ruins` |  |
| `philipsruins:level_two_ruins` | 20 | 1 | 16 | `philipsruins:level_two_ruins` |  |
| `philipsruins:level_two_ruins_pool` | 20 | 1 | 16 | `philipsruins:level_two_ruins` |  |
| `philipsruins:pumpkin_ruins` | 84 | 1 | 16 | `philipsruins:pumpkin_ruins` |  |
| `philipsruins:rare_ruin` | 254 | 0.007794 | 181 | `philipsruins:rare_ruin` |  |
| `philipsruins:start_nether_ruin` | 3 | 0.8688 | 17 | `philipsruins:nether_ruins` |  |
| `small_cat_statues:cat_1` | 19 | 1 | 16 | `small_cat_statues:cat_1` |  |
| `small_cat_statues:cat_2` | 19 | 1 | 16 | `small_cat_statues:cat_2` |  |
| `small_cat_statues:cat_3` | 19 | 1 | 16 | `small_cat_statues:cat_3` |  |
| `terralith:glacial_hut` | 261 | 0.686437 | 19 | `terralith:ruin` |  |
| `watchtower_building:abandoned_watchtower` | 21 | 0.194882 | 36 | `watchtower_building:abandoned_watchtower` |  |

### `surface_dungeons` -> `creativeengineering:group/surface_dungeons`

Surface dungeons: YUNG's desert and jungle temples, the vanilla desert pyramid and jungle temple (each in 10% of its YUNG's version's slots, "replaces" in structure_groups.json), the woodland mansion, Terralith's spire, the T&T desert mimic, Philip's badlands dungeon. The witch huts have their own group (group/witch_huts).

Grid: spacing 16, separation 10, salt 1261620025. Members never start closer than 11 chunks (176 blocks). 7 of 7 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `betterjungletemples:jungle_temple` | 1 | 0.7481 | 18 | `betterjungletemples:jungle_temples` |  |
| `eugene:undead_castle` | 1 | 0.026314 | 99 | `eugene:castle` |  |
| `minecraft:jungle_pyramid` | 1 | 1 | 16 | `minecraft:jungle_temples` | replaces `betterjungletemples:jungle_temple`: takes 10% of the slots it wins and keeps; this weight is never used |
| `minecraft:mansion` | 1 | 0.1191 | 46 | `minecraft:woodland_mansions` |  |
| `philipsruins:badlands_start_dungeon` | 106 | 0.441692 | 24 | `philipsruins:desert_structures` |  |
| `terralith:spire` | 1 | 0.445566 | 24 | `terralith:rare_dungeon` |  |
| `towns_and_towers:mimic_desert` | 79 | 0.521 | 22 | `towns_and_towers:other` |  |

### `desert_temples` -> `creativeengineering:group/desert_temples`

Desert temples: YUNG's desert temple and the vanilla desert pyramid, which takes a share of YUNG's temple slots ("replaces" in structure_groups.json). Own grid with twice the surface dungeons' minimum distance (structures/rates-and-groups #10).

Grid: spacing 32, separation 20, salt 1965760655. Members never start closer than 21 chunks (336 blocks). 2 of 2 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `betterdeserttemples:desert_temple` | 40 | 0.285 | 60 | `betterdeserttemples:desert_temples` |  |
| `minecraft:desert_pyramid` | 1 | 1 | 32 | `minecraft:desert_pyramids` | replaces `betterdeserttemples:desert_temple`: takes 10% of the slots it wins and keeps; this weight is never used |

### `witch_huts` -> `creativeengineering:group/witch_huts`

Witch huts, swamps only (land/swamp): YUNG's witch hut and witch circle, the Villages & Pillages witch village, and the vanilla swamp hut as a replacement of YUNG's hut. Own grid (at least 144 blocks between huts), so they no longer compete with the surface dungeons.

Grid: spacing 16, separation 8, salt 1023661448. Members never start closer than 9 chunks (144 blocks). 4 of 4 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `betterwitchhuts:witch_circle` | 61 | 0.9433 | 16 | `betterwitchhuts:witch_circle` |  |
| `betterwitchhuts:witch_hut` | 150 | 0.935 | 17 | `betterwitchhuts:witch_hut` |  |
| `minecraft:swamp_hut` | 1 | 1 | 16 | `minecraft:swamp_huts` | replaces `betterwitchhuts:witch_hut`: takes 10% of the slots it wins and keeps; this weight is never used |
| `villagesandpillages:village_witch` | 100 | 0.9374 | 17 | `villagesandpillages:village_witch` |  |

### `entrance_dungeons` -> `creativeengineering:group/entrance_dungeons`

Surface-entrance dungeons, land only: YUNG's zombie dungeon (crypt) and spider dungeon (spider cave). Own grid (at least 272 blocks apart); the water rule's surface check keeps their entrances out of the sea.

Grid: spacing 32, separation 16, salt 688304942. Members never start closer than 17 chunks (272 blocks). 2 of 2 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `betterdungeons:spider_dungeon` | 109 | 0.9754 | 32 | `betterdungeons:spider_dungeons` |  |
| `betterdungeons:zombie_dungeon` | 92 | 0.971 | 32 | `betterdungeons:zombie_dungeons` |  |

### `nether_ruins` -> `creativeengineering:group/nether_ruins`

Nether ruins: Twigs' bloodstone obelisk and Philip's three Nether ruins (Nether dungeon, sealed ghast statue, lava ruins). Grid from their crowding (spacing 14, separation 7). Added 2026-10-12.

Grid: spacing 14, separation 7, salt 1485554039. Members never start closer than 8 chunks (128 blocks). 4 of 4 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `philipsruins:nether_lava_ruins` | 100 | 0.5993 | 18 | `philipsruins:nether_structures` |  |
| `philipsruins:sealedruinedghaststatue1` | 100 | 0.5993 | 18 | `philipsruins:nether_structures` |  |
| `philipsruins:start_nether_dungeon` | 100 | 0.5993 | 18 | `philipsruins:nether_structures` |  |
| `twigs:bloodstone_obelisk` | 194 | 1 | 14 | `twigs:bloodstone_obelisk` |  |

### `way_signs` -> `creativeengineering:group/way_signs`

Supplementaries' way signs on a grid of their own (2026-10-12): the old set's placement (19/10, kept 8 chunks off settlement grid slots), so the rate is unchanged. Each sign points at the nearest #supplementaries:way_sign_destinations, which structure_overrides.js fills with every group/settlements member (villages, fortified villages, meta/village_towers).

Grid: spacing 19, separation 10, salt 247022060, exclusion_zone creativeengineering:group/settlements r8. Members never start closer than 11 chunks (176 blocks). 1 of 1 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `supplementaries:way_sign` | 1 | 1 | 19 | `supplementaries:way_signs` |  |

### `vehicles` -> `creativeengineering:group/vehicles`

Vehicles (hostile, 2026-10-14, plan 7.4.5): the 6 Illager Siege Weapons camps (moved out of pillager_settlements), the pillager loot carriage (with the villager chase and the golem prison van) and the witch wagon. Rarer than camps, more common than outposts, twice as common in harsh biomes (biome_frequency). Grid from the harsh rate (spacing 52: about 1 vehicle per 54 chunks in harsh biomes, 1 per 75 elsewhere, 2026-10-14); equal weights. Rates are hand-set (fit_base = the same values).

Grid: spacing 52, separation 16, salt 1361593970, exclusion_zone creativeengineering:group/settlements r4. Members never start closer than 17 chunks (272 blocks). 8 of 8 members live.

Biome frequency: every member's frequency x `#creativeengineering:harsh` 1, anywhere else 0.5 (the biome at its generation point; the first that matches).

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `eugene:pillager_loot_carriage` | 100 | 1 | 52 | `eugene:pillager_loot_carriage` |  |
| `eugene:witch_wagon` | 200 | 0.76 | 60 | `eugene:witch_wagon` |  |
| `illager_siege_weapons:ballista` | 100 | 1 | 52 | `illager_siege_weapons:ballista` |  |
| `illager_siege_weapons:battering_ram` | 100 | 1 | 52 | `illager_siege_weapons:battering_ram` |  |
| `illager_siege_weapons:catapult` | 100 | 1 | 52 | `illager_siege_weapons:catapult` |  |
| `illager_siege_weapons:reinforced_battering_ram` | 100 | 1 | 52 | `illager_siege_weapons:reinforced_battering_ram` |  |
| `illager_siege_weapons:siege_tower` | 100 | 1 | 52 | `illager_siege_weapons:siege_tower` |  |
| `illager_siege_weapons:trebuchet` | 100 | 1 | 52 | `illager_siege_weapons:trebuchet` |  |

### `ocean_ruins` -> `creativeengineering:group/ocean_ruins`

Ocean ruins and wrecks: vanilla, Hopo's and Philip's ocean ruins, Philip's ocean fortress, vanilla shipwrecks, the T&T wreckage.

Grid: spacing 8, separation 3, salt 541836770. Members never start closer than 4 chunks (64 blocks). 14 of 14 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `creativeengineering:meta/shipwreck_debris` | 1 | 1 | 8 | `(meta structure)` | meta structure: takes 30% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/shipwreck_graveyard` | 1 | 1 | 8 | `(meta structure)` | meta structure: takes 12% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/sunken_ruin_cluster` | 1 | 1 | 8 | `(meta structure)` | meta structure: takes 50% of the slots its anchors win here; this weight is never used |
| `hopo:underwater_fossils` | 215 | 1 | 8 | `hopo:underwater_fossils` |  |
| `hopo:underwater_ruins` | 113 | 1 | 8 | `hopo:underwater_ruins` |  |
| `hopo:underwater_small_ruins` | 513 | 0.6268 | 10 | `hopo:underwater_small_ruins` |  |
| `hopo:underwater_warm_small_ruins` | 560 | 0.9701 | 8 | `hopo:underwater_warm_small_ruins` |  |
| `minecraft:ocean_ruin_cold` | 143 | 0.627 | 10 | `minecraft:ocean_ruins` |  |
| `minecraft:ocean_ruin_warm` | 92 | 0.9743 | 8 | `minecraft:ocean_ruins` |  |
| `philipsruins:ocean_fortress` | 29 | 0.5047 | 11 | `philipsruins:ocean_fortress_main` |  |
| `philipsruins:ocean_ruins` | 58 | 0.4946 | 11 | `philipsruins:ocean_ruins` |  |
| `revamped_shipwrecks:shipwreck` | 258 | 1 | 8 | `revamped_shipwrecks:shipwreck` |  |
| `revamped_shipwrecks:shipwreck_beached` | 129 | 0.4444 | 12 | `revamped_shipwrecks:shipwreck` |  |
| `towns_and_towers:wreckage_ocean` | 108 | 0.3243 | 14 | `towns_and_towers:other` |  |

### `ocean_dungeons` -> `creativeengineering:group/ocean_dungeons`

Ocean monuments, on the monuments' own grid (same spacing, spread and salt, so positions match vanilla). Monument meta structures join later.

Grid: spacing 32, separation 6, spread_type triangular, salt 10387313. Members never start closer than 7 chunks (112 blocks). 1 of 1 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `minecraft:monument` | 100 | 0.1 | 101 | `minecraft:ocean_monuments` |  |

### `sky` -> `creativeengineering:group/sky`

Sky structures: Cloud Storage's hot air balloons and sky temples, AE2 meteorites (same rate as the clouds). Meta structures join later.

Grid: spacing 64, separation 16, salt 444386725. Members never start closer than 17 chunks (272 blocks). 4 of 4 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `ae2:meteorite` | 100 | 0.985 | 64 | `ae2:meteorite` |  |
| `cloudstorage:big_balloon` | 100 | 0.985 | 64 | `cloudstorage:big_balloon` |  |
| `cloudstorage:sky_temple` | 100 | 0.985 | 64 | `cloudstorage:sky_temple` |  |
| `skyvillages:skyvillage` | 299 | 1 | 64 | `skyvillages:skyvillage` |  |

### `boats` -> `creativeengineering:group/boats`

Ships at sea: the Towns & Towers ship village and pillager ship. Meta structures join later.

Grid: spacing 48, separation 28, salt 905198075. Members never start closer than 29 chunks (464 blocks). 12 of 12 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `antiquetradingship:antique_trading_ship` | 97 | 1 | 48 | `antiquetradingship:antique_trading_ship` |  |
| `blocky_siege:pillager_ship` | 57 | 1 | 48 | `blocky_siege:pillager_ship` |  |
| `creativeengineering:meta/pillager_fleet` | 1 | 1 | 48 | `(meta structure)` | meta structure: takes 35% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/raft_convoy` | 1 | 1 | 48 | `(meta structure)` | meta structure: takes 30% of the slots its anchors win here; this weight is never used |
| `illager_viking_longship:illager_longship` | 148 | 1 | 48 | `illager_viking_longship:illager_longship` |  |
| `illagerwarship:warship` | 97 | 1 | 48 | `illagerwarship:warship` |  |
| `towns_and_towers:pillager_outpost_ocean` | 72 | 0.9618 | 49 | `towns_and_towers:towers` |  |
| `towns_and_towers:village_ocean` | 138 | 0.9588 | 49 | `towns_and_towers:towns` |  |
| `unwrecked_ships:ship` | 73 | 1 | 48 | `unwrecked_ships:ship` |  |
| `villager_rafts:raft_1` | 83 | 1 | 48 | `villager_rafts:raft_1` |  |
| `villager_rafts:raft_2` | 83 | 1 | 48 | `villager_rafts:raft_2` |  |
| `villager_rafts:raft_3` | 83 | 1 | 48 | `villager_rafts:raft_3` |  |

### `flotsam` -> `creativeengineering:group/flotsam`

Floating debris at sea: the driftwood flotilla (meta structure). Its own grid, so it takes no slots from ruins or ships.

Grid: spacing 32, separation 8, salt 1805269162. Members never start closer than 9 chunks (144 blocks). 1 of 1 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `creativeengineering:meta/driftwood_flotilla` | 100 | 1 | 32 | `(meta structure)` |  |

### `remote` -> `creativeengineering:group/remote`

Lone camps and cabins: the T&T wandering trader camp and every single-building house mod in the Overworld. The two Farmer's Structures houses whose biomes come from mods this pack does not have (Aether, Undergarden) stay out.

Grid: spacing 24, separation 12, salt 419617315. Members never start closer than 13 chunks (208 blocks). 69 of 69 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `ancient_enchantment_temple:temple` | 1 | 0.1264 | 68 | `ancient_enchantment_temple:temple` |  |
| `animal_barn:barn` | 116 | 1 | 24 | `animal_barn:barn` |  |
| `beautify:botanist_house_desert` | 100 | 1 | 24 | `beautify:botanist_house_desert` |  |
| `beautify:botanist_house_plains` | 100 | 1 | 24 | `beautify:botanist_house_plains` |  |
| `beautify:botanist_house_savanna` | 100 | 1 | 24 | `beautify:botanist_house_savanna` |  |
| `beautify:botanist_house_snowy` | 100 | 1 | 24 | `beautify:botanist_house_snowy` |  |
| `beautify:botanist_house_taiga` | 100 | 1 | 24 | `beautify:botanist_house_taiga` |  |
| `bighorsestable:big_horse_stable` | 98 | 1 | 24 | `bighorsestable:big_horse_stable` |  |
| `campsite_structures:armorer_campsite` | 60 | 1 | 24 | `campsite_structures:armorer_campsite` |  |
| `campsite_structures:butcher_campsite` | 60 | 1 | 24 | `campsite_structures:butcher_campsite` |  |
| `campsite_structures:cartographer_campsite` | 60 | 1 | 24 | `campsite_structures:cartographer_campsite` |  |
| `campsite_structures:cleric_campsite` | 60 | 1 | 24 | `campsite_structures:cleric_campsite` |  |
| `campsite_structures:farmer_campsite` | 60 | 1 | 24 | `campsite_structures:farmer_campsite` |  |
| `campsite_structures:fisher_campsite` | 15 | 1 | 24 | `campsite_structures:fisher_campsite` |  |
| `creativeengineering:meta/farmers_camp` | 1 | 1 | 24 | `(meta structure)` | meta structure: takes 30% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/house_camp` | 1 | 1 | 24 | `(meta structure)` | meta structure: takes 15% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/waysign_camps` | 1 | 1 | 24 | `(meta structure)` | meta structure: takes 25% of the slots its anchors win here; this weight is never used |
| `creativeengineering:meta/waysign_house_camp` | 1 | 1 | 24 | `(meta structure)` | meta structure: takes 10% of the slots its anchors win here; this weight is never used |
| `desert_village_temple:village_temple` | 110 | 0.293336 | 44 | `desert_village_temple:village_temple` |  |
| `desert_watchtower:watchtower` | 110 | 0.342886 | 41 | `desert_watchtower:watchtower` |  |
| `eugene:camp` | 148 | 1 | 24 | `eugene:camp` |  |
| `farmers_structures:aquacultures_beach` | 64 | 0.0307 | 137 | `farmers_structures:aquacultures_beach_with_all` |  |
| `farmers_structures:aquacultures_carp` | 42 | 1 | 24 | `farmers_structures:aquacultures_carp_with_all` |  |
| `farmers_structures:aquacultures_common` | 42 | 1 | 24 | `farmers_structures:aquacultures_common_with_all` |  |
| `farmers_structures:cooks` | 183 | 0.417675 | 37 | `farmers_structures:cooks_with_all` |  |
| `farmers_structures:cooks_moss` | 100 | 1 | 24 | `farmers_structures:cooks_moss_with_all` |  |
| `farmers_structures:sushis` | 220 | 0.1058 | 74 | `farmers_structures:sushus_with_all` |  |
| `forest_watchtower:forest_watchtower` | 135 | 0.954795 | 25 | `forest_watchtower:forest_watchtower` |  |
| `gothic_church:church` | 81 | 1 | 24 | `gothic_church:church` |  |
| `greek_villager_house:greek_house` | 73 | 1 | 24 | `greek_villager_house:greek_house` |  |
| `hobbit_house_:hobbit_house` | 69 | 1 | 24 | `hobbit_house_:hobbit_house` |  |
| `llama_stable:llama_stable` | 62 | 1 | 24 | `llama_stable:llama_stable` |  |
| `minecraft:igloo` | 389 | 0.502816 | 34 | `minecraft:igloos` |  |
| `mini_clock_tower:mini_clock_tower` | 108 | 0.740947 | 28 | `mini_clock_tower:mini_clock_tower` |  |
| `old_swamp_fisherman_house:fisherman_house` | 247 | 0.147601 | 62 | `old_swamp_fisherman_house:fisherman_house` |  |
| `plains_villager_tents:big_villager_tent` | 116 | 1 | 24 | `plains_villager_tents:big_villager_tent` |  |
| `saloon:saloon` | 326 | 0.43779 | 36 | `saloon:saloon` |  |
| `sawmillhouse:sawmill` | 159 | 1 | 24 | `sawmillhouse:sawmill` |  |
| `simply_houses:forest_chapel` | 67 | 0.120438 | 69 | `simply_houses:forest_structures` |  |
| `simply_houses:forest_fort` | 67 | 0.120438 | 69 | `simply_houses:forest_structures` |  |
| `simply_houses:forest_house` | 135 | 0.1196 | 69 | `simply_houses:forest_structures` |  |
| `simply_houses:forest_stonemason` | 135 | 0.1196 | 69 | `simply_houses:forest_structures` |  |
| `simply_houses:plains_house` | 23 | 1 | 24 | `simply_houses:plains_structures` |  |
| `simply_houses:plains_mansion` | 23 | 1 | 24 | `simply_houses:plains_structures` |  |
| `simply_houses:taiga_forge` | 100 | 0.266606 | 46 | `simply_houses:taiga_structures` |  |
| `simply_houses:taiga_house` | 100 | 0.266606 | 46 | `simply_houses:taiga_structures` |  |
| `simply_houses:taiga_inn` | 200 | 0.266606 | 46 | `simply_houses:taiga_structures` |  |
| `simply_houses:taiga_woodcutter` | 200 | 0.266606 | 46 | `simply_houses:taiga_structures` |  |
| `small_viking_villager_house:small_viking_house` | 64 | 1 | 24 | `small_viking_villager_house:small_viking_house` |  |
| `small_villager_greenhouse:small_villager_greenhouse` | 72 | 1 | 24 | `small_villager_greenhouse:small_villager_greenhouse` |  |
| `smallhorsestable:horse_stable` | 111 | 1 | 24 | `smallhorsestable:horse_stable` |  |
| `snowy_tents:snowy_tent` | 284 | 1 | 24 | `snowy_tents:snowy_tent` |  |
| `towns_and_towers:exclusives/village_wandering_trader_camp` | 423 | 0.36673 | 40 | `towns_and_towers:towns` |  |
| `villager_archer_camp:villager_archer_camp` | 85 | 1 | 24 | `villager_archer_camp:villager_archer_camp` |  |
| `villager_butcher_house:butcher_house` | 87 | 1 | 24 | `villager_butcher_house:butcher_house` |  |
| `villager_cartographer_house:villager_cartographer_house` | 91 | 1 | 24 | `villager_cartographer_house:villager_cartographer_house` |  |
| `villager_circus_tent:villager_circus_tent` | 84 | 1 | 24 | `villager_circus_tent:villager_circus_tent` |  |
| `villager_cleric_house:villager_cleric_house` | 104 | 1 | 24 | `villager_cleric_house:villager_cleric_house` |  |
| `villager_country_house:country_house` | 447 | 0.166203 | 59 | `villager_country_house:country_house` |  |
| `villager_farmer_house:villager_farmer_house` | 74 | 1 | 24 | `villager_farmer_house:villager_farmer_house` |  |
| `villager_fish_shop:fish_shop` | 17 | 1 | 24 | `villager_fish_shop:fish_shop` |  |
| `villager_fishermans_hut:villager_fishermans_hut` | 125 | 1 | 24 | `villager_fishermans_hut:villager_fishermans_hut` |  |
| `villager_fletcher_house:villager_fletcher_house` | 109 | 1 | 24 | `villager_fletcher_house:villager_fletcher_house` |  |
| `villager_flower_house:flower_house` | 781 | 0.273887 | 46 | `villager_flower_house:flower_house` |  |
| `villager_forge:villager_forge` | 116 | 1 | 24 | `villager_forge:villager_forge` |  |
| `villager_gold_washing_facility:villager_gold_washing_facility` | 37 | 1 | 24 | `villager_gold_washing_facility:villager_gold_washing_facility` |  |
| `villager_tannery:tannery` | 73 | 1 | 24 | `villager_tannery:tannery` |  |
| `villager_windmill:windmill` | 103 | 1 | 24 | `villager_windmill:windmill` |  |
| `watchtower_building:fresh_watchtower` | 160 | 1 | 24 | `watchtower_building:fresh_watchtower` |  |

## Meta structures

Our structures built from other structures and features (`meta_structures.json`, patcherMod MetaStructure). They generate only through their group. `#creativeengineering:meta/*` = the `meta/*` tags below.

### `creativeengineering:meta/village_towers`

A village with 1 to 3 peaceful watchtowers or clock towers just outside it. A third of village slots try for towers; towers only stand where their own biome list allows, so a village where none fits stays a plain village.

Status: built. Group: `settlements`.

- anchor: `#creativeengineering:meta/villages` (`minecraft:village_desert`, `minecraft:village_plains`, `minecraft:village_savanna`, `minecraft:village_snowy`, `minecraft:village_taiga`, `towns_and_towers:exclusives/village_classic`, `towns_and_towers:exclusives/village_iberian`, `towns_and_towers:exclusives/village_mediterranean`, `towns_and_towers:exclusives/village_nilotic`, `towns_and_towers:exclusives/village_rustic`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:village_badlands`, `towns_and_towers:village_beach`, `towns_and_towers:village_birch_forest`, `towns_and_towers:village_flower_forest`, `towns_and_towers:village_forest`, `towns_and_towers:village_grove`, `towns_and_towers:village_jungle`, `towns_and_towers:village_meadow`, `towns_and_towers:village_mushroom_fields`, `towns_and_towers:village_old_growth_taiga`, `towns_and_towers:village_snowy_slopes`, `towns_and_towers:village_snowy_taiga`, `towns_and_towers:village_sparse_jungle`, `towns_and_towers:village_sunflower_plains`, `towns_and_towers:village_swamp`, `towns_and_towers:village_wooded_badlands`); takes 33% of the slots these win in its group (share 0.33)
- part 1: `#creativeengineering:meta/peaceful_towers` (`desert_watchtower:watchtower`, `forest_watchtower:forest_watchtower`, `mini_clock_tower:mini_clock_tower`, `watchtower_building:fresh_watchtower`); count [1,3], distance [40,104], spacing 48, margin 6

### `creativeengineering:meta/driftwood_flotilla`

A raft of floating driftwood logs out at sea, packed closer since 2026-10-12 (logs within 26 blocks), sometimes with a villager raft drifting in the middle.

Status: built. Group: `flotsam`.

- part 1: `#creativeengineering:meta/flotilla_rafts` (`villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`); count 1, distance [0,14], chance 0.25, margin 3
- part 2: features `creativeengineering:meta/flotilla_driftwood`; count [8,16], distance [0,26], spacing 4, margin 2, tries 8, water_depth 2

### `creativeengineering:meta/ruin_cluster`

A large ruin (Philip's level-three or rare ruin, an ancient tower, the forgotten church) with 2 to 5 small ruins scattered close around it.

Status: built. Group: `ruins`.

- anchor: `#creativeengineering:meta/large_ruins` (`forgotten_church:forgotten_church`, `philipsruins:ancient_towers`, `philipsruins:level_three_ruins`, `philipsruins:rare_ruin`); takes 50% of the slots these win in its group (share 0.5)
- part 1: `#creativeengineering:meta/small_ruins` (`forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `philipsruins:ancient_ruins`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:level_one_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `terralith:glacial_hut`); count [2,5], distance [18,56], spacing 18, margin 3

### `creativeengineering:meta/ruin_scatter`

2 to 4 small ruins together: one small ruin's slot, plus 1 to 3 more nearby.

Status: built. Group: `ruins`.

- anchor: `#creativeengineering:meta/small_ruins` (`forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `philipsruins:ancient_ruins`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:level_one_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `terralith:glacial_hut`); takes 30% of the slots these win in its group (share 0.3)
- part 1: `#creativeengineering:meta/small_ruins` (`forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `philipsruins:ancient_ruins`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:level_one_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `terralith:glacial_hut`); count [1,3], distance [14,44], spacing 16, margin 3

### `creativeengineering:meta/sunken_ruin_cluster`

A large underwater ruin (Hopo's big ruins, Philip's ocean ruins or ocean fortress) with 2 or 3 small underwater ruins around it.

Status: built. Group: `ocean_ruins`.

- anchor: `#creativeengineering:meta/underwater_large_ruins` (`hopo:underwater_ruins`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`); takes 50% of the slots these win in its group (share 0.5)
- part 1: `#creativeengineering:meta/underwater_small_ruins` (`hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`); count [2,3], distance [18,56], spacing 16, margin 3

### `creativeengineering:meta/raft_convoy`

2 to 5 villager rafts drifting together.

Status: built. Group: `boats`.

- anchor: `#creativeengineering:meta/flotilla_rafts` (`villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`); takes 30% of the slots these win in its group (share 0.3)
- part 1: `#creativeengineering:meta/flotilla_rafts` (`villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`); count [1,4], distance [10,44], spacing 12, margin 3

### `creativeengineering:meta/shipwreck_debris`

A sunken shipwreck with its debris around it: 1 to 3 floating wreckage on the surface (wider apart) and up to 6 driftwood logs close to the wreck (ocean_ruins; 30% of sunken wreck slots). Shallow oceans for the wreckage.

Status: built. Group: `ocean_ruins`.

- anchor: `#creativeengineering:meta/sunken_shipwrecks` (`revamped_shipwrecks:shipwreck`); takes 30% of the slots these win in its group (share 0.3)
- part 1: `#creativeengineering:meta/floating_wreckage` (`towns_and_towers:wreckage_ocean`); count [1,3], distance [6,48], spacing 22, margin 2
- part 2: features `creativeengineering:meta/flotilla_driftwood`; count [0,6], distance [4,30], spacing 5, margin 2, tries 8, water_depth 2

### `creativeengineering:meta/shipwreck_graveyard`

A shipwreck graveyard: 3 to 8 shipwrecks, sunken or beached, within about 70 blocks.

Status: built. Group: `ocean_ruins`.

- anchor: `#creativeengineering:meta/shipwrecks` (`revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`); takes 12% of the slots these win in its group (share 0.12)
- part 1: `#creativeengineering:meta/shipwrecks` (`revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`); count [2,7], distance [20,72], spacing 24, margin 3

### `creativeengineering:meta/pillager_tower_siege`

A standard pillager outpost tower (vanilla or Towns & Towers) with 1 or 2 siege-weapon camps or pillager camps around it.

Status: built. Group: `pillager_settlements`.

- anchor: `#creativeengineering:meta/pillager_towers` (`minecraft:pillager_outpost`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`); takes 35% of the slots these win in its group (share 0.35)
- part 1: `#creativeengineering:meta/pillager_outworks` (`eugene:pillager_camp`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`); count [1,2], distance [24,64], spacing 22, margin 4

### `creativeengineering:meta/pillager_great_tower_siege`

A large pillager tower (Mysticrift) with 2 to 4 siege-weapon camps or pillager camps around it.

Status: built. Group: `pillager_settlements`.

- anchor: `#creativeengineering:meta/pillager_large_towers` (`mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`); takes 50% of the slots these win in its group (share 0.5)
- part 1: `#creativeengineering:meta/pillager_outworks` (`eugene:pillager_camp`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`); count [2,4], distance [22,72], spacing 22, margin 4

### `creativeengineering:meta/pillager_stronghold_siege`

A special pillager outpost (a Towns & Towers fort or the mushroom tower) with 2 or 3 siege-weapon camps or pillager camps around it.

Status: built. Group: `pillager_settlements`.

- anchor: `#creativeengineering:meta/pillager_strongholds` (`takesapillage:bastille`, `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_old_growth_taiga`); takes 40% of the slots these win in its group (share 0.4)
- part 1: `#creativeengineering:meta/pillager_outworks` (`eugene:pillager_camp`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`); count [2,3], distance [30,88], spacing 24, margin 5

### `creativeengineering:meta/pillager_camp_siege`

A pillager camp with 1 or 2 more camps and 1 or 2 siege-weapon camps: 2 to 3 camps and 1 to 2 siege weapons in all.

Status: built. Group: `pillager_settlements`.

- anchor: `#creativeengineering:meta/pillager_camps` (`eugene:pillager_camp`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`); takes 20% of the slots these win in its group (share 0.2)
- part 1: `#creativeengineering:meta/pillager_camps` (`eugene:pillager_camp`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`); count [1,2], distance [20,56], spacing 20, margin 4
- part 2: `#creativeengineering:meta/siege_weapons` (`illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`); count [1,2], distance [20,64], spacing 18, margin 4

### `creativeengineering:meta/pillager_camp_ruin`

Pillagers camped at a large ruin: a pillager camp, the ruin, and sometimes a second camp.

Status: built. Group: `pillager_settlements`.

- anchor: `#creativeengineering:meta/pillager_camps` (`eugene:pillager_camp`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`); takes 15% of the slots these win in its group (share 0.15)
- part 1: `#creativeengineering:meta/large_ruins` (`forgotten_church:forgotten_church`, `philipsruins:ancient_towers`, `philipsruins:level_three_ruins`, `philipsruins:rare_ruin`); count 1, distance [24,64], margin 6
- part 2: `#creativeengineering:meta/pillager_camps` (`eugene:pillager_camp`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`); count [0,1], distance [20,56], spacing 20, margin 4

### `creativeengineering:meta/pillager_camp_ruins`

Pillagers camped among small ruins: a pillager camp, 2 to 4 small ruins, and sometimes a second camp.

Status: built. Group: `pillager_settlements`.

- anchor: `#creativeengineering:meta/pillager_camps` (`eugene:pillager_camp`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`); takes 15% of the slots these win in its group (share 0.15)
- part 1: `#creativeengineering:meta/small_ruins` (`forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `philipsruins:ancient_ruins`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:level_one_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `terralith:glacial_hut`); count [2,4], distance [16,56], spacing 16, margin 4
- part 2: `#creativeengineering:meta/pillager_camps` (`eugene:pillager_camp`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`); count [0,1], distance [20,56], spacing 20, margin 4

### `creativeengineering:meta/pillager_fleet`

A pillager fleet: 2 or 3 pillager ships with 2 to 5 patches of floating wreckage and shipwrecks around them.

Status: built. Group: `boats`.

- anchor: `#creativeengineering:meta/pillager_boats` (`blocky_siege:pillager_ship`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `towns_and_towers:pillager_outpost_ocean`); takes 35% of the slots these win in its group (share 0.35)
- part 1: `#creativeengineering:meta/pillager_boats` (`blocky_siege:pillager_ship`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `towns_and_towers:pillager_outpost_ocean`); count [1,2], distance [36,96], spacing 40, margin 8
- part 2: `#creativeengineering:meta/fleet_wreckage` (`revamped_shipwrecks:shipwreck`, `towns_and_towers:wreckage_ocean`); count [2,5], distance [16,100], spacing 18, margin 4

### `creativeengineering:meta/fortified_ruins`

A fortified town or fort (a meta/fortified_towns anchor: Terralith's fortified villages, the red roof castle) with 2 to 6 small ruins outside its walls (settlements; 20% of fortified town slots, D1 2026-10-14).

Status: built. Group: `settlements`.

- anchor: `#creativeengineering:meta/fortified_towns` (`eugene:red_roof_castle`, `terralith:fortified_desert_village`, `terralith:fortified_village`); takes 20% of the slots these win in its group (share 0.2)
- part 1: `#creativeengineering:meta/small_ruins` (`forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `philipsruins:ancient_ruins`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:level_one_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `terralith:glacial_hut`); count [2,6], distance [44,108], spacing 18, margin 6

### `creativeengineering:meta/fortified_camps`

A fortified town or fort (a meta/fortified_towns anchor: Terralith's fortified villages, the red roof castle) with 2 to 4 friendly camps (villager tents, archer camp, circus tent, trader camps, Campsite camps) outside its walls (settlements; 20% of fortified town slots, D1 2026-10-14).

Status: built. Group: `settlements`.

- anchor: `#creativeengineering:meta/fortified_towns` (`eugene:red_roof_castle`, `terralith:fortified_desert_village`, `terralith:fortified_village`); takes 20% of the slots these win in its group (share 0.2)
- part 1: `#creativeengineering:meta/friendly_camps` (`campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `eugene:camp`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`); count [2,4], distance [44,108], spacing 24, margin 6

### `creativeengineering:meta/farmers_camp`

1 to 3 Farmer's Structures (the anchor plus 0 to 2 more) with 1 to 2 friendly camps nearby (remote; 30% of their slots).

Status: built. Group: `remote`.

- anchor: `#creativeengineering:meta/farmers_structures` (`farmers_structures:aquacultures_beach`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `farmers_structures:cooks_moss`, `farmers_structures:sushis`); takes 30% of the slots these win in its group (share 0.3)
- part 1: `#creativeengineering:meta/farmers_structures` (`farmers_structures:aquacultures_beach`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `farmers_structures:cooks_moss`, `farmers_structures:sushis`); count [0,2], distance [20,64], spacing 20, margin 4
- part 2: `#creativeengineering:meta/friendly_camps` (`campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `eugene:camp`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`); count [1,2], distance [20,64], spacing 20, margin 4

### `creativeengineering:meta/house_camp`

A lone house with a friendly camp a short way off (remote; 15% of lone house slots).

Status: built. Group: `remote`.

- anchor: `#creativeengineering:meta/lone_houses` (`animal_barn:barn`, `beautify:botanist_house_desert`, `beautify:botanist_house_plains`, `beautify:botanist_house_savanna`, `beautify:botanist_house_snowy`, `beautify:botanist_house_taiga`, `bighorsestable:big_horse_stable`, `greek_villager_house:greek_house`, `hobbit_house_:hobbit_house`, `llama_stable:llama_stable`, `minecraft:igloo`, `old_swamp_fisherman_house:fisherman_house`, `saloon:saloon`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_country_house:country_house`, `villager_farmer_house:villager_farmer_house`, `villager_fish_shop:fish_shop`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_flower_house:flower_house`, `villager_forge:villager_forge`, `villager_gold_washing_facility:villager_gold_washing_facility`, `villager_tannery:tannery`, `villager_windmill:windmill`); takes 15% of the slots these win in its group (share 0.15)
- part 1: `#creativeengineering:meta/friendly_camps` (`campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `eugene:camp`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`); count 1, distance [16,48], margin 4

### `creativeengineering:meta/waysign_camps`

A way sign by 1 to 3 friendly camps (the anchor plus 0 to 2 more) (remote; 25% of camp slots). The sign points at the nearest settlements.

Status: built. Group: `remote`.

- anchor: `#creativeengineering:meta/friendly_camps` (`campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `eugene:camp`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`); takes 25% of the slots these win in its group (share 0.25)
- part 1: `#creativeengineering:meta/friendly_camps` (`campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `eugene:camp`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`); count [0,2], distance [16,56], spacing 20, margin 4
- part 2: `#creativeengineering:meta/way_signs` (`supplementaries:way_sign`); count 1, distance [10,40], margin 3

### `creativeengineering:meta/waysign_house_camp`

A lone house, a friendly camp and a way sign together (remote; 10% of lone house slots).

Status: built. Group: `remote`.

- anchor: `#creativeengineering:meta/lone_houses` (`animal_barn:barn`, `beautify:botanist_house_desert`, `beautify:botanist_house_plains`, `beautify:botanist_house_savanna`, `beautify:botanist_house_snowy`, `beautify:botanist_house_taiga`, `bighorsestable:big_horse_stable`, `greek_villager_house:greek_house`, `hobbit_house_:hobbit_house`, `llama_stable:llama_stable`, `minecraft:igloo`, `old_swamp_fisherman_house:fisherman_house`, `saloon:saloon`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_country_house:country_house`, `villager_farmer_house:villager_farmer_house`, `villager_fish_shop:fish_shop`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_flower_house:flower_house`, `villager_forge:villager_forge`, `villager_gold_washing_facility:villager_gold_washing_facility`, `villager_tannery:tannery`, `villager_windmill:windmill`); takes 10% of the slots these win in its group (share 0.1)
- part 1: `#creativeengineering:meta/friendly_camps` (`campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `eugene:camp`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`); count 1, distance [16,48], margin 4
- part 2: `#creativeengineering:meta/way_signs` (`supplementaries:way_sign`); count 1, distance [10,40], margin 3

### `creativeengineering:meta/town_villages`

A fortified town or fort (a meta/fortified_towns anchor, the red roof castle included) with 1 to 3 villages around it, 64 to 96 blocks out (pulled in from 112 to 160 on 2026-10-19: at that range no companion reached its town, 08_phase_8.md; settlements; 40% of fortified town slots, D1 2026-10-14; the three shares sum to 0.8, so 20% of towns stay plain). The villages are companions: full starts in chunks of their own (patcherMod PlacementPriority), so they keep their whole reach; the town outranks them, so a village that would run into it doesn't start.

Status: built. Group: `settlements`.

- anchor: `#creativeengineering:meta/fortified_towns` (`eugene:red_roof_castle`, `terralith:fortified_desert_village`, `terralith:fortified_village`); takes 40% of the slots these win in its group (share 0.4)

## Standalone sets written

### `farmers_structures:aethers_with_all`

Source: FarmersStructures-1.0.0-1.19.2.jar. Placement: spacing 55, separation 31, frequency 0.985926, salt 214225994, exclusion_zone creativeengineering:group/settlements r6 (edited). **Every member disabled: written with no structures.**

- `farmers_structures:aethers`: weight 1, frequency 1, disabled

### `farmers_structures:cooks_nether_1_with_all`

Source: FarmersStructures-1.0.0-1.19.2.jar. Placement: spacing 36, separation 25, frequency 0.9504, salt 593245336, exclusion_zone creativeengineering:group/settlements r6 (edited).

- `farmers_structures:cooks_nether_1`: weight 1, frequency 1, alone: 1 per 37

### `farmers_structures:cooks_nether_2_with_all`

Source: FarmersStructures-1.0.0-1.19.2.jar. Placement: spacing 45, separation 27, frequency 0.97626, salt 433617316, exclusion_zone creativeengineering:group/settlements r6 (edited).

- `farmers_structures:cooks_nether_2`: weight 1, frequency 1, alone: 1 per 46

### `farmers_structures:undergardens_sea_with_all`

Source: FarmersStructures-1.0.0-1.19.2.jar. Placement: spacing 190, separation 147, frequency 0.991717, salt 664652465 (edited). **Every member disabled: written with no structures.**

- `farmers_structures:undergardens_sea`: weight 1, frequency 1, disabled

### `hearths:towers`

Source: hearths-v1.0.0-mc1.19u1.19.3.jar. Placement: spacing 12, separation 9, frequency 0.93375, salt 214798319 (edited).

- `hearths:crimson_tower`: weight 1, frequency 1, alone: 1 per 12
- `hearths:warped_tower`: weight 1, frequency 1, alone: 1 per 12

### `mes:starlight_voyager`

Source: mes-1.3.1-1.19.2-forge.jar. Placement: spacing 26, separation 17, frequency 0.935567, salt 987874585 (edited).

- `mes:starlight_voyager`: weight 1, frequency 1, alone: 1 per 27

### `minecraft:ancient_cities`

Source: vanilla_known_defaults. Placement: spacing 19, separation 6, frequency 0.940104, salt 20083232 (edited).

- `minecraft:ancient_city`: weight 1, frequency 1, alone: 1 per 20

### `minecraft:buried_treasures`

Source: vanilla_known_defaults. Placement: spacing 1, separation 0, frequency 0.015, frequency_reduction_method legacy_type_2, salt 0 (edited).

- `minecraft:buried_treasure`: weight 1, frequency 1, alone: 1 per 8

### `minecraft:ruined_portals`

Source: vanilla_known_defaults. Placement: spacing 40, separation 15, frequency 1, salt 34222645. **Every member disabled: written with no structures.**

- `minecraft:ruined_portal`: weight 1, frequency 1, disabled
- `minecraft:ruined_portal_desert`: weight 1, frequency 1, disabled
- `minecraft:ruined_portal_jungle`: weight 1, frequency 1, disabled
- `minecraft:ruined_portal_swamp`: weight 1, frequency 1, disabled
- `minecraft:ruined_portal_mountain`: weight 1, frequency 1, disabled
- `minecraft:ruined_portal_ocean`: weight 1, frequency 1, disabled
- `minecraft:ruined_portal_nether`: weight 1, frequency 1, disabled

### `minecraft:shipwrecks`

Source: Revamped Shipwrecks 1.1.0.jar. Placement: spacing 24, separation 4, frequency 1, salt 165745295. **Every member disabled: written with no structures.**


### `minecraft:strongholds`

Source: vanilla_known_defaults. Placement: frequency 1, count 192, distance 32, spread 3, salt 0 (edited).

- `minecraft:stronghold`: weight 1, frequency 1, alone: 1 per -, **blocked:** YUNG's Better Strongholds always stops it (no config option). Tuning this set has no effect.

### `nullscape:dragon_skeleton`

Source: Nullscape_1.19.3_v1.2.2.jar. Placement: spacing 25, separation 14, frequency 0.968492, spread_type linear, salt 461798080 (edited).

- `nullscape:dragon_skeleton`: weight 1, frequency 1, alone: 1 per 25

### `philipsruins:ancient_crypt`

Source: PhilipsRuins1.19.2-10.8.jar. Placement: spacing 56, separation 35, frequency 0.98, salt 265792644 (edited).

- `philipsruins:ancient_crypt`: weight 1, frequency 1, alone: 1 per 57

### `philipsruins:ancient_dungeon`

Source: PhilipsRuins1.19.2-10.8.jar. Placement: spacing 7, separation 2, frequency 0.98, salt 512345192 (edited).

- `philipsruins:ancient_dungeon`: weight 1, frequency 1, alone: 1 per 7

### `philipsruins:antiquus_crypta`

Source: PhilipsRuins1.19.2-10.8.jar. Placement: spacing 63, separation 28, frequency 0.98, salt 348998429 (edited).

- `philipsruins:antiquus_crypta`: weight 1, frequency 1, alone: 1 per 64

### `philipsruins:end_ruins`

Source: PhilipsRuins1.19.2-10.8.jar. Placement: spacing 24, separation 17, frequency 0.940408, salt 265792644 (edited).

- `philipsruins:end_ruins`: weight 1, frequency 1, alone: 1 per 25
- `philipsruins:end_gate_fortress`: weight 1, frequency 1, alone: 1 per 25

### `philipsruins:lost_soul_city`

Source: PhilipsRuins1.19.2-10.8.jar. Placement: spacing 37, separation 30, frequency 0.962578, salt 121229911 (edited).

- `philipsruins:lost_soul_city`: weight 1, frequency 1, alone: 1 per 38

### `philipsruins:underground_structures`

Source: PhilipsRuins1.19.2-10.8.jar. Placement: spacing 23, separation 13, frequency 1, salt 641781942 (edited).

- `philipsruins:lost_soul_dungeon`: weight 86, frequency 0.991, alone: 1 per 23
- `philipsruins:bone_dungeon`: weight 116, frequency 0.29382, alone: 1 per 42
- `philipsruins:underground_structures`: weight 582, frequency 0.9885, alone: 1 per 23
- `philipsruins:sculk_dungeon`: weight 86, frequency 0.991, alone: 1 per 23

### `terralith:underground`

Source: Terralith_1.19.3_v2.3.12.jar. Placement: spacing 3, separation 1, frequency 1, salt 69696969 (edited).

- `terralith:underground/oak_cabin`: weight 50, frequency 1, alone: 1 per 3
- `terralith:underground/giant_bee_hive`: weight 78, frequency 0.4261, alone: 1 per 5
- `terralith:underground/mining_outpost`: weight 713, frequency 0.1097, alone: 1 per 9
- `terralith:underground/old_refinery`: weight 258, frequency 0.1166, alone: 1 per 9
- `terralith:underground/sunken_tower`: weight 129, frequency 0.4277, alone: 1 per 5
- `terralith:underground/witch_hut`: weight 44, frequency 1, alone: 1 per 3

### `unwrecked_ships:nether_ship`

Source: Unwrecked Ships Plus 1.19-1.0.4 Forge.jar. Placement: spacing 60, separation 1, frequency 1, salt 65126240. **Every member disabled: written with no structures.**

- `unwrecked_ships:nether_ship`: weight 1, frequency 1, disabled

## Sets emptied into groups

Written with no structures and frequency 0: they can neither generate nor show up in /locate or map searches.

- `ae2:meteorite`: `ae2:meteorite` -> sky
- `ancient_enchantment_temple:temple`: `ancient_enchantment_temple:temple` -> remote
- `animal_barn:barn`: `animal_barn:barn` -> remote
- `antiquetradingship:antique_trading_ship`: `antiquetradingship:antique_trading_ship` -> boats
- `beautify:botanist_house_desert`: `beautify:botanist_house_desert` -> remote
- `beautify:botanist_house_plains`: `beautify:botanist_house_plains` -> remote
- `beautify:botanist_house_savanna`: `beautify:botanist_house_savanna` -> remote
- `beautify:botanist_house_snowy`: `beautify:botanist_house_snowy` -> remote
- `beautify:botanist_house_taiga`: `beautify:botanist_house_taiga` -> remote
- `betterdeserttemples:desert_temples`: `betterdeserttemples:desert_temple` -> desert_temples
- `betterdungeons:spider_dungeons`: `betterdungeons:spider_dungeon` -> entrance_dungeons
- `betterdungeons:zombie_dungeons`: `betterdungeons:zombie_dungeon` -> entrance_dungeons
- `betterjungletemples:jungle_temples`: `betterjungletemples:jungle_temple` -> surface_dungeons
- `betterwitchhuts:witch_circle`: `betterwitchhuts:witch_circle` -> witch_huts
- `betterwitchhuts:witch_hut`: `betterwitchhuts:witch_hut` -> witch_huts
- `bighorsestable:big_horse_stable`: `bighorsestable:big_horse_stable` -> remote
- `blocky_siege:pillager_ship`: `blocky_siege:pillager_ship` -> boats
- `campsite_structures:armorer_campsite`: `campsite_structures:armorer_campsite` -> remote
- `campsite_structures:butcher_campsite`: `campsite_structures:butcher_campsite` -> remote
- `campsite_structures:cartographer_campsite`: `campsite_structures:cartographer_campsite` -> remote
- `campsite_structures:cleric_campsite`: `campsite_structures:cleric_campsite` -> remote
- `campsite_structures:farmer_campsite`: `campsite_structures:farmer_campsite` -> remote
- `campsite_structures:fisher_campsite`: `campsite_structures:fisher_campsite` -> remote
- `cloudstorage:big_balloon`: `cloudstorage:big_balloon` -> sky
- `cloudstorage:sky_temple`: `cloudstorage:sky_temple` -> sky
- `desert_village_temple:village_temple`: `desert_village_temple:village_temple` -> remote
- `desert_watchtower:watchtower`: `desert_watchtower:watchtower` -> remote
- `eugene:camp`: `eugene:camp` -> remote, `eugene:pillager_camp` -> pillager_settlements, `eugene:fake_trader_camp` -> pillager_settlements
- `eugene:castle`: `eugene:undead_castle` -> surface_dungeons, `eugene:red_roof_castle` -> settlements, `eugene:ruined_drum_tower` -> ruins
- `eugene:pillager_loot_carriage`: `eugene:pillager_loot_carriage` -> vehicles
- `eugene:witch_wagon`: `eugene:witch_wagon` -> vehicles
- `farmers_structures:aquacultures_beach_with_all`: `farmers_structures:aquacultures_beach` -> remote
- `farmers_structures:aquacultures_carp_with_all`: `farmers_structures:aquacultures_carp` -> remote
- `farmers_structures:aquacultures_common_with_all`: `farmers_structures:aquacultures_common` -> remote
- `farmers_structures:cooks_moss_with_all`: `farmers_structures:cooks_moss` -> remote
- `farmers_structures:cooks_with_all`: `farmers_structures:cooks` -> remote
- `farmers_structures:sushus_with_all`: `farmers_structures:sushis` -> remote
- `forest_ruins:ruin_1`: `forest_ruins:ruin_1` -> ruins
- `forest_ruins:ruin_2`: `forest_ruins:ruin_2` -> ruins
- `forest_ruins:ruin_3`: `forest_ruins:ruin_3` -> ruins
- `forest_ruins:ruin_4`: `forest_ruins:ruin_4` -> ruins
- `forest_ruins:ruin_5`: `forest_ruins:ruin_5` -> ruins
- `forest_ruins:ruin_6`: `forest_ruins:ruin_6` -> ruins
- `forest_ruins:ruin_7`: `forest_ruins:ruin_7` -> ruins
- `forest_watchtower:forest_watchtower`: `forest_watchtower:forest_watchtower` -> remote
- `forgotten_church:forgotten_church`: `forgotten_church:forgotten_church` -> ruins
- `gothic_church:church`: `gothic_church:church` -> remote
- `greek_villager_house:greek_house`: `greek_villager_house:greek_house` -> remote
- `hobbit_house_:hobbit_house`: `hobbit_house_:hobbit_house` -> remote
- `hopo:underwater_fossils`: `hopo:underwater_fossils` -> ocean_ruins
- `hopo:underwater_ruins`: `hopo:underwater_ruins` -> ocean_ruins
- `hopo:underwater_small_ruins`: `hopo:underwater_small_ruins` -> ocean_ruins
- `hopo:underwater_warm_small_ruins`: `hopo:underwater_warm_small_ruins` -> ocean_ruins
- `illager_siege_weapons:ballista`: `illager_siege_weapons:ballista` -> vehicles
- `illager_siege_weapons:battering_ram`: `illager_siege_weapons:battering_ram` -> vehicles
- `illager_siege_weapons:catapult`: `illager_siege_weapons:catapult` -> vehicles
- `illager_siege_weapons:reinforced_battering_ram`: `illager_siege_weapons:reinforced_battering_ram` -> vehicles
- `illager_siege_weapons:siege_tower`: `illager_siege_weapons:siege_tower` -> vehicles
- `illager_siege_weapons:trebuchet`: `illager_siege_weapons:trebuchet` -> vehicles
- `illager_viking_longship:illager_longship`: `illager_viking_longship:illager_longship` -> boats
- `illagerwarship:warship`: `illagerwarship:warship` -> boats
- `llama_stable:llama_stable`: `llama_stable:llama_stable` -> remote
- `minecraft:desert_pyramids`: `minecraft:desert_pyramid` -> desert_temples
- `minecraft:igloos`: `minecraft:igloo` -> remote
- `minecraft:jungle_temples`: `minecraft:jungle_pyramid` -> surface_dungeons
- `minecraft:ocean_monuments`: `minecraft:monument` -> ocean_dungeons
- `minecraft:ocean_ruins`: `minecraft:ocean_ruin_cold` -> ocean_ruins, `minecraft:ocean_ruin_warm` -> ocean_ruins
- `minecraft:pillager_outposts`: `minecraft:pillager_outpost` -> pillager_settlements
- `minecraft:swamp_huts`: `minecraft:swamp_hut` -> witch_huts
- `minecraft:villages`: `minecraft:village_plains` -> settlements, `minecraft:village_desert` -> settlements, `minecraft:village_savanna` -> settlements, `minecraft:village_snowy` -> settlements, `minecraft:village_taiga` -> settlements
- `minecraft:woodland_mansions`: `minecraft:mansion` -> surface_dungeons
- `mini_clock_tower:mini_clock_tower`: `mini_clock_tower:mini_clock_tower` -> remote
- `mysticrift_pillagers_towers_aris:acacia_pillager_tower`: `mysticrift_pillagers_towers_aris:acacia_pillager_tower` -> pillager_settlements
- `mysticrift_pillagers_towers_aris:birch_pillager_tower`: `mysticrift_pillagers_towers_aris:birch_pillager_tower` -> pillager_settlements
- `mysticrift_pillagers_towers_aris:cherry_pillager_tower`: `mysticrift_pillagers_towers_aris:cherry_pillager_tower` -> pillager_settlements
- `mysticrift_pillagers_towers_aris:desert_pillager_tower`: `mysticrift_pillagers_towers_aris:desert_pillager_tower` -> pillager_settlements
- `mysticrift_pillagers_towers_aris:end_pillager_tower`: `mysticrift_pillagers_towers_aris:end_pillager_tower` -> pillager_settlements
- `mysticrift_pillagers_towers_aris:jungle_pillager_tower`: `mysticrift_pillagers_towers_aris:jungle_pillager_tower` -> pillager_settlements
- `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`: `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` -> pillager_settlements
- `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`: `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` -> pillager_settlements
- `old_illager_statue:old_illager_statue`: `old_illager_statue:old_illager_statue` -> ruins
- `old_swamp_fisherman_house:fisherman_house`: `old_swamp_fisherman_house:fisherman_house` -> remote
- `oldvillagerstatue:old_villager_statue`: `oldvillagerstatue:old_villager_statue` -> ruins
- `philipsruins:ancient_ruins`: `philipsruins:ancient_ruins` -> ruins
- `philipsruins:ancient_towers`: `philipsruins:ancient_towers` -> ruins
- `philipsruins:desert_structures`: `philipsruins:badlands_start_dungeon` -> surface_dungeons, `philipsruins:badlands_structures` -> ruins, `philipsruins:desert_structures` -> ruins
- `philipsruins:field_stone_ruins`: `philipsruins:field_stone_ruins` -> ruins
- `philipsruins:field_stone_ruins_rocks`: `philipsruins:field_stone_ruins_rocks` -> ruins
- `philipsruins:level_one_ruins`: `philipsruins:level_one_ruins` -> ruins
- `philipsruins:level_three_ruins`: `philipsruins:level_three_ruins` -> ruins
- `philipsruins:level_two_ruins`: `philipsruins:level_two_ruins` -> ruins, `philipsruins:level_two_ruins_pool` -> ruins
- `philipsruins:nether_ruins`: `philipsruins:start_nether_ruin` -> ruins
- `philipsruins:nether_structures`: `philipsruins:start_nether_dungeon` -> nether_ruins, `philipsruins:sealedruinedghaststatue1` -> nether_ruins, `philipsruins:nether_lava_ruins` -> nether_ruins
- `philipsruins:ocean_fortress_main`: `philipsruins:ocean_fortress` -> ocean_ruins
- `philipsruins:ocean_ruins`: `philipsruins:ocean_ruins` -> ocean_ruins
- `philipsruins:pumpkin_ruins`: `philipsruins:pumpkin_ruins` -> ruins
- `philipsruins:rare_ruin`: `philipsruins:rare_ruin` -> ruins
- `plains_villager_tents:big_villager_tent`: `plains_villager_tents:big_villager_tent` -> remote
- `revamped_shipwrecks:shipwreck`: `revamped_shipwrecks:shipwreck` -> ocean_ruins, `revamped_shipwrecks:shipwreck_beached` -> ocean_ruins
- `saloon:saloon`: `saloon:saloon` -> remote
- `sawmillhouse:sawmill`: `sawmillhouse:sawmill` -> remote
- `simply_houses:forest_structures`: `simply_houses:forest_chapel` -> remote, `simply_houses:forest_house` -> remote, `simply_houses:forest_stonemason` -> remote, `simply_houses:forest_fort` -> remote
- `simply_houses:plains_structures`: `simply_houses:plains_mansion` -> remote, `simply_houses:plains_house` -> remote
- `simply_houses:taiga_structures`: `simply_houses:taiga_house` -> remote, `simply_houses:taiga_forge` -> remote, `simply_houses:taiga_inn` -> remote, `simply_houses:taiga_woodcutter` -> remote
- `skyvillages:skyvillage`: `skyvillages:skyvillage` -> sky
- `small_cat_statues:cat_1`: `small_cat_statues:cat_1` -> ruins
- `small_cat_statues:cat_2`: `small_cat_statues:cat_2` -> ruins
- `small_cat_statues:cat_3`: `small_cat_statues:cat_3` -> ruins
- `small_viking_villager_house:small_viking_house`: `small_viking_villager_house:small_viking_house` -> remote
- `small_villager_greenhouse:small_villager_greenhouse`: `small_villager_greenhouse:small_villager_greenhouse` -> remote
- `smallhorsestable:horse_stable`: `smallhorsestable:horse_stable` -> remote
- `snowy_tents:snowy_tent`: `snowy_tents:snowy_tent` -> remote
- `supplementaries:way_signs`: `supplementaries:way_sign` -> way_signs
- `takesapillage:pillager_structure`: `takesapillage:bastille` -> pillager_settlements, `takesapillage:pillager_camp` -> pillager_settlements
- `terralith:rare_dungeon`: `terralith:spire` -> surface_dungeons
- `terralith:rare_village`: `terralith:fortified_village` -> settlements, `terralith:fortified_desert_village` -> settlements
- `terralith:ruin`: `terralith:glacial_hut` -> ruins
- `towns_and_towers:other`: `towns_and_towers:mimic_desert` -> surface_dungeons, `towns_and_towers:wreckage_ocean` -> ocean_ruins
- `towns_and_towers:towers`: `towns_and_towers:pillager_outpost_badlands` -> pillager_settlements, `towns_and_towers:pillager_outpost_beach` -> pillager_settlements, `towns_and_towers:pillager_outpost_birch_forest` -> pillager_settlements, `towns_and_towers:pillager_outpost_desert` -> pillager_settlements, `towns_and_towers:pillager_outpost_flower_forest` -> pillager_settlements, `towns_and_towers:pillager_outpost_forest` -> pillager_settlements, `towns_and_towers:pillager_outpost_grove` -> pillager_settlements, `towns_and_towers:pillager_outpost_jungle` -> pillager_settlements, `towns_and_towers:pillager_outpost_meadow` -> pillager_settlements, `towns_and_towers:pillager_outpost_mushroom_fields` -> pillager_settlements, `towns_and_towers:pillager_outpost_ocean` -> boats, `towns_and_towers:pillager_outpost_old_growth_taiga` -> pillager_settlements, `towns_and_towers:pillager_outpost_savanna` -> pillager_settlements, `towns_and_towers:pillager_outpost_snowy_beach` -> pillager_settlements, `towns_and_towers:pillager_outpost_snowy_plains` -> pillager_settlements, `towns_and_towers:pillager_outpost_snowy_slopes` -> pillager_settlements, `towns_and_towers:pillager_outpost_sparse_jungle` -> pillager_settlements, `towns_and_towers:pillager_outpost_sunflower_plains` -> pillager_settlements, `towns_and_towers:pillager_outpost_swamp` -> pillager_settlements, `towns_and_towers:pillager_outpost_taiga` -> pillager_settlements, `towns_and_towers:pillager_outpost_snowy_taiga` -> pillager_settlements, `towns_and_towers:pillager_outpost_wooded_badlands` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_mediterranean` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_tudor` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_classic` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_oriental` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_swedish` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_iberian` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_rustic` -> pillager_settlements
- `towns_and_towers:towns`: `towns_and_towers:village_badlands` -> settlements, `towns_and_towers:village_beach` -> settlements, `towns_and_towers:village_birch_forest` -> settlements, `towns_and_towers:village_flower_forest` -> settlements, `towns_and_towers:village_forest` -> settlements, `towns_and_towers:village_snowy_slopes` -> settlements, `towns_and_towers:village_jungle` -> settlements, `towns_and_towers:village_meadow` -> settlements, `towns_and_towers:village_mushroom_fields` -> settlements, `towns_and_towers:village_ocean` -> boats, `towns_and_towers:village_old_growth_taiga` -> settlements, `towns_and_towers:village_grove` -> settlements, `towns_and_towers:village_sunflower_plains` -> settlements, `towns_and_towers:village_snowy_taiga` -> settlements, `towns_and_towers:village_sparse_jungle` -> settlements, `towns_and_towers:village_swamp` -> settlements, `towns_and_towers:village_wooded_badlands` -> settlements, `towns_and_towers:exclusives/village_mediterranean` -> settlements, `towns_and_towers:exclusives/village_swedish` -> settlements, `towns_and_towers:exclusives/village_tudor` -> settlements, `towns_and_towers:exclusives/village_wandering_trader_camp` -> remote, `towns_and_towers:exclusives/village_classic` -> settlements, `towns_and_towers:exclusives/village_rustic` -> settlements, `towns_and_towers:exclusives/village_iberian` -> settlements, `towns_and_towers:exclusives/village_nilotic` -> settlements
- `twigs:bloodstone_obelisk`: `twigs:bloodstone_obelisk` -> nether_ruins
- `unwrecked_ships:ship`: `unwrecked_ships:ship` -> boats
- `villager_archer_camp:villager_archer_camp`: `villager_archer_camp:villager_archer_camp` -> remote
- `villager_butcher_house:butcher_house`: `villager_butcher_house:butcher_house` -> remote
- `villager_cartographer_house:villager_cartographer_house`: `villager_cartographer_house:villager_cartographer_house` -> remote
- `villager_circus_tent:villager_circus_tent`: `villager_circus_tent:villager_circus_tent` -> remote
- `villager_cleric_house:villager_cleric_house`: `villager_cleric_house:villager_cleric_house` -> remote
- `villager_country_house:country_house`: `villager_country_house:country_house` -> remote
- `villager_farmer_house:villager_farmer_house`: `villager_farmer_house:villager_farmer_house` -> remote
- `villager_fish_shop:fish_shop`: `villager_fish_shop:fish_shop` -> remote
- `villager_fishermans_hut:villager_fishermans_hut`: `villager_fishermans_hut:villager_fishermans_hut` -> remote
- `villager_fletcher_house:villager_fletcher_house`: `villager_fletcher_house:villager_fletcher_house` -> remote
- `villager_flower_house:flower_house`: `villager_flower_house:flower_house` -> remote
- `villager_forge:villager_forge`: `villager_forge:villager_forge` -> remote
- `villager_gold_washing_facility:villager_gold_washing_facility`: `villager_gold_washing_facility:villager_gold_washing_facility` -> remote
- `villager_rafts:raft_1`: `villager_rafts:raft_1` -> boats
- `villager_rafts:raft_2`: `villager_rafts:raft_2` -> boats
- `villager_rafts:raft_3`: `villager_rafts:raft_3` -> boats
- `villager_tannery:tannery`: `villager_tannery:tannery` -> remote
- `villager_windmill:windmill`: `villager_windmill:windmill` -> remote
- `villagesandpillages:village_witch`: `villagesandpillages:village_witch` -> witch_huts
- `watchtower_building:abandoned_watchtower`: `watchtower_building:abandoned_watchtower` -> ruins
- `watchtower_building:fresh_watchtower`: `watchtower_building:fresh_watchtower` -> remote

## Coverage

- Structures with no `type/*` or `dungeon/*` tag: `takesapillage:bastille`, `takesapillage:pillager_camp`, `underground_bunkers:underground_bunker`
- Structures with no `mod/*` tag: `takesapillage:bastille`, `takesapillage:pillager_camp`, `underground_bunkers:underground_bunker`
- Structures another mod stops in code (tuning them has no effect, see "blocked" in structure_config.json): `betterdungeons:small_nether_dungeon`, `minecraft:fortress`, `minecraft:stronghold`
- Structures in no group (73, standalone): `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterfortresses:fortress`, `betterstrongholds:stronghold`, `eugene:piglin_camp`, `farmers_structures:aethers`, `farmers_structures:cooks_nether_1`, `farmers_structures:cooks_nether_2`, `farmers_structures:undergardens_sea`, `hearths:netherrack_spiral`, `hearths:crimson_tower`, `hearths:warped_tower`, `mes:astral_hideaway`, `mes:astral_meteorite`, `mes:ender_spire`, `mes:enderbloom_grove`, `mes:enderkeep_courtyard`, `mes:enderpin_spikes`, `mes:enderskog`, `mes:enderwatch_tower`, `mes:endscraps`, `mes:manuscript_shrine`, `mes:monolith`, `mes:mythic_garden`, `mes:phantom_citadel`, `mes:placid_prairie`, `mes:ruined_pillar`, `mes:starlight_voyager`, `minecraft:ancient_city`, `minecraft:buried_treasure`, `minecraft:end_city`, `minecraft:mineshaft`, `minecraft:mineshaft_mesa`, `minecraft:fortress`, `minecraft:bastion_remnant`, `minecraft:nether_fossil`, `minecraft:ruined_portal`, `minecraft:ruined_portal_desert`, `minecraft:ruined_portal_jungle`, `minecraft:ruined_portal_swamp`, `minecraft:ruined_portal_mountain`, `minecraft:ruined_portal_ocean`, `minecraft:ruined_portal_nether`, `minecraft:stronghold`, `nullscape:dragon_skeleton`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:end_ruins`, `philipsruins:end_gate_fortress`, `philipsruins:lost_soul_city`, `philipsruins:lost_soul_dungeon`, `philipsruins:bone_dungeon`, `philipsruins:underground_structures`, `philipsruins:sculk_dungeon`, `subterrestrial:deepslate_cabin`, `subterrestrial:default_cabin`, `subterrestrial:desert_cabin`, `subterrestrial:ice_cabin`, `subterrestrial:jungle_cabin`, `subterrestrial:mesa_cabin`, `subterrestrial:ocean_cabin`, `subterrestrial:taiga_cabin`, `terralith:underground/oak_cabin`, `terralith:underground/giant_bee_hive`, `terralith:underground/mining_outpost`, `terralith:underground/old_refinery`, `terralith:underground/sunken_tower`, `terralith:underground/witch_hut`, `terralith:underground/frosted_dungeon`, `underground_bunkers:underground_bunker`, `unwrecked_ships:nether_ship`

## Tag members

- **`mod/minecraft`** 30: `minecraft:ancient_city`, `minecraft:bastion_remnant`, `minecraft:buried_treasure`, `minecraft:desert_pyramid`, `minecraft:end_city`, `minecraft:fortress`, `minecraft:igloo`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:mineshaft`, `minecraft:mineshaft_mesa`, `minecraft:monument`, `minecraft:nether_fossil`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `minecraft:pillager_outpost`, `minecraft:ruined_portal`, `minecraft:ruined_portal_desert`, `minecraft:ruined_portal_jungle`, `minecraft:ruined_portal_mountain`, `minecraft:ruined_portal_nether`, `minecraft:ruined_portal_ocean`, `minecraft:ruined_portal_swamp`, `minecraft:stronghold`, `minecraft:swamp_hut`, `minecraft:village_desert`, `minecraft:village_plains`, `minecraft:village_savanna`, `minecraft:village_snowy`, `minecraft:village_taiga`
- **`mod/towns_and_towers`** 56: `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:exclusives/village_classic`, `towns_and_towers:exclusives/village_iberian`, `towns_and_towers:exclusives/village_mediterranean`, `towns_and_towers:exclusives/village_nilotic`, `towns_and_towers:exclusives/village_rustic`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `towns_and_towers:mimic_desert`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:pillager_outpost_wooded_badlands`, `towns_and_towers:village_badlands`, `towns_and_towers:village_beach`, `towns_and_towers:village_birch_forest`, `towns_and_towers:village_flower_forest`, `towns_and_towers:village_forest`, `towns_and_towers:village_grove`, `towns_and_towers:village_jungle`, `towns_and_towers:village_meadow`, `towns_and_towers:village_mushroom_fields`, `towns_and_towers:village_ocean`, `towns_and_towers:village_old_growth_taiga`, `towns_and_towers:village_snowy_slopes`, `towns_and_towers:village_snowy_taiga`, `towns_and_towers:village_sparse_jungle`, `towns_and_towers:village_sunflower_plains`, `towns_and_towers:village_swamp`, `towns_and_towers:village_wooded_badlands`, `towns_and_towers:wreckage_ocean`
- **`mod/philipsruins`** 29: `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:badlands_start_dungeon`, `philipsruins:badlands_structures`, `philipsruins:bone_dungeon`, `philipsruins:desert_structures`, `philipsruins:end_gate_fortress`, `philipsruins:end_ruins`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:lost_soul_city`, `philipsruins:lost_soul_dungeon`, `philipsruins:nether_lava_ruins`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`, `philipsruins:pumpkin_ruins`, `philipsruins:rare_ruin`, `philipsruins:sculk_dungeon`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_dungeon`, `philipsruins:start_nether_ruin`, `philipsruins:underground_structures`
- **`mod/mes`** 16: `mes:astral_hideaway`, `mes:astral_meteorite`, `mes:ender_spire`, `mes:enderbloom_grove`, `mes:enderkeep_courtyard`, `mes:enderpin_spikes`, `mes:enderskog`, `mes:enderwatch_tower`, `mes:endscraps`, `mes:manuscript_shrine`, `mes:monolith`, `mes:mythic_garden`, `mes:phantom_citadel`, `mes:placid_prairie`, `mes:ruined_pillar`, `mes:starlight_voyager`
- **`mod/terralith`** 11: `terralith:fortified_desert_village`, `terralith:fortified_village`, `terralith:glacial_hut`, `terralith:spire`, `terralith:underground/frosted_dungeon`, `terralith:underground/giant_bee_hive`, `terralith:underground/mining_outpost`, `terralith:underground/oak_cabin`, `terralith:underground/old_refinery`, `terralith:underground/sunken_tower`, `terralith:underground/witch_hut`
- **`mod/farmers_structures`** 10: `farmers_structures:aethers`, `farmers_structures:aquacultures_beach`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `farmers_structures:cooks_moss`, `farmers_structures:cooks_nether_1`, `farmers_structures:cooks_nether_2`, `farmers_structures:sushis`, `farmers_structures:undergardens_sea`
- **`mod/beautify`** 5: `beautify:botanist_house_desert`, `beautify:botanist_house_plains`, `beautify:botanist_house_savanna`, `beautify:botanist_house_snowy`, `beautify:botanist_house_taiga`
- **`mod/hearths`** 3: `hearths:crimson_tower`, `hearths:netherrack_spiral`, `hearths:warped_tower`
- **`mod/cloudstorage`** 2: `cloudstorage:big_balloon`, `cloudstorage:sky_temple`
- **`mod/nullscape`** 1: `nullscape:dragon_skeleton`
- **`mod/supplementaries`** 1: `supplementaries:way_sign`
- **`mod/twigs`** 1: `twigs:bloodstone_obelisk`
- **`mod/betterdungeons`** 5: `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`
- **`mod/betterwitchhuts`** 2: `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`
- **`mod/betterdeserttemples`** 1: `betterdeserttemples:desert_temple`
- **`mod/betterfortresses`** 1: `betterfortresses:fortress`
- **`mod/betterjungletemples`** 1: `betterjungletemples:jungle_temple`
- **`mod/betterstrongholds`** 1: `betterstrongholds:stronghold`
- **`mod/yungs`** 11: `betterdeserttemples:desert_temple`, `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterfortresses:fortress`, `betterjungletemples:jungle_temple`, `betterstrongholds:stronghold`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`
- **`mod/creativeengineering`** 22: `creativeengineering:meta/driftwood_flotilla`, `creativeengineering:meta/farmers_camp`, `creativeengineering:meta/fortified_camps`, `creativeengineering:meta/fortified_ruins`, `creativeengineering:meta/house_camp`, `creativeengineering:meta/pillager_camp_ruin`, `creativeengineering:meta/pillager_camp_ruins`, `creativeengineering:meta/pillager_camp_siege`, `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/pillager_great_tower_siege`, `creativeengineering:meta/pillager_stronghold_siege`, `creativeengineering:meta/pillager_tower_siege`, `creativeengineering:meta/raft_convoy`, `creativeengineering:meta/ruin_cluster`, `creativeengineering:meta/ruin_scatter`, `creativeengineering:meta/shipwreck_debris`, `creativeengineering:meta/shipwreck_graveyard`, `creativeengineering:meta/sunken_ruin_cluster`, `creativeengineering:meta/town_villages`, `creativeengineering:meta/village_towers`, `creativeengineering:meta/waysign_camps`, `creativeengineering:meta/waysign_house_camp`
- **`mod/ae2`** 1: `ae2:meteorite`
- **`mod/simply_houses`** 10: `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`
- **`mod/greek_villager_house`** 1: `greek_villager_house:greek_house`
- **`mod/hobbit_house_`** 1: `hobbit_house_:hobbit_house`
- **`mod/old_swamp_fisherman_house`** 1: `old_swamp_fisherman_house:fisherman_house`
- **`mod/sawmillhouse`** 1: `sawmillhouse:sawmill`
- **`mod/small_viking_villager_house`** 1: `small_viking_villager_house:small_viking_house`
- **`mod/small_villager_greenhouse`** 1: `small_villager_greenhouse:small_villager_greenhouse`
- **`mod/villager_butcher_house`** 1: `villager_butcher_house:butcher_house`
- **`mod/villager_cartographer_house`** 1: `villager_cartographer_house:villager_cartographer_house`
- **`mod/villager_cleric_house`** 1: `villager_cleric_house:villager_cleric_house`
- **`mod/villager_country_house`** 1: `villager_country_house:country_house`
- **`mod/villager_farmer_house`** 1: `villager_farmer_house:villager_farmer_house`
- **`mod/villager_fletcher_house`** 1: `villager_fletcher_house:villager_fletcher_house`
- **`mod/villager_flower_house`** 1: `villager_flower_house:flower_house`
- **`mod/villager_tannery`** 1: `villager_tannery:tannery`
- **`mod/villager_houses`** 14: `greek_villager_house:greek_house`, `hobbit_house_:hobbit_house`, `old_swamp_fisherman_house:fisherman_house`, `sawmillhouse:sawmill`, `small_viking_villager_house:small_viking_house`, `small_villager_greenhouse:small_villager_greenhouse`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_country_house:country_house`, `villager_farmer_house:villager_farmer_house`, `villager_fletcher_house:villager_fletcher_house`, `villager_flower_house:flower_house`, `villager_tannery:tannery`
- **`mod/hopo`** 4: `hopo:underwater_fossils`, `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`
- **`mod/watchtower_building`** 2: `watchtower_building:abandoned_watchtower`, `watchtower_building:fresh_watchtower`
- **`mod/skyvillages`** 1: `skyvillages:skyvillage`
- **`mod/mysticrift_pillagers_towers_aris`** 8: `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`
- **`mod/ancient_enchantment_temple`** 1: `ancient_enchantment_temple:temple`
- **`mod/animal_barn`** 1: `animal_barn:barn`
- **`mod/antiquetradingship`** 1: `antiquetradingship:antique_trading_ship`
- **`mod/bighorsestable`** 1: `bighorsestable:big_horse_stable`
- **`mod/desert_village_temple`** 1: `desert_village_temple:village_temple`
- **`mod/desert_watchtower`** 1: `desert_watchtower:watchtower`
- **`mod/forest_ruins`** 7: `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`
- **`mod/forest_watchtower`** 1: `forest_watchtower:forest_watchtower`
- **`mod/forgotten_church`** 1: `forgotten_church:forgotten_church`
- **`mod/gothic_church`** 1: `gothic_church:church`
- **`mod/illager_siege_weapons`** 6: `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`
- **`mod/illager_viking_longship`** 1: `illager_viking_longship:illager_longship`
- **`mod/illagerwarship`** 1: `illagerwarship:warship`
- **`mod/llama_stable`** 1: `llama_stable:llama_stable`
- **`mod/mini_clock_tower`** 1: `mini_clock_tower:mini_clock_tower`
- **`mod/old_illager_statue`** 1: `old_illager_statue:old_illager_statue`
- **`mod/oldvillagerstatue`** 1: `oldvillagerstatue:old_villager_statue`
- **`mod/plains_villager_tents`** 1: `plains_villager_tents:big_villager_tent`
- **`mod/saloon`** 1: `saloon:saloon`
- **`mod/small_cat_statues`** 3: `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`
- **`mod/smallhorsestable`** 1: `smallhorsestable:horse_stable`
- **`mod/snowy_tents`** 1: `snowy_tents:snowy_tent`
- **`mod/villager_archer_camp`** 1: `villager_archer_camp:villager_archer_camp`
- **`mod/villager_circus_tent`** 1: `villager_circus_tent:villager_circus_tent`
- **`mod/villager_fish_shop`** 1: `villager_fish_shop:fish_shop`
- **`mod/villager_fishermans_hut`** 1: `villager_fishermans_hut:villager_fishermans_hut`
- **`mod/villager_forge`** 1: `villager_forge:villager_forge`
- **`mod/villager_gold_washing_facility`** 1: `villager_gold_washing_facility:villager_gold_washing_facility`
- **`mod/villager_rafts`** 3: `villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`
- **`mod/villager_windmill`** 1: `villager_windmill:windmill`
- **`mod/villagesandpillages`** 1: `villagesandpillages:village_witch`
- **`mod/revamped_shipwrecks`** 2: `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`
- **`mod/unwrecked_ships`** 2: `unwrecked_ships:nether_ship`, `unwrecked_ships:ship`
- **`mod/blocky_siege`** 1: `blocky_siege:pillager_ship`
- **`mod/eugene`** 9: `eugene:camp`, `eugene:fake_trader_camp`, `eugene:piglin_camp`, `eugene:pillager_camp`, `eugene:pillager_loot_carriage`, `eugene:red_roof_castle`, `eugene:ruined_drum_tower`, `eugene:undead_castle`, `eugene:witch_wagon`
- **`mod/campsite_structures`** 6: `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`
- **`mod/subterrestrial`** 8: `subterrestrial:deepslate_cabin`, `subterrestrial:default_cabin`, `subterrestrial:desert_cabin`, `subterrestrial:ice_cabin`, `subterrestrial:jungle_cabin`, `subterrestrial:mesa_cabin`, `subterrestrial:ocean_cabin`, `subterrestrial:taiga_cabin`
- **`mod/villager_buildings`** 16: `ancient_enchantment_temple:temple`, `animal_barn:barn`, `bighorsestable:big_horse_stable`, `desert_village_temple:village_temple`, `desert_watchtower:watchtower`, `forest_watchtower:forest_watchtower`, `gothic_church:church`, `llama_stable:llama_stable`, `mini_clock_tower:mini_clock_tower`, `saloon:saloon`, `smallhorsestable:horse_stable`, `villager_fish_shop:fish_shop`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_forge:villager_forge`, `villager_gold_washing_facility:villager_gold_washing_facility`, `villager_windmill:windmill`
- **`mod/villager_camps`** 10: `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`
- **`type/village`** 36: `creativeengineering:meta/fortified_camps`, `creativeengineering:meta/fortified_ruins`, `creativeengineering:meta/town_villages`, `creativeengineering:meta/village_towers`, `minecraft:village_desert`, `minecraft:village_plains`, `minecraft:village_savanna`, `minecraft:village_snowy`, `minecraft:village_taiga`, `terralith:fortified_desert_village`, `terralith:fortified_village`, `towns_and_towers:exclusives/village_classic`, `towns_and_towers:exclusives/village_iberian`, `towns_and_towers:exclusives/village_mediterranean`, `towns_and_towers:exclusives/village_nilotic`, `towns_and_towers:exclusives/village_rustic`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `towns_and_towers:village_badlands`, `towns_and_towers:village_beach`, `towns_and_towers:village_birch_forest`, `towns_and_towers:village_flower_forest`, `towns_and_towers:village_forest`, `towns_and_towers:village_grove`, `towns_and_towers:village_jungle`, `towns_and_towers:village_meadow`, `towns_and_towers:village_mushroom_fields`, `towns_and_towers:village_ocean`, `towns_and_towers:village_old_growth_taiga`, `towns_and_towers:village_snowy_slopes`, `towns_and_towers:village_snowy_taiga`, `towns_and_towers:village_sparse_jungle`, `towns_and_towers:village_sunflower_plains`, `towns_and_towers:village_swamp`, `towns_and_towers:village_wooded_badlands`
- **`type/house`** 60: `ancient_enchantment_temple:temple`, `animal_barn:barn`, `beautify:botanist_house_desert`, `beautify:botanist_house_plains`, `beautify:botanist_house_savanna`, `beautify:botanist_house_snowy`, `beautify:botanist_house_taiga`, `bighorsestable:big_horse_stable`, `creativeengineering:meta/farmers_camp`, `creativeengineering:meta/house_camp`, `creativeengineering:meta/waysign_camps`, `creativeengineering:meta/waysign_house_camp`, `desert_village_temple:village_temple`, `desert_watchtower:watchtower`, `farmers_structures:aethers`, `farmers_structures:aquacultures_beach`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `farmers_structures:cooks_moss`, `farmers_structures:cooks_nether_1`, `farmers_structures:cooks_nether_2`, `farmers_structures:sushis`, `farmers_structures:undergardens_sea`, `forest_watchtower:forest_watchtower`, `gothic_church:church`, `greek_villager_house:greek_house`, `hobbit_house_:hobbit_house`, `llama_stable:llama_stable`, `minecraft:igloo`, `mini_clock_tower:mini_clock_tower`, `old_swamp_fisherman_house:fisherman_house`, `saloon:saloon`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_country_house:country_house`, `villager_farmer_house:villager_farmer_house`, `villager_fish_shop:fish_shop`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_flower_house:flower_house`, `villager_forge:villager_forge`, `villager_gold_washing_facility:villager_gold_washing_facility`, `villager_tannery:tannery`, `villager_windmill:windmill`
- **`type/camp`** 19: `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:piglin_camp`, `eugene:pillager_camp`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`
- **`type/fort`** 9: `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_old_growth_taiga`
- **`type/castle`** 1: `eugene:red_roof_castle`
- **`type/vehicle`** 8: `eugene:pillager_loot_carriage`, `eugene:witch_wagon`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`
- **`type/tower`** 30: `hearths:crimson_tower`, `hearths:warped_tower`, `minecraft:pillager_outpost`, `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`, `philipsruins:ancient_towers`, `terralith:spire`, `terralith:underground/sunken_tower`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`, `watchtower_building:fresh_watchtower`
- **`type/ship`** 14: `antiquetradingship:antique_trading_ship`, `blocky_siege:pillager_ship`, `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/raft_convoy`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `mes:starlight_voyager`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:village_ocean`, `unwrecked_ships:nether_ship`, `unwrecked_ships:ship`, `villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`
- **`type/enemy_building`** 63: `betterwitchhuts:witch_hut`, `blocky_siege:pillager_ship`, `creativeengineering:meta/pillager_camp_ruin`, `creativeengineering:meta/pillager_camp_ruins`, `creativeengineering:meta/pillager_camp_siege`, `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/pillager_great_tower_siege`, `creativeengineering:meta/pillager_stronghold_siege`, `creativeengineering:meta/pillager_tower_siege`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:pillager_loot_carriage`, `eugene:witch_wagon`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `minecraft:mansion`, `minecraft:pillager_outpost`, `minecraft:swamp_hut`, `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`, `terralith:underground/witch_hut`, `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:pillager_outpost_wooded_badlands`, `villagesandpillages:village_witch`
- **`type/ruin`** 29: `creativeengineering:meta/ruin_cluster`, `creativeengineering:meta/ruin_scatter`, `eugene:ruined_drum_tower`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:lost_soul_city`, `philipsruins:pumpkin_ruins`, `philipsruins:rare_ruin`, `philipsruins:start_nether_ruin`, `philipsruins:underground_structures`, `terralith:glacial_hut`, `terralith:underground/oak_cabin`, `watchtower_building:abandoned_watchtower`
- **`type/ruined_portal`** 7: `minecraft:ruined_portal`, `minecraft:ruined_portal_desert`, `minecraft:ruined_portal_jungle`, `minecraft:ruined_portal_mountain`, `minecraft:ruined_portal_nether`, `minecraft:ruined_portal_ocean`, `minecraft:ruined_portal_swamp`
- **`type/underwater_ruin`** 8: `creativeengineering:meta/sunken_ruin_cluster`, `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`
- **`type/shipwreck`** 5: `creativeengineering:meta/shipwreck_debris`, `creativeengineering:meta/shipwreck_graveyard`, `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`, `towns_and_towers:wreckage_ocean`
- **`type/mineshaft`** 3: `minecraft:mineshaft`, `minecraft:mineshaft_mesa`, `terralith:underground/mining_outpost`
- **`type/underground`** 17: `minecraft:buried_treasure`, `philipsruins:lost_soul_city`, `philipsruins:underground_structures`, `subterrestrial:deepslate_cabin`, `subterrestrial:default_cabin`, `subterrestrial:desert_cabin`, `subterrestrial:ice_cabin`, `subterrestrial:jungle_cabin`, `subterrestrial:mesa_cabin`, `subterrestrial:ocean_cabin`, `subterrestrial:taiga_cabin`, `terralith:underground/giant_bee_hive`, `terralith:underground/mining_outpost`, `terralith:underground/oak_cabin`, `terralith:underground/old_refinery`, `terralith:underground/sunken_tower`, `terralith:underground/witch_hut`
- **`type/environmental`** 4: `ae2:meteorite`, `creativeengineering:meta/driftwood_flotilla`, `hopo:underwater_fossils`, `terralith:underground/giant_bee_hive`
- **`type/decoration`** 14: `hearths:netherrack_spiral`, `mes:ender_spire`, `mes:enderpin_spikes`, `mes:endscraps`, `mes:ruined_pillar`, `minecraft:nether_fossil`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:field_stone_ruins_rocks`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `supplementaries:way_sign`, `twigs:bloodstone_obelisk`
- **`type/sky`** 3: `cloudstorage:big_balloon`, `cloudstorage:sky_temple`, `skyvillages:skyvillage`
- **`type/nether_environmental`** 2: `hearths:netherrack_spiral`, `minecraft:nether_fossil`
- **`type/nether_constructed`** 14: `betterdungeons:small_nether_dungeon`, `betterfortresses:fortress`, `eugene:piglin_camp`, `farmers_structures:cooks_nether_1`, `farmers_structures:cooks_nether_2`, `hearths:crimson_tower`, `hearths:warped_tower`, `minecraft:bastion_remnant`, `minecraft:fortress`, `minecraft:ruined_portal_nether`, `philipsruins:nether_lava_ruins`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_dungeon`, `twigs:bloodstone_obelisk`
- **`type/end_environmental`** 5: `mes:astral_meteorite`, `mes:enderbloom_grove`, `mes:enderskog`, `mes:placid_prairie`, `nullscape:dragon_skeleton`
- **`type/end_constructed`** 15: `mes:astral_hideaway`, `mes:ender_spire`, `mes:enderkeep_courtyard`, `mes:enderpin_spikes`, `mes:enderwatch_tower`, `mes:endscraps`, `mes:manuscript_shrine`, `mes:monolith`, `mes:mythic_garden`, `mes:phantom_citadel`, `mes:ruined_pillar`, `mes:starlight_voyager`, `minecraft:end_city`, `philipsruins:end_gate_fortress`, `philipsruins:end_ruins`
- **`dungeon/surface_vanilla`** 4: `minecraft:desert_pyramid`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:swamp_hut`
- **`dungeon/surface_better`** 4: `betterdeserttemples:desert_temple`, `betterjungletemples:jungle_temple`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`
- **`dungeon/surface_modded`** 6: `cloudstorage:sky_temple`, `eugene:undead_castle`, `philipsruins:badlands_start_dungeon`, `terralith:spire`, `towns_and_towers:mimic_desert`, `villagesandpillages:village_witch`
- **`dungeon/underground_vanilla`** 2: `minecraft:ancient_city`, `minecraft:stronghold`
- **`dungeon/underground_better`** 5: `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterstrongholds:stronghold`
- **`dungeon/underground_modded`** 7: `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_dungeon`, `philipsruins:sculk_dungeon`, `terralith:underground/frosted_dungeon`
- **`dungeon/surface`** 14: `betterdeserttemples:desert_temple`, `betterjungletemples:jungle_temple`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `cloudstorage:sky_temple`, `eugene:undead_castle`, `minecraft:desert_pyramid`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:swamp_hut`, `philipsruins:badlands_start_dungeon`, `terralith:spire`, `towns_and_towers:mimic_desert`, `villagesandpillages:village_witch`
- **`dungeon/underground`** 14: `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterstrongholds:stronghold`, `minecraft:ancient_city`, `minecraft:stronghold`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_dungeon`, `philipsruins:sculk_dungeon`, `terralith:underground/frosted_dungeon`
- **`dungeon/vanilla`** 10: `minecraft:ancient_city`, `minecraft:bastion_remnant`, `minecraft:desert_pyramid`, `minecraft:end_city`, `minecraft:fortress`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:monument`, `minecraft:stronghold`, `minecraft:swamp_hut`
- **`dungeon/better`** 11: `betterdeserttemples:desert_temple`, `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterfortresses:fortress`, `betterjungletemples:jungle_temple`, `betterstrongholds:stronghold`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`
- **`dungeon/modded`** 14: `cloudstorage:sky_temple`, `eugene:undead_castle`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:badlands_start_dungeon`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_dungeon`, `philipsruins:sculk_dungeon`, `philipsruins:start_nether_dungeon`, `terralith:spire`, `terralith:underground/frosted_dungeon`, `towns_and_towers:mimic_desert`, `villagesandpillages:village_witch`
- **`dungeon/classic`** 21: `betterdeserttemples:desert_temple`, `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterfortresses:fortress`, `betterjungletemples:jungle_temple`, `betterstrongholds:stronghold`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `minecraft:ancient_city`, `minecraft:bastion_remnant`, `minecraft:desert_pyramid`, `minecraft:end_city`, `minecraft:fortress`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:monument`, `minecraft:stronghold`, `minecraft:swamp_hut`
- **`dungeons`** 35: `betterdeserttemples:desert_temple`, `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterfortresses:fortress`, `betterjungletemples:jungle_temple`, `betterstrongholds:stronghold`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `cloudstorage:sky_temple`, `eugene:undead_castle`, `minecraft:ancient_city`, `minecraft:bastion_remnant`, `minecraft:desert_pyramid`, `minecraft:end_city`, `minecraft:fortress`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:monument`, `minecraft:stronghold`, `minecraft:swamp_hut`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:badlands_start_dungeon`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_dungeon`, `philipsruins:sculk_dungeon`, `philipsruins:start_nether_dungeon`, `terralith:spire`, `terralith:underground/frosted_dungeon`, `towns_and_towers:mimic_desert`, `villagesandpillages:village_witch`
- **`pillager`** 57: `blocky_siege:pillager_ship`, `creativeengineering:meta/pillager_camp_ruin`, `creativeengineering:meta/pillager_camp_ruins`, `creativeengineering:meta/pillager_camp_siege`, `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/pillager_great_tower_siege`, `creativeengineering:meta/pillager_stronghold_siege`, `creativeengineering:meta/pillager_tower_siege`, `eugene:pillager_camp`, `eugene:pillager_loot_carriage`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `minecraft:mansion`, `minecraft:pillager_outpost`, `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`, `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:pillager_outpost_wooded_badlands`
- **`nether`** 16: `betterdungeons:small_nether_dungeon`, `betterfortresses:fortress`, `eugene:piglin_camp`, `farmers_structures:cooks_nether_1`, `farmers_structures:cooks_nether_2`, `hearths:crimson_tower`, `hearths:netherrack_spiral`, `hearths:warped_tower`, `minecraft:bastion_remnant`, `minecraft:fortress`, `minecraft:nether_fossil`, `minecraft:ruined_portal_nether`, `philipsruins:nether_lava_ruins`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_dungeon`, `twigs:bloodstone_obelisk`
- **`end`** 20: `mes:astral_hideaway`, `mes:astral_meteorite`, `mes:ender_spire`, `mes:enderbloom_grove`, `mes:enderkeep_courtyard`, `mes:enderpin_spikes`, `mes:enderskog`, `mes:enderwatch_tower`, `mes:endscraps`, `mes:manuscript_shrine`, `mes:monolith`, `mes:mythic_garden`, `mes:phantom_citadel`, `mes:placid_prairie`, `mes:ruined_pillar`, `mes:starlight_voyager`, `minecraft:end_city`, `nullscape:dragon_skeleton`, `philipsruins:end_gate_fortress`, `philipsruins:end_ruins`
- **`ruins`** 49: `creativeengineering:meta/ruin_cluster`, `creativeengineering:meta/ruin_scatter`, `creativeengineering:meta/sunken_ruin_cluster`, `eugene:ruined_drum_tower`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`, `mes:endscraps`, `mes:ruined_pillar`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `minecraft:ruined_portal`, `minecraft:ruined_portal_desert`, `minecraft:ruined_portal_jungle`, `minecraft:ruined_portal_mountain`, `minecraft:ruined_portal_nether`, `minecraft:ruined_portal_ocean`, `minecraft:ruined_portal_swamp`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:end_ruins`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:lost_soul_city`, `philipsruins:nether_lava_ruins`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`, `philipsruins:pumpkin_ruins`, `philipsruins:rare_ruin`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_ruin`, `philipsruins:underground_structures`, `terralith:glacial_hut`, `terralith:underground/oak_cabin`, `watchtower_building:abandoned_watchtower`
- **`group/settlements`** 35: `creativeengineering:meta/fortified_camps`, `creativeengineering:meta/fortified_ruins`, `creativeengineering:meta/town_villages`, `creativeengineering:meta/village_towers`, `eugene:red_roof_castle`, `minecraft:village_desert`, `minecraft:village_plains`, `minecraft:village_savanna`, `minecraft:village_snowy`, `minecraft:village_taiga`, `terralith:fortified_desert_village`, `terralith:fortified_village`, `towns_and_towers:exclusives/village_classic`, `towns_and_towers:exclusives/village_iberian`, `towns_and_towers:exclusives/village_mediterranean`, `towns_and_towers:exclusives/village_nilotic`, `towns_and_towers:exclusives/village_rustic`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:village_badlands`, `towns_and_towers:village_beach`, `towns_and_towers:village_birch_forest`, `towns_and_towers:village_flower_forest`, `towns_and_towers:village_forest`, `towns_and_towers:village_grove`, `towns_and_towers:village_jungle`, `towns_and_towers:village_meadow`, `towns_and_towers:village_mushroom_fields`, `towns_and_towers:village_old_growth_taiga`, `towns_and_towers:village_snowy_slopes`, `towns_and_towers:village_snowy_taiga`, `towns_and_towers:village_sparse_jungle`, `towns_and_towers:village_sunflower_plains`, `towns_and_towers:village_swamp`, `towns_and_towers:village_wooded_badlands`
- **`group/pillager_settlements`** 47: `creativeengineering:meta/pillager_camp_ruin`, `creativeengineering:meta/pillager_camp_ruins`, `creativeengineering:meta/pillager_camp_siege`, `creativeengineering:meta/pillager_great_tower_siege`, `creativeengineering:meta/pillager_stronghold_siege`, `creativeengineering:meta/pillager_tower_siege`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `minecraft:pillager_outpost`, `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:pillager_outpost_wooded_badlands`
- **`group/ruins`** 31: `creativeengineering:meta/ruin_cluster`, `creativeengineering:meta/ruin_scatter`, `eugene:ruined_drum_tower`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `philipsruins:rare_ruin`, `philipsruins:start_nether_ruin`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `terralith:glacial_hut`, `watchtower_building:abandoned_watchtower`
- **`group/surface_dungeons`** 7: `betterjungletemples:jungle_temple`, `eugene:undead_castle`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `philipsruins:badlands_start_dungeon`, `terralith:spire`, `towns_and_towers:mimic_desert`
- **`group/desert_temples`** 2: `betterdeserttemples:desert_temple`, `minecraft:desert_pyramid`
- **`group/witch_huts`** 4: `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `minecraft:swamp_hut`, `villagesandpillages:village_witch`
- **`group/entrance_dungeons`** 2: `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`
- **`group/nether_ruins`** 4: `philipsruins:nether_lava_ruins`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_dungeon`, `twigs:bloodstone_obelisk`
- **`group/way_signs`** 1: `supplementaries:way_sign`
- **`group/vehicles`** 8: `eugene:pillager_loot_carriage`, `eugene:witch_wagon`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`
- **`group/ocean_ruins`** 14: `creativeengineering:meta/shipwreck_debris`, `creativeengineering:meta/shipwreck_graveyard`, `creativeengineering:meta/sunken_ruin_cluster`, `hopo:underwater_fossils`, `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`, `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`, `towns_and_towers:wreckage_ocean`
- **`group/ocean_dungeons`** 1: `minecraft:monument`
- **`group/sky`** 4: `ae2:meteorite`, `cloudstorage:big_balloon`, `cloudstorage:sky_temple`, `skyvillages:skyvillage`
- **`group/boats`** 12: `antiquetradingship:antique_trading_ship`, `blocky_siege:pillager_ship`, `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/raft_convoy`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:village_ocean`, `unwrecked_ships:ship`, `villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`
- **`group/flotsam`** 1: `creativeengineering:meta/driftwood_flotilla`
- **`group/remote`** 69: `ancient_enchantment_temple:temple`, `animal_barn:barn`, `beautify:botanist_house_desert`, `beautify:botanist_house_plains`, `beautify:botanist_house_savanna`, `beautify:botanist_house_snowy`, `beautify:botanist_house_taiga`, `bighorsestable:big_horse_stable`, `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `creativeengineering:meta/farmers_camp`, `creativeengineering:meta/house_camp`, `creativeengineering:meta/waysign_camps`, `creativeengineering:meta/waysign_house_camp`, `desert_village_temple:village_temple`, `desert_watchtower:watchtower`, `eugene:camp`, `farmers_structures:aquacultures_beach`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `farmers_structures:cooks_moss`, `farmers_structures:sushis`, `forest_watchtower:forest_watchtower`, `gothic_church:church`, `greek_villager_house:greek_house`, `hobbit_house_:hobbit_house`, `llama_stable:llama_stable`, `minecraft:igloo`, `mini_clock_tower:mini_clock_tower`, `old_swamp_fisherman_house:fisherman_house`, `plains_villager_tents:big_villager_tent`, `saloon:saloon`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `villager_archer_camp:villager_archer_camp`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_circus_tent:villager_circus_tent`, `villager_cleric_house:villager_cleric_house`, `villager_country_house:country_house`, `villager_farmer_house:villager_farmer_house`, `villager_fish_shop:fish_shop`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_flower_house:flower_house`, `villager_forge:villager_forge`, `villager_gold_washing_facility:villager_gold_washing_facility`, `villager_tannery:tannery`, `villager_windmill:windmill`, `watchtower_building:fresh_watchtower`
- **`water_ok`** 67: `ae2:meteorite`, `antiquetradingship:antique_trading_ship`, `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterstrongholds:stronghold`, `blocky_siege:pillager_ship`, `cloudstorage:big_balloon`, `cloudstorage:sky_temple`, `creativeengineering:meta/driftwood_flotilla`, `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/raft_convoy`, `creativeengineering:meta/shipwreck_debris`, `creativeengineering:meta/shipwreck_graveyard`, `creativeengineering:meta/sunken_ruin_cluster`, `farmers_structures:sushis`, `hopo:underwater_fossils`, `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `mes:starlight_voyager`, `minecraft:ancient_city`, `minecraft:buried_treasure`, `minecraft:mineshaft`, `minecraft:mineshaft_mesa`, `minecraft:monument`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `minecraft:ruined_portal_ocean`, `minecraft:stronghold`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_city`, `philipsruins:lost_soul_dungeon`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`, `philipsruins:sculk_dungeon`, `philipsruins:underground_structures`, `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`, `skyvillages:skyvillage`, `subterrestrial:deepslate_cabin`, `subterrestrial:default_cabin`, `subterrestrial:desert_cabin`, `subterrestrial:ice_cabin`, `subterrestrial:jungle_cabin`, `subterrestrial:mesa_cabin`, `subterrestrial:ocean_cabin`, `subterrestrial:taiga_cabin`, `terralith:underground/frosted_dungeon`, `terralith:underground/giant_bee_hive`, `terralith:underground/mining_outpost`, `terralith:underground/oak_cabin`, `terralith:underground/old_refinery`, `terralith:underground/sunken_tower`, `terralith:underground/witch_hut`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:village_ocean`, `towns_and_towers:wreckage_ocean`, `unwrecked_ships:nether_ship`, `unwrecked_ships:ship`, `villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`
- **`surface_check`** 2: `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`
- **`meta/villages`** 28: `minecraft:village_desert`, `minecraft:village_plains`, `minecraft:village_savanna`, `minecraft:village_snowy`, `minecraft:village_taiga`, `towns_and_towers:exclusives/village_classic`, `towns_and_towers:exclusives/village_iberian`, `towns_and_towers:exclusives/village_mediterranean`, `towns_and_towers:exclusives/village_nilotic`, `towns_and_towers:exclusives/village_rustic`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:village_badlands`, `towns_and_towers:village_beach`, `towns_and_towers:village_birch_forest`, `towns_and_towers:village_flower_forest`, `towns_and_towers:village_forest`, `towns_and_towers:village_grove`, `towns_and_towers:village_jungle`, `towns_and_towers:village_meadow`, `towns_and_towers:village_mushroom_fields`, `towns_and_towers:village_old_growth_taiga`, `towns_and_towers:village_snowy_slopes`, `towns_and_towers:village_snowy_taiga`, `towns_and_towers:village_sparse_jungle`, `towns_and_towers:village_sunflower_plains`, `towns_and_towers:village_swamp`, `towns_and_towers:village_wooded_badlands`
- **`meta/peaceful_towers`** 4: `desert_watchtower:watchtower`, `forest_watchtower:forest_watchtower`, `mini_clock_tower:mini_clock_tower`, `watchtower_building:fresh_watchtower`
- **`meta/flotilla_rafts`** 3: `villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`
- **`land`** 0: 
- **`land/plains`** 74: `animal_barn:barn`, `bighorsestable:big_horse_stable`, `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:pillager_loot_carriage`, `eugene:red_roof_castle`, `eugene:ruined_drum_tower`, `eugene:undead_castle`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `gothic_church:church`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:village_plains`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `plains_villager_tents:big_villager_tent`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `terralith:fortified_village`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:village_meadow`, `towns_and_towers:village_sunflower_plains`, `villager_archer_camp:villager_archer_camp`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_circus_tent:villager_circus_tent`, `villager_cleric_house:villager_cleric_house`, `villager_farmer_house:villager_farmer_house`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_forge:villager_forge`, `villager_tannery:tannery`, `villager_windmill:windmill`
- **`land/highlands`** 69: `animal_barn:barn`, `bighorsestable:big_horse_stable`, `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:ruined_drum_tower`, `eugene:undead_castle`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `gothic_church:church`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `llama_stable:llama_stable`, `minecraft:village_plains`, `minecraft:village_taiga`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `plains_villager_tents:big_villager_tent`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `terralith:fortified_village`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:village_old_growth_taiga`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_farmer_house:villager_farmer_house`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_forge:villager_forge`, `villager_tannery:tannery`, `villager_windmill:windmill`
- **`land/forest`** 51: `bighorsestable:big_horse_stable`, `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:red_roof_castle`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `gothic_church:church`, `hobbit_house_:hobbit_house`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:village_forest`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`, `villager_tannery:tannery`
- **`land/birch`** 6: `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:village_birch_forest`
- **`land/flower`** 11: `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `hobbit_house_:hobbit_house`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:village_flower_forest`, `towns_and_towers:village_meadow`, `towns_and_towers:village_sunflower_plains`, `villager_flower_house:flower_house`
- **`land/dark_forest`** 41: `bighorsestable:big_horse_stable`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:witch_wagon`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forest_watchtower:forest_watchtower`, `forgotten_church:forgotten_church`, `gothic_church:church`, `hobbit_house_:hobbit_house`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:mansion`, `minecraft:pillager_outpost`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `villager_tannery:tannery`
- **`land/taiga`** 63: `bighorsestable:big_horse_stable`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:undead_castle`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `gothic_church:church`, `hobbit_house_:hobbit_house`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:village_taiga`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `sawmillhouse:sawmill`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `small_viking_villager_house:small_viking_house`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `terralith:fortified_village`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:village_old_growth_taiga`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_farmer_house:villager_farmer_house`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_forge:villager_forge`, `villager_tannery:tannery`, `villager_windmill:windmill`
- **`land/snowy`** 32: `eugene:pillager_loot_carriage`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:igloo`, `minecraft:village_snowy`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:pumpkin_ruins`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`, `snowy_tents:snowy_tent`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:village_grove`, `towns_and_towers:village_snowy_slopes`, `towns_and_towers:village_snowy_taiga`
- **`land/icy`** 2: `terralith:glacial_hut`, `terralith:spire`
- **`land/desert`** 17: `betterdeserttemples:desert_temple`, `desert_village_temple:village_temple`, `eugene:pillager_loot_carriage`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:desert_pyramid`, `minecraft:village_desert`, `philipsruins:desert_structures`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `terralith:fortified_desert_village`, `towns_and_towers:mimic_desert`, `towns_and_towers:pillager_outpost_desert`
- **`land/badlands`** 18: `eugene:pillager_loot_carriage`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:mineshaft_mesa`, `philipsruins:badlands_start_dungeon`, `philipsruins:badlands_structures`, `saloon:saloon`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_wooded_badlands`, `towns_and_towers:village_badlands`, `towns_and_towers:village_wooded_badlands`, `villager_gold_washing_facility:villager_gold_washing_facility`
- **`land/savanna`** 27: `eugene:pillager_loot_carriage`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `greek_villager_house:greek_house`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `llama_stable:llama_stable`, `minecraft:village_savanna`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins_rocks`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_savanna`, `villager_country_house:country_house`
- **`land/warm_dry`** 4: `greek_villager_house:greek_house`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/village_mediterranean`, `villager_country_house:country_house`
- **`land/oasis`** 10: `betterdeserttemples:desert_temple`, `desert_village_temple:village_temple`, `minecraft:desert_pyramid`, `minecraft:village_desert`, `philipsruins:desert_structures`, `terralith:fortified_desert_village`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `towns_and_towers:mimic_desert`, `towns_and_towers:pillager_outpost_desert`
- **`land/jungle`** 18: `ancient_enchantment_temple:temple`, `betterjungletemples:jungle_temple`, `minecraft:jungle_pyramid`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:rare_ruin`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:village_jungle`, `towns_and_towers:village_sparse_jungle`
- **`land/swamp`** 23: `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `eugene:camp`, `eugene:fake_trader_camp`, `eugene:pillager_camp`, `eugene:witch_wagon`, `minecraft:swamp_hut`, `old_swamp_fisherman_house:fisherman_house`, `philipsruins:ancient_crypt`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:antiquus_crypta`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `philipsruins:rare_ruin`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:village_swamp`, `villagesandpillages:village_witch`
- **`land/beach`** 4: `campsite_structures:fisher_campsite`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:village_beach`, `villager_fish_shop:fish_shop`
- **`land/peaks`** 2: `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`
- **`land/oak`** 2: `mini_clock_tower:mini_clock_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`
- **`land/spruce`** 3: `forest_watchtower:forest_watchtower`, `watchtower_building:abandoned_watchtower`, `watchtower_building:fresh_watchtower`
- **`land/acacia`** 1: `mysticrift_pillagers_towers_aris:acacia_pillager_tower`
- **`land/mangrove`** 1: `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`
- **`land/cherry`** 1: `mysticrift_pillagers_towers_aris:cherry_pillager_tower`
- **`land/sand`** 2: `desert_watchtower:watchtower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`
- **`land/end`** 1: `mysticrift_pillagers_towers_aris:end_pillager_tower`
- **`land/any`** 3: `betterdungeons:small_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`
- **`land/any/deep`** 3: `betterdungeons:skeleton_dungeon`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_city`
- **`land/sky`** 1: `skyvillages:skyvillage`
- **`meta/large_ruins`** 4: `forgotten_church:forgotten_church`, `philipsruins:ancient_towers`, `philipsruins:level_three_ruins`, `philipsruins:rare_ruin`
- **`meta/small_ruins`** 16: `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `philipsruins:ancient_ruins`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:level_one_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `terralith:glacial_hut`
- **`meta/underwater_large_ruins`** 3: `hopo:underwater_ruins`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`
- **`meta/underwater_small_ruins`** 2: `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`
- **`meta/shipwrecks`** 2: `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`
- **`meta/sunken_shipwrecks`** 1: `revamped_shipwrecks:shipwreck`
- **`meta/floating_wreckage`** 1: `towns_and_towers:wreckage_ocean`
- **`meta/fleet_wreckage`** 2: `revamped_shipwrecks:shipwreck`, `towns_and_towers:wreckage_ocean`
- **`meta/pillager_boats`** 4: `blocky_siege:pillager_ship`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `towns_and_towers:pillager_outpost_ocean`
- **`meta/pillager_towers`** 15: `minecraft:pillager_outpost`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`
- **`meta/fortified_towns`** 3: `eugene:red_roof_castle`, `terralith:fortified_desert_village`, `terralith:fortified_village`
- **`meta/friendly_camps`** 12: `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`, `campsite_structures:fisher_campsite`, `eugene:camp`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`
- **`meta/coast_ok`** 6: `creativeengineering:meta/driftwood_flotilla`, `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/raft_convoy`, `creativeengineering:meta/shipwreck_debris`, `creativeengineering:meta/shipwreck_graveyard`, `creativeengineering:meta/sunken_ruin_cluster`
- **`meta/lone_houses`** 40: `animal_barn:barn`, `beautify:botanist_house_desert`, `beautify:botanist_house_plains`, `beautify:botanist_house_savanna`, `beautify:botanist_house_snowy`, `beautify:botanist_house_taiga`, `bighorsestable:big_horse_stable`, `greek_villager_house:greek_house`, `hobbit_house_:hobbit_house`, `llama_stable:llama_stable`, `minecraft:igloo`, `old_swamp_fisherman_house:fisherman_house`, `saloon:saloon`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_country_house:country_house`, `villager_farmer_house:villager_farmer_house`, `villager_fish_shop:fish_shop`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_flower_house:flower_house`, `villager_forge:villager_forge`, `villager_gold_washing_facility:villager_gold_washing_facility`, `villager_tannery:tannery`, `villager_windmill:windmill`
- **`meta/farmers_structures`** 6: `farmers_structures:aquacultures_beach`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `farmers_structures:cooks_moss`, `farmers_structures:sushis`
- **`meta/way_signs`** 1: `supplementaries:way_sign`
- **`priority/big_settlements`** 6: `creativeengineering:meta/fortified_camps`, `creativeengineering:meta/fortified_ruins`, `creativeengineering:meta/town_villages`, `eugene:red_roof_castle`, `terralith:fortified_desert_village`, `terralith:fortified_village`
- **`meta/pillager_large_towers`** 8: `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`
- **`meta/pillager_strongholds`** 11: `takesapillage:bastille`, `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_old_growth_taiga`
- **`meta/pillager_camps`** 6: `eugene:pillager_camp`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`
- **`meta/siege_weapons`** 6: `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`
- **`meta/pillager_outworks`** 12: `eugene:pillager_camp`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `takesapillage:pillager_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`
- **`land/mushroom`** 2: `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:village_mushroom_fields`
- **`ocean/deep`** 1: `philipsruins:ocean_fortress`
- **`plan/move_to_nether`** 4: `philipsruins:lost_soul_city`, `philipsruins:lost_soul_dungeon`, `philipsruins:rare_ruin`, `philipsruins:start_nether_ruin`

