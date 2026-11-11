package com.SaintAlfonzo.creativelabels.crafting;

import com.SaintAlfonzo.creativelabels.CLConfig;
import com.SaintAlfonzo.creativelabels.item.ItemLabels;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * The shared half of {@code blockLabelledCrafting} (sticky_notes.md 3.8, Q6): whether a grid holds a labelled stack,
 * and the "no result" answer both result-update paths give then (vanilla's {@code CraftingMenu.slotChangedCraftingGrid}
 * in {@code CraftingMenuResultMixin}, FastWorkbench's own copy in {@code FastBenchResultMixin}). Outside the mixin
 * package: Mixin refuses to load ordinary classes from there (a crash on the first craft, scratch server 2026-10-21).
 */
public final class LabelledCrafting {

    private LabelledCrafting() {}

    /** Server side, switch on, and some stack in the grid has label lines. */
    public static boolean blocks(Level level, Container grid) {
        if (level.isClientSide || !CLConfig.on(CLConfig.BLOCK_LABELLED_CRAFTING)) return false;
        for (int i = 0; i < grid.getContainerSize(); i++) {
            if (ItemLabels.has(grid.getItem(i))) return true;
        }
        return false;
    }

    /**
     * Empties the result and tells the client: through {@code menu} when the caller knows it, else through the
     * player's open menu when one of its slots shows {@code result} (FastWorkbench's path passes no menu; its own
     * result packet is the only other way the client learns the result, so without this it would keep the old one).
     */
    public static void clear(@Nullable AbstractContainerMenu menu, Player player, ResultContainer result) {
        result.setItem(0, ItemStack.EMPTY);
        if (!(player instanceof ServerPlayer server)) return;
        AbstractContainerMenu m = menu != null ? menu : player.containerMenu;
        for (int i = 0; i < m.slots.size(); i++) {
            if (m.slots.get(i).container == result) {
                server.connection.send(new ClientboundContainerSetSlotPacket(m.containerId, m.incrementStateId(), i, ItemStack.EMPTY));
                return;
            }
        }
    }
}
