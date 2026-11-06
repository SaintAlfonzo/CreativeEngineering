package com.SaintAlfonzo.creativedelight.roll.client;

import com.SaintAlfonzo.creativedelight.roll.logic.Chopsticks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/**
 * The chopstick line on every chopstick food (roll_delight.md 3.3): the longer wording when the food has an effect that
 * chopsticks lengthen (read from the food at tooltip time, so it follows later value changes).
 */
public final class RollClient {
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
        event.getToolTip().add(Component.translatable(lengthens ? "tooltip.roll_delight.chopstick_food.effects" : "tooltip.roll_delight.chopstick_food")
                .withStyle(ChatFormatting.GRAY));
    }
}
