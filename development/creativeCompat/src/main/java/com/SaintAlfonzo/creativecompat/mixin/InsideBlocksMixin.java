package com.SaintAlfonzo.creativecompat.mixin;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.surface.PassHolder;
import com.SaintAlfonzo.creativecompat.surface.PassItems;
import com.SaintAlfonzo.creativecompat.surface.PassState;
import com.SaintAlfonzo.creativecompat.surface.PassThrough;
import com.SaintAlfonzo.creativecompat.surface.PassTiming;
import com.SaintAlfonzo.creativecompat.surface.WallSlide;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The end of {@code Entity#checkInsideBlocks} ({@code m_20101_}), right where vanilla has run every block's
 * {@code entityInside}: first {@link WallSlide} (full {@code #creativecompat:wall_slide} blocks slide entities down
 * their sides like honey), then {@link PassThrough} (walking and falling through pass-through blocks,
 * plan_passthrough.md; timed when {@code [debug.passthrough] timing} is on, {@link PassTiming}). Common side: the local player moves itself on the client, mobs on the server. Was
 * WallSlideMixin until the pass-through phase 1 (2026-11-14).
 *
 * <p>Also gives every entity its {@link PassState} field ({@link PassHolder}), created on first use. Two more hooks in
 * {@code Entity#move} ({@code m_6478_}, scratch fixes 2026-11-15): at HEAD an item or orb held in a web skips the
 * whole move ({@link PassItems#heldMove}: only its web is checked), and the fast-fall reset clip ({@code Level#clip},
 * {@code m_45547_}) leaves out webs whose fall factor applies ({@link PassThrough#fallResetClip}); {@code require = 0}
 * so another mod's redirect of the same call wins quietly.
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

    @Inject(method = "m_6478_", at = @At("HEAD"), cancellable = true)
    private void creativecompat$heldMove(MoverType type, Vec3 motion, CallbackInfo ci) {
        PassState st = creativecompat$pass;
        if (st == null || type != MoverType.SELF || !PassItems.stuckState(st)) return;
        try {
            if (PassItems.heldMove((Entity) (Object) this, st)) ci.cancel();
        } catch (Throwable ignored) {
            // move as usual
        }
    }

    @Redirect(method = "m_6478_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;m_45547_(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"),
            require = 0)
    private BlockHitResult creativecompat$fallResetClip(Level level, ClipContext ctx) {
        BlockHitResult hit = level.clip(ctx);
        if (hit.getType() == HitResult.Type.MISS) return hit;
        try {
            return PassThrough.fallResetClip((Entity) (Object) this, level, ctx, hit);
        } catch (Throwable t) {
            return hit;
        }
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
            if (PassTiming.on) {
                long t = System.nanoTime();
                PassThrough.afterInsideBlocks(self);
                PassTiming.hook(self, System.nanoTime() - t);
            } else {
                PassThrough.afterInsideBlocks(self);
            }
        } catch (Throwable ignored) {
            // a broken lookup must never stop movement
        }
    }
}
