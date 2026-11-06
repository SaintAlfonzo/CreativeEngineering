# Wood mismatch: structures built of wood foreign to their biomes

Written by `python3 tools/wood_mismatch_scan.py`. Do not edit. Every live land structure; wood counted over every template it can place; native woods per biome from the wood fits in `biome_fits.json`. **Wood** = wood blocks / all blocks. **Mismatch** = share of its wood that is foreign where it spawns (area-weighted). **Keep** = share of its land area where its main wood is native (what restricting it to that wood keeps). Verdicts: **restrict** (to its main wood fit), **swap** (keep the biomes, swap the wood per biome: settlements, forts, or a restriction that would keep under 35%), **trim** (main wood native almost everywhere, only trim woods foreign), **woodless** (mostly biomes with no native tree: deserts, mushroom fields), **unchanged** (little wood or little mismatch).

## Restrict (39)

### Pillager towers and camps (11)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_flower_forest` | pillager_settlements | flower | 64% | spruce 936, oak 14, jungle 6 | 63% | 37% | T:sakura_grove, T:lavender_forest, T:blooming_valley, T:moonlight_grove |
| `towns_and_towers:pillager_outpost_birch_forest` | pillager_settlements | birch | 70% | oak 1521, birch 195, spruce 54 | 56% | 36% | T:yellowstone, T:orchid_swamp, T:sakura_grove, old_growth_birch_forest |
| `illager_siege_weapons:catapult` | vehicles | plains + highlands + dark_forest | 90% | spruce 101, dark_oak 25 | 52% | 58% | T:lush_desert, T:brushland, dark_forest, savanna |
| `towns_and_towers:pillager_outpost_snowy_taiga` | pillager_settlements | snowy | 72% | spruce 831, dark_oak 556, oak 31 | 51% | 82% | T:ice_marsh, snowy_plains, ice_spikes, snowy_taiga |
| `illager_siege_weapons:ballista` | vehicles | plains + highlands + dark_forest | 88% | spruce 65, oak 14, dark_oak 2 | 48% | 58% | dark_forest, T:lush_desert, T:brushland, savanna |
| `illager_siege_weapons:trebuchet` | vehicles | plains + highlands + dark_forest | 90% | spruce 144, oak 28, dark_oak 8 | 48% | 58% | dark_forest, T:lush_desert, T:brushland, savanna |
| `illager_siege_weapons:reinforced_battering_ram` | vehicles | plains + highlands + dark_forest | 97% | spruce 218, oak 47 | 47% | 58% | dark_forest, T:lush_desert, T:brushland, savanna |
| `illager_siege_weapons:siege_tower` | vehicles | plains + highlands + dark_forest | 81% | spruce 361, oak 60 | 46% | 58% | dark_forest, T:lush_desert, T:brushland, savanna |
| `towns_and_towers:pillager_outpost_forest` | pillager_settlements | forest | 18% | oak 353, dark_oak 6, spruce 3 | 42% | 59% | T:sakura_grove, old_growth_birch_forest, T:cloud_forest, T:white_cliffs |
| `towns_and_towers:pillager_outpost_sunflower_plains` | pillager_settlements | plains + flower | 34% | spruce 600, oak 128 | 36% | 61% | T:sakura_grove, T:hot_shrubland, T:lavender_forest, T:moonlight_grove |
| `towns_and_towers:pillager_outpost_meadow` | pillager_settlements | plains + flower | 48% | oak 699, spruce 47 | 25% | 76% | T:sakura_grove, T:lavender_forest, T:moonlight_grove, T:moonlight_valley |

### Towers (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `philipsruins:ancient_towers` | ruins | plains + highlands + forest | 15% | spruce 288, oak 75, birch 24 | 53% | 50% | forest, dark_forest, T:brushland, savanna |

### Houses and lone buildings (11)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `greek_villager_house:greek_house` | remote | savanna + warm_dry | 35% | oak 203, spruce 144, jungle 5 | 78% | 39% | savanna, T:arid_highlands, savanna_plateau, windswept_savanna |
| `llama_stable:llama_stable` | remote | highlands + savanna | 65% | spruce 316, dark_oak 142 | 76% | 35% | T:brushland, savanna, T:arid_highlands, savanna_plateau |
| `hobbit_house_:hobbit_house` | remote | forest + flower + dark_forest | 51% | spruce 105, oak 99, jungle 88, dark_oak 49 | 71% | 58% | T:yellowstone, forest, dark_forest, taiga |
| `farmers_structures:cooks` | remote | plains + highlands + forest | 43% | oak 1589, dark_oak 1530, spruce 757, birch 3 | 63% | 68% | forest, savanna, plains, T:brushland |
| `villager_flower_house:flower_house` | remote | flower | 60% | spruce 678, oak 168, dark_oak 7, jungle 6 | 61% | 37% | T:sakura_grove, T:lavender_forest, T:moonlight_grove, T:moonlight_valley |
| `gothic_church:church` | remote | plains + highlands + forest | 34% | spruce 541, dark_oak 283 | 52% | 67% | forest, T:yellowstone, dark_forest, plains |
| `smallhorsestable:horse_stable` | remote | plains + forest | 78% | oak 82, spruce 58, dark_oak 50 | 52% | 75% | forest, plains, T:sakura_grove, old_growth_birch_forest |
| `farmers_structures:aquacultures_common` | remote | plains + highlands + forest | 66% | spruce 195, oak 77, dark_oak 3 | 49% | 55% | forest, dark_forest, T:yellowstone, savanna |
| `villager_tannery:tannery` | remote | plains + highlands + forest | 56% | spruce 258, dark_oak 88, oak 24, jungle 10 | 49% | 67% | forest, T:yellowstone, dark_forest, T:hot_shrubland |
| `sawmillhouse:sawmill` | remote | forest + dark_forest + taiga | 81% | spruce 667, oak 63 | 48% | 55% | forest, dark_forest, T:temperate_highlands, T:sakura_grove |
| `small_villager_greenhouse:small_villager_greenhouse` | remote | plains + forest | 74% | oak 158, spruce 88, dark_oak 15 | 39% | 75% | forest, T:sakura_grove, old_growth_birch_forest, T:white_cliffs |

### Camps (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `villager_circus_tent:villager_circus_tent` | remote | plains + forest | 45% | spruce 286, oak 203, mangrove 157, dark_oak 25 | 58% | 46% | forest, plains, T:sakura_grove, old_growth_birch_forest |
| `villager_archer_camp:villager_archer_camp` | remote | plains + forest | 70% | spruce 152, oak 74 | 44% | 46% | forest, T:sakura_grove, old_growth_birch_forest, T:temperate_highlands |
| `snowy_tents:snowy_tent` | remote | snowy | 55% | spruce 118, oak 7 | 21% | 82% | T:ice_marsh, ice_spikes, snowy_slopes, snowy_plains |

### Ruins (7)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `philipsruins:level_two_ruins_pool` | ruins | plains + highlands + forest | 11% | spruce 118, dark_oak 100, oak 67 | 62% | 57% | T:yellowstone, forest, dark_forest, T:orchid_swamp |
| `philipsruins:pumpkin_ruins` | ruins | plains + highlands + forest | 25% | spruce 156, acacia 98, dark_oak 18, oak 6 | 61% | 66% | forest, T:yellowstone, dark_forest, plains |
| `philipsruins:ancient_ruins` | ruins | plains + highlands + forest | 11% | spruce 377, oak 209, acacia 4 | 54% | 50% | T:yellowstone, dark_forest, forest, savanna |
| `philipsruins:level_two_ruins` | ruins | plains + highlands + forest | 14% | oak 488, spruce 263, dark_oak 48 | 53% | 45% | T:yellowstone, dark_forest, forest, T:orchid_swamp |
| `philipsruins:level_three_ruins` | ruins | plains + highlands + forest | 21% | spruce 1377, oak 1276, dark_oak 199, birch 49 | 52% | 57% | T:yellowstone, dark_forest, forest, T:orchid_swamp |
| `philipsruins:level_one_ruins` | ruins | plains + highlands + forest | 11% | spruce 308, oak 87, birch 33, dark_oak 7 | 48% | 57% | forest, dark_forest, T:yellowstone, swamp |
| `forgotten_church:forgotten_church` | ruins | plains + highlands + forest | 21% | spruce 165, dark_oak 23 | 40% | 67% | forest, dark_forest, T:hot_shrubland, T:temperate_highlands |

### Other (6)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `villagesandpillages:village_witch` | witch_huts | swamp | 72% | oak 1807, spruce 1422 | 69% | 56% | T:orchid_swamp, mangrove_swamp, swamp, T:ice_marsh |
| `subterrestrial:deepslate_cabin` | standalone | own list | 77% | oak 3123, birch 48 | 65% | 35% | T:yellowstone, dark_forest, T:lush_desert, snowy_plains |
| `betterwitchhuts:witch_hut` | witch_huts | swamp | 92% | oak 624, spruce 295, crimson 2 | 62% | 56% | T:orchid_swamp, mangrove_swamp, swamp, T:ice_marsh |
| `takesapillage:bastille` | pillager_settlements | plains + highlands + taiga | 28% | spruce 10589, dark_oak 2861, birch 764, oak 20 | 53% | 62% | T:lush_desert, T:brushland, savanna, T:red_oasis |
| `subterrestrial:default_cabin` | standalone | own list | 77% | oak 3123, birch 48 | 39% | 61% | dark_forest, mushroom_fields, T:lavender_forest, jagged_peaks |
| `subterrestrial:ice_cabin` | standalone | own list | 91% | spruce 3107, dark_oak 46 | 33% | 68% | frozen_peaks, ice_spikes, snowy_slopes, snowy_plains |

## Swap (41)

### Villages and towns (14)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:village_swamp` | settlements | swamp | 66% | dark_oak 2554, spruce 1928, oak 7, mangrove 3 | 100% | 0% | swamp, T:orchid_swamp, T:ice_marsh, mangrove_swamp |
| `towns_and_towers:exclusives/village_mediterranean` | settlements | warm_dry | 13% | spruce 749, birch 167, dark_oak 13, oak 11 | 99% | 0% | T:brushland, T:arid_highlands, savanna_plateau, T:hot_shrubland |
| `towns_and_towers:village_sparse_jungle` | settlements | jungle | 73% | spruce 1479, oak 1269, dark_oak 147, jungle 93 | 97% | 0% | sparse_jungle, jungle, T:amethyst_rainforest, T:rocky_jungle |
| `towns_and_towers:village_badlands` | settlements | badlands | 25% | spruce 740, oak 614, dark_oak 458, jungle 64 | 95% | 8% | T:red_oasis, eroded_badlands, T:painted_mountains, badlands |
| `towns_and_towers:village_wooded_badlands` | settlements | badlands | 38% | spruce 572, oak 75, jungle 18 | 93% | 8% | T:red_oasis, eroded_badlands, T:painted_mountains, badlands |
| `towns_and_towers:exclusives/village_swedish` | settlements | taiga + snowy | 36% | mangrove 469, dark_oak 438, birch 361, spruce 45 | 86% | 0% | T:yellowstone, snowy_plains, taiga, old_growth_pine_taiga |
| `towns_and_towers:village_meadow` | settlements | plains + flower | 58% | dark_oak 2271, spruce 1721, oak 95, birch 2 | 67% | 9% | plains, T:hot_shrubland, T:sakura_grove, meadow |
| `towns_and_towers:village_snowy_slopes` | settlements | snowy | 72% | dark_oak 524, spruce 377, oak 49 | 66% | 1% | snowy_plains, T:ice_marsh, snowy_taiga, T:cold_shrubland |
| `towns_and_towers:village_flower_forest` | settlements | flower | 38% | spruce 3143, acacia 173, birch 129, oak 35 | 65% | 37% | T:sakura_grove, T:lavender_forest, T:blooming_valley, T:moonlight_grove |
| `towns_and_towers:village_old_growth_taiga` | settlements | highlands + taiga | 75% | dark_oak 1929, spruce 1656, oak 71, birch 2 | 53% | 4% | T:yellowstone, T:highlands, T:rocky_mountains, taiga |
| `towns_and_towers:village_grove` | settlements | snowy | 51% | spruce 488, oak 323, dark_oak 2 | 41% | 82% | snowy_plains, T:ice_marsh, ice_spikes, snowy_taiga |
| `minecraft:village_taiga` | settlements | highlands + taiga | 49% | oak 6661, spruce 6087 | 35% | 34% | T:yellowstone, taiga, old_growth_pine_taiga, old_growth_spruce_taiga |
| `towns_and_towers:village_sunflower_plains` | settlements | plains + flower | 54% | spruce 929, oak 790 | 32% | 61% | T:sakura_grove, T:lavender_forest, T:hot_shrubland, T:moonlight_grove |
| `towns_and_towers:village_snowy_taiga` | settlements | snowy | 56% | spruce 1413, dark_oak 136, jungle 31 | 27% | 82% | T:ice_marsh, ice_spikes, snowy_slopes, snowy_plains |

### Pillager forts (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_badlands` | pillager_settlements | badlands | 27% | oak 404, spruce 7 | 94% | 6% | T:red_oasis, eroded_badlands, T:painted_mountains, badlands |
| `towns_and_towers:pillager_outpost_jungle` | pillager_settlements | jungle | 35% | dark_oak 905, oak 425, spruce 406, jungle 252 | 87% | 0% | sparse_jungle, jungle, T:amethyst_rainforest, T:rocky_jungle |
| `towns_and_towers:exclusives/pillager_outpost_mediterranean` | pillager_settlements | warm_dry | 31% | oak 315, spruce 224 | 66% | 58% | T:arid_highlands, savanna_plateau, T:brushland, T:savanna_slopes |

### Pillager towers and camps (9)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_swamp` | pillager_settlements | swamp | 66% | dark_oak 748, spruce 287, oak 6 | 100% | 0% | swamp, T:orchid_swamp, T:ice_marsh, mangrove_swamp |
| `towns_and_towers:pillager_outpost_sparse_jungle` | pillager_settlements | jungle | 67% | spruce 404, oak 32, jungle 20, acacia 4 | 96% | 0% | sparse_jungle, jungle, T:amethyst_rainforest, T:rocky_jungle |
| `towns_and_towers:pillager_outpost_wooded_badlands` | pillager_settlements | badlands | 64% | spruce 414, oak 50 | 92% | 8% | T:red_oasis, eroded_badlands, T:painted_mountains, badlands |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | pillager_settlements | taiga + snowy | 56% | dark_oak 957, spruce 163, oak 10 | 84% | 4% | T:yellowstone, snowy_plains, taiga, old_growth_pine_taiga |
| `eugene:pillager_loot_carriage` | vehicles | plains + snowy + desert | 98% | dark_oak 94, spruce 59, oak 8 | 83% | 0% | T:lush_desert, T:brushland, plains, savanna |
| `towns_and_towers:pillager_outpost_snowy_slopes` | pillager_settlements | snowy | 31% | dark_oak 712, spruce 309, oak 2 | 75% | 1% | snowy_plains, T:ice_marsh, T:cold_shrubland, snowy_taiga |
| `towns_and_towers:pillager_outpost_snowy_plains` | pillager_settlements | snowy | 25% | dark_oak 413, spruce 279, oak 1 | 67% | 1% | snowy_plains, T:ice_marsh, T:cold_shrubland, snowy_taiga |
| `minecraft:mansion` | surface_dungeons | dark_forest | 65% | birch 6580, dark_oak 2476, oak 1776 | 63% | 18% | dark_forest, T:siberian_taiga, T:lavender_forest, T:lavender_valley |
| `illager_siege_weapons:battering_ram` | vehicles | plains + highlands + dark_forest | 59% | oak 48, spruce 24 | 58% | 34% | T:yellowstone, dark_forest, T:lush_desert, savanna |

### Houses and lone buildings (11)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `old_swamp_fisherman_house:fisherman_house` | remote | swamp | 78% | dark_oak 389, spruce 318, oak 27 | 98% | 0% | swamp, T:orchid_swamp, T:ice_marsh, mangrove_swamp |
| `saloon:saloon` | remote | badlands | 87% | spruce 673, oak 564, dark_oak 444 | 95% | 8% | T:red_oasis, eroded_badlands, T:painted_mountains, badlands |
| `villager_country_house:country_house` | remote | savanna + warm_dry | 23% | spruce 472, dark_oak 250, acacia 36 | 95% | 0% | T:brushland, savanna, T:arid_highlands, savanna_plateau |
| `villager_gold_washing_facility:villager_gold_washing_facility` | remote | badlands | 66% | spruce 314, oak 78, dark_oak 10, acacia 1 | 93% | 8% | T:red_oasis, eroded_badlands, T:painted_mountains, badlands |
| `bighorsestable:big_horse_stable` | remote | plains + highlands + forest | 85% | dark_oak 1069, spruce 395, oak 193 | 70% | 13% | T:yellowstone, forest, plains, T:highlands |
| `simply_houses:forest_fort` | remote | forest + dark_forest | 46% | dark_oak 824, spruce 499, oak 56, jungle 16 | 70% | 35% | forest, dark_forest, T:sakura_grove, old_growth_birch_forest |
| `simply_houses:forest_chapel` | remote | forest + dark_forest | 56% | dark_oak 804, spruce 261, oak 154, birch 12 | 67% | 35% | forest, dark_forest, T:sakura_grove, T:shield |
| `simply_houses:forest_stonemason` | remote | forest + dark_forest | 54% | dark_oak 803, spruce 261, birch 68, oak 37 | 67% | 35% | forest, dark_forest, T:shield, T:sakura_grove |
| `simply_houses:forest_house` | remote | forest + dark_forest | 63% | dark_oak 1061, spruce 377, oak 320, birch 176 | 64% | 35% | forest, dark_forest, T:sakura_grove, old_growth_birch_forest |
| `small_viking_villager_house:small_viking_house` | remote | taiga + snowy | 62% | oak 271, spruce 209, dark_oak 94 | 58% | 16% | T:yellowstone, snowy_plains, taiga, old_growth_pine_taiga |
| `forest_watchtower:forest_watchtower` | remote | dark_forest + spruce | 53% | dark_oak 148, spruce 98, oak 86 | 56% | 12% | T:yellowstone, plains, dark_forest, snowy_plains |

### Ruins (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `terralith:glacial_hut` | ruins | icy | 80% | spruce 528, birch 453, dark_oak 67 | 86% | 29% | T:glacial_chasm, frozen_peaks, T:ice_marsh, snowy_plains |

### Other (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `takesapillage:pillager_camp` | pillager_settlements | plains + highlands + taiga | 61% | dark_oak 1801, birch 24, spruce 1 | 98% | 2% | T:yellowstone, plains, T:lush_desert, T:brushland |
| `skyvillages:skyvillage` | sky | sky | 17% | acacia 2240, spruce 1653, dark_oak 512, jungle 52 | 75% | 16% | T:yellowstone, forest, T:lush_desert, dark_forest |
| `eugene:witch_wagon` | vehicles | dark_forest + swamp | 89% | oak 235, dark_oak 102, spruce 15 | 60% | 34% | dark_forest, T:orchid_swamp, mangrove_swamp, T:siberian_taiga |

## Trim (17)

### Villages and towns (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:village_birch_forest` | settlements | birch | 52% | birch 2830, dark_oak 1844, spruce 34, oak 3 | 38% | 100% | T:yellowstone, forest, T:orchid_swamp, T:shield |
| `minecraft:village_savanna` | settlements | savanna | 59% | acacia 7635, oak 6667, spruce 62 | 32% | 95% | savanna, T:arid_highlands, savanna_plateau, windswept_savanna |
| `terralith:fortified_village` | settlements | plains + highlands + taiga | 49% | spruce 6905, oak 4435, dark_oak 511, acacia 18 | 25% | 96% | T:yellowstone, taiga, T:hot_shrubland, old_growth_pine_taiga |

### Pillager forts (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_old_growth_taiga` | pillager_settlements | highlands + taiga | 79% | spruce 1296, dark_oak 629, oak 57 | 33% | 99% | T:yellowstone, T:highlands, T:rocky_mountains, taiga |

### Pillager towers and camps (4)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_snowy_beach` | pillager_settlements | own list | 83% | spruce 476, oak 207, dark_oak 195 | 46% | 100% | snowy_beach |
| `towns_and_towers:pillager_outpost_taiga` | pillager_settlements | highlands + taiga | 77% | spruce 779, oak 435, dark_oak 42 | 27% | 99% | T:yellowstone, taiga, old_growth_pine_taiga, old_growth_spruce_taiga |
| `towns_and_towers:pillager_outpost_savanna` | pillager_settlements | savanna | 62% | acacia 713, oak 346, spruce 42 | 26% | 95% | savanna, T:arid_highlands, savanna_plateau, T:shrubland |
| `towns_and_towers:pillager_outpost_beach` | pillager_settlements | beach | 83% | spruce 478, oak 207, dark_oak 194 | 25% | 100% | beach, T:gravel_beach, snowy_beach |

### Towers (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `watchtower_building:fresh_watchtower` | remote | spruce | 88% | spruce 1943, oak 768, dark_oak 516 | 28% | 100% | T:yellowstone, snowy_plains, taiga, plains |

### Houses and lone buildings (6)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `simply_houses:taiga_house` | remote | taiga + snowy | 47% | spruce 959, dark_oak 570, oak 188, jungle 36 | 47% | 92% | T:yellowstone, snowy_plains, T:ice_marsh, taiga |
| `simply_houses:plains_mansion` | remote | plains + highlands | 46% | spruce 1612, oak 1010, birch 873, dark_oak 752 | 41% | 93% | plains, T:highlands, T:hot_shrubland, T:rocky_mountains |
| `simply_houses:taiga_inn` | remote | taiga + snowy | 46% | spruce 1103, dark_oak 596, oak 47, jungle 24 | 41% | 92% | T:yellowstone, T:ice_marsh, snowy_plains, taiga |
| `simply_houses:plains_house` | remote | plains + highlands | 51% | spruce 828, oak 380, dark_oak 320, birch 310 | 37% | 93% | plains, T:hot_shrubland, T:highlands, T:rocky_mountains |
| `simply_houses:taiga_woodcutter` | remote | taiga + snowy | 45% | spruce 960, dark_oak 371, jungle 20 | 34% | 92% | T:yellowstone, T:ice_marsh, snowy_plains, ice_spikes |
| `simply_houses:taiga_forge` | remote | taiga + snowy | 36% | spruce 1054, dark_oak 360, jungle 16, oak 9 | 32% | 92% | T:yellowstone, T:ice_marsh, ice_spikes, snowy_plains |

### Ruins (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `watchtower_building:abandoned_watchtower` | ruins | spruce | 68% | spruce 2037, oak 768, dark_oak 688 | 31% | 100% | T:yellowstone, snowy_plains, plains, taiga |

### Shipwrecks (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `revamped_shipwrecks:shipwreck_beached` | ocean_ruins | own list | 100% | spruce 39858, oak 22634, dark_oak 22634, jungle 17400 | 44% | 100% | beach, T:gravel_beach, snowy_beach |

## Woodless (7)

### Villages and towns (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `terralith:fortified_desert_village` | settlements | desert + oasis | 17% | jungle 2290, spruce 1439, acacia 130, dark_oak 69 | 100% | 0% | T:lush_desert, T:red_oasis, T:desert_canyon, desert |
| `towns_and_towers:exclusives/village_wandering_trader_camp` | remote | oasis | 61% | jungle 213 | 100% | 0% | T:lush_desert, T:red_oasis, T:desert_oasis, T:sandstone_valley |
| `towns_and_towers:village_mushroom_fields` | settlements | mushroom | 31% | oak 575, spruce 3, birch 2, dark_oak 2 | 100% | 0% | mushroom_fields, T:mirage_isles |

### Pillager forts (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_desert` | pillager_settlements | desert + oasis | 41% | oak 1011, spruce 66 | 100% | 0% | T:lush_desert, T:red_oasis, T:desert_canyon, desert |

### Pillager towers and camps (3)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `mysticrift_pillagers_towers_aris:desert_pillager_tower` | pillager_settlements | sand | 15% | birch 169 | 100% | 0% | T:lush_desert, beach, T:red_oasis, T:desert_canyon |
| `towns_and_towers:exclusives/pillager_outpost_oriental` | pillager_settlements | oasis | 33% | oak 968, spruce 111 | 100% | 0% | T:lush_desert, T:red_oasis, T:desert_oasis, T:sandstone_valley |
| `towns_and_towers:pillager_outpost_mushroom_fields` | pillager_settlements | mushroom | 43% | spruce 720, dark_oak 240 | 97% | 0% | mushroom_fields, T:mirage_isles |

## Unchanged (84)

### Villages and towns (8)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `minecraft:village_desert` | settlements | desert + oasis | 3% | jungle 315, birch 12, oak 4, spruce 4 | 100% | 0% | T:lush_desert, T:red_oasis, T:desert_canyon, desert |
| `towns_and_towers:village_forest` | settlements | forest | 0% | oak 3 | 41% | 59% | T:sakura_grove, old_growth_birch_forest, T:cloud_forest, T:white_cliffs |
| `minecraft:village_snowy` | settlements | snowy | 70% | spruce 7143, oak 10 | 18% | 82% | T:ice_marsh, ice_spikes, snowy_slopes, snowy_plains |
| `towns_and_towers:exclusives/village_tudor` | settlements | plains + highlands | 23% | spruce 1203, oak 3, dark_oak 1, birch 1 | 7% | 93% | T:hot_shrubland, plains, T:highlands, T:rocky_mountains |
| `towns_and_towers:village_beach` | settlements | beach | 36% | spruce 613, dark_oak 27, oak 16 | 4% | 100% | beach, T:gravel_beach, snowy_beach |
| `towns_and_towers:village_jungle` | settlements | jungle | 37% | jungle 2082, oak 33, spruce 6 | 2% | 100% | sparse_jungle, jungle, T:amethyst_rainforest, T:rocky_jungle |
| `minecraft:village_plains` | settlements | plains + highlands | 53% | oak 9399, spruce 40 | 0% | 100% | T:hot_shrubland |
| `towns_and_towers:exclusives/village_classic` | settlements | own list | 44% | oak 964 | 0% | 100% |  |

### Pillager forts (2)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `towns_and_towers:pillager_outpost_grove` | pillager_settlements | snowy | 22% | spruce 265, oak 1 | 19% | 82% | T:ice_marsh, ice_spikes, snowy_slopes, snowy_plains |
| `towns_and_towers:exclusives/pillager_outpost_classic` | pillager_settlements | own list | 24% | oak 425, spruce 5 | 1% | 100% | T:alpha_islands |

### Pillager towers and camps (9)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `minecraft:pillager_outpost` | pillager_settlements | dark_forest | 69% | dark_oak 20365, spruce 1720, birch 370, oak 29 | 8% | 100% | dark_forest, T:temperate_highlands, T:lavender_forest, T:lavender_valley |
| `towns_and_towers:exclusives/pillager_outpost_tudor` | pillager_settlements | plains + highlands | 36% | spruce 763, oak 55 | 6% | 93% | T:hot_shrubland |
| `mysticrift_pillagers_towers_aris:birch_pillager_tower` | pillager_settlements | birch | 69% | birch 779, dark_oak 10 | 1% | 100% | T:yellowstone, forest, T:orchid_swamp, T:shield |
| `eugene:pillager_camp` | pillager_settlements | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `mysticrift_pillagers_towers_aris:acacia_pillager_tower` | pillager_settlements | acacia | 70% | acacia 787 | 0% | 100% |  |
| `mysticrift_pillagers_towers_aris:cherry_pillager_tower` | pillager_settlements | cherry | 69% | cherry 787 | 0% | 100% |  |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | pillager_settlements | jungle | 67% | jungle 786, birch 1, dark_oak 1 | 0% | 100% | sparse_jungle, jungle, T:amethyst_rainforest, T:rocky_jungle |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | pillager_settlements | mangrove | 69% | mangrove 787 | 0% | 100% |  |
| `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` | pillager_settlements | oak | 68% | oak 787 | 0% | 100% |  |

### Towers (1)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `terralith:spire` | surface_dungeons | icy | 0% | spruce 138, warped 8 | 73% | 29% | T:glacial_chasm, frozen_peaks, T:ice_marsh, ice_spikes |

### Houses and lone buildings (16)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `desert_village_temple:village_temple` | remote | desert + oasis | 5% | birch 148, acacia 121, spruce 52 | 100% | 0% | T:lush_desert, T:red_oasis, T:desert_canyon, desert |
| `animal_barn:barn` | remote | plains + highlands | 8% | birch 133, spruce 56 | 72% | 0% | plains, T:highlands, T:rocky_mountains, T:hot_shrubland |
| `desert_watchtower:watchtower` | remote | sand | 3% | spruce 11 | 71% | 29% | T:lush_desert, T:red_oasis, T:desert_canyon, desert |
| `minecraft:igloo` | remote | snowy | 2% | spruce 3, oak 2 | 41% | 82% | snowy_plains, T:ice_marsh, ice_spikes, snowy_taiga |
| `villager_cartographer_house:villager_cartographer_house` | remote | plains + highlands + taiga | 59% | spruce 644, dark_oak 120 | 19% | 96% | T:yellowstone, T:hot_shrubland, plains, T:highlands |
| `villager_windmill:windmill` | remote | plains + highlands + taiga | 74% | spruce 1046, dark_oak 117, oak 100 | 16% | 96% | T:yellowstone, T:hot_shrubland, T:birch_taiga, plains |
| `villager_cleric_house:villager_cleric_house` | remote | plains + highlands + taiga | 47% | spruce 526, dark_oak 62, oak 12 | 15% | 96% | T:hot_shrubland, T:yellowstone, plains, T:birch_taiga |
| `villager_fishermans_hut:villager_fishermans_hut` | remote | plains + highlands + taiga | 54% | spruce 479, dark_oak 56 | 14% | 96% | T:hot_shrubland, T:yellowstone, plains, T:birch_taiga |
| `mini_clock_tower:mini_clock_tower` | remote | oak | 40% | oak 121, spruce 54 | 13% | 100% | forest, T:brushland, swamp, T:temperate_highlands |
| `villager_farmer_house:villager_farmer_house` | remote | plains + highlands + taiga | 55% | spruce 959, dark_oak 97 | 13% | 96% | T:hot_shrubland, T:yellowstone, T:birch_taiga, plains |
| `villager_forge:villager_forge` | remote | plains + highlands + taiga | 45% | spruce 590, dark_oak 52, oak 4 | 12% | 96% | T:hot_shrubland, T:yellowstone, T:birch_taiga, plains |
| `villager_fletcher_house:villager_fletcher_house` | remote | plains + highlands + taiga | 61% | spruce 502, dark_oak 42 | 11% | 96% | T:hot_shrubland, T:yellowstone, T:birch_taiga, plains |
| `villager_butcher_house:butcher_house` | remote | plains + highlands + taiga | 52% | spruce 768, dark_oak 53, oak 4 | 10% | 96% | T:hot_shrubland, T:yellowstone, T:birch_taiga, plains |
| `farmers_structures:aquacultures_beach` | remote | own list | 19% | spruce 156, oak 142 | 6% | 100% | snowy_beach |
| `villager_fish_shop:fish_shop` | remote | beach | 75% | spruce 335, oak 44 | 1% | 100% | snowy_beach |
| `ancient_enchantment_temple:temple` | remote | jungle | 0% | jungle 1 | 0% | 100% |  |

### Camps (9)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `eugene:camp` | remote | plains + highlands + forest | 13% | oak 24 | 51% | 49% | T:yellowstone, dark_forest, taiga, old_growth_pine_taiga |
| `eugene:fake_trader_camp` | pillager_settlements | plains + highlands + forest | 13% | oak 12 | 51% | 49% | T:yellowstone, dark_forest, taiga, old_growth_pine_taiga |
| `campsite_structures:armorer_campsite` | remote | plains + highlands + forest | 11% | spruce 8 | 42% | 58% | forest, T:temperate_highlands, T:hot_shrubland, T:sakura_grove |
| `campsite_structures:butcher_campsite` | remote | plains + highlands + forest | 18% | spruce 14 | 42% | 58% | forest, T:temperate_highlands, T:hot_shrubland, T:sakura_grove |
| `campsite_structures:cartographer_campsite` | remote | plains + highlands + forest | 20% | spruce 10 | 42% | 58% | forest, T:temperate_highlands, T:hot_shrubland, T:sakura_grove |
| `campsite_structures:cleric_campsite` | remote | plains + highlands + forest | 9% | spruce 8 | 42% | 58% | forest, T:temperate_highlands, T:hot_shrubland, T:sakura_grove |
| `campsite_structures:farmer_campsite` | remote | plains + highlands + forest | 12% | spruce 12 | 42% | 58% | forest, T:temperate_highlands, T:hot_shrubland, T:sakura_grove |
| `plains_villager_tents:big_villager_tent` | remote | plains + highlands | 43% | spruce 178, oak 149, dark_oak 8 | 6% | 93% | T:hot_shrubland, plains, T:highlands, T:rocky_mountains |
| `campsite_structures:fisher_campsite` | remote | beach | 22% | spruce 16 | 0% | 100% |  |

### Ruins (12)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `philipsruins:desert_structures` | ruins | desert + oasis | 1% | dark_oak 23, oak 11, birch 2 | 100% | 0% | T:lush_desert, T:red_oasis, T:desert_canyon, desert |
| `philipsruins:badlands_structures` | ruins | badlands | 3% | dark_oak 100, oak 11 | 99% | 0% | T:red_oasis, eroded_badlands, T:painted_mountains, badlands |
| `philipsruins:field_stone_ruins` | ruins | plains + highlands + forest | 5% | dark_oak 181, spruce 34, oak 22, jungle 10 | 80% | 11% | T:yellowstone, forest, plains, T:highlands |
| `forest_ruins:ruin_1` | ruins | plains + highlands + forest | 1% | spruce 2 | 33% | 67% | forest, dark_forest, T:temperate_highlands, T:hot_shrubland |
| `forest_ruins:ruin_2` | ruins | plains + highlands + forest | 2% | spruce 2 | 33% | 67% | forest, dark_forest, T:temperate_highlands, T:hot_shrubland |
| `forest_ruins:ruin_3` | ruins | plains + highlands + forest | 3% | spruce 10 | 33% | 67% | forest, dark_forest, T:temperate_highlands, T:hot_shrubland |
| `forest_ruins:ruin_5` | ruins | plains + highlands + forest | 8% | spruce 12 | 33% | 67% | forest, dark_forest, T:temperate_highlands, T:hot_shrubland |
| `forest_ruins:ruin_7` | ruins | plains + highlands + forest | 5% | spruce 4 | 33% | 67% | forest, dark_forest, T:temperate_highlands, T:hot_shrubland |
| `eugene:ruined_drum_tower` | ruins | plains + highlands | 16% | oak 2165, spruce 1335, birch 404, acacia 232 | 17% | 100% | plains, T:hot_shrubland, T:highlands, T:rocky_mountains |
| `forest_ruins:ruin_4` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `forest_ruins:ruin_6` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `philipsruins:field_stone_ruins_rocks` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |

### Other (27)

| Structure | Group | Fits now | Wood | Woods | Mismatch | Keep | Foreign in |
|---|---|---|---:|---|---:|---:|---|
| `betterdeserttemples:desert_temple` | desert_temples | desert + oasis | 2% | acacia 1385, birch 819, oak 363, warped 24 | 100% | 1% | T:lush_desert, T:red_oasis, T:desert_canyon, desert |
| `betterdungeons:zombie_dungeon` | entrance_dungeons | any | 0% | warped 1 | 100% | 0% | T:yellowstone, forest, plains, dark_forest |
| `eugene:red_roof_castle` | settlements | plains + forest | 6% | mangrove 875, oak 338, spruce 47 | 78% | 0% | forest, plains, meadow, T:sakura_grove |
| `betterstrongholds:stronghold` | standalone | own list | 2% | oak 535, spruce 484, crimson 24, birch 8 | 66% | 31% | T:yellowstone, dark_forest, T:lush_desert, forest |
| `cloudstorage:big_balloon` | sky | own list | 7% | oak 42 | 65% | 35% | T:yellowstone, dark_forest, T:lush_desert, snowy_plains |
| `underground_bunkers:underground_bunker` | standalone | own list | 0% | birch 50, spruce 45, oak 38, dark_oak 19 | 65% | 34% | T:yellowstone, dark_forest, forest, plains |
| `philipsruins:ancient_dungeon` | standalone | own list | 3% | spruce 93, oak 33 | 59% | 44% | forest, dark_forest, T:lush_desert, T:yellowstone |
| `betterdungeons:skeleton_dungeon` | standalone | any/deep + any | 1% | spruce 39, oak 7 | 57% | 44% | forest, dark_forest, T:lush_desert, T:brushland |
| `philipsruins:ancient_crypt` | standalone | plains + highlands + forest | 3% | spruce 166, oak 71 | 45% | 59% | forest, T:yellowstone, savanna, T:brushland |
| `philipsruins:antiquus_crypta` | standalone | plains + highlands + forest | 2% | spruce 79, oak 25 | 44% | 59% | forest, T:yellowstone, savanna, T:brushland |
| `oldvillagerstatue:old_villager_statue` | ruins | plains + highlands + forest | 2% | spruce 8 | 42% | 58% | forest, T:brushland, savanna, T:arid_highlands |
| `philipsruins:badlands_start_dungeon` | surface_dungeons | badlands | 0% | acacia 8 | 29% | 71% | T:red_oasis, T:ancient_sands |
| `subterrestrial:desert_cabin` | standalone | own list | 5% | acacia 183 | 15% | 85% | desert, T:desert_spires, T:desert_oasis, T:sandstone_valley |
| `eugene:undead_castle` | surface_dungeons | plains + taiga | 4% | spruce 407 | 5% | 95% | T:hot_shrubland, T:birch_taiga |
| `subterrestrial:jungle_cabin` | standalone | own list | 50% | jungle 2486, oak 36 | 1% | 100% | sparse_jungle, jungle, T:amethyst_rainforest, T:rocky_jungle |
| `subterrestrial:taiga_cabin` | standalone | own list | 91% | spruce 3107, dark_oak 46 | 1% | 100% | T:highlands, taiga, old_growth_pine_taiga, old_growth_spruce_taiga |
| `betterdungeons:small_dungeon` | standalone | any | 0% |  | 0% | 100% |  |
| `betterjungletemples:jungle_temple` | surface_dungeons | jungle | 0% | jungle 103 | 0% | 100% |  |
| `betterwitchhuts:witch_circle` | witch_huts | swamp | 0% |  | 0% | 100% |  |
| `cloudstorage:sky_temple` | sky | own list | 0% |  | 0% | 100% |  |
| `old_illager_statue:old_illager_statue` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `philipsruins:bone_dungeon` | standalone | any/deep + any | 0% |  | 0% | 100% |  |
| `small_cat_statues:cat_1` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `small_cat_statues:cat_2` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `small_cat_statues:cat_3` | ruins | plains + highlands + forest | 0% |  | 0% | 100% |  |
| `subterrestrial:mesa_cabin` | standalone | own list | 5% | acacia 183 | 0% | 100% |  |
| `towns_and_towers:mimic_desert` | surface_dungeons | desert + oasis | 0% |  | 0% | 100% |  |

