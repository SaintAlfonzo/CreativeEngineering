package io.github.mortuusars.exposure.data.export;

import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ExportLook implements StringRepresentable {
   REGULAR("regular", ImageEffect.EMPTY),
   AGED("aged", ImageEffect.AGED),
   NEGATIVE("negative", ImageEffect.NEGATIVE),
   NEGATIVE_FILM("negative_film", ImageEffect.NEGATIVE_FILM);

   private final String name;
   private final ImageEffect modifier;

   private ExportLook(String name, ImageEffect modifier) {
      this.name = name;
      this.modifier = modifier;
   }

   @Nullable
   public static ExportLook byName(String name) {
      for (ExportLook value : values()) {
         if (value.getSerializedName().equals(name)) {
            return value;
         }
      }

      return null;
   }

   @NotNull
   public String getSerializedName() {
      return this.name;
   }

   public ImageEffect getModifier() {
      return this.modifier;
   }

   public String getIdSuffix() {
      return this != REGULAR ? "_" + this.getSerializedName() : "";
   }
}
