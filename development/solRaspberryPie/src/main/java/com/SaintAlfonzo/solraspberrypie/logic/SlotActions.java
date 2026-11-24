// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.logic;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.data.DietAdjustment;
import com.SaintAlfonzo.solraspberrypie.data.RpData;
import com.SaintAlfonzo.solraspberrypie.data.SlotChoice;
import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.Rung;
import com.SaintAlfonzo.solraspberrypie.net.C2SAction;
import com.SaintAlfonzo.solraspberrypie.net.RpNetwork;
import com.SaintAlfonzo.solraspberrypie.net.S2CPreview;
import com.tarinoita.solsweetpotato.tracking.CapabilityHandler;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Player slot actions (5). Free: fill an empty active slot (lands in the lowest empty active slot) and change a slot's
 * level. Paid, through a confirm panel: change a slot's effect (ages out the oldest foods of the decay window) and drop
 * a slot (ages out oldest foods until the highest active slot is lost, and only that slot; active choices above shift
 * down). Aged-out foods leave Apple Pie's memory (FoodWindow). Grayed slots
 * are read only. A toggle effect the player has is switched on and off for free (3.3). Everything is refused while the
 * player is paused (12.3).
 */
public final class SlotActions {

	public static void handle(ServerPlayer player, C2SAction a) {
		RpData d = RpData.get(player);
		Ladder ladder = Ladder.current;
		DietEngine.sanitize(player, d, ladder);
		if (d.paused) {
			DietEngine.sync(player);
			return;
		}
		DietEngine.State s = DietEngine.current(player);
		switch (a.op) {
			case FILL -> {
				int target = -1;
				for (int i = 0; i < s.activeSlots; i++) {
					if (d.slot(i) == null) {
						target = i;
						break;
					}
				}
				if (target < 0 || a.effect == null || !pickable(ladder, d, s, a.effect, a.level, -1)) break;
				d.slots.set(target, new SlotChoice(a.effect, a.level));
				DietEngine.evaluate(player, false);
			}
			case LEVEL -> {
				SlotChoice c = activeChoice(d, s, a.slot);
				if (c == null || a.level < 1 || a.level == c.level()) break;
				int max = Math.max(ladder.unlockedLevel(c.effect(), d.maxDiversity), 0);
				if (a.level > max) break;
				d.slots.set(a.slot, new SlotChoice(c.effect(), a.level));
				DietEngine.evaluate(player, false);
			}
			case TOGGLE -> {
				// 3.3: a toggle effect the player has (flat, or in an active slot) is switched on or off; nothing is paid.
				if (a.effect == null || !ladder.isToggle(a.effect)) break;
				boolean granted = s.flat.containsKey(a.effect) || s.slotEffects.containsKey(a.effect);
				if (!granted) break;
				d.toggles.put(a.effect, !d.isOn(ladder, a.effect));
				DietEngine.evaluate(player, false);
			}
			case PREVIEW_CHANGE, PREVIEW_DROP -> {
				Sim sim = simulate(player, d, ladder, s, a);
				if (sim != null) RpNetwork.toPlayer(player, sim.preview(a, d.stamp, false));
			}
			case CONFIRM_CHANGE, CONFIRM_DROP -> {
				Sim sim = simulate(player, d, ladder, s, a);
				if (sim == null) break;
				if (a.stamp != d.stamp) {
					// 5.4: something changed since the preview; nothing happens, a fresh preview goes back.
					RpNetwork.toPlayer(player, sim.preview(a, d.stamp, true));
					break;
				}
				Map<FoodInstance, Integer> real = FoodWindow.memory(FoodList.get(player));
				FoodWindow.ageOut(real, sim.group);
				if (sim.lowered != null) {
					// The Adjustment entry stays in the window with its lowered value, at its age.
					Integer k = real.remove(sim.lowered);
					if (k != null) real.put(sim.lowered, k);
				}
				d.slots.clear();
				d.slots.addAll(sim.slots);
				d.stamp++;
				CapabilityHandler.syncFoodList(player);
				DietEngine.evaluate(player, true);
			}
		}
		DietEngine.sync(player);
	}

	private static SlotChoice activeChoice(RpData d, DietEngine.State s, int slot) {
		return slot >= 0 && slot < s.activeSlots ? d.slot(slot) : null;
	}

	/**
	 * In the pool, unlocked at that level, not hidden by an active flat bonus (V11) or an unlocked replacement
	 * (food-tuning #49), not in another slot (Q-D).
	 */
	static boolean pickable(Ladder ladder, RpData d, DietEngine.State s, ResourceLocation effect, int level, int ignoreSlot) {
		if (!ladder.pool.containsKey(effect)) return false;
		if (level < 1 || level > ladder.unlockedLevel(effect, d.maxDiversity)) return false;
		if (s.flat.containsKey(effect) || ladder.replacedBy(effect, d.maxDiversity) != null) return false;
		for (int i = 0; i < d.slots.size(); i++) {
			SlotChoice c = d.slots.get(i);
			if (i != ignoreSlot && c != null && c.effect().equals(effect)) return false;
		}
		return true;
	}

	private static final class Sim {
		Map<FoodInstance, Integer> memory;
		List<FoodInstance> group;
		/** A drop's Adjustment entry with its lowered value (it stays in the window), or null. */
		FoodInstance lowered;
		List<SlotChoice> slots;
		double before;
		double after;
		DietEngine.State sBefore;
		DietEngine.State sAfter;
		int slot;

		S2CPreview preview(C2SAction a, int stamp, boolean stale) {
			S2CPreview p = new S2CPreview();
			boolean change = a.op == C2SAction.Op.PREVIEW_CHANGE || a.op == C2SAction.Op.CONFIRM_CHANGE;
			p.op = change ? C2SAction.Op.PREVIEW_CHANGE : C2SAction.Op.PREVIEW_DROP;
			p.slot = slot;
			p.effect = a.effect;
			p.level = a.level;
			p.stamp = stamp;
			p.before = before;
			p.after = after;
			p.stale = stale;
			p.slotGreyedAfter = change && slot >= sAfter.activeSlots;
			for (FoodInstance f : group) {
				ResourceLocation id = ForgeRegistries.ITEMS.getKey(f.getItem());
				if (id != null) p.aged.add(id);
			}
			lostLines(p);
			return p;
		}

		private void lostLines(S2CPreview p) {
			Ladder ladder = Ladder.current;
			for (Rung r : ladder.rungs) {
				if (!sBefore.active.containsKey(r.key()) || sAfter.active.containsKey(r.key())) continue;
				Component line = switch (r.kind) {
					case HEALTH -> hearts(r.amount);
					case ATTRIBUTE -> attribute(r.id, r.amount);
					case FLAT -> Component.translatable("solraspberrypie.preview.flat", DietEngine.effectName(r.id, r.level));
					case SLOT -> Component.translatable("solraspberrypie.slot", r.slotIndex + 1);
					default -> null;
				};
				if (line != null) add(p, line, false);
			}
			sBefore.slotEffects.forEach((id, lvl) -> {
				Integer now = sAfter.slotEffects.get(id);
				if (now != null && now >= lvl) return;
				if (sAfter.flat.containsKey(id)) {
					add(p, Component.translatable("solraspberrypie.msg.sustained", DietEngine.effectName(id, 0)), true);
				} else if (!sAfter.effects.containsKey(id) || sAfter.effects.get(id) < lvl) {
					add(p, DietEngine.effectName(id, lvl), false);
				}
			});
		}

		private static void add(S2CPreview p, Component line, boolean yellow) {
			p.lines.add(line);
			p.yellow.add(yellow);
		}
	}

	public static Component hearts(double healthPoints) {
		double hearts = Math.abs(healthPoints) / 2.0;
		String n = hearts == Math.floor(hearts) ? String.valueOf((long) hearts) : String.format(Locale.ROOT, "%.1f", hearts);
		return Component.translatable(hearts == 1 ? "solraspberrypie.heart" : "solraspberrypie.hearts", n);
	}

	public static Component attribute(ResourceLocation id, double amount) {
		Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(id);
		Component name = attr != null ? Component.translatable(attr.getDescriptionId()) : Component.literal(id.toString());
		String n = amount == Math.floor(amount) ? String.valueOf((long) Math.abs(amount)) : String.format(Locale.ROOT, "%.2f", Math.abs(amount));
		return Component.empty().append(name).append((amount < 0 ? " -" : " +") + n);
	}

	private static Sim simulate(ServerPlayer player, RpData d, Ladder ladder, DietEngine.State s, C2SAction a) {
		boolean change = a.op == C2SAction.Op.PREVIEW_CHANGE || a.op == C2SAction.Op.CONFIRM_CHANGE;
		SlotChoice current = activeChoice(d, s, a.slot);
		if (current == null) return null;
		if (change && (a.effect == null || a.effect.equals(current.effect()) || !pickable(ladder, d, s, a.effect, a.level, a.slot))) return null;

		FoodList fl = FoodList.get(player);
		int eaten = com.SaintAlfonzo.solraspberrypie.data.GateFoods.count(fl);
		Map<FoodInstance, Integer> original = FoodWindow.copy(fl);
		List<FoodInstance> oldest = FoodWindow.windowOldestFirst(original);
		Sim sim = new Sim();
		sim.slot = a.slot;
		sim.before = FoodWindow.diversity(original);
		sim.sBefore = DietEngine.compute(ladder, sim.before, eaten, d.slots, DietEngine.switchedOn(ladder, d), d.maxDiversity);
		sim.slots = new ArrayList<>(d.slots);

		if (change) {
			// food-tuning #50: changing a covered slot (a permanent buff or an unlocked replacement holds its effect) is free.
			int n = ladder.covered(current.effect(), sim.sBefore.flat.keySet(), d.maxDiversity) ? 0 : changeCount(oldest.size());
			sim.group = new ArrayList<>(oldest.subList(0, n));
			sim.memory = new HashMap<>(original);
			FoodWindow.ageOut(sim.memory, sim.group);
			sim.slots.set(a.slot, new SlotChoice(a.effect, a.level));
		} else {
			int active = sim.sBefore.activeSlots;
			DropPlan plan = planDrop(ladder, original, oldest, active);
			sim.group = plan.group;
			sim.lowered = plan.lowered;
			sim.memory = plan.memory;
			for (int j = a.slot; j < active - 1; j++) sim.slots.set(j, sim.slots.get(j + 1));
			sim.slots.set(active - 1, null);
		}
		sim.after = FoodWindow.diversity(sim.memory);
		sim.sAfter = DietEngine.compute(ladder, sim.after, eaten, sim.slots, DietEngine.switchedOn(ladder, d), d.maxDiversity);
		return sim;
	}

	/** Foods a change ages out (5.2): {@code respecFoods}, capped at the foods in the window. */
	static int changeCount(int inWindow) {
		return Math.min(RpConfig.SERVER.respecFoods.get(), inWindow);
	}

	/** What a drop ages out: whole foods, and possibly the Adjustment entry lowered instead of aged out whole. */
	public static final class DropPlan {
		public final List<FoodInstance> group = new ArrayList<>();
		public FoodInstance lowered;
		public Map<FoodInstance, Integer> memory;

		/** Foods touched, for the hover count. */
		public int count() {
			return group.size() + (lowered != null ? 1 : 0);
		}
	}

	/**
	 * A drop (5.2): ages out foods from the oldest end of the window until the highest active slot is lost, and only that
	 * slot (raspberry-pie-publish #24). A food whose ageing out alone would also take a lower slot is kept and the next
	 * oldest is tried; the Adjustment entry of {@code /solrp diversity set} (one entry worth the whole diversity) is lowered
	 * instead, to just under the highest slot's milestone. When no such order loses exactly one slot, foods go from the
	 * oldest end until the highest slot is lost. Nothing with no active slot. Public for the headless test.
	 */
	public static DropPlan planDrop(Ladder ladder, Map<FoodInstance, Integer> original, List<FoodInstance> oldest, int active) {
		DropPlan p = new DropPlan();
		p.memory = new HashMap<>(original);
		if (active <= 0) return p;
		for (FoodInstance f : oldest) {
			Integer k = p.memory.remove(f);
			int now = ladder.activeSlots(FoodWindow.diversity(p.memory));
			if (now >= active) {
				p.group.add(f);
				continue;
			}
			if (now == active - 1) {
				p.group.add(f);
				return p;
			}
			p.memory.put(f, k);
			FoodInstance part = lowered(ladder, p.memory, f, k, active);
			if (part != null) {
				p.memory.remove(f);
				p.memory.put(part, k);
				p.lowered = part;
				return p;
			}
		}
		DropPlan whole = new DropPlan();
		whole.memory = new HashMap<>(original);
		for (FoodInstance f : oldest) {
			whole.memory.remove(f);
			whole.group.add(f);
			if (ladder.activeSlots(FoodWindow.diversity(whole.memory)) < active) break;
		}
		return whole;
	}

	/**
	 * The Adjustment entry {@code f} (count {@code k}) with its value lowered so the diversity lands just under the
	 * highest active slot's milestone and above the next one; null for any other food or when no value does that.
	 */
	private static FoodInstance lowered(Ladder ladder, Map<FoodInstance, Integer> memory, FoodInstance f, int k, int active) {
		if (!((Object) f instanceof DietAdjustment adj) || adj.solrp$value() == null) return null;
		double top = ladder.slots.get(active - 1).threshold;
		double below = active >= 2 ? ladder.slots.get(active - 2).threshold : 0;
		double target = top - Math.min(0.01, (top - below) / 2);
		FoodInstance unit = new FoodInstance(f.getItem());
		((DietAdjustment) (Object) unit).solrp$setValue(1.0);
		double weight = FoodList.calculateDiversityContribution(unit, k);
		if (weight <= 0) return null;
		double value = adj.solrp$value() - (FoodWindow.diversity(memory) - target) / weight;
		if (!(value >= 0)) return null;
		FoodInstance part = new FoodInstance(f.getItem());
		((DietAdjustment) (Object) part).solrp$setValue(value);
		Map<FoodInstance, Integer> m = new HashMap<>(memory);
		m.remove(f);
		m.put(part, k);
		return ladder.activeSlots(FoodWindow.diversity(m)) == active - 1 ? part : null;
	}

	/**
	 * The hover counts on the effect screen's Change and Drop buttons (plan_diet_books.md 1.10): {change, drop}, by the
	 * same rules as the confirm panel, synced with the state instead of fetched by a preview.
	 */
	public static int[] ageCounts(ServerPlayer player, DietEngine.State s) {
		Ladder ladder = Ladder.current;
		if (ladder == null) return new int[]{0, 0};
		Map<FoodInstance, Integer> original = FoodWindow.copy(FoodList.get(player));
		// The drop count walks the window (thousands of lookups at pack settings) and syncs fire on every food: reuse the
		// last answer while the window, the active slots, the ladder and respecFoods are the same (PR-RP-13).
		int key = java.util.Objects.hash(original, s.diversity, s.activeSlots, System.identityHashCode(ladder), RpConfig.SERVER.respecFoods.get());
		synchronized (AGE_CACHE) {
			int[] hit = AGE_CACHE.get(player);
			if (hit != null && hit[2] == key) return new int[]{hit[0], hit[1]};
		}
		List<FoodInstance> oldest = FoodWindow.windowOldestFirst(original);
		int[] out = {changeCount(oldest.size()), planDrop(ladder, original, oldest, s.activeSlots).count()};
		synchronized (AGE_CACHE) {
			AGE_CACHE.put(player, new int[]{out[0], out[1], key});
		}
		return out;
	}

	/** Last {change, drop, key} per player for {@link #ageCounts}. */
	private static final Map<ServerPlayer, int[]> AGE_CACHE = new java.util.WeakHashMap<>();

	private SlotActions() {}
}
