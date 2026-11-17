// requires: creativedelight
// requires: cookingforblockheads
// Cooking for Blockheads kitchen tooltips. The oven burns any furnace fuel plus cooking oil (ovenRequiresCookingOil =
// false + Creative Delight Cooking for Blockheads compartment, CfbOvenFuelMixin), and fuel containers come back to the
// output. It cooks food from furnace and smoker recipes (Creative Delight compartment CfbOvenSmokingMixin, TODO
// cooking/oven #4). TODO cooking/kitchen #8. A tooltip line in a col role (global.tooltips, Creative Common's client
// _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	if (!global.CD) return
	var i = global.item
	e.add(i.cfb_sink, tipLine('Gives infinite water', 'hint'))
	// The oven lines describe the compartment's mixins: only while it is on
	if (!global.CD.on('cookingforblockheads')) return
	e.add(i.cfb_oven, [
		tipLine('Cooks food like a furnace and a smoker', 'text'),
		tipLine('Burns any furnace fuel, and Cooking Oil', 'hint'),
		tipLine('Empty fuel containers (bottles, buckets) go to the output slots', 'text'),
	])
})
