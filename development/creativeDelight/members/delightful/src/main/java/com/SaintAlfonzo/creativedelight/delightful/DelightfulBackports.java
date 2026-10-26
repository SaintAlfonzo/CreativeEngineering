package com.SaintAlfonzo.creativedelight.delightful;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Delightful 3.8.1 items backported into the 3.4.1 repack (feature_writeup/delightful.md 3). Called once from
 * Delightful's own constructor ({@code DelightfulInitMixin}), so the active mod container is Delightful's and the
 * registers belong to {@code delightful}. Nothing here reads Delightful's config: every backported item is always on.
 */
public final class DelightfulBackports {

	private DelightfulBackports() {}

	public static void init() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		DelightfulBackportRegistry.register(modBus);
		modBus.addListener(DelightfulBackports::commonSetup);
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		if (ModList.get().isLoaded("neapolitan")) event.enqueueWork(DelightfulMilkshakes::registerCauldronInteractions);
	}
}
