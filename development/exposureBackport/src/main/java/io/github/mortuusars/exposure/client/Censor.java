package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class Censor {
   public static boolean isAllowedToRender(Frame frame) {
      if (frame.identifier().isTexture()) {
         return true;
      } else {
         Player player = Minecraft.getInstance().player;
         return !Config.Client.HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS.get() || player != null && frame.isTakenBy(player)
            ? !(Boolean)Config.Client.HIDE_PROJECTED_PHOTOGRAPHS_MADE_BY_OTHERS.get() || !frame.isProjected() || player != null && frame.isTakenBy(player)
            : false;
      }
   }
}
