package com.jkvin114.displaydelight.init;

import com.jkvin114.displaydelight.item.FoodBlockItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DisplayItems {
   public static List<RegistryObject<Item>> items = new ArrayList<>();
   public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, "displaydelight");
   public static final RegistryObject<Item> PLATE = blockitem(DisplayBlocks.PLATE);
   public static final RegistryObject<Item> SMALL_PLATE = blockitem(DisplayBlocks.SMALL_PLATE);

   // 1.19.2: an item names its creative tab
   private static Properties props() {
      return new Properties().tab(DisplayTabs.TAB);
   }

   public static RegistryObject<Item> blockitem(RegistryObject<Block> block) {
      return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), props()));
   }

   /** The item of a food display block (1.7.0's block / drinkblock). */
   public static RegistryObject<Item> food(RegistryObject<Block> block) {
      RegistryObject<Item> item = REGISTRY.register(block.getId().getPath(), () -> new FoodBlockItem(block.get(), props()));
      items.add(item);
      return item;
   }

   public static Iterable<Item> GetAll() {
      return items.stream().<Item>map(RegistryObject::get).toList();
   }

   /** Registers every food display block and its item; called once from the mod constructor. */
   public static void registerFoods() {
      DisplayFoods.register();
   }
}
