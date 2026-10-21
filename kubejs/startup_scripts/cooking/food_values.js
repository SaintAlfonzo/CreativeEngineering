// Food values copied from one food onto another: target key -> source key (item_dict). The target takes the source's
// whole FoodProperties (hunger, saturation, effects, meat/fast/always flags), so anything the target had of its own is
// dropped. ItemEvents.modification runs at startup: restart to apply.
//   cantaloupe_slice <- pumpkin_slice   Delightful's 4 / 0.8 + Resistance was far above the other melon slices
//                                       (feature_writeup/can_parity.md 4.1)
global.foodValueCopies = {
	cantaloupe_slice: 'pumpkin_slice',
}

ItemEvents.modification(e => {
	var i = global.item
	var $FoodValuesRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $FoodValuesRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var done = 0
	Object.keys(global.foodValueCopies).forEach(key => {
		var srcKey = global.foodValueCopies[key]
		var id = i[key]
		var srcId = i[srcKey]
		if (typeof id !== 'string' || typeof srcId !== 'string') {
			console.warn('[food values] ' + key + ' <- ' + srcKey + ': not in item_dict')
			return
		}
		var src = $FoodValuesRegistries.ITEMS.getValue(new $FoodValuesRL(srcId))
		var food = src ? src.getFoodProperties() : null
		if (!food) { console.warn('[food values] ' + srcId + ' is not a food'); return }
		try {
			e.modify(id, item => item.setFoodProperties(food))
			done++
		} catch (err) {
			console.error('[food values] could not set food on ' + id + ': ' + err)
		}
	})
	console.log('[food values] ' + done + ' foods copied')
})
