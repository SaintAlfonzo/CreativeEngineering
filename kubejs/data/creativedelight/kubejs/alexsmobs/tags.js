// requires: creativedelight
// requires: alexsmobs
// Alex's Mobs items in the pack's food and cooking item tags (split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// cooked meat
	t.add(e, 'forge:cooked_meat', [
		i.moose_ribs_cooked,
		i.kangaroo_meat_cooked,
		i.catfish_cooked_mob,
		i.lobster_tail_cooked,
	])
})
