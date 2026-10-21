package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.mixin.FtbTooltipListAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

/**
 * FTB Library (1902.4.1) screens collect hover text in a {@code TooltipList} and draw it with their own text
 * renderer, which never fires {@code RenderTooltipEvent.GatherComponents}, so AppleSkin adds no food bars to
 * FTB Quests task, reward and description item hovers. Only a widget with nothing to say falls back to
 * vanilla's stack tooltip.
 *
 * <p>The places that copy an item's tooltip into that text ({@code GuiHelper.addStackTooltip}, the item task
 * header, item hover events in quest descriptions) {@link #remember} the stack; the screen wrappers forget it at
 * the start of each frame and, when one was remembered, draw the same lines with Forge's
 * {@code renderComponentTooltip(..., stack)} instead. Text-only hovers (quest titles, buttons) keep FTB's renderer.
 */
public final class FtbTooltipStacks {

    private static ItemStack stack = ItemStack.EMPTY;

    private FtbTooltipStacks() {}

    public static void remember(ItemStack itemStack) {
        stack = itemStack;
    }

    public static void forget() {
        stack = ItemStack.EMPTY;
    }

    /** Stands in for {@code TooltipList.render}: same arguments, same offsets as the item-only branch. */
    public static void render(Screen screen, TooltipList list, PoseStack poseStack, int mouseX, int mouseY,
                              int screenWidth, int screenHeight, Font font) {
        ItemStack itemStack = stack;
        stack = ItemStack.EMPTY;
        if (itemStack.isEmpty()) {
            list.render(poseStack, mouseX, mouseY, screenWidth, screenHeight, font);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, list.zOffsetItemTooltip);
        screen.renderComponentTooltip(poseStack, ((FtbTooltipListAccessor) list).creativeengineering$getLines(),
                mouseX + list.xOffset, mouseY + list.yOffset, font, itemStack);
        poseStack.popPose();
    }
}
