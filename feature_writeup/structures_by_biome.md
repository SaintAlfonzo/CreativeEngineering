# Structures by biome

Generated 2026-09-27 22:09 by `python3 tools/gen_structure_biome_table.py`. Do not edit; re-run it after changing `structure_groups.json`, the structure tags, biome merges, village pools or the mod list. Test prefix: none (a reference, checked against the game through ST-19 and ST-39 in [structure_tuning.md](structure_tuning.md)).

**How to read it.** Every structure that can spawn in a biome, grouped by kind. A rate like `1/48` means one structure of that kind per 48 x 48 chunks of that biome, as the game would place them now: the group grids, weights and frequencies in `structure_groups.json`, disabled members left out, structures another mod blocks in code left out. A structure is listed under its first kind in this order: Villages, Houses, Pillager, Sky, Surface dungeons, Ruins, Water, Underground dungeons, Underground, Environmental, Nether, End, Other dungeons (by the `type/*` and meta tags in `structure_tags.json`). Biome areas are each biome's share of its dimension layer after biome tuning (`biome_config.json`); 0% = removed by biome tuning. **none** = a hole.

## 0. Holes at a glance (overworld surface)

Biomes with at least 0.05% of the overworld surface and no structure of a kind, biggest first. Oceans and rivers are expected holes for land kinds.

| Kind | Biomes without it | Their share of the surface | Biomes |
|---|---|---|---|
| Villages | 28 of 96 | 34% | `ocean` 6.5%, `lukewarm_ocean` 5.2%, `warm_ocean` 4.8%, `cold_ocean` 3.6%, `river` 2.2%, `deep_frozen_ocean` 1.6%, `dark_forest` 1.3%, `frozen_ocean` 1.0%, `terralith:volcanic_peaks` 1.0%, `savanna_plateau` 0.8%, `windswept_savanna` 0.6%, `stony_peaks` 0.6%, `frozen_peaks` 0.6%, `jagged_peaks` 0.6%, `mangrove_swamp` 0.5%, `eroded_badlands` 0.4%, `frozen_river` 0.3%, `terralith:glacial_chasm` 0.3%, `snowy_beach` 0.3%, `terralith:stony_spires` 0.3%, `windswept_forest` 0.2%, `terralith:blooming_valley` 0.2%, `terralith:windswept_spires` 0.2%, `bamboo_jungle` 0.1%, `terralith:haze_mountain` 0.1%, `ice_spikes` 0.1%, `terralith:skylands_autumn` 0.1%, `terralith:warm_river` 0.1% |
| Houses | 53 of 96 | 64% | `deep_ocean` 7.8%, `ocean` 6.5%, `deep_lukewarm_ocean` 5.9%, `lukewarm_ocean` 5.2%, `warm_ocean` 4.8%, `deep_cold_ocean` 4.7%, `cold_ocean` 3.6%, `terralith:brushland` 3.6%, `deep_frozen_ocean` 1.6%, `badlands` 1.4%, `terralith:highlands` 1.2%, `terralith:alpine_highlands` 1.2%, `frozen_ocean` 1.0%, `terralith:volcanic_peaks` 1.0%, `terralith:hot_shrubland` 0.9%, `terralith:savanna_badlands` 0.9%, `desert` 0.8%, `jungle` 0.8%, `wooded_badlands` 0.8%, `sparse_jungle` 0.8%, `windswept_savanna` 0.6%, `stony_peaks` 0.6%, `frozen_peaks` 0.6%, `jagged_peaks` 0.6%, `terralith:arid_highlands` 0.5%, `terralith:temperate_highlands` 0.5%, `terralith:bryce_canyon` 0.5%, `terralith:orchid_swamp` 0.4%, `terralith:rocky_jungle` 0.4%, `terralith:siberian_taiga` 0.4%, `eroded_badlands` 0.4%, `terralith:shrubland` 0.3%, `terralith:blooming_plateau` 0.3%, `terralith:glacial_chasm` 0.3%, `terralith:steppe` 0.3%, `terralith:sakura_grove` 0.3%, `terralith:stony_spires` 0.3%, `terralith:tropical_jungle` 0.3%, `terralith:blooming_valley` 0.2%, `terralith:painted_mountains` 0.2%, `terralith:moonlight_valley` 0.2%, `mushroom_fields` 0.2%, `terralith:moonlight_grove` 0.2%, `terralith:warped_mesa` 0.2%, `terralith:windswept_spires` 0.2%, `bamboo_jungle` 0.1%, `ice_spikes` 0.1%, `terralith:lavender_valley` 0.1%, `terralith:skylands_autumn` 0.1%, `terralith:white_mesa` 0.1%, `terralith:desert_canyon` 0.1%, `terralith:sandstone_valley` 0.1%, `terralith:warm_river` 0.1% |
| Pillager | 22 of 96 | 29% | `ocean` 6.5%, `lukewarm_ocean` 5.2%, `warm_ocean` 4.8%, `cold_ocean` 3.6%, `river` 2.2%, `deep_frozen_ocean` 1.6%, `frozen_ocean` 1.0%, `savanna_plateau` 0.8%, `windswept_savanna` 0.6%, `terralith:cold_shrubland` 0.5%, `eroded_badlands` 0.4%, `frozen_river` 0.3%, `terralith:glacial_chasm` 0.3%, `terralith:stony_spires` 0.3%, `windswept_forest` 0.2%, `terralith:blooming_valley` 0.2%, `terralith:windswept_spires` 0.2%, `bamboo_jungle` 0.1%, `terralith:haze_mountain` 0.1%, `ice_spikes` 0.1%, `terralith:skylands_autumn` 0.1%, `terralith:warm_river` 0.1% |
| Surface dungeons | 80 of 96 | 92% | `deep_ocean` 7.8%, `ocean` 6.5%, `deep_lukewarm_ocean` 5.9%, `lukewarm_ocean` 5.2%, `warm_ocean` 4.8%, `deep_cold_ocean` 4.7%, `plains` 3.7%, `forest` 3.7%, `cold_ocean` 3.6%, `terralith:brushland` 3.6%, `terralith:yellowstone` 2.9%, `savanna` 2.5%, `river` 2.2%, `deep_frozen_ocean` 1.6%, `terralith:rocky_mountains` 1.6%, `taiga` 1.4%, `beach` 1.3%, `dark_forest` 1.3%, `terralith:highlands` 1.2%, `terralith:alpine_highlands` 1.2%, `meadow` 1.2%, `frozen_ocean` 1.0%, `old_growth_pine_taiga` 1.0%, `terralith:volcanic_peaks` 1.0%, `terralith:hot_shrubland` 0.9%, `terralith:savanna_badlands` 0.9%, `terralith:shield` 0.9%, `snowy_taiga` 0.9%, `savanna_plateau` 0.8%, `sparse_jungle` 0.8%, `old_growth_spruce_taiga` 0.7%, `snowy_plains` 0.7%, `windswept_savanna` 0.6%, `terralith:gravel_beach` 0.6%, `stony_peaks` 0.6%, `frozen_peaks` 0.6%, `jagged_peaks` 0.6%, `old_growth_birch_forest` 0.6%, `terralith:forested_highlands` 0.6%, `terralith:arid_highlands` 0.5%, `terralith:cold_shrubland` 0.5%, `terralith:temperate_highlands` 0.5%, `mangrove_swamp` 0.5%, `terralith:bryce_canyon` 0.5%, `birch_forest` 0.4%, `terralith:birch_taiga` 0.4%, `terralith:siberian_taiga` 0.4%, `terralith:snowy_badlands` 0.4%, `snowy_slopes` 0.3%, `terralith:fractured_savanna` 0.3%, `terralith:shrubland` 0.3%, `terralith:blooming_plateau` 0.3%, `terralith:glacial_chasm` 0.3%, `terralith:steppe` 0.3%, `snowy_beach` 0.3%, `terralith:sakura_grove` 0.3%, `terralith:stony_spires` 0.3%, `grove` 0.2%, `windswept_forest` 0.2%, `terralith:blooming_valley` 0.2%, `terralith:painted_mountains` 0.2%, `terralith:moonlight_valley` 0.2%, `flower_forest` 0.2%, `mushroom_fields` 0.2%, `terralith:moonlight_grove` 0.2%, `terralith:warped_mesa` 0.2%, `terralith:windswept_spires` 0.2%, `terralith:wintry_forest` 0.2%, `terralith:haze_mountain` 0.1%, `terralith:alpine_grove` 0.1%, `terralith:gravel_desert` 0.1%, `terralith:lavender_valley` 0.1%, `terralith:lush_valley` 0.1%, `terralith:skylands_autumn` 0.1%, `terralith:white_mesa` 0.1%, `sunflower_plains` 0.1%, `terralith:siberian_grove` 0.1%, `terralith:snowy_maple_forest` 0.1%, `terralith:warm_river` 0.1%, `terralith:yosemite_lowlands` 0.1% |
| Ruins | 50 of 96 | 62% | `deep_ocean` 7.8%, `ocean` 6.5%, `deep_lukewarm_ocean` 5.9%, `lukewarm_ocean` 5.2%, `warm_ocean` 4.8%, `deep_cold_ocean` 4.7%, `cold_ocean` 3.6%, `terralith:brushland` 3.6%, `river` 2.2%, `deep_frozen_ocean` 1.6%, `beach` 1.3%, `terralith:highlands` 1.2%, `terralith:alpine_highlands` 1.2%, `frozen_ocean` 1.0%, `terralith:hot_shrubland` 0.9%, `terralith:savanna_badlands` 0.9%, `terralith:gravel_beach` 0.6%, `terralith:arid_highlands` 0.5%, `terralith:cold_shrubland` 0.5%, `terralith:temperate_highlands` 0.5%, `terralith:bryce_canyon` 0.5%, `terralith:orchid_swamp` 0.4%, `terralith:siberian_taiga` 0.4%, `terralith:snowy_badlands` 0.4%, `terralith:ice_marsh` 0.4%, `terralith:fractured_savanna` 0.3%, `terralith:shrubland` 0.3%, `frozen_river` 0.3%, `terralith:blooming_plateau` 0.3%, `terralith:steppe` 0.3%, `snowy_beach` 0.3%, `terralith:sakura_grove` 0.3%, `terralith:stony_spires` 0.3%, `windswept_forest` 0.2%, `terralith:blooming_valley` 0.2%, `terralith:painted_mountains` 0.2%, `terralith:moonlight_valley` 0.2%, `mushroom_fields` 0.2%, `terralith:moonlight_grove` 0.2%, `terralith:warped_mesa` 0.2%, `terralith:windswept_spires` 0.2%, `terralith:haze_mountain` 0.1%, `ice_spikes` 0.1%, `terralith:gravel_desert` 0.1%, `terralith:lavender_valley` 0.1%, `terralith:skylands_autumn` 0.1%, `terralith:white_mesa` 0.1%, `terralith:siberian_grove` 0.1%, `terralith:snowy_maple_forest` 0.1%, `terralith:warm_river` 0.1% |

Village styles that never build some profession buildings (section 11 has the full matrix; names that hide the trade are listed there):

- `kaisyn:village/birch_forest_romanian` (`towns_and_towers:village_birch_forest`): cleric
- `kaisyn:village/exclusives/classic` (`towns_and_towers:exclusives/village_classic`): armorer, cartographer, fisherman, fletcher, leatherworker, mason, shepherd, toolsmith, weaponsmith
- `kaisyn:village/exclusives/mediterranean` (`towns_and_towers:exclusives/village_mediterranean`): cleric
- `kaisyn:village/exclusives/nilotic` (`towns_and_towers:exclusives/village_nilotic`): leatherworker, librarian
- `kaisyn:village/exclusives/wandering_trader_camp` (`towns_and_towers:exclusives/village_wandering_trader_camp`): **none at all**
- `kaisyn:village/mushroom_fields_fantasy` (`towns_and_towers:village_mushroom_fields`): cleric, librarian
- `kaisyn:village/sparse_jungle_polynesian` (`towns_and_towers:village_sparse_jungle`): cleric
- `kaisyn:village/wooded_badlands_tipi` (`towns_and_towers:village_wooded_badlands`): **none at all**
- `terralith:village/desert` (`terralith:fortified_desert_village`): armorer, cleric, leatherworker
- `terralith:village/fortified` (`terralith:fortified_village`): armorer, leatherworker, toolsmith, weaponsmith

## 1. Overworld, surface: kinds per biome

Cell = number of structures of that kind that can spawn there, and their combined rate.

| Biome | Area | Villages | Houses | Pillager | Sky | Surface dungeons | Ruins | Water | Underground dungeons | Underground | Environmental | Other dungeons |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `deep_ocean` | 7.83% | 1 · 1/60 | **none** | 1 · 1/84 | 2 · 1/89 | **none** | **none** | 5 · 1/20 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | 1 · 1/101 |
| `ocean` | 6.53% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | 5 · 1/21 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | **none** |
| `deep_lukewarm_ocean` | 5.87% | 1 · 1/60 | **none** | 1 · 1/84 | 2 · 1/89 | **none** | **none** | 5 · 1/17 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | 1 · 1/101 |
| `lukewarm_ocean` | 5.17% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | 5 · 1/17 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | **none** |
| `warm_ocean` | 4.83% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | 5 · 1/17 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `deep_cold_ocean` | 4.73% | 1 · 1/60 | **none** | 1 · 1/84 | 2 · 1/89 | **none** | **none** | 5 · 1/20 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | 1 · 1/101 |
| `plains` | 3.73% | 2 · 1/49 | 12 · 1/17 | 1 · 1/41 | 2 · 1/89 | **none** | 10 · 1/9.6 | **none** | 9 · 1/5.5 | 1 · 1/16 | 1 · 1/126 | **none** |
| `forest` | 3.70% | 1 · 1/115 | 9 · 1/23 | 1 · 1/60 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `cold_ocean` | 3.60% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | 5 · 1/21 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | **none** |
| `terralith:brushland` | 3.57% | 3 · 1/51 | **none** | 2 · 1/45 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:yellowstone` | 2.90% | 1 · 1/103 | 4 · 1/54 | 3 · 1/42 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `savanna` | 2.50% | 1 · 1/53 | 5 · 1/28 | 2 · 1/39 | 2 · 1/89 | **none** | 4 · 1/10 | **none** | 9 · 1/5.5 | 1 · 1/16 | 1 · 1/126 | **none** |
| `river` | 2.20% | **none** | 1 · 1/49 | **none** | 2 · 1/89 | **none** | **none** | 2 · 1/59 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `deep_frozen_ocean` | 1.57% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | 5 · 1/20 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | 1 · 1/101 |
| `terralith:rocky_mountains` | 1.57% | 2 · 1/61 | 1 · 1/33 | 2 · 1/41 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `taiga` | 1.43% | 1 · 1/49 | 6 · 1/32 | 2 · 1/37 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `badlands` | 1.37% | 1 · 1/104 | **none** | 1 · 1/55 | 2 · 1/89 | 1 · 1/25 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `beach` | 1.30% | 1 · 1/119 | 1 · 1/91 | 1 · 1/49 | 2 · 1/89 | **none** | **none** | 2 · 1/59 | 1 · 1/7.1 | 2 · 1/7.3 | 1 · 1/126 | **none** |
| `dark_forest` | 1.30% | **none** | 8 · 1/24 | 1 · 1/46 | 2 · 1/89 | **none** | 11 · 1/9.3 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:highlands` | 1.23% | 2 · 1/56 | **none** | 1 · 1/60 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:alpine_highlands` | 1.20% | 3 · 1/57 | **none** | 2 · 1/42 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `meadow` | 1.17% | 3 · 1/51 | 3 · 1/41 | 2 · 1/47 | 2 · 1/89 | **none** | 10 · 1/9.6 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `frozen_ocean` | 1.03% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | 4 · 1/20 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `old_growth_pine_taiga` | 1.03% | 1 · 1/103 | 6 · 1/32 | 1 · 1/50 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:volcanic_peaks` | 1.03% | **none** | **none** | 1 · 1/41 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:hot_shrubland` | 0.90% | 2 · 1/52 | **none** | 2 · 1/45 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:savanna_badlands` | 0.90% | 3 · 1/77 | **none** | 3 · 1/41 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 2 · 1/16 | 1 · 1/126 | **none** |
| `terralith:shield` | 0.90% | 4 · 1/66 | 4 · 1/54 | 5 · 1/44 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `snowy_taiga` | 0.87% | 1 · 1/109 | 6 · 1/36 | 1 · 1/58 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `desert` | 0.83% | 2 · 1/55 | **none** | 2 · 1/50 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `savanna_plateau` | 0.83% | **none** | 1 · 1/47 | **none** | 2 · 1/89 | **none** | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `jungle` | 0.80% | 1 · 1/72 | **none** | 1 · 1/54 | 2 · 1/89 | 1 · 1/18 | 9 · 1/12 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `wooded_badlands` | 0.80% | 1 · 1/115 | **none** | 1 · 1/37 | 2 · 1/89 | 1 · 1/25 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `sparse_jungle` | 0.77% | 1 · 1/72 | **none** | 1 · 1/39 | 2 · 1/89 | **none** | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `old_growth_spruce_taiga` | 0.73% | 1 · 1/103 | 6 · 1/32 | 1 · 1/50 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `snowy_plains` | 0.73% | 1 · 1/48 | 1 · 1/33 | 2 · 1/47 | 2 · 1/89 | **none** | 4 · 1/10 | **none** | 9 · 1/5.5 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:jungle_mountains` | 0.70% | 2 · 1/72 | 3 · 1/22 | 2 · 1/45 | 2 · 1/89 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `windswept_savanna` | 0.63% | **none** | **none** | **none** | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:gravel_beach` | 0.63% | 1 · 1/119 | 1 · 1/91 | 1 · 1/49 | 2 · 1/89 | **none** | **none** | 2 · 1/59 | 1 · 1/7.1 | 2 · 1/7.3 | 1 · 1/126 | **none** |
| `stony_peaks` | 0.60% | **none** | **none** | 1 · 1/41 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `swamp` | 0.60% | 1 · 1/68 | 1 · 1/46 | 2 · 1/20 | 2 · 1/89 | 1 · 1/31 | 11 · 1/9.3 | **none** | 9 · 1/5.5 | 1 · 1/16 | 1 · 1/126 | **none** |
| `frozen_peaks` | 0.57% | **none** | **none** | 1 · 1/41 | 2 · 1/89 | **none** | 9 · 1/8.8 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `jagged_peaks` | 0.57% | **none** | **none** | 1 · 1/41 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `old_growth_birch_forest` | 0.57% | 1 · 1/108 | 7 · 1/26 | 1 · 1/60 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:forested_highlands` | 0.57% | 5 · 1/62 | 4 · 1/54 | 4 · 1/44 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:arid_highlands` | 0.53% | 2 · 1/67 | **none** | 2 · 1/45 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:cold_shrubland` | 0.53% | 2 · 1/49 | 1 · 1/33 | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:temperate_highlands` | 0.53% | 4 · 1/68 | **none** | 2 · 1/60 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `mangrove_swamp` | 0.47% | **none** | 1 · 1/46 | 1 · 1/18 | 2 · 1/89 | **none** | 12 · 1/11 | **none** | 4 · 1/6.9 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:bryce_canyon` | 0.47% | 2 · 1/110 | **none** | 2 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 2 · 1/16 | 1 · 1/126 | **none** |
| `terralith:orchid_swamp` | 0.43% | 1 · 1/68 | **none** | 2 · 1/20 | 2 · 1/89 | 1 · 1/31 | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `birch_forest` | 0.40% | 1 · 1/108 | 8 · 1/24 | 1 · 1/60 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:birch_taiga` | 0.40% | 3 · 1/70 | 4 · 1/54 | 3 · 1/48 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:rocky_jungle` | 0.40% | 2 · 1/72 | **none** | 2 · 1/45 | 2 · 1/89 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:siberian_taiga` | 0.40% | 2 · 1/60 | **none** | 2 · 1/42 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:snowy_badlands` | 0.40% | 2 · 1/65 | 1 · 1/33 | 1 · 1/55 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `eroded_badlands` | 0.37% | **none** | **none** | **none** | 2 · 1/89 | 1 · 1/25 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:ice_marsh` | 0.37% | 2 · 1/56 | 1 · 1/33 | 2 · 1/20 | 2 · 1/89 | 1 · 1/31 | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `snowy_slopes` | 0.33% | 1 · 1/91 | 1 · 1/33 | 2 · 1/41 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:fractured_savanna` | 0.33% | 1 · 1/53 | 3 · 1/22 | 1 · 1/36 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:shrubland` | 0.33% | 2 · 1/52 | **none** | 2 · 1/45 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `frozen_river` | 0.30% | **none** | 1 · 1/49 | **none** | 2 · 1/89 | 1 · 1/25 | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:blooming_plateau` | 0.30% | 2 · 1/52 | **none** | 1 · 1/52 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:glacial_chasm` | 0.30% | **none** | **none** | **none** | 2 · 1/89 | **none** | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:steppe` | 0.30% | 2 · 1/65 | **none** | 1 · 1/60 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `snowy_beach` | 0.27% | **none** | 1 · 1/91 | 1 · 1/49 | 2 · 1/89 | **none** | **none** | 2 · 1/59 | 1 · 1/7.1 | 2 · 1/7.3 | 1 · 1/126 | **none** |
| `terralith:sakura_grove` | 0.27% | 1 · 1/119 | **none** | 3 · 1/35 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:stony_spires` | 0.27% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:tropical_jungle` | 0.27% | 2 · 1/72 | **none** | 2 · 1/45 | 2 · 1/89 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `grove` | 0.23% | 1 · 1/117 | 6 · 1/34 | 2 · 1/52 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `windswept_forest` | 0.23% | **none** | 4 · 1/20 | **none** | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:blooming_valley` | 0.23% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:painted_mountains` | 0.23% | 2 · 1/110 | **none** | 2 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 2 · 1/16 | 1 · 1/126 | **none** |
| `terralith:moonlight_valley` | 0.20% | 1 · 1/119 | **none** | 1 · 1/58 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `flower_forest` | 0.17% | 1 · 1/119 | 7 · 1/26 | 1 · 1/58 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `mushroom_fields` | 0.17% | 1 · 1/119 | **none** | 1 · 1/60 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:moonlight_grove` | 0.17% | 1 · 1/119 | **none** | 1 · 1/58 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:warped_mesa` | 0.17% | 3 · 1/34 | **none** | 3 · 1/39 | 2 · 1/89 | **none** | **none** | **none** | 5 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:windswept_spires` | 0.17% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:wintry_forest` | 0.17% | 3 · 1/68 | 5 · 1/42 | 3 · 1/47 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `bamboo_jungle` | 0.13% | **none** | **none** | **none** | 2 · 1/89 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:haze_mountain` | 0.13% | **none** | 3 · 1/22 | **none** | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `ice_spikes` | 0.10% | **none** | **none** | **none** | 2 · 1/89 | 1 · 1/25 | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:alpine_grove` | 0.10% | 3 · 1/73 | 7 · 1/34 | 1 · 1/61 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:gravel_desert` | 0.10% | 1 · 1/48 | 1 · 1/33 | 1 · 1/51 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:lavender_valley` | 0.10% | 1 · 1/119 | **none** | 1 · 1/58 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:lush_valley` | 0.10% | 2 · 1/60 | 4 · 1/54 | 3 · 1/42 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:skylands_autumn` | 0.10% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:white_mesa` | 0.10% | 2 · 1/110 | **none** | 2 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 5 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `sunflower_plains` | 0.07% | 2 · 1/76 | 12 · 1/23 | 1 · 1/60 | 2 · 1/89 | **none** | 4 · 1/10 | **none** | 9 · 1/5.5 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:desert_canyon` | 0.07% | 1 · 1/57 | **none** | 1 · 1/58 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:sandstone_valley` | 0.07% | 1 · 1/57 | **none** | 1 · 1/58 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:siberian_grove` | 0.07% | 3 · 1/74 | 1 · 1/33 | 3 · 1/50 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:snowy_maple_forest` | 0.07% | 4 · 1/66 | 1 · 1/33 | 4 · 1/44 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:warm_river` | 0.07% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:yosemite_lowlands` | 0.07% | 1 · 1/103 | 4 · 1/54 | 2 · 1/42 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `windswept_gravelly_hills` | 0.03% | **none** | 3 · 1/22 | **none** | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `windswept_hills` | 0.03% | **none** | 3 · 1/22 | **none** | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:amethyst_rainforest` | 0.03% | 2 · 1/72 | **none** | 2 · 1/45 | 2 · 1/89 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:ancient_sands` | 0.03% | 2 · 1/110 | **none** | 2 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 5 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:desert_oasis` | 0.03% | 3 · 1/30 | **none** | 2 · 1/45 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:emerald_peaks` | 0.03% | 1 · 1/48 | 1 · 1/33 | 1 · 1/41 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:lavender_forest` | 0.03% | 2 · 1/76 | **none** | 2 · 1/52 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:lush_desert` | 0.03% | 2 · 1/55 | **none** | 1 · 1/58 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:mirage_isles` | 0.03% | 1 · 1/119 | **none** | 1 · 1/60 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:red_oasis` | 0.03% | 2 · 1/110 | **none** | 2 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 5 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:savanna_slopes` | 0.03% | 1 · 1/53 | **none** | 1 · 1/36 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:skylands_spring` | 0.03% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:skylands_summer` | 0.03% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:skylands_winter` | 0.03% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:volcanic_crater` | 0.03% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:yosemite_cliffs` | 0.03% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `deep_dark` | 0.00% | **none** | **none** | **none** | 2 · 1/89 | **none** | 2 · 1/23 | **none** | 5 · 1/6.6 | **none** | 1 · 1/126 | **none** |
| `stony_shore` | 0.00% | **none** | **none** | **none** | 2 · 1/89 | **none** | 7 · 1/7.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:alpha_islands` | 0.00% | 1 · 1/119 | **none** | 1 · 1/67 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:alpha_islands_winter` | 0.00% | 1 · 1/48 | 1 · 1/33 | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:amethyst_canyon` | 0.00% | 2 · 1/72 | **none** | 2 · 1/45 | 2 · 1/89 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:ashen_savanna` | 0.00% | 1 · 1/53 | **none** | 1 · 1/36 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:basalt_cliffs` | 0.00% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:caldera` | 0.00% | **none** | **none** | 1 · 1/41 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:cave/frostfire_caves` | 0.00% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 2 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:cloud_forest` | 0.00% | 2 · 1/114 | 7 · 1/32 | 3 · 1/50 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:desert_spires` | 0.00% | 1 · 1/57 | **none** | 1 · 1/58 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:frozen_cliffs` | 0.00% | 1 · 1/119 | **none** | 1 · 1/49 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:granite_cliffs` | 0.00% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:rocky_shrubland` | 0.00% | 2 · 1/49 | 1 · 1/33 | 2 · 1/48 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:sakura_valley` | 0.00% | 1 · 1/119 | **none** | 3 · 1/35 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:shield_clearing` | 0.00% | 2 · 1/60 | **none** | 2 · 1/45 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:snowy_shield` | 0.00% | 2 · 1/60 | 1 · 1/33 | 1 · 1/58 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:valley_clearing` | 0.00% | 1 · 1/103 | **none** | 2 · 1/42 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:white_cliffs` | 0.00% | 1 · 1/108 | **none** | 1 · 1/60 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** |
| `terralith:wintry_lowlands` | 0.00% | 3 · 1/68 | 1 · 1/33 | 3 · 1/47 | 2 · 1/89 | 1 · 1/25 | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** |

## 2. Overworld, caves: kinds per biome

Cell = number of structures of that kind that can spawn there, and their combined rate.

| Biome | Area | Villages | Pillager | Sky | Ruins | Underground dungeons | Underground | Environmental |
|---|---|---|---|---|---|---|---|---|
| `terralith:cave/deep_caves` | 13.30% | **none** | **none** | 2 · 1/89 | **none** | 1 · 1/7.1 | 3 · 1/7.8 | 1 · 1/126 |
| `lush_caves` | 12.16% | **none** | 1 · 1/7.8 | 2 · 1/89 | 2 · 1/7.2 | 7 · 1/5.6 | 3 · 1/5.2 | 1 · 1/126 |
| `terralith:cave/andesite_caves` | 8.15% | **none** | **none** | 2 · 1/89 | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 |
| `terralith:cave/granite_caves` | 3.39% | **none** | **none** | 2 · 1/89 | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 |
| `terralith:cave/infested_caves` | 3.34% | **none** | **none** | 2 · 1/89 | **none** | 1 · 1/7.1 | 2 · 1/7.7 | 1 · 1/126 |
| `dripstone_caves` | 2.18% | **none** | **none** | 2 · 1/89 | 1 · 1/38 | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 |
| `terralith:cave/fungal_caves` | 1.31% | **none** | 1 · 1/7.9 | 2 · 1/89 | **none** | 6 · 1/5.6 | 2 · 1/8.1 | 1 · 1/126 |
| `terralith:cave/thermal_caves` | 1.28% | **none** | **none** | 2 · 1/89 | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 |
| `terralith:cave/diorite_caves` | 0.79% | **none** | **none** | 2 · 1/89 | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 |
| `terralith:cave/underground_jungle` | 0.74% | **none** | 1 · 1/7.2 | 2 · 1/89 | **none** | 1 · 1/7.1 | 3 · 1/4.8 | 1 · 1/126 |
| `terralith:scarlet_mountains` | 0.04% | 1 · 1/103 | 3 · 1/42 | 2 · 1/89 | 3 · 1/9.4 | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 |

## 3. Nether: kinds per biome

Cell = number of structures of that kind that can spawn there, and their combined rate.

| Biome | Area | Houses | Environmental | Nether |
|---|---|---|---|---|
| `nether_wastes` | 50.17% | 1 · 1/47 | 2 · 1/5.7 | 5 · 1/15 |
| `soul_sand_valley` | 17.80% | **none** | 1 · 1/2.0 | 5 · 1/15 |
| `crimson_forest` | 15.30% | 1 · 1/38 | 1 · 1/20 | 6 · 1/9.5 |
| `basalt_deltas` | 11.30% | **none** | **none** | 4 · 1/17 |
| `warped_forest` | 5.43% | 1 · 1/38 | **none** | 6 · 1/9.5 |

## 4. End: kinds per biome

Cell = number of structures of that kind that can spawn there, and their combined rate.

| Biome | Area | Water | Environmental | End |
|---|---|---|---|---|
| `end_highlands` | 73.33% | 1 · 1/27 | 4 · 1/7.9 | 14 · 1/7.5 |
| `small_end_islands` | 8.67% | 1 · 1/27 | 4 · 1/7.9 | 11 · 1/8.2 |
| `the_end` | 0.50% | 1 · 1/27 | 4 · 1/7.9 | 11 · 1/8.2 |

## 5. End (inner biomes): kinds per biome

Cell = number of structures of that kind that can spawn there, and their combined rate.

| Biome | Area | Water | Environmental | End |
|---|---|---|---|---|
| `nullscape:shadowlands` | 12.49% | 1 · 1/27 | 4 · 1/7.9 | 13 · 1/7.5 |
| `nullscape:crystal_peaks` | 5.94% | 1 · 1/27 | 4 · 1/7.9 | 12 · 1/7.8 |
| `nullscape:void_barrens` | 5.09% | 1 · 1/27 | 4 · 1/7.9 | 12 · 1/7.8 |

## 6. Villages by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Villages |
|---|---|---|
| `deep_ocean` | 7.83% | `towns_and_towers:village_ocean` 1/60 |
| `ocean` | 6.53% | **none** |
| `deep_lukewarm_ocean` | 5.87% | `towns_and_towers:village_ocean` 1/60 |
| `lukewarm_ocean` | 5.17% | **none** |
| `warm_ocean` | 4.83% | **none** |
| `deep_cold_ocean` | 4.73% | `towns_and_towers:village_ocean` 1/60 |
| `plains` | 3.73% | `village_plains` 1/57, `terralith:fortified_village` 1/97 |
| `forest` | 3.70% | `towns_and_towers:village_forest` 1/115 |
| `cold_ocean` | 3.60% | **none** |
| `terralith:brushland` | 3.57% | `village_savanna` 1/66, `terralith:fortified_village` 1/108, `towns_and_towers:exclusives/village_mediterranean` 1/123 |
| `terralith:yellowstone` | 2.90% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `savanna` | 2.50% | `village_savanna` 1/53 |
| `river` | 2.20% | **none** |
| `deep_frozen_ocean` | 1.57% | **none** |
| `terralith:rocky_mountains` | 1.57% | `village_snowy` 1/69, `towns_and_towers:village_snowy_slopes` 1/126 |
| `taiga` | 1.43% | `village_taiga` 1/49 |
| `badlands` | 1.37% | `towns_and_towers:village_badlands` 1/104 |
| `beach` | 1.30% | `towns_and_towers:village_beach` 1/119 |
| `dark_forest` | 1.30% | **none** |
| `terralith:highlands` | 1.23% | `village_taiga` 1/63, `towns_and_towers:exclusives/village_tudor` 1/119 |
| `terralith:alpine_highlands` | 1.20% | `village_taiga` 1/71, `terralith:fortified_village` 1/117, `towns_and_towers:village_old_growth_taiga` 1/175 |
| `meadow` | 1.17% | `village_plains` 1/65, `terralith:fortified_village` 1/110, `towns_and_towers:village_meadow` 1/122 |
| `frozen_ocean` | 1.03% | **none** |
| `old_growth_pine_taiga` | 1.03% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `terralith:volcanic_peaks` | 1.03% | **none** |
| `terralith:hot_shrubland` | 0.90% | `village_savanna` 1/59, `towns_and_towers:exclusives/village_mediterranean` 1/110 |
| `terralith:savanna_badlands` | 0.90% | `village_savanna` 1/94, `towns_and_towers:village_badlands` 1/187, `towns_and_towers:village_wooded_badlands` 1/189 |
| `terralith:shield` | 0.90% | `village_taiga` 1/79, `towns_and_towers:village_old_growth_taiga` 1/196, `towns_and_towers:exclusives/village_swedish` 1/203, `towns_and_towers:village_snowy_taiga` 1/208 |
| `snowy_taiga` | 0.87% | `towns_and_towers:village_snowy_taiga` 1/109 |
| `desert` | 0.83% | `village_desert` 1/64, `terralith:fortified_desert_village` 1/107 |
| `savanna_plateau` | 0.83% | **none** |
| `jungle` | 0.80% | `towns_and_towers:village_jungle` 1/72 |
| `wooded_badlands` | 0.80% | `towns_and_towers:village_wooded_badlands` 1/115 |
| `sparse_jungle` | 0.77% | `towns_and_towers:village_sparse_jungle` 1/72 |
| `old_growth_spruce_taiga` | 0.73% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `snowy_plains` | 0.73% | `village_snowy` 1/48 |
| `terralith:jungle_mountains` | 0.70% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `windswept_savanna` | 0.63% | **none** |
| `terralith:gravel_beach` | 0.63% | `towns_and_towers:village_beach` 1/119 |
| `stony_peaks` | 0.60% | **none** |
| `swamp` | 0.60% | `towns_and_towers:village_swamp` 1/68 |
| `frozen_peaks` | 0.57% | **none** |
| `jagged_peaks` | 0.57% | **none** |
| `old_growth_birch_forest` | 0.57% | `towns_and_towers:village_birch_forest` 1/108 |
| `terralith:forested_highlands` | 0.57% | `village_taiga` 1/84, `terralith:fortified_village` 1/139, `towns_and_towers:village_old_growth_taiga` 1/208, `towns_and_towers:exclusives/village_swedish` 1/216, `towns_and_towers:village_snowy_taiga` 1/221 |
| `terralith:arid_highlands` | 0.53% | `village_savanna` 1/75, `towns_and_towers:village_badlands` 1/148 |
| `terralith:cold_shrubland` | 0.53% | `village_taiga` 1/67, `village_snowy` 1/71 |
| `terralith:temperate_highlands` | 0.53% | `village_plains` 1/89, `terralith:fortified_village` 1/150, `towns_and_towers:village_birch_forest` 1/194, `towns_and_towers:village_forest` 1/218 |
| `mangrove_swamp` | 0.47% | **none** |
| `terralith:bryce_canyon` | 0.47% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:orchid_swamp` | 0.43% | `towns_and_towers:village_swamp` 1/68 |
| `birch_forest` | 0.40% | `towns_and_towers:village_birch_forest` 1/108 |
| `terralith:birch_taiga` | 0.40% | `village_plains` 1/82, `towns_and_towers:village_birch_forest` 1/177, `towns_and_towers:village_old_growth_taiga` 1/205 |
| `terralith:rocky_jungle` | 0.40% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `terralith:siberian_taiga` | 0.40% | `village_taiga` 1/65, `towns_and_towers:village_old_growth_taiga` 1/160 |
| `terralith:snowy_badlands` | 0.40% | `village_snowy` 1/73, `towns_and_towers:village_badlands` 1/139 |
| `eroded_badlands` | 0.37% | **none** |
| `terralith:ice_marsh` | 0.37% | `village_snowy` 1/69, `towns_and_towers:village_swamp` 1/94 |
| `snowy_slopes` | 0.33% | `towns_and_towers:village_snowy_slopes` 1/91 |
| `terralith:fractured_savanna` | 0.33% | `village_savanna` 1/53 |
| `terralith:shrubland` | 0.33% | `village_savanna` 1/59, `towns_and_towers:exclusives/village_mediterranean` 1/110 |
| `frozen_river` | 0.30% | **none** |
| `terralith:blooming_plateau` | 0.30% | `village_plains` 1/59, `towns_and_towers:village_meadow` 1/109 |
| `terralith:glacial_chasm` | 0.30% | **none** |
| `terralith:steppe` | 0.30% | `village_plains` 1/77, `towns_and_towers:village_sunflower_plains` 1/123 |
| `snowy_beach` | 0.27% | **none** |
| `terralith:sakura_grove` | 0.27% | `towns_and_towers:village_flower_forest` 1/119 |
| `terralith:stony_spires` | 0.27% | **none** |
| `terralith:tropical_jungle` | 0.27% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `grove` | 0.23% | `towns_and_towers:village_grove` 1/117 |
| `windswept_forest` | 0.23% | **none** |
| `terralith:blooming_valley` | 0.23% | **none** |
| `terralith:painted_mountains` | 0.23% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:moonlight_valley` | 0.20% | `towns_and_towers:village_flower_forest` 1/119 |
| `flower_forest` | 0.17% | `towns_and_towers:village_flower_forest` 1/119 |
| `mushroom_fields` | 0.17% | `towns_and_towers:village_mushroom_fields` 1/119 |
| `terralith:moonlight_grove` | 0.17% | `towns_and_towers:village_flower_forest` 1/119 |
| `terralith:warped_mesa` | 0.17% | `towns_and_towers:exclusives/village_wandering_trader_camp` 1/35, `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:windswept_spires` | 0.17% | **none** |
| `terralith:wintry_forest` | 0.17% | `village_snowy` 1/78, `towns_and_towers:village_old_growth_taiga` 1/185, `towns_and_towers:village_snowy_taiga` 1/196 |
| `bamboo_jungle` | 0.13% | **none** |
| `terralith:haze_mountain` | 0.13% | **none** |
| `ice_spikes` | 0.10% | **none** |
| `terralith:alpine_grove` | 0.10% | `village_snowy` 1/92, `towns_and_towers:village_snowy_slopes` 1/168, `towns_and_towers:village_grove` 1/176 |
| `terralith:gravel_desert` | 0.10% | `village_snowy` 1/48 |
| `terralith:lavender_valley` | 0.10% | `towns_and_towers:village_flower_forest` 1/119 |
| `terralith:lush_valley` | 0.10% | `village_taiga` 1/65, `towns_and_towers:village_old_growth_taiga` 1/160 |
| `terralith:skylands_autumn` | 0.10% | **none** |
| `terralith:white_mesa` | 0.10% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `sunflower_plains` | 0.07% | `towns_and_towers:village_sunflower_plains` 1/104, `terralith:fortified_village` 1/111 |
| `terralith:desert_canyon` | 0.07% | `village_desert` 1/57 |
| `terralith:sandstone_valley` | 0.07% | `village_desert` 1/57 |
| `terralith:siberian_grove` | 0.07% | `village_snowy` 1/89, `towns_and_towers:village_grove` 1/170, `towns_and_towers:village_old_growth_taiga` 1/210 |
| `terralith:snowy_maple_forest` | 0.07% | `village_snowy` 1/81, `towns_and_towers:village_old_growth_taiga` 1/190, `towns_and_towers:exclusives/village_swedish` 1/197, `towns_and_towers:village_snowy_taiga` 1/202 |
| `terralith:warm_river` | 0.07% | **none** |
| `terralith:yosemite_lowlands` | 0.07% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `windswept_gravelly_hills` | 0.03% | **none** |
| `windswept_hills` | 0.03% | **none** |
| `terralith:amethyst_rainforest` | 0.03% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `terralith:ancient_sands` | 0.03% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:desert_oasis` | 0.03% | `towns_and_towers:exclusives/village_wandering_trader_camp` 1/35, `village_desert` 1/64, `terralith:fortified_desert_village` 1/107 |
| `terralith:emerald_peaks` | 0.03% | `village_snowy` 1/48 |
| `terralith:lavender_forest` | 0.03% | `village_plains` 1/90, `towns_and_towers:village_flower_forest` 1/143 |
| `terralith:lush_desert` | 0.03% | `village_desert` 1/64, `terralith:fortified_desert_village` 1/107 |
| `terralith:mirage_isles` | 0.03% | `towns_and_towers:village_mushroom_fields` 1/119 |
| `terralith:red_oasis` | 0.03% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:savanna_slopes` | 0.03% | `village_savanna` 1/53 |
| `terralith:skylands_spring` | 0.03% | **none** |
| `terralith:skylands_summer` | 0.03% | **none** |
| `terralith:skylands_winter` | 0.03% | **none** |
| `terralith:volcanic_crater` | 0.03% | **none** |
| `terralith:yosemite_cliffs` | 0.03% | **none** |
| `deep_dark` | 0.00% | **none** |
| `stony_shore` | 0.00% | **none** |
| `terralith:alpha_islands` | 0.00% | `towns_and_towers:exclusives/village_classic` 1/119 |
| `terralith:alpha_islands_winter` | 0.00% | `village_snowy` 1/48 |
| `terralith:amethyst_canyon` | 0.00% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `terralith:ashen_savanna` | 0.00% | `village_savanna` 1/53 |
| `terralith:basalt_cliffs` | 0.00% | **none** |
| `terralith:caldera` | 0.00% | **none** |
| `terralith:cave/frostfire_caves` | 0.00% | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** |
| `terralith:cloud_forest` | 0.00% | `towns_and_towers:village_flower_forest` 1/136, `towns_and_towers:village_old_growth_taiga` 1/214 |
| `terralith:desert_spires` | 0.00% | `village_desert` 1/57 |
| `terralith:frozen_cliffs` | 0.00% | `towns_and_towers:village_beach` 1/119 |
| `terralith:granite_cliffs` | 0.00% | **none** |
| `terralith:rocky_shrubland` | 0.00% | `village_taiga` 1/67, `village_snowy` 1/71 |
| `terralith:sakura_valley` | 0.00% | `towns_and_towers:village_flower_forest` 1/119 |
| `terralith:shield_clearing` | 0.00% | `village_taiga` 1/65, `towns_and_towers:village_snowy_taiga` 1/169 |
| `terralith:snowy_shield` | 0.00% | `village_snowy` 1/65, `towns_and_towers:village_snowy_taiga` 1/162 |
| `terralith:valley_clearing` | 0.00% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `terralith:white_cliffs` | 0.00% | `towns_and_towers:village_birch_forest` 1/108 |
| `terralith:wintry_lowlands` | 0.00% | `village_snowy` 1/78, `towns_and_towers:village_old_growth_taiga` 1/185, `towns_and_towers:village_snowy_taiga` 1/196 |
| `terralith:scarlet_mountains` | 0.04% | `towns_and_towers:village_old_growth_taiga` 1/103 |

## 7. Houses by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Houses |
|---|---|---|
| `deep_ocean` | 7.83% | **none** |
| `ocean` | 6.53% | **none** |
| `deep_lukewarm_ocean` | 5.87% | **none** |
| `lukewarm_ocean` | 5.17% | **none** |
| `warm_ocean` | 4.83% | **none** |
| `deep_cold_ocean` | 4.73% | **none** |
| `plains` | 3.73% | `villager_fletcher_house:villager_fletcher_house` 1/47, `villager_cleric_house:villager_cleric_house` 1/48, `villager_cartographer_house:villager_cartographer_house` 1/51, `villager_butcher_house:butcher_house` 1/52, `villager_farmer_house:villager_farmer_house` 1/57, `villager_tannery:tannery` 1/57, `small_villager_greenhouse:small_villager_greenhouse` 1/57, `farmers_structures:cooks` 1/64, `farmers_structures:aquacultures_carp` 1/76, `farmers_structures:aquacultures_common` 1/76, `simply_houses:plains_house` 1/101, `simply_houses:plains_mansion` 1/101 |
| `forest` | 3.70% | `sawmillhouse:sawmill` 1/40, `villager_tannery:tannery` 1/53, `small_villager_greenhouse:small_villager_greenhouse` 1/53, `farmers_structures:aquacultures_carp` 1/71, `farmers_structures:aquacultures_common` 1/71, `simply_houses:forest_house` 1/145, `simply_houses:forest_stonemason` 1/145, `simply_houses:forest_chapel` 1/205, `simply_houses:forest_fort` 1/205 |
| `cold_ocean` | 3.60% | **none** |
| `terralith:brushland` | 3.57% | **none** |
| `terralith:yellowstone` | 2.90% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `savanna` | 2.50% | `greek_villager_house:greek_house` 1/53, `farmers_structures:cooks` 1/59, `villager_country_house:country_house` 1/62, `farmers_structures:aquacultures_carp` 1/70, `farmers_structures:aquacultures_common` 1/70 |
| `river` | 2.20% | `farmers_structures:sushis` 1/49 |
| `deep_frozen_ocean` | 1.57% | **none** |
| `terralith:rocky_mountains` | 1.57% | `igloo` 1/33 |
| `taiga` | 1.43% | `hobbit_house_:hobbit_house` 1/52, `small_viking_villager_house:small_viking_house` 1/54, `simply_houses:taiga_inn` 1/104, `simply_houses:taiga_woodcutter` 1/104, `simply_houses:taiga_forge` 1/147, `simply_houses:taiga_house` 1/147 |
| `badlands` | 1.37% | **none** |
| `beach` | 1.30% | `farmers_structures:aquacultures_beach` 1/91 |
| `dark_forest` | 1.30% | `sawmillhouse:sawmill` 1/38, `hobbit_house_:hobbit_house` 1/52, `farmers_structures:aquacultures_carp` 1/67, `farmers_structures:aquacultures_common` 1/67, `simply_houses:forest_house` 1/138, `simply_houses:forest_stonemason` 1/138, `simply_houses:forest_chapel` 1/195, `simply_houses:forest_fort` 1/195 |
| `terralith:highlands` | 1.23% | **none** |
| `terralith:alpine_highlands` | 1.20% | **none** |
| `meadow` | 1.17% | `villager_flower_house:flower_house` 1/51, `simply_houses:plains_house` 1/96, `simply_houses:plains_mansion` 1/96 |
| `frozen_ocean` | 1.03% | **none** |
| `old_growth_pine_taiga` | 1.03% | `hobbit_house_:hobbit_house` 1/52, `small_viking_villager_house:small_viking_house` 1/54, `simply_houses:taiga_inn` 1/104, `simply_houses:taiga_woodcutter` 1/104, `simply_houses:taiga_forge` 1/147, `simply_houses:taiga_house` 1/147 |
| `terralith:volcanic_peaks` | 1.03% | **none** |
| `terralith:hot_shrubland` | 0.90% | **none** |
| `terralith:savanna_badlands` | 0.90% | **none** |
| `terralith:shield` | 0.90% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `snowy_taiga` | 0.87% | `igloo` 1/55, `small_viking_villager_house:small_viking_house` 1/65, `simply_houses:taiga_inn` 1/124, `simply_houses:taiga_woodcutter` 1/124, `simply_houses:taiga_forge` 1/176, `simply_houses:taiga_house` 1/176 |
| `desert` | 0.83% | **none** |
| `savanna_plateau` | 0.83% | `villager_country_house:country_house` 1/47 |
| `jungle` | 0.80% | **none** |
| `wooded_badlands` | 0.80% | **none** |
| `sparse_jungle` | 0.77% | **none** |
| `old_growth_spruce_taiga` | 0.73% | `hobbit_house_:hobbit_house` 1/52, `small_viking_villager_house:small_viking_house` 1/54, `simply_houses:taiga_inn` 1/104, `simply_houses:taiga_woodcutter` 1/104, `simply_houses:taiga_forge` 1/147, `simply_houses:taiga_house` 1/147 |
| `snowy_plains` | 0.73% | `igloo` 1/33 |
| `terralith:jungle_mountains` | 0.70% | `farmers_structures:cooks` 1/35, `farmers_structures:aquacultures_carp` 1/41, `farmers_structures:aquacultures_common` 1/41 |
| `windswept_savanna` | 0.63% | **none** |
| `terralith:gravel_beach` | 0.63% | `farmers_structures:aquacultures_beach` 1/91 |
| `stony_peaks` | 0.60% | **none** |
| `swamp` | 0.60% | `old_swamp_fisherman_house:fisherman_house` 1/46 |
| `frozen_peaks` | 0.57% | **none** |
| `jagged_peaks` | 0.57% | **none** |
| `old_growth_birch_forest` | 0.57% | `sawmillhouse:sawmill` 1/36, `farmers_structures:aquacultures_carp` 1/64, `farmers_structures:aquacultures_common` 1/64, `simply_houses:forest_house` 1/131, `simply_houses:forest_stonemason` 1/131, `simply_houses:forest_chapel` 1/186, `simply_houses:forest_fort` 1/186 |
| `terralith:forested_highlands` | 0.57% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `terralith:arid_highlands` | 0.53% | **none** |
| `terralith:cold_shrubland` | 0.53% | `igloo` 1/33 |
| `terralith:temperate_highlands` | 0.53% | **none** |
| `mangrove_swamp` | 0.47% | `old_swamp_fisherman_house:fisherman_house` 1/46 |
| `terralith:bryce_canyon` | 0.47% | **none** |
| `terralith:orchid_swamp` | 0.43% | **none** |
| `birch_forest` | 0.40% | `sawmillhouse:sawmill` 1/38, `small_villager_greenhouse:small_villager_greenhouse` 1/51, `farmers_structures:aquacultures_carp` 1/67, `farmers_structures:aquacultures_common` 1/67, `simply_houses:forest_house` 1/138, `simply_houses:forest_stonemason` 1/138, `simply_houses:forest_chapel` 1/196, `simply_houses:forest_fort` 1/196 |
| `terralith:birch_taiga` | 0.40% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `terralith:rocky_jungle` | 0.40% | **none** |
| `terralith:siberian_taiga` | 0.40% | **none** |
| `terralith:snowy_badlands` | 0.40% | `igloo` 1/33 |
| `eroded_badlands` | 0.37% | **none** |
| `terralith:ice_marsh` | 0.37% | `igloo` 1/33 |
| `snowy_slopes` | 0.33% | `igloo` 1/33 |
| `terralith:fractured_savanna` | 0.33% | `farmers_structures:cooks` 1/35, `farmers_structures:aquacultures_carp` 1/41, `farmers_structures:aquacultures_common` 1/41 |
| `terralith:shrubland` | 0.33% | **none** |
| `frozen_river` | 0.30% | `farmers_structures:sushis` 1/49 |
| `terralith:blooming_plateau` | 0.30% | **none** |
| `terralith:glacial_chasm` | 0.30% | **none** |
| `terralith:steppe` | 0.30% | **none** |
| `snowy_beach` | 0.27% | `farmers_structures:aquacultures_beach` 1/91 |
| `terralith:sakura_grove` | 0.27% | **none** |
| `terralith:stony_spires` | 0.27% | **none** |
| `terralith:tropical_jungle` | 0.27% | **none** |
| `grove` | 0.23% | `farmers_structures:aquacultures_carp` 1/55, `farmers_structures:aquacultures_common` 1/55, `simply_houses:forest_house` 1/114, `simply_houses:forest_stonemason` 1/114, `simply_houses:forest_chapel` 1/161, `simply_houses:forest_fort` 1/161 |
| `windswept_forest` | 0.23% | `sawmillhouse:sawmill` 1/29, `farmers_structures:cooks` 1/44, `farmers_structures:aquacultures_carp` 1/52, `farmers_structures:aquacultures_common` 1/52 |
| `terralith:blooming_valley` | 0.23% | **none** |
| `terralith:painted_mountains` | 0.23% | **none** |
| `terralith:moonlight_valley` | 0.20% | **none** |
| `flower_forest` | 0.17% | `sawmillhouse:sawmill` 1/36, `farmers_structures:aquacultures_carp` 1/64, `farmers_structures:aquacultures_common` 1/64, `simply_houses:forest_house` 1/131, `simply_houses:forest_stonemason` 1/131, `simply_houses:forest_chapel` 1/186, `simply_houses:forest_fort` 1/186 |
| `mushroom_fields` | 0.17% | **none** |
| `terralith:moonlight_grove` | 0.17% | **none** |
| `terralith:warped_mesa` | 0.17% | **none** |
| `terralith:windswept_spires` | 0.17% | **none** |
| `terralith:wintry_forest` | 0.17% | `igloo` 1/53, `simply_houses:taiga_inn` 1/121, `simply_houses:taiga_woodcutter` 1/121, `simply_houses:taiga_forge` 1/170, `simply_houses:taiga_house` 1/170 |
| `bamboo_jungle` | 0.13% | **none** |
| `terralith:haze_mountain` | 0.13% | `farmers_structures:cooks` 1/35, `farmers_structures:aquacultures_carp` 1/41, `farmers_structures:aquacultures_common` 1/41 |
| `ice_spikes` | 0.10% | **none** |
| `terralith:alpine_grove` | 0.10% | `igloo` 1/50, `farmers_structures:aquacultures_carp` 1/74, `farmers_structures:aquacultures_common` 1/74, `simply_houses:forest_house` 1/153, `simply_houses:forest_stonemason` 1/153, `simply_houses:forest_chapel` 1/216, `simply_houses:forest_fort` 1/216 |
| `terralith:gravel_desert` | 0.10% | `igloo` 1/33 |
| `terralith:lavender_valley` | 0.10% | **none** |
| `terralith:lush_valley` | 0.10% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `terralith:skylands_autumn` | 0.10% | **none** |
| `terralith:white_mesa` | 0.10% | **none** |
| `sunflower_plains` | 0.07% | `villager_fletcher_house:villager_fletcher_house` 1/62, `villager_cleric_house:villager_cleric_house` 1/63, `villager_cartographer_house:villager_cartographer_house` 1/68, `villager_butcher_house:butcher_house` 1/69, `villager_flower_house:flower_house` 1/72, `villager_farmer_house:villager_farmer_house` 1/75, `small_villager_greenhouse:small_villager_greenhouse` 1/76, `farmers_structures:cooks` 1/85, `farmers_structures:aquacultures_carp` 1/101, `farmers_structures:aquacultures_common` 1/101, `simply_houses:plains_house` 1/135, `simply_houses:plains_mansion` 1/135 |
| `terralith:desert_canyon` | 0.07% | **none** |
| `terralith:sandstone_valley` | 0.07% | **none** |
| `terralith:siberian_grove` | 0.07% | `igloo` 1/33 |
| `terralith:snowy_maple_forest` | 0.07% | `igloo` 1/33 |
| `terralith:warm_river` | 0.07% | **none** |
| `terralith:yosemite_lowlands` | 0.07% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `windswept_gravelly_hills` | 0.03% | `farmers_structures:cooks` 1/35, `farmers_structures:aquacultures_carp` 1/41, `farmers_structures:aquacultures_common` 1/41 |
| `windswept_hills` | 0.03% | `farmers_structures:cooks` 1/35, `farmers_structures:aquacultures_carp` 1/41, `farmers_structures:aquacultures_common` 1/41 |
| `terralith:amethyst_rainforest` | 0.03% | **none** |
| `terralith:ancient_sands` | 0.03% | **none** |
| `terralith:desert_oasis` | 0.03% | **none** |
| `terralith:emerald_peaks` | 0.03% | `igloo` 1/33 |
| `terralith:lavender_forest` | 0.03% | **none** |
| `terralith:lush_desert` | 0.03% | **none** |
| `terralith:mirage_isles` | 0.03% | **none** |
| `terralith:red_oasis` | 0.03% | **none** |
| `terralith:savanna_slopes` | 0.03% | **none** |
| `terralith:skylands_spring` | 0.03% | **none** |
| `terralith:skylands_summer` | 0.03% | **none** |
| `terralith:skylands_winter` | 0.03% | **none** |
| `terralith:volcanic_crater` | 0.03% | **none** |
| `terralith:yosemite_cliffs` | 0.03% | **none** |
| `deep_dark` | 0.00% | **none** |
| `stony_shore` | 0.00% | **none** |
| `terralith:alpha_islands` | 0.00% | **none** |
| `terralith:alpha_islands_winter` | 0.00% | `igloo` 1/33 |
| `terralith:amethyst_canyon` | 0.00% | **none** |
| `terralith:ashen_savanna` | 0.00% | **none** |
| `terralith:basalt_cliffs` | 0.00% | **none** |
| `terralith:caldera` | 0.00% | **none** |
| `terralith:cave/frostfire_caves` | 0.00% | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** |
| `terralith:cloud_forest` | 0.00% | `farmers_structures:cooks` 1/55, `farmers_structures:aquacultures_carp` 1/65, `farmers_structures:aquacultures_common` 1/65, `simply_houses:forest_house` 1/134, `simply_houses:forest_stonemason` 1/134, `simply_houses:forest_chapel` 1/189, `simply_houses:forest_fort` 1/189 |
| `terralith:desert_spires` | 0.00% | **none** |
| `terralith:frozen_cliffs` | 0.00% | **none** |
| `terralith:granite_cliffs` | 0.00% | **none** |
| `terralith:rocky_shrubland` | 0.00% | `igloo` 1/33 |
| `terralith:sakura_valley` | 0.00% | **none** |
| `terralith:shield_clearing` | 0.00% | **none** |
| `terralith:snowy_shield` | 0.00% | `igloo` 1/33 |
| `terralith:valley_clearing` | 0.00% | **none** |
| `terralith:white_cliffs` | 0.00% | **none** |
| `terralith:wintry_lowlands` | 0.00% | `igloo` 1/33 |
| `nether_wastes` | 50.17% | `farmers_structures:cooks_nether_2` 1/47 |
| `crimson_forest` | 15.30% | `farmers_structures:cooks_nether_1` 1/38 |
| `warped_forest` | 5.43% | `farmers_structures:cooks_nether_1` 1/38 |

## 8. Pillager by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Pillager |
|---|---|---|
| `deep_ocean` | 7.83% | `towns_and_towers:pillager_outpost_ocean` 1/84 |
| `ocean` | 6.53% | **none** |
| `deep_lukewarm_ocean` | 5.87% | `towns_and_towers:pillager_outpost_ocean` 1/84 |
| `lukewarm_ocean` | 5.17% | **none** |
| `warm_ocean` | 4.83% | **none** |
| `deep_cold_ocean` | 4.73% | `towns_and_towers:pillager_outpost_ocean` 1/84 |
| `plains` | 3.73% | `pillager_outpost` 1/41 |
| `forest` | 3.70% | `towns_and_towers:pillager_outpost_forest` 1/60 |
| `cold_ocean` | 3.60% | **none** |
| `terralith:brushland` | 3.57% | `towns_and_towers:pillager_outpost_savanna` 1/60, `towns_and_towers:exclusives/pillager_outpost_mediterranean` 1/67 |
| `terralith:yellowstone` | 2.90% | `pillager_outpost` 1/72, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/73, `towns_and_towers:pillager_outpost_taiga` 1/73 |
| `savanna` | 2.50% | `pillager_outpost` 1/51, `towns_and_towers:pillager_outpost_savanna` 1/59 |
| `river` | 2.20% | **none** |
| `deep_frozen_ocean` | 1.57% | **none** |
| `terralith:rocky_mountains` | 1.57% | `pillager_outpost` 1/56, `towns_and_towers:pillager_outpost_snowy_slopes` 1/60 |
| `taiga` | 1.43% | `pillager_outpost` 1/52, `towns_and_towers:pillager_outpost_taiga` 1/53 |
| `badlands` | 1.37% | `towns_and_towers:pillager_outpost_badlands` 1/55 |
| `beach` | 1.30% | `towns_and_towers:pillager_outpost_beach` 1/49 |
| `dark_forest` | 1.30% | `mansion` 1/46 |
| `terralith:highlands` | 1.23% | `towns_and_towers:exclusives/pillager_outpost_tudor` 1/60 |
| `terralith:alpine_highlands` | 1.20% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/60, `towns_and_towers:pillager_outpost_taiga` 1/60 |
| `meadow` | 1.17% | `towns_and_towers:pillager_outpost_meadow` 1/63, `pillager_outpost` 1/72 |
| `frozen_ocean` | 1.03% | **none** |
| `old_growth_pine_taiga` | 1.03% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/50 |
| `terralith:volcanic_peaks` | 1.03% | `pillager_outpost` 1/41 |
| `terralith:hot_shrubland` | 0.90% | `towns_and_towers:pillager_outpost_savanna` 1/60, `towns_and_towers:exclusives/pillager_outpost_mediterranean` 1/67 |
| `terralith:savanna_badlands` | 0.90% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/58, `towns_and_towers:pillager_outpost_savanna` 1/79, `towns_and_towers:pillager_outpost_badlands` 1/86 |
| `terralith:shield` | 0.90% | `pillager_outpost` 1/90, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/91, `towns_and_towers:pillager_outpost_taiga` 1/92, `towns_and_towers:pillager_outpost_snowy_taiga` 1/111, `towns_and_towers:exclusives/pillager_outpost_swedish` 1/111 |
| `snowy_taiga` | 0.87% | `towns_and_towers:pillager_outpost_snowy_taiga` 1/58 |
| `desert` | 0.83% | `towns_and_towers:pillager_outpost_desert` 1/70, `pillager_outpost` 1/73 |
| `savanna_plateau` | 0.83% | **none** |
| `jungle` | 0.80% | `towns_and_towers:pillager_outpost_jungle` 1/54 |
| `wooded_badlands` | 0.80% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/37 |
| `sparse_jungle` | 0.77% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/39 |
| `old_growth_spruce_taiga` | 0.73% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/50 |
| `snowy_plains` | 0.73% | `towns_and_towers:pillager_outpost_snowy_plains` 1/62, `pillager_outpost` 1/72 |
| `terralith:jungle_mountains` | 0.70% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/55, `towns_and_towers:pillager_outpost_jungle` 1/76 |
| `windswept_savanna` | 0.63% | **none** |
| `terralith:gravel_beach` | 0.63% | `towns_and_towers:pillager_outpost_beach` 1/49 |
| `stony_peaks` | 0.60% | `pillager_outpost` 1/41 |
| `swamp` | 0.60% | `betterwitchhuts:witch_hut` 1/21, `towns_and_towers:pillager_outpost_swamp` 1/60 |
| `frozen_peaks` | 0.57% | `pillager_outpost` 1/41 |
| `jagged_peaks` | 0.57% | `pillager_outpost` 1/41 |
| `old_growth_birch_forest` | 0.57% | `towns_and_towers:pillager_outpost_birch_forest` 1/60 |
| `terralith:forested_highlands` | 0.57% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/81, `towns_and_towers:pillager_outpost_taiga` 1/82, `towns_and_towers:pillager_outpost_snowy_taiga` 1/99, `towns_and_towers:exclusives/pillager_outpost_swedish` 1/99 |
| `terralith:arid_highlands` | 0.53% | `towns_and_towers:pillager_outpost_savanna` 1/61, `towns_and_towers:pillager_outpost_badlands` 1/67 |
| `terralith:cold_shrubland` | 0.53% | **none** |
| `terralith:temperate_highlands` | 0.53% | `towns_and_towers:pillager_outpost_forest` 1/85, `towns_and_towers:pillager_outpost_birch_forest` 1/85 |
| `mangrove_swamp` | 0.47% | `betterwitchhuts:witch_hut` 1/18 |
| `terralith:bryce_canyon` | 0.47% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/52, `towns_and_towers:pillager_outpost_badlands` 1/77 |
| `terralith:orchid_swamp` | 0.43% | `betterwitchhuts:witch_hut` 1/21, `towns_and_towers:pillager_outpost_swamp` 1/60 |
| `birch_forest` | 0.40% | `towns_and_towers:pillager_outpost_birch_forest` 1/60 |
| `terralith:birch_taiga` | 0.40% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/82, `towns_and_towers:pillager_outpost_taiga` 1/82, `towns_and_towers:pillager_outpost_birch_forest` 1/88 |
| `terralith:rocky_jungle` | 0.40% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/55, `towns_and_towers:pillager_outpost_jungle` 1/76 |
| `terralith:siberian_taiga` | 0.40% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/60, `towns_and_towers:pillager_outpost_taiga` 1/60 |
| `terralith:snowy_badlands` | 0.40% | `towns_and_towers:pillager_outpost_badlands` 1/55 |
| `eroded_badlands` | 0.37% | **none** |
| `terralith:ice_marsh` | 0.37% | `betterwitchhuts:witch_hut` 1/21, `towns_and_towers:pillager_outpost_swamp` 1/60 |
| `snowy_slopes` | 0.33% | `pillager_outpost` 1/56, `towns_and_towers:pillager_outpost_snowy_slopes` 1/60 |
| `terralith:fractured_savanna` | 0.33% | `towns_and_towers:pillager_outpost_savanna` 1/36 |
| `terralith:shrubland` | 0.33% | `towns_and_towers:pillager_outpost_savanna` 1/60, `towns_and_towers:exclusives/pillager_outpost_mediterranean` 1/67 |
| `frozen_river` | 0.30% | **none** |
| `terralith:blooming_plateau` | 0.30% | `towns_and_towers:pillager_outpost_meadow` 1/52 |
| `terralith:glacial_chasm` | 0.30% | **none** |
| `terralith:steppe` | 0.30% | `towns_and_towers:pillager_outpost_sunflower_plains` 1/60 |
| `snowy_beach` | 0.27% | `towns_and_towers:pillager_outpost_snowy_beach` 1/49 |
| `terralith:sakura_grove` | 0.27% | `mansion` 1/46, `towns_and_towers:pillager_outpost_flower_forest` 1/66, `pillager_outpost` 1/86 |
| `terralith:stony_spires` | 0.27% | **none** |
| `terralith:tropical_jungle` | 0.27% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/55, `towns_and_towers:pillager_outpost_jungle` 1/76 |
| `grove` | 0.23% | `towns_and_towers:pillager_outpost_grove` 1/73, `pillager_outpost` 1/75 |
| `windswept_forest` | 0.23% | **none** |
| `terralith:blooming_valley` | 0.23% | **none** |
| `terralith:painted_mountains` | 0.23% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/52, `towns_and_towers:pillager_outpost_badlands` 1/77 |
| `terralith:moonlight_valley` | 0.20% | `towns_and_towers:pillager_outpost_flower_forest` 1/58 |
| `flower_forest` | 0.17% | `towns_and_towers:pillager_outpost_flower_forest` 1/58 |
| `mushroom_fields` | 0.17% | `towns_and_towers:pillager_outpost_mushroom_fields` 1/60 |
| `terralith:moonlight_grove` | 0.17% | `towns_and_towers:pillager_outpost_flower_forest` 1/58 |
| `terralith:warped_mesa` | 0.17% | `towns_and_towers:exclusives/pillager_outpost_oriental` 1/61, `towns_and_towers:pillager_outpost_wooded_badlands` 1/61, `towns_and_towers:pillager_outpost_badlands` 1/91 |
| `terralith:windswept_spires` | 0.17% | **none** |
| `terralith:wintry_forest` | 0.17% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/77, `towns_and_towers:pillager_outpost_taiga` 1/77, `towns_and_towers:pillager_outpost_snowy_taiga` 1/93 |
| `bamboo_jungle` | 0.13% | **none** |
| `terralith:haze_mountain` | 0.13% | **none** |
| `ice_spikes` | 0.10% | **none** |
| `terralith:alpine_grove` | 0.10% | `towns_and_towers:pillager_outpost_grove` 1/61 |
| `terralith:gravel_desert` | 0.10% | `towns_and_towers:pillager_outpost_snowy_plains` 1/51 |
| `terralith:lavender_valley` | 0.10% | `towns_and_towers:pillager_outpost_flower_forest` 1/58 |
| `terralith:lush_valley` | 0.10% | `pillager_outpost` 1/72, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/73, `towns_and_towers:pillager_outpost_taiga` 1/73 |
| `terralith:skylands_autumn` | 0.10% | **none** |
| `terralith:white_mesa` | 0.10% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/52, `towns_and_towers:pillager_outpost_badlands` 1/77 |
| `sunflower_plains` | 0.07% | `towns_and_towers:pillager_outpost_sunflower_plains` 1/60 |
| `terralith:desert_canyon` | 0.07% | `towns_and_towers:pillager_outpost_desert` 1/58 |
| `terralith:sandstone_valley` | 0.07% | `towns_and_towers:pillager_outpost_desert` 1/58 |
| `terralith:siberian_grove` | 0.07% | `towns_and_towers:pillager_outpost_grove` 1/84, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/87, `towns_and_towers:pillager_outpost_taiga` 1/88 |
| `terralith:snowy_maple_forest` | 0.07% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/81, `towns_and_towers:pillager_outpost_taiga` 1/82, `towns_and_towers:pillager_outpost_snowy_taiga` 1/99, `towns_and_towers:exclusives/pillager_outpost_swedish` 1/99 |
| `terralith:warm_river` | 0.07% | **none** |
| `terralith:yosemite_lowlands` | 0.07% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/60, `towns_and_towers:pillager_outpost_taiga` 1/60 |
| `windswept_gravelly_hills` | 0.03% | **none** |
| `windswept_hills` | 0.03% | **none** |
| `terralith:amethyst_rainforest` | 0.03% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/55, `towns_and_towers:pillager_outpost_jungle` 1/76 |
| `terralith:ancient_sands` | 0.03% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/52, `towns_and_towers:pillager_outpost_badlands` 1/77 |
| `terralith:desert_oasis` | 0.03% | `towns_and_towers:exclusives/pillager_outpost_oriental` 1/60, `towns_and_towers:pillager_outpost_desert` 1/68 |
| `terralith:emerald_peaks` | 0.03% | `pillager_outpost` 1/41 |
| `terralith:lavender_forest` | 0.03% | `towns_and_towers:pillager_outpost_flower_forest` 1/66, `pillager_outpost` 1/86 |
| `terralith:lush_desert` | 0.03% | `towns_and_towers:pillager_outpost_desert` 1/58 |
| `terralith:mirage_isles` | 0.03% | `towns_and_towers:pillager_outpost_mushroom_fields` 1/60 |
| `terralith:red_oasis` | 0.03% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/52, `towns_and_towers:pillager_outpost_badlands` 1/77 |
| `terralith:savanna_slopes` | 0.03% | `towns_and_towers:pillager_outpost_savanna` 1/36 |
| `terralith:skylands_spring` | 0.03% | **none** |
| `terralith:skylands_summer` | 0.03% | **none** |
| `terralith:skylands_winter` | 0.03% | **none** |
| `terralith:volcanic_crater` | 0.03% | **none** |
| `terralith:yosemite_cliffs` | 0.03% | **none** |
| `deep_dark` | 0.00% | **none** |
| `stony_shore` | 0.00% | **none** |
| `terralith:alpha_islands` | 0.00% | `towns_and_towers:exclusives/pillager_outpost_classic` 1/67 |
| `terralith:alpha_islands_winter` | 0.00% | **none** |
| `terralith:amethyst_canyon` | 0.00% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/55, `towns_and_towers:pillager_outpost_jungle` 1/76 |
| `terralith:ashen_savanna` | 0.00% | `towns_and_towers:pillager_outpost_savanna` 1/36 |
| `terralith:basalt_cliffs` | 0.00% | **none** |
| `terralith:caldera` | 0.00% | `pillager_outpost` 1/41 |
| `terralith:cave/frostfire_caves` | 0.00% | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** |
| `terralith:cloud_forest` | 0.00% | `towns_and_towers:pillager_outpost_flower_forest` 1/73, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/97, `towns_and_towers:pillager_outpost_taiga` 1/98 |
| `terralith:desert_spires` | 0.00% | `towns_and_towers:pillager_outpost_desert` 1/58 |
| `terralith:frozen_cliffs` | 0.00% | `towns_and_towers:pillager_outpost_beach` 1/49 |
| `terralith:granite_cliffs` | 0.00% | **none** |
| `terralith:rocky_shrubland` | 0.00% | `towns_and_towers:pillager_outpost_snowy_plains` 1/61, `towns_and_towers:pillager_outpost_snowy_slopes` 1/76 |
| `terralith:sakura_valley` | 0.00% | `mansion` 1/46, `towns_and_towers:pillager_outpost_flower_forest` 1/66, `pillager_outpost` 1/86 |
| `terralith:shield_clearing` | 0.00% | `towns_and_towers:pillager_outpost_taiga` 1/58, `towns_and_towers:pillager_outpost_snowy_taiga` 1/70 |
| `terralith:snowy_shield` | 0.00% | `towns_and_towers:pillager_outpost_snowy_taiga` 1/58 |
| `terralith:valley_clearing` | 0.00% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/60, `towns_and_towers:pillager_outpost_taiga` 1/60 |
| `terralith:white_cliffs` | 0.00% | `towns_and_towers:pillager_outpost_birch_forest` 1/60 |
| `terralith:wintry_lowlands` | 0.00% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/77, `towns_and_towers:pillager_outpost_taiga` 1/77, `towns_and_towers:pillager_outpost_snowy_taiga` 1/93 |
| `lush_caves` | 12.16% | `terralith:underground/witch_hut` 1/7.8 |
| `terralith:cave/fungal_caves` | 1.31% | `terralith:underground/witch_hut` 1/7.9 |
| `terralith:cave/underground_jungle` | 0.74% | `terralith:underground/witch_hut` 1/7.2 |
| `terralith:scarlet_mountains` | 0.04% | `pillager_outpost` 1/72, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/73, `towns_and_towers:pillager_outpost_taiga` 1/73 |

## 9. Surface dungeons by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Surface dungeons |
|---|---|---|
| `deep_ocean` | 7.83% | **none** |
| `ocean` | 6.53% | **none** |
| `deep_lukewarm_ocean` | 5.87% | **none** |
| `lukewarm_ocean` | 5.17% | **none** |
| `warm_ocean` | 4.83% | **none** |
| `deep_cold_ocean` | 4.73% | **none** |
| `plains` | 3.73% | **none** |
| `forest` | 3.70% | **none** |
| `cold_ocean` | 3.60% | **none** |
| `terralith:brushland` | 3.57% | **none** |
| `terralith:yellowstone` | 2.90% | **none** |
| `savanna` | 2.50% | **none** |
| `river` | 2.20% | **none** |
| `deep_frozen_ocean` | 1.57% | **none** |
| `terralith:rocky_mountains` | 1.57% | **none** |
| `taiga` | 1.43% | **none** |
| `badlands` | 1.37% | `philipsruins:badlands_start_dungeon` 1/25 |
| `beach` | 1.30% | **none** |
| `dark_forest` | 1.30% | **none** |
| `terralith:highlands` | 1.23% | **none** |
| `terralith:alpine_highlands` | 1.20% | **none** |
| `meadow` | 1.17% | **none** |
| `frozen_ocean` | 1.03% | **none** |
| `old_growth_pine_taiga` | 1.03% | **none** |
| `terralith:volcanic_peaks` | 1.03% | **none** |
| `terralith:hot_shrubland` | 0.90% | **none** |
| `terralith:savanna_badlands` | 0.90% | **none** |
| `terralith:shield` | 0.90% | **none** |
| `snowy_taiga` | 0.87% | **none** |
| `desert` | 0.83% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `savanna_plateau` | 0.83% | **none** |
| `jungle` | 0.80% | `betterjungletemples:jungle_temple` 1/18 |
| `wooded_badlands` | 0.80% | `philipsruins:badlands_start_dungeon` 1/25 |
| `sparse_jungle` | 0.77% | **none** |
| `old_growth_spruce_taiga` | 0.73% | **none** |
| `snowy_plains` | 0.73% | **none** |
| `terralith:jungle_mountains` | 0.70% | `betterjungletemples:jungle_temple` 1/18 |
| `windswept_savanna` | 0.63% | **none** |
| `terralith:gravel_beach` | 0.63% | **none** |
| `stony_peaks` | 0.60% | **none** |
| `swamp` | 0.60% | `betterwitchhuts:witch_circle` 1/31 |
| `frozen_peaks` | 0.57% | **none** |
| `jagged_peaks` | 0.57% | **none** |
| `old_growth_birch_forest` | 0.57% | **none** |
| `terralith:forested_highlands` | 0.57% | **none** |
| `terralith:arid_highlands` | 0.53% | **none** |
| `terralith:cold_shrubland` | 0.53% | **none** |
| `terralith:temperate_highlands` | 0.53% | **none** |
| `mangrove_swamp` | 0.47% | **none** |
| `terralith:bryce_canyon` | 0.47% | **none** |
| `terralith:orchid_swamp` | 0.43% | `betterwitchhuts:witch_circle` 1/31 |
| `birch_forest` | 0.40% | **none** |
| `terralith:birch_taiga` | 0.40% | **none** |
| `terralith:rocky_jungle` | 0.40% | `betterjungletemples:jungle_temple` 1/18 |
| `terralith:siberian_taiga` | 0.40% | **none** |
| `terralith:snowy_badlands` | 0.40% | **none** |
| `eroded_badlands` | 0.37% | `philipsruins:badlands_start_dungeon` 1/25 |
| `terralith:ice_marsh` | 0.37% | `betterwitchhuts:witch_circle` 1/31 |
| `snowy_slopes` | 0.33% | **none** |
| `terralith:fractured_savanna` | 0.33% | **none** |
| `terralith:shrubland` | 0.33% | **none** |
| `frozen_river` | 0.30% | `terralith:spire` 1/25 |
| `terralith:blooming_plateau` | 0.30% | **none** |
| `terralith:glacial_chasm` | 0.30% | **none** |
| `terralith:steppe` | 0.30% | **none** |
| `snowy_beach` | 0.27% | **none** |
| `terralith:sakura_grove` | 0.27% | **none** |
| `terralith:stony_spires` | 0.27% | **none** |
| `terralith:tropical_jungle` | 0.27% | `betterjungletemples:jungle_temple` 1/18 |
| `grove` | 0.23% | **none** |
| `windswept_forest` | 0.23% | **none** |
| `terralith:blooming_valley` | 0.23% | **none** |
| `terralith:painted_mountains` | 0.23% | **none** |
| `terralith:moonlight_valley` | 0.20% | **none** |
| `flower_forest` | 0.17% | **none** |
| `mushroom_fields` | 0.17% | **none** |
| `terralith:moonlight_grove` | 0.17% | **none** |
| `terralith:warped_mesa` | 0.17% | **none** |
| `terralith:windswept_spires` | 0.17% | **none** |
| `terralith:wintry_forest` | 0.17% | **none** |
| `bamboo_jungle` | 0.13% | `betterjungletemples:jungle_temple` 1/18 |
| `terralith:haze_mountain` | 0.13% | **none** |
| `ice_spikes` | 0.10% | `terralith:spire` 1/25 |
| `terralith:alpine_grove` | 0.10% | **none** |
| `terralith:gravel_desert` | 0.10% | **none** |
| `terralith:lavender_valley` | 0.10% | **none** |
| `terralith:lush_valley` | 0.10% | **none** |
| `terralith:skylands_autumn` | 0.10% | **none** |
| `terralith:white_mesa` | 0.10% | **none** |
| `sunflower_plains` | 0.07% | **none** |
| `terralith:desert_canyon` | 0.07% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:sandstone_valley` | 0.07% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:siberian_grove` | 0.07% | **none** |
| `terralith:snowy_maple_forest` | 0.07% | **none** |
| `terralith:warm_river` | 0.07% | **none** |
| `terralith:yosemite_lowlands` | 0.07% | **none** |
| `windswept_gravelly_hills` | 0.03% | **none** |
| `windswept_hills` | 0.03% | **none** |
| `terralith:amethyst_rainforest` | 0.03% | `betterjungletemples:jungle_temple` 1/18 |
| `terralith:ancient_sands` | 0.03% | **none** |
| `terralith:desert_oasis` | 0.03% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:emerald_peaks` | 0.03% | **none** |
| `terralith:lavender_forest` | 0.03% | **none** |
| `terralith:lush_desert` | 0.03% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:mirage_isles` | 0.03% | **none** |
| `terralith:red_oasis` | 0.03% | **none** |
| `terralith:savanna_slopes` | 0.03% | **none** |
| `terralith:skylands_spring` | 0.03% | **none** |
| `terralith:skylands_summer` | 0.03% | **none** |
| `terralith:skylands_winter` | 0.03% | **none** |
| `terralith:volcanic_crater` | 0.03% | **none** |
| `terralith:yosemite_cliffs` | 0.03% | **none** |
| `deep_dark` | 0.00% | **none** |
| `stony_shore` | 0.00% | **none** |
| `terralith:alpha_islands` | 0.00% | **none** |
| `terralith:alpha_islands_winter` | 0.00% | **none** |
| `terralith:amethyst_canyon` | 0.00% | `betterjungletemples:jungle_temple` 1/18 |
| `terralith:ashen_savanna` | 0.00% | **none** |
| `terralith:basalt_cliffs` | 0.00% | **none** |
| `terralith:caldera` | 0.00% | **none** |
| `terralith:cave/frostfire_caves` | 0.00% | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** |
| `terralith:cloud_forest` | 0.00% | **none** |
| `terralith:desert_spires` | 0.00% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:frozen_cliffs` | 0.00% | **none** |
| `terralith:granite_cliffs` | 0.00% | **none** |
| `terralith:rocky_shrubland` | 0.00% | **none** |
| `terralith:sakura_valley` | 0.00% | **none** |
| `terralith:shield_clearing` | 0.00% | **none** |
| `terralith:snowy_shield` | 0.00% | **none** |
| `terralith:valley_clearing` | 0.00% | **none** |
| `terralith:white_cliffs` | 0.00% | **none** |
| `terralith:wintry_lowlands` | 0.00% | `terralith:spire` 1/25 |

## 10. Ruins by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Ruins |
|---|---|---|
| `deep_ocean` | 7.83% | **none** |
| `ocean` | 6.53% | **none** |
| `deep_lukewarm_ocean` | 5.87% | **none** |
| `lukewarm_ocean` | 5.17% | **none** |
| `warm_ocean` | 4.83% | **none** |
| `deep_cold_ocean` | 4.73% | **none** |
| `plains` | 3.73% | `philipsruins:ancient_towers` 1/18, `philipsruins:ancient_ruins` 1/23, `philipsruins:field_stone_ruins_rocks` 1/26, `philipsruins:field_stone_ruins` 1/31, `philipsruins:level_one_ruins` 1/31, `philipsruins:level_three_ruins` 1/35, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/47, `philipsruins:level_two_ruins_pool` 1/47, `philipsruins:start_nether_ruin` 1/129 |
| `forest` | 3.70% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `cold_ocean` | 3.60% | **none** |
| `terralith:brushland` | 3.57% | **none** |
| `terralith:yellowstone` | 2.90% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `savanna` | 2.50% | `philipsruins:ancient_towers` 1/16, `philipsruins:ancient_ruins` 1/20, `philipsruins:field_stone_ruins_rocks` 1/23, `philipsruins:lost_soul_city` 1/38 |
| `river` | 2.20% | **none** |
| `deep_frozen_ocean` | 1.57% | **none** |
| `terralith:rocky_mountains` | 1.57% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `taiga` | 1.43% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `badlands` | 1.37% | `philipsruins:badlands_structures` 1/16 |
| `beach` | 1.30% | **none** |
| `dark_forest` | 1.30% | `philipsruins:ancient_towers` 1/19, `philipsruins:pumpkin_ruins` 1/24, `philipsruins:ancient_ruins` 1/25, `philipsruins:field_stone_ruins_rocks` 1/28, `philipsruins:field_stone_ruins` 1/33, `philipsruins:level_one_ruins` 1/33, `philipsruins:level_three_ruins` 1/38, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/50, `philipsruins:level_two_ruins_pool` 1/50, `philipsruins:start_nether_ruin` 1/137 |
| `terralith:highlands` | 1.23% | **none** |
| `terralith:alpine_highlands` | 1.20% | **none** |
| `meadow` | 1.17% | `philipsruins:ancient_towers` 1/18, `philipsruins:ancient_ruins` 1/23, `philipsruins:field_stone_ruins_rocks` 1/26, `philipsruins:field_stone_ruins` 1/31, `philipsruins:level_one_ruins` 1/31, `philipsruins:level_three_ruins` 1/35, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/47, `philipsruins:level_two_ruins_pool` 1/47, `philipsruins:start_nether_ruin` 1/129 |
| `frozen_ocean` | 1.03% | **none** |
| `old_growth_pine_taiga` | 1.03% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:volcanic_peaks` | 1.03% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:hot_shrubland` | 0.90% | **none** |
| `terralith:savanna_badlands` | 0.90% | **none** |
| `terralith:shield` | 0.90% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `snowy_taiga` | 0.87% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `desert` | 0.83% | `philipsruins:desert_structures` 1/16 |
| `savanna_plateau` | 0.83% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `jungle` | 0.80% | `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:field_stone_ruins` 1/30, `philipsruins:level_one_ruins` 1/30, `philipsruins:level_three_ruins` 1/34, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/45, `philipsruins:level_two_ruins_pool` 1/45, `philipsruins:start_nether_ruin` 1/123, `philipsruins:rare_ruin` 1/138 |
| `wooded_badlands` | 0.80% | `philipsruins:badlands_structures` 1/16 |
| `sparse_jungle` | 0.77% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `old_growth_spruce_taiga` | 0.73% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `snowy_plains` | 0.73% | `philipsruins:ancient_towers` 1/16, `philipsruins:ancient_ruins` 1/20, `philipsruins:field_stone_ruins_rocks` 1/23, `philipsruins:lost_soul_city` 1/38 |
| `terralith:jungle_mountains` | 0.70% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `windswept_savanna` | 0.63% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:gravel_beach` | 0.63% | **none** |
| `stony_peaks` | 0.60% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `swamp` | 0.60% | `philipsruins:ancient_towers` 1/19, `philipsruins:pumpkin_ruins` 1/24, `philipsruins:ancient_ruins` 1/25, `philipsruins:field_stone_ruins_rocks` 1/28, `philipsruins:field_stone_ruins` 1/33, `philipsruins:level_one_ruins` 1/33, `philipsruins:level_three_ruins` 1/38, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/50, `philipsruins:level_two_ruins_pool` 1/50, `philipsruins:start_nether_ruin` 1/137 |
| `frozen_peaks` | 0.57% | `philipsruins:ancient_towers` 1/15, `philipsruins:ancient_ruins` 1/20, `philipsruins:field_stone_ruins` 1/26, `philipsruins:level_one_ruins` 1/26, `philipsruins:level_three_ruins` 1/30, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/40, `philipsruins:level_two_ruins_pool` 1/40, `philipsruins:start_nether_ruin` 1/109 |
| `jagged_peaks` | 0.57% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `old_growth_birch_forest` | 0.57% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:forested_highlands` | 0.57% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:arid_highlands` | 0.53% | **none** |
| `terralith:cold_shrubland` | 0.53% | **none** |
| `terralith:temperate_highlands` | 0.53% | **none** |
| `mangrove_swamp` | 0.47% | `philipsruins:ancient_towers` 1/22, `philipsruins:pumpkin_ruins` 1/28, `philipsruins:ancient_ruins` 1/29, `philipsruins:field_stone_ruins_rocks` 1/32, `philipsruins:lost_soul_city` 1/38, `philipsruins:field_stone_ruins` 1/38, `philipsruins:level_one_ruins` 1/38, `philipsruins:level_three_ruins` 1/43, `philipsruins:level_two_ruins` 1/58, `philipsruins:level_two_ruins_pool` 1/58, `philipsruins:start_nether_ruin` 1/158, `philipsruins:rare_ruin` 1/177 |
| `terralith:bryce_canyon` | 0.47% | **none** |
| `terralith:orchid_swamp` | 0.43% | **none** |
| `birch_forest` | 0.40% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:birch_taiga` | 0.40% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:rocky_jungle` | 0.40% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `terralith:siberian_taiga` | 0.40% | **none** |
| `terralith:snowy_badlands` | 0.40% | **none** |
| `eroded_badlands` | 0.37% | `philipsruins:badlands_structures` 1/16 |
| `terralith:ice_marsh` | 0.37% | **none** |
| `snowy_slopes` | 0.33% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:fractured_savanna` | 0.33% | **none** |
| `terralith:shrubland` | 0.33% | **none** |
| `frozen_river` | 0.30% | **none** |
| `terralith:blooming_plateau` | 0.30% | **none** |
| `terralith:glacial_chasm` | 0.30% | `terralith:glacial_hut` 1/16 |
| `terralith:steppe` | 0.30% | **none** |
| `snowy_beach` | 0.27% | **none** |
| `terralith:sakura_grove` | 0.27% | **none** |
| `terralith:stony_spires` | 0.27% | **none** |
| `terralith:tropical_jungle` | 0.27% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `grove` | 0.23% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `windswept_forest` | 0.23% | **none** |
| `terralith:blooming_valley` | 0.23% | **none** |
| `terralith:painted_mountains` | 0.23% | **none** |
| `terralith:moonlight_valley` | 0.20% | **none** |
| `flower_forest` | 0.17% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `mushroom_fields` | 0.17% | **none** |
| `terralith:moonlight_grove` | 0.17% | **none** |
| `terralith:warped_mesa` | 0.17% | **none** |
| `terralith:windswept_spires` | 0.17% | **none** |
| `terralith:wintry_forest` | 0.17% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `bamboo_jungle` | 0.13% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `terralith:haze_mountain` | 0.13% | **none** |
| `ice_spikes` | 0.10% | **none** |
| `terralith:alpine_grove` | 0.10% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:gravel_desert` | 0.10% | **none** |
| `terralith:lavender_valley` | 0.10% | **none** |
| `terralith:lush_valley` | 0.10% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:skylands_autumn` | 0.10% | **none** |
| `terralith:white_mesa` | 0.10% | **none** |
| `sunflower_plains` | 0.07% | `philipsruins:ancient_towers` 1/16, `philipsruins:ancient_ruins` 1/20, `philipsruins:field_stone_ruins_rocks` 1/23, `philipsruins:lost_soul_city` 1/38 |
| `terralith:desert_canyon` | 0.07% | `philipsruins:desert_structures` 1/16 |
| `terralith:sandstone_valley` | 0.07% | `philipsruins:desert_structures` 1/16 |
| `terralith:siberian_grove` | 0.07% | **none** |
| `terralith:snowy_maple_forest` | 0.07% | **none** |
| `terralith:warm_river` | 0.07% | **none** |
| `terralith:yosemite_lowlands` | 0.07% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `windswept_gravelly_hills` | 0.03% | **none** |
| `windswept_hills` | 0.03% | **none** |
| `terralith:amethyst_rainforest` | 0.03% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `terralith:ancient_sands` | 0.03% | **none** |
| `terralith:desert_oasis` | 0.03% | `philipsruins:desert_structures` 1/16 |
| `terralith:emerald_peaks` | 0.03% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:lavender_forest` | 0.03% | **none** |
| `terralith:lush_desert` | 0.03% | `philipsruins:desert_structures` 1/16 |
| `terralith:mirage_isles` | 0.03% | **none** |
| `terralith:red_oasis` | 0.03% | **none** |
| `terralith:savanna_slopes` | 0.03% | **none** |
| `terralith:skylands_spring` | 0.03% | **none** |
| `terralith:skylands_summer` | 0.03% | **none** |
| `terralith:skylands_winter` | 0.03% | **none** |
| `terralith:volcanic_crater` | 0.03% | **none** |
| `terralith:yosemite_cliffs` | 0.03% | **none** |
| `deep_dark` | 0.00% | `philipsruins:underground_structures` 1/28, `philipsruins:lost_soul_city` 1/38 |
| `stony_shore` | 0.00% | `philipsruins:field_stone_ruins` 1/16, `philipsruins:level_one_ruins` 1/16, `philipsruins:level_three_ruins` 1/18, `philipsruins:level_two_ruins` 1/23, `philipsruins:level_two_ruins_pool` 1/23, `philipsruins:lost_soul_city` 1/38, `philipsruins:start_nether_ruin` 1/64 |
| `terralith:alpha_islands` | 0.00% | **none** |
| `terralith:alpha_islands_winter` | 0.00% | **none** |
| `terralith:amethyst_canyon` | 0.00% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `terralith:ashen_savanna` | 0.00% | **none** |
| `terralith:basalt_cliffs` | 0.00% | **none** |
| `terralith:caldera` | 0.00% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:cave/frostfire_caves` | 0.00% | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** |
| `terralith:cloud_forest` | 0.00% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:desert_spires` | 0.00% | `philipsruins:desert_structures` 1/16 |
| `terralith:frozen_cliffs` | 0.00% | **none** |
| `terralith:granite_cliffs` | 0.00% | **none** |
| `terralith:rocky_shrubland` | 0.00% | **none** |
| `terralith:sakura_valley` | 0.00% | **none** |
| `terralith:shield_clearing` | 0.00% | **none** |
| `terralith:snowy_shield` | 0.00% | **none** |
| `terralith:valley_clearing` | 0.00% | **none** |
| `terralith:white_cliffs` | 0.00% | **none** |
| `terralith:wintry_lowlands` | 0.00% | **none** |
| `lush_caves` | 12.16% | `terralith:underground/oak_cabin` 1/7.4, `philipsruins:lost_soul_city` 1/38 |
| `dripstone_caves` | 2.18% | `philipsruins:lost_soul_city` 1/38 |
| `terralith:scarlet_mountains` | 0.04% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |

## 11. Professions per village style

Which profession buildings each village style can build, read from every pool of the style in `pool_config.json` after your pool edits and `pool_tags.json` multipliers (so the tech houses tuned to 0 are not counted). A profession is found by the building's name (`plains_armorer_house_1`, `tudor_butcher_and_armorer_1`, Terralith's `church`), so a building named only "house" may still hold a workstation. Zombie variants are left out. **no** = the style never builds it (villagers there only get that job from a workstation elsewhere). Styles map to villages through each village structure's `start_pool`.

| Style | Villages | armorer | butcher | cartographer | cleric | farmer | fisherman | fletcher | leatherworker | librarian | mason | shepherd | toolsmith | weaponsmith | Missing | Modded buildings |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `kaisyn:village/badlands_pueblo` | `towns_and_towers:village_badlands` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/birch_forest_romanian` | `towns_and_towers:village_birch_forest` | yes | yes | yes | **no** | yes | yes | yes | yes | yes | yes | yes | yes | yes | 1 | - |
| `kaisyn:village/exclusives/classic` | `towns_and_towers:exclusives/village_classic` | **no** | yes | **no** | yes | yes | **no** | **no** | **no** | yes | **no** | **no** | **no** | **no** | 9 | - |
| `kaisyn:village/exclusives/iberian` | `towns_and_towers:exclusives/village_iberian` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/exclusives/mediterranean` | `towns_and_towers:exclusives/village_mediterranean` | yes | yes | yes | **no** | yes | yes | yes | yes | yes | yes | yes | yes | yes | 1 | - |
| `kaisyn:village/exclusives/nilotic` | `towns_and_towers:exclusives/village_nilotic` | yes | yes | yes | yes | yes | yes | yes | **no** | **no** | yes | yes | yes | yes | 2 | - |
| `kaisyn:village/exclusives/rustic` | `towns_and_towers:exclusives/village_rustic` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/exclusives/swedish` | `towns_and_towers:exclusives/village_swedish` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/exclusives/tudor` | `towns_and_towers:exclusives/village_tudor` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/exclusives/wandering_trader_camp` | `towns_and_towers:exclusives/village_wandering_trader_camp` | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | 13 | - |
| `kaisyn:village/flower_forest_japanese` | `towns_and_towers:village_flower_forest` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/forest_ruins` | `towns_and_towers:village_forest` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/jungle_tribal` | `towns_and_towers:village_jungle` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/meadow_swiss` | `towns_and_towers:village_meadow` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/mushroom_fields_fantasy` | `towns_and_towers:village_mushroom_fields` | yes | yes | yes | **no** | yes | yes | yes | yes | **no** | yes | yes | yes | yes | 2 | - |
| `kaisyn:village/old_growth_taiga_polish` | `towns_and_towers:village_old_growth_taiga` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/snowy_taiga_viking` | `towns_and_towers:village_snowy_taiga` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/sparse_jungle_polynesian` | `towns_and_towers:village_sparse_jungle` | yes | yes | yes | **no** | yes | yes | yes | yes | yes | yes | yes | yes | yes | 1 | - |
| `kaisyn:village/swamp_boat` | `towns_and_towers:village_swamp` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | - |
| `kaisyn:village/wooded_badlands_tipi` | `towns_and_towers:village_wooded_badlands` | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | **no** | 13 | - |
| `minecraft:village/desert` | `village_desert` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | beautify (1), chefsdelight (2), cloudstorage (1), domesticationinnovation (1), etched (1), farmersdelight (1), incubation (1), monobank (2), wares (1) |
| `minecraft:village/plains` | `village_plains` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | beautify (1), chefsdelight (2), cloudstorage (1), domesticationinnovation (1), etched (1), farmersdelight (1), incubation (1), monobank (2), wares (1) |
| `minecraft:village/savanna` | `village_savanna` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | beautify (1), chefsdelight (2), cloudstorage (1), domesticationinnovation (1), etched (1), farmersdelight (1), incubation (1), monobank (2), wares (1) |
| `minecraft:village/snowy` | `village_snowy` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | beautify (1), chefsdelight (2), cloudstorage (1), domesticationinnovation (1), etched (1), farmersdelight (1), incubation (1), monobank (2), wares (1) |
| `minecraft:village/taiga` | `village_taiga` | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | yes | 0 | beautify (1), chefsdelight (2), cloudstorage (1), domesticationinnovation (1), etched (1), farmersdelight (1), incubation (1), monobank (2), wares (1) |
| `terralith:village/desert` | `terralith:fortified_desert_village` | **no** | yes | yes | **no** | yes | yes | yes | **no** | yes | yes | yes | yes | yes | 3 | - |
| `terralith:village/fortified` | `terralith:fortified_village` | **no** | yes | yes | yes | yes | yes | yes | **no** | yes | yes | yes | **no** | **no** | 4 | - |

Buildings whose profession the name doesn't say (not counted above):

- `kaisyn:village/badlands_pueblo`: badlands_misc_professions_1 (several professions, not named)
- `kaisyn:village/exclusives/classic`: classic_blacksmith_1 (a smith, trade not in the name)
- `terralith:village/fortified`: smith1 (a smith, trade not in the name)

