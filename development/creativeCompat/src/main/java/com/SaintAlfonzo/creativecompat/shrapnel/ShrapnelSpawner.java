package com.SaintAlfonzo.creativecompat.shrapnel;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Spawns one burst of {@link ShrapnelEntity} pieces, evenly spread around the full horizontal
 * circle with a random per-piece pitch and speed. Shared by both TNT-explosion triggers:
 * {@code TntShrapnelHandler} (primed TNT entity) and Creative Compat's {@code itemphysic.ItemBurnTransform} (via util.BurnBursts)
 * (dropped TNT item burning). All tuning comes from {@link CompatConfig.Shrapnel}.
 */
public final class ShrapnelSpawner {

    private ShrapnelSpawner() {}

    /** {@code owner}, if non-null, is excluded from shrapnel hits (see {@link ShrapnelEntity#canHitEntity}). */
    public static void burst(Level level, Vec3 center, Entity owner) {
        if (level.isClientSide || !CompatConfig.SHRAPNEL.enabled.get()) return;

        int count = CompatConfig.SHRAPNEL.count.get();
        if (count <= 0) return;

        double speed = CompatConfig.SHRAPNEL.speed.get();
        double speedVariance = CompatConfig.SHRAPNEL.speedVariance.get();
        double verticalAngle = CompatConfig.SHRAPNEL.verticalAngle.get();
        double maxDistance = CompatConfig.SHRAPNEL.maxDistance.get();
        float damage = (float) (double) CompatConfig.SHRAPNEL.damage.get();

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
                    ShrapnelEntities.SHRAPNEL.get(), center.x, center.y, center.z, level);
            if (owner != null) shrapnel.setOwner(owner);
            shrapnel.setDeltaMovement(dx * pieceSpeed, dy * pieceSpeed, dz * pieceSpeed);
            shrapnel.init(center, maxDistance, damage);
            level.addFreshEntity(shrapnel);
        }
    }
}
