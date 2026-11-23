// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * creativecommon:wither_immunity (TODO food-tuning #47, ruling P7). The MobEffect is registered
 * by KubeJS ({@code kubejs_startup/creativecommon/effects/wither_immunity.js}), like Magic Resistance; this class
 * does the immunity at any level:
 * <ul>
 *   <li>Wither is not applicable while the effect is active ({@link MobEffectEvent.Applicable} DENY), from any source:
 *       potions, foods, Wither Skeletons, wither roses, commands.</li>
 *   <li>A Wither already on the entity when the immunity starts deals no damage: its ticks
 *       ({@link DamageSource#WITHER}) are canceled in {@link LivingAttackEvent}, before the hurt sound and flash. The
 *       Wither itself runs out as usual. Nothing is removed from the effect map, so no effect tick is disturbed.</li>
 * </ul>
 * The Wither boss's skulls and explosions are not the Wither effect and still hurt.
 */
public final class WitherImmunityEffect {

	public static final ResourceLocation ID = new ResourceLocation("creativecommon", "wither_immunity");

	private static MobEffect effect;

	private WitherImmunityEffect() {}

	private static MobEffect effect() {
		if (effect == null) effect = ForgeRegistries.MOB_EFFECTS.getValue(ID);
		return effect;
	}

	private static boolean immune(LivingEntity entity) {
		MobEffect e = effect();
		return e != null && entity.hasEffect(e);
	}

	@SubscribeEvent
	public static void onApplicable(MobEffectEvent.Applicable event) {
		if (event.getEffectInstance().getEffect() == MobEffects.WITHER && immune(event.getEntity())) {
			event.setResult(Event.Result.DENY);
		}
	}

	@SubscribeEvent
	public static void onAttack(LivingAttackEvent event) {
		if (event.getSource() == DamageSource.WITHER && immune(event.getEntity())) event.setCanceled(true);
	}
}
