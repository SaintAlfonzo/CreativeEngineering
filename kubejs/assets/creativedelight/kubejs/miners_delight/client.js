// requires: creativedelight
// requires: miners_delight
// Miner's Delight client rows (TODO cooking/kitchen #32, plan_farming_cooking_batch.md A4): what the Copper Pot can cook.
// It takes Farmer's Delight cooking pot recipes of up to four ingredients (CopperPotBlockEntity.INPUT_SLOTS).
ItemEvents.tooltip(e => {
	if (!global.CD) return
	var i = global.item
	var T = global.tooltips
	if (!T) { console.error('[creativedelight] global.tooltips missing: the pack\'s client_scripts/tooltips.js did not run'); return }
	T.addTooltips(e,
		[i.copper_pot, ['Cooks some of what a Cooking Pot can, only fits 4 ingredients', T.col.uncm]]
	)
})
