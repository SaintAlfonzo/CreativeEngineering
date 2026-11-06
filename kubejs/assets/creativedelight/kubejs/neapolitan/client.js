// requires: neapolitan
// Neapolitan client rows, moved from the pack's client_scripts/ 2026-10-27, rows unchanged.
ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	e.renameItem(global.item.chocolate_spider_eye, 'Chocolate Night Berries')
})

ItemEvents.tooltip(e => {
	if (!global.CD) return
	// Y follows neapolitan-common.toml "White Strawberry minimum height"
	e.add(global.item.white_strawberries, Text.green('Strawberry Bushes at Y 192 or higher grow these instead of red ones'))
})
