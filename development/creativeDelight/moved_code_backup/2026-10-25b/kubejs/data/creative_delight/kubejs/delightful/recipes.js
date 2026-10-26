// requires: delightful
// Recipes of the Delightful 3.8.1 items backported into the repack (feature_writeup/delightful.md 3.2). Creative
// Delight member script: recipe ids are creative_delight:<type>/<suffix> (global.CD.recipes). The 3.8.1 recipes are
// read from the donor jar and rebuilt here; only the ones whose inputs the pack has. The new items are registered
// only by the repack, so this script waits for it (the 3.4.1 jar has no cantaloupe_seeds).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('delightful') || !Item.exists(global.item.cantaloupe_seeds)) return

	var i = global.item
	var R = global.NCH
	var n = (id, count) => ({ id: id, count: count })
	// as cooking/baking.js
	var PIE = { exp: 0.1, time: 200, tool: i.t_bakeware_pie_tin }
	var SWEET = [i.any_sugar, i.honey_bottle]

	global.CD.recipes(() => {

		//######################### Cantaloupe #########################
		R.shapeless(i.cantaloupe_seeds, [i.cantaloupe_slice], e, 'cantaloupe_seeds')
		R.shapeless(i.cantaloupe_bread, [i.any_dough, i.t_fruits_cantaloupe, i.any_sugar], e, 'cantaloupe_bread')
		R.shapeless(i.wrapped_cantaloupe, [i.t_fruits_cantaloupe, i.cooked_bacon], e, 'wrapped_cantaloupe')
		R.shaped(i.cantaloupe_popsicle, [' cc', 'icc', 'si '], { c: i.t_fruits_cantaloupe, i: i.t_ice_cubes, s: i.any_stick }, e, 'cantaloupe_popsicle')
		R.shaped(i.cantaloupe_popsicle, ['im', 'si'], { m: i.cantaloupe, i: i.t_ice_cubes, s: i.any_stick }, e, 'cantaloupe_popsicle_from_cantaloupe')
		// Served from a whole Cantaloupe (the pot's container slot), placed as a feast of 4
		R.fdCooking([i.rice, i.cooked_mutton, i.onion, i.t_berries, i.t_seeds], i.stuffed_cantaloupe_block, e, 'stuffed_cantaloupe', 0.35, 400, i.cantaloupe)

		//######################### Gummies #########################
		// Pot: 1 each; heated mixer with a honey block: 9
		var GUMMIES = [[i.cantaloupe_gummy, i.t_fruits_cantaloupe], [i.matcha_gummy, i.t_matcha]]
		GUMMIES.forEach(g => {
			var name = g[0].split(':')[1]
			R.fdCooking([g[1], i.any_sugar, i.honey_bottle, i.kelp], g[0], e, name, 1.0, 200)
			R.createMixing(Item.of(g[0], 9), [g[1], i.any_sugar, i.honey_block, i.kelp], e, name, 'heated')
		})

		//######################### Cactus #########################
		R.fdCooking([i.raw_pork, i.t_crops_cactus, i.tomato_sauce, i.t_hot_spices, i.t_crops_corn], i.cactus_chili, e, 'cactus_chili', 1.0, 200, i.bowl)
		// The Cactus Soup Cup is the Copper Pot's serving (cup_conversion in the repack)
		R.fdCooking([i.t_crops_cactus, i.rice, i.tomato, i.onion], i.cactus_soup, e, 'cactus_soup', 1.0, 200, i.bowl)

		//######################### Baklava #########################
		// A pie here (oven.md 2.6): pie crust + 2 roasted nuts + honey + sweet in the oven, pie tin. Upstream's grid
		// recipe (dough, nuts, honey, citrus or sugar, hot spice) is not added.
		R.ceBaking([i.pie_crust, n(i.t_cooked_nuts, 2), i.honey_bottle, SWEET], Item.of(i.baklava, 1), e, 'baklava', PIE)
		R.fdCutting(i.baklava, i.any_knife, Item.of(i.baklava_slice, 4), e, 'baklava')
		R.shapeless(i.baklava, [i.baklava_slice, i.baklava_slice, i.baklava_slice, i.baklava_slice], e, 'baklava_from_slices')

		//######################### Milkshakes #########################
		// Grid: 3 bottles + ice cream + milk -> 3. The cauldrons (milk cauldron + ice cream) are Neapolitan's way, in the repack.
		R.shapeless(Item.of(i.matcha_milkshake, 3), [i.glass_bottle, i.glass_bottle, i.glass_bottle, i.matcha_ice_cream, i.milk], e, 'matcha_milkshake')
		R.shapeless(Item.of(i.salmonberry_milkshake, 3), [i.glass_bottle, i.glass_bottle, i.glass_bottle, i.salmonberry_ice_cream, i.milk], e, 'salmonberry_milkshake')

		//######################### Nuts #########################
		R.smelting(i.roasted_acorn, i.t_nuts_acorn, e, 'roasted_acorn', 0.35, 200)
		R.smoking(i.roasted_acorn, i.t_nuts_acorn, e, 'roasted_acorn', 0.35, 100)
		R.campfireCooking(i.roasted_acorn, i.t_nuts_acorn, e, 'roasted_acorn', 0.35, 600)
		R.shapeless(Item.of(i.nut_dough, 3), [i.t_nut_butter, i.eggs], e, 'nut_dough')

		//######################### Knives #########################
		R.shaped(i.aluminum_knife, ['m', 's'], { m: i.ingot_aluminum, s: i.any_stick }, e, 'aluminum_knife')
		R.shaped(i.certus_quartz_knife, ['m', 's'], { m: i.t_gems_certus_quartz, s: i.any_stick }, e, 'certus_quartz_knife')
		// 1.20's smithing template is not in AE2 1.19.2: 1.19.2 smithing, Certus Quartz Knife + Fluix Block
		R.smithing(i.fluix_knife, i.certus_quartz_knife, i.fluix_block, e, 'fluix_knife')
	})
})
