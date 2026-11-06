// requires: supplementaries
// Supplementaries food recipe changes (pancakes, sugar cube). Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js
// 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Egg solution #########################
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			R.createMixing(Item.of(i.pancakes, 5), [Fluid.of(f.egg_solution, 250), i.any_sweetener, [i.t_flour_or_cornmeal, i.rice]], e,
				'pancakes_egg_solution', 'heated')
		}

		//######################### Sugar #########################
		// Sugar cube recipe to reflect the sugar tag swap (minecraft/recipes.js)
		R.remove({ id: 'supplementaries:sugar_cube' }, e)
		R.shapeless(Item.of(i.sugar_cube, 1), Item.of(i.sugar, 9), e)

		//######################### Pancakes #########################
		//Wheat to flour (or cornmeal, or rice)
		R.remove({ id: 'supplementaries:pancake' }, e)
		R.shapeless(Item.of(i.pancakes, 3), [i.any_sweetener, i.eggs, i.milk, [i.t_flour_or_cornmeal, i.rice]], e)
		// Efficient mixing, cooking
		if (Platform.isLoaded('create')) {
			R.createMixing(Item.of(i.pancakes, 5), [Fluid.of(f.milk, 250), i.any_sweetener, i.eggs, [i.t_flour_or_cornmeal, i.rice]], e, undefined, 'heated')
		}
	})
})
