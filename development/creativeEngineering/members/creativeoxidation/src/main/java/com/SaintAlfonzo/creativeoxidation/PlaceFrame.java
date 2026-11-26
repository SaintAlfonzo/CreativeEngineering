// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import net.minecraft.core.BlockPos;

/**
 * One BlockItem.place call in BlockItemMixin: the stage read from the placed item and the position it really lands at.
 * Lives outside the mixin package because Mixin forbids loading any class from a mixin package directly.
 */
public final class PlaceFrame {
	public final OxidationItemData.StoredState stored;
	public BlockPos pos;

	public PlaceFrame(OxidationItemData.StoredState stored) {
		this.stored = stored;
	}
}
