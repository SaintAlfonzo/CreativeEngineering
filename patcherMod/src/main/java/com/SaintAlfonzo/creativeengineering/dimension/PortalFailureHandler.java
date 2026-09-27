package com.SaintAlfonzo.creativeengineering.dimension;

import com.SaintAlfonzo.creativeengineering.block.CreativeEngineeringBlocks;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent.PortalSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Intercepts every nether portal ignition attempt (flint and steel, dispenser,
 * fire charge - anything that routes through BaseFireBlock#onPlace, or Dark
 * Fire's own reimplementation of that flow).
 *
 * This is the single authoritative place portal blocks actually get created
 * from - neither DarkFireBlock nor anything else calls createPortalBlocks()
 * directly. Normal lighting fails outright: the portal never forms, the fire
 * that was placed inside the frame is snuffed out, and the surrounding
 * obsidian frame is converted to crying obsidian so it can't just be re-lit.
 */
@Mod.EventBusSubscriber(modid = "creativeengineering", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PortalFailureHandler {

    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    // Generous upper bound on the number of interior (air/fire) cells we'll
    // flood-fill through before giving up on an axis. Vanilla's largest legal
    // portal interior tops out well under this, so hitting the cap means
    // we've leaked into open space - i.e. this isn't a valid, fully-enclosed
    // frame on the axis we're currently trying.
    private static final int MAX_INTERIOR_BLOCKS = 500;

    @SubscribeEvent
    public static void onPortalSpawn(PortalSpawnEvent event) {
        // Diagnostic: confirms whether this fires at all for Dark Fire, and
        // what state triggered it. Safe to remove once this is confirmed
        // working - but leave it in until then.
        LOGGER.info("[PortalFailureHandler] PortalSpawnEvent fired, state={}", event.getState());

        // Dark Fire is the "real" ignition flame. This is the only place in
        // the mod that calls createPortalBlocks() - DarkFireBlock only finds
        // the shape and fires this event, it never creates blocks itself.
        // We cancel afterward purely so no other listener downstream also
        // tries to act on an already-handled spawn.
        if (event.getState().is(CreativeEngineeringBlocks.DARK_FIRE.get())) {
            event.getPortalSize().createPortalBlocks();
            event.setCanceled(true);
            return;
        }

        // Always cancel first - this is the part that actually stops the
        // portal from forming, regardless of which logical side we're on.
        event.setCanceled(true);

        LevelAccessor levelAccessor = event.getLevel();

        // We only want to mutate real world state once, on the logical
        // server. PortalSpawnEvent hands back a LevelAccessor rather than
        // a guaranteed Level, so guard both the type and the side.
        if (!(levelAccessor instanceof Level level) || level.isClientSide()) {
            return;
        }

        // PortalSpawnEvent fires from BaseFireBlock#onPlace (or Dark Fire's
        // own reimplementation of that flow), which runs AFTER the fire
        // block has already been placed in the world. Cancelling the event
        // only stops the portal - the fire itself is still sitting there,
        // so we replace it ourselves below.
        BlockPos firePos = event.getPos();

        // Find and convert the obsidian frame around the ignition point.
        // The flood fill only uses firePos as a seed - it doesn't care what
        // block is actually there, so this can safely run before we touch
        // the fire itself.
        convertFailedFrame(level, firePos);

        // Swap regular fire for soul fire rather than putting it out. Soul
        // fire is the same flame crying obsidian produces when lit, so this
        // reads as "the portal reacted, just not the way you wanted" and
        // sets up relighting once the frame's swapped blocks come into play.
        level.setBlock(firePos, Blocks.SOUL_FIRE.defaultBlockState(), 3);

        // Minimal feedback so this reads as an intentional failure rather
        // than a silent no-op or a bug.
        level.playSound(null, firePos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Locates the obsidian frame around a failed ignition point and turns it
     * into crying obsidian. Tries both horizontal axes since we have no
     * reliable way to know up front which way the frame is oriented.
     */
    private static void convertFailedFrame(Level level, BlockPos ignitionPos) {
        Set<BlockPos> frame = tryFrame(level, ignitionPos, Direction.Axis.X);
        if (frame == null) {
            frame = tryFrame(level, ignitionPos, Direction.Axis.Z);
        }

        if (frame == null) {
            // Couldn't resolve a fully-enclosed frame on either axis - leave
            // the obsidian alone. The portal has already failed to form
            // regardless, so this is a safe (if less satisfying) fallback.
            return;
        }

        for (BlockPos framePos : frame) {
            level.setBlock(framePos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
        }
    }

    /**
     * Flood-fills through the interior (air/fire) cells connected to
     * ignitionPos, assuming the portal's width runs along the given axis.
     * Returns the set of obsidian frame blocks bordering that interior if
     * the region is fully enclosed, or null if this axis doesn't produce a
     * valid, bounded enclosure - meaning either the axis guess is wrong, or
     * this genuinely isn't a legal portal frame.
     */
    @Nullable
    private static Set<BlockPos> tryFrame(Level level, BlockPos ignitionPos, Direction.Axis axis) {
        Direction along = axis == Direction.Axis.X ? Direction.EAST : Direction.NORTH;
        Direction[] planeDirections = { along, along.getOpposite(), Direction.UP, Direction.DOWN };

        Set<BlockPos> interior = new HashSet<>();
        Set<BlockPos> frame = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        interior.add(ignitionPos);
        queue.add(ignitionPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for (Direction dir : planeDirections) {
                BlockPos neighbor = current.relative(dir);
                if (interior.contains(neighbor) || frame.contains(neighbor)) {
                    continue;
                }

                BlockState state = level.getBlockState(neighbor);
                if (state.is(Blocks.OBSIDIAN)) {
                    frame.add(neighbor);
                } else if (isInterior(state)) {
                    interior.add(neighbor);
                    queue.add(neighbor);
                    if (interior.size() > MAX_INTERIOR_BLOCKS) {
                        return null; // leaked into open space - wrong axis, or not enclosed
                    }
                } else {
                    return null; // hit something that's neither frame nor interior
                }
            }
        }

        return frame;
    }

    private static boolean isInterior(BlockState state) {
        // Dark Fire no longer overrides isAir() (that was making the block
        // invisible - isAir() is treated by the client as "skip rendering
        // entirely"). It's added to the minecraft:fire block tag instead,
        // which is what this check - and PortalShape's own interior logic -
        // key off, so this covers vanilla fire, soul fire, and Dark Fire
        // uniformly without needing per-block identity checks.
        return state.isAir() || state.is(BlockTags.FIRE);
    }
}
