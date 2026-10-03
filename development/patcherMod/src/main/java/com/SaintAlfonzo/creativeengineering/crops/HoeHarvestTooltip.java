package com.SaintAlfonzo.creativeengineering.crops;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * "Harvest area: 3x3" under the name of every hoe whose Quark area is bigger than one block, from
 * the same {@link HoeHarvestArea#size} the harvest uses, so it follows the tags and Quark's config.
 */
public final class HoeHarvestTooltip {

    private HoeHarvestTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        int size = HoeHarvestArea.size(event.getItemStack());
        if (size <= 1) return;
        List<Component> tooltip = event.getToolTip();
        int at = Math.min(1, tooltip.size());
        String dims = size + "x" + size;
        tooltip.add(at, Component.translatable(size % 2 == 0
                        ? "creativeengineering.hoe_harvest.area_even" : "creativeengineering.hoe_harvest.area",
                Component.literal(dims).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
        tooltip.add(at + 1, Component.translatable("creativeengineering.hoe_harvest.how")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
