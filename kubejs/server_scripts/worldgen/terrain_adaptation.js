// Terrain adaptation by structure tag (patcherMod worldgen/TerrainAdaptation + TemplateTrim, CEConfig
// [worldgen.terrain_adaptation], feature_writeup/structure_tuning.md 3.15).
//
// creativeengineering:terrain_adaptation/<mode> replaces a structure's own terrain_adaptation:
//   none       no fill, no carve: the structure sits where its start lands
//   bury, beard_thin, beard_box   vanilla's
//   soft       the pack's beard: vanilla's fill and carve, but only under the template's footprint, fading
//              out over a short round skirt (4 blocks + [terrain_adaptation] skirtBump) instead of a square
//              8-block one around the whole template box
// creativeengineering:template_trim: template columns with only air, grass/dirt pad and plants (no built
// block) aren't placed, so the slope around the building stays natural. Goes with soft: the soft footprint
// then only covers the built columns. soft without trim keeps the pad (and levels it): for walled yards.
// Candidates for more: python3 tools/terrain_adaptation_scan.py -> feature_writeup/terrain_adaptation_scan.md.
//
// Structure ids as in /dumpstructuregen; '#tag' works too. A structure in two mode tags takes the first of
// none, bury, beard_box, beard_thin, soft (logged). YUNG's structures ignore the mode tags (their own
// adaptation; logged at world load).
ServerEvents.tags('worldgen/structure', function (e) {
	// Siege weapons: small open frames on a few slabs, no pad of their own. beard_thin flattened their whole
	// 7x13 to 21x16 template box plus 8 blocks around it into a square plateau.
	var siegeWeapons = [
		'illager_siege_weapons:ballista', 'illager_siege_weapons:battering_ram', 'illager_siege_weapons:catapult',
		'illager_siege_weapons:reinforced_battering_ram', 'illager_siege_weapons:siege_tower', 'illager_siege_weapons:trebuchet',
	]
	global.tags.add(e, 'creativeengineering:terrain_adaptation/none', siegeWeapons)

	// The MCreator structure mods converted to kubejs/data (tools/port_structure_jars.py): flat-world templates
	// with pads 2 to 279 columns wide around the building and up to 18,000 saved air blocks.
	var portedMods = [
		// houses and stables
		'animal_barn:barn', 'bighorsestable:big_horse_stable', 'smallhorsestable:horse_stable', 'llama_stable:llama_stable',
		'saloon:saloon', 'small_viking_villager_house:small_viking_house', 'small_villager_greenhouse:small_villager_greenhouse',
		'villager_butcher_house:butcher_house', 'villager_cartographer_house:villager_cartographer_house',
		'villager_cleric_house:villager_cleric_house', 'villager_country_house:country_house',
		'villager_farmer_house:villager_farmer_house', 'villager_fish_shop:fish_shop', 'villager_fishermans_hut:villager_fishermans_hut',
		'villager_fletcher_house:villager_fletcher_house', 'villager_flower_house:flower_house', 'villager_forge:villager_forge',
		'villager_gold_washing_facility:villager_gold_washing_facility', 'villager_tannery:tannery', 'villager_windmill:windmill',
		// camps and tents
		'plains_villager_tents:big_villager_tent', 'snowy_tents:snowy_tent', 'villager_archer_camp:villager_archer_camp',
		'villager_circus_tent:villager_circus_tent',
		// churches, temples, towers
		'ancient_enchantment_temple:temple', 'desert_village_temple:village_temple', 'forgotten_church:forgotten_church',
		'gothic_church:church', 'mini_clock_tower:mini_clock_tower', 'desert_watchtower:watchtower',
		'forest_watchtower:forest_watchtower', 'watchtower_building:abandoned_watchtower', 'watchtower_building:fresh_watchtower',
		'mysticrift_pillagers_towers_aris:acacia_pillager_tower', 'mysticrift_pillagers_towers_aris:birch_pillager_tower',
		'mysticrift_pillagers_towers_aris:cherry_pillager_tower', 'mysticrift_pillagers_towers_aris:desert_pillager_tower',
		'mysticrift_pillagers_towers_aris:end_pillager_tower', 'mysticrift_pillagers_towers_aris:jungle_pillager_tower',
		'mysticrift_pillagers_towers_aris:mangrove_pillager_tower', 'mysticrift_pillagers_towers_aris:oak_wood_pillager_tower',
		// ruins and statues
		'forest_ruins:ruin_1', 'forest_ruins:ruin_2', 'forest_ruins:ruin_3', 'forest_ruins:ruin_4', 'forest_ruins:ruin_5',
		'forest_ruins:ruin_6', 'forest_ruins:ruin_7', 'old_illager_statue:old_illager_statue', 'oldvillagerstatue:old_villager_statue',
		'small_cat_statues:cat_1', 'small_cat_statues:cat_2', 'small_cat_statues:cat_3',
	]

	// From the scan (feature_writeup/terrain_adaptation_scan.md, batches in structure_tuning.md 3.15).
	// Batch 1, houses on pads (the same case as the ported mods): 25 to 61% of each template box unbuilt.
	var padHouses = [
		'hobbit_house_:hobbit_house', 'sawmillhouse:sawmill', 'simply_houses:forest_fort', 'greek_villager_house:greek_house',
		'farmers_structures:cooks', 'farmers_structures:cooks_moss', 'terralith:glacial_hut',
		'beautify:botanist_house_desert', 'beautify:botanist_house_plains', 'beautify:botanist_house_savanna',
		'beautify:botanist_house_snowy', 'beautify:botanist_house_taiga',
	]
	// Batch 2, pillager outposts: towers on pads, overhanging tower tops, saved air around the feature pieces.
	var outposts = [
		'minecraft:pillager_outpost',
		'towns_and_towers:pillager_outpost_badlands', 'towns_and_towers:pillager_outpost_beach',
		'towns_and_towers:pillager_outpost_desert', 'towns_and_towers:pillager_outpost_forest',
		'towns_and_towers:pillager_outpost_grove', 'towns_and_towers:pillager_outpost_jungle',
		'towns_and_towers:pillager_outpost_meadow', 'towns_and_towers:pillager_outpost_mushroom_fields',
		'towns_and_towers:pillager_outpost_old_growth_taiga', 'towns_and_towers:pillager_outpost_savanna',
		'towns_and_towers:pillager_outpost_snowy_beach', 'towns_and_towers:pillager_outpost_snowy_plains',
		'towns_and_towers:pillager_outpost_snowy_taiga', 'towns_and_towers:pillager_outpost_sparse_jungle',
		'towns_and_towers:pillager_outpost_sunflower_plains', 'towns_and_towers:pillager_outpost_swamp',
		'towns_and_towers:pillager_outpost_taiga', 'towns_and_towers:pillager_outpost_wooded_badlands',
		'towns_and_towers:exclusives/pillager_outpost_iberian', 'towns_and_towers:exclusives/pillager_outpost_mediterranean',
		'towns_and_towers:exclusives/pillager_outpost_oriental', 'towns_and_towers:exclusives/pillager_outpost_rustic',
		'towns_and_towers:exclusives/pillager_outpost_swedish',
	]
	// Batch 3, walled compounds: soft only. Their courtyards are pad (the bastille has 9,423 pad columns) and
	// should stay flat, so no trim; soft still shortens and rounds the skirt outside the walls.
	var walledCompounds = ['takesapillage:bastille', 'takesapillage:pillager_camp']

	global.tags.add(e, 'creativeengineering:terrain_adaptation/soft', portedMods)
	global.tags.add(e, 'creativeengineering:terrain_adaptation/soft', padHouses)
	global.tags.add(e, 'creativeengineering:terrain_adaptation/soft', outposts)
	global.tags.add(e, 'creativeengineering:terrain_adaptation/soft', walledCompounds)

	global.tags.add(e, 'creativeengineering:template_trim', siegeWeapons)
	global.tags.add(e, 'creativeengineering:template_trim', portedMods)
	global.tags.add(e, 'creativeengineering:template_trim', padHouses)
	global.tags.add(e, 'creativeengineering:template_trim', outposts)
})
