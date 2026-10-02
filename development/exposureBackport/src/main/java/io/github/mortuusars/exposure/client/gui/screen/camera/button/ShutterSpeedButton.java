package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.NotNull;

public class ShutterSpeedButton extends CycleButton<ShutterSpeed> {
   protected final int secondaryFontColor;
   protected final int mainFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_MAIN_COLOR);

   public ShutterSpeedButton(
      int x,
      int y,
      int width,
      int height,
      List<ShutterSpeed> values,
      @NotNull ShutterSpeed startingValue,
      Function<ShutterSpeed, WidgetSprites> spritesFunc,
      CycleButton.OnCycle<ShutterSpeed> onCycle
   ) {
      super(x, y, width, height, values, startingValue, spritesFunc, onCycle);
      this.secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
   }

   public ShutterSpeedButton(
      int x, int y, int width, int height, List<ShutterSpeed> values, @NotNull ShutterSpeed startingValue, Function<ShutterSpeed, WidgetSprites> spritesFunc
   ) {
      super(x, y, width, height, values, startingValue, spritesFunc, (button, newValue) -> {
      });
      this.secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
   }

   @Override
   public void playDownSound(SoundManager handler) {
      if (this.clickSound != null) {
         handler.play(
            SimpleSoundInstance.forUI(this.clickSound, ThreadLocalRandom.current().nextFloat() * 0.05F + 0.9F + (float)this.currentIndex * 0.01F, 0.7F)
         );
      }
   }

   @Override
   public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
      ShutterSpeed shutterSpeed = this.getCurrentValue();
      String text = shutterSpeed.getNotation().replace("1/", "");
      if (shutterSpeed.equals(ShutterSpeed.DEFAULT)) {
         text = text + "•";
      }

      Font font = Minecraft.getInstance().font;
      int textWidth = font.width(text);
      int xPos = this.width / 2 - textWidth / 2 + 1;
      guiGraphics.drawString(font, text, this.getX() + xPos, this.getY() + 4, this.secondaryFontColor, false);
      guiGraphics.drawString(font, text, this.getX() + xPos, this.getY() + 3, this.mainFontColor, false);
   }
}
