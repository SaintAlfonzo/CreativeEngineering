package com.SaintAlfonzo.creativedelight.cooking;

import java.util.List;

import com.SaintAlfonzo.creativedelight.shared.StationContainers;

import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.tag.ModTags;

/**
 * The Farmer's Delight cooking pot's containers (remainder_buffer.md 3.8): every cooking recipe's container (or its
 * result's crafting remainder) and {@code #farmersdelight:serving_containers}. Loaded only from the pot mixins.
 */
public final class FdPotContainers {

    public static final StationContainers POT = new StationContainers("farmersdelight:cooking_pot",
            ModRecipeTypes.COOKING.get(),
            r -> r instanceof CookingPotRecipe c
                    ? List.of(c.getOutputContainer().isEmpty() ? c.getResultItem().getCraftingRemainingItem() : c.getOutputContainer())
                    : List.of(),
            List.of(ModTags.SERVING_CONTAINERS));

    private FdPotContainers() {}
}
