// requires: fruitsdelight
// Fruits Delight recipe changes. Non-member file: recipe ids are creative_delight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Cooked fruit #########################
		// Dried or baked fruit is made in all three of smoker, furnace and campfire (food_families.md 3.1
		// `fruit/cooked`). Fruits Delight ships smoker and campfire only; the furnace takes the smoker's input and xp
		// at twice its time.
		R.smelting(i.baked_durian, i.durian_flesh, e, 'baked_durian_from_durian_flesh', 0.1, 200)
		R.smelting(i.baked_pear, i.t_fruits_pear, e, 'baked_pear_from_pear', 0.1, 200)
		R.smelting(i.dried_persimmon, i.t_fruits_persimmon, e, 'dried_persimmon_from_persimmon', 0.1, 200)

		//######################### 1.1.3 carry-overs (fruits_delight.md 2.2) #########################
		// Fig Pudding takes any egg
		R.replaceInput({ id: 'fruitsdelight:fig_pudding' }, i.minecraft_egg, i.eggs, e)

		// The rows below moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.

		//######################### Egg solution #########################
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			R.createMixing(i.blueberry_custard, [i.t_fruits_blueberry, i.t_fruits_blueberry, i.any_sweetener, i.glass_bottle, Fluid.of(f.egg_solution, 250)], e,
				'blueberry_custard_egg_solution', 'heated')
		}

		//######################### Heatless meals #########################
		// Heatless meal rule: farmersdelight/recipes.js "Meals". Bone broth route (TODO dishes/ingredients #4).
		R.shapeless(i.fig_chicken_stew, [i.bone_broth, i.cooked_chicken, i.t_fruits_fig, i.t_fruits_fig, i.t_fruits_fig], e, 'fig_chicken_stew_cooked_manual_only')

		//Muffins: oven only, the muffin family call in creativeengineering/baking.js; it removes Fruits Delight's Cooking
		// Pot muffins (TODO cooking/oven #3, baking/baked-goods 2026-10-29).

		//Orange jam (TODO baking/sweets #6)
		// Fruits Delight's 2-orange pot and heated mixing recipes stay; one orange's 4 slices also work
		R.fdCooking([i.orange_slice, i.orange_slice, i.orange_slice, i.orange_slice, i.any_sweetener, i.lemon_slice],
			i.orange_jelly, e, 'orange_jam_from_slices', 0.1, 200, i.glass_bottle)
		if (Platform.isLoaded('create')) {
			R.createMixing(Fluid.of(f.orange_jam, 125), [Item.of(i.orange_slice, 4), i.lemon_slice, i.any_sweetener, Fluid.of(f.water, 100)],
				e, 'orange_jam_from_slices', 'heated')
		}
	})
})
