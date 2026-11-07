// priority: -100
// Creative Common since 2026-11-06 (plan_cb_polish.md 2: the effect frameworks live in Common, the rows with the jar
// that owns the gear: Creative Equipment's kubejs_startup/creativeequipment/gear_effects.js). Was Creative Brewing's,
// before that the pack's startup_scripts/func/tool_effects.js. Common rebuild + restart.
//   ^ load after _lib/item_dict.js (priority 0) — same load-order gotcha as
//   armor_effects.js / tune_tools.js / tune_armor.js (see [[tune-tools-func]]).
// ─────────────────────────────────────────────────────────────────────────────
//  Tool Effects — sibling framework to armor_effects.js (this folder), for
//  potion effects granted by a TOOL rather than armor. Reuses that file's private
//  effect-spec helpers (global.armorEffects._resolveEffect/_describe) since they're
//  fully generic — lazy MobEffect lookup + name/color/roman-numeral formatting, no
//  armor-specific logic at all. See armor_effects.js's own header for why resolution
//  has to be lazy (modded MobEffects aren't registered yet when startup scripts run).
//
//  This file only holds the DATA + registration API, same split as armor_effects.js:
//    CB client creativeengineering/tool_effects_tooltip.js  — ItemEvents.tooltip, builds the lines
//    CB server creativeengineering/tool_effects.js    — EntityEvents.hurt (onHit) + PlayerEvents.tick (held)
//
//  global.toolEffects.onHit(itemId, effects)
//    Effects apply to whatever the wielder HITS in melee, once per hit, each for its
//    own `duration` (in ticks — REQUIRED here, unlike every other effects list in this
//    codebase, since on-hit effects are a one-shot MobEffectInstance rather than a
//    continuously-refreshed one — there's no "while held" tick to refresh them from).
//    Tooltip header: "§7On hit, inflicts to target:" — same wording CDD's echo_sword
//    tooltip uses (this generalizes what server_scripts/magic/echo_tool_effects.js used
//    to hardcode for the echo tools — see the registration at the bottom of this file).
//
//  global.toolEffects.held(itemId, effects)
//    Effects apply to the WIELDER continuously while this item sits in either hand
//    (main or off) — same "(infinite)", reapplied-every-tick idiom as armor_effects.js,
//    and in fact reuses its exact tooltip builder for that reason. Tooltip header:
//    "§7While held:"
//
//  effects (held): array of up to 16 { effect, amplifier?, name?, color? } — identical
//  shape and defaults to armor_effects.js's piece()/set(), see there for field docs.
//
//  effects (onHit): array of up to 16 { effect, duration, amplifier?, name?, color? } —
//  same fields plus the required `duration` above. A spec missing it is dropped with a
//  console warning rather than silently living forever.
// ─────────────────────────────────────────────────────────────────────────────

console.log('Tool Effects framework loading')

;(function () {
	const MAX_EFFECTS = 16

	const ON_HIT = {} // itemId -> effects[]  (from .onHit())
	const HELD = {}   // itemId -> effects[]  (from .held())

	// onHit()/held() both accept either one spec object or an array of them, and can be
	// called more than once per item to add more effects incrementally (each call's
	// specs are appended to that item's existing list, not overwritten) - see the
	// per-effect onHit() calls on dragon_sword/dragon_axe below.
	function toArray(effects) {
		if (effects == null) return []
		return Array.isArray(effects) ? effects : [effects]
	}

	function clampEffects(context, effects) {
		effects = effects || []
		if (effects.length > MAX_EFFECTS) {
			console.warn('[toolEffects] ' + context + ': ' + effects.length + ' effects given, only the first ' + MAX_EFFECTS + ' are used')
			effects = effects.slice(0, MAX_EFFECTS)
		}
		return effects
	}

	// Same two-tone raw-string line style as armor_effects.js's buildTooltipLines, but
	// with an actual finite duration instead of "(infinite)" - onHit effects aren't
	// continuously refreshed, so the real configured duration is what the target sees.
	function buildOnHitTooltipLines(effects) {
		var lines = ['§7On hit, inflicts to target:']
		effects.forEach(spec => {
			var d = global.armorEffects._describe(spec)
			var seconds = spec.duration / 20
			var durationText = (seconds === Math.floor(seconds) ? seconds : seconds.toFixed(1)) + ' sec.'
			lines.push('§7- §' + d.color + d.name + ' §7(' + durationText + ')')
		})
		return lines
	}

	global.toolEffects = {
		MAX_EFFECTS: MAX_EFFECTS,
		ON_HIT: ON_HIT, // read by CB client creativeengineering/tool_effects_tooltip.js + CB server creativeengineering/tool_effects.js
		HELD: HELD,     // read by CB client creativeengineering/tool_effects_tooltip.js + CB server creativeengineering/tool_effects.js

		onHit(itemId, effects) {
			if (!itemId) { console.warn('[toolEffects] onHit() called without an item id'); return }
			effects = toArray(effects).filter(spec => {
				if (spec.duration == null) {
					console.warn('[toolEffects] onHit ' + itemId + ': effect "' + spec.effect + '" has no duration - dropped')
					return false
				}
				return true
			})
			ON_HIT[itemId] = clampEffects('onHit ' + itemId, (ON_HIT[itemId] || []).concat(effects))
		},

		held(itemId, effects) {
			if (!itemId) { console.warn('[toolEffects] held() called without an item id'); return }
			HELD[itemId] = clampEffects('held ' + itemId, (HELD[itemId] || []).concat(toArray(effects)))
		},

		// internal, exposed for the tooltip consumer - not meant to be called directly
		_buildOnHitTooltipLines: buildOnHitTooltipLines,
	}
})()
