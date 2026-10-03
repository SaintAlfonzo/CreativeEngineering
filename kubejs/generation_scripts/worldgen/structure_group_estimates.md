# Structure group estimates

Written by `python3 tools/gen_structure_groups.py`. Do not edit. Expected structures per area for every structure, from `structure_groups.json`, each structure's biome pool and the real biome areas in `biome_config.json`. "1 per N" means one structure per N x N chunks, averaged over the biomes it can spawn in (so a structure limited to rare biomes is still rare overall). The game itself writes `structure_tag_report.md`; this file is the offline estimate.

Compared against: **mod defaults**. Ratio = new / that; 1.00 = unchanged.

## Groups

### `settlements` -> `creativeengineering:group/settlements`

Friendly settlements: vanilla and Towns & Towers villages, Terralith fortified villages. Meta structures join later.

Grid: spacing 48, separation 12 (members never start closer than 13 chunks = 208 blocks apart). At most 1 structure per 48 x 48 chunks anywhere.

Capped (old combined density above the group grid): 46 biome(s), 54% of the group's area. Largest: `minecraft:plains` 1 per 24 -> 49, `terralith:brushland` 1 per 22 -> 51, `minecraft:savanna` 1 per 28 -> 53, `terralith:forested_highlands` 1 per 22 -> 62, `terralith:rocky_mountains` 1 per 25 -> 61, `minecraft:desert` 1 per 24 -> 55.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `minecraft:village_desert` | 424 | 0.6975 | 28 | 61 | 0.21 |  |
| `minecraft:village_plains` | 162 | 0.9263 | 28 | 63 | 0.19 |  |
| `minecraft:village_savanna` | 176 | 0.8134 | 28 | 61 | 0.21 |  |
| `minecraft:village_snowy` | 132 | 1.0 | 29 | 59 | 0.24 |  |
| `minecraft:village_taiga` | 153 | 0.9436 | 29 | 65 | 0.20 |  |
| `terralith:fortified_desert_village` | 108 | 1.0 | 46 | 107 | 0.19 |  |
| `terralith:fortified_village` | 53 | 1.0 | 46 | 109 | 0.18 |  |
| `towns_and_towers:exclusives/village_classic` | 101 | 0.1623 | 53 | 119 | 0.20 |  |
| `towns_and_towers:exclusives/village_iberian` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/village_mediterranean` | 41 | 1.0 | 53 | 119 | 0.20 |  |
| `towns_and_towers:exclusives/village_nilotic` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/village_rustic` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/village_swedish` | 22 | 1.0 | 92 | 211 | 0.19 |  |
| `towns_and_towers:exclusives/village_tudor` | 99 | 0.4132 | 53 | 119 | 0.20 |  |
| `towns_and_towers:village_badlands` | 173 | 0.2113 | 57 | 128 | 0.20 |  |
| `towns_and_towers:village_beach` | 101 | 0.1623 | 53 | 119 | 0.20 |  |
| `towns_and_towers:village_birch_forest` | 162 | 0.197 | 62 | 134 | 0.21 |  |
| `towns_and_towers:village_flower_forest` | 360 | 0.1637 | 54 | 120 | 0.20 |  |
| `towns_and_towers:village_forest` | 144 | 0.1749 | 55 | 122 | 0.20 |  |
| `towns_and_towers:village_grove` | 213 | 0.1696 | 56 | 124 | 0.20 |  |
| `towns_and_towers:village_jungle` | 46 | 0.4485 | 64 | 86 | 0.55 |  |
| `towns_and_towers:village_meadow` | 62 | 0.6944 | 53 | 119 | 0.20 |  |
| `towns_and_towers:village_mushroom_fields` | 101 | 0.1623 | 53 | 119 | 0.20 |  |
| `towns_and_towers:village_old_growth_taiga` | 110 | 0.2153 | 57 | 121 | 0.22 |  |
| `towns_and_towers:village_snowy_slopes` | 141 | 0.2799 | 53 | 115 | 0.21 |  |
| `towns_and_towers:village_snowy_taiga` | 108 | 0.1943 | 70 | 152 | 0.21 |  |
| `towns_and_towers:village_sparse_jungle` | 46 | 0.45 | 64 | 87 | 0.55 |  |
| `towns_and_towers:village_sunflower_plains` | 228 | 0.2615 | 53 | 122 | 0.19 |  |
| `towns_and_towers:village_swamp` | 142 | 0.5026 | 53 | 71 | 0.56 |  |
| `towns_and_towers:village_wooded_badlands` | 205 | 0.1738 | 62 | 136 | 0.21 |  |

### `pillager_settlements` -> `creativeengineering:group/pillager_settlements`

Pillager outposts: vanilla, and every Towns & Towers tower, fort and camp outpost except the ship (boats).

Grid: spacing 32, separation 8 (members never start closer than 9 chunks = 144 blocks apart). At most 1 structure per 32 x 32 chunks anywhere. Excluded within 4 chunks of `creativeengineering:group/settlements` grid slots.

Capped (old combined density above the group grid): 5 biome(s), 8% of the group's area. Largest: `minecraft:plains` 1 per 19 -> 34, `minecraft:savanna` 1 per 19 -> 34, `minecraft:snowy_plains` 1 per 19 -> 37, `minecraft:desert` 1 per 19 -> 38, `minecraft:taiga` 1 per 19 -> 34.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `minecraft:pillager_outpost` | 74 | 0.6336 | 121 | 78 | 2.44 |  |
| `towns_and_towers:exclusives/pillager_outpost_classic` | 80 | 0.2338 | 110 | 83 | 1.79 |  |
| `towns_and_towers:exclusives/pillager_outpost_iberian` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/pillager_outpost_mediterranean` | 78 | 0.3596 | 156 | 78 | 4.02 |  |
| `towns_and_towers:exclusives/pillager_outpost_oriental` | 65 | 1.0 | 171 | 66 | 6.71 |  |
| `towns_and_towers:exclusives/pillager_outpost_rustic` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | 31 | 1.0 | 221 | 109 | 4.07 |  |
| `towns_and_towers:exclusives/pillager_outpost_tudor` | 100 | 0.2918 | 110 | 71 | 2.39 |  |
| `towns_and_towers:pillager_outpost_badlands` | 83 | 0.3573 | 130 | 74 | 3.14 |  |
| `towns_and_towers:pillager_outpost_beach` | 151 | 0.4394 | 110 | 55 | 3.98 |  |
| `towns_and_towers:pillager_outpost_birch_forest` | 132 | 0.2977 | 132 | 78 | 2.84 |  |
| `towns_and_towers:pillager_outpost_desert` | 159 | 0.3206 | 111 | 80 | 1.90 |  |
| `towns_and_towers:pillager_outpost_flower_forest` | 252 | 0.3165 | 113 | 66 | 2.94 |  |
| `towns_and_towers:pillager_outpost_forest` | 136 | 0.2912 | 115 | 71 | 2.61 |  |
| `towns_and_towers:pillager_outpost_grove` | 172 | 0.2852 | 112 | 77 | 2.12 |  |
| `towns_and_towers:pillager_outpost_jungle` | 109 | 0.3648 | 133 | 73 | 3.26 |  |
| `towns_and_towers:pillager_outpost_meadow` | 152 | 0.4 | 110 | 66 | 2.79 |  |
| `towns_and_towers:pillager_outpost_mushroom_fields` | 100 | 0.2918 | 110 | 71 | 2.39 |  |
| `towns_and_towers:pillager_outpost_old_growth_taiga` | 108 | 0.4216 | 152 | 74 | 4.22 |  |
| `towns_and_towers:pillager_outpost_savanna` | 42 | 0.8362 | 136 | 77 | 3.14 |  |
| `towns_and_towers:pillager_outpost_snowy_beach` | 151 | 0.4394 | 110 | 55 | 3.98 |  |
| `towns_and_towers:pillager_outpost_snowy_plains` | 156 | 0.4073 | 110 | 83 | 1.77 |  |
| `towns_and_towers:pillager_outpost_snowy_slopes` | 67 | 0.6132 | 110 | 68 | 2.60 |  |
| `towns_and_towers:pillager_outpost_snowy_taiga` | 100 | 0.3106 | 154 | 88 | 3.03 |  |
| `towns_and_towers:pillager_outpost_sparse_jungle` | 110 | 0.6864 | 133 | 54 | 6.19 |  |
| `towns_and_towers:pillager_outpost_sunflower_plains` | 100 | 0.2918 | 110 | 71 | 2.39 |  |
| `towns_and_towers:pillager_outpost_swamp` | 100 | 0.2918 | 110 | 71 | 2.39 |  |
| `towns_and_towers:pillager_outpost_taiga` | 45 | 1.0 | 157 | 81 | 3.75 |  |
| `towns_and_towers:pillager_outpost_wooded_badlands` | 81 | 0.7962 | 133 | 51 | 6.67 |  |
| `illager_siege_weapons:ballista` | 57 | 1.0 | 50 | 98 | 0.26 |  |
| `illager_siege_weapons:battering_ram` | 57 | 1.0 | 50 | 98 | 0.26 |  |
| `illager_siege_weapons:catapult` | 57 | 1.0 | 50 | 98 | 0.26 |  |
| `illager_siege_weapons:reinforced_battering_ram` | 57 | 1.0 | 50 | 98 | 0.26 |  |
| `illager_siege_weapons:siege_tower` | 57 | 1.0 | 50 | 98 | 0.26 |  |
| `illager_siege_weapons:trebuchet` | 57 | 1.0 | 50 | 98 | 0.26 |  |
| `mysticrift_pillagers_towers_aris:acacia_pillager_tower` | 5 | 0.4211 | 190 | 179 | 1.13 |  |
| `mysticrift_pillagers_towers_aris:birch_pillager_tower` | 5 | 0.38 | 200 | 188 | 1.13 |  |
| `mysticrift_pillagers_towers_aris:cherry_pillager_tower` | 5 | 0.4211 | 190 | 179 | 1.13 |  |
| `mysticrift_pillagers_towers_aris:desert_pillager_tower` | 5 | 0.4692 | 180 | 169 | 1.13 |  |
| `mysticrift_pillagers_towers_aris:end_pillager_tower` | 5 | 0.4301 | 188 | 177 | 1.13 |  |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | 5 | 0.9278 | 128 | 120 | 1.13 |  |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | 5 | 0.5938 | 160 | 150 | 1.13 |  |
| `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` | 5 | 0.9425 | 127 | 119 | 1.13 |  |

### `ruins` -> `creativeengineering:group/ruins`

Surface land ruins: Philip's Ruins and Terralith's glacial hut. Underground ruins stay standalone.

Grid: spacing 8, separation 4 (members never start closer than 5 chunks = 80 blocks apart). At most 1 structure per 8 x 8 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `philipsruins:ancient_ruins` | 119 | 0.6739 | 50 | 21 | 5.51 |  |
| `philipsruins:ancient_towers` | 197 | 0.6758 | 50 | 17 | 9.16 |  |
| `philipsruins:badlands_structures` | 250 | 0.2387 | 55 | 16 | 11.35 |  |
| `philipsruins:desert_structures` | 250 | 0.2387 | 39 | 16 | 5.67 |  |
| `philipsruins:field_stone_ruins` | 45 | 0.9895 | 75 | 32 | 5.35 |  |
| `philipsruins:field_stone_ruins_rocks` | 196 | 0.322 | 68 | 23 | 8.40 |  |
| `philipsruins:level_one_ruins` | 45 | 0.9895 | 75 | 32 | 5.35 |  |
| `philipsruins:level_three_ruins` | 35 | 0.9905 | 85 | 37 | 5.36 |  |
| `philipsruins:level_two_ruins` | 20 | 0.9784 | 113 | 49 | 5.35 |  |
| `philipsruins:level_two_ruins_pool` | 20 | 0.9784 | 113 | 49 | 5.35 |  |
| `philipsruins:pumpkin_ruins` | 84 | 1.0 | 50 | 23 | 4.71 |  |
| `philipsruins:rare_ruin` | 254 | 0.0082 | 360 | 156 | 5.33 |  |
| `philipsruins:start_nether_ruin` | 3 | 0.8688 | 310 | 134 | 5.36 |  |
| `terralith:glacial_hut` | 261 | 0.249 | 27 | 16 | 2.84 |  |
| `forest_ruins:ruin_1` | 38 | 0.7313 | 45 | 46 | 0.96 |  |
| `forest_ruins:ruin_2` | 38 | 0.7313 | 45 | 46 | 0.96 |  |
| `forest_ruins:ruin_3` | 38 | 0.7313 | 45 | 46 | 0.96 |  |
| `forest_ruins:ruin_4` | 38 | 0.7313 | 45 | 46 | 0.96 |  |
| `forest_ruins:ruin_5` | 38 | 0.7313 | 45 | 46 | 0.96 |  |
| `forest_ruins:ruin_6` | 38 | 0.7313 | 45 | 46 | 0.96 |  |
| `forest_ruins:ruin_7` | 38 | 0.7313 | 45 | 46 | 0.96 |  |
| `forgotten_church:forgotten_church` | 16 | 0.9825 | 55 | 55 | 1.02 |  |
| `watchtower_building:abandoned_watchtower` | 21 | 1.0 | 48 | 49 | 0.98 |  |

### `surface_dungeons` -> `creativeengineering:group/surface_dungeons`

Surface dungeons: YUNG's temples and witch huts, the woodland mansion, Terralith's spire, the T&T desert mimic, Philip's badlands dungeon. The vanilla temples YUNG's blocks in code stay out.

Grid: spacing 16, separation 10 (members never start closer than 11 chunks = 176 blocks apart). At most 1 structure per 16 x 16 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `betterdeserttemples:desert_temple` | 97 | 0.7059 | 35 | 26 | 1.82 |  |
| `betterjungletemples:jungle_temple` | 186 | 0.7481 | 24 | 18 | 1.68 |  |
| `betterwitchhuts:witch_circle` | 62 | 1.0 | 40 | 37 | 1.16 |  |
| `betterwitchhuts:witch_hut` | 172 | 0.8021 | 30 | 24 | 1.53 |  |
| `minecraft:mansion` | 30 | 0.1191 | 80 | 46 | 2.98 |  |
| `philipsruins:badlands_start_dungeon` | 106 | 0.426 | 55 | 25 | 5.06 |  |
| `terralith:spire` | 103 | 0.415 | 36 | 25 | 2.10 |  |
| `towns_and_towers:mimic_desert` | 79 | 0.7049 | 32 | 28 | 1.27 |  |
| `villagesandpillages:village_witch` | 101 | 1.0 | 24 | 28 | 0.72 |  |

### `ocean_ruins` -> `creativeengineering:group/ocean_ruins`

Ocean ruins and wrecks: vanilla, Hopo's and Philip's ocean ruins, Philip's ocean fortress, vanilla shipwrecks, the T&T wreckage.

Grid: spacing 6, separation 2 (members never start closer than 3 chunks = 48 blocks apart). At most 1 structure per 6 x 6 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `hopo:underwater_ruins` | 113 | 1.0 | 22 | 19 | 1.37 |  |
| `hopo:underwater_small_ruins` | 513 | 0.6268 | 13 | 11 | 1.37 |  |
| `hopo:underwater_warm_small_ruins` | 560 | 0.9701 | 10 | 9 | 1.36 |  |
| `minecraft:ocean_ruin_cold` | 143 | 0.627 | 20 | 21 | 0.90 |  |
| `minecraft:ocean_ruin_warm` | 92 | 0.9743 | 20 | 21 | 0.90 |  |
| `philipsruins:ocean_fortress` | 29 | 0.5047 | 70 | 52 | 1.80 |  |
| `philipsruins:ocean_ruins` | 58 | 0.4946 | 50 | 37 | 1.80 |  |
| `towns_and_towers:wreckage_ocean` | 108 | 0.3243 | 32 | 34 | 0.90 |  |
| `revamped_shipwrecks:shipwreck` | 258 | 1.0 | 24 | 12 | 3.73 |  |
| `revamped_shipwrecks:shipwreck_beached` | 129 | 0.4444 | 24 | 9 | 7.11 |  |

### `ocean_dungeons` -> `creativeengineering:group/ocean_dungeons`

Ocean monuments, on the monuments' own grid (same spacing, spread and salt, so positions match vanilla). Monument meta structures join later.

Grid: spacing 32, separation 6 (members never start closer than 7 chunks = 112 blocks apart). At most 1 structure per 32 x 32 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `minecraft:monument` | 100 | 0.1 | 32 | 101 | 0.10 |  |

### `boats` -> `creativeengineering:group/boats`

Ships at sea: the Towns & Towers ship village and pillager ship. Meta structures join later.

Grid: spacing 48, separation 28 (members never start closer than 29 chunks = 464 blocks apart). At most 1 structure per 48 x 48 chunks anywhere.

Capped (old combined density above the group grid): 9 biome(s), 100% of the group's area. Largest: `minecraft:deep_ocean` 1 per 17 -> 48, `minecraft:deep_cold_ocean` 1 per 16 -> 48, `minecraft:deep_lukewarm_ocean` 1 per 17 -> 48, `minecraft:ocean` 1 per 17 -> 48, `minecraft:warm_ocean` 1 per 18 -> 48, `minecraft:cold_ocean` 1 per 17 -> 48.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `towns_and_towers:pillager_outpost_ocean` | 72 | 0.9618 | 110 | 160 | 0.48 |  |
| `towns_and_towers:village_ocean` | 138 | 0.9588 | 53 | 115 | 0.21 |  |
| `antiquetradingship:antique_trading_ship` | 97 | 1.0 | 36 | 123 | 0.09 |  |
| `blocky_siege:pillager_ship` | 57 | 1.0 | 120 | 159 | 0.57 |  |
| `illager_viking_longship:illager_longship` | 148 | 1.0 | 45 | 96 | 0.22 |  |
| `illagerwarship:warship` | 97 | 1.0 | 36 | 123 | 0.09 |  |
| `unwrecked_ships:ship` | 73 | 1.0 | 60 | 149 | 0.16 |  |
| `villager_rafts:raft_1` | 83 | 1.0 | 50 | 140 | 0.13 |  |
| `villager_rafts:raft_2` | 83 | 1.0 | 50 | 140 | 0.13 |  |
| `villager_rafts:raft_3` | 83 | 1.0 | 50 | 140 | 0.13 |  |

### `remote` -> `creativeengineering:group/remote`

Lone camps and cabins: the T&T wandering trader camp and every single-building house mod in the Overworld. The two Farmer's Structures houses whose biomes come from mods this pack does not have (Aether, Undergarden) stay out.

Grid: spacing 16, separation 8 (members never start closer than 9 chunks = 144 blocks apart). At most 1 structure per 16 x 16 chunks anywhere.

Capped (old combined density above the group grid): 3 biome(s), 15% of the group's area. Largest: `minecraft:plains` 1 per 10 -> 17, `minecraft:forest` 1 per 13 -> 19, `minecraft:sunflower_plains` 1 per 10 -> 20.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `beautify:botanist_house_desert` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_plains` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_savanna` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_snowy` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_taiga` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `farmers_structures:aquacultures_beach` | 64 | 0.0307 | 86 | 103 | 0.69 |  |
| `farmers_structures:aquacultures_carp` | 42 | 0.9679 | 63 | 76 | 0.70 |  |
| `farmers_structures:aquacultures_common` | 42 | 0.9679 | 63 | 76 | 0.70 |  |
| `farmers_structures:cooks` | 183 | 0.3129 | 53 | 62 | 0.73 |  |
| `farmers_structures:cooks_moss` | 100 | 1.0 | - | - | - | no biome in this pack |
| `farmers_structures:sushis` | 220 | 0.1058 | 46 | 49 | 0.88 |  |
| `greek_villager_house:greek_house` | 73 | 1.0 | 45 | 53 | 0.73 |  |
| `hobbit_house_:hobbit_house` | 69 | 0.9939 | 45 | 53 | 0.72 |  |
| `minecraft:igloo` | 389 | 0.2286 | 32 | 38 | 0.72 |  |
| `old_swamp_fisherman_house:fisherman_house` | 247 | 0.1192 | 40 | 46 | 0.74 |  |
| `sawmillhouse:sawmill` | 159 | 0.8113 | 33 | 45 | 0.55 |  |
| `simply_houses:forest_chapel` | 67 | 0.0718 | 171 | 228 | 0.57 |  |
| `simply_houses:forest_fort` | 67 | 0.0718 | 171 | 228 | 0.57 |  |
| `simply_houses:forest_house` | 135 | 0.0713 | 121 | 161 | 0.57 |  |
| `simply_houses:forest_stonemason` | 135 | 0.0713 | 121 | 161 | 0.57 |  |
| `simply_houses:plains_house` | 23 | 1.0 | 85 | 130 | 0.43 |  |
| `simply_houses:plains_mansion` | 23 | 1.0 | 85 | 130 | 0.43 |  |
| `simply_houses:taiga_forge` | 100 | 0.0871 | 122 | 141 | 0.76 |  |
| `simply_houses:taiga_house` | 100 | 0.0871 | 122 | 141 | 0.76 |  |
| `simply_houses:taiga_inn` | 200 | 0.0871 | 87 | 99 | 0.76 |  |
| `simply_houses:taiga_woodcutter` | 200 | 0.0871 | 87 | 99 | 0.76 |  |
| `small_viking_villager_house:small_viking_house` | 64 | 1.0 | 48 | 58 | 0.69 |  |
| `small_villager_greenhouse:small_villager_greenhouse` | 72 | 1.0 | 45 | 76 | 0.35 |  |
| `towns_and_towers:exclusives/village_wandering_trader_camp` | 423 | 0.2036 | 65 | 35 | 3.35 |  |
| `villager_butcher_house:butcher_house` | 87 | 1.0 | 43 | 78 | 0.30 |  |
| `villager_cartographer_house:villager_cartographer_house` | 91 | 1.0 | 42 | 76 | 0.30 |  |
| `villager_cleric_house:villager_cleric_house` | 104 | 1.0 | 39 | 71 | 0.30 |  |
| `villager_country_house:country_house` | 447 | 0.1166 | 50 | 57 | 0.77 |  |
| `villager_farmer_house:villager_farmer_house` | 74 | 1.0 | 47 | 85 | 0.31 |  |
| `villager_fletcher_house:villager_fletcher_house` | 109 | 1.0 | 38 | 70 | 0.30 |  |
| `villager_flower_house:flower_house` | 781 | 0.1035 | 45 | 51 | 0.77 |  |
| `villager_tannery:tannery` | 73 | 1.0 | 45 | 76 | 0.35 |  |
| `ancient_enchantment_temple:temple` | 110 | 0.1264 | 45 | 45 | 1.00 |  |
| `animal_barn:barn` | 116 | 1.0 | 45 | 68 | 0.44 |  |
| `bighorsestable:big_horse_stable` | 98 | 1.0 | 40 | 66 | 0.37 |  |
| `desert_village_temple:village_temple` | 110 | 0.2528 | 45 | 45 | 1.00 |  |
| `desert_watchtower:watchtower` | 110 | 0.2528 | 45 | 45 | 1.00 |  |
| `forest_watchtower:forest_watchtower` | 135 | 1.0 | 40 | 51 | 0.62 |  |
| `gothic_church:church` | 81 | 1.0 | 45 | 65 | 0.48 |  |
| `llama_stable:llama_stable` | 62 | 1.0 | 45 | 43 | 1.12 |  |
| `mini_clock_tower:mini_clock_tower` | 108 | 1.0 | 43 | 49 | 0.76 |  |
| `plains_villager_tents:big_villager_tent` | 116 | 1.0 | 45 | 68 | 0.44 |  |
| `saloon:saloon` | 326 | 0.3554 | 28 | 28 | 1.00 |  |
| `smallhorsestable:horse_stable` | 111 | 1.0 | 34 | 61 | 0.31 |  |
| `snowy_tents:snowy_tent` | 284 | 1.0 | 23 | 27 | 0.71 |  |
| `villager_archer_camp:villager_archer_camp` | 85 | 1.0 | 45 | 70 | 0.42 |  |
| `villager_circus_tent:villager_circus_tent` | 84 | 1.0 | 45 | 70 | 0.41 |  |
| `villager_fish_shop:fish_shop` | 17 | 0.7785 | 45 | 43 | 1.08 |  |
| `villager_fishermans_hut:villager_fishermans_hut` | 125 | 1.0 | 42 | 65 | 0.42 |  |
| `villager_forge:villager_forge` | 116 | 1.0 | 45 | 68 | 0.44 |  |
| `villager_gold_washing_facility:villager_gold_washing_facility` | 37 | 1.0 | 50 | 50 | 1.00 |  |
| `villager_windmill:windmill` | 103 | 1.0 | 50 | 72 | 0.49 |  |

### `sky` -> `creativeengineering:group/sky`

Sky structures: Cloud Storage's hot air balloons and sky temples, AE2 meteorites (same rate as the clouds). Meta structures join later.

Grid: spacing 72, separation 16 (members never start closer than 17 chunks = 272 blocks apart). At most 1 structure per 72 x 72 chunks anywhere.

Capped (old combined density above the group grid): 145 biome(s), 100% of the group's area. Largest: `terralith:cave/deep_caves` 1 per 20 -> 73, `minecraft:lush_caves` 1 per 20 -> 73, `minecraft:deep_ocean` 1 per 19 -> 72, `minecraft:deep_lukewarm_ocean` 1 per 19 -> 72, `minecraft:deep_cold_ocean` 1 per 19 -> 72, `terralith:cave/andesite_caves` 1 per 20 -> 73.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `ae2:meteorite` | 100 | 0.985 | 32 | 138 | 0.05 |  |
| `cloudstorage:big_balloon` | 100 | 0.985 | 32 | 138 | 0.05 |  |
| `cloudstorage:sky_temple` | 100 | 0.985 | 40 | 138 | 0.08 |  |
| `skyvillages:skyvillage` | 299 | 1.0 | 70 | 102 | 0.47 |  |

## Standalone sets (changed ones only)

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `farmers_structures:aethers` | 1 | 1.0 | - | - | - | no biome in this pack |
| `farmers_structures:cooks_nether_1` | 1 | 1.0 | 40 | 38 | 1.07 |  |
| `farmers_structures:cooks_nether_2` | 1 | 1.0 | 49 | 47 | 1.07 |  |
| `farmers_structures:undergardens_sea` | 1 | 1.0 | - | - | - | no biome in this pack |
| `hearths:crimson_tower` | 1 | 1.0 | 16 | 12 | 1.66 |  |
| `hearths:warped_tower` | 1 | 1.0 | 16 | 12 | 1.66 |  |
| `mes:starlight_voyager` | 1 | 1.0 | 31 | 27 | 1.33 |  |
| `minecraft:ancient_city` | 1 | 1.0 | 24 | 20 | 1.50 |  |
| `minecraft:buried_treasure` | 1 | 1.0 | 10 | 8 | 1.50 |  |
| `minecraft:desert_pyramid` | 1 | 1.0 | - | - | - | blocked in code |
| `minecraft:jungle_pyramid` | 1 | 1.0 | - | - | - | blocked in code |
| `minecraft:ruined_portal` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_desert` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_jungle` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_swamp` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_mountain` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_ocean` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_nether` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:stronghold` | 1 | 1.0 | - | - | - | blocked in code |
| `minecraft:swamp_hut` | 1 | 1.0 | - | - | - | blocked in code |
| `nullscape:dragon_skeleton` | 1 | 1.0 | 22 | 25 | 0.75 |  |
| `philipsruins:ancient_crypt` | 1 | 1.0 | 80 | 57 | 2.00 |  |
| `philipsruins:ancient_dungeon` | 1 | 1.0 | 10 | 7 | 2.00 |  |
| `philipsruins:antiquus_crypta` | 1 | 1.0 | 90 | 64 | 2.00 |  |
| `philipsruins:end_ruins` | 1 | 1.0 | 49 | 35 | 2.00 |  |
| `philipsruins:end_gate_fortress` | 1 | 1.0 | 49 | 35 | 2.00 |  |
| `philipsruins:lost_soul_city` | 1 | 1.0 | 80 | 38 | 4.50 |  |
| `philipsruins:start_nether_dungeon` | 1 | 1.0 | 52 | 37 | 2.00 |  |
| `philipsruins:sealedruinedghaststatue1` | 1 | 1.0 | 52 | 37 | 2.00 |  |
| `philipsruins:nether_lava_ruins` | 1 | 1.0 | 52 | 37 | 2.00 |  |
| `philipsruins:lost_soul_dungeon` | 86 | 0.991 | 120 | 73 | 2.67 |  |
| `philipsruins:bone_dungeon` | 116 | 0.2939 | 60 | 42 | 2.00 |  |
| `philipsruins:underground_structures` | 582 | 0.9885 | 120 | 28 | 18.00 |  |
| `philipsruins:sculk_dungeon` | 86 | 0.991 | 120 | 73 | 2.67 |  |
| `supplementaries:way_sign` | 1 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `terralith:underground/oak_cabin` | 50 | 1.0 | 16 | 7 | 4.47 |  |
| `terralith:underground/giant_bee_hive` | 78 | 0.4261 | 35 | 9 | 15.26 |  |
| `terralith:underground/mining_outpost` | 713 | 0.1097 | 13 | 11 | 1.50 |  |
| `terralith:underground/old_refinery` | 258 | 0.1166 | 17 | 13 | 1.65 |  |
| `terralith:underground/sunken_tower` | 129 | 0.4277 | 22 | 7 | 10.13 |  |
| `terralith:underground/witch_hut` | 44 | 1.0 | 23 | 8 | 8.68 |  |

