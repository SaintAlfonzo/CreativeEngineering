package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassFov;

import fuzs.blockrunner.client.helper.FieldOfViewHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BlockRunner's {@code FieldOfViewHelper#shouldRemoveBlockSpeedModifier} answers false except when
 * {@link PassFov} asks, so BlockRunner's own field of view handler always keeps the value and only
 * {@code PassFovMixin} recomputes it (skipping BlockRunner's and the pass-through modifier, whatever order the two
 * handlers run in). Client only; applies only with BlockRunner (compat/CompatMixinPlugin). {@code require = 0}: a
 * BlockRunner update that renames the method leaves BlockRunner's own behaviour.
 */
@Mixin(value = FieldOfViewHelper.class, remap = false)
public abstract class BlockRunnerFovMixin {

    @Inject(method = "shouldRemoveBlockSpeedModifier", at = @At("HEAD"), cancellable = true, require = 0)
    private static void creativecompat$onlyForPassFov(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!PassFov.querying()) {
            cir.setReturnValue(false);
        }
    }
}
