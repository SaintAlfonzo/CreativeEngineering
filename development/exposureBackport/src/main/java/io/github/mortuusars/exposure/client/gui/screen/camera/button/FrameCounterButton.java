package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.ImageButton;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.world.entity.CameraOperator;

public class FrameCounterButton extends ImageButton {
   protected final int secondaryFontColor;
   protected final int mainFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_MAIN_COLOR);

   public FrameCounterButton(int x, int y, int width, int height, WidgetSprites sprites) {
      super(x, y, width, height, sprites, button -> {
      });
      this.secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
   }

   public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {
      MutableComponent tooltipComponent = Component.translatable("gui.exposure.camera_controls.film_frame_counter.tooltip");
      if (!this.cameraHasFilmRoll()) {
         tooltipComponent.append(CommonComponents.NEW_LINE)
            .append(Component.translatable("gui.exposure.camera_controls.film_frame_counter.tooltip.no_film").withStyle(Style.EMPTY.withColor(14508887)));
      }

      this.setTooltip(Tooltip.create(tooltipComponent));
      super.renderWidget(guiGraphics, mouseX, mouseY, pPartialTick);
      String text = this.createText();
      Font font = Minecraft.getInstance().font;
      int textWidth = font.width(text);
      int xPos = 15 + (27 - textWidth) / 2;
      guiGraphics.drawString(font, text, this.getX() + xPos, this.getY() + 8, this.secondaryFontColor, false);
      guiGraphics.drawString(font, text, this.getX() + xPos, this.getY() + 7, this.mainFontColor, false);
   }

   protected String createText() {
      return ((CameraOperator)Minecrft.player()).getActiveExposureCameraOptional().map(camera -> {
         ItemStack filmStack = Attachment.FILM.get(camera.getItemStack()).getForReading();
         if (!filmStack.isEmpty() && filmStack.getItem() instanceof FilmRollItem filmItem) {
            int var5 = filmItem.getStoredFrames(filmStack).size();
            int totalFrames = filmItem.getMaxFrameCount(filmStack);
            return var5 + "/" + totalFrames;
         } else {
            return "-";
         }
      }).orElse("-");
   }

   protected boolean cameraHasFilmRoll() {
      return ((CameraOperator)Minecrft.player()).getActiveExposureCameraOptional().map(camera -> Attachment.FILM.isPresent(camera.getItemStack())).orElse(false);
   }
}
