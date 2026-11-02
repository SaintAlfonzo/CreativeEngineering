package net.mehdinoui.fungidelight.common.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class FungiDelightTags {
   public static final TagKey<Block> TRUFFLE_ORE = fungiDelightBlockTag("truffle_ore");
   public static final TagKey<Block> PIG_CAN_DIG_UP = fungiDelightBlockTag("pig_can_dig_up");
   public static final TagKey<Block> MOREL_PLACEABLE_ON = fungiDelightBlockTag("morel_placeable_on");
   public static final TagKey<Block> INKY_CAP_PLACEABLE_ON = fungiDelightBlockTag("inky_cap_placeable_on");
   public static final TagKey<Block> STORAGE_BLOCKS_BROWN_MUSHROOM = forgeBlockTag("storage_blocks/brown_mushroom");
   public static final TagKey<Block> STORAGE_BLOCKS_RED_MUSHROOM = forgeBlockTag("storage_blocks/red_mushroom");
   public static final TagKey<Block> STORAGE_BLOCKS_INKY_CAP = forgeBlockTag("storage_blocks/inky_cap");
   public static final TagKey<Block> STORAGE_BLOCKS_MOREL = forgeBlockTag("storage_blocks/morel");
   public static final TagKey<Block> STORAGE_BLOCKS_TRUFFLE = forgeBlockTag("storage_blocks/truffle");
   public static final TagKey<Item> INKY_CAPS = forgeItemTag("mushrooms/inky_cap");
   public static final TagKey<Item> MORELS = forgeItemTag("mushrooms/morel");
   public static final TagKey<Item> TRUFFLES = forgeItemTag("mushrooms/truffle");
   public static final TagKey<Item> STORAGE_BLOCKS_ITEM_BROWN_MUSHROOM = forgeItemTag("storage_blocks/brown_mushroom");
   public static final TagKey<Item> STORAGE_BLOCKS_ITEM_RED_MUSHROOM = forgeItemTag("storage_blocks/red_mushroom");
   public static final TagKey<Item> STORAGE_BLOCKS_ITEM_INKY_CAP = forgeItemTag("storage_blocks/inky_cap");
   public static final TagKey<Item> STORAGE_BLOCKS_ITEM_MOREL = forgeItemTag("storage_blocks/morel");
   public static final TagKey<Item> STORAGE_BLOCKS_ITEM_TRUFFLE = forgeItemTag("storage_blocks/truffle");
   // Port change: Weak Stomach reacts to the pack's alcohol tag instead of #forge:alcohol (fungi_delight.md 5.1.4).
   public static final TagKey<Item> ALCOHOL = ItemTags.create(new ResourceLocation("creativeengineering", "food_type/drink/alcohol"));
   public static final TagKey<Item> ALCOHOL_REACTION = fungiDelightItemTag("alcohol_reaction");

   private static TagKey<Block> fungiDelightBlockTag(String path) {
      return BlockTags.create(new ResourceLocation("fungidelight", path));
   }

   private static TagKey<Item> fungiDelightItemTag(String path) {
      return ItemTags.create(new ResourceLocation("fungidelight", path));
   }

   private static TagKey<Block> forgeBlockTag(String path) {
      return BlockTags.create(new ResourceLocation("forge", path));
   }

   private static TagKey<Item> forgeItemTag(String path) {
      return ItemTags.create(new ResourceLocation("forge", path));
   }
}
