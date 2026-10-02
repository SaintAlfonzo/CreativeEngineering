package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements IForgeKeyMapping {
   @Shadow
   private boolean isDown;

   @Inject(method = "isDown()Z", at = @At("HEAD"), cancellable = true)
   private void isDown(CallbackInfoReturnable<Boolean> cir) {
      if (exposure$controlsScreenOpen()) {
         cir.setReturnValue(this.isDown);
      }
   }

   /**
    * Port of upstream neoforge.mixin.KeyMappingExtensionNeoForgeMixin: keys count as active (conflict context and
    * modifier) while the viewfinder controls screen is open. Mixin 0.8.5 can't inject into the interface default, so
    * this overrides it on KeyMapping with the same default logic.
    */
   @Override
   public boolean isConflictContextAndModifierActive() {
      if (exposure$controlsScreenOpen()) {
         return true;
      }
      return this.getKeyConflictContext().isActive() && this.getKeyModifier().isActive(this.getKeyConflictContext());
   }

   private static boolean exposure$controlsScreenOpen() {
      return CameraClient.viewfinder() != null && CameraClient.viewfinder().controlsScreen().map(screen -> screen == Minecrft.get().screen).orElse(false);
   }
}
