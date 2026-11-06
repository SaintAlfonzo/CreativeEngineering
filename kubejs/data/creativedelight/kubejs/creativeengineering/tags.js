// Pack-native food and cooking item tags: powdered sugar, cornmeal, bakeware (moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged). Shared tags are split by the mod owning each
// entry: the other entries are in that mod's tags.js beside this folder.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// sugar (glycerol is not one: pneumaticcraft/tags.js)
	t.add(e, 'forge:sugar', i.powdered_sugar)
	// sweeteners: what a sugar slot takes, by kind of recipe (TODO dishes/ingredients #3; the swaps are in minecraft/recipes.js).
	// Cooking: everything. Drinks: no honeycomb. Keg and wine brewing: everything. Non-food uses: sugar and
	// powdered sugar. Honeycomb and the honey bottle stay out of #forge:sugar, so cans and the strict recipes do not take them.
	t.add(e, i.any_sweetener, [i.any_sugar, i.honey_bottle, i.honeycomb])
	t.add(e, i.any_sweetener_drink, [i.sugar, i.powdered_sugar, i.honey_bottle])
	t.add(e, i.any_sweetener_brewing, [i.sugar, i.powdered_sugar, i.honey_bottle, i.honeycomb])
	t.add(e, i.any_sweetener_plain, [i.sugar, i.powdered_sugar])

	// cornmeal: its own flour tag, plus the "flour or cornmeal" tag pancakes, pies and cakes take (TODO dishes/ingredients #1)
	t.add(e, i.t_flour_corn, i.cornmeal)
	t.add(e, i.t_flour_or_cornmeal, [i.flour, i.t_flour_corn])

	// bakeware: one tag per kind (what a baking recipe's `tool` names) + the oven's tool-slot filter
	t.add(e, i.t_bakeware_baking_sheet, i.baking_sheet)
	t.add(e, i.t_bakeware_cake_pan, i.cake_pan)
	t.add(e, i.t_bakeware_pie_tin, i.pie_tin)
	t.add(e, i.t_bakeware_muffin_tin, i.muffin_tin)
	t.add(e, i.t_bakeware, [
		i.t_bakeware_baking_sheet, i.t_bakeware_cake_pan, i.t_bakeware_pie_tin, i.t_bakeware_muffin_tin,
	])
})
