package io.github.mortuusars.exposure.data;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.color.Color;
import java.util.Arrays;
import java.util.List;

public record ColorPalette(int[] colors) {
   public static final Codec<ColorPalette> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(Color.HEX_STRING_CODEC.listOf().fieldOf("colors").forGetter(ColorPalette::toColorList)).apply(instance, ColorPalette::new)
   );

   public ColorPalette(int[] colors) {
      Preconditions.checkState(colors.length > 0, "Cannot create palette that's empty.");
      Preconditions.checkState(colors.length <= 256, "Palette cannot have more than 256 colors.");
      Preconditions.checkState(colors.length < 256 || colors[255] == Color.TRANSPARENT.getARGB(), "Color at index 255 (last) should always be transparent.");
      if (colors.length < 256) {
         int[] newColors = new int[256];
         Arrays.fill(newColors, Color.BLACK.getARGB());
         System.arraycopy(colors, 0, newColors, 0, colors.length);
         colors = newColors;
         newColors[255] = Color.TRANSPARENT.getARGB();
      }

      this.colors = colors;
   }

   public ColorPalette(List<Color> colors) {
      this(colors.stream().mapToInt(Color::getARGB).toArray());
   }

   public List<Color> toColorList() {
      return Arrays.stream(this.colors).mapToObj(Color::argb).toList();
   }

   public int byId(int id) {
      return this.colors[id & 0xFF];
   }

   public int closestTo(Color color) {
      if (color.getA() == 0) {
         return 255;
      } else {
         int closest = 0;
         int closestDistance = Integer.MAX_VALUE;

         for (int i = 0; i < this.colors.length - 1; i++) {
            int distance = color.squaredDifferenceTo(this.colors[i]);
            if (distance < closestDistance) {
               closest = i;
               closestDistance = distance;
            }
         }

         return closest;
      }
   }
}
