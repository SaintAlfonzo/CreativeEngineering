package com.SaintAlfonzo.solraspberrypie.data;

import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * The last meals eaten, newest first, duplicates kept (plan_diet_books.md 1.2): what the Diet Log's "Last meals" page
 * lists in place of Apple Pie's food history. Kept on Apple Pie's FoodList by FoodListMixin (NBT key
 * {@code solrp_meals}), so it is saved and synced with Apple Pie's own data. Length = Apple Pie's {@code endDecay}.
 */
public interface MealLog {
	List<ResourceLocation> solrp$meals();

	static List<ResourceLocation> of(FoodList list) {
		return new ArrayList<>(((MealLog) (Object) list).solrp$meals());
	}

	/** How many meals the log keeps: the decay window. */
	static int length() {
		try {
			return Math.max(1, SOLSweetPotatoConfig.endDecay());
		} catch (RuntimeException e) { // Apple Pie's server config not loaded yet (client before login)
			return 30;
		}
	}
}
