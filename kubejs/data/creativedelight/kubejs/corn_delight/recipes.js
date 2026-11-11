// requires: creativedelight
// requires: corn_delight
// Corn Delight recipe changes: kernels, raw tortilla and cornbread batter on the pack's cornmeal, the corn soup grid
// route. Non-member file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's
// kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Taco #########################
		// One taco recipe (TODO dishes/meals #51): Corn Delight's took an onion, Veggies Delight's a roasted red, green or
		// yellow bell pepper; the one recipe takes an onion or any roasted bell pepper (9 colours). No roasted onion
		// item exists in the pack.
		R.remove({ id: 'corn_delight:taco' }, e)
		R.remove({ id: 'veggiesdelight:compat/corndelight/taco' }, e)
		R.shapeless(i.taco, [i.t_tortilla, i.salad_ingredients, [i.onion, i.t_roasted_bell_pepper],
			[i.cooked_chicken, i.cooked_pork, i.cooked_beef, i.cooked_mutton]], e, 'taco')

		//######################### Cornbread stuffing #########################
		// One recipe (TODO merges/dishes #12): Corn Delight's took a baked potato, Veggies Delight's a baked sweet potato;
		// the one recipe takes either. Time and xp as upstream.
		R.remove({ id: 'corn_delight:cooking/cornbread_stuffing' }, e)
		R.remove({ id: 'veggiesdelight:compat/corndelight/cooking/cornbread_stuffing' }, e)
		var bakedPotato = Platform.isLoaded('veggiesdelight') ? [i.baked_potato, i.baked_sweet_potato] : i.baked_potato
		R.fdCooking([i.cornbread, i.vegetables, bakedPotato, i.sweet_berries], i.cornbread_stuffing, e, 'cornbread_stuffing', 0.5, 200)

		//######################### Egg solution #########################
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			R.createMixing(Item.of(i.cornbread_batter, 3), [Item.of(i.cornmeal, 2), Fluid.of(f.egg_solution, 250)], e,
				'cornbread_batter_egg_solution', 'n/a')
		}

		//######################### Cornmeal #########################
		// The pack's cornmeal rule and grinding: creativedelight/recipes.js. Corn gives no straw: the knife and IE
		// crusher recipes that did keep their kernels only.
		R.remove({ id: 'corn_delight:cutting/corn' }, e)
		R.remove({ id: 'corn_delight:cutting/wild_corn' }, e)
		R.remove({ id: 'corn_delight:integration/immersiveengineering/crusher/wild_corn' }, e)
		// Corn and wild corn cut alike (farming/crops #48): 6 kernels, a Corncob at 75 %, the husk at 33 %
		var cornCut = [Item.of(i.corn_seeds, 6), Item.of(i.corncob, 1).withChance(0.75)]
		if (global.CD.on('hearthandharvest')) cornCut.push(Item.of(i.corn_husk, 1).withChance(0.33))
		R.fdCutting(i.t_crops_corn, i.any_knife, cornCut, e, 'corn_kernels')
		R.fdCutting(i.wild_corn, i.any_knife, cornCut, e, 'wild_corn_kernels')
		// Required in tortillas and cornbread batter (corn -> cornmeal, same counts). Raw tortilla: 3 cornmeal over a water
		// bucket or bottle (the container comes back), or 1:1 by washing or the Bottler (250 mB water, as flour -> dough)
		// in place of Corn Delight's washed corn (dishes/ingredients #14).
		R.remove({ id: 'corn_delight:tortilla_raw' }, e)
		R.remove({ id: 'corn_delight:integration/create/splashing/tortilla_raw' }, e)
		R.shaped(Item.of(i.tortilla_raw, 3), ['CCC', ' W '], { C: i.cornmeal, W: [i.water_bucket, i.water_bottle] }, e, 'tortilla_raw')
		if (Platform.isLoaded('create')) {
			R.createSplashing(i.tortilla_raw, i.cornmeal, e, 'cornmeal_tortilla_raw')
		}
		if (Platform.isLoaded('immersiveengineering')) {
			R.ieBottling(i.tortilla_raw, f.t_water, i.cornmeal, e, 'cornmeal_tortilla_raw', 250)
		}
		R.remove({ id: 'corn_delight:cornbread_batter' }, e)
		R.remove({ id: 'corn_delight:integration/create/mixing/cornbread_batter_from_mixing' }, e)
		R.shapeless(Item.of(i.cornbread_batter, 3), [Item.of(i.cornmeal, 2), i.milk, i.eggs], e, 'cornbread_batter')
		if (Platform.isLoaded('create')) {
			R.createMixing(Item.of(i.cornbread_batter, 3), [Item.of(i.cornmeal, 2), Fluid.of(f.milk, 250), i.eggs], e,
				'cornbread_batter', 'n/a')
		}

		//######################### Meals #########################
		// Heatless meal rule: farmersdelight/recipes.js "Meals"
		var cookedCorn = [i.boiled_corn, i.grilled_corn]
		R.shapeless(i.corn_soup, [i.bone_broth, cookedCorn, cookedCorn, i.salad_ingredients,
			[i.cooked_chicken, i.cooked_pork, i.cooked_beef, i.brown_mushroom], i.milk], e, 'corn_soup_cooked_manual_only')
		// Classic Corn Dog: 1.2.11's grid route beside the pot one already in the jar (corn_delight.md 2.5)
		R.shapeless(i.classic_corn_dog, [i.corn_dog, i.tomato_sauce], e, 'classic_corn_dog_from_corn_dog')

		//######################### 1.2.11 items #########################
		// Corncob, Corn Popsicle, Cob Pipe (CD startup corn_delight/corn_items.js; corn_delight.md 2). The cob's 10% from
		// corn milling and crushing: creativedelight/recipes.js, immersiveengineering/recipes.js.
		// Cob on the cutting board: 2 kernels; the mortar and the machines give 3 (+0.5 cornmeal on the machines): creativeengineering/
		// recipes.js (Create, mortar), immersiveengineering/recipes.js (crusher). corn_delight.md 2.1
		R.fdCutting(i.corncob, i.any_knife, Item.of(i.corn_seeds, 2), e, 'corncob_kernels')
		// Corn Popsicle: upstream's shape, the family's ice cubes (frozen/popsicle, food_families.md)
		R.shaped(i.corn_popsicle, ['  m', 'im ', 'si '], { m: i.t_crops_corn, i: i.t_ice_cubes, s: i.any_stick }, e, 'corn_popsicle')
		R.shapeless(i.cob_pipe, [i.any_stick, i.corncob], e, 'cob_pipe')
	})
})
