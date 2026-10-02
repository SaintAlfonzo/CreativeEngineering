package io.github.mortuusars.exposure.client.image.renderable;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import java.util.function.Function;

public interface RenderableImage extends Image {
   RenderableImage EMPTY = new RenderableImage.Instance(Image.EMPTY, new RenderableImageIdentifier("-empty"));
   RenderableImage MISSING = new RenderableImage.Instance(Image.MISSING, new RenderableImageIdentifier("-missing"));

   Image getImage();

   RenderableImageIdentifier getIdentifier();

   @Override
   default boolean isEmpty() {
      return this.equals(EMPTY) || !this.getImage().equals(this) && this.getImage().isEmpty();
   }

   default RenderableImage modifyWith(Function<Image, Image> transformFunction, String variant) {
      Image image = transformFunction.apply(this.getImage());
      RenderableImageIdentifier identifier = this.getIdentifier().appendVariant(variant);
      return new RenderableImage.Instance(image, identifier);
   }

   default RenderableImage modifyWith(ImageEffect modifier) {
      return this.modifyWith(modifier::modify, modifier.getIdentifier());
   }

   static RenderableImage of(String id, Image image) {
      return new RenderableImage.Instance(image, new RenderableImageIdentifier(id));
   }

   public static class Instance extends Image.Wrapped implements RenderableImage {
      private final RenderableImageIdentifier identifier;

      public Instance(Image image, RenderableImageIdentifier identifier) {
         super(image);
         this.identifier = identifier;
      }

      @Override
      public RenderableImageIdentifier getIdentifier() {
         return this.identifier;
      }
   }
}
