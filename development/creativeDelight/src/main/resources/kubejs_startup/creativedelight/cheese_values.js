// Creative Delight startup script. Food values of the dishes whose milk became cheese (TODO dishes/meals #45,
// plan_food_batch_1119.md A1, A3; recipes: data creativedelight/kubejs/creativedelight/cheese_for_milk.js). The values
// are re-derived upward from the dearer input under the soup rule of food/types-and-tuning #8 (output hunger about 1.5x
// its ingredients' hunger; milk counts 2, a cheese wedge or slice 4): +3 hunger for a one-bowl dish, +1 a serving for a
// feast (one cheese spread over its servings). The saturation modifier stays. Values from the
// food dump; applied by food_value_copies.js (restart to apply; rows written directly, as this file loads before the
// applier defines absolute()). One file for the whole change, so a later
// retune of the rule finds every row here.
global.foodValueCopies = global.foodValueCopies || {}
;[
	// key (item_dict), nutrition, saturation modifier          was
	['broccoli_soup', 13, 0.7],                                 // 10
	['pasta_with_broccoli', 15, 0.8],                           // 12
	['stuffed_potato', 13, 0.7],                                // 10
	['truffle_pasta', 15, 0.8],                                 // 12
	['creamy_pasta_with_chicken_cuts', 15, 0.8],                // 12
	['creamy_pasta_with_ham', 15, 0.8],                         // 12
	['nachos_bowl', 13, 1.0],                                   // 12, feast serving
	['lasagna_slice', 15, 0.75],                                // 14, feast serving
	['fiery_fondue', 15, 0.75],                                 // 14, feast serving
].forEach(r => { global.foodValueCopies[r[0]] = { absolute: { nutrition: r[1], saturationMod: r[2] } } })
