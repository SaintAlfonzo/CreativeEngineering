package com.SaintAlfonzo.creativecompat.excavation;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

import net.minecraft.world.level.block.state.BlockState;
import oreexcavation.core.ExcavationSettings;
import oreexcavation.handlers.MiningScheduler;
import oreexcavation.undo.BlockHistory;
import oreexcavation.undo.ExcavateHistory;

/**
 * Ore Excavation internals that {@link VeinUndo} reads. Its obfuscator marks every field synthetic, which javac
 * won't reference: instance fields come through the {@code mixin/OreExcavation*Mixin}s ({@code @Shadow} matches by
 * name and type at runtime) and the two statics through reflection, looked up once.
 */
public final class VeinUndoAccess {

    private static final Field SCHEDULER = field(MiningScheduler.class, "INSTANCE");
    private static final Field AUTO_PICKUP = field(ExcavationSettings.class, "autoPickup");

    private VeinUndoAccess() {}

    /** On {@code MiningScheduler}: per-player undo stacks, newest first. */
    public interface Scheduler {
        Map<UUID, ArrayDeque<ExcavateHistory>> creativeengineering$undoHistory();
    }

    /** On {@code ExcavateHistory}: the blocks one vein mine broke. */
    public interface History {
        Deque<BlockHistory> creativeengineering$history();
    }

    /** On {@code BlockHistory}: the block state before it was broken. */
    public interface Block {
        BlockState creativeengineering$state();
    }

    public static Scheduler scheduler() {
        try {
            return (Scheduler) SCHEDULER.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Ore Excavation's "Auto Pickup" setting. */
    public static boolean autoPickup() {
        try {
            return AUTO_PICKUP.getBoolean(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Field field(Class<?> owner, String name) {
        try {
            return owner.getField(name);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Ore Excavation changed: no " + owner.getSimpleName() + "." + name, e);
        }
    }
}
