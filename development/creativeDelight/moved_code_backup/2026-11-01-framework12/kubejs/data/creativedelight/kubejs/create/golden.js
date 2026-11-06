// requires: create
// Golden foods in Create: gilding in a heated mixer, the enchanted golden apple by sequenced assembly. Grid recipe:
// minecraft/golden.js; arc furnace: immersiveengineering/golden.js. Moved from the pack's
// kubejs/server_scripts/cooking/golden.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	const i = global.item
	const f = global.fluid

	global.CD.recipes(() => {

		// The forge gold tags also hold rose gold (nugget from metaltags.js, ingot and block from MCA); all of it counts.
		// Machines save 2 of the grid's 8 (mixer) or 3 (arc furnace); revisit with food tuning (food/types-and-tuning #1, #3).

		//Golden Carrot
		//Efficient recipes
		R.createMixing(i.golden_carrot, [i.carrot, Ingredient.of([i.any_nugget_gold]).withCount(6)], e, 'golden_carrot_gilding', 'heated')

		//Golden Apple
		R.createMixing(i.golden_apple, [i.apple, Ingredient.of([i.any_ingot_gold]).withCount(6)], e, 'golden_apple_gilding', 'heated')

		//Enchanted Golden Apple
		let incEnchGoldApple = R.incomplete('enchanted_golden_apple')
		R.createSequencedAssembly(Item.of(i.enchanted_golden_apple, 1), i.golden_apple, [
			e.recipes.create.deploying(incEnchGoldApple, [incEnchGoldApple, i.any_block_gold]),
			e.recipes.create.filling(incEnchGoldApple, [incEnchGoldApple, Fluid.of(f.xp, 250)]),
			e.recipes.create.pressing(incEnchGoldApple, incEnchGoldApple)
		], ['enchanted_golden_apple'], e, 'enchanted_golden_sequenced_assembly', 4)
	})
})
