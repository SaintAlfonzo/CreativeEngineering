// requires: fruitsdelight
// Fruits Delight 0.5.11 food fixes (fruits_delight.md 2.3). Creative Delight startup script, loaded from the jar after
// the pack's startup_scripts. Restart to apply. Creative Brewing's alcohol_drinks.js rebuilds the Bellini after this
// (CB loads after CD since 2026-11-01); the result is the same either way (it keeps every non-Nausea effect, this
// keeps the longest of each).
// The Java fixes (placed Mangosteen Cake, getFoodData null guard) are CD's fruitsdelight mixin compartment.

// Bellini Cocktail lists Heal Aura twice: its own 10 s and the peach's 5 s (Fruits Delight's FoodType skips a fruit
// effect the food already has by comparing suppliers, which never match). 1.1.3 keeps one Heal Aura 10 s. Drinking
// it already gave 10 s; the tooltip loses the second line.
ItemEvents.modification(e => {
	var $FruitsFoodBuilder = Java.loadClass('net.minecraft.world.food.FoodProperties$Builder')

	e.modify(global.item.bellini_cocktail, item => {
		var old = item.getFoodProperties()
		if (!old) return
		var food = new $FruitsFoodBuilder().nutrition(old.getNutrition()).saturationMod(old.getSaturationModifier())
		if (old.isMeat()) food.meat()
		if (old.canAlwaysEat()) food.alwaysEat()
		if (old.isFastFood()) food.fast()
		// longest entry per effect, in the original order
		var effects = old.getEffects()
		var keep = []
		for (var n = 0; n < effects.size(); n++) {
			var pair = effects.get(n)
			var inst = pair.getFirst()
			var same = -1
			for (var k = 0; k < keep.length; k++) if (keep[k].getFirst().getEffect().equals(inst.getEffect())) same = k
			if (same < 0) keep.push(pair)
			else if (inst.getDuration() > keep[same].getFirst().getDuration()) keep[same] = pair
		}
		keep.forEach(p => food.effect(p.getFirst(), p.getSecond()))
		item.setFoodProperties(food.build())
	})
})
