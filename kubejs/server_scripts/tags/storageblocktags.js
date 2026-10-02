console.log('storage block tags starting')

var t = global.tags
var i = global.item

// material : [block ids]
var storage = {
	steel: [i.block_steel], // immersiveengineering:storage_steel
}

// soft storage blocks: hand-breakable, hoe is the fast tool (like prepared_flesh_block)
var softStorage = {
	beef_culture: [i.culture_block_beef],
	pork_culture: [i.culture_block_pork],
	mutton_culture: [i.culture_block_mutton],
	poultry_culture: [i.culture_block_poultry],
	rabbit_culture: [i.culture_block_rabbit],
	cod_culture: [i.culture_block_cod],
	salmon_culture: [i.culture_block_salmon],
}

ServerEvents.tags('block', e => {
	for (var mat in storage) t.storageBlock(e, mat, storage[mat], { level: 'iron' })
	for (var mat in softStorage) t.storageBlock(e, mat, softStorage[mat], { tool: 'hoe' })
})

ServerEvents.tags('item', e => {
	for (var mat in storage) t.storageBlock(e, mat, storage[mat])
	for (var mat in softStorage) t.storageBlock(e, mat, softStorage[mat])
})

console.log('storage block tags finished')
