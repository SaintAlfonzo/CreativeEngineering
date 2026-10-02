package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.world.camera.ExposureType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DevelopedFilmItem extends Item implements FilmItem {
   private final ExposureType type;

   public DevelopedFilmItem(ExposureType type, Properties properties) {
      super(properties);
      this.type = type;
   }

   @Override
   public ExposureType getType() {
      return this.type;
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      int exposedFrames = this.getStoredFramesCount(stack);
      if (exposedFrames > 0) {
         tooltipComponents.add(
            Component.translatable("item.exposure.developed_film.tooltip.frame_count", new Object[]{exposedFrames}).withStyle(ChatFormatting.GRAY)
         );
      }

      int frameSize = this.getFrameSize(stack);
      if (frameSize != this.getDefaultFrameSize(stack)) {
         tooltipComponents.add(
            Component.translatable(
                  "item.exposure.film_roll.tooltip.frame_size", new Object[]{Component.literal(String.format("%.1f", (float)frameSize / 10.0F))}
               )
               .withStyle(ChatFormatting.GRAY)
         );
      }
   }
}
