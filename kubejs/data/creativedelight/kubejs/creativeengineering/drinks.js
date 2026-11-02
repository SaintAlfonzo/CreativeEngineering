// requires: farmersrespite
// Drink rules: juice, tea, latte and coffee (feature_writeup/creative_delight_drinks.md, owner 2026-11-01 and
// 2026-11-02; TODO food/drinks #15, folding #8, #10, merges/drinks #1, flowers #5). Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes). Items and fluids: CD kubejs_startup/creativeengineering/drinks.js.
// The Kettle is Creative Delight's patched one (farmersrespite/kettle.js header): two ingredients, one 250 mB unit of
// its base fluid, one result. Pot recipes that have a mixer route of their own are re-id'd _manual_only so Slice & Dice
// does not copy them into a second (bottle) mixer recipe.
//
// 1  Juices: pot 4 fruit + 1 sugar-like (glass bottle) -> 1; Kettle 2 fruit on water. Exceptions: dandelion, nautilus,
//    pickerelweed, sea grape. New juices also mix heated at the Kettle's cost.
// 2  Teas: standard (Kettle: juice tank + any tea leaf + one extra); exceptions (azalea, lavender, sea pickle, chorus
//    tea, chorus fruit milk tea, chorus fruit bubble tea, Builder's Tea). Heated mixing: the tea's cost + one more tea
//    leaf + one sugar-like (2.2), or + one more of its own ingredient where 2.3 says so.
// 3  Lattes: cloud latte, matcha latte, ice berry matcha.
// 4  Coffee: roasted beans only; Kettle flavours straight from the plain coffee tank; heated mixing for every coffee.
// Tags the rules read: creativeengineering/drink_tags.js.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH
	var on = m => global.CD.on(m)
	var leaf = i.t_respite_tea_leaves     // any tea leaf: green, yellow, black
	var sweet = i.any_sweetener_drink     // sugar-like
	var bottle = i.glass_bottle
	var kettle = (input, output, id, base, opts) => R.frBrewing(input, output, e, id, Object.assign({ base: base }, opts || {}))
	var pot = (input, output, id, container) => R.fdCooking(input, output, e, id, 0.35, 200, container || bottle)
	var mixHot = (output, input, id) => R.createMixing(output, input, e, id, 'heated')
	var mb = (fluid) => Fluid.of(fluid, 250)
	var x = (ing, n) => { var a = []; for (var k = 0; k < n; k++) a.push(ing); return a }

	global.CD.recipes(() => {

		//######################### 1. Juices #########################
		// [key, item, fluid (null: none), fruit, mod, upstream pot / Kettle recipes the rule replaces, mix (new juices)].
		// Grid recipes (Fruits Delight's, Farmer's Delight's melon juice, Ender's Delight's chorus juice) stay.
		var JUICES = [
			['bayberry', i.bayberry_soup, f.bayberry_juice, i.t_fruits_bayberry, 'fruitsdelight', ['farmersdelight:cooking/bayberry_soup']],
			['hamimelon', i.hamimelon_juice, f.hamimelon_juice, i.t_drink_fruits_hamimelon, 'fruitsdelight', []],
			['chorus', i.chorus_juice, f.chorus_juice, i.chorus_fruit, 'endersdelight', []],
			['lychee', i.lychee_juice, f.lychee_juice, i.t_fruits_lychee, 'fruitsdelight', [], true],
			['hawberry', i.hawberry_juice, f.hawberry_juice, i.t_fruits_hawberry, 'fruitsdelight', [], true],
			['kiwi', i.kiwi_juice, f.kiwi_juice, i.t_fruits_kiwi, 'fruitsdelight', []],
			['lemon', i.lemon_juice, f.lemon_juice, i.t_drink_fruits_lemon, 'fruitsdelight', []],
			['mango', i.mango_juice, f.mango_juice, i.t_fruits_mango, 'fruitsdelight', [], true],
			['mangosteen', i.mangosteen_juice, f.mangosteen_juice, i.t_fruits_mangosteen, 'fruitsdelight', [], true],
			['melon', i.melon_juice, f.melon_juice, i.melon, null, ['farmersrespite:brewing/melon_juice']],
			['orange', i.orange_juice, f.orange_juice, i.t_drink_fruits_orange, 'fruitsdelight', []],
			['peach', i.peach_juice, f.peach_juice, i.t_fruits_peach, 'fruitsdelight', [], true],
			['pear', i.pear_juice, f.pear_juice, i.t_fruits_pear, 'fruitsdelight', []],
			['carrot', i.carrot_juice, f.carrot_juice, i.carrot, 'veggiesdelight', []],
			// Turnip Water is renamed Turnip Juice and follows the rule
			['turnip', i.turnip_water, f.turnip_water, i.turnip, 'veggiesdelight', []],
		]
		JUICES.forEach(j => {
			if (j[4] && !on(j[4])) return
			j[5].forEach(id => R.remove({ id: id }, e))
			pot(x(j[3], 4).concat([sweet]), j[1], j[0] + '_juice_manual_only')
			kettle([j[3], j[3]], j[1], j[0] + '_juice', f.water)
			if (j[6]) {
				mixHot(mb(j[2]), [j[3], j[3], mb(f.water)], j[0] + '_juice')
				R.createBottle(j[1], j[2], e, j[0] + '_juice')
			}
		})

		// Dandelion: pot 2 dandelion leaves + 2 sugar cane; Kettle 1 + 1 on water
		if (on('veggiesdelight')) {
			pot([i.dandelion_leaf, i.dandelion_leaf, i.sugar_cane, i.sugar_cane], i.dandelion_juice, 'dandelion_juice_manual_only')
			kettle([i.dandelion_leaf, i.sugar_cane], i.dandelion_juice, 'dandelion_juice', f.water)
		}
		// Nautilus: the pot recipe is already the rule (kelp, apple, sweet berries, plated in the nautilus shell); Kettle:
		// kelp and sweet berries on water, poured into a nautilus shell
		if (on('oceanic_delight')) {
			kettle([i.kelp, i.sweet_berries], i.nautilus_juice, 'nautilus_juice', f.water, { container: i.nautilus_shell })
		}
		// Pickerelweed and sea grape: pot 2 sugar + 2 of the ingredient; Kettle 1 + 1 on water. Their fluids exist for the
		// Sea Pickle Tea's Kettle base. Upgrade Aquatic's pot recipe goes in abnormals_delight/upgrade_aquatic.js; Oceanic
		// Delight's sea grape grid recipe stays.
		var SEA = []
		if (on('abnormals_delight') && on('upgrade_aquatic')) SEA.push(['pickerelweed', i.pickerelweed_juice, f.pickerelweed_juice, i.t_pickerelweed])
		if (on('oceanic_delight')) SEA.push(['sea_grape', i.sea_grape_juice, f.sea_grape_juice, i.sea_grape])
		SEA.forEach(j => {
			pot([j[3], j[3], sweet, sweet], j[1], j[0] + '_juice_manual_only')
			kettle([j[3], sweet], j[1], j[0] + '_juice', f.water)
			R.createBottle(j[1], j[2], e, j[0] + '_juice')
		})

		//######################### 2.1, 2.2 Standard teas #########################
		// Kettle: 250 mB of the matching juice + any tea leaf + one extra (another fruit unless named). Heated mixing: the
		// same + one more tea leaf + one sugar-like -> 250 mB of the tea.
		// [key, tea item, tea fluid, juice fluid, extra, mod, old recipes replaced]
		var TEAS = [
			['kiwi', i.kiwi_tea, f.kiwi_tea, f.kiwi_juice, i.t_fruits_kiwi, 'fruitsdelight', []],
			['turnip', i.turnip_tea, f.turnip_tea, f.turnip_water, i.turnip, 'veggiesdelight', []],
			['bayberry', i.bayberry_tea, f.bayberry_tea, f.bayberry_juice, i.t_fruits_bayberry, 'fruitsdelight', []],
			['hamimelon', i.hamimelon_tea, f.hamimelon_tea, f.hamimelon_juice, i.t_drink_fruits_hamimelon, 'fruitsdelight', []],
			['carrot', i.carrot_tea, f.carrot_tea, f.carrot_juice, i.carrot, 'veggiesdelight', []],
			['lemon', i.lemon_tea, f.lemon_tea, f.lemon_juice, i.t_drink_fruits_lemon, 'fruitsdelight', []],
			['melon', i.melon_tea, f.melon_tea, f.melon_juice, i.melon, null, []],
			['orange', i.orange_tea, f.orange_tea, f.orange_juice, i.t_drink_fruits_orange, 'fruitsdelight', []],
			['pear', i.pear_tea, f.pear_tea, f.pear_juice, i.t_fruits_pear, 'fruitsdelight', []],
			// existing teas moving to the rule (their old Kettle recipes are gone from farmersrespite/kettle.js; Fruits
			// Delight's own mixer recipes are replaced; their pot recipes stay)
			['hawberry', i.hawberry_tea, f.hawberry_tea, f.hawberry_juice, i.t_fruits_hawberry, 'fruitsdelight', ['fruitsdelight:mixing/flowing_hawberry_tea']],
			['mango', i.mango_tea, f.mango_tea, f.mango_juice, i.t_fruits_mango, 'fruitsdelight', ['fruitsdelight:mixing/flowing_mango_tea']],
			['mangosteen', i.mangosteen_tea, f.mangosteen_tea, f.mangosteen_juice, i.t_fruits_mangosteen, 'fruitsdelight', ['fruitsdelight:mixing/flowing_mangosteen_tea']],
			['peach', i.peach_tea, f.peach_tea, f.peach_juice, i.t_fruits_peach, 'fruitsdelight', ['fruitsdelight:mixing/flowing_peach_tea']],
			['lychee_cherry', i.lychee_cherry_tea, f.lychee_cherry_tea, f.lychee_juice, i.spore_blossom, 'fruitsdelight', ['fruitsdelight:mixing/flowing_lychee_cherry_tea']],
			['dandelion', i.dandelion_tea, f.dandelion_tea, f.dandelion_juice, i.dandelion, 'veggiesdelight', ['farmersrespite:brewing/dandelion_tea']],
		]
		var NEW_TEA_FLUIDS = ['kiwi', 'turnip', 'bayberry', 'hamimelon', 'carrot', 'lemon', 'melon', 'orange', 'pear']
		TEAS.forEach(t => {
			if (t[5] && !on(t[5])) return
			t[6].forEach(id => R.remove({ id: id }, e))
			kettle([leaf, t[4]], t[1], t[0] + '_tea', t[3])
			mixHot(mb(t[2]), [mb(t[3]), leaf, leaf, t[4], sweet], t[0] + '_tea')
			if (NEW_TEA_FLUIDS.indexOf(t[0]) >= 0) R.createBottle(t[1], t[2], e, t[0] + '_tea')
		})

		//######################### 2.3 Tea exceptions #########################
		// Azalea (fixes the missing recipe, food/drinks #10): pot 2 flowering azalea + 2 azalea flowers; Kettle 1 + 1 on
		// green tea; heated mixing the Kettle cost + one more azalea flower. Delightful's two recipes need Ecologics.
		R.remove({ output: i.azalea_tea, mod: 'delightful' }, e)
		pot([i.t_flowering_azalea, i.t_flowering_azalea, i.t_azalea_flowers, i.t_azalea_flowers], i.azalea_tea, 'azalea_tea_manual_only')
		kettle([i.t_flowering_azalea, i.t_azalea_flowers], i.azalea_tea, 'azalea_tea', f.green_tea)
		mixHot(i.azalea_tea, [mb(f.green_tea), i.t_flowering_azalea, i.t_azalea_flowers, i.t_azalea_flowers, bottle], 'azalea_tea')

		// Lavender: pot 2 pink flowers + 2 lavender herbs (amethyst shard, lavender blossom leaves); Kettle 1 + 1 on green
		// tea; heated mixing the Kettle cost + one more herb -> 250 mB
		R.remove({ id: 'delightful:food/kettle/lavender_tea' }, e)
		pot([i.t_pink_flowers, i.t_pink_flowers, i.t_lavender_tea_herbs, i.t_lavender_tea_herbs], i.lavender_tea, 'lavender_tea_manual_only')
		kettle([i.t_pink_flowers, i.t_lavender_tea_herbs], i.lavender_tea, 'lavender_tea', f.green_tea)
		mixHot(mb(f.lavender_tea), [mb(f.green_tea), i.t_pink_flowers, i.t_lavender_tea_herbs, i.t_lavender_tea_herbs], 'lavender_tea')

		// Sea Pickle Tea (Hostile Delight's Sea Cucumber Tea): pot 3 sea pickles; Kettle 2 on sea grape or pickerelweed
		// juice; heated mixing the Kettle cost + one more sea pickle
		if (on('hostiledelight')) {
			R.remove({ id: 'hostiledelight:cucumbertearecp' }, e)
			pot(x(i.t_sea_pickles, 3), i.seacucumbertea, 'sea_pickle_tea_manual_only')
			SEA.forEach(j => {
				kettle([i.t_sea_pickles, i.t_sea_pickles], i.seacucumbertea, 'sea_pickle_tea_on_' + j[0], j[2])
				mixHot(i.seacucumbertea, [mb(j[2]), i.t_sea_pickles, i.t_sea_pickles, i.t_sea_pickles, bottle], 'sea_pickle_tea_on_' + j[0])
			})
		}

		// Chorus Tea (Delightful's Ender Nectar): pot the chorus juice recipe + an ender eye; Kettle chorus juice + any tea
		// leaf + ender eye; heated mixing that + one more tea leaf + one sugar-like -> 250 mB
		if (on('delightful') && on('endersdelight')) {
			R.remove({ id: 'delightful:food/cooking/ender_nectar' }, e)
			pot(x(i.chorus_fruit, 4).concat([sweet, i.ender_eye]), i.ender_nectar, 'chorus_tea_manual_only')
			kettle([leaf, i.ender_eye], i.ender_nectar, 'chorus_tea', f.chorus_juice)
			mixHot(mb(f.chorus_tea), [mb(f.chorus_juice), leaf, leaf, i.ender_eye, sweet], 'chorus_tea')
			R.createBottle(i.ender_nectar, f.chorus_tea, e, 'chorus_tea')
		}

		// Chorus Fruit Milk Tea: pot milk + chorus fruit, grain or dried chorus flower + one more dried chorus flower;
		// Kettle the same item on milk (farmersrespite/kettle.js); heated mixing milk + the item + tea leaf + sugar-like
		if (on('ends_delight')) {
			var chorusPick = [i.chorus_fruit, i.chorus_fruit_grain, i.dried_chorus_flower]
			R.remove({ id: 'ends_delight:food/chorus_fruit_milk_tea' }, e)
			pot([i.milk, chorusPick, i.dried_chorus_flower], i.chorus_fruit_milk_tea, 'chorus_fruit_milk_tea_manual_only')
			mixHot(mb(f.chorus_fruit_milk_tea), [mb(f.milk), chorusPick, leaf, sweet], 'chorus_fruit_milk_tea')
			// Chorus Fruit Bubble Tea (was Bubble Tea): the milk tea + 2 ender pearl grain in the heated mixer; the pot
			// recipe (milk tea craft with ender pearl grain) and the Kettle (milk tea tank + grain) stay
			mixHot(mb(f.chorus_fruit_bubble_tea), [mb(f.chorus_fruit_milk_tea), i.ender_pearl_grain, i.ender_pearl_grain], 'chorus_fruit_bubble_tea')
		}

		// Builder's Tea: pot water + 2 tea leaves + milk; the Kettle (water tank + tea leaf + milk) stays; heated mixing
		// water + tea leaf + milk + one more tea leaf + one sugar-like -> 250 mB
		pot([i.water_bottle, leaf, leaf, i.milk], i.builders_tea, 'builders_tea_manual_only')
		mixHot(mb(f.tea), [mb(f.water), leaf, leaf, i.milk, sweet], 'builders_tea')

		//######################### 3. Lattes #########################
		// Cloud latte: milk, cocoa beans and two bottled clouds (one is the container); its Slice & Dice copy follows
		if (on('quarkdelight')) {
			R.remove({ id: 'quarkdelight:cooking/cloud_latte' }, e)
			pot([i.milk, i.bottled_cloud, i.cocoa_beans], i.cloud_latte, 'cloud_latte', i.bottled_cloud)
		}
		if (on('delightful')) {
			// Matcha latte: honey is any sugar-like; pot milk + 2 matcha + sugar-like (Kettle: farmersrespite/kettle.js)
			pot([i.milk, i.matcha, i.matcha, sweet], i.matcha_latte, 'matcha_latte')
			// Ice Berry Matcha (was Berry Matcha Latte): the matcha latte + 2 ice + 2 berries, shapeless or in the
			// unheated mixer (delightful/recipes.js no longer adds its grid recipe)
			R.shapeless(i.berry_matcha_latte, [i.matcha_latte, i.t_ice_cubes, i.t_ice_cubes, i.t_fruits_berries, i.t_fruits_berries], e, 'berry_matcha_latte')
			R.createMixing(mb(f.berry_matcha_latte), [mb(f.matcha_latte), i.t_ice_cubes, i.t_ice_cubes, i.t_fruits_berries, i.t_fruits_berries], e, 'berry_matcha_latte', 'n/a')
		}

		//######################### 4. Coffee #########################
		if (on('rusticdelight')) {
			// Pot: Rustic's own recipes, already the rule's counts (3 beans, 6 for dark; + milk; + flavour), on roasted
			// beans only; chocolate takes cocoa or a chocolate bar, pumpkin a pumpkin or a slice
			var COFFEE_POTS = ['rusticdelight:coffee', 'rusticdelight:dark_coffee', 'rusticdelight:milk_coffee',
				'rusticdelight:honey_coffee', 'rusticdelight:chocolate_coffee', 'rusticdelight:syrup_coffee',
				'rusticdelight:cooking/pumpkin_coffee', 'rusticdelight:cooking/cherry_blossom_coffee'].map(id => ({ id: id }))
			R.replaceInput(COFFEE_POTS, i.t_coffee_ingredients, i.roasted_coffee_beans, e)
			R.replaceInput({ id: 'rusticdelight:chocolate_coffee' }, i.cocoa_beans, i.t_coffee_chocolate, e)
			R.replaceInput({ id: 'rusticdelight:cooking/pumpkin_coffee' }, i.pumpkin_slice, i.t_coffee_pumpkin, e)

			// Flavours: [key, item, fluid, additive]. Kettle straight from the plain coffee tank (milk + additive in one
			// brew; the step from the milk coffee tank is in farmersrespite/kettle.js)
			var FLAVOURS = [
				['honey', i.honey_coffee, f.honey_coffee, i.honey_bottle],
				['chocolate', i.chocolate_coffee, f.chocolate_coffee, i.t_coffee_chocolate],
				['syrup', i.syrup_coffee, f.syrup_coffee, i.t_syrup],
				['pumpkin', i.pumpkin_coffee, f.pumpkin_coffee, i.t_coffee_pumpkin],
				['cherry_blossom', i.cherry_blossom_coffee, f.cherry_blossom_coffee, i.t_cherry_blossom],
			]
			var beans = i.roasted_coffee_beans
			// Heated mixing (4.2): coffee and dark coffee are the Kettle cost + 3 roasted beans; milk coffee is coffee +
			// milk; a flavour branches off milk coffee or straight off coffee (+ milk), never off another flavour
			mixHot(mb(f.coffee), x(beans, 5).concat([mb(f.water)]), 'coffee')
			mixHot(mb(f.dark_coffee), x(beans, 5).concat([mb(f.coffee)]), 'dark_coffee')
			mixHot(mb(f.milk_coffee), [mb(f.coffee), mb(f.milk)], 'milk_coffee')
			FLAVOURS.forEach(c => {
				kettle([i.milk, c[3]], c[1], c[0] + '_coffee_from_coffee', f.coffee)
				mixHot(mb(c[2]), [mb(f.milk_coffee), c[3]], c[0] + '_coffee')
				mixHot(mb(c[2]), [mb(f.coffee), mb(f.milk), c[3]], c[0] + '_coffee_from_coffee')
			})
		}
	})
})
