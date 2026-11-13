package com.SaintAlfonzo.solraspberrypie.logic;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
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
 * a slot (ages out oldest foods until the highest active slot is lost; active choices above shift down). Greyed slots
 * are read only. Everything is refused while the player is paused (12.3).
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
                for (FoodInstance f : sim.group) real.put(f, sim.memory.get(f));
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

    /** In the pool, unlocked at that level, not hidden by an active flat bonus (V11), not in another slot (Q-D). */
    static boolean pickable(Ladder ladder, RpData d, DietEngine.State s, ResourceLocation effect, int level, int ignoreSlot) {
        if (!ladder.pool.containsKey(effect)) return false;
        if (level < 1 || level > ladder.unlockedLevel(effect, d.maxDiversity)) return false;
        if (s.flat.containsKey(effect)) return false;
        for (int i = 0; i < d.slots.size(); i++) {
            SlotChoice c = d.slots.get(i);
            if (i != ignoreSlot && c != null && c.effect().equals(effect)) return false;
        }
        return true;
    }

    private static final class Sim {
        Map<FoodInstance, Integer> memory;
        List<FoodInstance> group;
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
        int eaten = fl.getFoodsEaten();
        Map<FoodInstance, Integer> original = FoodWindow.copy(fl);
        List<FoodInstance> oldest = FoodWindow.windowOldestFirst(original);
        Sim sim = new Sim();
        sim.slot = a.slot;
        sim.before = FoodWindow.diversity(original);
        sim.sBefore = DietEngine.compute(ladder, sim.before, eaten, d.slots);
        sim.slots = new ArrayList<>(d.slots);

        if (change) {
            int n = Math.min(RpConfig.SERVER.respecFoods.get(), oldest.size());
            sim.group = new ArrayList<>(oldest.subList(0, n));
            sim.memory = new HashMap<>(original);
            FoodWindow.moveOut(sim.memory, sim.group);
            sim.slots.set(a.slot, new SlotChoice(a.effect, a.level));
        } else {
            int active = sim.sBefore.activeSlots;
            sim.group = new ArrayList<>(oldest);
            sim.memory = new HashMap<>(original);
            FoodWindow.moveOut(sim.memory, sim.group);
            for (int n = 1; n <= oldest.size(); n++) {
                Map<FoodInstance, Integer> m = new HashMap<>(original);
                List<FoodInstance> g = oldest.subList(0, n);
                FoodWindow.moveOut(m, g);
                if (ladder.activeSlots(FoodWindow.diversity(m)) < active) {
                    sim.group = new ArrayList<>(g);
                    sim.memory = m;
                    break;
                }
            }
            for (int j = a.slot; j < active - 1; j++) sim.slots.set(j, sim.slots.get(j + 1));
            sim.slots.set(active - 1, null);
        }
        sim.after = FoodWindow.diversity(sim.memory);
        sim.sAfter = DietEngine.compute(ladder, sim.after, eaten, sim.slots);
        return sim;
    }

    private SlotActions() {}
}
