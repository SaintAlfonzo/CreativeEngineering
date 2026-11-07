// requires: creativedelight
// requires: fungidelight
// Fungi Delight grid-meal moves and heatless routes: Creative Delight QoL-5 (creative_delight.md 8; moved from the pack's
// server_scripts/cooking/cooking.js 2026-10-25, rows unchanged). Recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). Switch [qol] manualOnlyRoutes: off skips this file, so the mod's own recipes stay. Rules and
// helper recipes (bone broth, cooked forms) stay in the pack. Writeup: feature_writeup/fungi_delight.md 4 step 9.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('fungidelight') || !global.CD.qol('manualOnlyRoutes')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		// Fungi Delight (feature_writeup/fungi_delight.md 4 step 9), by the pack's rules (farmersdelight/recipes.js "Meals"). Grid meals re-id'd manual only; the
		// truffle ice cream grid recipe is a placeholder until the ice cream family is decided (TODO sweets #4). Heatless
		// routes for all 7 pot meals, one more main ingredient than the pot; the mushrooms stay raw.
		;['fungidelight:inky_cap_salad', 'fungidelight:inky_cap_scrambled_eggs', 'fungidelight:truffle_ice_cream']
			.forEach(id => R.remove({ id: id }, e))
		R.shapeless(i.inky_caps_salad, [i.salad_ingredients, i.cleaned_cap, i.bowl], e, 'inky_caps_salad_manual_only')
		R.shapeless(i.inky_cap_scrambled_eggs, [i.cooked_eggs, i.cooked_eggs, i.cooked_cleaned_cap, i.bowl], e,
			'inky_cap_scrambled_eggs_manual_only')
		R.shapeless(i.truffle_ice_cream, [i.truffle_slice, i.snowball, i.any_sweetener, i.milk, i.bowl], e, 'truffle_ice_cream_manual_only')   // placeholder (sweets #4)
		R.shapeless(i.creamy_morel_soup, [i.bone_broth, Item.of(i.morel_mushroom, 3), i.milk, i.onion], e, 'creamy_morel_soup_cooked_manual_only')
		R.shapeless(i.mutton_chops_with_truffle, [i.cooked_mutton, i.cooked_mutton, i.truffle, i.baked_potato, i.onion, i.bowl], e,
			'mutton_chops_with_truffle_cooked_manual_only')
		R.shapeless(i.pork_marsala_with_mushrooms, [i.cooked_pork, i.cooked_pork, i.onion, Item.of(i.red_mushroom, 2), i.tomato_sauce], e,
			'pork_marsala_with_mushrooms_cooked_manual_only')
		R.shapeless(i.rabbit_with_morels, [Item.of(i.rabbit_cooked, 2), Item.of(i.morel_mushroom, 2), i.carrot, i.onion, i.bowl], e,
			'rabbit_with_morels_cooked_manual_only')
		R.shapeless(i.steak_with_mushrooms, [i.cooked_beef, i.cooked_beef, i.brown_mushroom, i.onion, i.milk, i.bowl], e,
			'steak_with_mushrooms_cooked_manual_only')
		R.shapeless(i.stuffed_morels, [Item.of(i.morel_mushroom, 3), i.cooked_rice, i.tomato_sauce, i.cooked_chicken], e,
			'stuffed_morels_cooked_manual_only')
		R.shapeless(i.truffle_pasta, [i.pasta, Item.of(i.truffle, 2), i.milk, i.onion, i.bowl], e, 'truffle_pasta_cooked_manual_only')
	})
})
