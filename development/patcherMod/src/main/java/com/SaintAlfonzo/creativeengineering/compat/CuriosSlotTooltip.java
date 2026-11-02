package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Every item that fits a Curios slot names its slots (ui/tooltips #8, feature_writeup/curios_slots.md 1).
 *
 * <p>Curios 5.1.6.4 ({@code client/ClientEventHandler.onTooltip}) already builds "Slot: Face, Head" (gold, slots
 * yellow) from {@code getCurioTags(item)}, the {@code curios:<slot>} item tags plus mixins such as Simple Hats' head
 * rule. But an item with a curio capability hands that line to its {@code ICurio.getSlotsTooltip}, and a mod that
 * overrides it with an empty list (or its own text) hides the slots. This listener runs after Curios (LOW) and adds
 * the same line, in Curios' own format and position (under the name), to any item whose tooltip has no
 * {@code curios.slot} line yet. Client only, registered when Curios is loaded; {@code [tooltips] curiosSlotLine}.
 */
public final class CuriosSlotTooltip {

    private static final String SLOT_KEY = "curios.slot";

    private CuriosSlotTooltip() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltip(ItemTooltipEvent event) {
        if (!CEConfig.TOOLTIPS.curiosSlotLine.get()) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        Set<String> slots = CuriosApi.getCuriosHelper().getCurioTags(stack.getItem());
        if (slots.isEmpty()) return;
        List<Component> tooltip = event.getToolTip();
        for (Component line : tooltip) {
            if (line.getContents() instanceof TranslatableContents t && SLOT_KEY.equals(t.getKey())) return;
        }
        tooltip.add(Math.min(1, tooltip.size()), line(new ArrayList<>(slots)));
    }

    /** Curios' own line: "Slot:" gold, then the slot names in yellow, comma separated. */
    private static Component line(List<String> slots) {
        MutableComponent out = Component.translatable(SLOT_KEY).append(": ").withStyle(ChatFormatting.GOLD);
        for (int i = 0; i < slots.size(); i++) {
            MutableComponent name = Component.translatable("curios.identifier." + slots.get(i));
            if (i < slots.size() - 1) name = name.append(", ");
            out.append(name.withStyle(ChatFormatting.YELLOW));
        }
        return out;
    }
}
