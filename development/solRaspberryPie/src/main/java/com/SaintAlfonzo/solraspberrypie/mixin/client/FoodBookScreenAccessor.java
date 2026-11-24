// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import com.tarinoita.solsweetpotato.client.gui.elements.UIImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Apple Pie's page list and book background. Pages are plain UIElements, so Raspberry Pie can insert its own. */
@Mixin(value = FoodBookScreen.class, remap = false)
public interface FoodBookScreenAccessor {
	@SuppressWarnings("rawtypes")
	@Accessor("pages")
	List solrp$pages();

	@Accessor("background")
	UIImage solrp$background();
}
