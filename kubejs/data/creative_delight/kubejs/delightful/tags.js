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

// Delightful items in the pack's food and cooking item tags (moved from the pack's server_scripts/tags/cookingtags.js
// 2026-10-25, rows unchanged; split by the mod owning each entry). Not behind the 3.8.1 backport check above.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('delightful')) return
	var i = global.item
	var t = global.tags
	// meals (TODO dishes/meals #1, #2): cooked veggies for heatless soups and stews (minecraft/tags.js has the rule)
	t.add(e, i.t_soup_veggie_cooked, i.cactus_steak)
	// fruit salad slices (the rest: minecraft/tags.js, fruitsdelight/tags.js)
	t.add(e, i.t_fruit_salad_slice, i.cantaloupe_slice)
	// Field salad slot alternatives (TODO dishes/meals #39, recipes.js): each slot also takes the sliced form of its
	// items where one exists (zucchini slice, cauliflower floret). Berries: #forge:berries. Lettuce slot: the
	// salad ingredients, or flowers in the second recipe.
	t.add(e, i.t_field_salad_root, [i.carrot, i.onion, i.beetroot, i.corn, i.turnip])
	t.add(e, i.t_field_salad_nut, [i.t_nuts_acorn, i.peanut])
	t.add(e, i.t_field_salad_green, [i.cactus_steak, i.broccoli, i.cauliflower, i.cauliflower_floret, i.zucchini,
		i.zucchini_slice, i.cucumber, i.bell_pepper_green])
	// cooked meat (the other entries: minecraft, farmersdelight, alexsmobs, alexsdelight, quark tags.js). The venison
	// is a removed item; _core/removed_item_tags.js strips it again after the CD tag scripts.
	t.add(e, 'forge:cooked_meat', [
		i.venison_chops_cooked,
	])
})
