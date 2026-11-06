package com.SaintAlfonzo.creativecommon.compat.jei;

import com.SaintAlfonzo.creativecommon.CommonConfig;

/**
 * Fill height of a JEI fluid tank for an amount ({@code mixin.JeiFluidTankScaleMixin}, {@code [jei]
 * fluidAmountScale}). The share of the tank, before JEI's own clamp to 1..height:
 * <ul>
 *   <li>LOG: {@code ln(1 + amount) / ln(1 + capacity)}: in a 1000 mB tank 10 mB is 35%, 250 mB 80%, 500 mB 90%.</li>
 *   <li>SQRT: {@code sqrt(amount / capacity)}: 10 mB 10%, 250 mB 50%, 500 mB 71%.</li>
 *   <li>LINEAR: {@code amount / capacity} (JEI's own).</li>
 * </ul>
 * Amounts over the tank size fill it.
 */
public final class FluidTankScale {
    private FluidTankScale() {}

    public static long height(CommonConfig.Jei.FluidScale mode, long amount, long capacity, int height) {
        if (amount <= 0 || capacity <= 0) return 0;
        if (amount >= capacity) return height;
        double share = switch (mode) {
            case LOG -> Math.log1p(amount) / Math.log1p(capacity);
            case SQRT -> Math.sqrt((double) amount / capacity);
            case LINEAR -> (double) amount / capacity;
        };
        return Math.round(share * height);
    }
}
