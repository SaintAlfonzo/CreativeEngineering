// requires: jei
// requires: roll_delight
// Roll Delight JEI information pages: mechanics that are block interactions, not recipes (jei_coverage.md 4.11).
// Creative Delight QoL-6 (creative_delight.md 8), switch [qol] jeiInfoPages. Roll Delight port, roll_delight.md 2.4.
JEIEvents.information(e => {
	if (!global.CD.qol('jeiInfoPages')) return
	var i = global.item

	// The frogspawn drop is the member's BreakEvent hook: the only survival source of the Tobiko Roll's frogspawn
	e.addItem(i.frogspawn, [
		Text.of('Break frogspawn with shears in the main hand to pick it up. Broken any other way, it drops nothing.'),
	])
})
