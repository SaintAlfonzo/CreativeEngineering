// Curios slot membership for the pack's own slots (data: kubejs/data/creativeengineering/curios/; feature_writeup/curios_slots.md).
// Curios takes an item into a slot when it is in the item tag curios:<slot>.

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item

	// Hands (Falconry Glove) and the Simple Hats face rows: Creative Compat (creativecompat/kubejs/tags/curiostags.js).

	// Face: Botania's cosmetic masks, the Cob Pipe (corn_delight.md 2.3).
	t.add(e, i.t_curios_face, [
		i.cosmetic_ancient_mask, i.cosmetic_eerie_mask, i.cosmetic_tiny_potato_mask,
		i.cob_pipe,
	])

	// Feet: the stepstools (patcherMod stepstool/ reads #creativeengineering:stepstools; feature_writeup/stepstool.md).
	var colours = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
		'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']
	t.add(e, i.t_stepstools, colours.map(c => i[c + '_stepstool']))
	t.add(e, i.t_curios_feet, i.t_stepstools)
})
