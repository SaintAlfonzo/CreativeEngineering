// requires: creativedelight
// requires: neapolitan
// Milk bottle and chocolate cake merges (feature_writeup/item_merges.md 2.2, 2.3). Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's server_scripts/other/item_merges.js
// 2026-10-27, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		// Milk bottle. Abnormals Delight turns Farmer's Delight's bucket <-> bottle recipes off while Neapolitan is
		// loaded; Creative Delight replaces Neapolitan's two (3 per bucket) with 4 per bucket both ways
		// (abnormals_delight/neapolitan.js). Farmer's Delight's bottle joins #forge:bottles/milk below. Create's spout /
		// drain and Delightful's matcha latte already have a Farmer's Delight copy.
		R.remove({ id: 'create:filling/compat/neapolitan/milk_bottle' }, e)
		R.remove({ id: 'create:emptying/compat/neapolitan/milk_bottle' }, e)
		R.remove({ id: 'delightful:food/kettle/matcha_latte_neapolitan' }, e)

		// Chocolate cake. Abnormals Delight's 7 slices -> cake now makes Create Crafts & Additions' cake (output
		// swapped by the merge); its other recipes are creativeengineering/baking.js's (baking overhaul). The knife
		// recipe is rewritten on the cake that stays.
		R.remove({ id: 'abnormals_delight:neapolitan/cutting/chocolate_cake' }, e)
		R.fdCutting(i.chocolate_cake, i.any_knife, Item.of(i.abnormals_delight_chocolate_cake_slice, 7), e, 'chocolate_cake')
	})
})

ServerEvents.tags('item', e => {
	if (!global.CD) return
	global.tags.add(e, global.item.t_bottles_milk, global.item.farmersdelight_milk_bottle)
})

ServerEvents.tags('block', e => {
	if (!global.CD) return
	// a knife on the placed cake cuts a slice off (Neapolitan put its own cake in this tag)
	global.tags.add(e, 'abnormals_delight:drops_chocolate_cake_slice', global.item.chocolate_cake)
})
