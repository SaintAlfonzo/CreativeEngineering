package io.github.mortuusars.exposure.world.lightroom;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum PrintingMode implements StringRepresentable {
   REGULAR("regular"),
   CHROMATIC("chromatic");

   private final String name;

   private PrintingMode(String name) {
      this.name = name;
   }

   public static PrintingMode fromStringOrDefault(String serializedName, PrintingMode defaultValue) {
      for (PrintingMode value : values()) {
         if (value.getSerializedName().equals(serializedName)) {
            return value;
         }
      }

      return defaultValue;
   }

   public PrintingMode cycle() {
      return this == REGULAR ? CHROMATIC : REGULAR;
   }

   @NotNull
   public String getSerializedName() {
      return this.name;
   }
}
