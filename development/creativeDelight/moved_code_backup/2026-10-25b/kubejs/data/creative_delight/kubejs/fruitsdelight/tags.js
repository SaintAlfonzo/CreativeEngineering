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

ServerEvents.tags('block', e => {
	fruitsCrateTags(e)
})
