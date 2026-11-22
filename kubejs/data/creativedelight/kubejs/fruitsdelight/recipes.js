// requires: creativedelight
// requires: fruitsdelight
// Fruits Delight recipe changes. Non-member file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		// Lemon, orange, pineapple and hamimelon cuts and whole-from-slices: creativedelight/fruit_cuts.js (TODO fruit #3; the
		// lemon cut was 8 + seeds, farming/trees #3). The tree's break drop is
		// kubejs/data/fruitsdelight/loot_tables/blocks/lemon_tree.json, its right-click pick CD's LemonTreePickYieldMixin.

		//######################### Cooked fruit ######################### Dried or baked fruit is made in all three of
		// smoker, furnace and campfire (food_families.md 3.1 `fruit/cooked`): the family call (CD.FOOD values; Fruits
		// Delight ships smoker and campfire only, at 0.1 xp) replaces the mod's two with all three.
		F.fruitCook(i.durian_flesh, i.baked_durian)
		F.fruitCook(i.t_fruits_pear, i.baked_pear)
		F.fruitCook(i.t_fruits_persimmon, i.dried_persimmon)

		//######################### 1.1.3 carry-overs (fruits_delight.md 2.2) #########################
		// Fig Pudding: one oven recipe on the baking sheet (baking/baked-goods #36; was 2 milk bottles, sweet berries,
		// 2 eggs, orange jelly, 2 figs and dough in the grid)
		F._sweep(i.fig_pudding)
		R.ceBaking([i.sweet_berries, i.t_milk_bottle, i.eggs, i.orange_jelly, i.t_fruits_fig, i.any_dough], i.fig_pudding, e,
			'fig_pudding', F.ware.sheet)


		//######################### Egg solution #########################
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			R.createMixing(i.blueberry_custard, [i.t_fruits_blueberry, i.t_fruits_blueberry, i.any_sweetener, i.glass_bottle, Fluid.of(f.egg_solution, 250)], e,
				'blueberry_custard_egg_solution', 'heated')
		}

		//######################### Heatless meals #########################
		// Heatless meal rule: farmersdelight/recipes.js "Meals". Bone broth route (TODO dishes/ingredients #4).
		F.soupCraft([i.cooked_chicken, i.t_fruits_fig, i.t_fruits_fig, i.t_fruits_fig], i.fig_chicken_stew)

		//Muffins: oven only, the muffin family call in creativedelight/baking.js; it removes Fruits Delight's Cooking
		// Pot muffins (TODO cooking/oven #3, baking/baked-goods).

		//######################### Jam cook time ######################### Food family rule (food_families.md, preserve/jam,
		// owner): every jam cooks 400 ticks in the pot. The pack's own jams already do (vintagedelight/jams.js); Fruits
		// Delight's 22 jelly recipes had 200.
		R.setCookTime({ type: 'farmersdelight:cooking', id: /^farmersdelight:cooking\/\w+_jelly$/ }, 400, e)

		//######################### Jams take slices ######################### Orange and lemon jam (TODO baking/sweets #6,
		// dishes/jams #6, owner): every whole-fruit slot of Fruits Delight's pot and heated mixing recipes also takes that
		// fruit's slice, one for one (2 oranges or 2 orange slices, or one of each). Replaces the separate from-slices
		// recipes (4 orange slices, 5 lemon slices), which also kept a glass bottle the mason-jar swap never reached
		// (r1102b SQ-PSU-06). The melon, hamimelon and pineapple jams already take slices; the other jam fruits have none.
		// Slice & Dice copies the pot recipes after this.
		;[[i.t_fruits_orange, i.orange_slice, 'orange'], [i.t_fruits_lemon, i.lemon_slice, 'lemon']].forEach(p => {
			R.replaceInput([{ id: 'farmersdelight:cooking/' + p[2] + '_jelly' }, { id: 'fruitsdelight:mixing/flowing_' + p[2] + '_jam' }],
				p[0], [p[0], p[1]], e)
		})
	})
})
