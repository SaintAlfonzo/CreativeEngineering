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

		//######################### Egg solution #########################
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			R.createMixing(Item.of(i.cornbread_batter, 3), [Item.of(i.cornmeal, 2), Fluid.of(f.egg_solution, 250)], e,
				'cornbread_batter_egg_solution', 'n/a')
		}

		//######################### Cornmeal #########################
		// The pack's cornmeal rule and grinding: creativeengineering/recipes.js. Corn gives no straw: the knife and IE
		// crusher recipes that did keep their kernels only.
		R.remove({ id: 'corn_delight:cutting/corn' }, e)
		R.remove({ id: 'corn_delight:cutting/wild_corn' }, e)
		R.remove({ id: 'corn_delight:integration/immersiveengineering/crusher/wild_corn' }, e)
		R.fdCutting(i.t_crops_corn, i.any_knife, Item.of(i.corn_seeds, 4), e, 'corn_kernels')   // 4 kernels a maize
		R.fdCutting(i.wild_corn, i.any_knife, i.corn_seeds, e, 'wild_corn_kernels')
		if (Platform.isLoaded('immersiveengineering')) {
			R.ieCrushing(i.corn_seeds, i.wild_corn, e, 'wild_corn_kernels', [], 3200)
		}
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
		// corn milling and crushing: creativeengineering/recipes.js, immersiveengineering/recipes.js.
		// Cob on the cutting board: 6 kernels, more than the 4 from a whole corn (placeholder count, 2.1)
		R.fdCutting(i.corncob, i.any_knife, Item.of(i.corn_seeds, 6), e, 'corncob_kernels')
		// Corn Popsicle: upstream's shape, the family's ice cubes (frozen/popsicle, food_families.md)
		R.shaped(i.corn_popsicle, ['  m', 'im ', 'si '], { m: i.t_crops_corn, i: i.t_ice_cubes, s: i.any_stick }, e, 'corn_popsicle')
		R.shapeless(i.cob_pipe, [i.any_stick, i.corncob], e, 'cob_pipe')
	})
})
