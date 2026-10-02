package io.github.mortuusars.exposure.client.image;

import net.minecraft.util.Mth;

public class ResizedImage extends Image.Wrapped {
   private final int width;
   private final int height;

   public ResizedImage(Image image, int width, int height) {
      super(image);
      this.width = width;
      this.height = height;
   }

   @Override
   public int width() {
      return this.width;
   }

   @Override
   public int height() {
      return this.height;
   }

   @Override
   public int getPixelARGB(int x, int y) {
      Image image = this.getImage();
      double xRatio = (double)image.width() / (double)this.width;
      double yRatio = (double)image.height() / (double)this.height;
      int originalX = Mth.clamp(Mth.floor((double)x * xRatio), 0, image.width() - 1);
      int originalY = Mth.clamp(Mth.floor((double)y * yRatio), 0, image.height() - 1);
      return image.getPixelARGB(originalX, originalY);
   }
}
