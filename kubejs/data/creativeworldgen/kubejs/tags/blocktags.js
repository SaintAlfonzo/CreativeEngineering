// requires: creativeworldgen
// Creative Worldgen: block tags of the worldgen and fauna systems; moved from kubejs/server_scripts/tags/blocktags.js on
// 2026-11-02 (villager ladders, structure ground match, earth fill, template trim, wild plant soils).

ServerEvents.tags('block', e => {
	var t = global.tags
	var b = global.blocks
	var item = global.item

	// Villager ladders (patcherMod McaVillagerPathLaddersMixin, feature_writeup/mob_block_tweaks.md 2.8): every block named
	// *ladder*; the mixin also requires minecraft:climbable, so a non-climbable match does nothing. Vines, ropes, chains,
	// scaffolding and grab rails stay out on purpose.
	t.addMatching(e, b.t_villager_ladders, '*:*ladder*')

	// ── Structure ground matching (Creative Worldgen worldgen/GroundMatch, creativeworldgen-common.toml [ground_match]) ──
	// A base family: these blocks in a structure's bottom layers become the sampled local ground where
	// the ground isn't this family (grass pads on volcanic peaks, dirt in deserts). A new family = a new
	// tag here + its id in [ground_match] families. dirt_path and farmland stay: they're design, not base.
	t.add(e, 'creativeengineering:ground_match/grass', [
		item.grass_block, item.dirt, item.coarse_dirt, item.podzol, item.rooted_dirt, item.mycelium,
	])
	// Dropped when the block under them is swapped (both halves of tall plants).
	t.add(e, 'creativeengineering:ground_match/plants', [
		'#minecraft:flowers', '#minecraft:saplings', item.grass, item.fern, item.tall_grass, item.large_fern,
		item.dead_bush, item.brown_mushroom, item.red_mushroom, b.sweet_berry_bush,
	])
	// Never sampled as ground: tree and crop parts that neighbouring chunks' features spill onto the surface.
	t.add(e, 'creativeengineering:ground_match/not_ground', [
		'#minecraft:leaves', '#minecraft:logs', item.brown_mushroom_block, item.red_mushroom_block,
		item.mushroom_stem, item.cactus, item.bamboo, item.pumpkin, item.melon_block,
	])

	// ── Structure earth fill (Creative Worldgen worldgen/EarthFill, creativeworldgen-common.toml [worldgen.earth_fill]) ──
	// Earth that counts when a structure's foundation sits over water or air: a start whose template
	// bases hold more of these over water or air than the limits is cancelled. Loose ground only, not
	// stone or bricks (those are the building). More tags can be listed in [earth_fill] blocks.
	t.add(e, 'creativeengineering:earth_fill/earth', [
		'#minecraft:dirt', '#forge:sand', '#forge:gravel', 'minecraft:clay', 'minecraft:dirt_path',
	])
	// Stone and brick bases, counted as earth only for structures in the structure tag of the same name
	// (worldgen/earth_fill.js): ones that stand on a stone plate instead of a dirt pad, like the drum tower.
	t.add(e, 'creativeengineering:earth_fill/stone_base', [
		'#minecraft:base_stone_overworld', item.cobblestone, item.mossy_cobblestone, item.stone_bricks,
		item.mossy_stone_bricks, item.cracked_stone_bricks, item.smooth_stone, item.polished_andesite,
		item.polished_diorite, item.polished_granite, item.polished_deepslate, item.deepslate_bricks,
		// End builds (the End House): end stone, its bricks and purpur
		item.end_stone, item.end_stone_bricks, item.purpur_block,
	])
	// Sandstone and terracotta floors, counted as earth only for structures in the structure tag of the same name
	// (worldgen/earth_fill.js): desert and badlands builds that stand on them instead of sand (2026-11-02,
	// tools/earth_base_scan.py: Towns and Towers badlands villages 0 % earth, Better Desert Temples 0 %).
	t.add(e, 'creativeengineering:earth_fill/sand_base', [
		'#forge:sandstone', '#minecraft:terracotta', item.philips_sand_stone_brick, item.philips_red_sand_stone_brick,
		item.philips_sandstone_tiles,
	])

	// ── Template trim (Creative Worldgen worldgen/TemplateTrim, creativeworldgen-common.toml [worldgen.terrain_adaptation]) ──
	// For structures in creativeengineering:template_trim: a template column holding only air, pad and plants
	// isn't placed, so the natural ground stays there. Pad = loose ground a template brings along; dirt_path
	// and farmland are left out on purpose (paths and crop plots are design, so their columns count as built).
	t.add(e, 'creativeengineering:template_trim/pad', [
		'#minecraft:dirt', '#forge:sand', '#forge:gravel', 'minecraft:clay', item.snow_block,
	])
	// Loose things standing on a pad: dropped with it.
	t.add(e, 'creativeengineering:template_trim/plants', [
		'#creativeengineering:ground_match/plants', item.snow, item.moss_carpet,
	])

	// ── Wild plant soils (tools/gen_soil_compat.py, feature_writeup/crops.md 2.8.1) ──
	// Ground that worldgen plant/tree features hard-coded to grass_block also accept (the generator's
	// kubejs/data overrides and the pack's wild/hamimelon test this tag). Immersive Weathering lays these
	// soils before vegetation. Permafrost is left out on purpose.
	t.add(e, 'creativeengineering:worldgen/grass_like_soils', [
		item.grass_block, item.humus, item.fluvisol, item.immersive_weathering_silt, item.vertisol,
		item.cryosol, item.rooted_grass_block,
	])
})
