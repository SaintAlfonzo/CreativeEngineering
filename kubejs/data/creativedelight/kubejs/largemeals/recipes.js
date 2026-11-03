// requires: largemeals
// Large Meals 1.3.0 port recipes (feature_writeup/large_meals.md 3.1, 3.2, 4 step 8). Creative Delight member script:
// recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). The member loads none of the donor's recipe files,
// so every Large Meals recipe is here. Port changes against upstream: brown mushrooms, bacon and eggs on tags (B4, B6);
// the mutton rack cooks in the furnace, smoker and campfire (B2); the Roasted Rack of Mutton feast is a Cooking Pot
// recipe on the raw rack plus a dearer one on the cooked rack (B3, D7), the upstream grid craft stays as the heatless
// route; every pot meal gets a heatless grid route from cooked ingredients (one more main ingredient than the pot,
// bone broth for soups) and every feast one sequenced assembly on a bowl (recipes_cooking.md 2.3).
// The Cooking Pot holds 6 ingredients, so the Roasted Rack pot recipes are smaller than the grid craft (Q3, see large_meals.md 4.2).
// Slice & Dice copies the Cooking Pot recipe into heated mixing after KubeJS runs (its usual mirror).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('largemeals')) return

	var i = global.item
	var R = global.NCH
	var F = global.CD.food(e)

	global.CD.recipes(() => {

		//######################### Mutton rack #########################
		// Raw rack from a sheep killed with a knife (loot modifier); cooked by the pack's raw-meat route (B2)
		F.meatCook(i.raw_mutton_rack, i.cooked_mutton_rack)
		R.fdCutting(i.raw_mutton_rack, i.any_knife, Item.of(i.mutton_chops, 4), e, 'raw_mutton_rack')
		R.fdCutting(i.cooked_mutton_rack, i.any_knife, Item.of(i.cooked_mutton_chops, 4), e, 'cooked_mutton_rack')

		//######################### Soups and meals (Cooking Pot, bowl from the meal's remainder) #########################
		R.fdCooking([i.pufferfish, i.potato, i.beetroot, i.onion, i.milk], i.pufferfish_broth, e, 'pufferfish_broth', 1, 200)
		R.fdCooking([i.potato, i.potato, i.onion, i.milk], i.potato_soup, e, 'potato_soup', 1, 200)
		R.fdCooking([i.tomato, i.beetroot, i.onion], i.red_soup, e, 'red_soup', 1, 200)
		R.fdCooking([i.tomato, i.eggs, i.onion], i.tomato_egg_soup, e, 'tomato_egg_soup', 1, 200)
		R.fdCooking([i.t_raw_cod, i.beetroot, i.rice, i.tomato], i.cod_deluxe, e, 'cod_deluxe', 1, 200)
		R.fdCooking([i.t_raw_bacon, i.t_raw_bacon, i.rice, i.cabbage, i.carrot, i.onion], i.hearty_lunch, e, 'hearty_lunch', 1, 200)
		R.fdCooking([i.raw_chicken, i.onion, i.milk, i.tomato_sauce], i.lm_chicken_curry, e, 'chicken_curry', 1, 200)
		R.fdCooking([i.t_mushrooms_brown, i.t_mushrooms_brown, i.milk, i.onion, i.pasta], i.pasta_with_mushroom_sauce, e,
			'pasta_with_mushroom_sauce', 1, 200)

		//######################### Desserts (bottle / bowl from the remainder) #########################
		R.fdCooking([i.sweet_berries, i.milk, i.eggs, i.sugar], i.sweet_berry_custard, e, 'sweet_berry_custard', 1, 200)
		R.fdCooking([i.rice, i.milk, i.sugar], i.rice_pudding, e, 'rice_pudding', 1, 200)

		//######################### Feasts (Cooking Pot, bowl container, 250 ticks) #########################
		R.fdCooking([i.rice, i.raw_chicken, i.eggs, i.milk, i.carrot, i.cabbage], i.omurice_block, e, 'omurice_block', 1.5, 250, i.bowl)
		R.fdCooking([i.brown_mushroom_colony, i.raw_chicken, i.carrot, i.cabbage, i.milk, i.pie_crust], i.mushroom_pot_pie_block, e,
			'mushroom_pot_pie_block', 1.5, 250, i.bowl)
		// Roasted Rack of Mutton (D7, Q2, Q3): the pot takes 6 ingredients, so the base recipe is the raw rack with 2 cooked
		// rice, 2 carrots and a baked potato (6), the dearer one the cooked rack with a cooked rice, 2 carrots and 2 baked
		// potatoes (6). Both give the same feast; the grid craft below is the heatless route.
		R.fdCooking([i.raw_mutton_rack, i.cooked_rice, i.cooked_rice, i.carrot, i.carrot, i.baked_potato], i.roasted_mutton_rack_block, e,
			'roasted_mutton_rack_block', 1.5, 250, i.bowl)
		R.fdCooking([i.cooked_mutton_rack, i.cooked_rice, i.carrot, i.carrot, i.baked_potato, i.baked_potato],
			i.roasted_mutton_rack_block, e, 'roasted_mutton_rack_block_cooked', 1.5, 250, i.bowl)

		//######################### Heatless routes (_manual_only: Create's basin and crafters skip them) #########################
		// Soups: bone broth brings the bowl; cooked forms of the raw main ingredients, one more main ingredient than the pot
		F.soupCraft([i.baked_potato, i.baked_potato, i.baked_potato, i.onion, i.milk], i.potato_soup)
		F.soupCraft([i.tomato_sauce, i.tomato_sauce, i.beetroot, i.onion], i.red_soup)
		F.soupCraft([i.tomato_sauce, i.tomato_sauce, i.cooked_eggs, i.onion], i.tomato_egg_soup)
		F.soupCraft([i.pufferfish, i.baked_potato, i.baked_potato, i.beetroot, i.onion, i.milk], i.pufferfish_broth)
		// Meals and desserts: the bowl (or bottle) is in the grid
		R.shapeless(i.cod_deluxe, [i.cooked_cod, i.cooked_cod, i.beetroot, i.cooked_rice, i.tomato_sauce, i.bowl], e, 'cod_deluxe_cooked_manual_only')
		R.shapeless(i.hearty_lunch, [i.cooked_bacon, i.cooked_bacon, i.cooked_bacon, i.cooked_rice, i.cabbage, i.carrot, i.onion, i.bowl], e,
			'hearty_lunch_cooked_manual_only')
		R.shapeless(i.lm_chicken_curry, [i.cooked_chicken, i.cooked_chicken, i.onion, i.milk, i.tomato_sauce, i.bowl], e,
			'chicken_curry_cooked_manual_only')
		R.shapeless(i.pasta_with_mushroom_sauce, [i.t_mushrooms_brown, i.t_mushrooms_brown, i.t_mushrooms_brown, i.milk, i.onion, i.pasta, i.bowl], e,
			'pasta_with_mushroom_sauce_manual_only')
		R.shapeless(i.sweet_berry_custard, [i.sweet_berries, i.sweet_berries, i.milk, i.cooked_eggs, i.sugar, i.glass_bottle], e,
			'sweet_berry_custard_cooked_manual_only')
		R.shapeless(i.rice_pudding, [i.cooked_rice, i.cooked_rice, i.milk, i.sugar, i.bowl], e, 'rice_pudding_cooked_manual_only')
		// Feasts: cooked forms, one more main ingredient than the pot
		R.shapeless(i.omurice_block, [i.cooked_rice, i.cooked_chicken, i.cooked_chicken, i.cooked_eggs, i.milk, i.carrot, i.cabbage, i.bowl], e,
			'omurice_block_cooked_manual_only')
		R.shapeless(i.mushroom_pot_pie_block, [i.brown_mushroom_colony, i.cooked_chicken, i.cooked_chicken, i.carrot, i.cabbage, i.milk,
			i.pie_crust, i.bowl], e, 'mushroom_pot_pie_block_cooked_manual_only')
		// Upstream's grid craft, kept as the heatless route (B3)
		R.shapeless(i.roasted_mutton_rack_block, [i.cooked_rice, i.cooked_rice, i.cooked_rice, i.carrot, i.carrot, i.cooked_mutton_rack,
			i.baked_potato, i.baked_potato, i.bowl], e, 'roasted_mutton_rack_block_manual_only')

		//######################### Sequenced assemblies on a bowl (one per feast) #########################
		F.assemble(i.bowl, [i.cooked_rice, i.cooked_chicken, i.cooked_chicken, i.cooked_eggs, i.carrot, i.cabbage], i.omurice_block)
		F.assemble(i.bowl, [i.pie_crust, i.brown_mushroom_colony, i.cooked_chicken, i.cooked_chicken, i.carrot, i.cabbage], i.mushroom_pot_pie_block)
		F.assemble(i.bowl, [i.cooked_rice, i.cooked_rice, i.cooked_rice, i.cooked_mutton_rack, i.carrot, i.carrot, i.baked_potato, i.baked_potato],
			i.roasted_mutton_rack_block)
	})
})
