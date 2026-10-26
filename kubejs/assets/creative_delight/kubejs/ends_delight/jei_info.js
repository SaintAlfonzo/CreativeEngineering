// requires: jei
// requires: ends_delight
// End's Delight JEI information pages: mob drops that come from loot modifiers, not recipes (ends_delight.md 3.1).
// Creative Delight QoL-6, switch [qol] jeiInfoPages. Ender's Delight's knife drops: endersdelight/jei_info.js.
JEIEvents.information(e => {
	if (!global.CD || !global.CD.on('ends_delight') || !global.CD.qol('jeiInfoPages')) return
	var i = global.item

	e.addItem(i.raw_dragon_meat, [Text.of('Dropped by the Ender Dragon (5 to 7). Killed with a knife or a machete: 2 more.')])
	e.addItem(i.dragon_leg, [Text.of('Dropped by the Ender Dragon (2).')])
	e.addItem(i.dragon_tooth, [Text.of('Dropped by the Ender Dragon (1 or 2). Killed with a knife or a machete: 1 more.')])
	e.addItem(i.shulker_meat, [Text.of('Dropped by a Shulker killed with a knife or a machete.')])
	e.addItem(i.enderman_gristle, [Text.of('Dropped by an Enderman killed with a knife or a machete.')])
	e.addItem(i.raw_ender_mite_meat, [Text.of('Dropped by an Endermite killed with a knife or a machete.')])
	e.addItem(i.dried_endermite_meat, [Text.of('Dropped by a burning Endermite killed with a knife or a machete.')])
})
