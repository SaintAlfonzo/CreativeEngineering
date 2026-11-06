package net.ribs.vintagedelight.worldgen.tree;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

// Upstream's ModTreeGrower + MagicVineGrower: the Magic Peanut grows the data-driven vintagedelight:magic_vine_tree.
public class MagicVineGrower extends AbstractTreeGrower {
    private static final ResourceKey<ConfiguredFeature<?, ?>> TREE = ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY,
        new ResourceLocation("vintagedelight", "magic_vine_tree"));

    @Nullable
    @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
        return null;
    }

    @Override
    public boolean growTree(ServerLevel level, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, RandomSource random) {
        Optional<? extends Holder<ConfiguredFeature<?, ?>>> optional = level.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).getHolder(TREE);
        if (optional.isEmpty()) {
            return false;
        }
        ConfiguredFeature<?, ?> feature = optional.get().value();
        BlockState fluidState = level.getFluidState(pos).createLegacyBlock();
        level.setBlock(pos, fluidState, 4);
        if (feature.place(level, chunkGenerator, random, pos)) {
            if (level.getBlockState(pos) == fluidState) {
                level.sendBlockUpdated(pos, state, fluidState, 2);
            }
            return true;
        }
        level.setBlock(pos, state, 4);
        return false;
    }
}
