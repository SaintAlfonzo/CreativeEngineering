// Vanilla food tooltips, moved from the pack's client_scripts/tooltips.js 2026-10-27, rows unchanged.
ItemEvents.tooltip(e => {
	if (!global.CD) return
	e.add(global.item.poisonous_potato, Text.gray('Can be fed to a baby animal for a chance to stunt their growth'))
})
