// Oven: creativeengineering:baking recipes (NCH.ceBaking) and the oven / bakeware crafting recipes.
// Block, GUI and recipe type: patcherMod oven/ package. Design and tests: feature_writeup/oven.md.
//
// For now every baking recipe is an exact copy of the cooking-pot recipe for the same baked good (same
// ingredients, count, plating container, time and experience) and needs no tool. The pot recipes stay.
// Sugar is #forge:sugar, matching the pack-wide sugar swap in cooking.js.

ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH

	//######################### Baking (copies of the pot recipes) #########################
	// Fruits Delight 0.5.11 (data/farmersdelight/recipes/cooking/*.json). Muffins use #forge:dough like the
	// fixed pot recipes in cooking.js.
	R.ceBaking([i.t_fruits_blueberry, i.t_fruits_blueberry, i.any_dough, i.t_milk_bottle, i.eggs, i.any_sugar],
		Item.of(i.blueberry_muffin, 2), e, 'blueberry_muffin', { exp: 0.1, time: 200 })
	R.ceBaking([i.t_fruits_cranberry, i.t_fruits_cranberry, i.any_dough, i.t_milk_bottle, i.eggs, i.any_sugar],
		Item.of(i.cranberry_muffin, 2), e, 'cranberry_muffin', { exp: 0.1, time: 200 })
	R.ceBaking([i.durian_flesh, i.durian_flesh, i.pie_crust, i.eggs, i.any_sugar],
		Item.of(i.durian_pie, 2), e, 'durian_pie', { exp: 0.1, time: 200 })
	R.ceBaking([i.pineapple_slice, i.pineapple_slice, i.pie_crust, i.eggs, i.any_sugar],
		Item.of(i.pineapple_pie, 2), e, 'pineapple_pie', { exp: 0.1, time: 200 })
	R.ceBaking([i.t_fruits_lemon, i.pie_crust, i.eggs, i.any_sugar, i.t_milk_bottle],
		Item.of(i.lemon_tart, 2), e, 'lemon_tart', { exp: 0.1, time: 200 })
	R.ceBaking([i.t_fruits_fig, i.pie_crust, i.eggs, i.any_sugar, i.t_milk_bottle],
		Item.of(i.fig_tart, 2), e, 'fig_tart', { exp: 0.1, time: 200 })
	R.ceBaking([i.wheat, i.wheat, i.any_sugar, i.any_sugar, i.t_fruits_mangosteen, i.t_fruits_mangosteen],
		Item.of(i.mangosteen_cake, 1), e, 'mangosteen_cake', { exp: 0.1, time: 200, container: i.bowl })

	//######################### Oven and bakeware #########################
	// Oven: an iron-plate top over a hollow brick box.
	R.shaped(i.oven, ['PPP', 'B B', 'BBB'], { P: i.any_plate_iron, B: i.bricks }, e, 'oven')

	// Bakeware: iron plates (P) and nuggets (N), no durability.
	R.shaped(i.baking_sheet, ['NPN'], { N: i.nugget_iron, P: i.any_plate_iron }, e, 'baking_sheet')
	R.shaped(i.pie_tin, ['N N', ' P '], { N: i.nugget_iron, P: i.any_plate_iron }, e, 'pie_tin')
	R.shaped(i.cake_pan, ['P P', ' P '], { P: i.any_plate_iron }, e, 'cake_pan')
	R.shaped(i.muffin_tin, ['NNN', 'PPP'], { N: i.nugget_iron, P: i.any_plate_iron }, e, 'muffin_tin')
})
