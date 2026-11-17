// requires: creativedelight
// requires: oceanic_delight
// Oceanic Delight net tooltip (ocean_delight.md 5.1): blocks are the fishing_net tags, drops the biome tables. A
// tooltip line in a col role (global.tooltips, Creative Common's client _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('oceanic_delight')) return
	e.add(global.item.fishing_net, [
		tipLine('Use on kelp, seagrass, sea grapes or pickerelweed to search them', 'hint'),
		tipLine('Shrimp in oceans that are not cold, coral in warm oceans, Caviar in rivers', 'text'),
		tipLine('Oceans can give Nautilus Shells, rarely a Heart of the Sea', 'text'),
	])
})
