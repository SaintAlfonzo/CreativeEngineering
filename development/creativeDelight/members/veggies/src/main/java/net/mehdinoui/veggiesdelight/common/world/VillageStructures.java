package net.mehdinoui.veggiesdelight.common.world;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.server.ServerAboutToStartEvent;

// Adds the depot house to each vanilla village style's house pool, as upstream does. No pack tool adds a piece to a
// pool (PoolTuner only reweights what is there), so this stays in code; the pack's pool_config.json then weights the
// houses like every other modded house (feature_writeup/veggies_delight.md 4.12). Upstream's compat houses for mods
// that are not in the pack, and its config switches, are not ported.
public class VillageStructures {
    private static final int WEIGHT = 4;
    private static final String[][] HOUSES = {
            {"minecraft:village/plains/houses", "veggiesdelight:village/houses/plains_depot_house"},
            {"minecraft:village/snowy/houses", "veggiesdelight:village/houses/snowy_depot_house"},
            {"minecraft:village/taiga/houses", "veggiesdelight:village/houses/taiga_depot_house"},
            {"minecraft:village/savanna/houses", "veggiesdelight:village/houses/savanna_depot_house"},
            {"minecraft:village/desert/houses", "veggiesdelight:village/houses/desert_depot_house"},
    };

    public static void addNewVillageBuilding(ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePools = event.getServer().registryAccess().registry(Registry.TEMPLATE_POOL_REGISTRY).orElseThrow();
        Registry<StructureProcessorList> processorLists = event.getServer().registryAccess().registry(Registry.PROCESSOR_LIST_REGISTRY).orElseThrow();
        for (String[] house : HOUSES) {
            addBuildingToPool(templatePools, processorLists, new ResourceLocation(house[0]), house[1], WEIGHT);
        }
    }

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePools, Registry<StructureProcessorList> processorLists,
                                          ResourceLocation poolId, String pieceId, int weight) {
        StructureTemplatePool pool = templatePools.get(poolId);
        if (pool == null) {
            return;
        }
        Holder<StructureProcessorList> processors = processorLists.getHolderOrThrow(
                ResourceKey.create(Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("minecraft", "empty")));
        SinglePoolElement piece = SinglePoolElement.single(pieceId, processors).apply(StructureTemplatePool.Projection.RIGID);
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }
        List<Pair<StructurePoolElement, Integer>> entries = new ArrayList<>(pool.rawTemplates);
        entries.add(Pair.of(piece, weight));
        pool.rawTemplates = entries;
    }
}
