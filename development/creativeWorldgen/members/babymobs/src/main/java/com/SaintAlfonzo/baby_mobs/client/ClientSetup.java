package com.SaintAlfonzo.baby_mobs.client;

import com.SaintAlfonzo.baby_mobs.client.family.BabyFamilies;
import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Client-only registration (through DistExecutor): the baby layer definitions, the squid / dolphin replacement renderers,
 * and the 26.1 remodels of every other baby (client/family, applied by the LivingEntityRenderer mixin).
 */
public final class ClientSetup {

    public static void register(IEventBus modBus) {
        BabyFamilies.registerAll();
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions e) -> {
            BabyRender.definitions(e::registerLayerDefinition);
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
