package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.CEQConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Raises the generic.armor attribute's ceiling from vanilla's 30 to {@code dragon_gear.armorCap}
 * (default 50), so the full dragon set (36 points) is no longer clamped. The field can't be
 * shadowed (no refmap), so its two readers are overridden instead: sanitizeValue (the clamp on
 * the attribute total) and getMaxValue. Before the config loads the default is used.
 */
@Mixin(RangedAttribute.class)
public abstract class ArmorCapMixin {

    private static final double CE_DEFAULT_ARMOR_CAP = 50.0D;

    private static double creativeengineering$armorCap() {
        return CEQConfig.SPEC.isLoaded() ? CEQConfig.DRAGON_GEAR.armorCap.get() : CE_DEFAULT_ARMOR_CAP;
    }

    // sanitizeValue
    @Inject(method = "m_6740_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$sanitizeArmor(double value, CallbackInfoReturnable<Double> cir) {
        if ((Object) this == Attributes.ARMOR) {
            double min = ((RangedAttribute) (Object) this).getMinValue();
            cir.setReturnValue(Double.isNaN(value) ? min : Mth.clamp(value, min, creativeengineering$armorCap()));
        }
    }

    // getMaxValue
    @Inject(method = "m_147362_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$armorMax(CallbackInfoReturnable<Double> cir) {
        if ((Object) this == Attributes.ARMOR) {
            cir.setReturnValue(creativeengineering$armorCap());
        }
    }
}
