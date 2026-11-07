package com.SaintAlfonzo.creativecompat.compat;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * AE2 (12.9.12) draws most of its own tooltips through {@code AEBaseScreen.drawTooltip(pose, x, y, lines)}, which
 * wraps the lines and calls the stackless {@code renderTooltip(pose, List<FormattedCharSequence>, x, y)}. Forge
 * then fires {@code RenderTooltipEvent.GatherComponents} with {@code ItemStack.EMPTY}, so AppleSkin adds no food
 * bars to crafting CPU status / crafting plan entries or pattern-encoding amount slots.
 *
 * <p>Those callers set the item they describe with {@link #setPending} just before drawing; the
 * {@code drawTooltip} hook {@link #take}s it and draws the same lines with Forge's stack overload. Every other
 * {@code drawTooltip} (buttons, widgets) finds nothing pending and draws as before.
 */
public final class AeTooltipStacks {

    private static ItemStack pending = ItemStack.EMPTY;

    private AeTooltipStacks() {}

    public static void setPending(ItemStack stack) {
        pending = stack;
    }

    /** The item an AE2 stack stands for; fluids and other keys give EMPTY. */
    public static ItemStack itemOf(@Nullable GenericStack stack) {
        return stack != null && stack.what() instanceof AEItemKey itemKey ? itemKey.toStack() : ItemStack.EMPTY;
    }

    public static ItemStack take() {
        ItemStack stack = pending;
        pending = ItemStack.EMPTY;
        return stack;
    }
}
