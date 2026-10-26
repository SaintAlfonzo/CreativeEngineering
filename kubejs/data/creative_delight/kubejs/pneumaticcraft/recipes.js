// requires: pneumaticcraft
// PneumaticCraft food: sourdough, tempura, chips and fish and chips, and its machine-only routes for the pack's
// cornmeal. Non-member file: recipe ids are creative_delight:<type>/<suffix> (global.CD.recipes). Moved from the pack's
// kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Cornmeal #########################
		// The pack's cornmeal (creativeengineering/recipes.js has the rule and the grinding).
		// PneumaticCraft: corn as wheat -> flour (3 at 1.5 bar, explosion 1 at 50% loss); kernels 4 -> 3. No crates/bags.
		if (Platform.isLoaded('corn_delight')) {
			R.pcPressureChamber(Item.of(i.cornmeal, 3), [i.t_crops_corn], 1.5, e, 'corn_cornmeal')
			R.pcExplosionCrafting(Item.of(i.cornmeal, 1), i.t_crops_corn, 50, e, 'corn_cornmeal')
			R.pcPressureChamber(Item.of(i.cornmeal, 3), [Item.of(i.corn_seeds, 4)], 1.5, e, 'corn_kernels_cornmeal')
		}

		//######################### Sourdough #########################
		//Swap flour to dough
		R.remove({ id: 'pneumaticcraft:sourdough' }, e)
		R.shapeless(Item.of(i.sourdough, 1), [i.yeast_culture_bucket, Item.of(i.dough, 8)], e)
		// Make smoking
		R.remove({ id: 'pneumaticcraft:sourdough_bread' }, e)
		R.smoking(i.sourdough_bread, i.sourdough, e)
		// slice to 2 bread
		if (Platform.isLoaded('farmersdelight')) {
			R.fdCutting(i.sourdough_bread, i.any_knife, Item.of(i.bread, 2), e)
		}
		// Mixing
		if (Platform.isLoaded('create')) {
			R.createMixing(Item.of(i.sourdough, 8), [Fluid.of(f.yeast_culture, 500), Item.of(i.dough, 8)], e, undefined, 'heated')
		}
		// Bottling
		if (Platform.isLoaded('immersiveengineering')) {
			R.ieBottling(i.sourdough, f.t_yeast_culture, i.dough, e, undefined, 50)
		}

		//######################### Frying #########################
		// Frying with cooking oil in the pot, any whole fish (TODO dishes/meals #7). PneumaticCraft's Thermopneumatic deep
		// frying is gone; Slice & Dice gives each pot recipe its heated mixing copy. Renames in namechanges.js:
		// Fish Tempura, Raw Fish Tempura, Fries, Fish and Chips.
		R.remove({ id: 'pneumaticcraft:thermo_plant/chips' }, e)
		R.remove({ id: 'pneumaticcraft:thermo_plant/salmon_tempura' }, e)
		R.remove({ id: 'pneumaticcraft:raw_salmon_tempura' }, e)
		R.remove({ id: 'pneumaticcraft:cod_n_chips' }, e)
		R.shapeless(i.raw_salmon_tempura, [i.t_food_raw_fish, i.batter], e, 'raw_fish_tempura')
		if (Platform.isLoaded('farmersdelight')) {
			R.fdCooking([i.t_cooking_oil, i.raw_salmon_tempura], i.salmon_tempura, e, 'fish_tempura', 0.35, 200)
			R.fdCooking([i.t_cooking_oil, i.t_sliced_potato, i.t_sliced_potato], Item.of(i.chips, 2), e, 'fries', 0.35, 200)
			R.fdCooking([i.t_cooking_oil, i.t_food_raw_fish, i.t_sliced_potato, i.t_sliced_potato], i.cod_n_chips, e,
				'fish_and_chips', 0.35, 200, i.paper)
		}
		R.shapeless(i.cod_n_chips, [i.chips, i.cooked_fish, i.paper], e, 'fish_and_chips_cooked_manual_only')
	})
})
