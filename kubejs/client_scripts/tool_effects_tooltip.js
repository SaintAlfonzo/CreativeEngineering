// Tooltip renderer for the global.toolEffects framework (startup_scripts/func/
// tool_effects.js). Effects are actually applied server-side by
// server_scripts/magic/tool_effects.js — this file only draws the matching tooltip
// lines, styled after (and for held(), literally sharing code with) armor_effects.js's
// own tooltip renderer.
//
// A separate file from client_scripts/armor_effects_tooltip.js because it reads a
// different registry (global.toolEffects, not global.armorEffects.PIECES/SETS), even
// though held() borrows armor_effects.js's tooltip builder directly.

ItemEvents.tooltip(e => {
	var TE = global.toolEffects
	if (!TE) { console.warn('[tool_effects_tooltip] global.toolEffects missing - startup_scripts/func/tool_effects.js not loaded?'); return }

	Object.keys(TE.ON_HIT).forEach(itemId => {
		var effects = TE.ON_HIT[itemId]
		if (!effects.length) return
		e.add(itemId, TE._buildOnHitTooltipLines(effects))
	})

	Object.keys(TE.HELD).forEach(itemId => {
		var effects = TE.HELD[itemId]
		if (!effects.length) return
		e.add(itemId, global.armorEffects._buildTooltipLines('While held', effects))
	})
})
