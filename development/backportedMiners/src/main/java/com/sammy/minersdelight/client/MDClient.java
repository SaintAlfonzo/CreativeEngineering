package com.sammy.minersdelight.client;

import com.sammy.minersdelight.registry.MDBlocks;
import com.sammy.minersdelight.registry.MDMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client setup: the Copper Pot screen and cutout render layers (upstream sets them in code, not in the models). */
public final class MDClient {
    private MDClient() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(MDClient::onClientSetup);
    }

    @SuppressWarnings("removal")
    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MDMenuTypes.COPPER_POT.get(), CopperPotScreen::new);
            ItemBlockRenderTypes.setRenderLayer(MDBlocks.COPPER_POT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(MDBlocks.STICKY_BASKET.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(MDBlocks.WILD_CAVE_CARROTS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(MDBlocks.GOSSYPIUM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(MDBlocks.POTTED_GOSSYPIUM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(MDBlocks.CAVE_CARROTS.get(), RenderType.cutout());
        });
    }
}
