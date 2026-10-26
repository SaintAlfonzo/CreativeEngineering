// requires: upgrade_aquatic
// Upgrade Aquatic items in the pack's food and cooking item tags (moved from the pack's
// server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged; split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// meals (TODO dishes/meals #1, #2): cooked veggies for heatless soups and stews (minecraft/tags.js has the rule)
	t.add(e, i.t_soup_veggie_cooked, [i.boiled_blue_pickerelweed, i.boiled_purple_pickerelweed])

	// Abnormals Delight's two pickerelweed pot recipes take any two (abnormals_delight.md 3.3; 1.21 Upgrade Aquatic
	// merged the colours)
	t.add(e, i.t_pickerelweed, [i.blue_pickerelweed, i.purple_pickerelweed])
})
