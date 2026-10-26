package com.jkvin114.displaydelight.init;

import com.jkvin114.displaydelight.DisplayDelight;
import com.jkvin114.displaydelight.item.FoodBlockItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockAssociations {
   public static Map<Item, Block> blockMap = new HashMap<>();
   public static Map<Block, Item> itemMap = new HashMap<>();
   public static Map<Item, Block> plateBlockMap = new HashMap<>();
   public static Map<Block, Item> plateItemMap = new HashMap<>();
   public static Map<Item, Block> smallplateBlockMap = new HashMap<>();
   public static Map<Block, Item> smallplateItemMap = new HashMap<>();
   private static final String FARMERSDELIGHT = "farmersdelight";
   private static final String MINECRAFT = "minecraft";
   private static final String[] TypePrefixes = new String[]{"plated_", "small_plated_", ""};
   private static final Set<String> VanilaFoods = Set.of("mushroom_stew", "rabbit_stew", "beetroot_soup", "pumpkin_pie", "cookie");
   private static final Map<String, String> COMPAT_NAMESPACES = new HashMap<String, String>() {
      {
         this.put("", "");
         this.put("cd_", "corn_delight");
         this.put("ed_", "expandeddelight");
         this.put("df_", "delightful");
         this.put("pd_", "pineapple_delight");
         this.put("od_", "oceansdelight");
         this.put("ad_", "alexsdelight");
         this.put("ctd_", "culturaldelights");
         this.put("lm_", "largemeals");
         this.put("fd_", "festive_delight");
         this.put("bnc_", "brewinandchewin");
         this.put("acd_", "aquaculturedelight");
         this.put("erd_", "endersdelight");
         this.put("edd_", "ends_delight");
         this.put("mnd_", "mynethersdelight");
         this.put("nd_", "nethersdelight");
         this.put("crd_", "crabbersdelight");
         this.put("abd_", "abnormals_delight");
         this.put("fr_", "farmersrespite");
         // The pack's Miner's Delight port has the mod id miners_delight
         this.put("md_", "miners_delight");
      }
   };
   private static final Map<String, String> FULL_MODNAMES = new HashMap<String, String>() {
      {
         this.put("farmersdelight", "Farmer's Delight");
         this.put("corn_delight", "Corn Delight");
         this.put("expandeddelight", "Expanded Delight");
         this.put("delightful", "Delightful");
         this.put("pineapple_delight", "Pineapple Delight");
         this.put("oceansdelight", "Ocean's Delight");
         this.put("alexsdelight", "Alex's Delight");
         this.put("culturaldelights", "Cultural Delights");
         this.put("largemeals", "Large Meals");
         this.put("festive_delight", "Festive Delight");
         this.put("brewinandchewin", "Brewin' and Chewin'");
         this.put("aquaculturedelight", "Aquaculture Delight");
         this.put("endersdelight", "Ender's Delight");
         this.put("ends_delight", "End's Delight");
         this.put("mynethersdelight", "My Nether's Delight");
         this.put("nethersdelight", "Nether's Delight");
         this.put("crabbersdelight", "Crabber's Delight");
         this.put("abnormals_delight", "Abnormal's Delight");
         this.put("farmersrespite", "Farmer's Respite");
         this.put("miners_delight", "Miner's Delight");
      }
   };
   public static Block getBlockFor(Item i) {
      return blockMap.getOrDefault(i, Blocks.AIR);
   }

   public static Item getItemFor(Block b) {
      return itemMap.getOrDefault(b, Items.AIR);
   }

   public static Block getPlateBlockFor(Item i) {
      return plateBlockMap.getOrDefault(i, Blocks.AIR);
   }

   public static Item getPlatedItemFor(Block b) {
      return plateItemMap.getOrDefault(b, Items.AIR);
   }

   public static Block getSmallPlateBlockFor(Item i) {
      return smallplateBlockMap.getOrDefault(i, Blocks.AIR);
   }

   public static Item getSmallPlatedItemFor(Block b) {
      return smallplateItemMap.getOrDefault(b, Items.AIR);
   }

   public static void addSmallPlateToMap(Item i, Block b) {
      smallplateBlockMap.putIfAbsent(i, b);
      smallplateItemMap.putIfAbsent(b, i);
   }

   public static void addToMap(Item i, Block b, boolean isPlated) {
      if (isPlated) {
         plateBlockMap.putIfAbsent(i, b);
         plateItemMap.putIfAbsent(b, i);
      } else {
         blockMap.putIfAbsent(i, b);
         itemMap.putIfAbsent(b, i);
      }
   }

   private static String removeFirstPrefix(String s, Iterable<String> prefixes) {
      for (String prefix : prefixes) {
         if (!prefix.isEmpty() && s.startsWith(prefix)) {
            return s.substring(prefix.length());
         }
      }

      return s;
   }

   private static String removePrefix(String s, String prefix) {
      return s.startsWith(prefix) ? s.substring(prefix.length()) : s;
   }

   public static boolean isItemInTag(Item item, TagKey<Item> tag) {
      ItemStack s = new ItemStack(item);
      return s.is(tag);
   }

   private static String getNamespace(String s) {
      for (String key : COMPAT_NAMESPACES.keySet()) {
         if (!key.isEmpty() && s.startsWith(key)) {
            return COMPAT_NAMESPACES.get(key);
         }
      }

      return "";
   }

   public static void initialize(Iterable<Item> allItems) {
      ArrayList<String> allPrefixes = new ArrayList<>();

      for (String cp : COMPAT_NAMESPACES.keySet()) {
         for (String tp : TypePrefixes) {
            allPrefixes.add(cp + tp);
         }
      }

      for (Item item : allItems) {
         if (item instanceof FoodBlockItem && !item.equals(DisplayItems.PLATE.get()) && !item.equals(DisplayItems.SMALL_PLATE.get())) {
            try {
               String itemId = ForgeRegistries.ITEMS.getKey(item).getPath();
               String foodName = removeFirstPrefix(itemId, allPrefixes);
               String foodNameWithPlate = removeFirstPrefix(itemId, COMPAT_NAMESPACES.keySet());
               String fullNamespace = getNamespace(itemId);
               boolean isVanila = false;
               if (fullNamespace.isEmpty()) {
                  if (VanilaFoods.contains(foodName)) {
                     fullNamespace = "minecraft";
                     isVanila = true;
                  } else {
                     fullNamespace = "farmersdelight";
                  }
               }

               if (!fullNamespace.equals("minecraft") && !ModList.get().isLoaded(fullNamespace)) {
                  String name = FULL_MODNAMES.getOrDefault(fullNamespace, "");
                  ((FoodBlockItem)item).setRequiredModName(name);
               }

               if (foodName.equals("festive_chicken") && fullNamespace.equals("festive_delight")) {
                  foodName = "festive_chiken";
               }

               Item registeredFoodItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(fullNamespace, foodName));
               if (registeredFoodItem != Items.AIR) {
                  if (foodNameWithPlate.startsWith("plated_")) {
                     if (isItemInTag(registeredFoodItem, DisplayTags.PLATE_DISPLAYABLE)) {
                        addToMap(registeredFoodItem, ((BlockItem)item).getBlock(), true);
                     }
                  } else if (foodNameWithPlate.startsWith("small_plated_")) {
                     if (isItemInTag(registeredFoodItem, DisplayTags.SMALL_PLATE_DISPLAYABLE)) {
                        addSmallPlateToMap(registeredFoodItem, ((BlockItem)item).getBlock());
                     }
                  } else if (isItemInTag(registeredFoodItem, DisplayTags.DISPLAYABLE) && (!(Boolean)DisplayConfig.DISABLE_VANILLA_FOODS.get() || !isVanila)) {
                     addToMap(registeredFoodItem, ((BlockItem)item).getBlock(), false);
                  }
               }
            } catch (Exception e) {
               DisplayDelight.LOGGER.error("Failed to set block association for {}", ForgeRegistries.ITEMS.getKey(item).getPath(), e);
            }
         }
      }
   }
}
