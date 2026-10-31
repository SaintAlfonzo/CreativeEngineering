ServerEvents.recipes(e => {
	console.log('Compost server script starting')

	const R = global.NCH
	var i = global.item

	// Seeds -> bone meal on the millstone and crushing wheels (TODO farming/composting #4): 15 % per seed
	R.createMilling(Item.of(i.bonemeal).withChance(0.15), i.t_millable_seeds, e, 'seeds_bone_meal')
})
