package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.effect.EffectCategories;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies CommonConfig {@code [effects] categoryOverrides} ({@link EffectCategories}). {@code getCategory()} picks the
 * tooltip colour (red for HARMFUL) in potion, food and gear tooltips; {@code isBeneficial()} reads the private field
 * in vanilla, so it is answered here too and the two never disagree. None of the overridden effects' classes
 * override either method (checked 2026-10-20).
 *
 * <p>Vanilla-class target, matched by SRG name ({@code m_19483_} = {@code getCategory()},
 * {@code m_19486_} = {@code isBeneficial()}) with {@code remap = false} - same idiom as {@link ToastFilterMixin}.
 */
@Mixin(value = MobEffect.class, remap = false)
public class MobEffectCategoryMixin {

    @Inject(method = "m_19483_", at = @At("HEAD"), cancellable = true)
    private void creativeengineering$categoryOverride(CallbackInfoReturnable<MobEffectCategory> cir) {
        MobEffectCategory category = EffectCategories.override((MobEffect) (Object) this);
        if (category != null) cir.setReturnValue(category);
    }

    @Inject(method = "m_19486_", at = @At("HEAD"), cancellable = true)
    private void creativeengineering$beneficialOverride(CallbackInfoReturnable<Boolean> cir) {
        MobEffectCategory category = EffectCategories.override((MobEffect) (Object) this);
        if (category != null) cir.setReturnValue(category == MobEffectCategory.BENEFICIAL);
    }
}
