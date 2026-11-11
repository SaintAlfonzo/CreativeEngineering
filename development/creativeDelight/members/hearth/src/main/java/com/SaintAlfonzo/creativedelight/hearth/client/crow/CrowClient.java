package com.SaintAlfonzo.creativedelight.hearth.client.crow;

import com.SaintAlfonzo.creativedelight.hearth.entity.HHEntities;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Crow renderer, model layer and shoulder layer. */
public final class CrowClient {
    private CrowClient() {
    }

    public static void init(IEventBus bus) {
        bus.addListener(CrowClient::renderers);
        bus.addListener(CrowClient::layerDefinitions);
        bus.addListener(CrowClient::addLayers);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HHEntities.CROW.get(), CrowRenderer::new);
    }

    private static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CrowModel.LAYER_LOCATION, CrowModel::createBodyLayer);
    }

    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            LivingEntityRenderer<?, ?> renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer player) {
                player.addLayer(new CrowOnShoulderLayer(player, event.getEntityModels()));
            }
        }
    }
}
