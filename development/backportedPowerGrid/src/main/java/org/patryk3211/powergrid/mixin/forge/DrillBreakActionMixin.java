// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Phase P7. Upstream marks a player as mining with the Portable Drill from Forge 1.20's
// PlayerInteractEvent.LeftClickBlock, which carries the block break action (START, STOP, ABORT). Forge 1.19.2 fires that
// event for the start only and has no action, so this hook reads the action where the server handles it: mining
// starts on START_DESTROY_BLOCK and ends on any other action (stop or abort), as upstream's ForgeEvents.leftClickBlock.
package org.patryk3211.powergrid.mixin.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.patryk3211.powergrid.equipment.drill.DrillItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public abstract class DrillBreakActionMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void powerGrid$drillBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci) {
        var stack = player.getMainHandItem();
        if(stack.getItem() instanceof DrillItem drill)
            drill.leftClickEvent(level, pos, stack, player, action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
    }
}
