package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;

public interface Image extends AutoCloseable {
   Image EMPTY = new EmptyImage();
   Image MISSING = new MissingImage();

   int width();

   int height();

   int getPixelARGB(int var1, int var2);

   @Override
   default void close() {
   }

   default boolean isEmpty() {
      return this.equals(EMPTY) || this.width() <= 1 && this.height() <= 1 && this.getPixelARGB(0, 0) == 0;
   }

   static void validate(int width, int height, int pixelCount) {
      Preconditions.checkArgument(width > 0, "Width should be larger than 0. %s", width);
      Preconditions.checkArgument(height > 0, "Height should be larger than 0. %s ", height);
      Preconditions.checkArgument(
         pixelCount == width * height,
         "Pixel count '%s' is not correct for image dimensions of '%sx%s'. Count should be '%s'.",
         pixelCount,
         width,
         height,
         width * height
      );
   }

   public abstract static class Wrapped implements Image {
      private final Image image;

      public Wrapped(Image image) {
         this.image = image;
      }

      public Image getImage() {
         return this.image;
      }

      @Override
      public int width() {
         return this.image.width();
      }

      @Override
      public int height() {
         return this.image.height();
      }

      @Override
      public int getPixelARGB(int x, int y) {
         return this.image.getPixelARGB(x, y);
      }

      @Override
      public void close() {
         this.image.close();
      }

      @Override
      public boolean isEmpty() {
         return this.getImage().isEmpty();
      }
   }
}
