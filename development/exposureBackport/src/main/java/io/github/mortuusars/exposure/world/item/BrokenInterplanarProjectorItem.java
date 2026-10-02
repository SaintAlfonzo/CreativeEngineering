package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Exposure;
import java.util.List;
import net.minecraft.ChatFormatting;
import io.github.mortuusars.exposure.backport.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BrokenInterplanarProjectorItem extends Item {
   public BrokenInterplanarProjectorItem(Properties properties) {
      super(properties);
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      if (DataComponents.CUSTOM_NAME.has(stack)) {
         tooltipComponents.add(Component.translatable("item.exposure.broken_interplanar_projector.tooltip.broken").withStyle(ChatFormatting.DARK_RED));
      }
   }

   public String getErrorCode(ItemStack stack) {
      return (String)Exposure.DataComponents.INTERPLANAR_PROJECTOR_ERROR_CODE.getOrDefault(stack, "ERR_FAILURE_SEE_LOGS");
   }
}
