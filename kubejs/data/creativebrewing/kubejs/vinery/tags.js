// requires: creativebrewing
// requires: vinery
// Vinery item tags. Moved from Creative Engineering's server scripts (tags/wood_tags.js) 2026-11-06, row unchanged.
ServerEvents.tags('item', e => {
	if (!global.CB || !global.CB.on('vinery')) return
	var i = global.item
	var t = global.tags

	// Wooden barrel: the vanilla barrel counts as a Vinery barrel
	t.add(e, ['vinery:barrel', 'vinery:wooden_barrels'], i.barrel)
})
