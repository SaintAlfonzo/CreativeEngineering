// requires: immersiveengineering
// Golden foods in the Arc Furnace (gilding). Grid recipe: minecraft/golden.js; mixer and sequenced assembly:
// create/golden.js. Moved from the pack's kubejs/server_scripts/cooking/golden.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	const i = global.item

	global.CD.recipes(() => {

		// The forge gold tags also hold rose gold (nugget from metaltags.js, ingot and block from MCA); all of it counts.
		// Machines save 2 of the grid's 8 (mixer) or 3 (arc furnace); revisit with food tuning (food/types-and-tuning #1, #3).

		//Golden Carrot
		R.ieArcFurnace(i.golden_carrot, i.carrot, e, 'golden_carrot_arc_gilding', [], [i.any_nugget_gold, i.any_nugget_gold, i.any_nugget_gold, i.any_nugget_gold, i.any_nugget_gold])

		//Golden Apple
		R.ieArcFurnace(i.golden_apple, i.apple, e, 'golden_apple_arc_gilding', [], [i.any_ingot_gold, i.any_ingot_gold, i.any_ingot_gold, i.any_ingot_gold, i.any_ingot_gold])
	})
})
