// requires: ends_delight
// End's Delight drop tooltips (ends_delight.md 3.1): the mob parts that only come from loot modifiers. Same facts as
// the JEI pages in jei_info.js; grey like the pack's other drop lines (tooltips.md).
ItemEvents.tooltip(e => {
	if (!global.CD.on('ends_delight')) return
	var i = global.item

	e.add(i.raw_dragon_meat, Text.gray('Ender Dragon drop; more when killed with a knife or machete'))
	e.add(i.dragon_leg, Text.gray('Ender Dragon drop'))
	e.add(i.dragon_tooth, Text.gray('Ender Dragon drop; more when killed with a knife or machete'))
	e.add(i.shulker_meat, Text.gray('Shulker drop when killed with a knife or machete'))
	e.add(i.enderman_gristle, Text.gray('Enderman drop when killed with a knife or machete'))
	e.add(i.raw_ender_mite_meat, Text.gray('Endermite drop when killed with a knife or machete'))
	e.add(i.dried_endermite_meat, Text.gray('Burning Endermite drop when killed with a knife or machete'))
})
