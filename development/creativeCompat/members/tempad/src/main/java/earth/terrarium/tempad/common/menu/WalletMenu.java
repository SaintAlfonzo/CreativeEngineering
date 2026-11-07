// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/menu/WalletMenu.kt. Translated to Java for Forge 1.19.2.
// Menu type tempad:wallet (ModMenus.wallet); the client copy is built over WalletInventory(ItemStack.EMPTY), as upstream.
package earth.terrarium.tempad.common.menu;

import earth.terrarium.tempad.common.items.WalletInventory;
import earth.terrarium.tempad.common.registries.ModItems;
import earth.terrarium.tempad.common.registries.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class WalletMenu extends AbstractContainerMenu {
    public WalletMenu(int containerId, Inventory inventory, WalletInventory items) {
        super(ModMenus.wallet.get(), containerId);
        addMenuSlots(items, 8, 19);
        addPlayerInvSlots(inventory, 8, 71);
    }

    public WalletMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new WalletInventory(ItemStack.EMPTY));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (index < 18) {
                if (!this.moveItemStackTo(originalStack, 18, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, 18, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private void addPlayerInvSlots(Inventory inventory, int x, int y) {
        for (int row = 0; row <= 2; row++) {
            for (int column = 0; column <= 8; column++) {
                this.addSlot(new LockedSlot(inventory, column + row * 9 + 9 /* Hotbar is the first 9 */, x + column * 18, y + row * 18));
            }
        }

        for (int k = 0; k <= 8; k++) {
            this.addSlot(new LockedSlot(inventory, k, x + k * 18, y + 18 * 3 + 4));
        }
    }

    private void addMenuSlots(ItemStackHandler items, int x, int y) {
        for (int row = 0; row <= 1; row++) {
            for (int column = 0; column <= 8; column++) {
                this.addSlot(new SlotItemHandler(items, column + row * 9, x + column * 18, y + row * 18));
            }
        }
    }

    public class LockedSlot extends Slot {
        public final Inventory inventory;

        public LockedSlot(Inventory inventory, int slotIndex, int x, int y) {
            super(inventory, slotIndex, x, y);
            this.inventory = inventory;
        }

        @Override
        public boolean mayPickup(Player player) {
            return inventory.getItem(getContainerSlot()).getItem() != ModItems.cardWallet.get();
        }
    }
}
