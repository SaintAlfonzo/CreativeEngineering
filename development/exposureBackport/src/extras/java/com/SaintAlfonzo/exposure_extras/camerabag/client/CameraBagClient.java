package com.SaintAlfonzo.exposure_extras.camerabag.client;

import com.SaintAlfonzo.exposure_extras.ExposureExtras;
import com.SaintAlfonzo.exposure_extras.camerabag.CameraBag;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup for the camera bags. */
@Mod.EventBusSubscriber(modid = ExposureExtras.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CameraBagClient {

    private CameraBagClient() {}

    /** Layer 0 (camera_bag_dyable) takes the dye colour; layer 1 is the untinted outline. */
    @SubscribeEvent
    public static void registerColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer == 0 && stack.getItem() instanceof DyeableLeatherItem dyeable
                        ? dyeable.getColor(stack) : -1,
                CameraBag.BY_TIER.values().stream().map(RegistryObject::get).toArray(Item[]::new));
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(CameraBag.MENU.get(), CameraBagScreen::new));
    }
}
