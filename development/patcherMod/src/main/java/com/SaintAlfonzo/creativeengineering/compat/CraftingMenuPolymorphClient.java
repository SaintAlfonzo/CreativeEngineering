package com.SaintAlfonzo.creativeengineering.compat;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.recipe.widget.PlayerRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

/**
 * Polymorph selection widget on the menus patched by {@link CraftingMenuPolymorphCompat} (client
 * half). Polymorph's fallback widget would find their result slot too, but puts the button above it:
 * on the Crafting Terminal that covers the grid's bottom row, on the Engineer's Crafting Table the
 * title. Both result slots sit in a 26px frame with free space to its right, so the button goes there.
 */
public final class CraftingMenuPolymorphClient {

    private static final int BUTTON_X_FROM_SLOT = 23;

    private CraftingMenuPolymorphClient() {}

    public static void register() {
        PolymorphApi.client().registerWidget(screen ->
                screen.getMenu() instanceof CraftingMenuPolymorphCompat.PolymorphCraftingMenu menu
                        ? new SideButtonWidget(screen, menu.creativeengineering$resultSlot()) : null);
    }

    private static final class SideButtonWidget extends PlayerRecipesWidget {

        SideButtonWidget(AbstractContainerScreen<?> screen, Slot outputSlot) {
            super(screen, outputSlot);
        }

        @Override
        public int getXPos() {
            return getOutputSlot().x + BUTTON_X_FROM_SLOT;
        }

        @Override
        public int getYPos() {
            return getOutputSlot().y;
        }
    }
}
