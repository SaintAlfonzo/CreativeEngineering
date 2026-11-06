package ce.shared;

import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Moves a leftover waiting in a station's overflow slot into its container slot when it can plate the meal waiting in
 * the station (feature_writeup/remainder_buffer.md 3.8): only as many as the waiting meals still need (meals waiting
 * minus containers already in the slot), and only after the same candidate has waited {@code delay} ticks, so the step
 * can be seen. One instance per station block entity; not saved (a reload restarts the wait).
 * Shared source (development/shared/stations), compiled into each jar under its own package.
 */
public final class OverflowPull {

    /** The station's slots as this needs them. */
    public interface Slots {
        ItemStack get(int slot);

        void set(int slot, ItemStack stack);

        int limit(int slot);
    }

    private int wait = -1;
    private int waitingSlot = -1;

    /**
     * Call once a server tick. {@code containerSlot} and {@code overflowSlots} are indices into {@code slots};
     * {@code mealsWaiting} the count of the meal that waits for containers (0 = none); {@code platesMeal} whether an
     * item is what that meal is served in. Returns true on the tick it moved something.
     */
    public boolean tick(Slots slots, int containerSlot, int[] overflowSlots, int mealsWaiting, Predicate<ItemStack> platesMeal, int delay) {
        int from = -1;
        int count = 0;
        if (mealsWaiting > 0) {
            ItemStack inSlot = slots.get(containerSlot);
            for (int o : overflowSlots) {
                ItemStack over = slots.get(o);
                if (over.isEmpty() || !platesMeal.test(over)) continue;
                if (!inSlot.isEmpty() && !ItemHandlerHelper.canItemStacksStack(inSlot, over)) continue;
                int room = Math.min(slots.limit(containerSlot), over.getMaxStackSize()) - inSlot.getCount();
                int need = mealsWaiting - inSlot.getCount();
                count = Math.min(over.getCount(), Math.min(room, need));
                if (count > 0) {
                    from = o;
                    break;
                }
            }
        }
        if (from < 0) {
            wait = -1;
            waitingSlot = -1;
            return false;
        }
        if (waitingSlot != from || wait < 0) {
            waitingSlot = from;
            wait = Math.max(0, delay);
        }
        if (wait > 0) {
            --wait;
            return false;
        }
        ItemStack over = slots.get(from).copy();
        ItemStack inSlot = slots.get(containerSlot);
        ItemStack moved = ItemHandlerHelper.copyStackWithSize(over, inSlot.getCount() + count);
        over.shrink(count);
        slots.set(from, over.isEmpty() ? ItemStack.EMPTY : over);
        slots.set(containerSlot, moved);
        wait = -1;
        waitingSlot = -1;
        return true;
    }
}
