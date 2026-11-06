package net.mehdinoui.fungidelight.common.registry;

import net.mehdinoui.fungidelight.common.item.ModFoods;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.item.MushroomColonyItem;

public class ModItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "fungidelight");
   public static final RegistryObject<Item> INKY_CAP_COW_SPAWN_EGG = ITEMS.register(
      "inky_cap_cow_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.INKY_CAP_COW, 3158072, 11382453, new Properties().tab(ModCreativeTab.TAB))
   );
   public static final RegistryObject<Item> MOREL_COW_SPAWN_EGG = ITEMS.register(
      "morel_cow_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.MOREL_COW, 5522472, 3154192, new Properties().tab(ModCreativeTab.TAB))
   );
   public static final RegistryObject<Item> CLEANED_CAPS = ITEMS.register("cleaned_cap", () -> new Item(basicItem().food(ModFoods.CLEANED_CAPS)));
   public static final RegistryObject<Item> COOKED_CLEANED_CAPS = ITEMS.register(
      "cooked_cleaned_cap", () -> new Item(basicItem().food(ModFoods.COOKED_CLEANED_CAPS))
   );
   public static final RegistryObject<Item> TRUFFLE = ITEMS.register("truffle", () -> new Item(basicItem().food(ModFoods.TRUFFLE)));
   public static final RegistryObject<Item> TRUFFLE_SLICE = ITEMS.register("truffle_slice", () -> new Item(basicItem().food(ModFoods.TRUFFLE_SLICE)));
   public static final RegistryObject<Item> INKY_CAPS_SALAD = ITEMS.register(
      "inky_caps_salad", () -> new ConsumableItem(bowlFoodItem(ModFoods.INKY_CAPS_SALAD))
   );
   public static final RegistryObject<Item> CREAMY_MOREL_SOUP = ITEMS.register(
      "creamy_morel_soup", () -> new ConsumableItem(bowlFoodItem(ModFoods.CREAMY_MOREL_SOUP))
   );
   public static final RegistryObject<Item> TRUFFLE_ICE_CREAM = ITEMS.register(
      "truffle_ice_cream", () -> new ConsumableItem(bowlFoodItem(ModFoods.TRUFFLE_ICE_CREAM))
   );
   public static final RegistryObject<Item> INKY_CAP_SCRAMBLED_EGGS = ITEMS.register(
      "inky_cap_scrambled_eggs", () -> new ConsumableItem(bowlFoodItem(ModFoods.INKY_CAP_SCRAMBLED_EGGS))
   );
   public static final RegistryObject<Item> TRUFFLE_PASTA = ITEMS.register("truffle_pasta", () -> new ConsumableItem(bowlFoodItem(ModFoods.TRUFFLE_PASTA)));
   public static final RegistryObject<Item> STUFFED_MORELS = ITEMS.register("stuffed_morels", () -> new ConsumableItem(bowlFoodItem(ModFoods.STUFFED_MORELS)));
   public static final RegistryObject<Item> STEAK_WITH_MUSHROOMS = ITEMS.register(
      "steak_with_mushrooms", () -> new ConsumableItem(bowlFoodItem(ModFoods.STEAK_WITH_MUSHROOMS))
   );
   public static final RegistryObject<Item> MUTTON_CHOPS_WITH_TRUFFLE = ITEMS.register(
      "mutton_chops_with_truffle", () -> new ConsumableItem(bowlFoodItem(ModFoods.MUTTON_CHOPS_WITH_TRUFFLE))
   );
   public static final RegistryObject<Item> RABBIT_WITH_MORELS = ITEMS.register(
      "rabbit_with_morels", () -> new ConsumableItem(bowlFoodItem(ModFoods.RABBIT_WITH_MORELS))
   );
   public static final RegistryObject<Item> PORK_MARSALA_WITH_MUSHROOMS = ITEMS.register(
      "pork_marsala_with_mushrooms", () -> new ConsumableItem(bowlFoodItem(ModFoods.PORK_MARSALA_WITH_MUSHROOMS))
   );
   public static final RegistryObject<Item> BROWN_MUSHROOM_CRATE = ITEMS.register(
      "brown_mushroom_crate", () -> new BlockItem((Block)ModBlocks.BROWN_MUSHROOM_CRATE.get(), basicItem())
   );
   public static final RegistryObject<Item> INKY_CAP_MUSHROOM_CRATE = ITEMS.register(
      "inky_cap_mushroom_crate", () -> new BlockItem((Block)ModBlocks.INKY_CAP_MUSHROOM_CRATE.get(), basicItem())
   );
   public static final RegistryObject<Item> MOREL_MUSHROOM_CRATE = ITEMS.register(
      "morel_mushroom_crate", () -> new BlockItem((Block)ModBlocks.MOREL_MUSHROOM_CRATE.get(), basicItem())
   );
   public static final RegistryObject<Item> RED_MUSHROOM_CRATE = ITEMS.register(
      "red_mushroom_crate", () -> new BlockItem((Block)ModBlocks.RED_MUSHROOM_CRATE.get(), basicItem())
   );
   public static final RegistryObject<Item> TRUFFLE_CRATE = ITEMS.register(
      "truffle_crate", () -> new BlockItem((Block)ModBlocks.TRUFFLE_CRATE.get(), basicItem())
   );
   public static final RegistryObject<Item> INKY_CAP_MUSHROOM = ITEMS.register(
      "inky_cap_mushroom", () -> new BlockItem((Block)ModBlocks.INKY_CAP_MUSHROOM.get(), basicItem())
   );
   public static final RegistryObject<Item> MOREL_MUSHROOM = ITEMS.register(
      "morel_mushroom", () -> new BlockItem((Block)ModBlocks.MOREL_MUSHROOM.get(), basicItem())
   );
   public static final RegistryObject<Item> INKY_CAP_MUSHROOM_COLONY = ITEMS.register(
      "inky_cap_mushroom_colony", () -> new MushroomColonyItem((Block)ModBlocks.INKY_CAP_MUSHROOM_COLONY.get(), basicItem())
   );
   public static final RegistryObject<Item> MOREL_MUSHROOM_COLONY = ITEMS.register(
      "morel_mushroom_colony", () -> new MushroomColonyItem((Block)ModBlocks.MOREL_MUSHROOM_COLONY.get(), basicItem())
   );
   public static final RegistryObject<Item> TRUFFLE_DIRT = ITEMS.register("truffle_dirt", () -> new BlockItem((Block)ModBlocks.TRUFFLE_DIRT.get(), basicItem()));
   public static final RegistryObject<Item> INKY_CAP_MUSHROOM_BLOCK = ITEMS.register(
      "inky_cap_mushroom_block", () -> new BlockItem((Block)ModBlocks.INKY_CAP_MUSHROOM_BLOCK.get(), basicItem())
   );
   public static final RegistryObject<Item> INKY_CAP_MUSHROOM_EDGE = ITEMS.register(
      "inky_cap_mushroom_edge", () -> new BlockItem((Block)ModBlocks.INKY_CAP_MUSHROOM_EDGE.get(), basicItem())
   );
   public static final RegistryObject<Item> INKY_CAP_MUSHROOM_STEM = ITEMS.register(
      "inky_cap_mushroom_stem", () -> new BlockItem((Block)ModBlocks.INKY_CAP_MUSHROOM_STEM.get(), basicItem())
   );
   public static final RegistryObject<Item> INKY_GOO_VEIL = ITEMS.register(
      "inky_goo_veil", () -> new BlockItem((Block)ModBlocks.INKY_GOO_VEIL.get(), basicItem())
   );
   public static final RegistryObject<Item> MOREL_MUSHROOM_BLOCK = ITEMS.register(
      "morel_mushroom_block", () -> new BlockItem((Block)ModBlocks.MOREL_MUSHROOM_BLOCK.get(), basicItem())
   );
   public static final RegistryObject<Item> MOREL_MUSHROOM_STEM = ITEMS.register(
      "morel_mushroom_stem", () -> new BlockItem((Block)ModBlocks.MOREL_MUSHROOM_STEM.get(), basicItem())
   );

   public static Properties basicItem() {
      return new Properties().tab(ModCreativeTab.TAB);
   }

   public static Properties bowlFoodItem(FoodProperties food) {
      return new Properties().tab(ModCreativeTab.TAB).food(food).craftRemainder(Items.BOWL).stacksTo(16);
   }

   public static void register(IEventBus eventBus) {
      ITEMS.register(eventBus);
   }
}
