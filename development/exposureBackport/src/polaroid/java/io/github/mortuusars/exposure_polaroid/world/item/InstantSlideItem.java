package io.github.mortuusars.exposure_polaroid.world.item;

import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.item.SensitiveFilmItem;
import io.github.mortuusars.exposure_polaroid.Config;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class InstantSlideItem extends Item implements SensitiveFilmItem {
   protected final ExposureType type;

   public InstantSlideItem(ExposureType type, Properties properties) {
      super(properties);
      this.type = type;
   }

   public ExposureType getType() {
      return this.type;
   }

   public int getDefaultMaxFrameCount(ItemStack stack) {
      return 1;
   }

   public int getMaxFrameCount(ItemStack stack) {
      return 1;
   }

   public int getDefaultFrameSize(ItemStack stack) {
      return (Integer)Config.Server.INSTANT_CAMERA_FRAME_SIZE.get();
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> list, TooltipFlag tooltipFlag) {
      this.addFrameSizeToTooltip(stack, list);
      if (tooltipFlag.isAdvanced()) {
         this.addPaletteToTooltip(stack, list);
         this.addStyleToTooltip(stack, list);
      }
   }
}
