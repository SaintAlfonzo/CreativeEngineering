package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import net.minecraft.resources.ResourceLocation;

public record PalettedImage(int width, int height, byte[] pixels, ColorPalette palette) implements Image {
   public PalettedImage(int width, int height, byte[] pixels, ColorPalette palette) {
      Image.validate(width, height, pixels.length);
      this.width = width;
      this.height = height;
      this.pixels = pixels;
      this.palette = palette;
   }

   public PalettedImage(int width, int height, byte[] pixels, ResourceLocation paletteId) {
      this(width, height, pixels, (ColorPalette)ColorPalettes.get(Minecrft.registryAccess(), paletteId).value());
   }

   public static PalettedImage fromExposure(ExposureData exposure) {
      return new PalettedImage(exposure.getWidth(), exposure.getHeight(), exposure.getPixels(), exposure.getPaletteId());
   }

   @Override
   public int getPixelARGB(int x, int y) {
      int id = this.pixels[y * this.width + x] & 255;
      return this.palette.byId(id);
   }
}
