console.log('storage block tags starting')

var t = global.tags
var i = global.item

// material : [block ids]
var storage = {
	steel: [i.block_steel], // immersiveengineering:storage_steel
}

ServerEvents.tags('block', e => {
	for (var mat in storage) t.storageBlock(e, mat, storage[mat], { level: 'iron' })
})

ServerEvents.tags('item', e => {
	for (var mat in storage) t.storageBlock(e, mat, storage[mat])
})

console.log('storage block tags finished')
