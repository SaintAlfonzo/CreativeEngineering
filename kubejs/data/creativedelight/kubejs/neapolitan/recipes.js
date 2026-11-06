// requires: neapolitan
// Neapolitan recipe changes: vanilla pudding from egg solution, ice cubes and the ice foods that take them.
// Non-member file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's
// kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Smoothie #########################
		// The Strawberry Banana Smoothie is removed (global.removedItems; TODO dishes/sweets #5)
		R.remove({ output: i.strawberry_banana_smoothie }, e)

		//######################### Egg solution #########################
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			R.createMixing(i.vanilla_pudding, [i.dried_vanilla_pods, i.any_sweetener, i.bowl, Fluid.of(f.egg_solution, 250)], e,
				'vanilla_pudding_egg_solution', 'heated')
		}

		//######################### Ice cubes #########################
		//Ice cubes (TODO dishes/ingredients #5)
		// Neapolitan's pickaxe-on-board recipe names neapolitan:ice_cube (no such item) and never loads; the hammer does it
		// instead: 4 cubes by hand, 3 by machine (Create's own crushing gives 3 + 25% 3), 9 cubes back to 1 ice.
		R.remove({ id: 'neapolitan:ice_cutting' }, e)
		if (Platform.isLoaded('immersiveengineering') && Platform.isLoaded('farmersdelight')) {
			R.hammering(Item.of(i.ice_cubes, 4), i.ice, e, 'ice_cubes')
		}
		if (Platform.isLoaded('immersiveengineering')) {
			R.ieCrushing(Item.of(i.ice_cubes, 3), i.ice, e, 'ice_cubes')
		}
		// Every ice food takes ice or cubes: #forge:ice_cubes holds both (minecraft/tags.js), so the Cooking for Blockheads
		// fridge's ice unit (it lends minecraft:ice) covers them too. Not the 9 cubes -> ice recipe.
		// Runs after every pack script now; both swaps walk only the recipes loaded from data, as before.
		R.replaceInput({ not: { id: 'neapolitan:ice' } }, i.ice_cubes, i.t_ice_cubes, e)
		// FD's melon popsicle recipe is Abnormals Delight's while Neapolitan is loaded (farmersdelight:melon_popsicle doesn't load)
		// G5 begin: recipe ids handed only to R.replaceInput
		var iceFoods = ['abnormals_delight:neapolitan/melon_popsicle', 'moredelight:chocolate_popsicle', 'fruitsdelight:kiwi_popsicle',
			'fruitsdelight:hamimelon_shaved_ice', 'farmersdelight:cooking/bellini_cocktail',
			'fruitsdelight:mixing/flowing_bellini_cocktail']
		// G5 end
		R.replaceInput(iceFoods.map(id => ({ id: id })), i.ice, i.t_ice_cubes, e)
	})
})
