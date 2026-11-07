// requires: creativedelight
// requires: veggiesdelight
// Veggies Delight items in the pack's food and cooking item tags (Creative Delight member #2, moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged; split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('veggiesdelight')) return
	var t = global.tags
	var i = global.item

	// meals (TODO dishes/meals #1, #2): cooked veggies for heatless soups and stews (minecraft/tags.js has the rule)
	t.add(e, i.t_soup_veggie_cooked, [i.baked_sweet_potato, i.roasted_zucchini])
	// Gnocchi is a raw pasta: every pasta recipe takes it (TODO dishes/meals #38, farmersdelight/recipes.js)
	t.add(e, i.pasta, i.potato_noodle)
	// Sliced sweet potato (TODO dishes/ingredients #26): the sliced potato tags (More Delight's dishes, the pack's frying)
	// beside Rustic's potato slices, and its whole sweet potato's vegetable tags, as the slices sit in the potato's
	t.add(e, i.t_sliced_potato, i.sliced_sweet_potato)
	t.add(e, i.t_sliced_potatoes, i.sliced_sweet_potato)
	t.add(e, i.t_vegetables_sweet_potato, i.sliced_sweet_potato)
	t.add(e, i.any_potato, i.sliced_sweet_potato)
	// Mashed Sweet Potatoes take salt like More Delight's Mashed Potatoes (merges/dishes #2, owner 2026-11-01)
	t.add(e, i.t_can_be_salted, i.veggiesdelight_mashed_potatoes)
})

// Zucchini picks its fruit and drops back to stage 4; Quark's replant to stage 0 skips it (vintageport #8, crops.md 2.2)
ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('veggiesdelight')) return
	var b = global.blocks
	global.tags.add(e, b.t_own_pick_crops, b.zucchini_crop)
})
