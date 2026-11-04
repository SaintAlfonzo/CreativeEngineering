// requires: farmersrespite
// Farmer's Respite Kettle recipes in the tank format (feature_writeup/farmers_respite.md 2.1, K8 to K10). Non-member
// file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). Needs Creative Delight's Kettle patch
// (mixin.farmersrespite): every brew names its base fluid, uses one 250 mB unit of it from the tank and makes one
// result. Brew time: Creative Delight [cooking] kettleBrewTicks (default 600 = 30 s; frBrewing writes no time); 2.0 read `brewingtime`, the recipes wrote
// `cookingtime`, so 2.0 also fell back to 200.
//
// 1. Every live Kettle recipe from the mods is rewritten: plain teas and juices on water; long and strong tiers on
//    the drink's fluid plus milk or honey; recipes that took milk (an ingredient or a milk-bottle container) on milk.
// 2. New Kettle drinks: Rustic Delight's coffees. The juice, tea and coffee rules (food/drinks #15) add the rest in
//    creativeengineering/drinks.js.
// 3. Spout / Item Drain pairs (glass bottle <-> 250 mB) for the Kettle drinks that had none; the Kettle reads these
//    pairs to pour and bottle its tank.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH
	var kettle = (removeId, input, output, id, base, opts) => {
		if (removeId) R.remove({ id: removeId }, e)
		R.frBrewing(input, output, e, id, Object.assign({ base: base }, opts || {}))
	}

	global.CD.recipes(() => {

		//######################### 1. Farmer's Respite's own #########################
		// The three coffees are purged (item_merges.md 2.8); gambler's tea is rusticdelight/coffee_merge.js's.
		kettle('farmersrespite:brewing/black_tea', [i.black_tea_leaves, i.black_tea_leaves], i.black_tea, 'black_tea', f.water)
		kettle('farmersrespite:brewing/green_tea', [i.green_tea_leaves, i.green_tea_leaves], i.green_tea, 'green_tea', f.water)
		kettle('farmersrespite:brewing/yellow_tea', [i.yellow_tea_leaves, i.yellow_tea_leaves], i.yellow_tea, 'yellow_tea', f.water)
		kettle('farmersrespite:brewing/rose_hip_tea', [i.rose_hips, i.rose_hips], i.rose_hip_tea, 'rose_hip_tea', f.water)
		// dandelion tea and melon juice: the drink rules (creativeengineering/drinks.js, food/drinks #15)
		kettle('farmersrespite:brewing/purulent_tea', [i.nether_wart, i.fermented_spider_eye], i.purulent_tea, 'purulent_tea', f.water)
		kettle('farmersrespite:brewing/apple_cider', [i.apple, i.any_sweetener_drink], i.farmersdelight_apple_cider, 'apple_cider', f.water)
		// 2.0 used a milk bottle as the container; the milk is now the base and the cocoa is bottled in glass
		kettle('farmersrespite:brewing/hot_cocoa', [i.cocoa_beans, i.any_sweetener_drink], i.hot_cocoa, 'hot_cocoa', f.milk)

		// Tiers: [tier item, base fluid] per drink. Long: + milk. Strong: + honey bottle (its bottle goes to the
		// leftover slots).
		var LONG = [
			[i.long_black_tea, f.black_tea], [i.long_green_tea, f.green_tea], [i.long_yellow_tea, f.yellow_tea],
			[i.long_rose_hip_tea, f.rose_hip_tea], [i.long_dandelion_tea, f.dandelion_tea], [i.long_purulent_tea, f.purulent_tea],
			[i.long_gamblers_tea, f.gamblers_tea], [i.long_apple_cider, f.apple_cider],
		]
		var STRONG = [
			[i.strong_black_tea, f.black_tea], [i.strong_green_tea, f.green_tea], [i.strong_yellow_tea, f.yellow_tea],
			[i.strong_rose_hip_tea, f.rose_hip_tea], [i.strong_purulent_tea, f.purulent_tea], [i.strong_gamblers_tea, f.gamblers_tea],
			[i.strong_apple_cider, f.apple_cider], [i.strong_hot_cocoa, f.cck_hot_cocoa], [i.strong_melon_juice, f.melon_juice],
		]
		LONG.forEach(t => {
			var id = t[0].split(':')[1]
			kettle('farmersrespite:brewing/' + id, [i.milk], t[0], id, t[1])
		})
		STRONG.forEach(t => {
			var id = t[0].split(':')[1]
			kettle('farmersrespite:brewing/' + id, [i.honey_bottle], t[0], id, t[1])
		})

		//######################### 1. Other mods' #########################
		// Builder's Tea, two ways as before: Central Kitchen's (tea leaves + milk on water, 2 bottles) and Delightful's
		// (tea leaves + leaves, milk-bottle container -> milk base)
		kettle('create_central_kitchen:brewing/builders_tea', [i.t_respite_tea_leaves, i.milk], Item.of(i.builders_tea, 2), 'builders_tea', f.water)
		kettle('create:fr/tea', [i.t_tea_leaves, i.t_leaves], i.builders_tea, 'builders_tea_from_leaves', f.milk)
		// lavender tea: the drink rules (creativeengineering/drinks.js)
		// matcha latte: honey is any sugar-like (food/drinks #15)
		kettle('delightful:food/kettle/matcha_latte', [i.matcha, i.any_sweetener_drink], i.matcha_latte, 'matcha_latte', f.milk)
		// the pack's berry latte (TODO food/drinks #5): berries on the Matcha Latte in the tank
		kettle(null, [i.t_fruits_berries], i.berry_matcha_latte, 'berry_matcha_latte', f.matcha_latte)
		kettle('ends_delight:brewing/chorus_flower_tea', [i.ghast_tear, i.dried_chorus_flower], i.chorus_flower_tea, 'chorus_flower_tea', f.water)
		// chorus fruit, its grain or a dried chorus flower (food/drinks #15)
		kettle('ends_delight:brewing/chorus_fruit_milk_tea', [[i.chorus_fruit, i.chorus_fruit_grain, i.dried_chorus_flower]], i.chorus_fruit_milk_tea, 'chorus_fruit_milk_tea', f.milk)
		kettle('ends_delight:brewing/bubble_tea', [i.ender_pearl_grain], i.bubble_tea, 'bubble_tea', f.chorus_fruit_milk_tea)

		//######################### 2. Rustic Delight's coffees #########################
		// Coffee from roasted beans on water, the rest from the Coffee or Milk Coffee in the tank (as the grid
		// recipes build on Coffee). Rustic's pot and grid recipes stay. Upstream's 2.1-format Kettle coffee (a fluid
		// result) can't load on 2.0 and is replaced here.
		if (global.CD.on('rusticdelight')) {
			R.remove({ id: 'rusticdelight:brewing/coffee' }, e)
			kettle(null, [i.roasted_coffee_beans, i.roasted_coffee_beans], i.coffee, 'coffee', f.water)
			kettle(null, [i.milk], i.milk_coffee, 'milk_coffee', f.coffee)
			kettle(null, [i.roasted_coffee_beans, i.roasted_coffee_beans], i.dark_coffee, 'dark_coffee', f.coffee)
			kettle(null, [i.honey_bottle], i.honey_coffee, 'honey_coffee', f.milk_coffee)
			kettle(null, [i.t_coffee_chocolate], i.chocolate_coffee, 'chocolate_coffee', f.milk_coffee)
			kettle(null, [i.t_syrup], i.syrup_coffee, 'syrup_coffee', f.milk_coffee)
			kettle(null, [i.t_coffee_pumpkin], i.pumpkin_coffee, 'pumpkin_coffee', f.milk_coffee)
			kettle(null, [i.t_cherry_blossom], i.cherry_blossom_coffee, 'cherry_blossom_coffee', f.milk_coffee)
		}

		//######################### 2. Fruits Delight's teas #########################
		// Moved to the standard tea rule 2026-11-02 (juice tank + any tea leaf + one extra): creativeengineering/drinks.js.
		// The same file adds the flavoured coffees straight from the plain coffee tank (milk + additive in one brew).

		//######################### 3. Spout and Item Drain pairs #########################
		var BOTTLES = [
			[i.lavender_tea, f.lavender_tea], [i.matcha_latte, f.matcha_latte], [i.berry_matcha_latte, f.berry_matcha_latte],
		]
		if (global.CD.on('rusticdelight')) {
			BOTTLES.push([i.coffee, f.coffee], [i.milk_coffee, f.milk_coffee], [i.honey_coffee, f.honey_coffee],
				[i.chocolate_coffee, f.chocolate_coffee], [i.dark_coffee, f.dark_coffee], [i.syrup_coffee, f.syrup_coffee],
				[i.pumpkin_coffee, f.pumpkin_coffee], [i.cherry_blossom_coffee, f.cherry_blossom_coffee])
		}
		BOTTLES.forEach(b => R.createBottle(b[0], b[1], e, b[0].split(':')[1]))
	})
})
