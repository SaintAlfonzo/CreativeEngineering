// Creative Common since 2026-11-06 (plan_cb_polish.md 2), from Creative Brewing; before that the pack's server_scripts/magic/tool_effects.js.
// Applies global.toolEffects (Common startup effects/tool_effects.js; rows in Equipment gear_effects.js) onHit()/held()
// effects. Tooltip text for the same data lives in Common client effects/tool_effects_tooltip.js.
//
// onHit fires once per hit (EntityEvents.hurt, keyed to the attacker's main-hand item)
// and applies each effect for its own configured duration — a one-shot
// MobEffectInstance, not refreshed, since there's no "still holding it" tick to refresh
// it from once the hit has already landed. Replaces the old dedicated
// server_scripts/magic/echo_tool_effects.js, which did the same EntityEvents.hurt dance
// hardcoded to just the 4 echo tools — see that reasoning (SwordItem/DiggerItem
// override hurtEnemy for their own durability call, HoeItem doesn't override it at all,
// so piggybacking on the hurt event instead of Item#hurtEnemy avoids re-implementing
// that per-tool logic) which still applies here.
//
// held is (re)applied every tick to whichever hand(s) hold a registered item, same
// continuously-refreshed "(infinite)" idiom as armor_effects.js's PlayerEvents.tick.

// Named $ToolMobEffectInstance, not $MobEffectInstance - ALL server_scripts share one
// top-level JS scope in this KubeJS/Rhino build, and server_scripts/magic/
// armor_effects.js already declares its own top-level `const $MobEffectInstance`.
// Redeclaring the same const name here previously threw "redeclaration of const
// $MobEffectInstance" and knocked whichever of the two files loaded second out
// entirely (see [[rhino-fieldandmethods-gotcha]]).
const $ToolMobEffectInstance = Java.loadClass('net.minecraft.world.effect.MobEffectInstance')
const HELD_REFRESH_TICKS = 40 // 2 sec — same buffer-against-a-tick-hiccup idiom as armor_effects.js

EntityEvents.hurt(event => {
	let attacker
	try { attacker = event.getSource().getActual() } catch (err) { return } // KubeJS renames DamageSource.getEntity() to getActual()
	if (!attacker) return

	let heldId
	try { heldId = String(attacker.getMainHandItem().getItem().getId()) } catch (err) { return }
	// ^ getMainHandItem() only exists on LivingEntity; a non-living damage source (e.g. a
	// standalone projectile with no shooter) throws and we just bail.

	var TE = global.toolEffects
	if (!TE) return
	var effects = TE.ON_HIT[heldId]
	if (!effects || !effects.length) return

	const target = event.getEntity()
	effects.forEach(spec => {
		var me = global.armorEffects._resolveEffect(spec.effect)
		if (!me) return
		target.addEffect(new $ToolMobEffectInstance(me, spec.duration, spec.amplifier || 0))
	})
})

PlayerEvents.tick(event => {
	var player = event.getPlayer()
	if (!player || player.getLevel().isClientSide()) return

	var TE = global.toolEffects
	if (!TE || !Object.keys(TE.HELD).length) return

	var heldIds = []
	try {
		var mainStack = player.getMainHandItem()
		if (mainStack && !mainStack.isEmpty()) heldIds.push(String(mainStack.getItem().getId()))
	} catch (err) {}
	try {
		var offStack = player.getOffhandItem()
		if (offStack && !offStack.isEmpty()) heldIds.push(String(offStack.getItem().getId()))
	} catch (err) {}

	heldIds.forEach(id => {
		var effects = TE.HELD[id]
		if (!effects) return
		effects.forEach(spec => {
			var me = global.armorEffects._resolveEffect(spec.effect)
			if (!me) return
			player.addEffect(new $ToolMobEffectInstance(me, HELD_REFRESH_TICKS, spec.amplifier || 0, false, spec.visible === true))
		})
	})
})
