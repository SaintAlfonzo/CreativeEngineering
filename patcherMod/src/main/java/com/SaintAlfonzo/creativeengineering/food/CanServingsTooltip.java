package com.SaintAlfonzo.creativeengineering.food;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** "Servings: 3 / 4" under the name of every can that holds more than one ({@link CanServings}). */
public final class CanServingsTooltip {

    private CanServingsTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        int max = CanServings.maxServings(stack);
        if (max <= 1) return;
        int at = Math.min(1, event.getToolTip().size());
        event.getToolTip().add(at, Component.translatable("creativeengineering.can.servings",
                CanServings.servingsLeft(stack), max).withStyle(ChatFormatting.GRAY));
    }
}
