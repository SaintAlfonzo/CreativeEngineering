// requires: creativeequipment
// Creative Equipment; moved from kubejs/server_scripts/tags/curiostags.js on 2026-11-04 (creative_split.md 7.6, wave 7),
// rows unchanged. Curios slot membership: the feet slot (registered by IMC in patcherMod compat/CuriosSlots;
// feature_writeup/curios_slots.md) takes the stepstools.
ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item

	// Feet: the stepstools (stepstool/Stepstool reads #creativeequipment:stepstools; feature_writeup/stepstool.md).
	var colours = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
		'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']
	t.add(e, i.t_stepstools, colours.map(c => i[c + '_stepstool']))
	t.add(e, i.t_curios_feet, i.t_stepstools)
})
