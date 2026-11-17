// requires: fruitsdelight
// Fruits Delight 0.5.11 food fixes (fruits_delight.md 2.3). Creative Delight startup script, loaded from the jar after
// the pack's startup_scripts. Restart to apply. Creative Brewing's alcohol_drinks.js rebuilds the Bellini after this
// (CB loads after CD); the result is the same either way (it keeps every non-Nausea effect, this keeps the longest of
// each). The Java fixes (placed Mangosteen Cake, getFoodData null guard) are CD's fruitsdelight mixin compartment.

// Bellini Cocktail lists Heal Aura twice: its own 10 s and the peach's 5 s (Fruits Delight's FoodType skips a fruit
// effect the food already has by comparing suppliers, which never match). 1.1.3 keeps one Heal Aura 10 s. Drinking
// it already gave 10 s; the tooltip loses the second line.
// An absolute row of global.foodValueCopies (applied by creativedelight/food_value_copies.js): every value kept, the
// longest entry per effect, in the original order.
global.foodValueCopies = global.foodValueCopies || {}
global.foodValueCopies.bellini_cocktail = { absolute: { dedupeEffects: true } }
