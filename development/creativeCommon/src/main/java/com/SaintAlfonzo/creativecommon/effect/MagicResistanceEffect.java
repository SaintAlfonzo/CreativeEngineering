package com.SaintAlfonzo.creativecommon.effect;

import com.SaintAlfonzo.creativecommon.CreativeCommon;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import java.util.HashSet;
import java.util.Set;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativecommon.damage.DamageTags;
import com.SaintAlfonzo.creativecommon.shield.EffectTickContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * creativecommon:magic_resistance — the MobEffect itself is registered entirely in
 * KubeJS ({@code startup_scripts/magic/magic_resistance.js}, {@code StartupEvents.registry
 * ('mob_effect', ...)}); that part needs no Java at all since it's a pack-original effect
 * with no cross-mod registry-timing problem. Its potions do need Java ({@link CommonPotions}).
 *
 * <p>What KubeJS can't do is the actual damage reduction: this build's KubeJS wraps
 * {@code LivingHurtEvent} as a read-only {@code EntityEvents.hurt} (confirmed by
 * decompiling kubejs-forge-1902.6.2-build.73.jar — {@code LivingEntityHurtEventJS} exposes
 * only a getter for the damage amount, no {@code setAmount}/cancel), so reducing damage
 * requires a real Forge listener. Two rules:
 * <ul>
 *   <li>Periodic damage from an effect in {@code [magic_resistance].negatedEffectTicks} (Poison,
 *       Wither) is cancelled in {@code LivingAttackEvent}, before the hurt sound and flash, when the
 *       ticking effect's level is at most the Magic Resistance level. Shields never block these
 *       ticks ({@code [shields].unblockableEffectTicks}), so this is the cover for them. Recognised
 *       through {@link EffectTickContext}, since poison ticks share Instant Damage's source.</li>
 *   <li>Everything else in the {@code creativecommon:magic} damage tag ({@link DamageTags}, the
 *       same tag the shields' MAGIC column reads: vanilla-flagged magic plus thorns, sonic boom,
 *       psychic and life drain) and those ticks above the Magic Resistance level lose 20% per level
 *       (vanilla Resistance's shape, capped at full immunity) in {@code LivingHurtEvent}. The rate
 *       is {@code [magic_resistance].reductionPerLevel} ({@link CommonConfig.Magic}).</li>
 * </ul>
 *
 * <p>The MobEffect is looked up lazily (cached after the first hit) rather than at class
 * load / mod construction, since both of those run before KubeJS's own RegisterEvent
 * &lt;MobEffect&gt; has fired — same lazy-resolution reasoning as {@code potion_area.js}'s
 * {@code mobEffect()} helper and {@code armor_effects.js}'s {@code resolveEffect()}.
 */
public final class MagicResistanceEffect {

    private static final ResourceLocation ID = new ResourceLocation(CreativeCommon.MODID, "magic_resistance");

    private static final ResourceLocation MAGIC_TAG = DamageTags.id("magic");

    private static MobEffect effect;
    private static boolean lookedUp;

    // Parsed negatedEffectTicks keyed by the raw config string, so a live edit just re-parses.
    private static volatile String negatedRaw;
    private static volatile Set<ResourceLocation> negatedIds = Set.of();

    private MagicResistanceEffect() {}

    private static MobEffect effect() {
        if (!lookedUp) {
            effect = ForgeRegistries.MOB_EFFECTS.getValue(ID);
            lookedUp = true;
        }
        return effect;
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        MobEffect me = effect();
        if (me == null) return;
        LivingEntity entity = event.getEntity();
        MobEffect ticking = EffectTickContext.tickingOn(entity);
        if (ticking == null || !negated(ticking)) return;
        MobEffectInstance resistance = entity.getEffect(me);
        MobEffectInstance tick = entity.getEffect(ticking);
        if (resistance == null || tick == null) return;
        if (tick.getAmplifier() <= resistance.getAmplifier()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        MobEffect me = effect();
        if (me == null) return;

        LivingEntity entity = event.getEntity();
        MobEffectInstance instance = entity.getEffect(me);
        if (instance == null) return;

        MobEffect ticking = EffectTickContext.tickingOn(entity);
        boolean tickCovered = ticking != null && negated(ticking);
        if (!tickCovered && !DamageTags.is(event.getSource(), MAGIC_TAG)) return;

        float perLevel = CommonConfig.MAGIC_RESISTANCE.reductionPerLevel.get().floatValue();
        float reduction = Math.min(1.0f, perLevel * (instance.getAmplifier() + 1));
        event.setAmount(event.getAmount() * (1.0f - reduction));
    }

    /** True when {@code effect} is listed in {@code [magic_resistance].negatedEffectTicks}. */
    private static boolean negated(MobEffect effect) {
        String raw;
        try {
            raw = CommonConfig.MAGIC_RESISTANCE.negatedEffectTicks.get();
        } catch (RuntimeException notLoaded) {
            return false;
        }
        if (!raw.equals(negatedRaw)) {
            Set<ResourceLocation> ids = new HashSet<>();
            for (String part : raw.split(",")) {
                ResourceLocation id = ResourceLocation.tryParse(part.trim());
                if (id != null && !part.isBlank()) ids.add(id);
            }
            negatedIds = Set.copyOf(ids);
            negatedRaw = raw;
        }
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        return id != null && negatedIds.contains(id);
    }
}
