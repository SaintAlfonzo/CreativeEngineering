// requires: creativedelight
// requires: hearthandharvest
// Recipes for Hearth and Harvest content ported into the Creative Delight member hearthandharvest (hearth_and_harvest.md
// 4.0, 4.1). Recipe ids are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		// Raspberry Pips, the bush's planting item: 1 Raspberry -> 1 Pips (1.4.1 with Berry Good, shapeless)
		R.shapeless(i.raspberry_pips, [i.raspberry], e, 'raspberry_pips_from_raspberry')

		// Watering cans (4.0): copper is 1.4.1's recipe (5 copper ingots around a bucket). The tiers are smithing upgrades,
		// which keep the can's water and fertiliser (vanilla smithing copies the base's NBT).
		R.shaped(Item.of(i.watering_can), ['C  ', 'CBC', 'CC '], {C: i.ingot_copper, B: i.bucket}, e, 'watering_can')
		R.smithing(i.iron_watering_can, i.watering_can, i.block_iron, e, 'iron_watering_can')
		R.smithing(i.golden_watering_can, i.iron_watering_can, i.block_gold, e, 'golden_watering_can')
		R.smithing(i.diamond_watering_can, i.golden_watering_can, i.diamond, e, 'diamond_watering_can')
		R.smithing(i.netherite_watering_can, i.diamond_watering_can, i.ingot_netherite, e, 'netherite_watering_can')
		R.smithing(i.dragon_watering_can, i.netherite_watering_can, i.dragon_scale, e, 'dragon_watering_can')

		// Kept content (hearth_and_harvest.md 3.1, 4.2 steps 4 and 10). Inputs retargeted to the pack's survivors. Keg pouring
		// and the fluid routes are not here yet.
		var F = global.CD.food(e)
		var f = global.fluid
		var n = (id, count) => Item.of(id, count)  // items only; a counted tag slot is F.slot (Item.of(tag, n) is the first item)
		var SHEET = { exp: 0.35, time: 200, tool: i.t_bakeware_baking_sheet }
		var grape = [i.t_red_grapes, i.t_white_grapes]

		// Dairy
		R.shapeless(n(i.butter, 2), [i.t_salts, i.milk], e, 'butter')
		R.shapeless(i.chocolate_milk_bottle, [i.farmersdelight_milk_bottle, i.cocoa_beans, i.sugar], e, 'chocolate_milk_bottle')
		R.shapeless(i.brine_bottle, [i.water_bottle, i.t_salts], e, 'brine_bottle_from_water_bottle')
		R.bncFermenting(i.unripe_goat_cheese_wheel, [i.t_salts], f.goat_milk, e, 'unripe_goat_cheese_wheel', { temperature: 4 })
		R.fdCutting(i.goat_cheese_wheel, i.any_knife, n(i.goat_cheese_slice, 4), e, 'goat_cheese_wheel')

		// Fruit and pickles
		F.fruitCook(i.apple, i.baked_apple)
		F.fruitCook(i.t_red_grapes, i.raisins)
		F.fruitCook(i.t_white_grapes, i.raisins)
		R.fdCutting(i.sunflower, i.any_knife, n(i.sunflower_seeds, 2), e, 'sunflower_seeds')
		R.vdPickling(i.pickled_carrots, [F.slot(i.carrot, 5), i.t_salts], e, 'pickled_carrots')
		R.vdPickling(i.pickled_potatoes, [F.slot(i.potato, 5), i.t_salts], e, 'pickled_potatoes')

		// Oven (cookies, pies, pastry)
		F.cookieBake(i.raisins, i.raisin_cookie)
		F.cookieBake(i.nut_mash_jar, i.peanut_butter_cookie)
		F.pieBake(i.t_fruits_raspberries, i.raspberry_pie)
		R.ceBaking([i.pie_crust, grape, grape, grape, F.SWEET, F.SWEET], i.grape_pie, e, 'grape_pie', F.ware.pie)
		F.pieBake(i.peanut, i.peanut_butter_pie)
		F.pieSlice(i.raspberry_pie, i.raspberry_pie_slice)
		F.pieSlice(i.grape_pie, i.grape_pie_slice)
		F.pieSlice(i.peanut_butter_pie, i.peanut_butter_pie_slice)
		R.ceBaking([i.any_dough, i.butter], i.croissant, e, 'croissant', SHEET)
		R.ceBaking([i.butter, F.slot(i.t_fruits_raspberries, 2), i.sugar, i.batter], i.raspberry_scone, e, 'raspberry_scone', SHEET)
		R.ceBaking([i.farmersdelight_apple_cider, i.cooking_oil, i.t_salts, i.eggs, i.sugar], n(i.cider_donut, 2), e, 'cider_donut', SHEET)

		// Cooking Pot
		R.fdCooking([i.corn_seeds, i.dye_orange, i.syrup, i.sugar], n(i.candy_corn, 4), e, 'candy_corn', 0.35, 100)
		R.fdCooking([i.batter, i.syrup], i.hh_waffle, e, 'waffle', 0.35, 100)
		R.fdCooking([i.grilled_corn, i.blaze_powder, i.any_cheese], i.street_corn, e, 'street_corn', 0.35, 100)
		R.fdCooking([i.any_dough, i.raw_sausage, i.t_salts, i.milk], i.biscuits_and_gravy, e, 'biscuits_and_gravy', 1.0, 200, i.bowl)
		R.fdCooking([i.raw_pasta, i.t_salts, i.milk, i.butter, i.any_cheese], i.macaroni_and_cheese, e, 'macaroni_and_cheese', 1.0, 200, i.bowl)
		var gcCarrot = Platform.isLoaded('miners_delight') ? [i.carrot, i.cave_carrot] : i.carrot   // dishes/meals #64
		R.fdCooking([gcCarrot, gcCarrot, i.syrup], i.glazed_carrots, e, 'glazed_carrots', 1.0, 200, i.bowl)
		R.fdCooking([i.t_food_cooked_meat, i.vegetables, i.any_cheese], i.tamale, e, 'tamale', 1.0, 200, i.corn_husk)

		// By hand
		F.saladCraft([i.hops, i.hops, i.cooking_oil, i.t_salts], i.hops_salad)
		R.shapeless(i.hot_dog, [i.cooked_sausage, i.bread], e, 'hot_dog')
		R.shapeless(i.trail_mix, [i.roasted_peanut, i.raisins, i.cocoa_beans, i.t_seeds], e, 'trail_mix')
		R.shapeless(n(i.paper, 3), [n(i.corn_husk, 3)], e, 'paper_from_corn_husks')
		R.shapeless(n(i.canvas, 2), [n(i.corn_husk, 4)], e, 'canvas_from_corn_husks')

		// Storage: nine items to a block and back
		var storage = [
			[i.corn_husk, i.corn_husk_bundle], [i.hops, i.hops_bag], [i.manure, i.manure_bag], [i.fd_rope, i.rope_coil],
			[i.string, i.hh_spool], [i.crimson_fungus, i.crimson_fungus_crate], [i.warped_fungus, i.warped_fungus_crate],
			[i.glistering_melon_slice, i.glistering_melon_crate], [i.poisonous_potato, i.poisonous_potato_crate],
			[i.rotten_tomato, i.rotten_tomato_crate]
		]
		storage.forEach(p => {
			var name = p[1].split(':')[1]
			R.shaped(p[1], ['###', '###', '###'], { '#': p[0] }, e, name)
			R.shapeless(n(p[0], 9), [p[1]], e, name + '_unpack')
		})

		// Rugs
		R.shapeless(n(i.hay_rug, 4), [i.hay_bale, i.hay_bale], e, 'hay_rug')
		R.shapeless(n(i.straw_rug, 4), [i.straw_bale, i.straw_bale], e, 'straw_rug')

		// Half cabinets, FD's cabinet split and rejoined (9 woods; cherry, bamboo and palm dropped, N17)
		;['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'crimson', 'warped'].forEach(w => {
			var half = i[w + '_half_cabinet']
			var whole = 'farmersdelight:' + w + '_cabinet'
			R.shapeless(n(half, 2), [whole, whole], e, w + '_cabinet_into_halves')
			R.shapeless(whole, [half, half], e, w + '_cabinet_from_halves')
		})

		// Mums: a crafting route to one dye (N23-2) and upstream's cutting board route to two
		;['white', 'red', 'orange', 'yellow', 'light_blue', 'blue', 'purple', 'pink'].forEach(c => {
			var mum = i[c + '_mum']
			R.shapeless(i['dye_' + c], [mum], e, c + '_dye_from_mum')
			R.fdCutting(mum, i.any_knife, n(i['dye_' + c], 2), e, c + '_mum')
		})

		// Cleavers and sausage (tranche 2, 4.4). A cleaver is damaged by the craft, not used up.
		var cleaverTier = { flint: i.flint, iron: i.ingot_iron, golden: i.ingot_gold, diamond: i.diamond }
		Object.keys(cleaverTier).forEach(t => {
			R.shaped(i[t + '_cleaver'], ['  #', ' # ', 'S  '], { '#': cleaverTier[t], S: i.stick }, e, t + '_cleaver')
		})
		R.smithing(i.netherite_cleaver, i.diamond_cleaver, i.ingot_netherite, e, 'netherite_cleaver')
		;[[i.beef, 3, 'beef'], [i.porkchop, 2, 'porkchop'], [i.mutton, 2, 'mutton'], [i.chicken, 1, 'chicken'], [i.rabbit, 1, 'rabbit']].forEach(m => {
			R.shapeless(n(i.raw_sausage, m[1]), [i.t_cleavers, m[0]], e, 'sausage_from_' + m[2])
		})
		F.fruitCook(i.raw_sausage, i.cooked_sausage)
		R.shaped(i.pitchfork, ['nnn', 'isi', ' s '], { n: i.nugget_iron, i: i.ingot_iron, s: i.stick }, e, 'pitchfork')
		// Iron or wooden bucket (largecontent/mod-additions #58)
		R.shaped(i.tree_tapper, [' I ', 'SBS', ' S '], { I: i.ingot_iron, S: i.wood_slab, B: [i.bucket, i.wooden_bucket] }, e, 'tree_tapper')

		// Juices on the Apple Press (1 fruit + glass bottle) and wines in the Fermentation Barrel (3 fruit + 15 juice + wine
		// bottle), N22. The other drinks are Brewin' keg recipes, as upstream.
		;[[i.blueberry, i.blueberry_juice], [i.t_fruits_raspberries, i.raspberry_juice], [i.cherry, i.cherry_juice],
			[i.sweet_berries, i.sweet_berry_juice], [i.glow_berries, i.glow_berry_juice]].forEach(p => {
			R.vineryPressing(p[1], p[0], i.glass_bottle, e, p[1].split(':')[1])
		})
		var RED = { type: 'red_general', amount: 15 }, WHITE = { type: 'white_general', amount: 15 }
		;[[i.blueberry, i.blueberry_wine, RED], [i.t_fruits_raspberries, i.raspberry_wine, RED], [i.sweet_berries, i.sweet_berry_wine, RED],
			[i.glow_berries, i.glow_berry_wine, WHITE], [i.melon, i.melon_wine, WHITE], [i.lemon, i.lemon_wine, WHITE]].forEach(p => {
			R.vineryFermenting(p[1], [p[0], p[0], p[0]], p[2], e, p[1].split(':')[1])
		})
		R.bncFermenting(i.root_beer, [i.hanging_roots, i.hanging_roots, i.sugar], f.water, e, 'root_beer')
		R.bncFermenting(i.moonshine, [i.cornmeal, i.cornmeal, i.sugar], f.water, e, 'moonshine')

		// Corn husk grinds to 0.2 cornmeal (N23-1); its sources are in corn_delight/recipes.js, creativedelight/recipes.js
		// and the corn harvest loot modifier.
		R.foodGrinding([Item.of(i.cornmeal, 1).withChance(0.2)], i.corn_husk, e, 'corn_husk_cornmeal', { mortar: false })

		// Gear tranche (4.6). Fertilizer Bag: 4 bone meal + 4 manure + binder, 8 uses (N24-3).
		R.shaped(n(i.horseshoe, 2), ['n n', 'n n', 'nnn'], { n: i.nugget_iron }, e, 'horseshoe')
		R.shaped(i.farmers_hat, ['wbw'], { w: i.wheat, b: i.hay_bale }, e, 'farmers_hat')
		R.shapeless(i.fertilizer_bag, [i.bonemeal, i.bonemeal, i.bonemeal, i.bonemeal, i.manure, i.manure, i.manure, i.manure, [i.tree_bark, i.corn_husk, i.straw]], e, 'fertilizer_bag')
		R.shapeless(i.sugar_horse_treat, [i.sugar, i.honey_bottle], e, 'sugar_horse_treat')
		R.shapeless(i.blueberry_pips, [i.blueberry], e, 'blueberry_pips_from_blueberry')

		// Manure as fertilizer (D26): IE Garden Cloche at bone meal's 1.25, and FD Organic Compost as its tree bark recipe with
		// the 2 bone meal swapped for 2 manure.
		R.ieFertilizer(i.manure, 1.25, e, 'manure')
		R.shapeless(i.compost, [i.dirt, i.straw, i.straw, i.manure, i.manure, i.tree_bark, i.tree_bark, i.tree_bark, i.tree_bark], e, 'organic_compost_from_manure')

		// Fluid routes (step 9). Bottle both ways on Create (Spout and Item Drain, 250 mB); juices also from Create compacting
		// (4 fruit to 500 mB, upstream). No Create mixing of wines (3.1: skips the fermentation).
		;[[i.blueberry_juice, f.blueberry_juice, i.blueberry], [i.raspberry_juice, f.raspberry_juice, i.t_fruits_raspberries],
			[i.cherry_juice, f.cherry_juice, i.cherry], [i.sweet_berry_juice, f.sweet_berry_juice, i.sweet_berries],
			[i.glow_berry_juice, f.glow_berry_juice, i.glow_berries]].forEach(p => {
			var id = p[0].split(':')[1]
			R.createBottle(p[0], p[1], e, id)
			R.createCompacting(Fluid.of(p[1], 500), [p[2], p[2], p[2], p[2]], e, id + '_compacting')  // not F.slot: only the cooking helpers read it
		})
		;[[i.blueberry_wine, f.blueberry_wine], [i.raspberry_wine, f.raspberry_wine], [i.sweet_berry_wine, f.sweet_berry_wine],
			[i.glow_berry_wine, f.glow_berry_wine], [i.melon_wine, f.melon_wine], [i.lemon_wine, f.lemon_wine],
			[i.root_beer, f.root_beer], [i.moonshine, f.moonshine], [i.goat_milk_bottle, f.goat_milk], [i.brine_bottle, f.brine]].forEach(p => {
			R.createBottle(p[0], p[1], e, p[0].split(':')[1])
		})

		// Brine (N26-1, for the eval): salt into water on the keg and in a Create mixer; the salt is the pack's.
		R.bncFermenting(i.brine_bottle, [i.t_salts], f.water, e, 'brine_bottle_keg')
		R.createMixing(Fluid.of(f.brine, 250), [i.t_salts, Fluid.of(f.water, 250)], e, 'brine_mixing')

		// Sap to Rustic Delight syrup on a Create mixer, heated, and syrup to sugar (3.1, D4).
		R.createMixing(i.syrup, [Fluid.of(f.sap, 1000), i.glass_bottle], e, 'syrup_from_sap', 'heated')
		R.shapeless(n(i.sugar, 4), [i.syrup], e, 'sugar_from_syrup')

		// Manure building blocks
		R.shaped(i.manure_block, ['##', '##'], { '#': i.manure }, e, 'manure_block')
		R.shapeless(n(i.wet_manure_brick, 2), [i.straw, i.manure], e, 'wet_manure_bricks_from_straw')
		R.smelting(i.manure_brick, i.wet_manure_brick, e, 'manure_brick', 0.5, 400)
		R.smelting(i.polished_manure, i.manure_block, e, 'polished_manure', 0.5, 400)
		R.shaped(i.manure_bricks, ['##', '##'], { '#': i.manure_brick }, e, 'manure_bricks')
		var family = [['manure_brick', i.manure_bricks], ['polished_manure', i.polished_manure]]
		family.forEach(p => {
			var base = p[0], block = p[1]
			R.shaped(n(i[base + '_slab'], 6), ['###'], { '#': block }, e, base + '_slab')
			R.shaped(n(i[base + '_stairs'], 4), ['#  ', '## ', '###'], { '#': block }, e, base + '_stairs')
			R.shaped(n(i[base + '_wall'], 6), ['###', '###'], { '#': block }, e, base + '_wall')
			R.stonecutting(n(i[base + '_slab'], 2), block, e, base + '_slab_stonecutting')
			R.stonecutting(i[base + '_stairs'], block, e, base + '_stairs_stonecutting')
			R.stonecutting(i[base + '_wall'], block, e, base + '_wall_stonecutting')
		})
		R.stonecutting(i.polished_manure, i.manure_bricks, e, 'polished_manure_from_bricks_stonecutting')
	})
})
