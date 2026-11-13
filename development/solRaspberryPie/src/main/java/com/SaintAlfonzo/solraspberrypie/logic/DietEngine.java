package com.SaintAlfonzo.solraspberrypie.logic;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.data.RpData;
import com.SaintAlfonzo.solraspberrypie.data.SlotChoice;
import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.Rung;
import com.SaintAlfonzo.solraspberrypie.net.RpNetwork;
import com.SaintAlfonzo.solraspberrypie.net.S2CMessage;
import com.SaintAlfonzo.solraspberrypie.net.S2CState;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Evaluates the ladder for a player and applies the result (3, 8, 9, 10). Rungs are evaluated when a food is eaten,
 * when a paid action is confirmed, at death/respawn and at login; never on a timer. While a player is paused (12.3) the
 * data still updates but nothing is applied and no messages are sent.
 */
public final class DietEngine {
    private static final ResourceLocation HEALTH_ID = new ResourceLocation("minecraft", "generic.max_health");

    /** What the ladder grants at a given diversity, foods-eaten count and set of slot choices. */
    public static final class State {
        public double diversity;
        public boolean gate;
        public int activeSlots;
        public final Map<String, Rung> active = new LinkedHashMap<>();
        public final Map<ResourceLocation, Integer> flat = new LinkedHashMap<>();
        public final Map<ResourceLocation, Integer> slotEffects = new LinkedHashMap<>();
        public final Map<ResourceLocation, Integer> effects = new LinkedHashMap<>();
        public final Map<ResourceLocation, Double> attrs = new LinkedHashMap<>();
    }

    public static State compute(Ladder ladder, double diversity, int foodsEaten, List<SlotChoice> slots) {
        State s = new State();
        s.diversity = diversity;
        s.gate = foodsEaten >= SOLSweetPotatoConfig.minFoodsToActivate();
        if (!s.gate) return s;
        for (Rung r : ladder.rungs) {
            if (r.threshold > diversity || r.kind == Rung.Kind.UNLOCK) continue;
            s.active.put(r.key(), r);
            switch (r.kind) {
                case HEALTH -> s.attrs.merge(HEALTH_ID, r.amount, Double::sum);
                case ATTRIBUTE -> s.attrs.merge(r.id, r.amount, Double::sum);
                case FLAT -> s.flat.merge(r.id, r.level, Math::max);
                case SLOT -> {
                    s.activeSlots++;
                    SlotChoice c = r.slotIndex < slots.size() ? slots.get(r.slotIndex) : null;
                    if (c != null && ForgeRegistries.MOB_EFFECTS.containsKey(c.effect())) s.slotEffects.merge(c.effect(), c.level(), Math::max);
                }
                default -> {}
            }
        }
        s.effects.putAll(s.slotEffects);
        s.flat.forEach((k, v) -> s.effects.merge(k, v, Math::max));
        return s;
    }

    public static State current(ServerPlayer player) {
        FoodList fl = FoodList.get(player);
        return compute(Ladder.current, fl.foodDiversity(), fl.getFoodsEaten(), RpData.get(player).slots);
    }

    // ---------------------------------------------------------------------------------------------------------------

    public static void onFoodEaten(ServerPlayer player, Item item) {
        RpData d = RpData.get(player);
        if (!Drinks.isDrink(item)) d.stamp++;
        evaluate(player, true);
    }

    /** V8 and 3.2: fit the slot list to the ladder, clear choices naming effects that are not registered. */
    static void sanitize(ServerPlayer player, RpData d, Ladder ladder) {
        d.ensureSlots(ladder.slotCount());
        while (d.slots.size() > ladder.slotCount()) {
            SlotChoice gone = d.slots.remove(d.slots.size() - 1);
            if (gone != null) SolRaspberryPie.LOG.info("V8: {} lost the choice {} of slot {} (the ladder has {} slots now)",
                    player.getGameProfile().getName(), gone.effect(), d.slots.size() + 1, ladder.slotCount());
        }
        for (int i = 0; i < d.slots.size(); i++) {
            SlotChoice c = d.slots.get(i);
            if (c != null && !ForgeRegistries.MOB_EFFECTS.containsKey(c.effect())) {
                SolRaspberryPie.LOG.info("{}: slot {} held {}, which is not registered; cleared", player.getGameProfile().getName(), i + 1, c.effect());
                d.slots.set(i, null);
            }
        }
    }

    public static void evaluate(ServerPlayer player, boolean announce) {
        Ladder ladder = Ladder.current;
        RpData d = RpData.get(player);
        sanitize(player, d, ladder);
        FoodList fl = FoodList.get(player);
        double div = fl.foodDiversity();
        double oldMax = d.maxDiversity;
        if (div > d.maxDiversity) d.maxDiversity = div;
        State s = compute(ladder, div, fl.getFoodsEaten(), d.slots);
        if (announce && !d.paused) announce(player, ladder, d, s, oldMax);
        d.activeKeys = new HashSet<>(s.active.keySet());
        if (!d.paused) apply(player, d, s);
        sync(player);
    }

    // ---- messages (10) ----------------------------------------------------------------------------------------------

    private static void announce(ServerPlayer player, Ladder ladder, RpData d, State s, double oldMax) {
        for (Rung r : ladder.rungs) {
            boolean was = d.activeKeys.contains(r.key());
            boolean is = s.active.containsKey(r.key());
            if (r.kind == Rung.Kind.UNLOCK) {
                if (r.threshold > oldMax && r.threshold <= d.maxDiversity) {
                    send(player, S2CMessage.line(S2CMessage.GREEN, Component.translatable("solraspberrypie.msg.unlocked", effectName(r.id, r.level))));
                }
                continue;
            }
            if (was == is) continue;
            switch (r.kind) {
                case HEALTH -> send(player, S2CMessage.amount(is ? S2CMessage.GREEN : S2CMessage.RED, HEALTH_ID, is ? r.amount : -r.amount));
                case ATTRIBUTE -> send(player, S2CMessage.amount(is ? S2CMessage.GREEN : S2CMessage.RED, r.id, is ? r.amount : -r.amount));
                case FLAT -> {
                    if (is) {
                        send(player, S2CMessage.gained(S2CMessage.GREEN, effectName(r.id, r.level)));
                        for (int i = 0; i < s.activeSlots; i++) {
                            SlotChoice c = d.slot(i);
                            if (c != null && c.effect().equals(r.id) && c.level() == r.level) {
                                send(player, S2CMessage.line(S2CMessage.YELLOW, Component.translatable("solraspberrypie.msg.covered", effectName(r.id, r.level))));
                            }
                        }
                    } else {
                        send(player, S2CMessage.lost(effectName(r.id, r.level)));
                    }
                }
                case SLOT -> {
                    SlotChoice c = d.slot(r.slotIndex);
                    Component name = c != null ? effectName(c.effect(), c.level())
                            : Component.translatable("solraspberrypie.slot", r.slotIndex + 1);
                    if (is) {
                        send(player, S2CMessage.gained(S2CMessage.BLUE, name));
                    } else if (c != null && s.flat.containsKey(c.effect())) {
                        send(player, S2CMessage.line(S2CMessage.YELLOW, Component.translatable("solraspberrypie.msg.sustained", effectName(c.effect(), 0))));
                    } else {
                        send(player, S2CMessage.lost(name));
                    }
                }
                default -> {}
            }
        }
    }

    public static Component effectName(ResourceLocation id, int level) {
        MobEffect e = ForgeRegistries.MOB_EFFECTS.getValue(id);
        Component base = e != null ? e.getDisplayName() : Component.literal(id.toString());
        if (level <= 0) return base;
        return Component.empty().append(base).append(" ").append(Component.translatable("enchantment.level." + level));
    }

    private static void send(ServerPlayer player, S2CMessage msg) {
        RpNetwork.toPlayer(player, msg);
    }

    // ---- applying (3.1, 9) -----------------------------------------------------------------------------------------

    static UUID modifierId(ResourceLocation attribute) {
        return UUID.nameUUIDFromBytes((SolRaspberryPie.MOD_ID + ":" + attribute).getBytes(StandardCharsets.UTF_8));
    }

    static void apply(ServerPlayer player, RpData d, State s) {
        // Attributes: transient modifiers with fixed ids, nothing written to player data.
        Set<ResourceLocation> attrs = new HashSet<>(d.appliedAttrs);
        attrs.addAll(s.attrs.keySet());
        for (ResourceLocation id : attrs) {
            Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(id);
            if (attr == null) continue;
            setModifier(player, attr, modifierId(id), s.attrs.getOrDefault(id, 0.0));
        }
        d.appliedAttrs.clear();
        d.appliedAttrs.addAll(s.attrs.keySet());
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());

        // Effects.
        Set<ResourceLocation> ids = new HashSet<>(d.applied.keySet());
        ids.addAll(s.effects.keySet());
        for (ResourceLocation id : ids) {
            Integer old = d.applied.get(id);
            Integer neu = s.effects.get(id);
            if (old != null && old.equals(neu)) continue;
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
            if (effect == null) {
                d.applied.remove(id);
                continue;
            }
            if (old != null) EffectApplier.strip(player, d, effect, old);
            if (neu != null) {
                EffectApplier.add(player, d, effect, neu);
                d.applied.put(id, neu);
            } else {
                d.applied.remove(id);
            }
        }
        d.appliedFlat.clear();
        d.appliedFlat.addAll(s.flat.keySet());
    }

    private static void setModifier(ServerPlayer player, Attribute attr, UUID id, double amount) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) return;
        AttributeModifier existing = inst.getModifier(id);
        if (existing != null && existing.getAmount() == amount) return;
        if (existing != null) inst.removeModifier(id);
        if (amount != 0) inst.addTransientModifier(new AttributeModifier(id, "Raspberry Pie diet", amount, AttributeModifier.Operation.ADDITION));
    }

    /** Re-adds every applied diet effect (the 20 s refresh, and the tick after a cure or clear). */
    public static void reapplyAll(ServerPlayer player) {
        RpData d = RpData.get(player);
        if (d.paused) return;
        for (Map.Entry<ResourceLocation, Integer> e : new HashMap<>(d.applied).entrySet()) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(e.getKey());
            if (effect != null) EffectApplier.add(player, d, effect, e.getValue());
        }
    }

    /** Drops every applied bonus from the bookkeeping (a new player entity has none). */
    public static void forgetApplied(RpData d) {
        d.applied.clear();
        d.appliedFlat.clear();
        d.appliedAttrs.clear();
        d.activeKeys.clear();
    }

    public static boolean isMaxHealth(ResourceLocation id) {
        return HEALTH_ID.equals(id) || Attributes.MAX_HEALTH == ForgeRegistries.ATTRIBUTES.getValue(id);
    }

    public static void sync(ServerPlayer player) {
        RpNetwork.toPlayer(player, S2CState.of(player));
    }

    private DietEngine() {}
}
