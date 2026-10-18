package com.SaintAlfonzo.creativeengineering.oven.client;

import java.util.ArrayList;
import java.util.List;

import com.SaintAlfonzo.creativeengineering.oven.OvenBlockEntity;
import com.SaintAlfonzo.creativeengineering.oven.OvenMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Draws Farmer's Delight's own cooking pot GUI texture by resource location (FD is installed, nothing is copied) and
 * one extra slot frame, cut from the same texture, for the tool slot. Heat flame, progress arrow and the "served on"
 * tooltip work as in FD's CookingPotScreen.
 */
public class OvenScreen extends AbstractContainerScreen<OvenMenu> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation("farmersdelight", "textures/gui/cooking_pot.png");
    // FD texture regions
    private static final int HEAT_X = 47, HEAT_Y = 55, HEAT_W = 17, HEAT_H = 15;
    public static final int ARROW_X = 89, ARROW_Y = 25, ARROW_W = 24, ARROW_H = 17;
    private static final int SLOT_FRAME_U = 91, SLOT_FRAME_V = 54;

    public OvenScreen(OvenMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 28;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderSlotTooltips(poseStack, mouseX, mouseY);
        if (isHovering(HEAT_X, HEAT_Y, HEAT_W, HEAT_H, mouseX, mouseY)) {
            String key = "container.creativeengineering.oven." + (menu.isHeated() ? "heated" : "not_heated");
            renderTooltip(poseStack, Component.translatable(key), mouseX, mouseY);
        }
    }

    @SuppressWarnings("deprecation") // Rarity.color, as FD's CookingPotScreen
    private void renderSlotTooltips(PoseStack poseStack, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null || !menu.getCarried().isEmpty() || hoveredSlot == null || !hoveredSlot.hasItem()) {
            return;
        }
        if (hoveredSlot.index == OvenBlockEntity.MEAL_DISPLAY_SLOT) {
            ItemStack mealStack = hoveredSlot.getItem();
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(mealStack.getHoverName().copy().withStyle(mealStack.getRarity().color));
            ItemStack containerStack = menu.blockEntity.getContainer();
            String container = containerStack.isEmpty() ? "" : containerStack.getHoverName().getString();
            tooltip.add(Component.translatable("container.creativeengineering.oven.served_on", container).withStyle(ChatFormatting.GRAY));
            renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
        } else {
            renderTooltip(poseStack, hoveredSlot.getItem(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // Tool slot frame (the container slot's frame, moved left of the ingredient grid)
        blit(poseStack, leftPos + OvenMenu.TOOL_X - 1, topPos + OvenMenu.TOOL_Y - 1, SLOT_FRAME_U, SLOT_FRAME_V, 18, 18);
        if (menu.isHeated()) {
            blit(poseStack, leftPos + HEAT_X, topPos + HEAT_Y, 176, 0, HEAT_W, HEAT_H);
        }
        int progress = menu.getCookProgressionScaled();
        blit(poseStack, leftPos + ARROW_X, topPos + ARROW_Y, 176, 15, progress + 1, ARROW_H);
    }
}
