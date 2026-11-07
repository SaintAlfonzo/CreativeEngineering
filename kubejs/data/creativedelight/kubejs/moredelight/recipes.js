// requires: creativedelight
// requires: moredelight
// More Delight recipe changes. Non-member file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Toast #########################
		// Cheese Toast (more_delight.md 2.5): toast + a Flaxen Cheese Wedge, as the other toppings.
		R.shapeless(i.cheese_toast, [i.toast, i.flaxen_cheese_wedge], e, 'cheese_toast')

		//######################### Mashed Potatoes #########################
		// Mashed family (TODO merges/dishes #2, M2): More Delight's takes only potatoes or potato slices (Veggies Delight's
		// Mashed Sweet Potatoes the sweet ones, veggiesdelight/recipes.js). One set: Cooking Pot 2 of the type + milk, in a
		// bowl (was 2 of #forge:sliced_potatoes + milk, no bowl); by hand 3 baked potatoes or baked potato slices + milk + bowl.
		R.remove({ id: 'moredelight:cooking/mashed_potatoes' }, e)
		var potatoMash = [i.potato, i.potato_slices]
		R.fdCooking([potatoMash, potatoMash, i.milk], i.mashed_potatoes, e, 'mashed_potatoes', 1.0, 200, i.bowl)
		var bakedMash = [i.baked_potato, i.baked_potato_slices]
		R.shapeless(i.mashed_potatoes, [bakedMash, bakedMash, bakedMash, i.milk, i.bowl], e, 'mashed_potatoes_cooked_manual_only')

		//######################### Heatless meals #########################
		// Pot meal rule (recipes_cooking.md 2.3): the grid route from cooked forms, one more main ingredient than the
		// pot (sliced potatoes -> 2 baked potatoes; the tomato sauce's bowl is the dish's).
		R.shapeless(i.diced_potatoes_with_egg_and_tomato, [Item.of(i.baked_potato, 2), i.cooked_eggs, i.tomato_sauce,
			i.onion], e, 'diced_potatoes_with_egg_and_tomato_cooked_manual_only')
		// Moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged: bone broth routes
		// (TODO dishes/ingredients #4) and pastas (no broth: the pot's bowl), same rule.
		R.shapeless(i.carrot_soup, [i.bone_broth, i.milk, i.carrot, i.carrot, i.carrot], e, 'carrot_soup_cooked_manual_only')
		R.shapeless(i.creamy_pasta_with_chicken_cuts, [i.milk, i.pasta, i.chicken_cuts_cooked, i.chicken_cuts_cooked, i.onion, i.bowl], e, 'creamy_pasta_with_chicken_cuts_cooked_manual_only')
		R.shapeless(i.creamy_pasta_with_ham, [i.milk, i.pasta, i.smoked_ham, i.onion, i.onion, i.bowl], e, 'creamy_pasta_with_ham_cooked_manual_only')
	})
})
