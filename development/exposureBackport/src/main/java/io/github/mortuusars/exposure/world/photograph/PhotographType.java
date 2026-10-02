package io.github.mortuusars.exposure.world.photograph;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.resources.ResourceLocation;

public record PhotographType(ResourceLocation id) {
   public static final PhotographType REGULAR = new PhotographType(Exposure.resource("regular"));
   public static final PhotographType AGED = new PhotographType(Exposure.resource("aged"));

   public String getFileSuffix() {
      return this == REGULAR ? "" : this.id.getPath();
   }
}
