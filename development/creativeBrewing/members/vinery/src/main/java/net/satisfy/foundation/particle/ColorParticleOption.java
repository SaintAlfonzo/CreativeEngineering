package net.satisfy.foundation.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Port: 1.20.5's {@code ColorParticleOption} (a particle type plus an ARGB color), which 1.19.2 doesn't have.
 * Same accessors as the vanilla class, so the particle code reads the same.
 */
public class ColorParticleOption implements ParticleOptions {
    public static final Deserializer<ColorParticleOption> DESERIALIZER = new Deserializer<>() {
        @Override
        public @NotNull ColorParticleOption fromCommand(@NotNull ParticleType<ColorParticleOption> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            return new ColorParticleOption(type, (int) Long.parseLong(reader.readUnquotedString(), 16));
        }

        @Override
        public @NotNull ColorParticleOption fromNetwork(@NotNull ParticleType<ColorParticleOption> type, @NotNull FriendlyByteBuf buf) {
            return new ColorParticleOption(type, buf.readInt());
        }
    };

    private final ParticleType<ColorParticleOption> type;
    private final int color;

    public static ColorParticleOption create(ParticleType<ColorParticleOption> type, int argb) {
        return new ColorParticleOption(type, argb);
    }

    public static ColorParticleOption create(ParticleType<ColorParticleOption> type, float r, float g, float b) {
        return new ColorParticleOption(type, 0xFF000000 | ((int) (r * 255) & 0xFF) << 16 | ((int) (g * 255) & 0xFF) << 8 | ((int) (b * 255) & 0xFF));
    }

    public ColorParticleOption(ParticleType<ColorParticleOption> type, int color) {
        this.type = type;
        this.color = color;
    }

    public static Codec<ColorParticleOption> codec(ParticleType<ColorParticleOption> type) {
        return Codec.INT.xmap(color -> new ColorParticleOption(type, color), option -> option.color);
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return type;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(color);
    }

    @Override
    public @NotNull String writeToString() {
        return String.format(Locale.ROOT, "%s %08x", Registry.PARTICLE_TYPE.getKey(type), color);
    }

    public float getRed() {
        return (color >> 16 & 0xFF) / 255.0F;
    }

    public float getGreen() {
        return (color >> 8 & 0xFF) / 255.0F;
    }

    public float getBlue() {
        return (color & 0xFF) / 255.0F;
    }

    public float getAlpha() {
        return (color >>> 24 & 0xFF) / 255.0F;
    }
}
