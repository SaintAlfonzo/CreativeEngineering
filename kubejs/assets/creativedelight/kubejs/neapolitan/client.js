// requires: creativedelight
// requires: neapolitan
// Neapolitan client rows. A tooltip line in a col role (global.tooltips, Creative Common's client
// _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	e.renameItem(global.item.chocolate_spider_eye, 'Chocolate Night Berries')
})

ItemEvents.tooltip(e => {
	if (!global.CD) return
	// Y follows neapolitan-common.toml "White Strawberry minimum height"
	e.add(global.item.white_strawberries, tipLine('Strawberry Bushes at Y 192 or higher grow these instead of red ones', 'hint'))
})
