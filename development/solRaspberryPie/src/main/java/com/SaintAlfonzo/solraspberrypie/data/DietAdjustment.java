// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.data;

/**
 * The "Adjustment" entry of the food window ({@code /solrp diversity set}): one Apple Pie food entry of the hidden
 * {@code solraspberrypie:diet_adjustment} item whose diversity value is carried on the entry itself, not looked up.
 * Implemented on Apple Pie's {@code FoodInstance} by {@code FoodInstanceMixin}; saved and synced with the food list
 * (NBT key {@code solrp_adjust}).
 */
public interface DietAdjustment {
	/** The entry's own diversity value, or null for a normal food. */
	Double solrp$value();

	void solrp$setValue(Double value);
}
