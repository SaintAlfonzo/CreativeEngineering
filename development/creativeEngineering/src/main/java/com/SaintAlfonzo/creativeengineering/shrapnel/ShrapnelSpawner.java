package com.SaintAlfonzo.creativeengineering.shrapnel;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Spawns one burst of {@link ShrapnelEntity} pieces, evenly spread around the full horizontal
 * circle with a random per-piece pitch and speed. Shared by both TNT-explosion triggers:
 * {@code TntShrapnelHandler} (primed TNT entity) and Creative Compat's {@code itemphysic.ItemBurnTransform} (via util.BurnBursts)
 * (dropped TNT item burning). All tuning comes from {@link CEConfig.Shrapnel}.
 */
public final class ShrapnelSpawner {

    private ShrapnelSpawner() {}

    /** {@code owner}, if non-null, is excluded from shrapnel hits (see {@link ShrapnelEntity#canHitEntity}). */
    public static void burst(Level level, Vec3 center, Entity owner) {
        if (level.isClientSide || !CEConfig.SHRAPNEL.enabled.get()) return;

        int count = CEConfig.SHRAPNEL.count.get();
        if (count <= 0) return;

        double speed = CEConfig.SHRAPNEL.speed.get();
        double speedVariance = CEConfig.SHRAPNEL.speedVariance.get();
        double verticalAngle = CEConfig.SHRAPNEL.verticalAngle.get();
        double maxDistance = CEConfig.SHRAPNEL.maxDistance.get();
        float damage = (float) (double) CEConfig.SHRAPNEL.damage.get();

        RandomSource rng = level.random;
        // Evenly spaced around the circle, each with its own random jitter, rather than fully
        // random angles - keeps a fairly uniform ring even at low counts.
        double baseStep = (Math.PI * 2.0) / count;
        double startAngle = rng.nextDouble() * Math.PI * 2.0;

        for (int i = 0; i < count; i++) {
            double yaw = startAngle + baseStep * i + (rng.nextDouble() - 0.5) * baseStep * 0.5;
            double pitchDeg = (rng.nextDouble() * 2.0 - 1.0) * verticalAngle;
            double pitch = Math.toRadians(pitchDeg);

            double horiz = Math.cos(pitch);
            double dx = Math.cos(yaw) * horiz;
            double dz = Math.sin(yaw) * horiz;
            double dy = Math.sin(pitch);

            double pieceSpeed = speed * (1.0 + (rng.nextDouble() * 2.0 - 1.0) * speedVariance);

            ShrapnelEntity shrapnel = new ShrapnelEntity(
                    CreativeEngineeringEntities.SHRAPNEL.get(), center.x, center.y, center.z, level);
            if (owner != null) shrapnel.setOwner(owner);
            shrapnel.setDeltaMovement(dx * pieceSpeed, dy * pieceSpeed, dz * pieceSpeed);
            shrapnel.init(center, maxDistance, damage);
            level.addFreshEntity(shrapnel);
        }
    }
}
