// requires: creativecompat
// Creative Compat; moved from kubejs/server_scripts/other/feature_ports.js on 2026-11-01.
// Feature ports, tier 1 (feature_writeup/feature_porting.md 1): small recipe mods absorbed into the pack so their jars
// can go. Each block below rebuilds a mod's recipes through global.NCH, same inputs and outputs, ids under
// creativeengineering:<kind>/<name>. The jars are in disabledmods/ (2026-10-20).
//
//   Name Tag Crafting Recipe       1 shaped recipe
//   Create: Copper Splashing        33 create:splashing recipes (copper weathering, 11 shapes x 3 stages)
//   Furnace Recycle                 26 smelting recipes (iron / gold blocks and tools back to ingots or nuggets)
//   AE2 QoL Recipes                 311 cable recipes (16 colours x 5 cable kinds + 7 fluix), generated from the
//                                   mod's 19 templates; its two tags are in tags/feature_port_tags.js
//
// Create Waystone Recipes is NOT rebuilt: its one recipe overrode waystones:warp_stone with createcasing:chorium_ingot
// corners, and Create Casing isn't in the pack, so the recipe never loaded (feature_porting.md 1.2, decision D6).
// Copper Drops is a loot-table override: kubejs/data/minecraft/loot_tables/blocks/{copper_ore,deepslate_copper_ore}.json.
// Create: Generators is NOT rebuilt here: the pack had already replaced 5 of its 6 recipes with heat-tiered ones in
// engineering/resource_blocks.js, and its one live recipe (heated obsidian) moved there with them.
// Starter Kit: other/starter_kit.js. Book Fishing stays a jar (enchanted-book loot rebalance, feature_porting.md 1.1).

ServerEvents.recipes(e => {
	console.log('feature ports (tier 1) starting')

	var i = global.item
	var R = global.NCH
	var count = 0
	var bulk = (fn, sets) => { R.bulk(fn, sets); count += sets.length }

	// ============================================================
	// Name Tag Crafting Recipe (mr_name_tagcraftingrecipemod)
	// ============================================================
	R.shaped(Item.of(i.name_tag), ['#P', 'SS'], { '#': i.leather, P: i.paper, S: i.string }, e, 'name_tag'); count++

	// ============================================================
	// Create: Copper Splashing (create_copper_splashing)
	// Each copper shape weathers one stage in a fan's splashing stream: plain -> exposed -> weathered -> oxidized.
	// ============================================================
	var copperShapes = ['copper_shingle_slab', 'copper_shingle_stairs', 'copper_shingles', 'copper_tile_slab', 'copper_tile_stairs',
		'copper_tiles', 'cut_copper', 'cut_copper_slab', 'cut_copper_stairs', 'cut_copper_vertical_slab']
	var splashing = [[Item.of(i.exposed_copper), i.block_copper, e, 'copper_block_exposed'],
		[Item.of(i.weathered_copper), i.exposed_copper, e, 'copper_block_weathered'],
		[Item.of(i.oxidized_copper), i.weathered_copper, e, 'copper_block_oxidized']]
	// cut_copper_vertical_slab is Quark's: the dict drops it when Quark is absent
	copperShapes.filter(shape => global.has(i[shape], i['exposed_' + shape], i['weathered_' + shape], i['oxidized_' + shape])).forEach(shape => {
		splashing.push([Item.of(i['exposed_' + shape]), i[shape], e, shape + '_exposed'])
		splashing.push([Item.of(i['weathered_' + shape]), i['exposed_' + shape], e, shape + '_weathered'])
		splashing.push([Item.of(i['oxidized_' + shape]), i['weathered_' + shape], e, shape + '_oxidized'])
	})
	bulk('createSplashing', splashing)

	// ============================================================
	// Furnace Recycle (furnacerecycle): 0.1 xp, 200 ticks, as the mod
	// ============================================================
	var recycle = [
		[i.ingot_iron, [i.blast_furnace, i.bucket, i.cauldron, i.chain, i.crossbow, i.iron_pressure_plate, i.hopper, i.iron_door,
			i.minecart, i.piston, i.shears, i.smithing_table, i.sticky_piston]],
		[i.nugget_iron, [i.activator_rail, i.detector_rail, i.flint_and_steel, i.iron_bars, [i.lantern, i.soul_lantern], i.rail,
			i.shield, i.stonecutter, i.tripwire_hook]],
		[i.block_iron, [[i.anvil, i.chipped_anvil, i.damaged_anvil]]],
		[i.ingot_gold, [i.bell, i.light_weighted_pressure_plate]],
		[i.nugget_gold, [i.powered_rail]],
	]
	var smelting = []
	recycle.forEach(pair => pair[1].forEach(input => {
		var first = Array.isArray(input) ? input[0] : input
		smelting.push([pair[0], input, e, 'recycle_' + String(first).split(':')[1], 0.1, 200])
	}))
	bulk('smelting', smelting)

	// ============================================================
	// AE2 QoL Recipes (ae2qolrecipes): the mod's 19 templates x 16 colours + 7 fluix recipes = 311
	// ============================================================
	var shaped = [], shapeless = []
	if (global.has('ae2')) {
		var colors = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple',
			'blue', 'brown', 'green', 'red', 'black']
		var cable = (color, kind) => i[color + '_' + kind + '_cable']
		var dye = color => '#forge:dyes/' + color
		var tag = kind => '#ae2:' + kind + '_cable'
		var REDSTONE = '#forge:dusts/redstone', GLOWSTONE = '#forge:dusts/glowstone'
		colors.forEach(c => {
			var wool = i[c + '_wool']
			// ring of 8 same-kind cables around a dye -> 8 coloured (every kind)
			;['glass', 'covered', 'smart', 'covered_dense', 'smart_dense'].forEach(kind => {
				shaped.push([Item.of(cable(c, kind), 8), ['CCC', 'CDC', 'CCC'], { C: tag(kind), D: dye(c) }, e, 'ae2_' + c + '_' + kind + '_cable_ring'])
				shapeless.push([Item.of(cable(c, kind)), [tag(kind), dye(c)], e, 'ae2_' + c + '_' + kind + '_cable_dye'])
			})
			// covered: glass + wool; dense -> 4
			shapeless.push([Item.of(cable(c, 'covered')), [cable(c, 'glass'), wool], e, 'ae2_' + c + '_covered_cable_wool'])
			shapeless.push([Item.of(cable(c, 'covered'), 4), [cable(c, 'covered_dense')], e, 'ae2_' + c + '_covered_cable_split'])
			// smart: fluix glass + redstone + glowstone + wool; own covered + redstone + glowstone; any covered + dusts + dye; dense -> 4
			shapeless.push([Item.of(cable(c, 'smart')), [i.fluix_glass_cable, REDSTONE, GLOWSTONE, wool], e, 'ae2_' + c + '_smart_cable_wool'])
			shapeless.push([Item.of(cable(c, 'smart')), [cable(c, 'covered'), REDSTONE, GLOWSTONE], e, 'ae2_' + c + '_smart_cable_dusts'])
			shapeless.push([Item.of(cable(c, 'smart')), [tag('covered'), REDSTONE, GLOWSTONE, dye(c)], e, 'ae2_' + c + '_smart_cable_dusts_dye'])
			shapeless.push([Item.of(cable(c, 'smart'), 4), [cable(c, 'smart_dense')], e, 'ae2_' + c + '_smart_cable_split'])
			// dense: 2x2 of the thin kind; smart dense from covered dense + dusts
			shaped.push([Item.of(cable(c, 'covered_dense')), ['CC', 'CC'], { C: cable(c, 'covered') }, e, 'ae2_' + c + '_covered_dense_cable_pack'])
			shaped.push([Item.of(cable(c, 'smart_dense')), ['CC', 'CC'], { C: cable(c, 'smart') }, e, 'ae2_' + c + '_smart_dense_cable_pack'])
			shapeless.push([Item.of(cable(c, 'smart_dense')), [cable(c, 'covered_dense'), REDSTONE, GLOWSTONE], e, 'ae2_' + c + '_smart_dense_cable_dusts'])
		})
		// fluix (uncoloured): ring of 8 coloured cables around a water bucket (#ae2:can_remove_color) -> 8 fluix; dense -> 4
		;['glass', 'covered', 'smart', 'covered_dense', 'smart_dense'].forEach(kind => {
			shaped.push([Item.of(i['fluix_' + kind + '_cable'], 8), ['CCC', 'CDC', 'CCC'], { C: tag(kind), D: '#ae2:can_remove_color' }, e, 'ae2_fluix_' + kind + '_cable_ring'])
		})
		shapeless.push([Item.of(i.fluix_covered_cable, 4), [i.fluix_covered_dense_cable], e, 'ae2_fluix_covered_cable_split'])
		shapeless.push([Item.of(i.fluix_smart_cable, 4), [i.fluix_smart_dense_cable], e, 'ae2_fluix_smart_cable_split'])
		bulk('shaped', shaped)
		bulk('shapeless', shapeless)
	}

	console.log('[feature-ports] tier 1: ' + count + ' recipes (1 name tag, ' + splashing.length + ' splashing, ' + smelting.length +
		' recycle smelting, ' + (shaped.length + shapeless.length) + ' AE2 cable)')
})
