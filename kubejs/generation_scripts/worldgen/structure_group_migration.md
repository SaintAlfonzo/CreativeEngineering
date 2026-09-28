# Structure groups vs the tuning they replaced

Written by `python3 tools/gen_structure_groups.py`. Do not edit. Expected structures per area for every structure, from `structure_groups.json`, each structure's biome pool and the real biome areas in `biome_config.json`. "1 per N" means one structure per N x N chunks, averaged over the biomes it can spawn in (so a structure limited to rare biomes is still rare overall). The game itself writes `structure_tag_report.md`; this file is the offline estimate.

Compared against: **the tuning before groups (archived 2026-09-27)**. Ratio = new / that; 1.00 = unchanged.

## Groups

### `settlements` -> `creativeengineering:group/settlements`

Friendly settlements: vanilla, Towns & Towers and Missing Villages villages, Terralith fortified villages. Meta structures join later.

Grid: spacing 24, separation 10 (members never start closer than 11 chunks = 176 blocks apart). At most 1 structure per 24 x 24 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `minecraft:village_desert` | 424 | 0.6975 | 28 | 32 | 32 | 1.00 |  |
| `minecraft:village_plains` | 162 | 0.9263 | 28 | 32 | 31 | 1.04 |  |
| `minecraft:village_savanna` | 176 | 0.8134 | 28 | 32 | 31 | 1.05 |  |
| `minecraft:village_snowy` | 132 | 1.0 | 29 | 33 | 33 | 1.02 |  |
| `minecraft:village_taiga` | 153 | 0.9436 | 29 | 32 | 32 | 1.05 |  |
| `terralith:fortified_desert_village` | 108 | 1.0 | 46 | 53 | 53 | 0.99 |  |
| `terralith:fortified_village` | 53 | 1.0 | 46 | 53 | 54 | 0.99 |  |
| `themissingvillages:large/jungle_bamboo_village` | 6 | 0.3154 | 288 | 272 | 269 | 1.02 |  |
| `themissingvillages:large/jungle_sparse_village` | 4 | 0.4889 | 239 | 225 | 227 | 0.98 |  |
| `themissingvillages:large/jungle_village` | 4 | 0.4891 | 238 | 224 | 226 | 0.98 |  |
| `themissingvillages:large/swamp_mangrove_village` | 19 | 0.2883 | 218 | 205 | 201 | 1.04 |  |
| `themissingvillages:large/swamp_village` | 12 | 0.4916 | 213 | 201 | 201 | 1.00 |  |
| `themissingvillages:medium/jungle_bamboo_village` | 26 | 0.2911 | 144 | 136 | 135 | 1.02 |  |
| `themissingvillages:medium/jungle_sparse_village` | 17 | 0.4601 | 120 | 113 | 114 | 0.98 |  |
| `themissingvillages:medium/jungle_village` | 17 | 0.4604 | 119 | 112 | 113 | 0.98 |  |
| `themissingvillages:medium/swamp_mangrove_village` | 74 | 0.296 | 109 | 103 | 101 | 1.04 |  |
| `themissingvillages:medium/swamp_village` | 47 | 0.502 | 107 | 101 | 100 | 1.00 |  |
| `themissingvillages:small/jungle_bamboo_village` | 52 | 0.2911 | 102 | 96 | 95 | 1.02 |  |
| `themissingvillages:small/jungle_sparse_village` | 35 | 0.447 | 85 | 80 | 80 | 0.98 |  |
| `themissingvillages:small/jungle_village` | 35 | 0.4472 | 84 | 79 | 80 | 0.98 |  |
| `themissingvillages:small/swamp_mangrove_village` | 148 | 0.296 | 77 | 73 | 71 | 1.04 |  |
| `themissingvillages:small/swamp_village` | 94 | 0.502 | 75 | 71 | 71 | 1.00 |  |
| `towns_and_towers:exclusives/village_classic` | 101 | 0.1623 | 53 | 60 | 60 | 1.00 |  |
| `towns_and_towers:exclusives/village_iberian` | 100 | 1.0 | - | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/village_mediterranean` | 41 | 1.0 | 53 | 60 | 60 | 0.99 |  |
| `towns_and_towers:exclusives/village_nilotic` | 100 | 1.0 | - | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/village_rustic` | 100 | 1.0 | - | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/village_swedish` | 22 | 1.0 | 92 | 103 | 104 | 0.99 |  |
| `towns_and_towers:exclusives/village_tudor` | 99 | 0.4132 | 53 | 60 | 60 | 1.00 |  |
| `towns_and_towers:village_badlands` | 173 | 0.2113 | 61 | 68 | 66 | 1.04 |  |
| `towns_and_towers:village_beach` | 101 | 0.1623 | 53 | 60 | 60 | 1.00 |  |
| `towns_and_towers:village_birch_forest` | 162 | 0.197 | 61 | 69 | 66 | 1.09 |  |
| `towns_and_towers:village_flower_forest` | 360 | 0.1637 | 53 | 60 | 60 | 1.00 |  |
| `towns_and_towers:village_forest` | 144 | 0.1749 | 55 | 62 | 60 | 1.05 |  |
| `towns_and_towers:village_grove` | 213 | 0.1696 | 60 | 67 | 66 | 1.02 |  |
| `towns_and_towers:village_jungle` | 46 | 0.4485 | 64 | 72 | 70 | 1.08 |  |
| `towns_and_towers:village_meadow` | 62 | 0.6944 | 53 | 60 | 59 | 1.00 |  |
| `towns_and_towers:village_mushroom_fields` | 101 | 0.1623 | 53 | 60 | 60 | 1.00 |  |
| `towns_and_towers:village_old_growth_taiga` | 110 | 0.2153 | 58 | 65 | 62 | 1.09 |  |
| `towns_and_towers:village_snowy_slopes` | 141 | 0.2799 | 54 | 60 | 59 | 1.04 |  |
| `towns_and_towers:village_snowy_taiga` | 108 | 0.1943 | 70 | 79 | 76 | 1.07 |  |
| `towns_and_towers:village_sparse_jungle` | 46 | 0.45 | 64 | 72 | 70 | 1.08 |  |
| `towns_and_towers:village_sunflower_plains` | 228 | 0.2615 | 53 | 60 | 59 | 1.01 |  |
| `towns_and_towers:village_swamp` | 142 | 0.5026 | 53 | 60 | 58 | 1.06 |  |
| `towns_and_towers:village_wooded_badlands` | 205 | 0.1738 | 66 | 74 | 73 | 1.03 |  |

### `pillager_settlements` -> `creativeengineering:group/pillager_settlements`

Pillager outposts: vanilla, and every Towns & Towers tower, fort and camp outpost except the ship (boats).

Grid: spacing 32, separation 16 (members never start closer than 17 chunks = 272 blocks apart). At most 1 structure per 32 x 32 chunks anywhere. Excluded within 4 chunks of `creativeengineering:group/settlements` grid slots.

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `minecraft:pillager_outpost` | 74 | 0.6336 | 121 | 58 | 55 | 1.13 |  |
| `towns_and_towers:exclusives/pillager_outpost_classic` | 80 | 0.2338 | 110 | 71 | 71 | 1.00 |  |
| `towns_and_towers:exclusives/pillager_outpost_iberian` | 100 | 1.0 | - | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/pillager_outpost_mediterranean` | 78 | 0.3596 | 156 | 71 | 71 | 1.00 |  |
| `towns_and_towers:exclusives/pillager_outpost_oriental` | 65 | 1.0 | 184 | 64 | 65 | 0.98 |  |
| `towns_and_towers:exclusives/pillager_outpost_rustic` | 100 | 1.0 | - | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | 31 | 1.0 | 221 | 111 | 111 | 0.99 |  |
| `towns_and_towers:exclusives/pillager_outpost_tudor` | 100 | 0.2918 | 110 | 64 | 64 | 1.00 |  |
| `towns_and_towers:pillager_outpost_badlands` | 83 | 0.3573 | 135 | 71 | 69 | 1.08 |  |
| `towns_and_towers:pillager_outpost_beach` | 151 | 0.4394 | 110 | 52 | 52 | 1.00 |  |
| `towns_and_towers:pillager_outpost_birch_forest` | 132 | 0.2977 | 130 | 74 | 73 | 1.01 |  |
| `towns_and_towers:pillager_outpost_desert` | 159 | 0.3206 | 111 | 71 | 71 | 1.01 |  |
| `towns_and_towers:pillager_outpost_flower_forest` | 252 | 0.3165 | 111 | 64 | 64 | 1.01 |  |
| `towns_and_towers:pillager_outpost_forest` | 136 | 0.2912 | 114 | 66 | 66 | 1.00 |  |
| `towns_and_towers:pillager_outpost_grove` | 172 | 0.2852 | 117 | 75 | 75 | 0.99 |  |
| `towns_and_towers:pillager_outpost_jungle` | 109 | 0.3648 | 134 | 71 | 69 | 1.06 |  |
| `towns_and_towers:pillager_outpost_meadow` | 152 | 0.4 | 110 | 64 | 63 | 1.01 |  |
| `towns_and_towers:pillager_outpost_mushroom_fields` | 100 | 0.2918 | 110 | 64 | 64 | 1.00 |  |
| `towns_and_towers:pillager_outpost_old_growth_taiga` | 108 | 0.4216 | 149 | 72 | 69 | 1.08 |  |
| `towns_and_towers:pillager_outpost_savanna` | 42 | 0.8362 | 138 | 64 | 62 | 1.05 |  |
| `towns_and_towers:pillager_outpost_snowy_beach` | 151 | 0.4394 | 110 | 52 | 52 | 1.00 |  |
| `towns_and_towers:pillager_outpost_snowy_plains` | 156 | 0.4073 | 111 | 64 | 64 | 1.01 |  |
| `towns_and_towers:pillager_outpost_snowy_slopes` | 67 | 0.6132 | 110 | 64 | 64 | 1.00 |  |
| `towns_and_towers:pillager_outpost_snowy_taiga` | 100 | 0.3106 | 154 | 85 | 84 | 1.03 |  |
| `towns_and_towers:pillager_outpost_sparse_jungle` | 110 | 0.6864 | 134 | 52 | 51 | 1.06 |  |
| `towns_and_towers:pillager_outpost_sunflower_plains` | 100 | 0.2918 | 110 | 64 | 64 | 1.00 |  |
| `towns_and_towers:pillager_outpost_swamp` | 100 | 0.2918 | 110 | 64 | 64 | 1.00 |  |
| `towns_and_towers:pillager_outpost_taiga` | 45 | 1.0 | 152 | 70 | 71 | 0.96 |  |
| `towns_and_towers:pillager_outpost_wooded_badlands` | 81 | 0.7962 | 145 | 52 | 50 | 1.09 |  |

### `ruins` -> `creativeengineering:group/ruins`

Surface land ruins: Philip's Ruins and Terralith's glacial hut. Underground ruins stay standalone.

Grid: spacing 11, separation 6 (members never start closer than 7 chunks = 112 blocks apart). At most 1 structure per 11 x 11 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `philipsruins:ancient_ruins` | 119 | 0.6739 | 50 | 29 | 28 | 1.04 |  |
| `philipsruins:ancient_towers` | 197 | 0.6758 | 50 | 22 | 22 | 1.04 |  |
| `philipsruins:badlands_structures` | 250 | 0.2387 | 55 | 23 | 23 | 1.00 |  |
| `philipsruins:desert_structures` | 250 | 0.2387 | 39 | 23 | 23 | 1.00 |  |
| `philipsruins:field_stone_ruins` | 45 | 0.9895 | 75 | 43 | 43 | 1.01 |  |
| `philipsruins:field_stone_ruins_rocks` | 196 | 0.322 | 68 | 32 | 31 | 1.09 |  |
| `philipsruins:level_one_ruins` | 45 | 0.9895 | 75 | 43 | 43 | 1.01 |  |
| `philipsruins:level_three_ruins` | 35 | 0.9905 | 85 | 49 | 49 | 1.01 |  |
| `philipsruins:level_two_ruins` | 20 | 0.9784 | 113 | 65 | 65 | 1.01 |  |
| `philipsruins:level_two_ruins_pool` | 20 | 0.9784 | 113 | 65 | 65 | 1.01 |  |
| `philipsruins:pumpkin_ruins` | 84 | 1.0 | 50 | 29 | 30 | 0.94 |  |
| `philipsruins:rare_ruin` | 254 | 0.0082 | 360 | 208 | 205 | 1.03 |  |
| `philipsruins:start_nether_ruin` | 3 | 0.8688 | 310 | 179 | 178 | 1.01 |  |
| `terralith:glacial_hut` | 261 | 0.249 | 27 | 22 | 22 | 1.00 |  |

### `surface_dungeons` -> `creativeengineering:group/surface_dungeons`

Surface dungeons: YUNG's temples and witch huts, the woodland mansion, Terralith's spire, the T&T desert mimic, Philip's badlands dungeon. The vanilla temples YUNG's blocks in code stay out.

Grid: spacing 18, separation 7 (members never start closer than 8 chunks = 128 blocks apart). At most 1 structure per 18 x 18 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `betterdeserttemples:desert_temple` | 97 | 0.7059 | 35 | 29 | 29 | 1.00 |  |
| `betterjungletemples:jungle_temple` | 186 | 0.7481 | 24 | 21 | 21 | 1.00 |  |
| `betterwitchhuts:witch_circle` | 62 | 1.0 | 40 | 35 | 35 | 0.98 |  |
| `betterwitchhuts:witch_hut` | 172 | 0.8021 | 30 | 23 | 22 | 1.01 |  |
| `minecraft:mansion` | 30 | 0.1191 | 80 | 52 | 52 | 1.00 |  |
| `philipsruins:badlands_start_dungeon` | 106 | 0.426 | 55 | 28 | 28 | 1.00 |  |
| `terralith:spire` | 103 | 0.415 | 36 | 28 | 28 | 1.00 |  |
| `towns_and_towers:mimic_desert` | 79 | 0.7049 | 32 | 32 | 32 | 1.00 |  |

### `ocean_ruins` -> `creativeengineering:group/ocean_ruins`

Ocean ruins and wrecks: vanilla, Hopo's and Philip's ocean ruins, Philip's ocean fortress, vanilla shipwrecks, the T&T wreckage.

Grid: spacing 8, separation 4 (members never start closer than 5 chunks = 80 blocks apart). At most 1 structure per 8 x 8 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `hopo:underwater_ruins` | 113 | 1.0 | 22 | 22 | 22 | 1.00 |  |
| `hopo:underwater_small_ruins` | 513 | 0.6268 | 13 | 13 | 13 | 1.00 |  |
| `hopo:underwater_warm_small_ruins` | 560 | 0.9701 | 10 | 10 | 10 | 1.00 |  |
| `minecraft:ocean_ruin_cold` | 143 | 0.627 | 20 | 25 | 25 | 1.00 |  |
| `minecraft:ocean_ruin_warm` | 92 | 0.9743 | 20 | 25 | 25 | 1.00 |  |
| `minecraft:shipwreck` | 41 | 0.0733 | 17 | 42 | 42 | 1.00 |  |
| `minecraft:shipwreck_beached` | 41 | 0.0733 | 17 | 42 | 42 | 1.00 |  |
| `philipsruins:ocean_fortress` | 29 | 0.5047 | 70 | 61 | 61 | 1.00 |  |
| `philipsruins:ocean_ruins` | 58 | 0.4946 | 50 | 44 | 44 | 1.00 |  |
| `towns_and_towers:wreckage_ocean` | 108 | 0.3243 | 32 | 39 | 39 | 1.00 |  |

### `ocean_dungeons` -> `creativeengineering:group/ocean_dungeons`

Ocean monuments, on the monuments' own grid (same spacing, spread and salt, so positions match vanilla). Monument meta structures join later.

Grid: spacing 32, separation 5 (members never start closer than 6 chunks = 96 blocks apart). At most 1 structure per 32 x 32 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `minecraft:monument` | 100 | 0.1 | 32 | 101 | 101 | 1.00 |  |

### `sky` -> `creativeengineering:group/sky`

Sky structures: Cloud Storage's hot air balloons and sky temples. Meta structures join later.

Grid: spacing 151, separation 114 (members never start closer than 115 chunks = 1840 blocks apart). At most 1 structure per 151 x 151 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `cloudstorage:big_balloon` | 100 | 0.9896 | 32 | 215 | 215 | 1.00 |  |
| `cloudstorage:sky_temple` | 100 | 0.9896 | 40 | 215 | 215 | 1.00 |  |

### `boats` -> `creativeengineering:group/boats`

Ships at sea: the Towns & Towers ship village and pillager ship. Meta structures join later.

Grid: spacing 41, separation 21 (members never start closer than 22 chunks = 352 blocks apart). At most 1 structure per 41 x 41 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `towns_and_towers:pillager_outpost_ocean` | 72 | 0.9618 | 110 | 71 | 71 | 1.00 |  |
| `towns_and_towers:village_ocean` | 138 | 0.9588 | 53 | 52 | 52 | 1.00 |  |

### `remote` -> `creativeengineering:group/remote`

Lone camps and cabins: the T&T wandering trader camp and every single-building house mod in the Overworld. The two Farmer's Structures houses whose biomes come from mods this pack does not have (Aether, Undergarden) stay out.

Grid: spacing 17, separation 12 (members never start closer than 13 chunks = 208 blocks apart). At most 1 structure per 17 x 17 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `beautify:botanist_house_desert` | 100 | 1.0 | - | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_plains` | 100 | 1.0 | - | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_savanna` | 100 | 1.0 | - | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_snowy` | 100 | 1.0 | - | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_taiga` | 100 | 1.0 | - | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `farmers_structures:aquacultures_beach` | 64 | 0.0307 | 86 | 97 | 97 | 1.00 |  |
| `farmers_structures:aquacultures_carp` | 42 | 0.9679 | 63 | 72 | 69 | 1.08 |  |
| `farmers_structures:aquacultures_common` | 42 | 0.9679 | 63 | 72 | 69 | 1.08 |  |
| `farmers_structures:cooks` | 183 | 0.3129 | 53 | 60 | 56 | 1.13 |  |
| `farmers_structures:cooks_moss` | 100 | 1.0 | - | - | - | - | no biome in this pack |
| `farmers_structures:sushis` | 220 | 0.1058 | 46 | 52 | 52 | 1.00 |  |
| `greek_villager_house:greek_house` | 73 | 1.0 | 45 | 55 | 56 | 0.98 |  |
| `hobbit_house_:hobbit_house` | 69 | 0.9939 | 45 | 55 | 55 | 1.00 |  |
| `minecraft:igloo` | 389 | 0.2286 | 32 | 39 | 38 | 1.07 |  |
| `old_swamp_fisherman_house:fisherman_house` | 247 | 0.1192 | 40 | 49 | 49 | 1.00 |  |
| `sawmillhouse:sawmill` | 159 | 0.8113 | 33 | 41 | 40 | 1.01 |  |
| `simply_houses:forest_chapel` | 67 | 0.0718 | 171 | 211 | 210 | 1.01 |  |
| `simply_houses:forest_fort` | 67 | 0.0718 | 171 | 211 | 210 | 1.01 |  |
| `simply_houses:forest_house` | 135 | 0.0713 | 121 | 149 | 149 | 1.01 |  |
| `simply_houses:forest_stonemason` | 135 | 0.0713 | 121 | 149 | 149 | 1.01 |  |
| `simply_houses:plains_house` | 23 | 1.0 | 85 | 104 | 107 | 0.96 |  |
| `simply_houses:plains_mansion` | 23 | 1.0 | 85 | 104 | 107 | 0.96 |  |
| `simply_houses:taiga_forge` | 100 | 0.0871 | 122 | 151 | 150 | 1.01 |  |
| `simply_houses:taiga_house` | 100 | 0.0871 | 122 | 151 | 150 | 1.01 |  |
| `simply_houses:taiga_inn` | 200 | 0.0871 | 87 | 107 | 106 | 1.01 |  |
| `simply_houses:taiga_woodcutter` | 200 | 0.0871 | 87 | 107 | 106 | 1.01 |  |
| `small_viking_villager_house:small_viking_house` | 64 | 1.0 | 48 | 59 | 59 | 0.99 |  |
| `small_villager_greenhouse:small_villager_greenhouse` | 72 | 1.0 | 45 | 55 | 58 | 0.90 |  |
| `towns_and_towers:exclusives/village_wandering_trader_camp` | 423 | 0.2036 | 80 | 38 | 38 | 1.00 |  |
| `villager_butcher_house:butcher_house` | 87 | 1.0 | 43 | 53 | 56 | 0.91 |  |
| `villager_cartographer_house:villager_cartographer_house` | 91 | 1.0 | 42 | 52 | 54 | 0.90 |  |
| `villager_cleric_house:villager_cleric_house` | 104 | 1.0 | 39 | 48 | 51 | 0.89 |  |
| `villager_country_house:country_house` | 447 | 0.1166 | 50 | 62 | 61 | 1.03 |  |
| `villager_farmer_house:villager_farmer_house` | 74 | 1.0 | 47 | 58 | 60 | 0.92 |  |
| `villager_fletcher_house:villager_fletcher_house` | 109 | 1.0 | 38 | 47 | 50 | 0.89 |  |
| `villager_flower_house:flower_house` | 781 | 0.1035 | 45 | 55 | 55 | 1.01 |  |
| `villager_tannery:tannery` | 73 | 1.0 | 45 | 55 | 58 | 0.91 |  |

## Standalone sets (changed ones only)

| Structure | Weight | Frequency | Mod default 1 per | Before 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|---|
| `farmers_structures:aethers` | 1 | 1.0 | - | - | - | - | no biome in this pack |
| `farmers_structures:cooks_nether_1` | 1 | 1.0 | 40 | 45 | 44 | 1.04 |  |
| `farmers_structures:cooks_nether_2` | 1 | 1.0 | 49 | 55 | 54 | 1.04 |  |
| `farmers_structures:undergardens_sea` | 1 | 1.0 | - | - | - | - | no biome in this pack |
| `hearths:crimson_tower` | 1 | 1.0 | 16 | 12 | 12 | 1.00 |  |
| `hearths:warped_tower` | 1 | 1.0 | 16 | 12 | 12 | 1.00 |  |
| `mes:starlight_voyager` | 1 | 1.0 | 31 | 27 | 27 | 1.00 |  |
| `minecraft:ancient_city` | 1 | 1.0 | 24 | 20 | 20 | 1.00 |  |
| `minecraft:buried_treasure` | 1 | 1.0 | 10 | 8 | 8 | 1.00 |  |
| `minecraft:desert_pyramid` | 1 | 1.0 | - | - | - | - | blocked in code |
| `minecraft:jungle_pyramid` | 1 | 1.0 | - | - | - | - | blocked in code |
| `minecraft:ruined_portal` | 1 | 1.0 (off) | 40 | - | - | - |  |
| `minecraft:ruined_portal_desert` | 1 | 1.0 (off) | 40 | - | - | - |  |
| `minecraft:ruined_portal_jungle` | 1 | 1.0 (off) | 42 | - | - | - |  |
| `minecraft:ruined_portal_swamp` | 1 | 1.0 (off) | 40 | - | - | - |  |
| `minecraft:ruined_portal_mountain` | 1 | 1.0 (off) | 41 | - | - | - |  |
| `minecraft:ruined_portal_ocean` | 1 | 1.0 (off) | 40 | - | - | - |  |
| `minecraft:ruined_portal_nether` | 1 | 1.0 (off) | 40 | - | - | - |  |
| `minecraft:stronghold` | 1 | 1.0 | - | - | - | - | blocked in code |
| `minecraft:swamp_hut` | 1 | 1.0 | - | - | - | - | blocked in code |
| `nullscape:dragon_skeleton` | 1 | 1.0 | 22 | 25 | 25 | 1.00 |  |
| `philipsruins:ancient_crypt` | 1 | 1.0 | 80 | 57 | 57 | 1.00 |  |
| `philipsruins:ancient_dungeon` | 1 | 1.0 | 10 | 7 | 7 | 1.00 |  |
| `philipsruins:antiquus_crypta` | 1 | 1.0 | 90 | 64 | 64 | 1.00 |  |
| `philipsruins:end_ruins` | 1 | 1.0 | 49 | 35 | 35 | 1.00 |  |
| `philipsruins:end_gate_fortress` | 1 | 1.0 | 49 | 35 | 35 | 1.00 |  |
| `philipsruins:lost_soul_city` | 1 | 1.0 | 80 | 38 | 38 | 1.00 |  |
| `philipsruins:start_nether_dungeon` | 1 | 1.0 | 52 | 37 | 37 | 1.00 |  |
| `philipsruins:sealedruinedghaststatue1` | 1 | 1.0 | 52 | 37 | 37 | 1.00 |  |
| `philipsruins:nether_lava_ruins` | 1 | 1.0 | 52 | 37 | 37 | 1.00 |  |
| `philipsruins:lost_soul_dungeon` | 86 | 0.991 | 120 | 73 | 73 | 1.00 |  |
| `philipsruins:bone_dungeon` | 116 | 0.2939 | 60 | 42 | 42 | 1.00 |  |
| `philipsruins:underground_structures` | 582 | 0.9885 | 120 | 28 | 28 | 1.00 |  |
| `philipsruins:sculk_dungeon` | 86 | 0.991 | 120 | 73 | 73 | 1.00 |  |
| `supplementaries:way_sign` | 1 | 1.0 | - | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `terralith:underground/oak_cabin` | 50 | 1.0 | 16 | 7 | 7 | 0.99 |  |
| `terralith:underground/giant_bee_hive` | 78 | 0.4261 | 35 | 9 | 9 | 1.00 |  |
| `terralith:underground/mining_outpost` | 713 | 0.1097 | 13 | 11 | 11 | 1.00 |  |
| `terralith:underground/old_refinery` | 258 | 0.1166 | 17 | 13 | 13 | 1.00 |  |
| `terralith:underground/sunken_tower` | 129 | 0.4277 | 22 | 7 | 7 | 1.00 |  |
| `terralith:underground/witch_hut` | 44 | 1.0 | 24 | 8 | 8 | 1.00 |  |

## Changed by more than 20%

None.

