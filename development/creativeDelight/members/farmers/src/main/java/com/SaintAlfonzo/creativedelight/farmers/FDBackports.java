package com.SaintAlfonzo.creativedelight.farmers;

import com.SaintAlfonzo.creativedelight.farmers.client.FDBackportsClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;

/**
 * Farmer's Delight 1.3.4 features backported into the 1.2.4 repack (feature_writeup/farmers_delight.md 3,
 * creative_delight.md 21.2). Called once from FD's own constructor ({@code FarmersDelightInitMixin}), so the
 * active mod container is FD's: the registers, the config and the listeners all belong to {@code farmersdelight}.
 */
public final class FDBackports {

	private FDBackports() {}

	public static void init() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		FDBackportRegistry.register(modBus);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FDBackportConfig.SPEC, FDBackportConfig.FILE);
		modBus.addListener(FDBackportConfig::onConfigChange);
		modBus.addListener(FDBackportConfig::onConfigLoad);
		MinecraftForge.EVENT_BUS.register(FDBackportEvents.class);
		modBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent e) -> e.enqueueWork(FDBackportRegistry::allowExtraBoards));
		if (FMLEnvironment.dist == Dist.CLIENT) FDBackportsClient.init(modBus);
	}
}
