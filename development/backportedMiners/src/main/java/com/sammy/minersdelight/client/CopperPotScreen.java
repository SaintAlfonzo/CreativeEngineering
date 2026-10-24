package com.sammy.minersdelight.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.util.List;

import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.MEAL_DISPLAY_SLOT;

/** The Copper Pot screen on the upstream {@code textures/gui/copper_pot.png}, with FD's cooking recipe book. */
public class CopperPotScreen extends AbstractContainerScreen<CopperPotMenu> implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON = new ResourceLocation("textures/gui/recipe_button.png");
    private static final ResourceLocation BACKGROUND = MinersDelightMod.path("textures/gui/copper_pot.png");
    // x, y, width, height on the screen; the sprites sit at u 176 (heat at v 0, arrow at v 15).
    private static final int HEAT_X = 49, HEAT_Y = 57, HEAT_W = 17, HEAT_H = 15;
    private static final int ARROW_X = 78, ARROW_Y = 27, ARROW_H = 17;

    private final CopperPotRecipeBookComponent recipeBook = new CopperPotRecipeBookComponent();
    private boolean widthTooNarrow;

    public CopperPotScreen(CopperPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        widthTooNarrow = width < 379;
        titleLabelX = 28;
        recipeBook.init(width, height, minecraft, widthTooNarrow, menu);
        leftPos = recipeBook.updateScreenPosition(width, imageWidth);
        if (Configuration.ENABLE_RECIPE_BOOK_COOKING_POT.get()) {
            addRenderableWidget(new ImageButton(leftPos + 5, height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON, button -> {
                recipeBook.toggleVisibility();
                leftPos = recipeBook.updateScreenPosition(width, imageWidth);
                ((ImageButton) button).setPosition(leftPos + 5, height / 2 - 49);
            }));
        } else {
            recipeBook.hide();
            leftPos = recipeBook.updateScreenPosition(width, imageWidth);
        }
        addWidget(recipeBook);
        setInitialFocus(recipeBook);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        recipeBook.tick();
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        renderBackground(pose);
        if (recipeBook.isVisible() && widthTooNarrow) {
            renderBg(pose, partialTicks, mouseX, mouseY);
            recipeBook.render(pose, mouseX, mouseY, partialTicks);
        } else {
            recipeBook.render(pose, mouseX, mouseY, partialTicks);
            super.render(pose, mouseX, mouseY, partialTicks);
            recipeBook.renderGhostRecipe(pose, leftPos, topPos, false, partialTicks);
        }
        renderSlotTooltip(pose, mouseX, mouseY);
        if (isHovering(HEAT_X, HEAT_Y, HEAT_W, HEAT_H, mouseX, mouseY)) {
            String key = "container.cooking_pot." + (menu.isHeated() ? "heated" : "not_heated");
            renderComponentTooltip(pose, List.of(TextUtils.getTranslation(key, menu)), mouseX, mouseY);
        }
        recipeBook.renderTooltip(pose, leftPos, topPos, mouseX, mouseY);
    }

    /** The meal display names its container ("Served on"); other slots show the normal tooltip. */
    private void renderSlotTooltip(PoseStack pose, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null || !menu.getCarried().isEmpty() || hoveredSlot == null || !hoveredSlot.hasItem()) {
            return;
        }
        if (hoveredSlot.index != MEAL_DISPLAY_SLOT) {
            renderTooltip(pose, hoveredSlot.getItem(), mouseX, mouseY);
            return;
        }
        ItemStack meal = hoveredSlot.getItem();
        ItemStack container = menu.blockEntity.getContainer();
        String containerName = container.isEmpty() ? "" : container.getItem().getDescription().getString();
        renderComponentTooltip(pose, List.of(
                meal.getItem().getDescription().copy().withStyle(meal.getRarity().color),
                TextUtils.getTranslation("container.cooking_pot.served_on", containerName).withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack pose, int mouseX, int mouseY) {
        super.renderLabels(pose, mouseX, mouseY);
        font.draw(pose, playerInventoryTitle, 8.0F, imageHeight - 96 + 2, 4210752);
    }

    @Override
    protected void renderBg(@NotNull PoseStack pose, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isHeated()) {
            blit(pose, leftPos + HEAT_X, topPos + HEAT_Y, 176, 0, HEAT_W, HEAT_H);
        }
        blit(pose, leftPos + ARROW_X, topPos + ARROW_Y, 176, 15, menu.getCookProgressionScaled() + 1, ARROW_H);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!widthTooNarrow || !recipeBook.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (recipeBook.mouseClicked(mouseX, mouseY, button)) {
            setFocused(recipeBook);
            return true;
        }
        return widthTooNarrow && recipeBook.isVisible() || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int x, int y, int button) {
        boolean outside = mouseX < x || mouseY < y || mouseX >= x + imageWidth || mouseY >= y + imageHeight;
        return outside && recipeBook.hasClickedOutside(mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, button);
    }

    @Override
    protected void slotClicked(@NotNull Slot slot, int mouseX, int mouseY, @NotNull ClickType type) {
        super.slotClicked(slot, mouseX, mouseY, type);
        recipeBook.slotClicked(slot);
    }

    @Override
    public void recipesUpdated() {
        recipeBook.recipesUpdated();
    }

    @Override
    public void removed() {
        recipeBook.removed();
        super.removed();
    }

    @Override
    @NotNull
    public RecipeBookComponent getRecipeBookComponent() {
        return recipeBook;
    }
}
