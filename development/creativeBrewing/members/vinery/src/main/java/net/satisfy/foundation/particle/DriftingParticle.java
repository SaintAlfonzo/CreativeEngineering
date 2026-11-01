package net.satisfy.foundation.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class DriftingParticle extends TextureSheetParticle {
    private static final int FADE_DURATION = 6;
    private static final float PI = (float) Math.PI;
    private static final float TWO_PI = (float) (Math.PI * 2);

    public enum Style {
        LEAF(0.98F, 0.82F, 0.88F, false),
        FLUFF(0.96F, 0.85F, 0.9F, true);

        private final float friction;
        private final float swayZFactor;
        private final float growthDamping;
        private final boolean translucent;

        Style(float friction, float swayZFactor, float growthDamping, boolean translucent) {
            this.friction = friction;
            this.swayZFactor = swayZFactor;
            this.growthDamping = growthDamping;
            this.translucent = translucent;
        }
    }

    public record MotionProfile(int minimumLifetime, int maximumLifetime, float minimumSwaySpeed, float maximumSwaySpeed, float minimumSwayAmount, float maximumSwayAmount, float minimumUpwardVelocityStart, float maximumUpwardVelocityStart, float minimumUpwardVelocityEnd, float maximumUpwardVelocityEnd, float minimumSideVelocity, float maximumSideVelocity, float minimumRotationSpeed, float maximumRotationSpeed, float targetAlpha, float initialScale, float initialScaleVariance, float minimumScale, float maximumScale, float growthSpeed, float minimumGravityStrength, float maximumGravityStrength, float minimumDownwardAcceleration, float maximumDownwardAcceleration, float minimumGlideStrength, float maximumGlideStrength) {
        public static final MotionProfile LEAF = new MotionProfile(30, 42, 1.4F, 2.1F, 0.055F, 0.105F, 0.03F, 0.055F, -0.015F, 0.0F, 0.03F, 0.06F, 0.035F, 0.07F, 1.0F, 0.075F, 0.015F, 0.095F, 0.135F, 0.012F, 0.7F, 1.0F, 0.022F, 0.036F, 0.1F, 0.16F);
        public static final MotionProfile LEAF_RISING = new MotionProfile(34, 46, 1.25F, 1.9F, 0.05F, 0.09F, 0.065F, 0.095F, -0.005F, 0.01F, 0.008F, 0.02F, 0.03F, 0.06F, 1.0F, 0.075F, 0.015F, 0.095F, 0.135F, 0.012F, 0.62F, 0.9F, 0.018F, 0.03F, 0.08F, 0.14F);
        public static final MotionProfile FLUFF = new MotionProfile(28, 42, 1.1F, 1.8F, 0.045F, 0.085F, 0.05F, 0.085F, -0.02F, 0.005F, 0.01F, 0.03F, 0.0F, 0.0F, 0.92F, 0.09F, 0.03F, 0.11F, 0.18F, 0.012F, 0.45F, 0.72F, 0.012F, 0.024F, 0.08F, 0.14F);
    }

    private final Style style;
    private final float swayPhase;
    private final float swaySpeed;
    private final float swayAmountX;
    private final float swayAmountZ;
    private final float upwardVelocityStart;
    private final float upwardVelocityEnd;
    private final float sideVelocityX;
    private final float sideVelocityZ;
    private final float targetAlpha;
    private final float maximumScale;
    private final float gravityStrength;
    private final float downwardAcceleration;
    private final float glideStrength;
    private float rotationSpeed;
    private float previousQuadSize;
    private float growthSpeed;

    protected DriftingParticle(ClientLevel level, double x, double y, double z, Style style, MotionProfile profile) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.style = style;
        this.friction = style.friction;
        this.gravity = 0.0F;
        this.hasPhysics = true;
        this.lifetime = profile.minimumLifetime + random.nextInt(profile.maximumLifetime - profile.minimumLifetime + 1);
        this.swayPhase = random.nextFloat() * TWO_PI;
        this.swaySpeed = range(profile.minimumSwaySpeed, profile.maximumSwaySpeed);
        this.swayAmountX = range(profile.minimumSwayAmount, profile.maximumSwayAmount);
        this.swayAmountZ = range(profile.minimumSwayAmount, profile.maximumSwayAmount);
        this.upwardVelocityStart = range(profile.minimumUpwardVelocityStart, profile.maximumUpwardVelocityStart);
        this.upwardVelocityEnd = range(profile.minimumUpwardVelocityEnd, profile.maximumUpwardVelocityEnd);
        this.sideVelocityX = range(profile.minimumSideVelocity, profile.maximumSideVelocity) * randomSign();
        this.sideVelocityZ = range(profile.minimumSideVelocity, profile.maximumSideVelocity) * randomSign();
        this.rotationSpeed = range(profile.minimumRotationSpeed, profile.maximumRotationSpeed) * randomSign();
        this.targetAlpha = profile.targetAlpha;
        this.maximumScale = range(profile.minimumScale, profile.maximumScale);
        this.gravityStrength = range(profile.minimumGravityStrength, profile.maximumGravityStrength);
        this.downwardAcceleration = range(profile.minimumDownwardAcceleration, profile.maximumDownwardAcceleration);
        this.glideStrength = range(profile.minimumGlideStrength, profile.maximumGlideStrength);
        this.quadSize = profile.initialScale + random.nextFloat() * profile.initialScaleVariance;
        this.previousQuadSize = quadSize;
        this.growthSpeed = profile.growthSpeed;
        this.alpha = 0.0F;

        if (style == Style.LEAF) {
            this.roll = random.nextFloat() * TWO_PI;
            int foliage = level.getBiome(new BlockPos(x, y, z)).value().getFoliageColor();
            this.rCol = (foliage >> 16 & 255) / 255.0F;
            this.gCol = (foliage >> 8 & 255) / 255.0F;
            this.bCol = (foliage & 255) / 255.0F;
        } else {
            this.roll = random.nextFloat() * 0.15F - 0.075F;
            this.rCol = 0.96F + random.nextFloat() * 0.03F;
            this.gCol = 0.95F + random.nextFloat() * 0.03F;
            this.bCol = 0.93F + random.nextFloat() * 0.03F;
        }
        this.oRoll = roll;
    }

    private float range(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private float randomSign() {
        return random.nextBoolean() ? 1.0F : -1.0F;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        oRoll = roll;
        previousQuadSize = quadSize;

        if (age++ >= lifetime) {
            remove();
            return;
        }

        float progress = (float) age / (float) lifetime;
        float swayAngle = swayPhase + progress * TWO_PI * swaySpeed;
        float upwardVelocity = Mth.lerp(progress, upwardVelocityStart, upwardVelocityEnd);
        float glideCurve = Mth.sin(progress * PI) * glideStrength;
        float verticalVelocity = upwardVelocity - (gravityStrength * progress * progress + downwardAcceleration * age * progress - glideCurve);

        xd = sideVelocityX + Mth.sin(swayAngle) * swayAmountX * 0.08F;
        zd = sideVelocityZ + Mth.cos(swayAngle * style.swayZFactor) * swayAmountZ * 0.08F;
        yd = verticalVelocity * 0.08F;
        move(xd, yd, zd);

        if (onGround) {
            if (style == Style.LEAF) {
                xd = 0.0D;
                zd = 0.0D;
                rotationSpeed = 0.0F;
            } else {
                xd *= 0.35D;
                zd *= 0.35D;
            }
            yd = 0.0D;
        }

        if (style == Style.LEAF) {
            roll += rotationSpeed;
        } else {
            roll = Mth.sin(progress * TWO_PI + swayPhase) * 0.12F;
        }

        if (quadSize < maximumScale) {
            quadSize = Math.min(maximumScale, quadSize + growthSpeed);
            growthSpeed *= style.growthDamping;
        }

        if (age < FADE_DURATION) {
            alpha = Mth.lerp((float) age / FADE_DURATION, 0.0F, targetAlpha);
        } else {
            int remaining = lifetime - age;
            alpha = remaining < FADE_DURATION ? targetAlpha * ((float) remaining / FADE_DURATION) : targetAlpha;
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        return Mth.lerp(partialTick, previousQuadSize, quadSize);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return style.translucent ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Style style;
        private final MotionProfile profile;
        private final BooleanSupplier enabled;

        public Provider(SpriteSet sprites, Style style, MotionProfile profile) {
            this(sprites, style, profile, () -> true);
        }

        public Provider(SpriteSet sprites, Style style, MotionProfile profile, BooleanSupplier enabled) {
            this.sprites = sprites;
            this.style = style;
            this.profile = profile;
            this.enabled = enabled;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            if (!enabled.getAsBoolean()) {
                return null;
            }
            DriftingParticle particle = new DriftingParticle(level, x, y, z, style, profile);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
