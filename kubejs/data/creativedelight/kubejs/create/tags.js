// requires: create
// Create items in the pack's food and cooking item tags (moved from the pack's server_scripts/tags/cookingtags.js
// 2026-10-25, rows unchanged).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// flour / dough cleanup (the other flour and dough: pneumaticcraft/tags.js, farmersdelight/tags.js)
	t.add(e, 'forge:dusts/flour', i.in_flour)
})
