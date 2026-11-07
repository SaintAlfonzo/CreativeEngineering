// requires: creativedelight
// requires: ends_delight
// End's Delight recipe changes (Creative Delight member, ends_delight.md 3.1, 3.3; TODO largecontent/mod-additions #4,
// #15, #29). Recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). Rows naming Ender's Delight items
// are in the endersdelight folder beside this one; the shared tags are in tags.js here and there.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('ends_delight')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Gear #########################
		// Dragon Tooth Knife: smithed from the Dragon Egg Shell Knife and a Dragon Tooth (End's 2.6.1; 1.19.2 smithing has
		// no template), replacing 2.1's shapeless craft. Future: the End gear recipes and their CEConfig values (gear #12).
		R.remove({ id: 'ends_delight:dragon_tooth_knife' }, e)
		R.smithing(i.dragon_tooth_knife, i.dragon_egg_shell_knife, i.dragon_tooth, e, 'dragon_tooth_knife')

		//######################### Ingredients #########################
		// Fungus options (mod-additions #15): the crimson fungus inputs take any cookable fungus (crimson, warped, Fungi
		// Delight's inky cap, morel and truffle). Ender Noodle already takes either fungus or either mushroom: unchanged.
		;['ends_delight:food/assorted_salad', 'ends_delight:food/roasted_dragon_steak', 'ends_delight:food/dragon_leg_with_sauce_block']
			.forEach(id => R.replaceInput({ id: id }, i.crimson_fungus, i.t_cookable_fungi, e))

		// Chorus crate: one unpack route for both crates (#forge:storage_blocks/chorus_fruit, endersdelight/recipes.js)
		R.remove({ id: 'ends_delight:chorus_fruit_crate_to_chorus_fruit' }, e)

		//######################### Meals #########################
		// Heatless meal rule (farmersdelight/recipes.js "Meals"): every End's pot meal also crafts shapeless from the
		// cooked form of each raw ingredient, with one more main ingredient than the pot. Raw shulker / dragon meat ->
		// roasted; rice -> cooked rice; potato -> baked; tomato -> tomato sauce (brings the bowl). Gristle, the liquid
		// dragon egg, dried meats and the succulent have no cooked form and stay as they are. Drinks and the chorus sauce
		// are not meals (teas: food/drinks #7).
		R.shapeless(i.shulker_soup, [i.bone_broth, i.roasted_shulker_meat, i.roasted_shulker_meat, i.dried_chorus_flower, i.chorus_succulent],
			e, 'shulker_soup_cooked_manual_only')
		R.shapeless(i.enderman_gristle_stew, [i.bone_broth, i.enderman_gristle, i.enderman_gristle, i.enderman_gristle, i.dried_chorus_flower,
			i.chorus_succulent], e, 'enderman_gristle_stew_cooked_manual_only')
		R.shapeless(i.ender_congee, [i.cooked_rice, i.cooked_rice, i.dried_endermite_meat, i.dried_endermite_meat, i.ender_pearl_grain, i.bowl],
			e, 'ender_congee_cooked_manual_only')
		R.shapeless(i.ender_noodle, [i.chorus_sauce, i.roasted_shulker_meat, i.dried_endermite_meat, i.dried_endermite_meat, i.t_cookable_fungi,
			i.pasta], e, 'ender_noodle_cooked_manual_only')
		R.shapeless(i.ender_bamboo_rice, [i.bamboo, i.cooked_rice, i.roasted_dragon_meat, i.roasted_dragon_meat, i.chorus_succulent, i.carrot,
			i.onion], e, 'ender_bamboo_rice_cooked_manual_only')
		R.shapeless(i.stir_fried_shulker_meat, [i.tomato_sauce, i.roasted_shulker_meat, i.roasted_shulker_meat, i.chorus_fruit, i.onion],
			e, 'stir_fried_shulker_meat_cooked_manual_only')
		R.shapeless(i.dragon_breath_and_chorus_soup, [i.suspicious_stew, i.dragon_breath, i.dried_chorus_flower, i.chorus_fruit, i.chorus_fruit,
			i.chorus_fruit], e, 'dragon_breath_and_chorus_soup_cooked_manual_only')
		// feasts: the half dragon egg shell is the container, as in the pot
		R.shapeless(i.dragon_meat_stew_block, [i.half_dragon_egg_shell, i.roasted_dragon_meat, i.roasted_dragon_meat, i.chorus_fruit,
			i.chorus_succulent, i.carrot, i.baked_potato, i.tomato_sauce], e, 'dragon_meat_stew_block_cooked_manual_only')
		R.shapeless(i.steamed_dragon_egg_block, [i.half_dragon_egg_shell, i.liquid_dragon_egg, i.roasted_shulker_meat, i.roasted_shulker_meat,
			i.chorus_fruit, i.brown_mushroom, i.chorus_succulent, i.onion], e, 'steamed_dragon_egg_block_cooked_manual_only')
	})
})
