package net.satisfy.vinery.forge.client;

import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.state.properties.GrapeProperty;
import net.satisfy.vinery.core.registry.GrapeTypeRegistry;

/**
 * Pack (plan_bugfix_1114 3.1): the hops grape type has no variants in Vinery's blockstate files, which are All Rights
 * Reserved and read unmodified from the donor jar. After baking (ModelEvent.BakingCompleted, before the block model cache is built), every Vinery block state with a grape property set to hops
 * takes the baked model of the same state with jungle_white, so hops on a lattice show the white jungle grape art in place.
 * No file is copied or edited. Vinery's loader still logs the missing hops variants once while it reads the blockstates.
 */
final class VineryHopsModels {
    private VineryHopsModels() {
    }

    static void alias(Map<ResourceLocation, BakedModel> models) {
        int aliased = 0;
        for (Block block : ForgeRegistries.BLOCKS) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id == null || !Vinery.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            for (Property<?> property : block.getStateDefinition().getProperties()) {
                if (!(property instanceof GrapeProperty grape)) {
                    continue;
                }
                for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                    if (state.getValue(grape) != GrapeTypeRegistry.HOPS) {
                        continue;
                    }
                    BakedModel look = models.get(BlockModelShaper.stateToModelLocation(state.setValue(grape, GrapeTypeRegistry.JUNGLE_WHITE)));
                    if (look != null) {
                        models.put(BlockModelShaper.stateToModelLocation(state), look);
                        aliased++;
                    }
                }
            }
        }
        Vinery.LOGGER.info("[vinery] hops states drawn with the white jungle grape models: {}", aliased);
    }
}
