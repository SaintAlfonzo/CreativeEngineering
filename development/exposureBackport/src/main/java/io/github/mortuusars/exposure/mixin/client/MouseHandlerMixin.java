package io.github.mortuusars.exposure.mixin.client;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Port: scroll and button presses go through Forge's InputEvent.MouseScrollingEvent / MouseButton.Pre
 * (forge.event.ForgeClientEvents); only the sensitivity hook needs a mixin.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
   /** The cubed sensitivity ({@code d5} in 1.19.2, also the 4th double local in 1.21). */
   @ModifyVariable(method = "turnPlayer()V", at = @At("STORE"), ordinal = 3)
   private double modifySensitivity(double sensitivity) {
      return io.github.mortuusars.exposure.client.input.MouseHandler.modifySensitivity(sensitivity);
   }
}
