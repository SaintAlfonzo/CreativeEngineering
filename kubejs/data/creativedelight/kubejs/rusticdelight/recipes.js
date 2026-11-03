// requires: rusticdelight
// Rustic Delight 1.7.0 recipe changes (feature_writeup/rustic_delight.md 5.2). Creative Delight member script: recipe
// ids are creativedelight:<type>/<suffix> (global.CD.recipes). The port's data is upstream's 1.7.0 data; each change
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
		// pack): maple foods take half Rustic's syrup, half honey as their main flavour (owner 2026-11-01, TODO
		// creativedelight/members #16), so one syrup and one honey; it does not clash with the Syrup Cookie. Each family
		// call sweeps the cookie's and cheesecake's other recipes.
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
		// Abnormals Delight's maple foods stay; Rustic's syrup stands in for Autumnity's (owner 2026-10-25). Maple is half
		// syrup, half honey (owner 2026-11-01, creativedelight/members #16): the bacon takes one of each (was syrup only),
		// the Maple Cookie above too.
		R.shapeless(i.maple_glazed_bacon, [i.cooked_bacon, i.syrup, i.honey_bottle], e, 'maple_glazed_bacon')

		// The rows below moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.

		//######################### Potato Salad #########################
		// One Potato Salad (TODO merges/dishes #4, owner 2026-11-02; item_merges.md 2.17): Rustic Delight's stays, with its
		// texture; More Delight's and Oceanic Delight's merge into it (CD swap lists merges_moredelight.json,
		// merges_oceanic_delight_potato.json). Their recipes go (Oceanic's pickle and cabbage input with them). Cooking
		// Pot: 2 potatoes of any kind (potato, sweet potato, or their slices), 1 egg, 1 milk, 1 onion or garlic, in a bowl;
		// by hand the same plus 1 more potato.
		var POTATO_ANY = [i.any_potato, i.t_vegetables_sweet_potato]
		var ONION_OR_GARLIC = [i.onion, i.t_crops_garlic]
		;['rusticdelight:potato_salad', 'moredelight:cooking/potato_salad', 'oceanic_delight:potato_salad']
			.forEach(id => R.remove({ id: id }, e))
		R.fdCooking([POTATO_ANY, POTATO_ANY, i.eggs, i.milk, ONION_OR_GARLIC], i.potato_salad, e, 'potato_salad', 1.0, 200, i.bowl)
		R.shapeless(i.potato_salad, [POTATO_ANY, POTATO_ANY, POTATO_ANY, i.eggs, i.milk, ONION_OR_GARLIC, i.bowl], e,
			'potato_salad_manual_only')

		//######################### Egg solution #########################
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			// Batter: wheat flour, cornmeal or rice in each grain slot (TODO dishes/ingredients #24; was 2 wheat flour)
			R.createMixing(Item.of(i.batter, 2), [[i.t_flour_or_cornmeal, i.rice], [i.t_flour_or_cornmeal, i.rice], Item.of(i.bowl, 2),
				Fluid.of(f.egg_solution, 250)], e, 'batter_egg_solution', 'heated')
			// Potato Salad: the pot recipe's 2 potatoes of any kind and onion or garlic (merges/dishes #4; was 1 potato, onion)
			R.createMixing(i.potato_salad, [POTATO_ANY, POTATO_ANY, ONION_OR_GARLIC, i.bowl, Fluid.of(f.egg_solution, 250)], e,
				'potato_salad_egg_solution', 'n/a')
		}

		//######################### Glowing calamari #########################
		// The calamari line for glow squids (TODO dishes/ocean #3; items in kubejs_startup/rusticdelight/glowing_calamari.js,
		// glow squids cut to it in miners_delight/recipes.js). It is a raw calamari with a glow: it cuts, cooks and rolls like
		// Rustic's, and cooks into Rustic's own Cooked Calamari and Cooked Calamari Slice (no cooked glowing items). Slices
		// and bone meal from the cut, the cooking trio, 2 rolls from 2 raw slices and cooked rice. Fried calamari and calamari
		// soup take it through #forge:raw_fishes/calamari (tags.js).
		// The cut is the fish family's (CD.food fishSlice, with the bone meal), at Rustic's calamari count of 2.
		F.fishSlice(i.glowing_calamari, i.glowing_calamari_slice, 2)
		F.fishCook(i.glowing_calamari, i.cooked_calamari)
		F.fishCook(i.glowing_calamari_slice, i.cooked_calamari_slice)
		R.shapeless(Item.of(i.glowing_calamari_roll, 2), [i.glowing_calamari_slice, i.glowing_calamari_slice, i.cooked_rice], e,
			'glowing_calamari_roll')
		// Glowing Calamari Crate (creativedelight/members #25): 9 glowing calamari, as Rustic's Calamari Crate
		R.shaped(i.glowing_calamari_crate, ['ccc', 'ccc', 'ccc'], { c: i.glowing_calamari }, e, 'glowing_calamari_crate')
		R.shapeless(Item.of(i.glowing_calamari, 9), [i.glowing_calamari_crate], e, 'glowing_calamari_from_crate')

		//######################### Heatless meals #########################
		// Heatless meal rule: farmersdelight/recipes.js "Meals" (CD.food mealCraft / soupCraft: ids end _cooked_manual_only).
		// Pastas take no broth: the pot's bowl.
		// Bell pepper pasta: any 3 roasted peppers, as Rustic Delight 1.7.0's pot recipe takes any 3 peppers (was one of each colour)
		F.mealCraft([i.pasta, i.t_roasted_bell_pepper, i.t_roasted_bell_pepper, i.t_roasted_bell_pepper, i.t_soup_veggie_cooked,
			i.bowl], i.bell_pepper_pasta)
		// The other Rustic pot meals (creativedelight/members #26, "heated manual recipes"): the cooked form of each raw part,
		// one more main ingredient than the pot. Soups take the bone broth, which brings the bowl. Fried foods, spring rolls,
		// stuffed peppers and pancakes keep the pot only: frying and stuffing need the heat.
		F.soupCraft([i.t_roasted_bell_pepper, i.t_roasted_bell_pepper, i.t_roasted_bell_pepper, i.t_roasted_bell_pepper],
			i.bell_pepper_soup)   // pot: 3 peppers
		F.soupCraft([i.t_cooked_calamari, i.t_cooked_calamari, i.baked_potato, i.onion, i.milk], i.calamari_soup)   // pot: calamari, potato, onion, milk
		F.mealCraft([i.cooked_beef, i.cooked_beef, i.t_coffee_food_ingredients, i.carrot, i.baked_potato, i.bowl],
			i.coffee_braised_beef)   // pot: raw beef, a coffee, carrot, potato

		//######################### Roll assemblies #########################
		// Sequenced assembly for every Rustic sushi craft (creativedelight/members #26), beside the mod's shapeless recipes,
		// as Farmer's Delight's rolls (farmersdelight/recipes.js): on cooked rice, the grid's two fillings deployed, same
		// output. Calamari Roll deploys Rustic's Calamari Slice only: its tag also holds the glowing slices, which have their
		// own roll. Rice Roll Royale (the roll platter) on a bowl, its 8 parts in the grid's order.
		;['green', 'red', 'yellow', 'orange', 'white', 'pink', 'blue', 'purple', 'black'].forEach(c =>
			F.assemble(i.cooked_rice, [i['bell_pepper_slice_' + c], i['bell_pepper_slice_' + c]], Item.of(i['bell_pepper_roll_' + c], 2)))
		F.assemble(i.cooked_rice, [i.t_cherry_blossom, i.t_cherry_blossom], Item.of(i.cherry_blossom_roll, 2))
		F.assemble(i.cooked_rice, [i.calamari_slice, i.calamari_slice], Item.of(i.calamari_roll, 2))
		F.assemble(i.cooked_rice, [i.glowing_calamari_slice, i.glowing_calamari_slice], Item.of(i.glowing_calamari_roll, 2))
		F.assemble(i.bowl, [i.kelp_roll_slice, i.kelp_roll_slice, i.kelp_roll_slice, i.bell_pepper_roll_green,
			i.bell_pepper_roll_yellow, i.bell_pepper_roll_red, i.calamari_roll, i.cherry_blossom_roll], i.rice_roll_royale)

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

		//######################### Potato slices #########################
		// A potato cuts into 3 slices (upstream 2; TODO dishes/ingredients #26), as a sweet potato into 3 sliced sweet
		// potatoes (veggiesdelight/recipes.js). Slice & Dice's slicer copies every cutting board recipe after KubeJS
		// runs, so it follows. The baked potato's 2 baked slices stay.
		R.remove({ id: 'rusticdelight:cutting/potato_slices' }, e)
		R.fdCutting(i.minecraft_potato, i.any_knife, Item.of(i.potato_slices, 3), e, 'potato_slices')
	})
})
