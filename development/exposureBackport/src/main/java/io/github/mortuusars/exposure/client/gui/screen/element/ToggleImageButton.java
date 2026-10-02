package io.github.mortuusars.exposure.client.gui.screen.element;

import java.util.function.Consumer;
import java.util.function.Function;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.ImageButton;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

public class ToggleImageButton extends ImageButton {
   protected final WidgetSprites onSprites;
   protected final Consumer<Boolean> onToggled;
   protected boolean state;

   public ToggleImageButton(int x, int y, int width, int height, WidgetSprites offSprites, WidgetSprites onSprites, Consumer<Boolean> onToggled) {
      super(x, y, width, height, offSprites, b -> {
      });
      this.onSprites = onSprites;
      this.onToggled = onToggled;
   }

   public boolean isOn() {
      return this.state;
   }

   public boolean isOff() {
      return !this.isOn();
   }

   public void toggle() {
      this.state = !this.state;
      this.onToggled.accept(this.state);
   }

   public void setState(boolean state) {
      this.state = state;
   }

   public <T> T mapState(Function<Boolean, T> mappingFunction) {
      return mappingFunction.apply(this.state);
   }

   public void onPress() {
      this.toggle();
   }

   public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      WidgetSprites sprites = this.isOn() ? this.onSprites : this.sprites;
      ResourceLocation resourceLocation = sprites.get(this.isActive(), this.isHoveredOrFocused());
      guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
   }
}
