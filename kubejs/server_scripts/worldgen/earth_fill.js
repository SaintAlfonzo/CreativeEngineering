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
	])
})
