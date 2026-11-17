// Gear effect rows (plan_cb_polish.md 1): which armor set or tool grants which effect, with the jar that owns the gear.
// The frameworks (global.armorEffects, global.toolEffects, the tick appliers and tooltip lines) are Creative Common's
// (kubejs_startup/creativecommon/effects/). Moved here from Creative Brewing, where the echo tool rows leaked without
// this jar (scratch r1106cbo SQ-CBO-01). Equipment rebuild + restart. Each row needs every mod it names: the gear's own
// mod (dict keys resolve to it) and the effect's mod.
;(function () {
	var i = global.item
	var ef = global.effect

	// ═══════════════════════════ set bonuses ═══════════════════════════════════

	// Echo armor (create_deep_dark:echo_armor_*) is DELIBERATELY NOT registered here,
	// even though it's the exact case this framework was modeled on. It already has a
	// working Night Vision + Phantom Bane bonus via Creative Common's
	// EchoArmorEffectProcedureMixin + EchoArmorTooltipMixin (see [[echo-armor-set-bonus]]),
	// and this framework can't cleanly take over: EchoArmorEffectProcedure is Create:
	// Deep Dark's own hardcoded MCreator event listener that unconditionally applies
	// Resistance + Strength whenever the full set is worn — a KubeJS PlayerEvents.tick
	// handler has no way to CANCEL that, only ADD more effects on top of it, so
	// registering echo here would stack Night Vision + Phantom Bane ON TOP OF (not
	// instead of) CDD's original Resistance + Strength. Same problem for the tooltip:
	// CDD's item already bakes in its own "With full set on:" block (corrected to say
	// Night Vision / Phantom Bane by the mixin) — a second block from this framework
	// would just duplicate it underneath. The mixin pair can REDIRECT the existing
	// effect/tooltip in place; this framework can only ADD a new one, so for echo the
	// mixins stay authoritative.

	// Each row needs every mod it names: the armor's own mod (dict keys resolve to it) and the effect's mod.
	if (global.has('immersiveengineering')) {
		if (global.has('immersive_armors')) {
			global.armorEffects.set(
				[i.ia_wooden_helmet, i.ia_wooden_chestplate, i.ia_wooden_leggings, i.ia_wooden_boots],
				[{ effect: ef.flammable }]
			)
		}

		if (global.has('everythingcopper')) {
			global.armorEffects.set(
				[i.copper_helmet, i.copper_chestplate, i.copper_leggings, i.copper_boots],
				[{ effect: ef.conductive }]
			)
		}

		global.armorEffects.set(
			[i.iron_helmet, i.iron_chestplate, i.iron_leggings, i.iron_boots],
			[{ effect: ef.conductive }]
		)
	}

	var goldenEffects = [{ effect: ef.luck }]
	if (global.has('immersiveengineering')) goldenEffects.push({ effect: ef.conductive })
	global.armorEffects.set([i.golden_helmet, i.golden_chestplate, i.golden_leggings, i.golden_boots], goldenEffects)

	global.armorEffects.set(
		[i.diamond_helmet, i.diamond_chestplate, i.diamond_leggings, i.diamond_boots],
		[{ effect: ef.speed }]
	)

	var netheriteEffects = [{ effect: ef.fire_resistance }]
	if (global.has('alexsmobs')) {
		netheriteEffects.push({ effect: ef.lava_vision }, { effect: ef.knockback_resistance })
	}
	global.armorEffects.set([i.netherite_helmet, i.netherite_chestplate, i.netherite_leggings, i.netherite_boots], netheriteEffects)

	if (global.has('felsic_gear')) {
		var felsicEffects = []
		if (global.has('better_beacons')) felsicEffects.push({ effect: ef.long_reach })
		felsicEffects.push({ effect: ef.jump_boost }, { effect: ef.magic_resistance })
		global.armorEffects.set([i.felsic_helmet, i.felsic_chestplate, i.felsic_leggings, i.felsic_boots], felsicEffects)
	}

	// dragonloot:upgraded_dragon_chestplate (elytra-capable, see [[dragon-elytra-rocket-takeoff]])
	// replaces the base chestplate in the chest slot - a player wears one or the other, never both -
	// so the bonus is registered twice, once per chestplate variant, sharing the same effects list.
	// dragon_horse_armor (dragonloot:dragon_horse_armor) is a HorseArmorItem worn by an AbstractHorse,
	// not player equipment — see mount() above for why it needs its own path. Regeneration II, Health
	// Boost II, Resistance II (potions/effects #4).
	if (global.has('dragonloot')) {
		var dragonEffects = [
			{ effect: ef.resistance },
			{ effect: ef.magic_resistance },
			{ effect: ef.fire_resistance },
		]
		if (global.has('alexsmobs')) {
			dragonEffects.push({ effect: ef.poison_resistance }, { effect: ef.knockback_resistance })
		}
		if (global.has('botania')) dragonEffects.push({ effect: ef.feather_feet })
		if (global.has('better_beacons')) dragonEffects.push({ effect: ef.long_reach })
		global.armorEffects.set([i.dragon_helmet, i.dragon_chestplate, i.dragon_leggings, i.dragon_boots], dragonEffects)
		global.armorEffects.set([i.dragon_helmet, i.upgraded_dragon_chestplate, i.dragon_leggings, i.dragon_boots], dragonEffects)

		global.armorEffects.mount(i.dragon_horse_armor, [
			{ effect: ef.regeneration, amplifier: 1 },
			{ effect: ef.health_boost, amplifier: 1 },
			{ effect: ef.resistance, amplifier: 1 },
		])
	}

	// ═══════════════════════════ tool bonuses ═══════════════════════════════════

	// Echo tools (creativeequipment:echo_pickaxe/axe/shovel/hoe, see
	// [[echo-tier-tools]]) mirror create_deep_dark:echo_sword's own on-hit Weakness II +
	// Darkness II (5 sec.). Previously hardcoded in its own dedicated
	// server_scripts/magic/echo_tool_effects.js + manual .tooltip() lines in
	// startup_scripts/items/echo_tools.js - both replaced by this registration now that
	// the generic framework exists (see [[echo-armor-set-bonus]] for why echo ARMOR's set
	// bonus, by contrast, stays a mixin - that constraint doesn't apply here since the
	// on-hit tool effect was always our own KubeJS code, never CDD's).
	var echoHit = [
		{ effect: ef.weakness, amplifier: 1, duration: 100 },
		{ effect: ef.darkness, amplifier: 1, duration: 100 },
	]
	var discombobulate = [
		{ effect: ef.nausea, amplifier: 1, duration: 100 },
		{ effect: ef.hunger, amplifier: 1, duration: 100 },
	]
	if (global.has('supplementaries')) discombobulate.push({ effect: 'supplementaries:overencumbered', amplifier: 1, duration: 100 })
	if (global.has('vinery')) discombobulate.push({ effect: 'vinery:trippy', amplifier: 1, duration: 110 })
	if (global.has('alexsmobs')) {
		discombobulate.push({ effect: ef.earthquake, amplifier: 1, duration: 10 },
			{ effect: ef.fear, amplifier: 1, duration: 10 })
	}

	global.toolEffects.onHit(i.echo_pickaxe, echoHit)
	global.toolEffects.onHit(i.echo_axe, echoHit)
	global.toolEffects.onHit(i.echo_shovel, echoHit)
	global.toolEffects.onHit(i.echo_hoe, echoHit)

	// ═══════════════════════════ dragon ═══════════════════════════════════
	// Dragon Loot's tools; rows naming another optional mod's effect sit in its own guard.
	if (global.has('dragonloot')) {
		global.toolEffects.onHit(i.dragon_sword, { effect: ef.weakness, amplifier: 2, duration: 200 })
		if (global.has('alexsmobs')) global.toolEffects.onHit(i.dragon_sword, { effect: ef.exsanguination, amplifier: 1, duration: 150 })
		if (global.has('botania')) global.toolEffects.held(i.dragon_sword, { effect: ef.soul_cross, amplifier: 1, duration: 50 })

		global.toolEffects.onHit(i.dragon_axe, { effect: ef.levitation, amplifier: 1, duration: 60 })
		if (global.has('alexsmobs')) global.toolEffects.onHit(i.dragon_axe, { effect: ef.exsanguination, amplifier: 1, duration: 150 })
		global.toolEffects.held(i.dragon_axe, { effect: ef.jump_boost, amplifier: 2, duration: 100 })

		global.toolEffects.onHit(i.dragon_shovel, discombobulate)
		if (global.has('alexsmobs')) global.toolEffects.held(i.dragon_shovel, { effect: ef.knockback_resistance, amplifier: 1, duration: 50 })

		global.toolEffects.onHit(i.dragon_trident, { effect: ef.mining_fatigue, amplifier: 1, duration: 60 })
		global.toolEffects.held(i.dragon_trident, { effect: ef.slow_falling, amplifier: 1, duration: 50 })

		global.toolEffects.held(i.dragon_bow, { effect: ef.invisibility, amplifier: 0, duration: 60 })
		global.toolEffects.onHit(i.dragon_bow, { effect: ef.slowness, amplifier: 3, duration: 50 })
		global.toolEffects.onHit(i.dragon_bow, { effect: ef.glowing, amplifier: 0, duration: 500 })

		global.toolEffects.held(i.dragon_crossbow, { effect: ef.resistance, amplifier: 1, duration: 60 })
		if (global.has('alexsmobs')) global.toolEffects.onHit(i.dragon_crossbow, { effect: ef.exsanguination, amplifier: 1, duration: 150 })
		global.toolEffects.onHit(i.dragon_crossbow, { effect: ef.weakness, amplifier: 2, duration: 250 })
	}
})()
