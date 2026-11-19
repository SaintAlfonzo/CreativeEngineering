// requires: creativedelight
// requires: farmersdelight
// Pizza slices (TODO dishes/meals #5, plan_food_batch_1119.md C1, R2): a whole pizza is 8 slices. The Knife cut on the
// board gives 8, the placed block 2 a bite (the blocks' Java, by hand or knife) and its break loot 8/6/4/2 by bites
// left; 8 slices in a ring (center empty) craft one back. The mods' 4-slice cuts and 2x2 recrafts go. The Tropical
// Pizza's own recipe is the Vintage member's JSON (vintagedelight:tropical_pizza).
ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		// [whole, slice, the mod's cut and 2x2 recipe ids]
		var PIZZAS = [ // G5 begin (the rows only reach NCH calls, which skip absent mods' ids)
			[i.cheese_pizza, i.cheese_pizza_slice, ['vintagedelight:cutting/cheese_pizza_from_cutting', 'vintagedelight:cheese_pizza_from_slices']],
			[i.meat_pizza, i.meat_pizza_slice, ['vintagedelight:cutting/meat_pizza_from_cutting', 'vintagedelight:meat_pizza_from_slices']],
			[i.tropical_pizza, i.tropical_pizza_slice, []],
			[i.vegan_pizza, i.vegan_pizza_slice, ['veggiesdelight:cutting/vegan_pizza_slice', 'veggiesdelight:vegan_pizza_from_slices']],
			[i.pizza, i.pizza_slice, ['brewinandchewin:cutting/pizza', 'brewinandchewin:pizza_from_slices']],
		] // G5 end
		PIZZAS.forEach(p => {
			if (!p[0] || !Item.exists(p[0])) return
			var name = p[0].split(':')[1]
			p[2].forEach(id => R.remove({ id: id }, e))
			R.fdCutting(p[0], i.any_knife, Item.of(p[1], 8), e, name + '_slices')
			R.shaped(p[0], ['sss', 's s', 'sss'], { s: p[1] }, e, name + '_from_slices')
		})
	})
})
