package com.SaintAlfonzo.creativebrewing.mixin.jeed;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativebrewing.potion.CompoundEffects;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;

/**
 * JEED's effect hover in JEI ({@code EffectRenderer#getTooltipsWithDescription}, jeed-1.19.2-2.1.4, vineflower-checked)
 * prints its own "When Applied:" block from the effect's attribute modifiers. The parts of a compound effect go under it
 * ({@link CompoundEffects}, {@code potions/effects #10}); with the description shown (shift) a block of their own follows
 * the name lines.
 */
@Pseudo
@Mixin(targets = "net.mehvahdjukaar.jeed.common.EffectRenderer", remap = false)
public abstract class JeedEffectRendererMixin {

    @Inject(method = "getTooltipsWithDescription", at = @At("RETURN"), require = 0, remap = false)
    private static void creativeengineering$compoundParts(MobEffectInstance instance, TooltipFlag flag, boolean reactsToShift,
            boolean showDuration, CallbackInfoReturnable<List<Component>> cir) {
        List<Component> tooltip = cir.getReturnValue();
        if (instance == null || tooltip == null) return;
        CompoundEffects.insert(tooltip, List.of(instance), tooltip.size());
    }
}
