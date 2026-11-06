// requires: create
// requires: create_compressed
// Slime block from a dough block and green dye in a heated mixer (cooking/kitchen #12, briefs/cooking_kitchen.md):
// the bulk route; Create's own grid slime ball takes lime dye. Replaces the Pickling Jar's slime block
// (vintagedelight/recipes.js).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		R.createMixing(i.slime_block, [i.dough_block, i.any_dye_green], e, 'slime_block_from_dough_block', 'heated')
	})
})
