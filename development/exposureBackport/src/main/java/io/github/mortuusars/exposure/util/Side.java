package io.github.mortuusars.exposure.util;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Side implements StringRepresentable {
   LEFT(0, "left"),
   RIGHT(1, "right");

   private final int index;
   private final String name;

   private Side(int index, String name) {
      this.index = index;
      this.name = name;
   }

   public int getIndex() {
      return this.index;
   }

   @NotNull
   public String getSerializedName() {
      return this.name;
   }
}
