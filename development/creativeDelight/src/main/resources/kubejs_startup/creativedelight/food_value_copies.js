// Moved from the pack's startup_scripts/cooking/food_values.js 2026-10-25, rows unchanged.
// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
//
// Food values copied from one food onto another: target key -> source key (item_dict). The target takes the source's
// whole FoodProperties (hunger, saturation, effects, meat/fast/always flags), so anything the target had of its own is
// dropped. `{ from: key, effects: false }` copies everything but the effects; a non-food target becomes edible.
// ItemEvents.modification runs at startup: restart to apply.
// This file is the one applier. The rows live in the file of the mod that owns the target item, each adding to
// global.foodValueCopies (created with `global.foodValueCopies = global.foodValueCopies || {}`):
//   delightful/delightful_foods.js   cantaloupe_slice
//   minecraft/food_values.js         glistering_melon_slice
// The effect swaps and Farmer's Respite cake slice effects: Creative Brewing's kubejs_startup/creativeengineering/
// effect_swaps.js (moved from the pack's cooking/food_values.js 2026-11-01).
global.foodValueCopies = global.foodValueCopies || {}

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
