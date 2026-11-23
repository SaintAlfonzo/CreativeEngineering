// Creative Delight startup script. Pizza slice values (TODO baking/baked-goods #20, plan_food_batch_1119.md R25): a
// whole pizza is worth 1.5x its value before the 8-slice rule (4 slices then, 8 now at the same slice value, so it had
// doubled). Each slice is therefore 0.75x the mod's slice: hunger rounded, the saturation modifier set so a slice's
// saturation is 0.75x the old one. Recipes and the slice rule: data creativedelight/kubejs/creativedelight/pizza.js.
// Applied by food_value_copies.js (restart to apply).
global.foodValueCopies = global.foodValueCopies || {}
;[
	// key (item_dict), nutrition, saturation modifier       was (nutrition, modifier, saturation)
	['cheese_pizza_slice', 3, 0.75],                         // 4, 0.75, 6.0 -> 4.5
	['meat_pizza_slice', 4, 0.7],                            // 5, 0.75, 7.5 -> 5.6
	['tropical_pizza_slice', 4, 0.7],                        // 5, 0.75, 7.5 -> 5.6
	['vegan_pizza_slice', 4, 0.47],                          // 5, 0.5, 5.0 -> 3.76
	['pizza_slice', 4, 0.94],                                // Supreme (Brewin'): 5, 1.0, 10.0 -> 7.52
].forEach(r => { global.foodValueCopies[r[0]] = { absolute: { nutrition: r[1], saturationMod: r[2] } } })
