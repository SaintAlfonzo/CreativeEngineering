// requires: creativedelight
// requires: moredelight
// requires: rusticdelight
// Diced potatoes merge (feature_writeup/item_merges.md 2.4). Moved from the pack's server_scripts/other/item_merges.js
// 2026-10-27, rows unchanged.

// Rustic Delight's potato -> slices stays (3 since dishes/ingredients #26, rusticdelight/recipes.js), More Delight's potato -> 1 diced goes. More Delight's dishes (mashed
// potatoes, potato salad, the four diced potatoes with ...) read #forge:sliced_potatoes, whose only member was the
// diced potatoes; a merged item is stripped from every tag, so the slices fill it (dishes/ingredients #26). The
// pack's frying recipes read #forge:sliced_potato.
ServerEvents.recipes(e => {
	if (!global.CD) return
	global.NCH.remove({ id: 'moredelight:cutting/diced_potatoes' }, e)
})

ServerEvents.tags('item', e => {
	if (!global.CD) return
	var i = global.item
	global.tags.add(e, i.t_sliced_potato, i.potato_slices)
	global.tags.add(e, i.t_sliced_potatoes, i.potato_slices)
})
