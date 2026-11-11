package com.SaintAlfonzo.creativeworldgen.mixin.visceralheap;

import biomesoplenty.client.handler.ParticleFactoryHandler;
import biomesoplenty.client.particle.DripParticleBOP;
import biomesoplenty.client.particle.PusParticle;
import biomesoplenty.init.ModParticles;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/** Registers the four particles the Heap keeps. The original also registers glowworm, which is filtered out and would throw. */
@Mixin(value = ParticleFactoryHandler.class, remap = false)
public abstract class HeapParticleFactoryHandlerMixin {

    /**
     * @author SaintAlfonzo
     * @reason The original reads the removed glowworm particle; the four kept registrations are its other lines unchanged.
     * @SubscribeEvent is repeated here because @Overwrite replaces the method without keeping its annotations: without
     * it Forge never subscribes the method, no provider is registered and the client crashes on its first resource reload.
     */
    @SubscribeEvent
    @Overwrite
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.register(ModParticles.DRIPPING_BLOOD.get(), DripParticleBOP.BloodHangProvider::new);
        event.register(ModParticles.FALLING_BLOOD.get(), DripParticleBOP.BloodFallProvider::new);
        event.register(ModParticles.LANDING_BLOOD.get(), DripParticleBOP.BloodLandProvider::new);
        event.register(ModParticles.PUS.get(), PusParticle.Provider::new);
    }
}
