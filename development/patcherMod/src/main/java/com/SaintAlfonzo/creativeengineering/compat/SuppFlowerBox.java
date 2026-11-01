package com.SaintAlfonzo.creativeengineering.compat;

import javax.annotation.Nullable;

import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Supplementaries calls, only loaded when Supplementaries is present ({@link PlantBoxModels}). */
final class SuppFlowerBox {

    private SuppFlowerBox() {}

    static void registerExtras() {
        FlowerPotHandler.registerCustomFlower(Items.GLOW_BERRIES, PlantBoxModels.GLOW_BERRIES);
        FlowerPotHandler.registerCustomFlower(Items.GLOW_LICHEN, PlantBoxModels.GLOW_LICHEN);
    }

    @Nullable
    static ResourceLocation specialModel(Item item) {
        return FlowerPotHandler.getSpecialFlowerModel(item);
    }
}
