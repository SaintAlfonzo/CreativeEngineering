package net.satisfy.foundation;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.satisfy.foundation.network.FoundationNetwork;
import net.satisfy.foundation.registry.FoundationEntities;
import net.satisfy.foundation.registry.FoundationParticles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point of the Foundation lib.
 * Holds the mod id and kicks off all the common registries + packets.
 * <p>
 * Port: only the part of Let's Do Foundation 1.0.1 that Vinery 1.5.5 uses, as its own mod id inside the Vinery jar, so
 * every {@code foundation:} id (particles, the seat entity, the assets in the nested upstream jar) stays as upstream.
 */
@Mod(Foundation.MOD_ID)
public class Foundation {
    public static final String MOD_ID = "foundation";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Foundation() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        FoundationParticles.PARTICLE_TYPES.register(bus);
        FoundationEntities.ENTITY_TYPES.register(bus);
        FoundationNetwork.init();
    }

    /** Shortcut for {@code foundation:<path>}, saves some typing. */
    public static ResourceLocation identifier(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
