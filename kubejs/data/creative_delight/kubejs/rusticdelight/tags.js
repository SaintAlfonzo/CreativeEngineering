// requires: rusticdelight
// Rustic Delight 1.7.0 tag rows (feature_writeup/rustic_delight.md 5): the six new pepper colours join the pack's
// roasted pepper and cooked veggie tags beside the three base colours. The rows after them moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged; shared tags are split by the mod owning each entry.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('rusticdelight')) return
	var i = global.item
	var t = global.tags
	var roasted = ['orange', 'white', 'pink', 'blue', 'purple', 'black'].map(c => i['roasted_bell_pepper_' + c])
	t.add(e, i.t_roasted_bell_pepper, roasted)
	t.add(e, i.t_soup_veggie_cooked, roasted)

	// Cooking for Blockheads oven fuel (ovenRequiresCookingOil): Balm's tag lists only an optional item
	// `forge:cookingoil` (no '#', so not the forge tag) and is otherwise empty. TODO cooking/kitchen #8
	t.add(e, i.t_balm_cooking_oil, i.cooking_oil)

	// sliced potatoes: only More Delight's diced potatoes were in it; Rustic Delight's slices fry the same (TODO dishes/meals #7)
	t.add(e, i.t_sliced_potato, i.potato_slices)

	// meals (TODO dishes/meals #1, #2)
	// cooked veggies for heatless soups and stews (the rule: minecraft/tags.js)
	t.add(e, i.t_soup_veggie_cooked, [
		i.baked_potato_slices,
		i.roasted_bell_pepper_green, i.roasted_bell_pepper_red, i.roasted_bell_pepper_yellow,
	])
	// any roasted bell pepper: Veggies Delight's smoked pepper is not ported, its recipes take these
	t.add(e, i.t_roasted_bell_pepper, [i.roasted_bell_pepper_red, i.roasted_bell_pepper_green, i.roasted_bell_pepper_yellow]) // the six new colours: above
})
