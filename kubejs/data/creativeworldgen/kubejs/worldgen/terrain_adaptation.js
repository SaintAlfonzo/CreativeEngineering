// requires: creativeworldgen
// Terrain adaptation by structure tag (Creative Worldgen worldgen/TerrainAdaptation + TemplateTrim, CEConfig
// [worldgen.terrain_adaptation], feature_writeup/structure_tuning.md 3.15).
//
// creativeworldgen:terrain_adaptation/<mode> replaces a structure's own terrain_adaptation:
//   none       no fill, no carve: the structure sits where its start lands
//   bury, beard_thin, beard_box   vanilla's
//   soft       the pack's beard: vanilla's fill and carve, but only under the template's footprint, fading
//              out over a short round skirt (4 blocks + [terrain_adaptation] skirtBump) instead of a square
//              8-block one around the whole template box
// creativeworldgen:template_trim: template columns with only air, grass/dirt pad and plants (no built
// block) aren't placed, so the slope around the building stays natural. Goes with soft: the soft footprint
// then only covers the built columns. soft without trim keeps the pad (and levels it): for walled yards.
// creativeworldgen:template_trim/air: only air in columns with nothing solid is dropped (pad, plants, snow stay).
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
	global.tags.add(e, 'creativeworldgen:terrain_adaptation/none', siegeWeapons)

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
	// End builds (2026-10-25): Ender's Delight's End House (9x16 purpur and end stone floor, shipped by Creative
	// Delight's enders member). Soft only: its floor is the building, not a pad, so no trim. Its own json says beard_thin.
	var endBuilds = ['endersdelight:end_house']
	// Batch 4 (2026-10-30, TODO structures/terrain #1). Ponds: water is a built block for the trim, so the pond stays
	// whole and the pad around it goes. The carp pond is sunk too (start -4, grass rim at its y 4), like the camps.
	// Philip's level-three ruins: grass pads around the walls; meta/large_ruins parts follow their anchor.
	var ponds = [
		'farmers_structures:aquacultures_common', 'farmers_structures:aquacultures_carp', 'old_swamp_fisherman_house:fisherman_house',
	]
	var paddedRuins = ['philipsruins:level_three_ruins']

	global.tags.add(e, 'creativeworldgen:terrain_adaptation/soft', portedMods)
	global.tags.add(e, 'creativeworldgen:terrain_adaptation/soft', padHouses)
	global.tags.add(e, 'creativeworldgen:terrain_adaptation/soft', outposts)
	global.tags.add(e, 'creativeworldgen:terrain_adaptation/soft', walledCompounds)
	global.tags.add(e, 'creativeworldgen:terrain_adaptation/soft', endBuilds)
	global.tags.add(e, 'creativeworldgen:terrain_adaptation/soft', ponds)
	global.tags.add(e, 'creativeworldgen:terrain_adaptation/soft', paddedRuins)

	global.tags.add(e, 'creativeworldgen:template_trim', siegeWeapons)
	global.tags.add(e, 'creativeworldgen:template_trim', portedMods)
	global.tags.add(e, 'creativeworldgen:template_trim', padHouses)
	global.tags.add(e, 'creativeworldgen:template_trim', outposts)
	global.tags.add(e, 'creativeworldgen:template_trim', ponds)
	global.tags.add(e, 'creativeworldgen:template_trim', paddedRuins)

	// Air-only trim (patcherMod TemplateTrim#trimAir): only air in columns with nothing solid is dropped. Terralith's
	// spire (adaptation none) saves a 94x98 air box around its base layer that cut a square notch into the hills; the
	// full trim would also drop the snow on its upper layers (110 to 273 snow-only columns per layer, resting on the
	// layer below), so it gets this one and keeps its own adaptation.
	global.tags.add(e, 'creativeworldgen:template_trim/air', ['terralith:spire'])

	// Sunk starts (2026-10-19): start_height below 0 sinks these templates into the ground on purpose, and the beard
	// levels the ground at the surface they were projected to instead of at their sunk bottom layer (patcherMod
	// TerrainAdaptation#sink). Campsite Structures' camps stand on a 4-layer dirt dome under their grass layer (start
	// height -4: the grass layer replaces the surface block; their old features used -5, a 1-block dip); with the plain
	// beard_thin the ground around them was dug down to the dome's bottom and the dome stood 4 blocks out of a flat ring.
	global.tags.add(e, 'creativeworldgen:terrain_adaptation/sunk_start', [
		'campsite_structures:armorer_campsite', 'campsite_structures:butcher_campsite', 'campsite_structures:cartographer_campsite',
		'campsite_structures:cleric_campsite', 'campsite_structures:farmer_campsite', 'campsite_structures:fisher_campsite',
		// Not here: Philip's ancient_crypt and antiquus_crypta, T&T's nilotic village. Their start element is terrain
		// matching (patcherMod refuses them), so they keep their own placement (owner call 2026-11-01, TODO
		// structures/earthfill #17).
		// the carp pond (batch 4): a 4-layer dirt bowl under its grass rim, start height -4
		'farmers_structures:aquacultures_carp',
	])

	// Seabed clearance (2026-10-30, TODO structures/terrain #7; patcherMod SeabedClearance): underground structures at
	// a fixed height (Terralith's caves y -40..40, Subterrestrial's land cabins y 6..48) whose box top comes within 8
	// blocks of a floor with water on it are dropped; their beard carved the seabed open and the sea flooded them.
	// Not here: the ocean cabin (made for the seabed: y 24..44 in oceans only, it would almost never start), the
	// deepslate cabin (y -56..-6), Philip's y -40 ruins, and buried treasure (its box sits at y 90 until it is placed).
	global.tags.add(e, 'creativeworldgen:seabed_clearance', [
		'terralith:underground/oak_cabin', 'terralith:underground/giant_bee_hive', 'terralith:underground/mining_outpost',
		'terralith:underground/old_refinery', 'terralith:underground/sunken_tower', 'terralith:underground/witch_hut',
		'terralith:underground/frosted_dungeon',
		'subterrestrial:jungle_cabin', 'subterrestrial:default_cabin', 'subterrestrial:desert_cabin', 'subterrestrial:ice_cabin',
		'subterrestrial:mesa_cabin', 'subterrestrial:taiga_cabin',
	])
})
