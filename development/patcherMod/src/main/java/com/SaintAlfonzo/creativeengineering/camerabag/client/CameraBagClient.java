package com.SaintAlfonzo.creativeengineering.camerabag.client;

import com.SaintAlfonzo.creativeengineering.camerabag.CameraBag;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup for the camera bags; called from {@code CreativeEngineering.ClientModEvents}. */
public final class CameraBagClient {

    private CameraBagClient() {}

    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(CameraBag.MENU.get(), CameraBagScreen::new));
    }
}
