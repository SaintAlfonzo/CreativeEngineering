package com.SaintAlfonzo.creativedelight.farm.shippingbin.client;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.farm.shippingbin.FallingShippingCrateEntity;
import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBin;
import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingCrateEntity;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client registrations of the Shipping Bin port: screen, block entity renderer, the carriers' renderers and layers. */
@Mod.EventBusSubscriber(modid = CreativeDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ShippingBinClient {
	private ShippingBinClient() {
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> MenuScreens.register(ShippingBin.SHIPPING_BIN_MENU.get(), ShippingBinScreen::new));
	}

	@SubscribeEvent
	public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ShippingModels.BALLOON, ShippingModels::balloonLayer);
		event.registerLayerDefinition(ShippingModels.CRATE, ShippingModels::crateLayer);
	}

	@SubscribeEvent
	public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ShippingBin.SHIPPING_BIN_BLOCK_ENTITY.get(), ShippingBinRenderer::new);
		event.registerEntityRenderer(ShippingBin.SHIPPING_BALLOON.get(), ShippingBalloonRenderer::new);
		event.registerEntityRenderer(ShippingBin.SHIPPING_CRATE.get(), ShippingCrateRenderer<ShippingCrateEntity>::new);
		event.registerEntityRenderer(ShippingBin.FALLING_SHIPPING_CRATE.get(), ShippingCrateRenderer<FallingShippingCrateEntity>::new);
		event.registerEntityRenderer(ShippingBin.COURIER.get(), CourierRenderer::new);
	}
}
