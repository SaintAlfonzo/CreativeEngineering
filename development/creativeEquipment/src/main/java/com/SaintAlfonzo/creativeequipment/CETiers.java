package com.SaintAlfonzo.creativeequipment;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;

public class CETiers {
    public static final TagKey<Block> NEEDS_COPPER_TOOL =
        TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativeengineering", "needs_copper_tool"));

    public static final ForgeTier COPPER = new ForgeTier(
        1,
        180,
        6.0F,
        1.5F,
        8,
        NEEDS_COPPER_TOOL,
        () -> Ingredient.of(Items.COPPER_INGOT)
    );
}
