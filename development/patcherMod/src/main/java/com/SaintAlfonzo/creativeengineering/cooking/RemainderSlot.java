package com.SaintAlfonzo.creativeengineering.cooking;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * A take-only menu slot for one {@link RemainderBuffer} slot. The stations put them in a column right of their output
 * slot ({@link #X}, {@link #y}), where the pot, kettle and oven textures are empty; the screens draw the frames
 * ({@code cooking.client.RemainderSlotFrames}). They are added after the player inventory, so the stations' own
 * slot index maths stay as they are.
 */
public class RemainderSlot extends SlotItemHandler {

    public static final int X = 150;
    private static final int TOP_Y = 37;

    public RemainderSlot(IItemHandler buffer, int index) {
        super(buffer, index, X, y(index));
    }

    public static int y(int index) {
        return TOP_Y + index * 18;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
    }
}
