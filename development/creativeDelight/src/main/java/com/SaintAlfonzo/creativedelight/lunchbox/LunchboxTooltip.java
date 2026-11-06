package com.SaintAlfonzo.creativedelight.lunchbox;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** "Slots: 9" under the name of every lunchbox. */
public final class LunchboxTooltip {

    private LunchboxTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!Lunchboxes.isLunchbox(stack)) return;
        int slots = Lunchboxes.slots(stack);
        if (slots <= 0) return;
        int at = Math.min(1, event.getToolTip().size());
        event.getToolTip().add(at, Component.translatable("creativeengineering.lunchbox.slots", slots)
                .withStyle(ChatFormatting.GRAY));
    }
}
