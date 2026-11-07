package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.SinkingRender;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lowers the camera as if the viewed entity had really sunk into a sinking block
 * ({@link SinkingRender}). {@code Camera#setup} ({@code m_90575_}) places the camera once at the
 * interpolated eye position ({@code setPosition(DDD)}, {@code m_90584_}) and only then pulls it
 * back for third person (with its own wall check), so lowering that one Y argument matches a real
 * sink in every view. Fluid/fog checks read the camera position, so they follow too. HEAD stashes
 * the entity and partial tick because the ModifyArg handler only sees setPosition's arguments.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} (no refmap).
 */
@Mixin(value = Camera.class, remap = false)
public abstract class CameraSinkMixin {

    @Unique
    private Entity creativeengineering$viewer;

    @Unique
    private float creativeengineering$partialTick;

    @Inject(method = "m_90575_", at = @At("HEAD"))
    private void creativeengineering$captureViewer(BlockGetter level, Entity entity, boolean detached,
            boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        this.creativeengineering$viewer = entity;
        this.creativeengineering$partialTick = partialTick;
    }

    @ModifyArg(method = "m_90575_", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;m_90584_(DDD)V", ordinal = 0), index = 1)
    private double creativeengineering$sinkEye(double eyeY) {
        try {
            return eyeY - SinkingRender.depth(this.creativeengineering$viewer, this.creativeengineering$partialTick);
        } catch (Throwable ignored) {
            return eyeY;
        }
    }
}
