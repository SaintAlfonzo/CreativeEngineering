package com.SaintAlfonzo.creativedelight.compat.farmersrespite;

import java.util.List;

import com.SaintAlfonzo.creativedelight.shared.StationContainers;

import umpaz.farmersrespite.common.crafting.KettleRecipe;
import umpaz.farmersrespite.common.registry.FRRecipeTypes;

/**
 * The Farmer's Respite kettle's containers (remainder_buffer.md 3.8): every brewing recipe's container. Loaded only
 * from the kettle mixins.
 */
public final class KettleContainers {

    public static final StationContainers KETTLE = new StationContainers("farmersrespite:kettle",
            FRRecipeTypes.BREWING.get(),
            r -> r instanceof KettleRecipe k
                    ? List.of(k.getOutputContainer().isEmpty() ? k.getResultItem().getCraftingRemainingItem() : k.getOutputContainer())
                    : List.of(),
            List.of());

    private KettleContainers() {}
}
