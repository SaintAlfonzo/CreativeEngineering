package com.SaintAlfonzo.creativedelight.roll.client;

import com.SaintAlfonzo.creativedelight.roll.logic.Chopsticks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/**
 * The chopstick line on every chopstick food (roll_delight.md 3.3): the longer wording when the food has an effect that
 * chopsticks lengthen (read from the food at tooltip time, so it follows later value changes).
 * <p>
 * Added once per tooltip (plan_diet_books_r2.md B3): when the tooltip event runs a second time over the same list,
 * a second chopstick line would sit after Mod Name Tooltip's line, whose duplicate check only looks at the last line,
 * so the mod name was added again.
 */
public final class RollClient {
	private static final String KEY = "tooltip.roll_delight.chopstick_food";

	private RollClient() {
	}

	public static void register() {
		MinecraftForge.EVENT_BUS.addListener(RollClient::onTooltip);
	}

	private static void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (!stack.is(Chopsticks.FOODS)) return;
		FoodProperties food = stack.getFoodProperties(event.getEntity());
		boolean lengthens = food != null && food.getEffects().stream().anyMatch(e -> e.getFirst() != null && !e.getFirst().getEffect().isInstantenous());
		for (Component line : event.getToolTip()) {
			if (line.getContents() instanceof TranslatableContents t && t.getKey().startsWith(KEY)) return;
		}
		event.getToolTip().add(Component.translatable(lengthens ? KEY + ".effects" : KEY).withStyle(ChatFormatting.GRAY));
	}
}
