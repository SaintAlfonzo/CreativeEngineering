package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

public class Lenses {
   public static Optional<FocalRange> getFocalRange(RegistryAccess registryAccess, ItemStack stack) {
      return !stack.is(Exposure.Tags.Items.LENSES)
         ? Optional.empty()
         : registryAccess.registryOrThrow(Exposure.Registries.LENS).stream().filter(lens -> lens.predicate().matches(stack)).map(Lens::focalRange).findFirst();
   }

   public static FocalRange getFocalRangeOrDefault(RegistryAccess registryAccess, ItemStack stack) {
      return getFocalRange(registryAccess, stack).orElse(FocalRange.getDefault());
   }
}
