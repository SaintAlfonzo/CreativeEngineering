package com.SaintAlfonzo.solraspberrypie.client;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/** Client wiring: key mappings, the message queue tick, the inventory widget's render and input events. */
public final class ClientSetup {
    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterKeyMappingsEvent e) -> {
            e.register(RpKeys.OPEN);
            e.register(RpKeys.LOCK);
        });
        MinecraftForge.EVENT_BUS.addListener(ClientSetup::tick);
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> ClientState.reset());
        MinecraftForge.EVENT_BUS.addListener((ScreenEvent.Render.Post e) -> {
            if (e.getScreen() instanceof InventoryScreen inv) InventoryWidget.render(inv, e.getPoseStack(), e.getMouseX(), e.getMouseY());
        });
        MinecraftForge.EVENT_BUS.addListener((ScreenEvent.MouseButtonPressed.Pre e) -> {
            if (e.getScreen() instanceof InventoryScreen inv && InventoryWidget.mousePressed(inv, e.getMouseX(), e.getMouseY(), e.getButton())) e.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener((ScreenEvent.MouseDragged.Pre e) -> {
            if (e.getScreen() instanceof InventoryScreen inv && InventoryWidget.mouseDragged(inv, e.getMouseX(), e.getMouseY())) e.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener((ScreenEvent.MouseButtonReleased.Pre e) -> {
            if (e.getScreen() instanceof InventoryScreen && InventoryWidget.mouseReleased()) e.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener((ScreenEvent.KeyPressed.Pre e) -> {
            // Not cancelled: TrashSlot reads the same key and both widgets toggle together (11.1).
            if (e.getScreen() instanceof InventoryScreen
                    && RpKeys.LOCK.isActiveAndMatches(com.mojang.blaze3d.platform.InputConstants.getKey(e.getKeyCode(), e.getScanCode()))) {
                InventoryWidget.toggleLock();
            }
        });
    }

    private static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (RpKeys.OPEN.consumeClick()) {
            if (mc.screen == null && mc.player != null) EffectScreen.open();
        }
        MessageQueue.tick();
    }

    private ClientSetup() {}
}
