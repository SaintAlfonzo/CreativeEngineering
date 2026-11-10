package com.SaintAlfonzo.creativedelight.hearth.machine.client;

import com.SaintAlfonzo.creativedelight.hearth.machine.HHMachines;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client registration of the machines, reached from HHClient. */
public final class HHMachineClient {
    private HHMachineClient() {
    }

    public static void init(IEventBus bus) {
        bus.addListener(HHMachineClient::renderers);
        bus.addListener(HHMachineClient::models);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(HHMachines.SPRINKLER_BE.get(), SprinklerRenderer::new);
        event.registerBlockEntityRenderer(HHMachines.TROUGH_BE.get(), TroughRenderer::new);
        event.registerBlockEntityRenderer(HHMachines.CRATE_BE.get(), CrateRenderer::new);
        event.registerBlockEntityRenderer(HHMachines.STOMPING_BASIN_BE.get(), StompingBasinRenderer::new);
    }

    private static void models(ModelEvent.RegisterAdditional event) {
        event.register(StompingBasinRenderer.MODEL_NW);
        event.register(StompingBasinRenderer.MODEL_NE);
        event.register(StompingBasinRenderer.MODEL_SW);
        event.register(StompingBasinRenderer.MODEL_SE);
        // Every model under models/display in any namespace: the 3D bottles and eggs the crate stands on its grid.
        Minecraft.getInstance().getResourceManager().listResources("models/display", id -> id.getPath().endsWith(".json")).keySet().forEach(file -> {
            String path = file.getPath().substring("models/".length(), file.getPath().length() - ".json".length());
            event.register(new ResourceLocation(file.getNamespace(), path));
        });
    }
}
