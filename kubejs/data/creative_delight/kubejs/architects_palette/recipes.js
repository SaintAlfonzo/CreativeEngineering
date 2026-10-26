// requires: architects_palette
// Architect's Palette bread blocks from the dough block. Non-member file: recipe ids are
// creative_delight:<type>/<suffix> (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js
// 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Bread #########################
		//Add bread block recipes
		R.remove({ id: 'architects_palette:bread_block' }, e)
		R.remove({ id: 'architects_palette:bread_slab' }, e)
		R.remove({ id: 'architects_palette:stonecutting/bread_nub' }, e)
		if (Platform.isLoaded('create_compressed')) {
			R.smoking(i.bread_block, i.dough_block, e)
		}
		if (Platform.isLoaded('create')) {
			R.createSaw(Item.of(i.bread, 16), i.bread_block, e)
		}
		if (Platform.isLoaded('farmersdelight')) {
			R.fdCutting(i.bread_block, i.any_knife, Item.of(i.bread, 16), e)
		}
		R.stonecutting(i.bread_slab, i.bread_block, e)
		R.stonecutting(i.crustless_bread_block, i.bread_block, e)
		R.stonecutting(i.bread_nub, i.bread_slab, e)
	})
})
