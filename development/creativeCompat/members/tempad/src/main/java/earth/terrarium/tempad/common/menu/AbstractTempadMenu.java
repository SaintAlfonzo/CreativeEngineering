// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/menu/AbstractTempadMenu.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.menu;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.apps.AppContent;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AbstractTempadMenu<T extends AppContent<T>> extends AbstractContainerMenu {
    public final T appContent;
    public final ContextHolder<?> ctxHolder;
    public final SyncableContext<?> ctx;

    public AbstractTempadMenu(int id, Inventory inventory, @Nullable MenuType<?> type, T appContent) {
        super(type, id);
        this.appContent = appContent;
        this.addPlayerInvSlots(inventory, 48, 157);
        this.ctxHolder = appContent.ctx;
        this.ctx = ctxHolder.getCtx(inventory.player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private void addPlayerInvSlots(Inventory inventory, int x, int y) {
        for (int row = 0; row <= 2; row++) {
            for (int column = 0; column <= 8; column++) {
                this.addSlot(makeInvSlot(inventory, column + row * 9 + 9 /* Hotbar is the first 9 */, x + column * 18, y + row * 18));
            }
        }
        for (int k = 0; k <= 8; k++) {
            this.addSlot(makeInvSlot(inventory, k, x + k * 18, y + 18 * 3 + 4));
        }
    }

    private Slot makeInvSlot(Inventory inventory, int slotIndex, int x, int y) {
        return new LockedSlot(inventory, slotIndex, x, y);
    }

    protected void putBack(Player player, ItemStack stack) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isAlive() && !serverPlayer.hasDisconnected()) {
                serverPlayer.getInventory().placeItemBackInInventory(stack);
            } else {
                serverPlayer.drop(stack, false);
            }
        }
    }

    public static class LockedSlot extends Slot {
        public final Inventory inventory;

        public LockedSlot(Inventory inventory, int slotIndex, int x, int y) {
            super(inventory, slotIndex, x, y);
            this.inventory = inventory;
        }

        @Override
        public boolean mayPickup(Player player) {
            return inventory.getItem(getContainerSlot()).getItem() != ModItems.tempad.get();
        }
    }
}
