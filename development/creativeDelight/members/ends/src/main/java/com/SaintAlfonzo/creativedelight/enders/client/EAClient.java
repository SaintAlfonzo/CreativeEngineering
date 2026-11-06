package com.SaintAlfonzo.creativedelight.enders.client;

import com.SaintAlfonzo.creativedelight.enders.block.EABlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/** Client setup: cutout render layer for the plants (their models also name it; this covers the multipart vine). */
public final class EAClient {
    private EAClient() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(EAClient::onClientSetup);
    }

    @SuppressWarnings("removal")
    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (RegistryObject<Block> block : List.of(EABlocks.AMBERVEIL_MUSHROOM, EABlocks.ETHEREAL_SAFFRON_BUSH,
                    EABlocks.CHORUS_FLAME_BLOCK, EABlocks.VOIDPEPPER_BLOCK)) {
                ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutoutMipped());
            }
        });
    }
}
