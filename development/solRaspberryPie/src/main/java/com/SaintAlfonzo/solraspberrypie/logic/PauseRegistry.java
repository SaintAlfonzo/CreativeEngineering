package com.SaintAlfonzo.solraspberrypie.logic;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * The world's list of paused players (12.3), so {@code /solrp pause list} also finds players who are offline. The
 * pause itself lives in each player's own data; this is the index.
 */
public final class PauseRegistry extends SavedData {
    private static final String NAME = SolRaspberryPie.MOD_ID + "_paused";
    private final Map<UUID, String> paused = new TreeMap<>();

    public static PauseRegistry get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PauseRegistry::load, PauseRegistry::new, NAME);
    }

    private static PauseRegistry load(CompoundTag tag) {
        PauseRegistry r = new PauseRegistry();
        for (String key : tag.getAllKeys()) {
            try {
                r.paused.put(UUID.fromString(key), tag.getString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return r;
    }

    public void set(ServerPlayer player, boolean isPaused) {
        if (isPaused) paused.put(player.getUUID(), player.getGameProfile().getName());
        else paused.remove(player.getUUID());
        setDirty();
    }

    public Map<UUID, String> all() {
        return paused;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        paused.forEach((id, name) -> tag.putString(id.toString(), name));
        return tag;
    }
}
