// Watering can (Hearth and Harvest's, item in patcherMod wateringcan/; feature_writeup/hearth_and_harvest.md 4.0):
// upstream recipe, and the fertiliser tag it loads from the off hand. Upstream also lists its Manure (not in the pack yet).

ServerEvents.recipes(e => {
	const R = global.NCH
	var i = global.item

	R.shaped(Item.of(i.watering_can), ['C  ', 'CBC', 'CC '], {C: i.ingot_copper, B: i.bucket}, e, 'watering_can')
})

ServerEvents.tags('item', e => {
	var i = global.item
	global.tags.add(e, i.t_bonemeal_substitutes, [i.bonemeal])
})
