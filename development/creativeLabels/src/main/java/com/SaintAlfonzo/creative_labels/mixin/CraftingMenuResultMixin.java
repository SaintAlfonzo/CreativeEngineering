package com.SaintAlfonzo.creative_labels.mixin;

import com.SaintAlfonzo.creative_labels.CLConfig;
import com.SaintAlfonzo.creative_labels.item.ItemLabels;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Labelled items can't be crafted with (sticky_notes.md 3.8, Q6): while {@code blockLabelledCrafting} is on, a grid
 * holding any stack with {@code CreativeLabels.Lines} gives no result. {@code slotChangedCraftingGrid} is the shared
 * result update of the crafting table and the inventory grid (both menus call this static); it only does work on
 * the server, so the head injection cancels there and clears the result slot the way vanilla does when nothing
 * matches. Machines are not covered.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuResultMixin {

    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private static void creative_labels$blockLabelled(AbstractContainerMenu menu, Level level, Player player,
                                                      CraftingContainer grid, ResultContainer result, CallbackInfo ci) {
        if (level.isClientSide || !CLConfig.on(CLConfig.BLOCK_LABELLED_CRAFTING)) return;
        boolean labelled = false;
        for (int i = 0; i < grid.getContainerSize() && !labelled; i++) {
            labelled = ItemLabels.has(grid.getItem(i));
        }
        if (!labelled) return;
        result.setItem(0, ItemStack.EMPTY);
        if (player instanceof ServerPlayer server) {
            server.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, ItemStack.EMPTY));
        }
        ci.cancel();
    }
}
