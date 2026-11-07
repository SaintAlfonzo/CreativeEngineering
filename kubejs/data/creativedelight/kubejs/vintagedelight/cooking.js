// requires: creativedelight
// requires: vintagedelight
// Vintage Delight grid-meal moves and heatless routes: Creative Delight QoL-5 (creative_delight.md 8; moved from the pack's
// server_scripts/cooking/cooking.js 2026-10-25, rows unchanged). Recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). Switch [qol] manualOnlyRoutes: off skips this file, so the mod's own recipes stay. Rules and
// helper recipes (bone broth, cooked forms) stay in the pack. Writeup: feature_writeup/vintage_delight.md 4 step 9.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('vintagedelight') || !global.CD.qol('manualOnlyRoutes')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		// Vintage Delight (feature_writeup/vintage_delight.md 4 step 9), by the pack's rules (farmersdelight/recipes.js "Meals"). Grid meals re-id'd manual only;
		// the stuffed burrito is a wrap on a tortilla (its ghost pepper, or a roasted bell pepper as Veggies Delight's compat
		// version), the assembly starting on the pepper. Heatless routes for its pot meals: cheese pasta, pad thai, pickle
		// soup. Not given one: oatmeal (oats have no cooked form), ghostly chili (Nether, as blazing chili), the jams, curds
		// and nut milk (cooked preserves and a drink). Burgers are in "Burgers"; the oatmeal cookie bakes (baking.js).
		;['vintagedelight:pb_j', 'vintagedelight:cucumber_salad', 'vintagedelight:stuffed_burrito',
			'veggiesdelight:compat/vintagedelight/stuffed_burrito'].forEach(id => R.remove({ id: id }, e))
		// Nut Mash and Jam Sandwich (merges/dishes #7): any bread, any jam, a nut mash jar (the jars come back empty)
		R.shapeless(i.pb_j, [i.t_bread, i.nut_mash_jar, i.t_forge_jams], e, 'pb_j_manual_only')   // any jam (dishes/jams #3)
		R.shapeless(i.cucumber_salad, [i.salad_ingredients, i.pickled_beetroot, i.pickled_onion, i.cucumber, i.bowl], e, 'cucumber_salad_manual_only')
		var VTD_BURRITO_PEPPER = [i.ghost_pepper, i.t_roasted_bell_pepper]
		R.shapeless(i.stuffed_burrito, [i.t_tortilla, i.beef_patty, i.salad_ingredients, i.tomato, i.any_cheese, VTD_BURRITO_PEPPER], e,
			'stuffed_burrito_manual_only')
		var incBurrito = R.incomplete('stuffed_burrito')
		R.createSequencedAssembly(i.stuffed_burrito, i.t_tortilla, [
			e.recipes.create.deploying(incBurrito, [incBurrito, VTD_BURRITO_PEPPER]),
			e.recipes.create.deploying(incBurrito, [incBurrito, i.beef_patty]),
			e.recipes.create.deploying(incBurrito, [incBurrito, i.any_cheese]),
			e.recipes.create.deploying(incBurrito, [incBurrito, i.salad_ingredients]),
			e.recipes.create.deploying(incBurrito, [incBurrito, i.tomato])
		], ['stuffed_burrito'], e, 'stuffed_burrito_sequenced_assembly', 1)
		R.shapeless(i.cheese_pasta, [i.pasta, i.any_cheese, i.any_cheese, i.any_cheese, i.bowl], e, 'cheese_pasta_cooked_manual_only')
		R.shapeless(i.pad_thai, [i.pasta, i.ghost_pepper, Item.of(i.chicken_cuts_cooked, 2), i.nut_mash_jar, i.roasted_peanut, i.bowl], e,
			'pad_thai_cooked_manual_only')
		// Heatless: the same pickled veggie slot (twice, as the 2 pickles) and any 2 vegetables as the pot recipe (vintagedelight/recipes.js)
		R.shapeless(i.pickle_soup, [i.bone_broth, i.t_pickled_veg, i.t_pickled_veg, i.vegetables, i.vegetables, i.beetroot, i.salad_ingredients], e,
			'pickle_soup_cooked_manual_only')
	})
})
