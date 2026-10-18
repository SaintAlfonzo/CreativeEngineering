package net.satisfy.foundation.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.foundation.Foundation;
import net.satisfy.foundation.particle.ColorParticleOption;
import org.jetbrains.annotations.NotNull;

/**
 * The particle types Vinery uses ({@code DYE_SPLASH} and {@code COLORED_DRIP} take a color).
 * Port: the soup, feather, firefly and water particles of upstream Foundation have no user in Vinery and are left out.
 */
public final class FoundationParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Foundation.MOD_ID);

    public static final RegistryObject<ParticleType<ColorParticleOption>> DYE_SPLASH = PARTICLE_TYPES.register("dye_splash", FoundationParticles::colored);
    public static final RegistryObject<ParticleType<ColorParticleOption>> COLORED_DRIP = PARTICLE_TYPES.register("colored_drip", FoundationParticles::colored);
    public static final RegistryObject<SimpleParticleType> LEAF = PARTICLE_TYPES.register("leaf", () -> new SimpleParticleType(false));

    private static ParticleType<ColorParticleOption> colored() {
        return new ParticleType<>(false, ColorParticleOption.DESERIALIZER) {
            @Override
            public @NotNull Codec<ColorParticleOption> codec() {
                return ColorParticleOption.codec(this);
            }
        };
    }

    private FoundationParticles() {
    }
}
