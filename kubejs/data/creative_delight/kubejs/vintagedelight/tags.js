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
	// One salt (food/seasoning #6): salt dust takes The Salt's salt's tags. #forge:salts is the Salting craft's input.
	t.add(e, ['forge:salts', 'forge:dusts', 'supplementaries:hourglass_dusts'], i.salt_dust)
	// Pickled veggies: pickle soup's pickle slot (dishes/pickles-and-ferments #3). Brewin's kimchi is added only beside Brewin'.
	t.add(e, i.t_pickled_veg, [i.pickle, i.pickled_sea_pickle, i.pickled_beetroot, i.pickled_onion, i.pickled_pepper, i.vintagedelight_kimchi])
	if (Platform.isLoaded('brewinandchewin')) t.add(e, i.t_pickled_veg, i.kimchi)
	// the Pickled Sea Pickle takes the Pickled Pickles' place as a cooked soup veggie (heatless soups)
	t.add(e, i.t_soup_veggie_cooked, i.pickled_sea_pickle)
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

// Cucumber and ghost pepper pick their fruit and drop back to stage 5; Quark's replant to stage 0 skips them
// (vintageport #8, crops.md 2.2)
ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('vintagedelight')) return
	var b = global.blocks
	global.tags.add(e, b.t_own_pick_crops, [b.cucumber_crop, b.ghost_pepper_crop])

	// Ghost pepper seeds plant only on these (vintage_delight.md 2.1, S6; moved from the pack's
	// server_scripts/tags/blocktags.js 2026-10-27). Nylium is left out on purpose: the wild ghost pepper still generates
	// there (its own block accepts nylium too).
	global.tags.add(e, b.t_ghost_pepper_soil, [b.soul_soil, b.rich_soul_soil, b.iw_nulch])
})
