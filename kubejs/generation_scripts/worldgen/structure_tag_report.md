# Structure tuning report

Written 2026-09-29 02:20:56 UTC at world load by `kubejs/server_scripts/worldgen/structure_overrides.js`. Do not edit, it is rewritten every load. Edit `structure_groups.json` (rates) or the `group/*` tags in `structure_tags.json` (membership), then rejoin the world. Expected rates per biome: `structure_group_estimates.md` (`python3 tools/gen_structure_groups.py --check`).

## Groups

Members of a group share one grid: at most one structure per grid slot, and no two members start closer than separation + 1 chunks. "Alone: 1 per N" = one per N x N chunks where it is the only member that fits the biome (sharing a biome splits the slots by weight). 129 frequencies below 1 are rolled by patcherMod.

### `settlements` -> `creativeengineering:group/settlements`

Friendly settlements: vanilla and Towns & Towers villages, Terralith fortified villages. Meta structures join later.

Grid: spacing 48, separation 12, salt 871105700. Members never start closer than 13 chunks (208 blocks). 30 of 30 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `minecraft:village_desert` | 424 | 0.6975 | 57 | `minecraft:villages` |  |
| `minecraft:village_plains` | 162 | 0.9263 | 50 | `minecraft:villages` |  |
| `minecraft:village_savanna` | 176 | 0.8134 | 53 | `minecraft:villages` |  |
| `minecraft:village_snowy` | 132 | 1 | 48 | `minecraft:villages` |  |
| `minecraft:village_taiga` | 153 | 0.9436 | 49 | `minecraft:villages` |  |
| `terralith:fortified_desert_village` | 108 | 1 | 48 | `terralith:rare_village` |  |
| `terralith:fortified_village` | 53 | 1 | 48 | `terralith:rare_village` |  |
| `towns_and_towers:exclusives/village_classic` | 101 | 0.1623 | 119 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_iberian` | 100 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_mediterranean` | 41 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_nilotic` | 100 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_rustic` | 100 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_swedish` | 22 | 1 | 48 | `towns_and_towers:towns` |  |
| `towns_and_towers:exclusives/village_tudor` | 99 | 0.4132 | 75 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_badlands` | 173 | 0.2113 | 104 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_beach` | 101 | 0.1623 | 119 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_birch_forest` | 162 | 0.197 | 108 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_flower_forest` | 360 | 0.1637 | 119 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_forest` | 144 | 0.1749 | 115 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_grove` | 213 | 0.1696 | 117 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_jungle` | 46 | 0.4485 | 72 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_meadow` | 62 | 0.6944 | 58 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_mushroom_fields` | 101 | 0.1623 | 119 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_old_growth_taiga` | 110 | 0.2153 | 103 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_snowy_slopes` | 141 | 0.2799 | 91 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_snowy_taiga` | 108 | 0.1943 | 109 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_sparse_jungle` | 46 | 0.45 | 72 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_sunflower_plains` | 228 | 0.2615 | 94 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_swamp` | 142 | 0.5026 | 68 | `towns_and_towers:towns` |  |
| `towns_and_towers:village_wooded_badlands` | 205 | 0.1738 | 115 | `towns_and_towers:towns` |  |

### `pillager_settlements` -> `creativeengineering:group/pillager_settlements`

Pillager outposts: vanilla, and every Towns & Towers tower, fort and camp outpost except the ship (boats).

Grid: spacing 32, separation 8, salt 1867953461, exclusion_zone creativeengineering:group/settlements r4. Members never start closer than 9 chunks (144 blocks). 43 of 43 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `illager_siege_weapons:ballista` | 57 | 1 | 32 | `illager_siege_weapons:ballista` |  |
| `illager_siege_weapons:battering_ram` | 57 | 1 | 32 | `illager_siege_weapons:battering_ram` |  |
| `illager_siege_weapons:catapult` | 57 | 1 | 32 | `illager_siege_weapons:catapult` |  |
| `illager_siege_weapons:reinforced_battering_ram` | 57 | 1 | 32 | `illager_siege_weapons:reinforced_battering_ram` |  |
| `illager_siege_weapons:siege_tower` | 57 | 1 | 32 | `illager_siege_weapons:siege_tower` |  |
| `illager_siege_weapons:trebuchet` | 57 | 1 | 32 | `illager_siege_weapons:trebuchet` |  |
| `minecraft:pillager_outpost` | 74 | 0.6336 | 40 | `minecraft:pillager_outposts` |  |
| `mysticrift_pillagers_towers_aris:acacia_pillager_tower` | 5 | 0.4211 | 49 | `mysticrift_pillagers_towers_aris:acacia_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:birch_pillager_tower` | 5 | 0.38 | 52 | `mysticrift_pillagers_towers_aris:birch_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:cherry_pillager_tower` | 5 | 0.4211 | 49 | `mysticrift_pillagers_towers_aris:cherry_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:desert_pillager_tower` | 5 | 0.4692 | 47 | `mysticrift_pillagers_towers_aris:desert_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:end_pillager_tower` | 5 | 0.4301 | 49 | `mysticrift_pillagers_towers_aris:end_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | 5 | 0.9278 | 33 | `mysticrift_pillagers_towers_aris:jungle_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | 5 | 0.5938 | 42 | `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` |  |
| `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` | 5 | 0.9425 | 33 | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` |  |
| `towns_and_towers:exclusives/pillager_outpost_classic` | 80 | 0.2338 | 66 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_iberian` | 100 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_mediterranean` | 78 | 0.3596 | 53 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_oriental` | 65 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_rustic` | 100 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | 31 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:exclusives/pillager_outpost_tudor` | 100 | 0.2918 | 59 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_badlands` | 83 | 0.3573 | 54 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_beach` | 151 | 0.4394 | 48 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_birch_forest` | 132 | 0.2977 | 59 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_desert` | 159 | 0.3206 | 57 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_flower_forest` | 252 | 0.3165 | 57 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_forest` | 136 | 0.2912 | 59 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_grove` | 172 | 0.2852 | 60 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_jungle` | 109 | 0.3648 | 53 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_meadow` | 152 | 0.4 | 51 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_mushroom_fields` | 100 | 0.2918 | 59 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_old_growth_taiga` | 108 | 0.4216 | 49 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_savanna` | 42 | 0.8362 | 35 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_snowy_beach` | 151 | 0.4394 | 48 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_snowy_plains` | 156 | 0.4073 | 50 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_snowy_slopes` | 67 | 0.6132 | 41 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_snowy_taiga` | 100 | 0.3106 | 57 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_sparse_jungle` | 110 | 0.6864 | 39 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_sunflower_plains` | 100 | 0.2918 | 59 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_swamp` | 100 | 0.2918 | 59 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_taiga` | 45 | 1 | 32 | `towns_and_towers:towers` |  |
| `towns_and_towers:pillager_outpost_wooded_badlands` | 81 | 0.7962 | 36 | `towns_and_towers:towers` |  |

### `ruins` -> `creativeengineering:group/ruins`

Surface land ruins: Philip's Ruins and Terralith's glacial hut. Underground ruins stay standalone.

Grid: spacing 8, separation 4, salt 1418638049. Members never start closer than 5 chunks (80 blocks). 23 of 23 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `forest_ruins:ruin_1` | 38 | 0.7313 | 9 | `forest_ruins:ruin_1` |  |
| `forest_ruins:ruin_2` | 38 | 0.7313 | 9 | `forest_ruins:ruin_2` |  |
| `forest_ruins:ruin_3` | 38 | 0.7313 | 9 | `forest_ruins:ruin_3` |  |
| `forest_ruins:ruin_4` | 38 | 0.7313 | 9 | `forest_ruins:ruin_4` |  |
| `forest_ruins:ruin_5` | 38 | 0.7313 | 9 | `forest_ruins:ruin_5` |  |
| `forest_ruins:ruin_6` | 38 | 0.7313 | 9 | `forest_ruins:ruin_6` |  |
| `forest_ruins:ruin_7` | 38 | 0.7313 | 9 | `forest_ruins:ruin_7` |  |
| `forgotten_church:forgotten_church` | 16 | 0.9825 | 8 | `forgotten_church:forgotten_church` |  |
| `philipsruins:ancient_ruins` | 119 | 0.6739 | 10 | `philipsruins:ancient_ruins` |  |
| `philipsruins:ancient_towers` | 197 | 0.6758 | 10 | `philipsruins:ancient_towers` |  |
| `philipsruins:badlands_structures` | 250 | 0.2387 | 16 | `philipsruins:desert_structures` |  |
| `philipsruins:desert_structures` | 250 | 0.2387 | 16 | `philipsruins:desert_structures` |  |
| `philipsruins:field_stone_ruins` | 45 | 0.9895 | 8 | `philipsruins:field_stone_ruins` |  |
| `philipsruins:field_stone_ruins_rocks` | 196 | 0.322 | 14 | `philipsruins:field_stone_ruins_rocks` |  |
| `philipsruins:level_one_ruins` | 45 | 0.9895 | 8 | `philipsruins:level_one_ruins` |  |
| `philipsruins:level_three_ruins` | 35 | 0.9905 | 8 | `philipsruins:level_three_ruins` |  |
| `philipsruins:level_two_ruins` | 20 | 0.9784 | 8 | `philipsruins:level_two_ruins` |  |
| `philipsruins:level_two_ruins_pool` | 20 | 0.9784 | 8 | `philipsruins:level_two_ruins` |  |
| `philipsruins:pumpkin_ruins` | 84 | 1 | 8 | `philipsruins:pumpkin_ruins` |  |
| `philipsruins:rare_ruin` | 254 | 0.0082 | 88 | `philipsruins:rare_ruin` |  |
| `philipsruins:start_nether_ruin` | 3 | 0.8688 | 9 | `philipsruins:nether_ruins` |  |
| `terralith:glacial_hut` | 261 | 0.249 | 16 | `terralith:ruin` |  |
| `watchtower_building:abandoned_watchtower` | 21 | 1 | 8 | `watchtower_building:abandoned_watchtower` |  |

### `surface_dungeons` -> `creativeengineering:group/surface_dungeons`

Surface dungeons: YUNG's temples and witch huts, the woodland mansion, Terralith's spire, the T&T desert mimic, Philip's badlands dungeon. The vanilla temples YUNG's blocks in code stay out.

Grid: spacing 16, separation 10, salt 1261620025. Members never start closer than 11 chunks (176 blocks). 9 of 9 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `betterdeserttemples:desert_temple` | 97 | 0.7059 | 19 | `betterdeserttemples:desert_temples` |  |
| `betterjungletemples:jungle_temple` | 186 | 0.7481 | 18 | `betterjungletemples:jungle_temples` |  |
| `betterwitchhuts:witch_circle` | 62 | 1 | 16 | `betterwitchhuts:witch_circle` |  |
| `betterwitchhuts:witch_hut` | 172 | 0.8021 | 18 | `betterwitchhuts:witch_hut` |  |
| `minecraft:mansion` | 30 | 0.1191 | 46 | `minecraft:woodland_mansions` |  |
| `philipsruins:badlands_start_dungeon` | 106 | 0.426 | 25 | `philipsruins:desert_structures` |  |
| `terralith:spire` | 103 | 0.415 | 25 | `terralith:rare_dungeon` |  |
| `towns_and_towers:mimic_desert` | 79 | 0.7049 | 19 | `towns_and_towers:other` |  |
| `villagesandpillages:village_witch` | 101 | 1 | 16 | `villagesandpillages:village_witch` |  |

### `ocean_ruins` -> `creativeengineering:group/ocean_ruins`

Ocean ruins and wrecks: vanilla, Hopo's and Philip's ocean ruins, Philip's ocean fortress, vanilla shipwrecks, the T&T wreckage.

Grid: spacing 6, separation 2, salt 541836770. Members never start closer than 3 chunks (48 blocks). 10 of 10 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `hopo:underwater_ruins` | 113 | 1 | 6 | `hopo:underwater_ruins` |  |
| `hopo:underwater_small_ruins` | 513 | 0.6268 | 8 | `hopo:underwater_small_ruins` |  |
| `hopo:underwater_warm_small_ruins` | 560 | 0.9701 | 6 | `hopo:underwater_warm_small_ruins` |  |
| `minecraft:ocean_ruin_cold` | 143 | 0.627 | 8 | `minecraft:ocean_ruins` |  |
| `minecraft:ocean_ruin_warm` | 92 | 0.9743 | 6 | `minecraft:ocean_ruins` |  |
| `philipsruins:ocean_fortress` | 29 | 0.5047 | 8 | `philipsruins:ocean_fortress_main` |  |
| `philipsruins:ocean_ruins` | 58 | 0.4946 | 9 | `philipsruins:ocean_ruins` |  |
| `revamped_shipwrecks:shipwreck` | 258 | 1 | 6 | `revamped_shipwrecks:shipwreck` |  |
| `revamped_shipwrecks:shipwreck_beached` | 129 | 0.4444 | 9 | `revamped_shipwrecks:shipwreck` |  |
| `towns_and_towers:wreckage_ocean` | 108 | 0.3243 | 11 | `towns_and_towers:other` |  |

### `ocean_dungeons` -> `creativeengineering:group/ocean_dungeons`

Ocean monuments, on the monuments' own grid (same spacing, spread and salt, so positions match vanilla). Monument meta structures join later.

Grid: spacing 32, separation 6, spread_type triangular, salt 10387313. Members never start closer than 7 chunks (112 blocks). 1 of 1 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `minecraft:monument` | 100 | 0.1 | 101 | `minecraft:ocean_monuments` |  |

### `sky` -> `creativeengineering:group/sky`

Sky structures: Cloud Storage's hot air balloons and sky temples, AE2 meteorites (same rate as the clouds). Meta structures join later.

Grid: spacing 72, separation 16, salt 444386725. Members never start closer than 17 chunks (272 blocks). 4 of 4 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `ae2:meteorite` | 100 | 0.985 | 73 | `ae2:meteorite` |  |
| `cloudstorage:big_balloon` | 100 | 0.985 | 73 | `cloudstorage:big_balloon` |  |
| `cloudstorage:sky_temple` | 100 | 0.985 | 73 | `cloudstorage:sky_temple` |  |
| `skyvillages:skyvillage` | 299 | 1 | 72 | `skyvillages:skyvillage` |  |

### `boats` -> `creativeengineering:group/boats`

Ships at sea: the Towns & Towers ship village and pillager ship. Meta structures join later.

Grid: spacing 48, separation 28, salt 905198075. Members never start closer than 29 chunks (464 blocks). 10 of 10 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `antiquetradingship:antique_trading_ship` | 97 | 1 | 48 | `antiquetradingship:antique_trading_ship` |  |
| `blocky_siege:pillager_ship` | 57 | 1 | 48 | `blocky_siege:pillager_ship` |  |
| `illager_viking_longship:illager_longship` | 148 | 1 | 48 | `illager_viking_longship:illager_longship` |  |
| `illagerwarship:warship` | 97 | 1 | 48 | `illagerwarship:warship` |  |
| `towns_and_towers:pillager_outpost_ocean` | 72 | 0.9618 | 49 | `towns_and_towers:towers` |  |
| `towns_and_towers:village_ocean` | 138 | 0.9588 | 49 | `towns_and_towers:towns` |  |
| `unwrecked_ships:ship` | 73 | 1 | 48 | `unwrecked_ships:ship` |  |
| `villager_rafts:raft_1` | 83 | 1 | 48 | `villager_rafts:raft_1` |  |
| `villager_rafts:raft_2` | 83 | 1 | 48 | `villager_rafts:raft_2` |  |
| `villager_rafts:raft_3` | 83 | 1 | 48 | `villager_rafts:raft_3` |  |

### `remote` -> `creativeengineering:group/remote`

Lone camps and cabins: the T&T wandering trader camp and every single-building house mod in the Overworld. The two Farmer's Structures houses whose biomes come from mods this pack does not have (Aether, Undergarden) stay out.

Grid: spacing 16, separation 8, salt 419617315. Members never start closer than 9 chunks (144 blocks). 57 of 57 members live.

| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |
|---|---|---|---|---|---|
| `ancient_enchantment_temple:temple` | 110 | 0.1264 | 45 | `ancient_enchantment_temple:temple` |  |
| `animal_barn:barn` | 116 | 1 | 16 | `animal_barn:barn` |  |
| `beautify:botanist_house_desert` | 100 | 1 | 16 | `beautify:botanist_house_desert` |  |
| `beautify:botanist_house_plains` | 100 | 1 | 16 | `beautify:botanist_house_plains` |  |
| `beautify:botanist_house_savanna` | 100 | 1 | 16 | `beautify:botanist_house_savanna` |  |
| `beautify:botanist_house_snowy` | 100 | 1 | 16 | `beautify:botanist_house_snowy` |  |
| `beautify:botanist_house_taiga` | 100 | 1 | 16 | `beautify:botanist_house_taiga` |  |
| `bighorsestable:big_horse_stable` | 98 | 1 | 16 | `bighorsestable:big_horse_stable` |  |
| `desert_village_temple:village_temple` | 110 | 0.2528 | 32 | `desert_village_temple:village_temple` |  |
| `desert_watchtower:watchtower` | 110 | 0.2528 | 32 | `desert_watchtower:watchtower` |  |
| `farmers_structures:aquacultures_beach` | 64 | 0.0307 | 91 | `farmers_structures:aquacultures_beach_with_all` |  |
| `farmers_structures:aquacultures_carp` | 42 | 0.9679 | 16 | `farmers_structures:aquacultures_carp_with_all` |  |
| `farmers_structures:aquacultures_common` | 42 | 0.9679 | 16 | `farmers_structures:aquacultures_common_with_all` |  |
| `farmers_structures:cooks` | 183 | 0.3129 | 29 | `farmers_structures:cooks_with_all` |  |
| `farmers_structures:cooks_moss` | 100 | 1 | 16 | `farmers_structures:cooks_moss_with_all` |  |
| `farmers_structures:sushis` | 220 | 0.1058 | 49 | `farmers_structures:sushus_with_all` |  |
| `forest_watchtower:forest_watchtower` | 135 | 1 | 16 | `forest_watchtower:forest_watchtower` |  |
| `gothic_church:church` | 81 | 1 | 16 | `gothic_church:church` |  |
| `greek_villager_house:greek_house` | 73 | 1 | 16 | `greek_villager_house:greek_house` |  |
| `hobbit_house_:hobbit_house` | 69 | 0.9939 | 16 | `hobbit_house_:hobbit_house` |  |
| `llama_stable:llama_stable` | 62 | 1 | 16 | `llama_stable:llama_stable` |  |
| `minecraft:igloo` | 389 | 0.2286 | 33 | `minecraft:igloos` |  |
| `mini_clock_tower:mini_clock_tower` | 108 | 1 | 16 | `mini_clock_tower:mini_clock_tower` |  |
| `old_swamp_fisherman_house:fisherman_house` | 247 | 0.1192 | 46 | `old_swamp_fisherman_house:fisherman_house` |  |
| `plains_villager_tents:big_villager_tent` | 116 | 1 | 16 | `plains_villager_tents:big_villager_tent` |  |
| `saloon:saloon` | 326 | 0.3554 | 27 | `saloon:saloon` |  |
| `sawmillhouse:sawmill` | 159 | 0.8113 | 18 | `sawmillhouse:sawmill` |  |
| `simply_houses:forest_chapel` | 67 | 0.0718 | 60 | `simply_houses:forest_structures` |  |
| `simply_houses:forest_fort` | 67 | 0.0718 | 60 | `simply_houses:forest_structures` |  |
| `simply_houses:forest_house` | 135 | 0.0713 | 60 | `simply_houses:forest_structures` |  |
| `simply_houses:forest_stonemason` | 135 | 0.0713 | 60 | `simply_houses:forest_structures` |  |
| `simply_houses:plains_house` | 23 | 1 | 16 | `simply_houses:plains_structures` |  |
| `simply_houses:plains_mansion` | 23 | 1 | 16 | `simply_houses:plains_structures` |  |
| `simply_houses:taiga_forge` | 100 | 0.0871 | 54 | `simply_houses:taiga_structures` |  |
| `simply_houses:taiga_house` | 100 | 0.0871 | 54 | `simply_houses:taiga_structures` |  |
| `simply_houses:taiga_inn` | 200 | 0.0871 | 54 | `simply_houses:taiga_structures` |  |
| `simply_houses:taiga_woodcutter` | 200 | 0.0871 | 54 | `simply_houses:taiga_structures` |  |
| `small_viking_villager_house:small_viking_house` | 64 | 1 | 16 | `small_viking_villager_house:small_viking_house` |  |
| `small_villager_greenhouse:small_villager_greenhouse` | 72 | 1 | 16 | `small_villager_greenhouse:small_villager_greenhouse` |  |
| `smallhorsestable:horse_stable` | 111 | 1 | 16 | `smallhorsestable:horse_stable` |  |
| `snowy_tents:snowy_tent` | 284 | 1 | 16 | `snowy_tents:snowy_tent` |  |
| `towns_and_towers:exclusives/village_wandering_trader_camp` | 423 | 0.2036 | 35 | `towns_and_towers:towns` |  |
| `villager_archer_camp:villager_archer_camp` | 85 | 1 | 16 | `villager_archer_camp:villager_archer_camp` |  |
| `villager_butcher_house:butcher_house` | 87 | 1 | 16 | `villager_butcher_house:butcher_house` |  |
| `villager_cartographer_house:villager_cartographer_house` | 91 | 1 | 16 | `villager_cartographer_house:villager_cartographer_house` |  |
| `villager_circus_tent:villager_circus_tent` | 84 | 1 | 16 | `villager_circus_tent:villager_circus_tent` |  |
| `villager_cleric_house:villager_cleric_house` | 104 | 1 | 16 | `villager_cleric_house:villager_cleric_house` |  |
| `villager_country_house:country_house` | 447 | 0.1166 | 47 | `villager_country_house:country_house` |  |
| `villager_farmer_house:villager_farmer_house` | 74 | 1 | 16 | `villager_farmer_house:villager_farmer_house` |  |
| `villager_fish_shop:fish_shop` | 17 | 0.7785 | 18 | `villager_fish_shop:fish_shop` |  |
| `villager_fishermans_hut:villager_fishermans_hut` | 125 | 1 | 16 | `villager_fishermans_hut:villager_fishermans_hut` |  |
| `villager_fletcher_house:villager_fletcher_house` | 109 | 1 | 16 | `villager_fletcher_house:villager_fletcher_house` |  |
| `villager_flower_house:flower_house` | 781 | 0.1035 | 50 | `villager_flower_house:flower_house` |  |
| `villager_forge:villager_forge` | 116 | 1 | 16 | `villager_forge:villager_forge` |  |
| `villager_gold_washing_facility:villager_gold_washing_facility` | 37 | 1 | 16 | `villager_gold_washing_facility:villager_gold_washing_facility` |  |
| `villager_tannery:tannery` | 73 | 1 | 16 | `villager_tannery:tannery` |  |
| `villager_windmill:windmill` | 103 | 1 | 16 | `villager_windmill:windmill` |  |

## Standalone sets written

### `farmers_structures:aethers_with_all`

Source: FarmersStructures-1.0.0-1.19.2.jar. Placement: spacing 55, separation 31, frequency 0.985926, salt 214225994, exclusion_zone creativeengineering:group/settlements r6 (edited).

- `farmers_structures:aethers`: weight 1, frequency 1, alone: 1 per 55

### `farmers_structures:cooks_nether_1_with_all`

Source: FarmersStructures-1.0.0-1.19.2.jar. Placement: spacing 36, separation 25, frequency 0.9504, salt 593245336, exclusion_zone creativeengineering:group/settlements r6 (edited).

- `farmers_structures:cooks_nether_1`: weight 1, frequency 1, alone: 1 per 37

### `farmers_structures:cooks_nether_2_with_all`

Source: FarmersStructures-1.0.0-1.19.2.jar. Placement: spacing 45, separation 27, frequency 0.97626, salt 433617316, exclusion_zone creativeengineering:group/settlements r6 (edited).

- `farmers_structures:cooks_nether_2`: weight 1, frequency 1, alone: 1 per 46

### `farmers_structures:undergardens_sea_with_all`

Source: FarmersStructures-1.0.0-1.19.2.jar. Placement: spacing 190, separation 147, frequency 0.991717, salt 664652465 (edited).

- `farmers_structures:undergardens_sea`: weight 1, frequency 1, alone: 1 per 191

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

### `minecraft:desert_pyramids`

Source: vanilla_known_defaults. Placement: spacing 27, separation 7, frequency 0.946846, salt 14357617 (edited).

- `minecraft:desert_pyramid`: weight 1, frequency 1, alone: 1 per 28, **blocked:** YUNG's Better Desert Temples stops it in code (config/betterdeserttemples-forge-1_19.toml: "Disable Vanilla Pyramids" = true). Tuning this set has no effect.

### `minecraft:jungle_temples`

Source: vanilla_known_defaults. Placement: spacing 27, separation 7, frequency 0.946846, salt 14357619 (edited).

- `minecraft:jungle_pyramid`: weight 1, frequency 1, alone: 1 per 28, **blocked:** YUNG's Better Jungle Temples stops it in code (config/betterjungletemples-forge-1_19_2.toml: "Disable Vanilla Jungle Temples" = true). Tuning this set has no effect.

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

### `minecraft:swamp_huts`

Source: vanilla_known_defaults. Placement: spacing 24, separation 6, frequency 0.995006, salt 14357620 (edited).

- `minecraft:swamp_hut`: weight 1, frequency 1, alone: 1 per 24, **blocked:** YUNG's Better Witch Huts stops it in code (config/betterwitchhuts-forge-1_19.toml: "Disable Vanilla Witch Huts" = true). Tuning this set has no effect.

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

### `philipsruins:nether_structures`

Source: PhilipsRuins1.19.2-10.8.jar. Placement: spacing 21, separation 18, frequency 0.98, salt 785892126 (edited).

- `philipsruins:start_nether_dungeon`: weight 1, frequency 1, alone: 1 per 21
- `philipsruins:sealedruinedghaststatue1`: weight 1, frequency 1, alone: 1 per 21
- `philipsruins:nether_lava_ruins`: weight 1, frequency 1, alone: 1 per 21

### `philipsruins:underground_structures`

Source: PhilipsRuins1.19.2-10.8.jar. Placement: spacing 23, separation 13, frequency 1, salt 641781942 (edited).

- `philipsruins:lost_soul_dungeon`: weight 86, frequency 0.991, alone: 1 per 23
- `philipsruins:bone_dungeon`: weight 116, frequency 0.2939, alone: 1 per 42
- `philipsruins:underground_structures`: weight 582, frequency 0.9885, alone: 1 per 23
- `philipsruins:sculk_dungeon`: weight 86, frequency 0.991, alone: 1 per 23

### `supplementaries:way_signs`

Source: supplementaries-1.19.2-2.4.20.jar. Placement: spacing 19, separation 10, frequency 1, salt 431041527, exclusion_zone creativeengineering:group/settlements r8 (edited).

- `supplementaries:way_sign`: weight 1, frequency 1, alone: 1 per 19

### `terralith:underground`

Source: Terralith_1.19.3_v2.3.12.jar. Placement: spacing 3, separation 1, frequency 1, salt 69696969 (edited).

- `terralith:underground/oak_cabin`: weight 50, frequency 1, alone: 1 per 3
- `terralith:underground/giant_bee_hive`: weight 78, frequency 0.4261, alone: 1 per 5
- `terralith:underground/mining_outpost`: weight 713, frequency 0.1097, alone: 1 per 9
- `terralith:underground/old_refinery`: weight 258, frequency 0.1166, alone: 1 per 9
- `terralith:underground/sunken_tower`: weight 129, frequency 0.4277, alone: 1 per 5
- `terralith:underground/witch_hut`: weight 44, frequency 1, alone: 1 per 3

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
- `betterdeserttemples:desert_temples`: `betterdeserttemples:desert_temple` -> surface_dungeons
- `betterjungletemples:jungle_temples`: `betterjungletemples:jungle_temple` -> surface_dungeons
- `betterwitchhuts:witch_circle`: `betterwitchhuts:witch_circle` -> surface_dungeons
- `betterwitchhuts:witch_hut`: `betterwitchhuts:witch_hut` -> surface_dungeons
- `bighorsestable:big_horse_stable`: `bighorsestable:big_horse_stable` -> remote
- `blocky_siege:pillager_ship`: `blocky_siege:pillager_ship` -> boats
- `cloudstorage:big_balloon`: `cloudstorage:big_balloon` -> sky
- `cloudstorage:sky_temple`: `cloudstorage:sky_temple` -> sky
- `desert_village_temple:village_temple`: `desert_village_temple:village_temple` -> remote
- `desert_watchtower:watchtower`: `desert_watchtower:watchtower` -> remote
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
- `hopo:underwater_ruins`: `hopo:underwater_ruins` -> ocean_ruins
- `hopo:underwater_small_ruins`: `hopo:underwater_small_ruins` -> ocean_ruins
- `hopo:underwater_warm_small_ruins`: `hopo:underwater_warm_small_ruins` -> ocean_ruins
- `illager_siege_weapons:ballista`: `illager_siege_weapons:ballista` -> pillager_settlements
- `illager_siege_weapons:battering_ram`: `illager_siege_weapons:battering_ram` -> pillager_settlements
- `illager_siege_weapons:catapult`: `illager_siege_weapons:catapult` -> pillager_settlements
- `illager_siege_weapons:reinforced_battering_ram`: `illager_siege_weapons:reinforced_battering_ram` -> pillager_settlements
- `illager_siege_weapons:siege_tower`: `illager_siege_weapons:siege_tower` -> pillager_settlements
- `illager_siege_weapons:trebuchet`: `illager_siege_weapons:trebuchet` -> pillager_settlements
- `illager_viking_longship:illager_longship`: `illager_viking_longship:illager_longship` -> boats
- `illagerwarship:warship`: `illagerwarship:warship` -> boats
- `llama_stable:llama_stable`: `llama_stable:llama_stable` -> remote
- `minecraft:igloos`: `minecraft:igloo` -> remote
- `minecraft:ocean_monuments`: `minecraft:monument` -> ocean_dungeons
- `minecraft:ocean_ruins`: `minecraft:ocean_ruin_cold` -> ocean_ruins, `minecraft:ocean_ruin_warm` -> ocean_ruins
- `minecraft:pillager_outposts`: `minecraft:pillager_outpost` -> pillager_settlements
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
- `old_swamp_fisherman_house:fisherman_house`: `old_swamp_fisherman_house:fisherman_house` -> remote
- `philipsruins:ancient_ruins`: `philipsruins:ancient_ruins` -> ruins
- `philipsruins:ancient_towers`: `philipsruins:ancient_towers` -> ruins
- `philipsruins:desert_structures`: `philipsruins:badlands_start_dungeon` -> surface_dungeons, `philipsruins:badlands_structures` -> ruins, `philipsruins:desert_structures` -> ruins
- `philipsruins:field_stone_ruins`: `philipsruins:field_stone_ruins` -> ruins
- `philipsruins:field_stone_ruins_rocks`: `philipsruins:field_stone_ruins_rocks` -> ruins
- `philipsruins:level_one_ruins`: `philipsruins:level_one_ruins` -> ruins
- `philipsruins:level_three_ruins`: `philipsruins:level_three_ruins` -> ruins
- `philipsruins:level_two_ruins`: `philipsruins:level_two_ruins` -> ruins, `philipsruins:level_two_ruins_pool` -> ruins
- `philipsruins:nether_ruins`: `philipsruins:start_nether_ruin` -> ruins
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
- `small_viking_villager_house:small_viking_house`: `small_viking_villager_house:small_viking_house` -> remote
- `small_villager_greenhouse:small_villager_greenhouse`: `small_villager_greenhouse:small_villager_greenhouse` -> remote
- `smallhorsestable:horse_stable`: `smallhorsestable:horse_stable` -> remote
- `snowy_tents:snowy_tent`: `snowy_tents:snowy_tent` -> remote
- `terralith:rare_dungeon`: `terralith:spire` -> surface_dungeons
- `terralith:rare_village`: `terralith:fortified_village` -> settlements, `terralith:fortified_desert_village` -> settlements
- `terralith:ruin`: `terralith:glacial_hut` -> ruins
- `towns_and_towers:other`: `towns_and_towers:mimic_desert` -> surface_dungeons, `towns_and_towers:wreckage_ocean` -> ocean_ruins
- `towns_and_towers:towers`: `towns_and_towers:pillager_outpost_badlands` -> pillager_settlements, `towns_and_towers:pillager_outpost_beach` -> pillager_settlements, `towns_and_towers:pillager_outpost_birch_forest` -> pillager_settlements, `towns_and_towers:pillager_outpost_desert` -> pillager_settlements, `towns_and_towers:pillager_outpost_flower_forest` -> pillager_settlements, `towns_and_towers:pillager_outpost_forest` -> pillager_settlements, `towns_and_towers:pillager_outpost_grove` -> pillager_settlements, `towns_and_towers:pillager_outpost_jungle` -> pillager_settlements, `towns_and_towers:pillager_outpost_meadow` -> pillager_settlements, `towns_and_towers:pillager_outpost_mushroom_fields` -> pillager_settlements, `towns_and_towers:pillager_outpost_ocean` -> boats, `towns_and_towers:pillager_outpost_old_growth_taiga` -> pillager_settlements, `towns_and_towers:pillager_outpost_savanna` -> pillager_settlements, `towns_and_towers:pillager_outpost_snowy_beach` -> pillager_settlements, `towns_and_towers:pillager_outpost_snowy_plains` -> pillager_settlements, `towns_and_towers:pillager_outpost_snowy_slopes` -> pillager_settlements, `towns_and_towers:pillager_outpost_sparse_jungle` -> pillager_settlements, `towns_and_towers:pillager_outpost_sunflower_plains` -> pillager_settlements, `towns_and_towers:pillager_outpost_swamp` -> pillager_settlements, `towns_and_towers:pillager_outpost_taiga` -> pillager_settlements, `towns_and_towers:pillager_outpost_snowy_taiga` -> pillager_settlements, `towns_and_towers:pillager_outpost_wooded_badlands` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_mediterranean` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_tudor` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_classic` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_oriental` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_swedish` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_iberian` -> pillager_settlements, `towns_and_towers:exclusives/pillager_outpost_rustic` -> pillager_settlements
- `towns_and_towers:towns`: `towns_and_towers:village_badlands` -> settlements, `towns_and_towers:village_beach` -> settlements, `towns_and_towers:village_birch_forest` -> settlements, `towns_and_towers:village_flower_forest` -> settlements, `towns_and_towers:village_forest` -> settlements, `towns_and_towers:village_snowy_slopes` -> settlements, `towns_and_towers:village_jungle` -> settlements, `towns_and_towers:village_meadow` -> settlements, `towns_and_towers:village_mushroom_fields` -> settlements, `towns_and_towers:village_ocean` -> boats, `towns_and_towers:village_old_growth_taiga` -> settlements, `towns_and_towers:village_grove` -> settlements, `towns_and_towers:village_sunflower_plains` -> settlements, `towns_and_towers:village_snowy_taiga` -> settlements, `towns_and_towers:village_sparse_jungle` -> settlements, `towns_and_towers:village_swamp` -> settlements, `towns_and_towers:village_wooded_badlands` -> settlements, `towns_and_towers:exclusives/village_mediterranean` -> settlements, `towns_and_towers:exclusives/village_swedish` -> settlements, `towns_and_towers:exclusives/village_tudor` -> settlements, `towns_and_towers:exclusives/village_wandering_trader_camp` -> remote, `towns_and_towers:exclusives/village_classic` -> settlements, `towns_and_towers:exclusives/village_rustic` -> settlements, `towns_and_towers:exclusives/village_iberian` -> settlements, `towns_and_towers:exclusives/village_nilotic` -> settlements
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
- `villagesandpillages:village_witch`: `villagesandpillages:village_witch` -> surface_dungeons
- `watchtower_building:abandoned_watchtower`: `watchtower_building:abandoned_watchtower` -> ruins

## Coverage

- Structures with no `type/*` or `dungeon/*` tag: `takesapillage:bastille`, `takesapillage:pillager_camp`
- Structures with no `mod/*` tag: `takesapillage:bastille`, `takesapillage:pillager_camp`
- Structures another mod stops in code (tuning them has no effect, see "blocked" in structure_config.json): `betterdungeons:small_nether_dungeon`, `minecraft:desert_pyramid`, `minecraft:jungle_pyramid`, `minecraft:fortress`, `minecraft:stronghold`, `minecraft:swamp_hut`
- Structures in no group (82, standalone): `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterfortresses:fortress`, `betterstrongholds:stronghold`, `farmers_structures:aethers`, `farmers_structures:cooks_nether_1`, `farmers_structures:cooks_nether_2`, `farmers_structures:undergardens_sea`, `hearths:netherrack_spiral`, `hearths:crimson_tower`, `hearths:warped_tower`, `hopo:underwater_fossils`, `mes:astral_hideaway`, `mes:astral_meteorite`, `mes:ender_spire`, `mes:enderbloom_grove`, `mes:enderkeep_courtyard`, `mes:enderpin_spikes`, `mes:enderskog`, `mes:enderwatch_tower`, `mes:endscraps`, `mes:manuscript_shrine`, `mes:monolith`, `mes:mythic_garden`, `mes:phantom_citadel`, `mes:placid_prairie`, `mes:ruined_pillar`, `mes:starlight_voyager`, `minecraft:ancient_city`, `minecraft:buried_treasure`, `minecraft:desert_pyramid`, `minecraft:end_city`, `minecraft:jungle_pyramid`, `minecraft:mineshaft`, `minecraft:mineshaft_mesa`, `minecraft:fortress`, `minecraft:bastion_remnant`, `minecraft:nether_fossil`, `minecraft:ruined_portal`, `minecraft:ruined_portal_desert`, `minecraft:ruined_portal_jungle`, `minecraft:ruined_portal_swamp`, `minecraft:ruined_portal_mountain`, `minecraft:ruined_portal_ocean`, `minecraft:ruined_portal_nether`, `minecraft:stronghold`, `minecraft:swamp_hut`, `nullscape:dragon_skeleton`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:end_ruins`, `philipsruins:end_gate_fortress`, `philipsruins:lost_soul_city`, `philipsruins:start_nether_dungeon`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:nether_lava_ruins`, `philipsruins:lost_soul_dungeon`, `philipsruins:bone_dungeon`, `philipsruins:underground_structures`, `philipsruins:sculk_dungeon`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `supplementaries:way_sign`, `takesapillage:bastille`, `takesapillage:pillager_camp`, `terralith:underground/oak_cabin`, `terralith:underground/giant_bee_hive`, `terralith:underground/mining_outpost`, `terralith:underground/old_refinery`, `terralith:underground/sunken_tower`, `terralith:underground/witch_hut`, `terralith:underground/frosted_dungeon`, `twigs:bloodstone_obelisk`, `unwrecked_ships:nether_ship`, `watchtower_building:fresh_watchtower`

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
- **`mod/villager_buildings`** 16: `ancient_enchantment_temple:temple`, `animal_barn:barn`, `bighorsestable:big_horse_stable`, `desert_village_temple:village_temple`, `desert_watchtower:watchtower`, `forest_watchtower:forest_watchtower`, `gothic_church:church`, `llama_stable:llama_stable`, `mini_clock_tower:mini_clock_tower`, `saloon:saloon`, `smallhorsestable:horse_stable`, `villager_fish_shop:fish_shop`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_forge:villager_forge`, `villager_gold_washing_facility:villager_gold_washing_facility`, `villager_windmill:windmill`
- **`mod/villager_camps`** 4: `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`
- **`type/village`** 32: `minecraft:village_desert`, `minecraft:village_plains`, `minecraft:village_savanna`, `minecraft:village_snowy`, `minecraft:village_taiga`, `terralith:fortified_desert_village`, `terralith:fortified_village`, `towns_and_towers:exclusives/village_classic`, `towns_and_towers:exclusives/village_iberian`, `towns_and_towers:exclusives/village_mediterranean`, `towns_and_towers:exclusives/village_nilotic`, `towns_and_towers:exclusives/village_rustic`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `towns_and_towers:village_badlands`, `towns_and_towers:village_beach`, `towns_and_towers:village_birch_forest`, `towns_and_towers:village_flower_forest`, `towns_and_towers:village_forest`, `towns_and_towers:village_grove`, `towns_and_towers:village_jungle`, `towns_and_towers:village_meadow`, `towns_and_towers:village_mushroom_fields`, `towns_and_towers:village_ocean`, `towns_and_towers:village_old_growth_taiga`, `towns_and_towers:village_snowy_slopes`, `towns_and_towers:village_snowy_taiga`, `towns_and_towers:village_sparse_jungle`, `towns_and_towers:village_sunflower_plains`, `towns_and_towers:village_swamp`, `towns_and_towers:village_wooded_badlands`
- **`type/house`** 56: `ancient_enchantment_temple:temple`, `animal_barn:barn`, `beautify:botanist_house_desert`, `beautify:botanist_house_plains`, `beautify:botanist_house_savanna`, `beautify:botanist_house_snowy`, `beautify:botanist_house_taiga`, `bighorsestable:big_horse_stable`, `desert_village_temple:village_temple`, `desert_watchtower:watchtower`, `farmers_structures:aethers`, `farmers_structures:aquacultures_beach`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `farmers_structures:cooks_moss`, `farmers_structures:cooks_nether_1`, `farmers_structures:cooks_nether_2`, `farmers_structures:sushis`, `farmers_structures:undergardens_sea`, `forest_watchtower:forest_watchtower`, `gothic_church:church`, `greek_villager_house:greek_house`, `hobbit_house_:hobbit_house`, `llama_stable:llama_stable`, `minecraft:igloo`, `mini_clock_tower:mini_clock_tower`, `old_swamp_fisherman_house:fisherman_house`, `saloon:saloon`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_cleric_house:villager_cleric_house`, `villager_country_house:country_house`, `villager_farmer_house:villager_farmer_house`, `villager_fish_shop:fish_shop`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_flower_house:flower_house`, `villager_forge:villager_forge`, `villager_gold_washing_facility:villager_gold_washing_facility`, `villager_tannery:tannery`, `villager_windmill:windmill`
- **`type/camp`** 15: `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `plains_villager_tents:big_villager_tent`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_wooded_badlands`, `villager_archer_camp:villager_archer_camp`, `villager_circus_tent:villager_circus_tent`
- **`type/fort`** 9: `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_old_growth_taiga`
- **`type/tower`** 30: `hearths:crimson_tower`, `hearths:warped_tower`, `minecraft:pillager_outpost`, `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`, `philipsruins:ancient_towers`, `terralith:spire`, `terralith:underground/sunken_tower`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`, `watchtower_building:fresh_watchtower`
- **`type/ship`** 12: `antiquetradingship:antique_trading_ship`, `blocky_siege:pillager_ship`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `mes:starlight_voyager`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:village_ocean`, `unwrecked_ships:nether_ship`, `unwrecked_ships:ship`, `villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`
- **`type/enemy_building`** 52: `betterwitchhuts:witch_hut`, `blocky_siege:pillager_ship`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `minecraft:mansion`, `minecraft:pillager_outpost`, `minecraft:swamp_hut`, `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`, `terralith:underground/witch_hut`, `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:pillager_outpost_wooded_badlands`, `villagesandpillages:village_witch`
- **`type/ruin`** 26: `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:lost_soul_city`, `philipsruins:pumpkin_ruins`, `philipsruins:rare_ruin`, `philipsruins:start_nether_ruin`, `philipsruins:underground_structures`, `terralith:glacial_hut`, `terralith:underground/oak_cabin`, `watchtower_building:abandoned_watchtower`
- **`type/ruined_portal`** 7: `minecraft:ruined_portal`, `minecraft:ruined_portal_desert`, `minecraft:ruined_portal_jungle`, `minecraft:ruined_portal_mountain`, `minecraft:ruined_portal_nether`, `minecraft:ruined_portal_ocean`, `minecraft:ruined_portal_swamp`
- **`type/underwater_ruin`** 7: `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`
- **`type/shipwreck`** 3: `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`, `towns_and_towers:wreckage_ocean`
- **`type/mineshaft`** 3: `minecraft:mineshaft`, `minecraft:mineshaft_mesa`, `terralith:underground/mining_outpost`
- **`type/underground`** 9: `minecraft:buried_treasure`, `philipsruins:lost_soul_city`, `philipsruins:underground_structures`, `terralith:underground/giant_bee_hive`, `terralith:underground/mining_outpost`, `terralith:underground/oak_cabin`, `terralith:underground/old_refinery`, `terralith:underground/sunken_tower`, `terralith:underground/witch_hut`
- **`type/environmental`** 3: `ae2:meteorite`, `hopo:underwater_fossils`, `terralith:underground/giant_bee_hive`
- **`type/decoration`** 14: `hearths:netherrack_spiral`, `mes:ender_spire`, `mes:enderpin_spikes`, `mes:endscraps`, `mes:ruined_pillar`, `minecraft:nether_fossil`, `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue`, `philipsruins:field_stone_ruins_rocks`, `small_cat_statues:cat_1`, `small_cat_statues:cat_2`, `small_cat_statues:cat_3`, `supplementaries:way_sign`, `twigs:bloodstone_obelisk`
- **`type/sky`** 3: `cloudstorage:big_balloon`, `cloudstorage:sky_temple`, `skyvillages:skyvillage`
- **`type/nether_environmental`** 2: `hearths:netherrack_spiral`, `minecraft:nether_fossil`
- **`type/nether_constructed`** 13: `betterdungeons:small_nether_dungeon`, `betterfortresses:fortress`, `farmers_structures:cooks_nether_1`, `farmers_structures:cooks_nether_2`, `hearths:crimson_tower`, `hearths:warped_tower`, `minecraft:bastion_remnant`, `minecraft:fortress`, `minecraft:ruined_portal_nether`, `philipsruins:nether_lava_ruins`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_dungeon`, `twigs:bloodstone_obelisk`
- **`type/end_environmental`** 5: `mes:astral_meteorite`, `mes:enderbloom_grove`, `mes:enderskog`, `mes:placid_prairie`, `nullscape:dragon_skeleton`
- **`type/end_constructed`** 15: `mes:astral_hideaway`, `mes:ender_spire`, `mes:enderkeep_courtyard`, `mes:enderpin_spikes`, `mes:enderwatch_tower`, `mes:endscraps`, `mes:manuscript_shrine`, `mes:monolith`, `mes:mythic_garden`, `mes:phantom_citadel`, `mes:ruined_pillar`, `mes:starlight_voyager`, `minecraft:end_city`, `philipsruins:end_gate_fortress`, `philipsruins:end_ruins`
- **`dungeon/surface_vanilla`** 4: `minecraft:desert_pyramid`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:swamp_hut`
- **`dungeon/surface_better`** 4: `betterdeserttemples:desert_temple`, `betterjungletemples:jungle_temple`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`
- **`dungeon/surface_modded`** 5: `cloudstorage:sky_temple`, `philipsruins:badlands_start_dungeon`, `terralith:spire`, `towns_and_towers:mimic_desert`, `villagesandpillages:village_witch`
- **`dungeon/underground_vanilla`** 2: `minecraft:ancient_city`, `minecraft:stronghold`
- **`dungeon/underground_better`** 5: `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterstrongholds:stronghold`
- **`dungeon/underground_modded`** 7: `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_dungeon`, `philipsruins:sculk_dungeon`, `terralith:underground/frosted_dungeon`
- **`dungeon/surface`** 13: `betterdeserttemples:desert_temple`, `betterjungletemples:jungle_temple`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `cloudstorage:sky_temple`, `minecraft:desert_pyramid`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:swamp_hut`, `philipsruins:badlands_start_dungeon`, `terralith:spire`, `towns_and_towers:mimic_desert`, `villagesandpillages:village_witch`
- **`dungeon/underground`** 14: `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterstrongholds:stronghold`, `minecraft:ancient_city`, `minecraft:stronghold`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_dungeon`, `philipsruins:sculk_dungeon`, `terralith:underground/frosted_dungeon`
- **`dungeon/vanilla`** 10: `minecraft:ancient_city`, `minecraft:bastion_remnant`, `minecraft:desert_pyramid`, `minecraft:end_city`, `minecraft:fortress`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:monument`, `minecraft:stronghold`, `minecraft:swamp_hut`
- **`dungeon/better`** 11: `betterdeserttemples:desert_temple`, `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterfortresses:fortress`, `betterjungletemples:jungle_temple`, `betterstrongholds:stronghold`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`
- **`dungeon/modded`** 13: `cloudstorage:sky_temple`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:badlands_start_dungeon`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_dungeon`, `philipsruins:sculk_dungeon`, `philipsruins:start_nether_dungeon`, `terralith:spire`, `terralith:underground/frosted_dungeon`, `towns_and_towers:mimic_desert`, `villagesandpillages:village_witch`
- **`dungeon/classic`** 21: `betterdeserttemples:desert_temple`, `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterfortresses:fortress`, `betterjungletemples:jungle_temple`, `betterstrongholds:stronghold`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `minecraft:ancient_city`, `minecraft:bastion_remnant`, `minecraft:desert_pyramid`, `minecraft:end_city`, `minecraft:fortress`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:monument`, `minecraft:stronghold`, `minecraft:swamp_hut`
- **`dungeons`** 34: `betterdeserttemples:desert_temple`, `betterdungeons:skeleton_dungeon`, `betterdungeons:small_dungeon`, `betterdungeons:small_nether_dungeon`, `betterdungeons:spider_dungeon`, `betterdungeons:zombie_dungeon`, `betterfortresses:fortress`, `betterjungletemples:jungle_temple`, `betterstrongholds:stronghold`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `cloudstorage:sky_temple`, `minecraft:ancient_city`, `minecraft:bastion_remnant`, `minecraft:desert_pyramid`, `minecraft:end_city`, `minecraft:fortress`, `minecraft:jungle_pyramid`, `minecraft:mansion`, `minecraft:monument`, `minecraft:stronghold`, `minecraft:swamp_hut`, `philipsruins:ancient_crypt`, `philipsruins:ancient_dungeon`, `philipsruins:antiquus_crypta`, `philipsruins:badlands_start_dungeon`, `philipsruins:bone_dungeon`, `philipsruins:lost_soul_dungeon`, `philipsruins:sculk_dungeon`, `philipsruins:start_nether_dungeon`, `terralith:spire`, `terralith:underground/frosted_dungeon`, `towns_and_towers:mimic_desert`, `villagesandpillages:village_witch`
- **`pillager`** 48: `blocky_siege:pillager_ship`, `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `minecraft:mansion`, `minecraft:pillager_outpost`, `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`, `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:pillager_outpost_wooded_badlands`
- **`nether`** 15: `betterdungeons:small_nether_dungeon`, `betterfortresses:fortress`, `farmers_structures:cooks_nether_1`, `farmers_structures:cooks_nether_2`, `hearths:crimson_tower`, `hearths:netherrack_spiral`, `hearths:warped_tower`, `minecraft:bastion_remnant`, `minecraft:fortress`, `minecraft:nether_fossil`, `minecraft:ruined_portal_nether`, `philipsruins:nether_lava_ruins`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_dungeon`, `twigs:bloodstone_obelisk`
- **`end`** 20: `mes:astral_hideaway`, `mes:astral_meteorite`, `mes:ender_spire`, `mes:enderbloom_grove`, `mes:enderkeep_courtyard`, `mes:enderpin_spikes`, `mes:enderskog`, `mes:enderwatch_tower`, `mes:endscraps`, `mes:manuscript_shrine`, `mes:monolith`, `mes:mythic_garden`, `mes:phantom_citadel`, `mes:placid_prairie`, `mes:ruined_pillar`, `mes:starlight_voyager`, `minecraft:end_city`, `nullscape:dragon_skeleton`, `philipsruins:end_gate_fortress`, `philipsruins:end_ruins`
- **`ruins`** 45: `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`, `mes:endscraps`, `mes:ruined_pillar`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `minecraft:ruined_portal`, `minecraft:ruined_portal_desert`, `minecraft:ruined_portal_jungle`, `minecraft:ruined_portal_mountain`, `minecraft:ruined_portal_nether`, `minecraft:ruined_portal_ocean`, `minecraft:ruined_portal_swamp`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:end_ruins`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:lost_soul_city`, `philipsruins:nether_lava_ruins`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`, `philipsruins:pumpkin_ruins`, `philipsruins:rare_ruin`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_ruin`, `philipsruins:underground_structures`, `terralith:glacial_hut`, `terralith:underground/oak_cabin`, `watchtower_building:abandoned_watchtower`
- **`group/settlements`** 30: `minecraft:village_desert`, `minecraft:village_plains`, `minecraft:village_savanna`, `minecraft:village_snowy`, `minecraft:village_taiga`, `terralith:fortified_desert_village`, `terralith:fortified_village`, `towns_and_towers:exclusives/village_classic`, `towns_and_towers:exclusives/village_iberian`, `towns_and_towers:exclusives/village_mediterranean`, `towns_and_towers:exclusives/village_nilotic`, `towns_and_towers:exclusives/village_rustic`, `towns_and_towers:exclusives/village_swedish`, `towns_and_towers:exclusives/village_tudor`, `towns_and_towers:village_badlands`, `towns_and_towers:village_beach`, `towns_and_towers:village_birch_forest`, `towns_and_towers:village_flower_forest`, `towns_and_towers:village_forest`, `towns_and_towers:village_grove`, `towns_and_towers:village_jungle`, `towns_and_towers:village_meadow`, `towns_and_towers:village_mushroom_fields`, `towns_and_towers:village_old_growth_taiga`, `towns_and_towers:village_snowy_slopes`, `towns_and_towers:village_snowy_taiga`, `towns_and_towers:village_sparse_jungle`, `towns_and_towers:village_sunflower_plains`, `towns_and_towers:village_swamp`, `towns_and_towers:village_wooded_badlands`
- **`group/pillager_settlements`** 43: `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`, `minecraft:pillager_outpost`, `mysticrift_pillagers_towers_aris:acacia_pillager_tower`, `mysticrift_pillagers_towers_aris:birch_pillager_tower`, `mysticrift_pillagers_towers_aris:cherry_pillager_tower`, `mysticrift_pillagers_towers_aris:desert_pillager_tower`, `mysticrift_pillagers_towers_aris:end_pillager_tower`, `mysticrift_pillagers_towers_aris:jungle_pillager_tower`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`, `towns_and_towers:exclusives/pillager_outpost_classic`, `towns_and_towers:exclusives/pillager_outpost_iberian`, `towns_and_towers:exclusives/pillager_outpost_mediterranean`, `towns_and_towers:exclusives/pillager_outpost_oriental`, `towns_and_towers:exclusives/pillager_outpost_rustic`, `towns_and_towers:exclusives/pillager_outpost_swedish`, `towns_and_towers:exclusives/pillager_outpost_tudor`, `towns_and_towers:pillager_outpost_badlands`, `towns_and_towers:pillager_outpost_beach`, `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_desert`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`, `towns_and_towers:pillager_outpost_grove`, `towns_and_towers:pillager_outpost_jungle`, `towns_and_towers:pillager_outpost_meadow`, `towns_and_towers:pillager_outpost_mushroom_fields`, `towns_and_towers:pillager_outpost_old_growth_taiga`, `towns_and_towers:pillager_outpost_savanna`, `towns_and_towers:pillager_outpost_snowy_beach`, `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`, `towns_and_towers:pillager_outpost_sparse_jungle`, `towns_and_towers:pillager_outpost_sunflower_plains`, `towns_and_towers:pillager_outpost_swamp`, `towns_and_towers:pillager_outpost_taiga`, `towns_and_towers:pillager_outpost_wooded_badlands`
- **`group/ruins`** 23: `forest_ruins:ruin_1`, `forest_ruins:ruin_2`, `forest_ruins:ruin_3`, `forest_ruins:ruin_4`, `forest_ruins:ruin_5`, `forest_ruins:ruin_6`, `forest_ruins:ruin_7`, `forgotten_church:forgotten_church`, `philipsruins:ancient_ruins`, `philipsruins:ancient_towers`, `philipsruins:badlands_structures`, `philipsruins:desert_structures`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`, `philipsruins:rare_ruin`, `philipsruins:start_nether_ruin`, `terralith:glacial_hut`, `watchtower_building:abandoned_watchtower`
- **`group/surface_dungeons`** 9: `betterdeserttemples:desert_temple`, `betterjungletemples:jungle_temple`, `betterwitchhuts:witch_circle`, `betterwitchhuts:witch_hut`, `minecraft:mansion`, `philipsruins:badlands_start_dungeon`, `terralith:spire`, `towns_and_towers:mimic_desert`, `villagesandpillages:village_witch`
- **`group/ocean_ruins`** 10: `hopo:underwater_ruins`, `hopo:underwater_small_ruins`, `hopo:underwater_warm_small_ruins`, `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`, `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`, `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`, `towns_and_towers:wreckage_ocean`
- **`group/ocean_dungeons`** 1: `minecraft:monument`
- **`group/sky`** 4: `ae2:meteorite`, `cloudstorage:big_balloon`, `cloudstorage:sky_temple`, `skyvillages:skyvillage`
- **`group/boats`** 10: `antiquetradingship:antique_trading_ship`, `blocky_siege:pillager_ship`, `illager_viking_longship:illager_longship`, `illagerwarship:warship`, `towns_and_towers:pillager_outpost_ocean`, `towns_and_towers:village_ocean`, `unwrecked_ships:ship`, `villager_rafts:raft_1`, `villager_rafts:raft_2`, `villager_rafts:raft_3`
- **`group/remote`** 57: `ancient_enchantment_temple:temple`, `animal_barn:barn`, `beautify:botanist_house_desert`, `beautify:botanist_house_plains`, `beautify:botanist_house_savanna`, `beautify:botanist_house_snowy`, `beautify:botanist_house_taiga`, `bighorsestable:big_horse_stable`, `desert_village_temple:village_temple`, `desert_watchtower:watchtower`, `farmers_structures:aquacultures_beach`, `farmers_structures:aquacultures_carp`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`, `farmers_structures:cooks_moss`, `farmers_structures:sushis`, `forest_watchtower:forest_watchtower`, `gothic_church:church`, `greek_villager_house:greek_house`, `hobbit_house_:hobbit_house`, `llama_stable:llama_stable`, `minecraft:igloo`, `mini_clock_tower:mini_clock_tower`, `old_swamp_fisherman_house:fisherman_house`, `plains_villager_tents:big_villager_tent`, `saloon:saloon`, `sawmillhouse:sawmill`, `simply_houses:forest_chapel`, `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`, `simply_houses:plains_house`, `simply_houses:plains_mansion`, `simply_houses:taiga_forge`, `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`, `small_viking_villager_house:small_viking_house`, `small_villager_greenhouse:small_villager_greenhouse`, `smallhorsestable:horse_stable`, `snowy_tents:snowy_tent`, `towns_and_towers:exclusives/village_wandering_trader_camp`, `villager_archer_camp:villager_archer_camp`, `villager_butcher_house:butcher_house`, `villager_cartographer_house:villager_cartographer_house`, `villager_circus_tent:villager_circus_tent`, `villager_cleric_house:villager_cleric_house`, `villager_country_house:country_house`, `villager_farmer_house:villager_farmer_house`, `villager_fish_shop:fish_shop`, `villager_fishermans_hut:villager_fishermans_hut`, `villager_fletcher_house:villager_fletcher_house`, `villager_flower_house:flower_house`, `villager_forge:villager_forge`, `villager_gold_washing_facility:villager_gold_washing_facility`, `villager_tannery:tannery`, `villager_windmill:windmill`

