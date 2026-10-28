// requires: fruitsdelight
// Fruits Delight 1.1.3 tag changes on the installed 0.5.11 (fruits_delight.md 2.5).

var FRUITS_CRATES = ['bayberry', 'blueberry', 'cranberry', 'fig', 'hawberry', 'kiwi', 'lemon', 'lychee', 'mango',
	'mangosteen', 'orange', 'peach', 'pear', 'persimmon', 'pineapple']

// The 16 fruit crates are storage blocks (block and item; the crates' ids are the same for both)
function fruitsCrateTags(e) {
	var i = global.item
	var t = global.tags
	t.storageBlock(e, 'apple', i.fruitsdelight_apple_crate, { tool: null })
	FRUITS_CRATES.forEach(fruit => t.storageBlock(e, fruit, i[fruit + '_crate'], { tool: null }))
}

ServerEvents.tags('item', e => {
	// the 22 jam bottles
	global.tags.add(e, global.item.t_forge_jams, global.item.t_fruitsdelight_jelly)
	fruitsCrateTags(e)
})

// Fruits Delight items in the pack's food item tags (moved from the pack's server_scripts/tags/cookingtags.js
// 2026-10-25, rows unchanged; split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item
	// exotic fruits bowl: Fruits Delight's fruits, its two melons and the vanilla melon slice (minecraft/tags.js)
	t.add(e, i.t_exotic_fruit, [
		i.pear, i.hawberry, i.lychee, i.mango, i.persimmon, i.peach, i.orange, i.mangosteen,
		i.bayberry, i.kiwi, i.fig, i.blueberry, i.lemon, i.cranberry, i.pineapple, i.durian_flesh,
	])
	// the hamimelon slice is merged into the cantaloupe slice (fruitsdelight/item_merges.js, delightful/tags.js)
	t.add(e, i.t_fruit_salad_slice, [
		i.pineapple_slice, i.lemon_slice, i.orange_slice,
	])
})

ServerEvents.tags('block', e => {
	fruitsCrateTags(e)
})
