// requires: creativedelight
// requires: farmersdelight
// Farmer's Delight 1.3.4 recipe changes on the repacked 1.2.4 (feature_writeup/farmers_delight.md 3, TODO
// largecontent/mod-additions #18). Creative Delight member script: recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). The new blocks and items are in the repack's overlay (members/farmers).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('farmersdelight')) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		//######################### Bread ####################
		// The roast chicken stuffing took only vanilla bread; any bread works (creativedelight/bread_tags.js)
		R.replaceInput({ id: 'farmersdelight:roast_chicken_block' }, i.minecraft_bread, i.t_bread, e)

		//######################### Bread from dough #########################
		// A dough bakes into plain bread on a baking sheet, 1 / 3 / 6 at a time (TODO baking/baked-goods #31), only when it has
		// no bread of its own. #forge:dough holds exactly those (Create, sweet potato, oat; sourdough and cucumber dough are not
		// in it): a dough with its own bread must stay out of that tag and bake into that bread instead (sourdough: pneumaticcraft/recipes.js).
		// The 3 flour grid craft stays; no furnace, smoker or campfire route.
		var SHEET_BREAD = { exp: 0.1, time: 200, tool: i.t_bakeware_baking_sheet }
		;[1, 3, 6].forEach(k => {
			R.ceBaking([k > 1 ? { id: i.any_dough, count: k } : i.any_dough], Item.of(i.minecraft_bread, k), e, 'bread_from_dough_' + k, SHEET_BREAD)
		})

		//######################### New content #########################
		R.shaped(Item.of(i.rope_fence, 3), ['r/r', 'r/r'], { r: i.any_rope, '/': i.stick }, e, 'rope_fence')
		R.shaped(i.rope_fence_gate, ['/r/', '/r/'], { r: i.any_rope, '/': i.stick }, e, 'rope_fence_gate')
		// 1.3.4 recipe for the undyed Hanging Canvas Sign (no dyed variants)
		R.shaped(Item.of(i.hanging_canvas_sign, 6), ['c c', 'w#w', 'w#w'], { c: i.chain, w: i.any_log, '#': i.canvas }, e, 'hanging_canvas_sign')
		R.shapeless(i.gleaming_salad_block, [i.glow_berries, i.glow_berries, i.honey_bottle, i.tomato, i.golden_carrot,
			i.beetroot, i.farmersdelight_cabbage, i.farmersdelight_cabbage, i.bowl], e, 'gleaming_salad_block')
		// Slices from the placed Farmer's Delight pumpkin pie ([pumpkin_pie] placeAs) go back into a pie
		R.shaped(i.pumpkin_pie, ['##', '##'], { '#': i.fd_pumpkin_pie_slice }, e, 'pumpkin_pie_from_fd_slices')

		//######################### Kelp Encrusted Cod #########################
		// Ocean's Delight 1.0.4's dish in the pack namespace (TODO dishes/ocean #7; item: CD startup farmersdelight/ocean_dishes.js):
		// cod slice, any egg, kelp in the pot, 0.35 xp, 200 ticks as upstream
		R.fdCooking([i.cod_slice, i.eggs, i.kelp], i.kelp_encrusted_cod, e, 'kelp_encrusted_cod', 0.35, 200)

		//######################### Cabbage leaves #########################
		// A cabbage cuts into 4 leaves (TODO dishes/ingredients #25; upstream 2), on the board and in the Create mixer
		// (Slice & Dice copies the board recipe). The leaves-back-to-cabbage grid craft takes 4 to match (upstream 2).
		R.remove({ id: 'farmersdelight:cutting/cabbage' }, e)
		R.remove({ id: 'farmersdelight:integration/create/mixing/cabbage_slice_from_mixing' }, e)
		R.remove({ id: 'farmersdelight:cabbage_from_leaves' }, e)
		R.fdCutting(i.cabbage, i.any_knife, Item.of(i.cabbage_leaf, 4), e, 'cabbage_leaves')
		R.createMixing(Item.of(i.cabbage_leaf, 4), [i.cabbage], e, 'cabbage_leaves')
		R.shapeless(i.farmersdelight_cabbage, [i.cabbage_leaf, i.cabbage_leaf, i.cabbage_leaf, i.cabbage_leaf], e, 'cabbage_from_leaves')

		//######################### Ingredient changes #########################
		R.replaceInput({ id: 'farmersdelight:cooking/rabbit_stew' }, i.baked_potato, i.potato, e)
		R.replaceInput({ id: 'farmersdelight:cooking/vegetable_noodles' }, i.brown_mushroom, i.mushrooms, e)
		// 1.3.4 names #forge:raw_meat, which in this pack holds only two Miner's Delight items; the pack's raw meat
		// food type is the same idea (beef, pork, chicken, mutton, rabbit and the modded meats)
		R.replaceInput({ id: 'farmersdelight:cooking/dog_food' }, i.t_wolf_prey, i.t_food_raw_meat, e)
		// Cabbage Rolls: the filling tag is widened in farmersdelight/tags.js
		// Any raw pasta in every pasta recipe (TODO dishes/meals #38): recipes naming FD's raw pasta, by item or by
		// #forge:pasta/raw_pasta, take all of #forge:pasta (cucumber noodles, gnocchi: veggiesdelight/tags.js)
		R.replaceInput({ input: i.raw_pasta }, i.raw_pasta, i.pasta, e, true)
		R.replaceInput({ input: i.t_pasta_raw_pasta }, i.t_pasta_raw_pasta, i.pasta, e, true)

		//######################### Salvaging (cutting board, axe) #########################
		// One recipe per wood: every wooden furniture piece gives its plank at 75% (1.2.4: door, sign and trapdoor
		// only, 100%). Vanilla 1.19.2 woods; crimson and warped have no boat.
		var woods = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'crimson', 'warped']
		woods.forEach(w => {
			['door', 'sign', 'trapdoor'].forEach(p => R.remove({ id: 'farmersdelight:cutting/' + w + '_' + p }, e))
			var parts = ['door_', 'trapdoor_', 'sign_', 'fence_', 'fencegate_', 'pressureplate_', 'button_']
				.map(p => i[p + w]).concat([i[w + '_cabinet']])
			if (i[w + '_boat']) parts.push(i[w + '_boat'])
			R.fdCutting(Ingredient.of(parts), R.AXE, Item.of(i['planks_' + w]).withChance(0.75), e, 'salvaging/' + w + '_furniture')
		})
		woods.filter(w => i[w + '_chest_boat']).forEach(w =>
			R.fdCutting(i[w + '_chest_boat'], R.AXE, [i[w + '_boat'], i.chest], e, 'salvaging/' + w + '_chest_boat'))
		// Minecarts come apart with a hoe, as upstream lists it
		R.fdCutting(i.chest_minecart, i.any_hoe, [i.minecart, i.chest], e, 'salvaging/chest_minecart')
		R.fdCutting(i.furnace_minecart, i.any_hoe, [i.minecart, i.furnace], e, 'salvaging/furnace_minecart')
		R.fdCutting(i.hopper_minecart, i.any_hoe, [i.minecart, i.hopper], e, 'salvaging/hopper_minecart')
		R.fdCutting(i.tnt_minecart, i.any_hoe, [i.minecart, i.tnt], e, 'salvaging/tnt_minecart')
		// Abnormals Delight 6.0.3's salvaging (abnormals_delight.md 3.1): Boatload furnace boats come apart with a hoe,
		// as 6.0.3 lists them; chest boats with an axe like the vanilla ones above. Boatload's crimson and warped boats
		// join their wood's furniture. River wood is removed (removed_woods.js); hanging signs and rafts are 1.20.
		if (Platform.isLoaded('boatload')) {
			woods.filter(w => i[w + '_furnace_boat']).forEach(w => {
				var boat = i[w + '_boat'] || i['boatload_' + w + '_boat']
				R.fdCutting(i[w + '_furnace_boat'], i.any_hoe, [boat, i.furnace], e, 'salvaging/' + w + '_furnace_boat')
			})
			;['crimson', 'warped'].forEach(w => {
				R.fdCutting(i['boatload_' + w + '_boat'], R.AXE, Item.of(i['planks_' + w]).withChance(0.75), e, 'salvaging/' + w + '_boat')
				R.fdCutting(i['boatload_' + w + '_chest_boat'], R.AXE, [i['boatload_' + w + '_boat'], i.chest], e, 'salvaging/' + w + '_chest_boat')
			})
		}
		// Upgrade Aquatic's driftwood: one recipe replaces Abnormals Delight 4.1.2's door, sign and trapdoor (100%)
		if (Platform.isLoaded('upgrade_aquatic')) {
			['door', 'sign', 'trapdoor'].forEach(p => R.remove({ id: 'abnormals_delight:upgrade_aquatic/cutting/driftwood_' + p }, e))
			var driftwood = ['door_', 'trapdoor_', 'sign_', 'fence_', 'fencegate_', 'pressureplate_', 'button_']
				.map(p => i[p + 'driftwood']).concat([i.driftwood_boat])
			if (Platform.isLoaded('abnormals_delight')) driftwood.push(i.driftwood_cabinet)
			R.fdCutting(Ingredient.of(driftwood), R.AXE, Item.of(i.planks_driftwood).withChance(0.75), e, 'salvaging/driftwood_furniture')
			R.fdCutting(i.driftwood_chest_boat, R.AXE, [i.driftwood_boat, i.chest], e, 'salvaging/driftwood_chest_boat')
			if (Platform.isLoaded('boatload'))
				R.fdCutting(i.driftwood_furnace_boat, i.any_hoe, [i.driftwood_boat, i.furnace], e, 'salvaging/driftwood_furnace_boat')
		}

		// The rows below moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.

		//######################### Soups and bases #########################
		//Bone broth
		// Updated bone broth to accept bone meal + shapeless recipe
		R.remove({ id: 'farmersdelight:cooking/bone_broth' }, e)
		R.fdCooking([[i.bone, i.bonemeal], [i.glow_berries, i.mushrooms, i.hanging_roots, i.glow_lichen]], Item.of(i.bone_broth, 2), e, undefined, 1, 200, i.bowl)
		R.shapeless(Item.of(i.bone_broth, 1), [[i.bone, i.bonemeal], [i.glow_berries, i.hanging_roots, i.glow_lichen, i.mushrooms], i.bowl], e, 'bone_broth_manual_only')
		// Stews with bone broth (heatless meal rule: see "Meals" below; soupCraft adds the broth)
		F.soupCraft([i.cooked_beef, i.carrot, i.baked_potato], i.beef_stew)
		F.soupCraft([i.cooked_cod, i.cooked_eggs, i.baked_potato, i.tomato_sauce], i.baked_cod_stew)
		F.soupCraft([i.cooked_pork, i.cooked_eggs, i.pasta, i.dried_kelp], i.noodle_soup)
		// the raw carrot is gone (no cooked carrot); salad stays raw
		F.soupCraft([i.cooked_chicken, i.t_soup_veggie_cooked, i.salad_ingredients, i.t_soup_veggie_cooked], i.chicken_soup)
		R.shapeless(Item.of(i.cooked_rice, 1), [i.rice, i.bone_broth], e, 'cooked_rice_from_bone_broth_manual_only')

		//Cooked rice
		// Shapeless craft rice dishes w/ cooked_rice (Delightful's sinigang: delightful/recipes.js)
		F.mealCraft([i.cooked_rice, i.cooked_eggs, i.cooked_eggs, i.carrot, i.onion], i.fried_rice)
		F.mealCraft([i.cooked_rice, i.brown_mushroom, i.brown_mushroom, i.red_mushroom, i.baked_potato], i.mushroom_rice)

		//######################### Fluids #########################

		//Tomato sauce
		// Tomato sauce merge
		if (Platform.isLoaded('immersiveengineering') && Platform.isLoaded('create_central_kitchen')) {
			R.remove({ id: 'tmted:squeezer/tomato' }, e)
			R.remove({ id: 'tmted:bottling/tomato_sauce' }, e)
			R.ieSqueezing(Fluid.of(f.tomato_sauce, 250), i.tomato, e, undefined, undefined, 6400)
			R.ieBottling(i.tomato_sauce, f.t_tomato_sauce, i.bowl, e, undefined, 250)
		}
		//Expensive shapeless craft
		R.shapeless(Item.of(i.tomato_sauce, 1), [i.bowl, i.tomato, i.tomato, i.tomato, i.tomato], e)

		//Melon juice
		//melon juice merge
		if (Platform.isLoaded('create_central_kitchen')) {
			R.remove({ id: 'tmted:squeezer/melon_juice_from_slice' }, e)
			R.remove({ id: 'tmted:squeezer/melon_juice_from_block' }, e)
			R.remove({ id: 'tmted:bottling/melon_juice' }, e)
			R.remove({ id: 'create_central_kitchen:mixing/melon_juice' }, e)
			if (Platform.isLoaded('immersiveengineering')) {
				R.ieSqueezing(Fluid.of(f.melon_juice, 125), i.melon, e, undefined, undefined, 6400)
				R.ieSqueezing(Fluid.of(f.melon_juice, 1250), i.melon_block, e, undefined, undefined, 6400)
				R.ieBottling(i.melon_juice, f.t_melon_juice, i.glass_bottle, e, undefined, 250)
			}
			R.createMixing(Fluid.of(f.melon_juice, 250), [i.any_sweetener_drink, Item.of(i.melon, 3)], e, undefined, 'n/a')
		}

		//Apple cider
		// One chain on Central Kitchen's three cider fluids, run like a potion's (TODO drinks #3, item_merges.md 2.6).
		// Base: FD pot, FR kettle, CCK mixing (2 apples + sugar -> 250 mB, heated) and the IE Mixer below at the same cost.
		// Long (milk) and strong (honey), like redstone and glowstone: Create mixer 1000 mB + 1 reagent, heated; IE Mixer
		// 1500 mB + 1 reagent, 6400 RF/t. Both land on the machines' standard pages, not Automated Potion Mixing. The long
		// and strong rows are in farmersrespite/recipes.js.
		// tmted's cider went to a dead fluid and bottled from an empty tag.
		R.remove({ id: 'tmted:mixer/apple_cider' }, e)
		R.remove({ id: 'tmted:bottling/apple_cider' }, e)
		if (Platform.isLoaded('immersiveengineering') && Platform.isLoaded('create_central_kitchen')) {
			R.ieMixer(Fluid.of(f.apple_cider, 250), [Item.of(i.apple, 2), i.any_sweetener_drink], f.t_water, e, 'apple_cider')
			R.ieBottling(i.farmersdelight_apple_cider, f.t_apple_cider, i.glass_bottle, e, undefined, 250)
		}

		// Chorus juice: endersdelight/recipes.js. Veggies Delight juices: veggiesdelight/recipes.js

		//Egg solution
		// The pack's egg solution fluid: 250 mB stands in for one egg and its milk (create/recipes.js)
		if (Platform.isLoaded('create')) {
			R.createMixing(i.glow_berry_custard, [i.glow_berries, i.any_sweetener, i.glass_bottle, Fluid.of(f.egg_solution, 250)], e,
				'glow_berry_custard_egg_solution', 'heated')
		}

		//######################### Pasta #########################
		// More efficient press recipe for pasta
		if (Platform.isLoaded('immersiveengineering')) {
			R.remove({ id: 'tmted:metalpress/raw_pasta' }, e)
			R.ieMetalPress(Item.of(i.pasta, 2), i.dough, i.mold_wire, e)
		}

		//######################### Prepared #########################
		// Barbecue Skewer (barbecue_stick): raw skewer, cooking, heatless craft and assembly in barbequesdelight/skewers.js
		// (F.skewer, plan_meals_rolls_bbq.md C4)
		// Fish stew
		F.mealCraft([i.cooked_fish, i.cooked_fish, Item.of(i.tomato_sauce, 2), i.onion, i.bowl], i.fish_stew)

		// Cabbage rolls
		R.shapeless(Item.of(i.cabbage_rolls, 1), [i.cabbage, i.cabbage, i.cabbage_roll_veg, i.cabbage_roll_veg], e, 'cabbage_rolls_manual_only')

		// Sequenced assemblies (F.assemble skips them without Create)
		// Grilled salmon: FD's shapeless kept; sequenced assembly added
		F.assemble(i.bowl, [i.onion, i.cabbage, i.sweet_berries, i.cooked_salmon], i.grilled_salmon)
		// (Frying with cooking oil: pneumaticcraft/recipes.js)

		// Bacon and eggs: FD's shapeless kept; sequenced assembly added
		F.assemble(i.bowl, [i.cooked_bacon, i.cooked_eggs], i.bacon_and_eggs, 2)
		// Salmon roll, cod roll: FD's shapeless kept; sequenced assembly added
		F.assemble(i.cooked_rice, [i.salmon_slice], Item.of(i.salmon_roll, 3), 3)
		F.assemble(i.cooked_rice, [i.cod_slice], Item.of(i.cod_roll, 3), 3)
		F.assemble(i.cooked_rice, [i.carrot, i.dried_kelp, i.dried_kelp], i.kelp_roll)
		F.assemble(i.bowl, [i.cod_roll, i.salmon_roll, i.kelp_roll_slice], i.rice_medley, 2)

		//Pasta w/ mutton
		//Make cooked shapeless
		F.mealCraft([i.tomato_sauce, i.tomato_sauce, i.pasta, i.cooked_mutton, i.cooked_mutton, i.bowl], i.pasta_with_mutton)
		//Add bulk mixing
		if (Platform.isLoaded('create') && Platform.isLoaded('create_central_kitchen')) {
			R.createMixing(Item.of(i.pasta_with_mutton, 4), [Fluid.of(f.tomato_sauce, 1000), i.pasta, i.pasta, i.pasta, i.raw_mutton, i.raw_mutton, Item.of(i.bowl, 4)], e, undefined, 'heated')
		}

		//Roasted mutton chops
		// Sequenced assembly
		F.assemble(i.bowl, [i.tomato, i.beetroot, i.cooked_rice, i.cooked_mutton_chops], i.roasted_mutton_chops)

		//######################### Meals #########################
		// Heatless meal rule (TODO dishes/meals #1). A pot meal can also be crafted shapeless from the cooked form of each raw
		// ingredient: raw meat -> i.cooked_beef / cooked_pork / cooked_chicken / cooked_mutton / cooked_fish / cooked_meat;
		// potato -> i.baked_potato; tomato -> i.tomato_sauce (bowl, consumed); rice -> i.cooked_rice; egg -> i.cooked_eggs;
		// any veggie -> i.t_soup_veggie_cooked; raw pasta stays i.pasta. Soups and stews add i.bone_broth, which brings the
		// bowl (so no i.bowl next to it or next to tomato sauce). The grid route is the expensive one: at least one more main
		// ingredient than the pot, or half the pot's count. Ingredients with no cooked form (carrot, beetroot, mushrooms) stay
		// raw. Every one gets an id ending _manual_only: Create would otherwise run the shapeless recipe in an unheated basin,
		// and mechanical crafters run shaped meals that have an assembly. The other mods' rows are in their own folders
		// (corn_delight, endersdelight, quarkdelight, alexsdelight, moredelight, fruitsdelight, rusticdelight,
		// abnormals_delight, delightful); Brewin' and Chewin's stay in the pack's cooking.js.

		// Pot meals with no grid route yet
		F.soupCraft([i.carrot, i.baked_potato, i.beetroot, i.salad_ingredients, i.t_soup_veggie_cooked], i.vegetable_soup)
		F.mealCraft([i.carrot, i.brown_mushroom, i.pasta, i.salad_ingredients, i.t_soup_veggie_cooked, i.t_soup_veggie_cooked, i.bowl], i.vegetable_noodles)
		F.mealCraft([i.tomato_sauce, i.onion, i.beetroot, i.t_soup_veggie_cooked, i.t_soup_veggie_cooked], i.ratatouille)

		// Expensive grid routes for pot meals (TODO dishes/meals #4). Baked cod stew is in "Soups and bases".
		// The grid dumplings make 1 (the pot makes 2).
		F.soupCraft([i.pumpkin_slice, i.pumpkin_slice, i.salad_ingredients, i.cooked_pork, i.milk], i.pumpkin_soup)
		F.mealCraft([i.beef_patty, i.beef_patty, i.pasta, i.tomato_sauce], i.pasta_with_meatballs)
		F.mealCraft([i.any_dough, i.cabbage, i.onion, [i.cooked_meat, i.brown_mushroom]], i.dumplings)
		// Squid ink pasta: the grid, or ink + tomato sauce in a heated basin (2 a batch)
		F.mealCraft([i.cooked_fish, i.pasta, i.tomato_sauce, i.ink_sac, i.ink_sac], i.squid_ink_pasta)
		if (['create', 'create_enchantment_industry', 'create_central_kitchen'].every(m => Platform.isLoaded(m))) {
			R.createMixing(Item.of(i.squid_ink_pasta, 2), [Fluid.of(f.ink, 250), Fluid.of(f.tomato_sauce, 250),
				i.pasta, i.pasta, i.raw_fish, Item.of(i.bowl, 2)], e, 'squid_ink_pasta_mixing', 'heated')
		}

		// Veggies, Vintage and Fungi Delight grid-meal moves, heatless routes and wraps (by the rules in this file) are
		// Creative Delight QoL-5: kubejs/data/creativedelight/kubejs/<mod>/cooking.js, switch [qol] manualOnlyRoutes.

		// Dog food: two cooked meats for the pot's one raw; cooked rice brings the bowl (TODO dishes/meals #8)
		F.mealCraft([i.rotten_flesh, i.bonemeal, i.cooked_meat, i.cooked_meat, i.cooked_rice], i.dog_food)

		// Mutton wrap: a tortilla, not bread (FD's grid recipe and CCK's assembly both took bread). TODO dishes/meals #2
		R.remove({ id: 'farmersdelight:mutton_wrap' }, e)
		R.remove({ id: 'create_central_kitchen:sequenced_assembly/mutton_wrap' }, e)
		F.wrapAssemble([i.cooked_mutton, i.salad_ingredients, i.onion], i.mutton_wrap)

		// Fruit salad: any melon or fruit slice for the two melon slices (FD's and Delightful's recipes share the id)
		R.remove({ id: 'farmersdelight:fruit_salad' }, e)
		F.saladCraft([i.apple, i.t_fruit_salad_slice, i.t_fruit_salad_slice, i.t_berries, i.t_berries, i.pumpkin_slice], i.fruit_salad)
		F.assemble(i.bowl, [i.apple, i.t_fruit_salad_slice, i.t_fruit_salad_slice, i.t_berries, i.t_berries, i.pumpkin_slice],
			Item.of(i.fruit_salad, 2))

		// Burgers: creativedelight/recipes.js (one table over several mods)

		// Assembly meals (TODO dishes/meals #3)
		// Shepherd's pie: a cheese wedge in place of milk
		R.remove({ id: 'farmersdelight:shepherds_pie_block' }, e)
		R.shapeless(i.shepherds_pie_block, [i.baked_potato, i.baked_potato, i.t_cheese_wedge, i.cooked_mutton, i.cooked_mutton,
			i.cooked_mutton, i.onion, i.onion, i.bowl], e, 'shepherds_pie_manual_only')
		F.assemble(i.bowl, [i.cooked_mutton, i.cooked_mutton, i.cooked_mutton, i.onion, i.onion, i.baked_potato, i.baked_potato,
			i.t_cheese_wedge], i.shepherds_pie_block)
		// Stuffed potato: FD's shapeless stays
		F.assemble(i.baked_potato, [i.cooked_beef, { fill: Fluid.of(f.milk, 250) }], i.stuffed_potato)
		// Steak and potatoes: shaped
		R.remove({ id: 'farmersdelight:steak_and_potatoes' }, e)
		R.shaped(i.steak_and_potatoes, ['ROP', ' B ', ' W '], { R: i.cooked_rice, O: i.onion, P: i.baked_potato, B: i.cooked_beef, W: i.bowl },
			e, 'steak_and_potatoes_manual_only')
		F.assemble(i.bowl, [i.cooked_rice, i.baked_potato, i.cooked_beef, i.onion], i.steak_and_potatoes)

		//######################### Protein #########################
		//Egg
		// Abnormals Delight overrides Farmer's Delight's three fried egg recipes with copies that don't load when
		// Incubation is installed, which left only the cooking oil recipe. The egg cooking trio puts all three back
		// (Incubation's own fried egg recipes are removed in incubation/recipes.js).
		F.eggCook(i.minecraft_egg, i.farmersdelight_fried_egg)

		//Fish
		// Fish slice family (CD.FOOD: 6 slices and a bone meal; FD: 2), replacing FD's cuts (Alex's Delight's catfish:
		// alexsdelight/recipes.js)
		F.fishSlice(i.salmon, i.salmon_slice)
		F.fishSlice(i.cod, i.cod_slice)
	})
})
