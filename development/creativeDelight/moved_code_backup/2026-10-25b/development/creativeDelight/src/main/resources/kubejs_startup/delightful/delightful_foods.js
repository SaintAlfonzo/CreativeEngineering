// requires: delightful
// Delightful 3.8.1 hunger and saturation on the installed 3.4.1 (feature_writeup/delightful.md 2.4). Creative Delight
// startup script. Effects are not touched: the 3.8.1 effect changes wait for the effects review. Restart to apply.
// item key -> [hunger, saturation modifier]
global.delightfulFoodValues = {
	cheeseburger: [11, 0.85],
	deluxe_cheeseburger: [13, 1.0],
	sinigang: [12, 0.7],
	smore: [8, 0.4],
	marshmallow_stick: [3, 0.35],
	cooked_marshmallow_stick: [5, 0.35],
	crab_rangoon: [10, 0.5],
	field_salad: [10, 0.6],
	matcha_ice_cream: [6, 0.3],
	salmonberry_ice_cream: [6, 0.3],
	rock_candy: [4, 0.5],
	honey_glazed_walnut: [6, 1.0],
	nut_butter_bottle: [6, 0.6],
	ender_nectar: [4, 0.0],
	chunkwich: [11, 0.4],
	green_tea_leaf: [1, 0.1],
	chopped_clover: [1, 0.35],
}

ItemEvents.modification(e => {
	var $DelightfulFoodBuilder = Java.loadClass('net.minecraft.world.food.FoodProperties$Builder')

	Object.keys(global.delightfulFoodValues).forEach(key => {
		var id = global.item[key]
		if (typeof id !== 'string') { console.warn('[delightful foods] ' + key + ': not in item_dict'); return }
		var values = global.delightfulFoodValues[key]
		try {
			e.modify(id, item => {
				var old = item.getFoodProperties()
				if (!old) { console.warn('[delightful foods] ' + id + ' is not a food'); return }
				var food = new $DelightfulFoodBuilder().nutrition(values[0]).saturationMod(values[1])
				if (old.isMeat()) food.meat()
				if (old.canAlwaysEat()) food.alwaysEat()
				if (old.isFastFood()) food.fast()
				var effects = old.getEffects()
				for (var n = 0; n < effects.size(); n++) food.effect(effects.get(n).getFirst(), effects.get(n).getSecond())
				item.setFoodProperties(food.build())
			})
		} catch (err) {
			console.warn('[delightful foods] ' + id + ': ' + err)
		}
	})
})
