// requires: rusticdelight
// Rustic Delight 1.7.0 recipe changes (feature_writeup/rustic_delight.md 5.2). Creative Delight member script: recipe
// ids are creative_delight:<type>/<suffix> (global.CD.recipes). The port's data is upstream's 1.7.0 data; each change
// removes the upstream recipe and adds the pack recipe here.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('rusticdelight')) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH
	var F = global.CD.food(e)

	global.CD.recipes(() => {

		//######################### Baking (oven.md 2.6) #########################
		// The new cookies and cheesecakes join the pack's baking overhaul: upstream's grid recipes out, oven only.
		// Cookies: cookie dough + 2 flavour -> 12. Maple Cookie (Abnormals Delight, its Autumnity syrup is not in the
		// pack) takes one syrup and one honey, so it does not clash with the Syrup Cookie. Each family call sweeps the
		// cookie's and cheesecake's other recipes.
		var COOKIES = [
			[i.syrup_cookie, i.syrup],
			[i.coffee_cookie, i.roasted_coffee_beans],
			[i.cherry_blossom_cookie, i.t_cherry_blossom],
			[i.maple_cookie, i.syrup, i.honey_bottle],
		]
		COOKIES.forEach(c => F.cookieBake(c[1], c[0], c[2]))
		// Cheesecakes are pies here (as Sweet Berry Cheesecake): pie crust + 2 flavour + 2 sweet. Slices back to a
		// whole cheesecake stay.
		var CHEESECAKES = [
			[i.syrup_cheesecake, i.syrup],
			[i.coffee_cheesecake, i.roasted_coffee_beans],
			[i.cherry_blossom_cheesecake, i.t_cherry_blossom],
		]
		CHEESECAKES.forEach(c => F.pieBake(c[1], c[0]))

		//######################### Maple foods #########################
		// Abnormals Delight's maple foods stay; Rustic's syrup stands in for Autumnity's (owner 2026-10-25).
		R.shapeless(i.maple_glazed_bacon, [i.cooked_bacon, i.syrup], e, 'maple_glazed_bacon')

		// The rows below moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.

		//######################### Egg solution #########################
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			// Batter: wheat flour, cornmeal or rice in each grain slot (TODO dishes/ingredients #24; was 2 wheat flour)
			R.createMixing(Item.of(i.batter, 2), [[i.t_flour_or_cornmeal, i.rice], [i.t_flour_or_cornmeal, i.rice], Item.of(i.bowl, 2),
				Fluid.of(f.egg_solution, 250)], e, 'batter_egg_solution', 'heated')
			R.createMixing(i.potato_salad, [i.potato, i.onion, i.bowl, Fluid.of(f.egg_solution, 250)], e, 'potato_salad_egg_solution', 'n/a')
		}

		//######################### Glowing calamari #########################
		// The calamari line for glow squids (TODO dishes/ocean #3; items in kubejs_startup/rusticdelight/glowing_calamari.js,
		// glow squids cut to it in miners_delight/recipes.js). It is a raw calamari with a glow: it cuts, cooks and rolls like
		// Rustic's, and cooks into Rustic's own Cooked Calamari and Cooked Calamari Slice (no cooked glowing items). Slices
		// and bone meal from the cut, the cooking trio, 2 rolls from 2 raw slices and cooked rice. Fried calamari and calamari
		// soup take it through #forge:raw_fishes/calamari (tags.js).
		R.fdCutting(i.glowing_calamari, i.any_knife, [Item.of(i.glowing_calamari_slice, 2), i.bonemeal], e, 'glowing_calamari_slice')
		F.fishCook(i.glowing_calamari, i.cooked_calamari)
		F.fishCook(i.glowing_calamari_slice, i.cooked_calamari_slice)
		R.shapeless(Item.of(i.glowing_calamari_roll, 2), [i.glowing_calamari_slice, i.glowing_calamari_slice, i.cooked_rice], e,
			'glowing_calamari_roll')

		//######################### Heatless meals #########################
		// Heatless meal rule: farmersdelight/recipes.js "Meals". Pastas take no broth: the pot's bowl.
		// Bell pepper pasta: any 3 roasted peppers, as Rustic Delight 1.7.0's pot recipe takes any 3 peppers (was one of each colour)
		R.shapeless(i.bell_pepper_pasta, [i.pasta, i.t_roasted_bell_pepper, i.t_roasted_bell_pepper, i.t_roasted_bell_pepper,
			i.t_soup_veggie_cooked, i.bowl], e, 'bell_pepper_pasta_cooked_manual_only')

		//######################### Stuffed peppers #########################
		// Second pot recipe for each colour: the roasted pepper holds the filling in place of the raw one (upstream:
		// raw pepper + rice + one filling). The roasted pepper is an ingredient, not the pot's container: the pot matches
		// on ingredients only, so 9 recipes differing by container alone would all match the first colour. Same time and
		// experience as upstream; Slice & Dice's mixer gets a copy of each, as it does of upstream's.
		;['green', 'red', 'yellow', 'orange', 'white', 'pink', 'blue', 'purple', 'black'].forEach(c =>
			R.fdCooking([i['roasted_bell_pepper_' + c], i.rice, i.t_stuffed_bell_pepper_ingredients], i['stuffed_bell_pepper_' + c], e,
				'stuffed_bell_pepper_' + c + '_from_roasted', 1.0, 200))

		//######################### Cooking oil #########################
		// Slice & Dice turns Cooking Pot recipes into heated mixing after KubeJS runs, so R.remove can't reach its copy;
		// it skips ids ending in _manual_only. Re-add the pot recipe under such an id and give the mixer its own ratio.
		R.remove({ id: 'rusticdelight:cooking/cooking_oil' }, e)
		R.fdCooking([i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients,
			i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients],
			Item.of(i.cooking_oil, 2), e, 'cooking_oil_manual_only', 0.35, 100, i.glass_bottle)
		if (Platform.isLoaded('create')) {
			R.createMixing(Item.of(i.cooking_oil, 1), [i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients,
				i.t_cooking_oil_ingredients, i.glass_bottle], e, 'cooking_oil_heated_mixing', 'heated')
		}
	})
})
