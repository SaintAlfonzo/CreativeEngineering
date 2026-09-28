# Structure biome merges: status + review

20 proposals executed (45 additive tag files), 1 held, 2 new/undecided. Approved state lives in `biome_merges.json`; `tools/build_biome_merges.py` regenerates this file.

Global rules applied: **skylands biomes are never added; cave biomes are never added to non-cave-native structures.**

## Status by proposal

| # | theme -> target | status | notes |
|---|---|---|---|
| 1 | badlands -> towns_and_towers | executed |  |
| 2 | beach -> towns_and_towers | executed |  |
| 3 | desert -> betterdeserttemples | executed |  |
| 4 | desert -> minecraft | executed |  |
| 5 | desert -> philipsruins | executed |  |
| 6 | desert -> towns_and_towers | executed |  |
| 7 | end -> philipsruins | HELD |  |
| 8 | forest -> minecraft | EXECUTED (modified) | skylands removed |
| 9 | forest -> simply_houses | NEW (undecided, not applied) |  |
| 10 | forest -> towns_and_towers | EXECUTED (modified) | skylands removed |
| 11 | jungle -> betterjungletemples | executed | skylands removed; cave biomes withheld: terralith:cave/underground_jungle |
| 12 | jungle -> minecraft | executed | skylands removed; cave biomes withheld: terralith:cave/underground_jungle; 1 of 2 structures got no additions (no fitting biome) |
| 13 | jungle -> towns_and_towers | executed | skylands removed; cave biomes withheld: terralith:cave/underground_jungle |
| 14 | mushroom -> towns_and_towers | EXECUTED (modified) | cave biomes withheld: quark:glimmering_weald, terralith:cave/fungal_caves |
| 15 | plains -> towns_and_towers | EXECUTED (modified) | 2 of 2 structures got no additions (no fitting biome) |
| 16 | savanna -> minecraft | executed |  |
| 17 | savanna -> towns_and_towers | executed |  |
| 18 | snowy -> minecraft | executed | skylands removed |
| 19 | snowy -> towns_and_towers | EXECUTED (modified) | skylands removed; 1 of 8 structures got no additions (no fitting biome) |
| 20 | swamp -> betterwitchhuts | executed |  |
| 21 | swamp -> towns_and_towers | executed |  |
| 22 | taiga -> simply_houses | NEW (undecided, not applied) |  |
| 23 | taiga -> towns_and_towers | EXECUTED (modified) | skylands removed; removed: terralith:bryce_canyon |

## Applied tag merges

- **betterdeserttemples:has_structure/better_desert_temple** (proposal 3): terralith:desert_canyon, terralith:desert_oasis, terralith:desert_spires, terralith:lush_desert, terralith:sandstone_valley
- **betterjungletemples:has_structure/better_jungle_temple** (proposal 11): terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:jungle_mountains
- **betterwitchhuts:has_structure/better_witch_hut** (proposal 20): terralith:ice_marsh, terralith:orchid_swamp
- **betterwitchhuts:has_structure/witch_circle** (proposal 20): terralith:ice_marsh, terralith:orchid_swamp
- **minecraft:has_structure/desert_pyramid** (proposals 4, 6): terralith:desert_canyon, terralith:lush_desert
- **minecraft:has_structure/igloo** (proposal 18): terralith:alpha_islands_winter, terralith:alpine_grove, terralith:cold_shrubland, terralith:emerald_peaks, terralith:gravel_desert, terralith:ice_marsh, terralith:rocky_mountains, terralith:rocky_shrubland, terralith:siberian_grove, terralith:snowy_maple_forest, terralith:snowy_shield, terralith:wintry_lowlands
- **minecraft:has_structure/jungle_temple** (proposal 12): terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:jungle_mountains
- **minecraft:has_structure/ruined_portal_desert** (proposal 4): terralith:desert_canyon, terralith:lush_desert
- **minecraft:has_structure/village_desert** (proposal 4): terralith:desert_canyon, terralith:desert_spires, terralith:lush_desert
- **minecraft:has_structure/village_savanna** (proposal 16): terralith:ashen_savanna, terralith:brushland, terralith:fractured_savanna, terralith:shrubland
- **minecraft:has_structure/village_snowy** (proposal 18): terralith:alpha_islands_winter, terralith:alpine_grove, terralith:cold_shrubland, terralith:emerald_peaks, terralith:gravel_desert, terralith:ice_marsh, terralith:rocky_mountains, terralith:rocky_shrubland, terralith:siberian_grove, terralith:snowy_maple_forest, terralith:snowy_shield, terralith:wintry_forest, terralith:wintry_lowlands
- **minecraft:has_structure/woodland_mansion** (proposal 8): terralith:sakura_valley
- **philipsruins:has_structure/desert_structures_biomes** (proposal 5): terralith:desert_canyon, terralith:desert_oasis, terralith:desert_spires, terralith:lush_desert, terralith:sandstone_valley
- **towns_and_towers:has_structure/pillager_outpost_badlands** (proposal 1): terralith:ancient_sands, terralith:bryce_canyon, terralith:painted_mountains, terralith:red_oasis, terralith:savanna_badlands, terralith:warped_mesa, terralith:white_mesa
- **towns_and_towers:has_structure/pillager_outpost_beach** (proposal 2): terralith:frozen_cliffs, terralith:gravel_beach
- **towns_and_towers:has_structure/pillager_outpost_birch_forest** (proposal 10): terralith:birch_taiga, terralith:white_cliffs
- **towns_and_towers:has_structure/pillager_outpost_desert** (proposal 6): terralith:desert_canyon, terralith:desert_oasis, terralith:desert_spires, terralith:lush_desert, terralith:sandstone_valley
- **towns_and_towers:has_structure/pillager_outpost_flower_forest** (proposal 10): terralith:cloud_forest, terralith:lavender_forest, terralith:lavender_valley, terralith:moonlight_grove, terralith:moonlight_valley, terralith:sakura_grove, terralith:sakura_valley
- **towns_and_towers:has_structure/pillager_outpost_forest** (proposal 10): terralith:temperate_highlands
- **towns_and_towers:has_structure/pillager_outpost_grove** (proposal 19): terralith:alpine_grove, terralith:siberian_grove
- **towns_and_towers:has_structure/pillager_outpost_jungle** (proposal 13): terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:jungle_mountains, terralith:rocky_jungle, terralith:tropical_jungle
- **towns_and_towers:has_structure/pillager_outpost_mushroom_fields** (proposal 14): terralith:mirage_isles
- **towns_and_towers:has_structure/pillager_outpost_old_growth_taiga** (proposal 23): terralith:alpine_highlands, terralith:birch_taiga, terralith:cloud_forest, terralith:forested_highlands, terralith:lush_valley, terralith:scarlet_mountains, terralith:shield, terralith:siberian_grove, terralith:siberian_taiga, terralith:snowy_maple_forest, terralith:valley_clearing, terralith:wintry_forest, terralith:wintry_lowlands, terralith:yellowstone, terralith:yosemite_lowlands
- **towns_and_towers:has_structure/pillager_outpost_savanna** (proposal 17): terralith:arid_highlands, terralith:ashen_savanna, terralith:brushland, terralith:fractured_savanna, terralith:hot_shrubland, terralith:savanna_badlands, terralith:savanna_slopes, terralith:shrubland
- **towns_and_towers:has_structure/pillager_outpost_snowy_plains** (proposal 19): terralith:gravel_desert, terralith:rocky_shrubland
- **towns_and_towers:has_structure/pillager_outpost_snowy_slopes** (proposal 19): terralith:rocky_mountains
- **towns_and_towers:has_structure/pillager_outpost_snowy_taiga** (proposal 19): terralith:snowy_maple_forest, terralith:snowy_shield, terralith:wintry_forest, terralith:wintry_lowlands
- **towns_and_towers:has_structure/pillager_outpost_sparse_jungle** (proposal 13): terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:jungle_mountains, terralith:rocky_jungle, terralith:tropical_jungle
- **towns_and_towers:has_structure/pillager_outpost_swamp** (proposal 21): terralith:ice_marsh, terralith:orchid_swamp
- **towns_and_towers:has_structure/pillager_outpost_taiga** (proposal 23): terralith:alpine_highlands, terralith:birch_taiga, terralith:cloud_forest, terralith:forested_highlands, terralith:lush_valley, terralith:scarlet_mountains, terralith:shield, terralith:siberian_grove, terralith:siberian_taiga, terralith:snowy_maple_forest, terralith:valley_clearing, terralith:wintry_forest, terralith:wintry_lowlands, terralith:yellowstone, terralith:yosemite_lowlands
- **towns_and_towers:has_structure/pillager_outpost_wooded_badlands** (proposal 1): terralith:ancient_sands, terralith:bryce_canyon, terralith:painted_mountains, terralith:red_oasis, terralith:savanna_badlands, terralith:warped_mesa, terralith:white_mesa
- **towns_and_towers:has_structure/village_badlands_pueblo** (proposal 1): terralith:ancient_sands, terralith:bryce_canyon, terralith:painted_mountains, terralith:red_oasis, terralith:savanna_badlands, terralith:warped_mesa, terralith:white_mesa
- **towns_and_towers:has_structure/village_beach_lighthouse** (proposal 2): terralith:frozen_cliffs, terralith:gravel_beach
- **towns_and_towers:has_structure/village_birch_forest_romanian** (proposal 10): terralith:birch_taiga, terralith:white_cliffs
- **towns_and_towers:has_structure/village_flower_forest_japanese** (proposal 10): terralith:cloud_forest, terralith:lavender_forest, terralith:lavender_valley, terralith:moonlight_grove, terralith:moonlight_valley, terralith:sakura_grove, terralith:sakura_valley
- **towns_and_towers:has_structure/village_forest_ruins** (proposal 10): terralith:temperate_highlands
- **towns_and_towers:has_structure/village_grove_villager_outpost** (proposal 19): terralith:alpine_grove, terralith:siberian_grove
- **towns_and_towers:has_structure/village_jungle_tribal** (proposal 13): terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:jungle_mountains, terralith:rocky_jungle, terralith:tropical_jungle
- **towns_and_towers:has_structure/village_mushroom_fields_fantasy** (proposal 14): terralith:mirage_isles
- **towns_and_towers:has_structure/village_old_growth_taiga_polish** (proposal 23): terralith:alpine_highlands, terralith:birch_taiga, terralith:cloud_forest, terralith:forested_highlands, terralith:lush_valley, terralith:scarlet_mountains, terralith:shield, terralith:siberian_grove, terralith:siberian_taiga, terralith:snowy_maple_forest, terralith:valley_clearing, terralith:wintry_forest, terralith:wintry_lowlands, terralith:yellowstone, terralith:yosemite_lowlands
- **towns_and_towers:has_structure/village_snowy_slopes_inn** (proposal 19): terralith:rocky_mountains
- **towns_and_towers:has_structure/village_snowy_taiga_viking** (proposal 19): terralith:snowy_maple_forest, terralith:snowy_shield, terralith:wintry_forest, terralith:wintry_lowlands
- **towns_and_towers:has_structure/village_sparse_jungle_polynesian** (proposal 13): terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:jungle_mountains, terralith:rocky_jungle, terralith:tropical_jungle
- **towns_and_towers:has_structure/village_swamp_boat** (proposal 21): terralith:ice_marsh, terralith:orchid_swamp
- **towns_and_towers:has_structure/village_wooded_badlands_tipi** (proposal 1): terralith:ancient_sands, terralith:bryce_canyon, terralith:painted_mountains, terralith:red_oasis, terralith:savanna_badlands, terralith:warped_mesa, terralith:white_mesa

Variant-matched structures that received NO additions (no fitting biome):
- towns_and_towers:pillager_outpost_snowy_beach
- towns_and_towers:pillager_outpost_sunflower_plains
- towns_and_towers:village_sunflower_plains

## Variant-matched sets (proposals 10, 15, 19)

Each biome-variant structure only gets biomes resembling its own variant.

- birch forest structures: terralith:birch_taiga, terralith:white_cliffs  (birch-dominant trunks)
- flower forest structures: terralith:cloud_forest, terralith:lavender_forest, terralith:lavender_valley, terralith:moonlight_grove, terralith:moonlight_valley, terralith:sakura_grove, terralith:sakura_valley  (Terralith's own c:flower_forests, minus skylands)
- plain forest structures: terralith:temperate_highlands  (oak/birch/dark-oak temperate mix)
- sunflower plains structures: none. No Terralith biome has sunflowers except skylands_summer (excluded). Original candidates rejected as not sunflower-plains-like: alpine_highlands, highlands, rocky_shrubland
- grove structures: terralith:alpine_grove, terralith:siberian_grove
- snowy_slopes structures: terralith:rocky_mountains
- snowy_beach structures: (none)
- snowy_plains structures: terralith:gravel_desert, terralith:rocky_shrubland
- snowy_taiga structures: terralith:snowy_maple_forest, terralith:snowy_shield, terralith:wintry_forest, terralith:wintry_lowlands
- snowy biomes NOT assigned to any T&T variant (second review): terralith:alpha_islands_winter, terralith:cold_shrubland, terralith:emerald_peaks, terralith:ice_marsh, terralith:snowy_badlands

## Proposal 8: woodland mansion (second review)

Applied: terralith:sakura_valley (dark oak canopy trunks, sister of sakura_grove which Terralith already gives mansions).

Remaining biomes from the original 16, for second review:

- terralith:alpha_islands: temp 0.7, downfall 0.8, rain; trunks: -; flowers: dandelion, poppy
- terralith:alpine_grove: temp -0.2, downfall 0.8, snow; trunks: spruce; flowers: -
- terralith:birch_taiga: temp 0.22, downfall 0.2, rain; trunks: birch, oak; flowers: -
- terralith:cloud_forest: temp 0.25, downfall 0.8, rain; trunks: birch, spruce; flowers: azure_bluet, lily_of_the_valley, oxeye_daisy, white_tulip
- terralith:lavender_forest: temp 0.7, downfall 0.8, rain; trunks: dark_oak; flowers: -
- terralith:lavender_valley: temp 0.7, downfall 0.8, rain; trunks: dark_oak; flowers: -
- terralith:mirage_isles: temp 0.7, downfall 0.8, rain; trunks: dark_oak; flowers: -
- terralith:moonlight_grove: temp 0.7, downfall 0.8, rain; trunks: -; flowers: blue_orchid, cornflower
- terralith:moonlight_valley: temp 0.7, downfall 0.8, rain; trunks: -; flowers: blue_orchid, cornflower, orange_tulip
- terralith:skylands [SKYLANDS - removed]: temp 0.5, downfall 0.5, rain; trunks: birch, dark_oak, oak; flowers: -
- terralith:skylands_autumn [SKYLANDS - removed]: temp 0.5, downfall 0.5, rain; trunks: birch, dark_oak, oak; flowers: -
- terralith:skylands_spring [SKYLANDS - removed]: temp 0.5, downfall 0.5, rain; trunks: birch, dark_oak; flowers: allium, cornflower, dandelion, orange_tulip, pink_tulip, red_tulip, white_tulip
- terralith:temperate_highlands: temp 0.9, downfall 0.2, rain; trunks: birch, dark_oak, oak; flowers: orange_tulip, poppy, rose_bush, white_tulip
- terralith:white_cliffs: temp 0.4, downfall 0.3, rain; trunks: birch; flowers: -
- terralith:yosemite_lowlands: temp 0.25, downfall 0.8, rain; trunks: birch, oak, spruce; flowers: dandelion

## Proposal 7: End structures breakout (HELD)

Proposal 7 as generated only touches 2 structures (philipsruins end_ruins, end_gate_fortress, via tag `philipsruins:has_structure/end_ruins_biomes`, currently end_highlands + end_midlands). The End has 8 biomes (3 Nullscape + 5 vanilla), so here is the full structure x biome matrix ('X' = generates there now, 'p7' = proposal 7 would add):

| structure | tag | end_barrens | end_highlands | end_midlands | small_end_islands | the_end | crystal_peaks | shadowlands | void_barrens |
|---|---|---|---|---|---|---|---|---|---|
| mes:astral_hideaway | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:astral_meteorite | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:ender_spire | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:enderbloom_grove | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:enderkeep_courtyard | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:enderpin_spikes | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:enderskog | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:enderwatch_tower | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:endscraps | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:manuscript_shrine | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:monolith | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:mythic_garden | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:phantom_citadel | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:placid_prairie | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:ruined_pillar | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| mes:starlight_voyager | mes:has_structure/end_biomes | X | X | X | X | X | X | X | X |
| minecraft:end_city | minecraft:has_structure/end_city |  | X | X |  |  | X | X | X |
| nullscape:dragon_skeleton | literal |  |  |  |  |  |  | X |  |
| philipsruins:end_gate_fortress | philipsruins:has_structure/end_ruins_biomes |  | X | X |  |  | p7 | p7 | p7 |
| philipsruins:end_ruins | philipsruins:has_structure/end_ruins_biomes |  | X | X |  |  | p7 | p7 | p7 |

## Skylands biomes: structures that currently generate in them

Skylands biomes: terralith:skylands, terralith:skylands_autumn, terralith:skylands_spring, terralith:skylands_summer, terralith:skylands_winter. Removed from every add list; nothing below is changed, only listed.

- **minecraft:mineshaft** (5/5 skylands biomes) via minecraft:has_structure/mineshaft
- **minecraft:stronghold** (5/5 skylands biomes) via minecraft:has_structure/stronghold
- **philipsruins:ancient_dungeon** (5/5 skylands biomes) via philipsruins:has_structure/ancient_dungeon_biomes

## Cave biomes

Cave-native = underground/stronghold structure step, or vanilla mineshaft/stronghold/ancient_city, or Terralith `underground/*` (those declare step surface_structures but are cave structures by design, so they are treated as native).

### Already generating in cave biomes (non-cave-native)

- [CAVE] **minecraft:ruined_portal**: minecraft:dripstone_caves, minecraft:lush_caves via minecraft:has_structure/ruined_portal_standard

### Cave biomes in the suggestions (withheld, not applied)

- [CAVE] proposal 11 (jungle -> betterjungletemples): terralith:cave/underground_jungle
- [CAVE] proposal 12 (jungle -> minecraft): terralith:cave/underground_jungle
- [CAVE] proposal 13 (jungle -> towns_and_towers): terralith:cave/underground_jungle
- [CAVE] proposal 14 (mushroom -> towns_and_towers): quark:glimmering_weald, terralith:cave/fungal_caves

## Variant structures vs broader biome definitions

### In the suggestions (blanket across sibling variants, like proposal 10/15)

- [VARIANT-BROAD] proposal 1 (badlands -> towns_and_towers, executed): 2 distinct vanilla variants receive the identical 7-biome list -> minecraft:badlands: towns_and_towers:pillager_outpost_badlands, towns_and_towers:village_badlands | minecraft:wooded_badlands: towns_and_towers:pillager_outpost_wooded_badlands, towns_and_towers:village_wooded_badlands
- [VARIANT-BROAD] proposal 13 (jungle -> towns_and_towers, executed): 2 distinct vanilla variants receive the identical 7-biome list -> minecraft:jungle: towns_and_towers:pillager_outpost_jungle, towns_and_towers:village_jungle | minecraft:sparse_jungle: towns_and_towers:pillager_outpost_sparse_jungle, towns_and_towers:village_sparse_jungle
- [VARIANT-BROAD] proposal 20 (swamp -> betterwitchhuts, executed): 2 distinct vanilla variants receive the identical 2-biome list -> minecraft:swamp: betterwitchhuts:witch_circle | minecraft:mangrove_swamp/minecraft:swamp: betterwitchhuts:witch_hut
- [VARIANT-BROAD] proposal 23 (taiga -> towns_and_towers, executed): 2 distinct vanilla variants receive the identical 17-biome list -> minecraft:old_growth_pine_taiga/minecraft:old_growth_spruce_taiga: towns_and_towers:pillager_outpost_old_growth_taiga, towns_and_towers:village_old_growth_taiga | minecraft:taiga: towns_and_towers:pillager_outpost_taiga

### Already present (variant structure whose pool includes off-theme modded biomes)

- [VARIANT-BROAD] **farmers_structures:aquacultures_carp** (theme: forest): also generates in terralith:fractured_savanna, terralith:jungle_mountains
- [VARIANT-BROAD] **farmers_structures:aquacultures_common** (theme: forest): also generates in terralith:fractured_savanna, terralith:jungle_mountains
- [VARIANT-BROAD] **farmers_structures:cooks** (theme: plains): also generates in terralith:cloud_forest, terralith:fractured_savanna, terralith:jungle_mountains
- [VARIANT-BROAD] **minecraft:swamp_hut** (theme: swamp): also generates in terralith:siberian_taiga
- [VARIANT-BROAD] **minecraft:village_plains** (theme: plains): also generates in terralith:birch_taiga, terralith:lavender_forest, terralith:temperate_highlands
- [VARIANT-BROAD] **minecraft:village_taiga** (theme: taiga): also generates in terralith:cold_shrubland, terralith:highlands, terralith:rocky_shrubland

## Original proposal lists (as first generated, before decisions)

### 1. badlands (terralith) -> towns_and_towers

Add: terralith:ancient_sands, terralith:bryce_canyon, terralith:painted_mountains, terralith:red_oasis, terralith:savanna_badlands, terralith:warped_mesa, terralith:white_mesa

Structures: towns_and_towers:pillager_outpost_badlands (+7), towns_and_towers:pillager_outpost_wooded_badlands (+7), towns_and_towers:village_badlands (+7), towns_and_towers:village_wooded_badlands (+7)

### 2. beach (terralith) -> towns_and_towers

Add: terralith:frozen_cliffs, terralith:gravel_beach

Structures: towns_and_towers:pillager_outpost_beach (+2), towns_and_towers:village_beach (+2)

### 3. desert (terralith) -> betterdeserttemples

Add: terralith:desert_canyon, terralith:desert_oasis, terralith:desert_spires, terralith:lush_desert, terralith:sandstone_valley

Structures: betterdeserttemples:desert_temple (+5)

### 4. desert (terralith) -> minecraft

Add: terralith:desert_canyon, terralith:desert_spires, terralith:lush_desert

Structures: minecraft:desert_pyramid (+2), minecraft:ruined_portal_desert (+2), minecraft:village_desert (+3)

### 5. desert (terralith) -> philipsruins

Add: terralith:desert_canyon, terralith:desert_oasis, terralith:desert_spires, terralith:lush_desert, terralith:sandstone_valley

Structures: philipsruins:desert_structures (+5)

### 6. desert (terralith) -> towns_and_towers

Add: terralith:desert_canyon, terralith:desert_oasis, terralith:desert_spires, terralith:lush_desert, terralith:sandstone_valley

Structures: towns_and_towers:mimic_desert (+2), towns_and_towers:pillager_outpost_desert (+5)

### 7. end (nullscape) -> philipsruins

Add: nullscape:crystal_peaks, nullscape:shadowlands, nullscape:void_barrens

Structures: philipsruins:end_gate_fortress (+3), philipsruins:end_ruins (+3)

### 8. forest (terralith) -> minecraft

Add: terralith:alpha_islands, terralith:alpine_grove, terralith:birch_taiga, terralith:cloud_forest, terralith:lavender_forest, terralith:lavender_valley, terralith:mirage_isles, terralith:moonlight_grove, terralith:moonlight_valley, terralith:sakura_valley, terralith:skylands, terralith:skylands_autumn, terralith:skylands_spring, terralith:temperate_highlands, terralith:white_cliffs, terralith:yosemite_lowlands

Structures: minecraft:mansion (+16)

### 9. forest (terralith) -> simply_houses

Add: terralith:alpha_islands, terralith:birch_taiga, terralith:lavender_forest, terralith:lavender_valley, terralith:mirage_isles, terralith:moonlight_grove, terralith:moonlight_valley, terralith:sakura_grove, terralith:sakura_valley, terralith:skylands, terralith:skylands_autumn, terralith:skylands_spring, terralith:temperate_highlands, terralith:white_cliffs, terralith:yosemite_lowlands

Structures: simply_houses:forest_chapel (+15), simply_houses:forest_fort (+15), simply_houses:forest_house (+15), simply_houses:forest_stonemason (+15)

### 10. forest (terralith) -> towns_and_towers

Add: terralith:alpha_islands, terralith:alpine_grove, terralith:birch_taiga, terralith:cloud_forest, terralith:lavender_forest, terralith:lavender_valley, terralith:mirage_isles, terralith:moonlight_grove, terralith:moonlight_valley, terralith:sakura_grove, terralith:sakura_valley, terralith:skylands, terralith:skylands_autumn, terralith:skylands_spring, terralith:temperate_highlands, terralith:white_cliffs, terralith:yosemite_lowlands

Structures: towns_and_towers:pillager_outpost_birch_forest (+17), towns_and_towers:pillager_outpost_flower_forest (+17), towns_and_towers:pillager_outpost_forest (+17), towns_and_towers:village_birch_forest (+17), towns_and_towers:village_flower_forest (+17), towns_and_towers:village_forest (+17)

### 11. jungle (terralith) -> betterjungletemples

Add: terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:cave/underground_jungle, terralith:jungle_mountains, terralith:skylands_summer

Structures: betterjungletemples:jungle_temple (+5)

### 12. jungle (terralith) -> minecraft

Add: terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:cave/underground_jungle, terralith:jungle_mountains, terralith:skylands_summer

Structures: minecraft:jungle_pyramid (+5), minecraft:ruined_portal_jungle (+2)

### 13. jungle (terralith) -> towns_and_towers

Add: terralith:amethyst_canyon, terralith:amethyst_rainforest, terralith:cave/underground_jungle, terralith:jungle_mountains, terralith:rocky_jungle, terralith:skylands_summer, terralith:tropical_jungle

Structures: towns_and_towers:pillager_outpost_jungle (+7), towns_and_towers:pillager_outpost_sparse_jungle (+7), towns_and_towers:village_jungle (+7), towns_and_towers:village_sparse_jungle (+7)

### 14. mushroom (quark, terralith) -> towns_and_towers

Add: quark:glimmering_weald, terralith:cave/fungal_caves, terralith:mirage_isles

Structures: towns_and_towers:pillager_outpost_mushroom_fields (+3), towns_and_towers:village_mushroom_fields (+3)

### 15. plains (terralith) -> towns_and_towers

Add: terralith:alpine_highlands, terralith:highlands, terralith:rocky_shrubland

Structures: towns_and_towers:pillager_outpost_sunflower_plains (+3), towns_and_towers:village_sunflower_plains (+3)

### 16. savanna (terralith) -> minecraft

Add: terralith:ashen_savanna, terralith:brushland, terralith:fractured_savanna, terralith:shrubland

Structures: minecraft:village_savanna (+4)

### 17. savanna (terralith) -> towns_and_towers

Add: terralith:arid_highlands, terralith:ashen_savanna, terralith:brushland, terralith:fractured_savanna, terralith:hot_shrubland, terralith:savanna_badlands, terralith:savanna_slopes, terralith:shrubland

Structures: towns_and_towers:pillager_outpost_savanna (+8)

### 18. snowy (terralith) -> minecraft

Add: terralith:alpha_islands_winter, terralith:alpine_grove, terralith:cold_shrubland, terralith:emerald_peaks, terralith:gravel_desert, terralith:ice_marsh, terralith:rocky_mountains, terralith:rocky_shrubland, terralith:siberian_grove, terralith:skylands_winter, terralith:snowy_maple_forest, terralith:snowy_shield, terralith:wintry_forest, terralith:wintry_lowlands

Structures: minecraft:igloo (+13), minecraft:village_snowy (+14)

### 19. snowy (terralith) -> towns_and_towers

Add: terralith:alpha_islands_winter, terralith:alpine_grove, terralith:cold_shrubland, terralith:emerald_peaks, terralith:gravel_desert, terralith:ice_marsh, terralith:rocky_mountains, terralith:rocky_shrubland, terralith:siberian_grove, terralith:skylands_winter, terralith:snowy_badlands, terralith:snowy_maple_forest, terralith:snowy_shield, terralith:wintry_forest, terralith:wintry_lowlands

Structures: towns_and_towers:pillager_outpost_grove (+15), towns_and_towers:pillager_outpost_snowy_beach (+15), towns_and_towers:pillager_outpost_snowy_plains (+15), towns_and_towers:pillager_outpost_snowy_slopes (+15), towns_and_towers:pillager_outpost_snowy_taiga (+15), towns_and_towers:village_grove (+15), towns_and_towers:village_snowy_slopes (+15), towns_and_towers:village_snowy_taiga (+15)

### 20. swamp (terralith) -> betterwitchhuts

Add: terralith:ice_marsh, terralith:orchid_swamp

Structures: betterwitchhuts:witch_circle (+2), betterwitchhuts:witch_hut (+2)

### 21. swamp (terralith) -> towns_and_towers

Add: terralith:ice_marsh, terralith:orchid_swamp

Structures: towns_and_towers:pillager_outpost_swamp (+2), towns_and_towers:village_swamp (+2)

### 22. taiga (terralith) -> simply_houses

Add: terralith:alpine_highlands, terralith:bryce_canyon, terralith:cloud_forest, terralith:scarlet_mountains, terralith:siberian_grove, terralith:siberian_taiga, terralith:skylands_winter, terralith:snowy_maple_forest, terralith:valley_clearing, terralith:wintry_lowlands

Structures: simply_houses:taiga_forge (+10), simply_houses:taiga_house (+10), simply_houses:taiga_inn (+10), simply_houses:taiga_woodcutter (+10)

### 23. taiga (terralith) -> towns_and_towers

Add: terralith:alpine_highlands, terralith:birch_taiga, terralith:bryce_canyon, terralith:cloud_forest, terralith:forested_highlands, terralith:lush_valley, terralith:scarlet_mountains, terralith:shield, terralith:siberian_grove, terralith:siberian_taiga, terralith:skylands_winter, terralith:snowy_maple_forest, terralith:valley_clearing, terralith:wintry_forest, terralith:wintry_lowlands, terralith:yellowstone, terralith:yosemite_lowlands

Structures: towns_and_towers:pillager_outpost_old_growth_taiga (+17), towns_and_towers:pillager_outpost_taiga (+17), towns_and_towers:village_old_growth_taiga (+17)
