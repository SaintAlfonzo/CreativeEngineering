// requires: creativedelight
// requires: jei
// requires: salt
// Salt JEI information pages: block interactions, not recipes (salt.md 7, plan_post_scratch_1115 B6).
// Creative Delight QoL-6, switch [qol] jeiInfoPages.
JEIEvents.information(e => {
	if (!global.CD.qol('jeiInfoPages')) return
	var i = global.item

	// Salt Melt (plan_post_scratch_1115 B6)
	e.addItem(i.salt_melt, [
		Text.of('Right click ice or snow to melt it at once. Packed and blue ice step down one kind of ice per use.'),
		Text.of('Works in a dispenser facing the block. 64 uses, 1 per melt.'),
	])
})
