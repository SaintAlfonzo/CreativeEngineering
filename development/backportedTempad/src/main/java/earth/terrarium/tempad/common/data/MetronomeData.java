// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/data/MetronomeData.kt and common/registries/ModAttachments.kt
// (metronome_energy, an overworld attachment). Translated to Java for Forge 1.19.2: the overworld attachment -> overworld
// SavedData "tempad_metronome_energy" {stored: {<player uuid>: int}, positions: {<player uuid>: [GlobalPos]}} (the
// upstream codec's field names). Always saved (like an attachment), since callers edit the maps in place.
// Ticked from Tempad (ServerTickEvent END, every metronome.generation_rate ticks); metronomes add and remove themselves.
package earth.terrarium.tempad.common.data;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.config.CommonConfigCache;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MetronomeData extends SavedData {
    public static final String NAME = "tempad_metronome_energy";

    public final Map<UUID, List<GlobalPos>> positions = new LinkedHashMap<>();
    public final Object2IntMap<UUID> stored = new Object2IntOpenHashMap<>();

    /** Upstream {@code metronomeEnergy} (ServerDataDelegate): null without a running server. */
    @Nullable
    public static MetronomeData get() {
        var server = Tempad.server();
        if (server == null) return null;
        return server.overworld().getDataStorage().computeIfAbsent(MetronomeData::load, MetronomeData::new, NAME);
    }

    public void add(UUID player, GlobalPos pos, int contents) {
        if (positions.get(player) != null && positions.get(player).contains(pos)) return;
        positions.computeIfAbsent(player, id -> new ArrayList<>()).add(pos);
        stored.put(player, contents + stored.getOrDefault(player, 0));
    }

    public int getCount(UUID player) {
        List<GlobalPos> list = positions.get(player);
        return list == null ? 0 : list.size();
    }

    public int getCapacity(UUID player) {
        return getCount(player) * CommonConfigCache.Metronome.capacity();
    }

    public int getStored(UUID player) {
        return stored.getOrDefault(player, 0);
    }

    public void tick() {
        for (Object2IntMap.Entry<UUID> entry : stored.object2IntEntrySet()) {
            UUID player = entry.getKey();
            int amount = entry.getIntValue();
            int generated = CommonConfig.Metronome.generationAmount() * (CommonConfigCache.Metronome.scaleGeneration() ? getCount(player) : 1) + amount;
            entry.setValue(Math.min(generated, getCapacity(player)));
        }
    }

    public int remove(UUID player, GlobalPos pos) {
        if (getCapacity(player) == 0) return 0;
        if (!stored.containsKey(player)) return 0;
        int current = stored.getInt(player);
        int amount = current / getCount(player);
        stored.put(player, current - amount);
        positions.computeIfAbsent(player, id -> new ArrayList<>()).remove(pos);
        return amount;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag storedTag = new CompoundTag();
        stored.object2IntEntrySet().forEach(entry -> storedTag.putInt(entry.getKey().toString(), entry.getIntValue()));
        tag.put("stored", storedTag);
        CompoundTag positionsTag = new CompoundTag();
        positions.forEach((player, list) -> {
            ListTag listTag = new ListTag();
            for (GlobalPos pos : list) {
                GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).result().ifPresent(listTag::add);
            }
            positionsTag.put(player.toString(), listTag);
        });
        tag.put("positions", positionsTag);
        return tag;
    }

    public static MetronomeData load(CompoundTag tag) {
        MetronomeData data = new MetronomeData();
        CompoundTag storedTag = tag.getCompound("stored");
        for (String key : storedTag.getAllKeys()) {
            try {
                data.stored.put(UUID.fromString(key), storedTag.getInt(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
        CompoundTag positionsTag = tag.getCompound("positions");
        for (String key : positionsTag.getAllKeys()) {
            UUID player;
            try {
                player = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            List<GlobalPos> list = new ArrayList<>();
            for (Tag entry : positionsTag.getList(key, Tag.TAG_COMPOUND)) {
                GlobalPos.CODEC.parse(NbtOps.INSTANCE, entry).result().ifPresent(list::add);
            }
            data.positions.put(player, list);
        }
        return data;
    }
}
