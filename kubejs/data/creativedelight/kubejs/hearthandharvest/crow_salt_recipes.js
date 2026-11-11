// requires: creativedelight
// requires: hearthandharvest
// Salt lick, scarecrow and nest recipes (plan_hearth_crow_salt.md 2, 3, 5; plan_hearth_crow_swap.md N4; hearth_and_harvest.md 5.7 N23-9).
// The scarecrow takes any farmer's hat through #hearthandharvest:farmers_hats. Recipe ids are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		// 4 Raw Rock Salt in a square give stage 0 of the lick, The Salt's block (N23-9). Lossy against 16 salt by hand, so no loop.
		R.shaped(i.salt_salt_block, ['RR', 'RR'], {R: i.raw_rock_salt}, e, 'salt_lick_from_raw_rock_salt')
		R.shaped(i.scarecrow, [' f ', 'sHs', ' s '], {f: i.t_farmers_hats, H: i.hay_bale, s: i.stick}, e, 'scarecrow')
		R.shaped(i.nest, ['SSS', 'sss'], {S: i.straw, s: i.stick}, e, 'nest')
		// Crow Nest back to straw (plan_hearth_crow_swap.md N4): 2 by hand, 3 on the millstone. Generated nests are the main source.
		R.shapeless(Item.of(i.straw, 2), [i.nest], e, 'straw_from_crow_nest')
		R.createMilling(Item.of(i.straw, 3), i.nest, e, 'straw_from_crow_nest')
	})
})
