// requires: creativedelight
// requires: jei
// requires: largemeals
// Large Meals JEI information pages: the sheep knife drop is a loot modifier, not a recipe (large_meals.md 4 step 18).
// Creative Delight QoL-6 (creative_delight.md 8), switch [qol] jeiInfoPages.
JEIEvents.information(e => {
	if (!global.CD || !global.CD.on('largemeals')) return
	if (!global.CD.qol('jeiInfoPages')) return
	var i = global.item
	e.addItem(i.raw_mutton_rack, [
		Text.of('Obtained by killing a Sheep with a Knife in the main hand: 50% chance, +10% per level of Looting. The rack cuts into 4 Mutton Chops.'),
	])
	e.addItem(i.cooked_mutton_rack, [
		Text.of('Obtained by killing a burning Sheep with a Knife in the main hand (same chance), or by cooking a Raw Rack of Mutton. The rack cuts into 4 Cooked Mutton Chops.'),
	])
})
