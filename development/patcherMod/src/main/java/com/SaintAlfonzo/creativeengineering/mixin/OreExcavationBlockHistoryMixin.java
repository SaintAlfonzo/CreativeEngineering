package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.excavation.VeinUndoAccess;

import net.minecraft.world.level.block.state.BlockState;
import oreexcavation.undo.BlockHistory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Exposes a broken block's state to {@code excavation/VeinUndo}. The field is synthetic (obfuscator), so the
 * annotation processor warns it can't find it; Mixin finds it at runtime. No refmap: {@code remap = false}.
 */
@Mixin(value = BlockHistory.class, remap = false)
public class OreExcavationBlockHistoryMixin implements VeinUndoAccess.Block {

    @Shadow(remap = false)
    @Final
    public BlockState state;

    @Override
    public BlockState creativeengineering$state() {
        return state;
    }
}
