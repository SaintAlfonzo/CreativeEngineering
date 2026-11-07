// requires: creativedelight
// requires: alexsdelight
// Alex's Delight items in the pack's food and cooking item tags (moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged; split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// cooked meat
	t.add(e, 'forge:cooked_meat', [
		i.moose_rib_cooked_loose,
		i.kangaroo_shank_cooked,
		i.catfish_slice_cooked,
		i.bunfungus_cooked, i.bunfungus_drumstick_cooked,
		i.bison_cooked, i.bison_patty,
		i.centipede_leg_cooked,
	])
})
