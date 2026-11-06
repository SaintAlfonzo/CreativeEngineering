package com.SaintAlfonzo.creativedelight.barbeques.client;

import com.SaintAlfonzo.creativedelight.barbeques.BBQLang;
import com.SaintAlfonzo.creativedelight.barbeques.BarbequesDelight;
import com.SaintAlfonzo.creativedelight.barbeques.block.GrillBlockEntity;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Block entity renderers and the flip hint under the crosshair (1.3.0 BBQDClient, BBQOverlay). */
@Mod.EventBusSubscriber(modid = BarbequesDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BBQClient {

    private BBQClient() {
    }

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BBQBlocks.GRILL_BE.get(), GrillRenderer::new);
        event.registerBlockEntityRenderer(BBQBlocks.BASIN_BE.get(), StorageRenderer::new);
        event.registerBlockEntityRenderer(BBQBlocks.TRAY_BE.get(), StorageRenderer::new);
    }

    @SubscribeEvent
    public static void onOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "block_info", (gui, pose, partialTick, width, height) -> {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null || mc.options.hideGui || !(mc.hitResult instanceof BlockHitResult hit)) {
                return;
            }
            if (player.level.getBlockEntity(hit.getBlockPos()) instanceof GrillBlockEntity grill
                    && grill.canFlip(grill.getSlotForHitting(hit, player.level))) {
                Font font = mc.font;
                Component text = BBQLang.INFO_FLIP.get();
                font.drawShadow(pose, text, width / 2f - font.width(text) / 2f, height / 2f + 16, 0xFFFFFF);
            }
        });
    }
}
