package com.SaintAlfonzo.creativeengineering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.SaintAlfonzo.creativeengineering.shield.EffectTickContext;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Wraps the single call site of every effect's periodic tick - {@code MobEffectInstance#applyEffect}
 * ({@code m_19550_}) calling {@code MobEffect#applyEffectTick} ({@code m_6742_}), bytecode-checked on
 * the Forge 43.3.9 client jar - in {@link EffectTickContext}, so shield code can recognise damage
 * that comes from a ticking effect (see {@code ShieldBlocking}, {@code [shields].unblockableEffectTicks}).
 * Covers modded effects too, since they override {@code applyEffectTick} but are still called from here.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} - same idiom as
 * {@link LivingEntityShieldMixin}. No other mod in the pack mixes into this call (Bookshelf only has
 * an accessor on the class).
 */
@Mixin(MobEffectInstance.class)
public abstract class MobEffectTickContextMixin {

    @Redirect(method = "m_19550_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffect;m_6742_(Lnet/minecraft/world/entity/LivingEntity;I)V"),
            remap = false)
    private void creativeengineering$markEffectTick(MobEffect effect, LivingEntity entity, int amplifier) {
        if (entity.level.isClientSide) {
            // Only the server thread ever writes the context, so the singleplayer client thread
            // can't race it.
            effect.applyEffectTick(entity, amplifier);
            return;
        }
        LivingEntity outerEntity = EffectTickContext.entity();
        MobEffect outerEffect = EffectTickContext.effect();
        EffectTickContext.set(entity, effect);
        try {
            effect.applyEffectTick(entity, amplifier);
        } finally {
            EffectTickContext.set(outerEntity, outerEffect);
        }
    }
}
