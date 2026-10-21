package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.ordana.immersive_weathering.blocks.LeafPileBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Immersive Weathering {@code LeafPileBlock#entityInside} (m_7892_) slows an entity when {@code layers > 3}; the
 * 3 (the method's only {@code iconst_3}) becomes CEConfig [leaf_piles] slowFromLayers - 1, so a pile slows from
 * that many layers up. The WTHIT Run Speed line ({@code surface.StuckInBlock}) uses the same setting. Spruce
 * thorns stay at 6+ layers and only apply where the pile slows.
 */
@Mixin(value = LeafPileBlock.class, remap = false)
public abstract class LeafPileSlowMixin {

    @ModifyConstant(method = "m_7892_", constant = @Constant(intValue = 3, ordinal = 0), remap = false)
    private int creativeengineering$slowFromLayers(int original) {
        return CEConfig.LEAF_PILES.slowFromLayers.get() - 1;
    }
}
