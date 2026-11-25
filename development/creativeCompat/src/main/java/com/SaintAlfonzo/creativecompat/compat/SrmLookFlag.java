// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

/**
 * Snow! Real Magic's per-block render options ({@code SnowBlockEntity.Options}) carry the pack's "snowy look off" flag
 * (snow_real_magic.md ruling R19), added by {@code mixin.SrmLookOptionsMixin}. Saved with the block entity
 * ({@code mixin.SrmLookSaveMixin}) and read by the client render hook ({@code mixin.SrmRenderMixin}).
 */
public interface SrmLookFlag {

	boolean creativecompat$lookOff();

	void creativecompat$setLookOff(boolean off);
}
