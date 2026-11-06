package com.SaintAlfonzo.creativecommon.compat;

import com.google.common.collect.MapMaker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Remembers which ItemStack each Lightman's Currency item-tooltip line came from, so the tooltip can be
 * drawn with its stack and AppleSkin (or anything else reading {@code RenderTooltipEvent.GatherComponents
 * #getItemStack}) sees the item.
 *
 * <p>LC builds item tooltips with {@code EasyScreenHelper.getTooltipFromItem(stack)} and passes them around
 * as plain {@code List<Component>}: trade buttons copy them into new lists with price and info lines,
 * storage tabs append counts. The line objects themselves survive those copies, so they are tagged here
 * ({@code LCEasyScreenHelperMixin}) and looked up when {@code EasyGuiGraphics.renderComponentTooltip}
 * draws the list ({@code LCEasyGuiGraphicsMixin}). Keys are weak and compared by identity: a line only
 * matches the exact object {@code getTooltipLines} produced, never an equal-looking line from elsewhere.
 */
public final class LightmansTooltipStacks {

    private static final Map<Component, ItemStack> SOURCES = new MapMaker().weakKeys().makeMap();

    private LightmansTooltipStacks() {}

    public static void tag(List<Component> lines, ItemStack stack) {
        if (stack.isEmpty()) return;
        for (Component line : lines) {
            SOURCES.put(line, stack);
        }
    }

    /** The stack behind the first tagged line, or {@link ItemStack#EMPTY} if the list has none. */
    public static ItemStack find(List<? extends Component> lines) {
        for (Component line : lines) {
            ItemStack stack = SOURCES.get(line);
            if (stack != null) return stack;
        }
        return ItemStack.EMPTY;
    }
}
