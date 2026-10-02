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

public class ItemWith2ShiftTooltips extends Item {
   public String tooltip1;
   public String tooltip2;

   public ItemWith2ShiftTooltips(Properties properties, String tooltip1, String tooltip2) {
      super(properties);
      this.tooltip1 = tooltip1;
      this.tooltip2 = tooltip2;
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> components, TooltipFlag tooltipFlag) {
      if (Screen.hasShiftDown()) {
         components.add(Component.translatable(this.tooltip1).withStyle(Style.EMPTY.withColor(11184810)));
         components.add(Component.translatable(this.tooltip2).withStyle(Style.EMPTY.withColor(11184810)));
      } else {
         components.add(Component.translatable("tooltip.exposure.hold_for_details"));
      }
   }
}
