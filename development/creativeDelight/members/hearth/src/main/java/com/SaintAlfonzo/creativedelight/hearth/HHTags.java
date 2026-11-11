package com.SaintAlfonzo.creativedelight.hearth;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Tags the ported systems read; their contents come from kubejs/data/creativedelight/kubejs/hearthandharvest/tags.js. */
public final class HHTags {
    public static final TagKey<Item> RAW_MEAT = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("creativedelight", "food_type/raw/meat"));
    public static final TagKey<Item> CORN = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "crops/corn"));
    public static final TagKey<Item> FARMERS_HATS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(HearthAndHarvestMod.MODID, "farmers_hats"));
    public static final TagKey<Block> GROWS_ON_TILLED_END_STONE = block("grows_on_tilled_end_stone");
    public static final TagKey<EntityType<?>> CAN_BE_BUTCHERED = entity("can_be_butchered");
    public static final TagKey<EntityType<?>> DOES_NOT_POOP = entity("does_not_poop");
    public static final TagKey<EntityType<?>> PINNED_IMMUNE = entity("pinned_immune");
    public static final TagKey<Block> TAPPABLE = block("tappable");
    public static final TagKey<Block> MINEABLE_WITH_PITCHFORK = block("mineable_with_pitchfork");
    public static final TagKey<Block> PITCHFORK_GRASS = block("pitchfork_grass");
    public static final TagKey<Block> PITCHFORK_CROPS = block("pitchfork_crops");

    private static TagKey<EntityType<?>> entity(String id) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(HearthAndHarvestMod.MODID, id));
    }

    private static TagKey<Block> block(String id) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(HearthAndHarvestMod.MODID, id));
    }

    private HHTags() {
    }
}
