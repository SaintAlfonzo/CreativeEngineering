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

// dough blocks: shovel, like Create Compressed's dough block
var doughStorage = {
	gingerdough: [i.gingerdough_block],
	cookie_dough: [i.cookie_dough_block],
	sweet_potato_dough: [i.sweet_potato_dough_block],
	oat_dough: [i.oat_dough_block],
	cucumber_dough: [i.cucumber_dough_block],
	sourdough: [i.sourdough_block],
}

ServerEvents.tags('block', e => {
	for (var mat in storage) t.storageBlock(e, mat, storage[mat], { level: 'iron' })
	for (var mat in softStorage) t.storageBlock(e, mat, softStorage[mat], { tool: 'hoe' })
	for (var mat in doughStorage) t.storageBlock(e, mat, doughStorage[mat], { tool: 'shovel' })
})

ServerEvents.tags('item', e => {
	for (var mat in storage) t.storageBlock(e, mat, storage[mat])
	for (var mat in softStorage) t.storageBlock(e, mat, softStorage[mat])
	for (var mat in doughStorage) t.storageBlock(e, mat, doughStorage[mat])
})

console.log('storage block tags finished')
