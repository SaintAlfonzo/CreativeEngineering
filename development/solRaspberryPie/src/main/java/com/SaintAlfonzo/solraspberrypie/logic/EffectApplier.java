package com.SaintAlfonzo.solraspberrypie.logic;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.data.RpData;
import com.SaintAlfonzo.solraspberrypie.mixin.MobEffectInstanceAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Adding and removing diet effect instances (9). Every instance we add carries the marker item in its curative items
 * (9.1). Switch-off follows the potion cases of 9.4: a stronger potion on top keeps its place and ours is stripped from
 * underneath (A); ours merged into a same-level potion with more than one effect duration left is left alone (B); a
 * weaker potion under ours is promoted back to the top (C).
 */
public final class EffectApplier {
    public static int duration() {
        return RpConfig.SERVER.effectDuration.get();
    }

    public static boolean hasMarker(MobEffectInstance inst) {
        if (inst == null) return false;
        for (ItemStack s : inst.getCurativeItems()) if (s.is(SolRaspberryPie.MARKER.get())) return true;
        return false;
    }

    /** "Ours" (9.4): our marker with at most one effect duration left, or an unmarked same-level instance our refresh merged into. */
    static boolean isOurs(MobEffectInstance inst, int level) {
        if (inst.getDuration() > duration()) return false;
        return hasMarker(inst) || inst.getAmplifier() == level - 1;
    }

    public static boolean chainHasMarker(MobEffectInstance inst) {
        for (MobEffectInstance i = inst; i != null; i = ((MobEffectInstanceAccessor) i).solrp$getHidden()) {
            if (hasMarker(i)) return true;
        }
        return false;
    }

    public static void add(ServerPlayer player, RpData data, MobEffect effect, int level) {
        int dur = duration();
        if (effect == MobEffects.REGENERATION) {
            // 9.2.1: keep heals on the first add's 50-tick rhythm, whatever caused this (re-)add.
            long now = player.level.getGameTime();
            if (data.regenAnchor < 0) data.regenAnchor = now;
            int period = Math.max(1, 50 >> (level - 1));
            dur = dur - (int) ((now - data.regenAnchor) % period);
        }
        MobEffectInstance inst = new MobEffectInstance(effect, dur, level - 1, true, false, true);
        List<ItemStack> cures = new ArrayList<>(inst.getCurativeItems());
        cures.add(new ItemStack(SolRaspberryPie.MARKER.get()));
        inst.setCurativeItems(cures);
        player.addEffect(inst);
    }

    public static void strip(ServerPlayer player, RpData data, MobEffect effect, int level) {
        if (effect == MobEffects.REGENERATION) data.regenAnchor = -1;
        MobEffectInstance top = player.getEffect(effect);
        if (top == null) return;
        List<MobEffectInstance> chain = new ArrayList<>();
        for (MobEffectInstance i = top; i != null; i = ((MobEffectInstanceAccessor) i).solrp$getHidden()) chain.add(i);
        List<MobEffectInstance> keep = new ArrayList<>();
        for (MobEffectInstance i : chain) if (!isOurs(i, level)) keep.add(i);
        if (keep.size() == chain.size()) return; // case B, or nothing of ours left
        data.suppressRemoveEvent = true;
        try {
            player.removeEffect(effect);
        } finally {
            data.suppressRemoveEvent = false;
        }
        if (keep.isEmpty()) return;
        MobEffectInstance head = new MobEffectInstance(keep.get(0));
        MobEffectInstance cur = head;
        for (int i = 1; i < keep.size(); i++) {
            MobEffectInstance next = new MobEffectInstance(keep.get(i));
            ((MobEffectInstanceAccessor) cur).solrp$setHidden(next);
            cur = next;
        }
        player.addEffect(head);
    }

    private EffectApplier() {}
}
