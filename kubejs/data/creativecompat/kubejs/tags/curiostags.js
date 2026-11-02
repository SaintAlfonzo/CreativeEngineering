// Creative Compat: Curios slot membership for the pack's own slots (data: kubejs/data/creativeengineering/curios/;
// feature_writeup/curios_slots.md); moved from kubejs/server_scripts/tags/curiostags.js on 2026-11-01. Curios takes an
// item into a slot when it is in the item tag curios:<slot>. Botania's masks, the Cob Pipe and the stepstools stay in
// the pack file.

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item

	// Hands: the Falconry Glove (no mod registered a hands slot, so it had nowhere to go).
	t.add(e, i.t_curios_hands, i.falconry_glove)

	// Face: Simple Hats' masks and chalk stick. Simple Hats adds every hat to curios:head through a mixin, so these stay
	// valid in a head slot too.
	t.add(e, i.t_curios_face, [
		i.simplehats_chalk_stick,
		i.simplehats_angrymask, i.simplehats_cucumbereyemask, i.simplehats_ghostmask,
		i.simplehats_hockeymask, i.simplehats_horsemask, i.simplehats_sleepeyemask,
	])
})
