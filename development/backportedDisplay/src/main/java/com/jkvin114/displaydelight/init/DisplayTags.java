package com.jkvin114.displaydelight.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class DisplayTags {
   public static final TagKey<Item> PLATE_DISPLAYABLE = ItemTags.create(new ResourceLocation("displaydelight", "plate_displayable"));
   public static final TagKey<Item> SMALL_PLATE_DISPLAYABLE = ItemTags.create(new ResourceLocation("displaydelight", "small_plate_displayable"));
   public static final TagKey<Item> DISPLAYABLE = ItemTags.create(new ResourceLocation("displaydelight", "displayable"));
   public static final TagKey<Block> SUPPORT_EXCEPTIONS = BlockTags.create(new ResourceLocation("displaydelight", "support_exceptions"));
}
