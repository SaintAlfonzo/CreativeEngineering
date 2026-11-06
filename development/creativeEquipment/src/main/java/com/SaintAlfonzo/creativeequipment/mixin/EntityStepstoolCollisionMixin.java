package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.stepstool.Stepstool;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/**
 * A player standing on a stepstool collides with it: the stool's block joins the entity-collision list
 * {@code Entity#collide} ({@code m_20272_}) gathers, on both sides, so the player stands on it and the server's
 * movement checks agree with the client. Only the stool's owner collides with it ({@link Stepstool#withStool}).
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} (same idiom as
 * {@link LivingEntityShieldMixin}). Wrapped in a catch-all so a bug here can never break movement: it degrades to vanilla.
 */
@Mixin(Entity.class)
public abstract class EntityStepstoolCollisionMixin {

    @ModifyVariable(method = "m_20272_", at = @At(value = "STORE", ordinal = 0), ordinal = 0, remap = false)
    private List<VoxelShape> creativeengineering$stepstool(List<VoxelShape> shapes) {
        try {
            return Stepstool.withStool((Entity) (Object) this, shapes);
        } catch (Throwable ignored) {
            return shapes;
        }
    }
}
