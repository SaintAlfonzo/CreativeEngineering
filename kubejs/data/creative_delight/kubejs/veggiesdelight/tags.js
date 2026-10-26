// requires: veggiesdelight
// Veggies Delight items in the pack's food and cooking item tags (Creative Delight member #2, moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged; split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('veggiesdelight')) return
	var t = global.tags
	var i = global.item

	// meals (TODO dishes/meals #1, #2): cooked veggies for heatless soups and stews (minecraft/tags.js has the rule)
	t.add(e, i.t_soup_veggie_cooked, [i.baked_sweet_potato, i.roasted_zucchini])
})
