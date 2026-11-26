// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 registries/ItemRegistry.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.registries;

import com.yuushya.modelling.item.AbstractYuushyaItem;
import com.yuushya.modelling.item.YuushyaDebugStickItem;
import com.yuushya.modelling.item.showblocktool.ColorPickerItem;
import com.yuushya.modelling.item.showblocktool.DestroyItem;
import com.yuushya.modelling.item.showblocktool.EngraveItem;
import com.yuushya.modelling.item.showblocktool.GetBlockStateItem;
import com.yuushya.modelling.item.showblocktool.GetLitItem;
import com.yuushya.modelling.item.showblocktool.GetShowBlockEntityItem;
import com.yuushya.modelling.item.showblocktool.GuiItem;
import com.yuushya.modelling.item.showblocktool.HistoryItem;
import com.yuushya.modelling.item.showblocktool.MicroPosTransItem;
import com.yuushya.modelling.item.showblocktool.MoveTransformDataItem;
import com.yuushya.modelling.item.showblocktool.PosTransItem;
import com.yuushya.modelling.item.showblocktool.RotTransItem;
import com.yuushya.modelling.item.showblocktool.ScaleTransItem;
import com.yuushya.modelling.item.showblocktool.SlotTransItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// 1.19.2: an item's creative tab comes from Item.Properties.tab (1.20 fills tabs in GroupRegistry only); the tabs
// keep upstream's order through GroupRegistry's fillItemList.
public class ItemRegistry {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "yuushya");
   public static final RegistryObject<Item> GET_BLOCKSTATE_ITEM = ITEMS.register(
      "get_blockstate_item", () -> new GetBlockStateItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 3)
   );
   public static final RegistryObject<Item> POS_TRANS_ITEM = ITEMS.register("pos_trans_item", () -> new PosTransItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 4));
   public static final RegistryObject<Item> MICRO_POS_TRANS_ITEM = ITEMS.register(
      "micro_pos_trans_item", () -> new MicroPosTransItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 4)
   );
   public static final RegistryObject<Item> ROT_TRANS_ITEM = ITEMS.register("rot_trans_item", () -> new RotTransItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 4));
   public static final RegistryObject<Item> SCALE_TRANS_ITEM = ITEMS.register("scale_trans_item", () -> new ScaleTransItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 4));
   public static final RegistryObject<Item> SLOT_TRANS_ITEM = ITEMS.register("slot_trans_item", () -> new SlotTransItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 4));
   public static final RegistryObject<Item> GET_SHOWBLOCK_ITEM = ITEMS.register(
      "get_showblock_item", () -> new GetShowBlockEntityItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 4)
   );
   public static final RegistryObject<Item> MOVE_TRANSFORMDATA_ITEM = ITEMS.register(
      "move_transformdata_item", () -> new MoveTransformDataItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 4)
   );
   public static final RegistryObject<Item> DEBUG_STICK_ITEM = ITEMS.register(
      "debug_stick_item", () -> new YuushyaDebugStickItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 4)
   );
   public static final RegistryObject<Item> GET_LIT_ITEM = ITEMS.register("get_lit_item", () -> new GetLitItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 2));
   public static final RegistryObject<Item> DESTROY_ITEM = ITEMS.register("destroy_item", () -> new DestroyItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1).durability(384), 2));
   public static final RegistryObject<Item> GUI_ITEM = ITEMS.register("gui_item", () -> new GuiItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 2));
   public static final RegistryObject<Item> ENGRAVE_ITEM = ITEMS.register("engrave_item", () -> new EngraveItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 3));
   public static final RegistryObject<Item> COLOR_PICKER_ITEM = ITEMS.register("color_picker_item", () -> new ColorPickerItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 2));
   public static final RegistryObject<Item> HISTORY_ITEM = ITEMS.register("history_item", () -> new HistoryItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(1), 2));
   public static final RegistryObject<Item> THE_ENCYCLOPEDIA = ITEMS.register(
      "the_encyclopedia", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(16).rarity(Rarity.RARE), 1)
   );
   public static final RegistryObject<Item> SHIMMERING_PEARL = ITEMS.register(
      "shimmering_pearl", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(16).rarity(Rarity.RARE), 1)
   );
   public static final RegistryObject<Item> EVERLASTING_WOOD = ITEMS.register(
      "everlasting_wood", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(16).rarity(Rarity.RARE), 1)
   );
   public static final RegistryObject<Item> SPROUTING_DIRT = ITEMS.register(
      "sprouting_dirt", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(16).rarity(Rarity.RARE), 1)
   );
   public static final RegistryObject<Item> FLOATING_BLOOM = ITEMS.register(
      "floating_bloom", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(16).rarity(Rarity.RARE), 1)
   );
   public static final RegistryObject<Item> SPARKING_FLAME = ITEMS.register(
      "sparking_flame", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_MODELLING).stacksTo(16).rarity(Rarity.RARE), 1)
   );
   public static final RegistryObject<Item> CHIBI_0 = ITEMS.register(
      "chibi_0", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CHIBI_0_ALT = ITEMS.register(
      "chibi_0_alt", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CHIBI_1 = ITEMS.register(
      "chibi_1", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CHIBI_2 = ITEMS.register(
      "chibi_2", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CHIBI_3 = ITEMS.register(
      "chibi_3", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CHIBI_4 = ITEMS.register(
      "chibi_4", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CHIBI_5 = ITEMS.register(
      "chibi_5", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CHIBI_6 = ITEMS.register(
      "chibi_6", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> ANATOMY_0 = ITEMS.register(
      "anatomy_0", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> ANATOMY_0_ALT = ITEMS.register(
      "anatomy_0_alt", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> ANATOMY_1 = ITEMS.register(
      "anatomy_1", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> ANATOMY_2 = ITEMS.register(
      "anatomy_2", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_0 = ITEMS.register(
      "cube_0", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_1 = ITEMS.register(
      "cube_1", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_2 = ITEMS.register(
      "cube_2", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_3 = ITEMS.register(
      "cube_3", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_4 = ITEMS.register(
      "cube_4", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_5 = ITEMS.register(
      "cube_5", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_6 = ITEMS.register(
      "cube_6", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_7 = ITEMS.register(
      "cube_7", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_8 = ITEMS.register(
      "cube_8", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_9 = ITEMS.register(
      "cube_9", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_10 = ITEMS.register(
      "cube_10", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CUBE_11 = ITEMS.register(
      "cube_11", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> TRIANGLE_0 = ITEMS.register(
      "triangle_0", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> TRIANGLE_1 = ITEMS.register(
      "triangle_1", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CYLINDER_0 = ITEMS.register(
      "cylinder_0", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CYLINDER_1 = ITEMS.register(
      "cylinder_1", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CIRCLE_0 = ITEMS.register(
      "circle_0", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CIRCLE_1 = ITEMS.register(
      "circle_1", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CIRCLE_HOLLOW_0 = ITEMS.register(
      "circle_hollow_0", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> CIRCLE_HOLLOW_1 = ITEMS.register(
      "circle_hollow_1", () -> new AbstractYuushyaItem(new Properties().tab(GroupRegistry.YUUSHYA_PRIMITIVE).stacksTo(16).rarity(Rarity.RARE), 0)
   );
   public static final RegistryObject<Item> SHOW_BLOCK = ITEMS.register(
      "showblock", () -> new BlockItem((Block)BlockRegistry.SHOW_BLOCK.get(), new Properties().tab(GroupRegistry.YUUSHYA_MODELLING))
   );
   public static final RegistryObject<Item> ITEM_BLOCK = ITEMS.register(
      "itemblock", () -> new BlockItem((Block)BlockRegistry.ITEM_BLOCK.get(), new Properties().tab(GroupRegistry.YUUSHYA_MODELLING))
   );
   // PHASE-B: TEXT_BLOCK ("textblock", TextBlockItem::new) is registered with the Text Block (plan section 4, B0).

   public static void register(IEventBus bus) {
      ITEMS.register(bus);
   }
}
