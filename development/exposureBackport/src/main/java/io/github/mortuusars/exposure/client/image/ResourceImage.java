package io.github.mortuusars.exposure.client.image;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImageIdentifier;
import io.github.mortuusars.exposure.util.color.Color;
import java.io.IOException;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourceImage extends SimpleTexture implements RenderableImage {
   @Nullable
   protected NativeImage image;

   public ResourceImage(ResourceLocation location) {
      super(location);
   }

   @NotNull
   public static RenderableImage getOrCreate(ResourceLocation location) {
      TextureManager textureManager = Minecraft.getInstance().getTextureManager();
      AbstractTexture existingTexture = (AbstractTexture)textureManager.byPath.get(location);
      if (existingTexture instanceof ResourceImage) {
         return (ResourceImage)existingTexture;
      } else {
         try {
            ResourceImage texture = new ResourceImage(location);
            textureManager.register(location, texture);
            return texture;
         } catch (Exception var4) {
            Exposure.LOGGER.error("Cannot load texture [{}]. {}", location, var4);
            return RenderableImage.MISSING;
         }
      }
   }

   @Override
   public int width() {
      NativeImage image = this.getNativeImage();
      return image != null ? image.getWidth() : 1;
   }

   @Override
   public int height() {
      NativeImage image = this.getNativeImage();
      return image != null ? image.getHeight() : 1;
   }

   @Override
   public int getPixelARGB(int x, int y) {
      NativeImage image = this.getNativeImage();
      return image != null ? Color.ABGRtoARGB(image.getPixelRGBA(x, y)) : 0;
   }

   @Nullable
   public NativeImage getNativeImage() {
      if (this.image != null) {
         return this.image;
      } else {
         try {
            NativeImage image = super.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
            this.image = image;
            return image;
         } catch (IOException var2) {
            Exposure.LOGGER.error("Cannot load texture: {}", var2.toString());
            return null;
         }
      }
   }

   public void reset(
      @NotNull TextureManager textureManager, @NotNull ResourceManager resourceManager, @NotNull ResourceLocation path, @NotNull Executor executor
   ) {
      super.reset(textureManager, resourceManager, path, executor);
      if (this.image != null) {
         this.image.close();
         this.image = null;
      }
   }

   @Override
   public void close() {
      super.close();
      if (this.image != null) {
         this.image.close();
         this.image = null;
      }
   }

   @Override
   public Image getImage() {
      return this;
   }

   @Override
   public RenderableImageIdentifier getIdentifier() {
      return new RenderableImageIdentifier(this.location.toString());
   }
}
