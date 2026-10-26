package com.SaintAlfonzo.creativedelight.rustic.client;

import com.SaintAlfonzo.creativedelight.rustic.registry.RDBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Client setup: cutout render layers for the plants (upstream's 1.20 models name the layer; 1.19.2 also takes it from code). */
public final class RDClient {
    private RDClient() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(RDClient::onClientSetup);
    }

    @SuppressWarnings("removal")
    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (RegistryObject<Block> block : List.of(RDBlocks.COTTON_CROP, RDBlocks.BELL_PEPPER_CROP, RDBlocks.PALE_BELL_PEPPER_CROP,
                    RDBlocks.DARK_BELL_PEPPER_CROP, RDBlocks.COFFEE_CROP, RDBlocks.WILD_COTTON, RDBlocks.WILD_BELL_PEPPERS,
                    RDBlocks.WILD_PALE_BELL_PEPPERS, RDBlocks.WILD_DARK_BELL_PEPPERS, RDBlocks.WILD_COFFEE, RDBlocks.POTTED_WILD_COTTON,
                    RDBlocks.POTTED_WILD_BELL_PEPPERS, RDBlocks.POTTED_WILD_PALE_BELL_PEPPERS, RDBlocks.POTTED_WILD_DARK_BELL_PEPPERS,
                    RDBlocks.POTTED_WILD_COFFEE)) {
                ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout());
            }
        });
    }
}
