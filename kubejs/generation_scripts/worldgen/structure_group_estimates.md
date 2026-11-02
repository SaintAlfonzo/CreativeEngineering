# Structure group estimates

Written by `python3 tools/gen_structure_groups.py`. Do not edit. Expected structures per area for every structure, from `structure_groups.json`, each structure's biome pool and the real biome areas in `biome_config.json`. "1 per N" means one structure per N x N chunks, averaged over the biomes it can spawn in (so a structure limited to rare biomes is still rare overall). The game itself writes `structure_tag_report.md`; this file is the offline estimate.

Compared against: **mod defaults**. Ratio = new / that; 1.00 = unchanged.

## Groups

### `settlements` -> `creativeengineering:group/settlements`

Friendly settlements: vanilla and Towns & Towers villages, Terralith fortified villages. Meta structures join later.

Grid: spacing 48, separation 12 (members never start closer than 13 chunks = 208 blocks apart). At most 1 structure per 48 x 48 chunks anywhere.

Capped (old combined density above the group grid): 77 biome(s), 80% of the group's area. Largest: `terralith:yellowstone` 1 per 22 -> 42, `minecraft:plains` 1 per 22 -> 39, `terralith:highlands` 1 per 22 -> 40, `terralith:lush_desert` 1 per 24 -> 39, `terralith:brushland` 1 per 25 -> 40, `terralith:rocky_mountains` 1 per 22 -> 40.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `minecraft:village_desert` | 100 | 0.734547 | 28 | 86 | 0.11 |  |
| `minecraft:village_plains` | 100 | 1.0 | 32 | 115 | 0.08 |  |
| `minecraft:village_savanna` | 100 | 0.99558 | 29 | 62 | 0.21 |  |
| `minecraft:village_snowy` | 100 | 1.0 | 28 | 109 | 0.07 |  |
| `minecraft:village_taiga` | 100 | 1.0 | 30 | 105 | 0.08 |  |
| `terralith:fortified_desert_village` | 100 | 1.0 | 46 | 74 | 0.39 |  |
| `terralith:fortified_village` | 100 | 1.0 | 46 | 108 | 0.18 |  |
| `towns_and_towers:exclusives/village_classic` | 100 | 0.1623 | 53 | 119 | 0.20 |  |
| `towns_and_towers:exclusives/village_iberian` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/village_mediterranean` | 100 | 1.0 | 56 | 72 | 0.61 |  |
| `towns_and_towers:exclusives/village_nilotic` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/village_rustic` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/village_swedish` | 100 | 1.0 | 91 | 106 | 0.75 |  |
| `towns_and_towers:exclusives/village_tudor` | 100 | 1.0 | 85 | 115 | 0.55 |  |
| `towns_and_towers:village_badlands` | 100 | 0.430191 | 78 | 114 | 0.47 |  |
| `towns_and_towers:village_beach` | 100 | 0.171066 | 56 | 122 | 0.21 |  |
| `towns_and_towers:village_birch_forest` | 100 | 0.361997 | 86 | 158 | 0.30 |  |
| `towns_and_towers:village_flower_forest` | 100 | 0.41542 | 107 | 172 | 0.39 |  |
| `towns_and_towers:village_forest` | 100 | 0.457061 | 85 | 138 | 0.38 |  |
| `towns_and_towers:village_grove` | 100 | 0.499402 | 108 | 154 | 0.49 |  |
| `towns_and_towers:village_jungle` | 100 | 0.897 | 75 | 72 | 1.09 |  |
| `towns_and_towers:village_meadow` | 100 | 1.0 | 99 | 115 | 0.74 |  |
| `towns_and_towers:village_mushroom_fields` | 100 | 0.1623 | 53 | 119 | 0.20 |  |
| `towns_and_towers:village_old_growth_taiga` | 100 | 0.717581 | 82 | 124 | 0.44 |  |
| `towns_and_towers:village_snowy_slopes` | 100 | 1.0 | 108 | 109 | 0.98 |  |
| `towns_and_towers:village_snowy_taiga` | 100 | 0.590369 | 108 | 141 | 0.58 |  |
| `towns_and_towers:village_sparse_jungle` | 100 | 0.9 | 75 | 72 | 1.10 |  |
| `towns_and_towers:village_sunflower_plains` | 100 | 0.544825 | 99 | 156 | 0.40 |  |
| `towns_and_towers:village_swamp` | 100 | 0.582744 | 64 | 77 | 0.70 |  |
| `towns_and_towers:village_wooded_badlands` | 100 | 0.340365 | 78 | 128 | 0.37 |  |
| `creativeengineering:meta/village_towers` | 1 | 1.0 | - | 109 | new |  |
| `creativeengineering:meta/fortified_camps` | 1 | 1.0 | - | 184 | new |  |
| `creativeengineering:meta/fortified_ruins` | 1 | 1.0 | - | 181 | new |  |
| `creativeengineering:meta/town_villages` | 1 | 1.0 | - | 143 | new |  |
| `eugene:red_roof_castle` | 100 | 1.0 | 97 | 102 | 0.90 |  |
| `creativeengineering:meta/meteorite_village` | 1 | 1.0 | - | 275 | new |  |
| `creativeengineering:meta/village_siege_camp` | 1 | 1.0 | - | 294 | new |  |
| `creativeengineering:meta/village_siege_ring` | 1 | 1.0 | - | 294 | new |  |

### `pillager_settlements` -> `creativeengineering:group/pillager_settlements`

Pillager outposts: vanilla, and every Towns & Towers tower, fort and camp outpost except the ship (boats).

Grid: spacing 32, separation 8 (members never start closer than 9 chunks = 144 blocks apart). At most 1 structure per 32 x 32 chunks anywhere. Excluded within 4 chunks of `creativeengineering:group/settlements` grid slots.

Capped (old combined density above the group grid): 70 biome(s), 30% of the group's area. Largest: `terralith:yellowstone` 1 per 27 -> 28, `minecraft:plains` 1 per 26 -> 28, `terralith:highlands` 1 per 26 -> 28, `terralith:brushland` 1 per 30 -> 27, `terralith:lush_desert` 1 per 30 -> 28, `terralith:rocky_mountains` 1 per 26 -> 28.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `minecraft:pillager_outpost` | 100 | 1.0 | 121 | 66 | 3.33 |  |
| `towns_and_towers:exclusives/pillager_outpost_classic` | 100 | 0.2338 | 110 | 72 | 2.33 |  |
| `towns_and_towers:exclusives/pillager_outpost_iberian` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/pillager_outpost_mediterranean` | 100 | 1.0 | 163 | 64 | 6.45 |  |
| `towns_and_towers:exclusives/pillager_outpost_oriental` | 100 | 1.0 | 174 | 64 | 7.48 |  |
| `towns_and_towers:exclusives/pillager_outpost_rustic` | 100 | 1.0 | - | - | - | no biome in this pack |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | 100 | 1.0 | 222 | 85 | 6.79 |  |
| `towns_and_towers:exclusives/pillager_outpost_tudor` | 100 | 1.0 | 197 | 84 | 5.55 |  |
| `towns_and_towers:pillager_outpost_badlands` | 100 | 1.0 | 171 | 65 | 6.97 |  |
| `towns_and_towers:pillager_outpost_beach` | 100 | 0.878499 | 117 | 40 | 8.60 |  |
| `towns_and_towers:pillager_outpost_birch_forest` | 100 | 0.720211 | 188 | 93 | 4.08 |  |
| `towns_and_towers:pillager_outpost_desert` | 100 | 0.995091 | 147 | 58 | 6.29 |  |
| `towns_and_towers:pillager_outpost_flower_forest` | 100 | 0.891321 | 222 | 92 | 5.90 |  |
| `towns_and_towers:pillager_outpost_forest` | 100 | 0.610697 | 178 | 97 | 3.40 |  |
| `towns_and_towers:pillager_outpost_grove` | 100 | 1.0 | 250 | 83 | 9.01 |  |
| `towns_and_towers:pillager_outpost_jungle` | 100 | 0.560666 | 156 | 66 | 5.59 |  |
| `towns_and_towers:pillager_outpost_meadow` | 100 | 1.0 | 208 | 85 | 5.99 |  |
| `towns_and_towers:pillager_outpost_mushroom_fields` | 100 | 0.237868 | 110 | 70 | 2.48 |  |
| `towns_and_towers:pillager_outpost_old_growth_taiga` | 100 | 1.0 | 204 | 85 | 5.71 |  |
| `towns_and_towers:pillager_outpost_savanna` | 100 | 1.0 | 141 | 60 | 5.57 |  |
| `towns_and_towers:pillager_outpost_snowy_beach` | 100 | 0.4394 | 292 | 141 | 4.28 |  |
| `towns_and_towers:pillager_outpost_snowy_plains` | 100 | 0.584075 | 250 | 109 | 5.26 |  |
| `towns_and_towers:pillager_outpost_snowy_slopes` | 100 | 1.0 | 250 | 83 | 9.01 |  |
| `towns_and_towers:pillager_outpost_snowy_taiga` | 100 | 1.0 | 250 | 83 | 9.01 |  |
| `towns_and_towers:pillager_outpost_sparse_jungle` | 100 | 1.0 | 156 | 49 | 9.96 |  |
| `towns_and_towers:pillager_outpost_sunflower_plains` | 100 | 1.0 | 208 | 85 | 5.99 |  |
| `towns_and_towers:pillager_outpost_swamp` | 100 | 0.915151 | 134 | 68 | 3.85 |  |
| `towns_and_towers:pillager_outpost_taiga` | 100 | 1.0 | 204 | 85 | 5.71 |  |
| `towns_and_towers:pillager_outpost_wooded_badlands` | 100 | 1.0 | 171 | 65 | 6.97 |  |
| `mysticrift_pillagers_towers_aris:acacia_pillager_tower` | 30 | 0.930863 | 190 | 115 | 2.73 |  |
| `mysticrift_pillagers_towers_aris:birch_pillager_tower` | 24 | 0.905604 | 200 | 169 | 1.39 |  |
| `mysticrift_pillagers_towers_aris:cherry_pillager_tower` | 51 | 0.856877 | 190 | 137 | 1.92 |  |
| `mysticrift_pillagers_towers_aris:desert_pillager_tower` | 3 | 0.650021 | 180 | 355 | 0.26 |  |
| `mysticrift_pillagers_towers_aris:end_pillager_tower` | 3 | 0.019706 | 188 | 232 | 0.66 |  |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | 30 | 0.973476 | 128 | 91 | 1.96 |  |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | 15 | 0.038089 | 160 | 765 | 0.04 |  |
| `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` | 15 | 0.467934 | 127 | 190 | 0.45 |  |
| `creativeengineering:meta/pillager_camp_ruin` | 1 | 1.0 | - | 166 | new |  |
| `creativeengineering:meta/pillager_camp_ruins` | 1 | 1.0 | - | 160 | new |  |
| `creativeengineering:meta/pillager_camp_siege` | 1 | 1.0 | - | 138 | new |  |
| `creativeengineering:meta/pillager_great_tower_siege` | 1 | 1.0 | - | 214 | new |  |
| `creativeengineering:meta/pillager_stronghold_siege` | 1 | 1.0 | - | 105 | new |  |
| `creativeengineering:meta/pillager_tower_siege` | 1 | 1.0 | - | 88 | new |  |
| `takesapillage:bastille` | 34 | 1.0 | 55 | 124 | 0.20 |  |
| `takesapillage:pillager_camp` | 87 | 1.0 | 39 | 78 | 0.25 |  |
| `eugene:fake_trader_camp` | 100 | 1.0 | 200 | 78 | 6.68 |  |
| `eugene:pillager_camp` | 100 | 1.0 | 58 | 78 | 0.56 |  |
| `creativeengineering:meta/pillager_camp_meteorite` | 1 | 1.0 | - | 189 | new |  |

### `ruins` -> `creativeengineering:group/ruins`

Surface land ruins: Philip's Ruins and Terralith's glacial hut. Underground ruins stay standalone.

Grid: spacing 16, separation 8 (members never start closer than 9 chunks = 144 blocks apart). At most 1 structure per 16 x 16 chunks anywhere.

Capped (old combined density above the group grid): 62 biome(s), 62% of the group's area. Largest: `terralith:yellowstone` 1 per 10 -> 15, `minecraft:forest` 1 per 10 -> 15, `minecraft:plains` 1 per 10 -> 15, `terralith:highlands` 1 per 10 -> 15, `terralith:rocky_mountains` 1 per 10 -> 15, `minecraft:taiga` 1 per 10 -> 15.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `philipsruins:ancient_ruins` | 100 | 0.860538 | 50 | 46 | 1.18 |  |
| `philipsruins:ancient_towers` | 100 | 0.862964 | 50 | 46 | 1.18 |  |
| `philipsruins:badlands_structures` | 100 | 0.266459 | 58 | 35 | 2.73 |  |
| `philipsruins:desert_structures` | 100 | 0.250975 | 44 | 35 | 1.59 |  |
| `philipsruins:field_stone_ruins` | 100 | 1.0 | 75 | 64 | 1.37 |  |
| `philipsruins:field_stone_ruins_rocks` | 100 | 0.513101 | 68 | 68 | 1.00 |  |
| `philipsruins:level_one_ruins` | 100 | 1.0 | 75 | 64 | 1.37 |  |
| `philipsruins:level_three_ruins` | 100 | 1.0 | 85 | 64 | 1.76 |  |
| `philipsruins:level_two_ruins` | 100 | 1.0 | 113 | 64 | 3.11 |  |
| `philipsruins:level_two_ruins_pool` | 100 | 1.0 | 113 | 64 | 3.11 |  |
| `philipsruins:pumpkin_ruins` | 100 | 1.0 | 50 | 55 | 0.82 |  |
| `philipsruins:rare_ruin` | 100 | 0.007794 | 360 | 546 | 0.43 |  |
| `philipsruins:start_nether_ruin` | 100 | 0.8688 | 310 | 49 | 40.59 |  |
| `terralith:glacial_hut` | 100 | 0.686437 | 27 | 41 | 0.44 |  |
| `forest_ruins:ruin_1` | 100 | 0.904155 | 45 | 73 | 0.38 |  |
| `forest_ruins:ruin_2` | 100 | 0.904155 | 45 | 73 | 0.38 |  |
| `forest_ruins:ruin_3` | 100 | 0.904155 | 45 | 73 | 0.38 |  |
| `forest_ruins:ruin_4` | 100 | 0.904155 | 45 | 73 | 0.38 |  |
| `forest_ruins:ruin_5` | 100 | 0.904155 | 45 | 73 | 0.38 |  |
| `forest_ruins:ruin_6` | 100 | 0.904155 | 45 | 73 | 0.38 |  |
| `forest_ruins:ruin_7` | 100 | 0.904155 | 45 | 73 | 0.38 |  |
| `forgotten_church:forgotten_church` | 100 | 1.0 | 55 | 69 | 0.64 |  |
| `watchtower_building:abandoned_watchtower` | 21 | 0.194882 | 48 | 115 | 0.17 |  |
| `creativeengineering:meta/ruin_cluster` | 1 | 1.0 | - | 52 | new |  |
| `creativeengineering:meta/ruin_scatter` | 1 | 1.0 | - | 41 | new |  |
| `old_illager_statue:old_illager_statue` | 26 | 1.0 | 42 | 104 | 0.16 |  |
| `oldvillagerstatue:old_villager_statue` | 26 | 1.0 | 40 | 104 | 0.15 |  |
| `small_cat_statues:cat_1` | 19 | 1.0 | 30 | 120 | 0.06 |  |
| `small_cat_statues:cat_2` | 19 | 1.0 | 30 | 120 | 0.06 |  |
| `small_cat_statues:cat_3` | 19 | 1.0 | 30 | 120 | 0.06 |  |
| `eugene:ruined_drum_tower` | 100 | 1.0 | 101 | 71 | 2.05 |  |

### `surface_dungeons` -> `creativeengineering:group/surface_dungeons`

Surface dungeons: YUNG's desert and jungle temples, the vanilla desert pyramid and jungle temple (each in 10% of its YUNG's version's slots, "replaces" in structure_groups.json), the woodland mansion, Terralith's spire, the T&T desert mimic, Philip's badlands dungeon. The witch huts have their own group (group/witch_huts).

Grid: spacing 16, separation 10 (members never start closer than 11 chunks = 176 blocks apart). At most 1 structure per 16 x 16 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `betterjungletemples:jungle_temple` | 100 | 0.7481 | 24 | 19 | 1.51 |  |
| `minecraft:mansion` | 100 | 0.1191 | 80 | 48 | 2.75 |  |
| `philipsruins:badlands_start_dungeon` | 100 | 0.441692 | 58 | 26 | 4.95 |  |
| `terralith:spire` | 100 | 0.445566 | 36 | 24 | 2.26 |  |
| `towns_and_towers:mimic_desert` | 100 | 0.521 | 32 | 24 | 1.76 |  |
| `minecraft:jungle_pyramid` | 1 | 1.0 | 32 | 58 | 0.30 | replaces `betterjungletemples:jungle_temple` in 10% of its slots |
| `eugene:undead_castle` | 100 | 0.026314 | 93 | 100 | 0.87 |  |
| `creativeengineering:meta/jungle_temple_ruins` | 1 | 1.0 | - | 31 | new |  |

### `witch_huts` -> `creativeengineering:group/witch_huts`

Witch huts, swamps only (land/swamp): YUNG's witch hut and witch circle, the Villages & Pillages witch village, and the vanilla swamp hut as a replacement of YUNG's hut. Own grid (at least 144 blocks between huts), so they no longer compete with the surface dungeons.

Grid: spacing 16, separation 8 (members never start closer than 9 chunks = 144 blocks apart). At most 1 structure per 16 x 16 chunks anywhere.

Capped (old combined density above the group grid): 4 biome(s), 100% of the group's area. Largest: `minecraft:swamp` 1 per 15 -> 16, `terralith:orchid_swamp` 1 per 15 -> 16, `terralith:ice_marsh` 1 per 15 -> 16, `minecraft:mangrove_swamp` 1 per 15 -> 16.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `minecraft:swamp_hut` | 1 | 1.0 | 32 | 91 | 0.12 | replaces `betterwitchhuts:witch_hut` in 10% of its slots |
| `betterwitchhuts:witch_circle` | 100 | 0.9433 | 40 | 29 | 1.97 |  |
| `betterwitchhuts:witch_hut` | 100 | 0.935 | 30 | 30 | 0.99 |  |
| `villagesandpillages:village_witch` | 100 | 0.9374 | 24 | 29 | 0.70 |  |
| `creativeengineering:meta/witch_coven` | 1 | 1.0 | - | 45 | new |  |

### `entrance_dungeons` -> `creativeengineering:group/entrance_dungeons`

Surface-entrance dungeons, land only: YUNG's zombie dungeon (crypt) and spider dungeon (spider cave). Own grid (at least 272 blocks apart); the water rule's surface check keeps their entrances out of the sea.

Grid: spacing 32, separation 16 (members never start closer than 17 chunks = 272 blocks apart). At most 1 structure per 32 x 32 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `betterdungeons:spider_dungeon` | 100 | 0.9754 | 44 | 46 | 0.92 |  |
| `betterdungeons:zombie_dungeon` | 100 | 0.971 | 48 | 46 | 1.09 |  |

### `ocean_ruins` -> `creativeengineering:group/ocean_ruins`

Ocean ruins and wrecks: vanilla, Hopo's and Philip's ocean ruins, Philip's ocean fortress, vanilla shipwrecks, the T&T wreckage.

Grid: spacing 8, separation 3 (members never start closer than 4 chunks = 64 blocks apart). At most 1 structure per 8 x 8 chunks anywhere.

Capped (old combined density above the group grid): 2 biome(s), 21% of the group's area. Largest: `minecraft:deep_lukewarm_ocean` 1 per 7 -> 8, `minecraft:lukewarm_ocean` 1 per 7 -> 8.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `hopo:underwater_ruins` | 100 | 1.0 | 22 | 20 | 1.17 |  |
| `hopo:underwater_small_ruins` | 100 | 0.6268 | 13 | 25 | 0.27 |  |
| `hopo:underwater_warm_small_ruins` | 100 | 0.9701 | 10 | 20 | 0.25 |  |
| `minecraft:ocean_ruin_cold` | 100 | 0.627 | 20 | 25 | 0.65 |  |
| `minecraft:ocean_ruin_warm` | 100 | 0.9743 | 20 | 20 | 0.99 |  |
| `philipsruins:ocean_fortress` | 100 | 0.5047 | 70 | 29 | 5.96 |  |
| `philipsruins:ocean_ruins` | 100 | 0.4946 | 50 | 28 | 3.17 |  |
| `towns_and_towers:wreckage_ocean` | 100 | 0.3243 | 32 | 34 | 0.90 |  |
| `revamped_shipwrecks:shipwreck` | 100 | 1.0 | 24 | 20 | 1.48 |  |
| `revamped_shipwrecks:shipwreck_beached` | 100 | 0.4444 | 24 | 12 | 4.00 |  |
| `creativeengineering:meta/shipwreck_debris` | 1 | 1.0 | - | 35 | new |  |
| `creativeengineering:meta/shipwreck_graveyard` | 1 | 1.0 | - | 53 | new |  |
| `creativeengineering:meta/sunken_ruin_cluster` | 1 | 1.0 | - | 24 | new |  |
| `hopo:underwater_fossils` | 100 | 1.0 | 18 | 20 | 0.78 |  |
| `creativeengineering:meta/underwater_meteorite` | 1 | 1.0 | - | 54 | new |  |

### `ocean_dungeons` -> `creativeengineering:group/ocean_dungeons`

Ocean monuments, on the monuments' own grid (same spacing, spread and salt, so positions match vanilla). Monument meta structures join later.

Grid: spacing 32, separation 6 (members never start closer than 7 chunks = 112 blocks apart). At most 1 structure per 32 x 32 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `minecraft:monument` | 100 | 0.1 | 32 | 101 | 0.10 |  |
| `creativeengineering:meta/struck_monument` | 1 | 1.0 | - | 202 | new |  |

### `boats` -> `creativeengineering:group/boats`

Ships at sea: the Towns & Towers ship village and pillager ship. Meta structures join later.

Grid: spacing 48, separation 28 (members never start closer than 29 chunks = 464 blocks apart). At most 1 structure per 48 x 48 chunks anywhere.

Capped (old combined density above the group grid): 9 biome(s), 100% of the group's area. Largest: `minecraft:deep_ocean` 1 per 17 -> 44, `minecraft:deep_cold_ocean` 1 per 16 -> 44, `minecraft:deep_lukewarm_ocean` 1 per 17 -> 44, `minecraft:ocean` 1 per 17 -> 43, `minecraft:cold_ocean` 1 per 17 -> 43, `minecraft:lukewarm_ocean` 1 per 18 -> 43.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `towns_and_towers:pillager_outpost_ocean` | 100 | 0.9618 | 110 | 141 | 0.61 |  |
| `towns_and_towers:village_ocean` | 100 | 0.9588 | 53 | 141 | 0.14 |  |
| `antiquetradingship:antique_trading_ship` | 100 | 1.0 | 36 | 124 | 0.08 |  |
| `blocky_siege:pillager_ship` | 100 | 1.0 | 120 | 131 | 0.84 |  |
| `illager_viking_longship:illager_longship` | 100 | 1.0 | 45 | 116 | 0.15 |  |
| `illagerwarship:warship` | 100 | 1.0 | 36 | 124 | 0.08 |  |
| `unwrecked_ships:ship` | 100 | 1.0 | 60 | 135 | 0.20 |  |
| `villager_rafts:raft_1` | 100 | 1.0 | 50 | 135 | 0.14 |  |
| `villager_rafts:raft_2` | 100 | 1.0 | 50 | 135 | 0.14 |  |
| `villager_rafts:raft_3` | 100 | 1.0 | 50 | 135 | 0.14 |  |
| `creativeengineering:meta/pillager_fleet` | 1 | 1.0 | - | 141 | new |  |
| `creativeengineering:meta/raft_convoy` | 1 | 1.0 | - | 142 | new |  |

### `flotsam` -> `creativeengineering:group/flotsam`

Floating debris at sea: the driftwood flotilla (meta structure). Its own grid, so it takes no slots from ruins or ships.

Grid: spacing 32, separation 8 (members never start closer than 9 chunks = 144 blocks apart). At most 1 structure per 32 x 32 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `creativeengineering:meta/driftwood_flotilla` | 100 | 1.0 | - | 32 | new |  |

### `deadwood` -> `creativeengineering:group/deadwood`

Patches of dead wood on land: stumps and fallen logs by wood type (meta structures). Its own grid, so it takes no slots from buildings or ruins.

Grid: spacing 16, separation 5 (members never start closer than 6 chunks = 96 blocks apart). At most 1 structure per 16 x 16 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `creativeengineering:meta/deadwood_acacia` | 100 | 1.0 | - | 16 | new |  |
| `creativeengineering:meta/deadwood_birch` | 100 | 1.0 | - | 20 | new |  |
| `creativeengineering:meta/deadwood_cherry` | 100 | 1.0 | - | 16 | new |  |
| `creativeengineering:meta/deadwood_jungle` | 100 | 1.0 | - | 16 | new |  |
| `creativeengineering:meta/deadwood_oak` | 100 | 1.0 | - | 20 | new |  |
| `creativeengineering:meta/deadwood_spruce` | 100 | 1.0 | - | 16 | new |  |

### `remote` -> `creativeengineering:group/remote`

Lone camps and cabins: the T&T wandering trader camp and every single-building house mod in the Overworld. The two Farmer's Structures houses whose biomes come from mods this pack does not have (Aether, Undergarden) stay out.

Grid: spacing 24, separation 12 (members never start closer than 13 chunks = 208 blocks apart). At most 1 structure per 24 x 24 chunks anywhere.

Capped (old combined density above the group grid): 78 biome(s), 78% of the group's area. Largest: `terralith:yellowstone` 1 per 10 -> 23, `minecraft:plains` 1 per 8 -> 22, `minecraft:forest` 1 per 9 -> 24, `terralith:highlands` 1 per 8 -> 22, `terralith:rocky_mountains` 1 per 8 -> 22, `minecraft:meadow` 1 per 7 -> 22.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `beautify:botanist_house_desert` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_plains` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_savanna` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_snowy` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `beautify:botanist_house_taiga` | 100 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `farmers_structures:aquacultures_beach` | 100 | 0.0307 | 86 | 371 | 0.05 |  |
| `farmers_structures:aquacultures_common` | 100 | 1.0 | 63 | 100 | 0.41 |  |
| `farmers_structures:cooks` | 100 | 0.417675 | 53 | 146 | 0.13 |  |
| `farmers_structures:cooks_moss` | 100 | 1.0 | - | - | - | no biome in this pack |
| `farmers_structures:sushis` | 100 | 0.1058 | 71 | 74 | 0.94 |  |
| `greek_villager_house:greek_house` | 100 | 1.0 | 45 | 68 | 0.44 |  |
| `hobbit_house_:hobbit_house` | 100 | 1.0 | 45 | 116 | 0.15 |  |
| `minecraft:igloo` | 100 | 0.502816 | 32 | 112 | 0.08 |  |
| `old_swamp_fisherman_house:fisherman_house` | 100 | 0.147601 | 40 | 125 | 0.10 |  |
| `sawmillhouse:sawmill` | 100 | 1.0 | 33 | 115 | 0.08 |  |
| `simply_houses:forest_chapel` | 100 | 0.120438 | 171 | 319 | 0.29 |  |
| `simply_houses:forest_fort` | 100 | 0.120438 | 171 | 319 | 0.29 |  |
| `simply_houses:forest_house` | 100 | 0.1196 | 121 | 320 | 0.14 |  |
| `simply_houses:forest_stonemason` | 100 | 0.1196 | 121 | 320 | 0.14 |  |
| `simply_houses:plains_house` | 100 | 1.0 | 85 | 131 | 0.42 |  |
| `simply_houses:plains_mansion` | 100 | 1.0 | 85 | 131 | 0.42 |  |
| `simply_houses:taiga_forge` | 100 | 0.266606 | 122 | 193 | 0.40 |  |
| `simply_houses:taiga_house` | 100 | 0.266606 | 122 | 193 | 0.40 |  |
| `simply_houses:taiga_inn` | 100 | 0.266606 | 87 | 193 | 0.20 |  |
| `simply_houses:taiga_woodcutter` | 100 | 0.266606 | 87 | 193 | 0.20 |  |
| `small_viking_villager_house:small_viking_house` | 100 | 1.0 | 48 | 100 | 0.23 |  |
| `small_villager_greenhouse:small_villager_greenhouse` | 100 | 1.0 | 45 | 124 | 0.13 |  |
| `towns_and_towers:exclusives/village_wandering_trader_camp` | 100 | 0.36673 | 62 | 76 | 0.66 |  |
| `villager_butcher_house:butcher_house` | 100 | 1.0 | 43 | 127 | 0.12 |  |
| `villager_cartographer_house:villager_cartographer_house` | 100 | 1.0 | 42 | 127 | 0.11 |  |
| `villager_cleric_house:villager_cleric_house` | 100 | 1.0 | 39 | 127 | 0.09 |  |
| `villager_country_house:country_house` | 100 | 0.166203 | 50 | 166 | 0.09 |  |
| `villager_farmer_house:villager_farmer_house` | 100 | 1.0 | 47 | 127 | 0.14 |  |
| `villager_fletcher_house:villager_fletcher_house` | 100 | 1.0 | 38 | 127 | 0.09 |  |
| `villager_flower_house:flower_house` | 100 | 0.273887 | 45 | 223 | 0.04 |  |
| `villager_tannery:tannery` | 100 | 1.0 | 45 | 119 | 0.14 |  |
| `ancient_enchantment_temple:temple` | 100 | 0.1264 | 45 | 95 | 0.22 |  |
| `animal_barn:barn` | 100 | 1.0 | 45 | 131 | 0.12 |  |
| `bighorsestable:big_horse_stable` | 100 | 1.0 | 40 | 119 | 0.11 |  |
| `desert_village_temple:village_temple` | 100 | 0.293336 | 45 | 77 | 0.34 |  |
| `desert_watchtower:watchtower` | 110 | 0.342886 | 45 | 75 | 0.36 |  |
| `forest_watchtower:forest_watchtower` | 135 | 0.954795 | 40 | 88 | 0.21 |  |
| `gothic_church:church` | 100 | 1.0 | 45 | 119 | 0.14 |  |
| `llama_stable:llama_stable` | 100 | 1.0 | 45 | 78 | 0.33 |  |
| `mini_clock_tower:mini_clock_tower` | 108 | 0.740947 | 43 | 85 | 0.26 |  |
| `plains_villager_tents:big_villager_tent` | 100 | 1.0 | 45 | 131 | 0.12 |  |
| `saloon:saloon` | 100 | 0.43779 | 28 | 61 | 0.21 |  |
| `smallhorsestable:horse_stable` | 100 | 1.0 | 34 | 124 | 0.08 |  |
| `snowy_tents:snowy_tent` | 100 | 1.0 | 23 | 80 | 0.08 |  |
| `villager_archer_camp:villager_archer_camp` | 100 | 1.0 | 45 | 124 | 0.13 |  |
| `villager_circus_tent:villager_circus_tent` | 100 | 1.0 | 45 | 124 | 0.13 |  |
| `villager_fish_shop:fish_shop` | 100 | 1.0 | 45 | 65 | 0.48 |  |
| `villager_fishermans_hut:villager_fishermans_hut` | 100 | 1.0 | 42 | 127 | 0.11 |  |
| `villager_forge:villager_forge` | 100 | 1.0 | 45 | 127 | 0.13 |  |
| `villager_gold_washing_facility:villager_gold_washing_facility` | 100 | 1.0 | 50 | 41 | 1.51 |  |
| `villager_windmill:windmill` | 100 | 1.0 | 50 | 127 | 0.16 |  |
| `watchtower_building:fresh_watchtower` | 160 | 1.0 | 67 | 79 | 0.71 |  |
| `creativeengineering:meta/farmers_camp` | 1 | 1.0 | - | 137 | new |  |
| `creativeengineering:meta/house_camp` | 1 | 1.0 | - | 101 | new |  |
| `creativeengineering:meta/waysign_camps` | 1 | 1.0 | - | 128 | new |  |
| `creativeengineering:meta/waysign_house_camp` | 1 | 1.0 | - | 124 | new |  |
| `campsite_structures:armorer_campsite` | 60 | 1.0 | 32 | 161 | 0.04 |  |
| `campsite_structures:butcher_campsite` | 60 | 1.0 | 32 | 161 | 0.04 |  |
| `campsite_structures:cartographer_campsite` | 60 | 1.0 | 32 | 161 | 0.04 |  |
| `campsite_structures:cleric_campsite` | 60 | 1.0 | 32 | 161 | 0.04 |  |
| `campsite_structures:farmer_campsite` | 60 | 1.0 | 32 | 161 | 0.04 |  |
| `campsite_structures:fisher_campsite` | 15 | 1.0 | 32 | 168 | 0.04 |  |
| `eugene:camp` | 100 | 1.0 | 59 | 99 | 0.35 |  |
| `farmers_structures:aquacultures_cod` | 100 | 1.0 | 103 | 100 | 1.07 |  |
| `farmers_structures:cacaos` | 100 | 0.1561 | 86 | 86 | 1.00 |  |
| `farmers_structures:fishs_stew` | 100 | 1.0 | 48 | 80 | 0.36 |  |
| `farmers_structures:ratatouilles` | 100 | 1.0 | 58 | 48 | 1.46 |  |
| `farmers_structures:shepherd_farms` | 100 | 1.0 | 186 | 95 | 3.89 |  |
| `farmers_structures:squids` | 100 | 1.0 | 50 | 116 | 0.19 |  |
| `creativeengineering:meta/trader_caravan` | 1 | 1.0 | - | 193 | new |  |

### `sky` -> `creativeengineering:group/sky`

Sky structures: Cloud Storage's hot air balloons and sky temples, AE2 meteorites (same rate as the clouds). Meta structures join later.

Grid: spacing 64, separation 16 (members never start closer than 17 chunks = 272 blocks apart). At most 1 structure per 64 x 64 chunks anywhere.

Capped (old combined density above the group grid): 145 biome(s), 100% of the group's area. Largest: `terralith:cave/deep_caves` 1 per 20 -> 57, `minecraft:lush_caves` 1 per 20 -> 57, `minecraft:deep_ocean` 1 per 19 -> 59, `minecraft:deep_cold_ocean` 1 per 19 -> 59, `minecraft:deep_lukewarm_ocean` 1 per 19 -> 59, `terralith:cave/andesite_caves` 1 per 20 -> 57.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `ae2:meteorite` | 100 | 0.985 | 32 | 121 | 0.07 |  |
| `cloudstorage:big_balloon` | 100 | 0.985 | 32 | 121 | 0.07 |  |
| `cloudstorage:sky_temple` | 100 | 0.985 | 40 | 121 | 0.11 |  |
| `skyvillages:skyvillage` | 100 | 1.0 | 70 | 128 | 0.30 |  |
| `creativeengineering:meta/balloon_fleet` | 1 | 1.0 | - | 222 | new |  |
| `creativeengineering:meta/cloud_balloons` | 1 | 1.0 | - | 272 | new |  |
| `creativeengineering:meta/cloud_cluster` | 1 | 1.0 | - | 222 | new |  |
| `creativeengineering:meta/sky_village_islands` | 1 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |
| `creativeengineering:meta/meteor_shower` | 1 | 1.0 | - | - | - | empty biome tag: never spawns unless a mod fills it at runtime |

### `nether_ruins` -> `creativeengineering:group/nether_ruins`

Nether ruins: Twigs' bloodstone obelisk and Philip's three Nether ruins (Nether dungeon, sealed ghast statue, lava ruins). Grid from their crowding (spacing 14, separation 7). Added 2026-10-12.

Grid: spacing 14, separation 7 (members never start closer than 8 chunks = 128 blocks apart). At most 1 structure per 14 x 14 chunks anywhere.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `philipsruins:nether_lava_ruins` | 100 | 0.5993 | 52 | 34 | 2.31 |  |
| `philipsruins:sealedruinedghaststatue1` | 100 | 0.5993 | 52 | 34 | 2.31 |  |
| `philipsruins:start_nether_dungeon` | 100 | 0.5993 | 52 | 34 | 2.31 |  |
| `twigs:bloodstone_obelisk` | 100 | 1.0 | 20 | 28 | 0.51 |  |

### `way_signs` -> `creativeengineering:group/way_signs`

Supplementaries' way signs on a grid of their own (2026-10-12): the old set's placement (19/10, kept 8 chunks off settlement grid slots), so the rate is unchanged. Each sign points at the nearest #supplementaries:way_sign_destinations, which structure_overrides.js fills with every group/settlements member (villages, fortified villages, meta/village_towers).

Grid: spacing 19, separation 10 (members never start closer than 11 chunks = 176 blocks apart). At most 1 structure per 19 x 19 chunks anywhere. Excluded within 8 chunks of `creativeengineering:group/settlements` grid slots.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `supplementaries:way_sign` | 100 | 1.0 | 32 | 20 | 2.52 |  |

### `vehicles` -> `creativeengineering:group/vehicles`

Vehicles (hostile, 2026-10-14, plan 7.4.5): the 6 Illager Siege Weapons camps (moved out of pillager_settlements), the pillager loot carriage (with the villager chase and the golem prison van) and the witch wagon. Rarer than camps, more common than outposts, twice as common in harsh biomes (biome_frequency). Grid from the harsh rate (spacing 52: about 1 vehicle per 54 chunks in harsh biomes, 1 per 75 elsewhere, 2026-10-14); equal weights. Rates are hand-set (fit_base = the same values).

Grid: spacing 52, separation 16 (members never start closer than 17 chunks = 272 blocks apart). At most 1 structure per 52 x 52 chunks anywhere. Excluded within 4 chunks of `creativeengineering:group/settlements` grid slots.

Capped (old combined density above the group grid): 75 biome(s), 95% of the group's area. Largest: `terralith:yellowstone` 1 per 20 -> 75, `minecraft:plains` 1 per 20 -> 73, `minecraft:dark_forest` 1 per 19 -> 53, `terralith:lush_desert` 1 per 20 -> 52, `terralith:brushland` 1 per 20 -> 73, `terralith:highlands` 1 per 20 -> 75.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `eugene:pillager_loot_carriage` | 100 | 1.0 | 128 | 159 | 0.65 |  |
| `eugene:witch_wagon` | 200 | 0.76 | 60 | 86 | 0.48 |  |
| `illager_siege_weapons:ballista` | 100 | 1.0 | 50 | 165 | 0.09 |  |
| `illager_siege_weapons:battering_ram` | 100 | 1.0 | 50 | 165 | 0.09 |  |
| `illager_siege_weapons:catapult` | 100 | 1.0 | 50 | 165 | 0.09 |  |
| `illager_siege_weapons:reinforced_battering_ram` | 100 | 1.0 | 50 | 165 | 0.09 |  |
| `illager_siege_weapons:siege_tower` | 100 | 1.0 | 50 | 165 | 0.09 |  |
| `illager_siege_weapons:trebuchet` | 100 | 1.0 | 50 | 165 | 0.09 |  |
| `creativeengineering:meta/pillager_caravan` | 1 | 1.0 | - | 269 | new |  |
| `creativeengineering:meta/witch_cart_caravan` | 1 | 1.0 | - | 158 | new |  |

### `desert_temples` -> `creativeengineering:group/desert_temples`

Desert temples: YUNG's desert temple and the vanilla desert pyramid, which takes a share of YUNG's temple slots ("replaces" in structure_groups.json). Own grid with twice the surface dungeons' minimum distance (structures/rates-and-groups #10).

Grid: spacing 32, separation 20 (members never start closer than 21 chunks = 336 blocks apart). At most 1 structure per 32 x 32 chunks anywhere.

Capped (old combined density above the group grid): 9 biome(s), 100% of the group's area. Largest: `terralith:lush_desert` 1 per 24 -> 52, `terralith:red_oasis` 1 per 24 -> 52, `terralith:desert_canyon` 1 per 24 -> 52, `minecraft:desert` 1 per 24 -> 52, `terralith:desert_spires` 1 per 24 -> 52, `terralith:desert_oasis` 1 per 24 -> 52.

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `minecraft:desert_pyramid` | 1 | 1.0 | 32 | 268 | 0.01 | replaces `betterdeserttemples:desert_temple` in 5% of its slots |
| `betterdeserttemples:desert_temple` | 100 | 0.285 | 35 | 64 | 0.29 |  |
| `creativeengineering:meta/meteorite_desert_pyramid` | 1 | 1.0 | - | 212 | new | replaces `betterdeserttemples:desert_temple` in 8% of its slots |
| `creativeengineering:meta/desert_temple_ruins` | 1 | 1.0 | - | 101 | new |  |

## Standalone sets (changed ones only)

| Structure | Weight | Frequency | Mod default 1 per | New 1 per | Ratio | Note |
|---|---|---|---|---|---|---|
| `farmers_structures:aethers` | 1 | 1.0 (off) | - | - | - | no biome in this pack |
| `farmers_structures:cooks_nether_1` | 1 | 1.0 | 40 | 38 | 1.07 |  |
| `farmers_structures:cooks_nether_2` | 1 | 1.0 | 49 | 47 | 1.07 |  |
| `farmers_structures:undergardens_sea` | 1 | 1.0 (off) | - | - | - | no biome in this pack |
| `hearths:crimson_tower` | 1 | 1.0 | 16 | 12 | 1.66 |  |
| `hearths:warped_tower` | 1 | 1.0 | 16 | 12 | 1.66 |  |
| `mes:starlight_voyager` | 1 | 1.0 | 31 | 27 | 1.33 |  |
| `minecraft:ancient_city` | 1 | 1.0 | 24 | 20 | 1.50 |  |
| `minecraft:buried_treasure` | 1 | 1.0 | 10 | 8 | 1.50 |  |
| `minecraft:ruined_portal` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_desert` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_jungle` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_swamp` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_mountain` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_ocean` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:ruined_portal_nether` | 1 | 1.0 (off) | 40 | - | 0.00 |  |
| `minecraft:stronghold` | 1 | 1.0 | - | - | - | blocked in code |
| `nullscape:dragon_skeleton` | 1 | 1.0 | 22 | 25 | 0.75 |  |
| `philipsruins:ancient_crypt` | 1 | 1.0 | 80 | 57 | 2.00 |  |
| `philipsruins:ancient_dungeon` | 1 | 1.0 | 10 | 7 | 2.00 |  |
| `philipsruins:antiquus_crypta` | 1 | 1.0 | 90 | 64 | 2.00 |  |
| `philipsruins:end_ruins` | 1 | 1.0 | 49 | 35 | 2.00 |  |
| `philipsruins:end_gate_fortress` | 1 | 1.0 | 49 | 35 | 2.00 |  |
| `philipsruins:lost_soul_city` | 1 | 1.0 | 80 | 38 | 4.50 |  |
| `philipsruins:lost_soul_dungeon` | 1 | 0.991 | 120 | 46 | 6.74 |  |
| `philipsruins:bone_dungeon` | 1 | 0.29382 | 61 | 43 | 2.00 |  |
| `philipsruins:underground_structures` | 1 | 0.9885 | 120 | 46 | 6.73 |  |
| `philipsruins:sculk_dungeon` | 1 | 0.991 | 120 | 46 | 6.74 |  |
| `terralith:underground/oak_cabin` | 11 | 1.0 | 16 | 4 | 13.44 |  |
| `terralith:underground/giant_bee_hive` | 2 | 0.4261 | 34 | 14 | 5.73 |  |
| `terralith:underground/mining_outpost` | 13 | 0.1097 | 13 | 11 | 1.47 |  |
| `terralith:underground/old_refinery` | 5 | 0.1166 | 18 | 14 | 1.57 |  |
| `terralith:underground/sunken_tower` | 5 | 0.4277 | 22 | 9 | 5.75 |  |
| `terralith:underground/witch_hut` | 4 | 1.0 | 23 | 6 | 13.44 |  |
| `unwrecked_ships:nether_ship` | 1 | 1.0 (off) | 60 | - | 0.00 |  |

