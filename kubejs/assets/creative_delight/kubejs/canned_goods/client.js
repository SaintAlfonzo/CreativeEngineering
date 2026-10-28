// requires: canned_goods
// Canned Goods names, moved from the pack's client_scripts/namechanges.js 2026-10-27, rows unchanged.
ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	var i = global.item
	e.renameItem(i.canned_rotten_flesh, 'Canned Tough Jerky')
	e.renameItem(i.canned_spider_eyes, 'Canned Night Berries')
})
