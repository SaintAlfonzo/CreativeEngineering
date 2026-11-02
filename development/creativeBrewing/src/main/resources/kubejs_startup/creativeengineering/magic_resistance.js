// Moved from the pack's startup_scripts/magic/magic_resistance.js 2026-11-01 (creative_brewing.md 4.5).
// Startup script in the CB jar: runs after the pack folder and CD; CB rebuild + restart.
// creativeengineering:magic_resistance — a brand-new, pack-original MobEffect (the first
// one this pack defines itself, rather than reusing vanilla/Alex's Mobs/Better Beacons).
//
// Registering the effect itself needs no Java: unlike better_beacons:long_reach/phantom_bane
// (see cooking/potions.js's header comment + patcherMod's CEPotions), there's no
// cross-mod registry-timing problem here — this effect doesn't depend on any OTHER mod's
// MobEffect registry having already fired, it just needs to exist at all, which KubeJS's own
// 'mob_effect' registry event handles directly.
//
// What KubeJS CAN'T do is the actual damage reduction: EntityEvents.hurt in this build wraps
// LivingHurtEvent read-only (getDamage() only, no setAmount()/cancel — confirmed by
// decompiling kubejs-forge-1902.6.2-build.73.jar), so reducing incoming damage needs a real
// Forge listener. That half lives in patcherMod:
//   development/patcherMod/.../potion/MagicResistanceEffect.java — @SubscribeEvent on LivingHurtEvent,
//   reduces damage from sources vanilla already flags as magic (DamageSource#isMagic() —
//   potion/instant-damage, evoker fangs, conduit, dragon's breath, etc.) by 20% per
//   amplifier level (same scaling as vanilla Resistance), full immunity at amplifier 4+.
//
// The potions (base, long, strong) are registered in Java by patcherMod's
// potion/CEPotions.java (a real DeferredRegister<Potion>, the same dance
// the Better Beacons rows in it use: KubeJS's StartupEvents.registry('potion', ...) callback runs
// too early to see ANY MobEffect, including this one). Their brewing mixes are in
// cooking/potions.js (CE_POTION_MIXES, amethyst shard placeholder reagent). The effect can
// also be applied directly (/effect give, global.armorEffects, etc.).

StartupEvents.registry('mob_effect', event => {
	const SimpleColor = Java.loadClass('dev.latvian.mods.rhino.mod.util.color.SimpleColor')

	event.create('creativeengineering:magic_resistance', 'basic')
		.displayName(Text.of('Magic Resistance'))
		.beneficial()
		.color(new SimpleColor(0x8a2be2)) // blueviolet - arcane-coded, distinct from vanilla Resistance's grey
})
