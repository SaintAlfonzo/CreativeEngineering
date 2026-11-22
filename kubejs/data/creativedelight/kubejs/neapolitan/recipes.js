// requires: creativedelight
// requires: neapolitan
// Neapolitan recipe changes: vanilla pudding from egg solution, ice cubes and the ice foods that take them.
// Non-member file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {

		//######################### Banana bunch #########################
		// Knife cutting, 1 bunch -> 6 bananas (dishes/ingredients #18, plan_small_fixes_1116.md R15); Neapolitan's own
		// right-click opening of the bunch is unchanged
		R.fdCutting(i.banana_bunch, i.any_knife, Item.of(i.neapolitan_banana, 6), e, 'banana_bunch')

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
		if (Platform.isLoaded('immersiveengineering')) {
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

		//######################### Strawberry Scones #########################
		// The scone family's recipe (baking/baked-goods #36): Hearth and Harvest's raspberry scone with strawberries,
		// oven on the baking sheet; Neapolitan's grid and Create Central Kitchen's routes go (was 2 flour + strawberry +
		// sugar -> 2 by hand); 2 per bake (owner; was 1)
		var F = global.CD.food(e)
		F._sweep(i.strawberry_scones)
		R.ceBaking([i.butter, F.slot(i.t_fruits_strawberry, 2), i.sugar, i.batter], Item.of(i.strawberry_scones, 2), e, 'strawberry_scones', F.ware.sheet)

		//######################### Adzuki Bun #########################
		// Oven on the baking sheet (baking/baked-goods #35, owner; was roasted adzuki beans + milk + wheat ->
		// 1 by hand): flour + roasted adzuki beans + milk -> 2. Neapolitan's grid recipe goes.
		F._sweep(i.adzuki_bun)
		R.ceBaking([i.flour, i.roasted_adzuki_beans, i.t_milk_bottle], Item.of(i.adzuki_bun, 2), e, 'adzuki_bun', F.ware.sheet)
	})
})
