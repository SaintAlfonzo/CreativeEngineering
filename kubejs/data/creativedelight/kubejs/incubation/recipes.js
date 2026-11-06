// requires: incubation
// Incubation egg food recipe changes. Non-member file: recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Egg solution #########################
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			R.createMixing(i.scrambled_eggs, [i.bowl, Fluid.of(f.egg_solution, 750)], e, 'scrambled_eggs_egg_solution', 'heated')
		}

		//######################### Meals #########################
		// Scrambled eggs: one more egg than Incubation's
		R.remove({ id: 'incubation:scrambled_eggs' }, e)
		R.shapeless(i.scrambled_eggs, [i.eggs, i.eggs, i.eggs, i.milk, i.bowl], e, 'scrambled_eggs_manual_only')

		//######################### Protein #########################
		//Egg
		//Remove redundant fried egg (Farmer's Delight's smelting, smoking and campfire recipes are back in
		// farmersdelight/recipes.js)
		R.remove({ id: 'incubation:fried_egg' }, e)
		R.remove({ id: 'incubation:fried_egg_from_smoking' }, e)
		R.remove({ id: 'incubation:fried_egg_from_campfire_cooking' }, e)
	})
})
