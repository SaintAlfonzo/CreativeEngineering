package com.SaintAlfonzo.solraspberrypie.ladder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The validated ladder: rungs sorted by threshold, the slot pool (effect -> level cap) and the slot milestones in
 * order, and the toggle effects (effect -> on by default; a toggle effect is switched on and off by the player instead of
 * changing its level, raspberry_pie.md 3.3). Built once at server start by {@link LadderLoader} and synced to clients
 * on login.
 */
public final class Ladder {
	public static Ladder current = new Ladder(List.of(), Map.of(), Map.of());

	public final List<Rung> rungs;
	public final Map<ResourceLocation, Integer> pool;
	public final List<Rung> slots;
	/** Toggle effects: effect -> switched on by default. */
	public final Map<ResourceLocation, Boolean> toggles;

	public Ladder(List<Rung> rungs, Map<ResourceLocation, Integer> pool) {
		this(rungs, pool, Map.of());
	}

	public Ladder(List<Rung> rungs, Map<ResourceLocation, Integer> pool, Map<ResourceLocation, Boolean> toggles) {
		List<Rung> sorted = new ArrayList<>(rungs);
		sorted.sort((a, b) -> Double.compare(a.threshold, b.threshold)); // stable: file order kept on ties
		List<Rung> slotList = new ArrayList<>();
		for (int i = 0; i < sorted.size(); i++) {
			Rung r = sorted.get(i);
			r.index = i;
			if (r.kind == Rung.Kind.SLOT) {
				r.slotIndex = slotList.size();
				slotList.add(r);
			}
		}
		this.rungs = Collections.unmodifiableList(sorted);
		this.pool = Collections.unmodifiableMap(new LinkedHashMap<>(pool));
		this.slots = Collections.unmodifiableList(slotList);
		this.toggles = Collections.unmodifiableMap(new LinkedHashMap<>(toggles));
	}

	public boolean isToggle(ResourceLocation effect) {
		return toggles.containsKey(effect);
	}

	/** Whether a toggle effect is on for a player who never switched it (true for an effect that is no toggle). */
	public boolean toggleDefault(ResourceLocation effect) {
		return toggles.getOrDefault(effect, true);
	}

	/**
	 * Lang key of the hover tagline for an effect at a level: the unlock rung of that effect and level, else the highest
	 * lower level's unlock rung with one, else a flat rung of that effect and level; null = no tagline.
	 */
	public String tagline(ResourceLocation effect, int level) {
		String best = null;
		int bestLevel = 0;
		for (Rung r : rungs) {
			if (r.kind != Rung.Kind.UNLOCK || r.tagline == null || !effect.equals(r.id) || r.level > Math.max(level, 1)) continue;
			if (r.level >= bestLevel) {
				best = r.tagline;
				bestLevel = r.level;
			}
		}
		if (best != null) return best;
		for (Rung r : rungs) {
			if (r.kind == Rung.Kind.FLAT && r.tagline != null && effect.equals(r.id) && r.level == level) return r.tagline;
		}
		return null;
	}

	public int slotCount() {
		return slots.size();
	}

	/** Slots whose milestone is met at this diversity (slots are numbered in threshold order, so a prefix). */
	public int activeSlots(double diversity) {
		int n = 0;
		for (Rung s : slots) if (s.threshold <= diversity) n++;
		return n;
	}

	/** Highest unlocked level of an effect for a player's max diversity; 0 = not unlocked. Clamped to the pool cap. */
	public int unlockedLevel(ResourceLocation effect, double maxDiversity) {
		Integer cap = pool.get(effect);
		if (cap == null) return 0;
		int best = 0;
		for (Rung r : rungs) {
			if (r.kind == Rung.Kind.UNLOCK && effect.equals(r.id) && r.threshold <= maxDiversity) best = Math.max(best, r.level);
		}
		return Math.min(best, cap);
	}

	public double maxThreshold() {
		return rungs.isEmpty() ? 0 : rungs.get(rungs.size() - 1).threshold;
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(rungs.size());
		for (Rung r : rungs) r.write(buf);
		buf.writeVarInt(pool.size());
		pool.forEach((k, v) -> {
			buf.writeResourceLocation(k);
			buf.writeVarInt(v);
		});
		buf.writeVarInt(toggles.size());
		toggles.forEach((k, v) -> {
			buf.writeResourceLocation(k);
			buf.writeBoolean(v);
		});
	}

	public static Ladder read(FriendlyByteBuf buf) {
		int n = buf.readVarInt();
		List<Rung> rungs = new ArrayList<>(n);
		for (int i = 0; i < n; i++) rungs.add(Rung.read(buf));
		int p = buf.readVarInt();
		Map<ResourceLocation, Integer> pool = new LinkedHashMap<>();
		for (int i = 0; i < p; i++) pool.put(buf.readResourceLocation(), buf.readVarInt());
		int t = buf.readVarInt();
		Map<ResourceLocation, Boolean> toggles = new LinkedHashMap<>();
		for (int i = 0; i < t; i++) toggles.put(buf.readResourceLocation(), buf.readBoolean());
		return new Ladder(rungs, pool, toggles);
	}
}
