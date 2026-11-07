// requires: creativedelight
// Oven: creativeengineering:baking recipes (NCH.ceBaking) and the oven / bakeware recipes. Moved from the pack's
// server_scripts/cooking/baking.js 2026-10-25, rows unchanged; recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). The cake / pie / cookie tables stay whole here (they span many mods); each row is one
// global.CD.food family call, which also sweeps that output's other recipes.
// Block, GUI and recipe type: patcherMod oven/ package. Design and tests: feature_writeup/oven.md.
//
// Baking overhaul (oven.md 2.6): every cake, pie and cookie recipe of every mod is removed (slice -> whole recipes
// stay) and each family is rebuilt on one shared base, flavoured by 2 of its fruit or flavour item:
//   cake base    raw: 3 flour/cornmeal + 2 egg + 2 sugar, or 3 dough + egg + 2 sugar (manual craft)
//                baked: 2 flour/cornmeal + 2 egg + 2 sugar, or 2 dough + egg + 2 sugar, or a raw base (oven)
//   pie crust    3 flour/cornmeal + egg + milk, or 3 dough + milk -> 3 (manual craft)
//   frosting     sugar + 2 milk -> 2 (manual craft), or the frosting fluid (mixer, compactor, spout)
//   cookie dough dough + 2 sugar (manual craft)
//   cake         baked base + frosting + 2 flavour (manual craft), or raw base + frosting + 2 flavour (oven).
//                Plain cake: 3 frosting, or 2 frosting + a red berry.
//   pie          oven: crust + 2 fruit + 2 sugar (honey bottles count as sugar), or crust + a full can of that
//                pie's filling. Filling: 2 fruit + 2 sugar in the cooking pot, an empty can in the container slot.
//                Filling + crust also crafts by hand (creativeengineering/canning.js, from the can json's "pie" field).
//   cookie       oven only: cookie dough + 2 flavour -> 12
//   hand pie     oven only: pie crust + the mod's filling -> 1 (baking sheet)
// "Manual craft" = an NCH id ending in _manual_only, which Create's mixer and mechanical crafter skip. The bases have
// no Create or IE machine recipe (every other mod's is removed below), except frosting through its fluid.
//
// A repeated ingredient is written once with a count, n(id, 2). With CEConfig [cooking] ovenIngredientCounts off
// (the default) NCH.ceBaking turns that into 2 slots of 1; with it on the oven takes 2 from one slot. Sugar is
// any sweetener (i.any_sweetener), as the pack-wide sugar swap in minecraft/recipes.js gives other recipes.
//
// Bakeware (the oven's tool slot, never used up):
//   baking sheet  cookies, tarts
//   pie tin       pies
//   cake pan      cakes, cake base
//   muffin tin    muffins, plated on paper

ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH
	var F = global.CD.food(e)

	global.CD.recipes(() => {
		// One oven slot holding `count` of an item or tag.
		var n = (id, count) => ({ id: id, count: count })

		var CAKE = { exp: 0.1, time: 200, tool: i.t_bakeware_cake_pan }

		// [result key, flavour (2 per cake / pie / batch of cookies), pie filling can key (null: none) or a second cake / cookie flavour]
		// G5 begin: CAKES, PIES and COOKIES only reach the global.CD.food family calls below, which skip absent mods' ids
		var CAKES = [
			['adzuki_cake', i.roasted_adzuki_beans], ['banana_cake', i.t_fruits_banana],
			['chocolate_cake', i.t_bars_chocolate],          // Crafts & Additions' (Neapolitan's merges into it, item_merges.js)
			['honey_cake', i.honey_bottle, i.honeycomb],     // half honey, half comb (TODO baking/baked-goods #26)
			['mint_cake', i.mint_leaves], ['strawberry_cake', i.t_fruits_strawberry],
			['vanilla_cake', i.dried_vanilla_pods], ['coffee_cake', i.roasted_coffee_beans],
			['mangosteen_cake', i.t_fruits_mangosteen],
			['carrot_cake', i.carrot],                       // Veggies Delight's (veggies_delight.md 4.8)
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
			['sweet_potato_pie', i.baked_sweet_potato, 'canned_sweet_potato_pie_filling'],   // Veggies Delight's
			['truffle_pie', i.truffle, null],                // CCK's (dead: gated on Environmental), from Fungi Delight's truffle (fungi_delight.md 5.1.6)
			['chorus_fruit_pie', i.chorus_fruit_grain, null], // End's Delight's; no can: Ender's chorus pie owns the chorus filling
		]
		var COOKIES = [
			['cookie', i.cocoa_beans], ['honey_cookie', i.honey_bottle], ['sweet_berry_cookie', i.sweet_berries],
			['green_tea_cookie', i.green_tea_leaves], ['bayberry_cookie', i.t_fruits_bayberry],
			['cranberry_cookie', i.t_fruits_cranberry], ['persimmon_cookie', i.dried_persimmon],
			['lemon_cookie', i.t_fruits_lemon], ['mulberry_cookie', i.mulberry], ['uncanny_cookies', i.mite_crust],
			['cherry_cookie', i.cherry],                     // abnormals_delight, Vinery's cherry
			['oatmeal_cookie', i.raw_oats],                  // Vintage Delight (was 2 raw oats + honey -> 8 in the grid)
			// Baking audit 2026-10-27 (food_families.md `cookie`): each was a grid or furnace recipe -> 8 (the butter cookie 3)
			['bat_cookie', i.smoked_bat_wing],               // Miner's Delight
			['chorus_cookie', i.chorus_fruit_grain],         // End's Delight
			// Maple Cookie (Abnormals Delight's): rusticdelight/recipes.js, syrup + honey (creativedelight/members #16)
			['blazecookie', i.blaze_rod], ['ghastcookie', i.ghasttentacle], ['guardiancookie', i.guardianfillet],
			['magicbuttercookie', i.magicalbutter], ['magmacookie', i.magma_cream], ['mitecookie', i.rawendermite],
			['pearlcookie', i.ender_pearl],                  // Hostile Delight's seven
			// Create Confectionery's Gingerbread Man (was pressed from gingerdough); Gingerbread itself is a dessert
			['gingerbread_man', i.gingerdough, i.any_sweetener],

		]
		// G5 end

		//######################### Removals #########################
		// Every recipe that makes one of these, from any mod, except slices -> whole and this pack's own (made below).
		// The table rows are swept by their family call.
		var REMOVED_OUTPUTS = [i.cake, i.neapolitan_chocolate_cake, i.cake_base, i.cake_base_baked, i.pie_crust] // G5 (R.remove filters)
		REMOVED_OUTPUTS.forEach(id => R.remove({ output: id, not: [{ id: /_from_slices$/ }, { mod: 'creativeengineering' }] }, e))

		//######################### Bases #########################
		// Cake base: crafted raw by hand, baked in the oven.
		R.shapeless(i.cake_base, [i.t_flour_or_cornmeal, i.t_flour_or_cornmeal, i.t_flour_or_cornmeal, i.eggs, i.eggs, i.any_sweetener, i.any_sweetener], e,
			'cake_base_flour_manual_only')
		R.shapeless(i.cake_base, [i.any_dough, i.any_dough, i.any_dough, i.eggs, i.any_sweetener, i.any_sweetener], e, 'cake_base_dough_manual_only')
		R.ceBaking([n(i.t_flour_or_cornmeal, 2), n(i.eggs, 2), n(i.any_sweetener, 2)], Item.of(i.cake_base_baked, 1), e,
			'cake_base_baked_flour', CAKE)
		R.ceBaking([n(i.any_dough, 2), i.eggs, n(i.any_sweetener, 2)], Item.of(i.cake_base_baked, 1), e,
			'cake_base_baked_dough', CAKE)
		R.ceBaking([i.cake_base], Item.of(i.cake_base_baked, 1), e, 'cake_base_baked',
			{ exp: 0, time: 100, tool: i.t_bakeware_cake_pan })

		// Pie crust, 3 a craft.
		R.shapeless(Item.of(i.pie_crust, 3), [i.t_flour_or_cornmeal, i.t_flour_or_cornmeal, i.t_flour_or_cornmeal, i.eggs, i.milk], e, 'pie_crust_flour_manual_only')
		R.shapeless(Item.of(i.pie_crust, 3), [i.any_dough, i.any_dough, i.any_dough, i.milk], e, 'pie_crust_dough_manual_only')

		R.shapeless(Item.of(i.frosting, 2), [i.any_sweetener, i.milk, i.milk], e, 'frosting_manual_only')
		// Frosting fluid (TODO dishes/ingredients #2): the Create route. Heated mixer sugar + 250 mB milk -> 500 mB, the
		// grid's 2 frosting; the compactor presses 250 mB into 1 frosting; a spout tops a baked base with 750 mB (below).
		R.createMixing(Fluid.of(global.fluid.frosting, 500), [i.any_sweetener, Fluid.of(global.fluid.milk, 250)], e, 'frosting', 'heated')
		R.createCompacting(i.frosting, [Fluid.of(global.fluid.frosting, 250)], e, 'frosting')
		R.shapeless(i.cookie_dough, [i.any_dough, i.any_sweetener, i.any_sweetener], e, 'cookie_dough_manual_only')

		//######################### Cakes (cake pan) #########################
		// Plain cake: 3 frosting, or 2 frosting and a red berry on top (the vanilla look).
		var RED_BERRY = [i.sweet_berries, i.t_fruits_cranberry].concat(global.has(i.cherry) ? [i.cherry] : [])
		R.shapeless(i.cake, [i.cake_base_baked, Item.of(i.frosting, 3)], e, 'cake_manual_only')
		R.shapeless(i.cake, [i.cake_base_baked, Item.of(i.frosting, 2), RED_BERRY], e, 'cake_berry_manual_only')
		R.ceBaking([i.cake_base, n(i.frosting, 3)], Item.of(i.cake, 1), e, 'cake', CAKE)
		R.ceBaking([i.cake_base, n(i.frosting, 2), RED_BERRY], Item.of(i.cake, 1), e, 'cake_berry', CAKE)
		CAKES.forEach(c => F.cakeBake(c[1], i[c[0]], c[2]))
		// Chocolate cake also spouts: baked base + 500 mB chocolate (Crafts & Additions' recipe, swept by the removals).
		R.createFilling(i.chocolate_cake, [Fluid.of(global.fluid.chocolate, 500), i.cake_base_baked], e, 'chocolate_cake_spout')
		// Plain cake spouts too: baked base + 750 mB frosting fluid (3 frosting).
		R.createFilling(i.cake, [Fluid.of(global.fluid.frosting, 750), i.cake_base_baked], e, 'cake_frosting_spout')

		//######################### Pies (pie tin) #########################
		// The oven refuses an opened can (BakingRecipe skips partial CanServings), so only a full filling bakes a pie.
		PIES.forEach(p => F.pieBake(p[1], i[p[0]], p[2]))

		//######################### Cookies (baking sheet) #########################
		COOKIES.forEach(c => F.cookieBake(c[1], i[c[0]], c[2]))

		//######################### Muffins (muffin tin, paper) #########################
		// The family call bakes 4 in the oven (flour, one paper each) and removes the mods' Cooking Pot recipes: muffins
		// are not made in the pot (owner 2026-10-29).
		F.muffinBake(i.t_fruits_blueberry, i.blueberry_muffin) // G5
		F.muffinBake(i.t_fruits_cranberry, i.cranberry_muffin) // G5
		F.muffinBake(i.baked_sweet_potato, i.sweet_potato_cupcake)   // Veggies Delight's, named Sweet Potato Muffin (its sweet potato dough craft stays)

		//######################### Tarts (baking sheet) #########################
		// Fruits Delight pot recipes.
		F.tartBake(i.t_fruits_lemon, i.lemon_tart) // G5
		F.tartBake(i.t_fruits_fig, i.fig_tart) // G5

		// Oceanic Delight's Sponge Pie (renamed, assets oceanic_delight/names.js): crust + sponge + prismarine shard + 2 sugar,
		// as its own grid recipe had it. No filling can.
		F._sweep(i.sponge_cake)
		R.ceBaking([i.pie_crust, i.sponge, i.prismarine_shard, F.SWEET, F.SWEET], Item.of(i.sponge_cake, 1), e, 'sponge_cake', F.ware.pie)

		//######################### Hand pies (baking sheet) #########################
		// One-serving pies with a savory or odd filling: crust + the mod's own filling, oven only (food_families.md
		// `pie/small`). The mods' grid recipes are swept.
		var HAND_PIES = [ // G5 begin (family calls only)
			['rottenpie', [n(i.cookedrottenham, 2), i.eggs]],                 // Hostile Delight (was + wheat)
			['greenpie', [i.moss, i.turtle_egg, i.rottenham]],                // Hostile Delight (was the same, no crust)
			['chorus_flower_pie', [n(i.dried_chorus_flower, 2), i.poppy, F.SWEET]],   // End's Delight (was on dough)
		] // G5 end
		HAND_PIES.forEach(p => F.handPieBake(p[1], i[p[0]]))

		//######################### Breads (baking sheet) #########################
		// Veggies Delight's cauliflower bread: Creative Delight members #2 (kubejs/data/creativedelight/kubejs/veggiesdelight/recipes.js)

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
})
