// requires: creativedelight
// Vanilla items in the pack's food and cooking item tags (moved from the pack's server_scripts/tags/cookingtags.js
// 2026-10-25, rows unchanged). Shared tags are split by the mod owning each entry: the other entries are in that
// mod's tags.js beside this folder.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// ice cubes
	t.add(e, 'forge:ice_cubes', i.ice)

	// cooking oil: Rustic Delight only presses cotton seeds
	t.add(e, i.t_cooking_oil_ingredients, i.pumpkin_seeds)

	// cooked meat
	t.add(e, 'forge:cooked_meat', [
		i.beef_cooked,
		i.porkchop_cooked,
		i.chicken_cooked,
		i.mutton_cooked,
		i.cod_cooked,
		i.salmon_cooked,
		i.rabbit_cooked,
	])

	// meals (TODO dishes/meals #1, #2)
	// cooked veggies for heatless soups and stews: the cooked/veggie food types (food_types.json) minus snacks,
	// rice and kelp. Listed by key, not by the food type tags (the pack's tags/foodtypetags.js).
	t.add(e, i.t_soup_veggie_cooked, i.baked_potato)
	// exotic fruits bowl: Fruits Delight's fruits, its two melons and the vanilla melon slice
	t.add(e, i.t_exotic_fruit, i.melon)
	t.add(e, i.t_fruit_salad_slice, i.melon)
})
