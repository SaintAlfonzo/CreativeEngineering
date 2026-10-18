package umpaz.brewinandchewin.client.recipebook;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum FermentingBookCategory implements StringRepresentable {
   MEALS("meals", 0),
   DRINKS("drinks", 1),
   MISC("misc", 2);

   final String name;
   final int id;
   public static final Codec<FermentingBookCategory> CODEC = StringRepresentable.fromEnum(FermentingBookCategory::values);

   FermentingBookCategory(String name, int id) {
      this.name = name;
      this.id = id;
   }

   @Override
   public String getSerializedName() {
      return this.name;
   }

   public int id() {
      return this.id;
   }

   @Nullable
   public static FermentingBookCategory byName(String name) {
      for (FermentingBookCategory value : values()) {
         if (value.name.equals(name)) {
            return value;
         }
      }
      return null;
   }

   public static FermentingBookCategory byId(int id) {
      for (FermentingBookCategory value : values()) {
         if (value.id == id) {
            return value;
         }
      }
      return MEALS;
   }
}
