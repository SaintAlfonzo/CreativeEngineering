// requires: moredelight
// More Delight recipe changes. Non-member file: recipe ids are creative_delight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Heatless meals #########################
		// Pot meal rule (recipes_cooking.md 2.3): the grid route from cooked forms, one more main ingredient than the
		// pot (sliced potatoes -> 2 baked potatoes; the tomato sauce's bowl is the dish's).
		R.shapeless(i.diced_potatoes_with_egg_and_tomato, [Item.of(i.baked_potato, 2), i.cooked_eggs, i.tomato_sauce,
			i.onion], e, 'diced_potatoes_with_egg_and_tomato_cooked_manual_only')
	})
})
