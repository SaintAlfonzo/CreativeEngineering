// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/menu/MetronomeMenu.kt (MetronomeMenu;
// MetronomeMenuData is its own file). Translated to Java for Forge 1.19.2: NeoForge SlotItemHandler / ItemStackHandler ->
// Forge's; the Optional content of MenuContentHelper -> the client constructor's Optional (read in ModMenus).
package earth.terrarium.tempad.common.menu;

import earth.terrarium.tempad.common.block.MetronomeItemHandler;
import earth.terrarium.tempad.common.registries.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MetronomeMenu extends AbstractContainerMenu {
    public final ContainerData energy;
    @Nullable
    public final MetronomeMenuData data;

    public MetronomeMenu(int id, Inventory inv, IItemHandler items, ContainerData energy, @Nullable MetronomeMenuData data) {
        super(ModMenus.metronome.get(), id);
        this.energy = energy;
        this.data = data;
        addMenuSlots(items, 25, 10);
        addPlayerInvSlots(inv, 25, 85);
        addDataSlots(energy);
    }

    /** Client side (upstream's secondary constructor). */
    public MetronomeMenu(int id, Inventory inv, Optional<MetronomeMenuData> data) {
        this(id, inv, new MetronomeItemHandler(), new SimpleContainerData(2), data.orElse(null));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (index < 8) {
                if (!this.moveItemStackTo(originalStack, 8, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(originalStack, 0, 8, false)) {
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
                this.addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
        for (int k = 0; k <= 8; k++) {
            this.addSlot(new Slot(inventory, k, x + k * 18, y + 18 * 3 + 4));
        }
    }

    private void addMenuSlots(IItemHandler items, int x, int y) {
        for (int slot = 0; slot <= 7; slot++) {
            this.addSlot(new SlotItemHandler(items, slot, x + (slot + (slot > 3 ? 1 : 0)) * 18, y + 30));
        }
    }
}
