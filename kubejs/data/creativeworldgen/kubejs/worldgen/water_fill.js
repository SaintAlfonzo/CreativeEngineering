// requires: creativeworldgen
// Structures the water-fill check (Creative Worldgen worldgen/EarthFill, creativeworldgen-common.toml [worldgen.water_fill]) looks at:
// ones that carry their own water in their templates (water, waterlogged blocks, kelp, seagrass). Placed where
// the terrain isn't water, that water replaces dry ground or stands in air and spills, so past the limits the
// structure is cancelled. Only structures in water_fill/structures are checked: plenty of land and underground
// structures carry water on purpose (jungle temple pools, stronghold fountains, village farms), and those
// must not be counted. Structure ids as in /dumpstructuregen ('#tag' works too).
//
// Which templates carry water (2026-10-08 scan of mods/*.jar): Hopo's ruins and fossils (30 to 7,700 water
// blocks each), the T&T ocean outpost plate (10,000, y 57-62, flush with the sea), the T&T ocean village
// escort ships, Farmer's Structures sushi stall and fish ponds, the swamp fisherman house (a pond under it).
ServerEvents.tags('worldgen/structure', function (e) {
	// Water dug into the ground on purpose: a pond replaces dry ground every time, so only water in air
	// (a pond spilling down a slope) should cancel. The default [water_fill] overrides entry
	// "#creativeworldgen:water_fill/ponds=-1,16" lifts the ground limit for these.
	global.tags.add(e, 'creativeworldgen:water_fill/ponds', [
		'farmers_structures:aquacultures_beach', 'farmers_structures:aquacultures_carp', 'farmers_structures:aquacultures_common',
		'old_swamp_fisherman_house:fisherman_house',
	])
	global.tags.add(e, 'creativeworldgen:water_fill/structures', [
		// the seabed: Hopo's ruins and fossils sit on the ocean floor, full of water up to 20 blocks high
		'hopo:underwater_ruins', 'hopo:underwater_small_ruins', 'hopo:underwater_warm_small_ruins', 'hopo:underwater_fossils',
		// at sea: a water plate / hull water at sea level
		'towns_and_towers:pillager_outpost_ocean', 'towns_and_towers:village_ocean',
		// a stilted stall in rivers
		'farmers_structures:sushis',
		'#creativeworldgen:water_fill/ponds',
	])
})
