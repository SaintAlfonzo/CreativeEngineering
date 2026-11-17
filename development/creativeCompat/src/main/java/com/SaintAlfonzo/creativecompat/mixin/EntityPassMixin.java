package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassProjectiles;
import com.SaintAlfonzo.creativecompat.surface.PassSave;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Pass-through phase 2 on every entity (plan_passthrough.md 4.2, 5.1, 5.4): {@code surface.PassSave} writes and reads
 * the state that must survive a reload (stuck items and orbs, demoted arrows) at RETURN of
 * {@code Entity#saveWithoutId} ({@code m_20240_}) and TAIL of {@code Entity#load} ({@code m_20258_}); HEAD of
 * {@code Entity#playerTouch} ({@code m_6123_}, empty in Entity, not overridden by Quark pickarangs or the Botania
 * chakram) picks up a snagged pickarang or chakram ({@code surface.PassProjectiles#playerTouch}).
 * Vanilla-class target, SRG names, {@code remap = false}.
 */
@Mixin(value = Entity.class, remap = false)
public abstract class EntityPassMixin {

    @Inject(method = "m_20240_", at = @At("RETURN"))
    private void creativecompat$passSave(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        try {
            PassSave.save((Entity) (Object) this, cir.getReturnValue());
        } catch (Throwable ignored) {
            // never break a save
        }
    }

    @Inject(method = "m_20258_", at = @At("TAIL"))
    private void creativecompat$passLoad(CompoundTag tag, CallbackInfo ci) {
        try {
            PassSave.load((Entity) (Object) this, tag);
        } catch (Throwable ignored) {
            // never break a load
        }
    }

    @Inject(method = "m_6123_", at = @At("HEAD"))
    private void creativecompat$passTouch(Player player, CallbackInfo ci) {
        try {
            PassProjectiles.playerTouch((Entity) (Object) this, player);
        } catch (Throwable ignored) {
            // a failed pickup leaves the tool where it is
        }
    }
}
