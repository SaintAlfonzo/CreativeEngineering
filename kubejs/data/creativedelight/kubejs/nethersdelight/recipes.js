// requires: creativedelight
// requires: nethersdelight
// Nether's Delight recipe changes (Creative Delight member repackedNethers; dishes/meals #59): Roll
// Delight's Cooked Nether Wart joins the Nether's Delight nether wart dishes. Recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). The Nether Skewer stays as it is (barbequesdelight/skewers.js).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('nethersdelight')) return

	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		// Strider Moss Stew: nether wart in place of the second warped fungus in the Cooking Pot (upstream 200 ticks, 1.0 xp,
		// served in a bowl); Slice & Dice copies it to a heated mixer
		R.remove({ id: 'nethersdelight:cooking/strider_moss_stew' }, e)
		R.fdCooking([i.warped_fungus, i.crimson_fungus, i.crimson_roots, i.t_crops_nether_wart, i.t_raw_strider],
			i.strider_moss_stew, e, 'strider_moss_stew', 1.0, 200, i.bowl)

		if (!global.CD.on('roll_delight')) return
		// Warped Moldy Meat: Cooked Nether Wart in place of the bowl (it comes in one). Its bowl is carried into the dish:
		// no bowl handed back (consumeIngredient: no crafting remainder for that slot; plan_post_scratch_1115 G11)
		R.remove({ id: 'nethersdelight:warped_moldy_meat' }, e)
		R.shapeless(i.warped_moldy_meat, [i.warped_roots, i.warped_roots, i.hoglin_sirloin, i.cooked_nether_wart], e,
			'warped_moldy_meat').consumeIngredient(i.cooked_nether_wart)
		// Strider Moss Stew heatless (heatless meal rule, farmersdelight/recipes.js "Meals"): Cooked Nether Wart for the
		// pot's nether wart, bringing the bowl as bone broth does (the stew is served in it, so no bowl is handed back:
		// consumeIngredient; plan_post_scratch_1115 G11); one more strider than the pot (no cooked strider exists, so it stays
		// raw, as the fungi and roots do)
		// (an older Creative Delight jar's mealCraft returns nothing and keeps the bowl)
		var stew = F.mealCraft([i.cooked_nether_wart, i.warped_fungus, i.crimson_fungus, i.crimson_roots, i.t_raw_strider,
			i.t_raw_strider], i.strider_moss_stew)
		if (stew) stew.consumeIngredient(i.cooked_nether_wart)
	})
})
