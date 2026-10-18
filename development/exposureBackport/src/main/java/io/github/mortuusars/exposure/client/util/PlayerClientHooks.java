package io.github.mortuusars.exposure.client.util;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.world.camera.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Client half of {@code mixin.PlayerMixin}'s camera methods. Upstream overrides them in a LocalPlayer mixin (calling
 * {@code super}), which doesn't compile without interface injection; PlayerMixin calls these instead.
 */
public class PlayerClientHooks {
   public static void onActiveCameraSet(Player player, @Nullable Camera camera) {
      if (player instanceof LocalPlayer && camera != null) {
         CameraClient.setupViewfinder(camera);
      }
   }

   public static void onActiveCameraRemoved(Player player) {
      if (player instanceof LocalPlayer) {
         CameraClient.removeViewfinder();
      }
   }

   /**
    * Upstream LocalPlayerMixin's tick (RETURN). The viewfinder is static, so it outlives the LocalPlayer that opened it
    * (respawn, dimension change, rejoin); drop it once the ticking player has no camera, or it keeps eating clicks.
    */
   public static void onLocalPlayerTick(Player player, @Nullable Camera activeCamera) {
      if (player instanceof LocalPlayer) {
         if (activeCamera == null && CameraClient.viewfinder() != null) {
            CameraClient.removeViewfinder();
         }
         CameraClient.tick();
      }
   }
}
