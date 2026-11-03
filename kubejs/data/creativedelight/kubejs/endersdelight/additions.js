// requires: enders_additions
// Recipes and tags for Ender's Delight 1.1.4's content, rebuilt as enders_additions inside the End's Delight member (ids
// stay endersdelight:*; ends_delight.md 3.1, 3.2, 5.9; TODO largecontent/mod-additions #28). It registers whether or not
// the installed Ender's Delight 1.2.2 is loaded, so this file needs only enders_additions; the five dishes come in
// 1.2.2's Shulker Bowl, so their rows also need endersdelight. Pot recipes as 1.1.4, plus the pack's heatless grid routes
// and the void pepper grinding routes. Recipe ids are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('enders_additions')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Spice #########################
		// Crushed Void Pepper: knife 1 -> 2 (1.1.4); Create milling 2 + 30% one more (1.1.4) with the mortar mirror
		// (foodGrinding: 2.3 average -> 2 in the mortar). No crushing wheel route, as 1.1.4.
		R.fdCutting(i.voidpepper, i.any_knife, Item.of(i.crushed_voidpepper, 2), e, 'crushed_voidpepper')
		R.foodGrinding([Item.of(i.crushed_voidpepper, 2), Item.of(i.crushed_voidpepper, 1).withChance(0.3)], i.voidpepper, e,
			'voidpepper_crushed_voidpepper', { crushing: false })

		//######################### Meals #########################
		// Served in Ender's Delight 1.2.2's Shulker Bowl: only with that mod
		if (!global.CD.on('endersdelight')) return
		// Cooking pot, as 1.1.4: served in the Shulker Bowl, 1.0 xp, 200 ticks
		R.fdCooking([i.amberveil, i.crushed_voidpepper, i.ethereal_saffron, i.chorus_flower], i.amberveil_stew, e, 'amberveil_stew', 1.0, 200, i.shulker_bowl)
		R.fdCooking([i.amberveil, i.crushed_voidpepper, i.chorus_flower], i.amberveiled_curry, e, 'amberveiled_curry', 1.0, 200, i.shulker_bowl)
		R.fdCooking([i.amberveil, i.ethereal_saffron, i.chorus_flower], i.veil_of_flames_risotto, e, 'veil_of_flames_risotto', 1.0, 200, i.shulker_bowl)
		R.fdCooking([i.amberveil, i.ethereal_saffron, i.crushed_voidpepper, i.chicken, i.chicken_cuts, i.rice], i.chicken_curry, e,
			'chicken_curry', 1.0, 200, i.shulker_bowl)
		R.fdCooking([i.crushed_voidpepper, i.beef, i.milk, i.potato], i.steak_fries, e, 'steak_fries', 1.0, 200, i.shulker_bowl)

		// Heatless meal rule (farmersdelight/recipes.js "Meals"); like Ender's other stews (recipes.js) they come in the
		// Shulker Bowl rather than with bone broth. One more main ingredient than the pot; meat cooked, rice cooked,
		// potato baked; the End plants and the spice stay raw.
		R.shapeless(i.amberveil_stew, [i.amberveil, i.amberveil, i.crushed_voidpepper, i.ethereal_saffron, i.chorus_flower, i.shulker_bowl],
			e, 'amberveil_stew_cooked_manual_only')
		R.shapeless(i.amberveiled_curry, [i.amberveil, i.amberveil, i.crushed_voidpepper, i.chorus_flower, i.shulker_bowl],
			e, 'amberveiled_curry_cooked_manual_only')
		R.shapeless(i.veil_of_flames_risotto, [i.amberveil, i.amberveil, i.ethereal_saffron, i.chorus_flower, i.shulker_bowl],
			e, 'veil_of_flames_risotto_cooked_manual_only')
		R.shapeless(i.chicken_curry, [i.amberveil, i.ethereal_saffron, i.crushed_voidpepper, i.cooked_chicken, i.cooked_chicken, i.cooked_chicken,
			i.cooked_rice, i.shulker_bowl], e, 'chicken_curry_cooked_manual_only')
		R.shapeless(i.steak_fries, [i.crushed_voidpepper, i.cooked_beef, i.cooked_beef, i.milk, i.baked_potato, i.shulker_bowl],
			e, 'steak_fries_cooked_manual_only')
	})
})

ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('enders_additions')) return
	var t = global.tags
	var i = global.item

	// Void Pepper is the first spice (briefs/food_seasoning.md); saffron is not (owner, 2026-10-25)
	t.add(e, i.t_spices, [i.voidpepper, i.crushed_voidpepper])
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('enders_additions')) return
	var t = global.tags
	var b = global.blocks

	// Soils of the End plants: survive on both, grow slowly on end stone, faster when tilled.
	// Rates: config/enders_additions-common.toml [growth].
	t.add(e, b.t_end_plant_soil_slow, b.end_stone)
	if (Platform.isLoaded('endercrop')) t.add(e, b.t_end_plant_soil_fast, b.tilled_end_stone)
})
