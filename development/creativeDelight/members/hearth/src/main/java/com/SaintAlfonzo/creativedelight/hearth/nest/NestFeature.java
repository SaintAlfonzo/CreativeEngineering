package com.SaintAlfonzo.creativedelight.hearth.nest;

import com.SaintAlfonzo.creativedelight.hearth.crow.CrowConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Places one nest marked as world-generated, which is what crow spawning near nests looks for. */
public class NestFeature extends Feature<NoneFeatureConfiguration> {
    public NestFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!CrowConfig.GENERATE_NESTS.get()) {
            return false;
        }
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        BlockState existing = level.getBlockState(pos);
        if (!existing.getMaterial().isReplaceable() || !existing.getFluidState().isEmpty()) {
            return false;
        }
        BlockState nest = HHNestRegistry.NEST.get().defaultBlockState().setValue(NestBlock.GENERATED, true);
        if (!nest.canSurvive(level, pos)) {
            return false;
        }
        level.setBlock(pos, nest, 2);
        return true;
    }
}
