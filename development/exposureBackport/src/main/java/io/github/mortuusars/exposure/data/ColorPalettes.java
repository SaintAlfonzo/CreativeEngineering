package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ColorPalettes {
   public static final ResourceKey<ColorPalette> DEFAULT = createKey(Exposure.resource("map_colors_plus"));
   public static final ResourceKey<ColorPalette> MAP_COLORS = createKey(Exposure.resource("map_colors"));

   public static ResourceKey<ColorPalette> createKey(ResourceLocation location) {
      return ResourceKey.create(Exposure.Registries.COLOR_PALETTE, location);
   }

   public static Holder<ColorPalette> get(RegistryAccess registryAccess, ResourceKey<ColorPalette> key) {
      Registry<ColorPalette> registry = registryAccess.registryOrThrow(Exposure.Registries.COLOR_PALETTE);
      return registry.getHolder(key).map(h -> (Holder<ColorPalette>)h).or(() -> registry.getHolder(DEFAULT)).or(() -> registry.holders().findFirst()).orElseThrow();
   }

   public static Holder<ColorPalette> get(RegistryAccess registryAccess, ResourceLocation key) {
      return get(registryAccess, createKey(key));
   }

   public static Holder<ColorPalette> getDefault(RegistryAccess registryAccess) {
      return get(registryAccess, DEFAULT);
   }
}
