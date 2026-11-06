package com.SaintAlfonzo.creativedelight.qol;

import com.SaintAlfonzo.creativedelight.CDConfig;

/**
 * QoL-1 switch for the cooking pot and kettle compartments (mixin.farmersdelight, mixin.farmersrespite;
 * creative_delight.md 8, feature_writeup/remainder_buffer.md). The buffer itself (storage, slots, output handler) is
 * patcherMod's {@code cooking} API, shared with patcherMod's own oven, whose switch stays
 * {@code creativedelight-common.toml [cooking] remainderBuffer}. The stall dial is shared: {@code [cooking]
 * stallWhenFull}.
 *
 * The switch only stops new leftovers going in; what a station already holds still shows, drops and can be pulled.
 */
public final class RemainderBufferQol {

	private RemainderBufferQol() {}

	public static boolean enabled() {
		return CDConfig.qol(CDConfig.REMAINDER_BUFFER);
	}

	public static boolean stalls() {
		return enabled() && CDConfig.COOKING.stallWhenFull.get();
	}
}
