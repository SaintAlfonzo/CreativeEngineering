package com.SaintAlfonzo.creativeengineering.compat;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.base.ITickingRecipesWidget;
import com.illusivesoulworks.polymorph.client.recipe.widget.PlayerRecipesWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;

import javax.annotation.Nullable;

/**
 * Polymorph selection widget on Sophisticated storage screens (client half of
 * {@link SophisticatedPolymorphCompat}). Polymorph places its widget once, when the screen opens, from
 * a fixed output slot. The crafting upgrade's result slot only moves on screen when its tab is opened
 * (it sits at -100,-100 otherwise), and a backpack can hold several crafting upgrades. So this widget
 * follows the open crafting tab's result slot every tick and hides while no crafting tab is open.
 * The button sits just right of the result slot frame, inside the tab; Polymorph's default spot (above
 * the slot) would cover the bottom row of the grid.
 */
public final class SophisticatedPolymorphClient {

    private static final int RESULT_SLOT_INDEX = 9;
    private static final int BUTTON_X_FROM_SLOT = 22;

    private SophisticatedPolymorphClient() {}

    public static void register() {
        PolymorphApi.client().registerWidget(screen ->
                screen.getMenu() instanceof StorageContainerMenuBase<?> menu ? new CraftingTabWidget(screen, menu) : null);
    }

    private static final class CraftingTabWidget extends PlayerRecipesWidget implements ITickingRecipesWidget {

        private final StorageContainerMenuBase<?> menu;
        private final Slot hiddenSlot = new Slot(new SimpleContainer(1), 0, -100, -100);
        private int lastX = Integer.MIN_VALUE;
        private int lastY = Integer.MIN_VALUE;

        CraftingTabWidget(AbstractContainerScreen<?> screen, StorageContainerMenuBase<?> menu) {
            super(screen, null);
            this.menu = menu;
        }

        @Nullable
        private Slot openResultSlot() {
            return menu.getOpenContainer()
                    .filter(container -> container instanceof CraftingUpgradeContainer)
                    .map(container -> container.getSlots().get(RESULT_SLOT_INDEX))
                    .orElse(null);
        }

        @Override
        public Slot getOutputSlot() {
            Slot slot = openResultSlot();
            return slot != null ? slot : hiddenSlot;
        }

        @Override
        public int getXPos() {
            return getOutputSlot().x + BUTTON_X_FROM_SLOT;
        }

        @Override
        public int getYPos() {
            return getOutputSlot().y;
        }

        @Override
        public void tick() {
            Slot slot = openResultSlot();
            if (slot == null) {
                selectionWidget.setActive(false);
                return;
            }
            if (slot.x != lastX || slot.y != lastY) {
                lastX = slot.x;
                lastY = slot.y;
                resetWidgetOffsets();
            }
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            if (openResultSlot() != null) {
                super.render(poseStack, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return openResultSlot() != null && super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
