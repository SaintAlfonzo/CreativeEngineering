package io.github.mortuusars.exposure.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class GammaModifier {
   private static float offset = 0.0F;

   public static float getOffset() {
      return offset;
   }

   public static void apply(float offsetValue) {
      offsetValue = Mth.clamp(offsetValue, -1.0F, 1.0F);
      if (offset != offsetValue) {
         offset = offsetValue;
         Minecraft.getInstance().gameRenderer.lightTexture().tick();
      }
   }

   public static void restore() {
      if (offset != 0.0F) {
         offset = 0.0F;
         Minecraft.getInstance().gameRenderer.lightTexture().tick();
      }
   }

   public static float getModifiedValue(float original) {
      return original + offset;
   }
}
