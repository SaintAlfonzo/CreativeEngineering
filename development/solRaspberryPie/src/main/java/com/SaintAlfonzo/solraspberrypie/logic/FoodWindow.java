// SPDX-License-Identifier: GPL-3.0-only
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
 * Ageing foods out of the decay window (6). An aged-out food is removed from Apple Pie's food memory: it counts nothing
 * until eaten again, and the Diet Log shows it no more (raspberry-pie-publish #26). Nothing extra is stored.
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

	/** Ages the given foods out in place (6.1): removes them from the memory. */
	public static void ageOut(Map<FoodInstance, Integer> memory, Collection<FoodInstance> foods) {
		for (FoodInstance f : foods) memory.remove(f);
	}

	public static Map<FoodInstance, Integer> copy(FoodList list) {
		return new HashMap<>(memory(list));
	}

	public static double diversity(Map<FoodInstance, Integer> memory) {
		return FoodList.foodDiversity(memory.entrySet());
	}

	private FoodWindow() {}
}
