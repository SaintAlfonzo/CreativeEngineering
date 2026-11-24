// requires: creativedelight
// Smoothie and brain freeze tags (TODO sweets #8, #9; feature_writeup/smoothies.md sections 2 and 3). Items and the
// table: CD startup creativedelight/smoothies.js; recipes: creativedelight/smoothies.js.
//   #creativedelight:smoothies                 the 16 smoothies
//   #creativedelight:honey_drink_sound         drunk with the honey bottle sounds (mixin ItemStackHoneyDrinkMixin)
//   #creativedelight:berries/red, blue, purple Mixed Berry's slots by berry color (fruit.md section 4); the third
//   #creativedelight:berries/blue_or_purple    slot takes blue or purple
//   #creativecommon:brain_freeze               cold foods: Creative Common's BrainFreeze rolls on them (owner:
//                                              smoothies, ice cream, popsicles, shaved ice, cold milkshakes, Iced Berry
//                                              Matcha Latte, ice cubes; not Frozen Flesh, not the Ice Sickle)
//   #forge:fruits/cherry                       gets Vinery's cherry (it was empty)
ServerEvents.tags('item', e => {
	if (!global.CD || !global.smoothies) return
	var t = global.tags
	var i = global.item

	t.add(e, i.t_smoothies, global.smoothies.list.map(s => i[s.key]))
	t.add(e, i.t_honey_drink_sound, i.t_smoothies)

	t.add(e, i.t_fruits_cherry, i.cherry)
	t.add(e, i.t_berries_red, [i.raspberry, i.strawberries, i.sweet_berries, i.cranberry, i.hawberry, i.cherry])
	t.add(e, i.t_berries_blue, i.blueberry)
	t.add(e, i.t_berries_purple, [i.mulberry, i.bayberry])
	t.add(e, i.t_berries_blue_or_purple, [i.t_berries_blue, i.t_berries_purple])

	// Cold foods: the frozen food family (ice cream, popsicles, shaved ice; new frozen foods join with the family) and the
	// cold milk drinks (drink/milk without the hot chocolates)
	t.add(e, i.t_brain_freeze, [
		i.t_smoothies, i.t_food_family_frozen,
		i.vanilla_milkshake, i.chocolate_milkshake, i.strawberry_milkshake, i.banana_milkshake, i.mint_milkshake,
		i.adzuki_milkshake, i.matcha_milkshake, i.salmonberry_milkshake, i.mango_milkshake, i.chocolate_milk_bottle,
		i.berry_matcha_latte, i.ice_cubes,
	])
})

ServerEvents.tags('fluid', e => {
	if (!global.CD || !global.smoothies) return
	var f = global.fluid
	global.smoothies.list.forEach(s => {
		if (f[s.key]) global.tags.addFluid(e, 'forge:fluid/' + s.key, f[s.key])
	})
})
