// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Reads the wire_types files that upstream loads as a datapack registry (see WireRegistry).
package org.patryk3211.powergrid.electricity.wire.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;

import java.util.HashMap;
import java.util.Map;

public class WireTypeReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();

    public WireTypeReloadListener() {
        super(GSON, WireRegistry.DIRECTORY);
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> files, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, WireItemEntry> entries = new HashMap<>();
        files.forEach((id, json) -> WireItemEntry.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> PowerGrid.LOGGER.error("Failed to parse wire type {}: {}", id, error))
                .ifPresent(entry -> entries.put(id, entry)));
        WireRegistry.setServerEntries(entries);
        PowerGrid.LOGGER.info("Loaded {} wire types", entries.size());
    }
}
