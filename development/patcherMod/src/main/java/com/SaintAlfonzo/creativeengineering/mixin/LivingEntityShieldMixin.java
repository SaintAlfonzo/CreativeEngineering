package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.shield.ShieldBlocking;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a tier shield ({@code shield.TierShieldItem}) decide for itself what it can block.
 * Vanilla's {@code LivingEntity#isDamageSourceBlocked} ({@code m_21275_}) refuses anything flagged
 * {@code bypassArmor} or without a source position - fire, poison and most magic - so no
 * {@code ShieldBlockEvent} listener could ever reach those hits; the event only fires once this
 * has said yes. The HEAD inject answers only when the raised item is a tier shield;
 * {@link ShieldBlocking#verdict} returns {@code null} for everything else (vanilla, Immersive
 * Engineering's shield, mobs with no shield up), and vanilla's own method then runs untouched.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} - same idiom as
 * {@link ArmorMaterialEnchantabilityMixin} (no refmap; the pack only runs reobfuscated). Wrapped
 * in a catch-all so a bug here can never break combat for everyone: it degrades to vanilla.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldMixin {

    @Inject(method = "m_21275_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$tierShieldBlock(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        try {
            Boolean verdict = ShieldBlocking.verdict((LivingEntity) (Object) this, source);
            if (verdict != null) {
                cir.setReturnValue(verdict);
            }
        } catch (Throwable ignored) {
            // fall through to vanilla's own check
        }
    }
}
