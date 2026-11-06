package com.SaintAlfonzo.creativecompat.mixin;

import com.github.alexthe668.domesticationinnovation.server.entity.FeatherEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reeling in a Feather on a Stick that holds a mob lets go without yanking the mob at the player
 * ({@code FishingHook.pullEntity}, m_150155_; both sides, since the client pulls a hooked local player itself).
 * Fishing rods are unchanged.
 */
@Mixin(value = FishingHook.class, remap = false)
public abstract class FishingHookFeatherPullMixin {

    @Inject(method = "m_150155_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativecompat$featherNoPull(Entity entity, CallbackInfo ci) {
        if ((Object) this instanceof FeatherEntity) {
            ci.cancel();
        }
    }
}
