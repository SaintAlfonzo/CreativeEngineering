package io.github.mortuusars.exposure.mixin.client;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardHandler.class})
public abstract class KeyboardHandlerMixin {
   @Inject(
      method = {"keyPress(JIIII)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void keyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
      if (io.github.mortuusars.exposure.client.input.KeyboardHandler.handleKeyPress(windowPointer, key, scanCode, action, modifiers)) {
         ci.cancel();
      }
   }
}
