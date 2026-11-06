package com.SaintAlfonzo.creativeworldgen.block;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

/**
 * Longer leaf life range than vanilla's 7 ({@code mixin/LeavesDecayRangeMixin}, CWGConfig [leaf_decay]).
 *
 * <p>A leaf's {@code distance} state only holds 1-7 (7 = "no log within 6"), and widening the property
 * would add block states to every leaf block (state ids are already near the 2^19 packet limit, see
 * ghost-blocks notes). So the stored value stays vanilla, and the range is stretched at the one moment
 * it matters: when a non-persistent distance-7 leaf is about to decay in {@code randomTick} (vanilla
 * random ticks and Fast Leaf Decay's scheduled ticks both end there). A breadth-first walk through the
 * connected distance-7 leaves looks for a leaf that still has a real distance {@code d < 7}; reaching
 * one {@code k} steps away means a log is {@code k + d} away. Within {@code maxDistance} the leaf
 * stays (vanilla keeps paths up to 6, so maxDistance 6 = vanilla). Only distance-7 leaves are walked (the region past vanilla's reach), so the walk is small.
 *
 * <p>Timing: decay happens in {@code LeavesBlock#tick}, the scheduled tick vanilla queues from {@code updateShape}
 * when a neighbour changes (and {@link FastLeafDecayPort} queues when a neighbour turns to air). {@link #afterTick}:
 * a leaf that this tick took from a real distance to 7 (its log is gone) is checked again decayMinTicks to
 * decayMaxTicks later, which staggers the fall; a leaf that was
 * already at 7 is checked now ({@link #keep}) and dropped if no log is in range. Its removal updates its neighbours,
 * so a cut tree's canopy cascades away. Worldgen and sapling trees set their leaves' distances directly with no
 * shape updates, so their far leaves are never ticked and stay until something next to them changes. With
 * {@code onlyWhenDisturbed} (default) random ticks never decay a natural leaf; without it they also drop leaves past
 * maxDistance, at vanilla's slow random rate.
 *
 * <p>Which leaves connect follows Separated Leaves when it is loaded: a leaf only walks through
 * leaves in its own rule's {@code leaves} set (oak through oak, azalea through both azaleas), whose
 * distance values that mod already computes against matching logs only. Without a rule, any
 * {@link LeavesBlock} connects, as in vanilla.
 */
public final class LeafDecayRange {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int VANILLA_MAX = LeavesBlock.DECAY_DISTANCE;

    private LeafDecayRange() {}

    /** Random-tick hook: true = cancel this decay (natural leaf at distance 7). */
    public static boolean cancelRandomDecay(ServerLevel level, BlockPos pos, BlockState state) {
        CWGConfig.LeafDecay cfg = CWGConfig.LEAF_DECAY;
        if (!cfg.enabled.get()) return false;
        return cfg.onlyWhenDisturbed.get() || keep(level, pos, state);
    }

    /** Scheduled-tick hook, after vanilla stored the recomputed distance. {@code before} is the state the tick started with. */
    public static void afterTick(ServerLevel level, BlockPos pos, BlockState before, RandomSource random) {
        if (!CWGConfig.LEAF_DECAY.enabled.get()) return;
        BlockState now = level.getBlockState(pos);
        if (now.getBlock() != before.getBlock() || !decaying(now)) return;
        if (before.getValue(LeavesBlock.DISTANCE) < VANILLA_MAX) {
            level.scheduleTick(pos, now.getBlock(), FastLeafDecayPort.decayDelay(random));
        } else if (!keep(level, pos, now)) {
            Block.dropResources(now, level, pos);
            level.removeBlock(pos, false);
            LeafDecayPiles.afterDecay(now, level, pos, random);
        }
    }

    /** Vanilla {@code LeavesBlock#decaying} (m_221385_): natural and out of reach. */
    public static boolean decaying(BlockState state) {
        return state.hasProperty(LeavesBlock.PERSISTENT) && !state.getValue(LeavesBlock.PERSISTENT)
                && state.getValue(LeavesBlock.DISTANCE) == VANILLA_MAX;
    }

    /**
     * True when the decaying leaf at {@code pos} is within the configured range of a log and should
     * not decay. Only called for non-persistent leaves at distance 7.
     */
    public static boolean keep(ServerLevel level, BlockPos pos, BlockState state) {
        CWGConfig.LeafDecay cfg = CWGConfig.LEAF_DECAY;
        int max = cfg.maxDistance.get();
        if (max < VANILLA_MAX) return false;
        int budget = cfg.maxSearchBlocks.get();

        Set<Block> group = SeparatedLeavesRules.groupOf(state.getBlock());
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        LongOpenHashSet seen = new LongOpenHashSet();
        long start = pos.asLong();
        queue.enqueue(start);
        seen.add(start);
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos next = new BlockPos.MutableBlockPos();

        // Layered BFS: depth of everything in the queue is known per layer.
        for (int depth = 0; !queue.isEmpty(); depth++) {
            int layer = queue.size();
            int step = depth + 1;
            for (int i = 0; i < layer; i++) {
                at.set(queue.dequeueLong());
                for (Direction dir : Direction.values()) {
                    next.setWithOffset(at, dir);
                    long key = next.asLong();
                    if (seen.contains(key) || !level.isLoaded(next)) continue;
                    BlockState ns = level.getBlockState(next);
                    if (!connects(ns, group)) continue;
                    seen.add(key);
                    int d = ns.getValue(LeavesBlock.DISTANCE);
                    if (d < VANILLA_MAX) {
                        if (step + d <= max) return true;
                    } else if (step + 2 <= max) {
                        // A distance-7 leaf is worth walking past only if one more step and a d=1
                        // neighbour could still land inside the range.
                        queue.enqueue(key);
                    }
                    if (seen.size() > budget) return false;
                }
            }
        }
        return false;
    }

    private static boolean connects(BlockState ns, Set<Block> group) {
        if (!(ns.getBlock() instanceof LeavesBlock) || !ns.hasProperty(LeavesBlock.DISTANCE)) return false;
        return group == null || group.contains(ns.getBlock());
    }

    /** Reads Separated Leaves' live rule list ({@code Reload.LEAVES_RULES}) by reflection, if the mod is present. */
    static final class SeparatedLeavesRules {
        private static boolean resolved;
        private static List<?> rules;
        private static Method leaves;

        private SeparatedLeavesRules() {}

        /** The leaves set of the first rule naming {@code block}, or null (no mod, or no rule: any leaves connect). */
        @SuppressWarnings("unchecked")
        static Set<Block> groupOf(Block block) {
            if (!resolved) resolve();
            if (rules == null) return null;
            try {
                for (Object rule : rules) {
                    Set<Block> set = (Set<Block>) leaves.invoke(rule);
                    if (set.contains(block)) return set;
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                LOGGER.warn("[leaf_decay] Separated Leaves rules unreadable, falling back to any-leaves", e);
                rules = null;
            }
            return null;
        }

        private static synchronized void resolve() {
            if (resolved) return;
            try {
                Class<?> reload = Class.forName("nl.teamdiopside.separatedleaves.Reload");
                Field f = reload.getField("LEAVES_RULES");
                rules = (List<?>) f.get(null);
                leaves = Class.forName("nl.teamdiopside.separatedleaves.Reload$LeavesRule").getMethod("leaves");
            } catch (ReflectiveOperationException | LinkageError e) {
                rules = null;
            }
            resolved = true;
        }
    }
}
