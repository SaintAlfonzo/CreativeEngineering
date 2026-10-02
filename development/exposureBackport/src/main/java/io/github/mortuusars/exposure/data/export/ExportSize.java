package io.github.mortuusars.exposure.data.export;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ExportSize implements StringRepresentable {
   X1(1),
   X2(2),
   X3(3),
   X4(4),
   X5(5),
   X6(6);

   private final int multiplier;

   private ExportSize(int multiplier) {
      this.multiplier = multiplier;
   }

   public int getMultiplier() {
      return this.multiplier;
   }

   @NotNull
   public String getSerializedName() {
      return this.name().toLowerCase();
   }

   @Nullable
   public static ExportSize byName(String name) {
      for (ExportSize value : values()) {
         if (value.getSerializedName().equals(name)) {
            return value;
         }
      }

      return null;
   }
}
