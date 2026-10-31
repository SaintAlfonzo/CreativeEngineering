package com.SaintAlfonzo.creativeengineering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativeengineering.potion.CompoundEffects;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;

/**
 * JEED's effect page text ({@code EffectInfo#getDescription(MobEffect)}, the {@code effect.<ns>.<path>.description} lang key)
 * gets "Compound of: ..." appended for a compound effect ({@link CompoundEffects}, {@code potions/effects #10}), built from
 * the same part list as the tooltips so the two can't disagree.
 */
@Pseudo
@Mixin(targets = "net.mehvahdjukaar.jeed.common.EffectInfo", remap = false)
public abstract class JeedEffectInfoMixin {

    @Inject(method = "getDescription(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/network/chat/Component;",
            at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private static void creativeengineering$compoundParts(MobEffect effect, CallbackInfoReturnable<Component> cir) {
        Component parts = CompoundEffects.describe(effect);
        if (parts == null || cir.getReturnValue() == null) return;
        cir.setReturnValue(cir.getReturnValue().copy().append(" ").append(parts));
    }
}
