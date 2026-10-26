package com.SaintAlfonzo.creativedelight.compat.farmersrespite;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * The Farmer's Respite Kettle's tank and transfer boxes, added by {@code mixin.farmersrespite.KettleTankMixin}
 * (farmers_respite.md 2.1). The level is the block's {@code water_level} (0-4); the fluid type is kept here.
 */
public interface KettleTankHolder {

	/** Fill box (index 0): drink bottles poured into the tank. Empty box (index 1): glass bottles filled from it. */
	int FILL_BOX = 0;
	int EMPTY_BOX = 1;

	/** The stored fluid with amount level x {@link KettleTank#UNIT}, EMPTY when the level is 0. */
	FluidStack ce$tankFluid();

	int ce$tankLevel();

	/** Sets type and level together; level 0 empties it. Syncs the block entity to clients. */
	void ce$setTank(FluidStack type, int level);

	ItemStackHandler ce$boxes();

	/** Direction of the box transfer: true pours the fill box into the tank, false bottles into the empty box. */
	boolean ce$pouring();

	/** A player put a valid item in box {@code box}: the transfer turns to that box. */
	void ce$latch(int box);
}
