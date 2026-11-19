// requires: creativedelight
// requires: farmersdelight
// Cheese, not milk, in cheesy dishes (TODO dishes/meals #45, plan_food_batch_1119.md A1, R13): the mods' Cooking Pot
// recipes that used a milk for their cheese take any cheese (#forge:cheese: Brewin's flaxen wedge and Vintage Delight's
// cheese slice; Scarlet cheese is not in it). Slice & Dice's mixer copies follow the pot recipes. The pack's own grid
// routes are changed where they are made (veggiesdelight/cooking.js, fungidelight/cooking.js, moredelight/recipes.js,
// farmersdelight/recipes.js); the outputs' values went up for the dearer input (Creative Delight startup
// creativedelight/cheese_values.js). Mashed potatoes keep milk (owner ruling R13).
ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		var swap = ids => ids.forEach(id => R.replaceInput({ id: id }, i.milk, i.any_cheese, e))

		// Veggies Delight: broccoli soup and pasta with broccoli already have the mod's Vintage compat versions with
		// cheese beside the milk ones, so the milk ones go. The lasagna takes any cheese (baking/baked-goods #21).
		if (global.CD.on('veggiesdelight')) {
			R.remove({ id: 'veggiesdelight:cooking/broccoli_soup' }, e)
			R.remove({ id: 'veggiesdelight:cooking/pasta_with_broccoli' }, e)
			swap(['veggiesdelight:cooking/lasagna_pan'])
			if (global.has('moredelight')) swap(['veggiesdelight:compat/moredelight/cooking/creamy_pasta_with_chicken_cuts',
				'veggiesdelight:compat/moredelight/cooking/creamy_pasta_with_ham'])
		}
		if (global.has('corn_delight')) swap(['corn_delight:cooking/nachos_block'])
		swap(['farmersdelight:stuffed_potato'])
		if (global.CD.on('fungidelight')) swap(['fungidelight:cooking/truffle_pasta'])
		if (global.has('moredelight')) swap(['moredelight:cooking/creamy_pasta_with_chicken_cuts', 'moredelight:cooking/creamy_pasta_with_ham'])
		if (global.has('brewinandchewin')) swap(['brewinandchewin:cooking/fiery_fondue_pot'])
		// Not changed: Farmer's Delight's sweet berry cheesecake is the pack's Sweet Berry Pie (pie family, crust + 3 berries +
		// 2 sweet, no milk: creativedelight/baking.js), so it had no milk to swap (owner question, plan build notes).
	})
})
