// Pack-native food and cooking item tags: powdered sugar, cornmeal, bakeware (moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged). Shared tags are split by the mod owning each
// entry: the other entries are in that mod's tags.js beside this folder.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// sugar (glycerol: pneumaticcraft/tags.js)
	t.add(e, 'forge:sugar', i.powdered_sugar)

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
