// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.food;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * "Can be eaten when full" on every food that is always edible (food-tuning #62; owner ruling F6 assumption 3):
 * the stack's own food properties for the viewing player, so foods made always edible at runtime (KubeJS) show it too.
 * One gray line after the name. Client only in effect (the event fires on the client).
 */
public final class AlwaysEdibleTooltip {

	private AlwaysEdibleTooltip() {}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (!stack.isEdible()) return;
		FoodProperties food = stack.getFoodProperties(event.getEntity());
		if (food == null || !food.canAlwaysEat()) return;
		int at = Math.min(1, event.getToolTip().size());
		event.getToolTip().add(at, Component.translatable("tooltip.creativedelight.always_edible").withStyle(ChatFormatting.GRAY));
	}
}
