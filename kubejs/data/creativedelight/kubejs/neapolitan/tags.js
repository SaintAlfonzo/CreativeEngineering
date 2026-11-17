// requires: creativedelight
// requires: neapolitan
// Neapolitan items in the pack's food and cooking item tags (split by the mod owning each entry).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// meals (TODO dishes/meals #1, #2): cooked veggies for heatless soups and stews (minecraft/tags.js has the rule)
	t.add(e, i.t_soup_veggie_cooked, i.roasted_adzuki_beans)
})
