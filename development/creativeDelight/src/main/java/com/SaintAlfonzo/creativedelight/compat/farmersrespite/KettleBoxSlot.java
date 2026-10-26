package com.SaintAlfonzo.creativedelight.compat.farmersrespite;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

/**
 * One of the Kettle's transfer boxes in its menu (farmers_respite.md K4). Takes only what the box converts (so
 * shift-click and hoppers into the player inventory can't park other items there), and a player putting such an
 * item in latches the transfer to this box. The machine's own moves go through the handler, not the slot, so they
 * never flip it.
 */
public class KettleBoxSlot extends SlotItemHandler {

	public static final int X = 14;
	public static final int FILL_Y = 17;
	public static final int EMPTY_Y = 53;

	private final BlockEntity kettle;
	private final int box;

	public KettleBoxSlot(BlockEntity kettle, int box) {
		super(((KettleTankHolder) kettle).ce$boxes(), box, X, box == KettleTankHolder.FILL_BOX ? FILL_Y : EMPTY_Y);
		this.kettle = kettle;
		this.box = box;
	}

	public int box() {
		return box;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return KettleTank.boxAccepts(kettle, box, stack);
	}

	@Override
	public void set(ItemStack stack) {
		ItemStack before = getItem();
		super.set(stack);
		if (!stack.isEmpty() && mayPlace(stack)
				&& (stack.getCount() > before.getCount() || !ItemStack.isSameItemSameTags(stack, before))) {
			latch();
		}
	}

	@Override
	public ItemStack safeInsert(ItemStack stack, int amount) {
		int had = stack.getCount();
		ItemStack rest = super.safeInsert(stack, amount);
		if (rest.getCount() < had || rest.isEmpty() && had > 0) latch();
		return rest;
	}

	public void latch() {
		((KettleTankHolder) kettle).ce$latch(box);
	}
}
