package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.Exposure;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class Filters {
   public static Optional<Filter> of(RegistryAccess registryAccess, ItemStack stack) {
      return registryAccess.registryOrThrow(Exposure.Registries.FILTER).stream().filter(filter -> filter.predicate().matches(stack)).findFirst();
   }

   public static Optional<ResourceLocation> locationOf(RegistryAccess registryAccess, Filter filter) {
      return Optional.ofNullable(registryAccess.registryOrThrow(Exposure.Registries.FILTER).getKey(filter));
   }
}
