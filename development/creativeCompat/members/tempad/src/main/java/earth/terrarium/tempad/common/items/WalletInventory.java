// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/WalletInventory.kt. Translated to Java for Forge 1.19.2:
// NeoForge ItemStackHandler -> Forge ItemStackHandler; the wallet_contents component -> the stack's `wallet_contents` NBT
// (ModComponents.get/setWalletContents). Also upstream's `ItemStack.items` extension (WalletItem.kt) as `of(stack)`.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.common.menu.WalletMenu;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WalletInventory extends ItemStackHandler implements MenuProvider {
    public static final int SIZE = 18;

    public final ItemStack stack;

    public WalletInventory(ItemStack stack) {
        super(SIZE);
        this.stack = stack;
        List<ItemStack> contents = ModComponents.getWalletContents(stack);
        for (int i = 0; i < SIZE && i < contents.size(); i++) this.stacks.set(i, contents.get(i));
    }

    /** Upstream {@code ItemStack.items}. */
    public static WalletInventory of(ItemStack stack) {
        return new WalletInventory(stack);
    }

    public boolean isFull() {
        for (ItemStack item : stacks) {
            if (item.isEmpty()) return false;
        }
        return true;
    }

    public boolean hasAnyItems() {
        for (ItemStack item : stacks) {
            if (!item.isEmpty()) return true;
        }
        return false;
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int i = 0; i < SIZE; i++) {
            remaining = insertItem(i, remaining, simulate);
            if (remaining.isEmpty()) return ItemStack.EMPTY;
        }
        return remaining;
    }

    @Override
    protected void onContentsChanged(int slot) {
        ModComponents.setWalletContents(stack, stacks);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return ModComponents.getPortalTarget(stack) != null && stack.getItem() == ModItems.locationCard.get();
    }

    @Override
    public Component getDisplayName() {
        return stack.getHoverName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new WalletMenu(containerId, playerInventory, this);
    }
}
