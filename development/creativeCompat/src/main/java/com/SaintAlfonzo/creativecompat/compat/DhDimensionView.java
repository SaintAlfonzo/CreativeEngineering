package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.config.IDhApiConfig;
import com.seibel.distanthorizons.api.interfaces.config.IDhApiConfigValue;
import com.seibel.distanthorizons.api.interfaces.config.client.IDhApiFarFogConfig;
import com.seibel.distanthorizons.api.interfaces.config.client.IDhApiGraphicsConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Client half of {@link DhDimensions}: when a client level loads (join, dimension change), the Nether gets DH API
 * overrides for its LOD radius and far fog; any other level clears them, so the config file applies again. API
 * overrides are never written to DistantHorizons.toml. Runs before DH's own level-load handler (HIGHEST). perf.md 19.
 *
 * The far-fog distances are fractions of the LOD radius, so the shorter radius pulls the fog in by itself; the fog is
 * also pinned on (DH fog enabled, end 1.0 = the LOD edge, max thickness 1.0) so the cutoff is never a hard edge, whatever
 * the player set. Fog start, falloff, density and colour stay as configured.
 */
public final class DhDimensionView {

    private static boolean applied;

    private DhDimensionView() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ClientLevel level)) return;
        apply(DhDimensions.NETHER.equals(level.dimension().location().toString()));
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        apply(false);
    }

    private static void apply(boolean nether) {
        if (!nether && !applied) return;
        IDhApiConfig configs = DhApi.Delayed.configs;
        if (configs == null) return;
        try {
            IDhApiGraphicsConfig graphics = configs.graphics();
            IDhApiFarFogConfig farFog = graphics.fog().farFog();
            if (nether) {
                set(graphics.chunkRenderDistance(), DhDimensions.NETHER_LOD_RADIUS);
                set(graphics.fog().enableDhFog(), true);
                set(farFog.farFogEndDistance(), 1.0F);
                set(farFog.farFogMaxThickness(), 1.0F);
            } else {
                graphics.chunkRenderDistance().clearValue();
                graphics.fog().enableDhFog().clearValue();
                farFog.farFogEndDistance().clearValue();
                farFog.farFogMaxThickness().clearValue();
            }
            applied = nether;
        } catch (Throwable t) {
            CreativeCompat.LOGGER.error("[creativecompat] Distant Horizons dimension overrides failed", t);
        }
    }

    private static <T> void set(IDhApiConfigValue<T> value, T to) {
        if (!value.setValue(to)) CreativeCompat.LOGGER.warn("[creativecompat] Distant Horizons refused an API override");
    }
}
