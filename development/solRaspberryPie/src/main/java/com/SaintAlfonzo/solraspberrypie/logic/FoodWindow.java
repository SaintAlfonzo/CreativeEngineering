package com.SaintAlfonzo.solraspberrypie.logic;

import com.SaintAlfonzo.solraspberrypie.mixin.FoodListAccessor;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ageing foods out of the decay window (6). A moved food keeps its order relative to the other moved foods: each gets
 * k + (endDecay - smallest k in the group), capped below Apple Pie's memory size, so it counts minContribution and is
 * forgotten by Apple Pie at the usual age. Nothing extra is stored.
 */
public final class FoodWindow {
    public static Map<FoodInstance, Integer> memory(FoodList list) {
        return ((FoodListAccessor) (Object) list).solrp$uniqueFoods();
    }

    public static int endDecay() {
        return SOLSweetPotatoConfig.endDecay();
    }

    /** Foods still in the decay window, oldest first (largest count below endDecay). */
    public static List<FoodInstance> windowOldestFirst(Map<FoodInstance, Integer> memory) {
        int end = endDecay();
        List<Map.Entry<FoodInstance, Integer>> in = new ArrayList<>();
        for (Map.Entry<FoodInstance, Integer> e : memory.entrySet()) if (e.getValue() < end) in.add(e);
        in.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<FoodInstance> out = new ArrayList<>(in.size());
        for (Map.Entry<FoodInstance, Integer> e : in) out.add(e.getKey());
        return out;
    }

    /** Moves the given foods out of the window in place (6.1). */
    public static void moveOut(Map<FoodInstance, Integer> memory, Collection<FoodInstance> foods) {
        if (foods.isEmpty()) return;
        int minK = Integer.MAX_VALUE;
        for (FoodInstance f : foods) minK = Math.min(minK, memory.getOrDefault(f, Integer.MAX_VALUE));
        if (minK == Integer.MAX_VALUE) return;
        int shift = endDecay() - minK;
        int cap = Math.max(endDecay(), SOLSweetPotatoConfig.size() - 1);
        for (FoodInstance f : foods) {
            Integer k = memory.get(f);
            if (k != null) memory.put(f, Math.min(cap, k + shift));
        }
    }

    public static Map<FoodInstance, Integer> copy(FoodList list) {
        return new HashMap<>(memory(list));
    }

    public static double diversity(Map<FoodInstance, Integer> memory) {
        return FoodList.foodDiversity(memory.entrySet());
    }

    private FoodWindow() {}
}
