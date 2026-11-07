// The creativecommon: potions of vanilla effects (plan_cb_polish.md 2, 2026-11-06: every potion and effect the Creative
// mods own is registered from Creative Common; these were creativebrewing:*, before that kubejs:*, all remapped by
// data/creativecommon/swaps/effects_potions.json and, for ids saved in item NBT, PotionByNameRemapMixin).
// Brewing mixes stay with Creative Brewing (its potions.js reads global.creativePotions for which steps exist).
// Potions of modded effects (Better Beacons, Magic Resistance) are Java: effect/CommonPotions.java.
//
// Vanilla effects only: KubeJS evaluates this callback in one early pass, before any other mod's
// DeferredRegister<MobEffect> fires, so a modded effect id resolves to null here whatever registry API is used.

// name -> effect and timings. dur / long: ticks; strong: [ticks, amplifier]. Every name gets creativecommon:<name>,
// plus long_<name> / strong_<name> when given. (A function scope: the whole jar is one script scope.)
global.creativePotions = (function () {
	var S = 20 // ticks per second
	return {
	resistance:     { effect: 'minecraft:resistance',     dur: 180 * S, long: 480 * S, strong: [90 * S, 1] },
	health_boost:   { effect: 'minecraft:health_boost',   dur: 180 * S, long: 480 * S, strong: [90 * S, 1] },
	absorption:     { effect: 'minecraft:absorption',     dur: 120 * S, long: 300 * S, strong: [120 * S, 1] },
	glowing:        { effect: 'minecraft:glowing',        dur: 90 * S,  long: 240 * S },
	saturation:     { effect: 'minecraft:saturation',     dur: 5 },
	wither:         { effect: 'minecraft:wither',         dur: 40 * S,  long: 90 * S,  strong: [20 * S, 1] },
	levitation:     { effect: 'minecraft:levitation',     dur: 40 * S,  long: 90 * S,  strong: [20 * S, 1] },
	mining_fatigue: { effect: 'minecraft:mining_fatigue', dur: 90 * S,  long: 240 * S, strong: [30 * S, 1] },
	hunger:         { effect: 'minecraft:hunger',         dur: 30 * S,  long: 60 * S,  strong: [15 * S, 1] },
	nausea:         { effect: 'minecraft:nausea',         dur: 30 * S,  long: 60 * S },
	blindness:      { effect: 'minecraft:blindness',      dur: 30 * S,  long: 60 * S },
	darkness:       { effect: 'minecraft:darkness',       dur: 30 * S,  long: 60 * S },
	}
})()

StartupEvents.registry('potion', event => {
	const EFFECTS = Java.loadClass('net.minecraft.core.Registry').MOB_EFFECT
	const ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

	Object.keys(global.creativePotions).forEach(name => {
		const p = global.creativePotions[name]
		const eff = EFFECTS.get(new ResourceLocation(p.effect))
		if (!eff) {
			console.warn(`[creativecommon potions] effect '${p.effect}' not registered, skipped ${name}`)
			return
		}
		event.create('creativecommon:' + name).effect(eff, p.dur, 0)
		if (p.long) event.create('creativecommon:long_' + name).effect(eff, p.long, 0)
		if (p.strong) event.create('creativecommon:strong_' + name).effect(eff, p.strong[0], p.strong[1])
	})
})
