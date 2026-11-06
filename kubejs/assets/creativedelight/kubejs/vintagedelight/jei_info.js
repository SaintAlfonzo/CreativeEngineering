// requires: jei
// requires: vintagedelight
// Vintage Delight JEI information pages: mechanics that are block interactions, not recipes (jei_coverage.md 4.11).
// Creative Delight QoL-6, switch [qol] jeiInfoPages.
JEIEvents.information(e => {
	if (!global.CD.qol('jeiInfoPages')) return
	var i = global.item

	// Mixed Seeds (creativedelight/members #32, vintage_delight.md 2.3a)
	e.addItem(i.mixed_seeds, [
		Text.of('Planted, a Mixed Seed becomes wheat, beetroot, cabbage, melon or pumpkin, picked at random each time.'),
		Text.of('Found in village house and pillager outpost chests, and now and then when breaking grass without shears.'),
	])
})
