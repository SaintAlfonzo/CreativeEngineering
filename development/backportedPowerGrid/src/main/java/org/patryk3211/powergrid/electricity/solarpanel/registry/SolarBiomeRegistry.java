// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Port: upstream reads solar_biome_override as a 1.20 datapack registry (DataPackRegistryEvent, not on Forge 43). The
// same files (data/<namespace>/powergrid/solar_biome_override/*.json) are read by a JSON reload listener on the server;
// every reader is server side (SolarPanelBlockEntity), so nothing is synced to clients. Fix B10 (power_grid.md 3.5):
// upstream kept only the last entry's match result, so every override but the last file's was ignored.
package org.patryk3211.powergrid.electricity.solarpanel.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;

import java.util.HashMap;
import java.util.Map;

public class SolarBiomeRegistry {
    public static final String DIRECTORY = PowerGrid.MOD_ID + "/solar_biome_override";

    // biome id -> entry; replaced whole on every reload
    private static volatile Map<String, SolarBiomeEntry> entries = Map.of();

    public static SolarBiomeEntry forBiome(@NotNull Level level, BlockPos pos) {
        if (pos == null) return null;
        var current = entries;
        if (current.isEmpty()) return null;
        var biomeName = level.getBiome(pos).unwrap().map((resourceKey) ->
                resourceKey.location().toString(), (biome) -> "[unregistered " + biome + "]");
        return current.get(biomeName);
    }

    public static int size() {
        return entries.size();
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new GsonBuilder().create();

        public ReloadListener() {
            super(GSON, DIRECTORY);
        }

        @Override
        protected void apply(@NotNull Map<ResourceLocation, JsonElement> files, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
            Map<String, SolarBiomeEntry> loaded = new HashMap<>();
            files.forEach((id, json) -> SolarBiomeEntry.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> PowerGrid.LOGGER.error("Failed to parse solar biome override {}: {}", id, error))
                    .ifPresent(entry -> {
                        var previous = loaded.put(entry.biome(), entry);
                        if (previous != null)
                            PowerGrid.LOGGER.warn("Solar biome override {} replaces an earlier one for biome {}", id, entry.biome());
                    }));
            entries = Map.copyOf(loaded);
            PowerGrid.LOGGER.info("Loaded {} solar biome overrides", loaded.size());
        }
    }
}
