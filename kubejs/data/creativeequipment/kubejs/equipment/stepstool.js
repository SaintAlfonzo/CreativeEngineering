// Creative Equipment; moved from kubejs/server_scripts/equipment/stepstool.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
// Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes).
// Stepstools (items: startup_scripts/items/stepstools.js; feature_writeup/stepstool.md): a Create seat on top of an oak fence,
// the seat's colour giving the stool's.

ServerEvents.recipes(e => {
global.CEQ.recipes(() => {
	const R = global.NCH
	var i = global.item

	;['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
		'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black'].forEach(c => {
		R.shaped(Item.of(i[c + '_stepstool']), ['S', 'F'], {S: i[c + '_seat'], F: i.fence_oak}, e)
	})
})
})
