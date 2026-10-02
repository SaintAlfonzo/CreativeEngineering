package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.util.color.Color;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Matrix4f;

public class RenderedImageInstance implements AutoCloseable {
   private static final Function<ResourceLocation, RenderType> TEXT_MIPMAP = Util.memoize(
      texture -> RenderType.create(
            "exposure_mipmap",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            Mode.QUADS,
            786432,
            false,
            true,
            CompositeState.builder()
               .setShaderState(RenderType.RENDERTYPE_TEXT_SHADER)
               .setTextureState(new TextureStateShard(texture, false, true))
               .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
               .setLightmapState(RenderType.LIGHTMAP)
               .createCompositeState(false)
         )
   );
   protected final ResourceLocation textureLocation;
   protected RenderableImage image;
   protected DynamicTexture texture;
   protected final RenderType renderType;
   protected boolean requiresUpload = true;

   RenderedImageInstance(RenderableImage image) {
      this.image = image;
      this.texture = new DynamicTexture(image.width(), image.height(), true);
      this.textureLocation = image.getIdentifier().toResourceLocation();
      Minecraft.getInstance().getTextureManager().register(this.textureLocation, this.texture);
      int mipmapLevel = (Integer)Minecraft.getInstance().options.mipmapLevels().get();
      this.renderType = mipmapLevel > 0 ? TEXT_MIPMAP.apply(this.textureLocation) : RenderType.text(this.textureLocation);
      this.forceUpload();
   }

   public void replaceData(RenderableImage image) {
      boolean hasChanged = !image.getIdentifier().equals(this.image.getIdentifier());
      this.image = image;
      if (hasChanged) {
         this.texture = new DynamicTexture(image.width(), image.height(), true);
         this.forceUpload();
      }
   }

   public void forceUpload() {
      this.requiresUpload = true;
   }

   protected void updateTexture() {
      if (this.texture.getPixels() != null) {
         int width = this.image.width();
         int height = this.image.height();

         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               int ARGB = this.image.getPixelARGB(x, y);
               this.texture.getPixels().setPixelRGBA(x, y, Color.ABGRtoARGB(ARGB));
            }
         }

         int mipmapLevel = (Integer)Minecraft.getInstance().options.mipmapLevels().get();
         if (mipmapLevel > 0 && width > 2 && height > 2) {
            this.applyMipMap(mipmapLevel, width, height);
         }

         this.texture.upload();
      }
   }

   private void applyMipMap(int mipmapLevel, int width, int height) {
      if (this.texture.getPixels() != null) {
         try {
            this.texture.setFilter(false, true);
            TextureUtil.prepareImage(this.texture.getId(), mipmapLevel, width, height);
            // 1.19.3+ builds the mips with SpriteContents; 1.19.2 has the generator directly
            NativeImage[] mipLevels = MipmapGenerator.generateMipLevels(this.texture.getPixels(), mipmapLevel);
            for (int level = 0; level < mipLevels.length; level++) {
               mipLevels[level].upload(level, 0, 0, false);
               if (level > 0) {
                  mipLevels[level].close();
               }
            }
         } catch (Exception var5) {
            Exposure.LOGGER.error("Failed to generate mipmaps: {}", var5.getMessage());
         }
      }
   }

   public void draw(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      float minX,
      float minY,
      float maxX,
      float maxY,
      float minU,
      float minV,
      float maxU,
      float maxV,
      int packedLight,
      int r,
      int g,
      int b,
      int a
   ) {
      if (this.requiresUpload) {
         this.updateTexture();
         this.requiresUpload = false;
      }

      Matrix4f matrix4f = poseStack.last().pose();
      VertexConsumer vertexConsumer = bufferSource.getBuffer(this.renderType);
      vertexConsumer.vertex(matrix4f, minX, maxY, 0.0F).color(r, g, b, a).uv(minU, maxV).uv2(packedLight).endVertex();
      vertexConsumer.vertex(matrix4f, maxX, maxY, 0.0F).color(r, g, b, a).uv(maxU, maxV).uv2(packedLight).endVertex();
      vertexConsumer.vertex(matrix4f, maxX, minY, 0.0F).color(r, g, b, a).uv(maxU, minV).uv2(packedLight).endVertex();
      vertexConsumer.vertex(matrix4f, minX, minY, 0.0F).color(r, g, b, a).uv(minU, minV).uv2(packedLight).endVertex();
   }

   @Override
   public void close() {
      Minecraft.getInstance().getTextureManager().release(this.textureLocation);
      this.texture.close();
   }
}
