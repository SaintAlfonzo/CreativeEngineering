package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// Port: 1.19.2 creative tabs aren't registry entries. The tab lists BnCItems.CREATIVE_TAB_ITEMS in upstream's order;
// items still name the tab in their Properties, so the search tab and JEI see them.
public class BnCCreativeTabs {
   public static final CreativeModeTab TAB_BREWIN_AND_CHEWIN = new CreativeModeTab("brewinandchewin") {
      @Override
      public ItemStack makeIcon() {
         return new ItemStack(BnCItems.KEG);
      }

      @Override
      public void fillItemList(NonNullList<ItemStack> items) {
         for (Item item : BnCItems.CREATIVE_TAB_ITEMS) {
            item.fillItemCategory(this, items);
         }
      }
   };
}
