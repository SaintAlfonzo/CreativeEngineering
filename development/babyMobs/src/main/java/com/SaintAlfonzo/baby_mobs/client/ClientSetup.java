package com.SaintAlfonzo.baby_mobs.client;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client-only registration (through DistExecutor): the baby layer definitions and the three replacement renderers. */
public final class ClientSetup {

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions e) -> {
            e.registerLayerDefinition(BabyModels.SQUID, BabyModels::squid);
            e.registerLayerDefinition(BabyModels.GLOW_SQUID, BabyModels::squid);
            e.registerLayerDefinition(BabyModels.DOLPHIN, BabyModels::dolphin);
        });
        // Later registrations replace vanilla's renderers for these types (EntityRenderers map put).
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> {
            e.registerEntityRenderer(EntityType.SQUID, BabySquidRenderer::new);
            e.registerEntityRenderer(EntityType.GLOW_SQUID, BabyGlowSquidRenderer::new);
            e.registerEntityRenderer(EntityType.DOLPHIN, BabyDolphinRenderer::new);
        });
    }

    private ClientSetup() {}
}
