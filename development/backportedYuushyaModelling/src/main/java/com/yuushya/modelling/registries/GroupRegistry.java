// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 registries/GroupRegistry (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2:
// 1.20's registered CreativeModeTab.builder() tabs -> 1.19.2 CreativeModeTab subclasses (created during mod
// construction; items join them through Item.Properties.tab in ItemRegistry). fillItemList keeps upstream's item order.
// The Text Block entry follows the Modelling tab's Item Block in phase B (PHASE-B).
package com.yuushya.modelling.registries;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public class GroupRegistry {
   public static final CreativeModeTab YUUSHYA_MODELLING = new CreativeModeTab("yuushya.modelling") {
      @Override
      public ItemStack makeIcon() {
         return new ItemStack(ItemRegistry.POS_TRANS_ITEM.get());
      }

      @Override
      public void fillItemList(NonNullList<ItemStack> items) {
         add(items, ItemRegistry.GET_BLOCKSTATE_ITEM);
         add(items, ItemRegistry.POS_TRANS_ITEM);
         add(items, ItemRegistry.MICRO_POS_TRANS_ITEM);
         add(items, ItemRegistry.ROT_TRANS_ITEM);
         add(items, ItemRegistry.SCALE_TRANS_ITEM);
         add(items, ItemRegistry.SLOT_TRANS_ITEM);
         add(items, ItemRegistry.GET_SHOWBLOCK_ITEM);
         add(items, ItemRegistry.MOVE_TRANSFORMDATA_ITEM);
         add(items, ItemRegistry.DEBUG_STICK_ITEM);
         add(items, ItemRegistry.GET_LIT_ITEM);
         add(items, ItemRegistry.DESTROY_ITEM);
         add(items, ItemRegistry.GUI_ITEM);
         add(items, ItemRegistry.ENGRAVE_ITEM);
         add(items, ItemRegistry.COLOR_PICKER_ITEM);
         add(items, ItemRegistry.HISTORY_ITEM);
         add(items, ItemRegistry.SHOW_BLOCK);
         add(items, ItemRegistry.ITEM_BLOCK);
         add(items, ItemRegistry.THE_ENCYCLOPEDIA);
         add(items, ItemRegistry.SHIMMERING_PEARL);
         add(items, ItemRegistry.EVERLASTING_WOOD);
         add(items, ItemRegistry.SPROUTING_DIRT);
         add(items, ItemRegistry.FLOATING_BLOOM);
         add(items, ItemRegistry.SPARKING_FLAME);
      }
   };
   public static final CreativeModeTab YUUSHYA_PRIMITIVE = new CreativeModeTab("yuushya.primitive") {
      @Override
      public ItemStack makeIcon() {
         return new ItemStack(ItemRegistry.CHIBI_0.get());
      }

      @Override
      public void fillItemList(NonNullList<ItemStack> items) {
         add(items, ItemRegistry.CHIBI_0);
         add(items, ItemRegistry.CHIBI_0_ALT);
         add(items, ItemRegistry.CHIBI_1);
         add(items, ItemRegistry.CHIBI_2);
         add(items, ItemRegistry.CHIBI_3);
         add(items, ItemRegistry.CHIBI_4);
         add(items, ItemRegistry.CHIBI_5);
         add(items, ItemRegistry.CHIBI_6);
         add(items, ItemRegistry.ANATOMY_0);
         add(items, ItemRegistry.ANATOMY_0_ALT);
         add(items, ItemRegistry.ANATOMY_1);
         add(items, ItemRegistry.ANATOMY_2);
         add(items, ItemRegistry.CUBE_0);
         add(items, ItemRegistry.CUBE_1);
         add(items, ItemRegistry.CUBE_2);
         add(items, ItemRegistry.CUBE_3);
         add(items, ItemRegistry.CUBE_4);
         add(items, ItemRegistry.CUBE_5);
         add(items, ItemRegistry.CUBE_6);
         add(items, ItemRegistry.CUBE_7);
         add(items, ItemRegistry.CUBE_8);
         add(items, ItemRegistry.CUBE_9);
         add(items, ItemRegistry.CUBE_10);
         add(items, ItemRegistry.CUBE_11);
         add(items, ItemRegistry.TRIANGLE_0);
         add(items, ItemRegistry.TRIANGLE_1);
         add(items, ItemRegistry.CYLINDER_0);
         add(items, ItemRegistry.CYLINDER_1);
         add(items, ItemRegistry.CIRCLE_0);
         add(items, ItemRegistry.CIRCLE_1);
         add(items, ItemRegistry.CIRCLE_HOLLOW_0);
         add(items, ItemRegistry.CIRCLE_HOLLOW_1);
      }
   };

   // Each item's own fillItemCategory (an item may add more than one stack), in upstream's order.
   private static void add(NonNullList<ItemStack> items, RegistryObject<Item> item) {
      item.get().fillItemCategory(CreativeModeTab.TAB_SEARCH, items);
   }

   // Loads the class so both tabs exist before the item registry fires (1.19.2 tabs are created eagerly).
   public static void register() {
   }
}
