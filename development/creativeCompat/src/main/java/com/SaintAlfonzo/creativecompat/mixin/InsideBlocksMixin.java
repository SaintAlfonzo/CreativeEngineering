package com.SaintAlfonzo.creativecompat.mixin;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.surface.PassHolder;
import com.SaintAlfonzo.creativecompat.surface.PassState;
import com.SaintAlfonzo.creativecompat.surface.PassThrough;
import com.SaintAlfonzo.creativecompat.surface.WallSlide;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The end of {@code Entity#checkInsideBlocks} ({@code m_20101_}), right where vanilla has run every block's
 * {@code entityInside}: first {@link WallSlide} (full {@code #creativecompat:wall_slide} blocks slide entities down
 * their sides like honey), then {@link PassThrough} (walking and falling through pass-through blocks,
 * plan_passthrough.md). Common side: the local player moves itself on the client, mobs on the server. Was
 * WallSlideMixin until the pass-through phase 1 (2026-11-14).
 *
 * <p>Also gives every entity its {@link PassState} field ({@link PassHolder}), created on first use.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} (no refmap).
 */
@Mixin(value = Entity.class, remap = false)
public abstract class InsideBlocksMixin implements PassHolder {

    @Unique
    @Nullable
    private PassState creativecompat$pass;

    @Override
    public PassState creativecompat$passState(boolean create) {
        if (creativecompat$pass == null && create) {
            creativecompat$pass = new PassState();
        }
        return creativecompat$pass;
    }

    @Inject(method = "m_20101_", at = @At("TAIL"))
    private void creativecompat$insideBlocks(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        try {
            WallSlide.afterInsideBlocks(self);
        } catch (Throwable ignored) {
            // a broken lookup must never stop movement
        }
        try {
            PassThrough.afterInsideBlocks(self);
        } catch (Throwable ignored) {
            // a broken lookup must never stop movement
        }
    }
}
