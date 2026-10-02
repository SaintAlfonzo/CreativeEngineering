package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.render.GammaModifier;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.client.Minecraft;

public class ModifyGammaAction implements CaptureAction {
   protected final float offset;

   public ModifyGammaAction(float offset) {
      this.offset = offset;
   }

   public ModifyGammaAction(float brightnessStops, float gammaPerStop) {
      float currentGamma = ((Double)Minecraft.getInstance().options.gamma().get()).floatValue();
      float strength = (1.0F - currentGamma) * 0.65F + 0.35F;
      this.offset = gammaPerStop * brightnessStops * strength;
   }

   public ModifyGammaAction(ShutterSpeed shutterSpeed) {
      this(shutterSpeed.getStops(), 0.03F);
   }

   @Override
   public void beforeCapture() {
      GammaModifier.apply(this.offset);
   }

   @Override
   public void afterCapture() {
      GammaModifier.restore();
   }
}
