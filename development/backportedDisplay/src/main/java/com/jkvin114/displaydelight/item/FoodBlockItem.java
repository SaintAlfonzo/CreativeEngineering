package com.jkvin114.displaydelight.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class FoodBlockItem extends BlockItem {
   String requiredModName = "";

   public FoodBlockItem(Block pBlock, Properties pProperties) {
      super(pBlock, pProperties);
   }

   public void setRequiredModName(String modname) {
      this.requiredModName = modname;
   }

   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
      if (!this.requiredModName.isEmpty()) {
         pTooltipComponents.add(Component.translatable("item.displaydelight.tooltip.from_mod", new Object[]{this.requiredModName}).withStyle(ChatFormatting.BLUE));
      }

      super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
   }
}
