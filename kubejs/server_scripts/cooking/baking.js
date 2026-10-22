// Oven: creativeengineering:baking recipes (NCH.ceBaking) and the oven / bakeware recipes.
// Block, GUI and recipe type: patcherMod oven/ package. Design and tests: feature_writeup/oven.md.
//
// Baking overhaul (oven.md 2.6): every cake, pie and cookie recipe of every mod is removed (slice -> whole recipes
// stay) and each family is rebuilt on one shared base, flavoured by 2 of its fruit or flavour item:
//   cake base    raw: 3 flour/cornmeal + 2 egg + 2 sugar, or 3 dough + egg + 2 sugar (manual craft)
//                baked: 2 flour/cornmeal + 2 egg + 2 sugar, or 2 dough + egg + 2 sugar, or a raw base (oven)
//   pie crust    3 flour/cornmeal + egg + milk, or 3 dough + milk -> 3 (manual craft)
//   frosting     sugar + 2 milk -> 2 (manual craft)
//   cookie dough dough + 2 sugar (manual craft)
//   cake         baked base + frosting + 2 flavour (manual craft), or raw base + frosting + 2 flavour (oven).
//                Plain cake: 3 frosting, or 2 frosting + a red berry.
//   pie          oven: crust + 2 fruit + 2 sugar (honey bottles count as sugar), or crust + a full can of that
//                pie's filling. Filling: 2 fruit + 2 sugar in the cooking pot, an empty can in the container slot.
//                Filling + crust also crafts by hand (canning.js, from the can json's "pie" field).
//   cookie       oven only: cookie dough + 2 flavour -> 12
// "Manual craft" = an NCH id ending in _manual_only, which Create's mixer and mechanical crafter skip. The bases have
// no Create or IE machine recipe (every other mod's is removed below).
//
// A repeated ingredient is written once with a count, n(id, 2). With CEConfig [cooking] ovenIngredientCounts off
// (the default) NCH.ceBaking turns that into 2 slots of 1; with it on the oven takes 2 from one slot. Sugar is
// #forge:sugar, as the pack-wide sugar swap in cooking.js gives other recipes.
//
// Bakeware (the oven's tool slot, never used up):
//   baking sheet  cookies, tarts
//   pie tin       pies
//   cake pan      cakes, cake base
//   muffin tin    muffins, plated on paper

ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH
	// One oven slot holding `count` of an item or tag.
	var n = (id, count) => ({ id: id, count: count })

	var SHEET = { exp: 0.1, time: 200, tool: i.t_bakeware_baking_sheet }
	var PIE = { exp: 0.1, time: 200, tool: i.t_bakeware_pie_tin }
	var CAKE = { exp: 0.1, time: 200, tool: i.t_bakeware_cake_pan }
	var MUFFIN = { exp: 0.1, time: 200, tool: i.t_bakeware_muffin_tin, container: i.paper }
	var SWEET = [i.any_sugar, i.honey_bottle]   // one pie sugar slot

	// [result key, flavour (2 per cake / pie / batch of cookies), pie filling can key (null: none)]
	var CAKES = [
		['adzuki_cake', i.roasted_adzuki_beans], ['banana_cake', i.t_fruits_banana],
		['chocolate_cake', i.t_bars_chocolate],          // Crafts & Additions' (Neapolitan's merges into it, item_merges.js)
		['honey_cake', i.honey_bottle], ['mint_cake', i.mint_leaves], ['strawberry_cake', i.t_fruits_strawberry],
		['vanilla_cake', i.dried_vanilla_pods], ['coffee_cake', i.farmersrespite_coffee_beans],
		['mangosteen_cake', i.t_fruits_mangosteen],
	]
	var PIES = [
		['apple_pie', i.apple, 'canned_apple_pie_filling'],
		['chocolate_pie', i.t_bars_chocolate, 'canned_chocolate_pie_filling'],
		['chorus_pie', i.chorus_fruit, 'canned_chorus_pie_filling'],
		['mulberry_pie', i.mulberry, 'canned_mulberry_pie_filling'],
		['pumpkin_pie', i.pumpkin_slice, 'canned_pumpkin_pie_filling'],
		['rose_hip_pie', i.rose_hips, 'canned_rose_hip_pie_filling'],
		['salmonberry_pie', i.t_fruits_salmonberries, 'canned_salmonberry_pie_filling'],
		['durian_pie', i.durian_flesh, 'canned_durian_pie_filling'],
		['pineapple_pie', i.pineapple_slice, 'canned_pineapple_pie_filling'],
		['sweet_berry_cheesecake', i.sweet_berries, 'canned_sweet_berry_pie_filling'],
	]
	var COOKIES = [
		['cookie', i.cocoa_beans], ['honey_cookie', i.honey_bottle], ['sweet_berry_cookie', i.sweet_berries],
		['green_tea_cookie', i.green_tea_leaves], ['bayberry_cookie', i.t_fruits_bayberry],
		['cranberry_cookie', i.t_fruits_cranberry], ['persimmon_cookie', i.dried_persimmon],
		['lemon_cookie', i.t_fruits_lemon], ['mulberry_cookie', i.mulberry], ['uncanny_cookies', i.mite_crust],
		['cherry_cookie', i.cherry],                     // abnormals_delight, Vinery's cherry
	]

	//######################### Removals #########################
	// Every recipe that makes one of these, from any mod, except slices -> whole and this pack's own (made below).
	var REMOVED_OUTPUTS = [i.cake, i.neapolitan_chocolate_cake, i.cake_base, i.cake_base_baked, i.pie_crust]
	CAKES.concat(PIES, COOKIES).forEach(c => REMOVED_OUTPUTS.push(i[c[0]]))
	REMOVED_OUTPUTS.forEach(id => R.remove({ output: id, not: [{ id: /_from_slices$/ }, { mod: 'creativeengineering' }] }, e))

	//######################### Bases #########################
	// Cake base: crafted raw by hand, baked in the oven.
	R.shapeless(i.cake_base, [Item.of(i.t_flour_or_cornmeal, 3), Item.of(i.eggs, 2), Item.of(i.any_sugar, 2)], e,
		'cake_base_flour_manual_only')
	R.shapeless(i.cake_base, [Item.of(i.any_dough, 3), i.eggs, Item.of(i.any_sugar, 2)], e, 'cake_base_dough_manual_only')
	R.ceBaking([n(i.t_flour_or_cornmeal, 2), n(i.eggs, 2), n(i.any_sugar, 2)], Item.of(i.cake_base_baked, 1), e,
		'cake_base_baked_flour', CAKE)
	R.ceBaking([n(i.any_dough, 2), i.eggs, n(i.any_sugar, 2)], Item.of(i.cake_base_baked, 1), e,
		'cake_base_baked_dough', CAKE)
	R.ceBaking([i.cake_base], Item.of(i.cake_base_baked, 1), e, 'cake_base_baked',
		{ exp: 0, time: 100, tool: i.t_bakeware_cake_pan })

	// Pie crust, 3 a craft.
	R.shapeless(Item.of(i.pie_crust, 3), [Item.of(i.t_flour_or_cornmeal, 3), i.eggs, i.milk], e, 'pie_crust_flour_manual_only')
	R.shapeless(Item.of(i.pie_crust, 3), [Item.of(i.any_dough, 3), i.milk], e, 'pie_crust_dough_manual_only')

	R.shapeless(Item.of(i.frosting, 2), [i.any_sugar, Item.of(i.milk, 2)], e, 'frosting_manual_only')
	R.shapeless(i.cookie_dough, [i.any_dough, Item.of(i.any_sugar, 2)], e, 'cookie_dough_manual_only')

	//######################### Cakes (cake pan) #########################
	// Plain cake: 3 frosting, or 2 frosting and a red berry on top (the vanilla look).
	var RED_BERRY = [i.sweet_berries, i.t_fruits_cranberry, i.cherry]
	R.shapeless(i.cake, [i.cake_base_baked, Item.of(i.frosting, 3)], e, 'cake_manual_only')
	R.shapeless(i.cake, [i.cake_base_baked, Item.of(i.frosting, 2), RED_BERRY], e, 'cake_berry_manual_only')
	R.ceBaking([i.cake_base, n(i.frosting, 3)], Item.of(i.cake, 1), e, 'cake', CAKE)
	R.ceBaking([i.cake_base, n(i.frosting, 2), RED_BERRY], Item.of(i.cake, 1), e, 'cake_berry', CAKE)
	CAKES.forEach(c => {
		R.shapeless(i[c[0]], [i.cake_base_baked, i.frosting, Item.of(c[1], 2)], e, c[0] + '_manual_only')
		R.ceBaking([i.cake_base, i.frosting, n(c[1], 2)], Item.of(i[c[0]], 1), e, c[0], CAKE)
	})
	// Chocolate cake also spouts: baked base + 500 mB chocolate (Crafts & Additions' recipe, swept by the removals).
	R.createFilling(i.chocolate_cake, [Fluid.of(global.fluid.chocolate, 500), i.cake_base_baked], e, 'chocolate_cake_spout')

	//######################### Pies (pie tin) #########################
	// The oven refuses an opened can (BakingRecipe skips partial CanServings), so only a full filling bakes a pie.
	PIES.forEach(p => {
		R.ceBaking([i.pie_crust, n(p[1], 2), SWEET, SWEET], Item.of(i[p[0]], 1), e, p[0], PIE)
		if (!p[2]) return
		R.fdCooking([p[1], p[1], SWEET, SWEET], Item.of(i[p[2]], 1), e, p[2] + '_pot', 0.1, 200, i.empty_can)
		R.ceBaking([i.pie_crust, i[p[2]]], Item.of(i[p[0]], 1), e, p[0] + '_from_filling', PIE)
	})

	//######################### Cookies (baking sheet) #########################
	COOKIES.forEach(c => {
		R.ceBaking([i.cookie_dough, n(c[1], 2)], Item.of(i[c[0]], 12), e, c[0], SHEET)
	})

	//######################### Muffins (muffin tin, paper) #########################
	// Pot recipes in cooking.js (Fruits Delight's), flour instead of dough: fruit, milk, egg, sugar, flour.
	// 4 in the oven, 2 in the pot, so the oven is the efficient route (one paper per muffin).
	R.ceBaking([n(i.t_fruits_blueberry, 2), i.flour, i.t_milk_bottle, i.eggs, i.any_sugar],
		Item.of(i.blueberry_muffin, 4), e, 'blueberry_muffin', MUFFIN)
	R.ceBaking([n(i.t_fruits_cranberry, 2), i.flour, i.t_milk_bottle, i.eggs, i.any_sugar],
		Item.of(i.cranberry_muffin, 4), e, 'cranberry_muffin', MUFFIN)

	//######################### Tarts (baking sheet) #########################
	// Fruits Delight pot recipes.
	R.ceBaking([i.t_fruits_lemon, i.pie_crust, i.eggs, i.any_sugar, i.t_milk_bottle],
		Item.of(i.lemon_tart, 2), e, 'lemon_tart', SHEET)
	R.ceBaking([i.t_fruits_fig, i.pie_crust, i.eggs, i.any_sugar, i.t_milk_bottle],
		Item.of(i.fig_tart, 2), e, 'fig_tart', SHEET)

	//######################### Oven and bakeware #########################
	// Oven: an iron-plate top over a hollow brick box.
	R.shaped(i.oven, ['PPP', 'B B', 'BBB'], { P: i.any_plate_iron, B: i.bricks }, e, 'oven')

	// Bakeware: one iron plate hammered on, step by step (Engineer's Hammer, on a cutting board or in the grid):
	// iron plate -> baking sheet -> muffin tin -> pie tin -> cake pan.
	R.hammering(i.baking_sheet, i.any_plate_iron, e, 'baking_sheet')
	R.hammering(i.muffin_tin, i.baking_sheet, e, 'muffin_tin')
	R.hammering(i.pie_tin, i.muffin_tin, e, 'pie_tin')
	R.hammering(i.cake_pan, i.pie_tin, e, 'cake_pan')
})
