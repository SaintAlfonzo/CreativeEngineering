// requires: moredelight
// requires: rusticdelight
// Diced potatoes merge (feature_writeup/item_merges.md 2.4). Moved from the pack's server_scripts/other/item_merges.js
// 2026-10-27, rows unchanged.

// Rustic Delight's potato -> 2 slices stays, More Delight's potato -> 1 diced goes. More Delight's dishes read
// #forge:sliced_potato (tag below).
ServerEvents.recipes(e => {
	if (!global.CD) return
	global.NCH.remove({ id: 'moredelight:cutting/diced_potatoes' }, e)
})

ServerEvents.tags('item', e => {
	if (!global.CD) return
	global.tags.add(e, global.item.t_sliced_potato, global.item.potato_slices)
})
