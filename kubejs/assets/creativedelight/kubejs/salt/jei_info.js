// requires: creativedelight
// requires: jei
// requires: salt
// Salt JEI information pages: block interactions, not recipes (salt.md 7, plan_post_scratch_1115 B6).
// Creative Delight QoL-6, switch [qol] jeiInfoPages.
JEIEvents.information(e => {
	if (!global.CD.qol('jeiInfoPages')) return
	var i = global.item

	// Salt Melt (plan_post_scratch_1115 B6, G7.6, G7.11, G7.14)
	e.addItem(i.salt_melt, [
		Text.of('Right click a saltable block (its tooltip says so) to melt it at once: snow loses 3 layers, ice turns to water, Packed Ice to ice. Blue Ice does not melt.'),
		Text.of('Works in a dispenser facing the block. 64 uses, 1 per melt. Unused ones stack to 16.'),
	])
})
