// requires: creativedelight
// requires: fungidelight
// Fungi Delight item tags (Creative Delight members #4, moved from the pack's server_scripts/tags/cookingtags.js
// 2026-10-25, rows unchanged). Nothing in the pack removes from these tags, so running after the pack's tag scripts
// gives the same contents.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('fungidelight')) return
	var t = global.tags
	var i = global.item

	// Fungi Delight port (fungi_delight.md 5.1.6): every mushroom-type item in #forge:mushrooms and its type tag.
	// The port ships inky cap + cleaned cap, morel, truffle + slice; the cooked cap and the colonies are added here.
	// Dishes and crates stay out (a dish must not count as "any mushroom").
	t.add(e, i.t_mushrooms_inky_cap, [i.inky_cap_mushroom, i.cleaned_cap, i.cooked_cleaned_cap, i.inky_cap_mushroom_colony])
	t.add(e, i.t_mushrooms_morel, [i.morel_mushroom, i.morel_mushroom_colony])
	t.add(e, i.t_mushrooms_truffle, [i.truffle, i.truffle_slice])
	t.add(e, i.mushrooms, [i.t_mushrooms_inky_cap, i.t_mushrooms_morel, i.t_mushrooms_truffle])
	t.add(e, 'forge:storage_blocks/brown_mushroom', i.brown_mushroom_crate)
	t.add(e, 'forge:storage_blocks/red_mushroom', i.red_mushroom_crate)
	t.add(e, 'forge:storage_blocks/inky_cap', i.inky_cap_mushroom_crate)
	t.add(e, 'forge:storage_blocks/morel', i.morel_mushroom_crate)
	t.add(e, 'forge:storage_blocks/truffle', i.truffle_crate)
})
