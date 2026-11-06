package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.surface.SinkingRender;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws entities standing on {@code #creativeengineering:sinking} blocks a little lower
 * ({@link SinkingRender}). {@code EntityRenderDispatcher#render} ({@code m_114384_}) translates by
 * the renderer's offset ({@code vec3}, its only Vec3 local), renders the model and name tag, then
 * translates back before the shadow, fire and debug hitbox - so lowering that offset moves only
 * the model. HEAD stashes the entity and partial tick because the STORE handler can't see method
 * arguments. {@link CameraSinkMixin} moves the camera by the same amount.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} (no refmap).
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntitySinkRenderMixin {

    @Unique
    private Entity creativeengineering$rendering;

    @Unique
    private float creativeengineering$partialTick;

    @Inject(method = "m_114384_", at = @At("HEAD"), remap = false)
    private void creativeengineering$captureEntity(Entity entity, double x, double y, double z, float yRot,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        this.creativeengineering$rendering = entity;
        this.creativeengineering$partialTick = partialTicks;
    }

    @ModifyVariable(method = "m_114384_", at = @At("STORE"), ordinal = 0, remap = false)
    private Vec3 creativeengineering$sinkModel(Vec3 offset) {
        try {
            double depth = SinkingRender.depth(this.creativeengineering$rendering, this.creativeengineering$partialTick);
            return depth == 0 ? offset : offset.subtract(0, depth, 0);
        } catch (Throwable ignored) {
            return offset;
        }
    }
}
