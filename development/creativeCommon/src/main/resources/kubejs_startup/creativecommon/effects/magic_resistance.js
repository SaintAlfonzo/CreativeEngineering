// Creative Common since 2026-11-06 (plan_cb_polish.md 2: every potion and effect the Creative mods own is registered
// from Common, under creativecommon:; was creativebrewing:magic_resistance, remapped by
// data/creativecommon/swaps/effects_potions.json). Common rebuild + restart.
// creativecommon:magic_resistance — a brand-new, pack-original MobEffect (the first
// one this pack defines itself, rather than reusing vanilla/Alex's Mobs/Better Beacons).
//
// Registering the effect itself needs no Java: unlike better_beacons:long_reach/phantom_bane
// (see effects/potions.js's header comment + Common's CommonPotions), there's no
// cross-mod registry-timing problem here — this effect doesn't depend on any OTHER mod's
// MobEffect registry having already fired, it just needs to exist at all, which KubeJS's own
// 'mob_effect' registry event handles directly.
//
// What KubeJS CAN'T do is the actual damage reduction: EntityEvents.hurt in this build wraps
// LivingHurtEvent read-only (getDamage() only, no setAmount()/cancel — confirmed by
// decompiling kubejs-forge-1902.6.2-build.73.jar), so reducing incoming damage needs a real
// Forge listener. That half lives in Creative Common:
//   development/creativeCommon/.../effect/MagicResistanceEffect.java — @SubscribeEvent on LivingHurtEvent,
//   reduces damage from sources vanilla already flags as magic (DamageSource#isMagic() —
//   potion/instant-damage, evoker fangs, conduit, dragon's breath, etc.) by 20% per
//   amplifier level (same scaling as vanilla Resistance), full immunity at amplifier 4+.
//
// The potions (base, long, strong) are registered in Java by Creative Common's
// effect/CommonPotions.java (a real DeferredRegister<Potion>, the same dance
// the Better Beacons rows in it use: KubeJS's StartupEvents.registry('potion', ...) callback runs
// too early to see ANY MobEffect, including this one). Their brewing mixes are in
// Creative Brewing's potions.js (CB_POTION_MIXES, amethyst shard placeholder reagent). The effect can
// also be applied directly (/effect give, global.armorEffects, etc.).

StartupEvents.registry('mob_effect', event => {
	const SimpleColor = Java.loadClass('dev.latvian.mods.rhino.mod.util.color.SimpleColor')

	event.create('creativecommon:magic_resistance', 'basic')
		.displayName(Text.of('Magic Resistance'))
		.beneficial()
		.color(new SimpleColor(0x8a2be2)) // blueviolet - arcane-coded, distinct from vanilla Resistance's grey
})
