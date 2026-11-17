// requires: creativedelight
// Vanilla food tooltips, moved from the pack's client_scripts/tooltips.js 2026-10-27, rows unchanged.
// A tooltip line in a col role (global.tooltips, Creative Common's client _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	if (!global.CD) return
	e.add(global.item.poisonous_potato, tipLine('Can be fed to a baby animal for a chance to stunt their growth', 'text'))
})
