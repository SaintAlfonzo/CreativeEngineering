// requires: creativedelight
// requires: pneumaticcraft
// PneumaticCraft food: sourdough, tempura, chips and fish and chips, and its machine-only routes for the pack's
// cornmeal. Non-member file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's
// kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Cornmeal #########################
		// The pack's cornmeal (creativedelight/recipes.js has the rule and the grinding).
		// PneumaticCraft: corn as wheat -> flour (3 at 1.5 bar, explosion 1 at 50% loss); kernels 4 -> 3. No crates/bags.
		if (Platform.isLoaded('corn_delight')) {
			R.pcPressureChamber(Item.of(i.cornmeal, 3), [i.t_crops_corn], 1.5, e, 'corn_cornmeal')
			R.pcExplosionCrafting(Item.of(i.cornmeal, 1), i.t_crops_corn, 50, e, 'corn_cornmeal')
			R.pcPressureChamber(Item.of(i.cornmeal, 3), [Item.of(i.corn_seeds, 4)], 1.5, e, 'corn_kernels_cornmeal')
		}

		//######################### Sourdough #########################
		//Swap flour to dough
		R.remove({ id: 'pneumaticcraft:sourdough' }, e)
		R.shapeless(Item.of(i.sourdough, 1), [i.yeast_culture_bucket, i.dough, i.dough, i.dough, i.dough, i.dough, i.dough, i.dough, i.dough], e)
		// Make smoking
		R.remove({ id: 'pneumaticcraft:sourdough_bread' }, e)
		R.smoking(i.sourdough_bread, i.sourdough, e)
		// Oven: sourdough has its own bread, so it bakes into that and not plain bread (farmersdelight/recipes.js "Bread from
		// dough"; TODO baking/baked-goods #31), 1 / 3 / 6 on a baking sheet
		;[1, 3, 6].forEach(k => {
			R.ceBaking([k > 1 ? { id: i.sourdough, count: k } : i.sourdough], Item.of(i.sourdough_bread, k), e, 'sourdough_bread_' + k,
				{ exp: 0.1, time: 200, tool: i.t_bakeware_baking_sheet })
		})
		// slice to 3 bread
		if (Platform.isLoaded('farmersdelight')) {
			R.fdCutting(i.sourdough_bread, i.any_knife, Item.of(i.bread, 3), e)
		}
		// Mixing
		if (Platform.isLoaded('create')) {
			R.createMixing(Item.of(i.sourdough, 8), [Fluid.of(f.yeast_culture, 500), i.dough, i.dough, i.dough, i.dough, i.dough, i.dough, i.dough, i.dough], e, undefined, 'heated')
		}
		// Bottling
		if (Platform.isLoaded('immersiveengineering')) {
			R.ieBottling(i.sourdough, f.t_yeast_culture, i.dough, e, undefined, 50)
		}

		//######################### Frying #########################
		// Frying with cooking oil in the pot, any whole fish (TODO dishes/meals #7). PneumaticCraft's Thermopneumatic deep
		// frying is gone; Slice & Dice gives each pot recipe its heated mixing copy. PneumaticCraft's Chips (Fries) and its
		// two tempuras are removed; their recipes go here.
		R.remove({ id: 'pneumaticcraft:thermo_plant/chips' }, e)
		R.remove({ id: 'pneumaticcraft:thermo_plant/salmon_tempura' }, e)
		R.remove({ id: 'pneumaticcraft:raw_salmon_tempura' }, e)
		R.remove({ id: 'pneumaticcraft:cod_n_chips' }, e)
		// Fish Tempura and Raw Fish Tempura are removed items (TODO merges/dishes #11, 2026-11-02; CD startup
		// pneumaticcraft/pneumatic_items.js): no recipe makes them. Fried fish stays as Fish and Chips below.
		// Fish and Chips (TODO merges/dishes #1, Veggies Delight M1): PneumaticCraft's Cod 'n' Chips merges into Veggies
		// Delight's (CD startup pneumaticcraft/item_merges.js). One frying route makes the survivor, in its bowl; Veggies
		// Delight's two pot recipes (egg and potato, egg and diced potatoes) go as duplicates. PneumaticCraft's Chips
		// (Fries) are removed (owner 2026-11-01, CD startup pneumaticcraft/pneumatic_items.js), so the Fries + fish grid
		// route went with them.
		if (Platform.isLoaded('veggiesdelight')) {
			R.remove({ id: 'veggiesdelight:cooking/fish_and_chips' }, e)
			R.remove({ id: 'veggiesdelight:compat/moredelight/cooking/fish_and_chips' }, e)
			if (Platform.isLoaded('farmersdelight')) {
				R.fdCooking([i.t_cooking_oil, i.t_food_raw_fish, i.t_sliced_potato, i.t_sliced_potato], i.fish_and_chips, e,
					'fish_and_chips', 0.35, 200, i.bowl)
			}
		}
	})
})
