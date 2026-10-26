// Golden carrot grid recipe (vanilla's replaced). The machine routes for the golden foods are create/golden.js and
// immersiveengineering/golden.js. Moved from the pack's kubejs/server_scripts/cooking/golden.js 2026-10-25, rows
// unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	const i = global.item

	global.CD.recipes(() => {

		// The forge gold tags also hold rose gold (nugget from metaltags.js, ingot and block from MCA); all of it counts.
		// Machines save 2 of the grid's 8 (mixer) or 3 (arc furnace); revisit with food tuning (food/types-and-tuning #1, #3).

		//Golden Carrot
		//Replace default
		R.remove({ id: 'minecraft:golden_carrot' }, e)
		R.shaped(Item.of(i.golden_carrot, 1), ['NNN','NCN', 'NNN'], {C: i.carrot, N: i.any_nugget_gold}, e)
	})
})
