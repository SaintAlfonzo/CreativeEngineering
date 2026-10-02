package io.github.mortuusars.exposure.client.gui.screen.test;

import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class ShutterSpeedSlider extends Slider {
   private final List<ShutterSpeed> shutterSpeeds;

   public ShutterSpeedSlider(
      int x, int y, int width, int height, String name, List<ShutterSpeed> shutterSpeeds, ShutterSpeed defaultShutterSpeed, Consumer<Double> onChanged
   ) {
      super(x, y, width, height, (double)Math.max(shutterSpeeds.indexOf(defaultShutterSpeed), 0), 0.0, (double)(shutterSpeeds.size() - 1), 0, name, onChanged);
      this.shutterSpeeds = shutterSpeeds;
   }

   public ShutterSpeed getShutterSpeed() {
      if (this.shutterSpeeds == null) {
         return ShutterSpeed.DEFAULT;
      } else {
         int index = this.getValue().intValue();
         return index >= 0 && index < this.shutterSpeeds.size() ? this.shutterSpeeds.get(index) : ShutterSpeed.DEFAULT;
      }
   }

   @Override
   protected void updateMessage() {
      this.setMessage(Component.literal(this.name + ": " + this.getShutterSpeed().getNotation()));
   }
}
