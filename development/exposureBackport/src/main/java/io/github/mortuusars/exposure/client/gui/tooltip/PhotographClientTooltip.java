package io.github.mortuusars.exposure.client.gui.tooltip;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import org.jetbrains.annotations.NotNull;

public class PhotographClientTooltip implements ClientTooltipComponent {
   public static final int SIZE = 72;
   protected final PhotographTooltip tooltip;
   protected final List<ItemAndStack<PhotographItem>> photographs;

   public PhotographClientTooltip(PhotographTooltip tooltip) {
      this.tooltip = tooltip;
      this.photographs = tooltip.photographs();
   }

   public int getWidth(@NotNull Font font) {
      return 72;
   }

   public int getHeight() {
      return 74;
   }

   public void renderImage(@NotNull Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
      int photographsCount = this.photographs.size();
      int additionalPhotographs = Math.min(2, photographsCount - 1);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate((float)mouseX, (float)mouseY, 5.0F);
      float scale = 72.0F;
      float nextPhotographOffset = ExposureClient.photographRenderer().getStackedPhotographOffset();
      scale *= 1.0F - (float)additionalPhotographs * nextPhotographOffset;
      guiGraphics.pose().scale(scale, scale, 1.0F);
      BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
      ExposureClient.photographRenderer().renderStackedPhotographs(this.photographs, guiGraphics.pose(), bufferSource, 15728880, 255, 255, 255, 255);
      bufferSource.endBatch();
      guiGraphics.pose().popPose();
      if (photographsCount > 1) {
         guiGraphics.pose().pushPose();
         String count = Integer.toString(photographsCount);
         int fontWidth = Minecraft.getInstance().font.width(count);
         float fontScale = 1.6F;
         guiGraphics.pose().translate((float)mouseX + scale - 2.0F - (float)fontWidth * fontScale, (float)mouseY + scale - 2.0F - 8.0F * fontScale, 10.0F);
         guiGraphics.pose().scale(fontScale, fontScale, fontScale);
         guiGraphics.drawString(font, count, 0, 0, -1);
         guiGraphics.pose().popPose();
      }
   }
}
