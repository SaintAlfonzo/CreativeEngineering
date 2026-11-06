// Salt (mortuusars), MIT: a modified copy of the upstream io.github.mortuusars.salt.Dissolving for the Creative
// Delight repack (members/salt/NOTICE-upstream-MIT.txt). The edit is GNU GPL3 like the rest of the pack's code.
// Change against upstream 1.2.4.1: the raw rock salt block never dissolves (isKept), which CD's
// SaltRockNoDissolveMixin did before Salt became a member (feature_writeup/salt.md).
package io.github.mortuusars.salt;

import io.github.mortuusars.salt.block.ISaltBlock;
import io.github.mortuusars.salt.configuration.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class Dissolving {

    /**
     * Creative Delight: rock salt stays, beach patches are built on it (owner, 2026-10-26). Melting and cluster
     * growth are untouched.
     */
    private static boolean isKept(BlockState state) {
        return state.is(Salt.Blocks.RAW_ROCK_SALT_BLOCK.get());
    }

    /** dissolvedState is the state left behind, not the block, so the block is read at pos. */
    public static boolean maybeDissolveInRain(BlockState dissolvedState, ServerLevel level, BlockPos pos) {
        if (isKept(level.getBlockState(pos))) return false;
        if (Configuration.DISSOLVING_ENABLED.get()
                && Configuration.DISSOLVING_IN_RAIN.get()
                && level.isRainingAt(pos.above())
                && level.random.nextDouble() < Configuration.DISSOLVING_IN_RAIN_CHANCE.get()) {
            dissolve(dissolvedState, level, pos, Fluids.EMPTY, false);
            return true;
        }
        return false;
    }

    public static boolean maybeDissolve(BlockState dissolvedState, BlockPos dissolvedPos, BlockState adjacentState,
                                        BlockPos adjacentPos, ServerLevel level) {
        if (isKept(dissolvedState)) return false;
        if (Configuration.DISSOLVING_ENABLED.get()
                && adjacentState.is(Salt.BlockTags.SALT_DISSOLVABLES)
                && level.random.nextDouble() < Configuration.DISSOLVING_CHANCE.get()) {
            FluidState fluidState = adjacentState.getFluidState();
            if (dissolvedPos.getY() > adjacentPos.getY()) {
                fluidState = Fluids.EMPTY.defaultFluidState();
            }
            dissolve(dissolvedState, level, dissolvedPos, fluidState.getType(), fluidState.isSource());
            return true;
        }
        return false;
    }

    public static void dissolve(BlockState state, ServerLevel level, BlockPos pos, Fluid fluid, boolean isSource) {
        BlockState dissolvedState = state.getBlock() instanceof ISaltBlock saltBlock
                ? saltBlock.getDissolvedState(state, level, pos, fluid)
                : Blocks.AIR.defaultBlockState();
        if (shouldPlaceFluidSource(level, pos, fluid, isSource)) {
            if (dissolvedState.isAir()) {
                dissolvedState = fluid.defaultFluidState().createLegacyBlock();
            } else if (dissolvedState.getBlock() instanceof SimpleWaterloggedBlock waterloggedBlock
                    && waterloggedBlock.canPlaceLiquid(level, pos, dissolvedState, fluid)) {
                dissolvedState = dissolvedState.setValue(BlockStateProperties.WATERLOGGED, true);
            }
        }
        level.setBlockAndUpdate(pos, dissolvedState);
        onBlockDissolved(level, pos);
    }

    private static boolean shouldPlaceFluidSource(ServerLevel level, BlockPos pos, Fluid fluid, boolean isSource) {
        return Configuration.DISSOLVING_FLUID_SOURCE_CONVERSION.get() && fluid == Fluids.WATER && isSource;
    }

    private static void onBlockDissolved(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        RandomSource random = level.getRandom();
        level.playSound(null, center.x, center.y, center.z, Salt.Sounds.SALT_DISSOLVE.get(), SoundSource.BLOCKS,
                1.0F, random.nextFloat() * 0.2F + 0.9F);
        for (int i = 0; i < 6; i++) {
            level.sendParticles(ParticleTypes.SPIT,
                    center.x + random.nextGaussian() * 0.35F,
                    center.y + random.nextGaussian() * 0.35F,
                    center.z + random.nextGaussian() * 0.35F,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
