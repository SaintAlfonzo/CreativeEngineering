package io.github.mortuusars.exposure.client.util;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.capture.CaptureShader;
import java.io.IOException;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class Shader {
   private static boolean suppressViewfinder = false;

   public static void process(@NotNull PostChain shader, @NotNull RenderTarget renderTarget) {
      try {
         ResourceLocation shaderLocation = new ResourceLocation(shader.getName());
         PostChain tempShader = new PostChain(Minecrft.get().getTextureManager(), Minecrft.get().getResourceManager(), renderTarget, shaderLocation);
         tempShader.resize(renderTarget.width, renderTarget.height);
         RenderSystem.disableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.resetTextureMatrix();
         tempShader.process(Minecrft.get().getDeltaFrameTime());
      } catch (IOException var4) {
         Exposure.LOGGER.warn("Failed to load shader: {}", shader.getName(), var4);
      } catch (JsonSyntaxException var5) {
         Exposure.LOGGER.warn("Failed to parse shader: {}", shader.getName(), var5);
      }
   }

   public static void setSuppressViewfinder(boolean suppress) {
      suppressViewfinder = suppress;
   }

   public static void processForGameRenderer() {
      if (!suppressViewfinder && CameraClient.viewfinder() != null) {
         CameraClient.viewfinder().shader().process();
      }

      if (CaptureShader.hasShader()) {
         CaptureShader.process();
      }
   }

   public static void resize(int width, int height) {
      if (CameraClient.viewfinder() != null) {
         CameraClient.viewfinder().shader().resize(width, height);
      }

      if (CaptureShader.hasShader()) {
         CaptureShader.resize(width, height);
      }
   }
}
