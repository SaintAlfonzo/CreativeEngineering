// requires: corn_delight
// Corn Delight items in the pack's food and cooking item tags (moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged; split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// meals (TODO dishes/meals #1, #2): cooked veggies for heatless soups and stews (minecraft/tags.js has the rule)
	t.add(e, i.t_soup_veggie_cooked, [i.boiled_corn, i.grilled_corn])

	// Cob Pipe in Curios (corn_delight.md 2.3, curios_slots.md): the pack's Face slot and head. No mouth tag: no mod
	// registers a mouth slot, and the tag showed as a slot in the item's Curios tooltip (gameplay/misc #19). A right click
	// tries Face, the head slot, then a Curios head slot (Creative Delight compat/CornDelightCompat).
	t.add(e, i.t_curios_face, i.cob_pipe)
	t.add(e, i.t_curios_head, i.cob_pipe)
})
