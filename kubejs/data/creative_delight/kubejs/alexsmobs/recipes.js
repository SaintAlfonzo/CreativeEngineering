// requires: alexsmobs
// Alex's Mobs recipe changes. Non-member file: recipe ids are creative_delight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Shrimp fried rice #########################
		// Alex's Mobs makes it only in world (a tamed mantis shrimp fries the rice it holds on a furnace). Cooking pot
		// (TODO dishes/meals #10): Fried Rice + 2 shrimp (Oceanic Delight's #forge:shrimps). The Fried Rice's bowl comes
		// back as its crafting remainder. Food values from these inputs: CD startup alexsmobs/alexsmobs_foods.js.
		if (global.CD.on('oceanic_delight'))
			R.fdCooking([i.fried_rice, i.t_shrimps, i.t_shrimps], i.shrimp_fried_rice, e, 'shrimp_fried_rice', 1.0, 200)
	})
})
