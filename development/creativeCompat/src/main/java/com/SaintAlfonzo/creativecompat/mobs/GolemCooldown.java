package com.SaintAlfonzo.creativecompat.mobs;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Golem farm cooldown (R2, plan_small_fixes_1116.md; replaces Quark's iron farm nerf, which stays off). A village is
 * keyed by its meeting point (bell) nearest to where things happen, within {@link #RADIUS} blocks. When that village loses
 * {@code deaths} villager-summoned iron golems within {@code window} ticks, its villagers summon none for {@code cooldown}
 * ticks ({@code mixin/VillagerGolemCooldownMixin}). Player-built golems neither count nor are affected (players build
 * them by hand, not through the villager path). Deaths away from any meeting point are not counted. Per level, saved with
 * the world. Config: {@code [golems]} in creativecompat-common.toml.
 */
public final class GolemCooldown extends SavedData {
    static final int RADIUS = 64;
    private static final String NAME = "creativecompat_golem_cooldown";

    private final Map<Long, Deque<Long>> deaths = new HashMap<>();
    private final Map<Long, Long> blockedUntil = new HashMap<>();

    private static GolemCooldown get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(GolemCooldown::load, GolemCooldown::new, NAME);
    }

    static Optional<BlockPos> village(ServerLevel level, BlockPos at) {
        return level.getPoiManager().findClosest(h -> h.is(PoiTypes.MEETING), at, RADIUS, PoiManager.Occupancy.ANY);
    }

    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof IronGolem golem) || golem.isPlayerCreated()) return;
        if (!(golem.level instanceof ServerLevel level) || !CompatConfig.Golems.on()) return;
        village(level, golem.blockPosition()).ifPresent(v -> get(level).record(v.asLong(), level.getGameTime()));
    }

    /** True when the village around {@code at} may not summon golems now. */
    public static boolean blocked(ServerLevel level, BlockPos at) {
        if (!CompatConfig.Golems.on()) return false;
        GolemCooldown data = get(level);
        if (data.blockedUntil.isEmpty()) return false;
        Optional<BlockPos> v = village(level, at);
        if (v.isEmpty()) return false;
        Long until = data.blockedUntil.get(v.get().asLong());
        if (until == null) return false;
        if (level.getGameTime() < until) return true;
        data.blockedUntil.remove(v.get().asLong());
        data.setDirty();
        return false;
    }

    private void record(long village, long now) {
        CompatConfig.Golems c = CompatConfig.GOLEMS;
        Deque<Long> q = deaths.computeIfAbsent(village, k -> new ArrayDeque<>());
        q.addLast(now);
        while (!q.isEmpty() && now - q.peekFirst() > c.window.get()) q.removeFirst();
        if (q.size() >= c.deaths.get()) {
            blockedUntil.put(village, now + c.cooldown.get());
            q.clear();
        }
        if (q.isEmpty()) deaths.remove(village);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, Deque<Long>> e : deaths.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putLong("village", e.getKey());
            t.put("deaths", new LongArrayTag(e.getValue().stream().mapToLong(Long::longValue).toArray()));
            list.add(t);
        }
        tag.put("deaths", list);
        CompoundTag until = new CompoundTag();
        blockedUntil.forEach((k, v) -> until.putLong(Long.toString(k), v));
        tag.put("blockedUntil", until);
        return tag;
    }

    private static GolemCooldown load(CompoundTag tag) {
        GolemCooldown data = new GolemCooldown();
        for (Tag t : tag.getList("deaths", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            Deque<Long> q = new ArrayDeque<>();
            for (long d : c.getLongArray("deaths")) q.addLast(d);
            if (!q.isEmpty()) data.deaths.put(c.getLong("village"), q);
        }
        CompoundTag until = tag.getCompound("blockedUntil");
        for (String k : until.getAllKeys()) {
            try {
                data.blockedUntil.put(Long.parseLong(k), until.getLong(k));
            } catch (NumberFormatException ignored) {
            }
        }
        return data;
    }
}
