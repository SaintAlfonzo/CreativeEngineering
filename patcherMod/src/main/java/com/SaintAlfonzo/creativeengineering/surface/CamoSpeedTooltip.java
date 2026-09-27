package com.SaintAlfonzo.creativeengineering.surface;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Rewrites BlockRunner's "Speed Multiplier: X" tooltip line on camouflage blocks to say how their
 * speed really works ({@link CamoSpeed#tooltipLine}). Runs after BlockRunner's own listener
 * (LOW priority) and only replaces its line, so turning BlockRunner's tooltip off hides this too.
 */
public final class CamoSpeedTooltip {

    private CamoSpeedTooltip() {}

    private static final String BLOCKRUNNER_KEY = "block.blockrunner.speedMultiplier";

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof BlockItem item)
                || !item.getBlock().defaultBlockState().is(CamoSpeed.TAG)) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (line.getContents() instanceof TranslatableContents contents && BLOCKRUNNER_KEY.equals(contents.getKey())) {
                tooltip.set(i, CamoSpeed.tooltipLine(item.getBlock()).withStyle(line.getStyle()));
                return;
            }
        }
    }
}
