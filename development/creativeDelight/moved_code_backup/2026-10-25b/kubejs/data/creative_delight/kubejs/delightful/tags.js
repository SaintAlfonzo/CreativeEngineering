// requires: delightful
// Delightful 3.8.1 tags the backported items need (feature_writeup/delightful.md 3.2): 3.4.1 has none of them. Contents
// as 3.8.1's, limited to items in the pack.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('delightful') || !Item.exists(global.item.cantaloupe_seeds)) return
	var i = global.item
	var t = global.tags
	t.add(e, i.any_knife, [i.aluminum_knife, i.certus_quartz_knife, i.fluix_knife])
	t.add(e, i.t_seeds, i.cantaloupe_seeds)
	t.add(e, i.t_matcha, i.matcha)
	t.add(e, i.t_cooked_nuts, [i.roasted_acorn, i.roasted_peanut])
	t.add(e, i.t_nut_butter, [i.nut_butter_bottle, i.nut_mash_bottle])
	t.add(e, i.t_crops_cactus, i.cactus_flesh)
	// 3.8.1: blaze powder and #forge:vegetables/spicy (empty here); the pack's spicy vegetable is the ghost pepper
	t.add(e, i.t_hot_spices, [i.blaze_powder, i.t_chilipepper])
})
