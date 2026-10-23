package net.ribs.vintagedelight.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.WildCropBlock;

// Port (feature_writeup/vintage_delight.md S6): wild oats, peanuts and cucumbers stand on any #minecraft:dirt block,
// so the pack can place them on every dirt soil by hand, by /ceplace and by worldgen.
public class DirtWildCropBlock extends WildCropBlock {
    public DirtWildCropBlock(MobEffect suspiciousStewEffect, int effectDuration, Properties properties) {
        super(suspiciousStewEffect, effectDuration, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT);
    }
}
