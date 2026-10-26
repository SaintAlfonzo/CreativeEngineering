package com.SaintAlfonzo.creativedelight.farmers.client;

import com.SaintAlfonzo.creativedelight.farmers.FDBackportRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client half: the Gleaming Salad's sparkle particle, registered the way FD 1.2.4 registers its own. */
public final class FDBackportsClient {

	private FDBackportsClient() {}

	public static void init(IEventBus modBus) {
		modBus.addListener(FDBackportsClient::registerParticles);
	}

	private static void registerParticles(RegisterParticleProvidersEvent event) {
		Minecraft.getInstance().particleEngine.register(FDBackportRegistry.SPARKLE.get(), SparkleParticle.Factory::new);
	}
}
