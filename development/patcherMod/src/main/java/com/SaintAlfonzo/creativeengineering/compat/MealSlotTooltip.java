package com.SaintAlfonzo.creativeengineering.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Farmer's Delight's cooking pot (and the screens copied from it: Farmer's Respite kettle, Create Central
 * Kitchen guides) builds the finished-meal slot's tooltip by hand (name + "Served on: ...") and draws it with
 * the stackless {@code renderComponentTooltip(pose, lines, x, y)}. Forge then fires
 * {@code RenderTooltipEvent.GatherComponents} with {@code ItemStack.EMPTY}, so AppleSkin adds no food bars to
 * the one slot that always holds food. The mixins redirect that call here: same lines and position, drawn
 * with Forge's overload that carries the hovered slot's stack.
 */
public final class MealSlotTooltip {

    private MealSlotTooltip() {}

    public static void render(AbstractContainerScreen<?> screen, PoseStack poseStack, List<Component> lines, int x, int y) {
        Slot slot = screen.getSlotUnderMouse();
        screen.renderComponentTooltip(poseStack, lines, x, y, slot != null ? slot.getItem() : ItemStack.EMPTY);
    }
}
