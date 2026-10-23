package net.ribs.vintagedelight.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static final TagKey<Item> CONTAINER_ITEMS = ItemTags.create(new ResourceLocation("vintagedelight", "container_items"));
    // Where planted ghost peppers grow (was hard-coded nylium upstream). The jar ships soul soil; the pack adds the rest.
    public static final TagKey<Block> GHOST_PEPPER_SOIL = BlockTags.create(new ResourceLocation("vintagedelight", "ghost_pepper_soil"));
}
