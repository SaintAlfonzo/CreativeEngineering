package com.SaintAlfonzo.creativemagic.mixin;

import net.p3pp3rf1y.sophisticatedcore.util.XpHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Sophisticated Core hard-codes 20 mB liquid-XP per experience point
 * (XpHelper.RATIO, inlined as a float constant in both conversion methods).
 * Create: Enchantment Industry's experience fluid is 1 mB per point, so to
 * merge SB onto the Create fluid we retune the ratio to 1.
 *
 *   experienceToLiquid(xp)  = xp * 20f   ->  xp * 1f
 *   liquidToExperience(mB)  = mB / 20f   ->  mB / 1f
 */
@Mixin(value = XpHelper.class, remap = false)
public class XpHelperMixin {

    @ModifyConstant(
            method = { "experienceToLiquid", "liquidToExperience" },
            constant = @Constant(floatValue = 20.0F)
    )
    private static float creativeengineering$retuneXpRatio(float original) {
        return 1.0F;
    }
}
