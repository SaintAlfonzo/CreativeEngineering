package net.satisfy.vinery.core.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.satisfy.vinery.core.Vinery;

/** Port: the juice tags keep 1.5.5's names; their contents are the 1.3.10 juice ids (B4.1). */
public class TagRegistry {
    public static final TagKey<Block> CAN_NOT_CONNECT = BlockTags.create(Vinery.identifier("can_not_connect"));
    public static final TagKey<Item> IGNORE_BLOCK_ITEM = ItemTags.create(Vinery.identifier("ignore_block_item"));
    public static final TagKey<Item> SMALL_BOTTLE = ItemTags.create(Vinery.identifier("small_bottle"));
    public static final TagKey<Item> LARGE_BOTTLE = ItemTags.create(Vinery.identifier("large_bottle"));
    public static final TagKey<Item> WHITE_GRAPEJUICE = ItemTags.create(Vinery.identifier("white_grapejuice"));
    public static final TagKey<Item> WHITE_SAVANNA_GRAPEJUICE = ItemTags.create(Vinery.identifier("white_savanna_grapejuice"));
    public static final TagKey<Item> WHITE_TAIGA_GRAPEJUICE = ItemTags.create(Vinery.identifier("white_taiga_grapejuice"));
    public static final TagKey<Item> WHITE_JUNGLE_GRAPEJUICE = ItemTags.create(Vinery.identifier("white_jungle_grapejuice"));
    public static final TagKey<Item> RED_GRAPEJUICE = ItemTags.create(Vinery.identifier("red_grapejuice"));
    public static final TagKey<Item> RED_SAVANNA_GRAPEJUICE = ItemTags.create(Vinery.identifier("red_savanna_grapejuice"));
    public static final TagKey<Item> RED_TAIGA_GRAPEJUICE = ItemTags.create(Vinery.identifier("red_taiga_grapejuice"));
    public static final TagKey<Item> RED_JUNGLE_GRAPEJUICE = ItemTags.create(Vinery.identifier("red_jungle_grapejuice"));
    public static final TagKey<Item> GRAPE_SEEDS = ItemTags.create(Vinery.identifier("grape_seeds"));
    /** Port: logs a fired stackable log takes as fuel (1.20's #minecraft:logs_that_burn doesn't exist in 1.19.2). */
    public static final TagKey<Item> LOGS_THAT_BURN = ItemTags.LOGS_THAT_BURN;
}
