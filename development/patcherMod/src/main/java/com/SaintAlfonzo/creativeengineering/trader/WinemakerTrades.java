package com.SaintAlfonzo.creativeengineering.trader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mojang.logging.LogUtils;

import net.minecraft.world.entity.npc.VillagerTrades;

import org.slf4j.Logger;

/**
 * Extra listings for Vinery's Wandering Winemaker. Its pool is the public static map
 * {@code WanderingWinemakerEntity.TRADES} (key 1, read by its own {@code updateTrades}, which picks
 * 8); this keeps Vinery's original array and puts back original + extras on every change, so a
 * /reload that re-runs the KubeJS script (reset, then add) never stacks copies. Reflection keeps
 * patcherMod free of a compile dependency on Vinery; without Vinery every call is a no-op.
 */
public final class WinemakerTrades {

    private static final Logger LOG = LogUtils.getLogger();
    /** The Vinery backport's class first, then 1.3.10's. */
    private static final String[] CLASSES = {"net.satisfy.vinery.core.entity.WanderingWinemakerEntity", "satisfyu.vinery.entity.WanderingWinemakerEntity"};

    private static VillagerTrades.ItemListing[] original;
    private static final List<VillagerTrades.ItemListing> extra = new ArrayList<>();

    private WinemakerTrades() {}

    public static synchronized void reset() {
        extra.clear();
        apply();
    }

    public static synchronized boolean add(VillagerTrades.ItemListing listing) {
        if (listing == null) return false;
        extra.add(listing);
        return apply();
    }

    /** Listings in the Winemaker's pool right now (original + extras), 0 without Vinery. */
    public static synchronized int size() {
        Map<Integer, VillagerTrades.ItemListing[]> trades = trades();
        VillagerTrades.ItemListing[] arr = trades == null ? null : trades.get(1);
        return arr == null ? 0 : arr.length;
    }

    private static boolean apply() {
        Map<Integer, VillagerTrades.ItemListing[]> trades = trades();
        if (trades == null) return false;
        if (original == null) original = trades.get(1);
        if (original == null) return false;
        VillagerTrades.ItemListing[] arr = Arrays.copyOf(original, original.length + extra.size());
        for (int k = 0; k < extra.size(); k++) arr[original.length + k] = extra.get(k);
        trades.put(1, arr);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, VillagerTrades.ItemListing[]> trades() {
        Throwable last = null;
        for (String name : CLASSES) {
            try {
                return (Map<Integer, VillagerTrades.ItemListing[]>) Class.forName(name).getField("TRADES").get(null);
            } catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
                last = e;
            }
        }
        LOG.warn("[trades] Wandering Winemaker pool not reachable ({}); Vinery missing or changed", String.valueOf(last));
        return null;
    }
}
