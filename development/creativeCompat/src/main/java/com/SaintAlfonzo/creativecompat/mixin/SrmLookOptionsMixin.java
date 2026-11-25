// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.SrmLookFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

/**
 * The "snowy look off" flag on Snow! Real Magic's render options ({@code SnowBlockEntity.Options}), ruling R19
 * (snow_real_magic.md 4c). The options object is the one SRM hands to its render hook in the block's model data, so the
 * client reads the flag where it draws. Gated on {@code snowrealmagic}.
 */
@Pseudo
@Mixin(targets = "snownee.snow.block.entity.SnowBlockEntity$Options", remap = false)
public abstract class SrmLookOptionsMixin implements SrmLookFlag {

	@Unique
	private boolean creativecompat$lookOff;

	@Override
	public boolean creativecompat$lookOff() {
		return creativecompat$lookOff;
	}

	@Override
	public void creativecompat$setLookOff(boolean off) {
		creativecompat$lookOff = off;
	}
}
