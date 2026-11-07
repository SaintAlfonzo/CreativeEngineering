// requires: creativedelight
// requires: domesticationinnovation
// Domestication Innovation food tooltips, moved from the pack's client_scripts/tooltips.js 2026-10-27, rows unchanged.
ItemEvents.tooltip(e => {
	if (!global.CD) return
	var i = global.item
	e.add(i.rotten_apple, Text.darkPurple('Turns a horse into a zombie horse'))
	e.add(i.sinister_carrot, Text.darkPurple('Turns a zombie horse into a skeleton horse'))
})
