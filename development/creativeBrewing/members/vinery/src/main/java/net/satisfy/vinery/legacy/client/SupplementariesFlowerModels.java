package net.satisfy.vinery.legacy.client;

import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * Pack change: Supplementaries' own flower box / pot model for an item (its custom plant models, e.g. sugar cane,
 * cactus, bamboo). Only class that names Supplementaries; {@link PlantRenderer} calls it only when Supplementaries is loaded.
 */
@OnlyIn(Dist.CLIENT)
final class SupplementariesFlowerModels {
    private SupplementariesFlowerModels() {
    }

    static @Nullable ResourceLocation get(Item item) {
        return FlowerPotHandler.getSpecialFlowerModel(item);
    }
}
