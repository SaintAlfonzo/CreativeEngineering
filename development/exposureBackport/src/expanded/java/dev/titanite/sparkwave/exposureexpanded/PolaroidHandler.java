package dev.titanite.sparkwave.exposureexpanded;

import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure_polaroid.world.item.InstantSlideItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class PolaroidHandler {
   public static Item create_item(ExposureType type, Properties properties) {
      return new InstantSlideItem(type, properties);
   }
}
