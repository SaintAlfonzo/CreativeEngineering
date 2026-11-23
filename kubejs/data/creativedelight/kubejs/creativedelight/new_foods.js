// requires: creativedelight
// requires: farmersdelight
// Recipes of the foods and kitchen items of owner rulings S2 and F10 (items and placeholder art: Creative Delight startup
// creativedelight/new_foods.js). Bakeware tag: creativedelight/tags.js.
ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var R = global.NCH.forPack('creativedelight')
	var F = global.CD.food(e)
	var n = (id, count) => ({ id: id, count: count })

	global.CD.recipes(() => {
		// Plain Donut (baking/baked-goods #39): the cider donut's oven recipe with batter in place of the cider and the
		// egg (batter holds flour and egg; a batter use, dishes/ingredients #7) -> 2 on the baking sheet
		if (global.CD.on('rusticdelight'))
			R.ceBaking([i.batter, i.t_cooking_oil, i.t_salts, i.sugar], n(i.plain_donut, 2), e, 'plain_donut', F.ware.sheet)

		// Fruit scones (baking/baked-goods #40): the raspberry scone's recipe, butter + 2 fruit + sugar + batter -> 1
		if (global.CD.on('hearthandharvest') && global.CD.on('rusticdelight')) {
			var SCONES = [ // G5 begin
				[i.blueberry_scone, i.t_fruits_blueberry],
				[i.cranberry_scone, i.t_fruits_cranberry],
				[i.lemon_scone, i.t_fruits_lemon],
				[i.orange_scone, i.t_fruits_orange],
				[i.apple_scone, i.apple],
				[i.peach_scone, i.t_fruits_peach],
			] // G5 end
			if (global.has('vinery')) SCONES.push([i.cherry_scone, i.cherry])   // Vinery's cherry (Creative Brewing)
			SCONES.forEach(s => R.ceBaking([i.butter, F.slot(s[1], 2), i.sugar, i.batter], Item.of(s[0], 1), e,
				s[0].split(':')[1], F.ware.sheet))
		}

		// Muffins (baking/baked-goods #2, #32): the muffin family rule (oven 4, paper assembly 6)
		F.muffinBake(i.t_fruits_orange, i.orange_cranberry_muffin, i.t_fruits_cranberry)
		F.muffinBake(i.t_fruits_lemon, i.lemon_poppy_muffin, i.poppy)
		F.muffinBake(i.carrot, i.carrot_muffin)
		F.muffinBake(i.pumpkin_slice, i.pumpkin_muffin)
		if (global.CD.on('veggiesdelight')) F.muffinBake(i.zucchini, i.zucchini_muffin)

		// Oatmeal Raisin Cookie (baking/baked-goods #23): the oatmeal cookie with a grape for one of its raw oats -> 12
		if (global.CD.on('vintagedelight') && global.has('vinery'))
			R.ceBaking([i.oat_dough, F.SWEET, F.SWEET, i.raw_oats, [i.t_red_grapes, i.t_white_grapes]],
				Item.of(i.oatmeal_raisin_cookie, 12), e, 'oatmeal_raisin_cookie', F.ware.sheet)

		// Pickled Garlic (dishes/pickles-and-ferments #17): as Hearth & Harvest's pickled carrots, 5 garlic + salt in the
		// pickling jar
		R.vdPickling(i.pickled_garlic, [F.slot(i.t_crops_garlic, 5), i.t_salts], e, 'pickled_garlic')

		// Baking Dish (baking/baked-goods #30): 5 glass in a bowl shape (owner ruling: shaped from glass, not
		// hammered); never used up in the oven
		R.shaped(i.baking_dish, ['G G', 'GGG'], { G: i.glass }, e, 'baking_dish')

		// Boxed Raw Pasta (dishes/ingredients #8): 9 raw pasta <-> 1
		R.shapeless(i.boxed_raw_pasta, [Item.of(i.raw_pasta, 9)], e, 'boxed_raw_pasta')
		R.shapeless(Item.of(i.raw_pasta, 9), [i.boxed_raw_pasta], e, 'raw_pasta_from_boxed_raw_pasta')
	})
})
