package com.SaintAlfonzo.creativecompat.surface;

import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;

import fuzs.blockrunner.data.BlockSpeedManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Makes CompatConfig {@code [surface.speed]} the source of BlockRunner's surface-tier speeds.
 * {@code mixin/BlockRunnerSpeedConfigMixin} hands every {@code config/blockrunner.json} read to
 * {@link #apply} before BlockRunner parses it, which sets each {@link SurfaceTier}'s entry from the
 * toml. BlockRunner then treats them like its own json values: it syncs them to players on join
 * and on {@code /reload}, and they show in its Speed Multiplier tooltip.
 *
 * <p>Only the file read is hooked, not BlockRunner's client-side message handler, so a client on a
 * dedicated server keeps the server's values whatever its own toml says. Saving the toml re-runs
 * BlockRunner's load on the server ({@link #onConfigReload}) when a tier speed changed.
 */
public final class SurfaceSpeeds {

    private SurfaceSpeeds() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Tier speeds last written into BlockRunner's json, or {@code null} if it never happened. */
    private static volatile Map<SurfaceTier, Double> applied;

    /** Overwrites the surface-tier entries of BlockRunner's freshly read json with the toml's values. */
    public static JsonObject apply(JsonObject json) {
        Map<SurfaceTier, Double> speeds = current();
        speeds.forEach((tier, speed) -> {
            JsonElement old = json.get(tier.jsonKey());
            if (old != null && old.isJsonPrimitive() && old.getAsDouble() != speed) {
                LOGGER.warn("[surface] blockrunner.json sets {} to {}; using [surface.speed] {} = {} instead",
                        tier.jsonKey(), old.getAsDouble(), tier.key(), speed);
            }
            json.addProperty(tier.jsonKey(), speed);
        });
        applied = speeds;
        LOGGER.info("[surface] {} tier speeds applied to BlockRunner from [surface.speed]", speeds.size());
        return json;
    }

    /** Saving the toml (by hand or in Create's Configs screen): push changed tier speeds to BlockRunner. */
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != CompatConfig.SPEC || !ModList.get().isLoaded("blockrunner")) {
            return;
        }
        Map<SurfaceTier, Double> last = applied;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || last == null || last.equals(current())) {
            return; // no world running here (a client on a server gets the server's values), or nothing changed
        }
        // re-reads blockrunner.json (so apply() runs again) and re-sends the values to every player
        server.execute(BlockSpeedManager.INSTANCE::load);
    }

    /** The hook rides on a private BlockRunner method; say so loudly if it stopped applying. */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (applied == null && ModList.get().isLoaded("blockrunner")) {
            LOGGER.error("[surface] BlockRunner loaded blockrunner.json without BlockRunnerSpeedConfigMixin: the "
                    + "[surface.speed] tier speeds are NOT in use (did a BlockRunner update rename deserialize?)");
        }
    }

    private static Map<SurfaceTier, Double> current() {
        Map<SurfaceTier, Double> speeds = new EnumMap<>(SurfaceTier.class);
        CompatConfig.SURFACE.speed.forEach((tier, value) -> speeds.put(tier, value.get()));
        return speeds;
    }
}
