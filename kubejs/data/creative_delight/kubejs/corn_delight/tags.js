// requires: corn_delight
// Corn Delight items in the pack's food and cooking item tags (moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged; split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// meals (TODO dishes/meals #1, #2): cooked veggies for heatless soups and stews (minecraft/tags.js has the rule)
	t.add(e, i.t_soup_veggie_cooked, [i.boiled_corn, i.grilled_corn])

	// Cob Pipe in Curios (corn_delight.md 2.3): head, and mouth in case a mod adds that slot. The pack's Face slot takes it
	// through server_scripts/tags/curiostags.js. A right click tries mouth, face, the head slot, then a Curios head slot
	// (patcherMod compat/CornDelightCompat).
	t.add(e, i.t_curios_head, i.cob_pipe)
	t.add(e, i.t_curios_mouth, i.cob_pipe)
})
