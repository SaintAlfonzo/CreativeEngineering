package umpaz.brewinandchewin.common.mixin.client.integration.appleskin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import squeek.appleskin.client.HUDOverlayHandler;
import squeek.appleskin.util.IntPoint;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCEffects;

/**
 * Intoxication makes AppleSkin's hunger overlay (saturation and food previews) wobble, upstream's numbers. Port: 1.19
 * AppleSkin (2.4.2) has no OffsetsCache; its per-frame {@code generateHungerBarOffsets} fills {@code foodBarOffsets},
 * so the wobble is added to each icon's offset when it returns. {@code @Pseudo}: AppleSkin is optional.
 */
@Pseudo
@Mixin(targets = "squeek.appleskin.client.HUDOverlayHandler", remap = false)
public class HUDOverlayHandlerMixin {

   @Inject(method = "generateHungerBarOffsets", at = @At("TAIL"), remap = false)
   private static void brewinandchewin$intoxicationWobble(int top, int left, int right, int ticks, Player player, CallbackInfo ci) {
      if (player == null || !player.hasEffect(BnCEffects.INTOXICATION) || !BnCConfiguration.CLIENT_CONFIG.get().intoxicationFoodOverlay()) {
         return;
      }
      for (int i = 0; i < HUDOverlayHandler.foodBarOffsets.size(); i++) {
         IntPoint point = HUDOverlayHandler.foodBarOffsets.get(i);
         if (point != null) {
            point.x += (int)(Mth.cos((float)(ticks + i * 2) * 0.2F) * 2.0F);
            point.y += (int)(Mth.sin((float)(ticks + i * 2) * 0.25F) * 2.0F);
         }
      }
   }
}
