// priority: -100
// Moved from the pack's startup_scripts/func/tool_effects.js 2026-11-01 (creative_brewing.md 4.5).
// Startup script in the CB jar: runs after the pack folder and CD; CB rebuild + restart.
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

// ═══════════════════════════ tool bonuses ═══════════════════════════════════
var i = global.item

// Echo tools (creativeengineering:echo_pickaxe/axe/shovel/hoe, see
// [[echo-tier-tools]]) mirror create_deep_dark:echo_sword's own on-hit Weakness II +
// Darkness II (5 sec.). Previously hardcoded in its own dedicated
// server_scripts/magic/echo_tool_effects.js + manual .tooltip() lines in
// startup_scripts/items/echo_tools.js - both replaced by this registration now that
// the generic framework exists (see [[echo-armor-set-bonus]] for why echo ARMOR's set
// bonus, by contrast, stays a mixin - that constraint doesn't apply here since the
// on-hit tool effect was always our own KubeJS code, never CDD's).
var echoHit = [
	{ effect: 'minecraft:weakness', amplifier: 1, duration: 100 },
	{ effect: 'minecraft:darkness', amplifier: 1, duration: 100 },
]
var discombobulate = [
	{ effect: 'minecraft:nausea', amplifier: 1, duration: 100 },
	{ effect: 'minecraft:hunger', amplifier: 1, duration: 100 },
]
if (global.has('supplementaries')) discombobulate.push({ effect: 'supplementaries:overencumbered', amplifier: 1, duration: 100 })
discombobulate.push({ effect: 'vinery:trippy', amplifier: 1, duration: 110 })
if (global.has('alexsmobs')) {
	discombobulate.push({ effect: 'alexsmobs:earthquake', amplifier: 1, duration: 10 },
		{ effect: 'alexsmobs:fear', amplifier: 1, duration: 10 })
}

global.toolEffects.onHit(i.echo_pickaxe, echoHit)
global.toolEffects.onHit(i.echo_axe, echoHit)
global.toolEffects.onHit(i.echo_shovel, echoHit)
global.toolEffects.onHit(i.echo_hoe, echoHit)

// ═══════════════════════════ dragon ═══════════════════════════════════
// Dragon Loot's tools; rows naming another optional mod's effect sit in its own guard.
if (global.has('dragonloot')) {
	global.toolEffects.onHit(i.dragon_sword, { effect: 'minecraft:weakness', amplifier: 2, duration: 200 })
	if (global.has('alexsmobs')) global.toolEffects.onHit(i.dragon_sword, { effect: 'alexsmobs:exsanguination', amplifier: 1, duration: 150 })
	if (global.has('botania')) global.toolEffects.held(i.dragon_sword, { effect: 'botania:soul_cross', amplifier: 1, duration: 50 })

	global.toolEffects.onHit(i.dragon_axe, { effect: 'minecraft:levitation', amplifier: 1, duration: 60 })
	if (global.has('alexsmobs')) global.toolEffects.onHit(i.dragon_axe, { effect: 'alexsmobs:exsanguination', amplifier: 1, duration: 150 })
	global.toolEffects.held(i.dragon_axe, { effect: 'minecraft:jump_boost', amplifier: 2, duration: 100 })

	global.toolEffects.onHit(i.dragon_shovel, discombobulate)
	if (global.has('alexsmobs')) global.toolEffects.held(i.dragon_shovel, { effect: 'alexsmobs:knockback_resistance', amplifier: 1, duration: 50 })

	global.toolEffects.onHit(i.dragon_trident, { effect: 'minecraft:mining_fatigue', amplifier: 1, duration: 60 })
	global.toolEffects.held(i.dragon_trident, { effect: 'minecraft:slow_falling', amplifier: 1, duration: 50 })

	global.toolEffects.held(i.dragon_bow, { effect: 'minecraft:invisibility', amplifier: 0, duration: 60 })
	global.toolEffects.onHit(i.dragon_bow, { effect: 'minecraft:slowness', amplifier: 3, duration: 50 })
	global.toolEffects.onHit(i.dragon_bow, { effect: 'minecraft:glowing', amplifier: 0, duration: 500 })

	global.toolEffects.held(i.dragon_crossbow, { effect: 'minecraft:resistance', amplifier: 1, duration: 60 })
	if (global.has('alexsmobs')) global.toolEffects.onHit(i.dragon_crossbow, { effect: 'alexsmobs:exsanguination', amplifier: 1, duration: 150 })
	global.toolEffects.onHit(i.dragon_crossbow, { effect: 'minecraft:weakness', amplifier: 2, duration: 250 })
}
