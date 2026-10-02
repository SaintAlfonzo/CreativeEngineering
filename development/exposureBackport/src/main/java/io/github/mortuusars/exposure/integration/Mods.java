package io.github.mortuusars.exposure.integration;

import io.github.mortuusars.exposure.PlatformHelper;

public class Mods {
   public static final Mods.Mod CREATE = new Mods.Mod("create");
   public static final Mods.Mod REAL_CAMERA = new Mods.Mod("realcamera");
   public static final Mods.Mod SHOULDER_SURFING = new Mods.Mod("shouldersurfing");

   public static record Mod(String id) {
      public boolean isLoaded() {
         return PlatformHelper.isModLoaded(this.id);
      }

      public boolean isLoading() {
         return PlatformHelper.isModLoading(this.id);
      }
   }
}
