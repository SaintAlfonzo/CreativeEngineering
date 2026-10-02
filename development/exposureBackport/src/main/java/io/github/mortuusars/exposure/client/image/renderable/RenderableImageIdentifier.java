package io.github.mortuusars.exposure.client.image.renderable;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;

public record RenderableImageIdentifier(String base, String variant) {
   public RenderableImageIdentifier(String base) {
      this(base, "");
   }

   public RenderableImageIdentifier appendVariant(String appendedVariant) {
      if (org.apache.commons.lang3.StringUtils.isBlank(appendedVariant)) {
         return this;
      } else {
         return org.apache.commons.lang3.StringUtils.isBlank(this.variant)
            ? new RenderableImageIdentifier(this.base, appendedVariant)
            : new RenderableImageIdentifier(this.base, this.variant + "_" + appendedVariant);
      }
   }

   public ResourceLocation toResourceLocation() {
      String path = org.apache.commons.lang3.StringUtils.isBlank(this.variant) ? this.base : this.base + "/" + this.variant;
      String validPath = Util.sanitizeName(path, ResourceLocation::validPathChar);
      return Exposure.resource(validPath);
   }
}
