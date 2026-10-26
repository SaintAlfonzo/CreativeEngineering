package com.SaintAlfonzo.creativedelight.rustic.content;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.WildCropBlock;

/** Farmer's Delight's wild crop, placed on any block in {@code #minecraft:dirt}. */
public class RusticWildCropBlock extends WildCropBlock {
    public RusticWildCropBlock(MobEffect suspiciousStewEffect, int effectDuration, Properties properties) {
        super(suspiciousStewEffect, effectDuration, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT);
    }
}
