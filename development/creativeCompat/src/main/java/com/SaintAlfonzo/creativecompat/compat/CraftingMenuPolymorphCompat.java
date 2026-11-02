package com.SaintAlfonzo.creativecompat.compat;

import net.minecraft.world.inventory.Slot;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Polymorph recipe selection for modded 3x3 crafting menus that look their recipe up themselves
 * (server half): the IE Engineer's Crafting Table ({@code mixin.IECraftingTablePolymorphMixin}) and
 * the Pretty Pipes Crafting Terminal ({@code mixin.PrettyPipesCraftingTerminalPolymorphMixin}).
 * The mixins send that lookup to Polymorph's player recipe data, as Polymorph does for the vanilla
 * crafting table. Both menus recompute in {@code slotsChanged}, which Polymorph's selection packet
 * already calls, so a pick shows at once.
 * <p>
 * Polymorph only sends the recipe list for the player's open menu, and a menu's first result can be
 * worked out while it is built (the Crafting Terminal's grid is kept in the block), before it is the
 * open menu. So the result is recomputed once more when the menu opens ({@link #onContainerOpen}).
 * The client half is {@link CraftingMenuPolymorphClient}. Registered only when Polymorph is loaded.
 */
public final class CraftingMenuPolymorphCompat {

    private CraftingMenuPolymorphCompat() {}

    /** Implemented on the patched menus by their mixins. */
    public interface PolymorphCraftingMenu {
        void creativeengineering$refreshCraftingResult();

        Slot creativeengineering$resultSlot();
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof PolymorphCraftingMenu menu) {
            menu.creativeengineering$refreshCraftingResult();
        }
    }
}
