// requires: create
// Create's food items and machine-only food routes: the egg solution fluid and the wheat flour / dough merge onto
// Create's flour and dough. Non-member file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes).
// Moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Bread rolls ####################
		// Sweet roll and the Create Factory rolls took only vanilla bread; any bread works (creativeengineering/bread_tags.js)
		R.replaceInput([{ id: 'create:filling/sweet_roll' }, { id: /^create_factory:(caramel|chocolate|dark_chocolate|honey|ruby_chocolate|white_chocolate)_roll$/ }],
			i.minecraft_bread, i.t_bread, e)

		//######################### Egg solution #########################
		// The pack's egg solution fluid (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js, TODO dishes/ingredients #2). Mixer: 1 egg + 250 mB
		// milk -> 500 mB. Each 250 mB stands in for one egg and its milk, so an egg goes twice as far in the mixer and spout.
		// Tank-only, so every use is a Create recipe; the grid and pot recipes keep their eggs. The uses are in the folder
		// of the food they make (corn_delight, supplementaries, rusticdelight, incubation, farmersdelight, fruitsdelight,
		// neapolitan); Brewin' and Chewin's omelet and quiche stay in the pack's cooking.js.
		R.createMixing(Fluid.of(f.egg_solution, 500), [i.eggs, Fluid.of(f.milk, 250)], e, 'egg_solution', 'n/a')
		R.createMixing(Item.of(i.in_dough, 6), [i.flour, i.flour, i.flour, Fluid.of(f.egg_solution, 250)], e, 'dough_egg_solution', 'n/a')

		//######################### Flour, dough, bread
		// Combine wheat flours and doughs into create version
		R.remove({ id: 'create:crushing/misc/scrap_hay_block' }, e)
		R.remove({ id: 'tmted:wheat_dough_from_waterbucket' }, e)
		R.remove({ id: 'tmted:slime_ball' }, e)
		R.remove({ id: 'tmted:crusher/wheat_flour_from_bale' }, e)
		R.remove({ id: 'farmersdelight:bread_from_smelting' }, e)
		R.remove({ id: 'farmersdelight:bread_from_smoking' }, e)
		// Vintage Delight's furnace bread: Creative Delight members #3 (vintagedelight/recipes.js)
		R.remove({ id: 'create_central_kitchen:crafting/dough_1' }, e)
		R.remove({ id: 'create_central_kitchen:crafting/dough_2' }, e)
		R.remove({ id: 'create_central_kitchen:crafting/dough_3' }, e)
		R.remove({ id: 'create_central_kitchen:crafting/dough_4' }, e)
		R.remove({ id: 'farmersdelight:wheat_dough_from_water' }, e)
		R.remove({ id: 'tmted:bottling/wheat_dough' }, e)
		R.replaceOutput({ output: i.out_flour }, i.out_flour, i.in_flour, e)
		R.replaceInput({ input: i.out_flour }, i.out_flour, i.in_flour, e)
		R.shapeless(Item.of(i.dough, 1), [i.flour, i.water_bucket], e)
		if (Platform.isLoaded('immersiveengineering')) {
			R.ieBottling(i.dough, f.t_water, i.flour, e, undefined, 250)
			if (Platform.isLoaded('create_compressed')) {
				R.ieBottling(i.dough_block, f.t_water, i.wheat_flour_pile, e, undefined, 1000)
			}
		}
		// Dough blocks for the other doughs: creativeengineering/recipes.js
		if (Platform.isLoaded('immersiveengineering')) {
			R.ieCrushing(Item.of(i.flour, 2), i.wheat, e)
			R.ieCrushing(Item.of(i.flour, 18), i.hay_bale, e)
		}
		// Crushing wheels + Millstone + mortar and pestle (foodGrinding, TODO cooking/kitchen #10)
		R.foodGrinding([Item.of(i.flour, 13), Item.of(i.flour, 2).withChance(0.25)], i.hay_bale, e, 'hay_block_flour')
		// PC wheat compat
		if (Platform.isLoaded('pneumaticcraft')) {
			R.remove({ id: 'pneumaticcraft:pressure_chamber/wheat_flour' }, e)
			R.remove({ id: 'pneumaticcraft:explosion_crafting/wheat_flour' }, e)
			R.pcPressureChamber(Item.of(i.flour, 3), [i.wheat], 1.5, e)
			R.pcExplosionCrafting(Item.of(i.flour, 1), i.wheat, 50, e)
		}
		// More efficient craft w/ egg
		R.remove({ id: 'tmted:wheat_dough_from_egg' }, e)
		R.remove({ id: 'farmersdelight:wheat_dough_from_eggs' }, e)
		R.shapeless(Item.of(i.dough, 6), [i.flour, i.flour, i.flour, i.eggs], e, '3_wheat_flour_to_6_dough_by_shapeless')
	})
})
