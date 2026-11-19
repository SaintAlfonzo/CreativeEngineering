// requires: creativeengineering
// Creative Engineering.
console.log('storage block tags starting')

var t = global.tags
var i = global.item

// material : [block ids]
var storage = {
	steel: [i.block_steel], // immersiveengineering:storage_steel
}

// soft storage blocks: hand-breakable, hoe is the fast tool (like prepared_flesh_block).
var softStorage = {
}

// dough blocks (shovel): Creative Delight (creativeengineering/kitchen_tags.js)

// coil blocks (startup items/coil_blocks.js): pickaxe, iron
var coilBlocks = ['aluminum', 'brass', 'constantan', 'gold', 'iron', 'lead', 'nickel', 'silver', 'zinc', 'steel']
	.map(metal => i['coil_block_' + metal])

ServerEvents.tags('block', e => {
	t.mineable(e, 'pickaxe', 'iron', coilBlocks)
	// rose gold counts as gold (recipes/metal #3, owner 2026-11-18): the block tag as well as the item tag (metaltags.js)
	t.addTag(e, 'forge:storage_blocks/gold', i.block_rose_gold)
	for (var mat in storage) t.storageBlock(e, mat, storage[mat], { level: 'iron' })
	for (var mat in softStorage) t.storageBlock(e, mat, softStorage[mat], { tool: 'hoe' })
})

ServerEvents.tags('item', e => {
	for (var mat in storage) t.storageBlock(e, mat, storage[mat])
	for (var mat in softStorage) t.storageBlock(e, mat, softStorage[mat])
})

console.log('storage block tags finished')
