package net.satisfy.foundation.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public final class WobbleAnimation {
    public static final int DURATION = 10;
    private static final float MAX_ANGLE = 4.0F;
    private static final float SWINGS = 2.5F;
    private static final float TWO_PI = (float) (Math.PI * 2);

    private WobbleAnimation() {
    }

    public static boolean isActive(Level level, long startTime) {
        return level != null && level.getGameTime() - startTime < DURATION;
    }

    public static void apply(PoseStack poseStack, Level level, long startTime, float seed) {
        apply(poseStack, level, startTime, Minecraft.getInstance().getFrameTime(), seed);
    }

    public static void apply(PoseStack poseStack, Level level, long startTime, float partialTick, float seed) {
        apply(poseStack, level, startTime, partialTick, seed, 1.0F);
    }

    public static void apply(PoseStack poseStack, Level level, long startTime, float partialTick, float seed, float strength) {
        if (!isActive(level, startTime)) {
            return;
        }
        float swing = angle(level, startTime, partialTick) * strength;
        float direction = seed * TWO_PI;
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(swing * Mth.cos(direction)));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(swing * Mth.sin(direction)));
        poseStack.translate(-0.5F, 0.0F, -0.5F);
    }

    public static float angle(Level level, long startTime, float partialTick) {
        if (!isActive(level, startTime)) {
            return 0.0F;
        }
        float progress = Mth.clamp((level.getGameTime() - startTime + partialTick) / DURATION, 0.0F, 1.0F);
        float damping = (1.0F - progress) * (1.0F - progress);
        return Mth.sin(progress * SWINGS * TWO_PI) * MAX_ANGLE * damping;
    }

    public static float seed(long value) {
        return (Mth.murmurHash3Mixer((int) (value ^ (value >>> 32))) & 0xFFFF) / 65535.0F;
    }
}
