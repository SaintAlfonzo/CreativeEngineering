package io.github.mortuusars.exposure_polaroid.client.gui.screen.camera.button;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import io.github.mortuusars.exposure_polaroid.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.AbstractWidget;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ZoomWidget extends AbstractWidget {
   protected final WidgetSprites sprites;
   protected final Camera camera;
   protected final int secondaryFontColor;
   protected final int mainFontColor;

   public ZoomWidget(int x, int y, int width, int height, WidgetSprites sprites, Camera camera) {
      super(x, y, width, height, Component.empty());
      this.sprites = sprites;
      this.camera = camera;
      this.mainFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_MAIN_COLOR);
      this.secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
   }

   public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      boolean isFar = this.camera.map((i, s) -> (double)((Float)CameraSettings.ZOOM.getOrDefault(s)).floatValue() > 0.5).orElse(false);
      Font font = Minecraft.getInstance().font;
      MutableComponent text = Component.translatable("gui.exposure_polaroid.camera_controls.zoom." + (isFar ? "far" : "near"));
      int textWidth = font.width(text);
      int xPos = 17 + (29 - textWidth) / 2;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ResourceLocation sliderSprite = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
      guiGraphics.blitSprite(sliderSprite, this.getX(), this.getY(), this.width, this.height);
      guiGraphics.drawString(font, text, this.getX() + xPos, this.getY() + 8, this.secondaryFontColor, false);
      guiGraphics.drawString(font, text, this.getX() + xPos, this.getY() + 7, this.mainFontColor, false);
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }
}
