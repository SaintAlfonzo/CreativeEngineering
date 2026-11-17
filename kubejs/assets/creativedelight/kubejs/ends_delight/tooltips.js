// requires: creativedelight
// requires: ends_delight
// End's Delight drop tooltips (ends_delight.md 3.1): the mob parts that only come from loot modifiers. Same facts as
// the JEI pages in jei_info.js; gray like the pack's other drop lines (tooltips.md).
// A tooltip line in a col role (global.tooltips, Creative Common's client _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	if (!global.CD.on('ends_delight')) return
	var i = global.item

	e.add(i.raw_dragon_meat, tipLine('Ender Dragon drop; more when killed with a knife or machete', 'text'))
	e.add(i.dragon_leg, tipLine('Ender Dragon drop', 'text'))
	e.add(i.dragon_tooth, tipLine('Ender Dragon drop; more when killed with a knife or machete', 'text'))
	e.add(i.shulker_meat, tipLine('Shulker drop when killed with a knife or machete', 'text'))
	e.add(i.enderman_gristle, tipLine('Enderman drop when killed with a knife or machete', 'text'))
	e.add(i.raw_ender_mite_meat, tipLine('Endermite drop when killed with a knife or machete', 'text'))
	e.add(i.dried_endermite_meat, tipLine('Burning Endermite drop when killed with a knife or machete', 'text'))
})
