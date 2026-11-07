// requires: creativedelight
// Vanilla food and kitchen recipe changes. Non-member file: recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Tools #########################
		// Vanilla's cobblestone smoker removed: the smoker comes from Quark's deepslate / blackstone furnace + 4 logs
		// (quark:building/crafting/furnaces/{deepslate,blackstone}_smoker, Variant Furnaces on). Same rule as the blast furnace.
		R.remove({ id: 'minecraft:smoker' }, e)
		// Bamboo bowl: the vanilla bowl shape in bamboo, 1 bowl (planks give 4, so bamboo isn't the cheap route).
		// TODO cooking/kitchen #10
		R.shaped(i.bowl, ['B B', ' B '], { B: i.bamboo }, e, 'bowl_from_bamboo')

		//######################### Sugar #########################
		// Every sugar slot takes a sweetener tag by kind of recipe (TODO dishes/ingredients #3; tags in creativeengineering/tags.js):
		//   brewing (keg, wine)   sugar, powdered sugar, honey bottle, honeycomb
		//   plain (not food)      sugar, powdered sugar: Botania, speed modules and upgrades, ethanol, potions, spider eye, blaze cake
		//   drinks                sugar, powdered sugar, honey bottle (no honeycomb)
		//   cooking (the rest)    sugar, powdered sugar, honey bottle, honeycomb (glycerol is no sugar since 2026-11-02)
		// The four passes are disjoint (a swapped slot holds a tag with sugar in it, so a later pass would match it again).
		// Drinks: the drink food type, plus fluid drinks and the two drinks with no food type, by recipe id. Recipes that need
		// real sugar (powdered sugar, sugar cube, powdered sugar bucket) are script-added on i.sugar, so no pass sees them.
		// The passes walk only the recipes loaded from data; scripts write the tags themselves.
		// G5 begin: recipe filters (ids, types, mods) handed only to R.replaceInput
		var BREWING = [{ type: 'vinery:wine_fermentation' }, { type: 'brewinandchewin:fermenting' }]
		var PLAIN = [
			{ mod: 'botania' }, { mod: 'prettypipes' }, { mod: 'securitycraft' },
			{ id: 'pneumaticcraft:speed_upgrade' }, { type: 'pneumaticcraft:thermo_plant' }, { id: 'createaddition:mixing/bioethanol' },
			{ id: 'create:compacting/blaze_cake' }, { output: 'minecraft:potion' }, { output: 'minecraft:fermented_spider_eye' },
		]
		var DRINK = [{ output: i.t_food_drink }].concat([
			'create_confectionery:hot_chocolate_recipe', 'tmted:mixer/apple_cider',
			'farmersdelight:cooking/hot_cocoa', 'farmersdelight:melon_juice', 'farmersrespite:brewing/hot_cocoa',
			'farmersrespite:brewing/melon_juice', 'fruitsdelight:mixing/flowing_bellini_cocktail',
		].concat(['chocolate', 'glowberry', 'pumpkin', 'strawberry', 'vanilla'].map(m => 'create_dd:mixing/flowing_' + m + '_milkshake'),
			['hawberry_tea', 'lychee_cherry_tea', 'mango_tea', 'mangosteen_tea', 'peach_tea', 'lemon_juice', 'orange_juice',
				'pear_juice'].map(d => 'fruitsdelight:mixing/flowing_' + d)).map(id => ({ id: id })))
		// G5 end
		var swap = (only, not, tag) => R.replaceInput(only.map(x => Object.assign(not.length ? { input: i.sugar, not: not } : { input: i.sugar }, x)),
			i.sugar, tag, e)
		swap(BREWING, [], i.any_sweetener_brewing)
		swap(PLAIN, BREWING, i.any_sweetener_plain)
		swap(DRINK, BREWING.concat(PLAIN), i.any_sweetener_drink)
		R.replaceInput({ input: i.sugar, not: BREWING.concat(PLAIN, DRINK) }, i.sugar, i.any_sweetener, e)

		//######################### Sweets #########################
		// Cakes, pies, cookies, pie crust and cake base: all in creativeengineering/baking.js (baking overhaul,
		// feature_writeup/oven.md 2.6).
		//Remove redundant pie
		R.remove({ id: 'create_central_kitchen:cutting/pumpkin_pie_slice' }, e)
		R.remove({ id: 'create_central_kitchen:cutting/pumpkin_pie_slice_using_deployer' }, e)
		R.remove({ id: 'create_central_kitchen:crafting/pumpkin_pie_from_slices' }, e)
	})
})
