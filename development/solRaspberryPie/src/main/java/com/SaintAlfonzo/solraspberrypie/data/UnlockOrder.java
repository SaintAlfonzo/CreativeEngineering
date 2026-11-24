// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.data;

import com.cazsius.solcarrot.tracking.FoodList;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * The order of the player's Food Dictionary unlocks (plan_diet_books.md 2.3), oldest first. Kept on Spice of Life:
 * Carrot Edition's FoodList by CarrotFoodListMixin (NBT key {@code solrp_unlockOrder}), so it is saved and synced with
 * Carrot's own list. Foods unlocked before this existed have no place in it.
 */
public interface UnlockOrder {
	List<ResourceLocation> solrp$unlockOrder();

	static List<ResourceLocation> of(FoodList list) {
		return new ArrayList<>(((UnlockOrder) (Object) list).solrp$unlockOrder());
	}
}
