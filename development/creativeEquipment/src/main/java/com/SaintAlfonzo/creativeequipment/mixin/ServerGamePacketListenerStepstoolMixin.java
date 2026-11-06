package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.stepstool.Stepstool;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A player on a stepstool has a block under them that the world does not: the server's floating check
 * ({@code noBlocksAround}, {@code m_9793_}) would otherwise kick them for flying after 4 seconds.
 * SRG name, {@code remap = false}, as {@link LivingEntityShieldMixin}.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerStepstoolMixin {

    @Inject(method = "m_9793_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$standingOnStool(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (Stepstool.anchor(entity) != null) {
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {
            // vanilla's own check
        }
    }
}
