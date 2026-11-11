# Structure Catalogue: family map preview

Preview only, generated 2026-11-09 from the live dumps through the real rule resolver. Nothing in the game or the rule file has changed. Compare with the current map, [structure_catalogue_map.md](structure_catalogue_map.md).

New order: **Category / Family / Biome / Mod (only when several mods share that biome) / slot**. Category and Family run along +X. Inside a family, Biome and Mod slots run along +X until the row is full, then the next row starts further along +Z. Each slot is one column of items along +Z.

Reading a line: A `**Row**` heading is one row of a family along +Z. `- <biome> (<count>): <items>` is one slot. `- <biome> / <mod> (<count>): <items>` is one slot of a biome that several mods share. `name{1-4}` is shorthand for numbered variations. `towns_and_towers` is also the `kaisyn` template namespace, counted as one mod.

## Summary

| | Current | Preview |
|---|---|---|
| Village levels on +X | Subfamily, function, biome | Family, biome, mod (if shared) |
| Village family count | 27 | 25 |
| Village columns (slots) | 479 | 485 |
| Village length along +X, estimated | 7507 | 2300 |
| Village depth along +Z, estimated | 680 | 661 |
| Village families on more than one row | 0 | 20 of 25 |
| Placed items | 1651 | 1651 (mapped 1607, unmapped 44, same as now) |

Row rule: a family wraps to a new row so that it stays within 661 blocks of depth, the depth of the longest single slot (Savanna roads), and a row is at least 60 blocks wide so several biomes sit side by side. Road stays on one row because its slots are already that deep. Gaps: Category 50, Family 10, slots in a row 5, between rows 10, items in a slot 5. The 10-block row gap holds the signs for the row below it. Sizes come from template files in the jars; 10 pieces inside nested jars are assumed 9 x 9. Only Village is measured here: other categories are mostly whole structures, whose size is known only when they generate, so they would wrap by the same rule during the build and are listed below without rows.

Changes the preview makes beyond reordering:

- Small, Medium, Large and Other homes are one **House** family.
- Town centres are named **Well and centre**. All 69 are kept together, because only a handful of pieces carry `well` or `fountain` in their names and the rest are meeting points.
- Mod job sites that the current rules file under Other homes (compost piles, vaults, electrician, gunsmith, outfitter, mechanic and bard buildings) move to **Workplace**, so House holds homes.
- Streets are named **Road**.
- **Stall** holds the 2 Wandering Trader Camp tents. No template in the pack has `stall`, `booth`, `market` or `kiosk` in its name.
- Biome names lose the Towns and Towers style word (`Badlands Pueblo` is `Badlands`) and the `Terralith` prefix, so Terralith desert pieces share Desert with vanilla and split by mod.
- Zombie variants stay separate biome slots (`Plains zombie`), except roads, as now.
- Other categories use the same order with Family being the old function, or the old subfamily where there was no function. Nether and End keep only one Decoration or Towers family, so Constructed and Environmental no longer split those.
- Fixed in the preview only: Sunflower Plains farms, windmill and meeting point were filed under Decor by a `flower_` match. They are now Farm, Workplace and Well and centre.

# Families

## Village (1292)

### House (292)

**Row 1** (164 wide, 244 deep)

- Badlands (21): `kaisyn:village/badlands_pueblo/houses/badlands_large_house_{1-4}`, `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_1_l`, `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_1_r`, `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_2_l`, `kaisyn:village/badlands_pueblo/houses/badlands_medium_house_2_r`, `kaisyn:village/badlands_pueblo/houses/badlands_misc_professions_1`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_1_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_1_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_2_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_2_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_3_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_3_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_4_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_4_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_5_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_5_r`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_6_l`, `kaisyn:village/badlands_pueblo/houses/badlands_small_house_6_r`
- Beach (3): `kaisyn:village/beach_lighthouse/side/beach_giant_cross_1`, `kaisyn:village/beach_lighthouse/side/beach_main_house_1`, `kaisyn:village/beach_lighthouse/side/beach_outdoor_shack_1`
- Birch Forest (9): `kaisyn:village/birch_forest_romanian/houses/birch_forest_large_house_1`, `kaisyn:village/birch_forest_romanian/houses/birch_forest_medium_house_{1-2}`, `kaisyn:village/birch_forest_romanian/houses/birch_forest_small_house_{1-6}`
- Classic (5): `kaisyn:village/exclusives/classic/houses/classic_large_house_1`, `kaisyn:village/exclusives/classic/houses/classic_medium_house_{1-2}`, `kaisyn:village/exclusives/classic/houses/classic_small_house_{1-2}`
- Desert / minecraft (10): `minecraft:village/desert/houses/desert_medium_house_{1-2}`, `minecraft:village/desert/houses/desert_small_house_{1-8}`
- Desert / terralith (9): `terralith:village/desert/houses/armorsmith1`, `terralith:village/desert/houses/castle`, `terralith:village/desert/houses/generic{1-6}`, `terralith:village/desert/houses/generic_big1`
- Desert zombie (10): `minecraft:village/desert/zombie/houses/desert_medium_house_{1-2}`, `minecraft:village/desert/zombie/houses/desert_small_house_{1-8}`
- Flower Forest (10): `kaisyn:village/flower_forest_japanese/houses/flower_forest_large_house_1`, `kaisyn:village/flower_forest_japanese/houses/flower_forest_medium_house_1`, `kaisyn:village/flower_forest_japanese/houses/flower_forest_park_{1-2}`, `kaisyn:village/flower_forest_japanese/houses/flower_forest_small_house_{1-6}`
- Forest (11): `kaisyn:village/forest_ruins/houses/forest_large_house_1`, `kaisyn:village/forest_ruins/houses/forest_medium_house_{1-2}`, `kaisyn:village/forest_ruins/houses/forest_small_house_{1-8}`

**Row 2** (191 wide, 252 deep)

- Fortified (14): `terralith:village/fortified/houses/archer`, `terralith:village/fortified/houses/big1`, `terralith:village/fortified/houses/generic{1-5}`, `terralith:village/fortified/houses/generic_big{1-4}`, `terralith:village/fortified/houses/graveyard`, `terralith:village/fortified/houses/large_house_rework`, `terralith:village/fortified/houses/smith1`
- Iberian (13): `kaisyn:village/exclusives/iberian/houses/iberian_large_house_{1-2}`, `kaisyn:village/exclusives/iberian/houses/iberian_medium_house_{1-8}`, `kaisyn:village/exclusives/iberian/houses/iberian_small_house_{1-3}`
- Jungle (5): `kaisyn:village/jungle_tribal/houses/jungle_large_house_1`, `kaisyn:village/jungle_tribal/houses/jungle_small_house_{1-4}`
- Meadow (8): `kaisyn:village/meadow_swiss/houses/meadow_large_house_1`, `kaisyn:village/meadow_swiss/houses/meadow_medium_house_{1-5}`, `kaisyn:village/meadow_swiss/houses/meadow_small_house_{1-2}`
- Mediterranean (12): `kaisyn:village/exclusives/mediterranean/houses/corner/med_small_house_{1-2}`, `kaisyn:village/exclusives/mediterranean/houses/regular/med_large_house_{1-3}`, `kaisyn:village/exclusives/mediterranean/houses/regular/med_medium_house_{1-2}`, `kaisyn:village/exclusives/mediterranean/houses/regular/med_small_house_{1-5}`
- Mushroom Fields (6): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_heavy_professions_gazebo`, `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_large_house_{1-2}`, `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_light_professions_gazebo`, `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_small_house_{1-2}`
- Nilotic (3): `kaisyn:village/exclusives/nilotic/houses/nilotic_large_house_1`, `kaisyn:village/exclusives/nilotic/houses/nilotic_small_house_{1-2}`
- Old Growth Taiga (8): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_medium_house_{1-2}`, `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_small_house_{1-6}`
- Plains (11): `minecraft:village/plains/houses/plains_big_house_1`, `minecraft:village/plains/houses/plains_medium_house_{1-2}`, `minecraft:village/plains/houses/plains_small_house_{1-8}`
- Plains zombie (11): `minecraft:village/plains/zombie/houses/plains_big_house_1`, `minecraft:village/plains/zombie/houses/plains_medium_house_{1-2}`, `minecraft:village/plains/zombie/houses/plains_small_house_{1-8}`
- Rustic (8): `kaisyn:village/exclusives/rustic/houses/rustic_large_house_{1-4}`, `kaisyn:village/exclusives/rustic/houses/rustic_small_house_{1-4}`

**Row 3** (189 wide, 145 deep)

- Savanna (10): `minecraft:village/savanna/houses/savanna_medium_house_{1-2}`, `minecraft:village/savanna/houses/savanna_small_house_{1-8}`
- Savanna zombie (10): `minecraft:village/savanna/zombie/houses/savanna_medium_house_{1-2}`, `minecraft:village/savanna/zombie/houses/savanna_small_house_{1-8}`
- Snowy (11): `minecraft:village/snowy/houses/snowy_medium_house_{1-3}`, `minecraft:village/snowy/houses/snowy_small_house_{1-8}`
- Snowy Taiga (5): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_small_house_{1-5}`
- Snowy zombie (11): `minecraft:village/snowy/zombie/houses/snowy_medium_house_{1-3}`, `minecraft:village/snowy/zombie/houses/snowy_small_house_{1-8}`
- Sparse Jungle (8): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_large_house_1`, `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_medium_house_{1-3}`, `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_small_house_{1-4}`
- Swamp (9): `kaisyn:village/swamp_boat/houses/swamp_b_large_house_{1-2}`, `kaisyn:village/swamp_boat/houses/swamp_b_medium_house_{1-4}`, `kaisyn:village/swamp_boat/houses/swamp_b_small_house_{1-3}`
- Swedish (5): `kaisyn:village/exclusives/swedish/houses/swedish_small_house_{1-5}`
- Taiga (9): `minecraft:village/taiga/houses/taiga_medium_house_{1-4}`, `minecraft:village/taiga/houses/taiga_small_house_{1-5}`
- Taiga zombie (9): `minecraft:village/taiga/zombie/houses/taiga_medium_house_{1-4}`, `minecraft:village/taiga/zombie/houses/taiga_small_house_{1-5}`
- Tudor (5): `kaisyn:village/exclusives/tudor/houses/tudor_large_house_1`, `kaisyn:village/exclusives/tudor/houses/tudor_small_house_{1-4}`
- Wooded Badlands (12): `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_black_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_blue_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_brown_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_green_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_red_1`, `kaisyn:village/wooded_badlands_tipi/houses/large_huts/wooded_badlands_large_hut_yellow_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_black_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_blue_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_brown_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_green_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_red_1`, `kaisyn:village/wooded_badlands_tipi/houses/small_huts/wooded_badlands_small_hut_yellow_1`
- Any (1): `dustrial_decor:village/insane_house`

### Armorer (24)

**Row 1** (52 wide, 9 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/houses/badlands_armorer_1`
- Birch Forest (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_armorer_and_toolsmith_1`
- Desert (1): `minecraft:village/desert/houses/desert_armorer_1`
- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_armorer_1`

**Row 2** (59 wide, 10 deep)

- Forest (1): `kaisyn:village/forest_ruins/houses/forest_armorer_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_armorer_1`
- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_armorer_1`
- Meadow (1): `kaisyn:village/meadow_swiss/houses/meadow_armorer_and_leatherworker_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_armorer_and_weaponsmith_1`

**Row 3** (59 wide, 10 deep)

- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/heavy_professions/mushroom_armorer_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_armorer_1`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_armorer_and_butcher_1`
- Plains (1): `minecraft:village/plains/houses/plains_armorer_house_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_armorer_1`

**Row 4** (47 wide, 19 deep)

- Savanna (1): `minecraft:village/savanna/houses/savanna_armorer_1`
- Snowy (2): `minecraft:village/snowy/houses/snowy_armorer_house_{1-2}`
- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_armorer_1`
- Sparse Jungle (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_armorer_and_mason_1`

**Row 5** (53 wide, 19 deep)

- Swamp (1): `kaisyn:village/swamp_boat/houses/swamp_b_armorer_and_toolsmith_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_armorer_and_mason_1`
- Taiga (2): `minecraft:village/taiga/houses/taiga_armorer_2`, `minecraft:village/taiga/houses/taiga_armorer_house_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_butcher_and_armorer_1`

### Butcher (28)

**Row 1** (54 wide, 11 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_butcher_1`
- Birch Forest (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_butcher_and_leatherworker_1`
- Classic (1): `kaisyn:village/exclusives/classic/houses/classic_butcher_1`
- Desert / minecraft (1): `minecraft:village/desert/houses/desert_butcher_shop_1`

**Row 2** (47 wide, 24 deep)

- Desert / terralith (1): `terralith:village/desert/houses/butcher1`
- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_butcher_1`
- Forest (2): `kaisyn:village/forest_ruins/houses/forest_butcher_{1-2}`

**Row 3** (56 wide, 17 deep)

- Fortified (1): `terralith:village/fortified/houses/butcher`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_butcher_1`
- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_butcher_1`
- Meadow (1): `kaisyn:village/meadow_swiss/houses/meadow_butcher_and_mason_1`

**Row 4** (54 wide, 24 deep)

- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_butcher_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_butcher_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_butcher_and_fisher_1`
- Plains (2): `minecraft:village/plains/houses/plains_butcher_shop_{1-2}`

**Row 5** (57 wide, 25 deep)

- Plains zombie (1): `minecraft:village/plains/zombie/houses/plains_butcher_shop_2`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_butcher_1`
- Savanna (2): `minecraft:village/savanna/houses/savanna_butchers_shop_{1-2}`
- Snowy (2): `minecraft:village/snowy/houses/snowy_butchers_shop_{1-2}`

**Row 6** (50 wide, 11 deep)

- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_butcher_1`
- Sparse Jungle (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_butcher_and_shepherd_1`
- Swamp (1): `kaisyn:village/swamp_boat/houses/swamp_b_butcher_and_mason_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_butcher_1`

**Row 7** (11 wide, 9 deep)

- Taiga (1): `minecraft:village/taiga/houses/taiga_butcher_shop_1`

### Cartographer (26)

**Row 1** (58 wide, 35 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/houses/badlands_cartographer_1`
- Birch Forest (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_cartographer_and_library_1`
- Desert / minecraft (1): `minecraft:village/desert/houses/desert_cartographer_house_1`
- Desert / terralith (2): `terralith:village/desert/houses/cartographer{1-2}`

**Row 2** (56 wide, 18 deep)

- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_cartographer_1`
- Forest (1): `kaisyn:village/forest_ruins/houses/forest_cartographer_1`
- Fortified (1): `terralith:village/fortified/houses/cartographer`

**Row 3** (55 wide, 13 deep)

- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_cartographer_1`
- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_cartographer_1`
- Meadow (1): `kaisyn:village/meadow_swiss/houses/meadow_cartographer_and_library_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_cartographer_1`

**Row 4** (58 wide, 10 deep)

- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/light_professions/mushroom_cartographer_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_cartographer_1`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_cartographer_1`
- Plains (1): `minecraft:village/plains/houses/plains_cartographer_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_cartographer_1`

**Row 5** (48 wide, 11 deep)

- Savanna (1): `minecraft:village/savanna/houses/savanna_cartographer_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_cartographer_house_1`
- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_cartographer_1`
- Sparse Jungle (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_cartographer_and_library_1`

**Row 6** (59 wide, 11 deep)

- Swamp (1): `kaisyn:village/swamp_boat/houses/swamp_b_cartographer_and_shepherd_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_cartographer_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_cartographer_house_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_cartographer_house_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_cartographer_and_fletcher_1`

### Farmer (9)

**Row 1** (59 wide, 11 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_farmer_1`
- Fortified (1): `terralith:village/fortified/houses/farmer`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_farmer_1`
- Meadow (1): `kaisyn:village/meadow_swiss/houses/meadow_farmer_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_farmer_and_mason_1`

**Row 2** (44 wide, 11 deep)

- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_farmer_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_farmer_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_farmer_and_fisher_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_farmer_1`

### Fisher (24)

**Row 1** (58 wide, 33 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_fisher_1`
- Birch Forest (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_fisher_1`
- Desert / minecraft (1): `minecraft:village/desert/houses/desert_fisher_1`
- Desert / terralith (2): `terralith:village/desert/houses/fisherman{1-2}`

**Row 2** (53 wide, 15 deep)

- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_fisher_1`
- Forest (1): `kaisyn:village/forest_ruins/houses/forest_fisher_1`
- Fortified (1): `terralith:village/fortified/houses/fisherman`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_fisher_1`

**Row 3** (60 wide, 13 deep)

- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_fisher_1`
- Meadow (1): `kaisyn:village/meadow_swiss/houses/meadow_fisher_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_fisher_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/light_professions/mushroom_fisher_1`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_fisher_1`

**Row 4** (48 wide, 10 deep)

- Plains (1): `minecraft:village/plains/houses/plains_fisher_cottage_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_fisher_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_fisher_cottage_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_fisher_cottage`

**Row 5** (49 wide, 12 deep)

- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_fisher_1`
- Sparse Jungle (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_fisher_and_fletcher_1`
- Swamp (1): `kaisyn:village/swamp_boat/houses/swamp_b_fisher_and_leatherworker_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_fisher_cottage_1`

**Row 6** (23 wide, 12 deep)

- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_fisher_cottage_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_fisher_1`

### Fletcher (22)

**Row 1** (50 wide, 13 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_fletcher_1`
- Birch Forest (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_fletcher_1`
- Desert / minecraft (1): `minecraft:village/desert/houses/desert_fletcher_house_1`
- Desert / terralith (1): `terralith:village/desert/houses/fletcher1`

**Row 2** (60 wide, 13 deep)

- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_fletcher_1`
- Forest (1): `kaisyn:village/forest_ruins/houses/forest_fletcher_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_fletcher_1`
- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_fletcher_1`
- Meadow (1): `kaisyn:village/meadow_swiss/houses/meadow_fletcher_1`

**Row 3** (57 wide, 11 deep)

- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_fletcher_and_toolsmith_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/light_professions/mushroom_fletcher_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_fletcher_1`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_fletcher_1`
- Plains (1): `minecraft:village/plains/houses/plains_fletcher_house_1`

**Row 4** (49 wide, 11 deep)

- Plains zombie (1): `minecraft:village/plains/zombie/houses/plains_fletcher_house_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_fletcher_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_fletcher_house_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_fletcher_house_1`

**Row 5** (50 wide, 11 deep)

- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_fletcher_1`
- Swamp (1): `kaisyn:village/swamp_boat/houses/swamp_b_fletcher_and_library_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_fletcher_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_fletcher_house_1`

### Leatherworker (20)

**Row 1** (50 wide, 15 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_leatherworker_1`
- Desert / minecraft (1): `minecraft:village/desert/houses/desert_tannery_1`
- Desert / terralith (1): `terralith:village/desert/houses/tanner1`
- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_leatherworker_1`

**Row 2** (60 wide, 12 deep)

- Forest (1): `kaisyn:village/forest_ruins/houses/forest_leatherworker_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_leatherworker_1`
- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_leatherworker_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_leatherworker_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_leatherworker_1`

**Row 3** (48 wide, 10 deep)

- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_leatherworker_and_shepherd_1`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_leatherworker_1`
- Plains (1): `minecraft:village/plains/houses/plains_tannery_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_leatherworker_1`

**Row 4** (58 wide, 11 deep)

- Savanna (1): `minecraft:village/savanna/houses/savanna_tannery_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_tannery_1`
- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_leatherworker_1`
- Sparse Jungle (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_leatherworker_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_leatherworker_1`

**Row 5** (22 wide, 9 deep)

- Taiga (1): `minecraft:village/taiga/houses/taiga_tannery_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_leatherworker_1`

### Librarian (21)

**Row 1** (58 wide, 23 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/houses/badlands_library_1`
- Classic (1): `kaisyn:village/exclusives/classic/houses/classic_library_1`
- Desert / minecraft (1): `minecraft:village/desert/houses/desert_library_1`
- Desert / terralith (1): `terralith:village/desert/houses/library1`

**Row 2** (52 wide, 21 deep)

- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_library_1`
- Forest (1): `kaisyn:village/forest_ruins/houses/forest_library_1`
- Fortified (1): `terralith:village/fortified/houses/library`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_librarian_1`

**Row 3** (47 wide, 15 deep)

- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_library_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_library_1`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_library_1`

**Row 4** (54 wide, 31 deep)

- Plains (2): `minecraft:village/plains/houses/plains_library_{1-2}`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_librarian_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_library_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_library_1`

**Row 5** (56 wide, 10 deep)

- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_library_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_library_and_temple_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_library_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_library_1`

**Row 6** (9 wide, 9 deep)

- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_library_1`

### Mason (22)

**Row 1** (54 wide, 18 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/houses/badlands_mason_1`
- Birch Forest (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_mason_1`
- Desert / minecraft (1): `minecraft:village/desert/houses/desert_mason_1`
- Desert / terralith (1): `terralith:village/desert/houses/mason1`

**Row 2** (50 wide, 21 deep)

- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_mason_1`
- Forest (1): `kaisyn:village/forest_ruins/houses/forest_mason_1`
- Fortified (1): `terralith:village/fortified/houses/mason`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_mason_1`

**Row 3** (49 wide, 10 deep)

- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_mason_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/heavy_professions/mushroom_mason_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_mason_1`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_mason_1`

**Row 4** (58 wide, 23 deep)

- Plains (1): `minecraft:village/plains/houses/plains_masons_house_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_mason_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_mason_1`
- Snowy (2): `minecraft:village/snowy/houses/snowy_masons_house_{1-2}`
- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_mason_1`

**Row 5** (34 wide, 24 deep)

- Taiga (1): `minecraft:village/taiga/houses/taiga_masons_house_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_mason_1`
- Any (2): `dustrial_decor:village/mason`, `dustrial_decor:village/mason_2`

### Shepherd (23)

**Row 1** (38 wide, 7 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/professions_misc/badlands_shepherd_1`
- Birch Forest (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_shepherd_1`
- Desert / minecraft (1): `minecraft:village/desert/houses/desert_shepherd_house_1`

**Row 2** (45 wide, 14 deep)

- Desert / terralith (1): `terralith:village/desert/houses/shepherd1`
- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_shepherd_1`
- Forest (1): `kaisyn:village/forest_ruins/houses/forest_shepherd_1`

**Row 3** (50 wide, 13 deep)

- Fortified (1): `terralith:village/fortified/houses/shepherd`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_shepherd_1`
- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_shepherd_1`
- Meadow (1): `kaisyn:village/meadow_swiss/houses/meadow_shepherd_1`

**Row 4** (50 wide, 13 deep)

- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/houses/regular/med_shepherd_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/light_professions/mushroom_shepherd_1`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_shepherd_1`
- Plains (1): `minecraft:village/plains/houses/plains_shepherds_house_1`

**Row 5** (51 wide, 13 deep)

- Plains zombie (1): `minecraft:village/plains/zombie/houses/plains_shepherds_house_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_shepherd_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_shepherd_1`
- Snowy (1): `minecraft:village/snowy/houses/snowy_shepherds_house_1`

**Row 6** (60 wide, 11 deep)

- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_shepherd_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_shepherd_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_shepherds_house_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_shepherds_house_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_shepherd_1`

### Toolsmith (21)

**Row 1** (57 wide, 13 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/houses/badlands_toolsmith_and_weaponsmith_1`
- Desert (1): `minecraft:village/desert/houses/desert_tool_smith_1`
- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_toolsmith_1`
- Forest (1): `kaisyn:village/forest_ruins/houses/forest_toolsmith_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_toolsmith_1`

**Row 2** (48 wide, 13 deep)

- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_toolsmith_1`
- Meadow (1): `kaisyn:village/meadow_swiss/houses/meadow_toolsmith_and_weaponsmith_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/heavy_professions/mushroom_toolsmith_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_toolsmith_and_weaponsmith_1`

**Row 3** (49 wide, 12 deep)

- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_toolsmith_and_weaponsmith_1`
- Plains (1): `minecraft:village/plains/houses/plains_tool_smith_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_toolsmith_1`
- Savanna (1): `minecraft:village/savanna/houses/savanna_tool_smith_1`

**Row 4** (47 wide, 9 deep)

- Snowy (1): `minecraft:village/snowy/houses/snowy_tool_smith_1`
- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_toolsmith_and_weaponsmith_1`
- Sparse Jungle (1): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_toolsmith_and_weaponsmith_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/houses/swedish_toolsmith_and_weaponsmith_1`

**Row 5** (56 wide, 10 deep)

- Taiga (1): `minecraft:village/taiga/houses/taiga_tool_smith_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_tool_smith_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_toolsmith_and_weaponsmith_1`
- Any (1): `dustrial_decor:village/tool_smith`

### Weaponsmith (18)

**Row 1** (43 wide, 12 deep)

- Birch Forest (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_weaponsmith_1`
- Classic (1): `kaisyn:village/exclusives/classic/houses/classic_blacksmith_1`
- Desert / minecraft (1): `minecraft:village/desert/houses/desert_weaponsmith_1`

**Row 2** (51 wide, 16 deep)

- Desert / terralith (1): `terralith:village/desert/houses/weaponsmith1`
- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_weaponsmith_1`
- Forest (1): `kaisyn:village/forest_ruins/houses/forest_weaponsmith_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/professions_misc/iberian_weaponsmith_1`

**Row 3** (58 wide, 27 deep)

- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_weaponsmith_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/professions_misc/heavy_professions/mushroom_weaponsmith_1`
- Plains (1): `minecraft:village/plains/houses/plains_weaponsmith_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_weaponsmith_1`
- Savanna (2): `minecraft:village/savanna/houses/savanna_weaponsmith_{1-2}`

**Row 4** (46 wide, 19 deep)

- Snowy (1): `minecraft:village/snowy/houses/snowy_weapon_smith_1`
- Swamp (1): `kaisyn:village/swamp_boat/houses/swamp_b_weaponsmith_1`
- Taiga (2): `minecraft:village/taiga/houses/taiga_weaponsmith_{1-2}`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_weaponsmith_2`

### Road (428)

**Row 1** (704 wide, 661 deep)

- Badlands (6): `kaisyn:village/badlands_pueblo/streets/crossroad_{1-3}`, `kaisyn:village/badlands_pueblo/streets/straight_1`, `kaisyn:village/badlands_pueblo/terminators/terminator_{1-2}`
- Birch Forest (20): `kaisyn:village/birch_forest_romanian/streets/corner_{1-3}`, `kaisyn:village/birch_forest_romanian/streets/crossroad_{1-6}`, `kaisyn:village/birch_forest_romanian/streets/straight_{1-6}`, `kaisyn:village/birch_forest_romanian/streets/turn_1`, `kaisyn:village/birch_forest_romanian/terminators/terminator_{1-4}`
- Classic (6): `kaisyn:village/exclusives/classic/streets/crossroad_{1-3}`, `kaisyn:village/exclusives/classic/streets/straight_{1-3}`
- Desert / minecraft (25): `minecraft:village/desert/streets/corner_{1-2}`, `minecraft:village/desert/streets/crossroad_{1-3}`, `minecraft:village/desert/streets/square_{1-2}`, `minecraft:village/desert/streets/straight_{1-3}`, `minecraft:village/desert/streets/turn_1`, `minecraft:village/desert/terminators/terminator_{1-2}`, `minecraft:village/desert/zombie/streets/corner_{1-2}`, `minecraft:village/desert/zombie/streets/crossroad_{1-3}`, `minecraft:village/desert/zombie/streets/square_{1-2}`, `minecraft:village/desert/zombie/streets/straight_{1-3}`, `minecraft:village/desert/zombie/streets/turn_1`, `minecraft:village/desert/zombie/terminators/terminator_2`
- Desert / monobank (1): `monobank:village/streets/desert_straight_fix_1`
- Desert / pneumaticcraft (1): `pneumaticcraft:villages/custom_street_desert`
- Desert / terralith (15): `terralith:village/desert/roads/bend{1-2}`, `terralith:village/desert/roads/inter1`, `terralith:village/desert/roads/straight{1-6}`, `terralith:village/desert/roads/t{1-2}`, `terralith:village/desert/terminators/term{1-4}`
- Flower Forest (20): `kaisyn:village/flower_forest_japanese/streets/corner_{1-3}`, `kaisyn:village/flower_forest_japanese/streets/crossroad_{1-6}`, `kaisyn:village/flower_forest_japanese/streets/straight_{1-6}`, `kaisyn:village/flower_forest_japanese/streets/turn_1`, `kaisyn:village/flower_forest_japanese/terminators/terminator_{1-4}`
- Forest (20): `kaisyn:village/forest_ruins/streets/corner_{1-3}`, `kaisyn:village/forest_ruins/streets/crossroad_{1-6}`, `kaisyn:village/forest_ruins/streets/straight_{1-6}`, `kaisyn:village/forest_ruins/streets/turn_1`, `kaisyn:village/forest_ruins/terminators/terminator_{1-4}`
- Fortified (13): `terralith:village/fortified/roads/bend{1-2}`, `terralith:village/fortified/roads/inter{1-2}`, `terralith:village/fortified/roads/intersection_big`, `terralith:village/fortified/roads/straight{1-2}`, `terralith:village/fortified/roads/t{1-2}`, `terralith:village/fortified/terminators/term{1-3,5}`
- Iberian (18): `kaisyn:village/exclusives/iberian/streets/corner_{1-4}`, `kaisyn:village/exclusives/iberian/streets/crossroad_{1-4}`, `kaisyn:village/exclusives/iberian/streets/straight_{1-4}`, `kaisyn:village/exclusives/iberian/terminators/terminator_{1-6}`
- Jungle (20): `kaisyn:village/jungle_tribal/streets/corner_{1-3}`, `kaisyn:village/jungle_tribal/streets/crossroad_{1-6}`, `kaisyn:village/jungle_tribal/streets/straight_{1-6}`, `kaisyn:village/jungle_tribal/streets/turn_1`, `kaisyn:village/jungle_tribal/terminators/terminator_{1-4}`
- Meadow (15): `kaisyn:village/meadow_swiss/streets/corner_{1-3}`, `kaisyn:village/meadow_swiss/streets/crossroad_{1-4}`, `kaisyn:village/meadow_swiss/streets/straight_{1-5}`, `kaisyn:village/meadow_swiss/terminators/terminator_{1-3}`
- Mediterranean (12): `kaisyn:village/exclusives/mediterranean/streets/avenue/avenue_{1-5}`, `kaisyn:village/exclusives/mediterranean/streets/promenade/promenade_{1-3}`, `kaisyn:village/exclusives/mediterranean/terminators/avenue/avenue_end_{1-2}`, `kaisyn:village/exclusives/mediterranean/terminators/promenade/promenade_end_{1-2}`
- Mushroom Fields (6): `kaisyn:village/mushroom_fields_fantasy/streets/crossroad_1`, `kaisyn:village/mushroom_fields_fantasy/streets/straight_{1-3}`, `kaisyn:village/mushroom_fields_fantasy/streets/turn_1`, `kaisyn:village/mushroom_fields_fantasy/terminators/terminator_1`
- Old Growth Taiga (8): `kaisyn:village/old_growth_taiga_polish/streets/corner_{1-2}`, `kaisyn:village/old_growth_taiga_polish/streets/crossroad_{1-2}`, `kaisyn:village/old_growth_taiga_polish/streets/straight_{1-2}`, `kaisyn:village/old_growth_taiga_polish/terminators/terminator_{1-2}`
- Plains / minecraft (36): `minecraft:village/plains/streets/corner_{1-3}`, `minecraft:village/plains/streets/crossroad_{1-6}`, `minecraft:village/plains/streets/straight_{1-6}`, `minecraft:village/plains/streets/turn_1`, `minecraft:village/plains/terminators/terminator_{1-4}`, `minecraft:village/plains/zombie/streets/corner_{1-3}`, `minecraft:village/plains/zombie/streets/crossroad_{1-6}`, `minecraft:village/plains/zombie/streets/straight_{1-6}`, `minecraft:village/plains/zombie/streets/turn_1`
- Plains / monobank (1): `monobank:village/streets/plains_straight_fix_1`
- Rustic (14): `kaisyn:village/exclusives/rustic/streets/corner_{1-3}`, `kaisyn:village/exclusives/rustic/streets/crossroad_{1-4}`, `kaisyn:village/exclusives/rustic/streets/straight_{1-5}`, `kaisyn:village/exclusives/rustic/terminators/terminator_{1-2}`
- Savanna / minecraft (40): `minecraft:village/savanna/streets/corner_{1,3}`, `minecraft:village/savanna/streets/crossroad_{2-7}`, `minecraft:village/savanna/streets/split_{1-2}`, `minecraft:village/savanna/streets/straight_{2,4-6,8-11}`, `minecraft:village/savanna/streets/turn_1`, `minecraft:village/savanna/terminators/terminator_5`, `minecraft:village/savanna/zombie/streets/corner_{1,3}`, `minecraft:village/savanna/zombie/streets/crossroad_{2-7}`, `minecraft:village/savanna/zombie/streets/split_{1-2}`, `minecraft:village/savanna/zombie/streets/straight_{2,4-6,8-11}`, `minecraft:village/savanna/zombie/streets/turn_1`, `minecraft:village/savanna/zombie/terminators/terminator_5`
- Savanna / monobank (1): `monobank:village/streets/savanna_straight_fix_1`
- Snowy / minecraft (34): `minecraft:village/snowy/streets/corner_{1-3}`, `minecraft:village/snowy/streets/crossroad_{1-6}`, `minecraft:village/snowy/streets/square_1`, `minecraft:village/snowy/streets/straight_{1-4,6,8}`, `minecraft:village/snowy/streets/turn_1`, `minecraft:village/snowy/zombie/streets/corner_{1-3}`, `minecraft:village/snowy/zombie/streets/crossroad_{1-6}`, `minecraft:village/snowy/zombie/streets/square_1`, `minecraft:village/snowy/zombie/streets/straight_{1-4,6,8}`, `minecraft:village/snowy/zombie/streets/turn_1`
- Snowy / monobank (1): `monobank:village/streets/snowy_straight_fix_1`
- Snowy / pneumaticcraft (1): `pneumaticcraft:villages/custom_street_snowy`
- Snowy Taiga (20): `kaisyn:village/snowy_taiga_viking/streets/corner_{1-3}`, `kaisyn:village/snowy_taiga_viking/streets/crossroad_{1-6}`, `kaisyn:village/snowy_taiga_viking/streets/straight_{1-6}`, `kaisyn:village/snowy_taiga_viking/streets/turn_1`, `kaisyn:village/snowy_taiga_viking/terminators/terminator_{1-4}`
- Sparse Jungle (10): `kaisyn:village/sparse_jungle_polynesian/streets/corner_{1-2}`, `kaisyn:village/sparse_jungle_polynesian/streets/crossroad_{1-3}`, `kaisyn:village/sparse_jungle_polynesian/streets/straight_{1-4}`, `kaisyn:village/sparse_jungle_polynesian/terminators/terminator_1`
- Sunflower Plains (1): `kaisyn:village/sunflower_plains_farm/streets/turn_1`
- Swamp (9): `kaisyn:village/swamp_boat/streets/corner_1`, `kaisyn:village/swamp_boat/streets/crossroad_{1-2}`, `kaisyn:village/swamp_boat/streets/straight_{1-2}`, `kaisyn:village/swamp_boat/streets/turn_1`, `kaisyn:village/swamp_boat/terminators/terminator_{1-3}`
- Swedish (13): `kaisyn:village/exclusives/swedish/streets/corner_{1-3}`, `kaisyn:village/exclusives/swedish/streets/crossroad_{1-4}`, `kaisyn:village/exclusives/swedish/streets/straight_{1-5}`, `kaisyn:village/exclusives/swedish/terminators/terminator_1`
- Taiga / minecraft (32): `minecraft:village/taiga/streets/corner_{1-3}`, `minecraft:village/taiga/streets/crossroad_{1-6}`, `minecraft:village/taiga/streets/straight_{1-6}`, `minecraft:village/taiga/streets/turn_1`, `minecraft:village/taiga/zombie/streets/corner_{1-3}`, `minecraft:village/taiga/zombie/streets/crossroad_{1-6}`, `minecraft:village/taiga/zombie/streets/straight_{1-6}`, `minecraft:village/taiga/zombie/streets/turn_1`
- Taiga / monobank (1): `monobank:village/streets/taiga_straight_fix_1`
- Tudor (8): `kaisyn:village/exclusives/tudor/streets/corner_{1-2}`, `kaisyn:village/exclusives/tudor/streets/crossroad_{1-2}`, `kaisyn:village/exclusives/tudor/streets/straight_{1-2}`, `kaisyn:village/exclusives/tudor/terminators/terminator_{1-2}`

### Stall (2)

**Row 1** (8 wide, 19 deep)

- Wandering Trader Camp (2): `kaisyn:village/exclusives/wandering_trader_camp/side/trader_tent_blue_1`, `kaisyn:village/exclusives/wandering_trader_camp/side/trader_tent_red_1`

### Animal pen (36)

**Row 1** (50 wide, 59 deep)

- Desert / incubation (1): `incubation:village/desert_chicken_coop_1`
- Desert / minecraft (2): `minecraft:village/desert/houses/desert_animal_pen_{1-2}`
- Flower Forest (4): `kaisyn:village/flower_forest_japanese/houses/flower_forest_animal_pen_{1-3}`, `kaisyn:village/flower_forest_japanese/houses/flower_forest_stable_1`
- Forest (4): `kaisyn:village/forest_ruins/houses/forest_animal_pen_{1-3}`, `kaisyn:village/forest_ruins/houses/forest_stable_1`

**Row 2** (53 wide, 81 deep)

- Fortified (2): `terralith:village/fortified/houses/barn`, `terralith:village/fortified/houses/stable`
- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_animal_pen_1`
- Plains / incubation (1): `incubation:village/plains_chicken_coop_1`
- Plains / minecraft (5): `minecraft:village/plains/houses/plains_animal_pen_{1-3}`, `minecraft:village/plains/houses/plains_stable_{1-2}`

**Row 3** (55 wide, 40 deep)

- Plains zombie (2): `minecraft:village/plains/zombie/houses/plains_animal_pen_3`, `minecraft:village/plains/zombie/houses/plains_stable_1`
- Rustic (1): `kaisyn:village/exclusives/rustic/houses/rustic_barn_professions_1`
- Savanna / incubation (1): `incubation:village/savanna_chicken_coop_1`
- Savanna / minecraft (3): `minecraft:village/savanna/houses/savanna_animal_pen_{1-3}`

**Row 4** (52 wide, 26 deep)

- Savanna zombie (2): `minecraft:village/savanna/zombie/houses/savanna_animal_pen_{2-3}`
- Snowy / incubation (1): `incubation:village/snowy_chicken_coop_1`
- Snowy / minecraft (2): `minecraft:village/snowy/houses/snowy_animal_pen_{1-2}`
- Taiga / incubation (1): `incubation:village/taiga_chicken_coop_1`

**Row 5** (22 wide, 15 deep)

- Taiga / minecraft (1): `minecraft:village/taiga/houses/taiga_animal_pen_1`
- Wandering Trader Camp (2): `kaisyn:village/exclusives/wandering_trader_camp/side/trader_llama_pen_{1-2}`

### Well and centre (69)

**Row 1** (80 wide, 34 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/town_centers/badlands_meeting_point_1`
- Beach (1): `kaisyn:village/beach_lighthouse/town_centers/beach_meeting_point_1`
- Birch Forest (1): `kaisyn:village/birch_forest_romanian/town_centers/birch_forest_meeting_point_1`

**Row 2** (74 wide, 46 deep)

- Classic (2): `kaisyn:village/exclusives/classic/town_centers/classic_meeting_point_1/well_bottom`, `kaisyn:village/exclusives/classic/town_centers/classic_meeting_point_1/well_top`
- Desert (3): `minecraft:village/desert/town_centers/desert_meeting_point_{1-3}`
- Desert zombie (3): `minecraft:village/desert/zombie/town_centers/desert_meeting_point_{1-3}`
- Flower Forest (2): `kaisyn:village/flower_forest_japanese/town_centers/flower_forest_meeting_point_{1-2}`

**Row 3** (72 wide, 31 deep)

- Forest (2): `kaisyn:village/forest_ruins/town_centers/forest_meeting_point_{1-2}`
- Fortified (1): `terralith:village/fortified/start_castle`
- Grove (1): `kaisyn:village/grove_villager_outpost/town_centers/grove_meeting_point_1`

**Row 4** (52 wide, 44 deep)

- Iberian (1): `kaisyn:village/exclusives/iberian/town_centers/iberian_meeting_point_1`
- Jungle (3): `kaisyn:village/jungle_tribal/town_centers/jungle_meeting_point_{1-3}`
- Meadow (1): `kaisyn:village/meadow_swiss/town_centers/meadow_meeting_point_1`

**Row 5** (45 wide, 23 deep)

- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/town_centers/med_meeting_point_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/town_centers/mushroom_meeting_point_1`

**Row 6** (80 wide, 80 deep)

- Nilotic (1): `kaisyn:village/exclusives/nilotic/base_plate`

**Row 7** (67 wide, 97 deep)

- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/town_centers/old_growth_taiga_meeting_point_1`
- Plains (6): `minecraft:village/plains/houses/plains_meeting_point_{4-5}`, `minecraft:village/plains/town_centers/plains_fountain_1`, `minecraft:village/plains/town_centers/plains_meeting_point_{1-3}`
- Plains zombie (6): `minecraft:village/plains/zombie/houses/plains_meeting_point_{4-5}`, `minecraft:village/plains/zombie/town_centers/plains_fountain_1`, `minecraft:village/plains/zombie/town_centers/plains_meeting_point_{1-3}`
- Rustic (1): `kaisyn:village/exclusives/rustic/town_centers/rustic_meeting_point_1`

**Row 8** (76 wide, 58 deep)

- Savanna (4): `minecraft:village/savanna/town_centers/savanna_meeting_point_{1-4}`
- Savanna zombie (4): `minecraft:village/savanna/zombie/town_centers/savanna_meeting_point_{1-4}`
- Snowy (3): `minecraft:village/snowy/town_centers/snowy_meeting_point_{1-3}`
- Snowy Slopes (1): `kaisyn:village/snowy_slopes_inn/town_centers/snowy_slopes_meeting_point_1`

**Row 9** (77 wide, 34 deep)

- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/town_centers/snowy_taiga_meeting_point_1`
- Snowy zombie (3): `minecraft:village/snowy/zombie/town_centers/snowy_meeting_point_{1-3}`
- Sparse Jungle (1): `kaisyn:village/sparse_jungle_polynesian/town_centers/sparse_jungle_meeting_point_1`
- Sunflower Plains (1): `kaisyn:village/sunflower_plains_farm/town_centers/sunflower_plains_meeting_point_1`

**Row 10** (66 wide, 32 deep)

- Swamp (1): `kaisyn:village/swamp_boat/town_centers/swamp_b_meeting_point_1`
- Swedish (1): `kaisyn:village/exclusives/swedish/town_centers/swedish_meeting_point_1`
- Taiga (2): `minecraft:village/taiga/town_centers/taiga_meeting_point_{1-2}`

**Row 11** (73 wide, 32 deep)

- Taiga zombie (2): `minecraft:village/taiga/zombie/town_centers/taiga_meeting_point_{1-2}`
- Tudor (1): `kaisyn:village/exclusives/tudor/town_centers/tudor_meeting_point_1`
- Wandering Trader Camp (1): `kaisyn:village/exclusives/wandering_trader_camp/town_centers/trader_meeting_point_1`
- Wooded Badlands (3): `kaisyn:village/wooded_badlands_tipi/town_centers/wooded_badlands_meeting_point_{1-3}`
- Any (1): `minecraft:village/common/well_bottom`

### Temple (24)

**Row 1** (40 wide, 27 deep)

- Badlands (1): `kaisyn:village/badlands_pueblo/houses/badlands_temple_1`
- Classic (1): `kaisyn:village/exclusives/classic/houses/classic_church_1`
- Desert / minecraft (2): `minecraft:village/desert/houses/desert_temple_{1-2}`

**Row 2** (55 wide, 17 deep)

- Desert / terralith (1): `terralith:village/desert/houses/chapel1`
- Flower Forest (1): `kaisyn:village/flower_forest_japanese/houses/flower_forest_temple_1`
- Forest (1): `kaisyn:village/forest_ruins/houses/forest_temple_1`

**Row 3** (49 wide, 23 deep)

- Fortified (1): `terralith:village/fortified/houses/church`
- Iberian (1): `kaisyn:village/exclusives/iberian/houses/iberian_temple_1`

**Row 4** (49 wide, 12 deep)

- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_temple_1`
- Meadow (1): `kaisyn:village/meadow_swiss/houses/meadow_temple_1`
- Nilotic (1): `kaisyn:village/exclusives/nilotic/houses/nilotic_temple_1`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_temple_1`

**Row 5** (58 wide, 23 deep)

- Plains (2): `minecraft:village/plains/houses/plains_temple_{3-4}`
- Rustic (1): `kaisyn:village/exclusives/rustic/houses/rustic_temple_1`
- Savanna (2): `minecraft:village/savanna/houses/savanna_temple_{1-2}`
- Snowy (1): `minecraft:village/snowy/houses/snowy_temple_1`

**Row 6** (49 wide, 13 deep)

- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_temple_1`
- Swamp (1): `kaisyn:village/swamp_boat/houses/swamp_b_farm_and_temple_1`
- Taiga (1): `minecraft:village/taiga/houses/taiga_temple_1`

**Row 7** (26 wide, 13 deep)

- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_temple_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/houses/tudor_temple_1`

### Farm (33)

**Row 1** (40 wide, 40 deep)

- Birch Forest (1): `kaisyn:village/birch_forest_romanian/houses/birch_forest_small_farm_1`
- Classic (2): `kaisyn:village/exclusives/classic/houses/classic_large_farm_1`, `kaisyn:village/exclusives/classic/houses/classic_small_farm_1`
- Desert / minecraft (3): `minecraft:village/desert/houses/desert_farm_{1-2}`, `minecraft:village/desert/houses/desert_large_farm_1`

**Row 2** (48 wide, 24 deep)

- Desert / terralith (1): `terralith:village/desert/houses/farm1`
- Flower Forest (2): `kaisyn:village/flower_forest_japanese/houses/flower_forest_large_farm_1`, `kaisyn:village/flower_forest_japanese/houses/flower_forest_small_farm_1`

**Row 3** (58 wide, 23 deep)

- Forest (2): `kaisyn:village/forest_ruins/houses/forest_large_farm_1`, `kaisyn:village/forest_ruins/houses/forest_small_farm_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/houses/iberian_garden_1`
- Jungle (1): `kaisyn:village/jungle_tribal/houses/jungle_small_farm_1`
- Mushroom Fields (1): `kaisyn:village/mushroom_fields_fantasy/houses/mushroom_small_farm_1`

**Row 4** (56 wide, 36 deep)

- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/houses/old_growth_taiga_small_farm_1`
- Plains (2): `minecraft:village/plains/houses/plains_large_farm_1`, `minecraft:village/plains/houses/plains_small_farm_1`
- Savanna (3): `minecraft:village/savanna/houses/savanna_large_farm_{1-2}`, `minecraft:village/savanna/houses/savanna_small_farm`
- Savanna zombie (1): `minecraft:village/savanna/zombie/houses/savanna_large_farm_2`

**Row 5** (37 wide, 23 deep)

- Snowy (2): `minecraft:village/snowy/houses/snowy_farm_{1-2}`
- Snowy Taiga (1): `kaisyn:village/snowy_taiga_viking/houses/snowy_taiga_small_farm_1`
- Sparse Jungle (2): `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_large_farm_1`, `kaisyn:village/sparse_jungle_polynesian/houses/sparse_jungle_small_farm_1`

**Row 6** (49 wide, 58 deep)

- Sunflower Plains (3): `kaisyn:village/sunflower_plains_farm/side/sunflower_plains_farm_beetroot_1`, `kaisyn:village/sunflower_plains_farm/side/sunflower_plains_farm_pumpkin_1`, `kaisyn:village/sunflower_plains_farm/side/sunflower_plains_farm_wheat_1`
- Taiga (3): `minecraft:village/taiga/houses/taiga_large_farm_{1-2}`, `minecraft:village/taiga/houses/taiga_small_farm_1`
- Taiga zombie (1): `minecraft:village/taiga/zombie/houses/taiga_large_farm_2`

### Workplace (74)

**Row 1** (50 wide, 70 deep)

- Desert / advancedperipherals (1): `advancedperipherals:villages/scientist_desert`
- Desert / etched (1): `etched:village/desert/houses/desert_bard_house_1`
- Desert / farmersdelight (1): `farmersdelight:village/houses/desert_compost_pile`
- Desert / immersiveengineering (5): `immersiveengineering:village/houses/desert_electrician`, `immersiveengineering:village/houses/desert_engineer`, `immersiveengineering:village/houses/desert_gunsmith`, `immersiveengineering:village/houses/desert_machinist`, `immersiveengineering:village/houses/desert_outfitter`

**Row 2** (57 wide, 25 deep)

- Desert / monobank (3): `monobank:village/houses/desert_vault`, `monobank:village/monobank/desert_locked`, `monobank:village/monobank/desert_unlocked`
- Desert / pneumaticcraft (1): `pneumaticcraft:villages/mechanic_house_desert`
- Desert / veggiesdelight (1): `veggiesdelight:village/houses/desert_depot_house`
- Desert / wares (1): `wares:village/houses/desert_warehouse`

**Row 3** (52 wide, 65 deep)

- Plains / advancedperipherals (1): `advancedperipherals:villages/scientist_plains`
- Plains / etched (1): `etched:village/plains/houses/plains_bard_house_1`
- Plains / farmersdelight (1): `farmersdelight:village/houses/plains_compost_pile`
- Plains / immersiveengineering (5): `immersiveengineering:village/houses/plains_electrician`, `immersiveengineering:village/houses/plains_engineer`, `immersiveengineering:village/houses/plains_gunsmith`, `immersiveengineering:village/houses/plains_machinist`, `immersiveengineering:village/houses/plains_outfitter`

**Row 4** (51 wide, 24 deep)

- Plains / monobank (3): `monobank:village/houses/plains_vault`, `monobank:village/monobank/plains_locked`, `monobank:village/monobank/plains_unlocked`
- Plains / pneumaticcraft (1): `pneumaticcraft:villages/mechanic_house_plains`
- Plains / veggiesdelight (1): `veggiesdelight:village/houses/plains_depot_house`

**Row 5** (54 wide, 11 deep)

- Plains / wares (1): `wares:village/houses/plains_warehouse`
- Savanna / advancedperipherals (1): `advancedperipherals:villages/scientist_savanna`
- Savanna / etched (1): `etched:village/savanna/houses/savanna_bard_house_1`
- Savanna / farmersdelight (1): `farmersdelight:village/houses/savanna_compost_pile`

**Row 6** (58 wide, 65 deep)

- Savanna / immersiveengineering (5): `immersiveengineering:village/houses/savanna_electrician`, `immersiveengineering:village/houses/savanna_engineer`, `immersiveengineering:village/houses/savanna_gunsmith`, `immersiveengineering:village/houses/savanna_machinist`, `immersiveengineering:village/houses/savanna_outfitter`
- Savanna / monobank (3): `monobank:village/houses/savanna_vault`, `monobank:village/monobank/savanna_locked`, `monobank:village/monobank/savanna_unlocked`
- Savanna / pneumaticcraft (1): `pneumaticcraft:villages/mechanic_house_savanna`
- Savanna / veggiesdelight (1): `veggiesdelight:village/houses/savanna_depot_house`

**Row 7** (59 wide, 13 deep)

- Savanna / wares (1): `wares:village/houses/savanna_warehouse`
- Snowy / advancedperipherals (1): `advancedperipherals:villages/scientist_snowy`
- Snowy / etched (1): `etched:village/snowy/houses/snowy_bard_house_1`
- Snowy / farmersdelight (1): `farmersdelight:village/houses/snowy_compost_pile`

**Row 8** (57 wide, 70 deep)

- Snowy / immersiveengineering (5): `immersiveengineering:village/houses/snowy_electrician`, `immersiveengineering:village/houses/snowy_engineer`, `immersiveengineering:village/houses/snowy_gunsmith`, `immersiveengineering:village/houses/snowy_machinist`, `immersiveengineering:village/houses/snowy_outfitter`
- Snowy / monobank (3): `monobank:village/houses/snowy_vault`, `monobank:village/monobank/snowy_locked`, `monobank:village/monobank/snowy_unlocked`
- Snowy / pneumaticcraft (1): `pneumaticcraft:villages/mechanic_house_snowy`
- Snowy / veggiesdelight (1): `veggiesdelight:village/houses/snowy_depot_house`

**Row 9** (46 wide, 17 deep)

- Snowy / wares (1): `wares:village/houses/snowy_warehouse`
- Sunflower Plains (1): `kaisyn:village/sunflower_plains_farm/side/sunflower_plains_windmill_1`
- Taiga / advancedperipherals (1): `advancedperipherals:villages/scientist_taiga`

**Row 10** (57 wide, 60 deep)

- Taiga / etched (1): `etched:village/taiga/houses/taiga_bard_house_1`
- Taiga / farmersdelight (1): `farmersdelight:village/houses/taiga_compost_pile`
- Taiga / immersiveengineering (5): `immersiveengineering:village/houses/taiga_electrician`, `immersiveengineering:village/houses/taiga_engineer`, `immersiveengineering:village/houses/taiga_gunsmith`, `immersiveengineering:village/houses/taiga_machinist`, `immersiveengineering:village/houses/taiga_outfitter`
- Taiga / monobank (3): `monobank:village/houses/taiga_vault`, `monobank:village/monobank/taiga_locked`, `monobank:village/monobank/taiga_unlocked`

**Row 11** (58 wide, 22 deep)

- Taiga / pneumaticcraft (1): `pneumaticcraft:villages/mechanic_house_taiga`
- Taiga / veggiesdelight (1): `veggiesdelight:village/houses/taiga_depot_house`
- Taiga / wares (1): `wares:village/houses/taiga_warehouse`
- Any / dustrial_decor (2): `dustrial_decor:village/pillager_prison`, `dustrial_decor:village/workshop`

**Row 12** (1 wide, 1 deep)

- Any / monobank (1): `monobank:village/monobank/empty`

### Gate and tower (2)

**Row 1** (3 wide, 27 deep)

- Flower Forest (2): `kaisyn:village/flower_forest_japanese/flower_forest_gate_{1-2}`

### Animals (23)

**Row 1** (19 wide, 109 deep)

- Meadow (1): `kaisyn:village/meadow_swiss/animals/dog`
- Mushroom Fields (2): `kaisyn:village/mushroom_fields_fantasy/animals/mooshroom_brown`, `kaisyn:village/mushroom_fields_fantasy/animals/mooshroom_red`
- Any / incubation (1): `incubation:village/chickens_1`
- Any / minecraft (19): `minecraft:village/common/animals/cat_black`, `minecraft:village/common/animals/cat_british`, `minecraft:village/common/animals/cat_calico`, `minecraft:village/common/animals/cat_jellie`, `minecraft:village/common/animals/cat_persian`, `minecraft:village/common/animals/cat_ragdoll`, `minecraft:village/common/animals/cat_red`, `minecraft:village/common/animals/cat_siamese`, `minecraft:village/common/animals/cat_tabby`, `minecraft:village/common/animals/cat_white`, `minecraft:village/common/animals/cows_1`, `minecraft:village/common/animals/horses_{1-5}`, `minecraft:village/common/animals/pigs_1`, `minecraft:village/common/animals/sheep_{1-2}`

### Lamp (19)

**Row 1** (57 wide, 19 deep)

- Badlands (3): `kaisyn:village/badlands_pueblo/badlands_lamp_{1-3}`
- Desert (1): `minecraft:village/desert/desert_lamp_1`
- Flower Forest (1): `kaisyn:village/flower_forest_japanese/flower_forest_lamp_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/iberian_lamp_post_1`
- Jungle (3): `kaisyn:village/jungle_tribal/jungle_lamp_{1-3}`
- Meadow (1): `kaisyn:village/meadow_swiss/meadow_lamp_1`
- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/med_lamp_1`
- Plains (1): `minecraft:village/plains/plains_lamp_1`
- Savanna (1): `minecraft:village/savanna/savanna_lamp_post_1`

**Row 2** (23 wide, 15 deep)

- Snowy (3): `minecraft:village/snowy/snowy_lamp_post_{1-3}`
- Sparse Jungle (1): `kaisyn:village/sparse_jungle_polynesian/sparse_jungle_tiki_torch_1`
- Taiga (1): `minecraft:village/taiga/taiga_lamp_post_1`
- Tudor (1): `kaisyn:village/exclusives/tudor/tudor_lamp_1`

### Decor (31)

**Row 1** (53 wide, 19 deep)

- Birch Forest (3): `kaisyn:village/birch_forest_romanian/birch_forest_decor_{1-3}`
- Desert (1): `waystones:village/desert/waystone`
- Flower Forest (1): `kaisyn:village/flower_forest_japanese/flower_forest_planter_1`
- Grove (1): `kaisyn:village/grove_villager_outpost/grove_table_1`
- Iberian (1): `kaisyn:village/exclusives/iberian/iberian_plants_1`
- Jungle (1): `kaisyn:village/jungle_tribal/jungle_decor_1`

**Row 2** (58 wide, 15 deep)

- Mediterranean (1): `kaisyn:village/exclusives/mediterranean/med_planter_1`
- Modded (3): `kaisyn:village/modded/waystones/waystone_default`, `kaisyn:village/modded/waystones/waystone_desert`, `kaisyn:village/modded/waystones/waystone_mossy`
- Old Growth Taiga (1): `kaisyn:village/old_growth_taiga_polish/old_growth_taiga_planter_1`
- Plains (1): `minecraft:village/plains/houses/plains_accessory_1`
- Snowy Slopes (1): `kaisyn:village/snowy_slopes_inn/snowy_slopes_table_1`
- Snowy Taiga (2): `kaisyn:village/snowy_taiga_viking/snowy_taiga_drakkar_1`, `kaisyn:village/snowy_taiga_viking/snowy_taiga_wood_pile_1`
- Sunflower Plains (2): `kaisyn:village/sunflower_plains_farm/sunflower_plains_scarecrow_{1-2}`

**Row 3** (55 wide, 46 deep)

- Taiga (6): `minecraft:village/taiga/taiga_decoration_{1-6}`
- Tudor (1): `kaisyn:village/exclusives/tudor/tudor_table_1`
- Any / dustrial_decor (1): `dustrial_decor:village/cardboard_castle`
- Any / minecraft (3): `minecraft:village/decays/grass_9x9`, `minecraft:village/decays/grass_11x13`, `minecraft:village/decays/grass_16x16`
- Any / waystones (1): `waystones:village/common/waystone`

### Other (1)

**Row 1** (5 wide, 5 deep)

- Rustic (1): `kaisyn:village/exclusives/rustic/professions_misc/rustic_empty_piece_1`

## Pillager (54)

### Outposts (9)

- Badlands (1): `towns_and_towers:pillager_outpost_badlands`
- Classic (1): `towns_and_towers:exclusives/pillager_outpost_classic`
- Desert (1): `towns_and_towers:pillager_outpost_desert`
- Grove (1): `towns_and_towers:pillager_outpost_grove`
- Iberian (1): `towns_and_towers:exclusives/pillager_outpost_iberian`
- Jungle (1): `towns_and_towers:pillager_outpost_jungle`
- Mediterranean (1): `towns_and_towers:exclusives/pillager_outpost_mediterranean`
- Rustic (1): `towns_and_towers:exclusives/pillager_outpost_rustic`
- Taiga (1): `towns_and_towers:pillager_outpost_old_growth_taiga`

### Towers (23)

- Acacia (1): `mysticrift_pillagers_towers_aris:acacia_pillager_tower`
- Birch (1): `mysticrift_pillagers_towers_aris:birch_pillager_tower`
- Cherry (1): `mysticrift_pillagers_towers_aris:cherry_pillager_tower`
- Dark Forest (1): `minecraft:pillager_outpost`
- Desert (1): `mysticrift_pillagers_towers_aris:desert_pillager_tower`
- Forest (3): `towns_and_towers:pillager_outpost_birch_forest`, `towns_and_towers:pillager_outpost_flower_forest`, `towns_and_towers:pillager_outpost_forest`
- Jungle (1): `mysticrift_pillagers_towers_aris:jungle_pillager_tower`
- Mangrove (1): `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`
- Meadow (1): `towns_and_towers:pillager_outpost_meadow`
- Mushroom (1): `towns_and_towers:pillager_outpost_mushroom_fields`
- Oak (1): `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower`
- Oriental (1): `towns_and_towers:exclusives/pillager_outpost_oriental`
- Plains (1): `towns_and_towers:pillager_outpost_sunflower_plains`
- Savanna (1): `towns_and_towers:pillager_outpost_savanna`
- Snowy (3): `towns_and_towers:pillager_outpost_snowy_plains`, `towns_and_towers:pillager_outpost_snowy_slopes`, `towns_and_towers:pillager_outpost_snowy_taiga`
- Swamp (1): `towns_and_towers:pillager_outpost_swamp`
- Swedish (1): `towns_and_towers:exclusives/pillager_outpost_swedish`
- Taiga (1): `towns_and_towers:pillager_outpost_taiga`
- Tudor (1): `towns_and_towers:exclusives/pillager_outpost_tudor`

### Camps (6)

- Badlands (1): `towns_and_towers:pillager_outpost_wooded_badlands`
- Beach (1): `towns_and_towers:pillager_outpost_beach`
- Jungle (1): `towns_and_towers:pillager_outpost_sparse_jungle`
- Plains (2): `eugene:fake_trader_camp`, `eugene:pillager_camp`
- Snowy (1): `towns_and_towers:pillager_outpost_snowy_beach`

### Ships (4)

- Ocean (1): `towns_and_towers:pillager_outpost_ocean`
- Any / blocky_siege (1): `blocky_siege:pillager_ship`
- Any / illager_viking_longship (1): `illager_viking_longship:illager_longship`
- Any / illagerwarship (1): `illagerwarship:warship`

### Siege and wagons (8)

- Dark Forest (1): `eugene:witch_wagon`
- Plains / eugene (1): `eugene:pillager_loot_carriage`
- Plains / illager_siege_weapons (6): `illager_siege_weapons:ballista`, `illager_siege_weapons:battering_ram`, `illager_siege_weapons:catapult`, `illager_siege_weapons:reinforced_battering_ram`, `illager_siege_weapons:siege_tower`, `illager_siege_weapons:trebuchet`

### Witches and mansions (4)

- Dark Forest (1): `minecraft:mansion`
- Swamp / betterwitchhuts (1): `betterwitchhuts:witch_hut`
- Swamp / minecraft (1): `minecraft:swamp_hut`
- Swamp / villagesandpillages (1): `villagesandpillages:village_witch`

## Ruins (28)

### Towers (1)

- Plains (1): `philipsruins:ancient_towers`

### Portals (6)

- Desert (1): `minecraft:ruined_portal_desert`
- Jungle (1): `minecraft:ruined_portal_jungle`
- Ocean (1): `minecraft:ruined_portal_ocean`
- Swamp (1): `minecraft:ruined_portal_swamp`
- Any (2): `minecraft:ruined_portal`, `minecraft:ruined_portal_mountain`

### Surface ruins (21)

- Badlands (1): `philipsruins:badlands_structures`
- Desert (1): `philipsruins:desert_structures`
- Icy (1): `terralith:glacial_hut`
- Plains / eugene (1): `eugene:ruined_drum_tower`
- Plains / forest_ruins (7): `forest_ruins:ruin_{1-7}`
- Plains / forgotten_church (1): `forgotten_church:forgotten_church`
- Plains / philipsruins (8): `philipsruins:ancient_ruins`, `philipsruins:field_stone_ruins`, `philipsruins:field_stone_ruins_rocks`, `philipsruins:level_one_ruins`, `philipsruins:level_three_ruins`, `philipsruins:level_two_ruins`, `philipsruins:level_two_ruins_pool`, `philipsruins:pumpkin_ruins`
- Spruce (1): `watchtower_building:abandoned_watchtower`

## Underground (20)

### Mineshafts (3)

- Badlands (1): `minecraft:mineshaft_mesa`
- Any / minecraft (1): `minecraft:mineshaft`
- Any / terralith (1): `terralith:underground/mining_outpost`

### Cabins (9)

- Badlands (1): `subterrestrial:mesa_cabin`
- Deepslate (1): `subterrestrial:deepslate_cabin`
- Default (1): `subterrestrial:default_cabin`
- Desert (1): `subterrestrial:desert_cabin`
- Ice (1): `subterrestrial:ice_cabin`
- Jungle (1): `subterrestrial:jungle_cabin`
- Oak (1): `terralith:underground/oak_cabin`
- Ocean (1): `subterrestrial:ocean_cabin`
- Taiga (1): `subterrestrial:taiga_cabin`

### Other (8)

- Dark Forest (1): `farmers_structures:squids`
- Swamp (1): `farmers_structures:ratatouilles`
- Any / minecraft (1): `minecraft:buried_treasure`
- Any / philipsruins (1): `philipsruins:underground_structures`
- Any / terralith (4): `terralith:underground/giant_bee_hive`, `terralith:underground/old_refinery`, `terralith:underground/sunken_tower`, `terralith:underground/witch_hut`

## Underwater (11)

### Ruins (7)

- Ocean / minecraft (2): `minecraft:ocean_ruin_cold`, `minecraft:ocean_ruin_warm`
- Ocean / philipsruins (2): `philipsruins:ocean_fortress`, `philipsruins:ocean_ruins`
- Warm (1): `hopo:underwater_warm_small_ruins`
- Any (2): `hopo:underwater_ruins`, `hopo:underwater_small_ruins`

### Shipwrecks (3)

- Ocean (1): `towns_and_towers:wreckage_ocean`
- Any (2): `revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`

### Fossils (1)

- Any (1): `hopo:underwater_fossils`

## Sky (39)

### Balloons and temples (3)

- Any / cloudstorage (2): `cloudstorage:big_balloon`, `cloudstorage:sky_temple`
- Any / creativeengineering (1): `creativeengineering:sky_island`

### Cartographer (1)

- Sky (1): `skyvillages:cartographer_1`

### Cleric (1)

- Sky (1): `skyvillages:alchemist_1`

### Farmer (3)

- Sky (3): `skyvillages:farmersdelight/sky_village_farmersdelight_farm_{0-2}`

### Fletcher (1)

- Sky (1): `skyvillages:fletcher_1`

### Librarian (2)

- Sky (2): `skyvillages:librarian_{1-2}`

### Mason (1)

- Sky (1): `skyvillages:mason_1`

### Weaponsmith (2)

- Sky (2): `skyvillages:weaponsmith_{1-2}`

### Weaver (1)

- Sky (1): `skyvillages:loom_1`

### House (1)

- Sky (1): `skyvillages:house_1`

### Well and centre (1)

- Sky (1): `skyvillages:fountain`

### Farm (6)

- Sky (6): `skyvillages:farm_{1-3}`, `skyvillages:letsdo_vinery/sky_village_letsdo_vinery_{0-2}`

### Workplace (2)

- Sky (2): `skyvillages:letsdo_brewery/sky_village_letsdo_brewery_{0-1}`

### Road (5)

- Sky (5): `skyvillages:bridge_{1-5}`

### Decor (9)

- Sky (9): `skyvillages:letsdo_brewery/sky_village_letsdo_brewery_deco_{0-8}`

## Buildings (80)

### Camps (11)

- Beach (1): `campsite_structures:fisher_campsite`
- Plains / campsite_structures (5): `campsite_structures:armorer_campsite`, `campsite_structures:butcher_campsite`, `campsite_structures:cartographer_campsite`, `campsite_structures:cleric_campsite`, `campsite_structures:farmer_campsite`
- Plains / eugene (1): `eugene:camp`
- Plains / plains_villager_tents (1): `plains_villager_tents:big_villager_tent`
- Plains / villager_archer_camp (1): `villager_archer_camp:villager_archer_camp`
- Plains / villager_circus_tent (1): `villager_circus_tent:villager_circus_tent`
- Snowy (1): `snowy_tents:snowy_tent`

### Ships (7)

- Ocean (1): `towns_and_towers:village_ocean`
- Any / antiquetradingship (1): `antiquetradingship:antique_trading_ship`
- Any / unwrecked_ships (2): `unwrecked_ships:nether_ship`, `unwrecked_ships:ship`
- Any / villager_rafts (3): `villager_rafts:raft_{1-3}`

### Towers and castles (3)

- Icy (1): `terralith:spire`
- Plains (1): `eugene:red_roof_castle`
- Spruce (1): `watchtower_building:fresh_watchtower`

### Butcher (1)

- Plains (1): `villager_butcher_house:butcher_house`

### Cartographer (1)

- Plains (1): `villager_cartographer_house:villager_cartographer_house`

### Cleric (1)

- Plains (1): `villager_cleric_house:villager_cleric_house`

### Farmer (1)

- Plains (1): `villager_farmer_house:villager_farmer_house`

### Fisher (4)

- Beach (1): `villager_fish_shop:fish_shop`
- Plains (1): `villager_fishermans_hut:villager_fishermans_hut`
- Snowy (1): `farmers_structures:fishs_stew`
- Swamp (1): `old_swamp_fisherman_house:fisherman_house`

### Fletcher (1)

- Plains (1): `villager_fletcher_house:villager_fletcher_house`

### Leatherworker (1)

- Plains (1): `villager_tannery:tannery`

### Weaponsmith (2)

- Plains (1): `villager_forge:villager_forge`
- Taiga (1): `simply_houses:taiga_forge`

### Farms and stables (9)

- Highlands (1): `llama_stable:llama_stable`
- Jungle (1): `farmers_structures:cacaos`
- Plains / animal_barn (1): `animal_barn:barn`
- Plains / bighorsestable (1): `bighorsestable:big_horse_stable`
- Plains / farmers_structures (1): `farmers_structures:shepherd_farms`
- Plains / hearthandharvest (1): `hearthandharvest:corn_maze`
- Plains / small_villager_greenhouse (1): `small_villager_greenhouse:small_villager_greenhouse`
- Plains / smallhorsestable (1): `smallhorsestable:horse_stable`
- Plains / villager_windmill (1): `villager_windmill:windmill`

### Churches and temples (4)

- Desert (1): `desert_village_temple:village_temple`
- Forest (1): `simply_houses:forest_chapel`
- Jungle (1): `ancient_enchantment_temple:temple`
- Plains (1): `gothic_church:church`

### Watchtowers (3)

- Forest (1): `forest_watchtower:forest_watchtower`
- Oak (1): `mini_clock_tower:mini_clock_tower`
- Sand (1): `desert_watchtower:watchtower`

### Food (9)

- Beach (1): `farmers_structures:aquacultures_beach`
- Plains (3): `farmers_structures:aquacultures_cod`, `farmers_structures:aquacultures_common`, `farmers_structures:cooks`
- Any (5): `farmers_structures:aethers`, `farmers_structures:aquacultures_carp`, `farmers_structures:cooks_moss`, `farmers_structures:sushis`, `farmers_structures:undergardens_sea`

### House (22)

- Badlands / saloon (1): `saloon:saloon`
- Badlands / villager_gold_washing_facility (1): `villager_gold_washing_facility:villager_gold_washing_facility`
- Desert (1): `beautify:botanist_house_desert`
- Flower (1): `villager_flower_house:flower_house`
- Forest / hobbit_house_ (1): `hobbit_house_:hobbit_house`
- Forest / sawmillhouse (1): `sawmillhouse:sawmill`
- Forest / simply_houses (3): `simply_houses:forest_fort`, `simply_houses:forest_house`, `simply_houses:forest_stonemason`
- Plains / beautify (1): `beautify:botanist_house_plains`
- Plains / simply_houses (2): `simply_houses:plains_house`, `simply_houses:plains_mansion`
- Savanna / beautify (1): `beautify:botanist_house_savanna`
- Savanna / greek_villager_house (1): `greek_villager_house:greek_house`
- Savanna / villager_country_house (1): `villager_country_house:country_house`
- Snowy / beautify (1): `beautify:botanist_house_snowy`
- Snowy / minecraft (1): `minecraft:igloo`
- Taiga / beautify (1): `beautify:botanist_house_taiga`
- Taiga / simply_houses (3): `simply_houses:taiga_house`, `simply_houses:taiga_inn`, `simply_houses:taiga_woodcutter`
- Taiga / small_viking_villager_house (1): `small_viking_villager_house:small_viking_house`

## Nature and Decor (7)

### Statues and markers (6)

- Plains / old_illager_statue (1): `old_illager_statue:old_illager_statue`
- Plains / oldvillagerstatue (1): `oldvillagerstatue:old_villager_statue`
- Plains / small_cat_statues (3): `small_cat_statues:cat_{1-3}`
- Any (1): `supplementaries:way_sign`

### Environmental (1)

- Any (1): `ae2:meteorite`

## Nether (20)

### Decoration (3)

- Any / hearths (1): `hearths:netherrack_spiral`
- Any / minecraft (1): `minecraft:nether_fossil`
- Any / twigs (1): `twigs:bloodstone_obelisk`

### Constructed (11)

- Any / betterdungeons (1): `betterdungeons:small_nether_dungeon`
- Any / betterfortresses (1): `betterfortresses:fortress`
- Any / minecraft (2): `minecraft:bastion_remnant`, `minecraft:fortress`
- Any / philipsruins (7): `philipsruins:lost_soul_city`, `philipsruins:lost_soul_dungeon`, `philipsruins:nether_lava_ruins`, `philipsruins:rare_ruin`, `philipsruins:sealedruinedghaststatue1`, `philipsruins:start_nether_dungeon`, `philipsruins:start_nether_ruin`

### Towers (2)

- Crimson (1): `hearths:crimson_tower`
- Warped (1): `hearths:warped_tower`

### Camps (1)

- Any (1): `eugene:piglin_camp`

### House (2)

- Any (2): `farmers_structures:cooks_nether_{1-2}`

### Portals (1)

- Any (1): `minecraft:ruined_portal_nether`

## End (22)

### Environmental (5)

- Grove (1): `mes:enderbloom_grove`
- Any / mes (3): `mes:astral_meteorite`, `mes:enderskog`, `mes:placid_prairie`
- Any / nullscape (1): `nullscape:dragon_skeleton`

### Constructed (12)

- End (1): `endersdelight:end_house`
- Any / mes (8): `mes:astral_hideaway`, `mes:enderkeep_courtyard`, `mes:enderwatch_tower`, `mes:manuscript_shrine`, `mes:monolith`, `mes:mythic_garden`, `mes:phantom_citadel`, `mes:starlight_voyager`
- Any / minecraft (1): `minecraft:end_city`
- Any / philipsruins (2): `philipsruins:end_gate_fortress`, `philipsruins:end_ruins`

### Towers (1)

- End (1): `mysticrift_pillagers_towers_aris:end_pillager_tower`

### Decoration (4)

- Any (4): `mes:ender_spire`, `mes:enderpin_spikes`, `mes:endscraps`, `mes:ruined_pillar`

## Composite (34)

### Ships (2)

- Any (2): `creativeengineering:meta/pillager_fleet`, `creativeengineering:meta/raft_convoy`

### Ruins (3)

- Any (3): `creativeengineering:meta/ruin_cluster`, `creativeengineering:meta/ruin_scatter`, `creativeengineering:meta/waystone_ruins`

### Villages (4)

- Any (4): `creativeengineering:meta/fortified_camps`, `creativeengineering:meta/fortified_ruins`, `creativeengineering:meta/town_villages`, `creativeengineering:meta/village_towers`

### Pillager (6)

- Any (6): `creativeengineering:meta/pillager_camp_ruin`, `creativeengineering:meta/pillager_camp_ruins`, `creativeengineering:meta/pillager_camp_siege`, `creativeengineering:meta/pillager_great_tower_siege`, `creativeengineering:meta/pillager_stronghold_siege`, `creativeengineering:meta/pillager_tower_siege`

### Camps and houses (4)

- Any (4): `creativeengineering:meta/farmers_camp`, `creativeengineering:meta/house_camp`, `creativeengineering:meta/waysign_camps`, `creativeengineering:meta/waysign_house_camp`

### Underwater (4)

- Any (4): `creativeengineering:meta/shipwreck_debris`, `creativeengineering:meta/shipwreck_graveyard`, `creativeengineering:meta/sunken_ruin_cluster`, `creativeengineering:meta/underwater_meteorite`

### Sky (4)

- Any (4): `creativeengineering:meta/balloon_fleet`, `creativeengineering:meta/cloud_balloons`, `creativeengineering:meta/cloud_cluster`, `creativeengineering:meta/sky_village_islands`

### Nature (7)

- Acacia (1): `creativeengineering:meta/deadwood_acacia`
- Birch (1): `creativeengineering:meta/deadwood_birch`
- Cherry (1): `creativeengineering:meta/deadwood_cherry`
- Jungle (1): `creativeengineering:meta/deadwood_jungle`
- Oak (1): `creativeengineering:meta/deadwood_oak`
- Spruce (1): `creativeengineering:meta/deadwood_spruce`
- Any (1): `creativeengineering:meta/driftwood_flotilla`

## Unmapped (44)

No change: no `type/*` tag, placed along -X, one column per mod. Listed in [structure_catalogue_map.md](structure_catalogue_map.md).
