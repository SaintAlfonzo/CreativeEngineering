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

// Each pair becomes an absolute row of global.foodValueCopies (applied by creativedelight/food_value_copies.js): hunger
// and modifier set, the meat, always-edible and fast flags and the effects kept.
global.foodValueCopies = global.foodValueCopies || {}
Object.keys(global.delightfulFoodValues).forEach(key => {
	var v = global.delightfulFoodValues[key]
	global.foodValueCopies[key] = { absolute: { nutrition: v[0], saturationMod: v[1] } }
})

// Food value copies (rows of global.foodValueCopies, applied by creativedelight/food_value_copies.js). Moved from
// the pack's startup_scripts/cooking/food_values.js 2026-10-25, rows unchanged.
//   cantaloupe_slice <- pumpkin_slice              Delightful's 4 / 0.8 + Resistance was far above the other melon
//                                                  slices (feature_writeup/can_parity.md 4.1)
Object.assign(global.foodValueCopies, {
	cantaloupe_slice: 'pumpkin_slice',
})
