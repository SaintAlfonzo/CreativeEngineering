package com.SaintAlfonzo.more_mooblooms.client;

import com.SaintAlfonzo.more_mooblooms.entity.MMEntities;
import com.SaintAlfonzo.more_mooblooms.item.MoobloomSpawnEggItem;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariant;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomBreeding;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import net.minecraft.client.model.CowModel;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client-only registration; called through DistExecutor so none of this loads on a dedicated server. */
public final class ClientSetup {

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions e) ->
                e.registerLayerDefinition(MoobloomRenderer.LAYER, CowModel::createBodyLayer));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) ->
                e.registerEntityRenderer(MMEntities.MOOBLOOM.get(), MoobloomRenderer::new));
        // Forge registers every ForgeSpawnEggItem's fixed colours at HIGHEST; this one, later, takes over with
        // the colours of the stack's variant.
        modBus.addListener((RegisterColorHandlersEvent.Item e) -> e.register((stack, layer) -> {
            MoobloomVariant v = MoobloomSpawnEggItem.clientVariant(stack);
            if (v == null) return MoobloomSpawnEggItem.MOOBLOOM_SPAWN_EGG.get().getColor(layer);
            return layer == 0 ? v.eggBase() : v.eggSpots();
        }, MoobloomSpawnEggItem.MOOBLOOM_SPAWN_EGG.get()));
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> {
            MoobloomVariants.clearClient();
            MoobloomBreeding.clearClient();
        });
    }

    private ClientSetup() {}
}
