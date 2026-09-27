# Structure biome merge proposals

Each proposal adds modded biomes of one theme to the spawn pool of one mod's structures. Pools are fully tag-resolved (Terralith's existing additions to vanilla tags are already counted as "current"). Approve by number; nothing is applied yet. Execution = one additive tag file per tag listed (`"replace": false`) under `kubejs/data/<ns>/tags/worldgen/biome/`.

23 proposals, 67 structures.

## 1. badlands biomes (terralith) -> towns_and_towers structures

**Add:** terralith:ancient_sands, terralith:bryce_canyon, terralith:painted_mountains, terralith:red_oasis, terralith:savanna_badlands, terralith:warped_mesa, terralith:white_mesa

**Tags extended (4):** towns_and_towers:has_structure/pillager_outpost_badlands, towns_and_towers:has_structure/pillager_outpost_wooded_badlands, towns_and_towers:has_structure/village_badlands_pueblo, towns_and_towers:has_structure/village_wooded_badlands_tipi

**Structures:**
- towns_and_towers:pillager_outpost_badlands (+7)
- towns_and_towers:pillager_outpost_wooded_badlands (+7)
- towns_and_towers:village_badlands (+7)
- towns_and_towers:village_wooded_badlands (+7)

## 2. beach biomes (terralith) -> towns_and_towers structures

**Add:** terralith:frozen_cliffs, terralith:gravel_beach

**Tags extended (2):** towns_and_towers:has_structure/pillager_outpost_beach, towns_and_towers:has_structure/village_beach_lighthouse

**Structures:**
- towns_and_towers:pillager_outpost_beach (+2)
- towns_and_towers:village_beach (+2)

## 3. desert biomes (terralith) -> betterdeserttemples structures

**Add:** terralith:desert_canyon, terralith:desert_oasis, terralith:desert_spires, terralith:lush_desert, terralith:sandstone_valley

**Tags extended (1):** betterdeserttemples:has_structure/better_desert_temple

**Structures:**
- betterdeserttemples:desert_temple (+5)

## 4. desert biomes (terralith) -> minecraft structures

**Add:** terralith:desert_canyon, terralith:desert_spires, terralith:lush_desert

**Tags extended (3):** minecraft:has_structure/desert_pyramid, minecraft:has_structure/ruined_portal_desert, minecraft:has_structure/village_desert

**Structures:**
- minecraft:desert_pyramid (+2)
- minecraft:ruined_portal_desert (+2)
- minecraft:village_desert (+3)

## 5. desert biomes (terralith) -> philipsruins structures

**Add:** terralith:desert_canyon, terralith:desert_oasis, terralith:desert_spires, terralith:lush_desert, terralith:sandstone_valley

**Tags extended (1):** philipsruins:has_structure/desert_structures_biomes

**Structures:**
- philipsruins:desert_structures (+5)

## 6. desert biomes (terralith) -> towns_and_towers structures

**Add:** terralith:desert_canyon, terralith:desert_oasis, terralith:desert_spires, terralith:lush_desert, terralith:sandstone_valley

**Tags extended (2):** minecraft:has_structure/desert_pyramid, towns_and_towers:has_structure/pillager_outpost_desert

**Structures:**
- towns_and_towers:mimic_desert (+2)
- towns_and_towers:pillager_outpost_desert (+5)

## 7. end biomes (nullscape) -> philipsruins structures

**Add:** nullscape:crystal_peaks, nullscape:shadowlands, nullscape:void_barrens

**Tags extended (1):** philipsruins:has_structure/end_ruins_biomes

**Structures:**
- philipsruins:end_gate_fortress (+3)
- philipsruins:end_ruins (+3)

## 8. forest biomes (terralith) -> minecraft structures

**Add:** terralith:alpha_islands, terralith:alpine_grove, terralith:birch_taiga, terralith:cloud_forest, terralith:lavender_forest, terralith:lavender_valley, terralith:mirage_isles, terralith:moonlight_grove, terralith:moonlight_valley, terralith:sakura_valley, terralith:skylands, terralith:skylands_autumn, terralith:skylands_spring, terralith:temperate_highlands, terralith:white_cliffs, terralith:yosemite_lowlands

**Tags extended (1):** minecraft:has_structure/woodland_mansion

**Structures:**
- minecraft:mansion (+16)

## 9. forest biomes (terralith) -> towns_and_towers structures

**Add:** terralith:alpha_islands, terralith:alpine_grove, terralith:birch_taiga, terralith:cloud_forest, terralith:lavender_forest, terralith:lavender_valley, terralith:mirage_isles, terralith:moonlight_grove, terralith:moonlight_valley, terralith:sakura_grove, terralith:sakura_valley, terralith:skylands, terralith:skylands_autumn, terralith:skylands_spring, terralith:temperate_highlands, terralith:white_cliffs, terralith:yosemite_lowlands

**Tags extended (6):** towns_and_towers:has_structure/pillager_outpost_birch_forest, towns_and_towers:has_structure/pillager_outpost_flower_forest, towns_and_towers:has_structure/pillager_outpost_forest, towns_and_towers:has_structure/village_birch_forest_romanian, towns_and_towers:has_structure/village_flower_forest_japanese, towns_and_towers:has_structure/village_forest_ruins

**Structures:**
- towns_and_towers:pillager_outpost_birch_forest (+17)
- towns_and_towers:pillager_outpost_flower_forest (+17)
- towns_and_towers:pillager_outpost_forest (+17)
- towns_and_towers:village_birch_forest (+17)
- towns_and_towers:village_flower_forest (+17)
- towns_and_towers:village_forest (+17)

## 10. jungle biomes (terralith) -> betterjungletemples structures

**Add:** terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:cave/underground_jungle, terralith:jungle_mountains, terralith:skylands_summer

**Tags extended (1):** betterjungletemples:has_structure/better_jungle_temple

**Structures:**
- betterjungletemples:jungle_temple (+5)

## 11. jungle biomes (terralith) -> minecraft structures

**Add:** terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:cave/underground_jungle, terralith:jungle_mountains, terralith:skylands_summer

**Tags extended (2):** minecraft:has_structure/jungle_temple, minecraft:has_structure/ruined_portal_jungle

**Structures:**
- minecraft:jungle_pyramid (+5)
- minecraft:ruined_portal_jungle (+2)

## 12. jungle biomes (terralith) -> themissingvillages structures

**Add:** terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:cave/underground_jungle, terralith:jungle_mountains, terralith:rocky_jungle, terralith:skylands_summer, terralith:tropical_jungle

**Tags extended (3):** themissingvillages:jungle_bamboo, themissingvillages:jungle_normal, themissingvillages:jungle_sparse

**Structures:**
- themissingvillages:large/jungle_bamboo_village (+7)
- themissingvillages:large/jungle_sparse_village (+7)
- themissingvillages:large/jungle_village (+7)
- themissingvillages:medium/jungle_bamboo_village (+7)
- themissingvillages:medium/jungle_sparse_village (+7)
- themissingvillages:medium/jungle_village (+7)
- themissingvillages:small/jungle_bamboo_village (+7)
- themissingvillages:small/jungle_sparse_village (+7)
- themissingvillages:small/jungle_village (+7)

## 13. jungle biomes (terralith) -> towns_and_towers structures

**Add:** terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:cave/underground_jungle, terralith:jungle_mountains, terralith:rocky_jungle, terralith:skylands_summer, terralith:tropical_jungle

**Tags extended (4):** towns_and_towers:has_structure/pillager_outpost_jungle, towns_and_towers:has_structure/pillager_outpost_sparse_jungle, towns_and_towers:has_structure/village_jungle_tribal, towns_and_towers:has_structure/village_sparse_jungle_polynesian

**Structures:**
- towns_and_towers:pillager_outpost_jungle (+7)
- towns_and_towers:pillager_outpost_sparse_jungle (+7)
- towns_and_towers:village_jungle (+7)
- towns_and_towers:village_sparse_jungle (+7)

## 14. mushroom biomes (quark, terralith) -> towns_and_towers structures

**Add:** quark:glimmering_weald, terralith:cave/fungal_caves, terralith:mirage_isles

**Tags extended (2):** towns_and_towers:has_structure/pillager_outpost_mushroom_fields, towns_and_towers:has_structure/village_mushroom_fields_fantasy

**Structures:**
- towns_and_towers:pillager_outpost_mushroom_fields (+3)
- towns_and_towers:village_mushroom_fields (+3)

## 15. plains biomes (terralith) -> towns_and_towers structures

**Add:** terralith:alpine_highlands, terralith:highlands, terralith:rocky_shrubland

**Tags extended (2):** towns_and_towers:has_structure/pillager_outpost_sunflower_plains, towns_and_towers:has_structure/village_sunflower_plains_farm

**Structures:**
- towns_and_towers:pillager_outpost_sunflower_plains (+3)
- towns_and_towers:village_sunflower_plains (+3)

## 16. savanna biomes (terralith) -> minecraft structures

**Add:** terralith:ashen_savanna, terralith:brushland, terralith:fractured_savanna, terralith:shrubland

**Tags extended (1):** minecraft:has_structure/village_savanna

**Structures:**
- minecraft:village_savanna (+4)

## 17. savanna biomes (terralith) -> towns_and_towers structures

**Add:** terralith:arid_highlands, terralith:ashen_savanna, terralith:brushland, terralith:fractured_savanna, terralith:hot_shrubland, terralith:savanna_badlands, terralith:savanna_slopes, terralith:shrubland

**Tags extended (1):** towns_and_towers:has_structure/pillager_outpost_savanna

**Structures:**
- towns_and_towers:pillager_outpost_savanna (+8)

## 18. snowy biomes (terralith) -> minecraft structures

**Add:** terralith:alpha_islands_winter, terralith:alpine_grove, terralith:cold_shrubland, terralith:emerald_peaks, terralith:gravel_desert, terralith:ice_marsh, terralith:rocky_mountains, terralith:rocky_shrubland, terralith:siberian_grove, terralith:skylands_winter, terralith:snowy_maple_forest, terralith:snowy_shield, terralith:wintry_forest, terralith:wintry_lowlands

**Tags extended (2):** minecraft:has_structure/igloo, minecraft:has_structure/village_snowy

**Structures:**
- minecraft:igloo (+13)
- minecraft:village_snowy (+14)

## 19. snowy biomes (terralith) -> towns_and_towers structures

**Add:** terralith:alpha_islands_winter, terralith:alpine_grove, terralith:cold_shrubland, terralith:emerald_peaks, terralith:gravel_desert, terralith:ice_marsh, terralith:rocky_mountains, terralith:rocky_shrubland, terralith:siberian_grove, terralith:skylands_winter, terralith:snowy_badlands, terralith:snowy_maple_forest, terralith:snowy_shield, terralith:wintry_forest, terralith:wintry_lowlands

**Tags extended (8):** towns_and_towers:has_structure/pillager_outpost_grove, towns_and_towers:has_structure/pillager_outpost_snowy_beach, towns_and_towers:has_structure/pillager_outpost_snowy_plains, towns_and_towers:has_structure/pillager_outpost_snowy_slopes, towns_and_towers:has_structure/pillager_outpost_snowy_taiga, towns_and_towers:has_structure/village_grove_villager_outpost, towns_and_towers:has_structure/village_snowy_slopes_inn, towns_and_towers:has_structure/village_snowy_taiga_viking

**Structures:**
- towns_and_towers:pillager_outpost_grove (+15)
- towns_and_towers:pillager_outpost_snowy_beach (+15)
- towns_and_towers:pillager_outpost_snowy_plains (+15)
- towns_and_towers:pillager_outpost_snowy_slopes (+15)
- towns_and_towers:pillager_outpost_snowy_taiga (+15)
- towns_and_towers:village_grove (+15)
- towns_and_towers:village_snowy_slopes (+15)
- towns_and_towers:village_snowy_taiga (+15)

## 20. swamp biomes (terralith) -> betterwitchhuts structures

**Add:** terralith:ice_marsh, terralith:orchid_swamp

**Tags extended (2):** betterwitchhuts:has_structure/better_witch_hut, betterwitchhuts:has_structure/witch_circle

**Structures:**
- betterwitchhuts:witch_circle (+2)
- betterwitchhuts:witch_hut (+2)

## 21. swamp biomes (terralith) -> themissingvillages structures

**Add:** terralith:ice_marsh, terralith:orchid_swamp

**Tags extended (2):** themissingvillages:swamp_mangrove, themissingvillages:swamp_normal

**Structures:**
- themissingvillages:large/swamp_mangrove_village (+2)
- themissingvillages:large/swamp_village (+2)
- themissingvillages:medium/swamp_mangrove_village (+2)
- themissingvillages:medium/swamp_village (+2)
- themissingvillages:small/swamp_mangrove_village (+2)
- themissingvillages:small/swamp_village (+2)

## 22. swamp biomes (terralith) -> towns_and_towers structures

**Add:** terralith:ice_marsh, terralith:orchid_swamp

**Tags extended (2):** towns_and_towers:has_structure/pillager_outpost_swamp, towns_and_towers:has_structure/village_swamp_boat

**Structures:**
- towns_and_towers:pillager_outpost_swamp (+2)
- towns_and_towers:village_swamp (+2)

## 23. taiga biomes (terralith) -> towns_and_towers structures

**Add:** terralith:alpine_highlands, terralith:birch_taiga, terralith:bryce_canyon, terralith:cloud_forest, terralith:forested_highlands, terralith:lush_valley, terralith:scarlet_mountains, terralith:shield, terralith:siberian_grove, terralith:siberian_taiga, terralith:skylands_winter, terralith:snowy_maple_forest, terralith:valley_clearing, terralith:wintry_forest, terralith:wintry_lowlands, terralith:yellowstone, terralith:yosemite_lowlands

**Tags extended (3):** towns_and_towers:has_structure/pillager_outpost_old_growth_taiga, towns_and_towers:has_structure/pillager_outpost_taiga, towns_and_towers:has_structure/village_old_growth_taiga_polish

**Structures:**
- towns_and_towers:pillager_outpost_old_growth_taiga (+17)
- towns_and_towers:pillager_outpost_taiga (+17)
- towns_and_towers:village_old_growth_taiga (+17)
