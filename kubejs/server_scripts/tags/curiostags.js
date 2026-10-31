// Curios slot membership for the pack's own slots (data: kubejs/data/creativeengineering/curios/; feature_writeup/curios_slots.md).
// Curios takes an item into a slot when it is in the item tag curios:<slot>.

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item

	// Hands: the Falconry Glove (no mod registered a hands slot, so it had nowhere to go).
	t.add(e, i.t_curios_hands, i.falconry_glove)

	// Face: Simple Hats' masks and chalk stick, Botania's cosmetic masks, the Cob Pipe (corn_delight.md 2.3).
	// Simple Hats adds every hat to curios:head through a mixin, so these stay valid in a head slot too.
	t.add(e, i.t_curios_face, [
		i.simplehats_chalk_stick,
		i.simplehats_angrymask, i.simplehats_cucumbereyemask, i.simplehats_ghostmask,
		i.simplehats_hockeymask, i.simplehats_horsemask, i.simplehats_sleepeyemask,
		i.cosmetic_ancient_mask, i.cosmetic_eerie_mask, i.cosmetic_tiny_potato_mask,
		i.cob_pipe,
	])

	// Feet: the stepstools (patcherMod stepstool/ reads #creativeengineering:stepstools; feature_writeup/stepstool.md).
	var colours = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
		'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']
	t.add(e, i.t_stepstools, colours.map(c => i[c + '_stepstool']))
	t.add(e, i.t_curios_feet, i.t_stepstools)
})
