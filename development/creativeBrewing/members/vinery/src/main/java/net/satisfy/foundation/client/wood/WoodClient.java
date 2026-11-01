package net.satisfy.foundation.client.wood;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.satisfy.foundation.wood.BoatWood;

public final class WoodClient {
    private WoodClient() {
    }

    public static void registerBoatLayers(EntityRenderersEvent.RegisterLayerDefinitions event, BoatWood... woods) {
        for (BoatWood wood : woods) {
            event.registerLayerDefinition(WoodBoatRenderer.layer(wood, false), () -> BoatModel.createBodyModel(false));
            event.registerLayerDefinition(WoodBoatRenderer.layer(wood, true), () -> BoatModel.createBodyModel(true));
        }
    }

    /** Forge's sign material: {@code <namespace>:entity/signs/<path>} of the wood type name. Call during client setup. */
    public static void registerSignMaterials(WoodType... woodTypes) {
        for (WoodType woodType : woodTypes) {
            Sheets.addWoodType(woodType);
        }
    }
}
