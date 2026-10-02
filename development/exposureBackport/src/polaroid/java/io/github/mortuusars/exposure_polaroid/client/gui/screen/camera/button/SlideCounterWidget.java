package io.github.mortuusars.exposure_polaroid.client.gui.screen.camera.button;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure_polaroid.Config;
import io.github.mortuusars.exposure_polaroid.world.item.InstantCameraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.AbstractWidget;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class SlideCounterWidget extends AbstractWidget {
   protected final WidgetSprites sprites;
   protected final Camera camera;
   protected final int secondaryFontColor;
   protected final int mainFontColor;

   public SlideCounterWidget(int x, int y, int width, int height, WidgetSprites sprites, Camera camera) {
      super(x, y, width, height, Component.empty());
      this.sprites = sprites;
      this.camera = camera;
      this.mainFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_MAIN_COLOR);
      this.secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
   }

   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      MutableComponent tooltipComponent = Component.translatable("gui.exposure_polaroid.camera_controls.slide_counter.tooltip");
      if (!this.cameraHasSlides()) {
         tooltipComponent.append(CommonComponents.NEW_LINE)
            .append(Component.translatable("gui.exposure_polaroid.camera_controls.slide_counter.tooltip.no_slides").withStyle(Style.EMPTY.withColor(14508887)));
      }

      this.setTooltip(Tooltip.create(tooltipComponent));
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ResourceLocation sliderSprite = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
      guiGraphics.blitSprite(sliderSprite, this.getX(), this.getY(), this.width, this.height);
      String text = this.createText();
      Font font = Minecraft.getInstance().font;
      int textWidth = font.width(text);
      int xPos = 15 + (27 - textWidth) / 2;
      guiGraphics.drawString(font, text, this.getX() + xPos, this.getY() + 8, this.secondaryFontColor, false);
      guiGraphics.drawString(font, text, this.getX() + xPos, this.getY() + 7, this.mainFontColor, false);
   }

   protected String createText() {
      return this.camera.map((i, s) -> {
         if (i instanceof InstantCameraItem instantCamera && instantCamera.getRemainingSlides(s) != 0) {
            int remainingSlides = instantCamera.getRemainingSlides(s);
            int maxSlides = instantCamera.getMaxSlideCount();
            return remainingSlides + "/" + maxSlides;
         }

         return "-";
      }).orElse("-");
   }

   protected boolean cameraHasSlides() {
      return this.camera.map((i, s) -> {
         if (i instanceof InstantCameraItem instantCamera && instantCamera.getRemainingSlides(s) > 0) {
            return true;
         }

         return false;
      }).orElse(false);
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }
}
