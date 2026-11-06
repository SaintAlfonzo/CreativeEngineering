package com.SaintAlfonzo.creativecompat.mixin;

import codyhuh.breezy.common.network.BreezyNetworking;
import codyhuh.breezy.common.network.NewWindSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Supplementaries 2.4.20's Breezy integration was compiled against an older Breezy
 * ({@code coda.breezy.networking.BreezyNetworking}, {@code WindDirectionSavedData}). Breezy 1.0.8 moved
 * to {@code codyhuh.breezy.common.network} and now stores a wind angle in degrees per height layer, so
 * the first wind vane to tick on a client threw {@link NoClassDefFoundError}. This re-implements the one
 * method against the 1.0.8 API. The angle is the heading of Breezy's own wind vector
 * ({@code stepX = cos, stepZ = sin}) turned into a Minecraft yaw, as the old {@code Direction.toYRot()}
 * result was. Only called when Supplementaries sees Breezy loaded.
 */
@Mixin(targets = "net.mehvahdjukaar.supplementaries.integration.forge.BreezyCompatImpl", remap = false)
public abstract class SupplementariesBreezyCompatMixin {

    /** @author SaintAlfonzo */
    @Overwrite
    public static float getWindDirection(BlockPos pos, Level level) {
        NewWindSavedData wind = BreezyNetworking.CLIENT_CACHE;
        if (wind == null) return 90.0F;
        double angle = Math.toRadians(wind.getWindAtHeight(pos.getY(), level));
        return (float) (Mth.atan2(-Math.cos(angle), Math.sin(angle)) * 180.0D / Math.PI);
    }
}
