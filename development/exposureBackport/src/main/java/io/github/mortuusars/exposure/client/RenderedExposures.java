package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.image.ResourceImage;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class RenderedExposures {
   private final Map<String, RenderedExposures.RenderedExposureInstance> cachedRenderedExposures = new HashMap<>();

   public RenderableImage getOrCreate(Frame frame) {
      RenderableImage image = this.getOrCreateRaw(frame.identifier());
      return Censor.isAllowedToRender(frame) ? image : image.modifyWith(ImageEffect.CENSORED);
   }

   public RenderableImage getOrCreateRaw(ExposureIdentifier identifier) {
      return identifier.map(this::getOrCreateRaw, ResourceImage::getOrCreate);
   }

   public RenderableImage getOrCreateRaw(String id) {
      RenderedExposures.RenderedExposureInstance existingImage = this.cachedRenderedExposures.get(id);
      if (existingImage != null) {
         existingImage.setLastAccess(UnixTimestamp.Milliseconds.now());
         return existingImage.getImage();
      } else {
         return ExposureClient.exposureStore().getOrRequest(id).map(exposure -> {
            RenderableImage image = RenderableImage.of(id, this.imageFromExposure(exposure));
            this.cachedRenderedExposures.put(id, new RenderedExposures.RenderedExposureInstance(image, UnixTimestamp.Milliseconds.now()));
            return image;
         }, RenderableImage.EMPTY);
      }
   }

   public void clearCacheOf(String id) {
      this.cachedRenderedExposures.remove(id);
   }

   public void clearCache() {
      this.cachedRenderedExposures.clear();
   }

   public void clearStale() {
      this.cachedRenderedExposures.entrySet().removeIf(entry -> {
         boolean stale = UnixTimestamp.Milliseconds.now() - entry.getValue().getLastAccess() > 120000L;
         if (stale) {
            ExposureClient.imageRenderer().clearCacheOf(entry.getKey());
         }

         return stale;
      });
   }

   @NotNull
   private Image imageFromExposure(ExposureData exposure) {
      ColorPalette palette = (ColorPalette)ColorPalettes.get(Minecrft.level().registryAccess(), exposure.getPaletteId()).value();
      return new PalettedImage(exposure.getWidth(), exposure.getHeight(), exposure.getPixels(), palette);
   }

   private static class RenderedExposureInstance {
      private final RenderableImage image;
      private long lastAccess;

      public RenderedExposureInstance(RenderableImage image, long lastAccess) {
         this.image = image;
         this.lastAccess = lastAccess;
      }

      public RenderableImage getImage() {
         return this.image;
      }

      public long getLastAccess() {
         return this.lastAccess;
      }

      public void setLastAccess(long lastAccess) {
         this.lastAccess = lastAccess;
      }
   }
}
