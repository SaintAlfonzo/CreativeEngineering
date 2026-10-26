// Vanilla food value copies (rows of global.foodValueCopies, applied by creativeengineering/food_value_copies.js).
// Moved from the pack's startup_scripts/cooking/food_values.js 2026-10-25, rows unchanged. Creative Delight startup
// script. Restart to apply.
//   glistering_melon_slice <- golden_melon_slice   edible at the golden tier of its base food (Golden Food's gilded
//                                                  melon slice), effects left out for now (feature_writeup/food.md 3.10)
global.foodValueCopies = global.foodValueCopies || {}
Object.assign(global.foodValueCopies, {
	glistering_melon_slice: { from: 'golden_melon_slice', effects: false },
})
