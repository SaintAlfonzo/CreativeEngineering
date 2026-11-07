// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/data/TravelHistoryAttachment.kt. Translated to Java for Forge
// 1.19.2: attachment -> part of the TempadPlayerData capability; Date keys stored as epoch milliseconds (upstream's
// DATE_CODEC writes the same number as a string key); DimensionTransition -> forge/Teleports.
package earth.terrarium.tempad.common.data;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.forge.Teleports;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TravelHistoryAttachment {
    public static final ResourceLocation DIM_EXIT_MARKER = Tempad.id("dimension_exit");
    public static final ResourceLocation DIM_ENTER_MARKER = Tempad.id("dimension_enter");
    public static final ResourceLocation DEATH_MARKER = Tempad.id("death");
    public static final ResourceLocation RESPAWN_MARKER = Tempad.id("respawn");

    /** Insertion (time) order, as upstream's mutableMapOf. */
    private final LinkedHashMap<Date, HistoricalLocation> history = new LinkedHashMap<>();

    public Map<Date, HistoricalLocation> getHistory() {
        return Collections.unmodifiableMap(history);
    }

    public void add(HistoricalLocation historicalLocation) {
        history.put(new Date(), historicalLocation);
    }

    public void logLocation(LivingEntity entity) {
        logLocation(entity, null);
    }

    public void logLocation(LivingEntity entity, @Nullable ResourceLocation marker) {
        if (entity.isSpectator() || (entity instanceof Player player && player.getAbilities().instabuild)) return;
        if (history.isEmpty() || marker != null) {
            add(new HistoricalLocation(marker, entity.level.dimension(), entity.position()));
            if (history.size() > CommonConfig.maxHistorySize()) history.remove(history.keySet().iterator().next());
            return;
        }
        HistoricalLocation lastPos = null;
        for (HistoricalLocation value : history.values()) lastPos = value;
        if (entity.level.dimension() == lastPos.dimension() && entity.position().distanceToSqr(lastPos.pos()) < 64) return;
        if (history.size() > CommonConfig.maxHistorySize()) history.remove(history.keySet().iterator().next());
        add(new HistoricalLocation(null, entity.level.dimension(), entity.position()));
    }

    /** Backtrack to a specific location in the history, and removes all locations after it. */
    public void backtrackTo(LivingEntity entity, Date time) {
        HistoricalLocation historicalLocation = history.get(time);
        if (historicalLocation == null) return;
        List<Date> ids = new ArrayList<>();
        for (Date historicalTime : history.keySet()) {
            if (historicalTime.compareTo(time) >= 0) ids.add(historicalTime);
        }
        for (Date id : ids) history.remove(id);
        if (entity.getServer() == null) return;
        ServerLevel target = entity.getServer().getLevel(historicalLocation.dimension());
        if (target == null) return;
        Teleports.changeDimension(entity, target, historicalLocation.pos(), Vec3.ZERO, 0f, 0f);
    }

    public Map<Date, HistoricalLocation> getRelevantHistory() {
        LinkedHashMap<Date, HistoricalLocation> relevant = new LinkedHashMap<>();
        history.forEach((date, location) -> {
            if (location.marker() != null) relevant.put(date, location);
        });
        return relevant;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        history.forEach((date, location) -> tag.put(Long.toString(date.getTime()), location.save()));
        return tag;
    }

    /** Restores the entries in time order (a CompoundTag does not keep insertion order). */
    public void load(CompoundTag tag) {
        history.clear();
        List<Long> times = new ArrayList<>();
        for (String key : tag.getAllKeys()) {
            try {
                times.add(Long.parseLong(key));
            } catch (NumberFormatException ignored) {
            }
        }
        Collections.sort(times);
        for (Long time : times) history.put(new Date(time), HistoricalLocation.load(tag.getCompound(Long.toString(time))));
    }

    public void copyFrom(TravelHistoryAttachment other) {
        history.clear();
        history.putAll(other.history);
    }
}
