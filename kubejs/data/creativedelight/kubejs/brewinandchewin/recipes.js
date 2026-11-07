// requires: creativedelight
// requires: brewinandchewin
// Brewin' and Chewin' dishes (owner, 2026-10-31: the dishes stay in Creative Delight, added only while Brewin' is
// loaded; Brewin' itself is a Creative Brewing member, feature_writeup/creative_brewing.md 4.4 Q1). Moved from the pack's
// server_scripts/cooking/cooking.js 2026-10-31, rows unchanged. Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes; were creativeengineering:). The egg solution fluid is CD's
// (create/recipes.js); the heatless meal rule is farmersdelight/recipes.js "Meals".
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {
		//######################### Egg solution #########################
		// The pack's egg solution fluid (create/recipes.js in Creative Delight): 250 mB stands in for one egg and its milk.
		R.createMixing(i.vegetable_omelet, [i.t_cheese_wedge, i.onion, i.carrot, i.bowl, Fluid.of(f.egg_solution, 500)], e,
			'vegetable_omelet_egg_solution', 'heated')
		// (quiche: oven only since merges/dishes #8, no egg solution assembly; see Quiche below)

		//######################### Prepared #########################
		// Scarlet Pierogi: beetroot in place of nether wart
		R.replaceInput({ id: 'brewinandchewin:cooking/scarlet_pierogi' }, i.nether_wart, i.beetroot, e)

		// Supreme Pizza (owner 2026-10-25, rustic_delight.md 5.5; slots owner 2026-11-01, creativedelight/members #20,
		// merges/dishes #8). Pizza family rule (food_families.md `pizza/whole`): bottom row any dough, tomato sauce, any
		// dough; one topping slot each above: cheese, cooked meat (not fish), cooked fish, any mushroom, a bell pepper (raw,
		// sliced or roasted, any colour), onion. One shaped recipe (manual only) plus one assembly on the dough, as the Vegan
		// Pizza. Was: bell pepper, cheese wedge, onion, mushroom, tomato sauce, cooked meat (fish counted) on one wheat dough.
		// Vintage Delight's cheese and meat pizzas already sit on dough, sauce, dough (any dough).
		var PIZZA_PEPPER = [i.t_vegetables_bell_pepper, i.t_roasted_bell_pepper, i.t_roasted_bell_pepper_slice]
		R.remove({ id: 'brewinandchewin:pizza' }, e)
		R.shaped(i.pizza, ['cmf', 'upo', 'xtx'], { c: i.any_cheese, m: i.t_food_cooked_meat, f: i.t_food_cooked_fish, u: i.mushrooms,
			p: PIZZA_PEPPER, o: i.onion, x: i.any_dough, t: i.tomato_sauce }, e, 'pizza_manual_only')
		global.CD.food(e).assemble(i.any_dough, [i.tomato_sauce, i.any_dough, i.any_cheese, i.t_food_cooked_meat, i.t_food_cooked_fish,
			i.mushrooms, PIZZA_PEPPER, i.onion], i.pizza)

		// Ham and Cheese Sandwich: a side is any bread (#forge:bread, baking/baked-goods #28). Smoked ham cuts into two porkchops, so it gives double.
		R.remove({ id: 'brewinandchewin:ham_and_cheese_sandwich' }, e)
		var sandwichSide = i.t_bread
		R.shapeless(Item.of(i.ham_and_cheese_sandwich, 1), [sandwichSide, i.cooked_pork, i.flaxen_cheese_wedge, sandwichSide], e, 'ham_and_cheese_sandwich')
		R.shapeless(Item.of(i.ham_and_cheese_sandwich, 2), [sandwichSide, i.smoked_ham, i.flaxen_cheese_wedge, sandwichSide], e, 'ham_and_cheese_sandwich_smoked_ham')
		var incSandwich = R.incomplete('ham_and_cheese_sandwich')
		R.createSequencedAssembly(Item.of(i.ham_and_cheese_sandwich, 2), i.t_bread, [
			e.recipes.create.deploying(incSandwich, [incSandwich, i.cooked_pork]),
			e.recipes.create.deploying(incSandwich, [incSandwich, i.flaxen_cheese_wedge]),
			e.recipes.create.deploying(incSandwich, [incSandwich, i.t_bread])
		], ['ham_and_cheese_sandwich'], e, 'ham_and_cheese_sandwich_sequenced_assembly', 1)
		R.createSequencedAssembly(Item.of(i.ham_and_cheese_sandwich, 4), i.t_bread, [
			e.recipes.create.deploying(incSandwich, [incSandwich, i.smoked_ham]),
			e.recipes.create.deploying(incSandwich, [incSandwich, i.flaxen_cheese_wedge]),
			e.recipes.create.deploying(incSandwich, [incSandwich, i.flaxen_cheese_wedge]),
			e.recipes.create.deploying(incSandwich, [incSandwich, i.t_bread])
		], ['ham_and_cheese_sandwich'], e, 'ham_and_cheese_sandwich_smoked_ham_sequenced_assembly', 1)

		// Quiche (merges/dishes #8, owner 2026-11-02): baking type, oven only with a pie tin, no plating (the grid recipe and
		// both sequenced assemblies are gone). Pie crust + cheese wedge + milk + any egg + one filling, one recipe each:
		// any cooked meat, any fungus, or a leafy green (#forge:salad_ingredients: cabbage, cabbage leaf, dandelion leaf).
		// Was: bacon or brown mushroom + cabbage + 2 milk + 2 eggs + 2 cheese on a crust in the grid, or the assemblies.
		R.remove({ id: 'brewinandchewin:quiche_from_bacon' }, e)
		R.remove({ id: 'brewinandchewin:quiche_from_mushroom' }, e)
		var QUICHE = { exp: 0.1, time: 200, tool: i.t_bakeware_pie_tin }
		;[['meat', i.t_food_cooked_meat], ['fungus', i.mushrooms], ['greens', i.salad_ingredients]].forEach(q => {
			R.ceBaking([i.pie_crust, i.t_cheese_wedge, i.milk, i.eggs, q[1]], i.quiche, e, 'quiche_' + q[0], QUICHE)
		})

		//Horror Lasagna
		//Make cooked shapeless
		R.shapeless(Item.of(i.horror_lasagna, 1), [i.scarlet_cheese_wedge, i.scarlet_cheese_wedge, i.beetroot_soup, i.tomato_sauce, i.tomato_sauce, i.pasta, i.cooked_horror_lasagna_meat, i.cooked_horror_lasagna_meat, i.bowl], e, 'horror_lasagna_cooked_manual_only')

		//######################### Meals #########################
		// Heatless meal rule: Creative Delight farmersdelight/recipes.js "Meals". Bone broth routes (TODO dishes/ingredients #4).
		R.shapeless(i.creamy_onion_soup, [i.bone_broth, i.t_cheese_wedge, i.onion, i.onion, i.t_soup_veggie_cooked, i.t_bread], e, 'creamy_onion_soup_cooked_manual_only')
		// Pastas take no broth: the pot's bowl (tomato sauce brings its own)
		R.shapeless(i.cheesy_pasta, [i.flaxen_cheese_wedge, i.pasta, i.tomato_sauce, i.cooked_fish, i.cooked_fish], e, 'cheesy_pasta_cooked_manual_only')

		// Vegetable omelet: the pot takes fried eggs; or fold two omelettes (each one cooked fried egg) with the veggies
		R.replaceInput({ id: 'brewinandchewin:cooking/vegetable_omelet' }, i.eggs, i.cooked_eggs, e)
		R.shapeless(i.vegetable_omelet, [i.omelette, i.omelette, i.t_cheese_wedge, i.onion, i.carrot, i.bowl], e, 'vegetable_omelet_from_omelette_manual_only')

		// Assembly meals (TODO dishes/meals #3)
		// Horror lasagna: the efficient route, 2 a run. Pasta goes in first: a sequenced assembly's first step wins over a
		// plain recipe, so a bowl under a spout of tomato sauce would turn into lasagna instead of tomato sauce.
		// The filling step needs Central Kitchen's tomato sauce fluid (chain without it: scratch r1106cbo / r1106spf).
		if (global.has(f.tomato_sauce)) {
			var incHorrorLasagna = R.incomplete('horror_lasagna')
			R.createSequencedAssembly(Item.of(i.horror_lasagna, 2), i.bowl, [
				e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.pasta]),
				e.recipes.create.filling(incHorrorLasagna, [incHorrorLasagna, Fluid.of(f.tomato_sauce, 250)]),
				e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.beetroot]),
				e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.scarlet_cheese_wedge]),
				e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.cooked_horror_lasagna_meat]),
				e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.cooked_horror_lasagna_meat]),
				e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.scarlet_cheese_wedge])
			], ['horror_lasagna'], e, 'horror_lasagna_sequenced_assembly', 1)
		}
	})
})
