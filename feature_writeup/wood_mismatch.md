# Wood mismatch: structures built of wood foreign to their biomes

Written by `python3 tools/wood_mismatch_scan.py`. Do not edit. Every live land structure; wood counted over every template it can place; native woods per biome from the wood fits in `biome_fits.json`. **Wood** = wood blocks / all blocks. **Mismatch** = share of its wood that is foreign where it spawns (area-weighted). **Keep** = share of its land area where its main wood is native (what restricting it to that wood keeps). Verdicts: **restrict** (to its main wood fit), **swap** (keep the biomes, swap the wood per biome: settlements, forts, or a restriction that would keep under 35%), **trim** (main wood native almost everywhere, only trim woods foreign), **woodless** (mostly biomes with no native tree: deserts, mushroom fields), **unchanged** (little wood or little mismatch).

## Restrict (35)

### Pillager towers and camps (10)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `illager_siege_weapons:catapult` | pillager_settlements | plains + highlands + dark_forest | 90% | spruce 101, dark_oak 25 | 58% | 51% | T:brushland, savanna, desert, badlands |
| `illager_siege_weapons:battering_ram` | pillager_settlements | plains + highlands + dark_forest | 59% | oak 48, spruce 24 | 57% | 39% | savanna, T:yellowstone, desert, badlands |
| `illager_siege_weapons:trebuchet` | pillager_settlements | plains + highlands + dark_forest | 90% | spruce 144, oak 28, dark_oak 8 | 53% | 51% | T:brushland, savanna, desert, badlands |
| `illager_siege_weapons:ballista` | pillager_settlements | plains + highlands + dark_forest | 88% | spruce 65, oak 14, dark_oak 2 | 52% | 51% | T:brushland, savanna, desert, badlands |
| `illager_siege_weapons:reinforced_battering_ram` | pillager_settlements | plains + highlands + dark_forest | 97% | spruce 218, oak 47 | 51% | 51% | T:brushland, savanna, desert, badlands |
| `illager_siege_weapons:siege_tower` | pillager_settlements | plains + highlands + dark_forest | 81% | spruce 361, oak 60 | 51% | 51% | T:brushland, savanna, desert, badlands |
| `towns_and_towers:pillager_outpost_flower_forest` | pillager_settlements | flower | 64% | spruce 936, oak 14, jungle 6 | 51% | 49% | T:moonlight_valley, T:blooming_valley, T:sakura_grove, flower_forest |
| `towns_and_towers:pillager_outpost_birch_forest` | pillager_settlements | birch | 70% | oak 1521, birch 195, spruce 54 | 44% | 51% | T:yellowstone, old_growth_birch_forest, birch_forest, T:birch_taiga |
| `towns_and_towers:pillager_outpost_forest` | pillager_settlements | forest | 18% | oak 353, dark_oak 6, spruce 3 | 27% | 75% | old_growth_birch_forest, T:moonlight_valley, birch_forest, T:sakura_grove |
| `towns_and_towers:pillager_outpost_sunflower_plains` | pillager_settlements | plains + flower | 34% | spruce 600, oak 128 | 27% | 70% | T:hot_shrubland, T:moonlight_valley, T:blooming_valley, T:sakura_grove |

### Towers (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `philipsruins:ancient_towers` | ruins | plains + highlands + forest | 15% | spruce 288, oak 75, birch 24 | 55% | 46% | T:brushland, savanna, forest, dark_forest |

### Houses and lone buildings (11)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `greek_villager_house:greek_house` | remote | savanna + warm_dry | 35% | oak 203, spruce 144, jungle 5 | 73% | 47% | savanna, T:brushland, savanna_plateau, T:arid_highlands |
| `hobbit_house_:hobbit_house` | remote | forest + flower + dark_forest | 51% | spruce 105, oak 99, jungle 88, dark_oak 49 | 70% | 56% | T:yellowstone, forest, dark_forest, old_growth_spruce_taiga |
| `farmers_structures:aquacultures_carp` | remote | plains + highlands + forest | 73% | oak 228, dark_oak 169, spruce 9 | 66% | 52% | T:yellowstone, savanna, forest, T:brushland |
| `farmers_structures:cooks` | remote | plains + highlands + forest | 43% | oak 1589, dark_oak 1530, spruce 757, birch 3 | 63% | 71% | savanna, forest, T:brushland, savanna_plateau |
| `gothic_church:church` | remote | plains + highlands + forest | 34% | spruce 541, dark_oak 283 | 53% | 65% | forest, T:yellowstone, plains, dark_forest |
| `farmers_structures:aquacultures_common` | remote | plains + highlands + forest | 66% | spruce 195, oak 77, dark_oak 3 | 51% | 48% | savanna, forest, T:brushland, dark_forest |
| `villager_tannery:tannery` | remote | plains + highlands + forest | 56% | spruce 258, dark_oak 88, oak 24, jungle 10 | 50% | 65% | forest, T:yellowstone, dark_forest, plains |
| `sawmillhouse:sawmill` | remote | forest + dark_forest + taiga | 81% | spruce 667, oak 63 | 49% | 53% | forest, dark_forest, old_growth_birch_forest, T:temperate_highlands |
| `smallhorsestable:horse_stable` | remote | plains + forest | 78% | oak 82, spruce 58, dark_oak 50 | 48% | 86% | forest, plains, old_growth_birch_forest, T:moonlight_valley |
| `villager_flower_house:flower_house` | remote | flower | 60% | spruce 678, oak 168, dark_oak 7, jungle 6 | 48% | 49% | T:moonlight_valley, T:blooming_valley, T:sakura_grove, flower_forest |
| `small_villager_greenhouse:small_villager_greenhouse` | remote | plains + forest | 74% | oak 158, spruce 88, dark_oak 15 | 33% | 86% | forest, old_growth_birch_forest, T:moonlight_valley, birch_forest |

### Camps (2)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `villager_circus_tent:villager_circus_tent` | remote | plains + forest | 45% | spruce 286, oak 203, mangrove 157, dark_oak 25 | 55% | 45% | forest, plains, old_growth_birch_forest, T:hot_shrubland |
| `villager_archer_camp:villager_archer_camp` | remote | plains + forest | 70% | spruce 152, oak 74 | 42% | 45% | forest, old_growth_birch_forest, T:hot_shrubland, T:moonlight_valley |

### Ruins (8)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `terralith:glacial_hut` | ruins | icy | 80% | spruce 528, birch 453, dark_oak 67 | 80% | 40% | frozen_peaks, snowy_plains, T:glacial_chasm, T:ice_marsh |
| `philipsruins:level_two_ruins_pool` | ruins | plains + highlands + forest | 11% | spruce 118, dark_oak 100, oak 67 | 63% | 54% | forest, T:yellowstone, plains, dark_forest |
| `philipsruins:pumpkin_ruins` | ruins | plains + highlands + forest | 25% | spruce 156, acacia 98, dark_oak 18, oak 6 | 60% | 65% | forest, T:yellowstone, dark_forest, plains |
| `philipsruins:ancient_ruins` | ruins | plains + highlands + forest | 11% | spruce 377, oak 209, acacia 4 | 54% | 46% | savanna, forest, T:brushland, dark_forest |
| `philipsruins:level_three_ruins` | ruins | plains + highlands + forest | 21% | spruce 1377, oak 1276, dark_oak 199, birch 49 | 52% | 54% | forest, T:yellowstone, dark_forest, sparse_jungle |
| `philipsruins:level_two_ruins` | ruins | plains + highlands + forest | 14% | oak 488, spruce 263, dark_oak 48 | 52% | 49% | T:yellowstone, dark_forest, forest, sparse_jungle |
| `philipsruins:level_one_ruins` | ruins | plains + highlands + forest | 11% | spruce 308, oak 87, birch 33, dark_oak 7 | 49% | 54% | forest, dark_forest, sparse_jungle, jungle |
| `forgotten_church:forgotten_church` | ruins | plains + highlands + forest | 21% | spruce 165, dark_oak 23 | 41% | 65% | forest, dark_forest, T:hot_shrubland, old_growth_birch_forest |

### Other (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `villagesandpillages:village_witch` | witch_huts | swamp | 72% | oak 1807, spruce 1422 | 74% | 46% | mangrove_swamp, swamp, T:orchid_swamp, T:ice_marsh |
| `betterwitchhuts:witch_hut` | witch_huts | swamp | 92% | oak 624, spruce 295, crimson 2 | 69% | 46% | mangrove_swamp, swamp, T:orchid_swamp, T:ice_marsh |
| `takesapillage:bastille` | pillager_settlements | plains + highlands + taiga | 28% | spruce 10589, dark_oak 2861, birch 764, oak 20 | 59% | 54% | T:brushland, savanna, desert, badlands |

## Swap (36)

### Villages and towns (12)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:village_swamp` | settlements | swamp | 66% | dark_oak 2554, spruce 1928, oak 7, mangrove 3 | 100% | 0% | mangrove_swamp, swamp, T:orchid_swamp, T:ice_marsh |
| `towns_and_towers:exclusives/village_mediterranean` | settlements | warm_dry | 13% | spruce 749, birch 167, dark_oak 13, oak 11 | 99% | 0% | T:brushland, savanna_plateau, T:hot_shrubland, T:arid_highlands |
| `towns_and_towers:village_sparse_jungle` | settlements | jungle | 73% | spruce 1479, oak 1269, dark_oak 147, jungle 93 | 97% | 0% | sparse_jungle, jungle, T:tropical_jungle, T:rocky_jungle |
| `towns_and_towers:village_badlands` | settlements | badlands | 25% | spruce 740, oak 614, dark_oak 458, jungle 64 | 91% | 13% | badlands, eroded_badlands, T:savanna_badlands, wooded_badlands |
| `towns_and_towers:village_wooded_badlands` | settlements | badlands | 38% | spruce 572, oak 75, jungle 18 | 87% | 13% | badlands, eroded_badlands, wooded_badlands, T:savanna_badlands |
| `towns_and_towers:exclusives/village_swedish` | settlements | taiga + snowy | 36% | mangrove 469, dark_oak 438, birch 361, spruce 45 | 84% | 0% | T:yellowstone, old_growth_spruce_taiga, taiga, T:forested_highlands |
| `towns_and_towers:village_meadow` | settlements | plains + flower | 58% | dark_oak 2271, spruce 1721, oak 95, birch 2 | 68% | 1% | plains, T:hot_shrubland, T:moonlight_valley, meadow |
| `towns_and_towers:village_snowy_slopes` | settlements | snowy | 72% | dark_oak 524, spruce 377, oak 49 | 63% | 0% | snowy_plains, snowy_taiga, T:cold_shrubland, T:wintry_forest |
| `towns_and_towers:village_flower_forest` | settlements | flower | 38% | spruce 3143, acacia 173, birch 129, oak 35 | 53% | 49% | T:moonlight_valley, T:blooming_valley, T:sakura_grove, flower_forest |
| `towns_and_towers:village_old_growth_taiga` | settlements | highlands + taiga | 75% | dark_oak 1929, spruce 1656, oak 71, birch 2 | 52% | 6% | T:yellowstone, old_growth_spruce_taiga, taiga, T:highlands |
| `minecraft:village_taiga` | settlements | highlands + taiga | 49% | oak 6474, spruce 5882 | 36% | 34% | T:yellowstone, old_growth_spruce_taiga, taiga, T:forested_highlands |
| `towns_and_towers:village_sunflower_plains` | settlements | plains + flower | 54% | spruce 929, oak 790 | 22% | 70% | T:moonlight_valley, T:hot_shrubland, T:sakura_grove, T:blooming_valley |

### Pillager forts (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_badlands` | pillager_settlements | badlands | 27% | oak 404, spruce 7 | 87% | 13% | badlands, T:bryce_canyon, eroded_badlands, T:savanna_badlands |
| `towns_and_towers:pillager_outpost_jungle` | pillager_settlements | jungle | 35% | dark_oak 905, oak 425, spruce 406, jungle 252 | 87% | 0% | sparse_jungle, jungle, T:tropical_jungle, T:rocky_jungle |
| `towns_and_towers:exclusives/pillager_outpost_mediterranean` | pillager_settlements | warm_dry | 31% | oak 315, spruce 224 | 58% | 72% | T:brushland, savanna_plateau, T:arid_highlands, T:hot_shrubland |

### Pillager towers and camps (7)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_swamp` | pillager_settlements | swamp | 66% | dark_oak 748, spruce 287, oak 6 | 100% | 0% | mangrove_swamp, swamp, T:orchid_swamp, T:ice_marsh |
| `towns_and_towers:pillager_outpost_sparse_jungle` | pillager_settlements | jungle | 67% | spruce 404, oak 32, jungle 20, acacia 4 | 96% | 0% | sparse_jungle, jungle, T:tropical_jungle, T:rocky_jungle |
| `towns_and_towers:pillager_outpost_wooded_badlands` | pillager_settlements | badlands | 64% | spruce 414, oak 50 | 87% | 13% | badlands, eroded_badlands, wooded_badlands, T:savanna_badlands |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | pillager_settlements | taiga + snowy | 56% | dark_oak 957, spruce 163, oak 10 | 81% | 6% | T:yellowstone, old_growth_spruce_taiga, taiga, T:shield |
| `towns_and_towers:pillager_outpost_snowy_slopes` | pillager_settlements | snowy | 31% | dark_oak 712, spruce 309, oak 2 | 73% | 0% | snowy_plains, snowy_taiga, T:cold_shrubland, T:wintry_forest |
| `towns_and_towers:pillager_outpost_snowy_plains` | pillager_settlements | snowy | 25% | dark_oak 413, spruce 279, oak 1 | 64% | 0% | snowy_plains, snowy_taiga, T:cold_shrubland, T:wintry_forest |
| `minecraft:mansion` | surface_dungeons | dark_forest | 65% | birch 6580, dark_oak 2476, oak 1776 | 62% | 20% | dark_forest, T:siberian_taiga, T:lavender_valley, T:mirage_isles |

### Houses and lone buildings (12)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `old_swamp_fisherman_house:fisherman_house` | remote | swamp | 78% | dark_oak 389, spruce 318, oak 27 | 98% | 0% | mangrove_swamp, swamp, T:orchid_swamp, T:ice_marsh |
| `villager_country_house:country_house` | remote | savanna + warm_dry | 23% | spruce 472, dark_oak 250, acacia 36 | 95% | 0% | T:brushland, savanna, savanna_plateau, T:hot_shrubland |
| `saloon:saloon` | remote | badlands | 87% | spruce 673, oak 564, dark_oak 444 | 90% | 13% | badlands, eroded_badlands, T:savanna_badlands, wooded_badlands |
| `villager_gold_washing_facility:villager_gold_washing_facility` | remote | badlands | 66% | spruce 314, oak 78, dark_oak 10, acacia 1 | 87% | 13% | badlands, eroded_badlands, wooded_badlands, T:savanna_badlands |
| `llama_stable:llama_stable` | remote | highlands + savanna | 65% | spruce 316, dark_oak 142 | 85% | 22% | T:brushland, savanna, savanna_plateau, T:hot_shrubland |
| `simply_houses:forest_fort` | remote | forest + dark_forest | 46% | dark_oak 824, spruce 499, oak 56, jungle 16 | 73% | 30% | forest, dark_forest, old_growth_birch_forest, T:shield |
| `bighorsestable:big_horse_stable` | remote | plains + highlands + forest | 85% | dark_oak 1069, spruce 395, oak 193 | 71% | 11% | forest, T:yellowstone, plains, old_growth_spruce_taiga |
| `simply_houses:forest_stonemason` | remote | forest + dark_forest | 54% | dark_oak 803, spruce 261, birch 68, oak 37 | 70% | 30% | forest, T:shield, old_growth_birch_forest, dark_forest |
| `simply_houses:forest_chapel` | remote | forest + dark_forest | 56% | dark_oak 804, spruce 261, oak 154, birch 12 | 69% | 30% | forest, old_growth_birch_forest, T:shield, dark_forest |
| `simply_houses:forest_house` | remote | forest + dark_forest | 63% | dark_oak 1061, spruce 377, oak 320, birch 176 | 64% | 30% | forest, dark_forest, old_growth_birch_forest, T:shield |
| `small_viking_villager_house:small_viking_house` | remote | taiga + snowy | 62% | oak 271, spruce 209, dark_oak 94 | 57% | 14% | T:yellowstone, old_growth_spruce_taiga, taiga, T:forested_highlands |
| `forest_watchtower:forest_watchtower` | remote | dark_forest + spruce | 53% | dark_oak 148, spruce 98, oak 86 | 55% | 12% | T:yellowstone, plains, dark_forest, old_growth_spruce_taiga |

### Other (2)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `takesapillage:pillager_camp` | pillager_settlements | plains + highlands + taiga | 61% | dark_oak 1801, birch 24, spruce 1 | 98% | 2% | T:yellowstone, T:brushland, plains, savanna |
| `skyvillages:skyvillage` | sky | sky | 17% | acacia 1502, spruce 1128, dark_oak 283, jungle 52 | 71% | 25% | forest, desert, T:yellowstone, plains |

## Trim (20)

### Villages and towns (4)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:village_birch_forest` | settlements | birch | 52% | birch 2830, dark_oak 1844, spruce 34, oak 3 | 38% | 100% | T:yellowstone, forest, T:shield, old_growth_birch_forest |
| `towns_and_towers:village_grove` | settlements | snowy | 51% | spruce 488, oak 323, dark_oak 2 | 38% | 90% | snowy_plains, snowy_taiga, ice_spikes, T:wintry_forest |
| `minecraft:village_savanna` | settlements | savanna | 58% | acacia 7456, oak 6468, spruce 23 | 27% | 95% | savanna, savanna_plateau, T:arid_highlands, T:shrubland |
| `terralith:fortified_village` | settlements | plains + highlands + taiga | 49% | spruce 6905, oak 4435, dark_oak 511, acacia 18 | 25% | 94% | T:yellowstone, T:hot_shrubland, old_growth_spruce_taiga, taiga |

### Pillager forts (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_old_growth_taiga` | pillager_settlements | highlands + taiga | 79% | spruce 1296, dark_oak 629, oak 57 | 33% | 98% | T:yellowstone, old_growth_spruce_taiga, taiga, T:highlands |

### Pillager towers and camps (5)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_snowy_taiga` | pillager_settlements | snowy | 72% | spruce 831, dark_oak 556, oak 31 | 47% | 90% | snowy_plains, snowy_taiga, T:cold_shrubland, T:ice_marsh |
| `towns_and_towers:pillager_outpost_snowy_beach` | pillager_settlements | own list | 83% | spruce 476, oak 207, dark_oak 195 | 46% | 100% | snowy_beach |
| `towns_and_towers:pillager_outpost_taiga` | pillager_settlements | highlands + taiga | 77% | spruce 779, oak 435, dark_oak 42 | 27% | 98% | T:yellowstone, old_growth_spruce_taiga, taiga, T:forested_highlands |
| `towns_and_towers:pillager_outpost_savanna` | pillager_settlements | savanna | 62% | acacia 713, oak 346, spruce 42 | 24% | 95% | savanna, savanna_plateau, T:shrubland, T:arid_highlands |
| `towns_and_towers:pillager_outpost_beach` | pillager_settlements | beach | 83% | spruce 478, oak 207, dark_oak 194 | 23% | 100% | beach, T:gravel_beach, snowy_beach |

### Towers (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `watchtower_building:fresh_watchtower` | remote | spruce | 88% | spruce 1943, oak 768, dark_oak 516 | 27% | 100% | T:yellowstone, plains, old_growth_spruce_taiga, taiga |

### Houses and lone buildings (7)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `simply_houses:taiga_house` | remote | taiga + snowy | 47% | spruce 959, dark_oak 570, oak 188, jungle 36 | 44% | 96% | T:yellowstone, old_growth_spruce_taiga, taiga, T:forested_highlands |
| `simply_houses:plains_mansion` | remote | plains + highlands | 46% | spruce 1612, oak 1010, birch 873, dark_oak 752 | 42% | 91% | plains, T:hot_shrubland, T:highlands, T:alpine_highlands |
| `simply_houses:plains_house` | remote | plains + highlands | 51% | spruce 828, oak 380, dark_oak 320, birch 310 | 39% | 91% | plains, T:hot_shrubland, T:highlands, T:alpine_highlands |
| `simply_houses:taiga_inn` | remote | taiga + snowy | 46% | spruce 1103, dark_oak 596, oak 47, jungle 24 | 38% | 96% | T:yellowstone, old_growth_spruce_taiga, taiga, T:shield |
| `simply_houses:taiga_woodcutter` | remote | taiga + snowy | 45% | spruce 960, dark_oak 371, jungle 20 | 31% | 96% | T:yellowstone, old_growth_spruce_taiga, taiga, T:birch_taiga |
| `simply_houses:taiga_forge` | remote | taiga + snowy | 36% | spruce 1054, dark_oak 360, jungle 16, oak 9 | 29% | 96% | T:yellowstone, old_growth_spruce_taiga, T:birch_taiga, taiga |
| `villager_cartographer_house:villager_cartographer_house` | remote | plains + highlands + taiga | 59% | spruce 644, dark_oak 120 | 20% | 94% | T:hot_shrubland, T:yellowstone, plains, T:birch_taiga |

### Ruins (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `watchtower_building:abandoned_watchtower` | ruins | spruce | 68% | spruce 2037, oak 768, dark_oak 688 | 29% | 100% | T:yellowstone, plains, old_growth_spruce_taiga, taiga |

### Shipwrecks (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `revamped_shipwrecks:shipwreck_beached` | ocean_ruins | own list | 100% | spruce 39858, oak 22634, dark_oak 22634, jungle 17400 | 43% | 100% | beach, T:gravel_beach, snowy_beach |

## Woodless (7)

### Villages and towns (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `terralith:fortified_desert_village` | settlements | desert + oasis | 17% | jungle 2290, spruce 1439, acacia 130, dark_oak 69 | 100% | 0% | desert, T:desert_canyon, T:red_oasis, T:lush_desert |
| `towns_and_towers:exclusives/village_wandering_trader_camp` | remote | oasis | 61% | jungle 213 | 100% | 0% | T:red_oasis, T:lush_desert, T:warped_mesa, T:desert_oasis |
| `towns_and_towers:village_mushroom_fields` | settlements | mushroom | 31% | oak 575, spruce 3, birch 2, dark_oak 2 | 100% | 0% | mushroom_fields, T:mirage_isles |

### Pillager forts (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_desert` | pillager_settlements | desert + oasis | 41% | oak 1011, spruce 66 | 100% | 0% | desert, T:desert_canyon, T:red_oasis, T:lush_desert |

### Pillager towers and camps (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `mysticrift_pillagers_towers_aris:desert_pillager_tower` | pillager_settlements | sand | 15% | birch 169 | 100% | 0% | desert, beach, snowy_beach, T:desert_canyon |
| `towns_and_towers:exclusives/pillager_outpost_oriental` | pillager_settlements | oasis | 33% | oak 968, spruce 111 | 100% | 0% | T:red_oasis, T:lush_desert, T:warped_mesa, T:desert_oasis |
| `towns_and_towers:pillager_outpost_mushroom_fields` | pillager_settlements | mushroom | 43% | spruce 720, dark_oak 240 | 97% | 0% | mushroom_fields, T:mirage_isles |

## Unchanged (71)

### Villages and towns (8)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `minecraft:village_desert` | settlements | desert + oasis | 3% | jungle 315, birch 12, oak 4, spruce 4 | 100% | 0% | desert, T:desert_canyon, T:red_oasis, T:lush_desert |
| `towns_and_towers:village_forest` | settlements | forest | 0% | oak 3 | 25% | 75% | old_growth_birch_forest, T:moonlight_valley, birch_forest, T:sakura_grove |
| `towns_and_towers:village_snowy_taiga` | settlements | snowy | 56% | spruce 1413, dark_oak 136, jungle 31 | 19% | 90% | T:ice_marsh, ice_spikes, snowy_plains, snowy_taiga |
| `minecraft:village_snowy` | settlements | snowy | 70% | spruce 7128, oak 8 | 10% | 90% | T:ice_marsh, ice_spikes, snowy_slopes, snowy_plains |
| `towns_and_towers:exclusives/village_tudor` | settlements | plains + highlands | 23% | spruce 1203, oak 3, dark_oak 1, birch 1 | 10% | 91% | T:hot_shrubland, plains, T:highlands, T:alpine_highlands |
| `towns_and_towers:village_beach` | settlements | beach | 36% | spruce 613, dark_oak 27, oak 16 | 4% | 100% | beach, T:gravel_beach, snowy_beach |
| `towns_and_towers:village_jungle` | settlements | jungle | 37% | jungle 2082, oak 33, spruce 6 | 2% | 100% | sparse_jungle, jungle, T:tropical_jungle, T:rocky_jungle |
| `minecraft:village_plains` | settlements | plains + highlands | 53% | oak 9212, spruce 15 | 0% | 100% | T:hot_shrubland |

### Pillager forts (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_grove` | pillager_settlements | snowy | 22% | spruce 265, oak 1 | 10% | 90% | T:ice_marsh, ice_spikes, snowy_slopes, snowy_plains |

### Pillager towers and camps (9)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_meadow` | pillager_settlements | plains + flower | 48% | oak 699, spruce 47 | 13% | 88% | T:moonlight_valley, T:sakura_grove, T:lavender_valley, T:moonlight_grove |
| `towns_and_towers:exclusives/pillager_outpost_tudor` | pillager_settlements | plains + highlands | 36% | spruce 763, oak 55 | 9% | 91% | T:hot_shrubland |
| `minecraft:pillager_outpost` | pillager_settlements | dark_forest | 69% | dark_oak 20381, spruce 1720, birch 370, oak 29 | 7% | 100% | dark_forest, T:temperate_highlands, T:siberian_taiga, T:lavender_valley |
| `mysticrift_pillagers_towers_aris:birch_pillager_tower` | pillager_settlements | birch | 69% | birch 779, dark_oak 10 | 1% | 100% | T:yellowstone, forest, T:shield, old_growth_birch_forest |
| `mysticrift_pillagers_towers_aris:acacia_pillager_tower` | pillager_settlements | acacia | 70% | acacia 787 | 0% | 100% |  |
| `mysticrift_pillagers_towers_aris:cherry_pillager_tower` | pillager_settlements | cherry | 69% | cherry 787 | 0% | 100% |  |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | pillager_settlements | jungle | 67% | jungle 786, birch 1, dark_oak 1 | 0% | 100% | sparse_jungle, jungle, T:tropical_jungle, T:rocky_jungle |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | pillager_settlements | mangrove | 69% | mangrove 787 | 0% | 100% |  |
| `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` | pillager_settlements | oak | 68% | oak 787 | 0% | 100% |  |

### Towers (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `terralith:spire` | surface_dungeons | icy | 0% | spruce 138, warped 8 | 62% | 40% | frozen_peaks, T:glacial_chasm, T:ice_marsh, ice_spikes |

### Houses and lone buildings (15)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `desert_village_temple:village_temple` | remote | desert + oasis | 5% | birch 148, acacia 121, spruce 52 | 99% | 0% | desert, T:desert_canyon, T:red_oasis, T:lush_desert |
| `animal_barn:barn` | remote | plains + highlands | 8% | birch 133, spruce 56 | 73% | 0% | plains, T:hot_shrubland, T:highlands, T:alpine_highlands |
| `desert_watchtower:watchtower` | remote | sand | 3% | spruce 11 | 65% | 35% | desert, T:desert_canyon, T:red_oasis, T:sandstone_valley |
| `minecraft:igloo` | remote | snowy | 2% | spruce 3, oak 2 | 38% | 90% | snowy_plains, snowy_taiga, ice_spikes, T:wintry_forest |
| `villager_windmill:windmill` | remote | plains + highlands + taiga | 74% | spruce 1046, dark_oak 117, oak 100 | 18% | 94% | T:hot_shrubland, T:yellowstone, plains, T:birch_taiga |
| `villager_cleric_house:villager_cleric_house` | remote | plains + highlands + taiga | 47% | spruce 526, dark_oak 62, oak 12 | 16% | 94% | T:hot_shrubland, T:yellowstone, plains, T:birch_taiga |
| `villager_fishermans_hut:villager_fishermans_hut` | remote | plains + highlands + taiga | 54% | spruce 479, dark_oak 56 | 15% | 94% | T:hot_shrubland, T:yellowstone, plains, T:birch_taiga |
| `mini_clock_tower:mini_clock_tower` | remote | oak | 40% | oak 121, spruce 54 | 14% | 100% | forest, T:brushland, T:hot_shrubland, wooded_badlands |
| `villager_farmer_house:villager_farmer_house` | remote | plains + highlands + taiga | 55% | spruce 959, dark_oak 97 | 14% | 94% | T:hot_shrubland, T:yellowstone, plains, T:birch_taiga |
| `villager_forge:villager_forge` | remote | plains + highlands + taiga | 45% | spruce 590, dark_oak 52, oak 4 | 14% | 94% | T:hot_shrubland, T:yellowstone, T:birch_taiga, plains |
| `villager_fletcher_house:villager_fletcher_house` | remote | plains + highlands + taiga | 61% | spruce 502, dark_oak 42 | 13% | 94% | T:hot_shrubland, T:yellowstone, T:birch_taiga, plains |
| `villager_butcher_house:butcher_house` | remote | plains + highlands + taiga | 52% | spruce 768, dark_oak 53, oak 4 | 12% | 94% | T:hot_shrubland, T:birch_taiga, T:yellowstone, plains |
| `farmers_structures:aquacultures_beach` | remote | own list | 19% | spruce 156, oak 142 | 3% | 100% | snowy_beach |
| `villager_fish_shop:fish_shop` | remote | beach | 75% | spruce 335, oak 44 | 1% | 100% | snowy_beach |
| `ancient_enchantment_temple:temple` | remote | jungle | 0% | jungle 1 | 0% | 100% |  |

### Camps (2)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `snowy_tents:snowy_tent` | remote | snowy | 55% | spruce 118, oak 7 | 14% | 90% | T:ice_marsh, ice_spikes, snowy_plains, snowy_slopes |
| `plains_villager_tents:big_villager_tent` | remote | plains + highlands | 43% | spruce 178, oak 149, dark_oak 8 | 7% | 91% | T:hot_shrubland, plains, T:highlands, T:alpine_highlands |

### Ruins (14)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `philipsruins:desert_structures` | ruins | desert + oasis | 1% | dark_oak 23, oak 11, birch 2 | 100% | 0% | desert, T:desert_canyon, T:red_oasis, T:lush_desert |
| `philipsruins:badlands_structures` | ruins | badlands | 3% | dark_oak 100, oak 11 | 99% | 0% | badlands, T:bryce_canyon, eroded_badlands, wooded_badlands |
| `philipsruins:rare_ruin` | ruins | jungle + swamp | 3% | oak 8, dark_oak 8, crimson 2 | 94% | 13% | sparse_jungle, jungle, mangrove_swamp, T:tropical_jungle |
| `philipsruins:field_stone_ruins` | ruins | plains + highlands + forest | 5% | dark_oak 181, spruce 34, oak 22, jungle 10 | 81% | 9% | forest, T:yellowstone, plains, sparse_jungle |
| `forest_ruins:ruin_1` | ruins | plains + highlands + forest | 1% | spruce 2 | 35% | 65% | forest, dark_forest, T:hot_shrubland, old_growth_birch_forest |
| `forest_ruins:ruin_2` | ruins | plains + highlands + forest | 2% | spruce 2 | 35% | 65% | forest, dark_forest, T:hot_shrubland, old_growth_birch_forest |
| `forest_ruins:ruin_3` | ruins | plains + highlands + forest | 3% | spruce 10 | 35% | 65% | forest, dark_forest, T:hot_shrubland, old_growth_birch_forest |
| `forest_ruins:ruin_5` | ruins | plains + highlands + forest | 8% | spruce 12 | 35% | 65% | forest, dark_forest, T:hot_shrubland, old_growth_birch_forest |
| `forest_ruins:ruin_7` | ruins | plains + highlands + forest | 5% | spruce 4 | 35% | 65% | forest, dark_forest, T:hot_shrubland, old_growth_birch_forest |
| `forest_ruins:ruin_4` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `forest_ruins:ruin_6` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `philipsruins:field_stone_ruins_rocks` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `philipsruins:lost_soul_city` | standalone | any | 0% |  | 0% | 100% |  |
| `philipsruins:start_nether_ruin` | ruins | own list | 0% |  | 0% | 100% |  |

### Other (21)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `betterdungeons:zombie_dungeon` | entrance_dungeons | any | 0% | warped 1 | 100% | 0% | T:yellowstone, forest, T:brushland, plains |
| `betterdeserttemples:desert_temple` | surface_dungeons | desert + oasis | 2% | acacia 1385, birch 819, oak 363, warped 24 | 98% | 3% | desert, T:desert_canyon, T:red_oasis, T:lush_desert |
| `underground_bunkers:underground_bunker` | standalone | own list | 0% | birch 50, spruce 45, oak 38, dark_oak 19 | 67% | 32% | desert, forest, plains, T:yellowstone |
| `betterstrongholds:stronghold` | standalone | own list | 2% | oak 535, spruce 484, crimson 24, birch 8 | 65% | 37% | savanna, desert, badlands, T:yellowstone |
| `betterdungeons:skeleton_dungeon` | standalone | any | 1% | spruce 39, oak 7 | 59% | 41% | forest, T:brushland, savanna, desert |
| `cloudstorage:big_balloon` | sky | own list | 7% | oak 42 | 59% | 41% | T:yellowstone, savanna, desert, badlands |
| `philipsruins:ancient_dungeon` | standalone | own list | 3% | spruce 93, oak 33 | 59% | 41% | savanna, forest, T:brushland, desert |
| `oldvillagerstatue:old_villager_statue` | ruins | plains + highlands + forest | 2% | spruce 8 | 50% | 50% | forest, T:brushland, savanna, savanna_plateau |
| `philipsruins:ancient_crypt` | standalone | plains + highlands + forest | 3% | spruce 166, oak 71 | 49% | 51% | savanna, forest, T:brushland, savanna_plateau |
| `philipsruins:antiquus_crypta` | standalone | plains + highlands + forest | 2% | spruce 79, oak 25 | 49% | 51% | forest, savanna, T:brushland, savanna_plateau |
| `philipsruins:badlands_start_dungeon` | surface_dungeons | badlands | 0% | acacia 8 | 4% | 96% | T:red_oasis, T:ancient_sands |
| `betterdungeons:small_dungeon` | standalone | any | 0% |  | 0% | 100% |  |
| `betterjungletemples:jungle_temple` | surface_dungeons | jungle | 0% | jungle 103 | 0% | 100% |  |
| `betterwitchhuts:witch_circle` | witch_huts | swamp | 0% |  | 0% | 100% |  |
| `cloudstorage:sky_temple` | sky | own list | 0% |  | 0% | 100% |  |
| `old_illager_statue:old_illager_statue` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `philipsruins:bone_dungeon` | standalone | any | 0% |  | 0% | 100% |  |
| `small_cat_statues:cat_1` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `small_cat_statues:cat_2` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `small_cat_statues:cat_3` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `towns_and_towers:mimic_desert` | surface_dungeons | desert + oasis | 0% |  | 0% | 100% |  |

