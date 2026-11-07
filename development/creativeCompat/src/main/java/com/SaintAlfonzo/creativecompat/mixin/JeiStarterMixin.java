package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.jei.PackIngredientOrder;
import mezz.jei.library.startup.JeiStarter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pack ingredient order ({@link PackIngredientOrder}): JEI sorts its list while the plugins register the runtime,
 * before {@code onRuntimeAvailable}, where KubeJS adds and hides ingredients. So the pack sort runs when
 * {@code start()} returns, after every plugin; the head resets the per-start state (tags may have changed).
 */
@Mixin(value = JeiStarter.class, remap = false)
public abstract class JeiStarterMixin {

    @Inject(method = "start", at = @At("HEAD"))
    private void creativecommon$starting(CallbackInfo ci) {
        PackIngredientOrder.onJeiStarting();
    }

    @Inject(method = "start", at = @At("RETURN"))
    private void creativecommon$started(CallbackInfo ci) {
        PackIngredientOrder.onJeiStarted();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void creativecommon$stopping(CallbackInfo ci) {
        PackIngredientOrder.onJeiStopped();
    }
}
