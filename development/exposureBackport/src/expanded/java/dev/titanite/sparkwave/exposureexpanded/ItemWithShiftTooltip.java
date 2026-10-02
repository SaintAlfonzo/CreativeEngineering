package dev.titanite.sparkwave.exposureexpanded;

import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemWithShiftTooltip extends Item {
   public String tooltip;

   public ItemWithShiftTooltip(Properties properties, String tooltip) {
      super(properties);
      this.tooltip = tooltip;
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> components, TooltipFlag tooltipFlag) {
      if (Screen.hasShiftDown()) {
         components.add(Component.translatable(this.tooltip).withStyle(Style.EMPTY.withColor(11184810)));
      } else {
         components.add(Component.translatable("tooltip.exposure.hold_for_details"));
      }
   }
}
