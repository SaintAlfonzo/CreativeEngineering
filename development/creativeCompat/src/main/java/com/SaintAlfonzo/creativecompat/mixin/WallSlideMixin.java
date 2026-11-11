package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.WallSlide;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs {@link WallSlide} at the end of {@code Entity#checkInsideBlocks} ({@code m_20101_}), right
 * where vanilla calls honey's {@code entityInside}, so full {@code #creativecompat:wall_slide}
 * blocks slide entities down their sides exactly like honey. Common side: the local player moves
 * itself on the client, mobs on the server.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} (no refmap).
 */
@Mixin(value = Entity.class, remap = false)
public abstract class WallSlideMixin {

    @Inject(method = "m_20101_", at = @At("TAIL"))
    private void creativecompat$wallSlide(CallbackInfo ci) {
        try {
            WallSlide.afterInsideBlocks((Entity) (Object) this);
        } catch (Throwable ignored) {
            // a broken lookup must never stop movement
        }
    }
}
