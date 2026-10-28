package com.SaintAlfonzo.creativeengineering.potion;

import com.SaintAlfonzo.creativeengineering.CEConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * creativeengineering:magic_resistance — the MobEffect itself is registered entirely in
 * KubeJS ({@code startup_scripts/magic/magic_resistance.js}, {@code StartupEvents.registry
 * ('mob_effect', ...)}); that part needs no Java at all since it's a pack-original effect
 * with no cross-mod registry-timing problem. Its potions do need Java ({@link CEPotions}).
 *
 * <p>What KubeJS can't do is the actual damage reduction: this build's KubeJS wraps
 * {@code LivingHurtEvent} as a read-only {@code EntityEvents.hurt} (confirmed by
 * decompiling kubejs-forge-1902.6.2-build.73.jar — {@code LivingEntityHurtEventJS} exposes
 * only a getter for the damage amount, no {@code setAmount}/cancel), so reducing damage
 * requires a real Forge listener. Percentage scales with amplifier the same way vanilla
 * Resistance does (20% per level, capped at 100% / full immunity at amplifier 4+), applied
 * only to damage sources vanilla itself already flags as magic
 * ({@link net.minecraft.world.damagesource.DamageSource#isMagic()} — potion/instant-damage,
 * evoker fangs, conduit, dragon's breath, etc., same flag vanilla uses elsewhere). The
 * per-level reduction rate is a config knob - see {@link CEConfig.Magic}.
 *
 * <p>The MobEffect is looked up lazily (cached after the first hit) rather than at class
 * load / mod construction, since both of those run before KubeJS's own RegisterEvent
 * &lt;MobEffect&gt; has fired — same lazy-resolution reasoning as {@code potion_area.js}'s
 * {@code mobEffect()} helper and {@code armor_effects.js}'s {@code resolveEffect()}.
 */
public final class MagicResistanceEffect {

    private static final ResourceLocation ID = new ResourceLocation("creativeengineering", "magic_resistance");

    private static MobEffect effect;
    private static boolean lookedUp;

    private MagicResistanceEffect() {}

    private static MobEffect effect() {
        if (!lookedUp) {
            effect = ForgeRegistries.MOB_EFFECTS.getValue(ID);
            lookedUp = true;
        }
        return effect;
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        MobEffect me = effect();
        if (me == null || !event.getSource().isMagic()) return;

        LivingEntity entity = event.getEntity();
        MobEffectInstance instance = entity.getEffect(me);
        if (instance == null) return;

        float perLevel = CEConfig.MAGIC.reductionPerLevel.get().floatValue();
        float reduction = Math.min(1.0f, perLevel * (instance.getAmplifier() + 1));
        event.setAmount(event.getAmount() * (1.0f - reduction));
    }
}
