// requires: veggiesdelight
// Veggies Delight grid-meal moves and heatless routes: Creative Delight QoL-5 (creative_delight.md 8; moved from the pack's
// server_scripts/cooking/cooking.js 2026-10-25, rows unchanged). Recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). Switch [qol] manualOnlyRoutes: off skips this file, so the mod's own recipes stay. Rules and
// helper recipes (bone broth, cooked forms) stay in the pack. Writeup: feature_writeup/veggies_delight.md 4.7.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('veggiesdelight') || !global.CD.qol('manualOnlyRoutes')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		// Veggies Delight (feature_writeup/veggies_delight.md 4.7). Its own grid meals on cooked food are re-id'd manual only;
		// its pot meals get heatless routes by the pack's rule (farmersdelight/recipes.js "Meals"). Cooked forms: cauliflower -> roasted florets (2 a head),
		// garlic -> roasted cloves, sweet potato -> baked, zucchini -> roasted, bell pepper -> Rustic's roasted peppers.
		// Broccoli and turnip have none and stay raw. Fish and chips keeps the pack's frying; potato noodles start from an
		// already-cooked noodle; the lasagna pan is a feast.
		var VD_COOKED_MEAT = [i.cooked_beef, i.cooked_chicken, i.cooked_mutton]
		;[
			['broccoli_salad', 'veggiesdelight:broccoli_salad'],
			['cesar_salad', 'veggiesdelight:cesar_salad'],
			['steak_and_broccoli', 'veggiesdelight:steak_and_broccoli'],
			['garlic_rice_with_cauliflower', 'veggiesdelight:garlic_rice_with_cauliflower'],
			['garlic_baked_cod', 'veggiesdelight:garlic_baked_cod'],
			['dandelion_and_eggs', 'veggiesdelight:dandelion_and_eggs'],
			['stuffed_zucchini_boat', 'veggiesdelight:stuffed_zucchini_boat'],
			['zucchini_sandwich', 'veggiesdelight:zucchini_sandwich'],
			['coleslaw', 'veggiesdelight:coleslaw'],
			['turnip_salad', 'veggiesdelight:turnip_salad'],
			// its Vintage Delight compat versions (live with the Vintage port): the cheese goes in the recipes below
			['cesar_salad', 'veggiesdelight:compat/vintagedelight/cesar_salad'],
			['stuffed_zucchini_boat', 'veggiesdelight:compat/vintagedelight/stuffed_zucchini_boat'],
		].forEach(m => R.remove({ id: m[1] }, e))
		R.shapeless(i.broccoli_salad, [i.broccoli, i.tomato, i.onion, i.bowl], e, 'broccoli_salad_manual_only')
		R.shapeless(i.cesar_salad, [i.cabbage, i.t_bread, [i.garlic_clove, i.any_cheese], i.tomato, i.bowl], e, 'cesar_salad_manual_only')
		R.shapeless(i.coleslaw, [i.zucchini, i.potato, i.carrot, i.cabbage, i.bowl], e, 'coleslaw_manual_only')
		R.shapeless(i.turnip_salad, [i.salad_ingredients, i.turnip, i.bowl], e, 'turnip_salad_manual_only')
		R.shapeless(i.steak_and_broccoli, [i.cooked_beef, i.broccoli, i.cooked_rice], e, 'steak_and_broccoli_cooked_manual_only')
		R.shapeless(i.garlic_rice_with_cauliflower, [i.roasted_garlic_clove, Item.of(i.roasted_cauliflower_floret, 2), i.cooked_rice], e,
			'garlic_rice_with_cauliflower_cooked_manual_only')
		R.shapeless(i.garlic_baked_cod, [i.cooked_cod, Item.of(i.roasted_garlic_clove, 2), i.onion, i.bowl], e, 'garlic_baked_cod_cooked_manual_only')
		R.shapeless(i.dandelion_and_eggs, [Item.of(i.dandelion_leaf, 2), i.cooked_eggs, i.cooked_eggs, i.cooked_eggs, i.bowl], e, 'dandelion_and_eggs_cooked_manual_only')
		R.shapeless(i.stuffed_zucchini_boat, [i.roasted_zucchini, VD_COOKED_MEAT, [i.milk, i.any_cheese]], e, 'stuffed_zucchini_boat_cooked_manual_only')
		R.shapeless(i.zucchini_sandwich, [i.t_bread, i.zucchini, i.salad_ingredients, i.tomato], e, 'zucchini_sandwich_cooked_manual_only')
		// Turnip mutton skewer and vegetarian burger: skewer and burger family rules (recipes.js, creativedelight/members #30)
		// Heatless routes for its pot meals
		R.shapeless(i.broccoli_soup, [i.bone_broth, Item.of(i.broccoli, 3), i.milk], e, 'broccoli_soup_cooked_manual_only')
		R.shapeless(i.cauliflower_soup, [i.bone_broth, Item.of(i.roasted_cauliflower_floret, 5), i.milk], e, 'cauliflower_soup_cooked_manual_only')
		R.shapeless(i.garlic_chicken_stew, [i.bone_broth, Item.of(i.roasted_garlic_clove, 2), i.cooked_chicken, i.cooked_chicken, i.milk], e,
			'garlic_chicken_stew_cooked_manual_only')
		R.shapeless(i.turnip_beef_stew, [i.bone_broth, Item.of(i.turnip, 2), i.carrot, i.cooked_beef], e, 'turnip_beef_stew_cooked_manual_only')
		R.shapeless(i.cacciatore, [i.t_roasted_bell_pepper, i.cooked_chicken, i.cooked_chicken, i.tomato_sauce], e, 'cacciatore_cooked_manual_only')
		R.shapeless(i.cauliflower_kuku, [Item.of(i.roasted_cauliflower_floret, 3), i.onion, i.roasted_garlic_clove, i.milk, i.bowl], e,
			'cauliflower_kuku_cooked_manual_only')
		// Mashed family (TODO merges/dishes #2): 3 baked sweet potatoes + milk + bowl (pot route: recipes.js)
		R.shapeless(i.veggiesdelight_mashed_potatoes, [Item.of(i.baked_sweet_potato, 3), i.milk, i.bowl], e,
			'mashed_sweet_potatoes_cooked_manual_only')
		R.shapeless(i.pasta_with_broccoli, [i.pasta, Item.of(i.broccoli, 2), i.milk, i.bowl], e, 'pasta_with_broccoli_cooked_manual_only')
		R.shapeless(i.rice_and_vegetables, [i.cooked_rice, i.carrot, i.onion, i.roasted_zucchini, i.t_soup_veggie_cooked, i.t_soup_veggie_cooked], e,
			'rice_and_vegetables_cooked_manual_only')
		R.shapeless(i.roasted_vegetables, [i.baked_potato, i.carrot, i.onion, i.tomato_sauce, i.t_soup_veggie_cooked, i.t_soup_veggie_cooked,
			i.t_roasted_bell_pepper], e, 'roasted_vegetables_cooked_manual_only')
		R.shapeless(i.shakshouka, [i.t_roasted_bell_pepper, i.onion, i.tomato_sauce, i.cooked_eggs, i.cooked_eggs], e, 'shakshouka_cooked_manual_only')
		R.shapeless(i.stuffed_zucchinis, [Item.of(i.roasted_zucchini, 3), i.tomato_sauce, VD_COOKED_MEAT], e, 'stuffed_zucchinis_cooked_manual_only')
		R.shapeless(i.turnip_cake, [Item.of(i.turnip, 2), i.wheat, i.cooked_rice, i.cooked_meat], e, 'turnip_cake_cooked_manual_only')
		R.shapeless(i.garlic_stuffed_mushrooms, [Item.of(i.roasted_garlic_clove, 2), i.cabbage, Item.of(i.brown_mushroom, 2)], e,
			'garlic_stuffed_mushrooms_cooked_manual_only')


		// Veggies Delight wraps: a tortilla, as the mutton wrap (the port ships only its Corn Delight tortilla recipes; these
		// replace them as manual-only grid recipes plus an assembly). feature_writeup/veggies_delight.md 4.7
		R.remove({ id: 'veggiesdelight:compat/corndelight/chicken_fajitas_wrap' }, e)
		R.remove({ id: 'veggiesdelight:compat/corndelight/vegetable_wrap' }, e)
		R.remove({ id: 'veggiesdelight:compat/vintagedelight/chicken_fajitas_wrap' }, e)   // on dough
		var VEG_WRAP_GREENS = [i.broccoli, i.salad_ingredients]
		var VEG_WRAP_FILLING = [i.t_roasted_bell_pepper, i.roasted_zucchini, i.mushrooms]
		R.shapeless(i.chicken_fajitas_wrap, [i.t_tortilla, i.onion, i.cooked_chicken, i.t_roasted_bell_pepper], e, 'chicken_fajitas_wrap_manual_only')
		R.shapeless(i.vegetables_wrap, [i.t_tortilla, i.onion, VEG_WRAP_GREENS, VEG_WRAP_FILLING, i.cooked_rice], e, 'vegetables_wrap_manual_only')
		var incFajitasWrap = R.incomplete('chicken_fajitas_wrap')
		R.createSequencedAssembly(i.chicken_fajitas_wrap, i.t_tortilla, [
			e.recipes.create.deploying(incFajitasWrap, [incFajitasWrap, i.cooked_chicken]),
			e.recipes.create.deploying(incFajitasWrap, [incFajitasWrap, i.t_roasted_bell_pepper]),
			e.recipes.create.deploying(incFajitasWrap, [incFajitasWrap, i.onion])
		], ['chicken_fajitas_wrap'], e, 'chicken_fajitas_wrap_sequenced_assembly', 1)
		var incVegWrap = R.incomplete('vegetables_wrap')
		R.createSequencedAssembly(i.vegetables_wrap, i.t_tortilla, [
			e.recipes.create.deploying(incVegWrap, [incVegWrap, VEG_WRAP_GREENS]),
			e.recipes.create.deploying(incVegWrap, [incVegWrap, VEG_WRAP_FILLING]),
			e.recipes.create.deploying(incVegWrap, [incVegWrap, i.onion]),
			e.recipes.create.deploying(incVegWrap, [incVegWrap, i.cooked_rice])
		], ['vegetables_wrap'], e, 'vegetables_wrap_sequenced_assembly', 1)
	})
})
