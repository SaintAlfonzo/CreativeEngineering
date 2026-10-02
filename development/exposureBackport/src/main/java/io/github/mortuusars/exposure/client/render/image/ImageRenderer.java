package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImageIdentifier;
import io.github.mortuusars.exposure.util.color.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.renderer.MultiBufferSource;

public class ImageRenderer implements AutoCloseable {
   private final Map<RenderableImageIdentifier, RenderedImageInstance> cache = new HashMap<>();

   public void render(RenderableImage image, PoseStack poseStack, MultiBufferSource bufferSource, RenderCoordinates coords, Color color) {
      this.render(image, poseStack, bufferSource, coords, 15728880, color);
   }

   public void render(RenderableImage image, PoseStack poseStack, MultiBufferSource bufferSource, RenderCoordinates coords, int packedLight, Color color) {
      this.render(
         image,
         poseStack,
         bufferSource,
         coords.minX(),
         coords.minY(),
         coords.maxX(),
         coords.maxY(),
         coords.minU(),
         coords.minV(),
         coords.maxU(),
         coords.maxV(),
         packedLight,
         color.getR(),
         color.getG(),
         color.getB(),
         color.getA()
      );
   }

   public void render(
      RenderableImage image, PoseStack poseStack, MultiBufferSource bufferSource, RenderCoordinates coords, int packedLight, int r, int g, int b, int a
   ) {
      this.render(
         image,
         poseStack,
         bufferSource,
         coords.minX(),
         coords.minY(),
         coords.maxX(),
         coords.maxY(),
         coords.minU(),
         coords.minV(),
         coords.maxU(),
         coords.maxV(),
         packedLight,
         r,
         g,
         b,
         a
      );
   }

   public void render(
      RenderableImage image,
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
      this.getOrCreateInstance(image).draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
   }

   private RenderedImageInstance getOrCreateInstance(RenderableImage image) {
      return this.cache.compute(image.getIdentifier(), (id, expData) -> {
         if (expData == null) {
            return new RenderedImageInstance(image);
         } else {
            expData.replaceData(image);
            return (RenderedImageInstance)expData;
         }
      });
   }

   public void clearCache() {
      this.cache.values().forEach(RenderedImageInstance::close);
      this.cache.clear();
   }

   public void clearCacheOf(String baseID) {
      this.cache.entrySet().removeIf(entry -> {
         boolean shouldRemove = entry.getKey().base().equals(baseID);
         if (shouldRemove) {
            entry.getValue().close();
         }

         return shouldRemove;
      });
   }

   public void clearCacheOf(Predicate<RenderableImageIdentifier> predicate) {
      this.cache.entrySet().removeIf(entry -> {
         boolean shouldRemove = predicate.test(entry.getKey());
         if (shouldRemove) {
            entry.getValue().close();
         }

         return shouldRemove;
      });
   }

   @Override
   public void close() {
      this.clearCache();
   }
}
