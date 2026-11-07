package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.excavation.VeinUndoAccess;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;

import oreexcavation.handlers.MiningScheduler;
import oreexcavation.undo.ExcavateHistory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Exposes Ore Excavation's per-player undo stacks to {@code excavation/VeinUndo}. No refmap: {@code remap = false}. */
@Mixin(value = MiningScheduler.class, remap = false)
public class OreExcavationSchedulerMixin implements VeinUndoAccess.Scheduler {

    @Shadow(remap = false)
    @Final
    private Map<UUID, ArrayDeque<ExcavateHistory>> undoHistory;

    @Override
    public Map<UUID, ArrayDeque<ExcavateHistory>> creativeengineering$undoHistory() {
        return undoHistory;
    }
}
