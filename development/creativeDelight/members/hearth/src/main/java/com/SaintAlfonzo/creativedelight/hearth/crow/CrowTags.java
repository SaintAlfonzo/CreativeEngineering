package com.SaintAlfonzo.creativedelight.hearth.crow;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Tags the crow systems read; contents are in kubejs/data/creativedelight/kubejs/hearthandharvest/crow_salt_tags.js. */
public final class CrowTags {
    public static final TagKey<Item> CROW_FOOD = item("crow_food");
    public static final TagKey<Item> SHINY_ITEMS = item("crow_shiny_items");
    public static final TagKey<Item> CORN = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "crops/corn"));
    public static final TagKey<Item> CORN_SEEDS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "seeds/corn"));
    public static final TagKey<Block> EDIBLE_CROPS = block("crow_edible_crops");
    public static final TagKey<Block> REPELS_CROWS = block("repels_crows");
    public static final TagKey<Block> NESTS = block("nests");
    public static final TagKey<EntityType<?>> SCARY_FOR_CROW = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation("creativedelight", "scary_for_crow"));

    private static TagKey<Item> item(String id) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("creativedelight", id));
    }

    private static TagKey<Block> block(String id) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativedelight", id));
    }

    private CrowTags() {
    }
}
