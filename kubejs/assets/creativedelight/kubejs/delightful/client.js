// requires: creativedelight
// requires: delightful
// Delightful client rows. A tooltip line in a col role (global.tooltips, Creative Common's client
// _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	var i = global.item
	e.renameItem(i.cactus_flesh, 'Cactus Flesh') // was Cactus Chunk; its can is Canned Cactus Flesh
	// Delightful 3.8.1's names for three 3.4.1 items (feature_writeup/delightful.md 2.3)
	e.renameItem(i.berry_matcha_latte, 'Iced Berry Matcha Latte')
	e.renameItem(i.nut_butter_bottle, 'Nut Butter')
	e.renameItem(i.rock_candy, 'Rose Rock Candy')
})

ItemEvents.tooltip(e => {
	if (!global.CD) return
	e.add(global.item.lead_knife, tipLine('Extra nutrients in your food...', 'warn'))
})
