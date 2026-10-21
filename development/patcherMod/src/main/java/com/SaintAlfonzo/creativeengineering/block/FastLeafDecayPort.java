package com.SaintAlfonzo.creativeengineering.block;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Fast Leaf Decay (Olafski, FastLeafDecay-30), ported into the pack's leaf decay (CEConfig [leaf_decay],
 * {@link LeafDecayRange}). The mod listened for a block turning to air and, 4-10 ticks later, ran {@code tick}
 * then {@code randomTick} on the leaves next to it from its own unbounded server-tick list. Here the same event
 * queues a vanilla scheduled tick on each neighbouring leaf ({@code decayMinTicks} to {@code decayMaxTicks}),
 * which recomputes the distance; {@link LeafDecayRange#afterTick} then drops the leaf if no log is in range.
 * Vanilla already queues that tick from {@code updateShape}, so this mainly covers removals that skip shape
 * updates (flag 16: pistons, some machines) and spreads the timing. A tick already queued for the leaf wins.
 */
public final class FastLeafDecayPort {

    private FastLeafDecayPort() {}

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !event.getState().isAir()) return;
        if (!CEConfig.LEAF_DECAY.enabled.get()) return;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (Direction dir : event.getNotifiedSides()) {
            at.setWithOffset(event.getPos(), dir);
            if (!level.isLoaded(at)) continue;
            BlockState state = level.getBlockState(at);
            if (state.getBlock() instanceof LeavesBlock && !state.getValue(LeavesBlock.PERSISTENT)) {
                level.scheduleTick(at.immutable(), state.getBlock(), decayDelay(level.random));
            }
        }
    }

    /** Ticks until a leaf's decay check: uniform in [decayMinTicks, decayMaxTicks]. */
    static int decayDelay(RandomSource random) {
        int min = CEConfig.LEAF_DECAY.decayMinTicks.get();
        int max = Math.max(min, CEConfig.LEAF_DECAY.decayMaxTicks.get());
        return min + random.nextInt(max - min + 1);
    }
}
