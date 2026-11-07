// requires: creativedelight
// requires: create
// Seeds -> bone meal on the millstone and crushing wheels (TODO farming/composting #4): 15 % per seed. The seed tag is
// filled in ../creativeengineering/compost_tags.js. Moved from Creative Engineering's server scripts (wood/compost.js)
// 2026-11-06, row unchanged. Non-member file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes; was
// creativeengineering:).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		R.createMilling(Item.of(i.bonemeal).withChance(0.15), i.t_millable_seeds, e, 'seeds_bone_meal')
	})
})
