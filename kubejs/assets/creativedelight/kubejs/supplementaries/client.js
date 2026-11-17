// requires: creativedelight
// requires: supplementaries
// Supplementaries food tooltips, moved from the pack's client_scripts/tooltips.js 2026-10-27, rows unchanged.
// A tooltip line in a col role (global.tooltips, Creative Common's client _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	if (!global.CD) return
	var i = global.item
	e.add(i.sugar_cube, tipLine('Feed to your horse, dissolves on contact w/ water', 'hint'))
	e.add(i.candy, tipLine('Don\'t eat too many, they\'ll rot your teeth!', 'hint'))
	e.add(i.pancakes, tipLine('Can be covered in honey or chocolate', 'hint'))
	// The pack's one plain pancake (Rustic Delight's single is removed, creativedelight/members #15); its name is plain "Pancake"
	e.add(i.pancakes, tipLine('Stackable: place up to 8 on top of each other', 'text'))
})
