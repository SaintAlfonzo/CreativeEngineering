// Creative Common since 2026-11-06 (plan_cb_polish.md 2), from Creative Brewing; before that the pack's client_scripts/armor_effects_tooltip.js.
// Tooltip renderer for the global.armorEffects framework (Common startup effects/
// armor_effects.js). Effects are actually applied server-side by
// Common server effects/armor_effects.js's PlayerEvents.tick — this file only draws
// the matching tooltip lines, styled after Create: Deep Dark's echo-armor set-bonus
// tooltip (patcherMod's EchoArmorTooltipMixin).
//
// A separate ItemEvents.tooltip file (not folded into client_scripts/tooltips.js)
// because it needs to run AFTER global.armorEffects.piece()/.set() calls have
// populated PIECES/SETS, and because it emits raw pre-formatted strings (built by
// _buildTooltipLines) rather than tooltips.js's own {text,color} object convention -
// mixing the two styles in one giant handler would be more confusing than one small
// dedicated file.

ItemEvents.tooltip(e => {
	var AE = global.armorEffects
	if (!AE) { console.warn('[armor_effects_tooltip] global.armorEffects missing - Common startup effects/armor_effects.js; rows in Equipment gear_effects.js not loaded?'); return }

	Object.keys(AE.PIECES).forEach(itemId => {
		var effects = AE.PIECES[itemId]
		if (!effects.length) return
		e.add(itemId, AE._buildTooltipLines('When worn', effects))
	})

	// mount() entries (e.g. dragon_horse_armor) render the same "When worn:" tooltip as
	// piece() — the tooltip doesn't care that the effect is actually applied to whatever
	// mob wears the item rather than to the player holding it in their inventory.
	Object.keys(AE.MOUNTS).forEach(itemId => {
		var effects = AE.MOUNTS[itemId]
		if (!effects.length) return
		e.add(itemId, AE._buildTooltipLines('When worn', effects))
	})

	// A piece that belongs to more than one registered set (e.g. an "upgraded" variant
	// of one slot, registered as its own separate .set() alongside the base variant so
	// the OTHER 3 slots still work no matter which one is worn — see the dragon_chestplate
	// / upgraded_dragon_chestplate pair at the bottom of Common startup effects/
	// armor_effects.js) would otherwise get the exact same "With full set on:" block
	// appended once per matching group. Dedupe by item + exact rendered lines so shared
	// pieces only ever show it once.
	var added = {}
	AE.SETS.forEach(group => {
		if (!group.effects.length) return
		var lines = AE._buildTooltipLines('With full set on', group.effects)
		var sig = lines.join('\n')
		group.ids.forEach(itemId => {
			var key = itemId + '|' + sig
			if (added[key]) return
			added[key] = true
			e.add(itemId, lines)
		})
	})
})
