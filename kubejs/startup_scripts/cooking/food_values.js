// Food values copied from one food onto another: target key -> source key (item_dict). The target takes the source's
// whole FoodProperties (hunger, saturation, effects, meat/fast/always flags), so anything the target had of its own is
// dropped. `{ from: key, effects: false }` copies everything but the effects; a non-food target becomes edible.
// ItemEvents.modification runs at startup: restart to apply.
//   cantaloupe_slice <- pumpkin_slice              Delightful's 4 / 0.8 + Resistance was far above the other melon
//                                                  slices (feature_writeup/can_parity.md 4.1)
//   glistering_melon_slice <- golden_melon_slice   edible at the golden tier of its base food (Golden Food's gilded
//                                                  melon slice), effects left out for now (feature_writeup/food.md 3.10)
global.foodValueCopies = {
	cantaloupe_slice: 'pumpkin_slice',
	glistering_melon_slice: { from: 'golden_melon_slice', effects: false },
}

ItemEvents.modification(e => {
	var i = global.item
	var $FoodValuesRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $FoodValuesRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var $FoodValuesBuilder = Java.loadClass('net.minecraft.world.food.FoodProperties$Builder')
	var done = 0
	Object.keys(global.foodValueCopies).forEach(key => {
		var entry = global.foodValueCopies[key]
		var srcKey = typeof entry === 'string' ? entry : entry.from
		var keepEffects = typeof entry === 'string' || entry.effects !== false
		var id = i[key]
		var srcId = i[srcKey]
		if (typeof id !== 'string' || typeof srcId !== 'string') {
			console.warn('[food values] ' + key + ' <- ' + srcKey + ': not in item_dict')
			return
		}
		var src = $FoodValuesRegistries.ITEMS.getValue(new $FoodValuesRL(srcId))
		var food = src ? src.getFoodProperties() : null
		if (!food) { console.warn('[food values] ' + srcId + ' is not a food'); return }
		if (!keepEffects) {
			var b = new $FoodValuesBuilder().nutrition(food.getNutrition()).saturationMod(food.getSaturationModifier())
			if (food.isMeat()) b.meat()
			if (food.canAlwaysEat()) b.alwaysEat()
			if (food.isFastFood()) b.fast()
			food = b.build()
		}
		try {
			e.modify(id, item => item.setFoodProperties(food))
			done++
		} catch (err) {
			console.error('[food values] could not set food on ' + id + ': ' + err)
		}
	})
	console.log('[food values] ' + done + ' foods copied')
})
