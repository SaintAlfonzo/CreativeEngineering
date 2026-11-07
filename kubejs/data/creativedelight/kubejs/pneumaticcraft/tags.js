// requires: creativedelight
// requires: pneumaticcraft
// PneumaticCraft items in the pack's food and cooking item tags (moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged; split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// Glycerol is not a sugar (TODO dishes/ingredients #29, 2026-11-02): out of #forge:sugar and so out of every sweetener slot
	t.remove(e, 'forge:sugar', i.glycerol)

	// flour / dough cleanup (the Farmer's Delight dough: farmersdelight/tags.js)
	t.clearAll(e, i.wheat_flour_pc)
})
