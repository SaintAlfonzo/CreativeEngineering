package io.github.mortuusars.exposure.client.capture;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CaptureShader {
   @Nullable
   private static PostChain shader = null;

   public static boolean hasShader() {
      return shader != null;
   }

   public static void apply(ResourceLocation shaderLocation) {
      if (shader != null) {
         if (shader.getName().equals(shaderLocation.toString())) {
            return;
         }

         shader.close();
      }

      try {
         Minecraft minecraft = Minecrft.get();
         shader = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), shaderLocation);
         shader.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
      } catch (IOException var2) {
         Exposure.LOGGER.warn("Failed to load shader: {}", shaderLocation, var2);
         remove();
      } catch (JsonSyntaxException var3) {
         Exposure.LOGGER.warn("Failed to parse shader: {}", shaderLocation, var3);
         remove();
      }
   }

   public static void resize(int width, int height) {
      if (shader != null) {
         shader.resize(width, height);
      }
   }

   public static void process() {
      if (shader != null) {
         RenderSystem.disableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.resetTextureMatrix();
         shader.process(Minecrft.get().getDeltaFrameTime());
      }
   }

   public static void process(RenderTarget renderTarget) {
      if (shader != null) {
         process(shader, renderTarget);
      }
   }

   public static void process(@NotNull PostChain shader, @NotNull RenderTarget renderTarget) {
      try {
         ResourceLocation shaderLocation = new ResourceLocation(shader.getName());
         PostChain tempShader = new PostChain(Minecrft.get().getTextureManager(), Minecrft.get().getResourceManager(), renderTarget, shaderLocation);
         tempShader.resize(renderTarget.width, renderTarget.height);
         RenderSystem.disableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.resetTextureMatrix();
         tempShader.process(Minecrft.get().getDeltaFrameTime());
         tempShader.close();
      } catch (IOException var4) {
         Exposure.LOGGER.warn("Failed to load shader: {}", shader.getName(), var4);
      } catch (JsonSyntaxException var5) {
         Exposure.LOGGER.warn("Failed to parse shader: {}", shader.getName(), var5);
      }
   }

   public static void remove() {
      if (shader != null) {
         shader.close();
      }

      shader = null;
   }
}
