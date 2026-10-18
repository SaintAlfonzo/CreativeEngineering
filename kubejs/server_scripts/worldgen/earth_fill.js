// Structures the earth-fill check (patcherMod worldgen/EarthFill, CEConfig [worldgen.earth_fill]) never
// cancels: ones that float or stand in the water on purpose, so their ground is over air or water by
// design. Structure ids as in /dumpstructuregen ('#tag' works too; globs and '@mod' are not used here
// because the structure registry is dynamic). To give a structure its own limits instead of exempting
// it, use the [earth_fill] overrides list in config/creativeengineering-common.toml.
ServerEvents.tags('worldgen/structure', function (e) {
	global.tags.add(e, 'creativeengineering:earth_fill/exempt', [
		// sky: Cloud Storage balloons and temples, Sky Villages, AE2 meteorites
		'cloudstorage:big_balloon', 'cloudstorage:sky_temple', 'skyvillages:skyvillage', 'ae2:meteorite',
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
	])
	// Jigsaw structures whose offending pieces are dropped instead of the whole start (CEConfig [worldgen.earth_fill]
	// prune): a house over deep water or a cliff goes, with its villagers, and the village stays (structure_tuning.md
	// 3.13). Meta structures in the type tag are judged part by part and not pruned.
	global.tags.add(e, 'creativeengineering:earth_fill/prune', [
		'#creativeengineering:type/village',
	])
	// Structures on a stone or brick base instead of a dirt pad: their base blocks (block tag
	// creativeengineering:earth_fill/stone_base, blocktags.js) count as earth, for their size and their overhang.
	// Without this the drum tower's 49x45 andesite plate counted as "small, 10 earth" (02_new_mods_tests.md 4.2).
	global.tags.add(e, 'creativeengineering:earth_fill/stone_base', [
		'eugene:ruined_drum_tower',
	])
})
