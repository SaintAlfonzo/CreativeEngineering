// requires: endersdelight
// Ender's Delight client rows (non-member; ends_delight.md 3.1): knife drop tooltips and JEI pages (1.1.4's info
// lines), and 1.1.4's Shulker Bowl message for the Stuffed Shulker feast.
ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	// set in the endersdelight namespace, where the mod's lang sets it (client_scripts/name_fixes.js)
	e.add('endersdelight', 'endersdelight.block.stuffed_shulker.use_shulker_bowl', 'You need a Shulker Bowl to eat this')
})

ItemEvents.tooltip(e => {
	if (!global.CD) return
	var i = global.item
	e.add(i.enderman_sight, Text.gray('Enderman drop when killed with a knife'))
	e.add(i.mite_crust, Text.gray('Endermite drop when killed with a knife'))
	e.add(i.shulker_mollusk, Text.gray('Shulker drop when killed with a knife'))
})

if (Platform.isLoaded('jei')) {
	JEIEvents.information(e => {
		if (!global.CD || !global.CD.qol('jeiInfoPages')) return
		var i = global.item
		e.addItem(i.enderman_sight, [Text.of('Dropped by an Enderman killed with a knife.')])
		e.addItem(i.mite_crust, [Text.of('Dropped by an Endermite killed with a knife.')])
		e.addItem(i.shulker_mollusk, [Text.of('Dropped by a Shulker killed with a knife.')])
	})
}
