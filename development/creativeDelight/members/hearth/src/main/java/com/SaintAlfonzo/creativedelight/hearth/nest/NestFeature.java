package com.SaintAlfonzo.creativedelight.hearth.nest;

import com.SaintAlfonzo.creativedelight.hearth.crow.CrowConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Tree-top nest (plan_hearth_crow_swap.md 5.1): placed by {@code placed_feature/nest_tree_top.json} on the MOTION_BLOCKING
 * heightmap (which stops on leaves) at the top_layer_modification step, after the trees. Places only on leaves with nothing above.
 * The ground mode of the crow eval is gone; nests it generated stay.
 */
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
        if (!level.isEmptyBlock(pos) || !level.getBlockState(pos.below()).is(BlockTags.LEAVES)
                || level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) > pos.getY()) {
            return false;
        }
        NestCrows.placeGenerated(level, pos, context.random(), NestCrows.TREE_LOOT);
        return true;
    }
}
