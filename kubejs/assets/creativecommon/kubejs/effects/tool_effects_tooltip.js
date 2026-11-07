// Creative Common since 2026-11-06 (plan_cb_polish.md 2), from Creative Brewing; before that the pack's client_scripts/tool_effects_tooltip.js.
// Tooltip renderer for the global.toolEffects framework (Common startup effects/
// tool_effects.js). Effects are actually applied server-side by
// Common server effects/tool_effects.js — this file only draws the matching tooltip
// lines, styled after (and for held(), literally sharing code with) armor_effects.js's
// own tooltip renderer.
//
// A separate file from Common client effects/armor_effects_tooltip.js because it reads a
// different registry (global.toolEffects, not global.armorEffects.PIECES/SETS), even
// though held() borrows armor_effects.js's tooltip builder directly.

ItemEvents.tooltip(e => {
	var TE = global.toolEffects
	if (!TE) { console.warn('[tool_effects_tooltip] global.toolEffects missing - Common startup effects/tool_effects.js; rows in Equipment gear_effects.js not loaded?'); return }

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
