// requires: creativedelight
// requires: buzzier_bees
// Buzzier Bees food recipe removals. Non-member file: recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {

		//Honey
		//Honey item removals
		R.remove({ id: 'buzzier_bees:honey/honey_apple' }, e)
	})
})
