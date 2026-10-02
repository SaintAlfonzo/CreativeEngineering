package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.capture.CaptureShader;
import io.github.mortuusars.exposure.client.util.Shader;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public class SetFilterAction implements CaptureAction {
   protected Optional<ResourceLocation> filter;

   public SetFilterAction(Optional<ResourceLocation> filter) {
      this.filter = filter;
   }

   @Override
   public void beforeCapture() {
      Shader.setSuppressViewfinder(true);
      this.filter.ifPresent(CaptureShader::apply);
   }

   @Override
   public void afterCapture() {
      Shader.setSuppressViewfinder(false);
      this.filter.ifPresent(f -> CaptureShader.remove());
   }
}
