package net.satisfy.vinery.core.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Port: 1.20.5's {@code TreeGrower(name, megaTree, tree, flowers)} for data-driven trees. 1.19.2's growers take
 * built-in holders, so this one looks both features up in the server's registry when the sapling grows.
 */
public class VineryTreeGrower extends AbstractMegaTreeGrower {
    private final ResourceKey<ConfiguredFeature<?, ?>> megaTree;
    private final ResourceKey<ConfiguredFeature<?, ?>> tree;
    private ServerLevel growingIn;

    public VineryTreeGrower(ResourceKey<ConfiguredFeature<?, ?>> megaTree, ResourceKey<ConfiguredFeature<?, ?>> tree) {
        this.megaTree = megaTree;
        this.tree = tree;
    }

    @Override
    public boolean growTree(@NotNull ServerLevel level, @NotNull ChunkGenerator generator, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random) {
        growingIn = level;
        try {
            return super.growTree(level, generator, pos, state, random);
        } finally {
            growingIn = null;
        }
    }

    private @Nullable Holder<? extends ConfiguredFeature<?, ?>> lookup(ResourceKey<ConfiguredFeature<?, ?>> key) {
        if (growingIn == null) {
            return null;
        }
        return growingIn.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).getHolder(key).orElse(null);
    }

    @Override
    protected @Nullable Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(@NotNull RandomSource random) {
        return lookup(megaTree);
    }

    @Override
    protected @Nullable Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource random, boolean hasFlowers) {
        return lookup(tree);
    }
}
