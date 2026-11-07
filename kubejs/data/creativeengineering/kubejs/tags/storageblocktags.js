// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/tags/storageblocktags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
console.log('storage block tags starting')

var t = global.tags
var i = global.item

// material : [block ids]
var storage = {
	steel: [i.block_steel], // immersiveengineering:storage_steel
}

// soft storage blocks: hand-breakable, hoe is the fast tool (like prepared_flesh_block). The 7 meat culture blocks
// moved to Creative Delight 2026-10-27 (creativeengineering/cultivated_meat_tags.js; reserved for Creative Cultivation,
// creative_delight.md 29.5).
var softStorage = {
}

// dough blocks (shovel): Creative Delight (creativeengineering/kitchen_tags.js)

ServerEvents.tags('block', e => {
	for (var mat in storage) t.storageBlock(e, mat, storage[mat], { level: 'iron' })
	for (var mat in softStorage) t.storageBlock(e, mat, softStorage[mat], { tool: 'hoe' })
})

ServerEvents.tags('item', e => {
	for (var mat in storage) t.storageBlock(e, mat, storage[mat])
	for (var mat in softStorage) t.storageBlock(e, mat, softStorage[mat])
})

console.log('storage block tags finished')
