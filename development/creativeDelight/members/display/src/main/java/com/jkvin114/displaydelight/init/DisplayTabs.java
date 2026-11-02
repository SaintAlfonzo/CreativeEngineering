package com.jkvin114.displaydelight.init;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** 1.19.2 form of 1.7.0's creative tab: a CreativeModeTab subclass, name from the lang key itemGroup.displaydelight. */
public class DisplayTabs {
   public static final CreativeModeTab TAB = new CreativeModeTab("displaydelight") {
      @Override
      public ItemStack makeIcon() {
         return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("displaydelight", "grilled_salmon")));
      }

      @Override
      public void fillItemList(NonNullList<ItemStack> items) {
         if (!DisplayConfig.DISABLE_CREATIVE_TAB_ITEMS.get()) {
            super.fillItemList(items);
         }
      }
   };
}
