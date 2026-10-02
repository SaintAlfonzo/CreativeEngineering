package io.github.mortuusars.exposure.client.gui.tooltip;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import io.github.mortuusars.exposure.backport.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.EntityHitResult;

public class CameraStandTooltip {
   public static void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
      if ((Boolean)Config.Client.CAMERA_STAND_TOOLTIP.get()) {
         Minecraft minecraft = Minecrft.get();
         if (!minecraft.options.hideGui) {
            if (minecraft.level != null && minecraft.player != null) {
               if (minecraft.screen == null && minecraft.hitResult instanceof EntityHitResult entityHitResult) {
                  if (!(entityHitResult.getEntity() instanceof CameraStandEntity stand)) {
                     return;
                  }

                  if (!stand.getCamera().isEmpty()) {
                     int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 16;
                     int y = minecraft.getWindow().getGuiScaledHeight() / 2 - 9;
                     if (stand.isMalfunctioned()) {
                        List<FormattedCharSequence> lines = Minecrft.get()
                           .font
                           .split(Component.translatable("gui.exposure.camera_stand.tooltip.malfunctioned").withStyle(ChatFormatting.RED), 230);
                        guiGraphics.renderTooltip(minecraft.font, lines, x, y + 12);
                     } else {
                        TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y, 18, 18, 400);
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
                        guiGraphics.renderItem(stand.getCamera(), x + 1, y + 1);
                        guiGraphics.pose().popPose();
                        guiGraphics.renderTooltip(minecraft.font, stand.getCamera(), x + 16, y + 12);
                     }
                  }
               }
            }
         }
      }
   }
}
