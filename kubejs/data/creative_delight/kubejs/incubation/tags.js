// requires: incubation
// Incubation item tags (moved from the pack's server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// redundant egg
	t.clearAll(e, i.fried_egg_incubation)
})
