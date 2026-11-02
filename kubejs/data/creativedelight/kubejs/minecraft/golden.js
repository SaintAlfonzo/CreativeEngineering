// Golden foods (food_families.md 4.5 `golden`, creative_delight.md 30): the golden family calls of global.CD.food for
// vanilla's three. goldenGild = the food in a ring of 8 gold by hand (vanilla's shaped recipe replaced), a heated
// Create mixer with 6 gold and an Immersive Engineering Arc Furnace with 5 (each only when that mod is loaded);
// goldenEnchant = a Create sequenced assembly on the golden apple (gold block, 250 mB liquid experience, press, 4
// loops). Moved from the pack's kubejs/server_scripts/cooking/golden.js 2026-10-25; on the family calls since
// 2026-11-01 (framework #12), which folded in create/golden.js and immersiveengineering/golden.js.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const i = global.item

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		// The forge gold tags also hold rose gold (nugget from metaltags.js, ingot and block from MCA); all of it counts.
		// Machines save 2 of the grid's 8 (mixer) or 3 (arc furnace); revisit with food tuning (food/types-and-tuning #1, #3).
		F.goldenGild(i.carrot, i.golden_carrot, i.any_nugget_gold)
		F.goldenGild(i.apple, i.golden_apple, i.any_ingot_gold)
		F.goldenEnchant(i.golden_apple, i.enchanted_golden_apple)
	})
})
