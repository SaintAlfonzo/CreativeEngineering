package com.SaintAlfonzo.creativedelight.compat.farmersrespite;

import net.minecraftforge.items.ItemStackHandler;

/** The Kettle's two transfer boxes (farmers_respite.md K4): slot 0 fill box, slot 1 empty box. */
public final class KettleBoxes extends ItemStackHandler {

	private final Runnable onChanged;

	public KettleBoxes(Runnable onChanged) {
		super(2);
		this.onChanged = onChanged;
	}

	@Override
	protected void onContentsChanged(int slot) {
		onChanged.run();
	}
}
