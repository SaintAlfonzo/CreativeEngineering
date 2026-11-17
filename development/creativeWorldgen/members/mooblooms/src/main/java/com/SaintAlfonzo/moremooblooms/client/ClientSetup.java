package com.SaintAlfonzo.moremooblooms.client;

import com.SaintAlfonzo.moremooblooms.entity.MMEntities;
import com.SaintAlfonzo.moremooblooms.item.MoobloomSpawnEggItem;
import com.SaintAlfonzo.moremooblooms.variant.MoobloomVariant;
import com.SaintAlfonzo.moremooblooms.variant.MoobloomBreeding;
import com.SaintAlfonzo.moremooblooms.variant.MoobloomVariants;
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
        // Forge registers every ForgeSpawnEggItem's fixed colors at HIGHEST; this one, later, takes over with
        // the colors of the stack's variant.
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
