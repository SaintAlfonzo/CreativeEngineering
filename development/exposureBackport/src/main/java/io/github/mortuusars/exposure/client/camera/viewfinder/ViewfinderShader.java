package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Filters;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ViewfinderShader implements AutoCloseable {
   private final Minecraft minecraft = Minecrft.get();
   private final Camera camera;
   private final Viewfinder viewfinder;
   @Nullable
   private PostChain shader;
   private boolean active;

   public ViewfinderShader(Camera camera, Viewfinder viewfinder) {
      this.camera = camera;
      this.viewfinder = viewfinder;
      this.update();
   }

   public void apply(ResourceLocation shaderLocation) {
      if (this.shader != null) {
         if (this.shader.getName().equals(shaderLocation.toString())) {
            return;
         }

         this.shader.close();
      }

      try {
         this.shader = new PostChain(
            this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), shaderLocation
         );
         this.shader.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
         this.active = true;
      } catch (IOException var3) {
         Exposure.LOGGER.warn("Failed to load shader: {}", shaderLocation, var3);
         this.active = false;
      } catch (JsonSyntaxException var4) {
         Exposure.LOGGER.warn("Failed to parse shader: {}", shaderLocation, var4);
         this.active = false;
      }
   }

   public void resize(int width, int height) {
      if (this.shader != null) {
         this.shader.resize(width, height);
      }
   }

   public void process() {
      if (this.shader != null && this.active) {
         RenderSystem.disableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.resetTextureMatrix();
         this.shader.process(this.minecraft.getDeltaFrameTime());
      }
   }

   public void update() {
      this.setActive(this.viewfinder.isLookingThrough());
      if (this.active) {
         ItemStack filterStack = Attachment.FILTER.get(this.camera.getItemStack()).getForReading();
         Filters.of(Minecrft.registryAccess(), filterStack).map(Filter::shader).ifPresentOrElse(this::apply, this::remove);
      }
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public void remove() {
      if (this.shader != null) {
         this.shader.close();
      }

      this.shader = null;
   }

   @Override
   public void close() {
      this.remove();
   }
}
