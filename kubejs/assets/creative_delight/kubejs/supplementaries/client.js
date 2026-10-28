// requires: supplementaries
// Supplementaries food tooltips, moved from the pack's client_scripts/tooltips.js 2026-10-27, rows unchanged.
ItemEvents.tooltip(e => {
	if (!global.CD) return
	var i = global.item
	e.add(i.sugar_cube, Text.green('Feed to your horse, dissolves on contact w/ water'))
	e.add(i.candy, Text.green('Don\'t eat too many, they\'ll rot your teeth!'))
	e.add(i.pancakes, Text.green('Can be covered in honey or chocolate'))
})
