// priority: -100
// Item merge swaps over Creative Delight's recipes (feature_writeup/item_merges.md). The pack's swap pass
// (server_scripts/other/item_merges.js) runs before every CD script, so recipes added here that name a purged id
// (e.g. the generated coffee berry cans in creativeengineering/canning.js) get the survivor from this second pass.
// Last in the CD tree by priority; the pass only touches script-added recipes, so the pack's are not swapped twice.
ServerEvents.recipes(e => {
	if (!global.CD || !global.itemMergeSwaps) return
	global.itemMergeSwaps(e, 'creative_delight')
})
