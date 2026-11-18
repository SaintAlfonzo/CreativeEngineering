// requires: creativedelight
// requires: oceanic_delight
// Oceanic Delight recipe changes (ocean_delight.md 3). Recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('oceanic_delight')) return

	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		//######################### Fish egg roll #########################
		// Salmon eggs or ancient fish eggs on top; no chicken eggs (dishes/ocean #2).
		R.replaceInput({ id: 'oceanic_delight:fish_egg_roll' }, i.caviar, [i.caviar, i.ancient_fish_eggs], e)
	})
})
