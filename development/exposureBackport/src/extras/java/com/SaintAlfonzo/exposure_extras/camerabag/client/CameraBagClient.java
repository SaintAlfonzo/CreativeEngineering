package com.SaintAlfonzo.exposure_extras.camerabag.client;

import com.SaintAlfonzo.exposure_extras.ExposureExtras;
import com.SaintAlfonzo.exposure_extras.camerabag.CameraBag;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup for the camera bags. */
@Mod.EventBusSubscriber(modid = ExposureExtras.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CameraBagClient {

    private CameraBagClient() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(CameraBag.MENU.get(), CameraBagScreen::new));
    }
}
