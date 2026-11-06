// requires: cookingforblockheads
// Cooking for Blockheads kitchen tooltips, moved from the pack's client_scripts/tooltips.js 2026-10-27, rows unchanged.
// The oven burns any furnace fuel plus cooking oil (ovenRequiresCookingOil = false + Creative Delight Cooking for Blockheads compartment, CfbOvenFuelMixin), and
// fuel containers come back to the output. It cooks food from furnace and smoker recipes (Creative Delight compartment
// CfbOvenSmokingMixin, TODO cooking/oven #4). TODO cooking/kitchen #8.
ItemEvents.tooltip(e => {
	if (!global.CD) return
	var i = global.item
	e.add(i.cfb_sink, Text.green('Gives infinite water'))
	// The oven lines describe the compartment's mixins: only while it is on
	if (!global.CD.on('cookingforblockheads')) return
	e.add(i.cfb_oven, [
		Text.gray('Cooks food like a furnace and a smoker'),
		Text.green('Burns any furnace fuel, and Cooking Oil'),
		Text.gray('Empty fuel containers (bottles, buckets) go to the output slots'),
	])
})
