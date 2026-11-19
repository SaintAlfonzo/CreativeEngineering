// Creative Delight startup script. Edible seeds (TODO dishes/ingredients #40, plan_food_batch_1119.md R15, B3): seeds
// eaten raw. 1 hunger point is half a shank. Sunflower seeds and corn kernels: 1 hunger, 0.5 saturation points
// (modifier 0.25); pumpkin, oat and mixed seeds: 1 hunger, no saturation. Non-food seeds become edible (planting by
// using them on farmland is unchanged). The tag #creativedelight:edible_seeds (data creativedelight/tags.js) holds the
// same five. Applied by food_value_copies.js (restart to apply; rows written directly, as this file loads before the
// applier defines absolute()).
global.foodValueCopies = global.foodValueCopies || {}
;[
	// key (item_dict), nutrition, saturation modifier
	['sunflower_seeds', 1, 0.25],   // Hearth and Harvest's: was 1 / 0.3
	['corn_seeds', 1, 0.25],        // Corn Delight's kernels: was 1 / 0.2
	['pumpkin_seeds', 1, 0],
	['oat_seeds', 1, 0],            // Vintage Delight's
	['mixed_seeds', 1, 0],          // Vintage Delight's
].forEach(r => { global.foodValueCopies[r[0]] = { absolute: { nutrition: r[1], saturationMod: r[2] } } })
