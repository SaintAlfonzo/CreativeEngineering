// requires: brewinandchewin
// Brewin' and Chewin' item tooltips. Moved from the pack's client_scripts/tooltips.js 2026-11-01 (creative_brewing.md
// 4.5), rows unchanged; the helpers and palette are read through global.tooltips at event time.
ItemEvents.tooltip(e => {
	if (!global.CB || !global.CB.on('brewinandchewin')) return
	var T = global.tooltips
	if (!T) { console.error('[creativebrewing] global.tooltips missing: the pack\'s client_scripts/tooltips.js did not run'); return }
	var addTooltips = T.addTooltips, removeTooltipKey = T.removeTooltipKey
	var col = T.col
	var i = global.item

	// Brewin' and Chewin' keg temperature blocks (alcohol.md 8.3): each source within 2 blocks of a keg counts 1
	addTooltips(e,
		[i.heating_cask,
			['Keg heat source within 2 blocks: on its own, one makes a keg Warm and two make it Hot', col.uncm],
			['Heats a cooking pot like a stove', col.norm],
			['Use a water bucket or bottle to vent steam', col.norm]],
		[i.ice_crate,
			['Keg cold source within 2 blocks: on its own, one makes a keg Chilly and two make it Cold', col.uncm]]
	);
})
