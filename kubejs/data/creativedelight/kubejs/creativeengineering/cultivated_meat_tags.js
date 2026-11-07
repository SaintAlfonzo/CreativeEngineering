// requires: creativedelight
// Reserved for Creative Cultivation: all cultivated-meat code (the sample syringe, the meat cultures, the culture blocks,
// their recipes, tags, lang and textures) is marked for a future own mod, Creative Cultivation, the way potion brewing
// and alcohol are marked for Creative Brewing (creative_delight.md 29.5). Until then it lives in Creative Delight's
// creativeengineering/ trees.
//
// Cultivated meat tags, moved from the pack 2026-10-27, rows unchanged: the culture base (what a sample is grown on,
// server_scripts/tags/engineeringtags.js) and the 7 culture storage blocks (server_scripts/tags/storageblocktags.js,
// soft storage: hand-breakable, the hoe is the fast tool, like prepared_flesh_block). The BlockRunner "spongy" row
// for the culture blocks stays with the other soft blocks in the pack's server_scripts/tags/blocktags.js.

// material : [culture block]
var CD_CULTURE_STORAGE = {
	beef_culture: 'culture_block_beef',
	pork_culture: 'culture_block_pork',
	mutton_culture: 'culture_block_mutton',
	poultry_culture: 'culture_block_poultry',
	rabbit_culture: 'culture_block_rabbit',
	cod_culture: 'culture_block_cod',
	salmon_culture: 'culture_block_salmon',
}

ServerEvents.tags('block', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item
	for (var mat in CD_CULTURE_STORAGE) t.storageBlock(e, mat, [i[CD_CULTURE_STORAGE[mat]]], { tool: 'hoe' })
})

ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item
	t.add(e, i.culture_base, [i.mycelium, i.red_mushroom_colony, i.brown_mushroom_colony, i.red_mushroom_block, i.brown_mushroom_block])
	for (var mat in CD_CULTURE_STORAGE) t.storageBlock(e, mat, [i[CD_CULTURE_STORAGE[mat]]])
})
