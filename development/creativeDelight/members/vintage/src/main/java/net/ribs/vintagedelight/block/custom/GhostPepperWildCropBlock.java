package net.ribs.vintagedelight.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.ribs.vintagedelight.item.ModTags;
import vectorwing.farmersdelight.common.block.WildCropBlock;

// Wild ghost peppers keep crimson and warped nylium (where they generate) and also take the planted pepper's soils
// (#vintagedelight:ghost_pepper_soil). Upstream's dirt is dropped (feature_writeup/vintage_delight.md 2.2).
// The two nylium blocks are named, not #minecraft:nylium: Terralith adds grass, mycelium and moss to that tag
// (vintageport #7, SQ-VTD6-01).
public class GhostPepperWildCropBlock extends WildCropBlock {
    public GhostPepperWildCropBlock(MobEffect suspiciousStewEffect, int effectDuration, Properties properties) {
        super(suspiciousStewEffect, effectDuration, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM) || state.is(ModTags.GHOST_PEPPER_SOIL);
    }

    // Forge's BushBlock.canSurvive asks the soil's canSustainPlant instead, which lets a plains plant onto grass, dirt
    // and farmland; mayPlaceOn is the only soil rule (vintageport #7)
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }
}
