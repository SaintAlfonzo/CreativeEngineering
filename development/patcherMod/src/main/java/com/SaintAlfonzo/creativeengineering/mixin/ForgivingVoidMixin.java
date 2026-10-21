package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.forgivingvoid.ForgivingVoidBridge;
import net.blay09.mods.forgivingvoid.ForgivingVoid;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Forgiving Void catches a player who fell out of the world and drops them back in
 * at {@code fallingHeight} with the <em>same</em> X/Z. That lets a player void-fall
 * on a loop forever, and in particular lets them "travel" across the End by riding
 * repeated void falls without moving under their own power.
 *
 * <p>This redirect intercepts the single {@code teleportTo(x, y, z)} call inside
 * {@code ForgivingVoid.onPlayerTick} and moves the horizontal landing spot toward or
 * away from a configurable anchor each time, so every void fall costs (or gains) the
 * player ground and a loop no longer runs forever. All the dials — enabled/disabled,
 * anchor location (default + per-dimension overrides), direction, and jump-distance
 * range — are read through {@link ForgivingVoidBridge}, a read-through layer over the
 * {@code [forgiving_void]} section of CEConfig ({@code creativeengineering-common.toml}).
 *
 * <p>The {@code teleportTo} call is matched by its SRG name with
 * {@code remap = false} (this project ships no Mixin refmap); the
 * {@code teleportTo(...)} call in the handler body is a Mojmap reference that the
 * reobf step maps back to {@code m_6021_}.
 */
@Mixin(value = ForgivingVoid.class, remap = false)
public class ForgivingVoidMixin {

    @Redirect(
            method = "onPlayerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;m_6021_(DDD)V"
            ),
            remap = false
    )
    private static void creativeengineering$pullTowardAnchor(ServerPlayer player, double x, double y, double z) {
        if (!ForgivingVoidBridge.isEnabled()) {
            player.teleportTo(x, y, z);
            return;
        }

        String dimensionId = player.level.dimension().location().toString();
        double[] anchor = ForgivingVoidBridge.anchorFor(dimensionId);
        double ax = anchor[0];
        double az = anchor[1];

        double dx = x - ax;
        double dz = z - az;
        double dist = Math.sqrt(dx * dx + dz * dz);
        double pull = ForgivingVoidBridge.randomDistance();

        double newX;
        double newZ;
        if (ForgivingVoidBridge.isPullToward()) {
            if (dist <= pull || dist < 1.0e-4) {
                // Already within one pull of the anchor - just land on it.
                newX = ax;
                newZ = az;
            } else {
                double factor = (dist - pull) / dist;
                newX = ax + dx * factor;
                newZ = az + dz * factor;
            }
        } else {
            if (dist < 1.0e-4) {
                // Landed exactly on the anchor with nothing to push away from - pick a direction.
                double[] unit = ForgivingVoidBridge.randomUnitVector();
                dx = unit[0];
                dz = unit[1];
                dist = 1.0;
            }
            double ux = dx / dist;
            double uz = dz / dist;
            newX = x + ux * pull;
            newZ = z + uz * pull;
        }

        player.teleportTo(newX, y, newZ);
    }
}
