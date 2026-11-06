// The pack's own food items and food rules: the mortar and pestle, dough blocks, cornmeal grinding and the burger
// rule (a table over Farmer's, Delightful, More Delight, Alex's Delight and Alex's Mobs burgers). Non-member file:
// recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's
// kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Tools #########################
		// Mortar and pestle: Old Guns' Gunsmith's Bench shape (now its Powder Pestle) in the crafting table.
		// TODO cooking/kitchen #10
		R.shaped(i.mortar_and_pestle, [' L ', 'G G', ' G '], { L: i.lever, G: i.polished_granite }, e, 'mortar_and_pestle')

		//######################### Dough blocks #########################
		// Dough block for every other dough (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/baking_ingredients.js, TODO baking/baked-goods #27):
		// 9 dough <-> block, as Create Compressed's dough block (the wheat dough block is in create/recipes.js)
		// G5 begin: rows naming an absent mod's id are filtered out below (Item.of of a missing id is air)
		;[
			[i.gingerdough, i.gingerdough_block], [i.cookie_dough, i.cookie_dough_block],
			[i.sweet_potato_dough, i.sweet_potato_dough_block], [i.oat_dough, i.oat_dough_block],
			[i.cucumber_dough, i.cucumber_dough_block], [i.sourdough, i.sourdough_block],
		// G5 end
		].filter(d => !global.CD.absentIn(d)).forEach(d => {
			R.shapeless(Item.of(d[1], 1), [Item.of(d[0], 9)], e)
			R.shapeless(Item.of(d[0], 9), [Item.of(d[1], 1)], e)
		})

		//######################### Cornmeal #########################
		// creativeengineering:cornmeal (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cornmeal.js), TODO dishes/ingredients #1. Corn and kernels
		// (corn_delight:corn_seeds) crush to it; a 9-block (corn crate, kernel bag) gives the same per-item ratio.
		// The Millstone mirrors the crushing recipes below; Corn Delight's own corn and wild corn milling are removed (wild corn
		// now grinds as corn). Corn gives no straw: the knife and IE crusher recipes that did keep their kernels only
		// (corn_delight/recipes.js). IE crusher: immersiveengineering/recipes.js; PneumaticCraft: pneumaticcraft/recipes.js.
		if (Platform.isLoaded('create') && Platform.isLoaded('corn_delight')) {
			R.remove({ id: 'corn_delight:integration/create/milling/corn' }, e)
			R.remove({ id: 'corn_delight:integration/create/milling/wild_corn' }, e)
			// Crushing wheels, Millstone (same outputs) and mortar and pestle (foodGrinding; TODO cooking/kitchen #10,
			// dishes/ingredients #14). Corn and wild corn 1.5 cornmeal + 0.25 kernels, kernels 0.333, crate 13.5, kernel bag 3.
			// The mortar matches the machines' cornmeal average exactly: corn and wild corn 2 -> 3, kernels 3 -> 1. Crates and
			// bags are machine-only, as are the kernels.
			// Corn and wild corn also give a Corncob 10% of the time (corn_delight.md 2.1); a byproduct, so not from the mortar.
			R.foodGrinding([i.cornmeal, Item.of(i.cornmeal, 1).withChance(0.5), Item.of(i.corn_seeds, 1).withChance(0.25),
				Item.of(i.corncob, 1).withChance(0.1)], i.t_crops_corn, e, 'corn_cornmeal', { mortar: { count: 3, inputs: 2 } })
			R.foodGrinding([i.cornmeal, Item.of(i.cornmeal, 1).withChance(0.5), Item.of(i.corn_seeds, 1).withChance(0.25),
				Item.of(i.corncob, 1).withChance(0.1)], i.wild_corn, e, 'wild_corn_cornmeal', { mortar: { count: 3, inputs: 2 } })
			R.foodGrinding([Item.of(i.cornmeal, 13), Item.of(i.cornmeal, 1).withChance(0.5), Item.of(i.corn_seeds, 2),
				Item.of(i.corn_seeds, 1).withChance(0.25)], i.corn_crate, e, 'corn_crate_cornmeal', { mortar: false })
			R.foodGrinding([Item.of(i.cornmeal, 1).withChance(0.333)], i.t_seeds_corn, e, 'corn_kernels_cornmeal')
			R.foodGrinding([Item.of(i.cornmeal, 3)], i.corn_kernel_bag, e, 'corn_kernel_bag_cornmeal', { mortar: false })
			// Corncob: 4 kernels + 0.5 cornmeal on the machines, 3 kernels from the mortar (cutting board 2: corn_delight/recipes.js)
			R.foodGrinding([Item.of(i.corn_seeds, 4), Item.of(i.cornmeal, 1).withChance(0.5)], i.corncob, e, 'corncob_kernels',
				{ mortar: { count: 3, inputs: 1 } })
		}

		//######################### Burgers #########################
		// Burgers: shaped, bread or cornbread (any #forge:bread), cheese wedges never milk; the assembly is the only
		// automation. Pattern: toppings on top, patty in the middle, bread at the bottom.
		// Assemblies on bread need a different first step each, or Create only ever picks one of them: CCK's bacon, chicken
		// and egg sandwiches already start with cooked bacon, cooked chicken and a fried egg. So the egg burger starts on
		// the patty instead of the bread.
		if (global.has('create', 'farmersdelight', 'delightful', 'moredelight', 'alexsdelight', 'alexsmobs')) {
			;[
				'farmersdelight:hamburger', 'create_central_kitchen:sequenced_assembly/hamburger', // G5 (R.remove ids)
				'delightful:food/cheeseburger', 'delightful:food/cheeseburger_from_milk',
				'delightful:food/deluxe_cheeseburger', 'delightful:food/deluxe_cheeseburger_from_milk',
				'moredelight:simple_hamburger', 'moredelight:hamburger_with_egg', 'moredelight:loaded_hamburger',
				'alexsdelight:bison_burger', 'alexsmobs:kangaroo_burger',
			].forEach(id => R.remove({ id: id }, e))
			var burgerCheese = i.t_cheese_wedge
			// Patty rule (creativedelight/members #30 R11): Veggies Delight's cooked vegetarian patty fills the patty slot of
			// the three More Delight burgers it had shapeless versions of (veggiesdelight/recipes.js removes those)
			var mdPatty = Platform.isLoaded('veggiesdelight') ? [i.beef_patty, i.cooked_vegetarian_patty] : i.beef_patty
			// Vintage Delight's two burgers follow the same rule: Creative Delight members #3 (vintagedelight/recipes.js)
			;[
				// [key, pattern, keys, assembly base, deploy steps in order]
				['hamburger', ['STO', ' P ', ' B '], { S: i.salad_ingredients, T: i.tomato, O: i.onion, P: i.beef_patty, B: i.t_bread },
					i.t_bread, [i.salad_ingredients, i.beef_patty, i.tomato, i.onion]],
				['cheeseburger', ['STO', 'CP ', ' B '], { S: i.salad_ingredients, T: i.tomato, O: i.onion, C: burgerCheese, P: i.beef_patty, B: i.t_bread },
					i.t_bread, [burgerCheese, i.beef_patty, i.salad_ingredients, i.tomato, i.onion]],
				['deluxe_cheeseburger', ['STO', 'PAP', 'CBC'], { S: i.salad_ingredients, T: i.tomato, O: i.onion, P: i.beef_patty, A: i.cooked_bacon, C: burgerCheese, B: i.t_bread },
					i.t_bread, [i.onion, i.beef_patty, burgerCheese, i.beef_patty, burgerCheese, i.cooked_bacon, i.salad_ingredients, i.tomato]],
				['simple_hamburger', ['P', 'B'], { P: mdPatty, B: i.t_bread },
					i.t_bread, [mdPatty]],
				['hamburger_with_egg', ['E', 'P', 'B'], { E: i.cooked_eggs, P: mdPatty, B: i.t_bread },
					i.beef_patty, [i.cooked_eggs, i.t_bread]],   // the assembly stays on the beef patty
				['loaded_hamburger', ['CTO', 'APE', ' B '], { C: i.cabbage, T: i.tomato, O: i.onion, A: i.cooked_bacon, P: mdPatty, E: i.cooked_eggs, B: i.t_bread },
					i.t_bread, [i.tomato, mdPatty, i.cabbage, i.cooked_bacon, i.cooked_eggs, i.onion]],
				['bison_burger', ['STR', ' P ', ' B '], { S: i.salad_ingredients, T: i.tomato, R: i.beetroot, P: i.bison_patty, B: i.t_bread },
					i.t_bread, [i.bison_patty, i.salad_ingredients, i.tomato, i.beetroot]],
				['kangaroo_burger', ['S C', 'K K', ' B '], { S: i.salad_ingredients, C: i.carrot, K: i.t_cooked_kangaroo, B: i.t_bread },
					i.t_bread, [i.t_cooked_kangaroo, i.t_cooked_kangaroo, i.salad_ingredients, i.carrot]],
			].forEach(([key, pattern, keys, base, steps]) => {
				R.shaped(i[key], pattern, keys, e, key + '_manual_only')
				var inc = R.incomplete(key)
				R.createSequencedAssembly(i[key], base,
					steps.map(s => e.recipes.create.deploying(inc, [inc, Ingredient.of(s)])),
					[key], e, key + '_sequenced_assembly', 1)
			})
		}
	})
})
