package io.github.mortuusars.exposure.client.capture.palettizer;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.color.Color;

public class DitheredPalettizer implements Palettizer {
   @Override
   public PalettedImage palettize(Image image, ColorPalette palette) {
      return this.palettize(this.getPixels(image), palette);
   }

   public PalettedImage palettize(Color[][] pixels, ColorPalette palette) {
      int width = pixels[0].length;
      int height = pixels.length;
      byte[] indexedPixels = new byte[width * height];

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            Color oldColor = pixels[y][x];
            if (oldColor.getA() == 0) {
               indexedPixels[y * width + x] = -1;
            } else {
               int colorIndex = palette.closestTo(oldColor);
               indexedPixels[y * width + x] = (byte)colorIndex;
               Color newColor = Color.argb(palette.byId(colorIndex));
               Color.Unbounded error = oldColor.subtractUnbounded(newColor);
               if (x + 1 < width) {
                  pixels[y][x + 1] = this.applyError(pixels[y][x + 1], error, 0.4375);
               }

               if (x - 1 >= 0 && y + 1 < height) {
                  pixels[y + 1][x - 1] = this.applyError(pixels[y + 1][x - 1], error, 0.1875);
               }

               if (y + 1 < height) {
                  pixels[y + 1][x] = this.applyError(pixels[y + 1][x], error, 0.3125);
               }

               if (x + 1 < width && y + 1 < height) {
                  pixels[y + 1][x + 1] = this.applyError(pixels[y + 1][x + 1], error, 0.0625);
               }
            }
         }
      }

      return new PalettedImage(width, height, indexedPixels, palette);
   }

   private Color applyError(Color color, Color.Unbounded error, double scalar) {
      return color.addUnbounded(error.multiply(scalar)).clamp().withAlpha(color.getA());
   }

   private Color[][] getPixels(Image image) {
      int width = image.width();
      int height = image.height();
      Color[][] pixels = new Color[height][width];

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            pixels[y][x] = Color.argb(image.getPixelARGB(x, y));
         }
      }

      return pixels;
   }
}
