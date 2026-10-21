package com.SaintAlfonzo.creativeengineering.block;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.LeavesGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

/**
 * Immersive Weathering's leaf pile on decay, for the pack's decay path. IW's {@code LeavesMixin} calls
 * {@code LeavesGrowth.decayLeavesPile} right after {@code removeBlock} in {@code LeavesBlock#randomTick}; Fast Leaf
 * Decay ran randomTick, so its decays left piles. {@link LeafDecayRange#afterTick} removes the leaf from
 * {@code tick} instead, so it calls the same IW method here: IW's chance (spawn_entity_from_decay), blacklist,
 * 1-5 layer pile in the leaf's place and the falling-leaves particles. Random-tick decay (onlyWhenDisturbed false)
 * still goes through IW's own hook.
 */
final class LeafDecayPiles {

    private static final boolean IW_LOADED = ModList.get().isLoaded("immersive_weathering");

    private LeafDecayPiles() {}

    static void afterDecay(BlockState leaf, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!IW_LOADED || !CEConfig.LEAF_DECAY.leafPiles.get()) return;
        Iw.decay(leaf, level, pos, random);
    }

    /** Holds the IW reference so this class loads without the mod. */
    private static final class Iw {
        static void decay(BlockState leaf, ServerLevel level, BlockPos pos, RandomSource random) {
            LeavesGrowth.decayLeavesPile(leaf, level, pos, random);
        }
    }
}
