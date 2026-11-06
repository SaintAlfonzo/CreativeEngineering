// Moved from the pack's client_scripts/tool_effects_tooltip.js 2026-11-01 (creative_brewing.md 4.5), unchanged.
// Tooltip renderer for the global.toolEffects framework (startup_scripts/func/
// tool_effects.js). Effects are actually applied server-side by
// CB server creativeengineering/tool_effects.js — this file only draws the matching tooltip
// lines, styled after (and for held(), literally sharing code with) armor_effects.js's
// own tooltip renderer.
//
// A separate file from CB client creativeengineering/armor_effects_tooltip.js because it reads a
// different registry (global.toolEffects, not global.armorEffects.PIECES/SETS), even
// though held() borrows armor_effects.js's tooltip builder directly.

ItemEvents.tooltip(e => {
	var TE = global.toolEffects
	if (!TE) { console.warn('[tool_effects_tooltip] global.toolEffects missing - CB startup creativeengineering/tool_effects.js not loaded?'); return }

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
