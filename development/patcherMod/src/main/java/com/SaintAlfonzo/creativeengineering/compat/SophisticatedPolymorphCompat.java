package com.SaintAlfonzo.creativeengineering.compat;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;

/**
 * Polymorph recipe selection for the Sophisticated crafting upgrade (server half).
 * {@code mixin.SophisticatedCraftingPolymorphMixin} makes the upgrade ask Polymorph for its recipe,
 * as Polymorph does for the vanilla crafting table. The upgrade only recomputes its result when its
 * grid changes, so it is also recomputed here when Polymorph needs a fresh answer or a fresh recipe
 * list sent to the client:
 * <ul>
 *   <li>after a selection packet ({@code mixin.PolymorphRecipeSelectionPacketMixin});</li>
 *   <li>when a crafting tab is opened ({@code mixin.SophisticatedUpgradeOpenMixin});</li>
 *   <li>when the storage menu is opened ({@link #onContainerOpen}). The upgrade's own first result
 *       is worked out while the menu is built, before it is the player's open menu, and Polymorph
 *       only sends the recipe list for the open menu.</li>
 * </ul>
 * The client half is {@link SophisticatedPolymorphClient}. Registered only when Polymorph is loaded.
 */
public final class SophisticatedPolymorphCompat {

    private SophisticatedPolymorphCompat() {}

    /** Implemented on {@code CraftingUpgradeContainer} by the mixin. */
    public interface RefreshableCrafting {
        void creativeengineering$refreshCraftingResult();
    }

    public static void refreshOpenCrafting(AbstractContainerMenu menu) {
        if (menu instanceof StorageContainerMenuBase<?> storageMenu) {
            storageMenu.getOpenContainer().ifPresent(container -> {
                if (container instanceof RefreshableCrafting crafting) {
                    crafting.creativeengineering$refreshCraftingResult();
                }
            });
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        refreshOpenCrafting(event.getContainer());
    }
}
