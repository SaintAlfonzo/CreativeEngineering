// Structures the earth-fill check (patcherMod worldgen/EarthFill, CEConfig [worldgen.earth_fill]) never
// cancels: ones that float or stand in the water on purpose, so their ground is over air or water by
// design. Structure ids as in /dumpstructuregen ('#tag' works too; globs and '@mod' are not used here
// because the structure registry is dynamic). To give a structure its own limits instead of exempting
// it, use the [earth_fill] overrides list in config/creativeengineering-common.toml.
ServerEvents.tags('worldgen/structure', function (e) {
	global.tags.add(e, 'creativeengineering:earth_fill/exempt', [
		// sky: Cloud Storage balloons and temples, Sky Villages, AE2 meteorites, the floating islands of meta/sky_village_islands
		'cloudstorage:big_balloon', 'cloudstorage:sky_temple', 'skyvillages:skyvillage', 'ae2:meteorite', 'creativeengineering:sky_island',
		// ships at sea: they float
		'towns_and_towers:pillager_outpost_ocean', 'towns_and_towers:village_ocean', 'mes:starlight_voyager',
		'antiquetradingship:antique_trading_ship', 'illagerwarship:warship', 'illager_viking_longship:illager_longship',
		'blocky_siege:pillager_ship', 'unwrecked_ships:ship', 'unwrecked_ships:nether_ship',
		'villager_rafts:raft_1', 'villager_rafts:raft_2', 'villager_rafts:raft_3',
		// the seabed: ocean ruins and fortresses, wrecks, the monument, Hopo's fossils, the stilted sushi stall
		'minecraft:ocean_ruin_cold', 'minecraft:ocean_ruin_warm', 'philipsruins:ocean_ruins', 'philipsruins:ocean_fortress',
		'hopo:underwater_ruins', 'hopo:underwater_small_ruins', 'hopo:underwater_warm_small_ruins', 'hopo:underwater_fossils',
		'minecraft:shipwreck', 'minecraft:shipwreck_beached', 'revamped_shipwrecks:shipwreck', 'revamped_shipwrecks:shipwreck_beached',
		'towns_and_towers:wreckage_ocean', 'minecraft:monument', 'farmers_structures:sushis', 'minecraft:buried_treasure',
		// builds its own foundation: the woodland mansion pours cobblestone under its footprint down to the ground
		// (its afterPlace), so its pad never hangs (structure_tuning.md 7.3.1)
		'minecraft:mansion',
		// buried by design: the cellar kitchen and the squid tank start 55 / 40 blocks under the surface
		'farmers_structures:ratatouilles', 'farmers_structures:squids',
	])
	// Jigsaw structures whose offending pieces are dropped instead of the whole start (CEConfig [worldgen.earth_fill]
	// prune): a house over deep water or a cliff goes, with its villagers, and the village stays (structure_tuning.md
	// 3.13). Every other surface jigsaw structure drops only its too-wet pieces (CEConfig pruneWetJigsaws, 2026-10-30);
	// meta structure parts and anchors are pruned the same way, part by part. A jigsaw whose pieces only make sense
	// together can be judged whole instead by adding it to creativeengineering:earth_fill/no_prune (empty today).
	global.tags.add(e, 'creativeengineering:earth_fill/prune', [
		'#creativeengineering:type/village',
	])
	// Structures that stand in water on purpose (stilts in a swamp or mangrove): no footprint water share, their
	// other earth-fill limits as usual (2026-10-30, TODO structures/earthfill #14: every surface piece now joins the
	// footprint, not only pieces with earth). The fisherman house is a water-fill structure, skipped already.
	global.tags.add(e, 'creativeengineering:earth_fill/wet_ok', [
		'minecraft:swamp_hut', 'betterwitchhuts:witch_hut', 'mysticrift_pillagers_towers_aris:mangrove_pillager_tower',
	])
	// Structures on a stone or brick base instead of a dirt pad: their base blocks (block tag
	// creativeengineering:earth_fill/stone_base, blocktags.js) count as earth, for their size and their overhang.
	// Without this the drum tower's 49x45 andesite plate counted as "small, 10 earth" (02_new_mods_tests.md 4.2).
	// The End House (Ender's Delight, 2026-10-25) stands on a purpur and end stone floor (layer 0: 101 of its 144
	// columns). Counted as earth, a start over the void hangs that whole floor in air and is cancelled (the mod's own
	// structure type skipped chunks whose surface was below y 50, i.e. no island; the pack uses plain jigsaw).
	global.tags.add(e, 'creativeengineering:earth_fill/stone_base', [
		'eugene:ruined_drum_tower', 'endersdelight:end_house',
	])
	// Desert and badlands structures on sandstone or terracotta floors: those blocks (block tag
	// creativeengineering:earth_fill/sand_base) count as earth, so a house floor over a river or a cliff is seen
	// (2026-11-02, tools/earth_base_scan.py, bottom 3 layers): T&T badlands village 0 % earth (red sandstone 56 %,
	// terracotta 14 %), wooded badlands village 0 % (terracotta 37 %), badlands outpost 0 %, Philip's badlands dungeon 0 %,
	// vanilla desert village 24 % (sandstone 54 %), fortified desert village 43 % (37 %), T&T desert outpost 49 %,
	// desert village temple 22 %, Philip's desert ruins 42 %. Left out: Better Desert Temples and the T&T desert mimic
	// (buried / underground: their floors sit in caves and aquifers by design); the desert pyramid is a legacy piece
	// (judged by its footprint, 3.28).
	global.tags.add(e, 'creativeengineering:earth_fill/sand_base', [
		'minecraft:village_desert', 'terralith:fortified_desert_village', 'desert_village_temple:village_temple',
		'philipsruins:desert_structures', 'philipsruins:badlands_start_dungeon',
		'towns_and_towers:pillager_outpost_desert', 'towns_and_towers:pillager_outpost_badlands',
		'towns_and_towers:village_badlands', 'towns_and_towers:village_wooded_badlands',
	])
})
