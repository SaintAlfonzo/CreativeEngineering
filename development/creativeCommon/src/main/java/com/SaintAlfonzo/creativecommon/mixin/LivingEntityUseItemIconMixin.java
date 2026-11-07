package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.SaintAlfonzo.creativecommon.effect.EffectRules;

import net.minecraft.world.entity.LivingEntity;

/**
 * Marks the item being finished ({@code LivingEntity#completeUsingItem}, {@code m_8095_}) for {@link EffectRules#withItemIcon}:
 * every effect added meanwhile (vanilla food effects through {@code addEatEffect}, Create's Builder's Tea and Canned Goods'
 * pufferfish procedure, both called from {@code finishUsingItem}) gets its HUD icon when the item is in CommonConfig
 * {@code [effects] forceHudIconItems} ({@code potions/effects #8}). Server side only.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityUseItemIconMixin {

    @Inject(method = "m_8095_", at = @At("HEAD"), remap = false)
    private void creativeengineering$markItem(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide) EffectRules.setUsingItem(self.getUseItem().getItem());
    }

    @Inject(method = "m_8095_", at = @At("RETURN"), remap = false)
    private void creativeengineering$clearItem(CallbackInfo ci) {
        if (!((LivingEntity) (Object) this).level.isClientSide) EffectRules.setUsingItem(null);
    }
}
