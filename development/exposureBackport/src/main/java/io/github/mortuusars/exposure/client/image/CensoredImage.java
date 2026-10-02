package io.github.mortuusars.exposure.client.image;

import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public class CensoredImage extends Image.Wrapped {
   private final int blockSize = this.getBlockSize();
   private final Integer[][] cache = new Integer[Mth.ceil((float)this.width() / (float)this.blockSize)][Mth.ceil((float)this.height() / (float)this.blockSize)];

   public CensoredImage(Image image) {
      super(image);
   }

   public int getBlockSize() {
      int largerSize = Math.max(this.width(), this.height());
      return Math.max(largerSize / 16, 1);
   }

   @Override
   public int getPixelARGB(int x, int y) {
      int blockXIndex = x / this.blockSize;
      int blockYIndex = y / this.blockSize;
      int blockX = blockXIndex * this.blockSize;
      int blockY = blockYIndex * this.blockSize;
      Integer value = this.cache[blockXIndex][blockYIndex];
      if (value == null) {
         value = this.calculateAverageBlockColor(blockX, blockY, this.blockSize);
         this.cache[blockXIndex][blockYIndex] = value;
      }

      return value;
   }

   private int calculateAverageBlockColor(int startX, int startY, int blockSize) {
      int endX = Math.min(startX + blockSize, this.width());
      int endY = Math.min(startY + blockSize, this.height());
      int totalR = 0;
      int totalG = 0;
      int totalB = 0;
      int totalA = 0;
      int pixelCount = 0;

      for (int x = startX; x < endX; x++) {
         for (int y = startY; y < endY; y++) {
            int pixel = this.getImage().getPixelARGB(x, y);
            totalA += pixel >> 24 & 0xFF;
            totalR += pixel >> 16 & 0xFF;
            totalG += pixel >> 8 & 0xFF;
            totalB += pixel & 0xFF;
            pixelCount++;
         }
      }

      if (pixelCount == 0) {
         return 0;
      } else {
         int avgA = totalA / pixelCount;
         int avgR = totalR / pixelCount;
         int avgG = totalG / pixelCount;
         int avgB = totalB / pixelCount;
         return ARGB32.color(avgA, avgR, avgG, avgB);
      }
   }
}
