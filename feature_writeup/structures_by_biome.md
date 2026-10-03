# Structures by biome

Generated 2026-10-03 13:26 by `python3 tools/gen_structure_biome_table.py`. Do not edit; re-run it after changing `structure_groups.json`, the structure tags, biome merges, village pools or the mod list. Test prefix: none (a reference, checked against the game through ST-19 and ST-39 in [structure_tuning.md](structure_tuning.md)).

**How to read it.** Every structure that can spawn in a biome, grouped by kind. A rate like `1/48` means one structure of that kind per 48 x 48 chunks of that biome, as the game would place them now: the group grids, weights and frequencies in `structure_groups.json`, disabled members left out, structures another mod blocks in code left out. A structure is listed under its first kind in this order: Villages, Houses, Pillager, Sky, Surface dungeons, Ruins, Water, Underground dungeons, Underground, Environmental, Nether, End, Other dungeons (by the `type/*` and meta tags in `structure_tags.json`). Biome areas are each biome's share of its dimension layer after biome tuning (`biome_config.json`); 0% = removed by biome tuning. **none** = a hole.

## 0. Holes at a glance (overworld surface)

Biomes with at least 0.05% of the overworld surface and no structure of a kind, biggest first. Oceans and rivers are expected holes for land kinds.

| Kind | Biomes without it | Their share of the surface | Biomes |
|---|---|---|---|
| Villages | 29 of 100 | 32% | `ocean` 4.8%, `warm_ocean` 3.7%, `lukewarm_ocean` 3.2%, `deep_frozen_ocean` 3.0%, `cold_ocean` 3.0%, `river` 1.8%, `dark_forest` 1.7%, `terralith:glacial_chasm` 1.6%, `frozen_ocean` 1.5%, `savanna_plateau` 1.0%, `frozen_peaks` 0.9%, `terralith:volcanic_peaks` 0.7%, `mangrove_swamp` 0.7%, `windswept_savanna` 0.6%, `stony_peaks` 0.6%, `jagged_peaks` 0.5%, `eroded_badlands` 0.4%, `bamboo_jungle` 0.3%, `frozen_river` 0.3%, `ice_spikes` 0.2%, `snowy_beach` 0.2%, `windswept_hills` 0.2%, `terralith:stony_spires` 0.2%, `terralith:warm_river` 0.2%, `windswept_gravelly_hills` 0.2%, `terralith:blooming_valley` 0.2%, `terralith:haze_mountain` 0.1%, `terralith:windswept_spires` 0.1%, `terralith:yosemite_cliffs` 0.1% |
| Houses | 54 of 100 | 61% | `deep_ocean` 8.1%, `deep_lukewarm_ocean` 7.2%, `deep_cold_ocean` 6.8%, `ocean` 4.8%, `warm_ocean` 3.7%, `lukewarm_ocean` 3.2%, `deep_frozen_ocean` 3.0%, `cold_ocean` 3.0%, `terralith:brushland` 2.1%, `terralith:glacial_chasm` 1.6%, `frozen_ocean` 1.5%, `terralith:arid_highlands` 1.0%, `terralith:highlands` 1.0%, `frozen_peaks` 0.9%, `wooded_badlands` 0.9%, `terralith:alpine_highlands` 0.9%, `terralith:volcanic_peaks` 0.7%, `terralith:orchid_swamp` 0.6%, `windswept_savanna` 0.6%, `terralith:siberian_taiga` 0.6%, `stony_peaks` 0.6%, `terralith:temperate_highlands` 0.6%, `terralith:steppe` 0.5%, `terralith:hot_shrubland` 0.5%, `jagged_peaks` 0.5%, `terralith:sandstone_valley` 0.5%, `terralith:shrubland` 0.5%, `terralith:rocky_jungle` 0.4%, `terralith:bryce_canyon` 0.3%, `terralith:desert_canyon` 0.3%, `terralith:sakura_grove` 0.3%, `terralith:savanna_badlands` 0.3%, `terralith:scarlet_mountains` 0.3%, `terralith:lavender_valley` 0.3%, `ice_spikes` 0.2%, `terralith:red_oasis` 0.2%, `terralith:stony_spires` 0.2%, `terralith:warm_river` 0.2%, `mushroom_fields` 0.2%, `terralith:blooming_plateau` 0.2%, `terralith:blooming_valley` 0.2%, `terralith:desert_spires` 0.2%, `terralith:amethyst_rainforest` 0.2%, `terralith:painted_mountains` 0.1%, `terralith:tropical_jungle` 0.1%, `terralith:amethyst_canyon` 0.1%, `terralith:moonlight_grove` 0.1%, `terralith:moonlight_valley` 0.1%, `terralith:windswept_spires` 0.1%, `terralith:yosemite_cliffs` 0.1%, `terralith:alpha_islands` 0.1%, `terralith:ashen_savanna` 0.1%, `terralith:lush_desert` 0.1%, `terralith:mirage_isles` 0.1% |
| Pillager | 3 of 100 | 2% | `river` 1.8%, `frozen_river` 0.3%, `terralith:warm_river` 0.2% |
| Surface dungeons | 82 of 100 | 91% | `deep_ocean` 8.1%, `deep_lukewarm_ocean` 7.2%, `deep_cold_ocean` 6.8%, `ocean` 4.8%, `terralith:yellowstone` 4.2%, `warm_ocean` 3.7%, `lukewarm_ocean` 3.2%, `deep_frozen_ocean` 3.0%, `cold_ocean` 3.0%, `plains` 2.9%, `forest` 2.9%, `terralith:brushland` 2.1%, `savanna` 1.9%, `river` 1.8%, `dark_forest` 1.7%, `terralith:glacial_chasm` 1.6%, `frozen_ocean` 1.5%, `snowy_plains` 1.4%, `terralith:rocky_mountains` 1.3%, `terralith:forested_highlands` 1.1%, `savanna_plateau` 1.0%, `snowy_taiga` 1.0%, `terralith:arid_highlands` 1.0%, `taiga` 1.0%, `terralith:highlands` 1.0%, `meadow` 1.0%, `frozen_peaks` 0.9%, `old_growth_spruce_taiga` 0.9%, `beach` 0.9%, `terralith:alpine_highlands` 0.9%, `old_growth_pine_taiga` 0.7%, `terralith:cold_shrubland` 0.7%, `terralith:volcanic_peaks` 0.7%, `mangrove_swamp` 0.7%, `terralith:shield` 0.7%, `windswept_savanna` 0.6%, `terralith:gravel_beach` 0.6%, `terralith:siberian_taiga` 0.6%, `stony_peaks` 0.6%, `terralith:temperate_highlands` 0.6%, `old_growth_birch_forest` 0.5%, `terralith:steppe` 0.5%, `sparse_jungle` 0.5%, `terralith:hot_shrubland` 0.5%, `jagged_peaks` 0.5%, `terralith:shrubland` 0.5%, `snowy_slopes` 0.4%, `terralith:gravel_desert` 0.4%, `terralith:birch_taiga` 0.4%, `terralith:bryce_canyon` 0.3%, `terralith:sakura_grove` 0.3%, `terralith:savanna_badlands` 0.3%, `terralith:scarlet_mountains` 0.3%, `terralith:snowy_badlands` 0.3%, `birch_forest` 0.3%, `frozen_river` 0.3%, `terralith:lavender_valley` 0.3%, `snowy_beach` 0.2%, `windswept_hills` 0.2%, `terralith:red_oasis` 0.2%, `terralith:stony_spires` 0.2%, `terralith:warm_river` 0.2%, `mushroom_fields` 0.2%, `windswept_gravelly_hills` 0.2%, `terralith:blooming_plateau` 0.2%, `terralith:blooming_valley` 0.2%, `terralith:fractured_savanna` 0.2%, `terralith:wintry_forest` 0.2%, `grove` 0.2%, `terralith:haze_mountain` 0.1%, `terralith:lush_valley` 0.1%, `terralith:painted_mountains` 0.1%, `flower_forest` 0.1%, `terralith:moonlight_grove` 0.1%, `terralith:moonlight_valley` 0.1%, `terralith:windswept_spires` 0.1%, `terralith:yosemite_cliffs` 0.1%, `terralith:yosemite_lowlands` 0.1%, `terralith:alpha_islands` 0.1%, `terralith:ashen_savanna` 0.1%, `terralith:cloud_forest` 0.1%, `terralith:mirage_isles` 0.1% |
| Ruins | 51 of 100 | 59% | `deep_ocean` 8.1%, `deep_lukewarm_ocean` 7.2%, `deep_cold_ocean` 6.8%, `ocean` 4.8%, `warm_ocean` 3.7%, `lukewarm_ocean` 3.2%, `deep_frozen_ocean` 3.0%, `cold_ocean` 3.0%, `terralith:brushland` 2.1%, `river` 1.8%, `frozen_ocean` 1.5%, `terralith:arid_highlands` 1.0%, `terralith:highlands` 1.0%, `beach` 0.9%, `terralith:alpine_highlands` 0.9%, `terralith:cold_shrubland` 0.7%, `terralith:orchid_swamp` 0.6%, `terralith:gravel_beach` 0.6%, `terralith:siberian_taiga` 0.6%, `terralith:temperate_highlands` 0.6%, `terralith:steppe` 0.5%, `terralith:hot_shrubland` 0.5%, `terralith:shrubland` 0.5%, `terralith:gravel_desert` 0.4%, `terralith:bryce_canyon` 0.3%, `terralith:ice_marsh` 0.3%, `terralith:sakura_grove` 0.3%, `terralith:savanna_badlands` 0.3%, `terralith:snowy_badlands` 0.3%, `frozen_river` 0.3%, `terralith:lavender_valley` 0.3%, `ice_spikes` 0.2%, `snowy_beach` 0.2%, `windswept_hills` 0.2%, `terralith:red_oasis` 0.2%, `terralith:stony_spires` 0.2%, `terralith:warm_river` 0.2%, `mushroom_fields` 0.2%, `windswept_gravelly_hills` 0.2%, `terralith:blooming_plateau` 0.2%, `terralith:blooming_valley` 0.2%, `terralith:fractured_savanna` 0.2%, `terralith:haze_mountain` 0.1%, `terralith:painted_mountains` 0.1%, `terralith:moonlight_grove` 0.1%, `terralith:moonlight_valley` 0.1%, `terralith:windswept_spires` 0.1%, `terralith:yosemite_cliffs` 0.1%, `terralith:alpha_islands` 0.1%, `terralith:ashen_savanna` 0.1%, `terralith:mirage_isles` 0.1% |

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

| Biome | Area | Villages | Houses | Pillager | Sky | Surface dungeons | Ruins | Water | Underground dungeons | Underground | Environmental | Other dungeons | Other |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `deep_ocean` | 8.07% | 1 · 1/112 | **none** | 2 · 1/100 | 3 · 1/79 | **none** | **none** | 11 · 1/6.9 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/178 | 1 · 1/101 | **none** |
| `deep_lukewarm_ocean` | 7.23% | 1 · 1/112 | **none** | 2 · 1/100 | 3 · 1/79 | **none** | **none** | 11 · 1/6.1 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | 1 · 1/101 | **none** |
| `deep_cold_ocean` | 6.77% | 1 · 1/123 | **none** | 3 · 1/80 | 3 · 1/79 | **none** | **none** | 11 · 1/6.9 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | 1 · 1/101 | **none** |
| `ocean` | 4.77% | **none** | **none** | 3 · 1/74 | 3 · 1/79 | **none** | **none** | 11 · 1/7.3 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | **none** | **none** |
| `terralith:yellowstone` | 4.20% | 1 · 1/103 | 4 · 1/54 | 11 · 1/42 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `warm_ocean` | 3.73% | **none** | **none** | 2 · 1/93 | 3 · 1/79 | **none** | **none** | 11 · 1/6.4 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `lukewarm_ocean` | 3.17% | **none** | **none** | 2 · 1/93 | 3 · 1/79 | **none** | **none** | 11 · 1/6.4 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | **none** | **none** |
| `deep_frozen_ocean` | 3.00% | **none** | **none** | 2 · 1/57 | 3 · 1/79 | **none** | **none** | 7 · 1/6.9 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/178 | 1 · 1/101 | **none** |
| `cold_ocean` | 2.97% | **none** | **none** | 2 · 1/79 | 3 · 1/79 | **none** | **none** | 11 · 1/7.3 | 1 · 1/7.1 | 1 · 1/16 | 2 · 1/18 | **none** | **none** |
| `plains` | 2.93% | 2 · 1/49 | 20 · 1/18 | 15 · 1/34 | 2 · 1/89 | **none** | 12 · 1/9.5 | **none** | 9 · 1/5.5 | 1 · 1/16 | 3 · 1/28 | **none** | 6 · 1/24 |
| `forest` | 2.87% | 1 · 1/115 | 13 · 1/21 | 9 · 1/55 | 3 · 1/79 | **none** | 14 · 1/9.5 | **none** | 7 · 1/5.6 | 1 · 1/16 | 6 · 1/15 | **none** | 3 · 1/38 |
| `terralith:brushland` | 2.13% | 3 · 1/51 | **none** | 10 · 1/44 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `savanna` | 1.93% | 1 · 1/53 | 5 · 1/28 | 16 · 1/34 | 2 · 1/89 | **none** | 4 · 1/10 | **none** | 9 · 1/5.5 | 1 · 1/16 | 1 · 1/126 | **none** | 2 · 1/32 |
| `river` | 1.80% | **none** | 1 · 1/49 | **none** | 3 · 1/79 | **none** | **none** | 1 · 1/9.0 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `dark_forest` | 1.67% | **none** | 9 · 1/23 | 9 · 1/32 | 3 · 1/79 | **none** | 18 · 1/9.3 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `terralith:glacial_chasm` | 1.60% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `badlands` | 1.50% | 1 · 1/104 | 2 · 1/25 | 9 · 1/50 | 2 · 1/89 | 1 · 1/25 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `frozen_ocean` | 1.50% | **none** | **none** | 2 · 1/57 | 3 · 1/79 | **none** | **none** | 6 · 1/7.1 | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `snowy_plains` | 1.37% | 1 · 1/48 | 2 · 1/25 | 16 · 1/37 | 2 · 1/89 | **none** | 4 · 1/10 | **none** | 9 · 1/5.5 | 1 · 1/16 | 1 · 1/126 | **none** | 2 · 1/32 |
| `terralith:rocky_mountains` | 1.33% | 2 · 1/61 | 1 · 1/33 | 10 · 1/42 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `desert` | 1.20% | 2 · 1/55 | 2 · 1/32 | 16 · 1/38 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `swamp` | 1.07% | 1 · 1/68 | 1 · 1/46 | 11 · 1/18 | 2 · 1/89 | 1 · 1/37 | 11 · 1/9.3 | **none** | 9 · 1/5.5 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:forested_highlands` | 1.07% | 5 · 1/62 | 4 · 1/54 | 12 · 1/44 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `savanna_plateau` | 1.03% | **none** | 2 · 1/34 | 8 · 1/43 | 2 · 1/89 | **none** | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `snowy_taiga` | 1.03% | 1 · 1/109 | 6 · 1/41 | 9 · 1/52 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | 1 · 1/35 |
| `terralith:arid_highlands` | 1.03% | 2 · 1/67 | **none** | 10 · 1/45 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `taiga` | 1.00% | 1 · 1/49 | 6 · 1/32 | 16 · 1/34 | 2 · 1/89 | **none** | 7 · 1/9.8 | **none** | 7 · 1/5.6 | 1 · 1/16 | 4 · 1/17 | **none** | 1 · 1/67 |
| `terralith:highlands` | 1.00% | 2 · 1/56 | **none** | 9 · 1/53 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `meadow` | 0.97% | 3 · 1/51 | 3 · 1/41 | 10 · 1/46 | 2 · 1/89 | **none** | 10 · 1/9.6 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | 2 · 1/32 |
| `frozen_peaks` | 0.90% | **none** | **none** | 9 · 1/42 | 2 · 1/89 | **none** | 9 · 1/8.8 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `old_growth_spruce_taiga` | 0.90% | 1 · 1/103 | 6 · 1/32 | 9 · 1/48 | 2 · 1/89 | **none** | 6 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 4 · 1/17 | **none** | **none** |
| `beach` | 0.87% | 1 · 1/119 | 2 · 1/37 | 9 · 1/48 | 2 · 1/89 | **none** | **none** | 1 · 1/9.0 | 1 · 1/7.1 | 2 · 1/7.3 | 1 · 1/126 | **none** | **none** |
| `wooded_badlands` | 0.87% | 1 · 1/115 | **none** | 9 · 1/38 | 2 · 1/89 | 1 · 1/25 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:alpine_highlands` | 0.87% | 3 · 1/57 | **none** | 10 · 1/42 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `old_growth_pine_taiga` | 0.73% | 1 · 1/103 | 6 · 1/32 | 9 · 1/48 | 2 · 1/89 | **none** | 6 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 4 · 1/17 | **none** | **none** |
| `terralith:cold_shrubland` | 0.73% | 2 · 1/49 | 1 · 1/33 | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:volcanic_peaks` | 0.70% | **none** | **none** | 9 · 1/42 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `mangrove_swamp` | 0.67% | **none** | 1 · 1/46 | 10 · 1/16 | 2 · 1/89 | **none** | 12 · 1/11 | **none** | 4 · 1/6.9 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:shield` | 0.67% | 4 · 1/66 | 4 · 1/54 | 13 · 1/44 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:orchid_swamp` | 0.63% | 1 · 1/68 | **none** | 11 · 1/18 | 2 · 1/89 | 1 · 1/37 | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `windswept_savanna` | 0.60% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:gravel_beach` | 0.60% | 1 · 1/119 | 1 · 1/91 | 9 · 1/48 | 2 · 1/89 | **none** | **none** | 1 · 1/9.0 | 1 · 1/7.1 | 2 · 1/7.3 | 1 · 1/126 | **none** | **none** |
| `terralith:siberian_taiga` | 0.60% | 2 · 1/60 | **none** | 10 · 1/42 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `stony_peaks` | 0.57% | **none** | **none** | 9 · 1/42 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:temperate_highlands` | 0.57% | 4 · 1/68 | **none** | 10 · 1/57 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `jungle` | 0.53% | 1 · 1/72 | 1 · 1/45 | 9 · 1/50 | 3 · 1/79 | 1 · 1/18 | 9 · 1/12 | **none** | 7 · 1/5.6 | 1 · 1/16 | 4 · 1/17 | **none** | **none** |
| `old_growth_birch_forest` | 0.53% | 1 · 1/108 | 7 · 1/26 | 9 · 1/54 | 3 · 1/79 | **none** | 12 · 1/9.6 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `terralith:steppe` | 0.53% | 2 · 1/65 | **none** | 9 · 1/53 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | 2 · 1/32 |
| `sparse_jungle` | 0.50% | 1 · 1/72 | 1 · 1/45 | 9 · 1/40 | 3 · 1/79 | **none** | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 4 · 1/17 | **none** | **none** |
| `terralith:hot_shrubland` | 0.50% | 2 · 1/52 | **none** | 10 · 1/44 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `jagged_peaks` | 0.47% | **none** | **none** | 9 · 1/42 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:sandstone_valley` | 0.47% | 1 · 1/57 | **none** | 9 · 1/53 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:shrubland` | 0.47% | 2 · 1/52 | **none** | 10 · 1/44 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `eroded_badlands` | 0.43% | **none** | 1 · 1/27 | 8 · 1/43 | 2 · 1/89 | 1 · 1/25 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:rocky_jungle` | 0.43% | 2 · 1/72 | **none** | 10 · 1/45 | 3 · 1/79 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `snowy_slopes` | 0.40% | 1 · 1/91 | 1 · 1/44 | 10 · 1/42 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | 1 · 1/25 |
| `terralith:gravel_desert` | 0.40% | 1 · 1/48 | 1 · 1/33 | 9 · 1/49 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:birch_taiga` | 0.37% | 3 · 1/70 | 4 · 1/54 | 11 · 1/48 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `bamboo_jungle` | 0.33% | **none** | 1 · 1/45 | 8 · 1/43 | 3 · 1/79 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 4 · 1/17 | **none** | **none** |
| `terralith:bryce_canyon` | 0.33% | 2 · 1/110 | **none** | 10 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 2 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:desert_canyon` | 0.33% | 1 · 1/57 | **none** | 9 · 1/53 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:ice_marsh` | 0.33% | 2 · 1/56 | 1 · 1/33 | 11 · 1/18 | 2 · 1/89 | 1 · 1/37 | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:sakura_grove` | 0.33% | 1 · 1/119 | **none** | 11 · 1/34 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:savanna_badlands` | 0.33% | 3 · 1/77 | **none** | 11 · 1/41 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 2 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:scarlet_mountains` | 0.33% | 1 · 1/103 | **none** | 11 · 1/42 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:snowy_badlands` | 0.33% | 2 · 1/65 | 1 · 1/33 | 9 · 1/50 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `birch_forest` | 0.30% | 1 · 1/108 | 9 · 1/25 | 9 · 1/54 | 3 · 1/79 | **none** | 13 · 1/9.6 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | 3 · 1/34 |
| `frozen_river` | 0.27% | **none** | 1 · 1/49 | **none** | 3 · 1/79 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `terralith:lavender_valley` | 0.27% | 1 · 1/119 | **none** | 9 · 1/55 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `ice_spikes` | 0.23% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | 1 · 1/25 | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `snowy_beach` | 0.23% | **none** | 2 · 1/78 | 9 · 1/48 | 2 · 1/89 | **none** | **none** | 1 · 1/9.0 | 1 · 1/7.1 | 2 · 1/7.3 | 1 · 1/126 | **none** | 1 · 1/18 |
| `windswept_hills` | 0.23% | **none** | 4 · 1/20 | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:red_oasis` | 0.23% | 2 · 1/110 | **none** | 10 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 5 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:stony_spires` | 0.23% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:warm_river` | 0.23% | **none** | **none** | **none** | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `mushroom_fields` | 0.20% | 1 · 1/119 | **none** | 9 · 1/53 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `windswept_gravelly_hills` | 0.20% | **none** | 4 · 1/20 | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:blooming_plateau` | 0.20% | 2 · 1/52 | **none** | 9 · 1/49 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:blooming_valley` | 0.20% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:desert_spires` | 0.20% | 1 · 1/57 | **none** | 9 · 1/53 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:fractured_savanna` | 0.20% | 1 · 1/53 | 3 · 1/22 | 9 · 1/39 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:wintry_forest` | 0.20% | 3 · 1/68 | 5 · 1/42 | 11 · 1/46 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `grove` | 0.17% | 1 · 1/117 | 6 · 1/34 | 10 · 1/51 | 3 · 1/79 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `terralith:amethyst_rainforest` | 0.17% | 2 · 1/72 | **none** | 10 · 1/45 | 3 · 1/79 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `terralith:haze_mountain` | 0.13% | **none** | 3 · 1/22 | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:lush_valley` | 0.13% | 2 · 1/60 | 4 · 1/54 | 11 · 1/42 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:painted_mountains` | 0.13% | 2 · 1/110 | **none** | 10 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 2 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:tropical_jungle` | 0.13% | 2 · 1/72 | **none** | 10 · 1/45 | 3 · 1/79 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `flower_forest` | 0.10% | 1 · 1/119 | 8 · 1/23 | 9 · 1/55 | 3 · 1/79 | **none** | 13 · 1/9.6 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | 1 · 1/67 |
| `terralith:amethyst_canyon` | 0.10% | 2 · 1/72 | **none** | 10 · 1/45 | 3 · 1/79 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `terralith:moonlight_grove` | 0.10% | 1 · 1/119 | **none** | 9 · 1/55 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:moonlight_valley` | 0.10% | 1 · 1/119 | **none** | 9 · 1/55 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:windswept_spires` | 0.10% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:yosemite_cliffs` | 0.10% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:yosemite_lowlands` | 0.10% | 1 · 1/103 | 4 · 1/54 | 10 · 1/42 | 2 · 1/89 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:alpha_islands` | 0.07% | 1 · 1/119 | **none** | 9 · 1/55 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:ashen_savanna` | 0.07% | 1 · 1/53 | **none** | 9 · 1/39 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:cloud_forest` | 0.07% | 2 · 1/114 | 7 · 1/32 | 11 · 1/49 | 3 · 1/79 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `terralith:lush_desert` | 0.07% | 2 · 1/55 | **none** | 9 · 1/53 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:mirage_isles` | 0.07% | 1 · 1/119 | **none** | 9 · 1/53 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `windswept_forest` | 0.03% | **none** | 5 · 1/19 | 8 · 1/43 | 2 · 1/89 | **none** | 7 · 1/9.4 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:alpine_grove` | 0.03% | 3 · 1/73 | 7 · 1/34 | 9 · 1/56 | 3 · 1/79 | **none** | 5 · 1/9.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `terralith:desert_oasis` | 0.03% | 3 · 1/30 | **none** | 10 · 1/45 | 2 · 1/89 | 2 · 1/19 | 1 · 1/16 | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:emerald_peaks` | 0.03% | 1 · 1/48 | 1 · 1/33 | 9 · 1/42 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:jungle_mountains` | 0.03% | 2 · 1/72 | 3 · 1/22 | 10 · 1/45 | 3 · 1/79 | 1 · 1/18 | 2 · 1/13 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/178 | **none** | **none** |
| `terralith:lavender_forest` | 0.03% | 2 · 1/76 | **none** | 10 · 1/51 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:sakura_valley` | 0.03% | 1 · 1/119 | **none** | 11 · 1/34 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:skylands_summer` | 0.03% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:skylands_winter` | 0.03% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:warped_mesa` | 0.03% | 3 · 1/34 | **none** | 11 · 1/40 | 2 · 1/89 | **none** | **none** | **none** | 5 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:wintry_lowlands` | 0.03% | 3 · 1/68 | 1 · 1/33 | 11 · 1/46 | 2 · 1/89 | 1 · 1/25 | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `deep_dark` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | 2 · 1/23 | **none** | 5 · 1/6.6 | **none** | 1 · 1/126 | **none** | **none** |
| `stony_shore` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | 7 · 1/7.9 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `sunflower_plains` | 0.00% | 2 · 1/76 | 18 · 1/21 | 9 · 1/53 | 2 · 1/89 | **none** | 4 · 1/10 | **none** | 9 · 1/5.5 | 1 · 1/16 | 1 · 1/126 | **none** | 4 · 1/28 |
| `terralith:alpha_islands_winter` | 0.00% | 1 · 1/48 | 1 · 1/33 | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:ancient_sands` | 0.00% | 2 · 1/110 | **none** | 10 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 5 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:basalt_cliffs` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:caldera` | 0.00% | **none** | **none** | 9 · 1/42 | 2 · 1/89 | **none** | 3 · 1/9.4 | **none** | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:cave/frostfire_caves` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 2 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:frozen_cliffs` | 0.00% | 1 · 1/119 | **none** | 9 · 1/48 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:granite_cliffs` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:rocky_shrubland` | 0.00% | 2 · 1/49 | 1 · 1/33 | 10 · 1/47 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | 2 · 1/32 |
| `terralith:savanna_slopes` | 0.00% | 1 · 1/53 | **none** | 9 · 1/39 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:shield_clearing` | 0.00% | 2 · 1/60 | **none** | 10 · 1/45 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | 2 · 1/32 |
| `terralith:siberian_grove` | 0.00% | 3 · 1/74 | 1 · 1/33 | 11 · 1/49 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:skylands_autumn` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:skylands_spring` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:snowy_maple_forest` | 0.00% | 4 · 1/66 | 1 · 1/33 | 12 · 1/44 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:snowy_shield` | 0.00% | 2 · 1/60 | 1 · 1/33 | 9 · 1/52 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:valley_clearing` | 0.00% | 1 · 1/103 | **none** | 10 · 1/42 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | 2 · 1/32 |
| `terralith:volcanic_crater` | 0.00% | **none** | **none** | 8 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:white_cliffs` | 0.00% | 1 · 1/108 | **none** | 9 · 1/54 | 2 · 1/89 | **none** | **none** | **none** | 6 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |
| `terralith:white_mesa` | 0.00% | 2 · 1/110 | **none** | 10 · 1/43 | 2 · 1/89 | **none** | **none** | **none** | 5 · 1/5.6 | 1 · 1/16 | 1 · 1/126 | **none** | **none** |

## 2. Overworld, caves: kinds per biome

Cell = number of structures of that kind that can spawn there, and their combined rate.

| Biome | Area | Pillager | Sky | Ruins | Underground dungeons | Underground | Environmental |
|---|---|---|---|---|---|---|---|
| `terralith:cave/deep_caves` | 12.86% | 8 · 1/43 | 2 · 1/89 | **none** | 1 · 1/7.1 | 3 · 1/7.8 | 1 · 1/126 |
| `lush_caves` | 11.76% | 9 · 1/7.7 | 2 · 1/89 | 2 · 1/7.2 | 7 · 1/5.6 | 3 · 1/5.2 | 1 · 1/126 |
| `terralith:cave/andesite_caves` | 6.09% | 8 · 1/43 | 2 · 1/89 | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 |
| `dripstone_caves` | 5.08% | 8 · 1/43 | 2 · 1/89 | 1 · 1/38 | 7 · 1/5.6 | 1 · 1/16 | 1 · 1/126 |
| `terralith:cave/infested_caves` | 3.07% | 8 · 1/43 | 2 · 1/89 | **none** | 1 · 1/7.1 | 2 · 1/7.7 | 1 · 1/126 |
| `terralith:cave/granite_caves` | 2.87% | 8 · 1/43 | 2 · 1/89 | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 |
| `terralith:cave/fungal_caves` | 1.92% | 9 · 1/7.7 | 2 · 1/89 | **none** | 6 · 1/5.6 | 2 · 1/8.1 | 1 · 1/126 |
| `terralith:cave/thermal_caves` | 1.30% | 8 · 1/43 | 2 · 1/89 | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 |
| `terralith:cave/diorite_caves` | 1.21% | 8 · 1/43 | 2 · 1/89 | **none** | 1 · 1/7.1 | 1 · 1/16 | 1 · 1/126 |
| `terralith:cave/underground_jungle` | 1.15% | 9 · 1/7.1 | 2 · 1/89 | **none** | 1 · 1/7.1 | 3 · 1/4.8 | 1 · 1/126 |

## 3. Nether: kinds per biome

Cell = number of structures of that kind that can spawn there, and their combined rate.

| Biome | Area | Houses | Water | Environmental | Nether |
|---|---|---|---|---|---|
| `nether_wastes` | 46.83% | 1 · 1/47 | 1 · 1/60 | 2 · 1/5.7 | 5 · 1/15 |
| `soul_sand_valley` | 18.93% | **none** | 1 · 1/60 | 1 · 1/2.0 | 5 · 1/15 |
| `crimson_forest` | 16.07% | 1 · 1/38 | 1 · 1/60 | 1 · 1/20 | 6 · 1/9.5 |
| `basalt_deltas` | 12.40% | **none** | **none** | **none** | 4 · 1/17 |
| `warped_forest` | 5.77% | 1 · 1/38 | 1 · 1/60 | **none** | 6 · 1/9.5 |

## 4. End: kinds per biome

Cell = number of structures of that kind that can spawn there, and their combined rate.

| Biome | Area | Water | Environmental | End |
|---|---|---|---|---|
| `end_highlands` | 76.20% | 1 · 1/27 | 4 · 1/7.9 | 14 · 1/7.5 |
| `small_end_islands` | 9.00% | 1 · 1/27 | 4 · 1/7.9 | 11 · 1/8.2 |
| `the_end` | 0.33% | 1 · 1/27 | 4 · 1/7.9 | 11 · 1/8.2 |

## 5. End (inner biomes): kinds per biome

Cell = number of structures of that kind that can spawn there, and their combined rate.

| Biome | Area | Water | Environmental | End |
|---|---|---|---|---|
| `nullscape:shadowlands` | 9.85% | 1 · 1/27 | 4 · 1/7.9 | 13 · 1/7.5 |
| `nullscape:crystal_peaks` | 4.99% | 1 · 1/27 | 4 · 1/7.9 | 12 · 1/7.8 |
| `nullscape:void_barrens` | 4.94% | 1 · 1/27 | 4 · 1/7.9 | 12 · 1/7.8 |

## 6. Villages by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Villages |
|---|---|---|
| `deep_ocean` | 8.07% | `towns_and_towers:village_ocean` 1/112 |
| `deep_lukewarm_ocean` | 7.23% | `towns_and_towers:village_ocean` 1/112 |
| `deep_cold_ocean` | 6.77% | `towns_and_towers:village_ocean` 1/123 |
| `ocean` | 4.77% | **none** |
| `terralith:yellowstone` | 4.20% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `warm_ocean` | 3.73% | **none** |
| `lukewarm_ocean` | 3.17% | **none** |
| `deep_frozen_ocean` | 3.00% | **none** |
| `cold_ocean` | 2.97% | **none** |
| `plains` | 2.93% | `village_plains` 1/57, `terralith:fortified_village` 1/97 |
| `forest` | 2.87% | `towns_and_towers:village_forest` 1/115 |
| `terralith:brushland` | 2.13% | `village_savanna` 1/66, `terralith:fortified_village` 1/108, `towns_and_towers:exclusives/village_mediterranean` 1/123 |
| `savanna` | 1.93% | `village_savanna` 1/53 |
| `river` | 1.80% | **none** |
| `dark_forest` | 1.67% | **none** |
| `terralith:glacial_chasm` | 1.60% | **none** |
| `badlands` | 1.50% | `towns_and_towers:village_badlands` 1/104 |
| `frozen_ocean` | 1.50% | **none** |
| `snowy_plains` | 1.37% | `village_snowy` 1/48 |
| `terralith:rocky_mountains` | 1.33% | `village_snowy` 1/69, `towns_and_towers:village_snowy_slopes` 1/126 |
| `desert` | 1.20% | `village_desert` 1/64, `terralith:fortified_desert_village` 1/107 |
| `swamp` | 1.07% | `towns_and_towers:village_swamp` 1/68 |
| `terralith:forested_highlands` | 1.07% | `village_taiga` 1/84, `terralith:fortified_village` 1/139, `towns_and_towers:village_old_growth_taiga` 1/208, `towns_and_towers:exclusives/village_swedish` 1/216, `towns_and_towers:village_snowy_taiga` 1/221 |
| `savanna_plateau` | 1.03% | **none** |
| `snowy_taiga` | 1.03% | `towns_and_towers:village_snowy_taiga` 1/109 |
| `terralith:arid_highlands` | 1.03% | `village_savanna` 1/75, `towns_and_towers:village_badlands` 1/148 |
| `taiga` | 1.00% | `village_taiga` 1/49 |
| `terralith:highlands` | 1.00% | `village_taiga` 1/63, `towns_and_towers:exclusives/village_tudor` 1/119 |
| `meadow` | 0.97% | `village_plains` 1/65, `terralith:fortified_village` 1/110, `towns_and_towers:village_meadow` 1/122 |
| `frozen_peaks` | 0.90% | **none** |
| `old_growth_spruce_taiga` | 0.90% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `beach` | 0.87% | `towns_and_towers:village_beach` 1/119 |
| `wooded_badlands` | 0.87% | `towns_and_towers:village_wooded_badlands` 1/115 |
| `terralith:alpine_highlands` | 0.87% | `village_taiga` 1/71, `terralith:fortified_village` 1/117, `towns_and_towers:village_old_growth_taiga` 1/175 |
| `old_growth_pine_taiga` | 0.73% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `terralith:cold_shrubland` | 0.73% | `village_taiga` 1/67, `village_snowy` 1/71 |
| `terralith:volcanic_peaks` | 0.70% | **none** |
| `mangrove_swamp` | 0.67% | **none** |
| `terralith:shield` | 0.67% | `village_taiga` 1/79, `towns_and_towers:village_old_growth_taiga` 1/196, `towns_and_towers:exclusives/village_swedish` 1/203, `towns_and_towers:village_snowy_taiga` 1/208 |
| `terralith:orchid_swamp` | 0.63% | `towns_and_towers:village_swamp` 1/68 |
| `windswept_savanna` | 0.60% | **none** |
| `terralith:gravel_beach` | 0.60% | `towns_and_towers:village_beach` 1/119 |
| `terralith:siberian_taiga` | 0.60% | `village_taiga` 1/65, `towns_and_towers:village_old_growth_taiga` 1/160 |
| `stony_peaks` | 0.57% | **none** |
| `terralith:temperate_highlands` | 0.57% | `village_plains` 1/89, `terralith:fortified_village` 1/150, `towns_and_towers:village_birch_forest` 1/194, `towns_and_towers:village_forest` 1/218 |
| `jungle` | 0.53% | `towns_and_towers:village_jungle` 1/72 |
| `old_growth_birch_forest` | 0.53% | `towns_and_towers:village_birch_forest` 1/108 |
| `terralith:steppe` | 0.53% | `village_plains` 1/77, `towns_and_towers:village_sunflower_plains` 1/123 |
| `sparse_jungle` | 0.50% | `towns_and_towers:village_sparse_jungle` 1/72 |
| `terralith:hot_shrubland` | 0.50% | `village_savanna` 1/59, `towns_and_towers:exclusives/village_mediterranean` 1/110 |
| `jagged_peaks` | 0.47% | **none** |
| `terralith:sandstone_valley` | 0.47% | `village_desert` 1/57 |
| `terralith:shrubland` | 0.47% | `village_savanna` 1/59, `towns_and_towers:exclusives/village_mediterranean` 1/110 |
| `eroded_badlands` | 0.43% | **none** |
| `terralith:rocky_jungle` | 0.43% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `snowy_slopes` | 0.40% | `towns_and_towers:village_snowy_slopes` 1/91 |
| `terralith:gravel_desert` | 0.40% | `village_snowy` 1/48 |
| `terralith:birch_taiga` | 0.37% | `village_plains` 1/82, `towns_and_towers:village_birch_forest` 1/177, `towns_and_towers:village_old_growth_taiga` 1/205 |
| `bamboo_jungle` | 0.33% | **none** |
| `terralith:bryce_canyon` | 0.33% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:desert_canyon` | 0.33% | `village_desert` 1/57 |
| `terralith:ice_marsh` | 0.33% | `village_snowy` 1/69, `towns_and_towers:village_swamp` 1/94 |
| `terralith:sakura_grove` | 0.33% | `towns_and_towers:village_flower_forest` 1/119 |
| `terralith:savanna_badlands` | 0.33% | `village_savanna` 1/94, `towns_and_towers:village_badlands` 1/187, `towns_and_towers:village_wooded_badlands` 1/189 |
| `terralith:scarlet_mountains` | 0.33% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `terralith:snowy_badlands` | 0.33% | `village_snowy` 1/73, `towns_and_towers:village_badlands` 1/139 |
| `birch_forest` | 0.30% | `towns_and_towers:village_birch_forest` 1/108 |
| `frozen_river` | 0.27% | **none** |
| `terralith:lavender_valley` | 0.27% | `towns_and_towers:village_flower_forest` 1/119 |
| `ice_spikes` | 0.23% | **none** |
| `snowy_beach` | 0.23% | **none** |
| `windswept_hills` | 0.23% | **none** |
| `terralith:red_oasis` | 0.23% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:stony_spires` | 0.23% | **none** |
| `terralith:warm_river` | 0.23% | **none** |
| `mushroom_fields` | 0.20% | `towns_and_towers:village_mushroom_fields` 1/119 |
| `windswept_gravelly_hills` | 0.20% | **none** |
| `terralith:blooming_plateau` | 0.20% | `village_plains` 1/59, `towns_and_towers:village_meadow` 1/109 |
| `terralith:blooming_valley` | 0.20% | **none** |
| `terralith:desert_spires` | 0.20% | `village_desert` 1/57 |
| `terralith:fractured_savanna` | 0.20% | `village_savanna` 1/53 |
| `terralith:wintry_forest` | 0.20% | `village_snowy` 1/78, `towns_and_towers:village_old_growth_taiga` 1/185, `towns_and_towers:village_snowy_taiga` 1/196 |
| `grove` | 0.17% | `towns_and_towers:village_grove` 1/117 |
| `terralith:amethyst_rainforest` | 0.17% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `terralith:haze_mountain` | 0.13% | **none** |
| `terralith:lush_valley` | 0.13% | `village_taiga` 1/65, `towns_and_towers:village_old_growth_taiga` 1/160 |
| `terralith:painted_mountains` | 0.13% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:tropical_jungle` | 0.13% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `flower_forest` | 0.10% | `towns_and_towers:village_flower_forest` 1/119 |
| `terralith:amethyst_canyon` | 0.10% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `terralith:moonlight_grove` | 0.10% | `towns_and_towers:village_flower_forest` 1/119 |
| `terralith:moonlight_valley` | 0.10% | `towns_and_towers:village_flower_forest` 1/119 |
| `terralith:windswept_spires` | 0.10% | **none** |
| `terralith:yosemite_cliffs` | 0.10% | **none** |
| `terralith:yosemite_lowlands` | 0.10% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `terralith:alpha_islands` | 0.07% | `towns_and_towers:exclusives/village_classic` 1/119 |
| `terralith:ashen_savanna` | 0.07% | `village_savanna` 1/53 |
| `terralith:cloud_forest` | 0.07% | `towns_and_towers:village_flower_forest` 1/136, `towns_and_towers:village_old_growth_taiga` 1/214 |
| `terralith:lush_desert` | 0.07% | `village_desert` 1/64, `terralith:fortified_desert_village` 1/107 |
| `terralith:mirage_isles` | 0.07% | `towns_and_towers:village_mushroom_fields` 1/119 |
| `windswept_forest` | 0.03% | **none** |
| `terralith:alpine_grove` | 0.03% | `village_snowy` 1/92, `towns_and_towers:village_snowy_slopes` 1/168, `towns_and_towers:village_grove` 1/176 |
| `terralith:desert_oasis` | 0.03% | `towns_and_towers:exclusives/village_wandering_trader_camp` 1/35, `village_desert` 1/64, `terralith:fortified_desert_village` 1/107 |
| `terralith:emerald_peaks` | 0.03% | `village_snowy` 1/48 |
| `terralith:jungle_mountains` | 0.03% | `towns_and_towers:village_sparse_jungle` 1/101, `towns_and_towers:village_jungle` 1/101 |
| `terralith:lavender_forest` | 0.03% | `village_plains` 1/90, `towns_and_towers:village_flower_forest` 1/143 |
| `terralith:sakura_valley` | 0.03% | `towns_and_towers:village_flower_forest` 1/119 |
| `terralith:skylands_summer` | 0.03% | **none** |
| `terralith:skylands_winter` | 0.03% | **none** |
| `terralith:warped_mesa` | 0.03% | `towns_and_towers:exclusives/village_wandering_trader_camp` 1/35, `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:wintry_lowlands` | 0.03% | `village_snowy` 1/78, `towns_and_towers:village_old_growth_taiga` 1/185, `towns_and_towers:village_snowy_taiga` 1/196 |
| `deep_dark` | 0.00% | **none** |
| `stony_shore` | 0.00% | **none** |
| `sunflower_plains` | 0.00% | `towns_and_towers:village_sunflower_plains` 1/104, `terralith:fortified_village` 1/111 |
| `terralith:alpha_islands_winter` | 0.00% | `village_snowy` 1/48 |
| `terralith:ancient_sands` | 0.00% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |
| `terralith:basalt_cliffs` | 0.00% | **none** |
| `terralith:caldera` | 0.00% | **none** |
| `terralith:cave/frostfire_caves` | 0.00% | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** |
| `terralith:frozen_cliffs` | 0.00% | `towns_and_towers:village_beach` 1/119 |
| `terralith:granite_cliffs` | 0.00% | **none** |
| `terralith:rocky_shrubland` | 0.00% | `village_taiga` 1/67, `village_snowy` 1/71 |
| `terralith:savanna_slopes` | 0.00% | `village_savanna` 1/53 |
| `terralith:shield_clearing` | 0.00% | `village_taiga` 1/65, `towns_and_towers:village_snowy_taiga` 1/169 |
| `terralith:siberian_grove` | 0.00% | `village_snowy` 1/89, `towns_and_towers:village_grove` 1/170, `towns_and_towers:village_old_growth_taiga` 1/210 |
| `terralith:skylands_autumn` | 0.00% | **none** |
| `terralith:skylands_spring` | 0.00% | **none** |
| `terralith:snowy_maple_forest` | 0.00% | `village_snowy` 1/81, `towns_and_towers:village_old_growth_taiga` 1/190, `towns_and_towers:exclusives/village_swedish` 1/197, `towns_and_towers:village_snowy_taiga` 1/202 |
| `terralith:snowy_shield` | 0.00% | `village_snowy` 1/65, `towns_and_towers:village_snowy_taiga` 1/162 |
| `terralith:valley_clearing` | 0.00% | `towns_and_towers:village_old_growth_taiga` 1/103 |
| `terralith:volcanic_crater` | 0.00% | **none** |
| `terralith:white_cliffs` | 0.00% | `towns_and_towers:village_birch_forest` 1/108 |
| `terralith:white_mesa` | 0.00% | `towns_and_towers:village_badlands` 1/154, `towns_and_towers:village_wooded_badlands` 1/156 |

## 7. Houses by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Houses |
|---|---|---|
| `deep_ocean` | 8.07% | **none** |
| `deep_lukewarm_ocean` | 7.23% | **none** |
| `deep_cold_ocean` | 6.77% | **none** |
| `ocean` | 4.77% | **none** |
| `terralith:yellowstone` | 4.20% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `warm_ocean` | 3.73% | **none** |
| `lukewarm_ocean` | 3.17% | **none** |
| `deep_frozen_ocean` | 3.00% | **none** |
| `cold_ocean` | 2.97% | **none** |
| `plains` | 2.93% | `villager_fishermans_hut:villager_fishermans_hut` 1/65, `animal_barn:barn` 1/68, `villager_forge:villager_forge` 1/68, `smallhorsestable:horse_stable` 1/69, `villager_fletcher_house:villager_fletcher_house` 1/70, `mini_clock_tower:mini_clock_tower` 1/70, `villager_cleric_house:villager_cleric_house` 1/71, `villager_windmill:windmill` 1/72, `bighorsestable:big_horse_stable` 1/73, `villager_cartographer_house:villager_cartographer_house` 1/76, `villager_butcher_house:butcher_house` 1/78, `gothic_church:church` 1/81, `villager_farmer_house:villager_farmer_house` 1/85, `villager_tannery:tannery` 1/85, `small_villager_greenhouse:small_villager_greenhouse` 1/86, `farmers_structures:cooks` 1/96, `farmers_structures:aquacultures_carp` 1/114, `farmers_structures:aquacultures_common` 1/114, `simply_houses:plains_house` 1/152, `simply_houses:plains_mansion` 1/152 |
| `forest` | 2.87% | `forest_watchtower:forest_watchtower` 1/51, `sawmillhouse:sawmill` 1/52, `smallhorsestable:horse_stable` 1/57, `bighorsestable:big_horse_stable` 1/60, `gothic_church:church` 1/66, `villager_tannery:tannery` 1/70, `small_villager_greenhouse:small_villager_greenhouse` 1/70, `farmers_structures:aquacultures_carp` 1/93, `farmers_structures:aquacultures_common` 1/93, `simply_houses:forest_house` 1/192, `simply_houses:forest_stonemason` 1/192, `simply_houses:forest_chapel` 1/272, `simply_houses:forest_fort` 1/272 |
| `terralith:brushland` | 2.13% | **none** |
| `savanna` | 1.93% | `greek_villager_house:greek_house` 1/53, `farmers_structures:cooks` 1/59, `villager_country_house:country_house` 1/62, `farmers_structures:aquacultures_carp` 1/70, `farmers_structures:aquacultures_common` 1/70 |
| `river` | 1.80% | `farmers_structures:sushis` 1/49 |
| `dark_forest` | 1.67% | `sawmillhouse:sawmill` 1/40, `gothic_church:church` 1/50, `hobbit_house_:hobbit_house` 1/55, `farmers_structures:aquacultures_carp` 1/71, `farmers_structures:aquacultures_common` 1/71, `simply_houses:forest_house` 1/146, `simply_houses:forest_stonemason` 1/146, `simply_houses:forest_chapel` 1/206, `simply_houses:forest_fort` 1/206 |
| `terralith:glacial_chasm` | 1.60% | **none** |
| `badlands` | 1.50% | `saloon:saloon` 1/28, `villager_gold_washing_facility:villager_gold_washing_facility` 1/50 |
| `frozen_ocean` | 1.50% | **none** |
| `snowy_plains` | 1.37% | `mini_clock_tower:mini_clock_tower` 1/34, `igloo` 1/38 |
| `terralith:rocky_mountains` | 1.33% | `igloo` 1/33 |
| `desert` | 1.20% | `desert_village_temple:village_temple` 1/45, `desert_watchtower:watchtower` 1/45 |
| `swamp` | 1.07% | `old_swamp_fisherman_house:fisherman_house` 1/46 |
| `terralith:forested_highlands` | 1.07% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `savanna_plateau` | 1.03% | `llama_stable:llama_stable` 1/46, `villager_country_house:country_house` 1/50 |
| `snowy_taiga` | 1.03% | `igloo` 1/62, `small_viking_villager_house:small_viking_house` 1/73, `simply_houses:taiga_inn` 1/140, `simply_houses:taiga_woodcutter` 1/140, `simply_houses:taiga_forge` 1/198, `simply_houses:taiga_house` 1/198 |
| `terralith:arid_highlands` | 1.03% | **none** |
| `taiga` | 1.00% | `hobbit_house_:hobbit_house` 1/52, `small_viking_villager_house:small_viking_house` 1/54, `simply_houses:taiga_inn` 1/104, `simply_houses:taiga_woodcutter` 1/104, `simply_houses:taiga_forge` 1/147, `simply_houses:taiga_house` 1/147 |
| `terralith:highlands` | 1.00% | **none** |
| `meadow` | 0.97% | `villager_flower_house:flower_house` 1/51, `simply_houses:plains_house` 1/96, `simply_houses:plains_mansion` 1/96 |
| `frozen_peaks` | 0.90% | **none** |
| `old_growth_spruce_taiga` | 0.90% | `hobbit_house_:hobbit_house` 1/52, `small_viking_villager_house:small_viking_house` 1/54, `simply_houses:taiga_inn` 1/104, `simply_houses:taiga_woodcutter` 1/104, `simply_houses:taiga_forge` 1/147, `simply_houses:taiga_house` 1/147 |
| `beach` | 0.87% | `villager_fish_shop:fish_shop` 1/40, `farmers_structures:aquacultures_beach` 1/103 |
| `wooded_badlands` | 0.87% | **none** |
| `terralith:alpine_highlands` | 0.87% | **none** |
| `old_growth_pine_taiga` | 0.73% | `hobbit_house_:hobbit_house` 1/52, `small_viking_villager_house:small_viking_house` 1/54, `simply_houses:taiga_inn` 1/104, `simply_houses:taiga_woodcutter` 1/104, `simply_houses:taiga_forge` 1/147, `simply_houses:taiga_house` 1/147 |
| `terralith:cold_shrubland` | 0.73% | `igloo` 1/33 |
| `terralith:volcanic_peaks` | 0.70% | **none** |
| `mangrove_swamp` | 0.67% | `old_swamp_fisherman_house:fisherman_house` 1/46 |
| `terralith:shield` | 0.67% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `terralith:orchid_swamp` | 0.63% | **none** |
| `windswept_savanna` | 0.60% | **none** |
| `terralith:gravel_beach` | 0.60% | `farmers_structures:aquacultures_beach` 1/91 |
| `terralith:siberian_taiga` | 0.60% | **none** |
| `stony_peaks` | 0.57% | **none** |
| `terralith:temperate_highlands` | 0.57% | **none** |
| `jungle` | 0.53% | `ancient_enchantment_temple:temple` 1/45 |
| `old_growth_birch_forest` | 0.53% | `sawmillhouse:sawmill` 1/36, `farmers_structures:aquacultures_carp` 1/64, `farmers_structures:aquacultures_common` 1/64, `simply_houses:forest_house` 1/131, `simply_houses:forest_stonemason` 1/131, `simply_houses:forest_chapel` 1/186, `simply_houses:forest_fort` 1/186 |
| `terralith:steppe` | 0.53% | **none** |
| `sparse_jungle` | 0.50% | `ancient_enchantment_temple:temple` 1/45 |
| `terralith:hot_shrubland` | 0.50% | **none** |
| `jagged_peaks` | 0.47% | **none** |
| `terralith:sandstone_valley` | 0.47% | **none** |
| `terralith:shrubland` | 0.47% | **none** |
| `eroded_badlands` | 0.43% | `saloon:saloon` 1/27 |
| `terralith:rocky_jungle` | 0.43% | **none** |
| `snowy_slopes` | 0.40% | `igloo` 1/44 |
| `terralith:gravel_desert` | 0.40% | `igloo` 1/33 |
| `terralith:birch_taiga` | 0.37% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `bamboo_jungle` | 0.33% | `ancient_enchantment_temple:temple` 1/45 |
| `terralith:bryce_canyon` | 0.33% | **none** |
| `terralith:desert_canyon` | 0.33% | **none** |
| `terralith:ice_marsh` | 0.33% | `igloo` 1/33 |
| `terralith:sakura_grove` | 0.33% | **none** |
| `terralith:savanna_badlands` | 0.33% | **none** |
| `terralith:scarlet_mountains` | 0.33% | **none** |
| `terralith:snowy_badlands` | 0.33% | `igloo` 1/33 |
| `birch_forest` | 0.30% | `sawmillhouse:sawmill` 1/45, `smallhorsestable:horse_stable` 1/48, `small_villager_greenhouse:small_villager_greenhouse` 1/60, `farmers_structures:aquacultures_carp` 1/79, `farmers_structures:aquacultures_common` 1/79, `simply_houses:forest_house` 1/163, `simply_houses:forest_stonemason` 1/163, `simply_houses:forest_chapel` 1/231, `simply_houses:forest_fort` 1/231 |
| `frozen_river` | 0.27% | `farmers_structures:sushis` 1/49 |
| `terralith:lavender_valley` | 0.27% | **none** |
| `ice_spikes` | 0.23% | **none** |
| `snowy_beach` | 0.23% | `villager_fish_shop:fish_shop` 1/84, `farmers_structures:aquacultures_beach` 1/218 |
| `windswept_hills` | 0.23% | `llama_stable:llama_stable` 1/37, `farmers_structures:cooks` 1/38, `farmers_structures:aquacultures_carp` 1/46, `farmers_structures:aquacultures_common` 1/46 |
| `terralith:red_oasis` | 0.23% | **none** |
| `terralith:stony_spires` | 0.23% | **none** |
| `terralith:warm_river` | 0.23% | **none** |
| `mushroom_fields` | 0.20% | **none** |
| `windswept_gravelly_hills` | 0.20% | `llama_stable:llama_stable` 1/37, `farmers_structures:cooks` 1/38, `farmers_structures:aquacultures_carp` 1/46, `farmers_structures:aquacultures_common` 1/46 |
| `terralith:blooming_plateau` | 0.20% | **none** |
| `terralith:blooming_valley` | 0.20% | **none** |
| `terralith:desert_spires` | 0.20% | **none** |
| `terralith:fractured_savanna` | 0.20% | `farmers_structures:cooks` 1/35, `farmers_structures:aquacultures_carp` 1/41, `farmers_structures:aquacultures_common` 1/41 |
| `terralith:wintry_forest` | 0.20% | `igloo` 1/53, `simply_houses:taiga_inn` 1/121, `simply_houses:taiga_woodcutter` 1/121, `simply_houses:taiga_forge` 1/170, `simply_houses:taiga_house` 1/170 |
| `grove` | 0.17% | `farmers_structures:aquacultures_carp` 1/55, `farmers_structures:aquacultures_common` 1/55, `simply_houses:forest_house` 1/114, `simply_houses:forest_stonemason` 1/114, `simply_houses:forest_chapel` 1/161, `simply_houses:forest_fort` 1/161 |
| `terralith:amethyst_rainforest` | 0.17% | **none** |
| `terralith:haze_mountain` | 0.13% | `farmers_structures:cooks` 1/35, `farmers_structures:aquacultures_carp` 1/41, `farmers_structures:aquacultures_common` 1/41 |
| `terralith:lush_valley` | 0.13% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `terralith:painted_mountains` | 0.13% | **none** |
| `terralith:tropical_jungle` | 0.13% | **none** |
| `flower_forest` | 0.10% | `forest_watchtower:forest_watchtower` 1/39, `sawmillhouse:sawmill` 1/39, `farmers_structures:aquacultures_carp` 1/70, `farmers_structures:aquacultures_common` 1/70, `simply_houses:forest_house` 1/144, `simply_houses:forest_stonemason` 1/144, `simply_houses:forest_chapel` 1/204, `simply_houses:forest_fort` 1/204 |
| `terralith:amethyst_canyon` | 0.10% | **none** |
| `terralith:moonlight_grove` | 0.10% | **none** |
| `terralith:moonlight_valley` | 0.10% | **none** |
| `terralith:windswept_spires` | 0.10% | **none** |
| `terralith:yosemite_cliffs` | 0.10% | **none** |
| `terralith:yosemite_lowlands` | 0.10% | `simply_houses:taiga_inn` 1/94, `simply_houses:taiga_woodcutter` 1/94, `simply_houses:taiga_forge` 1/133, `simply_houses:taiga_house` 1/133 |
| `terralith:alpha_islands` | 0.07% | **none** |
| `terralith:ashen_savanna` | 0.07% | **none** |
| `terralith:cloud_forest` | 0.07% | `farmers_structures:cooks` 1/55, `farmers_structures:aquacultures_carp` 1/65, `farmers_structures:aquacultures_common` 1/65, `simply_houses:forest_house` 1/134, `simply_houses:forest_stonemason` 1/134, `simply_houses:forest_chapel` 1/189, `simply_houses:forest_fort` 1/189 |
| `terralith:lush_desert` | 0.07% | **none** |
| `terralith:mirage_isles` | 0.07% | **none** |
| `windswept_forest` | 0.03% | `sawmillhouse:sawmill` 1/31, `llama_stable:llama_stable` 1/45, `farmers_structures:cooks` 1/47, `farmers_structures:aquacultures_carp` 1/55, `farmers_structures:aquacultures_common` 1/55 |
| `terralith:alpine_grove` | 0.03% | `igloo` 1/50, `farmers_structures:aquacultures_carp` 1/74, `farmers_structures:aquacultures_common` 1/74, `simply_houses:forest_house` 1/153, `simply_houses:forest_stonemason` 1/153, `simply_houses:forest_chapel` 1/216, `simply_houses:forest_fort` 1/216 |
| `terralith:desert_oasis` | 0.03% | **none** |
| `terralith:emerald_peaks` | 0.03% | `igloo` 1/33 |
| `terralith:jungle_mountains` | 0.03% | `farmers_structures:cooks` 1/35, `farmers_structures:aquacultures_carp` 1/41, `farmers_structures:aquacultures_common` 1/41 |
| `terralith:lavender_forest` | 0.03% | **none** |
| `terralith:sakura_valley` | 0.03% | **none** |
| `terralith:skylands_summer` | 0.03% | **none** |
| `terralith:skylands_winter` | 0.03% | **none** |
| `terralith:warped_mesa` | 0.03% | **none** |
| `terralith:wintry_lowlands` | 0.03% | `igloo` 1/33 |
| `deep_dark` | 0.00% | **none** |
| `stony_shore` | 0.00% | **none** |
| `sunflower_plains` | 0.00% | `villager_fishermans_hut:villager_fishermans_hut` 1/72, `animal_barn:barn` 1/74, `villager_forge:villager_forge` 1/74, `smallhorsestable:horse_stable` 1/76, `villager_fletcher_house:villager_fletcher_house` 1/77, `mini_clock_tower:mini_clock_tower` 1/77, `villager_cleric_house:villager_cleric_house` 1/79, `villager_windmill:windmill` 1/79, `villager_cartographer_house:villager_cartographer_house` 1/84, `villager_butcher_house:butcher_house` 1/86, `villager_flower_house:flower_house` 1/89, `villager_farmer_house:villager_farmer_house` 1/93, `small_villager_greenhouse:small_villager_greenhouse` 1/94, `farmers_structures:cooks` 1/106, `farmers_structures:aquacultures_carp` 1/126, `farmers_structures:aquacultures_common` 1/126, `simply_houses:plains_house` 1/167, `simply_houses:plains_mansion` 1/167 |
| `terralith:alpha_islands_winter` | 0.00% | `igloo` 1/33 |
| `terralith:ancient_sands` | 0.00% | **none** |
| `terralith:basalt_cliffs` | 0.00% | **none** |
| `terralith:caldera` | 0.00% | **none** |
| `terralith:cave/frostfire_caves` | 0.00% | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** |
| `terralith:frozen_cliffs` | 0.00% | **none** |
| `terralith:granite_cliffs` | 0.00% | **none** |
| `terralith:rocky_shrubland` | 0.00% | `igloo` 1/33 |
| `terralith:savanna_slopes` | 0.00% | **none** |
| `terralith:shield_clearing` | 0.00% | **none** |
| `terralith:siberian_grove` | 0.00% | `igloo` 1/33 |
| `terralith:skylands_autumn` | 0.00% | **none** |
| `terralith:skylands_spring` | 0.00% | **none** |
| `terralith:snowy_maple_forest` | 0.00% | `igloo` 1/33 |
| `terralith:snowy_shield` | 0.00% | `igloo` 1/33 |
| `terralith:valley_clearing` | 0.00% | **none** |
| `terralith:volcanic_crater` | 0.00% | **none** |
| `terralith:white_cliffs` | 0.00% | **none** |
| `terralith:white_mesa` | 0.00% | **none** |
| `nether_wastes` | 46.83% | `farmers_structures:cooks_nether_2` 1/47 |
| `crimson_forest` | 16.07% | `farmers_structures:cooks_nether_1` 1/38 |
| `warped_forest` | 5.77% | `farmers_structures:cooks_nether_1` 1/38 |

## 8. Pillager by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Pillager |
|---|---|---|
| `deep_ocean` | 8.07% | `illagerwarship:warship` 1/131, `towns_and_towers:pillager_outpost_ocean` 1/155 |
| `deep_lukewarm_ocean` | 7.23% | `illagerwarship:warship` 1/131, `towns_and_towers:pillager_outpost_ocean` 1/155 |
| `deep_cold_ocean` | 6.77% | `illager_viking_longship:illager_longship` 1/117, `illagerwarship:warship` 1/144, `towns_and_towers:pillager_outpost_ocean` 1/171 |
| `ocean` | 4.77% | `illager_viking_longship:illager_longship` 1/106, `illagerwarship:warship` 1/131, `blocky_siege:pillager_ship` 1/171 |
| `terralith:yellowstone` | 4.20% | `pillager_outpost` 1/78, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/79, `towns_and_towers:pillager_outpost_taiga` 1/79, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/245, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/247, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/309, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/348, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/363, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/367, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/367, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/386 |
| `warm_ocean` | 3.73% | `illagerwarship:warship` 1/117, `blocky_siege:pillager_ship` 1/152 |
| `lukewarm_ocean` | 3.17% | `illagerwarship:warship` 1/117, `blocky_siege:pillager_ship` 1/152 |
| `deep_frozen_ocean` | 3.00% | `illager_viking_longship:illager_longship` 1/73, `illagerwarship:warship` 1/90 |
| `cold_ocean` | 2.97% | `illager_viking_longship:illager_longship` 1/102, `illagerwarship:warship` 1/126 |
| `plains` | 2.93% | `illager_siege_weapons:ballista` 1/92, `illager_siege_weapons:battering_ram` 1/92, `illager_siege_weapons:catapult` 1/92, `illager_siege_weapons:reinforced_battering_ram` 1/92, `illager_siege_weapons:siege_tower` 1/92, `illager_siege_weapons:trebuchet` 1/92, `pillager_outpost` 1/102, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/320, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/323, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/404, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/454, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/474, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/479, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/479, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/505 |
| `forest` | 2.87% | `towns_and_towers:pillager_outpost_forest` 1/69, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/199, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/201, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/251, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/282, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/295, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/298, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/298, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/314 |
| `terralith:brushland` | 2.13% | `towns_and_towers:pillager_outpost_savanna` 1/70, `towns_and_towers:exclusives/pillager_outpost_mediterranean` 1/78, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/190, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/191, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/239, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/269, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/281, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/284, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/284, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/299 |
| `savanna` | 1.93% | `illager_siege_weapons:ballista` 1/96, `illager_siege_weapons:battering_ram` 1/96, `illager_siege_weapons:catapult` 1/96, `illager_siege_weapons:reinforced_battering_ram` 1/96, `illager_siege_weapons:siege_tower` 1/96, `illager_siege_weapons:trebuchet` 1/96, `pillager_outpost` 1/106, `towns_and_towers:pillager_outpost_savanna` 1/123, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/335, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/338, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/422, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/475, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/496, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/501, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/501, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/527 |
| `river` | 1.80% | **none** |
| `dark_forest` | 1.67% | `mansion` 1/46, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:glacial_chasm` | 1.60% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `badlands` | 1.50% | `towns_and_towers:pillager_outpost_badlands` 1/66, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/166, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/168, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/210, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/236, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/246, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/249, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/249, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/262 |
| `frozen_ocean` | 1.50% | `illager_viking_longship:illager_longship` 1/73, `illagerwarship:warship` 1/90 |
| `snowy_plains` | 1.37% | `towns_and_towers:pillager_outpost_snowy_plains` 1/101, `illager_siege_weapons:ballista` 1/107, `illager_siege_weapons:battering_ram` 1/107, `illager_siege_weapons:catapult` 1/107, `illager_siege_weapons:reinforced_battering_ram` 1/107, `illager_siege_weapons:siege_tower` 1/107, `illager_siege_weapons:trebuchet` 1/107, `pillager_outpost` 1/118, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/371, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/374, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/468, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/526, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/550, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/555, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/555, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/585 |
| `terralith:rocky_mountains` | 1.33% | `pillager_outpost` 1/64, `towns_and_towers:pillager_outpost_snowy_slopes` 1/68, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/202, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/203, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/254, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/286, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/299, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/302, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/302, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/318 |
| `desert` | 1.20% | `illager_siege_weapons:ballista` 1/107, `illager_siege_weapons:battering_ram` 1/107, `illager_siege_weapons:catapult` 1/107, `illager_siege_weapons:reinforced_battering_ram` 1/107, `illager_siege_weapons:siege_tower` 1/107, `illager_siege_weapons:trebuchet` 1/107, `towns_and_towers:pillager_outpost_desert` 1/113, `pillager_outpost` 1/118, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/372, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/375, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/469, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/527, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/551, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/557, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/557, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/586 |
| `swamp` | 1.07% | `betterwitchhuts:witch_hut` 1/25, `villagesandpillages:village_witch` 1/29, `towns_and_towers:pillager_outpost_swamp` 1/71, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `terralith:forested_highlands` | 1.07% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/87, `towns_and_towers:pillager_outpost_taiga` 1/87, `towns_and_towers:pillager_outpost_snowy_taiga` 1/105, `towns_and_towers:exclusives/pillager_outpost_swedish` 1/105, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/270, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/272, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/340, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/383, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/400, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/404, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/404, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/425 |
| `savanna_plateau` | 1.03% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `snowy_taiga` | 1.03% | `towns_and_towers:pillager_outpost_snowy_taiga` 1/69, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `terralith:arid_highlands` | 1.03% | `towns_and_towers:pillager_outpost_savanna` 1/71, `towns_and_towers:pillager_outpost_badlands` 1/77, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/193, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/194, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/243, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/273, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/285, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/288, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/288, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/304 |
| `taiga` | 1.00% | `illager_siege_weapons:ballista` 1/97, `illager_siege_weapons:battering_ram` 1/97, `illager_siege_weapons:catapult` 1/97, `illager_siege_weapons:reinforced_battering_ram` 1/97, `illager_siege_weapons:siege_tower` 1/97, `illager_siege_weapons:trebuchet` 1/97, `pillager_outpost` 1/106, `towns_and_towers:pillager_outpost_taiga` 1/109, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/336, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/339, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/423, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/476, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/497, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/503, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/503, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/529 |
| `terralith:highlands` | 1.00% | `towns_and_towers:exclusives/pillager_outpost_tudor` 1/71, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `meadow` | 0.97% | `towns_and_towers:pillager_outpost_meadow` 1/68, `pillager_outpost` 1/78, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/245, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/247, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/308, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/347, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/362, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/366, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/366, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/385 |
| `frozen_peaks` | 0.90% | `pillager_outpost` 1/51, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/160, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/161, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/202, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/227, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/237, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/252 |
| `old_growth_spruce_taiga` | 0.90% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/59, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/183, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/184, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/230, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/259, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/270, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/273, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/273, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/288 |
| `beach` | 0.87% | `towns_and_towers:pillager_outpost_beach` 1/55, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/207, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/209, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/261, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/307, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/327 |
| `wooded_badlands` | 0.87% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/45, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/165, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/166, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/208, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/234, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/244, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/247, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/247, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/260 |
| `terralith:alpine_highlands` | 0.87% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/67, `towns_and_towers:pillager_outpost_taiga` 1/67, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/208, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/210, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/295, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/309, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/312, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/312, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/328 |
| `old_growth_pine_taiga` | 0.73% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/59, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/183, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/184, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/230, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/259, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/270, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/273, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/273, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/288 |
| `terralith:cold_shrubland` | 0.73% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:volcanic_peaks` | 0.70% | `pillager_outpost` 1/51, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/160, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/161, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/202, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/227, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/237, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/252 |
| `mangrove_swamp` | 0.67% | `betterwitchhuts:witch_hut` 1/23, `villagesandpillages:village_witch` 1/26, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:shield` | 0.67% | `pillager_outpost` 1/95, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/96, `towns_and_towers:pillager_outpost_taiga` 1/97, `towns_and_towers:pillager_outpost_snowy_taiga` 1/117, `towns_and_towers:exclusives/pillager_outpost_swedish` 1/117, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/299, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/302, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/377, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/424, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/443, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/448, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/448, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/472 |
| `terralith:orchid_swamp` | 0.63% | `betterwitchhuts:witch_hut` 1/25, `villagesandpillages:village_witch` 1/29, `towns_and_towers:pillager_outpost_swamp` 1/71, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `windswept_savanna` | 0.60% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:gravel_beach` | 0.60% | `towns_and_towers:pillager_outpost_beach` 1/55, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/207, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/209, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/261, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/307, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/327 |
| `terralith:siberian_taiga` | 0.60% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/67, `towns_and_towers:pillager_outpost_taiga` 1/67, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/208, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/210, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/295, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/309, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/312, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/312, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/328 |
| `stony_peaks` | 0.57% | `pillager_outpost` 1/51, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/160, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/161, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/202, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/227, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/237, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/252 |
| `terralith:temperate_highlands` | 0.57% | `towns_and_towers:pillager_outpost_forest` 1/91, `towns_and_towers:pillager_outpost_birch_forest` 1/91, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/265, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/332, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/373, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/390, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/394, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/394, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/415 |
| `jungle` | 0.53% | `towns_and_towers:pillager_outpost_jungle` 1/63, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/183, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/185, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/231, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/260, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/271, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/274, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/274, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/288 |
| `old_growth_birch_forest` | 0.53% | `towns_and_towers:pillager_outpost_birch_forest` 1/68, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/197, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/198, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/248, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/279, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/291, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/310 |
| `terralith:steppe` | 0.53% | `towns_and_towers:pillager_outpost_sunflower_plains` 1/71, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `sparse_jungle` | 0.50% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/46, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/184, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/185, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/232, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/260, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/272, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/275, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/275, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/289 |
| `terralith:hot_shrubland` | 0.50% | `towns_and_towers:pillager_outpost_savanna` 1/70, `towns_and_towers:exclusives/pillager_outpost_mediterranean` 1/78, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/190, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/191, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/239, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/269, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/281, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/284, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/284, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/299 |
| `jagged_peaks` | 0.47% | `pillager_outpost` 1/51, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/160, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/161, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/202, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/227, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/237, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/252 |
| `terralith:sandstone_valley` | 0.47% | `towns_and_towers:pillager_outpost_desert` 1/64, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/212, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/213, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/267, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/300, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/313, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/333 |
| `terralith:shrubland` | 0.47% | `towns_and_towers:pillager_outpost_savanna` 1/70, `towns_and_towers:exclusives/pillager_outpost_mediterranean` 1/78, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/190, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/191, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/239, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/269, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/281, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/284, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/284, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/299 |
| `eroded_badlands` | 0.43% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:rocky_jungle` | 0.43% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/60, `towns_and_towers:pillager_outpost_jungle` 1/83, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/242, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/243, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/342, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/358, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/380 |
| `snowy_slopes` | 0.40% | `pillager_outpost` 1/64, `towns_and_towers:pillager_outpost_snowy_slopes` 1/68, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/202, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/203, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/254, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/286, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/299, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/302, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/302, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/318 |
| `terralith:gravel_desert` | 0.40% | `towns_and_towers:pillager_outpost_snowy_plains` 1/57, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/210, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/212, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/265, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/298, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/311, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/314, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/314, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/331 |
| `terralith:birch_taiga` | 0.37% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/87, `towns_and_towers:pillager_outpost_taiga` 1/88, `towns_and_towers:pillager_outpost_birch_forest` 1/94, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/271, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/273, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/341, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/383, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/400, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/405, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/405, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/426 |
| `bamboo_jungle` | 0.33% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:bryce_canyon` | 0.33% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/58, `towns_and_towers:pillager_outpost_badlands` 1/85, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/214, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/216, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/270, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/338 |
| `terralith:desert_canyon` | 0.33% | `towns_and_towers:pillager_outpost_desert` 1/64, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/212, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/213, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/267, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/300, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/313, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/333 |
| `terralith:ice_marsh` | 0.33% | `betterwitchhuts:witch_hut` 1/25, `villagesandpillages:village_witch` 1/29, `towns_and_towers:pillager_outpost_swamp` 1/71, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `terralith:sakura_grove` | 0.33% | `mansion` 1/46, `towns_and_towers:pillager_outpost_flower_forest` 1/70, `pillager_outpost` 1/91, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/287, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/289, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/362, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/407, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/425, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/430, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/430, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/452 |
| `terralith:savanna_badlands` | 0.33% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/64, `towns_and_towers:pillager_outpost_savanna` 1/86, `towns_and_towers:pillager_outpost_badlands` 1/94, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/235, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/237, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/297, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/334, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/348, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/352, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/352, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/371 |
| `terralith:scarlet_mountains` | 0.33% | `pillager_outpost` 1/78, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/79, `towns_and_towers:pillager_outpost_taiga` 1/79, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/245, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/247, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/309, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/348, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/363, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/367, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/367, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/386 |
| `terralith:snowy_badlands` | 0.33% | `towns_and_towers:pillager_outpost_badlands` 1/66, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/166, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/168, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/210, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/236, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/246, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/249, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/249, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/262 |
| `birch_forest` | 0.30% | `towns_and_towers:pillager_outpost_birch_forest` 1/68, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/197, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/198, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/248, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/279, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/291, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/310 |
| `frozen_river` | 0.27% | **none** |
| `terralith:lavender_valley` | 0.27% | `towns_and_towers:pillager_outpost_flower_forest` 1/62, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/256, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/258, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/323, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/363, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/380, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/404 |
| `ice_spikes` | 0.23% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `snowy_beach` | 0.23% | `towns_and_towers:pillager_outpost_snowy_beach` 1/55, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/207, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/209, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/261, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/307, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/327 |
| `windswept_hills` | 0.23% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:red_oasis` | 0.23% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/58, `towns_and_towers:pillager_outpost_badlands` 1/85, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/214, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/216, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/270, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/338 |
| `terralith:stony_spires` | 0.23% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:warm_river` | 0.23% | **none** |
| `mushroom_fields` | 0.20% | `towns_and_towers:pillager_outpost_mushroom_fields` 1/71, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `windswept_gravelly_hills` | 0.20% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:blooming_plateau` | 0.20% | `towns_and_towers:pillager_outpost_meadow` 1/58, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/208, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/210, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/262, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/295, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/308, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/311, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/311, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/327 |
| `terralith:blooming_valley` | 0.20% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:desert_spires` | 0.20% | `towns_and_towers:pillager_outpost_desert` 1/64, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/212, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/213, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/267, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/300, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/313, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/333 |
| `terralith:fractured_savanna` | 0.20% | `towns_and_towers:pillager_outpost_savanna` 1/50, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/136, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/137, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/171, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/193, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/201, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/203, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/203, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/214 |
| `terralith:wintry_forest` | 0.20% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/83, `towns_and_towers:pillager_outpost_taiga` 1/83, `towns_and_towers:pillager_outpost_snowy_taiga` 1/100, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/257, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/259, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/324, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/364, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/380, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/405 |
| `grove` | 0.17% | `towns_and_towers:pillager_outpost_grove` 1/79, `pillager_outpost` 1/80, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/254, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/256, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/320, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/360, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/376, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/380, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/380, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/400 |
| `terralith:amethyst_rainforest` | 0.17% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/60, `towns_and_towers:pillager_outpost_jungle` 1/83, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/242, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/243, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/342, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/358, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/380 |
| `terralith:haze_mountain` | 0.13% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:lush_valley` | 0.13% | `pillager_outpost` 1/78, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/79, `towns_and_towers:pillager_outpost_taiga` 1/79, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/245, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/247, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/309, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/348, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/363, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/367, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/367, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/386 |
| `terralith:painted_mountains` | 0.13% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/58, `towns_and_towers:pillager_outpost_badlands` 1/85, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/214, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/216, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/270, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/338 |
| `terralith:tropical_jungle` | 0.13% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/60, `towns_and_towers:pillager_outpost_jungle` 1/83, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/242, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/243, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/342, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/358, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/380 |
| `flower_forest` | 0.10% | `towns_and_towers:pillager_outpost_flower_forest` 1/62, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/256, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/258, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/323, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/363, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/380, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/404 |
| `terralith:amethyst_canyon` | 0.10% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/60, `towns_and_towers:pillager_outpost_jungle` 1/83, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/242, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/243, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/342, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/358, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/380 |
| `terralith:moonlight_grove` | 0.10% | `towns_and_towers:pillager_outpost_flower_forest` 1/62, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/256, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/258, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/323, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/363, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/380, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/404 |
| `terralith:moonlight_valley` | 0.10% | `towns_and_towers:pillager_outpost_flower_forest` 1/62, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/256, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/258, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/323, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/363, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/380, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/404 |
| `terralith:windswept_spires` | 0.10% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:yosemite_cliffs` | 0.10% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:yosemite_lowlands` | 0.10% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/67, `towns_and_towers:pillager_outpost_taiga` 1/67, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/208, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/210, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/295, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/309, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/312, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/312, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/328 |
| `terralith:alpha_islands` | 0.07% | `towns_and_towers:exclusives/pillager_outpost_classic` 1/83, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/164, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/166, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/207, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/233, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/243, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/246, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/246, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/259 |
| `terralith:ashen_savanna` | 0.07% | `towns_and_towers:pillager_outpost_savanna` 1/50, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/136, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/137, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/171, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/193, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/201, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/203, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/203, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/214 |
| `terralith:cloud_forest` | 0.07% | `towns_and_towers:pillager_outpost_flower_forest` 1/77, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/102, `towns_and_towers:pillager_outpost_taiga` 1/102, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/319, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/399, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/449, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/469, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/474, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/474, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/499 |
| `terralith:lush_desert` | 0.07% | `towns_and_towers:pillager_outpost_desert` 1/64, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/212, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/213, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/267, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/300, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/313, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/333 |
| `terralith:mirage_isles` | 0.07% | `towns_and_towers:pillager_outpost_mushroom_fields` 1/71, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `windswept_forest` | 0.03% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:alpine_grove` | 0.03% | `towns_and_towers:pillager_outpost_grove` 1/68, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/219, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/220, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/275, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/323, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/327, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/327, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/344 |
| `terralith:desert_oasis` | 0.03% | `towns_and_towers:exclusives/pillager_outpost_oriental` 1/66, `towns_and_towers:pillager_outpost_desert` 1/74, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/244, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/246, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/307, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/346, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/365, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/365, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/384 |
| `terralith:emerald_peaks` | 0.03% | `pillager_outpost` 1/51, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/160, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/161, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/202, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/227, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/237, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/252 |
| `terralith:jungle_mountains` | 0.03% | `towns_and_towers:pillager_outpost_sparse_jungle` 1/60, `towns_and_towers:pillager_outpost_jungle` 1/83, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/242, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/243, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/342, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/358, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/380 |
| `terralith:lavender_forest` | 0.03% | `towns_and_towers:pillager_outpost_flower_forest` 1/70, `pillager_outpost` 1/91, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/287, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/289, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/362, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/407, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/425, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/430, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/430, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/452 |
| `terralith:sakura_valley` | 0.03% | `mansion` 1/46, `towns_and_towers:pillager_outpost_flower_forest` 1/70, `pillager_outpost` 1/91, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/287, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/289, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/362, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/407, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/425, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/430, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/430, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/452 |
| `terralith:skylands_summer` | 0.03% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:skylands_winter` | 0.03% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:warped_mesa` | 0.03% | `towns_and_towers:exclusives/pillager_outpost_oriental` 1/66, `towns_and_towers:pillager_outpost_wooded_badlands` 1/67, `towns_and_towers:pillager_outpost_badlands` 1/98, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/246, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/248, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/349, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/364, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/368, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/368, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/388 |
| `terralith:wintry_lowlands` | 0.03% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/83, `towns_and_towers:pillager_outpost_taiga` 1/83, `towns_and_towers:pillager_outpost_snowy_taiga` 1/100, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/257, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/259, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/324, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/364, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/380, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/384, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/405 |
| `deep_dark` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `stony_shore` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `sunflower_plains` | 0.00% | `towns_and_towers:pillager_outpost_sunflower_plains` 1/71, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `terralith:alpha_islands_winter` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:ancient_sands` | 0.00% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/58, `towns_and_towers:pillager_outpost_badlands` 1/85, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/214, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/216, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/270, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/338 |
| `terralith:basalt_cliffs` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:caldera` | 0.00% | `pillager_outpost` 1/51, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/160, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/161, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/202, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/227, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/237, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/240, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/252 |
| `terralith:cave/frostfire_caves` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:cave/mantle_caves` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:cave/tuff_caves` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:frozen_cliffs` | 0.00% | `towns_and_towers:pillager_outpost_beach` 1/55, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/207, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/209, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/261, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/307, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/310, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/327 |
| `terralith:granite_cliffs` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:rocky_shrubland` | 0.00% | `towns_and_towers:pillager_outpost_snowy_plains` 1/66, `towns_and_towers:pillager_outpost_snowy_slopes` 1/82, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/243, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/245, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/307, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/345, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/360, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/364, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/364, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/383 |
| `terralith:savanna_slopes` | 0.00% | `towns_and_towers:pillager_outpost_savanna` 1/50, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/136, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/137, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/171, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/193, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/201, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/203, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/203, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/214 |
| `terralith:shield_clearing` | 0.00% | `towns_and_towers:pillager_outpost_taiga` 1/66, `towns_and_towers:pillager_outpost_snowy_taiga` 1/80, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/204, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/206, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/257, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/289, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/302, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/305, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/305, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/321 |
| `terralith:siberian_grove` | 0.00% | `towns_and_towers:pillager_outpost_grove` 1/89, `towns_and_towers:pillager_outpost_old_growth_taiga` 1/92, `towns_and_towers:pillager_outpost_taiga` 1/93, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/287, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/289, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/361, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/406, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/424, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/429, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/429, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/452 |
| `terralith:skylands_autumn` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:skylands_spring` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:snowy_maple_forest` | 0.00% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/87, `towns_and_towers:pillager_outpost_taiga` 1/87, `towns_and_towers:pillager_outpost_snowy_taiga` 1/105, `towns_and_towers:exclusives/pillager_outpost_swedish` 1/105, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/270, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/272, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/340, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/383, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/400, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/404, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/404, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/425 |
| `terralith:snowy_shield` | 0.00% | `towns_and_towers:pillager_outpost_snowy_taiga` 1/69, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/178, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/179, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/224, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/252, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/266, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/280 |
| `terralith:valley_clearing` | 0.00% | `towns_and_towers:pillager_outpost_old_growth_taiga` 1/67, `towns_and_towers:pillager_outpost_taiga` 1/67, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/208, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/210, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/263, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/295, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/309, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/312, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/312, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/328 |
| `terralith:volcanic_crater` | 0.00% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:white_cliffs` | 0.00% | `towns_and_towers:pillager_outpost_birch_forest` 1/68, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/197, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/198, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/248, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/279, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/291, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/294, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/310 |
| `terralith:white_mesa` | 0.00% | `towns_and_towers:pillager_outpost_wooded_badlands` 1/58, `towns_and_towers:pillager_outpost_badlands` 1/85, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/214, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/216, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/270, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/304, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/317, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/321, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/338 |
| `terralith:cave/deep_caves` | 12.86% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `lush_caves` | 11.76% | `terralith:underground/witch_hut` 1/7.8, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:cave/andesite_caves` | 6.09% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `dripstone_caves` | 5.08% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:cave/infested_caves` | 3.07% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:cave/granite_caves` | 2.87% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:cave/fungal_caves` | 1.92% | `terralith:underground/witch_hut` 1/7.9, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:cave/thermal_caves` | 1.30% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:cave/diorite_caves` | 1.21% | `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |
| `terralith:cave/underground_jungle` | 1.15% | `terralith:underground/witch_hut` 1/7.2, `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` 1/95, `mysticrift_pillagers_towers_aris:jungle_pillager_tower` 1/96, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` 1/120, `mysticrift_pillagers_towers_aris:desert_pillager_tower` 1/135, `mysticrift_pillagers_towers_aris:end_pillager_tower` 1/141, `mysticrift_pillagers_towers_aris:acacia_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:cherry_pillager_tower` 1/142, `mysticrift_pillagers_towers_aris:birch_pillager_tower` 1/149 |

## 9. Surface dungeons by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Surface dungeons |
|---|---|---|
| `deep_ocean` | 8.07% | **none** |
| `deep_lukewarm_ocean` | 7.23% | **none** |
| `deep_cold_ocean` | 6.77% | **none** |
| `ocean` | 4.77% | **none** |
| `terralith:yellowstone` | 4.20% | **none** |
| `warm_ocean` | 3.73% | **none** |
| `lukewarm_ocean` | 3.17% | **none** |
| `deep_frozen_ocean` | 3.00% | **none** |
| `cold_ocean` | 2.97% | **none** |
| `plains` | 2.93% | **none** |
| `forest` | 2.87% | **none** |
| `terralith:brushland` | 2.13% | **none** |
| `savanna` | 1.93% | **none** |
| `river` | 1.80% | **none** |
| `dark_forest` | 1.67% | **none** |
| `terralith:glacial_chasm` | 1.60% | **none** |
| `badlands` | 1.50% | `philipsruins:badlands_start_dungeon` 1/25 |
| `frozen_ocean` | 1.50% | **none** |
| `snowy_plains` | 1.37% | **none** |
| `terralith:rocky_mountains` | 1.33% | **none** |
| `desert` | 1.20% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `swamp` | 1.07% | `betterwitchhuts:witch_circle` 1/37 |
| `terralith:forested_highlands` | 1.07% | **none** |
| `savanna_plateau` | 1.03% | **none** |
| `snowy_taiga` | 1.03% | **none** |
| `terralith:arid_highlands` | 1.03% | **none** |
| `taiga` | 1.00% | **none** |
| `terralith:highlands` | 1.00% | **none** |
| `meadow` | 0.97% | **none** |
| `frozen_peaks` | 0.90% | **none** |
| `old_growth_spruce_taiga` | 0.90% | **none** |
| `beach` | 0.87% | **none** |
| `wooded_badlands` | 0.87% | `philipsruins:badlands_start_dungeon` 1/25 |
| `terralith:alpine_highlands` | 0.87% | **none** |
| `old_growth_pine_taiga` | 0.73% | **none** |
| `terralith:cold_shrubland` | 0.73% | **none** |
| `terralith:volcanic_peaks` | 0.70% | **none** |
| `mangrove_swamp` | 0.67% | **none** |
| `terralith:shield` | 0.67% | **none** |
| `terralith:orchid_swamp` | 0.63% | `betterwitchhuts:witch_circle` 1/37 |
| `windswept_savanna` | 0.60% | **none** |
| `terralith:gravel_beach` | 0.60% | **none** |
| `terralith:siberian_taiga` | 0.60% | **none** |
| `stony_peaks` | 0.57% | **none** |
| `terralith:temperate_highlands` | 0.57% | **none** |
| `jungle` | 0.53% | `betterjungletemples:jungle_temple` 1/18 |
| `old_growth_birch_forest` | 0.53% | **none** |
| `terralith:steppe` | 0.53% | **none** |
| `sparse_jungle` | 0.50% | **none** |
| `terralith:hot_shrubland` | 0.50% | **none** |
| `jagged_peaks` | 0.47% | **none** |
| `terralith:sandstone_valley` | 0.47% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:shrubland` | 0.47% | **none** |
| `eroded_badlands` | 0.43% | `philipsruins:badlands_start_dungeon` 1/25 |
| `terralith:rocky_jungle` | 0.43% | `betterjungletemples:jungle_temple` 1/18 |
| `snowy_slopes` | 0.40% | **none** |
| `terralith:gravel_desert` | 0.40% | **none** |
| `terralith:birch_taiga` | 0.37% | **none** |
| `bamboo_jungle` | 0.33% | `betterjungletemples:jungle_temple` 1/18 |
| `terralith:bryce_canyon` | 0.33% | **none** |
| `terralith:desert_canyon` | 0.33% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:ice_marsh` | 0.33% | `betterwitchhuts:witch_circle` 1/37 |
| `terralith:sakura_grove` | 0.33% | **none** |
| `terralith:savanna_badlands` | 0.33% | **none** |
| `terralith:scarlet_mountains` | 0.33% | **none** |
| `terralith:snowy_badlands` | 0.33% | **none** |
| `birch_forest` | 0.30% | **none** |
| `frozen_river` | 0.27% | **none** |
| `terralith:lavender_valley` | 0.27% | **none** |
| `ice_spikes` | 0.23% | `terralith:spire` 1/25 |
| `snowy_beach` | 0.23% | **none** |
| `windswept_hills` | 0.23% | **none** |
| `terralith:red_oasis` | 0.23% | **none** |
| `terralith:stony_spires` | 0.23% | **none** |
| `terralith:warm_river` | 0.23% | **none** |
| `mushroom_fields` | 0.20% | **none** |
| `windswept_gravelly_hills` | 0.20% | **none** |
| `terralith:blooming_plateau` | 0.20% | **none** |
| `terralith:blooming_valley` | 0.20% | **none** |
| `terralith:desert_spires` | 0.20% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:fractured_savanna` | 0.20% | **none** |
| `terralith:wintry_forest` | 0.20% | **none** |
| `grove` | 0.17% | **none** |
| `terralith:amethyst_rainforest` | 0.17% | `betterjungletemples:jungle_temple` 1/18 |
| `terralith:haze_mountain` | 0.13% | **none** |
| `terralith:lush_valley` | 0.13% | **none** |
| `terralith:painted_mountains` | 0.13% | **none** |
| `terralith:tropical_jungle` | 0.13% | `betterjungletemples:jungle_temple` 1/18 |
| `flower_forest` | 0.10% | **none** |
| `terralith:amethyst_canyon` | 0.10% | `betterjungletemples:jungle_temple` 1/18 |
| `terralith:moonlight_grove` | 0.10% | **none** |
| `terralith:moonlight_valley` | 0.10% | **none** |
| `terralith:windswept_spires` | 0.10% | **none** |
| `terralith:yosemite_cliffs` | 0.10% | **none** |
| `terralith:yosemite_lowlands` | 0.10% | **none** |
| `terralith:alpha_islands` | 0.07% | **none** |
| `terralith:ashen_savanna` | 0.07% | **none** |
| `terralith:cloud_forest` | 0.07% | **none** |
| `terralith:lush_desert` | 0.07% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:mirage_isles` | 0.07% | **none** |
| `windswept_forest` | 0.03% | **none** |
| `terralith:alpine_grove` | 0.03% | **none** |
| `terralith:desert_oasis` | 0.03% | `betterdeserttemples:desert_temple` 1/26, `towns_and_towers:mimic_desert` 1/28 |
| `terralith:emerald_peaks` | 0.03% | **none** |
| `terralith:jungle_mountains` | 0.03% | `betterjungletemples:jungle_temple` 1/18 |
| `terralith:lavender_forest` | 0.03% | **none** |
| `terralith:sakura_valley` | 0.03% | **none** |
| `terralith:skylands_summer` | 0.03% | **none** |
| `terralith:skylands_winter` | 0.03% | **none** |
| `terralith:warped_mesa` | 0.03% | **none** |
| `terralith:wintry_lowlands` | 0.03% | `terralith:spire` 1/25 |
| `deep_dark` | 0.00% | **none** |
| `stony_shore` | 0.00% | **none** |
| `sunflower_plains` | 0.00% | **none** |
| `terralith:alpha_islands_winter` | 0.00% | **none** |
| `terralith:ancient_sands` | 0.00% | **none** |
| `terralith:basalt_cliffs` | 0.00% | **none** |
| `terralith:caldera` | 0.00% | **none** |
| `terralith:cave/frostfire_caves` | 0.00% | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** |
| `terralith:frozen_cliffs` | 0.00% | **none** |
| `terralith:granite_cliffs` | 0.00% | **none** |
| `terralith:rocky_shrubland` | 0.00% | **none** |
| `terralith:savanna_slopes` | 0.00% | **none** |
| `terralith:shield_clearing` | 0.00% | **none** |
| `terralith:siberian_grove` | 0.00% | **none** |
| `terralith:skylands_autumn` | 0.00% | **none** |
| `terralith:skylands_spring` | 0.00% | **none** |
| `terralith:snowy_maple_forest` | 0.00% | **none** |
| `terralith:snowy_shield` | 0.00% | **none** |
| `terralith:valley_clearing` | 0.00% | **none** |
| `terralith:volcanic_crater` | 0.00% | **none** |
| `terralith:white_cliffs` | 0.00% | **none** |
| `terralith:white_mesa` | 0.00% | **none** |

## 10. Ruins by biome

Every overworld surface biome (and any other biome that has one), biggest first; each structure with its own rate there.

| Biome | Area | Ruins |
|---|---|---|
| `deep_ocean` | 8.07% | **none** |
| `deep_lukewarm_ocean` | 7.23% | **none** |
| `deep_cold_ocean` | 6.77% | **none** |
| `ocean` | 4.77% | **none** |
| `terralith:yellowstone` | 4.20% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `warm_ocean` | 3.73% | **none** |
| `lukewarm_ocean` | 3.17% | **none** |
| `deep_frozen_ocean` | 3.00% | **none** |
| `cold_ocean` | 2.97% | **none** |
| `plains` | 2.93% | `philipsruins:ancient_towers` 1/19, `philipsruins:ancient_ruins` 1/24, `philipsruins:field_stone_ruins_rocks` 1/27, `philipsruins:field_stone_ruins` 1/32, `philipsruins:level_one_ruins` 1/32, `philipsruins:level_three_ruins` 1/36, `philipsruins:lost_soul_city` 1/38, `watchtower_building:abandoned_watchtower` 1/47, `philipsruins:level_two_ruins` 1/48, `philipsruins:level_two_ruins_pool` 1/48, `forgotten_church:forgotten_church` 1/54, `philipsruins:start_nether_ruin` 1/133 |
| `forest` | 2.87% | `philipsruins:ancient_towers` 1/21, `philipsruins:pumpkin_ruins` 1/26, `philipsruins:ancient_ruins` 1/27, `philipsruins:field_stone_ruins_rocks` 1/30, `philipsruins:lost_soul_city` 1/38, `forest_ruins:ruin_1` 1/46, `forest_ruins:ruin_2` 1/46, `forest_ruins:ruin_3` 1/46, `forest_ruins:ruin_4` 1/46, `forest_ruins:ruin_5` 1/46, `forest_ruins:ruin_6` 1/46, `forest_ruins:ruin_7` 1/46, `watchtower_building:abandoned_watchtower` 1/52, `forgotten_church:forgotten_church` 1/60 |
| `terralith:brushland` | 2.13% | **none** |
| `savanna` | 1.93% | `philipsruins:ancient_towers` 1/16, `philipsruins:ancient_ruins` 1/20, `philipsruins:field_stone_ruins_rocks` 1/23, `philipsruins:lost_soul_city` 1/38 |
| `river` | 1.80% | **none** |
| `dark_forest` | 1.67% | `philipsruins:ancient_towers` 1/22, `philipsruins:pumpkin_ruins` 1/28, `philipsruins:ancient_ruins` 1/29, `philipsruins:field_stone_ruins_rocks` 1/32, `philipsruins:lost_soul_city` 1/38, `philipsruins:field_stone_ruins` 1/38, `philipsruins:level_one_ruins` 1/38, `philipsruins:level_three_ruins` 1/44, `forest_ruins:ruin_1` 1/49, `forest_ruins:ruin_2` 1/49, `forest_ruins:ruin_3` 1/49, `forest_ruins:ruin_4` 1/49, `forest_ruins:ruin_5` 1/49, `forest_ruins:ruin_6` 1/49, `forest_ruins:ruin_7` 1/49, `philipsruins:level_two_ruins` 1/58, `philipsruins:level_two_ruins_pool` 1/58, `philipsruins:start_nether_ruin` 1/159 |
| `terralith:glacial_chasm` | 1.60% | `terralith:glacial_hut` 1/16 |
| `badlands` | 1.50% | `philipsruins:badlands_structures` 1/16 |
| `frozen_ocean` | 1.50% | **none** |
| `snowy_plains` | 1.37% | `philipsruins:ancient_towers` 1/16, `philipsruins:ancient_ruins` 1/20, `philipsruins:field_stone_ruins_rocks` 1/23, `philipsruins:lost_soul_city` 1/38 |
| `terralith:rocky_mountains` | 1.33% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `desert` | 1.20% | `philipsruins:desert_structures` 1/16 |
| `swamp` | 1.07% | `philipsruins:ancient_towers` 1/19, `philipsruins:pumpkin_ruins` 1/24, `philipsruins:ancient_ruins` 1/25, `philipsruins:field_stone_ruins_rocks` 1/28, `philipsruins:field_stone_ruins` 1/33, `philipsruins:level_one_ruins` 1/33, `philipsruins:level_three_ruins` 1/38, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/50, `philipsruins:level_two_ruins_pool` 1/50, `philipsruins:start_nether_ruin` 1/137 |
| `terralith:forested_highlands` | 1.07% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `savanna_plateau` | 1.03% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `snowy_taiga` | 1.03% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:arid_highlands` | 1.03% | **none** |
| `taiga` | 1.00% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/22, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38, `watchtower_building:abandoned_watchtower` 1/44, `forgotten_church:forgotten_church` 1/51 |
| `terralith:highlands` | 1.00% | **none** |
| `meadow` | 0.97% | `philipsruins:ancient_towers` 1/18, `philipsruins:ancient_ruins` 1/23, `philipsruins:field_stone_ruins_rocks` 1/26, `philipsruins:field_stone_ruins` 1/31, `philipsruins:level_one_ruins` 1/31, `philipsruins:level_three_ruins` 1/35, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/47, `philipsruins:level_two_ruins_pool` 1/47, `philipsruins:start_nether_ruin` 1/129 |
| `frozen_peaks` | 0.90% | `philipsruins:ancient_towers` 1/15, `philipsruins:ancient_ruins` 1/20, `philipsruins:field_stone_ruins` 1/26, `philipsruins:level_one_ruins` 1/26, `philipsruins:level_three_ruins` 1/30, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/40, `philipsruins:level_two_ruins_pool` 1/40, `philipsruins:start_nether_ruin` 1/109 |
| `old_growth_spruce_taiga` | 0.90% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/22, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38, `forgotten_church:forgotten_church` 1/50 |
| `beach` | 0.87% | **none** |
| `wooded_badlands` | 0.87% | `philipsruins:badlands_structures` 1/16 |
| `terralith:alpine_highlands` | 0.87% | **none** |
| `old_growth_pine_taiga` | 0.73% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/22, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38, `forgotten_church:forgotten_church` 1/50 |
| `terralith:cold_shrubland` | 0.73% | **none** |
| `terralith:volcanic_peaks` | 0.70% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `mangrove_swamp` | 0.67% | `philipsruins:ancient_towers` 1/22, `philipsruins:pumpkin_ruins` 1/28, `philipsruins:ancient_ruins` 1/29, `philipsruins:field_stone_ruins_rocks` 1/32, `philipsruins:lost_soul_city` 1/38, `philipsruins:field_stone_ruins` 1/38, `philipsruins:level_one_ruins` 1/38, `philipsruins:level_three_ruins` 1/43, `philipsruins:level_two_ruins` 1/58, `philipsruins:level_two_ruins_pool` 1/58, `philipsruins:start_nether_ruin` 1/158, `philipsruins:rare_ruin` 1/177 |
| `terralith:shield` | 0.67% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:orchid_swamp` | 0.63% | **none** |
| `windswept_savanna` | 0.60% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:gravel_beach` | 0.60% | **none** |
| `terralith:siberian_taiga` | 0.60% | **none** |
| `stony_peaks` | 0.57% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:temperate_highlands` | 0.57% | **none** |
| `jungle` | 0.53% | `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:field_stone_ruins` 1/30, `philipsruins:level_one_ruins` 1/30, `philipsruins:level_three_ruins` 1/34, `philipsruins:lost_soul_city` 1/38, `philipsruins:level_two_ruins` 1/45, `philipsruins:level_two_ruins_pool` 1/45, `philipsruins:start_nether_ruin` 1/123, `philipsruins:rare_ruin` 1/138 |
| `old_growth_birch_forest` | 0.53% | `philipsruins:ancient_towers` 1/20, `philipsruins:pumpkin_ruins` 1/26, `philipsruins:ancient_ruins` 1/26, `philipsruins:field_stone_ruins_rocks` 1/30, `philipsruins:lost_soul_city` 1/38, `forest_ruins:ruin_1` 1/45, `forest_ruins:ruin_2` 1/45, `forest_ruins:ruin_3` 1/45, `forest_ruins:ruin_4` 1/45, `forest_ruins:ruin_5` 1/45, `forest_ruins:ruin_6` 1/45, `forest_ruins:ruin_7` 1/45 |
| `terralith:steppe` | 0.53% | **none** |
| `sparse_jungle` | 0.50% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `terralith:hot_shrubland` | 0.50% | **none** |
| `jagged_peaks` | 0.47% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:sandstone_valley` | 0.47% | `philipsruins:desert_structures` 1/16 |
| `terralith:shrubland` | 0.47% | **none** |
| `eroded_badlands` | 0.43% | `philipsruins:badlands_structures` 1/16 |
| `terralith:rocky_jungle` | 0.43% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `snowy_slopes` | 0.40% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:gravel_desert` | 0.40% | **none** |
| `terralith:birch_taiga` | 0.37% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `bamboo_jungle` | 0.33% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `terralith:bryce_canyon` | 0.33% | **none** |
| `terralith:desert_canyon` | 0.33% | `philipsruins:desert_structures` 1/16 |
| `terralith:ice_marsh` | 0.33% | **none** |
| `terralith:sakura_grove` | 0.33% | **none** |
| `terralith:savanna_badlands` | 0.33% | **none** |
| `terralith:scarlet_mountains` | 0.33% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:snowy_badlands` | 0.33% | **none** |
| `birch_forest` | 0.30% | `philipsruins:ancient_towers` 1/21, `philipsruins:pumpkin_ruins` 1/26, `philipsruins:ancient_ruins` 1/27, `philipsruins:field_stone_ruins_rocks` 1/30, `philipsruins:lost_soul_city` 1/38, `forest_ruins:ruin_1` 1/45, `forest_ruins:ruin_2` 1/45, `forest_ruins:ruin_3` 1/45, `forest_ruins:ruin_4` 1/45, `forest_ruins:ruin_5` 1/45, `forest_ruins:ruin_6` 1/45, `forest_ruins:ruin_7` 1/45, `watchtower_building:abandoned_watchtower` 1/52 |
| `frozen_river` | 0.27% | **none** |
| `terralith:lavender_valley` | 0.27% | **none** |
| `ice_spikes` | 0.23% | **none** |
| `snowy_beach` | 0.23% | **none** |
| `windswept_hills` | 0.23% | **none** |
| `terralith:red_oasis` | 0.23% | **none** |
| `terralith:stony_spires` | 0.23% | **none** |
| `terralith:warm_river` | 0.23% | **none** |
| `mushroom_fields` | 0.20% | **none** |
| `windswept_gravelly_hills` | 0.20% | **none** |
| `terralith:blooming_plateau` | 0.20% | **none** |
| `terralith:blooming_valley` | 0.20% | **none** |
| `terralith:desert_spires` | 0.20% | `philipsruins:desert_structures` 1/16 |
| `terralith:fractured_savanna` | 0.20% | **none** |
| `terralith:wintry_forest` | 0.20% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `grove` | 0.17% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:amethyst_rainforest` | 0.17% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `terralith:haze_mountain` | 0.13% | **none** |
| `terralith:lush_valley` | 0.13% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:painted_mountains` | 0.13% | **none** |
| `terralith:tropical_jungle` | 0.13% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `flower_forest` | 0.10% | `philipsruins:ancient_towers` 1/21, `philipsruins:pumpkin_ruins` 1/26, `philipsruins:ancient_ruins` 1/27, `philipsruins:field_stone_ruins_rocks` 1/30, `philipsruins:lost_soul_city` 1/38, `forest_ruins:ruin_1` 1/45, `forest_ruins:ruin_2` 1/45, `forest_ruins:ruin_3` 1/45, `forest_ruins:ruin_4` 1/45, `forest_ruins:ruin_5` 1/45, `forest_ruins:ruin_6` 1/45, `forest_ruins:ruin_7` 1/45, `watchtower_building:abandoned_watchtower` 1/52 |
| `terralith:amethyst_canyon` | 0.10% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `terralith:moonlight_grove` | 0.10% | **none** |
| `terralith:moonlight_valley` | 0.10% | **none** |
| `terralith:windswept_spires` | 0.10% | **none** |
| `terralith:yosemite_cliffs` | 0.10% | **none** |
| `terralith:yosemite_lowlands` | 0.10% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:alpha_islands` | 0.07% | **none** |
| `terralith:ashen_savanna` | 0.07% | **none** |
| `terralith:cloud_forest` | 0.07% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:lush_desert` | 0.07% | `philipsruins:desert_structures` 1/16 |
| `terralith:mirage_isles` | 0.07% | **none** |
| `windswept_forest` | 0.03% | `forest_ruins:ruin_1` 1/25, `forest_ruins:ruin_2` 1/25, `forest_ruins:ruin_3` 1/25, `forest_ruins:ruin_4` 1/25, `forest_ruins:ruin_5` 1/25, `forest_ruins:ruin_6` 1/25, `forest_ruins:ruin_7` 1/25 |
| `terralith:alpine_grove` | 0.03% | `philipsruins:ancient_towers` 1/17, `philipsruins:pumpkin_ruins` 1/21, `philipsruins:ancient_ruins` 1/22, `philipsruins:field_stone_ruins_rocks` 1/25, `philipsruins:lost_soul_city` 1/38 |
| `terralith:desert_oasis` | 0.03% | `philipsruins:desert_structures` 1/16 |
| `terralith:emerald_peaks` | 0.03% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:jungle_mountains` | 0.03% | `philipsruins:field_stone_ruins_rocks` 1/14, `philipsruins:lost_soul_city` 1/38 |
| `terralith:lavender_forest` | 0.03% | **none** |
| `terralith:sakura_valley` | 0.03% | **none** |
| `terralith:skylands_summer` | 0.03% | **none** |
| `terralith:skylands_winter` | 0.03% | **none** |
| `terralith:warped_mesa` | 0.03% | **none** |
| `terralith:wintry_lowlands` | 0.03% | **none** |
| `deep_dark` | 0.00% | `philipsruins:underground_structures` 1/28, `philipsruins:lost_soul_city` 1/38 |
| `stony_shore` | 0.00% | `philipsruins:field_stone_ruins` 1/16, `philipsruins:level_one_ruins` 1/16, `philipsruins:level_three_ruins` 1/18, `philipsruins:level_two_ruins` 1/23, `philipsruins:level_two_ruins_pool` 1/23, `philipsruins:lost_soul_city` 1/38, `philipsruins:start_nether_ruin` 1/64 |
| `sunflower_plains` | 0.00% | `philipsruins:ancient_towers` 1/16, `philipsruins:ancient_ruins` 1/20, `philipsruins:field_stone_ruins_rocks` 1/23, `philipsruins:lost_soul_city` 1/38 |
| `terralith:alpha_islands_winter` | 0.00% | **none** |
| `terralith:ancient_sands` | 0.00% | **none** |
| `terralith:basalt_cliffs` | 0.00% | **none** |
| `terralith:caldera` | 0.00% | `philipsruins:ancient_towers` 1/12, `philipsruins:ancient_ruins` 1/16, `philipsruins:lost_soul_city` 1/38 |
| `terralith:cave/frostfire_caves` | 0.00% | **none** |
| `terralith:cave/mantle_caves` | 0.00% | **none** |
| `terralith:cave/tuff_caves` | 0.00% | **none** |
| `terralith:frozen_cliffs` | 0.00% | **none** |
| `terralith:granite_cliffs` | 0.00% | **none** |
| `terralith:rocky_shrubland` | 0.00% | **none** |
| `terralith:savanna_slopes` | 0.00% | **none** |
| `terralith:shield_clearing` | 0.00% | **none** |
| `terralith:siberian_grove` | 0.00% | **none** |
| `terralith:skylands_autumn` | 0.00% | **none** |
| `terralith:skylands_spring` | 0.00% | **none** |
| `terralith:snowy_maple_forest` | 0.00% | **none** |
| `terralith:snowy_shield` | 0.00% | **none** |
| `terralith:valley_clearing` | 0.00% | **none** |
| `terralith:volcanic_crater` | 0.00% | **none** |
| `terralith:white_cliffs` | 0.00% | **none** |
| `terralith:white_mesa` | 0.00% | **none** |
| `lush_caves` | 11.76% | `terralith:underground/oak_cabin` 1/7.4, `philipsruins:lost_soul_city` 1/38 |
| `dripstone_caves` | 5.08% | `philipsruins:lost_soul_city` 1/38 |

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

