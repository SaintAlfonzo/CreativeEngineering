// requires: vintagedelight
// Vintage Delight item tags (Creative Delight members #3, moved from the pack's server_scripts/tags/cookingtags.js
// 2026-10-25, rows unchanged). Nothing in the pack removes from these tags, so running after the pack's tag scripts
// gives the same contents.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('vintagedelight')) return
	var t = global.tags
	var i = global.item

	// Vintage Delight port (vintage_delight.md 3.1): oat dough is a #forge:dough (owner, 2026-10-23); cucumber dough
	// stays a named ingredient. Cucumber noodles stay in #forge:pasta until the VTD-40 overlap review.
	t.add(e, i.any_dough, i.oat_dough)
	// Crop, seed and crate tags for the Vintage crops, the Veggies port's pattern (vintage_delight.md 4 step 2)
	t.add(e, 'forge:crops/oat', i.oat)
	t.add(e, 'forge:crops/peanut', i.peanut)
	t.add(e, 'forge:crops/cucumber', i.cucumber)
	t.add(e, 'forge:crops/ghost_pepper', i.ghost_pepper)
	t.add(e, 'forge:crops', ['#forge:crops/oat', '#forge:crops/peanut', '#forge:crops/cucumber', '#forge:crops/ghost_pepper'])
	t.add(e, 'forge:seeds/oat', i.oat_seeds)
	t.add(e, 'forge:seeds/cucumber', i.cucumber_seeds)
	t.add(e, 'forge:seeds/ghost_pepper', i.ghost_pepper_seeds)
	t.add(e, 'forge:seeds', ['#forge:seeds/oat', '#forge:seeds/cucumber', '#forge:seeds/ghost_pepper'])
	t.add(e, 'forge:storage_blocks/oat', i.oat_bale)
	t.add(e, 'forge:storage_blocks/peanut', i.peanut_crate)
	t.add(e, 'forge:storage_blocks/cucumber', i.cucumber_crate)
	t.add(e, 'forge:storage_blocks/ghost_pepper', i.ghost_pepper_crate)
})
