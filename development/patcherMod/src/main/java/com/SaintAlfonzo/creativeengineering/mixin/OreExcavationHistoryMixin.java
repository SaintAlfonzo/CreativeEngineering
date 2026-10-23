package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.excavation.VeinUndoAccess;

import java.util.Deque;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import oreexcavation.undo.BlockHistory;
import oreexcavation.undo.ExcavateHistory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Exposes the vein mine's broken blocks to {@code excavation/VeinUndo}, and two fixes to Ore Excavation's undo restore, which the TPS guard can spread over several ticks:
 * <ul>
 *   <li>It takes the vein mine's XP back on every tick of the restore; the XP is zeroed after the first take.</li>
 *   <li>A player who logs out mid-restore crashed the server tick (null player); the restore now waits for them.</li>
 * </ul>
 * Ore Excavation classes are unobfuscated and this project ships no Mixin refmap, so {@code remap = false}.
 */
@Mixin(value = ExcavateHistory.class, remap = false)
public class OreExcavationHistoryMixin implements VeinUndoAccess.History {

    @Shadow(remap = false)
    @Final
    private Deque<BlockHistory> history;

    @Shadow(remap = false)
    private long experience;

    @Override
    public Deque<BlockHistory> creativeengineering$history() {
        return history;
    }

    @Inject(method = "tickRestore", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$waitForPlayer(MinecraftServer server, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) cir.setReturnValue(false);
    }

    @Inject(method = "tickRestore", remap = false, require = 1,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, remap = false,
                    target = "Loreexcavation/utils/XPHelper;addXP(Lnet/minecraft/world/entity/player/Player;JZ)V"))
    private void creativeengineering$takeXpOnce(MinecraftServer server, Player player, CallbackInfoReturnable<Boolean> cir) {
        experience = 0L;
    }
}
