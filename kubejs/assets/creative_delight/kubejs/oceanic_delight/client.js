// requires: oceanic_delight
// Oceanic Delight net tooltip (ocean_delight.md 5.1): blocks are the fishing_net tags, drops the biome tables. Moved
// from the pack's client_scripts/tooltips.js 2026-10-27, rows unchanged.
ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('oceanic_delight')) return
	e.add(global.item.fishing_net, [
		Text.green('Use on kelp, seagrass, sea grapes or pickerelweed to search them'),
		Text.gray('Shrimp in oceans that are not cold, coral in warm oceans, Caviar in rivers'),
		Text.gray('Oceans can give Nautilus Shells, rarely a Heart of the Sea'),
	])
})
