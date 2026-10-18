package net.satisfy.foundation.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.NotNull;

/** Same hang, fall and land drop as {@link WaterDripParticle}, but tinted with the color of the particle option. */
public class ColoredDripParticle extends WaterDripParticle {
    protected ColoredDripParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites, ColorParticleOption color) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.setColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static class Provider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull ColorParticleOption type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ColoredDripParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, type);
        }
    }
}
