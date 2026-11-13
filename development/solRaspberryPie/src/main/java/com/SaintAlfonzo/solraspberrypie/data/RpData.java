package com.SaintAlfonzo.solraspberrypie.data;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Raspberry Pie's own player data (13.4): max diversity, slot choices, the Regeneration anchor, the state stamp, the
 * pause flag and the Liquid Diet Log. Copied on death and on the End exit clone. Fields marked transient are rebuilt
 * from the world at login and never saved.
 */
public final class RpData implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<RpData> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation KEY = new ResourceLocation(SolRaspberryPie.MOD_ID, "diet");
    /** Unique drinks remembered, like Apple Pie's own food memory. */
    public static final int DRINK_MEMORY = 256;

    // ---- saved ----
    public double maxDiversity;
    /** Index = slot number; null = empty. Sized to the ladder's slot count at evaluation (V8). */
    public final List<SlotChoice> slots = new ArrayList<>();
    public long regenAnchor = -1;
    public int stamp;
    public boolean paused;
    public boolean booksGiven;
    /** Unique drinks, value = drinks since it was last drunk (0 = latest). */
    public final Map<ResourceLocation, Integer> drinks = new LinkedHashMap<>();
    /** Health saved at logout, restored once the transient max-health modifier is back (login). */
    public float savedHealth = -1;

    // ---- transient ----
    /** Rung keys active at the last evaluation (gain/loss messages). */
    public Set<String> activeKeys = new HashSet<>();
    /** Effects currently applied by us: effect id -> level (1-based). */
    public final Map<ResourceLocation, Integer> applied = new HashMap<>();
    /** Effects applied as flat bonuses (display: never on the HUD). */
    public final Set<ResourceLocation> appliedFlat = new HashSet<>();
    /** Attributes carrying our transient modifier. */
    public final Set<ResourceLocation> appliedAttrs = new HashSet<>();
    public boolean suppressRemoveEvent;
    public boolean pendingReapply;
    public boolean pendingDeathMessage;
    public float cloneHealth = -1;

    private final LazyOptional<RpData> self = LazyOptional.of(() -> this);

    public static RpData get(Player player) {
        return player.getCapability(CAP).orElseGet(RpData::new);
    }

    public SlotChoice slot(int i) {
        return i >= 0 && i < slots.size() ? slots.get(i) : null;
    }

    public void ensureSlots(int count) {
        while (slots.size() < count) slots.add(null);
    }

    public void addDrink(ResourceLocation id) {
        for (Map.Entry<ResourceLocation, Integer> e : drinks.entrySet()) e.setValue(e.getValue() + 1);
        drinks.remove(id);
        drinks.put(id, 0);
        Iterator<Map.Entry<ResourceLocation, Integer>> it = drinks.entrySet().iterator();
        while (it.hasNext()) if (it.next().getValue() >= DRINK_MEMORY) it.remove();
    }

    /** Drinks, most recent first. */
    public List<ResourceLocation> drinksByRecency() {
        List<Map.Entry<ResourceLocation, Integer>> list = new ArrayList<>(drinks.entrySet());
        list.sort(Map.Entry.comparingByValue());
        List<ResourceLocation> out = new ArrayList<>(list.size());
        for (Map.Entry<ResourceLocation, Integer> e : list) out.add(e.getKey());
        return out;
    }

    public void copyFrom(RpData o) {
        maxDiversity = o.maxDiversity;
        slots.clear();
        slots.addAll(o.slots);
        regenAnchor = -1;
        stamp = o.stamp;
        paused = o.paused;
        booksGiven = o.booksGiven;
        drinks.clear();
        drinks.putAll(o.drinks);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == CAP ? self.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag t = new CompoundTag();
        t.putDouble("maxDiversity", maxDiversity);
        ListTag sl = new ListTag();
        for (SlotChoice c : slots) {
            CompoundTag s = new CompoundTag();
            if (c != null) {
                s.putString("effect", c.effect().toString());
                s.putInt("level", c.level());
            }
            sl.add(s);
        }
        t.put("slots", sl);
        t.putInt("stamp", stamp);
        t.putBoolean("paused", paused);
        t.putBoolean("booksGiven", booksGiven);
        ListTag dl = new ListTag();
        for (Map.Entry<ResourceLocation, Integer> e : drinks.entrySet()) {
            CompoundTag d = new CompoundTag();
            d.putString("id", e.getKey().toString());
            d.putInt("since", e.getValue());
            dl.add(d);
        }
        t.put("drinks", dl);
        if (savedHealth >= 0) t.putFloat("savedHealth", savedHealth);
        return t;
    }

    @Override
    public void deserializeNBT(CompoundTag t) {
        maxDiversity = t.getDouble("maxDiversity");
        slots.clear();
        for (Tag el : t.getList("slots", Tag.TAG_COMPOUND)) {
            CompoundTag s = (CompoundTag) el;
            ResourceLocation id = s.contains("effect") ? ResourceLocation.tryParse(s.getString("effect")) : null;
            slots.add(id == null ? null : new SlotChoice(id, Math.max(1, s.getInt("level"))));
        }
        stamp = t.getInt("stamp");
        paused = t.getBoolean("paused");
        booksGiven = t.getBoolean("booksGiven");
        drinks.clear();
        for (Tag el : t.getList("drinks", Tag.TAG_COMPOUND)) {
            CompoundTag d = (CompoundTag) el;
            ResourceLocation id = ResourceLocation.tryParse(d.getString("id"));
            if (id != null) drinks.put(id, d.getInt("since"));
        }
        savedHealth = t.contains("savedHealth") ? t.getFloat("savedHealth") : -1;
    }
}
